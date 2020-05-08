 package gt.general.logic.trigger;
 
 import gt.general.PortableItem;
 import gt.general.character.Hero;
 import gt.general.character.HeroManager;
 import gt.general.logic.persistence.PersistenceMap;
 import gt.general.logic.persistence.exceptions.PersistenceException;
 import gt.general.world.BlockObserver;
 import gt.general.world.ObservableCustomBlock;
 import gt.general.world.ObservableCustomBlock.BlockEvent;
 import gt.lastgnome.DispenserItem;
 import gt.lastgnome.Key;
 import gt.plugin.meta.CustomBlockType;
 
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 
 /**
  * @author roman
  */
 public class KeyItemTrigger extends ItemTrigger implements BlockObserver{
 	
 	public static final String KEY_COLOR = "color";
 	
 	private DispenserItem keyColor;
 
 	/**
 	 * @param block THE block of the trigger
 	 */
 	public KeyItemTrigger(final Block block, final DispenserItem color) {
 	    super("key_trigger", block, UnlockItemType.KEY);
 		
 	    setCustomBlockType(color);
 	    
 	    registerWithSubject();
 		getCustomType().place(block);
 	}
 	
 	private void setCustomBlockType(DispenserItem color) {
 	    
 	    switch(color) {
 	    case BLUE_KEY:
 	    	setCustomType(CustomBlockType.BLUE_LOCK);
 	    	break;
 	    case RED_KEY:
 	    	setCustomType(CustomBlockType.RED_LOCK);
 	    	break;
 	    case GREEN_KEY:
 	    	setCustomType(CustomBlockType.GREEN_LOCK);
 	    	break;
 	    case YELLOW_KEY:
 	    	setCustomType(CustomBlockType.YELLOW_LOCK);
 	    	break;
 	    default:
 	    	break;
 	    }
 	}
 
 	/**
 	 * registers this trigger
 	 */
 	public void registerWithSubject() {
 		ObservableCustomBlock triggerBlock = getCustomType().getCustomBlock();
 		triggerBlock.addObserver(this, getBlock().getWorld());
 	}
 	
 	/**
 	 * unregisters this trigger
 	 */
 	public void unregisterFromSubject() {
 		ObservableCustomBlock triggerBlock = getCustomType().getCustomBlock();
 		triggerBlock.removeObserver(this, getBlock().getWorld());
 	}
 	
 	/** to be used in persistence */
 	public KeyItemTrigger() {}
 
     @Override
     public void setup(final PersistenceMap values, final World world)
             throws PersistenceException {
     	super.setup(values, world);
         
     	keyColor = DispenserItem.valueOf((String) values.get(KEY_COLOR));
     	setCustomBlockType(keyColor);
     	
     	registerWithSubject();
         getCustomType().place(getBlock());
     }
 
 	@Override
 	protected void triggered(final BlockEvent event) {
 		Hero hero = HeroManager.getHero(event.getPlayer());
 		event.getBlock().setType(Material.AIR);
 		
 		hero.removeActiveItem();
 	}
 	
 	@Override
 	public PersistenceMap dump() {
 		PersistenceMap map = super.dump();
 		
 		map.put(KEY_COLOR, keyColor.toString());
 		
 		return map;
 	}
 
 	@Override
 	protected boolean rightItemForTrigger(final Hero hero) {
 		PortableItem item = hero.getActiveItem();
 		return item.getType() == getType() && keyColor == ((Key) item).getKeyColor();
 	}
 
 
 }
