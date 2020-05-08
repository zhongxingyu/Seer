 package applet;
 
 import java.applet.Applet;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.nio.MappedByteBuffer;
 import java.nio.channels.FileChannel;
 import java.security.AccessController;
 import java.security.PrivilegedAction;
import java.util.Scanner;
 
 public class JsFsApplet extends Applet {
   public JsFsApplet(){
     AccessController.doPrivileged(new PrivilegedAction<Void>() {
       public Void run() {
         m_cwd = m_home = System.getProperty("user.home") + File.separator;
         return null;
       }
     });
   }
   
   public String cwd(){
     return m_cwd;
   }
   
   public boolean cd(){
     return cd(m_home);
   }
   
   public boolean cd(final String path){
     if(path == null){
       return false;
     }
     
     return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
       public Boolean run() {
         File dir = new File(getAbsolutePath(path));
         
         if(dir.exists()){
           m_cwd = dir.toString() + File.separator;
           return true;
         }
         
         return false;
       }
     });
   }
   
   public String[] ls(){
     return ls("");
   }
   
   public String[] ls(final String path){  
     if(path == null){
       return null;
     }
     
     return AccessController.doPrivileged(new PrivilegedAction<String[]>() {
       public String[] run() {
         return new File(getAbsolutePath(path)).list();
       }
     });
   }
   
   public boolean mkdir(final String path){
     if(path == null){
       return false;
     }
     
     return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
       public Boolean run() {
         return new File(getAbsolutePath(path)).mkdir();
       }
     });
   }
   
   public boolean rm(final String path){
     if(path == null){
       return false;
     }
     
     return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {      
       public Boolean run() {
         File file = new File(getAbsolutePath(path));
         
         if(file.isDirectory()){
           for(File c : file.listFiles()){
             rm(c.getAbsolutePath());
           }
         }
         
         return file.delete();
       }
     });
   }
   
   public String read(final String path) {
     if(path == null){
       return null;
     }
     
     return AccessController.doPrivileged(new PrivilegedAction<String>() {
       public String run() {
        // We can't use a memory mapped file because of this bug: http://bugs.sun.com/view_bug.do?bug_id=4724038
        Scanner scanner = null;
 
        try{
          scanner = new Scanner(new File(getAbsolutePath(path))).useDelimiter("\\A");
          return scanner.next();
        }catch(FileNotFoundException e){
           return null;
        }finally{
          if(scanner != null){
            scanner.close();
           }
         }
       }
     });
   }
   
   public boolean write(final String path, final String content) {
     if(path == null || content == null){
       return false;
     }
     
     return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
       public Boolean run() {
         BufferedWriter out = null;
         
         try {
           out = new BufferedWriter(new FileWriter(getAbsolutePath(path)), 32768);
           out.write(content);
         } catch (IOException e) {
           return false;
         } finally {
           try{
             if(out != null){
               out.close();
             }
           }catch(IOException e){
           }
         }
         
         return true;
       }
     });
   }
   
   public boolean cp(final String source, final String destination){
     return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
       public Boolean run() {
         FileChannel in = null;
         FileChannel out = null;
         
         try{
           in = new FileInputStream(getAbsolutePath(source)).getChannel();
           try{
             out = new FileInputStream(getAbsolutePath(destination)).getChannel();
           }catch(FileNotFoundException e){
             new File(getAbsolutePath(destination)).createNewFile();
             out = new FileOutputStream(getAbsolutePath(destination)).getChannel();
           }
           
           out.transferFrom(in, 0, in.size());
         }catch(Exception e){
           return false;
         }finally{
           try {
             in.close();
             out.close();
           } catch (IOException e) {
           }
         }
         
         return true;
       }
     });
   }
   
   private String getAbsolutePath(String path){
     return new File(path).isAbsolute() ? path : m_cwd + path;
   }
   
   private String m_cwd;
   private String m_home;
 }
