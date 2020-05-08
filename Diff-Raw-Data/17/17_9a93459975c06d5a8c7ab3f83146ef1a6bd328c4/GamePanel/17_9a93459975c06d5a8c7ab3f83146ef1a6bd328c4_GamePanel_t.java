 package com.cs408.supersweeper;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Properties;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.Timer;
 
 public class GamePanel extends JPanel implements ActionListener {
 
     private static final long serialVersionUID = -974978390404929465L;
     private GameState _gs;
     private Properties _prop = new Properties();
     private int time = 0;
 
 
     //SubComponents of GamePanel
     private GridPanel gridPanel;
     private JPanel labelPanel = new JPanel();
     private JPanel powerUpPanel = new JPanel();
     private JLabel statusLabel = new JLabel();
     private JLabel timeLabel = new JLabel();
     private Timer timeDelay;
     private Powerup missile, extralife, metalDetector;
     private JButton help;
 
 
     /** Constructor */
     public GamePanel(String propFileName) {
             
         // Fetch the specified properties file (throws ioexception if its not there)
         try {
             _prop.load(this.getClass().getResourceAsStream("/" + propFileName));
         } catch (Exception e) {
             System.err.println("Could not locate Properties File: " + propFileName);
             System.exit(1);
         }  
         Utility.validateProperties(_prop);
         
         // Make a new GameState from the specified properties file
         _gs = new GameState(Double.parseDouble(_prop.getProperty("time")), Integer.parseInt(_prop.getProperty("numMines")), Integer.parseInt(_prop.getProperty("gridHeight")), Integer.parseInt(_prop.getProperty("gridWidth")), Integer.parseInt(_prop.getProperty("score")));
 
         
         // set up the gamePanel's internal panel structure
         setLayout(new BorderLayout());
         this.add(labelPanel, BorderLayout.NORTH);
         labelPanel.add(statusLabel);
         labelPanel.add(timeLabel);
         statusLabel.setText("Flags: 0");
 
         //gridPanel.setBorder((BorderFactory.createLineBorder(Color.black)));
         gridPanel = new GridPanel(_gs, this);
         this.add(gridPanel, BorderLayout.CENTER);
         powerUpPanel.setLayout(new FlowLayout());
         
         extralife = new Powerup("+1", 100);
         powerUpPanel.add(extralife);
         extralife.addActionListener(this);
         
         metalDetector = new Powerup("\\_", 150);
         powerUpPanel.add(metalDetector);
         metalDetector.addActionListener(this);
         
         missile = new Powerup("^", 200);
         powerUpPanel.add(missile);
         missile.addActionListener(this);
         
         help = new JButton("?");
         powerUpPanel.add(help);
         help.addActionListener(this);
         
         this.add(powerUpPanel, BorderLayout.SOUTH);
        
         //Set Min Size for this container panel
         int w = GridUnit.sample.getWidth();
         int h = GridUnit.sample.getHeight();
         setPreferredSize(new Dimension(_gs.getGridWidth() * w, _gs.getGridHeight() * h + ((int) (labelPanel.getPreferredSize().getHeight() + powerUpPanel.getPreferredSize().getHeight())) ));
 
         time = (int)_gs.getTime();
         timeLabel.setText(time/60 + ":" + String.format("%02d", time%60));
         timeDelay = new Timer(1000, this);
     }
 
     public void restartLevel() {
        resetTimer();
         _gs.resetGrid();
         gridPanel.resetFirstClick();
         repaint();
     }
 
     public void updateStatusLabel() {
         statusLabel.setText("Flags: " + _gs.getFlagCount());
     }
 
     public void startTimer() {
         this.timeDelay.start();
     }
     
    public void resetTimer() {
        this.timeDelay.stop();
        time = (int)_gs.getTime();
        timeLabel.setText(time/60 + ":" + String.format("%02d", time%60));
    }
    
     
     /** Action Listener */
     public void actionPerformed(ActionEvent e) {
         Object action = e.getSource();
         
         if(action == help) {
             JOptionPane.showMessageDialog(null, helpMessage, "Help", JOptionPane.PLAIN_MESSAGE);
         } else if(action == missile){
             // GamePanel.points = GamePanel.points - missile.getPrice();
             
         } else if(action == metalDetector){
             // GamePanel.points = GamePanel.points - missile.getPrice();
             
         } else if(action == extralife){
             // GamePanel.points = GamePanel.points - missile.getPrice();
             
         } else if(action == timeDelay){
             if(--time == 0){
                 //Game Over
                 timeDelay.stop();
                 timeLabel.setText("0:00");
                 //TODO: Game Economics
                 _gs.endGame(0);
                 
            } else if(_gs.isGameOver()) {
                timeDelay.stop();
             } else {
                 timeLabel.setText(time/60 + ":" + String.format("%02d", time%60));
             }
         }
         
     }
     
     private static final String helpMessage = "Powerups are a powerful tool for winning SuperSweeper.  Their point value will deduct from your points.\n\n" +
     "Extra Lives will save you if you click on a mine.\nThe Metal Detector will allow you to see a small part of the board for a second.\n"
     + "The Missile will destroy a part of the board, safely detonating all mines.";
 
 
 }
