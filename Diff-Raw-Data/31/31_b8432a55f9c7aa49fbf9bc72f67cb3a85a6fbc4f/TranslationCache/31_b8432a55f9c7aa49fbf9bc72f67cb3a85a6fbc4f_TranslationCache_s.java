 package translation;
 
 import net.sf.ehcache.CacheManager;
 import net.sf.ehcache.Ehcache;
 import net.sf.ehcache.Element;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.net.URL;
 import java.util.logging.Logger;
 public class TranslationCache {
 
    private final Logger l = Logger.getLogger(this.getClass().getName());
 
     private static TranslationCache tc = null;
     public static TranslationCache getTranslationCache() {
        if (tc == null) 
             tc = new TranslationCache();
         return tc;
     }
 
     private static final TranslationDatabase translationDatabase = new TranslationDatabase();
 
    private static CacheManager cacheManager = null;
     private CacheManager getCacheManager() {
         if (cacheManager==null) {
             URL url = this.getClass().getResource("/ehcache.xml");
             System.out.println("URL calculated as: "+url);
             cacheManager = CacheManager.create(url);
        }
         return cacheManager;
     }
 
   public String getTranslation(String word) {
     Element elem = getCache().get(word);
     if (elem == null) {
       String translatedWord = translationDatabase.getTranslation(word);
       if (translatedWord == null) { return null; }
       getCache().put(elem = new Element(word, translatedWord));
     }
     return (String) elem.getValue();
   }
 
     public void putTranslation(String word, String translation) {
         getCache().put(new Element(word, translation));
         l.info(String.format("just put a new element (%s,%s), into the cache", word, translation));
     }
 
   private String getCachedTranslation(String name) {
     Element elem = getCache().get(name);
     return elem != null ? (String) elem.getValue() : null;
   }
 
     public int getSizeOfTranslationDictionary() {
         return getOriginalWordsInCache().length;
     }
 
   public String[] getOriginalWordsInCache() {
     @SuppressWarnings("unchecked")
     Iterator<String> keys = ((List<String>) getCache().getKeys()).iterator();
     List<String> list = new ArrayList<String>();
     while (keys.hasNext()) {
       String name = keys.next();
       l.info(String.format("checking for the translation of word: '%s'", name));
       
       boolean showWeirdCondition = false;
       if (showWeirdCondition) {
           if (getCachedTranslation(name) != null) {
             list.add(name);
           } else throw new RuntimeException("very rare - wasn't expecting that - race condition?");
                   // this is really happening with my terracotta installation
                   // how is it possible that a key is found in the cache without the value ?
                   // I should investigate into it.
       }
       else list.add(name);
     }
     return list.toArray(new String[list.size()]);
   }
 
   public long getTTL() {
     return getCache().getCacheConfiguration().getTimeToLiveSeconds();
   }
 
   public long getTTI() {
     return getCache().getCacheConfiguration().getTimeToIdleSeconds();
   }
 
   public int getSize() {
     return getCache().getSize();
   }
 
   private Ehcache getCache() {
       final String cacheName = "dictionary";
       Ehcache retValue = getCacheManager().getEhcache(cacheName);
       if (retValue == null) throw new RuntimeException("unable to find cache: "+ cacheName);
       return retValue;
   }
 }
