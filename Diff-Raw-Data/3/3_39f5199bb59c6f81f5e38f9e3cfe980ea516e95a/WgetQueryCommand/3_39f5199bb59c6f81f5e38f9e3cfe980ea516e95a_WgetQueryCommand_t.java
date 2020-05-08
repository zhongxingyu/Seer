 /**
  * Copyright 2014 Eediom Inc.
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
 package org.araqne.logdb.crawler.query;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.araqne.logdb.DriverQueryCommand;
 import org.araqne.logdb.Row;
 import org.jsoup.Connection;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Attribute;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author xeraph@eediom.com
  */
 public class WgetQueryCommand extends DriverQueryCommand {
 	private final Logger slog = LoggerFactory.getLogger(WgetQueryCommand.class);
 
 	private String url;
 	private String selector;
 	private int timeout;
 	private String method;
 	private String encoding;
 
 	public WgetQueryCommand(String url, String selector, int timeout, String method, String encoding) {
 		this.url = url;
 		this.selector = selector;
 		this.timeout = timeout;
 		this.method = method;
 		this.encoding = encoding;
 	}
 
 	@Override
 	public String getName() {
 		return "wget";
 	}
 
 	@Override
 	public boolean isDriver() {
 		return url != null;
 	}
 
 	@Override
 	public void run() {
 		try {
 			Row row = new Row();
 			fetchUrl(row, url);
 			pushPipe(row);
 		} catch (Throwable t) {
 			slog.error("araqne logdb crawler: wget failed - " + url, t);
			throw new IllegalStateException("wget: " + t.getMessage());
 		}
 	}
 
 	@Override
 	public void onPush(Row row) {
 		Object o = row.get("url");
 		String url = null;
 		try {
 			if (o == null)
 				return;
 
 			url = o.toString();
 
 			fetchUrl(row, url);
 		} catch (Throwable t) {
 			if (slog.isDebugEnabled())
 				slog.debug("araqne logdb crawler: wget failed - " + url, t);
 		} finally {
 			pushPipe(row);
 		}
 	}
 
 	private void fetchUrl(Row row, String url) throws IOException {
 		Connection conn = Jsoup.connect(url);
		conn.ignoreContentType(true);
 		conn.timeout(timeout * 1000);
 		Document doc = null;
 
 		if (method.equals("get"))
 			doc = conn.get();
 		else if (method.equals("post"))
 			doc = conn.post();
 
 		if (doc != null) {
 			if (selector != null) {
 				Elements elements = doc.select(selector);
 				ArrayList<Object> l = new ArrayList<Object>(elements.size());
 
 				for (Element e : elements) {
 					Map<String, Object> m = new HashMap<String, Object>();
 
 					for (Attribute attr : e.attributes()) {
 						m.put(attr.getKey(), attr.getValue());
 					}
 
 					m.put("own_text", e.ownText());
 					m.put("text", e.text());
 					l.add(m);
 				}
 
 				row.put("elements", l);
 			} else {
 				row.put("html", doc.outerHtml());
 			}
 		}
 	}
 
 	@Override
 	public String toString() {
 		String s = "wget";
 		if (url != null)
 			s += " url=\"" + url + "\"";
 
 		if (selector != null)
 			s += " selector=\"" + selector + "\"";
 
 		if (timeout != 30000)
 			s += " timeout=" + timeout;
 
 		if (!method.equals("get"))
 			s += " method=" + method;
 
 		if (!encoding.equals("utf-8"))
 			s += " encoding=" + encoding;
 
 		return s;
 	}
 }
