 package com.wideplay.warp.internal;
 
 import com.wideplay.warp.util.reflect.ClassNameUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.io.File;
 import java.net.URL;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * On: 20/03/2007
  *
  * @author Dhanji R. Prasanna
  * @since 1.0
  */
 class ClassListBuilder {
     private final Log log = LogFactory.getLog(getClass());
 
     public List<Class<?>> loadClasses(URL url, String packageName) {
         List<Class<?>> classes = new LinkedList<Class<?>>();
 
         //recursively iterate package tree and load classes
         List<String> classNames = ClassNameUtils.listRecursive(new File(url.getFile()), ClassNameUtils.CLASS_FILE_FILTER);
 
         //ensure class is loaded
         for (String className : classNames)
             try {
                classes.add(Class.forName(packageName + "." + className));
             } catch (ClassNotFoundException e) {
                 log.error("Could not load target page classes", e);
                 e.printStackTrace();
             }
 
         return classes;
     }
 }
