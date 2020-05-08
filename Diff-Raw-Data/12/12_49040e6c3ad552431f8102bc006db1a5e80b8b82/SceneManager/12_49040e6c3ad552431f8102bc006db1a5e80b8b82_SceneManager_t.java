 package com.nhydock.beatshot.util;
 
 import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.ObjectMap;
 
 /**
  * Very simple manager for storing all saved scene names.  Much like a route
  * manager for web development, except it's for screens and games.
  * 
  * @author nhydock
  */
 public class SceneManager {
 
	private static ObjectMap<String, Class<? extends Screen>> map;
	
	static
	{
		map = new ObjectMap<String, Class<? extends Screen>>();
	}
 	
 	/**
 	 * Signs a new screen into the manager
 	 * @param name
 	 * @param cls
 	 */
 	public static void register(String name, Class<? extends Screen> cls)
 	{
 		map.put(name, cls);
 	}
 	
 	/**
 	 * Generate a new screen from a registered name
 	 * @param name
 	 * @return
 	 * @throws NullPointerException
 	 */
 	public static Screen create(String name) throws NullPointerException
 	{
 		if (map.containsKey(name))
 		{
 			Class<? extends Screen> c = map.get(name);
 			Screen s;
 			try {
 				s = c.newInstance();
 				return s;
 			} catch (InstantiationException | IllegalAccessException e) {
 				e.printStackTrace();
 			}
 		}
 		else
 		{
 			System.out.println(name + " is not a registered Scene");
 		}
 		throw (new NullPointerException());
 	}
 }
