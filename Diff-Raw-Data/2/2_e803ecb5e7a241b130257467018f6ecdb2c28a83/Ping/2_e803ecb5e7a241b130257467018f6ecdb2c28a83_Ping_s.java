 package org.freenetproject.plugin.infocalypse_webui.ui.fcp;
 
 import freenet.support.SimpleFieldSet;
 
 /**
 * Responds to a Ping.
  */
 public class Ping implements  MessageHandler {
     @Override
     public SimpleFieldSet reply(SimpleFieldSet params) {
         SimpleFieldSet sfs = new SimpleFieldSet(true);
         sfs.putOverwrite("Message", "Pong");
         return sfs;
     }
 }
