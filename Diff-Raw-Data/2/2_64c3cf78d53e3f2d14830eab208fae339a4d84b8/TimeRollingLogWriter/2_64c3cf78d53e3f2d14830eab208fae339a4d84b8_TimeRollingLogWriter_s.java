 package org.araqne.log.api;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Map;
 
 public class TimeRollingLogWriter extends AbstractLogger implements LoggerRegistryEventListener, LogPipe {
 	private final org.slf4j.Logger slog = org.slf4j.LoggerFactory.getLogger(TimeRollingLogWriter.class);
 	private final String filePath;
 	private final String rotateInterval;
 	private final String charsetName;
 	private final boolean addCR;
 
 	/**
 	 * file name postfix
 	 */
 	private SimpleDateFormat dateFormat;
 
 	private LoggerRegistry loggerRegistry;
 
 	/**
 	 * full name of data source logger
 	 */
 	private String loggerName;
 
 	private BufferedOutputStream bos;
 	private FileOutputStream fos;
 	private boolean noRollingMode;
 
 	private String currentFilePath;
 
 	public TimeRollingLogWriter(LoggerSpecification spec, LoggerFactory factory, LoggerRegistry loggerRegistry) {
 		super(spec, factory);
 		this.loggerRegistry = loggerRegistry;
 		Map<String, String> config = spec.getConfig();
 		this.loggerName = config.get("source_logger");
 		this.filePath = config.get("file_path");
 		this.rotateInterval = config.get("rotate_interval");
 		if (rotateInterval.equals("day")) {
 			dateFormat = new SimpleDateFormat("yyyy-MM-dd");
 		} else if (rotateInterval.equals("hour")) {
 			dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH");
 		}
 
 		String s = config.get("charset");
 		this.charsetName = s != null ? s : "utf-8";
 		String osName = System.getProperty("os.name");
		this.addCR = osName != null && osName.toLowerCase().contains("indows");
 
 	}
 
 	public void flush() {
 		if (bos != null) {
 			try {
 				bos.flush();
 				fos.getFD().sync();
 			} catch (IOException e) {
 			}
 		}
 	}
 
 	@Override
 	protected void onStart() {
 		String targetPath = filePath;
 		if (dateFormat != null)
 			targetPath += "." + dateFormat.format(new Date());
 
 		ensureOpen(new File(targetPath));
 
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
 		ensureClose();
 
 		try {
 			if (loggerRegistry != null) {
 				Logger logger = loggerRegistry.getLogger(loggerName);
 				if (logger != null) {
 					slog.debug("araqne log api: disconnect pipe from source logger [{}]", loggerName);
 					logger.removeLogPipe(this);
 				}
 
 				loggerRegistry.removeListener(this);
 			}
 		} catch (Throwable t) {
 			slog.debug("araqne log api: cannot remove logger [" + getFullName() + "] from registry", t);
 		}
 	}
 
 	private void ensureOpen(File f) {
 		if (f.getParentFile().mkdirs())
 			slog.info("araqne log api: created parent directory [{}] by time rolling log file logger",
 					f.getParentFile().getAbsolutePath());
 
 		try {
 			fos = new FileOutputStream(f, true);
 			bos = new BufferedOutputStream(fos);
 			currentFilePath = f.getAbsolutePath();
 		} catch (IOException e) {
 			throw new IllegalStateException("cannot open time rolling logger [" + getFullName() + "]", e);
 		}
 	}
 
 	private void ensureClose() {
 		slog.debug("araqne log api: closing output file of time rolling logger [{}]", getFullName());
 
 		try {
 			if (bos != null) {
 				bos.close();
 				bos = null;
 			}
 		} catch (Throwable t) {
 		}
 
 		try {
 			if (fos != null) {
 				fos.close();
 				fos = null;
 			}
 		} catch (Throwable t) {
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
 		Map<String, Object> params = log.getParams();
 		write(new SimpleLog(log.getDate(), getFullName(), params));
 
 		String targetPath = filePath;
 		if (dateFormat != null) {
 			targetPath = filePath + "." + dateFormat.format(log.getDate());
 		}
 
 		try {
 			String line = buildLine(log);
 			byte[] b = line.getBytes(charsetName);
 			if (!currentFilePath.equals(targetPath)) {
 				if (!noRollingMode && !rollFile(targetPath)) {
 					noRollingMode = true;
 					slog.error("araqne log api: other process hold log file more than 10min, turn logger [{}] to non-rolling mode",
 							logger.getFullName());
 				}
 			}
 
 			bos.write(b);
 			if (addCR)
 				bos.write('\r');
 
 			bos.write('\n');
 		} catch (Throwable t) {
 			slog.debug("araqne log api: cannot write rolling log file, logger [" + logger.getFullName() + "]", t);
 		}
 	}
 
 	private String buildLine(Log log) {
 		Map<String, Object> params = log.getParams();
 		String line = (String) params.get("line");
 		if (line != null)
 			return line;
 
 		int i = 0;
 		StringBuilder sb = new StringBuilder(8096);
 		for (String key : params.keySet()) {
 			if (i++ != 0)
 				sb.append(", ");
 
 			sb.append(key);
 			sb.append("=");
 			sb.append("\"");
 			Object value = params.get(key);
 			sb.append(value == null ? "" : escape(value.toString()));
 			sb.append("\"");
 		}
 
 		return sb.toString();
 	}
 
 	private static String escape(String s) {
 		return s.replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\\\"");
 	}
 
 	private boolean rollFile(String newPath) {
 		// close file stream, rename and reopen
 		ensureClose();
 
 		ensureOpen(new File(newPath));
 
 		return true;
 	}
 }
