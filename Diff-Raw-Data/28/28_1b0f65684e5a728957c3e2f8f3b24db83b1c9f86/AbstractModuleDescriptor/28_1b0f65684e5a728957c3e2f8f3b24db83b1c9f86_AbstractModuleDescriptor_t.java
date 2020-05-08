 package com.atlassian.plugin.descriptors;
 
 import com.atlassian.plugin.ModuleDescriptor;
 import com.atlassian.plugin.Plugin;
 import com.atlassian.plugin.PluginParseException;
 import com.atlassian.plugin.Resources;
 import com.atlassian.plugin.StateAware;
 import com.atlassian.plugin.elements.ResourceDescriptor;
 import com.atlassian.plugin.elements.ResourceLocation;
 import com.atlassian.plugin.loaders.LoaderUtils;
 import com.atlassian.plugin.util.JavaVersionUtils;
 import static com.atlassian.plugin.util.validation.ValidationPattern.createPattern;
 import static com.atlassian.plugin.util.validation.ValidationPattern.test;
 import com.atlassian.plugin.util.validation.ValidationPattern;
 
 import org.dom4j.Element;
 
 import java.lang.reflect.Constructor;
 import java.util.List;
 import java.util.Map;
 
 public abstract class AbstractModuleDescriptor<T> implements ModuleDescriptor<T>, StateAware
 {
     protected Plugin plugin;
     String key;
     String name;
     String moduleClassName;
     Class<T> moduleClass;
     String description;
     boolean enabledByDefault = true;
     boolean systemModule = false;
 
     /**
      * @deprecated since 2.2.0
      */
     protected boolean singleton = true;
     Map<String, String> params;
     protected Resources resources = Resources.EMPTY_RESOURCES;
     private Float minJavaVersion;
     private String i18nNameKey;
     private String descriptionKey;
     private String completeKey;
     boolean enabled = false;
 
     public void init(final Plugin plugin, final Element element) throws PluginParseException
     {
         validate(element);
 
         this.plugin = plugin;
         this.key = element.attributeValue("key");
         this.name = element.attributeValue("name");
         this.i18nNameKey = element.attributeValue("i18n-name-key");
         this.completeKey = buildCompleteKey(plugin, key);
         this.description = element.elementTextTrim("description");
         this.moduleClassName = element.attributeValue("class");
         final Element descriptionElement = element.element("description");
         this.descriptionKey = (descriptionElement != null) ? descriptionElement.attributeValue("key") : null;
         params = LoaderUtils.getParams(element);
 
         if ("disabled".equalsIgnoreCase(element.attributeValue("state")))
         {
             enabledByDefault = false;
         }
 
         if ("true".equalsIgnoreCase(element.attributeValue("system")))
         {
             systemModule = true;
         }
 
         if (element.element("java-version") != null)
         {
             minJavaVersion = Float.valueOf(element.element("java-version").attributeValue("min"));
         }
 
         if ("false".equalsIgnoreCase(element.attributeValue("singleton")))
         {
             singleton = false;
         }
         else if ("true".equalsIgnoreCase(element.attributeValue("singleton")))
         {
             singleton = true;
         }
         else
         {
             singleton = isSingletonByDefault();
         }
 
         resources = Resources.fromXml(element);
     }
 
     /**
      * Validates the element, collecting the rules from {@link #provideValidationRules(ValidationPattern)}
      *
      * @param element The configuration element
      * @since 2.2.0
      */
     private void validate(Element element)
     {
         ValidationPattern pattern = createPattern();
         provideValidationRules(pattern);
         pattern.evaluate(element);
     }
 
     /**
      * Provides validation rules for the pattern
      *
      * @param pattern The validation pattern
      * @since 2.2.0
      */
     protected void provideValidationRules(ValidationPattern pattern)
     {
         pattern.rule(test("@key").withError("The key is required"));
     }
 
     /**
      * Override this for module descriptors which don't expect to be able to load a class successfully
      * @param plugin
      * @param element
      * @deprecated Since 2.1.0, use {@link #loadClass(Plugin,String)} instead
      */
     @Deprecated
     protected void loadClass(final Plugin plugin, final Element element) throws PluginParseException
     {
         loadClass(plugin, element.attributeValue("class"));
     }
 
     /**
      * Override this for module descriptors which don't expect to be able to load a class successfully
      * @param plugin
      * @param clazz The module class name to load
      * @since 2.1.0
      */
     protected void loadClass(final Plugin plugin, final String clazz) throws PluginParseException
     {
         try
         {
             if (clazz != null) //not all plugins have to have a class
             {
                 // First try and load the class, to make sure the class exists
                 @SuppressWarnings("unchecked")
                 final Class<T> loadedClass = (Class<T>) plugin.loadClass(clazz, getClass());
                 moduleClass = loadedClass;
 
                 // Then instantiate the class, so we can see if there are any dependencies that aren't satisfied
                 try
                 {
                     final Constructor<T> noargConstructor = moduleClass.getConstructor(new Class[] {});
                     if (noargConstructor != null)
                     {
                         moduleClass.newInstance();
                     }
                 }
                 catch (final NoSuchMethodException e)
                 {
                     // If there is no "noarg" constructor then don't do the check
                 }
             }
         }
         catch (final ClassNotFoundException e)
         {
             throw new PluginParseException("Could not load class: " + clazz, e);
         }
         catch (final NoClassDefFoundError e)
         {
             throw new PluginParseException("Error retrieving dependency of class: " + clazz + ". Missing class: " + e.getMessage(), e);
         }
         catch (final UnsupportedClassVersionError e)
         {
             throw new PluginParseException("Class version is incompatible with current JVM: " + clazz, e);
         }
         catch (final Throwable t)
         {
             throw new PluginParseException(t);
         }
     }
 
     /**
      * Build the complete key based on the provided plugin and module key. This method has no
      * side effects.
      * @param plugin The plugin for which the module belongs
      * @param moduleKey The key for the module
      * @return A newly constructed complete key, null if the plugin is null
      */
     private String buildCompleteKey(final Plugin plugin, final String moduleKey)
     {
         if (plugin == null)
         {
             return null;
         }
 
         final StringBuffer completeKeyBuffer = new StringBuffer(32);
         completeKeyBuffer.append(plugin.getKey()).append(":").append(moduleKey);
         return completeKeyBuffer.toString();
     }
 
     /**
      * Override this if your plugin needs to clean up when it's been removed.
      */
     public void destroy(final Plugin plugin)
     {}
 
     public boolean isEnabledByDefault()
     {
         return enabledByDefault && satisfiesMinJavaVersion();
     }
 
     public boolean isSystemModule()
     {
         return systemModule;
     }
 
     /**
      * @deprecated Since 2.2.0
      */
     @Deprecated
     public boolean isSingleton()
     {
         return singleton;
     }
 
     /**
      * @deprecated Since 2.2.0
      */
     @Deprecated
     protected boolean isSingletonByDefault()
     {
         return true;
     }
 
     /**
      * Check that the module class of this descriptor implements a given interface, or extends a given class.
      * @param requiredModuleClazz The class this module's class must implement or extend.
      * @throws PluginParseException If the module class does not implement or extend the given class.
      */
     final protected void assertModuleClassImplements(final Class<T> requiredModuleClazz) throws PluginParseException
     {
         if (!enabled)
         {
             throw new PluginParseException("Plugin module " + getKey() + " not enabled");
         }
         if (!requiredModuleClazz.isAssignableFrom(getModuleClass()))
         {
             throw new PluginParseException(
                 "Given module class: " + getModuleClass().getName() + " does not implement " + requiredModuleClazz.getName());
         }
     }
 
     public String getCompleteKey()
     {
         return completeKey;
     }
 
     public String getPluginKey()
     {
         return plugin.getKey();
     }
 
     public String getKey()
     {
         return key;
     }
 
     public String getName()
     {
         return name;
     }
 
     public Class<T> getModuleClass()
     {
         return moduleClass;
     }
 
     public abstract T getModule();
 
     public String getDescription()
     {
         return description;
     }
 
     public Map<String, String> getParams()
     {
         return params;
     }
 
     public String getI18nNameKey()
     {
         return i18nNameKey;
     }
 
     public String getDescriptionKey()
     {
         return descriptionKey;
     }
 
     public List<ResourceDescriptor> getResourceDescriptors()
     {
         return resources.getResourceDescriptors();
     }
 
     public List<ResourceDescriptor> getResourceDescriptors(final String type)
     {
         return resources.getResourceDescriptors(type);
     }
 
     public ResourceLocation getResourceLocation(final String type, final String name)
     {
         return resources.getResourceLocation(type, name);
     }
 
     public ResourceDescriptor getResourceDescriptor(final String type, final String name)
     {
         return resources.getResourceDescriptor(type, name);
     }
 
     public Float getMinJavaVersion()
     {
         return minJavaVersion;
     }
 
     public boolean satisfiesMinJavaVersion()
     {
         if (minJavaVersion != null)
         {
             return JavaVersionUtils.satisfiesMinVersion(minJavaVersion);
         }
         return true;
     }
 
     /**
      * Sets the plugin for the ModuleDescriptor
      */
     public void setPlugin(final Plugin plugin)
     {
         this.completeKey = buildCompleteKey(plugin, key);
         this.plugin = plugin;
     }
 
     /**
      * @return The plugin this module descriptor is associated with
      */
     public Plugin getPlugin()
     {
         return plugin;
     }
 
     @Override
     public String toString()
     {
         return getCompleteKey() + " (" + getDescription() + ")";
     }
 
     /**
      * Enables the descriptor by loading the module class. Classes overriding this method MUST
      * call super.enabled() before their own enabling code.
      *
      * @since 2.1.0
      */
     public void enabled()
     {
         loadClass(plugin, moduleClassName);
         enabled = true;
     }
 
     /**
      * Disables the module descriptor. Classes overriding this method MUST call super.disabled() after
      * their own disabling code.
      */
     public void disabled()
     {
         enabled = false;
         moduleClass = null;
     }
 }
