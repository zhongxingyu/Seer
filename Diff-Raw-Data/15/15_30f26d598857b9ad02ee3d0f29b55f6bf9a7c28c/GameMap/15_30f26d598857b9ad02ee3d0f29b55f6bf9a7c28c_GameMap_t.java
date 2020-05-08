 package infrastructure;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.Serializable;
 import java.nio.file.Path;
 import java.util.ArrayList;
 
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.Body;
 import org.jbox2d.dynamics.World;
 
 import utils.Parse;
 
 import entities.*;
 import guiobject.BackGround;
 
 public class GameMap implements Serializable {
 	private static final long serialVersionUID = 42L;
 	private ArrayList<Entity> gameElements;
 	private BackGround back;
 	private World world;
 	private Time time;
 	private float width;
 	private float height;
 	private float gravityMag;
 	private String originalDataLoc;
 	private Door door;
 	private float doorX;
 	private float doorY;
 	private float playerX;
 	private float playerY;
 	private TimeData timeData;
 
 	public GameMap(BackGround back, float doorX, float doorY, float playerX,
 			float playerY, float gravityMag) {
 		this.gravityMag = gravityMag;
 		this.world = new World(new Vec2(0.0f, -gravityMag));
 		this.gameElements = new ArrayList<Entity>();
 		this.time = new Time(this);
 		this.back = back;
 		this.width = back.getWidth();
 		this.height = back.getHeight();
 		this.door = new Door(doorX, doorY);
 		this.doorX = doorX;
 		this.doorY = doorY;
 		this.playerX = playerX;
 		this.playerY = playerY;
 		this.originalDataLoc = "";
 		this.timeData = new TimeData();
 	}
 
 	public synchronized TimeData getTimeData() {
 		return timeData;
 	}
 
 	public synchronized ArrayList<Entity> getElements() {
 		return gameElements;
 	}
 
 	public synchronized BackGround getBack() {/*
 											 * , you don't know what you're
 											 * dealing with
 											 */
 		return back;
 	}
 
 	public synchronized Door getDoor() {
 		return door;
 	}
 
 	public synchronized float getWidth() {
 		return width;
 	}
 
 	public synchronized float getHeight() {
 		return height;
 	}
 
 	public synchronized double getPHeight() {
 		return back.getPHeight();
 	}
 
 	public synchronized double getPWidth() {
 		return back.getWidth();
 	}
 	public synchronized void setTimeData(TimeData data){
 		this.timeData = data;
 	}
 
 	public synchronized Boolean isPaused() {
 		return time.isPaused();
 	}
 
 	public synchronized void startTime() {
 		time.startTime();
 	}
 
 	public synchronized void newTime() {
 		this.time = new Time(this);
 	}
 
 	public synchronized void newReverseTime() {
 		this.time = new ReverseTime(this);
 	}
 
 	public synchronized void killTime() {
 		time.killTime();
 	}
 
 	public synchronized Time getTime() {
 		return time;
 	}
 
 	public synchronized World getPhysics() {
 		return world;
 	}
 
 	public synchronized float getPX() {
 		return playerX;
 	}
 
 	public synchronized float getPY() {
 		return playerY;
 	}
 
 	public synchronized void reset() throws IOException {
 		App.game.getCurrentMap().killTime();
 		//stopAll();
 		removeAll();
 		if (originalDataLoc != null) {
 			String raw = Parse.readFromFile(originalDataLoc);
 			String[] parsed = raw.split("[\n]");
 			for (int i = 6; i < parsed.length - 1; i++) {
 				parseElements(parsed[i]).addToMap(this);
 			}
 		} else {
 			addCoreElements();
 		}
 		App.game.getCurrentMap().setVisible(false);
 		App.camera.reset();
 		App.game.setPlayer(new Player(App.game.getCurrentMap().getPX(),
 				App.game.getCurrentMap().getPY()));
 		App.game.getPlayer().addToMap(App.game.getCurrentMap());
 		App.game.getCurrentMap().setVisible(true);
 		App.root.getChildren().removeAll(App.menuBar);
 		App.root.getChildren().addAll(App.menuBar);
 		App.game.getCurrentMap().startTime();
 	}
 
 	public synchronized void removeAll() {
 		/*for (int i = 0; i < gameElements.size(); i++){
 			
 		}*/
 		gameElements.clear();
 		Body body = world.getBodyList();
 		while(body.getNext() != null){
 			world.destroyBody(body);
 			body = body.getNext();
 		}
 		world.destroyBody(body);
 		/*
 		 * this.getBack().setVisible(false); for (Entity a : gameElements) {
 		 * a.removeFromMap(); }
 		 */
 	}
 
 	public synchronized void setVisible(Boolean bool) {
 		if (bool == true) {
 			back.setVisible(true);
 			for (Entity a : gameElements) {
 				a.setVisible(true);
 			}
 			door.setVisible(true);
 			App.pS.setScene(App.scene);
 			App.pS.show();
 		} else {
 			back.setVisible(false);
 			for (Entity a : gameElements) {
 				a.setVisible(false);
 			}
 			door.setVisible(false);
 		}
 	}
 
 	private synchronized void stopAll() {
 		time.timeline.stop();
 	}
 
 	// Use Entity's addToMap method. DO NOT DIRECTLY INVOKE THIS METHOD.
 	public synchronized void addEntity(Entity entity) {
 		gameElements.add(entity);
 	}
 
 	// Use Entity's removeFromMap method. DO NOT DIRECTLY CALL THIS METHOD.
 	public synchronized void removeEntity(Entity entity) {
 		gameElements.remove(entity);
 	}
 
 	public synchronized void addCoreElements() {
 		Wall left = new Wall(0, height / 2, 1, height);
 		Wall right = new Wall(width, height / 2, 1, height);
 		Wall top = new Wall(width / 2, height, width, 1);
 		Wall bottom = new Wall(width / 2, 0, width, 1);
 		left.addToMap(this);
 		right.addToMap(this);
 		top.addToMap(this);
 		bottom.addToMap(this);
 	}
 	public synchronized void addLeftRightWalls(){
 		Wall left = new Wall(0, height / 2, 1, height);
 		Wall right = new Wall(width, height / 2, 1, height);
 		left.addToMap(this);
 		right.addToMap(this);
 	}
 
 	public synchronized void toggleTime() {
 		time.toggleTime();
 	}
 
 	private synchronized static Entity parseElements(String raw) {
 		String[] frags = Parse.fragment(raw);
 		String className = frags[0];
 		switch (className) {
 		case "class entities.BouncyBall":
 			return BouncyBall.parse(frags);
 		case "class entities.Creature":
 			return Creature.parse(frags);
 		case "class entities.Projectile":
 			return Projectile.parse(frags);
 		case "class entities.Wall":
 			return Wall.parse(frags);
 		case "class entities.Floor":
 			return Floor.parse(frags);
 		case "class entities.StaticPathEntity":
 			return StaticPathEntity.parse(frags);
 		case "class entities.DynamicPathEntity":
 			return DynamicPathEntity.parse(frags);
		case "class entities.DeadlyStaticPathEntity":
 			return DeadlyStaticPathEntity.parse(frags);
		case "class entities.DeadlyDynamicPathEntity":
 			return DeadlyDynamicPathEntity.parse(frags);
 		case "class entities.DeadlyBouncyBall":
 			return DeadlyBouncyBall.parse(frags);
 		default:
 			return null;
 		}
 	}
 
 	public synchronized static GameMap parse(String raw, File loc) {
 		String[] parsed = raw.split("[\n]");
 		GameMap game = new GameMap(BackGround.parse(parsed[0]),
 				Float.parseFloat(parsed[1]), Float.parseFloat(parsed[2]),
 				Float.parseFloat(parsed[3]), Float.parseFloat(parsed[4]),
 				Float.parseFloat(parsed[5]));
 		for (int i = 6; i < parsed.length; i++) {
 			parseElements(parsed[i]).addToMap(game);
 		}
 		game.setOriginalDataLoc(loc.getPath());
 		return game;
 	}
 
 	public synchronized void setOriginalDataLoc(String loc) {
 		this.originalDataLoc = loc;
 	}
 
 	public synchronized String getOriginalDataLoc() {
 		return originalDataLoc;
 	}
 
 	@Override
 	public synchronized String toString() {
 		String result = "";
 		result += back.toString() + "\n";
 		result += doorX + "\n";
 		result += doorY + "\n";
 		result += playerX + "\n";
 		result += playerY + "\n";
 		result += gravityMag + "\n";
 
 		for (Entity a : gameElements) {
 			if (a != App.game.getPlayer())
 				result += a.toString() + "\n";
 		}
 		return result;
 	}
 
 	public void newPIReverseTime() {
 		this.time = new ReverseTime2(this);
 	}
 }
