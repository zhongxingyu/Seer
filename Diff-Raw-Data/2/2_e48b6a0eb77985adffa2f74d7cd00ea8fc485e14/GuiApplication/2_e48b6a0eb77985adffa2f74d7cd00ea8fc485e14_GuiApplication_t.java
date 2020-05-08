 package de.menzerath.imwd;
 
 import de.menzerath.util.Helper;
 import de.menzerath.util.Updater;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.net.URI;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.nio.file.StandardCopyOption;
 import java.security.CodeSource;
 
 public class GuiApplication extends JFrame {
     // Tray-Icons
     private static final Image iconOk = Toolkit.getDefaultToolkit().getImage(GuiApplication.class.getResource("/res/ic_ok.png"));
     private static final Image iconWarning = Toolkit.getDefaultToolkit().getImage(GuiApplication.class.getResource("/res/ic_warning.png"));
     private static final Image iconError = Toolkit.getDefaultToolkit().getImage(GuiApplication.class.getResource("/res/ic_error.png"));
     private static final Image iconNoConnection = Toolkit.getDefaultToolkit().getImage(GuiApplication.class.getResource("/res/ic_noConnection.png"));
 
     // GUI-Elements
     private static JFrame frame;
     private JPanel mainPanel;
     private JPanel websiteSettings2;
     private JPanel websiteSettings3;
     private JMenu mnChecks;
     private JMenu mnChecker;
     private JMenu mnLogs;
     private JTextField urlTextField;
     private JTextField url2TextField;
     private JTextField url3TextField;
     private JTextField intervalTextField;
     private JTextField interval2TextField;
     private JTextField interval3TextField;
     private JButton startButton;
     private JButton stopButton;
 
     // Other
     private static TrayIcon[] trayIcon = new TrayIcon[4];
     private Checker[] checker = new Checker[4];
 
     /**
      * Start the GUI!
      * Prepares everything and then shows the form
      */
     public static void startGUI() {
         // For an nicer look according to the used OS.
         try {
             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         } catch (Exception ignored) {
         }
 
         frame = new JFrame("GuiApplication");
         frame.setContentPane(new GuiApplication().mainPanel);
         frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
         frame.pack();
         frame.setLocationRelativeTo(null);
         frame.setTitle("Is My Website Down?");
         frame.setIconImage(iconOk);
         frame.setResizable(false);
         frame.setVisible(true);
 
         // Run an update-check
         runUpdateCheck(true);
     }
 
     /**
      * Gives every button an action and adds an JMenuBar
      */
     public GuiApplication() {
         // How many Checkers will be shown
         int checkerAmount = Main.getCheckerCountFromSettings();
         if (checkerAmount < 3) websiteSettings3.setVisible(false);
         if (checkerAmount < 2) websiteSettings2.setVisible(false);
         pack();
 
         // Load saved / default values
         urlTextField.setText(Main.getUrlFromSettings(1));
         url2TextField.setText(Main.getUrlFromSettings(2));
         url3TextField.setText(Main.getUrlFromSettings(3));
         intervalTextField.setText("" + Main.getIntervalFromSettings(1));
         interval2TextField.setText("" + Main.getIntervalFromSettings(2));
         interval3TextField.setText("" + Main.getIntervalFromSettings(3));
 
         startButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 for (int i = 1; i < Main.getCheckerCountFromSettings() + 1; i++) {
                     addChecker(i);
                 }
             }
         });
 
         stopButton.setEnabled(false);
         stopButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 stop();
             }
         });
 
         // Start: JMenuBar
         JMenuBar menuBar = new JMenuBar();
         frame.setJMenuBar(menuBar);
 
         JMenu mnFile = new JMenu("File");
         menuBar.add(mnFile);
 
         JMenuItem mntmExit = new JMenuItem("Exit");
         mntmExit.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent arg0) {
                 try {
                     checker[1].stopTesting();
                     checker[2].stopTesting();
                     checker[3].stopTesting();
                 } catch (NullPointerException ignored) {
                 }
                 System.exit(0);
             }
         });
 
         JMenuItem mntmAbout = new JMenuItem("About");
         mntmAbout.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent arg0) {
                 JOptionPane.showMessageDialog(null,
                         "\"Is My Website Down?\" - Version " + Main.getVersion() +
                                 "\n\nIcons by Ampeross - http://ampeross.deviantart.com" +
                                 "\nSourcecode: http://github.com/MarvinMenzerath/IsMyWebsiteDown - GPLv2" +
                                 "\n© 2012-2014: Marvin Menzerath - http://marvin-menzerath.de", "About \"Is My Website Down?\"", JOptionPane.INFORMATION_MESSAGE);
             }
         });
         mnFile.add(mntmAbout);
         mnFile.add(mntmExit);
 
         mnChecker = new JMenu("Checker");
         menuBar.add(mnChecker);
 
         final JRadioButtonMenuItem rbOne = new JRadioButtonMenuItem("One");
         if (checkerAmount == 1) rbOne.setSelected(true);
         rbOne.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 Main.setCheckerCountForSettings(1);
                 websiteSettings2.setVisible(false);
                 websiteSettings3.setVisible(false);
                 frame.pack();
             }
         });
         mnChecker.add(rbOne);
 
         final JRadioButtonMenuItem rbTwo = new JRadioButtonMenuItem("Two");
         if (checkerAmount == 2) rbTwo.setSelected(true);
         rbTwo.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 Main.setCheckerCountForSettings(2);
                 websiteSettings2.setVisible(true);
                 websiteSettings3.setVisible(false);
                 frame.pack();
             }
         });
         mnChecker.add(rbTwo);
 
         final JRadioButtonMenuItem rbThree = new JRadioButtonMenuItem("Three");
         if (checkerAmount == 3) rbThree.setSelected(true);
         rbThree.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 Main.setCheckerCountForSettings(3);
                 websiteSettings2.setVisible(true);
                 websiteSettings3.setVisible(true);
                 frame.pack();
             }
         });
         mnChecker.add(rbThree);
 
         ButtonGroup checkerGroup = new ButtonGroup();
         checkerGroup.add(rbOne);
         checkerGroup.add(rbTwo);
         checkerGroup.add(rbThree);
 
         mnChecks = new JMenu("Checks");
         menuBar.add(mnChecks);
 
         final JCheckBoxMenuItem cbCheckContent = new JCheckBoxMenuItem("Content");
         cbCheckContent.setSelected(Main.getCheckContentFromSettings());
         cbCheckContent.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent arg0) {
                 Main.setCheckContentForSettings(cbCheckContent.isSelected());
             }
         });
         mnChecks.add(cbCheckContent);
 
         final JCheckBoxMenuItem cbCheckPing = new JCheckBoxMenuItem("Ping");
         cbCheckPing.setSelected(Main.getCheckPingFromSettings());
         cbCheckPing.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent arg0) {
                 Main.setCheckPingForSettings(cbCheckPing.isSelected());
             }
         });
         mnChecks.add(cbCheckPing);
 
         mnLogs = new JMenu("Logs");
         menuBar.add(mnLogs);
 
         final JCheckBoxMenuItem cbLogEnable = new JCheckBoxMenuItem("Enable");
         cbLogEnable.setSelected(Main.getCreateLogFromSettings());
         cbLogEnable.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 Main.setCreateLogForSettings(cbLogEnable.isSelected());
             }
         });
         mnLogs.add(cbLogEnable);
 
         final JCheckBoxMenuItem cbLogValid = new JCheckBoxMenuItem("Log valid checks");
         cbLogValid.setSelected(Main.getCreateValidLogFromSettings());
         cbLogValid.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 Main.setCreateValidLogForSettigs(cbLogValid.isSelected());
             }
         });
         mnLogs.add(cbLogValid);
 
         JMenu mnTools = new JMenu("Tools");
         menuBar.add(mnTools);
 
         JMenuItem mntmAutorun = new JMenuItem("Add to Autorun");
         mntmAutorun.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent arg0) {
                 addToAutorun();
             }
         });
 
         // Change the text if the user doesn't use Windows and disable it
         if (!System.getProperty("os.name").startsWith("Windows")) {
             mntmAutorun.setEnabled(false);
             mntmAutorun.setText("Add to Autorun (Windows-only)");
         }
         mnTools.add(mntmAutorun);
 
         JMenuItem mntmUpdates = new JMenuItem("Check for Updates");
         mntmUpdates.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 runUpdateCheck(false);
             }
         });
         mnTools.add(mntmUpdates);
         // End: JMenuBar
     }
 
     /**
      * This will create an TrayIcon and show information about the current check(s)
      */
     private static void createTrayIcon(int checkerId) {
         // Not supported? Bye, Bye!
         if (!SystemTray.isSupported()) {
             System.out.println("SystemTray is not supported. Exiting...");
             System.exit(1);
         }
 
         SystemTray tray = SystemTray.getSystemTray();
         trayIcon[checkerId] = new TrayIcon(iconOk, "Is My Website Down?");
         trayIcon[checkerId].setImageAutoSize(true);
         trayIcon[checkerId].setToolTip("Stopped - IMWD");
         trayIcon[checkerId].addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 frame.setVisible(true);
             }
         });
 
         try {
             tray.add(trayIcon[checkerId]);
         } catch (AWTException e) {
             // Not possible? Bye, Bye!
             System.out.println("TrayIcon could not be added. Exiting...");
             System.exit(1);
         }
     }
 
     /**
      * Validates the input and starts the process
      * Not the best solution, but it works ;)
      *
      * @param checkerId Currently added Checker (ID)
      */
     private void addChecker(int checkerId) {
         String errorMessage = "Enter a valid URL and interval:\nURL: Starts with \"http://\"\nInterval: Only Numbers, between 10 and 600";
         if (checkerId == 1) {
             if (Helper.validateUrlInput(urlTextField.getText().trim()) && Helper.validateIntervalInput(intervalTextField.getText().trim())) {
                 start(urlTextField.getText().trim(), Helper.parseInt(intervalTextField.getText().trim()), checkerId, Main.getCheckerCountFromSettings());
             } else {
                 JOptionPane.showMessageDialog(null, errorMessage, "Invalid Input (Website 1)", JOptionPane.ERROR_MESSAGE);
             }
         } else if (checkerId == 2) {
             if (Helper.validateUrlInput(url2TextField.getText().trim()) && Helper.validateIntervalInput(interval2TextField.getText().trim())) {
                 start(url2TextField.getText().trim(), Helper.parseInt(interval2TextField.getText().trim()), checkerId, Main.getCheckerCountFromSettings());
             } else {
                 JOptionPane.showMessageDialog(null, errorMessage, "Invalid Input (Website 2)", JOptionPane.ERROR_MESSAGE);
             }
         } else if (Main.getCheckerCountFromSettings() == 3) {
             if (Helper.validateUrlInput(url3TextField.getText().trim()) && Helper.validateIntervalInput(interval3TextField.getText().trim())) {
                 start(url3TextField.getText().trim(), Helper.parseInt(interval3TextField.getText().trim()), checkerId, Main.getCheckerCountFromSettings());
             } else {
                 JOptionPane.showMessageDialog(null, errorMessage, "Invalid Input (Website 3)", JOptionPane.ERROR_MESSAGE);
             }
         }
     }
 
     /**
      * Start testing!
      * Prepares the GUI, the TrayIcon and starts the Checker
      */
     private void start(String url, int interval, int checkerId, int maxChecker) {
         if (!Main.getCheckContentFromSettings() && !Main.getCheckPingFromSettings()) {
             // Show message only once (before adding last Checker)
             if (checkerId == maxChecker) {
                 JOptionPane.showMessageDialog(null, "You have to select at least one Check-Type (Content / Ping)!", "Error", JOptionPane.ERROR_MESSAGE);
             }
             return;
         }
 
         createTrayIcon(checkerId);
         trayIcon[checkerId].setToolTip("Running - IMWD");
 
         // Disable/Dispose GUI(-elements)
         frame.setVisible(false);
         frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         mnChecks.setEnabled(false);
         mnChecker.setEnabled(false);
         mnLogs.setEnabled(false);
         urlTextField.setEditable(false);
         url2TextField.setEditable(false);
         url3TextField.setEditable(false);
         intervalTextField.setEditable(false);
         interval2TextField.setEditable(false);
         interval3TextField.setEditable(false);
         startButton.setEnabled(false);
         stopButton.setEnabled(true);
 
         // Save the values
         Main.setUrlForSettings(checkerId, url);
         Main.setIntervalForSettings(checkerId, interval);
 
         // Create the Checker
         checker[checkerId] = new Checker(checkerId, url, interval, Main.getCheckContentFromSettings(), Main.getCheckPingFromSettings(), Main.getCreateLogFromSettings(), Main.getCreateValidLogFromSettings(), true);
         checker[checkerId].startTesting();
     }
 
     /**
      * Stop testing!
      * Prepares the GUI, the TrayIcon and stops the Checker
      */
     private void stop() {
         SystemTray tray = SystemTray.getSystemTray();
         for (int i = 1; i < 4; i++) {
             try {
                 tray.remove(trayIcon[i]);
                 checker[i].stopTesting();
             } catch (Exception ignored) {
             }
         }
 
         frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
 
         // Enable GUI-elements
         mnChecks.setEnabled(true);
         mnChecker.setEnabled(true);
         mnLogs.setEnabled(true);
         urlTextField.setEditable(true);
         url2TextField.setEditable(true);
         url3TextField.setEditable(true);
         intervalTextField.setEditable(true);
         interval2TextField.setEditable(true);
         interval3TextField.setEditable(true);
         startButton.setEnabled(true);
         stopButton.setEnabled(false);
     }
 
     /**
      * An update-check for the "GuiApplication": If there is an update available, it will show an message and a button to open
      * the website in a browser.
      *
      * @param startup Running this on startup? Then don't show "error"'s or "ok"'s.
      */
     private static void runUpdateCheck(final boolean startup) {
         Thread thread = new Thread() {
             public void run() {
                 Updater myUpdater = new Updater();
                 if (myUpdater.getServerVersion().equalsIgnoreCase("Error")) {
                     // Show this message if the Updater was created by the user
                     if (!startup) {
                        JOptionPane.showMessageDialog(null, "Unable to search for Updates. Please visit \"https://github.com/MarvinMenzerath/IsMyWebsiteDown/releases/\".", "Error", JOptionPane.ERROR_MESSAGE);
                     }
                 } else if (myUpdater.isUpdateAvailable()) {
                     int value = JOptionPane.showConfirmDialog(null, "There is an update to version " + myUpdater.getServerVersion() + " available.\nChanges: " + myUpdater.getServerChangelog() + "\n\nDo you want to download it now?", "Update Available", JOptionPane.YES_NO_OPTION);
                     if (value == JOptionPane.YES_OPTION) {
                         try {
                             Desktop.getDesktop().browse(new URI("https://github.com/MarvinMenzerath/IsMyWebsiteDown/releases"));
                         } catch (Exception ignored) {
                         }
                         System.exit(0);
                     }
                 } else {
                     // Show this message if the Updater was created by the user
                     if (!startup) {
                         JOptionPane.showMessageDialog(null, "Congrats, you are running the latest version of \"Is My Website Down?\".", "No Update Found", JOptionPane.INFORMATION_MESSAGE);
                     }
                 }
             }
         };
         thread.start();
     }
 
     /**
      * Copy "Is My Website Down" into the Autorun-folder (works with Windows XP, Vista, 7, 8 and 8.1).
      */
     private void addToAutorun() {
         try {
             CodeSource cSource = GuiApplication.class.getProtectionDomain().getCodeSource();
             File sourceFile = new File(cSource.getLocation().toURI().getPath());
             Path source = Paths.get(sourceFile.getParentFile().getPath() + File.separator + sourceFile.getName());
             Path dest;
             if (System.getProperty("os.name").equals("Windows XP")) {
                 dest = Paths.get("C:\\Documents and Settings\\" + System.getProperty("user.name") + "\\Start Menu\\Programs\\Startup\\IMWD.jar");
             } else {
                 dest = Paths.get("C:\\Users\\" + System.getProperty("user.name") + "\\AppData\\Roaming\\Microsoft\\Windows\\Start Menu\\Programs\\Startup\\IMWD.jar");
             }
             Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
             JOptionPane.showMessageDialog(null, "It's done!\nPlease remember to copy new versions/updates to the Autorun.", "Done", JOptionPane.INFORMATION_MESSAGE);
         } catch (Exception e) {
             e.printStackTrace();
             JOptionPane.showMessageDialog(null, "Could not copy IMWD to your Autorun. Please check...\n\n  * You are allowed to copy files to the Autorun-Folder.\n  * You are running Windows XP or higher.\n\nException: " + e.getMessage(), "Sorry", JOptionPane.ERROR_MESSAGE);
         }
     }
 
     /**
      * Changes the Notification according to the check-result
      *
      * @param status Result of the current check
      */
     public static void setNotification(int checkerId, String url, int status) {
         if (status == 1) {
             trayIcon[checkerId].setImage(iconOk);
             trayIcon[checkerId].setToolTip("OK - IMWD\n" + url);
         } else if (status == 2) {
             trayIcon[checkerId].setImage(iconWarning);
             trayIcon[checkerId].displayMessage("Not Reachable: " + url, "Unable to reach " + url + " while a ping was successful.", TrayIcon.MessageType.WARNING);
             trayIcon[checkerId].setToolTip("Not Reachable - IMWD\n" + url);
         } else if (status == 3) {
             trayIcon[checkerId].setImage(iconError);
             trayIcon[checkerId].displayMessage("Not Reachable: " + url, "Unable to reach (and ping) " + url + ".", TrayIcon.MessageType.ERROR);
             trayIcon[checkerId].setToolTip("Not Reachable - IMWD\n" + url);
         } else if (status == 4) {
             trayIcon[checkerId].setImage(iconNoConnection);
             trayIcon[checkerId].displayMessage("No Connection!", "Please check your connection to the internet.", TrayIcon.MessageType.ERROR);
             trayIcon[checkerId].setToolTip("No Connection - IMWD\n" + url);
         }
     }
 }
