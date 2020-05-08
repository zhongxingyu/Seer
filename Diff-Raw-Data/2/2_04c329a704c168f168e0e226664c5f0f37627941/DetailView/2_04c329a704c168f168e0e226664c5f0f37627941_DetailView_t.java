 package arcade.view;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.List;
 import java.util.ResourceBundle;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JEditorPane;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import arcade.games.GameInfo;
 import arcade.util.JPicture;
 import arcade.util.Pixmap;
 
 public class DetailView extends JFrame{
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
         private static final String HTML_NEWLINE = "<br />";
         
         
         
         
         private ResourceBundle myResources;
         private GameInfo myGameInfo;
         private JPanel myContentPanel;
         private JComponent myPicture;
         private JLabel myTitle,
                                    myRating,
                                    myGenre,
                                    myDescription,
                                    myAuthor,
                                    myComments;
         
 
         private JTextArea myDescriptionContent;
         private JEditorPane myCommentsContent;
         private JButton myPlayButton;
 
 
         public DetailView(GameInfo info, ResourceBundle resources){
                 setBackground(Color.WHITE);
                 myGameInfo= info;
                 myResources = resources;
                 setLayout(null);
                 setTitle(myGameInfo.getName());
                 setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                 setBounds(100, 100, WINDOW_WIDTH, WINDOW_HEIGHT);
 
                 myContentPanel = (JPanel) getContentPane();
                 Pixmap pic = myGameInfo.getThumbnail();
                 myPicture = new JPicture(pic, new Dimension(160,160));
                 myPicture.setBounds(ORIGIN_X,5,160,160);
 
                 myTitle = new JLabel("<html><b><font color = gray>"+"Name"+"</font></b></html>");
                 myTitle.setBounds(170,0, LABEL_WIDTH,LABEL_HEIGHT);
                 
                 myGenre = new JLabel("Genre");
                 myGenre.setBounds(170,20, LABEL_WIDTH,LABEL_HEIGHT);
                 
                 myRating = new JLabel("5");
                 myRating.setBounds(170, 40, LABEL_WIDTH, LABEL_HEIGHT);
                 
                 myPlayButton = new JButton("PLAY!!");
                 myPlayButton.setBounds(400, 100, 180, 80);
                 myPlayButton.addActionListener(new ActionListener(){
 
                     @Override
                     public void actionPerformed (ActionEvent e) {
                        //TODO: LAUNCH the GAME! PLAY!
                         
                     }
                     
                 });
 
                 myDescription = new JLabel("Description");
                 myDescription.setBounds(ORIGIN_X, 180, LABEL_WIDTH,LABEL_HEIGHT);
                 myDescription.setBackground(Color.lightGray);
                 
 
                 myDescriptionContent = new JTextArea(myGameInfo.getDescription(), 10, 1);
                 int r  = Color.GRAY.getRed();
                 int g = Color.gray.getGreen();
                 int b = Color.GRAY.getBlue();
                 Color c = new Color(r,g,b,20);
                 myDescriptionContent.setBackground(c);
                 
                 JScrollPane descriptionPane = new JScrollPane(myDescriptionContent); 
                 descriptionPane.setBounds(ORIGIN_X,200, WINDOW_WIDTH-25, 200);
                 myDescriptionContent.setLineWrap(true);
                 myDescriptionContent.setWrapStyleWord(true);
                 myDescriptionContent.setEditable(false);
 
                 
                 
                 myComments = new JLabel("Comment");
                 myComments.setBackground(Color.gray);
                 myComments.setBounds(ORIGIN_X, 400, LABEL_WIDTH, LABEL_HEIGHT);
                 
                 myCommentsContent = new JEditorPane();
                 myCommentsContent.setContentType("text/html");
                 //constructCommentAreaContent();
                 myCommentsContent.setText(HTML_HEADER+ BOLD_HEADER + "SUBJECT"+ BOLD_TAIL + 
                                           HTML_NEWLINE +
                                                                         "AUTHOR"+
                                                                         HTML_NEWLINE + 
                                                                         "RATING"+
                                                                         HTML_NEWLINE + 
                                                                         "COMMENT" + HTML_TAIL);
                 JScrollPane commentPane = new JScrollPane(myCommentsContent); 
                 commentPane.setBounds(ORIGIN_X, 420, WINDOW_WIDTH-25, 150);
         
                 
                 
                 
                 myContentPanel.add(myPicture);
                 myContentPanel.add(myPlayButton);
                 myContentPanel.add(myTitle);
                 myContentPanel.add(myGenre);
                 myContentPanel.add(myRating);
                 myContentPanel.add(myDescription);
                 myContentPanel.add(descriptionPane);
                 myContentPanel.add(myComments);
                 myContentPanel.add(commentPane);
                 
                 setVisible(true);
         }
 
         private void constructCommentAreaContent() {
                 List<String[]> commentContents = myGameInfo.getComments();
                 StringBuilder sb = new StringBuilder();
                 for(String[] comment: commentContents){
                        String subject = BOLD_HEADER+ comment[0] + NEWLINE +BOLD_TAIL;
                         sb.append(subject);
                         String user  = comment[1] + NEWLINE;
                         sb.append(user);
                         //TODO: rating
                         
                         String content = comment[3] + NEWLINE;
                         sb.append(content);
                 }
                 myCommentsContent.setText(sb.toString());
         }
         
         public static void main(String[] args){
                 GameInfo in = new GameInfo("example","English");
                 DetailView main = new DetailView(in, null);
         }
 
 }
