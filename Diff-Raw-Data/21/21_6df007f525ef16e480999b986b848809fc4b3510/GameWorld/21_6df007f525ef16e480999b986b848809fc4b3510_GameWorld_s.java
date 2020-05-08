 package com.friendlyblob.mayhemandhell.client.gameworld;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.concurrent.ConcurrentHashMap;
 
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Vector3;
 import com.friendlyblob.mayhemandhell.client.MyGame;
 import com.friendlyblob.mayhemandhell.client.controls.Input;
 import com.friendlyblob.mayhemandhell.client.entities.EnvironmentObject;
 import com.friendlyblob.mayhemandhell.client.entities.GameCharacter;
 import com.friendlyblob.mayhemandhell.client.entities.GameObject;
 import com.friendlyblob.mayhemandhell.client.entities.Player;
 import com.friendlyblob.mayhemandhell.client.entities.TargetMark;
 import com.friendlyblob.mayhemandhell.client.entities.TopicTv;
 import com.friendlyblob.mayhemandhell.client.entities.gui.ChatBubbleNotifications;
 import com.friendlyblob.mayhemandhell.client.network.packets.client.RequestAction;
 import com.friendlyblob.mayhemandhell.client.network.packets.client.RequestTarget;
 
 public class GameWorld {
 	public static GameWorld instance;
 	
 	private final int SORT_ON = 5;
 	private int sortFrameCount;
 	
 	/*-------------------------------------
 	 * Entities
 	 */
 	private Map map;
 	public Player player;
 	
 	public MyGame game;
 	
 	public List<GameCharacter> characters = new ArrayList<GameCharacter>(128);
 	public List<GameObject> gameObjects = new ArrayList<GameObject>(128);
 		
 	private static ArrayList<EnvironmentObject> environmentObjects = new ArrayList<EnvironmentObject>();
 	
 	public static HashMap<String, String> environmentObjectTypes = new HashMap<String, String>();
 	
 	public TargetMark targetMark;
 	public TopicTv topicTv;
 	
 	public ChatBubbleNotifications chatBubbles;
 	
 	/*-------------------------------------
 	 * Camera
 	 */
 	private OrthographicCamera worldCam;
 	private Vector3 camPos;
 	
 	public GameWorld() {
 		chatBubbles = new ChatBubbleNotifications();
 		
 		/*--------------------------------
 		 * World camera setup
 		 */
 		worldCam = new OrthographicCamera(MyGame.SCREEN_WIDTH, MyGame.SCREEN_HEIGHT);
 		worldCam.translate(MyGame.SCREEN_WIDTH/2, MyGame.SCREEN_HEIGHT/2);
 		worldCam.update();
 		camPos = worldCam.position;
 		
 		/*
 		 * Entities initialization
 		 */
 		targetMark = new TargetMark();
 		
 		topicTv = new TopicTv();
 	}
 	
 	public void putCharacter(GameCharacter character) {
 		synchronized (characters) {
 			characters.add(character);
 		}
 		putObject(character);
 	}
 	
 	public void putObject(GameObject object) {
 		synchronized (gameObjects) {
 			gameObjects.add(object);
 		}
 	}
 	
 	public void removeCharacter(int id) {
 		synchronized (characters) {
 			for	(GameCharacter gc : characters) {
 				if (gc.objectId == id) {
 					characters.remove(gc);
 					break;
 				}
 			}
 		}
 		
 		removeObject(id);
 	}
 	
 	public void removeObject(int id) {
 		synchronized (gameObjects) {
 			for	(GameObject go : gameObjects) {
 				if (go.objectId == id) {
 					gameObjects.remove(go);
 					break;
 				}
 			}
 		}
 	}
 	
 	public boolean characterExists(int id) {
 		synchronized (characters) {
 			for	(GameCharacter gc : characters) {
 				if (gc.objectId == id) {
 					return true;
 				}
 			}
 		}
 		
 		return false;
 	}
 	
 	public boolean objectExists(int id) {
 		synchronized (gameObjects) {
 			for	(GameObject go : gameObjects) {
 				if (go.objectId == id) {
 					return true;
 				}
 			}
 		}
 		
 		return false;
 	}
 	
 	public GameCharacter getCharacter(int id) {
 		synchronized (characters) {
 			for	(GameCharacter gc : characters) {
 				if (gc.objectId == id) {
 					return gc;
 				}
 			}
 		}
 		
 		return null;
 	}
 	
 	public GameObject getObject(int id) {
 		synchronized (gameObjects) {
 			for	(GameObject go : gameObjects) {
 				if (go.objectId == id) {
 					return go;
 				}
 			}
 		}
 		
 		return null;
 	}
 	
 	public void update(float deltaTime) {
 		// TODO optimize to avoid iterators (Make sure FastMap uses them first)
 		synchronized (characters) {
 			for (GameCharacter character : characters) {			
 				character.update(deltaTime);
 			}
 		}
 		
 		chatBubbles.update(deltaTime);
 		
 		cameraFollowPlayer(deltaTime);
 		
 		map.update(deltaTime);
 		
 		if (sortFrameCount++ == SORT_ON) {
 			synchronized (gameObjects) {
 				Collections.sort(gameObjects, new Comparator<GameObject>() {
 				    @Override
 				    public int compare(GameObject go1, GameObject go2) {
 				    	if (go1.position.y > go2.position.y) {
 				    		return -1;
 				    	} else if (go1.position.y == go2.position.y ) {
 				    		return 0;
 				    	}
 				    	
 				    	return 1;
 				    }
 				});	
 			}
 			
 			sortFrameCount = 0;
 		}
 
 	}
 	
 	/**
 	 * Updates input that is related to the world (not gui),
 	 * like walking or targeting an object.
 	 */
 	public void updateWorldInput() {
 		if (Input.isReleasing()) {
 			
			GameObject gameObject = getObjectAt(toWorldX(Input.getX()), toWorldY(Input.getY()));
			
			if (gameObject != null) {
				if (gameObject == targetMark.getTarget()) {
					MyGame.connection.sendPacket(new RequestAction(0));
				} else {
					MyGame.connection.sendPacket(new RequestTarget(gameObject.objectId));
				}
			} else {
 				// Movement should be the last case
 				getPlayer().requestMovementDestination(toWorldX(Input.getX()), toWorldY(Input.getY()));
			}
 		}
 	}
 	
 	public void draw(SpriteBatch spriteBatch) {
 		spriteBatch.setProjectionMatrix(worldCam.combined);
 
 		map.drawBelow(spriteBatch);
 
 		for (EnvironmentObject go : environmentObjects) {
 			go.draw(spriteBatch);
 		}
 		
 		targetMark.draw(spriteBatch);
 		topicTv.draw(spriteBatch);
 		
 		synchronized (gameObjects) {
 			for (GameObject go : gameObjects) {
 				go.draw(spriteBatch);
 			}		
 		}
 		
 		map.drawAbove(spriteBatch);
 		
 		GameWorld.instance.chatBubbles.draw(spriteBatch);
 	}
 	
 	public void cameraFollowPlayer(float deltaTime){
 		// X follow
 		if (player.hitBox.x > worldCam.position.x + MyGame.SCREEN_WIDTH*0.15f) {
 			worldCam.position.x += player.hitBox.x - (worldCam.position.x + MyGame.SCREEN_WIDTH*0.15f);
 		} else if (player.hitBox.x < worldCam.position.x - MyGame.SCREEN_WIDTH*0.15f) {
 			worldCam.position.x -= (worldCam.position.x - MyGame.SCREEN_WIDTH*0.15f) - player.hitBox.x;
 		}
 		
 		// Y follow
 		if (player.hitBox.y > worldCam.position.y + MyGame.SCREEN_HEIGHT*0.15f) {
 			worldCam.position.y += player.hitBox.y - (worldCam.position.y + MyGame.SCREEN_HEIGHT*0.15f);
 		} else if (player.hitBox.y < worldCam.position.y - MyGame.SCREEN_HEIGHT*0.15f) {
 			worldCam.position.y -= (worldCam.position.y - MyGame.SCREEN_HEIGHT*0.15f) - player.hitBox.y;
 		}
 		
 		// BOUNDS
 		
 		// Bottom
 		if (worldCam.position.y - worldCam.viewportHeight/2 < 0) {
 			worldCam.position.y = worldCam.viewportHeight/2;
 		}
 		
 		// Left
 		if (worldCam.position.x - worldCam.viewportWidth/2 < 0) {
 			worldCam.position.x = worldCam.viewportWidth/2;
 		}
 		
 		worldCam.update();
 	}
 	
 	public void setGame(MyGame game) {
 		this.game = game;
 	}
 	
 	public Map getMap() {
 		return map;
 	}
 	
 	/**
 	 * Creates a map from MapData
 	 * @param data
 	 */
 	public void createMap(MapData data) {
 		if (this.map != null) {
 			this.map.dispose();
 		}
 		map = new Map(this, data);
 	}
 	
 	public void setMap(Map map) {
 		if (this.map != null) {
 			this.map.dispose();
 		}
 		this.map = map;
 	}
 	
 	public Player getPlayer() {
 		return player;
 	}
 	
 	public static ArrayList<EnvironmentObject> getObjects() {
 		return environmentObjects;
 	}
 	
 	/*
 	* Translate screen x coordinates to world x coordinate
 	*/
 	public int toWorldX(int x) {
 		return (int)(x*worldCam.zoom + camPos.x-MyGame.SCREEN_HALF_WIDTH);
 	}
 	
 	/*
 	* Translate screen y coordinates to world y coordinate
 	*/
 	public int toWorldY(int y) {
 		return (int)(y*worldCam.zoom + camPos.y-MyGame.SCREEN_HALF_HEIGHT);
 	}
 	
 	
 	/**
 	 * Translate world x coordinate to screen x coordinate
 	 * @param x world coordinate
 	 * @return
 	 */
 	public int toScreenX(float x) {
 		return (int)((x + MyGame.SCREEN_HALF_WIDTH - camPos.x)/worldCam.zoom);
 	}
 	
 	/**
 	 * Translate world y coordinate to screen y coordinate
 	 * @param y world coordinate
 	 * @return
 	 */
 	public int toScreenY(float y) {
 		return (int)((y + MyGame.SCREEN_HALF_HEIGHT - camPos.y)/worldCam.zoom);
 	}
 	
 	/**
 	 * Gets a visible object that is at location x y
 	 * @param x
 	 * @param y
 	 * @return GameObject at x y, or null if there's no object
 	 */
 	public GameObject getObjectAt(int x, int y) {
 		
 		for (GameObject object : gameObjects) {
 			if (object.hitBox.contains(x, y)) {
 				return object;
 			}
 		}
 
 		return null;
 	}
 	
 	public static void initialize() {
 		instance = new GameWorld();
 	}
 	
 	public static GameWorld getInstance() {
 		return instance;
 	}
 	
 	public OrthographicCamera getWorldCam() {
 		return worldCam;
 	}
 	
 }
