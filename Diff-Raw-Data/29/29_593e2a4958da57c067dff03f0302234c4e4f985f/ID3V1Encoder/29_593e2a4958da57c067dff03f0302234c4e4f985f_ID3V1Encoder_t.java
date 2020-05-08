 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package TagEncoderLib;
 
 import TagEncoderLib.BicycleTagEncoder.Tag;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.HashMap;
 
 /**
  *
  * @author ovoloshchuk
  */
 public class ID3V1Encoder extends AbstractTagEncoder {
 
     public ID3V1Encoder(byte[] data) {
         super(data);
     }
 
     @Override
     public HashMap<Tag, String> getTags(String sCharsetName) throws IOException {
         HashMap<Tag, String> tags = new HashMap<Tag, String>();
         byte[] baTags = getTagBytes();
 
         InputStream is = new ByteArrayInputStream(baTags);
         
         tags.put(Tag.Title, readTag(is, Tag.Title, sCharsetName));
         tags.put(Tag.Artist, readTag(is, Tag.Artist, sCharsetName));
         tags.put(Tag.Album, readTag(is, Tag.Album, sCharsetName));
         tags.put(Tag.Year, readTag(is, Tag.Year, sCharsetName));
         tags.put(Tag.Comment, readTag(is, Tag.Comment, sCharsetName));
 
         return tags;
     }
     
     private int getTagLength(Tag tag) {
         switch(tag) {
             case Artist:
             case Album:
             case Title:
             case Comment:
                 return 30;
             case Year:
                 return 4;
         }
         return 30;
     }
     
     private String readTag(InputStream is, Tag tag, String sCharsetName) throws IOException {
         int nLength = getTagLength(tag);
         byte[] buf = new byte[nLength];
         is.read(buf);
         String sResult = new String(buf, sCharsetName).trim(); 
         return sResult;
     }
 
     @Override
     public void updateTagValue(OutputStream os, Tag tag, String value) throws IOException {
         byte[] baStringTagValue = value.getBytes("UTF-8");
         byte[] baPaddedTagValue = new byte[30];
         for (int i = 0; i < 30; i++) {
             baPaddedTagValue[i] = (i < baStringTagValue.length)
                     ? baStringTagValue[i] 
                     : 0;
         }
 
         //byte[] baTags = getTagBytes();
         os.write(data, 0, data.length - 125);
         int nOffset = getTagOffset(tag);
         
         os.write(data, data.length - 125, nOffset);
         os.write(baPaddedTagValue);
         os.write(data, data.length-125+nOffset+30, 125 - nOffset - 30);
     }
 
     private static int getTagOffset(Tag tag) {
         switch (tag) {
             case Title:
                 return 0;
             case Artist:
                 return 30;
             case Album:
                 return 60;
         }
         return 0;
     }
 
     private byte[] getTagBytes() throws IOException {
         InputStream is = new ByteArrayInputStream(data, data.length - 128 + 3, 128);
         byte[] baTags = new byte[125];
         is.read(baTags);
         return baTags;
     }
 }
