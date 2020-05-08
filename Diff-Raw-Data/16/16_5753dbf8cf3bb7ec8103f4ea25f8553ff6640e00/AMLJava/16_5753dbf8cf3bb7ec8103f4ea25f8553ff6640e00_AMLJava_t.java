 import java.util.*;
 import java.io.*;
 import javax.swing.*;
 import java.awt.*;
 import javax.swing.text.*;
 
 /*
  * Standard Library of Java code for amL
  * Programming Languages and Translators, Fall 2012
  * 
  * Sriramkumar Balasubramanian (sb3457)
  * Evan Drewry (ewd2106)
  * Timothy Giel (tkg2104)
  * Nikhil Helferty (nh2407)
  * 
  * Includes functions for the bot to move around the maze, or
  * obtain information about its surrounding environment.
  * 
  * Includes a rudimentary Swing GUI for the user to see
  * what the bot does when the program is run.
  * 
  * Makes use of the custom Cell object to represent a given cell of the maze.
  * 
  */
 public class AMLJava extends JFrame
 {
     static int width; // width of the maze
     static int height; // height of the maze
     static Cell current; // the current cell the bot is at
     static Cell [][] maze; // 2D representation of the maze - maze[row][col] is Cell at row row, column col, with top left being 0, 0
     static Stack<Cell> moves; // is a stack of Cells - consecutive moves that have been done, not counting "reverted" moves - used to backtrack in revert()
     static JTextArea textArea;
     
     // build the representation of the maze from the text file
     public static void buildMaze(String mazeFileName) {
         if (mazeFileName.equals("random")) randomGenMaze();
         else {
             File mazeFile  = new File(mazeFileName);
             try {
                 Scanner scan = new Scanner(mazeFile);
                 width = scan.nextInt();
                 height = scan.nextInt();
                 maze = new Cell[height][width];
                 for (int row = 0; row < height; row++) {
                     for (int col = 0; col < width; col++) {
                         int temp = scan.nextInt();
                         maze[row][col] = new Cell(temp, row, col);
                         if (temp == 2) current = maze[row][col];
                     }
                 }
                 scan.close();
             }
             catch (FileNotFoundException e) {
                 System.out.println("File Not Found");
                 return;
             }
         }
         moves = new Stack<Cell>();
         new AMLJava(); // initiliase the Swing GUI
     }
     
     public static void randomGenMaze() {
         width = (int)(Math.random() * 20) + 5;
         height = (int)(Math.random() * 20) + 5;
         maze = new Cell[height][width];
         int targetRow = (int)(Math.random()*(height-1));
         int targetCol = (int)(Math.random()*(width-1));
         maze[targetRow][targetCol] = new Cell(3, targetRow, targetCol); // randomly generate a target cell
         int stepLength = (int)((Math.random()*width)/2 + (Math.random()*height)/2) + width/2 + height/2; // determine how many steps it'll iterate back from the target until a start cell is picked
         boolean pathGenerated = false;
         while (!pathGenerated) pathGenerated =  pathGen(maze[targetRow][targetCol], stepLength);
         // start point has been picked - for remaining null cells, randomly pick between a step or a hole
         for (int row = 0; row < height; row++) {
             for (int col = 0; col < width; col++) {
                 if (maze[row][col] == null) {
                     double prob = Math.random();
                     if (prob < .3) maze[row][col] = new Cell(1, row, col);
                     else maze[row][col] = new Cell(0, row, col);
                 }
             }
         }
     }
     
     public static boolean pathGen(Cell c, int steps) {
         int cRow = c.getRow();
         int cCol = c.getCol();
         if (steps == 0) { // make this cell the starting one
             current = maze[cRow][cCol] = new Cell(2, cRow, cCol);
             return true;
         }
         LinkedList<String> dirs = new LinkedList<String>();
         dirs.add("up");
         dirs.add("down");
         dirs.add("left");
         dirs.add("right");
         if (cRow == 0) dirs.remove("up");
         if (cRow == (height-1)) dirs.remove("down");
         if (cCol == 0) dirs.remove("left");
         if (cCol == (width-1)) dirs.remove("right");
         if (dirs.size() == 0) return false;
         String randDir = dirs.get((int)(Math.random() * dirs.size())); // pick a random direction from the current cell
         if (randDir.equals("up")) {
             if (maze[cRow-1][cCol] == null) maze[cRow-1][cCol] = new Cell(1, cRow-1, cCol);            
             steps--;
             return pathGen(maze[cRow-1][cCol], steps);
         }
         else if (randDir.equals("down")) {
             if (maze[cRow+1][cCol] == null) maze[cRow+1][cCol] = new Cell(1, cRow+1, cCol);
             steps--;
             return pathGen(maze[cRow+1][cCol], steps);
         }
         else if (randDir.equals("left")) {
             if (maze[cRow][cCol-1] == null) maze[cRow][cCol-1] = new Cell(1, cRow, cCol-1);
             steps--;
             return pathGen(maze[cRow][cCol-1], steps);
         }
         else {
             if (maze[cRow][cCol+1] == null) maze[cRow][cCol+1] = new Cell(1, cRow, cCol+1);
             steps--;
             return pathGen(maze[cRow][cCol+1], steps);
         }
         
     }
     
     
     // creates the Swing GUI
     public AMLJava() {
         setDefaultCloseOperation(EXIT_ON_CLOSE);
         setTitle("Maze");
         JPanel mazePanel = new JPanel(new GridLayout(height, width)); // height bounds # of rows, width bounds # of columns
         for(int row = 0; row < height; row++) {
             for (int col = 0; col < width; col++) {
                 mazePanel.add(maze[row][col]); // add the cell to the maze
             }
         }
         add(mazePanel, BorderLayout.CENTER);
         // now add the text area to display moves explicitly
         textArea = new JTextArea(5, 1);
         textArea.setEditable(false);
         textArea.setFont(new Font("Times New Roman", Font.PLAIN, 16));
         DefaultCaret caret = (DefaultCaret)textArea.getCaret();
         caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
         JPanel textPanel = new JPanel();
         textPanel.add(textArea);
         textPanel.setBackground(Color.WHITE);
         JScrollPane scrollPane = new JScrollPane(textPanel);
         scrollPane.setPreferredSize(new Dimension(500, 100));
         scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
         add(scrollPane, BorderLayout.SOUTH);
         pack();
         setVisible(true);
     }
     
     // moves the bot up from its current position
     // if successful returns true, otherwise false
     public static boolean move_U() {
         if (hasTop()) {
             move(maze[current.getRow()-1][current.getCol()]);
             textArea.append("Bot moved UP\n");
             if (current.isTarget()) textArea.append("Bot moved on to a target!\n");
             return true;
         }
         else {
             textArea.append("Bot failed to move UP\n");
             return false;
         }
     }
     
     public static boolean move_D(){
         if (hasBottom()) {
             move(maze[current.getRow()+1][current.getCol()]);
             textArea.append("Bot moved DOWN\n");
             if (current.isTarget()) textArea.append("Bot moved on to a target!\n");
             return true;            
         }
         else {
             textArea.append("Bot failed to move DOWN\n");
             return false;
         }
     }
     
     public static boolean move_L() {
         if (hasLeft()) {
             move(maze[current.getRow()][current.getCol()-1]);
             textArea.append("Bot moved LEFT\n");            
             if (current.isTarget()) textArea.append("Bot moved on to a target!\n");
             return true;
         }        
         else {
             textArea.append("Bot failed to move LEFT\n");
             return false;
         }
     }
     
     public static boolean move_R() {
         if (hasRight()) {
             move(maze[current.getRow()][current.getCol()+1]);
             textArea.append("Bot moved RIGHT\n");
             if (current.isTarget()) textArea.append("Bot moved on to a target!\n");
             return true;
         }
         else {
             textArea.append("Bot failed to move RIGHT\n");
             return false;
         }
     }
     
     // private move function eliminating duplicate code
     // moves the bot from "current" cell to next cell in parameter, updates the GUI accordingly
    public static void move(Cell next) {
         moves.push(current);
         try {
             Thread.sleep(500);
         }
         catch (InterruptedException e) { }
         current.setText("");
         if (current.isTarget())  current.setText("TARGET");
         if (current.isSource()) current.setText("START");
         current = next;
         current.visited();
         current.setText("BOT");
     }
     
     // returns true if there is a cell the bot can go on above it
     // false otherwise
     public static boolean hasTop() {
         if (current.getRow() > 0) { // if not at top
             if (maze[current.getRow()-1][current.getCol()].getValue() != 0) return true; // if not a "hole"
         }
         return false;
     }
     
     public static boolean hasBottom() {
         if (current.getRow() < (height-1)) {
             if (maze[current.getRow()+1][current.getCol()].getValue() != 0) return true;
         }
         return false;
     }
             
     public static boolean hasLeft() {
         if (current.getCol() > 0) {
             if (maze[current.getRow()][current.getCol()-1].getValue() != 0) return true;
         }
         return false;
     }
     
     public static boolean hasRight() {
         if (current.getCol() < (width-1)) {
             if (maze[current.getRow()][current.getCol()+1].getValue() != 0) return true;
         }
         return false;
     }
     
     // returns the cell to the right of the bot's current position if it exists
     public static Cell right() {
         if (hasRight()) {
             Cell c = maze[current.getRow()][current.getCol()+1];
             return c;
         }
         else return null;
     }
     
     public static Cell up() {
         if (hasTop()) {
             Cell c = maze[current.getRow()-1][current.getCol()];
             return c;
         }
         else return null;
     }
     
     public static Cell down() {
         if (hasBottom()) {
             Cell c = maze[current.getRow()+1][current.getCol()];
             return c;
         }
         else return null;
     }
     
     public static Cell left() {
         if (hasLeft()) {
             Cell c = maze[current.getRow()][current.getCol()-1];
             return c;
         }
         else return null;
     }
     
     // returns whether or not the cell at row, col has been visited
     public static boolean visit(int row, int col) {
         return maze[row][col].getVisited();
     }
     
     // overloaded version of visit that instead accepts a single integer (cell ID)
     // cell ID is calculated as follows = (width of maze) * (cell row) + (cell column)
     public static boolean visit(int id) {
         return visit(id/width, id%width);
     }
     
     // "reverts" the previous move if possible (backtracks), returns true
     // if no moves committed returns false
     public static boolean revert() {
         if (moves.empty()) {
             textArea.append("Bot failed to REVERT (at starting position)\n");
             return false; // no moves executed!
         }
         else {
             try {
                 Thread.sleep(500);
             }
             catch (InterruptedException e) { }
             if (current.isTarget()) current.setText("TARGET");
             else current.setText("");
             current = moves.pop();
             current.setText("BOT");
             textArea.append("Bot BACKTRACKED\n");
             return true;
         }        
     }
     
     
     
 }
