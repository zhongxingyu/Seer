 package com.subfty.krkjam2013.game;
 
 import java.util.Comparator;
 import java.util.LinkedList;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.g2d.ParticleEffectPool.PooledEffect;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
 import com.badlogic.gdx.math.Matrix4;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.scenes.scene2d.Group;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.utils.Array;
 import com.subfty.krkjam2013.Krakjam;
 import com.subfty.krkjam2013.game.actor.Collider;
 import com.subfty.krkjam2013.game.actor.Player;
 import com.subfty.krkjam2013.game.actor.aliens.Alien;
 import com.subfty.krkjam2013.game.actor.aliens.Alien.ALIEN_TYPE;
 import com.subfty.krkjam2013.game.actor.aliens.AlienOverlord;
 import com.subfty.krkjam2013.game.actor.buildings.Building;
 import com.subfty.krkjam2013.game.actor.buildings.BuildingsOverlord;
 import com.subfty.krkjam2013.game.actor.buildings.BuildingsOverlord.B_TYPE;
 import com.subfty.krkjam2013.game.actor.crystals.Crystal;
 import com.subfty.krkjam2013.game.actor.crystals.CrystalOverlord;
 import com.subfty.krkjam2013.game.actor.ui.BloodyScreen;
 import com.subfty.krkjam2013.game.statsscreen.StatsScreen;
 import com.subfty.krkjam2013.util.Art;
 import com.subfty.krkjam2013.util.Screen;
 
 public class GameScreen extends Screen{
 
 	public boolean pause;
 	
     //GROUPS
 	public Background background;
 	public Group agents;
 	public Group ui;
 	public StatsScreen stats;
 	
 	public Player player;
 	public BuildingsOverlord bOverlord;
 	public AlienOverlord aOverlord;
 	public CrystalOverlord cOverlord;
 	
 	public static float timer = 0;
 	
 	
 	//UI
 	public BloodyScreen bScreen;
 	Sprite expBackSprite;
 	Sprite expFrontSprite;
 	Sprite counterSprite;
 	
 	public Building base;
 	
 	public LinkedList<Collider> colliders = new LinkedList<Collider>();
 	
 	public Building generators[];
 	
 	public GameScreen(Stage stage){
 		super(stage);
 		
 		Krakjam.gameScreen = this;
 		
 		agents= new Group();
 		background = new Background(this);
 		
 		ui = new Group();
 		
 		bOverlord = new BuildingsOverlord(background, agents);
 		
 		player = new Player(background, 0,0);
 		stats = new StatsScreen(player);
 		aOverlord = new AlienOverlord(background, this);
 		cOverlord = new CrystalOverlord(player, agents);
 		
 		this.addActor(cOverlord);
 		this.addActor(bOverlord);
 		this.addActor(aOverlord);
 		
 		bScreen = new BloodyScreen(player);
 		ui.addActor(bScreen);
 		
 		agents.addActor(player);
 		colliders.add(player);
 		
 		player.init();
 		
 		this.addActor(background);
 		this.addActor(agents);
 		this.addActor(ui);
 		
 		bOverlord.createNewBuilding(-2, 3, B_TYPE.BASE);
 		
 		bOverlord.createNewBuilding(-6, 2, B_TYPE.TURRET);
 		bOverlord.createNewBuilding(5, 0, B_TYPE.TURRET);
 		
 		generators = new Building[]{bOverlord.createNewBuilding(2, -5, B_TYPE.GENERATOR),
 									bOverlord.createNewBuilding(2, -9, B_TYPE.GENERATOR)};
 		
		bOverlord.createNewBuilding(-6, -8, B_TYPE.MEDCENTRE);
 		
 		
 		// LOADING HUD
 		expBackSprite = Krakjam.art.atlases[Art.A_BSCREEN].createSprite("exp-front");
 		expFrontSprite = Krakjam.art.atlases[Art.A_BSCREEN].createSprite("exp-back");
 		counterSprite = Krakjam.art.atlases[Art.A_BSCREEN].createSprite("counter");
 	}
 	
 //	public void start(){
 //		Krakjam.art.bgM.setLooping(true);
 //		Krakjam.art.bgM.play();
 //	}
 	
 	public boolean generatorsRunning(){
 		for(int i=0; i<generators.length; i++)
 			if(!generators[i].isHealth() || generators[i].possesedCristals < 60)
 				return false;
 		return true;
 	}
 	
 	@Override
 	public void act(float delta){
 		if(this.visible == false)
 			return;
 		
 		timer += delta;
 		
 		if(stats.visible)
 			stats.act(delta);
 		else {
 			super.act(delta);
 		
 	    	//SORTING ACTORS
 			agents.sortChildren(new Comparator<Actor>() {
 				@Override
 				public int compare(Actor arg0, Actor arg1) {
 					return (int)(arg1.y - arg0.y);
 				}
 			});
 
 			agents.removeActor(player.cursor);
 			agents.addActor(player.cursor);
 		}
 		
 		Array<Crystal> crystals = cOverlord.crystals;
 		for(int i=0; i<crystals.size; i++) {
 			Crystal c = crystals.get(i);
 			if(c.visible == false)
 				continue;
 			
 			if(c.aliensSpawned)
 				continue;
 			
 			if(Vector2.tmp.set(c.x, c.y).sub(player.x, player.y).len() < 600) {
 				c.aliensSpawned = true;
 				
 				final int MAX_ALIENS = 6;
 				final int MIN_ALIENS = 4;
 				
 				int aliensNum = MIN_ALIENS + Krakjam.rand.nextInt(MAX_ALIENS-MIN_ALIENS+1);
 				
 				for(int j=0; j<aliensNum; j++) {
 					int alienType = Krakjam.rand.nextInt(3);
 					Alien.ALIEN_TYPE alien_types[] = {ALIEN_TYPE.REGULAR, ALIEN_TYPE.EXPLODING, ALIEN_TYPE.SHOOTER};
 					aOverlord.spawn(c.x + Krakjam.rand.nextFloat()*100, 
 						    c.y + Krakjam.rand.nextFloat()*100 , alien_types[alienType]);
 				}
 			}
 		}
 	}
 	
 	Matrix4 translation = new Matrix4();
 	Matrix4 identity = new Matrix4();
 	
 	@Override
 	public void draw (SpriteBatch batch, float parentAlpha) {
 		super.draw(batch, parentAlpha);
 		
 		translation.idt();
 		float x = player.parent.x, y=player.parent.y;
 		translation.translate(x, y, 0);
 		
 		batch.setTransformMatrix(translation);
 		
 		for (int i = Krakjam.effects.size - 1; i >= 0; i--) {
 	        PooledEffect effect = Krakjam.effects.get(i);
 	        effect.draw(batch, Gdx.graphics.getDeltaTime());
 	        if (effect.isComplete()) {
 	                effect.free();
 	                Krakjam.effects.removeIndex(i);
 	        }
 		}
 		
 		translation.idt();
 		x = player.parent.x; y=player.parent.y;
 		translation.translate(x+player.x, y+player.y+40.0f, 0);
 		batch.setTransformMatrix(translation);
 		
 		for (int i = Krakjam.shotEffects.size - 1; i >= 0; i--) {
 	        PooledEffect effect = Krakjam.shotEffects.get(i);
 	        effect.draw(batch, Gdx.graphics.getDeltaTime());
 	        if (effect.isComplete()) {
 	                effect.free();
 	                Krakjam.shotEffects.removeIndex(i);
 	        }
 		}
 		
 		batch.setTransformMatrix(identity);
 		
 		//agents.x += 1;
 		Krakjam.shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
 		
 		counterSprite.setPosition(5, 600-counterSprite.getHeight()-5);
 		counterSprite.draw(batch);
 		
 		Krakjam.art.fonts[Art.F_DIGITAL].setColor(0.5f, 0, 0, 1);
 		Krakjam.art.fonts[Art.F_DIGITAL].setScale(0.4f);
 		Krakjam.art.fonts[Art.F_DIGITAL].drawWrapped(batch, ""+player.numberOfCrystals, 
 				0, 600 - 18, 138, HAlignment.RIGHT);
 		
 		int prevExp = player.getLevelExp(player.getLevel()-1);
 		int nextExp = player.getLevelExp(player.getLevel());
 		
 		float expState = (player.exp - prevExp)/(float)(nextExp-prevExp);
 		
 		expBackSprite.setPosition(800 - 5 - expBackSprite.getWidth(), 600 - expBackSprite.getHeight() - 5);
 		expBackSprite.draw(batch);
 		
 		expFrontSprite.setPosition(800 - 5 - expBackSprite.getWidth(), 600 - expBackSprite.getHeight() - 5);
 		expFrontSprite.setRegion(expFrontSprite.getRegionX(), expFrontSprite.getRegionY(),
 				(int)(expBackSprite.getRegionWidth()*expState), expBackSprite.getRegionHeight());
 		expFrontSprite.setSize((int)(expBackSprite.getRegionWidth()*expState), expBackSprite.getRegionHeight());
 		expFrontSprite.draw(batch);
 		
 		Krakjam.art.fonts[Art.F_DIGITAL].setColor(0.5f, 0, 0, 1);
 		Krakjam.art.fonts[Art.F_DIGITAL].setScale(0.4f);
 		Krakjam.art.fonts[Art.F_DIGITAL].drawWrapped(batch, "LEVEL "+player.getLevel(),
 				800-300-5, 600 - 18, 300, HAlignment.CENTER);
 		
 		stats.draw(batch, parentAlpha);
 	}
 }
