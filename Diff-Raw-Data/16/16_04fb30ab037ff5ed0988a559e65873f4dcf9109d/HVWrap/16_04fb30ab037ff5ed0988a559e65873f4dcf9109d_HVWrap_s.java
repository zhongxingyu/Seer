 package edu.mit.wi.haploview;
 
 import javax.swing.*;
 import java.io.*;
 import java.util.StringTokenizer;
 
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
     private static JTextArea errorTextArea;
     private static JFrame contentFrame;
 
 
     HVWrap() {}
 
     public static void main(String[] args) {
 
         int exitValue = 0;
         String dir = System.getProperty("user.dir");
         String sep = System.getProperty("file.separator");
         String ver = System.getProperty("java.version");
         //TODO:do some version checking and bitch at people with old JVMs
         /*StringTokenizer st = new StringTokenizer(ver, ".");
         while (st.hasMoreTokens()){
             System.out.println(st.nextToken());
         } */
        String jarfile = System.getProperty("java.class.path");
 
         String argsToBePassed = new String();
         boolean headless = false;
         for (int a = 0; a < args.length; a++){
             argsToBePassed = argsToBePassed.concat(" " + args[a]);
             if (args[a].equals("-n") || args[a].equalsIgnoreCase("-nogui")){
                 headless=true;
             }
         }
 
         try {
             //if the nogui flag is present we force it into headless mode
            String runString = "java -Xmx1024m -classpath \"" + jarfile + "\"";
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
 
             while ( !dead  && (line = besr.readLine()) != null) {
                 if(line.lastIndexOf("Memory") != -1) {
                     errorMsg.append(line);
                     //if the child generated an "Out of Memory" error message, kill it
                     child.destroy();
                     dead = true;
                 }else {
                     //for any other errors we show them to the user
                     if(headless) {
                         //if were in headless (command line) mode, then print the error text to command line
                         System.err.println(line);
                     } else {
                         //otherwise print it to the error textarea
                         if(errorTextArea == null) {
                             //if this is the first error line then we need to create the JFrame with the
                             //text area
                             javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
                                 public void run() {
                                     createAndShowGUI();
                                 }
                             });
                         }
                         //if the user closed the contentFrame, then we want to reopen it when theres error text
                         if(!contentFrame.isVisible()) {
                             contentFrame.setVisible(true);
                         }
 
                         errorTextArea.append(line + "\n");
                         errorTextArea.setCaretPosition(errorTextArea.getDocument().getLength());
                     }
                 }
             }
             final String realErrorMsg = errorMsg.toString();
 
             //if the child died painfully throw up R.I.P. dialog
             if (dead){
                 if (headless){
                     System.err.println(errorMsg);
                 }else{
                     Runnable showRip = new Runnable() {
                         public void run() {
                             JFrame jf = new JFrame();
                             JOptionPane.showMessageDialog(jf, realErrorMsg, null, JOptionPane.ERROR_MESSAGE);}
                     };
                     SwingUtilities.invokeAndWait(showRip);
                 }
                 exitValue = -1;
             }
         } catch (Exception e) {
             if (headless){
                 System.err.println("Error:\nUnable to launch Haploview.\n"+e.getMessage());
             }else{
                 JFrame jf = new JFrame();
                 JOptionPane.showMessageDialog(jf, "Error:\nUnable to launch Haploview.\n"+e.getMessage(), null, JOptionPane.ERROR_MESSAGE);
             }
         }
         System.exit(exitValue);
     }
 
 
     private static void createAndShowGUI() {
 
         contentFrame = new JFrame("Haploview Error Log");
         contentFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
 
         JComponent newContentPane = new JPanel();
         newContentPane.setOpaque(true); //content panes must be opaque
 
         errorTextArea = new JTextArea(15,50);
         errorTextArea.setEditable(false);
         errorTextArea.setLineWrap(true);
         JScrollPane scrollPane = new JScrollPane(errorTextArea,
                 JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                 JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 
         errorTextArea.append("*************************\n"+
                 "If you are reporting a problem please include the contents of this log.\njcbarret@broad.mit.edu\n"+
                 "*************************\n");
 
         newContentPane.add(scrollPane);
         contentFrame.setContentPane(newContentPane);
         contentFrame.pack();
         contentFrame.setVisible(true);
     }
 
 
 }
