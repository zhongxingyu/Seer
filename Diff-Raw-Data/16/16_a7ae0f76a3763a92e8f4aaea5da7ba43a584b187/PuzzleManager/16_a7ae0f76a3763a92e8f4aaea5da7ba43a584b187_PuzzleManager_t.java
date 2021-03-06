 //
// $Id: PuzzleManager.java,v 1.15 2004/09/01 21:03:11 ray Exp $
 //
 // Narya library - tools for developing networked games
 // Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
 // http://www.threerings.net/code/narya/
 //
 // This library is free software; you can redistribute it and/or modify it
 // under the terms of the GNU Lesser General Public License as published
 // by the Free Software Foundation; either version 2.1 of the License, or
 // (at your option) any later version.
 //
 // This library is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 // Lesser General Public License for more details.
 //
 // You should have received a copy of the GNU Lesser General Public
 // License along with this library; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
 package com.threerings.puzzle.server;
 
 import java.util.Arrays;
 
 import com.samskivert.util.IntListUtil;
 import com.samskivert.util.IntervalManager;
 import com.samskivert.util.StringUtil;
 
 import com.threerings.presents.data.ClientObject;
 import com.threerings.presents.dobj.AttributeChangedEvent;
 import com.threerings.presents.dobj.DObject;
 import com.threerings.presents.dobj.OidList;
 import com.threerings.presents.server.util.SafeInterval;
 
 import com.threerings.crowd.data.BodyObject;
 import com.threerings.crowd.server.CrowdServer;
 import com.threerings.crowd.server.PlaceManagerDelegate;
 
 import com.threerings.parlor.game.GameManager;
 
 import com.threerings.util.MessageBundle;
 import com.threerings.util.Name;
 import com.threerings.util.RandomUtil;
 
 import com.threerings.puzzle.Log;
 import com.threerings.puzzle.data.Board;
 import com.threerings.puzzle.data.BoardSummary;
 import com.threerings.puzzle.data.PuzzleCodes;
 import com.threerings.puzzle.data.PuzzleConfig;
 import com.threerings.puzzle.data.PuzzleGameMarshaller;
 import com.threerings.puzzle.data.PuzzleObject;
 
 /**
  * Extends the {@link GameManager} with facilities for the puzzle games
  * that are used in Yohoho. Only features generic to all of our games are
  * in this base class and additional features are supported both through
  * the inheritance hierarchy and through delegating helpers (because Java
  * conveniently doesn't support multiple inheritance).
  */
 public abstract class PuzzleManager extends GameManager
     implements PuzzleCodes, PuzzleGameProvider
 {
     /**
      * Returns the boards for all players.
      */
     public Board[] getBoards ()
     {
         return _boards;
     }
 
     /**
      * Returns the user object for the player with the specified index or
      * null if the player at that index is not online.
      */
     public BodyObject getPlayer (int playerIdx)
     {
         // if we have their oid, use that
         int ploid = _playerOids[playerIdx];
         if (ploid > 0) {
             return (BodyObject)CrowdServer.omgr.getObject(ploid);
         }
         // otherwise look them up by name
         Name name = getPlayerName(playerIdx);
         return (name == null) ? null : CrowdServer.lookupBody(name);
     }
 
     /**
      * Returns the board summary for the given player index.
      */
     public BoardSummary getBoardSummary (int pidx)
     {
         return (_puzobj == null || _puzobj.summaries == null) ? null :
             _puzobj.summaries[pidx];
     }
 
     /**
      * Returns whether this puzzle cares to make use of per-player board
      * summaries that are sent periodically to all users in the puzzle via
      * {@link #sendStatusUpdate}.  The default implementation returns
      * <code>false</code>.
      */
     public boolean needsBoardSummaries ()
     {
         return false;
     }
 
     /**
      * Returns whether this puzzle compares board states before it applies
      * progress events, or after. The default implementation returns
      * <code>true</code>.
      */
     protected boolean compareBeforeApply ()
     {
         return true;
     }
 
     /**
      * Calls {@link BoardSummary#summarize} on the given player's board
      * summary to refresh the summary information in preparation for
      * sending along to the client(s).
      *
      * @param pidx the player index of the player whose board is to be
      * summarized.
      */
     public void updateBoardSummary (int pidx)
     {
         if (_puzobj.summaries != null && _puzobj.summaries[pidx] != null) {
             _puzobj.summaries[pidx].summarize();
         }
     }
 
     /**
      * Ends the game for the given player.
      */
     public void endPlayerGame (int pidx)
     {
         // don't update board summaries for AI players since they keep
         // their own board summary up to date
         if (!isAI(pidx)) {
             // update the board summary with the player's final board
             updateBoardSummary(pidx);
         }
 
         // go for a little transactional efficiency
         _puzobj.startTransaction();
         try {
             // end the player's game
             _puzobj.setPlayerStatusAt(PuzzleObject.PLAYER_KNOCKED_OUT, pidx);
 
             // let derived classes do some business
             playerGameDidEnd(pidx);
 
             // force a status update
             updateStatus();
 
             // notify the players
             String message = MessageBundle.tcompose(
                 "m.player_game_over", getPlayerName(pidx));
             systemMessage(PUZZLE_MESSAGE_BUNDLE, message);
 
         } finally {
             _puzobj.commitTransaction();
         }
 
         // if it's time to end the game, then do so
         if (shouldEndGame()) {
             endGame();
 
         } else {
             // otherwise report that the player was knocked out to other
             // people in his/her room
             reportPlayerKnockedOut(pidx);
         }
     }
 
     /**
      * Returns whether game conclusion antics such as rating updates
      * should be performed when an in-play game is ended.  Derived classes
      * may wish to override this method to customize the conditions under
      * which the game is concluded.
      */
     public boolean shouldConcludeGame ()
     {
         return (_puzobj.state == PuzzleObject.GAME_OVER);
     }
 
     /**
      * Called when a player has been marked as knocked out but before the
      * knock-out status update has been sent to the players. Any status
      * information that needs be updated in light of the knocked out
      * player can be updated here.
      */
     protected void playerGameDidEnd (int pidx)
     {
     }
 
     // documentation inherited
     protected Class getPlaceObjectClass ()
     {
         return PuzzleObject.class;
     }
 
     // documentation inherited
     protected void didInit ()
     {
         super.didInit();
 
         // save off a casted reference to our puzzle config
         _puzconfig = (PuzzleConfig)_config;
     }
 
     // documentation inherited
     protected void didStartup ()
     {
         super.didStartup();
 
         // grab the puzzle object
         _puzobj = (PuzzleObject)_gameobj;
 
         // create and fill in our game service object
         PuzzleGameMarshaller service = (PuzzleGameMarshaller)
             _invmgr.registerDispatcher(new PuzzleGameDispatcher(this), false);
         _puzobj.setPuzzleGameService(service);
     }
 
     // documentation inherited
     protected void handlePartialNoShow ()
     {
         // mark the no-show players; this will cause allPlayersReady() to
         // think that everyone has arrived, but still allow us to tell who
         // has not shown up in gameDidStart()
         for (int ii = 0; ii < _playerOids.length; ii++) {
             if (_playerOids[ii] == 0) {
                 _playerOids[ii] = -1;
             }
         }
 
         // go ahead and start the game; gameDidStart() will take care of
         // giving the boot to anyone who isn't around
         Log.info("Forcing start of partial no-show game " +
                  "[game=" + _puzobj.which() +
                  ", players=" + StringUtil.toString(_puzobj.players) +
                  ", poids=" + StringUtil.toString(_playerOids) + "].");
         startGame();
     }
 
     // documentation inherited
     protected void gameWillStart ()
     {
         int size = getPlayerSlots();
         if (_boards == null) {
             // create our arrays
             _boards = new Board[size];
             _lastProgress = new long[size];
         } else {
             Arrays.fill(_boards, null);
         }
 
         // start everyone out with reasonable last progress stamps
         Arrays.fill(_lastProgress, System.currentTimeMillis());
 
         // compute the starting difficulty (this has to happen before we
         // set the seed because that triggers the generation of the boards
         // on the client)
         _puzobj.setDifficulty(computeDifficulty());
 
         // initialize the seed that goes out with this round
         _puzobj.setSeed(RandomUtil.rand.nextLong());
 
         // initialize the player status
         _puzobj.setPlayerStatus(new int[size]);
 
         // initialize the player boards
         initBoards();
 
         // let the game manager start up its business
         super.gameWillStart();
 
         // send along an initial status update before we start up the
         // status update interval
         sendStatusUpdate();
 
         long statusInterval = getStatusInterval();
         if (_uiid == -1 && statusInterval > 0) {
             // register the status update interval to address subsequent
             // periodic updates
             _uiid = IntervalManager.register(
                 new SafeInterval(CrowdServer.omgr) {
                     public void run () {
                         sendStatusUpdate();
                     }
                 }, statusInterval, null, true);
         }
     }
 
     /**
      * Returns the frequency with which puzzle status updates are
      * broadcast to the players (which is accomplished via a call to
      * {@link #sendStatusUpdate} which in turn calls {@link #updateStatus}
      * wherein derived classes can participate in the status update).
      * Returning <code>O</code> (the default) indicates that a periodic
      * status update is not desired.
      */
     protected long getStatusInterval ()
     {
         return 0L;
     }
 
     /**
      * When a puzzle game starts, the manager is given the opportunity to
      * configure the puzzle difficulty based on information known about
      * the player. Additionally, when the game resets due to the player
      * clearing the board, etc. this will be called again, so the
      * difficulty can be ramped up as the player progresses. In situations
      * where ratings and experience are tracked, the difficulty can be
      * seeded based on the players prior performance.
      */
     protected int computeDifficulty ()
     {
         return DEFAULT_DIFFICULTY;
     }
 
     // documentation inherited
     protected void gameDidStart ()
     {
         super.gameDidStart();
 
         // any players who have not claimed that they are ready should now
         // be given le boote royale
         for (int ii = 0; ii < _playerOids.length; ii++) {
             if (_playerOids[ii] == -1) {
                 Log.info("Booting no-show player [game=" + _puzobj.which() +
                          ", player=" + getPlayerName(ii) + "].");
                 _playerOids[ii] = 0; // unfiddle the blank oid
                 endPlayerGame(ii);
             }
         }
 
         // log the AI skill levels for games involving AIs as it's useful
         // when tuning AI algorithms
         if (_AIs != null) {
             Log.info("AIs on the job [game=" + _puzobj.which() +
                      ", skillz=" + StringUtil.toString(_AIs) + "].");
         }
     }
 
     /**
      * Updates (in one puzzle object transaction) all periodically updated
      * status information.
      */
     protected void sendStatusUpdate ()
     {
         _puzobj.startTransaction();
         try {
             // Log.info("Updating status [game=" + _puzobj.which() + "].");
             updateStatus();
         } finally {
             _puzobj.commitTransaction();
         }
     }
 
     /**
      * A puzzle periodically (default of once every 5 seconds but
      * configurable by puzzle) updates status information that is visible
      * to the user. Derived classes can override this method and effect
      * their updates by generating events on the puzzle object and they
      * will be packaged into the update transaction.
      */
     protected void updateStatus ()
     {
         // if we're a board summary updating kind of puzzle, do that
         if (needsBoardSummaries()) {
             // they're already modified in-situ, so we just rebroadcast
             // the latest versions
             _puzobj.setSummaries(_puzobj.summaries);
         }
     }
 
     /**
      * Send a system message with the puzzle bundle.
      */
     protected void systemMessage (String msg)
     {
         systemMessage(msg, false);
     }
 
     /**
      * Send a system message with the puzzle bundle.
      *
      * @param waitForStart if true, the message will not be sent until the
      * game has started.
      */
     protected void systemMessage (String msg, boolean waitForStart)
     {
         systemMessage(PUZZLE_MESSAGE_BUNDLE, msg, waitForStart);
     }
 
     /**
      * Creates and initializes boards and board summaries (if desired per
      * {@link #needsBoardSummaries}) for each player.
      */
     protected void initBoards ()
     {
         long seed = _puzobj.seed;
         BoardSummary[] summaries = needsBoardSummaries() ?
             new BoardSummary[getPlayerSlots()] : null;
 
         // set up game information for each player
         for (int ii = 0, nn = getPlayerSlots(); ii < nn; ii++) {
             boolean needsPlayerBoard = needsPlayerBoard(ii);
             if (needsPlayerBoard) {
                 // create the game board
                 _boards[ii] = newBoard(ii);
                 _boards[ii].initializeSeed(seed);
                 if (summaries != null) {
                     summaries[ii] = newBoardSummary(_boards[ii]);
                 }
             }
         }
         // these will be sent to the players on the first status update
         _puzobj.summaries = summaries;
     }
 
     /**
      * Returns whether this puzzle needs a board for the given player
      * index.  The default implementation only creates boards for occupied
      * player slots.  Derived classes may wish to override this method if
      * they have specialized board needs, e.g., they need only a single
      * board for all players.
      */
     protected boolean needsPlayerBoard (int pidx)
     {
         return (_puzobj.isOccupiedPlayer(pidx));
     }
 
     // documentation inherited
     protected void gameDidEnd ()
     {
         if (_uiid != -1) {
             // remove the client update interval
             IntervalManager.remove(_uiid);
             _uiid = -1;
         }
 
         // send along one final status update
         sendStatusUpdate();
 
         // report the winners and losers if appropriate
        int winnerCount = _puzobj.getWinnerCount();
        if (shouldConcludeGame() && winnerCount > 0 && !_puzobj.isDraw()) {
             reportWinnersAndLosers();
         }
 
         super.gameDidEnd();
     }
 
     /**
      * Report winner and loser oids to each room that any of the
      * winners/losers is in.
      */
     protected void reportWinnersAndLosers ()
     {
         OidList winners = new OidList();
         OidList losers = new OidList();
         OidList places = new OidList();
 
         Object[] args = new Object[] { winners, losers };
 
         for (int ii=0, nn=_playerOids.length; ii < nn; ii++) {
             BodyObject user = getPlayer(ii);
             if (user != null) {
                 places.add(user.location);
                 (_puzobj.isWinner(ii) ? winners : losers).add(user.getOid());
             }
         }
 
         // now send a message event to each room
         for (int ii=0, nn = places.size(); ii < nn; ii++) {
             DObject place = CrowdServer.omgr.getObject(places.get(ii));
             if (place != null) {
                 place.postMessage(WINNERS_AND_LOSERS, args);
             }
         }
     }
 
     /**
      * Report to the knocked-out player's room that they were knocked out.
      */
     protected void reportPlayerKnockedOut (int pidx)
     {
         BodyObject user = getPlayer(pidx);
         OidList knocky = new OidList(1);
         knocky.add(user.getOid());
 
         DObject place = CrowdServer.omgr.getObject(user.location);
         if (place != null) {
             place.postMessage(PLAYER_KNOCKED_OUT, new Object[] { knocky });
         }
     }
 
     // documentation inherited
     protected void didShutdown ()
     {
         super.didShutdown();
 
         // make sure our update interval is unregistered
         if (_uiid != -1) {
             // remove the client update interval
             IntervalManager.remove(_uiid);
             _uiid = -1;
         }
 
         // clear out our service registration
         _invmgr.clearDispatcher(_puzobj.puzzleGameService);
     }
 
     /**
      * Applies progress updates received from the client. If puzzle
      * debugging is enabled, this also compares the client board dumps
      * provided along with each puzzle event.
      */
     protected void applyProgressEvents (int pidx, int[] gevents, Board[] states)
     {
         int size = gevents.length;
         boolean before = compareBeforeApply();
 
         for (int ii = 0, pos = 0; ii < size; ii++) {
             int gevent = gevents[ii];
 
             // if we have state syncing enabled, make sure the board is
             // correct before applying the event
             if (before && (states != null)) {
                 compareBoards(pidx, states[ii], gevent, before);
             }
 
             // apply the event to the player's board
             if (!applyProgressEvent(pidx, gevent)) {
                 Log.warning("Unknown event [puzzle=" + where() +
                     ", pidx=" + pidx + ", event=" + gevent + "].");
             }
 
             // maybe we are comparing boards afterwards
             if (!before && (states != null)) {
                 compareBoards(pidx, states[ii], gevent, before);
             }
         }
     }
 
     /**
      * Compare our server board to the specified sent-back user board.
      */
     protected void compareBoards (int pidx, Board boardstate,
                                   int gevent, boolean before)
     {
         if (DEBUG_PUZZLE) {
             Log.info((before ? "About to apply " : "Just applied ") +
                      "[game=" + _puzobj.which() + ", pidx=" + pidx +
                      ", event=" + gevent + "].");
         }
         if (boardstate == null) {
             if (DEBUG_PUZZLE) {
                 Log.info("No board state provided. Can't compare.");
             }
             return;
         }
         boolean equal = _boards[pidx].equals(boardstate);
         if (!equal) {
             Log.warning("Client and server board states not equal! " +
                         "[game=" + _puzobj.which() +
                         ", type=" + _puzobj.getClass().getName() + "].");
         }
         if (DEBUG_PUZZLE) {
             // if we're debugging, dump the board state every time
             // we're about to apply an event
             _boards[pidx].dumpAndCompare(boardstate);
         }
         if (!equal) {
             if (DEBUG_PUZZLE) {
                 // bail out so that we know something's royally borked
                 System.exit(0);
             } else {
                 // dump the board state since we're not debugging and
                 // didn't just do it above
                 _boards[pidx].dumpAndCompare(boardstate);
             }
         }
     }
 
     /**
      * Called by {@link #updateProgress} to give the server a chance to
      * apply each game event received from the client to the respective
      * player's server-side board and, someday, confirm their validity.
      * Derived classes that make use of the progress updating
      * functionality should be sure to override this method to perform
      * their game-specific event application antics. They should first
      * perform a call to super() to see if the event is handled there.
      *
      * @return true to indicate that the event was handled.
      */
     protected boolean applyProgressEvent (int pidx, int gevent)
     {
         return false;
     }
 
     // documentation inherited
     protected void bodyLeft (int bodyOid)
     {
         super.bodyLeft(bodyOid);
 
         int pidx = IntListUtil.indexOf(_playerOids, bodyOid);
         if (pidx != -1) {
             if (_puzobj.isInPlay() && _puzobj.isActivePlayer(pidx)) {
                 // end the player's game if they bail on an in-progress puzzle
                 endPlayerGame(pidx);
 
             } else if (_puzobj.state == PuzzleObject.AWAITING_PLAYERS &&
                 isPartyGame()) {
                 // handle a player leaving a party game that hasn't yet begun
                 if (removePlayer(getPlayerName(pidx))) {
                     // if they were the creator, choose a new creator
                     if (getPlayerCount() > 0 && _puzobj.creator == pidx) {
                         int npidx = getNextCreator(pidx);
                         _puzobj.setCreator(npidx);
                         // inform occupants of the creator change
                         String message = MessageBundle.tcompose(
                             "m.creator_replaced", getPlayerName(npidx));
                         systemMessage(message);
                     }
                 }
             }
         }
 
         if (_puzobj.state != PuzzleObject.GAME_OVER) {
             // inform remaining users that the user left
             BodyObject user = (BodyObject)CrowdServer.omgr.getObject(bodyOid);
             if (user != null) {
                 systemMessage(MessageBundle.tcompose(
                                   "m.user_left", user.username));
             }
         }
     }
 
     /**
      * Returns the player index of the next feasible creating player
      * following the given player index, or <code>-1</code> if there is no
      * such available player.
      */
     protected int getNextCreator (int pidx)
     {
         int size = getPlayerSlots();
         int npidx = pidx;
         do {
             npidx = (npidx + 1) % size;
         } while (npidx != pidx && !_puzobj.isOccupiedPlayer(npidx));
         return (npidx == pidx) ? -1 : npidx;
     }
 
     /**
      * Called when a player leaves the game in order to determine whether
      * the game should be ended based on its current state, which will
      * include updated player status for the player in question.  The
      * default implementation returns true if the game is in play and
      * there is only one player left.  Derived classes may wish to
      * override this method in order to customize the required end-game
      * conditions.
      */
     protected boolean shouldEndGame ()
     {
         return (_puzobj.isInPlay() && _puzobj.getActivePlayerCount() == 1);
     }
 
     /**
      * Overrides the game manager implementation to mark all active
      * players as winners.  Derived classes may wish to override this
      * method in order to customize the winning conditions.
      */
     protected void assignWinners (boolean[] winners)
     {
         for (int ii = 0; ii < winners.length; ii++) {
             winners[ii] = _puzobj.isActivePlayer(ii);
         }
     }
 
     /**
      * Creates and returns a new starting board for the given player.
      */
     protected abstract Board newBoard (int pidx);
 
     /**
      * Creates and returns a new board summary for the given board.
      * Puzzles that do not make use of board summaries should implement
      * this method and return <code>null</code>.
      */
     protected abstract BoardSummary newBoardSummary (Board board);
 
     // documentation inherited from interface PuzzleGameProvider
     public void updateProgress (ClientObject caller, int roundId, int[] events)
     {
         updateProgressSync(caller, roundId, events, null);
     }
 
     /**
      * Called when the puzzle manager receives a progress update. It
      * checks to make sure that the progress update is valid and the
      * puzzle is still in play and then applies the updates via {@link
      * #applyProgressEvents}.
      */
     public void updateProgressSync (
         ClientObject caller, int roundId, int[] events, Board[] states)
     {
         // determine the caller's player index in the game
         int pidx = IntListUtil.indexOf(_playerOids, caller.getOid());
         if (pidx == -1) {
             Log.warning("Received progress update for non-player?! " +
                         "[game=" + _puzobj.which() + ", who=" + caller.who() +
                         ", ploids=" + StringUtil.toString(_playerOids) + "].");
             return;
         }
 
         // bail if the progress update isn't for the current round
         if (roundId != _puzobj.roundId) {
             // only warn if this isn't a straggling update from the
             // previous round
             if (roundId != _puzobj.roundId-1) {
                 Log.warning("Received progress update for invalid round, " +
                             "not applying [game=" + _puzobj.which() +
                             ", invalidRoundId=" + roundId +
                             ", roundId=" + _puzobj.roundId + "].");
             }
             return;
         }
 
         // if the game is over, we wing straggling updates
         if (!_puzobj.isInPlay()) {
             Log.debug("Ignoring straggling events " +
                       "[game=" + _puzobj.which() +
                       ", user=" + getPlayerName(pidx) +
                       ", events=" + StringUtil.toString(events) + "].");
             return;
         }
 
 //         Log.info("Handling progress events [game=" + _puzobj.which() +
 //                  ", pidx=" + pidx + ", roundId=" + roundId +
 //                  ", count=" + events.length + "].");
 
         // note that we received a progress update from this player
         _lastProgress[pidx] = System.currentTimeMillis();
 
         // apply the progress events to the player's puzzle state
         applyProgressEvents(pidx, events, states);
     }
 
     // documentation inherited
     protected void tick (long tickStamp)
     {
         super.tick(tickStamp);
 
         // every five seconds, we call the inactivity checking code
         if (_puzobj != null && _puzobj.isInPlay() && checkForInactivity()) {
             int pcount = getPlayerSlots();
             for (int ii = 0; ii < pcount && _puzobj.isInPlay(); ii++) {
                 if (!isAI(ii)) {
                     checkPlayerActivity(tickStamp, ii);
                 }
             }
         }
     }
 
     /**
      * Returns whether {@link #checkPlayerActivity} should be called
      * periodically while the game is in play to make sure players are
      * still active.
      */
     protected boolean checkForInactivity ()
     {
         return false;
     }
 
     /**
      * Called periodically for each human player to give puzzles a chance
      * to make sure all such players are engaging in reasonable levels of
      * activity.  The default implementation does naught.
      */
     protected void checkPlayerActivity (long tickStamp, int pidx)
     {
         // nothing for now
     }
 
     /** A casted reference to our puzzle config object. */
     protected PuzzleConfig _puzconfig;
 
     /** A casted reference to our puzzle game object. */
     protected PuzzleObject _puzobj;
 
     /** The player boards. */
     protected Board[] _boards;
 
     /** The client update interval identifier. */
     protected int _uiid = -1;
 
     /** Used to track the last time we received a progress event from each
      * player in this puzzle. */
     protected long[] _lastProgress;
 }
