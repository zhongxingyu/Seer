 package gomoku.client.states;
 
 import static gomoku.net.Request.*;
 
 import gomoku.client.GomokuClient;
 import gomoku.client.gui.BoardWidget;
 import gomoku.client.gui.BoardWidget.Callback;
 import gomoku.client.gui.Button;
 import gomoku.client.gui.Fonts;
 import gomoku.client.gui.ScrollingPane;
 import gomoku.logic.Board;
 import gomoku.logic.GomokuConfig;
 import gomoku.logic.Board.BoardAction;
 import gomoku.logic.GomokuGame.GameAction;
 import gomoku.logic.GomokuGame.PlacePieceGameAction;
 import gomoku.logic.IllegalActionException;
 import gomoku.logic.Player;
 import gomoku.logic.GomokuGame;
 import gomoku.logic.Swap2;
 import gomoku.logic.Swap2.Swap2ChooseColorAction;
 import gomoku.logic.Swap2.Swap2PlacePieceGameAction;
 import gomoku.net.*;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 
 import TWLSlick.RootPane;
 
 import com.esotericsoftware.kryonet.Connection;
 
 import static org.trew.log.Log.*;
 
 /**
  * The playing state of the Gomoku game.
  *
  * @author Samuel Andersson
  */
 public class GameplayState extends GomokuNetworkGameState {
 
     /** Contains the game logic */
     public GomokuGame gomokuGame;
 
     /** The player */
     public Player me;
 
     /** The board displayed */
     private ScrollingPane scrollpane;
     private BoardWidget boardWidget;
     private Button confirmMoveButton;
     private Button blackButton;
     private Button whiteButton;
     private Button place2Button;
 
     private Image versus;
     private Image nametag;
     private Image infobar;
     private Image messagebox;
 
     private boolean loading;
     private boolean gameOver;
     private int gameOverVictoryState;
 
     private String[] playerList;
 
     private String errorMsg;
     private long errorTimer;
 
     private boolean pendingMove;
     private GameAction pendingAction;
 
     @Override
     protected RootPane createRootPane() {
         RootPane rp = super.createRootPane();
 
         boardWidget = new BoardWidget();
         scrollpane = new ScrollingPane(boardWidget, boardWidget);
         rp.add(scrollpane);
 
         return rp;
     }
 
     public boolean initialLoading() {
         return loading;
     }
 
     public void setInitialData(Board board, GomokuConfig config,
             int swap2state, int playerID, int turnID, String[] playerList,
             int playerOneColor, int playerTwoColor) {
         // create a new game
         this.playerList = playerList;
         gomokuGame = new GomokuGame(board, config, true);
         Swap2 swap2 = gomokuGame.getSwap2();
         if (swap2 != null)
             swap2.setState(swap2state);
         if (turnID == Player.PLAYERONE)
             gomokuGame.setTurn(gomokuGame.getPlayerOne());
         else if (turnID == Player.PLAYERTWO)
             gomokuGame.setTurn(gomokuGame.getPlayerTwo());
         setupPlayers(playerID, playerOneColor, playerTwoColor);
 
         setBoard(board);
 
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
     public void setupPlayers(int playerID, int playerOneColor,
             int playerTwoColor) {
         if (playerID == Player.PLAYERONE) {
             me = gomokuGame.getPlayerOne();
             playerList[0] = getGame().getProperties().getProperty("playername",
                     "(none)");
             gomokuGame.getPlayerTwo().setName(playerList[1]);
 
         } else if (playerID == Player.PLAYERTWO) {
             me = gomokuGame.getPlayerTwo();
             playerList[1] = getGame().getProperties().getProperty("playername",
                     "(none)");
             gomokuGame.getPlayerOne().setName(playerList[0]);
 
         } else {
             me = new Player("", Player.NOPLAYER);
             gomokuGame.getPlayerOne().setName(playerList[0]);
             gomokuGame.getPlayerTwo().setName(playerList[1]);
         }
 
         gomokuGame.getPlayerOne().setColor(playerOneColor);
         gomokuGame.getPlayerTwo().setColor(playerTwoColor);
         me.setName(getGame().getProperties()
                 .getProperty("playername", "(none)"));
         debug("Player ID set to " + me.getID());
     }
 
     public String[] getPlayerList() {
         return playerList;
     }
 
     public void setPlayerList(String[] playerList) {
         this.playerList = playerList;
 
         // if I'm player 1, do not update my name from the server. I *should*
         // know my name better that the server.
         if (me.getID() == Player.PLAYERONE) {
             gomokuGame.getPlayerTwo().setName(playerList[1]);
         } else if (me.getID() == Player.PLAYERTWO) {
             gomokuGame.getPlayerOne().setName(playerList[0]);
         } else {
             gomokuGame.getPlayerOne().setName(playerList[0]);
             gomokuGame.getPlayerTwo().setName(playerList[1]);
         }
     }
 
     @Override
     public void init(GameContainer container, final GomokuClient gomokuClient)
             throws SlickException {
 
         loading = true; // will be set to false once we receive data from the
                         // server
         gameOver = false;
         pendingAction = null;
         pendingMove = false;
 
         errorMsg = "";
 
         Image black = new Image("res/buttons/black.png");
         Image white = new Image("res/buttons/white.png");
         Image place2 = new Image("res/buttons/place2.png");
         blackButton = new Button(black, 0, 0, 3) {
             @Override
             public void buttonClicked(int button, int x, int y) {
                 chooseColor(gomokuClient, Board.BLACKPLAYER);
             }
         };
         whiteButton = new Button(white, 100, 0, 3) {
             @Override
             public void buttonClicked(int button, int x, int y) {
                 chooseColor(gomokuClient, Board.WHITEPLAYER);
             }
         };
         place2Button = new Button(place2, 200, 0, 3) {
             @Override
             public void buttonClicked(int button, int x, int y) {
                 gomokuGame.getSwap2().nextState();
                 boardWidget.setEnabled(true);
                 blackButton.setVisible(false);
                 whiteButton.setVisible(false);
                 place2Button.setVisible(false);
                 gomokuClient.client.sendTCP(new GenericRequestPacket(
                         Request.SkipChooseColor));
             }
         };
         blackButton.setVisible(false);
         whiteButton.setVisible(false);
         place2Button.setVisible(false);
 
         // add the board
         boardWidget.addCallback(new Callback() {
             @Override
             public void callback(int x, int y) {
                 if (me == null || !myTurn())
                     return;
                 placePiece(gomokuClient, x, y);
             }
         });
 
         Image ok = new Image("res/buttons/ok.png");
         confirmMoveButton = new Button(ok, 640, 482, 3) {
             @Override
             public void buttonClicked(int button, int x, int y) {
                 if (button == 0 && pendingMove && pendingAction != null) {
                     sendGameAction(gomokuClient, pendingAction);
                     pendingMove = false;
                 }
             }
         };
 
         versus = new Image("res/versus.png");
         nametag = new Image("res/nametag.png");
         infobar = new Image("res/infobar.png");
         messagebox = new Image("res/bottommessagebox.png");
 
         addListener(confirmMoveButton);
         addListener(blackButton);
         addListener(whiteButton);
         addListener(place2Button);
     }
 
     public void chooseColor(GomokuClient client, int color) {
         Swap2 swap2 = gomokuGame.getSwap2();
         if (swap2 == null)
             throw new IllegalStateException(
                     "This function cannot be called unless we're using swap 2 opening.");
         Swap2ChooseColorAction action = new Swap2ChooseColorAction(gomokuGame,
                 me.getID(), color);
         try {
             action.doAction(gomokuGame);
         } catch (IllegalActionException e) {
             error(e);
         }
         sendGameAction(client, action);
 
         boardWidget.setEnabled(true);
         blackButton.setVisible(false);
         whiteButton.setVisible(false);
         place2Button.setVisible(false);
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
         boolean waitForConfirm = !gomokuClient.getProperties()
                 .getProperty("autoconfirm", "true").equalsIgnoreCase("true");
         Swap2 swap2 = gomokuGame.getSwap2();
         if (swap2 != null && swap2.isActive()) {
             if (pendingMove) {
                 Swap2PlacePieceGameAction action = (Swap2PlacePieceGameAction) pendingAction;
                 pendingAction.undoAction(gomokuGame);
                 // are we removing the current piece?
                 BoardAction bAction = action.getBoardAction();
                 if (bAction != null
                         && (bAction.getX() == x && bAction.getY() == y)) {
                     pendingMove = false;
                     return; // don't place a new piece if we are
                 }
             }
             pendingAction = new Swap2PlacePieceGameAction(swap2, x, y,
                     waitForConfirm);
             if (waitForConfirm) {
                 try {
                     pendingAction.doAction(gomokuGame);
                     pendingMove = true;
                 } catch (IllegalActionException e) {
                     setErrorMsg(e.getMessage());
                 }
             } else {
                 sendGameAction(gomokuClient, pendingAction);
             }
 
         } else {
 
             // game is running as normal
             try {
                 PlacePieceGameAction action = null;
                 if (pendingMove) {
                     action = (PlacePieceGameAction) pendingAction;
                     pendingAction.undoAction(gomokuGame);
 
                     // are we removing the current piece?
                     BoardAction bAction = action.getBoardAction();
                     if (bAction != null
                             && (bAction.getX() == x && bAction.getY() == y)) {
                         pendingMove = false;
                         return; // don't place a new piece if we are
                     }
                 }
                 pendingAction = gomokuGame.placePiece(x, y, me, waitForConfirm);
                 if (waitForConfirm)
                     pendingMove = true;
                 else
                     sendGameAction(gomokuClient, pendingAction);
             } catch (IllegalActionException e) {
                 setErrorMsg(e.getMessage());
             }
         }
     }
 
     protected void sendGameAction(GomokuClient client, GameAction action) {
         client.client.sendTCP(new GameActionPacket(action));
         action.confirmAction(gomokuGame);
     }
 
     /**
      * Sets the error message to be displayed with a timer of 5 seconds.
      *
      * @param msg
      */
     protected void setErrorMsg(String msg) {
         debug(msg);
         errorMsg = msg;
         errorTimer = 5000;
     }
 
     /**
      * Whether it's our turn or not
      *
      * @return True if it's our turn
      */
     public boolean myTurn() {
         return me == gomokuGame.getTurn();
     }
 
     protected void setBoard(Board board) {
         boardWidget.setBoard(board);
         boardWidget.adjustSize();
 
        int scrollpaneWidth = Math.min(381, board.getWidth() * 25 + 6);
        int scrollpaneHeight = Math.min(381, board.getHeight() * 25 + 6);
         scrollpane.setInnerSize(scrollpaneWidth, scrollpaneHeight);
         scrollpane.setPosition(300 - scrollpane.getWidth() / 2, 300 - scrollpane.getHeight() / 2);
        scrollpane.updateScrollbarSizes();
        scrollpane.setScrollPositionX(scrollpane.getMaxScrollPosX() / 2);
        scrollpane.setScrollPositionY(scrollpane.getMaxScrollPosY() / 2);

     }
 
     @Override
     public void update(GameContainer container, GomokuClient gomokuClient,
             int delta) throws SlickException {
 
         Swap2 swap2 = gomokuGame.getSwap2();
         if (swap2 != null && swap2.isActive()) {
             if (swap2.isChoosingColorOrPlace(me, gomokuGame)) {
                 blackButton.setLocation(620, 380);
                 whiteButton.setLocation(620, 430);
                 place2Button.setLocation(590, 480);
 
                 boardWidget.setEnabled(false);
                 blackButton.setVisible(true);
                 whiteButton.setVisible(true);
                 place2Button.setVisible(true);
             } else if (swap2.isChoosingColor(me, gomokuGame)) {
                 blackButton.setLocation(620, 420);
                 whiteButton.setLocation(620, 470);
 
                 boardWidget.setEnabled(false);
                 blackButton.setVisible(true);
                 whiteButton.setVisible(true);
             }
         }
 
         if (errorTimer > 0)
             errorTimer -= delta;
 
         Input input = container.getInput();
 
         if (input.isKeyPressed(Input.KEY_ESCAPE)) {
             if (pendingMove) {
                 pendingAction.undoAction(gomokuGame);
                 pendingMove = false;
             } else {
                 enterState(PAUSEMENUSTATE, this);
             }
         }
 
         /* *** NETWORK RELATED INPUT *** */
         if (gomokuClient.client.isConnected()) {
             if (input.isKeyPressed(Input.KEY_F5)) {
                 gomokuClient.client.sendTCP(new GenericRequestPacket(
                         BoardUpdate));
             }
         }
         /* *** END NETWORK RELATED INPUT *** */
     }
 
     private static int rowYPos = 85;
 
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
             g.drawImage(versus, 400 - 117 / 2, 0);
             g.drawImage(nametag, 20, 10);
             g.drawImage(nametag, 490, 10);
             g.drawImage(infobar, 800 - 225, (600 - 454) / 2 + 8);
             g.drawImage(messagebox, 0, 600 - 63);
 
             // draw game info
             g.setFont(Fonts.getDefaultFont());
             int xPos = 590;
             rowYPos = 85;
             drawRow("Your name: " + me.getName(), xPos, g);
             if (me.getColor() != Board.NOPLAYER) {
                 drawRow("Your color: " + me.getColorName(), xPos, g);
                 drawRow("Turn: " + (myTurn() ? "You" : "Opponent"), xPos, g);
             }
             drawRow("Board size: " + gomokuGame.getBoard().getWidth() + "x"
                     + gomokuGame.getBoard().getHeight(), xPos, g);
 
             drawRow("Connected players", xPos, g);
             drawRow("-----------------", xPos, g);
             for (String p : this.playerList) {
                 if (p.equals("(none)"))
                     break;
                 drawRow(p, xPos, g);
             }
 
             if (pendingMove) {
                 g.setFont(Fonts.getAngelCodeFont("res/fonts/messagebox"));
                 g.drawString("Confirm move?", 600, 440);
                 confirmMoveButton.render(container, g);
             }
 
             // top
             g.setColor(Color.white);
             g.setFont(Fonts.getAngelCodeFont("res/fonts/nametag"));
             String name = gomokuGame.getPlayerOne().getName();
             int stringWidth = g.getFont().getWidth(name);
             g.drawString(name, center(20, 20 + 290, stringWidth), 13);
 
             name = gomokuGame.getPlayerTwo().getName();
             stringWidth = g.getFont().getWidth(name);
             g.drawString(name, center(490, 490 + 290, stringWidth), 13);
 
             // error msg
             g.setColor(Color.black);
             g.setFont(Fonts.getAngelCodeFont("res/fonts/messagebox"));
             if (errorTimer > 0) {
                 g.setColor(Color.red);
                 stringWidth = g.getFont().getWidth(errorMsg);
                 g.drawString(errorMsg, center(0, 800, stringWidth), 550);
             } else if (me.getID() == gomokuGame.getPlayerOne().getID()
                     || me.getID() == gomokuGame.getPlayerTwo().getID()) {
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
                 stringWidth = g.getFont().getWidth(status);
                 g.drawString(status, center(0, 800, stringWidth), 550);
             }
 
             g.setColor(Color.white);
             Swap2 swap2 = gomokuGame.getSwap2();
             if (swap2 != null) {
                 if (swap2.isChoosingColorOrPlace(me, gomokuGame)) {
                     g.setColor(Color.white);
                     g.drawString("Choose color or", 595, 300);
                     g.drawString("place 2 pieces", 600, 330);
                 } else if (swap2.isChoosingColor(me, gomokuGame)) {
                     g.setColor(Color.white);
                     g.drawString("Choose color", 610, 380);
                 }
             }
             blackButton.render(container, g);
             whiteButton.render(container, g);
             place2Button.render(container, g);
         }
     }
 
     @Override
     protected void handleGameAction(Connection conn, GameActionPacket ppp) {
         try {
             debug("Received GameAction of type "
                     + ppp.action.getClass().getSimpleName());
             ppp.action.doAction(gomokuGame);
             ppp.action.confirmAction(gomokuGame);
         } catch (IllegalActionException e) {
             warn("Illegal Action from server: " + e.getMessage());
             debug("Requesting boardupdate...");
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
     protected void handleGenericRequest(Connection conn,
             GenericRequestPacket grp) {
         if (grp.getRequest() == Request.SkipChooseColor) {
             Swap2 swap2 = gomokuGame.getSwap2();
             if (swap2 != null && swap2.getState() == 3) {
                 swap2.nextState();
             }
         }
     }
 
     @Override
     protected void handleNotifyTurn(Connection conn, NotifyTurnPacket ntp) {
         gomokuGame.setTurn(gomokuGame.getPlayer(ntp.getID()));
         info("Notified about turn: " + gomokuGame.getTurn().getName());
     }
 
     @Override
     protected void handlePlayerList(Connection conn, PlayerListPacket plp) {
         debug("Updated playerlist");
         setPlayerList(plp.players);
 
         // the first two spots is reserved for black and white. If we're not the
         // specified color, update it.
         if (me.getColor() != Board.BLACKPLAYER) {
             gomokuGame.getPlayerOne().setName(plp.players[0]);
         }
         if (me.getColor() != Board.WHITEPLAYER) {
             gomokuGame.getPlayerTwo().setName(plp.players[1]);
         }
     }
 
     /**
      * Handles what to do when the server sends information about who won. (not
      * necessarily us)
      */
     @Override
     protected void handleVictory(Connection conn, VictoryPacket vp) {
         debug("Victorystatus received: " + vp.victory);
         boardWidget.setEnabled(false);
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
