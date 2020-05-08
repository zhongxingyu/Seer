 package com.tshap88.chat;
 
 import java.net.*;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

 
 public class Client {
     public static void main(String[] args) {
         Socket socket;
         try {
             System.out.println("Сlient starting.");
             socket = new Socket(ConfFile.getADDRESS(), ConfFile.getPORT());
             // System.out.println("Сonnection to the server:" + socket);
             Thread threadIn = new Thread(new InputThread(socket));
             Thread threadOut = new Thread(new OutputThread(socket));
             threadIn.start();
             threadOut.start();
 
            // you create new thread here. Note you can use
            // thread pool: final ExecutorService executorService = Executors.newFixedThreadPool(2);
            // and submit your Runnable there


         } catch (NullPointerException e) {
             System.out.println("Connection with server lost.");
         }catch (Exception x) {
             x.printStackTrace();
         }
     }
 }
