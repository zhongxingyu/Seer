 package game;
 import java.util.*;
 import java.io.*;
 
 
 /**
  * This is the model for the game. It represents the entire state
  * of the game at the present moment. 
  * In this version, the player is presented with two kinds of fish
  * (good and bad) that come from sides (left or right) and have 
  * certain visual and auditory cues. The player tries to identify the
  * fish using the left or right hand pressing the appropriate key.
  * The system logs each keypress storing info about the press 
  * and the reaction time. If no fish was present it was a miscue
  * and we store the "reaction time" as the time since the last fish appeared
  * The fish can be generated either by reading data from a file or
  * they can be generated randomly with respect to some specification. 
  * @author tim
  *
  */
 public class GameModel {
 	
 	/**
 	 * The gameSpec is a set of property/value pairs that describes
 	 * everything about the current game aesthetics and mechanics.
 	 */
 	public GameSpec gameSpec,lastGameSpec;
 	
 	// this class is too complex, we need to clean it up and refactor!!
 	// REFACTOR -- do we need all three of these?
 	public double width;
 	public double height;
 	public double size;
 	
 	
 	// currently we only ever have one actor at a time ...
 	public List<GameActor> actors = new ArrayList<GameActor>();
 	
 	// we need this when spawning fish ...
 	protected Random rand = new Random();
 	
     // this is set to true when the session is over ...
 	// we need to add a timer to end the session, or have the 
 	// experimenter end the session ...
 	public boolean gameOver = false;
 	
 	// do we really want to pause ... and does this really pause??
 	public boolean paused = true;
 	
 	
 	// we should be more clear about these ...
 	private long startTime=System.nanoTime();
 	private long nextFishTime=0;
 	private GameActor nextFish=null;
 	private long gameStart = startTime;
 	
 	
 	public int score;
 	// this is a temporary variable, we'll be reading from a file eventually
 	//public String typescript = "lb 100 lb 100 rg 100 rb 100 lb 100"; 
 	
 	// this is true if we are reading from a script
 	public String inputScriptFileName;
 	
 	// this is a scanner used to read the fish creation info
 	public Scanner scan; // = new Scanner(typescript);
 	
 	// this is where the log will be written...
 	public BufferedWriter logfile;
 
 
 
 	public boolean stereo = true;
 
 	/*
 	public String goodFishSounds = "sounds/fish6hz0p";
 	public String badFishSounds = "sounds/fish8hz0p";
 	*/
 	// store the min/max visual scaling factors in percent
 	public int goodvisualhz=6;
 	public int badvisualhz=8;
 	public int visualMin = 100;
 	public int visualMax = 125;
 	
 	/** these variables record good/bad hits */
 	public int 
 	  hits,misses,
       noKeyPress,
       pressWithNoFish;
 
 	
 	public GameModel(double size, int numActors, GameSpec gameSpec) { 
 		this.width =size;
 		this.height = size;
 		this.size=size;
 		this.gameSpec = gameSpec;
 		
 		this.gameOver = false;
 
 		long now = System.currentTimeMillis();
 		String logname = "logs/log"+now+".txt";
 		
 		// open the logfile
 		try {
 			this.logfile = new BufferedWriter(new FileWriter(new File(logname)));
 		} catch (IOException e) {
 			System.out.println("Problems opening logfile:" + logname+": "+e);
 			e.printStackTrace();
 		}
 		
 	}
 
 	
 	private long lastEventTime = System.nanoTime();
 	
 	
 	/**
 	 * this reads the next line in the Script file 
 	 * property/value lines cause the system to update the GameSpec
 	 * fish launches read the interval and use it to compute nextFishTime
 	 * and read the species and side and store it in this.nextFish
 	 * 
 	 * We should read this right after launching a fish, so we can know
 	 * when its time to launch the next fish!
 	 * @return
 	 */
 	private long updateNextFishTime() {
 		//initialize the scanner if its the first time we're reading a line
 		if (scan==null){
 			try {
 				scan = new Scanner(new File(this.inputScriptFileName));
 			} catch (FileNotFoundException e) {
 				System.out.println("Error in reading inputScriptFile:"+this.inputScriptFileName+" "+e);
 				e.printStackTrace();
 				this.stop();
 				return 0;
 			}
 		}
         if (! scan.hasNext()){
         	this.gameOver = true;
         	return this.nextFishTime + 10*1000000000L;
         }
 		long interval = scan.nextLong();
 		
 		// process all the 0 interval commands (which set game properties)
 		while (interval == 0){
 			String prop = scan.next();
 			String value = scan.next();
 			writeToLog("0\t"+prop+"\t"+value);
 			if (prop.equals("gameover")) {
 				this.stop();
 				this.gameOver=true;
 				return 1000000000000L;
 			}
			//System.out.println("interval="+interval+" prop="+prop+" value="+value);
 			interval = scan.nextLong();
 			gameSpec.update(prop,value);
 		}
 		long tmpNFT = this.nextFishTime - this.gameStart;
 		// calculate the next FishTime and the basic characteristics of the nextFish (species and side)
 		this.nextFishTime = interval*1000000 + this.nextFishTime;
 		String species = scan.next();
 		String side = scan.next();
 		int fishNum = scan.nextInt();
		//System.out.println("update:"+tmpNFT/1000000+" "+ interval+" "+
		//(this.nextFishTime-this.gameStart)/1000000);
 		
 		// write the launch event to the log
 
 		long now = (System.nanoTime()-this.gameStart)/1000000;
 		String logLine = ""+now+"\tlaunch\t"+species+"\t"+side+"\t"+fishNum;
		//System.out.println(logLine+" "+now+" "+interval);
 
 		writeToLog(logLine);
 		
 		//create the next Fish to be launched
 		GameActor a = new GameActor();
 		a.fromLeft = side.equals("left");
 		a.species = (species.equals("good"))?Species.good:Species.bad;
 		this.nextFish = a;
 		return nextFishTime;
 		
 	}
 	
 	
 	/**
 	 * This writes a string to the log file and prefixes it with the
 	 * number of milliseconds since the beginning of the session.
 	 * @param s the string to be written to the log file
 	 */
 	public void writeToLog(String s){
 		try{
 			long theTime = (System.nanoTime()-this.gameStart);
 			long theInterval = theTime - lastLogEventTimeNano;
 			lastLogEventTimeNano = theTime;
 			int theSeconds = (int) Math.round(theInterval/1000000.0);
 			this.logfile.write(theSeconds +" "+s+"\n");
			System.out.println("log:"+ theSeconds +" "+s+"\n");
 		} catch(Exception e){
 			System.out.println("Error writing to log "+e);
 		}
 	}
 	
 	public void writeToLog(GameEvent e){
 		try{
 			long theTime = (System.nanoTime()-this.gameStart);
 			long theInterval = theTime - lastLogEventTimeNano;
 			lastLogEventTimeNano = theTime;
 			// this is the time in milliseconds since the last event
 			int msInterval = (int) Math.round((theInterval/1000000.0));
 			this.logfile.write(msInterval+GameEvent.sep+e+"\n");
			System.out.println("log:"+ msInterval+GameEvent.sep+e+"\n");
 		} catch(Exception err){
 			System.out.println("Error writing GameEvent to log "+err);
 		}
 	}
 
 	private long lastLogEventTimeNano = 0;
 	
 
 
 
 	
 	
 	
 	
 	// spawn an actor randomly
 	/**
 	 * randomly spawns a new fish,based on the script
 	 * and resets the time for the next fish to be spawned...
 	 */
 	public void spawnFish(){
 		if (this.gameOver) return;	
 		
 		Side side = (this.nextFish.fromLeft)?Side.left:Side.right;
 		Species s = this.nextFish.species;
		//System.out.println("spawning "+s+" "+side);
 		
 		// pick starting location and velocity
 		double y = this.height/2;
 		double x = (side==Side.left)? 1 : this.width-1;
 		double vy=0;
 		double vx = (side==Side.left)? 1: -1;
 		
 		// then make an actor with that position
 		GameActor a = new GameActor(x,y,true,s,gameSpec.stereo,gameSpec.good.soundFile,gameSpec.bad.soundFile);
 		// and fill in all the needed fields...
 		// we don't need both fromLeft and origin .... eliminate fromLeft...
 		a.fromLeft=(side==Side.left);
 		a.origin=(side==Side.left)?0:1;  // we'll convert origin to Side later
 		//a.radius=4;
 		// start playing the music for the fish
 		if (a.fromLeft) a.ct = a.ctL; else a.ct = a.ctR;
 		a.ct.loop();
 
 		
 		// add the fish to the list of actors...
 		this.actors.add(a);
 	}
 	
 	public void start(){
 		paused = false;
 		this.nextFishTime = System.nanoTime();
 		this.gameStart = nextFishTime;
 		this.nextFishTime = updateNextFishTime(); 
 		spawnFish();
 		
 	}
 	
 	public void stop(){
 		paused = true;
 		gameOver=true;
 		java.util.Iterator<GameActor> iter =this.actors.iterator();
 		while (iter.hasNext()){
 			GameActor a = (GameActor) iter.next();
 			a.ct.stop();
 		}
 		this.actors.clear();
 		try{
 			if (logfile != null) logfile.close();
 			System.out.println("closing log/script files");
 		}catch (Exception e){
 			System.out.println("Problem closing logfile");
 		}
 
 	}
 	
 	/**
 	 * if an actor moves off the board, in the x (or y) direction, 
 	 * it is bounced back into the board and its velocity in the
 	 * offending direction is reversed
 	 * @param a
 	 */
 	public void keepOnBoard(GameActor a){
 		if (a.x<0) {
 			a.x = -a.x;a.vx = -a.vx;
 		}else if (a.x> width){
 			a.x = width - (a.x-width);
 			a.vx = -a.vx;
 		}
 		if (a.y<0) {
 			a.y = -a.y;a.vy = -a.vy;
 		}else if (a.y > height){
 			a.y = height - (a.y-height);
 			a.vy=-a.vy;
 		}
 	}
 	
 	// here is where we take the fish off the board
 	// when the user presses a key!
 	// we assume there is only one fish in the actors list ..
 	// this is a hack we should clean up or rename...
 	public void removeFish(GameActor a){
 		this.actors.clear();
 	}
 	/**
 	 * update moves all actors one step 
 	 * update will check if the difference between the lastUpdate 
 	 * and the current time is greater than the sRate plus a random number 
 	 * from 1 to 4, and spawn a fish if so.
 	 */
 	public void update() { //throws UnsupportedAudioFileException, IOException, LineUnavailableException {
 		if (paused || gameOver) return;
 		
 		// here is where we decide whether to spawn a fish
 		// I'm changing this to not use an input script ...
 		// but instead to randomly generate new fish
 		// using a nextFishTime variable
 		
 		// we need to write the code to generate a script from this session
 		// so we can randomly generate one script and then use it many times...
 		
 		long now=System.nanoTime();
 		
 		if (now > this.nextFishTime){
 			// time to launch the next fish!
			//System.out.println("newfish "+(now-this.gameStart)/1000000 + " "+
			//  (this.nextFishTime-this.gameStart)/1000000);
 			
 			this.nextFishTime = this.updateNextFishTime();
 			if (this.gameOver) return;
 			
 			
 			if (this.actors.size()>0) {
 				// this is the case where we didn't press a key to kill or eat the fish
 				this.noKeyPress++;
 				GameActor lastFish = this.actors.get(this.actors.size()-1);
 				lastFish.ct.stop();
 
 				this.actors.clear();
 
 				
 				this.writeToLog(new GameEvent(lastFish));
 
 			}
 			
 			// we now spawn the next fish
 			spawnFish();
 			this.lastEventTime = System.nanoTime();
 	
 		}
 		
 		// Finally, we update all of the fish (should only be one now!)
 		java.util.Iterator<GameActor> iter =this.actors.iterator();
 		while (iter.hasNext()){
 			GameActor a = (GameActor) iter.next();
 			a.update();
 			keepOnBoard(a);
 		}
 
 	}
 
 }
 
