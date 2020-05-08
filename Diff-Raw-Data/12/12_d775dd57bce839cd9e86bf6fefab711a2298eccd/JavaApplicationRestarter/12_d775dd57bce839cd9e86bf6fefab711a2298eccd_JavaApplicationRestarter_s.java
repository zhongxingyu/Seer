 package org.cujau.utils;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 public class JavaApplicationRestarter {
 
     protected List<String> restartArgs;
 
     /**
      * Create a new instance of the restarter that will restart the executable JAR file from which
      * the given Object comes.
      * <p>
      * If the Object is not in a JAR file, then the relaunch parameters will be set to use the
      * current main class.
      * </p>
      * 
      * @param classInJarFile
      *            An instance of a class contained in the executable JAR file to be restarted.
      */
     public JavaApplicationRestarter( Object classInJarFile ) {
         File exejar = getURIOfContainingJAR( classInJarFile );
         if ( !exejar.getName().endsWith( ".jar" ) ) {
             String classpath = getJavaClasspath();
             String mainclass = getMainClass();
             restartArgs = getRestartArgs( classpath, mainclass );
         } else {
             restartArgs = getRestartArgs( exejar );
         }
     }
 
     /**
      * Create a new instance of the restarter that will restart the named executable JAR file that
      * is in the same directory as the JAR file from which the given Object comes.
      * <p>
      * If the Object is not in a JAR file, then the relaunch parameters will be set to use the
      * current main class.
      * </p>
      * 
      * @param classInJarFile
      *            An instance of a class contained in a JAR file that is in the same directory as
      *            executable JAR.
      * @param executableJarFilename
      *            The name of the executable JAR file to be restarted.
      */
     public JavaApplicationRestarter( Object classInJarFile, String executableJarFilename ) {
         File exejar = getURIOfContainingJAR( classInJarFile );
         if ( exejar == null || !exejar.getName().endsWith( ".jar" ) ) {
             String classpath = getJavaClasspath();
             String mainclass = getMainClass();
             restartArgs = getRestartArgs( classpath, mainclass );
         } else {
             File parent = exejar.getParentFile();
             File finalExeJar = new File( parent, executableJarFilename );
             restartArgs = getRestartArgs( finalExeJar );
         }
     }
 
     /**
      * Create a new instance of the restarter that will restart the current main class with the
      * current classpath.
      */
     public JavaApplicationRestarter() {
         String classpath = getJavaClasspath();
         String mainclass = getMainClass();
         restartArgs = getRestartArgs( classpath, mainclass );
     }
 
     public boolean restart() {
         return restart( restartArgs );
     }
 
     public List<String> getRestartArgs() {
         return restartArgs;
     }
 
     @Override
     public String toString() {
         return getClass().getSimpleName() + " : " + StringUtil.toString( restartArgs );
     }
 
     protected boolean restart( List<String> args ) {
         String[] argAry = args.toArray( new String[] {} );
         try {
             /* Process p = */Runtime.getRuntime().exec( argAry );
         } catch ( Exception e ) {
             e.printStackTrace();
             return false;
         }
 
         System.exit( 0 );
         return true;
     }
 
     public File getURIOfContainingJAR( Object classInJarFile ) {
         File jarFile;
         try {
             jarFile =
                 new File( classInJarFile.getClass().getProtectionDomain().getCodeSource().getLocation()
                                         .toURI() );
         } catch ( Exception e ) {
             return null;
         }
         return jarFile;
     }
 
     public String getMainClass() {
         try {
             throw new Exception( "Dummy" );
         } catch ( Exception e ) {
             int length = e.getStackTrace().length;
             return e.getStackTrace()[length - 1].getClassName();
         }
     }
 
     public String getJavaExecutable() {
         File javaHome = new File( System.getProperty( "java.home" ) );
         File javaExe = new File( javaHome, "/bin/java" );
         return javaExe.getAbsolutePath();
     }
 
     public String getJavaClasspath() {
         return System.getProperty( "java.class.path" );
     }
 
     protected List<String> getRestartArgs( String classpath, String mainclass ) {
         String exe = getJavaExecutable();
         ArrayList<String> ret = new ArrayList<String>();
         ret.add( exe );
         List<String> extraArgs = getExtraRestartArgs();
         if ( extraArgs != null ) {
             ret.addAll( extraArgs );
         }
         ret.add( "-classpath" );
         ret.add( classpath );
         ret.add( mainclass );
         return ret;
     }
 
     protected List<String> getRestartArgs( File exejar ) {
         String exe = getJavaExecutable();
         ArrayList<String> ret = new ArrayList<String>();
         ret.add( exe );
         List<String> extraArgs = getExtraRestartArgs();
         if ( extraArgs != null ) {
             ret.addAll( extraArgs );
         }
         ret.add( "-jar" );
         ret.add( exejar.getPath() );
         return ret;
     }
 
     protected List<String> getExtraRestartArgs() {
         return null;
     }
 }
