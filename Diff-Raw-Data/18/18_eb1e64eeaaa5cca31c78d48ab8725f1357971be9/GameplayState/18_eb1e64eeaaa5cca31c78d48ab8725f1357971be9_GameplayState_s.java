 package gomoku.client.states;
 
 import static gomoku.net.Request.*;
 
 import gomoku.client.GomokuClient;
 import gomoku.client.gui.BoardComponent;
 import gomoku.logic.Board;
 import gomoku.logic.GomokuConfig;
 import gomoku.logic.IllegalMoveException;
 import gomoku.logic.Player;
 import gomoku.logic.GomokuGame;
 import gomoku.net.*;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 
 import com.esotericsoftware.kryonet.Connection;
 
 import static org.trew.log.Log.*;
 
 /**
  * The playing state of the Gomoku game.
  *
  * @author Samuel Andersson
  */
 public class GameplayState extends GomokuGameState {
 
     /** Contains the game logic */
     public GomokuGame gomokuGame;
 
     public GomokuClient client;
 
     /** The player */
     public Player me;
 
     /** The board displayed */
     private BoardComponent boardComponent;
 
     /** Enables network events to be passed to this state */
     private BounceListener listener;
 
     private boolean loading;
     private boolean gameOver;
     private int gameOverVictoryState;
 
     private String[] playerList;
 
     public boolean initialLoading() {
         return loading;
     }
 
     public void setInitialData(Board board, GomokuConfig config,
             int playerColor, int turn, String[] playerList) {
         // create a new game
         this.playerList = playerList;
         gomokuGame = new GomokuGame(board, config);
         gomokuGame.setTurn(gomokuGame.getPlayer(turn));
         setupPlayers(playerColor);
 
         boardComponent.setBoard(board);
 
         loading = false;
         gameOverVictoryState = 0;
     }
 
     /**
      * Setup names and values for players. If this client receives black, makes
      * sure the white player receives the correct name.
      *
      * @param playerColor
      *            The player color being given to this client
      */
     public void setupPlayers(int playerColor) {
 
         if (playerColor == Board.BLACKPLAYER) {
             me = gomokuGame.getBlack();
             playerList[0] = client.getPlayerName();
             gomokuGame.getWhite().setName(playerList[1]);
 
         } else if (playerColor == Board.WHITEPLAYER) {
             me = gomokuGame.getWhite();
             playerList[1] = client.getPlayerName();
             gomokuGame.getBlack().setName(playerList[0]);
 
         } else {
             me = new Player("", Board.NOPLAYER);
             gomokuGame.getBlack().setName(playerList[0]);
             gomokuGame.getWhite().setName(playerList[1]);
         }
 
         me.setName(client.getPlayerName());
         info("Color set to " + me.getColorName());
     }
 
     public String[] getPlayerList() {
         return playerList;
     }
 
     public void setPlayerList(String[] playerList) {
         this.playerList = playerList;
 
         if (me.getColor() == Board.BLACKPLAYER) {
             gomokuGame.getWhite().setName(playerList[1]);
         } else if (me.getColor() == Board.WHITEPLAYER) {
             gomokuGame.getBlack().setName(playerList[0]);
         } else {
             gomokuGame.getBlack().setName(playerList[0]);
             gomokuGame.getWhite().setName(playerList[1]);
         }
     }
 
     @Override
     public void init(GameContainer container, final GomokuClient gomokuClient)
             throws SlickException {
 
         loading = true; // will be set to false once we receive data from the
                         // server
         gameOver = false;
 
         client = gomokuClient;
         client.setPlayerName("(none)");
 
         // add the board
         boardComponent = new BoardComponent(container, null, 100, 50, 30, 15,
                 15) {
             @Override
             public void squareClicked(int x, int y) {
                 if (me == null || !myTurn())
                     return;
                 placePiece(gomokuClient, x, y);
             }
         };
 
         listener = new BounceListener(this);
     }
 
     @Override
     public void enter(GameContainer container, GomokuClient gomokuClient)
             throws SlickException {
         // add network listener
         gomokuClient.client.addListener(listener);
     }
 
     @Override
     public void leave(GameContainer container, GomokuClient gomokuClient)
             throws SlickException {
         gomokuClient.client.removeListener(listener);
     }
 
     /**
      * Try to place a new piece on provided position. If successful client-side,
      * send a packet to server trying to do the same thing.
      *
      * @param gomokuClient
      *            The game which we place the piece in
      * @param x
      *            The x location for the new piece
      * @param y
      *            The y location for the new piece
      */
     public void placePiece(GomokuClient gomokuClient, int x, int y) {
         try {
             this.gomokuGame.placePiece(x, y, me.getColor());
             gomokuClient.client.sendTCP(new PlacePiecePacket(x, y, me
                     .getColor()));
         } catch (IllegalMoveException e) {
             info(e.getMessage());
         }
     }
 
     /**
      * Whether it's our turn or not
      *
      * @return True if it's our turn
      */
     public boolean myTurn() {
         return me == gomokuGame.getTurn();
     }
 
     public void setBoardSize(int width, int height) {
         boardComponent.setDisplaySize(width, height, 25);
     }
 
     @Override
     public void update(GameContainer container, GomokuClient gomokuClient,
             int delta) throws SlickException {
 
         Input input = container.getInput();
 
         if (input.isKeyPressed(Input.KEY_ESCAPE)) {
             container.exit();
         }
 
         /* *** NETWORK RELATED INPUT *** */
         if (gomokuClient.client.isConnected()) {
             if (input.isKeyPressed(Input.KEY_F5)) {
                 gomokuClient.client.sendTCP(new GenericRequestPacket(
                         BoardUpdate));
             }
 
             // ctrl is pressed
             if (input.isKeyDown(Input.KEY_LCONTROL)
                     || container.getInput().isKeyDown(Input.KEY_RCONTROL)) {
 
                 // clear board
                 if (input.isKeyPressed(Input.KEY_C) && !gameOver) {
                     gomokuClient.client.sendTCP(new GenericRequestPacket(
                             ClearBoard));
                 }
             }
         }
         /* *** END NETWORK RELATED INPUT *** */
     }
 
     private static int rowYPos = 20;
 
     private void drawRow(String str, int x, Graphics g) {
         int textHeight = g.getFont().getHeight(str);
         g.drawString(str, x, rowYPos);
         rowYPos += textHeight + 1;
     }
 
     @Override
     public void render(GameContainer container, GomokuClient gomokuClient,
             Graphics g) throws SlickException {
         g.setFont(container.getDefaultFont());
 
         // draw the board
         if (loading) {
             g.drawString("Loading...", 200, 200);
         } else {
             boardComponent.render(container, g);
 
             // draw game info
             int xPos = 600;
             rowYPos = 20;
             drawRow("Your name: " + me.getName(), xPos, g);
             if (me.getColor() != Board.NOPLAYER) {
                 drawRow("Your color: " + me.getColorName(), xPos, g);
                 drawRow("Turn: " + (myTurn() ? "You" : "Opponent"), xPos, g);
             }
             drawRow("Board size: " + gomokuGame.getBoard().getWidth() + "x"
                     + gomokuGame.getBoard().getHeight(), xPos, g);
             drawRow("Displaysize: " + boardComponent.getDisplayWidth() + "x"
                     + boardComponent.getDisplayHeight(), xPos, g);
 
             drawRow("Connected players", xPos, g);
             drawRow("-----------------", xPos, g);
             for (String p : this.playerList) {
                 if (p == "(none)")
                     break;
                 drawRow(p, xPos, g);
             }
 
             String versus = gomokuGame.getBlack().getName() + " vs "
                     + gomokuGame.getWhite().getName();
             g.drawString(versus, 250, 10);
 
            if (me.getColor() == Board.NOPLAYER) {
 
            } else {
                 String status = "";
                 if (gameOver) {
                     g.setColor(Color.white);
                     status += "Game Over!";
                     if (gameOverVictoryState == 0) {
                         status += " You lost.";
                     } else if (gameOverVictoryState == 1) {
                         status += " You won!";
                     } else if (gameOverVictoryState == 2) {
                         status += " Draw!";
                     }
                 } else if (!myTurn()) {
                     g.setColor(Color.red);
                     status += "waiting for opponent";
                 } else {
                     g.setColor(Color.green);
                     status += "Your turn!";
                 }
                 g.drawString(status, 250, 550);
             }
 
             g.setColor(Color.white);
 
         }
     }
 
     @Override
     protected void handlePlacePiece(Connection conn, PlacePiecePacket ppp) {
         try {
             gomokuGame.placePiece(ppp.x, ppp.y, ppp.playerColor);
             Player player = gomokuGame.getPlayer(ppp.playerColor);
             info(player.getColorName() + " piece placed on " + ppp.x + ", "
                     + ppp.y);
         } catch (IllegalMoveException e) {
             warn("Piece couldn't be placed: " + e.getMessage());
             info("Requesting boardupdate...");
             conn.sendTCP(new GenericRequestPacket(BoardUpdate));
         }
     }
 
     @Override
     protected void handleBoard(Connection conn, BoardPacket bp) {
         // let's update our board with the board of the server
         gomokuGame.replaceBoard(bp.getBoard());
         info("Board updated");
     }
 
     @Override
     protected void handleNotifyTurn(Connection conn, NotifyTurnPacket ntp) {
         gomokuGame.setTurn(gomokuGame.getPlayer(ntp.getColor()));
         info("Notified about turn: " + gomokuGame.getTurn().getName());
     }
 
     @Override
     protected void handlePlayerList(Connection conn, PlayerListPacket plp) {
         debug("Updated playerlist");
         setPlayerList(plp.players);
 
         // the first two spots is reserved for black and white. If we're not the
         // specified color, update it.
         if (me.getColor() != Board.BLACKPLAYER) {
             gomokuGame.getBlack().setName(plp.players[0]);
         }
         if (me.getColor() != Board.WHITEPLAYER) {
             gomokuGame.getWhite().setName(plp.players[1]);
         }
     }
 
     /**
      * Handles what to do when the server sends information about who won. (not
      * necessarily us)
      */
     @Override
     protected void handleVictory(Connection conn, VictoryPacket vp) {
         debug("Victorystatus received: " + vp.victory);
         boardComponent.setChangeable(false);
         gameOver = true;
 
         gameOverVictoryState = 0;
         if (vp.victory == 0) { // draw
             gameOverVictoryState = 2;
         } else if (vp.victory == me.getColor()) { // victory
             gameOverVictoryState = 1;
         }
     }
 
     @Override
     public int getID() {
         return GAMEPLAYSTATE;
     }
 
 }
