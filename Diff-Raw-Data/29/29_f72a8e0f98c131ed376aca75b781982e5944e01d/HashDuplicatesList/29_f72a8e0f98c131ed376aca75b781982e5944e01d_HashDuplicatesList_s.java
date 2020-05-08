 package dupa.utils;
 
 import dupa.DupFile;
 import dupa.Duplicate;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
 
 /**
  *
  * @author Alexander Glumoff
  */
 public class HashDuplicatesList extends DuplicatesList {
 
   @Override
   public Duplicate collect(DupFile F) {
     String hash = F.getMd5();
     System.out.printf("File %s: %s\n", F, hash);
     return collect(hash, F);
   }
 }
