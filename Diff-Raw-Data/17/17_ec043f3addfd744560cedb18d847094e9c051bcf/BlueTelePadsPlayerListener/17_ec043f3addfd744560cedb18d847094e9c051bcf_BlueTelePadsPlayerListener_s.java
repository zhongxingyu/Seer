 package Ne0nx3r0.BlueTelePads;
 
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.player.PlayerInteractEvent;
 import java.util.Map;
 import java.util.HashMap;
 import org.bukkit.Material;
 import org.bukkit.event.block.Action;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Sign;
 import org.bukkit.Location;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.World;
 
 public class BlueTelePadsPlayerListener extends PlayerListener {
     private final BlueTelePads plugin;
     private static Map<String, Block> mLapisLinks  = new HashMap<String, Block>();
     private static Map<String, Long> mTimeouts = new HashMap<String, Long>();
     
     public BlueTelePadsPlayerListener(BlueTelePads instance){
         this.plugin = instance;
     }
 
     public static boolean isTelePadLapis(Block lapisBlock){
         if(lapisBlock.getType() == Material.LAPIS_BLOCK
         && lapisBlock.getFace(BlockFace.EAST).getType() == Material.DOUBLE_STEP
         && lapisBlock.getFace(BlockFace.WEST).getType() == Material.DOUBLE_STEP
         && lapisBlock.getFace(BlockFace.NORTH).getType() == Material.DOUBLE_STEP
         && lapisBlock.getFace(BlockFace.SOUTH).getType() == Material.DOUBLE_STEP
         && (lapisBlock.getFace(BlockFace.DOWN).getType() == Material.SIGN_POST
                 || lapisBlock.getFace(BlockFace.DOWN).getType() == Material.WALL_SIGN)
         && lapisBlock.getFace(BlockFace.UP).getType() == Material.STONE_PLATE){
             return true;
         }
         return false;
     }
 
     private String toHex(int number){
         return Integer.toHexString(number + 32000);
     }
 
     private int toInt(String hex){
         return Integer.parseInt(hex, 16) - 32000;
     }
 
     private Block getTelepadLapisReceiver(Block bSenderLapis){
         Block bSenderSign = bSenderLapis.getFace(BlockFace.DOWN);
 
         if(bSenderSign.getType() == Material.WALL_SIGN || bSenderSign.getType() == Material.SIGN_POST){
             Sign sbSenderSign = (Sign) bSenderSign.getState();
 
             String sHexLocation = sbSenderSign.getLine(2);
 
             String sWorld = sbSenderSign.getLine(1);
             String[] sXYZ = sHexLocation.split(":");
 
             World world = plugin.getServer().getWorld(sWorld);
 
             if(world == null){
                 return null;
             }
             
             Block bReceiverLapis = world.getBlockAt(toInt(sXYZ[0]),toInt(sXYZ[1]),toInt(sXYZ[2]));
             
             if(isTelePadLapis(bReceiverLapis)){
                 return bReceiverLapis;
             }
         }
         return null;
     }
     
     //currently assumes you checked both blocks with isTelePadLapis
     private void linkTelepadLapisReceivers(Block bLapis1,Block bLapis2){
         Sign sbLapis1 = (Sign) bLapis1.getFace(BlockFace.DOWN).getState();
         Sign sbLapis2 = (Sign) bLapis2.getFace(BlockFace.DOWN).getState();
 
         sbLapis1.setLine(1,sbLapis2.getWorld().getName());
         sbLapis2.setLine(1,sbLapis1.getWorld().getName());
 
         Location lLapis1 = bLapis1.getLocation();
         Location lLapis2 = bLapis2.getLocation();
 
         sbLapis1.setLine(2,toHex(lLapis2.getBlockX())+":"+toHex(lLapis2.getBlockY())+":"+toHex(lLapis2.getBlockZ()));
         sbLapis2.setLine(2,toHex(lLapis1.getBlockX())+":"+toHex(lLapis1.getBlockY())+":"+toHex(lLapis1.getBlockZ()));
 
         sbLapis1.update(true);
         sbLapis2.update(true);
     }
 
     private static int getDistance(Location loc1,Location loc2){
         return (int) Math.sqrt(Math.pow(loc2.getBlockX()-loc1.getBlockX(),2)+Math.pow(loc2.getBlockY()-loc1.getBlockY(),2)+Math.pow(loc2.getBlockZ()-loc1.getBlockZ(),2));
     }
 
     private boolean TelePadsWithinDistance(Block block1,Block block2){
         if(plugin.MAX_DISTANCE == 0){
             return true;
         }
         if(getDistance(block1.getLocation(),block2.getLocation()) < plugin.MAX_DISTANCE){
             return true;
         }
         return false;
     }
 
     @Override
     public void onPlayerInteract(PlayerInteractEvent event){
         if(event.getItem() != null 
         && event.getItem().getType() == Material.REDSTONE
         && isTelePadLapis(event.getClickedBlock().getFace(BlockFace.UP))){
             if((plugin.USE_PERMISSIONS && !plugin.Permissions.has(event.getPlayer(),"BlueTelePads.Create"))
             || (plugin.OP_ONLY && !event.getPlayer().isOp())){
                 event.getPlayer().sendMessage(ChatColor.RED+"You do not have permission to create a telepad!");
                 return;
             }
 
             if(!mLapisLinks.containsKey(event.getPlayer().getName())){
                 mLapisLinks.put(event.getPlayer().getName(),event.getClickedBlock().getFace(BlockFace.UP));
 
                 event.getPlayer().sendMessage(ChatColor.DARK_AQUA + "Telepad location stored!");
             }else{
                 Block bFirstLapis = mLapisLinks.get(event.getPlayer().getName());
                 
                 if(isTelePadLapis(bFirstLapis)){
                     if(!TelePadsWithinDistance(bFirstLapis,event.getClickedBlock())){
                         event.getPlayer().sendMessage(ChatColor.RED+"Error: "+ChatColor.DARK_AQUA + " Telepads are too far apart! (Distance:"+getDistance(bFirstLapis.getLocation(),event.getClickedBlock().getLocation())+",MaxAllowed:"+plugin.MAX_DISTANCE+")");
                         return;
                     }
 
                     mLapisLinks.remove(event.getPlayer().getName());
                    
                     linkTelepadLapisReceivers(bFirstLapis,event.getClickedBlock().getFace(BlockFace.UP));
 
                     event.getPlayer().sendMessage(ChatColor.DARK_AQUA + "Telepad location transferred!");
                 }
             }
         }
         else if(event.getAction() == Action.PHYSICAL
         && event.getClickedBlock().getType() == Material.STONE_PLATE
        && event.getClickedBlock().getFace(BlockFace.DOWN).getType() == Material.LAPIS_BLOCK
         && isTelePadLapis(event.getClickedBlock().getFace(BlockFace.DOWN))
         && (!mTimeouts.containsKey(event.getPlayer().getName()) || mTimeouts.get(event.getPlayer().getName()) < System.currentTimeMillis())){
             Block bLapis = event.getClickedBlock().getFace(BlockFace.DOWN);
             Block bReceiverLapis = getTelepadLapisReceiver(bLapis);
 
             if(bReceiverLapis != null){
                 if(plugin.USE_PERMISSIONS && !plugin.Permissions.has(event.getPlayer(),"BlueTelePads.Use")){
                     event.getPlayer().sendMessage(ChatColor.RED+"You do not have permission to use telepads");
                     return;
                 }
 
                 if(!TelePadsWithinDistance(bLapis,bReceiverLapis)){
                     event.getPlayer().sendMessage(ChatColor.RED+"Error: "+ChatColor.DARK_AQUA + " Telepads are too far apart! (Distance:"+getDistance(bLapis.getLocation(),bReceiverLapis.getLocation())+",MaxAllowed:"+plugin.MAX_DISTANCE+")");
                     return;
                 }
 
                 Sign sbReceiverSign = (Sign) bReceiverLapis.getFace(BlockFace.DOWN).getState();
 
                 event.getPlayer().sendMessage(ChatColor.DARK_AQUA + "Telepad: Preparing to send you to "+sbReceiverSign.getLine(3)+", stand still!");
 
                 plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin,new BluePadTeleport(plugin,event.getPlayer(),event.getPlayer().getLocation(),bLapis,bReceiverLapis),60);
             }
         }
     }
 
     private static class BluePadTeleport implements Runnable{
         private final BlueTelePads plugin;
         private final Player player;
         private final Location player_location;
         private final Block receiver;
         private final Block sender;
 
         BluePadTeleport(BlueTelePads plugin,Player player,Location player_location,Block senderLapis,Block receiverLapis){
             this.plugin = plugin;
             this.player = player;
             this.player_location = player_location;
             this.sender = senderLapis;
             this.receiver = receiverLapis;
         }
 
         public void run(){
             if(getDistance(player_location,player.getLocation()) > 1){
                 player.sendMessage(ChatColor.DARK_AQUA + "Telepad: You moved, cancelling teleport!");
                 return;
             }
             if(isTelePadLapis(sender) && isTelePadLapis(receiver)){
                 player.sendMessage(ChatColor.DARK_AQUA + "Telepad: Here goes nothing!");
 
                 Location lSendTo = receiver.getFace(BlockFace.UP,2).getFace(BlockFace.NORTH).getLocation();
                 lSendTo.setX(lSendTo.getX()+0.5);
                 lSendTo.setZ(lSendTo.getZ()+0.5);
 
                 player.teleport(lSendTo);
 
                 mTimeouts.put(player.getName(),System.currentTimeMillis()+5000);
             }else{
                 player.sendMessage(ChatColor.DARK_AQUA + "Telepad: Something went wrong! Just be grateful you didn't get split in half!");
             }
         }
     }
 }
