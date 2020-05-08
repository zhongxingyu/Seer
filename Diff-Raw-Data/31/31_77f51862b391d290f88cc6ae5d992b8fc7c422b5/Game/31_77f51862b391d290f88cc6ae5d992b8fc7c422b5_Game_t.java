 package tsa2035.game.content.core;
 
 import java.io.IOException;
 
 import org.lwjgl.LWJGLException;
 import org.newdawn.slick.openal.Audio;
 import org.newdawn.slick.openal.AudioLoader;
 
 import tsa2035.game.engine.audio.AudioManager;
 import tsa2035.game.engine.core.Menu;
 import tsa2035.game.engine.core.Renderer;
 
 public class Game {
 
 	/**
 	 * @param args
 	 * @throws LWJGLException 
 	 * @throws IOException 
 	 */
 	
 	public static Audio soundtrack = null;
 	
 	public static Audio getSoundtrack()
 	{
 		return soundtrack;
 	}
 	
 	public static void main(String[] args) throws LWJGLException, IOException {
		soundtrack = AudioLoader.getAudio("WAV", AudioManager.class.getResourceAsStream("/tsa2035/game/content/audio/game.WAV"));
		soundtrack.playAsMusic(1.0f, 0.5f, true);
 		Renderer.init(800, 600);
 		Renderer.renderLoop(new Menu());
 	}
 
 }
