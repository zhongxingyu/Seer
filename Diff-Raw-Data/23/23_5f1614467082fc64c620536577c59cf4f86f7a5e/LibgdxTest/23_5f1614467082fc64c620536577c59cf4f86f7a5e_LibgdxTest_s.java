 package android.game;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Random;
 
 import android.util.Log;
 
 import android.util.Log;
 
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.Audio;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.audio.Music;
 import com.badlogic.gdx.files.FileHandle;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.badlogic.gdx.input.GestureDetector;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 
 
 public class LibgdxTest implements ApplicationListener{
 	
 	private Stage stage;
 	private SpriteBatch batch;
 	Controller controller;
 	GestureDetector gestureDetector;
 	
 	Player player;
 	
 	ArrayList<Planet> planets;
 	Planet win;
 	
 	Audio audio;
 	static Music bambi;
 	
 	int levelEndWait = 0;
 	int maxLevelEndWait = 80;
 	
 	Sprite fuel;
 	
 	Camera camera;
 	
 	int x, y;
 	
 	int levelNum;
 	String levels = "levels";
 	
 	TiledSprite bg;
 	
 	ArrayList<ParticleSource> particleSources;
 	
 	boolean menuMode = true;
 	Menu currentMenu;
 	Menu herpMenu;
 	Menu levelMenu;
 	Menu mainMenu;
 	Menu creditsMenu;
 	
 	Sprite glowG, glowR, glowB;
 	
 	Sprite menuButton;
 	
 	BitmapFont terminus;
 
 	public void create() {	
 		
 		particleSources = new ArrayList<ParticleSource>();
 		
 		//particleSources.add(new ParticleSource(30, 3, 1, new Vector3D(.1, .1, 0), new Vector3D(0, 0, 0), 20, "spark_0.png"));
 		
 		stage = new Stage(0, 0, true);
 		batch = new SpriteBatch();
 		
 		controller = new Controller();
         controller.model = this;
         gestureDetector = new GestureDetector(controller);
         Gdx.input.setInputProcessor(gestureDetector);
         planets = new ArrayList<Planet>();
         
         player = new Player();
 
         loadLevel(1);
         
         audio = Gdx.audio;
         bambi = audio.newMusic(Gdx.files.internal("bambi.ogg"));
         //bambi.setLooping(true);
       
         fuel = new Sprite("fuelbar.png");
         fuel.height = 15;
         fuel.width = Gdx.graphics.getWidth();
         
        glowR = new Sprite("redGlow.png");
        glowG = new Sprite("greenGlow.png");
        glowG.width = glowG.height = 500;
        glowB = new Sprite("blueGlow.png");
 
         this.camera = new Camera(this.player);
         
         bg = new TiledSprite("starbg.png");
         
 //        terminus = new BitmapFont(Gdx.files.internal("terminus.fon"), false);
         
         menuButton = new Sprite("menuButton.png");
         menuButton.x = 0;
         menuButton.y = Gdx.graphics.getHeight() - menuButton.height;
         
         herpMenu = new Menu("menu.png");
         herpMenu.addButton(new Button(200, 100, 300, 100, true){
         	public void react(LibgdxTest model){
         		Log.d("LibgdxTest", "Button pressed!");
         		model.menuMode = false;
         	}
         });
         currentMenu = herpMenu;
         
         levelMenu = new Menu("levelmenu.png");
         currentMenu = levelMenu;
         levelMenu.addButton(new Button(28, 64 - 5 - 10, 10, 10, true){
         	public void react(LibgdxTest model){
         		model.currentMenu = model.mainMenu;
         	}
         });
         levelMenu.addButton(new LevelButton(17, 64 - 18 - 10, 10, 10, true, 1));
         levelMenu.addButton(new LevelButton(28, 64 - 18 - 10, 10, 10, true, 2));
         levelMenu.addButton(new LevelButton(39, 64 - 18 - 10, 10, 10, true, 3));
         levelMenu.addButton(new LevelButton(17, 64 - 29 - 10, 10, 10, true, 4));
         levelMenu.addButton(new LevelButton(28, 64 - 29 - 10, 10, 10, true, 5));
         levelMenu.addButton(new LevelButton(39, 64 - 29 - 10, 10, 10, true, 6));
         levelMenu.addButton(new LevelButton(17, 64 - 40 - 10, 10, 10, true, 7));
         levelMenu.addButton(new LevelButton(28, 64 - 40 - 10, 10, 10, true, 8));
         levelMenu.addButton(new LevelButton(39, 64 - 40 - 10, 10, 10, true, 9));
         
         mainMenu = new Menu("mainmenu.png");
         /*
          * Start Game
          */
         mainMenu.addButton(new Button(35, 256 - 90, 175 - 35, 90 - 75, true){
 			public void react(LibgdxTest model) {
 				Log.d("Button", "Start Game");
 				model.menuMode = false;
 				loadLevel(1);
 			}
         });
         /*
          * Tutorial
          */
         mainMenu.addButton(new Button(60, 256 - 120, 178 - 60, 120 - 103, true){
 			public void react(LibgdxTest model) {
 				Log.d("Button", "Tutorial");
 			}
         });
         /*
          * Levels
          */
         mainMenu.addButton(new Button(60, 256 - 146, 178 - 60, 146 - 129, true){
 			public void react(LibgdxTest model) {
 				Log.d("Button", "Levels");
 				model.currentMenu = model.levelMenu;
 			}
         });
         /*
          * Scores
          */
         mainMenu.addButton(new Button(60, 256 - 171, 178 - 60, 171 - 156, true){
 			public void react(LibgdxTest model) {
 				Log.d("Button", "Scores");
 			}
         });
         /*
          * Credits
          */
         mainMenu.addButton(new Button(60, 256 - 197, 178 - 60, 197 - 183, true){
 			public void react(LibgdxTest model) {
 				Log.d("Button", "Credits");
 				currentMenu = creditsMenu;
 			}
         });
         currentMenu = mainMenu;
         
         creditsMenu = new Menu("credits.png");
         /*
          * Credits
          */
         creditsMenu.addButton(new Button(60, 256 - 197, 178 - 60, 197 - 183, true){
 			public void react(LibgdxTest model) {
 				Log.d("Button", "BACK");
 				currentMenu = mainMenu;
 			}
         });
 	}
 
 	public void render() {
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		
 		if (!menuMode){
 			if (levelEndWait <= 0){
 				if (!player.launchMode){
 					player.move();
 					for (Planet planet : planets){
 						int response = player.influence(planet);
 						if (response == 1){
 							levelEndWait = maxLevelEndWait;
 							particleSources.add(new ParticleSource(200, 100, 2, new Vector3D(0, 0, 0), new Vector3D(player.x + player.width/2, player.y + player.height/2, 0), 40, "confetti_3.png"));
 						} else if (response == 2){
 							loadLevel(levelNum);
 						}
 					}
 				}
 			} else if (levelEndWait == 1){
 				levelEndWait = 0;
 				loadLevel(levelNum + 1);
 			} else {
 				levelEndWait --;
 			}
 		}
 		
 		Rectangle window = new Rectangle(x, y, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
 			
 		batch.begin();
 		
 		bg.draw(batch, x / 2, y / 2);
 
 		this.camera.update(player);
 		x = (int) camera.loc.getX();
 		y = (int) camera.loc.getY();
 		
 		for (Planet planet : planets){
 			planet.draw(batch, x, y);
 			
 		}
 		
 		for (Planet planet : planets){
 			Vector3D toPlanet = planet.p.subtract(player.p);
 			Vector3D wallIntersect = LibgdxTest.vectorIntersectionWithRectangle(player.p, toPlanet, x, y, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
 			Vector3D position = wallIntersect;//.add(toPlanet.scale(20 / toPlanet.lengthSquared()));
 			
 			if (planet.t == Planet.type.WIN){
 				glowG.x = position.getX() - glowG.width / 2;
 				glowG.y = position.getY() - glowG.height / 2;
 				glowG.draw(batch, x, y);
 			} else if (toPlanet.length() < Math.sqrt(Gdx.graphics.getWidth() * Gdx.graphics.getHeight()) && !window.contains((float) planet.x, (float) planet.y)){
 				if (planet.t == Planet.type.HOSTILE){
 					position = position.add(toPlanet.scale(1 / 30));
 					glowR.x = position.getX() - glowR.width / 2;
 					glowR.y = position.getY() - glowR.height / 2;
 					glowR.draw(batch, x, y);
 				} else if (planet.t == Planet.type.FRIENDLY){
 					position = position.add(toPlanet.scale(1 / 30));
 					glowB.x = position.getX() - glowB.width / 2;
 					glowB.y = position.getY() - glowB.height / 2;
 					glowB.draw(batch, x, y);
 				}
 			
 			}
 		}
 		
 		particleSources.add(new ParticleSource(30, 2, 1, new Vector3D(0, 0, 0), new Vector3D(player.x + player.width/2, player.y + player.height/2, 0), 15, "red.png"));
 	
 		menuButton.draw(batch, 0, 0);
 		
 		int i = 0;
 		while (i < particleSources.size()) {
 			if (particleSources.get(i).draw(batch, x, y)) {
 				i++;
 			} else {
 				particleSources.remove(i);
 			}
 		}
 		
 		fuel.width = (int) (Gdx.graphics.getWidth() * player.fuel / player.maxFuel);
 		fuel.x = (int) ((Gdx.graphics.getWidth() - fuel.width) / 2);
 		fuel.draw(batch, 0, 0);
 		
 		player.draw(batch, x, y, (int) win.p.getX(), (int) win.p.getY());
 		
 		batch.end();
 		
 		stage.act(Gdx.graphics.getDeltaTime());
 		stage.draw();
 			
 		/*
 		 * Draw menu if wanted
 		 */
 		if (menuMode){
 			batch.begin();
 			
 			currentMenu.draw(batch, 0, 0);
 			
 			batch.end();
 		}
 	}
 
 	public void resize(int width, int height) {
 		stage.setViewport(width, height, true);
 	}
 
 	public void dispose() {
 		stage.dispose();
 	}
 
 	public void pause() {
 		
 	}
 	
 	public void resume() {
 		
 	}
 	
 	/*
 	 * Load a series of levels
 	 */
 	public boolean loadLevel(int levelNum){
 		FileHandle file = Gdx.files.internal(levels);
 		BufferedReader reader = new BufferedReader(file.reader());
 		
 		try {
 			for (int i = 0; i < levelNum - 1; i++){
 					reader.readLine();
 			}
 			
 			loadLevel(reader.readLine());
 		} catch (IOException e) {
 			Log.e("LibgdxTest", "Not enough levels in level file!");
 			return false;
 		}
 		
 		this.levelNum = levelNum;
 		
 		return true;
 	}
 	
 	/*
 	 * Load the level stored in the specified file.
 	 */
 	public void loadLevel(String levelName){
 		FileHandle file = Gdx.files.internal(levelName);
 		BufferedReader reader = new BufferedReader(file.reader());
 		
 		String line;
 		
 		planets = new ArrayList<Planet>();
 		
 		Random random = new Random();
 		
 		try {
 			while ((line = reader.readLine()) != null){
 				String[] elements = line.split(" ");
 				String filename;
 				Planet.type type;
 				int x, y, r, m;
 				
 				Log.v("LibgdxTest", line);
 				
 				if (elements[0].equals("player")) {
 					player = new Player();
 					player.p = new Vector3D(Integer.parseInt(elements[1]),
 											Integer.parseInt(elements[2]),
 											0);
 					continue;
 				} else if (elements[0].equals("win")){
 					type = Planet.type.WIN;
 					filename = "earth.png";
 				} else if (elements[0].equals("friendly")){
 					type = Planet.type.FRIENDLY;
 					filename = "neptune.png";
 				} else if (elements[0].equals("hostile")){
 					type = Planet.type.HOSTILE;
 					filename = "mars.png";
 				} else {
 					type = Planet.type.NEUTRAL;
 					filename = "sun.png";
 				}
 				
 				x = Integer.parseInt(elements[1]);
 				y = Integer.parseInt(elements[2]);
 				r = Integer.parseInt(elements[3]);
 				m = Integer.parseInt(elements[4]);
 				
 				Planet p = new Planet(filename, type, new Vector3D(x, y, 0), r, m, random.nextDouble() * 360);
 				planets.add(p);
 				if (type == Planet.type.WIN){
 					win = p;
 				}
 			}
 			
 			player.launchMode = true;
 		} catch (IOException e) {
 			Log.e("LibgdxTest", "Failed to load level " + levelName);
 		}
 		
 	}
 	
 	public static Vector3D vectorIntersectionWithRectangle(Vector3D p, Vector3D v, int x, int y, int w, int h){
 		Vector3D toReturn = new Vector3D();
 		int distanceToPoint = 100000000;
 		
 		double a = - v.getY();
 		double b = v.getX();
 		double d = a * p.getX() + b * p.getY();
 		
 		/*
 		 * Check against each side of the rectangle, represented by four point-vector pairs
 		 */
 		for (Vector3D[] side : new Vector3D[][] {{new Vector3D(x, y, 0), new Vector3D(1, 0, 0)}, {new Vector3D(x, y, 0), new Vector3D(0, 1, 0)},
 												 {new Vector3D(x + w, y, 0), new Vector3D(0, 1, 0)}, {new Vector3D(x, y + h, 0), new Vector3D(1, 0, 0)}}){
 			Vector3D p1 = side[0];
 			Vector3D v1 = side[1];
 			
 			double a1 = - v1.getY();
 			double b1 = v1.getX();
 			double d1 = a1 * p1.getX() + b1 * p1.getY();
 			
 			Vector3D intersection = new Vector3D((b1 * d - b * d1) / (a * b1 - a1 * b),
 					  							(a * d1 - a1 * d) / (a * b1 - a1 * b), 0);
 			
 			int dist = (int) intersection.subtract(p).length();
 			
 			if (dist < distanceToPoint && intersection.subtract(p).dotProduct(v) > 0){
 				toReturn = intersection;
 				distanceToPoint = dist;
 			}
 		}
 		
 		return toReturn;
 	}
 
 }
