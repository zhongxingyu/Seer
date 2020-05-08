 package pieces;
 
 import java.awt.Color;
 import java.rmi.NotBoundException;
 import java.rmi.RemoteException;
 import java.rmi.server.UnicastRemoteObject;
 
 import pieces.gui.GUI;
 import pieces.gui.Message;
 import pieces.network.IClient;
 import pieces.network.IServer.ServerFullException;
 import pieces.network.Network;
 import pieces.utils.Config;
 import pieces.utils.AI;
 import pieces.utils.Matrix;
 import pieces.utils.Player;
 import pieces.utils.Matrix.MatrixPiece;
 
 public class Game extends UnicastRemoteObject implements IClient {
 	private static final long serialVersionUID = 1L;
 
 	public static final String APP_NAME = "Pieces";
 	public static final String APP_VERSION = "1.0-dev";
 
 	private final int RANGE = 8;
 	private final int DELTA = 500; // MS between each AI round.
 
 	public Player playerBlack;
 	public Player playerWhite;
 	private Matrix matrix;
 	private GUI gui;
 	private Thread threadAI;
 	private boolean round;
 	private boolean multiplayer;
 	private boolean player;
 	private boolean gameover;
 
 	public Game() throws RemoteException {
 		// Matrix.
 		matrix = new Matrix(RANGE);
 
 		// Players.
 		playerBlack = new Player(Player.TYPE_HUMAIN);
 		playerWhite = new Player(Player.TYPE_AI1);
 
 		// Graphical User Interface.
 		gui = new GUI(this);
 
 		// Launches a new game.
 		newGame();
 	}
 
 	public boolean canPlay() {
 		if (multiplayer) {
 			try {
 				return round == player && Network.canPlay();
 			} catch (RemoteException e) {
 				e.printStackTrace();
 			}
 			return false;
 		} else {
 			if (playerWhite.isAI() && round != Player.BLACK)
 				return false;
 			if (playerBlack.isAI() && round != Player.WHITE)
 				return false;
 			return true;
 		}
 	}
 
 	public void changeRound() {
 		// Change round.
 		round = !round;
 
 		// Terminates the game if matrix is full.
 		if (matrix.isFull()) {
 			end(true);
 			return;
 		}
 
 		/**
 		 * FIX ME
 		 */
 		// Search moves for the next player.
 		// If no move was found, pass.
 		if (searchMoves() == 0) {
 			round = !round; // Pass to the next round. This may be dirty.
 			// Check if the other player cannot play too.
 			if (searchMoves() == 0) {
 				end(false);
 				return;
 			}
 			round = !round; // Back to the previous round.
 			changeRound();
 		}
 
 		gui.changeRound();
 
 		// Let the AI plays if needed.
 		if (!multiplayer)
 			runAI(round);
 	}
 
 	public void end(boolean animation) {
 		// Prevents another round.
 		gameover = true;
 		matrix.clearMoves();
 
 		// Shows who's the winner.
 		int blackScore = matrix.score(MatrixPiece.BLACK);
 		int whiteScore = matrix.score(MatrixPiece.WHITE);
 
 		if (blackScore == whiteScore)
 			message("Draw.", Message.WARNING, true, false);
 		else
 			message((blackScore > whiteScore ? "Blacks" : "Whites") + " won !", Message.OK, true, false);
 
 		// Regroup all each piece if needed.
 		if (animation)
 			gui.endingAnimation();
 
 	}
 
 	public Matrix getMatrix() {
 		return matrix;
 	}
 
 	@Override
 	public void handshake() {
 		message("Opponent connected !", Message.OK, false, true);
 		gui.revalidate();
 		gui.repaint();
 	}
 
 	public boolean isHost() {
 		return player;
 	}
 
 	public boolean isMultiplayer() {
 		return multiplayer;
 	}
 
 	public void login() {
 		// Tell the player to wait.
 		message("Awaiting connection...", Message.WARNING, true, true);
 
 		// Try to reach the remote game.
 		try {
 			Network.login(this);
 		} catch (ServerFullException e) {
 			message("This host has already two players.", Message.ERROR, false, true);
 			e.printStackTrace();
 		} catch (RemoteException | NotBoundException e) {
 			message("Cannot join this game.", Message.ERROR, false, true);
 			e.printStackTrace();
 		}
 
 		// If ok, create a new game.
 		newGame();
 		gui.multiplayer();
 		round = false;
 		multiplayer = true;
 		player = Player.WHITE; // Client plays as white.
 
 		// Notify the player.
 		message("Connection established !", Message.OK, false, true);
 	}
 
 	@Override
 	public void logout() {
 		multiplayer = false;
 		gui.multiplayer();
 		try {
 			Network.logout(player);
 			newGame();
 			message("La partie a été interrompue.", Message.WARNING, false, true);
 		} catch (RemoteException e) {
 			message("Une erreur est survenue lors de l'arrêt de la partie.", Message.ERROR, false, true);
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void message(String texte, Color couleur, boolean persistant, boolean urgent) {
 		if (gui != null)
 			gui.message(texte, couleur, persistant, urgent);
 	}
 
 	public void multiplayer() {
 		updatePlayers();
 		try {
 			Network.createServer(this);
 			newGame();
 			multiplayer = true;
 			round = false;
 			player = Player.BLACK;
 			gui.multiplayer();
 			message("Une partie multijoueur a été lancée !", Message.OK, false, true);
 			message("Attente d'un autre joueur...", Message.NORMAL, true, false);
 		} catch (Exception e) {
 			message("Impossible de créer une partie en ligne. Voir la console pour plus d'informations", Message.ERROR, false, false);
 			e.printStackTrace();
 		}
 	}
 
 	public void newGame() {
 		if (multiplayer)
 			logout();
 
 		stopAI();
 
 		gameover = false;
 		round = false;
 		playerBlack.reset();
 		playerWhite.reset();
 
 		matrix.reset();
 		matrix.set(3, 3, MatrixPiece.WHITE);
 		matrix.set(4, 3, MatrixPiece.BLACK);
 		matrix.set(3, 4, MatrixPiece.BLACK);
 		matrix.set(4, 4, MatrixPiece.WHITE);
 
 		gui.newGame();
 
 		matrix.searchMoves(MatrixPiece.BLACK);
 
 		if (multiplayer)
 			return;
 		updatePlayers();
 	}
 
 	@Override
 	public void play(int piece) {
 		int type = round() ? MatrixPiece.WHITE : MatrixPiece.BLACK;
 
 		if (multiplayer) {
 			try {
 				// If it's our turn, notify the server about our game.
 				if (canPlay())
 					Network.putPiece(piece);
 				else {
 					matrix.set(piece, type); // Need by gui.play() but
 												// matrix.play() do it again
 												// later... Meh.
 					gui.play(piece);
 				}
 			} catch (RemoteException e) {
				message("Erreur lors de la transmission du coup.", Message.ERROR, false, true);
 				e.printStackTrace();
 			}
 		}
 
 		matrix.play(piece, type);
 		changeRound();
 	}
 
 	private void runAI(final boolean player) {
 		// If the AI is already playing, interrupt.
 		stopAI();
 
 		final Player playerAI = player ? playerWhite : playerBlack;
 
 		// Exit if current player is human.
 		if (!playerAI.isAI())
 			return;
 
 		// Plays.
 		final int delta = DELTA * (Config.get(Config.AI_SPEED, 0) + 1);
 		threadAI = new Thread(new Runnable() {
 			@Override
 			public void run() {
 				try {
 					Thread.sleep(delta);
 					// Plays if possible.
 					if (matrix.getAvailableMoves().size() > 0) {
 						int index = AI.play(matrix, playerAI.getType() - 1);
						matrix.set(index, playerAI.getType());
 						gui.play(index);
 						play(index);
 					} else {
 						changeRound();
 					}
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 		});
 		threadAI.start();
 	}
 
 	private void stopAI() {
 		// If thread doesn't exist, exit.
 		if (threadAI == null)
 			return;
 
 		// Else, interrupt it.
 		if (threadAI.isAlive())
 			threadAI.interrupt();
 	}
 
 	public boolean round() {
 		return round;
 	}
 
 	public int searchMoves() {
 		matrix.searchMoves(round ? MatrixPiece.WHITE : MatrixPiece.BLACK);
 		gui.repaint();
 		return matrix.getAvailableMoves().size();
 	}
 
 	public void updatePlayers() {
 		playerBlack.setType(Config.get(Config.DIFFICULTY_BLACK, Player.TYPE_HUMAIN));
 		playerWhite.setType(Config.get(Config.DIFFICULTY_WHITE, Player.TYPE_AI1));
 		gui.repaint();
 		if (!gameover)
 			runAI(round);
 	}
 
 	public void updateScores() {
 		gui.getScores().update();
 	}
 }
