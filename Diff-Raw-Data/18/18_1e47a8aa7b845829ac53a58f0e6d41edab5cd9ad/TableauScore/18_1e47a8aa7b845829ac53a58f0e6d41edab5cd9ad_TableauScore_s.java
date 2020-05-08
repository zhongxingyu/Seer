 package fr.jules_cesar.Paintball.ScoreBoard;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.scoreboard.DisplaySlot;
 import org.bukkit.scoreboard.Objective;
 import org.bukkit.scoreboard.Score;
 import org.bukkit.scoreboard.Scoreboard;
 import org.bukkit.scoreboard.Team;
 
 public class TableauScore {
 	
 	private Scoreboard board;
 	private Objective objective;
 	private Team rouge;
 	private Team bleu;
 	
 	public TableauScore(){
 		board = Bukkit.getScoreboardManager().getNewScoreboard();
 		rouge = board.registerNewTeam("Rouge");
 		bleu = board.registerNewTeam("Bleu");
 		objective = board.registerNewObjective("Vies", "dummy");
 		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
 		objective.setDisplayName("Vies restantes");
 	}
 	
 	public void ajouterVueSpectateur(Player joueur){
 		joueur.setScoreboard(board);
 	}
 	
 	public void ajouterVueJoueur(Player joueur, String equipe){
 		joueur.setScoreboard(board);
 		if(equipe.equalsIgnoreCase("bleu")) bleu.addPlayer(joueur);
 		else rouge.addPlayer(joueur);
 		Score score = objective.getScore(joueur);
 		score.setScore(4);
 	}
 	
 	public void retirerVue(Player joueur){
 		joueur.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
 	}
 	
 	public void enleverVie(Player joueur){
 		Score score = objective.getScore(joueur);
 		int vie = score.getScore()-1;
 		if(vie != 0) score.setScore(vie);
 		else board.resetScores(joueur);
 	}
 }
