 package cc.warlock.core.script;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import cc.warlock.core.client.IWarlockClient;
 import cc.warlock.core.script.internal.FilesystemScriptProvider;
 
 
 public class ScriptEngineRegistry {
 
 	private static ArrayList<IScriptEngine> engines = new ArrayList<IScriptEngine>();
 	private static ArrayList<IScriptProvider> providers = new ArrayList<IScriptProvider>();
 	
 	static {
 		// force initialization of filesystem script provider
 		FilesystemScriptProvider.instance();
 	}
 	
 	public static void addScriptProvider (IScriptProvider provider)
 	{
 		providers.add(provider);
 	}
 	
 	public static void removeScriptProvider (IScriptProvider provider)
 	{
 		if (providers.contains(provider))
 			providers.remove(provider);
 	}
 	
 	public static void addScriptEngine (IScriptEngine engine)
 	{
 		engines.add(engine);
 	}
 	
 	public static void removeScriptEngine (IScriptEngine engine)
 	{
 		engines.remove(engine);
 	}
 	
 	public static List<IScriptEngine> getScriptEngines ()
 	{
 		return engines;
 	}
 	
 	public static List<IScriptProvider> getScriptProviders ()
 	{
 		return providers;
 	}
 	
 	public static IScriptEngine getScriptEngine (String engineId)
 	{
 		for (IScriptEngine engine : engines)
 		{
 			if (engine.getScriptEngineId().equals(engineId))
 				return engine;
 		}
 		
 		return null;
 	}
 	
 	public static IScript startScript (String scriptName, IWarlockClient client, String[] arguments)
 	{
 		for (IScriptProvider provider : providers)
 		{
 			for (IScriptInfo scriptInfo : provider.getScriptInfos())
 			{
 				if (scriptInfo.getScriptName().equals(scriptName)) {
 					IScript script = provider.startScript(scriptInfo, client, arguments);
 					if(script != null)
 						return script;
 				}
 			}
 		}
		client.getDefaultStream().echo("Could not find script \"" + scriptName + "\"");
 		return null;
 	}
 	
 	public static List<IScript> getRunningScripts ()
 	{
 		ArrayList<IScript> scripts = new ArrayList<IScript>();
 		for (IScriptEngine engine : engines)
 		{
 			scripts.addAll(engine.getRunningScripts());
 		}
 		return scripts;
 	}
 }
