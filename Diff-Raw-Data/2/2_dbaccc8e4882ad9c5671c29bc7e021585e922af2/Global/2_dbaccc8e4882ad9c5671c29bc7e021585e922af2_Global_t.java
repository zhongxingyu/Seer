 package org.powerbat.configuration;
 
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 import org.powerbat.gui.Splash;
 import org.powerbat.methods.Updater;
 
 /**
  * Used for file, directory and image creations. Contains information regarding
  * URLs, operating system information and file directories.
  *
  * @author Naux
  * @see URLs
  * @see Paths
  * @since 1.0
  */
 public class Global {
 
     private static BufferedImage[] imgs;
 
     public static final int SPLASH_IMAGE = 0;
     public static final int ICON_IMAGE = 1;
     public static final int CLOSE_IMAGE = 2;
     public static final int COMPLETE_IMAGE = 3;
 
     /**
      * Loads the images. Preferably only ran once in the boot to avoid
      * unnecessary usage.
      * <p/>
      * You can load these images through {@link Global#getImage(int)
      * getImage(int)}
      *
      * @see Global#getImage(int)
      * @since 1.0
      */
 
     public static void loadImages() {
         imgs = new BufferedImage[URLs.IMAGES.length];
         for (int i = 0; i < URLs.IMAGES.length; i++) {
             try {
                 System.out.println(Global.class.getResource("/resources/splash.png"));
                 imgs[i] = ImageIO.read(Global.class.getResourceAsStream(URLs.IMAGES[i]));
             } catch (Exception e) {
                 e.printStackTrace();
             }
 
         }
     }
 
     /**
      * Used to return the loaded images. These are primarily for internal use
      * only.
      *
      * TODO: Pack images
      *
      * @param num The image instance to load.
      * @return Image instance from pre-loaded or defined images.
      * @see Global#CLOSE_IMAGE
      * @see Global#ICON_IMAGE
      * @see Global#SPLASH_IMAGE
      * @since 1.0
      */
 
     public static Image getImage(int num) {
         if (num >= 0 && num <= 3) {
             return imgs[num];
         }
         throw new IndexOutOfBoundsException("Invalid input");
     }
 
     /**
      * For path configurations. Depends on {@link OS} for certain dependencies.
      *
      * @since 1.0
      */
 
     public static class Paths {
 
         public static final String APP_DATA = getAppData();
         public static final String HOME = APP_DATA + File.separator + "Powerbat";
         public static final String SOURCE = HOME + File.separator + "src";
         public static final String ARRAY = SOURCE + File.separator + "array";
         public static final String STRING = SOURCE + File.separator + "string";
         public static final String LOGIC = SOURCE + File.separator + "logic";
         public static final String AP = SOURCE + File.separator + "AP";
         public static final String RECURSIVE = SOURCE + File.separator + "recursive";
         public static final String SETTINGS = HOME + File.separator + "data";
         public static final String JAVA = SETTINGS + File.separator + "java";
         public static final String[] PATHS = new String[]{APP_DATA, HOME, SOURCE, ARRAY, STRING, LOGIC, AP, RECURSIVE, SETTINGS, JAVA};
 
         /**
          * Used to build external folder sets. Internal use only.
          *
          * @since 1.0
          */
 
         public static void build() {
             for (final String s : PATHS) {
                 final File f = new File(s);
                 if (!f.exists()) {
                     Splash.setStatus("Creating Directory: " + s);
                     f.mkdir();
                 }
             }
             final File complete = new File(SETTINGS + File.separator + "data.dat");
             if (!complete.exists()) {
                 try {
                     complete.createNewFile();
                 } catch (IOException e) {
                     System.err.println("Failed to create data file.");
                     System.err.println("Completion progress will not be saved, code still will be.");
                 }
             }
         }
     }
 
     /**
      * The default URL's for all of Powerbat's necessities such as images,
      * version information and packaged Runners.
      *
      * @see {@link Updater#update()}
      * @since 1.0
      */
 
     public static class URLs {
 
        public static final String BIN = "https://github.com/Naux/Powerbat/blob/master/runners/";
         public static final String RESOURCES = "/resources/";
         public static final String SPLASH = RESOURCES + "splash.png";
         public static final String ICON = RESOURCES + "icon.png";
         public static final String CLOSE = RESOURCES + "close.png";
         public static final String COMPLETE = RESOURCES + "complete.png";
         public static final String[] IMAGES = new String[]{SPLASH, ICON, CLOSE, COMPLETE};
     }
 
     /**
      * To switch and find the current Operating system.
      *
      * @see Global#getAppData()
      * @see Global#getOS()
      * @since 1.0
      */
     public enum OS {
         WINDOWS, MAC, LINUX, OTHER
     }
 
     /**
      * Returns the location of the parent storage directory.
      *
      * @return <tt>String</tt> representation of the <tt>AppData</tt> or
      *         <tt>user.home</tt> directory
      * @see Global#getOS()
      * @since 1.0
      */
     public static String getAppData() {
         return getOS() == OS.WINDOWS ? System.getenv("APPDATA") : System.getProperty("user.home");
     }
 
     /**
      * Returns the OS system for basic configuration.
      *
      * @return Returns the <tt>enumerator</tt> of type {@link OS}
      * @see Global#getAppData()
      * @since 1.0
      */
     public static OS getOS() {
         String os = System.getProperty("os.name").toLowerCase();
         if (os.contains("windows")) {
             return OS.WINDOWS;
         }
         if (os.contains("mac")) {
             return OS.MAC;
         }
         if (os.contains("linux")) {
             return OS.LINUX;
         }
         return OS.OTHER;
     }
 }
