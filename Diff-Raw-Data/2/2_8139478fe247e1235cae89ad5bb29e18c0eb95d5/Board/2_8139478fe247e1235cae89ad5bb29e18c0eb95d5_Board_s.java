 package ErraiLearning.client.local;
 
 import javax.annotation.PostConstruct;
 import javax.inject.Inject;
 
 import org.jboss.errai.bus.client.api.base.MessageBuilder;
 import org.jboss.errai.bus.client.api.messaging.Message;
 import org.jboss.errai.bus.client.api.messaging.MessageBus;
 import org.jboss.errai.bus.client.api.messaging.MessageCallback;
 import org.jboss.errai.bus.client.api.messaging.RequestDispatcher;
 import org.jboss.errai.common.client.protocols.MessageParts;
 import org.jboss.errai.ui.nav.client.local.Page;
 
 import ErraiLearning.client.shared.Game;
 import ErraiLearning.client.shared.InvalidMoveException;
 import ErraiLearning.client.shared.Move;
 import ErraiLearning.client.shared.Player;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 /*
  * An Errai Navigation Page providing the UI for a tic-tac-toe game.
  */
 @Page
 public class Board extends Composite {
 	
 	/*
 	 * A class for controlling the board based on remote-triggered events.
 	 */
 	public class GameMessageCallback implements MessageCallback {
 
 		/*
 		 * (non-Javadoc)
 		 * @see org.jboss.errai.bus.client.api.messaging.MessageCallback#callback(org.jboss.errai.bus.client.api.messaging.Message)
 		 */
 		@Override
 		public void callback(Message message) {
 			// When a normal move is broadcasted from the server.
 			if ("validate-move".equals(message.getCommandType())) {
 				validateMoveCallback(message);
 			// When a game-ending move is broadcasted from the server.
 			} else if ("game-over".equals(message.getCommandType())) {
 				validateMoveCallback(message);
 				if (game.isOver())
 					endGame();
 				else {
 					//TODO: Error handling. State difference between client and server.
 				}
 			}
 		}
 
 		/*
 		 * Handle move broadcasted from server. If this client made the move, ignore it.
 		 * Otherwise, check if the move has been validated by the server and alter the board state.
 		 * 
 		 * This method should only be invoked by the Errai MessageBus.
 		 */
 		private void validateMoveCallback(Message message) {
 			Move move = message.get(Move.class, MessageParts.Value);
 			
 			if (move.getPlayerId() == TTTClient.getInstance().getPlayer().getId()) {
 				if (move.isValidated()) {
 					if (!game.validateLastMove(move)) {
 						//TODO: Rollback move. Possibly resync game state with server.
 					}
 				} else {
 					//TODO: Show message to user that move was rejected.
 					// Possibly try and resolve with server.
 				}
 			} else {
 				if (move.isValidated()) {
 					try {
 						game.makeMove(move.getPlayerId(), move.getRow(), move.getCol());
 						if (!game.validateLastMove(move)) {
 							//TODO: Error handling.
 						}
 
 						if (game.getPlayerX().getId() == move.getPlayerId())
 							setTileToX(move.getRow(), move.getCol());
 						else
 							setTileToO(move.getRow(), move.getCol());
 					} catch (InvalidMoveException e) {
 						e.printStackTrace();
 						//TODO: Show message to user/try to resolve error.
 					}
 				} // If !move.isValidated() then we may ignore this.
 			}
 		}
 
 	}
 
 	/*
	 * A tic-tac-toe tile.
 	 */
 	public class TileClickEvent implements ClickHandler {
 
 		/*
 		 * (non-Javadoc)
 		 * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
 		 */
 		@Override
 		public void onClick(ClickEvent event) {
 			// For debugging.
 			System.out.println(TTTClient.getInstance().getNickname()+": ClickHandler activated.");
 
 			Player activePlayer = TTTClient.getInstance().getPlayer();
 			Tile clickedTile = ((Tile) event.getSource());
 			
 			// Try to make move in model and catch if invalid.
 			try {
 				game.makeMove(activePlayer.getId(), clickedTile.getRow(), clickedTile.getColumn());
 				// For debugging.
 				System.out.println(TTTClient.getInstance().getNickname()+": Move was valid.");
 			} catch (InvalidMoveException e) {
 				// For debugging.
 				System.out.println(TTTClient.getInstance().getNickname()+": Move was invalid.");
 				//TODO: Add friendly error message for user in GUI.
 				return;
 			}
 			
 			// Display move on this client's page.
 			if (game.getPlayerX().equals(activePlayer))
 				setTileToX(clickedTile.getRow(), clickedTile.getColumn());
 			else
 				setTileToO(clickedTile.getRow(), clickedTile.getColumn());
 			
 			// For debugging.
 			System.out.println(TTTClient.getInstance().getNickname()+": Tile changed.");
 			
 			// Send message to server (to be relayed to other client).
 			MessageBuilder.createMessage()
 			.toSubject("Relay")
 			.command("publish-move")
 			.withValue(game.getLastMove())
 			.noErrorHandling()
 			.sendNowWith(dispatcher);
 			
 			// For debugging.
 			System.out.println(TTTClient.getInstance().getNickname()+": Move sent.");
 		}
 	}
 
 	/* The CSS class name to be assigned to all tiles. */
 	public static final String primaryTileStyle = "tile";
 	/* The CSS class name to be assigned to all vacant tiles. */
 	public static final String unusedTileStyle = "unused-tile";
 	/* The CSS class name to be assigned to all tiles used by player X. */
 	public static final String xTileStyle = "x-tile";
 	/* The CSS class name to be assigned to all tiles used by player O. */
 	public static final String oTileStyle = "o-tile";
 	
 	/* The model for this board. */
 	private Game game = null;
 	
 	/* The base GUI element for displaying this board. */
 	private VerticalPanel boardPanel = new VerticalPanel();
 	
 	/* For sending messages to the server. */
 	@Inject private RequestDispatcher dispatcher;
 	/* For receiving messages from the server. */
 	@Inject private MessageBus messageBus;
 	
 	/*
 	 * This class is the UI for a tic-tac-toe board.
 	 */
 	public Board() {
 		String nickname = TTTClient.getInstance().getNickname();
 		// For debugging.
 		System.out.println(nickname+": Board constructor called.");
 		
 		game = TTTClient.getInstance().getGame();
 		// For debugging.
 		System.out.println(TTTClient.getInstance().getNickname()+": Game object passed to board.");
 		
 		initWidget(boardPanel);
 	}
 	
 	/*
 	 * Called when the game is in a terminal state. Display an appropriate message to the client
 	 * regarding whether they won, lost, or tied.
 	 */
 	public void endGame() {
 		String message;
 		if (game.isDraw())
 			message = "Game Over: Draw.";
 		else if (game.getWinnerId() == TTTClient.getInstance().getPlayer().getId())
 			message = "Game Over: You win!";
 		else
 			message = "Game Over: You lose.";
 		
 		Window.alert(message);
 	}
 
 	/*
 	 * Setup the board display.
 	 */
 	@PostConstruct
 	public void setupBoard() {
 		// Subscribe to game channel, which will be used by server and clients to communicate moves.
 		messageBus.subscribe("Game"+game.getGameId(), new GameMessageCallback());
 		
 		for (int i = 0; i < 3; i++) {
 			HorizontalPanel hPanel = new HorizontalPanel();
 			boardPanel.add(hPanel);
 			for (int j = 0; j < 3; j++) {
 				Tile tile = new Tile(i, j);
 				
 				// Assign initial class names, used in CSS file.
 				tile.setStylePrimaryName(primaryTileStyle);
 				tile.setStyleName(unusedTileStyle, true);
 				
 				// Add click handler for user to make move.
 				tile.addClickHandler(new TileClickEvent());
 				
 				hPanel.add(tile);
 			}
 		}
 	}
 	
 	/*
 	 * Replaces secondary tile style with given style. Secondary tile styles are unusedTileStyle, xTileStyle,
 	 * or oTileStyle.
 	 * 
 	 * @param row The row index (0-indexed) of the tile to be re-styled.
 	 * @param col The col index (0-indexed) of the tile to be re-styled.
 	 * @param style The CSS class name of the new secondary style. Should be one of unusedTileStyle, xTileStyle,
 	 * or oTileStyle.
 	 */
 	private void setTileStyleSecondary(int row, int col, String style) {
 		SimplePanel tile = (SimplePanel) ((HorizontalPanel) boardPanel.getWidget(row)).getWidget(col);
 		
 		// Clear all styles
 		tile.setStyleName("");
 		
 		// Re-declare base tile style.
 		tile.setStylePrimaryName(primaryTileStyle);
 		// Add new style.
 		tile.setStyleName(style, true);
 	}
 	
 	/*
 	 * Set a tile to display an X.
 	 * 
 	 * @param row The row index (0-indexed) of the tile to be modified.
 	 * @param col The col index (0-indexed) of the tile to be modified.
 	 */
 	public void setTileToX(int row, int col) {
 		setTileStyleSecondary(row, col, xTileStyle);
 	}
 	
 	/*
 	 * Set a tile to display an O.
 	 * 
 	 * @param row The row index (0-indexed) of the tile to be modified.
 	 * @param col The col index (0-indexed) of the tile to be modified.
 	 */
 	public void setTileToO(int row, int col) {
 		setTileStyleSecondary(row, col, oTileStyle);
 	}
 	
 	/*
 	 * Set a tile to appear vacant.
 	 * 
 	 * @param row The row index (0-indexed) of the tile to be modified.
 	 * @param col The col index (0-indexed) of the tile to be modified.
 	 */
 	public void resetTile(int row, int col) {
 		setTileStyleSecondary(row, col, "");
 	}
 }
