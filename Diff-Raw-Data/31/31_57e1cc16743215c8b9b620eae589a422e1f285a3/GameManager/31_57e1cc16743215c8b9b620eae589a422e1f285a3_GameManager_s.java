 package me.kukkii.janken;
 
 import me.kukkii.janken.bot.AbstractBot;
 import me.kukkii.janken.bot.BotManager;
 
 public class GameManager{
 
   private JankenFragment fragment;
   private MySQLiteOpenHelper dataManager;
 
   private Judge judge;
   private AbstractBot bot;
   private Hand userHand;
   private Hand botHand;
   private Result result;
 
   private static final int timeJan = 2500;
   private static final int timeKen = 1000;
   private static final int timePon = 1000;
   private static final int timeAfterPon = 50;
   private static final int timeTilNewGame = 2500;
   private static final int timeTilPon = timeJan + timeKen + timePon;
 
   private Thread gameThread;
   
   private boolean gameIsRunning;
   
   public GameManager(JankenFragment activity, MySQLiteOpenHelper dataManager) {
     this.fragment = activity;
     this.dataManager = dataManager;
     judge = new Judge();
     startGame();
   }
 
   private void startGame(){
     userHand = Hand.UNKNOWN;
     bot =(AbstractBot) BotManager.getManager().next();
     fragment.showBot(bot);
 
     gameThread = new Thread(new Runnable() {
       public void run() {
         gameIsRunning = true;
         game();
       }
     });
     gameThread.start();
 
     new Thread(new Runnable() {
       public void run() {
         sleep(timeTilPon + bot.getTiming());
        setBotHand( bot.hand2(userHand) );
       }
     }).start();
   }
 
   public void sleep(int msec) {
     try{
       Thread.sleep(msec);
     }
     catch (InterruptedException e){
     }
   }
 
   public void jan(){
     fragment.showJan();
   }
 
   public void ken(){
     fragment.showKen();
   }
    
   public void pon(){
     fragment.showPon();
   }
 
   public void afterPon(){
     if(! fragment.isResumed0()){
       return;
     }
     result = judge.judge(userHand, botHand);
     dataManager.writeResultToSQL(result);
     fragment.showResult(dataManager.getResultAsString(bot,userHand,botHand,result));
   }
 
   public void game() {
     sleep(timeJan);
     if(!gameIsRunning){
       return;
     }
     jan();
     sleep(timeKen);
     if(!gameIsRunning){
       return;
     }
     ken();
     sleep(timePon);
     if(!gameIsRunning){
       return;
     }
     pon();
     sleep(timeAfterPon);
     if(!gameIsRunning){
       return;
     }
     afterPon();
     if(! fragment.isResumed0()){
       return;
     }
     sleep(timeTilNewGame);
     if(!gameIsRunning){
       return;
     }
     if(! fragment.isResumed0()){
       return;
     }
     startGame();
   }
 
   public void setUserHand(Hand hand){
     this.userHand = hand;
   }
 
   public void setBotHand(Hand hand){
     botHand = hand;
     fragment.showBotHand(hand);
   }
  
   public void killGameThread(){
  //   gameThread.stop();
     gameIsRunning = false;
   }
 }
