 package it.com.atlassian.jira.ext.commitacceptance;
 
 public class TestCvsCommitAcceptanceWithPythonClient extends AbstractCvsCommitAcceptanceTest {
 
     protected String getCommitHookScriptPath() {
         return "client/python/cvs/verifymsg";
     }
 
     protected String getPathToCommitHookImpl() {
         return "client/python/cvs/" + getCommitHookBaseName();
     }
 
     protected String getCommitHookBaseName() {
         return "jira-client.py";
     }

    public void testCommitIssueWithDifferentAssignee() {

    }

    public void testCommit() {

    }

    public void testCommmitWithNoIssueKey() {

    }

    public void testCommmitWithInvalidIssueKey() {

    }

    public void testCommitToResolvedIssue() {
        
    }
 }
