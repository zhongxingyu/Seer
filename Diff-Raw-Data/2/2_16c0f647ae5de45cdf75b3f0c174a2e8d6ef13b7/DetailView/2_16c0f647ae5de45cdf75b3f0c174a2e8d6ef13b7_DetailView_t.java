 package arcade.view;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.List;
 import java.util.ResourceBundle;
 
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JEditorPane;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.WindowConstants;
 
 import util.ImageHelper;
 import arcade.controller.Controller;
 import arcade.games.Comment;
 import arcade.games.GameInfo;
 import arcade.view.forms.payment.PaymentSelection;
 
 
 /**
  * Detail View is the view to display the detailed information about a game.
  * It serves as a extension of a game snapshot where only a thumbnail, the name 
  * and the rating of the game is displayed. 
  * 
  * Detial view has a thumbnail, the game's name, author, genre, rating. It also 
  * provides previous comments users input and a description the game programmers 
  * write when publishing.
  * 
  * Furthermore, it also provides users the ability to comment and rate this game.
  * 
  * Finally, the actual game is also trigger here, or it allows a user to buy 
  * the game and after purchase, the game would be put into the user's game center.
  * 
  * 
  * @author David Liu, Ellango Jothimurugesan
  * 
  */
 public class DetailView extends JFrame {
 
 	private static final Integer[] RATINGS = {1,2,3,4,5};
 	private static final String GAME_TITLE_FONT_TAIL = "</font></font></b></html>";
 	private static final String GAME_TITLE_FONT_HEADER = "<html><b><font color = gray><font size = 6>";
 	private static final String PLAY_IMAGE_URL = "/src/arcade/resources/images/PlayIcon.jpg";
 	private static final String COMMENT_AREA_CONTENT_TYPE = "text/html";
 	private static final String DESCRIPTION = "Description";
 	private static final String COMMENTS = "Comments";
 	private static final String USER_DIR = "user.dir";
 	private static final String SUBMIT = "Submit";
 	private static final String PRICE = "Price: ";
 	private static final String BUY = "BUY";
 
 	private static final String HTML_BOLD_HEADER = "<b>";
 	private static final String HTML_BOLD_TAIL = "</b>";
 	private static final String NEWLINE = "\n";
 	private static final String HTML_NEWLINE = "<br>";
 
 	private static final int DESCRIPTION_SECTION_BACKGROUND_OPACITY = 20;
 	private static final int WINDOW_Y_COORDINATE = 100;
 	private static final int WINDOW_X_COORDINATE = 100;
 	private static final int DESCRIPTION_COLUMN = 1;
 	private static final int DESCRIPTION_ROWS = 10;
 	private static final int WINDOW_WIDTH = 600;
 	private static final int WINDOW_HEIGHT = 600;
 
 	private ResourceBundle myResources;
 	private GameInfo myGameInfo;
 	private JPanel myContentPanel;
 	private Controller myController;
 
 
 
 	/**
 	 * Create and initialized the detail view
 	 * @param info
 	 * @param resources
 	 * @param controller
 	 */
 	public DetailView (GameInfo info, ResourceBundle resources, Controller controller) {
 		myController = controller;
 		setBackground(Color.WHITE);
 		myGameInfo = info;
 		myResources = resources;
 
 		myContentPanel = (JPanel) getContentPane();
 		setLayout(new BoxLayout(myContentPanel, BoxLayout.PAGE_AXIS));
 		myContentPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
 		setTitle(resources.getString(TextKeywords.TITLE));
 		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 
 
 		createTheBasicInfoPanel();
 		Color c = makeLightGray();
 		createAndConfigureTheDescriptionSection(c);
 		createCommentSection();
 		createCommentingAndRatingArea();
 
 		setBounds(WINDOW_X_COORDINATE, WINDOW_Y_COORDINATE, WINDOW_WIDTH, WINDOW_HEIGHT);
 		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 		setVisible(true);
 	}
 
 
 	/**
 	 * Create the area for users to write comments and rate this game.
 	 * 
 	 */
 	private void createCommentingAndRatingArea() {
 
 		final JTextArea commentsWriter = new JTextArea(3, 30);
 		commentsWriter.setBackground(Color.LIGHT_GRAY);
 		commentsWriter.setWrapStyleWord(true);
 		commentsWriter.setLineWrap(true);
 		final JComboBox ratingBox = new JComboBox(RATINGS);
 
 		JButton submitButton = new JButton(SUBMIT);
 		submitButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed (ActionEvent arg0) {
				double rating = new Double((Integer) ratingBox.getSelectedItem());
 				myController.commentAndRateGame(commentsWriter.getText(), rating, myGameInfo.getName());
 				commentsWriter.setText("");
 			}
 		});
 
 		JPanel subPanel = new JPanel();
 		subPanel.add(commentsWriter);
 		subPanel.add(ratingBox);
 		myContentPanel.add(subPanel);
 		myContentPanel.add(submitButton);
 	}
 
 
 	/**
 	 * Create the comment section with a message saying "comment" and a area
 	 * to display all the comments users have previously input for this game.
 	 * 
 	 */
 	private void createCommentSection() {
 
 		JLabel comments = new JLabel(COMMENTS);
 		comments.setBackground(Color.gray);
 		myContentPanel.add(comments);
 
 		JEditorPane commentsContent = new JEditorPane();
 		commentsContent.setEditable(false);
 		commentsContent.setContentType(COMMENT_AREA_CONTENT_TYPE);
 
 		constructCommentAreaContent(commentsContent);
 		JScrollPane commentPane = new JScrollPane(commentsContent);
 		myContentPanel.add(commentPane);
 	}
 
 
 	/**
 	 * Create the description section with a message "description" and a 
 	 * description of the game provided by the game programmer when publishing. 
 	 * 
 	 * @param c
 	 */
 	private void createAndConfigureTheDescriptionSection(Color c) {
 
 		JLabel description = new JLabel(DESCRIPTION);
 		description.setBackground(c);
 		description.setAlignmentX(Component.LEFT_ALIGNMENT);
 
 		JTextArea descriptionContent = new JTextArea(myGameInfo.getDescription(), DESCRIPTION_ROWS, DESCRIPTION_COLUMN);
 		descriptionContent.setLineWrap(true);
 		descriptionContent.setWrapStyleWord(true);
 		descriptionContent.setEditable(false);
 		descriptionContent.setBackground(c);
 
 		JScrollPane descriptionPane = new JScrollPane(descriptionContent);
 
 		myContentPanel.add(description);
 		myContentPanel.add(descriptionPane);
 	}
 
 
 	/**
 	 * Create the basic info area. It includes a thumbnail for the game, the 
 	 * game's name, author, genre, overall rating and a play or buy button 
 	 * base on whether the user is current in store or game center.
 	 * 
 	 */
 	private void createTheBasicInfoPanel() {
 
 		JPanel basicInfoPanel = new JPanel();
 
 		ImageIcon icon = myGameInfo.getThumbnail();
 		Image scaledImage = ImageHelper.getScaledImage(icon, 160);
 		JLabel picture = new JLabel(new ImageIcon(scaledImage));
 
 		basicInfoPanel.add(picture);
 		basicInfoPanel.add(createSubPanelForBasicInfoPanel(myGameInfo));
 
 		if (myController.getGamesPurchased().contains(myGameInfo))
 			basicInfoPanel.add(createThePlayButton());
 		else
 			basicInfoPanel.add(createThePurchaseButton());
 		myContentPanel.add(basicInfoPanel);
 	}
 
 
 	/**
 	 * Helper function, used to adjust the grayness of background for 
 	 * game description area.
 	 * @return
 	 */
 	private Color makeLightGray() {
 
 		int r = Color.GRAY.getRed();
 		int g = Color.gray.getGreen();
 		int b = Color.GRAY.getBlue();
 
 		Color c = new Color(r, g, b, DESCRIPTION_SECTION_BACKGROUND_OPACITY);
 		return c;
 	}
 
 
 	/**
 	 * Create the buy button which will trigger the payment view and methods.
 	 * 
 	 */
 	private JButton createThePurchaseButton() {
 
 		JButton buyButton = new JButton(BUY+ NEWLINE + PRICE + myGameInfo.getPrice());
 
 		buyButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed (ActionEvent arg0) {
 				new PaymentSelection(myController, myResources, myGameInfo);
 			}
 		});
 
 		return buyButton;
 	}
 
 
 	/**
 	 * Create the play button which starts the game the user selects. 
 	 * 
 	 */
 	private JButton createThePlayButton() {
 
 		String localDirectory = System.getProperty(USER_DIR);
 		ImageIcon playButtonIcon =
 				new ImageIcon(localDirectory + PLAY_IMAGE_URL);
 
 		JButton playButton = new JButton(playButtonIcon);
 
 		playButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed (ActionEvent e) {
 				myController.playGame(myGameInfo);
 			}
 		});
 		return playButton;
 	}
 
 
 	/**
 	 * Helper method used to create a JPanel with the game title, game genre,
 	 * the author, and the overall rating. 
 	 * 
 	 * Used to help better organize the view. 
 	 * 
 	 * @param info
 	 * @return
 	 */
 	private JPanel createSubPanelForBasicInfoPanel(GameInfo info) {
 
 		JPanel subPanelForBasicInfo = new JPanel();
 		subPanelForBasicInfo.setLayout(new BoxLayout(subPanelForBasicInfo, BoxLayout.PAGE_AXIS));
 
 		JLabel title = new JLabel(GAME_TITLE_FONT_HEADER + myGameInfo.getName() + GAME_TITLE_FONT_TAIL);
 		JLabel genre = new JLabel(myGameInfo.getGenre());
 		JLabel author = new JLabel(myGameInfo.getAuthor());
 		JLabel rating = new RatingIcons().makeLabel((int)info.getRating());
 
 		subPanelForBasicInfo.add(title);
 		subPanelForBasicInfo.add(genre);
 		subPanelForBasicInfo.add(author);
 		subPanelForBasicInfo.add(rating);
 
 		return subPanelForBasicInfo;
 	}
 
 
 	/**
 	 * Pulling previous comments from the database and fill the area with
 	 * comments. Comments are formatted with the user name in bold. It also 
 	 * shows what rating the user gave to the game
 	 * 
 	 */
 	private void constructCommentAreaContent (JEditorPane commentPane) {
 		List<Comment> comments = myGameInfo.getComments();
 		StringBuilder sb = new StringBuilder();
 		for (Comment comment : comments) {
 			sb.append(HTML_BOLD_HEADER + comment.getUser() + HTML_BOLD_TAIL);
 			sb.append(HTML_NEWLINE);
 			sb.append(new RatingIcons().makeHTMLText((int)comment.getRating()));
 			sb.append(HTML_NEWLINE);
 			sb.append(comment.getComment());
 			sb.append(HTML_NEWLINE);           
 			sb.append(HTML_NEWLINE);
 		}
 		commentPane.setText(sb.toString());
 	}
 }
