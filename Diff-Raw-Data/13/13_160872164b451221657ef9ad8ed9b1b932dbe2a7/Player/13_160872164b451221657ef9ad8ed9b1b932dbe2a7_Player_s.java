 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package pokerbot.Bot;
 
 import pokerbot.Game.Response;
 import pokerbot.Game.Game2;
 import pokerbot.Evaluator.Card;
 import pokerbot.Evaluator.Hand;
 import pokerbot.OddsCalculator.Calculator;
 /**
  *
  * @author scarfaceabel
  */
 public class Player {
     public long id;
     public Behavior b;
     public Card[] h;
     private int seatNumber;
     private int bankRoll;
     private boolean folded,committed;
     
     public Player(int seatNumber){
         this(seatNumber,2000);
     }
     public Player(int seatNumber, int bankRoll){
         b = new Behavior();
         id = new java.util.Random().nextLong();
         this.seatNumber = seatNumber;
         this.bankRoll = bankRoll;
     }
     
     public void setHoleCards(Card c1, Card c2){
         h = (new Card[]{c1,c2});
     }
     public void setHoleCards(Hand hole){
         h = hole.get();
         folded = false;
         committed = false;
     }
     public int getBankRoll(){
         return bankRoll;
     }
     public int getSeatNumber(){
         return seatNumber;
     }
     public void addToBankRoll(int amount){
         bankRoll += amount;
     }
     public void subFromBankRoll(int amount) throws Exception{
         if(bankRoll - amount < 0){
             throw new Exception("Invalid amount!");
         }else{
             bankRoll -= amount;
         }
     }
     public Response getResponse(Game2 g, int amountToCall, int amountEligible){
         double score = 0.0;
         int state = (g.isPreFlop())?Behavior.preFlop:Behavior.postFlop;
         Hand table = null;
         if(!g.isPreFlop())
             table = g.getTable();
         float oddsM = b.get(state,Behavior.oddsM);
         float numPlayersM = b.get(state,Behavior.numPlayersM);
         float numCommM = b.get(state,Behavior.numCommittedM);
         float toCallOverRollM = b.get(state,Behavior.toCallRollRatio);
         float riskM = b.get(state, Behavior.riskM);
         float sumM = oddsM+numPlayersM+numCommM+toCallOverRollM;
         
         double odds = (table != null)?Calculator.go(new Hand(h),table, g.getNumActivePlayers()):
                 Calculator.go(new Hand(h),"", g.getNumActivePlayers());
         double numPlayers = g.getNumActivePlayers()/10.0f;
         double numCommitted = g.getNumCommitted()/9.0f;
         double rollOverCall;
         try{
             rollOverCall = bankRoll/amountToCall;
         }catch(ArithmeticException e){
             rollOverCall = bankRoll;
         }
         rollOverCall /= 200.0;
 
         score = (oddsM*odds + numPlayersM*numPlayers + numCommM*numCommitted + toCallOverRollM*rollOverCall)/sumM;
         float risk = new java.util.Random().nextFloat();
         if(score > 0.5){
             committed = true;
             if(risk <= riskM){
                 if(amountToCall == 0)
                     return Response.checkResponse();
                 else
                     return Response.callResponse(amountToCall);
             }
             int raise = (int)(amountEligible *(score));
             if(raise == 0)
                 raise = (int)(amountToCall * (score));
             raise = (raise>bankRoll)?bankRoll:raise;
             if(raise <= amountToCall)
                 if(amountToCall > 0)
                     return Response.callResponse(amountToCall);
                 else
                     return Response.checkResponse();
             return Response.raiseResponse((int)(raise));
         }
         else if(score > 0.3){
             committed = true;
             return Response.callResponse(amountToCall);
         }
         else if(score > 0.2 && amountToCall > 0){
             if(risk <= riskM){
                 committed = true;
                 int raise = (amountEligible > bankRoll)?bankRoll:amountEligible;
                 if(raise == 0)
                     raise = (int)(amountToCall + amountToCall * (score));
                 raise = (raise>bankRoll)?bankRoll:raise;
                 if(raise <= amountToCall)
                     if(amountToCall > 0)
                         return Response.callResponse(amountToCall);
                     else
                         return Response.checkResponse();
                 return Response.raiseResponse(raise);
             }
             return fold(amountToCall);
         }
         else{
             //TODO: make sure you don't call with a very low score at river
             if(risk <= riskM){
                 committed = true;
                 if(amountToCall > 0)
                     return Response.callResponse(amountToCall);
             }
             return fold(amountToCall);
         }
             
     }
     private Response fold(int amountToCall){
         if(amountToCall == 0)
             return Response.checkResponse();
         committed = false;
         folded = true;
         return Response.foldResponse();
     }
     public boolean isPlaying(){
         return (bankRoll != -1);
     }
     public boolean folded(){
         return folded;
     }
     public boolean committed(){
         return committed;
     }
     public void setDone(){
         bankRoll = -1;
     }
     public String toString(){
         String ret = String.format("Player #%d \nID:%d",seatNumber,id);
         return ret;
     }
     public String toString(boolean details){
         if(!details)
             return toString();
         String ret = toString();
         ret += String.format("Behavior: %s%nCurrent Hand: %s%nSeat number: %d%nBank roll: %d%n",b,h,seatNumber,bankRoll);
         return ret;
     }
     public static void main(String[] args){
         
     }
 }
