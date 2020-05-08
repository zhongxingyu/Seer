 package no.runsafe.toybox.command;
 
 import no.runsafe.framework.command.player.PlayerCommand;
 import no.runsafe.framework.minecraft.Item;
 import no.runsafe.framework.server.RunsafeServer;
 import no.runsafe.framework.server.item.RunsafeItemStack;
 import no.runsafe.framework.server.item.meta.RunsafeSkullMeta;
 import no.runsafe.framework.server.player.RunsafeAmbiguousPlayer;
 import no.runsafe.framework.server.player.RunsafePlayer;
 
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
 		RunsafePlayer player = RunsafeServer.Instance.getPlayer(parameters.get("player"));
 		if (player instanceof RunsafeAmbiguousPlayer)
 			return player.toString();
 
 		RunsafeItemStack heads = Item.Decoration.Head.Human.getItem();
 		heads.setAmount(1);
 		RunsafeSkullMeta meta = (RunsafeSkullMeta) heads.getItemMeta();
 		meta.setOwner(player.getName());
 		heads.setItemMeta(meta);
 		executor.give(heads);
 
 		return "Creating the head of " + player.getName();
 	}
 }
