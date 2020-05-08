 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package org.javavfs.impl.sftp;
 
 import ch.ethz.ssh2.Connection;
 import ch.ethz.ssh2.ConnectionInfo;
 import ch.ethz.ssh2.SFTPv3Client;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import org.javavfs.Directory;
 import org.javavfs.FileSystem;
 import org.javavfs.Node;
 import org.javavfs.Path;
 
 /**
  *
  * @author krog
  */
 public class SftpFileSystem implements FileSystem {
     public SftpFileSystem(String host, int port, String user, String password) throws IOException {
        this(host,port,user,password,60000);
    }

    public SftpFileSystem(String host, int port, String user, String password, int timeout) throws IOException {
         //Add info to infomap
         infomap.put(FileSystem.FSInfo_Name, "SftpFS");
         infomap.put(FileSystem.FSInfo_Description, "A filesystem based upon Sftp.");
         infomap.put(FileSystem.FSInfo_Version, "0.1.0");
 
         //Java version < 1.6
         infomap.put(FileSystem.FSInfo_HasFreeSpaceInformation, "false");
         infomap.put(FileSystem.FSInfo_HasSizeInformation, "false");
 
         Connection sshcon = new Connection(host,port);
        ConnectionInfo ci = sshcon.connect(null, timeout, timeout);
         if(!sshcon.authenticateWithPassword(user, password)){
             sshcon.close();
             throw new IOException("Unable to authenticate.");
         }
 
         sftpc = new SFTPv3Client(sshcon);
         
     }
 
 
     public String getName() {
         return "SftpFileSystem";
     }
 
     private HashMap infomap = new HashMap();
     protected SFTPv3Client sftpc = null;
     
     public Map getInfo() {
         return infomap;
     }
 
     public Directory getRoot() throws FileNotFoundException {
         try {
             return new SftpDirectory(this, new Path(), null);
         } catch (IOException ex) {
             throw new FileNotFoundException(ex.getMessage());
         }
     }
 
     public long getSize() {
         return 0;
     }
 
     public long getFreeSpace() {
         return 0;
     }
 
     public Node getNode(String path) throws FileNotFoundException {
         Path pathObject = new Path(path);
         Node currentNode = getRoot();
 
         for(int i=0;i<pathObject.getLevels();i++){
             if(currentNode.isDirectory()){
                 currentNode = ((Directory)currentNode).getChild(pathObject.getLevel(i));
             } else
                 throw new FileNotFoundException("The path '"+path+"' does not exist.");
         }
         return currentNode;
     }
 
     public void close() throws IOException {
         sftpc.close();
     }
 
 
 
 }
