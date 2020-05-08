 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.timo.paginator;
 
 /**
  * Simple POJO component providing information about
  * a segment information.
  * @author Timoteo Ponce
  */
 public class Segment {
 
     private final int fromIndex;
     private final int toIndex;
 
     public Segment(int fromIndex, int toIndex) {
         this.fromIndex = fromIndex;
         this.toIndex = toIndex;
     }
 
     /**
      * Segment start point.
      * @return
      */
     public int getFromIndex() {
         return fromIndex;
     }
 
     /**
      * Segment end point.
      * @return
      */
     public int getToIndex() {
         return toIndex;
     }
 
	public int getSize(){
		return toIndex - fromIndex;
	}

     @Override
     public String toString() {
         return "Range{" + "fromIndex=" + fromIndex + ",toIndex=" + toIndex + '}';
     }
 }
