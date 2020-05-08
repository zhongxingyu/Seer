 package game;
 
import java.io.File;

 import javax.sound.sampled.AudioFormat;
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.Clip;
 import javax.sound.sampled.DataLine;
 
 public class Sounds {
 
 	public void playSound(String filename) {
 
 		try {
 			AudioInputStream audioInputStream = AudioSystem
					.getAudioInputStream(new File("/ressources/" + filename));
 			AudioFormat af = audioInputStream.getFormat();
 			int size = (int) (af.getFrameSize() * audioInputStream
 					.getFrameLength());
 			byte[] audio = new byte[size];
 			DataLine.Info info = new DataLine.Info(Clip.class, af, size);
 			audioInputStream.read(audio, 0, size);
 
			// for(int i=0; i < 32; i++) {
 			Clip clip = (Clip) AudioSystem.getLine(info);
 			clip.open(af, audio, 0, size);
 			clip.start();
			// }
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 
 }
