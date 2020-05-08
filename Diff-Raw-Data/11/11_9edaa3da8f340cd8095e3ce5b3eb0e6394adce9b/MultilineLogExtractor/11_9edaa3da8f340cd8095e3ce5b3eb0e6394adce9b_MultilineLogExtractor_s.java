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
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.text.ParsePosition;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.atomic.AtomicLong;
 import java.util.regex.Matcher;
 
 /**
  * @since 2.4.6
  * @author xeraph
  * 
  */
 public class MultilineLogExtractor {
 	private Logger logger;
 	private String charset = "utf-8";
 	private Matcher beginMatcher;
 	private Matcher endMatcher;
 	private Matcher dateMatcher;
 	private SimpleDateFormat dateFormat;
 	private LogPipe pipe;
 
 	// assign current year to date
 	private Calendar yearModifier;
 
 	public MultilineLogExtractor(Logger logger, LogPipe pipe) {
 		this.logger = logger;
 		this.pipe = pipe;
 	}
 
 	public String getCharset() {
 		return charset;
 	}
 
 	public void setCharset(String charset) {
 		this.charset = charset;
 	}
 
 	public Matcher getBeginMatcher() {
 		return beginMatcher;
 	}
 
 	public void setBeginMatcher(Matcher beginMatcher) {
 		this.beginMatcher = beginMatcher;
 	}
 
 	public Matcher getEndMatcher() {
 		return endMatcher;
 	}
 
 	public void setEndMatcher(Matcher endMatcher) {
 		this.endMatcher = endMatcher;
 	}
 
 	public Matcher getDateMatcher() {
 		return dateMatcher;
 	}
 
 	public void setDateMatcher(Matcher dateMatcher) {
 		this.dateMatcher = dateMatcher;
 	}
 
 	public SimpleDateFormat getDateFormat() {
 		return dateFormat;
 	}
 
 	public void setDateFormat(SimpleDateFormat dateFormat) {
 		this.dateFormat = dateFormat;
 		if (dateFormat != null && !dateFormat.toPattern().contains("yyyy"))
 			yearModifier = Calendar.getInstance();
 	}
 
 	public void extract(InputStream is, AtomicLong lastPosition) throws IOException {
 		extract(is, lastPosition, null);
 	}
 
 	public void extract(InputStream is, AtomicLong lastPosition, String dateFromFileName) throws IOException {
 		ByteArrayOutputStream logBuf = new ByteArrayOutputStream();
 
 		// last chunk of page which does not contains new line
 		ByteArrayOutputStream temp = new ByteArrayOutputStream();
 		byte[] b = new byte[128 * 1024];
 
 		while (true) {
 			LoggerStatus status = logger.getStatus();
 			if (status == LoggerStatus.Stopping || status == LoggerStatus.Stopped)
 				break;
 
 			int next = 0;
 			int len = is.read(b);
 			if (len < 0)
 				break;
 
 			for (int i = 0; i < len; i++) {
 				if (b[i] == 0xa) {
 					buildAndWriteLog(logBuf, b, next, i - next + 1, lastPosition, temp, dateFromFileName);
 					next = i + 1;
 				}
 			}
 
 			// temp should be matched later (line regex test)
 			temp.write(b, next, len - next);
 		}
 	}
 
 	private void buildAndWriteLog(ByteArrayOutputStream logBuf, byte[] b, int offset, int length, AtomicLong lastPosition,
 			ByteArrayOutputStream temp, String dateFromFileName) {
 		String log = null;
 		try {
 			log = buildLog(logBuf, b, offset, length, lastPosition, temp);
 		} catch (UnsupportedEncodingException e) {
 		}
 
 		if (log != null) {
 			log = log.trim();
 			if (log.length() > 0)
 				writeLog(log, dateFromFileName);
 		}
 	}
 
 	/**
 	 * @param buf
 	 *            the buffer which hold partial multiline log
 	 * @param b
 	 *            read block which contains new line
 	 * @param offset
 	 *            the new line offset
 	 * @param len
 	 *            the new line length
 	 * @param lastPosition
 	 *            the last position which read and written as log
 	 * @return new (multiline) log
 	 * @throws UnsupportedEncodingException
 	 */
 	private String buildLog(ByteArrayOutputStream buf, byte[] b, int offset, int len, AtomicLong lastPosition, ByteArrayOutputStream temp)
 			throws UnsupportedEncodingException {
 
 		String line = null;
 		if (temp.size() > 0) {
 			temp.write(b, offset, len);
 			line = new String(temp.toByteArray(), charset);
 		} else {
 			line = new String(b, offset, len, charset);
 		}
 
 		if (!line.endsWith("\n")) {
 			if (temp.size() == 0)
 				temp.write(b, offset, len);
 
 			return null;
 		}
 
 		if (beginMatcher != null)
 			beginMatcher.reset(line);
 
 		if (endMatcher != null)
 			endMatcher.reset(line);
 
 		if (beginMatcher == null && endMatcher == null) {
 			if (temp.size() > 0) {
 				byte[] t = temp.toByteArray();
 				buf.write(t, 0, t.length);
 				temp.reset();
 			} else {
 				buf.write(b, offset, len);
 			}
 
 			byte[] old = buf.toByteArray();
 			buf.reset();
 			lastPosition.addAndGet(old.length);
 			return new String(old, charset);
 		}
 
 		if (beginMatcher != null && beginMatcher.find()) {
 			byte[] old = buf.toByteArray();
 			String log = null;
 
 			if (old.length > 0) {
 				log = new String(old, charset);
 				lastPosition.addAndGet(old.length);
 				buf.reset();
 			}
 
 			if (temp.size() > 0) {
 				byte[] t = temp.toByteArray();
 				buf.write(t, 0, t.length);
 				temp.reset();
 			} else {
 				buf.write(b, offset, len);
 			}
 			return log;
 		} else if (endMatcher != null && endMatcher.find()) {
 			if (temp.size() > 0) {
 				byte[] t = temp.toByteArray();
 				buf.write(t, 0, t.length);
 				temp.reset();
 			} else {
 				buf.write(b, offset, len);
 			}
 			byte[] old = buf.toByteArray();
 			lastPosition.addAndGet(old.length);
 			String log = new String(old, charset);
 			buf.reset();
 			return log;
 		} else {
 			if (temp.size() > 0) {
 				byte[] t = temp.toByteArray();
 				buf.write(t, 0, t.length);
 				temp.reset();
 			} else {
 				buf.write(b, offset, len);
 			}
 		}
 
 		return null;
 	}
 
 	private void writeLog(String mline, String dateFromFileName) {
 		Date d = parseDate(mline, dateFromFileName);
 		Map<String, Object> log = new HashMap<String, Object>();
 		log.put("line", mline);
 		pipe.onLog(logger, new SimpleLog(d, logger.getFullName(), log));
 	}
 
 	protected Date parseDate(String line, String dateFromFileName) {
 		if (dateMatcher == null || dateFormat == null)
 			return new Date();
 
 		dateMatcher.reset(line);
 
 		if (!dateMatcher.find())
 			return new Date();
 
 		String s = null;
 		int count = dateMatcher.groupCount();
 		for (int i = 1; i <= count; i++) {
 			if (s == null)
 				s = dateMatcher.group(i);
 			else
 				s += dateMatcher.group(i);
 		}
 
 		if (dateFromFileName != null) {
 			s = dateFromFileName + s;
 		}
 
 		Date d = dateFormat.parse(s, new ParsePosition(0));
 		if (d == null)
 			return new Date();
 
 		if (yearModifier != null) {
 			int year = Calendar.getInstance().get(Calendar.YEAR);
 			yearModifier.setTime(d);
 			yearModifier.set(Calendar.YEAR, year);
 			d = yearModifier.getTime();
 		}
 
 		return d;
 	}
 
 }
