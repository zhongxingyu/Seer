 package org.ubif.goldfish;
 
 import org.shokai.evmsg.*;
 import java.net.URLEncoder;
 import org.json.JSONObject;
 
 public class JsObject {
     private AppActivity activity;
     private org.shokai.evmsg.TcpClient tcp;
     private org.shokai.evmsg.Udp udp;
     private String path;
     public String getPath(){return this.path;}
     
     public JsObject(AppActivity context, String path){
         this.activity = context;
         this.path = path;
     }
     
     public boolean exec(String javascript){
         try{
             String encoded = URLEncoder.encode(javascript);
             activity.loadUrl("javascript:goldfish.eval_script(\""+encoded+"\");");
         }
         catch(Exception ex){
             return false;
         }
         return true;
     }
     
     public boolean exec(String javascript, JsExecCallback callback){
         return this.exec(javascript);
     }
     
     private float accX, accY, accZ;
     public void setAccelerometer(float x, float y, float z){
         this.accX = x;
         this.accY = y;
         this.accZ = z;
     }
     public String _accelerometer(){
         return "{\"x\":" + Float.toString(this.accX) +
                ",\"y\":" + Float.toString(this.accY) +
                ",\"z\":" + Float.toString(this.accZ) + "}";
     }
 
     private float gyroX, gyroY, gyroZ;
     public void setGyroscope(float x, float y, float z){
         this.gyroX = x;
         this.gyroY = y;
         this.gyroZ = z;
     }
     public String _gyroscope(){
         return "{\"x\":" + Float.toString(this.gyroX) +
                ",\"y\":" + Float.toString(this.gyroY) +
                ",\"z\":" + Float.toString(this.gyroZ) + "}";
     }
     
     private float light;
     public void setLight(float light){
         this.light = light;
     }
     
     public String _light(){
         return Float.toString(this.light);
     }
     
     private float magnetX, magnetY, magnetZ;
     public void setMagneticField(float x, float y, float z){
         this.magnetX = x;
         this.magnetY = y;
         this.magnetZ = z;
     }
     
     public String _magnetic_field(){
         return "{\"x\":" + Float.toString(this.magnetX) +
                ",\"y\":" + Float.toString(this.magnetY) +
                ",\"z\":" + Float.toString(this.magnetZ) + "}";
     }
     
     private float azimuth, pitch, roll;
     public void setOrientation(float azimush, float pitch, float roll){
         this.azimuth = azimush;
         this.pitch = pitch;
         this.roll = roll;
     }
     
     public String _orientation(){
         return "{\"azimush\":" + Float.toString(this.azimuth) +
                ",\"pitch\":" + Float.toString(this.pitch) +
                ",\"roll\":" + Float.toString(this.roll) + "}";
     }
     
     public void _tcp_connect(String host_port){
         if(this.tcp != null){
             this.tcp.close();
             this.tcp = null;
         }
         try{
             JSONObject json = new JSONObject(host_port);
             this.tcp = new TcpClient(json.getString("host"), json.getInt("port"));
             final JsObject that = this;
             this.tcp.addEventHandler(new TcpClientEventHandler(){
                 public void onMessage(String line) {
                     that.exec(path+".tcp.onMessage(\""+line+"\");");
                 }
 
                 public void onOpen() {
                     new Thread(){
                         public void run(){
                             that.exec(path+".tcp.onOpen();");
                         }
                     }.start();
                 }
 
                 public void onClose() {
                     new Thread(){
                         public void run(){
                             that.exec(path+".tcp.onClose();");
                         }
                     }.start();
                 }
             });
             this.tcp.connect();
         }
         catch(Exception ex){
             ex.printStackTrace();
         }
     }
     
     public void _tcp_send(String msg){
         this.tcp.send(msg);
     }
     
     public void _tcp_close(){
        this.tcp.close();
        this.tcp = null;
     }
     
     public String app_name(){
         return activity.getResources().getString(R.string.app_name);
     }
     
     public String tag(){
         if(!this.activity.getClass().getName().equals("org.ubif.goldfish.TagActivity")) return null;
         return ((TagActivity)activity).getTagId();
     }
 
     protected void stop(){
        if(this.tcp != null) this.tcp.close();
     }
 }
