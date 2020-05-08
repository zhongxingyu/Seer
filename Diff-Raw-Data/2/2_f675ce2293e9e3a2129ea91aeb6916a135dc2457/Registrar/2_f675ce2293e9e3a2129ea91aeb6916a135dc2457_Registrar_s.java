 package alshain01.Flags;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.permissions.Permission;
 import org.bukkit.permissions.PermissionDefault;
 
 import alshain01.Flags.Flag;
 
 public class Registrar {
 	ConcurrentHashMap<String, Flag> flagStore = new ConcurrentHashMap<String, Flag>();
 	
 	protected Registrar() { }
 	
 	/**
 	 * Registers a non-player flag
 	 * 
 	 * @param name The name of the flag
 	 * @param description A brief description of the flag
 	 * @param def The flag's default state
 	 * @param group The group the flag belongs in. 
 	 * @return The flag if the flag was successfully registered. Null otherwise.
 	 */
 	public Flag register(String name, String description, boolean def, String group) {
 		if(!flagStore.containsKey(name)) {
 			Flag flag = new Flag(name, description, def, group, false, null, null);
 			flagStore.put(name, flag);
 			
 			//Add the permission for the flag to the server
 			Permission perm = new Permission(flag.getPermission(), "Allows players to set the flag " + flag.getName(), PermissionDefault.OP);
 			perm.addParent("flags.flagtype.*", true);
 			Bukkit.getServer().getPluginManager().addPermission(perm);
 
 			return flag;
 		}
 		return null;
 	}
 	
 	/**
 	 * Registers a player flag
 	 * 
 	 * @param name The name of the flag
 	 * @param description A brief description of the flag
 	 * @param def The flag's default state
 	 * @param group The group the flag belongs in. 
 	 * @param areaMessage The default message for areas.
 	 * @param worldMessage The default message for worlds.
 	 * @return The flag if the flag was successfully registered. Null otherwise.
 	 */
 	public Flag register(String name, String description, boolean def, String group, String areaMessage, String worldMessage) {
 		if(!flagStore.containsKey(name)) {
 			Flag flag = new Flag(name, description, def, group, true, areaMessage, worldMessage);
 			flagStore.put(name, flag);
 			
 			//Add the permission for the flag to the server
 			Permission perm = new Permission(flag.getPermission(), "Allows players to set the flag " + flag.getName(), PermissionDefault.OP);
 			perm.addParent("flags.flagtype.*", true);
 			Bukkit.getServer().getPluginManager().addPermission(perm);
 			
 			//Add the permission for the flag bypass to the server
			perm = new Permission(flag.getBypassPermission(), "Allows players to bypass the effects of the flag " + flag.getName(), PermissionDefault.OP);
 			perm.addParent("flags.bypass.*", true);
 			Bukkit.getServer().getPluginManager().addPermission(perm);
 			
 			return flag;
 		}
 		return null;
 	}
 	
 	/**
 	 * Informs whether or not a flag name has been registered.
 	 * 
 	 * @param flag The flag name
 	 * @return True if the flag name has been registered
 	 */
 	public boolean isFlag(String flag) {
 		return flagStore.containsKey(flag);
 	}
 	
 	/**
 	 * Retrieves a flag based on it's case sensitive name.
 	 * 
 	 * @param flag The flag to retrieve.
 	 * @return The flag requested or null if it does not exist.
 	 */
 	public Flag getFlag(String flag) {
 		if(isFlag(flag)) {
 			return flagStore.get(flag);
 		}
 		return null;
 	}
 	
 	/**
 	 * Gets a flag, ignoring the case.
 	 * This is an inefficient method, use it
 	 * only when absolutely necessary.
 	 * 
 	 * @param flag The flag to retrieve.
 	 * @return The flag requested or null if it does not exist.
 	 */
 	public Flag getFlagIgnoreCase(String flag) {
 		for(Flag f : getFlags())
 			if(f.getName().equalsIgnoreCase(flag)) {
 				return f;
 			}
 		return null;
 	}
 	
 	/**
 	 * Gets a collection of all registered flags.
 	 * 
 	 * @return A collection of all the flags registered.
 	 */
 	public Collection<Flag> getFlags() {
 		return flagStore.values();
 	}
 
 	/**
 	 * Gets a set of all registered flag names.
 	 * 
 	 * @return A list of names of all the flags registered.
 	 */
 	public Set<String> getFlagNames() {
 		return new HashSet<String>(Collections.list(flagStore.keys()));
 	}
 	
 	/**
 	 * Gets a set of all registered flag group names.
 	 * 
 	 * @return A list of names of all the flags registered.
 	 */
 	public Set<String> getFlagGroups() {
 		Set<String> groups = new HashSet<String>();
 		for(Flag flag : flagStore.values()) {
 			if(!groups.contains(flag.getGroup())) {
 				groups.add(flag.getGroup());
 			}
 		}
 		return new HashSet<String>(Collections.list(flagStore.keys()));
 	}
 }
