 package view;
 
import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.List;
 
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.GroupLayout.Group;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JTextField;
 
 import model.Card;
 import model.Game;
 import model.GameProperties;
 import server.HumanServer;
 import client.HumanClient;
 
 public class View
 {
     private HumanServer server;
     private HumanClient client;
 
     private Game game;
 
     private JFrame frame;
 
     private JTextField notificationField;
     private GamePanel gamePanel;
 
     private JButton createRoomButton, closeRoomButton, joinRoomButton,
             leaveRoomButton, newGameButton, newRoundButton;
     private JButton[] buttons;
 
     private JButton actionButton;
 
     public View(HumanClient client)
     {
         this.server = new HumanServer(this);
         this.client = client;
 
         frame = new JFrame("Tractor");
        frame.getContentPane().setBackground(Color.GREEN);
 
         notificationField = new JTextField("Welcome to Tractor");
         notificationField.setEditable(false);
 
         createRoomButton = new JButton("Create Room");
         createRoomButton.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 try
                 {
                     server.startServer(3003);
                 }
                 catch (IOException e2)
                 {
                     JOptionPane.showMessageDialog(frame, e2.getMessage());
                 }
             }
         });
         closeRoomButton = new JButton("Close Room");
         closeRoomButton.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 if (JOptionPane.showConfirmDialog(frame,
                         "Are you sure you want to close the room?") == JOptionPane.YES_OPTION)
                 {
                     server.close();
                 }
             }
         });
         joinRoomButton = new JButton("Join Room");
         joinRoomButton.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 // String input = JOptionPane
                 // .showInputDialog("Enter IP: e.g. 192.168.0.1");
                 // TODO Set Cancel to go back to original state.
                 String input = "127.0.0.1";
 
                 try
                 {
                     /* Try to parse input IP */
                     String[] addressStr = input.split("\\.");
                     byte[] address = new byte[4];
                     for (int i = 0; i < 4; i++)
                         address[i] = (byte) Short.parseShort(addressStr[i]);
 
                     /* Connect to server */
                     View.this.client.connect(3003, address);
                 }
                 catch (Exception e2)
                 {
                     JOptionPane.showMessageDialog(frame, e2.getMessage());
                 }
             }
         });
         leaveRoomButton = new JButton("Leave Room");
         leaveRoomButton.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 if (JOptionPane.showConfirmDialog(frame,
                         "Are you sure you want to leave the room?") == JOptionPane.YES_OPTION)
                 {
                     View.this.client.close();
                 }
             }
         });
         newGameButton = new JButton("New Game");
         newGameButton.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 // TODO Set Cancel to go back to original state.
                 GameProperties properties = new GameProperties();
                 properties.numDecks = 2;
                 properties.find_a_friend = false;
                 View.this.client.requestStartGame(properties);
             }
         });
         newRoundButton = new JButton("New Round");
         newRoundButton.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 View.this.client.requestStartRound();
             }
         });
         buttons = new JButton[]
         { createRoomButton, closeRoomButton, joinRoomButton, leaveRoomButton,
                 newGameButton, newRoundButton };
         for (JButton button : buttons)
             button.setVisible(false);
         createRoomButton.setVisible(true);
         joinRoomButton.setVisible(true);
 
         actionButton = new JButton();
         actionButton.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 if (game != null && game.started())
                 {
                     List<Card> cards = gamePanel.resetSelected();
                     if (cards.isEmpty())
                         return;
                     switch (game.getState())
                     {
                         case AWAITING_SHOW:
                             View.this.client.requestShowCards(cards);
                             break;
                         case AWAITING_KITTY:
                             View.this.client.requestMakeKitty(cards);
                             break;
                         case AWAITING_PLAY:
                             View.this.client.requestPlayCards(cards);
                             break;
                     }
                 }
             }
         });
 
         gamePanel = new GamePanel(client);
 
         frame.setSize(1000, 800);
         frame.setResizable(false);
 
         arrange();
     }
 
     public void arrange()
     {
         GroupLayout layout = new GroupLayout(frame.getContentPane());
 
         Group sequentialGroup = layout.createSequentialGroup();
         Group parallelGroup = layout.createParallelGroup(Alignment.BASELINE);
         sequentialGroup.addComponent(notificationField);
         parallelGroup.addComponent(notificationField);
         for (JButton button : buttons)
         {
             sequentialGroup.addComponent(button);
             parallelGroup.addComponent(button);
         }
        layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER)
                 .addGroup(sequentialGroup).addComponent(gamePanel)
                 .addComponent(actionButton));
         layout.setVerticalGroup(layout.createSequentialGroup()
                 .addGroup(parallelGroup).addComponent(gamePanel)
                 .addComponent(actionButton));
 
         frame.getContentPane().setLayout(layout);
     }
 
     public void show()
     {
         try
         {
             gamePanel.loadImages();
         }
         catch (IOException e)
         {
             JOptionPane.showMessageDialog(frame,
                     "Error: could not load card images.");
         }
         frame.setVisible(true);
         frame.setFocusable(true);
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
     }
 
     public void createRoom()
     {
         try
         {
             /* Find external IP */
             String IP = new BufferedReader(new InputStreamReader(new URL(
                     "http://api.exip.org/?call=ip").openStream())).readLine();
             notificationField.setText("Setting up server...");
 
             notificationField.setText("Your IP is " + IP + ". Players:");
 
             createRoomButton.setVisible(false);
             closeRoomButton.setVisible(true);
             frame.repaint();
         }
         catch (Exception e)
         {
             JOptionPane.showMessageDialog(frame, e.getMessage());
         }
     }
 
     public void closeRoom()
     {
         closeRoomButton.setVisible(false);
         createRoomButton.setVisible(true);
         frame.repaint();
     }
 
     public void joinRoom()
     {
         notificationField.setText("Joined room.");
         joinRoomButton.setVisible(false);
         leaveRoomButton.setVisible(true);
         newGameButton.setVisible(true);
         newRoundButton.setVisible(true);
         frame.repaint();
     }
 
     public void leaveRoom()
     {
         notificationField.setText("Left room.");
         leaveRoomButton.setVisible(false);
         joinRoomButton.setVisible(true);
         newGameButton.setVisible(false);
         newRoundButton.setVisible(false);
         frame.repaint();
     }
 
     public void requestStartGame()
     {
         notificationField.setText("Sending new game request...");
         frame.repaint();
     }
 
     public void startGame(Game game)
     {
         notificationField
                 .setText("New game started. Click 'New Round' to begin.");
         this.game = game;
         gamePanel.setGame(game);
         frame.repaint();
     }
 
     public void requestStartRound()
     {
         notificationField.setText("Waiting for other players...");
         frame.repaint();
     }
 
     public void startRound()
     {
         notificationField.setText("New round started.");
         frame.repaint();
     }
 
     public void repaint()
     {
         if (game != null && game.started())
             switch (game.getState())
             {
                 case AWAITING_SHOW:
                     actionButton.setText("SHOW");
                     break;
                 case AWAITING_KITTY:
                     actionButton.setText("SET KITTY");
                     break;
                 case AWAITING_PLAY:
                     actionButton.setText("PLAY");
                     break;
             }
         frame.repaint();
     }
 }
