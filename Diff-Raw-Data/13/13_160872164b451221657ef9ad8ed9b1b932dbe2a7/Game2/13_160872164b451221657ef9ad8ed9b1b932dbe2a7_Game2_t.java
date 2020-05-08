 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package pokerbot.Game;
 
 import pokerbot.Evaluator.Card;
 import pokerbot.Evaluator.Hand;
 import pokerbot.Evaluator.Deck;
 import pokerbot.Evaluator.HandEvaluator;
 import pokerbot.Bot.Player;
 
 import java.util.Random;
 import java.util.ArrayList;
 import pokerbot.OddsCalculator.Calculator;
 /**
  *
  * @author amengistu
  */
 public class Game2{
     public static final int PRE_FLOP = 0;
     public static final int FLOP = 1;
     public static final int TURN = 2;
     public static final int RIVER = 3;
     public static final int IDLE = -1;
     public static final int MAX_PLAYERS = 3;
     
     public int startingRoll;
     private Deck deck;
     private Player[] players;
     private int numPlayers;
     private int state;
     private int dealer;
     private Card[] table;
     
     public Game2(){
         deck = new Deck();
         numPlayers = 0;
         dealer = 0;
         startingRoll = 2000;
         players = new Player[MAX_PLAYERS];
         state = IDLE;
         table = new Card[5];
     }
     
     public void add(Player p){
         if(numPlayers < players.length)
             players[numPlayers++] = p;
     }
     public void add(Player ... ps){
         if(numPlayers + ps.length <= players.length)
             for(Player p : ps)
                 players[numPlayers++] = p;
     }
     public int getNumPlayers(){
         return numPlayers;
     }
     public int getNumActivePlayers(){
         int ret = 0;
         for(Player p : players)
             if(p.isPlaying())
                 ret++;
         return ret;
     }
     public int getNumCommitted(){
         int ret=0;
         for(Player p : players){
             if(p.committed())
                 ret++;
         }
         return ret;
     }
     public int smallBlind(){
         return (int)(startingRoll/200.f);
     }
     public int bigBlind(){
         return (int)(startingRoll/100.f);
     }
     public Hand getTable(){
         StringBuffer hand = new StringBuffer("");
         int numCards = 0;
         for(int i=0; i<table.length; i++){
             if(table[i] != null){
                 hand.append(table[i]+" ");
                 numCards++;
             }
         }
         Hand h = new Hand(hand.toString());
         return h;
     }
     public int getState(){
         return state;
     }
     public boolean doBetting(PotMaker potMaker){
         int amountToCall = 0;
         int betStart = dealer+1;
         int playing = 0;
         boolean bigBlindRound = false;
         int lastCall = 0;
         if(state == PRE_FLOP){
             bigBlindRound = true;
             amountToCall = bigBlind();
             betStart = (dealer+3)%numPlayers;
             playing = 1;    //Big blind automatically called amountToCall
             lastCall = dealer+2;
             try{
                 potMaker.add(smallBlind(), dealer+1);
                 players[dealer+1].subFromBankRoll(smallBlind());
                 potMaker.add(bigBlind(), dealer+2);
                 players[dealer+2].subFromBankRoll(bigBlind());
             }catch(Exception e){
                 e.printStackTrace(System.out);
             }
         }
         for(int i=0; i<players.length; i++){
             if(i==0)
                 playing = 1;
             int pos = (betStart + i)%numPlayers;
            if(!bigBlindRound && i == players.length-1 || (players[pos].folded() || !players[pos].isPlaying()) || (playing == 1 && i == players.length-1 && amountToCall > 0))
                 continue;
             int a2call = amountToCall - potMaker.bets[pos];
             int eligible = potMaker.bets[pos];
             for(int j=0; j<numPlayers; j++){
                 if(potMaker.bets[j] > 0 && j != pos)
                     eligible += potMaker.bets[pos];
             }            
             System.out.printf("Player($%d) %d its your turn to call %d: \n",players[pos].getBankRoll(),pos,a2call);
             Response r = players[pos].getResponse(this, a2call, eligible);
             System.out.printf("Player responded with %s\n\n",r);
             if(r.response == Response.FOLD || (r.response == Response.CHECK && a2call == 0))
                 continue;
             else if(r.response == Response.CALL){
                 potMaker.add(a2call, pos);
                 playing++; lastCall = pos;
                 try{
                     players[pos].subFromBankRoll(a2call);
                 }catch(Exception e){ e.printStackTrace(System.out);}
             }else if(r.response == Response.RAISE){
                 bigBlindRound = false;
                 amountToCall = r.amount + a2call;
                 playing++;
                 lastCall = pos;
                 potMaker.add(r.amount + a2call, pos);
                 try{
                     players[pos].subFromBankRoll(a2call + r.amount);
                 }catch(Exception e){ e.printStackTrace(System.out);}
                 i = -1; //Restart loop (new betting round -- somebody raised)
                 betStart = (pos+1)%numPlayers;
                 continue;
             }else{
                 System.out.println("Invalid response "+r+"\n.. exiting!");
                 System.exit(0);
             }
         }
         if(playing == 1){
             int amount = 0;
             for(int x : potMaker.bets)
                 amount += x;
             players[lastCall].addToBankRoll(amount);
             System.out.printf("Everyone folded, player %d wins %d!\n",lastCall,amount);
             return false;
         }
         potMaker.makePots();
         return true;
     }
     public boolean isPreFlop(){
         return (state == PRE_FLOP);
     }
     private static class PotMaker{
         int[] bets;
         ArrayList<Integer> pots;
         ArrayList<ArrayList<Integer>> eligible;
         
         public PotMaker(int numPlayers){
             bets = new int[numPlayers];
             for(int i=0; i<bets.length; i++)
                 bets[i] = 0;
             pots = new ArrayList<Integer>();
             eligible = new ArrayList<ArrayList<Integer>>();
         }
         public void add(int amount, int seatNum){
             if(seatNum >= 0 && seatNum < bets.length)
                 bets[seatNum] += amount;
         }
         public int getEligible(int pos){
             int ret = 0;
             for(int i=0; i<eligible.size(); i++){
                 ArrayList<Integer> elig = eligible.get(i);
                 if(elig.contains(pos))
                     ret += pots.get(i);
             }
             return ret;
         }
         public void makePots(){
             int min = bets[0];
             for(int i=0; i<bets.length; i++){
                 if(min != 0 && bets[i] < min)
                     min = bets[i];
             }
             while(notZero(bets)){
                 ArrayList<Integer> elig = new ArrayList<Integer>();
                 int pot = 0;            
                 for(int i=0; i<bets.length; i++){
                     if(bets[i] >= min){
                         pot += min;
                         elig.add(i);
                         bets[i] -= min;
                     }
                 }
                 pots.add(pot);
                 eligible.add(elig);
             }
         }
         public static boolean notZero(int[] a){
             for(int i : a)
                 if(i != 0) return true;
             return false;
         }
     }
     public static void main(String[] args){
         Game2 g = new Game2();
         for(int i=0; i<3; i++){
             g.add(new Player(i,g.startingRoll));
         }
         PotMaker potMaker = new PotMaker(g.numPlayers);
         while(true){
             g.deck.reset();
             //Deal to all players
             for(Player p : g.players)
                 p.setHoleCards(g.deck.deal(),g.deck.deal());
             g.state = PRE_FLOP;
             if(!g.doBetting(potMaker))
                 continue;
             //Deal flop
             g.table[0] = g.deck.deal();
             g.table[1] = g.deck.deal();
             g.table[2] = g.deck.deal();
             g.state = FLOP;
             if(!g.doBetting(potMaker))
                 continue;
             //Deal turn
             g.table[3] = g.deck.deal();
             if(!g.doBetting(potMaker))
                 continue;
             //Deal river
             g.table[4] = g.deck.deal();
             if(!g.doBetting(potMaker))
                 continue;
             //Showdown
             int[] handRanks = new int[g.numPlayers];
             for(int i=0; i<g.numPlayers; i++){
                 Player p = g.players[i];
                 if(p.isPlaying() && !p.folded()){
                     handRanks[i] = HandEvaluator.evalHand(p.h, g.table);
                 }else
                     handRanks[i] = 1000000000;
             }
             for(int i=0; i<potMaker.pots.size(); i++){
                 int pot = potMaker.pots.get(i);
                 ArrayList<Integer> winners = new ArrayList<Integer>();
                 int min = potMaker.eligible.get(i).get(0);
                 for(int eligible : potMaker.eligible.get(i)){
                     if(handRanks[min] > handRanks[eligible]){
                         min = eligible;
                     }
                 }
                 for(int eligible : potMaker.eligible.get(i)){
                     if(handRanks[eligible] == handRanks[min])
                         winners.add(eligible);
                 }
                 int amountWon = Math.round(pot/(float)winners.size());
                 for(int winner : winners){
                     System.out.printf("Player %d wins %d with a %s!\n**************************\n",
                             winner,amountWon,HandEvaluator.getHandRankAsString(handRanks[winner]));
                     g.players[winner].addToBankRoll(amountWon);
                 }
             }
             int numPlaying = 0;
             for(Player p : g.players){
                 if(p.getBankRoll() == 0)
                     p.setDone();
                 else
                     numPlaying ++;
             }
             if(numPlaying == 1)
                 break;
         }
     }
 }
