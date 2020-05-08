 package edu.berkeley.gamesman.database;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 import edu.berkeley.gamesman.core.Configuration;
 import edu.berkeley.gamesman.core.Database;
 import edu.berkeley.gamesman.util.Pair;
 import edu.berkeley.gamesman.util.Util;
 
 /**
  * A class for reading records (via ssh/dd) from a bunch of different files
  * distributed across multiple machines.<br />
  * WARNING! Does not work on Windows (Run on Cygwin)
  * 
  * @author dnspies
  */
 public class DistributedDatabase extends Database {
 	private final Scanner scan;
 	private final PrintStream masterWrite;
 	private long curLoc;
 	private Runtime r = Runtime.getRuntime();
 	private byte[] dumbArray = new byte[512];
 	private String parentPath;
 	private ArrayList<ArrayList<Pair<Long, String>>> files;
 	private boolean solve;
 	private boolean zipped = false;
 	private int tier;
 
 	/**
 	 * Default constructor (For read-only configuration)
 	 */
 	public DistributedDatabase() {
 		scan = null;
 		masterWrite = null;
 	}
 
 	/**
 	 * Solving constructor
 	 * 
 	 * @param masterRead
 	 *            The input stream which tells hosts and starting values
 	 * @param masterWrite
 	 *            The output stream to send name requests
 	 */
 	public DistributedDatabase(InputStream masterRead, PrintStream masterWrite) {
 		scan = new Scanner(masterRead);
 		this.masterWrite = masterWrite;
 	}
 
 	@Override
 	public void close() {
 		if (solve) {
 			scan.close();
 			masterWrite.close();
 		}
 	}
 
 	@Override
 	public void flush() {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public synchronized void getBytes(byte[] arr, int off, int len) {
 		try {
 			String result;
 			if (solve) {
 				masterWrite.println("fetch files: " + curLoc + " " + len);
 				result = scan.nextLine();
 			} else {
 				result = getFileList(files.get(tier), curLoc, len);
 			}
 			String[] nodeFiles = result.split(" ");
 			String[] nodeFile = nodeFiles[0].split(":");
 			String[] nextNodeFile = null;
 			long fileStart = Long.parseLong(nodeFile[1]);
 			long nextStart = 0;
 			long fileLoc = curLoc - fileStart + 4;
 			long fileBlocks = fileLoc >> 9;
 			int skipBytes = (int) (fileLoc & 511L);
 			for (int i = 0; i < nodeFiles.length; i++) {
 				if (i > 0) {
 					nodeFile = nextNodeFile;
 					fileStart = nextStart;
 				}
 				if (i < nodeFiles.length - 1) {
 					nextNodeFile = nodeFiles[i + 1].split(":");
 					nextStart = Long.parseLong(nextNodeFile[1]);
 				} else {
 					nextStart = curLoc + len;
 				}
 				if (zipped) {
 					GZippedFileDatabase myZipBase = new GZippedFileDatabase();
 					myZipBase.initialize(nodeFile[0] + ":" + parentPath + "t"
 							+ tier + File.separator + "s" + nodeFile[1]
 							+ ".db.gz", conf, false);
 					myZipBase.getBytes(curLoc - fileStart, arr, off,
 							(int) (nextStart - curLoc));
 					off += nextStart - curLoc;
 					len -= nextStart - curLoc;
 					curLoc = nextStart;
 				} else {
 					StringBuilder sb = new StringBuilder("ssh ");
 					sb.append(nodeFile[0]);
 					sb.append(" dd if=");
 					sb.append(parentPath);
 					sb.append("t");
 					sb.append(tier);
 					sb.append(File.separator);
 					sb.append("s");
 					sb.append(nodeFile[1]);
 					sb.append(".db");
 					if (i == 0 && fileBlocks > 0) {
 						sb.append(" skip=");
 						sb.append(fileBlocks);
 					}
 					if (i == nodeFiles.length - 1) {
 						sb.append(" count=");
 						sb.append((len + skipBytes + 511) >> 9);
 					}
 					sb.append("\n");
 					Process p = r.exec(sb.toString());
 					InputStream byteReader = p.getInputStream();
 					while (skipBytes > 0) {
 						int bytesRead = byteReader
 								.read(dumbArray, 0, skipBytes);
 						if (bytesRead == -1)
							Util.fatalError("No more bytes available");
 						skipBytes -= bytesRead;
 					}
 					skipBytes = 4;
 					while (curLoc < nextStart) {
 						int bytesRead = byteReader.read(arr, off,
 								(int) (nextStart - curLoc));
 						if (bytesRead == -1)
							Util.fatalError("No more bytes available");
 						curLoc += bytesRead;
 						off += bytesRead;
 						len -= bytesRead;
 					}
 					byteReader.close();
 				}
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void initialize(String uri, boolean solve) {
 		this.solve = solve;
 		if (solve) {
 			parentPath = uri + File.separator;
 		} else {
 			try {
 				files = new ArrayList<ArrayList<Pair<Long, String>>>();
 				FileInputStream fis = new FileInputStream(uri);
 				int fileLength = 0;
 				for (int i = 24; i >= 0; i -= 8) {
 					fileLength <<= 8;
 					fileLength |= fis.read();
 				}
 				byte[] cBytes = new byte[fileLength];
 				fis.read(cBytes);
 				if (conf == null) {
 					conf = Configuration.load(cBytes);
 					conf.db = this;
 				}
 				Scanner scan = new Scanner(fis);
 				scan.nextLine();
 				while (scan.hasNext())
 					files.add(0, parseArray(scan.nextLine()));
 			} catch (IOException e) {
 				e.printStackTrace();
 			} catch (ClassNotFoundException e) {
 				e.printStackTrace();
 			}
 			parentPath = conf.getProperty("gamesman.slaveDbFolder")
 					+ File.separator;
 			zipped = conf.getProperty("gamesman.db.compression", "none")
 					.equals("gzip");
 		}
 	}
 
 	/**
 	 * @param line
 	 *            The toString print-out of this ArrayList
 	 * @return The ArrayList
 	 */
 	public static ArrayList<Pair<Long, String>> parseArray(String line) {
 		line = line.substring(1, line.length() - 1);
 		String[] els = line.split(", ");
 		ArrayList<Pair<Long, String>> result = new ArrayList<Pair<Long, String>>(
 				els.length);
 		for (int i = 0; i < els.length; i++) {
 			els[i] = els[i].substring(1, els[i].length() - 1);
 			int split = els[i].indexOf(".");
 			Long resLong = new Long(els[i].substring(0, split));
 			String resNode = els[i].substring(split + 1);
 			result.add(new Pair<Long, String>(resLong, resNode));
 		}
 		return result;
 	}
 
 	@Override
 	public void putBytes(byte[] arr, int off, int len) {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public void seek(long loc) {
 		curLoc = loc;
 	}
 
 	/**
 	 * Sets the tier for read-only mode. This avoids group conflicts at tier
 	 * edges. When in read-only mode, make sure call this before calling
 	 * getBytes
 	 * 
 	 * @param tier
 	 *            The tier
 	 */
 	public void setTier(int tier) {
 		this.tier = tier;
 	}
 
 	/**
 	 * @param tierFiles
 	 *            A list of pairs of the byte of the first position in a file
 	 *            followed by the host it's on for this entire tier
 	 * @param byteNum
 	 *            The byte to start from
 	 * @param len
 	 *            The total number of bytes
 	 * @return A string with a list of only the necessary files for solving the
 	 *         given range (separated by spaces. host/starting position
 	 *         separated by ':')
 	 */
 	public static String getFileList(ArrayList<Pair<Long, String>> tierFiles,
 			long byteNum, int len) {
 		int low = 0, high = tierFiles.size();
 		int guess = (low + high) / 2;
 		while (high - low > 1) {
 			if (tierFiles.get(guess).car < byteNum) {
 				low = guess;
 			} else if (tierFiles.get(guess).car > byteNum) {
 				high = guess;
 			} else {
 				low = guess;
 				break;
 			}
 			guess = (low + high) / 2;
 		}
 		guess = low;
 		long end = byteNum + len;
 		Pair<Long, String> p = tierFiles.get(guess);
 		String s = p.cdr + ":" + p.car;
 		for (guess++; guess < tierFiles.size()
 				&& (p = tierFiles.get(guess)).car < end; guess++)
 			s += " " + p.cdr + ":" + p.car;
 		return s;
 	}
 
 	/**
 	 * Just for testing
 	 * 
	 * @param i the index into files
 	 * @return files.get(i);
 	 */
 	public ArrayList<Pair<Long, String>> getFiles(int i) {
 		return files.get(i);
 	}
 }
