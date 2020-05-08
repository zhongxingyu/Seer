 package com.censoredsoftware.demigods.engine.data;
 
 import com.censoredsoftware.demigods.engine.Demigods;
 import com.censoredsoftware.demigods.engine.language.TranslationManager;
 import com.google.common.collect.Maps;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import redis.clients.jedis.Jedis;
 import redis.clients.jedis.JedisPool;
 import redis.clients.jedis.JedisPoolConfig;
 import redis.clients.jedis.exceptions.JedisConnectionException;
 import redis.clients.johm.JOhm;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class DataManager
 {
 	// The Redis DB
 	private static JedisPool jedisPool;
 
 	// Temp Data
 	private static Map<String, HashMap<String, Object>> tempData;
 
 	public DataManager()
 	{
 		// Create Data Instances
 		jedisPool = new JedisPool(new JedisPoolConfig(), Demigods.config.getSettingString("redis.host"), Demigods.config.getSettingInt("redis.port"));
 		if(Demigods.config.isSettingSet("redis.password")) jedisPool.getResource().auth(Demigods.config.getSettingString("redis.password"));
 		tempData = Maps.newHashMap();
 
 		// Create Persistence
 		new JOhm();
 		JOhm.setPool(jedisPool);
 	}
 
 	public static boolean isConnected()
 	{
 		try
 		{
 			jedisPool.getResource();
 			return true;
 		}
 		catch(JedisConnectionException ignored)
 		{}
 		return false;
 	}
 
 	public static void disconnect()
 	{
 		try
 		{
 			Jedis jedis = jedisPool.getResource();
 			jedis.disconnect();
 			jedisPool.returnBrokenResource(jedis);
 			jedisPool.destroy();
 		}
 		catch(Exception ignored)
 		{}
 	}
 
 	public static void save()
 	{
 		Jedis jedis = jedisPool.getResource();
 		jedis.bgsave();
 		jedisPool.returnResource(jedis);
 	}
 
 	public static void flushData()
 	{
 		// Clear the data
 		Jedis jedis = jedisPool.getResource();
 		jedis.flushDB();
 		jedisPool.returnResource(jedis);
 		tempData.clear();
 
 		// Kick everyone
 		for(Player player : Bukkit.getOnlinePlayers())
 		{
 			player.kickPlayer(ChatColor.GREEN + Demigods.text.getText(TranslationManager.Text.DATA_RESET_KICK));
 		}
 
 		// Reload the server
 		Bukkit.getServer().reload();
 	}
 
 	public static boolean hasKeyTemp(String key, String subKey)
 	{
 		return tempData.containsKey(key) && tempData.get(key).containsKey(subKey);
 	}
 
 	public static Object getValueTemp(String key, String subKey)
 	{
 		if(tempData.containsKey(key)) return tempData.get(key).get(subKey);
 		else return null;
 	}
 
 	public static void saveTemp(String key, String subKey, Object value)
 	{
 		if(!tempData.containsKey(key)) tempData.put(key, new HashMap<String, Object>());
 		tempData.get(key).put(subKey, value);
 	}
 
 	public static void removeTemp(String key, String subKey)
 	{
 		if(tempData.containsKey(key) && tempData.get(key).containsKey(subKey)) tempData.get(key).remove(subKey);
 	}
 
 	public static void saveTimed(String key, String subKey, Object data, Integer seconds)
 	{
 		// Remove the data if it exists already
 		TimedData.Util.remove(key, subKey);
 
 		// Create and save the timed data
 		TimedData timedData = new TimedData();
 		timedData.setKey(key);
 		timedData.setSubKey(subKey);
 		timedData.setData(data.toString());
 		timedData.setSeconds(seconds);
 		TimedData.save(timedData);
 	}
 
 	public static void removeTimed(String key, String subKey)
 	{
 		TimedData.Util.remove(key, subKey);
 	}
 
 	public static boolean hasTimed(String key, String subKey)
 	{
 		return TimedData.Util.find(key, subKey) != null;
 	}
 
 	public static Object getTimedValue(String key, String subKey)
 	{
 		return TimedData.Util.find(key, subKey).getData();
 	}
}
