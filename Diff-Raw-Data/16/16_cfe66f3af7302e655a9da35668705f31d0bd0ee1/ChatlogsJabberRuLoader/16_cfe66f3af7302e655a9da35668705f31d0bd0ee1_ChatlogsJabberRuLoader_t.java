 package com.github.alexesprit.chatlogs.loader;
 
 import java.util.regex.Pattern;
 
 public final class ChatlogsJabberRuLoader extends SimpleLogLoader {
     ChatlogsJabberRuLoader(String conference) {
         super(conference);
     }
 
     @Override
     protected Pattern getMessagePattern() {
        return Pattern.compile("\"ts\">\\[(.+?)\\]</a> <font class=(?:\"mne\">(.+?)</font>|\"mn\">&lt;(.+?)&gt;</font> (.+?))<br/>\n");
     }
 
     @Override
     protected Pattern getTopicPattern() {
         return Pattern.compile("<div class=\"roomsubject\">(.+?)</div>");
     }
 
     @Override
     protected String getLogsRoot() {
         return "http://chatlogs.jabber.ru";
     }
 
     @Override
     protected boolean isMeMessage(String rawMessage) {
         return rawMessage.contains("<font class=\"mne\"");
     }
 }
