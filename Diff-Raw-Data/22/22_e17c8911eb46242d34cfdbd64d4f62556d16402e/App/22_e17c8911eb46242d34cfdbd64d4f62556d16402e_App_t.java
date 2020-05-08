 package org.inuua.executable_war;
 //
 // Copyright 2009 Jean-Francois Richard
 //
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at
 //
 // http://www.apache.org/licenses/LICENSE-2.0
 //
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 //
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.net.JarURLConnection;
 import java.net.MalformedURLException;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.jar.Attributes;
 import java.util.jar.Manifest;
 import javax.naming.directory.InvalidAttributeValueException;
 import javax.naming.directory.NoSuchAttributeException;
 
 /**
  * A launcher meant to be the Main-Class of a WAR file.  It will unpack a Winstone
  * servlet container jar from WEB-INF/lib, then launch itself (the WAR) by
  * asking the then-unpacked Winstone to deploy the WAR (again,
  * the original WAR itself).
  *
  * <p>
  * Confusing?
  */
 public final class App {
 
     private static final String WEB_INF_LIB_FOLDER = "/WEB-INF/lib/";
     /** Manifest entry pointing to the JAR of the winstone servlet container. */
     private static final String WINSTONE_JAR_MANIFEST_ENTRY = "X-Winstone-Jar";
     /** Manifest entry containing the Winstone parameters. */
     private static final String WINSTONE_JAR_MANIFEST_PARAMS = "X-Extra-Winstone-Parameters";
     /** Buffer size used for copying files over. */
     private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
     /** Manifest attributes. */
     private static Attributes myOwnManifest;
     /** File pointing to ourselve (the original .war file). */
     private static File myOwnFileName;
 
     /** This class is not meant to be instantiated, hence a private constructor. */
     private App() {
     }
 
     /**
      * Does the magic of extracting the Winstone servlet container and then
      * telling it to deploy and run the original WAR file.
      *
      * @param args command-line arguments
      */
     public static void main(final String... args) throws
             // Scary list of exceptions -- we percolate them up anyway since
             // this is really 'pre-startup'; the servlet container will handle
             // the real stuff.  There's not much we can do to correct any exception
             // situation at this point.
             IOException,
             URISyntaxException,
             ClassNotFoundException,
             NoSuchMethodException,
             InvocationTargetException,
             NoSuchAttributeException,
            InvalidAttributeValueException,
            IllegalAccessException {
 
         myOwnFileName = getMyOwnFileName();
         myOwnManifest = getMyOwnManifestAttributes();
 
         final URL originalWinstoneLocation = getOriginalWinstoneJarLocation();
         final File tmpWinstoneJar = getExtractedWinstoneJarFile(originalWinstoneLocation);
         final Method mainMethod = getWinstoneMainMethod(tmpWinstoneJar);
         final List<String> winstoneArgs = setupWinstoneArguments(args);
 
         showInfoOnStarting(winstoneArgs);
 
         // And we have a lift off!
         mainMethod.invoke(null, new Object[]{winstoneArgs.toArray(new String[0])});
     }
 
     private static void showInfoOnStarting(final List<String> arguments) {
         System.out.println("deploying: " + myOwnFileName.getName() + ":");
         StringBuilder sb = new StringBuilder();
         for (String elem : arguments) {
             sb.append(elem + " ");
         }
         System.out.println("arguments: " + sb);
     }
 
     private static void extractJarTo(final URL jar, final File tmpJar) throws IOException {
         // This could use some commons-io magic, but we want to keep the
         // dependency list slim as it would require carrying the dependencies in
         // the war and unpacking them together with Winstone.
 
         final InputStream in = jar.openStream();
         try {
 
             final OutputStream out = new FileOutputStream(tmpJar);
             try {
 
                 final byte[] buf = new byte[DEFAULT_BUFFER_SIZE];
                 int len;
                 while ((len = in.read(buf)) > 0) {
                     out.write(buf, 0, len);
                 }
 
             } finally {
                 out.close();
             }
 
         } finally {
             in.close();
         }
     }
 
     /**
      * Return a folder created in the home directory of the user,
      * named '.webroot-WAR_FILE_NAME'.
      *
      * We would have loved to create a temporary folder using something like
      * <code>File.createTempFile()</code>, but, as far as we can tell, there is
      * no way to get a temporary <i>directory</i> using the standard library.
      *
      * This seemed like a good compromise, as it is very unlikely that the user
      * will already have such a folder.  It however leaves traces behind by not
      * being deleted at shutdown.
      *
      * @return a File object pointing to a directory that we can use as the
      *         webroot parameter for Winstone
      */
     private static File getSaneWebRoot() throws IOException, URISyntaxException {
         File homeDir = new File(System.getProperty("user.home"));
         return new File(homeDir, ".webroot-" + myOwnFileName.getName());
     }
 
     private static File getMyOwnFileName() throws IOException, URISyntaxException {
         final URL classFile = App.class.getClassLoader().getResource("org/inuua/executable_war/App.class");
 
         // Trick taken from Hudson's source code (hudson-ci.org):
         // JNLP returns the URL where the jar was originally placed (like http://hudson.dev.java.net/...)
         // not the local cached file. So we need a rather round about approach to get to
         // the local file name.
         return new File(((JarURLConnection) classFile.openConnection()).getJarFile().getName());
     }
 
    private static Attributes getMyOwnManifestAttributes() throws IOException, URISyntaxException {
         URL manifest = new URL("jar:file:" + myOwnFileName.getAbsolutePath() + "!/META-INF/MANIFEST.MF");
         final InputStream mfin = manifest.openStream();
         try {
             return new Manifest(mfin).getMainAttributes();
         } finally {
             mfin.close();
         }
     }
 
     private static URL getOriginalWinstoneJarLocation() throws FileNotFoundException, NoSuchAttributeException, InvalidAttributeValueException {
 
         String winstoneJarFileName = myOwnManifest.getValue(WINSTONE_JAR_MANIFEST_ENTRY);
         if (winstoneJarFileName == null) {
             throw new NoSuchAttributeException("The Winstone jar to use is not set in the manifest. The META-INF/MANIFEST.MF file should contain an entry such as '" + WINSTONE_JAR_MANIFEST_ENTRY + ": winstone-lite-0.9.jar'.");
         } else {
             winstoneJarFileName = winstoneJarFileName.trim();
             if ("".equals(winstoneJarFileName) || !winstoneJarFileName.endsWith(".jar")) {
                 throw new InvalidAttributeValueException("The Winstone jar name was an empty string or did not look like a JAR file.  It needs to contain an entry such as 'winstone-lite-0.9.jar'.");
             }
         }
         final URL originalWinstoneJarLocation = App.class.getResource(WEB_INF_LIB_FOLDER + winstoneJarFileName);
         if (originalWinstoneJarLocation == null) {
             throw new FileNotFoundException("Could not find '" + winstoneJarFileName + "' in the " + WEB_INF_LIB_FOLDER + " directory inside the .war file.");
         }
         return originalWinstoneJarLocation;
     }
 
     private static File getExtractedWinstoneJarFile(final URL originalWinstoneJarLocation) throws IOException {
         File tmpWinstoneJar;
         try {
             tmpWinstoneJar = File.createTempFile("winstone", "jar");
         } catch (IOException e) {
             String tmpdir = System.getProperty("java.io.tmpdir");
             IOException x = new IOException("Failed to create a temporary file in '" + tmpdir + "'.");
             x.initCause(e);
             throw x;
         }
         tmpWinstoneJar.deleteOnExit();
         extractJarTo(originalWinstoneJarLocation, tmpWinstoneJar);
         return tmpWinstoneJar;
     }
 
    private static Method getWinstoneMainMethod(final File tmpWinstoneJar)
            throws NoSuchMethodException, ClassNotFoundException, MalformedURLException {
 
         // Get the Winstone Main Class
         final ClassLoader winstoneJarClassLoader = new URLClassLoader(new URL[]{tmpWinstoneJar.toURI().toURL()});
         final Class launcher = winstoneJarClassLoader.loadClass("winstone.Launcher");
         final Method mainMethod = launcher.getMethod("main", new Class[]{String[].class});
         return mainMethod;
     }
 
     private static List<String> setupWinstoneArguments(final String[] args)
             throws IOException, URISyntaxException {
 
         final List<String> arguments = new ArrayList<String>(Arrays.asList(args));
         arguments.add(0, "--warfile=" + myOwnFileName.getAbsolutePath());
         arguments.add("--webroot=" + getSaneWebRoot());
         String params = myOwnManifest.getValue(WINSTONE_JAR_MANIFEST_PARAMS);
         if (params != null) {
             params = params.trim();
             for (String e : params.split("\\s+")) {
                 arguments.add(e);
             }
         }
         return arguments;
     }
 }
