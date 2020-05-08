 package admin;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
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
 
 import data.GameData;
 import data.bonus.BonusQuestion;
 
 public class BonusPanel extends JPanel {
 
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
 	
 	JButton btnBack = new JButton("Back");
 	JButton btnSubmit = new JButton("Submit");
 	
 	JButton btnModify = new JButton("Modify");
 	
 	private JTextField txtQuestion;
 	private JTextField txtAnswer;
 	private JTextArea txtQuestionList;
 	
 	private JRadioButton rbMultChoice;
 	private JRadioButton rbShortAnswer;
 	
 	private ButtonGroup group;
 	private ButtonGroup group2;
 	
 	private String question;
 	private String answer;
 	
 	private JTextField txtAnswerA;
 	private JTextField txtAnswerB;
 	private JTextField txtAnswerC;
 	private JTextField txtAnswerD;
 	
 	private JRadioButton rbAnswerA;
 	private JRadioButton rbAnswerB;
 	private JRadioButton rbAnswerC;
 	private JRadioButton rbAnswerD;
 	
 	private Boolean shortAns;
 	private Boolean modifyBonusQuestion = false;
 	
 	private String[] questionList;
 	
 	private BonusQuestion bq;
 	
 	public BonusPanel() {
 		this.setLayout(new BorderLayout());
 		questionList = new String[GameData.getCurrentGame().weeksLeft()];
 		initPnlAddQuestion();
 		initPnlQuestionListing();
 		initListeners();
 	}
 
 	private void initPnlAddQuestion() {
 		pnlNewQ.removeAll();
 		pnlTypeQ.removeAll();
 		
 		pnlNewQ.setLayout(new GridLayout(1, 4, 50, 20));
 		pnlTypeQ.setLayout(new BorderLayout());
 		
 		txtQuestion = new JTextField("");
 		txtQuestion.setBorder(BorderFactory.createTitledBorder("Question"));
 		txtQuestion.setPreferredSize(new Dimension(300, 100));
 		
 		group = new ButtonGroup();
 		group.add(rbMultChoice);
 		group.add(rbShortAnswer);
 		
 		rbMultChoice = new JRadioButton("Multiple Choice");
 		rbShortAnswer = new JRadioButton("Short Answer");
 		
 		rbMultChoice.setAlignmentY(Component.CENTER_ALIGNMENT);
 		rbShortAnswer.setAlignmentY(Component.CENTER_ALIGNMENT);
 		pnlTypeQ.add(rbMultChoice, BorderLayout.WEST);
 		pnlTypeQ.add(rbShortAnswer, BorderLayout.EAST);
 		pnlTypeQ.add(btnNext, BorderLayout.SOUTH);
 		
 		pnlNewQ.add(txtQuestion);
 		pnlNewQ.add(pnlTypeQ);
 
 		rbAnswerA = new JRadioButton();
 		rbAnswerB = new JRadioButton();
 		rbAnswerC = new JRadioButton();
 		rbAnswerD = new JRadioButton();
 		
 		this.add(pnlNewQ, BorderLayout.NORTH);
 		this.repaint();
 		this.revalidate();
 	}
 	
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
 		this.repaint();
 		this.revalidate();
 	}
 	
 	private void initPnlAddMultipleAnswer() {
 		pnlNewQ.removeAll();
 		pnlTypeQ.removeAll();
 		pnlMultA.removeAll();
 		
 		pnlNewQ.setLayout(new GridLayout(1, 2, 50, 20));
 		
 		pnlMultA.setLayout(new GridLayout(4, 2));
 		pnlMultA.setBorder(BorderFactory.createTitledBorder("Answers"));
 		
 		group2 = new ButtonGroup();
 		rbAnswerA = new JRadioButton("A");
 		rbAnswerB = new JRadioButton("B");
 		rbAnswerC = new JRadioButton("C");
 		rbAnswerD = new JRadioButton("D");
 		group2.add(rbAnswerA);
 		group2.add(rbAnswerB);
 		group2.add(rbAnswerC);
 		group2.add(rbAnswerD);
 		
 		txtAnswerA = new JTextField("");
 		txtAnswerB = new JTextField("");
 		txtAnswerC = new JTextField("");
 		txtAnswerD = new JTextField("");
 		
 		pnlMultA.add(rbAnswerA);
 		pnlMultA.add(txtAnswerA);
 		pnlMultA.add(rbAnswerB);
 		pnlMultA.add(txtAnswerB);
 		pnlMultA.add(rbAnswerC);
 		pnlMultA.add(txtAnswerC);
 		pnlMultA.add(rbAnswerD);
 		pnlMultA.add(txtAnswerD);
 		
 		pnlTypeQ.setLayout(new GridLayout(2, 1, 50, 20));
 		pnlTypeQ.add(btnBack);
 		pnlTypeQ.add(btnSubmit);
 		
 		pnlNewQ.add(pnlMultA);
 		pnlNewQ.add(pnlTypeQ);
 
 		this.add(pnlNewQ, BorderLayout.NORTH);
 		this.repaint();
 		this.revalidate();
 	}
 	
 	private void initPnlQuestionListing() {		
 		pnlListQ.setLayout(new BorderLayout());
 		pnlListWeeks.setPreferredSize(new Dimension(640, 200));
 		
 		pnlListQ.setBorder(BorderFactory.createLineBorder(Color.BLACK));
 		
 		txtQuestionList = new JTextArea("");
 		txtQuestionList.setBorder(null);
 		txtQuestionList.setEditable(false);
 		
 		pnlViewWeek.add(lblViewWeek);
 		pnlViewWeek.add(spnWeek);
 		pnlViewWeek.add(btnModify);
 		
 		pnlListWeeks.add(txtQuestionList);
 		
 		pnlListQ.add(pnlViewWeek, BorderLayout.NORTH);
 		pnlListQ.add(pnlListWeeks, BorderLayout.CENTER);
 		
 		this.add(pnlListQ, BorderLayout.SOUTH);
 	}
 	
 	private void addQuestionToListing(BonusQuestion q) {
 		questionList[GameData.getCurrentGame().getCurrentWeek() - 1] = 
			"Week: " + q.getWeek() + "\t" + "Question: " + q.getPrompt() + 
			"\n" + "\t\t" + "Answer: " + q.getAnswer() + "\n\n";
 		txtQuestionList.setText(questionList[GameData.getCurrentGame().getCurrentWeek() - 1]);
 	}
 	
 	private void setQuestionListingPanel() {
 		txtQuestionList.setText(questionList[(Integer)spnWeek.getValue()]);
 	}
 	
 	private void setQuestionAddingPanelUneditable(){
 		txtQuestion.setEnabled(false);
 		rbMultChoice.setEnabled(false);
 		rbShortAnswer.setEnabled(false);
 		btnNext.setEnabled(false);
 	}
 	
 	private void setQuestionAddingPanelEditable(){
 		txtQuestion.setEnabled(true);
 		rbMultChoice.setEnabled(true);
 		rbShortAnswer.setEnabled(true);
 		btnNext.setEnabled(true);
 	}
 	
 	private void setQuestionAddingPanel(){
 		setQuestionAddingPanelEditable();
 		txtQuestion.setText(bq.getPrompt());
 		modifyBonusQuestion = true;
 	}
 	
 	private void setAnswerAddingPanel(){
 		if (rbMultChoice.isSelected() && bq.getChoices() != null){
 			txtAnswerA.setText(bq.getChoices()[0]);
 			txtAnswerB.setText(bq.getChoices()[1]);
 			txtAnswerC.setText(bq.getChoices()[2]);
 			txtAnswerD.setText(bq.getChoices()[3]);
 		} else if (rbShortAnswer.isSelected() && bq.getChoices() == null){
 			txtAnswer.setText(bq.getAnswer());
 		}
 	}
 	
 	private void initListeners(){
 		
 		btnNext.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent ae) {
 				if (txtQuestion.getText().length() > 0 && txtQuestion.getText().length() < 201){
 					question = txtQuestion.getText();
 					if (rbShortAnswer.isSelected()){
 						shortAns = true;
 						initPnlAddShortAnswer();							
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
 				if (shortAns){
 					if (txtAnswer.getText().length() > 0 && txtAnswer.getText().length() < 201){
 						answer = txtAnswer.getText();
 						bq = new BonusQuestion(question, answer, null, 
 								true, GameData.getCurrentGame().getCurrentWeek());
 						initPnlAddQuestion();
 						addQuestionToListing(bq);
 						setQuestionAddingPanelUneditable();
 						modifyBonusQuestion = false;
 					} else {
 						MainFrame.getRunningFrame().setStatusErrorMsg(
 								"Your answer must be 1-200 characters."
 										+ " (invalid answer)", txtAnswer);
 					}
 				} else {
 					if ((txtAnswerA.getText().length() > 0 && txtAnswerA.getText().length() < 201)
 							|| (txtAnswerB.getText().length() > 0 && txtAnswerB.getText().length() < 201)
 							|| (txtAnswerC.getText().length() > 0 && txtAnswerC.getText().length() < 201)
 							|| (txtAnswerD.getText().length() > 0 && txtAnswerD.getText().length() < 201)
 							&& txtAnswerA.getText().length() < 201 && txtAnswerB.getText().length() < 201
 							&& txtAnswerC.getText().length() < 201 && txtAnswerD.getText().length() < 201){
 						String[] answers = new String[4];
 						if (txtAnswerA.getText().length() > 0) answers[0] = txtAnswerA.getText();
 						if (txtAnswerB.getText().length() > 0) answers[1] = txtAnswerB.getText();
 						if (txtAnswerC.getText().length() > 0) answers[2] = txtAnswerC.getText();
 						if (txtAnswerD.getText().length() > 0) answers[3] = txtAnswerD.getText();
 						if (rbAnswerA.isSelected()){
 							bq = new BonusQuestion(question, txtAnswerA.getText(), answers, 
 									true, GameData.getCurrentGame().getCurrentWeek());
 						} else if (rbAnswerB.isSelected()){
 							bq = new BonusQuestion(question, txtAnswerB.getText(), answers, 
 									true, GameData.getCurrentGame().getCurrentWeek());
 						} else if (rbAnswerC.isSelected()){
 							bq = new BonusQuestion(question, txtAnswerC.getText(), answers, 
 									true, GameData.getCurrentGame().getCurrentWeek());
 						} else if (rbAnswerD.isSelected()){
 							bq = new BonusQuestion(question, txtAnswerD.getText(), answers, 
 									true, GameData.getCurrentGame().getCurrentWeek());
 						} else {
 							MainFrame.getRunningFrame().setStatusErrorMsg(
 									"You must select one correct answer."
 											+ " (correct answer unselected)", rbAnswerA, rbAnswerB, rbAnswerC, rbAnswerD);
 							return;
 						}
 						initPnlAddQuestion();
 						addQuestionToListing(bq);
 						setQuestionAddingPanelUneditable();
 						modifyBonusQuestion = false;
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
 		
 		rbMultChoice.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent ae) {
 				if (rbShortAnswer.isSelected()) rbShortAnswer.setSelected(false);
 			}			
 		});
 		
 		rbShortAnswer.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent ae) {
 				if (rbMultChoice.isSelected()) rbMultChoice.setSelected(false);
 			}			
 		});
 		
 		rbAnswerA.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent ae) {
 				if (rbAnswerB.isSelected()) rbAnswerB.setSelected(false);
 				else if (rbAnswerC.isSelected()) rbAnswerC.setSelected(false);
 				else if (rbAnswerD.isSelected()) rbAnswerD.setSelected(false);
 			}			
 		});
 		
 		rbAnswerB.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent ae) {
 				if (rbAnswerA.isSelected()) rbAnswerA.setSelected(false);
 				else if (rbAnswerC.isSelected()) rbAnswerC.setSelected(false);
 				else if (rbAnswerD.isSelected()) rbAnswerD.setSelected(false);
 			}			
 		});
 		
 		rbAnswerC.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent ae) {
 				if (rbAnswerB.isSelected()) rbAnswerB.setSelected(false);
 				else if (rbAnswerA.isSelected()) rbAnswerA.setSelected(false);
 				else if (rbAnswerD.isSelected()) rbAnswerD.setSelected(false);
 			}			
 		});
 		
 		rbAnswerD.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent ae) {
 				if (rbAnswerB.isSelected()) rbAnswerB.setSelected(false);
 				else if (rbAnswerC.isSelected()) rbAnswerC.setSelected(false);
 				else if (rbAnswerA.isSelected()) rbAnswerA.setSelected(false);
 			}			
 		});
 		
 		btnModify.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent ae) {
 				setQuestionAddingPanel();
 			}			
 		});
 		
 		spnWeek.addChangeListener(new ChangeListener() {
 
 			@Override
 			public void stateChanged(ChangeEvent ce) {
 				setQuestionListingPanel();
 			}
 		});
 	}
 }
