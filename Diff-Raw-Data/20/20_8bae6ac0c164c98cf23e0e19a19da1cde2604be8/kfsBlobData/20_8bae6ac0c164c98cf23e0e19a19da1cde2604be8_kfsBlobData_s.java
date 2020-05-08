 package kfs.kfsDbi;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author pavedrim
  */
 public class kfsBlobData {
     //private InputStream ind;
     private byte[] bb;
     
     public kfsBlobData(byte []data) {
         bb = data;//Arrays.copyOf(data, data.length);
     }
     public kfsBlobData(InputStream ind) {
         ByteArrayOutputStream output = new ByteArrayOutputStream();
         byte []buffer = new byte[1024];
         int n;
         try {
             while (-1 != (n = ind.read(buffer))) {
                 output.write(buffer, 0, n);
             }
         } catch (IOException ex) {
             Logger.getLogger(kfsBlobData.class.getName()).log(Level.SEVERE, //
                     "Error in copy inputstream", ex);
         }
         bb = output.toByteArray();
     }
     
     public boolean isNull() {
         return (bb == null) || (bb.length <= 0);
     }
     
     public int getLength() {
         return bb.length;
     }
 
     public InputStream getInputStream() {
        return new ByteArrayInputStream(bb);
     }
     
     public byte[] getBytes(){
         return bb;
     }
 }
