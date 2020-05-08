 /**
  * SAHARA Rig Client
  * 
  * Software abstraction of physical rig to provide rig session control
  * and rig device control. Automatically tests rig hardware and reports
  * the rig status to ensure rig goodness.
  *
  * @license See LICENSE in the top level directory for complete license terms.
  *
  * Copyright (c) 2009, University of Technology, Sydney
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice, 
  *    this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright 
  *    notice, this list of conditions and the following disclaimer in the 
  *    documentation and/or other materials provided with the distribution.
  *  * Neither the name of the University of Technology, Sydney nor the names 
  *    of its contributors may be used to endorse or promote products derived from 
  *    this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * @author <First name> <Last name> (mdiponio)
  * @date <day> <month> 2009
  *
  * Changelog:
  * - 29/10/2009 - mdiponio - Initial file creation.
  */
 package au.edu.uts.eng.remotelabs.rigclient.rig.control.tests;
 
 import static org.easymock.EasyMock.createMock;
 import static org.easymock.EasyMock.expect;
 import static org.easymock.EasyMock.replay;
 import static org.easymock.EasyMock.reset;
 import static org.easymock.EasyMock.verify;
 
 import java.io.File;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import junit.framework.TestCase;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import au.edu.uts.eng.remotelabs.rigclient.rig.control.AbstractBatchRunner;
 import au.edu.uts.eng.remotelabs.rigclient.util.ConfigFactory;
 import au.edu.uts.eng.remotelabs.rigclient.util.IConfig;
 
 /**
  * Tests the abstract batch runner.  Batch runner is intended to be run in 
  * its own thread, but for the purposes of testing it, runs in the same
  * thread as the test case.
  */
 public class AbstractBatchRunnerTester extends TestCase
 {
     /** Object of class under test. */
     private MockBatchRunner runner;
     
     /** Mock configuration class. */
     private IConfig mockConfig;
 
     /**
      * @throws java.lang.Exception
      */
     @Override
     @Before
     public void setUp() throws Exception
     {
         this.mockConfig = createMock(IConfig.class);
         expect(this.mockConfig.getProperty("Logger_Type"))
                 .andReturn("SystemErr");
         expect(this.mockConfig.getProperty("Log_Level"))
                 .andReturn("DEBUG");
         expect(this.mockConfig.getProperty("Default_Log_Format", "[__LEVEL__] - [__ISO8601__] - __MESSAGE__"))
                 .andReturn("[__LEVEL__] - [__ISO8601__] - __MESSAGE__");
         expect(this.mockConfig.getProperty("FATAL_Log_Format")).andReturn(null);
         expect(this.mockConfig.getProperty("PRIORITY_Log_Format")).andReturn(null);
         expect(this.mockConfig.getProperty("ERROR_Log_Format")).andReturn(null);
         expect(this.mockConfig.getProperty("WARN_Log_Format")).andReturn(null);
         expect(this.mockConfig.getProperty("INFO_Log_Format")).andReturn(null);
         expect(this.mockConfig.getProperty("DEBUG_Log_Format")).andReturn(null);
         replay(this.mockConfig);
         
         Field configField = ConfigFactory.class.getDeclaredField("instance");
         configField.setAccessible(true);
         configField.set(null, this.mockConfig);
         
         this.runner = new MockBatchRunner("", "", true);
     }
     
     /**
      * Test method for {@link au.edu.uts.eng.remotelabs.rigclient.rig.control.AbstractBatchRunner#invoke()}.
      */
     @Test
     public void testInvokeRunDirEnvFlush()
     {
         final String wdBase = System.getProperty("user.dir") + "/test/resources/BatchRunner";
         reset(this.mockConfig);
         expect(this.mockConfig.getProperty("Batch_Working_Dir"))
             .andReturn(wdBase);
         expect(this.mockConfig.getProperty("Batch_Create_Nested_Dir", "true"))
             .andReturn("true").atLeastOnce();
         expect(this.mockConfig.getProperty("Batch_Flush_Env", "false"))
             .andReturn("true");
         replay(this.mockConfig);
         
         try
         {
             Field field = AbstractBatchRunner.class.getDeclaredField("command");
             field.setAccessible(true);
             
             /* Sets a command which prints out the contents of a test file. On
              * Windows uses 'type' (apparently) and on sane operating
              * systems - Linux / UNIX, uses'cat'. */
             if (System.getProperty("os.name").equals("Windows"))
             {
                 field.set(this.runner, "type");
             }
             else
             {
                 field.set(this.runner, "cat");
             }
             
             final List<String> args = new ArrayList<String>();
             args.add(wdBase + "/TestFileToCat.txt");
             field = AbstractBatchRunner.class.getDeclaredField("commandArgs");
             field.setAccessible(true);
             field.set(this.runner, args);
             
             final Map<String, String> env = new HashMap<String, String>();
             env.put("PATH", "/bin");
             field = AbstractBatchRunner.class.getDeclaredField("envMap");
             field.setAccessible(true);
             field.set(this.runner, env);
             
             Method meth = AbstractBatchRunner.class.getDeclaredMethod("invoke");
             meth.setAccessible(true);
             assertTrue((Boolean)meth.invoke(this.runner));
             
             field = AbstractBatchRunner.class.getDeclaredField("workingDirBase");
             field.setAccessible(true);
             assertEquals(wdBase, field.get(this.runner));
             
             field = AbstractBatchRunner.class.getDeclaredField("workingDir");
             field.setAccessible(true);
             assertFalse(wdBase.equals(field.get(this.runner)));
             
             final String out = this.runner.getAllStandardOut();
             assertNotNull(out);
             assertEquals("", this.runner.getAllStandardErr());
             
             File[] files = new File(wdBase).listFiles();
             Calendar cal = Calendar.getInstance();
             boolean found = false;
             String name = null;
             for (File f : files)
             {
                 name = f.getName();
                 if (name.startsWith(String.valueOf(cal.get(Calendar.DATE))))
                 {
                     found = true;
                     assertTrue(f.isDirectory());
                     String[] children = f.list();
                     assertEquals(0, children.length);
                     
                     String parts[] = name.split("-");
                     assertEquals(String.valueOf(cal.get(Calendar.DATE)), parts[0]);
                     assertEquals(String.valueOf(cal.get(Calendar.MONTH) + 1), parts[1]);
                     assertEquals(String.valueOf(cal.get(Calendar.YEAR)), parts[2]);
                     assertEquals(String.valueOf(cal.get(Calendar.HOUR_OF_DAY)), parts[3]);
                     assertEquals(String.valueOf(cal.get(Calendar.MINUTE)), parts[4]);
                     int sec = Integer.parseInt(parts[5]);
                     assertTrue(sec >= 0 && sec < 60);
                     
                     assertTrue(f.delete());
                 }
             }
             
             assertTrue(found);
         }
         catch (Exception e)
         {
            fail(e.getClass().getName() + " - " + e.getMessage());
         }   
     }
     
     /**
      * Test method for {@link au.edu.uts.eng.remotelabs.rigclient.rig.control.AbstractBatchRunner#invoke()}.
      */
     @Test
     public void testInvokeEnv()
     {
         final String wdBase = System.getProperty("user.dir") + "/test/resources/BatchRunner";
         reset(this.mockConfig);
         expect(this.mockConfig.getProperty("Batch_Working_Dir"))
             .andReturn(wdBase);
         expect(this.mockConfig.getProperty("Batch_Create_Nested_Dir", "true"))
             .andReturn("false");
         expect(this.mockConfig.getProperty("Batch_Flush_Env", "false"))
             .andReturn("true");
         replay(this.mockConfig);
         
         try
         {
             Field field = AbstractBatchRunner.class.getDeclaredField("command");
             field.setAccessible(true);
             
             if (System.getProperty("os.name").equals("Windows"))
             {
                 field.set(this.runner, "set");
             }
             else
             {
                 field.set(this.runner, "env");
             }
             
             final Map<String, String> envMap = new HashMap<String, String>();
             for (int i = 1; i <= 20; i++)
             {
                 envMap.put("env" + i, "val" + i);
             }
             envMap.put("PATH", System.getenv("PATH"));
             
             field = AbstractBatchRunner.class.getDeclaredField("envMap");
             field.setAccessible(true);
             field.set(this.runner, envMap);
             
             Method meth = AbstractBatchRunner.class.getDeclaredMethod("invoke");
             meth.setAccessible(true);
             assertTrue((Boolean)meth.invoke(this.runner));
             
             String env[] = this.runner.getAllStandardOut().split(System.getProperty("line.separator"));
            
             assertEquals(21, env.length);
             Arrays.sort(env);
             for (int i = 1; i <= 20; i++)
             {
                 assertTrue(Arrays.binarySearch(env, "env" + i + "=val" + i) >= 0);
             }
         }
         catch (Exception e)
         {
            fail(e.getClass().getName() + " - " + e.getMessage());
         }   
     }
     
     /**
      * Test method for {@link au.edu.uts.eng.remotelabs.rigclient.rig.control.AbstractBatchRunner#invoke()}.
      */
     @Test
     public void testInvokeEnvWinPath()
     {
         final String wdBase = System.getProperty("user.dir") + "/test/resources/BatchRunner";
         reset(this.mockConfig);
         expect(this.mockConfig.getProperty("Batch_Working_Dir"))
             .andReturn(wdBase);
         expect(this.mockConfig.getProperty("Batch_Create_Nested_Dir", "true"))
             .andReturn("false");
         expect(this.mockConfig.getProperty("Batch_Flush_Env", "false"))
             .andReturn("true");
         replay(this.mockConfig);
         
         try
         {
             Field field = AbstractBatchRunner.class.getDeclaredField("command");
             field.setAccessible(true);
             
             if (System.getProperty("os.name").equals("Windows"))
             {
                 field.set(this.runner, "set");
             }
             else
             {
                 field.set(this.runner, "env");
             }
             
             final Map<String, String> envMap = new HashMap<String, String>();
             for (int i = 1; i <= 20; i++)
             {
                 envMap.put("env" + i, "val" + i);
             }
             
             /* Windows is annoying inconsistent with the environment variables. Some
              * use PATH, path or Path. */
             envMap.put("Path", System.getenv("PATH"));
             
             field = AbstractBatchRunner.class.getDeclaredField("envMap");
             field.setAccessible(true);
             field.set(this.runner, envMap);
             
             Method meth = AbstractBatchRunner.class.getDeclaredMethod("invoke");
             meth.setAccessible(true);
             assertTrue((Boolean)meth.invoke(this.runner));
             
             String env[] = this.runner.getAllStandardOut().split(System.getProperty("line.separator"));
             assertEquals(21, env.length);
             Arrays.sort(env);
             for (int i = 1; i <= 20; i++)
             {
                 assertTrue(Arrays.binarySearch(env, "env" + i + "=val" + i) >= 0);
             }
         }
         catch (Exception e)
         {
            fail(e.getClass().getName() + " - " + e.getMessage());
         }   
     }
     
     /**
      * Test method for {@link au.edu.uts.eng.remotelabs.rigclient.rig.control.AbstractBatchRunner#invoke()}.
      */
     @Test
     public void testInvokeNoRunDirEnvFlushFail()
     {
         final String wd = System.getProperty("user.dir") + "/test/resources/BatchRunner";
         reset(this.mockConfig);
         expect(this.mockConfig.getProperty("Batch_Working_Dir"))
             .andReturn(wd);
         expect(this.mockConfig.getProperty("Batch_Create_Nested_Dir", "true"))
             .andReturn("false").atLeastOnce();
         expect(this.mockConfig.getProperty("Batch_Flush_Env", "false"))
             .andReturn("true");
         replay(this.mockConfig);
         
         try
         {
             Field field = AbstractBatchRunner.class.getDeclaredField("command");
             field.setAccessible(true);
             
             /* Sets a command which prints out the contents of a test file. On
              * Windows uses 'type' (apparently) and on sane operating
              * systems - Linux / UNIX, uses'cat'. */
             if (System.getProperty("os.name").equals("Windows"))
             {
                 field.set(this.runner, "type");
             }
             else
             {
                 field.set(this.runner, "cat");
             }
             
             final List<String> args = new ArrayList<String>();
             args.add(wd + "/TestFileToCat.txt");
             field = AbstractBatchRunner.class.getDeclaredField("commandArgs");
             field.setAccessible(true);
             field.set(this.runner, args);
             
             Method meth = AbstractBatchRunner.class.getDeclaredMethod("invoke");
             meth.setAccessible(true);
             assertFalse((Boolean)meth.invoke(this.runner));
             
             field = AbstractBatchRunner.class.getDeclaredField("workingDirBase");
             field.setAccessible(true);
             assertEquals(wd, field.get(this.runner));
             
             field = AbstractBatchRunner.class.getDeclaredField("workingDir");
             field.setAccessible(true);
             assertEquals(wd, field.get(this.runner));
             
             assertEquals("", this.runner.getAllStandardOut());
             assertEquals("", this.runner.getAllStandardErr());
         }
         catch (Exception e)
         {
            fail(e.getClass().getName() + " - " + e.getMessage());
         }   
     }
     
     /**
      * Test method for {@link au.edu.uts.eng.remotelabs.rigclient.rig.control.AbstractBatchRunner#invoke()}.
      */
     @Test
     public void testInvokeNoRunDirNoEnvFlush()
     {
         final String wd = System.getProperty("user.dir") + "/test/resources/BatchRunner";
         reset(this.mockConfig);
         expect(this.mockConfig.getProperty("Batch_Working_Dir"))
             .andReturn(wd);
         expect(this.mockConfig.getProperty("Batch_Create_Nested_Dir", "true"))
             .andReturn("false").atLeastOnce();
         expect(this.mockConfig.getProperty("Batch_Flush_Env", "false"))
             .andReturn("false");
         replay(this.mockConfig);
         
         try
         {
             Field field = AbstractBatchRunner.class.getDeclaredField("command");
             field.setAccessible(true);
             
             /* Sets a command which prints out the contents of a test file. On
              * Windows uses 'type' (apparently) and on sane operating
              * systems - Linux / UNIX, uses'cat'. */
             if (System.getProperty("os.name").equals("Windows"))
             {
                 field.set(this.runner, "type");
             }
             else
             {
                 field.set(this.runner, "cat");
             }
             
             final List<String> args = new ArrayList<String>();
             args.add(wd + "/TestFileToCat.txt");
             field = AbstractBatchRunner.class.getDeclaredField("commandArgs");
             field.setAccessible(true);
             field.set(this.runner, args);
             
             Method meth = AbstractBatchRunner.class.getDeclaredMethod("invoke");
             meth.setAccessible(true);
             assertTrue((Boolean)meth.invoke(this.runner));
             
             field = AbstractBatchRunner.class.getDeclaredField("workingDirBase");
             field.setAccessible(true);
             assertEquals(wd, field.get(this.runner));
             
             field = AbstractBatchRunner.class.getDeclaredField("workingDir");
             field.setAccessible(true);
             assertEquals(wd, field.get(this.runner));
             
             final String out = this.runner.getAllStandardOut();
             assertEquals("", this.runner.getAllStandardErr());
             
             assertNotNull(out);
             String lines[]  = out.split("\n");
             assertEquals("Start File 1", lines[0]);
             for (int i = 1; i <= 20; i++)
             {
                 assertEquals(String.valueOf(i), lines[i]);
             }
             assertEquals("End File 1", lines[21]);
         }
         catch (Exception e)
         {
            fail(e.getClass().getName() + " - " + e.getMessage());
         }   
     }
 
     /**
      * Test method for {@link au.edu.uts.eng.remotelabs.rigclient.rig.control.AbstractBatchRunner#isStarted()}.
      */
     @Test
     public void testRun()
     {
         final String wdBase = System.getProperty("user.dir") + "/test/resources/BatchRunner";
         reset(this.mockConfig);
         expect(this.mockConfig.getProperty("Batch_Working_Dir"))
             .andReturn(wdBase);
         expect(this.mockConfig.getProperty("Batch_Create_Nested_Dir", "true"))
             .andReturn("true").atLeastOnce();
         expect(this.mockConfig.getProperty("Batch_Flush_Env", "false"))
             .andReturn("false");
         expect(this.mockConfig.getProperty("Batch_Clean_Up", "false"))
             .andReturn("true");
         expect(this.mockConfig.getProperty("Batch_Instruct_File_Delete", "true"))
             .andReturn("false");
         replay(this.mockConfig);
         
         try
         {
             Field field = AbstractBatchRunner.class.getDeclaredField("command");
             field.setAccessible(true);
             
             /* Sets a command which prints out the contents of a test file. On
              * Windows uses 'type' (apparently) and on sane operating
              * systems - Linux / UNIX, uses'cat'. */
             if (System.getProperty("os.name").equals("Windows"))
             {
                 // LATER Windows equivalent batch script
                 fail("Windows equivalent batch script not implemented");
             }
             else
             {
                 field.set(this.runner, System.getProperty("user.dir") + "/test/resources/BatchRunner/BatchRunnerTest.sh");
             }
             
             List<String> args = new ArrayList<String>();
             args.add(String.valueOf(10));
             args.add("stdout");
             args.add(String.valueOf(15));
             field = AbstractBatchRunner.class.getDeclaredField("commandArgs");
             field.setAccessible(true);
             field.set(this.runner, args);
             
             field = AbstractBatchRunner.class.getDeclaredField("fileName");
             field.setAccessible(true);
             field.set(this.runner, wdBase + "/test/resources/AbstractRig.class");
             
             Thread t = new Thread(this.runner);
             t.start();
             
             int to = 0;
             while (!this.runner.isStarted() && !this.runner.isFailed()) 
             {
                 Thread.sleep(100);
                 to++;
                 
                 /* An arbitrary time (5 seconds), but should be plenty of time
                  * for the batch process to be started. */ 
                 if (to > 50) fail("Starting batch process timed out."); 
             }
 
             assertTrue(this.runner.isStarted());
             assertTrue(this.runner.isRunning());
             assertFalse(this.runner.isFailed());
             
             Thread.sleep(1000);
             String stdout = this.runner.getBatchStandardOut();
             assertTrue(stdout.contains("Sleep loop count is 0")); // This is unreliable as it's based on timing
             assertNotNull(stdout);
             
             Thread.sleep(1000);
             stdout = this.runner.getBatchStandardOut();
             assertNotNull(stdout);          
             assertTrue(stdout.contains("Sleep loop count is 2")); // This is unreliable as it's based on correct timing
             
             to = 0;
             while (this.runner.isRunning())
             {
                 Thread.sleep(1000);
                 to++;
                 
                 /* An arbitrary time (20 seconds), but should be plenty of time
                  * for the batch process to complete. */
                 if (to > 200) fail("Starting batch process timed out."); 
             }
             
             assertTrue(this.runner.isStarted());
             assertFalse(this.runner.isRunning());
             assertFalse(this.runner.isFailed());
             assertEquals(15, this.runner.getExitCode());
             
             field = AbstractBatchRunner.class.getDeclaredField("workingDirBase");
             field.setAccessible(true);
             assertEquals(wdBase, field.get(this.runner));
             
             field = AbstractBatchRunner.class.getDeclaredField("workingDir");
             field.setAccessible(true);
             assertFalse(wdBase.equals(field.get(this.runner)));
             
             stdout = this.runner.getAllStandardOut();
             for (int i = 0; i < 10; i++)
             {
                 assertTrue(stdout.contains("Sleep loop count is " + i));
             }
             
             String stderr = this.runner.getAllStandardErr();
             assertEquals("", stderr);
             
             /* Check cleanup. */
             File[] files = new File(wdBase).listFiles();
             Calendar cal = Calendar.getInstance();
             for (File f : files)
             {
                 if (f.getName().startsWith(String.valueOf(cal.get(Calendar.DATE))))
                 {
                     fail("File " + f.getName() + " not cleaned up.");
                 }
             }
         }
         catch (Exception e)
         {
            fail(e.getClass().getName() + " - " + e.getMessage());
         }   
     }
 
     /**
      * Test method for {@link au.edu.uts.eng.remotelabs.rigclient.rig.control.AbstractBatchRunner#run()}.
      */
     @Test
     public void testRunStdErr()
     {
         final String wdBase = System.getProperty("user.dir") + "/test/resources/BatchRunner";
         reset(this.mockConfig);
         expect(this.mockConfig.getProperty("Batch_Working_Dir"))
             .andReturn(wdBase);
         expect(this.mockConfig.getProperty("Batch_Create_Nested_Dir", "true"))
             .andReturn("true").atLeastOnce();
         expect(this.mockConfig.getProperty("Batch_Flush_Env", "false"))
             .andReturn("false");
         expect(this.mockConfig.getProperty("Batch_Clean_Up", "false"))
             .andReturn("true");
         expect(this.mockConfig.getProperty("Batch_Instruct_File_Delete", "true"))
         .andReturn("false");
         replay(this.mockConfig);
         
         try
         {
             Field field = AbstractBatchRunner.class.getDeclaredField("command");
             field.setAccessible(true);
             
             /* Sets a command which prints out the contents of a test file. On
              * Windows uses 'type' (apparently) and on sane operating
              * systems - Linux / UNIX, uses'cat'. */
             if (System.getProperty("os.name").equals("Windows"))
             {
                 // LATER Windows equivalent batch script
                 fail("Windows equivalent batch script not implemented");
             }
             else
             {
                 field.set(this.runner, System.getProperty("user.dir") + "/test/resources/BatchRunner/BatchRunnerTest.sh");
             }
             
             List<String> args = new ArrayList<String>();
             args.add(String.valueOf(10));
             args.add("stderr");
             args.add(String.valueOf(15));
             field = AbstractBatchRunner.class.getDeclaredField("commandArgs");
             field.setAccessible(true);
             field.set(this.runner, args);
             
             field = AbstractBatchRunner.class.getDeclaredField("fileName");
             field.setAccessible(true);
             field.set(this.runner, wdBase + "/tests/resources/BatchRunner/AbstractRig.class");
             
             Thread t = new Thread(this.runner);
             t.start();
             
             int to = 0;
             while (!this.runner.isStarted() && !this.runner.isFailed()) 
             {
                 Thread.sleep(100);
                 to++;
                 
                 /* An arbitrary time (5 seconds), but should be plenty of time
                  * for the batch process to be started. */ 
                 if (to > 50) fail("Starting batch process timed out."); 
             }
 
             assertTrue(this.runner.isStarted());
             assertTrue(this.runner.isRunning());
             assertFalse(this.runner.isFailed());
             
             Thread.sleep(1000);
             String stderr = this.runner.getBatchStandardError();
             assertTrue(stderr.contains("Sleep loop count is 0")); // This is unreliable as it's based on timing
             assertNotNull(stderr);
             
             Thread.sleep(1000);
             stderr = this.runner.getBatchStandardError();
             assertNotNull(stderr);          
             assertTrue(stderr.contains("Sleep loop count is 2")); // This is unreliable as it's based on correct timing
             
             to = 0;
             while (this.runner.isRunning())
             {
                 Thread.sleep(1000);
                 to++;
                 
                 /* An arbitrary time (20 seconds), but should be plenty of time
                  * for the batch process to complete. */
                 if (to > 200) fail("Starting batch process timed out."); 
             }
             
             assertTrue(this.runner.isStarted());
             assertFalse(this.runner.isRunning());
             assertFalse(this.runner.isFailed());
             assertEquals(15, this.runner.getExitCode());
             
             field = AbstractBatchRunner.class.getDeclaredField("workingDirBase");
             field.setAccessible(true);
             assertEquals(wdBase, field.get(this.runner));
             
             field = AbstractBatchRunner.class.getDeclaredField("workingDir");
             field.setAccessible(true);
             assertFalse(wdBase.equals(field.get(this.runner)));
             
             stderr = this.runner.getAllStandardErr();
             for (int i = 0; i < 10; i++)
             {
                 assertTrue(stderr.contains("Sleep loop count is " + i));
             }
             
             String stdout = this.runner.getAllStandardOut();
             assertEquals("", stdout);
             
             /* Check cleanup. */
             File[] files = new File(wdBase).listFiles();
             Calendar cal = Calendar.getInstance();
             for (File f : files)
             {
                 if (f.getName().startsWith(String.valueOf(cal.get(Calendar.DATE))))
                 {
                     fail("File " + f.getName() + " not cleaned up.");
                 }
             }
         }
         catch (Exception e)
         {
            fail(e.getClass().getName() + " - " + e.getMessage());
         }   
     }
 
     /**
      * Test method for {@link au.edu.uts.eng.remotelabs.rigclient.rig.control.AbstractBatchRunner#getResultsFiles()}.
      */
     @Test
     public void testGetResultsFiles()
     {
         
         try
         {
             String wdBase = System.getProperty("user.dir") + "/test/resources/BatchRunner";
             String wd = wdBase + "/ResultsFiles";
             
             Field wdBaseField = AbstractBatchRunner.class.getDeclaredField("workingDirBase");
             wdBaseField.setAccessible(true);
             wdBaseField.set(this.runner, wdBase);
             
             Field wdField = AbstractBatchRunner.class.getDeclaredField("workingDir");
             wdField.setAccessible(true);
             wdField.set(this.runner, wd);
             
             Method meth = AbstractBatchRunner.class.getDeclaredMethod("detectResultsFiles");
             meth.setAccessible(true);
             meth.invoke(this.runner);
             
             List<String> results = this.runner.getResultsFiles();
             assertTrue(results.size() > 3); // There is at least a .svn directory also in this directory
             assertTrue(results.contains("a.txt"));
             assertTrue(results.contains("b.txt"));
             assertTrue(results.contains("c.txt"));
         }
         catch (Exception e)
         {
             fail(e.getClass().getName() + " - " + e.getMessage());
         }
     }
     
     /**
      * Test method for {@link au.edu.uts.eng.remotelabs.rigclient.rig.control.AbstractBatchRunner#cleanup()}.
      * This is a highly risky test due to the risky nature of the tested 
      * operation (i.e. don't fraking symlink to somewhere dangerous). 
      */
     @Test
     public void testCleanup()
     {
         try
         {
             final String wdBase = System.getProperty("user.dir") + "/test/resources/BatchRunner";
             final String wd = wdBase + "/To_Be_Deleted";
             final String tDir = "/testdirectory";
             final String tFile = "/testfile";
             
             /* Create directory to be deleted. */
             assertTrue(new File(wd).mkdir());
             
             /* Create directory in directory to be deleted. */
             assertTrue(new File(wd + tDir).mkdir());
             
             /* Create files in directories to be deleted. */
             for (int i = 1; i <= 10; i++)
             {
                 assertTrue(new File(wd + tFile + i).createNewFile());
                 assertTrue(new File(wd + tDir + tFile + i).createNewFile());
             }
             
             Field wdBaseField = AbstractBatchRunner.class.getDeclaredField("workingDirBase");
             wdBaseField.setAccessible(true);
             wdBaseField.set(this.runner, wdBase);
             
             Field wdField = AbstractBatchRunner.class.getDeclaredField("workingDir");
             wdField.setAccessible(true);
             wdField.set(this.runner, wd);
             
             Field batchFile = AbstractBatchRunner.class.getDeclaredField("fileName");
             batchFile.setAccessible(true);
             batchFile.set(this.runner, wdBase + "/test/resources/BatchRunner/AbstractRig.class");
             
             reset(this.mockConfig);
             expect(this.mockConfig.getProperty("Batch_Clean_Up", "false"))
                 .andReturn("true"); // True will run recursive delete
             expect(this.mockConfig.getProperty("Batch_Instruct_File_Delete", "true"))
                 .andReturn("false");
             replay(this.mockConfig);
             
             Method meth = AbstractBatchRunner.class.getDeclaredMethod("cleanup");
             meth.setAccessible(true);
             meth.invoke(this.runner); 
             
             assertFalse(new File(wd).exists());
             assertFalse(new File(wd + tDir).exists());
             for (int i = 1; i <= 10; i++)
             {
                 assertFalse(new File(wd + tFile + i).exists());
                 assertFalse(new File(wd + tDir + tFile + i).exists());
             }
             
             verify(this.mockConfig); 
         }
         catch (Exception e)
         {
             fail(e.getClass().getName() + " - " + e.getMessage());
         }
     }
 
     
     /**
      * Test method for {@link au.edu.uts.eng.remotelabs.rigclient.rig.control.AbstractBatchRunner#cleanup()}.
      * This is a highly risky test due to the risky nature of the tested 
      * operation (i.e. don't fraking symlink to somewhere dangerous). 
      */
     @Test
     public void testCleanupConfigOff()
     {
         try
         {
             final String wdBase = System.getProperty("user.dir") + "/test/resources/BatchRunner";
             final String wd = wdBase + "/To_Not_Be_Deleted";
        
             /* Create directory to be deleted. */
             assertTrue(new File(wd).mkdir());
             
             Field wdBaseField = AbstractBatchRunner.class.getDeclaredField("workingDirBase");
             wdBaseField.setAccessible(true);
             wdBaseField.set(this.runner, wdBase);
             
             Field wdField = AbstractBatchRunner.class.getDeclaredField("workingDir");
             wdField.setAccessible(true);
             wdField.set(this.runner, wd);
             
             Field batchFile = AbstractBatchRunner.class.getDeclaredField("fileName");
             batchFile.setAccessible(true);
             batchFile.set(this.runner, wdBase + "/test/resources/BatchRunner/AbstractRig.class");
             
             reset(this.mockConfig);
             expect(this.mockConfig.getProperty("Batch_Clean_Up", "false"))
                 .andReturn("false");
             expect(this.mockConfig.getProperty("Batch_Instruct_File_Delete", "true"))
                 .andReturn("false");
             replay(this.mockConfig);
             
             Method meth = AbstractBatchRunner.class.getDeclaredMethod("cleanup");
             meth.setAccessible(true);
             meth.invoke(this.runner); 
             
             assertTrue(new File(wd).exists());
             assertTrue(new File(wd).delete());
             
             verify(this.mockConfig); 
         }
         catch (Exception e)
         {
             fail(e.getClass().getName() + " - " + e.getMessage());
         }
     }
     
     /**
      * Test method for {@link au.edu.uts.eng.remotelabs.rigclient.rig.control.AbstractBatchRunner#getTimeStamp(char, char, char)}.
      */
     @Test
     public void testGetTimeStamp()
     {
         try
         {
             Method meth = AbstractBatchRunner.class.getDeclaredMethod("getTimeStamp", char.class, char.class, char.class);
             meth.setAccessible(true);
             String ts = (String) meth.invoke(this.runner, '-', '-', '-');
             String[] parts = ts.split("-");
             
             /* Format should be day - month - year - hour - minute - second. */
             Calendar cal = Calendar.getInstance();
             assertEquals(cal.get(Calendar.DAY_OF_MONTH), Integer.parseInt(parts[0]));
             assertEquals(cal.get(Calendar.MONTH) + 1, Integer.parseInt(parts[1]));
             assertEquals(cal.get(Calendar.YEAR), Integer.parseInt(parts[2]));
             assertEquals(cal.get(Calendar.HOUR_OF_DAY), Integer.parseInt(parts[3]));
             assertEquals(cal.get(Calendar.MINUTE), Integer.parseInt(parts[4]));
             assertTrue(Integer.parseInt(parts[5]) >= 0 && Integer.parseInt(parts[5]) < 60);
         }
         catch (Exception e)
         {
             fail(e.getClass().getName() + " - " + e.getMessage());
         }
     }
     
     /**
      * Test method for {@link au.edu.uts.eng.remotelabs.rigclient.rig.control.AbstractBatchRunner#getBatchUser()}
      */
     @Test
     public void testGetBatchUser()
     {
         AbstractBatchRunner r = new MockBatchRunner("", "tuser");
         assertEquals("tuser", r.getBatchUser());
     }
 
 }
