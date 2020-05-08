 package ru.amalyuhin;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.File;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.net.URLDecoder;
 import java.util.*;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 
 /**
  * Created with IntelliJ IDEA.
  * User: amalyuhin
  * Date: 20.04.13
  * Time: 12:29
  * To change this template use File | Settings | File Templates.
  */
 public class IndexServlet extends HttpServlet {
 
     protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         String dataDir = "/data";
 
         try {
             String[] files = getResourceListing(this.getClass(), dataDir);
             req.setAttribute("ontFiles", files);
 
         } catch (Exception e) { }
 
         getServletContext().getRequestDispatcher("/index.jsp").forward(req, resp);
     }
 
     private String[] getResourceListing(Class clazz, String path) throws URISyntaxException, IOException {
         URL dirURL = clazz.getClassLoader().getResource(path);
         if (dirURL != null && dirURL.getProtocol().equals("file")) {
         /* A file path: easy enough */
             return new File(dirURL.toURI()).list();
         }
 
         if (dirURL == null) {
         /*
          * In case of a jar file, we can't actually find a directory.
          * Have to assume the same jar as clazz.
          */
             String me = clazz.getName().replace(".", "/")+".class";
             dirURL = clazz.getClassLoader().getResource(me);
         }
 
         if (dirURL.getProtocol().equals("jar")) {
         /* A JAR path */
             String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); //strip out only the JAR file
             JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
             Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
             Set<String> result = new HashSet<String>(); //avoid duplicates in case it is a subdirectory
             while(entries.hasMoreElements()) {
                 String name = entries.nextElement().getName();
                 if (name.startsWith(path)) { //filter according to the path
                     String entry = name.substring(path.length());
                     int checkSubdir = entry.indexOf("/");
                     if (checkSubdir >= 0) {
                         // if it is a subdirectory, we just return the directory name
                         entry = entry.substring(0, checkSubdir);
                     }
                     result.add(entry);
                 }
             }
             return result.toArray(new String[result.size()]);
         }
 
         throw new UnsupportedOperationException("Cannot list files for URL "+dirURL);
     }
 }
