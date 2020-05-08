 package uk.ac.gla.dcs.tp3.w.algorithm;
 
 import java.util.LinkedList;
 
 import uk.ac.gla.dcs.tp3.w.league.*;
 import uk.ac.gla.dcs.tp3.w.algorithm.Graph;
 
 /**
  * @author gordon
  * 
  */
 public class Main {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		Team atlanta = new Team();
 		atlanta.setName("Atlanta");
 		atlanta.setGamesPlayed(170 - 8);
 		atlanta.setPoints(83);
 
 		Team philadelphia = new Team();
 		philadelphia.setName("Philadelphia");
 		philadelphia.setGamesPlayed(170 - 4);
 		philadelphia.setPoints(79);
 
 		Team newYork = new Team();
 		newYork.setName("New York");
 		newYork.setGamesPlayed(170 - 7);
 		newYork.setPoints(78);
 
 		Team montreal = new Team();
 		montreal.setName("Montreal");
 		montreal.setGamesPlayed(170 - 7);
 		montreal.setPoints(76);
 
 		Match[] atlantaMatches = new Match[8];
 		Match[] philadelphiaMatches = new Match[4];
 		Match[] newYorkMatches = new Match[7];
 		Match[] montrealMatches = new Match[5];
 		Match[] allMatches = new Match[12];
 
 		int am = 0;
 		int pm = 0;
 		int nym = 0;
 		int mm = 0;
 		int all = 0;
 
 		Match atlVphil = new Match();
 		atlVphil.setHomeTeam(atlanta);
 		atlVphil.setAwayTeam(philadelphia);
 		atlantaMatches[am++] = atlVphil;
 		philadelphiaMatches[pm++] = atlVphil;
 		allMatches[all++] = atlVphil;
 
 		Match atlVny = new Match();
 		atlVny.setHomeTeam(atlanta);
 		atlVny.setAwayTeam(newYork);
 		for (int i = 0; i < 6; i++) {
 			atlantaMatches[am++] = atlVny;
 			newYorkMatches[nym++] = atlVny;
 			allMatches[all++] = atlVny;
 		}
 
 		Match atlVmon = new Match();
 		atlVmon.setHomeTeam(atlanta);
 		atlVmon.setAwayTeam(montreal);
 		atlantaMatches[am++] = atlVmon;
 		montrealMatches[mm++] = atlVmon;
 		allMatches[all++] = atlVmon;
 
 		Match philVmon = new Match();
 		philVmon.setHomeTeam(philadelphia);
 		philVmon.setAwayTeam(montreal);
 		for (int i = 0; i < 3; i++) {
 			philadelphiaMatches[pm++] = philVmon;
 			montrealMatches[mm++] = philVmon;
 			allMatches[all++] = philVmon;
 
 		}
 
 		Match nyVmon = new Match();
 		nyVmon.setHomeTeam(newYork);
 		nyVmon.setAwayTeam(montreal);
 		newYorkMatches[nym++] = nyVmon;
 		montrealMatches[mm++] = nyVmon;
 		allMatches[all++] = nyVmon;
 
 		atlanta.setUpcomingMatches(atlantaMatches);
 		philadelphia.setUpcomingMatches(philadelphiaMatches);
 		newYork.setUpcomingMatches(newYorkMatches);
 		montreal.setUpcomingMatches(montrealMatches);
 
 		Team[] teams = new Team[4];
 		teams[0] = atlanta;
 		teams[1] = philadelphia;
 		teams[2] = newYork;
 		teams[3] = montreal;
 
 		League l = new League(teams, allMatches);
 
 		Graph g = new Graph(l, atlanta);
 		LinkedList<AdjListNode> list = g.getSource().getAdjList();
 		for (AdjListNode n : list) {
 			PairVertex v = (PairVertex) n.getVertex();
			System.out.println("Sink to " + v.getTeamA().getName() + " and "
 					+ v.getTeamB().getName() + " has capacity "
 					+ n.getCapacity());
 			for (AdjListNode m : v.getAdjList()) {
 				TeamVertex w = (TeamVertex) m.getVertex();
 				System.out.println("\t Pair Vertex to " + w.getTeam().getName()
 						+ " has capacity " + n.getCapacity());
 				for (AdjListNode b : w.getAdjList()) {
 					Vertex x = b.getVertex();
 					if (x != g.getSink()) {
 						System.out.println("Not adjacent to sink.");
 					}
 					System.out.println("\t\t Team vertex "
 							+ w.getTeam().getName() + " to sink has capacity "
 							+ b.getCapacity());
 				}
 			}
 		}
 
 	}
 }
