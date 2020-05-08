 package net.tigerstudios.RPGCraft;
 
 import java.io.File;
 import java.util.Calendar;
 import java.util.List;
 
 import net.tigerstudios.RPGCraft.utils.PropertiesFile;
 
 import org.bukkit.Bukkit;
import org.bukkit.GameMode;
 import org.bukkit.Server;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Listener;
 import org.bukkit.plugin.Plugin;
 import org.getspout.spoutapi.inventory.SpoutItemStack;
 
 
 public class listener_Currency implements Listener {
 	private Plugin rpgPlugin = null;
 	private Server rpgServer = null;
 	private List<World> rpgWorlds = null;
 	private PropertiesFile playerBalances = null;
 	private PropertiesFile transactionHistory = null;
 	private RPG_Player rpgPlayer = null;
 	
 	// public void onPlayerCommandPreprocess(PlayerChatEvent event)
 	
 	public boolean currencyProcessor(CommandSender sender, Command command, String label, String[] cmd)
 	{
 		Player p = rpgServer.getPlayer(sender.getName());
 		
 		if(command.getName().equalsIgnoreCase("balance") || command.getName().equalsIgnoreCase("bal"))
 		{
 			String playerName = p.getName();
 						
 			if(!(RPGCraft.permissionHandler.has(p, "rpgcraft.money")))
 			{
 				p.sendMessage("[2RPGf] Sorry, but you do not have access to the currency");
 				p.sendMessage("[2RPGf] commands. Speak to a server admin to gain access.");
 				return true;				
 			} // if(!(RPGCraft.Permissions.has(p, "rpgcraft.money")))
 			
 			if(cmd.length == 1 && RPGCraft.permissionHandler.has(p, "rpgcraft.money.mods"))
 			{
 				playerName = cmd[0];
 				rpgPlayer = mgr_Player.getPlayer(playerName);
 				if(rpgPlayer == null)
 				{	p.sendMessage("[2RPGf] The player cannot be found.  Either they are not online");
 					p.sendMessage("[2RPGf] or there may be a spelling mistake.");
 					return true;
 				} // if(rpgPlayer == null)
 			} // if(cmd.length == 2 && RPGCraft.Permissions.has(p, "rpgcraft.money.mods"))
 			
 			if(cmd.length == 0)
 			{	playerName = p.getName();
 				rpgPlayer = mgr_Player.getPlayer(playerName);
 			} // if(cmd.length == 1)			
 			
 			p.sendMessage("[2RPGf] Balance: 6"+rpgPlayer.getGold()+" Goldf, 7"+rpgPlayer.getSilver()+" Silverf, c"+rpgPlayer.getCopper()+" Copperf.");
 			if(cmd.length == 0 && RPGCraft.permissionHandler.has(p, "rpgcraft.money.mods"))
 			{
 				p.sendMessage("[2RPGf] You may also check other players balances by adding");
 				p.sendMessage("[2RPGf] their name.");
 				p.sendMessage("[2RPGf] Ex: 2/balance f<bplayernamef>");
 			}
 			return true;
 		} // if(cmd[0].equalsIgnoreCase("/balance") || cmd[0].equalsIgnoreCase("/bal"))
 		
 		
 		if(command.getName().equalsIgnoreCase("deposit"))
 		{
 			if(!(RPGCraft.permissionHandler.has(p, "rpgcraft.money")))
 			{
 				p.sendMessage("[2RPGf] Sorry, but you do not have access to the currency");
 				p.sendMessage("[2RPGf] commands. Speak to a server admin to gain access.");
 				return true;				
 			} // if(!(RPGCraft.Permissions.has(p, "rpgcraft.money")))			
 			
			
			if(p.getGameMode() == GameMode.CREATIVE)
			{
				p.sendMessage("[2RPGf] Nice try " + p.getDisplayName() + ". ");
				p.sendMessage("[2RPGf] This attempt will be logged, and admins will");
				p.sendMessage("[2RPGf] be notified.");
				return true;
			} // if(p.getGameMode() == GameMode.CREATIVE)
			
			
 			p.sendMessage("[2RPGf] To use the Deposit command please \"Use\" the");
 			p.sendMessage("[2RPGf] coin you want to deposit.");
 			
 			// Set the timer for 15 seconds
 			rpgPlayer = mgr_Player.getPlayer(p.getName());
 			rpgPlayer.lTimer = System.currentTimeMillis(); 
 			p.sendMessage("[2RPGf] Right click with the coins in your hand that you want");
 			p.sendMessage("[2RPGf] to deposit");		
 			
 			return true;
 		} // if(command.getName().equalsIgnoreCase("deposit"))
 		
 				
 		if(command.getName().equalsIgnoreCase("withdraw"))
 		{
 			if(!(RPGCraft.permissionHandler.has(p, "rpgcraft.money")))
 			{
 				p.sendMessage("[2RPGf] Sorry, but you do not have access to the currency");
 				p.sendMessage("[2RPGf] commands. Speak to a server admin to gain access.");
 				return true;				
 			} // if(!(RPGCraft.Permissions.has(p, "rpgcraft.money")))
 			
			if(p.getGameMode() == GameMode.CREATIVE)
			{
				p.sendMessage("[2RPGf] Sorry, but no withdraws allowed while");
				p.sendMessage("[2RPGf] in creative mode.");
				return true;
			} // if(p.getGameMode() == GameMode.CREATIVE)
			
 			if(cmd.length == 0 || cmd.length > 3)
 			{	// TODO: Give a more detailed help description
 				p.sendMessage("[2RPGf] Usage -> /withdraw <6goldf> <7silverf> <ccopperf>.");
 				return true;
 			}
 			
 			rpgPlayer = mgr_Player.getPlayer(p.getName());
 			
 // TODO: Add error checking here to make sure all values are valid
 // TODO: Add ability to append G, S, or C to amount and let player enter what they want instead
 			//of requiring all 3 values.  Eg. /withdraw 10S
 			int gp, sp, cp;
 			gp = Integer.parseInt(cmd[0]);
 			sp = Integer.parseInt(cmd[1]);
 			cp = Integer.parseInt(cmd[2]);
 			
 			if(rpgPlayer.getTotalCopper() < ( cp + (sp * 100) + (gp * 10000)))
 			{ 
 				p.sendMessage("[2RPGf] Sorry, but you do not have enough coin.");
 				p.sendMessage("[2RPGf] Balance: 6"+rpgPlayer.getGold()+" Goldf, 7"+rpgPlayer.getSilver()+" Silverf, c"+rpgPlayer.getCopper()+" Copperf.");						
 				return true;
 			}
 			if(cp > 0)
 			{				
 				p.getWorld().dropItem(p.getLocation(), new SpoutItemStack(RPGCraft.copperCoin, cp));
 								
 			}
 			if(sp > 0)
 				p.getWorld().dropItem(p.getLocation(), new SpoutItemStack(RPGCraft.silverCoin, sp));
 			if(gp > 0)
 				p.getWorld().dropItem(p.getLocation(), new SpoutItemStack(RPGCraft.goldCoin, gp));
 			
 			
 			int totalcp = cp + (sp * 100) + (gp * 10000);
 			rpgPlayer.removeCopper(totalcp);
 			p.sendMessage("[2RPGf] Balance: 6"+rpgPlayer.getGold()+" Goldf, 7"+rpgPlayer.getSilver()+" Silverf, c"+rpgPlayer.getCopper()+" Copperf.");						
 			return true;
 		} // if(command.getName().equalsIgnoreCase("withdraw"))
 		
 		
 		if(command.getName().equalsIgnoreCase("givecoin") || command.getName().equalsIgnoreCase("gc"))
 		{
 			
 			if(!(RPGCraft.permissionHandler.has(p, "rpgcraft.money")))
 			{
 				p.sendMessage("[2RPGf] Sorry, but you do not have access to the currency");
 				p.sendMessage("[2RPGf] commands. Speak to a server admin to gain access.");
 				return true;				
 			} // if(!(RPGCraft.Permissions.has(p, "rpgcraft.money")))
 			// example /givecoin 1g 1s 1c mrhodes						
 			if(cmd.length == 0 || cmd.length > 4)
 			{
 				// TODO: Give a more detailed help description
 				p.sendMessage("[2RPGf] Usage -> /givecoin <6goldf> <7silverf> <ccopperf> <player>.");
 				return true;
 			}
 			// Make sure player is logged in to RPGCraft
 			RPG_Player pSender = mgr_Player.getPlayer(p.getName());
 			if(pSender == null)
 				return true;
 			
 			// Get the receivers name and validate that player too
 			List<Player> receivList = rpgServer.matchPlayer(cmd[cmd.length-1]);
 			if(receivList.size() != 1)
 			{
 				if(receivList.size() == 0)
 				{
 					p.sendMessage("[2RPGf] "+cmd[cmd.length-1]+" is not online at the moment, or");
 					p.sendMessage("[2RPGf] there may be a spelling mistake in the name.");
 					return true;
 				}
 				p.sendMessage("[2RPGf] Please spell out more of that players name to");
 				p.sendMessage("[2RPGf] send some coin.");
 								
 				return true;
 			} // if(receivList.size() != 1)
 			Player pReceiver = receivList.get(0);
 			RPG_Player receiver = mgr_Player.getPlayer(pReceiver.getName());
 			if(receiver == null)
 			{
 				p.sendMessage("[2RPGf] Cannot give coin to "+receivList.get(0).getName()+".");
 				p.sendMessage("[2RPGf] They may not be set up yet as an RPG character");
 				p.sendMessage("[2RPGf] or they are not logged in.");
 				return true;
 			}
 			
 			int gold = pSender.getGold();
 			int silver = pSender.getSilver();
 			int copper = pSender.getCopper();
 			int totalCopper = (gold*100*100) + (silver*100) + copper;
 			
 			int sendGold = 0;
 			int sendSilver = 0;
 			int sendCopper = 0;
 			int sendTotalCopper = 0;
 			
 			// Now get the coin values...
 			
 			switch (cmd.length)
 			{
 			case 4:
 				sendGold = Integer.parseInt(cmd[0]);
 				sendSilver = Integer.parseInt(cmd[1]);
 				sendCopper = Integer.parseInt(cmd[2]);
 				break;
 			case 3:
 				sendSilver = Integer.parseInt(cmd[0]);
 				sendCopper = Integer.parseInt(cmd[1]);
 				break;
 			case 2:	
 				sendCopper = Integer.parseInt(cmd[0]);
 				break;
 			}
 			
 			sendTotalCopper = (sendGold*100*100) + (sendSilver*100) + sendCopper;
 			if(sendTotalCopper < 0)
 			{
 				p.sendMessage("[2RPGf] You cannot send a negative amount of coin.");
 				p.sendMessage("[2RPGf] Your transaction has been cancelled.");
 				return true;
 			}
 			
 			if(sendTotalCopper == 0)
 			{
 				p.sendMessage("[2RPGf] You cannot send nothing, that would be a waste of time.");
 				p.sendMessage("[2RPGf] Your transaction has been cancelled.");
 				return true;
 			}
 				
 			if(totalCopper < sendTotalCopper)
 			{
 				p.sendMessage("[2RPGf] You do not have enough coin to send to "+pReceiver.getDisplayName()+".");
 				p.sendMessage("[2RPGf] You only have 6"+gold+" goldf, 7"+silver+" silverf, and c"+copper+" copperf.");
 				return true;	
 			}
 				
 			receiver.setCopper(receiver.getCopper() + sendTotalCopper);
 			receiver.optimizeCoin();
 			pReceiver.sendMessage("[2RPGf] "+p.getName()+" has sent you 6"+sendGold+" Goldf, 7"+sendSilver+" Silverf, and c"+sendCopper+" Copperf.");
 			
 			pSender.removeCopper(sendTotalCopper);
 			p.sendMessage("[2RPGf] You sent 6"+sendGold+" Goldf, 7"+sendSilver+" Silverf, and c"+sendCopper+" Copper fto "+receiver.GetPlayer().getName()+".");
 			p.sendMessage("[2RPGf] You have 6"+pSender.getGold()+" Goldf, 7"+pSender.getSilver()+" Silverf, and C"+pSender.getCopper()+" Copper fleft.");
 			
 			// Save transaction
 			String transaction = pSender.mcName + " sent "+sendGold+"gold, "+sendSilver+"silver, and "+sendCopper+"copper to "+receiver.mcName;
 			String date = Calendar.getInstance().getTime().toString();
 			transactionHistory.setString(date, transaction, "Player-to-Player transaction");
 			transactionHistory.save();
 			
 			// Save updated player balance
 			String senderBalance = pSender.getGold()+","+pSender.getSilver()+","+pSender.getCopper();
 			String receiverBalance = receiver.getGold()+","+receiver.getSilver()+","+receiver.getCopper();
 			playerBalances.setString(pSender.getMCName(), senderBalance, "Last updated: "+date);
 			playerBalances.setString(receiver.getMCName(), receiverBalance, "Last updated: "+date);
 			playerBalances.save();
 			
 			return true;
 		} // if(cmd[0].equalsIgnoreCase("/givecoin") || cmd[0].equalsIgnoreCase("/gc"))	
 		
 		
 		// Exchange a players money from rpg <--> icon
 		// /banker exchange rpg [all]|[(coin amount)]
 		// /banker exchange icon [all]|[(g) (s) (c)]
 		/*if(command.getName().equalsIgnoreCase("exchange"))
 		{
 			event.setCancelled(true);
 					
 			String iConCurrency = RPGCraft.getiConomy().getBank(p.getName()).;
 			int exchangeRate = Integer.parseInt(RPGCraft.settings.getString("icon_Exchange", "0", "iConomy exchange rate"));
 			double exchangeFee = Double.parseDouble(RPGCraft.settings.getString("icon_ExchangePercent", "0", "Exchange fee"));
 			
 			Account pAccount = RPGCraft.getiConomy().getBank().getAccount(p.getName());
 			RPG_Player rpgPlayer = mgr_Player.getPlayer(p.getName());
 			
 			// make sure there are at least 2 more values after exchange.
 			if(cmd.length == 1)
 			{
 				p.sendMessage("aCurrency Exchange Help");
 				p.sendMessage(" ");
 				p.sendMessage("Usage: /exchange rpg f[aallf] f| f[acoin amountf]");
 				p.sendMessage("       /exchange icon [all] | [(6gf) (7sf) (ccf)]");
 				p.sendMessage("");
 				p.sendMessage("With the above command you can exchange your money from");
 				p.sendMessage("one currency to another.");
 				return;
 			} // if(cmd.length == 1)
 			
 			// Get the exchange rate
 			if(cmd[1].equalsIgnoreCase("rate"))
 			{
 				// Figure out how much Gold, Silver and Copper = 1 iConomy coin.
 				// All calculations will be done using just copper though
 				int gold = 0;  int silver = 0; int copper = exchangeRate;
 				while(copper >= 100)
 				{	silver = silver + 1;  copper = copper - 100; }
 				while(silver >= 100)
 				{	gold = gold + 1; silver = silver - 100;  }
 				
 				p.sendMessage("aCurrency Exchange Rates");
 				p.sendMessage(" ");
 				p.sendMessage("The current exchange rate for "+RPGCraft.getiConomy().getBank().getCurrency());
 				p.sendMessage("to RPG currency is:");
 				p.sendMessage("a1"+RPGCraft.getiConomy().getBank().getCurrency()+"f -> 6"+gold+" Goldf, 7"+silver+" Silverf, and c"+copper+" Copperf.");
 				p.sendMessage("The Exchange Fee is a"+exchangeFee+"f%");
 				
 				return;	
 			} // if(cmd[1].equalsIgnoreCase("rate"))
 			
 			
 			// Figure out what we are exchanging to...
 			if(cmd[1].equalsIgnoreCase("rpg"))
 			{
 				double iConBalance = pAccount.getBalance();
 				double coinAmount = 0;				
 				
 				// Find out how much the player wants to exchange
 				if(cmd.length == 3)
 				{
 					if(cmd[2].equalsIgnoreCase("all"))
 						coinAmount = iConBalance;
 					else
 					{	coinAmount = Double.parseDouble(cmd[2]);
 						if(coinAmount > iConBalance)
 						{
 							p.sendMessage("[2RPGf] You do not have a"+coinAmount+" "+iConCurrency);
 							p.sendMessage("[2RPGf] The most you can exchange right now is a"+pAccount.getBalance()+" "+iConCurrency);
 							return;
 						} // if(coinAmount > iConBalance)
 					}
 					
 					if(rpgPlayer != null)
 					{
 						// Subtract the fee from the coinAmount
 						double exFee = coinAmount * (exchangeFee / 100);
 					
 						// Minus this balance from the iConomy balance
 						pAccount.subtract(coinAmount);
 						// Make sure the iConomy balance is limited to 2 decimal places
 						int newBalance = (int) (pAccount.getBalance() * 100);
 						pAccount.setBalance(newBalance / 100);						
 						pAccount.save();
 										
 						coinAmount = coinAmount - exFee;
 					
 						// Convert all the players iConomy balance to RPG balance
 						int copper = (int) (coinAmount * exchangeRate);
 						rpgPlayer.setCopper(rpgPlayer.getCopper() + copper);
 						rpgPlayer.optimizeCoin();
 						
 						// Figure out how much gold, silver, and copper the player just gained.
 						int gold = 0; int silver = 0;
 						while(copper > 100)
 						{	silver = silver + 1; copper = copper - 100; }
 						while(silver > 100)
 						{	gold = gold + 1; silver = silver - 100; }
 						
 						p.sendMessage("[2RPGf] You have exchanged a"+ coinAmount+" "+iConCurrency);
 						p.sendMessage("[2RPGf] This translated to: 6"+gold+" Goldf, 7"+silver+" Silverf, and c"+copper+" Copperf.");
 						p.sendMessage("[2RPGf] The exchange fee for this was a"+exFee+" "+iConCurrency);
 						p.sendMessage("");
 						p.sendMessage("[2RPGf] You now have a"+pAccount.getBalance()+" "+iConCurrency+"f and");
 						p.sendMessage("[2RPGf] 6"+rpgPlayer.getGold()+" Goldf, 7"+rpgPlayer.getSilver()+" Silverf, and c"+rpgPlayer.getCopper()+" Copperf.");
 						
 						// Save transaction
 						String transaction = p.getName()+" exchanged "+coinAmount+" "+iConCurrency+" to "+gold+" Gold, "+silver+" Silver, and "+copper+" Copper";
 						String date = Calendar.getInstance().getTime().toString();
 						transactionHistory.setString(date, transaction, "Exchange iConomy --> RPG Currency");
 						transactionHistory.save();
 						
 						// Save updated player balance
 						String senderBalance = rpgPlayer.getGold()+","+rpgPlayer.getSilver()+","+rpgPlayer.getCopper();
 						playerBalances.setString(rpgPlayer.getMCName(), senderBalance, "Last updated: "+date);
 						playerBalances.save();
 						
 						return;						
 					} // if(rpgPlayer != null)				
 					return;
 				} // if(cmd.length == 3)
 				
 									
 				// Converting from iConomy to the RPG currency
 				return;
 			} // if(cmd[1].equalsIgnoreCase("rpg"))
 			if(cmd[1].equalsIgnoreCase("icon"))
 			{
 				// Get the gold, silver, and copper values
 				int gold = Integer.parseInt(cmd[2]); int silver = Integer.parseInt(cmd[3]);
 				double copper = Double.parseDouble(cmd[4]);
 				
 				// Now convert this value down to copper
 				copper = (gold * 100 * 100) + (silver * 100) + copper;
 				double exFee = copper * (exchangeFee / 100);
 								
 				double newbalance = (copper - exFee) / exchangeRate;
 				
 				// Add money to iConomy balance, and take away from rpg balance
 				pAccount.add(newbalance);	pAccount.save();
 				rpgPlayer.removeCopper((int)copper);
 				//rpgPlayer.optimizeCoin();
 				
 				p.sendMessage("[2RPGf] You have exchanged "+gold+" Gold, "+silver+" Silver, and "+Integer.parseInt(cmd[4])+" Copper");
 				p.sendMessage("[2RPGf] to "+newbalance+" "+ iConCurrency+".");
 				
 				// Calculate what the exfee was in gold, silver, and copper.
 				int feeGold = 0; int feeSilver = 0; int feeCopper = (int) exFee;
 				while(feeCopper > 100)	{	feeSilver = feeSilver + 1; feeCopper = feeCopper - 100; }
 				while(feeSilver > 100)	{	feeGold = feeGold + 1; feeSilver = feeSilver - 100; }
 				p.sendMessage("[2RPGf] The exchange fee for this was "+feeGold+" Gold, "+feeSilver+" Silver, and "+feeCopper+" Copper."); 
 				p.sendMessage("");
 				p.sendMessage("[2RPGf] You now have a"+pAccount.getBalance()+" "+iConCurrency+"f and");
 				p.sendMessage("[2RPGf] 6"+rpgPlayer.getGold()+" Goldf, 7"+rpgPlayer.getSilver()+" Silverf, and c"+rpgPlayer.getCopper()+" Copperf.");
 				
 			//	p.sendMessage("Copper converted: "+copper);
 				//p.sendMessage("iConomy Money: "+newbalance);
 				
 				return;					
 			} // if(cmd[1].equalsIgnoreCase("icon"))
 			
 				
 			// Get values for the exchange
 			return;
 		} // if(cmd[0].equalsIgnoreCase("exchange"))		
 		*/
 		
 		return false;
 	}	
 
 	public listener_Currency(Plugin p)
 	{
 		Bukkit.getServer().getPluginManager().registerEvents(this, p);
 		this.rpgPlugin = p;
 		this.rpgServer = p.getServer();
 		this.rpgWorlds = rpgServer.getWorlds();
 		
 		playerBalances = new PropertiesFile(RPGCraft.mainDirectory+"logs"+File.separatorChar+"playerBalances.log");
 		transactionHistory = new PropertiesFile(RPGCraft.mainDirectory+"logs"+File.separatorChar+"transactionHistory.log");
 	} // public listener_Currency(Plugin p)
 
 }
