 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package com.briantroy.AlertServer;
 
 
 /* Import tredrr beanstalk libraries */
 import com.trendrr.beanstalk.BeanstalkClient;
 import com.trendrr.beanstalk.BeanstalkException;
 import com.trendrr.beanstalk.BeanstalkJob;
 import com.trendrr.beanstalk.BeanstalkPool;
 
 
 import java.io.*;
 
 
 import org.apache.log4j.*;
 import org.json.*;
 
 /* Google Voice */
 import com.techventus.server.voice.Voice;
 
 /**
  *
  * @author brian.roy
  */
 public class QueueSendGVSMS extends Thread{
 
 
     static org.apache.log4j.Logger myLog = org.apache.log4j.Logger.getLogger("com.briantroy.alertserver.main");
     private static Boolean isDone = false;
     private static ConfigFileReader cfrCfg;
 
 
     public QueueSendGVSMS(ConfigFileReader cfg) {
         cfrCfg = cfg;
         myLog.info("Starting Google Voice Queue Worker...");
     }
 
     public void isDone() {
         isDone = true;
     }
 
     @Override
     public void run() {
 
         while(!isDone) {
             try {
 
                 pooledQueue();
 
             } catch (BeanstalkException bsE) {
                myLog.error(bsE.getMessage());
             }
         }
 
     }
 
     private static void pooledQueue()  throws BeanstalkException {
             BeanstalkPool pool = new BeanstalkPool(cfrCfg.getConfigItem("beanstalk_host"), Integer.valueOf(cfrCfg.getConfigItem("beanstalk_port")),
                             30, //poolsize
                     cfrCfg.getConfigItem("bs_queue_gvsms") //tube to use
             );
 
             BeanstalkClient client = pool.getClient();
 
             BeanstalkJob job = client.reserve(10);
             myLog.info("Got job: " + job);
             /*
              * Have to call the method here... need the client connection
              * to delete the job.
              */
             String jobBody = new String(job.getData());
             myLog.info("JSON From Queue: " + jobBody);
             try {
                 JSONObject iJob = new JSONObject(jobBody);
                 if(ClientMsg(iJob)) {
                     client.deleteJob(job);
                     client.close();
                 } else {
                     client.deleteJob(job);
                     client.close();
                 }
             } catch (JSONException e) {
                 myLog.error(e.getMessage());
                 client.close();
             }
 
 
     }
 
     private static boolean ClientMsg(JSONObject thisMsg) {
 
 		final int MAXMESSAGESIZE = 4;
 
 		String strMessage[] = new String [MAXMESSAGESIZE];
                     int i;
                     boolean blnTO = false;
                     boolean blnMSG = false;
                     String strTO = "";
                     String strMSG = "";
 
                     if(thisMsg.has("smsTo") && thisMsg.has("imMsg")) {
                         // Good message
                         
                         /* Test sending a Goggle Voice SMS
                          *
                          */
                         try {
                             Voice gvThis = new Voice(cfrCfg.getConfigItem("google_user"), cfrCfg.getConfigItem("google_password"));
                             gvThis.sendSMS(thisMsg.getString("smsTo"), thisMsg.getString("imMsg"));
                         } catch (IOException e) {
                             myLog.error(e.getMessage());
                         } catch (JSONException jsE) {
                             myLog.error(jsE.getMessage());
                         }
 
 
 
                         return true;
 
                        
                     } else {
                         myLog.error("Asked to send an invalid message...");
                         return false;
                     }
 	}
 }
 
 
