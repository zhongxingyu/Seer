 package org.kevoree.microsandbox.test;
 
 import junit.framework.TestCase;
 import org.kevoree.watchdog.Runner;
 import org.kevoree.watchdog.WatchDogCheck;
 import org.kevoree.watchdog.child.jvm.JVMStream;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Stack;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.TimeUnit;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Created with IntelliJ IDEA.
  * User: duke
  * Date: 08/07/13
  * Time: 13:36
  */
 public class AbstractMicroSandboxTester extends TestCase implements JVMStream.LineHandler {
 
     private BlockingQueue<Object> block = new ArrayBlockingQueue<Object>(1);
 
 
     private Stack<String> toRead = new Stack<String>();
 
     private StringBuilder builder;
 
    public String runSandbox(String kevsClassLoaderPath, int timeout, List<String> linestoObserve) {
         builder = new StringBuilder();
        for (String l : linestoObserve) {
             toRead.add(l);
         }
         Object result = null;
         try {
            File tempFile = File.createTempFile("kevsTempFile", ".kevs");
             FileOutputStream fos = new FileOutputStream(tempFile);
             InputStream is = this.getClass().getClassLoader().getResourceAsStream(kevsClassLoaderPath);
             byte data[] = new byte[1024];
             int count;
             while ((count = is.read(data, 0, 1024)) != -1) {
                 fos.write(data, 0, count);
             }
             is.close();
             fos.close();
 
             //put in param
             String[] params = new String[2];
            if (System.getProperty("kevoree.version") == null) {
                params[0] = "LATEST";
            } else {
                params[0] = System.getProperty("kevoree.version");
            }
             params[1] = tempFile.getAbsolutePath();
 
             WatchDogCheck.lineHandler = this;
             Runner.main(params);
            /*result = */
            block.poll(timeout, TimeUnit.MILLISECONDS);
 
             tempFile.delete();
         } catch (Exception e) {
             e.printStackTrace();
         }
 
         //remove temp kevs file
 
         /*if(result == null){
             fail();
         }*/
 
         return builder.toString();
     }
 
 
     @Override
     public void handle(String line) {
 
         System.out.println(line);
 
         List<String> toRemove = new ArrayList<String>();
         for (String regex : toRead) {
             Pattern pattern = Pattern.compile(regex);
             Matcher matcher = pattern.matcher(line);
             if (matcher.find()) {
                 toRemove.add(regex);
                 builder.append(line).append("\n");
                 // If a regex match then we don't need to test the others
                 break;
             }
         }
 
         for (String regex : toRemove) {
             toRead.remove(regex);
         }
         /*if(line.equals(toRead.firstElement())){
             toRead.pop();
         }*/
        if (toRead.empty()) {
             try {
                 block.put("success");
             } catch (InterruptedException e) {
                 e.printStackTrace();
             }
         }
     }
 }
