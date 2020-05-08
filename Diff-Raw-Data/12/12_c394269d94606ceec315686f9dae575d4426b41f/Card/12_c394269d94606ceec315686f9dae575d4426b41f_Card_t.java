 package com.cards;
 
 public abstract class Card<S extends Suit<S>, R extends Rank<R>> {
	protected S suit;
	protected R rank;
 	protected Card() {
 		this(null, null);
 	}
	protected Card(S s, R r) {
 		if (r == null) {
 			throw new IllegalArgumentException("Rank cannot be null");
 		}
 		suit = s;
 		rank = r;
 	}
	public S getSuit() { return suit; }
	public R getRank() { return rank; }
 }
