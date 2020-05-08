 package uk.co.geekcaroline.bowling;
 
 import java.io.*;
 
 /**
  * Wrapper for System.console to allow testing
  */
 public class InputDevice {
     BufferedReader reader;
     PrintWriter writer;
 
     private static InputDevice DEFAULT = (System.console() == null) ?
             new InputDevice(System.in, System.out)
             : new InputDevice(System.console());
 
     public InputDevice(InputStream in, OutputStream out) {
         this.reader = new BufferedReader(new InputStreamReader(in));
         this.writer = new PrintWriter(out, true);
     }
 
     public InputDevice(BufferedReader in, PrintWriter out) {
         this.reader = in;
         this.writer = out;
     }
 
     public InputDevice(Console console) {
         this.reader = new BufferedReader(console.reader());
         this.writer = console.writer();
     }
 
     public Reader reader() {
         return reader;
     }
 
     public PrintWriter writer() {
         return writer;
     }
 
     public String writeCommandThenReadLine(String msg){
        this.writer().write(msg);
         try{
             return this.reader.readLine();
         } catch (IOException ioe) {
             System.out.println("Sorry, an error occured whilst reading input");
         }
         return "";
     }
 
     public String readLine() {
         try{
             return this.reader.readLine();
         } catch (IOException ioe) {
             System.out.println("Sorry, an error occured whilst reading input");
         }
         return "";
     }
 
     public static InputDevice getDefaultTextDevice() {
         return DEFAULT;
     }
 
 }
