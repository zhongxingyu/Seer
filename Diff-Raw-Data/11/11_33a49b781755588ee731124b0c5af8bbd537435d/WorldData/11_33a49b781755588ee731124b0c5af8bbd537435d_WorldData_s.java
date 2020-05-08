 package com.censoredsoftware.demigods.engine.data;
 
 import org.bukkit.Bukkit;
 import org.bukkit.World;
 
import com.censoredsoftware.censoredlib.helper.MapDBFile;

 public class WorldData extends MapDBFile
 {
 	private final String worldName;
 
 	WorldData(String worldName, String worldFolder)
 	{
		super("demigods.dat", worldFolder + "\\demigods\\");
 		this.worldName = worldName;
 	}
 
 	public World getWorld()
 	{
 		return Bukkit.getWorld(worldName);
 	}
 
 	@Override
 	public boolean delete()
 	{
 		Data.removeWorld(worldName);
 		return super.delete();
 	}
}
