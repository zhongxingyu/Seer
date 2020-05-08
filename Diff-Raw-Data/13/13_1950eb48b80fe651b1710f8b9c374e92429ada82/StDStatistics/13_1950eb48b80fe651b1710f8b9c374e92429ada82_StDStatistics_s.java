 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package protoStatisticsDevice;
 
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
 import protoLowareInterfaceDevice.LIDRequestHandler;
 
 /**
  *
  * @author Rodrigo Budog
  */
 public class StDStatistics {
     private StDData statisticData;
     private LIDRequestHandler clientSocket;
     
     public StDStatistics(LIDRequestHandler clientSocket){
         this.statisticData = StDData.instanceOfStDData();
         this.clientSocket = clientSocket;
     }
     
     public void sendStatisticsOfDownloadsAndUploads(){
         try {
 
             JSONObject arjs = new JSONObject();
             for (StDDataTransmission stddt : this.statisticData.getDownloads()) {
                 try {
                     arjs.append(stddt.getMsgID(), stddt.getProgress().toString());
                 } catch (JSONException ex) {
                     Logger.getLogger(StDStatistics.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
             DataOutputStream toCliente = null;
             //toCliente = new DataOutputStream(this.clientSocket.getOutputStream());
             toCliente = this.clientSocket.getOut();
             //send response and close connection
             synchronized(toCliente){
                 toCliente.write(arjs.toString().getBytes());
             }
             //ending message for requester
             
             String bye = "GOODBYE";
             synchronized(toCliente){
                 toCliente.write(bye.getBytes());
                 toCliente.close();
             }
         } catch (IOException ex) {
             Logger.getLogger(StDStatistics.class.getName()).log(Level.SEVERE, null, ex);
         }
        
     }
     
 }
