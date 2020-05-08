 /*
  * Copyright (c) 2013, Tripwire, Inc.
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are
  * met:
  *
  *  o Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  *
  *  o Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
  * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.jmxdatamart.Extractor;
 
 import java.io.FileInputStream;
 import java.lang.management.ManagementFactory;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.management.InstanceAlreadyExistsException;
 import javax.management.InstanceNotFoundException;
 import javax.management.MBeanRegistrationException;
 import javax.management.MBeanServer;
 import javax.management.MalformedObjectNameException;
 import javax.management.NotCompliantMBeanException;
 import javax.management.ObjectName;
 import org.jmxdatamart.JMXTestServer.CarBean;
 import org.jmxdatamart.JMXTestServer.TestBean;
 
 public class Main {
 
   private static boolean demo = true;
 
   public static void main(String[] args) throws Exception {
     if (demo) {
       demo();
     }
     System.out.println("extract");
 
     if (args.length != 1) {
       System.out.println("Program need only 1 argument to the setting file");
      return;
     }

     Settings s = Settings.fromXML(
             new FileInputStream(args[0]));
 
     final Extractor extractor = new Extractor(s);
 
     Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
       @Override
       public void run() {
         if (extractor.isPeriodicallyExtract()) {
           extractor.stop();
         }
       }
     }));
 
     if (!extractor.isPeriodicallyExtract()) {
       System.out.println("Extractor is set to run once only!");
       return;
     }
 
    System.out.println("Ctrl-C to cancel the extracting process...");

    while (true) {
      Thread.sleep(10000);
    }

 
   }
 
   private static void demo() throws MalformedObjectNameException, NotCompliantMBeanException, InstanceAlreadyExistsException, MBeanRegistrationException, InstanceNotFoundException {
     TestBean tb1 = new TestBean();
     tb1.setA(new Integer(42));
     tb1.setB(new Long(-1));
     final ObjectName tbName1 = new ObjectName("com.personal.JMXTestServer:name=TestBean1");
 
     TestBean tb2 = new TestBean();
     tb2.setA(new Integer(55));
     tb2.setB(new Long(-99));
     final ObjectName tbName2 = new ObjectName("com.personal.JMXTestServer:name=TestBean2");
 
     CarBean cb = new CarBean();
     final ObjectName cbName = new ObjectName("org.jmxdatamart:name=CarBean");
 
     final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
     mbs.registerMBean(tb1, tbName1);
     mbs.registerMBean(cb, cbName);
     mbs.registerMBean(tb2, tbName2);
     System.out.println("Registered tb1, cb, and tb2");
   }
 }
