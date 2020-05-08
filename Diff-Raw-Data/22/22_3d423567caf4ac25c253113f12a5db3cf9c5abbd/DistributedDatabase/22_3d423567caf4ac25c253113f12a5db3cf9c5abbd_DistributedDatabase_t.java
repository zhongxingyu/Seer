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
 import edu.berkeley.gamesman.core.Record;
 import edu.berkeley.gamesman.game.TieredGame;
import edu.berkeley.gamesman.hasher.TieredHasher;
 import edu.berkeley.gamesman.parallel.ErrorThread;
 import edu.berkeley.gamesman.parallel.TierSlave;
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
 	private int tier;
 	private int lastZippedTier = -1;
 	private boolean zipped = false;
 
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
 	public void getBytes(byte[] arr, int off, int len) {
 		curLoc = fetchBytes(curLoc, arr, off, len);
 	}
 
 	@Override
 	public void getBytes(long location, byte[] arr, int off, int len) {
 		fetchBytes(location, arr, off, len);
 	}
 
 	private long fetchBytes(long location, byte[] arr, int off, int len) {
 		int firstTier, lastTier;
 		TieredGame<?> g = null;
 		if (solve) {
 			firstTier = lastTier = tier;
 		} else {
 			g = (TieredGame<?>) conf.getGame();
 			firstTier = g.hashToTier(location / conf.recordGroupByteLength
 					* conf.recordsPerGroup);
 			lastTier = g.hashToTier((location + len)
 					/ conf.recordGroupByteLength * conf.recordsPerGroup - 1);
 		}
 		for (int tier = firstTier; tier <= lastTier; tier++) {
 			boolean combineFirst = !solve
 					&& g.hashOffsetForTier(tier) / conf.recordsPerGroup
 							* conf.recordGroupByteLength == location;
 			try {
 				String result;
 				if (solve) {
 					synchronized (this) {
 						masterWrite.println("fetch files: " + location + " "
 								+ len);
 						result = scan.nextLine();
 					}
				} else {
 					result = getFileList(files.get(tier), location, len);
				}
 				String[] nodeFiles = result.split(" ");
 				String[] nodeFile = nodeFiles[0].split(":");
 				String[] nextNodeFile = null;
 				long fileStart = Long.parseLong(nodeFile[1]);
 				long nextStart = 0;
 				long fileLoc = location - fileStart + 4;
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
					} else if (tier == lastTier) {
 						nextStart = location + len;
					} else {
						TieredHasher<?> h = (TieredHasher<?>) conf.getHasher();
						nextStart = (g.hashOffsetForTier(tier)
								+ h.numHashesForTier(tier)
								+ conf.recordsPerGroup - 1)
								/ conf.recordsPerGroup
								* conf.recordGroupByteLength;
					}
 					if (lastZippedTier >= 0 && tier >= lastZippedTier
 							|| (zipped && TierSlave.jobFile != null)) {
 						String gamesmanPath = conf.getProperty("gamesman.path");
 						StringBuilder sb = new StringBuilder("ssh ");
 						sb.append(nodeFile[0]);
 						sb.append(" java -cp ");
 						sb.append(gamesmanPath);
 						sb.append(File.separator);
 						sb
 								.append("bin edu.berkeley.gamesman.parallel.ReadZippedBytes ");
 						sb.append(TierSlave.jobFile);
 						sb.append(" ");
 						sb.append(tier);
 						sb.append(" ");
 						sb.append(nodeFile[1]);
 						sb.append(" ");
 						sb.append(location);
 						sb.append(" ");
 						sb.append(nextStart - location);
 						Process p = r.exec(sb.toString());
 						InputStream byteReader = p.getInputStream();
 						new ErrorThread(p.getErrorStream(), nodeFile[0] + ":"
 								+ nodeFile[1]).start();
 						while (location < nextStart) {
 							if (combineFirst) {
 								arr[off++] += byteReader.read();
 								location++;
 								len--;
 							}
 							int bytesRead = byteReader.read(arr, off,
 									(int) (nextStart - location));
 							if (bytesRead == -1)
 								Util.fatalError(nodeFile[0] + ":" + nodeFile[1]
 										+ ": No more bytes available");
 							location += bytesRead;
 							off += bytesRead;
 							len -= bytesRead;
 						}
 					} else if (zipped) {
 						GZippedFileDatabase myZipBase = new GZippedFileDatabase();
 						myZipBase.initialize(nodeFile[0] + ":" + parentPath
 								+ "t" + tier + File.separator + "s"
 								+ nodeFile[1] + ".db.gz", conf, false);
 						myZipBase.getBytes(location - fileStart, arr, off,
 								(int) (nextStart - location));
 						off += nextStart - location;
 						len -= nextStart - location;
 						location = nextStart;
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
 						if (tier == lastTier && i == nodeFiles.length - 1) {
 							sb.append(" count=");
 							sb.append((len + skipBytes + 511) >> 9);
 						}
 						sb.append("\n");
 						Process p = r.exec(sb.toString());
 						InputStream byteReader = p.getInputStream();
 						while (skipBytes > 0) {
 							int bytesRead = byteReader.read(dumbArray, 0,
 									skipBytes);
 							if (bytesRead == -1)
 								Util.fatalError(nodeFile[0] + ":" + nodeFile[1]
 										+ ": No more bytes available");
 							skipBytes -= bytesRead;
 						}
 						skipBytes = 4;
 						while (location < nextStart) {
 							int bytesRead = byteReader.read(arr, off,
 									(int) (nextStart - location));
 							if (bytesRead == -1)
 								Util.fatalError(nodeFile[0] + ":" + nodeFile[1]
 										+ ": No more bytes available");
 							location += bytesRead;
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
 		return location;
 	}
 
 	@Override
 	public void getRecord(long recordIndex, Record r) {
 		if (!solve)
 			setTier(((TieredGame<?>) conf.getGame()).hashToTier(recordIndex));
 		super.getRecord(recordIndex, r);
 	}
 
 	@Override
 	public void initialize(String uri, boolean solve) {
 		this.solve = solve;
 		if (solve) {
 			parentPath = uri + File.separator;
 			lastZippedTier = conf.getInteger("gamesman.db.lastZippedTier", -1);
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
 			String gamesmanPath = conf.getProperty("gamesman.path");
 			TierSlave.jobFile = gamesmanPath + File.separator
 					+ conf.getProperty("gamesman.confFile", null);
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
 	 * @param i
 	 *            the index into files
 	 * @return files.get(i);
 	 */
 	public ArrayList<Pair<Long, String>> getFiles(int i) {
 		return files.get(i);
 	}
 }
