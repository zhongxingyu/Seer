 package sbt.eclipse;
 
import java.io.File;
import java.io.FileFilter;
import java.util.Locale;

 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jdt.core.IClasspathEntry;
 
 /**
  * @author Joonas Javanainen
  * 
  */
 public class Constants {
 
     public static final String DEFAULT_LIB_FOLDER = "lib";
     public static final String DEFAULT_MANAGED_LIB_FOLDER = "lib_managed";
     public static final String DEFAULT_OUTPUT_FOLDER = "target/classes";
     public static final String SCALA_NATURE_ID = "ch.epfl.lamp.sdt.core.scalanature";
    public static final IPath SCALA_CONTAINER_ID = new Path(
            "ch.epfl.lamp.sdt.launching.SCALA_CONTAINER");
    public static final IPath JRE_CONTAINER_ID = new Path(
            "org.eclipse.jdt.launching.JRE_CONTAINER");

    public static final FileFilter JAR_FILE_FILTER = new FileFilter() {
        public boolean accept(File pathname) {
            return (pathname.isFile() && pathname.getName().toLowerCase(
                    Locale.ENGLISH).endsWith(".jar"));
        }
    };
 
     public static final IClasspathEntry[] EMPTY_CLASSPATHENTRY_ARRAY = new IClasspathEntry[0];
     public static final IPath[] EMPTY_PATH_ARRAY = new IPath[0];
 
 }
