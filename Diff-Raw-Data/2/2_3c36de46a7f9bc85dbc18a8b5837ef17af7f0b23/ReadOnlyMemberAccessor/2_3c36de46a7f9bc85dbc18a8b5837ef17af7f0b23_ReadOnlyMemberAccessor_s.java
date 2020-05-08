 package yucatan.communication;
 
 public interface ReadOnlyMemberAccessor {
 	/**
 	 * Provides an instance of an wrapped object. Usually a public member of the instance.
 	 * 
 	 * @param key The
 	 * @return The requested Object or null;
 	 */
	public Object get(String key);
 }
