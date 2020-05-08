 package jixel.gui;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import javax.sound.sampled.AudioFormat;
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.BooleanControl;
 import javax.sound.sampled.Clip;
 import javax.sound.sampled.DataLine.Info;
 import javax.sound.sampled.FloatControl;
 import javax.sound.sampled.LineEvent;
 import javax.sound.sampled.LineListener;
 import javax.sound.sampled.LineUnavailableException;
 import javax.sound.sampled.SourceDataLine;
 import javax.sound.sampled.UnsupportedAudioFileException;
 
 import jixel.stage.JixelGame;
 
 public class JixelPlayer {
 
 	private ExecutorService threadPool = Executors.newFixedThreadPool(8);
 	private List<Clip> soundClipList = new ArrayList<Clip>();
 	private SourceDataLine dataLine;
 	private volatile boolean stopped = false;
 	private float sounddB;
 	private boolean soundMute;
 	private float musicdB;
 	private boolean musicMute;
 
 	private final int BUFFER_SIZE = 524288; // 128Kb
 
 	public void blockAndPlay(String filepath, int times) {
 		playFile(filepath, --times, false, true);
 		try {
 			synchronized (this) {
 				this.wait();
 			}
 		} catch (InterruptedException e) {
 			JixelGame.getConsole().printErr(e);
 		}
 	}
 
 	public void setSoundMute(boolean state) {
 		this.soundMute = state;
 		for (Clip c : soundClipList) {
 			BooleanControl bc = (BooleanControl) c.getControl(BooleanControl.Type.MUTE);
 			if (bc != null) {
 				bc.setValue(state);
 			}
 		}
 	}
 
 	public void setMusicMute(boolean state) {
 		this.musicMute = state;
 		if (dataLine == null) {
 			return;
 		}
 		if (dataLine.isControlSupported(BooleanControl.Type.MUTE)) {
 			BooleanControl bc = (BooleanControl) dataLine.getControl(BooleanControl.Type.MUTE);
 			if (bc != null) {
 				bc.setValue(state);
 			}
 		}
 	}
 
 	public boolean getSoundMute() {
 		return soundMute;
 	}
 
 	public boolean getMusicMute() {
 		return musicMute;
 	}
 
 	public void setSoundVolume(float dB) {
 		this.sounddB = dB;
 		for (Clip c : soundClipList) {
 			FloatControl volumeControl = (FloatControl) c.getControl(FloatControl.Type.MASTER_GAIN);
 			adjustVolume(dB, volumeControl);
 		}
 	}
 
 	public void setMusicVolume(float dB) {
 		this.musicdB = dB;
 		if (dataLine == null) {
 			return;
 		}
 		if (dataLine.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
 			FloatControl volumeControl = (FloatControl) dataLine.getControl(FloatControl.Type.MASTER_GAIN);
 			adjustVolume(dB, volumeControl);
 		}
 	}
 
 	private void adjustVolume(float dB, FloatControl volumeControl) {
 		if (volumeControl != null) {
 			if (dB > volumeControl.getMaximum()) {
 				volumeControl.setValue(volumeControl.getMaximum());
 			} else if (dB < volumeControl.getMinimum()) {
 				volumeControl.setValue(volumeControl.getMinimum());
 			} else {
 				volumeControl.setValue(dB);
 			}
 		}
 
 	}
 
 	public float getSoundVolume() {
 		return sounddB;
 	}
 
 	public float getMusicVolume() {
 		return musicdB;
 	}
 
 	public void playMusic(String filepath) {
 		playFile(filepath, 1, true, false);
 	}
 
 	public void stopMusic() {
 		stopped = true;
 	}
 
 	public void stopSound() {
 		for (Clip c : soundClipList) {
 			c.stop();
 		}
 	}
 
 	public boolean soundPlaying() {
 		return soundClipList.isEmpty();
 	}
 
 	public boolean musicPlaying() {
		if(dataLine == null){
			return false;
		}
 		return dataLine.isOpen();
 	}
 
 	public void playSound(String filepath, int times) {
 		playFile(filepath, --times, false, false);
 	}
 
	private void playFile(final String filepath, final int times, final boolean music, final boolean blocking) {
 		if (times < 0) {
 			JixelGame.getConsole().printErr(new IllegalArgumentException("Can not play a sound less than 1 time"));
 			return;
 		}
 		final File f = new File(filepath);
 		threadPool.execute(new Runnable() {
 			@Override
 			public void run() {
 				try (InputStream in = new FileInputStream(f);
 						InputStream wrappedStream = new BufferedInputStream(in);
 						AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(wrappedStream);) {
 					if (!music) {
 						final Clip audioClip = AudioSystem.getClip();
 						soundClipList.add(audioClip);
 						audioClip.open(audioInputStream);
 						FloatControl volumeControl = (FloatControl) audioClip.getControl(FloatControl.Type.MASTER_GAIN);
 						adjustVolume(sounddB, volumeControl);
 						if (soundMute) {
 							BooleanControl bc = (BooleanControl) audioClip.getControl(BooleanControl.Type.MUTE);
 							if (bc != null) {
 								bc.setValue(true);
 							}
 						}
 						audioClip.loop(times);
 						audioClip.start();
 						audioClip.addLineListener(new LineListener() {
 							@Override
 							public void update(LineEvent e) {
 								if (e.getType().equals(LineEvent.Type.STOP)) {
 									soundClipList.remove(audioClip);
 									if (blocking) {
 										synchronized (JixelGame.getPlayer()) {
 											JixelGame.getPlayer().notify();
 										}
 									}
 								}
 							}
 						});
 					} else {
 						if (dataLine != null && dataLine.isOpen()) {
 							stopped = true;
 							while (stopped)
 								;
 						}
 						AudioFormat format = audioInputStream.getFormat();
 						Info info = new Info(SourceDataLine.class, format);
 						dataLine = (SourceDataLine) AudioSystem.getLine(info);
 						dataLine.open(format, JixelGame.getPlayer().BUFFER_SIZE);
 
 						FloatControl volumeControl = (FloatControl) dataLine.getControl(FloatControl.Type.MASTER_GAIN);
 						adjustVolume(musicdB, volumeControl);
 						if (musicMute) {
 							BooleanControl bc = (BooleanControl) dataLine.getControl(BooleanControl.Type.MUTE);
 							if (bc != null) {
 								bc.setValue(true);
 							}
 						}
 
 						dataLine.start();
 
 						byte[] musicBuffer = new byte[JixelGame.getPlayer().BUFFER_SIZE];
 						int reader = 0;
 
 						try {
 							while (!stopped && (reader = audioInputStream.read(musicBuffer, 0, JixelGame.getPlayer().BUFFER_SIZE)) != -1) {
 								dataLine.write(musicBuffer, 0, reader);
 							}
 						} finally {
 							dataLine.stop();
 							dataLine.drain();
 							dataLine.close();
 						}
 					}
 				} catch (FileNotFoundException e) {
 					JixelGame.getConsole().printErr("File not found at " + f.getPath(), e);
 				} catch (IOException e) {
 					JixelGame.getConsole().printErr("IO error at " + f.getPath(), e);
 				} catch (UnsupportedAudioFileException e) {
 					JixelGame.getConsole().printErr("Unsupported file type at " + f.getPath(), e);
 				} catch (LineUnavailableException e) {
 					JixelGame.getConsole().printErr("Error playing file at " + f.getPath(), e);
 				} finally {
 					if (music) {
 						if (stopped) {
 							stopped = false;
 						} else {
 							playFile(filepath, 1, true, false);
 						}
 					}
 				}
 			}
 		});
 	}
 }
