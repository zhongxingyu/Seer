 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.laukvik.ftp;
 
 import com.enterprisedt.net.ftp.FTPException;
 import com.enterprisedt.net.ftp.FTPFile;
 import com.enterprisedt.net.ftp.FileTransferClient;
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Vector;
 
 /**
  *
  * @author morten
  */
 public class FtpConnection {
 
     FtpServer server;
     FileTransferClient client;
     String [] ignoreFiles;
     
     public FtpConnection( FtpServer server ) {
         this.server = server;
         this.client = new FileTransferClient();
         this.ignoreFiles = new String [] { ".", ".." }; 
     }
 
     public FtpConnection() {
         this( null );
     }
     
     public URL getURL( String path ){
         try {
            System.out.println( "FtpConnection: " + path );
            String webPath = path.replaceAll( "////" + server.path, "" );
             return new URL( server.url.getProtocol(), server.url.getHost(), server.url.getPort(), webPath );
         } catch (MalformedURLException ex) {
             ex.printStackTrace();
             return null;
         }
     }
     
     public URL getURL( FTPFile file ){
         return getURL( file.getPath() + "/" + file.getName() );
     }
 
     public void setServer(FtpServer server) {
         this.server = server;
     }
 
     public FtpServer getServer() {
         return server;
     }
     
     public void connect() throws FTPException, IOException{
         if (client.isConnected()){
         } else {
             client.setRemotePort( server.port );
             client.setRemoteHost( server.host );
             client.setUserName( server.user );
             client.setPassword( server.password ); 
             client.connect();
         }
     }
     
     public void download( FTPFile file, File downloadAs ){
         String path = file.getPath() + "/" + file.getName();
         log( "download: " + file + " => " + path ); 
         try {
             client.downloadFile( downloadAs.getAbsolutePath(), path );
             log( "downloadAs: " + downloadAs.exists() + " " + downloadAs.getAbsolutePath() );
         } catch (Exception ex) {
             ex.printStackTrace();
         }
     }
     
     public void upload( File file, FTPFile ftpFolder, String filename ) throws Exception{
         client.uploadFile( file.getAbsolutePath(), ftpFolder.getPath() + "/" + ftpFolder.getName() + "/" + filename );   
     }
     
     public void upload( File file, FTPFile ftpFile ) throws Exception{
         client.uploadFile( file.getAbsolutePath(), ftpFile.getPath() + "/" + ftpFile.getName() );   
     }
     
     public boolean isConnected(){
         return client.isConnected();
     }
     
     public FTPFile [] listFiles( String path ){
         FTPFile [] files = new FTPFile[ 0 ];
         try {
             files = client.directoryList( path );
        } catch(com.enterprisedt.net.ftp.ControlChannelIOException ccex){
            try{
                client.disconnect();
                client.connect();
                files = client.directoryList( path );
            } catch(Exception e){
                e.printStackTrace();
            }
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         Vector<FTPFile> items = new Vector<FTPFile>();
         for (FTPFile f : files){
             if (f.getName().equalsIgnoreCase(".") || f.getName().equalsIgnoreCase("..")){
             } else {
//                f.setPath( f.getPath().replaceAll("//", "/") );
                 items.add( f );
             }
         }
         FTPFile[] arr = new FTPFile[ items.size() ];
         items.toArray( arr );
         return arr;
     }
     
     private static void log( Object message ){
         System.out.println( "FtpConnection: " + message );
     }
    
 }
