 package com.fing.pis.bizativiti.test.general;
 
 import org.junit.Test;
 
 import com.fing.pis.bizativiti.plugin.xpdl.Util;
 
 public class HTMLtoTextTest {
 
     @Test
     public void test() {
        String html = "&lt;p&gt;&lt;span style=\"font-family:Segoe UI;font-size:8pt;\"&gt;Description Start Event&lt;/span&gt;&lt;/p&gt;&lt;p&gt;&lt;span style=\"font-family:Segoe UI;font-size:8pt;\"&gt;Description Start Event2&lt;/span&gt;&lt;/p&gt;";
         String toreturn = Util.getPlainTextFromHTML(html);
         System.out.print(toreturn);
     }
 }
