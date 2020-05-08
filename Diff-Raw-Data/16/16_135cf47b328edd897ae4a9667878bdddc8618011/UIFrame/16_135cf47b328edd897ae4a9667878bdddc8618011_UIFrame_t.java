 package dominoes;
 
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 
 // Frame which contains game UI implemented in UI class
 public class UIFrame extends JFrame {//} implements ActionListener {
     MenuBar menuBar;
     int windowWidth = 1400;
     int windowHeight = 800;
     private UI ui;
 
     public UIFrame() {
         super("Awesome Dominoes");
         setSize(windowWidth, windowHeight);
         setPreferredSize(new Dimension(windowWidth, windowHeight));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
         super.addWindowListener(new WindowAdapter() {
             @Override
             public void windowClosing(WindowEvent e) {
                exitOption();
             }
         });
 
         setupMenuBar();
 
         this.validate();
         this.setVisible(true);
     }
 
     //Creates menu bar for navigation
     private void setupMenuBar() {
         menuBar = new MenuBar();
         this.setMenuBar(menuBar);
         Menu dom = new Menu("Dominoes");
         menuBar.add(dom);
         Menu about = new Menu("About");
         menuBar.add(about);
         MenuItem newGame = new MenuItem("New Game", new MenuShortcut(KeyEvent.VK_N));
         MenuItem exitGame = new MenuItem("Exit Dominoes", new MenuShortcut(KeyEvent.VK_X));
         MenuItem aboutItem = new MenuItem("About AD", new MenuShortcut(KeyEvent.VK_A));
         newGame.setActionCommand("NewGame"); // set the command
         //newGame.addActionListener(ui.infoPanel);
         newGame.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 newOption();
             }
         });
         exitGame.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 exitOption();
             }
         });
         dom.add(newGame);
         dom.add(exitGame);
         aboutItem.setActionCommand("About");
         aboutItem.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 String aboutTxt = "Awesome Dominoes was written by:\nAbbie James\nNick Mackin\nTimothy Baldock";
                 JOptionPane.showMessageDialog(new JFrame(), aboutTxt, "About Awesome Dominoes",
                         JOptionPane.INFORMATION_MESSAGE);
             }
         });
         about.add(aboutItem);
     }
 
     private void newOption() {
         int x = JOptionPane.showOptionDialog(new JFrame(), "Leave this game?", "New Game",
                 JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,null,null,null);
         if (x==0) {
             ui.displayRoundWinner(null);
             showNewGameDialog();
         }
     }
 
     public void exitOption() {
         Object[] options = {"Yes, sorry", "No, whoops!"};
         int x = JOptionPane.showOptionDialog(new JFrame(), "Are you sure you want to quit?",
                 "Leaving so soon?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
                 null, options, options[1]);
         if (x == 1) {
         } else {
             this.setVisible(false);
             System.exit(0);
         }
     }
 
     public void showNewGameDialog() {
         NewGameDialog dialog = new NewGameDialog(this, this);
         dialog.setModal(true);
         dialog.setVisible(true);
 
         if (this.ui != null) {
             this.remove(this.ui);
         }
 
         // Collect results from dialog and use them to create game in new UI panel
         this.ui = new UI(dialog.player1Type, dialog.player1Name, dialog.player2Type, dialog.player2Name, dialog.targetScore);
 
         this.add(this.ui);
         this.validate();
     }
 }
