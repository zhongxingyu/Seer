 package ro.btanase.chordlearning.services;
 import java.io.File;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import javax.sound.sampled.AudioFormat;
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.Clip;
 import javax.sound.sampled.LineEvent;
 import javax.sound.sampled.LineListener;
 import javax.sound.sampled.LineUnavailableException;
 import javax.sound.sampled.UnsupportedAudioFileException;
 
 import org.apache.commons.lang3.ArrayUtils;
 
 public class WaveSoundMixer {
   
   private static final AudioFormat FORMAT = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, (16 / 8) * 2, 44100, false);
 
   public static void main(String[] args) throws LineUnavailableException,
       IOException, UnsupportedAudioFileException {
 
     WaveSoundMixer wsm = new WaveSoundMixer();
     
     List<String> fileList = Arrays.asList("A.wav", "Asus2.wav", "Asus4.wav", "A.wav", "Am.wav", "Am.wav", "Asus4.wav", "A.wav", "A7.wav", "Asus4.wav", "Asus2.wav", "A.wav");    
     Collections.shuffle(fileList);
     byte[] hostBuffer = wsm.createAudioStreamFromFileList(fileList, 1000);
     
     final Clip clip = AudioSystem.getClip();
     clip.open(FORMAT, hostBuffer, 0, hostBuffer.length);
     clip.start();
 
     clip.addLineListener(new LineListener() {
       
       @Override
       public void update(LineEvent event) {
         if (event.getType().equals(LineEvent.Type.STOP)){
           clip.close();
         }
         
       }
     });
     
     System.in.read();
   }
   
 
   /**
    * Creates an audio stream with sounds in fileList mixed at specified interval
    * @param fileList - path to audio files (Wave 44100, PCM, signed, 16 bit, stereo)
    * @param interval
    * @return
    * @throws UnsupportedAudioFileException
    * @throws IOException
    */
   public byte[] createAudioStreamFromFileList(List<String> fileList, int interval) throws UnsupportedAudioFileException, IOException {
     
     // start with an empty stream to mix; size equal to first sound in list
     AudioInputStream firstSound = AudioSystem.getAudioInputStream(new File(fileList.get(0)));
     byte[] hostBuffer = new byte[(int) (firstSound.getFrameLength() * 4)];
     firstSound.close();
     
     for(String fileName : fileList){
       AudioInputStream streamToAdd = AudioSystem.getAudioInputStream(new File(fileName));
       byte[] bufferToAdd = new byte[(int) (streamToAdd.getFrameLength() * 4)];
       
       streamToAdd.read(bufferToAdd);
       
       
       mixStreams(bufferToAdd, hostBuffer);
       hostBuffer = addSilence(interval, hostBuffer);
       streamToAdd.close();
     }
     return hostBuffer;
   }
 
   public byte[] createAudioStreamFromBufferList(List<byte[]> bufferList, int interval) throws UnsupportedAudioFileException, IOException {
     
     // start with an empty stream to mix; size equal to first sound in list
     byte[] hostBuffer = new byte[bufferList.get(0).length];
     
    for (int i=0; i < bufferList.size(); i++){
      byte[] currentBuffer = bufferList.get(i);
       mixStreams(currentBuffer, hostBuffer);
      
      if (i != bufferList.size() -1){
        hostBuffer = addSilence(interval, hostBuffer);
      }
     }
    
     return hostBuffer;
   }
 
   
   /**
    * Returns a stream with xxx milesconds of silence at the beggining of this stream
    * @param delay
    * @param stream
    * @return
    */
   public byte[] addSilence(int delay, byte[] stream) {
     long startTime = System.currentTimeMillis();
     int tm = delay/10; //convert miliseconds to 10th of miliseconds
     // create x seconds of silence
     int silenceSize =  ((441 * 4) * tm); // 100 tm == 1 second; a little hack to avoid floating point calculations
 
     byte[] silenceArr = new byte[silenceSize];
     byte[] stream2WithSilence = ArrayUtils.addAll(silenceArr, stream);
 
     long endTime = System.currentTimeMillis();
     
     System.out.println("addSilence: " + (endTime - startTime));
     return stream2WithSilence;
   }
 
   /**
    * mixes the firstBuffer at the beginning of the secondBuffer
    * 
    * The secondBuffer will contain the modified audio
    * 
    * @param firstBuffer
    * @param secondBuffer
    * @throws IOException
    */
   public void mixStreams(byte[] firstBuffer, byte[] secondBuffer) throws IOException {
     long startTime = System.currentTimeMillis();
     
     ByteBuffer bb = ByteBuffer.allocate(2);
     bb.order(ByteOrder.LITTLE_ENDIAN);
     byte[] frame = new byte[4];
     
 
     for (int i = 0; i < firstBuffer.length; i+=4){
       frame[0] = firstBuffer[i];
       frame[1] = firstBuffer[i+1];
       frame[2] = firstBuffer[i+2];
       frame[3] = firstBuffer[i+3];
       
       // get samples from first wave
       // get left ch sample
       bb.clear();
       bb.put(frame[0]);
       bb.put(frame[1]);
       
       short leftChSample1 = bb.getShort(0);
       
       // get right ch sample
       bb.clear();
       bb.put(frame[2]);
       bb.put(frame[3]);
       
       short rightChSample1 = bb.getShort(0);
       
       // cancel if the first stream was longer than the second
       // and we reached the end
       if (i+3 >= secondBuffer.length){
         break;
       }
       
       // get samples from second wave
       // get left ch sample
       bb.clear();
       bb.put(secondBuffer[i]);
       bb.put(secondBuffer[i+1]);
       
       short leftChSample2 = bb.getShort(0);
       
       // get right ch sample
       bb.clear();
       bb.put(secondBuffer[i+2]);
       bb.put(secondBuffer[i+3]);
       
       short rightChSample2 = bb.getShort(0);
       
       // mix samples for both channels
       short leftChMixed = (short) (leftChSample1 + leftChSample2 - ((leftChSample1 * leftChSample2))/65536);
       short rightChMixed = (short) (rightChSample1 + rightChSample2 - ((rightChSample1 * rightChSample2))/65356);
       
       // build byte for leftCh
       bb.clear();
       bb.putShort(leftChMixed);
       
       secondBuffer[i] = bb.get(0);
       secondBuffer[i+1] = bb.get(1);
       
       // build byte for rightCh
       bb.clear();
       bb.putShort(rightChMixed);
       secondBuffer[i+2] = bb.get(0);
       secondBuffer[i+3] = bb.get(1);
     }
     
     long endTime = System.currentTimeMillis();
     
     System.out.println("mixStreams: " + (endTime - startTime));
   }
 }
