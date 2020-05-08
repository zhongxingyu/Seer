 import java.awt.event.KeyEvent;
 import java.io.File;
 import java.io.IOException;
 
 import javax.sound.sampled.AudioFormat;
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.Clip;
 import javax.sound.sampled.DataLine;
 import javax.sound.sampled.LineUnavailableException;
 import javax.sound.sampled.UnsupportedAudioFileException;
 
 /**
  * Men
  * Hierber wird das Spiel gestartet. Es beginnt mit dem hier implementierten Men, welches den Spieler fragt, ob er das Spiel starten oder beenden mchte
  *
  */
 public class Menue {
 	
 
 	public static final Clip clip = null;
 
 	/**
 	 * Startet das Programm
 	 * @param args
 	 * @throws IOException 
 	 * @throws InterruptedException 
 	 * @throws UnsupportedAudioFileException 
 	 * @throws LineUnavailableException 
 	 */
 	
 
 	public static void execute() throws IOException, InterruptedException, UnsupportedAudioFileException, LineUnavailableException {
 		// TODO Auto-generated method stub
 		
 		StdDraw.clear();
 		StdDraw.setPenRadius(.01);
 		StdDraw.setPenColor();
 	
 
 
 	/*	File file = new File("src/Menusound.wav");
 		AudioInputStream stream = AudioSystem.getAudioInputStream(file);
 		AudioFormat format = stream.getFormat();
 		
 		// specify what kind of line we want to create
 		DataLine.Info info = new DataLine.Info(Clip.class, format);
 		// create the line
 		Clip clip = (Clip)AudioSystem.getLine(info);
 		// load the samples from the stream
 		clip.open(stream);
 		// begin playback of the sound clip
 		clip.start(); */
 		
 		double x=0.75;
 		double y=0.75;
 		while (true)
 		{
 			StdDraw.show(0);
 			StdDraw.clear();
 			
 			StdDraw.square(.5, .5, .5);
 			StdDraw.line(0, .5, 1, .5);
 			StdDraw.text(0.5, 0.75, "Start");
 			StdDraw.text(0.5, 0.25, "Beenden");
 			StdDraw.point(x, y);
 			if (StdDraw.isKeyPressed(KeyEvent.VK_ENTER))
 				if (y>0.5) //Start gedrckt
				{
 					//Gehe ins Untermen (Wahl ob Single oder Multiplayer)
 					SpielMenue.execute();
 					//clip.stop();
 					break;
 				}
 				else System.exit(0);
 			if (StdDraw.isKeyPressed(KeyEvent.VK_UP))
 			{
 				y=.75;
 			}
 			else if (StdDraw.isKeyPressed(KeyEvent.VK_DOWN))
 			{
 				y=.25;
 			}
 			
 			
 		}
 
 	}
 
 }
