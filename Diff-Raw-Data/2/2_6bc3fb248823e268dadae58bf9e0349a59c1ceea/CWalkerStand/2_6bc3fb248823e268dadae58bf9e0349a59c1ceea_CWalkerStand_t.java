 package Source;
 
 import Util.CPosition;
 
 /**
  * User: bohnp1
  * Date: 05.11.13
  * Time: 19:08
  */
 public class CWalkerStand extends CWalker {
 
     public CWalkerStand(CPosition start, CPosition target) {
         super(start, target);
     }
     /**
      * calculates the next position and saves the result to the nextDesiredPosition member var
      * @param roundCount the calculation round count
      * @return true if the new position has no collision with others
      */
 
     @Override
     public boolean calcNextDesiredPosition(Integer roundCount) {
         if(this.desiredPath.size() < 1) {
             this.desiredNextPosition = null;
             return false;
         }
 
         if(roundCount == 1) {
             CPosition nextCheckPoint = this.desiredPath.getFirst();
 
             Double xDelta = nextCheckPoint.getX() - this.currentPosition.getX();
             Double yDelta = nextCheckPoint.getY() - this.currentPosition.getY();
 
             Double dAngle = 0.0;
             if (xDelta != 0.0)
             {
                 dAngle = Math.atan( Math.abs(yDelta) / Math.abs(xDelta) );
             }
 
             double nextStepDeltaX = Math.cos(dAngle) * stepSize * ( xDelta > 0 ? 1 : -1 );
             double nextStepDeltaY = Math.sin(dAngle) * stepSize * ( yDelta > 0 ? 1 : -1 );
 
             this.desiredNextPosition = new CPosition(currentPosition.getX() + nextStepDeltaX, currentPosition.getY() + nextStepDeltaY);
 
             return false;
         }
         else {
            if( this.hasCollisions() ) {
                 this.desiredNextPosition = this.currentPosition;
 
                 /* boolean minOneIsBlocked = false;
                 for(CWalker blockedWalker : this.blockedWith) {
                     minOneIsBlocked = minOneIsBlocked || blockedWalker.isBlocked();
                 }
 
                 if(minOneIsBlocked) {
                     // if minimum one other is blocked, then we stand still and remove the blockation
                     this.desiredNextPosition = this.currentPosition;
                 } */
             }
         }
 
         return false;
     }
 }
