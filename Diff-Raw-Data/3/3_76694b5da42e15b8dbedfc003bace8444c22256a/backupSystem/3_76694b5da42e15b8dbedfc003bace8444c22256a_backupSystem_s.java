 
 package me.Crosant.System_Tools;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 
 public class backupSystem {
     
     public static void backup(){
         Timer timer = new Timer();
 
         if (getSystemPropertys.SystemOS.equalsIgnoreCase("Linux")){
         
         class task   extends TimerTask{
             public void run() 
             {
                 try{
                 boolean exists = (new File("home.zip")).exists();
                 if (exists) {
                  Runtime.getRuntime().exec("rm home.zip");
                  Runtime.getRuntime().exec("zip -r home.zip " + getSystemPropertys.UserDir);
                 System.out.println("Finished");
                 } else {
                    Runtime.getRuntime().exec("zip -r home.zip " + getSystemPropertys.UserDir);
                 }
                 
                     
                     
                }   catch (InterruptedException ex) {
                        Logger.getLogger(backupSystem.class.getName()).log(Level.SEVERE, null, ex);
                     } catch (IOException ex) {
                     Logger.getLogger(backupSystem.class.getName()).log(Level.SEVERE, null, ex);
                 }
         			    
             }
         }
               timer.schedule  ( new task() {}, 1000, 1000*60*60 );
         
     }
     
     else{
     System.out.println("not finished jet");
 }
     }
     
 }
