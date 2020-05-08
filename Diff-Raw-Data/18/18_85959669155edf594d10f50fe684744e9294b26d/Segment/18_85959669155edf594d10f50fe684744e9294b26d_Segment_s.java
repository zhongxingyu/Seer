 package fr.iutvalence.java.mp.RollingBall;
 
 // TODO (FIXED) it seems easier to consider a segment as defined by 2 points
 /**
  * piece of the physical field
  * the notion of "physical" : if the segment is physical (true), the ball 
  * will not go through the segment, otherwise it will
  * @author andrejul
  *
  */
 public class Segment
 {
     /**
      * starting point
      */
     private Point startingPoint;
 
     /**
      * length of the segment
      */
     private Point endingPoint;
 
    // TODO (think about it) is it still useful?
    /**
     * boolean which will decide if the segment is physical or not
     */
    private boolean isPhysical;
 
     /**
      * segment created with four defined parameters : a point, a length, 
      * an angle and a boolean
     * @param beginingOfTheSegment beginning of the segment
      * @param endingOfTheSegment length of the segment
      * @param angleOfTheSegment angle of the segment
      * @param physicalOrNot boolean which will decide if the segment is 
      *        physical or not
      */
    public Segment(Point beginingOfTheSegment, Point endingOfTheSegment, 
            boolean physicalOrNot)
     {
        this.startingPoint = beginingOfTheSegment;
         this.endingPoint = endingOfTheSegment;
        this.isPhysical = physicalOrNot;
     }
 }
