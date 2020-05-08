 package net.worldoftomorrow.noitem.permissions;
 
 import java.util.Map.Entry;
 
 import net.worldoftomorrow.noitem.Config;
 import net.worldoftomorrow.noitem.util.Dbg;
 import net.worldoftomorrow.noitem.util.Messenger;
 
 import org.bukkit.block.Block;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.permissions.Permission;
 
 public class PermMan {
 	
 	private static final char PS = '.';
 	
 	// Permission with no variable do not apply to PermsAsWhitelist and
 	// therefore are not checked against it
 	public boolean has(Player p, String perm) {
 		return p.hasPermission(perm);
 	}
 	
 	public boolean has(Player p, String perm, ItemStack item) {
 		boolean has = check(p, construct(perm, item));
 		// Is PermsAsWhiteList is true, return the opposite value.
 		return Config.getBoolean("PermsAsWhiteList") ? !has : has;
 	}
 	
 	public boolean has(Player p, String perm, Block b) {
 		boolean has = check(p, construct(perm, b));
 		return Config.getBoolean("PermsAsWhiteList") ? !has : has;
 	}
 	
 	public boolean has(Player p, String perm, Entity e) {
 		boolean has = check(p, construct(perm, e));
 		return Config.getBoolean("PermsAsWhiteList") ? !has : has;
 	}
 	
 	// Special method just for brewing
 	public boolean has(Player p, short data, ItemStack ingredient) {
 		boolean has = check(p, construct(Perm.BREW,  new PotionRecipe(data, ingredient)));
 		return Config.getBoolean("PermsAsWhiteList") ? !has : has;
 	}
 	
 	// Checks if a permission is explicitly set to false
 	private boolean permSetFalse(Player p, Permission perm) {
 		return p.isPermissionSet(perm.getName()) && !p.hasPermission(perm.getName()); 
 	}
 	
 	/**
 	 * Check to see if the permission is had. 
 	 * Does not adjust for PermsAsWhiteList.
 	 * @param p
 	 * @param perm
 	 * @return
 	 */
 	private boolean check(Player p, Permission[] perm) {
 		if(p.isOp()) return false;
 		if(permSetFalse(p, perm[0]) || permSetFalse(p, perm[1])) {
 			return false;
		} else if(p.hasPermission(perm[0]) || p.hasPermission(perm[1]) || p.hasPermission(perm[2])) {
 			return true;
 		} else {
 			return checkVault(p, perm);
 		}
 	}
 	
 	private boolean checkVault(Player p, Permission[] perms) {
 		// If vault is loaded
 		if(VaultHook.loaded) {
 			// Check each permission
 			for(Permission perm : perms) {
 				// If they have the main permission, return true
 				if(VaultHook.permission.has(p, perm.getName()))
 					return true;
 				// If they have any of the children permissions, return true
 				for(Entry<String, Boolean> entry : perm.getChildren().entrySet()) {
 					if(VaultHook.permission.has(p, entry.getKey())) {
 						return true;
 					}
 				}
 			}
 			// They do not have any of the permissions, return false.
 			return false;
 		} else {
 			// There is no vault loaded, return false.
 			return false;
 		}
 	}
 	
 	// Constructs an array of permissions with parents added
 	private Permission[] construct(String perm, Object o) {
 		if(perm.equals(Perm.ADMIN)
 				|| perm.equals(Perm.ALLITEMS)
 				|| perm.equals(Perm.CMD_CHECK)
 				|| perm.equals(Perm.CMD_RELOAD)
 				|| perm.equals(Perm.ONDEATH)) {
 			throw new UnsupportedOperationException("Incorrect checking of permission: " + perm);
 		}
 		int id;
 		String name;
 		short data;
 
 		if(o instanceof ItemStack) {
 			ItemStack stack = (ItemStack) o;
 			id = stack.getTypeId();
 			name = Messenger.getStackName(stack);
 			data = stack.getDurability();
 		} else if (o instanceof Block) {
 			Block b = (Block) o;
 			id = b.getTypeId();
 			name = Messenger.getBlockName(b);
 			data = b.getData();
 		} else if (o instanceof Entity) {
 			Entity e = (Entity) o;
 			name = Messenger.getEntityName(e);
 			id = -1;
 			data = -1;
 		} else if (o instanceof PotionRecipe) {
 			// This is just for brewing recipes
 			PotionRecipe recipe = (PotionRecipe) o;
 			name = Messenger.getStackName(new ItemStack(recipe.getIngId())); // ingredient name
 			// For potion recipes, it should now be noitem.nobrew.ingredient.potiondata
 			id = recipe.getIngId();
 			data = recipe.getData();
 		} else {
 			throw new UnsupportedOperationException("Unknown object type: " + o.getClass().getSimpleName());
 		}
 		Permission standard;
 		if(data != -1 && id != -1) {
 			standard = new Permission(perm + name + PS + data);
 			standard.addParent(perm + id + PS + data, true);
 			if(data == 0) {
 				standard.addParent(perm + id, true);
 				standard.addParent(perm + name, true);
 			}
 		} else {
 			standard = new Permission(perm + name);
 		}
 
 		Permission allData = new Permission(perm + name + PS + "*");
 		allData.addParent(perm + id + PS + "*", true);
 		
 		Permission allFeature = new Permission(perm + "*");
 		
 		Permission[] perms = new Permission[3];
 		perms[0] = standard;
 		perms[1] = allData;
 		perms[2] = allFeature;
 		Dbg.$("CONSTRUCT: " + perms[0].getName());
 		return perms;
 	}	
 }
