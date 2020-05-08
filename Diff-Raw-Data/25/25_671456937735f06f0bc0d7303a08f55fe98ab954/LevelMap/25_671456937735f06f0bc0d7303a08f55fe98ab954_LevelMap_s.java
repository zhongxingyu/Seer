 package com.me.ampdom;
 
 import java.util.ArrayList;
 
 import com.badlogic.gdx.Files.FileType;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.audio.Music;
 import com.badlogic.gdx.graphics.g2d.tiled.TiledObject;
 import com.badlogic.gdx.graphics.g2d.tiled.TiledObjectGroup;
 import com.badlogic.gdx.physics.box2d.World;
 
 public class LevelMap {
 	
 	protected TiledMapHelper tiledMapHelper;
 	int screenWidth;
 	int screenHeight;
 	//CurrentLevel currentLevel;//RC added
 	int currentLevel; //RC removed. using enums easier
 	EnemyContact detect;
 	Music test;
 	World world;
     public static final float PIXELS_PER_METER = 60.0f;	
     static EndLevelTrigger endlevelpt1;
 	static ArrayList<FlyingEnemy> flyers;
 	static ArrayList<Enemy> enemies;
 	static ArrayList<Obstacle> spikes;
 	static ArrayList<MovingPlat> plats;
     static FlyJar jar;
      
 	
 	public LevelMap(){
 		this.currentLevel=0;//CurrentLevel.ESCAPE_FROM_THE_CASTLE;
 		this.screenHeight = -1;
 		this.screenWidth = -1;
 		this.tiledMapHelper = new TiledMapHelper();
 	}
 	
 	public void create(World world, /*CurrentLevel*/int currentLevel, int screenWidth, int screenHeight,EnemyContact detect) {
 		this.world = world;
 		this.currentLevel = currentLevel;
 		this.screenHeight = screenHeight;
 		this.screenWidth = screenWidth;
 		this.detect = detect;
 		this.tiledMapHelper = new TiledMapHelper();		
 	
 		
 		
 		
 
 	
 		
 		switch(currentLevel)
 		{
 		case 0:
 			test = Gdx.audio.newMusic(Gdx.files.getFileHandle("data/sounds/beat.mp3", FileType.Internal));
 			flyers = new ArrayList<FlyingEnemy>();
 			enemies = new ArrayList<Enemy>();
 			spikes = new ArrayList<Obstacle>();
 			plats = new ArrayList<MovingPlat>();
 			
 			tiledMapHelper.prepareCamera(screenWidth, screenHeight);
 			tiledMapHelper.setPackerDirectory("data/packer/");
			//tiledMapHelper.loadMap("data/world/level1/level.tmx");
 			tiledMapHelper.loadMap("data/world/level1/level.tmx");
 			
 			tiledMapHelper.loadCollisions("data/collisions.txt", world,
 					PIXELS_PER_METER);
 	
 			
 			plats.add(new MovingPlat(world,"data/Objects/dungeonPlatform.png", 71.29f, 5.2f,0,1));
 			plats.add(new MovingPlat(world,"data/Objects/dungeonPlatform.png", 73.5f, 5.2f,0,-1));
 			plats.add(new MovingPlat(world,"data/Objects/dungeonPlatform.png", 82.64f, 5.2f,0,-1));
 			plats.add(new MovingPlat(world,"data/Objects/dungeonPlatform.png", 85.07f, 5.2f,0,1));
 			plats.add(new MovingPlat(world,"data/Objects/dungeonPlatform.png", 93.95f, 5.2f,0,1));
 			plats.add(new MovingPlat(world,"data/Objects/dungeonPlatform.png", 102.53f, 5.2f,0,1));
 			plats.add(new MovingPlat(world,"data/Objects/dungeonPlatform.png", 111.33f, 5.2f,0,1));
 			
 			
 			
 			
 			jar = new FlyJar(world, "data/Objects/flyjar.png", 78.96f, 7.65f);
 			
 	     	/*flyers.add(new FlyingEnemy(world, "data/Enemies/bat.png", 36.0f, 6.5f,64,64));
 	    	flyers.add(new FlyingEnemy(world, "data/Enemies/bat.png", 51.3f, 6.5f,64,64));
 	    	flyers.add(new FlyingEnemy(world, "data/Enemies/bat.png", 67.17f, 9.0f,64,64));
 	    	flyers.add(new FlyingEnemy(world, "data/Enemies/bat.png", 98.25f, 7.0f,64,64));
 	    	flyers.add(new FlyingEnemy(world, "data/Enemies/bat.png", 111.0f, 8.5f,64,64));
 	    	flyers.add(new FlyingEnemy(world, "data/Enemies/bat.png", 125.3f, 6.0f,64,64));*/
 			
 			/*****************************************************************************************/
 			//--!!!add flyers from object layer!!!
 			ArrayList<TiledObjectGroup> objectGroups = tiledMapHelper.getMap().objectGroups;
 			for(TiledObjectGroup tG: objectGroups)
 			{
 				for(TiledObject tO: tG.objects)
 				{
 					if(tO.name.equals("flyer"))
 					{
 						flyers.add(new FlyingEnemy(world, "data/Enemies/bat.png", tO.x/AmpDom.PIXELS_PER_METER, tO.y/AmpDom.PIXELS_PER_METER,64,64));
 					}
 				}
 			}
 			/*****************************************************************************************/
 	    	
 		
 			enemies.add(new Enemy(world, "data/Enemies/rat2.png", 11.0f, 3.43f, .7f, .7f,128,64));
 			enemies.add(new Enemy(world, "data/Enemies/rat2.png", 16.59f, 4.3f, .7f, .7f,128,64));
 			enemies.add(new Enemy(world, "data/Enemies/rat2.png", 21.82f, 3.43f,1.42f,1.42f,128,64));
 			enemies.add(new Enemy(world, "data/Enemies/rat2.png", 28.69f, 1.29f,1.62f,1.62f,128,64));
 			enemies.add(new Enemy(world, "data/Enemies/rat2.png", 36.2f, 3.44f,1.42f,1.42f,128,64));
 			enemies.add(new Enemy(world, "data/Enemies/rat2.png", 44.62f, 1.29f,1.6f,5.1f,128,64));
 			enemies.add(new Enemy(world, "data/Enemies/rat2.png", 56.2f, 4.49f,1.30f,1.30f,128,64));
 			enemies.add(new Enemy(world, "data/Enemies/rat2.png", 61.59f, 5.56f,1.30f,1.30f,128,64));
 			enemies.add(new Enemy(world, "data/Enemies/rat2.png", 67.21f, 6.63f,1.42f,1.42f,128,64));
 			enemies.add(new Enemy(world, "data/Enemies/rat2.png", 89.57f, 6.63f,1.42f,1.42f,128,64));
 			enemies.add(new Enemy(world, "data/Enemies/rat2.png", 98.08f, 4.49f,1.42f,1.42f,128,64));
 			enemies.add(new Enemy(world, "data/Enemies/rat2.png", 106.74f, 6.63f,1.42f,1.42f,128,64));
 			enemies.add(new Enemy(world, "data/Enemies/rat2.png", 124.11f, 1.5f,1.42f,1.42f,128,64));
 			
 			
 			
 			for(float i=0;i<6;i++)
 		    spikes.add(new Obstacle(world, "data/Objects/spikes.png", 14f+i, 2.0f));
 			
 			for(float i=0;i<22;i++)
 			spikes.add(new Obstacle(world, "data/Objects/spikes.png", 54f+i, 2.0f));
 			
 			for(float i=0;i<30;i++)
 		    spikes.add(new Obstacle(world, "data/Objects/spikes.png", 83f+i, 2.0f));
 			
 			endlevelpt1 = new EndLevelTrigger(world,"data/Objects/endGoalPost.png", 134.9f,6.62f);
 		    test.play();
 		
 			break;
		case 1:
 	
 			flyers = new ArrayList<FlyingEnemy>();
 			enemies = new ArrayList<Enemy>();
 			spikes = new ArrayList<Obstacle>();
 			tiledMapHelper.prepareCamera(screenWidth, screenHeight);
 		    tiledMapHelper.setPackerDirectory("data/packer");
 			tiledMapHelper.loadMap("data/world/level2/level2.tmx");
 	        tiledMapHelper.loadCollisions("data/collisions.txt", world,
 					PIXELS_PER_METER);
 			break;
 //		case LUCID_DESERT:
 //			break;
 //		case JUNGLE_OF_MISCHIEF:
 //			break;
 //		case RETURN_TO_THE_KINGDOM:
 //			break;
 //		case ZEUS_BATTLE:
 //			break;
 			
 		} 
 	}
 	
 	
 	public TiledMapHelper getMap(){
 		return tiledMapHelper;
 	}
 	public void setLevel(/*CurrentLevel*/int currentLevel){
 		this.currentLevel = currentLevel;
 	}
 	
 	public void deleteLevel(){
 		
 		for(FlyingEnemy f : flyers){
 			if(f != null)
 	       world.destroyBody(f.entity);
 			}
 			
 			for(Enemy e : LevelMap.enemies){
 				if(e != null)
 				  world.destroyBody(e.entity);
 			}
 			
 			for(Obstacle s : LevelMap.spikes){
 				if(s != null)
 				  world.destroyBody(s.entity);
 			}
 		
 		endlevelpt1.batch.dispose();
         jar.batch.dispose();
 	    flyers.clear();
 		spikes.clear();
 		enemies.clear();
 		test.dispose();
 		System.out.println("before dispose  "+tiledMapHelper);
         tiledMapHelper.dispose();
         tiledMapHelper=null;	
 	}
 }
 	
 
