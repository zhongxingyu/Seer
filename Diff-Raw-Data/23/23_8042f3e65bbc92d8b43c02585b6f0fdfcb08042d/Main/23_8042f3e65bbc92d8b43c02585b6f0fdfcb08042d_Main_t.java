 package org.netbeans.javafx.preview;
 
 import java.awt.Toolkit;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.lang.reflect.Method;
 
 /**
  *
  * @author Adam
  */
 public class Main {
 
     public static void main(String[] args) throws Exception {
         SilentWindowPeer.out = System.out;
         Method setOut = System.class.getDeclaredMethod("setOut0", PrintStream.class); //NOI18N
         setOut.setAccessible(true);
         setOut.invoke(null, new PrintStream(new OutputStream() {
             public void write(int b) throws IOException {}
         }));
         System.setProperty("apple.awt.UIElement", "true"); //NOI18N
         System.setProperty("com.apple.backgroundOnly", "true"); //NOI18N
         SilentToolkit.delegate = (Toolkit) Class.forName(System.getProperty("awt.toolkit", "sun.awt.X11.XToolkit")).newInstance(); //NOI18N
         System.setProperty("awt.toolkit", "org.netbeans.javafx.preview.SilentToolkit"); //NOI18N
         if (args.length < 1) return;
         Class c = Class.forName(args[0]);
         String newArgs[] = new String[args.length-1];
         System.arraycopy(args, 1, newArgs, 0, newArgs.length);
         new Thread(new Runnable() {
             public void run() {
                 try {
                     Thread.sleep(10000);
                 } catch (InterruptedException ex) {
                     ex.printStackTrace();
                 }
                SilentWindowPeer.out.close();
                 System.exit(1);
             }
         }, "Preview Timeout").start(); //NOI18N
         c.getDeclaredMethod("main", String[].class).invoke(null, (Object)newArgs); //NOI18N
     }
 
 }
