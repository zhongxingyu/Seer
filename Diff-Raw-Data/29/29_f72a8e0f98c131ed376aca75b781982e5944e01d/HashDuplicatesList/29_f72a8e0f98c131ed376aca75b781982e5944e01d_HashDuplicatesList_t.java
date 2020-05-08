 package dupa.utils;
 
 import dupa.DupFile;
 import dupa.Duplicate;
 
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
