// ==========================================================================
// $Id: Svc.java,v 1.63 2005/04/01 16:22:12 davidx Exp $
// (@) Copyright Sigma Systems (Canada) Inc.
// ==========================================================================
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
