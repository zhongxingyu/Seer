 package com.vu.se.hm.gui;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.image.*;
 import javax.imageio.*;
 import java.io.*;
 import javax.swing.*;
 public class HangmanWelcomeScreen extends JPanel {
 
 JPanel panel;
 JLabel welcome, credits;
 JButton onePlayerButton, twoPlayerButton;
 String currentWord, hostIP;
 String [] IPs;
 JTextField [] tfields;
 Box titleBox,imageBox,creditBox,buttonBox;
 int numPlayers;
 JFrame popUpFrame;
 WordValidator validator;
 	public HangmanWelcomeScreen(){
 		
 		validator = new WordValidator();
 		
 		ButtonListener action = new ButtonListener();
 		
 		
 		welcome = new JLabel("Welcome to Hangman!");
 		welcome.setFont(new Font("Courier New", Font.CENTER_BASELINE, 20));
 		onePlayerButton = new JButton("Create a game");
 		twoPlayerButton = new JButton("Join a game");
 		
 		onePlayerButton.addActionListener(action);
 		twoPlayerButton.addActionListener(action);
 		
 		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 		titleBox = new Box(BoxLayout.X_AXIS);
 		imageBox = new Box(BoxLayout.X_AXIS);
 		creditBox = new Box(BoxLayout.X_AXIS);
 		buttonBox = new Box(BoxLayout.X_AXIS);
 		
 		
 		titleBox.add(welcome);
 		
 		buttonBox.add(onePlayerButton);
 		buttonBox.add(Box.createRigidArea(new Dimension(100,0)));
 		buttonBox.add(twoPlayerButton);
 
 		try{
 			BufferedImage myPicture = ImageIO.read(new File("C:\\hangman\\teenage-fun.jpg"));
 			JLabel picLabel = new JLabel(new ImageIcon( myPicture ));
 			imageBox.add(picLabel);
 			
 			myPicture = ImageIO.read(new File("C:\\hangman\\hangman.png"));
 			picLabel = new JLabel(new ImageIcon( myPicture ));
 			imageBox.add(picLabel);
 			
 		}
 		catch (IOException e){
 			JOptionPane.showMessageDialog(null, "Image file not found");
 		}
 		
 		credits = new JLabel("Created by Team Teenage Mutant Ninja Turtles: Dan, Mike, Pal, and Mounika");
 		creditBox.add(credits);
 		this.add(titleBox);
 		this.add(Box.createRigidArea(new Dimension(0,25)));
 		this.add(imageBox);
 		this.add(Box.createRigidArea(new Dimension(0,15)));
 		this.add(creditBox);
 		this.add(Box.createRigidArea(new Dimension(0,50)));
 		this.add(buttonBox);
 	}
 	public void createGame1(){
 		final ButtonGroup buttonGroup = new ButtonGroup();
 		JPanel oPane = new JPanel();
 	
 		JRadioButton twoButton = new JRadioButton("2-Player", true);
 		JRadioButton threeButton = new JRadioButton("3-Player", false);
 		JRadioButton fourButton = new JRadioButton("4-Player", false);
 	
 		twoButton.setActionCommand("2p");
 		threeButton.setActionCommand("3p");
 		fourButton.setActionCommand("4p");
 	
 		buttonGroup.add(twoButton);
 		buttonGroup.add(threeButton);
 		buttonGroup.add(fourButton);
 		JButton nextButton = new JButton("Next");
 		nextButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				String command = buttonGroup.getSelection().getActionCommand();
 				if (command == "2p"){numPlayers = 2;}
 				if (command == "3p"){numPlayers = 3;}
 				if (command == "4p"){numPlayers = 4;}
 				createGame2();
 			}
 			});
 		oPane.setLayout(new BoxLayout(oPane,BoxLayout.Y_AXIS));
 		oPane.add(twoButton);
 		oPane.add(threeButton);
 		oPane.add(fourButton);
 		oPane.add(nextButton);
 		popUpFrame = new JFrame();
 		popUpFrame.setSize(250,250);
 		popUpFrame.setTitle("Create A Game");
 		popUpFrame.setLocationRelativeTo(this);
 		popUpFrame.add(oPane);
 		popUpFrame.setVisible(true);
 	
 	
 	}
 	/*
 	public void createGame2(){
 		popUpFrame.setVisible(false);
 		JTextField thisField;
 		Box tempBox;
 		JLabel label1 = new JLabel("Enter the IPs of the other players");
 		JLabel label2 = new JLabel("  (xxx.xxx.xxx.xxx format):");
 		JPanel iPane = new JPanel();
 		iPane.setLayout(new BoxLayout(iPane,BoxLayout.Y_AXIS));
 		
 		iPane.add(label1);
 		iPane.add(label2);
 		iPane.add(Box.createRigidArea(new Dimension(0,20)));
 		
 		tfields = new JTextField[numPlayers-1];
 		for(int i=0;i<numPlayers-1;i++){
 			thisField = new JTextField();
 			thisField.setMaximumSize(new Dimension(300,25));
 			tfields[i] = thisField;
 			tempBox = new Box(BoxLayout.X_AXIS);
 			tempBox.add(new JLabel("Player " + (i+2) + ":"));
 			tempBox.add(Box.createRigidArea(new Dimension(15,0)));
 			tempBox.add(tfields[i]);
 			iPane.add(tempBox);
 			iPane.add(Box.createRigidArea(new Dimension(0,20)));
 		}
 		
 		JButton nextButton = new JButton("Next");
 		nextButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				IPs = new String [numPlayers-1];
 				for(int i=0;i<numPlayers-1;i++){
 					IPs[i] = tfields[i].getText();
 				}
 				createGame3();
 			}
 			});
 		
 		iPane.add(nextButton);
 		popUpFrame = new JFrame();
 		popUpFrame.setSize(250, 250);
 		popUpFrame.add(iPane);
 		popUpFrame.setTitle("Create A Game");
 		popUpFrame.setLocationRelativeTo(this);
 		popUpFrame.setVisible(true);
 		
 	}
 	*/
 	public void createGame2(){
 		popUpFrame.setVisible(false);
 		popUpFrame = null;
 		boolean validPhrase = false;
 		while (!validPhrase){
 			currentWord = JOptionPane.showInputDialog(this, "Please enter the word or phrase to be guessed (only letters and spaces are allowed):",
 				"Word Entry", JOptionPane.PLAIN_MESSAGE);
 			currentWord = currentWord.toUpperCase();
 			validPhrase = verifyPhrase(currentWord);
 			//validPhrase = validator.isValidPhrase(currentWord);
 			if(!validPhrase){
 				JOptionPane.showMessageDialog(this,"Phrase is invalid (only letters and spaces please!)", "Invalid word",
 						JOptionPane.ERROR_MESSAGE);
 			}
 		}
 		
 		
 		/* This is when the game starts
 		 *    -Pass the phrase, number of players, and their IPs to the game manager
 		 */
                 HangmanViewController game = new HangmanViewController(currentWord, false); //Should be true since this is admin. False for test
 		
 	}
 	public void joinGame(){}
 	public class ButtonListener implements ActionListener{
 		public void actionPerformed(ActionEvent e){
 			if(e.getActionCommand().equals("Create a game")){
 				createGame1();
 			}
 			
 			if(e.getActionCommand().equals("Join a game")){
 				hostIP = JOptionPane.showInputDialog(null, "Please enter the host's IP (xxx.xxx.xxx.xxx format):", "Word Entry",
 						JOptionPane.PLAIN_MESSAGE);
 			}
 		}
 	}
 	
 	public boolean verifyPhrase(String phrase){
 		char thisChar;
 		if(phrase == null || phrase.equals("")){
 			return false;
 		}
 		currentWord.toUpperCase();
 		for(int i=0;i<currentWord.length();i++){
 			thisChar = currentWord.charAt(i);
 			if(!(Character.isLetter(thisChar) || Character.isSpaceChar(thisChar))){
 				return false;
 			}
 		}
 		return true;
 	}
 }
