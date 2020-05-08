 /*
  * Copyright 2011 Future Systems
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
 
 import java.io.File;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.araqne.log.api.LogParser;
 import org.araqne.log.api.LogParserBugException;
 import org.araqne.log.api.LogParserBuilder;
 import org.araqne.log.api.LogParserInput;
 import org.araqne.log.api.LogParserOutput;
 import org.araqne.logstorage.Log;
 import org.araqne.logstorage.LogMarshaler;
 import org.araqne.logstorage.LogMatchCallback;
 import org.araqne.logstorage.LogTraverseCallback;
 import org.araqne.logstorage.WrongTimeTypeException;
 
 public abstract class LogFileReader {
 	@Deprecated
 	public static LogFileReader getLogFileReader(String tableName, File indexPath, File dataPath) throws InvalidLogFileHeaderException,
 			IOException {
 		LogFileReader reader = null;
 		RandomAccessFile indexHeaderReader = null;
 		RandomAccessFile dataHeaderReader = null;
 
 		try {
 			indexHeaderReader = new RandomAccessFile(indexPath, "r");
 			dataHeaderReader = new RandomAccessFile(dataPath, "r");
 			LogFileHeader indexHeader = LogFileHeader.extractHeader(indexHeaderReader, indexPath);
 			LogFileHeader dataHeader = LogFileHeader.extractHeader(dataHeaderReader, dataPath);
 
 			if (indexHeader.version() != dataHeader.version())
 				throw new InvalidLogFileHeaderException("different log version index and data file");
 
 			if (indexHeader.version() == 1)
 				reader = new LogFileReaderV1(tableName, indexPath, dataPath);
 			else if (indexHeader.version() == 2)
 				reader = new LogFileReaderV2(tableName, indexPath, dataPath);
 			else
 				throw new InvalidLogFileHeaderException("unsupported log version");
 		} finally {
 			if (indexHeaderReader != null)
 				indexHeaderReader.close();
 			if (dataHeaderReader != null)
 				dataHeaderReader.close();
 		}
 
 		return reader;
 	}
 	
 	private static Log parseV1(LogParser parser, Log log) throws LogParserBugException {
 		Map<String, Object> m = null;
 		Object time = log.getDate();
 		try {
 			// can be unmodifiableMap when it comes from memory
 			// buffer.
 			Map<String, Object> m2 = new HashMap<String, Object>();
 			m2.putAll(log.getData());
 			m2.put("_time", log.getDate());
 			Map<String, Object> parsed = parser.parse(m2);
 			if (parsed == null)
 				throw new ParseException("log parse failed", -1);
 
 			parsed.put("_table", log.getTableName());
 			parsed.put("_id", log.getId());
 
 			time = parsed.get("_time");
 			if (time == null) {
 				parsed.put("_time", log.getDate());
 				time = log.getDate();
 			} else if (!(time instanceof Date)) {
 				throw new WrongTimeTypeException(time);
 			}
 
 			m = parsed;
 			return new Log(log.getTableName(), (Date)time, log.getId(), m);
 		} catch (WrongTimeTypeException e) {
 			throw e;
 		} catch (Throwable t) {
 			// can be unmodifiableMap when it comes from memory
 			// buffer.
 			m = new HashMap<String, Object>();
 			m.putAll(log.getData());
 			m.put("_table", log.getTableName());
 			m.put("_id", log.getId());
 			m.put("_time", log.getDate());
 
 			throw new LogParserBugException(t, log.getTableName(), log.getId(), (Date)time, m);
 		}
 	}
 	
 	private static List<Log> parseV2(LogParser parser, Log log) throws LogParserBugException {
 		LogParserInput input = new LogParserInput();
 		input.setDate(log.getDate());
 		input.setSource(log.getTableName());
 		input.setData(log.getData());
 
 		List<Log> ret = new ArrayList<Log>();
 		try {
 			LogParserOutput output = parser.parse(input);
 			if (output != null) {
 				for (Map<String, Object> row : output.getRows()) {
 					row.put("_table", log.getTableName());
 					row.put("_id", log.getId());
 
 					Object time = row.get("_time");
 					if (time == null)
 						row.put("_time", log.getDate());
 					else if (!(time instanceof Date)) {
 						throw new WrongTimeTypeException(time);
 					}
 					
 					ret.add(new Log(log.getTableName(), log.getDate(), log.getId(), row));
 				}
 
 			}
 			return ret;
 		} catch (Throwable t) {
 			// NOTE: log can be unmodifiableMap when it comes from memory buffer.
 			HashMap<String, Object> row = new HashMap<String, Object>(log.getData());
 			row.put("_table", log.getTableName());
 			row.put("_id", log.getId());
 			row.put("_time", log.getDate());
 			
 			throw new LogParserBugException(t, log.getTableName(), log.getId(), log.getDate(), row);
 		}
 		
 	}
 	
	protected static List<Log> parse(String tableName, LogParser parser, LogRecord record) throws LogParserBugException {
		Log log = LogMarshaler.convert(tableName, record);

 		if (parser != null) {
 			if (parser != null && parser.getVersion() == 2) {
 				return parseV2(parser, log);
 			} else {
 				List<Log> ret = new ArrayList<Log>(1);
 				ret.add(parseV1(parser, log));
 				return ret;
 			}
 		} else {
 			// can be unmodifiableMap when it comes from memory
 			// buffer.
 			Map<String, Object> m = new HashMap<String, Object>();
 			m.putAll(log.getData());
 			m.put("_table", log.getTableName());
 			m.put("_id", log.getId());
 			m.put("_time", log.getDate());
 			
 			List<Log> ret = new ArrayList<Log>(1);
 			ret.add(new Log(tableName, log.getDate(), log.getId(), m));
 			return ret;
 		}
 	}
 	
 	public abstract List<Log> find(Date from, Date to, List<Long> ids, LogParserBuilder builder);
 
 	@Deprecated
 	public abstract LogRecord find(long id) throws IOException;
 	@Deprecated
 	public abstract List<LogRecord> find(List<Long> ids) throws IOException;
 
 	public abstract void traverse(long limit, LogMatchCallback callback) throws IOException, InterruptedException;
 
 	public abstract void traverse(long offset, long limit, LogMatchCallback callback) throws IOException, InterruptedException;
 
 	public abstract void traverse(Date from, Date to, long limit, LogMatchCallback callback) throws IOException,
 			InterruptedException;
 
 	public abstract void traverse(Date from, Date to, long offset, long limit, LogMatchCallback callback) throws IOException,
 			InterruptedException;
 
 	public abstract void traverse(Date from, Date to, long minId, long offset, long limit, LogMatchCallback callback)
 			throws IOException,
 			InterruptedException;
 
 	// maxId is inclusive
 	public abstract void traverse(Date from, Date to, long minId, long maxId, long offset, long limit, LogMatchCallback callback,
 			boolean doParallel)
 			throws IOException,
 			InterruptedException;
 
 	public abstract void traverse(Date from, Date to, long minId, long maxId, LogParserBuilder builder, LogTraverseCallback callback,
 			boolean doParallel)
 			throws IOException,
 			InterruptedException;
 
 	public abstract LogRecordCursor getCursor() throws IOException;
 
 	public abstract LogRecordCursor getCursor(boolean ascending) throws IOException;
 
 	/**
 	 * @since 2.2.0
 	 */
 	public abstract LogBlockCursor getBlockCursor() throws IOException;
 
 	/**
 	 * @since 2.2.4
 	 */
 	public abstract File getIndexPath();
 
 	/**
 	 * @since 2.2.4
 	 */
 	public abstract File getDataPath();
 
 	public abstract void close();
 }
