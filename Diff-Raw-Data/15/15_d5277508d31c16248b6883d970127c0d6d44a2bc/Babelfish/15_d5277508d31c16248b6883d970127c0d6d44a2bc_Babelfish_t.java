 /*
  * Made with all the love in the world
  * by scireum in Remshalden, Germany
  *
  * Copyright by scireum GmbH
  * http://www.scireum.de - info@scireum.de
  */
 
 package sirius.kernel.nls;
 
 import com.google.common.collect.Maps;
 import sirius.kernel.Classpath;
 import sirius.kernel.Sirius;
 import sirius.kernel.commons.*;
 import sirius.kernel.health.Exceptions;
 import sirius.kernel.health.Log;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.util.*;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Internal translation engine used by {@link NLS}.
  * <p>
  * Loads all .properties files in the given {@link Classpath} which match the pattern
  * <code>name_lang.properties</code>. Where <tt>name</tt> is any string (must not contain "_") and <tt>lang</tt>
  * is a two letter language code.
  * </p>
  *
  * @author Andreas Haufler (aha@scireum.de)
  * @see NLS
  * @since 2013/08
  */
 public class Babelfish {
 
     /*
      * Logs WARN messages if translations are inconsistent (override each other).
      */
     protected static final Log LOG = Log.get("babelfish");
 
     /*
      * Since the translationMap is not thread-safe, modifying the map requires to acquire this lock
      */
     private final Lock translationsWriteLock = new ReentrantLock();
 
     /*
      * Contains all known translations
      */
     private Map<String, Translation> translationMap = Maps.newTreeMap();
 
     /*
      * Contains the relative paths of all loaded files
      */
     private Map<String, Long> loadedFiles = Maps.newConcurrentMap();
 
     /*
      * Used to frequently check loaded properties when running in DEVELOP mode.
      */
     private Timer reloadTimer;
 
     /*
      * Determines the interval which files are checked for update
      */
     private static final int RELOAD_INTERVAL = 1000;
 
     /**
      * Enumerates all translations matching the given filter.
      *
      * @param filter    the filter to apply when enumerating translations. The given filter must occur in either the key
      *                  or in one of the translations.
      * @param collector the collector which is supplied with all matching translations
      */
     public void getTranslations(String filter, Collector<Translation> collector) {
         String effectiveFilter = Strings.isEmpty(filter) ? null : filter.toLowerCase();
         for (Map.Entry<String, Translation> entry : translationMap.entrySet()) {
             if (entry.getValue().containsText(effectiveFilter)) {
                 collector.add(entry.getValue());
             }
         }
     }
 
     /**
      * Tries to write out all stored properties to their respective source location.
      * <p>
      * This intended to be called in development systems after translations have been edited via the web interface.
      * </p>
      */
     public void writeProperties() {
         LOG.INFO("Writing properties back to source files...");
         final MultiMap<String, Translation> mm = MultiMap.create();
         final ValueHolder<Integer> propertiesWithoutFile = ValueHolder.of(0);
         getTranslations(null, new BasicCollector<Translation>() {
 
             @Override
             public void add(Translation entity) {
                 if (entity.getFile() == null) {
                     propertiesWithoutFile.set(propertiesWithoutFile.get() + 1);
                 } else {
                     mm.put(entity.getFile(), entity);
                 }
             }
         });
         if (propertiesWithoutFile.get() > 0) {
             LOG.WARN("Skipping %d properties, without any file information", propertiesWithoutFile.get());
         }
 
         int properties = 0;
         int totalFiles = 0;
         int updatedFiles = 0;
         for (Map.Entry<String, Collection<Translation>> e : mm.getUnderlyingMap().entrySet()) {
             Map<String, SortedProperties> propMap = Maps.newTreeMap();
             Properties props = new SortedProperties();
             for (Translation entry : e.getValue()) {
                 properties++;
                 entry.writeTo(propMap);
             }
             for (Map.Entry<String, SortedProperties> entry : propMap.entrySet()) {
                 try {
                     File file = findSource(e.getKey() + "_" + entry.getKey() + ".properties");
                     if (file != null) {
                         FileOutputStream out = new FileOutputStream(file);
                         try {
                             entry.getValue().store(out, null);
                             updatedFiles++;
                         } finally {
                             out.close();
                         }
                     }
                 } catch (Throwable ex) {
                     Exceptions.handle()
                               .error(ex)
                               .to(LOG)
                               .withSystemErrorMessage("Error writing properties of: %s - %s (%s)", entry.getKey())
                               .handle();
                 }
                 totalFiles++;
             }
         }
         LOG.INFO("Wrote %d properties in %d of %d files", properties, updatedFiles, totalFiles);
     }
 
     private File findSource(String fileName) {
         String[] path = fileName.split("/");
         if (path.length == 0) {
             return null;
         }
 
         File baseDir = new File(System.getProperty("user.dir"));
         return findFile(baseDir.getParentFile(), path);
     }
 
     /*
      * When searching for the .properties file on disk for a given relative path, we need to scan all source folders.
      * This list enumerates directories not to enter, since they either contain a copy of the file or might
      * be too large to search through
      */
     private static final String[] IGNORED_DIRECTORIES = new String[]{"bin", "out", "target", "build", "data", "dist"};
 
     /*
      * Tries to find a source file for the given path, starting from current.
      */
     private File findFile(File current, String[] path) {
         if (!current.isDirectory() || !current.exists()) {
             return null;
         }
         for (File child : current.listFiles()) {
             if (child.getName().equals(path[0])) {
                 File result = completeFile(child, path);
                 if (result != null) {
                     return result;
                 }
             }
         }
         for (File child : current.listFiles()) {
             // We need a directory...
             if (!child.isDirectory()) {
                 break;
             }
             // Don't step into hidden directories
             if (child.isHidden() || child.getName().startsWith(".")) {
                 break;
             }
             // Don't step into one of the generally ignored directories
             for (String ignoredDir : IGNORED_DIRECTORIES) {
                 if (child.getName().startsWith(ignoredDir)) {
                     break;
                 }
             }
             File result = findFile(child, path);
             if (result != null) {
                 return result;
             }
         }
 
         return null;
     }
 
     /*
      * Tries to find the file denoted by the given path, starting from the given basePath
      */
     private File completeFile(File basePath, String[] path) {
         File result = basePath;
         for (int i = 1; i < path.length; i++) {
             result = new File(result, path[i]);
         }
         if (result.exists() && result.isFile() || (path.length > 1 && result.getParentFile().exists())) {
             return result;
         }
 
         return null;
     }
 
     /**
      * Retrieves the <tt>Translation</tt> for the given property.
      * <p>
      * If no matching entry is found, the given <tt>fallback</tt> is used. If still no match was found, either
      * a new one is create (if <tt>create</tt> is <tt>true</tt>) or <tt>false</tt> otherwise.
      * </p>
      *
      * @param property the property key for which a translation is required
      * @param fallback a fallback key, if no translation is found. May be <tt>null</tt>.
      * @param create   determines if a new <tt>Translation</tt> is created if non-existent
      * @return a <tt>Translation</tt> for the given property or fallback.
      *         Returns <tt>null</tt> if no translation was found and <tt>create</tt> is false.
      */
     protected Translation get(String property, String fallback, boolean create) {
         Translation entry = translationMap.get(property);
         if (entry == null && fallback != null) {
             entry = translationMap.get(fallback);
         }
         if (entry == null && create) {
             LOG.INFO("Non-existent translation: %s", property);
             entry = new Translation(property);
             entry.setAutocreated(true);
             Map<String, Translation> copy = new TreeMap<String, Translation>(translationMap);
             translationsWriteLock.lock();
             try {
                 copy.put(entry.getKey(), entry);
                 translationMap = copy;
             } finally {
                 translationsWriteLock.unlock();
             }
         }
 
         return entry;
     }
 
     /*
      * Describes the pattern for .properties files of interest.
      */
     private static final Pattern PROPERTIES_FILE = Pattern.compile("(.*?)_([a-z]{2}).properties");
 
     /**
      * Initializes the translation engine by using the given classpath.
      * <p>
      * Scans for all .properties files matching <tt>PROPERTIES_FILE</tt> and loads their contents.
      * </p>
      *
      * @param classpath the classpath used to discover all relevant properties files
      */
     protected void init(final Classpath classpath) {
         classpath.find(PROPERTIES_FILE, new BasicCollector<Matcher>() {
             @Override
             public void add(Matcher value) {
                 LOG.FINE("Loading: %s", value.group());
                 String baseName = value.group(1);
                 String lang = value.group(2);
                 importProperties(value.group(), baseName, lang, getLastModified(classpath, value.group()));
             }
         });
 
         if (Sirius.isDev()) {
             if (reloadTimer == null) {
                 reloadTimer = new Timer(true);
                 reloadTimer.schedule(new TimerTask() {
                     @Override
                     public void run() {
                         Thread.currentThread().setName("Babelfish-ResourceWatch");
                         for (Map.Entry<String, Long> entry : loadedFiles.entrySet()) {
                             long lastModified = getLastModified(classpath, entry.getKey());
                             if (lastModified > entry.getValue()) {
                                LOG.INFO("Reloading: %s", entry.getKey());
                                 Matcher m = PROPERTIES_FILE.matcher(entry.getKey());
                                 if (m.matches()) {
                                     importProperties(entry.getKey(), m.group(1), m.group(2), lastModified);
                                 }
                             }
                         }
                     }
                 }, RELOAD_INTERVAL, RELOAD_INTERVAL);
             }
         }
     }
 
     private long getLastModified(Classpath classpath, String relativePath) {
         try {
            return new File(classpath.getLoader().getResource(relativePath).toURI()).lastModified();
         } catch (Throwable e) {
             return 0;
         }
     }
 
     /*
      * Disables the cached for properties files to enable reloading
      */
     private static class NonCachingControl extends ResourceBundle.Control {
 
         @Override
         public long getTimeToLive(String baseName, Locale locale) {
             return ResourceBundle.Control.TTL_DONT_CACHE;
         }
 
     }
 
     private static ResourceBundle.Control CONTROL = new NonCachingControl();
 
 
     private void importProperties(String relativePath, String baseName, String lang, long lastModified) {
        ResourceBundle bundle = ResourceBundle.getBundle(baseName + "_" + lang, CONTROL);
         translationsWriteLock.lock();
         try {
             Map<String, Translation> copy = new TreeMap<String, Translation>(translationMap);
             for (String key : bundle.keySet()) {
                 String value = bundle.getString(key);
                 importProperty(copy, lang, relativePath, key, value);
             }
             translationMap = copy;
         } finally {
             translationsWriteLock.unlock();
         }
         loadedFiles.put(relativePath, lastModified);
     }
 
     private static void importProperty(Map<String, Translation> modifyableTranslationsCopy,
                                        String lang,
                                        String file,
                                        String key,
                                        String value) {
         Translation entry = modifyableTranslationsCopy.get(key);
         if (entry != null && entry.isAutocreated()) {
             // The entry was previously auto created - therefore we now replace it with our contents. This is a rare
             // case where a static initialize might fetch a property before the initialization of this class.
             entry = null;
         }
         if (entry == null) {
             entry = new Translation(key);
             entry.setFile(file);
             modifyableTranslationsCopy.put(key, entry);
         }
         entry.addTranslation(lang, value);
     }
 }
