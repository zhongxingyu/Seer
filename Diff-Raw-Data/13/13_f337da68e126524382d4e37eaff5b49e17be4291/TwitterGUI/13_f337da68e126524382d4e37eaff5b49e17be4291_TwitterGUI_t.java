 package view;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Event;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.awt.datatransfer.StringSelection;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.util.Scanner;
 
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextArea;
 import javax.swing.KeyStroke;
 import javax.swing.border.LineBorder;
 
 import model.Users;
 import controller.TwitterController;
 
 /**********************************************************************
  * Twitter GUI.
  * @author Nick, Vincenzo, Corey, Russ
  * @date 17 February, 2013
  *********************************************************************/
 public class TwitterGUI extends JFrame implements ActionListener, KeyListener {
 
     /** serialVersionUID. */
     private static final long serialVersionUID = 1L;
 
     /** Final frame height. */
     private static final int FRAME_HEIGHT = 450;
 
     /** Final frame width. */
     private static final int FRAME_WIDTH = 700;
 
     /** Final character limit: at most 140 characters per tweet. */
     private static final int CHAR_LIMIT = 140;
 
     /** Twitter Controller. */
     private TwitterController controller;
 
     /** Main frame. */
     private JFrame frame;
 
     /** Each panel of the GUI. */
     private JPanel profilePanel, tweetPanel, followingPanel, followersPanel;
 
     /** Menu Bar. */
     private JMenuBar menuBar;
 
     /** Categories of the menu bar. */
     private JMenu fileMenu, tweetMenu, aboutMenu;
 
     /** Items in the menu bar. */
     private JMenuItem exit, newTweet, delete, about;
 
     /** Tabbed pane. */
     private JTabbedPane tabbedPane;
 
     // Profile Panel *******************************************************
     /** Strings for profile information. */
     private String displayName, twitterName, description, location, website;
 
     /** Buttons for profile: Followers, Following, and number of tweets. */
     private JButton followersBtn, followingBtn, tweetsBtn;
 
     /** Profile Image. */
     private ImageIcon profileImage;
 
     /** Background and banner image. */
     private Image profileBanner, backgroundImage;
 
     // Tweet Panel *********************************************************
     /** GBC Layout. */
     private GridBagConstraints gbc;
 
     /** Chars remaining. */
     private int remaining = CHAR_LIMIT;
 
     /** JButtons for tweet panel. */
     private JButton cancel, tweetSubmit, tweetShow;
 
     /** Labels for characters remaining and total tweets. */
     private JLabel charsRemaining, tweetTotal;
 
     /** Text area to type outgoing tweet. */
     private JTextArea tweetText;
 
     // Followers Panel *****************************************************
     /** Final frame height. */
     private static final int FOLLOWERS_HEIGHT = 300;
 
     /** Final frame width. */
     private static final int FOLLOWERS_WIDTH = 450;
 
     // Following Panel *****************************************************
     /** Final frame height. */
     private static final int FOLLOWING_HEIGHT = 300;
 
     /** Final frame width. */
     private static final int FOLLOWING_WIDTH = 450;
 
     /** serialVersionUID. */
     private JList<String> jlistFollowing;
 
     /** serialVersionUID. */
     private JButton unfollow;
 
     /** serialVersionUID. */
     private Users following;
 
     /** serialVersionUID. */
     private GridBagConstraints finggbc;
 
     /** serialVersionUID. */
     private JLabel followingTotal;
 
     /****************************************************
      * Graphical User Interface.
      ***************************************************/
     public TwitterGUI() {
         frame = new JFrame();
         setTitle("Desktop Tweets");
         setBackground(Color.WHITE);
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         setSize(FRAME_WIDTH, FRAME_HEIGHT);
         setMaximumSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
         setMinimumSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
         setResizable(false);
         setLocationRelativeTo(null);
 
         setUpController();
 
         // Get User Information
         displayName = controller.getDisplayName();
         twitterName = controller.getTwitterName();
         description = controller.getDescription();
         location = controller.getLocation();
         website = controller.getWebsite();
         profileImage = controller.getProfileImage();
         profileBanner = controller.getProfileBanner();
         backgroundImage = controller.getBackgroundImage();
 
         // Create components
         createProfilePanel();
         createTweetPanel();
         createFollowingPanel();
         createFollowersPanel();
         createMenu();
         createTabbedPane();
 
         setVisible(true);
     }
 
     /****************************************************
      * Sets up controller.
      ***************************************************/
     private void setUpController() {
         controller = new TwitterController();
 
         if (!controller.getIsSetUp()) {
             String authUrl = controller.getAuthUrl();
             // copy to clipboard
             Toolkit.getDefaultToolkit().getSystemClipboard()
                     .setContents(new StringSelection(authUrl), null);
             String pin = JOptionPane.showInputDialog("Please "
                     + " follow this link to authenticate this"
                     + " App.\nEnter Pin:", authUrl);
             controller.setUp(pin);
         }
     }
 
     /****************************************************
      * Creates followers Panel and its components.
      ***************************************************/
     private void createFollowersPanel() {
         followersPanel = new JPanel() {
             protected void paintComponent(final Graphics g) {
                 g.drawImage(backgroundImage, 0, 0, null);
                 super.paintComponent(g);
             }
         };
         followersPanel.setOpaque(false);
         followersPanel.setLayout(new GridBagLayout());
 
         JPanel centerPnl = new JPanel();
         centerPnl.setBackground(Color.WHITE);
         centerPnl.setPreferredSize(new Dimension(FOLLOWERS_WIDTH,
                 FOLLOWERS_HEIGHT));
         JList<String> jlist = new JList<String>(new Users(
                 controller.getFollowers()));
         JScrollPane scrollpane = new JScrollPane(jlist);
         centerPnl.add(jlist);
         followersPanel.add(centerPnl);
     }
 
     /****************************************************
      * Creates following panel and its components.
      ***************************************************/
     private void createFollowingPanel() {
         followingPanel = new JPanel() {
             protected void paintComponent(final Graphics g) {
                 g.drawImage(backgroundImage, 0, 0, null);
                 super.paintComponent(g);
             }
         };
 
         followingPanel.setOpaque(false);
         followingPanel.setLayout(new GridBagLayout());
         finggbc = new GridBagConstraints();
 
         JPanel centerPnl = new JPanel();
         centerPnl.setBackground(Color.WHITE);
         centerPnl.setPreferredSize(new Dimension(FOLLOWING_WIDTH,
                 FOLLOWING_HEIGHT));
 
         following = new Users(controller.getFollowing());
 
         jlistFollowing = new JList<String>(following);
 
         finggbc.gridx = 0;
         finggbc.gridy = 0;
         final int w = 3;
         finggbc.gridwidth = w;
         finggbc.fill = GridBagConstraints.HORIZONTAL;
 
         centerPnl.add(jlistFollowing);
         followingPanel.add(centerPnl, finggbc);
 
         unfollow = new JButton("Unfollow");
         unfollow.addActionListener(this);
         finggbc.gridx = 0;
         finggbc.gridy = 1;
         finggbc.gridwidth = 2;
         finggbc.fill = 1;
         followingPanel.add(unfollow, finggbc);
 
         followingTotal = new JLabel(" Following "
                 + controller.getFriendsCount());
         finggbc.gridx = 2;
         finggbc.gridy = 1;
         finggbc.gridwidth = 1;
         finggbc.fill = 1;
         followingPanel.add(followingTotal, finggbc);
     }
 
     /****************************************************
      * Creates tweet panel and its components.
      ***************************************************/
     private void createTweetPanel() {
         tweetPanel = new JPanel() {
             protected void paintComponent(final Graphics g) {
                 g.drawImage(backgroundImage, 0, 0, null);
                 super.paintComponent(g);
             }
         };
 
         tweetPanel.setOpaque(false);
         tweetPanel.setLayout(new GridBagLayout());
         gbc = new GridBagConstraints();
 
         // Instantiate vars
         cancel = new JButton("Cancel");
         cancel.setFocusable(false);
         cancel.addActionListener(this);
         gbc.fill = GridBagConstraints.HORIZONTAL;
         final int ipady = 10;
         gbc.ipady = ipady;
         gbc.gridx = 0;
         gbc.gridy = 0;
         tweetPanel.add(cancel, gbc);
 
         tweetSubmit = new JButton("Send Tweet");
         tweetSubmit.setFocusable(false);
         tweetSubmit.addActionListener(this);
         gbc.fill = GridBagConstraints.HORIZONTAL;
         gbc.gridx = 1;
 
         tweetPanel.add(tweetSubmit, gbc);
 
         tweetText = new JTextArea();
         tweetText.addKeyListener(this);
         tweetText.setBorder(BorderFactory.createLineBorder(Color.BLACK));
         tweetText.setFocusable(true);
         final int col = 30;
         tweetText.setColumns(col);
         final int row = 8;
         tweetText.setRows(row);
         tweetText.setLineWrap(true);
         gbc.gridx = 0;
         gbc.gridy = 1;
         final int w = 3;
         gbc.gridwidth = w;
         tweetPanel.add(tweetText, gbc);
 
         charsRemaining = new JLabel("140", JLabel.RIGHT);
         gbc.gridx = 2;
         gbc.gridy = 2;
         tweetPanel.add(charsRemaining, gbc);
 
         tweetTotal = new JLabel(controller.getTweetCount() + " Tweets");
         gbc.gridx = 0;
         final int gridy = 3;
         gbc.gridy = gridy;
         gbc.gridwidth = 1;
         tweetPanel.add(tweetTotal, gbc);
 
         tweetShow = new JButton("Show Tweets");
         tweetShow.setFocusable(false);
         tweetShow.addActionListener(this);
         gbc.gridx = 1;
         gbc.gridy = gridy;
         gbc.gridwidth = 1;
         gbc.anchor = GridBagConstraints.PAGE_END;
         tweetPanel.add(tweetShow, gbc);
     }
 
     /****************************************************
      * Creates profile panel and its components.
      ***************************************************/
     private void createProfilePanel() {
         /** INFO PANEL */
         JPanel infoPanel = new JPanel() {
             protected void paintComponent(final Graphics g) {
                 final int eight = 8;
                 final int three = 3;
                 final int ten = 10;
                 g.drawImage(profileBanner,
                         (FRAME_WIDTH / 2) - profileBanner.getWidth(null) / 2
                                 - eight, FRAME_HEIGHT / three + ten
                                 - profileBanner.getHeight(null) / 2, null);
             }
         };
         infoPanel.setOpaque(false);
         infoPanel.setLayout(new GridBagLayout());
         GridBagConstraints c = new GridBagConstraints();
         infoPanel.paintComponents(null);
         c.fill = GridBagConstraints.NONE;
 
         // Profile Image
         ImageIcon img = profileImage;
         c.gridy = 0;
         JButton profImgBtn = getPlainButton(null, null);
         profImgBtn.setPreferredSize(new Dimension(img.getIconWidth() + 2, img
                 .getIconHeight() + 2));
         final int four = 4;
         profImgBtn.setBorder(new LineBorder(Color.WHITE, four));
         profImgBtn.setIcon(img);
         infoPanel.add(profImgBtn, c);
 
         // Display Name
         final int five = 5;
         c.ipady = five;
         c.gridy = 1;
         JLabel displayNameLbl = new JLabel(displayName);
         displayNameLbl.setFont(new Font("arial", Font.BOLD, five * five - 1));
         displayNameLbl.setForeground(Color.WHITE);
         infoPanel.add(displayNameLbl, c);
 
         // Twitter Name
         c.gridy = 2;
         JLabel twitterNameLbl = new JLabel(twitterName);
         twitterNameLbl
                 .setFont(new Font("arial", Font.PLAIN, five + five + five));
         twitterNameLbl.setForeground(Color.WHITE);
         infoPanel.add(twitterNameLbl, c);
         final int thirty = 30;
         c.ipady = thirty; // more padding
 
         // Description
         c.gridy = 1 + 1 + 1;
         c.ipady = 0;
         createDescriptionPanel(infoPanel, c);
 
         // Location
         c.ipady = five;
         c.gridy = five + 1;
         JLabel locationLbl = new JLabel(location);
         final int fifteen = 15;
         locationLbl.setFont(new Font("arial", Font.BOLD, fifteen));
         locationLbl.setForeground(Color.WHITE);
         infoPanel.add(locationLbl, c);
 
         // Website
         final int seven = 7;
         c.gridy = seven;
         JLabel websiteLbl = new JLabel(website);
         websiteLbl.setFont(new Font("arial", Font.PLAIN, seven + seven));
         websiteLbl.setForeground(Color.WHITE);
         infoPanel.add(websiteLbl, c);
 
         /** COUNT PANEL */
         JPanel countPanel = new JPanel();
         countPanel.setLayout(new GridBagLayout());
         GridBagConstraints cpc = new GridBagConstraints();
         cpc.fill = GridBagConstraints.NONE;
         final int thrity = 30;
         cpc.ipadx = thirty;
         countPanel.setOpaque(false);
         // countPanel.setBackground(Color.WHITE);
 
         followersBtn = getPlainButton("" + controller.getFollowersCount(),
                 "Followers");
         followersBtn.addActionListener(this);
         followingBtn = getPlainButton("" + controller.getFriendsCount(),
                 "Following");
         followingBtn.addActionListener(this);
         tweetsBtn = getPlainButton("" + controller.getTweetCount(), "Tweets");
         tweetsBtn.addActionListener(this);
 
         cpc.gridy = 0;
         JButton blankBtn1 = getPlainButton(null, null);
         blankBtn1.setBorderPainted(false);
         JButton blankBtn2 = getPlainButton(null, null);
         blankBtn2.setBorderPainted(false);
         countPanel.add(followersBtn, cpc);
         countPanel.add(new JLabel(" "), cpc);
         countPanel.add(followingBtn, cpc);
         countPanel.add(new JLabel(" "), cpc);
         countPanel.add(tweetsBtn, cpc);
         cpc.gridy = 1;
         countPanel.add(new JLabel(" "), cpc);
 
         /** PROFILE PANEL */
         profilePanel = new JPanel() {
             protected void paintComponent(final Graphics g) {
                 g.drawImage(backgroundImage, 0, 0, null);
                 super.paintComponent(g);
             }
         };
         profilePanel.setOpaque(false);
         profilePanel.setLayout(new BorderLayout());
         profilePanel.add(infoPanel, BorderLayout.CENTER);
         profilePanel.add(countPanel, BorderLayout.SOUTH);
     }
 
     /****************************************************
      * Gets plain button for the profile tab page.
      * 
      * @param String line1
      * @param String line2
      * @return JButton
      ***************************************************/
     private JButton getPlainButton(final String line1, final String line2) {
         String text = line1;
         if (line2 != null) {
             text = ("<html><center><font size=6>" + line1 + "</font><br><i>"
                     + line2 + "</i></center></html>");
         }
         JButton tmp = new JButton();
         final int w = 120;
         final int l = 50;
         final int fontsize = 15;
         tmp.setPreferredSize(new Dimension(w, l));
         tmp.setFont(new Font("Arial", Font.BOLD, fontsize));
         tmp.setText(text);
         tmp.setBackground(Color.WHITE);
         tmp.setForeground(Color.DARK_GRAY);
         tmp.setBorder(new LineBorder(Color.GRAY, 1, true));
         tmp.setFocusable(false);
         tmp.setFocusPainted(false);
         return tmp;
     }
 
     /****************************************************
      * Sets plain button for the profile tab page.
      * 
      * @param String line1
      * @param String line2
      * @return JButton
      ***************************************************/
     private void setPlainButton(final String line1, final String line2,
             final JButton j) {
         String text = line1;
         if (line2 != null) {
             text = ("<html><center><font size=6>" + line1 + "</font><br><i>"
                     + line2 + "</i></center></html>");
         }
         final int w = 120;
         final int l = 50;
         final int fontsize = 15;
         j.setPreferredSize(new Dimension(w, l));
         j.setFont(new Font("Arial", Font.BOLD, fontsize));
         j.setText(text);
         j.setBackground(Color.WHITE);
         j.setForeground(Color.DARK_GRAY);
         j.setBorder(new LineBorder(Color.GRAY, 1, true));
         j.setFocusable(false);
         j.setFocusPainted(false);
     }
 
     /****************************************************
      * Creates description panel for use within profile
      * panel.
      * 
      * @param JPanel panel
      * @param GridBagConstraints c
      ***************************************************/
     private void createDescriptionPanel(final JPanel panel,
             final GridBagConstraints c) {
         final int max = 55;
         final int fontsize = 14;
         
         //single line
         if (description.length() < max) {   
             JLabel descriptionLbl = new JLabel(description);
             descriptionLbl.setFont(new Font("arial", Font.PLAIN, fontsize));
             descriptionLbl.setForeground(Color.WHITE);
             panel.add(descriptionLbl, c);
             
         // double line    
         } else if (description.length() < max + max) {
             String d1, d2;
 
             String[] w = description.split(" ");
             int wMid = (w.length / 2) + 1;
             System.out.println("" + wMid);
             int dMid = description.length() / 2;
             System.out.println("" + dMid);
             d1 = description.substring(0,
                     description.indexOf(w[wMid], dMid - 1));
             d2 = description.substring(description.indexOf(w[wMid], dMid - 1));
 
             JLabel description1Lbl = new JLabel(d1);
             JLabel description2Lbl = new JLabel(d2);
             description1Lbl.setFont(new Font("arial", Font.PLAIN, fontsize));
             description1Lbl.setForeground(Color.WHITE);
             description2Lbl.setFont(new Font("arial", Font.PLAIN, fontsize));
             description2Lbl.setForeground(Color.WHITE);
             panel.add(description1Lbl, c);
             c.gridy = 2 + 2;
             panel.add(description2Lbl, c);
             
         // triple line    
         } else {
             String d1, d2, d3;
             String[] w = description.split(" ");
             int wThird = (w.length / (1 + 2)) + 1;
             int dThird = description.length() / (1 + 2);
 
             d1 = description.substring(0,
                     description.indexOf(w[wThird], dThird));
             d2 = description.substring(description.indexOf(w[wThird], dThird),
                     description.indexOf(w[wThird * 2], dThird * 2));
             d3 = description.substring(description.indexOf(w[wThird * 2],
                     dThird * 2));
 
             JLabel description1Lbl = new JLabel(d1);
             description1Lbl.setFont(new Font("arial", Font.PLAIN, fontsize));
             description1Lbl.setForeground(Color.WHITE);
             JLabel description2Lbl = new JLabel(d2);
             description2Lbl.setFont(new Font("arial", Font.PLAIN, fontsize));
             description2Lbl.setForeground(Color.WHITE);
             JLabel description3Lbl = new JLabel(d3);
             description3Lbl.setFont(new Font("arial", Font.PLAIN, fontsize));
             description3Lbl.setForeground(Color.WHITE);
 
             panel.add(description1Lbl, c);
             c.gridy = 2 + 2;
             panel.add(description2Lbl, c);
             c.gridy = 2 + 2 + 1;
             panel.add(description3Lbl, c);
         }
     }
 
     /****************************************************
      * Creates menu bar.
      ***************************************************/
     private void createMenu() {
         menuBar = new JMenuBar();
 
         // File Menu
         fileMenu = new JMenu("File");
         exit = new JMenuItem("Exit");
         exit.addActionListener(this);
         exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,
                 Event.CTRL_MASK));
         fileMenu.add(exit);
 
         // Tweet Menu
         tweetMenu = new JMenu("Tweet");
         newTweet = new JMenuItem("New Tweet");
         newTweet.addActionListener(this);
         newTweet.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
                 Event.CTRL_MASK));
         delete = new JMenuItem("Delete");
         delete.addActionListener(this);
         delete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D,
                 Event.CTRL_MASK));
         tweetMenu.add(newTweet);
         tweetMenu.add(delete);
 
         // About Menu
         aboutMenu = new JMenu("About");
         about = new JMenuItem("About Desktop Tweets");
         about.addActionListener(this);
         about.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
                 Event.CTRL_MASK));
         aboutMenu.add(about);
 
         // Add To MenuBar
         menuBar.add(fileMenu);
         menuBar.add(tweetMenu);
         menuBar.add(aboutMenu);
         setJMenuBar(menuBar);
     }
 
     /****************************************************
      * Creates tabbed pane.
      ***************************************************/
     private void createTabbedPane() {
         // UIManager.put("TabbedPane.selected", Color.WHITE);
         // UIManager.put("TabbedPane.contentAreaColor", Color.WHITE);
         tabbedPane = new JTabbedPane(JTabbedPane.TOP);
         tabbedPane.setFocusable(false);
         tabbedPane.setBackground(Color.WHITE);
         tabbedPane.setForeground(Color.BLACK);
         final int fontsize = 14;
         tabbedPane.setFont(new Font("arial", Font.BOLD, fontsize));
         tabbedPane.setTabLayoutPolicy(JTabbedPane.CENTER);
         tabbedPane.addTab("Profile", profilePanel);
         tabbedPane.addTab("Tweet", tweetPanel);
         tabbedPane.addTab("Followers", followersPanel);
         tabbedPane.addTab("Following", followingPanel);
         add(tabbedPane);
     }
 
     /***************************************************
      * Where all actions are delegated
      * @param ActionEvent e
      **************************************************/
     @Override
     public final void actionPerformed(final ActionEvent e) {
         Object source = e.getSource();
 
         if (source == exit) {
             System.exit(0);
         }
         if (source == newTweet || source == tweetsBtn) {
             tabbedPane.setSelectedComponent(tweetPanel);
         }
         if (source == delete || source == tweetsBtn) {
             DialogTweets x = new DialogTweets(this,
                     controller.getUserTimeline());
             for (long l : x.getRemoveList()) {
                 controller.destroyStatus(l);
             }
             updateTweetCount();
         }
 
         if (source == tweetSubmit) {
 
             if (tweetText.getText().length() > CHAR_LIMIT) {
                 JOptionPane.showMessageDialog(null, "Tweet is too long.",
                         "Oops", JOptionPane.PLAIN_MESSAGE);
             } else {
                 if (controller.tweet(tweetText.getText())) {
                     updateTweetCount();
                     JOptionPane.showMessageDialog(null, "Tweet Sent.",
                             "Confirmation", JOptionPane.PLAIN_MESSAGE);
                    e.setSource(cancel);
                    actionPerformed(e); 
                 } else {
                     JOptionPane.showMessageDialog(null, "Tweet could not "
                             + "be sent.", "Oops", JOptionPane.PLAIN_MESSAGE);
                 }
             }
 
         }
 
         if (source == about) {
             JOptionPane.showMessageDialog(null, "Desktop twitter application");
         }
         if (source == followersBtn) {
             tabbedPane.setSelectedComponent(followersPanel);
         }
         if (source == followingBtn) {
             tabbedPane.setSelectedComponent(followingPanel);
         }
         if (source == cancel) {
             tweetText.setText("");
            remaining = 140;
            charsRemaining.setText("" + remaining);
         }
         if (source == tweetShow) {
             DialogTweets x = new DialogTweets(this,
                     controller.getUserTimeline());
             for (long l : x.getRemoveList()) {
                 controller.destroyStatus(l);
             }
             updateTweetCount();
 
         }
 
         if (source == unfollow) {
             long l = following.remove(jlistFollowing.getSelectedIndex());
             controller.unfollow(l);
 
             updateFollowingCount();
 
         }
     }
 
     /****************************************************
      * Updates following count.
      ***************************************************/
     private void updateFollowingCount() {
         setPlainButton("" + controller.getFriendsCount(), "Following",
                 followingBtn);
         followingTotal.setText(" Following " + controller.getFriendsCount());
     }
 
     /****************************************************
      * Updates tweet count.
      ***************************************************/
     private void updateTweetCount() {
         setPlainButton("" + controller.getTweetCount(), "Tweets", tweetsBtn);
         tweetTotal.setText(controller.getTweetCount() + " Tweets");
     }
 
     /****************************************************
      * Main Method
      * @param String[] args
      ***************************************************/
     public static void main(final String[] args) {
         new TwitterGUI();
     }
 
     @Override
     public void keyTyped(final KeyEvent e) {
        if (e.getKeyChar() == KeyEvent.VK_BACK_SPACE) {
            if (remaining < 140)
                remaining++;
         } else {
             remaining--;
         }
         charsRemaining.setText("" + remaining);
     }
 
     @Override
     public void keyPressed(final KeyEvent e) {
     }
 
     @Override
     public void keyReleased(final KeyEvent e) {
     }
 }
