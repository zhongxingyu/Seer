 package org.generationcp.ibpworkbench.launcher;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.program.Program;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Tray;
 import org.eclipse.swt.widgets.TrayItem;
 
 public class Launcher {
     
     private Display display;
     private Shell shell;
     private Tray tray;
     
     private String mysqlBinDir = "mysql/bin";
     private String tomcatBinDir = "tomcat/bin";
     
     private Menu menu;
     private MenuItem launchWorkbenchItem;
     private MenuItem exitItem;
     private TrayItem trayItem;
     private Process mysqlProcess;
     private Process tomcatStartProcess;
     private Process tomcatStopProcess;
 
     protected void initializeComponents() {
         display = new Display ();
         shell = new Shell(display);
         
         // get the System Tray
         tray = display.getSystemTray ();
         if (tray == null) {
             throw new RuntimeException("System tray not available");
         }
         
         // create a System Tray item
         trayItem = new TrayItem (tray, SWT.NONE);
         trayItem.setToolTipText("IBPWorkbench");
         
         Image image = new Image(display, "images/systray.ico");
         trayItem.setImage(image);
         
         // create the menu
         menu = new Menu (shell, SWT.POP_UP);
         
         launchWorkbenchItem = new MenuItem(menu, SWT.PUSH);
         launchWorkbenchItem.setText("Launch IBPWorkbench");
         
         exitItem = new MenuItem(menu, SWT.PUSH);
         exitItem.setText("Exit");
     }
     
     protected void initializeActions() {
         // add listeners System Tray Item
         Listener showMenuListener = new Listener () {
             public void handleEvent (Event event) {
                 menu.setVisible(true);
             }
         };
         trayItem.addListener (SWT.Selection, showMenuListener);
         trayItem.addListener (SWT.MenuDetect, showMenuListener);
         
         // add listener to the Launch IBPWorkbench listener
         launchWorkbenchItem.addSelectionListener(new SelectionAdapter() {
             @Override
             public void widgetSelected(SelectionEvent e) {
                Program.launch("http://localhost/");
             }
         });
         
         // add listener to the Exit menu item
         exitItem.addSelectionListener(new SelectionAdapter() {
             @Override
             public void widgetSelected(SelectionEvent e) {
                 shell.dispose();
                 
                 if (mysqlProcess != null) {
                     mysqlProcess.destroy();
                 }
                 
                 if (tomcatStartProcess != null) {
                     tomcatStartProcess.destroy();
                 }
                 
                 shutdownWebApps();
             }
         });
     }
     
     protected void initializeMysql() {
         String workingDirPath = mysqlBinDir;
         String mysqldPath = "mysqld.exe";
         String myIniPath =  "../my.ini";
         
         ProcessBuilder pb = new ProcessBuilder(mysqldPath, "--defaults-file=" + myIniPath);
         pb.directory(new File(workingDirPath).getAbsoluteFile());
         try {
             mysqlProcess = pb.start();
         }
         catch (IOException e) {
             e.printStackTrace();
         }
     }
     
     protected void initializeWebApps() {
         File tomcatBinPath = new File(tomcatBinDir).getAbsoluteFile();
         String tomcatPath = "startup.bat";
         
         String platform = SWT.getPlatform();
         if (platform.equals("win32")) {
             tomcatPath = "startup.bat";
         }
         else {
             tomcatPath = "startup.sh";
         }
         
         ProcessBuilder pb = new ProcessBuilder(tomcatBinPath.getAbsolutePath() + File.separator + tomcatPath);
         pb.directory(tomcatBinPath);
         try {
             tomcatStartProcess = pb.start();
         }
         catch (IOException e) {
             e.printStackTrace();
         }
     }
     
     protected void shutdownWebApps() {
         File tomcatBinPath = new File(tomcatBinDir).getAbsoluteFile();
         String tomcatPath = "shutdown.bat";
         
         String platform = SWT.getPlatform();
         if (platform.equals("win32")) {
             tomcatPath = "shutdown.bat";
         }
         else {
             tomcatPath = "shutdown.sh";
         }
         
         ProcessBuilder pb = new ProcessBuilder(tomcatBinPath.getAbsolutePath() + File.separator + tomcatPath);
         pb.directory(tomcatBinPath);
         try {
             tomcatStopProcess = pb.start();
         }
         catch (IOException e) {
             e.printStackTrace();
         }
         
         try {
             tomcatStopProcess.waitFor();
         }
         catch (InterruptedException e) {
             e.printStackTrace();
         }
     }
     
     public void open() {
         while (!shell.isDisposed()) {
             if (!display.readAndDispatch()) display.sleep();
         }
     }
     
     public void assemble() {
         initializeComponents();
         initializeActions();
         initializeMysql();
         initializeWebApps();
     }
     
     public static void main(String[] args) {
         Launcher launcher = new Launcher();
         launcher.assemble();
         
         launcher.open();
     }
 }
