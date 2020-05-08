 package HandicapProcessing;
 
 
 /**
  * Created with IntelliJ IDEA.
  * User: snowhyzhang
  * Date: 13-1-10
  * Time: 上午1:41
  * To change this template use File | Settings | File Templates.
  */
 public class HandicapProcessor {
 
     private iHandicapProcessing hp = new HandicapProcessing();
 
     public static void main(String args[]){
         HandicapProcessor handicapProcessor = new HandicapProcessor();
        handicapProcessor.betOneMatch(1.53, 4, 4.8, -1, 0.875, 0.925, "snow001", "8", "8");
 //        System.out.println("==========");
 //        handicapProcessor.betOneMatch(1.36, 4.75, 8.0, 1, 0.7, 1.23, "snow001", "8", "8");
 //        System.out.println("==========");
 //        handicapProcessor.betOneMatch(1.36, 4.75, 8.0, 1.5, 1.29, 0.69, "snow001", "8", "8");
     }
 
     public void betOneMatch(double win, double push, double lose, double handicap, double winRate, double loseRate,
                             String matchId, String clientId, String cid){
         hp.setMatch(win, push, lose, handicap, winRate, loseRate, matchId, clientId, cid, "A", "B", 4);
         hp.getResult(true);
     }
 }
