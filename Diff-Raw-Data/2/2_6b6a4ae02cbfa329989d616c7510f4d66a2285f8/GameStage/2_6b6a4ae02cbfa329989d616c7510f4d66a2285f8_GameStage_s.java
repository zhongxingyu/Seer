 package com.yad.harpseal.gameobj.stage;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.Queue;
 
 import android.graphics.Canvas;
 import android.graphics.Paint;
 
 import com.yad.harpseal.constant.Direction;
 import com.yad.harpseal.constant.Layer;
 import com.yad.harpseal.constant.Screen;
 import com.yad.harpseal.constant.TileType;
 import com.yad.harpseal.gameobj.GameObject;
 import com.yad.harpseal.gameobj.SampleField;
 import com.yad.harpseal.gameobj.character.Fish;
 import com.yad.harpseal.gameobj.character.GoalFlag;
 import com.yad.harpseal.gameobj.character.PlayerSeal;
 import com.yad.harpseal.gameobj.tile.NormalTile;
 import com.yad.harpseal.gameobj.tile.RotatableTile;
 import com.yad.harpseal.gameobj.window.Joystick;
 import com.yad.harpseal.util.Communicable;
 import com.yad.harpseal.util.Func;
 import com.yad.harpseal.util.HarpEvent;
 import com.yad.harpseal.util.HarpLog;
 
 public class GameStage extends GameObject {
 	
 	// sample map structure
 	private final static String[] tileSample= {
 		"11252234",
 		"41342235",
 		"51114123",
 		"01423234",
 		"01511132",
 		"02341551",
 		"13231212",
 		"31213112",
 		"02314541",
 	};
 	private final static String[] charSample= {
 		"13000000",
 		"00000000",
 		"00000000",
 		"00000000",
 		"00000000",
 		"00000000",
 		"00030030",
 		"00000000",
 		"00000002"
 	};
 	
 	// map
 	private String[] tileString;
 	private String[] charString;
 	private int mapWidth,mapHeight;
 
 	// camera
 	private float cameraX,cameraY;
 
 	// objects
 	private ArrayList<GameObject> tiles;
 	private ArrayList<GameObject> characters;
 	private Queue<GameObject> removed;
 	private SampleField field;
 	private Joystick stick;
 	private PlayerSeal player;	// also exists in 'characters'
 	
 	// score data
 	private int stepCount;
 	private int fishCount;
 	private final static int FISH_MAX=3;
 	
 	
 	//// public method
 	
 	
 	public GameStage(Communicable con) {
 		super(con);
 		
 		// user interface, etc.
 		field=new SampleField(this);
 		stick=new Joystick(this);
 		player=null;
 		
 		// map vaildity check... later
 		tileString=tileSample;
 		charString=charSample;
 		////
 		
 		// read map
 		mapWidth=tileString[0].length();
 		mapHeight=tileString.length;
 		tiles=new ArrayList<GameObject>();
 		characters=new ArrayList<GameObject>();
 		removed=new LinkedList<GameObject>();
 		for(int y=0;y<mapHeight;y++) {
 			for(int x=0;x<mapWidth;x++) {
 				
 				// create tiles
 				switch(tileString[y].charAt(x)) {
 				case '0': break;
 				case '1': tiles.add(new NormalTile(this,x,y)); break;
 				case '2': tiles.add(new RotatableTile(this,x,y,TileType.RT_BRIDGE)); break;
 				case '3': tiles.add(new RotatableTile(this,x,y,TileType.RT_CORNER)); break;
 				case '4': tiles.add(new RotatableTile(this,x,y,TileType.RT_FORK)); break;
 				case '5': tiles.add(new RotatableTile(this,x,y,TileType.RT_INTERSECTION)); break;
 				default: HarpLog.danger("Invalid tile type"); break;
 				}
 				
 				// create characters
 				switch(charString[y].charAt(x)) {
 				case '0': break;
 				case '1':
 					player=new PlayerSeal(this,x,y);
 					characters.add(player);
 					break;
 					
 				case '2': characters.add(new GoalFlag(this,x,y)); break;
 				case '3': characters.add(new Fish(this,x,y)); break;
 				default: HarpLog.danger("Invalid character type"); break;
 				}
 			}
 		}
 		
 		// camera point init
 		cameraX=0;
 		cameraY=0;
 		regulateCamera(player);
 		
 		// score data init
 		stepCount=0;
 		fishCount=0;
 	}
 
 	@Override
 	public void playGame(int ms) {
 		for(GameObject o : tiles)
 			o.playGame(ms);
 		for(GameObject o : characters)
 			o.playGame(ms);
 		field.playGame(ms);
 		stick.playGame(ms);
 		regulateCamera(player);
 		tiles.remove(removed.peek());
 		characters.remove(removed.poll());
 	}
 
 	@Override
 	public void receiveMotion(HarpEvent ev, int layer) {
 
 		// camera setting
 		if(layer==Layer.LAYER_FIELD) ev.setCamera(cameraX, cameraY);
 		else if(layer==Layer.LAYER_WINDOW) ev.setCamera(0, 0);
 		
 		for(GameObject o : tiles) {
 			o.receiveMotion(ev, layer);
 			if(ev.isProcessed()) return;
 		}
 		for(GameObject o : characters) {
 			o.receiveMotion(ev, layer);
 			if(ev.isProcessed()) return;
 		}
 		field.receiveMotion(ev, layer);
 		if(ev.isProcessed()) return;
 		stick.receiveMotion(ev, layer);
 	}
 
 	@Override
 	public void drawScreen(Canvas c, Paint p, int layer) {
 		
 		// camera setting
 		if(layer==Layer.LAYER_TILE) c.translate(-cameraX,-cameraY);
 		else if(layer==Layer.LAYER_WINDOW) c.translate(cameraX,cameraY);
 		
 		for(GameObject o : tiles)
 			o.drawScreen(c, p, layer);
 		for(GameObject o : characters)
 			o.drawScreen(c, p, layer);
 		field.drawScreen(c, p, layer);
 		stick.drawScreen(c, p, layer);
 	}
 
 	@Override
 	public void restoreData() {
 		for(GameObject o : tiles)
 			o.restoreData();
 		for(GameObject o : characters)
 			o.restoreData();
 		field.restoreData();
 		stick.restoreData();
 		player=null;
 	}
 
 	@Override
 	public int send(String msg) {
 		String[] msgs=msg.split("/");
 
 		if(msgs[0].equals("stickAction")) {
 
 			if(player==null) return 0;
 			else if(movableCheck(player,Integer.parseInt(msgs[1]))==true) {
 				player.send("move/"+msgs[1]);
 				return 1;
 			} else return 0;
 		}
 		else if(msgs[0].equals("rotatableCheck")) {
 			
 			if(rotatableCheck(Integer.parseInt(msgs[1]),Integer.parseInt(msgs[2]))==true) return 1;
 			else return 0;
 		}
 		else if(msgs[0].equals("moved")) {
 			if(msgs[1].equals("PlayerSeal")) {
 				stepCount+=1;
 				eatFish(Integer.parseInt(msgs[2]), Integer.parseInt(msgs[3]));
 			}
 			return 1;
 		}
 		return con.send(msg);
 	}
 
 	@Override
 	public Object get(String name) {
 		if(name.equals("stepCount"))
 			return stepCount;
 		else if(name.equals("fishCount"))
 			return fishCount;
 		else
 			return con.get(name);
 	}
 	
 	
 	//// private method (game play)
 	
 	
 	private void regulateCamera(GameObject target) {
 		
 		if(target == null) return;
 		
 		// whole character
 		cameraX=(Integer)target.get("mapX")*Screen.TILE_LENGTH+Screen.TILE_LENGTH/2+Screen.FIELD_MARGIN_LEFT-Screen.SCREEN_X/2;
 		cameraY=(Integer)target.get("mapY")*Screen.TILE_LENGTH+Screen.TILE_LENGTH/2+Screen.FIELD_MARGIN_TOP-Screen.SCREEN_Y/2;
 		
 		// player seal (movable character)
 		if(target.getClass()==PlayerSeal.class) {
 			int pDirection=(Integer)target.get("moveDirection");
 			if(pDirection!=Direction.NONE) {
 				float value=(float)Screen.TILE_LENGTH*(Integer)target.get("moveTime")/(Integer)target.get("moveValue")-Screen.TILE_LENGTH;
 				switch(pDirection) {
 				case Direction.LEFT: cameraX-=value; break;
 				case Direction.RIGHT: cameraX+=value; break;
 				case Direction.UP: cameraY-=value; break;
 				case Direction.DOWN: cameraY+=value; break;
 				}
 			}
 		}
 		
 		// limit
 		cameraX=Func.limit(cameraX, 0, mapWidth*Screen.TILE_LENGTH+Screen.FIELD_MARGIN_LEFT*2-Screen.SCREEN_X);
 		cameraY=Func.limit(cameraY, 0,  mapHeight*Screen.TILE_LENGTH+Screen.FIELD_MARGIN_TOP*2-Screen.SCREEN_Y);
 	}
 	
 	private boolean movableCheck(GameObject target,int direction) {
 		
 		// start point check
 		int mapX=(Integer)target.get("mapX");
 		int mapY=(Integer)target.get("mapY");
 		for(GameObject o : tiles) {
 			if( (Integer)o.get("mapX")==mapX &&
 				(Integer)o.get("mapY")==mapY) {
 				
 				// tile type
 				if(o.getClass()==NormalTile.class)
 					continue;
 				else if(o.getClass()==RotatableTile.class) {
 					int tileType=(Integer)o.get("type");
 					int tileDirection=(Integer)o.get("direction");
 					if(direction==tileDirection) continue;
 					switch(tileType) {
 					case TileType.RT_BRIDGE: if(direction!=Direction.reverse(tileDirection)) return false; break;
 					case TileType.RT_CORNER: if(direction!=Direction.clockwise(tileDirection)) return false; break;
 					case TileType.RT_FORK: if(direction!=Direction.clockwise(tileDirection) && direction!=Direction.clockwiseR(tileDirection)) return false; break;
 					case TileType.RT_INTERSECTION: break;
 					default: HarpLog.error("Invalid Tile Type : "+tileType); return false;
 					}
 				}
 			}
 		}
 		
 		// destination check
 		switch(direction) {
 		case Direction.UP: mapY-=1; break;
 		case Direction.LEFT: mapX-=1; break;
 		case Direction.RIGHT: mapX+=1; break;
 		case Direction.DOWN: mapY+=1; break;
 		default: HarpLog.error("Invalid Direction : "+direction); return false;
 		}
 		for(GameObject o : tiles) {
 			if( (Integer)o.get("mapX")==mapX &&
 				(Integer)o.get("mapY")==mapY) {
 
 				// tile type
 				if(o.getClass()==NormalTile.class)
 					return true;
 				else if(o.getClass()==RotatableTile.class) {
 					int tileType=(Integer)o.get("type");
 					int tileDirection=(Integer)o.get("direction");
 					if(direction==Direction.reverse(tileDirection)) return true;
 					switch(tileType) {
 					case TileType.RT_BRIDGE: return (direction==tileDirection);
 					case TileType.RT_CORNER: return (direction==Direction.clockwiseR(tileDirection));
 					case TileType.RT_FORK: return (direction==Direction.clockwiseR(tileDirection) || direction==Direction.clockwise(tileDirection));
 					case TileType.RT_INTERSECTION: return true;
 					default: HarpLog.error("Invalid Tile Type : "+tileType); return false;
 					}
 				}
 				
 			}
 		}
 		return false;
 	}
 	
 	private boolean rotatableCheck(int x,int y) {
 		
 		for(GameObject o : characters)
 			if(o.getClass()==PlayerSeal.class && (Integer)o.get("mapX")==x && (Integer)o.get("mapY")==y)
 				return false;
 		return true;
 	}
 	
 	private void eatFish(int x,int y) {
 
 		for(GameObject o : characters) {
 			if(o.getClass()==Fish.class && (Integer)o.get("mapX")==x && (Integer)o.get("mapY")==y) {
 				o.send("eaten");
 				fishCount+=1;
 				preRemoveObject(o);
 				break;
 			}
 		}
 	}
 	
 	
 	//// private (data control)
 	
 	private void preRemoveObject(GameObject o) {
 		o.restoreData();
 		removed.add(o);
 	}
 
 
 
 
 
 }
