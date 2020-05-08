 package poker.arturka;
 
 import commands.*;
 import message.data.*;
 import poker.server.Room;
 import poker.server.Tuple2;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Game implements Runnable {
 
     private Deck deck;
     private Players players;
     private int blind;
     private List<Card> table;
     private int maxBet;
     private int state;
     private Room room;
     private boolean endGame;
     private HandEvaluator evaluator;
 
     public Game(Room room){
         deck=new Deck();
         players=new Players();
         blind=30;
         maxBet=0;
         table=new ArrayList<Card>();
         state=0;
         this.room=room;
         for(Tuple2 tuple: room.getUsers()){
             players.addPlayer(tuple.id,tuple.nick);
         }
         endGame=false;
     }
 
     private void triariaSolver(PlayerHand smallest,PlayerHand hand,PlayerHand hand2,List<SendWinnerListCommand.Tuple> winners){
         int betsForWinner;
         betsForWinner=players.fetchBets(smallest.getPlayer().getBet());
         smallest.getPlayer().giveCash(betsForWinner);
         winners.add(new SendWinnerListCommand.Tuple(smallest.getPlayer().getId(),betsForWinner));
         if (hand.getPlayer().getBet()<hand2.getPlayer().getBet()){
             betsForWinner=players.fetchBets(hand.getPlayer().getBet());
             hand.getPlayer().giveCash(betsForWinner);
             winners.add(new SendWinnerListCommand.Tuple(hand.getPlayer().getId(), betsForWinner));
             betsForWinner=players.fetchBets(hand2.getPlayer().getBet());
             hand2.getPlayer().giveCash(betsForWinner);
             winners.add(new SendWinnerListCommand.Tuple(hand2.getPlayer().getId(),betsForWinner));
         }else if(hand.getPlayer().getBet()==hand2.getPlayer().getBet()){
             betsForWinner=players.fetchBets(hand.getPlayer().getBet())/2;
             hand.getPlayer().giveCash(betsForWinner);
             hand2.getPlayer().giveCash(betsForWinner);
             winners.add(new SendWinnerListCommand.Tuple(hand.getPlayer().getId(),betsForWinner));
             winners.add(new SendWinnerListCommand.Tuple(hand2.getPlayer().getId(),betsForWinner));
         }else{
             betsForWinner=players.fetchBets(hand2.getPlayer().getBet());
             hand2.getPlayer().giveCash(betsForWinner);
             winners.add(new SendWinnerListCommand.Tuple(hand2.getPlayer().getId(),betsForWinner));
             betsForWinner=players.fetchBets(hand.getPlayer().getBet());
             hand.getPlayer().giveCash(betsForWinner);
             winners.add(new SendWinnerListCommand.Tuple(hand.getPlayer().getId(), betsForWinner));
         }
     }
 
     private void duariaSolver(PlayerHand equal1, PlayerHand equal2, PlayerHand biggest, List<SendWinnerListCommand.Tuple> winners){
         int betsForWinner;
         betsForWinner=players.fetchBets(equal1.getPlayer().getBet())/2;
         equal1.getPlayer().giveCash(betsForWinner);
         equal2.getPlayer().giveCash(betsForWinner);
         winners.add(new SendWinnerListCommand.Tuple(equal1.getPlayer().getId(),betsForWinner));
         winners.add(new SendWinnerListCommand.Tuple(equal2.getPlayer().getId(),betsForWinner));
         betsForWinner=players.fetchBets(biggest.getPlayer().getBet());
         biggest.getPlayer().giveCash(betsForWinner);
         winners.add(new SendWinnerListCommand.Tuple(biggest.getPlayer().getId(),betsForWinner));
     }
 
     private void endGame(){
         System.out.println("ENTER ENDGAME");
         if (!endGame) {
             System.out.println("ENTER IF ENDGAME");
             List<SendWinnerListCommand.Tuple> winners =new ArrayList<SendWinnerListCommand.Tuple>();
             if (players.playersLeft().size()>1){
                 evaluator=new HandEvaluator(players.playersLeft(), table);
                 List<PlayerHand> bestPlayers=evaluator.getPlayerHandEvaluation();
                 int i=0;
                 int betsForWinner;
                 PlayerHand currentWinnerHand;
                 PlayerHand anotherWinnerHand;
                 PlayerHand thirdWinnerHand;
                 currentWinnerHand=bestPlayers.get(i);
                 if (bestPlayers.size()>=i+3){
                     anotherWinnerHand=bestPlayers.get(i + 1);
                     thirdWinnerHand=bestPlayers.get(i + 2);
                     if(currentWinnerHand.getPosition()==anotherWinnerHand.getPosition()
                             &&currentWinnerHand.getPosition()==thirdWinnerHand.getPosition()){
                         int curVAno=currentWinnerHand.getPlayer().getBet()-anotherWinnerHand.getPlayer().getBet();
                         int curVThi=currentWinnerHand.getPlayer().getBet()-thirdWinnerHand.getPlayer().getBet();
                         int anoVThi=anotherWinnerHand.getPlayer().getBet()-thirdWinnerHand.getPlayer().getBet();
 
                         if(curVAno==0&&curVThi==0){
                             System.out.println("Case when all player have same bets");
                             betsForWinner=players.fetchBets(anotherWinnerHand.getPlayer().getBet())/3;
                             currentWinnerHand.getPlayer().giveCash(betsForWinner);
                             anotherWinnerHand.getPlayer().giveCash(betsForWinner);
                             thirdWinnerHand.getPlayer().giveCash(betsForWinner);
                             winners.add(new SendWinnerListCommand.Tuple(currentWinnerHand.getPlayer().getId(), betsForWinner));
                             winners.add(new SendWinnerListCommand.Tuple(anotherWinnerHand.getPlayer().getId(),betsForWinner));
                             winners.add(new SendWinnerListCommand.Tuple(thirdWinnerHand.getPlayer().getId(),betsForWinner));
                         }else if(curVAno<0&&curVThi<0){
                             System.out.println("Case when current has smallest bet");
                             triariaSolver(currentWinnerHand,anotherWinnerHand,thirdWinnerHand,winners);
                         }else if(curVAno>0&&anoVThi<0){
                             System.out.println("Case when another has smallest bet");
                             triariaSolver(anotherWinnerHand,currentWinnerHand,thirdWinnerHand,winners);
                         }else if(curVThi>0&&anoVThi>0){
                             System.out.println("Case when third has smallest bet");
                             triariaSolver(thirdWinnerHand,anotherWinnerHand,currentWinnerHand,winners);
                         }else if(curVAno==0){
                             System.out.println("Case when current and another have same bets");
                             duariaSolver(currentWinnerHand,anotherWinnerHand,thirdWinnerHand,winners);
                         }else if(curVThi==0){
                             System.out.println("Case when current and third have same bets");
                             duariaSolver(currentWinnerHand,thirdWinnerHand,anotherWinnerHand,winners);
                         }else if(anoVThi==0){
                             System.out.println("Case when another and third have same bets");
                             duariaSolver(thirdWinnerHand,anotherWinnerHand,currentWinnerHand,winners);
                         }
                     }
                 }
                 if(bestPlayers.size()>=i+2){
                     anotherWinnerHand=bestPlayers.get(i+1);
                     if(currentWinnerHand.getPosition()==anotherWinnerHand.getPosition()){
                         int curVAno=currentWinnerHand.getPlayer().getBet()-anotherWinnerHand.getPlayer().getBet();
                         if (curVAno<0){
                             betsForWinner=players.fetchBets(currentWinnerHand.getPlayer().getBet());
                             currentWinnerHand.getPlayer().giveCash(betsForWinner);
                             winners.add(new SendWinnerListCommand.Tuple(currentWinnerHand.getPlayer().getId(),betsForWinner));
                         }else if(curVAno==0){
                             betsForWinner=players.fetchBets(currentWinnerHand.getPlayer().getBet())/2;
                             currentWinnerHand.getPlayer().giveCash(betsForWinner);
                             anotherWinnerHand.getPlayer().giveCash(betsForWinner);
                             winners.add(new SendWinnerListCommand.Tuple(currentWinnerHand.getPlayer().getId(),betsForWinner));
                             winners.add(new SendWinnerListCommand.Tuple(anotherWinnerHand.getPlayer().getId(),betsForWinner));
                         }else{
                             betsForWinner=players.fetchBets(anotherWinnerHand.getPlayer().getBet());
                             anotherWinnerHand.getPlayer().giveCash(betsForWinner);
                             winners.add(new SendWinnerListCommand.Tuple(anotherWinnerHand.getPlayer().getId(),betsForWinner));
                         }
                     }
                 }
             }else if(players.playersLeft().size()>0){
                 players.playersLeft().get(0).giveCash(players.getPot());
                 winners.add(new SendWinnerListCommand.Tuple(players.playersLeft().get(0).getId(), players.getPot()));
             }else{
                 Thread.currentThread().interrupt();
                 return;
             }
             room.Broadcast(new SendWinnerListCommand(winners));
             System.out.println("BROADCAST WINNER");
             for(Player currentPlayer: players.getPlayersList()){
                 if(currentPlayer.isInGame()&&currentPlayer.getCash()==0){
                     currentPlayer.toggleInGame();
                 }
                 room.Broadcast(new ChangeCashCommand(currentPlayer.getId(),currentPlayer.getCash()));
                 System.out.println("BROADCAST CASH UPDATE");
             }
             players.fetchBets(99999);
             endGame=true;
         }
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
         try {
             Thread.sleep(1000);
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
         System.out.println("Game thread started");
 //        for(int id: Room.getUsers()){
 //            players.addPlayer(id);
 //        }
         players.getDealer();
         while(true){
            state=0;
             room.Broadcast(new FlopCommand(null,null,null));
             room.Broadcast(new TurnRiverCommand(null, TurnRiverCommand.RorT.TURN));
             room.Broadcast(new TurnRiverCommand(null, TurnRiverCommand.RorT.RIVER));
             for(Player player:players.getPlayersList()){
                 room.sendToUser(player.getId(), new SendPlayerListCommand(players.getSafeList(player)));
                 System.out.println("Send to: "+player.getId()+" SEND PLAYER LIST");
             }
             endGame=false;
             deck.shuffleDeck();
             for(Player currentPlayer: players.getPlayersList()){
                 currentPlayer.unFold();
                 currentPlayer.giveCards(deck.getTopCard(),deck.getTopCard());
                 room.sendToUser(currentPlayer.getId(),new SendCardsCommand(currentPlayer.getId(),currentPlayer.getHand()[0],currentPlayer.getHand()[1]));
                 System.out.println("Send to: "+currentPlayer.getId()+" HAND");
             }
             Player oldDealer=players.nextDealer();
             room.Broadcast(new ChangeDealersCommand(oldDealer.getId(),players.getDealer().getId()));
             System.out.println("BROADCAST NEW DEALER");
             Player nextPlayer=players.getNextPlayer(players.getDealer());
             if (!nextPlayer.bet(blind/2)){
                 nextPlayer.bet(nextPlayer.getCash());
             }
             raiseBet(nextPlayer.getBet());
             room.Broadcast(new PlayerMoveCommand(new PlayerMove(nextPlayer.getId(),ClientTurn.BLIND,nextPlayer.getBet(),nextPlayer.getCash())));
             System.out.println("BROADCAST SMALL BLIND");
             nextPlayer=players.getNextPlayer(nextPlayer);
             if (!nextPlayer.bet(blind)){
                 nextPlayer.bet(nextPlayer.getCash());
             }
             raiseBet(nextPlayer.getBet());
             room.Broadcast(new PlayerMoveCommand(new PlayerMove(nextPlayer.getId(),ClientTurn.BLIND,nextPlayer.getBet(),nextPlayer.getCash())));
             System.out.println("BROADCAST BIG BLIND");
             Player firstBetter=players.getNextPlayer(nextPlayer);
             Player better=firstBetter;
             ClientResponse move;
             while(state<4&&!endGame){
                 do{
                     if(!better.hasFolded()&&better.getCash()>0){
                         if(better.getBet()==maxBet){
                             move = room.sendToUser(better.getId(),new FRCheckCommand());
                             System.out.println("Send to: "+better.getId()+" DO CHECK");
                         }else{
                             move = room.sendToUser(better.getId(),new FRCallCommand());
                             System.out.println("Send to: "+better.getId()+" DO CALL");
                         }
                         if (move==null){
                             move=new ClientResponse(ClientTurn.EXIT,1);
                         }
                         System.out.println("Player: "+better.getId()+" has made turn "+move.turn);
                         switch(move.turn){
                             case FOLD:
                                 better.Fold();
                                 room.Broadcast(new PlayerMoveCommand(new PlayerMove(better.getId(),ClientTurn.FOLD,better.getBet(),better.getCash())));
                                 System.out.println("BROADCAST FOLD");
                                 break;
                             case CHECK:
                                 room.Broadcast(new PlayerMoveCommand(new PlayerMove(better.getId(),ClientTurn.CHECK,better.getBet(),better.getCash())));
                                 System.out.println("BROADCAST CHECK");
                                 break;
                             case CALL:
                                 better.bet(maxBet-better.getBet());
                                 room.Broadcast(new PlayerMoveCommand(new PlayerMove(better.getId(),ClientTurn.CALL,better.getBet(),better.getCash())));
                                 System.out.println("BROADCAST CALL");
                                 break;
                             case RAISE:
                                 better.bet(move.getBet());
                                 raiseBet(better.getBet());
                                 firstBetter=better;
                                 room.Broadcast(new PlayerMoveCommand(new PlayerMove(better.getId(),ClientTurn.RAISE,better.getBet(),better.getCash())));
                                 System.out.println("BROADCAST RAISE");
                                 break;
                             case EXIT:
                                 better.Fold();
                                 better.toggleInGame();
                                 room.Broadcast(new PlayerMoveCommand(new PlayerMove(better.getId(),ClientTurn.EXIT,better.getBet(),better.getCash())));
                                 System.out.println("BROADCAST FOLD");
                                 room.removeUser(better.getId());
                                 players.removePlayer(better.getId());
                         }
                         if(players.playersLeft().size()<2){
                             endGame();
                             break;
                         }
                     }
                     better=players.getNextPlayer(better);
                 }while (better!=firstBetter&&!endGame);
                 if(!endGame){
                     switch (state){
                         case 0:
                             table.add(deck.getTopCard());
                             table.add(deck.getTopCard());
                             table.add(deck.getTopCard());
                             room.Broadcast(new FlopCommand(table.get(0),table.get(1),table.get(2)));
                             System.out.println("BROADCAST FLOP");
                             state++;
                             break;
                         case 1:
                             deck.getTopCard();
                             table.add(deck.getTopCard());
                             room.Broadcast(new TurnRiverCommand(table.get(3), TurnRiverCommand.RorT.TURN));
                             System.out.println("BROADCAST TURN");
                             state++;
                             break;
                         case 2:
                             deck.getTopCard();
                             table.add(deck.getTopCard());
                             room.Broadcast(new TurnRiverCommand(table.get(4), TurnRiverCommand.RorT.RIVER));
                             System.out.println("BROADCAST RIVER");
                             state++;
                             break;
                         default:
                             state++;
                     }
                 }
             }
             endGame();
         }
     }
 }
