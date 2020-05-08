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
 package org.araqne.logdb.query.command;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.Map;
 import java.util.PriorityQueue;
 import java.util.Queue;
 import java.util.Set;
 import java.util.concurrent.CopyOnWriteArraySet;
 
 import org.araqne.codec.EncodingRule;
 import org.araqne.logdb.LogMap;
 import org.araqne.logdb.LogQueryCallback;
 import org.araqne.logdb.LogQueryCommand;
 import org.araqne.logdb.LogResultSet;
 import org.araqne.logstorage.Log;
 import org.araqne.logstorage.file.LogFileReaderV2;
 import org.araqne.logstorage.file.LogFileWriterV2;
 import org.araqne.logstorage.file.LogRecord;
 import org.araqne.logstorage.file.LogRecordCursor;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class Result extends LogQueryCommand {
 	private static File BASE_DIR = new File(System.getProperty("araqne.data.dir"), "araqne-logdb/query/");
 	private final Logger logger = LoggerFactory.getLogger(Result.class);
 	private LogFileWriterV2 writer;
 	private File indexPath;
 	private File dataPath;
 	private long count;
 
 	private Set<LogQueryCallback> callbacks;
 	private Queue<LogQueryCallbackInfo> callbackQueue;
 	private Integer nextCallback;
 	private long nextStatusChangeCallback;
 
 	/**
 	 * index and data file is deleted by user request
 	 */
 	private volatile boolean purged;
 
 	private volatile boolean eofCalled;
 	private volatile Date eofDate;
 
 	public Result() throws IOException {
 		this("");
 	}
 
 	public Result(String tag) throws IOException {
 		callbacks = new CopyOnWriteArraySet<LogQueryCallback>();
 		callbackQueue = new PriorityQueue<Result.LogQueryCallbackInfo>(11, new CallbackInfoComparator());
 
 		BASE_DIR.mkdirs();
 		indexPath = File.createTempFile("result-" + tag, ".idx", BASE_DIR);
 		dataPath = File.createTempFile("result-" + tag, ".dat", BASE_DIR);
 		writer = new LogFileWriterV2(indexPath, dataPath, 1024 * 1024, 1);
 	}
 
 	public Date getEofDate() {
 		return eofDate;
 	}
 
 	public long getCount() {
 		return count;
 	}
 
 	private class LogQueryCallbackInfo {
 		private int size;
 		private LogQueryCallback callback;
 
 		public LogQueryCallbackInfo(LogQueryCallback callback) {
 			this.size = callback.offset() + callback.limit();
 			this.callback = callback;
 		}
 
 		@Override
 		public int hashCode() {
 			final int prime = 31;
 			int result = 1;
 			result = prime * result + getOuterType().hashCode();
 			result = prime * result + ((callback == null) ? 0 : callback.hashCode());
 			result = prime * result + size;
 			return result;
 		}
 
 		@Override
 		public boolean equals(Object obj) {
 			if (this == obj)
 				return true;
 			if (obj == null)
 				return false;
 			if (getClass() != obj.getClass())
 				return false;
 			LogQueryCallbackInfo other = (LogQueryCallbackInfo) obj;
 			if (!getOuterType().equals(other.getOuterType()))
 				return false;
 			if (callback == null) {
 				if (other.callback != null)
 					return false;
 			} else if (!callback.equals(other.callback))
 				return false;
 			if (size != other.size)
 				return false;
 			return true;
 		}
 
 		private Result getOuterType() {
 			return Result.this;
 		}
 	}
 
 	private class CallbackInfoComparator implements Comparator<LogQueryCallbackInfo> {
 		@Override
 		public int compare(LogQueryCallbackInfo o1, LogQueryCallbackInfo o2) {
 			return o1.size - o2.size;
 		}
 	}
 
 	@Override
 	public void push(LogMap m) {
 		try {
 			synchronized (writer) {
 				writer.write(new Log("$Result$", new Date(), count + 1, m.map()));
 			}
 		} catch (IOException e) {
 			// cancel query when disk is full
 			File dir = indexPath.getParentFile();
 			if (dir != null && dir.getFreeSpace() == 0)
 				eof(true);
 
 			throw new IllegalStateException(e);
 		}
 		count++;
 
 		while (nextCallback != null && count >= nextCallback) {
 			LogQueryCallback callback = callbackQueue.poll().callback;
 			callback.onPageLoaded();
 			if (callbackQueue.isEmpty())
 				nextCallback = null;
 			else
 				nextCallback = callbackQueue.peek().size;
 		}
 
 		if (nextStatusChangeCallback < System.currentTimeMillis()) {
 			if (logger.isDebugEnabled())
 				logger.debug("araqne logdb: result next status change callback, size={}", callbacks.size());
 
 			for (LogQueryCallback callback : callbacks)
 				callback.onQueryStatusChange();
 			nextStatusChangeCallback = System.currentTimeMillis() + 2000;
 		}
 	}
 
 	@Override
 	public boolean isReducer() {
 		return false;
 	}
 
 	public void registerCallback(LogQueryCallback callback) {
 		callbacks.add(callback);
 		callbackQueue.add(new LogQueryCallbackInfo(callback));
 		nextCallback = this.callbackQueue.peek().size;
 	}
 
 	public void unregisterCallback(LogQueryCallback callback) {
 		callbacks.remove(callback);
 		callbackQueue.remove(new LogQueryCallbackInfo(callback));
 		nextCallback = null;
 		if (!this.callbackQueue.isEmpty())
 			nextCallback = this.callbackQueue.peek().size;
 	}
 
 	public LogResultSet getResult() throws IOException {
 		if (purged) {
 			String msg = "query result file is already purged, index=" + indexPath.getAbsolutePath() + ", data="
 					+ dataPath.getAbsolutePath();
 			throw new IOException(msg);
 		}
 
 		syncWriter();
 
 		// TODO : check tableName
		LogFileReaderV2 reader = null;
		try {
			reader = new LogFileReaderV2(null, indexPath, dataPath);
			return new LogResultSetImpl(reader, count);
		} catch (Throwable t) {
			if (reader != null)
				reader.close();
			throw new IOException(t);
		}
 	}
 
 	public void syncWriter() throws IOException {
 		synchronized (writer) {
 			writer.flush();
 			writer.sync();
 		}
 	}
 
 	@Override
 	public void eof(boolean canceled) {
 		if (eofCalled)
 			return;
 
 		eofCalled = true;
 		this.status = Status.Finalizing;
 
 		try {
 			synchronized (writer) {
 				writer.close();
 			}
 		} catch (IOException e) {
 		}
 
 		eofDate = new Date();
 
 		super.eof(canceled);
 		for (LogQueryCallback callback : callbacks)
 			callback.onEof(canceled);
 	}
 
 	public void purge() {
 		purged = true;
 
 		// clear query callbacks
 		callbacks.clear();
 		callbackQueue.clear();
 		nextCallback = null;
 
 		// delete files
 		indexPath.delete();
 		dataPath.delete();
 	}
 
 	private static class LogResultSetImpl implements LogResultSet {
 		private LogFileReaderV2 reader;
 		private LogRecordCursor cursor;
 		private long count;
 
 		public LogResultSetImpl(LogFileReaderV2 reader, long count) throws IOException {
 			this.reader = reader;
 			this.cursor = reader.getCursor(true);
 			this.count = count;
 		}
 
 		@Override
 		public File getIndexPath() {
 			return reader.getIndexPath();
 		}
 
 		@Override
 		public File getDataPath() {
 			return reader.getDataPath();
 		}
 
 		@Override
 		public long size() {
 			return count;
 		}
 
 		@Override
 		public boolean hasNext() {
 			return cursor.hasNext();
 		}
 
 		@Override
 		public Map<String, Object> next() {
 			LogRecord next = cursor.next();
 			return EncodingRule.decodeMap(next.getData());
 		}
 
 		@Override
 		public void reset() {
 			cursor.reset();
 		}
 
 		@Override
 		public void remove() {
 			throw new UnsupportedOperationException();
 		}
 
 		@Override
 		public void skip(long n) {
 			cursor.skip(n);
 		}
 
 		@Override
 		public void close() {
 			reader.close();
 		}
 	}
 }
