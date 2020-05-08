 package Roma.Cards;
 
 import Roma.Player;
 import Roma.PlayerInterfaceFiles.CancelAction;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Andrew
  * Date: 9/05/12
  * Time: 6:26 PM
  * To change this template use File | Settings | File Templates.
  */
 public class CardHolder implements Card{
     private final boolean isWrapper = false;
 
     private boolean playable = false;
     private Card contents;
 
     public CardHolder(Card card){
         contents = card;
     }
 
     public void setPlayable(boolean playable){
         this.playable = playable;
     }
 
     public boolean getPlayable(){
         return playable;
     }
 
     public Card getContents(){
         return contents;
     }
 
     public void setContents(Card contents) {
         this.contents = contents;
     }
 
     public Card getContainer(){
         return null;
     }
 
     public void setContainer(Card holder){
         assert false;
     }
 
     public boolean isActivateEnabled() {
         return contents.isActivateEnabled();
     }
 
     public String getName() {
         return contents.getName();
     }
 
     public String getType() {
         return contents.getType();
     }
 
     public String getDescription() {
         return contents.getDescription();
     }
 
     public int getCost() {
         return contents.getCost();
     }
 
     public int getDefense() {
         return contents.getDefense();
     }
 
     public boolean isWrapper() {
         return isWrapper();
     }
 
     public String toString() {
         return "Card Name: " + getName() + "; Type: " + getType() +
                 "\nDescription: " + getDescription() +
                 "\nCost: " + getCost() + "; Defence: " + getDefense();
     }
 
     @Override
     public void gatherData(Player player, int position) throws CancelAction {
         contents.gatherData(player, position);
     }
 
     @Override
     public void activate(Player player, int position) {
         contents.activate(player, position);
     }
 
     @Override
     public void enterPlay(Player player, int position) {
         contents.enterPlay(player, position);
     }
 
     @Override
     public void leavePlay() {
         Wrapper wrapper;
         //remove all wrappers
        while(contents.isWrapper()){
             wrapper = (Wrapper) contents;
             wrapper.deleteThisWrapper();
         }
         contents.leavePlay();
     }
 }
