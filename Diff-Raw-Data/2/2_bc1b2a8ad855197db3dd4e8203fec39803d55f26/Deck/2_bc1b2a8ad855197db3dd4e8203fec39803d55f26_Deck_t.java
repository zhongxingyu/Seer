 package com.stevengharms.javacard;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import javax.swing.text.DefaultEditorKit.*;
 import java.io.*;
 
 public class Deck implements Serializable
 {
	protected ArrayList<Card> cards;
 	
 	public Deck()
 	{
 		cards = new ArrayList<Card>();
 	}
 	
 	// Open and close a deck
 	void initializeDeck()
 	{
 	}
 	
 	void closeDeck()
 	{
 		// System.out.println("Closing " + this.toString() );		
 	}
 	
 	// State maintenance
 	void shuffle()
 	{
 		Collections.shuffle(cards);
 	}
 	
 	public boolean exists(int i){
 		try{
 		  cards.get(i);
 		}catch (IndexOutOfBoundsException e){
 			return false;
 		}
 		return true;
 	}
 	
 	public int indexOf(Object o){
 		return cards.indexOf(o);
 	}
 	
 	boolean addCard(Card c) throws UnsupportedOperationException,ClassCastException,
 								   NullPointerException,IllegalArgumentException,IllegalStateException{
 		boolean result = false;
 		
 		try
 		{
 			result =  cards.add(c);
 		}
 		catch (Exception e)
 		{
 			System.out.println("Was not able to add the card "+ c + " because "+ e);
 			
 		}
 		return result ? true : false;
 	}
 	
 	public boolean removeCard(Card c){
 		return cards.remove(c);
 	}
 	
 	Card get(int i){
 		return (Card)cards.get(i);
 	}
 	
 	public int size(){
 		return cards.size();
 	}
 	
 	public Card cardPriorTo(Card c){
 		int post = cards.indexOf(c);
 		if (post == 0){
 			System.out.println("Beep!  At first");
 			return c;
 		}
 		return cards.get(post-1);
 	}
 
 	public Card cardAfter(Card c){
 		int post = cards.indexOf(c);
 		return cards.get(post+1);
 	}
 
 	
 	public Card[] getCards(){
 		/*
 		This is pretty funky syntax, if you ask me
 		http://download-llnw.oracle.com/javase/tutorial/collections/interfaces/collection.html
 		
 		For example, suppose that c is a Collection. The following snippet
         dumps the contents of c into a newly allocated array of Object whose
         length is identical to the number of elements in c.
 		
 		 Object[] a = c.toArray(); Suppose that c is known to contain only
         strings (perhaps because c is of type Collection<String>). The
         following snippet dumps the contents of c into a newly allocated array
         of String whose length is identical to the number of elements in c.
         String[] a = c.toArray(new String[0]);
     
 		*/
 		return cards.toArray(new Card[0]);
 		
 	}
 	
 }
