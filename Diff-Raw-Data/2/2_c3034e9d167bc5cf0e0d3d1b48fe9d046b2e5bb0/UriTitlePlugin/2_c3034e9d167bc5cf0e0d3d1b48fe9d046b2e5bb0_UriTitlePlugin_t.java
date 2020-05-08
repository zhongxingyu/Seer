 /*
  	org.manalith.ircbot.plugin.urititle/UriTitlePlugin.java
  	ManalithBot - An open source IRC bot based on the PircBot Framework.
  	Copyright (C) 2012  Changwoo Ryu <cwryu@debian.org>
 
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
 package org.manalith.ircbot.plugin.urititle;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.jsoup.Jsoup;
 import org.manalith.ircbot.plugin.AbstractBotPlugin;
 import org.manalith.ircbot.resources.MessageEvent;
 import org.springframework.stereotype.Component;
 
 @Component
 public class UriTitlePlugin extends AbstractBotPlugin {
 	public String getCommands() {
 		return null;
 	}
 
 	public String getName() {
 		return "URI 타이틀";
 	}
 
 	public String getHelp() {
 		return "대화 중 등장하는 URI의 타이틀을 표시합니다";
 	}
 
 	private String findUri(String msg) {
 		if (!msg.contains("http"))
 			return null;
 
 		String URI_REGEX = ".*(https?://\\S+).*";
 		Pattern pattern = Pattern.compile(URI_REGEX);
 		Matcher matcher = pattern.matcher(msg);
 
 		if (!matcher.matches())
 			return null;
 
 		return matcher.group(1);
 	}
 
 	private String getTitle(String uri) {
 		try {
			return Jsoup.connect(uri).get().title().replaceAll("\\n", "").replaceAll("\\r", "")
 					.replaceAll("(\\s){2,}", " ");
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 	public void onMessage(MessageEvent event) {
 		String message = event.getMessage();
 		String channel = event.getChannel();
 
 		String uri = findUri(message);
 		if (uri == null)
 			return;
 
 		String title = getTitle(uri);
 		if (title != null) {
 			bot.sendLoggedMessage(channel, "[Link Title] " + title);
 		}
 
 		// This plugin runs implicitly; NO need to call
 		// event.setExecuted(true)
 	}
 }
