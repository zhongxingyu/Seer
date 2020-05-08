 package event;
 
 import java.lang.reflect.Method;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import entity.Entity;
 import game.Game;
 
 public class EntityEvent
 {
 	public final Entity owner;
 	public String target;
 	public String targetFunction;
 	public boolean onlyOnce;
 	public JSONArray parameters;
 	
 	public EntityEvent(Entity owner, JSONObject o)
 	{
 		this.owner = owner;
 		try
 		{
 			this.target = o.getString("target");
 			this.targetFunction = o.getString("function");
 			this.onlyOnce = o.getBoolean("onlyOnce");
 			this.parameters = o.getJSONArray("params");
 		}
 		catch (JSONException e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	public boolean trigger()
 	{
 		Entity eTarget = Game.getCurrentGame().getCurrentWorld().getEntity(target);
 		Method[] methods = eTarget.getClass().getMethods();
 		
 		Object[] o = new Object[parameters.length()];
 		
 		try
 		{
 			for(int i = 0; i < parameters.length(); i++)
 			{
 				Object obj = parameters.get(i);
 				if(obj instanceof String && ((String)obj).startsWith("@"))
					o[i] = owner.customValues.get(obj);
 				else o[i] = obj;				
 			}
 			
 			for(Method m : methods)
 				if(m.getName().equals("targetFunction"))
 					m.invoke(null, o);
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			return false;
 		}
 		
 		return true;
 	}
 }
