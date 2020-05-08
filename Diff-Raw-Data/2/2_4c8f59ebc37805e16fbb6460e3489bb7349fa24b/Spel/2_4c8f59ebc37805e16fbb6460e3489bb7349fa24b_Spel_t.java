 package controllers;
 
 import java.awt.EventQueue;
 
 import logic.Joker;
 import logic.Speler;
 
 
 public class Spel {
 	
 	private Speler speler;
 	private Joker joker;
 
 	
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		
 		
 		
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					new Spel();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 	
 	
 	/**
 	 * Contructor
 	 */
 	public Spel() {
 		speler = new Speler();
 		joker = new Joker();
 		
 		views.MainWindow window = new views.MainWindow(this);
 	}
 	
 	/**
 	 * Speler methodes
 	 */
 	
 	public void setSpelerNaam( String naam ) {
 		speler.setNaam(naam);
 	}
 	
 	public String getSpelerNaam() {
		return speler.getNaam();
 	}
 	
 	/**
 	 * Joker methodes
 	 */
 
 	public int getJokerAantal() {
 		return joker.getAantal();
 	}
 	
 	public void addJoker() {
 		joker.addJoker();
 	}
 	
 	public void verwijderJokers( int aantal ) {
 		joker.verwijderJokers(aantal);
 	}
 }
