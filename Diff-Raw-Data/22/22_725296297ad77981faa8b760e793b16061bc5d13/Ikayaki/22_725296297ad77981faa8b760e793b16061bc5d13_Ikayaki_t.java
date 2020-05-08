 /*
  * Ikayaki.java
  *
  * Copyright (C) 2005 Project SQUID, http://www.cs.helsinki.fi/group/squid/
  *
  * This file is part of Ikayaki.
  *
  * Ikayaki is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * Ikayaki is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Ikayaki; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  */
 
 package ikayaki;
 
 import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
 import com.jgoodies.looks.plastic.PlasticLookAndFeel;
 import com.jgoodies.looks.plastic.theme.SkyBlue;
 import ikayaki.gui.MainViewPanel;
 import ikayaki.util.LoggerPrintStream;
 import jutil.JUtil;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.PrintStream;
 import java.util.Date;
 
 /**
  * Starts the program. Lays out MainViewPanel, MainMenuBar and MainStatusBar in a JFrame.
  *
  * @author Esko Luontola
  */
 public class Ikayaki extends JFrame {
 
     /* Application information */
 
     public static final String APP_NAME = "Ikayaki";
    public static final String APP_VERSION = "1.0.1";
     public static final String APP_BUILD = "2005-05-06";
     public static final String APP_HOME_PAGE = "http://www.cs.helsinki.fi/group/squid/";
 
     public static final String FILE_TYPE = ".ika";
     public static final String FILE_DESCRIPTION = "Ikayaki Project File";
 
     public static final String[] AUTHORS = new String[]{
         "Mikko Jormalainen",
         "Samuli Kaipiainen",
         "Aki Korpua",
         "Esko Luontola",
         "Aki Sysmlinen"
     };
 
     /* Application files and directories */
 
     public static final File STARTUP_DIRECTORY = new File(System.getProperty("user.dir")).getAbsoluteFile();
     public static final String PROGRAM_JAR_NAME = "ikayaki.jar";
 
     static {
         // change to the program directory if it was started somewhere else
         String[] paths = System.getProperty("java.class.path").split(System.getProperty("path.separator"));
         for (String s : paths) {
             File file = new File(s);
             if (file.getName().equals(PROGRAM_JAR_NAME)) {
                 file = file.getAbsoluteFile();
                 System.setProperty("user.dir", file.getParent());
                 break;
             }
         }
     }
 
     public static final File PROPERTIES_FILE = new File("ikayaki.config").getAbsoluteFile();
     public static final File SEQUENCES_FILE = new File("ikayaki.sequences").getAbsoluteFile();
     public static final File CALIBRATION_PROJECT_DIR = new File("calibration").getAbsoluteFile();
     public static final File DEBUG_LOG_DIR = new File("logs").getAbsoluteFile();
     public static final File DEBUG_LOG_FILE = new File("debug.log").getAbsoluteFile();
     public static final String HELP_PAGES = new File("manual/index.html").getAbsolutePath();
 
     /**
      * Starts the program with the provided command line parameters. If the location of a project file is given as a
      * parameter, the program will try to load it.
      *
      * @param args command line parameters.
      */
     public static void main(String[] args) {
 
         // clean up log files
         logFileCleanup(DEBUG_LOG_FILE, 1024 * 1024, 5);
         logDirCleanup(DEBUG_LOG_DIR, 30);
 
         // redirect a copy of System.err to a file
         try {
             String message = "\n\n----- " + APP_NAME + " " + APP_VERSION
                     + " started on " + new Date().toString() + " -----";
             PrintStream logger = new LoggerPrintStream(new FileOutputStream(DEBUG_LOG_FILE, true), System.err, message);
             System.setErr(logger);
 
         } catch (FileNotFoundException e) {
             System.err.println("Unable to write to: " + DEBUG_LOG_FILE);
         }
 
         // read input parameters and load the optional project file
         Project project = null;
         if (args.length > 0) {
             File file = new File(args[0]);
             if (!file.isAbsolute()) {
                 file = new File(STARTUP_DIRECTORY, file.getPath());
             }
             if (file.exists() && file.isFile()) {
                 project = Project.loadProject(file.getAbsoluteFile());
             }
         }
 
         // the program must be started in the event dispatch thread
         final Project p = project;
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 new Ikayaki(p);
             }
         });
     }
 
     /**
      * Removes the old entries of a log file. When the maximum size for the current log file is reached, it will be
      * renamed to file.1, file.2 and so on.
      *
      * @param logFile   the log file to be cleaned.
      * @param maxLength maximum size in bytes for an individual log file.
      * @param maxFiles  maximum number of log files. When the number is reached, the oldest file will be deleted.
      */
     private static void logFileCleanup(File logFile, long maxLength, int maxFiles) {
         if (!logFile.isFile() || logFile.length() <= maxLength) {
             return;
         }
 
         // rename the files to file.1, file.2 and so on
         for (int i = maxFiles - 1; i >= 0; i--) {
             File from = new File(logFile.getAbsolutePath() + (i == 0 ? "" : "." + i));
             File to = new File(logFile.getAbsolutePath() + "." + (i + 1));
             if (from.isFile() && !to.exists()) {
                 from.renameTo(to);
             }
         }
 
         // delete the oldest file
         File f = new File(logFile.getAbsolutePath() + "." + maxFiles);
         if (f.isFile()) {
             f.delete();
         }
     }
 
     /**
      * Removes all old files from the specified directory.
      *
      * @param directory the directory from which the old files will be removed.
      * @param maxDays   the maximum age for the files in days.
      */
     private static void logDirCleanup(File directory, int maxDays) {
         if (!directory.isDirectory()) {
             return;
         }
 
         // get the directory contents and set time limits
         File[] files = directory.listFiles();
         long now = System.currentTimeMillis();
         long maxTime = (long) (maxDays) * 24 * 3600 * 1000;
 
         // delete all files which are more than maxDays old
         for (File file : files) {
             if (file.isFile() && (now - file.lastModified()) > maxTime) {
                 file.delete();
             }
         }
     }
 
     /**
      * Starts the user interface of the program.
      *
      * @param project a project to be opened when the program starts, or null to open no project.
      * @throws HeadlessException if GraphicsEnvironment.isHeadless() returns true.
      */
     public Ikayaki(Project project) throws HeadlessException {
 
         // set look and feel
         PlasticLookAndFeel.setMyCurrentTheme(new SkyBlue());
         try {
             UIManager.setLookAndFeel(new Plastic3DLookAndFeel());
         } catch (UnsupportedLookAndFeelException e) {
             System.err.println(e);
         }
 
         // do layout
         final MainViewPanel main = new MainViewPanel(project);
         setTitle(null);
         setIconImage(new ImageIcon(ClassLoader.getSystemResource("resources/icon.png")).getImage());
         setLayout(new BorderLayout());
         setJMenuBar(main.getMenuBar());
         add(main, "Center");
         //add(main.getStatusBar(), "South");    // TODO: there is no status bar
 
         setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
         pack();
 
         // monitor the size of this frame
         addComponentListener(new ComponentAdapter() {
             @Override public void componentResized(ComponentEvent e) {
                 if ((getExtendedState() & MAXIMIZED_BOTH) == 0) {
                     Settings.setWindowWidth(getWidth());
                     Settings.setWindowHeight(getHeight());
                 }
             }
         });
         addWindowStateListener(new WindowStateListener() {
             public void windowStateChanged(WindowEvent e) {
                 if ((getExtendedState() & MAXIMIZED_BOTH) != 0) {
                     Settings.setWindowMaximized(true);
                 } else {
                     Settings.setWindowMaximized(false);
                 }
             }
         });
 
         // set the window close operation
         addWindowListener(new WindowAdapter() {
             @Override public void windowClosing(WindowEvent e) {
                 main.exitProgram();
             }
         });
 
         // restore size and position
         setSize(Settings.getWindowWidth(), Settings.getWindowHeight());
         setLocationByPlatform(true);
         setVisible(true);
 
         Rectangle maxBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
         setLocation(getX() + getWidth() > maxBounds.width ? maxBounds.width - getWidth() : getX(),
                 getY() + getHeight() > maxBounds.height ? maxBounds.height - getHeight() : getY());
         setLocation(getX() < 0 ? 0 : getX(), getY() < 0 ? 0 : getY());
 
         if (Settings.getWindowMaximized() && System.getProperty("os.name").startsWith("Windows")) {
             try {
                 // native code for maximizing the window
                 int hwnd = JUtil.getHwnd(getTitle());
                 JUtil.setWindowMaximized(hwnd);
             } catch (UnsatisfiedLinkError e) {
                 e.printStackTrace();
             }
         }
     }
 
     /**
      * Sets the title of the program. Appends the name and version of the program with the supplied parameter.
      *
      * @param title the text to be shown in the title, or null to show only the program's name and version.
      */
     @Override public void setTitle(String title) {
         if (title != null) {
             super.setTitle(APP_NAME + " " + APP_VERSION + " - " + title);
         } else {
             super.setTitle(APP_NAME + " " + APP_VERSION);
         }
     }
 }
