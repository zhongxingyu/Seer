 package mta.devweb.bitcoinbuddy.model.db;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisShardInfo;
 
 
 /**
  * This class handles access to redis Db.
  * @author Dan
  *
  */
 public class RedisAccess {
 	
 	private Jedis jedis;
 	
 	/**
 	 * Connect to redis via VCAP_SERVICES or via local instance for debugging
 	 * @return
 	 */
 	public  Jedis connect() {
 		String jsonEnvVars = java.lang.System.getenv("VCAP_SERVICES");
         if(jsonEnvVars != null){
 			this.jedis = parseUrlFromEnvVarsAndConnect(jsonEnvVars); 
 		}
         else {
         	
         	this.jedis = new Jedis("ec2-23-22-114-99.compute-1.amazonaws.com",6379);
         }
         return this.jedis;
 	}
 	
 	public Jedis getJedis() {
 		return jedis;
 	}
 	
 	/**
 	 * Parse connection to redis out of VCAP_SERVICES
 	 * @param jsonEnvVars
 	 * @return
 	 */
 	 private Jedis parseUrlFromEnvVarsAndConnect(String jsonEnvVars) {
 			String url = "";
 			try {
 				JSONObject jsonObject = new JSONObject(jsonEnvVars);
 				JSONArray jsonArray = jsonObject.getJSONArray("redis-2.2");
 				jsonObject = jsonArray.getJSONObject(0);
 				jsonObject = jsonObject.getJSONObject("credentials");
				String name 	=  jsonObject.getString("name");
				System.out.println("redis name: " + name);
 				String host 	=  jsonObject.getString("hostname");
				System.out.println("redis host: " + host);
				int port 	=  jsonObject.getInt("port");
				System.out.println("redis port: " + port);
				String pass 	=  jsonObject.getString("password");
				System.out.println("redis pass: " + pass);
				JedisShardInfo info = new JedisShardInfo(host, port, name);
				Jedis jedis = new Jedis(info);
				jedis.auth(pass);
				return  jedis;
 			} 
 			catch (JSONException e) {
 				System.err.println("Conn.connect: " + e.getMessage());
 			}
 			
 			return null;
 		}
 
 }
