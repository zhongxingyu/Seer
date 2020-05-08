 package com.ekaqu.jython;
 
 import com.google.common.base.Strings;
 import com.google.common.base.Throwables;
 import com.google.common.io.CharStreams;
 import com.google.common.io.LineReader;
 import org.python.core.PySystemState;
 import org.python.util.InteractiveConsole;
 import org.python.util.JLineConsole;
 
 import java.awt.datatransfer.StringSelection;
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.Properties;
 
 /**
  * Date: 3/4/12
  * Time: 8:23 PM
  */
 public class JythonShell implements Runnable {
   private final InteractiveConsole console;
 
   JythonShell(Properties properties) {
     PySystemState.initialize(
       PySystemState.getBaseProperties(),
       properties);
     console = getConsole();
   }

   public void run() {
     console.interact();
   }
 
   public static void main(String[] args) {
     new JythonShell(System.getProperties()).run();
   }
 
   private static InteractiveConsole getConsole() {
     String interpreter = PySystemState.registry.getProperty("python.console", "");
    if (Strings.isNullOrEmpty(interpreter)) {
       return new JLineConsole();
     } else {
       try {
         return (InteractiveConsole) Class.forName(interpreter).newInstance();
       } catch (Throwable e) {
         throw Throwables.propagate(e);
       }
     }
   }
 }
