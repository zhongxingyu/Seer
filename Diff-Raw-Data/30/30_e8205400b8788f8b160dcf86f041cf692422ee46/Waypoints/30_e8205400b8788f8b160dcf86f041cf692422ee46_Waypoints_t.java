 /**
  * NOTE:
  * Latitude: X
  * Longitude: Z
  * Altitude: Y
  */
 
 /**
  *
  * @author jonathan
  */
 public class Waypoints extends Mod {
 	protected static HomeList homelist;
 	protected static GateList gatelist;
 	
 	protected int[] wayPointStart = new int[] {0, 1, 1};
 	protected BlockType[][][] wayPointPattern = new BlockType[][][] {
 		{
 			{BlockType.OBSIDIAN, BlockType.IRON_BLOCK, BlockType.OBSIDIAN},
 			{BlockType.IRON_BLOCK, BlockType.GOLD_BLOCK, BlockType.IRON_BLOCK},
 			{BlockType.OBSIDIAN, BlockType.IRON_BLOCK, BlockType.OBSIDIAN}
 		},
 		{
 			{BlockType.AIR, BlockType.AIR, BlockType.AIR},
 			{BlockType.IRON_BLOCK, BlockType.AIR, BlockType.IRON_BLOCK},
 			{BlockType.AIR, BlockType.AIR, BlockType.AIR}
 		},
 		{
 			{BlockType.AIR, BlockType.AIR, BlockType.AIR},
 			{BlockType.IRON_BLOCK, BlockType.AIR, BlockType.IRON_BLOCK},
 			{BlockType.AIR, BlockType.AIR, BlockType.AIR}
 		},
 		{
 			{BlockType.AIR, BlockType.AIR, BlockType.AIR},
 			{BlockType.IRON_BLOCK, BlockType.DIAMOND_BLOCK, BlockType.IRON_BLOCK},
 			{BlockType.AIR, BlockType.AIR, BlockType.AIR}
 		}
 	};
 	
 	public static Player player;
 
 	@Override
 	public void activate() {
 		Waypoints.homelist = new HomeList();
 		Waypoints.homelist.load();
 		Waypoints.gatelist = new GateList();
 		Waypoints.gatelist.load();
 	}
 
 	protected boolean parseCommand(Player player, String[] tokens) {
 		Waypoints.player = player;
 		String command = tokens[0].substring(1);
 
 		if(command.equalsIgnoreCase("home")) {
 			Waypoints.homelist.sendPlayerHome(player);
 			return true;
 		}
 		else if( command.equalsIgnoreCase("sethome") ) {
 			Waypoints.homelist.setUserHomePoint(player);
 			return true;
 		}
 		else if( command.equalsIgnoreCase("unsethome") ) {
 			Waypoints.homelist.unsetUserHomePoint(player);
 			return true;
 		}
 		else if( command.equalsIgnoreCase("setgate") ) {
 			if( !player.isAdmin() ) return false;
 			if( 2 <= tokens.length ) {
 				Waypoints.gatelist.setGate(tokens[1], player);
 			}
 			else {
 				player.sendChat("ERROR: Must supply gate name", Color.Red);
 			}
 			return true;
 		}
 		else if( command.equalsIgnoreCase("gateto") ) {
 			if( !player.isAdmin() ) return false;
 			if( 2 <= tokens.length ) {
 				Waypoints.gatelist.sendPlayerToGate(player, tokens[1]);
 			}
 			else {
 				player.sendChat("ERROR: Must supply gate name.",Color.Red);
 			}
 			return true;
 		}
 		else if( command.equalsIgnoreCase("gates") ) {
 			Waypoints.gatelist.listGates(player);
 			return true;
 		}
 
 		return false;
 	}
 
	public static void loadChunkAtLocation(Location loc) {
		if( !World.isChunkLoaded(loc) ) {
			World.loadChunk(loc);
		}
	}


 	@Override
 	public String toString() {
 		return "!home, !sethome, !unsethome";
 	}
 
 	@Override
 	public boolean onPlayerChat(Player player, String chat) {
 		String[] tokens = chat.split(" ");
 		return this.parseCommand(player, tokens);
 	}
 	
 	@Override
 	public boolean onPlayerCommand(Player player, String[] command) {
 		return this.parseCommand(player, command);
 	}	
 }
