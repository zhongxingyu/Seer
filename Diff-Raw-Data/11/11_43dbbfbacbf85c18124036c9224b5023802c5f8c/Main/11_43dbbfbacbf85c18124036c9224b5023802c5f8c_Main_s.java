 package uk.ac.gla.dcs.tp3.w.algorithm;
 
 import java.util.ArrayList;
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
 
 		ArrayList<Match> atlantaMatches = new ArrayList<Match>();
 		ArrayList<Match> philadelphiaMatches = new ArrayList<Match>();
 		ArrayList<Match> newYorkMatches = new ArrayList<Match>();
 		ArrayList<Match> montrealMatches = new ArrayList<Match>();
 		ArrayList<Match> allMatches = new ArrayList<Match>();
 
 		Match atlVphil = new Match();
 		atlVphil.setHomeTeam(atlanta);
 		atlVphil.setAwayTeam(philadelphia);
 		atlantaMatches.add(atlVphil);
 		philadelphiaMatches.add(atlVphil);
 		allMatches.add(atlVphil);
 
 		Match atlVny = new Match();
 		atlVny.setHomeTeam(atlanta);
 		atlVny.setAwayTeam(newYork);
 		for (int i = 0; i < 6; i++) {
 			atlantaMatches.add(atlVny);
 			newYorkMatches.add(atlVny);
 			allMatches.add(atlVny);
 		}
 
 		Match atlVmon = new Match();
 		atlVmon.setHomeTeam(atlanta);
 		atlVmon.setAwayTeam(montreal);
 		atlantaMatches.add(atlVmon);
 		montrealMatches.add(atlVmon);
 		allMatches.add(atlVmon);
 
 		Match philVmon = new Match();
 		philVmon.setHomeTeam(philadelphia);
 		philVmon.setAwayTeam(montreal);
 		for (int i = 0; i < 3; i++) {
 			philadelphiaMatches.add(philVmon);
 			montrealMatches.add(philVmon);
 			allMatches.add(philVmon);
 
 		}
 
 		Match nyVmon = new Match();
 		nyVmon.setHomeTeam(newYork);
 		nyVmon.setAwayTeam(montreal);
 		newYorkMatches.add(nyVmon);
 		montrealMatches.add(nyVmon);
 		allMatches.add(nyVmon);
 
 		atlanta.setUpcomingMatches(atlantaMatches);
 		philadelphia.setUpcomingMatches(philadelphiaMatches);
 		newYork.setUpcomingMatches(newYorkMatches);
 		montreal.setUpcomingMatches(montrealMatches);
 
 		ArrayList<Team> teams = new ArrayList<Team>();
 		teams.add(atlanta);
 		teams.add(philadelphia);
 		teams.add(newYork);
 		teams.add(montreal);
 
 		Division d = new Division(teams, allMatches);
 
 		Graph g = new Graph(d, atlanta);
 		System.out.println("Initial graph output from here.\n\n\n");
 		LinkedList<AdjListNode> list = g.getSource().getAdjList();
 		for (AdjListNode n : list) {
 			PairVertex v = (PairVertex) n.getVertex();
 			System.out.println("Source to " + v.getTeamA().getName() + " and "
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
 		System.out.println("\n\n\nBFS output from here.\n\n\n");
 		g.bfs();
 		for (Vertex v : g.getV()) {
 			Vertex pred = g.getV()[v.getPredecessor()];
 			if (v instanceof TeamVertex) {
 				TeamVertex tv = (TeamVertex) v;
 				System.out.print("\"" + tv.getTeam().getName()
 						+ "\" has predecessor ");
 			} else if (v instanceof PairVertex) {
 				PairVertex pv = (PairVertex) v;
 				System.out.print("\"" + pv.getTeamA().getName() + " and "
 						+ pv.getTeamB().getName() + "\" has predecessor ");
 			} else {
 				System.out.print("\"" + v.getIndex() + "\" has predecessor ");
 			}
 			if (pred instanceof TeamVertex) {
 				TeamVertex tv = (TeamVertex) pred;
 				System.out.print("\"" + tv.getTeam().getName() + "\"");
 			} else if (pred instanceof PairVertex) {
 				PairVertex pv = (PairVertex) pred;
 				System.out.print("\"" + pv.getTeamA().getName() + " and "
 						+ pv.getTeamB().getName() + "\"");
 			} else {
 				System.out.print("\"" + pred.getIndex() + "\"");
 			}
 			System.out.println();
 		}
 		System.out.println("\n\n\nResidual graph output from here.\n\n\n");
 		ResidualGraph rG = new ResidualGraph(g);
 		for (Vertex v : rG.getV()) {
 			list = v.getAdjList();
 			System.out.println(list.size()
 					+ " nodes in adjacency list for vertex " + v.getIndex());
 		}
 		list = rG.getV()[0].getAdjList();
 		for (Vertex v : rG.getV()) {
 			list = v.getAdjList();
 			for (AdjListNode n : list) {
 				System.out.println("Vertex " + v.getIndex()
 						+ " is adjacent to: " + n.getVertex().getIndex()
 						+ " with capacity " + n.getCapacity() + " and flow "
 						+ n.getFlow());
 			}
 		}
 		
 		System.out.println("\n\n\nInitial graph output from here.\n\n\n");
 		list = g.getSource().getAdjList();
 		for (AdjListNode n : list) {
 			PairVertex v = (PairVertex) n.getVertex();
 			System.out.println("Source to " + v.getTeamA().getName() + " and "
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
 		
 		Algorithm test = new Algorithm(d);
		boolean lol = test.isEliminated(newYork);
		if(lol) {
			System.out.println("Eliminated");
		} else {
			System.out.println("Not Eliminated");
 		}
 	}
 }
