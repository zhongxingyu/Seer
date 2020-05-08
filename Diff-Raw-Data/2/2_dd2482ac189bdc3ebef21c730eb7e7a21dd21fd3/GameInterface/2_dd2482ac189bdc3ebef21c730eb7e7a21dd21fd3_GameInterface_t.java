 import javax.imageio.ImageIO;
 import javax.swing.*;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.util.Calendar;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.util.Calendar;
 import java.util.InputMismatchException;
 import java.util.NoSuchElementException;
 import java.util.Scanner;
 
 /**
  * A class to display the user interface for the board to allow play
  * @author Sam
  */
 public class GameInterface {
 	
 	static JFrame frame;
 	static Container overall;
 	static Container pane;
 	static Container menu;
 	static Container centralMenu;
 	//The boxes themselves and images that go with them
 	static JButton[][] box;
 	static BufferedImage[] imageValue;
 	static BufferedImage[] imageSelectedValue;
 	static ImageIcon[] iconValue;
 	static ImageIcon[] iconSelectedValue;
 	
 	//texture for the background
 	static BufferedImage backgroundTexture;
 	static Graphics2D backgroundTextureGraphic;
 	
 	static JButton btnMenu;
 	static JButton btnHint;
 	static JLabel elapseTimer;
 	static JLabel timerLabel;
 	
 	static JButton[] btnNewGame;
 	static JButton btnMenuQuit;
 	
 	static final int boxWidth = 50;
 	static final int boxHeight = 50;
 	//static final int subBoxHeight = 20;
 	//static final int subBoxWidth = 50;
 	static final int frameWidth = 520;
 	static final int frameHeight = 580;
 	
 	
 	static final boolean MENU = true;
 	static final boolean GAME = false;
 	static boolean viewMode = MENU;
 	
 	static final Color PRESET_COLOR = new Color(140, 140, 140);
 	static final Color WRONG_COLOR = new Color(200, 140, 140);
 	static final Color USER_COLOR = new Color(255, 255, 255);
 	static final Color HINT_COLOR = new Color(230, 230, 230);
 	
 	static final Color PRESET_TEXT_COLOR = new Color(100, 100, 100);
 	static final Color WRONG_TEXT_COLOR = new Color(255, 0, 0);
 	static final Color USER_TEXT_COLOR = Color.BLACK;
 	static final Color HINT_TEXT_COLOR = new Color(150, 150, 250);
 	
 	/**
 	 * Constructor that creates a new gui of a game board
 	 * @param newLayout
 	 */
 	static final String SUDOKU_DESCRIPTION_1 = "<html><b>THE SUDOKU</b> is a number puzzle invented by American Architect Howard Garns around 1979.</html>";
 	static final String SUDOKU_DESCRIPTION_2 = "<html>In Sudoku a player must put the numbers 1-9 in a grid so that all squares are filled and there is " +
 			"no repeated number in any row, column or any one of the 9 3x3 squares</html>";
 	
 	public GameInterface(){
 		
 		try { //sets the appearance and behaviour of gui widgets
 			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (UnsupportedLookAndFeelException e) {
 			e.printStackTrace();
 		}
 		
 		loadImages();
 		//creates a sudoku frame
 		frame = new JFrame("Sudoku");
 		frame.setSize(frameWidth, frameHeight); //sets the frame size
 		pane = frame.getContentPane(); //sets a pane within the frame
 		frame.setSize(frameWidth, frameHeight);
 		frame.setBackground(Color.WHITE);
 		overall = frame.getContentPane();
 		overall.setBackground(Color.WHITE);
 		
 		
 		pane = new Container();
 		menu = new Container();
 		
 		centralMenu = new Container();
 		overall.add(pane);
 		overall.add(menu);
 		menu.add(centralMenu);
 		
 		menu.setSize(frame.getSize());
 		menu.setLocation(0, 0);
 		menu.setLayout(null);
 		
 		centralMenu.setBounds(10, 10, 500, 500);
 		centralMenu.setLayout(new GridLayout(3, 3));
 		
 		btnNewGame = new JButton[3];
 		btnNewGame[0] = new JButton("Easy");
 		btnNewGame[1] = new JButton("Medium");
 		btnNewGame[2] = new JButton("Hard");
 		
 		btnMenuQuit = new JButton("Quit");
 				
 		centralMenu.add(new JLabel("SUDOKU"));
 		centralMenu.add(new JLabel());
 		centralMenu.add(new JLabel(SUDOKU_DESCRIPTION_1));
 		centralMenu.add(new JLabel(SUDOKU_DESCRIPTION_2));
 		centralMenu.add(new JLabel());
 		centralMenu.add(btnMenuQuit);
 		centralMenu.add(btnNewGame[0]);
 		centralMenu.add(btnNewGame[1]);
 		centralMenu.add(btnNewGame[2]);
 		
 		//btnNewGame.setBounds(100, 500, btnNewGame.getPreferredSize().width, btnNewGame.getPreferredSize().height);
 		//btnMenuQuit.setBounds(300, 500, btnMenuQuit.getPreferredSize().width, btnMenuQuit.getPreferredSize().height);
 		
 		btnMenuQuit.addActionListener(new btnQuitListener());		
 		btnNewGame[0].addActionListener(new btnNewGameListener(0));		
 		btnNewGame[1].addActionListener(new btnNewGameListener(1));		
 		btnNewGame[2].addActionListener(new btnNewGameListener(2));
 		
 		menu.setVisible(true);
 		
 		pane.setSize(frame.getSize());
 		pane.setLayout(null);
 		
 		//sudoku board
 		box = new JButton[9][9];
 		//Get it so that I take this from the puzzle
 		//entry = new Square[9][9];
 		//subBox = new JTextPane[9][9];
 		setStartingBoxInfo();
 		
 		//timer label
 		timerLabel = new JLabel("Elapsed Time");
 		pane.add(timerLabel);
 		timerLabel.setBounds(10, frameHeight - 55, timerLabel.getPreferredSize().width, timerLabel.getPreferredSize().height);
 		
 		//timer label with time that will update
 		elapseTimer = new JLabel("00:00");
 		pane.add(elapseTimer);
 		elapseTimer.setBounds(115, frameHeight - 55, elapseTimer.getPreferredSize().width + 100, elapseTimer.getPreferredSize().height);
 		
 		//hint button
 		btnHint = new JButton("HINT");
 		pane.add(btnHint);
 		btnHint.setBounds(350, frameHeight - 60, 155, btnHint.getPreferredSize().height);
 		btnHint.addActionListener(new btnHintListener());
 		
 		btnHint = new JButton("HINT");
 		pane.add(btnHint);
 		btnHint.setBounds(350, frameHeight - 60, 155, btnHint.getPreferredSize().height);
 		btnHint.addActionListener(new btnHintListener());
 		
 		btnMenu = new JButton("MENU");
 		pane.add(btnMenu);
 		btnMenu.setBounds(180, frameHeight - 60, 155, btnHint.getPreferredSize().height);
 		btnMenu.addActionListener(new btnMenuListener());
 
 		//frame.addKeyListener(new keyPressedListener());
 		
 		pane.setVisible(false);
 		frame.requestFocus();
 		frame.setVisible(true); //set frame as visible
 		updateTimer(); //updates the time elapsed
 	}
 		
 	/**
 	 * Returns the integer of the given square or -1 if the square is empty
 	 * @param row	The row value of the square (0-8)
 	 * @param col	The column value of the square (0-8)
 	 * @return		The integer of the square
 	 */
 	public int getBoxValue(int row, int col){
 		return boardLayout[row][col].getCurrentValue();
 	}
 	
 	/**
 	 * Function to update the timer with how much time has elapsed
 	 */
 	public void updateTimer() {
 		int hour, minute, sec;
 		while (!hasWon()) { //checks if game is still going
 			long timeInSeconds = Puzzle.calculateTimeElapse(startTime); //finds the time elapsed
 			hour = (int) (timeInSeconds / 3600);
 			timeInSeconds = timeInSeconds - (hour * 3600);
 			minute = (int) (timeInSeconds / 60);
 			timeInSeconds = timeInSeconds - (minute * 60);
 			sec = (int) (timeInSeconds); //calculates hours, minutes based on total seconds
 			elapseTimer.setText(String.format("%02d", hour) + ":" + String.format("%02d", minute)  + 
 					":" + String.format("%02d", sec)); //updates the timer
 		}
 	}
 	
 	/**
 	 * Returns whether or not there is a selected square
 	 * @return	True if a square is selected, false otherwise
 	 */
 	public boolean squareSelected(){
 		return (source != null);
 	}
 
 	/**
 	 * Sets all the starting boxes including their positions and initially set values
 	 */
 	private static void setStartingBoxInfo(){
 		int x = 0;
 		int y = 0;
 		while (y < 9){
 			while (x < 9){
 				box[x][y] = new JButton();
 				//subBox[x][y] = new JTextPane();
 				pane.add(box[x][y]);
 				//pane.add(subBox[x][y]);
 				box[x][y].setBounds(x*boxWidth+((Math.round(x/3)+1) * 10) + x*3, y*boxHeight+((Math.round(y/3)+1) * 10) + y*3, boxWidth, boxHeight);
 				box[x][y].setBorder(BorderFactory.createEmptyBorder());
 
 				box[x][y].addActionListener(new btnSquareListener(x, y));
 				box[x][y].addKeyListener(new keyPressedListener(y, x));
 				//box[x][y].setForeground(defaultBGColor);
 				//subBox[x][y].setBounds(x*boxWidth+10, y*boxHeight+10, boxWidth, boxHeight);
 				//subBox[x][y].setEnabled(false);
 				//entry[x][y] = new Square(0, 0);
 				x++;
 			}
 			y++;
 			x = 0;
 		}
 
 	}
 	
 	/**
 	 * Function to update the display of the board when elements within it have changed
 	 */
 	private static void loadImages() {
 		imageValue = new BufferedImage[10];
 		imageSelectedValue = new BufferedImage[10];
 		iconValue = new ImageIcon[10];
 		iconSelectedValue = new ImageIcon[10];
 		//tries to load image files for each square type
 		try {
 			System.out.println("Attempt to load images");
 			imageValue[0] = ImageIO.read(new File("src/ProjectPics/Invisible/blank.png"));
 			System.out.print("*");
 			imageValue[1] = ImageIO.read(new File("src/ProjectPics/Invisible/1.png"));
 			System.out.print("*");
 			imageValue[2] = ImageIO.read(new File("src/ProjectPics/Invisible/2.png"));
 			System.out.print("*");
 			imageValue[3] = ImageIO.read(new File("src/ProjectPics/Invisible/3.png"));
 			System.out.print("*");
 			imageValue[4] = ImageIO.read(new File("src/ProjectPics/Invisible/4.png"));
 			System.out.print("*");
 			imageValue[5] = ImageIO.read(new File("src/ProjectPics/Invisible/5.png"));
 			System.out.print("*");
 			imageValue[6] = ImageIO.read(new File("src/ProjectPics/Invisible/6.png"));
 			System.out.print("*");
 			imageValue[7] = ImageIO.read(new File("src/ProjectPics/Invisible/7.png"));
 			System.out.print("*");
 			imageValue[8] = ImageIO.read(new File("src/ProjectPics/Invisible/8.png"));
 			System.out.print("*");
 			imageValue[9] = ImageIO.read(new File("src/ProjectPics/Invisible/9.png"));
 			System.out.print("*");
 			imageSelectedValue[0] = ImageIO.read(new File("src/ProjectPics/Selected/Invisible/blank.png"));
 			System.out.print("*");
 			imageSelectedValue[1] = ImageIO.read(new File("src/ProjectPics/Selected/Invisible/1.png"));
 			System.out.print("*");
 			imageSelectedValue[2] = ImageIO.read(new File("src/ProjectPics/Selected/Invisible/2.png"));
 			System.out.print("*");
 			imageSelectedValue[3] = ImageIO.read(new File("src/ProjectPics/Selected/Invisible/3.png"));
 			System.out.print("*");
 			imageSelectedValue[4] = ImageIO.read(new File("src/ProjectPics/Selected/Invisible/4.png"));
 			System.out.print("*");
 			imageSelectedValue[5] = ImageIO.read(new File("src/ProjectPics/Selected/Invisible/5.png"));
 			System.out.print("*");
 			imageSelectedValue[6] = ImageIO.read(new File("src/ProjectPics/Selected/Invisible/6.png"));
 			System.out.print("*");
 			imageSelectedValue[7] = ImageIO.read(new File("src/ProjectPics/Selected/Invisible/7.png"));
 			System.out.print("*");
 			imageSelectedValue[8] = ImageIO.read(new File("src/ProjectPics/Selected/Invisible/8.png"));
 			System.out.print("*");
 			imageSelectedValue[9] = ImageIO.read(new File("src/ProjectPics/Selected/Invisible/9.png"));
 			System.out.println("*");
 			for (int i = 0; i < 10; i++){
 				iconValue[i] = new ImageIcon(imageValue[i]);
 				iconSelectedValue[i] = new ImageIcon(imageSelectedValue[i]);
 			}
 			
			backgroundTexture = ImageIO.read(new File("src/ProjectPics/texture.jpg"));
 			backgroundTextureGraphic = backgroundTexture.createGraphics();
 			
 		} catch (Exception e) {
 			System.out.println("IMAGE FILES NOT FOUND!");
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Creates a newGame and sets the board layout.
 	 */
 	private static void newGame(int difficulty){
 		boardLayout = Puzzle.createPuzzle(difficulty);
 		int x, y;
 		Integer value;
 		for (x = 0; x < 9; x++){
 			for (y = 0; y < 9; y++){
 				value = boardLayout[y][x].getCurrentValue();
 				box[x][y].setBackground(Color.white);
 				box[x][y].setIcon(getSquareIcon(value, false));
 				if (value != 0){
 					box[x][y].setBackground(PRESET_COLOR);
 					box[x][y].setForeground(PRESET_TEXT_COLOR);
 				} else {
 					box[x][y].setText("");
 				}
 
 			}
 		}
 		
 		startTime = Calendar.getInstance();
 	}
 	
 	/**
 	 * Switches between the game board and the main menu when called
 	 */
 	private static void switchView(){
 		viewMode = !viewMode;
 		
 		menu.setVisible(!menu.isVisible());
 		pane.setVisible(!pane.isVisible());
 		
 		startTime = Calendar.getInstance();
 	}
 	
 	private static void resetSourceBox(){
 		System.out.println("STUFF");
 		int row = 0;
 		int col = 0;
 		int type = -1;
 		JButton currentBox = null;
 		Square currentSquare = null;
 		//for each square
 		while (row < 9){
 			while (col < 9){
 				currentSquare = boardLayout[row][col];
 				//if there is a set value for the square, set it to that
 				if (currentSquare.getCurrentValue() >= 0){
 					currentBox = box[col][row];
 					//if not legal, set the look
 					if (LegalCheck.isNotLegal(boardLayout, currentSquare, currentSquare.getCurrentValue())){
 						currentSquare.setType(Square.ERROR_CELL);
 					} else {
 						//if legal, and predefined cell, set it to that type
 						if (!(currentSquare.getType() == Square.PREDEFINE_CELL))
 							currentSquare.setType(currentSquare.getPreviousType());
 					}
 					type = currentSquare.getType(); //sets the look for various square types
 					currentBox.setIcon(getSquareIcon(currentSquare.getCurrentValue(), currentBox.hasFocus()));
 					type = currentSquare.getType();
 					if (type == Square.USER_INPUT_CELL){
 						currentBox.setBackground(USER_COLOR);
 						currentBox.setForeground(USER_TEXT_COLOR);
 					} else if (type == Square.ERROR_CELL){
 						currentBox.setBackground(WRONG_COLOR);
 						currentBox.setForeground(WRONG_TEXT_COLOR);
 					} else if (type == Square.EMPTY_CELL){
 						currentBox.setBackground(USER_COLOR);
 						currentBox.setForeground(USER_TEXT_COLOR);
 					} else if (type == Square.HINT_CELL){
 						currentBox.setBackground(HINT_COLOR);
 						currentBox.setForeground(HINT_TEXT_COLOR);
 					} else if (type == Square.PREDEFINE_CELL) {
 						currentBox.setBackground(PRESET_COLOR);
 						currentBox.setForeground(PRESET_TEXT_COLOR);
 					}
 					if (currentSquare.getCurrentValue() == 0)
 						currentBox.setText(""); //if empty cell, display it as such
 				} else {
 					Integer x = 0;
 					String boxString = "";
 					while (x < 9){ //displays the draft cell
 						if (currentSquare.isMarkedDraft(x)){
 							boxString = (boxString + " " + x.toString());
 						}
 						x++;
 					}
 	
 				}
 				col++;
 			}
 			row++;
 			col = 0;
 		}
 		/*
 		//If there is a set value for the square, set it to that
 		if (boardLayout[inputY][inputX].getCurrentValue() > 0){
 			source.setText(Integer.toString(boardLayout[inputY][inputX].getCurrentValue()));
 			if (boardLayout[inputY][inputX].getType() == Square.USER_INPUT_CELL){
 				source.setBackground(USER_COLOR);
 				source.setForeground(USER_TEXT_COLOR);
 			} else if (boardLayout[inputY][inputX].getType() == Square.ERROR_CELL){
 				source.setBackground(WRONG_COLOR);
 				source.setForeground(WRONG_TEXT_COLOR);
 			} else if (boardLayout[inputY][inputX].getType() == Square.EMPTY_CELL){
 				source.setBackground(USER_COLOR);
 			} else if (boardLayout[inputY][inputX].getType() == Square.HINT_CELL){
 				source.setBackground(HINT_COLOR);
 				source.setForeground(HINT_TEXT_COLOR);
 			}
 		} else {
 			Square tempDraft = boardLayout[inputY][inputX];
 			Integer x = 0;
 			String boxString = "";
 			while (x < 9){
 				if (tempDraft.isMarkedDraft(x)){
 					boxString = (boxString + " " + x.toString());
 				}
 				x++;
 			}
 			source.setText(boxString);
 		}
 		*/
 		//checks if game has been won
 		if (hasWon()){
 			System.out.println("CONGRATULATIONS YOU WON!!!");
 		}
 		//update the display
 		pane.repaint();
 
 	}
 		
 	
 	/**
 	 * Checks if the player has won the game. This checks
 	 * if the boards if completely filled first, then for each filled
 	 * squares, checks if there are duplicates in the regions.
 	 * @return True if the board is completely filled and all values
 	 * are valid. False otherwise.
 	 */
 	public static boolean hasWon() {
 		if (viewMode == GAME) {
 			boolean boardFilled = checkBoardFilled();
 			
 			if (boardFilled) {
 				//checks if each value of the board is legal, if so, has won
 				for (int i = 0; i < Puzzle.ROW_NUMBER; i++) {
 					for (int j = 0; j < Puzzle.COLUMN_NUMBER; j++) {
 						if (LegalCheck.isNotLegal(boardLayout, boardLayout[i][0], boardLayout[i][0].getCurrentValue()))
 							return false;
 					}
 				}
 			} else
 				return false;
 			return true;
 		} else
 			return false;
 	}
 	
 	/**
 	 * Checks if the board is completely filled out.
 	 * @return True if the board is completely filled out.
 	 * False otherwise.
 	 */
 	private static boolean checkBoardFilled() {
 		for (int i = 0; i < Puzzle.ROW_NUMBER; i++) {
 			for (int j = 0; j < Puzzle.COLUMN_NUMBER; j++) {
 				if (boardLayout[i][j].getCurrentValue() == 0)
 					return false;
 			} //checks if the board doesn't have empty values
 		}
 		return true;
 	}
 	
 	/**
 	 * De-selects all squares
 	 */
 	private static void deselectAll(){
 		inputX = -1;
 		inputY = -1;
 		source = null;
 		
 		int x, y;
 		for (x = 0; x < 9; x++){
 			for (y = 0; y < 9; y++){
 				box[x][y].setIcon(getSquareIcon(boardLayout[y][x].getCurrentValue(), false));
 			}
 		}
 	}
 	
 	private static ImageIcon getSquareIcon(int number, boolean selected){
 		if (selected){
 			return iconSelectedValue[number];
 		} else {
 			return iconValue[number];
 		}
 	}
 		
 	private static JButton source;
 	private static int inputX;
 	private static int inputY;
 	private static Square[][] boardLayout;
 	private static Calendar startTime = Calendar.getInstance();
 	
 	//============================================================================================================================================================
 	//AAAAAAAA  CCCCCCCC  TTTTTTTTTT  IIIIII  OOOOOOOO  NN      NN        LL      IIIIII  SSSSSSSS  TTTTTTTTTT  EEEEEEEE  NN      NN  EEEEEEEE  RRRRRR    SSSSSSSS
 	//AA    AA  CC            TT        II    OO    OO  NNNN    NN        LL        II    SS            TT      EE        NNNN    NN  EE        RR    RR  SS
 	//AA    AA  CC            TT        II    OO    OO  NN  NN  NN        LL        II    SSSS          TT      EEEEEE    NN  NN  NN  EEEEEE    RRRRRR    SSSS
 	//AAAAAAAA  CC            TT        II    OO    OO  NN  NN  NN        LL        II        SSSS      TT      EE        NN  NN  NN  EE        RRRR          SSSS
 	//AA    AA  CC            TT        II    OO    OO  NN    NNNN        LL        II          SS      TT      EE        NN    NNNN  EE        RR  RR          SS
 	//AA    AA  CCCCCCCC      TT      IIIIII  OOOOOOOO  NN      NN        LLLLLL  IIIIII  SSSSSSSS      TT      EEEEEEEE  NN      NN  EEEEEEEE  RR    RR  SSSSSSSS
 	//============================================================================================================================================================
 	
 	/**
 	 * Action Listener to quit
 	 * @author Sam
 	 */
 	public static class btnQuitListener implements ActionListener{
 		/**
 		 * Function to quit when button is pressed
 		 */
 		public void actionPerformed(ActionEvent e){
 			System.exit(0);
 		}
 	}
 	
 	/**
 	 * Action Listener to provide a hint when a user desires such
 	 */
 	public static class btnHintListener implements ActionListener{
 		/**
 		 * Function to provide a hint when button is pressed
 		 */
 		public void actionPerformed(ActionEvent e){
 			if (hasWon()) //if game is won, do nothing
 				return;
 			//creates new hint system and uses it to find hint from current board
 			HintSystem h = new HintSystem();
 			Move newMove = h.Hint(boardLayout);
 			//checks to see if move is valid
 			if (newMove != null && newMove.getValue() != 0) {
 				//sets the board value to hint
 				boardLayout[newMove.getY()][newMove.getX()].setCurrentValue(newMove.getValue());
 				//set what has changed
 				inputX = newMove.getX();
 				inputY = newMove.getY();
 				source = box[inputX][inputY];
 				//sets the color of the hint cell
 				Color hintColor = new Color(102, 255, 178);
 				source.setForeground(hintColor);
 				//updates the display
 				resetSourceBox();
 			}
 		}
 	}
 	
 	/**
 	 * Action Listener for each of the 81 squares 
 	 * @author Sam
 	 */
 	public static class btnSquareListener implements ActionListener{
 		int squareX, squareY;
 		
 		/**
 		 * Constructor to create a new square listener
 		 * @param x the x coor
 		 * @param y the y coor
 		 */
 		public btnSquareListener(int x, int y){
 			squareX = x;
 			squareY = y;
 		}
 		
 		/**
 		 * Function to update input when square has been selected
 		 */
 		public void actionPerformed(ActionEvent e){
 			//source.setForeground(defaultBGColor);
 			deselectAll();
 			System.out.println("square selected " + squareX + " " + squareY);
 			System.out.println(" square type is " + boardLayout[squareY][squareX].getType() + " and has value " + boardLayout[squareY][squareX].getCurrentValue());
 			source = (JButton) e.getSource(); //sets the square selection as the source
 			inputX = squareX;
 			inputY = squareY;
 //			source.setBackground(Color.green);
 			source.setIcon(getSquareIcon(boardLayout[inputY][inputX].getCurrentValue(), true));
 			//source.setText(inputX + " " + inputY);
 			//source.setForeground(selectedBGColor);
 		}
 	}
 	
 	/**
 	 * Action Listener for checking when someone presses the New Game buttons
 	 * it then changes the screens and loads a new board.
 	 * @author Sam
 	 *
 	 */
 	public static class btnNewGameListener implements ActionListener{
 		int difficulty;
 		
 		public btnNewGameListener(int diff){
 			difficulty = diff;
 		}
 		
 		public void actionPerformed(ActionEvent e){
 			newGame(difficulty);
 			switchView();
 		}
 	}
 	
 	/**
 	 * Action Listener for when someone presses the menu button in the main board
 	 * @author Sam
 	 *
 	 */
 	public static class btnMenuListener implements ActionListener{
 
 		public void actionPerformed(ActionEvent e) {
 			switchView();
 		}
 	}
 		
 	/**
 	 * Key Press listeners for typing
 	 * @author Sam
 	 */
 	public static class keyPressedListener implements KeyListener{
 		private int row;
 		private int column;
 		
 		/**
 		 * Creates a new action listener for the key
 		 * @param row x coor
 		 * @param col y coor
 		 */
 		public keyPressedListener(int row, int col){
 			this.row = row;
 			this.column = col;
 		}
 		
 		public void keyPressed(KeyEvent e) {}
 
 		public void keyReleased(KeyEvent e) {}
 
 		/**
 		 * Function to update the display when an input is received
 		 */
 		public void keyTyped(KeyEvent e) {
 			System.out.println("Key Typed " + e.getKeyChar());
 			char key = e.getKeyChar();
 			//if the square selected isn't a predefined cell
 			if (boardLayout[row][column].getType() != Square.PREDEFINE_CELL && Character.isDigit(key)){
 				int number = 0;
 				boolean shift = false;
 				// If shift is pressed, set shift to true
 				if (e.isShiftDown()){
 					shift = true;
 					System.out.println("SHIFT");
 				}
 				// then work out what number is pressed
 				if (key == '!' || key == '1'){
 					number = 1;
 				} else if (key == '@' || key == '2'){
 					number = 2;
 				} else if (key == '#' || key == '3'){
 					number = 3;
 				} else if (key == '$' || key == '4'){
 					number = 4;
 				} else if (key == '%' || key == '5'){
 					number = 5;
 				} else if (key == '^' || key == '6'){
 					number = 6;
 				} else if (key == '&' || key == '7'){
 					number = 7;
 				} else if (key == '*' || key == '8'){
 					number = 8;
 				} else if (key == '(' || key == '9'){
 					number = 9;
 				}
 				if (shift){ //if shift is pressed, change to draft move
 					boardLayout[row][column].switchDraftValue(number);
 				} else { //sets the value of the square to input
 					//boolean illegal = LegalCheck.checkLegal(boardLayout, boardLayout[row][column], number);
 					boardLayout[row][column].setCurrentValue(number);
 					//if (!illegal){
 					//	boardLayout[row][column].setType(Square.USER_INPUT_CELL);
 					//} else {
 					//	boardLayout[row][column].setType(Square.ERROR_CELL);
 					//	System.out.println(" WRONG!!!!!!!!!");
 					//}
 				}
 				//deletes the current value depending on input
 			} else if (boardLayout[row][column].getType() != Square.PREDEFINE_CELL && (e.getKeyCode() == KeyEvent.VK_0 || e.getKeyCode() == 0 || e.getKeyCode() == KeyEvent.VK_BACK_SPACE || e.getKeyCode() == KeyEvent.VK_DELETE)) {
 				boardLayout[row][column].setType(Square.EMPTY_CELL);
 				boardLayout[row][column].setCurrentValue(0);
 			}
 			resetSourceBox();
 		}
 	}
 		
 	//NOTE: if you're looking for private variables, they're above the Action Listeners
 }
