 package Application;
 
 import com.badlogic.gdx.backends.jogl.JoglApplication;
 import com.badlogic.gdx.tools.imagepacker.TexturePacker;
 import com.badlogic.gdx.tools.imagepacker.TexturePacker.Settings;
 
 public class Desktop {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 
 		//Empaquetar imagenes
 		Settings settings = new Settings();
         settings.padding=2;
         settings.maxHeight=2048;
         settings.maxWidth=2048;
         settings.incremental=true;
         TexturePacker.process(settings, "image", "data");    
 		
 		/*
 		 * Para poder lanzar el juego tenemos que hacer
 		- new Demo1Game(). Instancia de la clase que implementa Game
 		- "Demo1". Que es el ttulo de la aplicacin.
 		- 320. Ancho en pxeles.
 		- 480. Alto en pxeles.
 		- false. Para indicar que no queremos utilizar OpenGL S 2.0 en este caso. Por lo que se utilizar el 1.1
 		*/
        new JoglApplication(new LaunchFrozenWars(), "FrozenWars", 1024, 639, false);
 	}
 
 }
