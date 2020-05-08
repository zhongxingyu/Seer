 /******************************************************
 Cours:  LOG121
 Projet: Squelette du laboratoire #1
 Nom du fichier: FenetreFormes.java
 Date créé: 2013-05-03
  *******************************************************
 Historique des modifications
  *******************************************************
  *@author Patrice Boucher
 2013-05-03 Version initiale
  *******************************************************/
 
 import javax.swing.*;
 
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.util.Comparator;
 
 /**
  * Cette fenêtre gère l'affichage des formes
  * 
  * @author Patrice Boucher
  */
 public class FenetreFormes extends JComponent {
 
 	private static final long serialVersionUID = -2262235643903749505L;
 	public static final int WIDTH = 500;
 	public static final int HEIGHT = 500;
 	public static final Dimension dimension = new Dimension(500, 500);
 
 	private Ensemble<Forme> formes = new ListeChaine<Forme>();
 	
 	private Ensemble<Forme> formesOriginales = new ListeChaine<Forme>();
 
 	private Comparator<Forme> comparator;
 	
 	/**
 	 * Constructeur
 	 */
 	public FenetreFormes() {
 	}
 	
 	/**
 	 * Ajoute la forme et créé une copie originale de celle ci dans une autre liste
 	 * @param f
 	 */
 	public void ajouterForme(Forme f) {
 		formes.ajouterFin(f);	
 		formesOriginales.ajouterFin(f.clone());
 		reorganiser();
 	}
 
 	/**
 	 * Efface toutes les formes de la liste de forme qui est dessiné à l'écran
 	 */
 	public void effacerTout() {
 		formes.vider();
 		formesOriginales.vider();
 		reorganiser();
 	}
 	
 	/**
 	 * Copie toutes les formes original dans la liste de forme dessiné à l'écran
 	 */
 	private void copierFormes()
 	{
 		formes.vider();
 		for(Forme f : formesOriginales)
 			formes.ajouterFin(f.clone());
 	}
 	
 	/**
 	 * Réorganise les formes selon l'algorithme de tri selectionné
 	 */
 	public void reorganiser()
 	{
 		if(comparator != null) 
 		{
 			formes = Ensembles.sort(formes, comparator);
 			int i = 0;
 			for(Forme f : formes)
 			{
 				f.setX(i * 40);
 				f.setY(i * 40);
 				i++;
 			}
 		}
 	
 		repaint();
 	}
 
 	/*
 	 * Affiche la liste de formes
 	 */
 	@Override
 	public void paintComponent(Graphics g) {
 		for (Forme f : formes)
 			f.dessiner(g);
 	}
 
 	/*
 	 * Le Layout qui utilise (contient) FenetreFormes doit réserver l'espace
 	 * nécessaire à son affichage
 	 */
 	@Override
 	public Dimension getPreferredSize() {
 		return dimension;
 	}
 
 	/**
 	 * Retourne le comparateur utilisé pour effectuer le tri
 	 * @return comparateur de forme
 	 */
 	public Comparator<Forme> getComparator() {
 		return comparator;
 	}
 
 	/**
 	 * Assigne le comparateur de forme et effectue et restaure une copie si le comparateur assigné est null
 	 * @param comparator
 	 */
 	public void setComparator(Comparator<Forme> comparator) {
 		this.comparator = comparator;
 		if(comparator == null) copierFormes();
		reorganiser();
 	}
 
 }
