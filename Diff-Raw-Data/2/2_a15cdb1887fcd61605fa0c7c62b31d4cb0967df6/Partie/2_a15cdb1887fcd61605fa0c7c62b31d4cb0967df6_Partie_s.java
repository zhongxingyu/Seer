 package fr.jules_cesar.Paintball;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Queue;
 import java.util.Set;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 public class Partie {
 
 	/* Partie en attente */
 	Queue<Player> attente;
 	
 	/* Partie en cours */
 	private int kill_bleu = 0;
 	private int kill_rouge = 0;
 	private HashMap<Player, Integer> joueurs_rouge = new HashMap<Player, Integer>();
 	private HashMap<Player, Integer> joueurs_bleu = new HashMap<Player, Integer>();
 	
 	/* Autre */
 	private short etat;
 	private ArrayList<Player> liste_spectateurs = new ArrayList<Player>();
 	
 	public Partie(){
 		etat = 0;
 		attente = new LinkedList<Player>();
 	}
 	
 	/**
 	 * Ajoute un joueur dans la partie selon l'equipe choisie
 	 * @param joueur Le joueur a ajouter
 	 * @param equipe L'equipe du joueur
 	 */
 	public void ajouterJoueur(Player joueur, String equipe){
 		attente.add(joueur);
 		if(equipe.equalsIgnoreCase("rouge")) joueurs_rouge.put(joueur, 4);
 		else if(equipe.equalsIgnoreCase("bleu")) joueurs_bleu.put(joueur, 4);
 	}
 	
 	public void demarrerPartie(){
 		etat = 1;
 		equilibrerEquipe();
 		attente = null;
 		
 		// Teleportation des joueurs
 		Set<Player> joueurs = joueurs_rouge.keySet();
 		for(Player p : joueurs)
 			Paintball.getArene().teleporterRouge(p);
 		joueurs = joueurs_bleu.keySet();
 		for(Player p : joueurs)
 			Paintball.getArene().teleporterBleu(p);
 		annoncer("La partie commence !");
 	}
 	
 	/**
 	 * Determine le nombre de joueurs dans la partie
 	 * @return le nombre de joueurs
 	 */
 	public int nombreJoueurs(){
 		return joueurs_rouge.size() + joueurs_bleu.size();
 	}
 	
 	/**
 	 * Determine si le joueur est dans la partie
 	 * @param joueur Le joueur a tester
 	 * @return true si le joueur est dans la partie, false sinon
 	 */
 	public boolean estJoueur(Player joueur){
 		return (joueurs_rouge.containsKey(joueur) || joueurs_bleu.containsKey(joueur));
 	}
 	
 	/**
 	 * Retire un joueur du jeu si il quitte le serveur
 	 * @param joueur Le joueur a retirer
 	 */
 	public void retirerJoueur(Player joueur){
 		if(joueurs_rouge.containsKey(joueur))
 			joueurs_rouge.remove(joueur);
 		else
 			joueurs_bleu.remove(joueur);
 	}
 	
 	/**
 	 * Retire une vie au joueur et augmente le kill de l'equipe adverse
 	 * @param joueur Le joueur a qui il faut retirer une vie
 	 * @param tireur Le joueur qui a tire
 	 */
 	public void toucherJoueur(Player joueur, Player tireur){
 		if(joueurs_rouge.containsKey(joueur)){
 			kill_bleu++;
 			joueurs_rouge.put(joueur, joueurs_rouge.get(joueur) - 1);
 			if(joueurs_rouge.get(joueur) == 0) tuerJoueur(joueur, joueurs_rouge, true);
 		}
 		else{
 			kill_rouge++;
 			joueurs_bleu.put(joueur, joueurs_rouge.get(joueur) - 1);
 			if(joueurs_bleu.get(joueur) == 0) tuerJoueur(joueur, joueurs_bleu, true);
 		}
 		joueur.setHealth(10);
 		annoncer(joueur.getName() + " c'est fait toucher par " + tireur.getName());
 	}
 	
 	/**
 	 * Passe le joueur en spectateur et annonce sa mort au public
 	 * @param joueur Le joueur mort
 	 */
 	public void tuerJoueur(Player joueur, HashMap<Player, Integer> equipe, boolean naturel){
 		Paintball.getArene().teleporterSpectateur(joueur);
 		equipe.remove(joueur);
 		if(naturel) annoncer(joueur.getName() + " n'a plus de vie ! Il est donc elimine.");
 		else annoncer(joueur.getName() + " est considere comme mort suite a sa deconnexion.");
		if(nombreJoueurs() == 0) finPartie();
 	}
 	
 	private void finPartie() {
 		if(kill_bleu > kill_rouge) annoncer("L'equipe bleu gagne avec " + kill_bleu + " kills contre " + kill_rouge + " kills pour l'equipe rouge !");
 		else annoncer("L'equipe rouge gagne avec " + kill_rouge + " kills contre " + kill_bleu + " kills pour l'equipe bleu !");
 		Paintball.getArene().finPartie();
 	}
 
 	public void ajouterSpectateur(Player joueur){
 		// affichage scoreboard
 		// Paintball.getArene().teleporterSpectateur(joueur);
 		liste_spectateurs.add(joueur);
 	}
 	
 	/**
 	 * Affiche un message a tout les joueurs et spectateurs
 	 * @param message Le message a diffuser
 	 */
 	public void annoncer(String message){
 		ArrayList<Player> receveurs = new ArrayList<Player>();
 		receveurs.addAll(joueurs_bleu.keySet());
 		receveurs.addAll(joueurs_rouge.keySet());
 		receveurs.addAll(liste_spectateurs);
 		for(Player p : receveurs)
 			p.sendMessage(message);
 	}
 
 	public void deconnexion(Player player, boolean joueur) {
 		if(!joueur)
 			liste_spectateurs.remove(player);
 		else if(this.etat == 0){
 			if(joueurs_rouge.containsKey(player)) joueurs_rouge.remove(player);
 			else joueurs_bleu.remove(player);
 		}
 		else{
 			if(joueurs_rouge.containsKey(player)){
 				kill_bleu += joueurs_rouge.remove(player);
 				tuerJoueur(player, joueurs_rouge, false);
 			}
 			else{
 				kill_rouge += joueurs_bleu.remove(player);
 				tuerJoueur(player, joueurs_bleu, false);
 			}
 		}
 	}
 
 	public int obtenirEtat(){
 		return etat;
 	}
 	
 	public boolean estPresent(Player joueur) {
 		return (joueurs_rouge.containsKey(joueur) || joueurs_bleu.containsKey(joueur) || liste_spectateurs.contains(joueur));
 	}
 	
 	public void equilibrerEquipe(){
 		int difference = joueurs_rouge.size() - joueurs_bleu.size();
 		while(difference < -1){
 			while(!joueurs_bleu.containsKey(attente.peek())) attente.remove();
 			joueurs_rouge.put(attente.peek(), 4);
 			joueurs_bleu.remove(attente.peek());
 			annoncer(ChatColor.GREEN + attente.poll().getName() + ChatColor.BLUE + " passe dans l'equipe " + ChatColor.RED + "rouge " + ChatColor.BLUE + " suite a un desequilibre.");
 			difference++;
 		}
 		while(difference > 1){
 			while(!joueurs_rouge.containsKey(attente.peek())) attente.remove();
 			joueurs_bleu.put(attente.peek(), 4);
 			joueurs_rouge.remove(attente.peek());
 			annoncer(ChatColor.GREEN + attente.poll().getName() + ChatColor.BLUE + " passe dans l'equipe " + ChatColor.AQUA + "bleu " + ChatColor.BLUE + " suite a un desequilibre.");
 			difference--;
 		}
 	}
 }
