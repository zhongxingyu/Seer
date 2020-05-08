 package com.llnw.storage.client;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Lists;
 
 import com.llnw.storage.client.io.ActivityCallback;
 import com.llnw.storage.client.io.HeartbeatInputStream;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.net.ftp.FTP;
 import org.apache.commons.net.ftp.FTPClient;
 import org.apache.commons.net.ftp.FTPConnectionClosedException;
 import org.apache.commons.net.ftp.FTPFile;
 import org.apache.commons.net.ftp.FTPReply;
 import org.joda.time.Duration;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.annotation.Nullable;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Arrays;
 import java.util.List;
 
 public class EndpointFTP implements Endpoint {
     private static final Logger log = LoggerFactory.getLogger(EndpointFTP.class);
 
     private static final int THIRTY_SECONDS_MILLIS = (int)Duration.standardSeconds(30).getMillis();
 
     private final FTPClient client = new FTPClient();
     private final String host;
     private final int port;
     private final String username;
     private final String password;
 
 
     public EndpointFTP(String host, String username, String password) {
         this(host, FTPClient.DEFAULT_PORT, username, password);
     }
 
 
     public EndpointFTP(String host, int port, String username, String password) {
         this.host = host;
         this.port = port;
         this.username = username;
         this.password = password;
     }
 
 
     @Override
     public void deleteDirectory(String path) throws IOException {
         ensureConnected();
 
         if (!client.removeDirectory(path)) {
             throw new EndpointException("Couldn't delete " + path + ": " + client.getReplyCode());
         }
     }
 
 
     @Override
     public void deleteFile(String path) throws IOException {
         ensureConnected();
 
         if (!client.deleteFile(path)) {
             throw new EndpointException("Couldn't delete " + path + ": " + client.getReplyCode());
         }
     }
 
 
     @Override
     public void close() throws IOException {
         if (client.isConnected()) {
             try {
                 client.logout();
             } catch (FTPConnectionClosedException e) {
                 log.warn("Connection closed prematurely", e);
             } finally {
                 client.disconnect();
             }
         }
     }
 
 
     @Override
     public void makeDirectory(String path) throws IOException {
         ensureConnected();
         final String starting = client.printWorkingDirectory();
 
 
         if ('/' == path.charAt(0)) { // explicit full path
             if (!client.changeWorkingDirectory("/")) {
                 throw new EndpointException("Couldn't chdir to '/' ");
             }
         }
 
         // TODO nbeaudrot 2013-01-24 we should really do some funky "longest shared path from root" if
         // we have an explicit path ("/foo/bar" as opposed to "foo/bar") since we might be on
         // an FTP server administrated by some joker who won't let us chdir to /
 
         try {
             final StringBuilder current = new StringBuilder("/");
             final String[] dirs = path.split("/");
 
             for (String dir : dirs) {
                 if (dir.isEmpty())
                     continue;
 
                 if(!client.changeWorkingDirectory(dir)) {
                     client.makeDirectory(dir);
                     if (!client.changeWorkingDirectory(dir)) {
                        throw new EndpointException("Couldn't make directory: " + dir + " current path " + current);
                     } else {
                         current.append(dir);
                         current.append("/");
                     }
                 }
             }
         } finally {
             client.changeWorkingDirectory(starting);
         }
     }
 
 
     @Override
     public List<String> listFiles(String path) throws IOException {
         ensureConnected();
 
         final FTPFile[] files = client.listFiles(path);
 
         if (files == null)
             return Lists.newArrayList();
 
         return Lists.newArrayList(Lists.transform(Arrays.asList(files), new Function<FTPFile, String>() {
             @Override
             public String apply(@Nullable FTPFile ftpFile) {
                 return ftpFile == null ? "" : ftpFile.getName();
             }
         }));
     }
 
 
     @Override
     public boolean exists(String path) throws IOException {
         return !listFiles(path).isEmpty();
     }
 
 
     @Override
     public void noop() throws IOException {
         ensureConnected(); // Uses noop to verify connection
         client.noop();
     }
 
 
     @Override
     public void upload(File file, String path, String name, @Nullable ActivityCallback callback) throws IOException {
         ensureConnected();
 
         InputStream is = null;
         try {
             is = new HeartbeatInputStream(file, callback);
 
             if (!client.storeFile(path + "/" + name, is)) {
                 throw new EndpointException("Couldn't store " + name + " on the server: " + client.getReplyCode());
             }
         } finally {
             IOUtils.closeQuietly(is);
         }
     }
 
 
     private void ensureConnected() throws IOException {
         try {
             if (client.isConnected() && client.sendNoOp()) return;
         } catch (FTPConnectionClosedException e) {
             // Oh, we're closed, OK :)
         }
 
         // Set timeouts
         client.setConnectTimeout(THIRTY_SECONDS_MILLIS);
         client.setDataTimeout(THIRTY_SECONDS_MILLIS);
         client.setControlKeepAliveTimeout(120);
 
         client.connect(host, port);
         // Set SO_TIMEOUT _AFTER_ connect because ??
         client.setSoTimeout(THIRTY_SECONDS_MILLIS);
 
         if (FTPReply.isPositiveCompletion(client.getReplyCode())) {
             if (client.login(username, password)) {
                 client.enterLocalPassiveMode();
                 client.setFileType(FTP.BINARY_FILE_TYPE);
             } else {
                 client.disconnect();
                 throw new EndpointException("Couldn't log into FTP server");
             }
         } else {
             client.disconnect();
             throw new EndpointException("Couldn't connect to FTP server: " + host + ":" + port);
         }
     }
 }
