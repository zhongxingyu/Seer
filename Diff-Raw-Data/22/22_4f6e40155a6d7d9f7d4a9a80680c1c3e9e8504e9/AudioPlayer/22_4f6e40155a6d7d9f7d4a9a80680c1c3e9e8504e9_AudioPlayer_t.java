 package sound;
 
 import java.io.File;
 import java.io.IOException;
 
 import javax.sound.sampled.AudioFormat;
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.DataLine;
 import javax.sound.sampled.LineUnavailableException;
 import javax.sound.sampled.SourceDataLine;
 import javax.sound.sampled.UnsupportedAudioFileException;
 import javax.swing.JFileChooser;
 
 public class AudioPlayer extends Thread {
 	private static final int EXTERNAL_BUFFER_SIZE = 1024;
 	File soundFile;
 	AudioInputStream audioInputStream;
 	private volatile boolean finished;
 	private boolean runOnce;
 
 
 	/**
 	 * creates a new Audio Player Thread with specified soundFileName. If runonce flag is false it will continue
 	 *  looping forever until you call stopPlayer() method on audioplayer
 	 *
 	 *
 	 * @param soundFileName
 	 * @param runOnce
 	 */
 	public AudioPlayer(String soundFileName, boolean runOnce) {
 		this.runOnce = runOnce;
 
 
 		// TODO probably need to deleted these lines later
 		//	System.out.println("New Audio Player");
 
 		// sound we are playing
 		soundFile = new File("Assets/sounds/" + soundFileName);
 		audioInputStream = null;
 
 	}
 
 	public void run() {
 		finished = false;
 
 		try {
 			audioInputStream = AudioSystem.getAudioInputStream(soundFile);
 		} catch (Exception e) {
 
 			e.printStackTrace();
 			System.exit(1);
 		}
 
 		AudioFormat audioFormat = audioInputStream.getFormat();
 
 		SourceDataLine line = null;
 		DataLine.Info info = new DataLine.Info(SourceDataLine.class,
 				audioFormat);
 
 		try {
 			line = (SourceDataLine) AudioSystem.getLine(info);
 			line.open(audioFormat);
 		} catch (LineUnavailableException e) {
 			e.printStackTrace();
			//System.exit(1);
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.exit(1);
 		}
 
 		line.start();
 		while (!finished) {
 
 			int nBytesRead = 0;
 			byte[] abData = new byte[EXTERNAL_BUFFER_SIZE];
 			while (nBytesRead != -1 && !finished) {
 				try {
 					nBytesRead = audioInputStream
 							.read(abData, 0, abData.length);
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 				if (nBytesRead >= 0) {
 					int nBytesWritten = line.write(abData, 0, nBytesRead);
 				}
 			}
 			line.drain();
 			try {
 				audioInputStream.close();
 				audioInputStream = AudioSystem.getAudioInputStream(soundFile);
 			} catch (IOException e) {
 				e.printStackTrace();
 			} catch (UnsupportedAudioFileException e) {
 				e.printStackTrace();
 			}
 			if(runOnce){
 				break;
 			}
 		}
 		line.close();
 		finished = false;
 	}
 
 	/**
 	 * Terminates audio playing
 	 */
 	public void stopPlayer() {
 		finished = true;
 		// stop changes variable in player class that causes the whole Audio
 		// PLayer to terminate.
 	}
 
 	/**
 	 * for testing
 	 *
 	 * @throws InterruptedException
 	 */
 	public static void main(String args[]) throws InterruptedException {
 		AudioPlayer a = new AudioPlayer("timer1.wav", false);
 		a.start();
 		Thread.sleep(1000);
 		a.stopPlayer();
 		a.join();
 		System.out.println("stopped");
 
 		a = new AudioPlayer("laugh.wav", true);
 		a.start();
 
 	}
 
 }
