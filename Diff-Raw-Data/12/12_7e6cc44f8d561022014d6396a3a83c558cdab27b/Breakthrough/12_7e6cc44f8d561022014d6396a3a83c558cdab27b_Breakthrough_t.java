 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 
 public class Breakthrough extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
 	static JButton [][] buttons = new JButton[8][8]; 
 	static int counter = 0;
 	static String buttonPressed, buttonValue;
 	JPanel north, center, south;
 	JLabel playerTurn, p1Score, p2Score;
 	JMenuBar menuBar;
 	JMenu file;
 	JMenuItem newGame;
 	
 	public Breakthrough() {		
 		northPanel();
 		centerPanel();
 		southPanel();
 		createMenu();
 		
 		setVisible(true);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		pack();
 		setResizable(false);	
 		setLocationRelativeTo(null);
 	
 	}
 	
 	public void northPanel() {
 		north = new JPanel();
 		playerTurn = new JLabel("Player 1's Turn");
 		north.add(playerTurn);
 		add(north, BorderLayout.NORTH);
 	}
 	
 	public void centerPanel()	{
 		center = new JPanel(new GridLayout(8, 8));
 		//setLayout(new GridLayout(8, 8));
 		
 		for(int col = 0; col < 8; col++)
 		{
 			for(int row = 0; row < 8; row++)
 			{
 				
 				buttons[row][col] = new Button(row, col);
 				center.add(buttons[row][col]);		
 
 			}
 		}
 		
 		add(center, BorderLayout.CENTER);
 	}
 	
 	public void southPanel() {
 		south = new JPanel();
 		p1Score = new JLabel("Player 1: 1");
 		p2Score = new JLabel("Player 2: 0");
 		
 		south.add(p1Score);
 		south.add(p2Score);
 		
 		add(south, BorderLayout.SOUTH);
 		
 	}
 	
 	public void createMenu() {
 		menuBar = new JMenuBar();
 		file = new JMenu("File");
 		newGame = new JMenuItem("New Game");
 		
 		file.add(newGame);
 		menuBar.add(file);
 		
 		file.setMnemonic('F');
 		newGame.setMnemonic('N');
 		
 		setJMenuBar(menuBar);
 	}
 	
 	public static void main(String [] args) {
 		new Breakthrough();
 	}
 }
 
 class Button extends JButton implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
 	int x, y;
 
 	public Button(int row, int col) {
 		x = row;
 		y = col;
 		assignButtonValue();
 		setCoordinates();
 		this.addActionListener(this);
 	}
 	
 	public void assignButtonValue() {
 		if (x == 0 || x == 1) {
 			setText("O");			
 		}
 		else if (x == 7 || x == 6)	{
 			setText("X");
 		}
 	}
 	
 	public void setCoordinates()	{
 		String message = "(" + x + ", " + y + ")";
 		setToolTipText(message);
 	}
 	
 	public String getCoordinates() {
 		String coordinates = "(" + x + "," + y + ")";
 		return coordinates;
 	}
 	
 	public void makeMove(int x, int y) {		
 		if(Breakthrough.buttonValue.equals("X"))
 		{
 			if(this.x == (x - 1)) { //this line checks for forward movement
 				if ((this.y == (y - 1)) || (this.y == y) || (this.y == (y + 1)) ) { //this line checks for diagonal forward movement
 					if (Breakthrough.buttons[(this.x + 1)][(this.y - 1)].getText().equals("O") || Breakthrough.buttons[(this.x + 1)][(this.y + 1)].getText().equals("O")) { //this line checks if diagonal has value
 					}
 					else {					
 						Breakthrough.buttons[x][y].setText("");	// x and y are the old coordinates
 						Breakthrough.buttons[this.x][this.y].setText(Breakthrough.buttonValue);	//this.x and this.y are the new coordinates
 					}
 				}
 				else {
 					JOptionPane.showMessageDialog(null, "You can only move forward or diagonal by one space");
 				}
 			}
 			else {
 				JOptionPane.showMessageDialog(null, "You can only move forward or diagonal by one space");
 			}
 		}
 		
 		else if(Breakthrough.buttonValue.equals("O"))
 		{
 			if(this.x == (x + 1)) {
 				if ((this.y == (y - 1)) || (this.y == y) || (this.y == (y + 1)) ) {
 					Breakthrough.buttons[x][y].setText("");	// x and y are the old coordinates
 					Breakthrough.buttons[this.x][this.y].setText(Breakthrough.buttonValue);	//this.x and this.y are the new coordinates
 				}
 				else {
 					JOptionPane.showMessageDialog(null, "You can only move forward or diagonal by one space");
 				}
 			}
 			else {
 				JOptionPane.showMessageDialog(null, "You can only move forward or diagonal by one space");
 			}
 		}
 
 	}
 	
 	public void actionPerformed(ActionEvent ae)	{
 		if (ae.getActionCommand().equals("X") && Breakthrough.counter == 0) {
 			Breakthrough.buttonValue = "X";
 			Breakthrough.buttonPressed = getCoordinates();
 			Breakthrough.counter++;
 		}
 		else if (ae.getActionCommand().equals("") && Breakthrough.counter == 1) {
 			int x = Integer.parseInt(Breakthrough.buttonPressed.substring(1,2));
 			int y = Integer.parseInt(Breakthrough.buttonPressed.substring(3,4));
 			makeMove(x,y);
 			Breakthrough.counter = 0;			
 		}
 		else if (ae.getActionCommand().equalsIgnoreCase("O") && Breakthrough.counter == 0) {
 			Breakthrough.buttonValue = "O";	
 			Breakthrough.buttonPressed = getCoordinates();
 			Breakthrough.counter++;		
 		}
 		else if (ae.getActionCommand().equals("") && Breakthrough.counter == 1) {
 			int x = Integer.parseInt(Breakthrough.buttonPressed.substring(1,2));
 			int y = Integer.parseInt(Breakthrough.buttonPressed.substring(3,4));
 			makeMove(x,y);
 			Breakthrough.counter = 0;			
 		}
 		
 		else if(ae.getActionCommand().equals("") && Breakthrough.counter == 0)
 		{
 			JOptionPane.showMessageDialog(null, "Error: You cannot move an empty space");
 		}
 		
 		else if( (ae.getActionCommand().equals("")) == false && Breakthrough.counter == 1)
 		{
 			JOptionPane.showMessageDialog(null, "Error: You can");
 		}
 		
 	}
 
 }
