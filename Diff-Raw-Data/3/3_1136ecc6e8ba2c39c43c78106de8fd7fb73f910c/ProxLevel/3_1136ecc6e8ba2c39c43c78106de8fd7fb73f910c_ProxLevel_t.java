 package propinquity;
 
 import processing.core.*;
 import processing.xml.*;
 import propinquity.hardware.*;
 import ddf.minim.*;
 import java.util.*;
 
 /**
  * The ProxLevel is the "original" game mechanic for Propinquity, players score by being in proximity to the opponent's patches. The opponent's patches may not be on at all times. There are also cooperative and versus rounds and there are pauses between rounds. It supports loading a level from an XML file and can have multiple songs per level.
  *
  * It is currently not in use, but support two scoring zones.
  *
  */
 public class ProxLevel extends Level {
 
 	static int TOTAL_LEN = 180000; //2min;
 
 	long[] lastScoreTime;
 	long[] lastScoreTimePauseDiff;
 
 	AudioPlayer song;
 	AudioSample gong;
 
 	int transitionCount;
 	Vector<AudioPlayer> songs;
 
 	String songFile;
 	int songBPM;
 
 	VolumeFader fader;
 
 	Step[] steps;
 	long stepInterval;
 	int currentStep;
 
 	String name;
 
 	boolean coop, lastCoop;
 	boolean running;
 	
 	int coopScore;
 
 	long startTime, startTimeDiff;
 
 	public ProxLevel(Propinquity parent, Hud hud, Sounds sounds, String levelFile, Player[] players) throws XMLException {
 		super(parent, hud, sounds, players);
 
 		gong = sounds.getGong();
 
 		lastScoreTime = new long[players.length];
 		lastScoreTimePauseDiff = new long[players.length];
 		coopScore = 0;
 
 		startTime = -1;
 
 		songs = new Vector<AudioPlayer>();
 
 		XMLElement xml = new XMLElement(parent, levelFile);
 
 		name = xml.getString("name");
 
 		if(name == null) {
 			name = "Level";
 			System.err.println("Warning: XML file \""+levelFile+"\" contained no level name. Name defaulted to \"Level\"");
 		}
 
 		XMLElement[] song_tags = xml.getChildren("song");
 
 		if(song_tags.length > 0) {
 			if(song_tags.length > 1) {
 				System.err.println("Warning: XML contained multiple songs tags for a single Level. Ignoring extra tags.");
 			}
 
 			XMLElement song = song_tags[0];
 
 			songFile = song.getString("file");
 			if(songFile.equals("")) {
 				throw new XMLException("XMLException: XML song tag has empty file attribute");
 			}
 
 			songBPM = song.getInt("bpm", DEFAULT_BPM);
 		} else {
 			throw new XMLException("XMLException: XML for level \"" + name + "\" has no song tag");
 		}
 
 		try {
 			song = sounds.loadSong(songFile);
 		} catch(Exception e) {
 			throw new NullPointerException("Loading song file failed. Likely file name invalid or file missing for level \""+name+"\". Given file name was \""+songFile+"\".");
 		}
 
 		songs.add(song);
 
 		XMLElement[] step_tags = xml.getChildren("sequence/step");
 		steps = new Step[step_tags.length+1];
 		stepInterval = TOTAL_LEN/(step_tags.length+1);
 
 		if(step_tags.length > 0) {
 			for(int i = 0; i < step_tags.length; i++) {
 				String modeString = step_tags[i].getString("mode");
 				StepType type = null;
 				if(modeString.equals("versus")) {
 					type = StepType.VERSUS;
 				} else if(modeString.equals("transition")) {
 					type = StepType.TRANSITION; //FIXME: Rework the transision mechanism
 					String songFile = step_tags[i].getString("file");
 					if(songFile == null || songFile.equals("")) {
 						System.err.println("Warning: XML for level \""+name+"\" step "+i+" is a transition tag with no file attribute, this might be corrent, but you should be sure");
 					} else {
 						try {
 							AudioPlayer song = sounds.loadSong(songFile);
 							songs.add(song);
 						} catch(Exception e) {
 							throw new NullPointerException("Loading song file failed. Likely file name invalid or file missing for level \""+name+"\". Given file name was \""+songFile+"\".");
 						}
 					}
 				} else {
 					type = StepType.COOP;
 				}
 
 				XMLElement[] player_tags = step_tags[i].getChildren("player");
 				boolean patches[][] = new boolean[player_tags.length][4];
 				if(player_tags.length >= players.length) {
 					for(int j = 0; j < player_tags.length; j++) {
 						patches[j][0] = (player_tags[j].getInt("patch1", 0) != 0);
 						patches[j][1] = (player_tags[j].getInt("patch2", 0) != 0);
 						patches[j][2] = (player_tags[j].getInt("patch3", 0) != 0);
 						patches[j][3] = (player_tags[j].getInt("patch4", 0) != 0);
 					}
 				} else {
 					throw new XMLException("XMLException: XML for level \"" + name + "\", step " + i + " has too few player tags.");
 				}
 
 				steps[i] = new Step(type, patches);
 			}
 
 			boolean[][] tmpPatchState = new boolean[players.length][];
 			for(int i = 0;i < tmpPatchState.length;i++) tmpPatchState[i] = new boolean[] {false, false, false, false};
 
 			steps[step_tags.length] = new Step(StepType.VERSUS, tmpPatchState);
 		} else {
 			throw new XMLException("Warning: XML for level \"" + name + "\" has no sequence tag and/or no step tags");
 		}
 		
 		fader = new VolumeFader();
 
 		reset();
 	}
 
 	public void pause() {
 		song.pause();
 		running = false;
 		for(int i = 0;i < players.length;i++) {
 			players[i].pause();
 			lastScoreTimePauseDiff[i] = parent.millis()-lastScoreTime[i];
 		}
 		startTimeDiff = parent.millis()-startTime;
 	}
 
 	public void start() {
 		for(int i = 0;i < players.length;i++) {
 			players[i].start();
 			lastScoreTime[i] = parent.millis()-lastScoreTimePauseDiff[i];
 		}
 		running = true;
 		if(startTime == -1) startTime = parent.millis();
 		else startTime = parent.millis()-startTimeDiff;
 		song.play();
 	}
 
 	public void reset() {
 		running = false;
 		
 		startTime = -1;
 
 		for(Player player : players) player.reset(); //Clears all the particles, scores, patches and gloves
 
 		lastScoreTime = new long[players.length];
 		lastScoreTimePauseDiff = new long[players.length];
 		coopScore = 0;
 		
 		for(AudioPlayer song : songs) {
 			song.pause();
 			song.rewind();
 			song.setGain(0);
 			//rewound
 		}
 
 		song = songs.get(0);
 		stepUpdate(0); //Load for banner
 	}
 
 	public void close() {
 		song.close();
 	}
 	
 	void stepUpdate(int nextStep) {
 		currentStep = nextStep;
 		
 		coop = steps[currentStep].isCoop();
 		boolean[][] patchStates = steps[currentStep].getPatches();
 		
 		if(steps[currentStep].isTransition() && (currentStep == 0 || (currentStep > 0 && !steps[currentStep-1].isTransition()))) {
 			transitionCount++;
 			fader.fadeOut();
 			for(Player player : players) player.transferScore();
 		} else if(!steps[currentStep].isTransition() && currentStep > 0 && steps[currentStep-1].isTransition()) {
 			if(songs.size() > transitionCount) {
 				song.pause();
 				song = songs.get(transitionCount);
 				song.play();
 			}
 			// fader.fadeIn();
 			song.setGain(0);
 		}
 
 		// Reset coop score when leaving coop step.
 		if(lastCoop && !coop) coopScore = 0;
 	
 		if(currentStep >= steps.length-1) { //Last step is the end of the level we want all patches off
 			for(Player player : players) {
 				player.transferScore();
 				player.clearPatches();
 				player.clearGloves();
 				player.bump();
 			}
 
 			fader.fadeOut(); //Mute the sound
 		} else { //Otherwise do the step
 			for(int i = 0;i < players.length;i++) {
 				if(i < patchStates.length) {
 					players[i].step(coop, patchStates[i]);
 				} else {
 					System.err.println("Warning: There are more players than we have patch state data for in level \""+name+"\", step number "+currentStep+". Specifically we have no data for player number "+i+". We'll refrain form setting the player's patches for this step.");
 					break;
 				}
 			}
 		}
 
 		lastCoop = coop;
 
 	}
 
 	public void proxEvent(Patch patch) {
 		if(!isRunning() || isDone()) return;
 		if(!patch.getActive()) return;
 		//Handle patch feedback
 		patch.setMode(patch.getZone());
 	}
 
 	public void update() {
 		for(Player player : players) player.update();
 
 		//Handle Glove feedback
 		for(int i = 0;i < players.length;i++) {
 			Glove glove = players[i].getGlove();
 			if(glove.getActive()) {
 				Patch bestPatch = players[(i+1)%players.length].getBestPatch(); //TODO: Hack being use to get opponent. Nothing significantly better can be done with this hardware.
 				if(bestPatch != null) glove.setMode(bestPatch.getZone());
 				else glove.setMode(0);
 			}
 		}
 
 		//Handle score
 		long currentTime = parent.millis();
 		
 		for(int i = 0;i < players.length;i++) {
 			Player proxPlayer = players[(i+1)%players.length]; //TODO: Hack being use to get opponent. Nothing significantly better can be done with this hardware.
 			Player scoringPlayer = players[i];
 
 			Patch bestPatch = proxPlayer.getBestPatch();
 
 			if(bestPatch != null && bestPatch.getZone() > 0) {
 				if(coop) {
 					if(currentTime-lastScoreTime[i] > proxPlayer.getSpawnInterval() * COOP_FUDGE_FACTOR) {
 						coopScore++;
 						proxPlayer.addPoints(1, PlayerConstants.NEUTRAL_COLOR);
 						scoringPlayer.addPoints(1, PlayerConstants.NEUTRAL_COLOR);
 						lastScoreTime[i] = currentTime;
 					}
 				} else {
 					if(currentTime-lastScoreTime[i] > proxPlayer.getSpawnInterval()) {
 						scoringPlayer.addPoints(1);
 						lastScoreTime[i] = currentTime;
 					}
 				}
 			} else {
 				// lastScoreTime[i] = currentTime;
 			}
 		}
 
 		int nextStep = (int)PApplet.constrain((parent.millis()-startTime)/stepInterval, 0, steps.length-1);
 		if(nextStep != currentStep) stepUpdate(nextStep);
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public Player getWinner() {
 		Player winner = null;
 		int highScore = -1;
 
 		for(Player player : players) {
 
 			if(player.getScore() > highScore) {
 				winner = player;
 				highScore = player.getScore();
 			} else if(player.getScore() == highScore) {
 				winner = null;
 			}
 		}
 
 		return winner;
 	}
 
 	public boolean isRunning() {
 		return running;
 	}
 
 	public boolean isDone() {
 		return (currentStep >= steps.length-1);
 	}
 	
 	public void keyPressed(char key, int keyCode) {
 		if(!isVisible) return;
 
 		switch(key) {
 			case BACKSPACE: {
 				if(song.position() == 0) parent.changeGameState(GameState.LevelSelect);
 				else reset(); //Make sure particles are gone
 				break;
 			}
 
 			case ENTER:
 			case ' ': {
 				if(isDone()) {
 					reset(); //Make sure particles are gone
 					parent.changeGameState(GameState.LevelSelect);
 				} else {
 					if(isRunning()) pause();
 					else start();
 				}
 				break;
 			}
 
 			case 'e': { //Force End 
 				// song.cue(song.length()-1000);
 				startTime = parent.millis()-179000;
 				break;
 			}
 		}
 	}
 
 	public void draw() {
 		if(!isVisible) return;
 
 		//Particles and Liquid
 		if(!PARTICLES_ABOVE) for(int i = 0; i < players.length; i++) players[i].draw();
 
 		//Outlines
 		hud.drawInnerBoundary();
 		hud.drawOuterBoundary();
 
 		//Score Banners
 		if(coop) {
 			String score = String.valueOf(coopScore);
 			String name = "Coop";
 
 			while(parent.textWidth(score + name) < 240) name += ' ';
 
 			hud.drawBannerCenter(name + score, PlayerConstants.NEUTRAL_COLOR, hud.getAngle());
 		} else {
 			for(int i = 0; i < players.length; i++) {
 				String score = String.valueOf(players[i].score.getScore());
 				String name = players[i].getName();
 
 				while(parent.textWidth(score + name) < 240) name += ' ';
 
 				hud.drawBannerSide(name + score, PlayerConstants.PLAYER_COLORS[i], hud.getAngle() - PConstants.HALF_PI + (i * PConstants.PI));
 			}
 		}
 
 		//Particles and Liquid
 		if(PARTICLES_ABOVE) for(int i = 0; i < players.length; i++) players[i].draw();
 
 		if(isDone()) { //Someone won
 			Player winner = getWinner();
 			String text = winner != null ? winner.getName() + " won!" : "You Tied!";
 			Color color = winner != null ? winner.getColor() : PlayerConstants.NEUTRAL_COLOR;
 			if(coop) {
 				text = "";
 				color = PlayerConstants.NEUTRAL_COLOR;
 			}
 			hud.drawCenterText("", text, color, hud.getAngle());
 			hud.drawCenterImage(hud.hudPlayAgain, hud.getAngle());
 		} else if(isRunning()) { //Running
 			update();
 		} else { //Pause
 			hud.drawCenterImage(hud.hudPlay, hud.getAngle());
 		}
 	}
 
 	class VolumeFader implements Runnable {
 
 		Thread thread;
 
 		boolean running, fadeIn;
 
 		public void stop() {
 			if(thread != null && thread.isAlive()) {
 				running = false;
 				thread.interrupt();
 				while(thread.isAlive()) Thread.yield();
 			}
 		}
 
 		public void fadeIn() {
 			stop();
 			fadeIn = true;
 			running = true;
 			thread = new Thread(this);
 			thread.setDaemon(true);
 			thread.start();
 		}
 
 		public void fadeOut() {
 			stop();
 			fadeIn = false;
 			running = true;
 			thread = new Thread(this);
 			thread.setDaemon(true);
 			thread.start();
 		}
 
 		public void run() {
 			if(fadeIn) {
 				for(int i = 100;i >= 0;i--) {
 					song.setGain(-(float)i/4);
 					try {
 						Thread.sleep(20);
 					} catch(Exception e) {
 
 					}
 				}
 				song.setGain(0);
 			} else {
 				gong.trigger();
 
 				for(int i = 0;i < 100;i++) {
 					song.setGain(-(float)i/4);
 					try {
 						Thread.sleep(20);
 					} catch(Exception e) {
 
 					}
 				}
 				song.setGain(-100);
				song.pause();
 			}
 		}
 
 	}
 
 }
