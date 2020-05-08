 import java.util.ArrayList;
 import java.util.HashMap;
 
 
 public class TpDoorListener extends PluginListener{
 	TpDoorFiler filer = new TpDoorFiler();
 	
 	HashMap<String, ArrayList<String>> selection = new HashMap<String, ArrayList<String>>();
 	HashMap<String, ArrayList<String>> groupManager = new HashMap<String, ArrayList<String>>();
 	
 	//TODO order = x,y,z,world,dimension(,rotation,pitch:allowed,-disallowed) 
 	
 	public boolean onCommand(Player player, String[] split) {
 		if ((split[0].equalsIgnoreCase("/tpdoor")) && (player.canUseCommand("/tpdoor"))) {
 			
 			if (split.length == 1){
 				player.sendMessage("a-b-a-b-a-b-f[bTpDoorf]a-b-a-b-a-b-");
 				player.sendMessage("f/tpdoor select - bTo start selecting your tpDoor");
 				player.sendMessage("f/tpdoor save - bTo save your tpDoor");
 				player.sendMessage("f/tpdoor delete - bTo delete your tpDoor");
 				player.sendMessage("f/tpdoor reset - bTo reset your selecting");
 				player.sendMessage("f/tpdoor undo - bTo undo your last selected tpdoor");
 				player.sendMessage("f/tpdoor deny <group> - bTo deny a group using the selected tpDoor");
 			}
 			
 			if (split.length == 2){
 				if (split[1].equalsIgnoreCase("select")){
 					selection.put(player.getName(), new ArrayList<String>());
 					groupManager.put(player.getName(), new ArrayList<String>());
 					player.sendMessage("f[bTpDoorsf]b - You started selecting.");
 				}
 			
 				if (split[1].equalsIgnoreCase("save")){
 					if (selection.containsKey(player.getName())){
 						if (!selection.get(player.getName()).isEmpty()){
 							ArrayList<String> selected = selection.get(player.getName());
 							filer.saveTpDoor(selection.get(player.getName()), groupManager.get(player.getName()), player.getLocation());
 							this.removeBlocks(player);
 							selection.remove(player.getName());
 							player.giveItem(20, selected.size());
 							player.sendMessage("f[bTpDoorsf]b - TpDoor has been saved.");
 						} else {
 							player.sendMessage("f[bTpDoorsf]c - You did not make a selection yet!");
 						}
 					} else {
 						player.sendMessage("f[bTpDoorsf]c - You are not selecting a TpDoor!");
 					}
 				}
 				
 				if (split[1].equalsIgnoreCase("delete")){
 					if (selection.containsKey(player.getName())){
 						if (!selection.get(player.getName()).isEmpty()){
 							ArrayList<String> selected = selection.get(player.getName());
 							filer.deleteTpDoor(selection.get(player.getName()));
 							this.removeBlocks(player);
 							selection.remove(player.getName());
 							player.giveItem(20, selected.size());
 							player.sendMessage("f[bTpDoorsf]b - You deleted a TpDoor.");
 						} else {
 							player.sendMessage("f[bTpDoorsf]c - You did not make a selection yet!");
 						}
 					} else {
 						player.sendMessage("f[bTpDoorsf]c - You are not selecting a TpDoor!");
 					}
 				}
 				
 				if (split[1].equalsIgnoreCase("reset")){
 					if (selection.containsKey(player.getName())){
 						if (!selection.get(player.getName()).isEmpty()){
 							ArrayList<String> selected = selection.get(player.getName());
 							this.removeBlocks(player);
 							selection.remove(player.getName());
 							selection.put(player.getName(), new ArrayList<String>());
 							player.giveItem(20, selected.size());
 							player.sendMessage("f[bTpDoorsf]b - You restarted setting a TpDoor.");
 						} else {
 							player.sendMessage("f[bTpDoorsf]c - You did not make a selection yet!");
 						}
 					} else {
 						player.sendMessage("f[bTpDoorsf]c - You are not selecting a TpDoor!");
 					}
 				}
 				
 				if (split[1].equalsIgnoreCase("undo")){
 					if (selection.containsKey(player.getName())){
 						if (!selection.get(player.getName()).isEmpty()){
 							ArrayList<String> selected = selection.get(player.getName());
 							this.removeBlocks(selected.get(selected.size()-1));
 							selected.remove(selected.size()-1);
 							selection.get(player.getName()).remove(selected.size()-1);
 							player.giveItem(20, 1);
 							player.sendMessage("f[bTpDoorsf]b - Last selected block undone.");
 						} else {
 							player.sendMessage("f[bTpDoorsf]c - You did not make a selection yet!");
 						}
 					} else {
 						player.sendMessage("f[bTpDoorsf]c - You are not selecting a TpDoor!");
 					}
 				}
 		}
 			
 			if (split.length == 3){
 				if (split[1].equalsIgnoreCase("deny")){
 					if (selection.containsKey(player.getName())){
 						groupManager.get(player.getName()).add(split[2]);
 						player.sendMessage("f[bTpDoorsf]b - group denied!");
 						}else{
 							player.sendMessage("f[bTpDoorsf]c - You are not selecting a TpDoor!");
 					}
 				}
 			}
 			return true;
 		}
 		return false;
 	}
 	
 	public boolean onBlockPlace(Player player, Block block, Block brc, Item iih) {
 		if (block.getType() == 20 && selection.containsKey(player.getName())){
 			String loc = block.getX()+","+block.getY()+","+block.getZ()+","+block.getWorld().getName()+","+block.getLocation().dimension;
 			selection.get(player.getName()).add(loc);
 			player.sendMessage("f[bTpDoorsf]b - Block added to the selection.");
 		}
 		return false;
 	}
 	
 	public void onPlayerMove(Player player, Location from, Location to) {
 		String loc = etc.floor(to.x)+","+etc.floor(to.y)+","+etc.floor(to.z)+","+to.getWorld().getName()+","+to.dimension;
 		if (filer.isTpDoor(loc)){
 			if (filer.mayPlayerAcess(loc,(player.getGroups().length > 0 ? player.getGroups()[0] : etc.getDataSource().getDefaultGroup().Name))){
 				String[] location = filer.getLocation(loc).split(",");
 				if (!etc.getServer().isWorldLoaded(location[3])){
 				World world = etc.getServer().getWorld(location[5])[Integer.parseInt(location[6])];
 				Location toLoc = new Location(world,Double.parseDouble(location[0]),Double.parseDouble(location[1]),Double.parseDouble(location[2]),Float.parseFloat(location[3]),Float.parseFloat(location[4]));
 				player.teleportTo(toLoc);
 				player.sendMessage("f[bTpDoorsf]b - You have been teleported.");
 				}
 			}
 		}
 	}
 	
 	public void removeBlocks(Player player){
 		ArrayList<String> blocks = selection.get(player.getName());
 		for (String block : blocks){
 			String[] locationData = block.split(",");
 			World world = etc.getServer().getWorld(locationData[3])[Integer.parseInt(locationData[4])];
 			world.setBlockAt(0,Integer.parseInt(locationData[0]), Integer.parseInt(locationData[1]), Integer.parseInt(locationData[2]));
 		}
 	}
 	
     public void removeBlocks(String location){
 		String[] locationData = location.split(",");
 		World world = etc.getServer().getWorld(locationData[3])[Integer.parseInt(locationData[4])];
 		world.setBlockAt(0,Integer.parseInt(locationData[0]), Integer.parseInt(locationData[1]), Integer.parseInt(locationData[2]));
 	}
 
 }
