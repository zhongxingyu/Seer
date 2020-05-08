 /**
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.fusesource.cloudlaunch.packaging;
 
 import junit.framework.TestCase;
 import org.fusesource.cloudlaunch.Expression.FileExpression;
 import static org.fusesource.cloudlaunch.Expression.file;
 import static org.fusesource.cloudlaunch.Expression.path;
 import org.fusesource.cloudlaunch.LaunchClient;
 import org.fusesource.cloudlaunch.LaunchDescription;
 import org.fusesource.cloudlaunch.Process;
 import org.fusesource.cloudlaunch.ProcessListener;
import org.fusesource.cloudlaunch.distribution.PluginClassLoader;
 import org.fusesource.mop.MOPRepository;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 import java.io.File;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.concurrent.TimeoutException;
 
 /**
  * RemoteLaunchTest
  * <p>
  * Description:
  * </p>
  *
  * @author cmacnaug
  * @version 1.0
  */
 public class RemoteLaunchTest extends TestCase {
 
     ClassPathXmlApplicationContext context;
     LaunchClient launchClient;
 
     protected void setUp() throws Exception {
         
         File basedir = new File(file("target/test-data").evaluate());
         recursiveDelete(basedir);
 
 
         // This should be getting set by the junit test runner to actuall plugins being tested.
         if( System.getProperty(PluginClassLoader.KEY_DEFAULT_PLUGINS_VERSION) == null ) {
             System.setProperty(PluginClassLoader.KEY_DEFAULT_PLUGINS_VERSION, "LATEST");
 
         }
 
         System.setProperty(MOPRepository.MOP_BASE, new File(basedir, "mop").getCanonicalPath());
         System.setProperty("basedir", basedir.getCanonicalPath());
         String commonRepo = new File(basedir, "common-repo").toURI().toString();
         System.setProperty("common.repo.url", commonRepo);
         System.setProperty("local.repo.url", new File(basedir, "local-repo").getCanonicalPath() );
 
         context = new ClassPathXmlApplicationContext("cloudlaunch-all-spring.xml");
         launchClient = (LaunchClient) context.getBean("launch-client");
 
     }
 
     public static void recursiveDelete(File f) {
         if (f.isDirectory()) {
             File[] files = f.listFiles();
             for (int i = 0; i < files.length; i++) {
                 recursiveDelete(files[i]);
             }
         }
         f.delete();
     }
 
 
     protected void tearDown() throws Exception {
         if (context != null) {
             context.destroy();
         }
 
         launchClient = null;
     }
 
     private String getAgent() throws InterruptedException, TimeoutException
     {
         launchClient.waitForAvailableAgents(5000);
         return launchClient.getAvailableAgents()[0].getAgentId();
     }
 
     public void testDataOutput() throws Exception {
         LaunchDescription ld = new LaunchDescription();
         ld.add("java");
         ld.add("-cp");
 
         ArrayList<FileExpression> files = new ArrayList<FileExpression>();
         for (String file : System.getProperty("java.class.path").split(File.pathSeparator)) {
             files.add(file(file));
         }
 
         ld.add(path(files));
         ld.add(DataInputTestApplication.class.getName());
 
         DataOutputTester tester = new DataOutputTester();
         tester.test(launchClient.launchProcess(getAgent(), ld, tester));
 
     }
 
     public class DataOutputTester implements ProcessListener {
 
         private final int TEST_OUTPUT = 0;
         private final int TEST_ERROR = 1;
         private final int SUCCESS = 2;
         private final int FAIL = 3;
 
         private static final String EXPECTED_OUTPUT = "test output";
         private static final String EXPECTED_ERROR = "test error";
 
         private int state = TEST_OUTPUT;
 
         private Exception failure;
 
         public DataOutputTester() throws RemoteException {
         }
 
         public void test(Process process) throws Exception {
 
             try {
 
                 synchronized (this) {
                     while (true) {
 
                         switch (state) {
                         case TEST_OUTPUT: {
                             System.out.println("Testing output");
                             process.write(Process.FD_STD_IN, new String("echo:" + EXPECTED_OUTPUT + "\n").getBytes());
                             break;
                         }
                         case TEST_ERROR: {
                             System.out.println("Testing error");
                             process.write(Process.FD_STD_IN, new String("error:" + EXPECTED_ERROR + "\n").getBytes());
                             break;
                         }
                         case SUCCESS: {
                             if (failure != null) {
                                 throw failure;
                             }
                             return;
                         }
                         case FAIL:
                         default: {
                             if (failure == null) {
                                 failure = new Exception();
                             }
                             throw failure;
                         }
                         }
 
                         int oldState = state;
                         wait(10000);
                         if (oldState == state) {
                             throw new Exception("Timed out in state: " + state);
                         }
                     }
                 }
             } finally {
                 process.kill();
             }
         }
 
         synchronized public void onProcessOutput(int fd, byte[] data) {
             String output = new String(data);
 
             if (fd == Process.FD_STD_OUT) {
                 System.out.print("STDOUT: " + output);
                 if (state == TEST_OUTPUT && EXPECTED_OUTPUT.equals(output.trim())) {
                     state = TEST_ERROR;
                 } else {
                     failure = new Exception("Unexpected system output: " + output);
                     state = FAIL;
                 }
                 notifyAll();
             } else if (fd == Process.FD_STD_ERR) {
                 System.out.print("STDERR: " + output);
                 if (state == TEST_ERROR && EXPECTED_ERROR.equals(output.trim())) {
                     state = SUCCESS;
                 } else {
                     failure = new Exception("Unexpected system err: " + output);
                     state = FAIL;
                 }
                 notifyAll();
             }
         }
 
         public synchronized void onProcessExit(int exitCode) {
             if (state < SUCCESS) {
                 failure = new Exception("Premature process exit");
                 state = FAIL;
                 notifyAll();
             }
         }
 
         public synchronized void onProcessError(Throwable thrown) {
             failure = new Exception("Unexpected process error", thrown);
             state = FAIL;
             notifyAll();
         }
 
         public void onProcessInfo(String message) {
             System.out.println("PROCESS INFO: " + message);
         }
     }
 
 }
