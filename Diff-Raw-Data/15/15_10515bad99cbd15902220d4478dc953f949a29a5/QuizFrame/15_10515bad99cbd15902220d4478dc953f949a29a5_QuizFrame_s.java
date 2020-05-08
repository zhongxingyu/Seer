 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.*;
 import javax.swing.event.*;
 
 public class QuizFrame extends JFrame implements ActionListener 
 {
 	public static int WIDTH = 600;
 	public static int HEIGHT = 400;
 
 	private final Class currClass;
 	private final User curr;
 	private final String title;
 	private final int size;
 	private final Quiz quiz;
 	private int i;
 
 	private JTextField questionTxt;
 	private JButton cancelBtn;
 	private JButton submitBtn;
 	private JRadioButton radio1;
 	private JRadioButton radio2;
 	private JRadioButton radio3;
 	private JRadioButton radio4;
 	private JTextField answer1;
 	private JTextField answer2;
 	private JTextField answer3;
 	private JTextField answer4;
 	private JLabel qLbl;
 	private JLabel errorLbl;
 
 	public QuizFrame(String _title, int _size, Class _currClass, User _curr)
 	{
		super("Just In Time Teaching");
 		currClass = _currClass;
 		curr = _curr;
 		title = _title;
 		quiz = null;
 		size = _size;
 		i = 1;
 
 		try
 		{
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		}	
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 
 		setSize(WIDTH, HEIGHT);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setResizable(false);
 		setLocationRelativeTo(null);
 
 		JPanel panel = createQuiz();	
 		add(panel);
 	}	
 
 	public QuizFrame(Quiz _quiz, Class _currClass, User _curr)
 	{
		super("Just In Time Teaching");
 		currClass = _currClass;
 		curr = _curr;
 		quiz = _quiz;
 		title = "";
 		size = 4; // Amount of Quiz Questions
 		i = 1;
 
 		try
 		{
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		}	
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 
 		setSize(WIDTH, HEIGHT);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setLocationRelativeTo(null);
 
 		add(takeQuiz());
 	}
 
 	public JPanel createQuiz()
 	{
 		JPanel panel = new JPanel();
 		SpringLayout layout = new SpringLayout();
 		panel.setLayout(layout);
 		
 		int x = 25;
 		int y = 25;
 		int inc = 45;
 
 		JLabel titleLbl = new JLabel(title + " Creation Page");
 		panel.add(titleLbl);
 
 		JLabel questionLbl = new JLabel("Enter the Question:");
 		panel.add(questionLbl);
 
 		questionTxt = new JTextField("", 20);
 		panel.add(questionTxt);
 
 		radio1 = new JRadioButton();
 		radio1.addActionListener(this);
 		panel.add(radio1);
 
 		radio2 = new JRadioButton();
 		radio2.addActionListener(this);
 		panel.add(radio2);
 
 		radio3 = new JRadioButton();
 		radio3.addActionListener(this);
 		panel.add(radio3);
 
 		radio4 = new JRadioButton();	
 		radio4.addActionListener(this);
 		panel.add(radio4);
 
 		ButtonGroup group = new ButtonGroup();
 		group.add(radio1);
 		group.add(radio2);
 		group.add(radio3);
 		group.add(radio4);
 
 		answer1 = new JTextField("", 15);
 		panel.add(answer1);
 
 		answer2 = new JTextField("", 15);
 		panel.add(answer2);
 
 		answer3 = new JTextField("", 15);
 		panel.add(answer3);
 
 		answer4 = new JTextField("", 15);
 		panel.add(answer4);
 
 		errorLbl = new JLabel("");
 		panel.add(errorLbl);
 
 		submitBtn = new JButton("Continue");
 		submitBtn.addActionListener(this);
 		panel.add(submitBtn);
 
 		if (i == size)
 		{
 			submitBtn.setText("Submit");
 		}
 
 		cancelBtn = new JButton("Cancel");
 		cancelBtn.addActionListener(this);
 		panel.add(cancelBtn);
 
 		layout.putConstraint(SpringLayout.WEST, titleLbl, ((WIDTH/2)-100), SpringLayout.WEST,
 			panel);
 		layout.putConstraint(SpringLayout.NORTH, titleLbl, y, SpringLayout.NORTH,
 			panel); 
 
 		y += inc;
 
 		layout.putConstraint(SpringLayout.WEST, questionLbl, (((WIDTH/2/2)-100)), SpringLayout.WEST,
 			panel);
 		layout.putConstraint(SpringLayout.NORTH, questionLbl, y, SpringLayout.NORTH,
 			panel);
 		layout.putConstraint(SpringLayout.NORTH, questionTxt, y, SpringLayout.NORTH,
 			panel);
 		layout.putConstraint(SpringLayout.WEST, questionTxt, 20, SpringLayout.EAST,
 			questionLbl);
 
 		y += inc;
 
 		layout.putConstraint(SpringLayout.WEST, radio1, (((WIDTH/2)/2)-50), SpringLayout.WEST,
 			panel);
 		layout.putConstraint(SpringLayout.NORTH, radio1, y, SpringLayout.NORTH,
 			panel);
 		layout.putConstraint(SpringLayout.NORTH, answer1, y, SpringLayout.NORTH,
 			panel);
 		layout.putConstraint(SpringLayout.WEST, answer1, 20, SpringLayout.EAST,
 			radio1);
 
 		y += inc;
 
 
 		layout.putConstraint(SpringLayout.WEST, radio2, (((WIDTH/2)/2)-50), SpringLayout.WEST,
 			panel);
 		layout.putConstraint(SpringLayout.NORTH, radio2, y, SpringLayout.NORTH,
 			panel);
 		layout.putConstraint(SpringLayout.NORTH, answer2, y, SpringLayout.NORTH,
 			panel);
 		layout.putConstraint(SpringLayout.WEST, answer2, 20, SpringLayout.EAST,
 			radio2);
 
 		y += inc;
 
 		layout.putConstraint(SpringLayout.WEST, radio3, (((WIDTH/2)/2)-50), SpringLayout.WEST,
 			panel);
 		layout.putConstraint(SpringLayout.NORTH, radio3, y, SpringLayout.NORTH,
 			panel);
 		layout.putConstraint(SpringLayout.NORTH, answer3, y, SpringLayout.NORTH,
 			panel);
 		layout.putConstraint(SpringLayout.WEST, answer3, 20, SpringLayout.EAST,
 			radio3);
 
 		y += inc;
 	
 		layout.putConstraint(SpringLayout.WEST, radio4, (((WIDTH/2)/2)-50), SpringLayout.WEST,
 			panel);
 		layout.putConstraint(SpringLayout.NORTH, radio4, y, SpringLayout.NORTH,
 			panel);
 		layout.putConstraint(SpringLayout.NORTH, answer4, y, SpringLayout.NORTH,
 			panel);
 		layout.putConstraint(SpringLayout.WEST, answer4, 20, SpringLayout.EAST,
 			radio4);
 
 		y += inc;
 
 		layout.putConstraint(SpringLayout.WEST, errorLbl, ((WIDTH/2)-50), SpringLayout.WEST,
 			panel);
 		layout.putConstraint(SpringLayout.NORTH, errorLbl, y, SpringLayout.NORTH,
 			panel);
 
 		layout.putConstraint(SpringLayout.NORTH ,cancelBtn, (HEIGHT-70), SpringLayout.NORTH, 
 			panel);
 		layout.putConstraint(SpringLayout.EAST, cancelBtn, -5, SpringLayout.WEST, 
 			submitBtn);
 		layout.putConstraint(SpringLayout.WEST, submitBtn, (WIDTH-100), SpringLayout.WEST, 
 			panel);
 		layout.putConstraint(SpringLayout.NORTH, submitBtn, (HEIGHT-70), SpringLayout.NORTH, 
 			panel);
 
 		return panel;
 	}
 
 	public JPanel takeQuiz()
 	{
 		JPanel panel = new JPanel();
 		SpringLayout layout = new SpringLayout();
 		panel.setLayout(layout);
 
 		int x = 25;
 		int y = 25;
 		int inc = 45;
 
 		JLabel titleLbl = new JLabel("Quiz Title");//quiz.getTitle()
 		panel.add(titleLbl);
 	
 		qLbl = new JLabel("Quesion");
 		panel.add(qLbl);	
 
 		radio1 = new JRadioButton();
 		radio1.addActionListener(this);
 		panel.add(radio1);
 
 		radio2 = new JRadioButton();
 		radio2.addActionListener(this);
 		panel.add(radio2);
 
 		radio3 = new JRadioButton();
 		radio3.addActionListener(this);
 		panel.add(radio3);
 
 		radio4 = new JRadioButton();	
 		radio4.addActionListener(this);
 		panel.add(radio4);
 
 		ButtonGroup group = new ButtonGroup();
 		group.add(radio1);
 		group.add(radio2);
 		group.add(radio3);
 		group.add(radio4);	
 
 		errorLbl = new JLabel("");
 		panel.add(errorLbl);
 
 		submitBtn = new JButton("Continue");
 		submitBtn.addActionListener(this);
 		panel.add(submitBtn);
 
 		if (i == size)
 		{
 			submitBtn.setText("Submit");
 		}
 
 		cancelBtn = new JButton("Cancel");
 		cancelBtn.addActionListener(this);
 		panel.add(cancelBtn);
 
 
 
 		question();
 
 		layout.putConstraint(SpringLayout.WEST, titleLbl, ((WIDTH/2)-100), SpringLayout.WEST,
 			panel);
 		layout.putConstraint(SpringLayout.NORTH, titleLbl, y, SpringLayout.NORTH,
 			panel); 
 
 		y += inc;
 
 		layout.putConstraint(SpringLayout.WEST, qLbl, (((WIDTH/2))-50), SpringLayout.WEST,
 			panel);
 		layout.putConstraint(SpringLayout.NORTH, qLbl, y, SpringLayout.NORTH,
 			panel);
 
 		y += inc;
 
 		layout.putConstraint(SpringLayout.WEST, radio1, (((WIDTH/2)/2)-50), SpringLayout.WEST,
 			panel);
 		layout.putConstraint(SpringLayout.NORTH, radio1, y, SpringLayout.NORTH,
 			panel);
 
 		y += inc;
 
 
 		layout.putConstraint(SpringLayout.WEST, radio2, (((WIDTH/2)/2)-50), SpringLayout.WEST,
 			panel);
 		layout.putConstraint(SpringLayout.NORTH, radio2, y, SpringLayout.NORTH,
 			panel);
 
 		y += inc;
 
 		layout.putConstraint(SpringLayout.WEST, radio3, (((WIDTH/2)/2)-50), SpringLayout.WEST,
 			panel);
 		layout.putConstraint(SpringLayout.NORTH, radio3, y, SpringLayout.NORTH,
 			panel);
 
 		y += inc;
 	
 		layout.putConstraint(SpringLayout.WEST, radio4, (((WIDTH/2)/2)-50), SpringLayout.WEST,
 			panel);
 		layout.putConstraint(SpringLayout.NORTH, radio4, y, SpringLayout.NORTH,
 			panel);
 
 		y += inc;
 
 		layout.putConstraint(SpringLayout.WEST, errorLbl, ((WIDTH/2)-50), SpringLayout.WEST,
 			panel);
 		layout.putConstraint(SpringLayout.NORTH, errorLbl, y, SpringLayout.NORTH,
 			panel);
 
 		layout.putConstraint(SpringLayout.NORTH ,cancelBtn, (HEIGHT-70), SpringLayout.NORTH, 
 			panel);
 		layout.putConstraint(SpringLayout.EAST, cancelBtn, -5, SpringLayout.WEST, 
 			submitBtn);
 		layout.putConstraint(SpringLayout.WEST, submitBtn, (WIDTH-100), SpringLayout.WEST, 
 			panel);
 		layout.putConstraint(SpringLayout.NORTH, submitBtn, (HEIGHT-70), SpringLayout.NORTH, 
 			panel);
 
 		return panel;
 	}
 
 	public void question()
 	{// DB
 		qLbl.setText("Quesion " + i);
 
 		radio1.setText("Answer 1");
 
 		radio2.setText("Answer 2");
 
 		radio3.setText("Answer 3");
 
 		radio4.setText("Answer 4");
 	}
 
 	public void actionPerformed(ActionEvent event)
 	{
 		Object source = event.getSource();
 
 
 		if (source == radio1)
 			System.out.println("Radio1");
 		else if (source == radio2)
 			System.out.println("Radio2");
 		else if (source == radio3)
 			System.out.println("Radio3");
 		else if (source == radio4)
 			System.out.println("Radio4");
 		else if (source == cancelBtn)
 		{
 			dispose();
 			Menu gui = new Menu(currClass, curr);
 			gui.setVisible(true);
 		}
 		else if (source == submitBtn)
 		{
 			if (quiz == null)
 			{
 				String question = questionTxt.getText();
 				String ans1 = answer1.getText();
 				String ans2 = answer2.getText();
 				String ans3 = answer3.getText();
 				String ans4 = answer4.getText();
 
 				// add to array n such
 			}
 			else
 			{
 				// DB
 				String ans1 = radio1.getText();
 				String ans2 = radio2.getText();
 				String ans3 = radio3.getText();
 				String ans4 = radio4.getText();
 			}
 
 			if (event.getActionCommand().equals("Submit"))
 			{
 				dispose();
 				Menu gui = new Menu(currClass, curr);
 				gui.setVisible(true);
 			}
 
 			i++;
 
 			if (i == size)
 				submitBtn.setText("Submit");
 			// DB
 			if (quiz != null)
 				question();			
 		}
 		else
 			System.out.println("Something Unexpected happened");
 	}
 }
