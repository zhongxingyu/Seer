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
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.zcarioca.zcommons.config.Environment;
 import net.zcarioca.zcommons.config.EnvironmentAccessor;
 
 import org.apache.commons.lang.StringUtils;
 
 /**
  * <p>
  * Works as a simple builder used to create properties objects. This class
  * allows for keyword substitution so that if there is a property defined as "
  * <code>my.greeting=Hello</code>" and a property defined as "
  * <code>my.message=${my.greeting} World<code>", the result of calling "<code>prop.getProperty("my.message");</code>
  * " will return the string "Hello World".
  * </p>
  * <p>
  * Properties are overridden by the order in which they are added. If you would
  * like to add a system property and allow that property to be overridden, then
  * you would need to add the system property first, followed by the overridden
  * property and value, if necessary.
  * </p>
  * <p>
  * Note: this object is not thread-safe.
  * </p>
  * 
  * @author zcarioca
  */
 public class PropertiesBuilder
 {
    private static final Pattern pattern = Pattern.compile("\\$\\{[^\\s]+\\}");
 
    private final Map<String, String> props;
    private Environment environment;
    private boolean generated;
 
    /**
     * Constructor for the PropertiesBuilder.
     */
    public PropertiesBuilder()
    {
       this.environment = EnvironmentAccessor.getInstance().getEnvironment();
       this.props = new HashMap<String, String>();
       this.generated = false;
    }
 
    Environment getEnvironment()
    {
       return environment;
    }
 
    void setEnvironment(Environment environment)
    {
       this.environment = environment;
    }
 
    /**
     * Adds an environment property to the builder. If the property doesn't
     * exist, a blank value will be added.
     * 
     * @param propertyName The name of the environment property.
     * @return Returns the builder.
     * @throws IllegalArgumentException if the property name is blank or null.
     * @throws IllegalStateException if the {@link PropertiesBuilder#build()}
     *         method has already been called on this instance.
     */
    public PropertiesBuilder addEnvironmentProperty(String propertyName)
    {
       return addEnvironmentProperty(propertyName, null);
    }
 
    /**
     * Adds an environment property to the builder, or the default value if the
     * property doesn't exist.
     * 
     * @param propertyName The name of the environment property.
     * @param defaultValue The default value.
     * @return Returns the builder.
     * @throws IllegalArgumentException if the property name is blank or null.
     * @throws IllegalStateException if the {@link PropertiesBuilder#build()}
     *         method has already been called on this instance.
     */
    public PropertiesBuilder addEnvironmentProperty(String propertyName, String defaultValue)
    {
       String value = environment.getEnvVariable(propertyName, defaultValue);
       return addProperty(propertyName, value);
    }
 
    /**
     * Adds a system property to the builder. If the property doesn't exist, a
     * blank value will be added.
     * 
     * @param propertyName The name of the system property.
     * @return Returns the builder.
     * @throws IllegalArgumentException if the property name is blank or null.
     * @throws IllegalStateException if the {@link PropertiesBuilder#build()}
     *         method has already been called on this instance.
     */
    public PropertiesBuilder addSystemProperty(String propertyName)
    {
       return addSystemProperty(propertyName, null);
    }
 
    /**
     * Adds a system property to the builder, or the default value if the
     * property doesn't exist.
     * 
     * @param propertyName The name of the system property.
     * @param defaultValue The default value.
     * @return Returns the builder.
     * @throws IllegalArgumentException if the property name is blank or null.
     * @throws IllegalStateException if the {@link PropertiesBuilder#build()}
     *         method has already been called on this instance.
     */
    public PropertiesBuilder addSystemProperty(String propertyName, String defaultValue)
    {
       if (StringUtils.isEmpty(propertyName))
       {
          throw new IllegalArgumentException("Empty or NULL property name");
       }
       return addProperty(propertyName, environment.getSystemProperty(propertyName, defaultValue));
    }
 
    /**
     * Adds a new property to the builder. If the value is null, an empty string
     * will be added.
     * 
     * @param propertyName The property name.
     * @param propertyValue The property value (may be null).
     * @return Returns the builder.
     * @throws IllegalArgumentException if the property name is blank or null.
     * @throws IllegalStateException if the {@link PropertiesBuilder#build()}
     *         method has already been called on this instance.
     */
    public PropertiesBuilder addProperty(String propertyName, String propertyValue)
    {
       if (generated)
       {
          throw new IllegalStateException(
                "The properties object for this builder has already been built. A separate builder is needed for each properties object.");
       }
       if (StringUtils.isEmpty(propertyName))
       {
          throw new IllegalArgumentException("Empty or NULL property name");
       }
       if (propertyValue == null)
       {
          propertyValue = "";
       }
       this.props.put(propertyName, propertyValue);
       return this;
    }
 
    /**
     * Adds all of the environment properties to the builder.
     * 
     * @return Returns the builder.
     * @throws IllegalArgumentException if any of the keys are null or blank
     *         strings.
     * @throws IllegalStateException if the {@link PropertiesBuilder#build()}
     *         method has already been called on this instance.
     */
    public PropertiesBuilder addAllEnvironmentProperties()
    {
       return addAll(environment.getAllEnvProperties());
    }
 
    /**
     * Adds all of the system properties to the builder.
     * 
     * @return Returns the builder.
     * @throws IllegalArgumentException if any of the keys are null or blank
     *         strings.
     * @throws IllegalStateException if the {@link PropertiesBuilder#build()}
     *         method has already been called on this instance.
     */
    public PropertiesBuilder addAllSystemProperties()
    {
       return addAll(environment.getAllSystemProperties());
    }
 
    /**
     * Adds all of the entries in the map to the builder. If any values are null,
     * an empty string will be added in its place.
     * 
     * @param props The map of properties.
     * @return Returns the builder.
     * @throws IllegalArgumentException if any of the keys are null or blank
     *         strings.
     * @throws IllegalStateException if the {@link PropertiesBuilder#build()}
     *         method has already been called on this instance.
     */
    @SuppressWarnings("rawtypes")
    public PropertiesBuilder addAll(Map props)
    {
       for (Object key : props.keySet())
       {
          Object val = props.get(key);
          this.addProperty(key.toString(), val != null ? val.toString() : null);
       }
       return this;
    }
    
    /**
     * Reads the properties from the input stream.
     * 
     * @param inputStream The input stream.
     * @return Returns the builder.
     * 
     * @throws IOException if an error occurred when reading from the input
     *         stream.
     */
    public PropertiesBuilder readAll(InputStream inputStream) throws IOException
    {
      if (inputStream != null)
       {
         Properties props = new Properties();
         props.load(inputStream);
         addAll(props);
       }
       return this;
    }
 
    /**
     * Builds the {@link Properties} object.
     * 
     * @return Returns a new {@link Properties} object.
     * @throws IllegalStateException if this method has been called more than
     *         once.
     */
    public Properties build()
    {
       if (this.generated)
       {
          throw new IllegalStateException("The build() method has already been called on this instance.");
       }
       this.generated = true;
       Properties newProps = new Properties();
 
       for (String key : this.props.keySet())
       {
          newProps.setProperty(key, getFilteredValue(key));
       }
 
       return newProps;
    }
 
    /**
     * Returns the number of properties currently within this builder.
     * 
     * @return Returns the number of properties currently in the builder.
     * @throws IllegalStateException if the {@link PropertiesBuilder#build()}
     *         method has already been called on this instance.
     */
    int size()
    {
       if (generated)
       {
          throw new IllegalStateException(
                "The properties object for this builder has already been built. A separate builder is needed for each properties object.");
       }
       return this.props.size();
    }
 
    /**
     * Returns the property value for given property name currently within this
     * builder.
     * 
     * @param propertyName The property name.
     * @return Returns the unfiltered property value, may be null.
     * @throws IllegalStateException if the {@link PropertiesBuilder#build()}
     *         method has already been called on this instance.
     */
    String getProperty(String propertyName)
    {
       if (generated)
       {
          throw new IllegalStateException(
                "The properties object for this builder has already been built. A separate builder is needed for each properties object.");
       }
       return this.props.get(propertyName);
    }
 
    String getFilteredValue(String key)
    {
       String value = this.props.get(key);
       if (StringUtils.isEmpty(value))
       {
          return "";
       }
       Matcher matcher = pattern.matcher(value);
       while (matcher.find())
       {
          String grouping = matcher.group();
          String valKey = grouping.substring(2, grouping.length() - 1);
 
          String prop = getFilteredValue(valKey);
          if (!StringUtils.isEmpty(prop))
          {
             value = matcher.replaceFirst(prop);
             matcher = pattern.matcher(value);
          }
       }
       return value;
    }
 }
