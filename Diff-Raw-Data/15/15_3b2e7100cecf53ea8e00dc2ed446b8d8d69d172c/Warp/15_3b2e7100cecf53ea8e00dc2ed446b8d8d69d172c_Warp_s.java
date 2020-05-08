 package no.runsafe.warpdrive.commands;
 
 import no.runsafe.framework.event.player.IPlayerRightClickSign;
 import no.runsafe.framework.output.ChatColour;
 import no.runsafe.framework.output.IOutput;
 import no.runsafe.framework.server.RunsafeLocation;
 import no.runsafe.framework.server.block.RunsafeSign;
import no.runsafe.framework.server.item.RunsafeItemStack;
 import no.runsafe.framework.server.player.RunsafePlayer;
 import no.runsafe.framework.timer.IScheduler;
 import no.runsafe.warpdrive.Engine;
 import no.runsafe.warpdrive.PlayerTeleportCommand;
 import no.runsafe.warpdrive.database.WarpRepository;
 import org.apache.commons.lang.StringUtils;
 
 import java.util.HashMap;
 
 public class Warp extends PlayerTeleportCommand implements IPlayerRightClickSign
 {
 	public Warp(WarpRepository repository, IOutput output, IScheduler scheduler, Engine engine)
 	{
 		super("warp", "Teleports you to a predefined warp location", "runsafe.warp.use.<destination>", scheduler, engine, "destination");
 		warpRepository = repository;
 		console = output;
 	}
 
 	@Override
 	public PlayerTeleport OnAsyncExecute(RunsafePlayer player, HashMap<String, String> parameters)
 	{
 		PlayerTeleport target = new PlayerTeleport();
 		target.force = true;
 		target.player = player;
 		target.location = warpRepository.GetPublic(parameters.get("destination").toLowerCase());
 		if (target.location == null)
 			target.message = String.format("The warp %s does not exist.", parameters.get("destination"));
 		return target;
 	}
 
 	@Override
 	public String getUsage()
 	{
 		return String.format("\nExisting warps: %1$s", StringUtils.join(warpRepository.GetPublicList(), ", "));
 	}
 
 	@Override
	public boolean OnPlayerRightClickSign(RunsafePlayer player, RunsafeItemStack itemStack, RunsafeSign sign)
 	{
 		if (!sign.getLine(0).contains(signHeader))
 			return true;
 
 		String name = sign.getLine(1).toLowerCase();
 		RunsafeLocation destination = warpRepository.GetPublic(name);
 		if (destination == null)
 		{
 			console.write(String.format("%s used a invalid warp sign %s.", player.getName(), name));
 			return false;
 		}
 		if (!player.hasPermission("runsafe.warpsign.use.*")
 			&& !player.hasPermission(String.format("runsafe.warpsign.use.%s", name)))
 			return false;
 
 		player.teleport(destination);
 		return false;
 	}
 
 	private final WarpRepository warpRepository;
 	private final IOutput console;
 	public static final String signHeader = "[" + ChatColour.BLUE.toBukkit() + "warp" + ChatColour.RESET.toBukkit() + "]";
 	public static final String signTag = "[warp]";
 }
