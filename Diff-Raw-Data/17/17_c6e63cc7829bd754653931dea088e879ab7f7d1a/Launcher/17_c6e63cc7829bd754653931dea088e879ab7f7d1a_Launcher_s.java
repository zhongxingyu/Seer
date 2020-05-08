 /***************************************************************
  * Copyright (c) 2012, All Rights Reserved.
  * 
  * Generation Challenge Programme (GCP)
  * 
  * 
  * This software is licensed for use under the terms of the 
  * GNU General Public License (http://bit.ly/8Ztv8M) and the 
  * provisions of Part F of the Generation Challenge Programme 
  * Amended Consortium Agreement (http://bit.ly/KQX1nL)
  * 
  **************************************************************/
 package org.generationcp.ibpworkbench.launcher;
 
 import java.io.File;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.ShellAdapter;
 import org.eclipse.swt.events.ShellEvent;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.program.Program;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.Monitor;
 import org.eclipse.swt.widgets.ProgressBar;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Tray;
 import org.eclipse.swt.widgets.TrayItem;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class Launcher {
     
     private static final Logger LOG = LoggerFactory.getLogger(Launcher.class);
     
     private final static int MAX_CONNECT_TRIES = 5;
     
     private String mysqlBinDir = "mysql/bin";
     private String tomcatDir = "tomcat";
     private String firstRunFilename = "first_run";
     
     private String workbenchUrl = "http://localhost:18080/ibpworkbench/";
     
     private Display display;
     private Shell shell;
 //    private Tray tray;
 //    
 //    private Menu menu;
 //    private MenuItem launchWorkbenchItem;
 //    private MenuItem exitItem;
     private TrayItem trayItem;
     private Process mysqlProcess;
     private TomcatServer tomcatServer;
     
     private Label splashLabel;
     private Label progressLabel;
     private ProgressBar progressBar;
     
     private File icisIni;
     private File icisIniBakFile;
     
     private long mysqlHeadstartMillis = 1000;
     private long tomcatHeadstartMillis = 2000;
     
     private StartupThread startupThread;
 
     private Shell warningShell;
     private Label warningLabel;
     private Composite warningImageArea;
     private Button buttonOk;
 
     protected void initializeComponents() {
         display = new Display ();
         shell = new Shell(display, SWT.NO_TRIM | SWT.INHERIT_DEFAULT);
         shell.setText("IBP Workbench");
         shell.setSize(640, 480);
         centerShellToPrimaryMonitor(shell);
         
         // add the splash image
         splashLabel = new Label(shell, SWT.NONE);
         splashLabel.setImage(new Image(display, "images/splash.png"));
         
         // add progress label
         progressLabel = new Label(shell, SWT.NONE);
         progressLabel.moveAbove(splashLabel);
         progressLabel.setText("Loading...");
         
         // add the progress bar
         progressBar = new ProgressBar(shell, SWT.HORIZONTAL | SWT.INDETERMINATE);
         progressBar.moveAbove(splashLabel);
         
         // create the warning shell
         warningShell = new Shell(display, SWT.DIALOG_TRIM);
         warningShell.setSize(640, 480);
         warningShell.setText("IBP Workbench");
         centerShellToPrimaryMonitor(warningShell);
         
         warningLabel = new Label(warningShell, SWT.LEFT);
         warningLabel.setText("IBPWorkbench will start MySQL and Tomcat for the first time.\nPlease \"Allow Access\" or \"Unblock\" it on your firewall.");
         
         warningImageArea = new Composite(warningShell, SWT.CENTER);
         
         buttonOk = new Button(warningShell, SWT.PUSH);
         buttonOk.setText("OK");
     }
     
     protected void initializeLayout() {
         // layout the splash screen
         layoutSplashShell();
         
         // layout the warning shell
         layoutWarningShell();
     }
     
     protected void centerShellToPrimaryMonitor(Shell shell) {
         // center shell to primary monitor
         Monitor monitor = display.getPrimaryMonitor();
         Rectangle monitorBounds = monitor.getBounds();
         
         Rectangle bounds = shell.getBounds();
         int x = (monitorBounds.x + (monitorBounds.width / 2)) - (bounds.width / 2);
         int y = (monitorBounds.y + (monitorBounds.height / 2)) - (bounds.height / 2);
         shell.setLocation(x, y);
     }
     
     protected void layoutSplashShell() {
         Rectangle shellBounds = shell.getBounds();
         splashLabel.setBounds(0, 0, shellBounds.width, shellBounds.height);
         
         final int vMargin = 5;
         final int hMargin = 40;
         //Point progressLabelPreferredSize = progressLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT);
         Point progressBarPreferredSize = progressBar.computeSize(SWT.DEFAULT, SWT.DEFAULT);
         
         //int progressLabelY = shellBounds.height - margin - progressBarPreferredSize.y - margin - progressLabelPreferredSize.y;
         int progressBarY = shellBounds.height - vMargin - progressBarPreferredSize.y;
         
         //progressLabel.setBounds(margin, progressLabelY, shellBounds.width - (2* margin), progressLabelPreferredSize.y);
         progressBar.setBounds(hMargin, progressBarY, shellBounds.width - (2 * hMargin), progressBarPreferredSize.y);
     }
     
     protected void layoutWarningShell() {
         GridLayout layout = new GridLayout(1, true);
         
         GridData warningLabelLayoutData = new GridData();
         warningLabelLayoutData.grabExcessHorizontalSpace = false;
         warningLabelLayoutData.grabExcessVerticalSpace = false;
         warningLabel.setLayoutData(warningLabelLayoutData);
         
         GridData warningImageAreaLayoutData = new GridData(GridData.FILL_BOTH);
         warningImageAreaLayoutData.grabExcessHorizontalSpace = true;
         warningImageAreaLayoutData.grabExcessVerticalSpace = true;
         warningImageArea.setLayoutData(warningImageAreaLayoutData);
         
         GridData buttonOkLayoutData = new GridData();
         buttonOkLayoutData.grabExcessHorizontalSpace = false;
         buttonOkLayoutData.grabExcessVerticalSpace = false;
         buttonOkLayoutData.horizontalAlignment = SWT.RIGHT;
         buttonOkLayoutData.widthHint = 80;
         buttonOk.setLayoutData(buttonOkLayoutData);
         
         warningShell.setLayout(layout);
     }
     
     protected void initializeActions() {
         warningImageArea.addPaintListener(new PaintListener() {
             
             public void paintControl(PaintEvent e) {
                 GC gc = e.gc;
                 
                 Point labelSize = warningImageArea.getSize();
                 
                 Image image = new Image(display, "images/unblock.png");
                 int x = (labelSize.x - image.getImageData().width) / 2;
                 int y = (labelSize.y - image.getImageData().height) / 2;
                 
                 gc.drawImage(image, x, y);
             }
         });
 
         buttonOk.addSelectionListener(new SelectionAdapter() {
             
             @Override
             public void widgetSelected(SelectionEvent e) {
                 warningShell.dispose();
                 
                 launchMySQLAndTomcat();
             }
         });
         
         warningShell.addShellListener(new ShellAdapter() {
             @Override
             public void shellClosed(ShellEvent e) {
                 launchMySQLAndTomcat();
             }
         });
     }
     
     protected void initializeMysql() {
         LOG.trace("Starting MySQL...");
         
         File workingDirPath = new File(mysqlBinDir).getAbsoluteFile();
         String mysqldPath = "mysqld.exe";
         String myIniPath =  "../my.ini";
         
         ProcessBuilder pb = new ProcessBuilder(workingDirPath.getAbsolutePath() + File.separator + mysqldPath, "--defaults-file=" + myIniPath);
         pb.directory(workingDirPath);
         try {
             mysqlProcess = pb.start();
         }
         catch (IOException e) {
             LOG.error("IOException", e );
         }
         
         // give Tomcat a headstart
         try {
             Thread.sleep(mysqlHeadstartMillis);
         }
         catch (InterruptedException e) {
             LOG.error("Interrupted while waiting for MySQL to start", e);
         }
         
         try {
             Class.forName("com.mysql.jdbc.Driver");
         }
         catch (ClassNotFoundException e) {
             LOG.error("Error encountered while trying to load JDBC Driver", e);
         }
         
         Connection conn = null;
         boolean success = false;
         int tryNum = 0;
         while (++tryNum < MAX_CONNECT_TRIES) {
             try {
                 conn = DriverManager.getConnection("jdbc:mysql://localhost:13306/?user=root");
                 success = true;
                 break;
             }
             catch (SQLException e) {
                 LOG.error("SQL Exception", e);
             }
             
             try {
                 Thread.sleep(1000);
             }
             catch (InterruptedException e) {
                 LOG.error("Interrupted while trying to connect to MySQL", e);
                 break;
             }
         }
         
         if (conn != null) {
             try {
                 conn.close();
             }
             catch (SQLException e) {
                 LOG.error("Error encountered while trying to close JDBC connection", e);
             }
         }
         
         if (!success) {
             LOG.trace("Unable to connect to MySQL after {} tries", tryNum);
         }
         else {
             LOG.trace("Connected to MySQL after {} tries", tryNum);
         }
     }
     
     protected void initializeTomcat() {
         LOG.trace("Starting Tomcat...");
         
         File tomcatBinPath = new File(tomcatDir).getAbsoluteFile();
         tomcatServer = new TomcatServer();
         tomcatServer.setCatalinaHome(tomcatBinPath.getAbsolutePath());
         
         tomcatServer.start();
         
         // give Tomcat a headstart
         try {
             Thread.sleep(tomcatHeadstartMillis);
         }
         catch (InterruptedException e) {
             LOG.error("Interrupted while waiting for Tomcat to start", e);
         }
         
         // try connecting to the workbench url
         HttpParams httpParams = new BasicHttpParams();
         HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
         
        HttpClient httpClient = new DefaultHttpClient(httpParams);
        HttpGet httpGet = new HttpGet(workbenchUrl);
        HttpResponse response = null;
         
         int tryNum = 0;
         boolean success = false;
         while (++tryNum < MAX_CONNECT_TRIES) {
             try {
                 response = httpClient.execute(httpGet);
             }
             catch (ClientProtocolException e) {
                 LOG.error("ClientProtocolException", e);
             }
             catch (IOException e) {
                 LOG.error("IOException", e);
             }
             
             if (response != null && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                 success = true;
                 break;
             }
             
             try {
                 Thread.sleep(1000);
             }
             catch (InterruptedException e) {
                 LOG.error("Interrupted while trying to connect to Tomcat", e);
                 break;
             }
         }
         
         if (!success) {
             LOG.trace("Unable to connect to Tomcat after {} tries", tryNum);
         }
         else {
             LOG.trace("Connected to Tomcat after {} tries", tryNum);
         }
     }
     
     protected void shutdownMysql() {
         LOG.trace("Stopping MySQL...");
         
         if (mysqlProcess != null) {
             mysqlProcess.destroy();
         }
         
         try {
             mysqlProcess.waitFor();
         }
         catch (InterruptedException e) {
             LOG.error("Interrupted while waiting for MySQL to stop", e);
         }
     }
     
     protected void shutdownWebApps() {
         LOG.trace("Stopping Tomcat...");
         
         tomcatServer.stopServer();
     }
     
     protected void renameIcisIni() {
         String tempDir = System.getProperty("java.io.tmpdir");
         icisIni = new File(tempDir + File.separator + "ICIS.ini") ;
         icisIniBakFile = new File(tempDir + File.separator + "ICIS.ini.bak") ;
         
         if (icisIni.exists()) {
             boolean result = icisIni.renameTo(icisIniBakFile);
             LOG.debug("Renamed ICIS.ini to ICIS.ini.bak: {}", result);
         }
     }
     
     protected void revertIcisIni() {
         if (icisIniBakFile.exists()) {
             boolean result = icisIniBakFile.renameTo(icisIni);
             LOG.debug("Renamed ICIS.ini.bak to ICIS.ini: {}", result);
         }
     }
     
     protected void launchMySQLAndTomcat() {
         // show the splash screen
         shell.open();
         shell.setActive();
         
         // start Tomcat and MySQL
         startupThread = new StartupThread();
         startupThread.start();
     }
     
     public void open() {
         File firstRunFile = new File(firstRunFilename);
         if (firstRunFile.exists()) {
             warningShell.open();
             firstRunFile.delete();
         }
         else {
             launchMySQLAndTomcat();
         }
         
         while (!shell.isDisposed()) {
             if (!display.readAndDispatch()) display.sleep();
         }
     }
     
     public void assemble() {
         initializeComponents();
         initializeLayout();
         initializeActions();
     }
     
     protected void createSystemTrayItem() {
         // get the System Tray
         Tray tray = display.getSystemTray();
         if (tray == null) {
             throw new RuntimeException("System tray not available");
         }
         
         // create a System Tray item
         trayItem = new TrayItem (tray, SWT.NONE);
         trayItem.setToolTipText("IBPWorkbench");
         
         Image image = new Image(display, "images/systray.ico");
         trayItem.setImage(image);
         
         // create the menu
         final Menu menu = new Menu (shell, SWT.POP_UP);
         
         MenuItem launchWorkbenchItem = new MenuItem(menu, SWT.PUSH);
         launchWorkbenchItem.setText("Launch IBPWorkbench");
         
         MenuItem exitItem = new MenuItem(menu, SWT.PUSH);
         exitItem.setText("Exit");
         
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
                 try {
                     Program.launch(workbenchUrl);
                 }
                 catch (Exception ex) {
                     LOG.error("Cannot launch workbench due to error", ex);
                 }
             }
         });
         
         // add listener to the Exit menu item
         exitItem.addSelectionListener(new SelectionAdapter() {
             @Override
             public void widgetSelected(SelectionEvent e) {
                 LOG.debug("Exiting workbench launcher");
                 
                 try {
                     startupThread.join();
                 }
                 catch (InterruptedException e1) {
                     LOG.error("Interrupted while waiting for startup thread to stop", e);
                 }
                 
                 trayItem.dispose();
                 
                 shell.dispose();
                 
                 shutdownMysql();
                 shutdownWebApps();
                 revertIcisIni();
             }
         });
     }
     
     private class StartupThread extends Thread {
         @Override
         public void run() {
             // rename %TEMP%/icis.ini
             renameIcisIni();
             
             // initialize MySQL
             initializeMysql();
             
             // initialize Tomcat
             initializeTomcat();
             
             // launch the workbench url
             display.asyncExec(new Runnable() {
                 
                 public void run() {
                     try {
                         Program.launch(workbenchUrl);
                     }
                     catch (Exception ex) {
                         LOG.error("Cannot launch workbench due to error", ex);
                     }
                     
                     // hide the splash screen
                     shell.setVisible(false);
                     
                     // create the system tray item
                     createSystemTrayItem();
                 }
             });
         }
     }
     
     public static void main(String[] args) {
         Launcher launcher = new Launcher();
         launcher.assemble();
         
         launcher.open();
     }
 }
