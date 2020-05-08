 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package es.cediant.util;
 
 import es.cediant.db.ProcessHelper;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author miguel
  */
 public class ExecThread extends Thread {
     
     private int idProcess;
     private ProcessHelper ph;
     private String command;
 
     public ExecThread(int idProcess, ProcessHelper ph, String command) {
         this.idProcess = idProcess;
         this.ph = ph;
         this.command = command;
     }
 
     public int getIdProcess() {
         return idProcess;
     }
 
     public void setIdProcess(int idProcess) {
         this.idProcess = idProcess;
     }        
     
     @Override
     public void run(){
         try {
             Process p = Runtime.getRuntime().exec(command);
             p.waitFor();
             BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
             String line = reader.readLine();
             while (line != null) {
                 line = reader.readLine();
                 System.out.println(" >> "+line);
             }            
             ph.modify(idProcess, "finished");
         } catch (InterruptedException ex) {
             Logger.getLogger(ExecThread.class.getName()).log(Level.SEVERE, null, ex);
             ph.modify(idProcess, "error");
         } catch (IOException ex) {
             Logger.getLogger(ExecThread.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("IOException ex");
             ph.modify(idProcess, "error");
         }
     }
 }
