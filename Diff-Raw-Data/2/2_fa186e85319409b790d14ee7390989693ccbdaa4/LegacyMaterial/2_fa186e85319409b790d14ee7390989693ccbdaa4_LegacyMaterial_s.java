 package no.runsafe.framework.internal;
 
 import no.runsafe.framework.api.log.IConsole;
 import org.bukkit.Material;
 
 public class LegacyMaterial
 {
 	public LegacyMaterial(IConsole console)
 	{
 		this.console = console;
 	}
 
 	public void generate()
 	{
 		for (Material material : Material.values())
 		{
			console.logInformation(String.format("%s(%d, Material.%s", material.getId(), material.name()));
 		}
 	}
 	private final IConsole console;
 }
