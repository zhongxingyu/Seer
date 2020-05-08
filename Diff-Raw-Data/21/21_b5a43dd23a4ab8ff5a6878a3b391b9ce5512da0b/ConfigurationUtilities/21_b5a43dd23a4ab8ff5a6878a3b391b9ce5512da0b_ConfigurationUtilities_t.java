 /*
  * Project: zlib-config
  * 
  * Copyright (C) 2010 zcarioca.net
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package net.zcarioca.zcommons.config.util;
 
 import java.io.BufferedInputStream;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.Method;
 import java.lang.reflect.Type;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Properties;
 import java.util.Set;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 
 import net.zcarioca.zcommons.config.ConfigurationProcessListener;
 import net.zcarioca.zcommons.config.ConfigurationUpdateListener;
 import net.zcarioca.zcommons.config.data.BeanPropertySetter;
 import net.zcarioca.zcommons.config.data.BeanPropertySetterFactory;
 import net.zcarioca.zcommons.config.exceptions.ConfigurationException;
 import net.zcarioca.zcommons.config.source.ConfigurationSourceIdentifier;
 import net.zcarioca.zcommons.config.source.ConfigurationSourceProvider;
 import net.zcarioca.zcommons.config.source.ConfigurationSourceProviderFactory;
 
 import org.apache.commons.collections.map.MultiValueMap;
 import org.apache.commons.io.IOUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * This singleton works as the configuration injection engine.
  * 
  * @author zcarioca
  */
 public class ConfigurationUtilities
 {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationUtilities.class);
    private static ConfigurationUtilities configUtil = new ConfigurationUtilities();
 
    private final MultiValueMap beanSourceMap;
    private final Set<ConfigurationProcessListener> processListeners;
    private final Set<ConfigurationUpdateListener> updateListeners;
 
    private PropertiesBuilderFactory propertiesBuilderFactory = new PropertiesBuilderFactory();
 
    private boolean reconfigureOnUpdateEnabled;
 
    /**
     * Gets an instance of this singleton.
     * 
     * @return Returns an instance of the singleton.
     */
    public static ConfigurationUtilities getInstance()
    {
       return configUtil;
    }
 
    /**
     * Configures the supplied bean.
     * 
     * @param bean The bean to configure.
     */
    public void configureBean(Object bean) throws ConfigurationException
    {
       configureBean(bean, false);
    }
 
    /**
     * Configures the supplied bean.
     * 
     * @param bean The bean to configure.
     * @param invokePostConstruct Whether to invoke any &#64;PostConstruct
     *        methods.
     */
    public void configureBean(Object bean, boolean invokePostConstruct) throws ConfigurationException
    {
       if (bean == null)
       {
          logger.warn("Bean is NULL");
          throw new IllegalArgumentException("The bean is NULL");
       }
       invokeStartConfig(bean);
 
       configureBeanObject(bean);
       if (invokePostConstruct)
       {
          invokePostConstruct(bean);
       }
 
       invokeCompleteConfig(bean);
    }
 
    /**
     * Forces all of the beans that inherited their configuration from the given
     * {@link ConfigurationSourceIdentifier} to be reconfigured. Messages are all
     * sent to any registered {@link ConfigurationUpdateListener}.
     * 
     * @param sourceId The {@link ConfigurationSourceIdentifier}.
     * @throws ConfigurationException if there is an error configuring any of the
     *         associated beans.
     */
    @SuppressWarnings("unchecked")
    public void runReconfiguration(ConfigurationSourceIdentifier sourceId) throws ConfigurationException
    {
       if (isReconfigureOnUpdateEnabled())
       {
          synchronized (this.beanSourceMap)
          {
             Collection<Object> beans = (Collection<Object>) this.beanSourceMap.remove(sourceId);
             for (Object bean : beans)
             {
                invokeStartUpdate(bean);
                configureBeanObject(bean);
                invokeCompleteUpdate(bean);
             }
          }
       }
    }
 
    /**
     * Gets the {@link PropertiesBuilderFactory} for configuring the system.
     * 
     * @return Returns the {@link PropertiesBuilderFactory} for this
     *         configuration.
     */
    public PropertiesBuilderFactory getPropertiesBuilderFactory()
    {
       return this.propertiesBuilderFactory;
    }
 
    /**
     * Sets the {@link PropertiesBuilderFactory} for the configuration.
     * 
     * @param propertiesBuilderFactory The {@link PropertiesBuilderFactory} to
     *        use.
     */
    public void setPropertiesBuilderFactory(PropertiesBuilderFactory propertiesBuilderFactory)
    {
       this.propertiesBuilderFactory = propertiesBuilderFactory;
    }
    
    /**
     * Reads a properties file into a {@link Properties} object.
     * 
     * @param filePath
     * @return
     * @throws ConfigurationException
     */
    public static Properties loadProperties(String filePath) throws ConfigurationException 
    {
       InputStream inputStream = null;
       try 
       {
          inputStream = new BufferedInputStream(new FileInputStream(filePath));
          
          PropertiesBuilderFactory factory = new PropertiesBuilderFactory(true, true);
          return factory.newPropertiesBuilder().readAll(inputStream).build();
       }
       catch (IOException exc) 
       {
          throw new ConfigurationException("Could not load properties from file: " + filePath, exc);
       }
       finally
       {
          if (inputStream != null) 
          {
             IOUtils.closeQuietly(inputStream);
          }
       }
    }
 
    /**
     * Loads a properties map for a class and resource location.
     * 
     * @param referenceClass The reference class.
     * @param resourceLocation The resource file to load.
     * @return Returns a properties map.
     * @throws ConfigurationException if the resource file cannot be found, read,
     *         or parsed.
     */
    public Properties loadProperties(Class<?> referenceClass, String resourceLocation) throws ConfigurationException
    {
       try
       {
          ConfigurationSourceIdentifier sourceId = new ConfigurationSourceIdentifier(referenceClass, resourceLocation);
          ConfigurationSourceProvider provider = ConfigurationSourceProviderFactory.getInstance().getConfigurationSourceProvider(sourceId);
 
          return provider.getProperties(sourceId, getPropertiesBuilderFactory());
       }
       catch (Throwable t)
       {
          throw new ConfigurationException("Could not load the properties", t);
       }
    }
 
    /**
     * Sets the properties of a bean with the given properties map;
     * 
     * @param bean The bean to configure.
     * @param properties The properties to set.
     */
    public void setProperties(Object bean, Properties properties) throws ConfigurationException
    {
       BeanPropertySetterFactory bpsFactory = new BeanPropertySetterFactory();
       Collection<BeanPropertySetter> setters = bpsFactory.getPropertySettersForBean(bean);
 
       for (BeanPropertySetter setter : setters)
       {
         setter.setProperty(properties);
       }
    }
 
    @PreDestroy
    public void invokePreDestroyAll()
    {
       ConfigurationSourceProviderFactory.getInstance().clearAssociations();
    }
 
    /**
     * Searches the bean for any methods annotated with &#64;PostConstruct for
     * immediate invocation.
     * 
     * @param bean The bean to process
     */
    void invokePostConstruct(Object bean) throws ConfigurationException
    {
       Class<?> beanClass = bean.getClass();
 
       do
       {
          for (Method method : beanClass.getDeclaredMethods())
          {
             if (method.isAnnotationPresent(PostConstruct.class))
             {
                Type[] types = method.getGenericParameterTypes();
                if (types.length == 0)
                {
                   // cannot be called on method that takes arguments
                   try
                   {
                      method.invoke(bean, (Object[]) null);
                   }
                   catch (Exception exc)
                   {
                      throw new ConfigurationException(String.format("Could not call method %s on the class %s", method.getName(), bean.getClass().getName()), exc);
                   }
                }
             }
          }
       }
       while ((beanClass = beanClass.getSuperclass()) != null);
    }
 
    /**
     * Adds a {@link ConfigurationProcessListener} to the listener queue.
     * 
     * @param listener The listener to add.
     */
    public void addConfigurationProcessListener(ConfigurationProcessListener listener)
    {
       synchronized (processListeners)
       {
          processListeners.add(listener);
       }
    }
 
    /**
     * Adds a {@link ConfigurationUpdateListener} to the listener queue.
     * 
     * @param listener The listener to add.
     */
    public void addConfigurationUpdateListener(ConfigurationUpdateListener listener)
    {
       synchronized (updateListeners)
       {
          updateListeners.add(listener);
       }
    }
 
    /**
     * Removes a {@link ConfigurationProcessListener} from the listener queue.
     * 
     * @param listener The listener to remove.
     * @return Returns true if the listener was removed, false otherwise.
     */
    public boolean removeConfigurationProcessListener(ConfigurationProcessListener listener)
    {
       synchronized (processListeners)
       {
          return this.processListeners.remove(listener);
       }
    }
 
    /**
     * Removes a {@link ConfigurationUpdateListener} from the listener queue.
     * 
     * @param listener The listener to remove.
     * @return Returns true if the listener was removed, false otherwise.
     */
    public boolean removeConfigurationUpdateListener(ConfigurationUpdateListener listener)
    {
       synchronized (updateListeners)
       {
          return this.updateListeners.remove(listener);
       }
    }
 
    /**
     * Gets a collection of configuration identifiers that have already been
     * used.
     * 
     * @return Returns a collection of {@link ConfigurationSourceIdentifier} that
     *         have already been loaded.
     */
    @SuppressWarnings("unchecked")
    public Collection<ConfigurationSourceIdentifier> getConfiguredSourceIdentifiers()
    {
       return this.beanSourceMap.keySet();
    }
 
    public void setReconfigureOnUpdateEnabled(boolean reconfigureOnUpdateEnabled)
    {
       this.reconfigureOnUpdateEnabled = reconfigureOnUpdateEnabled;
    }
 
    public boolean isReconfigureOnUpdateEnabled()
    {
       return this.reconfigureOnUpdateEnabled;
    }
 
    private void configureBeanObject(Object bean) throws ConfigurationException
    {
       ConfigurationSourceIdentifier sourceId = new ConfigurationSourceIdentifier(bean);
       ConfigurationSourceProvider provider = ConfigurationSourceProviderFactory.getInstance().getConfigurationSourceProvider(sourceId);
 
       if (logger.isDebugEnabled())
       {
          logger.debug(String.format("Bean %s of type %s, has the source id %s", bean, bean.getClass(), sourceId));
          logger.debug(String.format("Source ID %s being processed by provider type %s", sourceId, provider.getProviderID()));
       }
 
       Properties props = provider.getProperties(sourceId, getPropertiesBuilderFactory());
       setProperties(bean, props);
       beanSourceMap.put(sourceId, bean);
    }
 
    private void invokeStartConfig(Object bean)
    {
       if (logger.isDebugEnabled())
          logger.debug(String.format("Configuration of bean '%s' started", bean.toString()));
 
       synchronized (this.processListeners)
       {
          for (ConfigurationProcessListener listener : this.processListeners)
          {
             listener.startingConfiguration(bean);
          }
       }
    }
 
    private void invokeCompleteConfig(Object bean)
    {
       if (logger.isDebugEnabled())
          logger.debug(String.format("Configuration of bean '%s' completed", bean.toString()));
 
       synchronized (this.processListeners)
       {
          for (ConfigurationProcessListener listener : this.processListeners)
          {
             listener.completedConfiguration(bean);
          }
       }
    }
 
    private void invokeStartUpdate(Object bean)
    {
       if (logger.isDebugEnabled())
          logger.debug(String.format("Update of bean '%s' started", bean.toString()));
 
       synchronized (updateListeners)
       {
          for (ConfigurationUpdateListener listener : updateListeners)
          {
             listener.startingBeanUpdate(bean);
          }
       }
    }
 
    private void invokeCompleteUpdate(Object bean)
    {
       if (logger.isDebugEnabled())
          logger.debug(String.format("Update of bean '%s' completed", bean.toString()));
 
       synchronized (updateListeners)
       {
          for (ConfigurationUpdateListener listener : updateListeners)
          {
             listener.completedBeanUpdate(bean);
          }
       }
    }
 
    static void resetConfigurationUtilities()
    {
       configUtil = new ConfigurationUtilities();
    }
 
    protected ConfigurationUtilities()
    {
       this.processListeners = new HashSet<ConfigurationProcessListener>();
       this.updateListeners = new HashSet<ConfigurationUpdateListener>();
       this.beanSourceMap = new MultiValueMap();
    }
 }
