 package dupa;
 
 import dupa.utils.HashDuplicatesList;
 import java.io.BufferedInputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
import java.text.Format;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Alexander Glumoff
  */
 public class DupFile {
 
   protected String name;
   protected long size;
 
   public DupFile(String name) {
     this.name = name;
   }
 
   public String getName() {
     return name;
   }
 
   public void setName(String name) {
     this.name = name;
   }
 
   public long getSize() {
     return size;
   }
 
   public String getSizeStr() {
     return String.format("%d", size);
   }
 
   public void setSize(long size) {
     this.size = size;
   }
 
   @Override
   public String toString() {
     return String.format("%s (%d b)", name, size);
   }
   
   private String makeFileHash(String alg) throws NoSuchAlgorithmException {
     String hash = "";
     MessageDigest md = MessageDigest.getInstance(alg);
     md.reset();
     try {
       BufferedInputStream in = new BufferedInputStream(new FileInputStream(getName()));
       try {
         int theByte;
         while ((theByte = in.read()) != -1) {
           md.update((byte) theByte);
         }
         in.close();
       } catch (IOException ex) {
         Logger.getLogger(HashDuplicatesList.class.getName()).log(Level.SEVERE, null, ex);
       }
       byte messageDigest[] = md.digest();
       StringBuffer hexString = new StringBuffer();
       for (int i = 0; i < messageDigest.length; i++) {
         String hex = Integer.toHexString(0xFF & messageDigest[i]);
         if (hex.length() == 1) {
           hexString.append('0');
         }
         hexString.append(hex);
       }
       hash = hexString.toString();
 
     } catch (FileNotFoundException ex) {
       Logger.getLogger(HashDuplicatesList.class.getName()).log(Level.SEVERE, null, ex);
     }
     return hash;
   }  
   
   public String getMd5() {
     String hash = "";
     try {
       hash = makeFileHash("MD5");
     } catch (NoSuchAlgorithmException ex) {
       Logger.getLogger(DupFile.class.getName()).log(Level.SEVERE, null, ex);
     }
     return hash;
   }
   
 }
