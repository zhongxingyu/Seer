 package com.trendmicro.mist.cmd;
 
 import java.io.IOException;
 import java.io.File;
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.util.Date;
 
 import com.trendmicro.mist.Daemon;
 import com.trendmicro.mist.MistException;
 import com.trendmicro.mist.ThreadInvoker;
 import com.trendmicro.mist.util.Exchange;
 import com.trendmicro.mist.util.Packet;
 
 import gnu.getopt.Getopt;
 import gnu.getopt.LongOpt;
 
 import com.trendmicro.mist.proto.GateTalk;
 import com.trendmicro.spn.common.util.Utils;
 
 public class MistSource extends ThreadInvoker {
 
     class Reaper implements Runnable {
         private long timeToLive = -1;
         private long timeBirth = -1;
         
         ////////////////////////////////////////////////////////////////////////////////
 
         public void setLive(long duration) {
             timeToLive = duration;
         }
 
         public void setBirth(long time) {
             timeBirth = time;
         }
 
         public void run() {
             while(true) {
                 long now = new Date().getTime();
                 if(timeBirth != -1 && now - timeBirth > timeToLive) 
                     System.exit(RetVal.TIMEOUT.ordinal());
                 Utils.justSleep(50);
             }
         }
     }
 
     enum CmdType {
         NONE, LIST, MOUNT, UNMOUNT, ATTACH, DETACH,
     }
     
     enum RetVal {
         OK,
         MOUNT_FAILED,
         UNMOUNT_FAILED,
         ATTACH_FAILED,
         DETACH_FAILED,
         TIMEOUT, 
     }
 
     private Exchange exchange = new Exchange();
     private int targetSessionId = -1;
 
     private CmdType currCmd = CmdType.NONE;
     private Thread threadMain;
     private static MistSource myApp;
     private boolean daemonDetach = false;
     private boolean usePerf = false;
     private final int PERF_COUNT = 1000;
     private int limitCount = -1;
     private int timeout = -1;
     private Reaper reaper = new Reaper();
 
     private void addShutdownHook() {
         Runtime.getRuntime().addShutdownHook(new Thread() {
             public void run() {
                 myApp.shutdown();
             }
         });
     }
     
     private GateTalk.Request makeDetachRequest() {
         GateTalk.Request.Builder req_builder = GateTalk.Request.newBuilder();
         req_builder.setType(GateTalk.Request.Type.CLIENT_DETACH);
         req_builder.setRole(GateTalk.Request.Role.SOURCE);
         req_builder.setArgument(String.valueOf(targetSessionId));
         return req_builder.build();
     }
     
     private void flushRequest(GateTalk.Request request) {
         GateTalk.Command.Builder cmd_builder = GateTalk.Command.newBuilder();
         cmd_builder.addRequest(request);
         try {
             MistSession.sendRequest(cmd_builder.build());
         }
         catch(MistException e) {
             myErr.println(e.getMessage());
         }
     }
 
     private void shutdown() {
         if(new File(Daemon.namePidfile).exists()) {
             if(currCmd == CmdType.ATTACH && (exitCode == RetVal.OK.ordinal() && !daemonDetach))
                 flushRequest(makeDetachRequest());
             try {
                 if(threadMain != null)
                     threadMain.join(1000);
             }
             catch(InterruptedException e) {
             }
         }
     }
 
     private void printUsage() {
         myOut.printf("Usage:%n");
         myOut.printf("      mist-source SESSION_ID [options [arguments...] ]... %n%n");
         myOut.printf("Options: %n");
         myOut.printf("  --mount=EXCHANGE, -m EXCHANGE %n");
         myOut.printf("  --unmount=EXCHANGE, -u EXCHANGE %n");
         myOut.printf("        mount/unmount exchange to/from SESSION_ID %n");
         myOut.printf("        where EXCHANGE={queue,topic}:EXCHANGENAME %n");
         myOut.printf("    --queue, -q %n");
         myOut.printf("        use queue (default) %n");
         myOut.printf("    --topic, -t %n");
         myOut.printf("        use topic %n%n");
         myOut.printf("  --attach, -a %n");
         myOut.printf("        attach to SESSION_ID and start transmission %n%n");
         myOut.printf("    --perf, -p %n");
         myOut.printf("        display performance number %n%n");
         myOut.printf("  --detach, -d %n");
         myOut.printf("        detach from SESSION_ID %n%n");
         //myOut.printf("  --limit-count=NUMBER %n");
         //myOut.printf("        limit the number of messages to receive %n%n");
         //myOut.printf("  --timeout=MILLISECOND %n");
         //myOut.printf("        limit the waiting time to receive messages %n%n");
         myOut.printf("  --help, -h %n");
         myOut.printf("        display help messages %n%n");
     }
 
     private void processAttach() {
         if(timeout != -1) {
             reaper.setLive((long) timeout);
             new Thread(reaper).start();
         }
 
         GateTalk.Request.Builder req_builder = GateTalk.Request.newBuilder();
         req_builder.setType(GateTalk.Request.Type.CLIENT_ATTACH);
         req_builder.setArgument(String.valueOf(targetSessionId));
         req_builder.setRole(GateTalk.Request.Role.SOURCE);
 
         GateTalk.Command.Builder cmd_builder = GateTalk.Command.newBuilder();
         cmd_builder.addRequest(req_builder.build());
         int commPort;
         try {
             GateTalk.Response res = MistSession.sendRequest(cmd_builder.build());
             if(!res.getSuccess()) {
                 myErr.printf("failed: %s %n", res.getException());
                 exitCode = RetVal.ATTACH_FAILED.ordinal();
                 return;
             }
             commPort = Integer.parseInt(res.getContext());
         }
         catch(MistException e) {
             myErr.println(e.getMessage());
             exitCode = RetVal.ATTACH_FAILED.ordinal();
             return;
         }
 
         Socket sock = null;
         BufferedInputStream socketIn = null;
         BufferedOutputStream socketOut = null;
         BufferedOutputStream out = null;
         try {
             sock = new Socket();
             sock.setReuseAddress(true);
             sock.setTcpNoDelay(true);
             sock.connect(new InetSocketAddress("127.0.0.1", commPort));
 
             socketIn = new BufferedInputStream(sock.getInputStream());
             socketOut = new BufferedOutputStream(sock.getOutputStream());
             out = new BufferedOutputStream(myOut);
             Packet pack = new Packet();
             int rdcnt = -1;
             int cnt = 0;
             long prev_time = System.nanoTime();
             do {
                 reaper.setBirth(new Date().getTime());
                 if((rdcnt = pack.read(socketIn)) > 0) {
                     pack.write(out);
                    if(myOut.checkError()) {
                        throw new IOException("MistSource: Pipe is broken!");
                    }
                     cnt++;
 
                     pack.setPayload(GateTalk.Response.newBuilder().setSuccess(true).build().toByteArray());
                     pack.write(socketOut);
 
                     if(usePerf && cnt % PERF_COUNT == 0) {
                         long curr_time = System.nanoTime();
                         float duration = (float) (curr_time - prev_time) / (1000000000);
                         myErr.printf("mist-source: %.2f mps%n", (float) PERF_COUNT / duration);
                         prev_time = curr_time;
                     }
 
                     if(limitCount != -1 && cnt >= limitCount) {
                         flushRequest(makeDetachRequest());
                         break;
                     }
                 }
             } while(rdcnt != -1);
             daemonDetach = true;
         }
         catch(IOException e) {
             myErr.println(e.getMessage());
         }
         finally {
             try {
                 socketIn.close();
                 socketOut.close();
                 out.close();
                 sock.close();
             }
             catch(IOException e) {
                 myErr.println(e.getMessage());
             }
         }
     }
 
     private void processMount() {
         GateTalk.Client client = MistSession.makeClientRequest(targetSessionId, exchange, true, true); 
         GateTalk.Command.Builder cmd_builder = GateTalk.Command.newBuilder();
         cmd_builder.addClient(client);
         try {
             GateTalk.Response res = MistSession.sendRequest(cmd_builder.build());
             if(res.getSuccess()) 
                 myOut.printf("%s%n", res.getContext());
             else {
                 myErr.printf("failed: %s%n", res.getException());
                 exitCode = RetVal.MOUNT_FAILED.ordinal();
             }
         }
         catch(MistException e) {
             myErr.println(e.getMessage());
         }
     }
 
     private void processUnmount() {
         GateTalk.Client client = MistSession.makeClientRequest(targetSessionId, exchange, true, false); 
         GateTalk.Command.Builder cmd_builder = GateTalk.Command.newBuilder();
         cmd_builder.addClient(client);
         try {
             GateTalk.Response res = MistSession.sendRequest(cmd_builder.build());
             if(res.getSuccess()) 
                 myOut.printf("%s%n", res.getContext());
             else {
                 myErr.printf("failed: %s%n", res.getException());
                 exitCode = RetVal.UNMOUNT_FAILED.ordinal();
             }
         }
         catch(MistException e) {
             myErr.println(e.getMessage());
         }
     }
 
     private void processDetach() {
         GateTalk.Command.Builder cmd_builder = GateTalk.Command.newBuilder();
         cmd_builder.addRequest(makeDetachRequest());
         try {
             GateTalk.Response res = MistSession.sendRequest(cmd_builder.build());
             if(res.getSuccess()) 
                 myOut.printf("%s%n", res.getContext());
             else {
                 myErr.printf("failed: %s%n", res.getException());
                 exitCode = RetVal.DETACH_FAILED.ordinal();
             }
         }
         catch(MistException e) {
             myErr.println(e.getMessage());
         }
     }
     
     ////////////////////////////////////////////////////////////////////////////////
 
     public MistSource() {
         super("mist-source");
         if(!Daemon.isRunning()) { 
             System.err.println("Daemon not running");
             System.exit(-1);
         }
         exitCode = RetVal.OK.ordinal();
     }
 
     public int run(String argv[]) {
         exchange.reset();
 
         LongOpt[] longopts = new LongOpt[] {
             new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h'), 
             new LongOpt("mount", LongOpt.REQUIRED_ARGUMENT, null, 'm'), 
             new LongOpt("unmount", LongOpt.REQUIRED_ARGUMENT, null, 'u'), 
             new LongOpt("attach", LongOpt.NO_ARGUMENT, null, 'a'), 
             new LongOpt("detach", LongOpt.NO_ARGUMENT, null, 'd'), 
             new LongOpt("topic", LongOpt.NO_ARGUMENT, null, 't'), 
             new LongOpt("queue", LongOpt.NO_ARGUMENT, null, 'q'), 
             new LongOpt("perf", LongOpt.NO_ARGUMENT, null, 'p'),
             new LongOpt("limit-count", LongOpt.REQUIRED_ARGUMENT, null, 'c'),
             new LongOpt("timeout", LongOpt.REQUIRED_ARGUMENT, null, 'i'),
         };
 
         try {
             Getopt g = new Getopt("mist-source", argv, "hm:u:adtqp", longopts);
             int c;
             while((c = g.getopt()) != -1) {
                 switch(c) {
                 case 'm':
                     currCmd = CmdType.MOUNT;
                     exchange.set(g.getOptarg());
                     break;
                 case 'u':
                     currCmd = CmdType.UNMOUNT;
                     exchange.set(g.getOptarg());
                     break;
                 case 'a':
                     currCmd = CmdType.ATTACH;
                     break;
                 case 'd':
                     currCmd = CmdType.DETACH;
                     break;
                 case 'q':
                     exchange.setQueue();
                     break;
                 case 't':
                     exchange.setTopic();
                     break;
                 case 'p':
                     usePerf = true;
                     break;
                 case 'c':
                     limitCount = Integer.parseInt(g.getOptarg());
                     break;
                 case 'i':
                     timeout = Integer.parseInt(g.getOptarg());
                     break;
                 }
             }
             
             if(g.getOptind() < argv.length)
                 targetSessionId = Integer.parseInt(argv[g.getOptind()]);
             else {
                 myErr.printf("no SESSION_ID specified %n");
                 currCmd = CmdType.NONE;
             }
 
             if(currCmd == CmdType.MOUNT)
                 processMount();
             else if(currCmd == CmdType.UNMOUNT)
                 processUnmount();
             else if(currCmd == CmdType.ATTACH)
                 processAttach();
             else if(currCmd == CmdType.DETACH)
                 processDetach();
             else if(currCmd == CmdType.NONE)
                 printUsage();
         }
         catch(NumberFormatException e) {
             myErr.printf("%s, invalid number format %n", e.getMessage());
         }
         catch(Exception e) {
             myErr.println(e.getMessage());
         }
         return exitCode;
     }
 
     public static void main(String argv[]) {
         myApp = new MistSource();
         myApp.threadMain = Thread.currentThread();
         myApp.addShutdownHook();
         myApp.run(argv);
         myApp.threadMain = null;
         System.exit(myApp.exitCode);
     }
 }
