 package ru.spbau.cheque.server.reciever;
 
 import java.io.*;
 import java.net.Socket;
 
 public class Worker implements Runnable{
     private Socket mySocket;
     private static final Integer bufferSize = 1024; // bufferSize temporary hardcoded
 
     public Worker(Socket inpSocket){
         mySocket = inpSocket;
     }
     
     File recieveAndStoreImage() throws ImageSavingFailureException{
         System.out.println("New worker started.");  //Supposed to be written in log.
         byte[] buffer = new byte[bufferSize];
         try{
             File imageFile = File.createTempFile("img", ".tmp");
             InputStream is = mySocket.getInputStream();
             FileOutputStream fos = new FileOutputStream(imageFile);
             BufferedOutputStream bos = new BufferedOutputStream(fos);
             final int offset = 0;
             int bytesRead = 0;
             while (is.available() > 0){
                 bytesRead = is.read(buffer, offset, buffer.length);
                 bos.write(buffer, 0, bytesRead);
             }
             bos.close();
             mySocket.close();
             System.out.println("File successfully written."); //Supposed to be written in log.
             return imageFile;
         } catch (IOException e){
             System.err.println("Can't save image. Got IOException."); //Supposed to be written in log.
             throw new ImageSavingFailureException();
         }
     }
 
     public void sendResponse(String inpResponse){
         DataOutputStream os = null;
         try{
             os = new DataOutputStream(mySocket.getOutputStream());
             os.writeBytes(inpResponse);
             os.close();
         } catch (IOException e) {
             System.err.println("Couldn't get I/O for the connection to: hostname");
         }
     }
 
     @Override
     public void run(){
         try{
             File imageFile = recieveAndStoreImage();
             sendResponse("OK");
            mySocket.close();
         } catch (ImageSavingFailureException e){
             System.err.println("Worker cant't complete task because image can not be saved or received."); //Supposed to be written in log.
             sendResponse("ERROR");
        } catch (IOException e){
            System.err.println("Strange IOException on closing socket."); //Supposed to be written in log.
         }
     }
 }
