 package checkers;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.*;
 import javax.swing.*;
 import java.io.*;
 public class GUI implements ActionListener 
 {
 	protected JFrame window;
 	public GameScreen gamescr;
     public SetupScreen setupscr;
     Container cp;
     JButton loadGameButton = new JButton("Load Game");
     Game gamer;
     String one;
     String two;
 
 GUI(int bsize, String playerOne, String playerTwo)
 {
 	window = new JFrame("Checkers");
 	cp = window.getContentPane();
 	one = playerOne;two = playerTwo;
 
     setupscr = new SetupScreen(bsize,this);
 
     loadGameButton.addActionListener(this);
    
 	window.setSize(1000,500);
 	window.setVisible(true);
 	window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	cp.add(setupscr.panel);
 
 
     
  
 
 }
 
 public void actionPerformed(ActionEvent event)  //important, associates ui actions with consequences
 {
     if(event.getSource() == loadGameButton)
     {
 
     }
 }
 
 public static void main (String[] args)
 {
 	//GUI test = new GUI(8, "asdf", "fda");
 	
         JFrame f1 = new JFrame("3-D Checkers");
         f1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         homePageScreen p1 = new homePageScreen();
         f1.getContentPane().add(p1);
         f1.setSize(675,400);
         f1.setVisible(true);
         
 }
 
 
 }
