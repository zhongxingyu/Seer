 package com.soc.core;
 
 public class Constants {
 
 	public static class Characters{
 		public static final int WIDTH = 32;
 		public static final int HEIGHT = 47;
 		public static final int FEET_WIDTH = 32;
 		public static final int FEET_HEIGTH = 10;
 		public static final int VELOCITY=150;
 		public static final String WARRIOR = "warrior";
 		public static final String MAGE = "mage";
 	}
 	
 	public static class Groups{
 		public static final String PLAYERS = "player";
 		public static final String ENEMIES = "enemy";
 		public static final String WALLS = "wall";
 		public static final String ITEMS = "item";
 		
 		public static final String MAGGOTS = "maggot";
 		public static final String SLIMES = "slime";
 		public static final String SKELETONS = "skeleton";
 		public static final String GREEN_KNIGHTS = "green knight";
 		public static final String GOLD_KNIGHTS = "gold knight";
 		public static final String BOW_KNIGHTS = "bow knight";
 		public static final String BALLISTAS= "ballista";
 		public static final String ZOMBIES= "zombie";
 		public static final String SKULL_KNIGHT= "satan";
 		public static final String GAIA_AIR = "gaia-air";
 		public static final String GAIA_DARK = "gaia-dark";
 		public static final String GAIA_FLAME = "gaia-flame";
 		public static final String GAIA = "gaia";
 		public static final String GAIAS = "gaia-flame";
 		public static final String EYEBALLS = "eyeball";
 		public static final String RED_MONSTER = "mid-monster";
 		public static final String FIRE_STONE = "fire-stone-monster";
 		public static final String ANTI_VENOM_FOUNTAIN = "anti-venom-fountain";
 		public static final String RIGHT_MONSTER = "right-monster";
 		public static final String BLACK_MAGE = "black mage";
 		public static final String KNIGHT_CAPTAIN = "knight captain";
 		
 		public static final String ENEMY_ATTACKS = "enemyattack";
 		public static final String PLAYER_ATTACKS = "playerattack";
 		public static final String MAP_BOUND = "mapbound"; //The ones which dissapear with the map
 		public static final String LEVEL = "level";
 		public static final String CHARACTERS = "characters";
 		public static final String PROJECTILES = "projectiles";
 		public static final String DESTROYABLE_PROJECTILES="destroyable_projectiles";
 	}
 	
 	public static class World{
 		public static final float TILE_SIZE = 32f;
 		public static final float TILE_FACTOR = 0.03125f;
 		public static final int TILE_WALKABLE = 0;
 		public static final int TILE_OBSTACLE = 1; 
 		public static final int TILE_GATE = 2;
 		public static final int TILE_LEVEL_CHANGE = 3;
 		public static final int TILE_STAIRS = 4;
 		public static final int TILE_UNWALKABLE = 5;
 		public static final int TILE_LAVA = 6;
 		public static final int TILE_HOLE = 7;
 		public static final int TILE_DIALOG = 8;
 		public static final int TILE_PUSH = 9;
 		public static final int TILE_TELEPORT = 10;
 		public static final int LAYERS_PER_LEVEL = 4;
 	}
 	
 	public static class Configuration{
 		public static final String RESOURCE_DIR = "resources/";
 		public static final String MUSIC_DIR = "resources/music/";
 		public static final String MAP_DIR = "resources/map/";
 		public static final String LEVEL_DIR = "resources/level/";
 		public static final float LABEL_SPEED = 50;
 		public static final float LABEL_DURATION = 0.6f;
 	}
 	
 	public static class Classes{
 		public static final int NONE = 0;
 		public static final int WARRIOR = 1;
 		public static final int HUNTER = 2;
 		public static final int MAGE = 3;
 	}
 	
 	public static class Spells{
 		public static final int NO_SPELL = -1;
 		public static final int SLASH = 0;
 		public static final int DAGGER_THROW = 1;
 		public static final int ICICLE = 2;
 		public static final int FIREBALL = 3;
 		public static final int ICEBLAST = 4;
 		public static final int BONE_THROW= 5;
 		public static final int CHARGE=6;
 		public static final int ARROW=7;
 		public static final int WHIRLBLADE=8;
 		public static final int QUAKEBLADE = 9;
 		public static final int BITE = 10;
 		public static final int VENOMSWORD = 11;
 		public static final int WINDBLADE = 12;
 		public static final int TENTACLES = 13;
 		public static final int FLAME=14;
 		public static final int FIREBREATH=15;
 		public static final int RIDE_THE_LIGHTNING=16;
 		public static final int INFERNO=17;
 		public static final int FIRELION=18;
 		
 		public static final int SPELL_NUMBER = 19;
 		
 		public static final float FIRELION_DELAY = 0.5f;
 		public static final float AIR_BLAST_TIME = 0.5f;
 		public static final float AIR_BLAST_TICK_INTERVAL = 0.1f;
 		public static final float AIR_BLAST_RADIUS_INCREASE = 50f; 
 		public static final float RIDE_THE_LIGHTNING_DURATION = 1.5f;
 		public static final float ICICLE_SPEED = 300f;
 		public static final float ICICLE_RANGE = 500f;
 		public static final float FIREBALL_SPEED = 400f;
 		public static final float FIREBALL_RANGE = 800f;
 		public static final int TORNADO_RANGE = 600;
 		public static final int TORNADO_SPEED = 500;
 		public static final int FIREBREATH_SPEED = 1000;
 		public static final float BALLISTA_FIRE_RATE = 1.75f;
 		public static final int DAGGER_SPEED = 900;
 		public static final int DAGGER_RANGE = 700;
 		public static final int ARROW_SPEED = 900;
 		public static final int CHARGE_SPEED = 700;
 		public static final float CHARGE_DURATION = 1.5f;
 		public static final int CHARGE_BOX = 70;
 		public static final float QUAKEBLADE_TICK_INTERVAL = 0.3f;
 		public static final int QUAKEBLADE_RADIUS_INITIAL = 30;
 		public static final int QUAKEBLADE_RADIUS_INCREASE = 16;
 		public static final float SPIN_DURATION = 3.5f;
 		public static final float SPIN_RADIUS = 60f;
 		public static final float POISON_CLOUD_DURATION = 7f;
 		public static final float POISON_CLOUD_RADIUS = 65f;
 		public static final float METEOR_BLAST_TICK_INTERVAL = 0.1f;
 		public static final int METEOR_BLAST_TICK_NUMBER = 6;
 		public static final float METEOR_FALL_DISTANCE = 800f;
 		public static final float METEOR_FALL_SPEED = 600f;
 		public static final float METEOR_RADIUS = 35f;
 		public static final float METEOR_RADIUS_INCREASE = 10f;
 		public static final float WINDBLADE_SPEED = 700f;
 		public static final float TENTACLES_DURATION = 1.5f;
 		public static final float TENTACLES_PREPARE = 1.03f;
 		public static final float TENTACLES_RADIUS = 170f;
 		public static final float AIR_CIRCLE_TIME = 2f;
 		public static final float BONE_THROW_SPEED = 1000f;
 		public static final float FIREBALL_THROW_SPEED = 1000f;
 		public static final int STOMP_RADIUS = 100;
 	}
 	
 	
 	public static class Attributes{
 		public final static String MAP = "map"; 
 		public final static String MUSIC = "music";
 		public final static String POSITION = "position";
 		public final static String INVENTORY = "inventory";
 		public final static String STATS = "music";
 		public final static String CLASS = "class";
 	}
 	
 	public static class Tags{
 		public final static String PLAYER = "player";
 	}
 		
 	public static class Items{
 		public final static int INVENTORY_SIZE=20;
 		public final static int ITEM_NUMBER=24;
 		public final static int NONE=0;
 		public final static int HEALTH_POTION=1;
 		public final static int MANA_POTION=2;
 		public final static int HEALTH_ULTRAPOTION=3;
 		public final static int MANA_ULTRAPOTION=4;
 		public final static int MIX_POTION=5;
 		public final static int MIX_ULTRAPOTION=6;
 		public final static int STONE_AXE=7;
 		public final static int IRON_AXE=8;
 		public final static int GOLD_AXE=9;
 		public final static int BRONZE_SWORD=10;
 		public final static int SILVER_SWORD=11;
 		public final static int GOLD_SWORD=12;
 		public final static int LEATHER_HELM=13;
 		public final static int IRON_HELM=14;
 		public final static int GOLD_HELM=15;
 		public final static int WOODEN_SHIELD=16;
 		public final static int IRON_SHIELD=17;
 		public final static int GOLD_SHIELD=18;
 		public final static int ANTIDOTE=19;
 		public final static int ANTIBURN=20;
 		public final static int WOOD_WAND=21;
 		public final static int MAGIC_WAND=22;
 		public final static int DIVINE_WAND=23;
 		
		public final static int ITEM_SIZE = 32;
 
 
 	}
 	
 	public static class Alteration{
 		public final static float BURN_DURATION=5f;
 		public final static float BURN_TICK_INTERVAL=1f;
 		public final static int BURN_DAMAGE = 10;
 		public final static float LAVA_BURN_DURATION=5f;
 		public final static float LAVA_BURN_TICK_INTERVAL=1f;
 		public final static float  LAVA_BURN_DAMAGE = 0.10f;
 		public final static float POISON_DURATION = 10f;
 		public final static float POISON_TICK_INTERVAL = 1f;
 		public final static int POISON_DAMAGE = 5;
 		public final static float VENOM_DURATION = 60f;
 		public final static float VENOM_TICK_INTERVAL = 1f;
 		public final static int VENOM_DAMAGE = 5;
 	}
 	
 	public static class Buff{
 		public final static float SHIELD_DURATION=10F;
 		public final static float SHIELD_GAINHEALTH=50F;
 		public final static float RAGE_DURATION = 60f;
 		public final static float GAIN_STRENGTH = 5f;
 		public final static float TELEPORT_CAST_TIME = 1f;
 		public final static float SHIELD_CHARGE_TIME=0.3f;
 	}
 	
 	public static class BuffColors{
 		//public final static String GREEN="greenCast";
 		public final static String RED="red";
 		public final static String DARK = "dark";
 	}
 	
 }
