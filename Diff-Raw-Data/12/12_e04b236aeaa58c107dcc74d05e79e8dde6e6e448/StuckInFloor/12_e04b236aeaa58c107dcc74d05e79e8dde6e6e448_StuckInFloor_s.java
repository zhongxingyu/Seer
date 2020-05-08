 package net.slipcor.pvparena.modules.fixes;
 
 import java.util.HashMap;
 
import org.bukkit.Chunk;
import org.bukkit.World;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.event.player.PlayerTeleportEvent;
 import net.slipcor.pvparena.arena.Arena;
 import net.slipcor.pvparena.neworder.ArenaModule;
 
 public class StuckInFloor extends ArenaModule {
 
 	public StuckInFloor() {
 		super("StuckInFloor");
 	}
 
 	@Override
 	public String version() {
		return "v0.8.8.8";
 	}
 
 	@Override
 	public void addSettings(HashMap<String, String> types) {
 		types.put("fix.stuckInFloor", "boolean");
 	}
 
 	@Override
 	public void configParse(Arena arena, YamlConfiguration config, String type) {
 		config.addDefault("fix.stuckInFloor", Boolean.valueOf(false));
 		config.options().copyDefaults(true);
 	}
 
 	@Override
 	public void onPlayerTeleport(Arena arena, PlayerTeleportEvent event) {
 		if (arena.cfg.getBoolean("fix.stuckInFloor")) {
			World world = event.getTo().getWorld();
			Chunk chunk = world.getChunkAt(event.getTo());
			int x = chunk.getX();
			int z = chunk.getZ();
			world.refreshChunk(x, z);
 		}
 	}
 }
