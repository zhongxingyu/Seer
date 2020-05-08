 package uk.org.sappho.jira.soap.getparent;
 
 import com.atlassian.jira.rpc.exception.RemoteException;
 
 public interface GetParent {
 
     public String login(String username, String password) throws RemoteException;
 
     public String getParentKey(String token, String issueKey) throws RemoteException;
 }
