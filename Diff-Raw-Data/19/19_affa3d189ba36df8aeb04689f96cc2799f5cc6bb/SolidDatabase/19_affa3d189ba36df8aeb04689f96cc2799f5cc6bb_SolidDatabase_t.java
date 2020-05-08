 package edu.berkeley.gamesman.database;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.zip.GZIPInputStream;
 import java.util.zip.GZIPOutputStream;
 
 import edu.berkeley.gamesman.core.Configuration;
 import edu.berkeley.gamesman.core.Database;
 import edu.berkeley.gamesman.util.Util;
 
 /**
  * Database that does nor permit seeking without being entirely read into memory.
  * (Used for Hadoop or GZIP).
  * 
  * @author Patrick Horn
  */
 public abstract class SolidDatabase extends Database {
 
 	private String uri;
 	
 	protected OutputStream outputStream;
 
 	/** Set to false if you want an empty configuration header
 	 * (used for SplitDatabase.) Make sure to set 'conf' yourself.
 	 */
 	public boolean storeConfiguration = true;
 
 	/** Default constructor */
 	SolidDatabase() {
 	}
 	
 	@Override
 	public void close() {
 		if (outputStream != null) {
 			try {
 				outputStream.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			outputStream = null;
 		}
 	}
 
 	@Override
 	public void flush() {
 		try {
 			outputStream.flush();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	protected abstract InputStream openInputStream(String uri) throws IOException;
 	protected abstract OutputStream openOutputStream(String uri) throws IOException;
 
 	private List<byte[]> initializeReadDatabase() throws IOException {
 		List<byte[]> readContents = new ArrayList<byte[]>();
 		if (true) {
 		}
 		return readContents;
 	}
 	
 	@Override
 	public void getBytes(byte[] arr, int off, int len) {
 		throw new UnsupportedOperationException("getBytes without position argument is not supported in SolidDatabase");
 	}
 
 	@Override
 	public void getBytes(long loc, byte[] arr, int off, int len) {
 		byte[]decompressed = new byte[1000000];
 		long currentLoc = 0;
 		int decompressedOffset=0;
 		int lenToCopy=0;
 		int count=-999;
 		try {
 			InputStream inputStream = openInputStream(this.getUri());
 			{
 				byte[] tmpArray = new byte[4];
 				inputStream.read(tmpArray);
 				int conflen = ByteBuffer.wrap(tmpArray).getInt();
 				byte[] storedConf = new byte[conflen];
 				inputStream.read(storedConf);
 				if (conf == null) {
 					try {
 						conf = Configuration.load(storedConf);
 					} catch (ClassNotFoundException e) {
 						Util.fatalError("Failed to read header from SolidDatabase", e);
 					}
 				}
 			}
 			String compression = conf.getProperty("gamesman.db.compression", null);
 			if (compression != null) {
 				if (compression.equals("gzip")) {
 					inputStream = new GZIPInputStream(inputStream);
 				} else {
 					Util.fatalError("Unknown compression method '"+compression+"'");
 				}
 			}
 
 			while (len > 0) {
 				count = inputStream.read(decompressed, 0, decompressed.length);
 				if (count <= 0) {
 					break;
 				}
 				if (currentLoc+count > loc && len > 0) {
 					decompressedOffset = (int)(loc-currentLoc);
 					lenToCopy = count - decompressedOffset;
 					if (lenToCopy > len) {
 						lenToCopy = len;
 					}
 					System.arraycopy(arr, off,
 						decompressed, decompressedOffset,
 						lenToCopy);
 					len -= lenToCopy;
 					off += lenToCopy;
					loc += lenToCopy;
 				}
 				currentLoc += count;
 			}
 			if (currentLoc <= 0) {
 				throw new IOException("SolidDatabase at "+this.getUri()+" appears to be empty");
 			}
 			if (len > 0) {
 				throw new IOException("Failed to read enough from SolidDatabase "+
 					this.getUri()+"; "+len+" remaining");
 			}
 		} catch (IOException ie) {
 			System.out.println("[SolidDatabase] IOException in getBytes"+ie);
 			throw new RuntimeException(ie);
 		} catch (ArrayIndexOutOfBoundsException e) {
 			System.out.println("[SolidDatabase] Out of bounds in arrayCopy: "+
 				"input len="+decompressed.length+" off="+decompressedOffset+
 				"; file curLoc="+currentLoc+" count="+count+
 				"; output len="+arr.length+" off="+off+" ... LEN="+lenToCopy);
 			throw e;
 		}
 	}
 
 	@Override
 	public void initialize(String uri) {
 		this.uri = uri;
 	}
 
 	@Override
 	public synchronized void putBytes(byte[] arr, int off, int len) {
 		try {
 			if (outputStream == null) {
 				outputStream = this.openOutputStream(this.getUri());
 				String compression = conf.getProperty("gamesman.db.compression", null);
 				if (compression != null) {
 					if (compression.equals("gzip")) {
 					} else {
 						conf.deleteProperty("gamesman.db.compression");
 					}
 				}
 				byte[] storedConf;
 				if (storeConfiguration) {
 					storedConf = conf.store();
 				} else {
 					storedConf = new byte[0];
 				}
 				byte[] tmpArray = new byte[4];
 				ByteBuffer.wrap(tmpArray).putInt(storedConf.length);
 				outputStream.write(tmpArray);
 				if (compression != null) {
 					if (compression.equals("gzip")) {
 						outputStream = new GZIPOutputStream(outputStream);
 					}
 				}
 				outputStream.write(storedConf);
 			}
 			outputStream.write(arr, off, len);
 		} catch (IOException e) {
 			Util.fatalError("Failed to write "+len+"bytes to SolidDatabase", e);
 		}
 	}
 
 	@Override
 	public void seek(long loc) {
 		throw new UnsupportedOperationException("Seeking is not supported in SolidDatabase");
 	}
 
 	/**
 	 * @return The filename that was originally passed into initialize().
 	 */
 	public String getUri() {
 		return uri;
 	}
 
 }
