 package mustache.core;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * The {@code Context} class is a wrapper providing a facility for Mustache
  * {@link Interpolation}. It offers a means to querying a specific value in a
  * complex graph of objects. The {@code Context} itself only holds the root
  * object of the graph.
  * 
  * <h4>Interpolation</h4>
  * 
  * <p>
  * Interpolation has two distinct behaviours :
  * <ol>
  *  <li>returning the root object for {@link #SELF} queries</li>
  *  <li>computing {@link Interpolation} otherwise</li>
  * </ol>
  * </p>
  * 
  * <h4>Behaviour</h4>
  * 
  * <p>
  * Though instances of this class are themself <i>immutable</i>, it might not be
  * the case for data they're holding. Those data might not even be suitable for
  * <i>concurrent access</i>. It is therefore not recommended to share instances
  * among multiple threads. One should also be aware that instances of this class
  * hold <i>strong references</i> to interpolated data. Retaining such instances
  * might lead to memory leaks if not handled with care.
  * </p>
  * 
  * @author Dri
  */
 public class Context {
 	
 	/**
 	 * The interpolation representing actual data wrapped by the {@code Context}.
 	 */
 	public static final String SELF = ".";
 	
 	private final Object data;
 	
 	private Context(Object data) {
 		this.data = data;
 	}
 	
 	/**
 	 * A factory for creating a {@code Context} holding {@code data}.
 	 * 
 	 * @param data data wrapped by the {@code Context}
 	 * @return a newly created {@code Context}
 	 */
 	public static Context newInstance(Object data) {
 		return isBasic(data) ? new BasicContext(data) : new Context(data);
 	}
 	
 	/**
 	 * A factory for creating a {@link List} of {@code Context} holding {@code data}.
 	 * 
 	 * @param data a list of data wrapped by the {@code Context} instances
 	 * @return a {@link List} of newly created {@code Context}
 	 */
 	public static List<Context> newInstances(List<?> data) {
 		if (data == null) {
 			throw new IllegalArgumentException();
 		}
 		
 		List<Context> contexts = new ArrayList<Context>( data.size() );
 		
 		for (Object item : data) {
 			contexts.add( newInstance(item) );
 		}
 		
 		return contexts;
 	}
 	
 	/**
 	 * Returns {@code true} if the query is a valid interpolation. In a
 	 * {@code Context}, {@link #SELF} is a valid query.
 	 * 
 	 * @param query the query to check
 	 * @return {@code true} if the query is valid
 	 * @see Interpolation#isValidQuery(String)
 	 */
 	public static boolean isValidQuery(String query) {
 		return SELF.equals(query) || Interpolation.isValidQuery(query);
 	}
 	
 	/**
 	 * This method interpolates a value within the context unless the query equals
 	 * to {@link #SELF}.
 	 * 
 	 * @param query the context query
 	 * @return the interpolated value or {@code null} if it failed
 	 * @throws IllegalArgumentException if the query is not valid
 	 * @see Interpolation#interpolate(String, Object)
 	 */
 	public Object interpolate(String query) {
 		if ( SELF.equals(query) ) {
 			return data;
 		}
 		return Interpolation.interpolate(query, data);
 	}
 	
 	/**
 	 * Indicates whether the first part of the query matches a variable in the
 	 * context's root. A {@link #SELF} query shall always return {@code true}
 	 * since it matches the root itself.
 	 * 
 	 * @param query the context query
 	 * @return {@code true} if there is a base variable matching the query
 	 * @throws IllegalArgumentException if the query is not valid
 	 * @see Interpolation#hasBaseVariable(String, Object)
 	 */
 	public boolean hasBaseVariable(String query) {
 		if ( SELF.equals(query) ) {
 			return true;
 		}
 		return Interpolation.hasBaseVariable(query, data);
 	}
 	
 	private static final Class<?>[] BASIC_CLASSES = {String.class, Number.class, Boolean.class};
 	
 	private static boolean isBasic(Object data) {
 		if (data == null) {
 			return true;
 		}
 		
 		for (Class<?> clazz : BASIC_CLASSES) {
 			if ( clazz.isInstance(data) ) {
 				return true;
 			}
 		}
 		
		return !data.getClass().isArray();
 	}
 	
 	/**
 	 * The {@code BasicContext} class is a simplified {@link Context} for basic types.
 	 * Basic types are types for which interpolation other than {@link #SELF} is irrelevant.
 	 */
 	static class BasicContext extends Context {
 		private BasicContext(Object data) {
 			super(data);
 		}
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public Object interpolate(String query) {
 			if ( SELF.equals(query) ) {
 				return super.data;
 			}
 			
 			Interpolation.checkQuery(query);
 			return null;
 		}
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public boolean hasBaseVariable(String query) {
 			if ( SELF.equals(query) ) {
 				return true;
 			}
 			
 			Interpolation.checkQuery(query);
 			return false;
 		}
 	}
 }
