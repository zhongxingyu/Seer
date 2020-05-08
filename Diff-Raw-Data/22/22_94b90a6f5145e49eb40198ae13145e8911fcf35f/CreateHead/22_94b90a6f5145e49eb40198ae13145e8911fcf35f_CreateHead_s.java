 package no.runsafe.toybox.command;
 
 import no.runsafe.framework.command.player.PlayerCommand;
 import no.runsafe.framework.server.item.RunsafeItemStack;
 import no.runsafe.framework.server.item.meta.RunsafeSkullMeta;
import no.runsafe.framework.server.material.RunsafeMaterial;
 import no.runsafe.framework.server.player.RunsafePlayer;
import org.bukkit.Material;
 
 import java.util.HashMap;
 
 public class CreateHead extends PlayerCommand
 {
 	public CreateHead()
 	{
 		super("createhead", "Creates the head of a player", "runsafe.toybox.createhead", "player");
 	}
 
 	@Override
 	public String OnExecute(RunsafePlayer executor, HashMap<String, String> parameters)
 	{
		RunsafeItemStack heads = new RunsafeItemStack(Material.SKULL_ITEM.getId(), 1, (short) 3);
 		RunsafeSkullMeta meta = (RunsafeSkullMeta) heads.getItemMeta();
		meta.setOwner(parameters.get("player"));
		executor.sendMessage("Creating head of player " + parameters.get("player"));
		heads.setItemMeta(meta);
 		executor.give(heads);
 
		return null;
 	}
 }
