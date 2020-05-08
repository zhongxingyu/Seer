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
 package org.araqne.logdb.msgbus;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.zip.Deflater;
 import java.util.zip.GZIPOutputStream;
 
 import org.apache.felix.ipojo.annotations.Component;
 import org.apache.felix.ipojo.annotations.Invalidate;
 import org.apache.felix.ipojo.annotations.Requires;
 import org.apache.felix.ipojo.annotations.Validate;
 import org.araqne.codec.Base64;
 import org.araqne.codec.FastEncodingRule;
 import org.araqne.cron.AbstractTickTimer;
 import org.araqne.cron.TickService;
 import org.araqne.logdb.Query;
 import org.araqne.logdb.QueryContext;
 import org.araqne.logdb.QueryParseException;
 import org.araqne.logdb.QueryParserService;
 import org.araqne.logdb.QueryResult;
 import org.araqne.logdb.QueryResultCallback;
 import org.araqne.logdb.QueryResultSet;
 import org.araqne.logdb.QueryService;
 import org.araqne.logdb.QueryStatusCallback;
 import org.araqne.logdb.QueryStopReason;
 import org.araqne.logdb.Row;
 import org.araqne.logdb.RowBatch;
 import org.araqne.logdb.RunMode;
 import org.araqne.logdb.SavedResult;
 import org.araqne.logdb.SavedResultManager;
 import org.araqne.logdb.impl.QueryHelper;
 import org.araqne.logstorage.Log;
 import org.araqne.logstorage.LogStorage;
 import org.araqne.logstorage.LogTableRegistry;
 import org.araqne.msgbus.MsgbusException;
 import org.araqne.msgbus.PushApi;
 import org.araqne.msgbus.Request;
 import org.araqne.msgbus.Response;
 import org.araqne.msgbus.Session;
 import org.araqne.msgbus.handler.MsgbusMethod;
 import org.araqne.msgbus.handler.MsgbusPlugin;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @Component(name = "logdb-logquery-msgbus")
 @MsgbusPlugin
 public class LogQueryPlugin {
 	private final Logger logger = LoggerFactory.getLogger(LogQueryPlugin.class.getName());
 	private final Logger streamLogger = LoggerFactory.getLogger(LogQueryPlugin.class.getName() + "-stream");
 
 	private static final int GENERAL_QUERY_FAILURE_CODE = 1;
 	private static final int DEFAULT_STREAM_FLUSH_SIZE = 10000;
 
 	// milliseconds
 	private static final int DEFAULT_STREAM_FLUSH_INTERVAL = 1000;
 
 	@Requires
 	private QueryService service;
 
 	@Requires
 	private QueryParserService parserService;
 
 	@Requires
 	private LogTableRegistry tableRegistry;
 
 	@Requires
 	private LogStorage storage;
 
 	@Requires
 	private PushApi pushApi;
 
 	@Requires
 	private SavedResultManager savedResultManager;
 
 	@Requires
 	private TickService tickService;
 
 	private StreamingResultEncoder streamingEncoder;
 	private StreamingResultDecoder streamingDecoder;
 
 	@Validate
 	public void start() {
 		int poolSize = Math.min(8, Runtime.getRuntime().availableProcessors());
 		streamingEncoder = new StreamingResultEncoder("Streaming Result Encoder", poolSize);
 		streamingDecoder = new StreamingResultDecoder("Streaming Result Decoder", poolSize);
 	}
 
 	@Invalidate
 	public void stop() {
 		if (streamingEncoder != null) {
 			streamingEncoder.close();
 			streamingEncoder = null;
 		}
 
 		if (streamingDecoder != null) {
 			streamingDecoder.close();
 			streamingDecoder = null;
 		}
 	}
 
 	@MsgbusMethod
 	public void logs(Request req, Response resp) {
 		String tableName = req.getString("table");
 		int limit = req.getInteger("limit");
 		int offset = 0;
 		if (req.has("offset"))
 			offset = req.getInteger("offset");
 
 		if (!tableRegistry.exists(tableName))
 			throw new MsgbusException("logdb", "table-not-exists");
 
 		Collection<Log> logs = storage.getLogs(tableName, null, null, offset, limit);
 		List<Object> serialized = new ArrayList<Object>(limit);
 		for (Log log : logs)
 			serialized.add(serialize(log));
 
 		resp.put("logs", serialized);
 	}
 
 	private Map<String, Object> serialize(Log log) {
 		Map<String, Object> m = new HashMap<String, Object>();
 		m.put("table", log.getTableName());
 		m.put("id", log.getId());
 		m.put("date", log.getDate());
 		m.put("data", log.getData());
 		return m;
 	}
 
 	@MsgbusMethod
 	public void queries(Request req, Response resp) {
 		org.araqne.logdb.Session dbSession = getDbSession(req);
 		List<Object> result = QueryHelper.getQueries(dbSession, service);
 		resp.put("queries", result);
 	}
 
 	@MsgbusMethod
 	public void createQuery(Request req, Response resp) {
 		try {
 			org.araqne.logdb.Session dbSession = getDbSession(req);
 			Query query = service.createQuery(dbSession, req.getString("query"));
 			resp.put("id", query.getId());
 		} catch (QueryParseException e) {
 			if (e.getParams() == null)
 				throw new MsgbusException("logdb", e.getMessage());
 
 			Map<String, Object> params = new HashMap<String, Object>();
 			params.putAll(e.getParams());
 			throw new MsgbusException("logdb", e.getMessage(), params);
 		} catch (Exception e) {
 			logger.error("araqne logdb: cannot create query", e);
 			throw new MsgbusException("logdb", e.getMessage());
 		}
 	}
 
 	@MsgbusMethod
 	public void removeQuery(Request req, Response resp) {
 		int id = req.getInteger("id", true);
 		org.araqne.logdb.Session dbSession = getDbSession(req);
 		service.removeQuery(dbSession, id);
 	}
 
 	private org.araqne.logdb.Session getDbSession(Request req) {
 		return getDbSession(req.getSession());
 	}
 
 	private org.araqne.logdb.Session getDbSession(Session session) {
 		return (org.araqne.logdb.Session) session.get("araqne_logdb_session");
 	}
 
 	@MsgbusMethod
 	public void startQuery(Request req, Response resp) {
 		String orgDomain = req.getOrgDomain();
 		int id = req.getInteger("id");
 		boolean streaming = false;
 		if (req.getBoolean("streaming") != null)
 			streaming = req.getBoolean("streaming");
 
 		String compression = "deflate";
 		if (req.getString("compression") != null) {
 			compression = req.getString("compression");
 			if (!compression.equals("gzip") && !compression.equals("none"))
 				throw new MsgbusException("logdb", "invalid-compression-type");
 		}
 
 		int streamFlushSize = DEFAULT_STREAM_FLUSH_SIZE;
 		int streamFlushInterval = DEFAULT_STREAM_FLUSH_INTERVAL;
 
 		Query query = service.getQuery(id);
 
 		// validation check
 		if (query == null) {
 			Map<String, Object> params = new HashMap<String, Object>();
 			params.put("query_id", id);
 			throw new MsgbusException("logdb", "query not found", params);
 		}
 
 		if (query.isStarted())
 			throw new MsgbusException("logdb", "already running");
 
 		// set query and timeline callback
 		QueryResultCallback qc = new MsgbusQueryResultCallback(query, orgDomain, streaming, compression, streamFlushSize,
 				streamFlushInterval);
 		QueryResult result = query.getResult();
 		result.setStreaming(streaming);
 		result.getResultCallbacks().add(qc);
 
 		QueryStatusCallback qs = new MsgbusStatusCallback(orgDomain);
 		query.getCallbacks().getStatusCallbacks().add(qs);
 
 		// start query
 		service.startQuery(query.getId());
 	}
 
 	@MsgbusMethod
 	public void stopQuery(Request req, Response resp) {
 		int id = req.getInteger("id", true);
 		Query query = service.getQuery(id);
 		if (query != null)
 			query.stop(QueryStopReason.UserRequest);
 		else {
 			Map<String, Object> params = new HashMap<String, Object>();
 			params.put("query_id", id);
 			throw new MsgbusException("logdb", "query-not-found", params);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	@MsgbusMethod
 	public void insertBatch(Request req, Response resp) {
 		// isAdmin
 		org.araqne.logdb.Session dbSession = getDbSession(req);
 		if (!dbSession.isAdmin())
 			throw new IllegalStateException("no permission");
 
 		// decode
 		if (!req.has("bins") || !req.has("table"))
 			throw new IllegalStateException("no data");
 		try {
 			String tableName = req.getString("table");
 			List<Map<String, Object>> chunk = (List<Map<String, Object>>) req.get("bins");
 			List<Object> l = streamingDecoder.decode(chunk);
 
 			for (Object m : l) {
 				Map<String, Object> data = (Map<String, Object>) m;
 				Date date = (Date) data.get("_time");
 				Log log = new Log(tableName, date, data);
 				// storage.writeBatch(log);
 				try {
 					storage.write(log);
 				} catch (InterruptedException e) {
 					logger.warn("storage.write interrupted", e);
 				}
 
 			}
 		} catch (ExecutionException e) {
 			logger.error("araqne logdb : cannot insert data", e);
 		}
 	}
 
 	@MsgbusMethod
 	public void getResult(Request req, Response resp) throws IOException {
 		int id = req.getInteger("id", true);
 		int offset = req.getInteger("offset", true);
 		int limit = req.getInteger("limit", true);
 		Boolean binaryEncode = req.getBoolean("binary_encode");
 		String compression = req.getString("compression");
 		boolean useGzip = compression != null && compression.equals("gzip");
 
 		Map<String, Object> m = QueryHelper.getResultData(service, id, offset, limit);
 		if (m == null)
 			return;
 
 		FastEncodingRule enc = new FastEncodingRule();
 		if (binaryEncode != null && binaryEncode) {
 			ByteBuffer binary = enc.encode(m);
 			int uncompressedSize = binary.array().length;
 			byte[] b = compress(binary.array(), useGzip);
 			resp.put("binary", new String(Base64.encode(b)));
 			resp.put("uncompressed_size", uncompressedSize);
 		} else
 			resp.putAll(m);
 	}
 
 	private byte[] compress(byte[] b, boolean useGzip) throws IOException {
 		ByteArrayOutputStream bos = new ByteArrayOutputStream(b.length);
 
 		if (useGzip) {
 			GZIPOutputStream zos = new GZIPOutputStream(bos);
 			try {
 				zos.write(b);
 				zos.finish();
 				return bos.toByteArray();
 			} finally {
 				zos.close();
 			}
 		} else {
 			Deflater c = new Deflater();
 			try {
 				c.reset();
 				c.setInput(b);
 				c.finish();
 				byte[] compressed = new byte[b.length];
 
 				while (true) {
 					int compressedSize = c.deflate(compressed);
 					if (compressedSize == 0)
 						break;
 					bos.write(compressed, 0, compressedSize);
 				}
 
 				return bos.toByteArray();
 			} finally {
 				c.end();
 			}
 		}
 	}
 
 	/**
 	 * @since 0.17.0
 	 */
 	@MsgbusMethod
 	public void setRunMode(Request req, Response resp) {
 		int id = req.getInteger("id", true);
 		boolean background = req.getBoolean("background", true);
 
 		Query query = service.getQuery(id);
 		if (query == null)
 			throw new MsgbusException("logdb", "query-not-found");
 
 		org.araqne.logdb.Session dbSession = getDbSession(req);
 
 		if (!query.isAccessible(dbSession))
 			throw new MsgbusException("logdb", "no-permission");
 
 		query.setRunMode(background ? RunMode.BACKGROUND : RunMode.FOREGROUND, new QueryContext(dbSession));
 	}
 
 	/**
 	 * @since 1.4.0
 	 */
 	@MsgbusMethod
 	public void queryStatus(Request req, Response resp) {
 		int id = req.getInteger("id", true);
 		org.araqne.logdb.Session dbSession = getDbSession(req);
 
 		Query query = service.getQuery(dbSession, id);
 		if (query == null)
 			throw new MsgbusException("logdb", "query-not-found");
 
 		resp.putAll(QueryHelper.getQuery(query));
 	}
 
 	@MsgbusMethod
 	public void getSavedResults(Request req, Response resp) {
 		org.araqne.logdb.Session dbSession = getDbSession(req);
 
 		Integer offset = req.getInteger("offset");
 		Integer limit = req.getInteger("limit");
 
 		List<SavedResult> l = savedResultManager.getResultList(dbSession.getLoginName());
 
 		// make sublist for offset and limit
 		List<SavedResult> subList = subList(l, offset, limit);
 
 		List<Object> savedResults = new ArrayList<Object>();
 		for (SavedResult s : subList) {
 			Map<String, Object> m = new HashMap<String, Object>();
 			m.put("guid", s.getGuid());
 			m.put("title", s.getTitle());
 			m.put("file_size", s.getFileSize());
 			m.put("created", s.getCreated());
 			m.put("owner", s.getOwner());
 			m.put("row_count", s.getRowCount());
 			m.put("storage", s.getStorageName());
 			m.put("index_path", s.getIndexPath());
 			m.put("data_path", s.getDataPath());
 			savedResults.add(m);
 		}
 		resp.put("total", l.size());
 		resp.put("saved_results", savedResults);
 	}
 
 	public static <T> List<T> subList(List<T> list, int offset, int limit) {
 		if (offset < 0)
 			throw new IllegalArgumentException("Offset must be more than 0");
 
 		if (limit < -1)
 			throw new IllegalArgumentException("Limit must be more than -1");
 
 		if (offset > 0) {
 			if (offset >= list.size()) {
 				// return empty.
 				return list.subList(0, 0);
 			}
 			if (limit > -1) {
 				// apply offset and limit
 				return list.subList(offset, Math.min(offset + limit, list.size()));
 			} else {
 				// apply just offset
 				return list.subList(offset, list.size());
 			}
 		} else if (limit > -1) {
 			// apply just limit
 			return list.subList(0, Math.min(limit, list.size()));
 		} else {
 			return list.subList(0, list.size());
 		}
 	}
 
 	/**
 	 * @since 1.6.8
 	 */
 	@MsgbusMethod
 	public void saveResult(Request req, Response resp) {
 		String title = req.getString("title", true);
 		int queryId = req.getInteger("query_id", true);
 		Query query = service.getQuery(queryId);
 		if (query == null)
 			throw new MsgbusException("logdb", "query-not-found");
 
 		QueryResultSet rs = null;
 		try {
 			rs = query.getResultSet();
 			long total = rs.getIndexPath().length() + rs.getDataPath().length();
 
 			org.araqne.logdb.Session dbSession = getDbSession(req);
 
 			SavedResult sr = new SavedResult();
 			sr.setStorageName(rs.getStorageName());
 			sr.setOwner(dbSession.getLoginName());
 			sr.setQueryString(query.getQueryString());
 			sr.setTitle(title);
 			sr.setIndexPath(rs.getIndexPath().getAbsolutePath());
 			sr.setDataPath(rs.getDataPath().getAbsolutePath());
 			sr.setRowCount(rs.size());
 			sr.setFileSize(total);
 
 			savedResultManager.saveResult(sr);
 			resp.put("guid", sr.getGuid());
 		} catch (IOException e) {
 			logger.error("araqne logdb: cannot save result of query " + query.getId(), e);
 			throw new MsgbusException("logdb", "io-error");
 		} finally {
 			if (rs != null)
 				rs.close();
 		}
 	}
 
 	/**
 	 * @since 1.6.8
 	 */
 	@MsgbusMethod
 	public void deleteResult(Request req, Response resp) {
 		org.araqne.logdb.Session dbSession = getDbSession(req);
 		String guid = req.getString("guid", true);
 		try {
 			SavedResult sr = savedResultManager.getResult(guid);
 			if (sr == null)
 				throw new MsgbusException("logdb", "saved-result-not-found");
 
 			if (!sr.getOwner().equals(dbSession.getLoginName()))
 				throw new MsgbusException("logdb", "no-permission");
 
 			savedResultManager.deleteResult(guid);
 		} catch (IOException e) {
 			throw new MsgbusException("logdb", "io-error");
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	@MsgbusMethod
 	public void deleteResults(Request req, Response resp) {
 		org.araqne.logdb.Session dbSession = getDbSession(req);
 		List<String> guids = (List<String>) req.get("guids", true);
 
 		try {
 			for (String guid : guids) {
 				SavedResult sr = savedResultManager.getResult(guid);
 				if (sr == null)
 					throw new MsgbusException("logdb", "saved-result-not-found");
 
 				if (!sr.getOwner().equals(dbSession.getLoginName()))
 					throw new MsgbusException("logdb", "no-permission");
 
 				savedResultManager.deleteResult(guid);
 			}
 		} catch (IOException e) {
 			throw new MsgbusException("logdb", "io-error");
 		}
 	}
 
 	private class MsgbusStatusCallback implements QueryStatusCallback {
 		private String orgDomain;
 
 		private MsgbusStatusCallback(String orgDomain) {
 			this.orgDomain = orgDomain;
 		}
 
 		@Override
 		public void onChange(Query query) {
 			if (query.isFinished()) {
 				try {
 					Map<String, Object> m = new HashMap<String, Object>();
 					m.put("id", query.getId());
 					m.put("type", "eof");
 					m.put("total_count", query.getResultCount());
 					m.put("stamp", query.getNextStamp());
 
 					// @since 2.2.17
 					if (query.getCause() != null) {
 						m.put("error_code", GENERAL_QUERY_FAILURE_CODE);
 						m.put("error_detail", query.getCause().getMessage() != null ? query.getCause().getMessage() : query
 								.getCause().getClass().getName());
 					}
 
 					pushApi.push(orgDomain, "logdb-query-" + query.getId(), m);
 					pushApi.push(orgDomain, "logstorage-query-" + query.getId(), m); // deprecated
 
 					query.getCallbacks().getStatusCallbacks().remove(this);
 				} catch (IOException e) {
 					logger.error("araqne logdb: msgbus push fail", e);
 				}
 			} else {
 				try {
 					String status = null;
 					if (!query.isStarted())
 						status = "Waiting";
 					else
 						status = query.isFinished() ? "End" : "Running";
 
 					Map<String, Object> m = new HashMap<String, Object>();
 					m.put("id", query.getId());
 					m.put("type", "status_change");
 					m.put("status", status);
 					m.put("count", query.getResultCount());
 					m.put("stamp", query.getNextStamp());
 					pushApi.push(orgDomain, "logdb-query-" + query.getId(), m);
 					pushApi.push(orgDomain, "logstorage-query-" + query.getId(), m); // deprecated
 				} catch (IOException e) {
 					logger.error("araqne logdb: msgbus push fail", e);
 				}
 			}
 		}
 	}
 
 	private class MsgbusQueryResultCallback extends AbstractTickTimer implements QueryResultCallback {
 		private Query query;
 		private String orgDomain;
 
 		private final String callbackName;
 		private boolean streaming;
 		private boolean noCompression;
 		private boolean useGzip;
 		private int streamFlushSize;
 		private int streamFlushInterval;
 		private ArrayList<Object> rows = new ArrayList<Object>(10000);
 		private AtomicBoolean closed = new AtomicBoolean();
 
 		private MsgbusQueryResultCallback(Query query, String orgDomain, boolean streaming, String compression,
 				int streamFlushSize, int streamFlushInterval) {
 			this.query = query;
 			this.orgDomain = orgDomain;
 			this.streaming = streaming;
 			this.streamFlushSize = streamFlushSize;
 			this.streamFlushInterval = streamFlushInterval;
 			this.useGzip = compression != null && compression.equals("gzip");
 			this.noCompression = compression != null && compression.equals("none");
 			this.callbackName = "logdb-query-result-" + query.getId();
 
 			tickService.addTimer(this);
 		}
 
 		@Override
 		public int getInterval() {
 			return streamFlushInterval;
 		}
 
 		@Override
 		public void onTick() {
 			synchronized (rows) {
 				flushResultSet(query, false);
 			}
 		}
 
 		@Override
 		public void onRow(Query query, Row row) {
 			if (!streaming)
 				return;
 
 			synchronized (rows) {
 				rows.add(row.map());
 				if (rows.size() >= streamFlushSize)
 					flushResultSet(query, false);
 			}
 		}
 
 		@Override
 		public void onRowBatch(Query query, RowBatch rowBatch) {
 			if (!streaming)
 				return;
 
 			synchronized (rows) {
 				if (rowBatch.selectedInUse) {
 					for (int i = 0; i < rowBatch.size; i++) {
 						int p = rowBatch.selected[i];
 						Row row = rowBatch.rows[p];
 						rows.add(row.map());
 					}
 				} else {
 					for (Row row : rowBatch.rows)
 						rows.add(row.map());
 				}
 
 				if (rows.size() >= streamFlushSize)
 					flushResultSet(query, false);
 			}
 		}
 
 		@Override
 		public void onClose(Query query, QueryStopReason reason) {
 			if (!closed.compareAndSet(false, true))
 				return;
 
 			tickService.removeTimer(this);
 
 			synchronized (rows) {
 				flushResultSet(query, true);
 			}
 		}
 
 		private void flushResultSet(Query query, boolean last) {
 			if (!last && rows.isEmpty())
 				return;
 
 			streamLogger.debug("araqne logdb: flushing stream of query [{}], rows [{}]", query.getId(), rows.size());
 
 			try {
 				if (noCompression) {
 					Map<String, Object> m = new HashMap<String, Object>();
 					m.put("rows", rows);
 					m.put("last", last);
 
 					pushApi.push(orgDomain, callbackName, m);
 					rows.clear();
 				} else {
 					List<Map<String, Object>> bins = streamingEncoder.encode(rows, useGzip);
 
 					Map<String, Object> m = new HashMap<String, Object>();
 					m.put("bins", bins);
 					m.put("last", last);
 
 					pushApi.push(orgDomain, callbackName, m);
 					rows.clear();
 				}
 			} catch (Throwable t) {
 				logger.error("araqne logdb: cannot encode streaming result", t);
 			}
 		}
 	}
 }
