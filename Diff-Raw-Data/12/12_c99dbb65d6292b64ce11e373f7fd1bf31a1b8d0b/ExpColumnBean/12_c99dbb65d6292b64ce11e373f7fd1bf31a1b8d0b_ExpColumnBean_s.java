 package com.mymed.model.data.storage;
 
 import java.util.Arrays;
 
 /**
  * Bean that stores an expiring column.
  * 
  * @author iacopo
  * 
  */
 public class ExpColumnBean {
  private byte[] value;
   private long timestamp;
   private int timeToLive;
 
   public void setValue(final byte[] value) {
     this.value = Arrays.copyOf(value, value.length);
   }
 
   public byte[] getValue() {
     return Arrays.copyOf(value, value.length);
   }
 
   public void setTimestamp(final long timestamp) {
     this.timestamp = timestamp;
   }
 
   public long getTimestamp() {
     return timestamp;
   }
 
   public void setTimeToLive(final int timeToLive) {
     this.timeToLive = timeToLive;
   }
 
   public int getTimeToLive() {
     return timeToLive;
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {
     final int prime = 31;
     int result = 1;
     result = prime * result + timeToLive;
     result = prime * result + (int) (timestamp ^ timestamp >>> 32);
     result = prime * result + Arrays.hashCode(value);
     return result;
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(final Object obj) {
 
     boolean equal = false;
 
     if (this == obj) {
       equal = true;
     } else if (obj instanceof ExpColumnBean) {
       final ExpColumnBean comparable = (ExpColumnBean) obj;
 
       equal = true;
 
       equal &= timeToLive == comparable.getTimeToLive();
       equal &= timestamp == comparable.getTimestamp();
       equal &= Arrays.equals(value, comparable.getValue());
     }
 
     return equal;
   }
 }
