 import java.awt.*;
 import java.awt.event.*;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.awt.Font;
 import javax.swing.*;
 import javax.swing.text.AbstractDocument;
 import javax.swing.text.Document;
 import javax.swing.text.DocumentFilter;
 import javax.swing.text.PlainDocument;
 
 
 /*TODO: We need to restrict input to jTextArea to numbers only
  *"Finish and Check Answer" should ask sudoku if it is solved correctly, if not 
  *
  * When 
  * 
  */
 public class PlayMenu {
 	private Sudoku sudokuBoard;
 	private JTextField[][] listOfJTextAreaEntries;
 	private HintSystem hintSystem;
 	AbstractDocument doc;
 	int difficulty;
 	private SudokuImporter importer;
   	int[] sudokuFile;
   	boolean showConflicts;
   	JButton saveButton;
   	JButton pauseButton;
   	JButton restartButton;
   	JButton checkButton;
   	JButton hintButton;
   	SudokuBoard board;
   	JCheckBox checkbox;
   	JLabel timeLabel;
   	Timer gameTimer;
   	JLabel highScoreLabel ;
   	
   	long startTime;
   	long highScore; //for output
   	private final static long NOHIGHSCORE = 1999999999;
 	public PlayMenu(Sudoku SudokuBoard) {
 		this.sudokuBoard = SudokuBoard;
 		this.listOfJTextAreaEntries = new JTextField[9][9];
 		this.hintSystem = new HintSystem(SudokuBoard);
 		this.difficulty = SudokuBoard.getDifficulty();
 		importer = new SudokuImporter();
 		sudokuFile = new int[81];
 		startTime = 0;
 		highScore = getHighScore();
 	}
 	
 	public void setHintsLeft (int n){
 		hintSystem.setHintsLeft(n);
 	}
 	
 	public void setStartTime (long time){
 		startTime = time;
 	}
    
 	public void startPlayMenu(final BackgroundJFrame f) {
 		System.out.println("High score: "+ highScore);
 		f.setBackgroundImage("image.jpg");
 		GridBagLayout gridbag = new GridBagLayout();
 		GridBagConstraints c = new GridBagConstraints();
 		
 		f.setLayout(gridbag);
 		c.fill = GridBagConstraints.BOTH;
 		  
 		this.saveQuitButton(f, c);
 		this.pauseButton(f, c);
 		this.restartButton(f, c);
 		this.checkButton(f, c);
 		this.hintButton(f, c);
 		this.sudokuBoard(f, c);
 		this.checkBox(f, c);
 		this.showTimer(f, c);
 		this.showHighScore(f, c); 
 		
 		f.setSize(720,700);
 		f.setVisible(true);
 	}
       
 	private void saveQuitButton(final BackgroundJFrame f, GridBagConstraints c) {
 		saveButton = new JButton("Quit and Save");
 		c.gridx = 0;
 		c.gridy = 0;
 		c.gridwidth = 2;
 		c.gridheight = 1;
 		  
 		f.add(saveButton, c);
 		saveButton.addActionListener(new
 			ActionListener() {
 				public void actionPerformed(ActionEvent event) {
 					try {
 						FileOutputStream fileStream = new FileOutputStream("MyGame.ser");
 						ObjectOutputStream os = new ObjectOutputStream(fileStream);
 
 						os.writeObject(sudokuBoard);
 						os.writeObject(hintSystem.getNumHintsLeft());
 						os.writeObject(gameTimer.getTime());
 						
 						os.close();
 						f.getContentPane().removeAll();
 						SwingUtilities.updateComponentTreeUI(f);
 						MainMenu mainMenu = new MainMenu();
 						mainMenu.startMainMenu(f);
 					} catch (FileNotFoundException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					
 				}
 		});
 	}
 	
 	private void pauseButton(final BackgroundJFrame f, GridBagConstraints c) {
		pauseButton = new JButton("Pause Current Game");
 		c.gridx = 2;
 		c.gridy = 0;
 		c.gridwidth = 2;
 		c.gridheight = 1;
 		  
 		f.add(pauseButton, c);
 		pauseButton.addActionListener(new
 			ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					saveButton.setVisible(false);
 				  	pauseButton.setVisible(false);
 				  	restartButton.setVisible(false);
 				  	checkButton.setVisible(false);
 				  	hintButton.setVisible(false);
 				  	board.setVisible(false);
 				  	checkbox.setVisible(false);
 				  	timeLabel.setVisible(false);
 				  	highScoreLabel.setVisible(false);
 				  	gameTimer.pause();
 				  	
 				  	final JLabel pause = new JLabel("Game Paused");
 					f.getContentPane().add(pause);
 					final JButton returnB = new JButton("Resume Game");
 					returnB.addActionListener(new ActionListener() {
 						
 						@Override
 						public void actionPerformed(ActionEvent arg0) {
 							f.getContentPane().remove(pause);
 							f.getContentPane().remove(returnB);
 							saveButton.setVisible(true);
 						  	pauseButton.setVisible(true);
 						  	restartButton.setVisible(true);
 						  	checkButton.setVisible(true);
 						  	hintButton.setVisible(true);
 						  	board.setVisible(true);
 						  	checkbox.setVisible(true);
 						  	timeLabel.setVisible(true);
 						  	highScoreLabel.setVisible(true);
 						  	gameTimer.resume();
 							
 						}
 					});
 					f.getContentPane().add(returnB);
 					SwingUtilities.updateComponentTreeUI(f);
 					//PausePage paused = new PausePage(f);
 					
 				}
 
 		});
 	}
       
 	private void restartButton(final BackgroundJFrame f, GridBagConstraints c) {
 		restartButton = new JButton("Restart");
 		c.gridx = 4;
 		c.gridy = 0;
 		c.gridwidth = 2;
 		f.add(restartButton, c);
 		restartButton.addActionListener(new
 			ActionListener() {
 				public void actionPerformed(ActionEvent event) {
 					sudokuBoard.resetSudoku();
 					f.getContentPane().removeAll();
 					SwingUtilities.updateComponentTreeUI(f);
 					PlayMenu p = new PlayMenu(sudokuBoard);
 					p.startPlayMenu(f);
 				}
 		});
 	}
       
   
 	private void checkButton(final BackgroundJFrame f, GridBagConstraints c) {
 		checkButton = new JButton("Check Answer");
 		c.gridx = 3;
 		c.gridy = 10;
 		c.gridwidth =1;
 		f.add(checkButton, c);
 		checkButton.addActionListener(new
 			ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					int mistakes = checkProgress();
 					if (mistakes == 0) {
 						JOptionPane.showMessageDialog
         				(null,"All correct so far", null, JOptionPane.PLAIN_MESSAGE);
 					} else {
 						if (mistakes == 1) {
 							JOptionPane.showMessageDialog
 							(null,"Incorrect: There is " + mistakes + " mistake so far", "Incorrect Solution", JOptionPane.PLAIN_MESSAGE);
 						} else {
 							JOptionPane.showMessageDialog
 							(null,"Incorrect: There are " + mistakes + " mistakes so far", "Incorrect Solution", JOptionPane.PLAIN_MESSAGE);
 						}
 					}
 					
 				}
 				
 		});
 	}
       
 	private void hintButton(final BackgroundJFrame f, GridBagConstraints c) {
 		hintButton = new JButton();
 		hintButton.setText("Hint (" + String.valueOf(hintSystem.getNumHintsLeft()) + ")");
 		c.gridx = 1;
 		c.gridy = 10;
 		f.add(hintButton, c);
 		hintButton.addActionListener(new
 			ActionListener() {
 				public void actionPerformed(ActionEvent event) {
 					hintSystem.getHint(sudokuBoard);
 					hintButton.setText("Hint (" + String.valueOf(hintSystem.getNumHintsLeft()) + ")");
 					for(int i = 0; i < 9; i++) {
 						for(int j = 0; j < 9; j++) {
 							if(sudokuBoard.getPlayerSudoku()[i][j] != 0)
 									listOfJTextAreaEntries[i][j].setText(String.valueOf(sudokuBoard.getPlayerSudoku()[i][j]));
 						}
 					}
 					updateUserSudoku(f);
 				}
 		});
 	}
        
 	private void sudokuBoard(final BackgroundJFrame f, GridBagConstraints c) {
 		board = new SudokuBoard(sudokuBoard); 
 		c.gridx = 0;
 		c.gridwidth = 9;
 		c.gridy = 1;
 		c.gridheight = 9;
 		c.weighty = 0.01;
 		c.weightx = 0.01;
 		c.anchor = GridBagConstraints.CENTER;
 		c.insets = new Insets(25,55,25,55);
 		c.fill = GridBagConstraints.BOTH;
 		f.add(board, c);
 		listOfJTextAreaEntries = board.getSubGrids();
 		
 		
 		for(int i = 0; i < 9; i++) {
 			for(int j = 0; j < 9; j++) {
 				final JTextField sudokuArea = listOfJTextAreaEntries[i][j];
 		
 				Font font;
 				if (sudokuBoard.getUnsolvedSudoku()[i][j] != 0) {  //Enter in the details for reset board
 					sudokuArea.setText(Integer.toString(sudokuBoard.getPlayerSudoku()[i][j]));
 					sudokuArea.setEditable(false);
 					font = new Font("Verdana", Font.BOLD, 12);
 				} else {
 					sudokuArea.setEditable(true);
 					font = new Font("Verdana", Font.PLAIN, 12);
 				}
 				
 				if (sudokuBoard.getPlayerSudoku()[i][j] != 0 && sudokuBoard.getUnsolvedSudoku()[i][j] == 0) { //Add in player's details
 					sudokuArea.setText(Integer.toString(sudokuBoard.getPlayerSudoku()[i][j]));
 					font = new Font("Verdana", Font.PLAIN, 12);
 				}
 				
 				Document styleDoc = (sudokuArea).getDocument();
 				if (styleDoc instanceof AbstractDocument) {
 					doc = (AbstractDocument)styleDoc;
 					doc.setDocumentFilter(new InputLimiter());
 				}
 				sudokuArea.setFont(font);
 				sudokuArea.setOpaque(false);
 				sudokuArea.setHorizontalAlignment(JTextField.CENTER);
 				sudokuArea.addKeyListener(new
 						KeyListener() {
 
 					@Override
 					public void keyPressed(KeyEvent arg0) {
 						int keyCode = arg0.getKeyChar();
 						if (!sudokuArea.getText().isEmpty() && sudokuArea.isEditable() && keyCode <= 57 && keyCode >= 49) {
 							sudokuArea.setText("");
 						}
 					}
 
 					@Override
 					public void keyReleased(KeyEvent arg0) {
 						updateUserSudoku(f);
 					}
 
 					@Override
 					public void keyTyped(KeyEvent arg0) {
 						
 					}
 				});
 			}
 		}
 	}
 	
 	private void showTimer(BackgroundJFrame f, GridBagConstraints c) {
 		//start a new thread
 		timeLabel = new JLabel();
 		
 		Font font = new Font("Avenir", Font.BOLD, 16);
 		timeLabel.setFont(font);
 		
 		c.gridx = 8;
 		c.gridy = 10;
 		c.gridwidth = 1;
 		gameTimer = new Timer(f, c, timeLabel);
 		
 		f.add(timeLabel, c);
 		gameTimer.start(startTime);
 	}
 	
 	private void checkBox(BackgroundJFrame f, GridBagConstraints c) {
 		checkbox = new JCheckBox("Show conflicts");
 		c.gridy = 20;
 		c.gridx = 10;
 		checkbox.addItemListener(new
 				ItemListener() {
 
 					@Override
 					public void itemStateChanged(ItemEvent arg0) {
 						if (arg0.getStateChange() == ItemEvent.DESELECTED) {
 							showConflicts = false;
 						} else {
 							showConflicts = true;
 						}
 						
 					}
 			
 		});
 		f.getContentPane().add(checkbox);
 		
 	}
 	
 	private void showHighScore(BackgroundJFrame f, GridBagConstraints c) {
 
 		highScoreLabel = new JLabel();
 		
 		if(highScore == NOHIGHSCORE){
 			highScoreLabel.setText("best time: ---");
 		} else {
 			highScoreLabel.setText("best time: " + highScore/1000/60/60 + "h  " 
 									+ highScore/1000/60%60 + "m  " + highScore/1000%60 + "s");
 		}
 		
 		Font font = new Font("Avenir", Font.BOLD, 16);
 		highScoreLabel.setFont(font);
 		
 		c.gridx = 1;
 		c.gridy = 10;
 		c.gridwidth = 1;
 	
 		
 		f.add(highScoreLabel, c);
 
 	}
 	
 	private void updateUserSudoku(BackgroundJFrame f) {
 		int count = 0;
 		for(int i = 0; i < 9; i++) {
 			for(int j = 0; j < 9; j++) {
 				int userValue = sudokuBoard.getPlayerSudoku()[i][j];
 				int textValue;
 				if (listOfJTextAreaEntries[i][j].getText().isEmpty()) {
 					textValue = 0;
 				} else {
 					textValue = Integer.parseInt(listOfJTextAreaEntries[i][j].getText());
 				}
 				if (listOfJTextAreaEntries[i][j].getText().trim().isEmpty()) {
 					if (sudokuBoard.getPlayerSudoku()[i][j] != 0) {
 						listOfJTextAreaEntries[i][j].setBackground(Color.WHITE);
 						int rowCount = 0;
 						int colCount = 0;
 						int boxCount = 0;
 						for (int l = 0; l < 9; l++) {
 							if (listOfJTextAreaEntries[l][j].getText().compareTo(Integer.toString(userValue)) == 0) {
 								rowCount++;
 							}
 							if (listOfJTextAreaEntries[i][l].getText().compareTo(Integer.toString(userValue)) == 0) {
 								colCount++;
 							}
 						}
 						int row = SudokuBoard.getBoxRowCoordinate(sudokuBoard.findBoxNum(i, j));
 						int col = SudokuBoard.getBoxColCoordinate(sudokuBoard.findBoxNum(i, j));
 						for (int r = row; r < row + 3; r++) {
 							for (int c = col; c < col + 3; c++) {
 								if (listOfJTextAreaEntries[r][c].getText().compareTo(Integer.toString(userValue)) == 0) {
 									boxCount++;
 								}
 							}
 						}
 						if (rowCount == 0) {
 							sudokuBoard.setRowUserValueConditions(i, j, userValue, false);
 						} else if (rowCount == 1) {
 							//make the other box turn white
 						}
 						if (colCount == 0) {
 							sudokuBoard.setColUserValueConditions(i, j, userValue, false);
 						} else if (colCount == 1) {
 							//make the other box turn white
 						}
 						if (boxCount == 0) {
 							sudokuBoard.setBoxUserValueConditions(i, j, userValue, false);
 						} else if (boxCount == 1) {
 							//make the other box turn white
 						}
 					}
 				} else {
 					if (userValue != textValue) {
 						listOfJTextAreaEntries[i][j].setBackground(Color.WHITE);
 						if (userValue != 0) {
 							int rowCount = 0;
 							int colCount = 0;
 							int boxCount = 0;
 							for (int l = 0; l < 9; l++) {
 								if (listOfJTextAreaEntries[l][j].getText().compareTo(Integer.toString(userValue)) == 0) {
 									rowCount++;
 								}
 								if (listOfJTextAreaEntries[i][l].getText().compareTo(Integer.toString(userValue)) == 0) {
 									colCount++;
 								}
 							}
 							int row = SudokuBoard.getBoxRowCoordinate(sudokuBoard.findBoxNum(i, j));
 							int col = SudokuBoard.getBoxColCoordinate(sudokuBoard.findBoxNum(i, j));
 							for (int r = row; r < row + 3; r++) {
 								for (int c = col; c < col + 3; c++) {
 									if (listOfJTextAreaEntries[r][c].getText().compareTo(Integer.toString(userValue)) == 0) {
 										boxCount++;
 									}
 								}
 							}
 							if (rowCount == 0) {
 								sudokuBoard.setRowUserValueConditions(i, j, userValue, false);
 								listOfJTextAreaEntries[i][j].setBackground(Color.WHITE);
 							} else if (rowCount == 1) {
 								listOfJTextAreaEntries[i][j].setBackground(Color.WHITE);
 								//make the other box turn white
 							}
 							if (colCount == 0) {
 								sudokuBoard.setColUserValueConditions(i, j, userValue, false);
 								listOfJTextAreaEntries[i][j].setBackground(Color.WHITE);
 							} else if (colCount == 1) {
 								listOfJTextAreaEntries[i][j].setBackground(Color.WHITE);
 								//make the other box turn white
 							}
 							if (boxCount == 0) {
 								sudokuBoard.setBoxUserValueConditions(i, j, userValue, false);
 								listOfJTextAreaEntries[i][j].setBackground(Color.WHITE);
 							} else if (boxCount == 1) {
 								listOfJTextAreaEntries[i][j].setBackground(Color.WHITE);
 								//make the other box turn white
 							}
 						}
 						if (sudokuBoard.checkRowUserValueConditions(i, j, textValue) == true) {
 							for (int k = 0; k < 9; k++) {
 								if (listOfJTextAreaEntries[k][j].getText().compareTo(Integer.toString(textValue)) == 0) {
 									listOfJTextAreaEntries[k][j].setBackground(Color.RED);
 								}
 							}
 						} else {
 							sudokuBoard.setRowUserValueConditions(i, j, textValue, true);
 						}
 						if (sudokuBoard.checkColUserValueConditions(i, j, textValue) == true) {
 							for (int k = 0; k < 9; k++) {
 								if (listOfJTextAreaEntries[i][k].getText().compareTo(Integer.toString(textValue)) == 0) {
 									listOfJTextAreaEntries[i][k].setBackground(Color.RED);
 								}
 							}
 						} else {
 							sudokuBoard.setColUserValueConditions(i, j, textValue, true);
 						}
 						if (sudokuBoard.checkBoxUserValueConditions(i, j, textValue) == true) {
 							int row = SudokuBoard.getBoxRowCoordinate(sudokuBoard.findBoxNum(i, j));
 							int col = SudokuBoard.getBoxColCoordinate(sudokuBoard.findBoxNum(i, j));
 							for (int r = row; r < row + 3; r++) {
 								for (int c = col; c < col + 3; c++) {
 									if (listOfJTextAreaEntries[r][c].getText().compareTo(Integer.toString(textValue)) == 0) {
 										listOfJTextAreaEntries[r][c].setBackground(Color.RED);
 									}
 								}
 							}
 						} else {
 							sudokuBoard.setBoxUserValueConditions(i, j, textValue, true);
 						}
 						
 					}
 				}
 				if (listOfJTextAreaEntries[i][j].getText().trim().isEmpty()) {
 					sudokuBoard.getPlayerSudoku()[i][j] = 0;
 					count++;
 				} else {
 					sudokuBoard.getPlayerSudoku ()[i][j] = new Integer(listOfJTextAreaEntries[i][j].getText());
 				}
 			}
 		}
 		//Also, we need to set it back to false if we change a number
 		
 		if (count == 0) {
 			int mistakes = this.isCorrect();
 			if (mistakes == 0) {
 				//save high score
 				saveHighScore(gameTimer.getTime());
 				ImageIcon icon = new ImageIcon("icon.gif");
 				int query = JOptionPane.showConfirmDialog (null, 
                         "<html><font size=\"20\" face" +
                         "=\"Papyrus\">Congratulations!</font><br><font size=\"10\" face" +
                         "=\"Papyrus\">Would you like to start a new game?</font></html>", 
                        "You Win", 
                         JOptionPane.YES_NO_OPTION,
                         JOptionPane.QUESTION_MESSAGE,
                         icon);
 				 if (query == JOptionPane.YES_OPTION){
 					 sudokuFile = importer.readSudoku(difficulty);
 						sudokuBoard.initSudoku(sudokuFile, difficulty);
 						f.getContentPane().removeAll();
 						SwingUtilities.updateComponentTreeUI(f);
 						PlayMenu p = new PlayMenu(sudokuBoard);
 						p.startPlayMenu(f);
 				 } else if (query == JOptionPane.NO_OPTION) {
 				     f.getContentPane().removeAll();
 				     SwingUtilities.updateComponentTreeUI(f);
 				     MainMenu mainMenu = new MainMenu();
 				     mainMenu.startMainMenu(f);
 				 }
 			} else {
 				if (mistakes == 1) {
 					JOptionPane.showMessageDialog
 					(null,"Incorrect: There is " + mistakes + " mistake", "Incorrect Solution", JOptionPane.PLAIN_MESSAGE);
 				} else {
 					JOptionPane.showMessageDialog
 					(null,"Incorrect: There are " + mistakes + " mistakes", "Incorrect Solution", JOptionPane.PLAIN_MESSAGE);
 				}
 			}
 		}
 		sudokuBoard.printSudoku();
 		System.out.println();
 	}
 	
 	private int isCorrect() {
 		int mistakes = 0;
 		for (int i = 0; i < 9; i++) {
 			for (int j = 0; j < 9; j++) {
 				if (sudokuBoard.getPlayerSudoku()[i][j] != sudokuBoard.getSolvedSudoku()[i][j]) {
 					mistakes++;
 				}
 			}
 		}
 		return mistakes;
 	}
 	
 	private int checkProgress() {
 		int mistakes = 0;
 		for (int i = 0; i < 9; i++) {
 			for (int j = 0; j < 9; j++) {
 				if (sudokuBoard.getPlayerSudoku()[i][j] != 0) {
 					if (sudokuBoard.getPlayerSudoku()[i][j] != sudokuBoard.getSolvedSudoku()[i][j]) {
 						mistakes++;
 					}
 				}
 			}
 		}
 		return mistakes;
 	}
 	
 	private long getHighScore(){
 		try {
 			FileInputStream fileStream;
 			
 			if(sudokuBoard.getDifficulty() == 1){
 				fileStream = new FileInputStream("HighScore1.ser");
 			} else if (sudokuBoard.getDifficulty() == 2){
 				fileStream = new FileInputStream("HighScore2.ser");
 			} else {
 				fileStream = new FileInputStream("HighScore3.ser");
 			}
 			
 			ObjectInputStream os = new ObjectInputStream(fileStream);
 			Object one = os.readObject();
 	
 			long highScore = (long) one;
 			os.close();
 			return highScore;
 	
 		} catch (ClassNotFoundException | IOException e) {
 			e.printStackTrace();
 		}
 		
 		return NOHIGHSCORE;
 	}
 	
 	private boolean saveHighScore(long newScore){
 		
 		if(newScore < highScore) { //if its less than high Score
 			try {
 				FileOutputStream fileStream;
 				System.out.println("!!");
 				if(sudokuBoard.getDifficulty() == 1){
 					System.out.println("Error1");
 					fileStream = new FileOutputStream("HighScore1.ser");
 				} else if (sudokuBoard.getDifficulty() == 2){
 					fileStream = new FileOutputStream("HighScore2.ser");
 				} else {
 					System.out.println("Error2");
 					fileStream = new FileOutputStream("HighScore3.ser");
 				}
 				
 				
 				ObjectOutputStream os = new ObjectOutputStream(fileStream);
 				os.writeObject(newScore);
 
 				os.close();
 				return true;
 				
 			} catch (FileNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		
 		return false;
 	}
 }
