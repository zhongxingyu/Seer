 
 import javax.sound.sampled.*;
import java.lang.Math;
 
 public class LoopbackTest{
 	
 	public static void main(String[] args) throws Exception{
 
 		// Set the format for the speaker and microphone
 		AudioFormat format = new AudioFormat(8000.0f, 16, 1, true, true);
 
 		// Get the system's default microphone
 		TargetDataLine microphone = AudioSystem.getTargetDataLine(format);
 
 		// Get the system's default speaker
 		SourceDataLine speaker = AudioSystem.getSourceDataLine(format);
 
 		// Open streams to both mic and speaker
 		microphone.open(format);
 		speaker.open(format);
 
 		int numBytesRead;

		// Read in the entire buffer to ensure we don't build up 
		// data in the TargetDataLine buffer, causing the mic to make
		// clicking noises.
		byte[] data = new byte[microphone.getBufferSize()];
 
 		// Start sending/receiving data
 		microphone.start();
 		speaker.start();
 
 		while (true){
 			numBytesRead = microphone.read(data, 0, data.length);

			// we take the min here to ensure that we don't write more to the speaker
			// than the speaker can output per iteration.
			speaker.write(data, 0, Math.min( numBytesRead, speaker.getBufferSize()) );
 		}
 
 		// Clean up
 //		microphone.stop();
 //		speaker.stop();
 
 	}
 }
