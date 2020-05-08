 package cmf.eventing.patterns.streaming;
 
 /**
  * Working in conjunction {@link IStreamingCollectionHandler}, this notifies
  * interested parties in how many events have been received before delivering
  * the {@link java.util.Collection} of type TEVENT.
  * <p>
  *   This can be useful for delivering updates to the UI even though the
  *   result set has not been fully received yet.
  * </p>
  * User: jholmberg
  * Date: 6/6/13
  */
 public interface IStreamingProgressUpdater {
     void updateProgress(String sequenceId, int numEventsProcessed);
 }
