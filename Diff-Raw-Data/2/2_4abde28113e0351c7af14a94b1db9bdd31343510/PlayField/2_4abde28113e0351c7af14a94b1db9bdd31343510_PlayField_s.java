 package cs447.PuzzleFighter;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.Random;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import jig.engine.RenderingContext;
 import jig.engine.util.Vector2D;
 
 public class PlayField {
 	public static final Vector2D    UP = new Vector2D( 0, -1);
 	public static final Vector2D  DOWN = new Vector2D( 0, +1);
 	public static final Vector2D  LEFT = new Vector2D(-1,  0);
 	public static final Vector2D RIGHT = new Vector2D(+1,  0);
 	public static final Gem WALL = new WallGem();
 	private final Vector2D START_TOP;
 	private final Vector2D START_BOT;
 
 	private final static Color[] colors = new Color[] { Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW };
 	private Random randSrc = new Random();
 
 	private int width;
 	private int height;
 	private int turnScore;
 	private int gemCount;
 	private boolean secondary;
 	private Socket socket;
 	private ObjectInputStream ois;
 	private ObjectOutputStream oos;
 	private ColoredGem[][] grid;
 	private GemPair cursor;
 
 	private long inputTimer = 0;
 	private long renderTimer = 0;
 	private short updateCount = 0;
 
 	public int garbage;
 
 	private RobotMaster fighter;
 	
 	private Color randomColor() {
 		return colors[randSrc.nextInt(colors.length)];
 	}
 
 	private ColoredGem randomGem(Vector2D pos) {
 		if(gemCount % 25 == 0 && gemCount != 0){
 			gemCount++;
 			return new Diamond(this, pos, Color.RED);
 		}
 		if (randSrc.nextFloat() > 0.25) {
 			gemCount++;
 			return new PowerGem(this, pos, randomColor());
 		}
 		else {
 			gemCount++;
 			return new CrashGem(this, pos, randomColor());
 		}
 	}
 
 	public PlayField(int width, int height, Socket socket, boolean secondary) throws IOException {
 		this.width = width;
 		this.height = height;
 		this.grid = new ColoredGem[height][width];
 		this.turnScore = 0;
 		this.garbage = 0;
 		this.socket = socket;
 		this.secondary = secondary;
 		START_TOP = new Vector2D(width/2, 0);
 		START_BOT = START_TOP.translate(DOWN);
 		if(socket == null){
 			this.cursor = new GemPair(randomGem(START_BOT), new PowerGem(this, START_TOP, Color.RED));
 		}
 		if(socket != null){
 			if(!secondary){
 				OutputStream os = socket.getOutputStream();
 				oos = new ObjectOutputStream(os);
 				oos.flush();
 			}
 			if(secondary){
 				InputStream is = socket.getInputStream();
 				ois = new ObjectInputStream(is);
 			}
 		}
 		System.out.println("done creating");
 		//this.cursor = new GemPair(new Diamond(this, START_BOT, Color.RED), new PowerGem(this, START_TOP, Color.RED));
 		this.fighter = !secondary ? new CutMan() : new MegaMan();
 		this.secondary = secondary;
 	}
 
 	public int getWidth() {
 		return width;
 	}
 
 	public int getHeight() {
 		return height;
 	}
 	
 	public void render(RenderingContext rc) {
 		for (int y = 0; y < height; y++) {
 			for (int x = 0; x < width; x++) {
 				Gem g = grid[y][x];
 				if (g != null && (cursor == null || !cursor.contains(g))) {
 					g.render(rc);
 				}
 			}
 		}
 		if (cursor != null) {
 			cursor.render(rc);
 		}
 		fighter.render(rc, !secondary);
 	}
 
 	public Gem ref(Vector2D pos) {
 		int x = (int) pos.getX();
 		int y = (int) pos.getY();
 		if (x < 0 || x >= width || y < 0 || y >= height) {
 			return WALL;
 		}
 
 		return grid[y][x];
 	}
 
 	public void set(Vector2D pos, ColoredGem g) {
 		int x = (int) pos.getX();
 		int y = (int) pos.getY();
 
 		grid[y][x] = g;
 	}
 
 	public void clear(Vector2D pos) {
 		set(pos, null);
 	}
 
 	public boolean isFilled(Vector2D pos) {
 		return (ref(pos) != null);
 	}
 
 	public boolean move(Vector2D dv) {
 		return cursor.move(dv);
 	}
 
 	public void step() {
 		if (!cursor.move(DOWN)) {
 			cursor = null;
 			stepTimers();
 		}
 	}
 
 	public boolean fall() {
 		boolean hadEffect = false;
 
 		for (int y = height-1; y >= 0; y--) {
 			for (int x = 0; x < width; x++) {
 				if (grid[y][x] != null) {
 					hadEffect |= grid[y][x].move(DOWN);
 				}
 			}
 		}
 
 		return hadEffect;
 	}
 
 	public int crashGems() {
 		int crashScore = 0;
 
 		for (int y = height-1; y >= 0; y--) {
 			for (int x = 0; x < width; x++) {
 				ColoredGem g = grid[y][x];
 				if (g != null) {
 					crashScore += g.endTurn();
 				}
 			}
 		}
 
 		return crashScore;
 	}
 	
 	public int crashDiamonds(){
 		int crashScore = 0;
 		for(int y = height -1; y >= 0; y--){
 			for(int x = 0; x < width; x++){
 				if(grid[y][x] != null && grid[y][x] instanceof Diamond){
 					crashScore += grid[y][x].endTurn();
 				}
 			}
 		}
 		return crashScore;
 	}
 
 	public void stepTimers() {
 		for (int y = height-1; y >= 0; y--) {
 			for (int x = 0; x < width; x++) {
 				ColoredGem g = grid[y][x];
 				if (g instanceof TimerGem) {
 					TimerGem tg = (TimerGem) g;
 					if (tg.stepTimer()) {
 						grid[y][x] = new PowerGem(this, tg.pos, tg.color);
 					}
 				}
 			}
 		}
 	}
 	
 	public void combine(){
 		for(int x = 0; x < width; x++){
 			for(int y = 0; y < height; y++){
 				if(grid[y][x] instanceof PowerGem){
 					((PowerGem)grid[y][x]).combine();
 				}
 			}
 		}	
 	}
 	
 	public boolean gravitate() {
 		if (fall()) {
 			return true;
 		}
 		int crashScore = crashDiamonds();
 		crashScore += crashGems();
 		if (crashScore != 0) {
 			turnScore += crashScore;
 			return true;
 		}
 
 		return false;
 	}
 	
 	public int update(long deltaMs, boolean down, boolean left, boolean right, boolean ccw, boolean cw) {
 		updateCount++;
 		fighter.update(deltaMs);
 		if(socket == null || (socket != null && !secondary)){
 			renderTimer += deltaMs;
 			inputTimer += deltaMs;
 
 			if (inputTimer > 100) {
 				inputTimer = 0;
 				if (cursor != null) {
 
 					if (ccw && !cw) {
 						cursor.rotateCounterClockwise();
 					}
 					
 					if (cw && !ccw) {
 						cursor.rotateClockwise();
 					}
 					if (down) {
 						move(PlayField.DOWN);
 					}
 					if (left && !right) {
 						move(PlayField.LEFT);
 					}
 					if (right && !left) {
 						move(PlayField.RIGHT);
 					}
 				}
 			}
 
 			if (cursor != null && renderTimer > 500) {
 				renderTimer = 0;
 				step();
 			}
 			if (cursor == null && renderTimer > 100) {
 				renderTimer = 0;
 				boolean moreToDo = gravitate();
 				combine();
 				if (!moreToDo) {
 					if (garbage > 0) {
 						garbage /= 2;
 						for (int i = 0; i < garbage; i++) {
 							grid[i / width][i % width] = new TimerGem(this, new Vector2D(i%width,i/width), Color.RED);
 						}
 						garbage = 0;
 						fighter.attack();
 						if(socket != null){
 							try {
 								oos.writeInt(1);
 							} catch (IOException ex) {
 								Logger.getLogger(PlayField.class.getName()).log(Level.SEVERE, null, ex);
 							}
 							netsend(0, true);
 						}
 						return 0;
 					}
 					else {
 						cursor = new GemPair(randomGem(START_BOT), randomGem(START_TOP));
 						//cursor = new GemPair(new PowerGem(this, START_BOT, Color.RED), new PowerGem(this, START_TOP, Color.RED));
 						int tmp = turnScore;
 						turnScore = 0;
 						if(socket != null){
 							try {
 								oos.writeInt(1);
 							} catch (IOException ex) {
 								Logger.getLogger(PlayField.class.getName()).log(Level.SEVERE, null, ex);
 							}
 							netsend(tmp, false);
 						}
 						return tmp;
 					}
 				}
 			}
 			if(socket != null && updateCount > 20){
 				try {
 					oos.writeInt(1);
 				} catch (IOException ex) {
 					Logger.getLogger(PlayField.class.getName()).log(Level.SEVERE, null, ex);
 				}
 				netsend(0, false);
 				updateCount = 0;
 			}
 			return 0;
 		}else{
 			try {
 				if(ois.available() > 0){
 					ois.readInt();
 					Packet pack = null;
 					try {
 						pack = (Packet)ois.readObject();
 					} catch (IOException ex) {
 						Logger.getLogger(PlayField.class.getName()).log(Level.SEVERE, null, ex);
 					} catch (ClassNotFoundException ex) {
 						Logger.getLogger(PlayField.class.getName()).log(Level.SEVERE, null, ex);
 					}
 					for(int i = 0; i < height; i++){
 						for(int j = 0; j < width; j++){
 							grid[i][j] = null;
 						}
 					}
 					if(pack.attacking){
 						fighter.attack();
 					}
 					for(int i = 0; i < height; i++){
 						for(int j = 0; j < width; j++){
 							if(pack.grid[i][j] != null){
 								if(pack.grid[i][j].type.contentEquals("Crash")){
 									grid[i][j] = new CrashGem(this, new Vector2D(j, i), pack.grid[i][j].color);
 								}else if(pack.grid[i][j].type.contentEquals("Diamond")){
 									grid[i][j] = new Diamond(this, new Vector2D(j, i), Color.RED);
 								}else if(pack.grid[i][j].type.contentEquals("Power")){
 									grid[i][j] = new PowerGem(this, new Vector2D(j, i), pack.grid[i][j].color);
 								}else{
 									grid[i][j] = new TimerGem(this, new Vector2D(j, i), pack.grid[i][j].color);
 									((TimerGem)grid[i][j]).setFrame(pack.grid[i][j].frame);
 								}
 							}
 						}
 					}
 					return pack.garbage;
 				}
 			} catch (IOException ex) {
 				Logger.getLogger(PlayField.class.getName()).log(Level.SEVERE, null, ex);
 			}
 			return 0;
 		}
 	}
 	
 	public void netsend(int garb, boolean attacking){
 		ArrayList<ColoredGem> list = new ArrayList();
 		Packet thepacket = new Packet();
 		thepacket.garbage = garb;
 		thepacket.attacking = attacking;
 		thepacket.grid = new SerializableGem[height][width];
 		for(int i = 0; i < height; i++){
 			for(int j = 0; j < width; j++){
 				if(grid[i][j] != null && !list.contains(grid[i][j])){
 					thepacket.grid[i][j] = new SerializableGem();
 					thepacket.grid[i][j].x = (int)grid[i][j].pos.getX();
 					thepacket.grid[i][j].y = (int)grid[i][j].pos.getY();
 					thepacket.grid[i][j].color = grid[i][j].color;
 					if(grid[i][j] instanceof Diamond){
 						thepacket.grid[i][j].type = "Diamond";
 					}else if(grid[i][j] instanceof PowerGem){
 						thepacket.grid[i][j].type = "Power";
 						thepacket.grid[i][j].height = ((PowerGem)grid[i][j]).gemHeight;
 						thepacket.grid[i][j].width = ((PowerGem)grid[i][j]).gemWidth;
 					}else if(grid[i][j] instanceof CrashGem){
 						thepacket.grid[i][j].type = "Crash";
 					}else{
 						thepacket.grid[i][j].type = "Time";
 						thepacket.grid[i][j].frame = ((TimerGem)grid[i][j]).frame;
 					}
 					list.add(grid[i][j]);
 				}
 			}
 		}
 		try {
 			oos.writeObject(thepacket);
 		} catch (IOException ex) {
 			Logger.getLogger(PlayField.class.getName()).log(Level.SEVERE, null, ex);
 		}
 	}
 }
