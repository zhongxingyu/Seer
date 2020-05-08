 package edu.desktop.server.ui;
 
 import java.awt.BorderLayout;
 import java.net.InetAddress;
 import java.util.Map;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.TreeMap;
 
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 
 public class GClicker extends JFrame implements Observer, TimerListener
 {
 	private AlignedPanel answerPanel, clickerPanel;
 	private AnswerField answers[];
 	private ClickerBox clickerBox[];
 	private GClickerModel model;
 	private GClickerStats stats;
 	private int ipAddress[];
 	private InetAddress myAddress;
 	private JPanel content, ipPanel, questionPanel, clickerRows[];
 	private JScrollPane questionPane;
 	private JTextArea question;
 	private VisibleTimer timer;
 
 	public GClicker() throws Exception
 	{
 		super("gClicker");
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setLocation(30, 30);
 		model = new GClickerModel();
 
 		myAddress = InetAddress.getLocalHost();
 		ipAddress = new int[4];
 		for (int i = 0; i < 4; i++)
 			ipAddress[i] = (0x000000FF & myAddress.getAddress()[i]);
 
 		ipPanel = new JPanel();
 		ipPanel.setBorder(BorderFactory.createTitledBorder("IP Address:"));
 		JLabel ipInfo = new JLabel("This server's IP address is: " + ipAddress[0]+"."+
 				ipAddress[1]+"."+ipAddress[2]+"."+ipAddress[3], JLabel.CENTER);
 		ipInfo.setFont(GClickerModel.bigFont);
 		ipPanel.add(ipInfo);
 
 		question = new JTextArea("Enter question here...", 13, 25);
 		question.setFont(GClickerModel.smallerFont);
 		question.setLineWrap(true);
 		questionPane = new JScrollPane(question);
 		questionPanel = new JPanel();
 		questionPanel.setBorder(BorderFactory.createTitledBorder("Question:"));
 		questionPanel.add(questionPane);
 
 		answerPanel = new AlignedPanel("RIGHT");
 		answerPanel.setBorder(BorderFactory.createTitledBorder("Answers:"));
 		answers = new AnswerField[GClickerModel.MAX_ANSWERS];
 		for (int i = 0; i < GClickerModel.MAX_ANSWERS; i++){
 			answers[i] = new AnswerField((char)('A' + i));
 			answerPanel.add(answers[i]);
 			answerPanel.add(Box.createVerticalStrut(5));
 		}
 
 		timer = new VisibleTimer(30);
 		timer.addListener(this);
 
 		AlignedPanel eastPanel = new AlignedPanel("LEFT");
 		eastPanel.add(answerPanel);
 		eastPanel.add(timer);
 
 		int numClickRows = GClickerModel.NUM_CLICK_POSS / GClickerModel.NUM_CLICK_COLS;
 		if ((GClickerModel.NUM_CLICK_POSS % GClickerModel.NUM_CLICK_COLS) != 0){
 			numClickRows++;
 		}
 
 
 		clickerRows = new JPanel[numClickRows];
 		for (int i = 0; i < numClickRows; i++)
 		{
 			clickerRows[i] = new JPanel();
 			clickerRows[i].setLayout(new BoxLayout(clickerRows[i], BoxLayout.X_AXIS));
 		}
 
 		clickerBox = new ClickerBox[GClickerModel.NUM_CLICK_POSS];
 		for (int numAdded = 0; numAdded < clickerBox.length; numAdded++)
 		{
 			clickerBox[numAdded] = new ClickerBox(numAdded);
 			clickerRows[numAdded / GClickerModel.NUM_CLICK_COLS].add(Box.createHorizontalStrut(1));
 			clickerRows[numAdded / GClickerModel.NUM_CLICK_COLS].add(clickerBox[numAdded]);
 			clickerRows[numAdded / GClickerModel.NUM_CLICK_COLS].add(Box.createHorizontalStrut(1));
 		}
 
 		clickerPanel = new AlignedPanel("CENTER");
 		clickerPanel.setBorder(BorderFactory.createTitledBorder("Clickers:"));
 		for (int i = 0; i < clickerRows.length; i++)
 		{
 			clickerPanel.add(clickerRows[i]);
 			clickerPanel.add(Box.createVerticalStrut(2));
 		}
 
 		content = new JPanel(new BorderLayout());
 		content.add(ipPanel, BorderLayout.NORTH);
 		content.add(questionPanel, BorderLayout.WEST);
 		content.add(eastPanel, BorderLayout.CENTER);
 		content.add(clickerPanel, BorderLayout.SOUTH);
 		getContentPane().add(content);
 		pack();
 		setVisible(true);
 		model.addObserver(this);
 	}
 
 	private void enableEdits(boolean enable)
 	{
 		question.setEditable(enable);
 
 		for (int i = 0; i < answers.length; i++)
 			answers[i].answer.setEditable(enable);
 	}
 
 	public void stateChanged(String s)
 	{
 		if (s.equals(VisibleTimer.START))
 		{
 			enableEdits(false);
 
 			String answerList[] = new String[answers.length];
 			for (int i = 0; i < answerList.length; i++)
 				answerList[i] = answers[i].getAnswer();
 
 			model.broadcastQuestion(question.getText(), answerList);
 
 			if (!timer.isActive())
 				model.resetClickers();
 		}
 		else if (s.equals(VisibleTimer.PAUSE) || s.equals(VisibleTimer.CANCEL))
 			enableEdits(true);
 		else if (s.equals(VisibleTimer.SUBMIT) || s.equals(VisibleTimer.EXPIRED))
 		{
 			stats = new GClickerStats(this, model);
 			model.resetClickers();
 			enableEdits(true);
 		}
 	}
 
 	public void update(Observable obs, Object obj){
 		GPerson[] clients = model.getPeople();
 		for(GPerson person : clients){
			
			if (person != null && person.isNewAnswer()){
				clickerBox[person.getClickerID()].changeAnswer(person.getAnswer());
 			}
 
 		}
 	}
 
 	public AnswerField[] getAnswers() {
 		//only return answers that have stuff written in their boxes
				int answer_count = 0;
 		for(int i = 0; i < answers.length; i++){
 			if(answers[i].getAnswer() != null && answers[i].getAnswer().trim().equals("") != true){
 				answer_count++;
 			}
 		}
 
 		AnswerField[] valid_answers = new AnswerField[answer_count];
 
 		//assumes answers are contiguous.
 		for(int i = 0; i < answer_count; i++){
 			valid_answers[i] = answers[i];
 		}
 		return valid_answers; 
 	}
 	public JComponent getRelativeComponent() { return answerPanel; }
 
 	public class AnswerField extends JPanel
 	{
 		public JTextField answer;
 		private char choice;
 
 		private AnswerField(char c)
 		{
 			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
 			choice = c;
 
 			answer = new JTextField(20);
 			answer.setFont(GClickerModel.bigFont);
 
 			add(new JLabel(choice + ": "));
 			add(answer);
 		}
 
 		public char getChoice() { return choice; }
 		public String getAnswer() { return answer.getText(); }
 	}
 
 	public static void main(String args[])
 	{
 		try
 		{
 			new GClicker();
 		} catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 }
