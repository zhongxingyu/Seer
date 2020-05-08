 /*
  * MainUI.java
  *
  * Created on 10 April 2008, 17:25
  */
 package xmppclient;
 
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import xmppclient.chat.ChatUI;
 import xmppclient.chat.MultiUserChatUI;
 import java.awt.AWTException;
 import java.awt.Image;
 import java.awt.MenuItem;
 import java.awt.PopupMenu;
 import java.awt.SystemTray;
 import java.awt.TrayIcon;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPopupMenu;
 import javax.swing.JSeparator;
 import javax.swing.JToolTip;
 import javax.swing.SwingUtilities;
 import javax.swing.ToolTipManager;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreePath;
 import org.jivesoftware.smack.PacketListener;
 import org.jivesoftware.smack.PrivacyListManager;
 import org.jivesoftware.smack.Roster;
 import org.jivesoftware.smack.RosterEntry;
 import org.jivesoftware.smack.RosterGroup;
 import org.jivesoftware.smack.RosterListener;
 import org.jivesoftware.smack.XMPPConnection;
 import org.jivesoftware.smack.XMPPException;
 import org.jivesoftware.smack.filter.PacketTypeFilter;
 import org.jivesoftware.smack.packet.Packet;
 import org.jivesoftware.smack.packet.Presence;
 import org.jivesoftware.smack.packet.PrivacyItem;
 import org.jivesoftware.smack.util.StringUtils;
 import org.jivesoftware.smackx.filetransfer.FileTransferListener;
 import org.jivesoftware.smackx.filetransfer.FileTransferManager;
 import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
 import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
 import org.jivesoftware.smackx.muc.MultiUserChat;
 import org.jivesoftware.smackx.packet.VCard;
 import xmppclient.audio.ui.AudioLibraryUI;
 import xmppclient.audio.AudioManager;
 import xmppclient.audio.ui.StreamUI;
 import xmppclient.images.Icons;
 import xmppclient.images.tango.TangoIcons;
 import xmppclient.chat.InvitationReceivedUI;
 import xmppclient.jingle.IncomingSessionUI;
 import xmppclient.jingle.JingleSessionRequest;
 import xmppclient.vcard.VCardEditor;
 import xmppclient.jingle.JingleManager;
 import xmppclient.jingle.JingleSessionRequestListener;
 
 /**
  * The main user interface of the application. This class displays the contact
  * list. It also acts as a listener for various events. These include:
  * <ul>
  * <li>Normal file transfer requests</li>
  * <li>Jingle file transfer requests (used for audio streaming)</li> 
  * <li>Changes in the roster</li>
  * <li>Multiuser chat invitations</li>
  * <li>Subscription requests (when another user adds this user to their contact 
  * list)</li>
  * </ul>
  * @author Lee Boynton (323326)
  */
 public class MainUI extends javax.swing.JFrame implements FileTransferListener, JingleSessionRequestListener, RosterListener
 {
     /** The XMPP connection to the server */
     public static XMPPConnection connection;
     private TrayIcon trayIcon;
     private SystemTray tray;
     private Image appIcon = new ImageIcon(this.getClass().getResource(
             "/xmppclient/images/user.png")).getImage();
     /**
      * The chat UI handles the one-to-one conversations, and listens for new chats
      * and messages.
      */
     public static ChatUI chatUI;
     private String accountName;
     private final int SORT_BY_STATUS = 0;
     private final int SORT_BY_GROUP = 1;
     private int sortMethod = SORT_BY_STATUS;
     private JingleManager jingleManager;
     private AudioManager audioManager;
     /**
      * The settings manager is used for storing and loading settings associated
      * with the XMPP connection instance
      */
     public static SettingsManager settingsManager;
 
     /**
      * Creates the main user interface which displays the contacts etc
      * @param connection The XMPP connection the server to use
      * @param accountName The name of the account the connection is associated with,
      * so that configuration information can be stored with the specified account.
      * If this is null then the account name will be set to the JID of the user.
      */
     public MainUI(XMPPConnection connection, String accountName)
     {
         MainUI.connection = connection;
         this.accountName = accountName;
         if (accountName == null)
         {
             this.accountName = connection.getUser();
         }
         jingleManager = new JingleManager(connection);
         settingsManager = new SettingsManager(connection.getUser());
         audioManager = new AudioManager(connection, settingsManager.createDirectory(SettingsManager.AUDIO_DIR).getAbsolutePath(), jingleManager);
         chatUI = new ChatUI();
         initComponents();
         initSystemTray();
         initStatusComboBox();
         updateContacts();
         addListeners();
         contactTree.requestFocus();
     }
 
     private void addListeners()
     {
         MultiUserChat.addInvitationListener(connection, new InvitationReceivedUI(this, true));
         Roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.manual);
         connection.getChatManager().addChatListener(chatUI);
         connection.addPacketListener(new SubscriptionRequestListener(), new PacketTypeFilter(Presence.class));
         FileTransferManager manager = new FileTransferManager(connection);
         manager.addFileTransferListener(this);
         connection.getRoster().addRosterListener(this);
     }
 
     private void initStatusComboBox()
     {
         statusComboBox.removeAllItems();
 
         // add default presences
         statusComboBox.addItem(new Presence(
                 Presence.Type.available,
                 "Online",
                 0,
                 Presence.Mode.available));
         statusComboBox.addItem(new Presence(
                 Presence.Type.available,
                 "Away",
                 0,
                 Presence.Mode.away));
         statusComboBox.addItem(new Presence(
                 Presence.Type.available,
                 "Extended away",
                 0,
                 Presence.Mode.xa));
         statusComboBox.addItem(new Presence(
                 Presence.Type.available,
                 "Busy",
                 0,
                 Presence.Mode.dnd));
         statusComboBox.addItem(new Presence(
                 Presence.Type.available,
                 "Free to chat",
                 0,
                 Presence.Mode.chat));
         statusComboBox.addItem(new Presence(
                 Presence.Type.unavailable,
                 "Offline",
                 0,
                 Presence.Mode.away));
 
         statusComboBox.addItem(new JSeparator());
         statusComboBox.addItem(new CustomStatusDialog(this, true));
         statusComboBox.addItem(new JSeparator());
 
         // add custom statuses
         for (Presence presence : settingsManager.getPresences())
         {
             statusComboBox.addItem(presence);
         }
 
         statusComboBox.setSelectedIndex(0);
     }
 
     private void initSystemTray()
     {
         if (SystemTray.isSupported())
         {
             tray = SystemTray.getSystemTray();
             //Image image = Toolkit.getDefaultToolkit().getImage("images/user.png");
 
             PopupMenu popup = new PopupMenu();
 
             trayIcon = new TrayIcon(appIcon, "XMPPClient", popup);
             trayIcon.setImageAutoSize(true);
             MenuItem exitMenuItem = new MenuItem("Exit");
             MenuItem showMenuItem = new MenuItem("Show/hide");
             exitMenuItem.addActionListener(new ActionListener()
             {
                 @Override
                 public void actionPerformed(ActionEvent evt)
                 {
                     exit();
                 }
             });
             showMenuItem.addActionListener(new ActionListener()
             {
                 @Override
                 public void actionPerformed(ActionEvent evt)
                 {
                     toggleWindowVisibility();
                 }
             });
 
             popup.add(showMenuItem);
             popup.addSeparator();
             popup.add(exitMenuItem);
 
             trayIcon.addMouseListener(new MouseAdapter()
             {
                 @Override
                 public void mouseClicked(MouseEvent e)
                 {
                     // check the correct button was pressed
                     if (e.getButton() != MouseEvent.BUTTON1)
                     {
                         return;
                     // toggle only once on double click
                     }
                     if (e.getClickCount() == 2)
                     {
                         return;
                     }
                     toggleWindowVisibility();
                 }
             });
 
             try
             {
                 tray.add(trayIcon);
             } catch (AWTException e)
             {
                 System.err.println("TrayIcon could not be added.");
             }
         }
     }
 
     private void sortContactsByGroup()
     {
         DefaultMutableTreeNode root = new DefaultMutableTreeNode("Contacts");
         DefaultTreeModel model = new DefaultTreeModel(root);
 
         Collection<RosterGroup> groups = connection.getRoster().getGroups();
         DefaultMutableTreeNode groupNode;
 
         // get defined groups
         for (RosterGroup group : groups)
         {
             groupNode = new DefaultMutableTreeNode(group.getName());
             root.add(groupNode);
 
             for (RosterEntry entry : group.getEntries())
             {
                 groupNode.add(new DefaultMutableTreeNode(entry));
             }
         }
 
         // get unfiled contacts
         if (connection.getRoster().getUnfiledEntryCount() > 0)
         {
             DefaultMutableTreeNode unfiled = new DefaultMutableTreeNode("Unfiled");
             root.add(unfiled);
             for (RosterEntry entry : connection.getRoster().getUnfiledEntries())
             {
                 unfiled.add(new DefaultMutableTreeNode(entry));
             }
         }
 
         contactTree.setModel(model);
 
         int row = 0;
         while (row < contactTree.getRowCount())
         {
             contactTree.expandRow(row++);
         }
     }
 
     private void sortContactsByStatus()
     {
         DefaultMutableTreeNode root = new DefaultMutableTreeNode("Contacts");
         DefaultMutableTreeNode online = new DefaultMutableTreeNode("Online");
         DefaultMutableTreeNode offline = new DefaultMutableTreeNode("Offline");
 
         DefaultTreeModel model = new DefaultTreeModel(root);
 
         root.add(online);
         root.add(offline);
 
         Collection<RosterEntry> entries = connection.getRoster().getEntries();
 
         for (RosterEntry entry : entries)
         {
             if (connection.getRoster().getPresence(entry.getUser()).getType().equals(Presence.Type.available))
             {
                 online.add(new DefaultMutableTreeNode(entry));
             } else
             {
                 offline.add(new DefaultMutableTreeNode(entry));
             }
         }
 
         contactTree.setModel(model);
 
         int row = 0;
         while (row < contactTree.getRowCount())
         {
             contactTree.expandRow(row++);
         }
     }
 
     private void toggleWindowVisibility()
     {
         if (MainUI.this.isVisible())
         {
             MainUI.this.setVisible(false);
         } else
         {
             MainUI.this.setVisible(true);
         }
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         sortContactsButtonGroup = new javax.swing.ButtonGroup();
         jButton1 = new javax.swing.JButton();
         contentPanel = new javax.swing.JPanel();
         toolBar = new javax.swing.JToolBar();
         addContactButton = new javax.swing.JButton();
         joinConferenceButton = new javax.swing.JButton();
         vCardButton = new javax.swing.JButton();
         avatarButton = new javax.swing.JButton();
         viewReceivedFilesButton = new javax.swing.JButton();
         viewAudioFilesButton = new javax.swing.JButton();
         nicknameTextField = new javax.swing.JTextField();
         statusComboBox = new javax.swing.JComboBox()
         {
             public void setSelectedIndex(int index)
             {
                 // prevent the separators from being selected
                 if(statusComboBox.getItemAt(index) instanceof JSeparator)
                 return;
 
                 super.setSelectedIndex(index);
             }
         };//);
         avatarLabel = new javax.swing.JLabel()
         {
             public void setIcon(Icon icon)
             {
                 super.setIcon(icon);
                 if(icon == null) this.setVisible(false);
                 else this.setVisible(true);
             }
         };//);
         contactTreeScrollPane = new javax.swing.JScrollPane();
         contactTree = new javax.swing.JTree()
         {
             public JToolTip createToolTip()
             {
                 JToolTip tip = super.createToolTip();
                 String text = tip.getTipText();
                 return new JToolTip();
             }
 
             public String getToolTipText(MouseEvent evt)
             {
                 TreePath path = getPathForLocation(evt.getX(), evt.getY());
                 if(path == null) return null;
                 if(path.getLastPathComponent() instanceof DefaultMutableTreeNode)
                 {
                     DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                     if(node.getUserObject() instanceof RosterEntry)
                     {
                         RosterEntry entry = (RosterEntry) node.getUserObject();
                         return StringUtils.unescapeNode(entry.getUser());
                     }
                 }
 
                 return null;
             }
         };//);
         menuBar = new javax.swing.JMenuBar();
         fileMenu = new javax.swing.JMenu();
         signOutMenuItem = new javax.swing.JMenuItem();
         minimiseMenuItem = new javax.swing.JMenuItem();
         exitMenuItem = new javax.swing.JMenuItem();
         contactsMenu = new javax.swing.JMenu();
         sortMenu = new javax.swing.JMenu();
         statusRadioButton = new javax.swing.JRadioButtonMenuItem();
         groupRadioButton = new javax.swing.JRadioButtonMenuItem();
         toolsMenu = new javax.swing.JMenu();
         createChatRoomMenuItem = new javax.swing.JMenuItem();
         joinChatRoomMenuItem = new javax.swing.JMenuItem();
         aboutMenu = new javax.swing.JMenu();
         jMenuItem1 = new javax.swing.JMenuItem();
 
         jButton1.setText("jButton1");
 
         setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
         setTitle("XMPPClient");
         setIconImage(appIcon);
         setLocationByPlatform(true);
         setMinimumSize(new java.awt.Dimension(200, 300));
         setName("XMPPClient"); // NOI18N
         addWindowListener(new java.awt.event.WindowAdapter() {
             public void windowClosing(java.awt.event.WindowEvent evt) {
                 formWindowClosing(evt);
             }
         });
 
         toolBar.setFloatable(false);
         toolBar.setRollover(true);
 
         addContactButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/xmppclient/images/user_add.png"))); // NOI18N
         addContactButton.setToolTipText("Add contact");
         addContactButton.setFocusable(false);
         addContactButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         addContactButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         addContactButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 addContactButtonActionPerformed(evt);
             }
         });
         toolBar.add(addContactButton);
 
         joinConferenceButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/xmppclient/images/tango/internet-group-chat.png"))); // NOI18N
         joinConferenceButton.setToolTipText("Join conference");
         joinConferenceButton.setFocusable(false);
         joinConferenceButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         joinConferenceButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         joinConferenceButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 joinConferenceButtonActionPerformed(evt);
             }
         });
         toolBar.add(joinConferenceButton);
 
         vCardButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/xmppclient/images/vcard.png"))); // NOI18N
         vCardButton.setToolTipText("Edit VCard");
         vCardButton.setFocusable(false);
         vCardButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         vCardButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         vCardButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 vCardButtonActionPerformed(evt);
             }
         });
         toolBar.add(vCardButton);
 
         avatarButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/xmppclient/images/tango/image-x-generic.png"))); // NOI18N
         avatarButton.setToolTipText("Change avatar");
         avatarButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 avatarButtonActionPerformed(evt);
             }
         });
         toolBar.add(avatarButton);
 
         viewReceivedFilesButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/xmppclient/images/tango/document-save-16x16.png"))); // NOI18N
         viewReceivedFilesButton.setToolTipText("View received files");
         viewReceivedFilesButton.setFocusable(false);
         viewReceivedFilesButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         viewReceivedFilesButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         viewReceivedFilesButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 viewReceivedFilesButtonActionPerformed(evt);
             }
         });
         toolBar.add(viewReceivedFilesButton);
 
         viewAudioFilesButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/xmppclient/images/tango/audio-x-generic16x16.png"))); // NOI18N
         viewAudioFilesButton.setToolTipText("Open audio folder");
         viewAudioFilesButton.setFocusable(false);
         viewAudioFilesButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         viewAudioFilesButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         viewAudioFilesButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 viewAudioFilesButtonActionPerformed(evt);
             }
         });
         toolBar.add(viewAudioFilesButton);
 
         nicknameTextField.setFont(new java.awt.Font("Tahoma", 1, 12));
         nicknameTextField.setText(Utils.getNickname());
         nicknameTextField.setToolTipText("Press enter to set the nickname");
         nicknameTextField.setOpaque(false);
         nicknameTextField.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 nicknameTextFieldActionPerformed(evt);
             }
         });
         nicknameTextField.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusGained(java.awt.event.FocusEvent evt) {
                 nicknameTextFieldFocusGained(evt);
             }
             public void focusLost(java.awt.event.FocusEvent evt) {
                 nicknameTextFieldFocusLost(evt);
             }
         });
 
         statusComboBox.setMaximumRowCount(12);
         statusComboBox.setRenderer(new xmppclient.StatusComboBoxRenderer());
         statusComboBox.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 statusComboBoxActionPerformed(evt);
             }
         });
 
         avatarLabel.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(160, 160, 160), 1, true));
         setAvatar();
 
         contactTree.setModel(null);
         ToolTipManager.sharedInstance().registerComponent(contactTree);
         contactTree.setCellRenderer(new ContactTreeRenderer());
         contactTree.setRootVisible(false);
         contactTree.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 contactTreeMouseClicked(evt);
             }
             public void mousePressed(java.awt.event.MouseEvent evt) {
                 contactTreeMousePressed(evt);
             }
             public void mouseReleased(java.awt.event.MouseEvent evt) {
                 contactTreeMouseReleased(evt);
             }
         });
         contactTreeScrollPane.setViewportView(contactTree);
 
         javax.swing.GroupLayout contentPanelLayout = new javax.swing.GroupLayout(contentPanel);
         contentPanel.setLayout(contentPanelLayout);
         contentPanelLayout.setHorizontalGroup(
             contentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(contentPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(contentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(contactTreeScrollPane, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 236, Short.MAX_VALUE)
                     .addGroup(javax.swing.GroupLayout.Alignment.LEADING, contentPanelLayout.createSequentialGroup()
                         .addGroup(contentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(nicknameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE)
                             .addComponent(statusComboBox, 0, 140, Short.MAX_VALUE)
                             .addComponent(toolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 140, Short.MAX_VALUE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(avatarLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)))
                 .addContainerGap())
         );
         contentPanelLayout.setVerticalGroup(
             contentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(contentPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(contentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(contentPanelLayout.createSequentialGroup()
                         .addComponent(nicknameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(statusComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(toolBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addComponent(avatarLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(contactTreeScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 259, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         fileMenu.setText("File");
 
         signOutMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/xmppclient/images/tango/log-out-22x22.png"))); // NOI18N
         signOutMenuItem.setText("Sign out");
         signOutMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 signOutMenuItemActionPerformed(evt);
             }
         });
         fileMenu.add(signOutMenuItem);
 
         minimiseMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_M, java.awt.event.InputEvent.CTRL_MASK));
         minimiseMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/xmppclient/images/tango/go-down-22x22.png"))); // NOI18N
         minimiseMenuItem.setText("Minimise");
         minimiseMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 minimiseMenuItemActionPerformed(evt);
             }
         });
         fileMenu.add(minimiseMenuItem);
 
         exitMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_W, java.awt.event.InputEvent.CTRL_MASK));
         exitMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/xmppclient/images/tango/system-shutdown-22x22.png"))); // NOI18N
         exitMenuItem.setText("Exit");
         exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 exitMenuItemActionPerformed(evt);
             }
         });
         fileMenu.add(exitMenuItem);
 
         menuBar.add(fileMenu);
 
         contactsMenu.setText("Contacts");
 
         sortMenu.setText("Sort by");
 
         sortContactsButtonGroup.add(statusRadioButton);
         statusRadioButton.setSelected(true);
         statusRadioButton.setText("Status");
         statusRadioButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 statusRadioButtonActionPerformed(evt);
             }
         });
         sortMenu.add(statusRadioButton);
 
         sortContactsButtonGroup.add(groupRadioButton);
         groupRadioButton.setText("Group");
         groupRadioButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 groupRadioButtonActionPerformed(evt);
             }
         });
         sortMenu.add(groupRadioButton);
 
         contactsMenu.add(sortMenu);
 
         menuBar.add(contactsMenu);
 
         toolsMenu.setText("Tools");
         toolsMenu.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 toolsMenuActionPerformed(evt);
             }
         });
 
         createChatRoomMenuItem.setText("Create conference");
         createChatRoomMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 createChatRoomMenuItemActionPerformed(evt);
             }
         });
         toolsMenu.add(createChatRoomMenuItem);
 
         joinChatRoomMenuItem.setText("Join conference");
         joinChatRoomMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 joinChatRoomMenuItemActionPerformed(evt);
             }
         });
         toolsMenu.add(joinChatRoomMenuItem);
 
         menuBar.add(toolsMenu);
 
         aboutMenu.setText("Help");
 
         jMenuItem1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/xmppclient/images/tango/help-browser.png"))); // NOI18N
         jMenuItem1.setText("About XMPP Client");
         jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItem1ActionPerformed(evt);
             }
         });
         aboutMenu.add(jMenuItem1);
 
         menuBar.add(aboutMenu);
 
         setJMenuBar(menuBar);
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(contentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(contentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
         exit();
     }//GEN-LAST:event_exitMenuItemActionPerformed
 
     private void signOutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_signOutMenuItemActionPerformed
         signOut();
 }//GEN-LAST:event_signOutMenuItemActionPerformed
 
     private void nicknameTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nicknameTextFieldActionPerformed
         VCard vCard = new VCard();
         try
         {
             // first try to get stored VCard
             vCard.load(connection);
             System.out.println("Loaded nickname");
         } catch (XMPPException e)
         {
         } // no vcard
 
         vCard.setNickName(nicknameTextField.getText());
 
         if (nicknameTextField.getText().equals(""))
         {
             vCard.setNickName(connection.getUser());
         }
         try
         {
             // send the new nickname
             vCard.save(connection);
             System.out.println("Saved nickname");
         } catch (XMPPException e)
         {
             e.printStackTrace();
         }
 
         connection.sendPacket((Presence) statusComboBox.getSelectedItem());
 
         if (nicknameTextField.getText().equals(""))
         {
             nicknameTextField.setText(connection.getUser());
         }
 
         contactTree.requestFocus();
     }//GEN-LAST:event_nicknameTextFieldActionPerformed
 
     private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
 
         if (Utils.loadProperty(accountName, "display_minimise_message").equals("false"))
         {
             setVisible(false);
             return;
         }
 
         JOptionPane.showMessageDialog(this,
                 "Closing the window minimises to tray. " +
                 "\nTo close, right click the tray icon and select exit.",
                 "Minimising client",
                 JOptionPane.INFORMATION_MESSAGE);
 
         Utils.saveProperty(accountName, "display_minimise_message", "false");
 
         setVisible(false);
     }//GEN-LAST:event_formWindowClosing
 
     private void statusComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_statusComboBoxActionPerformed
         if (statusComboBox.getSelectedItem() instanceof CustomStatusDialog)
         {
             CustomStatusDialog dialog = (CustomStatusDialog) statusComboBox.getSelectedItem();
             Presence presence = dialog.showDialog();
             if (presence == null)
             {
                 statusComboBox.setSelectedItem(statusComboBox.getSelectedItem());
                 return;
             }
             settingsManager.addPresence(presence);
             initStatusComboBox();
             statusComboBox.setSelectedItem(presence);
         }
         connection.sendPacket((Presence) statusComboBox.getSelectedItem());
     }//GEN-LAST:event_statusComboBoxActionPerformed
 
 private void avatarButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_avatarButtonActionPerformed
     new AvatarChooser(this, false);
 }//GEN-LAST:event_avatarButtonActionPerformed
 
 private void toolsMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toolsMenuActionPerformed
 }//GEN-LAST:event_toolsMenuActionPerformed
 
 private void vCardButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vCardButtonActionPerformed
     VCard vCard = new VCard();
     try
     {
         vCard.load(connection);
     } catch (XMPPException ex)
     {
     }
     new VCardEditor(vCard, true).setVisible(true);
 }//GEN-LAST:event_vCardButtonActionPerformed
 
 private void addContactButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addContactButtonActionPerformed
     showAddContactDialog();
 }//GEN-LAST:event_addContactButtonActionPerformed
 
 private void joinChatRoomMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_joinChatRoomMenuItemActionPerformed
     joinConference();
 }//GEN-LAST:event_joinChatRoomMenuItemActionPerformed
 
 private void createChatRoomMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createChatRoomMenuItemActionPerformed
     String room = JOptionPane.showInputDialog(this, "Enter room name");
 
     if (room == null || room.equals(""))
     {
         return;
     }
 
     MultiUserChatUI mucui = new MultiUserChatUI(room);
 
     try
     {
         mucui.create(Utils.getNickname());
     } catch (XMPPException ex)
     {
         JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
         return;
     }
 
     mucui.setVisible(true);
 }//GEN-LAST:event_createChatRoomMenuItemActionPerformed
 
 private void minimiseMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_minimiseMenuItemActionPerformed
     setVisible(false);
 }//GEN-LAST:event_minimiseMenuItemActionPerformed
 
 private void contactTreeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_contactTreeMouseClicked
 
     DefaultMutableTreeNode node = (DefaultMutableTreeNode) contactTree.getLastSelectedPathComponent();
     if (node == null)
     {
         return;
     }
 
     if (node.getUserObject() instanceof RosterEntry)
     {
         if (evt.getClickCount() == 2)
         {
             RosterEntry rosterEntry = (RosterEntry) node.getUserObject();
             connection.getChatManager().createChat(rosterEntry.getUser(), chatUI);
         }
     }
 }//GEN-LAST:event_contactTreeMouseClicked
 
 private void groupRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_groupRadioButtonActionPerformed
     sortMethod = SORT_BY_GROUP;
     updateContacts();
 }//GEN-LAST:event_groupRadioButtonActionPerformed
 
 private void statusRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_statusRadioButtonActionPerformed
     sortMethod = SORT_BY_STATUS;
     updateContacts();
 }//GEN-LAST:event_statusRadioButtonActionPerformed
 
 private void contactTreeMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_contactTreeMouseReleased
     showContactPopup(evt);
 }//GEN-LAST:event_contactTreeMouseReleased
 
 private void contactTreeMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_contactTreeMousePressed
     showContactPopup(evt);
 }//GEN-LAST:event_contactTreeMousePressed
 
 private void nicknameTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_nicknameTextFieldFocusGained
     nicknameTextField.setOpaque(true);
     nicknameTextField.repaint();
 }//GEN-LAST:event_nicknameTextFieldFocusGained
 
 private void nicknameTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_nicknameTextFieldFocusLost
     nicknameTextField.setOpaque(false);
     nicknameTextField.repaint();
 }//GEN-LAST:event_nicknameTextFieldFocusLost
 
 private void joinConferenceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_joinConferenceButtonActionPerformed
     joinConference();
 }//GEN-LAST:event_joinConferenceButtonActionPerformed
 
 private void viewReceivedFilesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewReceivedFilesButtonActionPerformed
     try
     {
         Utils.openFileBrowser(settingsManager.getRootDir() + File.separator + SettingsManager.RECEIVED_DIR, true);
     } catch (IOException ex)
     {
         Logger.getLogger(MainUI.class.getName()).log(Level.SEVERE, null, ex);
     } catch (SecurityException ex)
     {
         Logger.getLogger(MainUI.class.getName()).log(Level.SEVERE, null, ex);
     }
     }//GEN-LAST:event_viewReceivedFilesButtonActionPerformed
 private void viewAudioFilesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewAudioFilesButtonActionPerformed
     try
     {
         Utils.openFileBrowser(settingsManager.getRootDir() + File.separator + SettingsManager.AUDIO_DIR, true);
     } catch (Exception ex)
     {
         Logger.getLogger(MainUI.class.getName()).log(Level.SEVERE, null, ex);
     }
 }//GEN-LAST:event_viewAudioFilesButtonActionPerformed
 
 private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItem1ActionPerformed
 {//GEN-HEADEREND:event_jMenuItem1ActionPerformed
    new AboutDialog(this, false).setVisible(true);
 }//GEN-LAST:event_jMenuItem1ActionPerformed
 
     private void joinConference()
     {
         String room = JOptionPane.showInputDialog(this, "Enter room name");
 
         if (room == null || room.equals(""))
         {
             return;
         }
 
         MultiUserChatUI mucui = new MultiUserChatUI(room);
 
         try
         {
             mucui.join(Utils.getNickname());
         } catch (XMPPException ex)
         {
             JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
         }
 
         mucui.setVisible(true);
     }
 
     private void showContactPopup(java.awt.event.MouseEvent evt)
     {
         final JPopupMenu menu = new JPopupMenu();
 
         if (!evt.isPopupTrigger())
         {
             return;
         }
         DefaultMutableTreeNode node = (DefaultMutableTreeNode) contactTree.getClosestPathForLocation(evt.getX(), evt.getY()).getLastPathComponent();
 
         if (node.getUserObject() instanceof RosterGroup)
         {
             final RosterGroup group = (RosterGroup) node.getUserObject();
             JMenuItem groupMenuItem = new JMenuItem(group.getName());
             groupMenuItem.setEnabled(false);
             menu.add(groupMenuItem);
             JMenuItem blockMenuItem = new JMenuItem("Block group");
             blockMenuItem.addActionListener(new ActionListener()
             {
                 @Override
                 public void actionPerformed(ActionEvent e)
                 {
                     try
                     {
                         PrivacyListManager manager = PrivacyListManager.getInstanceFor(connection);
                         PrivacyItem item = new PrivacyItem("group", false, 0);
                         item.setValue(group.getName());
                         List<PrivacyItem> items = new ArrayList<PrivacyItem>();
                         items.add(item);
                         manager.createPrivacyList("default", items);
                         manager.setActiveListName("default");
                         updateContacts();
                     } catch (XMPPException ex)
                     {
                         Logger.getLogger(MainUI.class.getName()).log(Level.SEVERE, null, ex);
                     }
                 }
             });
         }
         if (node.getUserObject() instanceof RosterEntry)
         {
             final RosterEntry entry = (RosterEntry) node.getUserObject();
             JMenuItem nickname = new JMenuItem(Utils.getNickname(entry));
             nickname.setEnabled(false);
             menu.add(nickname);
             menu.add(new JSeparator());
             JMenuItem chat = new JMenuItem("Open chat", Icons.userComment);
             menu.add(chat);
             chat.addActionListener(new ActionListener()
             {
                 @Override
                 public void actionPerformed(ActionEvent e)
                 {
                     connection.getChatManager().createChat(entry.getUser(), chatUI);
                 }
             });
             JMenuItem vcard = new JMenuItem("View VCard", Icons.vcard);
             vcard.addActionListener(new ActionListener()
             {
                 @Override
                 public void actionPerformed(ActionEvent e)
                 {
                     VCard vCard = new VCard();
                     try
                     {
                         vCard.load(connection, entry.getUser());
                     } catch (XMPPException ex)
                     {
                     }
                     new VCardEditor(vCard, false).setVisible(true);
                 }
             });
             menu.add(vcard);
             JMenuItem sendFileMenuItem = new JMenuItem("Send file");
             sendFileMenuItem.addActionListener(new ActionListener()
             {
                 @Override
                 public void actionPerformed(ActionEvent e)
                 {
                     new FileTransferChooser(MainUI.this, true, entry).setVisible(true);
                 }
             });
             menu.add(sendFileMenuItem);
             JMenuItem groupMenuItem = new JMenuItem("Set groups");
             groupMenuItem.addActionListener(new ActionListener()
             {
                 @Override
                 public void actionPerformed(ActionEvent e)
                 {
                     String oldGroups = Utils.getGroupsCSV(entry);
                     String groups =
                             JOptionPane.showInputDialog(MainUI.this,
                             "Insert group names, separated by commas",
                             oldGroups);
                     if (groups == null || groups.equals(oldGroups))
                     {
                         return;
                     }
 
                     // remove user from all groups
                     for (RosterGroup r : entry.getGroups())
                     {
                         try
                         {
                             r.removeEntry(entry);
                         } catch (XMPPException ex)
                         {
                             JOptionPane.showMessageDialog(MainUI.this,
                                     "Could not remove user from group" +
                                     ex.getMessage(),
                                     "Error removing user from group",
                                     JOptionPane.ERROR_MESSAGE);
                         }
                     }
 
                     if (groups.equals(""))
                     {
                         return;
                     }
 
                     String groupsArray[] = groups.split(",");
 
                     RosterGroup rosterGroup;
                     for (String group : groupsArray)
                     {
                         if (connection.getRoster().getGroup(group.trim()) == null)
                         {
                             rosterGroup = connection.getRoster().createGroup(group.trim());
                         } else
                         {
                             rosterGroup = connection.getRoster().getGroup(group.trim());
                         }
                         try
                         {
                             rosterGroup.addEntry(entry);
                         } catch (XMPPException ex)
                         {
                             JOptionPane.showMessageDialog(MainUI.this,
                                     "Could not add user to group" +
                                     ex.getMessage(),
                                     "Error adding user to group",
                                     JOptionPane.ERROR_MESSAGE);
                         }
 
                         updateContacts();
                     }
                 }
             });
             menu.add(groupMenuItem);
             JMenuItem setNameMenuItem = new JMenuItem("Set name");
             setNameMenuItem.addActionListener(new ActionListener()
             {
                 @Override
                 public void actionPerformed(ActionEvent e)
                 {
                     String name = JOptionPane.showInputDialog(MainUI.this,
                             "Enter a name for this user, or leave blank to remove");
 
                     if (name == null || name.equals(""))
                     {
                         return;
                     }
 
                     entry.setName(name);
                     updateContacts();
                 }
             });
             menu.add(setNameMenuItem);
             JMenuItem viewLibraryMenuItem = new JMenuItem("View library");
             viewLibraryMenuItem.addActionListener(new ActionListener()
             {
                 @Override
                 public void actionPerformed(ActionEvent e)
                 {
                     new AudioLibraryUI(MainUI.this, audioManager, entry, jingleManager).setVisible(true);
                 }
             });
             menu.add(viewLibraryMenuItem);
             JMenuItem blockMenuItem = new JMenuItem("Block");
             blockMenuItem.addActionListener(new ActionListener()
             {
                 @Override
                 public void actionPerformed(ActionEvent e)
                 {
                     try
                     {
                         PrivacyListManager manager = PrivacyListManager.getInstanceFor(connection);
                         PrivacyItem item = new PrivacyItem("jid", false, 0);
                         item.setValue(entry.getUser());
                         List<PrivacyItem> items = new ArrayList<PrivacyItem>();
                         items.add(item);
                         manager.createPrivacyList("default", items);
                         manager.setActiveListName("default");
                         updateContacts();
                     } catch (XMPPException ex)
                     {
                         Logger.getLogger(MainUI.class.getName()).log(Level.SEVERE, null, ex);
                     }
                 }
             });
             menu.add(blockMenuItem);
 
             JMenuItem removeMenuItem = new JMenuItem("Delete", Icons.delete);
             removeMenuItem.addActionListener(new ActionListener()
             {
                 @Override
                 public void actionPerformed(ActionEvent e)
                 {
                     // confirm removal
                     if (JOptionPane.showConfirmDialog(MainUI.this,
                             "Remove this user from the roster? This user will\n" +
                             "be removed from all groups and will no longer be\n" +
                             "able to communicate with you.",
                             "Confirm Removal",
                             JOptionPane.YES_NO_CANCEL_OPTION,
                             JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION)
                     {
                         return;
                     }
 
                     // try to remove contact
                     try
                     {
                         connection.getRoster().removeEntry(entry);
                     } catch (XMPPException ex)
                     {
                         JOptionPane.showMessageDialog(MainUI.this,
                                 "Error deleting contact: " + entry.getUser() +
                                 "\n" + ex.getMessage(),
                                 "Error Removing Contact",
                                 JOptionPane.ERROR_MESSAGE);
                     }
                 }
             });
             menu.add(removeMenuItem);
         }
 
         menu.show(evt.getComponent(), evt.getX(), evt.getY());
     }
 
     private void showAddContactDialog()
     {
         String contact = (String) JOptionPane.showInputDialog(this, "Enter the JID of the contact you wish to add", "Add user", JOptionPane.PLAIN_MESSAGE, TangoIcons.users32x32, null, null);
         contact = StringUtils.parseBareAddress(contact);
         Presence request = new Presence(Presence.Type.subscribe);
         request.setTo(contact);
         connection.sendPacket(request);
     }
 
     private void exit()
     {
         requestFocus();
 
         // if the user is signed in then sign them out before exiting
         if (connection != null)
         {
             int confirm = JOptionPane.showConfirmDialog(this,
                     "Do you wish to sign out and close XMPPClient?",
                     "Close XMPPClient",
                     JOptionPane.OK_CANCEL_OPTION);
 
             if (confirm == JOptionPane.OK_OPTION)
             {
                 // prevent any refreshing occuring in the GUI in between being signed out
                 // and calling System.exit which causes not authenticated errors by hiding
                 // the frame
                 setVisible(false);
                 signOut();
             } else
             {
                 return;
             }
         }
 
         System.exit(0);
     }
 
     /**
      * Gets the avatar assoiciated with the local account and, if present, displays it
      */
     public void setAvatar()
     {
         avatarLabel.setIcon(Utils.getAvatar(84));
     }
 
     /**
      * Sets the displayed avatar
      * @param icon Avatar icon
      */
     public void setAvatar(ImageIcon icon)
     {
         avatarLabel.setIcon(Utils.resizeImage(icon, 84));
     }
 
     /**
      * Updates the list of contacts by calling the appropriate contact sorting method
      */
     public void updateContacts()
     {
         SwingUtilities.invokeLater(new Runnable()
         {
             @Override
             public void run()
             {
                 try
                 {
                     if (sortMethod == SORT_BY_STATUS)
                     {
                         sortContactsByStatus();
                     } else
                     {
                         sortContactsByGroup();
                     }
                 } catch (Exception e)
                 {
                 }
             }
         });
     }
 
     private void signOut()
     {
         connection.disconnect();
         connection = null;
         new XMPPClient().setVisible(true);
         if (tray != null)
         {
             tray.remove(trayIcon);
         }
         this.dispose();
     }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JMenu aboutMenu;
     private javax.swing.JButton addContactButton;
     private javax.swing.JButton avatarButton;
     private javax.swing.JLabel avatarLabel;
     private javax.swing.JTree contactTree;
     private javax.swing.JScrollPane contactTreeScrollPane;
     private javax.swing.JMenu contactsMenu;
     private javax.swing.JPanel contentPanel;
     private javax.swing.JMenuItem createChatRoomMenuItem;
     private javax.swing.JMenuItem exitMenuItem;
     private javax.swing.JMenu fileMenu;
     private javax.swing.JRadioButtonMenuItem groupRadioButton;
     private javax.swing.JButton jButton1;
     private javax.swing.JMenuItem jMenuItem1;
     private javax.swing.JMenuItem joinChatRoomMenuItem;
     private javax.swing.JButton joinConferenceButton;
     private javax.swing.JMenuBar menuBar;
     private javax.swing.JMenuItem minimiseMenuItem;
     private javax.swing.JTextField nicknameTextField;
     private javax.swing.JMenuItem signOutMenuItem;
     private javax.swing.ButtonGroup sortContactsButtonGroup;
     private javax.swing.JMenu sortMenu;
     private javax.swing.JComboBox statusComboBox;
     private javax.swing.JRadioButtonMenuItem statusRadioButton;
     private javax.swing.JToolBar toolBar;
     private javax.swing.JMenu toolsMenu;
     private javax.swing.JButton vCardButton;
     private javax.swing.JButton viewAudioFilesButton;
     private javax.swing.JButton viewReceivedFilesButton;
     // End of variables declaration//GEN-END:variables
 
     /**
      * Called when a file transfer request is received. A dialog is opened asking the user
      * if they wish to accept the file transfer. If so, the file is stored in the received 
      * directory.
      * @param request The file transfer request
      */
     @Override
     public void fileTransferRequest(FileTransferRequest request)
     {
         Object[] options =
         {
             "Accept", "Reject"
         };
 
         String desc = request.getDescription();
 
         if (desc == null)
         {
             desc = "None entered";
         }
 
         int option = JOptionPane.showOptionDialog(this,
                 "File transfer request received from " + request.getRequestor() + "\nFilename: " + request.getFileName() +
                 "\nDescription: " + desc +
                 "\nWould you like to accept or reject it?",
                 "File Transfer Request",
                 JOptionPane.YES_NO_OPTION,
                 JOptionPane.QUESTION_MESSAGE,
                 null,
                 options,
                 options[0]);
 
         if (option == JOptionPane.YES_OPTION)
         {
             IncomingFileTransfer transfer = request.accept();
             File dir = settingsManager.createDirectory(SettingsManager.RECEIVED_DIR);
 
             try
             {
                 transfer.recieveFile(new File(dir.getAbsolutePath() + File.separator + request.getFileName()));
                 new FileTransferUI(transfer);
             } catch (InterruptedException ex)
             {
                 ex.printStackTrace();
             } catch (XMPPException ex)
             {
                 ex.printStackTrace();
             }
         } else
         {
             request.reject();
         }
     }
 
     private class SubscriptionRequestListener implements PacketListener
     {
         @Override
         public void processPacket(Packet packet)
         {
             Presence presence = (Presence) packet;
 
             if (presence.getType() == Presence.Type.subscribe)
             {
                 Object options[] =
                 {
                     "Accept", "Reject"
                 };
 
                 int selection = JOptionPane.showOptionDialog(MainUI.this,
                         "The user " + presence.getFrom() + " wishes to subscribe" +
                         "\nto your presence information. Do you accept or reject?",
                         "Subscription Request",
                         JOptionPane.YES_NO_OPTION,
                         JOptionPane.QUESTION_MESSAGE,
                         null,
                         options,
                         null);
 
                 Presence auth;
 
                 if (selection == JOptionPane.YES_OPTION)
                 {
                     // accept
                     auth = new Presence(Presence.Type.subscribe);
                 } else
                 {
                     // reject
                     auth = new Presence(Presence.Type.unsubscribe);
                 }
 
                 auth.setTo(presence.getFrom());
                 connection.sendPacket(auth);
             }
         }
     }
 
     /**
      * Called when a Jingle file transfer session is requested. This means that
      * a user wishes to send a file.
      * @param request The request that was received
      */
     @Override
     public void sessionRequested(JingleSessionRequest request)
     {
         System.out.println("Jingle session request received");
 
         new IncomingSessionUI(this, false, request).setVisible(true);
     }
 
     /**
      * Used for debugging runtime errors in this class which won't appear
      * when logging in normally
      * @param args Not used
      */
     public static void main(String args[])
     {
         try
         {
             XMPPConnection connection2 = new XMPPConnection("192.168.0.8");
             connection2.connect();
             connection2.login("lee", "password", "home");
             new MainUI(connection2, "Fef");
         } catch (XMPPException ex)
         {
             Logger.getLogger(MainUI.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     /**
      * Updates the contact list when an entry is added to the roster
      * @param addresses The addresses which were added
      * @see #updateContacts()
      */
     @Override
     public void entriesAdded(Collection<String> addresses)
     {
         updateContacts();
     }
 
     /**
      * Updates the contact list when an entry on the roster is updated
      * @param addresses The addresses which were updated
      * @see #updateContacts()
      */
     @Override
     public void entriesUpdated(Collection<String> addresses)
     {
         updateContacts();
     }
 
     /**
      * Updates the contact list when a entry is removed from the roster
      * @param addresses The addresses which were removed
      * @see #updateContacts()
      */
     @Override
     public void entriesDeleted(Collection<String> addresses)
     {
         updateContacts();
     }
 
     /**
      * Updates the contact list when a presence packet is received from a user
      * on the roster
      * @param presence The presence packet received
      * @see #updateContacts()
      */
     @Override
     public void presenceChanged(Presence presence)
     {
         updateContacts();
     }
 }
