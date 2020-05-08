 package pl.shockah.easyslick.tests;
 
 import org.newdawn.slick.Input;
 import pl.shockah.audio.Audio;
 import pl.shockah.audio.AudioWave;
 import pl.shockah.audio.MIDI;
 import pl.shockah.audio.Sample;
 import pl.shockah.audio.Stream;
 import pl.shockah.easyslick.App;
 import pl.shockah.easyslick.GraphicsHelper;
 import pl.shockah.easyslick.Key;
 import pl.shockah.easyslick.Room;
 
 public class TestAudio {
 	public static void main(String[] args) {
 		App.start(new RoomTest(),"Test - Audio");
 	}
 	
 	public static class RoomTest extends Room {
 		protected static MIDI midi;
 		protected static Sample sample;
 		protected static Stream stream;
 		protected static Audio audio;
 		
 		static {
 			try {
				midi = new MIDI(TestAudio.class.getClassLoader().getResource("midiFile.mid"));
				sample = new Sample(TestAudio.class.getClassLoader().getResource("wavFile.wav"));
				stream = new Stream(TestAudio.class.getClassLoader().getResource("aiffFile.aiff"));
 				audio = midi;
 			} catch (Exception e) {App.getApp().handle(e);}
 		}
 		
 		protected void setupRoom() {
 			size.x = viewSize.x = 160;
 			size.y = viewSize.y = 120;
 			maxFPS = 60;
 		}
 		
 		protected void onTick() {
 			if (Key.pressed(Input.KEY_Z) || Key.pressed(Input.KEY_X) || Key.pressed(Input.KEY_C)) audio.stop();
 			if (Key.pressed(Input.KEY_Z)) audio = midi;
 			if (Key.pressed(Input.KEY_X)) audio = sample;
 			if (Key.pressed(Input.KEY_C)) audio = stream;
 			
 			float p = 0;
 			if (Key.pressed(Input.KEY_P)) p += .025f;
 			if (Key.pressed(Input.KEY_L)) p -= .025f;
 			if (p != 0 && audio instanceof AudioWave) ((AudioWave)audio).setPitch(((AudioWave)audio).getPitch()+p);
 			
 			try {
 				if (Key.pressed(Input.KEY_1)) audio.play();
 				if (Key.pressed(Input.KEY_2)) audio.loop();
 				if (Key.pressed(Input.KEY_3)) audio.pause();
 				if (Key.pressed(Input.KEY_4)) audio.stop();
 			} catch (Exception e) {App.getApp().handle(e);}
 		}
 		protected void onRender(GraphicsHelper gh) {
 			String s = "";
 			if (audio instanceof AudioWave) s = "\nPitch: "+((AudioWave)audio).getPitch();
 			gh.g().drawString("isPlaying: "+audio.isPlaying()+"\nisPaused: "+audio.isPaused()+"\nlooping: "+audio.isLooping()+s,2,2);
 		}
 	}
 }
