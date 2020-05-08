 package game;
 
 public class Warehouse {
 	//characters
 	public static final int CAPTAIN = 0;
 	public static final int LIZARD = 1;
 	public static final int SLIME = 2;
 	public static final int MARINE = 3;
 	public static final int ROBOT = 4;
 	public static final int CHAR_NUM = 5; //number of implemented players
 	public static final int SCIENTIST = 5;
 
 	//bullets
 	public static final int RAYGUN = 0;
 	public static final int FIREBALL = 1;
 	public static final int BUBBLE = 2;
 	public static final int MORTAR = 3;
 	public static final int BEAMSWORD = 4;
 	public static final int BOOMERANG = 5;
 	public static final int MISSILE = 6;
 	public static final int EXPLOSION = 7;
 	public static final int LAVABALL = 8;
 	public static final int METEOR = 9;
 	public static final int LAVAWAVE = 10;
 	public static final int LAVAWARN = 11;
 
 	//levels
 	public static final int HOLODECK = 0;
 	public static final int PLANET = 1;
 	public static final int FACTORY = 2;
 	public static final int DEMO = -1; //dummied out but data still exists for now
 
 	//dimensions for easy level building
 	private static final int WIDTH = 640, HEIGHT = 480;
 
 	//actual lists
 	private static RoleModel characters[] = 
 		{captain(), lizardman(), slime(), spaceMarine(), japaneseRobot(), madScientist()};
 	private static ShotModel shots[] =
 		{raygun(), fireball(), bubble(), mortar(), beamSword(), boomerang(), missile(), explosion(),
 		lavaball(), meteor(), lavaWave(), lavaWarning()};
 	private static Blueprint maps[] = 
 		{holodeck(), alienPlanet(), factory()};
 
 	/**
 	 * @return Array of selectable characters
 	 */
 	public static RoleModel[] getCharacters() {
 		return characters;
 	}
 
 	/**
 	 * @return Array of shot types
 	 */
 	public static ShotModel[] getShots() {
 		return shots;
 	}
 
 	/**
 	 * @return Array of selectable maps
 	 */
 	public static Blueprint[] getMaps() {
 		return maps;
 	}
 
 	/*private method to init each character*/
 	//lizard man
 	private static RoleModel lizardman() { //TODO: Make useful movement
 		RoleModel r = new RoleModel();
 		r.setSkin(LIZARD);
 
 		//agile on ground, sluggish in air
 		r.setRunFrict(.3);
 		r.setAirFrict(.045);
 		r.setMaxSpeed(3.9);
 
 		//powerful jump
 		r.setJumpPower(8.6);
 		r.setJumpHold(4);
 
 		//standard gravity
 		r.setGrav(.65);
 		r.setTermVel(7);
 
 		//great wall slide
 		r.setWallTermVel(1);
 		r.setSink(1.3);
 
 		//slightly quicker respawn
 		r.setSpawnTime(45);
 		r.setSpawnInv(50);
 
 		//bouncing fireballs
 		r.setShotType(fireball());
 		return r;
 	}
 	//slime alien
 	private static RoleModel slime() {
 		RoleModel r = new RoleModel();
 		r.setSkin(SLIME);
 
 		//always slippery, equal air and ground
 		r.setRunFrict(.1);
 		r.setAirFrict(.1);
 		r.setMaxSpeed(3);
 
 		//floaty jump
 		r.setJumpPower(3.8);
 		r.setJumpHold(25);
 
 		//slow fall
 		r.setGrav(0.25);
 		r.setTermVel(3.5);
 
 		//good fall control
 		r.setWallTermVel(1.5);
 		r.setSink(1.6);
 
 		//slightly faster respawn
 		r.setSpawnTime(45);
 		r.setSpawnInv(50);
 
 		//slow bubbles
 		r.setShotType(bubble());
 		return r;
 	}
 	//dashing captain
 	private static RoleModel captain() { //TODO: Make thoroughly average
 		RoleModel r = new RoleModel();
 		r.setSkin(CAPTAIN);
 
 		//average land speed, a bit weaker in the air
 		r.setRunFrict(.2);
 		r.setAirFrict(.05);
 		r.setMaxSpeed(3);
 
 		//middling jump
 		r.setJumpPower(8.0);
 		r.setJumpHold(6);
 
 		//average fall
 		r.setGrav(.62);
 		r.setTermVel(7);
 
 		//weak fall control
 		r.setWallTermVel(5.8);
 		r.setSink(1.15);
 
 		//quick respawn, long spawn armor
 		r.setSpawnTime(45);
 		r.setSpawnInv(100);
 
 		//basic raygun
 		r.setShotType(raygun());
 		return r;
 	}
 	//Big chufty marine
 	private static RoleModel spaceMarine() {
 		RoleModel r = new RoleModel();
 		r.setSkin(MARINE);
 
 		//average land speed, a bit weaker in the air
 		r.setRunFrict(.2);
 		r.setAirFrict(.15);
 		r.setMaxSpeed(2.15);
 
 		//jetpack jump
 		r.setJumpPower(2.2);
 		r.setJumpHold(70);
 
 		//hard fall
 		r.setGrav(0.6);
 		r.setTermVel(7.5);
 
 		//not bad fall control
 		r.setWallTermVel(7);
 		r.setSink(1.4);
 
 		//slow respawn, average armor
 		r.setSpawnTime(65);
 		r.setSpawnInv(50);
 
 		//mortar shots
 		r.setShotType(mortar());
 		return r;
 	}
 	//Samurai robot
 	private static RoleModel japaneseRobot() {
 		RoleModel r = new RoleModel();
 		r.setSkin(ROBOT);
 
 		//high speed very slippery
 		r.setRunFrict(.04);
 		r.setAirFrict(.03);
 		r.setMaxSpeed(5.4);
 
 		//short hops with potential for higher
 		r.setJumpPower(6.1);
 		r.setJumpHold(10);
 
 		//slightly brisk fall
 		r.setGrav(.85);
 		r.setTermVel(7);
 
 		//wallrun
 		r.setWallTermVel(-3);
 		r.setSink(1.3);
 
 		//average respawn, slightly short armor
 		r.setSpawnTime(50);
 		r.setSpawnInv(40);
 
 		//quick beam slashes
 		r.setShotType(beamSword());
 		return r;
 	}
 	//mad scientist
 	private static RoleModel madScientist() { //TODO: Make a unique movement style
 		RoleModel r = new RoleModel();
 		r.setSkin(SCIENTIST);
 
 		//movement...?
 		r.setRunFrict(.04);
 		r.setAirFrict(.025);
 		r.setMaxSpeed(4.75);
 
 		//average jump?
 		r.setJumpPower(6.8);
 		r.setJumpHold(4);
 
 		//slow fall?
 		r.setGrav(0.55);
 		r.setTermVel(3.8);
 
 		//ok fall control?
 		r.setWallTermVel(3.8);
 		r.setSink(1.3);
 
 		//slightly longer spawn armor
 		r.setSpawnTime(50);
 		r.setSpawnInv(55);
 
 		//cartoony missiles
 		r.setShotType(missile());
 		return r;
 	}
 
 	/*private methods to init each shot type*/
 	//bouncing, mario-type fireballs
 	private static ShotModel fireball() {
 		ShotModel s = new ShotModel();
 		s.setSkin(FIREBALL);
 
 		//quick firing
 		s.setReload(47);
 
 		//small shots
 		s.setH(8);
 		s.setW(8);
 
 		//long range, mid speed
 		s.setLife(400);
		s.setSpeed(3);
		s.setVSpeed(3);
 
 		//bounce across the ground
 		s.setType(Shot.GRAV+Shot.BOUNCE);
		s.setVar(30);
 		return s;
 	}
 	//slow, lingering bubbles
 	private static ShotModel bubble() {
 		ShotModel s = new ShotModel();
 		s.setSkin(BUBBLE);
 
 		//average firing
 		s.setReload(55);
 
 		//large bubbles
 		s.setH(15);
 		s.setW(15);
 
 		//slow-moving, long lasting
 		s.setLife(393);
 		s.setSpeed(1);
 
 		//bouncy bubbles slow down
 		s.setType(Shot.BOUNCE+Shot.ACCEL+Shot.WEAK);
 		s.setVar(-2);
 		return s;
 	}
 	//straight, simple lasers
 	private static ShotModel raygun() {
 		ShotModel s = new ShotModel();
 		s.setSkin(RAYGUN);
 
 		//fast fire
 		s.setReload(43);
 
 		//small lasers
 		s.setH(2);
 		s.setW(20);
 
 		//mid speed long range
 		s.setLife(68);
 		s.setSpeed(4.2);
 
 		//standard type
 		s.setType(0);
 		return s;
 	}
 	//currently just a cannon
 	private static ShotModel mortar() {
 		ShotModel s = new ShotModel();
 		s.setSkin(MORTAR);
 
 		//slow speed
 		s.setReload(65);
 
 		//mid-size shots
 		s.setH(10);
 		s.setW(10);
 
 		//shoot forward
 		s.setLife(100);
 		s.setSpeed(6.5);
 		s.setVSpeed(-3);
 
 		//standard type
 		s.setType(Shot.GRAV|Shot.BOMB);
 		s.setVar(35);
 		return s;
 	}
 	//mid-range blade
 	private static ShotModel beamSword() {
 		ShotModel s = new ShotModel();
 		s.setSkin(BEAMSWORD);
 
 		//super quick
 		s.setReload(30);
 
 		//long blades
 		s.setH(4);
 		s.setW(20);
 
 		//short range
 		s.setLife(12);
 		s.setSpeed(9.5);
 
 		//stop quickly
 		s.setType(Shot.ACCEL+Shot.MOMENT+Shot.SHIELD);
 		s.setVar(-850);
 		return s;
 	}
 	//mid-range blade
 	private static ShotModel boomerang() {
 		ShotModel s = new ShotModel();
 		s.setSkin(BOOMERANG);
 
 		//quick fire
 		s.setReload(40);
 
 		//long blades
 		s.setH(4);
 		s.setW(16);
 
 		//short range
 		s.setLife(52);
 		s.setSpeed(9);
 
 		//go out then return
 		s.setType(Shot.ACCEL);
 		s.setVar(-400);
 		return s;
 	}
 	//acceleration missiles
 	private static ShotModel missile() {
 		ShotModel s = new ShotModel();
 		s.setSkin(MISSILE);
 
 		//mid speed
 		s.setReload(65);
 
 		//mid-size shots
 		s.setH(12);
 		s.setW(12);
 
 		//shoot slightly backward
 		s.setLife(150);
 		s.setSpeed(-4);
 
 		//accelerate quickly forward and explode on impact
 		s.setType(Shot.ACCEL|Shot.BOMB);
 		s.setVar(210);
 		return s;
 	}
 	//explosions for various uses
 	private static ShotModel explosion() {
 		ShotModel s = new ShotModel();
 		s.setSkin(EXPLOSION);
 
 		//slow speed
 		s.setReload(75);
 
 		//start small
 		s.setH(4);
 		s.setW(4);
 
 		//unmoving
 		s.setLife(7);
 		s.setSpeed(0);
 
 		//ignore walls and players and get bigger
 		s.setType(Shot.GROW|Shot.PHASE|Shot.PIERCE);
 		s.setVar(4);
 		return s;
 	}
 	//jumping lava fireballs
 	private static ShotModel lavaball() {
 		ShotModel s = new ShotModel();
 		s.setSkin(FIREBALL);
 
 		//mid-speed firing
 		s.setReload(80);
 
 		//small shots
 		s.setH(8);
 		s.setW(8);
 
 		//short hops up
 		s.setLife(30);
 		s.setSpeed(0);
 		s.setVSpeed(-5);
 
 		//bounce across the ground
 		s.setType(Shot.GRAV|Shot.PHASE);
 		s.setVar(35);
 		return s;
 	}
 	private static ShotModel meteor() {
 		ShotModel s = new ShotModel();
 		s.setSkin(METEOR);
 
 		//slow firing
 		s.setReload(250);
 
 		//huge shots
 		s.setH(32);
 		s.setW(32);
 
 		//drops straight
 		s.setLife(200);
 		s.setSpeed(0);
 		s.setVSpeed(4);
 
 		//fall straight blow up
 		s.setType(Shot.BOMB);
 		s.setVar(0);
 		return s;
 	}
 	private static ShotModel lavaWave() {
 		ShotModel s = new ShotModel();
 		s.setSkin(LAVAWAVE);
 
 		//very infrequent
 		s.setReload(25*50);
 
 		//screen-covering
 		s.setH(240);
 		s.setW(640);
 
 		//drops straight
 		s.setLife(500);
 		s.setSpeed(0);
 		//s.setVSpeed(-10);
 		s.setVSpeed(-12);
 
 		//slowly rise, then fall
 		s.setType(Shot.GRAV+Shot.PIERCE+Shot.PHASE);
 		s.setVar(6);
 		return s;
 	}
 	private static ShotModel lavaWarning() {
 		ShotModel s = new ShotModel();
 		s.setSkin(LAVAWAVE);
 
 		//very infrequent
 		s.setReload(25*50);
 
 		//screen-covering
 		s.setH(240);
 		s.setW(640);
 
 		//drops straight
 		s.setLife(300);
 		s.setSpeed(0);
 		s.setVSpeed(-10);
 
 		//slowly rise, then fall
 		s.setType(Shot.GRAV+Shot.PIERCE+Shot.PHASE);
 		s.setVar(12);
 		return s;
 	}
 
 	/*private methods to init each map*/
 	/**
 	 * TODO: Make some levels, baby!
 	 * 
 	 * How to make a level:
 	 * 
 	 * b is the map itself. Call b.setId, and tell it its position in the array of maps.
 	 * 
 	 * Call b.setName and give it some name. These may be used internally.
 	 * 
 	 * Call b.add to add a terrain object. First specify the x and y of the upper left corner,
 	 * then give it a width and height (positive values), then create a bitmask of traits it has.
 	 * 
 	 * To create a bitmask, simply sum(+) or OR(|) together some of the flags below as one argument.
 	 * 
 	 * To specify a Var (used by some flags), simply add one more argument afterward with the value for Var.
 	 * 
 	 * Lastly, use b.setSpawn(i, x, y) to specify 4 spawn points on the level (0-3). i tells which point you're setting.
 	 * 
 	 * Eventually, bg, bgm, and skins will matter, but not yet.
 	 * Remember: The player wraps around the level, but only when he is completely offscreen.
 	 * The level is 640 x 480, so extend around ~16 pixels beyond that to make sure.
 	 */
 
 	public static final int SOLID = 1; //solid all around (includes top)
 	public static final int PLATFORM = 2; //solid top
 	public static final int DANGER = 4; //kill on contact
 	public static final int BOUNCE = 8; //bouncy (var = bounciness mod, 0 = 100%, 5 = 150%, -2 = 80%, etc.)
 	public static final int SLIP = 16; //slippery
 	public static final int MOVE = 32; //moves you (var = speed*10)
 	public static final int HATCH = 64; //appears when control is on
 	public static final int NHATCH = 128; //appears when controls is off
 	public static final int SWITCH = 256; //toggles control when hit
 	public static final int PIPE = 512; //slide in, slide out
 	public static final int WARP = 1024; //for in-game level select (var = target level)
 	public static final int CHAR = 2048; //change characters (var = target character)
 	public static final int OPTION = 4096; //for in game options (var = various things)
 	public static final int GUN = 8192; //emits shots (var = bullet to emit)
 	public static final int COLOR = 16384; //painted a different color (temp)
 	public static final int PSPAWN = 32768; //capable of spawning powerups
 
 	//level select / menu world
 	private static Blueprint holodeck() {
 		Blueprint b = new Blueprint();
 		b.setId(HOLODECK);
 		b.setName("Level select");
 
 		//character change chambers
 		b.add(-1*16, -1*16, 3*16, 6*16, SOLID);
 		b.add(2*16, -1*16, 3*16, 3*16, BOUNCE|CHAR, LIZARD);
 		b.add(2*16, -1*16, 3*16, 5*16, PIPE|SOLID|COLOR, LIZARD);
 		b.add(5*16, -1*16, 4*16, 6*16, SOLID);
 		b.add(9*16, -1*16, 3*16, 3*16, BOUNCE|CHAR, SLIME);
 		b.add(9*16, -1*16, 3*16, 5*16, PIPE|SOLID|COLOR, SLIME);
 		b.add(12*16, -1*16, 4*16, 6*16, SOLID);
 		b.add(16*16, -1*16, 3*16, 3*16, BOUNCE|CHAR, MARINE);
 		b.add(16*16, -1*16, 3*16, 5*16, PIPE|SOLID|COLOR, MARINE);
 		b.add(19*16, -1*16, 4*16, 6*16, SOLID);
 		b.add(23*16, -1*16, 3*16, 3*16, BOUNCE|CHAR, ROBOT);
 		b.add(23*16, -1*16, 3*16, 5*16, PIPE|SOLID|COLOR, ROBOT);
 		b.add(26*16, -1*16, 8*16, 6*16, SOLID);
 		b.add(34*16, -1*16, 3*16, 3*16, BOUNCE|CHAR, CAPTAIN);
 		b.add(34*16, -1*16, 3*16, 5*16, PIPE|SOLID|COLOR, CAPTAIN);
 		b.add(37*16, -1*16, 4*16, 6*16, SOLID);
 
 		//top/'attic' ladders
 		b.add(2*16, 8*16, 3*16, 16, PLATFORM);
 		b.add(9*16, 8*16, 3*16, 16, PLATFORM);
 		b.add(16*16, 8*16, 3*16, 16, PLATFORM);
 		b.add(23*16, 8*16, 3*16, 16, PLATFORM);
 		b.add(34*16, 8*16, 3*16, 16, PLATFORM);
 
 		//top floor
 		b.add(-1*16, 11*16, 18*16, 2*16, SOLID);
 		b.add(17*16, 11*16, 6*16, 16, PLATFORM);
 		b.add(23*16, 11*16, 18*16, 2*16, SOLID);
 
 		//middle/top ladder
 		b.add(18*16, 15*16, 4*16, 16, PLATFORM);
 
 		//option change blocks
 		b.add(11*16, 14*16, 2*16, 2*16, SOLID|BOUNCE|OPTION|HATCH|SWITCH, 0); //stock
 		b.add(8*16, 14*16, 2*16, 2*16, SOLID|BOUNCE|OPTION|HATCH, 1); //stock adjust
 		b.add(27*16, 14*16, 2*16, 2*16, SOLID|BOUNCE|OPTION|NHATCH|SWITCH, 0); //time
 		b.add(30*16, 14*16, 2*16, 2*16, SOLID|BOUNCE|OPTION|NHATCH, 1); //time adjust
 
 		//middle floor
 		b.add(-1*16, 19*16, 7*16, 16, PLATFORM);
 		b.add(6*16, 19*16, 28*16, 2*16, SOLID);
 		b.add(34*16, 19*16, 7*16, 16, PLATFORM);
 
 		//bottom/middle ladders
 		b.add(1*16, 23*16, 4*16, 16, PLATFORM);
 		b.add(35*16, 23*16, 4*16, 16, PLATFORM);
 
 		//base floor + warps
 		b.add(-1*16, 27*16, 13*16, 4*16, SOLID);
 		b.add(12*16, 27*16, 4*16, 1*16, PLATFORM);
 		//b.add(12*16, 30*16, 4*16, 1*16, BOUNCE|WARP, DEMO);
 		b.add(12*16, 30*16, 4*16, 1*16, BOUNCE|WARP, PLANET);
 		b.add(12*16, 27*16+1, 4*16, 3*16, PIPE|COLOR, CAPTAIN); //planet portal
 		b.add(16*16, 27*16, 8*16, 4*16, SOLID);
 		b.add(24*16, 27*16, 4*16, 1*16, PLATFORM);
 		b.add(24*16, 30*16, 4*16, 1*16, BOUNCE|WARP, FACTORY);
 		b.add(24*16, 27*16+1, 4*16, 3*16, PIPE|COLOR, MARINE); //factory portal
 		b.add(28*16, 27*16, 13*16, 4*16, SOLID);
 
 		//add spawn points
 		b.setSpawn(0, 68+0*WIDTH/4, 9*16+8);
 		b.setSpawn(1, 68+1*WIDTH/4, 9*16+8);
 		b.setSpawn(2, 68+2*WIDTH/4, 9*16+8);
 		b.setSpawn(3, 68+3*WIDTH/4, 9*16+8);
 
 		return b;
 	}
 	//lava and sand surface world
 	private static Blueprint alienPlanet() {
 		Blueprint b = new Blueprint();
 		b.setId(PLANET);
 		b.setName("Alien Planet Surface");
 
 		//meteor shooter
 		b.add(1*16, -9*16, 38*16, 8*16, GUN|PSPAWN, METEOR);
 
 		//cave
 		b.add(-1*16, 16*16, 3*16, 15*16, SOLID);
 		b.add(38*16, 16*16, 3*16, 15*16, SOLID);
 		b.add(1*16, 27*16, 17*16, 4*16, SOLID);
 		b.add(22*16, 27*16, 17*16, 4*16, SOLID);
 		//cannon escape
 		b.add(18*16, 27*16, 4*16, 1*16, PLATFORM);
 		b.add(18*16, 29*16, 4*16, 1*16, BOUNCE, 180);
 		b.add(18*16, 27*16+1, 4*16, 4*16, PIPE|SOLID|COLOR, MARINE);
 
 		//surface
 		b.add(-1*16, 15*16, 15*16, 1*16, SOLID|MOVE, 8);
 		b.add(14*16, 15*16, 5*16, 1*16, PLATFORM|MOVE, 22);
 		b.add(21*16, 15*16, 5*16, 1*16, PLATFORM|MOVE, -22);
 		b.add(26*16, 15*16, 15*16, 1*16, SOLID|MOVE, -8);
 
 		//internal platforms
 		b.add(18*16, 21*16, 2*16, 1*16, PLATFORM|MOVE, -8);
 		b.add(20*16, 21*16, 2*16, 1*16, PLATFORM|MOVE, 8);
 		
 		b.add(36*16, 24*16, 2*16, 1*16, PLATFORM);
 		b.add(8*16, 22*16, 4*16, 1*16, PLATFORM);
 		
 		b.add(24*16, 18*16, 1*16, 1*16, PLATFORM);
 
 		//lava waves!
 		b.add(WIDTH/2, HEIGHT*7/2-64, 1, 1, GUN, LAVAWAVE);
 		b.add(WIDTH/2, HEIGHT*2+32, 1, 1, GUN, LAVAWARN);
 
 		//spawn points
 		b.setSpawn(0, 5*16, 14*16);
 		b.setSpawn(1, 10*16, 14*16);
 		b.setSpawn(2, 30*16, 14*16);
 		b.setSpawn(3, 35*16, 14*16);
 
 		return b;
 	}
 	//springs and conveyers machine world
 	private static Blueprint factory() {
 		Blueprint b = new Blueprint();
 		b.setId(FACTORY);
 		b.setName("Factory");
 
 		//TODO: build actual map (in a not-terrible way)
 		//floor+lower walls
 		b.add(5*16, 29*16, 35*16, 2*16, SOLID);
 		b.add(2*16, 29*16, 3*16, 2*16, PLATFORM);
 		b.add(2*16, 29*16+1, 3*16, 3*16, PIPE);
 		b.add(2*16, 29*16, 3*16, 2*16, COLOR, LIZARD);
 		b.add(-1*16, 16*16, 3*16, 14*16, SOLID);
 		b.add(38*16, 16*16, 3*16, 14*16, SOLID);
 		
 		//melter
 		b.add(29*16, 28*16, 1*16, 1*16, SOLID);
 		b.add(30*16, 28*16, 8*16, 1*16, SOLID|DANGER|GUN, LAVABALL);
 		
 		//upper walls+ceiling
 		b.add(-1*16, -1*16, 3*16, 14*16, SOLID);
 		b.add(38*16, 1*16, 3*16, 12*16, SOLID);
 		b.add(2*16, -1*16, 3*16, 2*16, PIPE);
 		b.add(2*16, -1*16, 3*16, 2*16, COLOR, LIZARD);
 		b.add(5*16, -1*16, 35*16, 2*16, SOLID);
 		
 		//middle wraparound
 		b.add(2*16, 16*16, 3*16, 2*16, SOLID);
 		b.add(5*16, 12*16, 3*16, 6*16, SOLID);
 		b.add(30*16, 16*16, 8*16, 2*16, SOLID);
 		
 		//top production
 		b.add(0*16, 5*16, 10*16, 2*16, SOLID|MOVE, 19);
 		b.add(1*16, 4*16, 1, 1, PSPAWN);
 		b.add(0*16, 3*16, 2*16, 2*16, COLOR, SLIME);
 		
 		//middle production
 		b.add(30*16, 20*16, 8*16, 2*16, SOLID|MOVE, -19);
 		b.add(37*16, 19*16, 1, 1, PSPAWN);
 		b.add(36*16, 18*16, 2*16, 2*16, SOLID|COLOR, SLIME);
 
 		//middle carry
 		b.add(10*16, 16*16, 10*16, 2*16, PLATFORM|MOVE, 19);
 		b.add(20*16, 24*16, 11*16, 2*16, PLATFORM|MOVE, 19);
 		
 		//spring lift
 		b.add(13*16, 25*16, 3*16, 1*16, PLATFORM|BOUNCE, 15);
 		
 		//spawn points
 		b.setSpawn(0, 2*16, 15*16);
 		b.setSpawn(1, 38*16, 15*16);
 		b.setSpawn(2, 10*16, 35*16);
 		b.setSpawn(3, 20*16, 15*16);
 
 		return b;
 	}
 	//test environment
 	private static Blueprint demo() {
 		Blueprint b = new Blueprint();
 		b.setId(DEMO);
 		b.setName("Test level");
 
 		//build various test platforms
 		b.add(WIDTH/4, HEIGHT*3/4, WIDTH/2, 48, SOLID|PSPAWN);
 		b.add(WIDTH/4, HEIGHT*3/4-40, WIDTH/8, 4, PLATFORM|MOVE|HATCH, 18);
 		b.add(WIDTH*7/16, HEIGHT*3/4-90, WIDTH/8, 24, BOUNCE|SOLID, 2);
 		b.add(WIDTH*5/8, HEIGHT*3/4-40, WIDTH/8, 4, PLATFORM|SLIP|NHATCH);
 		b.add(20, HEIGHT/2-50, 50, 50, SOLID|DANGER);
 		b.add(WIDTH-70, HEIGHT/2-50, 50, 50, SOLID|DANGER);
 
 		b.add(WIDTH/4+10, HEIGHT*3/4-160, 25, 25, SOLID|HATCH|SWITCH|BOUNCE);
 		b.add(WIDTH*3/4-35, HEIGHT*3/4-160, 25, 25, SOLID|NHATCH|SWITCH|BOUNCE);
 
 		b.add(WIDTH/2-10, HEIGHT/4, 20, 20, GUN, EXPLOSION);
 
 		//add spawn points
 		b.setSpawn(0, 150, 60);
 		b.setSpawn(1, 200, 50);
 		b.setSpawn(2, 250, 50);
 		b.setSpawn(3, 300, 60);
 
 		return b;
 	}
 }
