 /*
  * The MIT License
  *
  * Copyright 2013 Jason Unger <entityreborn@gmail.com>.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package com.laytonsmith.extensions.chhttpd;
 
 import com.laytonsmith.annotations.shutdown;
 import com.laytonsmith.annotations.startup;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import me.entityreborn.chhttpd.SimpleServer;
 import static me.entityreborn.chhttpd.SimpleServer.getVersion;
 
 /**
  *
  * @author Jason Unger <entityreborn@gmail.com>
  */
 public class Tracking {
    private static SimpleServer server;
    private static boolean started;
     
     public static SimpleServer getServer() {
         return server;
     }
     
     @startup
    public static void startup() {
         System.out.println("CHHTTPd v" + getVersion() + " starting...");
         
         server = new SimpleServer();
         started = false;
         server.listen(80);
         
         try {
             server.start();
             server.listen(8080);
             started = true;
         } catch (Exception ex) {
             Logger.getLogger(Tracking.class.getName()).log(Level.SEVERE, 
                     "Could not start HTTP server!", ex);
         }
         
         System.out.println("CHHTTPd v" + getVersion() + " started.");
     }
     
     @shutdown
    public static void shutdown() {
         System.out.println("CHHTTPd v" + getVersion() + " stopping...");
         
         try {
             if (started) {
                 server.stop();
             }
         } catch (Exception ex) {
             Logger.getLogger(Tracking.class.getName()).log(Level.SEVERE, 
                     "Could not stop HTTP server!", ex);
         }
         
         System.out.println("CHHTTPd v" + getVersion() + " stopped");
     }
 }
