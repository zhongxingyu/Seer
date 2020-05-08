 package cmf.eventing.patterns.streaming;
 
 import cmf.eventing.IEventConsumer;
 
 /**
  * Adds behavior to the {@link cmf.eventing.IEventConsumer} that enables subscribers of
  * events to receive a stream of events and store them in a collection or
  * to stream reader.
  *
  * User: jholmberg
  * Date: 6/5/13
  */
 public interface IStreamingEventConsumer extends IEventConsumer {
 
     /**
      * Subscribe to a stream of events that will get handled to a {@link java.util.Collection}.
      * <p>See {@link IStreamingCollectionHandler#getProgress()} as a way to
      * get a progress check on how many events have been processed.
      * </p>
      * <p>
      *     This offers the subscriber a simpler API allowing them to get the entire collection
      *     of events once the last one has been received from the producer.
      * </p>
      * @param handler
      * @param <TEVENT>
      * @throws Exception
      */
     <TEVENT> void subscribeToCollection(IStreamingCollectionHandler<TEVENT> handler) throws Exception;
 
     /**
      * Subscribe to a stream of events that will get handled by a {@link IStreamingReaderHandler}.
      * <p>
      *     This offers the subscriber a lower latency API to immediately pull events from a
      *     stream as they arrive.
      * </p>
      * @param handler
      * @param <TEVENT>
      * @throws Exception
      */
    <TEVENT> void subscribeToNotifier(IStreamingReaderHandler<TEVENT> handler) throws Exception;
 
 
 }
