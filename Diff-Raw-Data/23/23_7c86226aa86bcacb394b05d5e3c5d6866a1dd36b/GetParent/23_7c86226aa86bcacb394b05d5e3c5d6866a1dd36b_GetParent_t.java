 package uk.org.sappho.jira.soap.getparent;
 
import java.util.Map;

import com.atlassian.jira.issue.ModifiedValue;
 import com.atlassian.jira.rpc.exception.RemoteException;
 
 public interface GetParent {
 
     public String login(String username, String password) throws RemoteException;
 
     public String getParentKey(String token, String issueKey) throws RemoteException;

    public Map<String, ModifiedValue> getModifiedFields(String token, String issueKey) throws RemoteException;
 }
