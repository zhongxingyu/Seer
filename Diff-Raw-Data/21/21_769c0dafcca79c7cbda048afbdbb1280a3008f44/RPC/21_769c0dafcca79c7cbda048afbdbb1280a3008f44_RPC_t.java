 /*
  *  The OpenDiamond Platform for Interactive Search
  *  Version 5
  *
  *  Copyright (c) 2009-2010 Carnegie Mellon University
  *  All rights reserved.
  *
  *  This software is distributed under the terms of the Eclipse Public
  *  License, Version 1.0 which can be found in the file named LICENSE.
  *  ANY USE, REPRODUCTION OR DISTRIBUTION OF THIS SOFTWARE CONSTITUTES
  *  RECIPIENT'S ACCEPTANCE OF THIS AGREEMENT
  */
 
 package edu.cmu.cs.diamond.opendiamond;
 
 import java.io.IOException;
 import java.util.concurrent.Callable;
 
 class RPC implements Callable<MiniRPCReply> {
     final public static int DIAMOND_SUCCESS = 0;
 
     final public static int DIAMOND_FAILURE = 500;
 
     final public static int DIAMOND_FCACHEMISS = 501;
 
     final public static int DIAMOND_COOKIE_EXPIRED = 504;
 
     final public static int MAX_FILTER_NAME = 128;
 
     final public static int MAX_FILTERS = 64;
 
     final private Connection connection;
 
     final private int cmd;
 
     final private byte data[];
 
     final private String hostname;
 
     public RPC(Connection connection, String hostname, int cmd, byte[] data) {
         this.connection = connection;
         this.hostname = hostname;
         this.cmd = cmd;
         this.data = data;
     }
 
     public MiniRPCReply call() throws Exception {
         return doRPC();
     }
 
     public MiniRPCReply doRPC() throws IOException {
         connection.sendControlRequest(cmd, data);
         MiniRPCReply reply = new MiniRPCReply(connection.receiveControl(),
                 hostname);
 
         // System.out.println(reply);
 
         return reply;
     }
 
     public static String statusToString(int status) {
         switch (status) {
         case DIAMOND_SUCCESS:
             return "success";
 
         case DIAMOND_FAILURE:
             return "failure";
 
         case DIAMOND_FCACHEMISS:
             return "cache miss";
 
         case DIAMOND_COOKIE_EXPIRED:
             return "cookie expired";
 
         default:
             return MiniRPCMessage.statusToString(status);
         }
     }
 }
