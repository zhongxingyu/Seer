 package dblike.api;
 
 
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */ 
 import dblike.service.FileInfo;
 import java.rmi.*;
 
 /**
  *
  * @author wenhanwu
  */
 public interface ClientAPI extends Remote {
  
     public void showMessage(String mmm) throws RemoteException;
     public boolean beatFromServer(String serverIP, int port) throws RemoteException;
     
     public FileInfo getFileInfoFromServer(String serverIP, int port, String userName, String directory, String fileName) throws RemoteException;
     
     public void setFileInfoToServer(String serverIP, int port, String userName, String directory, String fileName, FileInfo fileInfo) throws RemoteException;
    
    public FileInfo getFileInfoFromClient(String serverIP, int port, String userName, String directory, String fileName) throws RemoteException;
    
    public void setFileInfoToClient(String serverIP, int port, String userName, String directory, String fileName, FileInfo fileInfo) throws RemoteException;
 }
