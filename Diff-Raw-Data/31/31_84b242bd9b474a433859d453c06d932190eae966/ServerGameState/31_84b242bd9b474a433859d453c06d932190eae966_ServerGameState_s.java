 package server;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.Random;
 import java.util.Set;
 
 import physics.Arbiter;
 import physics.CattoPhysicsEngine;
 
 import world.GameObject;
 import world.PlayerObject;
 import jig.engine.physics.AbstractBodyLayer;
 import jig.engine.physics.BodyLayer;
 import jig.engine.util.Vector2D;
 import net.NetObject;
 import net.NetState;
 
 public class ServerGameState {
 
 	private NetState netState;
 	private Hashtable<Integer, GameObject> goTable;
 	private BodyLayer<GameObject> layer; // all layer
 	// private BodyLayer<PlayerObject> pLayer; // player layer
 	// private BodyLayer<GameObject> bLayer; // bullet layer
 	// private BodyLayer<GameObject> dLayer; // dynamic layer
 	// private BodyLayer<GameObject> sLayer; // static layer
 	private Random generator;
 	private static ServerGameState theGameState;
 	public long totalMs;
 
 	public ServerGameState() {
 		netState = new NetState();
 		goTable = new Hashtable<Integer, GameObject>();
 		layer = new AbstractBodyLayer.NoUpdate<GameObject>();
 		generator = new Random();
 		totalMs = 0;
 		theGameState = this;
 	}
 	
 	public static ServerGameState getGameState() {
 		return theGameState;
 	}
 
 	public int getUniqueId() {
 		Set<Integer> usedIds = goTable.keySet();
 		int id = 100;
 		while (usedIds.contains(id))
 			id = generator.nextInt(65000);
 		return id;
 	}
 
 	/**
 	 * This method is called when adding a player because id is already provided
 	 * 
 	 */
 	public void addPlayer(int id, PlayerObject p) {
 		if (goTable.containsKey(id))
 			return;
 		p.setID(id);
 		goTable.put(id, p);
 		netState.add(new NetObject(id, p.getPosition(), GameObject.PLAYER));
 		layer.add(p);
 	}
 
 	public void add(GameObject go) {
 		if (go.type == GameObject.STATIC) {
 			addStatic(go);
 		} else {
 			addDynamic(go);
 		}	
 	}
 
 	// add dynamic objects such as boxes
 	public int addDynamic(GameObject go) {
 		int id = getUniqueId();
 		go.setID(id);
 		if (goTable.containsKey(id)) return id;
 
 		layer.add(go);
 		goTable.put(id, go);
 		netState.add(new NetObject(id, go.getPosition(), go.type));
 
 		return id;
 	}
 
 	// add static object such as platforms
 	public void addStatic(GameObject go) {
 		layer.add(go);
 	}
 
 	public void update(long deltaMs) {
 		totalMs += deltaMs;
 		updatePlayers(deltaMs);
 
 		//netState.clear(); // TODO: this could be done faster by just adding and removing
 		/*System.out.println("ServerGameState update: " + goTable.size());
 		for (GameObject go : goTable.values()) {
 			if (go == null) {
 				System.out.println("ServerGameState update: null");
 				return;
 			}
 			if (!go.isActive()) {
 				System.out.println("ServerGameState update: not active: " + go.getType());
 				return;
 			}
 			
 			NetObject no = null;
 			if (netState.objectList.contains(go.getID())) {
 				no = netState.objectList.get(go.getID());
 			} else {
 				no = new NetObject(go.getID(), go.getPosition(), go.type);
 				netState.add(no);
 			}
 			no.setPosition(go.getPosition());
 			no.setVelocity(go.getVelocity());
 			no.setRotation(go.getRotation());
 			if (go.getType() == GameObject.PLAYER){
 				no.setHealth(((PlayerObject)go).getHealth());
 			}
 		}*/
 		
 		Hashtable<Integer, NetObject> netList = netState.getHashtable();
 		for (Integer i : goTable.keySet()) {
 			GameObject go = goTable.get(i);
 			if (go == null)
 				return;
 
 			NetObject no = netList.get(i);
 			if (no == null)
 				return; // probably should add it
 
 			no.setActive(go.isActive());
 			no.setPosition(go.getPosition());
 			// System.out.println(b.getVelocity());
 			no.setVelocity(go.getVelocity());
 			no.setRotation(go.getRotation());
 			if (go.getType() == GameObject.PLAYER){
 				no.setHealth(((PlayerObject)go).getHealth());
 				no.setFrameIndex(((PlayerObject)go).getFrameIndex());
 			}
 		}
 
 		
 		// check for bullet collisions
 		// this could be added above to speed things up but for now
 		// I want it separate for simplicity sake
 		ArrayList<Arbiter> arbiters = CattoPhysicsEngine.getPhysicsEngine().getArbiters();
 
 		for (Arbiter arbit : arbiters) {
 			if (((GameObject) arbit.body1).getType() == GameObject.BULLET) {
 				if (arbit.getNumContacts() > 0) {
 					handleBullet((GameObject) arbit.body1,
 							(GameObject) arbit.body2);
 					return;
 				}
 			}
 			if (((GameObject) arbit.body2).getType() == GameObject.BULLET) {
 				if (arbit.getNumContacts() > 0) {
 					handleBullet((GameObject) arbit.body2,
 							(GameObject) arbit.body1);
 					return;
 				}
 			}
 		}
 	}
 
 	/**
 	 * Handle bullet collision
 	 * 
 	 * @param bullet
 	 * @param other
 	 */
 	private void handleBullet(GameObject bullet, GameObject other) {
 		bullet.setActivation(false);
 		bullet.setVelocity(new Vector2D(0, 0));
 		bullet.setPosition(new Vector2D(-10000, -10000));
 		
 		if (other.getType() == GameObject.PLAYER) {
 			PlayerObject player = (PlayerObject)other;
			if (player.isAlive) 
 				player.woundBy(bullet);
 			
 		} else if (other.getType() == GameObject.DRUM) {
 			// 20% chance of going boom? hmm
 			// if boom event...
 			// remove drum
 			// 3-5 explosion events around drum
 			// X bullets from drum, random direction and initial positions from somewhere inside orginial drum location
 			System.out.println("Drum hit by bullet event - at " + other.getCenterPosition() + " servergamestate");
 		}
 	}
 
 	public Hashtable<Integer, GameObject> getHashtable() {
 		return goTable;
 	}
 
 	/**
 	 * Returns a Layer which can be added to rendering layer
 	 * 
 	 * @return
 	 */
 	public BodyLayer<GameObject> getLayer() {
 		return layer;
 	}
 
 	// public BodyLayer<GameObject> getbLayer() {
 	// return bLayer;
 	// }
 	//
 	// public BodyLayer<GameObject> getdLayer() {
 	// return dLayer;
 	// }
 	//
 	// public BodyLayer<GameObject> getsLayer() {
 	// return sLayer;
 	// }
 
 	/**
 	 * clamp players angle and velocity
 	 * 
 	 * @return
 	 */
 	public void updatePlayers(long deltaMs) {
 		PlayerObject p = null;
 		for (GameObject go : layer)
 			if (go.type == GameObject.PLAYER) {
 				p = (PlayerObject) go;
 				p.updatePlayerState(deltaMs);
 			}
 		return;
 	}
 
 	public NetState getNetState() {
 		return netState;
 	}
 
 	public void removeByID(int ID) {
 		// GameObject ret = goList.get(ID);
 		// goList.get(ID).setActivation(false);
 		goTable.remove(ID);
 		netState.objectList.remove(ID);
 		// return ret;
 	}
 	
 	public PlayerObject playerByID(int id) {
 		return (PlayerObject) goTable.get(id);
 	}
 }
