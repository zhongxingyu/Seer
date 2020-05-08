 /*******************************************************************************
  * Copyright (c) 2013 James Richardson
  * 
  * BukkitPermissionManager.java is part of BukkitUtilities.
  * 
  * BukkitUtilities is free software: you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation, either version 3 of the License, or (at your option) any
  * later version.
  * 
  * BukkitUtilities is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * BukkitUtilities. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package name.richardson.james.bukkit.utilities.permissions;
 
 import java.util.ArrayList;
import java.util.Arrays;
 import java.util.Collections;
import java.util.LinkedList;
 import java.util.List;
 import java.util.ResourceBundle;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.permissions.Permission;
 import org.bukkit.permissions.PermissionDefault;
 import org.bukkit.plugin.PluginManager;
 
import name.richardson.james.bukkit.utilities.formatters.StringFormatter;
 import name.richardson.james.bukkit.utilities.localisation.ResourceBundles;
 import name.richardson.james.bukkit.utilities.logging.PluginLogger;
 
 public class BukkitPermissionManager implements PermissionManager {
 
 	private static final PluginManager pluginManager = Bukkit.getPluginManager();
 	private static final ResourceBundle localisation = ResourceBundle.getBundle(ResourceBundles.PERMISSIONS.getBundleName());
 	private static final Logger logger = PluginLogger.getLogger(BukkitPermissionManager.class);
 
 	private final List<Permission> permissions = new ArrayList<Permission>();
 
 	public BukkitPermissionManager() {
 		return;
 	}
 
 	public BukkitPermissionManager(final Permission parent) {
 		this.permissions.add(parent);
 	}
 
 	public Permission addPermission(final Permission permission) {
 		BukkitPermissionManager.logger.log(Level.CONFIG, "Adding permission: {0}", permission.getName());
 		BukkitPermissionManager.pluginManager.addPermission(permission);
 		this.permissions.add(permission);
 		return permission;
 	}
 
 	public Permission createPermission(final String node) {
 		final String description = BukkitPermissionManager.localisation.getString(node);
 		final Permission permission = new Permission(node, description);
 		final Permission parent = this.getParentPermission(permission);
 		if (this.getParentPermission(permission) != null) {
 			permission.addParent(parent, true);
 		}
 		return this.addPermission(permission);
 	}
 
 	public Permission createPermission(final String node, final PermissionDefault defaultPermission) {
 		final String description = BukkitPermissionManager.localisation.getString(node);
 		final Permission permission = new Permission(node, description, defaultPermission);
 		final Permission parent = this.getParentPermission(permission);
 		if (this.getParentPermission(permission) != null) {
 			permission.addParent(parent, true);
 		}
 		return this.addPermission(permission);
 	}
 
 	public Permission createPermission(final String node, final PermissionDefault defaultPermission, final Permission parent) {
 		final String description = BukkitPermissionManager.localisation.getString(node);
 		final Permission permission = new Permission(node, description, defaultPermission);
 		permission.addParent(permission, true);
 		return this.addPermission(permission);
 	}
 
 	public Permission createPermission(final String node, final PermissionDefault defaultPermission, final Permission parent, final boolean defaultParent) {
 		final String description = BukkitPermissionManager.localisation.getString(node);
 		final Permission permission = new Permission(node, description, defaultPermission);
 		permission.addParent(permission, defaultParent);
 		return this.addPermission(permission);
 	}
 
 	public List<Permission> createPermissions(final String[] nodes) {
 		final List<Permission> permissions = new ArrayList<Permission>();
 		for (final String node : nodes) {
 			this.createPermission(node);
 		}
 		return permissions;
 	}
 
 	public List<Permission> listPermissions() {
 		return Collections.unmodifiableList(this.permissions);
 	}
 
 	private Permission getParentPermission(final Permission permission) {
		final List<String> nodes = new LinkedList<String>(Arrays.asList(permission.getName().split("\\.")));
		if (nodes.size() > 1) {
			nodes.remove(nodes.size() - 1);
			final String parentNode = StringFormatter.combineString(nodes, ".");
			BukkitPermissionManager.logger.log(Level.FINE, "Resolving parent permission: {0}", parentNode);
 			return Bukkit.getPluginManager().getPermission(parentNode);
 		} else {
 			return null;
 		}
 	}
 }
