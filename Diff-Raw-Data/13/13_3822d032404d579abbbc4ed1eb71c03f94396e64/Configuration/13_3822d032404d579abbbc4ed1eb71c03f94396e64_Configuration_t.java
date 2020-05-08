 /*
  * Copyright 2012 Luis Rodero-Merino.
  * 
  * This file is part of Onodrim.
  * 
  * Onodrim is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * Onodrim is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with Onodrim.  If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 
 package org.onodrim;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.lang.reflect.Array;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Each instance of this class contains the configuration parameters of the
  * job/task/experiment/whatever-the-user-requires to execute. For example, when using
  * Onodrim's own functionality to run experiments (through {@link Job} instances, grouped in
  * a single {@link JobsSet}), each {@link Job} will be created with the {@link Configuration}
  * of the task to run.
  * 
  * It extends the {@link <a href="http://docs.oracle.com/javase/6/docs/api/java/util/Properties.html">java.util.Properties</a>}
  * class by adding several methods that ease the reading of parameters. In general,
  * what we call here <i>parameter</i> it is denoted <i>property</i> in parent class Properties.
  * 
  * Also, it contains the {@code static} methods that allow to generate a
  * collection of configurations from a single one.
  * 
  * @author Luis Rodero-Merino
  * @since 1.0
  */
 public class Configuration extends Properties {
     
     // TODO: Unify notation, now code refers to 'properties' and 'parameters' and is confusing. Use only 'parameters' 
 
     private static final long serialVersionUID = 1L;
     // How configurations must be read can be set in the properties object sent
     private static final String ONODRIM_CONF_PROPERTIES_HEADER = Onodrim.PROJECT_NAME;
     private static final String DEFAULT_PARAMETERS_SEPARATOR = ";";
     public static final String PARAMETERS_SEPARATOR_PROPERTY_NAME = ONODRIM_CONF_PROPERTIES_HEADER + ".valuesSeparator";
     private static final int DEFAULT_REPETITIONS_PER_CONF = 1;
     public static final String REPETITIONS_PER_CONF_PROPERTY_NAME = ONODRIM_CONF_PROPERTIES_HEADER + ".repetitions";
     public static final String GROUP_CONFS_BY_PROPERTY_NAME = ONODRIM_CONF_PROPERTIES_HEADER + ".groupConfsBy";
     public static final String PACKETS_PROPERTY_NAME = ONODRIM_CONF_PROPERTIES_HEADER + ".packets";
     public static final String PACKETS_TO_IGNORE_PROPERTY_NAME = ONODRIM_CONF_PROPERTIES_HEADER + ".packetsToIgnore";
     public static final String GENERATE_PARAMETERS_VALUES_CONDITION_PROPERTY_NAME = ONODRIM_CONF_PROPERTIES_HEADER + ".generateParametersValuesCondition";
     private static final String PARAMETER_CONDITIONS_SEPARATOR = ":";
     public static final String PARAMETERS_BINDING_PROPERTY_NAME = ONODRIM_CONF_PROPERTIES_HEADER + ".boundParameters";
     private static final String BINDING_SEPARATOR = ":";
 
 
     /* TODO: Probably it would be more coherent to define also 'bindings' and 'generation conditions'
      * with patterns */
     /**
      * Parameters can be defined by ranges, e.g. Param1=[0:5:20] would be read
      * as Param1=0;5;10;15;20. This is the regular expression that we use to
      * recognize range definitions.
      */
     private static final Pattern rangeDefPattern = Pattern.compile("\\[\\s*[^;]+\\s*\\:\\s*[^;]+\\s*\\:\\s*[^;]+\\s*\\]");
 
     /**
      * When a parameter value is a list (array), then its definition must start by the
      * String defined by this variable.
      */
     private static final String LIST_OPENING = "{";
     /**
      * When a parameter value is a list (array), then its definition must finish by the
      * String defined by this variable.
      */
     private static final String LIST_CLOSING = "}";
     /**
      * When a parameter value is a list (array), its items must be separated by this variable.
      */
     private static final String LIST_SEPARATOR = ",";
     
     /**
      * Name of the 'default' packet, which is assigned to those configurations that are not assigned
      * to any other packet by the user (if no packet is defined by the user, then all configurations
      * will below to this default packet). 
      */
     public static final String DEFAULT_PACKET_NAME = "ONODRIM_DEFAULT_PACKET";
     
     /**
      * If there are several packets it could be that the user does not want the default packet to be run, only the
      * ones she has defined. In such case this flag will be false.
      */
     public static final boolean RUN_DEFAULT_PACKET = true;
     
     public static final String PACKET_AND_PARAM_NAMES_SEPARATOR = ".";
     
     /**
      * Each configuration can be defined as being part of a 'packet'. The idea is that users can differentiate, for different
      * configurations, how they should be processed depending on the packet they belong to. Default packet is 
      */
     private String packetName = DEFAULT_PACKET_NAME;
     
     /**
      * Create new empty configuration, only used internally by
      * {@link #generateConfigurations(Map)}, to create the new instance right
      * before copying inside the elements in the input map.
      */
     private Configuration() {
         super();
     }
 
     /**
      * Create new configuration and insert into it all the name-value pairs
      * included in the {@link <a href="http://docs.oracle.com/javase/6/docs/api/java/util/Properties.html">java.util.Properties</a>} instance.
      * 
      * @param properties
      *            All the elements defined here (name and value) are copied into
      *            the new instance.
      */
     public Configuration(Properties properties) {
         for (String key : properties.stringPropertyNames())
             super.setProperty((String) key, properties.getProperty((String) key));
     }
     
     /**
      * Set to which packet the parameters of this configuration belong to.
      * @param packetName
      */
     protected void setPacketName(String packetName) {
         this.packetName = packetName;
     }
     
     /**
      * Each configuration can be defined as being part of a 'packet'. The idea is that users can differenciate, for different
      * configurations, how they should be processed depending on the packet they belong to. This method returns to which
      * packet the conf belongs to. If not defined, the configuration belongs to a default packet called 
      * 
      * @return The packet the parameters belong to.
      */
     public String getPacketName() {
         return packetName;
     }
 
     /**
      * Get the names of all parameters, in fact this function just calls parent
      * class method {@link #stringPropertyNames()}.
      * 
      * @return A {@link java.util.Set} instance containing the names of all the
      *         parameters defined in this configuration.
      */
     public Set<String> getParameterNames() {
         return super.stringPropertyNames();
     }
 
     /**
      * Check whether some configuration parameter is defined, in fact this
      * function just call parent class method {@link #containsKey(Object)}.
      * 
      * @param parameterName
      *            Parameter name.
      * @return {@code true} if the parameter is defined in this configuration
      *         instance, {@code false} otherwise.
      */
     public boolean parameterDefined(String parameterName) {
         return super.containsKey(parameterName);
     }
 
     // TODO: Change name of getParameter to getValue
     /**
      * Get the value of some parameter if it is defined. If not, return
      * {@code null}.
      * 
      * @param parameterName
      *            Name of the parameter whose value is required.
      * @return The value of the parameter as {@link String} after applying
      *         {@link String#trim()}; {@code null} if the parameter was not
      *         defined.
      * @throws IllegalArgumentException
      *             if the parameter name is {@code null}.
      */
     public String getParameter(String parameterName) {
         if (parameterName == null)
             throw new IllegalArgumentException("The name of the parameter to get the value of cannot be null");
         String parameterValue = super.getProperty(parameterName);
         if (parameterValue == null)
             return null;
         return parameterValue.trim();
     }
 
     /**
      * Get the value of some configuration parameter, if it is defined. If not
      * return the default value.
      * 
      * @param parameterName
      *            Name of the parameter whose value is required.
      * @param defaultValue
      *            Value to return in case this parameter is not defined in this
      *            configuration.
      * @return The value of the parameter as {@link String} after applying
      *         {@link String#trim()}; or if the parameter was not defined.
      * @throws IllegalArgumentException
      *             if the parameter name is {@code null}.
      */
     public String getParameter(String parameterName, String defaultValue) {
         if (parameterName == null)
             throw new IllegalArgumentException("The name of the parameter to get the value of cannot be null");
         String parameterValue = super.getProperty(parameterName);
         if (parameterValue == null)
             return defaultValue;
         return parameterValue.trim();
     }
 
     /**
      * Get the value of the parameter as instance of a particular class. This
      * method basically wraps a call to
      * {@link #getParameter(String, Class, Object)} method, where the default
      * value is {@code null}.
      * 
      * @param parameterName
      *            Name of the parameter whose value is required.
      * @param valueClass
      *            Class to cast the value to.
      * @return The parameter value, or {@code null} if the parameter is not
      *         defined.
      * @throws ConfigurationException
      *             if the parameter value could not be 'transformed' into an
      *             instance of the class.
      */
     public <T> T getParameter(String parameterName, Class<T> valueClass) throws ConfigurationException {
         return getParameter(parameterName, valueClass, null);
     }
 
     /**
      * Get the value of the parameter as instance of a particular class, if it
      * is defined. If not return the default value.
      * 
      * @param parameterName
      *            Name of the parameter whose value is required.
      * @param valueClass
      *            Class to cast the value to.
      * @param defaultValue
      * @return The parameter value, or the default value if the parameter is not
      *         defined.
      * @throws ConfigurationException
      *             if the parameter value could not be 'transformed' into an
      *             instance of the class.
      */
     public <T> T getParameter(String parameterName, Class<T> valueClass, T defaultValue) throws ConfigurationException {
         String parameterValue = getParameter(parameterName);
         if (parameterValue == null)
             return defaultValue;
         return parseValue(parameterValue, valueClass);
     }
 
     /**
      * Get parameter value as {@code boolean}, if it is defined. If not return
      * the default value.
      * 
      * @param parameterName
      *            Name of the parameter whose value is required.
      * @param defaultValue
      *            Value to return if the parameter is not defined.
      * @return The value of the parameter if defined. If not, the default value.
      * @throws ConfigurationException
      *             if the parameter value could not be parsed to {@code boolean}
      *             using {@link Boolean#parseBoolean(String)}.
      */
     public boolean getBooleanParameter(String parameterName, boolean defaultValue) throws ConfigurationException {
         return getParameter(parameterName, Boolean.class, defaultValue);
     }
     
     /** 
      * Get parameter value as {@code Boolean}, if it is defined. If not, return {@code null}.
      * @param parameterName
      *            Name of the parameter whose value is required.
      * @return The value of the parameter if defined. If not, {@code null}.
      * @throws ConfigurationException
      *             if the parameter value could not be parsed to {@code Boolean}
      *             using {@link Boolean#parseBoolean(String)}.
      */
     public Boolean getBooleanParameter(String parameterName) throws ConfigurationException {
     	return getParameter(parameterName, Boolean.class, null);
     }
 
     /**
      * Get parameter value as {@code byte}, if it is defined. If not return the
      * default value.
      * 
      * @param parameterName
      *            Name of the parameter whose value is required.
      * @param defaultValue
      *            Value to return if the parameter is not defined.
      * @return The value of the parameter if defined. If not, the default value.
      * @throws ConfigurationException
      *             if the parameter value could not be parsed to {@code byte}
      *             using {@link Byte#parseByte(String)}.
      */
     public byte getByteParameter(String parameterName, byte defaultValue)
             throws ConfigurationException {
         return getParameter(parameterName, Byte.class, defaultValue);
     }
     
     /** 
      * Get parameter value as {@code Byte}, if it is defined. If not, return {@code null}.
      * @param parameterName
      *            Name of the parameter whose value is required.
      * @return The value of the parameter if defined. If not, {@code null}.
      * @throws ConfigurationException
      *             if the parameter value could not be parsed to {@code Boolean}
      *             using {@link Byte#parseByte(String)}.
      */
     public Byte getByteParameter(String parameterName) throws ConfigurationException {
     	return getParameter(parameterName, Byte.class, null);
     }
 
     /**
      * Get parameter value as {@code short}, if it is defined. If not return the
      * default value.
      * 
      * @param parameterName
      *            Name of the parameter whose value is required.
      * @param defaultValue
      *            Value to return if the parameter is not defined.
      * @return The value of the parameter if defined. If not, the default value.
      * @throws ConfigurationException
      *             if the parameter value could not be parsed to {@code short}
      *             using {@link Short#parseShort(String)}.
      */
     public short getShortParameter(String parameterName, short defaultValue) throws ConfigurationException {
         return getParameter(parameterName, Short.class, defaultValue);
     }
     
     /** 
      * Get parameter value as {@code Short}, if it is defined. If not, return {@code null}.
      * @param parameterName
      *            Name of the parameter whose value is required.
      * @return The value of the parameter if defined. If not, {@code null}.
      * @throws ConfigurationException
      *             if the parameter value could not be parsed to {@code Short}
      *             using {@link Short#parseShort(String)}.
      */
     public Short getShortParameter(String parameterName) throws ConfigurationException {
     	return getParameter(parameterName, Short.class, null);
     }
 
     /**
      * Get parameter value as {@code int}, if it is defined. If not return the
      * default value.
      * 
      * @param parameterName
      *            Name of the parameter whose value is required.
      * @param defaultValue
      *            Value to return if the parameter is not defined.
      * @return The value of the parameter if defined. If not, the default value.
      * @throws ConfigurationException
      *             if the parameter value could not be parsed to {@code int}
      *             using {@link Integer#parseInt(String)}.
      */
     public int getIntParameter(String parameterName, int defaultValue) throws ConfigurationException {
         return getParameter(parameterName, Integer.class, defaultValue);
     }
     
     /** 
      * Get parameter value as {@code Integer}, if it is defined. If not, return {@code null}.
      * @param parameterName
      *            Name of the parameter whose value is required.
      * @return The value of the parameter if defined. If not, {@code null}.
      * @throws ConfigurationException
      *             if the parameter value could not be parsed to {@code Integer}
      *             using {@link Integer#parseInt(String)}.
      */
     public Integer getIntParameter(String parameterName) throws ConfigurationException {
     	return getParameter(parameterName, Integer.class, null);
     }
 
     /**
      * Get parameter value as {@code long}, if it is defined. If not return the
      * default value.
      * 
      * @param parameterName
      *            Name of the parameter whose value is required.
      * @param defaultValue
      *            Value to return if the parameter is not defined.
      * @return The value of the parameter if defined. If not, the default value.
      * @throws ConfigurationException
      *             if the parameter value could not be parsed to {@code long}
      *             using {@link Long#parseLong(String)}.
      */
     public long getLongParameter(String parameterName, long defaultValue) throws ConfigurationException {
         return getParameter(parameterName, Long.class, defaultValue);
     }
     
     /** 
      * Get parameter value as {@code Long}, if it is defined. If not, return {@code null}.
      * @param parameterName
      *            Name of the parameter whose value is required.
      * @return The value of the parameter if defined. If not, {@code null}.
      * @throws ConfigurationException
      *             if the parameter value could not be parsed to {@code Long}
      *             using {@link Long#parseLong(String)}.
      */
     public Long getLongParameter(String parameterName) throws ConfigurationException {
     	return getParameter(parameterName, Long.class, null);
     }
 
     /**
      * Get parameter value as {@code float}, if it is defined. If not return the
      * default value.
      * 
      * @param parameterName
      *            Name of the parameter whose value is required.
      * @param defaultValue
      *            Value to return if the parameter is not defined.
      * @return The value of the parameter if defined. If not, the default value.
      * @throws ConfigurationException
      *             if the parameter value could not be parsed to {@code float}
      *             using {@link Float#parseFloat(String)}.
      */
     public double getFloatParameter(String parameterName, float defaultValue) throws ConfigurationException {
         return getParameter(parameterName, Float.class, defaultValue);
     }
     
     /** 
      * Get parameter value as {@code Float}, if it is defined. If not, return {@code null}.
      * @param parameterName
      *            Name of the parameter whose value is required.
      * @return The value of the parameter if defined. If not, {@code null}.
      * @throws ConfigurationException
      *             if the parameter value could not be parsed to {@code Float}
      *             using {@link Float#parseFloat(String)}.
      */
     public Float getFloatParameter(String parameterName) throws ConfigurationException {
     	return getParameter(parameterName, Float.class, null);
     }
 
     /**
      * Get parameter value as {@code double}, if it is defined. If not return
      * the default value.
      * 
      * @param parameterName
      *            Name of the parameter whose value is required.
      * @param defaultValue
      *            Value to return if the parameter is not defined.
      * @return The value of the parameter if defined. If not, the default value.
      * @throws ConfigurationException
      *             if the parameter value could not be parsed to {@code double}
      *             using {@link Double#parseDouble(String)}.
      */
     public double getDoubleParameter(String parameterName, double defaultValue) throws ConfigurationException {
         return getParameter(parameterName, Double.class, defaultValue);
     }
     
     /** 
      * Get parameter value as {@code Double}, if it is defined. If not, return {@code null}.
      * @param parameterName
      *            Name of the parameter whose value is required.
      * @return The value of the parameter if defined. If not, {@code null}.
      * @throws ConfigurationException
      *             if the parameter value could not be parsed to {@code Double}
      *             using {@link Double#parseDouble(String)}.
      */
     public Double getDoubleParameter(String parameterName) throws ConfigurationException {
     	return getParameter(parameterName, Double.class, null);
     }
 
     /**
      * Get the parameter value as a {@link java.util.List} whose items are
      * instances of the class, if it is defined. If not, return the default
      * value. For example, if this definition is included in the configuration:<br>
      * {@code mylist.example= $&#123 1,2,3 $&#125 }
      * <p>
      * then a call to
      * {@code getListParameter("mylist.example", Integer.class, null)} will
      * return a {@link java.util.List} of {@link Integer} instances whose values
      * will be {@code 1}, {@code 2} and {@code 3}.
      * 
      * @param parameterName
      *            Name of the parameter whose value is required.
      * @param valuesClass
      *            The items in the list returned must be instances of this
      *            class.
      * @param defaultValue
      *            Value to return if the parameter is not defined.
      * @return The value of the parameter if defined. If not, the default value.
      * @throws ConfigurationException
      *             if the parameter value could not be parsed to a list, or some
      *             of its items could not be casted to the class.
      * @throws IllegalArgumentException
      *             if the parameter name or the values class is {@code null}.
      */
     public <T> List<T> getListParameter(String parameterName, Class<T> valuesClass, List<T> defaultValue)
             throws ConfigurationException {
 
         if (valuesClass == null)
             throw new IllegalArgumentException("It is needed to know the class type of the elements in the list of the parameter '"
                                             + parameterName + "' to build it");
         String parameterValue = getParameter(parameterName);
         if (parameterValue == null) // If not found in configuration, then return default value
             return defaultValue;
         if (parameterValue.isEmpty()) // If found but with not value associated, then return empty list
             return new ArrayList<T>();
 
         String tokensString = parameterValue.trim();
 
         if (!tokensString.startsWith(LIST_OPENING))
             throw new ConfigurationException("Parameter '" + parameterName
                                             + "' should start by '" + LIST_OPENING
                                             + "' to be able to parse it as a List");
         if (!tokensString.endsWith(LIST_CLOSING))
             throw new ConfigurationException("Parameter '" + parameterName
                                             + "' should finish by '" + LIST_CLOSING
                                             + "' to be able to parse it as a List");
 
         tokensString = tokensString.substring(1, tokensString.length() - 1).trim();
 
         List<T> valuesList = new ArrayList<T>();
         if (tokensString.isEmpty())
             return valuesList;
 
         String[] valuesArray = tokensString.split(LIST_SEPARATOR);
 
         for (String value : valuesArray)
             valuesList.add(parseValue(value.trim(), valuesClass));
 
         return valuesList;
     }
 
     /**
      * Get the parameter value as an array whose items are instances of the
      * class, if it is defined. If not, return the default value. In fact this
      * method calls to {@link #getListParameter(String, Class, List)} and then
      * passes the list elements to an array using
      * {@link java.util.List#toArray(Object[])}. For example, if this definition
      * is included in the configuration:<br>
      * {@code myarray.example=} {{@code 1,2,3}}
      * <p>
      * then a call to
      * {@code getArrayParameter("myarray.example", Integer.class, null)} will
      * return an array of {@link Integer} instances whose values will be
      * {@code 1}, {@code 2} and {@code 3}.
      * 
      * @param parameterName
      *            Name of the parameter whose value is required.
      * @param valuesClass
      *            The items in the array returned must be instances of this
      *            class.
      * @param defaultValue
      *            Value to return if the parameter is not defined.
      * @return The value of the parameter if defined. If not, the default value.
      * @throws ConfigurationException
      *             if the parameter value could not be parsed to an array, or
      *             some of its items could not be casted to the class.
      * @throws IllegalArgumentException
      *             if the parameter name or the values class are {@code null}.
      */
     @SuppressWarnings("unchecked")
     // The casting in (T[])Array.newInstance() causes a warning that we can
     // safely ignore
     public <T> T[] getArrayParameter(String parameterName, Class<T> valuesClass, T[] defaultValue)
                     throws ConfigurationException {
 
         if (valuesClass == null)
             throw new IllegalArgumentException("It is needed to know the class type of the elements in the array of the parameter '"
                                                 + parameterName + "' to build it");
         String parameterValue = getParameter(parameterName);
         if (parameterValue == null) // If not found in configuration, then return default value
             return defaultValue;
         if (parameterValue.isEmpty()) // If found but with not value associated, then return empty array
             return (T[]) Array.newInstance(valuesClass, 0);
 
         List<T> asList = getListParameter(parameterName, valuesClass, (defaultValue != null ? Arrays.asList(defaultValue) : null));
 
         if (valuesClass.isPrimitive()) {
             if (valuesClass.equals(Character.TYPE))
                 return (T[]) asList.toArray(new Character[0]);
             if (valuesClass.equals(Boolean.TYPE))
                 return (T[]) asList.toArray(new Boolean[0]);
             if (valuesClass.equals(Byte.TYPE))
                 return (T[]) asList.toArray(new Byte[0]);
             if (valuesClass.equals(Short.TYPE))
                 return (T[]) asList.toArray(new Short[0]);
             if (valuesClass.equals(Integer.TYPE))
                 return (T[]) asList.toArray(new Integer[0]);
             if (valuesClass.equals(Long.TYPE))
                 return (T[]) asList.toArray(new Long[0]);
             if (valuesClass.equals(Float.TYPE))
                 return (T[]) asList.toArray(new Float[0]);
             if (valuesClass.equals(Double.TYPE))
                 return (T[]) asList.toArray(new Double[0]);
         }
 
         return asList != null ? asList.toArray((T[]) Array.newInstance(valuesClass, 0)) : null;
     }
 
     /**
      * This method parses the {@code value} param and returns it as an instance
      * of the class. It is internally used by several methods.
      * 
      * @param value
      *            Value to cast/parse.
      * @param valueClass
      *            Class to made the cast to, the value will be returned as an
      *            instance of this class.
      * @return The value as a new instance of the class.
      * @throws ConfigurationException
      *             if the parameter value could not be casted to the class.
      * @throws IllegalArgumentException
      *             if the value to parse or the class to made the cast to are
      *             {@code null}.
      */
     // The castings to (T) cause a warning (unckeched) that we can safely ignore, also
     // the Enum conversion causes a warning (rawtypes) as it should be parameterized (but
     // I can't find how to do it?!)
     @SuppressWarnings({ "unchecked", "rawtypes" })
     protected <T> T parseValue(String value, Class<T> valueClass)
             throws ConfigurationException {
 
         if (value == null)
             throw new IllegalArgumentException("Cannot parse a null value");
         if (valueClass == null)
             throw new IllegalArgumentException("It is needed to know the class to parse the value to");
 
         if (valueClass.isPrimitive()) {
             try {
                 if (valueClass.equals(Character.TYPE))
                     if (value.isEmpty())
                         throw new ConfigurationException("Cannot parse an empty string to a character");
                     else
                         return (T) new Character(value.toCharArray()[0]);
                 if (valueClass.equals(Boolean.TYPE))
                     return (T) new Boolean(Boolean.parseBoolean(value));
                 if (valueClass.equals(Byte.TYPE))
                     return (T) new Byte(Byte.parseByte(value));
                 if (valueClass.equals(Short.TYPE))
                     return (T) new Short(Short.parseShort(value));
                 if (valueClass.equals(Integer.TYPE))
                     return (T) new Integer(Integer.parseInt(value));
                 if (valueClass.equals(Long.TYPE))
                     return (T) new Long(Long.parseLong(value));
                 if (valueClass.equals(Float.TYPE))
                     return (T) new Float(Float.parseFloat(value));
                 if (valueClass.equals(Double.TYPE))
                     return (T) new Double(Double.parseDouble(value));
             } catch (NumberFormatException exception) {
                 throw new ConfigurationException("Could not parse value '" + value
                                                 + "' to " + valueClass.getSimpleName()
                                                 + ", please check");
             }
         }
 
         if (valueClass.equals(String.class))
             return valueClass.cast(value);
 
         if (valueClass.equals(Boolean.class))
             return valueClass.cast(Boolean.parseBoolean(value));
 
         try {
             if (valueClass.equals(Byte.class))
                 return valueClass.cast(Byte.parseByte(value));
             if (valueClass.equals(Short.class))
                 return valueClass.cast(Short.parseShort(value));
             if (valueClass.equals(Integer.class))
                 return valueClass.cast(Integer.parseInt(value));
             if (valueClass.equals(Long.class))
                 return valueClass.cast(Long.parseLong(value));
             if (valueClass.equals(Float.class))
                 return valueClass.cast(Float.parseFloat(value));
             if (valueClass.equals(Double.class))
                 return valueClass.cast(Double.parseDouble(value));
         } catch (NumberFormatException exception) {
             throw new ConfigurationException("Could not parse value '" + value
                                             + "' to " + valueClass.getSimpleName() + ", please check");
         }
 
         if (valueClass.isEnum())
             return valueClass.cast(Enum.valueOf((Class<? extends Enum>) valueClass, value));
 
         if (valueClass.equals(Class.class))
             try {
                 return valueClass.cast(Class.forName(value));
             } catch (ClassNotFoundException exception) {
                 throw new ConfigurationException("Cannot find class with name '" + value + "'");
             }
 
         throw new ConfigurationException("Cannot parse objects of class " + valueClass.getName());
 
     }
     
     protected static boolean paramBelongsToPacket(String param, String packet) {
         return param.startsWith(packet + PACKET_AND_PARAM_NAMES_SEPARATOR);
     }
 
     /**
      * One of the <i>key</i> methods of Onodrim, to be used along with
      * {@link #extractAllParamValues(Properties, String)}. Given a single
      * configuration, where several values are assigned to some parameter, it
      * generates a {@link java.util.List} of configurations where that param has
      * only one of those values. In other words, it the map passed as input
      * contains the following associations:<br>
      * {@code param1->[a]}<br>
      * {@code param2->[1,2]}
      * <p>
      * this method would generate a {@link java.util.List} instance with 2
      * instances of {@link Configuration}, where each one has the following
      * contents:<br>
      * {@code param1=a         param1=a}<br>
      * {@code param2=1         param2=2}
      * <p>
      * Also, it is possible to set how many times each configuration must be
      * repeated, so if we ask for two repetitions of the previous map then this
      * method would return:<br>
      * {@code param1=a    param1=a    param1=a    param1=a}<br>
      * {@code param2=1    param2=2    param2=1    param2=2} Grouping sets how
      * generated configurations must be ordered, let's assume the input map is:<br>
      * {@code param1->[a,b]}<br>
      * {@code param2->[1,2]} the generated configurations could be:<br>
      * {@code param1=a    param1=a    param1=b    param1=b}<br>
      * {@code param2=1    param2=2    param2=1    param2=2} But if we demand
      * configurations to be grouped by {@code param2}, then we would get
      * instead:<br>
      * {@code param1=a    param1=b    param1=a    param1=b}<br>
      * {@code param2=1    param2=1    param2=2    param2=2}
      * <p>
      * Finally, it is possible to set in which order the generated
      * configurations should be in the list by the {@code groupBy} param. This
      * method will group and order the resulting configurations by the values of
      * the parameters included in {@code groupBy}.
      * <p>
      * Please recall that this method <i>does not check for numeric ranges definition</i>,
      * for that you must call first
      * {@link #extractAllParamValues(Properties, String)}. I.e. a typical call
      * could be as follows:<br>
      * {@code List<Properties> allConfigs = buildConfigurations(extractAllParamValues(props, ";"),1,null,null);}
      * <p>
      * Also, all parameters are treated equally, regardless whether they are
      * Onodrim-intended parameters or not. In other words, this method is an
      * utility that can be used by developers interested only in the simplest
      * generation properties of Onodrim.
      * 
      * @param paramsValues
      *            {@link java.util.Map} containing the pairs of parameter names
      *            and values that will be used to generate the configurations.
      * @param repetitions
      *            The amount of times each configuration must be repeated. If
      *            set to 0 or some negative value, the resulting list will be
      *            empty.
      * @param groupBy
      *            The name of the parameters whose values would determine how to
      *            group the generated configurations. If {@code null}, it is
      *            ignored.
      * @param packets
      *            Experiments parameters can be organized in 'packets' or families, which is
      *            set by their name. Each packet parameters configures an independent
      *            set of experiments.      
      * @param paramsValuesBindings
      *            Only configurations with parameters with values as defined in the bindings will
      *            be returned. Parameters bindings are in the form Param1=Value1:Param2=Value2, in this
      *            case the method will only return configurations where the first Param1 equals Value1
      *            and Param2 equals Value2.
      * @return The generated configurations.
      * @throws ConfigurationException
      *             if the {@link java.util.Map} instance is empty,
      * @throws IllegalArgumentException
      *             if the {@link java.util.Map} instance is {@code null}.
      */
     protected static List<Configuration> buildConfigurations(   Map<String, String[]> paramsValues,
                                                                 int repetitions,
                                                                 String[] groupBy,
                                                                 String[] packets,
                                                                 ParamValueBinding[] paramsValuesBindings,
                                                                 ParamGenerationCondition[] paramsGerationConditions) throws ConfigurationException {
 
         if (paramsValues == null)
             throw new IllegalArgumentException("Cannot generate configurations from a null map");
         
         // Create map that associates each experiments packet to its parameters-values map, the idea is to group
         // parameters that belong to the same packet. If no packet has been given, all parameters are assigned to
         // a default packet.
         Map<String, Map<String, String[]>> paramsValuesPerPacket = new HashMap<String, Map<String, String[]>>();
         if(packets != null) {
             for(String packet: packets) {
                 if(packet.isEmpty())
                     throw new ConfigurationException("Cannot generate configuration with an empty packet name");
                 if(packet.equals(DEFAULT_PACKET_NAME))
                     throw new ConfigurationException("Cannot define a packet with the name '" + DEFAULT_PACKET_NAME + ", it is reserved");
                 Map<String, String[]> paramsValuesOfPacket = new HashMap<String, String[]>();
                 Iterator<String> iter = paramsValues.keySet().iterator();
                 while(iter.hasNext()) {
                     String param = iter.next();
                     if(paramBelongsToPacket(param, packet)) {
                         paramsValuesOfPacket.put(param, paramsValues.get(param));
                         iter.remove();
                     }
                 }
                 if(paramsValuesOfPacket.isEmpty())
                     throw new ConfigurationException("No param found that belongs to packet " + packet);
                 paramsValuesPerPacket.put(packet, paramsValuesOfPacket);
             }
             // Remaining elements in paramsValues, i.e. those that do not belong to any particular packet, belong
             // to all packets.
             for(String packet: paramsValuesPerPacket.keySet())
                 paramsValuesPerPacket.get(packet).putAll(paramsValues);
         } else {
             // No packet defined, all parameters belong to the default package.
             paramsValuesPerPacket.put(DEFAULT_PACKET_NAME, paramsValues);
         }
         
         // None of these conditions should ever happen in fact, but let's check
         if(paramsValuesPerPacket.keySet().size() < 1)
             throw new Error("0 packets found(?)");
         if((paramsValuesPerPacket.keySet().size() > 1) && (paramsValuesPerPacket.keySet().contains(DEFAULT_PACKET_NAME)))
             throw new Error("Default packet '" + DEFAULT_PACKET_NAME + "' present when there are more than one packet (?)");
         if((packets != null) && !(paramsValuesPerPacket.keySet().containsAll(Arrays.asList(packets))))
             throw new Error("Not all packets were properly processed, some not have any parameter assigned");
         if((packets == null) && ((paramsValuesPerPacket.keySet().size() != 1) || (!paramsValuesPerPacket.keySet().contains(DEFAULT_PACKET_NAME))) )
             throw new Error("Not packets defined but '" + DEFAULT_PACKET_NAME + "' default packet is missing as well(?)");
         
         // Checking that binding conditions are properly defined, i.e. both parameters refer to the same packet (but they are not the same)
         // This checking is necessary only if the default packet is not being used (if so all parameters belong to that packet)
         if(!paramsValuesPerPacket.keySet().contains(DEFAULT_PACKET_NAME) && (paramsValuesBindings != null))
             for(ParamValueBinding bindingCondition: paramsValuesBindings) {
                 String firstParameter = bindingCondition.getFirstParamValue().getParameter();
                 String secondParameter = bindingCondition.getSecondParamValue().getParameter();
                 if(firstParameter.equals(secondParameter))
                     throw new ConfigurationException("Found binding that bounds parameter '" + firstParameter +
                             "' with itself, check configuration parameter '" + PARAMETERS_BINDING_PROPERTY_NAME + "'");
                 // Looking for which packet the first parameter belongs to
                 String packetInBinding = null;
                 for(String packet: paramsValuesPerPacket.keySet()) 
                     if(paramBelongsToPacket(firstParameter, packet) || paramBelongsToPacket(secondParameter, packet)) {
                         packetInBinding = packet;
                         break;
                     }
                 if(packetInBinding != null) // Found, now checking if the second parameter belongs to any other packet
                     for(String packet: paramsValuesPerPacket.keySet()) 
                         if(!packet.equals(packetInBinding))
                             if(paramBelongsToPacket(secondParameter, packet) || paramBelongsToPacket(firstParameter, packet))
                                 throw new ConfigurationException("Found binding for parameters '" + firstParameter+ "' and '" +
                                         secondParameter + "' but they belong to different packets: '" + packetInBinding +
                                         "' and '" + packet + "', check configuration parameter '" + PARAMETERS_BINDING_PROPERTY_NAME + "'");
             }
         
         // Checking that generation conditions are properly defined, i.e. each one refers to parameters in the same packet
         // or in non packet in particular
         // As before, this is necessary only if the default packet is not being used.
         if(!paramsValuesPerPacket.keySet().contains(DEFAULT_PACKET_NAME) && (paramsGerationConditions != null))
             for(ParamGenerationCondition paramGenerationCondition:paramsGerationConditions) {
                 System.out.println("\nChecking parameter generation condition: " + paramGenerationCondition);
                 List<String> parametersInCondition = new ArrayList<String>();
                 parametersInCondition.addAll(paramGenerationCondition.getParameters());
                 parametersInCondition.add(paramGenerationCondition.getParamValue().getParameter());
                 System.out.println("Parameters in condition:" + parametersInCondition.toString());
                 // Looking for packet...
                 String packetUsedInGenCond = null;
                 for(String parameterInCondition: parametersInCondition)
                     for(String packet: paramsValuesPerPacket.keySet())
                         if(paramBelongsToPacket(parameterInCondition, packet)) {
                             if((packetUsedInGenCond != null) && (!packetUsedInGenCond.equals(packet)))
                                 throw new ConfigurationException("Found two packets '" + packetUsedInGenCond + "' and '" + packet +
                                         "' in generation condition definition '" + paramGenerationCondition +
                                         "', check configuration parameter '" + GENERATE_PARAMETERS_VALUES_CONDITION_PROPERTY_NAME + "'");
                             packetUsedInGenCond = packet;
                             break;
                         }
             }
         
         List<Configuration> configurations = new ArrayList<Configuration>();
         for(String packet: paramsValuesPerPacket.keySet()) {
 
             Map<String, String[]> paramsValuesPacket = paramsValuesPerPacket.get(packet);
             
             Configuration[] confs = generateConfigurations(paramsValuesPacket, paramsGerationConditions);
 
             // Grouping jobs configurations as set by the user
             if (groupBy != null) {
                 // Must group only by the parameters that belong to this packet
                 List<String> groupByForPacket = new ArrayList<String>(Arrays.asList(groupBy));
                 groupByForPacket.retainAll(paramsValuesPacket.keySet());
                 confs = groupConfsBy(confs, groupByForPacket.toArray(new String[]{}));
             }
 
             // Filtering to get those configurations where parameters are bound as defined, for this first we get those bindings that
             // refer to this packet (if this is the default packet, all the bindings are applicable, if not, only those that refer to
             // parameters of the packet)
             if(paramsValuesBindings != null) {
                 List<ParamValueBinding> paramsValuesBindingsForPacket = new ArrayList<ParamValueBinding>(Arrays.asList(paramsValuesBindings));
                 if(!packet.equals(DEFAULT_PACKET_NAME)) {
                     Iterator<ParamValueBinding> iter = paramsValuesBindingsForPacket.iterator();
                     while(iter.hasNext()) {
                         ParamValueBinding paramValueBinding = iter.next();
                         if(!paramBelongsToPacket(paramValueBinding.getFirstParamValue().getParameter(), packet))
                             iter.remove();
                     }
                 }
                 confs = filterConfsByParamsBinding(confs, paramsValuesBindingsForPacket.toArray(new ParamValueBinding[]{}));
             }
             
             // Generating repetitions (i.e. copies) of each configuration
             List<Configuration> confsRepeated = new ArrayList<Configuration>();
             for (Configuration conf : confs)
                 for (int repCounter = 0; repCounter < repetitions; repCounter++)
                     if(repCounter == 0)
                         confsRepeated.add(conf);
                     else
                         confsRepeated.add(new Configuration(conf));
             
             // Setting to which packet the configurations belong to
             for(Configuration conf: confsRepeated)
                 conf.setPacketName(packet);
             
             configurations.addAll(confsRepeated);
             
         }
 
         if (configurations.size() == 0)
             throw new ConfigurationException("Could not build any job configuration (all properties empty?)");
         
         return configurations;
     }
     
     /**
      * This method will load the properties in the file passed as parameter
      * (using {@link java.util.Properties#load(java.io.Reader)} method),
      * and then will forward the call to the {@link #buildConfigurations(Properties)} method.
     * @param jobsConfsFile File containing the definition of the configurations to build. It must be
     * written using the {@code .properties} files format ( @see http://en.wikipedia.org/wiki/.properties).
      * @return The configurations generated, in a {@code List} of {@link Configuration} instances.
      * @throws ConfigurationException If some error is found in the definition.
      */
     public static List<Configuration> buildConfigurations(File jobsConfsFile) throws ConfigurationException {
     	
     	Properties properties = new Properties();
     	try {
 			properties.load(new FileReader(jobsConfsFile));
 		} catch (FileNotFoundException exception) {
 			throw new ConfigurationException("The properties file '" + jobsConfsFile.getAbsolutePath() + "' could not be found", exception);
 		} catch (IOException exception) {
 			throw new ConfigurationException(IOException.class.getName() + " exception caught when trying to read the properties file '" + jobsConfsFile.getAbsolutePath() + "'", exception);
 		}
     	
     	return buildConfigurations(properties);
     	
     }
 
     /**
      * This method will build the configurations as defined by the Properties instance passed
      * as a parameter. To know more about how Onodrim interprets this file and generates the
      * configurations please @see https://github.com/lrodero/onodrim/wiki .
      * @param jobsConfsProps The definition of the configurations to build.
      * @return The configurations generated, in a {@code List} of {@link Configuration} instances.
      * @throws ConfigurationException If some error is found in the definition.
      */
     public static List<Configuration> buildConfigurations(Properties jobsConfsProps) throws ConfigurationException {
 
         Configuration jobsConf = new Configuration(jobsConfsProps);
 
         // Separator between parameter values
         String paramSeparator = jobsConf.containsKey(PARAMETERS_SEPARATOR_PROPERTY_NAME) ? jobsConf.getProperty(PARAMETERS_SEPARATOR_PROPERTY_NAME) : DEFAULT_PARAMETERS_SEPARATOR;
         if (paramSeparator.isEmpty())
             throw new ConfigurationException("The separator among parameters, set by property '"
                                             + PARAMETERS_SEPARATOR_PROPERTY_NAME
                                             + "' cannot be an empty string");
 
         // Amount of times each configuration must be repeated
         int repetitions = DEFAULT_REPETITIONS_PER_CONF;
         if (jobsConf.containsKey(REPETITIONS_PER_CONF_PROPERTY_NAME))
             try {
                 repetitions = jobsConf.getIntParameter(REPETITIONS_PER_CONF_PROPERTY_NAME, DEFAULT_REPETITIONS_PER_CONF);
             } catch (ConfigurationException exception) {
                 throw new ConfigurationException("Property '" + REPETITIONS_PER_CONF_PROPERTY_NAME
                                                 + "' sets the amount of repetitions per configuration"
                                                 + "and so it must contain an integer");
             }
 
         // Jobs can be grouped by parameters
         String[] groupBy = null;
         if (jobsConf.containsKey(GROUP_CONFS_BY_PROPERTY_NAME)) {
             groupBy = jobsConf.getArrayParameter(GROUP_CONFS_BY_PROPERTY_NAME, String.class, null);
             // Checking that all the parameters set to group by, are indeed defined in the configuration
             if (groupBy != null)
                 for (String paramName : groupBy)
                     if (!jobsConf.containsKey(paramName))
                         throw new ConfigurationException("Cannot group jobs by parameter '" + paramName
                                                         + "', that parameter is not in the configuration, check values in '" + GROUP_CONFS_BY_PROPERTY_NAME + "'");
         }
         
         // Packets are used to define 'families' of jobs; jobs in different packets do not share the configuration parameters
         String[] packets = null;
         if(jobsConf.containsKey(PACKETS_PROPERTY_NAME)) {
             packets = jobsConf.getArrayParameter(PACKETS_PROPERTY_NAME, String.class, null);
             // Checking that at least one parameter belongs to that packet, and that no packet is defined as ""
             for(String packet: packets) {
                 packet = packet.trim();
                 if(packet.isEmpty())
                     throw new ConfigurationException("Cannot define a packet with empty name, check values in '" + PACKETS_PROPERTY_NAME + "'");
                 // Checking that at least some parameter belongs to that packet
                 boolean paramInPacketFound = false;
                 for(String param: jobsConf.stringPropertyNames())
                     if(paramBelongsToPacket(param, packet)) {
                         paramInPacketFound = true;
                         break;
                     }
                 if(!paramInPacketFound)
                     throw new ConfigurationException("Not parameter found that belongs to packet '" + packet + "', check values in '" + PACKETS_PROPERTY_NAME + "'");
             }
             // Checking that no packet starts with the name of any other packet, and that no two packets have the same name
             for(int firstPacketIndex = 0; firstPacketIndex < packets.length; firstPacketIndex++) {
                 String firstPacket = packets[firstPacketIndex];
                 for(int secondPacketIndex = firstPacketIndex + 1; secondPacketIndex < packets.length; secondPacketIndex++) {
                     String secondPacket = packets[secondPacketIndex];
                     if(firstPacket.equals(secondPacket))
                         throw new ConfigurationException("Packet '" + firstPacket + "' defined more than once, check '" + PACKETS_PROPERTY_NAME + "' property");
                     if(firstPacket.startsWith(secondPacket) || secondPacket.startsWith(firstPacket))
                         throw new ConfigurationException("No packet can be the prefix of any other packet, check packets '" +
                                                             firstPacket + "' and '" + secondPacket + "' in property '" + PACKETS_PROPERTY_NAME + "'");
                 }
             }
         }
         
         // Shall we ignore any package?
         String[] packetsToIgnore = null;
         if(jobsConf.containsKey(PACKETS_TO_IGNORE_PROPERTY_NAME)) {
             packetsToIgnore = jobsConf.getArrayParameter(PACKETS_TO_IGNORE_PROPERTY_NAME, String.class, null);
             // Checking that no packet to ignore is defined as ""
             for(String packetToIgnore: packetsToIgnore) {
                 packetToIgnore = packetToIgnore.trim();
                 if(packetToIgnore.isEmpty())
                     throw new ConfigurationException("Cannot define a packet to ignore with empty name, check values in '" + PACKETS_TO_IGNORE_PROPERTY_NAME + "'");
             }
             // Checking that all packets to ignore are indeed defined in the packets list (first, checking if there is any packet list at all)
             if((packets == null) || (packets.length == 0))
                 throw new ConfigurationException("Cannot defined a set of packets to ignore without defining any packet first! Check '" + PACKETS_PROPERTY_NAME + "' and '" + PACKETS_TO_IGNORE_PROPERTY_NAME + "' parameters");
             List<String> packetsCopy = new ArrayList<String>(Arrays.asList(packets));
             for(String packetToIgnore: packetsToIgnore)
                 if(!packetsCopy.contains(packetToIgnore))
                     throw new ConfigurationException("Packet '" + packetToIgnore + "' marked as packet to ignore, but not found in the packets list, check '" + PACKETS_PROPERTY_NAME + "' and '" + PACKETS_TO_IGNORE_PROPERTY_NAME + "' parameters");
             packetsCopy.removeAll(new ArrayList<String>(Arrays.asList(packetsToIgnore)));
             // Checking that we still have some packet to run
             if(packetsCopy.isEmpty())
                 throw new ConfigurationException("All defined packets are to be ignored, cannot create any configuration! Check '" + PACKETS_PROPERTY_NAME + "' and '" + PACKETS_TO_IGNORE_PROPERTY_NAME + "' parameters");
             packets = packetsCopy.toArray(new String[]{});
         }
         
         String[] paramsValuesBindingsValue = jobsConf.getArrayParameter(PARAMETERS_BINDING_PROPERTY_NAME, String.class, null);
         ParamValueBinding[] paramsValuesBindings = parseParametersBindings(paramsValuesBindingsValue);
         if(paramsValuesBindings != null)
             for(ParamValueBinding paramValueBinding: paramsValuesBindings) {
                 // Checking that no binding refers to parameters that are not present in the configuration
                 if(!jobsConf.containsKey(paramValueBinding.getFirstParamValue().getParameter()))
                     throw new ConfigurationException("Defined parameter binding with parameter '" + paramValueBinding.getFirstParamValue().getParameter() + "', but parameter is not defined, check values in '" + PARAMETERS_BINDING_PROPERTY_NAME + "'");
                 if(!jobsConf.containsKey(paramValueBinding.getSecondParamValue().getParameter()))
                     throw new ConfigurationException("Defined parameter binding with parameter '" + paramValueBinding.getSecondParamValue().getParameter() + "', but parameter is not defined, check values in '" + PARAMETERS_BINDING_PROPERTY_NAME + "'");
             }
         
         
         String[] generateParametersCondValues = jobsConf.getArrayParameter(GENERATE_PARAMETERS_VALUES_CONDITION_PROPERTY_NAME, String.class, null);
         ParamGenerationCondition[] paramGenerationsConditions = parseParametersGenerationConditions(generateParametersCondValues);
         if(paramGenerationsConditions != null)
             for(ParamGenerationCondition paramGenerationCondition: paramGenerationsConditions) {
                 Set<String> parameters = paramGenerationCondition.getParameters();
                 for(String parameter: parameters)
                     if(!jobsConf.containsKey(parameter))
                         throw new ConfigurationException("Defined parameter generation condition with parameter '" + parameter + "', but parameter is not defined, check values in '" + GENERATE_PARAMETERS_VALUES_CONDITION_PROPERTY_NAME + "'");
                 ParamValue paramValue = paramGenerationCondition.getParamValue();
                 if(!jobsConf.containsKey(paramValue.getParameter()))
                     throw new ConfigurationException("Defined parameter generation condition with parameter '" + paramValue.getParameter() + "', but parameter is not defined, check values in '" + GENERATE_PARAMETERS_VALUES_CONDITION_PROPERTY_NAME + "'");
             }
         
         jobsConf.remove(PARAMETERS_SEPARATOR_PROPERTY_NAME);
         jobsConf.remove(REPETITIONS_PER_CONF_PROPERTY_NAME);
         jobsConf.remove(GROUP_CONFS_BY_PROPERTY_NAME);
         jobsConf.remove(PACKETS_PROPERTY_NAME);
         jobsConf.remove(PARAMETERS_BINDING_PROPERTY_NAME);
         jobsConf.remove(GENERATE_PARAMETERS_VALUES_CONDITION_PROPERTY_NAME);
 
         return Configuration.buildConfigurations(Configuration.extractAllParamValues(jobsConf, paramSeparator),
                                                  repetitions, groupBy, packets,
                                                  paramsValuesBindings, paramGenerationsConditions);
     }
 
     private static Configuration[] filterConfsByParamsBinding(Configuration confs[], ParamValueBinding[] paramsValuesBindings)
             throws ConfigurationException {
 
         if(paramsValuesBindings == null)
             return confs;
         
         if(paramsValuesBindings.length == 0)
             return confs;
 
         List<Configuration> filteredConfs = new ArrayList<Configuration>(Arrays.asList(confs));
         Iterator<Configuration> filteredConfsIter = filteredConfs.iterator();
         while (filteredConfsIter.hasNext()) {
             Configuration conf = filteredConfsIter.next();
             for (ParamValueBinding paramBinding : paramsValuesBindings) {
                 System.out.println("Checking binding: "
                             +paramBinding.getFirstParamValue().getParameter()+"="+paramBinding.getFirstParamValue().getValue()+"<->"
                             +paramBinding.getSecondParamValue().getParameter()+"="+paramBinding.getSecondParamValue().getValue());
                 // Maybe the generation configuration conditions lead to configurations where one or the two parameters in the binding
                 // are not present, such configurations MUST be ignored
                 if(!conf.containsKey(paramBinding.getFirstParamValue().getParameter()) || !conf.containsKey(paramBinding.getSecondParamValue().getParameter())) {
                     filteredConfsIter.remove();
                     break;
                 }
                 if ( conf.getParameter(paramBinding.getFirstParamValue().getParameter()).equals(paramBinding.getFirstParamValue().getValue()) &&
                     !conf.getParameter(paramBinding.getSecondParamValue().getParameter()).equals(paramBinding.getSecondParamValue().getValue()) ) {
                     filteredConfsIter.remove();
                     break;
                 }
                 if (!conf.getParameter(paramBinding.getFirstParamValue().getParameter()).equals(paramBinding.getFirstParamValue().getValue()) &&
                      conf.getParameter(paramBinding.getSecondParamValue().getParameter()).equals(paramBinding.getSecondParamValue().getValue()) ) {
                     filteredConfsIter.remove();
                     break;
                 }
             }
         }
 
         return filteredConfs.toArray(new Configuration[] {});
     }
 
     private static ParamValueBinding[] parseParametersBindings(String[] paramsBindings) throws ConfigurationException {
         
         if ((paramsBindings == null) || (paramsBindings.length == 0))
             return null;
         
         ParamValueBinding[] parsedBindings = new ParamValueBinding[paramsBindings.length];
 
         for (int index = 0; index < paramsBindings.length; index++) {
             String paramsBinding = paramsBindings[index];
             String[] binding = paramsBinding.split(BINDING_SEPARATOR);
             if (binding.length != 2)
                 throw new ConfigurationException( "Cannot parse '"
                                 + PARAMETERS_BINDING_PROPERTY_NAME + "' properly, '" + paramsBinding
                                 + "' is not well constructed, it should contain two strings split by '"
                                 + BINDING_SEPARATOR + "'");
             String[] paramValue1 = binding[0].split("=");
             if (paramValue1.length != 2)
                 throw new ConfigurationException("Cannot parse '"
                         + PARAMETERS_BINDING_PROPERTY_NAME + "' properly, '"
                         + paramsBinding + "' is not well constructed in '"
                         + binding[0]
                         + "', it should contain two strings split by '='");
             String[] paramValue2 = binding[1].split("=");
             if (paramValue2.length != 2)
                 throw new ConfigurationException("Cannot parse '"
                         + PARAMETERS_BINDING_PROPERTY_NAME + "' properly, '"
                         + paramsBinding + "' is not well constructed in '"
                         + binding[1]
                         + "', it should contain two strings split by '='");
             parsedBindings[index] = new ParamValueBinding(new ParamValue(paramValue1[0], paramValue1[1]), new ParamValue(paramValue2[0], paramValue2[1]));
         }
 
         return parsedBindings;
     }
     
     private static ParamGenerationCondition[] parseParametersGenerationConditions(String[] paramsGenerationConds) throws ConfigurationException {
         
         if((paramsGenerationConds == null) || (paramsGenerationConds.length == 0))
             return null;
         
         ParamGenerationCondition[] parsedGenerationConditions = new ParamGenerationCondition[paramsGenerationConds.length];
         
         for(int index = 0; index < paramsGenerationConds.length; index++) {
             String paramsGenerationCond = paramsGenerationConds[index];
             String[] condition = paramsGenerationCond.split(PARAMETER_CONDITIONS_SEPARATOR);
             if(condition.length < 2)
                 throw new ConfigurationException("Cannot parse '"
                                 + GENERATE_PARAMETERS_VALUES_CONDITION_PROPERTY_NAME + "' properly, '" + paramsGenerationCond
                                 + "' is not well constructed, it should contain at least two strings split by '" + PARAMETER_CONDITIONS_SEPARATOR + "'");
             Set<String>paramsToGenerate = new HashSet<String>(Arrays.asList(Arrays.copyOfRange(condition, 0, condition.length-1)));
             String generationParameter = condition[condition.length-1];
             String[] paramValue = generationParameter.trim().split("=");
             if(paramValue.length != 2)
                 throw new ConfigurationException("Cannot parse '"
                         + GENERATE_PARAMETERS_VALUES_CONDITION_PROPERTY_NAME + "' properly, '" + paramsGenerationCond
                         + "' is not well constructed in '" + generationParameter + "', it should contain two strings split by '='");
             parsedGenerationConditions[index] = new ParamGenerationCondition(paramsToGenerate, new ParamValue(paramValue[0], paramValue[1]));
         }
         
         return parsedGenerationConditions;
     }
     
     static class ParamValue {
         private String parameter = null;
         private String value = null;
         ParamValue(String parameter, String value) {
             this.parameter = parameter;
             this.value = value;
         }
         String getParameter() {
             return parameter;
         }
         String getValue() {
             return value;
         }
     }
     
     static class ParamValueBinding {
         private ParamValue firstParamValue = null;
         private ParamValue secondParamValue = null;
         ParamValueBinding(ParamValue firstParamValue, ParamValue secondParamValue) {
             this.firstParamValue = firstParamValue;
             this.secondParamValue = secondParamValue;
         }
         ParamValue getFirstParamValue() {
             return firstParamValue;
         }
         ParamValue getSecondParamValue() {
             return secondParamValue;
         }
     }
     
     static class ParamGenerationCondition {
         private Set<String> parameters = null;
         private ParamValue paramValue = null;
         ParamGenerationCondition(Set<String> parameters, ParamValue paramValue) {
             this.parameters = parameters;
             this.paramValue = paramValue;
         }
         Set<String> getParameters() {
             return parameters;
         }
         ParamValue getParamValue() {
             return paramValue;
         }
         @Override
         public String toString() {
             return "{Parameters:" + Arrays.toString(parameters.toArray(new String[]{})) + " Condition:" + paramValue.getParameter() + "=" + paramValue.getValue() + "}";
         }
     }
 
     /**
      * Reorders {@link Configuration} instances by the values of the parameters
      * passed in the string array.
      * @param confs
      *            Configurations to order. 
      * @param groupBy
      *            Names of the parameters whose values will be used to group the
      *            configurations.
      * @return The same configurations passed as input, but re-ordered.
      * @throws ConfigurationException
      *             if some parameter defined in the string array cannot be found
      *             in the input configurations
      */
     private static Configuration[] groupConfsBy(Configuration[] confs, String[] groupBy) throws ConfigurationException {
 
         if (confs.length <= 1)
             return confs;
 
         if (groupBy.length == 0)
             return confs;
 
         Configuration[][] groupedPropsSets = splitByProperty(confs, groupBy[0]);
 
         String[] remainingPropsNames = new String[groupBy.length - 1];
         System.arraycopy(groupBy, 1, remainingPropsNames, 0, remainingPropsNames.length);
 
         for (int propsSetsGroupIndex = 0; propsSetsGroupIndex < groupedPropsSets.length; propsSetsGroupIndex++)
             groupedPropsSets[propsSetsGroupIndex] = groupConfsBy(groupedPropsSets[propsSetsGroupIndex], remainingPropsNames);
 
         return (Configuration[]) Util.Collections.union(groupedPropsSets, Configuration.class);
         // return joinConfArrays(groupedPropsSets);
     }
 
     /**
      * Simple method that splits an array of configurations by the values of a
      * certain parameter. Only called by
      * {@link #groupConfsBy(Configuration[], String[])}.
      * 
      * @param confs
      *            Configurations to split.
      * @param propName
      *            Parameter to split the configurations by.
      * @return The same configurations passed as input, but grouped into new
      *         arrays.
      * @throws ConfigurationException
      *             if the parameter to split the configurations by does not
      *             exist.
      */
     private static Configuration[][] splitByProperty(Configuration[] confs, String propName) throws ConfigurationException {
 
         if (confs.length <= 1)
             return new Configuration[][] { confs };
 
         // Gathering all different values of the property, and grouping confs by
         // those values
         // Checking also that all sets of properties contain that property.
         Map<String, List<Configuration>> valuesConfsMap = new HashMap<String, List<Configuration>>();
         List<String> values = new ArrayList<String>(); // To remember in which
         // order all values were
         // found
         for (Configuration conf : confs) {
             String value = conf.getParameter(propName);
             if (value == null)
                 throw new ConfigurationException("Parameter '" + propName + "' can not be found in properties set, cannot group by it");
             if (!valuesConfsMap.containsKey(value)) {
                 valuesConfsMap.put(value, new ArrayList<Configuration>());
                 values.add(value);
             }
             valuesConfsMap.get(value).add(conf);
         }
 
         Configuration[][] splitConfs = new Configuration[valuesConfsMap.size()][];
         int arrayCounter = 0;
         for (String value : values)
             splitConfs[arrayCounter++] = valuesConfsMap.get(value).toArray(new Configuration[0]);
 
         return splitConfs;
     }
 
     /**
      * This method 'converts' the value associated to each parameter
      * configuration in the input properties instance, converting it into an
      * array of values after splitting it using the separator passed also as
      * input (Onodrim uses as default ';'). Also, numeric parameters can be
      * defined not as a list of values split by ';', but also as a <i>range</i>,
      * for example {@code [5:2:9]} is like saying 'all values from 5 to 9, every
      * two steps', i.e. it is equivalent to {@code 5;7;9}; this method generates
      * those values for each range before splitting the string.
      * 
      * @param unparsedConfs
      *            Parameters whose values must be split into strings, if
      *            ranges are defined the corresponding values are generated
      *            first.
      * @param separator
      *            Separator to split the parameter values by.
      * @return A {@link java.util.Map} instance where to each parameter name
      *         corresponds an array of string instances, where each item is one
      *         of the values assigned to the parameter in the input
      *         {@link java.util.Properties} instance.
      * @throws ConfigurationException
      *             if the generation process failed because some range
      *             definition found was incorrect. This could happen if some non
      *             numeric value is given, or the final value cannot be reached
      *             from the original value in the increments included in the
      *             definition.
      */
     public static Map<String, String[]> extractAllParamValues(Properties unparsedConfs, String separator) throws ConfigurationException {
 
         Map<String, String[]> paramsValues = new HashMap<String, String[]>();
 
         // Parsing values: splitting if more than one value is set for some
         // parameter (values are
         // separated by the 'separator' String). Also, value ranges are
         // generated.
         for (String param : unparsedConfs.stringPropertyNames()) {
             String rawValue = unparsedConfs.getProperty(param);
             rawValue = checkAndGenRanges(rawValue, separator);
             String[] parsedValues = rawValue.split(separator);
             paramsValues.put(param, parsedValues);
         }
 
         return paramsValues;
     }
     
     private static Configuration[] generateConfigurations(Map<String, String[]> paramsValues, ParamGenerationCondition[] paramGenerationConditions) throws ConfigurationException {
         
         // Is there any condition to apply?
         if((paramGenerationConditions == null) ||(paramGenerationConditions.length == 0))
             return generateConfigurations(paramsValues);
         
         // Checking if parameters that define the generation condition are in the params-values map. Either
         // all or none of them must be in the map.
         List<ParamGenerationCondition> paramGenerationConditionsApplicable = new ArrayList<ParamGenerationCondition>(); 
         for(ParamGenerationCondition paramGenerationCondition: paramGenerationConditions) {
             // First getting all the parameters in the condition
             if(paramsValues.containsKey(paramGenerationCondition.getParamValue().getParameter()))
                 paramGenerationConditionsApplicable.add(paramGenerationCondition);
         }
         
         // If no applicable conditions are found, then we act as there are not conditions at all
         if(paramGenerationConditionsApplicable.size() == 0)
             return generateConfigurations(paramsValues);
         
         // For each value of the parameter that controls whether parameters must be generated or not,
         // we get rid of the parameters that are filtered out by the value
         List<Configuration>configurations = new ArrayList<Configuration>();
         for(ParamGenerationCondition applicableCondition: paramGenerationConditionsApplicable) {
             ParamValue paramValue = applicableCondition.getParamValue();
             if(!paramsValues.containsKey(paramValue.getParameter()))
                 throw new Error("Found a non existing parameter '" + paramValue.getParameter() + "' in a condition that passed the filters!");
             Map<String, String[]> paramsValuesForThisCondition = new HashMap<String, String[]>(paramsValues);
             Set<String> valuesOfParamValueInCondition = new HashSet<String>(Arrays.asList( paramsValuesForThisCondition.remove(paramValue.getParameter()) ));
             if(!valuesOfParamValueInCondition.contains(paramValue.getValue())) // If the value in the condition is not defined, then it must be ignored!
                 continue;
             paramsValuesForThisCondition.keySet().retainAll(applicableCondition.getParameters());
             Configuration[] confsGenUnderCondition = generateConfigurations(paramsValuesForThisCondition);
             for(Configuration confGenUnderCondition: confsGenUnderCondition)
                 confGenUnderCondition.put(paramValue.getParameter(), paramValue.getValue());
             configurations.addAll(Arrays.asList(confsGenUnderCondition));
         }
         
         return configurations.toArray(new Configuration[]{});
     }
 
     /**
      * All possible combinations of parameters and their values are generated
      * and stored as {@link Configuration} instances. This method is called by
      * and only by {@link #generateConfigurations(Map,ParamGenerationCondition[])}.
      * 
      * @param paramsValues
      *            Mappings of parameters with their values.
      * @return All possible combinations of parameters and values, stored as
      *         {@link Configuration} instances.
      */
     private static Configuration[] generateConfigurations(Map<String, String[]> paramsValues) {
 
         List<String> keysWithValues = new ArrayList<String>();
         List<String> keysWithNoValues = new ArrayList<String>();
         for (String paramKey : paramsValues.keySet())
             if (paramsValues.get(paramKey).length > 0)
                 keysWithValues.add(paramKey);
             else
                 keysWithNoValues.add(paramKey);
 
         String[][] valuesArrays = new String[keysWithValues.size()][];
         for (int keyIndex = 0; keyIndex < keysWithValues.size(); keyIndex++)
             valuesArrays[keyIndex] = paramsValues.get(keysWithValues.get(keyIndex));
 
         String[][] combinedValues = Util.Collections.product(valuesArrays, String.class);
 
         Configuration[] configurations = new Configuration[combinedValues.length];
         for (int confIndex = 0; confIndex < configurations.length; confIndex++) {
             String[] configValues = combinedValues[confIndex];
             if (configValues.length != keysWithValues.size())
                 throw new Error("Inconsistency, the 'cartesian product' generated an array of values with length '"
                                 + configValues + "'" + "that differs from the amount of keys with values detected '"
                                 + keysWithValues.size());
             Configuration configuration = new Configuration();
             for (int paramIndex = 0; paramIndex < keysWithValues.size(); paramIndex++)
                 configuration.setProperty(keysWithValues.get(paramIndex), configValues[paramIndex]);
             for (int noValueParamIndex = 0; noValueParamIndex < keysWithNoValues.size(); noValueParamIndex++)
                 configuration.setProperty(keysWithNoValues.get(noValueParamIndex), new String());
             configurations[confIndex] = configuration;
         }
 
         return configurations;
     }
 
     /**
      * Used by {@link #extractAllParamValues(Properties, String)}, this methods
      * analyzes the input string to check whether it contains one or more range
      * definitions. If so, it will generate all the values in the range and will
      * add them to the input string, separated by the separator input string.
      * 
      * @param paramValue
      *            Initial value to check, where ranges definition can be
      *            included.
      * @param separator
      *            How values are separated in the incoming (and resulting)
      *            string.
      * @return The initial string if no range was found, or the same string
      *         where ranges have been replaced by the values defined by those
      *         ranges.
      * @throws ConfigurationException
      *             if some range definition was found but no value could be
      *             generated (see method
      *             {@link #convertRangeDef(String, String)}.
      */
     private static String checkAndGenRanges(String paramValue, String separator) throws ConfigurationException {
         Matcher matcher = rangeDefPattern.matcher(paramValue);
         if (matcher.find()) {
             matcher.reset();
             StringBuffer stringBuffer = new StringBuffer();
             while (matcher.find()) {
                 String range = convertRangeDef(matcher.group(), separator);
                 matcher.appendReplacement(stringBuffer, range);
             }
             matcher.appendTail(stringBuffer);
             return stringBuffer.toString();
         } else
             return paramValue;
     }
 
     /**
      * Generates all the values for a range definition, and returns them in a
      * new string with the corresponding separator.
      * 
      * @param rangeDef
      *            The range definition.
      * @param separator
      *            String (usually only one char) that sets apart the values
      *            generated in the string returned.
      * @return All the value included in the range definition, separated by a
      *         separator.
      * @throws ConfigurationException
      *             if the generation process failed because the range definition
      *             was incorrect. This could happen if some non numeric value is
      *             given, or the final value cannot be reached from the original
      *             value in the increments included in the definition.
      */
     private static String convertRangeDef(String rangeDef, String separator) throws ConfigurationException {
 
         // Getting rid of enclosing brackets '[' ']' and all white spaces
         String range = rangeDef.replaceAll("[\\s\\[\\]]", "");
         String[] rangeVals = range.split(":");
 
         double firstValue = 0;
         double lastValue = 0;
         double increment = 0;
 
         try {
             firstValue = Double.parseDouble(rangeVals[0]);
             lastValue = Double.parseDouble(rangeVals[2]);
             increment = Double.parseDouble(rangeVals[1]);
         } catch (NumberFormatException exception) {
             throw new ConfigurationException("Could not parse value in range definition " + rangeDef, exception);
         }
 
         if ((lastValue - firstValue) % increment != 0)
             throw new ConfigurationException("Invalid range definition: "
                                             + range + ", (" + lastValue + "-" + firstValue + ")%"
                                             + increment + " should return cero");
 
         if ((lastValue - firstValue) / increment < 0)
             throw new ConfigurationException("Invalid range definition: "
                                             + range + ", cannot reach " + lastValue + " from "
                                             + firstValue + " in intervals of " + increment);
 
         String extendedRange = numberToString(firstValue) + "";
         double presentValue = firstValue;
         while (presentValue != lastValue) {
             presentValue += increment;
             extendedRange += separator + numberToString(presentValue);
         }
 
         return extendedRange;
     }
 
     // The idea is to represent double numbers with no decimals (e.g. 32423.0)
     // as integers, i.e.
     // getting rid of the ending '.0'. If the number has decimals, then they are
     // respected.
     private static String numberToString(double number) {
         long rounded = Math.round(number);
         if (rounded == number)
             return rounded + "";
         else
             return number + "";
     }
 
     /**
      * Save all parameters as pairs 'name=value' into the file passed as input.
      * A commented line (i.e. started by '#') will be written at the beginning
      * of the file with the present date.
      * 
      * @param destFile
      *            File to write the parameters names and values to.
      * @throws IOException
      *             if some I/O error occurred wehn opening, writing to, or
      *             closing the file.
      */
     public void saveInFile(File destFile) throws IOException {
         SortedSet<String> orderedParams = new TreeSet<String>(getParameterNames());
         PrintWriter writer = new PrintWriter(new FileWriter(destFile));
         writer.println("# " + new Date().toString() + " #");
         for (String param : orderedParams)
             writer.println(param + "=" + super.getProperty(param));
         writer.close();
     }
 
 }
