 public class BoomSpell extends Spell {
 
 	// cost values
 	private int REDSTONE_COST;
 	private int OTHER_COST;
 	private String OTHER_COST_NAME;
 	private int OTHER_COST_AMT;
 	
 	// options
 	private int BLAST_RADIUS;
 	private boolean PLUGIN_CHECK;
 	
 	// strings
 	private String STR_CAST;
 	private String STR_CAST_OTHERS;
 	
 	// reagents
 	private int [][] reagents;
 
 	public BoomSpell(MagicSpellsListener listener, PropertiesFile properties) {
 		super(listener,properties);
 		
 		// register spell
 		listener.registerSpellName(properties.getString("boom-spellname","boom"),this,properties.getString("boom-desc","Blows stuff up at target."));
 		
 		// get properties
 		REDSTONE_COST = properties.getInt("boom-redstone-cost",10);
 		OTHER_COST = properties.getInt("boom-other-cost-type",289);
 		OTHER_COST_NAME = properties.getString("boom-other-cost-name","gunpowder");
 		OTHER_COST_AMT = properties.getInt("boom-other-cost-amt",4);
 		
 		BLAST_RADIUS = properties.getInt("boom-blast-radius",4);
 		PLUGIN_CHECK = properties.getBoolean("boom-plugin-check",true);
 		
 		STR_CAST = properties.getString("boom-cast-str","Boom!");
 		STR_CAST_OTHERS = properties.getString("boom-cast-others-str","Boom!");
 		
 		// setup reagents
 		reagents = new int [][] {{REDSTONE_DUST, REDSTONE_COST},{OTHER_COST,OTHER_COST_AMT}};
 	}
 
 	public boolean cast(Player player, String [] command) {
 		// get targeted block
 		HitBlox hit = new HitBlox(player);
 		Block target = hit.getTargetBlock();
 		
 		// check destroy against other plugins
 		if (PLUGIN_CHECK) {
 			boolean cancel = false;
			cancel = cancel || (Boolean)etc.getLoader().callHook(PluginLoader.Hook.BLOCK_DESTROYED, new Object [] {player.getUser(), target});
			cancel = cancel || (Boolean)etc.getLoader().callHook(PluginLoader.Hook.BLOCK_DESTROYED, new Object [] {player.getUser(), etc.getServer().getBlockAt(target.getX() + (BLAST_RADIUS), target.getY(), target.getZ())});
			cancel = cancel || (Boolean)etc.getLoader().callHook(PluginLoader.Hook.BLOCK_DESTROYED, new Object [] {player.getUser(), etc.getServer().getBlockAt(target.getX() - (BLAST_RADIUS), target.getY(), target.getZ())});
			cancel = cancel || (Boolean)etc.getLoader().callHook(PluginLoader.Hook.BLOCK_DESTROYED, new Object [] {player.getUser(), etc.getServer().getBlockAt(target.getX(), target.getY() + (BLAST_RADIUS), target.getZ())});
			cancel = cancel || (Boolean)etc.getLoader().callHook(PluginLoader.Hook.BLOCK_DESTROYED, new Object [] {player.getUser(), etc.getServer().getBlockAt(target.getX(), target.getY() - (BLAST_RADIUS), target.getZ())});
			cancel = cancel || (Boolean)etc.getLoader().callHook(PluginLoader.Hook.BLOCK_DESTROYED, new Object [] {player.getUser(), etc.getServer().getBlockAt(target.getX(), target.getY(), target.getZ() + (BLAST_RADIUS))});
			cancel = cancel || (Boolean)etc.getLoader().callHook(PluginLoader.Hook.BLOCK_DESTROYED, new Object [] {player.getUser(), etc.getServer().getBlockAt(target.getX(), target.getY(), target.getZ() - (BLAST_RADIUS))});
 			
 			// a plugin returned true to cancel destruction
 			if (cancel) {
 				player.sendMessage(TEXT_COLOR + MagicSpellsListener.STR_CAST_FAIL);
 				return false;
 			}
 		}
 		
 		if (removeReagents(player, reagents)) {		
 			// find this method by searching for "explode"
 			etc.getServer().getMCServer().e.a(null,(double)target.getX(),(double)target.getY(),(double)target.getZ(), BLAST_RADIUS*1.0F);
 		
 			player.sendMessage(TEXT_COLOR + STR_CAST);
 			sendMessageToPlayersInRange(player, STR_CAST_OTHERS);
 		
 			return true;
 		} else {
 			player.sendMessage(TEXT_COLOR + STR_NO_REAGENTS);
 		
 			return false;
 		}
 	}
 	
 	public String getCostDesc(String s) {
 		return REDSTONE_COST + " " + REDSTONE_NAME + (OTHER_COST_AMT>0?", "+OTHER_COST_AMT+" "+OTHER_COST_NAME:"");
 	}
 		
 
 }
