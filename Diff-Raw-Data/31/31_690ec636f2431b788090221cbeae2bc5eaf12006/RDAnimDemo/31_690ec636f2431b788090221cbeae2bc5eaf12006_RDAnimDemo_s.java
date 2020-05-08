 package demos;
 
 import math.Vector2;
 import misc.SoundManager;
 import animation.Animation;
 import animation.AnimationPlayer;
 import animation.Bones;
 import animation.PonyColorChangeHelper;
 import components.*;
 import entityFramework.*;
 import entitySystems.CameraControlSystem;
 import gameMap.Scene;
 import graphics.Color;
 import graphics.SpriteBatch;
 import graphics.Texture2D;
 import scripting.PlayerScript;
 import utils.Camera2D;
 import utils.GameTime;
 import utils.Keyboard;
 import utils.Mouse;
 import utils.Rectangle;
 
 public class RDAnimDemo extends Demo {
 	private static final String playerAsset = "Player.archetype";
 	private static Rectangle screenDim 		= new Rectangle(0,0,1920,1020);
 
 	private IEntityWorld gameWorld;
 	private Camera2D camera;
 	private SpriteBatch spriteBatch;
 	private Scene scene;
 
 	public static void main(String[] str) {
 		RDAnimDemo demo = new RDAnimDemo();
 		demo.start();
 	}
 
 	public RDAnimDemo() {
 		super(screenDim, 60);
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	public void update(GameTime time) {
 		this.gameWorld.update(time);
 		this.gameWorld.getEntityManager().destoryKilledEntities();

		if(Math.random() < 0.1f) {
			IEntityDatabase database  = this.gameWorld.getDatabase();
			if(database.getEntityCount() < 150) {
				spawnManlyMan();
			}
		}

 	}
 
 	private void spawnManlyMan() {
 
 		IEntityManager manager = this.gameWorld.getEntityManager();
 
 		//MANLYMAN
 		AnimationPlayer manPlayer = new AnimationPlayer();
 		Animation walkAnimation = ContentManager.load("manlymanwalk.anim", Animation.class);
 		manPlayer.addAnimation("walk", walkAnimation);
 		Animation explodeAnimation = ContentManager.load("manlymanexplosion.anim", Animation.class);
 		manPlayer.addAnimation("explode", explodeAnimation);
 
 
 		manPlayer.startAnimation("walk");
 		AnimationComp manAniCom = new AnimationComp(manPlayer);
 
 
 		IEntity manEntity = manager.createEntity(ContentManager.loadArchetype("ManlyMan.archetype"));
 		manEntity.addComponent(manAniCom);
 		manEntity.refresh();
 		//ENDMAN
 
 		placeAtRandomPosition(manEntity);
 
 
 	}
 
 	@Override
 	public void render(GameTime time) {
 		this.spriteBatch.clearScreen(Color.Black);
 		this.spriteBatch.begin(null, this.camera.getTransformation());
 		this.gameWorld.render();
 		this.spriteBatch.end();	
 	}
 
 	@Override
 	protected void initialize() {
		scene = ContentManager.load("MaseScenev1.xml", Scene.class);
 		camera = new Camera2D(scene.getWorldBounds(), screenDim);
 		spriteBatch = new SpriteBatch(screenDim);
 
 		SoundManager soundManager = new SoundManager(this.ContentManager,1.0f,1.0f,1.0f);
 
		this.gameWorld = WorldBuilder.buildGameWorld(camera,  scene, new Mouse(), new Keyboard(), this.ContentManager,soundManager, spriteBatch, false);
 		gameWorld.initialize();
 
 		//ANIMATION UGLY SHIT
 		IEntityArchetype archetype = ContentManager.loadArchetype(playerAsset);
 		IEntity entity = gameWorld.getEntityManager().createEntity(archetype);
 		entity.addComponent(new BehaviourComp(new PlayerScript()));
		entity.getComponent(RenderingComp.class).setTexture(Texture2D.getPixel());

 		AnimationPlayer player = ContentManager.load("rdset.animset", AnimationPlayer.class);
 		player.attachAnimationToBone(Bones.EYE.getValue(), ContentManager.load("monocle.anim", Animation.class));
 		player.startAnimation("idle");
 		AnimationComp animComp = new AnimationComp(player);
 		PonyColorChangeHelper.setBodyColor(Color.Violet, animComp);
 		PonyColorChangeHelper.setEyeColor(Color.Gold, animComp);
 		PonyColorChangeHelper.setManeColor(Color.Purple, animComp);
 		entity.addComponent(animComp);
 		//END OF ANIMATION UGLY SHIT
		entity.getComponent(TransformationComp.class).setPosition(600, 600);
 
 		
 
 		entity.addToGroup(CameraControlSystem.GROUP_NAME);
 		entity.refresh();
 
 	}
 
 	private void placeAtRandomPosition(IEntity entity) {
 		Vector2 pos = new Vector2((float)Math.random() * (this.scene.getWorldBounds().Width - 100),
 				(float)Math.random() * (this.scene.getWorldBounds().Height - 100));
 		entity.getComponent(TransformationComp.class).setPosition(pos);
 	}
 
 }
