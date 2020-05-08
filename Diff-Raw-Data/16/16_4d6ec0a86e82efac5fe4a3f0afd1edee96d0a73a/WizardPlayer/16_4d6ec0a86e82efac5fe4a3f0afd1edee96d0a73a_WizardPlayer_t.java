 package cardGame.wizard;
 
 import cardGame.CardGame;
 import cardGame.CardGameCard;
 import cardGame.CardGamePlayer;
 import java.util.List;
 
 public class WizardPlayer extends CardGamePlayer {
 
     protected int currentBid;
     protected int tricks;
 
     public WizardPlayer(CardGame game, WizardController controller) {
         super(game, controller);
     }
 
     public int getCurrentBid() {
         return currentBid;
     }
 
     public void setCurrentBid(int currentBid) {
         this.currentBid = currentBid;
     }
 
     public int getTricks() {
         return tricks;
     }
 
     public void resetTricks() {
         this.tricks = 0;
     }
 
     public void notifyBid() {
        ((WizardController) controller).bid();
     }
 
     public void notifyTrickCompleted(List<CardGameCard> trick, CardGamePlayer player) {
         ((WizardController) controller).notifyTrickCompleted(trick, player);
     }
 
     public void addTrick() {
         tricks++;
 
     }
 
     public String toString() {
         return super.toString() + "(" + tricks + "/" + currentBid + ")";
     }
 }
