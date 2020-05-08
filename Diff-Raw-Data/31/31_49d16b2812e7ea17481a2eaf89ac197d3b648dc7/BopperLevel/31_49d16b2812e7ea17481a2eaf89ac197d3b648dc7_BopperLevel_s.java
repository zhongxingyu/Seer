 package propinquity;
 
 import ddf.minim.*;
 import processing.core.*;
 import propinquity.hardware.*;
 
 /**
  * <p>The BopperLevel is a whack-a-mole type turn based game. In each round one player attacks and one player defends. All the defending player's patches light up and the attacking player has a fixed amount of time to "bop" the defending player's patches. Once the round has elapsed the attacking and defending players are switched.</p>
  *
  * <p>When a patch is "bopped" by proximity the attacking player gets a fixed number of points and the patch is turned off, thus the attacker can no longer score on that patch. Thus there is a maximum number of potential points per round.</p>
  *
  * <p>This implementation was only a rough sketch done for playtesting and should be reworked if it is to be used permanently.</p>
  *
  */
 public class BopperLevel extends Level {
 
 	static int NUM_ROUNDS = 3;
 	
 	static int ROUND_TIME = 10000;
 	static int GAP_TIME = 5000;
 
 	static int BOP_TIME = 500;
 
 	AudioPlayer song;
 	String songFile;
 
 	AudioSample gong;
 	AudioSample whoosh;
 
 	int currentRound;
 	boolean gap;
 
 	boolean scoring;
 	long lastTime;
 	long lastTimeDiff;
 
 	Player defendingPlayer;
 	Player scoringPlayer;
 
 	boolean running;
 
 	public BopperLevel(Propinquity parent, Hud hud, Sounds sounds, String songFile, Player[] players) {
 		super(parent, hud, sounds, players);
 
 		try {
 			song = sounds.loadSong(songFile);
 		} catch(Exception e) {
 			throw new NullPointerException("Loading song file failed. Likely file name invalid or file missing for BopperLevel. Given file name was \""+songFile+"\".");
 		}
 
 		// song.setGain(-100); //To mute the music
 
 		gong = sounds.getGong();
 		whoosh = sounds.getWhooshBubble();
 
 		reset();
 	}
 
 	public void pause() {
 		song.pause();
 		running = false;
 		lastTimeDiff = parent.millis()-lastTime;
 
 		for(int i = 0;i < players.length;i++) players[i].pause();
 	}
 
 	public void start() {
 		for(int i = 0;i < players.length;i++) players[i].start();
 		lastTime = parent.millis()-lastTimeDiff;
 		running = true;
 		song.play();
 	}
 
 	public void reset() {
 		song.pause();
 		running = false;
 
 		for(Player player : players) {
          player.configurePatches(Mode.PROX);
		  player.reset(); //Clears all the particles, scores, patches and gloves
 		}
 
 		song.rewind();
 
 		roundUpdate(0, false);
 	}
 
 	public void close() {
 		song.close();
 	}
 	
 	void roundUpdate(int nextRound, boolean nextGap) {
 		currentRound = nextRound;
 		gap = nextGap;
 
 		for(Player player : players) player.transferScore();
 
 		if(currentRound >= NUM_ROUNDS*players.length) {
 			for(Player player : players) {
 				player.transferScore();
 				player.clearPatches();
 				player.clearGloves();
 				player.bump();
 			}
 
 			defendingPlayer = null;
 			scoringPlayer = null;
 		} else {
 			if(gap) {
 				for(Player player : players) {
 					player.clearPatches();
 					player.clearGloves();
 				}
 
 				defendingPlayer = null;
 				scoringPlayer = null;
 
 				gong.trigger();
 			} else {
 				defendingPlayer = players[currentRound%players.length];
 				defendingPlayer.activatePatches();
 
 				for(Player player : players) {
 					if(player != defendingPlayer) player.clearPatches();
 				}
 
 				scoringPlayer = players[(currentRound+1)%players.length]; //TODO: Hack being use to get opponent. Nothing significantly better can be done with this hardware.
 			}
 		}
 	}
 
 	public void proxEvent(Patch patch) {
 		return;
 	}
 
    public void accelXYZEvent(Patch patch) {
      
    }
 
    public void accelInterrupt0Event(Patch patch) {
      
    }
 
    public void accelInterrupt1Event(Patch patch) {
      
    }
 
     public void update() {
 		for(Player player : players) player.update();
 	
 		long currentTime = parent.millis();
 		
 		if(defendingPlayer != null && scoringPlayer != null) {
 			Patch bestPatch = defendingPlayer.getBestPatch();
 
 			if(bestPatch != null && bestPatch.getZone() > 0) {
 				if(!scoring) {
 					whoosh.trigger();
 					scoring = true;
 				}
 
 				if(currentTime-lastTime > BOP_TIME) {
 					scoringPlayer.addPoints(5, false);
 
 					(new Thread(new Runnable() {
 						public void run() {
 							scoringPlayer.getGlove().setVibeLevel(255);
 							try {
 								Thread.sleep(1500);
 							} catch(Exception e) {
 
 							}
 							scoringPlayer.getGlove().setVibeLevel(0);
 						}
 					})).start();
 
 
 					bestPatch.setActive(false);
 					
 					lastTime = currentTime;
 					scoring = false;
 				}
 			} else {
 				if(scoring) {
 					whoosh.stop();
 					scoring = false;
 				}
 
 				lastTime = currentTime;
 			}
 		}
 
 		int nextRound = song.position()/(ROUND_TIME+GAP_TIME);
 		boolean nextGap = false;
 		if(song.position()-(ROUND_TIME+GAP_TIME)*nextRound > ROUND_TIME) nextGap = true;
 
 		if(nextRound != currentRound || nextGap != gap) roundUpdate(nextRound, nextGap);
 	}
 
 	public String getName() {
 		return "Bopper";
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
 
 	public int getTotalPoints() {
 		int total = 0;
 		for(int i = 0; i < players.length; i++) total += players[i].getScore();
 		return total;
 	}
 
 	public boolean isRunning() {
 		return running;
 	}
 
 	public boolean isDone() {
 		return (currentRound >= NUM_ROUNDS*players.length);
 	}
 	
 	public void keyPressed(char key, int keyCode) {
 		if(!isVisible) return;
 
 		switch(key) {
 			case BACKSPACE: {
 				reset(); //Make sure particles are gone
 				if(song.position() == 0) parent.changeGameState(GameState.LevelSelect);
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
 				song.cue(song.length()-1000);
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
 		for(int i = 0; i < players.length; i++) {
 			String score = String.valueOf(players[i].score.getScore());
 			String name = players[i].getName();
 
 			while(parent.textWidth(score + name) < 240) name += ' ';
 
 			hud.drawBannerSide(name + score, PlayerConstants.PLAYER_COLORS[i], hud.getAngle() - PConstants.HALF_PI + (i * PConstants.PI));
 		}
 
 		//Particles and Liquid
 		if(PARTICLES_ABOVE) for(int i = 0; i < players.length; i++) players[i].draw();
 
 		if(isDone()) { //Someone won
 			Player winner = getWinner();
 			String text = winner != null ? winner.getName() + " won!" : "You Tied!";
 			Color color = winner != null ? winner.getColor() : PlayerConstants.NEUTRAL_COLOR;
 			hud.drawCenterText("", text, color, hud.getAngle());
 			hud.drawCenterImage(hud.hudPlayAgain, hud.getAngle());
 		} else if(isRunning()) { //Running
 			update();
 		} else { //Pause
 			hud.drawCenterImage(hud.hudPlay, hud.getAngle());
 		}
 	}
 
 }
