 /*
  * Copyright 2013 Future Systems
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
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.Closeable;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.zip.ZipEntry;
 
 import org.araqne.log.api.LogParser;
 import org.araqne.logdb.LogMap;
 import org.araqne.logdb.LogQueryCommand;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class ZipFile extends LogQueryCommand {
 	private final Logger logger = LoggerFactory.getLogger(TextFile.class.getName());
 	private String filePath;
 	private String entryPath;
 	private LogParser parser;
 	private int offset;
 	private int limit;
 
 	public ZipFile(String filePath, String entryPath, LogParser parser, int offset, int limit) {
 		this.filePath = filePath;
 		this.entryPath = entryPath;
 		this.parser = parser;
 		this.offset = offset;
 		this.limit = limit;
 	}
 
 	@Override
 	public void start() {
 		status = Status.Running;
 
 		java.util.zip.ZipFile zipFile = null;
 		BufferedReader br = null;
 		InputStream is = null;
 		try {
 			zipFile = new java.util.zip.ZipFile(new File(filePath));
 			logger.debug("araqne logdb: zipfile path: {}, zip entry: {}", filePath, entryPath);
 
 			ZipEntry entry = zipFile.getEntry(entryPath);
 			if (entry == null)
 				throw new IllegalStateException("entry [" + entryPath + "] not found in zip file [" + filePath + "]");
 
			zipFile.getInputStream(entry);

 			br = new BufferedReader(new InputStreamReader(new BufferedInputStream(is), "utf-8"));
 
 			int i = 0;
 			int count = 0;
 			while (true) {
 				if (limit > 0 && count >= limit)
 					break;
 
 				String line = br.readLine();
 				if (line == null)
 					break;
 
 				Map<String, Object> m = new HashMap<String, Object>();
 				Map<String, Object> parsed = null;
 				m.put("line", line);
 				if (parser != null) {
 					parsed = parser.parse(m);
 					if (parsed == null)
 						continue;
 				}
 
 				if (i >= offset) {
 					write(new LogMap(parsed != null ? parsed : m));
 					count++;
 				}
 				i++;
 			}
 		} catch (Throwable t) {
 			logger.error("araqne logdb: zipfile error", t);
 		} finally {
 			ensureClose(is);
 			ensureClose(br);
			ensureClose(zipFile);
 		}
 
 		eof(false);
 	}
 
 	private void ensureClose(Closeable c) {
 		if (c != null) {
 			try {
 				c.close();
 			} catch (IOException e) {
 			}
 		}
 	}
 
 	@Override
 	public void push(LogMap m) {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public boolean isReducer() {
 		return false;
 	}
 
 	@Override
 	public String toString() {
 		return "zipfile offset=" + offset + " limit=" + limit + " " + filePath + " " + entryPath;
 	}
 }
