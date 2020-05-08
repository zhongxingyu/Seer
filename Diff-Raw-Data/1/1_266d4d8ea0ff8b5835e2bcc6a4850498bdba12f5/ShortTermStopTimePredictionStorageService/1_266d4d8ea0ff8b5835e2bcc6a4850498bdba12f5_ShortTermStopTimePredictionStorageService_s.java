 package org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime;
 
 import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.gtfs.model.String;
 
 /**
  * A short-term store for stop time predictions.
  * 
  * @author <a href="mailto:git@michaelscheper.com">Michael Scheper</a>,
  *         <a href="mailto:Michael.Scheper@rms.nsw.gov.au">New South Wales Roads &amp; Maritime Service</a>
  */
 public interface ShortTermStopTimePredictionStorageService {
 
   /** @see #setMaximumPredictionAgeSeconds(int) */
   public int DEFAULT_MAXIMUM_PREDICTION_AGE_SECONDS = 80;
 
   /** @see #setBucketSizeSeconds(int) */
   public int DEFAULT_BUCKET_SIZE_SECONDS = 30;
 
   /**
    * Gets the stored arrival and departure predictions for a trip and stop.
    * Returns <code>null</code> if no prediction for the trip and stop is stored.
    * Either element of the Pair can also be <code>null</code>.
    * 
    * @param trip the ID for the trip
    * @param stop the ID for the stop
    * @return the arrival and departure times, respectively, as milliseconds
    *         since the epoch, or <code>null</code> if no prediction for the trip
    *         and stop is stored
    */
   public Pair<Long> getPrediction(String trip, String stop);
 
   /**
    * Stores an arrival and departure prediction data for a trip and stop. All
    * times are expressed in <em>seconds</em> since the epoch.
    * 
    * @param trip the ID for the trip
    * @param stop the ID for the stop
    * @param arrivalTimeSeconds the predicted arrival time for the trip at the
    *          stop
    * @param departureTimeSeconds the predicted departure time for the trip at
    *          the stop
    * @param timestampSeconds the time that the prediction was made
    */
   public void putPrediction(String trip, String stop,
       Long arrivalTimeSeconds, Long departureTimeSeconds,
       long timestampSeconds);
 
   /**
    * The maximum age, in seconds, of predictions, before they are eligible for
    * being discarded.
    * 
    * @param maximumPredictionAgeSeconds
    */
   public void setMaximumPredictionAgeSeconds(int maximumPredictionAgeSeconds);
 
   /**
    * The size, in seconds, of the 'buckets' predictions are stored in.
    * Predictions are grouped in buckets by the time the prediction was made, to
    * aid efficient clearing of old predictions.
    * 
    * @param bucketSizeSeconds
    */
   public void setBucketSizeSeconds(int bucketSizeSeconds);
 }
