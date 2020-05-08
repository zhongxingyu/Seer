 /**
  *
  * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 
 package org.apache.yoko;
 
 import java.io.File;
 import java.rmi.registry.Registry;
 import java.util.Iterator;
 import java.util.Map.Entry;
 
 import org.apache.yoko.processmanager.JavaProcess;
 import org.apache.yoko.processmanager.ProcessManager;
 
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 /**
  * Superclass for ORB tests. Takes care of setting up a a server process and a client process.
  * It also sets the java.endorsed.dirs property and launches the client process.
  *
  * Currently, the client waits for the server to create a file containing an IOR. This
  * is used to delay the client until the server has started. the setWaitFile(File) method 
  * can be used to set the name of this file, which varies in the ORB tests.
  */
 public class AbstractOrbTestBase extends TestCase {
     protected ProcessManager processManager;
     protected JavaProcess server, client;
     protected File waitForFile;
     int waitForFileTimeout = 10000;
         
     public AbstractOrbTestBase() {
         super();
     }
         
     public AbstractOrbTestBase(String name) {
         super(name);
     }
         
     protected void setUp() throws Exception {
         super.setUp();
         processManager = new ProcessManager(Registry.REGISTRY_PORT);
         client = new JavaProcess("client", processManager);
         client.addSystemProperty("java.endorsed.dirs");
         server = new JavaProcess("server", processManager);
         server.addSystemProperty("java.endorsed.dirs");
         JavaProcess[] processes = new JavaProcess[] {server, client};
         for(int i = 0; i < processes.length; i++) {
             JavaProcess process = processes[i];
             for(Iterator it = System.getProperties().entrySet().iterator(); it.hasNext();) {
                 Entry entry = (Entry) it.next();
                 String key = entry.getKey().toString();
                 if(key.startsWith(process.getName() + ":")){
                     int pos = key.indexOf(':') + 1;
                     String property = key.substring(pos);
                     String value = entry.getValue().toString();
                     System.out.println("Adding (" + property + ", " + value + ")");
                     process.addSystemProperty(property, value);
                 }
             }
         }
         client.launch();
     }
 
     public void tearDown() throws Exception {
         client.exit(0);
         if(getWaitForFile() != null && getWaitForFile().exists()) {
             getWaitForFile().delete();
         }
     }
     protected void runServerClientTest(String serverClass, String clientClass) throws Exception {
         runServerClientTest(serverClass, new String[0], clientClass, new String[0]);
     }
     protected void runServerClientTest(String serverClass, String[] serverArgs, 
                                        String clientClass, String[] clientArgs) throws Exception {
         server.launch();
         Thread serverThread = server.invokeMainAsync(serverClass, serverArgs);
         waitForFile();
         // TODO: Need to find a better way, this slows down testing unneccesarily,
         // and is somewhat non-robust.
         Thread.sleep(1000);
         client.invokeMain(clientClass, clientArgs);
        serverThread.join(10000);
         server.exit(0);
                 
     }
         
     public void setWaitForFile(File file) {
         this.waitForFile = file;
     }
         
     public File getWaitForFile() {
         return waitForFile;
     }
         
     protected void waitForFile() {
         long timeBefore = System.currentTimeMillis();
         if(getWaitForFile() != null) {
             while(true) {
                 try {
                     if(getWaitForFile().exists()) {
                         break;
                     }
                     Thread.sleep(50);
                     if(System.currentTimeMillis() > timeBefore + waitForFileTimeout) {
                         fail("The file " + getWaitForFile() + 
                              "was not created within " + waitForFileTimeout + "ms");
                     }
                 }
                 catch(InterruptedException e) {
                     throw new Error(e);
                 }
                 getWaitForFile().deleteOnExit();
             }
         }
     }           
 }
