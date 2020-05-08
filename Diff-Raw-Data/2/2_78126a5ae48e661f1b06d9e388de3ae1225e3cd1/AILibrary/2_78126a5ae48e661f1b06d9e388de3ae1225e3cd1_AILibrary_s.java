 package no.runsafe.eventengine.libraries;
 
 import no.runsafe.framework.RunsafePlugin;
 import no.runsafe.framework.api.lua.*;
 import no.runsafe.framework.minecraft.RunsafeServer;
 import no.runsafe.framework.minecraft.RunsafeWorld;
 import no.runsafe.framework.minecraft.event.player.RunsafePlayerFakeChatEvent;
 import no.runsafe.framework.minecraft.player.RunsafeFakePlayer;
 import org.luaj.vm2.LuaError;
 import org.luaj.vm2.LuaTable;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class AILibrary extends Library
 {
 	public AILibrary(RunsafePlugin plugin)
 	{
 		super(plugin, "ai");
 	}
 
 	@Override
 	protected LuaTable getAPI()
 	{
 		LuaTable lib = new LuaTable();
 		lib.set("create", new IntegerFunction()
 		{
 			@Override
 			public Integer run(FunctionParameters parameters)
 			{
 				return createAI(parameters.getString(0), parameters.getString(1), parameters.getWorld(2));
 			}
 		});
 		lib.set("speak", new VoidFunction()
 		{
 			@Override
 			protected void run(FunctionParameters parameters)
 			{
 				speak(parameters.getInt(0), parameters.getString(1));
 			}
 		});
 		return lib;
 	}
 
 	private static void speak(int id, String message)
 	{
 		if (ai.size() <= id)
 			throw new LuaError("No AI with given ID.");
 
 		RunsafePlayerFakeChatEvent event = new RunsafePlayerFakeChatEvent(AILibrary.ai.get(id), message);
 		event.Fire();
 
		if (!event.getCancelled())
 			RunsafeServer.Instance.broadcastMessage(String.format(event.getFormat(), event.getPlayer().getName(), event.getMessage()));
 	}
 
 	private static int createAI(String name, String group, RunsafeWorld world)
 	{
 		RunsafeFakePlayer newAI = new RunsafeFakePlayer(name);
 		newAI.getGroups().add(group);
 		newAI.setWorld(world);
 
 		AILibrary.ai.add(newAI);
 		return AILibrary.ai.size() - 1;
 	}
 
 	private static final List<RunsafeFakePlayer> ai = new ArrayList<RunsafeFakePlayer>();
 }
