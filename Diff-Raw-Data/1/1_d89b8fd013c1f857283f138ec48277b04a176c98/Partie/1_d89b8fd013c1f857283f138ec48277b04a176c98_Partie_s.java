 package fr.jules_cesar.Paintball;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Set;
 import java.util.Stack;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 import fr.jules_cesar.Paintball.ScoreBoard.TableauScore;
 
 public class Partie {
 
 	/* Partie en attente */
 	Stack<Player> file;
 	
 	/* Partie en cours */
 	private int kill_bleu = 0;
 	private int kill_rouge = 0;
 	private HashMap<Player, Integer> joueurs_rouge = new HashMap<Player, Integer>();
 	private HashMap<Player, Integer> joueurs_bleu = new HashMap<Player, Integer>();
 	
 	/* Autre */
 	private short etat;
 	private ArrayList<Player> liste_spectateurs = new ArrayList<Player>();
 	private TableauScore tableau;
 	
 	public Partie(){
 		etat = 0;
 		file = new Stack<Player>();
 		tableau = new TableauScore();
 	}
 	
 	/**
 	 * Ajoute un joueur dans la partie selon l'equipe choisie
 	 * @param joueur Le joueur a ajouter
 	 * @param equipe L'equipe du joueur
 	 */
 	public void ajouterJoueur(Player joueur, String equipe){
 		file.push(joueur);
 		if(equipe.equalsIgnoreCase("rouge")) joueurs_rouge.put(joueur, 4);
 		else if(equipe.equalsIgnoreCase("bleu")) joueurs_bleu.put(joueur, 4);
 	}
 	
 	public void demarrerPartie(){
 		etat = 1;
 		equilibrerEquipe();
 		file = null;
 		
 		// Teleportation des joueurs
 		Set<Player> joueurs = joueurs_rouge.keySet();
 		for(Player p : joueurs){
 			Paintball.getArene().teleporterRouge(p);
 			tableau.ajouterVueJoueur(p, "rouge");
 			Paintball.saveInventory(p, 'r');
 		}
 		joueurs = joueurs_bleu.keySet();
 		for(Player p : joueurs){
 			Paintball.getArene().teleporterBleu(p);
 			tableau.ajouterVueJoueur(p, "bleu");
 			Paintball.saveInventory(p, 'b');
 		}
 		annoncer(Paintball.messages.get("game.start"));
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
 	 * Retire un joueur du jeu
 	 * @param joueur Le joueur a retirer
 	 */
 	public void retirerJoueur(Player joueur, boolean join){
 		if(join) file.remove(joueur);
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
 	public void toucherJoueur(Player victime, Player tireur){
 		char equipe_victime = (joueurs_rouge.containsKey(victime)?'r':'b');
 		char equipe_tireur = (joueurs_bleu.containsKey(tireur)?'b':'r');
 		int vie = (equipe_victime=='r'?joueurs_rouge.get(victime):joueurs_bleu.get(victime));
 		if(equipe_victime == equipe_tireur){
 			if(vie == 1){
 				annoncer(Paintball.messages.get("game.badhit.died", victime.getName(), tireur.getName()));
 				tuerJoueur(victime, true);
 			}
 			else{
 				annoncer(Paintball.messages.get("game.badhit", victime.getName(), tireur.getName()));
 				if(equipe_victime == 'r') joueurs_rouge.put(victime, vie - 1);
 				else joueurs_bleu.put(victime, vie - 1);
 			}
 		}
 		else{
 			if(vie == 1){
 				annoncer(Paintball.messages.get("game.hit.died", victime.getName(), tireur.getName()));
 				tuerJoueur(victime, true);
 			}
 			else{
 				annoncer(Paintball.messages.get("game.hit", victime.getName(), tireur.getName()));
 				if(equipe_victime == 'r') joueurs_rouge.put(victime, vie - 1);
 				else joueurs_bleu.put(victime, vie - 1);
 			}
 			if(equipe_tireur == 'r') kill_rouge++;
 			else kill_bleu++;
 		}
 		tableau.enleverVie(victime);
 	}
 	
 	/**
 	 * Passe le joueur en spectateur et annonce sa mort au public
 	 * @param joueur Le joueur mort
 	 */
 	private void tuerJoueur(Player joueur, boolean naturel){
 		retirerJoueur(joueur, false);
 		Paintball.getArene().teleporterSpectateur(joueur);
 		ajouterSpectateur(joueur);
 		if(!naturel) annoncer(Paintball.messages.get("game.disconnect", joueur.getName()));
 		Paintball.loadInventoryIfNecessary(joueur);
 	}
 	
 	@SuppressWarnings("unchecked")
 	public void finPartie() {
 		if(kill_bleu > kill_rouge) annoncer(Paintball.messages.get("game.victory.blue", kill_bleu, kill_rouge));
 		else annoncer(Paintball.messages.get("game.victory.red", kill_rouge, kill_bleu));
 		if(joueurs_bleu.size() != 0){
 			for(Player p : joueurs_bleu.keySet()){
 				Paintball.getArene().teleporterSpectateur(p);
 				Paintball.loadInventoryIfNecessary(p);
 				tableau.retirerVue(p);
 			}
 		}
 		else{
 			for(Player p : joueurs_rouge.keySet()){
 				Paintball.getArene().teleporterSpectateur(p);
 				Paintball.loadInventoryIfNecessary(p);
 				tableau.retirerVue(p);
 			}
 		}
 		ArrayList<Player> spec = (ArrayList<Player>) liste_spectateurs.clone();
 		for(Player p : spec)
 			retirerSpectateur(p);
 	}
 	
 	public int nombreJoueursBleu(){
 		return joueurs_bleu.size();
 	}
 	
 	public int nombreJoueursRouge(){
 		return joueurs_rouge.size();
 	}
 
 
 	public void ajouterSpectateur(Player joueur){
 		joueur.setAllowFlight(true);
 		tableau.ajouterVueSpectateur(joueur);
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
 			retirerSpectateur(player);
 		else if(this.etat == 0){
 			if(joueurs_rouge.containsKey(player)) joueurs_rouge.remove(player);
 			else joueurs_bleu.remove(player);
 		}
 		else{
 			if(joueurs_rouge.containsKey(player)){
 				kill_bleu += joueurs_rouge.remove(player);
 				tuerJoueur(player, false);
 			}
 			else{
 				kill_rouge += joueurs_bleu.remove(player);
 				tuerJoueur(player, false);
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
 			while(!joueurs_bleu.containsKey(file.peek())) file.pop();
 			joueurs_rouge.put(file.peek(), 4);
 			joueurs_bleu.remove(file.peek());
			annoncer(ChatColor.GREEN + file.pop().getName() + ChatColor.BLUE + " passe dans l'equipe " + ChatColor.RED + "rouge " + ChatColor.BLUE + " suite a un desequilibre.");
 			annoncer(Paintball.messages.get("game.transfer.red", file.pop().getName()));
 			difference++;
 		}
 		while(difference > 1){
 			while(!joueurs_rouge.containsKey(file.peek())) file.pop();
 			joueurs_bleu.put(file.peek(), 4);
 			joueurs_rouge.remove(file.peek());
 			annoncer(Paintball.messages.get("game.transfer.blue", file.pop().getName()));
 			difference--;
 		}
 	}
 
 	public void retirerSpectateur(Player joueur) {
 		joueur.setAllowFlight(false);
 		liste_spectateurs.remove(joueur);
 		tableau.retirerVue(joueur);
 	}
 }
