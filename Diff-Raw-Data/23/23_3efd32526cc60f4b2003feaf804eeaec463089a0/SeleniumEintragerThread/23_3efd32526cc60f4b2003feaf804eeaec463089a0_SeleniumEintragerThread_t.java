 package com.googlecode.mad_schuelerturnier.test.matchonly;
 
 import com.googlecode.mad_schuelerturnier.test.SeleniumDriverWrapper;
 import org.apache.log4j.Logger;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Created with IntelliJ IDEA. User: dama Date: 02.01.13 Time: 19:30 To change this template use File | Settings | File Templates.
  */
 public class SeleniumEintragerThread extends Thread {
 
     private static final Logger LOG = Logger.getLogger(SeleniumEintragerThread.class);
 
     private SeleniumDriverWrapper util = new SeleniumDriverWrapper();
 
     int i = 0;
 
     @Override
     public void run() {
 
        this.util.login("root", "root");
         this.util.sleepAMoment(1);
 
 
         for (int i = 0; i < 20; i++) {
 
 
             if (MatchOnly.restart) {
                 break;
             }
 
             this.util.clickById("form_m:m_eintrager");
 
             final String str = this.util.getSourceAsString();
 
             final Pattern pattern = Pattern.compile("([M]|[K])[0-9]{3}");
             final Matcher matcher = pattern.matcher(str);
 
             final List<String> mannschaften = new ArrayList<String>();
 
             // penalty eintragen
 
             if (this.util.getSourceAsString().contains("form1:dataTablePen:0:j_idt47")) {
                 this.util.sendByName("form1:dataTablePen:0:j_idt47", "irgendwas");
                 this.util.clickById("form1:dataTablePen:0:j_idt51");
             }
 
             while (matcher.find()) {
                 mannschaften.add(matcher.group());
             }
 
             if (mannschaften.size() < 2) {
                 this.util.sleepAMoment(3);
                 continue;
             }
 
             final String mansch = mannschaften.get(0);
             final String m = mansch.substring(2, 4);
             this.util.sleepAMoment(1);
             this.util.sendById("form1:dataTable1:0:toreA", m);
             this.util.sleepAMoment(1);
             final String mansch2 = mannschaften.get(1);
             final String m2 = mansch2.substring(2, 4);
             this.util.sendById("form1:dataTable1:0:toreB", m2);
             this.util.sleepAMoment(1);
 
             this.util.clickById("form1:dataTable1:0:save");
             this.util.sleepAMoment(1);
             this.i = 0;
 
 
         }
 
         this.util.distroy();
 
     }
 
     public static void main(final String[] args) {
         final SeleniumEintragerThread th = new SeleniumEintragerThread();
         th.run();
         try {
             th.join();
         } catch (final InterruptedException e) {
             e.printStackTrace();
         }
     }
 
 }
