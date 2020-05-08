 package org.araqne.log.api;
 
 import java.util.Map;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.TimeUnit;
 
 public class ParseLogger extends AbstractLogger implements LoggerRegistryEventListener, LogPipe {
 	private final org.slf4j.Logger slog = org.slf4j.LoggerFactory.getLogger(ParseLogger.class.getName());
 	private LoggerRegistry loggerRegistry;
 	private LogParserRegistry parserRegistry;
 	/**
 	 * full name of data source logger
 	 */
 	private String loggerName;
 	private LogParser parser;
 
 	private volatile boolean stopRunner = false;
 	private ParseRunner runner;
 	private ArrayBlockingQueue<Log> queue = new ArrayBlockingQueue<Log>(100000);
 
 	public ParseLogger(LoggerSpecification spec, LoggerFactory factory, LoggerRegistry loggerRegistry, LogParserRegistry parserRegistry) {
 		super(spec, factory);
 		this.loggerRegistry = loggerRegistry;
 		this.parserRegistry = parserRegistry;
 		Map<String, String> config = spec.getConfig();
 		loggerName = config.get("source_logger");
 	}
 
 	@Override
 	protected void onStart() {
 		if (runner == null) {
 			runner = new ParseRunner();
 			runner.start();
 		}
 
 		parser = parserRegistry.newParser(getConfig().get("parser_name"));
 		loggerRegistry.addListener(this);
 		Logger logger = loggerRegistry.getLogger(loggerName);
 
 		if (logger != null) {
 			slog.debug("araqne log api: connect pipe to source logger [{}]", loggerName);
 			logger.addLogPipe(this);
 		} else
 			slog.debug("araqne log api: source logger [{}] not found", loggerName);
 	}
 
 	@Override
 	protected void onStop() {
 		try {
 			stopRunner = true;
 			runner.interrupt();
 			try {
 				runner.join(5000);
 			} catch (InterruptedException e) {
 				slog.info("araqne log api: failed to join parse runner, logger [{}]", getFullName());
 			}
 
 			if (loggerRegistry != null) {
 				Logger logger = loggerRegistry.getLogger(loggerName);
 				if (logger != null) {
 					slog.debug("araqne log api: disconnect pipe from source logger [{}]", loggerName);
 					logger.removeLogPipe(this);
 				}
 
 				loggerRegistry.removeListener(this);
 			}
 		} catch (RuntimeException e) {
			if (!e.getMessage().endsWith("unavailable"))
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
 			slog.debug("araqne log api: source logger [{}] loaded", loggerName);
 			logger.addLogPipe(this);
 		}
 	}
 
 	@Override
 	public void loggerRemoved(Logger logger) {
 		if (logger.getFullName().equals(loggerName)) {
 			slog.debug("araqne log api: source logger [{}] unloaded", loggerName);
 			logger.removeLogPipe(this);
 		}
 	}
 
 	@Override
 	public void onLog(Logger logger, Log log) {
 		try {
 			if (isRunning())
 				queue.put(log);
 		} catch (Throwable t) {
 			slog.error("araqne log api: cannot parse log [" + log.getParams() + "]", t);
 		}
 	}
 
 	private class ParseRunner extends Thread {
 
 		@Override
 		public void run() {
 			try {
 				slog.info("araqne log api: begin parser runner, logger [{}]", getFullName());
 				while (!stopRunner) {
 					Log log = null;
 					try {
 						log = queue.poll(1, TimeUnit.SECONDS);
 						if (log == null)
 							continue;
 
 						Map<String, Object> row = parser.parse(log.getParams());
 						if (row != null)
 							write(new SimpleLog(log.getDate(), getFullName(), row));
 					} catch (Throwable t) {
 						if (log != null)
 							slog.error("araqne log api: cannot parse log [" + log.getParams() + "]", t);
 					}
 				}
 			} catch (Throwable t) {
 				slog.error("araqne log api: parser runner failed", t);
 			} finally {
 				stopRunner = false;
 				runner = null;
 			}
 		}
 	}
 }
