 package uk.hux;
 
 public abstract class UserStatus
 {
   public final long userID;
   public final long timestamp; //unix
  
  protected UserStatus(long uid, long time)
  {
    this.userId = uid;
    this.timestamp = time;
  }
 }
