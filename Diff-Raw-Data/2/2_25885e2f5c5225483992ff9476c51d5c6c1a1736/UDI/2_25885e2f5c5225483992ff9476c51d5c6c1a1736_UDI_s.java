 import java.io.*;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 public class UDI {
   public static void main(String[] args) throws IOException {
     build(new File("."), "/");
   }
 
   private static void build(File root, String relativePath) throws IOException {
     Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(root, "index.html")), "utf-8"));
     out.append("<html><head><title>Index of ").append(relativePath).append("</title></head><body><h1>Index of ").append(relativePath).append("</h1>\n<hr/><table>");
     List<File> dirs = new ArrayList<File>();
     List<String> files = new ArrayList<String>();
     for (String name : root.list()) {
       if (name.charAt(0) != '.' && !name.endsWith(".iml") && 
         !name.endsWith(".markdown") && !name.endsWith(".java") && !name.equals("index.html") && (!relativePath.equals("/") || !name.equals("out"))) {
         File file = new File(root, name);
         if (!file.isHidden()) {
           if (file.isDirectory()) {
             dirs.add(file);
           }
           else {
             files.add(name);
           }
         }
       }
     }
 
     Collections.sort(files);
     Collections.sort(dirs);
 
     for (String name : files) {
       out.append("\n\t<tr><td><a href=\"").append(name).append("\">").append(name).append("</a></td></tr>");
     }
 
     for (File dir : dirs) {
       out.append("\n\t<tr><td><a href=\"").append(dir.getName()).append("\">").append(dir.getName()).append("/</a></td></tr>");
      build(dir, relativePath + dir.getName());
     }
 
     out.append("</table></body></html>");
     out.close();
   }
 }
