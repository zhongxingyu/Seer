 package no.runsafe.framework.api.command.argument;
 
 import no.runsafe.framework.api.command.ICommandExecutor;
 import no.runsafe.framework.api.player.IPlayerUniverse;
 
 import javax.annotation.Nullable;
 
 public class AutoWorldArgument extends WorldArgument
 {
 	public AutoWorldArgument()
 	{
		super(true);
 	}
 
 	@Nullable
 	@Override
 	public String expand(ICommandExecutor context, @Nullable String value)
 	{
 		if (value == null && context instanceof IPlayerUniverse)
 			return ((IPlayerUniverse) context).getWorldName();
 		return super.expand(context, value);
 	}
 }
