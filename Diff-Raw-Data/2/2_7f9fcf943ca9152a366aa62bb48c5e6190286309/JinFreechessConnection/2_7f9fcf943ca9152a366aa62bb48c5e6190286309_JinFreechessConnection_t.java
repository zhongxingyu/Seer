 /**
  * Jin - a chess client for internet chess servers.
  * More information is available at http://www.jinchess.com/.
  * Copyright (C) 2002, 2003 Alexander Maryanovsky.
  * All rights reserved.
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 
 package free.jin.freechess;
 
 import free.chess.*;
 import free.chess.variants.BothSidesCastlingVariant;
 import free.chess.variants.NoCastlingVariant;
 import free.chess.variants.atomic.Atomic;
 import free.chess.variants.bughouse.Bughouse;
 import free.chess.variants.fischerrandom.FischerRandom;
 import free.chess.variants.suicide.Suicide;
 import free.chessclub.ChessclubConnection;
 import free.freechess.*;
 import free.jin.*;
 import free.jin.event.*;
 import free.jin.freechess.event.IvarStateChangeEvent;
 import free.util.Pair;
 import free.util.TextUtilities;
 
 import javax.swing.*;
 import java.io.IOException;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 
 /**
  * An implementation of the JinConnection interface for the freechess.org
  * server.
  */
 
 public class JinFreechessConnection extends FreechessConnection implements Connection,
     SeekConnection, PGNConnection, ChannelsConnection, MessagesConnection, BughouseConnection{
 
 //TODO add implementation of FreechessChannelsConnection.
 //TODO add methods and regular expressions to handle channel list events.
 
     /**
     * Overrides getConnectionName() method returning name of connection. whp 2006
     */
     
     public String getConnectionName(){
         return "FreechessConnection";
     }
 
     /**
      * Boolean indicating whether this connection's method waits for game info from
      * processing line about examined game.
      */
 
     private boolean waiting = false;
 
     /**
      * The examined game Style12Struct.
      */
 
     private Style12Struct examinedGameBoardData;
 
     /**
      * The number of examined game we are willing to gather information about.
      */
 
     private int examinedGameId;
 
     /**
      * The variant of examined game.
      */
 
     private String  examinedGameVariant;
 
     /**
      * Boolean telling whether examined game is rated.
      */
     private boolean isExaminedRated;
 
     /**
      * Boolean telling whether examined game is private.
      */
 
     private boolean isExaminedPrivate;
 
   /**
    * Our listener manager.
    */
 
   private final FreechessListenerManager listenerManager = new FreechessListenerManager(this);
 
 
 
   /**
    * Creates a new JinFreechessConnection with the specified hostname, port,
    * requested username and password.
    */
 
   public JinFreechessConnection(String requestedUsername, String password){
     super(requestedUsername, password, System.out);
 
     setInterface(Jin.getInstance().getAppName() + ' ' + Jin.getInstance().getAppVersion() +
       " (" + System.getProperty("java.vendor") + ' ' + System.getProperty("java.version") +
       ", " + System.getProperty("os.name") + ' ' + getSafeOSVersion() + ')');
 
     setStyle(12);
 
     setIvarState(Ivar.GAMEINFO, true);
     setIvarState(Ivar.SHOWOWNSEEK, true);
     setIvarState(Ivar.PENDINFO, true);
     setIvarState(Ivar.MOVECASE, true);
     // setIvarState(Ivar.COMPRESSMOVE, true); Pending DAV's bugfixing spree
     setIvarState(Ivar.LOCK, true);
   }
 
     /**
      * <code>sendCommand()</code> method overriden
      * to get rid of bug [ 1642877 ] Non-channel numbers list pass to channel manager
      */
 
     public void sendCommand(String command){
         super.sendCommand(command);
         userChannelListNext = false;
     }
   
   
   
   /**
    * Returns the OS version after stripping out the patch level from it.
    * We do this to avoid revealing that information to everyone on the server.
    */
   
   private static String getSafeOSVersion(){
     String osVersion = System.getProperty("os.version");
     int i  = osVersion.indexOf(".", osVersion.indexOf(".") + 1);
     if (i != -1)
       osVersion = osVersion.substring(0, i) + ".x";
     
     return osVersion;
   }
 
 
 
 
   /**
    * Returns a Player object corresponding to the specified string. If the
    * string is "W", returns <code>Player.WHITE</code>. If it's "B", returns
    * <code>Player.BLACK</code>. Otherwise, throws an IllegalArgumentException.
    */
 
   public static Player playerForString(String s){
     if (s.equals("B"))
       return Player.BLACK_PLAYER;
     else if (s.equals("W"))
       return Player.WHITE_PLAYER;
     else
       throw new IllegalArgumentException("Bad player string: "+s);
   }
 
 
 
 
 
   /**
    * Returns our ListenerManager.
    */
 
   public ListenerManager getListenerManager(){
     return getFreechessListenerManager();
   }
 
 
 
 
   /**
    * Returns out ListenerManager as a reference to FreechessListenerManager.
    */
 
   public FreechessListenerManager getFreechessListenerManager(){
     return listenerManager;
   }
   
   
   
   /**
    * Fires an "attempting" connection event and invokes {@link free.util.Connection#initiateConnect(String, int)}.
    */
   
   public void initiateConnectAndLogin(String hostname, int port){
     listenerManager.fireConnectionAttempted(this, hostname, port);
 
     initiateConnect(hostname, port);
   }
   
   
   
   /**
    * Fires an "established" connection event.
    */
   
   protected void handleConnected(){
     listenerManager.fireConnectionEstablished(this);
     
     super.handleConnected();
   }
   
   
   
   /**
    * Fires a "failed" connection event.
    */
   
   protected void handleConnectingFailed(IOException e){
     listenerManager.fireConnectingFailed(this, e.getMessage());
     
     super.handleConnectingFailed(e);
   }
   
   
   
   /**
    * Fires a "login succeeded" connection event and performs other on-login tasks.
    */
   
   protected void handleLoginSucceeded(){
     super.handleLoginSucceeded();
     
     sendCommand("$set bell 0");
     filterLine("Bell off.");
     
     listenerManager.fireLoginSucceeded(this);
   }
   
   
   
   /**
    * Fires a "login failed" connection event.
    */
   
   protected void handleLoginFailed(String reason){
     listenerManager.fireLoginFailed(this, reason);
     
     super.handleLoginFailed(reason);
   }
   
   
   
   /**
    * Fires a "connection lost" connection event.
    */
   
   protected void handleDisconnection(IOException e){
     listenerManager.fireConnectionLost(this);
     
     super.handleDisconnection(e);
   }
 
 
 
   /**
    * Overrides {@link free.util.Connection#connectImpl(String, int)} to return a timesealing socket.
    */
 
   protected Socket connectImpl(String hostname, int port) throws IOException{
     // Comment this to disable timesealing
     return new free.freechess.timeseal.TimesealingSocket(hostname, port);
     
     // Comment this to enable timesealing
     // return new Socket(hostname, port);
   }
 
 
 
   /**
    * Notifies any interested PlainTextListener of the received line of otherwise
    * unidentified text.
    */
 
   protected void processLine(String line){
     listenerManager.firePlainTextEvent(new PlainTextEvent(this, line));
   }
   
   
   
   /**
    * Gets called when the server notifies us of a change in the state of some
    * ivar.
    */
    
   protected boolean processIvarStateChanged(Ivar ivar, boolean state){
     IvarStateChangeEvent evt = new IvarStateChangeEvent(this, ivar, state);
     
     listenerManager.fireIvarStateChangeEvent(evt);
     
     return false;
   }
   
 
 
   /**
    * Fires an appropriate ChatEvent.
    */
 
   protected boolean processPersonalTell(String username, String titles, String message){
     listenerManager.fireChatEvent(new ChatEvent(this, "tell", ChatEvent.PERSON_TO_PERSON_CHAT_CATEGORY,
       username, (titles == null ? "" : titles), -1, message, null));
 
     return true;
   }
 
 
 
   /**
    * Fires an appropriate ChatEvent.
    */
 
   protected boolean processSayTell(String username, String titles, int gameNumber, String message){
     listenerManager.fireChatEvent(new ChatEvent(this, "say", ChatEvent.PERSON_TO_PERSON_CHAT_CATEGORY,
       username, (titles == null ? "" : titles), -1, message, new Integer(gameNumber)));
 
     return true;
   }
 
 
 
 
   /**
    * Fires an appropriate ChatEvent.
    */
 
   protected boolean processPTell(String username, String titles, String message){
     listenerManager.fireChatEvent(new ChatEvent(this, "ptell", ChatEvent.PERSON_TO_PERSON_CHAT_CATEGORY,
       username, (titles == null ? "" : titles), -1, message, null));
 
     return true;
   }
 
 
 
 
   /**
    * Fires an appropriate ChatEvent.
    */
 
   protected boolean processChannelTell(String username, String titles, int channelNumber, 
       String message){
 
     listenerManager.fireChatEvent(new ChatEvent(this, "channel-tell", ChatEvent.ROOM_CHAT_CATEGORY,
       username, (titles == null ? "" : titles), -1, message, new Integer(channelNumber)));
 
     return true;
   }
 
 
 
 
   /**
    * Fires an appropriate ChatEvent.
    */
 
   protected boolean processKibitz(String username, String titles, int rating, int gameNumber,
       String message){
 
     if (titles == null)
       titles = "";
 
     listenerManager.fireChatEvent(new ChatEvent(this, "kibitz", ChatEvent.GAME_CHAT_CATEGORY,
       username, titles, rating, message, new Integer(gameNumber)));
 
     return true;
   }
 
 
 
 
   /**
    * Fires an appropriate ChatEvent.
    */
 
   protected boolean processWhisper(String username, String titles, int rating, int gameNumber,
       String message){
     if (titles == null)
       titles = "";
 
     listenerManager.fireChatEvent(new ChatEvent(this, "whisper", ChatEvent.GAME_CHAT_CATEGORY,
       username, titles, rating, message, new Integer(gameNumber)));
 
     return true;
   }
 
 
 
   /**
    * Regex for matching tourney tell qtells.
    */
 
   private static final Pattern TOURNEY_TELL_REGEX =
      Pattern.compile("^("+USERNAME_REGEX+")("+TITLES_REGEX+")?\\(T(\\d+)\\): (.*)");
 
 
 
   /**
    * Fires an appropriate ChatEvent.
    */
 
   protected boolean processQTell(String message){
     ChatEvent evt;
     Matcher matcher = TOURNEY_TELL_REGEX.matcher(message);
     if (matcher.matches()){
       String sender = matcher.group(1);
       String title = matcher.group(2);
       if (title == null)
         title = "";
       Integer tourneyIndex = new Integer(matcher.group(3));
       message = matcher.group(4);
       evt = new ChatEvent(this, "qtell.tourney", ChatEvent.TOURNEY_CHAT_CATEGORY,
         sender, title, -1, message, tourneyIndex);
     }
     else{
       evt = new ChatEvent(this, "qtell", ChatEvent.PERSON_TO_PERSON_CHAT_CATEGORY,
         null, null, -1, message, null);
     }
 
     listenerManager.fireChatEvent(evt);
 
     return true;
   }
 
 
 
 
   /**
    * Fires an appropriate ChatEvent.
    */
 
   protected boolean processShout(String username, String titles, String message){
     listenerManager.fireChatEvent(new ChatEvent(this, "shout", ChatEvent.ROOM_CHAT_CATEGORY,
       username, (titles == null ? "" : titles), -1, message, null));
 
     return true;
   }
 
 
 
 
   /**
    * Fires an appropriate ChatEvent.
    */
 
   protected boolean processIShout(String username, String titles, String message){
     listenerManager.fireChatEvent(new ChatEvent(this, "ishout", ChatEvent.ROOM_CHAT_CATEGORY, 
       username, (titles == null ? "" : titles), -1, message, null));
 
     return true;
   }
 
 
 
 
   /**
    * Fires an appropriate ChatEvent.
    */
 
   protected boolean processTShout(String username, String titles, String message){
     listenerManager.fireChatEvent(new ChatEvent(this, "tshout", ChatEvent.TOURNEY_CHAT_CATEGORY,
       username, (titles == null ? "" : titles), -1, message, null));
 
     return true;
   }
 
 
 
 
   /**
    * Fires an appropriate ChatEvent.
    */
 
   protected boolean processCShout(String username, String titles, String message){
     listenerManager.fireChatEvent(new ChatEvent(this, "cshout", ChatEvent.ROOM_CHAT_CATEGORY,
       username, (titles == null ? "" : titles), -1, message, null));
 
     return true;
   }
 
 
 
 
   /**
    * Fires an appropriate ChatEvent.
    */
 
   protected boolean processAnnouncement(String username, String message){
     listenerManager.fireChatEvent(new ChatEvent(this, "announcement", ChatEvent.BROADCAST_CHAT_CATEGORY, 
       username, "", -1, message, null));
 
     return true;
   }
 
 
   /**
    * Fires an appropriate ChannelsEvent.
    */
 
   protected boolean processChannelListChanged(String change, int channelNumber){
     if (change.equals("added")){
         int[] channelsNumbers = {999};
       listenerManager.fireChannelsEvent(new ChannelsEvent(this, ChannelsEvent.CHANNEL_ADDED, channelNumber, channelsNumbers));
     }
 
     if (change.equals("removed")){
         int[] channelsNumbers = {999};
         listenerManager.fireChannelsEvent(new ChannelsEvent(this, ChannelsEvent.CHANNEL_REMOVED, channelNumber, channelsNumbers));
     }
       
     return false;
   }
 
   /**
    * Field that states wether next line is user's channel list.
    */
 
    boolean userChannelListNext = true;
 
   /**
    * Assures wether the next line is user's channel list.
    */
 
    protected boolean processUserChannelListNext(String userName){
         if (!userName.equals(Jin.getInstance().getConnManager().getSession().getUser().getUsername())){
             this.userChannelListNext = false;
             
         }
         else{
         	this.userChannelListNext = true;
         }
         
         if (userName.equals("ok")){
             this.userChannelListNext = true;
             
         }
         else{
             this.userChannelListNext = false;
         }
         if (!this.fromPlugin){
             return false;
         }
         else {
             
             return true;
         }
         
         
    }
 
   /**
    * Fires an appropriate @{link}ChannelsEvent.
    */
 
    protected boolean processChannelListArrives(String channelsNumbers){
        //System.out.println(">>>USER_CHANNEL_LIST_NEXT = " + this.userChannelListNext);
         if (userChannelListNext == false){
             return false;
         }
         else{
             int channelNumber = 999;
             String[] channelsNumbersStrings = channelsNumbers.split("\\s{1,4}");
             int[] channels = new int[channelsNumbersStrings.length];
             for (int i = 0; i < channelsNumbersStrings.length; i++){
                 //System.out.println("#" + i + " CHANNEL = " + channelsNumbersStrings[i]);
                 channels[i] = Integer.parseInt(channelsNumbersStrings[i]);
             }
 
             listenerManager.fireChannelsEvent(new ChannelsEvent(this, ChannelsEvent.USER_CHANNEL_LIST_RECEIVED, channelNumber, channels));
             
             //this.userChannelListNext = false;
         }
         if (this.fromPlugin == false){
             return false;
         }
         else {
 
             return true;
         }
    }
    
    /**
     * Fires an appropriate @{link}MessageEvent (the one with information about unread messages number).
     */
    
    protected boolean processMessagesInfo(int allMessages, int unreadMessages){
        System.out.println("YOU HAVE UNREAD MESSAGES. " + unreadMessages + " that is.");
        return false;
    }
    
    /**
     * Fires an appropriate @{link}MessageEvent (message's content, sender, date and number in messages list).
     */
    
    protected boolean processMessages(int number, String user, String date, String content){
        System.out.println("Message received:" + content);
        return false;
    }
 
   /*;
    * Returns the wild variant corresponding to the given server wild variant 
    * name/category name, or <code>null</code> if that category is not supported. 
    */
 
   private static WildVariant getVariant(String categoryName){
     if (categoryName.equalsIgnoreCase("lightning") ||
         categoryName.equalsIgnoreCase("blitz") ||
         categoryName.equalsIgnoreCase("standard") ||
         categoryName.equalsIgnoreCase("untimed"))
       return Chess.getInstance();
 
     
     if (categoryName.startsWith("wild/")){
       String wildId = categoryName.substring("wild/".length());
       if (wildId.equals("0") || wildId.equals("1"))
         return new BothSidesCastlingVariant(Chess.INITIAL_POSITION_FEN, categoryName);
       else if (wildId.equals("2") || wildId.equals("3"))
         return new NoCastlingVariant(Chess.INITIAL_POSITION_FEN, categoryName);
       else if (wildId.equals("5") || wildId.equals("8") || wildId.equals("8a"))
         return new ChesslikeGenericVariant(Chess.INITIAL_POSITION_FEN, categoryName);
       else if (wildId.equals("fr"))
         return FischerRandom.getInstance();
     }
     else if (categoryName.equals("pawns/pawns-only"))
         return new ChesslikeGenericVariant(Chess.INITIAL_POSITION_FEN, categoryName);
     else if (categoryName.equals("nonstandard"))
         return new ChesslikeGenericVariant(Chess.INITIAL_POSITION_FEN, categoryName);
     else if (categoryName.equals("suicide"))
       return Suicide.getInstance();
     else if (categoryName.equals("losers"))
       return new ChesslikeGenericVariant(Chess.INITIAL_POSITION_FEN, categoryName);
     else if (categoryName.equals("atomic"))
       return Atomic.getInstance();
     else if (categoryName.equals("bughouse") || categoryName.equals("crazyhouse")){
         return Bughouse.getInstance();
     }
 
     // This means it's a fake variant we're using because the server hasn't told us the real one.
     else if (categoryName.equals("Unknown variant"))
       return Chess.getInstance();
 
     return null;
   }
   
   
  
   
   /**
    * Returns the wild variant name corresponding to the specified wild variant,
    * that can be used for issuing a seek, e.g. "w1" or "fr".
    * Returns null if the specified wild variant is not supported by FICS.
    */
    
   private String getWildName(WildVariant variant){
     if (variant == null)
       throw new IllegalArgumentException("Null variant");
 
     String variantName = variant.getName();
 
     if (variantName.startsWith("wild/"))
       return "w" + variantName.substring("wild/".length());
     else if (variant.equals(Chess.getInstance()))
       return "";
     else if (variant.equals(FischerRandom.getInstance()))
       return "fr";
     else if (variant.equals(Suicide.getInstance()))
       return "suicide";
     else if (variant.equals(Atomic.getInstance()))
       return "atomic";
     else if ("losers".equals(variantName))
       return "losers";
     else if (variantName.startsWith("uwild"))
       return "uwild" + variantName.substring("uwild".length());
     else if (variantName.startsWith("pawns"))
       return "pawns" + variantName.substring("pawns".length());
     else if (variantName.startsWith("odds"))
       return "odds" + variantName.substring("odds".length());
     else if (variantName.startsWith("misc"))
       return "misc" + variantName.substring("misc".length());
     else if (variantName.startsWith("openings"))
       return "openings" + variantName.substring("openings".length());
     
     return null;
   }
   
 
 
   /**
    * A list of supported wild variants, initialized lazily.
    */
    
   private static WildVariant [] wildVariants;
   
   
   //TODO: Describe all the variants
   /**
    * Returns a list of support wild variants.
    */
    
   public WildVariant [] getSupportedVariants(){
     if (wildVariants == null){
       wildVariants = new WildVariant[]{
         Chess.getInstance(),
         FischerRandom.getInstance(),
         Suicide.getInstance(),
         Atomic.getInstance(),
         new ChesslikeGenericVariant(Chess.INITIAL_POSITION_FEN, "losers"),
         new BothSidesCastlingVariant(Chess.INITIAL_POSITION_FEN, "wild/0"),
         new BothSidesCastlingVariant(Chess.INITIAL_POSITION_FEN, "wild/1"),
         new NoCastlingVariant(Chess.INITIAL_POSITION_FEN, "wild/2"),
         new NoCastlingVariant(Chess.INITIAL_POSITION_FEN, "wild/3"),
         new ChesslikeGenericVariant(Chess.INITIAL_POSITION_FEN, "wild/5"),
         new ChesslikeGenericVariant(Chess.INITIAL_POSITION_FEN, "wild/8"),
         new ChesslikeGenericVariant(Chess.INITIAL_POSITION_FEN, "wild/8a"),
         new ChesslikeGenericVariant(Chess.INITIAL_POSITION_FEN, "misc little-game"),
         new ChesslikeGenericVariant(Chess.INITIAL_POSITION_FEN, "pawns pawns-only"),
         new ChesslikeGenericVariant(Chess.INITIAL_POSITION_FEN, "odds"),
         new ChesslikeGenericVariant(Chess.INITIAL_POSITION_FEN, "openings"),
         new ChesslikeGenericVariant(Chess.INITIAL_POSITION_FEN, "uwild"),
       };
     }
     
     return (WildVariant [])wildVariants.clone();
   }
 
   
 
   /**
    * A HashMap where we keep game numbers mapped to GameInfoStruct objects
    * of games that haven't started yet.
    */
 
   private final HashMap unstartedGamesData = new HashMap(1);
 
 
 
 
   /**
    * Maps game numbers to InternalGameData objects of ongoing games.
    */
 
   private final HashMap ongoingGamesData = new HashMap(5);
 
 
 
 
 
   /**
    * A HashMap mapping Game objects to ArrayLists of moves which were sent for
    * these games but the server didn't tell us yet whether the move is legal
    * or not.
    */
 
   private final HashMap unechoedMoves = new HashMap(1);
 
 
 
 
   /**
    * A list of game numbers of ongoing games which we can't support for some
    * reason (not a supported variant for example).
    */
 
   private final ArrayList unsupportedGames = new ArrayList();
 
 
 
   /**
    * The user's primary played (by the user) game, -1 if unknown. This is only
    * set when the user is playing more than one game.
    */
 
   private int primaryPlayedGame = -1;
 
 
 
   /**
    * The user's primary observed game, -1 if unknown. This is only set when
    * the user is observing more than one game.
    */
 
   private int primaryObservedGame = -1;
   
 
 
   /**
    * Returns the game with the specified number.
    * This method (currently) exists solely for the benefit of the arrow/circle
    * script.
    */
 
   public Game getGame(int gameNumber) throws NoSuchGameException{
     return getGameData(gameNumber).game;
   }
 
 
 
   /**
    * Returns the InternalGameData for the ongoing game with the specified
    * number. Throws a <code>NoSuchGameException</code> if there's no such game.
    */
 
   private InternalGameData getGameData(int gameNumber) throws NoSuchGameException{
     InternalGameData gameData = (InternalGameData)ongoingGamesData.get(new Integer(gameNumber));
     if (gameData == null)
       throw new NoSuchGameException();
 
     return gameData;
   }
 
 
 
   /**
    * Finds the (primary) game played by the user. Throws a
    * <code>NoSuchGameException</code> if there's no such game.
    */
 
   private InternalGameData findMyGame() throws NoSuchGameException{
     if (primaryPlayedGame != -1)
       return getGameData(primaryPlayedGame);
 
     Iterator gameNumbers = ongoingGamesData.keySet().iterator();
     while (gameNumbers.hasNext()){
       Integer gameNumber = (Integer)gameNumbers.next();
       InternalGameData gameData = (InternalGameData)ongoingGamesData.get(gameNumber);
       Game game = gameData.game;
       if (game.getGameType() == Game.MY_GAME)
         return gameData;
     }
 
     throw new NoSuchGameException();
   }
 
 
 
   /**
    * Finds the played user's game against the specified opponent.
    * Returns the game number of null if no such game exists.
    */
 
   private InternalGameData findMyGameAgainst(String playerName) throws NoSuchGameException{
     Iterator gameNumbers = ongoingGamesData.keySet().iterator();
     while (gameNumbers.hasNext()){
       Integer gameNumber = (Integer)gameNumbers.next();
       InternalGameData gameData = (InternalGameData)ongoingGamesData.get(gameNumber);
       Game game = gameData.game;
       Player userPlayer = game.getUserPlayer();
       if (userPlayer == null) // Not our game or not played
         continue;
       Player oppPlayer = userPlayer.getOpponent();
       if ((oppPlayer.isWhite() && game.getWhiteName().equals(playerName)) ||
           (oppPlayer.isBlack() && game.getBlackName().equals(playerName)))
         return gameData;
     }
 
     throw new NoSuchGameException();
   }
 
 
 
   /**
    * Saves the GameInfoStruct until we receive enough info to fire a
    * GameStartEvent.
    */
 
   protected boolean processGameInfo(GameInfoStruct data){
     unstartedGamesData.put(new Integer(data.getGameNumber()), data);
 
     return true;
   }
 
 
 
   /**
    * Fires an appropriate GameEvent depending on the situation.
    */
 
   @Override
   protected boolean processStyle12(Style12Struct boardData){
     Integer gameNumber = new Integer(boardData.getGameNumber());
     InternalGameData gameData = (InternalGameData)ongoingGamesData.get(gameNumber);
     GameInfoStruct unstartedGameInfo = (GameInfoStruct)unstartedGamesData.remove(gameNumber);
 
     if (unstartedGameInfo != null) // A new game
       gameData = startGame(unstartedGameInfo, boardData);
     else if (gameData != null){ // A known game
       Style12Struct oldBoardData = gameData.boardData;
       int plyDifference = boardData.getPlayedPlyCount() - oldBoardData.getPlayedPlyCount();
 
       if (plyDifference < 0)
         tryIssueTakeback(gameData, boardData);
       else if (plyDifference == 0){
         if (!oldBoardData.getBoardFEN().equals(boardData.getBoardFEN()))
           changePosition(gameData, boardData);
 
         // This happens if you:
         // 1. Issue "refresh".
         // 2. Make an illegal move, because the server will re-send us the board
         //    (although we don't need it)
         // 3. Issue board setup commands.
         // 4. Use "wname" or "bname" to change the names of the white or black
         //    players.
       }
       else if (plyDifference == 1){
         if (boardData.getMoveVerbose() != null)
           makeMove(gameData, boardData);
         else
           changePosition(gameData, boardData); 
           // This shouldn't happen, but I'll leave it just in case
       }
       else if (plyDifference > 1){
         changePosition(gameData, boardData);
         // This happens if you:
         // 1. Issue "forward" with an argument of 2 or bigger.
       }
     }
     else if (!unsupportedGames.contains(gameNumber)){ 
       // Grr, the server started a game without sending us a GameInfo line.
       // Currently happens if you start examining a game (26.08.2002), or
       // doing "refresh <game>" (04.07.2004).
 
         //TODO: Implement getting info for starting examined game
         this.waiting = true;
         askForMoreGameInfo(boardData.getGameNumber(), boardData);
       // We have no choice but to fake the data, since the server simply doesn't
       // send us this information.
 
 
 
 
 
 
 
 
 
 
 
     }
 
     if (gameData != null)
       updateGame(gameData, boardData);
 
     return true;
   }
 
     /**
      * Method that process info about examined game.
      * @param gameNr
      * @param gameCategory
      * @return
      */
 
     protected boolean processExaminedGameInfo(int gameNr, String gameCategory) {
         Style12Struct boardData = this.examinedGameBoardData;
         InternalGameData gameData = (InternalGameData)ongoingGamesData.get(gameNr);
         if (this.examinedGameId == gameNr){
 
               String[] category = parseExaminedGameCategory(gameCategory);
               System.out.println("CATEGORY STRING = " + category[0]);
 
               this.examinedGameVariant = category[0];
               if (category[1].equals("rated")){
                     this.isExaminedRated = true;
               }else{
                     this.isExaminedRated = false;
                 }
               System.out.println("RATED? = " + this.isExaminedRated);
               System.out.println("THIRD IN CATEGORY = " + category[2]);
               if (category[2].length() != 0){
                   this.isExaminedPrivate = true;
               }
                         GameInfoStruct fakeGameInfo = new GameInfoStruct(boardData.getGameNumber(),
         this.isExaminedPrivate, this.examinedGameVariant, this.isExaminedRated, false, false, boardData.getInitialTime(),
         boardData.getIncrement(), boardData.getInitialTime(), boardData.getIncrement(),
         0, -1, ' ', -1, ' ', false, false);
 
       gameData = startGame(fakeGameInfo, boardData);
                if (gameData != null){
                     updateGame(gameData, boardData);
                }
 
                this.waiting = false;
 
 
           }
 
 
 
            return false;
     }
 
     /**
      * Method that parses through category string of examined game.
      * @param s
      * @return array of strings that is further processed
      */
 
 
     private String[] parseExaminedGameCategory(String s) {
         String[] categories = new String[3];
 
             switch(s.charAt(s.length()-1)){
                 case 'r': categories[1] = "rated";
                     break;
                 case 'u': categories[1] = "unrated";
                     break;
             }
             switch(s.charAt(s.length()-2)){
                 case 's': categories[0] = "standard";
                     break;
                 case 'b': categories[0] = "blitz";
                     break;
                 case 'l': categories[0] = "lightning";
                     break;
                 case 'w': categories[0] = "wild";
                     break;
                 case 'x': categories[0] = "atomic";
                     break;
                 case 'B': categories[0] = "bughouse";
                     break;
                 case 'z': categories[0] = "crazyhouse";
                     break;
                 case 'L': categories[0] = "losers";
                     break;
                 case 'S': categories[0] = "suicide";
                     break;
                 case 'n': categories[0] = "nonstandard";
                     break;
                 default: categories[0] = "unknown";
             }
 
         if (s.length()>2){
               categories[2] = "private";
         }else {
             categories[2] = "";
         }
 
 
 
         return categories;
     }
 
     /**
      * Method that sends to server question for more info about game with specified number.
      */
 
     private void askForMoreGameInfo(int gameNumber, Style12Struct boardData) {
         this.examinedGameId = gameNumber;
         this.examinedGameBoardData = boardData;
         this.sendCommFromPlugin("games " + String.valueOf(gameNumber));
     }
 
 
     /**
    * Processes a delta-board. Instead of actually handing the delta-board, this
    * method, instead, creates a Style12Struct object and then asks
    * <code>processStyle12</code> to handle it.
    */
    
   protected boolean processDeltaBoard(DeltaBoardStruct data){
     Integer gameNumber = new Integer(data.getGameNumber());
     InternalGameData gameData = (InternalGameData)ongoingGamesData.get(gameNumber);
     
     Game game = gameData.game;
     if (game.getVariant() != Chess.getInstance())
       throw new IllegalStateException("delta-boards should only be sent for regular chess");
     
     Style12Struct lastBoardData = gameData.boardData;
     ArrayList moveList = gameData.moveList;
     
     Position pos = game.getInitialPosition();
     for (int i = 0; i < moveList.size(); i++)
       pos.makeMove((Move)moveList.get(i));
     
     ChessMove move = (ChessMove)(Move.parseWarrenSmith(data.getMoveSmith(), pos, data.getMoveAlgebraic()));
     
     Square startSquare = move.getStartingSquare();
     ChessPiece movingPiece = (ChessPiece)((startSquare == null) ? null : pos.getPieceAt(startSquare));  
     
     pos.makeMove(move);
     
     
     String boardLexigraphic = pos.getLexigraphic();
     String currentPlayer = pos.getCurrentPlayer().isWhite() ? "W" : "B";
     int doublePawnPushFile = move.getDoublePawnPushFile();
     boolean kingMoved = movingPiece.isKing();
     boolean canWhiteCastleKingside =
       lastBoardData.canWhiteCastleKingside() && !kingMoved && !Square.getInstance(7, 0).equals(startSquare);
     boolean canWhiteCastleQueenside =
       lastBoardData.canBlackCastleQueenside() && !kingMoved && !Square.getInstance(0, 0).equals(startSquare);
     boolean canBlackCastleKingside =
       lastBoardData.canBlackCastleKingside() && !kingMoved && !Square.getInstance(7, 7).equals(startSquare);
     boolean canBlackCastleQueenside =
       lastBoardData.canBlackCastleQueenside() && !kingMoved && !Square.getInstance(0, 7).equals(startSquare);
     
     boolean isIrreversibleMove = movingPiece.isPawn() || move.isCapture() ||
       (canWhiteCastleKingside != lastBoardData.canWhiteCastleKingside()) ||
       (canWhiteCastleQueenside != lastBoardData.canWhiteCastleQueenside()) ||
       (canBlackCastleKingside != lastBoardData.canBlackCastleKingside()) ||
       (canBlackCastleQueenside != lastBoardData.canBlackCastleQueenside());
     int pliesSinceIrreversible = isIrreversibleMove ? 0 : lastBoardData.getPliesSinceIrreversible() + 1;
     
     String whiteName = lastBoardData.getWhiteName();
     String blackName = lastBoardData.getBlackName();
     int gameType = lastBoardData.getGameType();
     boolean isPlayedGame = lastBoardData.isPlayedGame();
     boolean isMyTurn = pos.getCurrentPlayer() == game.getUserPlayer();
     int initTime = lastBoardData.getInitialTime();
     int inc = lastBoardData.getIncrement();
     int whiteStrength = calcStrength(pos, Player.WHITE_PLAYER);
     int blackStrength = calcStrength(pos, Player.BLACK_PLAYER);
     int whiteTime = pos.getCurrentPlayer().isBlack() ? data.getRemainingTime() : lastBoardData.getWhiteTime();
     int blackTime = pos.getCurrentPlayer().isWhite() ? data.getRemainingTime() : lastBoardData.getBlackTime();
     int nextMoveNumber = lastBoardData.getNextMoveNumber() + (pos.getCurrentPlayer().isWhite() ? 1 : 0); 
     String moveVerbose = createVerboseMove(pos, move);
     String moveSAN = data.getMoveAlgebraic();
     int moveTime = data.getTakenTime();
     boolean isBoardFlipped = lastBoardData.isBoardFlipped();
     boolean isClockRunning = true;
     int lag = 0; // The server doesn't currently send us this information
     
     Style12Struct boardData = new Style12Struct(boardLexigraphic, currentPlayer, doublePawnPushFile,
       canWhiteCastleKingside, canWhiteCastleQueenside, canBlackCastleKingside, canBlackCastleQueenside,
       pliesSinceIrreversible, gameNumber.intValue(), whiteName, blackName, gameType, isPlayedGame,
       isMyTurn, initTime, inc, whiteStrength, blackStrength, whiteTime, blackTime, nextMoveNumber,
       moveVerbose, moveSAN, moveTime, isBoardFlipped, isClockRunning, lag);
       
     processStyle12(boardData);
     
     return true; 
   }
   
   
   
   /**
    * Calculates the material strength of the specified player in the specified
    * position.
    */
   
   private static int calcStrength(Position pos, Player player){
     int count = 0;
     for (int i = 0; i < 8; i++){
       for (int j = 0; j < 8; j++){
         ChessPiece piece = (ChessPiece)(pos.getPieceAt(i, j));
         if ((piece != null) && (piece.getPlayer() == player)){
           if (piece.isPawn())
             count += 1;
           else if (piece.isBishop())
             count += 3;
           else if (piece.isKnight())
             count += 3;
           else if (piece.isRook())
             count += 5;
           else if (piece.isQueen())
             count += 9;
           else if (piece.isKing())
             count += 0;
         }
       }
     }
     
     return count;
   }
   
   
   
   /**
    * Creates a verbose representation of the specified move in the specified
    * position. The move has already been made in the position.
    */
    
   private static String createVerboseMove(Position pos, ChessMove move){
     if (move.isShortCastling())
       return "o-o";
     else if (move.isLongCastling())
       return "o-o-o";
     else{
       ChessPiece piece = (ChessPiece)pos.getPieceAt(move.getEndingSquare());
       String moveVerbose = piece.toShortString() + '/' + move.getStartingSquare() + '-' + move.getEndingSquare();
       if (move.isPromotion())
         return moveVerbose + '=' + move.getPromotionTarget().toShortString();
       else
         return moveVerbose;
     }
   }
 
 
 
   /**
    * Changes the bsetup state of the game.
    */
 
   protected boolean processBSetupMode(boolean entered){
     try{
       findMyGame().isBSetup = entered;
     } catch (NoSuchGameException e){}
 
     return super.processBSetupMode(entered);
   }
 
     /**
      * Sends command to server saying to clear messages from messageFromNumber to 
      * messageToNumber. If both numbers are set to zeros then all messages are cleared.
      * If you pass invalid values to this method's params it will do nothing.
      * @param messageFromNumber - message to start from
      * @param messageToNumber - message to end with
      */
     
     public void clearMessage(int messageFromNumber, int messageToNumber) {
         if (messageFromNumber == 0 && messageToNumber == 0 ){
             sendCommand("clearmessages *");
             return;
         }
         else if (messageFromNumber == messageToNumber){
             sendCommand("clearmessages " + String.valueOf(messageToNumber));
             return;
         }
         else if (messageFromNumber < messageToNumber){
             sendCommand("clearmessages " + String.valueOf(messageFromNumber) + "-" + String.valueOf(messageToNumber));
             return;
         }
         else{return;}
             
         
     }
     
     /**
      * Sends command to server for getting messages from messageFromNumber to
      * messageToNumber. If both numbers are set to zeros then this method calls for all
      * messages from server.
      * @param messageFromNumber - message number to start from
      * @param messageToNumber - message number to end with
      */
 
     public void getMessages(int messageFromNumber, int messageToNumber) {
         if (messageFromNumber == 0 && messageToNumber == 0){
             sendCommand("messages");
             return;
         }
         else if (messageFromNumber == messageToNumber){
             sendCommand("messages " + String.valueOf(messageToNumber));
             return;
         }
         else if (messageFromNumber < messageToNumber){
             sendCommand("messages " + String.valueOf(messageFromNumber) + "-" + String.valueOf(messageToNumber));
             return;
         }
         else{return;}
     }
 
 
     /**
    * A small class for keeping internal data about a game.
    */
 
   private static class InternalGameData{
 
 
     /**
      * The Game object representing the game.
      */
 
     public final Game game;
 
 
 
     /**
      * A list of Moves done in the game.
      */
 
     public ArrayList moveList = new ArrayList();
 
 
 
     /**
      * The last Style12Struct we got for this game.
      */
 
     public Style12Struct boardData = null;
 
 
 
     /**
      * Is this game in bsetup mode?
      */
 
     public boolean isBSetup = false;
 
 
 
     /**
      * Maps offer indices to offers. Offers are Pairs where the first element
      * is the <code>Player</code> who made the offer and the 2nd is the offer
      * id. Takeback offers are kept separately.
      */
 
     public final HashMap indicesToOffers = new HashMap();
 
 
 
     /**
      * Maps takeback offer indices to takeback offers. Takeback offers are Pairs
      * where the first element is the <code>Player</code> who made the offer
      * and the 2nd is an <code>Integer</code> specifying the amount of plies
      * offered to take back.
      */
 
     public final HashMap indicesToTakebackOffers = new HashMap();
 
 
 
 
     /**
      * Works as a set of the offers currently in this game. The elements are
      * Pairs in which the first item is the <code>Player</code> who made the
      * offer and the second one is the offer id. Takeback offers are kept
      * separately.
      */
 
     private final HashMap offers = new HashMap();
 
 
 
     /**
      * The number of plies the white player offerred to takeback.
      */
 
     private int whiteTakeback;
 
 
 
     /**
      * The number of plies the black player offerred to takeback.
      */
 
     private int blackTakeback;
 
 
 
     /**
      * Creates a new InternalGameData.
      */
 
     public InternalGameData(Game game){
       this.game = game;
     }
 
 
 
     /**
      * Returns the amount of moves made in the game (as far as we counted).
      */
 
     public int getMoveCount(){
       return moveList.size();
     }
 
 
  
     /**
      * Adds the specified move to the moves list.
      */
 
     public void addMove(Move move){
       moveList.add(move);
     }
 
 
 
     /**
      * Removes the last <code>count</code> moves from the movelist, if possible.
      * Otherwise, throws an <code>IllegalArgumentException</code>.
      */
 
     public void removeLastMoves(int count){
       if (count > moveList.size())
         throw new IllegalArgumentException("Can't remove more elements than there are elements");
 
       int first = moveList.size() - 1;
       int last = moveList.size() - count;
       for (int i = first; i >= last; i--)
         moveList.remove(i);
     }
 
 
 
     /**
      * Removes all the moves made in the game.
      */
 
     public void clearMoves(){
       moveList.clear();
     }
 
 
 
     /**
      * Returns true if the specified offer is currently made by the specified
      * player in this game.
      */
 
     public boolean isOffered(int offerId, Player player){
       return offers.containsKey(new Pair(player, new Integer(offerId)));
     }
 
 
 
     /**
      * Sets the state of the specified offer in the game. Takeback offers are
      * handled by the setTakebackCount method.
      */
 
     public void setOffer(int offerId, Player player, boolean isMade){
       Pair offer = new Pair(player, new Integer(offerId));
       if (isMade) 
         offers.put(offer, offer);
       else
         offers.remove(offer);
     }
 
 
 
     /**
      * Sets the takeback offer in the game to the specified amount of plies.
      */
 
     public void setTakebackOffer(Player player, int plies){
       if (player.isWhite())
         whiteTakeback = plies;
       else
         blackTakeback = plies;
     }
 
 
 
     /**
      * Returns the amount of plies offered to take back by the specified player.
      */
 
     public int getTakebackOffer(Player player){
       if (player.isWhite())
         return whiteTakeback;
       else
         return blackTakeback;
     }
 
 
   }
 
 
 
   /**
    * Changes the primary played game.
    */
 
   protected boolean processSimulCurrentBoardChanged(int gameNumber, String oppName){
     primaryPlayedGame = gameNumber;
 
     return true;
   }
 
 
 
   /**
    * Changes the primary observed game.
    */
 
   protected boolean processPrimaryGameChanged(int gameNumber){
     primaryObservedGame = gameNumber;
 
     return true;
   }
 
 
     /**
      * Method that processes information about available pieces for players in Bughouse game.
      * Eventually it fire apopriate BughouseEvent
      * @param gameNumber
      * @param whiteAvailablePieces
      * @param blackAvailablePieces
      * @return
      */
 
     protected boolean processBughouseHoldings(int gameNumber, String whiteAvailablePieces, String blackAvailablePieces) {
         //System.out.println(">>>BUGHOUSE event occured with data: " + gameNumber + whiteAvailablePieces + blackAvailablePieces);
       listenerManager.fireBughouseEvent(new BughouseEvent(this, gameNumber, whiteAvailablePieces, blackAvailablePieces));
       return true;
 
     }
 
 
 
   /**
    * Invokes <code>closeGame(int)</code>.
    */
 //TODO review and make it smarter. Involves changing Game and GameEvent (Not sure
 // about that.
   protected boolean processGameEnd(int gameNumber, String whiteName, String blackName,
       String reason, String result){
 
     int resultCode;
     if ("1-0".equals(result))
       resultCode = Game.WHITE_WINS;
     else if ("0-1".equals(result))
       resultCode = Game.BLACK_WINS;
     else if ("1/2-1/2".equals(result))
       resultCode = Game.DRAW;
     else
       resultCode = Game.UNKNOWN_RESULT;
 
     closeGame(gameNumber, resultCode);
 
     return false;
   }
 
 
 
 
   /**
    * Invokes <code>closeGame(int)</code>.
    */
 
   protected boolean processStoppedObserving(int gameNumber){
     closeGame(gameNumber, Game.UNKNOWN_RESULT);
 
     return false;
   }
 
 
 
 
   /**
    * Invokes <code>closeGame(int)</code>.
    */
 
   protected boolean processStoppedExamining(int gameNumber){
     closeGame(gameNumber, Game.UNKNOWN_RESULT);
 
     return false;
   }
 
 
 
 
   /**
    * Invokes <code>illegalMoveAttempted</code>.
    */
 
   protected boolean processIllegalMove(String moveString, String reason){
     illegalMoveAttempted(moveString);
 
     return false;
   }
 
 
 
 
 
 
   /**
    * Called when a new game is starting. Responsible for creating the game on
    * the client side and firing appropriate events. Returns an InternalGameData
    * instance for the newly created Game.
    */
 
   private InternalGameData startGame(GameInfoStruct gameInfo, Style12Struct boardData){
     String categoryName = gameInfo.getGameCategory();
     WildVariant variant = getVariant(categoryName);
 
     if (variant == null){
       String starsPad = TextUtilities.padStart("", '*', categoryName.length()+2);
       String spacePad = TextUtilities.padStart("", ' ', categoryName.length()+1) + '*';
       processLine("********************************************************" + starsPad);
       processLine("* This version of Tonic does not support the wild variant " + categoryName + " *");
       processLine("* and is thus unable to display the game.               " + spacePad);
       processLine("* Please use the appropriate command to close the game. " + spacePad);
       processLine("********************************************************" + starsPad);
       unsupportedGames.add(new Integer(gameInfo.getGameNumber()));
       return null;
     }
 
     int gameType;
     switch (boardData.getGameType()){
       case Style12Struct.MY_GAME: gameType = Game.MY_GAME; break;
       case Style12Struct.OBSERVED_GAME: gameType = Game.OBSERVED_GAME; break;
       case Style12Struct.ISOLATED_BOARD: gameType = Game.ISOLATED_BOARD; break;
       default:
         throw new IllegalArgumentException("Bad game type value: "+boardData.getGameType());
     }
 
     Position initPos = new Position(variant);
     initPos.setFEN(boardData.getBoardFEN());
 
     String whiteName = boardData.getWhiteName();
     String blackName = boardData.getBlackName();
 
     int whiteTime = 1000 * gameInfo.getWhiteTime();
     int blackTime = 1000 * gameInfo.getBlackTime();
     int whiteInc = 1000 * gameInfo.getWhiteInc();
     int blackInc = 1000 * gameInfo.getBlackInc();
 
     int whiteRating = gameInfo.isWhiteRegistered() ? -1 : gameInfo.getWhiteRating();
     int blackRating = gameInfo.isBlackRegistered() ? -1 : gameInfo.getBlackRating();
 
     String gameID = String.valueOf(gameInfo.getGameNumber());
 
     boolean isRated = gameInfo.isGameRated();
 
     boolean isPlayed = boardData.isPlayedGame();
 
     String whiteTitles = "";
     String blackTitles = "";
 
     boolean initiallyFlipped = boardData.isBoardFlipped();
 
     Player currentPlayer = playerForString(boardData.getCurrentPlayer());
     Player userPlayer = null;
     if ((gameType == Game.MY_GAME) && isPlayed)
       userPlayer = boardData.isMyTurn() ? currentPlayer : currentPlayer.getOpponent();
 
     Game game = new Game(gameType, initPos, boardData.getPlayedPlyCount(), whiteName, blackName,
       whiteTime, whiteInc, blackTime, blackInc, whiteRating, blackRating, gameID, categoryName,
       isRated, isPlayed, whiteTitles, blackTitles, initiallyFlipped, userPlayer);
 
     InternalGameData gameData = new InternalGameData(game);
 
     ongoingGamesData.put(new Integer(gameInfo.getGameNumber()), gameData);
 
     listenerManager.fireGameEvent(new GameStartEvent(this, game));
 
     // The server doesn't send us seek remove lines during games, so we have
     // no choice but to remove *all* seeks during a game. The seeks are restored
     // when a game ends by setting seekinfo to 1 again.
     if (gameType == Game.MY_GAME)
       removeAllSeeks(); 
 
     return gameData;
   }
 
 
 
 
   /**
    * Updates any game parameters that differ in the board data from the current
    * game data.
    */
 
   private void updateGame(InternalGameData gameData, Style12Struct boardData){
     Game game = gameData.game;
     Style12Struct oldBoardData = gameData.boardData;
 
     updateClocks(gameData, boardData); // Update the clocks
 
     // Flip board
     if ((oldBoardData != null) && (oldBoardData.isBoardFlipped() != boardData.isBoardFlipped()))
       flipBoard(gameData, boardData);
 
     game.setWhiteName(boardData.getWhiteName()); // Change white name
     game.setBlackName(boardData.getBlackName()); // Change black name
     game.setWhiteTime(1000 * boardData.getInitialTime()); // Change white's initial time
     game.setWhiteInc(1000 * boardData.getIncrement()); // Change white's increment
     game.setBlackTime(1000 * boardData.getInitialTime()); // Change black's initial time
     game.setBlackInc(1000 * boardData.getIncrement()); // Change black's increment
 
 
     gameData.boardData = boardData;
   }
 
 
 
 
   /**
    * Gets called when a move is made. Fires an appropriate MoveMadeEvent.
    */
 
   private void makeMove(InternalGameData gameData, Style12Struct boardData){
     Game game = gameData.game;
     Style12Struct oldBoardData = gameData.boardData;
 
     String moveVerbose = boardData.getMoveVerbose();
     String moveSAN = boardData.getMoveSAN();
 
     String currentPlayerName = boardData.getMovingPlayerName();
     WildVariant variant = game.getVariant();
 
     Position position = new Position(variant);
     position.setLexigraphic(oldBoardData.getBoardLexigraphic());
     Player currentPlayer = playerForString(oldBoardData.getCurrentPlayer());
     position.setCurrentPlayer(currentPlayer);
 
     Move move;
     Square fromSquare, toSquare;
     Piece promotionPiece = null;
 
       //TODO: Start implementing bughouse and crazyhouse support from here. Look at second else if.
       Piece dropPiece = null;
     if (moveVerbose.equals("o-o"))
       move = variant.createShortCastling(position);
     else if (moveVerbose.equals("o-o-o"))
       move = variant.createLongCastling(position);
     else if (moveVerbose.indexOf('@') != -1){
 
         toSquare = Square.parseSquare(moveVerbose.substring(5,7));
         String dropPieceString = String.valueOf(moveVerbose.charAt(0));
         if (currentPlayer.isBlack()){
             dropPieceString = dropPieceString.toLowerCase();
 
         }
         dropPiece = variant.parsePiece(dropPieceString);
 
 
         move = variant.createMove(position, dropPiece,null, toSquare, promotionPiece, moveSAN);
     }
     else{
       fromSquare = Square.parseSquare(moveVerbose.substring(2, 4));
       toSquare = Square.parseSquare(moveVerbose.substring(5, 7));
       int promotionCharIndex = moveVerbose.indexOf("=")+1;
       if (promotionCharIndex != 0){
         String pieceString = moveVerbose.substring(promotionCharIndex, promotionCharIndex + 1);
         if (currentPlayer.isBlack()) // The server always sends upper case characters, even for black pieces.
           pieceString = pieceString.toLowerCase(); 
         promotionPiece = variant.parsePiece(pieceString);
       }
 
       move = variant.createMove(position, fromSquare, toSquare, promotionPiece, moveSAN);
     }
 
     listenerManager.fireGameEvent(new MoveMadeEvent(this, game, move, true, currentPlayerName)); 
       // (isNew == true) because FICS never sends the entire move history
 
     ArrayList unechoedGameMoves = (ArrayList)unechoedMoves.get(game);
 
     if ((unechoedGameMoves != null) && (unechoedGameMoves.size() != 0)){ // Looks like it's our move.
       Move madeMove = (Move)unechoedGameMoves.get(0);
       if (moveToString(game, move).equals(moveToString(game, madeMove))) // Same move.
         unechoedGameMoves.remove(0);
     }
 
     gameData.addMove(move);
   }
 
 
 
 
   /**
    * Fires an appropriate ClockAdjustmentEvent.
    */
 
   private void updateClocks(InternalGameData gameData, Style12Struct boardData){
     Game game = gameData.game;
 
     int whiteTime = boardData.getWhiteTime();
     int blackTime = boardData.getBlackTime();
 
     Player currentPlayer = playerForString(boardData.getCurrentPlayer());
 
     // Don't make clocks run for an isolated position.
     boolean isIsolatedBoard = game.getGameType() == Game.ISOLATED_BOARD; 
     boolean whiteRunning = (!isIsolatedBoard) && boardData.isClockRunning() && currentPlayer.isWhite();
     boolean blackRunning = (!isIsolatedBoard) && boardData.isClockRunning() && currentPlayer.isBlack();
     
     listenerManager.fireGameEvent(new ClockAdjustmentEvent(this, game, Player.WHITE_PLAYER, whiteTime, whiteRunning));
     listenerManager.fireGameEvent(new ClockAdjustmentEvent(this, game, Player.BLACK_PLAYER, blackTime, blackRunning));
   }
 
 
 
 
   /**
    * Fires an appropriate GameEndEvent.
    */
 
   private void closeGame(int gameNumber, int result){
     Integer gameID = new Integer(gameNumber);
 
     if (gameID.intValue() == primaryPlayedGame)
       primaryPlayedGame = -1;
     else if (gameID.intValue() == primaryObservedGame)
       primaryObservedGame = -1;
 
     InternalGameData gameData = (InternalGameData)ongoingGamesData.remove(gameID);
     if (gameData != null){
       Game game = gameData.game;
 
       game.setResult(result);
       listenerManager.fireGameEvent(new GameEndEvent(this, game, result));
 
       if ((game.getGameType() == Game.MY_GAME) && getIvarState(Ivar.SEEKINFO))
         setIvarState(Ivar.SEEKINFO, true); // Refresh the seeks
     }
     else
       unsupportedGames.remove(gameID);
   }
 
 
 
   /**
    * Fires an appropriate BoardFlipEvent.
    */
 
   private void flipBoard(InternalGameData gameData, Style12Struct newBoardData){
     listenerManager.fireGameEvent(new BoardFlipEvent(this, gameData.game, newBoardData.isBoardFlipped()));
   }
 
 
 
 
   /**
    * Fires an appropriate IllegalMoveEvent.
    */
 
   private void illegalMoveAttempted(String moveString){
     try{
       InternalGameData gameData = findMyGame(); 
       Game game = gameData.game;
 
       ArrayList unechoedGameMoves = (ArrayList)unechoedMoves.get(game);
 
       // Not a move we made (probably the user typed it in)
       if ((unechoedGameMoves == null) || (unechoedGameMoves.size() == 0)) 
         return;
 
 
       Move move = (Move)unechoedGameMoves.get(0);
 
       // We have no choice but to allow (moveString == null) because the server
       // doesn't always send us the move string (for example if it's not our turn).
       if ((moveString == null) || moveToString(game, move).equals(moveString)){
         // Our move, probably
 
         unechoedGameMoves.clear();
         listenerManager.fireGameEvent(new IllegalMoveEvent(this, game, move));
       }
     } catch (NoSuchGameException e){}
   }
 
 
 
 
   /**
    * Determines whether it's possible to issue a takeback for the specified
    * game change and if so calls issueTakeback, otherwise calls changePosition.
    */
 
   private void tryIssueTakeback(InternalGameData gameData, Style12Struct boardData){
     Style12Struct oldBoardData = gameData.boardData;
     int plyDifference =  oldBoardData.getPlayedPlyCount() - boardData.getPlayedPlyCount();
 
     if ((gameData.getMoveCount() < plyDifference)) // Can't issue takeback
       changePosition(gameData, boardData);
     else if (gameData.isBSetup)
       changePosition(gameData, boardData);
     else{
       Game game = gameData.game;
       ArrayList moveList = gameData.moveList;
       // Check whether the positions match, otherwise it could just be someone
       // issuing "bsetup fen ..." after making a few moves which resets the ply
       // count.
 
       Position oldPos = game.getInitialPosition();
       for (int i = 0; i < moveList.size() - plyDifference; i++){
         Move move = (Move)moveList.get(i);
         oldPos.makeMove(move);
       }
 
       Position newPos = game.getInitialPosition();
       newPos.setFEN(boardData.getBoardFEN());
 
       if (newPos.equals(oldPos)){
         issueTakeback(gameData, boardData);
       }else if (gameData.game.getVariant() instanceof Bughouse){
           issueTakeback(gameData, boardData);
       }else{
         changePosition(gameData, boardData);
       }
     }
   }
 
 
 
 
   /**
    * Fires an appropriate TakebackEvent.
    */
 
   private void issueTakeback(InternalGameData gameData, Style12Struct newBoardData){
     Style12Struct oldBoardData = gameData.boardData;
     int takebackCount = oldBoardData.getPlayedPlyCount() - newBoardData.getPlayedPlyCount();
 
     listenerManager.fireGameEvent(new TakebackEvent(this, gameData.game, takebackCount));
 
     gameData.removeLastMoves(takebackCount);
   }
 
 
 
 
   /**
    * Fires an appropriate PositionChangedEvent.
    */
 
   private void changePosition(InternalGameData gameData, Style12Struct newBoardData){
     Game game = gameData.game;
 
     Position newPos = game.getInitialPosition();
     newPos.setFEN(newBoardData.getBoardFEN());
 
     game.setInitialPosition(newPos);
     game.setPliesSinceStart(newBoardData.getPlayedPlyCount());
 
     listenerManager.fireGameEvent(new PositionChangedEvent(this, game, newPos));
 
     gameData.clearMoves();
 
     // We do this because moves in bsetup mode cause position change events, not move events
     if (gameData.isBSetup){
       ArrayList unechoedGameMoves = (ArrayList)unechoedMoves.get(game);
       if ((unechoedGameMoves != null) && (unechoedGameMoves.size() != 0))
         unechoedGameMoves.remove(0);
     }
   }
 
 
 
   /**
    * Maps seek IDs to Seek objects currently in the sought list.
    */
 
   private final HashMap seeks = new HashMap();
 
 
 
 
   /**
    * Returns the SeekListenerManager via which you can register and unregister
    * SeekListeners.
    */
 
   public SeekListenerManager getSeekListenerManager(){
     return getFreechessListenerManager();
   }
 
 
 
 
   /**
    * Creates an appropriate Seek object and fires a SeekEvent.
    */
 
   protected boolean processSeekAdded(SeekInfoStruct seekInfo){
     // We may get seeks after setting seekinfo to false because the server
     // already sent them when we sent it the request to set seekInfo to false.
     if (getRequestedIvarState(Ivar.SEEKINFO)){
       WildVariant variant = getVariant(seekInfo.getMatchType());
       if (variant != null){
         String seekID = String.valueOf(seekInfo.getSeekIndex());
         StringBuffer titlesBuf = new StringBuffer();
         int titles = seekInfo.getSeekerTitles();
 
         if ((titles & SeekInfoStruct.COMPUTER) != 0)
           titlesBuf.append("(C)");
         if ((titles & SeekInfoStruct.GM) != 0)
           titlesBuf.append("(GM)");
         if ((titles & SeekInfoStruct.IM) != 0)
           titlesBuf.append("(IM)");
         if ((titles & SeekInfoStruct.FM) != 0)
           titlesBuf.append("(FM)");
         if ((titles & SeekInfoStruct.WGM) != 0)
           titlesBuf.append("(WGM)");
         if ((titles & SeekInfoStruct.WIM) != 0)
           titlesBuf.append("(WIM)");
         if ((titles & SeekInfoStruct.WFM) != 0)
           titlesBuf.append("(WFM)");
 
         boolean isProvisional = (seekInfo.getSeekerProvShow() == 'P');
 
         boolean isSeekerRated = (seekInfo.getSeekerRating() != 0);
 
         boolean isRegistered = ((seekInfo.getSeekerTitles() & SeekInfoStruct.UNREGISTERED) == 0);
 
         boolean isComputer = ((seekInfo.getSeekerTitles() & SeekInfoStruct.COMPUTER) != 0);
 
         Player color;
         switch (seekInfo.getSeekerColor()){
           case 'W':
             color = Player.WHITE_PLAYER;
             break;
           case 'B':
             color = Player.BLACK_PLAYER;
             break;
           case '?':
             color = null;
             break;
           default:
             throw new IllegalStateException("Bad desired color char: "+seekInfo.getSeekerColor());
         }
 
         boolean isRatingLimited = ((seekInfo.getOpponentMinRating() > 0) || (seekInfo.getOpponentMaxRating() < 9999));
 
         Seek seek = new Seek(seekID, seekInfo.getSeekerHandle(), titlesBuf.toString(), seekInfo.getSeekerRating(),
           isProvisional, isRegistered, isSeekerRated, isComputer, variant, seekInfo.getMatchType(),
           seekInfo.getMatchTime()*60*1000, seekInfo.getMatchIncrement()*1000, seekInfo.isMatchRated(), color,
           isRatingLimited, seekInfo.getOpponentMinRating(), seekInfo.getOpponentMaxRating(),
           !seekInfo.isAutomaticAccept(), seekInfo.isFormulaUsed());
 
         Integer seekIndex = new Integer(seekInfo.getSeekIndex());
 
         Seek oldSeek = (Seek)seeks.get(seekIndex);
         if (oldSeek != null)
           listenerManager.fireSeekEvent(new SeekEvent(this, SeekEvent.SEEK_REMOVED, oldSeek));
 
         seeks.put(seekIndex, seek);
         listenerManager.fireSeekEvent(new SeekEvent(this, SeekEvent.SEEK_ADDED, seek));
       }
     }
     
     return true;
   }
 
 
 
 
   /**
    * Issues the appropriate SeekEvents and removes the seeks.
    */
 
   protected boolean processSeeksRemoved(int [] removedSeeks){
     for (int i = 0; i < removedSeeks.length; i++){
       Integer seekIndex = new Integer(removedSeeks[i]);
       Seek seek = (Seek)seeks.get(seekIndex);
       if (seek == null) // Happens if the seek is one we didn't fire an event for,
         continue;       // for example if we don't support the variant.
 
       listenerManager.fireSeekEvent(new SeekEvent(this, SeekEvent.SEEK_REMOVED, seek));
 
       seeks.remove(seekIndex);
     }
     
     return true;
   }
 
 
 
 
   /**
    * Issues the appropriate SeeksEvents and removes the seeks.
    */
 
   protected boolean processSeeksCleared(){
     removeAllSeeks();
     return true;
   }
 
 
 
 
   /**
    * Removes all the seeks and notifies the listeners.
    */
 
   private void removeAllSeeks(){
     int seeksCount = seeks.size();
     if (seeksCount != 0){
       Object [] seeksIndices = new Object[seeksCount];
 
       // Copy all the keys into a temporary array
       Iterator seekIDsEnum = seeks.keySet().iterator();
       for (int i = 0; i < seeksCount; i++)
         seeksIndices[i] = seekIDsEnum.next();
 
       // Remove all the seeks one by one, notifying any interested listeners.
       for (int i = 0; i < seeksCount; i++){
         Object seekIndex = seeksIndices[i];
         Seek seek = (Seek)seeks.get(seekIndex);
         listenerManager.fireSeekEvent(new SeekEvent(this, SeekEvent.SEEK_REMOVED, seek));
         seeks.remove(seekIndex);
       }
     }
   }
 
 
 
 
   /**
    * This method is called by our FreechessJinListenerManager when a new
    * SeekListener is added and we already had registered listeners (meaning that
    * iv_seekinfo was already on, so we need to notify the new listeners of all
    * existing seeks as well).
    */
 
   void notFirstListenerAdded(SeekListener listener){
     Iterator seeksEnum = seeks.values().iterator();
     while (seeksEnum.hasNext()){
       Seek seek = (Seek)seeksEnum.next();
       SeekEvent evt = new SeekEvent(this, SeekEvent.SEEK_ADDED, seek);
       listener.seekAdded(evt);
     }
   }
 
 
 
 
   /**
    * This method is called by our ChessclubJinListenerManager when the last
    * SeekListener is removed.
    */
 
   void lastSeekListenerRemoved(){
     seeks.clear();
   }
 
 
 
 
   /**
    * Maps offer indices to the <code>InternalGameData</code> objects
    * representing the games in which the offer was made.
    */
 
   private final HashMap offerIndicesToGameData = new HashMap();
 
 
 
 	/**
 	 * Override processOffer to always return true, since we don't want the
 	 * user to ever see these messages.
 	 */
 		
 
 	protected boolean processOffer(boolean toUser, String offerType, int offerIndex,
 		String oppName, String offerParams){
 
 		super.processOffer(toUser, offerType, offerIndex, oppName, offerParams);
 		return true;
 	}
 
 
 	
   /**
    * Overrides the superclass' method only to return true.
    */
 
   protected boolean processMatchOffered(boolean toUser, int offerIndex, String oppName,
       String matchDetails){
     super.processMatchOffered(toUser, offerIndex, oppName, matchDetails);
 
     return true;
   }
 
 
 
   /**
    * Fires the appropriate OfferEvent(s).
    */
 
   protected boolean processTakebackOffered(boolean toUser, int offerIndex, String oppName,
       int takebackCount){
     super.processTakebackOffered(toUser, offerIndex, oppName, takebackCount);
 
     try{
       InternalGameData gameData = findMyGameAgainst(oppName);
       Player userPlayer = gameData.game.getUserPlayer();
       Player player = toUser ? userPlayer.getOpponent() : userPlayer;
 
       offerIndicesToGameData.put(new Integer(offerIndex), gameData);
       gameData.indicesToTakebackOffers.put(new Integer(offerIndex),
         new Pair(player, new Integer(takebackCount)));
 
       updateTakebackOffer(gameData, player, takebackCount);
     } catch (NoSuchGameException e){}
 
     return true;
   }
 
 
 
   /**
    * Fires the appropriate OfferEvent(s).
    */
 
   protected boolean processDrawOffered(boolean toUser, int offerIndex, String oppName){
     super.processDrawOffered(toUser, offerIndex, oppName);
 
     processOffered(toUser, offerIndex, oppName, OfferEvent.DRAW_OFFER);
 
     return true;
   }
 
 
 
   /**
    * Fires the appropriate OfferEvent(s).
    */
 
   protected boolean processAbortOffered(boolean toUser, int offerIndex, String oppName){
     super.processAbortOffered(toUser, offerIndex, oppName);
 
     processOffered(toUser, offerIndex, oppName, OfferEvent.ABORT_OFFER);
 
     return true;
   }
 
 
 
 
   /**
    * Fires the appropriate OfferEvent(s).
    */
 
   protected boolean processAdjournOffered(boolean toUser, int offerIndex, String oppName){
     super.processAdjournOffered(toUser, offerIndex, oppName);
 
     processOffered(toUser, offerIndex, oppName, OfferEvent.ADJOURN_OFFER);
 
     return true;
   }
 
 
 
   /**
    * Gets called by the various process[offerType]Offered() methods to handle
    * the offers uniformly.
    */
 
   private void processOffered(boolean toUser, int offerIndex, String oppName, int offerId){
     try{
       InternalGameData gameData = findMyGameAgainst(oppName);
       Player userPlayer = gameData.game.getUserPlayer();
       Player player = toUser ? userPlayer.getOpponent() : userPlayer;
 
       offerIndicesToGameData.put(new Integer(offerIndex), gameData);
       gameData.indicesToOffers.put(new Integer(offerIndex),
         new Pair(player, new Integer(offerId)));
 
       updateOffers(gameData, offerId, player, true);
     } catch (NoSuchGameException e){}
   }
 
   
 
   /**
    * Fires the appropriate OfferEvent(s).
    */
 
   protected boolean processOfferRemoved(int offerIndex){
     super.processOfferRemoved(offerIndex);
 
     InternalGameData gameData =
       (InternalGameData)offerIndicesToGameData.remove(new Integer(offerIndex));
 
     if (gameData != null){
       // Check regular offers
       Pair offer = (Pair)gameData.indicesToOffers.remove(new Integer(offerIndex));
       if (offer != null){
         Player player = (Player)offer.getFirst();
         int offerId = ((Integer)offer.getSecond()).intValue();
         updateOffers(gameData, offerId, player, false);
       }
       else{
         // Check takeback offers
         offer = (Pair)gameData.indicesToTakebackOffers.remove(new Integer(offerIndex));
         if (offer != null){
           Player player = (Player)offer.getFirst();
           updateTakebackOffer(gameData, player, 0);
         }
       }
     }
 
     return true;
   }
 
 
 
 
   /**
    * Fires the appropriate OfferEvent(s).
    */
 
   protected boolean processPlayerCounteredTakebackOffer(int gameNum, String playerName,
       int takebackCount){
     super.processPlayerCounteredTakebackOffer(gameNum, playerName, takebackCount);
 
     try{
       InternalGameData gameData = getGameData(gameNum);
       Player player = gameData.game.getPlayerNamed(playerName);
 
       updateTakebackOffer(gameData, player.getOpponent(), 0);
       updateTakebackOffer(gameData, player, takebackCount);
     } catch (NoSuchGameException e){}
 
     return false;
   }
 
 
 
 
   /**
    * Fires the appropriate OfferEvent(s).
    */
 
   protected boolean processPlayerOffered(int gameNum, String playerName, String offerName){
     super.processPlayerOffered(gameNum, playerName, offerName);
 
     try{
       InternalGameData gameData = getGameData(gameNum);
       Player player = gameData.game.getPlayerNamed(playerName);
       int offerId;
       try{
         offerId = offerIdForOfferName(offerName);
         updateOffers(gameData, offerId, player, true);
       } catch (IllegalArgumentException e){}
     } catch (NoSuchGameException e){}
 
     return false;
   }
 
 
 
   /**
    * Fires the appropriate OfferEvent(s).
    */
 
   protected boolean processPlayerDeclined(int gameNum, String playerName, String offerName){
     super.processPlayerDeclined(gameNum, playerName, offerName);
 
     try{
       InternalGameData gameData = getGameData(gameNum);
       Player player = gameData.game.getPlayerNamed(playerName);
       int offerId;
       try{
         offerId = offerIdForOfferName(offerName);
         updateOffers(gameData, offerId, player.getOpponent(), false);
       } catch (IllegalArgumentException e){}
     } catch (NoSuchGameException e){}
 
     return false;
   }
 
 
 
   /**
    * Fires the appropriate OfferEvent(s).
    */
 
   protected boolean processPlayerWithdrew(int gameNum, String playerName, String offerName){
     super.processPlayerWithdrew(gameNum, playerName, offerName);
 
     try{
       InternalGameData gameData = getGameData(gameNum);
       Player player = gameData.game.getPlayerNamed(playerName);
       int offerId;
       try{
         offerId = offerIdForOfferName(offerName);
         updateOffers(gameData, offerId, player, false);
       } catch (IllegalArgumentException e){}
     } catch (NoSuchGameException e){}
 
     return false;
   }
 
 
 
   /**
    * Fires the appropriate OfferEvent(s).
    */
 
   protected boolean processPlayerOfferedTakeback(int gameNum, String playerName, int takebackCount){
     super.processPlayerOfferedTakeback(gameNum, playerName, takebackCount);
 
     try{
       InternalGameData gameData = getGameData(gameNum);
       Player player = gameData.game.getPlayerNamed(playerName);
 
       updateTakebackOffer(gameData, player, takebackCount);
     } catch (NoSuchGameException e){}
 
     return false;
   }
 
 
 
 
   /**
    * Returns the offerId (as defined by OfferEvent) corresponding to the
    * specified offer name. Throws an IllegalArgumentException if the offer name
    * is not recognizes.
    */
 
   private static int offerIdForOfferName(String offerName) throws IllegalArgumentException{
     if ("draw".equals(offerName))
       return OfferEvent.DRAW_OFFER;
     else if ("abort".equals(offerName))
       return OfferEvent.ABORT_OFFER;
     else if ("adjourn".equals(offerName))
       return OfferEvent.ADJOURN_OFFER;
     else if ("takeback".equals(offerName))
       return OfferEvent.TAKEBACK_OFFER;
     else
       throw new IllegalArgumentException("Unknown offer name: "+offerName);
   }
 
 
 
 
   /**
    * Updates the specified offer, firing any necessary events.
    */
 
   private void updateOffers(InternalGameData gameData, int offerId, Player player, boolean on){
     Game game = gameData.game;
 
     if (offerId == OfferEvent.TAKEBACK_OFFER){
       // We're forced to fake this so that an event is fired even if we start observing a game
       // with an existing takeback offer (of which we're not aware).
       if ((!on) && (gameData.getTakebackOffer(player) == 0))
         gameData.setTakebackOffer(player, 1);
 
       updateTakebackOffer(gameData, player.getOpponent(), 0); // Remove any existing offers
       updateTakebackOffer(gameData, player, on ? 1 : 0);
       // 1 as the server doesn't tell us how many
     }
     else{// if (gameData.isOffered(offerId, player) != on){ this
          // We check this because we might get such an event if we start observing a game with
          // an existing offer.
 
       gameData.setOffer(offerId, player, on);
       listenerManager.fireGameEvent(new OfferEvent(this, game, offerId, on, player));
     }
   }
 
 
 
 
   /**
    * Updates the takeback offer in the specified game to the specified amount of
    * plies.
    */
 
   private void updateTakebackOffer(InternalGameData gameData, Player player, int takebackCount){
     Game game = gameData.game;
 
     int oldTakeback = gameData.getTakebackOffer(player);
     if (oldTakeback != 0)
       listenerManager.fireGameEvent(new OfferEvent(this, game, false, player, oldTakeback));
 
     gameData.setTakebackOffer(player, takebackCount);
 
     if (takebackCount != 0)
       listenerManager.fireGameEvent(new OfferEvent(this, game, true, player, takebackCount));
   }
 
 
 
 
   /**
    * Accepts the given seek. Note that the given seek must be an instance generated
    * by this SeekJinConnection and it must be in the current sought list.
    */
 
   public void acceptSeek(Seek seek){
     if (!seeks.containsValue(seek))
       throw new IllegalArgumentException("The specified seek is not on the seek list");
 
     sendCommand("$play "+seek.getID());
   }
   
   
   
   /**
    * Issues the specified seek.
    */
    
   public void issueSeek(UserSeek seek){
     WildVariant variant = seek.getVariant();
     String wildName = getWildName(variant);
     if (wildName == null)
       throw new IllegalArgumentException("Unsupported variant: " + variant);
     
     Player color = seek.getColor();
     
     String seekCommand = "seek " + seek.getTime() + ' ' + seek.getInc() + ' ' +
       (seek.isRated() ? "rated" : "unrated") + ' ' +
       (color == null ? "" : color.isWhite() ? "white " : "black ") +
       wildName + ' ' +
       (seek.isManualAccept() ? "manual " : "") +
       (seek.isFormula() ? "formula " : "") +
       (seek.getMinRating() == Integer.MIN_VALUE ? "0" : String.valueOf(seek.getMinRating())) + '-' +
       (seek.getMaxRating() == Integer.MAX_VALUE ? "9999" : String.valueOf(seek.getMaxRating())) + ' ';
       
     sendCommand(seekCommand);
   }
 
 
 
   /**
    * Sends the "exit" command to the server.
    */
 
   public void exit(){
     sendCommand("$quit");
   }
 
 
 
 
   /**
    * Quits the specified game.
    */
 
   public void quitGame(Game game){
     Object id = game.getID();
     switch (game.getGameType()){
       case Game.MY_GAME:
         if (game.isPlayed())
           sendCommand("$resign");
         else
           sendCommand("$unexamine");
         break;
       case Game.OBSERVED_GAME:
         sendCommand("$unobserve "+id);
         break;
       case Game.ISOLATED_BOARD:
         break;
     }
   }
 
 
 
 
   /**
    * Makes the given move in the given game.
    */
 
   public void makeMove(Game game, Move move){
    Iterator gamesDataEnum = ongoingGamesData.values().iterator();
     boolean ourGame = false;
     while (gamesDataEnum.hasNext()){
       InternalGameData gameData = (InternalGameData)gamesDataEnum.next();
       if (gameData.game == game){
         ourGame = true;
         break;
       }
     }
 
     if (!ourGame)
       throw new IllegalArgumentException("The specified Game object was not created by this JinConnection or the game has ended.");
 
     sendCommand(moveToString(game, move));
 
     ArrayList unechoedGameMoves = (ArrayList)unechoedMoves.get(game);
     if (unechoedGameMoves == null){
       unechoedGameMoves = new ArrayList(2);
       unechoedMoves.put(game, unechoedGameMoves);
     }
     unechoedGameMoves.add(move);
   }
 
 
 
 
   /**
    * Converts the given move into a string we can send to the server.
    */
 
   private static String moveToString(Game game, Move move){
     WildVariant variant = game.getVariant();
     if (move instanceof ChessMove){
       ChessMove cmove = (ChessMove)move;
       if (cmove.isShortCastling())
         return "O-O";
       else if (cmove.isLongCastling())
         return "O-O-O";
 
       String s = cmove.getStartingSquare().toString() + cmove.getEndingSquare().toString();
       if (cmove.isPromotion())
         return s + '=' + variant.pieceToString(cmove.getPromotionTarget());
       else
         return s;
     }
     else
       throw new IllegalArgumentException("Unsupported Move type: "+move.getClass());
   }
 
 
 
 
 
   /**
    * Resigns the given game. The given game must be a played game and of type
    * Game.MY_GAME.
    */
 
   public void resign(Game game){
     checkGameMineAndPlayed(game);
 
     sendCommand("$resign");
   }
 
 
 
   /**
    * Sends a request to draw the given game. The given game must be a played 
    * game and of type Game.MY_GAME.
    */
 
   public void requestDraw(Game game){
     checkGameMineAndPlayed(game);
 
     sendCommand("$draw");
   }
 
 
 
 
   /**
    * Returns <code>true</code>.
    */
 
   public boolean isAbortSupported(){
     return true;
   }
 
 
 
   /**
    * Sends a request to abort the given game. The given game must be a played 
    * game and of type Game.MY_GAME.
    */
 
   public void requestAbort(Game game){
     checkGameMineAndPlayed(game);
 
     sendCommand("$abort");
   }
 
 
 
   /**
    * Returns <code>true</code>.
    */
 
   public boolean isAdjournSupported(){
     return true;
   }
 
 
 
   /**
    * Sends a request to adjourn the given game. The given game must be a played
    * game and of type Game.MY_GAME.
    */
 
   public void requestAdjourn(Game game){
     checkGameMineAndPlayed(game);
 
     sendCommand("$adjourn");
   }
 
   
 
   /**
    * Returns <code>true</code>.
    */
    
   public boolean isTakebackSupported(){
     return true;
   }
   
   
   
   /**
    * Sends "takeback 1" to the server.
    */
    
   public void requestTakeback(Game game){
     checkGameMineAndPlayed(game);
     
     sendCommand("$takeback 1");
   }
   
   
   
   /**
    * Returns <code>true</code>.
    */
    
   public boolean isMultipleTakebackSupported(){
     return true;
   }
   
   
   
   /**
    * Sends "takeback plyCount" to the server.
    */
    
   public void requestTakeback(Game game, int plyCount){
     checkGameMineAndPlayed(game);
     
     if (plyCount < 1)
       throw new IllegalArgumentException("Illegal ply count: " + plyCount);
     
     sendCommand("$takeback " + plyCount);
   }
   
 
 
   /**
    * Goes back the given amount of plies in the given game. If the given amount
    * of plies is bigger than the amount of plies since the beginning of the game,
    * goes to the beginning of the game.
    */
 
   public void goBackward(Game game, int plyCount){
     checkGameMineAndExamined(game);
 
     if (plyCount < 1)
       throw new IllegalArgumentException("Illegal ply count: " + plyCount);
     
     sendCommand("$backward " + plyCount);
   }
 
 
 
 
   /**
    * Goes forward the given amount of plies in the given game. If the given amount
    * of plies is bigger than the amount of plies remaining until the end of the
    * game, goes to the end of the game.
    */
 
   public void goForward(Game game, int plyCount){
     checkGameMineAndExamined(game);
 
     if (plyCount < 1)
       throw new IllegalArgumentException("Illegal ply count: " + plyCount);
     
     sendCommand("$forward " + plyCount);
   }
 
 
 
 
   /**
    * Goes to the beginning of the given game.
    */
 
   public void goToBeginning(Game game){
     checkGameMineAndExamined(game);
 
     sendCommand("$backward 999");
   }
 
 
 
   /**
    * Goes to the end of the given game.
    */
 
   public void goToEnd(Game game){
     checkGameMineAndExamined(game);
 
     sendCommand("$forward 999");
   }
 
 
 
   /**
    * Throws an IllegalArgumentException if the given Game is not of type 
    * Game.MY_GAME or is not a played game. Otherwise, simply returns.
    */
 
   private void checkGameMineAndPlayed(Game game){
     if ((game.getGameType() != Game.MY_GAME) || (!game.isPlayed()))
       throw new IllegalArgumentException("The given game must be of type Game.MY_GAME and a played one");
   }
 
 
 
 
   /**
    * Throws an IllegalArgumentException if the given Game is not of type 
    * Game.MY_GAME or is a played game. Otherwise, simply returns.
    */
 
   private void checkGameMineAndExamined(Game game){
     if ((game.getGameType() != Game.MY_GAME)||game.isPlayed())
       throw new IllegalArgumentException("The given game must be of type Game.MY_GAME and an examined one");
   }
   
   
   
   /**
    * Sends the "help" command to the server. 
    */
    
   public void showServerHelp(){
     sendCommand("help");
   }
   
   
   
   /**
    * Sends the specified question string to channel 1.
    */
    
   public void sendHelpQuestion(String question){
     sendCommand("tell 1 [" + Jin.getInstance().getAppName() + ' ' + Jin.getInstance().getAppVersion() + "] "+ question);    
   }
 
 
 
 
   /**
    * Overrides ChessclubConnection.execRunnable(Runnable) to execute the
    * runnable on the AWT thread using SwingUtilities.invokeLater(Runnable), 
    * since this class is meant to be used by Jin, a graphical interface using 
    * Swing.
    *
    * @see ChessclubConnection#execRunnable(Runnable)
    * @see SwingUtilities.invokeLater(Runnable)
    */
 
   public void execRunnable(Runnable runnable){
     SwingUtilities.invokeLater(runnable);
   }
 
     /**
      * Method called to get user's channel list from server.
      */
     public void updateChannelsList() {
         sendCommand("inch " + this.getUsername());
     }
 
     /**
      * Method called  to remove channel from user's channel list.
      * 
      * @param channelNumber - the number of removed channel.
      */
     public void removeChannel(int channelNumber) {
         sendCommand("-channel " + channelNumber);
     }
 
     /**
      * Method called to add channel to user's channel list.
      * 
      * @param channelNumber - the number of added channel.
      */
 
     public void addChannel(int channelNumber) {
         sendCommand("+channel " + channelNumber);
     }
 
     /**
      * Returns ListenerManager that lets us register listeners
      */
 
     public ChannelsListenerManager getChannelsListenerManager() {
         return getFreechessListenerManager();
     }
 
 
 }
