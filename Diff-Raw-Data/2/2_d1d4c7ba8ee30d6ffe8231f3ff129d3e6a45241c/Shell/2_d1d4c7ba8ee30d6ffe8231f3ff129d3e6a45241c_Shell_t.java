 package net.ant.rc.rpi;
 
 import org.apache.log4j.Logger;
 
 import java.io.IOException;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Ant
  * Date: 19.10.13
  * Time: 3:00
  */
 public class Shell {
     public static String execute(String command){
         Logger logger = Logger.getLogger("log4j.logger.net.ant.rc");
         if (command == null) return null;
 
         String[] c;
         if ("reboot".equals(command)){
             c = new String[]{"/usr/bin/sudo", "/sbin/reboot"};
         }else
         if ("shutdown".equals(command)){
             c = new String[]{"/usr/bin/sudo", "/sbin/shutdown", "now"};
         }else
         if ("temperature".equals(command)){
             c = new String[]{"cat", "/sys/class/thermal/thermal_zone0/temp"};
         }else{
             return null;
         }
 
         try {
             Process p = Runtime.getRuntime().exec(c);
             byte[] buf = new byte[500];
             Thread.sleep(3000);
             if(p.exitValue()==1){
                 p.getErrorStream().read(buf);
             }else{
                 p.getInputStream().read(buf);
             }
             return new String(buf);
         } catch (IOException e) {
            logger.error(e.getMessage());
             return e.getMessage();
         } catch (InterruptedException e) {
             logger.error(e.getMessage(), e);
             return e.getMessage();
         }
     }
 }
