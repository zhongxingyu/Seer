 
 package delegation;
 
 import java.util.ServiceLoader;
 
 /**
  * PriorityLoader - load an implementation provider based on the priorities
  * expressed by implementation factories.
  */
 public final class PriorityLoader {
   /** Private constructor. */
   private PriorityLoader() {
   }
 
   /**
    * Factory method.
    *
    * @return a PriorityLoader.
    */
   public static PriorityLoader get() {
     return new PriorityLoader();
   }
 
   /**
    * Load a service instance that provides the required service.
    *
    * <p>Multiple service providers can all satisfy the service requirement;
    * they express their relative ability to do so by analyzing the runtimeHint
    * and returning a priority level that indicates how willing they are to
    * provide it. The highest-ranked provider is instantiated.</p>
    *
    * @param service the service to load.
    * @param runtimeHint a hint provided by the system (version string, etc)
    *     indicating the environment in which this is running, and used to
    *     sort providers by their returned priorities.
    * @return a new instance of the 'service' class if available, or null if
    *     none is avialable.
    */
   public <T extends PriorityProvider> T loadProvider(Class<T> service, String runtimeHint) {
     ServiceLoader<T> loader = ServiceLoader.load(service);
 
     int bestPriority = 0;
     T bestService = null;
 
     for (T serviceImpl : loader) {
       int thisPriority = serviceImpl.getPriority(runtimeHint);
       if (thisPriority > bestPriority) {
         bestService = serviceImpl;
       }
     }
 
     return bestService;
   }
 }
