 package Main;
 
 import Accessories.Rules;
 import Accessories.SaveGame;
 import Accessories.Timer;
 import Logic.*;
 import java.awt.*;
 import javax.swing.*;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.ButtonGroup;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.io.*;
 import java.net.ServerSocket;
 import java.net.Socket;
 import javax.swing.SpringLayout;
 import javax.swing.JLabel;
 import static javax.swing.JOptionPane.*;
 
 public class GUI extends JFrame {
 
     //OBJECT VARIABLES
     private JLabel background = new JLabel(new ImageIcon(getClass().getResource("/Accessories/Pictures/Background.png")));
     private Chess chess;
     private SaveGame[] savegames = new SaveGame[6];
     private Timer timerS;
     private Timer timerH;
     private int[] counterH = new int[6];
     private int[] counterS = new int[6];
     private JTextArea textarea = new JTextArea(10, 12);
     private JTextArea textarea2 = new JTextArea(10, 12);
     private JScrollPane scrollpane = new JScrollPane(textarea);
     private JScrollPane scrollpane2 = new JScrollPane(textarea2);
     private JLabel lostPieceWLabel = new JLabel(new ImageIcon(getClass().getResource("/Accessories/Pictures/LostPieceWNormal.png")));
     private JLabel lostPieceBLabel = new JLabel(new ImageIcon(getClass().getResource("/Accessories/Pictures/LostPieceBNormal.png")));
     private JLabel whitegif = new JLabel(new ImageIcon(getClass().getResource("/Accessories/Pictures/WhiteGIF.gif")));
     private JLabel blackgif = new JLabel(new ImageIcon(getClass().getResource("/Accessories/Pictures/BlackGIF.gif")));
     private JTextArea lostPieceW = new JTextArea(15, 5);
     private JTextArea lostPieceB = new JTextArea(15, 5);
     private Container contentPane = getContentPane();
     private SpringLayout layout = new SpringLayout(); //Using springlayout and adding constraints to place the components.
     ChessAction chessAction;
     Socket s;
     ObjectOutputStream oops;
     ObjectInputStream oips;
     int choice;
 
     //Constructor
     public GUI(String title) {
 
 
 
         //Settings for the frame and adding components.
         String[] options = {"Join Lan", "Host Lan", "Normal"};
         choice = showOptionDialog(null, "Choose Gametype", null, OK_OPTION, QUESTION_MESSAGE, null, options, null);
         if (choice == -1) {
             System.exit(0);
         }
         setDefaultCloseOperation(EXIT_ON_CLOSE);
         setTitle(title);
         contentPane.setLayout(layout);
         background.setPreferredSize(new Dimension(1084, 661));
         chess = new Chess(choice);
         chessAction = new ChessAction(chess);
         chess.addChessListener(chessAction);
         timerS = new Timer();
         timerH = new Timer();
         scrollpane = new JScrollPane(textarea);
         scrollpane2 = new JScrollPane(textarea2);
         JMenuBar menuBar = new JMenuBar();
         setJMenuBar(menuBar);
         add(scrollpane);
         add(chess);
         add(scrollpane2);
         add(timerS);
         add(timerH);
         add(lostPieceWLabel);
         add(lostPieceBLabel);
         add(lostPieceB);
         add(lostPieceW);
         add(whitegif);
         add(blackgif);
         add(background);
 
         setConstraints(); //Sets constraints to the stuff added to the JFrame.
         settings(); //Using the settings() method to create the log.
         lostpieceTableW();
         lostpieceTableB();
 
 
         //Creating and adding the menubar
         JMenu file = new JMenu("File");
         JMenu settings = new JMenu("Settings");
         JMenu credits = new JMenu("Credits");
         JMenu help = new JMenu("Help");
         menuBar.add(file);
         menuBar.add(settings);
         menuBar.add(credits);
         menuBar.add(help);
         menuBar.setBorder(null);
 
         //Creating buttons for the menubar with icons and key bindings.
         if (choice == 2) {
             JMenuItem Newgame = new JMenuItem("New Game", new ImageIcon(getClass().getResource("/Accessories/Pictures/newgame.png")));
             JMenuItem Save = new JMenuItem("Save game", new ImageIcon(getClass().getResource("/Accessories/Pictures/mac.png")));
             JMenuItem Load = new JMenuItem("Open game", new ImageIcon(getClass().getResource("/Accessories/Pictures/LoadIcon.png")));
             Newgame.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, ActionEvent.SHIFT_MASK));
             Save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
             Load.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
             file.add(Newgame);
             file.add(Save);
             file.add(Load);
             Newgame.addActionListener(new ActionListener() {
 
                 public void actionPerformed(ActionEvent e) {
                     reset();
                 }
             });
             Save.addActionListener(new ActionListener() {
 
                 public void actionPerformed(ActionEvent e) {
                     saveGame();
                 }
             });
             Load.addActionListener(new ActionListener() {
 
                 public void actionPerformed(ActionEvent e) {
                     try {
                         fromSerialized();
                     } catch (IOException io) {
                         System.out.println("ioExcepion" + io.getMessage());
                     }
                     loadGame();
                 }
             });
         }
         JMenuItem Exit = new JMenuItem("Exit", new ImageIcon(getClass().getResource("/Accessories/Pictures/Exit.png")));
         JMenuItem Meme = new JMenuItem("Meme pieces");
         JMenuItem Regular = new JMenuItem("Regular pieces");
         JMenuItem Rules = new JMenuItem("Rules");
         Meme.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.SHIFT_MASK));
         Regular.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.SHIFT_MASK));
         Exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, ActionEvent.SHIFT_MASK));
         JMenuItem Developers = new JMenuItem("Developers");
         ButtonGroup bg = new ButtonGroup(); //Creating a button group for the two radiobuttons.
 
         //Adding the buttons to the different
         bg.add(Meme);
         bg.add(Regular);
         file.add(Exit);
         settings.add(Meme);
         settings.add(Regular);
         credits.add(Developers);
         help.add(Rules);
 
 
         //Listeners for the buttons in the menubar.
         Exit.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 System.exit(0);
             }
         });
         Developers.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 developers();
             }
         });
 
         Meme.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 chess.changeUI(1);
                 lostPieceWLabel.setIcon(new ImageIcon(getClass().getResource("/Accessories/Pictures/LostPieceWMeme.png")));
                 lostPieceBLabel.setIcon(new ImageIcon(getClass().getResource("/Accessories/Pictures/LostPieceBMeme.png")));
             }
         });
         Regular.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 chess.changeUI(2);
                 lostPieceWLabel.setIcon(new ImageIcon(getClass().getResource("/Accessories/Pictures/LostPieceWNormal.png")));
                 lostPieceBLabel.setIcon(new ImageIcon(getClass().getResource("/Accessories/Pictures/LostPieceBNormal.png")));
             }
         });
         Rules.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 rules();
             }
         });
 
         //Finishing the constructor by packing and setting visible.
         pack();
         setVisible(true);
         lan(choice);
     }
 
     //METHODS
     //Method for adding info to the lost pieces table.
     public void lostpieceTableW() {
         String res = "";
         for (int i = 0; i < counterS.length; i++) {
             res += ": " + counterS[i] + "\n";
         }
         lostPieceW.setText(res);
     }
 
     public void lostpieceTableB() {
         String res = "";
         for (int i = 0; i < counterH.length; i++) {
             res += ": " + counterH[i] + "\n";
         }
         lostPieceB.setText(res);
     }
     //A reset method for the 'New Game' option in the menubar.
 
     public void reset() {
         String[] options = {"Join Lan", "Host Lan", "Normal"};
         int choice = showOptionDialog(null, "Choose Gametype", null, OK_OPTION, QUESTION_MESSAGE, null, options, null);
         System.out.println(choice);
         remove(chess);
         remove(scrollpane);
         remove(scrollpane2);
         remove(background);
         counterH = new int[6];
         counterS = new int[6];
         lostpieceTableW();
         lostpieceTableB();
         textarea.setText("");
         textarea2.setText("");
         chess = new Chess(choice);
         timerS.reset();
         timerH.reset();
         scrollpane = new JScrollPane(textarea);
         scrollpane2 = new JScrollPane(textarea2);
         chess.addChessListener(chessL);
         setConstraints();
         add(chess);
         add(scrollpane, SpringLayout.WEST);
         add(scrollpane2, SpringLayout.EAST);
         settings();
         add(background);
         lostPieceWLabel.setIcon(new ImageIcon(getClass().getResource("/Accessories/Pictures/LostPieceWNormal.png")));
         lostPieceBLabel.setIcon(new ImageIcon(getClass().getResource("/Accessories/Pictures/LostPieceBNormal.png")));
         repaint();
         setVisible(true);
     }
 
     public void developers() {
         JLabel devpicture = new JLabel(new ImageIcon(getClass().getResource("/Accessories/Pictures/Developers.png")));
         JFrame devlabel = new JFrame();
         devlabel.add(devpicture);
         devlabel.pack();
         devlabel.setVisible(true);
     }
     //Method for creating the rules page.
 
     public void rules() {
         Rules rules = new Rules();
         JFrame helplabel = new JFrame();
         JTextArea helparea = rules.getRules();
         JLabel helpbg = new JLabel(new ImageIcon(getClass().getResource("/Accessories/Pictures/Helpbackground.png")));
         JScrollPane helppane = new JScrollPane(helparea);
         helplabel.setTitle("Rules");
         helplabel.setPreferredSize(new Dimension(500, 400));
         helparea.setFont(new Font("Arial", Font.PLAIN, 15));
         helparea.setOpaque(false);
         helppane.setOpaque(false);
         helparea.setEditable(true);
         helparea.setOpaque(false);
         helparea.setSelectionStart(0);
         helparea.setSelectionEnd(0);
         helppane.getViewport().setOpaque(false);
         helppane.setBorder(null);
         helppane.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
         helplabel.add(helppane, BorderLayout.WEST);
         helplabel.add(helpbg, BorderLayout.EAST);
         helplabel.pack();
         helplabel.setResizable(true);
         helplabel.setVisible(true);
     }
     //Settings for the log and lost pieces table.
 
     public void settings() {
         textarea.setEditable(false);
         textarea2.setEditable(false);
         textarea.setOpaque(false);
         scrollpane.setOpaque(false);
         scrollpane.getViewport().setOpaque(false);
         scrollpane.setBorder(null);
         textarea2.setOpaque(false);
         scrollpane2.setOpaque(false);
         scrollpane2.getViewport().setOpaque(false);
         scrollpane2.setBorder(null);
         textarea.setForeground(Color.white);
         textarea2.setForeground(Color.white);
         scrollpane.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
         scrollpane2.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
         lostPieceW.setOpaque(false);
         lostPieceW.setEditable(false);
         lostPieceW.setFont(new Font("SansSerif", 4, 31));
         lostPieceW.setForeground(Color.white);
         lostPieceB.setOpaque(false);
         lostPieceB.setEditable(false);
         lostPieceB.setFont(new Font("SansSerif", 4, 31));
         lostPieceB.setForeground(Color.white);
         blackgif.setVisible(false);
     }
     //Adding constraints to the different components, determining their positions.
 
     public void setConstraints() {
         layout.putConstraint(SpringLayout.WEST, scrollpane, 35, SpringLayout.WEST, contentPane);
         layout.putConstraint(SpringLayout.NORTH, scrollpane, 20, SpringLayout.NORTH, contentPane);
         layout.putConstraint(SpringLayout.NORTH, scrollpane2, 20, SpringLayout.NORTH, contentPane);
         layout.putConstraint(SpringLayout.WEST, scrollpane2, 920, SpringLayout.WEST, contentPane);
         layout.putConstraint(SpringLayout.EAST, chess, 840, SpringLayout.WEST, contentPane);
         layout.putConstraint(SpringLayout.NORTH, chess, 30, SpringLayout.NORTH, contentPane);
         layout.putConstraint(SpringLayout.NORTH, timerS, 223, SpringLayout.WEST, contentPane);
         layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, timerS, 102, SpringLayout.WEST, contentPane);
         layout.putConstraint(SpringLayout.NORTH, timerH, 223, SpringLayout.WEST, contentPane);
         layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, timerH, 982, SpringLayout.WEST, contentPane);
         layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, lostPieceW, 165, SpringLayout.WEST, contentPane);
         layout.putConstraint(SpringLayout.NORTH, lostPieceW, 385, SpringLayout.NORTH, contentPane);
         layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, lostPieceWLabel, 130, SpringLayout.WEST, contentPane);
         layout.putConstraint(SpringLayout.NORTH, lostPieceWLabel, 360, SpringLayout.NORTH, contentPane);
         layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, lostPieceB, 1045, SpringLayout.WEST, contentPane);
         layout.putConstraint(SpringLayout.NORTH, lostPieceB, 385, SpringLayout.NORTH, contentPane);
         layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, lostPieceBLabel, 1010, SpringLayout.WEST, contentPane);
         layout.putConstraint(SpringLayout.NORTH, lostPieceBLabel, 360, SpringLayout.NORTH, contentPane);
         layout.putConstraint(SpringLayout.NORTH, whitegif, 268, SpringLayout.NORTH, contentPane);
         layout.putConstraint(SpringLayout.WEST, whitegif, 1, SpringLayout.WEST, contentPane);
         layout.putConstraint(SpringLayout.NORTH, blackgif, 267, SpringLayout.NORTH, contentPane);
         layout.putConstraint(SpringLayout.WEST, blackgif, 881, SpringLayout.WEST, contentPane);
     }
 
     public void save(String title, int index) {
         PieceLabel[] table = new PieceLabel[64];
         for (int i = 0; i < table.length; i++) {
             if (chess.getPiece(i) instanceof PieceLabel) {
                 table[i] = (PieceLabel) chess.getPiece(i);
             }
         }
         SaveGame save = new SaveGame("lan", chess.getTurn(), timerH.getTime(), timerS.getTime(), counterH, counterS, chess.getWhiteLog(), chess.getBlackLog(), table, chess.getPieceTable(), chess.getPassanten(), chess.getEnPassantB(), chess.getEnPassantW(), chess.getEnPassantPW(), chess.getEnPassantPB(), chess.getMeme());
         savegames[index] = save;
     }
 
     public void load(int index) {
         chess.loadGame(savegames[index].getTable(), savegames[index].getTurn(), savegames[index].getLogW(), savegames[index].getLogB(), savegames[index].getPieces(), savegames[index].getPassanten(), savegames[index].getEnPassantB(), savegames[index].getEnPassantW(), savegames[index].getEnPassantPW(), savegames[index].getEnPassantPB(), savegames[index].getMeme());
         timerH.setTime(savegames[index].getTimerW());
         timerS.setTime(savegames[index].getTimerB());
         timerH.pause();
         timerS.pause();
         counterH = savegames[index].getCounterW();
         counterS = savegames[index].getCounterB();
         lostpieceTableW();
         lostpieceTableB();
         textarea.setText(chess.getWhiteLog());
         textarea2.setText(chess.getBlackLog());
         if (chess.getTurn() % 2 == 0) {
             blackgif.setVisible(false);
             whitegif.setVisible(true);
         } else {
             blackgif.setVisible(true);
             whitegif.setVisible(false);
         }
     }
 
     public void load(SaveGame save) {               //boolean passanten2, int enPassantB2, int enPassantW2, Point enPassantPW2, Point enPassantPB2, boolean meme2){
         chess.loadGame(save.getTable(), save.getTurn(), save.getLogW(), save.getLogB(), save.getPieces(), save.getPassanten(), save.getEnPassantB(), save.getEnPassantW(), save.getEnPassantPW(), save.getEnPassantPB(), save.getMeme());
         timerH.setTime(save.getTimerW());
         timerS.setTime(save.getTimerB());
         timerH.pause();
         timerS.pause();
         counterH = save.getCounterW();
         counterS = save.getCounterB();
         lostpieceTableW();
         lostpieceTableB();
         textarea.setText(chess.getWhiteLog());
         textarea2.setText(chess.getBlackLog());
         if (chess.getTurn() % 2 == 0) {
             blackgif.setVisible(false);
             whitegif.setVisible(true);
         } else {
             blackgif.setVisible(true);
             whitegif.setVisible(false);
         }
     }
     //A method that changes the GUI based on movement.
     ChessListener chessL = new ChessListener() {
 
         public void chessReceived(ChessEvent event) {
             if (event.team() == 1) {
                 if (event.piece() != -1) {
                     counterH[event.piece()]++;
                 }
                 timerH.resume();
                 timerS.pause();
                 textarea.setText(chess.getWhiteLog());
                 lostpieceTableB();
                 whitegif.setVisible(false);
                 blackgif.setVisible(true);
             } else if (event.team() == 2) {
                 if (event.piece() != -1) {
                     counterS[event.piece()]++;
                 }
                 timerH.pause();
                 timerS.resume();
                 textarea2.setText(chess.getBlackLog());
                 lostpieceTableW();
                 blackgif.setVisible(false);
                 whitegif.setVisible(true);
             }
         }
     };
 
     public void saveGame() {
         JButton button1 = new JButton("blank");
         try {
             button1 = new JButton(savegames[0].getTitle());
         } catch (NullPointerException npe) {
         }
         JButton button2 = new JButton("blank");
         try {
             button2 = new JButton(savegames[1].getTitle());
         } catch (NullPointerException npe) {
         }
         JButton button3 = new JButton("blank");
         try {
             button3 = new JButton(savegames[2].getTitle());
         } catch (NullPointerException npe) {
         }
         JButton button4 = new JButton("blank");
         try {
             button4 = new JButton(savegames[3].getTitle());
         } catch (NullPointerException npe) {
         }
         JButton button5 = new JButton("blank");
         try {
             button5 = new JButton(savegames[4].getTitle());
         } catch (NullPointerException npe) {
         }
         JButton button6 = new JButton("blank");
         try {
             button6 = new JButton(savegames[5].getTitle());
         } catch (NullPointerException npe) {
         }
 
         button1.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 JOptionPane.getRootFrame().dispose();
                 String a = showInputDialog("Name: ");
                 if (a == null || a.equals("")) {
                     JOptionPane.getRootFrame().dispose();
                 } else {
                     save(a, 0);
                     try {
                         serialize();
                     } catch (IOException ioe) {
                     }
                 }
             }
         });
         button2.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 JOptionPane.getRootFrame().dispose();
                 String a = showInputDialog("Name: ");
                 if (a == null || a.equals("")) {
                     JOptionPane.getRootFrame().dispose();
                 } else {
                     save(a, 1);
                     try {
                         serialize();
                     } catch (IOException ioe) {
                     }
                 }
             }
         });
         button3.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 JOptionPane.getRootFrame().dispose();
                 String a = showInputDialog("Name: ");
                 if (a == null || a.equals("")) {
                     JOptionPane.getRootFrame().dispose();
                 } else {
                     save(a, 2);
                     try {
                         serialize();
                     } catch (IOException ioe) {
                     }
                 }
             }
         });
         button4.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 JOptionPane.getRootFrame().dispose();
                 String a = showInputDialog("Name: ");
                 if (a == null || a.equals("")) {
                     JOptionPane.getRootFrame().dispose();
                 } else {
                     save(a, 3);
                     try {
                         serialize();
                     } catch (IOException ioe) {
                     }
                 }
             }
         });
         button5.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 JOptionPane.getRootFrame().dispose();
                 String a = showInputDialog("Name: ");
                 if (a == null || a.equals("")) {
                     JOptionPane.getRootFrame().dispose();
                 } else {
                     save(a, 4);
                     try {
                         serialize();
                     } catch (IOException ioe) {
                     }
                 }
             }
         });
         button6.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 JOptionPane.getRootFrame().dispose();
                 String a = showInputDialog("Name: ");
                 if (a == null || a.equals("")) {
                     JOptionPane.getRootFrame().dispose();
                 } else {
                     save(a, 5);
                     try {
                         serialize();
                     } catch (IOException ioe) {
                     }
                 }
             }
         });
 
         Object[] group = {button1, button2, button3, button4, button5, button6};
         showConfirmDialog(null, group, "Save", DEFAULT_OPTION, PLAIN_MESSAGE);
     }
 
     public void loadGame() {
         JButton button1 = new JButton("blank");
         try {
             button1 = new JButton(savegames[0].getTitle());
         } catch (NullPointerException npe) {
         }
         JButton button2 = new JButton("blank");
         try {
             button2 = new JButton(savegames[1].getTitle());
         } catch (NullPointerException npe) {
         }
         JButton button3 = new JButton("blank");
         try {
             button3 = new JButton(savegames[2].getTitle());
         } catch (NullPointerException npe) {
         }
         JButton button4 = new JButton("blank");
         try {
             button4 = new JButton(savegames[3].getTitle());
         } catch (NullPointerException npe) {
         }
         JButton button5 = new JButton("blank");
         try {
             button5 = new JButton(savegames[4].getTitle());
         } catch (NullPointerException npe) {
         }
         JButton button6 = new JButton("blank");
         try {
             button6 = new JButton(savegames[5].getTitle());
         } catch (NullPointerException npe) {
         }
 
         button1.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 try {
                     load(0);
                     JOptionPane.getRootFrame().dispose();
                 } catch (NullPointerException np) {
                     System.out.println("NullPointer load");
                 }
             }
         });
         button2.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 try {
                     load(1);
                     JOptionPane.getRootFrame().dispose();
                 } catch (NullPointerException np) {
                 }
             }
         });
         button3.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 try {
                     load(2);
                     JOptionPane.getRootFrame().dispose();
                 } catch (NullPointerException np) {
                 }
             }
         });
         button4.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 try {
                     load(3);
                     JOptionPane.getRootFrame().dispose();
                 } catch (NullPointerException np) {
                 }
             }
         });
         button5.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 try {
                     load(4);
                     JOptionPane.getRootFrame().dispose();
                 } catch (NullPointerException np) {
                 }
             }
         });
 
         button6.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 try {
                     load(5);
                     JOptionPane.getRootFrame().dispose();
                 } catch (NullPointerException np) {
                 }
             }
         });
 
         Object[] group = {button1, button2, button3, button4, button5, button6};
         showConfirmDialog(null, group, "Load", DEFAULT_OPTION, PLAIN_MESSAGE);
     }
 
     public void serialize() throws IOException {
         try {
             FileOutputStream utstrøm = new FileOutputStream("save.ser");
             ObjectOutputStream ut = new ObjectOutputStream(utstrøm);
             ut.writeObject(savegames);
             ut.close();
         } catch (EOFException eof) {
         }
     }
 
     public void fromSerialized() throws IOException {
         FileInputStream innstrøm = new FileInputStream("save.ser");
         ObjectInputStream inn = new ObjectInputStream(innstrøm);
         try {
             Object tab = inn.readObject();
             savegames = (SaveGame[]) tab;
         } catch (EOFException eof) {
         } catch (ClassNotFoundException cnfe) {
             cnfe.printStackTrace();
         } catch (IOException cnfe) {
         }
         inn.close();
     }
 
     public void lan(int ch) {
         try {
             switch (ch) {
                 case 2:
                     return;
                 case 1:
                     setSize(1090, 720);
                     s = (new ServerSocket(7777)).accept();
                     oops = new ObjectOutputStream(s.getOutputStream());
                     PieceLabel[] table = new PieceLabel[64];
                     for (int i = 0; i < table.length; i++) {
                         if (chess.getPiece(i) instanceof PieceLabel) {
                             table[i] = (PieceLabel) chess.getPiece(i);
                         }
                     }
                     SaveGame save = new SaveGame("lan", chess.getTurn(), timerH.getTime(), timerS.getTime(), counterH, counterS, chess.getWhiteLog(), chess.getBlackLog(), table, chess.getPieceTable(), chess.getPassanten(), chess.getEnPassantB(), chess.getEnPassantW(), chess.getEnPassantPW(), chess.getEnPassantPB(), chess.getMeme());
                     oops.writeObject(save);
                     chessAction.ready = false;
                     break;
                 case 0:
                     setSize(1090, 720);
                 default:
                     s = new Socket(showInputDialog("Host IP: "), 7777);
                     chessAction.ready = true;
             }
             while (true) {
                 oips = new ObjectInputStream(s.getInputStream());
                 SaveGame save = (SaveGame) (oips.readObject());
                 load(save);
                 chessAction.ready = true;
                 while (chessAction.ready) {
                     Thread.sleep(100);
                 }
                 oops = new ObjectOutputStream(s.getOutputStream());
                 PieceLabel[] table = new PieceLabel[64];
                 for (int i = 0; i < table.length; i++) {
                     if (chess.getPiece(i) instanceof PieceLabel) {
                         table[i] = (PieceLabel) chess.getPiece(i);
                     }
                 }
                 save = new SaveGame("lan", chess.getTurn(), timerH.getTime(), timerS.getTime(), counterH, counterS, chess.getWhiteLog(), chess.getBlackLog(), table, chess.getPieceTable(), chess.getPassanten(), chess.getEnPassantB(), chess.getEnPassantW(), chess.getEnPassantPW(), chess.getEnPassantPB(), chess.getMeme());
                 oops.writeObject(save);
             }
 
         } catch (Exception e) {
             System.out.println(e);
             System.exit(1);
         }
     }
 }
