 package edu.berkeley.gamesman.database;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.lang.reflect.InvocationTargetException;
 import java.math.BigInteger;
 
 import edu.berkeley.gamesman.core.Configuration;
 import edu.berkeley.gamesman.util.DebugFacility;
 import edu.berkeley.gamesman.util.Pair;
 import edu.berkeley.gamesman.util.Util;
 
 /**
  * A Database is the abstract superclass of all data storage methods used in
  * Gamesman. Each particular Database is responsible for the persistent storage
  * of Records derived from solving games.
  */
 public abstract class Database {
 
 	protected final Configuration conf;
 
 	protected final boolean solve;
 
 	private final long numRecords;
 
 	protected final long numContainedRecords;
 
 	private final long firstRecord;
 
 	protected final long firstContainedRecord;
 
 	protected final long totalStates;
 
 	protected final BigInteger bigIntTotalStates;
 
 	protected final BigInteger[] multipliers;
 
 	protected final long[] longMultipliers;
 
 	protected final int recordsPerGroup;
 
 	protected final int recordGroupByteLength;
 
 	protected final int recordGroupByteBits;
 
 	protected final boolean superCompress;
 
 	protected final boolean recordGroupUsesLong;
 
 	protected Database(String uri, Configuration conf, boolean solve,
 			long firstRecord, long numRecords, DatabaseHeader header) {
 		byte[] dbInfo = null;
 		if (numRecords == -1) {
 			firstRecord = 0;
 			numRecords = conf.getGame().numHashes();
 		}
 		if (header == null)
 			if (!solve)
 				try {
 					FileInputStream fis = new FileInputStream(uri);
 					dbInfo = new byte[18];
 					fis.read(dbInfo);
 					if (conf == null)
 						conf = Configuration.load(fis);
 					fis.close();
 					header = new DatabaseHeader(dbInfo);
 				} catch (ClassNotFoundException e) {
 					throw new Error(e);
 				} catch (IOException e) {
 					throw new Error(e);
 				}
 		this.conf = conf;
 		this.solve = solve;
 		totalStates = conf.getGame().recordStates();
 		bigIntTotalStates = BigInteger.valueOf(totalStates);
 		if (header != null) {
 			recordGroupByteBits = header.recordGroupByteBits;
 			recordGroupByteLength = header.recordGroupByteLength;
 			recordsPerGroup = header.recordsPerGroup;
 			superCompress = header.superCompress;
 			firstContainedRecord = header.firstRecord;
 			this.firstRecord = Math.max(firstRecord, firstContainedRecord);
 			numContainedRecords = header.numRecords;
 			this.numRecords = Math.min(firstRecord + numRecords,
 					firstContainedRecord + numContainedRecords)
 					- this.firstRecord;
 		} else {
 			double requiredCompression = Double.parseDouble(conf.getProperty(
 					"record.compression", "0")) / 100;
 			double compression;
 			if (requiredCompression == 0D) {
 				superCompress = false;
 				int bits = (int) (Math.log(totalStates) / Math.log(2));
 				if ((1 << bits) < totalStates)
 					++bits;
 				int recordGroupByteLength = (bits + 7) >> 3;
 				int recordGroupByteBits = 0;
 				recordGroupByteLength >>= 1;
 				while (recordGroupByteLength > 0) {
 					recordGroupByteBits++;
 					recordGroupByteLength >>= 1;
 				}
 				this.recordGroupByteBits = recordGroupByteBits;
 				this.recordGroupByteLength = 1 << recordGroupByteBits;
 				recordsPerGroup = 1;
 			} else {
 				superCompress = true;
 				int recordGuess;
 				int bitLength;
 				double log2;
 				log2 = Math.log(totalStates) / Math.log(2);
 				if (log2 > 8) {
 					recordGuess = 1;
 					bitLength = (int) Math.ceil(log2);
 					compression = (log2 / 8) / ((bitLength + 7) >> 3);
 					while (compression < requiredCompression) {
 						recordGuess++;
 						bitLength = (int) Math.ceil(recordGuess * log2);
 						compression = (recordGuess * log2 / 8)
 								/ ((bitLength + 7) >> 3);
 					}
 				} else {
 					bitLength = 8;
 					recordGuess = (int) (8D / log2);
 					compression = recordGuess * log2 / 8;
 					while (compression < requiredCompression) {
 						bitLength += 8;
 						recordGuess = (int) (bitLength / log2);
 						compression = (recordGuess * log2 / 8)
 								/ (bitLength >> 3);
 					}
 				}
 				recordsPerGroup = recordGuess;
 				recordGroupByteLength = (bigIntTotalStates.pow(recordsPerGroup)
 						.bitLength() + 7) >> 3;
 				recordGroupByteBits = -1;
 			}
 			firstContainedRecord = this.firstRecord = firstRecord;
 			numContainedRecords = this.numRecords = numRecords;
 		}
 		if (recordGroupByteLength < 8) {
 			recordGroupUsesLong = true;
 			multipliers = null;
 			longMultipliers = new long[recordsPerGroup + 1];
 			long longMultiplier = 1;
 			for (int i = 0; i <= recordsPerGroup; i++) {
 				longMultipliers[i] = longMultiplier;
 				longMultiplier *= totalStates;
 			}
 		} else {
 			recordGroupUsesLong = false;
 			longMultipliers = null;
 			multipliers = new BigInteger[recordsPerGroup + 1];
 			BigInteger multiplier = BigInteger.ONE;
 			for (int i = 0; i <= recordsPerGroup; i++) {
 				multipliers[i] = multiplier;
 				multiplier = multiplier.multiply(bigIntTotalStates);
 			}
 		}
 		assert Util.debug(DebugFacility.DATABASE, recordsPerGroup
 				+ " records per group\n" + recordGroupByteLength
 				+ " bytes per group");
 
 	}
 
 	/**
 	 * Ensure that all threads reading from this database have access to the
 	 * same information
 	 */
 	public void flush() {
 
 	}
 
 	/**
 	 * Close this Database, flush to disk, and release all associated resources.
 	 * This object should not be used again after making this call.
 	 */
 	public abstract void close();
 
 	/**
 	 * Retrieve the Configuration associated with this Database.
 	 * 
 	 * @return the Configuration stored in the database
 	 */
 	public final Configuration getConfiguration() {
 		return conf;
 	}
 
 	/**
 	 * Read the Nth Record from the Database as a long
 	 * 
 	 * @param dh
 	 *            A handle for this database.
 	 * 
 	 * @param recordIndex
 	 *            The record number
 	 * @return The record as a long
 	 */
 	public long getRecord(DatabaseHandle dh, long recordIndex) {
 		long byteIndex = toByte(recordIndex);
 		int num = toNum(recordIndex);
 		if (recordGroupUsesLong)
 			return getRecord(
 					getRecordsAsLongGroup(dh, byteIndex, num, num + 1), num);
 		else
 			return getRecord(getRecordsAsBigIntGroup(dh, byteIndex, num,
 					num + 1), num);
 	}
 
 	/**
 	 * @param dh
 	 *            A handle for this database
 	 * 
 	 * @param recordIndex
 	 *            The record number
 	 * @param r
 	 *            The Record to store
 	 */
 	public void putRecord(DatabaseHandle dh, long recordIndex, long r) {
 		long byteIndex = toByte(recordIndex);
 		int num = toNum(recordIndex);
 		if (recordGroupUsesLong)
 			putRecordsAsGroup(dh, byteIndex, num, num + 1,
 					setRecord(0L, num, r));
 		else
 			putRecordsAsGroup(dh, byteIndex, num, num + 1, setRecord(
 					BigInteger.ZERO, num, r));
 	}
 
 	/**
 	 * @param loc
 	 *            The index of the byte the group begins on
 	 * @return The group beginning at byte-index loc
 	 */
 	protected long getLongRecordGroup(DatabaseHandle dh, long loc) {
 		byte[] groupBytes = dh.getRecordGroupBytes();
 		getBytes(dh, loc, groupBytes, 0, recordGroupByteLength);
 		long v = longRecordGroup(groupBytes, 0);
 		dh.releaseBytes(groupBytes);
 		return v;
 	}
 
 	/**
 	 * @param loc
 	 *            The index of the byte the group begins on
 	 * @return The group beginning at loc
 	 */
 	protected BigInteger getBigIntRecordGroup(DatabaseHandle dh, long loc) {
 		byte[] groupBytes = dh.getRecordGroupBytes();
 		getBytes(dh, loc, groupBytes, 0, recordGroupByteLength);
 		BigInteger v = bigIntRecordGroup(groupBytes, 0);
 		dh.releaseBytes(groupBytes);
 		return v;
 	}
 
 	/**
 	 * @param loc
 	 *            The index of the byte the group begins on
 	 * @param rg
 	 *            The record group to store
 	 */
 	protected void putRecordGroup(DatabaseHandle dh, long loc, long rg) {
 		byte[] groupBytes = dh.getRecordGroupBytes();
 		toUnsignedByteArray(rg, groupBytes, 0);
 		putBytes(dh, loc, groupBytes, 0, recordGroupByteLength);
 		dh.releaseBytes(groupBytes);
 	}
 
 	/**
 	 * @param loc
 	 *            The index of the byte the group begins on
 	 * @param rg
 	 *            The record group to store
 	 */
 	protected void putRecordGroup(DatabaseHandle dh, long loc, BigInteger rg) {
 		byte[] groups = dh.getRecordGroupBytes();
 		toUnsignedByteArray(rg, groups, 0);
 		putBytes(dh, loc, groups, 0, recordGroupByteLength);
 		dh.releaseBytes(groups);
 	}
 
 	protected void putRecordsAsBytes(DatabaseHandle dh, long byteIndex,
 			int recordNum, byte[] arr, int off, int numBytes, int lastNum,
 			boolean edgesAreCorrect) {
 		prepareRange(dh, byteIndex, recordNum, numBytes, lastNum);
 		putBytes(dh, arr, off, numBytes, edgesAreCorrect);
 	}
 
 	protected void putRecordsAsGroup(DatabaseHandle dh, long byteIndex,
 			int firstNum, int lastNum, long rg) {
 		byte[] groupBytes = dh.getRecordGroupBytes();
 		toUnsignedByteArray(rg, groupBytes, 0);
 		putRecordsAsBytes(dh, byteIndex, firstNum, groupBytes, 0,
 				recordGroupByteLength,
 				lastNum == recordsPerGroup ? 0 : lastNum, false);
 		dh.releaseBytes(groupBytes);
 	}
 
 	protected void putRecordsAsGroup(DatabaseHandle dh, long byteIndex,
 			int firstNum, int lastNum, BigInteger rg) {
 		byte[] groupBytes = dh.getRecordGroupBytes();
 		toUnsignedByteArray(rg, groupBytes, 0);
 		putRecordsAsBytes(dh, byteIndex, firstNum, groupBytes, 0,
 				recordGroupByteLength,
 				lastNum == recordsPerGroup ? 0 : lastNum, false);
 		dh.releaseBytes(groupBytes);
 	}
 
 	protected final long splice(long group1, long group2, int num) {
 		if (superCompress)
 			return group1 % longMultipliers[num]
 					+ (group2 - group2 % longMultipliers[num]);
 		else if (num == 0)
 			return group2;
 		else
 			return group1;
 	}
 
 	protected final BigInteger splice(BigInteger group1, BigInteger group2,
 			int num) {
 		if (superCompress)
 			return group1.mod(multipliers[num]).add(
 					group2.subtract(group2.mod(multipliers[num])));
 		else if (num == 0)
 			return group2;
 		else
 			return group1;
 	}
 
 	/**
 	 * Seek to this location and write len bytes from an array into the database
 	 * 
 	 * @param loc
 	 *            The location to seek to
 	 * @param arr
 	 *            An array to read from
 	 * @param off
 	 *            The offset into the array
 	 * @param len
 	 *            The number of bytes to write
 	 */
 	protected abstract void putBytes(DatabaseHandle dh, long loc, byte[] arr,
 			int off, int len);
 
 	protected void getRecordsAsBytes(DatabaseHandle dh, long byteIndex,
 			int recordNum, byte[] arr, int off, int numBytes, int lastNum,
 			boolean overwriteEdgesOk) {
 		prepareRange(dh, byteIndex, recordNum, numBytes, lastNum);
 		getBytes(dh, arr, off, numBytes, overwriteEdgesOk);
 	}
 
 	protected long getRecordsAsLongGroup(DatabaseHandle dh, long byteIndex,
 			int firstNum, int lastNum) {
 		byte[] groupBytes = dh.getRecordGroupBytes();
 		getRecordsAsBytes(dh, byteIndex, firstNum, groupBytes, 0,
 				recordGroupByteLength,
 				lastNum == recordsPerGroup ? 0 : lastNum, true);
 		long group = longRecordGroup(groupBytes, 0);
 		dh.releaseBytes(groupBytes);
 		return group;
 	}
 
 	protected BigInteger getRecordsAsBigIntGroup(DatabaseHandle dh,
 			long byteIndex, int firstNum, int lastNum) {
 		byte[] groupBytes = dh.getRecordGroupBytes();
 		getRecordsAsBytes(dh, byteIndex, firstNum, groupBytes, 0,
 				recordGroupByteLength,
 				lastNum == recordsPerGroup ? 0 : lastNum, true);
 		BigInteger group = bigIntRecordGroup(groupBytes, 0);
 		dh.releaseBytes(groupBytes);
 		return group;
 	}
 
 	/**
 	 * Seek to this location and read len bytes from the database into an array
 	 * 
 	 * @param loc
 	 *            The location to seek to
 	 * @param arr
 	 *            The array to write to
 	 * @param off
 	 *            The offset into the array
 	 * @param len
 	 *            The number of bytes to read
 	 */
 	protected abstract void getBytes(DatabaseHandle dh, long loc, byte[] arr,
 			int off, int len);
 
 	protected void prepareRange(DatabaseHandle dh, long byteIndex,
 			int firstNum, long numBytes, int lastNum) {
 		dh.byteIndex = byteIndex;
 		dh.firstNum = firstNum;
 		dh.lastByteIndex = numBytes + byteIndex;
 		dh.lastNum = lastNum;
 		dh.location = byteIndex;
 	}
 
 	/**
 	 * Writes len bytes from the array into the database
 	 * 
 	 * @param arr
 	 *            An array to read from
 	 * @param off
 	 *            The offset into the array
 	 */
 	protected int putBytes(DatabaseHandle dh, byte[] arr, int off, int maxLen,
 			boolean edgesAreCorrect) {
 		final int numBytes;
 		if (edgesAreCorrect) {
 			numBytes = (int) Math.min(dh.lastByteIndex - dh.location, maxLen);
 			putBytes(dh, dh.location, arr, off, numBytes);
 			dh.location += numBytes;
 			return numBytes;
 		} else if (!superCompress || (dh.firstNum == 0 && dh.lastNum == 0)) {
 			return putBytes(dh, arr, off, maxLen, true);
 		}
 		numBytes = (int) Math.min(dh.lastByteIndex - dh.location, maxLen);
 		if (dh.innerHandle == null)
 			dh.innerHandle = getHandle();
 		int remainBytes = numBytes;
 		final long beforeBytes = dh.location - dh.byteIndex;
 		long afterBytes = dh.lastByteIndex - (dh.location + numBytes);
 		if (beforeBytes < recordGroupByteLength && dh.firstNum > 0) {
 			int iByteNum = (int) beforeBytes;
 			if (dh.lastByteIndex - dh.byteIndex == recordGroupByteLength
 					&& dh.lastNum > 0) {
 				if (dh.firstGroup == null) {
 					dh.firstGroup = dh.getRecordGroupBytes();
 					getBytes(dh.innerHandle, dh.byteIndex, dh.firstGroup, 0,
 							recordGroupByteLength);
 					// Although two separate calls to getRecordsAsBytes (for
 					// either edge) might look nicer, in practice that would
 					// usually be redundant and less efficient
 				}
 				if (recordGroupUsesLong) {
 					long group1;
 					long group3;
 					group1 = group3 = longRecordGroup(dh.firstGroup, 0);
 					long group2 = longRecordGroup(arr, off - iByteNum);
 					long resultGroup = splice(group1, group2, dh.firstNum);
 					resultGroup = splice(resultGroup, group3, dh.lastNum);
 					toUnsignedByteArray(resultGroup, iByteNum, dh.firstGroup,
 							iByteNum, numBytes);
 				} else {
 					BigInteger group1;
 					BigInteger group3;
 					group1 = group3 = bigIntRecordGroup(dh.firstGroup, 0);
 					BigInteger group2 = bigIntRecordGroup(arr, off - iByteNum);
 					BigInteger resultGroup = splice(group1, group2, dh.firstNum);
 					resultGroup = splice(resultGroup, group3, dh.lastNum);
 					toUnsignedByteArray(resultGroup, iByteNum, dh.firstGroup,
 							iByteNum, numBytes);
 				}
 				putBytes(dh, dh.firstGroup, iByteNum, numBytes, true);
 				if (dh.location == dh.lastByteIndex) {
 					dh.releaseBytes(dh.firstGroup);
 					dh.firstGroup = null;
 				}
 				return numBytes;
 			}
 			if (dh.firstGroup == null) {
 				dh.firstGroup = dh.getRecordGroupBytes();
 				getRecordsAsBytes(dh.innerHandle, dh.byteIndex, 0,
 						dh.firstGroup, 0, recordGroupByteLength, dh.firstNum,
 						true);
 			}
 			int bytesInGroupStill = Math.min(recordGroupByteLength - iByteNum,
 					remainBytes);
 			if (recordGroupUsesLong) {
 				long group1 = longRecordGroup(dh.firstGroup, 0);
 				long group2 = longRecordGroup(arr, off - iByteNum);
 				long resultGroup = splice(group1, group2, dh.firstNum);
 				toUnsignedByteArray(resultGroup, iByteNum, dh.firstGroup,
 						iByteNum, bytesInGroupStill);
 			} else {
 				BigInteger group1 = bigIntRecordGroup(dh.firstGroup, 0);
 				BigInteger group2 = bigIntRecordGroup(arr, off - iByteNum);
 				BigInteger resultGroup = splice(group1, group2, dh.firstNum);
 				toUnsignedByteArray(resultGroup, iByteNum, dh.firstGroup,
 						iByteNum, bytesInGroupStill);
 			}
 			putBytes(dh, dh.firstGroup, iByteNum, bytesInGroupStill, true);
 			if (dh.location == dh.byteIndex + recordGroupByteLength
 					|| dh.location == dh.lastByteIndex) {
 				dh.releaseBytes(dh.firstGroup);
 				dh.firstGroup = null;
 			}
 			remainBytes -= bytesInGroupStill;
 			off += bytesInGroupStill;
 		}
 		final int middleBytes;
 		if (afterBytes < recordGroupByteLength && dh.lastNum > 0)
 			middleBytes = remainBytes
 					- (recordGroupByteLength - (int) afterBytes);
 		else
 			middleBytes = remainBytes;
 		if (middleBytes > 0) {
 			putBytes(dh, arr, off, middleBytes, true);
 			remainBytes -= middleBytes;
 			off += middleBytes;
 		}
 		if (remainBytes > 0) {
 			if (dh.lastGroup == null) {
 				dh.lastGroup = dh.getRecordGroupBytes();
 				getRecordsAsBytes(dh.innerHandle, dh.lastByteIndex
 						- recordGroupByteLength, dh.lastNum, dh.lastGroup, 0,
 						recordGroupByteLength, 0, true);
 			}
 			int groupByte = recordGroupByteLength
 					- (remainBytes + (int) afterBytes);
 			if (recordGroupUsesLong) {
 				long group1 = longRecordGroup(arr, off - groupByte);
 				long group2 = longRecordGroup(dh.lastGroup, 0);
 				long resultGroup = splice(group1, group2, dh.lastNum);
 				toUnsignedByteArray(resultGroup, groupByte, dh.lastGroup,
 						groupByte, remainBytes);
 			} else {
 				BigInteger group1 = bigIntRecordGroup(arr, off - groupByte);
 				BigInteger group2 = bigIntRecordGroup(dh.lastGroup, 0);
 				BigInteger resultGroup = splice(group1, group2, dh.lastNum);
 				toUnsignedByteArray(resultGroup, groupByte, dh.lastGroup,
 						groupByte, remainBytes);
 			}
 			putBytes(dh, dh.lastGroup, groupByte, remainBytes, true);
 			if (afterBytes == 0) {
 				dh.releaseBytes(dh.lastGroup);
 				dh.lastGroup = null;
 			}
 		}
 		return numBytes;
 	}
 
 	/**
 	 * Reads len bytes from the database into an array
 	 * 
 	 * @param arr
 	 *            An array to write to
 	 * @param off
 	 *            The offset into the array
 	 */
 	protected int getBytes(final DatabaseHandle dh, final byte[] arr, int off,
 			final int maxLen, final boolean overwriteEdgesOk) {
 		final int numBytes;
 		if (overwriteEdgesOk) {
 			numBytes = (int) Math.min(dh.lastByteIndex - dh.location, maxLen);
 			getBytes(dh, dh.location, arr, off, numBytes);
 			dh.location += numBytes;
 			return numBytes;
 		} else if (!superCompress || (dh.firstNum == 0 && dh.lastNum == 0)) {
 			return getBytes(dh, arr, off, maxLen, true);
 		}
 		numBytes = (int) Math.min(dh.lastByteIndex - dh.location, maxLen);
 		int remainBytes = numBytes;
 		long byteLoc = dh.location - dh.byteIndex;
 		long afterBytes = dh.lastByteIndex - (dh.location + numBytes);
 		if (byteLoc < recordGroupByteLength && dh.firstNum > 0) {
 			byte[] firstGroup = dh.getRecordGroupBytes();
 			int iByteNum = (int) byteLoc;
 			if (dh.lastByteIndex - dh.byteIndex == recordGroupByteLength
 					&& dh.lastNum > 0) {
 				getBytes(dh, firstGroup, iByteNum, numBytes, true);
 				if (recordGroupUsesLong) {
 					long group1;
 					long group3;
 					group1 = group3 = longRecordGroup(arr, off - iByteNum);
 					long group2 = longRecordGroup(firstGroup, 0);
 					long resultGroup = splice(group1, group2, dh.firstNum);
 					resultGroup = splice(resultGroup, group3, dh.lastNum);
 					toUnsignedByteArray(resultGroup, iByteNum, arr, off,
 							numBytes);
 				} else {
 					BigInteger group1;
 					BigInteger group3;
 					group1 = group3 = bigIntRecordGroup(arr, off - iByteNum);
 					BigInteger group2 = bigIntRecordGroup(firstGroup, 0);
 					BigInteger resultGroup = splice(group1, group2, dh.firstNum);
 					resultGroup = splice(resultGroup, group3, dh.lastNum);
 					toUnsignedByteArray(resultGroup, iByteNum, arr, off,
 							numBytes);
 				}
 				dh.releaseBytes(firstGroup);
 				return numBytes;
 			}
 			int bytesInGroupStill = Math.min(recordGroupByteLength - iByteNum,
 					remainBytes);
 			getBytes(dh, firstGroup, iByteNum, bytesInGroupStill, true);
 			if (recordGroupUsesLong) {
 				long group1 = longRecordGroup(arr, off - iByteNum);
 				long group2 = longRecordGroup(firstGroup, 0);
 				long resultGroup = splice(group1, group2, dh.firstNum);
 				toUnsignedByteArray(resultGroup, iByteNum, arr, off,
 						bytesInGroupStill);
 			} else {
 				BigInteger group1 = bigIntRecordGroup(arr, off - iByteNum);
 				BigInteger group2 = bigIntRecordGroup(firstGroup, 0);
 				BigInteger resultGroup = splice(group1, group2, dh.firstNum);
 				toUnsignedByteArray(resultGroup, iByteNum, arr, off,
 						bytesInGroupStill);
 			}
 			dh.releaseBytes(firstGroup);
 			byteLoc += bytesInGroupStill;
 			remainBytes -= bytesInGroupStill;
 			off += bytesInGroupStill;
 		}
 		final int middleBytes;
 		if (afterBytes < recordGroupByteLength && dh.lastNum > 0)
 			middleBytes = remainBytes
 					- (recordGroupByteLength - (int) afterBytes);
 		else
 			middleBytes = remainBytes;
 		if (middleBytes > 0) {
 			getBytes(dh, arr, off, middleBytes, true);
 			remainBytes -= middleBytes;
 			off += middleBytes;
 			byteLoc += middleBytes;
 		}
 		if (remainBytes > 0) {
 			byte[] lastGroup = dh.getRecordGroupBytes();
 			int groupByte = recordGroupByteLength
 					- (remainBytes + (int) afterBytes);
 			getBytes(dh, lastGroup, groupByte, remainBytes, true);
 			if (recordGroupUsesLong) {
 				long group1 = longRecordGroup(lastGroup, 0);
 				long group2 = longRecordGroup(arr, off - groupByte);
 				long resultGroup = splice(group1, group2, dh.lastNum);
 				toUnsignedByteArray(resultGroup, groupByte, arr, off,
 						remainBytes);
 			} else {
 				BigInteger group1 = bigIntRecordGroup(lastGroup, 0);
 				BigInteger group2 = bigIntRecordGroup(arr, off - groupByte);
 				BigInteger resultGroup = splice(group1, group2, dh.lastNum);
 				toUnsignedByteArray(resultGroup, groupByte, arr, off,
 						remainBytes);
 			}
 			dh.releaseBytes(lastGroup);
 		}
 		return numBytes;
 	}
 
 	/**
 	 * Fills a portion of the database with the passed record.
 	 * 
 	 * @param r
 	 *            The record
 	 * @param offset
 	 *            The byte offset into the database
 	 * @param len
 	 *            The number of bytes to fill
 	 */
 	public void fill(long r, long offset, long len) {
 		long[] recs = new long[recordsPerGroup];
 		for (int i = 0; i < recordsPerGroup; i++)
 			recs[i] = r;
 		DatabaseHandle dh = getHandle();
 		prepareRange(dh, offset, 0, len, 0);
 		int maxBytes = 1024 - 1024 % recordGroupByteLength;
 		byte[] groups = new byte[maxBytes];
 		while (len > 0) {
 			int groupsLength = (int) Math.min(len, maxBytes);
 			int numGroups = groupsLength / recordGroupByteLength;
 			groupsLength = numGroups * recordGroupByteLength;
 			int onByte = 0;
 			if (recordGroupUsesLong) {
 				long recordGroup = longRecordGroup(recs, 0);
 				for (int i = 0; i < numGroups; i++) {
 					toUnsignedByteArray(recordGroup, groups, onByte);
 					onByte += recordGroupByteLength;
 				}
 			} else {
 				BigInteger recordGroup = bigIntRecordGroup(recs, 0);
 				for (int i = 0; i < numGroups; i++) {
 					toUnsignedByteArray(recordGroup, groups, onByte);
 					onByte += recordGroupByteLength;
 				}
 
 			}
 			putBytes(dh, groups, 0, groupsLength, true);
 			len -= groupsLength;
 		}
 		closeHandle(dh);
 	}
 
 	/**
 	 * @return The number of bytes used to store all the records (This does not
 	 *         include the header size)
 	 */
 	public long numRecords() {
 		return numRecords;
 	}
 
 	/**
 	 * @return The index of the first byte in this database (Will be zero if
 	 *         this database stores the entire game)
 	 */
 	public long firstRecord() {
 		return firstRecord;
 	}
 
 	public final boolean containsRecord(long hash) {
 		return hash >= firstRecord() && hash < firstRecord() + numRecords();
 	}
 
 	public DatabaseHandle getHandle() {
 		return new DatabaseHandle(recordGroupByteLength);
 	}
 
 	public void closeHandle(DatabaseHandle dh) {
 	}
 
 	protected final long toByte(long recordIndex) {
 		if (superCompress)
 			if (recordGroupByteLength > 1)
 				return recordIndex / recordsPerGroup * recordGroupByteLength;
 			else
 				return recordIndex / recordsPerGroup;
 		else
 			return recordIndex << recordGroupByteBits;
 	}
 
 	protected final long toFirstRecord(long byteIndex) {
 		if (superCompress)
 			if (recordGroupByteLength > 1)
 				return byteIndex / recordGroupByteLength * recordsPerGroup;
 			else
 				return byteIndex * recordsPerGroup;
 		else
 			return byteIndex >> recordGroupByteBits;
 	}
 
 	protected long toLastRecord(long byteIndex) {
 		return toFirstRecord(byteIndex + recordGroupByteLength - 1);
 	}
 
 	protected final int toNum(long recordIndex) {
 		if (superCompress)
 			return (int) (recordIndex % recordsPerGroup);
 		else
 			return 0;
 	}
 
 	protected final long lastByte(long lastRecord) {
 		return toByte(lastRecord + recordsPerGroup - 1);
 	}
 
 	protected final long numBytes(long firstRecord, long numRecords) {
 		return lastByte(firstRecord + numRecords) - toByte(firstRecord);
 	}
 
 	protected final long longRecordGroup(byte[] values, int offset) {
 		long longValues = 0;
 		for (int i = 0; i < recordGroupByteLength; i++) {
 			longValues <<= 8;
 			if (offset >= 0 && offset < values.length)
 				longValues |= (values[offset++] & 255L);
 		}
 		return longValues;
 	}
 
 	protected final BigInteger bigIntRecordGroup(byte[] values, int offset) {
 		byte[] bigIntByte = new byte[recordGroupByteLength];
 		for (int i = 0; i < recordGroupByteLength; i++) {
 			if (offset >= 0 && offset < values.length)
 				bigIntByte[i] = values[offset++];
 			else
 				bigIntByte[i] = 0;
 		}
 		return new BigInteger(1, bigIntByte);
 	}
 
 	protected final long longRecordGroup(long[] recs, int offset) {
 		if (superCompress) {
 			long longValues = 0;
 			for (int i = offset + recordsPerGroup - 1; i >= offset; i--) {
 				if (longValues > 0)
 					longValues *= totalStates;
 				longValues += recs[i];
 			}
 			return longValues;
 		} else
 			return recs[0];
 	}
 
 	protected final BigInteger bigIntRecordGroup(long[] recs, int offset) {
 		if (superCompress) {
 			BigInteger bigIntValues = BigInteger.ZERO;
 			for (int i = offset + recordsPerGroup - 1; i >= offset; i--) {
 				if (bigIntValues.compareTo(BigInteger.ZERO) > 0)
 					bigIntValues = bigIntValues.multiply(bigIntTotalStates);
 				bigIntValues = bigIntValues.add(BigInteger.valueOf(recs[i]));
 			}
 			return bigIntValues;
 		} else
 			return BigInteger.valueOf(recs[0]);
 	}
 
 	protected final void getRecords(long recordGroup, long[] recs, int offset) {
 		if (superCompress) {
 			for (int i = 0; i < recordsPerGroup; i++) {
 				long mod = recordGroup % totalStates;
 				recordGroup /= totalStates;
 				recs[offset++] = mod;
 			}
 		} else
 			recs[0] = recordGroup;
 	}
 
 	protected final void getRecords(BigInteger recordGroup, long[] recs,
 			int offset) {
 		if (superCompress) {
 			for (int i = 0; i < recordsPerGroup; i++) {
 				BigInteger[] results = recordGroup
 						.divideAndRemainder(bigIntTotalStates);
 				recordGroup = results[0];
 				recs[offset++] = results[1].longValue();
 			}
 		} else
 			recs[0] = recordGroup.longValue();
 	}
 
 	protected final long setRecord(long recordGroup, int num, long r) {
 		if (superCompress) {
 			if (recordGroup == 0) {
 				if (num == 0)
 					return r;
 				else {
 					return r * longMultipliers[num];
 				}
 			}
 			return (num < recordsPerGroup - 1 ? recordGroup - recordGroup
 					% longMultipliers[num + 1] : 0)
 					+ (num > 0 ? (recordGroup % longMultipliers[num] + r
 							* longMultipliers[num]) : r);
 		} else
 			return r;
 	}
 
 	protected final BigInteger setRecord(BigInteger recordGroup, int num, long r) {
 		if (superCompress) {
 			if (recordGroup.equals(BigInteger.ZERO)) {
 				if (num == 0)
 					return BigInteger.valueOf(r);
 				else {
 					return BigInteger.valueOf(r).multiply(multipliers[num]);
 				}
 			}
 			return (num < recordsPerGroup - 1 ? recordGroup
 					.subtract(recordGroup.mod(multipliers[num + 1]))
 					: BigInteger.ZERO).add(num > 0 ? (recordGroup
 					.mod(multipliers[num]).add(BigInteger.valueOf(r).multiply(
 					multipliers[num]))) : BigInteger.valueOf(r));
 		} else
 			return BigInteger.valueOf(r);
 	}
 
 	protected final long getRecord(long recordGroup, int num) {
 		if (superCompress) {
 			if (num == 0)
 				return recordGroup % totalStates;
 			else if (num == recordsPerGroup - 1)
 				return recordGroup / longMultipliers[num];
 			else
 				return recordGroup / longMultipliers[num] % totalStates;
 		} else
 			return recordGroup;
 	}
 
 	protected final long getRecord(BigInteger recordGroup, int num) {
 		if (superCompress)
 			if (num == 0)
 				return recordGroup.mod(bigIntTotalStates).longValue();
 			else if (num == recordsPerGroup - 1)
 				return recordGroup.divide(multipliers[num]).longValue();
 			else
 				return recordGroup.divide(multipliers[num]).mod(
 						bigIntTotalStates).longValue();
 		else
 			return recordGroup.longValue();
 	}
 
 	protected final void toUnsignedByteArray(long recordGroup,
 			byte[] byteArray, int offset) {
 		toUnsignedByteArray(recordGroup, 0, byteArray, offset,
 				recordGroupByteLength);
 	}
 
 	private final void toUnsignedByteArray(long recordGroup, int byteNum,
 			byte[] byteArray, int offset, int numBytes) {
 		int stopAt = offset + byteNum;
 		int startAt = stopAt + numBytes;
 		for (int i = offset + recordGroupByteLength - 1; i >= startAt; i--) {
 			recordGroup >>>= 8;
 		}
 		for (int i = startAt - 1; i >= stopAt; i--) {
 			byteArray[i] = (byte) recordGroup;
 			recordGroup >>>= 8;
 		}
 	}
 
 	protected final void toUnsignedByteArray(BigInteger recordGroup,
 			byte[] byteArray, int offset) {
 		toUnsignedByteArray(recordGroup, 0, byteArray, offset,
 				recordGroupByteLength);
 	}
 
 	protected final void toUnsignedByteArray(BigInteger recordGroup,
 			int byteOff, byte[] byteArray, int offset, int numBytes) {
 		byte[] bigIntArray = recordGroup.toByteArray();
 		int initialZeros = recordGroupByteLength - (bigIntArray.length - 1);
 		initialZeros -= byteOff;
 		for (int i = 0; i < initialZeros; i++) {
 			byteArray[offset++] = 0;
 		}
 		int start = 1;
 		if (initialZeros < 0)
 			start -= initialZeros;
 		else
 			numBytes -= initialZeros;
 		int last = start + numBytes;
 		for (int i = start; i < last; i++) {
 			byteArray[offset++] = bigIntArray[i];
 		}
 	}
 
 	public long[] splitRange(long firstRecord, long numRecords, int numSplits) {
 		return Util.groupAlignedTasks(numSplits, firstRecord, numRecords,
 				recordsPerGroup);
 	}
 
 	public final long requiredMem(long numHashes) {
 		return toByte(numHashes);
 	}
 
 	public static Database openDatabase(String uri) {
 		return openDatabase(uri, 0, -1);
 	}
 
 	public static Database openDatabase(Configuration conf, boolean solve) {
 		return openDatabase(null, conf, solve, null);
 	}
 
 	public static Database openDatabase(String uri, long firstRecord,
 			long numRecords) {
 		return openDatabase(uri, null, false, firstRecord, numRecords);
 	}
 
 	public static Database openDatabase(String uri, Configuration conf,
 			boolean solve, long firstRecord, long numRecords) {
 		return openDatabase(null, uri, conf, solve, firstRecord, numRecords);
 	}
 
 	public static Database openDatabase(String uri, Configuration conf,
 			boolean solve, DatabaseHeader header) {
 		return openDatabase(null, uri, conf, solve, header);
 	}
 
 	public static Database openDatabase(String dbType, String uri,
 			Configuration conf, boolean solve, DatabaseHeader header) {
 		return openDatabase(dbType, uri, conf, solve, 0, -1, header);
 	}
 
 	public static Database openDatabase(String dbType, String uri,
 			Configuration conf, boolean solve, long firstRecord, long numRecords) {
 		return openDatabase(dbType, uri, conf, solve, firstRecord, numRecords,
 				null);
 	}
 
 	/**
 	 * @param solve
 	 *            true for solving, false for playing
 	 * @param firstRecord
 	 *            The index of the first record this database contains
 	 * @param numRecords
 	 *            The number of records in this database
 	 * @return the Database used to store this particular solve Could not load
 	 *         the database class
 	 */
 	private static Database openDatabase(String dbType, String uri,
 			Configuration conf, boolean solve, long firstRecord,
 			long numRecords, DatabaseHeader header) {
 		if (uri == null)
 			uri = conf.getProperty("gamesman.db.uri");
 		if (uri.contains(":")) {
 			String[] hostFile = uri.split(":");
 			String host = hostFile[0];
 			String path = hostFile[1];
 			String file = hostFile[2];
 			if (!file.startsWith("/") && !file.startsWith(path))
 				file = path + "/" + file;
 			String user = null;
 			if (host.contains("@")) {
 				String[] userHost = host.split("@");
 				user = userHost[0];
 				host = userHost[1];
 			}
 			if (dbType == null || !dbType.endsWith("RemoteDatabase"))
 				dbType = "RemoteDatabase";
 			if (conf == null) {
 				Pair<DatabaseHeader, Configuration> p = RemoteDatabase
 						.remoteHeaderConf(user, host, file);
 				if (header == null)
 					header = p.car;
 				conf = p.cdr;
 			} else if (header == null) {
 				header = RemoteDatabase.remoteHeader(user, host, file);
 			}
			conf.setProperty("gamesman.remote.user", user);
 			conf.setProperty("gamesman.remote.server", host);
 			conf.setProperty("gamesman.remote.path", path);
 			conf.setProperty("gamesman.remote.db.uri", file);
 		} else {
 			if (conf == null) {
 				try {
 					FileInputStream fis = new FileInputStream(uri);
 					skipFully(fis, 18);
 					conf = Configuration.load(fis);
 					fis.close();
 				} catch (ClassNotFoundException e) {
 					throw new Error(e);
 				} catch (IOException e) {
 					throw new Error(e);
 				}
 			}
 			if (dbType == null)
 				dbType = conf.getProperty("gamesman.database");
 		}
 		String[] dbClasses = dbType.split(":");
 		for (int i = 0; i < dbClasses.length; i++) {
 			if (!dbClasses[i].startsWith("edu.berkeley.gamesman"))
 				dbClasses[i] = "edu.berkeley.gamesman.database." + dbClasses[i];
 		}
 		try {
 			Class<? extends Database> dbClass = Class.forName(
 					dbClasses[dbClasses.length - 1]).asSubclass(Database.class);
 			conf.db = dbClass.getConstructor(String.class, Configuration.class,
 					Boolean.TYPE, Long.TYPE, Long.TYPE, DatabaseHeader.class)
 					.newInstance(uri, conf, solve, firstRecord, numRecords,
 							header);
 			for (int i = dbClasses.length - 2; i >= 0; i--) {
 				Class<? extends DatabaseWrapper> wrapperClass = Class.forName(
 						dbClasses[i]).asSubclass(DatabaseWrapper.class);
 				conf.db = wrapperClass.getConstructor(Database.class,
 						String.class, Configuration.class, Boolean.TYPE,
 						Long.TYPE, Long.TYPE).newInstance(conf.db, uri, conf,
 						solve, firstRecord, numRecords);
 			}
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		} catch (SecurityException e) {
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			e.getCause().printStackTrace();
 		} catch (NoSuchMethodException e) {
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 		return conf.db;
 	}
 
 	public static void skipFully(InputStream is, long len) throws IOException {
 		while (len > 0) {
 			long bytesSkipped = is.skip(len);
 			if (bytesSkipped < 0)
 				break;
 			else {
 				len -= bytesSkipped;
 			}
 		}
 	}
 
 	protected final void store(OutputStream os, String uri) throws IOException {
 		storeInfo(os);
 		conf.store(os, this.getClass().getName(), uri);
 	}
 
 	protected final void skipHeader(InputStream is) throws IOException {
 		skipInfo(is);
 		Configuration.skipConf(is);
 	}
 
 	protected final void storeInfo(OutputStream os) throws IOException {
 		for (int i = 56; i >= 0; i -= 8)
 			os.write((int) (firstRecord() >>> i));
 		for (int i = 56; i >= 0; i -= 8)
 			os.write((int) (numRecords() >>> i));
 		if (superCompress) {
 			os.write(recordsPerGroup >>> 2);
 			os.write(((recordsPerGroup & 3) << 6) | recordGroupByteLength);
 		} else {
 			os.write(-1);
 			os.write(recordGroupByteBits);
 		}
 	}
 
 	private void skipInfo(InputStream is) throws IOException {
 		byte[] b = new byte[18];
 		readFully(is, b, 0, 18);
 	}
 
 	public static final void readFully(InputStream is, byte[] arr, int off,
 			int len) throws IOException {
 		while (len > 0) {
 			int bytesRead = is.read(arr, off, len);
 			if (bytesRead < 0)
 				break;
 			else {
 				off += bytesRead;
 				len -= bytesRead;
 			}
 		}
 	}
 
 	public final DatabaseHeader getHeader() {
 		if (superCompress)
 			return new DatabaseHeader(firstRecord(), numRecords(),
 					recordsPerGroup, recordGroupByteLength);
 		else
 			return new DatabaseHeader(firstRecord(), numRecords(),
 					recordGroupByteBits);
 	}
 
 	public final DatabaseHeader getHeader(long dbFirstRecord, long dbNumRecords) {
 		if (superCompress)
 			return new DatabaseHeader(dbFirstRecord, dbNumRecords,
 					recordsPerGroup, recordGroupByteLength);
 		else
 			return new DatabaseHeader(dbFirstRecord, dbNumRecords,
 					recordGroupByteBits);
 	}
 }
