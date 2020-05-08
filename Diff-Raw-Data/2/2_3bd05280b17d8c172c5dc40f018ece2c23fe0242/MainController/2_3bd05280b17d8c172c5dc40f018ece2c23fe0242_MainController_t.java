 package controller;
 
 import view.*;
 import view.Tile.FIELD;
 
 import java.util.Hashtable;
 
 import model.*;
 import model.exception.DeadBossException;
 import model.exception.DeadUnitException;
 import model.exception.LoseException;
 import model.exception.VictoryException;
 import model.units.Unit;
 
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.BasicGame;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Vector;
 
 /**
  * @author Aurel
  * @Singleton
  * 
  */
 public class MainController {
 
     private static MainController controller;
     public static String[] BOSS = { "Pegasus", "Dragon" }; // available boss
 
     /**
      * Returns the MainController.
      * 
      * @return the instance of MainController
      */
     public static MainController getInstance() {
 	if (controller == null)
 	    new MainController();
 	return controller;
     }
 
     public static void main(String[] args) {
 	MainController.getInstance().init(args);
     }
 
     private AppGameContainer app;
     private TurnController turn;
     private Player a; // local player
     private Player b; // remote player
     private ConnexionController client;
     private boolean auto;
     private boolean left;
     private String lastMessage;
 
     /**
      * The only constructor, the private no-argument constructor, can only be
      * called from this class within the getInstance method. It should be called
      * exactly once, the first time getInstance is called.
      */
     private MainController() {
 	if (controller == null)
 	    controller = this;
 	else
 	    throw new IllegalArgumentException(
 		    "Default constructor called more than once.");
 	this.auto = false;
 	// this.left = true;
 	a = null; // Instanciate by setPlayerA
 	b = new Player("Dragon"); // for local demo only
     }
 
     /**
      * Add a unit to player A.
      * 
      * @param name
      *            the name of unit
      * @param tile
      *            the tile
      */
     public void addUnit(String name, Tile tile) {
 	a.addUnit(name);
 	if (a.getUnit(name) != null)
 	    a.getUnit(name).setTile(tile);
     }
 
     /**
      * Add a unit to player B. (for demo only)
      * 
      * @param name
      *            the name of unit
      * @param tile
      *            the tile
      */
     public void addUnitToB(String name, Tile tile) {
 	b.addUnit(name);
 	if (b.getUnit(name) != null)
 	    b.getUnit(name).setTile(tile);
     }
     
     /**
      * Send a vector of Tile to see which Tile are in range of the unit's attack.
      * 
      * @param tiles
      *            the map build of tiles 
      * @param base
      *            the position of the attacker
      * @return the vector of tiles which can be attacked
      */
     public Vector<Tile> atkHighLight(Vector<Tile> tiles, Tile base) {
 	Vector<Tile> result = new Vector<Tile>();
 	boolean temp = false;
 	for (Tile t : tiles) {
 	    for (int i = 1; i <= getUnit(base).getRange(); i++) {
 		if (distance(t, base) <= getUnit(base).getRange()
 			&& distance(t, base) > 0) {
 		    if (t.isBlocked() == false) {
 			for (Tile p : result) {
 			    if (t == p) {
 				temp = true;
 			    }
 			}
 			if (temp == false) {
 			    result.add(t);
 			}
 			temp = false;
 		    }
 		}
 	    }
 	}
 	return result;
     }
     
     /**
      * Send a two dimensions String array which contains unit and positions of player A
      * 
      * @return Two dimensions String array
      */
     public String[][] aToTab() {
 	String[][] tab;
 	int i = 0;
 	Hashtable<String, Unit> units = a.getUnits();
 	Iterator<Unit> it = units.values().iterator();
 	Iterator<String> itKey = units.keySet().iterator();
 	tab = new String[units.size()][3];
 	Unit temp;
 	while (it.hasNext()) {
 	    temp = it.next();
 	    tab[i][0] = itKey.next();
 	    tab[i][1] = String.valueOf(temp.getTile().x);
 	    tab[i][2] = String.valueOf(temp.getTile().y);
 	    i++;
 	}
 
 	return tab;
     }
 
     /**
      * Attack between two units.
      * 
      * @param att
      *            the attacking unit
      * @param def
      *            the defending unit
      * @return the result of the fight
      * @throws VictoryException
      *             if a boss is dead
      */
     private String attack(String att, String def) throws VictoryException {
 	try {
 	    return a.attackWith(a.getUnit(att), b.getUnit(def),
 		    isTankInRange(b.getUnit(def)));
 	} catch (DeadBossException e) {
	    b.delUnit(e.getName());
 	    throw new VictoryException(e);
 	} catch (DeadUnitException e) {
 	    b.delUnit(e.getName());
 	    if (!a.getUnit(att).isPowActivate()) {
 		a.getUnit(att).activatePower();
 		return e.getName() + " est mort ! Activation du pouvoir de "
 			+ att + " !";
 	    }
 	    return e.getName() + " est mort !";
 	} catch (NullPointerException e) {
 	    return "L'unit " + def + " n'xiste pas !";
 	}
     }
 
     /**
      * Check if possible and attack between two tiles.
      * 
      * @param att
      *            the attacking tile
      * @param def
      *            the defending tile
      * @return the result of the fight
      * @throws VictoryException
      *             if a boss if dead
      */
     public String attack(Tile att, Tile def) throws VictoryException {
 	try {
 
 	    Unit at = a.getUnit(att.x, att.y);
 	    Unit de = b.getUnit(def.x, def.y);
 
 	    if (!turn.hasAttack(at)) { // si on a pas dj attaqu  ce tour
 
 		if ((att.x + 1 == def.x && att.y == def.y) // CaC
 			|| (att.x - 1 == def.x && att.y == def.y)
 			|| (att.y + 1 == def.y && att.x == def.x)
 			|| (att.y - 1 == def.y && att.x == def.x)) {
 		    String str = attack(at.getName(), de.getName());
 		    turn.setHasAttack(at);
 		    return str;
 		}
 
 		if (at.canAttackFromRange(distance(att, def))) // Distance
 		{
 		    String str = attack(at.getName(), de.getName());
 		    turn.setHasAttack(at);
 		    return str;
 		}
 		return "Impossible d'attaquer !";
 	    }
 	    return "Vous avez dj attaqu avec cette unit !";
 
 	} catch (NullPointerException e) {
 	    return "unit introuvable";
 	}
     }
     
     /**
      * Send a two dimensions String array which contains unit and positions of player B
      * 
      * @return Two dimensions String array
      */
     public String[][] bToTab() {
 	String[][] tab;
 	int i = 0;
 	Hashtable<String, Unit> units = b.getUnits();
 	Iterator<Unit> it = units.values().iterator();
 	Iterator<String> itKey = units.keySet().iterator();
 	tab = new String[units.size()][3];
 	Unit temp;
 	while (it.hasNext()) {
 	    temp = it.next();
 	    tab[i][0] = itKey.next();
 	    tab[i][1] = String.valueOf(temp.getTile().x);
 	    tab[i][2] = String.valueOf(temp.getTile().y);
 	    i++;
 	}
 
 	return tab;
     }
 
     /**
      * A recursive method which calculate how the unit can move.
      * @param tiles
      * 		    Tiles of the map
      * @param base
      * 		    Tile of the unit
      * @param moveNb
      * 		    Number of recursion
      * 
      * @return A vector of Tile which contains all possibilities of move
      */
     private Vector<Tile> canCross(Vector<Tile> tiles, Tile base, int moveNb) {
 	Tile[] finale = null;
 	Vector<Tile> tempo = new Vector<Tile>();
 	Vector<Tile> result = new Vector<Tile>();
 	boolean temp = false;
 
 	for (Tile t : tiles) {
 	    if (t.x == base.x + 1 && t.y == base.y) {
 		if (t.isBlocked() == false && !isPlayerBUnit(t)) {
 		    for (Tile p : result) {
 			if (p == t) {
 			    temp = true;
 			}
 		    }
 		    if (temp == false) {
 			result.add(t);
 		    }
 		    temp = false;
 		}
 	    }
 
 	    if (t.x == base.x && t.y + 1 == base.y) {
 		if (t.isBlocked() == false && !isPlayerBUnit(t)) {
 		    for (Tile p : result) {
 			if (p == t) {
 			    temp = true;
 			}
 		    }
 		    if (temp == false) {
 			result.add(t);
 		    }
 		    temp = false;
 		}
 	    }
 
 	    if (t.x == base.x - 1 && t.y == base.y) {
 		if (t.isBlocked() == false && !isPlayerBUnit(t)) {
 		    for (Tile p : result) {
 			if (p == t) {
 			    temp = true;
 			}
 		    }
 		    if (temp == false) {
 			result.add(t);
 		    }
 		    temp = false;
 		}
 	    }
 
 	    if (t.x == base.x && t.y - 1 == base.y) {
 		if (t.isBlocked() == false && !isPlayerBUnit(t)) {
 		    for (Tile p : result) {
 			if (p == t) {
 			    temp = true;
 			}
 		    }
 		    if (temp == false) {
 			result.add(t);
 		    }
 		    temp = false;
 		}
 	    }
 	}
 	finale = new Tile[result.size()];
 	int i = 0;
 	for (Tile test : result) {
 	    finale[i] = test;
 	    i++;
 	}
 	moveNb--;
 	if (moveNb == 0 || result.isEmpty()) {
 	    return result;
 	}
 	// System.out.println(finale.size());
 	for (i = 0; i < finale.length; i++) {
 	    tempo = this.canCross(tiles, finale[i], moveNb);
 	    for (Tile k : tempo) {
 		for (Tile p : result) {
 		    if (k == p) {
 			temp = true;
 		    }
 		}
 		if (temp == false) {
 		    result.add(k);
 		}
 		temp = false;
 	    }
 	}
 	return result;
     }
     
     /**
      * Call the recursive method canCross
      * @param tiles
      * 		   Tiles of the map
      * @param base 
      * 		   Tile of the unit
      * @return A vector of Tile which contains all possibilities of move
      */
     
     public Vector<Tile> canMove(Vector<Tile> tiles, Tile base) {
 	if (getUnit(base).hasMat() && getTurn().hasAttack(getUnit(base))
 		&& !getTurn().hasMoveAfterAttack(getUnit(base))) {
 	    Vector<Tile> result;
 	    if(getTurn().isCrippled(getUnit(base))){
 		result = canCross(tiles, base, getUnit(base).getMat());
 	    }
 	    else{
 		result = canCross(tiles, base, getUnit(base).getMat());
 
 	    }
 	    return result;
 	} else {
 	    Vector<Tile> result;
 	    if(getTurn().isCrippled(getUnit(base))){
 		result = canCross(tiles, base, getUnit(base).getMove()/2);
 	    }
 	    else{
 		result = canCross(tiles, base, getUnit(base).getMove());
 
 	    }
 	    return result;
 	}
     }
     
     /**
      * Init the connection between client and server.
 
      */
     public void connexion() {
 	client = new ConnexionController();
 	Msg firstco = new Msg("", true, false);
 	client.sendMsg(firstco);
 	while (client.getMsg().getOkCo() != true) {
 	    try {
 		Thread.sleep(1);
 	    } catch (InterruptedException e) {
 		e.printStackTrace();
 	    }
 	}
 	left = client.getMsg().getFirstCo();
 	a.setTurn(client.getMsg().getFirstCo());
 	if(a.getTurn()){
 	    lastMessage = "A vous de commencer.";
 	}
 	client.eraseMsg();
 
     }
 
     /**
      * Give the Manhattan distance between two Tiles.
      * @param tile1
      * 		   First tile.
      * @param tile2 
      * 		   Second tile.
      * @return The distance between tile1 and tile2 as an integer.
      */
     public int distance(Tile tile1, Tile tile2) {
 	int a = tile1.x - tile2.x;
 	if (a < 0) {
 	    a = a * -1;
 	}
 	int b = tile1.y - tile2.y;
 	if (b < 0) {
 	    b = b * -1;
 	}
 	return a + b;
     }
 
     /**
      * End a turn of the game.
      */
     public void endNewTurn() {
 	for (Unit u : a.getUnits().values()) {
 	    u.setAttackedPrevious(turn.hasAttack(u));
 	}
     }
 
     /**
      * Set the turn to false.
      */
     public void endTurn() {
 	a.setTurn(false);
     }
 
     /**
      * Return player a.
      * @return Player A as a Player.
      */
     public Player getPlayerA() {
 	return a;
     }
 
     /**
      * Returns all available units of player A.
      * 
      * @return strings of names.
      */
     public String[] getPlayerAUnitsNames() {
 	return a.getNamesOfUnits();
     }
 
     /**
      * Return player B.
      * @return Player B as a Player.
      */
     public Player getPlayerB() {
 	return b;
     }
     
     /**
      * Return TurnController.
      * @return TurnController.
      */
     public TurnController getTurn() {
 	return turn;
     }
     
     /**
      * Return the unit at a certain Tile.
      * @param t
      * 		Tile where method will search an unit.
      * @return An unit if there is an unit in this Tile, null if there is no unit.
      */
     public Unit getUnit(Tile t) {
 	for (Unit u : a.getUnits().values())
 	    if (u.getTile().x == t.x && u.getTile().y == t.y)
 		return u;
 	for (Unit u : b.getUnits().values())
 	    if (u.getTile().x == t.x && u.getTile().y == t.y)
 		return u;
 	return null;
     }
 
     /**
      * Init the GameContainer, start the game.
      * 
      * @param args
      *            the arguments
      */
     public void init(String[] args) {
 
 	for (String s : args) {
 	    System.out.println(s);
 	    if (s.equals("-auto")) {
 		System.out.println("mode auto");
 		this.auto = true;
 	    } else if (s.contains("-ip=")) {
 		s = s.replace("-ip=", "");
 		ConnexionController.IP = s;
 	    } else if (s.contains("-port=")) {
 		s = s.replace("-port=", "");
 		ConnexionController.PORT = s;
 	    }
 
 	}
 
 	try {
 	    app = new AppGameContainer(new ViewController());
 	    // app.setShowFPS(false);
 	    app.setDisplayMode(1280, 704, false);
 	    app.start();
 	} catch (SlickException e) {
 	    e.printStackTrace();
 	}
     }
 
     /**
      * Init a new turn of the game.
      * 
      * @return results
      * @throws VictoryException
      *             if a boss is dead
      */
     public synchronized String initNewTurn() throws VictoryException {
 	String str = "";
 	turn = new TurnController(a.getUnits());
 	for (Unit u : a.getUnits().values()) {
 	    try {
 		if (u.getTile().getField() == FIELD.FORT)
 		    u.addRegenerationFort();
 		if (turn.isPoisoned(u))
 		    u.receivePoisonedDmg();
 	    } catch (DeadBossException e) {
 		throw new VictoryException(e);
 	    } catch (DeadUnitException e) {
 		a.delUnit(e.getName());
 		str += e.getName() + " est mort !";
 	    }
 	}
 	for (Unit u : a.getUnits().values()) {
 	    if (u.getTile().getField() == FIELD.FORT)
 		u.addRegenerationFort();
 	    if (u.getName() == "Rodeur"
 		    && u.getTile().getField() == FIELD.FOREST)
 		u.addRegenerationForest();
 	    u.addRegeneration();
 	}
 	return str;
     }
 
     /**
      * Returns if automatic game. (for demo only)
      * 
      * @return true if demo false otherwise
      */
     public boolean isAuto() {
 	return auto;
     }
 
     /**
      * Check if the Eclaireur is stealth.
      * 
      * @return true if the Eclaireur is stealth.
      */
     public boolean isCamo() {
 	return b.getUnit("Eclaireur").getTile().getField() == FIELD.FOREST;
     }
 
     /**
      * Returns crippled units on the map.
      * 
      * @param tiles
      *            the map
      * @return tiles with units crippled.
      */
     public Vector<Tile> isCrippled(Vector<Tile> tiles) {
 	Vector<Tile> result = new Vector<Tile>();
 	for (Tile t : tiles) {
 	    if (getUnit(t) != null && getUnit(t).getTurnsCripple() > 0) {
 		result.add(t);
 	    }
 	}
 
 	return result;
     }
 
     /**
      * Returns is a Tile does'nt already have a unit.
      * 
      * @param tile
      *            the tile
      * @return true if free false otherwise
      */
     public boolean isFreeTileset(Tile tile) {
 	Hashtable<String, Unit> units = a.getUnits();
 	Iterator<Unit> it = units.values().iterator();
 	Unit temp;
 	while (it.hasNext()) {
 	    temp = it.next();
 	    if (temp.getTile().x == tile.x && temp.getTile().y == tile.y) {
 		return false;
 	    }
 	}
 
 	if (b != null) {
 	    units = b.getUnits();
 	    if (units != null) {
 		it = units.values().iterator();
 		while (it.hasNext()) {
 		    temp = it.next();
 		    if (temp.getTile().x == tile.x
 			    && temp.getTile().y == tile.y)
 			return false;
 		}
 	    }
 	}
 	return true;
     }
 
     /**
      * Returns if a unit on a tile belongs to player A.
      * 
      * @param tile
      *            the tile
      * @return true if the unit belongs to A false otherwise
      */
     public boolean isPlayerAUnit(Tile tile) {
 	Hashtable<String, Unit> units = a.getUnits();
 	Iterator<Unit> it = units.values().iterator();
 	Unit temp;
 	while (it.hasNext()) {
 	    temp = it.next();
 	    if (temp.getTile().x == tile.x && temp.getTile().y == tile.y)
 		return true;
 	}
 	return false;
     }
 
     /**
      * Returns if a unit on a tile belongs to player B.
      * 
      * @param tile
      *            the tile
      * @return true if the unit belongs to B false otherwise
      */
     public boolean isPlayerBUnit(Tile tile) {
 	Hashtable<String, Unit> units = b.getUnits();
 	Iterator<Unit> it = units.values().iterator();
 	Unit temp;
 	while (it.hasNext()) {
 	    temp = it.next();
 	    if (temp.getTile().x == tile.x && temp.getTile().y == tile.y)
 		return true;
 	}
 	return false;
     }
 
     /**
      * Returns poisoned units on the map.
      * 
      * @param tiles
      *            the map
      * @return tiles with units poisoned.
      */
     public Vector<Tile> isPoisoned(Vector<Tile> tiles) {
 	Vector<Tile> result = new Vector<Tile>();
 	for (Tile t : tiles) {
 	    if (getUnit(t) != null && getUnit(t).getTurnsPoisoned() > 0) {
 		result.add(t);
 	    }
 	}
 	return result;
     }
 
     /**
      * Returns if the tank of the player is in range of the unit.
      * 
      * @param u
      *            the unit
      * @return true if the tank of the player is in range false otherwise
      */
     public boolean isTankInRange(Unit u) {
 	int range = 2;
 	if (isPlayerAUnit(u.getTile())) {
 	    Unit tank = a.getUnit("Tank");
 	    if (tank == null)
 		return false;
 	    if (tank.isPowActivate())
 		return distance(tank.getTile(), u.getTile()) <= range * 2;
 	    return distance(tank.getTile(), u.getTile()) <= range;
 	} else {
 	    Unit tank = b.getUnit("Tank");
 	    if (tank == null)
 		return false;
 	    if (tank.isPowActivate())
 		return distance(tank.getTile(), u.getTile()) <= range * 2;
 	    return distance(tank.getTile(), u.getTile()) <= range;
 	}
     }
 
     /**
      * Returns true if it's your turn to play.
      * 
      * @return true if the server sent you a message with firstCo field at true.
      */
     public boolean isTurn() {
 	if (client.getMsg() != null) {
 	    boolean temp = client.getMsg().getFirstCo();
 	    client.eraseMsg();
 	    return temp;
 	} else {
 	    return false;
 	}
     }
 
     /**
      * Move an unit at a certain Tile to another tile.
      * @param tile
      * 		   Tile to move the unit.
      * @param currentSelected
      * 		   		Tile where is the unit.
      * @param hightLight
      * 			 Vector of Tile the unit can move on.
      */
     public void move(Tile tile, Tile currentSelected, Vector<Tile> highLight) {
 	Unit u = getUnit(currentSelected);
 	if ((!turn.hasMove(u) && !turn.hasAttack(u)))
 	    for (Tile t : highLight) {
 		if (tile == t) {
 		    if (isFreeTileset(tile) && tile.isBlocked() == false) {
 			u.setTile(tile);
 			turn.setHasMove(u);
 		    } else
 			System.out.println("La case n'est pas libre");
 		} else {
 		    // System.out.println("C'est trop loin");
 		}
 	    }
 	else if (turn.hasAttack(u) && !turn.hasMoveAfterAttack(u)) {
 	    for (Tile t : highLight) {
 		if (tile == t) {
 		    if (isFreeTileset(tile) && tile.isBlocked() == false) {
 			u.setTile(tile);
 			turn.setHasMoveAfterAttack(u);
 		    } else
 			System.out.println("La case n'est pas libre");
 		} else {
 		    // System.out.println("C'est trop loin");
 		}
 	    }
 	}
     }
 
     /**
      * Returns if the player side is left.
      * 
      * @return true if the player play left
      */
     public boolean playLeft() {
 	return left;
     }
 
     public void recPlayer() {
 	while (client.getPlayer() == null) {
 	    try {
 		Thread.sleep(1);
 	    } catch (InterruptedException e) {
 		e.printStackTrace();
 	    }
 	}
 	b = client.getPlayer()[0];
 	client.setNull();
     }
 
     public void recPlayers() throws LoseException {
 	Player[] tab = client.getPlayer();
 	if (tab != null) {
 	    if (tab.length == 2) {
 		b = tab[0];
 		a = tab[1];
 	    }
 	    client.setNull();
 	}
 	if(a.getUnit(a.getBoss()) == null) throw new LoseException();
     }
 
     public void sendMsg(String message) {
 	Msg msg = new Msg(message, false, true);
 	client.sendMsg(msg);
     }
 
     public void sendLastMessage() {
 	Msg msg = new Msg(lastMessage, false, true);
 	client.sendMsg(msg);
     }
     
     public void recMsg() {
 	if (client.getMsg() != null) {
 	    lastMessage = client.getMsg().getMsg();
 	}
     }
 
     public String getLastMessage() {
 	return lastMessage;
     }
 
     public void sendBoth() {
 	client.sendPlayers(a, b);
     }
 
     public void sendEnd() {
 	Msg msg = new Msg("C'est  vous de jouer.", true, true);
 	client.sendMsg(msg);
     }
 
 
     public void setLastMessage(String text) {
 	lastMessage = text;
     }
 
     public void sendPlayer() {
 	client.sendPlayer(a);
     }
 
     /**
      * Instantiates the player A according to the choice of boss.
      * 
      * @param boss
      *            the boss
      */
     public void setPlayerA(String boss) {
 	if (a == null)
 	    a = new Player(boss);
     }
 }
