 package test1;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.io.*;
 import javax.swing.*;
 import javax.swing.event.MouseInputListener;
 
 
 /**
  * 
  * @author yamilasusta
  *
  */
 public class Logic extends JPanel implements ActionListener, MouseInputListener {
 
 	/**
 	 * Mandatory modification by eclipse
 	 */
 	private static final long serialVersionUID = -1144062643362290194L;
 
 	// Instance variables
 	public JMenuBar bar;
 	public JTextField inputField;
 	public JButton tryButton;
 	public JPanel southJPanel;
 	private HighScores hs;
 	private TheGrid grid1;
	private TheGrid grid2;
 	private static final int FRAMEX = 600;
 	private static final int FRAMEY = 300;
 	private int areax = 20; // modificar cuando cesar haga new
 	private int areay = 10; // modificar cuando cesar haga new
 	//private String player1; // mod cesar new
 	//private String player2; // mod cesar new/default roboto
 	//private int ships; // mod cesar new
 	
 	/**
 	 * Default constructor
 	 * Generates the menus, the grid, and the input options
 	 */
 	public Logic() {
 
 		//Initialize menu
 		bar = new JMenuBar();
 		bar.setBackground(Color.DARK_GRAY);
 		JMenu menu = new JMenu("Menu");
 		menu.setBackground(Color.DARK_GRAY);
 		bar.add(menu);
 		JMenuItem newgame = new JMenuItem("New Game");
 		menu.add(newgame);
 		newgame.addActionListener(this);
 		JMenuItem opengame = new JMenuItem("Open Saved Game");
 		menu.add(opengame);
 		opengame.addActionListener(this);
 		menu.addSeparator();
 		JMenuItem highscores = new JMenuItem("View High Scores");
 		menu.add(highscores);
 		highscores.addActionListener(this);
 
 		//Initialize high scores
 		hs = new HighScores();
 
 		//Text field setup
 		inputField = new JTextField(30);
 		inputField.addActionListener(new ActionListener() {	
 			public void actionPerformed(ActionEvent e) {
 				String input = inputField.getText();
 				if(verifyInput(input)) {
 					inputField.setText("");
 				}
 			}
 		});
 
 		//JButton setup
 		tryButton = new JButton("Try");
 		tryButton.addActionListener(new ActionListener() {	
 			public void actionPerformed(ActionEvent e) {
 				String input = inputField.getText();
 				if (verifyInput(input)) {
 					inputField.setText("");
 				}
 			}
 		});
 
 		//Input panel setup
 		southJPanel = new JPanel();
 		southJPanel.add(inputField);
 		southJPanel.add(tryButton);
 		
 		//TheGrid preparation
 		grid1 = new TheGrid();
		grid2 = new TheGrid();
 
 	}
 
 	/**
 	 * Modification of the paintComponent to draw in the JPanel
 	 */
 	public void paintComponent(Graphics g) {
 		Graphics2D g2 = (Graphics2D) g; 
 		drawBorders(g2);
 		drawGrid(g2);
 	}
 	
 	/**
 	 * Draws the actual playing field
 	 * @param g2 Graphical painter
 	 */
 	public void drawGrid(Graphics2D g2) {
 		Color background = Color.white;
 		g2.setColor(background);
 		Rectangle gridRectangle = new Rectangle(31, 16, 601-((int)FRAMEX/FRAMEY), 301-((int)FRAMEY/FRAMEY));
 		g2.fill(gridRectangle);
 		
 		for (int i = 0; i < grid1.theGrid.length; i++) {
 			for (int j = 0; j < grid1.theGrid[0].length; j++) {
 				g2.setColor(Color.BLACK);
 				g2.draw(grid1.theGrid[i][j].getRect());
				if(grid1.theGrid[i][j].isHit())
 					g2.drawString("X", (int)grid1.theGrid[i][j].getRect().getCenterX(), (int)grid1.theGrid[i][j].getRect().getCenterY());
 			}
 		}
		
		for (int i = 0; i < grid2.theGrid.length; i++) {
			for (int j = 0; j < grid2.theGrid[0].length; j++) {
				g2.setColor(Color.BLACK);
				g2.draw(grid2.theGrid[i][j].getRect());
				if(grid2.theGrid[i][j].isHit())
					g2.drawString("X", (int)grid2.theGrid[i][j].getRect().getCenterX(), (int)grid2.theGrid[i][j].getRect().getCenterY());
			}
		}
		
 
 	}
 
 	/**
 	 * Draws the margin borders and the legend
 	 * @param g2 Graphical painter
 	 */
 	public void drawBorders(Graphics2D g2) {
 		//Borders of game
 		Color borde = Color.BLACK;
 		Color fill = Color.GRAY;
 		g2.setColor(fill);
 		Rectangle background = new Rectangle(0, 0, FRAMEX+FRAMEX/areax, 15);
 		g2.fill(background);
 
 		g2.setColor(borde);
 		Rectangle corner = new Rectangle(0, 0, FRAMEX/areax, 15);
 		g2.draw(corner);
 
 		char letter = 'A';
 		for (int i = 1; i <= areax; i++) {
 			corner = new Rectangle(i*(FRAMEX/areax), 0, FRAMEX/areax, 15);
 			g2.draw(corner);
 			g2.drawString(letter + "", (int)corner.getCenterX()-3, (int)(corner.getCenterY()+5 ));
 			letter += 1;
 		}
 
 		background = new Rectangle(0, 15, FRAMEX/areax, FRAMEY);
 		g2.setColor(fill);
 		g2.fill(background);
 
 		for (int i = 1; i <= areay; i++) {
 			g2.setColor(borde);
 			corner = new Rectangle(0, i*(FRAMEY/areay)-15, FRAMEX/areax, FRAMEY/areay);
 			g2.draw(corner);
 			g2.drawString(Integer.toString(i), (int)corner.getCenterX()-4, (int)corner.getCenterY()+4);
 		}
 	}
 
 
 	public void mouseClicked(MouseEvent e) {
 
 	}
 
 	public void mouseEntered(MouseEvent e) {
 
 	}
 
 	public void mouseExited(MouseEvent e) {
 
 	}
 
 	public void mousePressed(MouseEvent e) {
 
 	}
 
 	public void mouseReleased(MouseEvent e) {
 
 	}
 
 	public void mouseDragged(MouseEvent e) {
 
 	}
 
 	public void mouseMoved(MouseEvent e) {
 
 	}
 
 	/**
 	 * Waits for the input in the menu bar
 	 */
 	public void actionPerformed(ActionEvent e) {
 		if(e.getActionCommand().equals("View High Scores")) {
 			if(!new File("highscores.txt").exists()) {
 				try {
 					hs.generateFile();
 				} catch (IOException e1) {}
 
 			}
 			JFrame highscores = new JFrame();
 			highscores.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
 			highscores.setTitle("High Scores");
 			JTextArea scores = new JTextArea(hs.getString());
 			highscores.add(scores);
 			highscores.setSize(400, 400);
 			highscores.setVisible(true);
 
 		}
 
 		if (e.getActionCommand().equals("New Game")) {
 
 		}
 
 		if (e.getActionCommand().equals("Open Saved Game")) {
 
 		}
 
 	}
 
 	/**
 	 * Verifies that the input is a valid one
 	 * @param input Input from the user
 	 * @return If the input is valid
 	 */
 	static boolean verifyInput(String input) {
 		char[] parse = input.toCharArray();
 		if(!Character.isLetter(parse[0]))
 			return false;
 		for (int i = 1; i < parse.length; i++) 
 			if (!Character.isDigit(parse[i])) 
 				return false;
 		return true;
 
 	}
 
 
 }
