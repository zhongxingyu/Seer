 package com.stonetolb.game.module;
 
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.tiled.TiledMap;
 
 import com.artemis.Entity;
 import com.artemis.World;
 import com.google.common.base.Preconditions;
 import com.google.common.collect.ImmutableTable;
 import com.google.common.collect.Table;
 import com.stonetolb.asset.graphics.Texture;
 import com.stonetolb.asset.graphics.TextureLoader;
 import com.stonetolb.engine.component.control.PlayerControl;
 import com.stonetolb.engine.component.movement.Rotation;
 import com.stonetolb.engine.component.movement.Velocity;
 import com.stonetolb.engine.component.physics.DynamicBody;
 import com.stonetolb.engine.component.physics.StaticBody;
 import com.stonetolb.engine.component.position.Position;
 import com.stonetolb.engine.component.render.CameraMount;
 import com.stonetolb.engine.component.render.RenderComponent;
 import com.stonetolb.engine.component.render.SpriteControl;
 import com.stonetolb.engine.profiles.WorldProfile;
 import com.stonetolb.engine.system.CameraSystem;
 import com.stonetolb.engine.system.CollisionSystem;
 import com.stonetolb.engine.system.MovementSystem;
 import com.stonetolb.engine.system.PlayerControlSystem;
 import com.stonetolb.engine.system.RenderSystem;
 import com.stonetolb.engine.system.SpriteControlSystem;
 import com.stonetolb.render.Animation;
 import com.stonetolb.render.ImageRenderMode;
 import com.stonetolb.render.NullDrawable;
 import com.stonetolb.render.Sprite;
 import com.stonetolb.render.util.Camera;
 import com.stonetolb.render.util.FluidVantage;
 import com.stonetolb.util.Vector2f;
 
 /**
  * Demo module for Kent State University's ACM speaker series.
  * Giving a talk at Kent's ACM titled "Game Programming : Making the First Jump".
  * This demo module is the final game I'll be showing.
  * 
  * @author james.baiera
  *
  */
 public class DemoModule implements Module {
 	private Texture vaughnTextureSheet;
 	private Texture environmentSheet;
 	private static int VAUGHN_TEXTURE_SHEET_WIDTH = 32;
 	private static int VAUGHN_TEXTURE_SHEET_HEIGHT = 48;
 	private static int TILE = 40;
 	private static Vector2f CAMERA_START = Vector2f.from(46, 554);
 	
 	private World world;
 	
 	private Entity vaughn;
 	private RenderSystem renderSystem;
 	
 	private Sprite worldCeiling;
 	
 	private TiledMap map;
 	
 	/**
 	 * {@inheritDoc Module}
 	 */
 	@Override
 	public void init() {
 		System.out.println("Loading Module Resources");
 		
 		// Camera setup
 		Camera.setVantage(FluidVantage.create(0.04F));
 		Camera.getInstance().setPosition(CAMERA_START);
 		
 		// Load texture resources
 		try {
 			this.vaughnTextureSheet = TextureLoader.getInstance().getTexture("sprites/Vaughn/world/Vaughn.png");
 			this.worldCeiling = new Sprite("maps/TheGrotto.png",ImageRenderMode.STANDING);
 			this.environmentSheet = TextureLoader.getInstance().getTexture("maps/TreesRocksGate.gif");
 		} catch(Exception e) {
 			// TODO : Throw an actual exception
 			System.out.println("BAD THINGS HAPPENED");
 			e.printStackTrace();
 			System.exit(1);
 		}
 	
 		// Gotta make a way to procedurally generate this from a file input...
 		// Create the Sprites and Animations first:
 		Table<Integer, Integer, Sprite> vaughnSpriteAtlas = createCharacterAtlas();
 		
 		Animation.Builder builder = Animation.builder();
 		Animation toward = builder
 				.addFrame(vaughnSpriteAtlas.get(0, 1), 175)
 				.addFrame(vaughnSpriteAtlas.get(0, 2), 175)
 				.addFrame(vaughnSpriteAtlas.get(0, 3), 175)
 				.addFrame(vaughnSpriteAtlas.get(0, 0), 175)
 				.build();
 		Animation left = builder
 				.addFrame(vaughnSpriteAtlas.get(1, 1), 175)
 				.addFrame(vaughnSpriteAtlas.get(1, 2), 175)
 				.addFrame(vaughnSpriteAtlas.get(1, 3), 175)
 				.addFrame(vaughnSpriteAtlas.get(1, 0), 175)
 				.build();
 		Animation right = builder
 				.addFrame(vaughnSpriteAtlas.get(2, 1), 175)
 				.addFrame(vaughnSpriteAtlas.get(2, 2), 175)
 				.addFrame(vaughnSpriteAtlas.get(2, 3), 175)
 				.addFrame(vaughnSpriteAtlas.get(2, 0), 175)
 				.build();
 		Animation away = builder
 				.addFrame(vaughnSpriteAtlas.get(3, 1), 175)
 				.addFrame(vaughnSpriteAtlas.get(3, 2), 175)
 				.addFrame(vaughnSpriteAtlas.get(3, 3), 175)
 				.addFrame(vaughnSpriteAtlas.get(3, 0), 175)
 				.build();
 		
 		Sprite tree = new Sprite(environmentSheet.getSubTexture(2*TILE, 0*TILE, 2*TILE, 3*TILE), ImageRenderMode.STANDING);
 		Sprite bush = new Sprite(environmentSheet.getSubTexture(3*TILE, 3*TILE, 3*TILE, 3*TILE), ImageRenderMode.STANDING);
 		Sprite rock = new Sprite(environmentSheet.getSubTexture(4*TILE, 2*TILE, 1*TILE, 1*TILE), ImageRenderMode.STANDING);
 		Sprite spike= new Sprite(environmentSheet.getSubTexture(6*TILE, 0*TILE, 1*TILE, 2*TILE), ImageRenderMode.STANDING);
 		
 		// Processing World initialization
 		world = new World();
 		renderSystem = new RenderSystem(800,600);
 		world.setSystem(renderSystem, true);
 		world.setSystem(new PlayerControlSystem());
 		world.setSystem(new MovementSystem());
 		world.setSystem(new SpriteControlSystem());
 		world.setSystem(new CameraSystem());
 		world.setSystem(new CollisionSystem());
 		world.initialize();
 		
 		// Component creation 
 		Position positionComponent = new Position(1150, 700);
 		
 		CameraMount cameraMount = new CameraMount(
 				  VAUGHN_TEXTURE_SHEET_WIDTH/2.0F
 				, VAUGHN_TEXTURE_SHEET_HEIGHT/2.0F
 				);
 		
 		RenderComponent renderComponent = new RenderComponent(vaughnSpriteAtlas.get(0, 0));
 		
 		SpriteControl spriteControl = addAnimations(
 				addSprites(
 						  new SpriteControl()
 						, vaughnSpriteAtlas.get(0, 0)
 						, vaughnSpriteAtlas.get(3, 0)
 						, vaughnSpriteAtlas.get(2, 0)
 						, vaughnSpriteAtlas.get(1, 0)
 						)
 					, toward
 					, away
 					, right
 					, left
 					);
 		
 		DynamicBody body = new DynamicBody(
 				  1150
 				, 700
 				, 16
 				, 12
 				, 16
 				, 36
 				);
 		
 		// Entity Creation
 		vaughn = world.createEntity();
 		vaughn.addComponent(positionComponent);
 		vaughn.addComponent(renderComponent);
 		vaughn.addComponent(new Rotation(WorldProfile.WorldDirection.DOWN.getDirection()));
 		vaughn.addComponent(new Velocity(WorldProfile.Speed.STOP.getSpeed()));
 		vaughn.addComponent(new PlayerControl(WorldProfile.Control.ARROWS));
 		vaughn.addComponent(spriteControl);
 		vaughn.addComponent(Camera.attachTo(cameraMount));
 		vaughn.addComponent(body);
 		vaughn.addToWorld();
 		
 		Entity treeEnt = world.createEntity();
 		treeEnt.addComponent(new Position(28*TILE, 14*TILE));
 		treeEnt.addComponent(new RenderComponent(tree));
 		treeEnt.addComponent(new StaticBody(28*TILE, 14*TILE, 60, 50, 40, 95));
 		treeEnt.addToWorld();
 		
 		treeEnt = world.createEntity();
 		treeEnt.addComponent(new Position(15*TILE, 13*TILE));
 		treeEnt.addComponent(new RenderComponent(tree));
 		treeEnt.addComponent(new StaticBody(15*TILE, 13*TILE, 60, 50, 40, 95));
 		treeEnt.addToWorld();
 		
 		treeEnt = world.createEntity();
 		treeEnt.addComponent(new Position(24*TILE, 19*TILE));
 		treeEnt.addComponent(new RenderComponent(tree));
 		treeEnt.addComponent(new StaticBody(24*TILE, 19*TILE, 60, 50, 40, 95));
 		treeEnt.addToWorld();
 		
 		Entity rockEnt = world.createEntity();
 		rockEnt.addComponent(new Position(26*TILE,18*TILE));
 		rockEnt.addComponent(new RenderComponent(rock));
 		rockEnt.addComponent(new StaticBody(26*TILE, 18*TILE,40, 15, 20, 30));
 		rockEnt.addToWorld();
 		
 		rockEnt = world.createEntity();
 		rockEnt.addComponent(new Position(21*TILE,14*TILE));
 		rockEnt.addComponent(new RenderComponent(rock));
 		rockEnt.addComponent(new StaticBody(21*TILE, 14*TILE,40, 15, 20, 30));
 		rockEnt.addToWorld();
 		
 		rockEnt = world.createEntity();
 		rockEnt.addComponent(new Position(20*TILE,19*TILE));
 		rockEnt.addComponent(new RenderComponent(rock));
 		rockEnt.addComponent(new StaticBody(20*TILE, 19*TILE,40, 15, 20, 30));
 		rockEnt.addToWorld();
 		
 		Entity bushEnt = world.createEntity();
 		bushEnt.addComponent(new Position(38*TILE, 22*TILE));
 		bushEnt.addComponent(new RenderComponent(bush));
 		bushEnt.addComponent(new StaticBody(38*TILE, 22*TILE, 75, 50, 65, 90));
 		bushEnt.addToWorld();
 		
 		Entity spireEnt = world.createEntity();
 		spireEnt.addComponent(new Position(34*TILE,20*TILE));
 		spireEnt.addComponent(new RenderComponent(spike));
 		spireEnt.addComponent(new StaticBody(34*TILE, 20*TILE,40, 30, 20, 60));
 		spireEnt.addToWorld();
 		
 		// Create Tiled Map
 		try {
			map = new TiledMap("maps/grotto.tmx");
 		} catch(SlickException se) {
 			// TODO : Throw an actual exception
 			se.printStackTrace();
 			System.exit(1);
 		}
 		System.out.println("Load Complete");
 	}
 
 	/**
 	 * {@inheritDoc Module}
 	 */
 	@Override
 	public void step(long delta) {
 		// Set delta in world object.
 		world.setDelta(delta);
 		
 		// Run system logic over entities
 		world.process();
 	}
 
 	/**
 	 * {@inheritDoc Module}
 	 */
 	@Override
 	public void render(long delta) {
 		// Clear Screen
 		renderSystem.clearScreen();
 		
 		// Render the Ground and backdrop
 		map.render(0, 0, 0);
 		map.render(0, 0, 1);
 		
 		// Render Entites
 		renderSystem.process();
 		
 		// Render the front cliff edge 
 		worldCeiling.draw(9*40, 20*40, 50, delta);
 	}
 	
 	@Override
 	public String toString() {
 		// String Identifier
 		return "DemoModule";
 	}
 	
 	/**
 	 * Takes a SpriteControl and adds the character's animation objects to it.
 	 * This method will take care of Animation Cloning.
 	 * @param sc
 	 * @param first - First Animation
 	 * @param rest - Array of three more Animations
 	 * @return Initialized {@link SpriteControl} Object
 	 */
 	private SpriteControl addAnimations(SpriteControl sc, Animation first, Animation ... rest) {
 		Preconditions.checkArgument(rest.length >= 3, "Incorrect Animation List Size");
 		
 		return sc
 		.setNoOp(NullDrawable.getInstance())
 		.addAction(
 				first.clone(),
 				WorldProfile.Speed.WALK.getSpeed(),
 				WorldProfile.WorldDirection.DOWN.getDirection()
 			)
 		.addAction(
 				rest[0].clone(),
 				WorldProfile.Speed.WALK.getSpeed(),
 				WorldProfile.WorldDirection.UP.getDirection()
 			)
 		.addAction(
 				rest[1].clone(),
 				WorldProfile.Speed.WALK.getSpeed(),
 				WorldProfile.WorldDirection.RIGHT.getDirection()
 			)
 		.addAction(
 				rest[2].clone(),
 				WorldProfile.Speed.WALK.getSpeed(),
 				WorldProfile.WorldDirection.LEFT.getDirection()
 			);
 	}
 	
 	/**
 	 * Takes a SpriteControl and adds the character's sprites to it
 	 * @param sc
 	 * @param first - First sprite
 	 * @param rest - Array of three more sprites
 	 * @return Initialized {@link SpriteControl} Object
 	 */
 	private SpriteControl addSprites(SpriteControl sc, Sprite first, Sprite ... rest) {
 		Preconditions.checkArgument(rest.length >= 3, "Incorrect Sprite List Size");
 		
 		return sc
 		.addAction(
 				first,
 				WorldProfile.Speed.STOP.getSpeed(),
 				WorldProfile.WorldDirection.DOWN.getDirection()
 			)
 		.addAction(
 				rest[0],
 				WorldProfile.Speed.STOP.getSpeed(),
 				WorldProfile.WorldDirection.UP.getDirection()
 			)
 		.addAction(
 				rest[1],
 				WorldProfile.Speed.STOP.getSpeed(),
 				WorldProfile.WorldDirection.RIGHT.getDirection()
 			)
 		.addAction(
 				rest[2],
 				WorldProfile.Speed.STOP.getSpeed(),
 				WorldProfile.WorldDirection.LEFT.getDirection()
 			);
 	}
 	
 	/**
 	 * Spin off method to populate a Guava Table with textures for the character sprite sheet.
 	 * @return Table representing a sprite atlas.
 	 */
 	private Table<Integer, Integer, Sprite> createCharacterAtlas() {
 		ImmutableTable.Builder<Integer, Integer, Sprite> builder = ImmutableTable.builder();
 		
 		Sprite sprite = null;
 		for(int row = 0; row < 4; row++) {
 			for(int col = 0; col < 4; col++) {
 				sprite = new Sprite(
 						  vaughnTextureSheet.getSubTexture(
 								    col*VAUGHN_TEXTURE_SHEET_WIDTH
 								  , row*VAUGHN_TEXTURE_SHEET_HEIGHT
 								  , VAUGHN_TEXTURE_SHEET_WIDTH
 								  , VAUGHN_TEXTURE_SHEET_HEIGHT
 								  )
 						, ImageRenderMode.STANDING
 						);
 				builder.put(row, col,sprite);
 			}
 		}
 		
 		return builder.build();
 	}
 }
