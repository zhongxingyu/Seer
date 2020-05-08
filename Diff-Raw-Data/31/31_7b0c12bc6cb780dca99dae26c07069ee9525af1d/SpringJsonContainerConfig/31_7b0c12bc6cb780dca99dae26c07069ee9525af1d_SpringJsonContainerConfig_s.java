 /* vim: set ts=2 et sw=2 cindent fo=qroca: */
 
 package com.globant.katari.shindig;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.Validate;
 import org.apache.shindig.common.JsonSerializer;
 import org.apache.shindig.common.util.ResourceLoader;
 import org.apache.shindig.config.AbstractContainerConfig;
 import org.apache.shindig.config.ContainerConfigELResolver;
 import org.apache.shindig.config.ContainerConfigException;
 import org.apache.shindig.config.DynamicConfigProperty;
 import org.apache.shindig.expressions.Expressions;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.Maps;
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 import com.google.inject.name.Named;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.logging.Logger;
 import java.util.logging.Level;
 
 import javax.el.ELContext;
 import javax.el.ELException;
 import javax.el.ValueExpression;
 
 /** Represents a container configuration using JSON notation.
  *
  * See config/container.js for an example configuration.
  *
  * This is almost the same as shindigs JsonContainerConfig, but replaces
  * %context% with the provided contextPath.
  */
 @Singleton
 public class SpringJsonContainerConfig extends AbstractContainerConfig {
   /** The class logger.
    */
   private static Logger log =
     Logger.getLogger(SpringJsonContainerConfig.class.getName());
 
   /** The symbol to separate mulitple config files.
    */
   public static final char FILE_SEPARATOR = ',';
 
   public static final String PARENT_KEY = "parent";
 
   // TODO: Rename this to simply "container", gadgets.container is unnecessary.
   public static final String CONTAINER_KEY = "gadgets.container";
 
   /** The configuration options as loaded from container.js (or its
    * replacement).
    */
   private final Map<String, Map<String, Object>> config;
 
   private final Expressions expressions;
 
   /** The web application context path.
    *
    * This is never null.
    */
   private final String context;
 
   /** Creates a new configuration from files.
    *
    * @param containers A comma separated list of configuration resources. It
    * cannot be null.
    *
    * @param contextPath The configured web application context path. It cannot
    * be null.
    *
    * @throws ContainerConfigException
    */
   @Inject
   public SpringJsonContainerConfig(
       @Named("shindig.containers.default") final String containers,
       @Named("katari.contextPath") final String contextPath,
       final Expressions expressions)
         throws ContainerConfigException {
 
     Validate.notNull(contextPath, "The context path cannot be null.");
 
     String tmpContext;
     if (!contextPath.startsWith("/")) {
       tmpContext = "/";
     } else {
       tmpContext = "";
     }
     tmpContext = tmpContext + contextPath;
     if (tmpContext.endsWith("/")) {
       // Remove the trailing '/'
       tmpContext = tmpContext.substring(0, tmpContext.length() - 1);
     }
     context = tmpContext;
 
     this.expressions = expressions;
     config = createContainers(loadContainers(containers));
     init();
   }
 
   /** Initializes the configuration.
    *
    * Called during construction.
    */
   @SuppressWarnings("unchecked")
   protected void init() {
     for (Map.Entry<String, Map<String, Object>> entry : config.entrySet()) {
       Map<String, Object> value;
       value = (Map<String, Object>) evaluateAll(entry.getValue());
       entry.setValue(value);
     }
   }
 
   /** The set of containers.
    *
    * @return a set with the values of the gadgets.container json entries. Never
    * null.
    */
   @Override
   public Collection<String> getContainers() {
     return Collections.unmodifiableSet(config.keySet());
   }
 
   /** The full collection of properties for one container.
    *
    * @param container The container, never null.
    *
    * @return a map of properties for the provided container. Null if not
    * found.
    */
   @Override
   public Map<String, Object> getProperties(final String container) {
     return config.get(container);
   }
 
   /** A property for one container.
    *
    * @param container The container, never null.
    *
    * @param property The name of the property to obtain, never null.
    *
    * @return the value of the property. Null if not found.
    */
   @Override
   public Object getProperty(final String container, final String property) {
     if (property.startsWith("${")) {
       // An expression!
       try {
         ValueExpression expression = expressions.parse(property, Object.class);
         return expression.getValue(createExpressionContext(container));
       } catch (ELException e) {
         return null;
       }
     }
 
     Map<String, Object> containerData = config.get(container);
     if (containerData == null) {
       return null;
     }
     return containerData.get(property);
   }
 
   /** Initialize each container's configuration.
    */
   private Map<String, Map<String, Object>> createContainers(
       final JSONObject json) {
     Map<String, Map<String, Object>> map = Maps.newHashMap();
     for (String container : JSONObject.getNames(json)) {
       ELContext context = createExpressionContext(container);
       map.put(container,
           jsonToMap(json.optJSONObject(container), expressions, context));
     }
     return map;
   }
 
  /** Make Expressions available to subclasses so they can create ELContexts
    */
   protected Expressions getExpressions() {
     return expressions;
   }
 
   /** Protected to allow overriding.
    */
   protected ELContext createExpressionContext(final String container) {
     return getExpressions().newELContext(
         new ContainerConfigELResolver(this, container));
   }
 
   /** Convert a JSON value to a configuration value.
    */
   private static Object jsonToConfig(final Object json, final Expressions
       expressions, final ELContext context) {
     if (JSONObject.NULL.equals(json)) {
       return null;
     } else if (json instanceof CharSequence) {
       return new DynamicConfigProperty(json.toString(), expressions, context);
     } else if (json instanceof JSONArray) {
       JSONArray jsonArray = (JSONArray) json;
       List<Object> values = new ArrayList<Object>(jsonArray.length());
       for (int i = 0, j = jsonArray.length(); i < j; ++i) {
         values.add(jsonToConfig(jsonArray.opt(i), expressions, context));
       }
       return Collections.unmodifiableList(values);
     } else if (json instanceof JSONObject) {
       return jsonToMap((JSONObject) json, expressions, context);
     }
 
     // A (boxed) primitive.
     return json;
   }
 
   private static Map<String, Object> jsonToMap(final JSONObject json,
       final Expressions expressions, final ELContext context) {
     Map<String, Object> values = new HashMap<String, Object>(json.length(), 1);
     for (String key : JSONObject.getNames(json)) {
       Object val = jsonToConfig(json.opt(key), expressions, context);
       if (val != null) {
         values.put(key, val);
       }
     }
     return Collections.unmodifiableMap(values);
   }
 
   /** Loads containers from directories recursively.
    *
    * Only files with a .js or .json extension will be loaded.
    *
    * @param files The files to examine.
    *
    * @throws ContainerConfigException
    */
   private void loadFiles(final File[] files, final JSONObject all)
       throws ContainerConfigException {
     for (File file : files) {
       try {
        if (file == null) continue;
         log.info("Reading container config: " + file.getName());
         if (file.isDirectory()) {
           loadFiles(file.listFiles(), all);
        } else if (file.getName().toLowerCase(Locale.ENGLISH).endsWith(".js") ||
            file.getName().toLowerCase(Locale.ENGLISH).endsWith(".json")) {
           if (!file.exists()) {
             throw new ContainerConfigException(
                 "The file '" + file.getAbsolutePath() + "' doesn't exist.");
           }
           loadFromString(ResourceLoader.getContent(file), all);
         } else {
          if (log.isLoggable(Level.FINEST))
             log.finest(file.getAbsolutePath()
                 + " doesn't seem to be a JS or JSON file.");
         }
       } catch (IOException e) {
         throw new ContainerConfigException("The file '"
             + file.getAbsolutePath() + "' has errors", e);
       }
     }
   }
 
   /** Loads resources recursively.
    *
    * @param files The base paths to look for container.xml
    *
    * @throws ContainerConfigException
    */
   private void loadResources(final String[] files, final JSONObject all)
     throws ContainerConfigException {
     try {
       for (String entry : files) {
         log.info("Reading container config: " + entry);
         String content = ResourceLoader.getContent(entry);
        if (content == null || content.length() == 0)
           throw new IOException("The file " + entry + "is empty");
         loadFromString(content, all);
       }
     } catch (IOException e) {
       throw new ContainerConfigException(e);
     }
   }
 
   /** Merges two JSON objects together (recursively), with values from "merge"
    * replacing values in "base" to produce a new object.
    *
    * @param base The base object that values will be replaced into.
    *
    * @param merge The object to merge values from.
    *
    * @throws JSONException if the two objects can't be merged for some reason.
    */
   private JSONObject mergeObjects(final JSONObject base, final JSONObject
       merge) throws JSONException {
     // Clone the initial object (JSONObject doesn't support "clone").
 
     JSONObject clone = new JSONObject(base, JSONObject.getNames(base));
     // Walk parameter list for the merged object and merge recursively.
     String[] fields = JSONObject.getNames(merge);
     for (String field : fields) {
       Object existing = clone.opt(field);
       Object update = merge.get(field);
       if (JSONObject.NULL.equals(existing) || JSONObject.NULL.equals(update)) {
         // It's new custom config, not referenced in the prototype, or
         // it's removing a pre-configured value.
         clone.put(field, update);
       } else {
         // Merge if object type is JSONObject.
        if (update instanceof JSONObject &&
            existing instanceof JSONObject) {
          clone.put(field, mergeObjects((JSONObject)existing,
                                        (JSONObject)update));
         } else {
           // Otherwise we just overwrite it.
           clone.put(field, update);
         }
       }
     }
     return clone;
   }
 
   /**
    * Recursively merge values from parent objects in the prototype chain.
    *
    * @return The object merged with all parents.
    *
    * @throws ContainerConfigException If there is an invalid parent parameter
    *    in the prototype chain.
    */
   private JSONObject mergeParents(final String container, final JSONObject all)
       throws ContainerConfigException, JSONException {
     JSONObject base = all.getJSONObject(container);
     if (DEFAULT_CONTAINER.equals(container)) {
       return base;
     }
 
     String parent = base.optString(PARENT_KEY, DEFAULT_CONTAINER);
     if (!all.has(parent)) {
       throw new ContainerConfigException(
           "Unable to locate parent '" + parent + "' required by "
           + base.getString(CONTAINER_KEY));
     }
     return mergeObjects(mergeParents(parent, all), base);
   }
 
   /**
    * Processes a container file.
    *
    * @param json
    * @throws ContainerConfigException
    */
   protected void loadFromString(final String json2, final JSONObject all)
     throws ContainerConfigException {
     // Context must start with / and must not end with /.
     String json = json2.replaceAll("%context%", context);
 
     try {
       JSONObject contents = new JSONObject(json);
       JSONArray containers = contents.getJSONArray(CONTAINER_KEY);
 
       for (int i = 0, j = containers.length(); i < j; ++i) {
         // Copy the default object and produce a new one.
         String container = containers.getString(i);
         all.put(container, contents);
       }
     } catch (JSONException e) {
       throw new ContainerConfigException("Trouble parsing " + json, e);
     }
   }
 
   /**
    * Loads containers from the specified resource. Follows the same rules
    * as {@code JsFeatureLoader.loadFeatures} for locating resources.
    *
    * @param path
    * @throws ContainerConfigException
    */
   private JSONObject loadContainers(final String path)
       throws ContainerConfigException {
     JSONObject all = new JSONObject();
     try {
       for (String location : StringUtils.split(path, FILE_SEPARATOR)) {
         if (location.startsWith("res://")) {
           location = location.substring(6);
           log.info("Loading resources from: " + location);
           if (path.endsWith(".txt")) {
             loadResources(ResourceLoader.getContent(location).split("[\r\n]+"),
                 all);
           } else {
             loadResources(new String[]{location}, all);
           }
         } else {
           log.info("Loading files from: " + location);
           File file = new File(location);
           loadFiles(new File[]{file}, all);
         }
       }
 
       // Now that all containers are loaded, we go back through them and merge
       // recursively. This is done at startup to simplify lookups.
       for (String container : JSONObject.getNames(all)) {
         all.put(container, mergeParents(container, all));
       }
 
       return all;
     } catch (IOException e) {
       throw new ContainerConfigException(e);
     } catch (JSONException e) {
       throw new ContainerConfigException(e);
     }
   }
 
   @Override
   public String toString() {
     return JsonSerializer.serialize(config);
   }
 
   private Object evaluateAll(final Object value) {
     if (value instanceof CharSequence) {
       return value.toString();
     } else if (value instanceof Map<?, ?>) {
       ImmutableMap.Builder<Object, Object> newMap = ImmutableMap.builder();
       for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
         newMap.put(entry.getKey(), evaluateAll(entry.getValue()));
       }
 
       return newMap.build();
     } else if (value instanceof List<?>) {
       ImmutableList.Builder<Object> newList = ImmutableList.builder();
       for (Object entry : (List<?>) value) {
         newList.add(evaluateAll(entry));
       }
 
       return newList.build();
     } else {
       return value;
     }
   }
 }
 
