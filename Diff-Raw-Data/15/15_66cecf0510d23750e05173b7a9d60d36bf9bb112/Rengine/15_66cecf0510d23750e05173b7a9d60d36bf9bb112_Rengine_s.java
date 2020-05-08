 package org.rosuda.JRI;
 
 import java.lang.*;
 import java.io.*;
 
 public class Rengine extends Thread {
     static {
 	System.loadLibrary("jri");
     }
 
     boolean died, alive, runLoop, loopRunning;
     String[] args;
     Mutex Rsync;
     RMainLoopCallbacks callback;
     
     public Rengine(String[] args, boolean runMainLoop, RMainLoopCallbacks initialCallbacks) {
         super();
         Rsync=new Mutex();
         died=false;
         alive=false;
         runLoop=runMainLoop;
         loopRunning=false;
         this.args=args;
         callback=initialCallbacks;
         start();
     }
 
     public native int rniSetupR(String[] args);
     
     synchronized int setupR() {
         return setupR(null);
     }
     
     synchronized int setupR(String[] args) {
         int r=rniSetupR(args);
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
     public synchronized native double[] rniGetDoubleArray(long exp);
 
     public synchronized native long rniPutString(String s);
     public synchronized native long rniPutStringArray(String[] a);
     public synchronized native long rniPutIntArray(int [] a);
    public synchronized native long rniPutDoubleArray(double[] exp);
     
    public synchronized native long rniAttr(long exp);
    public synchronized native void rniSetAttr(long exp);
 
     public synchronized native long rniCons(long head, long tail);
     public synchronized native long rniCAR(long exp);
     public synchronized native long rniCDR(long exp);
     public synchronized native long rniPutList(long[] cont);
     public synchronized native long[] rniGetList(long exp);
         
     public synchronized native int rniExpType(long exp);
     public native void rniRunMainLoop();
     
     public synchronized native void rniIdle();
 
     public void addMainLoopCallbacks(RMainLoopCallbacks c)
     {
         // we don't really "add", we just replace ... (so far)
         callback = c;
     }
     
     //============ R callback methods =========
 
     public void jriWriteConsole(String text)
     {
         if (callback!=null) callback.rWriteConsole(this, text);
     }
 
     public void jriBusy(int which)
     {
         if (callback!=null) callback.rBusy(this, which);
     }
 
     public String jriReadConsole(String prompt, int addToHistory)
     {
         return (callback==null)?null:callback.rReadConsole(this, prompt, addToHistory);
     }
 
     public void jriShowMessage(String message)
     {
         if (callback!=null) callback.rShowMessage(this, message);
     }
     
     
     //============ "official" API =============
 
     
     public synchronized REXP eval(String s) {
         long pr=rniParse(s, 1);
         if (pr>0) {
             long er=rniEval(pr, 0);
             if (er>0) {
                 REXP x=new REXP(this, er);
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
         if (setupR(args)==0) {
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
