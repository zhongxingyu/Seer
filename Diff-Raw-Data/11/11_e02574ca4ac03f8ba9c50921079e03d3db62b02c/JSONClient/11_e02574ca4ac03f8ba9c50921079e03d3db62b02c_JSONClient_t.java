 package com.jckimble.jsonprotocol;
 
 import java.io.BufferedReader;
 import java.io.Closeable;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.Date;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /**
  *
  * @author jckimble
  */
public class JSONClient extends Thread implements Closeable {
     protected JSONCallback callback;
     protected DataOutputStream out=null;
     public long closedAt=0;
     protected Socket sock = null;
     private BufferedReader br = null;
     public JSONClient(String addr,int port){
         try{
             this.sock=new Socket(addr,port);
         } catch (UnknownHostException ex) {
         } catch (IOException ex) {
         }
     }
     public JSONClient(Socket sock){
         this.sock=sock;
     }
     public void setCallBack(JSONCallback callback){
         this.callback=callback;
     }
     public void sendRequest(JSONObject obj){
         sendRequest(obj.toString());
     }
     public void sendRequest(JSONArray arr){
         sendRequest(arr.toString());
     }
     public void sendRequest(String json){
         try {
             if(out != null)
                 out.writeBytes(json);
             else
                 callback.JSONError(json, new IOException("Connection is not open!"));
         } catch (IOException ex) {
             callback.JSONError(json, ex);
         }
     }
     @Override
     public void run(){
         try {
            sock.setKeepAlive(true);
             br=new BufferedReader(new InputStreamReader(sock.getInputStream()));
             out=new DataOutputStream(sock.getOutputStream());
             String line;
             while(br.ready()){
                 line=br.readLine();
                 parseJSON(line);
             }
         } catch (IOException ex) {
             callback.JSONError(null, ex);
         } finally {
             try {
                 out.close();
                 out=null;
                 if(br != null)
                     br.close();
                 if(sock != null)
                     sock.close();
             } catch (IOException ex) {
                 
             }
             closedAt=new Date().getTime();
         }
     }
 
     private void parseJSON(String line) {
         try {
             JSONObject obj=new JSONObject(line);
             callback.parseJSON(obj,this);
         } catch (JSONException ex) {
             try {
                 JSONArray array=new JSONArray(line);
                 callback.parseJSON(array,this);
             } catch (JSONException ex1) {
                 callback.JSONError(line, new JSONException("Unable To Parse JSON"));
             }
         }
     }
 
     public void close() {
             try {
                 if(out != null){
                     out.close();
                     out=null;
                 }
                 if(br != null){
                     br.close();
                     br=null;
                 }
                 if(sock != null){
                     sock.close();
                     sock=null;
                 }
             } catch (IOException ex) {
                 
             }
             closedAt=new Date().getTime();
     }
 }
