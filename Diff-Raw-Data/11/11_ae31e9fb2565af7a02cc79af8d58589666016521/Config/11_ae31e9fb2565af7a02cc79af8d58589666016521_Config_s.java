 package silentAbyss.configuration;
 
 public class Config {
 	
 	/*
 	 * Misc. config settings
 	 */
 	public static int SHARDS_PER_GEM;
 	public static final int SHARDS_PER_GEM_DEFAULT = 9;
 	public static final String SHARDS_PER_GEM_CONFIGNAME = "ShardsPerGem";
 	
 	/*
 	 * Graphic config settings
 	 */
 	// nothing... yet
 	
 	/*
 	 * Audio config settings
 	 */
 	public static String ENABLE_SOUNDS;
 	public static final String ENABLE_SOUNDS_CONFIGNAME = "Sounds.Enabled";
 	public static final String ENABLE_SOUNDS_DEFAULT = "all";
 	
 	/*
 	 * Sigil config settings
 	 */
 	public static int SIGIL_BASE_BREAK_CHANCE;
 	public static final int SIGIL_BASE_BREAK_CHANCE_DEFAULT = 50;
 	public static final String SIGIL_BASE_BREAK_CHANCE_CONFIGNAME = "Sigil.BaseBreakChance";
 	
 	public static int SIGIL_BASE_USE_DURATION;
 	public static final int SIGIL_BASE_USE_DURATION_DEFAULT = 30;
 	public static final String SIGIL_BASE_USE_DURATION_CONFIGNAME = "Sigil.BaseUseDuration";
 	
 	public static int SIGIL_BASE_PROJECTILE_DAMAGE;
 	public static final int SIGIL_BASE_PROJECTILE_DAMAGE_DEFAULT = 8;
 	public static final String SIGIL_BASE_PROJECTILE_DAMAGE_CONFIGNAME = "Sigil.BaseProjectileDamage";
 	
 	public static int SIGIL_BASE_SUPPORT_DURATION;
 	public static final int SIGIL_BASE_SUPPORT_DURATION_DEFAULT = 1200;
 	public static final String SIGIL_BASE_SUPPORT_DURATION_CONFIGNAME = "Sigil.BaseSupportDuration";
 	
 	/*
 	 * World chaos config settings
 	 */
 	public static int CHAOS_PER_WORLD_TICK;
 	public static final int CHAOS_PER_WORLD_TICK_DEFAULT = 10;
 	public static final String CHAOS_PER_WORLD_TICK_CONFIGNAME = "Chaos.ChaosPerWorldTick";
 	
 	public static int CHAOS_MAX_AMBIENT;
 	public static final int CHAOS_MAX_AMBIENT_DEFAULT = 10000;
 	public static final String CHAOS_MAX_AMBIENT_CONFIGNAME = "Chaos.MaxAmbientChaos";
 	
 	public static int CHAOS_COST_ABYSS_TELEPORTER;
 	public static final int CHAOS_COST_ABYSS_TELEPORTER_DEFAULT = 100;
 	public static final String CHAOS_COST_ABYSS_TELEPORTER_CONFIGNAME = "Chaos.Cost.AbyssTeleporter";
 	
 	public static int CHAOS_COST_SIGIL;
 	public static final int CHAOS_COST_SIGIL_DEFAULT = 25;
 	public static final String CHAOS_COST_SIGIL_CONFIGNAME = "Chaos.Cost.Sigil";
 	
 	public static int CHAOS_COST_SIGIL_SCEPTER;
 	public static final int CHAOS_COST_SIGIL_SCEPTER_DEFAULT = 50;
 	public static final String CHAOS_COST_SIGIL_SCEPTER_CONFIGNAME = "Chaos.Cost.SigilScepter";
 	
 	public static int CHAOS_COST_MENDING;
 	public static final int CHAOS_COST_MENDING_DEFAULT = 5;
 	public static final String CHAOS_COST_MENDING_CONFIGNAME = "Chaos.Cost.Mending";
 	
 	public static int CHAOS_COST_NIHIL;
 	public static final int CHAOS_COST_NIHIL_DEFAULT = 0;
 	public static final String CHAOS_COST_NIHIL_CONFIGNAME = "Chaos.Cost.Nihil";
 	
 	/*
 	 * World chaos event config settings
 	 */
 	public static int METEOR_SHOWER_RARITY;
 	public static final int METEOR_SHOWER_RARITY_DEFAULT = 5400;
 	public static final String METEOR_SHOWER_RARITY_CONFIGNAME = "Chaos.Event.Meteor.Rarity";
 	
 	public static int METEOR_SHOWER_COUNT;
 	public static final int METEOR_SHOWER_COUNT_DEFAULT = 42;
 	public static final String METEOR_SHOWER_COUNT_CONFIGNAME = "Chaos.Event.Meteor.Count";
 	
 	public static int METEOR_SHOWER_RADIUS;
 	public static final int METEOR_SHOWER_RADIUS_DEFAULT = 32;
 	public static final String METEOR_SHOWER_RADIUS_CONFIGNAME = "Chaos.Event.Meteor.Radius";
 	
 	public static int METEOR_SHOWER_DURATION;
 	public static final int METEOR_SHOWER_DURATION_DEFAULT = 20;
 	public static final String METEOR_SHOWER_DURATION_CONFIGNAME = "Chaos.Event.Meteor.Duration";
 	
 	public static int METEOR_SHOWER_WARNING_DURATION;
 	public static final int METEOR_SHOWER_WARNING_DURATION_DEFAULT = 10;
 	public static final String METEOR_SHOWER_WARNING_DURATION_CONFIGNAME = "Chaos.Event.Meteor.WarningDuration";
 	
 	/*
 	 * World generation config settings
 	 */
 	public static int WORLD_ABYSS_GEM_CLUSTER_COUNT;
 	public static final int WORLD_ABYSS_GEM_CLUSTER_COUNT_DEFAULT = 4;
 	public static final String WORLD_ABYSS_GEM_CLUSTER_COUNT_CONFIGNAME = "World.AbyssGemClusterCount";
 	
 	public static int WORLD_ABYSS_GEM_CLUSTER_SIZE;
 	public static final int WORLD_ABYSS_GEM_CLUSTER_SIZE_DEFAULT = 8;
 	public static final String WORLD_ABYSS_GEM_CLUSTER_SIZE_CONFIGNAME = "World.AbyssGemClusterSize";
 	
 	public static int WORLD_ABYSS_GEM_MAX_HEIGHT;
 	public static final int WORLD_ABYSS_GEM_MAX_HEIGHT_DEFAULT = 40;
 	public static final String WORLD_ABYSS_GEM_MAX_HEIGHT_CONFIGNAME = "World.AbyssGemMaxHeight";
 	
 	public static int WORLD_ABYSSITE_CLUSTER_COUNT;
 	public static final int WORLD_ABYSSITE_CLUSTER_COUNT_DEFAULT = 1;
 	public static final String WORLD_ABYSSITE_CLUSTER_COUNT_CONFIGNAME = "World.AbyssiteClusterCount";
 	
 	public static int WORLD_ABYSSITE_CLUSTER_SIZE;
 	public static final int WORLD_ABYSSITE_CLUSTER_SIZE_DEFAULT = 8;
 	public static final String WORLD_ABYSSITE_CLUSTER_SIZE_CONFIGNAME = "World.AbyssiteClusterSize";
 	
 	public static int WORLD_ABYSSITE_MAX_HEIGHT;
 	public static final int WORLD_ABYSSITE_MAX_HEIGHT_DEFAULT = 20;
 	public static final String WORLD_ABYSSITE_MAX_HEIGHT_CONFIGNAME = "World.AbyssiteMaxHeight";
 	
 	public static int WORLD_PURITE_CLUSTER_COUNT;
 	public static final int WORLD_PURITE_CLUSTER_COUNT_DEFAULT = 1;
	public static final String WORLD_PURITE_CLUSTER_COUNT_CONFIGNAME = "World.AbyssiteClusterCount";
 	
 	public static int WORLD_PURITE_CLUSTER_SIZE;
 	public static final int WORLD_PURITE_CLUSTER_SIZE_DEFAULT = 8;
	public static final String WORLD_PURITE_CLUSTER_SIZE_CONFIGNAME = "World.AbyssiteClusterSize";
 	
 	public static int WORLD_PURITE_MAX_HEIGHT;
 	public static final int WORLD_PURITE_MAX_HEIGHT_DEFAULT = 20;
	public static final String WORLD_PURITE_MAX_HEIGHT_CONFIGNAME = "World.AbyssiteMaxHeight";
 	
 	/*
 	 * World structure config settings
 	 */
 	public static int STRUCTURE_SHRINE_RARITY;
	public static final int STRUCTURE_SHRINE_RARITY_DEFAULT = 64;
 	public static final String STRUCTURE_SHRINE_RARITY_CONFIGNAME = "World.ShrineRarity";
 	
 }
