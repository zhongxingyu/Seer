 /*
  * Copyright 2006 by Sun Microsystems, Inc.  All rights reserved.
  */
 
 package com.sun.gi.apps.battleboard.client;
 
 import com.sun.gi.comm.discovery.impl.URLDiscoverer;
 import com.sun.gi.comm.users.client.ClientAlreadyConnectedException;
 import com.sun.gi.comm.users.client.ClientChannel;
 import com.sun.gi.comm.users.client.ClientChannelListener;
 import com.sun.gi.comm.users.client.ClientConnectionManager;
 import com.sun.gi.comm.users.client.ClientConnectionManagerListener;
 import com.sun.gi.comm.users.client.impl.ClientConnectionManagerImpl;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.nio.ByteBuffer;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.security.auth.callback.Callback;
 import javax.security.auth.callback.NameCallback;
 import javax.security.auth.callback.PasswordCallback;
 
 // XXX import static?  what's that?
 
 public class BattleBoardPlayer implements ClientChannelListener {
 
     private static final Logger log =
 	Logger.getLogger("com.sun.gi.apps.battleboard.client");
 
     private final ClientChannel channel;
 
     private List<String> playerNames = null;
     private Map<String, BattleBoard> playerBoards = null;
     private String myName;
     private BattleBoard myBoard;
     private boolean lost = false;
     private ClientConnectionManager mgr;
 
     public BattleBoardPlayer(ClientConnectionManager mgr, ClientChannel chan,
 	    String playerName)
     {
 	this.mgr = mgr;
 	this.channel = chan;
 	this.myName = playerName;
     }
 
     /**
      * {@inheritDoc}
      */
     public void playerJoined(byte[] playerID) {
 	log.info("playerJoined on " + channel.getName());
     }
 
     /**
      * {@inheritDoc}
      */
     public void playerLeft(byte[] playerID) {
 	log.info("playerJoined on " + channel.getName());
     }
 
     public void dataArrived(byte[] uid, ByteBuffer data, boolean reliable) {
 
 	// XXX: sanity checks?
 
 	log.info("dataArrived on " + channel.getName());
 
 	byte[] bytes = new byte[data.remaining()];
 	data.get(bytes);
 	String text = new String(bytes);
 
 	log.info("dataArrived: (" + text + ")");
 
 	String[] tokens = text.split("\\s+");
 	if (tokens.length == 0) {
 	    log.warning("empty message");
 	    return;
 	}
 
 	if (log.isLoggable(Level.FINER)) {
 	    int pos = 0;
 	    for (String t : tokens) {
 		log.finer("\tpos = " + pos++ + " token = " + t);
 	    }
 	}
 
 	playGame(tokens);
     }
 
     /**
      * {@inheritDoc}
      */
     public void channelClosed() {
 	log.info("channel " + channel.getName() + " closed");
     }
 
     /**
      * Performs the game-play for the given array of tokens.
      *
      * @param tokens an array of Strings containing the tokens
      * of the message from the server
      */
     void playGame(String[] tokens) {
 	String cmd = tokens[0];
 
 	if ("ok".equals(cmd)) {
 	    setBoard(tokens);
 	} else if ("turn-order".equals(cmd)) {
 	    setTurnOrder(tokens);
 	} else if ("move-started".equals(cmd)) {
 	    if (myName.equals(tokens[1])) {
 		yourTurn();
 	    } else {
 		moveStarted(tokens);
 	    }
 	} else if ("move-ended".equals(cmd)) {
 	    moveEnded(tokens);
 	} else if ("withdraw".equals(cmd)) {
 	    withdraw(tokens);
 	}
 
 	if (lost) {
 	    displayMessage("You lose!");
 	    displayMessage("Better luck next time.");
 	    // mgr.disconnect();
	} else if ((playerNames != null) && (playerNames.size() == 1)) {
 	    if (myName.equals(playerNames.get(0))) {
 		displayMessage("YOU WIN!  w00t!");
 	    } else {
 		displayMessage(playerNames.get(1) + " has won.");
 	    }
 	    mgr.disconnect();
 	}
     }
 
     /**
      * Implements the operations for the "ok" message, which tells the
      * user what board the server has chosen for them.
      *
      * @param tokens an array of Strings containing the tokens of the
      * message from the server
      *
      * @return <code>true</code> if the message was valid and executed
      * correctly, <code>false</code> otherwise
      */
     private boolean setBoard(String[] args) {
 	if (args.length < 4) {
 	    log.severe("setBoard: incorrect number of arguments");
 	    return false;
 	}
 
 	int boardWidth = (int) new Integer(args[1]);
 	int boardHeight = (int) new Integer(args[2]);
 	int numCities = (int) new Integer(args[3]);
 
 	if ((boardWidth < 1) || (boardHeight < 1)) {
 	    log.severe("bad board dimensions (" +
 		    boardWidth + ", " + boardHeight + ")");
 	    return false;
 	}
 
 	if (numCities < 1) {
 	    log.severe("bad numCities (" + numCities + ")");
 	    return false;
 	}
 
 	BattleBoard tempBoard = new BattleBoard(myName,
 		boardWidth, boardHeight, numCities);
 
 	if (((args.length - 4) % 2) != 0) {
 	    log.severe("bad list of city positions");
 	    return false;
 	}
 
 	for (int base = 4; base < args.length; base += 2) {
 	    int x = (int) new Integer(args[base]);
 	    int y = (int) new Integer(args[base+1]);
 
 	    if ((x < 0) || (x >= boardWidth) || (y < 0) || (y >= boardHeight)) {
 		log.severe("improper city position (" + x + ", " + y + ")");
 		return false;
 	    }
 
 	    tempBoard.update(x, y, BattleBoard.positionValue.CITY);
 	}
 
 	myBoard = tempBoard;
 	displayMessage("Here is your board:\n");
 	myBoard.display();
 
 	return true;
     }
 
     /**
      * Implements the operations for the "turn-order" message, which
      * tells the player what the order of turns will be among the
      * players.
      *
      * @param tokens an array of Strings containing the tokens of the
      * message from the server
      *
      * @return <code>true</code> if the message was valid and executed
      * correctly, <code>false</code> otherwise
      */
     private boolean setTurnOrder(String[] args) {
 
 	if (playerNames != null) {
 	    log.severe("setTurnOrder has already been done");
 	    return false;
 	}
 
 	if (args.length < 3) {
 	    log.severe("setTurnOrder: " +
 		    "incorrect number of args: " + args.length + " != 3");
 	    return false;
 	}
 
 	playerNames = new LinkedList<String>();
 	playerBoards = new HashMap<String, BattleBoard>();
 
 	for (int i = 1; i < args.length; i++) {
 	    String playerName = args[i];
 	    playerNames.add(playerName);
 
 	    if (myName.equals(playerName)) {
 		playerBoards.put(myName, myBoard);
 	    } else {
 		playerBoards.put(playerName,
 			new BattleBoard(playerName,
 				myBoard.getWidth(), myBoard.getHeight(),
 				myBoard.getStartCities()));
 	    }
 	}
 
 	displayMessage("Initial Boards:\n");
 	displayBoards(null);
 	return true;
     }
 
     /**
      * Implements the operations for the "move-started" message, for
      * the player whose move it is.
      *
      * @return <code>true</code> if the move was executed correctly,
      * <code>false</code> otherwise
      */
     private boolean yourTurn() {
 	displayMessage("Your move!\n");
 
 	for (;;) {
 	    String[] move = BattleBoardUtils.getKeyboardInputTokens(
 			"player x y, or pass ");
 	    if ((move.length == 1) && "pass".equals(move[0])) {
 		if (mgr != null) {
 		    ByteBuffer buf = ByteBuffer.wrap("pass".getBytes());
 		    buf.position(buf.limit());
 		    mgr.sendToServer(buf, true);
 		} else {
 		    displayMessage("TO SERVER: " + "pass" + "\n");
 		}
 		break;
 	    } else if (move.length == 3) {
 		String bombedPlayer = move[0];
 		if (!playerNames.contains(bombedPlayer)) {
 		    displayMessage("Error: player (" + bombedPlayer +
 			    ") is not in the game\n");
 		    displayMessage("Please try again.\n");
 		    continue;
 		}
 
 		int x = (int) new Integer(move[1]);
 		int y = (int) new Integer(move[2]);
 
 		if ((x < 0) || (x >= myBoard.getWidth()) ||
 			(y < 0) || (y >= myBoard.getHeight())) {
 		    displayMessage("Illegal (x,y)\n");
 		    displayMessage("Please try again.\n");
 		    continue;
 		}
 
 		String moveMessage = "move " + bombedPlayer + " " +
 			x + " " + y;
 
 		if (mgr != null){
 		    ByteBuffer buf = ByteBuffer.wrap(moveMessage.getBytes());
 		    buf.position(buf.limit());
 		    mgr.sendToServer(buf, true);
 		} else {
 		    displayMessage("TO SERVER: " + moveMessage + "\n");
 		}
 		break;
 	    } else {
 		displayMessage(
 			"Improperly formatted move.  Please try again.\n");
 	    }
 
 	    return true;
 	}
 
 	return true;
     }
 
     /**
      * Implements the operations for the "move-started" message, for
      * any player whose move it is not.
      *
     * @param tokens an array of Strings containing the tokens of the
     * message from the server
     *
      * @return <code>true</code> if the move was executed correctly,
      * <code>false</code> otherwise
      */
     private boolean moveStarted(String[] args) {
 
 	if (playerNames == null) {
 	    log.severe("setTurnOrder has not yet been done");
 	    return false;
 	}
 
 	if (args.length != 2) {
 	    log.severe("moveStarted: " +
 		    "incorrect number of args: " + args.length + " != 2");
 	    return false;
 	}
 
 	String currPlayer = args[1];
 	log.info("move-started for " + currPlayer);
 
 	if (!playerNames.contains(currPlayer)) {
 	    log.severe("moveStarted: nonexistant player (" + currPlayer + ")");
 	    return false;
 	}
 
 	displayMessage(currPlayer + " is making a move...\n");
 	return true;
     }
 
     /**
      * Implements the operations for the "move-ended" message.
      *
     * @param tokens an array of Strings containing the tokens of the
     * message from the server
     *
      * @return <code>true</code> if the move was executed correctly,
      * <code>false</code> otherwise
      */
     private boolean moveEnded(String[] args) {
 
 	if (playerNames == null) {
 	    log.severe("setTurnOrder has not yet been done");
 	    return false;
 	}
 
 	if (args.length < 3) {
 	    log.severe("moveEnded: " +
 		    "incorrect number of args: " + args.length + " < 3");
 	}
 
 	String currPlayer = args[1];
 	String action = args[2];
 
 	log.info("move-ended for " + currPlayer);
 
 	if ("pass".equals(action)) {
 	    if (args.length != 3) {
 		log.severe("moveEnded: " +
 			"incorrect number of args: " + args.length + " != 3");
 		return false;
 	    }
 	    log.info(currPlayer + " passed");
 
 	    displayMessage(currPlayer + " passed.\n");
 	    return true;
 	} else if ("bomb".equals(action)) {
 	    if (args.length != 7) {
 		log.severe("moveEnded: " +
 			"incorrect number of args: " + args.length + " != 7");
 		return false;
 	    }
 
 	    String bombedPlayer = args[3];
 	    BattleBoard board = playerBoards.get(bombedPlayer);
 	    if (board == null) {
 		log.severe("nonexistant player (" + bombedPlayer + ")");
 		return false;
 	    }
 
 	    int x = Integer.parseInt(args[4]);
 	    int y = Integer.parseInt(args[5]);
 
 	    if ((x < 0) || (x >= myBoard.getWidth()) ||
 		    (y < 0) || (y >= myBoard.getHeight())) {
 		log.warning("impossible board position " +
 			"(" + x + ", " + y + ")");
 		return false;
 	    }
 
 	    String outcome = args[6];
 	    boolean lost = false;
 
 	    log.info(bombedPlayer + " bombed ("
 		    + x + ", " + y + ") with outcome " + outcome);
 	    displayMessage(currPlayer + " bombed " + bombedPlayer +
 		    " at " + x + "," + y + " with outcome " + outcome + "\n");
 
 	    if ("HIT".equals(outcome) || "LOSS".equals(outcome)) {
 		board.update(x, y, BattleBoard.positionValue.HIT);
 		board.hit();
 
 		if ("LOSS".equals(outcome)) {
 		    if (bombedPlayer.equals(myName)) {
 			displayMessage("You just lost your last city!");
 			lost = true;
 		    } else {
 			displayMessage(bombedPlayer + " lost their last city.");
 		    }
 		} else {
 		    if (bombedPlayer.equals(myName)) {
 			displayMessage("You just lost a city!");
 		    } else {
 			displayMessage(bombedPlayer + " lost a city.");
 		    }
 		}
 	    } else if ("NEAR_MISS".equals(outcome)) {
 		board.update(x, y, BattleBoard.positionValue.NEAR);
 	    } else if ("MISS".equals(outcome)) {
 		board.update(x, y, BattleBoard.positionValue.MISS);
 	    }
 
 	    displayBoards(bombedPlayer);
 	} else {
 	    log.severe("moveEnded: invalid command");
 	    return false;
 	}
 	return true;
     }
 
    /**
     * Implements the operations for the "withdraw" message.
     *
     * @param tokens an array of Strings containing the tokens of the
     * message from the server
     *
     * @return <code>true</code> if the move was executed correctly,
     * <code>false</code> otherwise
     */
     private boolean withdraw(String[] args) {
 	if (playerNames == null) {
 	    log.severe("setTurnOrder has not yet been done");
 	    return false;
 	}
 
 	if (args.length != 2) {
 	    log.severe("withdraw: incorrect number of args: " +
 		    args.length + " != 2");
 	    return false;
 	}
 
 	String withdrawnPlayer = args[1];
 	if (!playerNames.remove(withdrawnPlayer)) {
 	    log.warning("withdraw: nonexistant player (" +
 		    withdrawnPlayer + ")");
 	    return false;
 	} else {
 	    log.info(withdrawnPlayer + " has withdrawn");
 
 	    displayMessage(withdrawnPlayer + " has withdrawn.");
 	    displayBoards(null);
 	}
 
 	return true;
     }
 
    /**
     * Returns <code>true</code> if this player has lost the
     * game, <code>false</code> otherwise.
     *
     * @returns <code>true</code> if this player has lost the
     * game, <code>false</code> otherwise
     */
     public boolean lost() {
 	return lost;
     }
 
     private void displayMessage(String message) {
 	System.out.print(message);
 	System.out.flush();
     }
 
     private void displayBoards(String activePlayer) {
 
 	if ((activePlayer != null) && myName.equals(activePlayer)) {
 	    System.out.println("========");
 	    myBoard.display();
 	    System.out.println("========");
 	} else {
 	    myBoard.display();
 	}
 
 	for (String name : playerNames) {
 	    if (name.equals(myName)) {
 		continue;
 	    }
 
 	    if ((activePlayer != null) && name.equals(activePlayer)) {
 		System.out.println("========");
 		playerBoards.get(name).display();
 		System.out.println("========");
 	    } else {
 		playerBoards.get(name).display();
 	    }
 	}
     }
 }
