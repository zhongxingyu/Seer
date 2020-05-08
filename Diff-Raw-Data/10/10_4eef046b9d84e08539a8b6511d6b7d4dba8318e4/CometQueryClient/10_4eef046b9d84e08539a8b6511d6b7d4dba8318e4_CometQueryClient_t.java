 /*
  * Copyright 2013 Eediom Inc.
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
 package org.araqne.logdb.client.http;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.NoSuchElementException;
 
 import org.araqne.logdb.client.LogCursor;
 import org.araqne.logdb.client.LogQuery;
 import org.araqne.logdb.client.Message;
 import org.araqne.logdb.client.MessageException;
 import org.araqne.logdb.client.http.impl.Session;
 import org.araqne.logdb.client.http.impl.TrapListener;
 
 public class CometQueryClient implements TrapListener {
 	private Session session;
 	private Map<Integer, LogQuery> queries = new HashMap<Integer, LogQuery>();
 
 	public List<LogQuery> getQueries() {
 		return new ArrayList<LogQuery>(queries.values());
 	}
 
 	public void connect(String host, String loginName, String password) throws IOException {
 		this.session = new Session(host);
 		this.session.login(loginName, password, true);
 		this.session.addListener(this);
 	}
 
 	public LogCursor query(String queryString) throws IOException {
 		int id = createQuery(queryString);
 		startQuery(id);
 		LogQuery q = queries.get(id);
 		q.waitUntil(null);
 		long total = q.getLoadedCount();
 
 		return new LogCursorImpl(id, 0L, total, true);
 	}
 
 	private class LogCursorImpl implements LogCursor {
 
 		private int id;
 		private long offset;
 		private long limit;
 		private boolean removeOnClose;
 
 		private long p;
 		private Map<String, Object> cached;
 		private Long currentCacheOffset;
 		private Long nextCacheOffset;
 		private int fetchUnit;
 		private Map<String, Object> prefetch;
 
 		public LogCursorImpl(int id, long offset, long limit, boolean removeOnClose) {
 			this.id = id;
 			this.offset = offset;
 			this.limit = limit;
 			this.removeOnClose = removeOnClose;
 
 			this.p = offset;
 			this.nextCacheOffset = offset;
 			this.fetchUnit = 1000;
 		}
 
 		@SuppressWarnings("unchecked")
 		@Override
 		public boolean hasNext() {
 			if (prefetch != null)
 				return true;
 
 			if (p < offset || p >= offset + limit)
 				return false;
 
 			try {
 				if (cached == null || p >= currentCacheOffset + fetchUnit) {
 					cached = getResult(id, nextCacheOffset, fetchUnit);
 					currentCacheOffset = nextCacheOffset;
 					nextCacheOffset += fetchUnit;
 				}
 
 				int relative = (int) (p - currentCacheOffset);
 				List<Object> l = (List<Object>) cached.get("result");
 				if (relative >= l.size())
 					return false;
 
 				prefetch = (Map<String, Object>) l.get(relative);
 				p++;
 				return true;
 			} catch (IOException e) {
 				e.printStackTrace();
 				return false;
 			}
 		}
 
 		@Override
 		public Map<String, Object> next() {
 			if (!hasNext())
 				throw new NoSuchElementException("end of log cursor");
 
 			Map<String, Object> m = prefetch;
 			prefetch = null;
 			return m;
 		}
 
 		@Override
 		public void remove() {
 			throw new UnsupportedOperationException();
 		}
 
 		@Override
 		public void close() throws IOException {
 			if (removeOnClose)
 				removeQuery(id);
 		}
 	}
 
 	public int createQuery(String queryString) throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("query", queryString);
 
 		Message resp = session.rpc("org.araqne.logdb.msgbus.LogQueryPlugin.createQuery", params);
 		int id = resp.getInt("id");
 		session.registerTrap("logstorage-query-" + id);
 		session.registerTrap("logstorage-query-timeline-" + id);
 
 		queries.put(id, new LogQuery(id, queryString));
 		return id;
 	}
 
 	public void startQuery(int id) throws IOException {
 		startQuery(id, 10, 10);
 	}
 
 	public void startQuery(int id, int pageSize, int timelineSize) throws IOException {
 		verifyQueryId(id);
 
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("id", id);
 		params.put("offset", 0);
 		params.put("limit", pageSize);
 		params.put("timeline_limit", timelineSize);
 
 		session.rpc("org.araqne.logdb.msgbus.LogQueryPlugin.startQuery", params);
		queries.get(id);
 	}
 
 	public void stopQuery(int id) throws IOException {
 		verifyQueryId(id);
 
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("id", id);
 
 		session.rpc("org.araqne.logdb.msgbus.LogQueryPlugin.stopQuery", params);
 	}
 
 	public void removeQuery(int id) throws IOException {
 		verifyQueryId(id);
 
 		session.unregisterTrap("logstorage-query-" + id);
 		session.unregisterTrap("logstorage-query-timeline-" + id);
 
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("id", id);
 		session.rpc("org.araqne.logdb.msgbus.LogQueryPlugin.removeQuery", params);
 
 		queries.remove(id);
 	}
 
 	public void waitUntil(int id, Long count) {
 		verifyQueryId(id);
 		queries.get(id).waitUntil(count);
 	}
 
 	public Map<String, Object> getResult(int id, long offset, int limit) throws IOException {
 		verifyQueryId(id);
 
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("id", id);
 		params.put("offset", offset);
 		params.put("limit", limit);
 
 		Message resp = session.rpc("org.araqne.logdb.msgbus.LogQueryPlugin.getResult", params);
 		if (resp.getParameters().size() == 0)
 			throw new MessageException("query-not-found", "", resp.getParameters());
 
 		return resp.getParameters();
 	}
 
 	private void verifyQueryId(int id) {
 		if (!queries.containsKey(id))
 			throw new MessageException("query-not-found", "query [" + id + "] does not exist", null);
 	}
 
 	public void close() {
 		if (session != null)
 			session.close();
 	}
 
 	@Override
 	public void onTrap(Message msg) {
 		if (msg.getMethod().startsWith("logstorage-query-timeline")) {
 			int id = msg.getInt("id");
 			LogQuery q = queries.get(id);
 			q.updateCount(msg.getLong("count"));
 			if (msg.getString("type").equals("eof"))
 				q.updateStatus("Ended");
 		} else if (msg.getMethod().startsWith("logstorage-query")) {
 			int id = msg.getInt("id");
 			LogQuery q = queries.get(id);
 			if (msg.getString("type").equals("eof")) {
 				q.updateCount(msg.getLong("total_count"));
 				q.updateStatus("Ended");
 			}
 		}
 	}
 
 	@Override
 	public void onClose(Throwable t) {
 		close();
 	}
 }
