 package com.github.alexesprit.chatlogs.loader;
 
 import java.util.regex.Pattern;
 
 public final class LogsJimmNetRuLoader extends SimpleLogLoader {
     LogsJimmNetRuLoader(String conference) {
         super(conference);
     }
 
     @Override
     protected Pattern getMessagePattern() {
        return Pattern.compile("#t.+?\\'>(.+?)</a>]</font> <font class=(?:\\'me\\'>\\*(.+?)</font>|\\'nick\\'>&lt;(.+?)&gt; </font>(.+?))</br>\n");
     }
 
     @Override
     protected Pattern getTopicPattern() {
         return Pattern.compile("<div class=\"subject\">(.+?)</div>");
     }
 
     @Override
     protected String getLogsRoot() {
         return "http://logs.jimm.net.ru/historic";
     }
 
     @Override
     protected boolean isMeMessage(String rawMessage) {
         return rawMessage.contains("<font class='me'>");
     }
 }
