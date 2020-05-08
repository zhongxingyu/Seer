 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package brutes.net;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.nio.ByteBuffer;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Karl
  */
 public class NetworkReader {
     private InputStream is;
 
     public NetworkReader(InputStream is) {
         this.is = is;
     }
     
     public long readMessageSize() throws IOException{
         return this.readLongInt();
     }
     public byte readByte() throws IOException{
         byte[] b = new byte[1];
         this.is.read(b);
         return b[0];
     }
     public byte readDiscriminant() throws IOException{
         return this.readByte();
     }
     public short readShortInt() throws IOException{
         byte[] b = new byte[Protocol.SIZE_SHORTINT];
        int read = 0;
        while(read < Protocol.SIZE_SHORTINT){
            read += this.is.read(b, read, Protocol.SIZE_SHORTINT - read);
        }
         ByteBuffer bb = ByteBuffer.wrap(b);
         return bb.getShort();
     }
     public int readLongInt() throws IOException{
         byte[] b = new byte[Protocol.SIZE_LONGINT];
        int read = 0;
        while(read < Protocol.SIZE_LONGINT){
            read += this.is.read(b, read, Protocol.SIZE_LONGINT - read);
        }
         ByteBuffer bb = ByteBuffer.wrap(b);
         return bb.getInt();
     }
     public String readString() throws IOException{
         String out = null;
         try {
             short length = this.readShortInt();
             byte[] b = new byte[length];
            int read = 0;
            while(read < length){
                read += this.is.read(b, read, length - read);
            }
             out = new String(b, "UTF-8");
         } catch (UnsupportedEncodingException ex) {
             Logger.getLogger(NetworkReader.class.getName()).log(Level.SEVERE, null, ex);
         }
         return out;
     }
     public boolean readBoolean() throws IOException {
         byte[] b = new byte[1];
         this.is.read(b);
         return (b[0] != 0x00);
     }
     public boolean[] readBooleanArray() throws IOException{
         short nbElements = this.readShortInt();
         byte type = this.readByte();
         if(type != Protocol.TYPE_BOOLEAN){
             throw new IOException(NetworkException.ARRAY_TYPE);
         }
         boolean[] array = new boolean[nbElements];
         for(int i = 0; i < nbElements; i++){
             array[i] = this.readBoolean();
         }
         return array;
     }
     public int[] readLongIntArray() throws IOException{
         short nbElements = this.readShortInt();
         byte type = this.readByte();
         if(type != Protocol.TYPE_LONG){
             throw new IOException(NetworkException.ARRAY_TYPE);
         }
         int[] array = new int[nbElements];
         for(int i = 0; i < nbElements; i++){
             array[i] = this.readLongInt();
         }
         return array;
     }
     public short[] readShortIntArray() throws IOException{
         short nbElements = this.readShortInt();
         byte type = this.readByte();
         if(type != Protocol.TYPE_SHORT){
             throw new IOException(NetworkException.ARRAY_TYPE);
         }
         short[] array = new short[nbElements];
         for(int i = 0; i < nbElements; i++){
             array[i] = this.readShortInt();
         }
         return array;
     }
     public String[] readStringArray() throws IOException{
         short nbElements = this.readShortInt();
         byte type = this.readByte();
         if(type != Protocol.TYPE_STRING){
             throw new IOException(NetworkException.ARRAY_TYPE);
         }
         String[] array = new String[nbElements];
         for(int i = 0; i < nbElements; i++){
             array[i] = this.readString();
         }
         return array;
     }
     
     public String readImage(String dest) throws IOException{
         short imgSize = this.readShortInt();
         byte[] bFile = new byte[imgSize];
        int read = 0;
        while(read < imgSize){
            read += this.is.read(bFile, read, imgSize - read);
        }
         
         File file = new File(dest);
         try (FileOutputStream fos = new FileOutputStream(file)) {
             fos.write(bFile);
         }
         
         return file.getPath();
     }
 }
