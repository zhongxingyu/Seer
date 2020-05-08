 package testpackage.interfaces;
 
 import javax.sound.sampled.*;
 
 import testpackage.shared.ship.SoundHandler.Sound;
 import testpackage.shared.ship.gui.TemplateImages;
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 
 
 public class SoundStreamPlayer { // http://www.javalobby.org/java/forums/t18465.html
 	private Object lock = new Object();
 
 	private volatile boolean paused = true;
 	private volatile boolean doRestart = false;
 
 	private float panValue;
 
 	private Sound sound;
 	private Thread pt;
 
 	public SoundStreamPlayer(Sound sound, float panValue) {
 		this.sound = sound;
 		this.panValue = panValue;
 		pt = new Thread() {
 			public void run() {
 				runLoop();
 			}
 		};
 		pt.start();
 	}
 
 	private void pause() {
 		//System.err.println("Pausing");
 		paused = true;
 	}
 
 	void play() {
 		//System.err.println("Playing");
 		synchronized(lock) {
 			paused = false;
 			lock.notifyAll();
 		}
 	}
 
 	void restart() {
 		//System.err.println("Restarting");
 		doRestart = true;
 		play();
 	}
 
 	private void runLoop() {
 		AudioInputStream din = null;
 		try {
			BufferedInputStream soundStream = new BufferedInputStream(GUISchiffe.class.getResourceAsStream(TemplateImages.getSoundPath(sound)));
			soundStream.mark(1);
			SourceDataLine line;
 
 			outer: for (;;) {
			soundStream.reset();
 			AudioInputStream in;
 			try {
 				 in = AudioSystem.getAudioInputStream(soundStream);
 			} catch (IOException ex) {
 				System.err.println(getClass().getName() + ": " + ex.getMessage());
 				return;
 			}
 			AudioFormat baseFormat = in.getFormat();
 			AudioFormat decodedFormat = new AudioFormat(
 					AudioFormat.Encoding.PCM_SIGNED,
 					baseFormat.getSampleRate(), 16, baseFormat.getChannels(),
 					baseFormat.getChannels() * 2, baseFormat.getSampleRate(),
 					false);
 			din = AudioSystem.getAudioInputStream(decodedFormat, in);
 
 			DataLine.Info info = new DataLine.Info(SourceDataLine.class, decodedFormat);
 			line = (SourceDataLine) AudioSystem.getLine(info);
 			if(line != null) {
 				line.open(decodedFormat);
 				
 				if (line.isControlSupported(FloatControl.Type.PAN)) {
 					FloatControl pan = (FloatControl) line.getControl(FloatControl.Type.PAN);
 					pan.setValue(panValue);
 				} else {
 					System.err.println(sound + ": Pan not supported");
 				}
 				
 				byte[] data = new byte[4096];
 				// Start
 				line.start();
 				
 				int nBytesRead;
 
 				synchronized (lock) {
 					while ((nBytesRead = din.read(data, 0, data.length)) != -1) {
 						while (paused) {
 							if(line.isRunning()) {
 								line.stop();
 							}
 							try {
 								lock.wait();
 							}
 							catch(InterruptedException e) {
 								System.err.println("SoundSteamPlayer thread interrupted");
 								break outer;
 							}
 						}
 						if (doRestart) { doRestart = false; continue outer; }
 	
 						if(!line.isRunning()) {
 							line.start();
 						}
 						line.write(data, 0, nBytesRead);
 					}
 				}
 
 			}
			//System.err.println("Done");
			pause();
			}//for(;;)
			// Stop
 			line.drain();
 			line.stop();
 			line.close();
 			din.close();
 			
 		}
 		catch(Exception e) {
 			throw new RuntimeException(e);
 		}
 		finally {
 			if(din != null) {
 				try { din.close(); } catch(IOException e) { }
 			}
 		}
 	}
 
 	public static void main( String[] args ) {
 		SoundStreamPlayer ssp = new SoundStreamPlayer(Sound.shipAllShotUp_mp3, 1);
 		ssp.play();
 		sleep(1000);
 		ssp.restart();
 		sleep(1000);
 		ssp.pause();
 		sleep(1000);
 		ssp.play();
 		sleep(10000);
 		ssp.restart();
 		sleep(10000);
 
 	}
 
 	private static void sleep(int count) {
 		try {
 			Thread.sleep(count);
 		} catch (Exception e) {}
 	}
 
 	public void kill() {
 		pt.interrupt();
 		play();
 	}
 
}
