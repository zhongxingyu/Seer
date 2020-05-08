 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.KeyEvent;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 
 /**
  * Class used to play a board via the terminal.
  *
  * Great for testing BoardState stuff!
  *
  * There are two ways of running it:
  * 1. With no arguments, giving a board on stdin (easiest by piping from a file)
  * 2. With 1 argument, a number specifying which level from the test boards you want to play (11000+ boards available)
  */
 public class Play {
 
     public static void main(String[] args) throws IOException {
         if (args.length == 0) {
             // Read board from stdin
             ArrayList<String> lines = new ArrayList<String>();
             BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
 
             String line;
             while ((line = in.readLine()) != null) {
                 lines.add(line);
             }
             BoardState board = new BoardState(lines);
             boardInteract(board);
         } else if (args.length == 1) {
             int boardNum = -1;
             try {
                 boardNum = Integer.parseInt(args[0]);
             } catch (NumberFormatException e) {
                 System.out.println("Argument must be a board number");
                 System.exit(0);
             }
             System.out.println("Searching for board " + boardNum + "...");
             BoardState board = BoardUtil.getTestBoard(boardNum);
             if (board == null) {
                 System.out.println("Invalid board number: " + boardNum);
                 System.exit(0);
             }
             System.out.println("Found board!");
             System.out.println("===================================================");
             boardInteract(board);
         }
     }
 
 
     public static void boardInteract(BoardState board) {
         final BoardState b = board;
 
         JFrame frame = new JFrame();
 
         frame.setVisible(true);
 
         KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
         manager.addKeyEventDispatcher(new KeyEventDispatcher() {
             @Override
             public boolean dispatchKeyEvent(KeyEvent keyEvent) {
                 if (keyEvent.getID() == KeyEvent.KEY_PRESSED) {
                     boolean changed = false;
                     int move = -1;
                     switch (keyEvent.getKeyCode()) {
                         case KeyEvent.VK_LEFT:
                             move = BoardState.LEFT;
                             break;
                         case KeyEvent.VK_UP:
                             move = BoardState.UP;
                             break;
                         case KeyEvent.VK_RIGHT:
                             move = BoardState.RIGHT;
                             break;
                         case KeyEvent.VK_DOWN:
                             move = BoardState.DOWN;
                             break;
                         case KeyEvent.VK_SPACE:
                             changed = b.reverseMove();
                             break;
                         case KeyEvent.VK_BACK_SPACE:
                             String path = b.backtrackPath();
                             if (path.length() > 0) {
                                 changed = true;
                                 System.out.println("Path taken:");
                                 System.out.println(path);
                             }
                             break;
                         case KeyEvent.VK_ESCAPE:
                             System.exit(0);
                             break;
                         default:
                             return false;
                     }
 
                     if (move >= 0) {
                         boolean goodMove = b.isGoodMove(move);
                         changed = b.performMove(move);
                         if (changed && !goodMove) {
                             System.out.println("Board now unsolvable :/");
                         }
                     }
                     if (changed) {
                         System.out.println(b);
                         System.out.println("Been here? " + b.isPreviousGameState());
                     }
                     if (b.isBoardSolved()) {
                         System.out.println("Board solved!");
                         System.out.println("Path taken:");
                         System.out.println(b.backtrackPath());
                         System.out.println("Exiting..");
                         System.exit(0);
                     }
 
                     return true;
                 }
                 return false;
             }
         });
         System.out.println("Started awesome board player!");
         System.out.println();
         System.out.println("Instructions:");
         System.out.println("1. Use arrow keys to move");
         System.out.println("2. Space bar to regret the last move");
         System.out.println("3. Backspace to print the total path and reset the board");
         System.out.println("4. Escape to quit");
         System.out.println();
         System.out.println(b);
     }
 }
