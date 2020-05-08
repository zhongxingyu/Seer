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
 import java.text.ParsePosition;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.araqne.log.api.impl.FileUtils;
 import org.araqne.log.api.impl.LastPositionHelper;
 
 public class DirectoryWatchLogger extends AbstractLogger {
 	private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DirectoryWatchLogger.class.getName());
 	protected File dataDir;
 	protected String basePath;
 	protected Pattern fileNamePattern;
 	protected Pattern dateExtractPattern;
 	protected String charset;
 	protected SimpleDateFormat dateFormat;
 	private Matcher dateExtractMatcher;
 	private Matcher newlogDsgnMatcher;
 
 	public DirectoryWatchLogger(LoggerSpecification spec, LoggerFactory factory) {
 		super(spec, factory);
 
 		dataDir = new File(System.getProperty("araqne.data.dir"), "araqne-log-api");
 		dataDir.mkdirs();
 		basePath = getConfig().get("base_path");
 
 		String fileNameRegex = getConfig().get("filename_pattern");
 		fileNamePattern = Pattern.compile(fileNameRegex);
 
 		// optional
 		String dateExtractRegex = getConfig().get("date_pattern");
 		if (dateExtractRegex != null)
 			dateExtractPattern = Pattern.compile(dateExtractRegex);
 
 		// optional
 		String dateLocale = getConfig().get("date_locale");
 		if (dateLocale == null)
 			dateLocale = "en";
 
 		// optional
 		String dateFormatString = getConfig().get("date_format");
 		if (dateFormatString != null)
 			dateFormat = new SimpleDateFormat(dateFormatString, new Locale(dateLocale));
 
 		// optional
 		String newlogRegex = getConfig().get("newlog_designator");
 		if (newlogRegex != null) {
			if (!newlogRegex.startsWith("^"))
				newlogRegex = "^" + newlogRegex;
 			newlogDsgnMatcher = Pattern.compile(newlogRegex).matcher("");
 		}
 
 		// optional
 		charset = getConfig().get("charset");
 		if (charset == null)
 			charset = "utf-8";
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
 		TextFileReader reader = null;
 
 		try {
 			// get date pattern-matched string from filename
 			String fileDateStr = null;
 			Matcher fileNameDateMatcher = fileNamePattern.matcher(path);
 			if (fileNameDateMatcher.find()) {
 				int fileNameGroupCount = fileNameDateMatcher.groupCount();
 				if (fileNameGroupCount > 0) {
 					StringBuffer sb = new StringBuffer();
 					for (int i = 1; i <= fileNameGroupCount; ++i) {
 						sb.append(fileNameDateMatcher.group(i));
 					}
 					fileDateStr = sb.toString();
 				}
 			}
 
 			// skip previous read part
 			long offset = 0;
 			if (lastPositions.containsKey(path)) {
 				offset = Long.valueOf(lastPositions.get(path));
 				logger.trace("logpresso igloo: target file [{}] skip offset [{}]", path, offset);
 			}
 
 			reader = new TextFileReader(new File(path), offset, charset);
 
 			// read and normalize log
 			StringBuffer sb = new StringBuffer();
 			while (true) {
 				if (getStatus() == LoggerStatus.Stopping || getStatus() == LoggerStatus.Stopped)
 					break;
 
 				String line = reader.readLine();
 				if (line == null)
 					break;
 				if (newlogDsgnMatcher != null) {
 					// multi-line logger
 					newlogDsgnMatcher.reset(line);
 					if (newlogDsgnMatcher.find()) {
 						// new log detected.
 						if (sb.length() != 0)
 							writeLog(fileDateStr, sb.toString());
 						sb = new StringBuffer();
 						sb.append(line);
 					} else {
 						// append log to prev line
 						sb.append("\n");
 						sb.append(line);
 					}
 				} else {
 					// empty line is allowed for multiline log
 					if (line.trim().isEmpty())
 						break;
 
 					writeLog(fileDateStr, line);
 				}
 			}
 			if (newlogDsgnMatcher != null) {
 				if (sb.length() != 0)
 					writeLog(fileDateStr, sb.toString());
 			}
 
 			long position = reader.getPosition();
 			logger.debug("araqne log api: updating file [{}] old position [{}] new last position [{}]", new Object[] { path,
 					offset, position });
 			lastPositions.put(path, Long.toString(position));
 
 		} catch (Throwable e) {
 			logger.error("araqne log api: [" + getName() + "] logger read error", e);
 		} finally {
 			if (reader != null)
 				reader.close();
 		}
 	}
 
 	private void writeLog(String fileDateStr, String mline) {
 		Date d = parseDate(fileDateStr, mline);
 		Map<String, Object> log = new HashMap<String, Object>();
 		log.put("line", mline);
 
 		write(new SimpleLog(d, getFullName(), log));
 	}
 
 	protected File getLastLogFile() {
 		return new File(dataDir, "dirwatch-" + getName() + ".lastlog");
 	}
 
 	protected Date parseDate(String fileDateStr, String line) {
 		if (dateExtractPattern == null || dateFormat == null)
 			return new Date();
 
 		if (dateExtractMatcher == null)
 			dateExtractMatcher = dateExtractPattern.matcher(line);
 		else
 			dateExtractMatcher.reset(line);
 
 		if (!dateExtractMatcher.find())
 			return new Date();
 
 		String s = null;
 		int count = dateExtractMatcher.groupCount();
 		for (int i = 1; i <= count; i++) {
 			if (s == null)
 				s = dateExtractMatcher.group(i);
 			else
 				s += dateExtractMatcher.group(i);
 		}
 
 		if (fileDateStr != null) {
 			s = fileDateStr + s;
 		}
 
 		Date d = dateFormat.parse(s, new ParsePosition(0));
 		return d != null ? d : new Date();
 	}
 }
