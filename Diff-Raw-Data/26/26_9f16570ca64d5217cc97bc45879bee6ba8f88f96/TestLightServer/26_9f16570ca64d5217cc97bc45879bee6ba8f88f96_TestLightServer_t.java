/**
 * Razvan's code. 
 * Copyright 2008 based on Apache (share alike) see LICENSE.txt for details.
 */
 package com.razie.pub.http.test;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.net.Socket;
 import java.net.URL;
 
 import com.razie.pub.base.ActionItem;
 import com.razie.pub.base.ActionToInvoke;
 import com.razie.pub.base.log.Log;
 import com.razie.pub.lightsoa.HttpSoaBinding;
 
 /**
  * test the light server
  * 
  * @author razvanc99
  */
 public class TestLightServer extends TestLightBase {
 
     /** test the simple echo */
     public void testSimpleEcho() throws IOException, InterruptedException {
         // start server with echo impl
         SampleEchoCmdListener echo = new SampleEchoCmdListener();
         server.registerCmdListener(echo);
 
         // send echo command
         Socket remote = new Socket("localhost", PORT);
         PrintStream out = new PrintStream(remote.getOutputStream());
         out.println("echo samurai");
 
         // wait a bit for receiver thread to consume...
         for (long deadline = System.currentTimeMillis() + 2000; deadline > System.currentTimeMillis();) {
             Thread.sleep(100);
             if (echo.input != null)
                 break;
         }
         server.removeCmdListener(echo);
         assertTrue(echo.input.contains("samurai"));
     }
 
     /**
      * test the sample server
      */
     public void testSampleWebServer() throws IOException, InterruptedException {
         // start server with different port so it doesn't clash with the test server
         // also, since it's not threaded, we have to thread it...
         Thread runner = new Thread() {
             public void run() {
                 SampleWebServer.start(PORT + 1);
             }
         };
         // test doens't stop until the server actually died - stupid way ot test the server dying
         // runner.setDaemon(true);
         runner.start();
 
         // give it some time to start up...
         Thread.sleep(250);
 
         // send echo command
         ActionToInvoke action = new ActionToInvoke("http://localhost:" + (PORT + 1), new ActionItem(
                 "service/echo"), "msg", "samurai");
         String result = (String) action.exec(null);
         assertTrue(result.contains("samurai"));
         assertTrue(result.contains("style.css"));
 
         // send echo command
         action = new ActionToInvoke("http://localhost:" + (PORT + 1), new ActionItem("service/die"));
         result = (String) action.exec(null);
 
         // TODO should check the server died
     }
 
     static final Log logger = Log.Factory.create(TestLightServer.class.getName());
 }
