 package com.soc.core;
 
 import java.util.Stack;
 
 import com.artemis.ComponentMapper;
 import com.artemis.Entity;
 import com.artemis.World;
 import com.artemis.managers.GroupManager;
 import com.artemis.managers.TagManager;
 import com.artemis.utils.ImmutableBag;
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.InputMultiplexer;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
 import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 import com.badlogic.gdx.utils.Array;
 import com.soc.game.attacks.spells.ArrowSpell;
 import com.soc.game.attacks.spells.BiteSpell;
 import com.soc.game.attacks.spells.BoneThrowSpell;
 import com.soc.game.attacks.spells.ChargeSpell;
 import com.soc.game.attacks.spells.DaggerThrowSpell;
 import com.soc.game.attacks.spells.FireBreathSpell;
 import com.soc.game.attacks.spells.FireLionSpell;
 import com.soc.game.attacks.spells.FireballSpell;
 import com.soc.game.attacks.spells.FlameSpell;
 import com.soc.game.attacks.spells.IceConeSpell;
 import com.soc.game.attacks.spells.IcicleSpell;
 import com.soc.game.attacks.spells.InfernoSpell;
 import com.soc.game.attacks.spells.QuakebladeSpell;
 import com.soc.game.attacks.spells.RideTheLightningSpell;
 import com.soc.game.attacks.spells.SlashSpell;
 import com.soc.game.attacks.spells.Spell;
 import com.soc.game.attacks.spells.TentacleSpell;
 import com.soc.game.attacks.spells.VenomSwordSpell;
 import com.soc.game.attacks.spells.WhirlbladeSpell;
 import com.soc.game.attacks.spells.WindbladeSpell;
 import com.soc.game.components.Attack;
 import com.soc.game.components.Bounds;
 import com.soc.game.components.Buff;
 import com.soc.game.components.Character;
 import com.soc.game.components.Damage;
 import com.soc.game.components.Debuff;
 import com.soc.game.components.Delay;
 import com.soc.game.components.Drop;
 import com.soc.game.components.Enemy;
 import com.soc.game.components.Expires;
 import com.soc.game.components.Feet;
 import com.soc.game.components.Player;
 import com.soc.game.components.Position;
 import com.soc.game.components.Spawner;
 import com.soc.game.components.State;
 import com.soc.game.components.Stats;
 import com.soc.game.components.Velocity;
 import com.soc.game.map.Map;
 import com.soc.game.objects.Antidote;
 import com.soc.game.objects.Armor;
 import com.soc.game.objects.Item;
 import com.soc.game.objects.Potion;
 import com.soc.game.objects.Weapon;
 import com.soc.game.states.alterations.Burn;
 import com.soc.game.states.alterations.Poison;
 import com.soc.game.states.alterations.Venom;
 import com.soc.game.systems.AttackDelaySystem;
 import com.soc.game.systems.AttackProcessingSystem;
 import com.soc.game.systems.BuffProcessingSystem;
 import com.soc.game.systems.CameraSystem;
 import com.soc.game.systems.CollisionSystem;
 import com.soc.game.systems.DamageProcessingSystem;
 import com.soc.game.systems.DebuffProcessingSystem;
 import com.soc.game.systems.EffectSystem;
 import com.soc.game.systems.EnemyActuatorSystem;
 import com.soc.game.systems.EntitySpawningTimerSystem;
 import com.soc.game.systems.ExpiringSystem;
 import com.soc.game.systems.MovementSystem;
 import com.soc.game.systems.PlayerInputSystem;
 import com.soc.game.systems.RenderSystem;
 import com.soc.hud.HudSystem;
 import com.soc.screens.CreditsScreen;
 import com.soc.screens.GameOverScreen;
 import com.soc.screens.MenuScreen;
 import com.soc.screens.SplashScreen;
 import com.soc.utils.EffectsPlayer;
 import com.soc.utils.GameLoader;
 import com.soc.utils.GraphicsLoader;
 import com.soc.utils.LevelManager;
 import com.soc.utils.MapLoader;
 import com.soc.utils.MusicManager;
 import com.soc.utils.MusicPlayer;
 import com.soc.utils.SpawnerManager;
 import com.soc.utils.WallManager;
 
 public class SoC extends Game {
 	public static final int FRAME_WIDTH = 1440;
 	public static final int FRAME_HEIGHT = 900;
 	
 	public static SoC game;
 	public GameProgress progress;
 	public OrthographicCamera camera;
 	public Entity player;
 	public World world;
 	public Spell[] spells;
 	public Item [] items;
 	
 	public ComponentMapper<Position> positionmapper;
 	public ComponentMapper<Feet> feetmapper;
 	public ComponentMapper<Bounds> boundsmapper;
 	public ComponentMapper<Stats> statsmapper;
 	public ComponentMapper<Player> playermapper;
 	public ComponentMapper<Enemy> enemymapper;
 	public ComponentMapper<Expires> expiresmapper;
 	public ComponentMapper<Character> charactermapper;
 	public ComponentMapper<State> statemapper;
 	public ComponentMapper<Velocity> velocitymapper;
 	public ComponentMapper<Delay> delaymapper;
 	public ComponentMapper<Attack> attackmapper;
 	public ComponentMapper<Damage> damagemapper;
 	public ComponentMapper<Spawner> spawnermapper;
 	public ComponentMapper<Debuff> debuffmapper;
 	public ComponentMapper<Buff> buffmapper;
 	public ComponentMapper<Drop> dropmapper;
 
 
 	
 	public GroupManager groupmanager;
 	public TagManager tagmanager;
 	public LevelManager levelmanager;
 	public SpawnerManager spawnermanager;
 	public WallManager wallmanager;
 	public MusicManager musicmanager;
 
 	
 	public HudSystem hudSystem;
 	public RenderSystem renderSystem;
 	public CameraSystem cameraSystem;
 	public InputMultiplexer inputMultiplexer;
 	public EffectSystem effectSystem;
 	
 	public Map map;
 	
 	public Skin skin;
 	
 	public boolean pause;
 	
 	public Stack<Screen> screens;
 	public Stack<InputProcessor> processors;
 		
 	
 	
 	@SuppressWarnings("unchecked")
 	@Override
 	public void create() {		
 		SoC.game = this;
 		skin = new Skin(Gdx.files.internal( "resources/skin2.json" ));
 		GraphicsLoader.initialize();
 		MusicPlayer.initialize();
 		EffectsPlayer.initialize();
 		GameLoader.initialize();
 		
 		spells = new Spell[Constants.Spells.SPELL_NUMBER];
 		spells[Constants.Spells.DAGGER_THROW] = new DaggerThrowSpell(); 
 		spells[Constants.Spells.BONE_THROW] = new BoneThrowSpell();
 		spells[Constants.Spells.SLASH] = new SlashSpell();
 		spells[Constants.Spells.CHARGE] = new ChargeSpell();
 		spells[Constants.Spells.ARROW] = new ArrowSpell();
 		spells[Constants.Spells.WHIRLBLADE] = new WhirlbladeSpell();
 		spells[Constants.Spells.QUAKEBLADE] = new QuakebladeSpell();
 		spells[Constants.Spells.BITE] = new BiteSpell();
 		spells[Constants.Spells.VENOMSWORD] = new VenomSwordSpell();
 		spells[Constants.Spells.WINDBLADE] = new WindbladeSpell();
 		spells[Constants.Spells.TENTACLES] = new TentacleSpell();
 		spells[Constants.Spells.FLAME] = new FlameSpell();
 		spells[Constants.Spells.FIREBREATH] = new FireBreathSpell();
 		spells[Constants.Spells.FIREBALL] = new FireballSpell();
 		spells[Constants.Spells.ICICLE] = new IcicleSpell();
 		spells[Constants.Spells.FIREBREATH]=new FireBreathSpell();
 		spells[Constants.Spells.RIDE_THE_LIGHTNING]=new RideTheLightningSpell();
 		spells[Constants.Spells.INFERNO]= new InfernoSpell();
 		spells[Constants.Spells.FIRELION] = new FireLionSpell();
 		spells[Constants.Spells.ICECONE] = new IceConeSpell();
 		
 		items=new Item[Constants.Items.ITEM_NUMBER];
 		items[Constants.Items.NONE] = null;
 		items[Constants.Items.HEALTH_POTION] = new Potion(Constants.Items.HEALTH_POTION,"Health potion","potion-health.png","\nHEALTH POTION\n\nRecovers 100 health.",100,0);
 		items[Constants.Items.MANA_POTION] = new Potion(Constants.Items.MANA_POTION,"Mana potion","potion-mana.png","\nMANA POTION\n\nRecovers 100 mana.",0,100);
 		items[Constants.Items.HEALTH_ULTRAPOTION] = new Potion(Constants.Items.HEALTH_ULTRAPOTION,"Health ultrapotion","potion-health-big.png","\nHEALTH ULTRAPOTION\n\nRecovers 300 health.",300,0);
 		items[Constants.Items.MANA_ULTRAPOTION] = new Potion(Constants.Items.MANA_ULTRAPOTION,"Mana ultrapotion","potion-mana-big.png","\nMANA ULTRAPOTION\n\nRecovers 300 mana.",0,300);
 		items[Constants.Items.MIX_POTION] = new Potion(Constants.Items.MIX_POTION,"Mix potion","potion-mix.png","\nREMEDY\n\nRecovers 150 health and mana.",150,150);
 		items[Constants.Items.MIX_ULTRAPOTION] = new Potion(Constants.Items.MIX_ULTRAPOTION,"Mix Ultrapotion","potion-mix-big.png","\nELIXIR\n\nRecovers 300 health and mana.",300,300);
 		items[Constants.Items.ANTIDOTE] = new Antidote(Constants.Items.ANTIDOTE,"Antidote","antidote.png","\nANTIDOTE\n\nCures poison.", Poison.class, Venom.class);
 		items[Constants.Items.ANTIBURN] = new Antidote(Constants.Items.ANTIBURN,"Antiburn","antiburn.png","\nANTIBURN\n\nRemoves burn.", Burn.class);	
 		items[Constants.Items.STONE_AXE] = new Weapon(Constants.Items.STONE_AXE, "Stone axe", "stone-axe.png", "\nSTONE AXE:\n\n+10 strength", 10, 0, 0);
 		items[Constants.Items.IRON_AXE] = new Weapon(Constants.Items.IRON_AXE, "Iron axe", "iron-axe.png", "\nIRON AXE:\n\n+25 strength", 25, 0, 0);
 		items[Constants.Items.GOLD_AXE] = new Weapon(Constants.Items.GOLD_AXE, "Gold axe", "gold-axe.png", "\nGOLD AXE:\n\n+40 strength", 40, 0, 0);
 		items[Constants.Items.BRONZE_SWORD] = new Weapon(Constants.Items.BRONZE_SWORD, "Bronze sword", "bronze-sword.png", "\nSTONE AXE:\n\n+5 strength\n+5 agility", 5, 5, 0);
 		items[Constants.Items.SILVER_SWORD] = new Weapon(Constants.Items.SILVER_SWORD, "Silver sword", "silver-sword.png", "\nSILVER SWORD:\n\n+25 strength\n+15 agility", 25, 15, 0);
 		items[Constants.Items.GOLD_SWORD] = new Weapon(Constants.Items.GOLD_SWORD, "Gold sword", "gold-sword.png", "\nGOLD SWORD:\n\n+40 strength\n+35 agility", 40, 35, 0);
 		items[Constants.Items.WOODEN_SHIELD] = new Armor(Constants.Items.WOODEN_SHIELD, "Wooden shield", "wooden-shield.png", "\nWOODEN SHIELD\n\n+3 armor", 3);
 		items[Constants.Items.IRON_SHIELD] = new Armor(Constants.Items.IRON_SHIELD, "Iron shield", "iron-shield.png", "\nIRON SHIELD\n\n+12 armor", 12);
 		items[Constants.Items.GOLD_SHIELD] = new Armor(Constants.Items.GOLD_SHIELD, "Gold shield", "gold-shield.png", "\nGOLD SHIELD\n\n+20 armor", 20);
 		items[Constants.Items.LEATHER_HELM] = new Armor(Constants.Items.LEATHER_HELM, "Leather helm", "leather-helm.png", "\nWOODEN SHIELD\n\n+3 armor", 5);
 		items[Constants.Items.IRON_HELM] = new Armor(Constants.Items.IRON_HELM, "Iron helm", "iron-helm.png", "\nIRON HELM\n\n+8 armor", 8);
 		items[Constants.Items.GOLD_HELM] = new Armor(Constants.Items.GOLD_HELM, "Gold helm", "gold-helm.png", "\nGOLD HELM\n\n+15 armor", 15);
 		items[Constants.Items.WOOD_WAND] = new Weapon(Constants.Items.WOOD_WAND, "Wood wand", "wood-wand.png","\nWOOD WAND\n\n+10 intelligence", 0,0,10);
 		items[Constants.Items.MAGIC_WAND] = new Weapon(Constants.Items.MAGIC_WAND, "Magic wand", "magic-wand.png","\nMAGIC WAND\n\n+25 intelligence", 0,0,25);
 		items[Constants.Items.DIVINE_WAND] = new Weapon(Constants.Items.DIVINE_WAND, "Divine wand", "divine-wand.png","\nDIVINE WAND\n\n+45 intelligence", 0,0,40);
 
 		world = new World();
 		positionmapper = world.getMapper(Position.class);
 		feetmapper = world.getMapper(Feet.class);
 		boundsmapper = world.getMapper(Bounds.class);
 		statsmapper = world.getMapper(Stats.class);
 		playermapper = world.getMapper(Player.class);
 		expiresmapper = world.getMapper(Expires.class);
 		charactermapper = world.getMapper(Character.class);
 		statemapper = world.getMapper(State.class);
 		velocitymapper = world.getMapper(Velocity.class);
 		delaymapper = world.getMapper(Delay.class);
 		attackmapper = world.getMapper(Attack.class);
 		damagemapper = world.getMapper(Damage.class);
 		spawnermapper = world.getMapper(Spawner.class);
 		enemymapper = world.getMapper(Enemy.class);
 		debuffmapper = world.getMapper(Debuff.class);
 		buffmapper = world.getMapper(Buff.class);
 		dropmapper = world.getMapper(Drop.class);
 		
 		inputMultiplexer=new InputMultiplexer();
 		Gdx.input.setInputProcessor(inputMultiplexer);
 		
 		camera = new OrthographicCamera();
 		
 		groupmanager = new GroupManager();
 		tagmanager = new TagManager();
 		levelmanager = new LevelManager();
 		spawnermanager = new SpawnerManager();
 		wallmanager = new WallManager();
 		musicmanager = new MusicManager();
 		
 		world.setManager(spawnermanager);
 		world.setManager(groupmanager);
 		world.setManager(tagmanager);
 		world.setManager(levelmanager);
 		world.setManager(wallmanager);
 		world.setManager(musicmanager);
 		
 	    world.setSystem(new PlayerInputSystem());
 		world.setSystem(new AttackDelaySystem());
 		world.setSystem(new AttackProcessingSystem());
 	    world.setSystem(new EnemyActuatorSystem());
 	    world.setSystem(new EntitySpawningTimerSystem());
 	    world.setSystem(new BuffProcessingSystem());
 	    world.setSystem(new DebuffProcessingSystem());
 	    world.setSystem(new DamageProcessingSystem());
 	    world.setSystem(new CollisionSystem());	
 	    world.setSystem(new MovementSystem());
 	    world.setSystem(new ExpiringSystem());
 	    effectSystem=world.setSystem(new EffectSystem());
 	    
 		cameraSystem = SoC.game.world.setSystem( new CameraSystem(camera), true);
 		renderSystem = SoC.game.world.setSystem( new RenderSystem(camera), true );
 		hudSystem = SoC.game.world.setSystem(new HudSystem(camera),true);
 				
 		Gdx.graphics.setDisplayMode(Gdx.graphics.getDesktopDisplayMode().width, Gdx.graphics.getDesktopDisplayMode().height, true);
 		screens=new Stack<Screen>();
 		processors = new Stack<InputProcessor>();
 			
 		//Save, New, Load game handler.
		//setScreen(new MenuScreen(this));
		setScreen(new CreditsScreen(this));
 
 	}
 	
 	public void openMenu(){
 		SoC.game.screens.push(SoC.game.getScreen());
 	}
 	
 	public void changeMap(String name){
 		MapLoader.loadMap(name);
 	}
 	
 	public void resetWorld(){
 		ImmutableBag<Entity> enemies = world.getManager(GroupManager.class).getEntities(Constants.Groups.MAP_BOUND);
 		for(int i = 0; i < enemies.size(); i++){
 			world.deleteEntity(enemies.get(i));
 		}
 	}
 	
 	public void closeMenu(){
 		Screen menu = getScreen();
 		setScreen(screens.pop());
 		menu.dispose();
 	}
 	
 	public void openSplashScreen(){
 		setScreen(new SplashScreen(this));
 	}
 	public void closeSplashScreen(){
 		Screen splash = getScreen();
 		setScreen(screens.pop());
 		splash.dispose();
 	}
 	public void openGameOverScren(){
 		game.setScreen(new GameOverScreen(game));
 	}
 	
 	public void openMenuScreen(){
 		game.setScreen(new MenuScreen(game));
 	}
 
 	public static void main(String[] args) {
 		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
 		cfg.width = FRAME_WIDTH;
 		cfg.height = FRAME_HEIGHT;
 		cfg.useGL20 = true;
 		cfg.resizable = false;
 		//cfg.fullscreen = true;
 		cfg.title = "GameXYZ";
 		new LwjglApplication(new SoC(), cfg);
 	}
 
 	public void restoreInputProcessors() {
 		inputMultiplexer.clear();
 		while(!processors.isEmpty()){
 			inputMultiplexer.addProcessor(processors.pop());
 		}
 	}
 	
 	public void archiveProcessors(){
 		Array<InputProcessor> procs = inputMultiplexer.getProcessors();
 		for(int i = 0; i < procs.size; i++){
 			processors.push(procs.get(i));
 		}
 		inputMultiplexer.clear();
 	}
 	
 	public void clearProcessors(){
 		processors.empty();
 		inputMultiplexer.clear();
 	}
 	
 	public void clearScreens(){
 		screens.empty();
 	}
 
 }
