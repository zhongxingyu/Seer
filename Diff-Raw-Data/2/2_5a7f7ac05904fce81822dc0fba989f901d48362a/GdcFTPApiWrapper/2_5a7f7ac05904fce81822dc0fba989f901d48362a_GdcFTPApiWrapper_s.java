 package com.gooddata.integration.ftp;
 
 import com.gooddata.integration.rest.configuration.NamePasswordConfiguration;
 import com.gooddata.integration.rest.exceptions.GdcUploadErrorException;
 import org.apache.commons.net.ftp.FTPClient;
 import org.apache.commons.net.ftp.FTPReply;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.net.SocketException;
 
 /**
  * GoodData FTP API Java wrapper
  *
  * @author zd <zd@gooddata.com>
  * @version 1.0
  */
 public class GdcFTPApiWrapper {
 
     protected static final String DEFAULT_ARCHIVE_NAME = "upload.zip";
 
     protected FTPClient client;
     protected NamePasswordConfiguration config;
 
     /**
      * Constructs the GoodData FTP API Java wrapper
      *
      * @param config NamePasswordConfiguration object with the GDC name and password configuration
      */
     public GdcFTPApiWrapper(NamePasswordConfiguration config) {
         this.config = config;
         client = new FTPClient();
     }
 
 
     /**
      * FTP transfers a local directory to the remote GDC FTP server
      * @param archiveName the name of the ZIP archive that is going to be transferred
      * @throws SocketException
      * @throws IOException
      * @throws GdcUploadErrorException
      */
     public void transferDir(String archiveName) throws SocketException, IOException, GdcUploadErrorException {
         try {
             File file = new File(archiveName);
             String dir = file.getName().split("\\.")[0];
             client.connect(config.getGdcHost());
             if (FTPReply.isPositiveCompletion(client.getReplyCode())) {
                 client.login(config.getUsername(), config.getPassword());
                 if (FTPReply.isPositiveCompletion(client.getReplyCode())) {
                     client.makeDirectory(dir);
                     if (FTPReply.isPositiveCompletion(client.getReplyCode())) {
                         client.changeWorkingDirectory(dir);
                         if (FTPReply.isPositiveCompletion(client.getReplyCode())) {
                             client.setFileType(FTPClient.BINARY_FILE_TYPE);
                             if (FTPReply.isPositiveCompletion(client.getReplyCode())) {
                                 // client.enterRemotePassiveMode();
                                 client.storeFile(file.getName(), new FileInputStream(file));
                                 if (FTPReply.isPositiveCompletion(client.getReplyCode())) {
                                     client.rename(file.getName(),DEFAULT_ARCHIVE_NAME);
                                     if (!FTPReply.isPositiveCompletion(client.getReplyCode())) {
                                         throw new GdcUploadErrorException("Can't change the file's name: server="
                                                 + config.getGdcHost() + ", file=" + file.getName() + ", " + clientReply(client));
                                     }
                                 }
                                 else
                                     throw new GdcUploadErrorException("Can't copy file to the FTP: server="
                                             + config.getGdcHost() + ", file=" + file.getName()  + ", " + clientReply(client));
                             }
                             else
                             throw new GdcUploadErrorException("Can't set the BINARY file transfer: server="
                                     + config.getGdcHost()  + ", " + clientReply(client));
                         }
                         else
                             throw new GdcUploadErrorException("Can't cd to the '"+dir+"' directory: server="
                                     + config.getGdcHost()  + ", " + clientReply(client));
                     } else
                         throw new GdcUploadErrorException("Can't create the '"+dir+"' directory: server="
                                 + config.getGdcHost()  + ", " + clientReply(client));
                     client.logout();
                 } else
                     throw new GdcUploadErrorException("Can't FTP login: server=" + config.getGdcHost()
                             + ", username=" + config.getUsername()  + ", " + clientReply(client));
             } else throw new GdcUploadErrorException("Can't FTP connect: server=" + config.getGdcHost()  + ", " + clientReply(client));
         } finally {
             if (client.isConnected()) {
                 try {
                     client.disconnect();
                 } catch (IOException ioe) {
                     // do nothing
                 }
             }
         }
     }
 
     private String clientReply(FTPClient client) {
    	return client.getReplyString() + " (code: " + client.getReplyCode();
     }
 }
