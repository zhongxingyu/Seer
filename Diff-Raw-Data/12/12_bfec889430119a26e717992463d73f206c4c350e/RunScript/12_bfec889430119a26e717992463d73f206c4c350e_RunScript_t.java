 package no.runsafe.eventengine.commands;
 
 import no.runsafe.eventengine.engine.Environment;
 import no.runsafe.framework.command.ExecutableCommand;
 import no.runsafe.framework.output.IOutput;
 import no.runsafe.framework.server.ICommandExecutor;
 import org.bukkit.plugin.Plugin;
import org.luaj.vm2.LuaError;
 import org.luaj.vm2.LuaValue;
 
 import java.io.File;
 import java.util.HashMap;
 
 public class RunScript extends ExecutableCommand
 {
 	public RunScript(Plugin eventEngine, IOutput output)
 	{
 		super("run", "Executes an LUA script", "runsafe.eventengine.run", "script");
 		this.output = output;
 
 		// Setup folders
 		this.path = String.format("plugins/%s/scripts/", eventEngine.getName());
 		if (!new File(this.path).mkdirs())
 			this.output.warning("Failed to create scripts directory at: " + this.path);
 	}
 
 	@Override
 	public String OnExecute(ICommandExecutor executor, HashMap<String, String> parameters)
 	{
 		String file = path + parameters.get("script") + ".lua";
 
 		if (!new File(file).exists())
 			return "&cScript not found.";
 
		try
		{
			Environment.global.get("dofile").call(LuaValue.valueOf(file));
		}
		catch (LuaError error)
		{
			return "&c" + error.getMessage();
		}

 		return "&2Script executed.";
 	}
 
 	private String path;
 	private IOutput output;
 }
