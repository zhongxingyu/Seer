 /* This class is part of the XP framework
  *
  * $Id$ 
  */
 
 package net.xp_forge.jar;
 
 import net.xp_framework.cmd.*;
 import java.util.jar.*;
 import java.io.*;
 import java.util.List;
 import java.util.Arrays;
 
 /**
  * Creates a jar file
  *
  */
 public class CreateJar extends Command {
     protected JarOutputStream jar;
     protected String[] input;
     protected List<String> excludes= Arrays.asList(new String[] { ".svn", "CVS" });
 
     /**
      * Set filename of jar file
      *
      */
    @Arg(position= 0) public void setJar(String fileName) throws IOException {
         this.jar= new JarOutputStream(new FileOutputStream(fileName));
     }
 
     /**
      * Set files to be included
      *
      */
     @Args(select= "[1..]") public void setFiles(String[] list) throws IOException {
         this.input= list;
     }
 
     /**
      * Adds a file to this JAR, recursing into directories if necessary
      * and stripping the base directory.
      *
      */
     protected void addFile(File file, File base) throws IOException {
         if (this.excludes.contains(file.getName())) {
 
             // Skip over excluded files
             return;
         } else if (file.isDirectory()) {
 
             // Recurse into subdirectories
             for (File child: file.listFiles()) {
                 this.addFile(child, base);
             }
         } else {
             long bytes;
             String name= file.getCanonicalPath();
             String basename= base.getCanonicalPath();
             String entry= name;
 
             // Strip base if necessary
             if (name.startsWith(basename)) {
                 entry= name.substring(basename.length()+ 1, name.length());
             }
             
             // Inside the JAR, all files should be named using forward-slashes
             entry= entry.replace('\\', '/');
             
             // Add file contents
             this.out.print("Adding (" + base + ") " + entry);
             this.jar.putNextEntry(new JarEntry(entry));
             bytes= this.writeFileContents(file);
             this.out.println(": " + bytes);
             this.jar.flush();
         }
     }
     
     /**
      * Writes a given file's contents using 16 kB chunks to the JAR output stream.
      *
      */
     protected long writeFileContents(File file) throws IOException {
         long length= file.length();
         if (0 == length) return 0;      // Short-circuit this
 
 	    byte[] arr= new byte[Math.min((int)length, 0x4000)];
 	    FileInputStream in= new FileInputStream(file);
         long written= 0;
         int chunk;
 
         while (-1 != (chunk= in.read(arr))) {
             this.jar.write(arr);
             written+= chunk;
 	    }
         return written;
     }
 
     /**
      * Run this command
      *
      */
     public void run() {
         try {
             for (String name: this.input) {
                 int p;
                 String base= ".";
 
                 if (-1 != (p= name.indexOf(':'))) {
                     base= name.substring(0, p);
                     name= name.substring(p+ 1, name.length());
                 }
 
                 this.addFile(new File(name), new File(base));
             }
             this.jar.close();
         } catch (IOException e) {
             e.printStackTrace(this.err);
         }
     }
 }
