 package edu.mit.wi.haploview;
 
 import javax.swing.*;
 import java.io.*;
 
 class StreamGobbler extends Thread{
     InputStream is;
 
     StreamGobbler(InputStream is){
         this.is = is;
     }
 
     public void run(){
         try{
             InputStreamReader isr = new InputStreamReader(is);
             BufferedReader br = new BufferedReader(isr);
             String line=null;
             while ( (line = br.readLine()) != null)
                 System.out.println(line);
         } catch (IOException ioe){
             ioe.printStackTrace();
         }
     }
 }
 
 public class HVWrap {
 
     HVWrap() {}
 
     public static void main(String[] args) {
 
         int exitValue = 0;
         String dir = System.getProperty("user.dir");
         String sep = System.getProperty("file.separator");
         String ver = System.getProperty("java.version");
         System.out.println(ver);
         String jarfile = System.getProperty("java.class.path");
 
         String argsToBePassed = new String();
         boolean headless = false;
         for (int a = 0; a < args.length; a++){
             argsToBePassed = argsToBePassed.concat(" " + args[a]);
            if (args[a].equals("-n")){
                 headless=true;
             }
         }
 
         try {
             //if the nogui flag is present we force it into headless mode
             String runString = "java -Xmx650m -classpath " + jarfile;
             if (headless){
                 runString += " -Djava.awt.headless=true";
             }
             runString += " edu.mit.wi.haploview.HaploView"+argsToBePassed;
             Process child = Runtime.getRuntime().exec(runString);
 
             //start up a thread to simply pump out all messages to stdout
             StreamGobbler isg = new StreamGobbler(child.getInputStream());
             isg.start();
 
             //while the child is alive we wait for error messages
             boolean dead = false;
             StringBuffer errorMsg = new StringBuffer("Fatal Error:\n");
             BufferedReader besr = new BufferedReader(new InputStreamReader(child.getErrorStream()));
             String line = null;
             if ((line = besr.readLine()) != null) {
                 errorMsg.append(line);
                 //if the child generated an error message, kill it
                 child.destroy();
                 dead = true;
             }
 
             //if the child died painfully throw up R.I.P. dialog
             if (dead){
                 if (headless){
                     System.out.println(errorMsg);
                 }else{
                     JFrame jf = new JFrame();
                     JOptionPane.showMessageDialog(jf, errorMsg, null, JOptionPane.ERROR_MESSAGE);
                 }
                 exitValue = -1;
             }
         } catch (Exception e) {
             if (headless){
                 System.out.println("Error:\nUnable to launch Haploview.\n"+e.getMessage());
             }else{
                 JFrame jf = new JFrame();
                 JOptionPane.showMessageDialog(jf, "Error:\nUnable to launch Haploview.\n"+e.getMessage(), null, JOptionPane.ERROR_MESSAGE);
             }
         }
         System.exit(exitValue);
     }
 
 
 }
