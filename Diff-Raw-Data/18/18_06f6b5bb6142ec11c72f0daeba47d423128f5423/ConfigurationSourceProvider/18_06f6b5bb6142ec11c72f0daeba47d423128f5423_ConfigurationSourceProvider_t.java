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
 package net.zcarioca.zcommons.config.source;
 
 import java.util.Properties;
 
 import net.zcarioca.zcommons.config.exceptions.ConfigurationException;
 import net.zcarioca.zcommons.config.source.spi.DefaultConfigSourceServiceProvider;
 import net.zcarioca.zcommons.config.util.PropertiesBuilderFactory;
 
 /**
  * An interface that defines the configuration source service. This interface
  * should be implemented by a service class provider.
  * 
  * @author zcarioca
  */
 public interface ConfigurationSourceProvider
 {
    /**
     * Defines the priority with which a {@link ConfigurationSourceProvider} will
     * be used to configure a POJO with a given {@link ConfigurationSourceIdentifier}.  
     * Providers with lower priority will only be used if a provider with a higher priority 
     * cannot be found to support the particular identifier.
     * 
     * The lowest priority provider is the {@link DefaultConfigSourceServiceProvider}.
     */
    public static enum Priority
    {
       /** 
        * Should only be used by {@link DefaultConfigSourceServiceProvider}, this is the lowest
        * possible priority.
        */
       BACKUP,
       
       /** A low priority provider can be overridden easily. */
       LOW,
       
       /** A medium priority provider. */ 
       MEDIUM,
       
       /** A high priority provider. */
       HIGH,
       
       /** The highest priority provider, will override any provider that is not also marked as OVERRIDE. */
       OVERRIDE
    }
    
    /**
     * This is the Provider ID, which is used to determine which Provider to use.
     * This ID should be unique; as such, it is recommended that custom providers
     * include the package name in the ID.
     * 
     * @return Returns a unique Provider ID.
     */
    public String getProviderID();
    
    /**
     * Gets the provider's priority level. 
     * 
     * @return Returns the provider's priority level.
     * 
     * @see Priority
     */
    public Priority getPriorityLevel();
 
    /**
     * Runs this method immediately after initializing this class.
     */
    public void postInit();
 
    /**
     * Runs this method immediately before destroying this class. Note: this
     * method is not guaranteed to run.
     */
    public void preDestroy();
 
    /**
     * Uses the {@link ConfigurationSourceIdentifier} to locate and parse the
     * data into a properties map.
     * 
     * @param configurationSourceIdentifier The source of the configuration data.
     * 
     * @param propertiesBuilderFactory The properties builder factory used to
     *        create the properties object.
     * 
    * @return Returns a {@link Properties} object.
     */
    public Properties getProperties(ConfigurationSourceIdentifier configurationSourceIdentifier, PropertiesBuilderFactory propertiesBuilderFactory)
          throws ConfigurationException;
 
    /**
     * Determines whether this source provider will support the configuration
     * source identifier.
     * 
     * @param configurationSourceIdentifier The source of the configuration data.
     * @return Returns true if this provider supports the provided identifier.
     */
    public boolean supportsIdentifier(ConfigurationSourceIdentifier configurationSourceIdentifier);
 }
