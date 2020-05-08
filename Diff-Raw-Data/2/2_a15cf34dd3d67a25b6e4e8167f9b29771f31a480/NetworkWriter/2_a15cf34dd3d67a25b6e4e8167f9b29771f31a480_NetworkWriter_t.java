 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package brutes.net;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.nio.ByteBuffer;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Karl
  */
 public class NetworkWriter {    
     private OutputStream os;
     private ByteArrayOutputStream baos;
 
     public NetworkWriter(OutputStream os) {
         this.os = os;
         this.baos = new ByteArrayOutputStream();
     }
     
     public void flush() throws IOException{
         Logger.getLogger(NetworkWriter.class.getName()).log(Level.INFO, "Flush");
         this.baos.flush();
         this.os.flush();
     }
     
     public NetworkWriter writeDiscriminant(byte value){
         this.writeByte(value);
         return this;
     }
     public NetworkWriter writeByte(byte value){
         Logger.getLogger(NetworkWriter.class.getName()).log(Level.INFO, "Byte  : {0}", value);
         try {
             this.baos.write(ByteBuffer.allocate(Protocol.SIZE_BYTE).put(value).array());
         } catch (IOException ex) {
             Logger.getLogger(NetworkWriter.class.getName()).log(Level.WARNING, null, ex);
         }
         return this;
     }
     public NetworkWriter writeShortInt(short value){
         Logger.getLogger(NetworkWriter.class.getName()).log(Level.INFO, "Short  : {0}", value);
         try {
             this.baos.write(ByteBuffer.allocate(Protocol.SIZE_SHORTINT).putShort(value).array());
         } catch (IOException ex) {
             Logger.getLogger(NetworkWriter.class.getName()).log(Level.WARNING, null, ex);
         }
         return this;
     }
     public NetworkWriter writeLongInt(int value){
         Logger.getLogger(NetworkWriter.class.getName()).log(Level.INFO, "Long  : {0}", value);
         try {
             this.baos.write(ByteBuffer.allocate(Protocol.SIZE_LONGINT).putInt(value).array());
         } catch (IOException ex) {
             Logger.getLogger(NetworkWriter.class.getName()).log(Level.WARNING, null, ex);
         }
         return this;
     }
     public NetworkWriter writeString(String value){
         Logger.getLogger(NetworkWriter.class.getName()).log(Level.INFO, "String  : {0}", value);
         try {
             this.writeShortInt((short)value.length());
             this.baos.write(value.getBytes(Charset.forName("UTF-8")));
         } catch (IOException ex) {
             Logger.getLogger(NetworkWriter.class.getName()).log(Level.WARNING, null, ex);
         }
         return this;
     }
     public NetworkWriter writeBoolean(boolean value){
         Logger.getLogger(NetworkWriter.class.getName()).log(Level.INFO, "Boolean  : {0}", value);
         try {
             byte bool = (byte)(value?0xFF:0x00);
             this.baos.write(ByteBuffer.allocate(Protocol.SIZE_BOOLEAN).put(bool).array());
         } catch (IOException ex) {
             Logger.getLogger(NetworkWriter.class.getName()).log(Level.WARNING, null, ex);
         }
         return this;
     }
     public NetworkWriter writeBooleanArray(boolean[] array){
         Logger.getLogger(NetworkWriter.class.getName()).log(Level.INFO, "Array : Boolean");
         this.writeShortInt((short)array.length);
         this.writeByte(Protocol.TYPE_BOOLEAN);
         for(int i = 0; i < array.length; i++){
             this.writeBoolean(array[i]);
         }
         return this;
     }
     public NetworkWriter writeLongIntArray(int[] array){
         Logger.getLogger(NetworkWriter.class.getName()).log(Level.INFO, "Array : Long");
         this.writeShortInt((short)array.length);
         this.writeByte(Protocol.TYPE_LONG);
         for(int i = 0; i < array.length; i++){
             this.writeLongInt(array[i]);
         }
         return this;
     }
     public NetworkWriter writeShortIntArray(short[] array){
         Logger.getLogger(NetworkWriter.class.getName()).log(Level.INFO, "Array : Short");
         this.writeShortInt((short)array.length);
         this.writeByte(Protocol.TYPE_SHORT);
         for(int i = 0; i < array.length; i++){
             this.writeShortInt(array[i]);
         }
         return this;
     }
     public NetworkWriter writeStringArray(String[] array){
         Logger.getLogger(NetworkWriter.class.getName()).log(Level.INFO, "Array : String");
         this.writeShortInt((short)array.length);
         this.writeByte(Protocol.TYPE_STRING);
         for(int i = 0; i < array.length; i++){
             this.writeString(array[i]);
         }
         return this;
     }
     
     public void send(){
         Logger.getLogger(NetworkWriter.class.getName()).log(Level.INFO, "Message sending...");
         try {
             ByteArrayOutputStream message = new ByteArrayOutputStream();
            int messageSize = baos.size();
             message.write(ByteBuffer.allocate(Protocol.SIZE_LONGINT).putInt(messageSize).array());
             message.write(baos.toByteArray());
             byte[] send = message.toByteArray();
             this.os.write(send);
             this.flush();
         } catch (IOException ex) {
             Logger.getLogger(NetworkWriter.class.getName()).log(Level.WARNING, null, ex);
         }
         Logger.getLogger(NetworkWriter.class.getName()).log(Level.INFO, "Message send");
     }
 }
