package Userscreens;

import ServerClient.Server;
 
 import javax.swing.*;
 import java.awt.*;
 import java.io.IOException;
 import java.net.Socket;
 
 //Job:- Understands to display a window to connect to a server

 public class StartServerScreen {
     public final int totalPlayers = 1;
     public Server server;
     JFrame startServerFrame;
     JLabel playersJoinedLabel;
     JButton startGameButton;
     ImagePanel startServerScreenImage;
     DefaultListModel<String> players = new DefaultListModel<String>();
     JList<String> playersList;
     JPanel displayMessage;
 
     public StartServerScreen() {
         startServer();
 
         startServerFrame = new JFrame("Starting Server");
         startServerFrame.setBackground(Color.BLACK);
         startServerFrame.setBounds(100, 100, 600, 600);
         startServerFrame.setVisible(true);
 
         startServerScreenImage = new ImagePanel(new ImageIcon("images/joinServerScreen.jpg").getImage());
         startServerFrame.getContentPane().add(startServerScreenImage);
         startServerFrame.pack();
 
         //JLabel
 
         playersJoinedLabel = new JLabel("Players Joined");
         Font font = new Font("Monospaced", Font.BOLD, 20);
         playersJoinedLabel.setFont(font);
         playersJoinedLabel.setForeground(Color.WHITE);
         playersJoinedLabel.setBackground(Color.BLACK);
         playersJoinedLabel.setSize(200, 200);
         playersJoinedLabel.setLocation(100, -50);
 
         //JList
         playersList = new JList<String>(players);
         playersList.setSize(200, 200);
         playersList.setLocation(100, 80);
         Font f = new Font("Monospaced", Font.BOLD, 15);
         playersList.setFont(f);
 
         //StartButton
         startGameButton = new JButton("Start Game");
         startGameButton.setSize(145, 50);
         startGameButton.setLocation(800, 800);
         startGameButton.setFont(new Font("Verdana", Font.BOLD, 14));
         startGameButton.setForeground(Color.ORANGE);
         startGameButton.setBackground(Color.BLACK);
 
         //Frame Contents
         startServerScreenImage.add(playersList);
         startServerScreenImage.add(playersJoinedLabel);
         startServerScreenImage.add(startGameButton);
     }
 
     public void startServer() {
         try {
             server = Server.createServer(totalPlayers);
             server.startEvents();
         } catch (IOException e) {
             displayMessage = new JPanel();
             JOptionPane.showMessageDialog(displayMessage, "Sorry , Unable to Start Server.  Try Again", "Error",
                     JOptionPane.ERROR_MESSAGE);
         }
     }
 
     public void display() throws IOException {
         startServerFrame.setVisible(true);
         players.addElement(server.getServerName());
         for (Socket client : server.getClients()) {
             players.addElement(client.getLocalAddress().getHostName().toString());
         }
 
         String result = server.getClientsListToString();
         server.sendMessage(result);
     }
 }
