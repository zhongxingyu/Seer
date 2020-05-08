 import java.awt.*;
 import java.util.*;
 import javax.swing.*;//System.currentTimeMillis();
 import java.awt.geom.*;
 import java.awt.event.*;
 
 public class MasterGUI extends JFrame
 {
 
 	final static Font TEXT_FONT = new Font("arial",Font.BOLD,16);
 	final static Font MESSAGE_FONT = new Font("arial",Font.BOLD,12);
 
 	JTextField guessBox = new JTextField(5);
 	JButton submit = new JButton("Guess!");
 	MasterMindBoard graphic;
 
 	//Constructor makes a JFrame, adds the guess menu, and the board
 	//needs the number of guesses for this game and an object to register button clicks
 	public MasterGUI(int numGuesses, String correctGuess, ActionListener listener)
 	{
 		super("MasterMind");
 
 		//layouts for the window and the menu-panel
 		BorderLayout org = new BorderLayout();
 		FlowLayout org2 = new FlowLayout();
 
 
 		//sets up the top menu
 		JPanel menu = new JPanel();
 		menu.setLayout(org2);
 		guessBox.setSize(new Dimension(200,50));
 		guessBox.setFont(TEXT_FONT);
 		submit.setSize(new Dimension(200,50));
 		submit.setFont(TEXT_FONT);
 		menu.add(guessBox,BorderLayout.NORTH);
 		menu.add(submit,BorderLayout.SOUTH);
 		submit.addActionListener(listener);
		submit.setMnemonic(KeyEvent.VK_ENTER);//The constant value for the carriage return (enter)
 
 		graphic = new MasterMindBoard(numGuesses, correctGuess);
 
 		//sets the values for the window
 		setLayout(org);
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 		setResizable(false);
 		setSize(400,650);
 		add(menu,BorderLayout.NORTH);
 		add(graphic);
 		setVisible(true);
 	}
 
 	public void gameOver()
 	{
 		submit.setEnabled(false);
 		guessBox.setEnabled(false);
 		graphic.gameOver();
 		repaint();
 	}
 
 	//returns what is currently in the textbox
 	public String getGuess()
 	{
 		return guessBox.getText().toUpperCase();
 	}
 
 	//registers the guess with the MasterMindBoard and resets the textbox
 	public void submitFeedback(int numRight, int numClose)
 	{
 		graphic.registerGuess(guessBox.getText().toUpperCase(),numRight,numClose);
 		guessBox.setText("");
		guessBox.requestFocus();
 		repaint();
 	}
 
 	//used for demoing the board
 	public static void main(String[] args)
 	{
 		int numGuess = 8;
 		MasterGUI obj = new MasterGUI(numGuess,"ABCD",null);
 		for(int y = 0; y < numGuess; y++)
 		{
 			for(int x = 0; x<30000;x++)
 			{
 				System.out.println(".");
 			}
 			obj.submitFeedback(1,2);
 			//obj.repaint();
 		}
 		obj.gameOver();
 	}
 }
