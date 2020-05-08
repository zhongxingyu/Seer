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
 
import org.apache.commons.logging.impl.SimpleLog;
 import org.apache.log4j.*;
 import org.jivesoftware.smack.*;
 
import org.apache.commons.logging.*;

 import java.io.*;
 import org.json.*;
 
 /* Google Voice */
 import com.techventus.server.voice.Voice;
 
 
 /**
  *
  * @author brian.roy
  */
 
 public class QueueSendXMPP extends Thread {
 
     private static XMPPConnection tConn;
     private static Boolean isDone = false;
     private static ConfigFileReader cfrCfg;
 
     static org.apache.log4j.Logger myLog = org.apache.log4j.Logger.getLogger("com.briantroy.alertserver.main");
    protected static Log log = LogFactory.getLog("xmpp_beanstalk_log");
 
     public QueueSendXMPP(XMPPConnection xConn, ConfigFileReader cfg) {
         tConn = xConn;
         cfrCfg = cfg;
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
                if(bsE.getMessage() != "TIMED OUT") myLog.error(bsE.getMessage());
             }
         }
 
 
     }
 
     private static void pooledQueue()  throws BeanstalkException {
             BeanstalkPool pool = new BeanstalkPool(cfrCfg.getConfigItem("beanstalk_host"), Integer.valueOf(cfrCfg.getConfigItem("beanstalk_port")),
                             30, //poolsize
                     cfrCfg.getConfigItem("bs_queue_xmpp") //tube to use
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
 
                     if(thisMsg.has("imTo") && thisMsg.has("imMsg")) {
                         // Good message
                         try {
                             Chat newChat = tConn.createChat(thisMsg.getString("imTo"));
                             try {
                                     newChat.sendMessage(thisMsg.getString("imMsg"));
                                     return true;
                             } catch (XMPPException e) {
                                     // TODO Auto-generated catch block
                                     e.printStackTrace();
                                     myLog.error(e.getMessage());
                                     return false;
                             }
                         } catch (JSONException e) {
                             myLog.error(e.getMessage());
                             return false;
                         }
                     } else {
                         myLog.error("Asked to send an invalid message...");
                         return false;
                     }
 	}
 
 }
