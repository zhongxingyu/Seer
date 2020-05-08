 package com.danielwellman.jschallenge.java1;
 
 public class FileAppenderDecoderListener implements DecoderListener {
    private final OutputWriter file;
 
     public FileAppenderDecoderListener(OutputWriter file) {
         this.file = file;
     }
 
     public void messageDecoded(String message) {
         file.println(message);
     }
 }
