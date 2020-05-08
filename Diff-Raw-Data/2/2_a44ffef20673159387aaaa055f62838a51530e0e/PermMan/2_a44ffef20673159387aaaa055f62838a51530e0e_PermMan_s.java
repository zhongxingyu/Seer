 package net.worldoftomorrow.noitem.permissions;
 
 import net.worldoftomorrow.noitem.Config;
 import net.worldoftomorrow.noitem.util.Messenger;
 
 import org.bukkit.block.Block;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 public class PermMan {
 	
 	private static final char PS = '.';
 	public boolean pawl = Config.getBoolean("PermsAsWhiteList");
 	
 	// Permission with no variable do not apply to PermsAsWhitelist and
 	// therefore are not checked against it
 	public boolean has(Player p, String perm) {
 		return p.hasPermission(perm);
 	}
 	
 	public boolean has(Player p, String perm, ItemStack item) {
 		boolean has = check(p, construct(perm, item));
 		// Is PermsAsWhiteList is true, return the opposite value.
 		return pawl ? !has : has;
 	}
 	
 	public boolean has(Player p, String perm, Block b) {
 		boolean has = check(p, construct(perm, b));
 		return pawl ? !has : has;
 	}
 	
 	public boolean has(Player p, String perm, Entity e) {
 		boolean has = check(p, construct(perm, e));
 		return pawl ? !has : has;
 	}
 	
 	// New special method just for brewing!
 	public boolean has(Player p, int data) {
 		boolean has = check(p, construct(Perm.BREW, data));
 		return pawl ? !has : has;
 	}
 	
 	// Checks if a permission is explicitly set to false
 	private boolean permSetFalse(Player p, String perm) {
 		return p.isPermissionSet(perm) && !p.hasPermission(perm); 
 	}
 	
 	/**
 	 * Check to see if the permission is had. 
 	 * Does not adjust for PermsAsWhiteList.
 	 * @param p
 	 * @param perm
 	 * @return
 	 */
 	//
 	private boolean check(Player p, String[] perms) {
		if(p.isOp()) return pawl ? true : false;
 		for(int i = 0; i <= 3; i++) {
 			if(perms[i] == null) continue;
 			if(permSetFalse(p, perms[i])) return false;
 		}
 		for(String perm : perms) {
 			if(perm == null) continue;
 			if(p.hasPermission(perm)) return true;
 		}
 		return checkVault(p, perms);
 	}
 	
 	private boolean checkVault(Player p, String[] perms) {
 		// If vault is loaded
 		if(VaultHook.loaded) {
 			// Check each permission
 			for(String perm : perms) {
 				if(perm == null) continue;
 				if(VaultHook.permission.has(p, perm)) return true;
 			}
 			return false; // They don't have the permission
 		} else {
 			return false; // There is no vault.
 		}
 	}
 	
 	// Constructs an array of permissions with parents added
 	private String[] construct(String perm, Object o) {
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
 		} else if (o instanceof Integer) {
 			name = o.toString();
 			id = -1;
 			data = -1;
 		} else {
 			throw new UnsupportedOperationException("Unknown object type: " + o.getClass().getSimpleName());
 		}
 		
 		String[] perms = new String[7];
 		if(id != -1 && data != -1) {
 			perms[0] = perm + name + PS + data; // Standard name
 			perms[1] = perm + id + PS + data; // Standard number
 			if(data == 0) {
 				perms[2] = perm + id; // ID, no data
 				perms[3] = perm + name; // Name, no data
 			}
 			perms[4] = perm + name + PS + "*"; // Name, all data
 			perms[5] = perm + id + PS + "*"; // ID, all data
 			perms[6] = perm + "*"; // Entire feature
 			
 		} else {
 			perms[0] = perm + name; // Standard permission 
 		}
 		
 		return perms;
 	}	
 }
