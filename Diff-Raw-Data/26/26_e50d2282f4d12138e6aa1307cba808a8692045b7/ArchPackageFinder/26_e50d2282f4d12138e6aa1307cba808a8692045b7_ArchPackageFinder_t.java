 /*
  	org.manalith.ircbot.plugin.distopkgfinder/ArchPkgFinderRunner.java
  	ManalithBot - An open source IRC bot based on the PircBot Framework.
  	Copyright (C) 2012  Seong-ho, Cho <darkcircle.0426@gmail.com>
 
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
 
 package org.manalith.ircbot.plugin.linuxpkgfinder;
 
 import java.io.IOException;
 import java.util.Iterator;
 
 import org.apache.commons.lang3.StringUtils;
 import org.apache.commons.lang3.math.NumberUtils;
 import org.apache.log4j.Logger;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 import org.manalith.ircbot.common.stereotype.BotCommand;
 import org.manalith.ircbot.resources.MessageEvent;
 import org.springframework.stereotype.Component;
 
 @Component
 public class ArchPackageFinder extends PackageFinder {
 	private Logger logger = Logger.getLogger(getClass());
 
 	@Override
 	public String getName() {
 		return "아치";
 	}
 
 	@Override
 	public String getCommands() {
 		return "!ar";
 	}
 
 	public String getHelp() {
 		return "설  명: 지정한 이름을 가진 아치의 패키지를 검색합니다, 사용법: !ar [키워드]";
 	}
 
 	@BotCommand(value = { "!ar" }, minimumArguments = 1)
 	public String find(MessageEvent event, String... args) {
 		return this.find(args[0]);
 	}
 
 	public String find(String arg) {
 		String result = "";
 
 		String[] arch_keywords = { "any", "i686", "x86_64" };
 		int len = arch_keywords.length;
 		int pages = 100000000;
 		String description = "";
 
 		String url = "";
 		try {
 			String infostr = "";
 			for (int i = 0; i < len; i++) {
 				for (int j = 0; j < pages; j++) {
 					url = "http://www.archlinux.org/packages/"
 							+ (new Integer(j + 1)).toString() + "/?arch="
 							+ arch_keywords[i] + "&q=" + arg;
 
 					String pageinfo = "";
 					try {
 						pageinfo = Jsoup
 								.connect(url)
 								.get()
 								.select("div#pkglist-results>div.pkglist-stats>p")
 								.get(0).text();
 					} catch (Exception e) {
 						pages = 1;
 					}
 
 					if (j == 0 && pages == 100000000) {
 						if (StringUtils.countMatches(pageinfo, ".") == 1)
 							pages = 1;
 						else {
 							if (pageinfo.charAt(pageinfo.length() - 1) == '.')
 								pageinfo = pageinfo.substring(0,
 										pageinfo.length() - 1);
 							String[] piarray = pageinfo.split("\\s");
 							pages = NumberUtils
 									.toInt(piarray[piarray.length - 1]);
 						}
 
 						j--;
 						continue;
 					}
 
 					Iterator<Element> e = Jsoup.connect(url).get()
 							.select("table.results>tbody>tr").iterator();
 
 					while (e.hasNext()) {
 						Elements ee = e.next().select("td");
 						if (ee.get(2).select("a").get(0).text().equals(arg)) {
 							if (!infostr.equals(""))
 								infostr += ", ";
 							infostr += "[main-" + ee.get(0).text() + "] ";
 							infostr += ee.get(2).select("a").get(0).text()
 									+ "  ";
 
 							if (ee.get(3).select("span.flagged").size() > 0)
 								infostr += ee.get(3).select("span.flagged")
 										.get(0).text();
 							else
 								infostr += ee.get(3).text();
 
 							infostr += "(" + ee.get(1).text() + ")";
 							if (description.equals(""))
 								description = ee.get(4).text();
 
 							break;
 						}
 					}
 				}
 				pages = 100000000;
 			}
 
 			if (!infostr.equals(""))
 				infostr += " : " + description;
 
 			url = "http://aur.archlinux.org/packages.php?K=" + arg;
 			Iterator<Element> e = Jsoup.connect(url).get()
 					.select("table>tbody>tr").iterator();
 
 			boolean firstrow = false;
 
 			while (e.hasNext()) {
 				Elements ee = e.next().select("td");
				if (ee.get(1).select("td").get(0).text().equals(arg)) {
 					if (!infostr.equals(""))
 						infostr += " | ";
					infostr += "[AUR-" + ee.get(0).text() + "] ";
					infostr += ee.get(1).select("a").text() + "  "
							+ ee.get(2).text();
					infostr += " : " + ee.get(4).text();
 					break;
 				}
 			}
 
 			if (!infostr.equals(""))
 				result = infostr;
 			else
 				result = "결과가 없습니다";
 
 		} catch (IOException e) {
 			logger.error(e.getMessage(), e);
 			result = "오류: " + e.getMessage();
 		}
 
 		return result;
 	}
}
