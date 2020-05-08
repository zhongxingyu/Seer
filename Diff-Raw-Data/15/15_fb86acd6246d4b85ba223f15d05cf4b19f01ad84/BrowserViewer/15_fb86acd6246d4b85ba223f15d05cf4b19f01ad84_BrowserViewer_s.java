 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JEditorPane;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.event.HyperlinkEvent;
 import javax.swing.event.HyperlinkListener;
 
 
 /**
  * A class used to display the viewer for a simple HTML browser.
  * 
  * See this tutorial for help on how to use the variety of components:
  *   http://docs.oracle.com/javase/tutorial/uiswing/examples/components/
  * 
  * @author Owen Astrachan
  * @author Marcin Dobosz
  * @author Robert C. Duvall
  */
 @SuppressWarnings("serial")
 public class BrowserViewer extends JPanel {
     // constants
     public static final Dimension SIZE = new Dimension(800, 600);
     public static final String PROTOCOL_PREFIX = "http://";
     public static final String BLANK = " ";
 
     // web page
     private JEditorPane myPage;
     // information area
     private JLabel myStatus;
     // navigation
     private JTextField myURLDisplay;
     private JButton myBackButton;
     private JButton myNextButton;
     private JButton myHomeButton;
     // favorites
     private JButton myAddButton;
     private DefaultComboBoxModel myFavorites;
     private JComboBox myFavoritesDisplay;
     // the data
     private BrowserModel myModel;
 
 
     /**
      * Create a view of the given model of a web browser.
      */
     public BrowserViewer (BrowserModel model) {
         myModel = model;
         // add components to frame
         setLayout(new BorderLayout());
         // must be first since other panels may refer to page
         add(makePageDisplay(), BorderLayout.CENTER);
         add(makeInputPanel(), BorderLayout.NORTH);
         add(makeInformationPanel(), BorderLayout.SOUTH);
         // control the navigation
         enableButtons();
     }
 
     /**
      * Display given URL.
      */
     public void showPage (String url) {
         try {
             if (url != null) {
                 // check for a valid URL before updating model, view
                 URL valid = new URL(completeURL(url)); 
                 myModel.go(valid);
                 update(valid);
             }
         }
         catch (MalformedURLException e) {
             showError("Could not load " + url);
         }
     }
 
     /**
      * Display given message as an error in the GUI.
      */
     public void showError (String message) {
         JOptionPane.showMessageDialog(this,
                                       message,
                                       "Browser Error",
                                       JOptionPane.ERROR_MESSAGE);
     }
 
     /**
      * Display given message as information in the GUI.
      */
     public void showStatus (String message) {
         myStatus.setText(message);
     }
 
     // move to the next URL in the history
     private void next () {
         update(myModel.next());
     }
 
     // move to the previous URL in the history
     private void back () {
         update(myModel.back());
     }
 
     // change current URL to the home page, if set
     private void home () {
         showPage(myModel.getHome().toString());
     }
 
     // update just the view to display given URL
    private void update (URL url) {
         try {
             myPage.setPage(url);
             myURLDisplay.setText(url.toString());
             enableButtons();
         }
         catch (IOException e) {
             // should never happen since only checked URLs make it this far ...
             showError("Could not load " + url);
         }
     }
 
     // prompt user for name of favorite to add to collection
     private void addFavorite () {
         String name = JOptionPane.showInputDialog(this,
                                                   "Enter name",
                                                   "Add Favorite",
                                                   JOptionPane.QUESTION_MESSAGE);
         // did user make a choice?
         if (name != null) {
             myModel.addFavorite(name);
             myFavorites.addElement(name);
         }
     }
 
     // deal with a potentially incomplete URL, 
     //   e.g., let user leave off initial protocol
     private String completeURL (String url) {
         if (! url.startsWith(PROTOCOL_PREFIX)) {
             return PROTOCOL_PREFIX + url;
         }
         return url;
     }
 
     // only enable buttons when useful to user
     private void enableButtons () {
         myBackButton.setEnabled(myModel.hasPrevious());
         myNextButton.setEnabled(myModel.hasNext());
         myHomeButton.setEnabled(myModel.getHome() != null);
     }
 
     // convenience method to create HTML page display
     private JComponent makePageDisplay () {
         // displays the web page
         myPage = new JEditorPane();
         myPage.setPreferredSize(SIZE);
         // allow editor to respond to link-clicks/mouse-overs
         myPage.setEditable(false);
         myPage.addHyperlinkListener(new LinkFollower());
         return new JScrollPane(myPage);
     }
 
     // organize user's options for controlling/giving input to model
     private JComponent makeInputPanel () {
         JPanel result = new JPanel(new BorderLayout());
         result.add(makeNavigationPanel(), BorderLayout.NORTH);
         result.add(makePreferencesPanel(), BorderLayout.SOUTH);
         return result;
     }
 
     // make the panel where "would-be" clicked URL is displayed
     private JComponent makeInformationPanel () {
         // BLANK must be non-empty or status label will not be displayed in GUI
         myStatus = new JLabel(BLANK);
         return myStatus;
     }
 
     // make user-entered URL/text field and back/next buttons
     private JComponent makeNavigationPanel () {
         JPanel result = new JPanel();
 
         myBackButton = new JButton("Back");
         myBackButton.addActionListener(new ActionListener()
         {
             public void actionPerformed (ActionEvent e)
             {
                 back();
             }
         });
        panel.add(myBackButton);
 
         myNextButton = new JButton("Next");
         myNextButton.addActionListener(new ActionListener()
         {
             public void actionPerformed (ActionEvent e)
             {
                 next();
             }
         });
        panel.add(myNextButton);
 
         myHomeButton = new JButton("Home");
         myHomeButton.addActionListener(new ActionListener()
         {
             public void actionPerformed (ActionEvent e)
             {
                 home();
             }
         });
        panel.add(myHomeButton);
 
         // if user presses button, load/show the URL
         JButton goButton = new JButton("Go");
         goButton.addActionListener(new ShowPageAction());
         result.add(goButton);
 
         // if user presses return, load/show the URL
         myURLDisplay = new JTextField(35);
         myURLDisplay.addActionListener(new ShowPageAction());
         result.add(myURLDisplay);
 
         return result;
     }
 
     // make buttons for setting favorites/home URLs
     private JComponent makePreferencesPanel () {
         JPanel result = new JPanel();
 
         myAddButton = new JButton("Add Favorite");
         myAddButton.addActionListener(new ActionListener()
         {
             public void actionPerformed (ActionEvent e)
             {
                 addFavorite();
             }
         });
         result.add(myAddButton);
 
         myFavorites = new DefaultComboBoxModel();
         myFavorites.addElement(" All Favorites ");
         myFavoritesDisplay = new JComboBox(myFavorites);
         result.add(myFavoritesDisplay);
 
         JButton setHomeButton = new JButton("Set Home");
         setHomeButton.addActionListener(new ActionListener()
         {
             public void actionPerformed (ActionEvent e)
             {
                myModel.setHome(myURLDisplay.getText());
                 enableButtons();
             }
         });
         result.add(setHomeButton);
 
         return result;
     }
 
     /**
      * Inner class to factor out showing page associated with the entered URL
      */
     private class ShowPageAction implements ActionListener {
         @Override
         public void actionPerformed (ActionEvent e) {
             showPage(myURLDisplay.getText());
         }
     }
 
     /**
      * Inner class to deal with link-clicks and mouse-overs
      */
     private class LinkFollower implements HyperlinkListener {
         @Override
         public void hyperlinkUpdate (HyperlinkEvent evt) {
             // user clicked a link, load it and show it
             if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                 showPage(evt.getURL().toString());
             }
             // user moused-into a link, show what would load
             else if (evt.getEventType() == HyperlinkEvent.EventType.ENTERED) {
                 showStatus(evt.getURL().toString());
             }
             // user moused-out of a link, erase what was shown
             else if (evt.getEventType() == HyperlinkEvent.EventType.EXITED) {
                 showStatus(BLANK);
             }
         }
     }
 }
