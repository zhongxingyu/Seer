 package com.steamedpears.comp3004.models.players;
 
 import com.steamedpears.comp3004.models.*;
 import com.steamedpears.comp3004.models.assets.AssetMap;
 import com.steamedpears.comp3004.models.assets.AssetSet;
 import com.steamedpears.comp3004.models.players.strategies.HeuristicStrategy;
 import com.steamedpears.comp3004.models.players.strategies.NullStrategy;
 import com.steamedpears.comp3004.models.players.strategies.Strategy;
 import org.apache.log4j.Logger;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import static com.steamedpears.comp3004.models.PlayerCommand.PlayerCardAction.*;
 import static com.steamedpears.comp3004.models.assets.Asset.*;
 
 public abstract class Player extends Changeable implements Runnable{
     //static variables//////////////////////////////////////////////////////
 
     public static boolean TESTING_AI = false;
 
     private static Strategy defaultStrategy = new HeuristicStrategy();
     private static int currentId = 0;
     private static Logger log = Logger.getLogger(Player.class);
     private Thread thread;
 
     //static methods////////////////////////////////////////////////////////
     private static int getNextId(){
         return currentId++;
     }
 
     /**
      * cleanup state after a game is over
      */
     public static void cleanup(){
         currentId = 0;
     }
 
     /**
      * Given the count of each science, calculate the total victory points.
      * @param science1 the count of cuneiform tablet science cards
      * @param science2 the count of compass and right angle science cards
      * @param science3 the count of gear science cards
      * @return the total victory points from sciences
      */
     public static int getTotalSciencePoints(int science1, int science2, int science3) {
         int total = Math.min(Math.min(science1, science2), science3)*7;
 
         total += science1*science1;
         total += science2*science2;
         total += science3*science3;
 
         return total;
     }
 
     /**
      * Get a new AIPlayer
      * @param wonder the Wonder it should have
      * @param game the SevenWondersGame it is part of
      * @return an AIPlayer
      */
     public static Player newAIPlayer(Wonder wonder, SevenWondersGame game){
         return new AIPlayer(wonder, game, defaultStrategy);
     }
 
     public static Player newHumanPlayer(Wonder wonder, SevenWondersGame game){
         return TESTING_AI ? new AIPlayer(wonder, game, new NullStrategy()) : new HumanPlayer(wonder, game);
     }
 
     public static void setDefaultStrategy(Strategy strategy){
         defaultStrategy = strategy;
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
     private int stagedCommandResult;
 
     //constructor///////////////////////////////////////////////////////////
 
     /**
      * Creates a Player with the given wonder, game, and id
      * @param wonder the wonder of the player
      * @param game the game the player is part of
      * @param id the unique id of the player
      */
     private Player(Wonder wonder, SevenWondersGame game, int id){
         this.wonder = wonder;
         this.game = game;
         this.playedCards = new ArrayList<Card>();
         this.hand = new ArrayList<Card>();
         this.gold = 3;
         this.militaryResults = new ArrayList<Integer>();
         this.id = id;
     }
 
     /**
      * Creates a Player with the given wonder and game
      * @param wonder the wonder of the player
      * @param game the game the player is part of
      */
     public Player(Wonder wonder, SevenWondersGame game){
         this(wonder, game, getNextId());
     }
 
     private void discardCard(Card card, boolean isFinal){
         log.debug("discarding card: " + isFinal);
         if(isFinal){
             game.discard(card);
             hand.remove(card);
         }else{
             stagedCommandResult+=3;
         }
 
     }
 
     private void buildWonder(Card card, boolean isFinal){
         log.debug("building wonder: "+isFinal);
         Card stage = wonder.getNextStage();
         if(isFinal){
             wonder.buildNextStage(this);
             hand.remove(card);
         }else{
             stagedCommandResult+=stage.getAssets(this).get(ASSET_GOLD);
         }
     }
 
     private void playCard(Card card, boolean isFinal){
         log.debug("playing card: "+isFinal);
         if(isFinal){
             card.playCard(this);
             playedCards.add(card);
             hand.remove(card);
         }else{
             stagedCommandResult+=card.getAssets(this).get(ASSET_GOLD);
         }
     }
 
     private void undiscard(Card card, boolean isFinal){
         log.debug("undiscarding card: "+isFinal);
         if(isFinal){
             playedCards.add(card);
             game.undiscard(card);
         }else{
             stagedCommandResult+=card.getAssets(this).get(ASSET_GOLD);
         }
     }
 
     private void playFree(Card card, boolean isFinal){
         log.debug("playing free: " + isFinal);
         int oldGold = gold;
         playCard(card, isFinal);
         if(isFinal){
             wonder.expendLimitedAsset(ASSET_BUILD_FREE);
             gold = oldGold;
         }
     }
 
     /**
      * Applies the given command to the player
      * @param command the PlayerCommand object to apply to this player
      * @throws Exception if the command is invalid
      */
     public final void applyCommand(PlayerCommand command) throws Exception {
         log.debug("applying command");
         PlayerCommand temp = command;
         while(temp!=null){
             if(!isValid(command)){
                 throw new Exception("Given command is invalid: "+ command);
             }
             temp = temp.followup;
         }
         stagedCommandResult = 0;
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
         Card card = game.getCardById(command.cardID);
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
 
     /**
      * Finalizes a given command
      * @param command the command to finalize
      */
     public final void finalizeCommand(PlayerCommand command){
         log.debug("finalizing command");
         this.gold += stagedCommandResult;
         while(command!=null){
             payForTrades(command);
             resolveCommand(command, true);
             command = command.followup;
         }
     }
 
     private void payForTrades(PlayerCommand command) {
 
 
         int leftTotal = getCostOfTrade(command.leftPurchases, getDiscounts(getPlayerLeft()));
         int rightTotal = getCostOfTrade(command.rightPurchases, getDiscounts(getPlayerRight()));
         getPlayerLeft().changeGold(leftTotal);
         getPlayerRight().changeGold(rightTotal);
         changeGold(-leftTotal-rightTotal);
     }
 
     /**
      * Gets the discounts this player gets when trading with the given player
      * @param targetPlayer the player to trade with
      * @return a set of assets which are discounted
      */
     public AssetSet getDiscounts(Player targetPlayer) {
         String player;
         if(targetPlayer.equals(getPlayerLeft())){
             player = Card.PLAYER_LEFT;
         }else if(targetPlayer.equals(getPlayerRight())){
             player = Card.PLAYER_RIGHT;
         }else{
             //can't trade with this player
             return new AssetSet();
         }
         AssetSet discounts = new AssetSet();
 
         List<Card> cards = new ArrayList<Card>();
         cards.addAll(playedCards);
 
         cards.addAll(wonder.getStages());
 
         for(Card card: cards){
             if(card.getDiscountsTargets().contains(player)){
                 discounts.addAll(card.getDiscountsAssets());
             }
         }
         return discounts;
     }
 
     /**
      * gets the cost of a given trade
      * @param purchases an AssetMap of the purchases made
      * @param discounts as AssetSet of the discounts applicable to this trade
      * @return the gold cost of the trade
      */
     public int getCostOfTrade(AssetMap purchases, AssetSet discounts) {
         if(purchases==null){
             return 0;
         }else{
             int total = 0;
             for(String key: purchases.keySet()){
                 total+= purchases.get(key)*(discounts.contains(key) ? 1 : 2);
             }
             return total;
         }
     }
 
     /**
      * handles a new age
      */
     public void handleNewAge(){
         wonder.handleNewAge();
     }
 
     /**
      * when this method terminates, currentCommand should be set to the player's desired command
      */
     protected abstract void handleTurn();
 
     /**
      * begins the player thread
      */
     public final void run(){
         thread = Thread.currentThread();
         handleTurn();
     }
 
     /**
      * wakes a waiting player thread
      */
     public void wake() {
         thread.interrupt();
     }
 
     //setters///////////////////////////////////////////////////////////////////
 
     /**
      * sets the player on the left of this player to the given player
      * @param playerLeft the player to this player's left
      */
     public final void setPlayerLeft(Player playerLeft){
         this.playerLeft = playerLeft;
     }
 
     /**
      * sets the player on the right of this player to the given player
      * @param playerRight the player to this player's right
      */
     public final void setPlayerRight(Player playerRight){
         this.playerRight = playerRight;
     }
 
     /**
      * sets the player's current command to the given command
      * @param currentCommand the command to set to this player's current command
      */
     public final void setCurrentCommand(PlayerCommand currentCommand){
         this.currentCommand = currentCommand;
     }
 
     /**
      * sets the player's hand to the given list of cards
      * @param hand the list of cards to set to this player's hand
      */
     public final void setHand(List<Card> hand){
         this.hand = hand;
     }
 
     /**
      * Sets the player's wonder to the give one
      * @param wonder to set
      */
     public void setWonder(Wonder wonder){
         this.wonder = wonder;
     }
 
     /**
      * adds the given amount to the player's current amount of gold
      * @param amount the amount of gold to add to this player's current amount of gold
      */
     public void changeGold(int amount) {
         gold+=amount;
     }
 
     //getters///////////////////////////////////////////////////////////////////
 
     /**
      * validates the given command on this player
      * @param command the command to validate
      * @return true only if the command is valid on this player
      */
     public boolean isValid(PlayerCommand command){
         log.debug(this+" validating move "+command);
         //initial result: either they're doing one action, or their other action is UNDISCARD
         log.debug("validating number/types of moves");
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
         if(!result) log.debug("validating failed after move type sanity check");
 
         result = result && validateGoldCosts(command);
         if(!result) log.debug("validating failed after gold cost check");
 
         result = result && validateCanMakeTrades(command);
         if(!result) log.debug("validation failed after trade check");
 
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
         if(!result) log.debug("validation failed after specific command check");
 
         return result;
     }
 
     private boolean validateHasCard(PlayerCommand command){
         log.debug("validating player has card");
         return hand.contains(game.getCardById(command.cardID));
     }
 
     private boolean validateHasNotPlayedCard(PlayerCommand command){
         log.debug("validating player hasn't already player that card");
         Card card = game.getCardById(command.cardID);
         for(Card playedCard: playedCards){
             if(playedCard.getName().equals(card.getName())){
                 return false;
             }
         }
 
         log.debug("validating player will not be playing that card again this turn");
         command = command.followup;
         while(command!=null){
             if(command.action==PLAY || command.action==PLAY_FREE || command.action == UNDISCARD){
                 if(getGame().getCardById(command.cardID).getName().equals(card.getName())){
                     return false;
                 }
             }
             command = command.followup;
         }
 
         return true;
     }
 
     private boolean validatePlayFree(PlayerCommand command) {
         log.debug("validating player can play this card for free");
         return validateHasCard(command)
                 && validateHasNotPlayedCard(command)
                 && getAssets().containsKey(ASSET_BUILD_FREE);
     }
 
     private boolean validateUndiscard(PlayerCommand command) {
         log.debug("validating player can undiscard this card");
         return game.getDiscard().contains(game.getCardById(command.cardID))
                 && validateHasNotPlayedCard(command)
                 && getAssets().containsKey(ASSET_DISCARD);
     }
 
     private boolean validateDiscard(PlayerCommand command) {
         log.debug("validating player can discard this card");
         return validateHasCard(command);
     }
 
     private boolean validatePlayCard(PlayerCommand command) {
         log.debug("validating player can play card");
         return validateHasCard(command)
                 && validateHasNotPlayedCard(command)
                 && game.getCardById(command.cardID).canAfford(this, command);
     }
 
     private boolean validateBuildWonder(PlayerCommand command) {
         log.debug("validating player can build out wonder with this card");
         return validateHasCard(command)
                 && wonder.getNextStage()!=null
                 && wonder.getNextStage().canAfford(this, command);
     }
 
     private boolean validateCanMakeTrades(PlayerCommand command){
         log.debug("validating player can make the trades they are trying to make");
         return validateCanMakeTradesInternal(command.leftPurchases, getPlayerLeft())
                 && validateCanMakeTradesInternal(command.rightPurchases, getPlayerRight());
     }
 
     private boolean validateGoldCosts(PlayerCommand command){
         int total = 0;
         while(command!=null){
             total+=getCostOfTrade(command.leftPurchases, getDiscounts(getPlayerLeft()));
             total+=getCostOfTrade(command.rightPurchases, getDiscounts(getPlayerRight()));
             if(command.action==PLAY){
                 total+=getGame().getCardById(command.cardID).getCost(this).get(ASSET_GOLD);
             }
             command = command.followup;
         }
         return total<=getGold();
     }
 
     private boolean validateCanMakeTradesInternal(AssetMap purchases, Player player){
         if(purchases.isEmpty()){
             return true;
         }else{
             AssetMap tradeables = player.getAssetsTradeable();
             AssetMap purchasesLessBase = AssetMap.difference(purchases, tradeables);
             return purchasesLessBase.existsValidChoices(player.getOptionalAssetsCompleteTradeable());
         }
     }
 
     /**
      * gets the player to this player's left
      * @return the player to this player's left
      */
     public Player getPlayerLeft(){
         return playerLeft;
     }
 
     /**
      * gets the player to this player's right
      * @return the player to this player's right
      */
     public Player getPlayerRight(){
         return playerRight;
     }
 
     /**
      * gets the cards in this player's hand
      * @return the list of cards in this player's hand
      */
     public List<Card> getHand(){
         return hand;
     }
 
     /**
      * gets the current amount of gold held by this player
      * @return the current amount of gold held by this player
      */
     public int getGold(){
         return gold;
     }
 
     /**
      * gets this player's military results
      * @return this player's military results
      */
     public List<Integer> getMilitaryResults(){
         return militaryResults;
     }
 
     /**
      * gets the sum of this player's military defeats
      * @return the sum of this player's military defeats
      */
     public final int getMilitaryDefeats(){
         int result = 0;
         for(int value: getMilitaryResults()){
             if(value==-1){
                 ++result;
             }
         }
 
         return result;
     }
 
     /**
      * gets this player's current command
      * @return this player's current command
      */
     public final PlayerCommand getCurrentCommand(){
         return currentCommand;
     }
 
     /**
      * gets this player's wonder
      * @return this player's wonder
      */
     public Wonder getWonder(){
         return wonder;
     }
 
     /**
      * gets this player's id
      * @return this player's id
      */
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
     public final AssetMap getPrivateAssets(){
         AssetMap privateAssets = new AssetMap();
         privateAssets.put(ASSET_GOLD, getGold());
         privateAssets.put(ASSET_MILITARY_DEFEAT, getMilitaryDefeats());
         privateAssets.put(ASSET_MILITARY_VICTORY, getMilitaryWins());
         privateAssets.put(ASSET_WONDER_STAGES, wonder.getCurrentStage());
         return privateAssets;
     }
     /**
      * gets all the Assets the player definitely has before any decisions
      * @return the map
      */
     public AssetMap getAssets(){
         AssetMap result = new AssetMap();
 
         for(Card card: playedCards){
             result.add(card.getAssets(this));
         }
 
         result.add(wonder.getAssets(this));
 
         result.add(getPrivateAssets());
 
         result.put(ASSET_GOLD, gold);
 
         return result;
     }
 
     /**
      * get all the asset choices the player can make
      * @return the list of choices
      */
     public List<AssetSet> getOptionalAssetsComplete(){
         List<AssetSet> collectedAssets = new ArrayList<AssetSet>();
 
         for(Card card: playedCards){
             AssetSet options = card.getAssetsOptional(this);
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
     public AssetMap getOptionalAssetsSummary(){
         List<AssetSet> options = getOptionalAssetsComplete();
 
         AssetMap result = new AssetMap();
 
         for(AssetSet option: options){
             for(String key: option){
                 result.add(key);
             }
         }
 
         return result;
     }
 
     /**
      * get a map of all the assets a player can definitely trade before making any decisions
      * @return the map
      */
     public AssetMap getAssetsTradeable(){
         AssetMap collectedAssets = new AssetMap();
 
         for(Card card: playedCards){
             if(card.getColor().equals(COLOR_BROWN) || card.getColor().equals(COLOR_GREY)){
                 collectedAssets.add(card.getAssets(this));
             }
         }
 
         collectedAssets.add(wonder.getTradeableAssets());
 
         return collectedAssets;
     }
 
     /**
      * get a list of all the tradeable asset choices a player can make
      * @return the list
      */
     public List<AssetSet> getOptionalAssetsCompleteTradeable(){
         List<AssetSet> collectedAssets = new ArrayList<AssetSet>();
 
         for(Card card: playedCards){
             if(card.getColor().equals(COLOR_BROWN) || card.getColor().equals(COLOR_GREY)){
                 AssetSet options = card.getAssetsOptional(this);
                 if(options!=null && !options.isEmpty()){
                     collectedAssets.add(options);
                 }
             }
         }
 
         collectedAssets.addAll(wonder.getOptionalAssetsComplete(this));
 
         return collectedAssets;
     }
 
     /**
      * Gets all the assets a player has that a multiplier might be interested in (avoids infinite loop)
      * @return multiplier assets
      */
     public AssetMap getConcreteAssets() {
         AssetMap result = new AssetMap();
 
         for(Card card: playedCards){
             result.add(card.getColor());
         }
 
         result.put(ASSET_WONDER_STAGES, wonder.getCurrentStage());
 
         result.put(ASSET_MILITARY_DEFEAT, getMilitaryDefeats());
 
         return result;
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
 
     /**
      * adds a military victory in the given age
      * @param age the age in which the military victory occured
      */
     public void registerMilitaryVictory(int age) {
         militaryResults.add(age*2-1);
     }
 
     /**
      * adds a military defeat
      */
     public void registerMilitaryDefeat(){
         militaryResults.add(-1);
     }
 
     /**
      * gets the final total victory points for this player
      *
      * @return the final total victory points for this player
      */
     public int getFinalVictoryPoints() {
         int total = 0;
         AssetMap assets = getAssets();
         if (assets.get(ASSET_GUILD_COPY) > 0) {
             List<Card> guildChoices = new ArrayList<Card>();
             guildChoices.addAll(getPlayerLeft().getGuilds());
             guildChoices.addAll(getPlayerRight().getGuilds());
             for (Card guild : guildChoices) {
                 playedCards.add(guild);
                 total = Math.max(total, getFinalVictoryPointsInternal());
                 playedCards.remove(guild);
             }
         } else {
             total = getFinalVictoryPointsInternal();
         }
         return total;
     }
 
     private int getFinalVictoryPointsInternal(){
         int total = 0;
         AssetMap assets = getAssets();
         List<AssetSet> optionalAssets = getOptionalAssetsComplete();
 
         total+=getGold()/3;
         total+=assets.get(ASSET_MILITARY_VICTORY)-assets.get(ASSET_MILITARY_DEFEAT);
         total+=assets.get(ASSET_VICTORY_POINTS);
         total+=getMaximumPotentialSciencePoints(assets, optionalAssets);
 
         return total;
     }
 
     private int getMaximumPotentialSciencePoints(AssetMap assets, List<AssetSet> optionalAssets) {
         int total;
 
         int science1 = assets.get(ASSET_SCIENCE_1);
         int science2 = assets.get(ASSET_SCIENCE_2);
         int science3 = assets.get(ASSET_SCIENCE_3);
 
         int numScienceChoices = 0;
         for(AssetSet choices: optionalAssets){
             if(choices.contains(ASSET_SCIENCE_1)){
                 numScienceChoices++;
             }
         }
 
         total = Player.getTotalSciencePoints(science1, science2, science3);
 
         //totally legit
         for(int i=0; i<=numScienceChoices; i++){
             for(int j=0; j<=numScienceChoices-i; j++){
                 for(int k=0; k<=numScienceChoices-j-i; k++){
                     total = Math.max(total, getTotalSciencePoints(science1+i, science2+j, science3+k));
                 }
             }
         }
 
         return total;
     }
 
     /**
       * Check if the player has completed their wonder.
       * @return true only if this player has completed their wonder
       */
     public boolean hasFinishedWonder() {
         return wonder.getNextStage() == null;
     }
 
     /**
      * Gets the player's played cards.
      * @return the player's played cards.
      */
     public List<Card> getPlayedCards() {
         return playedCards;
     }
 
     /**
      * Get the game associated with this player
      * @return the game associated with this player
      */
     public SevenWondersGame getGame() { return game; }
 
     @Override
     public String toString(){
         return "Player "+getPlayerId();
     }
 
     /**
      * Creates a generic player with the same state to try hypothetical states out on.
      * @param strategy the Strategy this clone should use
      * @return the clone
      */
     public Player clone(Strategy strategy){
         log.debug("Cloning self");
         Player clone = new AIPlayer(getWonder(), getGame(), strategy);
         clone.getHand().addAll(getHand());
         clone.getPlayedCards().addAll(getPlayedCards());
         clone.setPlayerRight(getPlayerRight());
         clone.setPlayerLeft(getPlayerLeft());
         clone.gold = getGold();
         clone.getMilitaryResults().addAll(getMilitaryResults());
         clone.id = id;
 
         return clone;
     }
 
     /**
      * Get all the guilds this player has played
      * @return the guilds the player has played
      */
     public Set<Card> getGuilds() {
         Set<Card> guilds = new HashSet<Card>();
         for(Card card: playedCards){
             if(card.getColor().equals(COLOR_PURPLE)){
                 guilds.add(card);
             }
         }
         return guilds;
     }
 }
