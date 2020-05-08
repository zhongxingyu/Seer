 package edu.uwm.cs552;
 
 import java.awt.BorderLayout;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.logging.Logger;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.SwingUtilities;
 
 import edu.uwm.cs552.playerchoice.BuildTrackChoice;
 import edu.uwm.cs552.playerchoice.MoveTrainChoice;
 import edu.uwm.cs552.playerchoice.PlayerChoice;
 import edu.uwm.cs552.util.JLoggerList;
 
 public class PlayerFrame extends JFrame implements Observer {
   public PlayerFrame(RailGame g, Player p, HexBoard b) {
     game = g;
     player = p;
     hexBoardPanel = new HexBoardPanel(b);
 
     game.addObserver(this);
 
     getContentPane().setLayout(new BorderLayout());
     getContentPane().add(new JScrollPane(hexBoardPanel));
 
     JPanel panel = new JPanel();
     panel.add(new ScaleComboBox(hexBoardPanel));
 
     if (p == null) {
       setTitle("Game Control Panel");
     }
     else {
       setTitle("Player " + p.getNum());
 
       moveButton = new JButton("Move");
       moveButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent arg0) {
           doMove();
         }
       });
 
       clearLogButton = new JButton("Clear Log");
       clearLogButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
           doClearLog();
         }
       });
 
       choiceComboBox = new JComboBox(playerChoices);
       choiceComboBox.setMaximumRowCount(playerChoices.length);
 
       MouseAdapter mouseActions = new MouseAdapter() {
         @Override
         public void mouseDragged(MouseEvent arg0) {
           doPlayerChoice(arg0.getPoint());
         }
 
         @Override
         public void mousePressed(MouseEvent arg0) {
           doPlayerChoice(arg0.getPoint());
         }
       };
       hexBoardPanel.addMouseListener(mouseActions);
       hexBoardPanel.addMouseMotionListener(mouseActions);
 
       panel.add(moveButton);
       panel.add(choiceComboBox);
       panel.add(clearLogButton);
       panel.add(moneyLabel);
       setMoneyLabelText();
       logPanel = new JScrollPane(logList);
       getContentPane().add(logPanel, BorderLayout.SOUTH);
     }
     getContentPane().add(panel, BorderLayout.NORTH);
     setSize(600, 450);
     setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
     addWindowListener(new WindowAdapter() {
       @Override
       public void windowClosing(WindowEvent we) {
         // doQuit();
       }
     });
   }
 
   public void paint(Graphics g) {
     super.paint(g);
 
    Graphics hbpg = hexBoardPanel.getGraphics();
    game.draw(hbpg, hexBoardPanel.getScale(), player);
   }
 
   @Override
   public void update(Observable arg0, Object arg1) {
     verifyTurns();
     setMoneyLabelText();
 
     SwingUtilities.invokeLater(new Runnable() {
       @Override
       public void run() {
         repaint();
       }
     });
   }
 
   private void doMove() {
     player.setTurns(player.getTurns() - 1);
     game.doTurn(player);
   }
 
   private void doClearLog() {
 
   }
 
   private void doPlayerChoice(Point point) {
     PlayerChoice choice = (PlayerChoice) choiceComboBox.getSelectedItem();
     choice.doThings(player, point, hexBoardPanel.getScale());
   }
 
   private void setMoneyLabelText() {
     if (player != null) moneyLabel.setText("Money: M$ " + player.getCash());
   }
 
   private void verifyTurns() {
     if (player != null) moveButton.setEnabled(player.getTurns() > 0);
   }
 
   PlayerChoice[] playerChoices = new PlayerChoice[] { new BuildTrackChoice(),
       new MoveTrainChoice(), };
 
   private Player player;
   private RailGame game;
   private JComboBox choiceComboBox;
   private HexBoardPanel hexBoardPanel;
   private JButton moveButton, clearLogButton;
   private JLabel moneyLabel = new JLabel("Hello world");
   private JScrollPane logPanel;
   private Logger log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
   private JLoggerList logList = new JLoggerList(log);
   private static final long serialVersionUID = 1L;
 }
