 /*
  * Utils.java
  *
  * Created on July 25, 2007, 4:19 PM
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 package com.asascience.utilities;
 
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.net.URLDecoder;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.jar.Attributes;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 import java.util.jar.Manifest;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 import java.util.zip.ZipOutputStream;
 
 import javax.swing.ImageIcon;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.RootPaneContainer;
 import javax.swing.filechooser.FileFilter;
 
 import org.apache.commons.lang.ArrayUtils;
 
 /**
  * 
  * @author cmueller
  */
 public class Utils {
 
   public enum HostOS {
 
     UNKNOWN(-1, "Unknown"), AIX(0, "AIX"), DIGITAL_UNIX(1, "Digital Unix"), FREE_BSD(2, "FreeBSD"), HP_UX(3,
     "HP UX"), IRIX(4, "Irix"), LINUX(5, "Linux"), MAC_OS(6, "Mac OS"), MAC_OSX(7, "Mac OS X"), MPE_IX(8,
     "MPE/iX"), NETWARE_4_11(9, "Netware 4.11"), OS_2(10, "OS/2"), SOLARIS(11, "Solaris"), WIN_2000(12,
     "Windows 2000"), WIN_95(13, "Windows 95"), WIN_98(14, "Windows 98"), WIN_NT(15, "Windows NT"), WIN_XP(16,
    "Windows XP"),WIN_VISTA(16,"Windows VISTA"),WIN_7(16,"Windows 7"),;
     private final int hostId;
     private final String hostName;
 
     HostOS(int hostId, String hostName) {
       this.hostId = hostId;
       this.hostName = hostName;
     }
 
     public int getHostId() {
       return hostId;
     }
 
     public String getName() {
       return hostName;
     }
   }
 
   // /**
   // * Creates a new instance of Utils
   // */
   // public Utils() {
   // }
   public static HostOS determineHostOS() {
     String osName = System.getProperty("os.name").toLowerCase();
     if (osName.startsWith("mac os x")) {
       return HostOS.MAC_OSX;
     } else if (osName.startsWith("mac os")) {
       return HostOS.MAC_OS;
    } else if (osName.startsWith("windows 7")) {
      return HostOS.WIN_7;
    } else if (osName.startsWith("windows vista")) {
      return HostOS.WIN_VISTA;
     } else if (osName.startsWith("windows xp")) {
       return HostOS.WIN_XP;
     } else if (osName.startsWith("windows 2000")) {
       return HostOS.WIN_2000;
     } else if (osName.startsWith("windows 98")) {
       return HostOS.WIN_98;
     } else if (osName.startsWith("windows 95")) {
       return HostOS.WIN_95;
     } else if (osName.startsWith("windows nt")) {
       return HostOS.WIN_NT;
     } else if (osName.startsWith("solaris")) {
       return HostOS.SOLARIS;
     } else if (osName.startsWith("os/2")) {
       return HostOS.OS_2;
     } else if (osName.startsWith("netware 4.11")) {
       return HostOS.NETWARE_4_11;
     } else if (osName.startsWith("mpe/ix")) {
       return HostOS.MPE_IX;
     } else if (osName.startsWith("linux")) {
       return HostOS.LINUX;
     } else if (osName.startsWith("irix")) {
       return HostOS.IRIX;
     } else if (osName.startsWith("hp ux")) {
       return HostOS.HP_UX;
     } else if (osName.startsWith("freebsd")) {
       return HostOS.FREE_BSD;
     } else if (osName.startsWith("digital unix")) {
       return HostOS.DIGITAL_UNIX;
     } else if (osName.startsWith("aix")) {
       return HostOS.AIX;
     } else {
       return HostOS.UNKNOWN;
     }
   }
 
   public static void setLoggingOutput(boolean useFile, PrintStream consolePs, String logPath) {
     if (useFile) {
       try {
         // File f = new File(Utils.appendSeparator(userDirectory) +
         // "sharc.log");
         File f = new File(logPath);
         if (f.exists()) {
           if (f.length() >= 10485760l)// delete the log file if it
           // exceeds 10mb
           {
             f.delete();
           }
         }
         PrintStream ps = new PrintStream(new FileOutputStream(f, true), true);
         System.setOut(ps);
         System.setErr(ps);
         System.out.println("\n\n\n-----Start New: " + new Date().toString() + "-----");
       } catch (FileNotFoundException ex) {
         Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
       }
     } else {
       System.setOut(consolePs);
       System.setErr(consolePs);
     }
   }
 
   public static String getVersionFromJarFile(Class classInJar) {
     try {
       String className = classInJar.getSimpleName();
       String classFileName = className + ".class";
       String pathToThisClass = classInJar.getResource(classFileName).toString();
 
       int mark = pathToThisClass.indexOf("!");
       String pathToManifest = pathToThisClass.toString().substring(0, mark + 1);
       pathToManifest += "/META-INF/MANIFEST.MF";
 
       String version = "Developer";
       if (pathToManifest.contains("jar:file:")) {
 
         Manifest manifest = new Manifest(new URL(pathToManifest).openStream());
         version = manifest.getMainAttributes().getValue(Attributes.Name.IMPLEMENTATION_VERSION);
       }
       return version;
     } catch (IOException ex) {
       Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
     }
     return "";
 
   }
 
   public static Class[] getClassesFromJarFile(String pckgname, File jarFile) throws ClassNotFoundException {
     ArrayList<Class> classes = new ArrayList<Class>();
     String packagePath = pckgname.replace(".", "/") + "/";
     try {
       URL[] urls = new URL[]{jarFile.toURI().toURL()};
       JarFile currentFile = new JarFile(jarFile.getAbsolutePath());
       for (Enumeration e = currentFile.entries(); e.hasMoreElements();) {
         JarEntry current = (JarEntry) e.nextElement();
         if (current.getName().length() > packagePath.length()
                 && current.getName().substring(0, packagePath.length()).equals(packagePath)
                 && current.getName().endsWith(".class")) {
           classes.add(Class.forName(current.getName().replaceAll("/", ".").replace(".class", ""), true,
                   new URLClassLoader(urls)));
         }
       }
     } catch (IOException e) {
       e.printStackTrace();
     }
 
     return classes.toArray(new Class[0]);
   }
 
   public static List<String> getClassNamesInJarFile(String packageName, File jarFile) {
     List<String> names = new ArrayList<String>();
     try {
       Class[] classes = getClassesFromJarFile(packageName, jarFile);
       for (Class c : classes) {
         if (!c.getSimpleName().equals("")) {
           if (!names.contains(c.getSimpleName())) {
             names.add(c.getSimpleName());
           }
         }
       }
 
     } catch (Exception ex) {
       // Logger.getLogger(Utils.class.getName()).
       // log(Level.SEVERE, null, ex);
       names = null;
     }
     return names;
   }
 
   public static Class[] getClassesFromJarFile(String pckgname, String baseDirPath) throws ClassNotFoundException {
     ArrayList<Class> classes = new ArrayList<Class>();
     // the JarEntry.getName() always returns a "/" separated
     // name...regardless of OS
     String packagePath = pckgname.replace(".", "/") + "/";
     File mF = new File(baseDirPath);
     String[] files = mF.list();
     List<String> jars = new ArrayList<String>();
     for (int i = 0; i < files.length; i++) {
       if (files[i].endsWith(".jar")) {
         jars.add(mF.getAbsolutePath() + "/" + files[i]);
       }
     }
     if (jars.size() == 0) {// covers the Netbeans setup
       mF = new File(Utils.appendSeparator(Utils.appendSeparator(baseDirPath) + "dist"));
       files = mF.list();
       for (int i = 0; i < files.length; i++) {
         if (files[i].endsWith(".jar")) {
           jars.add(mF.getAbsolutePath() + "/" + files[i]);
         }
       }
     }
     for (int i = 0; i < jars.size(); i++) {
       try {
         JarFile currentFile = new JarFile(jars.get(i).toString());
         for (Enumeration e = currentFile.entries(); e.hasMoreElements();) {
           JarEntry current = (JarEntry) e.nextElement();
           if (current.getName().length() > packagePath.length()
                   && current.getName().substring(0, packagePath.length()).equals(packagePath)
                   && current.getName().endsWith(".class")) {
             classes.add(Class.forName(current.getName().replaceAll("/", ".").replace(".class", "")));
           }
         }
       } catch (IOException e) {
         e.printStackTrace();
       }
     }
     Class[] classesA = new Class[classes.size()];
     classes.toArray(classesA);
     return classesA;
   }
 
   public static List<String> getClassNamesInJarFile(String packageName, String baseDirPath) {
     List<String> names = new ArrayList<String>();
     try {
       Class[] classes = getClassesFromJarFile(packageName, baseDirPath);
       for (Class c : classes) {
         if (!c.getSimpleName().equals("")) {
           if (!names.contains(c.getSimpleName())) {
             names.add(c.getSimpleName());
           }
         }
       }
 
     } catch (Exception ex) {
       // Logger.getLogger(Utils.class.getName()).
       // log(Level.SEVERE, null, ex);
       names = null;
     }
     return names;
   }
 
   public static Class[] getClassesInPackage(String packageName) throws ClassNotFoundException {
     ArrayList<Class> classes = new ArrayList<Class>();
     // Get a File object for the package
     File directory = null;
     try {
       ClassLoader cld = Thread.currentThread().getContextClassLoader();
       if (cld == null) {
         throw new ClassNotFoundException("Can't get class loader.");
       }
       String path = packageName.replace('.', '/');
       URL resource = cld.getResource(path);
       if (resource == null) {
         throw new ClassNotFoundException("No resource for " + path);
       }
       directory = new File(URLDecoder.decode(resource.getFile(), "UTF-8"));
     } catch (UnsupportedEncodingException ex) {
       Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
     } catch (NullPointerException x) {
       throw new ClassNotFoundException(packageName + " (" + directory + ") does not appear to be a valid package");
     }
     if (directory.exists()) {
       // Get the list of the files contained in the package
       String[] files = directory.list();
       for (int i = 0; i < files.length; i++) {
         // we are only interested in .class files
         if (files[i].endsWith(".class")) {
           // removes the .class extension
           classes.add(Class.forName(packageName + '.' + files[i].substring(0, files[i].length() - 6)));
         }
       }
     } else {
       throw new ClassNotFoundException(packageName + " does not appear to be a valid package");
     }
     Class[] classesA = new Class[classes.size()];
     classes.toArray(classesA);
     return classesA;
   }
 
   public static List<String> getClassNamesInPackage(String packageName) {
     List<String> names = new ArrayList<String>();
     try {
       Class[] classes = getClassesInPackage(packageName);
       for (Class c : classes) {
         if (!c.getSimpleName().equals("")) {
           if (!names.contains(c.getSimpleName())) {
             names.add(c.getSimpleName());
           }
         }
       }
 
     } catch (Exception ex) {
       Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
       names = null;
     }
     return names;
   }
 
   public static String retrieveHomeDirectory(String appName) {
     // the directory
     String userHome = null;
     try {
       userHome = System.getProperty("user.home");
     } catch (Exception e) {
       System.err.println("XMLStore.makeStandardFilename: error System.getProperty(user.home) " + e);
     }
     if (null == userHome) {
       userHome = ".";
     }
 
     // String dirFilename = userHome + File.separator + appName;
     String dirFilename = userHome + File.separator + "asascience" + File.separator + appName;
     File f = new File(dirFilename);
     if (!f.exists()) {
       f.mkdirs();
     } // now ready for file creation in writeXML
     return dirFilename;
   }
 
   public static boolean checkInvalidCharacters(String in) {
     try {
       if (in.contains("\\")) {
         return false;
       }
       if (in.contains("/")) {
         return false;
       }
       if (in.contains(":")) {
         return false;
       }
       if (in.contains("*")) {
         return false;
       }
       if (in.contains("?")) {
         return false;
       }
       if (in.contains("\"")) {
         return false;
       }
       if (in.contains("<")) {
         return false;
       }
       if (in.contains(">")) {
         return false;
       }
       if (in.contains("|")) {
         return false;
       }
 
       return true;
     } catch (Exception ex) {
       ex.printStackTrace();
     }
     return false;
   }
 
   public static void bringFrameToFront(java.awt.Frame f) {
     f.setAlwaysOnTop(true);
     f.setAlwaysOnTop(false);
   }
 
   public static void showBusyCursor(JComponent comp) {
     RootPaneContainer root = (RootPaneContainer) comp.getTopLevelAncestor();
     root.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
     root.getGlassPane().setVisible(true);
   }
 
   public static void hideBusyCursor(JComponent comp) {
     RootPaneContainer root = (RootPaneContainer) comp.getTopLevelAncestor();
     root.getGlassPane().setCursor(Cursor.getDefaultCursor());
     root.getGlassPane().setVisible(false);
   }
 
   public static void setComponetCursor(JComponent comp, Cursor cur) {
     RootPaneContainer root = (RootPaneContainer) comp.getTopLevelAncestor();
     root.getGlassPane().setCursor(cur);
     root.getGlassPane().setVisible(false);
   }
 
   public static float[] swapDimensions(float[] original, int dim1, int dim2) {
     float[] orig = original.clone();
     float[] swap = new float[orig.length];
 
     float[][] temp = new float[dim2][dim1];
     int oi = 0;
     for (int d2 = 0; d2 < dim2; d2++) {
       for (int d1 = 0; d1 < dim1; d1++) {
         temp[d2][d1] = orig[oi++];
       }
     }
 
     int si = 0;
     for (int d1 = 0; d1 < dim1; d1++) {
       for (int d2 = 0; d2 < dim2; d2++) {
         swap[si++] = temp[d2][d1];
       }
     }
 
     // System.out.println("orig,swap");
     // for(int it = 0; it < swap.length; it++) {
     // System.out.println(orig[it] + "," + swap[it]);
     // }
     return swap;
   }
 
   public static int closestPrevious(Long[] myArray, long checkVal, long tolerance) {
     int index = 0;
     while (index < myArray.length) {
       if (checkVal >= myArray[index] & checkVal < (myArray[index] + tolerance)) {
         return index;
       }
       index++;
     }
     return -1;
   }
 
   public static double[] minMaxDouble(double[] vals) {
     return minMaxDouble(vals, Double.NaN);
   }
 
   public static double[] minMaxDouble(double[] vals, double fillVal) {
     double[] ret = new double[2];
     double min = Double.POSITIVE_INFINITY;
     double max = Double.NEGATIVE_INFINITY;
     for (double d : vals) {
       if (Double.isNaN(fillVal)) {
         if (Double.isNaN(d)) {
           continue;
         }
       } else {
         if (d == fillVal) {
           continue;
         }
       }
 
       if (d > max) {
         max = d;
       }
       if (d < min) {
         min = d;
       }
     }
 
     ret[0] = min;
     ret[1] = max;
 
     return ret;
   }
 
   public static float[] minMaxFloat(float[] vals) {
     float[] ret = new float[2];
     float min = Float.POSITIVE_INFINITY;
     float max = Float.NEGATIVE_INFINITY;
     for (float d : vals) {
       if (d > max) {
         max = d;
       }
       if (d < min) {
         min = d;
       }
     }
 
     return ret;
   }
 
   public static float averageFloat(float[] vals) {
     return averageFloat(vals, Float.NaN);
   }
 
   public static float[] calculateModes(float[] numbers, Float fillVal) {
     Map<Float, Integer> table = new HashMap<Float, Integer>();
     List<Float> modes = new ArrayList<Float>();
     Integer max = 0;
     for (Float n : numbers) {
       if (Float.isNaN(fillVal)) {
         if (Float.isNaN(n)) {
           continue;
         }
       } else {
         if (n == fillVal) {
           continue;
         }
       }
       Integer frequency = table.remove(n);
       if (frequency == null) {
         frequency = 0;
       }
       table.put(n, ++frequency);
       if (frequency > max) {
         max = frequency;
         modes = new ArrayList<Float>();
       }
       if (frequency >= max) {
         modes.add(n);
       }
     }
     return ArrayUtils.toPrimitive(modes.toArray(new Float[0]));
     // return Utils.floatObjArrayToFloatPrimArray(modes.toArray(new
     // Float[]{}));
   }
 
   public static float averageFloat(float[] vals, float fillVal) {
     float sum = 0;
     for (float f : vals) {
       if (Float.isNaN(fillVal)) {
         if (Float.isNaN(f)) {
           continue;
         }
       } else {
         if (f == fillVal) {
           continue;
         }
       }
 
       sum += f;
     }
     if (sum == 0) {
       return Float.NaN;
     }
     return sum / vals.length;
   }
 
   public static double averageDouble(double[] vals) {
     double sum = 0;
     for (double d : vals) {
       sum += d;
     }
     if (sum == 0) {
       return Double.NaN;
     }
     return sum / vals.length;
   }
 
   public static Cursor createCustomCursor(String imageName, Class relClass) {
     try {
       Toolkit tk = Toolkit.getDefaultToolkit();
       ImageIcon pointer = new ImageIcon(getImageResource(imageName, relClass));
       // Dimension bestSize = tk.getBestCursorSize(pointer.getIconWidth(),
       // pointer.getIconHeight());
       // Image pointerImage =
       // ImageScaler.getOptimalScalingImage(pointer.getImage(),(int)
       // bestSize.getWidth(),
       // (int) bestSize.getHeight());
       Cursor cursor = tk.createCustomCursor(pointer.getImage(), new Point(0, 0), "CustCur");
 
       return cursor;
     } catch (Exception e) {
       // Problem finding image probably, just move on.
     }
     return null;
   }
 
   public static Cursor createCustomCursor(String imageName, Class relClass, boolean centerHotSpot) {
     try {
       Toolkit tk = Toolkit.getDefaultToolkit();
       ImageIcon pointer = new ImageIcon(getImageResource(imageName, relClass));
       // Dimension bestSize = tk.getBestCursorSize(pointer.getIconWidth(),
       // pointer.getIconHeight());
       // Image pointerImage =
       // ImageScaler.getOptimalScalingImage(pointer.getImage(),(int)
       // bestSize.getWidth(),
       // (int) bestSize.getHeight());
       int ih = pointer.getIconHeight();
       int iw = pointer.getIconWidth();
       Dimension d = tk.getBestCursorSize(iw, ih);
       Point hs;
       if (centerHotSpot) {
         hs = new Point((int) (d.getWidth() * 0.5), (int) (d.getHeight() * 0.5));
       } else {
         hs = new Point(0, 0);
       }
       Cursor cursor = tk.createCustomCursor(pointer.getImage(), hs, "CustCur");
 
       return cursor;
     } catch (Exception e) {
       // Problem finding image probably, just move on.
     }
     return null;
   }
 
   public static Color colorFromHex(String hex) {
     int cVal;
     try {
       cVal = (int) Long.parseLong(hex, 16);
     } catch (NumberFormatException nfe) {
       cVal = Long.decode(hex).intValue();
     }
     return new Color(cVal);
   }
   
   public static String getABGRFromColor(Color color) {
     return getABGRFromColor("ff",color);
   }
   
   public static String getABGRFromColor(String alpha, Color color) {
     String red = Integer.toHexString(color.getRed());
     String green = Integer.toHexString(color.getGreen());
     String blue = Integer.toHexString(color.getBlue());
 
     return  (alpha.length() == 1? "f" + alpha : alpha) + 
             (blue.length() == 1? "0" + blue : blue) +
             (green.length() == 1? "0" + green : green) +
             (red.length() == 1? "0" + red : red);
   }
 
   public static boolean comboBoxContains(javax.swing.ComboBoxModel model, Object o) {
     int size = model.getSize();
     for (int i = 0; i < size; i++) {
       Object obj = model.getElementAt(i);
       if (obj.equals(o)) {
         return true;
       }
     }
     return false;
   }
 
   public static List<String> parseDelimitedString(String input, String delimiter) {
     List<String> ret = new ArrayList<String>();
     String s;
     int start = 0, end = 0;
     while (end != -1 & end < input.length()) {
       end = input.indexOf(delimiter, end);
       if (end == -1) {
         s = input.substring(start);
       } else {
         s = input.substring(start, end);
         end++;
       }
 
       start = end;
       ret.add(s);
     }
 
     return ret;
   }
 
   public static String appendExtension(String inString, String extension) {
     return (inString.endsWith(extension)) ? inString : inString
             + ((extension.startsWith(".")) ? extension : "." + extension);
   }
 
   public static String appendExtension(File inFile, String extension) {
     String fileString = inFile.getAbsolutePath();
     return Utils.appendExtension(fileString, extension);
   }
 
   public static String appendSeparator(String inString) {
     return (inString.endsWith(File.separator)) ? inString : inString + File.separator;
   }
 
   public static String appendSeparator(File inFile) {
     String fileString = inFile.getAbsolutePath();
     return Utils.appendSeparator(fileString);
   }
 
   /**
    * Creates a unique filename by appending an incrementing a number at the
    * end of the desired root name of the file.
    *
    * @param inName
    *            - The desired root name for the file
    * @param ext
    *            - The extention to append to the file, use null for an
    *            incrementing directory
    * @return The absolute path name for file
    */
   public static File createIncrementalName(String inName, String ext) {
     return createIncrementalName("", inName, ext);
   }
 
   /**
    * Creates a unique filename by appending an incrementing a number at the
    * end of the desired root name of the file.
    *
    * @param dir
    *            - The directory of the file
    * @param inName
    *            - The desired root name for the file
    * @param ext
    *            - The extention to append to the file, use null for an
    *            incrementing directory
    * @return The absolute path name for file
    */
   public static File createIncrementalName(String dir, String inName, String ext) {
     File outFile = null;
     try {
       if (!dir.endsWith(File.separator)) {
         dir = dir + File.separator;
       }
       if (ext == null) {
         ext = "";
       }
       if (!ext.equals("") && !ext.startsWith(".")) {
         ext = "." + ext;
       }
 
       String testName = dir + inName + ext;
       outFile = new File(testName);
       int j = 1;
       while (outFile.exists()) {
         testName = dir + inName + j + ext;
         outFile = new File(testName);
         j++;
       }
 
       // outName = testName;
     } catch (Exception ex) {
       ex.printStackTrace();
     }
 
     return outFile;
   }
 
   /**
    * Retrieves an image resource relative to a class
    *
    * @param imageName
    * @param relClass
    * @return
    */
   public static Image getImageResource(String imageName, Class relClass) {
     File f;
     URL url = relClass.getResource(imageName);
     if (url == null) {
       // f = new File("images" + File.separator + imageName);
       // f = new File("images/" + imageName);
       // url = relClass.getResource(f.toString());
       url = relClass.getResource("images/" + imageName);
       // url = relClass.getResource("images" + File.separator +
       // imageName);
     }
     if (url != null) {
       Toolkit tk = Toolkit.getDefaultToolkit();
       Image img = tk.getImage(url);
       return img;
     }
     return null;
   }
 
   /**
    * Returns an ImageIcon, or null if the path was invalid.
    *
    * @param path
    * @return
    */
   protected static ImageIcon createImageIcon(String path) {
     java.net.URL imgURL = Utils.class.getResource(path);
     if (imgURL != null) {
       return new ImageIcon(imgURL);
     } else {
       System.err.println("Couldn't find file: " + path);
       return null;
     }
   }
 
   /*
    * Get the extension of a file.
    */
   public static String getExtension(File f) {
     String ext = null;
     String s = f.getName();
     int i = s.lastIndexOf('.');
 
     if (i > 0 && i < s.length() - 1) {
       ext = s.substring(i + 1).toLowerCase();
     }
     return ext;
   }
 
   public static String replaceExtension(File f, String ext) {
     String e = getExtension(f);
     String path = f.getAbsolutePath();
 
     return path.replace("." + e, ext);
   }
 
   public static double[] doubleListArrayToDoublePrimArray(List<Double> in) {
     double[] ret = new double[in.size()];
     for (int i = 0; i < in.size(); i++) {
       ret[i] = in.get(i);
     }
     return ret;
   }
 
   public static double[] doubleObjArrayToDoublePrimArray(Double[] in) {
     double[] ret = new double[in.length];
     for (int i = 0; i < in.length; i++) {
       ret[i] = in[i];
     }
     return ret;
   }
 
   public static float[] floatListArrayToFloatPrimArray(List<Float> in) {
     float[] ret = new float[in.size()];
     for (int i = 0; i < in.size(); i++) {
       ret[i] = in.get(i);
     }
     return ret;
   }
 
   public static float[] floatObjArrayToFloatPrimArray(Float[] in) {
     float[] ret = new float[in.length];
     for (int i = 0; i < in.length; i++) {
       ret[i] = in[i];
     }
     return ret;
   }
 
   public static float[] getSubsetArrayFloat(float[] in, int start, int skip) {
     // float[] out = new float[(in.length-(start))/skip];
     List<Float> out = new ArrayList<Float>();
     int i = start;
     // int j = 0;
     while (i < in.length) {
       out.add(in[i]);
       // j++;
       i += skip;
     }
     return ArrayUtils.toPrimitive(out.toArray(new Float[0]));
     // return Utils.floatObjArrayToFloatPrimArray(out.toArray(new
     // Float[0]));
   }
 
   public static float[] doubleArrayToFloatArray(double[] in) {
     float[] out = new float[in.length];
     for (int i = 0; i < in.length; i++) {
       if (Double.isNaN(in[i])) {
         out[i] = Float.NaN;
       } else {
         out[i] = (float) in[i];
       }
     }
     return out;
   }
 
   public static double[] floatArrayToDoubleArray(float[] in) {
     double[] out = new double[in.length];
     for (int i = 0; i < in.length; i++) {
       if (Float.isNaN(in[i])) {
         out[i] = Double.NaN;
       } else {
         out[i] = (double) in[i];
       }
     }
     return out;
   }
 
   /**
    * Rounds the double value to the nearest value of 'nearest'. The
    * 'roundDirection' parameter indicates which direction to force rounding. A
    * '0' rounds naturally (up for values > 0.5, down for values < 0.5). A
    * number > 0 forces a round up, while < 0 forces a round down.
    * <p>
    * Examples:
    * <p>
    * The code <br>
    * <i> double val = roundToNearest(102.536, 0.05, 1); </i><br>
    * results in <i>val</i> having a value of: <b>102.55</b>
    * <p>
    * The code <br>
    * <i> double val = roundToNearest(102.536, 50, -1); </i><br>
    * results in <i>val</i> having a value of: <b>100</b>
    *
    * @param value
    *            the input value.
    * @param nearest
    *            the value of the nearest number to round to
    * @param roundDirection
    *            the direction to force rounding. <i>0</i> uses natural
    *            rounding, <i><0</i> always rounds up, <i>>0</i> always rounds
    *            down.
    * @return the rounded number
    */
   public static double roundToNearest(double value, double nearest, int roundDirection) {
     if (nearest == 0) {
       return value;
     }
     if (roundDirection < 0) {
       value = value - (nearest * 0.5);// round down
     } else if (roundDirection > 0) {
       value = value + (nearest * 0.5);// round up
     }
     double ret = Math.round(value / nearest) * nearest;
     return ret;
   }
 
   public static double roundDouble(double value, int precision) {
     double sign = (value >= 0) ? 1 : -1;
     double factor = Math.pow(10, precision);
     double n = value * factor;
 
     n = sign * Math.abs(Math.floor(n + 0.5));
 
     return n / factor;
   }
 
   public static double roundDouble_old_slow(double value, int precision) {
     if (Double.isNaN(value)) {
       return value;
     }
     if (Double.isInfinite(value)) {
       return value;
     }
     double ret = 0;
     try {
       StringBuilder sbFormat = new StringBuilder();
       StringBuilder sbAppend = new StringBuilder();
       sbFormat.append("#########0.");
       for (int i = 0; i < precision; i++) {
         sbAppend.append("0");
       }
       if (value < 0.0001) {
         sbFormat.delete(0, sbFormat.length());
         sbFormat.append("0.");
         sbFormat.append(sbAppend.toString());
         sbFormat.append("E0");
       } else {
         sbFormat.append(sbAppend.toString());
       }
       // String sFormat = "#########0.";
       // String append = "";
       // for(int i = 0; i < precision; i++){
       // append += "0";
       // }
       // if(value < 0.0001){
       // sFormat = "0." + append + "E0";
       // }else{
       // sFormat += append;
       // }
 
       DecimalFormat df = new DecimalFormat(sbFormat.toString());
 
       ret = new Double(df.format(value)).doubleValue();
     } catch (Exception ex) {
       ex.printStackTrace();
       ret = value;
     }
 
     return ret;
   }
 
   public static boolean isNumeric(Object value) {
     try {
       double d = Double.parseDouble((String) value);
       return true;
     } catch (NumberFormatException ex) {
       // logger.warning("isNumeric failed for text: " + (String)value);
     }
     return false;
   }
 
   public static boolean deleteDirectory(File path) {
     if (path.exists()) {
       File[] files = path.listFiles();
       for (int i = 0; i < files.length; i++) {
         if (files[i].isDirectory()) {
           deleteDirectory(files[i]);
         } else {
           files[i].delete();
         }
       }
     }
     return (path.delete());
   }
 
   public static long getTimeInMilliseconds(double time, String units) {
     long timeIncrement = 0;
     if (units.contains("hour") || units.contains("hours") || units.contains("hrs") || units.contains("hr")) {
       timeIncrement = (long) (time * 60 * 60 * 1000);
     } else if (units.contains("minute") || units.contains("minutes") || units.contains("mins")
             || units.contains("min")) {
       timeIncrement = (long) (time * 60 * 1000);
     }
     return timeIncrement;
   }
 
   // If targetLocation does not exist, it will be created.
   public static void copyDirectory(File in, File out) throws IOException {
 
     if (in.isDirectory()) {
       if (!out.exists()) {
         out.mkdir();
       }
 
       String[] children = in.list();
       for (int i = 0; i < children.length; i++) {
         copyDirectory(new File(in, children[i]), new File(out, children[i]));
       }
     } else {
       try {
         copyFile(in, out);
       } catch (Exception ex) {
         Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
       }
     }
   }
 
   public static void copyDirectory(File in, File out, FileFilter acceptFilter) {
     if (in.isDirectory()) {
       if (!out.exists()) {
         out.mkdir();
       }
 
       String[] children = in.list();
       for (int i = 0; i < children.length; i++) {
         copyDirectory(new File(in, children[i]), new File(out, children[i]), acceptFilter);
       }
     } else {
       try {
         if (acceptFilter.accept(in)) {// If the file is accepted,
           // included it
           copyFile(in, out);
         }
       } catch (Exception ex) {
         Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
       }
     }
   }
 
   public static void copyFile(File in, File out) throws Exception {
     FileInputStream fis = new FileInputStream(in);
     FileOutputStream fos = new FileOutputStream(out);
     try {
       byte[] buf = new byte[1024];
       int i = 0;
       while ((i = fis.read(buf)) != -1) {
         fos.write(buf, 0, i);
       }
     } catch (Exception e) {
       throw e;
     } finally {
       if (fis != null) {
         fis.close();
       }
       if (fos != null) {
         fos.close();
       }
     }
   }
 
   /** Zip the contents of the directory, and save it in the zipfile */
   public static void zipDirectory(String dir, String zipFile) throws IOException, IllegalArgumentException {
     // Check that the directory is a directory, and get its contents
     File d = new File(dir);
     if (!d.isDirectory()) {
       throw new IllegalArgumentException("Not a directory:  " + dir);
     }
     String[] entries = d.list();
     String parentDir = d.getParent();
     byte[] buffer = new byte[4096]; // Create a buffer for copying
     int bytesRead;
 
     ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));
 
     for (int i = 0; i < entries.length; i++) {
       File f = new File(d, entries[i]);
       if (f.isDirectory()) {
         zipDirectory(f.getAbsolutePath(), zipFile);
         continue;
       }
       FileInputStream in = new FileInputStream(f); // Stream to read file
       ZipEntry entry = new ZipEntry(f.getPath().replace(parentDir, "")); // Make
       // a
       // ZipEntry
       out.putNextEntry(entry); // Store entry
       while ((bytesRead = in.read(buffer)) != -1) {
         out.write(buffer, 0, bytesRead);
       }
       in.close();
     }
     out.close();
   }
 
   /** Zip the contents of the directory, and save it in the zipfile */
   public static void zipDirectory(String dir, ZipOutputStream out, FileFilter acceptFilter, String addParent)
           throws IOException, IllegalArgumentException {
     // Check that the directory is a directory, and get its contents
     File d = new File(dir);
     if (!d.isDirectory()) {
       throw new IllegalArgumentException("Not a directory:  " + dir);
     }
     String parentDir = d.getParent();
     String[] entries = d.list();
     byte[] buffer = new byte[4096]; // Create a buffer for copying
     int bytesRead;
 
     if (addParent != null) {
       parentDir = parentDir.replace(addParent, "");
     }
     String replaceText = Utils.appendSeparator(parentDir);
 
     for (int i = 0; i < entries.length; i++) {
       File f = new File(d, entries[i]);
       if (f.isDirectory()) {
         zipDirectory(f.getAbsolutePath(), out, acceptFilter, d.getName());
         continue;
       }
       if (acceptFilter.accept(f)) {
         FileInputStream in = new FileInputStream(f); // Stream to read
         // file
         ZipEntry entry = new ZipEntry(f.getPath().replace(replaceText, "")); // Make
         // a
         // ZipEntry
         out.putNextEntry(entry); // Store entry
         while ((bytesRead = in.read(buffer)) != -1) {
           out.write(buffer, 0, bytesRead);
         }
         in.close();
       }
     }
     // out.close();
   }
 
   public static void zipDirectory(String dir, String zipFile, FileFilter acceptFilter) throws IOException,
           IllegalArgumentException {
     ZipOutputStream out = null;
     try {
       out = new ZipOutputStream(new FileOutputStream(zipFile));
       Utils.zipDirectory(dir, out, acceptFilter, null);
     } catch (Exception ex) {
     } finally {
       if (out != null) {
         out.close();
       }
     }
   }
 
   public static void unzipDirectory(String zipFile, String dir, String oldDirName, String newDirName)
           throws IOException {
     final int buffer = 2048;
     BufferedOutputStream dest = null;
     FileInputStream fis = new FileInputStream(zipFile);
     ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
     ZipEntry entry;
     String name;
     HostOS os = Utils.determineHostOS();
     while ((entry = zis.getNextEntry()) != null) {
       int count;
       byte data[] = new byte[buffer];
       name = entry.getName().replace(oldDirName, newDirName);
       if (os == HostOS.MAC_OSX) {
         name = name.replace("\\", "/");
       }
       // ignore .DS_Store files
       if (name.contains(".DS_Store")) {
         continue;
       }
       // write the files to the disk
       File f = new File(dir, name);
       // when the entry is a directory
       if (entry.isDirectory()) {
         f.mkdirs();
       } else {
         // make sure the parent file exists
         if (!f.getParentFile().exists()) {
           f.getParentFile().mkdirs();
         }
         FileOutputStream fos = new FileOutputStream(f.getAbsolutePath());
         dest = new BufferedOutputStream(fos, buffer);
         while ((count = zis.read(data, 0, buffer)) != -1) {
           dest.write(data, 0, count);
         }
         dest.flush();
         dest.close();
       }
     }
     zis.close();
   }
 
   public static void unzipDirectory(String zipFile, String dir) throws IOException {
     final int buffer = 2048;
     BufferedOutputStream dest = null;
     FileInputStream fis = new FileInputStream(zipFile);
     ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
     ZipEntry entry;
     HostOS os = Utils.determineHostOS();
     while ((entry = zis.getNextEntry()) != null) {
       int count;
       byte data[] = new byte[buffer];
       String name = entry.getName();
       if (os == Utils.HostOS.MAC_OSX) {
         name = name.replace("\\", "/");
       }
       // ignore .DS_Store files
       if (name.contains(".DS_Store")) {
         continue;
       }
       // write the files to the disk
       File f = new File(dir, name);
       // when the entry is a directory
       if (entry.isDirectory()) {
         f.mkdirs();
       } else {
         // make sure the parent file exists
         if (!f.getParentFile().exists()) {
           f.getParentFile().mkdirs();
         }
         FileOutputStream fos = new FileOutputStream(f.getAbsolutePath());
         dest = new BufferedOutputStream(fos, buffer);
         while ((count = zis.read(data, 0, buffer)) != -1) {
           dest.write(data, 0, count);
         }
         dest.flush();
         dest.close();
       }
     }
     zis.close();
   }
 
   public static boolean checkForZipEntry(String zipFile, String queryFile) {
     FileInputStream fis = null;
     ZipInputStream zis = null;
     try {
       fis = new FileInputStream(zipFile);
       zis = new ZipInputStream(new BufferedInputStream(fis));
       ZipEntry entry;
       String eName;
       while ((entry = zis.getNextEntry()) != null) {
         eName = entry.getName().toLowerCase();
         if (eName.contains(queryFile.toLowerCase())) {
           return true;
         }
       }
     } catch (IOException ex) {
       Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
     } finally {
       try {
         if (zis != null) {
           zis.close();
         }
         if (fis != null) {
           fis.close();
         }
       } catch (IOException ex) {
         Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
       }
     }
 
     return false;
   }
 
   public static boolean directoryContainsFile(File directory, String searchFileName) {
     for (File f : directory.listFiles()) {
       if (f.getName().toLowerCase().equals(searchFileName.toLowerCase())) {
         return true;
       }
     }
     return false;
   }
 
   public static Point centerFrameOnScreen(JFrame frame) {
     java.awt.Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
 
     int x = (int) ((dim.getWidth() / 2) - (frame.getWidth() / 2));
     int y = (int) ((dim.getHeight() / 2) - (frame.getHeight() / 2));
 
     return new Point(x, y);
   }
 
   // public final static int swapInt(int v) {
   // return (v >>> 24) | (v << 24) |
   // ((v << 8) & 0x00FF0000) | ((v >> 8) & 0x0000FF00);
   // }
   // public final static float swapFloat(float v){
   // return
   // Float.intBitsToFloat(Integer.reverseBytes(Float.floatToIntBits(v)));
   // }
   // public final static short swapShort(short v){
   // return Short.reverseBytes(v);
   // }
   public static class Memory {
 
     public static int BYTE = 0;
     public static int MEGABYTE = 1;
     public static int GIGABYTE = 2;
 
     public static double freeMemoryAs(int sizeType) {
       long freeMem = java.lang.Runtime.getRuntime().freeMemory();
       switch (sizeType) {
         case 0:// bytes
           break;
         case 1:// mb
           return Convert.bytesToMegabytes(freeMem);
         case 2:// gb
           return Convert.bytesToGigabytes(freeMem);
       }
 
       return Double.NaN;
     }
 
     public static double totalMemoryAs(int sizeType) {
       long freeMem = java.lang.Runtime.getRuntime().totalMemory();
       switch (sizeType) {
         case 0:// bytes
           break;
         case 1:// mb
           return Convert.bytesToMegabytes(freeMem);
         case 2:// gb
           return Convert.bytesToGigabytes(freeMem);
       }
 
       return Double.NaN;
     }
 
     public static class Convert {
 
       public static double bytesToMegabytes(long bytes) {
         return (bytes * 9.53674316e-7);
       }
 
       public static double megabytesToBytes(double megabytes) {
         return (megabytes * 1048576);
       }
 
       public static double bytesToGigabytes(long bytes) {
         return (bytes * 9.31322575e-10);
       }
 
       public static double gigabytesToBytes(double gigabytes) {
         return (gigabytes * 1073741824);
       }
 
       public static double megabytesToGigabytes(double megabytes) {
         return (megabytes * 0.0009765625);
       }
 
       public static double gigabytesToMegabytes(double gigabytes) {
         return (gigabytes * 1024);
       }
     }
   }
 }
