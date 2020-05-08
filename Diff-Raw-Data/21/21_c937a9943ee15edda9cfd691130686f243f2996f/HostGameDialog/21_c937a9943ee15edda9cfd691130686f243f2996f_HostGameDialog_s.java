 package com.steamedpears.comp3004.views;
 import com.steamedpears.comp3004.SevenWonders;
 
 import net.miginfocom.swing.MigLayout;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 
 public class HostGameDialog extends JDialog {
     private final SevenWonders controller;
     private static final int WIDTH = 480;
     private static final int HEIGHT = 120;
 
     private JLabel playersJoinedLabel;
 
     public HostGameDialog(Frame f, final SevenWonders controller) {
         super(f, "Host Game", true);
 
         // initialize
         this.controller = controller;
         setLayout(new MigLayout("wrap 4"));
         setBounds((f.getWidth() / 2) - (WIDTH / 2), (f.getHeight() / 2) - (HEIGHT / 2), WIDTH, HEIGHT);
         addWindowListener(new DialogClosingListener());
         setResizable(false);
 
         // Players joined label
         add(new JLabel("Players joined:"));
         // TODO: make this label respond to player joins
        playersJoinedLabel = new JLabel("1");
         add(playersJoinedLabel);
 
         // Start game button
         JButton startGameButton = new JButton("Start Game");
         startGameButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 controller.startGame();
             }
         });
         add(startGameButton);
     }
 
     private class DialogClosingListener extends WindowAdapter {
         @Override
         public void windowClosing(WindowEvent windowEvent) {
             controller.openNewGameDialog();
         }
     }
 }
