 package net.catharos.recipes;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import net.catharos.recipes.crafting.CustomRecipe;
 import net.catharos.recipes.util.TextUtil;
 
 import org.bukkit.GameMode;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.HumanEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.inventory.CraftItemEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.Recipe;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.spaceemotion.updater.Updater;
 
 public class cRecipes extends JavaPlugin implements Listener {
 	private cRecipes instance;
 
 	protected Map<Integer, Map<Byte, CustomRecipe>> recipes;
 	protected RecipeLoader loader;
 
 	public void onEnable() {
 		instance = this;
 
 		getConfig().options().copyDefaults( true );
 		this.saveConfig();
 
 		if (getConfig().getBoolean( "check-updates", true )) new Updater( this, true );
 
 		// Run async, to reduce lagg
 		getServer().getScheduler().runTaskAsynchronously( this, new Runnable() {
 			public void run() {
 				try {
 					Metrics metrics = new Metrics( instance );
 					metrics.start();
 				} catch (IOException e) {
 					getLogger().info( "cRecipes failed plugin metrics" );
 				}
 			}
 		} );
 
 		getDataFolder().mkdirs();
 
 		recipes = new HashMap<Integer, Map<Byte, CustomRecipe>>();
 		loader = new RecipeLoader( this );
 
 		getServer().getPluginManager().registerEvents( this, this );
 	}
 
 	public void onDisable() {
 		getServer().resetRecipes();
 	}
 
 	/**
 	 * Returns a list of stored {@link CustomRecipe}s
 	 * 
 	 * @return A list of {@link CustomRecipe}s
 	 */
 	public Map<Integer, Map<Byte, CustomRecipe>> getRecipes() {
 		return recipes;
 	}
 
 	public void addRecipe( int mat, byte data, CustomRecipe recipe ) {
 		Map<Byte, CustomRecipe> map = getRecipes().get( mat );
 
 		if (map == null) {
 			map = new HashMap<Byte, CustomRecipe>();
 			recipes.put( mat, map );
 		}
 
 		map.put( data, recipe );
 		getServer().addRecipe( recipe.getRecipe() );
 	}
 
 	public CustomRecipe getRecipe( int mat, short data ) {
 		Map<Byte, CustomRecipe> stored = getRecipes().get( mat );
 
 		if (stored != null)
 			return stored.get( (byte) data );
 		else
 			return null;
 	}
 
 	@EventHandler
 	public void e( BlockBreakEvent event ) {
 		if (event.isCancelled() || event.getPlayer().getGameMode() == GameMode.CREATIVE) return;
 
 		Block block = event.getBlock();
 		CustomRecipe cr = this.getRecipe( block.getTypeId(), block.getData() );
 
 		if (cr != null) {
 			for (ItemStack drop : cr.getDrops())
 				block.getWorld().dropItemNaturally( block.getLocation(), drop );
 
 			block.setType( Material.AIR );
 			event.setCancelled( true );
 		}
 	}
 
 	@EventHandler
 	public void c( CraftItemEvent event ) {
		if (event.isCancelled()) return;
 
 		Recipe recipe = event.getRecipe();
 		ItemStack result = recipe.getResult();
 
 		CustomRecipe cr = getRecipe( result.getTypeId(), result.getData().getData() );
 
 		if (cr != null) {
 			event.setCurrentItem( cr.getItem() );
 
 			String perm = cr.getPermission();
 			HumanEntity entity = event.getWhoClicked();
 
 			if (!perm.isEmpty() && !entity.isOp() && !entity.hasPermission( perm )) {
 				event.setCancelled( true );
 
 				String msg = cr.getNoPermissionMessage();
 				if (msg.isEmpty()) msg = getConfig().getString( "permissions.message" );
 
 				if (msg != null && !msg.isEmpty() && entity instanceof Player) ((Player) entity).sendMessage( TextUtil.parseColors( msg ) );
 			}
 		}
 	}
 }
