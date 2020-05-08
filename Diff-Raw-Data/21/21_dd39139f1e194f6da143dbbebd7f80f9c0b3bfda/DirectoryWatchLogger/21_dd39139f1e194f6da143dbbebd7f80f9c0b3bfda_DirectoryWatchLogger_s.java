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
 package org.araqne.log.api;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.concurrent.atomic.AtomicLong;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.araqne.log.api.impl.FileUtils;
 import org.araqne.log.api.impl.LastPositionHelper;
 
 public class DirectoryWatchLogger extends AbstractLogger implements LogPipe {
 	private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DirectoryWatchLogger.class.getName());
 	protected File dataDir;
 	protected String basePath;
 	protected Pattern fileNamePattern;
 
 	private MultilineLogExtractor extractor;
 
 	public DirectoryWatchLogger(LoggerSpecification spec, LoggerFactory factory) {
 		super(spec, factory);
 
 		dataDir = new File(System.getProperty("araqne.data.dir"), "araqne-log-api");
 		dataDir.mkdirs();
 		basePath = getConfig().get("base_path");
 
 		String fileNameRegex = getConfig().get("filename_pattern");
 		fileNamePattern = Pattern.compile(fileNameRegex);
 
 		extractor = new MultilineLogExtractor(this, this);
 
 		// optional
 		String dateExtractRegex = getConfig().get("date_pattern");
 		if (dateExtractRegex != null)
 			extractor.setDateMatcher(Pattern.compile(dateExtractRegex).matcher(""));
 
 		// optional
 		String dateLocale = getConfig().get("date_locale");
 		if (dateLocale == null)
 			dateLocale = "en";
 
 		// optional
 		String dateFormatString = getConfig().get("date_format");
 		if (dateFormatString != null)
 			extractor.setDateFormat(new SimpleDateFormat(dateFormatString, new Locale(dateLocale)));
 
 		// optional
 		String newlogRegex = getConfig().get("newlog_designator");
 		if (newlogRegex != null)
 			extractor.setBeginMatcher(Pattern.compile(newlogRegex).matcher(""));
 
 		String newlogEndRegex = getConfig().get("newlog_end_designator");
 		if (newlogEndRegex != null)
 			extractor.setEndMatcher(Pattern.compile(newlogEndRegex).matcher(""));
 
 		// optional
 		String charset = getConfig().get("charset");
 		if (charset == null)
 			charset = "utf-8";
 
 		extractor.setCharset(charset);
 	}
 
 	@Override
 	public void onLog(Logger logger, Log log) {
 		write(log);
 	}
 
 	@Override
 	protected void runOnce() {
 		List<String> logFiles = FileUtils.matchFiles(basePath, fileNamePattern);
 		Map<String, String> lastPositions = LastPositionHelper.readLastPositions(getLastLogFile());
 
 		for (String path : logFiles) {
 			processFile(lastPositions, path);
 		}
 
 		LastPositionHelper.updateLastPositionFile(getLastLogFile(), lastPositions);
 	}
 
 	protected void processFile(Map<String, String> lastPositions, String path) {
 		FileInputStream is = null;
 
 		try {
 			// get date pattern-matched string from filename
 			String dateFromFileName = null;
 			Matcher fileNameDateMatcher = fileNamePattern.matcher(path);
 			if (fileNameDateMatcher.find()) {
 				int fileNameGroupCount = fileNameDateMatcher.groupCount();
 				if (fileNameGroupCount > 0) {
 					StringBuffer sb = new StringBuffer();
 					for (int i = 1; i <= fileNameGroupCount; ++i) {
 						sb.append(fileNameDateMatcher.group(i));
 					}
 					dateFromFileName = sb.toString();
 				}
 			}
 
 			// skip previous read part
 			long offset = 0;
 			if (lastPositions.containsKey(path)) {
 				offset = Long.valueOf(lastPositions.get(path));
 				logger.trace("logpresso igloo: target file [{}] skip offset [{}]", path, offset);
 			}
 
 			AtomicLong lastPosition = new AtomicLong(offset);
 			is = new FileInputStream(new File(path));
 			is.skip(offset);
 
 			extractor.extract(is, lastPosition, dateFromFileName);
 
 			logger.debug("araqne log api: updating file [{}] old position [{}] new last position [{}]", new Object[] { path,
 					offset, lastPosition.get() });
 			lastPositions.put(path, Long.toString(lastPosition.get()));
 		} catch (Throwable e) {
 			logger.error("araqne log api: [" + getName() + "] logger read error", e);
 		} finally {
 			if (is != null) {
 				try {
 					is.close();
 				} catch (IOException e) {
 				}
 			}
 		}
 	}
 
 	protected File getLastLogFile() {
 		return new File(dataDir, "dirwatch-" + getName() + ".lastlog");
 	}
 
 }
