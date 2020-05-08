 package com.eddygao;
 
 import android.graphics.Canvas;
 import android.graphics.drawable.Drawable;
 
 public class Card {
 	private int value;
 	private Drawable image;
 	private Deck parentDeck;
 	private int x, y;
 	private boolean revealed;
 
 	public enum Suit {
 		hearts, diamonds, clubs, spades;
 	}
 
 	private Suit suit;
 
 	public Card(int value, Suit suit, Deck parent, boolean revealed) {
 		this.value = value;
 		this.suit = suit;
 
 		if (parentDeck != null) {
 			this.parentDeck = parent;
 			this.x = parentDeck.getX();
 			this.y = parentDeck.getY();
 		}
 		this.revealed = revealed;
 	}
 	
 	public void setReveal(boolean revealed) {
 		this.revealed = revealed;
 	}
 
 	public void setImage(Drawable image) {
 		this.image = image;
 	}
 
 	public boolean inCard(float x, float y) {
 		return (x > this.x && x < (this.x + parentDeck.getWidth())
 				&& y > this.y && y < (this.y + parentDeck.getHeight()));
 	}
	
	public String toString() {
		return "Value: "+this.value+", suit: "+this.suit;
	}
 
 	public boolean isRevealed() {
 		return revealed;
 	}
 	
 	public int getValue() {
 		return value;
 	}
 
 	public Suit getSuit() {
 		return suit;
 	}
 
 	public void setValue(int value) {
 		this.value = value;
 	}
 
 	public void setSuit(Suit suit) {
 		this.suit = suit;
 	}
 
 	public void moveByDelta(int x, int y) {
 		this.x += x;
 		this.y += y;
 	}
 
 	public void moveTo(int x, int y) {
 		this.x = x;
 		this.y = y;
 	}
 
 	public Deck getParent() {
 		return this.parentDeck;
 	}
 
 	public void setParent(Deck parent) {
 		this.parentDeck = parent;
 	}
 
 	public void draw(Canvas canvas) {
 		if (parentDeck != null && image != null) {
 		int width = parentDeck.getWidth();
 			int height = parentDeck.getHeight();
 			image.setBounds(x, y, x + width, y + height);
 			image.draw(canvas);
 		}
 	}
 }
