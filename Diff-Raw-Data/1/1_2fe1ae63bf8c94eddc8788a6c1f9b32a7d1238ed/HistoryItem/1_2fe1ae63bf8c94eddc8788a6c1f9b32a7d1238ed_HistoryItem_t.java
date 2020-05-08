 package com.HuskySoft.metrobike.ui.utility;
 
 /**
  * This class is immutable to store the history details.
  */
 public class HistoryItem {
     /**
      * The position index of this item.
      */
     private int index;
     /**
      * The address starting from.
      */
     private String from;
     /**
      * The address going to.
      */
     private String to;
 
     /**
      * Constructor to initialize those field. Once this object is created, it
      * will not be changed.
      * 
      * @param indexPos
      *            the position index
      * @param fromAddress
      *            the starting address
      * @param endAddress
      *            the ending address
      */
     public HistoryItem(final int indexPos, final String fromAddress,
             final String endAddress) {
         this.index = indexPos;
         this.from = fromAddress;
         this.to = endAddress;
     }
 
     /**
      * @return int index where the position of this item.
      */
     public final int getIndex() {
         return index;
     }
 
     /**
      * @return String from where the starting address.
      */
     public final String getFrom() {
         return from;
     }
 
     /**
      * @return String to where the ending address.
      */
     public final String getTo() {
         return to;
     }
 }
