 package org.jboss.fpak.generator;
 
 import org.jboss.fpak.Runner;
 import org.jboss.fpak.parser.FPakParser;
 
 import java.io.*;
 
 /**
  * @author Mike Brock .
  */
 public class FPakGenerator {
     private File workingDirectory = new File("").getAbsoluteFile();
     private File targetFile = new File("out" + Runner.DEFAULT_FILE_EXTENSION);
 
     public void generate() throws IOException {
         if (targetFile.exists()) {
             throw new RuntimeException("file already exists: " + targetFile.getAbsolutePath());
         }
 
         FileOutputStream outputStream = new FileOutputStream(targetFile);
         PrintWriter writer = new PrintWriter(outputStream);
 
         _generate(workingDirectory, writer);
 
         System.out.println("generated: " + targetFile.getAbsolutePath());
     }
 
     private void _generate(File file, PrintWriter out) throws IOException {
         if (file.isHidden()) return;
 
         if (file.isDirectory()) {
             for (File f : file.listFiles()) {
                 _generate(f, out);
             }
         } else if (!(file.getAbsolutePath().equals(targetFile.getAbsolutePath()))) {
 
             writeFile(file, out);
         }
     }
 
     private void writeFile(File file, PrintWriter out) throws IOException {
         System.out.println(file.getAbsolutePath());
 
         String fileName = file.getAbsolutePath().substring(workingDirectory.getAbsolutePath().length());
         if (fileName.charAt(0) == '/') {
             fileName = fileName.substring(1);
         }
         out.print("++");
         out.print(fileName);
         out.print(":{\n");
 
         InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
 
         int read;
         while ((read = inputStream.read()) != -1) {
             switch (read) {
                 case '{':
                 case '}':
                 case '@':
                 case '$':
                     out.print('\\');
                     if (read == '@' || read == '$') {
                         out.print((char) read);
                     }
                     out.print((char) read);
                     break;
 
                 default:
                     out.print((char) read);
             }
         }
 
         out.print("\n}\n\n");
 
         out.flush();
     }
 
     public static void main(String[] args) throws IOException {
         FPakGenerator gen = new FPakGenerator();
         if (args.length > 0) {
             gen.targetFile = new File(args[0]);
         }
         gen.generate();
     }
 
 }
