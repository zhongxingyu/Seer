 package cmf.eventing.patterns.streaming;
 
 import cmf.eventing.IEventHandler;
 
 import java.util.Collection;
 import java.util.Map;
 
 /**
  * Allows the streaming API gather events received into one collection to be handled by user code.
  * <p>
  *     The generic {@link java.util.Collection} is of type {@link StreamingEventItem} comes sorted based
  *     on the position flag set in each event header. The position along with each event's own headers
  *     can be obtained through the {@link StreamingEventItem}
  * </p>
  * User: jholmberg
  * Date: 6/4/13
  */
 public interface IStreamingCollectionHandler<TEVENT> {
     /**
      * Aggregates all events of type TEVENT and stores them into a {@link java.util.Collection}
      * when the last event was received with the message header "isLast" set to true.
      * <p>
      *     This provides a way to gather all events belonging to a particular sequence and deliver them
      *     and a cohesive collection.
      * </p>
      * <p>
      *     A drawback, however, is that because it's holding the collection until all events in the stream
      *     have been received, it introduces a higher degree of latency into the process.
      *     This can be especially apparent the larger the sequence becomes.
      * </p>
      * @param events The collections of events that all belong to the same sequence
      * @return
      */
     void handleCollection(Collection<StreamingEventItem<TEVENT>> events);
 
     /**
      * Enables subscribers with the ability to know how many events have
      * been processed to date.
     * @param percent Percent of events processed so far.
      * @return
      */
     void onPercentCollectionReceived(double percent);
 
 
     Class<TEVENT> getEventType();
 }
