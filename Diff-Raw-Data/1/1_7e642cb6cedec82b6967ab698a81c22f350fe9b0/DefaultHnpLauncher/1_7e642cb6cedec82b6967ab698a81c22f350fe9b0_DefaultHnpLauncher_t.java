 package com.pivotal.hamster.appmaster.hnp;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map.Entry;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.yarn.event.Dispatcher;
 
 import com.pivotal.hamster.appmaster.event.HamsterFailureEvent;
 import com.pivotal.hamster.appmaster.hnp.HnpService.HnpState;
 import com.pivotal.hamster.common.HamsterConfig;
 import com.pivotal.hamster.common.HamsterException;
 
 public class DefaultHnpLauncher extends HnpLauncher {
   private static final Log LOG = LogFactory.getLog(DefaultHnpLauncher.class);
 
   Dispatcher dispatcher;
   Thread execThread;
   Thread errThread;
   Thread outThread;
   HnpService service;
   String[] args;
   int serverPort;
   
   static class StreamGobbler implements Runnable {
     BufferedReader reader;
     boolean out;
     Dispatcher dispatcher;
 
     public StreamGobbler(Dispatcher dispatcher, BufferedReader reader, boolean out) {
       this.reader = reader;
       this.out = out;
       this.dispatcher = dispatcher;
     }
 
     public void run() {
       try {
         String line = null;
         while ((line = reader.readLine()) != null) {
           if (out)
             System.out.println(line);
           else
             System.err.println(line);
         }
       } catch (IOException e) {
         dispatcher.getEventHandler().handle(new HamsterFailureEvent(e, "failed when fetch output from HNP"));
         return;
       }
     }
   }
   
   
   public DefaultHnpLauncher(Dispatcher dispatcher, HnpService service, String[] args) {
     super(DefaultHnpLauncher.class.getName());
     this.dispatcher = dispatcher;
     this.args = args;
     this.service = service;
   }
 
   @Override
   public void init(Configuration conf) {
     super.init(conf);
     this.serverPort = service.getServerPort();
   }
   
   @Override
   public void start() {
     execThread = new Thread(new Runnable() {
 
       @Override
       public void run() {
         // try to launch process
         Process proc;
         int exitCode = -1;
         
         try {
           // exec process
           proc = Runtime.getRuntime().exec(args, copyAndSetEnvs());
           
           // get err stream and out stream
           BufferedReader bre = new BufferedReader(new InputStreamReader(
               proc.getErrorStream()));
           BufferedReader bri = new BufferedReader(new InputStreamReader(
               proc.getInputStream()));
 
           // use thread fetch output
           errThread = new Thread(new StreamGobbler(dispatcher, bre, false));
           outThread = new Thread(new StreamGobbler(dispatcher, bri, true));
           
           // start thread fetch output
           errThread.start();
           outThread.start();
           
           // wait for HNP dead
           exitCode = proc.waitFor();
         } catch (Exception e) {
           dispatcher.getEventHandler().handle(new HamsterFailureEvent(e, "exception when launch HNP process"));
           return;
         }
         
         LOG.info("note HNP exit with exit code = " + exitCode);
         if (service.getHnpState() != HnpState.Finished) {
           // send a message to hamster terminate handler, this message will be ignored if "finish" is called by hnp
           dispatcher.getEventHandler().handle(new HamsterFailureEvent(
               new HamsterException("terminate before HNP called finish"), "terminate before HNP called finish"));
         }
       }
     });
     
     execThread.start();    
     super.start();
   }
   
   @Override
   public void stop() {
     LOG.info("stop hnp launcher");
     if (execThread.isAlive()) {
       execThread.interrupt();
     }
     if (errThread.isAlive()) {
       errThread.interrupt();
     }
     if (outThread.isAlive()) {
       outThread.interrupt();
     }
   }
   
   String[] copyAndSetEnvs() {
     List<String> envs = new ArrayList<String>();
     for (Entry<String, String> entry : System.getenv().entrySet()) {
       envs.add(entry.getKey() + "=" + entry.getValue());
     }
     envs.add(HamsterConfig.AM_UMBILICAL_PORT_ENV_KEY + "=" + serverPort);
     return envs.toArray(new String[0]);
   }
 }
