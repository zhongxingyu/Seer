 package logic.level;
 
 import logic.Engine;
 import logic.Player;
 import logic.Consts.DataDir;
 import logic.level.LevelData.Background.FieldData;
 import logic.level.LevelData.Background.LayerData;
 import logic.level.LevelData.Background.StaticData;
 import logic.level.LevelData.Spawn;
 import logic.level.LevelData.SpawnSet;
 
 import EntitySystems.*;
 import EntitySystems.Components.Angle;
 import EntitySystems.Components.Position;
 import EntitySystems.Components.Renderable;
 import EntitySystems.Components.Rotation;
 import EntitySystems.Components.Scrollable;
 import EntitySystems.Components.Velocity;
 
 import aurelienribon.tweenengine.TweenManager;
 
 import com.artemis.Entity;
 import com.artemis.World;
 import com.artemis.managers.GroupManager;
 import com.artemis.managers.TagManager;
 import com.artemis.utils.ImmutableBag;
 import com.badlogic.gdx.audio.Music;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.Texture.TextureWrap;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.utils.Array;
 import com.shipvgdc.sugdk.graphics.SpriteSheet;
 
 public class Level{
 	
 	//we set a constant maximum amount of sprites we draw to keep performance at its peak
 	//sprites includes both bullets and creatures, pretty much anything in the world
 	private static final int MAX_SPRITES = 128;
 	private int currentEnemyGroup;
 	
 	//game load data files
 	public LevelData data;
 	ImmutableBag<Entity> activeEnemies;
 		
 	public static final float[] FOV = {0, 0, 190, 220};
 
 	/**
 	 * Container of all the entities for this level
 	 */
 	public World world;
 	
 	/**
 	 * Loads and constructs a level
 	 * @param id - name of the level
 	 */
 	public Level(String id)
 	{
 		data = new LevelData(id);
 		this.world = Engine.world;
 	}
 	
 	public void advance(float delta)
 	{
 		world.setDelta(delta);
 		world.process();
 		Engine.score += 10f*delta;
 	}
 	
 	/**
 	 * Draws the render system
 	 */
 	public void draw(SpriteBatch batch)
 	{
 		world.getSystem(RenderSystem.class).draw(batch);
 		
 	}
 
 	/**
 	 * sets all the level's data into its starting positions
 	 */
 	public void start()
 	{
 		GroupManager gm = this.world.getManager(GroupManager.class);
 		TagManager tm = this.world.getManager(TagManager.class);
 		
 		// place the enemies in the world
 		for (int i = 0; i < data.enemyData.size; i++)
 		{
 			SpawnSet s = data.enemyData.get(i);
 			for (int j = 0; j < s.spawns.size; j++)
 			{
 				Spawn enemy = s.spawns.get(j);
 				
 				Entity e = this.world.createEntity();
 				e = s.atlas.createEnemy(enemy.name, e);
 				Position p = (Position)e.getComponent(Position.CType);
 				p.location.x = enemy.pos.x;
 				p.location.y = enemy.pos.y;
 				this.world.addEntity(e);
 			}
 		}
 		
 		//create layered background
 		for (int i = 0; i < data.background.stack.size; i++)
 		{
 			
 			Entity layer = this.world.createEntity();
 			
 			FieldData f = data.background.stack.get(i);
 			Texture t = Engine.assets.get(f.image, Texture.class);
 			Sprite s = new Sprite(t);
 			
 			if (f instanceof LayerData)
 			{
 				LayerData d = (LayerData)data.background.stack.get(i);
 				t.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
				s.setSize(FOV[2], FOV[3]);
 				layer.addComponent(new Position(), Position.CType);
 					
 				layer.addComponent(new Scrollable(0, d.rate, Math.max(FOV[2]/(float)t.getWidth(), 1.0f), Math.max(FOV[3]/(float)t.getHeight(), 1.0f)), Scrollable.CType);
 			}
 			else if (f instanceof StaticData)
 			{
 				StaticData d = (StaticData)data.background.stack.get(i);
 				layer.addComponent(new Position(d.x, d.y), Position.CType);
 				layer.addComponent(new Angle(0), Angle.CType);
 				layer.addComponent(new Rotation(d.dps), Rotation.CType);
 			}
 			
 			layer.addComponent(new Renderable(s), Renderable.CType);
 			
 			gm.add(layer, "Field");
 			this.world.addEntity(layer);
 		}
 		
 		//load banners
 		Texture t = Engine.assets.get(DataDir.Ui+"banners.png", Texture.class);
 		t.setWrap(TextureWrap.Repeat, TextureWrap.ClampToEdge);
 		SpriteSheet bannerTex = new SpriteSheet(t, 1, 3);
 		for (int i = 0; i < 3; i++)
 		{
 			Entity e = this.world.createEntity();
 			TextureRegion r = bannerTex.getFrame(i);
 			Sprite s = new Sprite(r);
 			s.setSize(FOV[3], 12);
 			s.setRotation(90);
 			s.setOrigin(0, 0);
 			
 			e.addComponent(new Position(0, 0, 12, 0));
 			e.addComponent(new Scrollable(.35f, 0f, FOV[3]/bannerTex.getFrameWidth(), 1f, r));
 			e.addComponent(new Renderable(s));
 			
 			gm.add(e, "Banner");
 			gm.add(e, "Banner"+(char)(i+65));
 			
 			e.addToWorld();
 			
 			e = this.world.createEntity();
 			s = new Sprite(r);
 			
 			s.setSize(FOV[3], 12);
 			s.setRotation(-90);
 			s.setOrigin(0, 0);
 			s.flip(false, true);
 			
 			e.addComponent(new Position(FOV[2], 0, -12, FOV[3]));
 			e.addComponent(new Scrollable(-.35f, 0f, 220f/bannerTex.getFrameWidth(), 1f, r));
 			e.addComponent(new Renderable(s));
 			
 			gm.add(e, "Banner");
 			gm.add(e, "Banner"+(char)(i+65));
 			e.addToWorld();
 		}
 		
 		this.world.initialize();
 		
 		Engine.score = 0f;
 		
 		Position p = (Position)Engine.player.getComponent(Position.CType);
 		p.location.x = FOV[2]/2.0f;
 	}
 
 	/**
 	 * Loads the assets necessary for the level to be displayed
 	 */
 	public void loadAssets() {
 		for (int i = 0; i < data.background.stack.size; i++)
 		{
 			FieldData f = data.background.stack.get(i);
 			Engine.assets.load(f.image, Texture.class);
 		}
 		Engine.assets.load(data.bgm, Music.class);
 		Engine.assets.load(DataDir.Ui + "banners.png", Texture.class);
 	}
 }
