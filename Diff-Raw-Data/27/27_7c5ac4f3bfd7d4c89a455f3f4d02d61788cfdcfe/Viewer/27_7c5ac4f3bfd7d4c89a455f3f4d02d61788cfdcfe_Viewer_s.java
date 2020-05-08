 package ru.nsu.vakhrushev.pusher.viewer;
 
 import ru.nsu.vakhrushev.pusher.controller.Controller;
 import ru.nsu.vakhrushev.pusher.controller.Score;
 import ru.nsu.vakhrushev.pusher.model.Model;
 
 import javax.swing.*;
 import javax.swing.border.BevelBorder;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyListener;
 import java.io.IOException;
 import java.util.Observable;
 import java.util.Observer;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Vakhrushev Maxim
  * Date: 11.04.13
  * Time: 9:23
  */
 public class Viewer extends JFrame implements Observer {
 
     private final static int cellSize = 40;
     private final static int infoWidth = 5 * cellSize;
 
     private JMenuBar menuBar = new JMenuBar();
     private ScoresFrame scoresFrame;
     private HelpFrame helpFrame;
     private JPanel panel = new JPanel();
     private JPanel fieldPanel = new JPanel();
     private InfoComponent infoPanel;
 
     private Model model = null;
     private int fieldWidth = 0;
     private int fieldHeight = 0;
     private String currentLevel;
     private Controller controller;
 
 
     public Viewer(Controller newController)
     {
         helpFrame = new HelpFrame();
         controller = newController;
         setMenuBar();
         setTitle("Pusher");
         setLayout(new GridLayout(1, 1));
         panel.setLayout(new BorderLayout());
         add(panel);
         setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
         setStartWindow();
         pack();
         setLocationRelativeTo(null);
         setResizable(false);
         setVisible(true);
     }
 
     private void setMenuBar()
     {
         JMenu gameMenu = new JMenu("Game");
         JMenuItem newGameItem = new JMenuItem("New game");
         JMenuItem restartGameItem = new JMenuItem("Restart game");
         JMenuItem scoresItem = new JMenuItem("Scores");
         JMenuItem resetItem = new JMenuItem("Reset scores");
         JMenuItem exitItem = new JMenuItem("Exit");
         gameMenu.add(newGameItem);
         gameMenu.addSeparator();
         gameMenu.add(scoresItem);
         gameMenu.add(restartGameItem);
         gameMenu.add(resetItem);
         gameMenu.addSeparator();
         gameMenu.add(exitItem);
 
         JMenu helpMenu = new JMenu("Help");
         JMenuItem viewHelpItem = new JMenuItem("View help");
         JMenuItem aboutItem = new JMenuItem("About");
         helpMenu.add(viewHelpItem);
         helpMenu.add(aboutItem);
 
         restartGameItem.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 try
                 {
                     if (controller.isPlaying())
                     {
                         controller.stopTimer();
                         controller.startLevel(currentLevel);
                         setGameWindow();
                     }
                     else
                     {
                         JOptionPane.showMessageDialog(new JFrame(), "You don't start game yet.");
                     }
                 }
                 catch (IOException ex)
                 {
                     ex.printStackTrace();
                 }
             }
         });
         newGameItem.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                setStartWindow();
             }
         });
         scoresItem.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 scoresFrame.setVisible(true);
             }
         });
         resetItem.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 controller.clearScores();
                 scoresFrame = new ScoresFrame(controller, "scoresProperty.txt");
             }
         });
         exitItem.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 System.exit(0);
             }
         });
         viewHelpItem.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 helpFrame.setVisible(true);
             }
         });
         aboutItem.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 JOptionPane.showMessageDialog(new JFrame(), "Pusher\n" +
                         "Developed by Vakhrushev Maxim\n" +
                         "April 2013");
             }
         });
         menuBar.add(gameMenu);
         menuBar.add(helpMenu);
         setJMenuBar(menuBar);
     }
 
     private void setStartWindow()
     {
         try
         {
             model = null;
             panel.removeAll();
             panel.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 1));
 
             setPreferredSize(new Dimension(500, 300));
             setMaximumSize(new Dimension(500, 300));
             setMinimumSize(new Dimension(500, 300));
             setSize(new Dimension(500, 300));
 
             panel.setPreferredSize(new Dimension(500, 300));
             panel.setMaximumSize(new Dimension(500, 300));
             panel.setMinimumSize(new Dimension(500, 300));
             panel.setSize(new Dimension(500, 300));
 
             final JList <String> list = new JList<>();
             list.setModel(new LevelsListModel("levels/levels.txt"));
             list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
             list.setPreferredSize(new Dimension(300, list.getLastVisibleIndex() * 20));
             list.setMaximumSize(new Dimension(300, list.getLastVisibleIndex() * 20));
             list.setMinimumSize(new Dimension(300, list.getLastVisibleIndex() * 20));
             list.setSize(new Dimension(300, list.getLastVisibleIndex() * 20));
 
             JScrollPane listPane = new JScrollPane(list);
 
             JButton button = new JButton("Start");
             button.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     try
                     {
                         currentLevel = list.getSelectedValue();
                        controller.startLevel(currentLevel);
                        setGameWindow();
                     }
                     catch (IOException ex)
                     {
                         ex.printStackTrace();
                     }
                 }});
             panel.add(listPane);
             panel.add(button);
 
             scoresFrame = new ScoresFrame(controller, "scoresProperty.txt");
 
             pack();
             setLocationRelativeTo(null);
             repaint();
 
         }
         catch (IOException ex)
         {
             ex.printStackTrace();
         }
     }
     private void setGameWindow()
     {
         fieldPanel.removeAll();
         panel.removeAll();
         panel.setLayout(new BorderLayout());
 
         setPreferredSize(new Dimension(fieldWidth + infoWidth, fieldHeight + menuBar.getHeight()));
         setMaximumSize(new Dimension(fieldWidth + infoWidth, fieldHeight + menuBar.getHeight()));
         setMinimumSize(new Dimension(fieldWidth + infoWidth, fieldHeight + menuBar.getHeight()));
         setSize(new Dimension(fieldWidth + infoWidth, fieldHeight + menuBar.getHeight()));
         panel.setPreferredSize(this.getPreferredSize());
         panel.setMaximumSize(this.getPreferredSize());
         panel.setMinimumSize(this.getPreferredSize());
         panel.setSize(this.getPreferredSize());
 
         fieldPanel = new FieldComponent(model, new Dimension(fieldWidth, fieldHeight));
         fieldPanel.setPreferredSize(new Dimension(fieldWidth, fieldHeight));
         fieldPanel.setMaximumSize(new Dimension(fieldWidth, fieldHeight));
         fieldPanel.setMinimumSize(new Dimension(fieldWidth, fieldHeight));
         fieldPanel.setSize(new Dimension(fieldWidth, fieldHeight));
         fieldPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, Color.BLACK, Color.DARK_GRAY));
 
         infoPanel = new InfoComponent(new Dimension(infoWidth, fieldHeight), cellSize);
         infoPanel.setPreferredSize(new Dimension(infoWidth, fieldHeight));
         infoPanel.setMaximumSize(new Dimension(infoWidth, fieldHeight));
         infoPanel.setMinimumSize(new Dimension(infoWidth, fieldHeight));
         infoPanel.setSize(new Dimension(infoWidth, fieldHeight));
         infoPanel.setLayout(new GridLayout(2, 1));
         infoPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, Color.BLACK, Color.DARK_GRAY));
 
         panel.add(fieldPanel, "Center");
         panel.add(infoPanel, "East");
 
         fieldPanel.setFocusable(true);
         fieldPanel.requestFocusInWindow();
         fieldPanel.addKeyListener(new KeyEventListener(model, controller.getScore()));
 
         pack();
         setLocationRelativeTo(null);
         repaint();
     }
 
     public void updateInfoPanel(Score score)
     {
//        System.out.println("Steps :" + score.getSteps() + ", Time :" + score.getTime());
         Component [] components = infoPanel.getComponents();
         ((JLabel) components[0]).setText("Steps :" + score.getSteps());
         ((JLabel) components[1]).setText("Time :" + score.getTime());
         infoPanel.repaint();
     }
 
     public void setModel(Model newModel)
     {
         model = newModel;
         fieldPanel.removeAll();
         fieldWidth = model.getWidth() * cellSize + infoWidth;
         fieldHeight = model.getHeight() * cellSize;
     }
 
     public String getPlayerName ()
     {
         return JOptionPane.showInputDialog(this, "You made a record! Enter your name:");
     }
     public void showEndLevelMessage()
     {
         JOptionPane.showMessageDialog(this, "You win");
     }
 
     @Override
     public void update(Observable o, Object arg)
     {
         if (model.isVictory())
         {
             controller.exitLevel();
             KeyListener[] kel = fieldPanel.getKeyListeners();
             for (KeyListener k : kel)
             {
                 fieldPanel.removeKeyListener(k);
             }
             setStartWindow();
         }
         else
         {
             fieldPanel.repaint();
         }
     }
 }
