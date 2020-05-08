 package com.objet.lofteurs;
 
 import java.util.ArrayList;
 
 import com.lofteur.nourriture.Nourriture;
 import com.objet.lofteurs.neuneus.Neuneu;
 
 /**
  * Classe repr�sentant une case du loft.
  * 
  * @author Denis
  */
 public class Case {
 
 	private final int x;
 	private final int y;
 	private ArrayList<Nourriture> listeNourriture;
 	private ArrayList<Neuneu> listeNeuneus;
 
 	/**
 	 * Instancie une nouvelle instance de la classe Case.
 	 * 
 	 * @param x
 	 * @param y
 	 */
 	public Case(int x, int y) {
 		this.x = x;
 		this.y = y;
		listeNeuneus = new ArrayList<Neuneu>();
		listeNourriture = new ArrayList<Nourriture>();
 	}
 
 	/**
 	 * Obtient la valeur de x.
 	 * 
 	 * @return x
 	 */
 	public int getX() {
 		return x;
 	}
 
 	/**
 	 * Obtient la valeur de y.
 	 * 
 	 * @return the y
 	 */
 	public int getY() {
 		return y;
 	}
 
 	/**
 	 * Obtient une liste de nourriture presentes sur la case.
 	 * 
 	 * @return Map<Integer, Nourriture>
 	 */
 	public ArrayList<Nourriture> getNourriture() {
 		return this.listeNourriture;
 	}
 
 	/**
 	 * Obtient une liste de neuneus prsents sur la case.
 	 * 
 	 * @return Map<Integer, Neuneu>
 	 */
 	public ArrayList<Neuneu> getNeuneu() {
 		return this.listeNeuneus;
 	}
 
 	/**
 	 * Ajoute un neuneu a la case
 	 * 
 	 * @param neuneu
 	 */
 	public void addNeuneu(Neuneu neuneu) {
 		listeNeuneus.add(neuneu);
 	}
 
 	/**
 	 * Ajoute un element de nourriture a la case.
 	 * 
 	 * @param nourriture
 	 */
 	public void addNourriture(Nourriture nourriture) {
 		listeNourriture.add(nourriture);
 	}
 
 	/**
 	 * Enleve un neuneu d'une case.
 	 * 
 	 * @param neuneu
 	 */
 	public void removeNeuneu(Neuneu neuneu) {
 
 		listeNeuneus.remove(neuneu);
 	}
 }
