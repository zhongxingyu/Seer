 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.io.FileInputStream;
 import java.util.Arrays;
 import java.util.Random;
 import java.util.Scanner;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.KeyStroke;
 import javax.swing.Timer;
 
 
 public class GridLayoutPage extends JFrame implements ActionListener{
 	
 	public static final int WIDTHGRID = 500;
 	public static final int HEIGHTGRID = 500;
 	
 	private String getLevel = null;
 	private String constantArray[] = new String[81];
 	private String answerArray[] = new String[81];
 	
 	private int counterPress = 0, timeCount = 0, delay = 1000, indexCounter = 0, randomFile = 0;
 	private int indexArray[] = new int[81];
 	
 	private JPanel topPanel = null, gridPanel = null;
 	private JLabel timeClock = null;
 	private JLabel moveCounter = null;
 	private JLabel levelView = null;
 	private JTextField[] input = new JTextField[81];
 	private JMenuBar menuBar = null;
 	private JMenu menu = null, color = null, colorNumber = null;
 	//private JMenuItem menuItem = null;
 	
 	private Color getColor = null, getNumColor = null;
 	private Boolean checkBackColor = false, checkNumColor = false;
 	
 
 
 	private Boolean[] checkIfAnswered = new Boolean[81];
 	Boolean checkDemovar;
 	
 	Font inputFont = new Font("Arial", Font.BOLD, 30);
 	
 	Timer time = new Timer(delay, new ActionListener() {   // added javax.swing.Timer to count by time delays(1000 milliseconds or 1 second)
         public void actionPerformed(ActionEvent e) {
             timeUpdate();
         }
      });
 	
 	public GridLayoutPage(int x, int y, String level, Boolean checkDemo, Boolean checkColor, Color setColor, Boolean checkNumberColor, Color numberColor) {
 		
 		super();
 		
 		
 		// gets info need to make certain "check"s 
 		getLevel = level; // gets level
 		checkDemovar = checkDemo; // checks if "Demo" has been pressed
 		checkBackColor = checkColor;
 		getColor = setColor;
 		getNumColor = numberColor;
 		checkNumColor = checkNumberColor;
 		Arrays.fill(checkIfAnswered, false); // fills the "check if answered" to all false
 		Arrays.fill(indexArray, 0); // fills the index array to all 0
 		getRandom();
 		getConstantNumbers(); // opens an input .txt file to input "constant" numbers
 		getAnswers(); // opens the corresponding answers .txt file to get "answers"
 
 		
 		
 		// check if "Demo" is ON, if 'ON' then will fill every array position but 1 to the answer
 		if(checkDemovar){
 			indexCounter = 81;
 			int i = 0, lastIndex = 0;
 			while(indexArray[i] > 0){
 				lastIndex = indexArray[i];
 				i++;
 			}
 			for (i = 0; i < 81; i++){
 				if(i == lastIndex){
 					continue;
 				}
 				else{
 					constantArray[i] = answerArray[i];
 					indexArray[i] = i;
 				}
 			}
 		}
 		
 		
 		// sets screen to pop up in middle of screen
 		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 		int sWidth = (((int)screenSize.getWidth()/2) - WIDTHGRID/2);
 		int sHeight = (((int)screenSize.getHeight()/2) - HEIGHTGRID/2);
 		
 		
 		// sets properties of frame
 		setSize(WIDTHGRID, HEIGHTGRID);
 		setTitle("Team Uhhhh");
 		setLocation(sWidth, sHeight);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setLayout(new BorderLayout()); // sets the base layout to Border Layout to add multiple frames
 
 		
 		menuBar = new JMenuBar();
 		
 		
 		menu = new JMenu("File");
 		menu.add(makeMenuItem("Open"));
 		menu.add(makeMenuItem("Close"));
 		menu.add(makeMenuItem("Save"));
 		
 		menuBar.add(menu);
 		
 		JPanel changeBackGrid = new JPanel();
 		changeBackGrid.setLayout(new BorderLayout());
 		
 		JPanel colorGrid = new JPanel();
 		colorGrid.setLayout(new GridLayout(3,3));
 		
 		JButton test = new JButton(" RED ");
 		test.addActionListener(this);
 		test.setBackground(Color.RED);
 		test.setForeground(Color.RED);
 		test.setOpaque(true);
 		colorGrid.add(test);
 		
 		JButton test1 = new JButton(" BLUE ");
 		test1.addActionListener(this);
 		test1.setBackground(Color.BLUE);
 		test1.setForeground(Color.BLUE);
 		test1.setOpaque(true);
 		colorGrid.add(test1);
 		
 		JButton test2 = new JButton(" GREEN ");
 		test2.addActionListener(this);
 		test2.setBackground(Color.GREEN);
 		test2.setForeground(Color.GREEN);
 		test2.setOpaque(true);
 		colorGrid.add(test2);
 		
 		JButton test3 = new JButton(" BLACK ");
 		test3.addActionListener(this);
 		test3.setBackground(Color.BLACK);
 		test3.setForeground(Color.BLACK);
 		test3.setOpaque(true);
 		colorGrid.add(test3);
 		
 		JButton test4 = new JButton(" GRAY ");
 		test4.addActionListener(this);
 		test4.setBackground(Color.GRAY);
 		test4.setForeground(Color.GRAY);
 		test4.setOpaque(true);
 		colorGrid.add(test4);
 		
 		JButton test5 = new JButton(" ORANGE ");
 		test5.addActionListener(this);
 		test5.setBackground(Color.ORANGE);
 		test5.setForeground(Color.ORANGE);
 		test5.setOpaque(true);
 		colorGrid.add(test5);
 		
 		JButton test6 = new JButton(" CYAN ");
 		test6.addActionListener(this);
 		test6.setBackground(Color.CYAN);
 		test6.setForeground(Color.CYAN);
 		test6.setOpaque(true);
 		colorGrid.add(test6);
 		
 		JButton test7 = new JButton(" PINK ");
 		test7.addActionListener(this);
 		test7.setBackground(Color.pink);
 		test7.setForeground(Color.pink);
 		test7.setOpaque(true);
 		colorGrid.add(test7);
 		
 		JButton test8 = new JButton(" YELLOW ");
 		test8.addActionListener(this);
 		test8.setBackground(Color.YELLOW);
 		test8.setForeground(Color.YELLOW);
 		test8.setOpaque(true);
 		colorGrid.add(test8);
 		
 		
 		changeBackGrid.add(colorGrid, BorderLayout.CENTER);
 		
 		JPanel defaultButtonPanel1 = new JPanel();
 		defaultButtonPanel1.setLayout(new FlowLayout(FlowLayout.CENTER));
 		JButton defaultButton1 = new JButton("Set Background Color Back to Default");
 		defaultButton1.addActionListener(this);
 		defaultButtonPanel1.add(defaultButton1);
 		colorGrid.add(defaultButtonPanel1);
 		
 		changeBackGrid.add(defaultButtonPanel1, BorderLayout.SOUTH);
 		
 
 		
 		color = new JMenu("Background Color");
 		color.add(changeBackGrid);
 		menuBar.add(color);
 		
 		JPanel changeNumberGrid = new JPanel();
 		changeNumberGrid.setLayout(new BorderLayout());
 		
 		JPanel colorGridNumbers = new JPanel();
 		colorGridNumbers.setLayout(new GridLayout(3,3));
 		
 		JButton numberRed = new JButton(" red ");
 		numberRed.addActionListener(this);
 		numberRed.setBackground(Color.RED);
 		numberRed.setForeground(Color.RED);
 		numberRed.setOpaque(true);
 		colorGridNumbers.add(numberRed);
 		
 		JButton numberBlue = new JButton(" blue ");
 		numberBlue.addActionListener(this);
 		numberBlue.setBackground(Color.BLUE);
 		numberBlue.setForeground(Color.BLUE);
 		numberBlue.setOpaque(true);
 		colorGridNumbers.add(numberBlue);
 		
 		JButton numberGreen = new JButton(" green ");
 		numberGreen.addActionListener(this);
 		numberGreen.setBackground(Color.GREEN);
 		numberGreen.setForeground(Color.GREEN);
 		numberGreen.setOpaque(true);
 		colorGridNumbers.add(numberGreen);
 		
 		JButton numberBlack = new JButton(" black ");
 		numberBlack.addActionListener(this);
 		numberBlack.setBackground(Color.BLACK);
 		numberBlack.setForeground(Color.BLACK);
 		numberBlack.setOpaque(true);
 		colorGridNumbers.add(numberBlack);
 		
 		JButton numberGray = new JButton(" gray ");
 		numberGray.addActionListener(this);
 		numberGray.setBackground(Color.GRAY);
 		numberGray.setForeground(Color.GRAY);
 		numberGray.setOpaque(true);
 		colorGridNumbers.add(numberGray);
 		
 		JButton numberOrange = new JButton(" orange ");
 		numberOrange.addActionListener(this);
 		numberOrange.setBackground(Color.ORANGE);
 		numberOrange.setForeground(Color.ORANGE);
 		numberOrange.setOpaque(true);
 		colorGridNumbers.add(numberOrange);
 		
 		JButton numberCyan = new JButton(" cyan ");
 		numberCyan.addActionListener(this);
 		numberCyan.setBackground(Color.CYAN);
 		numberCyan.setForeground(Color.CYAN);
 		numberCyan.setOpaque(true);
 		colorGridNumbers.add(numberCyan);
 		
 		JButton numberPink = new JButton(" pink ");
 		numberPink.addActionListener(this);
 		numberPink.setBackground(Color.pink);
 		numberPink.setForeground(Color.pink);
 		numberPink.setOpaque(true);
 		colorGridNumbers.add(numberPink);
 		
 		JButton numberYellow = new JButton(" yellow ");
 		numberYellow.addActionListener(this);
 		numberYellow.setBackground(Color.YELLOW);
 		numberYellow.setForeground(Color.YELLOW);
 		numberYellow.setOpaque(true);
 		colorGridNumbers.add(numberYellow);
 		
 		changeNumberGrid.add(colorGridNumbers, BorderLayout.CENTER);
 		
 		JPanel defaultButtonPanel = new JPanel();
 		defaultButtonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
 		JButton defaultButton = new JButton("Set Number Color Back to Default");
 		defaultButton.addActionListener(this);
 		defaultButtonPanel.add(defaultButton);
 		colorGridNumbers.add(defaultButtonPanel);
 		
 		changeNumberGrid.add(defaultButtonPanel, BorderLayout.SOUTH);
 		
 		colorNumber = new JMenu("Number Color");
 		colorNumber.add(changeNumberGrid);
 		menuBar.add(colorNumber);
 		
 		setJMenuBar(menuBar);
 		
 		
 		setJMenuBar(menuBar);
 		
 		
 		
 		
 		
 		
 		
 		
 		// creates a new Grid Layout to add the top pane (indicator, grid for game, check buttons)
 		topPanel = new JPanel();
 		topPanel.setLayout(new GridLayout(1, 3));
 		
 		
 		// creates a "Time" label to keep track of time
 		timeClock = new JLabel("Time: " + timeCount, JLabel.CENTER);
 		timeClock.setFont(new Font("Verdana", Font.BOLD, 13));
 		topPanel.add(timeClock);
 		
 		
 		// creates a "Total Moves" to keep track of the total moves
 		moveCounter = new JLabel("Total Moves: " + counterPress, JLabel.CENTER);
 		moveCounter.setFont(new Font("Verdana", Font.BOLD, 13));
 		topPanel.add(moveCounter);
 		
 		
 		// creates a new "Level" label to inform user of the Level
 		levelView = new JLabel("Level: " + level, JLabel.CENTER);
 		levelView.setFont(new Font("Verdana", Font.BOLD, 13));
 		topPanel.add(levelView);
 		
 		add(topPanel, BorderLayout.NORTH); // adds the pane to the top of the base Border Layout (to frame)
 		
 		
 		// creates a new 9x9 Grid Layout
 		gridPanel = new JPanel();
 		gridPanel.setLayout(new GridLayout(x, y));
 		
 		if(checkColor)
 			gridPanel.setBackground(setColor);
 		
 		
 		
 		
 		// will fill in or leave blank the text fields in the grid
 		for (int i = 0; i < (9*9); i++){
 			if(checkColor){
 				input[i] = new JTextField(1);
 				input[i].setFont(inputFont);
 				input[i].setHorizontalAlignment(JTextField.CENTER);
 				input[i].setBackground(setColor);
 				if(checkNumColor){
 					input[i].setForeground(getNumColor);
 				}
 			}
 			else{
 				input[i] = new JTextField(1);
 				input[i].setFont(inputFont);
 				input[i].setHorizontalAlignment(JTextField.CENTER);
 				if(checkNumColor){
 					input[i].setForeground(getNumColor);
 				}
 			}
 			int moveConstant = 0;
 			for (int j = 0; j < indexCounter; j++){ // 
 				if(i == indexArray[j]){ // if the index i is equal to a cell that should have a constant value then the cell will show the constant and remain un-editble
 					input[i].setText(constantArray[indexArray[j]]);
 					input[i].setEditable(false);
 					moveConstant++;
 				}
 				else
 					input[i].addActionListener(this); // else the cell becomes "actionable"
 				
 			}
 			
 			// used to make the layout look nice
 			if (i == 2 || i == 11 || i == 29 || i == 38 || i == 56 || i == 65 || i == 74)
 			{                                                     //top left bottom right color
 				input[i].setBorder(BorderFactory.createMatteBorder(1, 1, 1, 5, Color.black));
 			}
 			else if (i == 5 || i == 14  || i == 32 || i == 41 || i == 59 || i == 68 || i == 77)
 			{
 				input[i].setBorder(BorderFactory.createMatteBorder(1, 1, 1, 5, Color.black));
 			}
 			else if (i == 20 || i == 47 || i == 23 || i == 50)
 			{
 				input[i].setBorder(BorderFactory.createMatteBorder(1, 1, 5, 5, Color.black));
 			}
 			else if ((i >= 18 && i <= 26) || (i >= 45 && i <= 53))
 			{
 				input[i].setBorder(BorderFactory.createMatteBorder(1, 1, 5, 1, Color.black));
 			}
 			else
 			{
 				input[i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
 			}
 			
 			gridPanel.add(input[i]);
 		}
 		
 		add(gridPanel, BorderLayout.CENTER); // adds the game grid to frame
 		
 		
 		// creates a new Panel to place buttons (centered and spread 50px apart)
 		JPanel bottomPanel = new JPanel();
 		bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 50, 5));
 		
 		
 		// submit button to check if answers are correct
 		JButton submitButton = new JButton("Submit");
 		submitButton.addActionListener(this);
 		bottomPanel.add(submitButton);
 		
 		
 		// quit to home to exit and go back to Start Screen
 		JButton exitButton = new JButton("Quit to Home");
 		exitButton.addActionListener(this);
 		bottomPanel.add(exitButton);
 		 
 		
 		// the user can guess if what they have is correct
 		JButton guessButton = new JButton("Guess");
 		guessButton.addActionListener(this);
 		bottomPanel.add(guessButton);
 		
 		add(bottomPanel, BorderLayout.SOUTH); // adds all buttons to frame
 		
 		gridPanel.setVisible(true);
 		
 		time.start(); // starts time
 	}	
 	private JMenuItem makeMenuItem(String name){
 		JMenuItem m = new JMenuItem(name);
 		m.addActionListener(this);
 		return m;
 	}
 
 	public void actionPerformed(ActionEvent e) {
 		String buttonCheck = e.getActionCommand();
 		Boolean checkWin;
 		
 		if (buttonCheck.equals("Submit")){
 			time.stop(); // stops time momentarily to check for win
 			
 			// if the user hits "Submit" then the program will check if they have the correct answers
 			checkWin = checkIfWin(); // returns true if correct, false if wrong
 			checkWinWindow(checkWin); // will either throw an error if wrong or diplay a "Win Screen" if right
 		}
 		// will get rid of current window and go back to home
 		else if (buttonCheck.equals("Quit to Home")){
 			dispose();
 			StartMenu newStart = new StartMenu(false);
 			newStart.setVisible(true);
 		}
 		// check the current numbers for correct/incorrect values
 		else if (buttonCheck.equals("Guess")){
 			checkGuessNumbers();
 		}
 		else if(buttonCheck.equals("Open")){
 			System.out.println("Open");
 		}
 		else if(buttonCheck.equals(" RED ")){
 			dispose();
 			GridLayoutPage g1 = new GridLayoutPage(9, 9, getLevel, checkDemovar, true, Color.RED, checkNumColor, getNumColor);
 			g1.setVisible(true);
 		}
 		else if(buttonCheck.equals(" BLUE ")){
 			dispose();
 			GridLayoutPage g1 = new GridLayoutPage(9, 9, getLevel, checkDemovar, true, Color.BLUE, checkNumColor, getNumColor);
 			g1.setVisible(true);
 		}
 		else if(buttonCheck.equals(" GREEN ")){
 			dispose();
 			GridLayoutPage g1 = new GridLayoutPage(9, 9, getLevel, checkDemovar, true, Color.GREEN, checkNumColor, getNumColor);
 			g1.setVisible(true);
 		}
 		else if(buttonCheck.equals(" BLACK ")){
 			dispose();
 			GridLayoutPage g1 = new GridLayoutPage(9, 9, getLevel, checkDemovar, true, Color.BLACK, checkNumColor, getNumColor);
 			g1.setVisible(true);
 		}
 		else if(buttonCheck.equals(" GRAY ")){
 			dispose();
 			GridLayoutPage g1 = new GridLayoutPage(9, 9, getLevel, checkDemovar, true, Color.GRAY, checkNumColor, getNumColor);
 			g1.setVisible(true);
 		}
 		else if(buttonCheck.equals(" ORANGE ")){
 			dispose();
 			GridLayoutPage g1 = new GridLayoutPage(9, 9, getLevel, checkDemovar, true, Color.ORANGE, checkNumColor, getNumColor);
 			g1.setVisible(true);
 		}
 		else if(buttonCheck.equals(" CYAN ")){
 			dispose();
 			GridLayoutPage g1 = new GridLayoutPage(9, 9, getLevel, checkDemovar, true, Color.CYAN, checkNumColor, getNumColor);
 			g1.setVisible(true);
 		}
 		else if(buttonCheck.equals(" PINK ")){
 			dispose();
 			GridLayoutPage g1 = new GridLayoutPage(9, 9, getLevel, checkDemovar, true, Color.pink, checkNumColor, getNumColor);
 			g1.setVisible(true);
 		}
 		else if(buttonCheck.equals(" YELLOW ")){
 			dispose();
 			GridLayoutPage g1 = new GridLayoutPage(9, 9, getLevel, checkDemovar, true, Color.YELLOW, checkNumColor, getNumColor);
 			g1.setVisible(true);
 		}
 		else if(buttonCheck.equals(" red ")){
 			dispose();
 			GridLayoutPage g1 = new GridLayoutPage(9, 9, getLevel, checkDemovar, checkBackColor, getColor, true, Color.RED);
 			g1.setVisible(true);
 		}
 		else if(buttonCheck.equals(" blue ")){
 			dispose();
 			GridLayoutPage g1 = new GridLayoutPage(9, 9, getLevel, checkDemovar, checkBackColor, getColor, true, Color.BLUE);
 			g1.setVisible(true);
 		}
 		else if(buttonCheck.equals(" green ")){
 			dispose();
 			GridLayoutPage g1 = new GridLayoutPage(9, 9, getLevel, checkDemovar, checkBackColor, getColor,  true, Color.GREEN);
 			g1.setVisible(true);
 		}
 		else if(buttonCheck.equals(" black ")){
 			dispose();
 			GridLayoutPage g1 = new GridLayoutPage(9, 9, getLevel, checkDemovar, checkBackColor, getColor, true, Color.BLACK);
 			g1.setVisible(true);
 		}
 		else if(buttonCheck.equals(" gray ")){
 			dispose();
 			GridLayoutPage g1 = new GridLayoutPage(9, 9, getLevel, checkDemovar, checkBackColor, getColor, true, Color.GRAY);
 			g1.setVisible(true);
 		}
 		else if(buttonCheck.equals(" orange ")){
 			dispose();
 			GridLayoutPage g1 = new GridLayoutPage(9, 9, getLevel, checkDemovar, checkBackColor, getColor, true, Color.ORANGE);
 			g1.setVisible(true);
 		}
 		else if(buttonCheck.equals(" cyan ")){
 			dispose();
 			GridLayoutPage g1 = new GridLayoutPage(9, 9, getLevel, checkDemovar, checkBackColor, getColor, true, Color.CYAN);
 			g1.setVisible(true);
 		}
 		else if(buttonCheck.equals(" pink ")){
 			dispose();
 			GridLayoutPage g1 = new GridLayoutPage(9, 9, getLevel, checkDemovar, checkBackColor, getColor, true, Color.pink);
 			g1.setVisible(true);
 		}
 		else if(buttonCheck.equals(" yellow ")){
 			dispose();
 			GridLayoutPage g1 = new GridLayoutPage(9, 9, getLevel, checkDemovar, checkBackColor, getColor, true, Color.YELLOW);
 			g1.setVisible(true);
 		}
 		else if(buttonCheck.equals("Set Number Color Back to Default")){
 			dispose();
 			GridLayoutPage g1 = new GridLayoutPage(9, 9, getLevel, checkDemovar, checkBackColor, getColor, false, null);
 			g1.setVisible(true);
 		}
 		else if(buttonCheck.equals("Set Background Color Back to Default")){
 			dispose();
 			GridLayoutPage g1 = new GridLayoutPage(9, 9, getLevel, checkDemovar, false, null, checkNumColor, getNumColor);
 			g1.setVisible(true);
 		}
 		else{
			moveCounter.setText("Total Moves: " + (++counterPress)); 
 			checkCorrectFormat(e);
			counterPress -= 35;
		}
 	}
 
 	public void checkCorrectFormat(ActionEvent e) {
 		
 		for (int i = 0; i<(9*9); i++){
 			String inputCheck = input[i].getText(); // reads in the input from the grid
 
 			if (!inputCheck.equals("")){
 				try{
 					int number = Integer.parseInt(inputCheck); // trys to parse the string to an integer (only numbers allowed)
 					
 					// if it is a number then will check for: less then 9 and greater then 0
 					if(number>9||number<0){
 						
 						// if all it is an incorrect number it will get removed and an error window will pop up
 						input[i].setText("");
 						ErrorWindow ew = new ErrorWindow("Bad Number");
 						ew.setVisible(true);
 						break;
 					}
 					// if all conditions hold true and the number has been "answered" before through guess then it will be set back to black (if red)
 					else if(!checkIfAnswered[i])
 						input[i].setForeground(Color.BLACK);
 				}
 				// if not a number entered then an error window will pop up informing the user
 				catch(NumberFormatException nfe){
 					ErrorWindow ew = new ErrorWindow("Not Number");
 					ew.setVisible(true);
 					input[i].setText("");
 				}
 			}
 		}
 	}
 	
 	public void timeUpdate(){
 		// starts the clock
 		timeClock.setText("Time: " + ++timeCount);
 	}
 	public void getConstantNumbers(){
 		
 		Scanner inputNumbers = null;
 		int constantCounter = 0;
 		String readInNumbers = "";
 		
 		try{
 			inputNumbers = new Scanner(new FileInputStream("samplesudoku" + randomFile + ".txt")); // trys to open the "constant" number file
 		}
 		catch (Exception e){
 			System.out.println("File Error");
 			System.exit(0);
 		}
 		// reads in each number until it has reached the 81st number
 		while(inputNumbers.hasNextLine() || constantCounter < 81){
 			if (constantCounter == 81)
 				break;
 			
 			readInNumbers = inputNumbers.next();
 			
 			// if the number equals 0 then the program will record and move on
 			if(readInNumbers.equals("0")){
 				constantArray[constantCounter] = "0";
 				constantCounter++;
 			}
 			// if the number is not 0 then the program will record the index in the graph, take the constant value for later use and record how many constant there are
 			else{
 				constantArray[constantCounter] = readInNumbers;
 				indexArray[indexCounter] = constantCounter;
 				indexCounter++;
 				constantCounter++;
 			}
 		}
 		inputNumbers.close();
 			
 	}
 	public void getAnswers(){
 		
 		Scanner inputNumbers = null;
 		int constantCounter = 0;
 		String readInNumbers = "";
 		
 		try{
 			inputNumbers = new Scanner(new FileInputStream("samplesudoku"  + randomFile + "ans.txt")); // trys to open the answers file
 			
 		}
 		catch (Exception e){
 			System.out.println("File Error");
 			System.exit(0);
 		}
 		
 		// reads in each number until it has reached the 81st number
 		while(inputNumbers.hasNextLine() || constantCounter < 81){
 			if (constantCounter == 81)
 				break;
 			
 			
 			// will read in each number until the parameters are meet
 			readInNumbers = inputNumbers.next();
 			answerArray[constantCounter] = readInNumbers;
 			constantCounter++;
 		}
 		inputNumbers.close();
 	}
 	public void checkGuessNumbers(){
 		
 		String getInput = "";
 		int indexCounter = 0;
 		
 		for(int i = 0; i< 81; i++){
 			getInput = input[i].getText(); // obtain the text from the user input
 			
 			
 			// used to check if the number is correct and if the number is not a predefined "constant"
 			if(getInput.equals(answerArray[i])){
 				input[i].setText(answerArray[i]);
 				
 				// checks for constant
 				if(i == indexArray[indexCounter]){
 					input[i].setForeground(Color.BLACK);
 					indexCounter++;
 				}
 				// if all holds then the number guessed is correct and will turn blue and will not be editable
 				else{
 					checkIfAnswered[i] = true;
 					input[i].setForeground(Color.BLUE);
 					input[i].setEditable(false);	
 				}
 			}
 			// if the number was guessed and it wrong it will turn red
 			else if(!getInput.equals(""))
 				input[i].setForeground(Color.RED);
 
 		}
 	}
 	public boolean checkIfWin(){
 		
 		String inputCheck = null;
 		
 		for(int i = 0; i < 81; i++){
 			
 			inputCheck = input[i].getText(); // gets user input
 			
 			// if any single answer is wrong then the whole board is wrong as well
 			if(!inputCheck.equals(answerArray[i])){
 				return false;
 			}
 		}
 		return true; // if everything is right then the user won and will return true;
 	}
 	public void checkWinWindow(Boolean win){
 		
 		// if the user did not win then an error will be thrown by a window
 		if(!win){
 			ErrorWindow ew = new ErrorWindow("Not Win");
 			ew.setVisible(true);
 			time.start();
 		}
 		// if user did win then a new "WinWindow" will pop up
 		else{
			dispose();
 			WinWindow ww = new WinWindow(timeCount, counterPress, getLevel);
 			ww.setVisible(true);
 		}
 	}
 	public void getRandom(){
 		Random random = new Random();
 		randomFile = random.nextInt(2) + 1;
 		System.out.println(randomFile);
 	}
 }
 
 
