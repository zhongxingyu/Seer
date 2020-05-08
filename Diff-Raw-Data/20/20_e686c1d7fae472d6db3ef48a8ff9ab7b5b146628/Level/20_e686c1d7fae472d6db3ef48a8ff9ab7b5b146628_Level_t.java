 package propinquity;
 
 import processing.core.PApplet;
 import proxml.XMLElement;
 import proxml.XMLInOut;
 import xbee.XBeeReader;
 
 public class Level {
 
 	/*
 	 * Here's how the data packet is constructed.
 	 * 
 	 * Byte 1: __ type (2 bits) ___ destination address (3 bits) _ free (1 bit)
 	 * xx not used (2 bits)
 	 * 
 	 * Byte 2: sequence number
 	 * 
 	 * Byte 3: ____ current LED states ____ next LED states
 	 * 
 	 * Byte 4: ____ next+1 LED states ____ next+2 LED states
 	 */
 
 	// level parameters
 	public String songName;
 	public String songFile;
 	public String songDuration;
 	public int tempo;
 	public int multiplier;
 	public int stepCount;
 	public int currentStep;
 	
 	public int successfullyRead = -1; // -1 -> not read yet, 0 -> false, 1 -> true
 
 	private boolean isCoop;
 	private boolean lastCoopDone;
 	private boolean isRunning;
 	private int coopPoints;
 	private long time;
 	private long lastUpdate;
 	private long lastStep;
 	private long stepInterval;
 
 	// if there is nothing in the file, we want to default to 2 min of lights-on
 	// free play.
 	private static final int MAX_PLAYERS = 2;
 	private static final int MAX_STEPS = 256;
 	private static final int DEFAULT_PLAYERS = 2;
 	private static final int DEFAULT_TEMPO = 120;
 	private static final int DEFAULT_MULTIPLIER = 4;
 	private static final int DEFAULT_STEPS = 80;
 
 	// parent applet
 	private Propinquity parent;
 	private Sounds sounds;
 
 	private XMLElement levelXML;
 	private Player[] players;
 
 	public Level(Propinquity parent, Sounds sounds) {
 		this(parent, sounds, null, null);
 	}
 
 	public Level(Propinquity parent, Sounds sounds, Player[] players, String levelFile) {
 		this.parent = parent;
 		this.sounds = sounds;
 
 		if(players == null) {
 			players = new Player[2];
 			players[0] = new Player(parent, parent.patches, parent.glove, parent.playerColors[0]);
 			players[0].name = "Player 1";
 			players[1] = new Player(parent, parent.patches, parent.glove, parent.playerColors[1]);
 			players[1].name = "Player 2";
 		} else {
 			this.players = players;
 		}
 
 		if(levelFile != null) {
 			parent.xmlInOut = new XMLInOut(parent, this);
 			parent.xmlInOut.loadElement(levelFile);
 		}
 		
 		players[0].registerNegativePlayerSound(sounds.negativeP1);
 		players[0].registerNegativeCoopSound(sounds.negativeCoop);
 		players[1].registerNegativePlayerSound(sounds.negativeP2);
 		players[1].registerNegativeCoopSound(sounds.negativeCoop);
 
 		lastCoopDone = false;
 		reset();
 	}
 
 	public void clear() {
 		for(int i = 0; i < players.length; i++)
 			players[i].clear();
 	}
 
 	public void reset() {
 		if(players != null) {
 			for(int i = 0; i < players.length; i++)
 				players[i].reset();
 		}
 
 		// TODO: hmm...
 		// For March 28 playtest, setting one player to take prox data
 		// one is faked
 		currentStep = 0;
 		tempo = DEFAULT_TEMPO;
 		multiplier = DEFAULT_MULTIPLIER;
 		stepInterval = (long) (60f / tempo * multiplier * 1000);
 		// stubReading = 0;
 		time = 0;
 		lastStep = -PApplet.MAX_INT;
 		isRunning = false;
 		lastUpdate = 0;
 
 		// rewind song
 		if(sounds.song != null)
 			sounds.song.rewind();
 	}
 
 	public void pause() {
 		isRunning = false;
 		sounds.song.pause();
 	}
 
 	public void start() {
 		isRunning = true;
 		lastUpdate = parent.millis();
 
 		if(!Sounds.MUTE)
 			sounds.song.play();
 	}
 
 	public void update() {
 		long now = parent.millis();
 		long dt = now - lastUpdate;
 		if(dt > 0)
 			time += dt;
 		lastUpdate = now;
 
 		// process stubs
 		for(int i = 0; i < players.length; i++) {
 			// players[i].stub(time);
 			ProxData psd;
 			while((psd = players[i].nextProxStub(time)) != null) {
 				processProxReading(psd);
 				// proxStubIndex++;
 			}
 			
 			players[i].update();
 		}
 
 		parent.box2d.setGravity(0, 0);
 		
 		// process step
 		if(time - lastStep > stepInterval)
 			step();
 	}
 	
 	public void draw() {
 		for(int i = 0; i < players.length; i++)
 			players[i].draw();
 	}
 
 	private void step() {
 		// println("Step " + currentStep + " ("+time+")");
 
 		// keep track of time
 		lastStep = time;
 
 		// process each player step
 		if(currentStep < stepCount)
 			for(int i = 0; i < players.length; i++)
 				players[i].sendStep(currentStep);
 
 		// increment
 		currentStep++;
 	}
 
 	public boolean isCoop() {
 		return isCoop;
 	}
 
 	public boolean isInCoopMode() {
 		return (isCoop && (coopPoints == 0 || getTotalPoints() < coopPoints * 2));
 	}
 
 	public boolean isCoopDone() {
 		return (coopPoints != 0) && (getTotalPoints() / 2) >= coopPoints;
 	}
 
 	public boolean getLastCoopDone() {
 		return lastCoopDone;
 	}
 
 	public void setLastCoopDone(boolean b) {
 		lastCoopDone = b;
 		if(b) {
 			players[0].setCoopMode(!b);
 			players[1].setCoopMode(!b);
 		}
 	}
 
 	public int getNumberOfPlayers() {
 		return players.length;
 	}
 
 	public int getNumberOfSteps() {
 		return stepCount;
 	}
 
 	public long getStepInterval() {
 		return stepInterval;
 	}
 
 	public Player getPlayer(int i) {
 		return players[i];
 	}
 
 	public Player getWinner() {
 		int maxScore = -1;
 		int winner = -1;
 
 		// TODO: This "loop" looks a little fishy...
 		for(int i = 0; i < players.length; i++)
 			// check if we have a winner
 			if(players[i].getTotalPts() > maxScore) {
 				maxScore = players[i].getTotalPts();
 				winner = i;
 			}
 			// if not check if we have a tie
 			else if(players[i].getTotalPts() == maxScore) {
 				winner = -1;
 			}
 
 		if(winner == -1)
 			return null;
 		else
 			return players[winner];
 	}
 
 	public int getTotalPoints() {
 		int total = 0;
 		for(int i = 0; i < players.length; i++)
 			total += players[i].score.getScore();
 		return total;
 	}
 
 	public boolean isRunning() {
 		return isRunning;
 	}
 
 	public boolean isDone() {
 		return (currentStep > stepCount);
 	} // extra step to make sure the last one got in
 
 	public long getTime() {
 		return time;
 	}
 
 	public void load() {
 
 		while(true)
 			if(successfullyRead > -1)
 				break;
 
 		if(successfullyRead == 0) {
 			loadDefaults();
 			System.err.println("I had some trouble reading the level file.");
 			System.err.println("Defaulting to 2 minutes of free play instead.");
 		}
 		
 		for(int i = 0; i < players.length; i++) 
 			parent.simulator.addProxEventListener(players[i]);
 	}
 
 	public void loadSong(XMLElement songXML) {
 		// load number of players
 		int numPlayers = songXML.countChildren() - 1;
 
 		// check if we have an correct level file
 		if(numPlayers < 0) {
 			System.out.println("Error: Empty level file");
 			successfullyRead = 0;
 			return;
 		}
 
 		// read song
 		songName = songXML.getChild(0).getAttribute("name");
 		songFile = songXML.getChild(0).getAttribute("file");
 		songDuration = songXML.getChild(0).getAttribute("duration");
 		tempo = songXML.getChild(0).getIntAttribute("bpm");
 		multiplier = songXML.getChild(0).getIntAttribute("multiplier");
 
 		successfullyRead = 1;
 	}
 
 	private void loadDefaults() {
 
 		tempo = DEFAULT_TEMPO;
 		multiplier = DEFAULT_MULTIPLIER;
 		stepCount = DEFAULT_STEPS;
 		stepInterval = (long) (60f / tempo * multiplier * 1000);
 
 		for(int i = 0; i < DEFAULT_PLAYERS; i++) {
 			players[i].initializeSteps(stepCount);
 			for(int j = 0; j < stepCount; j++) {
 				Step step = new Step(true, true, true, true, true);
 				players[i].addStep(step, j);
 			}
 		}
 	}
 
 	public void xmlEvent(XMLElement songXML) {
 		levelXML = songXML;
 		XMLElement player;
 		XMLElement sequence;
 		XMLElement step;
 		int numPlayers, numSteps;
 
 		// load number of players
 		numPlayers = levelXML.countChildren() - 1;
 
 		// check if we have an correct level file
 		if(numPlayers < 2) {
 			System.out.println("Error: Bad level file. We need data for 2 players.");
 			successfullyRead = 0;
 			return;
 		}
 		// limit number of players to default (?)
 		else if(numPlayers > MAX_PLAYERS)
 			numPlayers = MAX_PLAYERS;
 
 		// read coop parameter
 		isCoop = levelXML.hasAttribute("coop");
 		coopPoints = isCoop ? levelXML.getIntAttribute("coop") : 0;
 		if(isCoop) {
 			players[0].setCoopMode(true);
 			players[1].setCoopMode(true);
 		}
 
 		// read song
 		songName = levelXML.getChild(0).getAttribute("name");
 		songFile = levelXML.getChild(0).getAttribute("file");
 		songDuration = levelXML.getChild(0).getAttribute("duration");
 		tempo = levelXML.getChild(0).getIntAttribute("bpm");
 		multiplier = levelXML.getChild(0).getIntAttribute("multiplier");
 
 		// calculate step interval
 		stepInterval = (long) (60f / tempo * multiplier * 1000);
 		System.out.println("Step Interval: " + stepInterval);
 
 		// init the number of steps
 		// this makes it possible to do 1 player, but not 3
 		int[] numStepsEach = new int[numPlayers];
 		for(int i = 0; i < numPlayers; i++) {
 			player = levelXML.getChild(i + 1);
 			sequence = player.getChild(0);
 			numStepsEach[i] = sequence.countChildren();
 			if(numStepsEach[i] > MAX_STEPS)
 				numStepsEach[i] = MAX_STEPS;
 		}
 
 		// Now make sure that both players have the same number of steps.
 		// if not, we'll let the other player have free play.
 		numSteps = numStepsEach[0]; // stays this way if there is only one
 									// player.
 		if(numPlayers > 1) {
 			for(int i = 0; i < numPlayers; i++) {
 				// for now this will only go up to 2
 				// but let's keep the possibility of increasing the max number
 				// of players
 				if(numStepsEach[i] > numSteps)
 					numSteps = numStepsEach[i];
 			}
 		}
 
 		// save number of steps
 		stepCount = numSteps;
 
 		// read the steps
 		int readStepsUntil = 0;
 		if(numSteps > 0)
 			readStepsUntil = numSteps;
 		else
 			readStepsUntil = DEFAULT_STEPS;
 		for(int i = 0; i < numPlayers; i++) {
 
 			// init player steps
 			System.out.println("Sending Config for Step Interval " + stepInterval);
 			players[i].sendConfig((int) stepInterval);
 			players[i].initializeSteps(numSteps);
 			player = levelXML.getChild(i + 1);
 			sequence = player.getChild(0);
 			for(int j = 0; j < readStepsUntil; j++) {
 				if(j < numStepsEach[i]) {
 					step = sequence.getChild(j);
 					boolean pad1 = PApplet.parseBoolean(step.getIntAttribute("pad1"));
 					boolean pad2 = PApplet.parseBoolean(step.getIntAttribute("pad2"));
 					boolean pad3 = PApplet.parseBoolean(step.getIntAttribute("pad3"));
 					boolean pad4 = PApplet.parseBoolean(step.getIntAttribute("pad4"));
 					boolean free = PApplet.parseBoolean(step.getIntAttribute("free"));
 					players[i].addStep(new Step(pad1, pad2, pad3, pad4, free), j);
 					
 				} else {
 					// give them free play. All lights on.
 					players[i].addStep(new Step(true, true, true, true, true), j);
 				}
 			}
 		}
 
 		sounds.loadSong(songFile + ".mp3");
 
 		// success!
 		System.out.println("Successfully read the level file.");
 		successfullyRead = 1;
 		return;
 	}
 
 	public void xBeeEvent(XBeeReader xbee) {
 
 		// XBeeDataFrame data = xbee.getXBeeReading();
 
 		// if(data.getApiID() == XBeeReader.SERIES1_RX16PACKET) {
 		// int[] packet = data.getBytes();
 		// if(packet.length == XPan.PROX_IN_PACKET_LENGTH && packet[0] ==
 		// XPan.PROX_IN_PACKET_TYPE) {
 		// // println("prox message received");
 		// int patch = (packet[1] >> 1);
 		// int player = getPlayerIndexForPatch(patch);
 
 		// if(player != -1) {
 		// boolean touched = (packet[1] & 1) == 1;
 		// // println(packet[1]);
 		// // println(touched);
 		// int step = ((packet[2] & 0xFF) << 8) | (packet[3] & 0xFF);
 		// // println(step);
 		// int proximity = ((packet[4] & 0xFF) << 8) | (packet[5] & 0xFF);
 		// ;
 		// // println(proximity);
 		// // player.processProxReading(patch, step, touched,
 		// // proximity);
 		// processProxReading(new ProxData(player, patch, step, touched,
 		// proximity));
 		// } else
 		// System.err.println("Trouble in paradise, we received a packet from patch '"
 		// + patch
 		// + "', which is not assigned to a player");
 		// } else if(packet.length == XPan.CONFIG_ACK_LENGTH && packet[0] ==
 		// XPan.CONFIG_ACK_PACKET_TYPE) {
 		// int myTurnLength = ((packet[2] & 0xFF) << 8) | (packet[3] & 0xFF);
 		// int patch = packet[1];
 		// int player = getPlayerIndexForPatch(patch);
 		// players[player].processConfigAck(patch, myTurnLength);
 		// System.out.println("Config Ack Received in Level, Turn Length is " +
 		// myTurnLength);
 		// } else if(packet.length == XPan.VIBE_IN_PACKET_LENGTH && packet[0]
 		// == XPan.VIBE_IN_PACKET_TYPE) {
 		// if(packet[2] == 4)
 		// doPause();
 		// } else {
 		// System.err.println("Level received a bad packet.");
 		// }
 		// }
 	}
 
 	// TODO make this work with patch detection in LevelSelect
 	public Player getPlayerForPatch(int patch) {
 		if(patch >= 1 && patch <= 4)
 			return players[0];
 		else if(patch >= 9 && patch <= 16)
 			return players[1];
 		else
 			return null;
 	}
 
 	public int getPlayerIndexForPatch(int patch) {
 		if(patch >= 1 && patch <= 4)
 			return 0;
 		else if(patch >= 9 && patch <= 16)
 			return 1;
 		else
 			return -1;
 	}
 
 	public void processProxReading(ProxData data) {
 		// check if we are in coop mode
 		if(isCoop && (coopPoints == 0 || getTotalPoints() < coopPoints * 2)) {
 			for(int i = 0; i < players.length; i++)
 				players[i].processProxReading(data.patch, data.step, data.touched, data.proximity);
 			// println("Proximity reading: " + data + " (coop)");
 		}
 		// if not only need to process for incoming player
 		else {
 			players[data.player].processProxReading(data.patch, data.step, data.touched, data.proximity);
 			// println("Proximity reading: " + data);
 		}
 	}
 
 }
