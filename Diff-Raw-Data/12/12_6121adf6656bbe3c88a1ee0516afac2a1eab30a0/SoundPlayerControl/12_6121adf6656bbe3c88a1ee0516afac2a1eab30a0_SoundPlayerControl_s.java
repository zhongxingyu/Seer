 package fi.mikuz.boarder.util;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import java.util.ListIterator;
 
 import fi.mikuz.boarder.component.GlobalSettings;
 import fi.mikuz.boarder.component.SoundPlayer;
 import fi.mikuz.boarder.gui.SoundboardMenu;
 
 /**
  * 
  * @author Jan Mikael Lindlf
  */
 public class SoundPlayerControl {
 	
 	public static void playSound(boolean playSimultaneously, File soundPath, Float volumeLeft, Float volumeRight, 
 			Float boardVolume) {
 		
 		SoundPlayer soundPlayer = new SoundPlayer();
 		try {
 			soundPlayer.setDataSource(soundPath.getAbsolutePath());
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 		SoundboardMenu.mSoundPlayerList.add(soundPlayer);
 		
 		if (playSimultaneously == false) {
     		ListIterator<SoundPlayer> iterator = SoundboardMenu.mSoundPlayerList.listIterator();
 			while (iterator.hasNext()) {
 				SoundPlayer iteratedPlayer = iterator.next();
 				if (iteratedPlayer != soundPlayer) {
 					iterator.remove();
 					iteratedPlayer.release();
 				}
 			}
         }
 		
         try {
         	soundPlayer.prepare();
         	soundPlayer.setVolume(volumeLeft*boardVolume, volumeRight*boardVolume);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		soundPlayer.start();
 		
 		ListIterator<SoundPlayer> iterator = SoundboardMenu.mSoundPlayerList.listIterator();
         
 		while (iterator.hasNext()) {
 			SoundPlayer iteratedPlayer = iterator.next();
 			if (iteratedPlayer.isPlaying() == false) {
 				iterator.remove();
 				iteratedPlayer.release();
 	    	}
 		}
 		
 	}
 
 	public static void togglePlayPause() {
 		
 		final GlobalSettings globalSettings = SoundboardMenu.mGlobalSettings;
 		final List<SoundPlayer> soundPlayerList = SoundboardMenu.mSoundPlayerList;
     	boolean playing = false;
 		
 		for (SoundPlayer soundPlayer : soundPlayerList) {
 			if (soundPlayer.isPlaying()) {
 				playing = true;
 				break;
 	    	}
 		}
 		
 		if (playing == true && globalSettings.getFadeOutDuration() > 0) {
 			
 			Thread t = new Thread() {
 				public void run() {
 					float volumeMultiplier = 1;
 
 					while(volumeMultiplier > 0.1) {
 						long currentTime = System.currentTimeMillis();
 						volumeMultiplier -= 0.1;
 						
 						for (SoundPlayer soundPlayer : soundPlayerList) {
 							soundPlayer.setFadeVolume(soundPlayer.getLeftVolume()*volumeMultiplier, 
 									soundPlayer.getRightVolume()*volumeMultiplier);
 						}
 						
 						try {
 							long realSleepTime = globalSettings.getFadeOutDuration()/10 - 
 								(System.currentTimeMillis() - currentTime);
 							long sleepTime = realSleepTime > 0 ? realSleepTime : 0;
 							Thread.sleep(sleepTime);
 						} catch (InterruptedException e) {
 							e.printStackTrace();
 						}
 					}
 					
 					for (SoundPlayer soundPlayer : soundPlayerList) {
 						soundPlayer.pause();
 					}
 				}
 			};
 			t.start();
 		} else if (playing == true) {
 			for (SoundPlayer soundPlayer : soundPlayerList) {
 				if (soundPlayer.isPlaying()) {
 					soundPlayer.pause();
 		    	}
 			}
 		} else if (playing == false && globalSettings.getFadeInDuration() > 0) {
 			Thread t = new Thread() {
 				public void run() {
 					float volumeMultiplier = (float) 0.1;
 					
 					for (SoundPlayer soundPlayer : soundPlayerList) {
 						soundPlayer.setFadeVolume(soundPlayer.getLeftVolume()*volumeMultiplier, 
 								soundPlayer.getRightVolume()*volumeMultiplier);
 						soundPlayer.start();
 					}
 					
 					long currentTime = System.currentTimeMillis();
 					
 					while(volumeMultiplier < 1) {
 						try {
 							long realSleepTime = globalSettings.getFadeInDuration()/10 - 
 								(System.currentTimeMillis() - currentTime);
 							long sleepTime = realSleepTime > 0 ? realSleepTime : 0;
 							Thread.sleep(sleepTime);
 						} catch (InterruptedException e) {
 							e.printStackTrace();
 						}
 						currentTime = System.currentTimeMillis();
 						
 						volumeMultiplier += 0.1;
 						
 						for (SoundPlayer soundPlayer : soundPlayerList) {
 							soundPlayer.setFadeVolume(soundPlayer.getLeftVolume()*volumeMultiplier, 
 									soundPlayer.getRightVolume()*volumeMultiplier);
 						}
 					}
 				}
 			};
 			t.start();
 		} else {
 			for (SoundPlayer soundPlayer : soundPlayerList) {
 				soundPlayer.setFadeVolume(soundPlayer.getLeftVolume(), soundPlayer.getRightVolume());
 				soundPlayer.start();
 			}
 		}
     }
 	
 	public static boolean isPlaying(File soundPath) {
 		ListIterator<SoundPlayer> iterator = SoundboardMenu.mSoundPlayerList.listIterator();
     	boolean playing = false;
         
 		while (iterator.hasNext()) {
 			SoundPlayer soundPlayer = iterator.next();
 			if (soundPlayer.isPlaying() && soundPlayer.getPlayingFile().equals(soundPath.getAbsolutePath())) {
 				playing = true;
 				break;
 	    	}
 		}
 		return playing;
 	}
 
 	public static void pauseSound(boolean playSimultaneously, File soundPath, Float volumeLeft, Float volumeRight, 
 			Float boardVolume) {
 
 		ListIterator<SoundPlayer> iterator = SoundboardMenu.mSoundPlayerList.listIterator();
 		boolean soundFound = false;
 		while (iterator.hasNext()) {
 			SoundPlayer soundPlayer = iterator.next();
 			if (soundPlayer.getPlayingFile().equals(soundPath.getAbsolutePath())) {
 				if (soundPlayer.isPlaying() == true) {
 					soundPlayer.pause();
 				} else {
 					soundPlayer.setFadeVolume(soundPlayer.getLeftVolume(), soundPlayer.getRightVolume());
 					soundPlayer.start();
 				}
 				soundFound = true;
 				break;
 			}
 		}
 		if (!soundFound) {
 			playSound(playSimultaneously, soundPath, volumeLeft, volumeRight, boardVolume);
 		}
 	}
 	
 	public static void stopSound(boolean playSimultaneously, File soundPath, Float volumeLeft, Float volumeRight, 
 			Float boardVolume) {
 		
 		ListIterator<SoundPlayer> iterator = SoundboardMenu.mSoundPlayerList.listIterator();
 		boolean soundFound = false;
 		while (iterator.hasNext()) {
 			SoundPlayer soundPlayer = iterator.next();
 			if (soundPlayer.getPlayingFile() == soundPath.getAbsolutePath()) {
 				if (soundPlayer.isPlaying() == true) {
 					soundPlayer.release();
 					iterator.remove();
 				} else {
 					soundPlayer.setFadeVolume(soundPlayer.getLeftVolume(), soundPlayer.getRightVolume());
 					soundPlayer.start();
 				}
 				soundFound = true;
 				break;
 			}
 		}
 		if (!soundFound) {
 			playSound(playSimultaneously, soundPath, volumeLeft, volumeRight, boardVolume);
 		}
 	}
 	
 	public static SoundPlayer getSoundPlayer(File soundPath) {
 		SoundPlayer soundPlayer = null;
 		ListIterator<SoundPlayer> iterator = SoundboardMenu.mSoundPlayerList.listIterator();
 		while (iterator.hasNext()) {
 			SoundPlayer iteratorSoundPlayer = iterator.next();
 			if (iteratorSoundPlayer.getPlayingFile() == soundPath.getAbsolutePath()) {
 				soundPlayer = iteratorSoundPlayer;
 	    	}
 		}
 		return soundPlayer;
 	}
 }
