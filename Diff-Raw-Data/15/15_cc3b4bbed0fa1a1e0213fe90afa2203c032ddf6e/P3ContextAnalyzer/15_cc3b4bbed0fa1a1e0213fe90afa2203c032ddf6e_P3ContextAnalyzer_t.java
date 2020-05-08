 package phase3;
 
 import java.util.ArrayList;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import core.Database;
 import core.Player;
 import core.TexasHoldEm;
 
 public class P3ContextAnalyzer {
 
 	private static final double potOddsBins[] = { -0.1, 0.1, 0.2, 0.3 };
 	// player # , gamestate, number of raises, potOddsBin, action , 0 = strength
 	// <-> 1 = numberofobs
 	private static double contextOdds[][][][][][] = new double[10][4][30][4][4][2];
 	private static int bets;
 	private TexasHoldEm game;
 	private ArrayList<int[]> contextQueue;
 
 	public P3ContextAnalyzer(TexasHoldEm game) {
 		this.game = game;
 		contextQueue = new ArrayList<int[]>();
 	}
 
 	public void newBettingRound(ArrayList<Player> players) {
 		bets = 0;
 	}
 
 	public void raise() {
 		bets++;
 	}
 
 	public void event(Player p, TexasHoldEm.Action a) {
 		int event[] = { game.getIndexOfPlayer(p),
 				game.getGameState().getStateNum(), bets,
 				potOddsBin(p.potOdds()), a.getActionNum() };
 		contextQueue.add(event);
 	}
 
 	public void notifyEndOfHand(ArrayList<Player> players) {
 		boolean inShowdown = false;
 		for (int[] e : contextQueue) {
 			Player eventPlayer = null;
 			for (Player p : players) {
 				if (game.getIndexOfPlayer(p) == e[0]) {
 					inShowdown = true;
 					eventPlayer = p;
 					break;
 				}
 			}
 			if (!inShowdown)
 				continue;
 			inShowdown = false;
 
 			double handStrength = 0.0;
 			switch (e[1]) {
 			case 0: // pre-flop
 				int suited = (eventPlayer.isSuited()) ? 0 : 9;
 				int ID = eventPlayer.getHoleID();
 				Database db = new Database();
 				db.connect();
 
 				ResultSet rs = db.query("select hole_strength."
 						+ (suited + game.getNumPlayers() - 1)
 						+ " from hole_strength where ref = " + ID + ";");
 				try {
 					rs.next();
 					handStrength = rs.getDouble(1);// * game.getNumPlayers();
 				} catch (SQLException sql) {
 					// TODO Auto-generated catch block
 					sql.printStackTrace();
 				}
 				db.disconnect();
 				break;
 			case 1: // flop
 			case 2: // turn
 			case 3: // river
 				handStrength = eventPlayer.handStrength(e[1] + 2);
 				break;
 			default:
 				throw new NullPointerException();
 			}
 			// System.out.println("CONTEXTODDS "+ e[0] + " " + e[1] + " " + e[2]
 			// + " " + e[3] + " " + e[4]);
 			contextOdds[e[0]][e[1]][e[2]][e[3]][e[4]][0] += handStrength;
 			contextOdds[e[0]][e[1]][e[2]][e[3]][e[4]][1]++;
 		}
 		contextQueue = new ArrayList<int[]>();
 	}
 
 	/*
 	 * public double getPlayerModel(Player p){ return
 	 * contextOdds[game.getActivePlayersSize
 	 * ()][game.getGameState().getStateNum()][bets][potOddsBin(p.potOdds())][][]
 	 * }
 	 */
 	public double highestAnticipated() {
		System.out.println("HER ER JEG OG!");
 		double highest = 0.0, tmp,divisor;
 		int cx[];
 		for (int i = contextQueue.size() - 1; i > contextQueue.size()
 				- game.getActivePlayersSize() + 1; i--) {
 			// get current contextplayer array from queue of last activities
 			cx = contextQueue.get(i);
 			divisor = contextOdds[cx[0]][cx[1]][cx[2]][cx[3]][cx[4]][1];
 			if (divisor == 0) continue;
 			tmp = contextOdds[cx[0]][cx[1]][cx[2]][cx[3]][cx[4]][0] // accumulated strength
 					/ divisor; // number of accumulations
 			if (tmp > highest) highest = tmp;
 		}
 		System.out.println(highest + " highest");
 		return highest;
 	}
 
 	private int potOddsBin(double potOdds) {
 		// System.out.println("potodds " + potOdds);
 		for (int i = 3; i >= 0; i--)
 			if (potOdds > potOddsBins[i])
 				return i;
 		throw new NullPointerException();
 	}
 }
