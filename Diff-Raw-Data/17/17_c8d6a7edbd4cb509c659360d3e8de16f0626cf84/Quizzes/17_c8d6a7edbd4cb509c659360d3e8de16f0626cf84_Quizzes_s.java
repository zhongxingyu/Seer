 package gui;
 
 import java.awt.GridLayout;
 
 import javax.swing.BorderFactory;
 import javax.swing.ButtonGroup;
 import javax.swing.ButtonModel;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.WindowConstants;
 
 import java.awt.BorderLayout;
 import javax.swing.JLabel;
 import javax.swing.JPopupMenu;
 import javax.swing.JTextPane;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.SystemColor;
 import java.awt.Font;
 import java.awt.Toolkit;
 
 import javax.swing.JButton;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.awt.event.WindowStateListener;
 import java.awt.event.WindowAdapter;
 
 public class Quizzes extends JFrame {
 	
 	private JRadioButton first;
 	private JRadioButton second;
 	private JRadioButton third;
 	private JRadioButton fourth;
 	private ButtonGroup group;
 	private boolean isDone = false;
 	
 	private JPanel radioPanel;
 	private JFrame jframe;
 	private LessonsTab ltab;
 	
 	private int nCorrect=0;
 	
 	@SuppressWarnings("deprecation")
 	public Quizzes(int Chapter, JFrame frame, LessonsTab tab)
 	{
 		super("Chapter " + Chapter + " Quiz");
 		addWindowListener(new WindowAdapter() {
 			@Override
 			public void windowClosing(WindowEvent e) {
 				setVisible(false);
 		        jframe.enable();
 		        jframe.toFront();
 			}
 			@Override
 			public void windowClosed(WindowEvent e) {
 				setVisible(false);
 		        jframe.enable();
 		        jframe.toFront();
 			}
 		});
 
 		setVisible(true);
 		getContentPane().setForeground(SystemColor.info);
 		getContentPane().setFont(new Font("Segoe UI Semibold", Font.BOLD, 14));
 		getContentPane().setBackground(SystemColor.control);
 		
 		setResizable(false);
 		setSize(488,361);
 		
 		//get size of the screen
 		Toolkit toolkit = Toolkit.getDefaultToolkit();  
 		Dimension screenSize = toolkit.getScreenSize(); 
 				
 		///TO SHOW THE WINDOW IN THE CENTER OF THE SCREEN
 		int x = (screenSize.width - super.getWidth()) / 2;  
 		int y = (screenSize.height - super.getHeight()) / 2;  
 				
 		setLocation(x,y);
 		ltab = tab;
 		jframe = frame;
 		jframe.disable();
 		
 		if(Chapter == 2)
 		{
 			Chapter2();
 		}
 		else if(Chapter == 3)
 		{
 			Chapter3();
 		}
 		else if(Chapter == 1)
 		{
 			Chapter1();
 		}
 		
 	}
 	
 
 	public void Chapter2()
 	{
 		getContentPane().removeAll();
 		getContentPane().repaint();
 		revalidate();
 		
 		radioPanel = new JPanel();
 		radioPanel.setBounds(48, 109, 313, 117);
 		radioPanel.setLayout(new GridLayout(0, 1));
 		
 		//Create the radio buttons.
         first = new JRadioButton("2");
         first.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
         first.setSelected(true);
 
         second = new JRadioButton("22");
         second.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
 
         third = new JRadioButton("Syntax Error");
         third.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
 
         fourth = new JRadioButton("4");
         fourth.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
    	 
         first.setActionCommand("1");
         second.setActionCommand("2");
         third.setActionCommand("3");
         fourth.setActionCommand("4");
         
         //Group the radio buttons.
         group = new ButtonGroup();
         getContentPane().setLayout(null);
         group.add(first);
         group.add(second);
         group.add(third);
         group.add(fourth);
 
         //Put the radio buttons in a column in a panel.
         radioPanel.add(first);
         radioPanel.add(second);
         radioPanel.add(third);
         radioPanel.add(fourth);
 
         getContentPane().add(radioPanel);
         
         JTextPane txtpnQuestion = new JTextPane();
         txtpnQuestion.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 15));
         txtpnQuestion.setForeground(new Color(160, 82, 45));
         txtpnQuestion.setBackground(SystemColor.control);
         txtpnQuestion.setEditable(false);
         txtpnQuestion.setText("Question 1: \r\n\r\nWhat result can we expect from the command (+2  2) within LISP?");
         txtpnQuestion.setBounds(12, 13, 384, 100);
         getContentPane().add(txtpnQuestion);
         
         JButton btnNext = new JButton("NEXT");
         btnNext.addActionListener(new ActionListener() {
         	public void actionPerformed(ActionEvent arg0) {
         		if (group.getSelection().getActionCommand().equals("4"))
         			nCorrect++;
         		Chapter22();
         	}
         });
         btnNext.setBounds(335, 263, 119, 30);
         getContentPane().add(btnNext);
 	}
 	
 	private void Chapter22()
 	{
 		getContentPane().removeAll();
 		getContentPane().repaint();
 		revalidate();
 		
 		radioPanel = new JPanel();
 		radioPanel.setBounds(22, 147, 313, 117);
 		radioPanel.setLayout(new GridLayout(0, 1));
 		
 		//Create the radio buttons.
         first = new JRadioButton("5");
         first.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
         first.setSelected(true);
 
         second = new JRadioButton("10");
         second.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
 
         third = new JRadioButton("25");
         third.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
 
         fourth = new JRadioButton("Syntax Error");
         fourth.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
    	 
         first.setActionCommand("1");
         second.setActionCommand("2");
         third.setActionCommand("3");
         fourth.setActionCommand("4");
         
         //Group the radio buttons.
         group = new ButtonGroup();
         getContentPane().setLayout(null);
         group.add(first);
         group.add(second);
         group.add(third);
         group.add(fourth);
 
         //Put the radio buttons in a column in a panel.
         radioPanel.add(first);
         radioPanel.add(second);
         radioPanel.add(third);
         radioPanel.add(fourth);
 
         getContentPane().add(radioPanel);
         
         JTextPane txtpnQuestion = new JTextPane();
         txtpnQuestion.setForeground(new Color(160, 82, 45));
         txtpnQuestion.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 15));
         txtpnQuestion.setBackground(SystemColor.control);
         txtpnQuestion.setEditable(false);
         txtpnQuestion.setText("Question 2:  \r\nWhat will be the result of the following LISP code:\r\n\tUSER(57): (defun square(x)\r\n\t\t(* x x))\r\n     \t\tSQUARE\r\n\tUSER(58): (square 5)");
         txtpnQuestion.setBounds(12, 13, 384, 132);
         getContentPane().add(txtpnQuestion);
         
         JButton btnNext = new JButton("NEXT");
         btnNext.addActionListener(new ActionListener() {
         	public void actionPerformed(ActionEvent arg0) {
         		if (group.getSelection().getActionCommand().equals("3"))
         			nCorrect++;
         		Chapter23();
         	}
         });
         btnNext.setBounds(335, 263, 119, 30);
         getContentPane().add(btnNext);
 	}
 	
 	private void Chapter23()
 	{
 		getContentPane().removeAll();
 		getContentPane().repaint();
 		revalidate();
 		
 		radioPanel = new JPanel();
 		radioPanel.setBounds(48, 109, 313, 117);
 		radioPanel.setLayout(new GridLayout(0, 1));
 		
 		//Create the radio buttons.
         first = new JRadioButton("T");
         first.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
         first.setSelected(true);
 
         second = new JRadioButton("True");
         second.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
 
         third = new JRadioButton("NIL");
         third.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
 
         fourth = new JRadioButton("False");
         fourth.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
    	 
         first.setActionCommand("1");
         second.setActionCommand("2");
         third.setActionCommand("3");
         fourth.setActionCommand("4");
 
         //Group the radio buttons.
         group = new ButtonGroup();
         getContentPane().setLayout(null);
         group.add(first);
         group.add(second);
         group.add(third);
         group.add(fourth);
 
         //Put the radio buttons in a column in a panel.
         radioPanel.add(first);
         radioPanel.add(second);
         radioPanel.add(third);
         radioPanel.add(fourth);
 
         getContentPane().add(radioPanel);
         
         JTextPane txtpnQuestion = new JTextPane();
         txtpnQuestion.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 15));
         txtpnQuestion.setForeground(new Color(160, 82, 45));
         txtpnQuestion.setBackground(SystemColor.control);
         txtpnQuestion.setEditable(false);
         txtpnQuestion.setText("Question 3: \r\n\r\nWhen testing whether or not an object is a list, \r\nwhat does \u201C(listp nil)\u201D will return?");
         txtpnQuestion.setBounds(12, 13, 384, 100);
         getContentPane().add(txtpnQuestion);
         
         JButton btnNext = new JButton("NEXT");
         btnNext.addActionListener(new ActionListener() {
         	public void actionPerformed(ActionEvent arg0) {
         		if (group.getSelection().getActionCommand().equals("1"))
         			nCorrect++;
         		Chapter24();
         	}
         });
         btnNext.setBounds(335, 263, 119, 30);
         getContentPane().add(btnNext);
 
 	}
 	
 	private void Chapter24()
 	{
 		getContentPane().removeAll();
 		getContentPane().repaint();
 		revalidate();
 		
 		radioPanel = new JPanel();
 		radioPanel.setForeground(new Color(255, 239, 213));
 		radioPanel.setBounds(22, 109, 453, 150);
 		radioPanel.setLayout(new GridLayout(0, 1));
 		
 		//Create the radio buttons.
         first = new JRadioButton("Rest returns NIL");
         first.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
         first.setSelected(true);
 
         second = new JRadioButton("Rest returns True");
         second.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
 
         third = new JRadioButton("Rest returns a list consisting of all but the first \r\nof its argument");
         third.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
 
         fourth = new JRadioButton("Rest returns a list consisting of all but the repeated arguments");
         fourth.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
    	 
         first.setActionCommand("1");
         second.setActionCommand("2");
         third.setActionCommand("3");
         fourth.setActionCommand("4");
         
         //Group the radio buttons.
         group = new ButtonGroup();
         getContentPane().setLayout(null);
         group.add(first);
         group.add(second);
         group.add(third);
         group.add(fourth);
 
         //Put the radio buttons in a column in a panel.
         radioPanel.add(first);
         radioPanel.add(second);
         radioPanel.add(third);
         radioPanel.add(fourth);
 
         getContentPane().add(radioPanel);
         
         JTextPane txtpnQuestion = new JTextPane();
         txtpnQuestion.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 15));
         txtpnQuestion.setForeground(new Color(160, 82, 45));
         txtpnQuestion.setBackground(SystemColor.control);
         txtpnQuestion.setEditable(false);
         txtpnQuestion.setText("Question 4: \r\n\r\nWhat does the command \u2018rest\u2019 returns as a list?");
         txtpnQuestion.setBounds(12, 13, 384, 100);
         getContentPane().add(txtpnQuestion);
         
         JButton btnNext = new JButton("NEXT");
         btnNext.addActionListener(new ActionListener() {
         	public void actionPerformed(ActionEvent arg0) {
         		if (group.getSelection().getActionCommand().equals("3"))
         			nCorrect++;
         		Chapter25();
         	}
         });
         btnNext.setBounds(335, 263, 119, 30);
         getContentPane().add(btnNext);
 		
 	}
 	
 	private void Chapter25()
 	{
 		getContentPane().removeAll();
 		getContentPane().repaint();
 		revalidate();
 		
 		radioPanel = new JPanel();
 		radioPanel.setBounds(48, 109, 313, 117);
 		radioPanel.setLayout(new GridLayout(0, 1));
 		
 		//Create the radio buttons.
         first = new JRadioButton("True");
         first.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
         first.setSelected(true);
 
         second = new JRadioButton("NIL");
         second.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
 
         third = new JRadioButton("False");
         third.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
 
         fourth = new JRadioButton("A");
         fourth.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
    	 
         first.setActionCommand("1");
         second.setActionCommand("2");
         third.setActionCommand("3");
         fourth.setActionCommand("4");
 
         //Group the radio buttons.
         group = new ButtonGroup();
         getContentPane().setLayout(null);
         group.add(first);
         group.add(second);
         group.add(third);
         group.add(fourth);
 
         //Put the radio buttons in a column in a panel.
         radioPanel.add(first);
         radioPanel.add(second);
         radioPanel.add(third);
         radioPanel.add(fourth);
 
         getContentPane().add(radioPanel);
         
         JTextPane txtpnQuestion = new JTextPane();
         txtpnQuestion.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 15));
         txtpnQuestion.setForeground(new Color(160, 82, 45));
         txtpnQuestion.setBackground(SystemColor.control);
         txtpnQuestion.setEditable(false);
         txtpnQuestion.setText("Question 5: \r\n\r\nWhat does the function \u201C(null\u2019(a b c)\u201D returns?");
         txtpnQuestion.setBounds(12, 13, 384, 100);
         getContentPane().add(txtpnQuestion);
         
         JButton btnNext = new JButton("SUBMIT");
         btnNext.addActionListener(new ActionListener() {
         	@SuppressWarnings("deprecation")
 			public void actionPerformed(ActionEvent arg0) {
         		if (group.getSelection().getActionCommand().equals("2"))
         			nCorrect++;
         		
         		setVisible(false);
         		jframe.enable();
         		jframe.toFront();
         		isDone = true;
         		ltab.result(getResult());
         	}
         });
         btnNext.setBounds(335, 263, 119, 30);
         getContentPane().add(btnNext);
 		
 	}
 	
 	
 	public void Chapter3()
 	{
 		getContentPane().removeAll();
 		getContentPane().repaint();
 		revalidate();
 		
 		radioPanel = new JPanel();
 		radioPanel.setBounds(12, 102, 458, 132);
 		radioPanel.setLayout(new GridLayout(0, 1));
 		
 		//Create the radio buttons.
         first = new JRadioButton("first a test-form and second an else-form");
         first.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
         first.setSelected(true);
 
         second = new JRadioButton("first a then-form and second an else-form");
         second.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
 
         third = new JRadioButton("first a test-form, second a then-form, and the third else-form");
         third.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
 
         fourth = new JRadioButton("first a test-form, second an else-form, and the third is then-form");
         fourth.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
    	 
         first.setActionCommand("1");
         second.setActionCommand("2");
         third.setActionCommand("3");
         fourth.setActionCommand("4");
         
         //Group the radio buttons.
         group = new ButtonGroup();
         getContentPane().setLayout(null);
         group.add(first);
         group.add(second);
         group.add(third);
         group.add(fourth);
 
         //Put the radio buttons in a column in a panel.
         radioPanel.add(first);
         radioPanel.add(second);
         radioPanel.add(third);
         radioPanel.add(fourth);
 
         getContentPane().add(radioPanel);
         
         JTextPane txtpnQuestion = new JTextPane();
         txtpnQuestion.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 15));
         txtpnQuestion.setForeground(new Color(160, 82, 45));
         txtpnQuestion.setBackground(SystemColor.control);
         txtpnQuestion.setEditable(false);
         txtpnQuestion.setText("Question 1: \r\n\r\nWhat are arguments that the IF macro takes?");
         txtpnQuestion.setBounds(12, 13, 384, 100);
         getContentPane().add(txtpnQuestion);
         
         JButton btnNext = new JButton("NEXT");
         btnNext.addActionListener(new ActionListener() {
         	public void actionPerformed(ActionEvent arg0) {
         		if (group.getSelection().getActionCommand().equals("3"))
         			nCorrect++;
         		Chapter32();
         	}
 
         });
         btnNext.setBounds(335, 263, 119, 30);
         getContentPane().add(btnNext);
 	}
 	
 
 	public void Chapter32() {
 		// TODO Auto-generated method stub
 		getContentPane().removeAll();
 		getContentPane().repaint();
 		revalidate();
 		
 		radioPanel = new JPanel();
 		radioPanel.setBounds(48, 109, 313, 117);
 		radioPanel.setLayout(new GridLayout(0, 1));
 		
 		//Create the radio buttons.
         first = new JRadioButton("NIL");
         first.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
         first.setSelected(true);
 
         second = new JRadioButton("True");
         second.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
 
         third = new JRadioButton("False");
         third.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
 
        fourth = new JRadioButton("Its not possible to leave it out");
         fourth.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
    	 
         first.setActionCommand("1");
         second.setActionCommand("2");
         third.setActionCommand("3");
         fourth.setActionCommand("4");
         
         //Group the radio buttons.
         group = new ButtonGroup();
         getContentPane().setLayout(null);
         group.add(first);
         group.add(second);
         group.add(third);
         group.add(fourth);
 
         //Put the radio buttons in a column in a panel.
         radioPanel.add(first);
         radioPanel.add(second);
         radioPanel.add(third);
         radioPanel.add(fourth);
 
         getContentPane().add(radioPanel);
         
         JTextPane txtpnQuestion = new JTextPane();
         txtpnQuestion.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 15));
         txtpnQuestion.setForeground(new Color(160, 82, 45));
         txtpnQuestion.setBackground(SystemColor.control);
         txtpnQuestion.setEditable(false);
         txtpnQuestion.setText("Question 2: \r\n\r\nIf the else-form within the IF macro is left out, what is the default value?");
         txtpnQuestion.setBounds(12, 13, 384, 100);
         getContentPane().add(txtpnQuestion);
         
         JButton btnNext = new JButton("SUBMIT");
         btnNext.addActionListener(new ActionListener() {
         	public void actionPerformed(ActionEvent arg0) {
         		if (group.getSelection().getActionCommand().equals("1"))
         			nCorrect++;
         		
         		Chapter33();
         	}
         });
         btnNext.setBounds(335, 263, 119, 30);
         getContentPane().add(btnNext);
 		
 	}
 	
 	public void Chapter33()
 	{
 		getContentPane().removeAll();
 		getContentPane().repaint();
 		revalidate();
 		
 		radioPanel = new JPanel();
 		radioPanel.setBounds(12, 198, 313, 117);
 		radioPanel.setLayout(new GridLayout(0, 1));
 		
 		//Create the radio buttons.
         first = new JRadioButton("9");
         first.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
         first.setSelected(true);
 
         second = new JRadioButton("11");
         second.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
 
         third = new JRadioButton("(2 3 4)");
         third.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
 
         fourth = new JRadioButton("(4 5 6)");
         fourth.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
    	 
         first.setActionCommand("1");
         second.setActionCommand("2");
         third.setActionCommand("3");
         fourth.setActionCommand("4");
         
         //Group the radio buttons.
         group = new ButtonGroup();
         getContentPane().setLayout(null);
         group.add(first);
         group.add(second);
         group.add(third);
         group.add(fourth);
 
         //Put the radio buttons in a column in a panel.
         radioPanel.add(first);
         radioPanel.add(second);
         radioPanel.add(third);
         radioPanel.add(fourth);
 
         getContentPane().add(radioPanel);
         
         JTextPane txtpnQuestion = new JTextPane();
         txtpnQuestion.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 15));
         txtpnQuestion.setForeground(new Color(160, 82, 45));
         txtpnQuestion.setBackground(SystemColor.control);
         txtpnQuestion.setEditable(false);
         txtpnQuestion.setText("Question 3:  What is the output of the following code:\r\n\r\nUSER(13): (defun add-2 (num)\r\n(+ num 2))\r\nADD-2\r\nUSER(14): (symbol-function 'add-2)\r\n#<Interpreted Function ADD-2>\r\nUSER(15): (mapcar #'add-2'(2 3 4))\r\n");
         txtpnQuestion.setBounds(12, 13, 384, 180);
         getContentPane().add(txtpnQuestion);
         
         JButton btnNext = new JButton("NEXT");
         btnNext.addActionListener(new ActionListener() {
         	public void actionPerformed(ActionEvent arg0) {
         		if (group.getSelection().getActionCommand().equals("4"))
         			nCorrect++;
         		Chapter34();
         	}
 
 
         });
         btnNext.setBounds(335, 263, 119, 30);
         getContentPane().add(btnNext);
 	}
 	
 	public void Chapter34() {
 		// TODO Auto-generated method stub
 		
 		getContentPane().removeAll();
 		getContentPane().repaint();
 		revalidate();
 		
 		radioPanel = new JPanel();
 		radioPanel.setBounds(12, 169, 313, 117);
 		radioPanel.setLayout(new GridLayout(0, 1));
 		
 		//Create the radio buttons.
         first = new JRadioButton("What's up");
         first.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
         first.setSelected(true);
 
         second = new JRadioButton("What's up Sonny");
         second.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
 
         third = new JRadioButton("Sonny");
         third.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
 
         fourth = new JRadioButton("Greeting- list");
         fourth.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
    	 
         first.setActionCommand("1");
         second.setActionCommand("2");
         third.setActionCommand("3");
         fourth.setActionCommand("4");
 
         //Group the radio buttons.
         group = new ButtonGroup();
         getContentPane().setLayout(null);
         group.add(first);
         group.add(second);
         group.add(third);
         group.add(fourth);
 
         //Put the radio buttons in a column in a panel.
         radioPanel.add(first);
         radioPanel.add(second);
         radioPanel.add(third);
         radioPanel.add(fourth);
 
         getContentPane().add(radioPanel);
         
         JTextPane txtpnQuestion = new JTextPane();
         txtpnQuestion.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 15));
         txtpnQuestion.setForeground(new Color(160, 82, 45));
         txtpnQuestion.setBackground(SystemColor.control);
         txtpnQuestion.setEditable(false);
         txtpnQuestion.setText("Question 4:  What is the output of the following code:\r\n\r\nUSER(23): (defun greeting-list (&optional (username \"Sonny\"))\r\n(list 'what\u2019s up\u2019 username))\r\nGREETING-LIST\r\nUSER(24): (greeting-list)\r\n\r\n");
         txtpnQuestion.setBounds(12, 13, 384, 180);
         getContentPane().add(txtpnQuestion);
         
         JButton btnNext = new JButton("NEXT");
         btnNext.addActionListener(new ActionListener() {
         	public void actionPerformed(ActionEvent arg0) {
         		if (group.getSelection().getActionCommand().equals("4"))
         			nCorrect++;
         		Chapter34();
         	}
 
 
         });
         btnNext.setBounds(335, 263, 119, 30);
         getContentPane().add(btnNext);
 		
 	}
 	
 	public void Chapter35() {
 		// TODO Auto-generated method stub
 		getContentPane().removeAll();
 		getContentPane().repaint();
 		revalidate();
 		
 		radioPanel = new JPanel();
 		radioPanel.setBounds(12, 198, 313, 117);
 		radioPanel.setLayout(new GridLayout(0, 1));
 		
 		//Create the radio buttons.
         first = new JRadioButton("What's up");
         first.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
         first.setSelected(true);
 
         second = new JRadioButton("What's up Sonny");
         second.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
 
         third = new JRadioButton("Jobelle");
         third.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
 
         fourth = new JRadioButton("What's up Jobelle");
         fourth.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
    	 
         first.setActionCommand("1");
         second.setActionCommand("2");
         third.setActionCommand("3");
         fourth.setActionCommand("4");
 
         //Group the radio buttons.
         group = new ButtonGroup();
         getContentPane().setLayout(null);
         group.add(first);
         group.add(second);
         group.add(third);
         group.add(fourth);
 
         //Put the radio buttons in a column in a panel.
         radioPanel.add(first);
         radioPanel.add(second);
         radioPanel.add(third);
         radioPanel.add(fourth);
 
         getContentPane().add(radioPanel);
         
         JTextPane txtpnQuestion = new JTextPane();
         txtpnQuestion.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 15));
         txtpnQuestion.setForeground(new Color(160, 82, 45));
         txtpnQuestion.setBackground(SystemColor.control);
         txtpnQuestion.setEditable(false);
         txtpnQuestion.setText("Question 5:  From question 4, if the user enters \u201C(greeting-list \u201CJobelle\u201D)\u201D what will the output be?\r\n\r\nUSER(23): (defun greeting-list (&optional (username \"Sonny\"))\r\n(list 'what\u2019s up\u2019 username))\r\nGREETING-LIST\r\nUSER(24): (greeting-list)\r\n\r\n");
         txtpnQuestion.setBounds(12, 13, 384, 201);
         getContentPane().add(txtpnQuestion);
         
         JButton btnNext = new JButton("NEXT");
         btnNext.addActionListener(new ActionListener() {
         	@SuppressWarnings("deprecation")
 			public void actionPerformed(ActionEvent arg0) {
         		if (group.getSelection().getActionCommand().equals("4"))
         			nCorrect++;
         		setVisible(false);
         		isDone = true;
         		jframe.enable();
         		jframe.toFront();
         		ltab.result(getResult());
         	}
 
         });
         btnNext.setBounds(335, 263, 119, 30);
         getContentPane().add(btnNext);
 		
 	}
 	
 	public void Chapter1()
 	{
 		getContentPane().removeAll();
 		getContentPane().repaint();
 		revalidate();
 		
 		radioPanel = new JPanel();
 		radioPanel.setBounds(48, 109, 313, 117);
 		radioPanel.setLayout(new GridLayout(0, 1));
 		
 		//Create the radio buttons.
         first = new JRadioButton("(irobot.rotate-deg -270)");
         first.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
         first.setSelected(true);
 
         second = new JRadioButton("(irobot.rotate-deg -30)");
         second.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
 
         third = new JRadioButton("(irobot.rotate-mm 270)");
         third.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
 
         fourth = new JRadioButton("(irobot.rotate-mm 90)");
         fourth.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
 
         first.setActionCommand("1");
         second.setActionCommand("2");
         third.setActionCommand("3");
         fourth.setActionCommand("4");
 
         //Group the radio buttons.
         group = new ButtonGroup();
         getContentPane().setLayout(null);
         group.add(first);
         group.add(second);
         group.add(third);
         group.add(fourth);
 
         //Put the radio buttons in a column in a panel.
         radioPanel.add(first);
         radioPanel.add(second);
         radioPanel.add(third);
         radioPanel.add(fourth);
 
         getContentPane().add(radioPanel);
         
         JTextPane txtpnQuestion = new JTextPane();
         txtpnQuestion.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 15));
         txtpnQuestion.setForeground(new Color(160, 82, 45));
         txtpnQuestion.setBackground(SystemColor.control);
         txtpnQuestion.setEditable(false);
         txtpnQuestion.setText("Question 1: \r\n\r\nIf you wish to rotate the iRobot by 270 degrees, \r\nwhich one of the following options is a valid approach:");
         txtpnQuestion.setBounds(12, 13, 384, 100);
         getContentPane().add(txtpnQuestion);
         
         JButton btnNext = new JButton("SUBMIT");
         btnNext.addActionListener(new ActionListener() {
         	public void actionPerformed(ActionEvent arg0) {
         		if (group.getSelection().getActionCommand().equals("2"))
         			nCorrect++;
         		Chapter12();
         	}
         });
         btnNext.setBounds(335, 263, 119, 30);
         getContentPane().add(btnNext);
 	}
 	
 	public void Chapter12()
 	{
 		getContentPane().removeAll();
 		getContentPane().repaint();
 		revalidate();
 		
 		radioPanel = new JPanel();
 		radioPanel.setBounds(48, 109, 313, 117);
 		radioPanel.setLayout(new GridLayout(0, 1));
 		
 		//Create the radio buttons.
         first = new JRadioButton("0");
         first.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
         first.setSelected(true);
 
         second = new JRadioButton("99");
         second.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
 
         third = new JRadioButton("69");
         third.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
 
         fourth = new JRadioButton("255");
         fourth.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
    	 
         first.setActionCommand("1");
         second.setActionCommand("2");
         third.setActionCommand("3");
         fourth.setActionCommand("4");
         
         //Group the radio buttons.
         group = new ButtonGroup();
         getContentPane().setLayout(null);
         group.add(first);
         group.add(second);
         group.add(third);
         group.add(fourth);
 
         //Put the radio buttons in a column in a panel.
         radioPanel.add(first);
         radioPanel.add(second);
         radioPanel.add(third);
         radioPanel.add(fourth);
 
         getContentPane().add(radioPanel);
         
         JTextPane txtpnQuestion = new JTextPane();
         txtpnQuestion.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 15));
         txtpnQuestion.setForeground(new Color(160, 82, 45));
         txtpnQuestion.setBackground(SystemColor.control);
         txtpnQuestion.setEditable(false);
        txtpnQuestion.setText("Question 2: \r\n\r\nIn order to set up the iRobots LED color to red; which integer value can be used?");
         txtpnQuestion.setBounds(12, 13, 384, 100);
         getContentPane().add(txtpnQuestion);
         
         JButton btnNext = new JButton("SUBMIT");
         btnNext.addActionListener(new ActionListener() {
         	public void actionPerformed(ActionEvent arg0) {
         		if (group.getSelection().getActionCommand().equals("4"))
         			nCorrect++;
         		Chapter13();
         	}
         });
         btnNext.setBounds(335, 263, 119, 30);
         getContentPane().add(btnNext);
 	}
 	
 	public void Chapter13()
 	{
 		getContentPane().removeAll();
 		getContentPane().repaint();
 		revalidate();
 		
 		radioPanel = new JPanel();
 		radioPanel.setBounds(48, 109, 313, 117);
 		radioPanel.setLayout(new GridLayout(0, 1));
 		
 		//Create the radio buttons.
        first = new JRadioButton("-2000  2000");
         first.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
         first.setSelected(true);
 
         second = new JRadioButton("0-1000");
         second.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
 
         third = new JRadioButton("0- 100");
         third.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
 
        fourth = new JRadioButton("-100  100");
         fourth.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
    	 
         first.setActionCommand("1");
         second.setActionCommand("2");
         third.setActionCommand("3");
         fourth.setActionCommand("4");
 
         //Group the radio buttons.
         group = new ButtonGroup();
         getContentPane().setLayout(null);
         group.add(first);
         group.add(second);
         group.add(third);
         group.add(fourth);
 
         //Put the radio buttons in a column in a panel.
         radioPanel.add(first);
         radioPanel.add(second);
         radioPanel.add(third);
         radioPanel.add(fourth);
 
         getContentPane().add(radioPanel);
         
         JTextPane txtpnQuestion = new JTextPane();
         txtpnQuestion.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 15));
         txtpnQuestion.setForeground(new Color(160, 82, 45));
         txtpnQuestion.setBackground(SystemColor.control);
         txtpnQuestion.setEditable(false);
        txtpnQuestion.setText("Question 3: \r\n\r\nWhat is the range within the drives parameter radius?");
         txtpnQuestion.setBounds(12, 13, 384, 100);
         getContentPane().add(txtpnQuestion);
         
         JButton btnNext = new JButton("SUBMIT");
         btnNext.addActionListener(new ActionListener() {
         	public void actionPerformed(ActionEvent arg0) {
         		if (group.getSelection().getActionCommand().equals("1"))
         			nCorrect++;
         		Chapter14();
         	}
         });
         btnNext.setBounds(335, 263, 119, 30);
         getContentPane().add(btnNext);
 	}
 	
 	public void Chapter14()
 	{
 		getContentPane().removeAll();
 		getContentPane().repaint();
 		revalidate();
 		
 		radioPanel = new JPanel();
 		radioPanel.setBounds(48, 109, 313, 117);
 		radioPanel.setLayout(new GridLayout(0, 1));
 		
 		//Create the radio buttons.
        first = new JRadioButton("Luther, Isabelle, Sonny, Patrick (LISPs developers)");
         first.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
         first.setSelected(true);
 
         second = new JRadioButton("List Processing");
         second.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
 
         third = new JRadioButton("Louisiana Institute Secret Project");
         third.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
 
         fourth = new JRadioButton("Louis I Saint-Pierre");
         fourth.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
    	 
         first.setActionCommand("1");
         second.setActionCommand("2");
         third.setActionCommand("3");
         fourth.setActionCommand("4");
 
         //Group the radio buttons.
         group = new ButtonGroup();
         getContentPane().setLayout(null);
         group.add(first);
         group.add(second);
         group.add(third);
         group.add(fourth);
 
         //Put the radio buttons in a column in a panel.
         radioPanel.add(first);
         radioPanel.add(second);
         radioPanel.add(third);
         radioPanel.add(fourth);
 
         getContentPane().add(radioPanel);
         
         JTextPane txtpnQuestion = new JTextPane();
         txtpnQuestion.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 15));
         txtpnQuestion.setForeground(new Color(160, 82, 45));
         txtpnQuestion.setBackground(SystemColor.control);
         txtpnQuestion.setEditable(false);
         txtpnQuestion.setText("Question 4: \r\n\r\nWhat does LISP stands for?");
         txtpnQuestion.setBounds(12, 13, 384, 100);
         getContentPane().add(txtpnQuestion);
         
         JButton btnNext = new JButton("SUBMIT");
         btnNext.addActionListener(new ActionListener() {
         	public void actionPerformed(ActionEvent arg0) {
         		if (group.getSelection().getActionCommand().equals("2"))
         			nCorrect++;
         		Chapter15();
         	}
         });
         btnNext.setBounds(335, 263, 119, 30);
         getContentPane().add(btnNext);
 	}
 	
 	public void Chapter15()
 	{
 		getContentPane().removeAll();
 		getContentPane().repaint();
 		revalidate();
 		
 		radioPanel = new JPanel();
 		radioPanel.setBounds(48, 109, 313, 117);
 		radioPanel.setLayout(new GridLayout(0, 1));
 		
 		//Create the radio buttons.
         first = new JRadioButton("Values have types, but variables do not");
         first.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
         first.setSelected(true);
 
         second = new JRadioButton("Values do not have types, but variables do");
         second.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
 
         third = new JRadioButton("Values and variables do not have types");
         third.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
 
         fourth = new JRadioButton("Both values and variables have types");
         fourth.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
    	 
         first.setActionCommand("1");
         second.setActionCommand("2");
         third.setActionCommand("3");
         fourth.setActionCommand("4");
         
         //Group the radio buttons.
         group = new ButtonGroup();
         getContentPane().setLayout(null);
         group.add(first);
         group.add(second);
         group.add(third);
         group.add(fourth);
 
         //Put the radio buttons in a column in a panel.
         radioPanel.add(first);
         radioPanel.add(second);
         radioPanel.add(third);
         radioPanel.add(fourth);
 
         getContentPane().add(radioPanel);
         
         JTextPane txtpnQuestion = new JTextPane();
         txtpnQuestion.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 15));
         txtpnQuestion.setForeground(new Color(160, 82, 45));
         txtpnQuestion.setBackground(SystemColor.control);
         txtpnQuestion.setEditable(false);
         txtpnQuestion.setText("Question 5: \r\n\r\nIn CL:");
         txtpnQuestion.setBounds(12, 13, 384, 100);
         getContentPane().add(txtpnQuestion);
         
         JButton btnNext = new JButton("SUBMIT");
         btnNext.addActionListener(new ActionListener() {
         	@SuppressWarnings("deprecation")
 			public void actionPerformed(ActionEvent arg0) {
         		if (group.getSelection().getActionCommand().equals("1"))
         			nCorrect++;
         		setVisible(false);
         		isDone = true;
         		jframe.enable();
         		jframe.toFront();
         		ltab.result(getResult());
         	}
         });
         btnNext.setBounds(335, 263, 119, 30);
         getContentPane().add(btnNext);
 	}
 	
 	public int getResult()
 	{
 		return nCorrect;
 	}
 	
 	public boolean isDone()
 	{
 		return isDone;
 	}
 
 }
