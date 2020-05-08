 package screens.server;
 
 import controllers.ConnectionFactory;
 import controllers.WorkFlowManager;
 import controllers.server.GameController;
 import model.Player;
 import screens.controls.MainFrame;
 import views.server.GameView;
 
 import javax.swing.*;
 import javax.swing.table.DefaultTableModel;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 
 public class GameScreen implements GameView {
     private final MainFrame mainFrame;
     private GameController controller;
     private JButton cancelButton;
     JTable playersDetails;
     DefaultTableModel dm;
     JTextArea logs;
     String[] columnHeaders = {"Player Names", "Roles"};
 
     public GameScreen (MainFrame mainFrame, GameController controller) {
         this.mainFrame = mainFrame;
         this.controller = controller;
     }
 
     @Override
     public void display (String[][] details) {
         final JPanel gamePanel = new JPanel ();
 
         dm = new DefaultTableModel (details, columnHeaders);
         playersDetails = new JTable (dm);
         playersDetails.setEnabled (false);
         JScrollPane playersScroll = new JScrollPane (playersDetails);
         playersScroll.setBounds (20, 50, 300, 300);
 
         cancelButton = new JButton ("Cancel");
         cancelButton.setFont (new Font ("Times New Roman", Font.BOLD, 18));
         cancelButton.setBounds (20, 370, 100, 30);
         cancelButton.addActionListener (new ActionListener () {
             @Override
             public void actionPerformed (ActionEvent e) {
                 gamePanel.setVisible (false);
             }
         });
 
         JLabel playersList = new JLabel ("Players List : ");
         playersList.setForeground (Color.WHITE);
         playersList.setFont (new Font ("Times New Roman", Font.ITALIC + Font.BOLD, 16));
         playersList.setBounds (20, 10, 150, 30);
 
         JLabel log = new JLabel ("Log : ");
         log.setForeground (Color.WHITE);
         log.setFont (new Font ("Times New Roman", Font.ITALIC + Font.BOLD, 16));
         log.setBounds (350, 10, 150, 30);
 
         logs = new JTextArea ("WELCOME");
         logs.setForeground (Color.BLACK);
         logs.setEditable (false);
         logs.setFont (new Font ("Times New Roman", Font.PLAIN, 16));
         JScrollPane logScroll = new JScrollPane (logs);
         logScroll.setBounds (350, 50, 300, 300);
 
         gamePanel.setLayout (null);
         gamePanel.setBackground (Color.BLACK);
         gamePanel.add (playersScroll);
         gamePanel.add (cancelButton);
         gamePanel.add (playersList);
         gamePanel.add (log);
         gamePanel.add (logScroll);
 
         mainFrame.setFrameSize (700, 450);
         mainFrame.activate (gamePanel);
         buttonHandler ();
     }
 
     @Override
     public void updatePlayersList (String[][] playersChart) {
         playersDetails.removeAll ();
         dm.setDataVector (playersChart,columnHeaders);
//        dm = new DefaultTableModel (playersChart, columnHeaders);
//        playersDetails = new JTable (dm);
     }
 
     @Override
     public void updateLog (String s) {
         String timeStamp = new SimpleDateFormat ("hh:mm:ss a: ").format (Calendar.getInstance ().getTime ());
         logs.setText (logs.getText ()+"\n"+timeStamp+s);
     }
 
     private void buttonHandler () {
         cancelButton.addActionListener (new ActionListener () {
             @Override
             public void actionPerformed (ActionEvent e) {
                 controller.stopServer ();
             }
         });
     }
 
     public static void main (String[] args) {
         new GameScreen (new MainFrame (), GameController.createController (new WorkFlowManager (new ConnectionFactory ()), new ArrayList<Player> ())).display (new String[][]{{"Hemanth", "Mafia"}});
     }
 
 }
 
