 package com.lohika.itkachuk.javatc.lesson4.client;
 
 import java.io.*;
 import java.net.Socket;
 import java.util.Arrays;
 
 import com.lohika.itkachuk.javatc.lesson4.server.FileTransferServer;
 
 /**
  * Created with IntelliJ IDEA.
  * User: itkachuk
  * Date: 3/18/13 3:30 PM
  */
 public class FileTransferClient {
 
     private String host;
     private int port;
     private String filePath;
     private String targetPath;
 
     public FileTransferClient(String host, int port, String filePath, String targetPath) {
         this.host = host;
         this.port = port;
         this.filePath = filePath;
         this.targetPath = targetPath;
     }
 
     private void validateInputs() throws IllegalArgumentException {
        if (isEmpty(host)) {
             throw new IllegalArgumentException("Input error: server host name can not be empty");
         }
         if (port < 1 || port > 65535) {
             throw new IllegalArgumentException("Input error: server port should be from range 1-65535");
         }
 
        if (isEmpty(filePath)) {
             throw new IllegalArgumentException("Input error: file path can not be empty");
         }
         if (filePath.length() > 260) {
             throw new IllegalArgumentException("Input error: the total file path length can not be more than 260 characters");
         }
 
         File fileToSend = new File(filePath);
         if (!fileToSend.isFile()) {
             throw new IllegalArgumentException("Input error: \"" + filePath + "\" is not a file");
         }
        if (isNotEmpty(targetPath)) {
             if ((targetPath.length() + fileToSend.getName().length()) > 260) {
                 throw new IllegalArgumentException("Input error: the total path length of target file can not be more than 260 characters");
             }
         }
     }
 
    // Copied source from Apache Commons lib, since we don't want to have any dependencies for this simple class
    private boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    private boolean isNotEmpty(CharSequence cs) {
        return !isEmpty(cs);
    }
    //-------------------------------------------

     public void uploadFile() throws IOException {
         File file = new File(filePath);
         ObjectInputStream ois = null;
         ObjectOutputStream oos = null;
         try {
             Socket socket = new Socket(host, port);
             ois = new ObjectInputStream(socket.getInputStream());
             oos = new ObjectOutputStream(socket.getOutputStream());
 
             oos.writeObject(file.getName());
            if (isNotEmpty(targetPath)) {
                 oos.writeObject(targetPath);
             } else {
                 oos.writeObject(FileTransferServer.DEFAULT_TARGET_PATH);
             }
 
             FileInputStream fis = new FileInputStream(file);
             byte [] buffer = new byte[FileTransferServer.BUFFER_SIZE];
             Integer bytesRead;
 
             while ((bytesRead = fis.read(buffer)) > 0) {
                 oos.writeObject(bytesRead);
                 oos.writeObject(Arrays.copyOf(buffer, buffer.length));
             }
         } finally {
             try {
                 oos.close();
                 ois.close();
             } catch (Exception e) {}
         }
     }
 
     public static void main(String[] args) throws Exception {
 
         if (args.length != 3 && args.length != 4) {
            System.out.println("Usage: java FileTransferClient <host> <port> <file path> [<target path>]");
             System.exit(1);
         }
 
         FileTransferClient fileTransferClient;
         try {
             if (args.length == 3)
                 fileTransferClient = new FileTransferClient(args[0], Integer.valueOf(args[1]), args[2], null);
             else
                 fileTransferClient = new FileTransferClient(args[0], Integer.valueOf(args[1]), args[2], args[3]);
 
             fileTransferClient.validateInputs();
 
             fileTransferClient.uploadFile();
 
         } catch (NumberFormatException nfe) {
             System.out.println("One or more input arguments are not valid:\n\t port is not a valid integer number");
         } catch (IllegalArgumentException iae) {
             System.out.println("One or more input arguments are not valid:\n\t" + iae.getMessage());
         }
     }
 }
