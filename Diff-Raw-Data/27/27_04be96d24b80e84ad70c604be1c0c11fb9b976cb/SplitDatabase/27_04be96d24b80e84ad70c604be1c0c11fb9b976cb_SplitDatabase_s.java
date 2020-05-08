 package edu.berkeley.gamesman.database;
 
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 import edu.berkeley.gamesman.core.Configuration;
 
 public class SplitDatabase extends Database {
 	private final Database[] databaseList;
 	private final long[] firstByteIndices;
 	private final int[] firstNums;
 	private final long[] lastByteIndices;
 	private final int[] lastNums;
 	private final String uri;
 	private final ArrayList<String> dbTypeList;
 	private final ArrayList<String> uriList;
 	private final ArrayList<Long> firstRecordList;
 	private final ArrayList<Long> numRecordsList;
 
 	public SplitDatabase(String uri, Configuration conf, boolean solve,
 			long firstRecord, long numRecords, DatabaseHeader header) {
 		this(uri, conf, solve, firstRecord, numRecords, header, null);
 	}
 
 	public SplitDatabase(String uri, Configuration conf, boolean solve,
 			long firstRecord, long numRecords, DatabaseHeader header,
 			InputStream dbStream) {
 		super(uri, conf, solve, firstRecord, numRecords,
 				header == null ? getSplitHeader(uri) : header);
 		try {
 			final boolean closeAfter;
 			if (dbStream == null) {
 				if (uri == null)
 					uri = conf.getProperty("gamesman.db.uri");
 				dbStream = new FileInputStream(uri);
 				skipHeader(dbStream);
 				closeAfter = false;
 			} else
 				closeAfter = true;
 			this.uri = uri;
 			ArrayList<Database> databaseList = new ArrayList<Database>();
 			Scanner scan = new Scanner(dbStream);
 			String dbType = scan.next();
 			while (!dbType.equals("end")) {
 				String dbUri = scan.next();
 				long dbFirstRecord = scan.nextLong();
 				long dbNumRecords = scan.nextLong();
 				databaseList.add(Database.openDatabase(dbType, dbUri, conf,
 						solve, dbFirstRecord, dbNumRecords, getHeader(
 								dbFirstRecord, dbNumRecords)));
 				dbType = scan.next();
 			}
 			if (closeAfter)
 				scan.close();
 			this.databaseList = databaseList.toArray(new Database[databaseList
 					.size()]);
 			firstByteIndices = new long[databaseList.size()];
 			firstNums = new int[databaseList.size()];
 			lastByteIndices = new long[databaseList.size()];
 			lastNums = new int[databaseList.size()];
 			for (int i = 0; i < lastByteIndices.length; i++) {
 				Database db = this.databaseList[i];
 				long dbFirstRecord = db.firstRecord();
 				firstByteIndices[i] = toByte(dbFirstRecord);
 				firstNums[i] = toNum(dbFirstRecord);
 				long dbLastRecord = dbFirstRecord + db.numRecords();
 				lastByteIndices[i] = lastByte(dbLastRecord);
 				lastNums[i] = toNum(dbLastRecord);
 			}
 			dbTypeList = null;
 			uriList = null;
 			firstRecordList = null;
 			numRecordsList = null;
 		} catch (IOException e) {
 			throw new Error(e);
 		}
 	}
 
 	private DatabaseHeader getHeader(long dbFirstRecord, long dbNumRecords) {
 		if (superCompress)
 			return new DatabaseHeader(dbFirstRecord, dbNumRecords,
 					recordsPerGroup, recordGroupByteLength);
 		else
 			return new DatabaseHeader(dbFirstRecord, dbNumRecords,
 					recordGroupByteBits);
 	}
 
 	public SplitDatabase(String uri, Configuration conf, long firstRecord,
 			long numRecords) {
 		super(uri, conf, true, firstRecord, numRecords, null);
 		if (uri == null)
 			uri = conf.getProperty("gamesman.db.uri");
 		this.uri = uri;
 		databaseList = null;
 		firstByteIndices = null;
 		firstNums = null;
 		lastByteIndices = null;
 		lastNums = null;
 		dbTypeList = new ArrayList<String>();
 		uriList = new ArrayList<String>();
 		firstRecordList = new ArrayList<Long>();
 		numRecordsList = new ArrayList<Long>();
 	}
 
 	public SplitDatabase(String uri, Configuration conf) {
 		this(uri, conf, 0, -1);
 	}
 
 	private static DatabaseHeader getSplitHeader(String uri) {
 		try {
 			FileInputStream fis = new FileInputStream(uri);
 			byte[] headBytes = new byte[18];
 			readFully(fis, headBytes, 0, 18);
 			fis.close();
 			return new DatabaseHeader(headBytes);
 		} catch (IOException e) {
 			throw new Error(e);
 		}
 	}
 
 	@Override
 	protected void closeDatabase() {
 		if (dbTypeList == null)
 			for (Database d : databaseList)
 				d.close();
 		else {
 			try {
 				FileOutputStream fos = new FileOutputStream(uri);
 				store(fos);
 				PrintStream myStream = new PrintStream(fos);
 				myStream.println();
 				for (int i = 0; i < dbTypeList.size(); i++) {
 					myStream.println(dbTypeList.get(i) + " " + uriList.get(i)
 							+ " " + firstRecordList.get(i) + " "
 							+ numRecordsList.get(i));
 				}
 				myStream.println("end");
 				myStream.close();
 			} catch (IOException ioe) {
 				throw new Error(ioe);
 			}
 		}
 	}
 
 	@Override
 	protected void getBytes(DatabaseHandle dh, long loc, byte[] arr, int off,
 			int len) {
 		getRecordsAsBytes(dh, loc, 0, arr, off, len, 0, true);
 	}
 
 	@Override
 	protected void putBytes(DatabaseHandle dh, long loc, byte[] arr, int off,
 			int len) {
 		putRecordsAsBytes(dh, loc, 0, arr, off, len, 0, true);
 	}
 
 	@Override
 	protected void prepareRange(DatabaseHandle dh, long byteIndex,
 			int firstNum, long numBytes, int lastNum) {
 		super.prepareRange(dh, byteIndex, firstNum, numBytes, lastNum);
 		SplitHandle sh = (SplitHandle) dh;
 		sh.currentDb = binSearch(byteIndex, firstNum);
 		long firstNumBytes = lastByteIndices[sh.currentDb] - byteIndex;
 		int firstLastNum;
 		if (numBytes < firstNumBytes) {
 			firstNumBytes = numBytes;
 			firstLastNum = lastNum;
 		} else if (numBytes > firstNumBytes) {
 			firstLastNum = lastNums[sh.currentDb];
 		} else {
 			firstLastNum = Math.min(lastNum, lastNums[sh.currentDb]);
 		}
 		databaseList[sh.currentDb].prepareRange(sh.handles[sh.currentDb],
 				byteIndex, firstNum, firstNumBytes, firstLastNum);
 		sh.dbLoc[sh.currentDb] = byteIndex;
 	}
 
 	@Override
 	protected int getBytes(DatabaseHandle dh, byte[] arr, int off, int maxLen,
 			boolean overwriteEdgesOk) {
 		if (!overwriteEdgesOk) {
 			return super.getBytes(dh, arr, off, maxLen, false);
 		}
 		final int numBytes = (int) Math.min(dh.lastByteIndex - dh.location,
 				maxLen);
 		SplitHandle sh = (SplitHandle) dh;
 		while (sh.location >= lastByteIndices[sh.currentDb])
 			sh.dbLoc[sh.currentDb++] = -1;
 		int db = sh.currentDb;
 		while (db < firstByteIndices.length
 				&& firstByteIndices[db] < sh.location + numBytes) {
 			if (sh.dbLoc[db] < 0) {
				long lastByteIndex = lastByteIndices[sh.currentDb];
 				int lastNum;
 				if (lastByteIndex > sh.lastByteIndex) {
 					lastByteIndex = sh.lastByteIndex;
 					lastNum = sh.lastNum;
 				} else if (lastByteIndex < sh.lastByteIndex) {
					lastNum = lastNums[sh.currentDb];
 				} else
					lastNum = Math.min(sh.lastNum, lastNums[sh.currentDb]);
 				if (firstByteIndices[db] < lastByteIndex
 						- recordGroupByteLength
 						|| lastNum == 0 || firstNums[db] < lastNum) {
 					databaseList[db].prepareRange(sh.handles[db],
 							firstByteIndices[db], firstNums[db], lastByteIndex
 									- firstByteIndices[db], lastNum);
 					sh.dbLoc[db] = firstByteIndices[db];
 					if (sh.dbLoc[db] < sh.location) {
 						byte[] skipBytes = dh.getRecordGroupBytes();
 						sh.dbLoc[db] += databaseList[db].getBytes(
 								sh.handles[db], skipBytes, 0,
 								(int) (sh.location - sh.dbLoc[db]), true);
 						dh.releaseBytes(skipBytes);
 					}
 				} else
 					break;
 			}
 			sh.dbLoc[db] += databaseList[db].getBytes(sh.handles[db], arr, off
 					+ (int) (sh.dbLoc[db] - sh.location), numBytes
 					- (int) (sh.dbLoc[db] - sh.location), false);
 			db++;
 		}
 		sh.location += numBytes;
 		if (sh.lastByteIndex == sh.location) {
 			for (int i = sh.currentDb; i < sh.dbLoc.length && sh.dbLoc[i] >= 0; i++)
 				sh.dbLoc[i] = -1;
 		}
 		return numBytes;
 	}
 
 	@Override
 	protected int putBytes(DatabaseHandle dh, byte[] arr, int off, int maxLen,
 			boolean edgesAreCorrect) {
 		if (!edgesAreCorrect) {
 			return super.putBytes(dh, arr, off, maxLen, false);
 		}
 		final int numBytes = (int) Math.min(dh.lastByteIndex - dh.location,
 				maxLen);
 		SplitHandle sh = (SplitHandle) dh;
 		while (sh.location >= lastByteIndices[sh.currentDb])
 			sh.dbLoc[sh.currentDb++] = -1;
 		int db = sh.currentDb;
 		while (db < firstByteIndices.length
 				&& firstByteIndices[db] < sh.location + numBytes) {
 			if (sh.dbLoc[db] < 0) {
				long lastByteIndex = lastByteIndices[sh.currentDb];
 				int lastNum;
 				if (lastByteIndex > sh.lastByteIndex) {
 					lastByteIndex = sh.lastByteIndex;
 					lastNum = sh.lastNum;
 				} else if (lastByteIndex < sh.lastByteIndex) {
					lastNum = lastNums[sh.currentDb];
 				} else
					lastNum = Math.min(sh.lastNum, lastNums[sh.currentDb]);
 				if (firstByteIndices[db] < lastByteIndex
 						- recordGroupByteLength
 						|| lastNum == 0 || firstNums[db] < lastNum) {
 					databaseList[db].prepareRange(sh.handles[db],
 							firstByteIndices[db], firstNums[db], lastByteIndex
 									- firstByteIndices[db], lastNum);
 					sh.dbLoc[db] = firstByteIndices[db];
 					if (sh.dbLoc[db] < sh.location) {
 						byte[] skipBytes = dh.getRecordGroupBytes();
 						sh.dbLoc[db] += databaseList[db].getBytes(
 								sh.handles[db], skipBytes, 0,
 								(int) (sh.location - sh.dbLoc[db]), true);
 						dh.releaseBytes(skipBytes);
 					}
 				} else
 					break;
 			}
 			sh.dbLoc[db] += databaseList[db].putBytes(sh.handles[db], arr, off
 					+ (int) (sh.dbLoc[db] - sh.location), numBytes
 					- (int) (sh.dbLoc[db] - sh.location), false);
 			db++;
 		}
 		sh.location += numBytes;
 		if (sh.lastByteIndex == sh.location) {
 			for (int i = sh.currentDb; i < sh.dbLoc.length && sh.dbLoc[i] >= 0; i++)
 				sh.dbLoc[i] = -1;
 		}
 		return numBytes;
 	}
 
 	private int binSearch(long byteIndex, int num) {
 		int low = 0, high = databaseList.length;
 		while (high - low > 1) {
 			int guess = (low + high) >>> 1;
 			if (firstByteIndices[guess] > byteIndex
 					|| (firstByteIndices[guess] == byteIndex && firstNums[guess] > num))
 				high = guess;
 			else
 				low = guess;
 		}
 		return low;
 	}
 
 	private class SplitHandle extends DatabaseHandle {
 		public final DatabaseHandle[] handles;
 		public int currentDb;
 		public final long[] dbLoc;
 
 		public SplitHandle(int recordGroupByteLength) {
 			super(recordGroupByteLength);
 			handles = new DatabaseHandle[databaseList.length];
 			dbLoc = new long[databaseList.length];
 			for (int i = 0; i < handles.length; i++) {
 				handles[i] = databaseList[i].getHandle();
 				dbLoc[i] = -1;
 			}
 		}
 	}
 
 	public static void main(String[] args) throws ClassNotFoundException {
 		String confFile = args[0];
 		String dbFile = args[1];
 		SplitDatabase sd;
 		int start;
 		if ((args.length & 3) == 2) {
 			sd = new SplitDatabase(dbFile, new Configuration(confFile));
 			start = 2;
 		} else {
 			sd = new SplitDatabase(dbFile, new Configuration(confFile), Long
 					.parseLong(args[2]), Long.parseLong(args[3]));
 			start = 4;
 		}
 		for (int i = start; i < args.length; i += 4) {
 			sd.addDatabase(args[i], args[i + 1], Long.parseLong(args[i + 2]),
 					Long.parseLong(args[i + 3]));
 		}
 		sd.close();
 	}
 
 	private void addDatabase(String dbType, String uri, long firstRecord,
 			long numRecords) {
 		dbTypeList.add(dbType);
 		uriList.add(uri);
 		firstRecordList.add(firstRecord);
 		numRecordsList.add(numRecords);
 	}
 
 	@Override
 	public DatabaseHandle getHandle() {
 		return new SplitHandle(recordGroupByteLength);
 	}
 }
