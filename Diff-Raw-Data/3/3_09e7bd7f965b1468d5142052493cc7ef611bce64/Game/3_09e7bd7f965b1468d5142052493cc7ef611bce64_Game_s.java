 import java.util.*;
 public class Game{
     private final int STARTINGCASH = 200;
     ArrayList<Player> playerList;
     ArrayList<Deck> deckList;
     Player currPlayer;
     int currPlayerIndex;
     Hand currHand;
     int currHandIndex;
     //int numDecks, numPlayers;
     int numDiscardedDecks = 0;
     
     public Game(int numPlayers ,  int numDecks){
         startGame(numPlayers , numDecks);
     }
     
     public void startGame(int numPlayers , int numDecks){
         deckList = new ArrayList<Deck>();
         playerList = new ArrayList<Player>();
         
         for(int i = 0; i < numDecks; i ++){
             Deck d = new Deck();
             d.shuffle();
             deckList.add(d);
         }
         
         for(int i = 0; i < numPlayers; i ++){
             playerList.add(new Player("" + i, STARTINGCASH));
         }
 
         for(int i = 0; i < numPlayers; i ++){
             ArrayList<Hand> handList = playerList.get(i).getHands();
             for(int j = 0; j < handList.size(); j ++){
                 currHand = handList.get(j);
                 if(deckList.get(0).getNumCards() <= 0)
                     deckList.remove(0);
 
                currHand.addCard(deckList.get(0).draw());
 		Card c = deckList.get(0).draw();
 		c.faceDown();
 		currHand.addCard(c);
             }
         }
         currPlayerIndex = 0;
         currPlayer = playerList.get(0);
         currHandIndex = 0;
         currHand = currPlayer.getHands().get(0);
     }
 
     public void endAction(){
         currHandIndex = currHandIndex + 1;
         if(currHandIndex < currPlayer.getHands().size())
             currHand = currPlayer.getHands().get(currHandIndex);
         else{
             currPlayerIndex = (currPlayerIndex + 1) % playerList.size();
             currPlayer = playerList.get(currPlayerIndex);
             currHandIndex = 0;
             currHand = currPlayer.getHands().get(currHandIndex);
         }
     }
 
     public void stand(){
         endAction();
     }
 
     public void hit(){
         if(deckList.get(0).getNumCards() <= 0){
             numDiscardedDecks ++;
             deckList.remove(0);
         }
 
         currHand.addCard(deckList.get(0).draw());
         
         if(currHand.leastValue() > 21)
             endAction();
     }
     
     public void doubleDown(){
         if(currPlayer.getLargestHand().size() == 2){
             currPlayer.bet(currPlayer.getBet() * 2);
             if(deckList.get(0).getNumCards() <= 0){
                 numDiscardedDecks ++;
                 deckList.remove(0);
             }
             Card currCard = deckList.get(0).draw();
             currCard.faceDown();
             currHand.addCard(currCard);
             
             if(currHand.leastValue() > 21)
                 endAction();
         }
     }
     
     public void split(){
         if(currHand.size() == 2){
             if(currHand.getCards().get(0) == currHand.getCards().get(1)){
                 currPlayer.bet(currPlayer.getBet() * 2);
                 currPlayer.createNewHand();
                 currPlayer.getHands().get(currPlayer.getHands().size() - 1).addCard(currHand.getCards().remove(0));
             }
         }
     }   
     public int getProbablity(int val){
         return getProbablity(val , currPlayer);
     }
 
     public int getProbablity(int val , Player p){
         
         int numVal = getNumCardsVal(val , p);
         
         if(val == 10)
             return (16 * deckList.size() - numVal) / 
                 (52 * deckList.size() - (52 * numDiscardedDecks + deckList.get(0).getNumRemovedCards()));
         else  return (4 * deckList.size() - numVal) / 
                   (52 * deckList.size() - (52 * numDiscardedDecks + deckList.get(0).getNumRemovedCards()));
     }
     
     public int probabilityOfBusting(){
         return probabilityOfBusting(currPlayer);
     }
 
     public int probabilityOfBusting(Player p){
         int prob = 0;
         for(int i = 0; i < p.getHands().size(); i ++){
             Hand currHand = p.getHands().get(i);
             for(int j = 21 - currHand.leastValue(); j <= 11; j++)
                 prob += getProbablity(j);
         }
         return prob;
     }
     
     public int probablityOfGreaterHand(Player to){
         probabilityOfGreaterHand(currPlayer , to);
     }
 
     public int probablityOfGreaterHand(Player curr , Player to){
         int prob = 0;
         for(int i = 0; i < curr.getHands().size(); i ++){
             Hand currHand = curr.getHands().get(i);
             for(int j = 0; j < to.getHands().size(); j ++){
                 Hand toHand = to.getHands().get(j);
                 for(int k = toHand.maxValue() - currHand.maxValue(); k <= 11;  k ++){
                     prob += getNumCardsVal(k , curr);
                 }
             }
         }
     }
         
     public int getNumCardsVal(int val , Player p){
         int numVal = 0;
         for(int i = 0; i < playerList.size(); i ++){
             ArrayList<Hand> currHands = playerList.get(i).getHands();
             for(int j = 0; j < currHands.size(); j ++){
                 ArrayList<Card> currHand = currHands.get(j).getCards();
                 for(int h = 0; h < currHand.size(); h ++){
                     if(val == 1){
                         if(currHand.get(h).getValue() == 11 && ( !currHand.get(h).isFaceDown() ||  p.equals(playerList.get(i))))
                             numVal ++;
                     }
                 else if(currHand.get(h).getValue() == val && (!currHand.get(h).isFaceDown() || p.equals(playerList.get(i))))
                     numVal ++;
                 }
             }
         }
         return numVal;
     }
             
 }
     
     
 
     
     
 
     
         
         
