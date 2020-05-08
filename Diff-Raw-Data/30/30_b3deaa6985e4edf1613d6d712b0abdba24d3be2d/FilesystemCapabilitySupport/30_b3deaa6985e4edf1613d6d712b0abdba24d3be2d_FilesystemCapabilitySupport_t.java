 package com.redshape.net.ssh.connection.capabilities;
 
 import com.redshape.net.connection.ConnectionException;
 import com.redshape.net.connection.capabilities.IFilesystemCapabilitySupport;
 import com.redshape.net.ssh.connection.SshConnectionSupport;
 import com.redshape.utils.Commons;
 import com.redshape.utils.SimpleStringUtils;
 import net.schmizz.sshj.sftp.*;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * @author nikelin
  * @date 15:49
  */
 public class FilesystemCapabilitySupport implements IFilesystemCapabilitySupport {
    private static final String SEPARATOR = "/";
    private static final char SEPARATOR_CHAR = '/';
    
     public class Node implements IFilesystemCapabilitySupport.Node {
         private SFTPClient client;
         private RemoteFile file;
         private String[] nameParts;
         private String parentPath;
 
         public Node( SFTPClient client, RemoteFile file ) {
             this.client = client;
             this.file = file;
             this.nameParts = this.getFile().getPath().split("\\\\/");
             this.parentPath = SimpleStringUtils.join(
                     Arrays.asList(this.nameParts)
                             .subList(
                                     0, this.nameParts.length == 1 ? 1 : this.nameParts.length - 1
                             ),
                     "/");
         }
 
         protected SFTPClient getSftpClient() {
             return this.client;
         }
 
         @Override
         public String getName() {
             return this.nameParts[this.nameParts.length - 1];
         }
 
         @Override
         public InputStream getInputStream() {
             return this.getFile().getInputStream();
         }
 
         @Override
         public OutputStream getOutputStream() {
             return this.getFile().getOutputStream();
         }
 
         @Override
         public String getCanonicalPath() {
             return this.getFile().getPath();
         }
 
         @Override
         public boolean isExists() throws ConnectionException {
             try {
                 return !this.getSftpClient().type( this.getCanonicalPath() )
                         .equals( FileMode.Type.UNKNOWN );
             } catch ( IOException e ) {
                 throw new ConnectionException( e.getMessage(), e );
             }
         }
 
         @Override
         public boolean isDirectory() throws ConnectionException {
             try {
                 return this.getSftpClient().type( this.getCanonicalPath() ).equals( FileMode.Type.DIRECTORY );
             } catch ( IOException e ) {
                 throw new ConnectionException( e.getMessage(), e );
             }
         }
 
         @Override
         public void mkdir() throws ConnectionException {
             try {
                 if ( this.isExists() ) {
                     this.delete();
                 }
     
                 this.getSftpClient().mkdir( this.getCanonicalPath() );
             } catch ( IOException e ) {
                 throw new ConnectionException( e.getMessage(), e );
             }
         }
 
         @Override
         public boolean isFile() throws ConnectionException {
             try {
                 return this.getSftpClient().type( this.getCanonicalPath() ).equals( FileMode.Type.REGULAR );
             } catch ( IOException e ) {
                 throw new ConnectionException( e.getMessage(), e );
             }
         }
 
         @Override
         public String[] list() throws ConnectionException {
             try {
                 Collection<String> result = new HashSet<String>();
                 for ( RemoteResourceInfo info : this.getSftpClient().ls( this.getCanonicalPath() ) ) {
                     result.add( info.getName() );
                 }
     
                 return result.toArray( new String[ result.size() ] );
             } catch ( IOException e ) {
                 throw new ConnectionException( e.getMessage(), e );
             }
         }
 
         @Override
         public String getParent() {
             return this.parentPath;
         }
 
         @Override
         public void delete() throws ConnectionException {
             try {
                 this.getSftpClient().rm( this.getCanonicalPath() );
             } catch ( IOException e ) {
                 throw new ConnectionException( e.getMessage(), e );
             }
         }
 
         @Override
         public void createNew() throws ConnectionException {
             try {
                 this.setFile( this.getSftpClient().open( this.getCanonicalPath(), this.createAttributesSet(OpenMode.CREAT, OpenMode.WRITE) ) );
             } catch ( IOException e ) {
                 throw new ConnectionException( e.getMessage(), e );
             }
         }
 
         protected void setFile( RemoteFile file ) {
             this.file = file;
             this.nameParts = this.getFile().getPath().split("/");
             this.parentPath = SimpleStringUtils.join(
                     Arrays.asList(this.nameParts)
                             .subList(
                                     0, this.nameParts.length == 1 ? 1 : this.nameParts.length - 1
                             ),
                     "/");
         }
 
         protected RemoteFile getFile() {
             return this.file;
         }
 
 
         protected Set<OpenMode> createAttributesSet( OpenMode... modes ) {
             HashSet<OpenMode> result = new HashSet();
             for ( OpenMode mode : modes ) {
                 result.add(mode);
             }
 
             return result;
         }
 
     }
 
     private SFTPClient sftpClient;
     private SshConnectionSupport connection;
 
     public FilesystemCapabilitySupport( SshConnectionSupport connection ) {
         Commons.checkNotNull(connection);
 
         this.connection = connection;
     }
 
    @Override
    public String getSeparator() {
        return SEPARATOR;
    }

    @Override
    public char getSeparatorChar() {
        return SEPARATOR_CHAR;
    }

     protected SshConnectionSupport getConnection() {
         return this.connection;
     }
     
     protected SFTPClient getSftpClient() throws ConnectionException {
         try {
             if ( this.sftpClient == null ) {
                 this.sftpClient = connection.asRawConnection().newSFTPClient();
             }
             
             return this.sftpClient;
         } catch ( IOException e ) {
             throw new ConnectionException( e.getMessage(), e );
         }
     }
 
     @Override
     public Node getRoot() throws ConnectionException {
         return this.findFile("/");
     }
 
     @Override
     public Node createFile(String path) throws ConnectionException {
         try {
             return new Node( this.getSftpClient(), this.getSftpClient().open(path,
                             Commons.set(OpenMode.CREAT, OpenMode.WRITE) ) );
         } catch ( SFTPException e ) {
             throw new ConnectionException( e.getMessage(), e );
         } catch ( IOException e ) {
             throw new ConnectionException( e.getMessage(), e );
         }
     }
 
     @Override
     public Node findFile(String name) throws ConnectionException {
         try {
             return new Node( this.getSftpClient(), this.getSftpClient().open(name) );
         } catch ( IOException e ) {
             throw new ConnectionException( e.getMessage(), e );
         }
     }
 
     @Override
     public boolean isAvailable() {
         return true;
     }
 
 }
