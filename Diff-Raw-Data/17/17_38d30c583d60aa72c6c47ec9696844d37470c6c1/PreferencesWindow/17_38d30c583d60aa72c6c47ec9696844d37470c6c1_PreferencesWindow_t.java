 package cz.vutbr.fit.gja.gjaddr.gui;
 
 import cz.vutbr.fit.gja.gjaddr.gui.util.EscapeKeyHandler;
 import cz.vutbr.fit.gja.gjaddr.importexport.FacebookOauth;
 import cz.vutbr.fit.gja.gjaddr.importexport.GoogleOauth;
 import cz.vutbr.fit.gja.gjaddr.persistancelayer.AuthToken;
 import cz.vutbr.fit.gja.gjaddr.persistancelayer.Database;
 import cz.vutbr.fit.gja.gjaddr.persistancelayer.Group;
 import cz.vutbr.fit.gja.gjaddr.persistancelayer.Settings;
 import cz.vutbr.fit.gja.gjaddr.persistancelayer.util.NotificationsEnum;
 import cz.vutbr.fit.gja.gjaddr.persistancelayer.util.ServicesEnum;
 import cz.vutbr.fit.gja.gjaddr.util.LoggerUtil;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import javax.swing.*;
 import org.slf4j.LoggerFactory;
 
 /**
  * Window with application preferences.
  *
  * @author Bc. Drahomira Herrmannova <xherrm01@stud.fit.vutbr.cz>
  */
 public class PreferencesWindow extends JFrame implements ActionListener {
 
     static final long serialVersionUID = 0;
 
     /**
      * List of action commands for radio buttons selection.
      */
     private enum ActionCommands {
 
         FACEBOOK_CONNECT("fb_conn"),
         FACEBOOK_DISCONNECT("fb_disconn"),
         GOOGLE_CONNECT("goog_conn"),
         GOOGLE_DISCONNECT("goog_disconn");
         private String command;
 
         private ActionCommands(String command) {
             this.command = command;
         }
     }
     /**
      * Application database.
      */
     private Database database = Database.getInstance();
     /**
      * Service buttons.
      */
     private JButton fbButton, googButton;
     /**
      * Radio button for folder selection.
      */
     private JRadioButton folder;
 
     /**
      * Constructor with setting which window should be opened after preferences.
      *
      * @param cameFrom
      */
     public PreferencesWindow(final String cameFrom) {
         this();
 
         this.addWindowListener(new WindowAdapter() {
 
             @Override
             public void windowClosing(WindowEvent e) {
                 try {
                     Class c = Class.forName(cameFrom);
                     if (cameFrom.contains("ImportWindow")) {
                         Group[] selectedGroups = GroupsPanel.getSelectedGroups();
                         if (selectedGroups.length == 1) {
                             new ImportWindow(selectedGroups[0]);
                         } else {
                             new ImportWindow(null);
                         }
                     } else {
                         c.newInstance();
                     }
                 } catch (ClassNotFoundException ex) {
                     LoggerFactory.getLogger(this.getClass()).error(LoggerUtil.getStackTrace(ex));
                 } catch (InstantiationException ex) {
                     LoggerFactory.getLogger(this.getClass()).error(LoggerUtil.getStackTrace(ex));
                 } catch (IllegalAccessException ex) {
                     LoggerFactory.getLogger(this.getClass()).error(LoggerUtil.getStackTrace(ex));
                 }
             }
         });
     }
 
     /**
      * Constructor. Initializes the window.
      */
     public PreferencesWindow() {
         super("Preferences");
         super.setLayout(new BorderLayout());
         this.setPreferredSize(new Dimension(800, 250));
         this.setResizable(true);
         LoggerFactory.getLogger(this.getClass()).debug("Opening preferences window.");
 
         // set window apearance
         try {
             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         } catch (Exception e) {
             System.err.println(e);
         }
 
         // set application icon
         ImageIcon icon = new ImageIcon(getClass().getResource("/res/icon.png"), "GJAddr");
         this.setIconImage(icon.getImage());
 
         // create tabs
         JTabbedPane tabs = new JTabbedPane();
         tabs.addTab("Services Connection", this.createServicesSettingsPanel());
         tabs.addTab("Notification Settings", this.createNotificationSettingsPanel());
         tabs.addTab("Display Settings", this.createDisplaySettingsPanel());
         tabs.addTab("Persistance Settings", this.createPersistenceSettingsPanel());
         tabs.addTab("Application Folder", this.createAppFolderSettingsPanel());
         this.add(tabs);
 
         // enable scrolling tabs
         tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
 
         // make window escapable
         EscapeKeyHandler.setEscapeAction(this);
 
         // make window visible
         this.setResizable(false);
         this.setLocationRelativeTo(null);
         this.pack();
         this.setVisible(true);
 
         LoggerFactory.getLogger(this.getClass()).info("Opening preferences window.");
     }
 
     /**
      * Create display settings panel.
      *
      * @return
      */
     private JPanel createDisplaySettingsPanel() {
         JPanel displaySettingsPanel = new JPanel();
         displaySettingsPanel.setLayout(new BoxLayout(displaySettingsPanel, BoxLayout.PAGE_AXIS));
         displaySettingsPanel.add(this.createNameOrderHeader());
         displaySettingsPanel.add(this.createNameOrderRadionButtons());
        displaySettingsPanel.add(Box.createVerticalGlue());
         return displaySettingsPanel;
     }
 
     /**
      * Create persistence settings panel.
      *
      * @return
      */
     private JPanel createPersistenceSettingsPanel() {
         JPanel persistenceSettingsPanel = new JPanel();
         persistenceSettingsPanel.setLayout(new BoxLayout(persistenceSettingsPanel, BoxLayout.PAGE_AXIS));
         persistenceSettingsPanel.add(this.createPersistanceHeader());
         persistenceSettingsPanel.add(this.createPersistanceRadioButtons());
        persistenceSettingsPanel.add(Box.createVerticalGlue());
         return persistenceSettingsPanel;
     }
 
     /**
      * Create persistence settings panel.
      *
      * @return
      */
     private JPanel createServicesSettingsPanel() {
         JPanel servicesSettingsPanel = new JPanel();
         servicesSettingsPanel.setLayout(new BoxLayout(servicesSettingsPanel, BoxLayout.PAGE_AXIS));
         servicesSettingsPanel.add(this.createServicesHeader());
         servicesSettingsPanel.add(this.createFacebookActionButton());
         servicesSettingsPanel.add(this.createGoogleActionButton());
        servicesSettingsPanel.add(Box.createVerticalGlue());
         return servicesSettingsPanel;
     }
 
     /**
      * Create persistence settings panel.
      *
      * @return
      */
     private JPanel createNotificationSettingsPanel() {
         JPanel notifSettingsPanel = new JPanel();
         notifSettingsPanel.setLayout(new BoxLayout(notifSettingsPanel, BoxLayout.PAGE_AXIS));
         notifSettingsPanel.add(this.createNotificationSettingsHeader());
         notifSettingsPanel.add(this.createNotificationSettingsOptions());
        notifSettingsPanel.add(Box.createVerticalGlue());
         return notifSettingsPanel;
     }
 
     /**
      * Create persistence settings panel.
      *
      * @return
      */
     private JPanel createAppFolderSettingsPanel() {
         JPanel folderSettingsPanel = new JPanel();
         folderSettingsPanel.setLayout(new BoxLayout(folderSettingsPanel, BoxLayout.PAGE_AXIS));
         folderSettingsPanel.add(this.createFolderSettingHeader());
         folderSettingsPanel.add(this.createFolderSettginsOptions());
        folderSettingsPanel.add(Box.createVerticalGlue());
         return folderSettingsPanel;
     }
 
     /**
      * Create persistance header.
      *
      * @return
      */
     private JPanel createPersistanceHeader() {
         JPanel persistanceHeader = new JPanel(new GridLayout());
         JLabel firstHeader = new JLabel("<html><h2>Persistance layer</h2></html>", JLabel.LEFT);
         persistanceHeader.add(firstHeader);
         persistanceHeader.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
 
         return persistanceHeader;
     }
 
     /**
      * Create persistance radio buttons.
      *
      * @return
      */
     private JPanel createPersistanceRadioButtons() {
         JRadioButton binaryButton = new JRadioButton("Binary persistance");
         binaryButton.setActionCommand("bin");
         binaryButton.addActionListener(this);
 
         JRadioButton xmlButton = new JRadioButton("XML persistance");
         xmlButton.setActionCommand("xml");
         xmlButton.addActionListener(this);
 
         ButtonGroup bg = new ButtonGroup();
         bg.add(binaryButton);
         bg.add(xmlButton);
 
         JPanel radioPanel = new JPanel(new GridLayout(0, 1));
         radioPanel.add(binaryButton);
         radioPanel.add(xmlButton);
        radioPanel.add(Box.createVerticalGlue());
 
         binaryButton.setSelected(Settings.instance().isBinPersistance());
         xmlButton.setSelected(!Settings.instance().isBinPersistance());
 
         return radioPanel;
     }
 
     /**
      * Create name order header.
      *
      * @return
      */
     private JPanel createNameOrderHeader() {
         JPanel nameOrderHeader = new JPanel(new GridLayout());
         JLabel firstHeader = new JLabel("<html><h2>User names order</h2></html>", JLabel.LEFT);
         nameOrderHeader.add(firstHeader);
         nameOrderHeader.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
 
         return nameOrderHeader;
     }
 
     /**
      * Create name order radion buttons.
      *
      * @return
      */
     private JPanel createNameOrderRadionButtons() {
         JRadioButton firstNameButton = new JRadioButton("Firstname first");
         firstNameButton.setActionCommand("firstName");
         firstNameButton.addActionListener(this);
 
         JRadioButton surNameButton = new JRadioButton("Surname first");
         surNameButton.setActionCommand("surName");
         surNameButton.addActionListener(this);
 
         ButtonGroup bg = new ButtonGroup();
         bg.add(firstNameButton);
         bg.add(surNameButton);
 
         JPanel radioPanel = new JPanel(new GridLayout(0, 1));
         radioPanel.add(firstNameButton);
         radioPanel.add(surNameButton);
        radioPanel.add(Box.createVerticalGlue());
 
         firstNameButton.setSelected(Settings.instance().isNameFirst());
         surNameButton.setSelected(!Settings.instance().isNameFirst());
 
         return radioPanel;
     }
 
     /**
      * Create name order header.
      *
      * @return
      */
     private JPanel createFolderSettingHeader() {
         JPanel folderHeader = new JPanel(new GridLayout());
         JLabel firstHeader = new JLabel("<html><h2>Application Folder</h2></html>", JLabel.LEFT);
         folderHeader.add(firstHeader);
         folderHeader.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
 
         return folderHeader;
     }
 
     /**
      * Create name order radion buttons.
      *
      * @return
      */
     private JPanel createFolderSettginsOptions() {
         JRadioButton homeFolder = new JRadioButton("Default folder (user.home)");
         homeFolder.setActionCommand("default");
         homeFolder.addActionListener(this);
 
         String title = "Custom folder";
         if (!Settings.instance().isDefaultAppFolder()) {
             title += " (" + Settings.instance().getAppFolder() + ")";
         }
         this.folder = new JRadioButton(title);
         this.folder.setActionCommand("custom");
         this.folder.addActionListener(this);
 
         ButtonGroup bg = new ButtonGroup();
         bg.add(homeFolder);
         bg.add(this.folder);
 
         JPanel radioPanel = new JPanel(new GridLayout(0, 1));
         radioPanel.add(homeFolder);
         radioPanel.add(this.folder);
        radioPanel.add(Box.createVerticalGlue());
 
         homeFolder.setSelected(Settings.instance().isDefaultAppFolder());
         homeFolder.setFocusPainted(Settings.instance().isDefaultAppFolder());
         this.folder.setSelected(!Settings.instance().isDefaultAppFolder());
         this.folder.setFocusPainted(!Settings.instance().isDefaultAppFolder());
 
         return radioPanel;
     }
 
     /**
      * Create notification settings header.
      *
      * @return
      */
     private JPanel createNotificationSettingsHeader() {
         JPanel notifHeader = new JPanel(new GridLayout());
         JLabel firstHeader = new JLabel("<html><h2>Display Notifications</h2></html>", JLabel.LEFT);
         notifHeader.add(firstHeader);
         notifHeader.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
 
         return notifHeader;
     }
 
     /**
      * Create notification settings radio buttons.
      *
      * @return
      */
     private JPanel createNotificationSettingsOptions() {
         JRadioButton monthButton = new JRadioButton("Month in advance");
         monthButton.setActionCommand("month");
         monthButton.addActionListener(this);
 
         JRadioButton weekButton = new JRadioButton("Week in advance");
         weekButton.setActionCommand("week");
         weekButton.addActionListener(this);
 
         JRadioButton dayButton = new JRadioButton("Day in advance");
         dayButton.setActionCommand("day");
         dayButton.addActionListener(this);
 
         JRadioButton neverButton = new JRadioButton("Never");
         neverButton.setActionCommand("never");
         neverButton.addActionListener(this);
 
         ButtonGroup bg = new ButtonGroup();
         bg.add(monthButton);
         bg.add(weekButton);
         bg.add(dayButton);
 
         JPanel radioPanel = new JPanel(new GridLayout(0, 1));
         radioPanel.add(monthButton);
         radioPanel.add(weekButton);
         radioPanel.add(dayButton);
         radioPanel.add(neverButton);
        radioPanel.add(Box.createVerticalGlue());
 
         monthButton.setSelected(NotificationsEnum.MONTH.equals(Settings.instance().getNotificationsSettings()));
         monthButton.setFocusPainted(NotificationsEnum.MONTH.equals(Settings.instance().getNotificationsSettings()));
         weekButton.setSelected(NotificationsEnum.WEEK.equals(Settings.instance().getNotificationsSettings()));
         weekButton.setFocusPainted(NotificationsEnum.WEEK.equals(Settings.instance().getNotificationsSettings()));
         dayButton.setSelected(NotificationsEnum.DAY.equals(Settings.instance().getNotificationsSettings()));
         dayButton.setFocusPainted(NotificationsEnum.DAY.equals(Settings.instance().getNotificationsSettings()));
         neverButton.setSelected(NotificationsEnum.NEVER.equals(Settings.instance().getNotificationsSettings()));
         neverButton.setFocusPainted(NotificationsEnum.NEVER.equals(Settings.instance().getNotificationsSettings()));
 
         return radioPanel;
     }
 
     /**
      * Create services header.
      *
      * @return
      */
     private JPanel createServicesHeader() {
         JPanel firstHeaderPanel = new JPanel(new GridLayout());
         JLabel firstHeader = new JLabel("<html><h2>Connected services</h2></html>", JLabel.LEFT);
         firstHeaderPanel.add(firstHeader);
         firstHeaderPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
         return firstHeaderPanel;
     }
 
     /**
      * Create Facebook action button.
      *
      * @return
      */
     private JPanel createFacebookActionButton() {
         JPanel facebookPanel = new JPanel();
         facebookPanel.setLayout(new BoxLayout(facebookPanel, BoxLayout.X_AXIS));
         // check if facebook token is valid
         boolean valid = new FacebookOauth().isTokenValid();
         // offer connection to facebook
         if (!valid) {
             this.fbButton = new JButton("Connect to Facebook");
             this.fbButton.setActionCommand(ActionCommands.FACEBOOK_CONNECT.toString());
         } // if user is connected, he can invalidate the token
         else {
             this.fbButton = new JButton("Disconnect from Facebook");
             this.fbButton.setActionCommand(ActionCommands.FACEBOOK_DISCONNECT.toString());
         }
         this.fbButton.setIcon(new ImageIcon(this.getClass().getResource("/res/facebook.png")));
         this.fbButton.setIconTextGap(10);
         this.fbButton.addActionListener(this);
         this.fbButton.setSelected(false);
         this.fbButton.setHorizontalAlignment(SwingConstants.LEFT);
         facebookPanel.add(this.fbButton);
         facebookPanel.add(Box.createHorizontalGlue());
         facebookPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
         return facebookPanel;
     }
 
     /**
      * Create Google action button.
      *
      * @return
      */
     private JPanel createGoogleActionButton() {
         JPanel googlePanel = new JPanel();
         googlePanel.setLayout(new BoxLayout(googlePanel, BoxLayout.X_AXIS));
         // check if the token is valid
         boolean valid = new GoogleOauth().isTokenValid();
         // if user is connected, he can invalidate the token
         if (valid) {
             this.googButton = new JButton("Disconnect from Google");
             this.googButton.setActionCommand(ActionCommands.GOOGLE_DISCONNECT.toString());
         } // offer connection to google
         else {
             this.googButton = new JButton("Connect to Google");
             this.googButton.setActionCommand(ActionCommands.GOOGLE_CONNECT.toString());
         }
         this.googButton.setIcon(new ImageIcon(this.getClass().getResource("/res/google.png")));
         this.googButton.setIconTextGap(10);
         this.googButton.addActionListener(this);
         this.googButton.setSelected(false);
         this.googButton.setHorizontalAlignment(SwingConstants.LEFT);
         googlePanel.add(this.googButton);
         googlePanel.add(Box.createHorizontalGlue());
         googlePanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
         return googlePanel;
     }
 
     /**
      * Disconnect user from Facebook.
      *
      * @throws HeadlessException
      */
     private void facebookDisconnect() throws HeadlessException {
         int reply = JOptionPane.showConfirmDialog(null, "Are you sure?", "Disconnect from Facebook",
                 JOptionPane.YES_NO_OPTION);
         if (reply == JOptionPane.YES_OPTION) {
             this.database.removeToken(ServicesEnum.FACEBOOK);
             this.fbButton.setText("Connect to Facebook");
             this.fbButton.setActionCommand(ActionCommands.FACEBOOK_CONNECT.toString());
         }
     }
 
     /**
      * Disconnect user from Google.
      *
      * @throws HeadlessException
      */
     private void googleDisconnect() throws HeadlessException {
         int reply = JOptionPane.showConfirmDialog(null, "Are you sure?", "Disconnect from Google",
                 JOptionPane.YES_NO_OPTION);
         if (reply == JOptionPane.YES_OPTION) {
             this.database.removeToken(ServicesEnum.GOOGLE);
             this.googButton.setText("Connect to Google");
             this.googButton.setActionCommand(ActionCommands.GOOGLE_CONNECT.toString());
         }
     }
 
     /**
      * Connect user to Facebook and save generated authentication token.
      *
      * @throws HeadlessException
      */
     private void facebookConnect() throws HeadlessException {
         JOptionPane.showMessageDialog(this, "After you confirm this dialog a window with Facebook\n"
                 + "authentication will open. Please copy generated code\n"
                 + "(from the browser navigation bar) to GJAddr.", "Instructions",
                 JOptionPane.INFORMATION_MESSAGE);
         new FacebookOauth().authenticate();
         String s = (String) JOptionPane.showInputDialog(this, "Authentication token:",
                 "Authentication token", JOptionPane.PLAIN_MESSAGE, null, null, null);
         // user didn't give any token
         if (s == null || s.isEmpty()) {
             return;
         }
         // set cursor to wait cursor
         this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
         // add token to database
         AuthToken token = new AuthToken(ServicesEnum.FACEBOOK, s);
         this.database.addToken(token);
         // check if token is valid and display information message
         FacebookOauth foa = new FacebookOauth();
         boolean valid = foa.isTokenValid();
         // set cursor back
         this.setCursor(Cursor.getDefaultCursor());
         if (valid) {
             JOptionPane.showMessageDialog(this, "You were successfuly connected to Facebook.", "Success",
                     JOptionPane.INFORMATION_MESSAGE);
             this.fbButton.setText("Disconnect from Facebook");
             this.fbButton.setActionCommand(ActionCommands.FACEBOOK_DISCONNECT.toString());
         } else {
             JOptionPane.showMessageDialog(this, "Connection was unsuccessful. Please try again.", "Failure",
                     JOptionPane.INFORMATION_MESSAGE);
         }
     }
 
     /**
      * Connect user to Google and save generated authentication token.
      *
      * @throws HeadlessException
      */
     private void googleConnect() throws HeadlessException {
         JOptionPane.showMessageDialog(this, "After you confirm this dialog a window with Google\n"
                 + "authentication will open. Please copy generated code to GJAddr.", "Instructions",
                 JOptionPane.INFORMATION_MESSAGE);
         GoogleOauth goa = new GoogleOauth();
         goa.authenticate();
         String s = (String) JOptionPane.showInputDialog(this, "Authentication token:",
                 "Authentication token", JOptionPane.PLAIN_MESSAGE, null, null, null);
         // user didn't give any token
         if (s == null || s.isEmpty()) {
             return;
         }
         // set cursor to wait cursor
         this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
         // add token to database
         AuthToken token = goa.authenticate(s);
         // set cursor back
         this.setCursor(Cursor.getDefaultCursor());
         if (token != null) {
             this.database.addToken(token);
             JOptionPane.showMessageDialog(this, "You were successfuly connected to Google.", "Success",
                     JOptionPane.INFORMATION_MESSAGE);
             this.googButton.setText("Disconnect from Google");
             this.googButton.setActionCommand(ActionCommands.GOOGLE_DISCONNECT.toString());
         } else {
             JOptionPane.showMessageDialog(this, "Connection was unsuccessful. Please try again.", "Failure",
                     JOptionPane.INFORMATION_MESSAGE);
         }
     }
 
     /**
      * Assign actions to components.
      *
      * @param ae
      */
     @Override
     public void actionPerformed(ActionEvent ae) {
         // facebook button pressed
         if (ae.getSource() == this.fbButton) {
             String action = this.fbButton.getActionCommand();
             if (action.equals(ActionCommands.FACEBOOK_CONNECT.toString())) {
                 this.facebookConnect();
             } else {
                 this.facebookDisconnect();
             }
         } // google button pressed
         else if (ae.getSource() == this.googButton) {
             String action = this.googButton.getActionCommand();
             if (action.equals(ActionCommands.GOOGLE_CONNECT.toString())) {
                 this.googleConnect();
             } else {
                 this.googleDisconnect();
             }
         } // persistance changed
         else if (ae.getActionCommand().equals("bin") || ae.getActionCommand().equals("xml")) {
             Settings.instance().changePersistance();
         } // name order changed
         else if (ae.getActionCommand().equals("firstName") || ae.getActionCommand().equals("surName")) {
             Settings.instance().changeNameOrder();
         } else if (ae.getActionCommand().equals("month")) {
             Settings.instance().setNotificationsSettings(NotificationsEnum.MONTH);
         } else if (ae.getActionCommand().equals("week")) {
             Settings.instance().setNotificationsSettings(NotificationsEnum.WEEK);
         } else if (ae.getActionCommand().equals("day")) {
             Settings.instance().setNotificationsSettings(NotificationsEnum.DAY);
         } else if (ae.getActionCommand().equals("never")) {
             Settings.instance().setNotificationsSettings(NotificationsEnum.NEVER);
         } else if (ae.getActionCommand().equals("default")) {
             Settings.instance().setDefaultApplicationFolder();
         } else if (ae.getActionCommand().equals("custom")) {
             JFileChooser chooser = new JFileChooser();
             chooser.setCurrentDirectory(new File(System.getProperty("user.home")));
             chooser.setDialogTitle("Select Application Folder");
             chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
             chooser.setAcceptAllFileFilterUsed(false);
             if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                 Settings.instance().setApplicationFolder(chooser.getSelectedFile().toString());
                 this.folder.setText("Custom folder (" + chooser.getSelectedFile().toString() + ")");
             } else {
                 LoggerFactory.getLogger(this.getClass()).info("Action was canceled by user.");
             }
         }
     }
 }
