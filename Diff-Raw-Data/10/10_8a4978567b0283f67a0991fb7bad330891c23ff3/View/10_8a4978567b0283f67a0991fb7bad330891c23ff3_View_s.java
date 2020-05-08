 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.GridBagLayout;
 
 import javax.swing.BoxLayout;
 import javax.swing.ButtonGroup;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JTextArea;
 
 import java.awt.Color;
 import java.util.ArrayList;
 
 import javax.swing.JLabel;
 
 
 /**
  * Singleton instantiation
  *
  */
 @SuppressWarnings("serial")
 public class View extends JFrame{
 	
 	private static View view = null;
 	private Container contentPane;
 	private ActionController actionController;
 	
 	private JPanel scorePanel;
 	private int questionsAnswerd;
 	private int correctAnswers;
 
 
 	private JPanel currentView; // question, fasit or finished
 	
 	//Question view
 	private JPanel questionPanel;
 	private JLabel currentQuestion;
 	private JPanel currentAnswersPanel;
 	private ArrayList<JRadioButton> currentAnswers;
 	private ButtonGroup currentAnswersGroup;
 	private JPanel currentQuestionPanel;
 	
 	//Fasit view
 	private JPanel fasitPanel;
 	private JPanel wrongRightImagePanel;
 	private JPanel wrongRightLabelHolder;
 	private JLabel currentWrongRightLabel;
 	private JPanel movieDescriptionHolder;
 	private JTextArea currentMovieDescription;
 	
 	//Finished view
 	private JPanel finishedPanel;
 	private JPanel scoreLabelHolder;
 	private JLabel currentScoreLabel;
 	
 	//buttons
 	private JPanel buttonPanel;
 	private JButton currentButton;
 	private JButton answerButton;
 	private JButton startButton;
 	private JButton nextButton;
 	private JButton finishButton;
 	
 
 
 	/**
 	 * 
 	 */
 	private View(){
 		setUpInterFace();
 	}
 	
 	public static View instantiate(){
 		
 		if(view == null){
 			view = new View();
 		}
 		
 		return view;
 	}
 	
 	
 
 	public void setQuestion(String question, ArrayList<String> answerList){
 		updateScore();
 		
 		if(currentView != questionPanel){
 			if(currentView != null){
 				contentPane.remove(currentView);
 			}
 			currentView = questionPanel;
 			contentPane.add(questionPanel, BorderLayout.CENTER);
 		}
 		
 		currentQuestionPanel.remove(currentQuestion);
 		currentQuestion = new JLabel(question);
 		
 		buttonPanel.remove(currentButton);
 		currentButton = answerButton;
 		buttonPanel.add(currentButton);
 		
 		if(currentAnswers.size() >= 1){
 			for(JRadioButton answer : currentAnswers){
 				currentAnswersGroup.remove(answer);
 				currentAnswersPanel.remove(answer);
 			}
 			currentAnswers.clear();
 		}
 
 		for(String answer : answerList){
 			JRadioButton button = new JRadioButton(answer);
 			button.setBackground(Color.WHITE);
 			button.setActionCommand(answer);
 			currentAnswers.add(button);
 			currentAnswersPanel.add(button);
 			currentAnswersGroup.add(button);
 			//button.setVisible(true);
 		}
 		
 		
 		currentQuestionPanel.add(currentQuestion);
 		pack();
 
 	}
 	
 	
 	public String getAnswer() {
 		return currentAnswersGroup.getSelection().getActionCommand();
 	}
 	
 	
 	public void fasit(Boolean correct, String movieDescription, String correctAnswer){
 		questionsAnswerd ++;
 		
 		if(currentView != fasitPanel){
 			if(currentView != null){
 				contentPane.remove(currentView);
 			}
 			currentView = fasitPanel;
 			contentPane.add(fasitPanel, BorderLayout.CENTER);
 		}
 		
 		if(correct == true){
 			correctAnswers ++;
 			//Sette riktig-bilde
 			wrongRightLabelHolder.remove(currentWrongRightLabel);
 			currentWrongRightLabel = new JLabel("Riktig");
 			wrongRightLabelHolder.add(currentWrongRightLabel);
 		}
 		else{
 			//Sette galt-bilde
 			wrongRightLabelHolder.remove(currentWrongRightLabel);
 			currentWrongRightLabel = new JLabel("Feil! Riktig svar er " + correctAnswer);
 			wrongRightLabelHolder.add(currentWrongRightLabel);
 		}
 		
 		updateScore();
 		movieDescriptionHolder.remove(currentMovieDescription);
 		currentMovieDescription = textAreaProperties(new JTextArea(movieDescription));
 		movieDescriptionHolder.add(currentMovieDescription);
 		
 		buttonPanel.remove(currentButton);
		if(questionsAnswerd == 4){
 			currentButton = finishButton;
 		}
 		else{
 			currentButton = nextButton;
 		}
 		buttonPanel.add(currentButton);
 		
 		pack();
 		
 	}
 
 	
 	public void finish(){
 		if(currentView != finishedPanel){
 			if(currentView != null){
 				contentPane.remove(currentView);
 			}
 			currentView = finishedPanel;
 			contentPane.add(finishedPanel, BorderLayout.CENTER);
 		}
 		scoreLabelHolder.remove(currentScoreLabel);
		currentScoreLabel = new JLabel("Gratulerer din score ble " + correctAnswers);
 		scoreLabelHolder.add(currentScoreLabel);
 		
 		buttonPanel.remove(currentButton);
 		currentButton = startButton;
 		buttonPanel.add(currentButton);
 		
 		buttonPanel.repaint();
 		pack();
 	}
 
 	
 	/**
 	 * 
 	 */
 	private void setUpInterFace() {
 		actionController = new ActionController(this);
 		questionsAnswerd = 0;
 		correctAnswers = 0;
 		
 		contentPane = this.getContentPane();
 		contentPane.setLayout(new BorderLayout());
 		//contentPane.setFont(new Font("Serif", Font.PLAIN, 13));
 		contentPane.setBackground(Color.WHITE);
 		
 		
 		addLogo();
 		setUpQuestionPanel();
 		addFooter();
 		
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 		setVisible(true);
 		pack();
 	}
 	
 	
 	private void addLogo() {
 		JPanel west = new JPanel();
 		west.setBackground(Color.WHITE);
 		
 		JLabel logo = new JLabel();
 		ImageIcon imgThisImg = new ImageIcon("img/semantic_Quiz2.jpg");
 		logo.setIcon(imgThisImg);
 		west.add(logo);
 		
 		contentPane.add(west, BorderLayout.WEST);
 	}
 	
 	
 	private void updateScore(){
 		
 		if(scorePanel != null){
 			contentPane.remove(scorePanel);
 		}
 		
 		JLabel score = new JLabel(correctAnswers + " / " + questionsAnswerd);
 		scorePanel = new JPanel();
 		scorePanel.add(score);
 		contentPane.add(scorePanel, BorderLayout.NORTH);
 		contentPane.repaint();
 		pack();
 	}
 	
 	
 	private void addFooter(){
 		answerButton = new JButton("Answer");
 		answerButton.addActionListener(actionController);
 		
 		startButton = new JButton("Start");
 		startButton.addActionListener(actionController);
 		
 		nextButton = new JButton("Neste");
 		nextButton.addActionListener(actionController);
 		
 		finishButton = new JButton("Ferdig");
 		finishButton.addActionListener(actionController);
 		
 		buttonPanel = new JPanel();
 		buttonPanel.setBackground(Color.WHITE);
 		buttonPanel.add(startButton);
 		currentButton = startButton;
 		
 		contentPane.add(buttonPanel, BorderLayout.SOUTH);
 	}
 
 	
 	/**
 	 * Skal parteres
 	 */
 	private void setUpQuestionPanel() {
 		
 		//QuestionView
 		questionPanel = new JPanel();
 		questionPanel.setLayout(new BoxLayout(questionPanel, BoxLayout.Y_AXIS)); //shall be vertical
 		questionPanel.setPreferredSize(new Dimension(500, 500));
 		questionPanel.setBackground(Color.WHITE);
 		contentPane.add(questionPanel, BorderLayout.CENTER);
 		currentView = questionPanel;
 		
 		currentQuestion = new JLabel("Welcome!");
 		currentQuestionPanel = new JPanel();
 		currentQuestionPanel.add(currentQuestion);
 		currentQuestionPanel.setBackground(Color.WHITE);
 		currentAnswersPanel = new JPanel();
 		currentAnswersPanel.setBackground(Color.WHITE);
 		currentAnswers = new ArrayList<JRadioButton>();
 		currentAnswersGroup = new ButtonGroup();
 		questionPanel.add(currentQuestionPanel);
 		questionPanel.add(currentAnswersPanel);
 		
 		//Fasit View
 		fasitPanel = new JPanel();
 		fasitPanel.setLayout(new BoxLayout(fasitPanel, BoxLayout.Y_AXIS)); //shall be vertical
 		fasitPanel.setPreferredSize(new Dimension(500, 500));
 		fasitPanel.setBackground(Color.WHITE);
 		
 		wrongRightImagePanel = new JPanel();
 		wrongRightImagePanel.setBackground(Color.WHITE);
 		wrongRightLabelHolder = new JPanel();
 		wrongRightLabelHolder.setBackground(Color.WHITE);
 		currentWrongRightLabel = new JLabel("");
 		wrongRightLabelHolder.add(currentWrongRightLabel);
 		movieDescriptionHolder = new JPanel();
 		movieDescriptionHolder.setBackground(Color.WHITE);
 		currentMovieDescription = textAreaProperties(new JTextArea());
 		movieDescriptionHolder.add(currentMovieDescription);
 		
 		fasitPanel.add(wrongRightImagePanel);
 		fasitPanel.add(wrongRightLabelHolder);
 		fasitPanel.add(movieDescriptionHolder);
 		
 		//Finished view
 		finishedPanel = new JPanel();
 		finishedPanel.setLayout(new BoxLayout(finishedPanel, BoxLayout.Y_AXIS)); //shall be vertical
 		finishedPanel.setPreferredSize(new Dimension(500, 500));
 		finishedPanel.setBackground(Color.WHITE);
 		
 		scoreLabelHolder = new JPanel();
 		scoreLabelHolder.setBackground(Color.WHITE);
 		currentScoreLabel = new JLabel("Hei");
 		scoreLabelHolder.add(currentScoreLabel);
 		
 		finishedPanel.add(scoreLabelHolder);
 	}
 	
 	private JTextArea textAreaProperties(JTextArea textArea) {
 	    textArea.setEditable(false);  
 	    textArea.setLineWrap(true);
 	    textArea.setWrapStyleWord(true);
 	    return textArea;
 	}
 
 //	/**
 //	 * @param args 
 //	 * Temporary
 //	 */
 //	public static void main(String[] args) {
 //		View view = new View();
 //		
 //
 //	}
 
 }
