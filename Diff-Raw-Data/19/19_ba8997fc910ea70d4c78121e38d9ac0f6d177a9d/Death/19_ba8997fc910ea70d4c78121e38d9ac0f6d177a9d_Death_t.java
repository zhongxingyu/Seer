 package no.runsafe.nchat;
 
 public enum Death
 {
 	FALL(" hit the ground too hard", false),
 	SUFFOCATION(" suffocated in a wall", false),
 	SLAIN(" was slain by ", true),
 	DROWNED(" drowned", false),
 	EXPLOSION(" blew up", false),
 	LAVA(" tried to swim in lava", false),
 	UNKNOWN(" died", false),
 	CACTUS(" was pricked to death", false),
 	FIRE(" went up in flames", false),
 	ARROW(" was shot by ", true),
 	STARVATION(" starved to death", false),
 	KILLED(" was killed by ", true),
 	VOID(" fell out of the world", false);
 
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
