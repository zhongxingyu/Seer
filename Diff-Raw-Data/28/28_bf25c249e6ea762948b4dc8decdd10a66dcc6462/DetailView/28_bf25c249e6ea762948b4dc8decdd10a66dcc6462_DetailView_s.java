 package arcade.view;
 
 import java.awt.Color;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.List;
 import java.util.ResourceBundle;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JEditorPane;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.WindowConstants;
 import util.ImageHelper;
 import arcade.controller.Controller;
 import arcade.games.Comment;
 import arcade.games.GameInfo;
 import arcade.view.forms.payment.PaymentSelection;
 
 
 /**
  * WILL REFACTOR. At this point, for test purpose only
  * 
  * 
  * @author David Liu
  * 
  */
 public class DetailView extends JFrame {
     private static final int WINDOW_WIDTH = 600;
     private static final int WINDOW_HEIGHT = 600;
     private static final int LABEL_HEIGHT = 15;
     private static final int LABEL_WIDTH = 100;
     private static final int ORIGIN_X = 5;
     private static final String NEWLINE = "\n";
     private static final String HTML_HEADER = "<html>";
     private static final String BOLD_HEADER = "<b>";
 
     private static final String BOLD_TAIL = "</b>";
     private static final String HTML_TAIL = "</html>";
     private static final String HTML_NEWLINE = "<br>";
 
     private ResourceBundle myResources;
     private GameInfo myGameInfo;
     private JPanel myContentPanel;
     private JLabel myTitle,
             myPicture,
             myRating,
             myGenre,
             myDescription,
             myAuthor,
             myComments;
 
     private JTextField myCommentsWriter;
     private JTextField myRatingWriter;
     private JTextArea myDescriptionContent;
     private JEditorPane myCommentsContent;
     private JButton myPlayButton, myBuyButton;
     private Controller myController;
 
     public DetailView (GameInfo info, ResourceBundle resources, Controller controller) {
         myController = controller;
         setBackground(Color.WHITE);
         myGameInfo = info;
         myResources = resources;
         setLayout(null);
         setTitle(resources.getString(TextKeywords.TITLE));
         setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
         setBounds(100, 100, WINDOW_WIDTH, WINDOW_HEIGHT);
 
         myContentPanel = (JPanel) getContentPane();
         ImageIcon icon = myGameInfo.getThumbnail();
         Image scaledImage = ImageHelper.getScaledImage(icon, 160);
         myPicture = new JLabel(new ImageIcon(scaledImage));
         myPicture.setBounds(ORIGIN_X, 5, 160, 160);
 
         myTitle = new JLabel("<html><b><font color = gray>" + info.getName() + "</font></b></html>");
         myTitle.setBounds(170, 0, LABEL_WIDTH, LABEL_HEIGHT);
 
         myGenre = new JLabel(info.getGenre());
         myGenre.setBounds(170, 20, LABEL_WIDTH, LABEL_HEIGHT);
 
         myRating = new JLabel(info.getRating()+"");
         myRating.setBounds(170, 40, LABEL_WIDTH, LABEL_HEIGHT);
 
         String localDirectory = System.getProperty("user.dir");
         ImageIcon playButtonIcon =
                 new ImageIcon(localDirectory + "/src/arcade/resources/images/PlayIcon.jpg");
 
         myPlayButton = new JButton(playButtonIcon);
         myPlayButton.setBounds(380, 50, 130, 110);
         myPlayButton.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed (ActionEvent e) {
                 myController.playGame(myGameInfo);
             }
 
         });
         
         myBuyButton = new JButton("buy"); 
         myBuyButton.setBounds(180, 50, 130, 110);
         myBuyButton.addActionListener(new ActionListener() {
             
             @Override
             public void actionPerformed (ActionEvent arg0) {
                 new PaymentSelection(myController, myResources, myGameInfo);
             }
         });
         
 
         myDescription = new JLabel("Description");
         myDescription.setBounds(ORIGIN_X, 180, LABEL_WIDTH, LABEL_HEIGHT);
         myDescription.setBackground(Color.lightGray);
 
         myDescriptionContent = new JTextArea(myGameInfo.getDescription(), 10, 1);
         int r = Color.GRAY.getRed();
         int g = Color.gray.getGreen();
         int b = Color.GRAY.getBlue();
         Color c = new Color(r, g, b, 20);
         myDescriptionContent.setBackground(c);
 
         JScrollPane descriptionPane = new JScrollPane(myDescriptionContent);
         descriptionPane.setBounds(ORIGIN_X, 200, WINDOW_WIDTH - 25, 200);
         myDescriptionContent.setLineWrap(true);
         myDescriptionContent.setWrapStyleWord(true);
         myDescriptionContent.setEditable(false);
 
         myComments = new JLabel("Comments");
         myComments.setBackground(Color.gray);
         myComments.setBounds(ORIGIN_X, 400, LABEL_WIDTH, LABEL_HEIGHT);
 
         myCommentsContent = new JEditorPane();
         myCommentsContent.setContentType("text/html");
         constructCommentAreaContent();
         JScrollPane commentPane = new JScrollPane(myCommentsContent);
         commentPane.setBounds(ORIGIN_X, 420, WINDOW_WIDTH - 25, 150);
         
         myCommentsWriter = new JTextField();
         myCommentsWriter.setBounds(ORIGIN_X, 600, WINDOW_WIDTH, 50);
        
         
         myRatingWriter = new JTextField();
         myRatingWriter.setBounds(ORIGIN_X, 650, WINDOW_WIDTH, 50);
         JButton ratingButton = new JButton("Submit");
         ratingButton.setBounds(ORIGIN_X, 700, WINDOW_WIDTH, 50);
         ratingButton.addActionListener(new ActionListener() {
             
             @Override
             public void actionPerformed (ActionEvent arg0) {
                 double rating = Double.parseDouble(myRatingWriter.getText());
                myController.commentAndRateGame(myRatingWriter.getText(), rating, myGameInfo.getName());
             }
         });
         
         
 
         myContentPanel.add(myPicture);
         myContentPanel.add(myPlayButton);
         myContentPanel.add(myTitle);
         myContentPanel.add(myGenre);
         myContentPanel.add(myRating);
         myContentPanel.add(myDescription);
         myContentPanel.add(descriptionPane);
         myContentPanel.add(myComments);
         myContentPanel.add(commentPane);
         myContentPanel.add(myCommentsWriter);
         myContentPanel.add(myRatingWriter);
         myContentPanel.add(ratingButton);
 
         setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
         setVisible(true);
     }
 
     private void constructCommentAreaContent () {
         List<Comment> comments = myGameInfo.getComments();
         StringBuilder sb = new StringBuilder();
         for (Comment comment : comments) {
             sb.append(BOLD_HEADER + comment.getUser() + BOLD_TAIL);
             sb.append(HTML_NEWLINE);
             sb.append(comment.getRating() + "");
             sb.append(HTML_NEWLINE);
             sb.append(comment.getComment());
             sb.append(HTML_NEWLINE);           
             sb.append(HTML_NEWLINE);
         }
         myCommentsContent.setText(sb.toString());
     }
 }
