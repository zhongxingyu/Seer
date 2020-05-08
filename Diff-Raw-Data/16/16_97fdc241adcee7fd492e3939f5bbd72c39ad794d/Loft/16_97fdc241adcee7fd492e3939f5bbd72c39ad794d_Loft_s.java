 package com.objet.lofteurs;
 
 import java.awt.Graphics;
 import java.util.Map;
 
 import com.objet.lofteurs.neuneus.Neuneu;
 
 /**
  * Classe correspondant au Loft
  * 
  * @author Denis
  * 
  */
 public class Loft implements ObjetDessinable {
 
 	private final int w;
 	private final int h;
 	private final ZoneGraphique zone;
 	private Case[][] listeCases;
 	private Map<Integer, Neuneu> listeNeuneu;
 
 	/**
	 * Cre un loft avec le nombre de cases ncessaires.
 	 * 
 	 * @param w
 	 *            largeur du loft
 	 * @param h
 	 *            longueur du loft
 	 * @param zone
 	 */
 	public Loft(final int w, final int h, ZoneGraphique zone) {
 		this.w = w;
 		this.h = h;
 		this.zone = zone;
		this.listeCases = new Case[h][w];
 
 		for (int i = 0; i < w; i++) {
 			for (int j = 0; j < h; j++) {
 				this.listeCases[i][j] = new Case(i, j);
 			}
 		}
 	}
 
 	/**
 	 * Lance l'�mission !
 	 */
 	public void go() {
 		// TODO
 	}
 
 	/**
 	 * Remplit le loft en nourriture
 	 * 
 	 * @param nombre
 	 */
 	public void remplissageAleatoire(float nombre) {
 		// TODO
 	}
 
 	/**
	 * Ajoute un neuneu  la liste des neuneus du loft.
 	 * 
 	 * @param neuneu
 	 */
 	public void add(Neuneu neuneu) {
 		listeNeuneu.put(listeNeuneu.size(), neuneu);
 	}
 
 	@Override
 	public void dessinerObjet(Graphics g) {
 		// TODO Auto-generated method stub
 
 	}
 	
 	/**
 	 * Permet de recuperer une case du loft
 	 * 
 	 * @param coordX - coordonnee selon X de la case
 	 * @param coordY - coordonnee selon Y de la case
 	 * @return La case du loft demandee
 	 */
 	public Case getCase(int coordX, int coordY){
 		return this.listeCases[coordX][coordY];
 	}
 }
