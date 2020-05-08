 import javax.sound.sampled.DataLine;
 import javax.sound.sampled.SourceDataLine;
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioFormat;
 import javax.sound.sampled.AudioSystem;
 import java.io.FileInputStream;
 import java.io.File;
 import java.util.regex.*;
 
 public class Player{
 
 	protected InterfaceAudioFileDecoder audioInput;
 	protected boolean stop, pause;
 	protected String format;
 
 	public Player(File file)
 	{
 		// prend un fichier et test quel est son extention (mp3, ogg, wav, flac)
 		boolean b = Pattern.matches(".*\\.mp3$", file.getName());
 		InterfaceAudioFileDecoder result = null;
 		if(b)
 		{
 			result = new AbstractNonWaveReader(file);
 			format = "mp3";
 		}
 		b = Pattern.matches(".*\\.wav$", file.getName());
 		if(b)
 		{
 			result = new MusicWave(file);
 			format = "wav";
 		}
 		b = Pattern.matches(".*\\.flac$", file.getName());
 		if(b)
 		{
 			//result = new MusicFlac(file);
 			format = "flac";
 		}
 		b = Pattern.matches(".*\\.ogg$", file.getName());
 		if(b)
 		{
 			result = new AbstractNonWaveReader(file);
 			format = "ogg";
 		}
 		if ( result != null)
 		{
 			setStream(result);
 		}
		else {
			System.out.println("The file "+file.getName()+" have an unrecognized type");
		}
 	}
 
 	public void setStream(InterfaceAudioFileDecoder audioInput){
 		this.audioInput = audioInput;
 	}
 
 	public void play(){
 			audioInput.play();
 		
 	}
 	public void pause() {
 		this.pause = !pause;
 	}
 	public void stop() {
 		this.stop = true;
 	}
 	public static void main(String args[]){
 		Player play;
 		try {
 			play = new Player(new File("../Music/1-welcome.wav"));
 			play.play();
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 	}
 }
