 /*
  * $Id: $
  *
  * Copyright (c) 2009 Fujitsu Denmark
  * All rights reserved.
  */
 package dk.fujitsu.issuecheck;
 
 import dk.fujitsu.issuecheck.ims.ImsService;
 import dk.fujitsu.issuecheck.scm.ScmService;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.wooddog.woodstub.junit.AssertionFlow;
 import org.wooddog.woodstub.junit.StubEvent;
 import org.wooddog.woodstub.junit.StubListener;
 import org.wooddog.woodstub.junit.WoodRunner;
 import org.wooddog.woodstub.junit.annotation.Excludes;
 import org.wooddog.woodstub.junit.annotation.Stubs;
 
 import java.util.Arrays;
 import java.util.List;
 
 
 /**
  * @author Claus Brøndby Reimer (dencbr) / Fujitsu Denmark a|s
  * @version $Revision: $ $Date: $
  */
 @RunWith(WoodRunner.class)
 @Stubs({Config.class})
 @Excludes({"org\\.junit.*", "sun\\.com.*", "net\\.sourceforge\\.cobertura\\..*"})
 public class CommitCheckTest {
     private String refs = "/refs/objects/master";
     private String oldrev = "oldrev";
     private String newrev = "newrev";
     private String issue;
     private List<String> issues;
     private boolean isIssue;
     private boolean isIssueOpen;
     private AssertionFlow flow;
 
     @Before
     public void init() {
         flow = new AssertionFlow();
 
         flow.add(new StubListener() {
             @Override
             public void invoked(StubEvent stubEvent) {
                 Assert.assertEquals("getInstance", stubEvent.getMethodName());
                 Assert.assertEquals("scm", stubEvent.getObject(1));
 
                 stubEvent.setResult(new ScmMock());
             }
         });
 
         flow.add(new StubListener() {
             @Override
             public void invoked(StubEvent stubEvent) {
                 Assert.assertEquals("getInstance", stubEvent.getMethodName());
                 Assert.assertEquals("ims", stubEvent.getObject(1));
 
                 stubEvent.setResult(new ImsMock());
             }
         });
 
         flow.add(new StubListener() {
             @Override
             public void invoked(StubEvent stubEvent) {
                 Assert.assertEquals("get", stubEvent.getMethodName());
                 Assert.assertEquals("ims.rally.issuePattern", stubEvent.getObject(1));
 
                 stubEvent.setResult("^(\\w\\w\\d+)");
             }
         });
 
         WoodRunner.addListener(flow);
         flow.start();
     }
 
     @Test
     public void testOk() {
         CommitCheck test;
 
         isIssue = true;
         isIssueOpen = true;
         issues = Arrays.asList("DE202 Hello World");
         issue = "DE202";
 
         test = new CommitCheck(refs, oldrev, newrev);
         test.execute();
 
         Assert.assertEquals(0, test.getStatus());
         Assert.assertEquals(null, test.getMessage());
 
         Assert.assertEquals(true, flow.isComplete());
     }
 
     @Test
     public void testMessageWithNoIssue() {
         CommitCheck test;
 
         isIssue = false;
         isIssueOpen = true;
         issues = Arrays.asList("Hello World");
         issue = null;
 
         test = new CommitCheck(refs, oldrev, newrev);
         test.execute();
 
         Assert.assertEquals(1, test.getStatus());
        Assert.assertEquals("commit message:\n" + issues.get(0) + "\n>> has no issue number <<", test.getMessage());
 
         Assert.assertEquals(true, flow.isComplete());
     }
 
     @Test
     public void testNoMatchingIssue() {
         CommitCheck test;
 
         isIssue = false;
         isIssueOpen = true;
         issues = Arrays.asList("DE203 Hello World");
         issue = "DE203";
 
         test = new CommitCheck(refs, oldrev, newrev);
         test.execute();
 
         Assert.assertEquals(2, test.getStatus());
        Assert.assertEquals("commit message:\n" + issues.get(0) + "\n>> contains unknown issue id \"" + issue + "\" <<", test.getMessage());
 
         Assert.assertEquals(true, flow.isComplete());
     }
 
     @Test
     public void testIssueAcceptNoCommits() {
         CommitCheck test;
 
         isIssue = true;
         isIssueOpen = false;
         issues = Arrays.asList("DE202 Hello World");
         issue = "DE202";
 
         test = new CommitCheck(refs, oldrev, newrev);
         test.execute();
 
         Assert.assertEquals(3, test.getStatus());
        Assert.assertEquals("commit message:\n" + issues.get(0) + "\n>> refers to an issue where no commits are allowed <<", test.getMessage());
 
         Assert.assertEquals(true, flow.isComplete());
 
     }
 
 
     class ScmMock implements ScmService {
 
         @Override
         public List<String> getMessages(String... arguments) {
             Assert.assertEquals(3, arguments.length);
             Assert.assertEquals(refs, arguments[0]);
             Assert.assertEquals(oldrev, arguments[1]);
             Assert.assertEquals(newrev, arguments[2]);
 
             return issues;
         }
     }
 
     class ImsMock implements ImsService {
         @Override
         public boolean isIssue(String issueId) {
             Assert.assertEquals(issue, issueId);
 
             return isIssue;
         }
 
         @Override
         public boolean isIssueOpen(String issueId) {
             Assert.assertEquals(issue, issueId);
 
             return isIssueOpen;
         }
     }
 }
