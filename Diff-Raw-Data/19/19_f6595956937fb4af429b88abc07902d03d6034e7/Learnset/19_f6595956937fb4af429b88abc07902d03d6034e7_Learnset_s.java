 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 //represents a sequence of moves and levels which a species learns moves at
 public class Learnset {
 	private LevelMove[] levelMoves;
 
 	private static final Learnset[] allLearnsetsRSE;
 	private static final Learnset[] allLearnsetsFRLG;
 	private static final Learnset[] allLearnsetsDP;
 	private static final Learnset[] allLearnsetsPT;
 	private static final Learnset[] allLearnsetsHGSS;
 	private static final Learnset[] allLearnsetsBW1;
 	private static final Learnset[] allLearnsetsBW2;
 
 	public Learnset(LevelMove[] new_levelMoves) {
 		if (new_levelMoves == null) {
 			levelMoves = new LevelMove[0];
 		} else {
 			int n = new_levelMoves.length;
 			levelMoves = new LevelMove[n];
 			System.arraycopy(new_levelMoves, 0, levelMoves, 0, n);
 		}
 	}
 
 	public Learnset() {
 		levelMoves = new LevelMove[0];
 	}
 
 	// get species #i's learnset,
 	public static Learnset getLearnset(int i, Game game) {
 		Learnset[] chosenLearnset = null;
 		switch (game) {
 		case RUBY:
 		case SAPPHIRE:
 		case EMERALD:
 			chosenLearnset = allLearnsetsRSE;
 			break;
 		case FIRERED:
 		case LEAFGREEN:
 			chosenLearnset = allLearnsetsFRLG;
 			break;
 		case DIAMOND:
 		case PEARL:
 			chosenLearnset = allLearnsetsDP;
 			break;
 		case PLATINUM:
 			chosenLearnset = allLearnsetsPT;
 			break;
 		case HEARTGOLD:
 		case SOULSILVER:
 			chosenLearnset = allLearnsetsHGSS;
 			break;
 		case BLACK:
 		case WHITE:
 			chosenLearnset = allLearnsetsBW1;
 			break;
 		case BLACK2:
 		case WHITE2:
 			chosenLearnset = allLearnsetsBW2;
 			break;
 		}
 		if(chosenLearnset==null) {
 			return null;
 		}
 		else {
 			if (i < 0 || i >= chosenLearnset.length)
 				return null;
 			else
 				return chosenLearnset[i];
 		}
 	}
 
 	static {
 		// cheat to get the right movedata for the right learnsets
 		Settings.game = Game.EMERALD;
 		allLearnsetsRSE = getData("moveset_rse.txt");
 		allLearnsetsFRLG = getData("moveset_frlg.txt");
 		Settings.game = Game.HEARTGOLD;
 		allLearnsetsDP = getData("moveset_dp.txt");
 		allLearnsetsPT = getData("moveset_pt.txt");
 		allLearnsetsHGSS = getData("moveset_hgss.txt");
 		Settings.game = Game.BLACK2;
 		allLearnsetsBW1 = getData("moveset_bw1.txt");
 		allLearnsetsBW2 = getData("moveset_bw2.txt");
 	}
 
 	private static Learnset[] getData(String filename) {
 		try {
 			BufferedReader in = new BufferedReader(new InputStreamReader(
 					System.class.getResource("/resources/" + filename)
 							.openStream()));
 			String text = in.readLine();
 			int n = Integer.parseInt(text);
 			Learnset[] output = new Learnset[n + 1];
 			output[0] = null;
 			Learnset l;
 			LevelMove[] lms;
 			for (int i = 1; i <= n; i++) {
 				String[] moves = in.readLine().split("\\s+");
 				int k = moves.length / 2;
 				lms = new LevelMove[k];
 				for (int j = 0; j < k; j++) {
 					int lvl = Integer.parseInt(moves[2 * j]);
 					Move move = Move
 							.getMove(Integer.parseInt(moves[2 * j + 1]));
 					lms[j] = new LevelMove(lvl, move);
 				}
 				l = new Learnset(lms);
 				output[i] = l;
 			}
 			in.close();
 			return output;
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	public LevelMove[] getLevelMoves() {
 		return levelMoves.clone();
 	}
 
 	public String toString() {
 		String output = "";
 		for (LevelMove lm : levelMoves) {
 			output += " " + lm.toString(); // string buffers are for noobs
 		}
 		return output;
 	}
 }
