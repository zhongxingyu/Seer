 package com.cards;
 
 public abstract class Card<S extends Suit<S>, R extends Rank<R>> {
	protected Suit<S> suit;
	protected Rank<R> rank;
 	protected Card() {
 		this(null, null);
 	}
	protected Card(Suit<S> s, Rank<R> r) {
 		if (r == null) {
 			throw new IllegalArgumentException("Rank cannot be null");
 		}
 		suit = s;
 		rank = r;
 	}
	public Suit<S> getSuit() { return suit; }
	public Rank<R> getRank() { return rank; }
 }
