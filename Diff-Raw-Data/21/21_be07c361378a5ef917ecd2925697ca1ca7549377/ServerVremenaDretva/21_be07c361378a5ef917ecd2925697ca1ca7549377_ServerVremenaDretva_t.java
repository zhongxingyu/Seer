 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package org.foi.nwtis.lurajcevi.zadaca_1;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.Socket;
 import java.util.Date;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import org.foi.nwtis.lurajcevi.konfiguracije.Konfiguracija;
 
 /**
  * 
  * @author Luka Rajcevic
  */
 public class ServerVremenaDretva extends Thread{
     
     private String setTimeRegex = "^USER ([a-zA-Z0-9_]+)\\; PASSWD ([a-zA-Z0-9_]+)\\; SETTIME (\\d\\d.\\d\\d.\\d\\d\\d\\d \\d\\d:\\d\\d:\\d\\d)\\;";
     private String adminCommandRegex = "^USER ([a-zA-Z0-9_]+)\\; PASSWD ([a-zA-Z0-9_]+)\\; (START|STOP|CLEAN|PAUSE)\\;";
     private Pattern p;
     private Matcher m;
     private boolean status;
     
     
     private Socket client;
     private String serializeFileName;
     private Konfiguracija config;
     private Dnevnik dnevnik;
 
     public ServerVremenaDretva(Socket client, String serializeFileName, Konfiguracija config) {
         this.client = client;
         this.serializeFileName = serializeFileName;
         this.config = config;
     }
 
     @Override
     public synchronized void start() {
         super.start(); 
         //System.out.println("Thread started");
     }
 
     @Override
     public void run() {
         InputStream is = null;
         OutputStream os = null;
         StringBuilder command;
         String response;
         int character;
         try {
             is = client.getInputStream();
             os = client.getOutputStream();
             command = new StringBuilder();
             
             while (true){
                 character = is.read();
                 if (character == -1 )
                     break;
                 command.append((char) character);
             }
             String strCommand = command.toString().trim();
             if (command.indexOf("GETTIME") != -1){
                 if (ServerVremena.isPaused() || ServerVremena.isStopped())
                     response = "ERROR: Server is not accepting requests at the moment.";
                 else{
                     //TODO format time
                     response = "OK " +  new Date();
                 }
                 System.out.println("Responded to GETTIME request with " + response);
             }
             else if (command.indexOf("START") != -1){
                 if (isMatchingRegex(command)){
                     if (verifyCredentials(m.group(1), m.group(2))){
                         if (ServerVremena.isPaused()){
                             ServerVremena.setPaused(false);
                             response = "OK";
                             System.out.println("Server is starting!");
                         }
                         else{
                             response = "ERROR: server is already paused.";
                         }
                     }
                     else{
                         response = "ERROR: Wrong username or password.";
                     }
                 }
                 else{
                     response = "ERROR: bad command.";
                 }
             }
             else if (command.indexOf("STOP") != -1){
                 if (isMatchingRegex(command) && verifyCredentials(m.group(1), m.group(2))){
                     if (!ServerVremena.isStopped()){
                         ServerVremena.setStopped(true);
                         response = "OK";
                         System.out.println("Server is stopping!");
                        //client.close();
                        interrupt();
                     }
                     else{
                         response = "ERROR: Server is already stopped.";
                     }
                 }
                 else{
                     response = "ERROR: Wrong username or password.";
                 }
             }
             else if (command.indexOf("PAUSE") != -1){
                 if (isMatchingRegex(command) && verifyCredentials(m.group(1), m.group(2))){
                     if (!ServerVremena.isPaused()){
                         ServerVremena.setPaused(true);
                         response = "OK";
                         System.out.println("Server is pausing!");
                     }
                     else{
                         response = "ERROR: Server is already paused.";
                     }
                 }
                 else{
                     response = "ERROR: wrong username or password.";
                 }
             }
             else if (command.indexOf("CLEAN") != -1){
                 if (isMatchingRegex(command) && verifyCredentials(m.group(1), m.group(2))){
                     //TODO implement cleaning
                     response = "OK";
                 }
                 else{
                     response = "ERROR: wrong username or password.";
                 }
             }
             else if (command.indexOf("SETTIME") != -1){
                 p = Pattern.compile(setTimeRegex);
                 m = p.matcher(command);
                 status = m.matches();
                 if (status){
                     if (verifyCredentials(m.group(1), m.group(2))){
                         response = "OK";
                         System.out.println("Setting new time according to the given one " + m.group(3));
                     }
                     else{
                         response = "ERROR: wrong username or password.";
                     }
                 }
                 else
                     response = "ERROR: bad command ";
             }
             else{
                 response = "Wrong input.";
             }
             os.write(response.getBytes());
             os.flush();
         } 
         catch (IOException e) {
             System.out.println("ERROR");
         } 
         finally{
             try {
                 if (is != null)
                     is.close();
                 if (os != null)
                     os.close();
                 //dnevnik.zatvoriDnevnik();
             } catch (IOException ex) {
                 Logger.getLogger(ServerVremenaDretva.class.getName()).log(Level.SEVERE, null, ex);
                 System.out.println("ERROR");
             }
         }
     }
 
     @Override
     public void interrupt() {
         super.interrupt(); 
     }
     
     public boolean verifyCredentials(String username, String password){
         if (username.equals(config.dajPostavku("admin")) && password.equals(config.dajPostavku("lozinka"))){
             return true;
         }
         return false;
     }
     
     public boolean isMatchingRegex(StringBuilder command){
         p = Pattern.compile(adminCommandRegex);
         m = p.matcher(command);
         status = m.matches();
         if (status){
             return true;
         }
         return false;
     }
   
 }
