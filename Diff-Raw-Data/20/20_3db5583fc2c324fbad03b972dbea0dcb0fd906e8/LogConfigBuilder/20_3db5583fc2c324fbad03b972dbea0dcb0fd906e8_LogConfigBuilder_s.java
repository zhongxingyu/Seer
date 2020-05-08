 package com.google.jstestdriver.util;
 
 import com.google.common.collect.Lists;
 
 import java.util.List;
 
 public class LogConfigBuilder {
   List<String> handlers = Lists.newLinkedList();
   List<String> configLines = Lists.newLinkedList();
 
   public LogConfigBuilder useFileHandler() {
     handlers.add("java.util.logging.FileHandler");
     configLines.add("java.util.logging.FileHandler.pattern=%t/jstd-" +
                     System.currentTimeMillis() +
                     ".log");
     configLines.add("java.util.logging.FileHandler.formatter=java.util.logging.SimpleFormatter");
     configLines.add("java.util.logging.FileHandler.level=ALL");
     return this;
   }
   
   public LogConfigBuilder useConsoleHandler() {
     handlers.add("java.util.logging.ConsoleHandler");
     configLines.add("java.util.logging.ConsoleHandler.level=ALL");
     return this;
   }
 
   public LogConfigBuilder finest(String className) {
     configLines.add(className + ".level=FINEST");
     return this;
   }
 
   public LogConfigBuilder severe(String className) {
     configLines.add(className + ".level=SEVERE");
     return this;
   }
   
   public LogConfigBuilder warn(String className) {
     configLines.add(className + ".level=WARNING");
     return this;
   }
 
   public LogConfigBuilder info(String className) {
     configLines.add(className + ".level=INFO");
     return this;
   }
   
   public LogConfigBuilder fine(String className) {
     configLines.add(className + ".level=FINE");
     return this;
   }
 
   public String build() {
     StringBuilder builder = new StringBuilder("handlers=");
     String sep = "";
     for (String handler : handlers) {
       builder.append(sep).append(handler);
       sep = ",";
     }
     builder.append('\n');
     for (String line : configLines) {
       builder.append(line).append('\n');
     }
     return builder.toString();
   }
 }
