 package Roma;
 
 import Roma.Cards.Card;
 
 import java.util.ArrayList;
 
 public class PlayArea {
     //#defines
     private final int NUM_INIT_CARDS = 5;
     private final int PAD_LENGTH = 20;
 
     //Object pointers
     private Roma mainProgram;
     private CardManager cardManager;
     private DiceHolder diceHolder;
     private MoneyManager moneyManager;
     private VictoryTokens victoryTokens;
     private DiceDiscs diceDiscs;
     private Player players[];
     private BattleManager battleManager;
 
     //Variables
     private int turn = 0;
 
 
     public PlayArea(Roma mainProgram) {
         cardManager = new CardManager(this);
         diceHolder = new DiceHolder();
         moneyManager = new MoneyManager();
         victoryTokens = new VictoryTokens(this);
         diceDiscs = new DiceDiscs(this);
         players = new Player[Roma.MAX_PLAYERS];
         battleManager = new BattleManager(this);
         this.mainProgram = mainProgram;
 
         for (int i = 0; i < Roma.MAX_PLAYERS; i++) {
             players[i] = Player.makeRealPlayer(i, this);
         }
         getAndSwapCards();
         layAllCardsInHand();
     }
 
     private void layAllCardsInHand() {
         Player activePlayer = null;
         ArrayList<Card> hand = null;
         Card chosenCard = null;
         int targetDisc;
 
         for(int i = 0; i < Roma.MAX_PLAYERS; i++){
             activePlayer = players[i];
             hand = activePlayer.getHand();
 
             while(!hand.isEmpty()){
                 printStats();
                 System.out.println(activePlayer.getName() + ", please lay all your cards");
                 chosenCard = null;
                 while(chosenCard == null){
                     chosenCard = activePlayer.chooseCard(hand);
                     if(chosenCard == null){
                         System.out.println("You must choose a card");
                     }
                 }
                 targetDisc = -1; // cancel value
                 while(targetDisc == -1){
                     targetDisc = activePlayer.chooseCardDisc();
                     if(targetDisc == -1){
                         System.out.println("You must choose a disc");
                     }
                 }
                 diceDiscs.layCard(activePlayer.getPlayerID(), targetDisc, chosenCard);
             }
         }
     }
 
     //for testing
     public PlayArea(){
         System.err.println("In Testing phase");
         cardManager = new CardManager(this);
         diceHolder = new DiceHolder();
         moneyManager = new MoneyManager();
         victoryTokens = new VictoryTokens(this);
         diceDiscs = new DiceDiscs(this);
         players = new Player[Roma.MAX_PLAYERS];
         battleManager = new BattleManager(this);
 
         for (int i = 0; i < Roma.MAX_PLAYERS; i++) {
             players[i] = Player.makeDummyPlayer(i, this);
         }
     }
 
     public void getCardfromDeckfor(String cardName, int playerID){
         //for testing
 
     }
 
     public void endGame(){
         mainProgram.endGame();
     }
 
 
     public void getAndSwapCards(){
         ArrayList<Card> initialSet = new ArrayList<Card>();
         //gets the inital cards for the game
 
         for(int i = 0; i<Roma.MAX_PLAYERS;i++){
             initialSet.addAll(0, cardManager.drawNCards(Roma.NUM_INIT_CARDS));
         } //gets all the cards needed
 
         ArrayList<Card> choices = new ArrayList<Card>();
         //stores the choices of the previous player
 
         for (int i =0; i<Roma.MAX_PLAYERS;i++){
             choices.clear();
             //add prev choices
             ArrayList<Card> individualHand = new ArrayList<Card>();
 
             individualHand.addAll(initialSet.subList(
                     (i * Roma.NUM_INIT_CARDS), (i + 1) * Roma.NUM_INIT_CARDS));
             //extracts the first num-init-cards from the initial set
 
             System.out.println(players[i].getName() +
                     ", these are the " +Roma.NUM_INIT_CARDS+ " cards dealt to you.\n" +
                     "You must choose " + Roma.NUM_CARDS_SWAPPED + " to give to your opponent.\n" +
                     "Choose the first Card");
             //Prompt: move printing to player interface?
 
             Card temp = null;
 
             for(int j = 0; j<Roma.NUM_CARDS_SWAPPED;j++, temp = null){
                 while(temp == null){
                     temp = players[i].chooseCard(individualHand);
                     if (temp == null) System.out.println("You must choose a card: ");
                 }
                 choices.add(temp);
                 individualHand.remove(temp);
                 if(j!=Roma.NUM_CARDS_SWAPPED-1) System.out.println("Choose the next card:");
             }
             players[i].addCardListToHand(individualHand);
             players[(i+1)%Roma.MAX_PLAYERS].addCardListToHand(choices);
 
         }
     }
 
     public CardManager getCardManager() {
         return cardManager;
     }
 
     public DiceHolder getDiceHolder() {
         return diceHolder;
     }
 
     public MoneyManager getMoneyManager() {
         return moneyManager;
     }
 
     public VictoryTokens getVictoryTokens() {
         return victoryTokens;
     }
 
     public DiceDiscs getDiceDiscs() {
         return diceDiscs;
     }
 
     public Player getPlayer(int playerID) {
         return players[playerID];
     }
 
     public void runGame() {
         while (!mainProgram.getGameOver()) {
             Player player = players[turn % Roma.MAX_PLAYERS];
             playerTurn(player);
         }
     }
 
     public void playerTurn(Player player) {
         boolean endTurn = false;
         char roll = 'b';
 
         System.out.println("It's " + player.getName() + "'s turn");
 
         deductVictoryTokens(player.getPlayerID());
         diceDiscs.clearPlayerDice(player.getPlayerID());
         player.rollActionDice();
 
         //TODO: set up auto roll option
 //        if (player.getAutoRoll()) {
 //            player.rollActionDice();
 //        } else {
 //            System.out.println("Press space to roll action dice." +
 //                    "Press 'a' for automated dice roll for the rest of the game.");
 //            while (!(roll == ' ' || roll == 'a')) {
 //                roll = mainProgram.getInput().nextChar
 //            }
 //            if (roll == 'a') {
 //                player.setAutoRoll(true);
 //            }
 //        }
 
         while (!mainProgram.getGameOver() && !endTurn) {
             endTurn = players[turn % Roma.MAX_PLAYERS].takeAction();
         }
         battleManager.clearDefenseModActive(); // reset temporary defense modifiers
         turn++;
     }
 
     private void deductVictoryTokens(int playerID) {
         Card[] friendlyCards = diceDiscs.getPlayerActives(playerID);
         int countNull = 0;
         for(Card card : friendlyCards){
             if(card == null){
                 countNull++;
             }
         }
        System.out.println("There are " + countNull + " empty slots, losing that many players");
         victoryTokens.playerToPool(playerID, countNull);
     }
 
 
     public void printStats() {
         String cardName;
 
         for(int player = 0; player < Roma.MAX_PLAYERS; player++){
             System.out.println("-------------------------------------");
             System.out.println("Player: " + players[player].getName());
             System.out.println("Victory Tokens: " + victoryTokens.getPlayerTokens(player) +
                                 "  \tMoney: " + moneyManager.getPlayerMoney(player));
             System.out.println("Cards in hand: " + players[player].handSize());
             System.out.println("Cards in play: ");
             for(int position = 0; position < DiceDiscs.CARD_POSITIONS; position++){
                 cardName = diceDiscs.getCardName(player, position);
                 if(position == 6){
                     System.out.print("Bribery) " + String.format("%1$-" + PAD_LENGTH + "s",cardName) +
                             " : Dice on disc: ");
                 } else {
                     System.out.print("      " + (position + 1) + ") " + String.format("%1$-" + PAD_LENGTH + "s",cardName) +
                             " : Dice on disc: ");
                 }
                 for(Dice die : diceDiscs.checkForDice(player, position)){
                     System.out.print(die.getValue() + " ");
                 }
                 System.out.println();
             }
         }
     }
 
     public BattleManager getBattleManager() {
         return battleManager;
     }
 }
