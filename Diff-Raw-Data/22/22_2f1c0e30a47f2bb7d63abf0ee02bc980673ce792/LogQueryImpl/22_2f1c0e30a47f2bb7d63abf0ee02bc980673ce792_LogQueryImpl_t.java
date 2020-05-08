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
 package org.araqne.logdb.query.engine;
 
 import java.io.IOException;
 import java.util.Collections;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.CopyOnWriteArraySet;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.araqne.logdb.LogQuery;
 import org.araqne.logdb.LogQueryCallback;
 import org.araqne.logdb.LogQueryCommand;
 import org.araqne.logdb.RunMode;
 import org.araqne.logdb.LogQueryCommand.Status;
 import org.araqne.logdb.LogQueryContext;
 import org.araqne.logdb.LogResultSet;
 import org.araqne.logdb.LogTimelineCallback;
 import org.araqne.logdb.Session;
 import org.araqne.logdb.query.command.Result;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class LogQueryImpl implements LogQuery {
 	private Logger logger = LoggerFactory.getLogger(LogQueryImpl.class);
 	private static AtomicInteger nextId = new AtomicInteger(1);
 
 	private final int id = nextId.getAndIncrement();
 	private LogQueryContext context;
 	private String queryString;
 	private List<LogQueryCommand> commands;
 	private Date lastStarted;
 	private Result result;
 	private boolean cancelled;
 	private RunMode runMode = RunMode.FOREGROUND;
 
 	private Set<LogQueryCallback> logQueryCallbacks = new CopyOnWriteArraySet<LogQueryCallback>();
 	private Set<LogTimelineCallback> timelineCallbacks = new CopyOnWriteArraySet<LogTimelineCallback>();
 
 	public LogQueryImpl(LogQueryContext context, String queryString, List<LogQueryCommand> commands) {
 		this.context = context;
 		this.queryString = queryString;
 		this.commands = commands;
 	}
 
 	@Override
 	public void run() {
 		if (!isEnd())
 			throw new IllegalStateException("already running");
 
 		lastStarted = new Date();
 		if (commands.isEmpty())
 			return;
 
 		try {
 			result = new Result();
 			result.setLogQuery(this);
 			commands.get(commands.size() - 1).setNextCommand(result);
 			for (LogQueryCallback callback : logQueryCallbacks)
 				result.registerCallback(callback);
 			logQueryCallbacks.clear();
 
 			logger.trace("araqne logdb: run query => {}", queryString);
 			for (LogQueryCommand command : commands)
 				command.init();
 
 			commands.get(0).start();
 		} catch (Exception e) {
 			logger.error("araqne logdb: query failed - " + this, e);
 		}
 	}
 
 	@Override
 	public boolean isAccessible(Session session) {
 		Session querySession = getContext().getSession();
 		if (runMode == RunMode.FOREGROUND)
 			return querySession.equals(session);
 		else
 			return querySession.getLoginName().equals(session.getLoginName());
 	}
 
 	@Override
 	public LogQueryContext getContext() {
 		return context;
 	}
 
 	@Override
 	public boolean isCancelled() {
 		return cancelled;
 	}
 
 	@Override
 	public RunMode getRunMode() {
 		return runMode;
 	}
 
 	@Override
 	public void setRunMode(RunMode runMode, LogQueryContext context) {
 		this.runMode = runMode;
 		if (context != null)
 			this.context = context;
 	}
 
 	@Override
 	public int getId() {
 		return id;
 	}
 
 	@Override
 	public String getQueryString() {
 		return queryString;
 	}
 
 	@Override
	public boolean isEof() {
		return result != null && result.getStatus().equals(Status.End);
	}

	@Override
 	public boolean isEnd() {
 		if (commands.size() == 0)
 			return true;
 		if (result == null)
 			return true;
 		if (commands.get(0).getStatus() == Status.Waiting)
 			return true;
 		return result.getStatus().equals(Status.End);
 	}
 
 	@Override
 	public void purge() {
 		// prevent deleted result file access caused by result check of query
 		// callback or timeline callbacks
 		clearTimelineCallbacks();
 		clearQueryCallbacks();
 
 		if (result != null)
 			result.purge();
 	}
 
 	@Override
 	public void cancel() {
 		if (result == null)
 			return;
 
 		cancelled = true;
 
 		if (result.getStatus() != Status.End && result.getStatus() != Status.Finalizing)
 			result.eof();
 
 		for (int i = commands.size() - 1; i >= 0; i--) {
 			LogQueryCommand command = commands.get(i);
 			if (command.getStatus() != Status.End && command.getStatus() != Status.Finalizing) {
 				command.eof();
 			}
 		}
 	}
 
 	@Override
 	public Date getLastStarted() {
 		return lastStarted;
 	}
 
 	@Override
 	public LogResultSet getResult() throws IOException {
 		if (result != null)
 			return result.getResult();
 		return null;
 	}
 
 	@Override
 	public Long getResultCount() throws IOException {
 		if (result == null)
 			return null;
 
 		LogResultSet rs = null;
 		try {
 			rs = result.getResult();
 			return rs.size();
 		} finally {
 			if (rs != null)
 				rs.close();
 		}
 	}
 
 	@Override
 	public List<Map<String, Object>> getResultAsList() throws IOException {
 		return getResultAsList(0, Integer.MAX_VALUE);
 	}
 
 	@Override
 	public List<Map<String, Object>> getResultAsList(long offset, int limit) throws IOException {
 		LinkedList<Map<String, Object>> l = new LinkedList<Map<String, Object>>();
 
 		LogResultSet rs = null;
 		try {
 			rs = getResult();
 			if (rs == null)
 				return null;
 
 			long p = 0;
 			long count = 0;
 			while (rs.hasNext()) {
 				if (count >= limit)
 					break;
 
 				Map<String, Object> m = rs.next();
 				if (p++ < offset)
 					continue;
 
 				l.add(m);
 				count++;
 			}
 		} finally {
 			rs.close();
 		}
 		return l;
 	}
 
 	@Override
 	public List<LogQueryCommand> getCommands() {
 		return commands;
 	}
 
 	@Override
 	public Set<LogQueryCallback> getLogQueryCallback() {
 		return Collections.unmodifiableSet(logQueryCallbacks);
 	}
 
 	@Override
 	public void registerQueryCallback(LogQueryCallback callback) {
 		logQueryCallbacks.add(callback);
 	}
 
 	@Override
 	public void unregisterQueryCallback(LogQueryCallback callback) {
 		logQueryCallbacks.remove(callback);
 	}
 
 	@Override
 	public void clearQueryCallbacks() {
 		logQueryCallbacks.clear();
 	}
 
 	@Override
 	public Set<LogTimelineCallback> getTimelineCallbacks() {
 		return Collections.unmodifiableSet(timelineCallbacks);
 	}
 
 	@Override
 	public void registerTimelineCallback(LogTimelineCallback callback) {
 		timelineCallbacks.add(callback);
 	}
 
 	@Override
 	public void unregisterTimelineCallback(LogTimelineCallback callback) {
 		timelineCallbacks.remove(callback);
 	}
 
 	@Override
 	public void clearTimelineCallbacks() {
 		timelineCallbacks.clear();
 	}
 
 	@Override
 	public String toString() {
 		return "id=" + id + ", query=" + queryString;
 	}
 
 }
