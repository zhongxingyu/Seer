 package com.sonrlabs.test.sonr.signal;
 
 import java.util.List;
 
 import org.acra.ErrorReporter;
 
 import android.media.AudioFormat;
 import android.media.AudioRecord;
 import android.media.MediaRecorder.AudioSource;
 import android.util.Log;
 
 import com.sonrlabs.test.sonr.ISampleBuffer;
 import com.sonrlabs.test.sonr.SONR;
 
 /**
 * Process an ordered collecton of reusable sample buffers.
  * <p>
  * This is effectively a singleton as it's only instantiated once by the
  * {@link com.sonrlabs.test.sonr.AudioProcessorQueue} singleton.
  * <p>
  * This class is only public to provide access to the static utility method
  * {@link #findAudioRecord()}.
  */
 public final class AudioProcessor
       implements AudioSupportConstants, IAudioProcessor {
    
    private static final String TAG = "AudioProcessor";
    
    private final TransmissionPreprocessor preprocessor = new TransmissionPreprocessor();
    
    AudioProcessor() {
    }
    
    @Override
    public void nextSamples(List<ISampleBuffer> buffers) {
       for (ISampleBuffer buffer : buffers) {
          try {
             preprocessor.nextSample(buffer);
          } catch (RuntimeException e) {
             e.printStackTrace();
             ErrorReporter.getInstance().handleException(e);
          } finally {
             buffer.release();
          }
       }
    }
    
    private static final short[] FORMATS = {
       AudioFormat.ENCODING_PCM_16BIT
       // AudioRecord.java:467 PCM_8BIT not supported at the moment
    };
 
    private static final short[] CHANNEL_CONFIGS = {
       AudioFormat.CHANNEL_CONFIGURATION_MONO,
       AudioFormat.CHANNEL_IN_MONO,
       AudioFormat.CHANNEL_CONFIGURATION_DEFAULT,
       AudioFormat.CHANNEL_IN_DEFAULT
    };
 
    /**
     * Utility method to find the right audio format for a given phone.
     * @return a suitable record, or null if none found.
     */
    public static AudioRecord findAudioRecord() {
       for (short audioFormat : FORMATS) {
          for (short channelConfig : CHANNEL_CONFIGS) {
             Log.d(TAG, "Attempting rate " + SAMPLE_RATE + "Hz, bits: " + audioFormat + ", channel: " + channelConfig);
             try {
                int bufferSize = SAMPLES_PER_BUFFER * AudioRecord.getMinBufferSize(SAMPLE_RATE, channelConfig, audioFormat);
                if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
                   // check if we can instantiate and have a success
                   AudioRecord recorder = new AudioRecord(AudioSource.DEFAULT, SAMPLE_RATE, channelConfig, audioFormat, bufferSize);
 
                   if (recorder.getState() == AudioRecord.STATE_INITIALIZED) {
                      /*
                       * Stash the value in public static field of SONR, since
                       * existing code expects to find it there. Ugh.
                       */
                      SONR.bufferSize = bufferSize;
                      return recorder;
                   }
                }
             } catch (RuntimeException e) {
                Log.v(TAG, "Unable to allocate for: " + SAMPLE_RATE + "Hz, bits: " + audioFormat + ", channel: " + channelConfig);
                Log.e(TAG, "Exception " + e);
             }
          }
       }
       return null;
    }
 
 }
 
 
