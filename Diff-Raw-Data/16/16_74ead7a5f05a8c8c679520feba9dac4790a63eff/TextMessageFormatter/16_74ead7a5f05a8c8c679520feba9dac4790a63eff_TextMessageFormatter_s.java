 package org.uli.tcpmon;
 
 import java.util.LinkedList;
 import java.util.List;
 
 public class TextMessageFormatter implements MessageFormatter {
     @Override
     public List<String> format(byte[] message) {
        String rawText = new String(message);
         String[] splittedText = rawText.split("\\n");
         List<String> formatted = new LinkedList<String>();
         for (String l : splittedText) {
             l = l.replaceAll("\\r",  "[CR]");
             l += "[LF]";
             formatted.add(l);
         }
         return formatted;
     }
 }
