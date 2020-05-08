 //package wars.dragon.engine;
 
 import java.util.*;
 import java.io.*;
 
 public class GameState {
     
     Map map;
    Logic logic;
     List<Player> players = new ArrayList<Player>();
     Integer turns = 0;
 
 
     public static void main(String[] argv) {
 	if (argv.length == 1) {
 	    Map m = MapReader.readMap(GameState.readFile(argv[0]));
	    Logic l = new Logic();
	    GameState game = new GameState(m, l);
 	    game.play();
 	}
 	else {
 	    System.err.println("USAGE: java Game <mapName>");
 	    System.exit(1);
 	}	    
     }
 
     public GameState(Map map, Logic logic) {
 	this.map = map;
 	this.logic = logic;
 
 	// Test data
 	this.players = new ArrayList<Player>(2);
 	players.add(new Player("Shana"));
 	players.add(new Player("Yukari"));
     }
     
     private static void printMap(Map m) {
 	    System.out.println(m);
 	    System.out.println(m.dumpMobMap());
     }
     
     private static List<String> readFile(String filename) {
 	List<String> text = new ArrayList<String>();
 	
 	try {
 	    BufferedReader in = new BufferedReader(new FileReader(filename));
 	    String line;
 	    while ((line = in.readLine()) != null) 
 		text.add(line);
 	    in.close();
 	}
 	catch (FileNotFoundException fnf) {
 	    System.err.println("Couldn't find " + fnf.getMessage());
 	    System.exit(1);
 	}
 	catch (IOException ioe) {
 	    System.err.println("Couldn't read " + ioe.getMessage());
 	    System.exit(1);
 	}
 	return text;
     }    
 
 
     public void play() {
 	Position p = new Position(0, 0);
 	Dragon d = new Dragon();
 	d.setPosition(p);
 	map.getField(p).setUnit(d);
 	printMap(map);
 	// System.out.println(logic.getAttackableUnits(map, d));
 	Position dest = new Position(4, 4);
 	System.out.println(String.format("Getting a %s from %s to %s", d, d.getPosition(), dest));
 	System.out.println(logic.findPath(map, d, dest));
 	int playersInGame = 0;
 	for (Player player : this.players)
 	    if (!player.hasLost())
 		playersInGame += 1;
 
 	if (playersInGame < 2)
 	    System.exit(0); /* Announce winner etc. */	
 	
     }
 
     public void attack(Unit attacker, Unit defender) {
 	Set<Position> attackable = logic.getAttackableUnitPositions(map, attacker);
 	if (!attackable.contains(defender.getPosition()))
 	    return;
 
 	Pair<Double, Double> damage = logic.calculateDamage(map, attacker, defender);
 
 	defender.reduceHealth(damage.getLeft());
 
 	Boolean died = removeUnitIfDead(defender);
 	if (died)
 	    return;
 
 	/* Possibly counter */
 	attacker.reduceHealth(damage.getRight());
 	removeUnitIfDead(attacker);
 
     }
 
     private Boolean removeUnitIfDead(Unit unit) {
 	if (unit.isDead()) {
 	    map.getField(unit.getPosition()).setUnit(null);
 	    unit.getOwner().removeUnit(unit);
	    return true;
 	}
	return false;
     }
 
     private void updateBuildingCaptureCounters() {
	for (GameField gf : map) {
 
 	    /* No building. */
 	    if (!gf.hostsBuilding())
 		continue;
 
 	    Building b = gf.getBuilding();
 	    
 	    /* Unit on the building. */
 	    if (gf.hostsUnit()) {
 		Unit unit = gf.getUnit();
		Integer turnReduce = unit.getHealth().intValue();
 		/* Unit already owns the building or is capturing for >1 turn. */
 		if (unit.getOwner().equals(b.getLastCapturer())) {
		    b.reduceCaptureTime(turnReduce);
 		    continue;
 		}
 		else {
 		    b.resetCaptureTime();
 		    b.setLastCapturer(unit.getOwner());
		    b.reduceCaptureTime(turnReduce);
 		}
 	    }
 	    /* No unit on the building. */
 	    else {
 		if (b.hasOwner())
 		    continue;
 		else
 		    b.resetCaptureTime();
 	    }
 	}
     }
 
     public void advanceTurn() {
 	updateBuildingCaptureCounters();
 	++this.turns;
     }
 
 
     public Integer getTurns() {
 	return this.turns;
     }
 
     public Map getMap() {
 	return this.map;
     }
     
     
 }
