 package be.uclouvain.jail.adapt;
 
 /** 
  * Marker for adaptable classes. 
  * 
  * <p>An adaptable class C claims its ability to convert himself to some other 
 * classes D, E, F, etc. Users may then invoke the {@link #adapt(Class)} method 
  * to adapt instances of C. Each adaptable class should declare the predefined 
  * classes which are recognized as adaptation targets. Note however that additional
  * adaptations may be provided by external adapters registered to {@link AdaptUtils}
  * utility class.</p>
  */
 public interface IAdaptable {
 
 	/** 
 	 * Adapts the object to a specific class. 
 	 * 
 	 * <p>The algorithm provided by this method should always be the following:</p>
 	 * <pre>
 	 *     // test natural adaptabilities of the class,
 	 *     // return adapted instance when adaptibility found 
 	 *     [...] 
 	 *     
 	 *     // allow external adapters to do their work
 	 *     return AdaptUtils.externalAdapt(this,c);
 	 * </pre> 
 	 * 
 	 * @param c the class of which returned object should be an instance.
 	 * @return an instance of class C (or one of its subclass), null if the adaptation
 	 * cannot be provided. 
 	 */
 	public <T> Object adapt(Class<T> c);
 	
 }
