 package gt.plugin.helloeditor;
 
 import static org.bukkit.ChatColor.*;
 import gt.general.trigger.AttachableRedstoneTrigger;
 import gt.general.trigger.PressurePlateRedstoneTrigger;
 import gt.general.trigger.TriggerContext;
 import gt.general.trigger.response.BlockDisappearResponse;
 import gt.general.trigger.response.DoorResponse;
 import gt.general.trigger.response.RedstoneTorchResponse;
 import gt.general.trigger.response.SignResponse;
 
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockPlaceEvent;
 
 
 public class BuildManager implements Listener {	
 	
 	private final PlayerManager playerManager;
 	
 	/**
 	 * @param playerManager manages the player states
 	 */
 	public BuildManager(final PlayerManager playerManager) {
 		this.playerManager = playerManager;
 	}
 	
 	/**
 	 * register Triggers and Responses to playerTriggerContexts
 	 * @param event player places a block
 	 */
 	@EventHandler
 	public void onBlockPlaced(final BlockPlaceEvent event) {
 		String name = event.getPlayer().getName();
 		
 		switch(playerManager.getState(name)) {
 			case TRIGGER: 
 				addTrigger(event);
 				break;
 			case RESPONSE:
 				addResponse(event);
 				break;
 			default:
 		}
 	}
 	
 	/**
 	 * adds a new trigger to the currently active context
 	 * 
 	 * @param event the trigger(event) for the trigger to be placed
 	 */
 	private void addTrigger(final BlockPlaceEvent event) {
 		Player player = event.getPlayer();
 		Block block = event.getBlockPlaced();
 		TriggerContext activeContext = playerManager.getContext(player.getName());
 		
 		switch(block.getType()) {
 			case WOOD_PLATE:
 			case STONE_PLATE:
 				activeContext.addTrigger(new PressurePlateRedstoneTrigger(block));
 				break;
				
 			case LEVER:
 			case STONE_BUTTON:
 				activeContext.addTrigger(new AttachableRedstoneTrigger(block));
 				break;
				
 			default: 
 				// fail feedback
 				player.sendMessage(RED + "This Block can't be used as Trigger.");
 				System.out.println("This Block can't be used as Trigger. --> " + block.getType());
 				return;
 		}
 		// success feedback
 		player.sendMessage(GREEN + "Trigger has been added");
 	}
 
 	/**
 	 * adds a new response to the currently active context
 	 * 
 	 * @param event the trigger(event) for the response to be placed
 	 */
 	private void addResponse(final BlockPlaceEvent event) {
 		Player player = event.getPlayer();
 		Block block = event.getBlockPlaced();
 		TriggerContext activeContext = playerManager.getContext(player.getName());
 		
 		switch(block.getType()) {
 			case WOOD_DOOR:
 			case WOODEN_DOOR:
 			case IRON_DOOR:
 			case IRON_DOOR_BLOCK: 
 				activeContext.addResponse(new DoorResponse(block));
 				break;
 			case DIAMOND_BLOCK:
 				activeContext.addResponse(new BlockDisappearResponse(block));
 				break;
 			case REDSTONE_LAMP_OFF:
 			case REDSTONE_TORCH_ON:
 				activeContext.addResponse(new RedstoneTorchResponse(block));
 				break;
 			case WALL_SIGN:
 			case SIGN_POST:
 				activeContext.addResponse(new SignResponse(block));
 				break;
 			default:
 				// fail feedback
 				player.sendMessage(RED + "This Block can't be used as Response.");
 				System.out.println("This Block can't be used as Response. --> " + block.getType());
 				return;
 		}
 		// success message
 		player.sendMessage(GREEN + "Response has been added");
 	}
 }
