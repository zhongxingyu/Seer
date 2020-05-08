 package mods.ryanjh5521.core.api;
 
 import mods.ryanjh5521.core.api.language.ILocalizationManager;
 import mods.ryanjh5521.core.api.net.INetworkingManager;
 
 /**
  * Contains methods to get instances of various API interfaces.
  * 
  * Mods using any APIs should specify "after:ryanjh5521Core" in their dependency list, so that
  * if the mod includes an outdated API then the newer one will take precedence.
  */
 public final class APILocator {
 	
 	private static <T> T getField(String name) {
 		try {
			return (T)Class.forName("mods.ryanjh5521.core.ImmibisCore").getField(name).get(null);
 		} catch(ClassNotFoundException e) {
 			return null;
 		} catch(RuntimeException e) {
 			throw e;
 		} catch(Exception e) {
 			throw (AssertionError)new AssertionError("Should not happen").initCause(e);
 		}
 	}
 	
 	/**
 	 * Returns the ID allocator interface, used to request block and item IDs
 	 * and add recipes.
 	 * 
 	 * Null if ryanjh5521 Core is not installed.
 	 */
 	public static IIDAllocator getIDAllocator() {
 		return getField("idAllocator");
 	}
 	
 	/**
 	 * Returns the networking manager interface, used to send and listen
 	 * for packets.
 	 * 
 	 * Null if ryanjh5521 Core is not installed.
 	 */
 	public static INetworkingManager getNetManager() {
 		return getField("networkingManager");
 	}
 	
 	/**
 	 * Returns the localization manager.
 	 * 
 	 * Null if ryanjh5521 Core is not installed.
 	 */
 	public static ILocalizationManager getLocalizationManager() {
 		return getField("localizationManager");
 	}
 }
