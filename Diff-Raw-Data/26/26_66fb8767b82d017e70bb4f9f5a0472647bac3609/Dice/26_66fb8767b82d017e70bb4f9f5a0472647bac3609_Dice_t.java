 package de.htwg.monopoly.entities.impl;
 
 import de.htwg.monopoly.util.IMonopolyUtil;
 
 public final class Dice {

 	private int fieldSize;
 
 	public Dice(int fieldSize) {
 		this.fieldSize = fieldSize;
 	}
 
 	private int dice1 = 0;
 	private int dice2 = 0;
 	private int resultDice = 0;
 
 	/**
	 * This Method throws (sets) the two dices, represented as two private dice
	 * variables. It also sets the result dice according to the field size.
 	 * 
 	 * @return
 	 */
 	public void throwDice() {
 		dice1 = setDice(1, IMonopolyUtil.DICE);
 		dice2 = setDice(1, IMonopolyUtil.DICE);
 		resultDice = (dice1 + dice2) % this.fieldSize + 1;
 	}
 
 	/**
 	 * Set the dice with a random number between lowerbound (standard 1) and
 	 * upperbound (standard 6) (inclusive)
 	 * 
 	 * @param lowerBound
 	 * @param upperBound
 	 */
 	private int setDice(int lowerBound, int upperBound) {
 		return (int) (Math.random() * upperBound + lowerBound);
 	}
 
 	public int getDice1() {
 		return dice1;
 	}
 
 	public int getDice2() {
 		return dice2;
 	}
 
 	public int getResultDice() {
 		return resultDice;
 	}
 }
