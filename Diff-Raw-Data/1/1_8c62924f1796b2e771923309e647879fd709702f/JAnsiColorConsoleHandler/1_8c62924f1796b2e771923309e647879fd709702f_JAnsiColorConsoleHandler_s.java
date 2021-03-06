 package com.colorlog.jdk;
 
 import java.util.logging.LogRecord;
 // Color Console Handler for jdk: using jansi (http://jansi.fusesource.org/)
 import org.fusesource.jansi.AnsiConsole;
 
 public class JAnsiColorConsoleHandler extends BaseColorConsoleHandler {
     @Override
     public void publish(LogRecord record) {
         AnsiConsole.err.print(logRecordToString(record));
     }
 }
