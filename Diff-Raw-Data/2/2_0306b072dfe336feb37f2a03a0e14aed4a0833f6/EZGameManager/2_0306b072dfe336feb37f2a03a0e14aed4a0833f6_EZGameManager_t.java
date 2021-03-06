 //
 // $Id$
 //
 // Vilya library - tools for developing networked games
 // Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
 // http://www.threerings.net/code/vilya/
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
 
 package com.threerings.ezgame.server;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 
 import com.samskivert.util.ArrayIntSet;
 import com.samskivert.util.ArrayUtil;
 import com.samskivert.util.CollectionUtil;
 import com.samskivert.util.HashIntMap;
 import com.samskivert.util.Interval;
 import com.samskivert.util.IntListUtil;
 import com.samskivert.util.RandomUtil;
 import com.samskivert.util.ResultListener;
 
 import com.threerings.util.Name;
 
 import com.threerings.presents.data.ClientObject;
 import com.threerings.presents.data.InvocationCodes;
 import com.threerings.presents.dobj.AccessController;
 import com.threerings.presents.dobj.DObjectManager;
 import com.threerings.presents.dobj.DSet;
 import com.threerings.presents.dobj.MessageEvent;
 import com.threerings.presents.client.InvocationService;
 import com.threerings.presents.server.InvocationException;
 
 import com.threerings.crowd.data.BodyObject;
 import com.threerings.crowd.data.OccupantInfo;
 import com.threerings.crowd.data.PlaceObject;
 
 import com.threerings.crowd.server.CrowdServer;
 
 import com.threerings.parlor.game.data.GameConfig;
 
 import com.threerings.parlor.game.server.GameManager;
 
 import com.threerings.parlor.turn.server.TurnGameManager;
 
 import com.threerings.ezgame.data.EZGameConfig;
 import com.threerings.ezgame.data.EZGameObject;
 import com.threerings.ezgame.data.EZGameMarshaller;
 import com.threerings.ezgame.data.PropertySetEvent;
 import com.threerings.ezgame.data.UserCookie;
 
 import static com.threerings.ezgame.server.Log.log;
 
 /**
  * A manager for "ez" games.
  */
 public class EZGameManager extends GameManager
     implements EZGameProvider, TurnGameManager
 {
     public EZGameManager ()
     {
         addDelegate(_turnDelegate = new EZGameTurnDelegate(this));
     }
 
     // from TurnGameManager
     public void turnWillStart ()
     {
     }
 
     // from TurnGameManager
     public void turnDidStart ()
     {
     }
 
     // from TurnGameManager
     public void turnDidEnd ()
     {
     }
 
     // from EZGameProvider
     public void endTurn (ClientObject caller, int nextPlayerId,
                          InvocationService.InvocationListener listener)
         throws InvocationException
     {
         validateStateModification(caller, true);
 
         Name nextTurnHolder = null;
         if (nextPlayerId != 0) {
             BodyObject target = getPlayerByOid(nextPlayerId);
             if (target != null) {
                 nextTurnHolder = target.getVisibleName();
             }
         }
 
         _turnDelegate.endTurn(nextTurnHolder);
     }
 
     // from EZGameProvider
     public void endGame (ClientObject caller, int[] winnerOids,
                          InvocationService.InvocationListener listener)
         throws InvocationException
     {
         if (!_gameObj.isInPlay()) {
             throw new InvocationException("e.already_ended");
         }
         validateStateModification(caller, false);
 
         _winnerIds = winnerOids;
         endGame();
     }
 
     // from EZGameProvider
     public void sendMessage (ClientObject caller, String msg, Object data, int playerId,
                              InvocationService.InvocationListener listener)
         throws InvocationException
     {
         validateUser(caller);
 
         if (playerId == 0) {
            _gameObj.postMessage(EZGameObject.USER_MESSAGE, msg, data);
         } else {
             sendPrivateMessage(playerId, msg, data);
         }
     }
 
     // from EZGameProvider
     public void setProperty (ClientObject caller, String propName, Object data, int index,
                              boolean testAndSet, Object testValue,
                              InvocationService.InvocationListener listener)
         throws InvocationException
     {
         validateUser(caller);
         if (testAndSet && !_gameObj.testProperty(propName, index, testValue)) {
             return; // the test failed: do not set the property
         }
         setProperty(propName, data, index);
     }
 
     // from EZGameProvider
     public void getDictionaryLetterSet (ClientObject caller, String locale, int count, 
                                         InvocationService.ResultListener listener)
         throws InvocationException
     {
         DictionaryManager dictionary = getDictionaryManager ();
         dictionary.getLetterSet (locale, count, listener);
     }
     
     // from EZGameProvider
     public void checkDictionaryWord (ClientObject caller, String locale, String word, 
                                      InvocationService.ResultListener listener)
         throws InvocationException
     {
         DictionaryManager dictionary = getDictionaryManager ();
         dictionary.checkWord (locale, word, listener);
     }  
 
     /**
      * Find the dictionary manager
      */
     protected DictionaryManager getDictionaryManager ()
         throws InvocationException
     {
         DictionaryManager dictionary = DictionaryManager.getInstance ();
         if (dictionary == null) {
             log.warning("DictionaryManager not initialized.");
             throw new InvocationException(INTERNAL_ERROR);
         }
         return dictionary;
     }
 
     // from EZGameProvider
     public void addToCollection (ClientObject caller, String collName, byte[][] data,
                                  boolean clearExisting,
                                  InvocationService.InvocationListener listener)
         throws InvocationException
     {
         validateUser(caller);
         if (_collections == null) {
             _collections = new HashMap<String, ArrayList<byte[]>>();
         }
 
         // figure out if we're adding to an existing collection or creating a new one
         ArrayList<byte[]> list = null;
         if (!clearExisting) {
             list = _collections.get(collName);
         }
         if (list == null) {
             list = new ArrayList<byte[]>();
             _collections.put(collName, list);
         }
 
         CollectionUtil.addAll(list, data);
     }
 
     // from EZGameProvider
     public void getFromCollection (ClientObject caller, String collName, boolean consume, int count,
                                    String msgOrPropName, int playerId,
                                    InvocationService.ConfirmListener listener)
         throws InvocationException
     {
         validateUser(caller);
 
         int srcSize = 0;
         if (_collections != null) {
             ArrayList<byte[]> src = _collections.get(collName);
             srcSize = (src == null) ? 0 : src.size();
             if (srcSize >= count) {
                 byte[][] result = new byte[count][];
                 for (int ii=0; ii < count; ii++) {
                     int pick = RandomUtil.getInt(srcSize);
                     if (consume) {
                         result[ii] = src.remove(pick);
                         srcSize--;
 
                     } else {
                         result[ii] = src.get(pick);
                     }
                 }
 
                 if (playerId == 0) {
                     setProperty(msgOrPropName, result, -1);
                 } else {
                     sendPrivateMessage(playerId, msgOrPropName, result);
                 }
                 listener.requestProcessed(); // SUCCESS!
                 return;
             }
         }
         
         // TODO: decide what we want to return here
         throw new InvocationException(String.valueOf(srcSize));
     }
     
     // from EZGameProvider
     public void mergeCollection (ClientObject caller, String srcColl, String intoColl,
                                  InvocationService.InvocationListener listener)
         throws InvocationException
     {
         validateUser(caller);
 
         // non-existent collections are treated as empty, so if the source doesn't exist, we
         // silently accept it
         if (_collections != null) {
             ArrayList<byte[]> src = _collections.remove(srcColl);
             if (src != null) {
                 ArrayList<byte[]> dest = _collections.get(intoColl);
                 if (dest == null) {
                     _collections.put(intoColl, src);
                 } else {
                     dest.addAll(src);
                 }
             }
         }
     }
 
     // from EZGameProvider
     public void setTicker (ClientObject caller, String tickerName, int msOfDelay,
                            InvocationService.InvocationListener listener)
         throws InvocationException
     {
         validateUser(caller);
 
         Ticker t;
         if (msOfDelay >= MIN_TICKER_DELAY) {
             if (_tickers != null) {
                 t = _tickers.get(tickerName);
             } else {
                 _tickers = new HashMap<String, Ticker>();
                 t = null;
             }
 
             if (t == null) {
                 if (_tickers.size() >= MAX_TICKERS) {
                     throw new InvocationException(ACCESS_DENIED);
                 }
                 t = new Ticker(tickerName, _gameObj);
                 _tickers.put(tickerName, t);
             }
             t.start(msOfDelay);
 
         } else if (msOfDelay <= 0) {
             if (_tickers != null) {
                 t = _tickers.remove(tickerName);
                 if (t != null) {
                     t.stop();
                 }
             }
 
         } else {
             throw new InvocationException(ACCESS_DENIED);
         }
     }
 
     // from EZGameProvider
     public void getCookie (ClientObject caller, final int playerId,
                            InvocationService.InvocationListener listener)
         throws InvocationException
     {
         GameCookieManager gcm = getCookieManager();
         if (_gameObj.userCookies.containsKey(playerId)) {
             // already loaded: we do nothing
             return;
         }
 
         if (_cookieLookups == null) {
             _cookieLookups = new ArrayIntSet();
         }
         // we only start looking up the cookie if nobody else already is
         if (!_cookieLookups.contains(playerId)) {
             BodyObject body = getOccupantByOid(playerId);
             if (body == null) {
                 log.fine("getCookie() called with invalid occupant [occupantId=" + playerId + "].");
                 throw new InvocationException(INTERNAL_ERROR);
             }
 
             gcm.getCookie(getPersistentGameId(), body, new ResultListener<byte[]>() {
                 public void requestCompleted (byte[] result) {
                     // Result may be null: that's ok, it means we've looked up the user's
                     // nonexistant cookie.  Only set the cookie if the playerIndex is still in the
                     // lookup set, otherwise they left!
                     if (_cookieLookups.remove(playerId) && _gameObj.isActive()) {
                         _gameObj.addToUserCookies(new UserCookie(playerId, result));
                     }
                 }
 
                 public void requestFailed (Exception cause) {
                     log.warning("Unable to retrieve cookie [cause=" + cause + "].");
                     requestCompleted(null);
                 }
             });
 
             // indicate that we're looking up a cookie
             _cookieLookups.add(playerId);
         }
     }
 
     // from EZGameProvider
     public void setCookie (ClientObject caller, byte[] value,
                            InvocationService.InvocationListener listener)
         throws InvocationException
     {
         validateUser(caller);
 
         GameCookieManager gcm = getCookieManager();
         UserCookie cookie = new UserCookie(caller.getOid(), value);
         if (_gameObj.userCookies.containsKey(cookie.getKey())) {
             _gameObj.updateUserCookies(cookie);
         } else {
             _gameObj.addToUserCookies(cookie);
         }
 
         gcm.setCookie(getPersistentGameId(), caller, value);
     }
 
     /**
      * Get the cookie manager, and do a bit of other setup.
      */
     protected GameCookieManager getCookieManager ()
         throws InvocationException
     {
         GameCookieManager gcm = GameCookieManager.getInstance();
         if (gcm == null) {
             log.warning("GameCookieManager not initialized.");
             throw new InvocationException(INTERNAL_ERROR);
         }
 
         if (_gameObj.userCookies == null) {
             // lazy-init this
             _gameObj.setUserCookies(new DSet<UserCookie>());
         }
         return gcm;
     }
 
     /**
      * Helper method to send a private message to the specified player oid (must already be
      * verified).
      */
     protected void sendPrivateMessage (
         int playerId, String msg, Object data)
         throws InvocationException
     {
         BodyObject target = getPlayerByOid(playerId);
         if (target == null) {
             // TODO: this code has no corresponding translation
             throw new InvocationException("m.player_not_around");
         }
 
         target.postMessage(EZGameObject.USER_MESSAGE + ":" + _gameObj.getOid(),
                            new Object[] { msg, data });
     }
 
     /**
      * Helper method to post a property set event.
      */
     protected void setProperty (String propName, Object value, int index)
     {
         // apply the property set immediately
         Object oldValue = _gameObj.applyPropertySet(propName, value, index);
         _gameObj.postEvent(
             new PropertySetEvent(_gameObj.getOid(), propName, value, index, oldValue));
     }
 
     /**
      * Get the game id of this ezgame, as set in the config.
      */
     protected int getPersistentGameId ()
         throws InvocationException
     {
         int id = ((EZGameConfig) _config).persistentGameId;
         if (id == 0) {
             throw new InvocationException("Persistent game id not set.");
         }
         return id;
     }
 
     /**
      * Validate that the specified user has access to do things in the game.
      */
     protected void validateUser (ClientObject caller)
         throws InvocationException
     {
         switch (getGameType()) {
         case GameConfig.PARTY:
             return; // always validate.
 
         default:
             if (getPresentPlayerIndex(caller.getOid()) == -1) {
                 throw new InvocationException(InvocationCodes.ACCESS_DENIED);
             }
             return;
         }
     }
 
     /**
      * Validate that the specified listener has access to make a change.
      */
     protected void validateStateModification (ClientObject caller, boolean requireHoldsTurn)
         throws InvocationException
     {
         validateUser(caller);
 
         if (requireHoldsTurn) {
             Name holder = _gameObj.turnHolder;
             if (holder != null && !holder.equals(((BodyObject) caller).getVisibleName())) {
                 throw new InvocationException(InvocationCodes.ACCESS_DENIED);
             }
         }
     }
 
     /**
      * Get the specified player body by Oid.
      */
     protected BodyObject getPlayerByOid (int oid)
     {
         // verify that they're a player
         switch (getGameType()) {
         case GameConfig.PARTY:
             // all occupants are players in a party game
             break;
 
         default:
             if (!IntListUtil.contains(_playerOids, oid)) {
                 return null; // not a player!
             }
             break;
         }
 
         return getOccupantByOid(oid);
     }
 
     /**
      * Get the specified occupant body by Oid.
      */
     protected BodyObject getOccupantByOid (int oid)
     {
         if (!_gameObj.occupants.contains(oid)) {
             return null;
         }
         // return the body
         return (BodyObject) CrowdServer.omgr.getObject(oid);
     }
 
     @Override
     protected PlaceObject createPlaceObject ()
     {
         return new EZGameObject();
     }
 
     @Override
     protected void didStartup ()
     {
         super.didStartup();
 
         _gameObj = (EZGameObject) _plobj;
         _gameObj.setEzGameService(
             (EZGameMarshaller) CrowdServer.invmgr.registerDispatcher(new EZGameDispatcher(this)));
 
         // if we don't need the no-show timer, start.
         if (!needsNoShowTimer()) {
             startGame();
         }
     }
 
     @Override // from PlaceManager
     protected void bodyEntered (int bodyOid)
     {
         super.bodyEntered(bodyOid);
 
         // if we have no controller, then our new friend gets control
         if (_gameObj.controllerOid == 0) {
             _gameObj.setControllerOid(bodyOid);
         }
     }
 
     @Override // from PlaceManager
     protected void bodyUpdated (OccupantInfo info)
     {
         super.bodyUpdated(info);
 
         // if the controller just disconnected, reassign control
         if (info.status == OccupantInfo.DISCONNECTED && info.bodyOid == _gameObj.controllerOid) {
             _gameObj.setControllerOid(getControllerOid());
 
         // if everyone in the room was disconnected and this client just reconnected, it becomes
         // the new controller
         } else if (_gameObj.controllerOid == 0) {
             _gameObj.setControllerOid(info.bodyOid);
         }
     }
 
     @Override // from PlaceManager
     protected void bodyLeft (int bodyOid)
     {
         super.bodyLeft(bodyOid);
 
         // if this player was the controller, reassign control
         if (bodyOid == _gameObj.controllerOid) {
             _gameObj.setControllerOid(getControllerOid());
         }
     }
 
     @Override
     protected void didShutdown ()
     {
         CrowdServer.invmgr.clearDispatcher(_gameObj.ezGameService);
         stopTickers();
 
         super.didShutdown();
     }
 
     @Override
     protected void gameDidEnd ()
     {
         stopTickers();
 
         super.gameDidEnd();
     }
 
     @Override
     protected void playerGameDidEnd (int pidx)
     {
         super.playerGameDidEnd(pidx);
 
         // kill any of their cookies
         if (_gameObj.userCookies != null && _gameObj.userCookies.containsKey(pidx)) {
             _gameObj.removeFromUserCookies(pidx);
         }
         // halt the loading of their cookie, if in progress
         if (_cookieLookups != null) {
             _cookieLookups.remove(pidx);
         }
     }
 
     @Override
     protected void assignWinners (boolean[] winners)
     {
         if (_winnerIds != null) {
             for (int oid : _winnerIds) {
                 int index = IntListUtil.indexOf(_playerOids, oid);
                 if (index >= 0 && index < winners.length) {
                     winners[index] = true;
                 }
             }
             _winnerIds = null;
         }
     }
 
     /**
      * Stop and clear all tickers.
      */
     protected void stopTickers ()
     {
         if (_tickers != null) {
             for (Ticker ticker : _tickers.values()) {
                 ticker.stop();
             }
             _tickers = null;
         }
     }
 
     /**
      * Returns the oid of a player to whom to assign control of the game or zero if no players
      * qualify for control.
      */
     protected int getControllerOid ()
     {
         for (OccupantInfo info : _gameObj.occupantInfo) {
             if (info.status != OccupantInfo.DISCONNECTED) {
                 return info.bodyOid;
             }
         }
         return 0;
     }
 
     /**
      * A timer that fires message events to a game.
      */
     protected static class Ticker
     {
         /**
          * Create a Ticker.
          */
         public Ticker (String name, EZGameObject gameObj)
         {
             _name = name;
             // once we are constructed, we want to avoid calling methods on dobjs.
             _oid = gameObj.getOid();
             _omgr = gameObj.getManager();
         }
 
         public void start (int msOfDelay)
         {
             _value = 0;
             _interval.schedule(0, msOfDelay);
         }
 
         public void stop ()
         {
             _interval.cancel();
         }
 
         /**
          * The interval that does our work. Note well that this is not a 'safe' interval that
          * operates using a RunQueue.  This interval instead does something that we happen to know
          * is safe for any thread: posting an event to the dobj manager.  If we were using a
          * RunQueue it would be the same event queue and we would be posted there, wait our turn,
          * and then do the same thing: post this event. We just expedite the process.
          */
         protected Interval _interval = new Interval() {
             public void expired () {
                 _omgr.postEvent(
                     new MessageEvent(_oid, EZGameObject.TICKER, new Object[] { _name, _value++ }));
             }
         };
 
         protected int _oid;
         protected DObjectManager _omgr;
         protected String _name;
         protected int _value;
     } // End: static class Ticker
 
     /** A nice casted reference to the game object. */
     protected EZGameObject _gameObj;
 
     /** Our turn delegate. */
     protected EZGameTurnDelegate _turnDelegate;
 
     /** The map of collections, lazy-initialized. */
     protected HashMap<String, ArrayList<byte[]>> _collections;
 
     /** The map of tickers, lazy-initialized. */
     protected HashMap<String, Ticker> _tickers;
 
     /** Tracks which cookies are currently being retrieved from the db. */
     protected ArrayIntSet _cookieLookups;
 
 //    /** User tokens, lazy-initialized. */
 //    protected HashIntMap<HashSet<String>> _tokens;
 
     /** The array of winner oids, after the user has filled it in. */
     protected int[] _winnerIds;
 
     /** The minimum delay a ticker can have. */
     protected static final int MIN_TICKER_DELAY = 50;
 
     /** The maximum number of tickers allowed at one time. */
     protected static final int MAX_TICKERS = 3;
 }
