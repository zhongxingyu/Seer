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
 package org.araqne.logdb.logapi;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.TimeUnit;
 
 import org.araqne.log.api.AbstractLogger;
 import org.araqne.log.api.Log;
 import org.araqne.log.api.LogPipe;
 import org.araqne.log.api.Logger;
 import org.araqne.log.api.LoggerFactory;
 import org.araqne.log.api.LoggerRegistry;
 import org.araqne.log.api.LoggerRegistryEventListener;
 import org.araqne.log.api.LoggerSpecification;
 import org.araqne.log.api.SimpleLog;
 import org.araqne.logdb.LogMap;
 import org.araqne.logdb.LogQuery;
 import org.araqne.logdb.LogQueryCommand;
 
 /**
  * @since 1.7.8
  * @author xeraph
  * 
  */
 public class QueryTransformLogger extends AbstractLogger implements LoggerRegistryEventListener, LogPipe {
 	private final org.slf4j.Logger slog = org.slf4j.LoggerFactory.getLogger(QueryTransformLogger.class.getName());
 
 	private LoggerRegistry loggerRegistry;
 
 	/**
 	 * full name of data source logger
 	 */
 	private String loggerName;
 
 	private QueryResult queryResult = new QueryResult();
 	private LogQueryCommand first;
 	private Log currentLog;
 
 	private volatile boolean stopRunner = false;
 	private QueryRunner runner;
 	private ArrayBlockingQueue<Log> queue = new ArrayBlockingQueue<Log>(100000);
 
 	public QueryTransformLogger(LoggerSpecification spec, LoggerFactory factory, LoggerRegistry loggerRegistry, LogQuery q) {
 		super(spec, factory);
 		this.loggerRegistry = loggerRegistry;
 
 		Map<String, String> config = spec.getConfig();
 		loggerName = config.get("source_logger");
 
 		List<LogQueryCommand> commands = q.getCommands();
 		first = commands.get(0);
 		commands.add(queryResult);
 
 		for (int i = commands.size() - 2; i >= 0; i--)
 			commands.get(i).setNextCommand(commands.get(i + 1));
 	}
 
 	@Override
 	protected void onStart() {
 		if (runner == null) {
 			runner = new QueryRunner();
			runner.start();
 		}
 
 		loggerRegistry.addListener(this);
 		Logger logger = loggerRegistry.getLogger(loggerName);
 
 		if (logger != null) {
 			slog.debug("araqne logdb: connect pipe to source logger [{}]", loggerName);
 			logger.addLogPipe(this);
 		} else
 			slog.debug("araqne logdb: source logger [{}] not found", loggerName);
 	}
 
 	@Override
 	protected void onStop() {
 		try {
 			stopRunner = true;
 			runner.interrupt();
 			try {
 				runner.join(5000);
 			} catch (InterruptedException e) {
 				slog.info("araqne logdb: failed to join query runner, logger [{}]", getFullName());
 			}
 
 			if (loggerRegistry != null) {
 				Logger logger = loggerRegistry.getLogger(loggerName);
 				if (logger != null) {
 					slog.debug("araqne logdb: disconnect pipe from source logger [{}]", loggerName);
 					logger.removeLogPipe(this);
 				}
 
 				loggerRegistry.removeListener(this);
 			}
 		} catch (RuntimeException e) {
			if (e.getMessage() == null || !e.getMessage().endsWith("unavailable"))
 				throw e;
 		}
 	}
 
 	@Override
 	public boolean isPassive() {
 		return true;
 	}
 
 	@Override
 	protected void runOnce() {
 	}
 
 	@Override
 	public void loggerAdded(Logger logger) {
 		if (logger.getFullName().equals(loggerName)) {
 			slog.debug("araqne logdb: source logger [{}] loaded", loggerName);
 			logger.addLogPipe(this);
 		}
 	}
 
 	@Override
 	public void loggerRemoved(Logger logger) {
 		if (logger.getFullName().equals(loggerName)) {
 			slog.debug("araqne logdb: source logger [{}] unloaded", loggerName);
 			logger.removeLogPipe(this);
 		}
 	}
 
 	@Override
 	public void onLog(Logger logger, Log log) {
 		try {
 			if (isRunning())
 				queue.put(log);
 		} catch (Throwable t) {
 			slog.error("araqne logdb: cannot evaluate query, log [" + log.getParams() + "], logger " + getFullName(), t);
 		}
 	}
 
 	private class QueryResult extends LogQueryCommand {
 		@Override
 		public void push(LogMap m) {
 			Date date = currentLog.getDate();
 			SimpleLog log = new SimpleLog(date, loggerName, m.map());
 			QueryTransformLogger.this.write(log);
 		}
 	}
 
 	private class QueryRunner extends Thread {
 
 		@Override
 		public void run() {
 			try {
 				slog.info("araqne logdb: begin query runner, logger [{}]", getFullName());
 				ArrayList<Log> buffer = new ArrayList<Log>(10002);
 				while (!stopRunner) {
 					Log log = null;
 					try {
 						log = queue.poll(1, TimeUnit.SECONDS);
 						if (log == null)
 							continue;
 
 						buffer.add(log);
 						queue.drainTo(buffer, 10000);
 
 						for (Log l : buffer) {
 							currentLog = log;
 							first.push(new LogMap(l.getParams()));
 						}
 
 					} catch (Throwable t) {
 						if (log != null)
 							slog.error("araqne logdb: cannot evaluate query, log [" + log.getParams() + "], logger " + getFullName(), t);
 					} finally {
 						buffer.clear();
 					}
 				}
 			} catch (Throwable t) {
 				slog.error("araqne logdb: query runner failed, logger " + getFullName(), t);
 			} finally {
 				stopRunner = false;
 				runner = null;
 			}
 		}
 	}
 }
