 package net.cscott.sdr.recog;
 
 import java.util.ArrayList;
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
  */
 public class SpeechInterrupter extends BaseDataProcessor {
     /** Marker class. */
     private static class Interruption { }
     /** Thread-safe queue to record interruptions. */
     private final BlockingQueue<Interruption> interruptQueue =
 	new LinkedBlockingQueue<Interruption>();
     /** Insertion queue for Data which should be provided to the clients. */
     private final LinkedList<Data> dataQueue = new LinkedList<Data>();
     /** Are we in a speech segment now? */
     private boolean inSpeech = false;
     /** Have we seen the DataStartSignal yet? */
     private boolean inData = false;
 
     /** Insert speech start/end markers in the current data stream.
      *  Thread-safe. */
     public void interrupt() {
         interruptQueue.add(new Interruption());
     }
 
     public Data getData() throws DataProcessingException {
 	Data data = dataQueue.poll();
 	if (data != null)
 	    return data;
 	if (inData &&
 	    interruptQueue.drainTo(new ArrayList<Interruption>()) != 0) {
	    // work around bug in AbstractFeatureExtractor.getData/processFirstCepstrum
	    // which throws an ArrayStoreException in Arrays.fill if a
	    // SpeechStartSignal is immediately followed by a SpeechEndSignal.
	    // So we pad with a frame of silence.
	    Data pad = new DoubleData(new double[320]);
 	    if (inSpeech) {
                dataQueue.add(pad);
 		dataQueue.add(new SpeechEndSignal());
 		dataQueue.add(new SpeechStartSignal());
 	    } else {
 		dataQueue.add(new SpeechStartSignal());
                dataQueue.add(pad);
 		dataQueue.add(new SpeechEndSignal());
 	    }
 	    return getData();
 	}
 	data = getPredecessor().getData();
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
