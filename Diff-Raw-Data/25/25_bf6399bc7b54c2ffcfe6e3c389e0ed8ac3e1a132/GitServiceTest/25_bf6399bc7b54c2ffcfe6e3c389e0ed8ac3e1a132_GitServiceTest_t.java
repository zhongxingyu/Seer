 /*
  * $Id: $
  *
  * Copyright (c) 2009 Fujitsu Denmark
  * All rights reserved.
  */
 package dk.fujitsu.issuecheck.scm;
 
 import dk.fujitsu.issuecheck.Config;
 import dk.fujitsu.issuecheck.Shell;
 import org.junit.Assert;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.wooddog.woodstub.junit.AssertionFlow;
 import org.wooddog.woodstub.junit.StubEvent;
 import org.wooddog.woodstub.junit.StubListener;
 import org.wooddog.woodstub.junit.WoodRunner;
 import org.wooddog.woodstub.junit.annotation.Excludes;
 import org.wooddog.woodstub.junit.annotation.Stubs;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 
 /**
  * @author Claus Brøndby Reimer (dencbr) / Fujitsu Denmark a|s
  * @version $Revision: $ $Date: $
  */
 @RunWith(WoodRunner.class)
 @Stubs({Shell.class, Config.class})
 @Excludes({"org\\.junit.*", "sun\\.com.*", "net\\.sourceforge\\.cobertura\\..*"})
 public class GitServiceTest {
     private List<String> messages = new ArrayList<String>();
 
     @Test
     public void testGetMessages() {
         AssertionFlow flow;
         GitService service;
         List gitMessages;
 
 
         messages.add("49aea38 DE1345 fixed defect");
         messages.add("3ecac45 DE1346 changed something");
         messages.add("4fa97f7 DE1347 added something");
 
 
         flow = createFlow("ALL");
 
         WoodRunner.addListener(flow);
         flow.start();
 
         service = new GitService();
        gitMessages = service.getMessages("old", "new", "refs");
 
         Assert.assertArrayEquals(messages.toArray(), gitMessages.toArray());
         Assert.assertTrue(flow.isComplete());
     }
 
     @Test (expected=RuntimeException.class)
     public void testInvalidArguments() {
         GitService service;
 
         service = new GitService();
         service.getMessages(new String[]{});
     }
 
     @Test
     public void testShellFailure() {
         GitService service;
         AssertionFlow flow;
 
         flow = createFlow("UNTIL EXECUTE");
         flow.add(new StubListener() {
             @Override
             public void invoked(StubEvent stubEvent) {
                 stubEvent.raiseException(new IOException("command not found"));
             }
         });
 
         WoodRunner.addListener(flow);
         flow.start();
 
         service = new GitService();
         try {
            service.getMessages("old", "new", "refs");
         } catch (RuntimeException x) {
             Assert.assertTrue(flow.isComplete());
             Assert.assertEquals("unable to execute git, command not found", x.getMessage());
             return;
         }
 
         Assert.fail("GitService failed detecting shell error");
     }
 
     @Test
     public void testGitFailure() {
         GitService service;
         AssertionFlow flow;
 
         flow = createFlow("AFTER EXECUTE");
         flow.add(new StubListener() {
             @Override
             public void invoked(StubEvent stubEvent) {
                 Assert.assertEquals("getExitValue", stubEvent.getMethodName());
 
                 stubEvent.setResult(1);
             }
         });
 
         flow.add(new StubListener() {
             @Override
             public void invoked(StubEvent stubEvent) {
                 Assert.assertEquals("getStandard", stubEvent.getMethodName());
 
                 stubEvent.setResult(new StringBuffer("stdout"));
             }
         });
 
         flow.add(new StubListener() {
             @Override
             public void invoked(StubEvent stubEvent) {
                 Assert.assertEquals("getError", stubEvent.getMethodName());
 
                 stubEvent.setResult(new StringBuffer("error"));
             }
         });
 
 
         WoodRunner.addListener(flow);
         flow.start();
 
         service = new GitService();
         try {
            service.getMessages("old", "new", "refs");
         } catch (RuntimeException x) {
             Assert.assertTrue(flow.isComplete());
            Assert.assertEquals("unable to retrieve git log for package [old, new, refs] - stdout: stdout errout: error", x.getMessage());
             return;
         }
 
         Assert.fail("GitService failed detecting shell error");
     }
 
     private AssertionFlow createFlow(String name) {
         AssertionFlow flow;
 
         flow = new AssertionFlow();
 
         flow.add(new StubListener() {
             @Override
             public void invoked(StubEvent stubEvent) {
                 Assert.assertEquals("dk.fujitsu.issuecheck.Shell", stubEvent.getMethodName());
             }
         });
 
         flow.add(new StubListener() {
             @Override
             public void invoked(StubEvent stubEvent) {
                 Assert.assertEquals("get", stubEvent.getMethodName());
                 Assert.assertEquals("scm.git.bin", stubEvent.getObject(1));
 
                 stubEvent.setResult("bin");
             }
         });
 
         if ("UNTIL EXECUTE".equals(name)) {
             return flow;
         }
 
         flow.add(new StubListener() {
             @Override
             public void invoked(StubEvent stubEvent) {
                 Assert.assertEquals("execute", stubEvent.getMethodName());
 
                 Assert.assertEquals("bin", ((String[])stubEvent.getObject(1))[0]);
                 Assert.assertEquals("log", ((String[])stubEvent.getObject(1))[1]);
                 Assert.assertEquals("--pretty=format:%s", ((String[])stubEvent.getObject(1))[2]);
                Assert.assertEquals("old..new", ((String[])stubEvent.getObject(1))[3]);
             }
         });
 
         if ("AFTER EXECUTE".equals(name)) {
             return flow;
         }
 
         flow.add(new StubListener() {
             @Override
             public void invoked(StubEvent stubEvent) {
                 Assert.assertEquals("getExitValue", stubEvent.getMethodName());
 
                 stubEvent.setResult(0);
             }
         });
 
         flow.add(new StubListener() {
             @Override
             public void invoked(StubEvent stubEvent) {
                 StringBuffer log;
 
                 Assert.assertEquals("getStandard", stubEvent.getMethodName());
 
                 log = new StringBuffer();
                 for (String message : messages) {
                     log.append(message);
                     log.append(System.getProperty("line.separator"));
                 }
 
                 stubEvent.setResult(log);
             }
         });
 
         return flow;
     }
 }
