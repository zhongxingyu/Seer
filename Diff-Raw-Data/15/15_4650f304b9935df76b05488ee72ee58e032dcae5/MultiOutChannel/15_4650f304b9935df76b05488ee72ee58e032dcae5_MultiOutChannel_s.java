 package cryptocast.comm;
 
 /**
 * Multiplexes several OutChannels so that they can be used as a single
  * destination.
  */
 public class MultiOutChannel implements OutChannel {
     /**
      * Adds the given channel to the list of receivers.
      * @param channel the channel to add
      */
     public void addChannel(OutChannel channel) { }
     /**
      * Removes the given channel from the list of receivers.
      * @param channel the channel to remove
      */
     public void removeChannel(OutChannel channel) { }
 
     /**
      * Sends the given data.
      * @param data the data to send
      */
     public void send(byte[] data) { }
 }
