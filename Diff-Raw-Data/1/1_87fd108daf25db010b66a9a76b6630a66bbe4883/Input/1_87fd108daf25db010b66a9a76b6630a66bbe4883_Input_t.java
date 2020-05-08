 package net.fourbytes.shadow;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.concurrent.ConcurrentHashMap;
 
 import net.fourbytes.shadow.Input.TouchPoint.TouchMode;
 
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.math.Vector3;
 import com.badlogic.gdx.utils.Array;
 import com.badlogic.gdx.utils.ObjectMap;
 
 public class Input {
 	
 	public static class TouchPoint {
 		public static enum TouchMode {
 			KeyInput, 
 			Cursor;
 		}
 		TouchMode touchmode;
 		int id = -1;
 		int button = -1;
 		Vector2 pos = new Vector2(-1f, -1f);
 		public TouchPoint(int x, int y, int id, int button, TouchMode touchmode) {
 			this.id = id;
 			this.button = button;
 			this.pos.set(x, y);
 			this.touchmode = touchmode;
 			if (touchmode == TouchMode.Cursor && Shadow.level != null && Shadow.level.player != null && Input.isAndroid && !Input.isOuya) {
 				this.button = -1; //Special button id for Android
 				Shadow.level.fillLayer(0);
 				Cursor c = new Cursor(new Vector2(0f, 0f), Shadow.level.layers.get(0), id);
 				c.pos.set(c.calcPos(pos));
 				Shadow.level.cursors.add(c);
 			}
 		}
 	}
 
 	public static interface KeyListener {
 		public void keyDown(Key key);
 		public void keyUp(Key key);
 	}
 
 	static OrthographicCamera cam;
 	
 	public static boolean isAndroid = false;
 	public static boolean isOuya = false;
 	public static boolean isInMenu = false;
 	
 	public static class Key {
 		public static final class Triggerer {
 			public static final int KEYBOARD = 0;
 			public static final int SCREEN = 0;//Screen simulates keyboard presses and as for now shares same ID.
 			public static final int CONTROLLER_BUTTON = 1;
 			public static final int CONTROLLER_AXIS = 2;
 		}
 
 		public String name;
 		public int[] keyid;
 		Rectangle disprec;
 		Rectangle rec;
 		public Array<KeyListener> listeners = new Array<KeyListener>();
 		public Array<KeyListener> tmp;
 		public Array<KeyListener> tmp2;
 		int pointer = -1;
 		int triggerer = 0;
 		
 		public boolean wasDown = false;
 		public boolean isDown = false;
 		public boolean nextState = false;
 		
 		public Key(String name, int[] keyid, Rectangle rec) {
 			this.name = name;
 			this.keyid = keyid;
 			this.rec = new Rectangle(rec);
 			this.disprec = new Rectangle(rec);
 			all.add(this);
 			
 			tmp = Garbage.keys;
 			tmp2 = Garbage.keys2;
 		}
 		
 		public void down() {
 			//System.out.println("N: "+name+"; M: D; X: "+rec.x+"; Y:"+rec.y+"; W: "+rec.width+"; H: "+rec.height);
 			tmp.clear();
 			tmp.addAll(listeners);
 			for (KeyListener l : tmp) {
 				if (l instanceof Level && ((Level)l) != Shadow.level) {
 					continue;
 				}
 				if (l instanceof GameObject && ((GameObject)l).layer.level != Shadow.level) {
 					continue;
 				}
 				l.keyDown(this);
 			}
 		}
 		
 		public void up() {
 			//System.out.println("N: "+name+"; M: U; X: "+rec.x+"; Y:"+rec.y+"; W: "+rec.width+"; H: "+rec.height);
 			tmp.clear();
 			tmp.addAll(listeners);
 			for (KeyListener l : tmp) {
 				if (l instanceof Level && ((Level)l) != Shadow.level) {
 					continue;
 				}
 				if (l instanceof GameObject && ((GameObject)l).layer.level != Shadow.level) {
 					continue;
 				}
 				l.keyUp(this);
 			}
 		}
 		
 		public void tick() {
 			wasDown = isDown;
 			isDown = nextState;
 			
 			rec.width = Shadow.dispw/Shadow.touchw;
 			rec.x = disprec.x*Shadow.dispw/Shadow.touchw;
 			rec.height = Shadow.disph/Shadow.touchh;
 			rec.y = disprec.y*Shadow.disph/Shadow.touchh;
 			
 			if (wasPressed()) up();
 			if (wasReleased()) down();
 			
 			tmp.clear();
 			tmp2.clear();
 			tmp.addAll(listeners);
 			for (KeyListener kl : tmp) {
 				if (tmp2.contains(kl, true)) {
 					continue;
 				}
 				tmp2.add(kl);
 				
 				if (kl == null) {
 					listeners.removeValue(kl, true);
 					continue;
 				}
 				
 				check(kl);
 			}
 		}
 		
 		public void check(KeyListener kl) {
 			if (kl instanceof Level) {
 				Level l = (Level) kl;
 				Level sl = Shadow.level;
 				
 				if (!(sl instanceof MenuLevel)) {
 					if (l instanceof MenuLevel) {
 						MenuLevel ml = (MenuLevel) l;
 						if (ml != sl) {
 							listeners.removeValue(kl, true);
 						}
 					} else {
 						if (sl instanceof MenuLevel) {
 							MenuLevel ml = (MenuLevel) sl;
 							if (l != ml.bglevel) {
 								listeners.removeValue(kl, true);
 							}
 						} else {
 							listeners.removeValue(kl, true);
 						}
 					}
 				}
 			}
 			if (kl instanceof GameObject) {
 				GameObject go = (GameObject) kl;
 				Level clevel = Shadow.level;
 				if (clevel instanceof MenuLevel) {
 					clevel = ((MenuLevel)clevel).bglevel;
 				}
 				if (go instanceof Entity) {
 					Entity e = (Entity) go;
 					if (e.layer == null || !e.layer.entities.contains(e, true) || clevel != e.layer.level) {
 						listeners.removeValue(kl, true);
 					}
 				}
 				if (go instanceof Block) {
 					Block b = (Block) go;
 					if (b.layer == null || !b.layer.blocks.contains(b, true) || clevel != b.layer.level) {
 						listeners.removeValue(kl, true);
 					}
 				}
 			}
 		}
 
 		public void render() {
 			ShapeType type = Shadow.shapeRenderer.getCurrentType();
 			Shadow.shapeRenderer.end();
 			Shadow.shapeRenderer.begin(ShapeType.Line);
 			if (isDown) {
 				Shadow.shapeRenderer.setColor(new Color(1, 0.5f, 0.5f, 1));
 			} else {
 				Shadow.shapeRenderer.setColor(new Color(1, 1, 1, 1));
 			}
 			Shadow.shapeRenderer.rect(disprec.x, disprec.y, 1f, 1f);
 			Shadow.shapeRenderer.end();
 			Shadow.shapeRenderer.begin(type);
 		}
 		
 		public boolean wasPressed() {
 			return wasDown && !isDown;
 		}
 		
 		public boolean wasReleased() {
 			return !wasDown && isDown;
 		}
 	}
 	
 	public static Array<Key> all = new Array<Key>();
 	
 	public static Key up = new Key("Up", new int[] {Keys.UP, Keys.W}, new Rectangle(-1, -1, -1, -1));
 	public static Key jump = new Key("Jump", new int[] {Keys.UP, Keys.W}, new Rectangle(-1, -1, -1, -1));
 	public static Key down = new Key("Down", new int[] {Keys.DOWN, Keys.S}, new Rectangle(-1, -1, -1, -1));
 	public static Key left = new Key("Left", new int[] {Keys.LEFT, Keys.A}, new Rectangle(-1, -1, -1, -1));
 	public static Key right = new Key("Right", new int[] {Keys.RIGHT, Keys.D}, new Rectangle(-1, -1, -1, -1));
 	
 	public static Key pause = new Key("Pause", new int[] {Keys.ESCAPE}, new Rectangle(-1, -1, -1, -1));
 	public static Key enter = new Key("Confirm", new int[] {Keys.ENTER}, new Rectangle(-1, -1, -1, -1));
 	
 	public static Key androidBack = new Key("Back", new int[] {Keys.BACK}, new Rectangle(-1, -1, -1, -1));
 	public static Key androidMenu = new Key("Menu", new int[] {Keys.MENU}, new Rectangle(-1, -1, -1, -1));
 	
 	public static ObjectMap<Integer, TouchPoint> touches = new ObjectMap<Integer, TouchPoint>();
 	
 	public static void setUp() {
 		for (Key k : all) {
 			if (k.rec.x < 0) {
 				k.rec.x = -k.rec.x;
 			}
 			if (k.rec.y < 0) {
 				k.rec.y = -k.rec.y;
 			}
 		}
 		
 		resize();
 	}
 	
 	public static void resize() {
 		cam = new OrthographicCamera(Shadow.touchw, -Shadow.touchh);
 		cam.position.set(Shadow.touchw/2, Shadow.touchh/2, 0);
 		cam.update();
 		
 		//up.disprec = new Rectangle(Shadow.touchw-2, Shadow.touchh-3, 1, 1);
 		jump.disprec = new Rectangle(Shadow.touchw-2, Shadow.touchh-3, 1, 1);
		up.disprec = new Rectangle(Shadow.touchw-2, Shadow.touchh-3, 1, 1);
 		down.disprec = new Rectangle(Shadow.touchw-2, Shadow.touchh-2, 1, 1);
 		left.disprec = new Rectangle(1, Shadow.touchh-2, 1, 1);
 		right.disprec = new Rectangle(3, Shadow.touchh-2, 1, 1);
 	}
 	
 	public static void tick() {
 		for (Key k :all) {
 			k.tick();
 		}
 	}
 	
 	public static void render() {
 		Shadow.shapeRenderer.setProjectionMatrix(cam.combined);
 		Shadow.shapeRenderer.begin(ShapeType.Line);
 		if (isAndroid && !isOuya && !isInMenu) {
 			for (Key k : all) {
 				k.render();
 			}
 		}
 		Shadow.shapeRenderer.end();
 	}
 	
 }
