 package de.skuzzle.polly.sdk.eventlistener;
 
 import de.skuzzle.polly.sdk.IrcManager;
 
 
 /**
  * This is an event which is fired when polly spots a user for the first time (= he joins
  * a channel where polly is on) or if polly lost the user (= he parts the last common 
  * channel or quit).
  * 
  * Note that {@link #getQuitEvent()} returns null if this event is not caused by a quit.
  * 
  * @author Simon
  * @since 0.6.0
  */
 public class SpotEvent extends ChannelEvent {
 
     /** Indicates a quit. */
     public final static int USER_QUIT = 1;
     
     /** Indicates a part */
     public final static int USER_PART = 2;
     
    /** Indicates a join */
     public final static int USER_JOINED = 4;
     
     
     
     private QuitEvent quitEvent;
     private int type;
     
     
     
     /**
      * Creates a new SpotEvent which is caused by a {@link QuitEvent}.
      * 
      * @param e The quit event that caused this is event.
      */
     public SpotEvent(QuitEvent e) {
         super(e.getSource(), e.getUser(), "");
         this.type = USER_QUIT;
         this.quitEvent = e;
     }
     
     
     
     /**
      * Creates a new SpotEvent for Joins/Parts.
      * 
      * @param source The source {@link IrcManager}.
      * @param user The user that joined/parted.
      * @param channel The channel. 
      * @param type The type of this event.
      */
     public SpotEvent(IrcManager source, IrcUser user, String channel, int type) {
         super(source, user, channel);
         this.type = type;
     }
     
     
     
     /**
      * Returns the type of this event. This may be either of the constants: 
      * {@link #USER_JOINED}, {@link #USER_PART}, {@link #USER_QUIT}.
      * 
      * @return The type of this event.
      */
     public int getType() {
         return type;
     }
     
     
     
     /**
      * Gets the quit event if this event was caused by a {@link QuitEvent}. Otherwise
      * returns null.
      * 
      * @return The quitevent that caused this event.
      */
     public QuitEvent getQuitEvent() {
         return quitEvent;
     }
 }
