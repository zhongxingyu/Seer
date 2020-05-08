 package controller;
 
 import view.*;
 
 import java.util.Hashtable;
 
 import model.*;
 
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.BasicGame;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Vector;
 
 public class MainController {
     private static MainController controller;
     private AppGameContainer app;
     private TurnController turn;
     private Player a;
     private Player b;
 
     /**
      * @param args
      */
     private MainController() {
 	a = new Player();
 	b = new Player();
     }
 
     public static MainController getInstance() {
 	if (controller == null)
 	    controller = new MainController();
 	return controller;
     }
 
     public Player getPlayerA() {
 	return a;
     }
 
     public Player getPlayerB() {
 	return b;
     }
 
     public boolean isFreeTileset(Tile tile) {
 	Hashtable<String, Unit> units = a.getUnits();
 	Iterator<Unit> it = units.values().iterator();
 	Unit temp;
 	while (it.hasNext()) {
 	    temp = it.next();
 	    if (temp.getTile().x == tile.x && temp.getTile().y == tile.y)
 		return false;
 
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
     
     public Unit getUnit(Tile t)
     {
 	for(Unit u : a.getUnits().values())
 	    if(u.getTile()==t) return u;
 	for(Unit u : b.getUnits().values())
 	    if(u.getTile()==t) return u;
 	return null;
     }
 
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
 
     public void addUnit(String name, Tile tile) {
 	a.addUnit(name);
 	if (a.getUnit(name) != null)
 	    a.getUnit(name).setTile(tile);
     }
 
     public void addUnitToB(String name, Tile tile) {
 	b.addUnit(name);
 	b.getUnit(name).setTile(tile);
     }
 
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
 
     public void move(Tile tile, Tile currentSelected, Vector<Tile> highLight) {
	Unit u = getUnit(currentSelected);
	if(!turn.hasMove(u))
         	for (Tile t : highLight) {
         	    if (tile == t) {
         		if (isFreeTileset(tile) && tile.isBlocked() == false)
        		{
        		    u.setTile(tile);
        		    turn.setHasMove(u);
        		    
        		} else
         		    System.out.println("La case n'est pas libre");
         	    } else {
         		System.out.println("C'est trop loin");
         	    }
         	}
 	else System.out.println("Vous avez dj dplac cette unit !");
     }
 
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
 
     public Vector<Tile> canCross(Vector<Tile> tiles, Tile base, int moveNb) {
 	Tile[] finale = null;
 	Vector<Tile> tempo = new Vector<Tile>();
 	Vector<Tile> result = new Vector<Tile>();
 	boolean temp = false;
 
 	for (Tile t : tiles) {
 	    if (t.x == base.x + 1 && t.y == base.y) {
 		if (t.isBlocked() == false) {
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
 		if (t.isBlocked() == false) {
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
 		if (t.isBlocked() == false) {
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
 		if (t.isBlocked() == false) {
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
 
     public void init() {
 
 	try {
 	    app = new AppGameContainer(new ViewController());
 	    // app.setShowFPS(false);
 	    app.setDisplayMode(1280, 704, false);
 	    app.start();
 	} catch (SlickException e) {
 	    e.printStackTrace();
 	}
 
 	/*
 	 * 
 	 * this.a.addUnit("Eclaireur"); this.a.getUnit("Eclaireur");
 	 * this.a.addUnit("Fantassin"); this.a.addUnit("Rodeur");
 	 * 
 	 * // b est gr par le rseau on ne le cr jamais il est rcupr !
 	 * this.b.addUnit("Eclaireur"); this.b.addUnit("Tank");
 	 * this.b.addUnit("Bretteur");
 	 * 
 	 * // Pour changer la port d'un rodeur  5: exemple de pouvoir
 	 * a.getUnit("Rodeur").setIAttack(new AttackDistance(5)); // Pour donner
 	 * 15% de double attack a.getUnit("Fantassin").setIAttack(new
 	 * AttackCaCBerserker());
 	 * 
 	 * if (a.getUnit("Rodeur").canAttackFromRange(3))
 	 * System.out.println(attack("Rodeur", "Tank"));
 	 * 
 	 * System.out.println(attack("Eclaireur", "Eclaireur"));
 	 * System.out.println(attack("Fantassin", "Tank"));
 	 * System.out.println(attack("Fantassin", "Tank"));
 	 * System.out.println(attack("Fantassin", "Tank"));
 	 */
 
     }
 
     public static void main(String[] args) {
 	MainController.getInstance().init();
     }
 
     public void initNewTurn() {
 	turn = new TurnController(a.getUnits());
 	// TODO Ajout des pv aux units sur les forts
     }
 
     private String attack(String att, String def) {
 	try {
 	    return a.attackWith(a.getUnit(att), b.getUnit(def));
 	} catch (DeadUnitException e) {
 	    b.delUnit(e.getName());
 	    return e.getName() + " est mort !";
 	} catch (NullPointerException e) {
 	    return "L'unit " + def + " n'xiste pas !";
 	}
     }
 
     public String[] getPlayerAUnitsNames() {
 	return a.getNamesOfUnits();
     }
     
 
     public String attack(Tile att, Tile def) {
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
 }
