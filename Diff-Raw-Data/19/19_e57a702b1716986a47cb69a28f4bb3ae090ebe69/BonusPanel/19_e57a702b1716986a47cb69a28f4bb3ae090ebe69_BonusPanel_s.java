 package admin.panel.bonus;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.BorderFactory;
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JSpinner;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import admin.MainFrame;
 
 import data.GameData;
 import data.GameData.Field;
 import data.bonus.Bonus;
 import data.bonus.BonusQuestion;
 import data.bonus.BonusQuestion.BONUS_TYPE;
 
 /**
  * 
  * @author Kevin Brightwell, Justin MacDonald, Ramesh Raj
  *
  */
 public class BonusPanel extends JPanel implements Observer {
 
 	private static final long serialVersionUID = 1L;
 
 	JPanel pnlNewQ = new JPanel();
 	JPanel pnlListQ = new JPanel();
 	JPanel pnlTypeQ = new JPanel();
 	JPanel pnlViewWeek = new JPanel();
 	JPanel pnlListWeeks = new JPanel();
 	JPanel pnlMultA = new JPanel();
 	
 	JButton btnNext = new JButton("Next");
 	
 	JLabel lblViewWeek = new JLabel("View Week:");
 	
 	SpinnerNumberModel weekModel = new SpinnerNumberModel(1, 1, 1, 1); // default,low,min,step
 	JSpinner spnWeek = new JSpinner(weekModel);
 	
 	JLabel lblViewQuestion = new JLabel("View Question:");
 	
 	SpinnerNumberModel snmQuestion = new SpinnerNumberModel(1, 1, 1, 1); // default,low,min,step
 	JSpinner spnQuestion = new JSpinner(snmQuestion);
 	
 	JButton btnBack = new JButton("Back");
 	JButton btnSubmit = new JButton("Submit");
 	
 	JButton btnModify = new JButton("Modify");
 	
 	private JTextField txtQuestion;
 	private JTextField txtAnswer;
 	private JTextArea txtQuestionList;
 	
 	private JRadioButton rbMultChoice;
 	private JRadioButton rbShortAnswer;
 
 	private String question;
 	private String answer;
 	
 	private JTextField txtAnswerA;
 	private JTextField txtAnswerB;
 	private JTextField txtAnswerC;
 	private JTextField txtAnswerD;
 	private List<JTextField> txtAnsList;
 	
 	private JRadioButton rbAnswerA;
 	private JRadioButton rbAnswerB;
 	private JRadioButton rbAnswerC;
 	private JRadioButton rbAnswerD;
 	private List<JRadioButton> rbAnsList;
 	
 	private Boolean shortAns;
 	private Boolean modifyBonusQuestion = false;
 	
 	private String currentQuestion;
 	
 	private BonusQuestion bq;
 	
 	private int currentWeek;
 	private int currentQuestionNumber;
 	
 	private ChangeListener clWeek;
 	private ChangeListener clQuestion;
 	
 	/**
 	 * Constructor for Bonus Panel
 	 */
 	public BonusPanel() {
 		super();
 		initFields();
 		
 		this.setLayout(new BorderLayout());
 		currentQuestion = "";
 		initPnlAddQuestion();
 		initPnlQuestionListing();
 		
 		//check if bonus questions already exist
 		if (Bonus.getAllQuestions().isEmpty()){
 			setQuestionAddingPanelEditable(false);
 			btnModify.setEnabled(false);
 		} else {
 			initExistingBonus();
 		}
 
 		initListeners();
 		GameData.getCurrentGame().addObserver(this);
 	}
 	
 	/**
 	 * Initiates all fields necessary, associates button groups
 	 */
 	private void initFields() {
 		txtQuestion = new JTextField("");
 		
 		rbMultChoice = new JRadioButton("Multiple Choice");
 		rbShortAnswer = new JRadioButton("Short Answer");
 		
 		ButtonGroup bg = new ButtonGroup();
 		bg.add(rbMultChoice);
 		bg.add(rbShortAnswer);
 		
 		rbMultChoice.setSelected(true);
 		
 		// multiple choice answer radio buttons
 		bg = new ButtonGroup();
 		rbAnswerA = new JRadioButton("A");
 		rbAnswerB = new JRadioButton("B");
 		rbAnswerC = new JRadioButton("C");
 		rbAnswerD = new JRadioButton("D");
 		rbAnsList = Arrays.asList(rbAnswerA, rbAnswerB, rbAnswerC, rbAnswerD);
 		
 		rbAnswerA.setSelected(true);
 		
 		for (JRadioButton rb: rbAnsList) 
 			bg.add(rb);
 		
 		// multiple choice text fields:
 		txtAnswerA = new JTextField("");
 		txtAnswerB = new JTextField("");
 		txtAnswerC = new JTextField("");
 		txtAnswerD = new JTextField("");
 		txtAnsList = Arrays.asList(txtAnswerA, txtAnswerB, txtAnswerC, txtAnswerD);
 	}
 
 	/**
 	 * construct the top panel
 	 */
 	private void initPnlAddQuestion() {
 		pnlNewQ.removeAll();
 		pnlTypeQ.removeAll();
 		
 		pnlNewQ.setLayout(new GridLayout(1, 4, 50, 20));
 		pnlTypeQ.setLayout(new BorderLayout());
 		
 		txtQuestion.setBorder(BorderFactory.createTitledBorder("Question"));
 		txtQuestion.setPreferredSize(new Dimension(300, 100));
 		
 		rbMultChoice.setAlignmentY(Component.CENTER_ALIGNMENT);
 		rbShortAnswer.setAlignmentY(Component.CENTER_ALIGNMENT);
 		
 		pnlTypeQ.add(rbMultChoice, BorderLayout.WEST);
 		pnlTypeQ.add(rbShortAnswer, BorderLayout.EAST);
 		pnlTypeQ.add(btnNext, BorderLayout.SOUTH);
 		
 		pnlNewQ.add(txtQuestion);
 		pnlNewQ.add(pnlTypeQ);
 		
 		this.add(pnlNewQ, BorderLayout.NORTH);
 	}
 	
 	/**
 	 * construct the short answer question adding panel
 	 */
 	private void initPnlAddShortAnswer() {
 		pnlNewQ.removeAll();
 		pnlTypeQ.removeAll();
 		
 		pnlNewQ.setLayout(new GridLayout(1, 2, 50, 20));
 		
 		txtAnswer = new JTextField("");
 		txtAnswer.setBorder(BorderFactory.createTitledBorder("Answer"));
 		txtAnswer.setPreferredSize(new Dimension(300, 100));
 		
 		pnlTypeQ.setLayout(new GridLayout(2, 1, 50, 20));
 		pnlTypeQ.add(btnBack);
 		pnlTypeQ.add(btnSubmit);
 		
 		pnlNewQ.add(txtAnswer);
 		pnlNewQ.add(pnlTypeQ);
 
 		this.add(pnlNewQ, BorderLayout.NORTH);
 	}
 	
 	/**
 	 * construct the multiple choice answer adding panel
 	 */
 	private void initPnlAddMultipleAnswer() {
 		pnlNewQ.removeAll();
 		pnlTypeQ.removeAll();
 		pnlMultA.removeAll();
 		
 		pnlNewQ.setLayout(new GridLayout(1, 2, 50, 20));
 		
 		pnlMultA.setLayout(new GridLayout(4, 2));
 		pnlMultA.setBorder(BorderFactory.createTitledBorder("Answers"));
 		
 		for (int i = 0; i < 4; i++) {
 			pnlMultA.add(rbAnsList.get(i));
 			pnlMultA.add(txtAnsList.get(i));
 		}
 		
 		pnlTypeQ.setLayout(new GridLayout(2, 1, 50, 20));
 		pnlTypeQ.add(btnBack);
 		pnlTypeQ.add(btnSubmit);
 		
 		pnlNewQ.add(pnlMultA);
 		pnlNewQ.add(pnlTypeQ);
 
 		this.add(pnlNewQ, BorderLayout.NORTH);
 	}
 	
 	/**
 	 * construct the questing LISTING panel
 	 */
 	private void initPnlQuestionListing() {		
 		pnlListQ.setLayout(new BorderLayout());
 		pnlListWeeks.setPreferredSize(new Dimension(640, 200));
 		
 		pnlListQ.setBorder(BorderFactory.createLineBorder(Color.BLACK));
 		
 		txtQuestionList = new JTextArea("");
 		txtQuestionList.setBorder(null);
 		txtQuestionList.setEditable(false);
 		
 		pnlViewWeek.add(lblViewWeek);
 		pnlViewWeek.add(spnWeek);
 		pnlViewWeek.add(lblViewQuestion);
 		pnlViewWeek.add(spnQuestion);
 		pnlViewWeek.add(btnModify);
 		
 		pnlListWeeks.add(txtQuestionList);
 		
 		pnlListQ.add(pnlViewWeek, BorderLayout.NORTH);
 		pnlListQ.add(pnlListWeeks, BorderLayout.CENTER);
 		
 		this.add(pnlListQ, BorderLayout.SOUTH);
 	}
 	
 	/**
 	 * initialise the bonus panel if bonus questions already exist
 	 */
 	private void initExistingBonus() {
 		currentWeek = GameData.getCurrentGame().getCurrentWeek();
 		currentQuestionNumber = 1;
 		
 		try {
 			bq = Bonus.getQuestionByWeekAndNumber(currentWeek, currentQuestionNumber);
 		} catch (IndexOutOfBoundsException e) {
 			bq = null;
 		}
 		
 		setWeekSpinner(currentWeek, GameData.getCurrentGame().getCurrentWeek());
 		setQuestionSpinner(currentQuestionNumber, Bonus.getNumQuestionsInWeek(currentWeek));
 		addQuestionToListing(bq);
 	}
 	
 	/**
 	 * add the indicated bonus question to the LISTING panel 
 	 * 		basically, turn the bonus question into readable text
 	 * @param q
 	 */
 	private void addQuestionToListing(BonusQuestion q) {
 		if (q != null){
 			currentQuestion = 
 				"Week: " + "\t\t" + q.getWeek() + "\n" + 
 				"Question #: " + "\t\t" + q.getNumber() + "\n" +
 				"Question Type: " + "\t\t" + q.getBonusType() + "\n" + 
 				"Question: " + "\t\t" + q.getPrompt() + "\n" + 
 				"Answer: " + "\t\t" + q.getAnswer() + "\n\n";
 		} else {
 			currentQuestion = "";
 		}
 		txtQuestionList.setText(currentQuestion);
 	}
 	
 	/**
 	 * set the value, and maximum value of the WEEK spinner
 	 * @param wValue
 	 * @param wMax
 	 */
 	private void setWeekSpinner(int wValue, int wMax) {
 		spnWeek.removeChangeListener(clWeek);
 		weekModel.setValue(wValue);
 		weekModel.setMaximum(wMax);
 		spnWeek.addChangeListener(clWeek);
<<<<<<< HEAD:src/admin/BonusPanel.java
=======
		//spnWeek.validate();
>>>>>>> 2bb3c919361f28b67c70ec04cc49536f79ed23f8:src/admin/panel/bonus/BonusPanel.java
 	}
 	
 	/**
 	 * set the value and maximum value of the QUESTION spinner
 	 * @param qValue
 	 * @param qMax
 	 */
 	private void setQuestionSpinner(int qValue, int qMax) {
 		spnQuestion.removeChangeListener(clQuestion);
 		snmQuestion.setValue(qValue);
 		snmQuestion.setMaximum(qMax);
 		spnQuestion.addChangeListener(clQuestion);
<<<<<<< HEAD:src/admin/BonusPanel.java
	}
	
	/**
	 * sets components in the questing adding panel to UNEDITABLE
	 */
	private void setQuestionAddingPanelUneditable(){
		txtQuestion.setEnabled(false);
		rbMultChoice.setEnabled(false);
		rbShortAnswer.setEnabled(false);
		btnNext.setEnabled(false);
=======
		//spnQuestion.validate();
>>>>>>> 2bb3c919361f28b67c70ec04cc49536f79ed23f8:src/admin/panel/bonus/BonusPanel.java
 	}
 	
 	/**
 	 * sets components in the questing adding panel to edit
 	 * @param Whether editable or not (enabled).
 	 */
 	private void setQuestionAddingPanelEditable(boolean edit) {
 		txtQuestion.setEnabled(edit);
 		rbMultChoice.setEnabled(edit);
 		rbShortAnswer.setEnabled(edit);
 		btnNext.setEnabled(edit);
 	}
 	
 	/**
 	 * loads the question adding panel with the current bonus question
 	 */
 	private void setQuestionAddingPanel(){
 		setQuestionAddingPanelEditable(true);
 		txtQuestion.setText(bq.getPrompt());
 		modifyBonusQuestion = true;
 	}
 	
 	/**
 	 * loads the answer adding panel with the current bonus question
 	 */
 	private void setAnswerAddingPanel(){
 		String[] choices = bq.getChoices();
 		if (rbMultChoice.isSelected() && choices != null){
 			for (int i = 0; i < 4; i++) 
 				txtAnsList.get(i).setText(choices[i]);
 			
 		} else {//if (rbShortAnswer.isSelected()) { // one must be selected?
 			txtAnswer.setText(bq.getAnswer());
 		}
 	}
 	
 	/**
 	 * returns the correct answer of a multiple choice question, 
 	 * 		indicated by the selected radio button
 	 * @return
 	 */
 	private String getMultiAnswer(){
 		for (int i = 0; i < 4; i++) {
 			if (rbAnsList.get(i).isSelected()) {
 				return txtAnsList.get(i).getText();
 			}
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * returns a string array of the multiple choice answers
 	 * @return answers
 	 */
 	private String[] getMultiAnswerArray(){
 		String[] answers = new String[4];
 		for (int i = 0; i < 4; i++) {
 			JTextField tf = txtAnsList.get(i);
 			if (tf.getText().length() > 0) {
 				answers[i] = tf.getText();
 			}
 		}
 		
 		return answers;
 	}
 	
 	/**
 	 * checks if all multiple choice answers are 1-200 characters 
 	 * 		AND if at least one answer exists
 	 * @return
 	 */
 	// returns whether all four are valid as of now.. 
 	private Boolean getValidMultiAnswers() {
 		boolean res = true;
 		for (int i=0; i < 4 && res; i++) {
 			String s = txtAnsList.get(i).getText();
 			// checks each tf is (1,200)
 			res = res && (s.length() > 0 && s.length() <= 200); 
 		}
 		
 		return res;
 		/*return (txtAnswerA.getText().length() > 0 && txtAnswerA.getText().length() < 201)
 		|| (txtAnswerB.getText().length() > 0 && txtAnswerB.getText().length() < 201)
 		|| (txtAnswerC.getText().length() > 0 && txtAnswerC.getText().length() < 201)
 		|| (txtAnswerD.getText().length() > 0 && txtAnswerD.getText().length() < 201)
 		&& txtAnswerA.getText().length() < 201 && txtAnswerB.getText().length() < 201	 // if you break down the logic here, 	
 		&& txtAnswerC.getText().length() < 201 && txtAnswerD.getText().length() < 201;*/ // these are already checked in the or's
 	}
 	
 	/**
 	 * checks if a textfield is 1-200 characters
 	 * @param t
 	 * @return true if text is 1-200 char
 	 */
 	private Boolean getValidQuestionOrAnswer(JTextField t){
 		return (t.getText().length() > 0 && t.getText().length() < 201);
 	}
 	
 	/**
 	 * adds a new short answer question:
 	 * 		sets Question spinner,
 	 * 		adds bonus question to backend,
 	 * 		resets question adding panel,
 	 * 		adds question to the gui LIST
 	 */
 	private void addNewShortAnswer() {
 		currentWeek = GameData.getCurrentGame().getCurrentWeek();
 		currentQuestionNumber = Bonus.getNumQuestionsInWeek(currentWeek) + 1;				
 		answer = txtAnswer.getText();						
 		bq = new BonusQuestion(question, answer, null, 
 				GameData.getCurrentGame().getCurrentWeek(), currentQuestionNumber);
 		initPnlAddQuestion();
 		addQuestionToListing(bq);
 		setWeekSpinner(currentWeek, currentWeek);
 		setQuestionSpinner(currentQuestionNumber, currentQuestionNumber);
 		btnModify.setEnabled(true);
 		modifyBonusQuestion = false;
 	}
 	
 	/**
 	 * adds a new multiple choice question:
 	 * 		sets the question spinner,
 	 * 		adds the bonus question to backend,
 	 * 		sets gui fields
 	 * @param answer: the correct answer
 	 * @param answers: the list of possible answers
 	 */
 	private void addNewMultipleChoice(String answer, String[] answers){
 		currentWeek = GameData.getCurrentGame().getCurrentWeek();
 		currentQuestionNumber = Bonus.getNumQuestionsInWeek(currentWeek) + 1;
 		bq = new BonusQuestion(question, answer, answers, 
 				GameData.getCurrentGame().getCurrentWeek(), currentQuestionNumber);
 		initPnlAddQuestion();
 		addQuestionToListing(bq);
 		setWeekSpinner(currentWeek, currentWeek);
 		setQuestionSpinner(currentQuestionNumber, currentQuestionNumber);
 		btnModify.setEnabled(true);
 		modifyBonusQuestion = false;
 	}
 
 	/**
 	 * modifies the current short answer question & 
 	 * 		sets appropriate gui fields.
 	 */
 	private void modifyShortAnswer(){
 		bq.setAnswer(txtAnswer.getText());
 		bq.setChoices(null);
 		bq.setBonusType(BONUS_TYPE.SHORT);
 		initPnlAddQuestion();
 		addQuestionToListing(bq);
 		modifyBonusQuestion = false;
 	}
 	
 	/**
 	 * modifies the current multiple choice bonus question &
 	 * 		resets appropriate gui fields.
 	 * @param answer
 	 * @param answers
 	 */
 	private void modifyMultipleChoice(String answer, String[] answers){
 		bq.setAnswer(answer);
 		bq.setChoices(answers);
 		bq.setBonusType(BONUS_TYPE.MULTI);
 		initPnlAddQuestion();
 		addQuestionToListing(bq);
 		modifyBonusQuestion = false;
 	}
 	
 	/**
 	 * initialises all of the listeners
 	 */
 	private void initListeners(){
 		
 		btnNext.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent ae) {
 				if (getValidQuestionOrAnswer(txtQuestion)){
 					
 					question = txtQuestion.getText();
 					
 					if (rbShortAnswer.isSelected()){
 						
 						shortAns = true;
 						initPnlAddShortAnswer();	
 						if (modifyBonusQuestion) setAnswerAddingPanel();
 						
 					} else if (rbMultChoice.isSelected()){
 						
 						shortAns = false;
 						initPnlAddMultipleAnswer();
 						if (modifyBonusQuestion) setAnswerAddingPanel();
 						
 					} else {
 						MainFrame.getRunningFrame().setStatusErrorMsg(
 								"You must select a question type."
 										+ " (question type unselected)", rbShortAnswer, rbMultChoice);
 					}
 				} else {
 					MainFrame.getRunningFrame().setStatusErrorMsg(
 							"Questions must be 1-200 characters."
 									+ " (invalid question)", txtQuestion);
 				}
 			}		
 		});
 		
 		btnSubmit.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent ae) {
 				// enter if we are modifying a question
 				if (modifyBonusQuestion){
 					bq.setPrompt(txtQuestion.getText());
 					
 					// is it a short answer?
 					if (shortAns){
 						if (getValidQuestionOrAnswer(txtAnswer)){
 							
 							modifyShortAnswer();
 							
 						} else {
 							MainFrame.getRunningFrame().setStatusErrorMsg(
 									"Your answer must be 1-200 characters."
 											+ " (invalid answer)", txtAnswer);
 						}
 						// otherwise it is a multiple choice question
 					} else {
 						if (getValidMultiAnswers()){
 							
 							String[] answers = getMultiAnswerArray();	
 							String a = getMultiAnswer();
 							
 							if (a != null){
 								
 								modifyMultipleChoice(a, answers);
 							} else {
 								MainFrame.getRunningFrame().setStatusErrorMsg(
 										"You must select one correct answer."
 												+ " (correct answer unselected)", 
 												rbAnsList.toArray(new Component[0]));
 								return;
 							}													
 						} else {
 							MainFrame.getRunningFrame().setStatusErrorMsg(
 									"Your must write atleast one answer. Answers must be 1-200 characters."
 											+ " (invalid answers)", pnlMultA);
 							return;
 						}
 					}
 					//otherwise, we are adding a NEW bonus question
 				} else if (shortAns){
 					if (getValidQuestionOrAnswer(txtAnswer)){
 						
 						addNewShortAnswer();
 						
 					} else {
 						MainFrame.getRunningFrame().setStatusErrorMsg(
 								"Your answer must be 1-200 characters."
 										+ " (invalid answer)", txtAnswer);
 					}
 					// otherwise, we are adding a NEW MULTIPLE CHOICE
 				} else {
 					if (getValidMultiAnswers()){
 				
 						String[] answers = getMultiAnswerArray();						
 						String a = getMultiAnswer();
 						
 						if (a != null){
 							addNewMultipleChoice(a, answers);
 						} else {
 							MainFrame.getRunningFrame().setStatusErrorMsg(
 									"You must select one correct answer."
 											+ " (correct answer unselected)", rbAnswerA, rbAnswerB, rbAnswerC, rbAnswerD);
 						}
 					} else {
 						MainFrame.getRunningFrame().setStatusErrorMsg(
 								"Your must write atleast one answer. Answers must be 1-200 characters."
 										+ " (invalid answers)", pnlMultA);
 					}
 				}				
 			}		
 		});
 		
 		btnBack.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent ae) {
 				initPnlAddQuestion();
 				if (modifyBonusQuestion) setQuestionAddingPanel();
 			}
 		});
 		
 		btnModify.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent ae) {
 				setQuestionAddingPanel();
 			}			
 		});
 		
 		clWeek = new ChangeListener() {
 
 			@Override
 			public void stateChanged(ChangeEvent ce) {
 				currentWeek = (Integer)spnWeek.getValue();
 				currentQuestionNumber = 1;
 
 				setQuestionSpinner(currentQuestionNumber, Bonus.getNumQuestionsInWeek(currentWeek));
 				
 				try {
 					bq = Bonus.getQuestionByWeekAndNumber(currentWeek, currentQuestionNumber);
 				} catch (IndexOutOfBoundsException e){
 					bq = null;
 				}
 				
 				addQuestionToListing(bq);
 				
 				if (currentWeek == GameData.getCurrentGame().getCurrentWeek())
 					btnModify.setEnabled(true);
 				else btnModify.setEnabled(false);
 			}			
 		};
 		
 		spnWeek.addChangeListener(clWeek);
 		
 		clQuestion = new ChangeListener() {
 
 			@Override
 			public void stateChanged(ChangeEvent ce) {
 				currentQuestionNumber = (Integer)spnQuestion.getValue();
 				
 				try {
 					bq = Bonus.getQuestionByWeekAndNumber(currentWeek, currentQuestionNumber);
 				} catch (IndexOutOfBoundsException e) {
 					bq = null;
 				}
 				
 				addQuestionToListing(bq);
 			}
 		};
 		
 		spnQuestion.addChangeListener(clQuestion);
 	}
 
 	@Override
 	public void update(Observable observ, Object obj) {
 		GameData g = (GameData)observ;
 		
 		if (obj.equals(Field.START_SEASON)){
 			setQuestionAddingPanelEditable(true);
 			currentWeek = g.getCurrentWeek();
 			currentQuestionNumber = 1;
 			setWeekSpinner(currentWeek, g.getCurrentWeek());
 			setQuestionSpinner(currentQuestionNumber, Bonus.getNumQuestionsInWeek(currentWeek));
 		}
 		if (obj.equals(Field.ADVANCE_WEEK)){
 			currentWeek = g.getCurrentWeek();
 			currentQuestionNumber = 1;
 			setWeekSpinner(currentWeek, g.getCurrentWeek());
 			setQuestionSpinner(1, 1);
 			txtQuestionList.setText("");
 		}
 	}
 }
