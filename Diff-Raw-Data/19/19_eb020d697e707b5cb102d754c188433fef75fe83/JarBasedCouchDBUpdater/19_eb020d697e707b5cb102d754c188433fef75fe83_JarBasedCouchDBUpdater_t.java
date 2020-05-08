 package org.jcouchdb.util;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 import java.util.regex.Pattern;
 
 import org.apache.commons.io.IOUtils;
 import org.jcouchdb.document.DesignDocument;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class JarBasedCouchDBUpdater
 extends AbstractCouchDBUpdater
 {
     private static Logger log = LoggerFactory.getLogger(JarBasedCouchDBUpdater.class);
     
     private static final String FILE_SEPARATOR = System.getProperty("file.separator");
 
     private String pathInsideJar;
 
     private Pattern pattern;
     
     private File jarFile;
     
     public void setJarFile(File jarFile)
     {
         this.jarFile = jarFile;
     }
 
     public void setPathInsideJar(String pathInsideJar)
     {
         this.pathInsideJar = pathInsideJar;
     }
 
     public void setJarFilePattern(String pattern)
     {
         this.pattern = Pattern.compile(pattern);
     }
 
     @Override
     protected List<DesignDocument> readDesignDocuments() throws IOException
     {
         File file = findJarFileOrSourceDirectory();
         if (file.isDirectory())
         {
             // delegate reading to normal couchdb updater
             CouchDBUpdater couchDBUpdater = new CouchDBUpdater();
             couchDBUpdater.setCreateDatabase(createDatabase);
             couchDBUpdater.setDatabase(database);
             couchDBUpdater.setDesignDocumentDir(file);
             return couchDBUpdater.readDesignDocuments();
         }
 
         JarFile jarFile = new JarFile(file);
 
         Map<String, DesignDocument> designDocuments = new HashMap<String, DesignDocument>();
         for (Enumeration<JarEntry> e = jarFile.entries(); e.hasMoreElements(); )
         {
             JarEntry entry = e.nextElement();
 
             String name = entry.getName();
             boolean isMapFunction = name.endsWith(MAP_SUFFIX);
             boolean isReduceFunction = name.endsWith(REDUCE_SUFFIX);
             if (isMapFunction || isReduceFunction)
             {
 
                 if (name.startsWith(pathInsideJar))
                 {
                     log.debug("found map or reduce function: {}", name);
                     
                     String content = IOUtils.toString(jarFile.getInputStream(entry));
                    createViewFor(name.substring(pathInsideJar.length()), content, designDocuments, "/");
                 }
             }
         }
 
         return new ArrayList<DesignDocument>(designDocuments.values());
     }
 
     File findJarFileOrSourceDirectory()
     {
         if (jarFile != null)
         {
             return jarFile;
         }
         
         String classpath = System.getProperty("java.class.path");
         
         String[] paths = classpath.split("\\" + System.getProperty("path.separator"));
         for (String path : paths)
         {
             if (pattern.matcher(path).matches())
             {
                 log.debug("'{}' matches {}, returning it as jar to generate from", path, pattern);
                 return new File(path);
             }
         }
 
         for (String path : paths)
         {
             if (!path.endsWith(".jar"))
             {
                 File baseDir = new File(path);
                 if (baseDir.isDirectory())
                 {
                     String file = pathInsideJar;
                     if (file.startsWith(FILE_SEPARATOR))
                     {
                         file = file.substring(1);
                     }
                     File dir = new File(baseDir, file);
                     log.debug("{} exists as sub directory of class path entry {}, using it to generate from", dir, baseDir);
                     if (dir.exists() && dir.isDirectory())
                     {
                         return dir;
                     }
                 }
             }
         }
         
         throw new IllegalStateException("Could not find jar or source dir in classpath " + classpath);
     }
 }
