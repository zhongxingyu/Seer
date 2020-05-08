 package uk.org.sappho.code.heatmap.issues;
 
 public interface IssueManagement {
 
    public void init() throws IssueManagementException;

     public IssueWrapper getIssue(String commitComment);
 }
