 /*
  Project: dbx-capture
  File: Platforming.java (com.dividebyxero.dbxcapture)
  Author: Alex Kersten
  */
 package com.dividebyxero.dbxcapture;
 
 import com.dividebyxero.dbxcapture.binaries.BinaryLoader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import javax.swing.JOptionPane;
 
 /**
  * This file takes care of all platform-dependant stuff (within reason) that we
  * can from within Java. It'll automatically set the platform value in the
  * DBXCapture class, and attempt to statically load the correct dynamic library.
  *
  * @author Alex Kersten
  */
 public abstract class Platforming {
 
     /**
      * Copies a file out of the com.dividebyxero.dbxcapture.binaries package and
      * writes it into the home working directory of DBXCapture.
      *
      * We obtain an InputStream to the file and then copy that stream to a
      * FileOutputStream on disk.
      *
      * @param nameInBinaryPackage The name of the file in the JAR's binaries
      * package
      * @param nameOnDisk The name for the file on disk
      * @return If the operation completed successfully
      */
     public static boolean copyFileFromBinariesToDisk(
             String nameInBinaryPackage, String nameOnDisk) {
         InputStream is =
                     BinaryLoader.class.getResourceAsStream(nameInBinaryPackage);
         try {
             FileOutputStream fout = new FileOutputStream(new File(nameOnDisk));
 
             byte buf[] = new byte[1024];
             int len;
             while ((len = is.read(buf)) != -1) {
                 fout.write(buf, 0, len);
             }
 
             fout.close();
             is.close();
         } catch (IOException ioe) {
             return false;
         }
 
         return true;
     }
 
     /**
      * The first thing we'll will do is make sure we've got a directory to write
      * in the user's home directory, and create it if not. It will also
      * determine what platform we're on, set the platform variable in DBXCapture
      * and load the native library.
      *
      * This method also needs to create ~/dbx/DBXCapture/scripts (in turn
      * creating ~/dbx/DBXCapture which is where the libraries will be) and write
      * the default script bytecode to the scripts directory and the Windows
      * native libraries (if Windows is our platform) to the latter directory.
      *
      * This is the first method that runs, and it's in Platforming.java because
      * the Platform enum is here and DBXCRuntime was getting a little cluttered.
      */
     public static void staticInitialize() {
         //Check if the user directory exists, and make it if it doesn't.
         Path workingDir = Paths.get(DBXCRuntime.PROGRAM_HOME);
 
         if (!Files.isDirectory(workingDir)) {
             try {
                 Files.createDirectories(workingDir);
             } catch (IOException ex) {
                 JOptionPane.showMessageDialog(
                         null,
                         "Couldn't create directory: "
                         + workingDir.toAbsolutePath().toString()
                         + "\nCheck the permissions on your home directory.",
                         "DBXC- Error", JOptionPane.ERROR_MESSAGE);
 
                 System.exit(1);
             }
         }
 
         String platformString = System.getProperty("os.name");
         boolean is64 = (System.getProperty("os.arch").indexOf("64") >= 0);
 
         //We won't differentiate 64/32 on *NIX or Mac since we're not going to
         //be providing the compiled library for those platforms anyway, and
         //users will probably compile it themselves for those platforms so the
         //word width doesn't really matter - it'll be running locally and most
         //people will probably compile with the correct word length if they're
         //planning on having it work.
 
         //Determine platform
         if (platformString.toLowerCase().indexOf("win") >= 0) {
             DBXCapture.platform = Platform.WIN;
         } else if (platformString.toLowerCase().indexOf("mac") >= 0) {
             DBXCapture.platform = Platform.MAC;
         } else if (platformString.toLowerCase().indexOf("nix") >= 0
                    || platformString.toLowerCase().indexOf("linux") >= 0) {
             DBXCapture.platform = Platform.NIX;
         } else {
             //Probably some weird unix variant, eh, just load the shared object
             //file, it'll probably work if they bothered to actually compile it.
             DBXCapture.platform = Platform.NIX;
         }
 
         //If the architecture isn't x86 or amd64, they'll probably have to build
         //their own native library - tell them this if the native library load
         //fails and the architecture isn't one of these. Otherwise suggest that
         //the native library doesn't exist, which will be the case for non-
         //Windows platforms unless they roll their own from the native source.
 
         //If the platform is Windows, drop the correct DLL into the user working
         //directory so that people don't have to compile it/move it there (and
         //so that the actual dbxc.exe can move around and be portable - it just
         //looks in one place for these files, the user home directory).
 
         //This is hand-holding, but without it Windows folk won't run the
         //program at all.
         if (DBXCapture.platform == Platform.WIN) {
             Path testDLL =
                  Paths.get(DBXCRuntime.PROGRAM_HOME + "/"
                            + DBXCapture.platform.getLibName());
 
             if (!Files.exists(testDLL)) {
                 if (!copyFileFromBinariesToDisk(
                         is64 ? "libcapture64.bin" : "libcapture32.bin",
                        DBXCRuntime.PROGRAM_HOME
                         + DBXCapture.platform.getLibName())) {
 
                     JOptionPane.showMessageDialog(
                             null,
                             "Couldn't write library file. Check home directory "
                             + "permissions.", "DBXCapture - Error",
                             JOptionPane.ERROR_MESSAGE);
 
                     System.exit(1);
                 }
             }
         }
 
 
         try {
 
             System.load(DBXCRuntime.PROGRAM_HOME + "/"
                         + DBXCapture.platform.getLibName());
 
         } catch (Throwable e) {
             String arch = System.getProperty("os.arch");
 
             if (!((arch.toLowerCase().indexOf("x86") >= 0)
                   || (arch.toLowerCase().indexOf("amd64") >= 0))) {
                 JOptionPane.showMessageDialog(
                         null,
                         "Couldn't load the native "
                         + "library.\nIt looks like you're not running on x86 or"
                         + " amd64, and will need to build your own library.\n"
                         + "You can download the source off of akersten's GitHub"
                         + "page - the project is named dbx-libcapture.\n\nYou "
                         + "will need to place a resulting file named "
                         + DBXCapture.platform.getLibName() + " into the "
                         + "directory " + DBXCRuntime.PROGRAM_HOME,
                         "DBXCapture - Error",
                         JOptionPane.ERROR_MESSAGE);
             } else {
                 JOptionPane.showMessageDialog(
                         null,
                         "Couldn't load the native "
                         + "library " + DBXCapture.platform.getLibName() + " in "
                         + "the directory " + DBXCRuntime.PROGRAM_HOME + "\n\n"
                         + "One of the following is true:\n- It does not exist"
                         + "\n- It is not binary compatible with your system"
                         + "\n- It is corrupt"
                         + "\n- It was compiled or linked incorrectly"
                         + "\n\nMore information in the next error message...",
                         "DBXCapture - Error",
                         JOptionPane.ERROR_MESSAGE);
             }
 
             JOptionPane.showMessageDialog(
                     null,
                     e.getLocalizedMessage(),
                     "DBXCapture - Error",
                     JOptionPane.ERROR_MESSAGE);
 
             System.exit(1);
         }
     }
 
     public enum Platform {
         //32- vs 64-bit naming is only in the binaries package - when it gets
         //written out to disk, it's named the same.
 
         WIN("Windows", "libcapture.dll"),
         NIX("*NIX", "libcapture.so"),
         MAC("Mac", "libcapture.so");
 
         private String platformName, libName;
 
         Platform(String platformName, String libName) {
             this.platformName = platformName;
             this.libName = libName;
         }
 
         /**
          * Each platform might have a differently named capture library, so
          * return it here.
          *
          * @return The name of the libcapture library in the working directory.
          */
         public String getLibName() {
             return libName;
         }
 
         /**
          * Gets the user-friendly name of the system platform.
          *
          * @return the platformName
          */
         public String getPlatformName() {
             return platformName;
         }
     }
 }
