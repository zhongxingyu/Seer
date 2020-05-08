 package com.stevengharms.javacard;
 
 import javax.swing.*;
 import java.awt.event.ActionListener;
 import java.io.*;
 import java.awt.*;
 import java.awt.event.*;
 
 public class JavaCardApp{
 	private JavaCardDeck sourceDeck;
 	private JavaCardView view;
     private JFrame rootFrame;
 
 	private int deckIndex = 0;
     private Card currentCard = null;
     private int currentIndex = 0;
 
 		
 	// Create an instance of the game
 	public JavaCardApp(){
 		// Model Objects
 		sourceDeck    = new JavaCardDeck();
 		JavaCardDeck correctDeck= new JavaCardDeck();
 		JavaCardDeck incorrectDeck= new JavaCardDeck();
 
 	}
 	
 	public static void main (String[] args)
 	{
 		JavaCardApp app = new JavaCardApp();
         app.initialGUI();
 	}
 
     private int initialGUI() {
         final JFrame rootFrame = new JFrame();
         rootFrame.setSize(500,100);
 
         JPanel basicPanel = new JPanel();
         JLabel description = new JLabel("Welcome to JavaCard");
 
         basicPanel.add(description);
         rootFrame.getContentPane().add(basicPanel, BorderLayout.NORTH);
 
         JPanel  buttonPanel = new JPanel();
         JButton editButton = new JButton("Edit a flash card deck");
         JButton reviewButton = new JButton("Review a flash card deck");
 
 
         
         editButton.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent ev){
                 view.setVisible(true);
                 rootFrame.setVisible(false);
             }
         });
         buttonPanel.add(editButton);
         buttonPanel.add(reviewButton);
         rootFrame.getContentPane().add(buttonPanel,BorderLayout.SOUTH);
 
         rootFrame.setVisible(true);
 
         // Secondary view Object
 		view          = new JavaCardView(this, rootFrame);
         
         return 1;
     }
 
     public void addNewCard(String[] qa) throws IllegalArgumentException{
 		boolean result = false;
 		JavaCard jc = null;
 		
 		try{
 			jc = new JavaCard(qa);
 			result = sourceDeck.addCard(jc);			
 		}
 		catch (Exception e){
 			System.err.println("Exception Handled:"+e);
 			view.giveFocusToEmptyTextArea();
 		}
 		
 		if (!result){
 			throw new IllegalArgumentException();
 		}
 		
 		this.setCurrentCard(jc);
 		this.currentIndex = sourceDeck.indexOf(jc);
 		System.out.println("Succesfully added a card!");
 	}
 	
 	public Card getCurrentCard(){
         return (currentCard == null) ? new Card() : currentCard;
 	}
 	
 	public  void nullifyCurrentCard(){
 		currentCard = null;
 	}
 	
 	public void deleteCurrentCard() throws NullPointerException{
		int reset_index = sourceDeck.indexOf(currentCard);
 		boolean result = sourceDeck.removeCard(currentCard);
 		if (! result){
 			throw new NullPointerException();
 		}
		currentCard = sourceDeck.get(reset_index);
 	}
 	
 	public void setCurrentCard(Card c){
 		currentCard = c;
 	}
 	
 	public void goToPreviousCard(){
 		currentCard = sourceDeck.cardPriorTo(currentCard);
 	}
 	public void goToSubsequentCard(){
 		currentCard = sourceDeck.cardAfter(currentCard);
 	}
 	
 	public boolean nextCardExists(){
 		int currIndex = sourceDeck.indexOf(currentCard);
		boolean yesno = sourceDeck.exists(currIndex+1) ? true : false;
 		System.out.println("I say " + yesno);
 		return yesno;
 	}
 	
 	public boolean priorCardExists(){
 		int currIndex = sourceDeck.indexOf(currentCard);
		return (currIndex == 0) ? false : true;
 	}
 	
 	public Deck getDeck(){
 		return sourceDeck;
 	}
 		
 	public void razzle(){
 		System.out.println("razzle!");
 	}
 	
 	public void save(String aFile){
 		System.out.printf("Controller about to save [%s] records to [%s]\n", this.getDeck().length(), aFile);
 
 		try{ 
 			FileOutputStream fileStream = new FileOutputStream(aFile);
 			ObjectOutputStream os = new ObjectOutputStream(fileStream);
 			os.writeObject(this.getDeck());
 			os.close();
 		}catch (Exception e){
 			System.out.println("We were unable to save the file: " + aFile);
 		}
 	}
 	
 	public void open(String aFile){
 		System.out.println("Controller has "+ aFile);
 		
 		try{
 			FileInputStream instream = new FileInputStream(aFile);
 			ObjectInputStream is = new ObjectInputStream(instream);
 			sourceDeck = (JavaCardDeck) is.readObject();
 			System.out.println(sourceDeck);
 			currentIndex = 0;
			currentCard = sourceDeck.get(0);
			view.update();
 		}catch (Exception e){
 			System.out.println("Was not able to find:  " + e);
 		}
 	}
 	
 }
