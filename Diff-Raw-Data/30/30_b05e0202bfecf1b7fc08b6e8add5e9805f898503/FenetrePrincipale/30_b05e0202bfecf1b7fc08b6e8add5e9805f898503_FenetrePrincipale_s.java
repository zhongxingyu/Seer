 package vue;
 
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.LinkedList;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.Box;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JInternalFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JWindow;
 
 import carte.attaque.*;
 import carte.bottes.*;
 import carte.etape.*;
 
 import tasDeCartes.Carte;
 import tasDeCartes.Defausse;
 
 import jeu.Menu;
 import jeu.PartieDeJeu;
 import joueurs.*;
 import jeu.*;
 /**
  * Correspond a la fenetre de jeu : interface graphique
  * @author Damien
  *
  */
 public class FenetrePrincipale extends JFrame implements Observer{
 	
 	
 	private int nbJoueurs;
 	private String nom;
 	private Carte carteSelectionnee;
 	private static FenetrePrincipale fenetre = null;
 	private Controleur controleur = Controleur.getInstance();
 	
 	
 	/**
 	 * Variables pour le premier joueur
 	 */
 	private JButton etapeJ1=new JButton("Etape");
 	private JButton attaqueJ1=new JButton("Pile Bataille");
 	private JButton limiteJ1=new JButton("Limite de vitesse");
 	private JButton botteJ1=new JButton("Bottes");
 	private JLabel kmJ1=new JLabel("Bornes : 0");
 	private JLabel bottePossJ1=new JLabel("Bottes : vide");
 	private JLabel isAttaqueJ1=new JLabel("Pile Bataille : vide");
 	private JLabel isLimiteJ1=new JLabel("Pile Limite : vide");
 	private JLabel feuVertInitJ1=new JLabel("Feu vert initial : vide");
 	
 	
 	
 	/**
 	 *  Variables pour le deuxieme joueur
 	 */
 	private JButton etapeJ2=new JButton("Etape");
 	private JButton attaqueJ2=new JButton("Pile Bataille");
 	private JButton limiteJ2=new JButton("Limite de vitesse");
 	private JButton botteJ2=new JButton("Bottes");
 	private JLabel kmJ2=new JLabel("Bornes : 0");
 	private JLabel bottePossJ2=new JLabel("Bottes : vide");
 	private JLabel isAttaqueJ2=new JLabel("Pile Bataille : vide");
 	private JLabel isLimiteJ2=new JLabel("Pile Limite : vide");
 	private JLabel feuVertInitJ2=new JLabel("Feu vert initial :vide");
 	
 	/**
 	 *  Variables pour le troisieme joueur
 	 */
 	private JButton etapeJ3=new JButton("Etape");
 	private JButton attaqueJ3=new JButton("Pile Bataille");
 	private JButton limiteJ3=new JButton("Limite de vitesse");
 	private JButton botteJ3=new JButton("Bottes");
 	private JLabel kmJ3=new JLabel("Bornes : 0");
 	private JLabel bottePossJ3=new JLabel("Bottes : vide");
 	private JLabel isAttaqueJ3=new JLabel("Pile Bataille : vide");
 	private JLabel isLimiteJ3=new JLabel("Pile Limite : vide");
 	private JLabel feuVertInitJ3=new JLabel("Feu vert initial :vide");
 	
 	/**
 	 * Variables pour le 4e joueur
 	 */
 	private JButton etapeJ4=new JButton("Etape");
 	private JButton attaqueJ4=new JButton("Pile Bataille");
 	private JButton limiteJ4=new JButton("Limite de vitesse");
 	private JButton botteJ4=new JButton("Bottes");
 	private JLabel kmJ4=new JLabel("Bornes : 0");
 	private JLabel bottePossJ4=new JLabel("Bottes : vide");
 	private JLabel isAttaqueJ4=new JLabel("Pile Bataille : vide");
 	private JLabel isLimiteJ4=new JLabel("Pile Limite : vide");
 	private JLabel feuVertInitJ4=new JLabel("Feu vert initial :vide");
 	
 	/**
 	 * variables pour les cartes en main
 	 */
 	private JInternalFrame carte1 = new JInternalFrame(); 
 	private JButton carte1Joueur=new JButton("Selectionner");
 	private JInternalFrame carte2 = new JInternalFrame(); 
 	private JButton carte2Joueur=new JButton("Selectionner");
 	private JInternalFrame carte3 = new JInternalFrame(); 
 	private JButton carte3Joueur=new JButton("Selectionner");
 	private JInternalFrame carte4 = new JInternalFrame(); 
 	private JButton carte4Joueur=new JButton("Selectionner");
 	private JInternalFrame carte5 = new JInternalFrame(); 
 	private JButton carte5Joueur=new JButton("Selectionner");
 	
 	/**
 	 * Variables talon/defausse/evenements
 	 */
 	private JButton talonBouton=new JButton("Piocher");
 	private JButton defausseBouton=new JButton("Piocher/Defausser");
 	private JLabel eventLabel = new JLabel("");
 	
 	private JInternalFrame talon = new JInternalFrame(); 
 	private JInternalFrame defausse = new JInternalFrame(); 
     
 	/**
 	 * On crée un conteneur avec gestion horizontale pour les joueurs
 	 */
 	Box b1 = Box.createHorizontalBox();
 	
 	
 	public static FenetrePrincipale getInstance() {
 		if(fenetre == null) {
 			fenetre = new FenetrePrincipale();
 		}
 		return fenetre;
 	}
 	
   private FenetrePrincipale(){
 	  this.carteSelectionnee = null;
 	  this.setTitle("1000 Bornes EXPRESS");
 	  this.setSize(1366, 768);
 	  this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	  this.setLocationRelativeTo(null);
 
 	  nbJoueurs=Menu.getInstance().getNbJoueurTotal();
     
    
     if (nbJoueurs==2){
 
     /**
      * On crée une fenetre pour le premier joueur (idem pour chaque joueur)
      */
     
     JInternalFrame joueur1 = new JInternalFrame(); 
     joueur1.setSize(30, 20);
     
     nom=controleur.getJoueurs().get(0).getNom();
     
 	joueur1.setTitle(nom);
 	/**
 	 * On la rend visible
 	 */
 	joueur1.setVisible(true);
 	/**
 	 * Quadrillage pour les tas du joueur
 	 */
 	joueur1.setLayout(new GridLayout(5, 2));
 	
 		
 	/**
 	 * On implémente l'action d'appuyer sur CE bouton (on doit le faire pour chaque bouton)
 	 */
 	etapeJ1.addActionListener(new ActionListener(){
 		public synchronized void actionPerformed(ActionEvent event){
 			Joueur joueurActuel = controleur.getJoueurActuel();
 			Joueur joueurChoisi = controleur.getJoueurs().get(0);
 
 			if (carteSelectionnee != null){
 				
 				if (controleur.isPosableSurEtape(joueurActuel,controleur.getJoueurs().get(0), carteSelectionnee)){
 					controleur.jouer(joueurActuel,controleur.getJoueurs().get(0), carteSelectionnee);
 					eventLabel.setText("Vous avez jouer sur la pile etape de "+joueurChoisi);
 					synchronized (joueurActuel) {
 						joueurActuel.notify(); 
 					}
 				} else {
 					eventLabel.setText("Vous ne pouvez pas jouer cette carte ici, re-selectionnez une carte");}
 			} else {
 				eventLabel.setText("Il faut selectionner une carte");
 			
 			}
 			carteSelectionnee=null;
 		}
 	});
 	
 	
 	joueur1.add (etapeJ1);
 	
 	
 	
 	attaqueJ1.addActionListener(new ActionListener(){
 		public synchronized void actionPerformed(ActionEvent event){
 			Joueur joueurChoisi = controleur.getJoueurs().get(0);
 			Joueur joueurActuel = controleur.getJoueurActuel();
 		
 			if (carteSelectionnee!=null){
 				
 				if (controleur.isPosableSurAttaque(joueurActuel,controleur.getJoueurs().get(0), carteSelectionnee)==true){
 				
 					controleur.jouer(joueurActuel,controleur.getJoueurs().get(0), carteSelectionnee);
 					feuVertInitJ1.setText("Feu vert initial: Feu Vert");
 					eventLabel.setText("Vous avez jouer sur la pile attaque de "+joueurChoisi);
 
 					Botte botte = controleur.canCoupFourre(joueurActuel, joueurChoisi); 
 					if(botte != null) {
 						if(joueurChoisi instanceof Robot) {
 							joueurChoisi.coupFourre(botte);
 						}
 					}
 					synchronized (joueurActuel) {
 						joueurActuel.notify(); 
 					}
 				} else {
 					eventLabel.setText("Vous ne pouvez pas jouer cette carte ici, re-selectionnez une carte");	}
 			}
 			else {
 				eventLabel.setText("Il faut selectionner une carte");
 			
 			}
 			carteSelectionnee=null;
 			
 			
 			
 		}
 	});
 	joueur1.add(attaqueJ1);
 	
 	
 	
 	
 	limiteJ1.addActionListener(new ActionListener(){
 		public synchronized void actionPerformed(ActionEvent event){
 			Joueur joueurActuel = controleur.getJoueurActuel();
 
 			Joueur joueurChoisi = controleur.getJoueurs().get(0);
 			if (carteSelectionnee!=null){
 				if (controleur.isPosableSurLimiteVitesse(joueurActuel,joueurChoisi, carteSelectionnee)==true){
 					controleur.jouer(joueurActuel,joueurChoisi, carteSelectionnee);
 					eventLabel.setText("Vous avez jouer sur la pile limite de "+joueurChoisi);
 					Botte botte = controleur.canCoupFourre(joueurActuel, joueurChoisi); 
 					if(botte != null) {
 						joueurChoisi.coupFourre(botte);
 					}
 					synchronized (joueurActuel) {
 						joueurActuel.notify(); 
 					}
 				}
 				else{
 					eventLabel.setText("Vous ne pouvez pas jouer cette carte ici, re-selectionnez une carte");
 					}
 			}
 			else {
 				eventLabel.setText("Il faut selectionner une carte");
 			
 			}
 			carteSelectionnee=null;
 		}
 	});
 	joueur1.add(limiteJ1);
 	
 	
 	
 	botteJ1.addActionListener(new ActionListener(){
 		public  synchronized void actionPerformed(ActionEvent event){
 			Joueur joueurActuel = controleur.getJoueurActuel();
 			Joueur joueurChoisi = controleur.getJoueurs().get(0);
 
 			if (carteSelectionnee!=null){
 				if (controleur.isPosableSurBotte(joueurActuel,controleur.getJoueurs().get(0), carteSelectionnee)==true){
 					controleur.jouer(joueurActuel,controleur.getJoueurs().get(0), carteSelectionnee);
 					eventLabel.setText("Vous avez jouer sur la pile botte de "+joueurChoisi);
 					synchronized (joueurActuel) {
 						joueurActuel.notify(); 
 					}
 				}
 				else{
 					eventLabel.setText("Vous ne pouvez pas jouer cette carte ici, re-selectionnez une carte");}
 			}
 			else {
 				eventLabel.setText("Il faut selectionner une carte");
 			
 			}
 			carteSelectionnee=null;
 		}
 	});
 	joueur1.add(botteJ1);
 	
 	
 	joueur1.add(kmJ1);
 	joueur1.add(bottePossJ1);
 	joueur1.add(isAttaqueJ1);
 	joueur1.add(isLimiteJ1);
 	joueur1.add(feuVertInitJ1);
 	this.feuVertInitJ1.setHorizontalAlignment(JLabel.CENTER);
 	
 	/**
 	 * On ajoute le joueur a la box horizontale
 	 */
 	b1.add(joueur1);
 	
 	/////////
 	JInternalFrame joueur2 = new JInternalFrame(); 
 	joueur2.setSize(30, 20);
 	nom=controleur.getJoueurs().get(1).getNom();
 	joueur2.setTitle(nom);
 	joueur2.setVisible(true);
 	joueur2.setLayout(new GridLayout(5, 2));
 	
 	
 	etapeJ2.addActionListener(new ActionListener(){
 		public  synchronized void actionPerformed(ActionEvent event){
 			Joueur joueurActuel = controleur.getJoueurActuel();
 			Joueur joueurChoisi = controleur.getJoueurs().get(0);
 
 			if (carteSelectionnee!=null){
 				if (controleur.isPosableSurEtape(joueurActuel,controleur.getJoueurs().get(1), carteSelectionnee)==true){
 					controleur.jouer(joueurActuel,controleur.getJoueurs().get(1), carteSelectionnee);
 					eventLabel.setText("Vous avez jouer sur la pile etape de "+joueurChoisi);
 					synchronized (joueurActuel) {
 						joueurActuel.notify(); 
 					}
 				}
 				else{
 					eventLabel.setText("Vous ne pouvez pas jouer cette carte ici, re-selectionnez une carte");
 				}
 			}
 			else {
 				eventLabel.setText("Il faut selectionner une carte");
 			
 			}
 			carteSelectionnee=null;
 		}
 	});
 	joueur2.add (etapeJ2);
 	
 	
 	attaqueJ2.addActionListener(new ActionListener(){
 		public  synchronized void actionPerformed(ActionEvent event){
 			Joueur joueurChoisi = controleur.getJoueurs().get(1);
 			Joueur joueurActuel = controleur.getJoueurActuel();
 			if (carteSelectionnee!=null){
 				if (controleur.isPosableSurAttaque(joueurActuel,joueurChoisi, carteSelectionnee)==true){
 					
 					controleur.jouer(joueurActuel,joueurChoisi, carteSelectionnee);
 					feuVertInitJ2.setText("Feu vert initial: Feu Vert");
 					eventLabel.setText("Vous avez jouer sur la pile attaque de "+joueurChoisi);
 
 					Botte botte = controleur.canCoupFourre(joueurActuel, joueurChoisi); 
 					if(botte != null) {
 						joueurChoisi.coupFourre(botte);
 					}
 					synchronized (joueurActuel) {
 						joueurActuel.notify(); 
 					}
 				}
 				else{
 					eventLabel.setText("Vous ne pouvez pas jouer cette carte ici, re-selectionnez une carte");
 				}
 			}
 			else {
 				eventLabel.setText("Il faut selectionner une carte");
 			
 			}
 			carteSelectionnee=null;
 		}
 	});
 	joueur2.add(attaqueJ2);
 	
 	
 	limiteJ2.addActionListener(new ActionListener(){
 		public  synchronized void actionPerformed(ActionEvent event){
 			Joueur joueurChoisi = controleur.getJoueurs().get(1);
 			Joueur joueurActuel = controleur.getJoueurActuel();
 
 			if (carteSelectionnee!=null){
 				if (controleur.isPosableSurLimiteVitesse(joueurActuel,joueurChoisi, carteSelectionnee)==true){
 					controleur.jouer(joueurActuel,joueurChoisi, carteSelectionnee);
 					eventLabel.setText("Vous avez jouer sur la pile limite de "+joueurChoisi);
 
 					Botte botte = controleur.canCoupFourre(joueurActuel, joueurChoisi); 
 					if(botte != null) {
 						joueurChoisi.coupFourre(botte);
 					}
 					synchronized (joueurActuel) {
 						joueurActuel.notify(); 
 					}
 				}
 				else{
 					eventLabel.setText("Vous ne pouvez pas jouer cette carte ici, re-selectionnez une carte");
 					}
 			}
 			else {
 				eventLabel.setText("Il faut selectionner une carte");
 			
 			}
 			carteSelectionnee=null;
 		}
 	});
 	joueur2.add(limiteJ2);
 	
 	
 	botteJ2.addActionListener(new ActionListener(){
 		public  synchronized void actionPerformed(ActionEvent event){
 			Joueur joueurActuel = controleur.getJoueurActuel();
 			Joueur joueurChoisi = controleur.getJoueurs().get(0);
 
 			if (carteSelectionnee!=null){
 				if (controleur.isPosableSurBotte(joueurActuel,controleur.getJoueurs().get(1), carteSelectionnee)==true){
 					controleur.jouer(joueurActuel,controleur.getJoueurs().get(1), carteSelectionnee);
 					eventLabel.setText("Vous avez jouer sur la pile botte de "+joueurChoisi);
 
 					synchronized (joueurActuel) {
 						joueurActuel.notify(); 
 					}
 				}
 				else{
 					eventLabel.setText("Vous ne pouvez pas jouer cette carte ici, re-selectionnez une carte");
 					}
 			}
 			else {
 				eventLabel.setText("Il faut selectionner une carte");
 			
 			}
 			carteSelectionnee=null;
 		}
 	});
 	joueur2.add(botteJ2);
 	
 	
 	joueur2.add(kmJ2);
 	joueur2.add(bottePossJ2);
 	joueur2.add(isAttaqueJ2);
 	joueur2.add(isLimiteJ2);
 	joueur2.add(feuVertInitJ2);
 	this.feuVertInitJ2.setHorizontalAlignment(JLabel.CENTER);
 	
 	b1.add(joueur2);
     }
 	
     else if (nbJoueurs==3){
     	
     	
     	 
         
         JInternalFrame joueur1 = new JInternalFrame(); 
         joueur1.setSize(30, 20);
         nom=controleur.getJoueurs().get(0).getNom();
     	joueur1.setTitle(nom);
     	joueur1.setVisible(true);
     	joueur1.setLayout(new GridLayout(5, 2));
     	
     		
     	
     	etapeJ1.addActionListener(new ActionListener(){
     		public  synchronized void actionPerformed(ActionEvent event){
     			Joueur joueurActuel = controleur.getJoueurActuel();
     			Joueur joueurChoisi = controleur.getJoueurs().get(0);
     			if (carteSelectionnee!=null){
     				if (controleur.isPosableSurEtape(joueurActuel,controleur.getJoueurs().get(0), carteSelectionnee)==true){
     					controleur.jouer(joueurActuel,controleur.getJoueurs().get(0), carteSelectionnee);
     					eventLabel.setText("Vous avez jouer sur la pile etape de "+joueurChoisi);
 
     					synchronized (joueurActuel) {
     						joueurActuel.notify(); 
     					}
     				}
     				else{
     					eventLabel.setText("Vous ne pouvez pas jouer cette carte ici, re-selectionnez une carte");;
     					}
     			}
     			else {
     				eventLabel.setText("Il faut selectionner une carte");
     			
     			}
     			carteSelectionnee=null;
     		}
     	});
     	joueur1.add (etapeJ1);
     	
     	
     	
     	attaqueJ1.addActionListener(new ActionListener(){
     		public  synchronized void actionPerformed(ActionEvent event){
     			Joueur joueurActuel = controleur.getJoueurActuel();
 
     			Joueur joueurChoisi = controleur.getJoueurs().get(0);
     			if (carteSelectionnee!=null){
     				if (controleur.isPosableSurAttaque(joueurActuel,joueurChoisi, carteSelectionnee)==true){
     					controleur.jouer(joueurActuel,joueurChoisi, carteSelectionnee);
     					feuVertInitJ1.setText("Feu vert initial: Feu Vert");
     					eventLabel.setText("Vous avez jouer sur la pile attaque de "+joueurChoisi);
 
     					Botte botte = controleur.canCoupFourre(joueurActuel, joueurChoisi); 
     					if(botte != null) {
     						joueurChoisi.coupFourre(botte);
     					}
     					synchronized (joueurActuel) {
     						joueurActuel.notify(); 
     					}
     				}
     				else{
     					eventLabel.setText("Vous ne pouvez pas jouer cette carte ici, re-selectionnez une carte");
     					}
     			}
     			else {
     				eventLabel.setText("Il faut selectionner une carte");
     			
     			}
     			carteSelectionnee=null;
     		}
     	});
     	joueur1.add(attaqueJ1);
     	
     	
     	
     	
     	limiteJ1.addActionListener(new ActionListener(){
     		public  synchronized void actionPerformed(ActionEvent event){
     			Joueur joueurActuel = controleur.getJoueurActuel();
 
     			Joueur joueurChoisi = controleur.getJoueurs().get(0);
     			if (carteSelectionnee!=null){
 					if (controleur.isPosableSurLimiteVitesse(joueurActuel,joueurChoisi, carteSelectionnee)==true){
 						controleur.jouer(joueurActuel, joueurChoisi, carteSelectionnee);
 						eventLabel.setText("Vous avez jouer sur la pile limite de "+joueurChoisi);
 
 						Botte botte = controleur.canCoupFourre(joueurActuel, joueurChoisi); 
     					if(botte != null) {
     						joueurChoisi.coupFourre(botte);
     					}
 						synchronized (joueurActuel) {
 							joueurActuel.notify(); 
 						}
     				}
     				else{
     					eventLabel.setText("Vous ne pouvez pas jouer cette carte ici, re-selectionnez une carte");
     					}
     			}
     			else {
     				eventLabel.setText("Il faut selectionner une carte");
     			
     			}
     			carteSelectionnee=null;    		}
     	});
     	joueur1.add(limiteJ1);
     	
     	
     	
     	botteJ1.addActionListener(new ActionListener(){
     		public  synchronized void actionPerformed(ActionEvent event){
     			Joueur joueurActuel = controleur.getJoueurActuel();
     			Joueur joueurChoisi = controleur.getJoueurs().get(0);
     			if (carteSelectionnee!=null){
     				if (controleur.isPosableSurBotte(joueurActuel,controleur.getJoueurs().get(0), carteSelectionnee)==true){
     					controleur.jouer(joueurActuel,controleur.getJoueurs().get(0), carteSelectionnee);
     					eventLabel.setText("Vous avez jouer sur la pile botte de "+joueurChoisi);
 
     					synchronized (joueurActuel) {
     						joueurActuel.notify(); 
     					}
     				}
     				else{
     					eventLabel.setText("Vous ne pouvez pas jouer cette carte ici, re-selectionnez une carte");
     					}
     			}
     			else {
     				eventLabel.setText("Il faut selectionner une carte");
     			
     			}
     			carteSelectionnee=null;
     		}
     	});
     	joueur1.add(botteJ1);
     	
     	
     	joueur1.add(kmJ1);
     	joueur1.add(bottePossJ1);
     	joueur1.add(isAttaqueJ1);
     	joueur1.add(isLimiteJ1);
     	joueur1.add(feuVertInitJ1);
     	this.feuVertInitJ1.setHorizontalAlignment(JLabel.CENTER);
     	
     	
     	b1.add(joueur1);
     	
     	/////////
     	JInternalFrame joueur2 = new JInternalFrame(); 
     	joueur2.setSize(30, 20);
     	nom=controleur.getJoueurs().get(1).getNom();
     	joueur2.setTitle(nom);
     	joueur2.setVisible(true);
     	joueur2.setLayout(new GridLayout(5, 2));
     	
     	
     	etapeJ2.addActionListener(new ActionListener(){
     		public  synchronized void actionPerformed(ActionEvent event){
     			Joueur joueurActuel = controleur.getJoueurActuel();
     			Joueur joueurChoisi = controleur.getJoueurs().get(1);
     			if (carteSelectionnee!=null){
     				if (controleur.isPosableSurEtape(joueurActuel,controleur.getJoueurs().get(1), carteSelectionnee)==true){
     					controleur.jouer(joueurActuel,controleur.getJoueurs().get(1), carteSelectionnee);
     					eventLabel.setText("Vous avez jouer sur la pile etape de "+joueurChoisi);
 
     					synchronized (joueurActuel) {
     						joueurActuel.notify(); 
     					}
     				}
     				else{
     					eventLabel.setText("Vous ne pouvez pas jouer cette carte ici, re-selectionnez une carte");
     					}
     			}
     			else {
     				eventLabel.setText("Il faut selectionner une carte");
     			
     			}
     			carteSelectionnee=null;
     		}
     	});
     	joueur2.add (etapeJ2);
     	
     	
     	attaqueJ2.addActionListener(new ActionListener(){
     		public  synchronized void actionPerformed(ActionEvent event){
     			Joueur joueurActuel = controleur.getJoueurActuel();
 
     			Joueur joueurChoisi = controleur.getJoueurs().get(1);
     			if (carteSelectionnee!=null){
     				if (controleur.isPosableSurAttaque(joueurActuel,joueurChoisi, carteSelectionnee)==true){
     					controleur.jouer(joueurActuel,joueurChoisi, carteSelectionnee);
     					feuVertInitJ2.setText("Feu vert initial: Feu Vert");
     					eventLabel.setText("Vous avez jouer sur la pile attaque de "+joueurChoisi);
     					Botte botte = controleur.canCoupFourre(joueurActuel, joueurChoisi); 
     					if(botte != null) {
     						joueurChoisi.coupFourre(botte);
     					}
     					synchronized (joueurActuel) {
     						joueurActuel.notify(); 
     					}
     				}
     				else{
     					eventLabel.setText("Vous ne pouvez pas jouer cette carte ici, re-selectionnez une carte");
     					}
     			}
     			else {
     				eventLabel.setText("Il faut selectionner une carte");
     			
     			}
     			carteSelectionnee=null;
     		}
     	});
     	joueur2.add(attaqueJ2);
     	
     	
     	limiteJ2.addActionListener(new ActionListener(){
     		public  synchronized void actionPerformed(ActionEvent event){
     			Joueur joueurActuel = controleur.getJoueurActuel();
 
     			Joueur joueurChoisi = controleur.getJoueurs().get(1);
     			if (carteSelectionnee!=null){
     				if (controleur.isPosableSurLimiteVitesse(joueurActuel,joueurChoisi, carteSelectionnee)==true){
     					controleur.jouer(joueurActuel,joueurChoisi, carteSelectionnee);
     					eventLabel.setText("Vous avez jouer sur la pile limite de "+joueurChoisi);
     					Botte botte = controleur.canCoupFourre(joueurActuel, joueurChoisi); 
     					if(botte != null) {
     						joueurChoisi.coupFourre(botte);
     					}
     					synchronized (joueurActuel) {
     						joueurActuel.notify(); 
     					}
     				}
     				else{
     					eventLabel.setText("Vous ne pouvez pas jouer cette carte ici, re-selectionnez une carte");
     					}
     			}
     			else {
     				eventLabel.setText("Il faut selectionner une carte");
     			
     			}
     			carteSelectionnee=null;
     		}
     	});
     	joueur2.add(limiteJ2);
     	
     	
     	botteJ2.addActionListener(new ActionListener(){
     		public  synchronized void actionPerformed(ActionEvent event){
     			Joueur joueurActuel = controleur.getJoueurActuel();
     			Joueur joueurChoisi = controleur.getJoueurs().get(1);
     			if (carteSelectionnee!=null){
     				if (controleur.isPosableSurBotte(joueurActuel,controleur.getJoueurs().get(1), carteSelectionnee)){
     					controleur.jouer(joueurActuel,controleur.getJoueurs().get(1), carteSelectionnee);
     					eventLabel.setText("Vous avez jouer sur la pile botte de "+joueurChoisi);
     					synchronized (joueurActuel) {
     						joueurActuel.notify(); 
     					}
     				}
     				else{
     					eventLabel.setText("Vous ne pouvez pas jouer cette carte ici, re-selectionnez une carte");
     				}
     			}
     			else {
     				eventLabel.setText("Il faut selectionner une carte");
     			
     			}
     			carteSelectionnee=null;
     		}
     	});
     	joueur2.add(botteJ2);
     	
     	
     	joueur2.add(kmJ2);
     	joueur2.add(bottePossJ2);
     	joueur2.add(isAttaqueJ2);
     	joueur2.add(isLimiteJ2);
     	joueur2.add(feuVertInitJ2);
     	this.feuVertInitJ2.setHorizontalAlignment(JLabel.CENTER);
     	
     	b1.add(joueur2);
     
     /////////
 	JInternalFrame joueur3 = new JInternalFrame(); 
 	joueur3.setSize(30, 20);
 	nom=controleur.getJoueurs().get(2).getNom();
 	joueur3.setTitle(nom);
 	joueur3.setVisible(true);
 	joueur3.setLayout(new GridLayout(5, 2));
 	
 	
 	etapeJ3.addActionListener(new ActionListener(){
 		public  synchronized void actionPerformed(ActionEvent event){
 			Joueur joueurActuel = controleur.getJoueurActuel();
 			Joueur joueurChoisi = controleur.getJoueurs().get(2);
 			if (carteSelectionnee!=null){
 				if (controleur.isPosableSurEtape(joueurActuel,controleur.getJoueurs().get(2), carteSelectionnee)==true){
 					controleur.jouer(joueurActuel,controleur.getJoueurs().get(2), carteSelectionnee);
 					eventLabel.setText("Vous avez jouer sur la pile etape de "+joueurChoisi);
 					synchronized (joueurActuel) {
 						joueurActuel.notify(); 
 					}
 				}
 				else{
 					eventLabel.setText("Vous ne pouvez pas jouer cette carte ici, re-selectionnez une carte");
 					}
 			}
 			else {
 				eventLabel.setText("Il faut selectionner une carte");
 			
 			}
 			carteSelectionnee=null;
 		}
 	});
 	joueur3.add (etapeJ3);
 	
 	
 	attaqueJ3.addActionListener(new ActionListener(){
 		public  synchronized void actionPerformed(ActionEvent event){
 			Joueur joueurActuel = controleur.getJoueurActuel();
 
 			Joueur joueurChoisi = controleur.getJoueurs().get(2);
 			if (carteSelectionnee!=null){
 				if (controleur.isPosableSurAttaque(joueurActuel,joueurChoisi, carteSelectionnee)==true){
 					controleur.jouer(joueurActuel,joueurChoisi, carteSelectionnee);
 					feuVertInitJ3.setText("Feu vert initial: Feu Vert");
 					eventLabel.setText("Vous avez jouer sur la pile attaque de "+joueurChoisi);
 					Botte botte = controleur.canCoupFourre(joueurActuel, joueurChoisi); 
 					if(botte != null) {
 						joueurChoisi.coupFourre(botte);
 					}
 					synchronized (joueurActuel) {
 						joueurActuel.notify(); 
 					}
 					}
 				else{
 					eventLabel.setText("Vous ne pouvez pas jouer cette carte ici, re-selectionnez une carte");					}
 			}
 			else {
 				eventLabel.setText("Il faut selectionner une carte");
 			
 			}
 			carteSelectionnee=null;
 		}
 	});
 	joueur3.add(attaqueJ3);
 	
 	
 	limiteJ3.addActionListener(new ActionListener(){
 		public  synchronized void actionPerformed(ActionEvent event){
 			Joueur joueurActuel = controleur.getJoueurActuel();
 
 			Joueur joueurChoisi = controleur.getJoueurs().get(2);
 			if (carteSelectionnee!=null){
 				if (controleur.isPosableSurLimiteVitesse(joueurActuel,joueurChoisi, carteSelectionnee)==true){
 					controleur.jouer(joueurActuel,joueurChoisi, carteSelectionnee);
 					eventLabel.setText("Vous avez jouer sur la pile limite de "+joueurChoisi);
 					Botte botte = controleur.canCoupFourre(joueurActuel, joueurChoisi); 
 					if(botte != null) {
 						joueurChoisi.coupFourre(botte);
 					}
 					synchronized (joueurActuel) {
 						joueurActuel.notify(); 
 					}
 				}
 				else{
 					eventLabel.setText("Vous ne pouvez pas jouer cette carte ici, re-selectionnez une carte");					}
 			}
 			else {
 				eventLabel.setText("Il faut selectionner une carte");
 			
 			}
 			carteSelectionnee=null;
 		}
 	});
 	joueur3.add(limiteJ3);
 	
 	
 	botteJ3.addActionListener(new ActionListener(){
 		public  synchronized void actionPerformed(ActionEvent event){
 			Joueur joueurActuel = controleur.getJoueurActuel();
 			Joueur joueurChoisi = controleur.getJoueurs().get(2);
 			if (carteSelectionnee!=null){
 				if (controleur.isPosableSurBotte(joueurActuel,controleur.getJoueurs().get(2), carteSelectionnee)==true){
 					controleur.jouer(joueurActuel,controleur.getJoueurs().get(2), carteSelectionnee);
 					eventLabel.setText("Vous avez jouer sur la pile botte de "+joueurChoisi);
 					synchronized (joueurActuel) {
 						joueurActuel.notify(); 
 					}
 				}
 				else{
 					eventLabel.setText("Vous ne pouvez pas jouer cette carte ici, re-selectionnez une carte");					}
 			}
 			else {
 				eventLabel.setText("Il faut selectionner une carte");
 			
 			}
 			carteSelectionnee=null;
 		}
 	});
 	joueur3.add(botteJ3);
 	
 	
 	joueur3.add(kmJ3);
 	joueur3.add(bottePossJ3);
 	joueur3.add(isAttaqueJ3);
 	joueur3.add(isLimiteJ3);
 	joueur3.add(feuVertInitJ3);
 	this.feuVertInitJ3.setHorizontalAlignment(JLabel.CENTER);
 	
 	b1.add(joueur3);
 	
     }
     
     else if (nbJoueurs == 4){
     	
     	
        
         JInternalFrame joueur1 = new JInternalFrame(); 
         joueur1.setSize(30, 20);
         nom=controleur.getJoueurs().get(0).getNom();
     	joueur1.setTitle(nom);
     	joueur1.setVisible(true);
     	joueur1.setLayout(new GridLayout(5, 2));
     	
     		
     	
     	etapeJ1.addActionListener(new ActionListener(){
     		public  synchronized void actionPerformed(ActionEvent event){
     			Joueur joueurActuel = controleur.getJoueurActuel();
     			Joueur joueurChoisi = controleur.getJoueurs().get(0);
     			if (carteSelectionnee!=null){
     				if (controleur.isPosableSurEtape(joueurActuel,controleur.getJoueurs().get(0), carteSelectionnee)==true){
     					controleur.jouer(joueurActuel,controleur.getJoueurs().get(0), carteSelectionnee);
     					eventLabel.setText("Vous avez jouer sur la pile etape de "+joueurChoisi);
     					synchronized (joueurActuel) {
     						joueurActuel.notify(); 
     					}
     				}
     				else{
     					eventLabel.setText("Vous ne pouvez pas jouer cette carte ici, re-selectionnez une carte");    					}
     			}
     			else {
     				eventLabel.setText("Il faut selectionner une carte");
     			
     			}
     			carteSelectionnee=null;
     		}
     	});
     	joueur1.add (etapeJ1);
     	
     	
     	
     	attaqueJ1.addActionListener(new ActionListener(){
     		public  synchronized void actionPerformed(ActionEvent event){
     			Joueur joueurActuel = controleur.getJoueurActuel();
 
     			Joueur joueurChoisi = controleur.getJoueurs().get(0);
     			if (carteSelectionnee!=null){
     				if (controleur.isPosableSurAttaque(joueurActuel, joueurChoisi, carteSelectionnee)==true){
     					controleur.jouer(joueurActuel,joueurChoisi, carteSelectionnee);
     					feuVertInitJ1.setText("Feu vert initial: Feu Vert");
     					eventLabel.setText("Vous avez jouer sur la pile attaque de "+joueurChoisi);
     					Botte botte = controleur.canCoupFourre(joueurActuel, joueurChoisi); 
 
     					if(botte != null) {
     						joueurChoisi.coupFourre(botte);
     					}
     					synchronized (joueurActuel) {
     						joueurActuel.notify(); 
     					}
     					}
     				else{
     					eventLabel.setText("Vous ne pouvez pas jouer cette carte ici, re-selectionnez une carte");    					}
     			}
     			else {
     				eventLabel.setText("Il faut selectionner une carte");
     			
     			}
     			carteSelectionnee=null;
     		}
     	});
     	joueur1.add(attaqueJ1);
     	
     	
     	
     	
     	limiteJ1.addActionListener(new ActionListener(){
     		public  synchronized void actionPerformed(ActionEvent event){
     			Joueur joueurActuel = controleur.getJoueurActuel();
 
     			Joueur joueurChoisi = controleur.getJoueurs().get(0);
     			if (carteSelectionnee!=null){
     				if (controleur.isPosableSurLimiteVitesse(joueurActuel,joueurChoisi, carteSelectionnee)==true){
     					controleur.jouer(joueurActuel,joueurChoisi, carteSelectionnee);
     					eventLabel.setText("Vous avez jouer sur la pile limite de "+joueurChoisi);
     					Botte botte = controleur.canCoupFourre(joueurActuel, joueurChoisi); 
     					if(botte != null) {
     						joueurChoisi.coupFourre(botte);
     					}
     					synchronized (joueurActuel) {
     						joueurActuel.notify(); 
     					}
     				}
     				else{
     					eventLabel.setText("Vous ne pouvez pas jouer cette carte ici, re-selectionnez une carte");    					}
     			}
     			else {
     				eventLabel.setText("Il faut selectionner une carte");
     			
     			}
     			carteSelectionnee=null;
     		}
     	});
     	joueur1.add(limiteJ1);
     	
     	
     	
     	botteJ1.addActionListener(new ActionListener(){
     		public  synchronized void actionPerformed(ActionEvent event){
     			Joueur joueurActuel = controleur.getJoueurActuel();
     			Joueur joueurChoisi = controleur.getJoueurs().get(0);
     			if (carteSelectionnee!=null){
     				if (controleur.isPosableSurBotte(joueurActuel,controleur.getJoueurs().get(0), carteSelectionnee)==true){
     					controleur.jouer(joueurActuel,controleur.getJoueurs().get(0), carteSelectionnee);
     					eventLabel.setText("Vous avez jouer sur la pile botte de "+joueurChoisi);
     					synchronized (joueurActuel) {
     						joueurActuel.notify(); 
     					}
     				}
     				else{
     					eventLabel.setText("Vous ne pouvez pas jouer cette carte ici, re-selectionnez une carte");    					}
     			}
     			else {
     				eventLabel.setText("Il faut selectionner une carte");
     			
     			}
     			carteSelectionnee=null;
     		}
     	});
     	joueur1.add(botteJ1);
     	
     	
     	joueur1.add(kmJ1);
     	joueur1.add(bottePossJ1);
     	joueur1.add(isAttaqueJ1);
     	joueur1.add(isLimiteJ1);
     	joueur1.add(feuVertInitJ1);
     	this.feuVertInitJ1.setHorizontalAlignment(JLabel.CENTER);
     	
     	
     	b1.add(joueur1);
     	
     	/////////
     	JInternalFrame joueur2 = new JInternalFrame(); 
     	joueur2.setSize(30, 20);
     	nom=controleur.getJoueurs().get(1).getNom();
     	joueur2.setTitle(nom);
     	joueur2.setVisible(true);
     	joueur2.setLayout(new GridLayout(5, 2));
     	
     	
     	etapeJ2.addActionListener(new ActionListener(){
     		public  synchronized void actionPerformed(ActionEvent event){
     			Joueur joueurActuel = controleur.getJoueurActuel();
     			Joueur joueurChoisi = controleur.getJoueurs().get(1);
     			if (carteSelectionnee!=null){
     				if (controleur.isPosableSurEtape(joueurActuel,controleur.getJoueurs().get(1), carteSelectionnee)==true){
     					controleur.jouer(joueurActuel,controleur.getJoueurs().get(1), carteSelectionnee);
     					eventLabel.setText("Vous avez jouer sur la pile etape de "+joueurChoisi);
     					synchronized (joueurActuel) {
     						joueurActuel.notify(); 
     					}
     				}
     				else{
     					eventLabel.setText("Vous ne pouvez pas jouer cette carte ici, re-selectionnez une carte");    					}
     			}
     			else {
     				eventLabel.setText("Il faut selectionner une carte");
     			
     			}
     			carteSelectionnee=null;
     		}
     	});
     	joueur2.add (etapeJ2);
     	
     	
     	attaqueJ2.addActionListener(new ActionListener(){
     		public  synchronized void actionPerformed(ActionEvent event){
     			Joueur joueurChoisi = controleur.getJoueurs().get(1);
     			Joueur joueurActuel = controleur.getJoueurActuel();
 
     			if (carteSelectionnee!=null){
 
     				if (controleur.isPosableSurAttaque(joueurActuel,joueurChoisi, carteSelectionnee)==true){
     					controleur.jouer(joueurActuel,joueurChoisi, carteSelectionnee);
     					eventLabel.setText("Vous avez jouer sur la pile attaque de "+joueurChoisi);
     					feuVertInitJ2.setText("Feu vert initial: Feu Vert");
     					Botte botte = controleur.canCoupFourre(joueurActuel, joueurChoisi); 
 
     					if(botte != null) {
     						joueurChoisi.coupFourre(botte);
     					}
     					synchronized (joueurActuel) {
     						joueurActuel.notify(); 
     					}
     				}
     				else{
     					eventLabel.setText("Vous ne pouvez pas jouer cette carte ici, re-selectionnez une carte");    					}
     			}
     			else {
     				eventLabel.setText("Il faut selectionner une carte");
     			
     			}
     			carteSelectionnee=null;
     		}
     	});
     	joueur2.add(attaqueJ2);
     	
     	
     	limiteJ2.addActionListener(new ActionListener(){
     		public  synchronized void actionPerformed(ActionEvent event){
     			Joueur joueurChoisi = controleur.getJoueurs().get(1);
     			Joueur joueurActuel = controleur.getJoueurActuel();
 
     			if (carteSelectionnee!=null){
     				if (controleur.isPosableSurLimiteVitesse(joueurActuel,joueurChoisi , carteSelectionnee)){
     					controleur.jouer(joueurActuel, joueurChoisi , carteSelectionnee);
     					eventLabel.setText("Vous avez jouer sur la pile limite de "+joueurChoisi);
     					Botte botte = controleur.canCoupFourre(joueurActuel, joueurChoisi); 
     					if(botte != null) {
     						joueurChoisi.coupFourre(botte);
     					}
     					synchronized (joueurActuel) {
     						joueurActuel.notify(); 
     					}
     				}
     				else{
     					eventLabel.setText("Vous ne pouvez pas jouer cette carte ici, re-selectionnez une carte");    					}
     			}
     			else {
     				eventLabel.setText("Il faut selectionner une carte");
     			
     			}
     			carteSelectionnee=null;
     		}
     	});
     	joueur2.add(limiteJ2);
     	
     	
     	botteJ2.addActionListener(new ActionListener(){
     		public  synchronized void actionPerformed(ActionEvent event){
     			Joueur joueurActuel = controleur.getJoueurActuel();
     			Joueur joueurChoisi = controleur.getJoueurs().get(1);
     			if (carteSelectionnee!=null){
     				if (controleur.isPosableSurBotte(joueurActuel,controleur.getJoueurs().get(1), carteSelectionnee)==true){
     					controleur.jouer(joueurActuel,controleur.getJoueurs().get(1), carteSelectionnee);
     					eventLabel.setText("Vous avez jouer sur la pile botte de "+joueurChoisi);
     					synchronized (joueurActuel) {
     						joueurActuel.notify(); 
     					}
     				}
     				else{
     					eventLabel.setText("Vous ne pouvez pas jouer cette carte ici, re-selectionnez une carte");    					}
     			}
     			else {
     				eventLabel.setText("Il faut selectionner une carte");
     			
     			}
     			carteSelectionnee=null;
     		}
     	});
     	joueur2.add(botteJ2);
     	
     	
     	joueur2.add(kmJ2);
     	joueur2.add(bottePossJ2);
     	joueur2.add(isAttaqueJ2);
     	joueur2.add(isLimiteJ2);
     	joueur2.add(feuVertInitJ2);
     	this.feuVertInitJ2.setHorizontalAlignment(JLabel.CENTER);
     	
     	b1.add(joueur2);
     
     /////////
 	JInternalFrame joueur3 = new JInternalFrame(); 
 	joueur3.setSize(30, 20);
 	nom=controleur.getJoueurs().get(2).getNom();
 	joueur3.setTitle(nom);
 	joueur3.setVisible(true);
 	joueur3.setLayout(new GridLayout(5, 2));
 	
 	
 	etapeJ3.addActionListener(new ActionListener(){
 		public  synchronized void actionPerformed(ActionEvent event){
 			Joueur joueurActuel = controleur.getJoueurActuel();
 			Joueur joueurChoisi = controleur.getJoueurs().get(2);
 			if (carteSelectionnee!=null){
 				if (controleur.isPosableSurEtape(joueurActuel,controleur.getJoueurs().get(2), carteSelectionnee)==true){
 					controleur.jouer(joueurActuel,controleur.getJoueurs().get(2), carteSelectionnee);
 					eventLabel.setText("Vous avez jouer sur la pile etape de "+joueurChoisi);
 					
 					synchronized (joueurActuel) {
 						joueurActuel.notify(); 
 					}
 				}
 				else{
 					eventLabel.setText("Vous ne pouvez pas jouer cette carte ici, re-selectionnez une carte");					}
 			}
 			else {
 				eventLabel.setText("Il faut selectionner une carte");
 			
 			}
 			carteSelectionnee=null;
 		}
 	});
 	joueur3.add (etapeJ3);
 	
 	
 	attaqueJ3.addActionListener(new ActionListener(){
 		public  synchronized void actionPerformed(ActionEvent event){
 			Joueur joueurChoisi = controleur.getJoueurs().get(2);
 			Joueur joueurActuel = controleur.getJoueurActuel();
 
 			if (carteSelectionnee!=null){
 
 				if (controleur.isPosableSurAttaque(joueurActuel, joueurChoisi, carteSelectionnee)==true){
 					controleur.jouer(joueurActuel, joueurChoisi, carteSelectionnee);
 					feuVertInitJ3.setText("Feu vert initial: Feu Vert");
 					eventLabel.setText("Vous avez jouer sur la pile attaque de "+joueurChoisi);
 					Botte botte = controleur.canCoupFourre(joueurActuel, joueurChoisi); 
 
 					if(botte != null) {
 						joueurChoisi.coupFourre(botte);
 					}
 					synchronized (joueurActuel) {
 						joueurActuel.notify(); 
 					}
 				}
 				else{
 					eventLabel.setText("Vous ne pouvez pas jouer cette carte ici, re-selectionnez une carte");					}
 			}
 			else {
 				eventLabel.setText("Il faut selectionner une carte");
 			
 			}
 			carteSelectionnee=null;
 		}
 	});
 	joueur3.add(attaqueJ3);
 	
 	
 	limiteJ3.addActionListener(new ActionListener(){
 		public  synchronized void actionPerformed(ActionEvent event){
 			Joueur joueurActuel = controleur.getJoueurActuel();
 
 			Joueur joueurChoisi = controleur.getJoueurs().get(2);
 			if (carteSelectionnee!=null){
 				if (controleur.isPosableSurLimiteVitesse(joueurActuel,joueurChoisi, carteSelectionnee)==true){
 					controleur.jouer(joueurActuel,joueurChoisi, carteSelectionnee);
 					eventLabel.setText("Vous avez jouer sur la pile limite de "+joueurChoisi);
 					Botte botte = controleur.canCoupFourre(joueurActuel, joueurChoisi); 
 					if(botte != null) {
 						joueurChoisi.coupFourre(botte);
 					}
 					synchronized (joueurActuel) {
 						joueurActuel.notify(); 
 					}
 				}
 				else{
 					eventLabel.setText("Vous ne pouvez pas jouer cette carte ici, re-selectionnez une carte");					}
 			}
 			else {
 				eventLabel.setText("Il faut selectionner une carte");
 			
 			}
 			carteSelectionnee=null;
 		}
 	});
 	joueur3.add(limiteJ3);
 	
 	
 	botteJ3.addActionListener(new ActionListener(){
 		public  synchronized void actionPerformed(ActionEvent event){
 			Joueur joueurActuel = controleur.getJoueurActuel();
 			Joueur joueurChoisi = controleur.getJoueurs().get(2);
 			if (carteSelectionnee!=null){
 				if (controleur.isPosableSurBotte(joueurActuel,controleur.getJoueurs().get(2), carteSelectionnee)==true){
 					controleur.jouer(joueurActuel,controleur.getJoueurs().get(2), carteSelectionnee);
 					eventLabel.setText("Vous avez jouer sur la pile botte de "+joueurChoisi);
 					synchronized (joueurActuel) {
 						joueurActuel.notify(); 
 					}
 				}
 				else{
 					eventLabel.setText("Vous ne pouvez pas jouer cette carte ici, re-selectionnez une carte");					}
 			}
 			else {
 				eventLabel.setText("Il faut selectionner une carte");
 			
 			}
 			carteSelectionnee=null;
 		}
 	});
 	joueur3.add(botteJ3);
 	
 	
 	joueur3.add(kmJ3);
 	joueur3.add(bottePossJ3);
 	joueur3.add(isAttaqueJ3);
 	joueur3.add(isLimiteJ3);
 	joueur3.add(feuVertInitJ3);
 	this.feuVertInitJ3.setHorizontalAlignment(JLabel.CENTER);
 	
 	b1.add(joueur3);
 	
 	///////////
 	JInternalFrame joueur4 = new JInternalFrame(); 
 	joueur4.setSize(30, 20);
 	nom=controleur.getJoueurs().get(3).getNom();
 	joueur4.setTitle(nom);
 	joueur4.setVisible(true);
 	joueur4.setLayout(new GridLayout(5, 2));
 	
 	
 	etapeJ4.addActionListener(new ActionListener(){
 		public  synchronized void actionPerformed(ActionEvent event){
 			Joueur joueurActuel = controleur.getJoueurActuel();
 			Joueur joueurChoisi = controleur.getJoueurs().get(3);
 			if (carteSelectionnee!=null){
 				if (controleur.isPosableSurEtape(joueurActuel,controleur.getJoueurs().get(3), carteSelectionnee)==true){
 					controleur.jouer(joueurActuel,controleur.getJoueurs().get(3), carteSelectionnee);
 					eventLabel.setText("Vous avez jouer sur la pile etape de "+joueurChoisi);
 					synchronized (joueurActuel) {
 						joueurActuel.notify(); 
 					}
 				}
 				else{
 					eventLabel.setText("Vous ne pouvez pas jouer cette carte ici, re-selectionnez une carte");					}
 			}
 			else {
 				eventLabel.setText("Il faut selectionner une carte");
 			
 			}
 			carteSelectionnee=null;
 		}
 	});
 	joueur4.add (etapeJ4);
 	
 	
 	attaqueJ4.addActionListener(new ActionListener(){
 		public  synchronized void actionPerformed(ActionEvent event){
 			Joueur joueurChoisi = controleur.getJoueurs().get(3);
 			Joueur joueurActuel = controleur.getJoueurActuel();
 
 			if (carteSelectionnee!=null){
 
 				if (controleur.isPosableSurAttaque(joueurActuel,joueurChoisi, carteSelectionnee)==true){
 					controleur.jouer(joueurActuel,joueurChoisi, carteSelectionnee);
 					eventLabel.setText("Vous avez jouer sur la pile attaque de "+joueurChoisi);
 					feuVertInitJ4.setText("Feu vert initial: Feu Vert");
 					Botte botte = controleur.canCoupFourre(joueurActuel, joueurChoisi); 
 
 					if(botte != null) {
 						joueurChoisi.coupFourre(botte);
 					}
 					synchronized (joueurActuel) {
 						joueurActuel.notify(); 
 					}
 				}
 				else{
 					eventLabel.setText("Vous ne pouvez pas jouer cette carte ici, re-selectionnez une carte");					}
 			}
 			else {
 				eventLabel.setText("Il faut selectionner une carte");
 			
 			}
 			carteSelectionnee=null;
 		}
 	});
 	joueur4.add(attaqueJ4);
 	
 	
 	limiteJ4.addActionListener(new ActionListener(){
 		public  synchronized void actionPerformed(ActionEvent event){
 			Joueur joueurChoisi = controleur.getJoueurs().get(3);
 			Joueur joueurActuel = controleur.getJoueurActuel();
 
 			if (carteSelectionnee!=null){
 				if (controleur.isPosableSurLimiteVitesse(joueurActuel,joueurChoisi, carteSelectionnee)==true){
 					controleur.jouer(joueurActuel,joueurChoisi, carteSelectionnee);
 					eventLabel.setText("Vous avez jouer sur la pile limite de "+joueurChoisi);
 					Botte botte = controleur.canCoupFourre(joueurActuel, joueurChoisi); 
 					if(botte != null) {
 						joueurChoisi.coupFourre(botte);
 					}
 					synchronized (joueurActuel) {
 						joueurActuel.notify(); 
 					}
 				}
 				else{
 					eventLabel.setText("Vous ne pouvez pas jouer cette carte ici, re-selectionnez une carte");					}
 			}
 			else {
 				eventLabel.setText("Il faut selectionner une carte");
 			
 			}
 			carteSelectionnee=null;
 		}
 	});
 	joueur4.add(limiteJ4);
 	
 	
 	botteJ4.addActionListener(new ActionListener(){
 		public  synchronized void actionPerformed(ActionEvent event){
 			Joueur joueurActuel = controleur.getJoueurActuel();
 			Joueur joueurChoisi = controleur.getJoueurs().get(3);
 			if (carteSelectionnee!=null){
 				if (controleur.isPosableSurBotte(joueurActuel,controleur.getJoueurs().get(3), carteSelectionnee)==true){
 					controleur.jouer(joueurActuel,controleur.getJoueurs().get(3), carteSelectionnee);
 					eventLabel.setText("Vous avez jouer sur la pile botte de "+joueurChoisi);
 					synchronized (joueurActuel) {
 						joueurActuel.notify(); 
 					}
 				} else {
 					
 					eventLabel.setText("Vous ne pouvez pas jouer cette carte ici, re-selectionnez une carte");
 				}
 			}
 			else {
 				eventLabel.setText("Il faut selectionner une carte");
 			
 			}
 			carteSelectionnee=null;
 		}
 	});
 	joueur4.add(botteJ4);
 	
 	
 	joueur4.add(kmJ4);
 	joueur4.add(bottePossJ4);
 	joueur4.add(isAttaqueJ4);
 	joueur4.add(isLimiteJ4);
 	joueur4.add(feuVertInitJ4);
 	this.feuVertInitJ4.setHorizontalAlignment(JLabel.CENTER);
 	
 	b1.add(joueur4);
 	
     }
     
     /**
      * Idem pour les cartes en main du joueur qui joue
      */
     
     Box b2 = Box.createHorizontalBox();
     
   
 	carte1.setSize(30, 20);
 	carte1.setTitle("Carte 1");
 	carte1.setVisible(true);
 	
 	
 	carte1Joueur.addActionListener(new ActionListener(){
 		public  synchronized void actionPerformed(ActionEvent event){
 
 			if(controleur.getJoueurActuel() instanceof Humain) {
 				if(controleur.hasPioche(controleur.getJoueurActuel()) ){
 					eventLabel.setText("Vous avez choisi la premiere carte");
 					carteSelectionnee=controleur.getMain(controleur.getJoueurActuel()).get(0);
 				
 				} else {
 					eventLabel.setText("Vous devez piochez avant de sélectionner une carte.");
 					
 				}
 			} else {
 				eventLabel.setText("Ce n'est pas votre tour."); 
 				
 			}
 		}
 	});
 	carte1.add(carte1Joueur);
 	b2.add(carte1);
 	
 	
 	carte2.setSize(30, 20);
 	carte2.setTitle("Carte 2");
 	carte2.setVisible(true);
 	
 	
 	carte2Joueur.addActionListener(new ActionListener(){
 		public void actionPerformed(ActionEvent event){
 			if(controleur.getJoueurActuel() instanceof Humain) {
 				if(controleur.hasPioche(controleur.getJoueurActuel()) ){
 					eventLabel.setText("Vous avez choisi la seconde carte");
 					carteSelectionnee=controleur.getMain(controleur.getJoueurActuel()).get(1);
 				
 				} else {
 					eventLabel.setText("Vous devez piochez avant de sélectionner une carte.");
 					
 				}
 			} else {
 				eventLabel.setText("Ce n'est pas votre tour."); 
 			}
 		}
 	});
 	
 	carte2.add(carte2Joueur);
 	b2.add(carte2);
 	
 	
 	carte3.setSize(30, 20);
 	carte3.setTitle("Carte 3");
 	carte3.setVisible(true);
 
 	
 	carte3Joueur.addActionListener(new ActionListener(){
 		public  synchronized void actionPerformed(ActionEvent event){
 			if(controleur.getJoueurActuel() instanceof Humain) {
 				if(controleur.hasPioche(controleur.getJoueurActuel()) ){
 					eventLabel.setText("Vous avez choisi la troisième carte");
 					carteSelectionnee=controleur.getMain(controleur.getJoueurActuel()).get(2);
 				
 				} else {
 					eventLabel.setText("Vous devez piochez avant de sélectionner une carte.");
 					
 				}
 			} else {
 				eventLabel.setText("Ce n'est pas votre tour."); 
 			}
 		}
 	});
 	carte3.add(carte3Joueur);
 	b2.add(carte3);
 	
 	
 	carte4.setSize(30, 20);
 	carte4.setTitle("Carte 4");
 	carte4.setVisible(true);
 	
 	
 	carte4Joueur.addActionListener(new ActionListener(){
 		public  synchronized void actionPerformed(ActionEvent event){
 			if(controleur.getJoueurActuel() instanceof Humain) {
 				if(controleur.hasPioche(controleur.getJoueurActuel()) ){
 					eventLabel.setText("Vous avez choisi la quatrième carte");
 					carteSelectionnee=controleur.getMain(controleur.getJoueurActuel()).get(3);
 				
 				} else {
 					eventLabel.setText("Vous devez piochez avant de sélectionner une carte.");
 					
 				}
 			} else {
 				eventLabel.setText("Ce n'est pas votre tour."); 
 			}
 		}
 	});
 	carte4.add(carte4Joueur);
 	b2.add(carte4);
 	
 	
 	carte5.setSize(30, 20);
 	carte5.setTitle("Carte 5");
 	carte5.setVisible(false);
 	
 	
 	carte5Joueur.addActionListener(new ActionListener(){
 		public  synchronized void actionPerformed(ActionEvent event){
 			if(controleur.getJoueurActuel() instanceof Humain) {
 				eventLabel.setText("Vous avez choisi la cinquieme carte");
 				carteSelectionnee=controleur.getMain(controleur.getJoueurActuel()).get(4);
 		
 			} else {
 				eventLabel.setText("Ce n'est pas votre tour.");
 				
 			}
 		}
 	});
 	carte5.add(carte5Joueur);
 	b2.add(carte5);
 	
     /**
      * Idem pour les talon, defausse, et boite d'evenements
      */
     Box b3 = Box.createHorizontalBox();
     
     talon.setSize(30, 20);
 	talon.setTitle("Talon");
 	talon.setVisible(true);
 	
 	
 	talonBouton.addActionListener(new ActionListener(){
 		public  synchronized void actionPerformed(ActionEvent event){
 			if(controleur.getJoueurActuel() instanceof Humain) {
 				if(controleur.getJoueurActuel().getJeuEnMain().getMain().size() == 4) {
 					Joueur joueurActuel = controleur.getJoueurActuel();
 					joueurActuel.piocherTalon();
 					eventLabel.setText("Vous avez pioché dans le talon.");
 					
 				} else {
 					eventLabel.setText("Vous avez déjà pioché.");
 					
 				}
 			}
 		}
 	});
 	talon.add(talonBouton);
 	b3.add(talon);
 	
 	///////
 	defausse.setSize(30, 20);
 	defausse.setTitle("Defausse");
 	defausse.setVisible(true);
 	
 	
 	defausseBouton.addActionListener(new ActionListener(){
 		public synchronized void actionPerformed(ActionEvent event){
 			
 			if(controleur.getJoueurActuel() instanceof Humain) {
 				
 					if(controleur.getJoueurActuel().getJeuEnMain().getMain().size() == 4) {
 						if(controleur.isDefausseEmpty() == false) {
 							Joueur joueurActuel = controleur.getJoueurActuel();
 							joueurActuel.piocherDefausse();
 							eventLabel.setText("Vous avez pioché dans la défausse.");
 						} else {
 							eventLabel.setText("La défausse est vide.");							
 						}
 					} else {
 						Joueur joueurActuel = controleur.getJoueurActuel();
 						joueurActuel.defausser(carteSelectionnee);
 						carteSelectionnee = null;
 						
 						synchronized (joueurActuel) {
 							joueurActuel.notify(); 
 						}
 						
 					}
 				
 			}
 		}
 	});
 	defausse.add(defausseBouton);
 	b3.add(defausse);
 	
 	//////
 	JInternalFrame evenement = new JInternalFrame(); 
 	eventLabel.setForeground(Color.BLUE);
 	evenement.setSize(30, 20);
 	evenement.setTitle("Guide de jeu");
 	evenement.setVisible(true);
 	evenement.add(eventLabel);
 	b3.add(evenement);
     
     
     /**
      * On crée un conteneur avec gestion verticale pour empiler les autres box
      */
     Box b4 = Box.createVerticalBox();
     b4.add(b1);
     b4.add(b2);
     b4.add(b3);
 
     this.getContentPane().add(b4);
     this.setVisible(true);
   
 	  
 }
   	/**
   	 * Methode update permettant a la vue de se mettre a jour par rapport au modèle.
   	 */
 	@Override
 	public void update(Observable arg0, Object arg1) {
 		if(arg0 instanceof Defausse) {
 			this.defausse.setTitle("Defausse : "+this.controleur.getCarteVisibleDefausse());
 			
 
 		} else if (arg0 instanceof JeuEnMain && ((JeuEnMain) arg0).getJoueur() instanceof Humain) {
 			this.carte5.setVisible(false);//On cache la 5e carte qui est vide a ce moment
 
 			this.carte1.setTitle(""+((JeuEnMain) arg0).getMain().get(0).toString());
 
 			this.carte2.setTitle(""+((JeuEnMain) arg0).getMain().get(1).toString());
 			this.carte3.setTitle(""+((JeuEnMain) arg0).getMain().get(2).toString());
 			
 			if(((JeuEnMain) arg0).getMain().size() >= 4) {
 				this.carte4.setTitle(""+((JeuEnMain) arg0).getMain().get(3).toString());
 			}
 			
 	  		if(((JeuEnMain) arg0).getMain().size() == 5) {
 	  			this.carte5.setVisible(true);
 	  			this.carte5.setTitle(""+((JeuEnMain) arg0).getMain().get(4).toString());
 	  		}
 		
 		} else if (arg0 instanceof PartieDeJeu) {
 			
 			if(controleur.isPartieFinie()) {
 				JOptionPane.showMessageDialog(null, "Victoire de "+ controleur.getGagnant().getNom());
 						
 			}
 			
 			this.carte5.setVisible(false);
 			this.carte1.setTitle("");
 
 			this.carte2.setTitle("");
 			this.carte3.setTitle("");
 	  		this.carte4.setTitle("");
 	  	
 	  		
 	  		if(controleur.getJoueurActuel() instanceof Humain && arg1 =="debut") {
 	  			JOptionPane.showMessageDialog(null, controleur.getJoueurActuel().getNom()+", à vous de jouer.");
 	  			
 	  		}
 	  		
 	  		
 	  		
 	  		/**
 			 * On rend visible les cartes du joueur a qui c'est le tour une fois qu'il a clqiuer sur la JoptionPane.
 			 */
 			this.carte5.setVisible(false);
 			Joueur joueurActuel = controleur.getJoueurActuel();
 			this.carte1.setTitle(""+joueurActuel.getJeuEnMain().getMain().get(0).toString());
 
 			this.carte2.setTitle(""+ joueurActuel.getJeuEnMain().getMain().get(1).toString());
 			this.carte3.setTitle(""+ joueurActuel.getJeuEnMain().getMain().get(2).toString());
 	  		this.carte4.setTitle(""+joueurActuel.getJeuEnMain().getMain().get(3).toString());
 	  		if(joueurActuel.getJeuEnMain().getMain().size() == 5) {
 	  			this.carte5.setVisible(true);
 	  			this.carte5.setTitle(""+joueurActuel.getJeuEnMain().getMain().get(4).toString());
 
 	  		}
 	  		
 		} else if(arg0 instanceof JeuSurTable) {
 					
 	  			this.isAttaqueJ1.setText("Pile Bataille : "+controleur.getCarteBataille( controleur.getJ1().getJeuSurTable() )) ;
 	  			this.isLimiteJ1.setText("Pile Vitesse : "+controleur.getCarteLimiteVitesse( controleur.getJ1().getJeuSurTable()));
 	  			this.kmJ1.setText("Bornes : "+controleur.getKm(controleur.getJ1().getJeuSurTable()));
 	  			this.bottePossJ1.setText("<html>Bottes :<br>"+controleur.getCarteBotte(controleur.getJ1().getJeuSurTable())+"</html>");
 	  			this.feuVertInitJ1.setText("Feu vert initial: "+controleur.getEtatFeuVertInitial(controleur.getJ1().getJeuSurTable()));
 	  		
 		
 	  			this.isAttaqueJ2.setText("Pile Bataille : "+controleur.getCarteBataille( controleur.getJ2().getJeuSurTable()));
 	  			this.isLimiteJ2.setText("Pile Vitesse : "+controleur.getCarteLimiteVitesse( controleur.getJ2().getJeuSurTable()));
 	  			this.kmJ2.setText("Bornes : "+controleur.getKm(controleur.getJ2().getJeuSurTable()));
 	  			this.bottePossJ2.setText("<html>Bottes :<br>"+controleur.getCarteBotte(controleur.getJ2().getJeuSurTable())+"</html>");
 	  			this.feuVertInitJ2.setText("Feu vert initial: "+controleur.getEtatFeuVertInitial(controleur.getJ2().getJeuSurTable()));
 	  		
 	  		if(this.nbJoueurs > 2) {
 	  			this.isAttaqueJ3.setText("Pile Bataille : "+controleur.getCarteBataille(controleur.getJ3().getJeuSurTable()));
 	  			this.isLimiteJ3.setText("Pile Vitesse : "+controleur.getCarteLimiteVitesse(controleur.getJ3().getJeuSurTable()));
 	  			this.kmJ3.setText("Bornes : "+controleur.getKm(controleur.getJ3().getJeuSurTable()));
 	  			this.bottePossJ3.setText("<html>Bottes :<br>"+controleur.getCarteBotte(controleur.getJ3().getJeuSurTable())+"</html>");
 	  			this.feuVertInitJ3.setText("Feu vert initial: "+controleur.getEtatFeuVertInitial(controleur.getJ3().getJeuSurTable()));
 	  		}
 	  		if(this.nbJoueurs > 3) {
 	  			this.isAttaqueJ4.setText("Pile Bataille : "+controleur.getCarteBataille( controleur.getJ4().getJeuSurTable()));
 	  			this.isLimiteJ4.setText("Pile Vitesse : "+controleur.getCarteLimiteVitesse(  controleur.getJ4().getJeuSurTable()));
 	  			this.kmJ4.setText("Bornes : "+controleur.getKm( controleur.getJ4().getJeuSurTable()));
 	  			this.bottePossJ4.setText("<html>Bottes :<br>"+controleur.getCarteBotte( controleur.getJ4().getJeuSurTable())+"</html>");
 	  			this.feuVertInitJ4.setText("Feu vert initial: "+controleur.getEtatFeuVertInitial( controleur.getJ4().getJeuSurTable()));
 	  		}
 	  		
   			
   		} else if(arg0 instanceof Humain && arg1 == "coupFourree") {
   			Integer choix = JOptionPane.showConfirmDialog(null, ((Joueur) arg0).getNom()+", vous pouvez jouer un coup fourrée, souhaitez vous le jouer ?");
   			if(choix ==0) {
   				controleur.jouerCoupFourre((Joueur) arg0, ((Joueur) arg0).canCoupFourre());
   			}
  			synchronized(arg0) {
  				arg0.notify();
  			}
   		}
 	}
 }
 
