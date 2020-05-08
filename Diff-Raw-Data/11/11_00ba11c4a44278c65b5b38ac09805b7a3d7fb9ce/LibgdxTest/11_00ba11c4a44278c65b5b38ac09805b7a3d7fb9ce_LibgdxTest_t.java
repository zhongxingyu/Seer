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
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.input.GestureDetector;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 
 public class LibgdxTest implements ApplicationListener{
 	
 	private Stage stage;
 	private SpriteBatch batch;
 	Controller controller;
 	GestureDetector gestureDetector;
 	
 	Player player;
 	ArrayList<Planet> planets;
 	
 	Audio audio;
 	static Music bambi;
 	
 	Sprite fuel;
 	
 	Camera camera;
 	
 	int x, y;
 	
 	int levelNum;
 	String levels = "levels";
 
 	public void create() {		
 		stage = new Stage(0, 0, true);
 		batch = new SpriteBatch();
 		
 		controller = new Controller();
         controller.model = this;
         gestureDetector = new GestureDetector(controller);
         Gdx.input.setInputProcessor(gestureDetector);
         planets = new ArrayList<Planet>();
         
         player = new Player();
         
         /*
          * Creates planets
          */
 //        int scale = 750;
 //        Random random = new Random();
 //    	planets.add(new Planet("earth.png", new Vector3D(random.nextInt(1000), random.nextInt(1000), 0), random.nextInt(scale) * .1 + 20, 0, random.nextDouble() * 360));
 //        for (int i = 0; i < 5; i++){
 //        	int planetDecide = random.nextInt(100);
 //        	String planet;
 //        	if (planetDecide > 50){
 //        		planet = "sun.png";
 //        	} else if (planetDecide > 25){
 //        		planet = "neptune.png";
 //        	} else {
 //        		planet = "mars.png";
 //        	}
 //        	planets.add(new Planet(planet, new Vector3D(random.nextInt(1000), random.nextInt(1000), 0), random.nextInt(scale) * .05 + 20, 0, random.nextDouble() * 360));
 //        }
 
         loadLevels("levels", 1);
         
         audio = Gdx.audio;
         bambi = audio.newMusic(Gdx.files.internal("bambi.ogg"));
         //bambi.setLooping(true);
       
         fuel = new Sprite("fuelbar.png");
         fuel.height = 15;
         fuel.width = Gdx.graphics.getWidth();
 
         this.camera = new Camera(this.player);
 	}
 
 	public void render() {
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		
 		if (!player.launchMode){
 			player.move();
 			for (Planet planet : planets){
 				int response = player.influence(planet);
 				if (response == 1){
 					loadLevels(levels, levelNum + 1);
 				} else if (response == 2){
 					loadLevels(levels, levelNum);
 				}
 			}
 		}
 		
 		batch.begin();
 
		this.camera.update(player);
 		x = (int) camera.loc.getX();
 		y = (int) camera.loc.getY();
 		
 		for (Planet planet : planets){
 			planet.draw(batch, x, y);
 		}
 		
 		player.draw(batch, x, y);
 		
 		fuel.width = (int) (Gdx.graphics.getWidth() * player.fuel / player.maxFuel);
 		fuel.x = (int) ((Gdx.graphics.getWidth() - fuel.width) / 2);
 		fuel.draw(batch, 0, 0);
 		
 		batch.end();
 		
 		stage.act(Gdx.graphics.getDeltaTime());
 		stage.draw();
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
 	public boolean loadLevels(String levelsList, int levelNum){
 		FileHandle file = Gdx.files.internal(levelsList);
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
 		
 		player = new Player();
 		
 		try {
 			while ((line = reader.readLine()) != null){
 				String[] elements = line.split(" ");
 				String filename;
 				Planet.type type;
 				int x, y, r, m;
 				
 				Log.v("LibgdxTest", line);
 				
 				if (elements[0].equals("win")){
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
 				
 				planets.add(new Planet(filename, type, new Vector3D(x, y, 0), r, m));
 			}
 		} catch (IOException e) {
 			Log.e("LibgdxTest", "Failed to load level " + levelName);
 		}
 		
 	}
 
 }
