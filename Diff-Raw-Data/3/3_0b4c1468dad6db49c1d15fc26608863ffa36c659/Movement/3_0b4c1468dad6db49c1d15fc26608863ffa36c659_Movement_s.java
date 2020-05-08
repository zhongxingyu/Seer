 package player;
 
 import ui.PositionExtractor;
 import utils.RoundCounter;
 
 public class Movement {
     private final RoundCounter roundCounter = new RoundCounter();
     private Position currentPosition = new Position(0);
     private Position previousPosition = new Position(0);
 
     public void walk() {
         previousPosition = currentPosition;
         currentPosition = currentPosition.offset(1);
     }
 
     public void pass() {
         roundCounter.pass();
     }
 
     public boolean isNeedNotSkip() {
         return roundCounter.isExceed();
     }
 
     public void block(int times) {
         roundCounter.setMaxCount(times);
     }
 
     public void jumpToHospital() {
         currentPosition = new PositionExtractor().getHospital();
         block(3);
     }
 
     public Position currentPosition() {
         return currentPosition;
     }
 
     public Position previousPosition() {
         return previousPosition;
     }
 
     public Position offset(int step) {
         return currentPosition.offset(step);
     }
 
     public void leave() {
        final Position INVALID_POSITION = new Position(-1);
         previousPosition = currentPosition;
         currentPosition = INVALID_POSITION;
     }
 
     public boolean equals(Object object) {
         return getClass() == object.getClass() &&
                 previousPosition.equals(((Movement) object).previousPosition) &&
                 currentPosition.equals(((Movement) object).currentPosition);
     }
 }
