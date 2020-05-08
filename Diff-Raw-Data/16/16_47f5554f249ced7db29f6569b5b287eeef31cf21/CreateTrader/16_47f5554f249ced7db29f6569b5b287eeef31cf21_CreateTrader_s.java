 package no.runsafe.ItemControl.trading.commands;
 
 import no.runsafe.ItemControl.trading.TradingHandler;
import no.runsafe.framework.api.command.IBranchingExecution;
 import no.runsafe.framework.api.command.argument.IArgumentList;
 import no.runsafe.framework.api.command.player.PlayerCommand;
 import no.runsafe.framework.api.player.IPlayer;
 
public class CreateTrader extends PlayerCommand implements IBranchingExecution
 {
 	public CreateTrader(TradingHandler handler)
 	{
 		super("create", "Create a trader", "runsafe.traders.create");
 		this.handler = handler;
 	}
 
 	@Override
 	public String OnExecute(IPlayer executor, IArgumentList parameters)
 	{
 		// Spawn a merchant at the players location.
 		handler.spawnTrader(executor.getLocation());
 		return "&eMerchant spawned!";
 	}
 
 	private final TradingHandler handler;
 }
