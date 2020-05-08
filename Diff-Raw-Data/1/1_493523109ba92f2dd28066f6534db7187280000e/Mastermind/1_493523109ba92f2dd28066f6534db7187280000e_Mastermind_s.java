 package Mastermind;
 
 import java.io.*;
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 import java.util.Random;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 public class Mastermind extends JFrame implements ActionListener {
     InputButton guesses[][];
     OutputButton clues[][];
     InputButton key[];
     JButton btnEnter;
     int counter;
     int mode;
     String keyVal = new String();
     Random rand = new Random();
 
     public static void main(String args[]) {
         new Mastermind();
     }
 
     public Mastermind() {
         super("Mastermind");
         setupGUI();
         initValues();
         registerListeners();
     }
 
     public void setupGUI() {
         JPanel mainPanel = new JPanel();
         mainPanel.setBackground(Color.LIGHT_GRAY);
         mainPanel.setLayout(new GridLayout(11, 2, 0, 15));
         JPanel keyPanel = new JPanel();
         keyPanel.setBackground(Color.LIGHT_GRAY);
         keyPanel.setLayout(new FlowLayout());
         key = new InputButton[4];
         for (int i=0; i<4; i++) {
             key[i] = new InputButton();
             keyPanel.add(key[i]);
         }
         mainPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
         mainPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
         JPanel guessPanels[] = new JPanel[10];
         JPanel cluePanels[] = new JPanel[10];
         guesses = new InputButton[10][4];
         clues = new OutputButton[10][4];
         for (int i=0; i<10; i++) {
             cluePanels[i] = new JPanel();
             cluePanels[i].setBackground(Color.LIGHT_GRAY);
             cluePanels[i].setLayout(new GridLayout(2,2));
             guessPanels[i] = new JPanel();
             guessPanels[i].setBackground(Color.LIGHT_GRAY);
             for (int j=0; j<4; j++) {
                 clues[i][j] = new OutputButton();
                 cluePanels[i].add(clues[i][j]);
                 guesses[i][j] = new InputButton();
                 guessPanels[i].add(guesses[i][j]);
             }
             mainPanel.add(cluePanels[i]);
             mainPanel.add(guessPanels[i]);
         }
         btnEnter = new JButton("Enter");
         JPanel enterPanel = new JPanel();
         enterPanel.setBackground(Color.LIGHT_GRAY);
         enterPanel.add(btnEnter);
         Container window = this.getContentPane();
         window.setLayout(new BorderLayout());
         window.add(keyPanel, BorderLayout.NORTH);
         window.add(mainPanel, BorderLayout.CENTER);
         window.add(enterPanel, BorderLayout.SOUTH);
 
         this.setDefaultCloseOperation(Mastermind.EXIT_ON_CLOSE);
         this.setSize(325, 700);
         this.setVisible(true);
     }
 
     public void initValues() {
         Object[] options = {"Codebreaker", "Codemaker"};
         mode = JOptionPane.showOptionDialog(null,
                                             "Who would you like to play as?",
                                             "Game Mode",
                                             JOptionPane.YES_NO_OPTION,
                                             JOptionPane.QUESTION_MESSAGE,
                                             null,
                                             options,
                                             options[0]);
         System.out.println(mode);
 
         counter = 9;
 
         for (int i=0;i<4;i++) {
             if (mode == 0) {
                 key[i].visOff();
                 key[i].setCurrColor((rand.nextInt(6) + 1));
             } else {
                 key[i].setCurrColor(0);
                 key[i].editOn();
             }
         }
 
         if (mode == 0) {
             keyVal = stringValue(key);
         }
 
         for (int i=0; i<10; i++) {
             for (int j=0; j<4; j++) {
                 guesses[i][j].setCurrColor(0);
                 if (i==9 && mode == 0) {
                     guesses[i][j].editOn();
                 } else {
                     guesses[i][j].editOff();
                 }
                 clues[i][j].setCurrColor(0);
             }
         }
     }
 
     public String stringValue(InputButton[] buttons) {
         String value = new String();
 
         for (int i=0; i<buttons.length; i++) {
             value += String.format("%d", buttons[i].getCurrColor());
         }
 
         System.out.println(value);
         return value;
     }
 
     public void registerListeners() {
         btnEnter.addActionListener(this);
     }
 
     public void actionPerformed(ActionEvent e) {
         System.out.println(e.getActionCommand());
         if (e.getSource() == btnEnter && mode == 0) {
             submitGuess();
         } else if (e.getSource() == btnEnter && mode == 1) {
             playComp();
         } else {
             System.out.println("ERROR");
         }
     }
 
     public void submitGuess() {
         boolean win = false;
         if (validGuess(guesses[counter])) {
             win = returnClue();
             if (win) {
                 System.out.println("winner");
                 gameOver(true);
                 return;
             }
             counter--;
             if (counter < 0) {
                 gameOver(false);
                 return;
             }
             for (int i=0; i<4; i++) {
                 guesses[counter][i].editOn();
                 guesses[counter+1][i].editOff();
             }
         }
     }
 
     public boolean validGuess(InputButton[] button) {
         for (int i=0; i<4; i++) {
             if (button[i].getCurrColor() == 0) {
                 return false;
             }
         }
         return true;
     }
 
     public void gameOver(boolean winner) {
         for (int i=0; i<4; i++) {
             key[i].visOn();
         }
         String msg = new String();
         if (winner) {
             msg = "You win!";
         } else {
             msg = "You lose!";
         }
         int ans =
             JOptionPane.showConfirmDialog(null,"Do you want to play again?",
                                           msg, JOptionPane.YES_NO_OPTION,
                                           JOptionPane.PLAIN_MESSAGE);
         if (ans == 0) {
             initValues();
         } else if (ans == 1) {
             System.exit(0);
         } else {
             System.out.println("ERROR");
         }
     }
 
     public void playComp() {
         if (validGuess(key)) {
             String guess = new String();
             String[] sc = new String[2];
             ArrayList<String> possible = new ArrayList<String>();
             ArrayList<String> sol = new ArrayList<String>();
 
             for (int i=1111; i<6667; i++) {
                 if ( !String.format("%d", i).contains("0") &&
                      !String.format("%d", i).contains("7") &&
                      !String.format("%d", i).contains("8") &&
                      !String.format("%d", i).contains("9") ) {
                     possible.add(String.format("%d", i));
                     sol.add(String.format("%d", i));
                 }
             }
 
             keyVal = stringValue(key);
             guess = "1122";
             guesses[counter][0].setCurrColor(1);
             guesses[counter][1].setCurrColor(1);
             guesses[counter][2].setCurrColor(2);
             guesses[counter][3].setCurrColor(2);
             sc = score(keyVal, guess).split(",");
             while (counter >= 0 && !sc[0].equals("4")) {
                 submitGuess();
                 for (int i=0; i<sol.size(); i++) {
                     while (i != sol.size() &&
                            !Arrays.deepEquals(sc,
                                               score(sol.get(i),
                                                     guess).split(","))) {
                         sol.remove(i);
                     }
                 }
                 if (sol.size() == 1) {
                     guess = sol.get(0);
                 } else {
                     int minval = 1296;
                     for (int i=0; i<1296; i++) {
                         int tmp = getMax(possible.get(i), sol);
                         if (tmp < minval) {
                             minval = tmp;
                             guess = possible.get(i);
                         }
                     }
                 }
                 for (int i=0; i<4; i++) {
                     guesses[counter][i].setCurrColor(Integer.parseInt(guess.substring(i, i+1)));
                 }
                 sc = score(keyVal, guess).split(",");
             }
 
             if (counter < 0) {
                 gameOver(true);
             } else if (sc[0].equals("4")) {
                 gameOver(false);
             }
         }
     }
 
     public int getMax(String attempt, ArrayList<String> set) {
         String[] scores = {"0,0", "0,1", "0,2", "0,3", "0,4", "1,0", "1,1",
                            "1,2", "1,3", "2,0", "2,1", "2,2", "3,0", "4,0"};
         int max = 0;
 
         for (int i=0; i<scores.length; i++) {
             int cnt = 0;
             for (int j=0; j<set.size(); j++) {
                 if (score(set.get(j), attempt).equals(scores[i])) {
                     cnt++;
                 }
             }
             if (cnt > max) {
                 max = cnt;
             }
         }
         return max;
     }
 
     public boolean returnClue() {
         int exactMatch = 0;
         int colorMatch = 0;
         int result = 0;
         String guessVal = new String();
         String sc[] = new String[2];
 
         guessVal = stringValue(guesses[counter]);
         sc = score(keyVal, guessVal).split(",");
         System.out.println(sc[0] + "," + sc[1]);
 
         for (int i=Integer.parseInt(sc[0]); i>0; i--) {
             clues[counter][result].setCurrColor(2);
             result++;
         }
         for (int i=Integer.parseInt(sc[1]); i>0; i--) {
             clues[counter][result].setCurrColor(1);
             result++;
         }
 
         if (sc[0].equals("4")) {
             return true;
         } else {
             return false;
         }
     }
 
     public String score(String key, String guess) {
         int exactMatch = 0;
         int colorMatch = 0;
 
         for (int i=0; i<4; i++) {
             if (key.charAt(i) == guess.charAt(i)) {
                 exactMatch++;
             }
         }
         for (int i=1; i<7; i++) {
             colorMatch += Math.min(count(key, Character.forDigit(i,10)),
                                    count(guess, Character.forDigit(i,10)));
         }
         colorMatch -= exactMatch;
 
         return exactMatch + "," + colorMatch;
     }
 
     public int count(String value, char x) {
         int cnt = 0;
         for (int i=0; i<value.length(); i++) {
             if (value.charAt(i) == x) {
                 cnt++;
             }
         }
         return cnt;
     }
 }
