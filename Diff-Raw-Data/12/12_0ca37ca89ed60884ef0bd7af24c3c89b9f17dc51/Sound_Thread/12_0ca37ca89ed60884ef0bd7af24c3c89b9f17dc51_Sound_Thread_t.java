 import java.io.File;
 import java.io.IOException;
import java.util.ArrayList;
 import java.util.HashMap;
 
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.Clip;
 import javax.sound.sampled.UnsupportedAudioFileException;
 
 public class Sound_Thread extends Thread{
 	private Clip clip;
 	private String SongPath;
 	private boolean repeat;
 	private HashMap<AudioInputStream, Boolean>  Sounds ;
 
 	public Sound_Thread(String sp, boolean rp){
 		SongPath = sp;
 		repeat = rp;
 		music(SongPath, repeat);
 
 	}
 	
 	public void StopMusic(){
 		clip.stop();
 	}
 
 	public void music(String SongPath, boolean repeat){
 		try{
 			File f = new File(SongPath);
 			AudioInputStream ais = AudioSystem.getAudioInputStream(f.getAbsoluteFile());
 			clip = AudioSystem.getClip();
 			clip.open(ais);
 			clip.start();
 			if(repeat)
 				clip.loop(Clip.LOOP_CONTINUOUSLY);
 
 		}
 
 		catch(Exception e){
 			System.out.println("Audio file Not found!");
 		}
 	}
	/**   //Sound
	   ArrayList<AudiosPair> list = new ArrayList<AudiosPair>(new Audios().getAudios());	
	   Sound_Thread st1 = new Sound_Thread(list.get(0).getSongName(), list.get(0).getRepeat());    **/
 }
