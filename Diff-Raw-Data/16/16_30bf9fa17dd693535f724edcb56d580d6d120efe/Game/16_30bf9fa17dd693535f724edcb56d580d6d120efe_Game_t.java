 package poker.arturka;
 
 import commands.FRCallCommand;
 import commands.FRCheckCommand;
 import commands.FlopCommand;
 import commands.TurnRiverCommand;
 import poker.server.Room;
import message.data.ClientTurn;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Game implements Runnable {
 
     private Deck deck;
     private Players players;
     private int blind;
     private List<Card> table;
     private int maxBet;
     private int state;
 
     public Game(Room room){
         deck=new Deck();
         players=new Players();
         blind=30;
         maxBet=0;
         table=new ArrayList<Card>();
         state=0;
         for(int id: room.getUsers()){
             players.addPlayer(id);
         }
     }
 
     private void splitPot(){
 
     }
 
     private boolean raiseBet(int playerPot){
         if (playerPot>maxBet){
             maxBet=playerPot;
             return true;
         }else{
             return false;
         }
     }
 
     public void run() {
 //        for(int id: Room.getUsers()){
 //            players.addPlayer(id);
 //        }
         players.nextDealer();
         Player nextPlayer=players.getNextPlayer(players.getDealer());
         if (!nextPlayer.bet(blind/2)){
             nextPlayer.bet(nextPlayer.getCash());
         }
         raiseBet(nextPlayer.getBet());
         nextPlayer=players.getNextPlayer(nextPlayer);
         if (!nextPlayer.bet(blind)){
             nextPlayer.bet(nextPlayer.getCash());
         }
         raiseBet(nextPlayer.getBet());
         deck.shuffleDeck();
         Player currentPlayer=players.getNextPlayer(players.getDealer());
         do{
             currentPlayer.giveCards(deck.getTopCard(),deck.getTopCard());
         }while(players.getDealer()!=currentPlayer);
         Player firstBetter=players.getNextPlayer(nextPlayer);
         //abstractions, rewrite to actual methods
         Player better=firstBetter;
         do{
             if(!better.hasFolded()){
                 if(better.getBet()==maxBet){
                     String Move = Room.sendToUser(better.getId(),new FRCheckCommand());
                 }else{
                     String Move = Room.sendToUser(better.getId(),new FRCallCommand());
                 }
                 switch(moveEnum){
                    case ClientTurn.FOLD:
                         better.toggleFold();
                         continue;
                    case ClientTurn.CHECK:
                         continue;
                    case ClientTurn.CALL:
                         better.bet(maxBet-better.getBet());
                         continue;
                    case ClientTurn.RAISE:
                         better.bet(moveInt);
                         raiseBet(better.getBet());
                         firstBetter=better;
                         continue;
                    case ClientTurn.EXIT:
                         better.toggleFold();
                         players.removePlayer(better.getId());
                 }
                 if(players.playersLeft()<2){
                     splitPot();
                 }
             }
             better=players.getNextPlayer(better);
         }while (better==firstBetter);
         switch (state){
             case 0:
                 table.add(deck.getTopCard());
                 table.add(deck.getTopCard());
                 table.add(deck.getTopCard());
                 Room.Broadcast(new FlopCommand(table.get(0),table.get(1),table.get(2)));
                 break;
             case 1:
                 deck.getTopCard();
                 table.add(deck.getTopCard());
                 Room.Broadcast(new TurnRiverCommand(table.get(3),TurnRiverCommand.RorT.Turn));
             case 2:
                 deck.getTopCard();
                 table.add(deck.getTopCard());
                 Room.Broadcast(new TurnRiverCommand(table.get(4),TurnRiverCommand.RorT.River));
         }
     }
 }
