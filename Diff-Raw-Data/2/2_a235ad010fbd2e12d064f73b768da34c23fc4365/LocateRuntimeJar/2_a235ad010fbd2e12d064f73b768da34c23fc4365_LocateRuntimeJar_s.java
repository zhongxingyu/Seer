 package org.kevoree.watchdog.child.jvm;
 
 import java.io.File;
 import java.io.InputStream;
 
 /**
  * Created with IntelliJ IDEA.
  * User: duke
  * Date: 04/07/13
  * Time: 15:41
  */
 public class LocateRuntimeJar {
 
     public static File locateRuntimeJar(){
         if(System.getProperty("os.name").toLowerCase().contains("mac")){
             try {
                 Process p = Runtime.getRuntime().exec("/usr/libexec/java_home");
                 InputStream rin = p.getInputStream();
                 p.waitFor();
 
                 String path = "";
 
                 while(rin.available() > 0){
                     char c = (char) rin.read();
                     path = path + c;
                 }
 
                 File classes = new File(path.trim()+File.separator+"classes"+File.separator+"classes.jar");
                 return classes;
             } catch (Exception e) {
                 e.printStackTrace();
             }
 
         } else {
             //TODO for unix, I just want to see this working
            return new File("/usr/lib/jvm/java-1.7.0-openjdk-i386/jre/lib/rt.jar");
         }
         return null;
     }
 
 }
