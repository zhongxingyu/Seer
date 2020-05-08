 package aor.SimplePlugin;
 
 import java.util.HashSet;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.*;
 import org.bukkit.block.Block;
 import org.bukkit.Material;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.Location;
 import org.bukkit.World;
 
 /* Example Template
  * By Adamki11s
  * HUGE Plugin Tutorial
  */
 public class SPPlayerListener extends PlayerListener {
     public static SimplePlugin plugin;
     //create a hashmap for the list of the players' currently selected spells
     public static Map<Player, int> currentSpell = new HashMap<Player, int>();
     public SPPlayerListener(SimplePlugin instance) {
         plugin = instance;
     }
     /*public boolean clickedWithHoe(PlayerInteractEvent event) { // The function clickedWithHoe.
         return event.getPlayer().getItemInHand().getType() == Material.GOLD_HOE;
     }*/
     public void onPlayerInteract(PlayerInteractEvent event) {
         /// Blah blah blah
         Player player = event.getPlayer();
         //player.sendMessage("You just right clicked something! Congratz!");
          //if the player has not yet tried to do a spell, set the spell to the default of 0.
     	if(!currentSpell.containsKey(player){
                currentSpell.put(player,0);
          }
          //a switch that goes to the correct spell, based on the one selected
     	//Right clicking with golden hoe
         if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)&&player.getItemInHand().getType() == Material.GOLD_HOE && player.getTargetBlock(null, 256).getType() != Material.AIR) {
                       //starts the switch that changes what spell is executed, depending upon which is set in the hashmap
                       switch(currentSpell.get(player)){
                             //the first spell
                            case 0:
 			      player.getTargetBlock(null, 256).setType(Material.BEDROCK); // Set the material to bedrock 'cause they got a GOLD HOE!
 		               player.getWorld().strikeLightningEffect(event.getPlayer().getTargetBlock(null, 256).getLocation());
                                  break;
                            case 1://a template for a second spell
                                  break;
                            default://in case an invalid spell is selected
                                  currentSpell.put(player,0);
                                  break;
                       }
         }
        //Left clicking with golden hoe
        if ((event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK)&&player.getItemInHand().getType() == Material.GOLD_HOE && player.getTargetBlock(null, 256).getType() != Material.AIR){
        	      player.sendMessage("DaDadadaaaaaaa!");
               if(currentSpell.get(player)>1||currentSpell.get(player)<0){
                    currentSpell.put(player,0);
               }
               else{
                    currentSpell.put(player,currentSpell.get(player)++);
               }
         }
     }
     
 /*  public void onPlayerMove(PlayerMoveEvent event){
         
         Player player = event.getPlayer();
 		Location playerLoc = player.getLocation();
 		
 		player.sendMessage("Your X Coordinates : " + playerLoc.getX());
 		player.sendMessage("Your Y Coordinates : " + playerLoc.getY());
 		player.sendMessage("Your Z Coordinates : " + playerLoc.getZ());
 	}
 	*/
 
 }
