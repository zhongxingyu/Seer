 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.SwingUtilities;
 
 
 public class HighScoreMenu {
 
 	//private Sudoku SudokuBoards;
   	int[] sudokuFile;
   	private final static long NOHIGHSCORE = 1999999999;
   	private JLabel highScoreLabel1;
   	private JLabel highScoreLabel2;
   	private JLabel highScoreLabel3;
   	
 	/**
 	 * starts up the High Score meny
 	 * @param f
 	 */
 	public void startHighScoreMenu(final BackgroundJFrame f) {
 		f.setBackgroundImage("image.jpg");
 		GridBagLayout gridbag = new GridBagLayout();
 		GridBagConstraints c = new GridBagConstraints();
 
 		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		f.setLayout(gridbag);
 
 		c.fill = GridBagConstraints.BOTH;
 
 		this.easyLabel(f, c);
 		this.mediumLabel(f, c);
 		this.hardLabel(f, c);
 		this.showEasyHighScore(f, c);
 		this.showMediumHighScore(f, c);
 		this.showHardHighScore(f, c);
 		this.resetEasyScoreButton(f, c);
 		this.resetMediumScoreButton(f, c);
 		this.resetHardScoreButton(f, c);
 		this.backButton(f, c);

 		//f.pack();
 		f.setVisible(true);
 	}
      /**
       * Label for "easy" 
       * @param f
       * @param c
       */
 	private void easyLabel(BackgroundJFrame f, GridBagConstraints c) {
 
 		JLabel highScoreLabel = new JLabel("   Easy");
 
 		Font font = new Font("Avenir", Font.BOLD, 16);
 		highScoreLabel.setFont(font);
 
 		c.gridx = 0;
 		c.gridy = 0;
 		c.gridwidth = 1;
 
 		f.add(highScoreLabel, c);
 	}
 
     /**
      * Label for "medium" 
      * @param f
      * @param c
      */
 	private void mediumLabel(BackgroundJFrame f, GridBagConstraints c) {
 
 		JLabel highScoreLabel = new JLabel(" Medium");
 
 		Font font = new Font("Avenir", Font.BOLD, 16);
 		highScoreLabel.setFont(font);
 
 		c.gridx = 1;
 		c.gridy = 0;
 		c.gridwidth = 1;
 
 		f.add(highScoreLabel, c);
 	}
 
     /**
      * Label for "hard" 
      * @param f
      * @param c
      */
 	private void hardLabel(BackgroundJFrame f, GridBagConstraints c) {
 
 		JLabel highScoreLabel = new JLabel("    Hard");
 
 		Font font = new Font("Avenir", Font.BOLD, 16);
 		highScoreLabel.setFont(font);
 
 		c.gridx = 2;
 		c.gridy = 0;
 		c.gridwidth = 1;
 
 		f.add(highScoreLabel, c);
 	}
 
 
 	/**
 	 * Simple shows the easy high score given to it from getHighScore method. 
 	 * @param f
 	 * @param c
 	 */
 	private void showEasyHighScore(BackgroundJFrame f, GridBagConstraints c) {
 
 		highScoreLabel1 = new JLabel();
 		c.gridx = 0;
 		c.gridy = 1;
 		c.gridwidth = 1;
 
 
 		long highScore = getHighScore(1);
 
 		if(highScore == NOHIGHSCORE){
 			highScoreLabel1.setText("      ---");
 		} else {
 			highScoreLabel1.setText("  " + highScore/1000/60/60 + "h  " 
 									+ highScore/1000/60%60 + "m  " + highScore/1000%60 + "s");
 		}
 
 		Font font = new Font("Avenir", Font.BOLD, 14);
 		highScoreLabel1.setFont(font);
 
 		f.add(highScoreLabel1, c);
 	}
 
 	/**
 	 * Simple shows the medium high score given to it from getHighScore method. 
 	 * @param f
 	 * @param c
 	 */
 	private void showMediumHighScore(BackgroundJFrame f, GridBagConstraints c) {
 
 		highScoreLabel2 = new JLabel();
 		c.gridx = 1;
 		c.gridy = 1;
 		c.gridwidth = 1;
 
 
 		long highScore = getHighScore(2);
 
 		if(highScore == NOHIGHSCORE){
 			highScoreLabel2.setText("      ---");
 		} else {
 			highScoreLabel2.setText("  " + highScore/1000/60/60 + "h  " 
 									+ highScore/1000/60%60 + "m  " + highScore/1000%60 + "s");
 		}
 
 		Font font = new Font("Avenir", Font.BOLD, 14);
 		highScoreLabel2.setFont(font);
 
 		f.add(highScoreLabel2, c);
 	}
 
 	/**
 	 * Simple shows the hard high score given to it from getHighScore method. 
 	 * @param f
 	 * @param c
 	 */
 	private void showHardHighScore(BackgroundJFrame f, GridBagConstraints c) {
 
 		highScoreLabel3 = new JLabel();
 		c.gridx = 2;
 		c.gridy = 1;
 		c.gridwidth = 1;
 
 		long highScore = getHighScore(3);
 
 		if(highScore == NOHIGHSCORE){
 			highScoreLabel3.setText("      ---");
 		} else {
 			highScoreLabel3.setText("  " + highScore/1000/60/60 + "h  " 
 									+ highScore/1000/60%60 + "m  " + highScore/1000%60 + "s");
 		}
 
 		Font font = new Font("Avenir", Font.BOLD, 14);
 		highScoreLabel3.setFont(font);
 
 		f.add(highScoreLabel3, c);
 	}
 
 	/**
 	 * Gets the high score from the difficulties input file "HIghScore(1-3).ser".
 	 * @return the high score retrieved
 	 */
 	private long getHighScore(int difficulty){
 		try {
 
 			FileInputStream fileStream;
 
 			if (difficulty == 1){
 				fileStream = new FileInputStream("HighScore1.ser");
 			} else if (difficulty == 2){
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
 
 	/**
 	 * Resets the easy high score by replacing the score with NOHIGHSCORE.
 	 * @param f
 	 * @param c
 	 */
 	private void resetEasyScoreButton (final BackgroundJFrame f, GridBagConstraints c) {
 		JButton exitButton = new JButton("Reset");
 		c.gridx = 0;
 		c.gridy = 2;
 		f.add(exitButton, c);
 		exitButton.addActionListener(new
 			ActionListener() {
 				public void actionPerformed(ActionEvent event) {
 					try {
 						FileOutputStream fileStream = new FileOutputStream("HighScore1.ser");
 						ObjectOutputStream os = new ObjectOutputStream(fileStream);
 
 						os.writeObject(NOHIGHSCORE);
 						os.close();
 						highScoreLabel1.setText("      ---");
 
 					} catch (FileNotFoundException e) {
 						e.printStackTrace();
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 
 				}
 		});
 
 	}
 
 	/**
 	 * Resets the medium high score by replacing the score with NOHIGHSCORE.
 	 * @param f
 	 * @param c
 	 */
 	private void resetMediumScoreButton (final BackgroundJFrame f, GridBagConstraints c) {
 		JButton exitButton = new JButton("Reset");
 		c.gridx = 1;
 		c.gridy = 2;
 		f.add(exitButton, c);
 		exitButton.addActionListener(new
 			ActionListener() {
 				public void actionPerformed(ActionEvent event) {
 					try {
 						FileOutputStream fileStream = new FileOutputStream("HighScore2.ser");
 						ObjectOutputStream os = new ObjectOutputStream(fileStream);
 
 						os.writeObject(NOHIGHSCORE);
 						os.close();
 						highScoreLabel2.setText("      ---");
 
 					} catch (FileNotFoundException e) {
 						e.printStackTrace();
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 
 				}
 		});
 
 	}
 
 	/**
 	 * Resets the hard high score by replacing the score with NOHIGHSCORE.
 	 * @param f
 	 * @param c
 	 */
 	private void resetHardScoreButton (final BackgroundJFrame f, GridBagConstraints c) {
 		JButton exitButton = new JButton("Reset");
 		c.gridx = 2;
 		c.gridy = 2;
 		f.add(exitButton, c);
 		exitButton.addActionListener(new
 			ActionListener() {
 				public void actionPerformed(ActionEvent event) {
 					try {
 						FileOutputStream fileStream = new FileOutputStream("HighScore3.ser");
 						ObjectOutputStream os = new ObjectOutputStream(fileStream);
 
 						os.writeObject(NOHIGHSCORE);
 						os.close();
 						highScoreLabel3.setText("      ---");
 
 					} catch (FileNotFoundException e) {
 						e.printStackTrace();
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 
 				}
 		});
 	}
 
 	/**
 	 * Loads up the main menu. 
 	 * @param f
 	 * @param c
 	 */
 	private void backButton(final BackgroundJFrame f, GridBagConstraints c) {
 		JButton exitButton = new JButton("Back");
 		c.gridx = 1;
 		c.gridy = 4;
 		c.gridheight = 2;
 		f.add(exitButton, c);
 		exitButton.addActionListener(new
 			ActionListener() {
 				public void actionPerformed(ActionEvent event) {
 					f.getContentPane().removeAll();
 					SwingUtilities.updateComponentTreeUI(f);
 					MainMenu mainMenu = new MainMenu();
 					mainMenu.startMainMenu(f);
 				}
 		});
 	}
 }
