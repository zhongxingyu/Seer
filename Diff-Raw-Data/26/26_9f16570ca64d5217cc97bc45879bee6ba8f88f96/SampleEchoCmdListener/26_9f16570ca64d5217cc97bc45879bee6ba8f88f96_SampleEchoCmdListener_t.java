/**
 * Razvan's code. 
 * Copyright 2008 based on Apache (share alike) see LICENSE.txt for details.
 */
 package com.razie.pub.http.test;
 
 import java.util.Properties;
 
 import com.razie.pub.base.log.Log;
 import com.razie.pub.http.AuthException;
 import com.razie.pub.http.MyServerSocket;
 import com.razie.pub.http.SocketCmdListener;
 
 /**
  * sample command listener implementing the echo command, as a simple url mapping
  *  $
  * @author razvanc99
  * 
  */
 public class SampleEchoCmdListener extends SocketCmdListener.Impl {
     public String input = null;
 
     @Override
     public Object executeCmdServer(String cmdName, String protocol, String args, Properties parms,
             MyServerSocket socket) throws AuthException {
         input = cmdName + ": " + args;
         String m = "execute cmdName=" + cmdName + ", protocol=" + protocol + ", args=" + args;
         Log.logThis(m);
         return args;
     }
 
     @Override
     public String[] getSupportedCommands() {
         return COMMANDS;
     }
 
     String[] COMMANDS = { "echo" };
 }
