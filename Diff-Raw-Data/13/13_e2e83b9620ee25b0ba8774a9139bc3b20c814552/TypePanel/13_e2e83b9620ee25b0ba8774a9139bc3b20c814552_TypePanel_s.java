 package com.nigorojr.typebest;
 
 import javax.swing.BoxLayout;
 import javax.swing.JPanel;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Scanner;
 import java.io.File;
 import java.io.IOException;
 import java.sql.SQLException;
 
 /**
  * This is the panel that the the user types in whatever is shown. It shows the
  * untyped words in blue and the correctly typed words in red.
  * 
  * @author Naoki Mizuno
  * 
  */
 // TODO: Change the changeUser method so that it shows the existing users
 
 @SuppressWarnings("serial")
 public class TypePanel extends JPanel {
 
     private Preferences pref;
     private Records records;
 
     public static final File lastUserFile = new File("lastUser.txt");
 
     private ArrayList<Line> lines = new ArrayList<Line>();
     private Iterator<Line> lineIterator;
     private Line currentLine = null;
     private Word currentWord = null;
     private Letter currentLetter = null;
     private boolean waitForSpace = false;
 
     private int miss = 0;
     private int totalNumOfLetters;
     private long startTime;
     private long endTime;
     private boolean running = false;
 
     private String keyboardLayout;
     private JLabel keyboardLayoutLabel;
 
     public TypePanel() {
         super();
 
         setSize(800, 400);
         setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
 
         try {
             records = new Records();
         }
         catch (SQLException e) {
             e.printStackTrace();
         }
 
         keyboardLayoutLabel = new JLabel("");
     }
 
     /**
      * Attempts to find the last user who used this program and load his/her
      * preferences. If that attempt fails, this method will create a new set of
      * preferences with the default parameters. The lastUserFile must have the
      * username on the first line and the user's ID on the second line. The
      * username will be used when creating a new set of preferences when the ID
      * is not present.
      */
     public void loadPreferences() {
         if (!lastUserFile.exists()) {
             // Prompt for a username
             String message = "It looks like it's the first time this program has been used.\nEnter your user name:";
             String username = JOptionPane.showInputDialog(message);
             try {
                 pref = new Preferences(username);
             }
             catch (SQLException e) {
                 e.printStackTrace();
             }
         }
         else {
             try {
                 Scanner scanner = new Scanner(lastUserFile);
 
                 if (scanner.hasNext()) {
                     String username = scanner.nextLine();
                     if (scanner.hasNext()) {
                         int user_id = Integer.parseInt(scanner.nextLine());
                         pref = new Preferences(user_id, username);
                     }
                     // When there's only 1 line
                     else
                         pref = new Preferences(username);
                 }
                 // When the lastUserFile is an invalid empty file
                 else
                     pref = new Preferences();
             }
             catch (IOException e) {
                 e.printStackTrace();
             }
             catch (SQLException e) {
                 e.printStackTrace();
             }
         }
     }
 
     /**
      * Uses the WordSelector to choose random words and adds them to the lines.
      * This method well then set the current letter to the first letter.
      */
     public void loadLinesAndAddToPanel() {
         // TODO: get modes and number of letters
         ArrayList<String> words = WordSelector.getWords(WordSelector.NORMAL);
         totalNumOfLetters = 0;
         lines.clear();
 
         int panelWidth = this.getWidth();
         Line line = new Line(pref);
         for (int i = 0; i < words.size(); i++) {
             String word = words.get(i);
 
             if (line.isWordWithin(word, panelWidth)) {
                 line.addWord(word);
                 totalNumOfLetters += word.length();
             }
             // If adding the word exceeds the boundary
             else {
                 lines.add(line);
                 line = new Line(pref);
                 i--;
             }
         }
 
         // Add to this panel
         lineIterator = lines.iterator();
         while (lineIterator.hasNext())
             add(lineIterator.next());
         validate();
 
         // Reset lineIterator
         lineIterator = lines.iterator();
         // Set currentLetter to the first letter
         currentLine = lineIterator.next();
         currentWord = currentLine.nextWord();
         currentLetter = currentWord.nextLetter();
     }
 
     /**
      * Accepts the typed letter and checks whether that letter is the correct
      * letter. The foreground color of the current letter is changed
      * accordingly. Because the "spaces" between the words are not added to the
      * lines as JLabels, they are treated separately.
      * 
      * @param pressed
      *            The letter typed.
      */
     public void processPressedKey(char pressed) {
         if (waitForSpace && pressed == ' ') {
             waitForSpace = false;
             // Note: nextLetter() is not called here because the
             // currentLetter is already set
         }
         else if (!waitForSpace) {
             if (currentLetter.isCorrectKeyStroke(pressed)) {
                 currentLetter.setForeground(pref.getAlreadyTyped());
                 nextLetter();
 
                 // Check if this was the last letter
                 if (currentLetter == null)
                     finish();
             }
             else {
                 currentLetter.setForeground(pref.getMissTypeColor());
                 miss++;
             }
         }
         // Mistyped space key
         else
             // miss++;
             ;
 
         repaint();
     }
 
     /**
      * Changes the current letter to the next letter. If there is none, the
      * current letter will be set to null.
      */
     public void nextLetter() {
         if (!currentWord.hasNext()) {
             if (!currentLine.hasNext()) {
                 // If finished everything
                 if (!lineIterator.hasNext()) {
                     currentLetter = null;
                     return;
                 }
                 currentLine = lineIterator.next();
             }
             // Prepare next word
             currentWord = currentLine.nextWord();
             waitForSpace = true;
         }
         currentLetter = currentWord.nextLetter();
     }
 
     /**
      * Process what needs to be done after finishing the round.
      */
     public void finish() {
         // Stop the timer in the main window
         endTime = System.nanoTime();
 
         running = false;
     }
 
     /**
      * Shows a JOptionPane of the result and adds the result to the database.
      */
     public void showResultAndAdd() {
         long time = endTime - startTime;
 
         // Then, show the list of results and where this round falls into
         // user.getRecords().showListOfRecords();
         // Show the result
         DecimalFormat df = (DecimalFormat) NumberFormat.getNumberInstance();
         df.setMaximumFractionDigits(pref.getTimeFractionDigit());
         String message = String.format("Time: %s\nMiss: %d\n",
                 df.format(time / 1000000000.0), miss);
 
         df.setMaximumFractionDigits(pref.getSpeedFractionDigit());
         message += String.format("Speed: %s keys/sec",
                 df.format(totalNumOfLetters / (time / 1000000000.0)));
 
         JOptionPane.showMessageDialog(null, message, "Result",
                 JOptionPane.INFORMATION_MESSAGE);
 
         Record record = new Record(pref.getID(), pref.getUsername(),
                keyboardLayout, time, miss);
 
         records.addNewRecord(record);
     }
 
     public void start() {
         running = true;
         startTime = System.nanoTime();
     }
 
     /**
      * Clears the words and prepares for a new round.
      */
     public void reset() {
         // Clear everything
         this.removeAll();
         repaint();
 
         // Set everything to default value
         running = false;
         miss = 0;
         waitForSpace = false;
         lines.clear();
 
         loadLinesAndAddToPanel();
     }
 
     /**
      * Returns the JLabel that shows the current keyboard layout. TODO: Modify
      * this to get information from the database and then create a JLabel and
      * return that.
      */
     public JLabel getKeyboardLayoutLabel() {
         return keyboardLayoutLabel;
     }
 
     /**
      * Returns the current set of preferences. This is used in the Letter class
      * for setting the foreground/background color of the letters. Make sure
      * that pref is not null.
      * 
      * @return The Preferences used by the current user.
      */
     public Preferences getPreferences() {
         return pref;
     }
 
     /**
      * Returns the Record instance.
      * 
      * @return The Record instance.
      */
     public Records getRecords() {
         return records;
     }
 
     public String getKeyboardLayout() {
         return keyboardLayout;
     }
 
     /**
      * Returns whether the trial is running or not.
      * 
      * @return True if running, false if not.
      */
     public boolean isRunning() {
         return running;
     }
 }
