 package no.runsafe.nchat;
 
 public enum Death
 {
 	ANVIL(" was squashed by a falling anvil", false),
 	CACTUS(" was pricked to death", false),
	CACTUS_FLEE(" walked into a cactus whilst trying to escape ", true),
 	ARROW(" was shot by arrow", false),
 	DROWNED(" drowned", false),
 	EXPLOSION(" blew up", false),
 	EXPLOSION_BY(" was blown up by ", true),
 	FALL(" hit the ground too hard", false),
 	FALL_LADDER(" fell off a ladder", false),
 	FALL_VINE(" fell off some vines", false),
 	FALL_WATER(" fell out of the water", false),
 	FALL_HIGH(" fell from a high place", false),
 	FALL_FIRE(" fell into a patch of fire", false),
 	FALL_CACTI(" fell into a patch of cacti", false),
 	FALL_GHAST(" was blown from a high place", false),
 	FALL_DOOMED(" was doomed to fall by ", true),
 	FALL_DOOM(" was doomed to fall", false),
 	FIRE(" went up in flames", false),
 	BURNING(" burned to death", false),
 	BURNING_WHILE(" was burnt to a crisp while fighting ", true),
 	FIRE_WHILE(" walked into a fire whilst fighting ", true),
 	SLAIN(" was slain by ", true),
 	SHOT(" was shot by ", true),
 	FIREBALL(" was fireballed by ", true),
 	KILLED(" was killed by ", true),
 	WEAPON1(" got finished off by ", true),
 	WEAPON2(" got slain by ", true),
 	LAVA(" tried to swim in lava", false),
 	LAVA_FLEE(" tried to swim in lava while trying to escape ", true),
 	UNKNOWN(" died", false),
 	MAGIC(" was killed by magic", false),
 	STARVATION(" starved to death", false),
 	SUFFOCATION(" suffocated in a wall", false),
 	THORNS(" was killed while trying to hurt ", true),
 	PUMMELED(" was pummeled by ", true),
 	VOID(" fell out of the world", false),
 	VOID_HIGH(" fell out of the world", false),
 	VOID_KNOCK(" was knocked into the void by ", true),
 	WITHER(" withered away", false);
 
 	private final String defaultMessage;
 	private final boolean entityInvolved;
 
 	Death(String defaultMessage, boolean entityInvolved)
 	{
 		this.defaultMessage = defaultMessage;
 		this.entityInvolved = entityInvolved;
 	}
 
 	public boolean hasEntityInvolved()
 	{
 		return this.entityInvolved;
 	}
 
 	public String getDefaultMessage()
 	{
 		return this.defaultMessage;
 	}
 }
