 package com.gentics.cr.configuration;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Hashtable;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Vector;
 import java.util.Map.Entry;
 
 /**
  * Stores a Configuration tree
  * 		- Keys are stored in Upper Case
  * 
  * Last changed: $Date: 2010-04-01 15:25:54 +0200 (Do, 01 Apr 2010) $
  * @version $Revision: 545 $
  * @author $Author: supnig@constantinopel.at $
  *
  */
 public class GenericConfiguration implements Serializable{
 	
 	/**
 	 * Version ID for Serialization
 	 */
 	private static final long serialVersionUID = 2984152538932729448L;
 	protected Properties properties;
 	protected Hashtable<String,GenericConfiguration> subconfigs;
 	
 	/**
 	 * Create new instance of Generic Configuration
 	 */
 	public GenericConfiguration()
 	{
 		
 	}
 	
 	/**
 	 * Returns the containing properties as flat Properties class
 	 * 			- will not resolve to sub configurations
 	 * @return properties or null if there are no containing properties
 	 */
 	public Properties getProperties()
 	{
 		return(this.properties);
 	}
 	
 	/**
 	 * Returns rebuilt property tree as flat property file
 	 * @return flattened property file
 	 */
 	public Properties getRebuiltPropertyTree()
 	{
 		Properties ret = new Properties();
 		if(this.properties!=null)
 		{
 			for(Entry<Object,Object> e:this.properties.entrySet())
 			{
 				ret.put(e.getKey(), e.getValue());
 			}
 		}
 		if(this.subconfigs!=null)
 		{
 			for(Entry<Object,Object> e:this.properties.entrySet())
 			{
 				String subConfKey = (String)e.getKey();
 				Object subVal = e.getValue();
 				if(subVal!=null && subVal instanceof GenericConfiguration)
 				{
 					for(Entry<Object,Object> se:((GenericConfiguration)subVal).getRebuiltPropertyTree().entrySet())
 					{
 						String K = subConfKey+"."+se.getKey();
 						ret.put(K, se.getValue());
 					}
 				}
 				else
 				{
 					ret.put(subConfKey, subVal);
 				}
 			}
 		}
 		return(ret);
 	}
 	
 	/**
 	 * Returns the containing sub configurations
 	 * 			
 	 * @return hashtable of configurations with keys or null if there are no containing sub configs
 	 */
 	public Hashtable<String,GenericConfiguration> getSubConfigs()
 	{
 		return this.subconfigs;
 	}
 	
 	/**
 	 * Returns the containing sub configurations sorted by key
 	 * 			
 	 * @return hashtable of configurations with keys or null if there are no containing sub configs
 	 */
 	public synchronized Map<String,GenericConfiguration> getSortedSubconfigs()
 	{
		Map<String,GenericConfiguration> ret = null;
 		
		if(this.subconfigs!=null)
		{
			ret=Collections.synchronizedMap(new LinkedHashMap<String,GenericConfiguration>(this.subconfigs.size()));
			
			Vector<String> v = new Vector<String>(this.subconfigs.keySet());
		    Collections.sort(v);
		    for(String key:v)
		    {
		    	ret.put(key, this.subconfigs.get(key));
		    }
		}
 	    return(ret);
 
 	}
 	
 	/**
 	 * Returns size of properties in this instance
 	 * 		- does not count sub configurations
 	 * @return count
 	 */
 	public int getPropertySize()
 	{
 		if(this.properties!=null)
 		{
 			return this.properties.size();
 		}
 		return(0);
 	}
 	
 	/**
 	 * Returns size of sub configs in this instance
 	 * 		- does not count properties
 	 * @return count
 	 */
 	public int getSubConfigSize()
 	{
 		if(this.subconfigs!=null)
 		{
 			return(this.subconfigs.size());
 		}
 		return(0);
 	}
 	
 	/**
 	 * Returns size of properties in this instance
 	 * 		- does count sub configurations
 	 * @return count
 	 */
 	public int getChildSize()
 	{
 		return(getSubConfigSize()+getPropertySize());
 	}
 	
 	/**
 	 * Gets flat properties in the resolved instance as sorted collection
 	 * @param key
 	 * @return collection of properties or null if no properties set or resolved object is not able to contain properties
 	 */
 	public ArrayList<String> getPropertiesAsSortedCollection(String key)
 	{
 		Object obj = get(key);
 		if(obj!=null && obj instanceof GenericConfiguration)
 		{
 			return ((GenericConfiguration)obj).getPropertiesAsSortedCollection();
 		}
 		return(null);
 	}
 	
 	/**
 	 * Gets flat properties in this instance as sorted collection
 	 * @return Collection of String or null if there are no properties
 	 */
 	@SuppressWarnings("unchecked")
 	public ArrayList<String> getPropertiesAsSortedCollection()
 	{
 		if(this.properties!=null)
 		{
 			ArrayList<String> ret = new ArrayList<String>(this.properties.size());
 			Vector v = new Vector(this.properties.keySet());
 			Collections.sort(v);
 			for(Object k:v)
 			{
 				ret.add(this.properties.getProperty((String)k));
 			}
 			return(ret);
 		}
 		return(null);
 
 	}
 	
 	/**
 	 * Gets the property to the given key<br />
 	 * &nbsp;- will resolve sub properties like "conf1.A.1.1" to<br />
 	 * &nbsp;&nbsp;this config<br />
 	 * &nbsp;&nbsp;&nbsp;-config config1<br />
 	 * &nbsp;&nbsp;&nbsp;&nbsp;-config A<br />
 	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;-config 1<br />
 	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;-config 1<br />
 	 * &nbsp;- will resolve properties like "conf1" to <br />
 	 * &nbsp;&nbsp;this config<br />
 	 * &nbsp;&nbsp;&nbsp;-property config1<br />
 	 * @param key
 	 * @return property value as string or a GenericConfiguration object if key points to a config
 	 */
 	public Object get(String key)
 	{
 		key=convertKey(key);
 		if(isSubKey(key))
 		{
 			if(this.subconfigs!=null)
 			{
 				GenericConfiguration subConf = this.subconfigs.get(getSubConfigKey(key));
 				if(subConf!=null)
 				{
 					return(subConf.get(getSubKey(key)));
 				}
 			}
 		}
 		else if(this.properties!=null || this.subconfigs!=null)
 		{
 			Object prop = null;
 			if(properties!=null)prop=properties.getProperty(key);
 			if(prop==null && subconfigs!=null)prop = subconfigs.get(key);
 			return(prop);
 		}
 		return null;
 	}
 	
 	/**
 	 * Wrapper for {@link #get(String)}
 	 * @param key
 	 * @return returns the result of {@link #get(String)} as a String
 	 */
 	
 	public String getString(String key) {
 		Object result = get(key);
 		if(result!=null)
 			return (String) result;
 		else
 			return null;
 	}
 	
 	
 	/**
 	 * Sets the property value to the given key<br />
 	 * &nbsp;- will resolve sub properties like "conf1.A.1.1" to<br />
 	 * &nbsp;&nbsp;this config<br />
 	 * &nbsp;&nbsp;&nbsp;-config config1<br />
 	 * &nbsp;&nbsp;&nbsp;&nbsp;-config A<br />
 	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;-config 1<br />
 	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;-config 1<br />
 	 * &nbsp;- will resolve properties like "conf1" to<br />
 	 * &nbsp;&nbsp;this config<br />
 	 * &nbsp;&nbsp;&nbsp;-property config1<br />
 	 * @param key Property key as string
 	 * @param value Property value as string
 	 * 
 	 */
 	public void set(String key, String value)
 	{
 		key=convertKey(key);
 		if(isSubKey(key))
 		{
 			if(this.subconfigs==null)this.subconfigs = new Hashtable<String,GenericConfiguration>();
 			String confKey = getSubConfigKey(key);
 			if(this.subconfigs.get(confKey)==null)this.subconfigs.put(confKey, new GenericConfiguration());
 			this.subconfigs.get(confKey).set(getSubKey(key), value);
 		}
 		else
 		{
 			if(this.properties==null)this.properties = new Properties();
 			this.properties.setProperty(key, value);
 		}
 	}
 	
 	/**
 	 * Returns true if key is resolving to a sub configuration
 	 * @param key
 	 * @return true if key is resolving to a sub configuration
 	 */
 	private boolean isSubKey(String key)
 	{
 		boolean ret = false;
 		if(key!=null)
 		{
 			String[] arr = key.split("\\.");
 			if(arr!=null && arr.length>1)
 			{
 				ret=true;
 			}
 		}
 		return (ret);
 	}
 	
 	/**
 	 * Gets the sub configuration key
 	 * @param key
 	 * @return the sub configuration key as string, null if the key does not resolve to a sub configuration
 	 */
 	private String getSubKey(String key)
 	{
 		String sub = null;
 		
 		if(key!=null)
 		{
 			String[] arr = key.split("\\.");
 			if(arr!=null && arr.length>1)
 			{
 				int firstpos = key.indexOf(".")+1;
 				if(key.length()>firstpos)
 				{
 					sub = key.substring(firstpos);
 				}
 			}
 		}
 		return(sub);
 	}
 	
 	/**
 	 * Gets the key under which the sub configuration is stored in the subconfigs hashmap
 	 * @param key
 	 * @return sub config key as string
 	 */
 	private String getSubConfigKey(String key)
 	{
 		String sub = null;
 		
 		if(key!=null)
 		{
 			String[] arr = key.split("\\.");
 			if(arr!=null && arr.length>1)
 			{
 				sub=arr[0];
 			}
 		}
 		return(sub);
 	}
 	
 	/**
 	 * Prepares key for storage and fetching
 	 * 		- converty key to upper case
 	 * @param key
 	 * @return
 	 */
 	private String convertKey(String key)
 	{
 		return key.toUpperCase();
 	}
 }
