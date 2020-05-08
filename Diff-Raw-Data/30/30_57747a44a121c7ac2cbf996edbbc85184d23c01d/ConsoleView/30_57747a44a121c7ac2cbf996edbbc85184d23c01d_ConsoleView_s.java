 package com.yoursway.autoupdater.installer.gui;
 
 public class ConsoleView implements InstallerView {
     
     public void debug(String msg) {
         System.out.println(msg);
     }
     
     public void error(String msg) {
         System.err.println(msg);
         
     }
     
     public void error(Throwable e) {
         e.printStackTrace(System.err);
     }
     
     public void cycle() {
         // nothing
     }
     
 }
