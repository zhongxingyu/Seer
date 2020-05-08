 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.wgr.xenmaster.monitoring.data;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import net.wgr.xenmaster.TestBase;
 import org.junit.Test;
 
 /**
  * 
  * @author double-u
  */
 public class RRDTest extends TestBase {
 
    //@Test
     public void testSomeMethod() throws MalformedURLException, IOException {
         RRD rrd = RRD.parse(new FileInputStream("rrd.xml"));
     }
 }
