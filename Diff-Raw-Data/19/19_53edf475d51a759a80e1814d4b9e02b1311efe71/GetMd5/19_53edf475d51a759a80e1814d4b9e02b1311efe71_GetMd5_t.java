 package fr.minepod.boostrap;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import org.bouncycastle.util.encoders.Hex;
 
 public class GetMd5
 {
   private BufferedReader lect;
   private FileInputStream fis;
 
   public boolean VerifyMd5(File Md5F, File f)
   {
     if ((Md5F.exists()) && (f.exists()) && (f.length() > 0L)) {
       String tmp = "";
       try
       {
         this.lect = new BufferedReader(new FileReader(Md5F));
         if (this.lect.ready())
           tmp = tmp + this.lect.readLine();
       }
       catch (NullPointerException a) {
         System.out.println("Erreur : pointeur null");
       } catch (IOException a) {
         System.out.println("Probleme d'IO");
       }
 
       System.out.println("Signature numerique lue: " + tmp);
       try
       {
         MessageDigest md = MessageDigest.getInstance("MD5");
         this.fis = new FileInputStream(f);
         byte[] dataBytes = new byte[1024];
         int nread = 0;
 
         while ((nread = this.fis.read(dataBytes)) != -1) {
           md.update(dataBytes, 0, nread);
         }
 
         byte[] mdbytes = md.digest();
         StringBuffer sb = new StringBuffer();
 
         String result = new String(Hex.encode(mdbytes));
 
         if (tmp.equals(result)) {
           System.err.println("Verification MD5 = ok");
           return true;
         }
         return false;
       }
       catch (NoSuchAlgorithmException e)
       {
         e.printStackTrace();
         return false;
       }
       catch (IOException e) {
         e.printStackTrace();
         return false;
       }
     }
     System.out.println("The file not exists!");
     return false;
   }
 }
