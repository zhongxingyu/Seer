 /* Copyright 2013 Kevin Seiden. All rights reserved.
 
  This works is licensed under the Creative Commons Attribution-NonCommercial 3.0
 
  You are Free to:
     to Share: to copy, distribute and transmit the work
     to Remix: to adapt the work
 
  Under the following conditions:
     Attribution: You must attribute the work in the manner specified by the author (but not in any way that suggests that they endorse you or your use of the work).
     Non-commercial: You may not use this work for commercial purposes.
 
  With the understanding that:
     Waiver: Any of the above conditions can be waived if you get permission from the copyright holder.
     Public Domain: Where the work or any of its elements is in the public domain under applicable law, that status is in no way affected by the license.
     Other Rights: In no way are any of the following rights affected by the license:
         Your fair dealing or fair use rights, or other applicable copyright exceptions and limitations;
         The author's moral rights;
         Rights other persons may have either in the work itself or in how the work is used, such as publicity or privacy rights.
 
  Notice: For any reuse or distribution, you must make clear to others the license terms of this work. The best way to do this is with a link to this web page.
  http://creativecommons.org/licenses/by-nc/3.0/
  */
 
 package io.github.alshain01.flags;
 
 import org.apache.commons.lang.Validate;
 import org.bukkit.Bukkit;
 import org.bukkit.permissions.Permission;
 import org.bukkit.permissions.PermissionDefault;
 
 import java.util.Set;
 
 /**
  * API for bundle management.
  */
 @SuppressWarnings("WeakerAccess") // API
 public final class Bundle {
 	/**
 	 * Gets a bundle from the data store.
 	 * 
 	 * @param bundle
 	 *            The bundle name to retrieve
 	 * @return A list containing the bundle. Null if it doesn't exist.
      * @throws java.lang.IllegalArgumentException
 	 */
 	public static Set<Flag> getBundle(String bundle) {
         Validate.notNull(bundle);
         if(!isBundle(bundle)) { throw new IllegalArgumentException("The provided bundle name does not exist."); }
 		return Flags.getDataStore().readBundle(bundle.toLowerCase());
 	}
 
 	/**
 	 * Gets a set of bundle names created on the server.
 	 * 
 	 * @return A set of bundles names configured on the server.
 	 */
 	public static Set<String> getBundleNames() {
 		return Flags.getDataStore().readBundles();
 	}
 
 	/**
 	 * Checks if a bundle name exists in the data store.
 	 * 
 	 * @param bundle
 	 *            A string bundle name.
 	 * @return True if the string is a valid bundle name.
 	 */
     public static boolean isBundle(String bundle) {
         Validate.notNull(bundle);
 		return getBundleNames().contains(bundle.toLowerCase());
 	}
 
     /**
      * Gets the total number of bundles defined on the server
      *
      * @return A count of the bundles on the server
      */
     @SuppressWarnings("unused") // API
     public static int count() {
         return getBundleNames().size();
     }
 
 	/**
 	 * Sets a bundle to the data file.
 	 * 
 	 * @param name
 	 *            The bundle name
 	 * @param flags
 	 *            A list of flags in the bundle.
 	 */
     @SuppressWarnings("unused") // API
 	public static void setBundle(String name, Set<Flag> flags) {
         Validate.notNull(name);
 
         if(flags != null) {
             // The main variable may be null to remove
             // but not the elements
             Validate.noNullElements(flags);
         }
 
 		Flags.getDataStore().writeBundle(name, flags);
        String permName = "flags.bundle." + name.toLowerCase();
         if(flags == null || flags.size() == 0) {
             if(Bukkit.getPluginManager().getPermission(permName) != null) {
                 Bukkit.getPluginManager().removePermission(permName);
             }
         } else {
             if(Bukkit.getPluginManager().getPermission(permName) == null) {
                 addPermission(name);
             }
         }
 	}
 
     // Used only on plugin enable
     static void registerPermissions() {
         for(String b : Flags.getDataStore().readBundles()) {
             addPermission(b);
         }
     }
 
     private static void addPermission(String name) {
         Validate.notNull(name);
 
         final Permission perm = new Permission("flags.bundle." + name.toLowerCase(),
                 "Grants ability to use the bundle " + name, PermissionDefault.FALSE);
         perm.addParent("flags.bundle", true);
         Bukkit.getPluginManager().addPermission(perm);
     }
 
 	private Bundle() {
 	}
 }
