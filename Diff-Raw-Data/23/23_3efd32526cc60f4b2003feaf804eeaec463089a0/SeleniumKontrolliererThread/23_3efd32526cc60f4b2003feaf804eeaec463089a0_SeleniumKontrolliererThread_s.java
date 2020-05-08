 package com.googlecode.mad_schuelerturnier.test.matchonly;
 
 import com.googlecode.mad_schuelerturnier.test.SeleniumDriverWrapper;
 import org.apache.log4j.Logger;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 //
 
 /**
  * Created with IntelliJ IDEA. User: dama Date: 02.01.13 Time: 19:30 To change this template use File | Settings | File Templates.
  */
 public class SeleniumKontrolliererThread extends Thread {
 
     private static final Logger LOG = Logger.getLogger(SeleniumKontrolliererThread.class);
 
     private SeleniumDriverWrapper util = new SeleniumDriverWrapper();
 
 
     @Override
     public void run() {
 
        this.util.login("t", "t");
 
 
         for (int i = 0; i < 10; i++) {
 
             if (MatchOnly.restart) {
                 break;
             }
 
             this.util.clickById("form_m:m_kontrollierer");
 
             this.util.sleepAMoment(5);
 
             final String str = this.util.getSourceAsString();
 
             final Pattern pattern = Pattern.compile("([M]|[K])[0-9]{3}");
 
             final Matcher matcher = pattern.matcher(str);
 
             final List<String> mannschaften = new ArrayList<String>();
 
             while (matcher.find()) {
                 mannschaften.add(matcher.group());
             }
 
             if (mannschaften.size() < 1) {
                 this.util.sleepAMoment(5);
             } else {
                 if ((mannschaften.size() % 4) == 0) {
                     //this.util.clickById("form1:dataTable1:0:reject",true);
                     this.util.clickById("form1:dataTable1:0:save");
                 } else {
                     this.util.clickById("form1:dataTable1:0:save");
                 }
                 i = 0;
             }
 
         }
         this.util.distroy();
     }
 
     public static void main(final String[] args) {
         final SeleniumKontrolliererThread th = new SeleniumKontrolliererThread();
         th.run();
         try {
             th.join();
         } catch (final InterruptedException e) {
             e.printStackTrace();
         }
     }
 
 
 }
