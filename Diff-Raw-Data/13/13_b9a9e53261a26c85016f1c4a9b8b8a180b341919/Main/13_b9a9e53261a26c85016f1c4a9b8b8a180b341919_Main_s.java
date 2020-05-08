 package game;
 
 import game.TournamentExample.Entrant;
 
 import java.util.HashSet;
 import java.util.Set;
 
 
 public class Main {
 
 	public static void main(String[] args) {
 		
		TournamentExample.Entrant entrant = new TournamentExample.Entrant(5, "Nils");
         Set<String> players = new HashSet<String>();
         players.add("Bryan");
         players.add("Jared");
 
         TournamentExample.Team team = new TournamentExample.Team(5, "Nils", players);
         
         System.out.println("team.equals(entrant) is " + team.equals(entrant));
         System.out.println("entrant.equals(team) is " + entrant.equals(team));
 
 	}
 
 }
