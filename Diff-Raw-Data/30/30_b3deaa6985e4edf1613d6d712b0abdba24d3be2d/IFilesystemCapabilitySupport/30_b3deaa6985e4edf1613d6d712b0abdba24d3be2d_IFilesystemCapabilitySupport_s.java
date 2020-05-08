 package com.redshape.net.connection.capabilities;
 
 import com.redshape.net.connection.ConnectionException;
 
 import java.io.InputStream;
 import java.io.OutputStream;
 
 /**
  * @author nikelin
  * @date 16:06
  */
 public interface IFilesystemCapabilitySupport extends IServerCapabilitySupport {
 
     public interface Node {
 
         public String getName();
 
         public OutputStream getOutputStream();
         
         public InputStream getInputStream();
         
         public boolean isFile() throws ConnectionException;
 
         public boolean isDirectory() throws ConnectionException;
 
         public boolean isExists() throws ConnectionException;
 
         public String getCanonicalPath();
         
         public void createNew() throws ConnectionException;
         
         public String[] list() throws ConnectionException;
         
         public String getParent();
 
         public void delete() throws ConnectionException;
 
         public void mkdir() throws ConnectionException;
         
     }
     
     public Node getRoot() throws ConnectionException;
     
     public Node createFile( String path ) throws ConnectionException;
 
     public Node findFile(String name) throws ConnectionException;
     
 }
