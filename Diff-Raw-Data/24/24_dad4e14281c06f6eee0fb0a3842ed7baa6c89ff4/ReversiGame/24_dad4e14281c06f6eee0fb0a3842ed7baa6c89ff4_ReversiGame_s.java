 package reversi.game;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Set;
 
 import reversi.game.board.ReversiMoveFinder;
 import reversi.game.controller.ReversiBoardController;
 import reversi.game.controller.ReversiControllerSet;
 import reversi.game.controller.ReversiDisplayController;
 import reversi.game.controller.ReversiInputController;
 import reversi.game.controller.ReversiPlayerController;
 import reversi.game.controller.ReversiTurnController;
 import reversi.models.ReversiBoard;
 import reversi.models.ReversiPlayer;
 import reversi.models.game.ReversiInput;
 import reversi.server.ReversiServerResponse;
 import reversi.server.commands.ReversiCommand.ReversiCommandType;
 import base.game.messages.MessageHandler;
 import base.models.Position;
 import base.models.game.Turn;
 
 public class ReversiGame {
 
 	private class ReversiGameContext {
 		private Integer turnId;
 		private final ReversiBoard currentBoard;
 		private final List<ReversiPlayer> activePlayers;
 
 		public ReversiGameContext(ReversiBoard currentBoard, List<ReversiPlayer> activePlayers) {
 			this.turnId = 0;
 			this.currentBoard = currentBoard;
 			this.activePlayers = activePlayers;
 		}
 
 		public Integer getTurnId() {
 			return turnId;
 		}
 
 		public void increaseTurnId() {
 			this.turnId += 1;
 		}
 
 		public ReversiBoard getCurrentBoard() {
 			return currentBoard;
 		}
 
 		public List<ReversiPlayer> getActivePlayers() {
 			return activePlayers;
 		}
 	};
 
 	protected final ReversiControllerSet controllerSet;
 	protected ReversiGameContext context;
 
 	public ReversiGame(ReversiControllerSet controllerSet) {
 		this.controllerSet = controllerSet;
 		this.context = null;
 	}
 
 	public void resetBoard() {
 		ReversiBoardController boardController = controllerSet.getBoardController();
 		ReversiPlayerController playerController = controllerSet.getPlayerController();
 
 		ReversiBoard board = boardController.generateNewBoard();
 		List<ReversiPlayer> activePlayers = playerController.getPlaying();
 		board.setInitialPieces(activePlayers);
 
 		this.context = new ReversiGameContext(board, activePlayers);
 	}
 	
 	public Integer getFirstPlayerIndex() {
 		List<ReversiPlayer> activePlayers = this.context.getActivePlayers();
 		Integer firstPlayerIndex = 0;
 		
 		for(int i = 0; i < activePlayers.size(); i++) {
			ReversiPlayer player = activePlayers.get(0);
 			String asciiPiece = player.getAsciiDisplayPiece();
 			
 			if(asciiPiece.equals(ReversiBoard.blackReversiPiece)){
 				firstPlayerIndex = i;
 				break;
 			}
 		}
 		
 		return firstPlayerIndex;
 	}
 
 	public void playGame() throws IOException {
 		this.resetBoard();
 
 		MessageHandler messageHandler = this.controllerSet.getMessageHandler();
 
 		List<ReversiPlayer> activePlayers = this.context.getActivePlayers();
 		Integer playerCount = activePlayers.size();
 		Integer firstPlayerOffset = this.getFirstPlayerIndex();
 
 		boolean hasWinner = false;
 		ReversiBoard board = this.context.currentBoard;
 
 		boolean previousPlayerSkippedTurn = false;
 		while (hasWinner == false) {
 
 			Integer currentTurn = this.context.getTurnId();			
 			this.redrawBoard();
 
 			ReversiPlayer currentPlayer = activePlayers.get((currentTurn + firstPlayerOffset) % playerCount);
 			// ReversiPlayer nextPlayer = players.get((currentTurn + 1) %
 			// playerCount);
 
 			boolean playerSkippedTurn = false;
 			boolean success = false;
 			while (!success) {
 				ReversiMoveFinder finder = new ReversiMoveFinder(board, currentPlayer);
 				Set<Position> availableMoves = finder.findMoves();
 
 				if (availableMoves.size() > 0) {
 					String formattedMovesString = String.format("Available Moves: %s", availableMoves.toString());
 					ReversiServerResponse.sendComment(currentPlayer, formattedMovesString);
 					success = this.runPlayerTurn(currentPlayer, availableMoves);
 				} else {
 					ReversiServerResponse.sendComment(currentPlayer, "No moves are available.");
 					success = true;
 					playerSkippedTurn = true;
 				}
 			}
 
 			if (playerSkippedTurn) {
 				if (previousPlayerSkippedTurn == false) {
 					previousPlayerSkippedTurn = true;
 					messageHandler.writeMessage(currentPlayer + " cannot make a move!");
 				} else {
 					hasWinner = true;
 				}
 			} else {
 				previousPlayerSkippedTurn = false;
 			}
 
 			this.context.increaseTurnId();
 		} // End Has Winner Loop
 
 		ReversiPlayerController playerController = controllerSet.getPlayerController();
 		ReversiPlayer winner = playerController.determineWinner(board);
 		messageHandler.writeMessage(winner + " wins!");
 	}
 
 	private void redrawBoard() throws IOException {
 		ReversiPlayerController playerController = controllerSet.getPlayerController();
 		ReversiDisplayController displayController = controllerSet.getDisplayController();
 
 		ReversiBoard board = this.context.getCurrentBoard();
 		List<ReversiPlayer> allPlayers = playerController.getAllPlayers();
 
 		playerController.updateScore(board);
 		displayController.drawBoard(board, allPlayers);
 	}
 
 	public boolean runPlayerTurn(ReversiPlayer currentPlayer, Set<Position> availableMoves) throws IOException {
 		boolean success = false;
 
 		ReversiInputController inputController = controllerSet.getInputController();
 		ReversiTurnController turnController = controllerSet.getTurnController();
 		MessageHandler messageHandler = this.controllerSet.getMessageHandler();
 		List<ReversiPlayer> activePlayers = this.context.getActivePlayers();
 		
 		Integer turnId = this.context.getTurnId();
 
 		ReversiBoard board = this.context.getCurrentBoard();
 
 		ReversiInput input = inputController.getInputForPlayer(currentPlayer, board);
 
 		// Process Undo/Redo commands here.
 		if (input.isCommand()) {
 			ReversiCommandType type = input.getCommand().getType();
 
 			switch (type) {
 			case Undo: {
 				success = turnController.undoTurn(activePlayers, board);
 			}
 				break;
 			case Redo: {
 				success = turnController.redoTurn(activePlayers, board);
 			}
 				break;
 			default: {
 				success = false;
 			}
 				break;
 			}
 		} else {
 
 			// Process turns from here.
 			Turn<ReversiPlayer, ReversiInput> turn = new Turn<ReversiPlayer, ReversiInput>(turnId);
 			turn.addInput(input);
 			success = turnController.processTurn(turn, board);
 		}
 
 		if (currentPlayer.isHuman()) {
 			if (success == false) {
 				ReversiServerResponse.sendIllegal(currentPlayer);
 			} else {
 				ReversiServerResponse.sendOk(currentPlayer);
 			}
 		} else {
 			messageHandler.writeUnformattedMessage(input.toString());
 		}
 		
 		return success;
 	}
 	
 	@Deprecated
 	public void printMoveInfo() {
 		/*
 		 * // System.out.print("Allowed to play at [" + allowed.size() +"] ");
 		 * 
 		 * System.out.println(availableMoves.toString()); }
 		 * 
 		 * // System.out.println(); // System.out.println("Current player is " +
 		 * currentPlayer.getName());
 		 */
 	}
 }
