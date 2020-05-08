 package models;
 
 import java.util.Random;
 import java.util.UUID;
 
 import static models.Message.sendMessage;
 
 public class Game {
     private String gameId;
 
 
     private Player playerOne;
     private Player playerTwo;
     private Player currentPlayer;
     private TurnState currentState;
     private int playerNumber;
     private int leavers;
 
     public Game() {
         gameId = UUID.randomUUID().toString();
     }
 
     void startGame() {
         leavers = 0;
         playerNumber = 2;
         setTurn();
         notifyOponent();
         //generateStrategy();
         notifyTurn();
     }
 
     public String getGameId() {
         return gameId;
     }
 
     public Player getPlayerOne() {
         return playerOne;
     }
 
     public Player getPlayerTwo() {
         return playerTwo;
     }
 
 
     public void setPlayerOne(Player playerOne) {
         this.playerOne = playerOne;
     }
 
     public void setPlayerTwo(Player playerTwo) {
         this.playerTwo = playerTwo;
     }
 
     public boolean isPlayerOneDefined() {
         return playerOne != null;
     }
 
 
     public boolean isPlayerTwoDefined() {
         return playerTwo != null;
     }
 
 
     private void setTurn() {
         playerOne.setTurn(turn());
         playerOne.setTurn(!playerTwo.isTurn());
     }
 
     public boolean turn(){
         Random random = new Random();
         boolean turn = random.nextBoolean();
         return turn;
     }
 
 
     private void notifyOponent() {
         sendMessage(getPlayerOne(), "start", "Let's play Boom Boom Splash, you are playing against " + getPlayerTwo().getUsername());
         sendMessage(getPlayerTwo(), "start", "Let's play Boom Boom Splash, you are playing against " + getPlayerOne().getUsername());
     }
 
    public void play(Player player){
         if (getCurrentPlayer() == player) {
             //Todo ver lo que toco
             changeTurn();
             notifyTurn();
         } else {
            sendMessage(getCurrentPlayer(), "wait", "Wait, is not your turn!");
         }
 
     }
 
     private void changeTurn(){
        if(playerOne.isTurn()){
            playerOne.setTurn(false);
            playerTwo.setTurn(true);
        }
        else{
            playerTwo.setTurn(false);
            playerOne.setTurn(true);
        }
     }
 
     private void notifyTurn() {
           sendMessage(getCurrentPlayer(), "Fire", "It's your turn, Fire.");
            sendMessage(getOpponent(getCurrentPlayer()), "wait", "It is " + getCurrentPlayer().getUsername() +  "turn!");
 
     }
 
     public Player getOpponent(Player player){
         Player opponent = isPlayerOne(player) ? getPlayerTwo() : getPlayerOne();
         return opponent;
     }
 
     private boolean isPlayerOne(Player player) {
         return player == getPlayerOne();
     }
 
     public void leave(){
         playerNumber--;
     }
 
     public boolean isStart() {
        return isPlayerOneDefined() && isPlayerTwoDefined();
     }
 
     public boolean isFinish() {
         return playerNumber == 0;
     }
     
     private Player getCurrentPlayer(){
         return currentPlayer = playerOne.isTurn() ? playerOne : playerTwo;
     }
 
     @Override
     public String toString() {
         return "Game{" +
                 "playerOne=" + playerOne +
                 ", playerTwo=" + playerTwo +
                 ", playerNumber=" + playerNumber +
                 '}';
     }
 
     private enum TurnState {ASKING, ANSWERING}
 }
