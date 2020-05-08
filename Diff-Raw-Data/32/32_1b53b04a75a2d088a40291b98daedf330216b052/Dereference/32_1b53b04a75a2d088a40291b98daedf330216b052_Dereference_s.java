 package org.demond.java.dereference;
 
 /**
  * Created by IntelliJ IDEA.
  * User: degtyarev.dm
  */
 public class Dereference
 {
     public String dereferenceUrl(String url)
     {
         DereferenceReferences dereference = new DereferenceReferences();
         return dereference.getLocation(url);
     }
 
     public String [] dereferenceAndCheckUrl(String url)
     {
         GoogleSafebrowsingChecker gsc = new GoogleSafebrowsingChecker();
         String reference=dereferenceUrl(url);
         String check = gsc.check(reference);
         return new String[] {reference,check};
     }
 }
