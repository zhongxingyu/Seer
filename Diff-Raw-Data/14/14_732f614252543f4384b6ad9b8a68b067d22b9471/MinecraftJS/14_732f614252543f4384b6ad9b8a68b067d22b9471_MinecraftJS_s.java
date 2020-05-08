 package mave.minecraftjs;
 
 import java.util.List;
 import java.io.*;
 import java.lang.reflect.*;
 import java.util.ArrayList;
 
 import org.bukkit.command.*;
 import org.bukkit.plugin.InvalidDescriptionException;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class MinecraftJS extends JavaPlugin
 {
 	public static MinecraftJS m_singleton = null;
 	
 	public static boolean m_bInternalConstruction = false;
 	
 	public static Script m_currentScript = null;
 	private List<Script> m_lstScripts = new ArrayList<Script>();
 	
 	public MinecraftJS()
 	{
 		m_singleton = this;
 	}
 	
 	public void onEnable()
	{
 		File pluginsDir = new File("plugins/");
 		for (String s : pluginsDir.list())
 		{
 			if (s.endsWith(".js"))
 			{
 				try
 				{
 					Script script = new Script();
 					script.load(new File("plugins/", s));
 					m_lstScripts.add(script);
 					
 				}
 				catch (FileNotFoundException e)
 				{
 					e.printStackTrace();
 				}
 				catch (IOException e)
 				{
 					e.printStackTrace();
 				}
 				catch (InvalidDescriptionException e)
 				{
 					System.err.println(e.getMessage());
 				}
 			}
 		}
 	}
 	
 	public void onDisable()
 	{
 	}
 	
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
 	{
 		return false;
 	}
 	
 	@SuppressWarnings("rawtypes")
 	public void addCommand(Command command) throws ClassNotFoundException, SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, InstantiationException, InvocationTargetException
 	{
 		Class c = Class.forName("org.bukkit.craftbukkit.CraftServer");
 		Field f = c.getDeclaredField("commandMap");
 		f.setAccessible(true);
 		CommandMap cmdMap = (CommandMap)f.get((Object)getServer());
 		cmdMap.register(command.getName(), command);
 	}
 }
