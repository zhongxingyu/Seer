 package yuuki.sound;
 
 /**
  * An AudioEngine intended for playing long tracks that loop. Only one track
  * at a time is allowed.
  */
 class MusicEngine extends AudioEngine {
 	
 	/**
 	 * The currently playing music.
 	 */
 	private SoundPlayer musicPlayer;
 	
 	/**
 	 * @inheritDoc
 	 */
 	protected void spawnPlayerThread(String soundFile) {
 		byte[] data = sounds.get(soundFile);
 		stopSound();
 		musicPlayer = new SoundPlayer(data, getVolume());
 		(new Thread(musicPlayer, "MusicPlayer")).start();
 	}
 	
 	/**
	 * Change the volume of the currently playing music.
	 * 
	 * @param volume The new volume.
	 */
	@Override
	public void setVolume(int volume) {
		super.setVolume(volume);
		if (musicPlayer != null) {
			musicPlayer.setVolume(volume);
		}
	}
	
	/**
 	 * Stops the current sound immediately.
 	 */
 	public void stopSound() {
 		if (musicPlayer != null) {
 			musicPlayer.stop();
 		}
 	}
 	
 }
