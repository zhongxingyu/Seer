 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dialog.ModalityType;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 
 import javax.swing.BorderFactory;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.Timer;
 
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Scanner;
 import java.util.TimeZone;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.PrintWriter;
 
 
 /**
  * This is the panel that the the user types in whatever is shown.
  * It shows the untyped words in blue and the correctly typed words in red.
  * 
  * @author Naoki Mizuno
  *
  */
 // TODO: Create menu
 // TODO: Change the changeUser method so that it shows the existing users (like in the keyboard layout)
 // TODO: Add JLable to the main panel that shows which layout the user is currently using
 
 public class MainTypePanel extends JPanel {
 
 	public static final String DIC_FILENAME = "dic.txt";
     
 	// public static final String LAST_USER = "lastUser.dat";
 	public static final File lastUserFile = new File("lastUser.dat");
 	private User user;
     
     private ArrayList<String> words = new ArrayList<String>();
 	private ArrayList<JPanel> wordPanels = new ArrayList<JPanel>();
     private int totalNumOfLetters = 0;
     private int correctKeyStrokes = 0;
     private int cnt = 0;
     private int words_cnt = 0;
     private int miss = 0;
     private boolean finished = false;
     private boolean restartFlag = false;
     
     // Not make it null because it will cause a NullPointerException when using SpringLayout
     private JLabel currentKeyboardLayout = new JLabel("");
     private JLabel timeElapsed;
     private Timer timer;
     private long startTime = -1;
 
     // Change the color of the each letter
 	private boolean fun = false;
 
 
     public MainTypePanel() {
         super();
         
         // Set up timer for the main window
 		timeElapsed = new JLabel("0.0", JLabel.RIGHT);
 		timeElapsed.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 30));
 		timeElapsed.setOpaque(true);
 		timeElapsed.setPreferredSize(new Dimension(90, 34));
 		timeElapsed.setBorder(BorderFactory.createLoweredBevelBorder());
 	    timer = new Timer(10, new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				DecimalFormat df = new DecimalFormat();
 				df.setMaximumFractionDigits(1);
 				df.setMinimumFractionDigits(1);
 				timeElapsed.setText(df.format((System.nanoTime() - startTime) / 1000000000.0));
 			}
 		});
 	    
         setSize(800, 400);
         setLayout(new FlowLayout(FlowLayout.LEADING));
     }
 
     public void processPressedKey(char pressed) {
 		// Restart when ESCAPE key is pressed twice-in-a-row
 		if (pressed == KeyEvent.VK_ESCAPE) {
 			if (restartFlag) {
 				restartFlag = false;
 				restart();
 			}
 			else
 				restartFlag = true;
 			return;
 		}
 		else
 			restartFlag = false;
 
     	// Don't go any further if it's done
     	if (finished)
     		return;
     	
 		
     	JPanel p = wordPanels.get(words_cnt);
     	JLabel l = (JLabel)(p.getComponent(cnt));
         String s;
         if ((s = l.getText()) != null) {
             // Start timer
             if (startTime == -1) {
             	// Start timer on the MainWindow
             	timer.start();
             	startTime = System.nanoTime();
             }
             
         	// Don't hilight spaces
         	if (s.charAt(0) == '_' && pressed == ' ') {
         		l.setForeground(user.getSettings().getBackgroundColor());
         		correctKeyStrokes++;
         	}
         	else if (pressed == s.charAt(0)) {
                 l.setForeground(user.getSettings().getAlreadyTyped());
                 correctKeyStrokes++;
             }
             else {
             	miss++;
             	l.setForeground(user.getSettings().getMissTypeColor());
             	// This is supposed to emit a beep sound
             	// Toolkit.getDefaultToolkit().beep();
                 return;
             }
         }
         
         cnt++;
         // If it's the end of the word
     	if (pressed == ' ') {
     		words_cnt++;
     		cnt = 0;
     	}
 
         // Finish if the user finishes typing all the words
         // (subtracts 1 because the last word is always a white space)
         if (correctKeyStrokes == totalNumOfLetters  - 1) {
         	// Record the time it took
         	long endTime = System.nanoTime();
         	
         	// Stop the timer in the main window
         	timer.stop();
         	
         	// Set finished to true
         	finished = true;
         	
         	DecimalFormat df = (DecimalFormat)NumberFormat.getNumberInstance();
         	df.setMaximumFractionDigits(user.getSettings().getTimeFractionDigit());
         	double duration = (double)(endTime - startTime) / 1000000000;
         	String message = "Time: " + df.format(duration) + " sec\n";
         	
         	message += "Miss: " + miss + "\n";
         	
         	df.setMaximumFractionDigits(user.getSettings().getSpeedFractionDigit());
         	message += "Speed: " + df.format(totalNumOfLetters / duration) + " keys/sec\n";
         	
         	// Create a Result instance for this round and write to a binary file
         	Result result = new Result(duration, miss, user.getUserName(),
         			user.getSettings().getKeyboardLayout(),
         			Calendar.getInstance(TimeZone.getDefault()).getTimeInMillis());
         	user.getRecords().writeRecords(result);
         	
         	// Show the result
             JOptionPane.showMessageDialog(null, message,
                      "Result", JOptionPane.INFORMATION_MESSAGE);
             
             // Then, show the list of results and where this round falls into
             user.getRecords().showListOfRecords();
         }
         repaint();
     }
 
     /**
 	 * Loads the data where the user exited last time from a
 	 * file named lastUser.dat and put that to the window.
 	 * lastUser.dat contains which user last used, and another file with
 	 * the user's name as a file name will be loaded.
 	 * For example, if the name John was in lastUser.dat, then
 	 * John_settings.dat and John_records.dat will be loaded (if they exist).
 	 */
 	public void loadLastUser() {
 		// Read from the file if it exists
 		if (lastUserFile.exists()) {
 			try {
 				Scanner read = new Scanner(lastUserFile);
 				changeUser(read.nextLine());
 				read.close();
 			}
 			catch (FileNotFoundException e) {
 				e.printStackTrace();
 			}
 		}
 		else {
			// Prompt for a user name
			String newUser = JOptionPane.showInputDialog("It looks like it's the first time this program has been used.\nEnter your user name:");
			changeUser(newUser);
 		}
 		
         // Add a JLabel that shows what keyboard layout is currently selected
 		// This can only be done after the user has been read,
 		// because we don't know what keyboard layout the last user used.
         currentKeyboardLayout.setText(user.getSettings().getKeyboardLayout());
         // Uses the default font's family but set the style and size to specified.
         currentKeyboardLayout.setFont(new Font(user.getSettings().getDefaultFont().getFamily(), Font.PLAIN, 30)); 
         
 		// Do the things that needs to be done after loading the previous user
 		afterLoadingUser();
 	}
 	
 	/**
 	 * These are the things that needs to be done after the previous user's data has been retrieved.
 	 * Importing the settings and the records, reading from the dictionary  and tokenizing
 	 * to prepare for a new round are some of the things.
 	 */
 	private void afterLoadingUser() {
 		// Set the panel's background to whatever the user specified
         setBackground(user.getSettings().getBackgroundColor());
         // Read from dictionary file
         readDic();
         // Tokenize the words read from the file
         tokenize();
 	}
 
 	/**
 	 * Reads in words from a dictionary file and store them into an ArrayList.
 	 * The total number of words is also counted so that it can be used when determining
 	 * when the user successfully finished typing by comparing it with the number of "correctKeyStrokes".
 	 * TODO: Add shuffle and make the words appear randomly
 	 */
 	public void readDic() {
 	    // Add words from dictionary
 	    try {
 	        Scanner input = new Scanner(new File(DIC_FILENAME));
 	        while (input.hasNext())
 	            words.add(input.next());
 	        input.close();
 	    }
 	    catch (FileNotFoundException e) {
 	    	JOptionPane.showMessageDialog(null, "No dictionary file was found",
 	    			"No Dictionary file", JOptionPane.OK_OPTION);
 	    }
 	    
 	    // Shuffle the order of appearance of words when it is set to do so.
 	    if (user.getSettings().isShuffled())
 		    Collections.shuffle(words);
 	
 	    // Find out the total number of letters in the dictionary file
 	    // Add 1 for the white space after the word
 	    for (int i = 0; i < words.size(); i++)
 	    	totalNumOfLetters += words.get(i).length() + 1;
 	}
 
 	/**
 	 * Tokenize the words into chunks of letters that will then be set to the color "toBeTyped".
 	 * Words will first be added to a JPanel so that the word will not be separated when coming to a new line.
 	 * This method will update the panel to the newest state after adding everything to the panel.
 	 */
 	public void tokenize() {
 		// First, clear all the words that are currently on the panel
 		removeAll();
 		wordPanels.clear();
 		
 		// Then, change the background color
 		this.setBackground(user.getSettings().getBackgroundColor());
 	    // Separate the words into chunks of letters
 	    for (int w = 0; w < words.size(); w++) {
 	        String word = words.get(w);
 	        JPanel oneWordPanel = new JPanel();
 	        oneWordPanel.setBackground(user.getSettings().getBackgroundColor());
 	        for (int i = 0; i < word.length(); i++) {
 	            oneWordPanel.add(new JLabel(Character.toString(word.charAt(i))));
 	        }
 	        oneWordPanel.add(new JLabel("_"));
 	        
 	        wordPanels.add(oneWordPanel);
 	    }
 	    
 	    // Add all the elements in the ArrayList to "this" after setting font and color
 	    for (int i = 0; i < wordPanels.size(); i++) {
 	    	JPanel p = wordPanels.get(i);
         	p.setBackground(user.getSettings().getBackgroundColor());
 	    	for (Component c : p.getComponents()) {
 	        	JLabel l = (JLabel)c;
 	        	// Hide '_' by making it the same as the background color
 	        	if (l.getText().charAt(0) == '_')
 	        		l.setForeground(user.getSettings().getBackgroundColor());
 	        	// Randomize the color of the letters
 	        	else if (fun)
 		        	l.setForeground(new Color((int)(Math.random() * 256), (int)(Math.random() * 256), (int)(Math.random() * 256)));
 	        	else
 		        	l.setForeground(user.getSettings().getToBeTyped());
 	        	
 	        	l.setFont(user.getSettings().getDefaultFont());
 	    	}
 	    	this.add(p);
 	    }
 	    
 	    repaint();
 	    revalidate();
 	}
 
 	/**
 	 * Clears the words and prepares for a new round.
 	 * It will shuffle the words in the dictionary file (of course it depends on the "shuffle" parameter).
 	 */
 	public void restart() {
 		// Clear everything
 		this.removeAll();
 		
 		// Reset the timer
 		timer.stop();
 		// Sets text of the JLabel (timeElapsed) to "0.0"
 		timeElapsed.setText("0.0");
 		
 		// Set everything to default value
 		finished = false;
 		cnt = 0;
 		words_cnt = 0;
 		miss = 0;
 		totalNumOfLetters = 0;
 		correctKeyStrokes = 0;
 		startTime = -1;
 		
 		words.clear();
 		wordPanels.clear();
 		readDic();
 		tokenize();
 	}
 	
 	/**
 	 * Shows a dialog so that the user can change the default font.
 	 * The values will then be passed to the user's Settings instance and finally,
 	 * printed to the file so that the user doesn't have to change every time.
 	 */
 	public void changeFont() {
 		user.getSettings().changeFont();
 		// It's questionable whether to re-shuffle the words or not
 		// restart();
 		tokenize();
     }
 	
 	/**
 	 * Changes the current user to the given user name. Also updates the lastUserFile, which contains the name
 	 * of the last user (creates one when it's not found).
 	 * @param userName The new user name that will be switched to.
 	 */
 	public void changeUser(String userName) {
 		user = new User(userName);
 		
 		try {
 			PrintWriter pw = new PrintWriter(lastUserFile);
 			pw.println(userName);
 			pw.flush();
 			pw.close();
 		}
 		catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Shows an input dialog and changes the current user to the input. 
 	 */
 	public void changeUser() {
 		String newUser = "";
 		newUser = JOptionPane.showInputDialog("Current user is: " + user.getUserName() + "\nEnter new user:");
 		// Change the user name if it's a valid value
 		if (newUser != null && !newUser.trim().equals("")) {
 			changeUser(newUser);
 			afterLoadingUser();
 			restart();
 		}
 	}
 	
 	/**
 	 * Changes the keyboard layout to a new layout. The records are stored separately among each layout.
 	 * It makes a copy of the previous layout and compare it with the new layout.
 	 * After successfully changing the keyboard layout, it re-reads the records and starts a new round.
 	 */
 	public void changeKeyboardLayout() {
 		String previous = user.getSettings().getKeyboardLayout();
 		
 		user.getSettings().changeKeyboardLayout(user.getRecords().getExistingKeyboardLayouts());
 		
 		String current = user.getSettings().getKeyboardLayout();
 		
 		if (!current.equals(previous)) {
 			currentKeyboardLayout.setText(current);
 			restart();
 		}
 	}
 	
 	/**
 	 * Shows a dialog that allows user to change the color. A dialog that asks the user which color
 	 * to change is shown first. Then, according to the selection, another dialog appears that allows
 	 * the user to change the selected color. Clicking on cancel will make no changes.
 	 */
 	public void changeColor() {
 		
 		final JDialog dialog = new JDialog();
 		dialog.setTitle("Change color of");
 		dialog.setLocationRelativeTo(null);
 		dialog.setSize(190, 70);
 		dialog.setLayout(new GridLayout(0, 1));
 		
 		// Ask which color the user wants to change
 		final String[] choices = {"Untyped Letters", "Typed Letters", "Misstypes", "Background"};
 		final JComboBox choose = new JComboBox(choices);
 		
 		JPanel buttons = new JPanel() {{
 			final ActionListener click = new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					if (e.getActionCommand().equals("OK")) {
 						dialog.setVisible(false);
 						String choice = (String)choose.getSelectedItem();
 						// Change the selected
 						Color selected;
 						if (choice.equals(choices[0]) && (selected = ColorDialog.chooseColor(user.getSettings().getToBeTyped())) != null)
 							user.getSettings().setToBeTyped(selected);
 						else if (choice.equals(choices[1]) && (selected = ColorDialog.chooseColor(user.getSettings().getAlreadyTyped())) != null)
 							user.getSettings().setAlreadyTyped(selected);
 						else if (choice.equals(choices[2]) && (selected = ColorDialog.chooseColor(user.getSettings().getMissTypeColor())) != null)
 							user.getSettings().setMissTypeColor(selected);
 						else if (choice.equals(choices[3]) && (selected = ColorDialog.chooseColor(user.getSettings().getBackgroundColor())) != null)
 							user.getSettings().setBackgroundColor(selected);
 					}
 					else if (e.getActionCommand().equals("Cancel"))
 						dialog.setVisible(false);
 				}
 			};
 			this.add(new JButton("OK") {{
 				this.addActionListener(click);
 			}});
 			this.add(new JButton("Cancel") {{
 				this.addActionListener(click);
 			}});
 		}};
 		
 		dialog.add(choose);
 		dialog.add(buttons);
 		
 		dialog.setModalityType(ModalityType.DOCUMENT_MODAL);
 		dialog.setVisible(true);
 		
 		tokenize();
 	}
 	
 	/**
 	 * Changes whether to shuffle the words from the dictionary.
 	 * Restarts a new round after changing the settings.
 	 * @param Whether the check box is selected or not.
 	 */
 	public void changeShuffled(boolean isSelected) {
 		user.getSettings().setShuffled(isSelected);
 		restart();
 	}
 	
 	/**
 	 * Change the "fun" parameter in the Settings class. This randomizes the color of the each letters.
 	 * Re-tokenizes the words (so that the change is reflected immediately without restarting).
 	 * @param fun True if "fun", which changes the color of the letters randomly, is on.
 	 */
 	public void changeFun(boolean fun) {
 		this.fun = fun;
 		tokenize();
 	}
 	
 	/**
 	 * Saves the settings to the user's settings file.
 	 */
 	public void saveSettings() {
 		int choice = JOptionPane.showConfirmDialog(null,
 				"Save settings to a file?", null, JOptionPane.YES_OPTION);
 
 		if (choice == 0)
 			user.getSettings().writeSettings();
 	}
 	
 	/**
 	 * Returns the JLabel that has the text showing how much time has passed since the user started typing.
 	 * @return The JLabel containing information about the time it passed since the start.
 	 */
 	public JLabel getTimeElapsedLabel() {
 		return timeElapsed;
 	}
 	
 	/**
 	 * Returns the JLabel that shows the current keyboard layout.
 	 */
 	public JLabel getCurrentKeyboardLayout() {
 		return currentKeyboardLayout;
 	}
 }
