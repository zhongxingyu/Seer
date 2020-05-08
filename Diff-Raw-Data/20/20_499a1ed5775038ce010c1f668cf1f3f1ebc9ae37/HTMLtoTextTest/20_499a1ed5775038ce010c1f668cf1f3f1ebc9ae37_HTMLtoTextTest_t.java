 package com.fing.pis.bizativiti.test.general;
 
 import org.junit.Test;
 
 import com.fing.pis.bizativiti.plugin.xpdl.Util;
 
 public class HTMLtoTextTest {
 
     @Test
     public void test() {
        //        String html = "&lt;p&gt;&lt;span style=\"font-family:Segoe UI;font-size:8pt;\"&gt;Descripci&amp;#243;n Start Event&lt;/span&gt;&lt;/p&gt;";
        String html = "&amp;#224; &amp;#225; &amp;#226; &amp;#227; &amp;#228; &amp;#229; &amp;&#230; &amp;#231;";
         String toreturn = Util.getPlainTextFromHTML(html);
         System.out.print(toreturn);
     }
 }
