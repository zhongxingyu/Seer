 /*
  * Copyright 2010 NCHOVY
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.araqne.logstorage.file;
 
 import java.io.Closeable;
 import java.io.DataInput;
 import java.io.EOFException;
 import java.io.IOException;
 import java.nio.BufferUnderflowException;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.zip.DataFormatException;
 import java.util.zip.Inflater;
 
 import org.araqne.log.api.LogParser;
 import org.araqne.log.api.LogParserBugException;
 import org.araqne.log.api.LogParserBuilder;
 import org.araqne.logstorage.Log;
 import org.araqne.logstorage.LogMarshaler;
 import org.araqne.logstorage.LogTraverseCallback;
 import org.araqne.logstorage.TableScanRequest;
 import org.araqne.storage.api.FilePath;
 import org.araqne.storage.api.StorageInputStream;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class LogFileReaderV2 extends LogFileReader {
 	private static final int DATA_BLOCK_HEADER_LENGTH = 24;
 	private Logger logger = LoggerFactory.getLogger(LogFileReaderV2.class);
 	public static final int INDEX_ITEM_SIZE = 4;
 
 	private String tableName;
 
 	private FilePath indexPath;
 	private FilePath dataPath;
 	private StorageInputStream indexStream;
 	private StorageInputStream dataStream;
 
 	private List<IndexBlockHeader> indexBlockHeaders = new ArrayList<IndexBlockHeader>();
 	private List<DataBlockHeader> dataBlockHeaders = new ArrayList<DataBlockHeader>();
 
 	private byte[] buf;
 	private DataBlockHeader nowDataBlock;
 	private ByteBuffer dataBuffer;
 
 	private Inflater decompresser = new Inflater();
 	private long totalCount;
 	private boolean useDeflater;
 
 	public LogFileReaderV2(String tableName, FilePath indexPath, FilePath dataPath) throws IOException, InvalidLogFileHeaderException {
 		try {
 			this.tableName = tableName;
 			this.indexPath = indexPath;
 			this.dataPath = dataPath;
 			this.indexStream = indexPath.newInputStream();
 			LogFileHeader indexFileHeader = LogFileHeader.extractHeader(indexStream);
 			if (indexFileHeader.version() != 2)
 				throw new InvalidLogFileHeaderException("version not match, index file " + indexPath.getAbsolutePath());
 
 			long length = indexStream.length() - 4;
 			long pos = indexFileHeader.size();
 			while (pos < length) {
 				indexStream.seek(pos);
 				IndexBlockHeader header = new IndexBlockHeader(indexStream);
 				header.fp = pos;
 				header.ascLogCount = totalCount;
 				totalCount += header.logCount;
 				indexBlockHeaders.add(header);
 				pos += 4 + header.logCount * INDEX_ITEM_SIZE;
 			}
 
 			long t = 0;
 			for (int i = indexBlockHeaders.size() - 1; i >= 0; i--) {
 				IndexBlockHeader h = indexBlockHeaders.get(i);
 				h.dscLogCount = t;
 				t += h.logCount;
 			}
 
 			logger.trace("araqne logstorage: {} has {} blocks, {} logs.",
 					new Object[] { indexPath.getName(), indexBlockHeaders.size(), totalCount });
 
 			this.dataStream = dataPath.newInputStream();
 			LogFileHeader dataFileHeader = LogFileHeader.extractHeader(dataStream);
 			if (dataFileHeader.version() != 2)
 				throw new InvalidLogFileHeaderException("version not match");
 
 			byte[] ext = dataFileHeader.getExtraData();
 			int dataBlockSize = getInt(dataFileHeader.getExtraData());
 			dataBuffer = ByteBuffer.allocate(dataBlockSize);
 			buf = new byte[dataBlockSize];
 
 			if (new String(ext, 4, ext.length - 4).trim().equals("deflater"))
 				useDeflater = true;
 
 			length = dataStream.length();
 			pos = dataFileHeader.size();
 			while (pos < length) {
 				if (pos < 0)
 					throw new IOException("negative seek offset " + pos + ", index file: " + indexPath.getAbsolutePath()
 							+ ", data file: " + dataPath.getAbsolutePath());
 
 				// ignore last immature data block
 				if (length - pos < DATA_BLOCK_HEADER_LENGTH)
 					break;
 
 				try {
 					dataStream.seek(pos);
 					DataBlockHeader header = new DataBlockHeader(dataStream);
 					header.fp = pos;
 					dataBlockHeaders.add(header);
 					pos += 24 + header.compressedLength;
 				} catch (BufferUnderflowException e) {
 					logger.error("araqne logstorage: buffer underflow at position {}, data file [{}]", pos,
 							dataPath.getAbsolutePath());
 					throw e;
 				} catch (EOFException e) {
 					break;
 				}
 			}
 
 			if (indexBlockHeaders.size() > dataBlockHeaders.size())
 				throw new IOException("invalid log file, index file: " + indexPath + ", data file: " + dataPath);
 		} catch (IOException t) {
 			ensureClose(indexStream, indexPath);
 			ensureClose(dataStream, dataPath);
 			throw t;
 		} catch (RuntimeException t) {
 			ensureClose(indexStream, indexPath);
 			ensureClose(dataStream, dataPath);
 			throw t;
 		}
 	}
 
 	@Override
 	public FilePath getIndexPath() {
 		return indexPath;
 	}
 
 	@Override
 	public FilePath getDataPath() {
 		return dataPath;
 	}
 
 	private int getInt(byte[] extraData) {
 		int value = 0;
 		for (int i = 0; i < 4; i++) {
 			value <<= 8;
 			value |= extraData[i] & 0xFF;
 		}
 		return value;
 	}
 
 	public long count() {
 		return totalCount;
 	}
 
 	private long getLogRecordDate(DataBlockHeader data, int offset) throws IOException {
 		prepareDataBlock(data);
 
 		dataBuffer.position(offset + 8);
 		return dataBuffer.getLong();
 	}
 
 	private LogRecord getLogRecord(DataBlockHeader data, int offset) throws IOException {
 		prepareDataBlock(data);
 
 		dataBuffer.position(offset);
 		long id = dataBuffer.getLong();
 		Date date = new Date(dataBuffer.getLong());
 		byte[] b = new byte[dataBuffer.getInt()];
 		dataBuffer.get(b);
 
 		return new LogRecord(date, id, ByteBuffer.wrap(b));
 	}
 
 	private void prepareDataBlock(DataBlockHeader header) throws IOException {
 		if (!header.equals(nowDataBlock)) {
 			nowDataBlock = header;
 
 			dataBuffer.clear();
 			dataStream.seek(header.fp + 24L);
 
 			// assume deflate if original length != compress length for backward
 			// compatibility
 			if (useDeflater || header.origLength != header.compressedLength) {
 				dataStream.readFully(buf, 0, header.compressedLength);
 				decompresser.setInput(buf, 0, header.compressedLength);
 				try {
 					dataBuffer.limit(header.origLength);
 					decompresser.inflate(dataBuffer.array());
 					decompresser.reset();
 				} catch (DataFormatException e) {
 					throw new IOException(e);
 				}
 			} else {
 				dataStream.readFully(dataBuffer.array(), 0, header.origLength);
 			}
 		}
 	}
 
 	@Override
 	public void close() {
 		decompresser.end();
 
 		ensureClose(indexStream, indexPath);
 		ensureClose(dataStream, dataPath);
 	}
 
 	private void ensureClose(Closeable stream, FilePath path) {
 		try {
 			if (stream != null)
 				stream.close();
 		} catch (IOException e) {
 			logger.error("araqne logstorage: cannot close file - " + path.getAbsolutePath(), e);
 		}
 	}
 
 	private Integer indexBlockNextId = 1;
 
 	private class IndexBlockHeader {
 		private long fp;
 		private int firstId;
 		private int logCount;
 
 		// except this block's log count
 		private long ascLogCount;
 		private long dscLogCount;
 
 		private IndexBlockHeader(DataInput f) throws IOException {
 			try {
 				this.logCount = f.readInt();
 			} catch (IOException e) {
 				logger.error("araqne logstorage: broken index file - " + indexPath.getAbsolutePath());
 				throw e;
 			}
 			this.firstId = indexBlockNextId;
 			indexBlockNextId += logCount;
 		}
 
 		@Override
 		public String toString() {
 			return "index block header, fp=" + fp + ", first_id=" + firstId + ", count=" + logCount + ", asc=" + ascLogCount
 					+ ", dsc=" + dscLogCount + "]";
 		}
 	}
 
 	private ByteBuffer dataBlockHeader = ByteBuffer.allocate(DATA_BLOCK_HEADER_LENGTH);
 
 	private class DataBlockHeader {
 		private long fp;
 		private long startDate;
 		private long endDate;
 		private int origLength;
 		private int compressedLength;
 
 		private DataBlockHeader(DataInput f) throws IOException {
 			try {
 				f.readFully(dataBlockHeader.array());
 			} catch (IOException e) {
 				logger.warn("araqne logstorage: broken data file - " + dataPath.getAbsolutePath());
 				throw e;
 			}
 			dataBlockHeader.position(0);
 			this.startDate = dataBlockHeader.getLong();
 			this.endDate = dataBlockHeader.getLong();
 			this.origLength = dataBlockHeader.getInt();
 			this.compressedLength = dataBlockHeader.getInt();
 		}
 	}
 
 	/**
 	 * descending order by default
 	 * 
 	 * @return log record cursor
 	 */
 	@Override
 	public LogRecordCursor getCursor() throws IOException {
 		return getCursor(false);
 	}
 
 	@Override
 	public LogRecordCursor getCursor(boolean ascending) throws IOException {
 		return new LogCursorImpl(ascending);
 	}
 
 	@Override
 	public LogBlockCursor getBlockCursor() throws IOException {
 		throw new UnsupportedOperationException();
 	}
 
 	private class LogCursorImpl implements LogRecordCursor {
 		// offset of next log
 		private long pos;
 
 		private IndexBlockHeader currentIndexHeader;
 		private int currentIndexBlockNo;
 		private DataBlockHeader currentDataHeader;
 		private ArrayList<Integer> currentOffsets = new ArrayList<Integer>();
 
 		private final boolean ascending;
 
 		public LogCursorImpl(boolean ascending) {
 			this.ascending = ascending;
 
 			if (indexBlockHeaders.size() == 0)
 				return;
 
 			if (ascending) {
 				currentIndexHeader = indexBlockHeaders.get(0);
 				currentDataHeader = dataBlockHeaders.get(0);
 				currentIndexBlockNo = 0;
 			} else {
 				currentIndexHeader = indexBlockHeaders.get(indexBlockHeaders.size() - 1);
 				currentDataHeader = dataBlockHeaders.get(dataBlockHeaders.size() - 1);
 				currentIndexBlockNo = indexBlockHeaders.size() - 1;
 			}
 
 			replaceBuffer();
 		}
 
 		public void skip(long offset) {
 			if (offset == 0)
 				return;
 
 			if (offset < 0)
 				throw new IllegalArgumentException("negative offset is not allowed");
 
 			pos += offset;
 
 			int relative = getRelativeOffset();
 			if (relative >= currentIndexHeader.logCount)
 				replaceBuffer();
 		}
 
 		public void reset() {
 			pos = 0;
 			replaceBuffer();
 		}
 
 		private void replaceBuffer() {
 			Integer next = findIndexBlock(pos);
 			if (next == null)
 				return;
 
 			currentIndexBlockNo = next;
 			currentIndexHeader = indexBlockHeaders.get(currentIndexBlockNo);
 			currentDataHeader = dataBlockHeaders.get(currentIndexBlockNo);
 
 			// read log data offsets from index block
 			try {
 				ByteBuffer indexBuffer = ByteBuffer.allocate(currentIndexHeader.logCount * 4);
 				indexStream.seek(currentIndexHeader.fp + 4);
 				indexStream.read(indexBuffer.array());
 				currentOffsets = new ArrayList<Integer>(currentIndexHeader.logCount + 2);
 				for (int i = 0; i < currentIndexHeader.logCount; i++)
 					currentOffsets.add(indexBuffer.getInt());
 			} catch (IOException e) {
 				throw new IllegalStateException("cannot load data offsets from index file", e);
 			}
 		}
 
 		/**
 		 * 
 		 * @param offset
 		 *            relative offset from file begin or file end
 		 * @return the index block number
 		 */
 		private Integer findIndexBlock(long offset) {
 			int no = currentIndexBlockNo;
 			int blockCount = indexBlockHeaders.size();
 
 			while (true) {
 				if (no < 0 || no >= blockCount)
 					return null;
 
 				IndexBlockHeader h = indexBlockHeaders.get(no);
 				if (ascending) {
 					if (offset < h.ascLogCount)
 						no--;
 					else if (h.logCount + h.ascLogCount <= offset)
 						no++;
 					else
 						return no;
 				} else {
 					if (offset < h.dscLogCount)
 						no++;
 					else if (h.logCount + h.dscLogCount <= offset)
 						no--;
 					else
 						return no;
 				}
 			}
 
 		}
 
 		@Override
 		public boolean hasNext() {
 			return pos < totalCount;
 		}
 
 		@Override
 		public LogRecord next() {
 			if (!hasNext())
 				throw new IllegalStateException("log file is closed: " + dataPath.getAbsolutePath());
 
 			int relative = getRelativeOffset();
 
 			try {
 				// absolute log offset in block (consider ordering)
 				int n = ascending ? relative : (int) (currentIndexHeader.logCount - relative - 1);
 				if (n < 0)
 					throw new IllegalStateException("n " + n + ", current index no: " + currentIndexBlockNo
 							+ ", current index count " + currentIndexHeader.logCount + ", relative " + relative);
 				LogRecord record = getLogRecord(currentDataHeader, currentOffsets.get(n));
 				pos++;
 				return record;
 			} catch (IOException e) {
 				throw new IllegalStateException(e);
 			} finally {
 				// replace block if needed
 				if (++relative >= currentIndexHeader.logCount) {
 					replaceBuffer();
 				}
 			}
 		}
 
 		private int getRelativeOffset() {
 			// accumulated log count except current block
 			long accCount = ascending ? currentIndexHeader.ascLogCount : currentIndexHeader.dscLogCount;
 
 			// relative offset in block
 			int relative = (int) (pos - accCount);
 			if (relative < 0)
 				throw new IllegalStateException("relative bug check: " + relative + ", pos " + pos + ", acc: " + accCount);
 			return relative;
 		}
 
 		@Override
 		public void remove() {
 			throw new UnsupportedOperationException("log remove() is not supported");
 		}
 	}
 
 	@Override
 	public List<Log> find(Date from, Date to, List<Long> ids, LogParserBuilder builder) {
 		boolean suppressBugAlert = false;
 		List<Log> ret = new ArrayList<Log>(ids.size());
 		LogParser parser = null;
 		if (builder != null)
 			parser = builder.build();
 
 		for (long id : ids) {
 			LogRecord record = null;
 			try {
 				if (id <= 0)
 					return null;
 
 				int l = 0;
 				int r = indexBlockHeaders.size() - 1;
 				while (r >= l) {
 					int m = (l + r) / 2;
 					IndexBlockHeader header = indexBlockHeaders.get(m);
 
 					if (id < header.firstId)
 						r = m - 1;
 					else if (header.firstId + header.logCount <= id)
 						l = m + 1;
 					else {
 						indexStream.seek(header.fp + (id - header.firstId + 1) * INDEX_ITEM_SIZE);
 						int offset = indexStream.readInt();
 						record = getLogRecord(dataBlockHeaders.get(m), offset);
 						break;
 					}
 				}
 			} catch (IOException e) {
 			}
 			if (record == null)
 				continue;
 			List<Log> result = null;
 			try {
 				result = parse(tableName, parser, LogMarshaler.convert(tableName, record));
 			} catch (LogParserBugException e) {
 				result = new ArrayList<Log>(1);
 				result.add(new Log(e.tableName, e.date, e.id, e.logMap));
 				if (!suppressBugAlert) {
 					logger.error("araqne logstorage: PARSER BUG! original log => table " + e.tableName + ", id " + e.id
 							+ ", data " + e.logMap, e.cause);
 					suppressBugAlert = true;
 				}
 			} finally {
 				if (result != null) {
 					for (Log log : result) {
 						if (from != null || to != null) {
 							Date logDate = log.getDate();
 							if (from != null && logDate.before(from))
 								continue;
 							if (to != null && !logDate.before(to))
 								continue;
 						}
 
 						ret.add(log);
 					}
 				}
 			}
 		}
 
 		return ret;
 	}
 
 	@Override
 	public void traverse(TableScanRequest req) throws IOException, InterruptedException {
 		Date from = req.getFrom();
 		Date to = req.getTo();
 		long minId = req.getMinId();
 		long maxId = req.getMaxId();
 		LogParserBuilder builder = req.getParserBuilder();
 		LogTraverseCallback callback = req.getTraverseCallback();
 		
 		LogParser parser = null;
 		if (builder != null)
 			parser = builder.build();
 
		for (int i = indexBlockHeaders.size() - 1; i >= 0; i--) {
			IndexBlockHeader index = indexBlockHeaders.get(i);
 
 			if ((maxId >= 0 && index.firstId > maxId) || (minId >= 0 && index.firstId + index.logCount <= minId))
 				continue;
 
			DataBlockHeader data = dataBlockHeaders.get(i);
 			Long fromTime = (from == null) ? null : from.getTime();
 			Long toTime = (to == null) ? null : to.getTime();
 			if ((fromTime == null || data.endDate >= fromTime) && (toTime == null || data.startDate < toTime)) {
				if (!readBlock(index, data, fromTime, toTime, minId, maxId, parser, callback))
 					return;
 			}
 		}
 	}
 
 	private boolean readBlock(IndexBlockHeader index, DataBlockHeader data, Long from, Long to, long minId, long maxId,
			LogParser parser, LogTraverseCallback callback) throws IOException, InterruptedException {
 		List<Integer> offsets = new ArrayList<Integer>();
 		boolean suppressBugAlert = false;
 
 		indexStream.seek(index.fp + 4);
 		ByteBuffer indexBuffer = ByteBuffer.allocate(index.logCount * 4);
 		indexStream.read(indexBuffer.array());
 		for (int i = 0; i < index.logCount; i++)
 			offsets.add(indexBuffer.getInt());
 
		// reverse order
 		List<Log> logs = new ArrayList<Log>();
		for (int i = offsets.size() - 1; i >= 0; i--) {
			long date = getLogRecordDate(data, offsets.get(i));
 			if (from != null && date < from)
 				continue;
 			if (to != null && date >= to)
 				continue;
 
			LogRecord record = getLogRecord(data, offsets.get(i));
 			if (minId > 0 && record.getId() < minId)
 				continue;
 			if (maxId > 0 && record.getId() > maxId)
 				continue;
 
 			List<Log> result = null;
 			try {
 				result = parse(tableName, parser, LogMarshaler.convert(tableName, record));
 			} catch (LogParserBugException e) {
 				result = new ArrayList<Log>(1);
 				result.add(new Log(e.tableName, e.date, e.id, e.logMap));
 				if (!suppressBugAlert) {
 					logger.error("araqne logstorage: PARSER BUG! original log => table " + e.tableName + ", id " + e.id
 							+ ", data " + e.logMap, e.cause);
 					suppressBugAlert = true;
 				}
 			} finally {
 				if (result == null)
 					continue;
 
 				logs.addAll(result);
 			}
 		}
 
 		callback.writeLogs(logs);
 
 		return !callback.isEof();
 	}
 }
