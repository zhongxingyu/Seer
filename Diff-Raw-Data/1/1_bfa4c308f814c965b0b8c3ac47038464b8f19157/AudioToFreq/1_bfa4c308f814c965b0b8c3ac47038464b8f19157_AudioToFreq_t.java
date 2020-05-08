 
 
 import java.io.*;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.util.Arrays;
 import javax.sound.sampled.*;
 import org.apache.commons.math3.complex.Complex;
 import org.apache.commons.math3.transform.*;
 
 /**
  *
  * @author Kevin
  */
 public class AudioToFreq {
     final static double INITIAL_THRESHHOLD = 450000.0;
     final static int MIN_BIN = 50;
     final static int MAX_BIN = 70;
     public static boolean running = false;
     private static boolean hasFired = false;
     /**
      * @param args the command line arguments
      */
     public static void PlaySongAndTransform(String filename) {
         // TODO code application logic here
         running = true;
         try {
             File file = new File(filename);
             try (AudioInputStream in = AudioSystem.getAudioInputStream(file)) {
                 AudioInputStream din;
                 AudioFormat baseFormat = in.getFormat();
                 AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                                                             baseFormat.getSampleRate(),
                                                             16,
                                                             baseFormat.getChannels(),
                                                             baseFormat.getChannels()*2,
                                                             baseFormat.getSampleRate(),
                                                             false);
                 din = AudioSystem.getAudioInputStream(decodedFormat, in);
                 
                 rawplay(decodedFormat, din);
             }
         } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
             System.err.println("Something happened here.");
         }
     }
     
     /**Provides a method to detect a beat given bin limits and a fourier 
     *  transformed waveform
     * @param minBin the minimum bin from which to detect a beat
     * @param maxBin same, but maximum
     * @param fourierTransformed a fourier transformed waveform
     * @param difficulty A scale of difficulty. 0.5 being the hardest, 1 being the easiest
     * @param spawnIndex Which speaker to send the messages to
     */
     private static void minMaxBuffer(int minBin, int maxBin, 
                                      double initialThreshhold, 
                                      Complex[] fourierTransformed, 
                                      double difficulty,
                                      int spawnIndex) {
         double[] minmaxBuffer = new double[15];
         double min;
         double max;
         boolean useRealValues = false;
         double testMetric;
         int nextWriteOver = 0;
         boolean hasFired = false;
         
         Arrays.fill(minmaxBuffer, 0.0);
         double sum = 0;
         for (int i = minBin; i < maxBin; ++i) {
             sum += Math.sqrt(fourierTransformed[i].getReal()*
                     fourierTransformed[i].getReal() + 
                     (fourierTransformed[i].getImaginary()*
                     fourierTransformed[i].getImaginary()));
         } // This loop is for getting the average strength of a frequency range over the frame.
         sum /= (maxBin - minBin);
         
         minmaxBuffer[nextWriteOver] = sum;  //This next block of code is for getting the min/max of the last 15 frames
         ++nextWriteOver;
         if (nextWriteOver == minmaxBuffer.length) {nextWriteOver = 0; useRealValues = true;}
         max = 0; min = 800000;
         for (double i : minmaxBuffer) {
              if (i > max) {max = i;}
              if (i < min) {min = i;}
         }  
         testMetric = (max - min)*.25 + min; // This is the number to test against to see if we are in a beat
         if (useRealValues == true) {
             if (sum > testMetric && hasFired == false) {
                DrawPanel.spawn[spawnIndex] = true;
                hasFired = true;
             } else if (sum <= testMetric) {
                DrawPanel.spawn[spawnIndex] = false;
                hasFired = false;
             }
         }else {
             if (sum > INITIAL_THRESHHOLD && hasFired == false) {
                 DrawPanel.spawn[spawnIndex] = true;
             } else if (sum <= INITIAL_THRESHHOLD) {
                 DrawPanel.spawn[spawnIndex] = false;
                 hasFired = false;
             }
         }
     }
     
     //Given an audio format and an audio stream, plays audio and calculates fourier transforms
     private static void rawplay(AudioFormat targetFormat, AudioInputStream din) throws IOException, LineUnavailableException
     {
         byte[] data = new byte[4096];           //Holds the current frame's data. 
         SourceDataLine line = getLine(targetFormat);
         FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD); //The class that performs fourier transforms
         int[] volBuffer = new int[30];  //A buffer to hold the volume of the last 30 frames
         int nextVol = 0;                //To keep the position you are in. in the volume buffer
        int curVolume;
         
         Arrays.fill(volBuffer, 0);
         
         if (line != null) {
             line.start();
             int nBytesRead = 0, nBytesWritten = 0;
             while (nBytesRead != -1 && running) {
                 nBytesRead = din.read(data, 0, data.length);        //Reads in 4096 bytes of data or 2048 samples
                 curVolume = 0;
                 ByteBuffer bb = ByteBuffer.allocate(2);             //The data is actually 16 bits, so I need to transform it from 8
                 bb.order(ByteOrder.LITTLE_ENDIAN);
                 short[] shortVals = new short[2048];
                 for (int i = 0; i < 4096; i+=2) {
                     bb.put(data[i]);
                     bb.put(data[i+1]);
                     shortVals[i/2] = bb.getShort(0);
                     bb.clear();
                     curVolume += shortVals[i/2];//Add up all of the amplitudes
                 }
                 curVolume /= 2048;              //The average volume of every sample in the frame
                 volBuffer[nextVol] = curVolume;//put it in the volume buffer
                 ++nextVol; if (nextVol == volBuffer.length) { nextVol = 0; }    //Fill the volBuffer
                 int volSum = 0;
                 for (int i : volBuffer) {
                     volSum+= Math.abs(i);
                 }
                 volSum/=50;     //The average volume of the last 30 frames
 
                 double[] doubleData = new double[2048];         //The fourier transform requires a double array
                 for (int i = 0; i < shortVals.length; ++i) {
                     doubleData[i] = (double) shortVals[i];
                 }
                 
                 Complex[] result = fft.transform(doubleData, TransformType.FORWARD); //Here's the star of the show
                 
                 minMaxBuffer(MIN_BIN,MAX_BIN, INITIAL_THRESHHOLD, result, 0.85, 0);
                 minMaxBuffer(1200,1220, 400000, result, 0.5, 1);
                 minMaxBuffer(2000,2020, INITIAL_THRESHHOLD, result, 0.5, 2);
 
                 if (nBytesRead != -1) {
                     nBytesWritten = line.write(data, 0, nBytesRead);
                 }
             }
         line.drain(); //close the audio stream. Song's over
         line.stop();
         line.close();
         din.close();
         }
     }
     private static SourceDataLine getLine(AudioFormat audioFormat) throws LineUnavailableException
     {
         SourceDataLine res;
         DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
         res = (SourceDataLine) AudioSystem.getLine(info);
         res.open(audioFormat);
         return res;
     }
 }
 
