 package net.catharos.recipes.crafting;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.catharos.recipes.util.TextUtil;
 
 import org.bukkit.ChatColor;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.Recipe;
 import org.bukkit.inventory.meta.ItemMeta;
 
 public abstract class CustomRecipe {
 	protected String name;
 	protected String permission;
 	protected String noPermMsg;
 
 	protected final ItemStack item;
 
 	protected List<ItemStack> drops;
	protected List<String> details;
 
 	public CustomRecipe( String name, ItemStack item ) {
 		this.name = name;
 		this.item = item;
 
 		this.permission = "";
 		this.noPermMsg = "";
 
 		this.drops = new ArrayList<ItemStack>();
 	}
 
 	/* ---------------- Public methods ---------------- */
 
 	/**
 	 * Returns the name of the item result
 	 * 
 	 * @return The name
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * Returns the item result of this recipe
 	 * 
 	 * @return The {@link ItemStack} representation
 	 */
 	public ItemStack getItem() {
 		return item;
 	}
 
 	/**
 	 * Returns the permission string needed to craft this item. If no permission
 	 * was set an empty string is being returned.
 	 * 
 	 * @return The Permission String
 	 */
 	public String getPermission() {
 		return permission;
 	}
 
 	/**
 	 * Sets the permission string needed to craft this item. If no permission is
 	 * needed, the string can be empty.
 	 * 
 	 * @param permission The permission string to set
 	 */
 	public void setPermission( String permission ) {
 		this.permission = permission;
 	}
 
 	/**
 	 * Returns the message showed when the player lacks the permission to craft
 	 * the item
 	 * 
 	 * @return The message to show
 	 */
 	public String getNoPermissionMessage() {
 		return noPermMsg;
 	}
 
 	/**
 	 * Sets the message showed when the player lacks the permission to craft the
 	 * item
 	 * 
 	 * @param msg The message to show
 	 */
 	public void setNoPermissionMessage( String msg ) {
 		this.noPermMsg = msg;
 	}
 
 	/**
 	 * Returns a list of item drops of this block
 	 * 
 	 * @return A {@link List} of item drops
 	 */
 	public List<ItemStack> getDrops() {
 		return drops;
 	}
 
 	/**
 	 * Sets the drops for this item block
 	 * 
 	 * @param drops The {@link List} of drops
 	 */
 	public void setDrops( List<ItemStack> drops ) {
 		this.drops = drops;
 	}
 
 	/**
	 * Sets the item details for the result of this recipe
	 * 
	 * @param details A {@link List} of item details
	 */
	public void setDetails( List<String> details ) {
		ItemMeta meta = getItem().getItemMeta();
		meta.setLore( details );
		getItem().setItemMeta( meta );
	}

	/**
 	 * Returns the {@link Recipe} for this block item
 	 * 
 	 * @return The crafting {@link Recipe}
 	 */
 	public abstract Recipe getRecipe();
 
 	/* ---------------- Protected / Private Methods ---------------- */
 
 	/**
 	 * Sets the name of this block item
 	 * 
 	 * @param name The name to set
 	 */
 	protected void setName( String name ) {
 		ItemMeta meta = getItem().getItemMeta();
 		meta.setDisplayName( ChatColor.RESET + TextUtil.parseColors( name ) );
 		getItem().setItemMeta( meta );
 	}
 }
