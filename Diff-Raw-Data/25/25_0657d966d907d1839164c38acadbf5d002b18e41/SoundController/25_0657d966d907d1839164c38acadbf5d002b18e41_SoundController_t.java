 package soundfriend;
 
 import java.io.File;
 import java.io.IOException;
 
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.Clip;
 import javax.sound.sampled.FloatControl;
 import javax.sound.sampled.LineUnavailableException;
 import javax.sound.sampled.UnsupportedAudioFileException;
 
 public class SoundController {
 	
 	public static enum Sounds {
 		SAD("sad.wav"),
 		MUNCH("munch.wav"),
		MEOW("meow.wav"),
 		HUNGRY("hungry.wav");
 		
 		private String filename;
 		
 		Sounds(final String filename) {
 			this.filename = filename;
 		}
 		
 		public String filepath() {
 			return "tamodatchi" + File.separator + "sounds" + File.separator + filename;
 		}
 	}
 
	private static Clip clip;
 
     // play the MP3 file to the sound card
    public static void play(Sounds sound) {
     	String filepath = sound.filepath();
    	if (clip != null && clip.isActive()) {
    		return;
    	}
 		try {
 			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(
 					new File(filepath));
			clip = AudioSystem.getClip();
 			clip.open(audioInputStream);
 			FloatControl gainControl = 
 					(FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
 			clip.start();
 //			while (clip.isActive()) {
 //				gainControl.setValue(gainControl.getValue()); // Reduce volume by 10 decibels.
 //				System.out.println("lowering volume");
 //				try {
 //					Thread.sleep(100);
 //				} catch (InterruptedException e) {
 //					// TODO Auto-generated catch block
 //					e.printStackTrace();
 //				}
 //			}
 			
 		} catch (LineUnavailableException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (UnsupportedAudioFileException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 
     }
     
 }
