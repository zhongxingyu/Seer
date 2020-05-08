 package com.steamedpears.comp3004.models;
 
 import com.steamedpears.comp3004.models.players.AIPlayer;
 import org.apache.log4j.Logger;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import static com.steamedpears.comp3004.models.PlayerCommand.PlayerCardAction.*;
 import static com.steamedpears.comp3004.models.Asset.*;
 
 public abstract class Player implements Runnable{
     //static variables//////////////////////////////////////////////////////
     private static int currentId = 0;
     private static Logger log = Logger.getLogger(Player.class);
     private Thread thread;
 
     //static methods////////////////////////////////////////////////////////
     private static int getNextId(){
         return currentId++;
     }
 
     public static Player getAIPlayer(Wonder wonder, SevenWondersGame game){
         return new AIPlayer(wonder, game);
     }
 
     //instance variables////////////////////////////////////////////////////
     private Wonder wonder;
     private List<Card> playedCards;
     private Player playerLeft;
     private Player playerRight;
     private SevenWondersGame game;
     private PlayerCommand currentCommand;
     private List<Card> hand;
     private List<Integer> militaryResults;
     private int gold;
     private int id;
     private Map<String, Integer> stagedCommandResult;
 
     //constructor///////////////////////////////////////////////////////////
     public Player(Wonder wonder, SevenWondersGame game, int id){
         this.wonder = wonder;
         this.game = game;
         this.playedCards = new ArrayList<Card>();
         this.hand = new ArrayList<Card>();
         this.gold = 0;
         this.militaryResults = new ArrayList<Integer>();
         this.id = id;
     }
 
     public Player(Wonder wonder, SevenWondersGame game){
         this(wonder, game, getNextId());
     }
 
     private void discardCard(Card card, boolean isFinal){
         log.debug("discarding card: "+isFinal);
         if(isFinal){
             game.discard(card);
             hand.remove(card);
         }else{
             Map<String, Integer> result = new HashMap<String, Integer>();
             result.put(ASSET_GOLD, 3);
 
             stagedCommandResult = sumAssets(stagedCommandResult, result);
         }
 
     }
 
     private void buildWonder(Card card, boolean isFinal){
         log.debug("building wonder: "+isFinal);
         Card stage = wonder.getNextStage();
         if(isFinal){
            wonder.buildNextStage(this, card);
         }else{
             stagedCommandResult = sumAssets(stagedCommandResult, stage.getAssets(this));
         }
     }
 
     private void playCard(Card card, boolean isFinal){
         log.debug("playing card: "+isFinal);
         if(isFinal){
             playedCards.add(card);
             hand.remove(card);
         }else{
             stagedCommandResult = sumAssets(stagedCommandResult, card.getAssets(this));
         }
     }
 
     private void undiscard(Card card, boolean isFinal){
         log.debug("undiscarding card: "+isFinal);
         if(isFinal){
             playedCards.add(card);
             game.undiscard(card);
         }else{
             stagedCommandResult = sumAssets(stagedCommandResult, card.getAssets(this));
         }
     }
 
     private void playFree(Card card, boolean isFinal){
         log.debug("playing free: "+isFinal);
         playCard(card, isFinal);
         if(isFinal){
             wonder.expendLimitedResource(ASSET_BUILD_FREE);
         }
     }
 
     public final void applyCommand(PlayerCommand command) throws Exception {
         log.debug("applying command");
         PlayerCommand temp = command;
         while(temp!=null){
             if(!isValid(command)){
                 throw new Exception("Given command is invalid: "+ command);
             }
             temp = temp.followup;
         }
         stagedCommandResult = new HashMap<String, Integer>();
         applyCommandInternal(command);
     }
 
     private void applyCommandInternal(PlayerCommand command){
         if(command!=null){
             resolveCommand(command, false);
             if(command.followup!=null){
                 applyCommandInternal(command.followup);
             }
         }
     }
 
     private void resolveCommand(PlayerCommand command, boolean isFinal){
         Card card = game.getCardById(command.card);
         if(command.action.equals(BUILD)){
             buildWonder(card, isFinal);
         }else if(command.action.equals(PLAY)){
             playCard(card, isFinal);
         }else if(command.action.equals(DISCARD)){
             discardCard(card, isFinal);
         }else if(command.action.equals(UNDISCARD)){
             undiscard(card, isFinal);
         }else if(command.action.equals(PLAY_FREE)){
             playFree(card, isFinal);
         }
     }
 
     public final void finalizeCommand(PlayerCommand command){
         log.debug("finalizing command");
         if(stagedCommandResult.containsKey(ASSET_GOLD)){
             this.gold+=stagedCommandResult.get(ASSET_GOLD);
         }
         while(command!=null){
             resolveCommand(command, true);
             if(command.followup!=null && command.followup.action!=UNDISCARD){
                 wonder.expendLimitedResource(ASSET_DOUBLE_PLAY);
             }
             command = command.followup;
         }
     }
 
     //extend with a GUI or AI
     //when handleTurn terminates, currentCommand should be set with the Player's desired command
     protected abstract void handleTurn();
 
     public final void run(){
         thread = Thread.currentThread();
         handleTurn();
     }
 
     public void wake() {
         thread.interrupt();
     }
 
     //setters///////////////////////////////////////////////////////////////////
     public final void setPlayerLeft(Player playerLeft){
         this.playerLeft = playerLeft;
     }
 
     public final void setPlayerRight(Player playerRight){
         this.playerRight = playerRight;
     }
 
     protected final void setCurrentCommand(PlayerCommand currentCommand){
         this.currentCommand = currentCommand;
     }
 
     public final void setHand(List<Card> hand){
         this.hand = hand;
     }
 
     //getters///////////////////////////////////////////////////////////////////
     public final boolean isValid(PlayerCommand command){
         //TODO: determine if further validation necessary
 
         //initial result: either they're doing one action, or their other action is UNDISCARD
         boolean result = command.followup==null
                 || command.followup.action==UNDISCARD;
 
         if(!result){
             //if above is false, then they must be using an ASSET_DOUBLE_PLAY
             Map<String, Integer> assets = getAssets();
             result = assets.containsKey(ASSET_DOUBLE_PLAY)
                     && assets.get(ASSET_DOUBLE_PLAY)>0
                     && hand.size()==2
                     && !command.action.equals(UNDISCARD);
         }
 
         //if everything is good so far, perform specific validation
         if(command.action.equals(BUILD)){
             result = result && validateBuildWonder(command);
         }else if(command.action.equals(PLAY)){
             result = result && validatePlayCard(command);
         }else if(command.action.equals(DISCARD)){
             result = result && validateDiscard(command);
         }else if(command.action.equals(UNDISCARD)){
             result = result && validateUndiscard(command);
         }else if(command.action.equals(PLAY_FREE)){
             result = result && validatePlayFree(command);
         }
 
         return result;
     }
 
     private boolean validateHasCard(PlayerCommand command){
         return hand.contains(game.getCardById(command.card));
     }
 
     private boolean validateHasNotPlayedCard(PlayerCommand command){
         Card card = game.getCardById(command.card);
         for(Card playedCard: playedCards){
             if(playedCard.getName().equals(card.getName())){
                 return false;
             }
         }
         return true;
     }
 
     private boolean validatePlayFree(PlayerCommand command) {
         return validateHasCard(command)
                 && validateHasNotPlayedCard(command);
     }
 
     private boolean validateUndiscard(PlayerCommand command) {
         return game.getDiscard().contains(game.getCardById(command.card))
                 && validateHasNotPlayedCard(command);
     }
 
     private boolean validateDiscard(PlayerCommand command) {
         return validateHasCard(command);
     }
 
     private boolean validatePlayCard(PlayerCommand command) {
         return validateHasCard(command)
                 && validateHasNotPlayedCard(command)
                 && game.getCardById(command.card).canAfford(this, command);
     }
 
     private boolean validateBuildWonder(PlayerCommand command) {
         return validateHasCard(command)
                 && wonder.getNextStage()!=null
                 && wonder.getNextStage().canAfford(this, command);
     }
 
     public final Player getPlayerLeft(){
         return playerLeft;
     }
 
     public final Player getPlayerRight(){
         return playerRight;
     }
 
     // probably not going to break anything if this isn't final
     public List<Card> getHand(){
         return hand;
     }
 
     // probably fine to mock this out
     public int getGold(){
         return gold;
     }
 
     // probably fine to mock this out
     public List<Integer> getMilitaryResults(){
         return militaryResults;
     }
 
     public final int getMilitaryDefeats(){
         int result = 0;
         for(int value: getMilitaryResults()){
             if(value==-1){
                 ++result;
             }
         }
 
         return result;
     }
 
     public final PlayerCommand getCurrentCommand(){
         return currentCommand;
     }
 
     public final Wonder getWonder(){
         return wonder;
     }
 
     public final int getPlayerId(){
         return id;
     }
 
     @Override
     public int hashCode(){
         return getPlayerId();
     }
 
     /**
      * gets all the assets that aren't captured in the cards or wonder of the player
      * @return the map
      */
     public final Map<String, Integer> getPrivateAssets(){
         Map<String, Integer> privateAssets = new HashMap<String, Integer>();
         privateAssets.put(ASSET_GOLD, getGold());
         privateAssets.put(ASSET_DEFEAT, getMilitaryDefeats());
 
         return privateAssets;
     }
 
     /**
      * gets all the Assets the player definitely has before any decisions
      * @return the map
      */
     public final Map<String, Integer> getAssets(){
         List<Map<String, Integer>> collectedAssets = new ArrayList<Map<String, Integer>>();
 
         for(Card card: playedCards){
             collectedAssets.add(card.getAssets(this));
         }
 
         collectedAssets.add(wonder.getAssets(this));
 
         collectedAssets.add(getPrivateAssets());
 
         return sumAssets(collectedAssets);
     }
 
     /**
      * get all the asset choices the player can make
      * @return the list of choices
      */
     public final List<Set<String>> getOptionalAssetsComplete(){
         List<Set<String>> collectedAssets = new ArrayList<Set<String>>();
 
         for(Card card: playedCards){
             Set<String> options = card.getAssetsOptional(this);
             if(options!=null && !options.isEmpty()){
                 collectedAssets.add(options);
             }
         }
 
         collectedAssets.addAll(wonder.getOptionalAssetsComplete(this));
 
         return collectedAssets;
     }
 
     /**
      * get a map representing the total of all the asset choices a player can make
      * e.g. wood/stone and stone/clay -> wood:1, stone:2, clay:1
      * @return the map
      */
     public final Map<String, Integer> getOptionalAssetsSummary(){
         List<Set<String>> options = getOptionalAssetsComplete();
 
         Map<String, Integer> result = new HashMap<String, Integer>();
 
         for(Set<String> option: options){
             for(String key: option){
                 if(result.containsKey(key)){
                     result.put(key, result.get(key)+1);
                 }else{
                     result.put(key, 1);
                 }
             }
         }
 
         return result;
     }
 
     /**
      * get a map of all the assets a player can definitely trade before making any decisions
      * @return the map
      */
     public final Map<String, Integer> getAssetsTradeable(){
         List<Map<String, Integer>> collectedAssets = new ArrayList<Map<String, Integer>>();
 
         for(Card card: playedCards){
             if(card.getColor().equals(COLOR_BROWN) || card.getColor().equals(COLOR_GREY)){
                 collectedAssets.add(card.getAssets(this));
             }
         }
 
         collectedAssets.add(wonder.getTradeableAssets());
 
         return sumAssets(collectedAssets);
     }
 
     /**
      * get a list of all the tradeable asset choices a player can make
      * @return the list
      */
     public final List<Set<String>> getOptionalAssetsCompleteTradeable(){
         List<Set<String>> collectedAssets = new ArrayList<Set<String>>();
 
         for(Card card: playedCards){
             if(card.getColor().equals(COLOR_BROWN) || card.getColor().equals(COLOR_GREY)){
                 Set<String> options = card.getAssetsOptional(this);
                 if(options!=null && !options.isEmpty()){
                     collectedAssets.add(options);
                 }
             }
         }
 
         collectedAssets.addAll(wonder.getOptionalAssetsComplete(this));
 
         return collectedAssets;
     }
 
     /**
      * get the total value of all military wins
      * @return value of all military wins
      */
     public int getMilitaryWins() {
         int wins = 0;
         for(int i : getMilitaryResults()) {
             if(i > 0) {
                 wins += i;
             }
         }
         return wins;
     }
 }
