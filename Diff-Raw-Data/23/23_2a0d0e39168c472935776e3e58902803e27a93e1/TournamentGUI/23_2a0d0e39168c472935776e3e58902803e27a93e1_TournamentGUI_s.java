 package chessGUI;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.util.Hashtable;
 
 import javax.swing.JFrame;
 import javax.swing.JTextArea;
 
 import chessAI.AI;
 import chessBackend.Board;
 import chessBackend.Game;
 import chessBackend.GameResults;
 import chessBackend.Player;
 import chessBackend.Side;
 import chessEthernet.EthernetPlayerClient;
 import chessIO.FileIO;
 import chessIO.XMLParser;
 
 public class TournamentGUI {
 	JFrame frame;
 	JTextArea statusTxt;
 
 	public static void main(String[] args) {
 
 		boolean debug = true;
 		Game game;
 		GameResults results;
 
 		int draws = 0;
 
 		TournamentGUI tournamentGui = new TournamentGUI();
 
 		ObserverGUI observer = new ObserverGUI(null, false);
 
 		// Game game = new Game(GameType.AI_VS_AI);
 
 		Player playerOne = new AI(null, debug);
 		// Player playerTwo = new AI(null, debug);
 
 		Player playerTwo = new EthernetPlayerClient();
 
 		String playerOneVersion = playerOne.getVersion();
 		String playerTwoVersion = playerTwo.getVersion();
 		
 		System.out.println("Player One: " + playerOneVersion);
 		System.out.println("Player Two: " + playerTwoVersion);
 
 		((AI) playerOne).setUseBook(true);
 
		Board defaultBoard = XMLParser.XMLToBoard(FileIO.readFile("default.xml"));
 		System.out.println(defaultBoard.toString());
 
 		Hashtable<Side, Player> players = new Hashtable<Side, Player>();
 
 		players.put(Side.WHITE, playerOne);
 		players.put(Side.BLACK, playerTwo);
 
 		Hashtable<Player, String> playerNames = new Hashtable<Player, String>();
 
 		playerNames.put(playerOne, playerOneVersion);
 		playerNames.put(playerTwo, playerTwoVersion);
 
 		Hashtable<String, Long[]> playerScore = new Hashtable<String, Long[]>();
 
 		// blackwins, whitewins ,winby,numMoves,time,maxTime
 		Long[] temp1 = { 0L, 0L, 0L, 0L, 0L, 0L };
 		Long[] temp2 = { 0L, 0L, 0L, 0L, 0L, 0L };
 		playerScore.put(playerNames.get(playerOne), temp1);
 		playerScore.put(playerNames.get(playerTwo), temp2);
 
 		game = new Game(players);
 
 		playerOne.setGame(game);
 		playerTwo.setGame(game);
 		game.addObserver(observer);
 
 		Long[] winnerScore;
 		Long[] loserScore;
 		int numOfGames = 1000;
 		for (int i = 0; i < numOfGames; i++) {
 
 			System.out.println("Game#" + i);
 			results = game.newGame(defaultBoard, true);
 
 			if (results.getWinner() != Side.NONE) {
 
 				winnerScore = playerScore.get(playerNames.get(players.get(results.getWinner())));
 				winnerScore[results.getWinner().ordinal()]++;
 				winnerScore[2] += results.getWinBy();
 				winnerScore[3] += results.getNumOfMoves() / 2;
 				winnerScore[4] += results.getTime(results.getWinner());
 				if (winnerScore[5] < results.getMaxTime(results.getWinner())) {
 					winnerScore[5] = results.getMaxTime(results.getWinner());
 				}
 
 				loserScore = playerScore.get(playerNames.get(players.get(results.getWinner().otherSide())));
 				loserScore[3] += results.getNumOfMoves() / 2;
 				loserScore[4] += results.getTime(results.getWinner().otherSide());
 				if (loserScore[5] < results.getMaxTime(results.getWinner().otherSide())) {
 					loserScore[5] = results.getMaxTime(results.getWinner().otherSide());
 				}
 			} else {
 				draws++;
 			}
 
 			String playerOneName = playerNames.get(playerOne);
 			Long[] playerOneScore = playerScore.get(playerOneName);
 
 			int totalWins1 = (int) (playerOneScore[0] + playerOneScore[1]);
 			double avgWinby1 = (double) playerOneScore[2] / ((double) totalWins1);
 			double avgTimePerMove1 = (double) playerOneScore[4] / (double) (playerOneScore[3]);
 
 			String out1 = "Player: " + playerOneName + "\n";
 			out1 += "Total wins: " + totalWins1 + " (" + playerOneScore[Side.BLACK.ordinal()] + " black, " + playerOneScore[Side.WHITE.ordinal()]
 					+ " white)\n";
 			out1 += "Average pts won by: " + avgWinby1 + "\n";
 			out1 += "Average time per move: " + avgTimePerMove1 + "\n";
 			out1 += "Max time on move: " + playerOneScore[5] + "\n";
 			
 			if(playerOne instanceof AI){
 				out1+= "MaxHashSize = " + ((AI)playerOne).getMaxHashSize() + "\n";
 			}
 
 			String playerTwoName = playerNames.get(playerTwo);
 			Long[] playerTwoScore = playerScore.get(playerTwoName);
 
 			int totalWins2 = (int) (playerTwoScore[0] + playerTwoScore[1]);
 			double avgWinby2 = (double) playerTwoScore[2] / ((double) totalWins2);
 			double avgTimePerMove2 = (double) playerTwoScore[4] / (double) (playerTwoScore[3]);
 
 			String out2 = "\nPlayer: " + playerTwoName + "\n";
 			out2 += "Total wins: " + totalWins2 + " (" + playerTwoScore[Side.BLACK.ordinal()] + " black, " + playerTwoScore[Side.WHITE.ordinal()]
 					+ " white)\n";
 			out2 += "Average pts won by: " + avgWinby2 + "\n";
 			out2 += "Average time per move: " + avgTimePerMove2 + "\n";
 			out2 += "Max time on move: " + playerTwoScore[5] + "\n";
 			
 			if(playerTwo instanceof AI){
 				out2+= "MaxHashSize = " + ((AI)playerTwo).getMaxHashSize() + "\n";
 			}
 
 			tournamentGui.setStatusTxt(out1 + out2 + "Draws: " + draws + "\nGames played: " + (i+1) + "/" + numOfGames + " - " + (numOfGames - i) + " left");
 
 			Player whitePlayer = players.get(Side.WHITE);
 			players.put(Side.WHITE, players.get(Side.BLACK));
 			players.put(Side.BLACK, whitePlayer);
 
 		}
 
 		FileIO.log("Tournament done");
 	}
 
 	public TournamentGUI() {
 
 		frame = new JFrame("Tournament");
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setLayout(new BorderLayout());
 
 		frame.setVisible(true);
 
 		frame.setPreferredSize(new Dimension(500, 500));
 		statusTxt = new JTextArea();
 
 		// statusTxt.setFont(new Font("Courier New", Font.ITALIC, 45));
 		// statusTxt.setHorizontalAlignment(SwingConstants.CENTER);
 
 		frame.add(statusTxt, BorderLayout.CENTER);
 		frame.pack();
 	}
 
 	public void setStatusTxt(String stats) {
 		statusTxt.setText(stats);
 	}
 
 }
