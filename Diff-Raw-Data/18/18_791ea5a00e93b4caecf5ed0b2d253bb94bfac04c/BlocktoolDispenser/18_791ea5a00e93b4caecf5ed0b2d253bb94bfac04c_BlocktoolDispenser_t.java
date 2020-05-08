 package gt.lastgnome;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import gt.general.character.Hero;
 import gt.general.character.HeroManager;
 import gt.general.world.BlockObserver;
 import gt.general.world.ObservableCustomBlock;
 import gt.general.world.ObservableCustomBlock.BlockEvent;
 import gt.general.world.ObservableCustomBlock.BlockEventType;
 import gt.plugin.meta.CustomBlockType;
 
 /**
  * Item-class for GnomeSocket
  */
 public class BlocktoolDispenser implements BlockObserver {
 	/** maximum active blockTools at the same time */
 	private static final int DEFAULT_CONTINGENT = 1;
 	
 	private Block block;
 	private int contingent;
 	
 	/**
 	 * @param block the bukkit block that holds the dispenser
 	 * @param contingent amount of blockTools that the dispenser will give out
 	 */
 	public BlocktoolDispenser(final Block block, final int contingent) {
 		this.block = block;
 		this.contingent = contingent;
 
 		CustomBlockType.BLOCKTOOL_DISPENSER.place(block);
 		
 		registerWithSubject();
 	}
 	
 	/**
 	 * @param block the bukkit block that holds the dispenser
 	 */
 	public BlocktoolDispenser(final Block block) {
 		this(block, DEFAULT_CONTINGENT);
 	}
 	
 	/**
 	 * registers this dispenser
 	 */
 	public void registerWithSubject() {
 		ObservableCustomBlock oBlock = CustomBlockType.BLOCKTOOL_DISPENSER.getCustomBlock();
 		oBlock.addObserver(this, block.getWorld());
 	}
 	
 	/**
 	 * unregisters this dispenser
 	 */
 	public void unregisterFromSubject() {
 		ObservableCustomBlock oBlock = CustomBlockType.BLOCKTOOL_DISPENSER.getCustomBlock();
 		oBlock.removeObserver(this, block.getWorld());
 	}
 	
 	/**
 	 * destroy the dispenser
 	 */
 	public void dispose() {
 		unregisterFromSubject();
 		block.setType(Material.AIR);
 	}
 	
 	/**
 	 * @return block that holds the dispenser
 	 */
 	public Block getBlock() {
 		return block;
 	}
 
 	@Override
 	public void onBlockEvent(final BlockEvent blockEvent) {
 		if(blockEvent.getBlock().equals(block) && blockEvent.getBlockEventType() == BlockEventType.BLOCK_INTERACT) {
 			Player player = blockEvent.getPlayer();
 			Hero hero = HeroManager.getHero(player);
 			
 			if(hero!=null) {
 				if(contingent>0) {
					if(hero.canRecieveItem()) {
						//can receive
						hero.setActiveItem(new BlockTool(this));
						player.sendMessage(ChatColor.GREEN + "You obtained an Obsidian Placing Device.");
						
						contingent--;
					} else {
						// can not receive
						player.sendMessage(ChatColor.YELLOW + "You already carry enough.");
					}
 				} else {
					// no contingent left
 					player.sendMessage(ChatColor.YELLOW + "The Obsidian Placing Device is already in use.");
 				}
 			} else {
				// no hero (editor mode)
 				player.sendMessage(ChatColor.YELLOW + "The device is not available in the current game mode.");
 			}
 		}
 		
 	}
 
 	/**
 	 * @return the maximum contingent
 	 */
 	public int getContingent() {
 		return contingent;
 	}
 
 	/**
 	 * @param contingent the new contingent
 	 */
 	public void setContingent(final int contingent) {
 		this.contingent = contingent;
 	}
 	
 	/**
 	 * increase the contingent by 1
 	 */
 	public void increaseContingent() {
 		this.contingent++;
 	}
 }
