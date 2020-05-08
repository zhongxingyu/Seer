 package com.github.alexesprit.chatlogs.loader;
 
 import java.util.regex.Pattern;
 
 public final class FreizeOrgLoader extends SimpleLogLoader {
     FreizeOrgLoader(String conference) {
         super(conference);
     }
 
     @Override
     protected Pattern getMessagePattern() {
        return Pattern.compile(">\\[(.+?)\\].+?(?:\"me\"> \\*(.+?)</span>|&lt;(.+?)&gt;</span> (.+?))<br\\s{0,1}/>\n");
     }
 
     @Override
     protected Pattern getTopicPattern() {
         return null;
     }
 
     @Override
     protected String getLogsRoot() {
         return "http://www.freize.org/log/logs";
     }
 
     @Override
     protected boolean isMeMessage(String rawMessage) {
         return rawMessage.contains("<span class=\"me\">");
     }
 }
