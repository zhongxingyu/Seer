 /*
  * Copyright 2011 Alexander Baumgartner
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.madthrax.ridiculousRPG.audio;
 
 import java.util.concurrent.Semaphore;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.audio.Music;
 import com.madthrax.ridiculousRPG.service.GameServiceDefaultImpl;
 
 /**
 * This service plays background music.<br>
  * NEEDS REFACTORING!
  * 
  * @author Alexander Baumgartner
  */
 // TODO: NEEDS REFACTORING
 public class JukeboxService extends GameServiceDefaultImpl {
 	private Semaphore musicMutex = new Semaphore(1, true);
 	private boolean playingMusic, waiting;
 	private Music backgroundMusic = null;
 	private float volume = .5f;
 	private boolean freezed;
 
 	public void setVolume(float volume) {
 		this.volume = volume;
 		if (backgroundMusic != null) {
 			backgroundMusic.setVolume(volume);
 		}
 	}
 
 	public void playBackgroundMusic(String internalPath, boolean fade) {
 		Music newBGM = Gdx.audio.newMusic(Gdx.files.internal(internalPath));
 		new MusicChanger(backgroundMusic, newBGM, volume, fade);
 		backgroundMusic = newBGM;
 	}
 
 	public void stopBackgroundMusic(boolean fade) {
 		new MusicChanger(backgroundMusic, null, volume, fade);
 	}
 
 	public class MusicChanger extends Thread {
 		private Music oldMusic, newMusic;
 		private float volume;
 		private int speed;
 		private boolean fade, loop;
 
 		public MusicChanger(Music oldMusic, Music newMusic) {
 			this(oldMusic, newMusic, 1f, true);
 		}
 
 		public MusicChanger(Music oldMusic, Music newMusic, float volume,
 				boolean fade) {
 			this(oldMusic, newMusic, volume, fade, true, 20);
 		}
 
 		public MusicChanger(Music oldMusic, Music newMusic, float volume,
 				boolean fade, boolean loop, int speed) {
 			this.oldMusic = oldMusic;
 			this.newMusic = newMusic;
 			this.volume = volume;
 			this.fade = fade;
 			this.loop = loop;
 			this.speed = speed;
 			start();
 		}
 
 		@Override
 		public void run() {
 			while (!musicMutex.tryAcquire()) {
 				try {
 					waiting = true;
 					Thread.sleep(speed);
 				} catch (InterruptedException e) {
 				}
 			}
 			waiting = false;
 			if (oldMusic != null) {
 				if (oldMusic.isPlaying()) {
 					for (float vol = volume; fade && vol > 0.01f; vol -= 0.01f) {
 						oldMusic.setVolume(vol);
 						try {
 							Thread.sleep(speed);
 						} catch (InterruptedException e) {
 						}
 					}
 					oldMusic.stop();
 				}
 				oldMusic.dispose();
 				oldMusic = null;
 			}
 			if (newMusic != null) {
 				newMusic.setLooping(loop);
 				newMusic.setVolume(0.01f);
 				// check freeze
 				if (freezed) {
 					playingMusic = true;
 				} else if (!waiting) {
 					newMusic.play();
 					for (float vol = 0.02f; fade && !waiting
 							&& vol < volume - 0.01f; vol += 0.01f) {
 						try {
 							Thread.sleep(speed);
 						} catch (InterruptedException e) {
 						}
 						newMusic.setVolume(vol);
 					}
 				}
 				newMusic.setVolume(volume);
 			}
 			musicMutex.release();
 		}
 	}
 
 	@Override
 	public void freeze() {
 		freezed = true;
 		if (backgroundMusic != null && backgroundMusic.isPlaying()) {
 			backgroundMusic.pause();
 		}
 	}
 
 	@Override
 	public void unfreeze() {
 		freezed = false;
 		if (playingMusic)
 			backgroundMusic.play();
 	}
 
 	public void dispose() {
 		if (backgroundMusic != null) {
 			if (backgroundMusic.isPlaying())
 				backgroundMusic.stop();
 			backgroundMusic.dispose();
 			backgroundMusic = null;
 		}
 	}
 }
