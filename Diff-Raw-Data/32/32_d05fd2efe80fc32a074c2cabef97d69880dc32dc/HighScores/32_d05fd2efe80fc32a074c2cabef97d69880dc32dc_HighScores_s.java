 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.util.Scanner;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 
 
 public class HighScores extends JFrame implements ActionListener{
 	
 	public static final int WIDTHHIGH = 400;
 	public static final int HEIGHTHIGH = 400;
 	
 	Scanner readHighScores = null;
 	JLabel highScoreScreen;
 	
 	String name = null, time = null, moves = null, level = null;
 	
 	HighScores(){
 		
 		super();
 		
 		
 		// gets the dimensions to set frame in center of screen
 		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 		int sWidth = (((int)screenSize.getWidth()/2) - WIDTHHIGH/2);
 		int sHeight = (((int)screenSize.getHeight()/2) - HEIGHTHIGH/2);
 		
 		
 		// sets the properties of the frame
 		setSize(WIDTHHIGH, HEIGHTHIGH);
 		setTitle("Team Uhhhh");
 		setLocation(sWidth, sHeight);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		
 		setLayout(new BorderLayout()); // creates a "bottom" Border Layout
 		
 		
 		// reads in the from the file and inputs them into a corresponding variable 
 		try{
 			readHighScores = new Scanner(new FileInputStream("highscores.txt"));
 			name = readHighScores.next();
 			time = readHighScores.next();
 			moves = readHighScores.next();
 			level = readHighScores.next();
 			
 			highScoreScreen = new JLabel("Name: " + name + "   Your Time: " + time + "   Moves: " + moves + "   Levels: " + level); // creates a TextArea to display results
 		}
 		catch (FileNotFoundException e){
 			System.out.println("HighScores file not found");
 		}
 		
 		
 		// adds the highScoreScreen Panel to the center of the frame
 		add(highScoreScreen, BorderLayout.CENTER);
 		
 		
 		// creates a new Panel and Button to allow the user exit the High-Score Screen
 		JPanel submitButton = new JPanel(new FlowLayout());
 		JButton closeWindow = new JButton("OK");
 		closeWindow.addActionListener(this);
 		submitButton.add(closeWindow);
 		add(submitButton, BorderLayout.SOUTH);
 	}
 
 	public void actionPerformed(ActionEvent e) {
 		String getAction = e.getActionCommand(); // gets the label on the button
 		
 		
 		// if the button says "OK" then a new settings page will be created and the current High-Scores Screen will be disposed
 		if(getAction.equals("OK")){
 			dispose();
 		}
 	}
 }
