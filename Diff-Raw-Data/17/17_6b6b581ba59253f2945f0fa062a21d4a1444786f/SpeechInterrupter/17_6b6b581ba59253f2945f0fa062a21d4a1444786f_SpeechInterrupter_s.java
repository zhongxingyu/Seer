 package net.cscott.sdr.recog;
 
 import java.util.LinkedList;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import edu.cmu.sphinx.frontend.BaseDataProcessor;
 import edu.cmu.sphinx.frontend.Data;
 import edu.cmu.sphinx.frontend.DataEndSignal;
 import edu.cmu.sphinx.frontend.DataProcessingException;
 import edu.cmu.sphinx.frontend.DataStartSignal;
 import edu.cmu.sphinx.frontend.DoubleData;
 import edu.cmu.sphinx.frontend.Signal;
 import edu.cmu.sphinx.frontend.endpoint.SpeechEndSignal;
 import edu.cmu.sphinx.frontend.endpoint.SpeechStartSignal;
 
 /**
  * {@link SpeechInterrupter} allows insertion of {@link SpeechEndSignal} and
  * {@link SpeechStartSignal} packets into an audio stream in order to force
  * the interruption of an on-going recognition.  This allows the recognition
  * grammar to be switched asynchronously.
  * <p>
  * The {@link SpeechInterrupter} also runs a thread to pull data eagerly
  * through the frontend; this ensures that interruption events occur at the
  * right point in the timeline, as well as helping upstream
  * {@link LevelMonitor}s, etc, remain in real time (even if the recognizer is
  * running behind).
  */
 public class SpeechInterrupter extends BaseDataProcessor {
     /** Marker class. */
     private static class Interruption implements Data { }
     /** Insertion queue for Data which should be provided to the clients. */
     private final LinkedList<Data> pushbackQueue = new LinkedList<Data>();
     /** Thread-safe queue to buffer incoming data & record interruptions. */
     private final BlockingQueue<Data> dataQueue =
         new LinkedBlockingQueue<Data>();
 
     /** Are we in a speech segment now? */
     private boolean inSpeech = false;
     /** Have we seen the DataStartSignal yet? */
     private boolean inData = false;
     /** Are we processing an interrupt? */
     private boolean interrupting = false;
 
     /** Insert speech start/end markers in the current data stream.
      *  Thread-safe. */
     public void interrupt() {
         dataQueue.clear(); // throw away pending data
         // note there's a race here: not an important one, though.
         dataQueue.add(new Interruption()); // record an interruption
     }
 
     public Data getData() throws DataProcessingException {
 	while (true) {
 	    // pull from the pushbackQueue first, if it's not empty.
 	    Data data = pushbackQueue.poll();
 	    if (data != null) return data;
 	    // otherwise get the next bit of data and/or interruption.
 	    try {
 	        data = dataQueue.take();
 	    } catch (InterruptedException e) {
 	        continue; /* retry */
 	    }
 	    if (data instanceof Interruption) {
 	        if (!inData) continue; // we haven't gotten started yet
 	        if (interrupting) continue; // throw away runs
 	        interrupting = true;
 	        // work around bug in AbstractFeatureExtractor.getData/processFirstCepstrum
 	        // which throws an ArrayStoreException in Arrays.fill if a
 	        // SpeechStartSignal is immediately followed by a SpeechEndSignal.
 	        // So we pad with a frame of silence.
	        Data pad = new DoubleData(new double[320]);
 	        if (inSpeech) {
	            pushbackQueue.add(pad);
 	            pushbackQueue.add(new SpeechEndSignal());
 	            pushbackQueue.add(new SpeechStartSignal());
 	        } else {
 	            pushbackQueue.add(new SpeechStartSignal());
	            pushbackQueue.add(pad);
 	            pushbackQueue.add(new SpeechEndSignal());
 	        }
 	        continue; // go back and pull from the pushbackQueue
 	    }
 	    interrupting = false;
 	    if (data instanceof Signal) {
 	        if (data instanceof DataStartSignal)
 	            inData = true;
 	        if (data instanceof DataEndSignal)
 	            inData = false;
 	        if (data instanceof SpeechStartSignal)
 	            inSpeech = true;
 	        if (data instanceof SpeechEndSignal)
 	            inSpeech = false;
	    }
 	    return data;
 	}
     }
 
     @Override
     public void initialize() {
         super.initialize();
         new PullThread().start();
     }
 
     /** Aggressively pull data from source to ensure that LevelMonitor is
      *  tracking real time, even if recognizer is not. */
     private class PullThread extends Thread {
         PullThread() { setDaemon(true); }
         @Override
         public void run() {
             while (true) {
                 dataQueue.add(getPredecessor().getData());
             }
         }
     }
 }
