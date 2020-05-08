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
 
    // TODO (FIXED) is it still useful?
 
     /**
      * segment created with four defined parameters : a point, a length, 
      * an angle and a boolean
     * @param beginningOfTheSegment beginning of the segment
      * @param endingOfTheSegment length of the segment
      * @param angleOfTheSegment angle of the segment
      * @param physicalOrNot boolean which will decide if the segment is 
      *        physical or not
      */
    public Segment(Point beginningOfTheSegment, Point endingOfTheSegment)
     {
        this.startingPoint = beginningOfTheSegment;
         this.endingPoint = endingOfTheSegment;
     }
 }
