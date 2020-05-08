 package eu.uberdust.caching;
 
 import net.sf.ehcache.Cache;
 import net.sf.ehcache.CacheManager;
 import net.sf.ehcache.Element;
 import org.apache.log4j.Logger;
 import org.aspectj.lang.JoinPoint;
 import org.aspectj.lang.ProceedingJoinPoint;
 import org.aspectj.lang.annotation.Around;
 import org.aspectj.lang.annotation.Aspect;
 import org.aspectj.lang.annotation.Pointcut;
 
 
 @Aspect
 public class CachingAspect {
 
     private static final Logger LOGGER = Logger.getLogger(CachingAspect.class);
 
     private final CacheManager singletonManager;
 
     public CachingAspect() {
         singletonManager = CacheManager.create();
 
     }
 
     /**
      * This pointcut matches all methods with a <code>@Cachable</code> annotation.
      */
     @Pointcut("execution(@Cachable * *.*(..))")
     @SuppressWarnings("unused")
     private void cache() {
     }
 
     /**
      * Returns objects from the cache if necessary.
      */
     @Around("cache()")
     public Object aroundCache(final ProceedingJoinPoint thisJoinPoint) throws Throwable {
         final String thisJoinPointName = getJoinPointName(thisJoinPoint);
         final String thisJoinPointArgs = getJointPointArgs(thisJoinPoint);
         final String objName = thisJoinPointName + "-" + thisJoinPointArgs;
        LOGGER.info("Injecting method: " + objName);
 
 
         if (singletonManager.cacheExists(thisJoinPointName)) {
             final Cache cache = singletonManager.getCache(thisJoinPointName);
 
            if (cache.getKeysWithExpiryCheck().contains(thisJoinPoint.getArgs()[0].hashCode())) {
                 LOGGER.info(" HIT: " + cache.getName());
                return cache.get(thisJoinPoint.getArgs()[0].hashCode()).getValue();
             } else {
                final Element element = new Element(thisJoinPoint.getArgs()[0].hashCode(), thisJoinPoint.proceed());
                 cache.put(element);
                 LOGGER.info("MISS: " + cache.getName());
                 return element.getValue();
             }
         } else {
             singletonManager.addCacheIfAbsent("defaultCache");
 
             final Cache cache = singletonManager.getCache("defaultCache");
 
            if (cache.getKeysWithExpiryCheck().contains((thisJoinPointName + thisJoinPoint.getArgs()[0]).hashCode())) {
 
                 LOGGER.info("HIT: " + cache.getName());
 
                return cache.get((thisJoinPointName + thisJoinPoint.getArgs()[0]).hashCode()).getValue();
             } else {
                final Element element = new Element((thisJoinPointName + thisJoinPoint.getArgs()[0]).hashCode(),
                         thisJoinPoint.proceed());
                 LOGGER.info("MISS: " + cache.getName());
                 cache.put(element);
                 return element.getValue();
             }
         }
     }
 
     @Pointcut("@annotation( thisCachename )")
     @SuppressWarnings("unused")
     private void evictCache(EvictCache thisCachename) {
     }
 
     @Around("evictCache(thisCachename)")
     public Object processRequest(final ProceedingJoinPoint thisJoinPoint, EvictCache thisCachename) throws Throwable {
         if (thisJoinPoint.getKind().equals(ProceedingJoinPoint.METHOD_CALL)) {
             return thisJoinPoint.proceed();
         } else {
 
             final String[] cacheNames = thisCachename.cacheName().split(",");
             for (final String cache : cacheNames) {
                 CacheManager.getInstance().getCache(cache).removeAll();
                 LOGGER.info("Evicting cache: " + cache);
             }
 
             return thisJoinPoint.proceed();
         }
     }
 
 
     /**
      * Convenience method that returns the class- and method-name for a given join point.
      */
     public final String getJoinPointName(final JoinPoint joinPoint) {
         return joinPoint.getThis().getClass().getCanonicalName() + "." + joinPoint.getSignature().getName();
     }
 
     /**
      * Returns the arguments of the current join point as a string.
      *
      * @param joinPoint
      * @return string representing the arguments of this join point
      */
     public final String getJointPointArgs(final JoinPoint joinPoint) {
         final StringBuilder buf = new StringBuilder();
         for (final Object arg : joinPoint.getArgs()) {
             buf.append(arg.getClass().getSimpleName()).append("-").append(arg.hashCode());
         }
        return buf.toString().replaceAll("\\+$", "");
     }
 }
