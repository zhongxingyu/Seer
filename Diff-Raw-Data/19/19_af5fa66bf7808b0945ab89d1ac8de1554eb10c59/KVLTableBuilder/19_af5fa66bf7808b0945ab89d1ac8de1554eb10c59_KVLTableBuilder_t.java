 /*
  	org.manalith.ircbot.plugin.kvl/KVLTableBuilder.java
  	ManalithBot - An open source IRC bot based on the PircBot Framework.
  	Copyright (C) 2011, 2012  Seong-ho, Cho <darkcircle.0426@gmail.com>
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.manalith.ircbot.plugin.kvl;
 
 import java.io.IOException;
 import java.util.Iterator;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 public class KVLTableBuilder {
 
 	private String url;
 
 	public KVLTableBuilder() {
		this.setURL("");
 	}
 
 	public KVLTableBuilder(String newURL) {
		this.setURL(newURL);
 	}
 
 	private void setURL(String url) {
 		this.url = url;
 	}
 
 	private String getURL() {
 		return url;
 	}
 
 	public KVLTable generateKernelVersionTable() throws IOException {
 		KVLTable result = new KVLTable();
 
 		String newTag = null;
 		String newVerElement = null;
 
 		Iterator<Element> e = Jsoup.connect(getURL()).get()
				.select("table#releases>tbody>tr").iterator();
 
 		while (e.hasNext()) {
 			Elements tds = e.next().select("td");
 			newTag = tds.get(0).text().replaceAll("\\:", "");
 			newVerElement = tds.get(1).text();
 
 			result.addVersionInfo(newTag, newVerElement);
 		}
 
 		return result;
 	}
 
 }
