 package no.feide.moria.service;
 
 import java.rmi.Remote;
 import java.rmi.RemoteException;
 import java.util.HashMap;
 
 public interface AuthenticationIF extends Remote {
     
     public SessionDescriptor requestSession(String[] attributes, String prefix, String postfix) throws RemoteException;
     public SessionDescriptor requestSession(String prefix, String postfix) throws RemoteException;
    public SessionDescriptor requestSession() throws RemoteException;
     public SessionDescriptor requestUserAuthentication(String id, String username, String password) throws RemoteException;
     public String verifySession(String id) throws RemoteException;
     public UserAttribute[] getAttributes(String id) throws RemoteException;
 
 }
