 package cmf.eventing.patterns.streaming;
 
 import cmf.eventing.IEventHandler;
 
 /**
  * Adds behavior to the {@link cmf.eventing.IEventHandler} allowing it to process
  * a stream of events sent via the {@link IStreamingEventBus}
  * User: jholmberg
  * Date: 6/5/13
  */
 public interface IStreamingReaderHandler<TEVENT> extends IEventHandler<TEVENT> {
     /**
     * Streams all events of type TEVENT as they are received from the bus. and places into into an
      * {@link IStreamingEventItem} that can be returned immediately to the caller.
      * <p>
      *     This method will continue to be called until the second to last message in the
      *     sequence is received. The last message in the sequence will be passed through
      *     the {@link IStreamingReaderHandler#onSequenceFinished(IStreamingEventItem)} method.
      * </p>
      * <p>
      *     This implementation reduces the latency that the collection based option has but requires
      *     a little more complex code to handle the results as they are received.
      * </p>
      * @param eventItem
      * @return
      */
     Object onSequenceEventRead(IStreamingEventItem<TEVENT> eventItem);
 
     /**
      * Called when the last event in a sequence is received.
      * @param eventItem
      * @return
      */
     Object onSequenceFinished(IStreamingEventItem<TEVENT> eventItem);
 }
