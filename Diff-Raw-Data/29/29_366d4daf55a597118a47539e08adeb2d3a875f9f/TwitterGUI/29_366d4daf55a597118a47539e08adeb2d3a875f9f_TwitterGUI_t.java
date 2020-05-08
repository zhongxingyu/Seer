 
 package view;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
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
 import javax.swing.JTextField;
 import javax.swing.BoxLayout;
 
 
 import twitter4j.Status;
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 import twitter4j.TwitterFactory;
 
 /**
  * @author Chris Cirefice, Robert Smith, Eric Venlet, & Cristine Waite
  *TwitterGUI builds a simple GUI for use with a program that accesses
  *Twitter
  */
 public class TwitterGUI extends JFrame  {
 	/**
      * Constructor for Twitter GUI.
      */
     public TwitterGUI() {
         initComponents();
         setVisible(true);
     }   
 
     
     /**
      * frame is a JFrame to be used with the reply panel.
      */
     private JFrame frame;
     /**
      * font size.
      */
     private final int fSize = 17;
     /**
      * Size for struts.
      */
     private final int strut = 10;
     /**
      * Speed for scrolling.
      */
     private final int scroll = 20;
     /**
      * quit menu item for the menu bar.
      */
     private JMenuItem quit;
     /**
      * Text area for posting function.
      */
     private JTextArea textPost, textReply;
     /**
      * JButton for post feature.
      */
     private JButton post;
     /**
      * JButton for Save Draft feature.
      */
     private JButton saveDraft;
     /**
      * JButton for Load Draft feature.
      */
     private JButton loadDraft;
     /**
      * JButton for clearing drafts.
      */
     private JButton clearDraft;
     /**
      * TextField to receive the search terms.
      */
     private JTextField terms;
     /**
      * Status object to help build search, favorites, and post panels.
      */
     private Status status;
     /**
      * ActionListener for the delete button.
      */
     private ActionListener dListen, rListen, faListen, foListen, repListen,
         repListen2;
 
     /**
      * JLabel to display available number of characters left for a post.
      */
     private JLabel textCounter; // R2 timeCounter;
     /**
      * Buttons to search and to favorite.
      */
     private JButton search, favoriteButton, homeButton, 
         replyButton; // R2 refreshHome;
     /**
      * Button for the delete feature.
      */
     private JButton delete = new JButton();
 
     /**
      * JCheckbox for following function.
      */
     private JCheckBox follow, favorite, retweet;
     /**
      * ScrollPanes for searchResults, favorites and homeTimeline.
      */
     private JScrollPane favorites, searchResults, homeTimeline;
 
     /**
      * JPanels for favorite, delete, tweet, search, post, homepanel,
      * and tPane functions.
      */
   // R2 aboutPanel;
     private JPanel favoritesPanel, toDelete, tweets,
     searchPanel, postPanel, homePanel, tPane;
 
     /**
      * ArrayList to handle individual statuses displayed in the tPanes.
      */
     private ArrayList<JPanel> tPanes = new ArrayList<JPanel>();
     /**
      * Map used to help identify which delete button was pushed and
      * which post is associated with it.
      */
     private Map<JButton, Long> deleteMap = new HashMap<JButton, Long>();
     // R2
     /**
      * Map to associate favorites JCheckBox with specific tweet.
      */
     private Map<JCheckBox, Long> favoriteMap = new HashMap<JCheckBox, Long>();
     /**
      * Map to associate follow JCheckBox with specific tweet.
      */
     private Map<JCheckBox, Long> followMap = new HashMap<JCheckBox, Long>();
     /**
      * Map to associate retweet JCheckBox with specific tweet.
      */
     private Map<JCheckBox, Long> retweetMap = new HashMap<JCheckBox, Long>();
     /**
      * Map to associate reply JButton with specific tweet.
      */
     private Map<Long, String> replyMap = new HashMap<Long, String>();
     
     /**
      * Long for the id of a tweet.
      */
     private long id;
     
     /**
      * String for a username.
      */
     private String username;
     /**
      * Sizing integer for JPanels.
      */
     private final int x = 450;
     /**
      * Dimension for JPanels within the TabbedPane.
      */
     private Dimension tabPanels = new Dimension(x, x);
     /**
      * Options for which search feature to use.
      */
     private JComboBox searchType;
     /**
      * JPanel to verify tweet was posted.
      */
     private JPanel postConfirm = new JPanel();
     // R2
     // private JButton about;
     /**
      * Initializes TwitterGUI.
      */
     private void initComponents()   {
         setTitle("NitTwit");
         //DraftList d = new DraftList();
         final int wdth = 475;
         final int hght = 600;
         Dimension panelSize = new Dimension(wdth, hght);
         setMinimumSize(panelSize);
         setPreferredSize(panelSize);
         setMaximumSize(panelSize);
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         JMenuBar menu = new JMenuBar();
         JMenu file = new JMenu("File");
         /*// R? JMenu sort = new JMenu("Sort");
         JMenuItem created = new JRadioButtonMenuItem("By Creation Date", false);
         // created.addActionListener(menuListener);
         JMenuItem login = new JRadioButtonMenuItem("By Login Name", false);
         // login.addActionListener(menuListener);
         JMenuItem display = new JRadioButtonMenuItem("By Display Name", false);
         // display.addActionListener(menuListener);
         JMenuItem friend = new JRadioButtonMenuItem("By Friends Count", false);
         // friend.addActionListener(menuListener);
         JMenuItem follow = new JRadioButtonMenuItem
         ("By Followers Count", false);
         // follow.addActionListener(menuListener);
         sort.add(created);
         sort.add(login);
         sort.add(display);
         sort.add(friend);
         sort.add(follow);*/
         quit = new JMenuItem("Quit");
         quit.addActionListener(menuListener);
         file.add(quit);
         menu.add(file);
         // R? menu.add(sort);
         setJMenuBar(menu);
         String str = "background.jpg";
         JPanel main = new ImagePanel(str);
         main.add(westPanel());
         add(main);
         // add(eastPanel(), BorderLayout.CENTER);
         pack();
         
     }
     
     /**
      * Builds the panel for replying to a tweet.
      * @param s Status of the Tweet to be replied to.
      */
     public final void replyToTweet(final Status s)  {
         id = s.getId();
         username = s.getUser().getScreenName();
         final int wdth = 475;
         final int hght = 450;
         Dimension d = new Dimension(wdth, hght);
         frame = new JFrame("Reply to Tweet");
         frame.setMinimumSize(d);
         frame.setPreferredSize(d);
         frame.setMaximumSize(d);
         repListen2 = new ActionListener() {
             @Override
             public void actionPerformed(final ActionEvent arg0)   {
                 frame.dispose();                
             }            
         };
         JPanel replyPanel = new JPanel();
         replyPanel.setPreferredSize(d);
         replyPanel.setMaximumSize(d);
         textReply = new JTextArea();
         textReply.setLineWrap(true);
         Font font = new Font("Verdana", Font.PLAIN, fSize);
         textReply.setFont(font);
         final int tw = 450;
         final int th = 300;
         d = new Dimension(tw, th);
         textReply.setPreferredSize(d);
         textReply.setMaximumSize(d);
         textReply.setBorder(BorderFactory.createEtchedBorder());
         textReply.setText("@" + username + " :");
         replyButton = new JButton("Send");
         replyButton.addActionListener(repListen2);
         replyButton.addActionListener(repListen);        
         replyPanel.add(textReply);
         replyPanel.add(replyButton);
         frame.add(replyPanel);
         frame.setVisible(true);         
     }
     
     /**
      * Getter for the text in the reply textarea.
      * @return String for the text in the reply textarea.
      */
     public final String getReplyText() {
         return textReply.getText();
     }
     
     /**
      * Getter for the ID of a tweet.
      * @return long id for the ID of the tweet.
      */
     public final long getReplyID() {
         return id;
     }
     /**
      * westPanel() creates a JTabbedPane to add tabs into.
      * @return JTabbedPane for the Home tab, Search tab, Post tab, Message tab,
      * Favorites tab, and About tab.
      */
     private JTabbedPane westPanel() {
         JTabbedPane westPanel = new JTabbedPane();
         final int wdth = 455;
         final int hght = 500;
         Dimension tabPanel = new Dimension(wdth, hght);
         westPanel.setBorder(BorderFactory.createRaisedBevelBorder());
         westPanel.setPreferredSize(tabPanel);
         westPanel.addTab("Home", homePanel());
         westPanel.addTab("Search", searchPanel());
         westPanel.addTab("Post", postPanel());
         //westPanel.addTab("Message", messagePanel());
         westPanel.addTab("Favorites", favoritesPanel());
         // R2 westPanel.addTab("About", aboutPanel());
         //westPanel.addTab("Test", new ImagePanel());
         return westPanel;
     }
     /**
      * Creates a JPanel to show favorited Tweets.
      * @return JPanel favoritesPanel to be displayed by the GUI on the Favorites
      * tab.
      */
     private JPanel favoritesPanel() {
         favoritesPanel = new ImagePanel();
         favoriteButton = new JButton("Get Favorites");
         favoriteButton.setAlignmentX(CENTER_ALIGNMENT);
         favoritesPanel
         .setLayout(new BoxLayout(favoritesPanel, BoxLayout.Y_AXIS));
         favoritesPanel.add(favoriteButton);
         return favoritesPanel;
     }
     /**
      * Displays a list of favorited Tweets determined by the parameter.
      * @param s List<Status> to be converted into a JPanel using the searchPane
      * method.
      */
     public final void displayFavorites(final List<Status> s)    {
         
         toDelete = searchPane(s);
         
         favorites = new JScrollPane(toDelete);
         favoritesPanel.remove(favorites);
         favorites.setMaximumSize(tabPanels);
         favorites.setMinimumSize(tabPanels);
         favorites
         .setVerticalScrollBarPolicy(
                 JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
         favorites.getVerticalScrollBar().setUnitIncrement(scroll);
         favoritesPanel.add(favorites);
         this.pack();
         if (s.size() == 0) {
             JOptionPane.showMessageDialog(this, "No results.");
         }
     }
     /**
      * Removes the favorites panel.
      */
     public final void removeFavorites() {
         favoritesPanel.remove(favorites);
     }
     /**
      * Sets up the Home Panel which is the first panel viewed when NitTwit is
      * initialized.
      * @return JPanel homePanel which is displayed by the GUI in the Home tab.
      */
     private JPanel homePanel()  {
         homePanel = new ImagePanel();
         homeButton = new JButton("Get Home Timeline");
         homeButton.setAlignmentX(CENTER_ALIGNMENT);
         homePanel.add(homeButton);
         homePanel.setLayout(new BoxLayout(homePanel, BoxLayout.Y_AXIS));
         final int w = 450;
         final int h = 470;
         Dimension d = new Dimension(w, h);
         homePanel.setMinimumSize(d);
         homePanel.setPreferredSize(d);
         homePanel.setMaximumSize(d);
         homeTimeline = new JScrollPane();
         homePanel.add(homeTimeline);
         return homePanel;
     }
     
     /**
      * Removes the hometimeline panel.
      */
     public final void removeHomeTimeline() {
         homePanel.remove(homeTimeline);
     }
     /**
      * Displays the Home Timeline based a list of Statuses recently published to
      * Twitter by the current user's list of Followers.
      * @param s List<Status> to be displayed by the GUI in the Home
      * Timeline tab.
      */
     public final void displayTimeline(final List<Status> s) {
         tweets = searchPane(s);
         homePanel.remove(homeTimeline);
         homeTimeline = new JScrollPane(tweets);
         homeTimeline.setMinimumSize(tabPanels);
         homeTimeline.setMaximumSize(tabPanels);
         homeTimeline
         .setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
         homeTimeline.getVerticalScrollBar().setUnitIncrement(scroll);
         homePanel.add(homeTimeline);
         homeTimeline.getVerticalScrollBar().setValue(
                 homeTimeline.getVerticalScrollBar().getMinimum());
         this.pack();
         if (s.size() == 0) {
             JOptionPane.showMessageDialog(this, "No results.");
         }
     }
     /**
      * Builds the JPanel for NitTwit's search function.
      * @return JPanel searchPanel object that can be arranged by the GUI to
      * be displayed to the user.
      */
     private JPanel searchPanel()    {
         searchPanel = new ImagePanel();
         searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.Y_AXIS));
         JPanel searchBar = new JPanel();
         terms = new JTextField();
         final int wdth = 300;
         final int hght = 25;
         Dimension termField = new Dimension(wdth, hght);
         terms.setMinimumSize(termField);
         terms.setMaximumSize(termField);
         terms.setPreferredSize(termField);
         terms.setBorder(BorderFactory.createEtchedBorder());
         search = new JButton("Search");
         searchBar.add(terms);
         searchBar.add(search);
         searchBar.setOpaque(false);
         final int sWdth = 450;
         final int sHght = 30;
         Dimension sBarSize = new Dimension(sWdth, sHght);
         searchBar.setMaximumSize(sBarSize);
         searchPanel.add(Box.createVerticalStrut(strut));
         searchPanel.add(searchBar);
         searchPanel.add(Box.createVerticalStrut(strut));
         JPanel options = new JPanel();
         options.setOpaque(false);
         String[] opt = {"Keyword", "Username", "Hashtag"};
         searchType = new JComboBox(opt);
         JLabel optionlabel = new JLabel("Search Type: ");
         options.add(optionlabel);
         options.add(searchType);
         searchPanel.add(options);
         searchPanel.add(Box.createVerticalStrut(strut));
         searchResults = new JScrollPane();
         searchResults.setOpaque(false);
         searchResults.getViewport().setOpaque(false);
         searchResults.setBorder(null);
         searchPanel.add(searchResults);
         return searchPanel;
     }
     /**
      * Fetches the search type from the JComboBox.
      * @return int an integer which represents the type of search.
      */
     public final int getSearchType()  {
         return searchType.getSelectedIndex();
     }
     /**
      * Displays the search results in a series of JPanels using the searchPane
      * method.
      * @param s is the List<Status> to be displayed through the GUI.
      * @param searchterms is the search term entered that initiates the search.
      */
     public final void displaySearchResults(
             final List<Status> s, final String searchterms)  {
         tweets = searchPane(s);
         searchPanel.remove(searchResults);
         searchResults = new JScrollPane(tweets);
         searchResults.getVerticalScrollBar().setUnitIncrement(scroll);
         searchPanel.add(searchResults);
         this.pack();
         //if (s.size() == 0) {
          //   JOptionPane.showMessageDialog(this, "No results.");
         //}
     }
     /**
      * Retrieve from JTextField the word(s) to be searched for.
      * @return the String that is searched for.
      */
     public final String getSearchTerms()  {
         return terms.getText();
     }
     /**
      * Builds the JPanel for NitTwit's posting function.
      * @return a postPanel object that can be arranged by the GUI to
      * be displayed to the user.
      */
     private JPanel postPanel()  {
         post = new JButton("Post");
         saveDraft = new JButton("Save Draft");
         loadDraft = new JButton("Load Draft");
         clearDraft = new JButton("Clear Drafts");
         saveDraft.setAlignmentX(CENTER_ALIGNMENT);
         post.setAlignmentX(CENTER_ALIGNMENT);
         loadDraft.setAlignmentX(CENTER_ALIGNMENT);
         clearDraft.setAlignmentX(CENTER_ALIGNMENT);
         Font font = new Font("Verdana", Font.PLAIN, fSize);
         textPost = new JTextArea();
         textPost.setFont(font);
         textPost.setLineWrap(true);
         JScrollPane postArea = new JScrollPane();
         postArea.setViewportView(textPost);
         postArea.setVerticalScrollBarPolicy(
                 JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
         postArea.setHorizontalScrollBarPolicy(
                 JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
         textPost.setOpaque(false);
         textCounter = new JLabel();
         textPost.addKeyListener(new KeyListener() {
 
             @Override
             public void keyPressed(final KeyEvent arg0)   {
                final int maxText = 140;
                int count = maxText - textPost.getText().length();
                textCounter.setText("" + count);
             }
 
             @Override
             public void keyReleased(final KeyEvent arg0)  {
                 final int maxText = 140;
                 int count = maxText - textPost.getText().length();
                 textCounter.setText("" + count);
             }
 
             @Override
             public void keyTyped(final KeyEvent arg0)   {
                 final int maxText = 140;
                 int count = maxText - textPost.getText().length();
                 textCounter.setText("" + count);
             }
         });
         final int wdth = 400;
         final int hght = 200;
         Dimension postSize = new Dimension(wdth, hght);
         postArea.setMaximumSize(postSize);
         postArea.setPreferredSize(postSize);
         postArea.setMinimumSize(postSize);
         postArea.setAlignmentX(CENTER_ALIGNMENT);
         postArea.setOpaque(false);
         postArea.getViewport().setOpaque(false);
         postArea.setBorder(BorderFactory.createEtchedBorder());
         textCounter.setAlignmentX(CENTER_ALIGNMENT);
         postPanel = new ImagePanel();
         postPanel.setLayout(new BoxLayout(postPanel, BoxLayout.Y_AXIS));
         postPanel.add(postArea);
         postPanel.add(Box.createVerticalStrut(strut));
         postPanel.add(textCounter);
         JPanel buttonP = new JPanel();
         buttonP.setOpaque(false);
         buttonP.setLayout(new BoxLayout(buttonP, BoxLayout.X_AXIS));
         buttonP.add(loadDraft);
         buttonP.add(saveDraft);
         buttonP.add(clearDraft);
         postPanel.add(buttonP);
         postPanel.add(post);
         return postPanel;
     }
     // R2 - Duplicate of Post Panel. Needs editing to display "About NitTwit".
     /*private JPanel aboutPanel()
     {
         about = new JButton("Post");
         about.setAlignmentX(CENTER_ALIGNMENT);
         Font font = new Font("Verdana", Font.PLAIN, 16);
         aboutPost.setFont(font);
         aboutPost.setLineWrap(true);
         JScrollPane aboutArea = new JScrollPane();
         aboutArea.setViewportView(aboutPost);
         aboutArea
         .setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
         aboutArea
         .setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
         aboutArea.setMaximumSize(new Dimension(450, 250));
         aboutArea.setPreferredSize(new Dimension(450, 250));
         aboutArea.setMinimumSize(new Dimension(450, 250));
         aboutArea.setAlignmentX(CENTER_ALIGNMENT);
         timeCounter = new JLabel();
         timeCounter.setAlignmentX(CENTER_ALIGNMENT);
         aboutPanel = new JPanel();
         aboutPanel.setLayout(new BoxLayout(aboutPanel, BoxLayout.Y_AXIS));
         aboutPanel.add(aboutArea);
         aboutPanel.add(Box.createVerticalStrut(10));
         aboutPanel.add(about);
         aboutPanel.add(timeCounter);
         return aboutPanel;
     }*/
     /**
      * Displays the text that was posted based off of the Status of the post
      * using searchPane(s).
      * @param s is a Status object that will be displayed by the
      * GUI to the user.
      */
     public final void showPost(final Status s)  {
         postPanel.remove(postConfirm);
         postConfirm = searchPane(s);
         postConfirm.setOpaque(false);
         // p.setBackground(Color.LIGHT_GRAY);
         postPanel.add(postConfirm);
         pack();
     }
     /**
      * Clears the text in the textPost area.
      */
     public final void clearPost()   {
         textPost.setText("");
     }
 
     /**
      * Sets the text in the post area to a specified String.
      * @param s String representing text to be set to postBox.
      */
     public final void setText(final String s)  {
         textPost.setText(s);
     }
     /**
      * Finds the length of the user's entered "to-post" text in the textPost
      * area.
      * @return returns the length of the string in the textPost area as
      * an int.
      */
     public final int getPostTextLength()  {
         return textPost.getText().length();
     }
     /**
      * Updates the text counter for the posting text area.
      * @param s updates the "remaining characters" text field based on
      * s.length.
      */
     public final void setTextCounter(final String s)  {
         textCounter.setText(s);
     }
     // R2
     /* public void setTimeCounter(String s)
     {
         timeCounter.setText(s);
     }*/
     /**
      * Makes a Panel for the Twitter message feature.
      * @return JPanel messagePanel to be added to the tabbedPane.
      */
     private JPanel messagePanel() {
         JPanel messagePanel = new JPanel();
         return messagePanel;
     }
     /**
      * Takes an Actionlistener to be added to the Delete button.
      * @param a an ActionListener for the Delete Tweet button.
      */
     public final void addDeleteListener(final ActionListener a) {
         dListen = a;
     }
     // R2
     /**
      * Takes an ActionListener to be added to the Favorite button.
      * @param a an ActionListener for the Favorite Tweet button.
      */
     public final void addFavoriteListener(final ActionListener a)   {
         faListen = a;
     }
     /**
      * Takes an ActionListener to be added to the Unfavorite button.
      * @param a an ActionListener for the Unfavorite Tweet button.
      */
     public final void addFollowListener(final ActionListener a) {
         foListen = a;
     }
     /**
      * Takes an ActionListener to be added to the Retweet button.
      * @param a an ActionListener for the Retweet button.
      */
     public final void addRetweetListener(final ActionListener a)    {
         rListen = a;
     }
     /**
      * Takes an ActionListener to be added to the Post button.
      * @param a an ActionListener for the Post Tweet button.
      */
     public final void addPostButtonListener(final ActionListener a) {
         post.addActionListener(a);
     }
     /**
      * Takes an ActionListener to be added to the Save Draft button.
      * @param a an ActionListener for the Save Draft button.
      */
     public final void addSaveDraftButtonListener(final ActionListener a) {
         saveDraft.addActionListener(a);
     }
     /**
      * Takes an ActionListener to be added to the Save Draft button.
      * @param a an ActionListener for the Save Draft button.
      */
     public final void addLoadDraftButtonListener(final ActionListener a) {
         loadDraft.addActionListener(a);
     }
     /**
      * Takes an ActionListener to be added to the Clear Drafts button.
      * @param a an ActionListener for the Clear Drafts button.
      */
     public final void addClearDraftButtonListener(final ActionListener a) {
         clearDraft.addActionListener(a);
     }
     
     /**
      * Instantiates a DraftList object to load a drafted tweet.
      * @param t Map<String, String> which holds the draft information.
      */
     public final void callLoadDraft(final Map<String, String> t)  {
         DraftList list = new DraftList(t);
     }
     /**
      * Takes an actionListener to be added to the Favorites button. This
      * button refreshes the Favorites tab.
      * @param a an ActionListener for the Favorites button.
      */
     public final void addFavoriteButtonListener(final ActionListener a) {
         favoriteButton.addActionListener(a);
     }
     /**
      * Takes an actionListener to be added to the Home button. This
      * button refreshes the Home tab.
      * @param a an ActionListener for the Home button.
      */
     public final void addHomeButtonListener(final ActionListener a) {
         homeButton.addActionListener(a);
     }
     /**
      * Takes an ActionListener to be added to the search button.
      * @param a ActionListener
      */
     public final void addSearchButtonListener(final ActionListener a)   {
         search.addActionListener(a);
         terms.addActionListener(a);
     }
     
     /**
      * Takes an ActionListener to be added to the reply button.
      * @param a ActionListener
      */
     public final void addReplyButtonListener(final ActionListener a) {
         repListen = a;        
     }
     /**
      * getPostText() returns the text within the posting text area.
      * @return String containing the text within the posting text area.
      */
     public final String getPostText() {
         return textPost.getText();
     }
     /**
      * Return an individual JPanel to display a status.
      * @param s is a Status to be placed within the pane.
      * @param i is ImagePanel to be used for the tweetPane.
      * @return a JPanel to display the individual Status through the GUI.
      */
     public final JPanel tweetPane(final Status s, final ImagePanel i)   {
         ImagePanel iPanel = i;
         i.setBorder(BorderFactory.createEtchedBorder());
         final int lngth = 425;
         final int hght = 110;
         Dimension d = new Dimension(lngth, hght);
         i.setPreferredSize(d);
         i.setMaximumSize(d);
         status = s;
         JLabel name = new JLabel(s.getUser().getName() + " (@" + s.getUser().
                 getScreenName() + ")");        
         name.setAlignmentX(CENTER_ALIGNMENT);
 
         Font curFont = name.getFont();
         name.setFont(new Font(curFont.getFontName(),
                 curFont.getStyle(), fSize));
         name.setForeground(Color.BLUE);
         JPanel textpane = new ImagePanel();
         //textpane.setBackground(c);
         JTextArea text = new JTextArea(s.getText() + "\n"
                 + s.getCreatedAt().toString());
         text.setEnabled(false);
         text.setOpaque(false);
         text.setAlignmentX(CENTER_ALIGNMENT);
         //text.setBackground(c);
         text.setDisabledTextColor(Color.BLACK);
         text.setLineWrap(true);
         text.setWrapStyleWord(true);
         final int col = 30;
         text.setColumns(col);
         textpane.add(text);
         iPanel.setLayout(new BoxLayout(iPanel, BoxLayout.Y_AXIS));
         iPanel.add(name);
         iPanel.add(text);
         JPanel buttonPanel = new JPanel();
         buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
         buttonPanel.setOpaque(false);
 
         buttonPanel.add(favoriteBox(s));        
         buttonPanel.add(followBox(s));
         buttonPanel.add(retweetBox(s));
         if (status.getUser().getScreenName().equalsIgnoreCase("350nittwit")) {
             delete = new JButton("Delete");
             delete.addActionListener(dListen);
             buttonPanel.add(delete);
             deleteMap.put(delete, status.getId());
         }   else {
             ActionListener replyListen = new ActionListener() {
                 @Override
                 public void actionPerformed(final ActionEvent e)    {
                     replyToTweet(s);                    
                 }    
             };
             JButton repButton = new JButton("Reply");
             repButton.addActionListener(replyListen);
             buttonPanel.add(repButton);
         }
         iPanel.add(buttonPanel);
         return iPanel;
     }
     /**
      * Creates a JCheckBox for favoriting functions.
      * @param s Status of specific tweet.
      * @return JCheckBox for display in panel.
      */
     private JCheckBox favoriteBox(final Status s) {
         if (!s.isFavorited()) {
             favorite = new JCheckBox("Favorite");
             favorite.setOpaque(false);
             favorite.setSelected(false);
             favorite.addActionListener(faListen);
             favoriteMap.put(favorite, s.getId());
             return favorite;
           } else {
               favorite = new JCheckBox("Favorite");
               favorite.setOpaque(false);
               favorite.setSelected(true);
               favorite.addActionListener(faListen);
               favoriteMap.put(favorite, s.getId());
               /*favorite.setSelected(true);
               favorite.addActionListener(faListen);
               favoriteMap.put(favorite, s.getId());*/
               return favorite;
           }
     }
 
     /**
      * Creates a JCheckBox for following functions.
      * @param s Status of specific tweet.
      * @return JCheckBox to be added to panel.
      */
     private JCheckBox followBox(final Status s) {
         follow = new JCheckBox("Follow");
         follow.setOpaque(false);
         follow.addActionListener(rListen);
         //followMap.put(retweet, s.getId());
         //return reTweet;
         if (isFollowed(s.getUser().getScreenName())) {
                 follow = new JCheckBox("Follow");
                 follow.setOpaque(false);
                 follow.setSelected(true);
                 follow.addActionListener(foListen);
                 followMap.put(follow, s.getUser().getId());
                 return follow;
               } else {
                   follow = new JCheckBox("Follow");
                   follow.setOpaque(false);
                   follow.addActionListener(foListen);
                   followMap.put(follow, s.getUser().getId());
                   return follow;
               }
     }
     /**
      * Checks whether a specific tweeter is followed by the user.
      * @param s String representing username of tweeter.
      * @return boolean true if followed false if otherwise.
      */
     private boolean isFollowed(final String s) {
         Twitter t = new TwitterFactory().getInstance();
         try {
             return t.showFriendship("350nittwit", s).isSourceFollowingTarget();
         } catch (TwitterException e) {
             return false;
         }
     }
     /**
      * Creates JCheckBox for the retweeting function.
      * @param s Specific Status of tweet.
      * @return JCheckBox to be added to panel.
      */
     private JCheckBox retweetBox(final Status s) {
         retweet = new JCheckBox("Retweet");
         retweet.setOpaque(false);
 
         retweet.addActionListener(rListen);
         retweetMap.put(retweet, s.getId());
         return retweet;
     }
 
     /**
      * Takes a status and transcribes the info into a JPanel.
      * @param s takes a single Status to transcribe onto a JPanel through
      * the tweetPane method.
      * @return a JPanel containing the panel that holds the Status information
      */
     public final JPanel searchPane(final Status s)  {
         JPanel sPanel = new JPanel();
         String str = "twitter1.jpg";
         tPane = tweetPane(s, new ImagePanel(str));
         sPanel.add(tPane);
         return sPanel;
     }
     /**
      * Returns a Map for the delete function with JButtons as the keys and the
      * ID for a Tweet as the information.
      * @return  a Map relating the Status and an ID (identifier) so that the
      * Status may be manipulated by the user through the GUI.
      */
     public final Map<JButton, Long> getDeleteMap()  {
         return deleteMap;
     }
     /**
      * Returns Map associated with Favorites function.
      * @return Map<JCheckBox, Long> for favorites function.
      */
     public final Map<JCheckBox, Long> getFavoritesMap() {
         return favoriteMap;
     }
     /**
      * Returns Map associated with Retweet function.
      * @return Map<JCheckBox, Long> for Retweet function.
      */
     public final Map<JCheckBox, Long> getRetweetMap() {
         return retweetMap;
     }
     
     /**
      * Returns Map associated with the reply function.
      * @return Map<Long, String> for reply function.
      */
     public final Map<Long, String> getReplyMap()    {
         return replyMap;
     }
     
     /**
      * Returns Map associated with the Follow function.
      * @return Map<JCheckBox, Long> for the Follow function.
      */
     public final Map<JCheckBox, Long> getFollowMap()    {
     	return followMap;
     }
     /**
      * Takes a List of Statuses and uses the tweetPane method to transcribe
      * those Statuses onto JPanels.
      * @param list a List<Status> that will be displayed through the GUI.
      * @return a JPanel containing individual panels for each status within
      * the parameter.
      */
     public final JPanel searchPane(final List<Status> list) {
         JPanel sPanel = new JPanel();
         sPanel.setLayout(new BoxLayout(sPanel, BoxLayout.Y_AXIS));
         String s = "twitter1.jpg";
         for (Status t : list) {
             tPane = tweetPane(t, new ImagePanel(s));
             //tPanes.add(tPane);
             sPanel.add(tPane);
             if (s.equals("twitter1.jpg")) {
                 s = "twitter2.jpg";
             }  else {
                 s = "twitter1.jpg";
             }
         }
         return sPanel;
     }
     /**
      * menuListener is an ActionListener for the menu functions.
      */
     private ActionListener menuListener = new ActionListener()
     {
         public void actionPerformed(final ActionEvent e)    {
             Object source = e.getSource();
             // if (source == xml) {
             // exportButtonAction();
             // }
             if (source == quit) {
                 JFrame quitting = new JFrame();
                 String message = "Are you sure you want to quit?";
                 int answer = JOptionPane.showConfirmDialog(quitting, message,
                         "Quit", JOptionPane.YES_NO_OPTION);
                 if (answer == JOptionPane.YES_OPTION) {
                     System.exit(0);
                 }
             }           
         }
     };
     /**
      * Displays an error message box to the user as a generic function to
      * be called with different parameters by different methods, depending on
      * the context of the operation.
      * @param message is the dialogue to be displayed to the user.
      */
     public final void showMessage(final String message) {
         JOptionPane.showMessageDialog(new JPanel(), message, "Error",
                 JOptionPane.INFORMATION_MESSAGE);
     }
     /**
      * Displays a "yes"/"no" dialogue box to the user as a generic function to
      * be called with different parameters by different methods, depending on
      * the context of the operation.
      * @param dialogue is the dialogue to be displayed to the user.
      * @return an integer representing "yes" or "no", depending on user choice.
      */
     public final int showDialogue(final String dialogue)    {
         int answer = JOptionPane.showConfirmDialog(
                 new JPanel(), dialogue, "Confirm", JOptionPane.YES_NO_OPTION);
         return answer;
     }
     
     /**
 	 * Erases currently saved drafts.
 	 * @throws IOException if File not found.
 	 */
 	public final void eraseDrafts() throws IOException {
 		PrintWriter outfile = new PrintWriter(new FileWriter("drafts.txt"));
 		outfile.print("");
 		outfile.close();
 	}
 
     // R2
     /**
      * Converts the seconds the application has been running into days,
      * hours, minutes and seconds. Used for time logging functions in
      * the application.
      * @param seconds is the seconds that the clock has been running since
      * the application was launched.
      * @return a String that acts as a clock.
      */
     /*public String secondsToClock(int seconds)
     {
         int days = seconds / 86400;
         int hours = seconds / 3600;
         int minutes = seconds / 60;
         int hoursRemaining = hours - (days * 24);
         int minutesRemaining = minutes - (hours * 60);
         int secondsRemaining = seconds - (minutes * 60);
         String result = days + "d " + hoursRemaining + "h " + minutesRemaining
                 + "m " + secondsRemaining + "s";
         return result;
     }*/
     /**
      * The DraftList class creates a JFrame to hold the LoadDraft functions.
      * @author venlete
      */
     public class DraftList extends JFrame  {
         /**
          * Map that holds the different drafts.
          */
         private Map<String, String> drafts;
         /**
          * The titles of the different drafts.
          */
         private String[] draftTitles;
         /**
          * JPanel for the LoadDraft screen.
          */
         private JPanel listPanel;
         /**
          * JList to hold the draft titles.
          */
         private JList dList;
         /**
          * JButtons for select draft and cancel.
          */
         private JButton cancel, select;
         /**
          * Text area to display the actual draft.
          */
         private JTextArea text;
         /**
          * Font to be used in JList and TextArea.
          */
         private Font font = new Font("Verdana", Font.PLAIN, fSize);
         
         /**
          * Constructor for DraftList which makes the LoadDraft screen.
          * @param s Map<String,String> which holds the different drafts.
          */
         public DraftList(final Map<String, String> s) {
             
             drafts = s;
             System.out.println(drafts);
             Set<String> keys = drafts.keySet();
             draftTitles = new String[keys.size()];
             int i = 0;
             for (String key: keys) {
                 draftTitles[i] = key;
                 i++;
             }
             createJListWindow();
             setVisible(true);
            
         }
 
 		/**
          * createJListWindow() instantiates the frame for the Load
          * Draft screen.
          */
         private void createJListWindow()    {
             setTitle("Select Draft");
             final int w = 600;
             final int h = 400;
             Dimension d = new Dimension(w, h);
             setMinimumSize(d);
             setMaximumSize(d);
             setPreferredSize(d);
             
             listPanel = new JPanel();
             listPanel.setMinimumSize(d);
             listPanel.setMaximumSize(d);
             listPanel.setPreferredSize(d);
             listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.X_AXIS));
             
             dList = new JList(draftTitles);
             final int w1 = 200;
             final int h1 = 315;
             d = new Dimension(w1, h1);
             dList.setMaximumSize(d);
             dList.setPreferredSize(d);
             
             
             
             dList.setFont(font);
             dList.setBorder(BorderFactory.createEtchedBorder());
             dList.addMouseListener(mListen);
             JScrollPane p = new JScrollPane(dList);
             p.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            p.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
             p.getVerticalScrollBar().setUnitIncrement(scroll);
             p.setMaximumSize(d);
             p.setPreferredSize(d);
             listPanel.add(Box.createHorizontalStrut(strut));
             listPanel.add(p);
             listPanel.add(eastDraft());
             add(listPanel);            
         }
         
         /**
          * Makes a JPanel for the east side of the LoadDraft
          * screen.
          * @return JPanel eastP for the LoadDraft screen.
          */
         private JPanel eastDraft() {
             JPanel eastP = new JPanel();
             final int w = 375;
             final int h = 450;
             Dimension d = new Dimension(w, h);
             eastP.setMinimumSize(d);
             eastP.setMaximumSize(d);
             eastP.setPreferredSize(d);
             eastP.setLayout(new BoxLayout(eastP, BoxLayout.Y_AXIS));
             text = new JTextArea();
             final int w2 = 350;
            final int h2 = 300;
            Dimension d2 = new Dimension(w2, h2);
            text.setMinimumSize(d2);
            text.setMaximumSize(d2);
            text.setPreferredSize(d2);
             text.setFont(font);
             text.setBorder(BorderFactory.createEtchedBorder());
             text.setLineWrap(true);
            text.setOpaque(false);
             eastP.add(Box.createVerticalStrut(strut + strut));
            JScrollPane e = new JScrollPane(text);
            e.setMaximumSize(d2);
            e.setPreferredSize(d2);
            e.setMinimumSize(d2);
            e.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            e.getVerticalScrollBar().setUnitIncrement(scroll);
            e.setOpaque(false);
            e.getViewport().setOpaque(false);
            e.setBorder(BorderFactory.createEtchedBorder());
            eastP.add(e);
             eastP.add(Box.createVerticalStrut(strut));
             eastP.add(buttonPanel());
             eastP.add(Box.createVerticalStrut(strut));
             return eastP;
         }
         
         /**
          * buttonPanel makes a panel for the cancel and select buttons.
          * @return JPanel panel for the buttons.
          */
         private JPanel buttonPanel() {
             JPanel butPanel = new JPanel();
             select = new JButton("Select Draft");
             cancel = new JButton("Cancel");
             select.addActionListener(sListen);
             cancel.addActionListener(sListen);
             butPanel.setLayout(new BoxLayout(butPanel, BoxLayout.X_AXIS));
             butPanel.add(select);
             butPanel.add(cancel);
             return butPanel;
         }
         
         /**
          * sListen is an ActionListener for the select and cancel
          * buttons.
          */
         private ActionListener sListen = new ActionListener() {
 
             @Override
             public void actionPerformed(final ActionEvent e)    {
                 if (((JButton) e.getSource()).getText() 
                         == "Select Draft") {
                     textPost.setText(text.getText());
                     dispose();
                 }   else {
                     dispose();
                 }
                 
             }
            
         };         
         
         /**
          * mListen is a MouseListener for the JList.
          */
         private MouseListener mListen = new MouseListener() {
 
             @Override
             public void mouseClicked(final MouseEvent e)    {
               int index = dList.getSelectedIndex();               
               text.setText(drafts.get(draftTitles[index]));
             }
 
             @Override
             public void mouseEntered(final MouseEvent arg0)   {                
             }
 
             @Override
             public void mouseExited(final MouseEvent arg0)  {                
             }
 
             @Override
             public void mousePressed(final MouseEvent arg0) {               
             }
 
             @Override
             public void mouseReleased(final MouseEvent arg0)    {         
             }
             
         };
     }
 }
 
 
