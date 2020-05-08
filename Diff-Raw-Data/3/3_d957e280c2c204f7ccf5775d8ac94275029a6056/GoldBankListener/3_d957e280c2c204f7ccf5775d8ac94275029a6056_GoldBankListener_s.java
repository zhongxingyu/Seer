 public class GoldBankListener extends PluginListener
 {
 	private GoldBankData data;
 	private Bank bank;
 	private Bool bool;
 	private static Sign sign;
 	private static Server server;
 	
 	public GoldBankListener()
 	{
 		data = new GoldBankData();
 		bank = new Bank(data);
 		bool = new Bool();
 	}
 		
 	public boolean onBlockRightClick(Player player,
                                 Block block,
                                 Item itemInHand)
 	{
 				
 		if(bool.isGoldBankSign(block))
 		{
         	 	 //command flag
 			if(!player.canUseCommand("/goldbank"))
 			{
 				player.notify("No permission to use GoldBank!");
 				return true;
 			}
 			
 			sign = bool.getSign();
               			 	
               		if(!bool.isChest(player, player.getWorld().getBlockAt(sign.getX(), sign.getY() - 1, sign.getZ())))
               		{
               			player.notify("GoldBank broken!");
               			return true;
               		}
               		
 			Chest chest = (Chest)player.getWorld().getOnlyComplexBlock(sign.getX(), sign.getY() - 1, sign.getZ());
 			
 		      	bank.handleBlockRightClick(player, chest);
 		}
 	
 			return false;
 	}
 		
 	public boolean onBlockBreak(Player player, Block block)
 	{
 		if(bool.isGoldBankSign(block) || bool.isGoldBankChest(player, block))
 		{
 			//command flag
 			if(player.canUseCommand("/goldbankdestroy"))
 			{
 							
 				player.notify("Destroyed GoldBank!");
 				return false;
 			}else
 			{
 				player.notify("No permission to destroy GoldBank!");
 				return true;
 			}
 		}else
 			return false;
 	}
 	
 	public boolean onSignChange(Player player, Sign sign)
 	{
 		if(bool.isGoldBankSign(player.getWorld().getBlockAt(sign.getX(), sign.getY(), sign.getZ())))
 		{
 			if(bool.isChest(player, player.getWorld().getBlockAt(sign.getX(), sign.getY() - 1, sign.getZ())))
 			{
 				if(player.canUseCommand("/goldbankcreate"))
 				{						
 					player.notify("GoldBank created!");
 					return false;
 				}else
 				{
 					player.notify("No permission to create a GoldBank");
 					return true;
 				}
 			}else
 			{
 				player.notify("Chest must be placed first!");
 				return true;
 			}
 		}else
 			return false;
 	}
 	
 	public boolean onCommand(Player player, java.lang.String[] split)
 	{
 		if(split[0].equalsIgnoreCase("/goldbank"))
 		{
 			
 			if(split[1].equalsIgnoreCase("transfer") && !split[2].equalsIgnoreCase("") && !split[3].equalsIgnoreCase("") && !split[4].equalsIgnoreCase(""))
 			{
 				if(split[3].equalsIgnoreCase("diamond"))
 				{
 					if(data.getDiamond(player.getName()) < Integer.parseInt(split[4]))
 					{
 						player.notify("Not enough Diamond! Gold: " + data.getDiamond(player.getName()));
 					}else
 					{
 						int amount = data.getDiamond(player.getName()) - Integer.parseInt(split[4]);
 						player.notify("" + amount);
 						data.setDiamond(split[2], Integer.parseInt(split[4]));
 						data.setDiamond(player.getName(), amount);
 						player.notify(data.getInfo(player.getName()));
 					}
 				}else if(split[3].equalsIgnoreCase("gold"))
 				{
 					if(data.getGold(player.getName()) < Integer.parseInt(split[4]))
 					{
 						player.notify("Not enough Gold! Gold: " + data.getGold(player.getName()));
 					}else
 					{
 						int amount = data.getGold(player.getName()) - Integer.parseInt(split[4]);
 						player.notify("" + amount);
 						data.setGold(split[2], Integer.parseInt(split[4]));
 						data.setGold(player.getName(), amount);
 						player.notify(data.getInfo(player.getName()));
 					}
 				}else if(split[3].equalsIgnoreCase("iron"))
 				{
 					if(data.getIron(player.getName()) < Integer.parseInt(split[4]))
 					{
 						player.notify("Not enough Iron! Gold: " + data.getIron(player.getName()));
 					}else
 					{
 						int amount = data.getIron(player.getName()) - Integer.parseInt(split[4]);
 						player.notify("" + amount);
 						data.setIron(split[2], Integer.parseInt(split[4]));
 						data.setIron(player.getName(), amount);
 						player.notify(data.getInfo(player.getName()));
 					}
 				}
 			}else if(split[1].equalsIgnoreCase("info"))
 			{
 				player.notify(data.getInfo(player.getName()));
 			}else
 			{
				//player.notify("Usage: /goldbank transfer <player> <diamond/gold/iron> <amount>");
 			}
 			return true;			
 		}
 		return false;
 	}
 }
