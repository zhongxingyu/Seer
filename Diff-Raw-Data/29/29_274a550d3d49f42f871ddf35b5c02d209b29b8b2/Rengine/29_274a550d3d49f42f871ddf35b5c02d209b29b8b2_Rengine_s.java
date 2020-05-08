 package org.rosuda.JRI;
 
 import java.lang.*;
 import java.io.*;
 
import org.rosuda.RGui.*;
import org.rosuda.RGui.toolkit.*;

 public class Rengine extends Thread {
     static {
 	System.loadLibrary("jri");
     }
 
     boolean died, alive, runLoop, loopRunning;
     
     public Rengine(boolean runMainLoop) {
         super();
         died=false;
         alive=false;
         runLoop=runMainLoop;
         loopRunning=false;
         start();
         while (!alive && !died) yield();
     }
 
     public native int rniSetupR();
     synchronized int setupR() {
         int r=rniSetupR();
         if (r==0) {
             alive=true; died=false;
         } else {
             alive=false; died=true;
         }
         return r;
     }
     
     public synchronized native long rniParse(String s, int parts);
     public synchronized native long rniEval(long exp, long rho);
     public synchronized native String rniGetString(long exp);
     public synchronized native String[] rniGetStringArray(long exp);
     public synchronized native int[] rniGetIntArray(long exp);
    public synchronized native int[] rniGetDoubleArray(long exp);
     public synchronized native int rniExpType(long exp);
     public native void rniRunMainLoop();
     
     public synchronized native void rniIdle();
 
     //============ R callback methods =========
 
 
     public void jriWriteConsole(String text)
     {
         /*if (console != null) {
             try {
                 console.output.append(text,Preferences.RESULT);
             } catch (Exception e) { e.printStackTrace();}
         }
         else*/ System.out.println("R> "+text);
     }
 
     public void jriBusy(int which)
     {
         System.out.println("R_is_busy? "+which);
     }
 
     public String jriReadConsole(String prompt, int addToHistory)
     {
         System.out.print(prompt);
         try {
             BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
             String s=br.readLine();
             return (s==null||s.length()==0)?s:s+"\n";
         } catch (Exception e) {
             System.out.println("jriReadConsole exception: "+e.getMessage());
         }
         return "q('no')\n";
     }
 
     public void jriShowMessage(String message)
     {
         System.out.println("R message: "+message);
     }
     
     
     //============ "official" API =============
 
     
    public synchronized RXP eval(String s) {
         long pr=rniParse(s, 1);
         if (pr>0) {
             long er=rniEval(pr, 0);
             if (er>0) {
                RXP x=new RXP(this, er);
                 return x;
             }
         }
         return null;
     }
     
     public synchronized boolean waitForR() {
         return alive;
     }
 
     public void end() {
         alive=false;
         interrupt();
     }
     
     public void run() {
         System.out.println("Starting R...");
         if (setupR()==0) {
             while (alive) {
                 try {
                     if (runLoop) {                        
                         System.out.println("***> launching main loop:");
                         loopRunning=true;
                         rniRunMainLoop();
                         loopRunning=false;
                         System.out.println("***> main loop finished:");
                         System.exit(0);
                     }
                     sleep(100);
                     rniIdle();
                 } catch (InterruptedException ie) {
                     interrupted();
                 }
             }
             died=true;
             System.out.println("Terminating R thread.");
         } else {
             System.out.println("Unable to start R");
         }
     }
 }
