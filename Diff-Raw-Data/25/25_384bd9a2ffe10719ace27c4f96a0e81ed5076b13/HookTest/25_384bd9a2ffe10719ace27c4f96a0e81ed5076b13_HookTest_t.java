 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.wgr.xenmaster.web;
 
 import org.apache.log4j.ConsoleAppender;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.apache.log4j.TTCCLayout;
 import com.google.gson.Gson;
 import com.google.gson.JsonElement;
 import net.wgr.wcp.Command;
 import net.wgr.xenmaster.controller.Controller;
 import net.wgr.xenmaster.api.Host;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author double-u
  */
 public class HookTest {
     
     protected Hook hook;
 
     public HookTest() {
     }
 
     @BeforeClass
     public static void setUpClass() throws Exception {
         Logger root = Logger.getRootLogger();
         root.setLevel(Level.INFO);
         root.addAppender(new ConsoleAppender(new TTCCLayout()));
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception {
     }
 
     @Before
     public void setUp() {
         hook = new Hook();
     }
 
     @After
     public void tearDown() {
     }
 
     /**
      * Test of execute method, of class Hook.
      */
     @Test
     public void testExecute() {
         Controller.start();
         
         Hook.APICall apic = new Hook.APICall();
         apic.command = "Session[].getThisHost";
         Gson gson = new Gson();
         JsonElement json = gson.toJsonTree(apic);
         Command cmd = new Command("xen", "execute", apic);
         Object o = hook.execute(cmd);
         assertNotNull(o);
         Host s = (Host) o;
         System.out.println("Your current host id: " + s.getUUID().toString());
     }
 }
