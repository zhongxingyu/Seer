 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package TagEncoderLib;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.HashMap;
 
 /**
  *
  * @author ovoloshchuk
  */
 public class BicycleTagEncoder {
     
     public static class UnknownFormatException extends Exception {
         
     }
 
     public static int BUFFER_SIZE = 4096;
 
     public enum Tag {
         Artist,
         Album,
         Title,
         Year,
         Comment,
         Genre;
     }
 
     public enum TagVersion {
         ID3V1,
         ID3V2
     }
 
     public static byte[] readFile(InputStream is) throws IOException {
         int nFileSize = is.available();
         byte[] buf = new byte[nFileSize];
         is.read(buf);
         return buf;
     }
 
     private static TagVersion parseTagVersion(byte[] data) throws IOException {
         byte[] first_3 = new byte[3];
         
         ByteArrayInputStream bis = new ByteArrayInputStream(data, 0, 3);      
         bis.read(first_3);
         if (new String(first_3).equals("ID3"))
             return TagVersion.ID3V2;
         
         bis = new ByteArrayInputStream(data, data.length - 128, 3);
         bis.read(first_3);
         if (new String(first_3).equals("TAG"))
             return TagVersion.ID3V1;
         return null;
     }
     
     public static AbstractTagEncoder getEncoder(InputStream is) throws IOException, UnknownFormatException {
         byte[] data = readFile(is);
         TagVersion version = parseTagVersion(data);
         switch(version) {
             case ID3V1:
                 return new ID3V1Encoder(data);
             case ID3V2:
                 return new ID3V2Encoder(data);
         }
         throw new UnknownFormatException();
     }
 
     public static void updateTagValue(InputStream is, OutputStream os, Tag tag, String value) throws IOException, UnknownFormatException {
         AbstractTagEncoder encoder = getEncoder(is);
         encoder.updateTagValue(os, tag, value);
     }
     
     
    public static void updateTags(InputStream is, OutputStream os, Tag[] tags, String[] values) {
        
     }
     
     public static HashMap<Tag, String> getTags(InputStream is, String sCharsetName) throws IOException, UnknownFormatException {
         AbstractTagEncoder encoder = getEncoder(is);
         return encoder.getTags(sCharsetName);
     }
 }
