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
			console.logInformation(String.format("%2$s(%1$d, Material.%2$s", material.getId(), material.name()));
 		}
 	}
 	private final IConsole console;
 }
