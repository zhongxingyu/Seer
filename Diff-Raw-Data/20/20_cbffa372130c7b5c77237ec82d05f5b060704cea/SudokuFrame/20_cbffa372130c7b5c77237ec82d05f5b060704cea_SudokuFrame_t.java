 import java.awt.Color;
import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.KeyStroke;
 
 /**
  * This class defines a GUI the solves a sudoku.
  * @author hrkalona
  */
 public class SudokuFrame extends JFrame {
   private JTextField[][][] board; //The board of textfields.
   private JTextField[][] reversed_board; //The board of textfields for the reversed board.
   private JTextField[] list; //The board of textfields for the list.
   private JButton start; //The start button.
   private JButton next; //The next button.
   private JButton save; //The save button.
   private JButton load; //The load button.
   private JMenuItem menu_save; //The save menu item.
   private JMenuItem menu_load; //The load menu item.
   private JMenuItem menu_start; //The start menu item.
   private JMenuItem menu_next; //The next menu item.
   private JMenuItem menu_about; //The about menu item.
   private JMenuItem menu_reversed; //The reversed board menu item.
   private Sudoku new_puzzle; //The instance of a sudoku.
   private int list_node; //The number of the current node.
   private JPanel[] panel_array; //A panel board.
   private JPanel panel1; //A panel.
   private boolean run_once; //Was each node of the list visited once?
   private JFrame reversed; //The window for the reversed board.
   private JFrame about; //The window for about.
  
 
     /**
      * The constractor, SudokuFrame, creates a GUI for a sudoku solver.
      */
     public SudokuFrame() {
 
         //All the basic stuff to create a frame, setting up buttons and menu items
         //Pretty much boring to explain!
         //Anyway the whole point is on the solving algorithm.
         //The start button/menu creates a new instance of a sudoku depending the numbers that there are on the original board.
         //The next button/menu is solving a number depending on the instance variable list_node and the moves to the next node by increasing
         //list_node's value.
         super();
        setSize(1024, 520);
         setTitle("Sudoku Solver");
         setResizable(false);
         getContentPane().setBackground(Color.DARK_GRAY);
 
         addWindowListener(new WindowAdapter() {
             @Override
             public void windowClosing(WindowEvent e) {
 
                 System.exit(0);
 
             }
         });
         
         
         JMenuBar menubar = new JMenuBar();
         JMenu menu1 = new JMenu("File");
         JMenu menu2 = new JMenu("Edit");
         JMenu menu3 = new JMenu("View");
         JMenu menu4 = new JMenu("Help");
         menu_save = new JMenuItem("Save");
         menu_load = new JMenuItem("Load");
         JMenuItem menu_exit = new JMenuItem("Exit");
         menu_start = new JMenuItem("Start");
         menu_next = new JMenuItem("Next");
         JMenuItem menu_clear = new JMenuItem("Clear");
         menu_reversed = new JMenuItem("Reversed Map");
         menu_about = new JMenuItem("About");
         
         menu_save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
         menu_load.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
         menu_exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
         menu_start.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, ActionEvent.CTRL_MASK));
         menu_next.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
         menu_clear.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK));
         menu_reversed.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
         menu_about.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK));
 
 
         menu_save.setEnabled(false);
         menu_next.setEnabled(false);
         menu_reversed.setEnabled(false);
 
 
         menu_save.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
 
                 save();
 
             }
         });
 
 
         menu_load.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
 
                 load();
 
             }
         });
 
         menu_exit.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
 
                 System.exit(0);
 
             }
         });
 
         menu_start.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
 
                 start.setEnabled(false);
                 menu_start.setEnabled(false);
                 next.setEnabled(true);
                 menu_next.setEnabled(true);
                 save.setEnabled(true);
                 menu_save.setEnabled(true);
                 load.setEnabled(false);
                 menu_load.setEnabled(false);
                 menu_reversed.setEnabled(true);
 
                 char[][] sudoku_board = new char[9][9];
                 for(int i = 0; i < board[0].length; i++) {
                     for(int j = 0; j < board[0][0].length; j++) {
                         try {
                             if(Character.isDigit(board[0][i][j].getText().charAt(0)) && board[0][i][j].getText().charAt(0) != '0') {
                                 sudoku_board[i][j] = board[0][i][j].getText().charAt(0);
                             }
                             else {
                                 sudoku_board[i][j] = ' ';
                                 board[0][i][j].setText("");
                             }
                         }
                         catch(StringIndexOutOfBoundsException ex) {
                             sudoku_board[i][j] = ' ';
                         }
                         board[0][i][j].setEditable(false);
                     }
                 }
 
                 new_puzzle = new Sudoku(sudoku_board);
 
                 sudoku_board = null;
                 
                 if(new_puzzle.getArrayList().get(0).getCount() == 0) {
                     start.setEnabled(true);
                     menu_start.setEnabled(true);
                     next.setEnabled(false);
                     menu_next.setEnabled(false);
                     save.setEnabled(false);
                     menu_save.setEnabled(false);
                     load.setEnabled(true);
                     menu_load.setEnabled(true);
                     menu_reversed.setEnabled(false);
 
                     for(int i = 0; i < board[0].length; i++) {
                         for(int j = 0; j < board[0][0].length; j++) {
                             board[0][i][j].setEditable(true);
                         }
                     }
 
                     return;
                 }
 
                 for(int k = 0; k < board.length; k++) {
                     for(int i = 0; i < board[0].length; i++) {
                         for(int j = 0; j < board[0][0].length; j++) {
                             if(new_puzzle.getBoard()[k][i][j] != ' ') {
                                 if(new_puzzle.getBoard()[k][i][j] == 'X') {
                                     board[k][i][j].setForeground(Color.RED);
                                 }
                                 else {
                                     board[k][i][j].setForeground(Color.BLACK);
                                 }
                                 board[k][i][j].setText("" + new_puzzle.getBoard()[k][i][j]);
                             }
                             else {
                                 board[k][i][j].setText("");
                             }
                             list[i].setText("" + new_puzzle.getArrayList().get(i).getCharNumber());
                         }
                     }
                 }
 
             }
         });
 
         menu_next.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
 
                 if((list_node != 0 || run_once == true) && new_puzzle.hasFailed() == true /*|| new_puzzle.getArrayList().get(list_node - 1).getCount() == 0*/) {
                     next.setEnabled(false);
                     menu_next.setEnabled(false);
                     save.setEnabled(false);
                     menu_save.setEnabled(false);
 
                     for(int i = 0; i < board[0].length; i++) {
                         for(int j = 0; j < board[0][0].length; j++) {
                             if(i < 3 && j < 3) {
                                 board[new_puzzle.getArrayList().get(list_node - 1).getIntNumber()][i][j].setBackground(Color.ORANGE);
                             }
                             if(i < 3 && j >= 6 && j < 9) {
                                 board[new_puzzle.getArrayList().get(list_node - 1).getIntNumber()][i][j].setBackground(Color.ORANGE);
                             }
                             if(i >= 3 && i < 6 && j >= 3 && j < 6) {
                                 board[new_puzzle.getArrayList().get(list_node - 1).getIntNumber()][i][j].setBackground(Color.ORANGE);
                             }
                             if(i >= 6 && i < 9 && j < 3) {
                                 board[new_puzzle.getArrayList().get(list_node - 1).getIntNumber()][i][j].setBackground(Color.ORANGE);
                             }
                             if(i >= 6 && i < 9 && j >= 6 && j < 9) {
                                 board[new_puzzle.getArrayList().get(list_node - 1).getIntNumber()][i][j].setBackground(Color.ORANGE);
                             }
                         }
                     }
                     panel1.setBackground(Color.RED);
                     list[list_node - 1].setBackground(Color.RED);
                     panel_array[new_puzzle.getArrayList().get(list_node - 1).getIntNumber() - 1].setBackground(Color.RED);
                     return;
                 }
 
 
                 if(new_puzzle.isSolved() == true && run_once == true) {
                     next.setEnabled(false);
                     menu_next.setEnabled(false);
                     save.setEnabled(false);
                     menu_save.setEnabled(false);
                     menu_reversed.setEnabled(false);
 
                     try {
                         reversed.dispose();
                     }
                     catch(NullPointerException ex) {}
 
                     for(int k = 1; k < board.length; k++) {
                         for(int i = 0; i < board[0].length; i++) {
                             for(int j = 0; j < board[0][0].length; j++) {
                                 board[k][i][j].setBackground(Color.BLACK);
                                 board[k][i][j].setText("");
                             }
                         }
                         new_puzzle.getBoard()[k] = null;
                     }
                     panel1.setBackground(Color.GREEN);
                     list[list_node - 1].setBackground(Color.GREEN);
                     panel_array[new_puzzle.getArrayList().get(list_node - 1).getIntNumber() - 1].setBackground(Color.GREEN);
 
                     for(int i = 0; i < board[0].length; i++) {
                         for(int j = 0; j < board[0][0].length; j++) {
                             board[new_puzzle.getArrayList().get(list_node - 1).getIntNumber()][i][j].setEnabled(false);
                         }
                     }
 
                     new_puzzle.sort();
 
  
                     for(int i = 0; i < new_puzzle.getArrayList().size(); i++) {
                         list[i].setText("" + new_puzzle.getArrayList().get(i).getCharNumber());
                     }
                     return;
                 }
 
                 if(list_node != 0) {
                     if(new_puzzle.getArrayList().get(list_node - 1).getCount() != 9) {
                         list[list_node - 1].setBackground(Color.WHITE);
                         panel_array[new_puzzle.getArrayList().get(list_node - 1).getIntNumber() - 1].setBackground(Color.LIGHT_GRAY);
                     }
                     else {
                         list[list_node - 1].setBackground(Color.GREEN);
                         panel_array[new_puzzle.getArrayList().get(list_node - 1).getIntNumber() - 1].setBackground(Color.GREEN);
                         for(int i = 0; i < board[0].length; i++) {
                             for(int j = 0; j < board[0][0].length; j++) {
                                 board[new_puzzle.getArrayList().get(list_node - 1).getIntNumber()][i][j].setEnabled(false);
                             }
                         }
                     }
                 }
 
                 if(list_node == 9) {
                     list_node = 0;
                     run_once = true;
                 }
 
                 if(new_puzzle.getArrayList().get(list_node).getCount() != 9 && run_once == true) {
                     new_puzzle.solve(list_node);
                     list[list_node].setBackground(Color.YELLOW);
                     panel_array[new_puzzle.getArrayList().get(list_node).getIntNumber() - 1].setBackground(Color.YELLOW);
                     list_node++;
                 }
                 else {
                     if(run_once == true) {
                         list_node++;
                         actionPerformed(e);
                     }
                     else {
                        if(new_puzzle.hasStarted() == false) {
                             if(new_puzzle.getCountAll() == Sudoku.NUMBER_COUNT) {
                                 new_puzzle.setStartToTrue();
                                 actionPerformed(e);
                             }
                             else {
                                 for(int i = 0; i < new_puzzle.getArrayList().size(); i++) {
                                     new_puzzle.solve(i);
                                 }
                                 new_puzzle.setStartToTrue();
                             }
                         }
                         else {
                             new_puzzle.solve(list_node);
                             list[list_node].setBackground(Color.YELLOW);
                             panel_array[new_puzzle.getArrayList().get(list_node).getIntNumber() - 1].setBackground(Color.YELLOW);
                             list_node++;
                         }
                     }
                 }
 
                 try {
                     for(int k = 0; k < board.length; k++) {
                         for(int i = 0; i < board[0].length; i++) {
                             for(int j = 0; j < board[0][0].length; j++) {
                                 if(new_puzzle.getBoard()[k][i][j] != ' ') {
                                     if(new_puzzle.getBoard()[k][i][j] == 'X') {
                                         board[k][i][j].setForeground(Color.RED);
                                     }
                                     else {
                                         board[k][i][j].setForeground(Color.BLACK);
                                     }
                                     board[k][i][j].setText("" + new_puzzle.getBoard()[k][i][j]);
                                 }
                                 else {
                                     board[k][i][j].setText("");
                                 }
                             }
                         }
                     }
                 }
                 catch(NullPointerException ex) {}
 
                 try {
                     for(int i = 0; i < reversed_board.length; i++) {
                         for(int j = 0; j < reversed_board[0].length; j++) {
                             reversed_board[i][j].setText("");
                             for(int k = 1; k < new_puzzle.getBoard().length; k++) {
                                 if(new_puzzle.getBoard()[k][i][j] == ' ') {
                                     reversed_board[i][j].setFont(new Font("simple", Font.PLAIN, 12));
                                     reversed_board[i][j].setText(reversed_board[i][j].getText() + k);
                                 }
                                 else {
                                     if(Character.isDigit(new_puzzle.getBoard()[k][i][j])) {
                                         reversed_board[i][j].setFont(new Font("BOLD", Font.BOLD, 12));
                                         reversed_board[i][j].setText("" + new_puzzle.getBoard()[k][i][j]);
                                         continue;
                                     }
                                 }
                             }
                         }
                     }
                 }
                 catch(NullPointerException ex) {}
 
             }
         });
 
         menu_clear.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
 
                 next.setEnabled(false);
                 menu_next.setEnabled(false);
                 save.setEnabled(false);
                 menu_save.setEnabled(false);
                 start.setEnabled(true);
                 menu_start.setEnabled(true);
                 load.setEnabled(true);
                 menu_load.setEnabled(true);
                 menu_reversed.setEnabled(false);
 
                 try {
                     reversed.dispose();
                 }
                 catch(NullPointerException ex) {}
 
                 for(int i = 0; i < list.length; i++) {
                     list[i].setBackground(Color.WHITE);
                     list[i].setText("");
                 }
 
                 for(int i = 0; i < panel_array.length; i++) {
                     panel_array[i].setBackground(Color.LIGHT_GRAY);
                 }
 
                 panel1.setBackground(Color.LIGHT_GRAY);
 
                 list_node = 0;
                 run_once = false;
 
                 for(int i = 0; i < board[0].length; i++) {
                     for(int j = 0; j < board[0][0].length; j++) {
                         if(i < 3 && j < 3) {
                             board[0][i][j].setBackground(Color.GREEN);
                         }
                         if(i < 3 && j >= 6 && j < 9) {
                             board[0][i][j].setBackground(Color.GREEN);
                         }
                         if(i >= 3 && i < 6 && j >= 3 && j < 6) {
                             board[0][i][j].setBackground(Color.GREEN);
                         }
                         if(i >= 6 && i < 9 && j < 3) {
                             board[0][i][j].setBackground(Color.GREEN);
                         }
                         if(i >= 6 && i < 9 && j >= 6 && j < 9) {
                             board[0][i][j].setBackground(Color.GREEN);
                         }
                         board[0][i][j].setText("");
                         board[0][i][j].setEditable(true);
                     }
                 }
 
                 for(int k = 1; k < board.length; k++) {
                     for(int i = 0; i < board[0].length; i++) {
                        for(int j = 0; j < board[0][0].length; j++) {
                            board[k][i][j].setBackground(Color.WHITE);
                            if(i < 3 && j < 3) {
                                board[k][i][j].setBackground(Color.GREEN);
                            }
                            if(i < 3 && j >= 6 && j < 9) {
                                board[k][i][j].setBackground(Color.GREEN);
                            }
                            if(i >= 3 && i < 6 && j >= 3 && j < 6) {
                                board[k][i][j].setBackground(Color.GREEN);
                            }
                            if(i >= 6 && i < 9 && j < 3) {
                                board[k][i][j].setBackground(Color.GREEN);
                            }
                            if(i >= 6 && i < 9 && j >= 6 && j < 9) {
                                board[k][i][j].setBackground(Color.GREEN);
                            }
                            board[k][i][j].setText("");
                            board[k][i][j].setEnabled(true);
                        }
                    }
                }
 
             }
         });
 
         menu_reversed.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
               int reversed_window_width = 720;
               int reversed_window_height = 280;
 
                 menu_reversed.setEnabled(false);
                 reversed = new JFrame("Reversed Map");
                 reversed.setSize(reversed_window_width, reversed_window_height);
                 reversed.setLocation((int)(getLocation().getX() + getSize().getWidth() / 2) - (reversed_window_width / 2), (int)(getLocation().getY() + getSize().getHeight() / 2) - (reversed_window_height / 2));
                 reversed.getContentPane().setBackground(Color.DARK_GRAY);
                 reversed.setResizable(false);
 
                 reversed.addWindowListener(new WindowAdapter() {
                     @Override
                     public void windowClosing(WindowEvent e) {
 
                         menu_reversed.setEnabled(true);
                         reversed.dispose();
 
                     }
                 });
 
 
                 reversed.setLayout(new FlowLayout());
 
                 JPanel alt_panel = new JPanel();
                 alt_panel.setLayout(new GridLayout(9, 9));
 
                 reversed_board = new JTextField[9][9];
                 for(int i = 0; i < reversed_board.length; i++) {
                     for(int j = 0; j < reversed_board[0].length; j++) {
                         reversed_board[i][j] = new JTextField(6);
                         reversed_board[i][j].setEditable(false);
                         if(i < 3 && j < 3) {
                             reversed_board[i][j].setBackground(Color.GREEN);
                         }
                         if(i < 3 && j >= 6 && j < 9) {
                             reversed_board[i][j].setBackground(Color.GREEN);
                         }
                         if(i >= 3 && i < 6 && j >= 3 && j < 6) {
                             reversed_board[i][j].setBackground(Color.GREEN);
                         }
                         if(i >= 6 && i < 9 && j < 3) {
                             reversed_board[i][j].setBackground(Color.GREEN);
                         }
                         if(i >= 6 && i < 9 && j >= 6 && j < 9) {
                             reversed_board[i][j].setBackground(Color.GREEN);
                         }
                         alt_panel.add(reversed_board[i][j]);
 
                         for(int k = 1; k < new_puzzle.getBoard().length; k++) {
                             if(new_puzzle.getBoard()[k][i][j] == ' ') {
                                 reversed_board[i][j].setFont(new Font("simple", Font.PLAIN, 12));
                                 reversed_board[i][j].setText(reversed_board[i][j].getText() + k);
                             }
                             else {
                                 if(Character.isDigit(new_puzzle.getBoard()[k][i][j])) {
                                     reversed_board[i][j].setFont(new Font("BOLD", Font.BOLD, 12));
                                     reversed_board[i][j].setText("" + new_puzzle.getBoard()[k][i][j]);
                                     continue;
                                 }
                             }
                         }
                     }
                 }
 
                 JButton close = new JButton("Close");
                 close.addActionListener(new ActionListener() {
                     public void actionPerformed(ActionEvent e) {
 
                         menu_reversed.setEnabled(true);
                         reversed.dispose();
 
                     }
                 });
 
                 reversed.add(new JLabel("                                                                                                                                                                                                                                                                                               "));
                 reversed.add(new JLabel("  "));
                 reversed.add(alt_panel);
                 reversed.add(new JLabel("  "));
                 reversed.add(close);
 
                 reversed.setVisible(true);
 
             }
         });
 
         menu_about.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
               int about_window_width = 298;
               int about_window_height = 226;
 
                 menu_about.setEnabled(false);
                 about = new JFrame("About");
                 about.setSize(about_window_width, about_window_height);
                 about.setLocation((int)(getLocation().getX() + getSize().getWidth() / 2) - (about_window_width / 2), (int)(getLocation().getY() + getSize().getHeight() / 2) - (about_window_height / 2));
                 about.getContentPane().setBackground(Color.DARK_GRAY);
                 about.setResizable(false);
 
                 about.addWindowListener(new WindowAdapter() {
                     @Override
                     public void windowClosing(WindowEvent e) {
 
                         menu_about.setEnabled(true);
                         about.dispose();
 
                     }
                 });
               
                 about.setLayout(new FlowLayout());
                 JPanel panel = new JPanel();
                 panel.setLayout(new GridLayout(8, 1));
                 panel.add(new JLabel(" "));
                 panel.add(new JLabel("       Sudoku Solver, beta version."));
                 panel.add(new JLabel());
                 panel.add(new JLabel("  Made by Chris Kalonakis using java  "));
                 panel.add(new JLabel("  on NetBeans IDE 6.8"));
                 panel.add(new JLabel());
                 panel.add(new JLabel("       Contact: hrkalona@inf.uth.gr"));
                 panel.add(new JLabel(" "));
                 
                 JButton close = new JButton("Close");
                 close.addActionListener(new ActionListener() {
                     public void actionPerformed(ActionEvent e) {
 
                         menu_about.setEnabled(true);
                         about.dispose();
 
                     }
                 });
 
                 about.add(new JLabel("                                                                                                          "));
                 about.add(new JLabel("  "));
                 about.add(panel);
                 about.add(new JLabel("  "));
                 about.add(close);
                 about.setVisible(true);
 
             }
         });
 
         menu1.add(menu_save);
         menu1.addSeparator();
         menu1.add(menu_load);
         menu1.addSeparator();
         menu1.add(menu_exit);
 
         menu2.add(menu_start);
         menu2.addSeparator();
         menu2.add(menu_next);
         menu2.addSeparator();
         menu2.add(menu_clear);
 
         menu3.add(menu_reversed);
 
         menu4.add(menu_about);
 
 
         menubar.add(menu1);
         menubar.add(menu2);
         menubar.add(menu3);
         menubar.add(menu4);
 
         setJMenuBar(menubar);
         
         setLayout(new FlowLayout());
         
 
         list = new JTextField[9];
         JPanel panel3 = new JPanel();
         panel3.setLayout(new GridLayout(9, 1));
         for(int i = 0; i < list.length; i++) {
            list[i] = new JTextField();
            list[i].setHorizontalAlignment(JTextField.CENTER);
            list[i].setPreferredSize(new Dimension(20, 20));
             list[i].setEditable(false);
             list[i].setBackground(Color.WHITE);
             list[i].setFont(new Font("BOLD", Font.BOLD, 16));
             panel3.add(list[i]);
         }
 
         add(new JLabel("                                                                                                                                                                                                                                                                                                                                                      "));
         add(new JLabel(" "));
         add(panel3);
 
         add(new JLabel("            "));
         
         panel1 = new JPanel();
         panel1.setLayout(new GridLayout(10,9));
         add(panel1);
         add(new JLabel("         "));
   
         panel_array = new JPanel[9];
         for(int i = 0; i < panel_array.length; i++) {
             panel_array[i] = new JPanel();
             panel_array[i].setLayout(new GridLayout(10, 9));
             panel_array[i].add(new JLabel(" "));
             panel_array[i].add(new JLabel("M"));
             panel_array[i].add(new JLabel("a"));
             panel_array[i].add(new JLabel("p"));
             panel_array[i].add(new JLabel(" "));
             panel_array[i].add(new JLabel("o"));
             panel_array[i].add(new JLabel("f"));
             panel_array[i].add(new JLabel(" "));
             panel_array[i].add(new JLabel("" + (i + 1)));
             panel_array[i].setBackground(Color.LIGHT_GRAY);
             add(panel_array[i]);
             add(new JLabel(""));
         }
 
        
         list_node = 0;
         run_once = false;
 
         panel1.add(new JLabel(" "));
         panel1.add(new JLabel("O"));
         panel1.add(new JLabel("r"));
         panel1.add(new JLabel("i"));
         panel1.add(new JLabel("g"));
         panel1.add(new JLabel("i"));
         panel1.add(new JLabel("n"));
         panel1.add(new JLabel("a"));
         panel1.add(new JLabel("l "));
         panel1.setBackground(Color.LIGHT_GRAY);
 
         board = new JTextField[10][9][9];
         for(int i = 0; i < board[0].length; i++) {
             for(int j = 0; j < board[0][0].length; j++) {
                board[0][i][j] = new JTextField();
                board[0][i][j].setPreferredSize(new Dimension(20, 20));
                board[0][i][j].setHorizontalAlignment(JTextField.CENTER);
                 if(i < 3 && j < 3) {
                     board[0][i][j].setBackground(Color.GREEN);
                 }
                 if(i < 3 && j >= 6 && j < 9) {
                     board[0][i][j].setBackground(Color.GREEN);
                 }
                 if(i >= 3 && i < 6 && j >= 3 && j < 6) {
                     board[0][i][j].setBackground(Color.GREEN);
                 }
                 if(i >= 6 && i < 9 && j < 3) {
                     board[0][i][j].setBackground(Color.GREEN);
                 }
                 if(i >= 6 && i < 9 && j >= 6 && j < 9) {
                     board[0][i][j].setBackground(Color.GREEN);
                 }
                 board[0][i][j].setFont(new Font("BOLD", Font.BOLD, 16));
                 panel1.add(board[0][i][j]);
             }
         }
         
 
         for(int k = 1; k < board.length; k++) {
             for(int i = 0; i < board[0].length; i++) {
                 for(int j = 0; j < board[0][0].length; j++) {
                    board[k][i][j] = new JTextField();
                     board[k][i][j].setEditable(false);
                    board[k][i][j].setPreferredSize(new Dimension(16, 16));
                    board[k][i][j].setHorizontalAlignment(JTextField.CENTER);
                     if(i < 3 && j < 3) {
                         board[k][i][j].setBackground(Color.GREEN);
                     }
                     if(i < 3 && j >= 6 && j < 9) {
                         board[k][i][j].setBackground(Color.GREEN);
                     }
                     if(i >= 3 && i < 6 && j >= 3 && j < 6) {
                         board[k][i][j].setBackground(Color.GREEN);
                     }
                     if(i >= 6 && i < 9 && j < 3) {
                         board[k][i][j].setBackground(Color.GREEN);
                     }
                     if(i >= 6 && i < 9 && j >= 6 && j < 9) {
                         board[k][i][j].setBackground(Color.GREEN);
                     }
                     board[k][i][j].setFont(new Font("BOLD", Font.BOLD, 12));
                     panel_array[k - 1].add(board[k][i][j]);
                 }
             }
         }
 
         start = new JButton("Start");
         start.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
 
                 start.setEnabled(false);
                 menu_start.setEnabled(false);
                 next.setEnabled(true);
                 menu_next.setEnabled(true);
                 save.setEnabled(true);
                 menu_save.setEnabled(true);
                 load.setEnabled(false);
                 menu_load.setEnabled(false);
                 menu_reversed.setEnabled(true);
 
                 char[][] sudoku_board = new char[9][9];
                 for(int i = 0; i < board[0].length; i++) {
                     for(int j = 0; j < board[0][0].length; j++) {
                         try {
                             if(Character.isDigit(board[0][i][j].getText().charAt(0)) && board[0][i][j].getText().charAt(0) != '0') {
                                 sudoku_board[i][j] = board[0][i][j].getText().charAt(0);
                             }
                             else {
                                 sudoku_board[i][j] = ' ';
                                 board[0][i][j].setText("");
                             }
                         }
                         catch(StringIndexOutOfBoundsException ex) {
                             sudoku_board[i][j] = ' ';
                         }
                         board[0][i][j].setEditable(false);
                     }
                 }
 
                 new_puzzle = new Sudoku(sudoku_board);
 
                 sudoku_board = null;
 
                 if(new_puzzle.getArrayList().get(0).getCount() == 0) {
                     start.setEnabled(true);
                     menu_start.setEnabled(true);
                     next.setEnabled(false);
                     menu_next.setEnabled(false);
                     save.setEnabled(false);
                     menu_save.setEnabled(false);
                     load.setEnabled(true);
                     menu_load.setEnabled(true);
                     menu_reversed.setEnabled(false);
 
                     for(int i = 0; i < board[0].length; i++) {
                         for(int j = 0; j < board[0][0].length; j++) {
                             board[0][i][j].setEditable(true);
                         }
                     }
                     
                     return;
                 }
            
                 for(int k = 0; k < board.length; k++) {
                     for(int i = 0; i < board[0].length; i++) {
                         for(int j = 0; j < board[0][0].length; j++) {
                             if(new_puzzle.getBoard()[k][i][j] != ' ') {
                                 if(new_puzzle.getBoard()[k][i][j] == 'X') {
                                     board[k][i][j].setForeground(Color.RED);
                                 }
                                 else {
                                     board[k][i][j].setForeground(Color.BLACK);
                                 }
                                 board[k][i][j].setText("" + new_puzzle.getBoard()[k][i][j]);
                             }
                             else {
                                 board[k][i][j].setText("");
                             }
                             list[i].setText("" + new_puzzle.getArrayList().get(i).getCharNumber());
                         }
                     }
                 }
 
             }
         });
 
         next = new JButton("Next");
         next.setEnabled(false);
 
         next.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
 
                 if((list_node != 0 || run_once == true) && new_puzzle.hasFailed() == true /*|| new_puzzle.getArrayList().get(list_node - 1).getCount() == 0*/) {
                     next.setEnabled(false);
                     menu_next.setEnabled(false);
                     save.setEnabled(false);
                     menu_save.setEnabled(false);
                     
                     for(int i = 0; i < board[0].length; i++) {
                         for(int j = 0; j < board[0][0].length; j++) {
                             if(i < 3 && j < 3) {
                                 board[new_puzzle.getArrayList().get(list_node - 1).getIntNumber()][i][j].setBackground(Color.ORANGE);
                             }
                             if(i < 3 && j >= 6 && j < 9) {
                                 board[new_puzzle.getArrayList().get(list_node - 1).getIntNumber()][i][j].setBackground(Color.ORANGE);
                             }
                             if(i >= 3 && i < 6 && j >= 3 && j < 6) {
                                 board[new_puzzle.getArrayList().get(list_node - 1).getIntNumber()][i][j].setBackground(Color.ORANGE);
                             }
                             if(i >= 6 && i < 9 && j < 3) {
                                 board[new_puzzle.getArrayList().get(list_node - 1).getIntNumber()][i][j].setBackground(Color.ORANGE);
                             }
                             if(i >= 6 && i < 9 && j >= 6 && j < 9) {
                                 board[new_puzzle.getArrayList().get(list_node - 1).getIntNumber()][i][j].setBackground(Color.ORANGE);
                             }
                         }
                     }
                     panel1.setBackground(Color.RED);
                     list[list_node - 1].setBackground(Color.RED);
                     panel_array[new_puzzle.getArrayList().get(list_node - 1).getIntNumber() - 1].setBackground(Color.RED);
                     return;
                 }
 
                 if(new_puzzle.isSolved() == true && run_once == true) {
                     next.setEnabled(false);
                     menu_next.setEnabled(false);
                     save.setEnabled(false);
                     menu_save.setEnabled(false);
                     menu_reversed.setEnabled(false);
 
                     try {
                         reversed.dispose();
                     }
                     catch(NullPointerException ex) {}
 
                     for(int k = 1; k < board.length; k++) {
                         for(int i = 0; i < board[0].length; i++) {
                             for(int j = 0; j < board[0][0].length; j++) {
                                 board[k][i][j].setBackground(Color.BLACK);
                                 board[k][i][j].setText("");
                             }
                         }
                         new_puzzle.getBoard()[k] = null;
                     }
                     panel1.setBackground(Color.GREEN);
                     list[list_node - 1].setBackground(Color.GREEN);
                     panel_array[new_puzzle.getArrayList().get(list_node - 1).getIntNumber() - 1].setBackground(Color.GREEN);
 
                     for(int i = 0; i < board[0].length; i++) {
                         for(int j = 0; j < board[0][0].length; j++) {
                             board[new_puzzle.getArrayList().get(list_node - 1).getIntNumber()][i][j].setEnabled(false);
                         }
                     }
 
                     new_puzzle.sort();
 
                     for(int i = 0; i < new_puzzle.getArrayList().size(); i++) {
                         list[i].setText("" + new_puzzle.getArrayList().get(i).getCharNumber());
                     }
                     return;
                 }
 
                 if(list_node != 0) {
                     if(new_puzzle.getArrayList().get(list_node - 1).getCount() != 9) {
                         list[list_node - 1].setBackground(Color.WHITE);
                         panel_array[new_puzzle.getArrayList().get(list_node - 1).getIntNumber() - 1].setBackground(Color.LIGHT_GRAY);
                     }
                     else {
                         list[list_node - 1].setBackground(Color.GREEN);
                         panel_array[new_puzzle.getArrayList().get(list_node - 1).getIntNumber() - 1].setBackground(Color.GREEN);
                         for(int i = 0; i < board[0].length; i++) {
                             for(int j = 0; j < board[0][0].length; j++) {
                                 board[new_puzzle.getArrayList().get(list_node - 1).getIntNumber()][i][j].setEnabled(false);
                             }
                         }
                     }
                 }
 
                 if(list_node == 9) {
                     list_node = 0;
                     run_once = true;
                 }
 
                 if(new_puzzle.getArrayList().get(list_node).getCount() != 9 && run_once == true) {
                     new_puzzle.solve(list_node);
                     list[list_node].setBackground(Color.YELLOW);
                     panel_array[new_puzzle.getArrayList().get(list_node).getIntNumber() - 1].setBackground(Color.YELLOW);
                     list_node++;
                 }
                 else {
                     if(run_once == true) {
                         list_node++;
                         actionPerformed(e);
                     }
                     else {
                         if(new_puzzle.hasStarted() == false) {
                             if(new_puzzle.getCountAll() == Sudoku.NUMBER_COUNT) {
                                 new_puzzle.setStartToTrue();
                                 actionPerformed(e);
                             }
                             else {
                                 for(int i = 0; i < new_puzzle.getArrayList().size(); i++) {
                                     new_puzzle.solve(i);
                                 }
                                 new_puzzle.setStartToTrue();
                             }
                         }
                         else {
                             new_puzzle.solve(list_node);
                             list[list_node].setBackground(Color.YELLOW);
                             panel_array[new_puzzle.getArrayList().get(list_node).getIntNumber() - 1].setBackground(Color.YELLOW);
                             list_node++;
                         }
                     }
                 }
 
                 try {
                     for(int k = 0; k < board.length; k++) {
                         for(int i = 0; i < board[0].length; i++) {
                             for(int j = 0; j < board[0][0].length; j++) {
                                 if(new_puzzle.getBoard()[k][i][j] != ' ') {
                                     if(new_puzzle.getBoard()[k][i][j] == 'X') {
                                         board[k][i][j].setForeground(Color.RED);
                                     }
                                     else {
                                         board[k][i][j].setForeground(Color.BLACK);
                                     }
                                     board[k][i][j].setText("" + new_puzzle.getBoard()[k][i][j]);
                                 }
                                 else {
                                     board[k][i][j].setText("");
                                 }
                             }
                         }
                     }
                 }
                 catch(NullPointerException ex) {}
 
                 try {
                     for(int i = 0; i < reversed_board.length; i++) {
                         for(int j = 0; j < reversed_board[0].length; j++) {
                             reversed_board[i][j].setText("");
                             for(int k = 1; k < new_puzzle.getBoard().length; k++) {
                                 if(new_puzzle.getBoard()[k][i][j] == ' ') {
                                     reversed_board[i][j].setFont(new Font("simple", Font.PLAIN, 12));
                                     reversed_board[i][j].setText(reversed_board[i][j].getText() + k);
                                 }
                                 else {
                                     if(Character.isDigit(new_puzzle.getBoard()[k][i][j])) {
                                         reversed_board[i][j].setFont(new Font("BOLD", Font.BOLD, 12));
                                         reversed_board[i][j].setText("" + new_puzzle.getBoard()[k][i][j]);
                                         continue;
                                     }
                                 }
                             }
                         }
                     }
                 }
                 catch(NullPointerException ex) {}
 
             }
         });
 
         save = new JButton("Save");
         save.setEnabled(false);
         save.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
 
                 save();
 
             }
         });
 
 
         load = new JButton("Load");
 
         load.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
 
                 load();
 
             }
         });
 
         JButton clear = new JButton("Clear");
 
         clear.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
 
                 next.setEnabled(false);
                 menu_next.setEnabled(false);
                 save.setEnabled(false);
                 menu_save.setEnabled(false);
                 start.setEnabled(true);
                 menu_start.setEnabled(true);
                 load.setEnabled(true);
                 menu_load.setEnabled(true);
                 menu_reversed.setEnabled(false);
 
                 try {
                     reversed.dispose();
                 }
                 catch(NullPointerException ex) {}
 
                 for(int i = 0; i < list.length; i++) {
                     list[i].setBackground(Color.WHITE);
                     list[i].setText("");
                 }
 
                 for(int i = 0; i < panel_array.length; i++) {
                     panel_array[i].setBackground(Color.LIGHT_GRAY);
                 }
 
                 panel1.setBackground(Color.LIGHT_GRAY);
 
                 list_node = 0;
                 run_once = false;
 
                 for(int i = 0; i < board[0].length; i++) {
                     for(int j = 0; j < board[0][0].length; j++) {
                         if(i < 3 && j < 3) {
                             board[0][i][j].setBackground(Color.GREEN);
                         }
                         if(i < 3 && j >= 6 && j < 9) {
                             board[0][i][j].setBackground(Color.GREEN);
                         }
                         if(i >= 3 && i < 6 && j >= 3 && j < 6) {
                             board[0][i][j].setBackground(Color.GREEN);
                         }
                         if(i >= 6 && i < 9 && j < 3) {
                             board[0][i][j].setBackground(Color.GREEN);
                         }
                         if(i >= 6 && i < 9 && j >= 6 && j < 9) {
                             board[0][i][j].setBackground(Color.GREEN);
                         }
                         board[0][i][j].setText("");
                         board[0][i][j].setEditable(true);
                     }
                 }
 
                 for(int k = 1; k < board.length; k++) {
                     for(int i = 0; i < board[0].length; i++) {
                        for(int j = 0; j < board[0][0].length; j++) {
                            board[k][i][j].setBackground(Color.WHITE);
                            if(i < 3 && j < 3) {
                                board[k][i][j].setBackground(Color.GREEN);
                            }
                            if(i < 3 && j >= 6 && j < 9) {
                                board[k][i][j].setBackground(Color.GREEN);
                            }
                            if(i >= 3 && i < 6 && j >= 3 && j < 6) {
                                board[k][i][j].setBackground(Color.GREEN);
                            }
                            if(i >= 6 && i < 9 && j < 3) {
                                board[k][i][j].setBackground(Color.GREEN);
                            }
                            if(i >= 6 && i < 9 && j >= 6 && j < 9) {
                                board[k][i][j].setBackground(Color.GREEN);
                            }
                            board[k][i][j].setText("");
                            board[k][i][j].setEnabled(true);
                        }
                    }
                }
 
             }
         });
 
 
         add(new JLabel("   "));
         JPanel panel4 = new JPanel();
         panel4.setBackground(Color.DARK_GRAY);
         panel4.setLayout(new GridLayout(5, 3));
         
         panel4.add(save);
         panel4.add(new JLabel());
         panel4.add(start);
         panel4.add(new JLabel());
         panel4.add(new JLabel());
         panel4.add(new JLabel());
         panel4.add(load);
         panel4.add(new JLabel());    
         panel4.add(next);
         panel4.add(new JLabel());
         panel4.add(new JLabel());
         panel4.add(new JLabel());
         panel4.add(new JLabel());
         panel4.add(new JLabel());
         panel4.add(clear);
         add(panel4);
         
     }
 
     /*
      * The method, save, is saving the status of the original board.
      */
     private void save() {
       ObjectOutputStream file = null;
 
         try {
            file = new ObjectOutputStream(new FileOutputStream("sudoku.dat"));
 
            file.writeObject(new_puzzle.getBoard()[0]);
 
            file.close();
         }
         catch (FileNotFoundException ex) {}
         catch (IOException ex) {}
 
     }
 
     /*
      * The method, load, is load a saved status in the original board.
      */
     private void load() {
       ObjectInputStream file = null;
 
         try {
             file = new ObjectInputStream(new FileInputStream("sudoku.dat"));
 
             char[][] temp = (char[][]) file.readObject();
 
             for(int i = 0; i < temp.length; i++) {
                 for(int j = 0; j < temp[0].length; j++) {
                     if(temp[i][j] == ' ') {
                         board[0][i][j].setText("");
                     }
                     else {
                         board[0][i][j].setText("" + temp[i][j]);
                     }
                 }
             }
 
 
             temp = null;
 
 
             file.close();
         }
         catch (ClassNotFoundException ex) {}
         catch (FileNotFoundException ex) {}
         catch (IOException ex) {}
 
     }
 
     /**
      * Creates a new instance of the GUI.
      * @param args Command-line parameters (Not used).
      */
     public static void main(String[] args) {
 
         SudokuFrame test = new SudokuFrame();
         test.setVisible(true);
 
     }
 
 }
