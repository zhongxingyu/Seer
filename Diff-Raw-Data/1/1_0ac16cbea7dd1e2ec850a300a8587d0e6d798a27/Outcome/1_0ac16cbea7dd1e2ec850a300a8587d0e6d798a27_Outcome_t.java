 package game;
 
 /**
  * 
  * @author wdencker
  * 
  */
 
 // Outcome modifies the person after receiving a choice.
 public class Outcome {
 	private boolean alive;
 	private int charisma;
 	private int intelligence;
 	private int strength;
 	private int wealth;
 	private int confidence;
 	private int age;
 
 	/**
 	 * @param alive
 	 *            is a boolean, indicating whether the decision results in the
 	 *            person being alive or dead
 	 * @param charisma
 	 *            is the number (+ or -) which indicates how the charisma of the
 	 *            person is changed by the decision.
 	 * @param intelligence
 	 *            is the number (+ or -) which indicates how the intelligence of
 	 *            the person is changed by the decision.
 	 * @param strength
 	 *            is the number (+ or -) which indicates how the strength of the
 	 *            person is changed by the decision.
 	 * @param wealth
 	 *            is the number (+ or -) which indicates how the wealth of the
 	 *            person is changed by the decision.
 	 * @param confidence
 	 *            is the number (+ or -) which indicates how the wealth of the
 	 *            person is changed by the decision.
 	 * @param age
 	 *            is the number representing the age of the person after the
 	 *            decision.
 	 */
 
 	public Outcome(boolean alive, int charisma, int intelligence, int strength, int wealth, int confidence, int age) {
 		this.alive = alive;
 		this.charisma = charisma;
 		this.intelligence = intelligence;
 		this.strength = strength;
 		this.wealth = wealth;
 		this.confidence = confidence;
 		this.age = age;
 	}
 
 	/**
 	 * @param p
 	 *            is the Person object playing the game that is to be modified.
 	 */
 	public void updateAttributes(Person p) {
 		p.setAlive(alive);
 		int newStat = p.getCharisma() + charisma;
 		if (newStat <= 0) {
 			p.setCharisma(0);
 		} else {
 			p.setCharisma(newStat);
 		}
 		newStat = p.getIntelligence() + intelligence;
 		if (newStat <= 0) {
 			p.setIntelligence(0);
 		} else {
 			p.setIntelligence(newStat);
 		}
 		newStat = p.getStrength() + strength;
 		if (newStat <= 0) {
 			p.setStrength(0);
 		} else {
 			p.setStrength(newStat);
 		}
 		newStat = p.getWealth() + wealth;
 		if (newStat <= 0) {
 			p.setWealth(0);
 		} else {
 			p.setWealth(newStat);
 		}
 		newStat = p.getConfidence() + confidence;
 		if (newStat <= 0) {
 			p.setConfidence(0);
 		} else {
 			p.setConfidence(newStat);
 		}
 		p.setAge(age);
 	}
 
 }
