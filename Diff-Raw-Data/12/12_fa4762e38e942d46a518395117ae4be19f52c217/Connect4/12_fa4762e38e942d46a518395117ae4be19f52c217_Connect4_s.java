 import java.awt.*;
 import java.io.*;
 import java.util.EventObject;
 import javax.swing.*;
 
 
 public class Connect4 {
     //Boord used in this game
     //The grids & labels in the board
     private static JButton[][] squares = new JButton[8][8];
     private static JButton resetButton = new JButton("Reset");
     private static JLabel winnerlbl = new JLabel();
     private static JLabel turnlbl = new JLabel();
     private static JLabel gameName = new JLabel("Connect 4");
     private static int[][] board = new int[8][8];            //game board [row][column]
     private static JLabel scoreTitle = new JLabel("Game wins");
     private static JLabel botWins = new JLabel("Bot: ");
     private static JLabel playerWins = new JLabel("Player: ");
         
     //GridColors
     private static ImageIcon blankButton = new ImageIcon("blank.png");
     private static ImageIcon blueButton = new ImageIcon("blue.png");
     private static ImageIcon redButton = new ImageIcon("red.png");
     private static ImageIcon blueButtonLight = new ImageIcon("bluel.png");
     private static ImageIcon redButtonLight = new ImageIcon("redl.png");
     
     //marks if the game is in progress or a player has won
     private static boolean isWon = false;
     private static int[][] winningCells = new int[4][2];
     private static final int AMIMATION_TIME = 100; //block drop animation in milliseconds
     
     public static void main(String[] args) throws InterruptedException {
         Player p = new Player();                   // bot
         drawGui();
         
         boolean firstMove = false;        
         while(!isWon){
             
             if(firstMove) {                                  //DELTE AFTER TESTING LINE IS ONLY USED TO SIMULATE COMPUTER GOING FIRST
                 makeMove(10, 4);
                 firstMove = false;
             }                                     
             
                 userTurn();                                  // user makes move
             if(!isWon){
                 makeMove(10, p.move(board, 10));                 // bot makes move (playerNumber, column)
             }
         }
     }
     
     //returns true is there is a 4 in a row at the latest point where the color was added
     public static boolean isWin(int row, int column){
         return isWinHelper(row, column, 1, -1) || //Check diagonal up 
             isWinHelper(row, column, 1, 1) ||     //Check diagonal down
             isWinHelper(row, column, 1, 0) ||     //Check horizontal
             isWinHelper(row, column, 0, 1);       //Check vertical
     }
 
     public static boolean isWinHelper(int row, int column, int stepX, int stepY){        
         int playerSum = 0;
         int player = board[row][column];
         
         addWinningCell(row, column);
         
         for(int i=1;i<4;i++){
             if((row + (stepY*i)) < 0 || (row + (stepY*i)) > 7){
                 break;
             }
             if((column + (stepX*i)) < 0 || (column + (stepX*i)) > 7){
                 break;
             }
             //debug variable
             int a = row + (stepY*i);
             int b = column + (stepX*i);
             
             if(board[row + (stepY*i)][column + (stepX*i)] == player){
                 addWinningCell(row + (stepY*i), column + (stepX*i));
                 playerSum += board[row + (stepY*i)][column + (stepX*i)];
             }else{
                 break;
             } 
         }
         
         stepX *= -1;
         stepY *= -1;
         for(int i=1;i<4;i++){
             if((row + (stepY*i)) < 0 || (row + (stepY*i)) > 7){
                 break;
             }
             if((column + (stepX*i)) < 0 || (column + (stepX*i)) > 7){
                 break;
             }
             //debug variable
             int a = row + (stepY*i);
             int b = column + (stepX*i);
             
             if(board[row + (stepY*i)][column + (stepX*i)] == player){
                 addWinningCell(row + (stepY*i), column + (stepX*i));
                 playerSum += board[row + (stepY*i)][column + (stepX*i)];
             }else{
                 break;
             }           
         }
         
         if(playerSum%10 >= 3){
             return true;
         }else if(playerSum/10 >= 3){
             return true;
         }else{
             resetWinningCell();
         }
         
         return false;
     }
     
     //Draws the windows and the board when called. Call only once at start of program!
     public static void drawGui(){
         ImageIcon blankButton = new ImageIcon("src/blank.png");
         ImageIcon blueButton = new ImageIcon("src/blue.png");
         ImageIcon redButton = new ImageIcon("src/red.png");
 
         //Updates the score board
         updateGuiScore();
         
         //Create Gui Frame
         JFrame frame = new JFrame("Connect 4");
         frame.setSize(537, 695);
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.setBackground(new Color(255, 240, 180));
         
         
         for(int i=0; i<squares.length; i++){
             for(int j=0; j<squares.length; j++){
                 if(board[i][j] == 1){
                     squares[i][j] = new JButton(redButton);
                 }else if(board[i][j] == 10){
                     squares[i][j] = new JButton(blueButton);
                 }else{
                     squares[i][j] = new JButton(blankButton);
                 }
                 JBox.setSize(squares[i][j], 65, 65);
                 squares[i][j].setIconTextGap(0);
             }  
         }
         
         
         JBox body = JBox.vbox(
                               JBox.hbox(
                                         JBox.hglue(),
                                         JBox.hbox(gameName),
                                         JBox.hglue()),
                               JBox.vglue(),
                               JBox.hbox(
                                         squares[0][0], squares[0][1], squares[0][2], squares[0][3],
                                         squares[0][4], squares[0][5], squares[0][6], squares[0][7]
                                        ),
                               JBox.hbox(
                                         squares[1][0], squares[1][1], squares[1][2], squares[1][3],
                                         squares[1][4], squares[1][5], squares[1][6], squares[1][7]
                                        ),
                               JBox.hbox(
                                         squares[2][0], squares[2][1], squares[2][2], squares[2][3],
                                         squares[2][4], squares[2][5], squares[2][6], squares[2][7]
                                        ),
                               JBox.hbox(
                                         squares[3][0], squares[3][1], squares[3][2], squares[3][3],
                                         squares[3][4], squares[3][5], squares[3][6], squares[3][7]
                                        ),
                               JBox.hbox(
                                         squares[4][0], squares[4][1], squares[4][2], squares[4][3],
                                         squares[4][4], squares[4][5], squares[4][6], squares[4][7]
                                        ),
                               JBox.hbox(
                                         squares[5][0], squares[5][1], squares[5][2], squares[5][3],
                                         squares[5][4], squares[5][5], squares[5][6], squares[5][7]
                                        ),
                               JBox.hbox(
                                         squares[6][0], squares[6][1], squares[6][2], squares[6][3],
                                         squares[6][4], squares[6][5], squares[6][6], squares[6][7]
                                        ),
                               JBox.hbox(
                                         squares[7][0], squares[7][1], squares[7][2], squares[7][3],
                                         squares[7][4], squares[7][5], squares[7][6], squares[7][7]
                                        ),
                               JBox.vglue(),
                               JBox.hbox(
                                         resetButton, 
                                         JBox.hglue(),
                                         JBox.vbox(
                                                     scoreTitle,
                                                     botWins,
                                                     playerWins
                                                 ),
                                         JBox.hglue(),
                                         turnlbl, 
                                         winnerlbl
                                        ),
 
                               JBox.vglue()
                              );
         gameName.setFont(new Font("Connect 4", Font.BOLD, 48));
         turnlbl.setFont(new Font("", Font.ITALIC, 30));
         winnerlbl.setFont(new Font("", Font.ITALIC, 30));
         frame.add(body);
         frame.setVisible(true);
         
     }
     
     //call this whenever you update the board to redraw the grids
     public static void updateGui(boolean reset){
         if(reset) {
             for(int i=0; i<board.length; i++){
                 for(int j=0; j<board[i].length; j++){
                     board[i][j] = 0;
                     squares[i][j].setIcon(blankButton);
                     squares[i][j].setEnabled(true);
                 }
             }
             winnerlbl.setText("");
             isWon = false;
             
             
         } else {
             for(int i=0; i<squares.length; i++){
                 for(int j=0; j<squares.length; j++){
                     if(board[i][j] == 1){
                         squares[i][j].setIcon(redButton);
                     }else if(board[i][j] == 10){
                         squares[i][j].setIcon(blueButton);
                     }else{
                         squares[i][j].setIcon(blankButton);
                     }
                     JBox.setSize(squares[i][j], 65, 65);
                     squares[i][j].setIconTextGap(0);
                 }  
             }
         }
         
         //Updates the score board
         updateGuiScore();
     }
     
     //wait for user input
     public static void userTurn(){
         if(!isWon)
             turnlbl.setText("User Turn...");
         //Event listeners
         JEventQueue events = new JEventQueue();
         events.listenTo(resetButton,"resetButton");
         for(int i=0; i<squares.length; i++){
             for(int j=0; j<squares.length; j++){
                 events.listenTo(squares[i][j], "box|"+i+"|"+j);
             }  
         }
         
         while(true){
             EventObject event = events.waitEvent();
             String name = events.getName(event);
             if (name.substring(0, 3).equals("box")) {
                 //get the coordinates
                 int column = Integer.parseInt(name.split("[|]+")[2]);

                //Change the board
                makeMove(1, column);
                break;
             }
             if(name.equals("resetButton")) {
                 updateGui(true);
             }
         }
         if(!isWon)
             turnlbl.setText("Bot Turn...");
     }
     
     public static void makeMove(int Player, int column){                    // Vaeman
         int row =0;
         
         //Sets the next available row
         while(row<7){
             if(Player == 1){
                 squares[row][column].setIcon(redButton);
             }else{
                 squares[row][column].setIcon(blueButton);
             }
             try {
                 Thread.sleep(AMIMATION_TIME);
             } catch (InterruptedException ex) {
                 
             }
             row++;
             squares[row-1][column].setIcon(blankButton);
             if (board[row][column] != 0) {
                 row--;
                 break;
             }
         }
         makeMove(Player, column, row);
     }
     
     public static void makeMove(int player, int column, int row){
         board[row][column] = player;        
         //Detect a win
         if (isWin(row, column)) {
             //Sets the win message
             turnlbl.setText(" ");
             if(player == 1){
                 highlightWinningCells(1);
                 recordScore(1);
                 winnerlbl.setText("You have won! "); 
             }else if(player == 10){
                 recordScore(10);
                 highlightWinningCells(10);
                 winnerlbl.setText("Bot has won! ");
             }
             
             //Disables all buttons
             for(int i=0; i<8; i++){
                 for(int j=0; j<8; j++){
                     squares[i][j].setEnabled(true);
                 }
             }
             
             // Sets isWon flag to true to halt game
             isWon = true;
             if(isWon) {
                 try {                                         // this will pause execution for 100 milliseconds (0.1 sec)
                     Thread.sleep(2500);                         
                 } catch(InterruptedException ex) {
                     Thread.currentThread().interrupt();
                 }
                 updateGui(true);
             }
             // Debuging thing
             System.out.println("Win detected!");
             return;
         }  
         
         //Updates the gui to relfect changes in the board
         updateGui(false);
     }
     
     public static void addWinningCell(int row, int column){
         for(int i=0;i<4;i++){
             if(winningCells[i][0] == 0){
                 winningCells[i][0] = row;
                 winningCells[i][1] = column;
                 break;
             }
         }
     }
     
     public static void resetWinningCell(){
         for(int i=0; i<4; i++){
             for(int j=0; j<2; j++){
                 winningCells[i][j] = 0;
             }
         }
     }
     
     public static void highlightWinningCells(int player){
         if(player == 1){
             for(int i=0; i<4; i++){
                 squares[winningCells[i][0]][winningCells[i][1]].setIcon(redButtonLight);
             }
         }else if(player == 10){
             for(int i=0; i<4; i++){
                 squares[winningCells[i][0]][winningCells[i][1]].setIcon(blueButtonLight);
             }
         }
     }
     
     //This method takes in a winner and modifies the score file
     public static void recordScore(int player){
         try {
             String[] currentScore;
 
             //Read current score
             currentScore = getScore();
 
             //modifiy Score
             if(player == 1){
                 currentScore[0] = String.valueOf(Integer.parseInt(currentScore[0]) + 1);
             }else if(player == 10){
                 currentScore[1] = String.valueOf(Integer.parseInt(currentScore[1]) + 1);
             }
             
             //Write new score
             PrintWriter writer = new PrintWriter("score.dat", "UTF-8");
             writer.println(currentScore[0]+"|"+currentScore[1]);
             writer.close();
             
         }catch (Exception ex2){
             
         }
     }
     
     public static String[] getScore(){
         String[] currentScore = new String[2];
         
         //Read current score
         try{
             BufferedReader br = new BufferedReader(new FileReader("score.dat"));
             currentScore = br.readLine().toString().split("[|]+");
         }catch(FileNotFoundException ex1){
             //Incase file is missing write a new file
             try{
                PrintWriter writer = new PrintWriter("score.dat", "UTF-8");
                 writer.println("0|0");
                 writer.close(); 
                 return getScore();
             }catch(Exception ex2){}
         }catch(Exception ex3){
             
         }
         
         return currentScore;
     }
     
     public static void updateGuiScore(){
         String[] score = getScore(); //gets score from score.dat file
         playerWins.setText("player: " + score[0]);
         botWins.setText("bot: " + score[1]);
     }
 }
