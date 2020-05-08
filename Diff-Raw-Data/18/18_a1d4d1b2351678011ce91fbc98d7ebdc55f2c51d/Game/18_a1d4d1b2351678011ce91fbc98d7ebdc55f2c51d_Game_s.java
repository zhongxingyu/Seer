 package tsa2035.game.content.core;
 
 import java.io.IOException;
 
 import org.lwjgl.LWJGLException;
 import org.newdawn.slick.openal.Audio;
 import org.newdawn.slick.openal.AudioLoader;
 
 import tsa2035.game.engine.audio.AudioManager;
 import tsa2035.game.engine.core.Renderer;
 import tsa2035.game.engine.texture.AnimatedTexture;
 
 public class Game {
 
 	/**
 	 * @param args
 	 * @throws LWJGLException 
 	 * @throws IOException 
 	 */
 	
 	public static Audio soundtrack = null;
 	public static AirMeter airMeter = null;
 	
 	public static void stopSoundtrack()
 	{
 		if ( soundtrack == null )
 			return;
 		soundtrack.stop();
 		soundtrack = null;
 	}
 	
 	public static void startSoundtrack() throws IOException
 	{
		stopSoundtrack();
		soundtrack = AudioLoader.getAudio("WAV", AudioManager.class.getResourceAsStream("/tsa2035/game/content/audio/game.WAV"));
		soundtrack.playAsMusic(1.0f, 0.5f, true);
 	}
 	
 	public static Audio getSoundtrack()
 	{
 		return soundtrack;
 	}
 	
 	public static AirMeter getAirMeter()
 	{
 		return airMeter;
 	}
 	
 
 	
 	public static void main(String[] args) throws LWJGLException, IOException {
		
		startSoundtrack();
 		Renderer.init(800, 600);
 		airMeter = new AirMeter(0.85f, 0.83f, new AnimatedTexture("/tsa2035/game/content/images/airmeter", "airmeter", 54, 1), 120000);
 		airMeter.setScale(0.7f);
 		Renderer.renderLoop(new Menu());
 	}
 
 }
