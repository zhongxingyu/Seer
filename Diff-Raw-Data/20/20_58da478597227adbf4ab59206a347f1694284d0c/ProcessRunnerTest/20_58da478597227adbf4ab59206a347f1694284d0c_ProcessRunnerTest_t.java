 /* $Id: NativeRunnerTest.java 39 2008-01-17 14:58:36Z mke $
  * $Revision: 39 $
  * $Date: 2008-01-17 14:58:36 +0000 (Thu, 17 Jan 2008) $
  * $Author: mke $
  *
  * The Summa project.
  * Copyright (C) 2005-2007  The State and University Library
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package dk.statsbiblioteket.util.console;
 
 import dk.statsbiblioteket.util.qa.QAInfo;
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 import java.io.*;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * NativeRunner Tester.
  */
 @QAInfo(level = QAInfo.Level.NORMAL,
         state = QAInfo.State.IN_DEVELOPMENT,
         author = "te")
 public class ProcessRunnerTest extends TestCase {
     public ProcessRunnerTest(String name) {
         super(name);
     }
 
     public void setUp() throws Exception {
         super.setUp();
     }
 
     public void tearDown() throws Exception {
         super.tearDown();
     }
 
     public void testSetParameters() throws Exception {
         //TODO: Test goes here...
     }
 
     public void testGetProcessOutput() throws Exception {
         //TODO: Test goes here...
     }
 
     public void testGetProcessError() throws Exception {
         //TODO: Test goes here...
     }
 
     public void testGetProcessOutputAsString() throws Exception {
         //TODO: Test goes here...
     }
 
     public void testGetProcessErrorAsString() throws Exception {
         //TODO: Test goes here...
     }
 
     public void testSimpleCall() throws Exception {
         ProcessRunner runner = new ProcessRunner("true");
         assertEquals("The execution of true should work fine",
                      0, runner.executeNoCollect());
         runner = new ProcessRunner("false");
         assertEquals("The execution of false should give 1",
                      1, runner.executeNoCollect());
     }
 
     public void testTimeout() throws Exception {
 
         ProcessRunner runner = new ProcessRunner(Arrays.asList("sleep","2"));
 
         try {
             runner.executeNoCollect(100);
             fail("The execution of sleep should time out");
         } catch(Exception e) {
             // Expected behaviour
         }
     }
 
     public void testEnvironment() throws Exception {
 
         Map<String,String> env = new HashMap<String,String>();
         env.put("FLAM","flim");
 
 
         ProcessRunner runner =
                 new ProcessRunner(Arrays.asList("/bin/sh","-c", "echo $FLAM"),env);
         //Nessesary for the command variable expansion to work correctly
 
         assertEquals("The execution of echo should work fine",
                      0, runner.executeNoCollect());
         assertEquals("The result of echo should be flim",
                      "flim\n", runner.getProcessOutputAsString());
     }
 
     public void testNullEnvironment () throws Exception {
         // Make sure we don't throw NPEs on null envs
        ProcessRunner runner = new ProcessRunner(Arrays.asList("/bin/echo",
                                                               "boo"));
     }
 
 
     public void testFeedProcess() throws Exception{
         Map<String,String> env = new HashMap<String,String>();
                 env.put("FLAM","flim");
 
         File f = new File("testfile");
         f.createNewFile();
         f.deleteOnExit();
         InputStream in = new FileInputStream(f);
         Writer out = new FileWriter(f);
         out.write("echo $FLAM");
         out.flush();
         ProcessRunner runner =
                 new ProcessRunner(Arrays.asList("/bin/sh"),env,in,null);
         //Nessesary for the command variable expansion to work correctly
 
         assertEquals("The execution of echo should work fine",
                      0, runner.executeNoCollect());
         assertEquals("The result of echo should be flim",
                      "flim\n", runner.getProcessOutputAsString());
 
     }
     public static Test suite() {
         return new TestSuite(ProcessRunnerTest.class);
     }
 }
