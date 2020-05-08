 package ru.spbau.cheque.server.reciever;
 
 public class Main {
     public static void main(String[] argv){
         try{
             ChequeRecognitionServer srv = new ChequeRecognitionServer(3843);
             srv.start();
         } catch (NoConnectionException e){
            System.out.println("Can't establish connection.");
         }
     }
 }
