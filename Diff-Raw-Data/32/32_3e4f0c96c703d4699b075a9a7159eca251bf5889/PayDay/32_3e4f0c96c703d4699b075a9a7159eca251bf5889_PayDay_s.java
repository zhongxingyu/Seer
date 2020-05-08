 package me.greatman.Craftconomy;
 
 import java.util.TimerTask;
 
 import me.greatman.Craftconomy.utils.PayDayConfig;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 public class PayDay extends TimerTask {
 
 	private String groupName;
 	private double amount;
 	private Currency currency;
 	public PayDay(String group)
 	{
 		groupName = group;
 		amount = PayDayConfig.getAmount(groupName);
 		currency = PayDayConfig.getCurrency(groupName);
 	}
 	@Override
 	public void run() {
 		Player[] playerList = Craftconomy.plugin.getServer().getOnlinePlayers();
 		for (int i = 0; i < playerList.length; i++)
 		{
 			ILogger.info("Player ID : " + i +  ":" + playerList[i].getPlayer());
 			ILogger.info(playerList[i].hasPermission("Craftconomy.payday." + groupName) + "");
 			ILogger.info("Craftconomy.payday." + groupName);
 			if (playerList[i].hasPermission("Craftconomy.payday." + groupName))
 			{
				AccountHandler.getAccount(playerList[i]).addMoney(amount,currency);
 				playerList[i].sendMessage(ChatColor.GREEN + "It's payday! You received " + Craftconomy.format(amount, currency));
 			}
 			
 		}
 	}
 
 }
