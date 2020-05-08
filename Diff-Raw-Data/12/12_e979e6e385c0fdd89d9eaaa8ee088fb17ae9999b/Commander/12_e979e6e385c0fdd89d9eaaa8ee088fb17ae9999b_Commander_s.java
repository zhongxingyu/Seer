 package de.bdh.ks;
 import java.util.Map;
 
 import org.bukkit.Material;
 import org.bukkit.command.*;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 public class Commander implements CommandExecutor {
 	
 	Main plugin;
 	public Commander(Main plugin)
 	{
 		this.plugin = plugin;
 	}
 
 	public boolean enderChestClose(CommandSender s)
 	{
 		return Main.helper.ahNear(s);
 	}
 	
 	
 	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String args[])
     {
 		if(sender instanceof Player)
         {
         	if(command.getName().equals("auction"))
         	{
         		if(args.length == 0)
         		{
         			Main.lng.msg(sender,"usage");
         		} else
         		{
         			if(args[0].equalsIgnoreCase("sign"))
         			{
         				if(sender.hasPermission("ks.sign"))
         				{
 	        				if(args.length < 3)
 	                		{
 	        					Main.lng.msg(sender,"usage_sign");
 	                		} else
 	                		{
 	                			int of=0,id=0;
 	                			if(args[1].equalsIgnoreCase("offer"))
 	                				of = 1;
 	                			else if(args[1].equalsIgnoreCase("request"))
 	                				of = 2;
 	                			try
 	            				{
 	            					id = Integer.parseInt(args[2]);
 	            				}
 	            				catch(Exception e) 
 	            				{ 
 	            					Main.lng.msg(sender,"err_num",new Object[]{"ID"});
 	            					return true;
 	            				}
 	                			
 	                			if(of > 0 && id > 0)
 	                			{
 	                				this.plugin.poffer.put((Player) sender, new KSId(of,id));
 	                				Main.lng.msg(sender,"suc_sign");
 	                			} else
 	                				Main.lng.msg(sender,"usage_sign");
 	                		}
         				} else
         				{
         					Main.lng.msg(sender,"err_noperm");
         					return true;
         				}
         			}
         			//Min 1 Parameter
         			else if(args[0].equalsIgnoreCase("abort"))
         			{
         				if(args.length < 2)
                 		{
         					Main.lng.msg(sender,"usage_abort");
                 		} else
                 		{
                 			int id = 0;
                 			try
             				{
             					id = Integer.parseInt(args[1]);
             				}
             				catch(Exception e) 
             				{ 
             					Main.lng.msg(sender,"err_num",new Object[]{"ID"});
             					return true;
             				}
                 			if(Main.helper.removeAuction(id,(Player)sender))
                 			{
                 				Main.lng.msg(sender,"rem_success");
                 			} else 
                 				Main.lng.msg(sender,"err_invalid_id");
 
                 		}
         			}
         			else if(args[0].equalsIgnoreCase("abortrequest"))
         			{
         				if(args.length < 2)
                 		{
         					Main.lng.msg(sender,"usage_abortrequest");
                 		} else
                 		{
                 			int id = 0;
                 			try
             				{
             					id = Integer.parseInt(args[1]);
             				}
             				catch(Exception e) 
             				{ 
             					Main.lng.msg(sender,"err_num",new Object[]{"ID"});
             					return true;
             				}
                 			if(Main.helper.removeRequest(id,(Player)sender))
                 			{
                 				Main.lng.msg(sender,"rem_req_suc");
                 			} else 
                 				Main.lng.msg(sender,"err_invalid_id");
 
                 		}
         			}
         			else if(args[0].equalsIgnoreCase("list"))
         			{
         				int page = 1;
         				try
         				{
         					page = Integer.parseInt(args[1]);
         				}
         				catch(Exception e) { }
         				
         				
         				int amount = Main.helper.getOfferAmountFromPlayer(sender.getName());
         				int maxpage = (int) Math.ceil(amount / 5.0);
         				
         				if(amount == 0)
         				{
         					Main.lng.msg(sender,"err_nosale");
         				} else
         					Main.lng.msg(sender,"header_list",new Object[]{amount,"auctions",page,maxpage});
         				
         				page = page -1;
         				page = page * 5;
         				Map<Integer,KSOffer> l = Main.helper.getOffersFromPlayer(sender.getName(),5,page);
         				for(Map.Entry<Integer, KSOffer> e: l.entrySet())
         				{
         					sender.sendMessage("ID: "+e.getKey()+ " - Block: "+KrimBlockName.getNameByItemStack(e.getValue().getItemStack()) + " Amount: "+e.getValue().getAmount()+ " for "+e.getValue().getFullPrice()+ " "+Main.econ.currencyNamePlural());
         				}
         				
         				
         			}
         			else if(args[0].equalsIgnoreCase("listrequests"))
         			{
         				int page = 1;
         				try
         				{
         					page = Integer.parseInt(args[1]);
         				}
         				catch(Exception e) { }
         				
         				
         				int amount = Main.helper.getRequestAmountFromPlayer(sender.getName());
         				int maxpage = (int) Math.ceil(amount / 5.0);
         				
         				if(amount == 0)
         				{
         					Main.lng.msg(sender,"err_noreq");
         				} else
         					Main.lng.msg(sender,"header_list",new Object[]{amount,"requests",page,maxpage});
         				
         				page = page -1;
         				page = page * 5;
         				Map<Integer,KSOffer> l = Main.helper.getRequestsFromPlayer(sender.getName(),5,page);
         				for(Map.Entry<Integer, KSOffer> e: l.entrySet())
         				{
         					sender.sendMessage("ID: "+e.getKey()+ " - Block: "+KrimBlockName.getNameByItemStack(e.getValue().getItemStack()) + " Amount: "+e.getValue().getAmount()+ " for "+e.getValue().getFullPrice()+ " "+Main.econ.currencyNamePlural());
         				}
         				
         				
         			}
         			else if(args[0].equalsIgnoreCase("sell") || args[0].equalsIgnoreCase("qsell"))
         			{
         				if(!sender.hasPermission("ks.sell"))
         				{
         					Main.lng.msg(sender,"err_noperm");
         					return true;
         				}
         				
         				if(configManager.enderForTransaction == 1 && this.enderChestClose(sender) == false)
     					{
         					Main.lng.msg(sender,"err_to_ah");	
         					return true;
     					}
         				
         				//VERKAUFE
         				if(args.length < 2 && !args[0].equalsIgnoreCase("qsell"))
                 		{
         					Main.lng.msg(sender,"usage_sell");
                 		} else
                 		{
                 			ItemStack i = null;
                 			int price = 0;
                 			int maxAm = 0;
                 			
                 			//Verkaufe Item in der Hand
                 			if(args.length == 1 &&  args[0].equalsIgnoreCase("qsell"))
                 			{
                 				i = ((Player) sender).getItemInHand();
                 				if(configManager.werte.get(KrimBlockName.getIdByItemStack(i)) == null)
                 				{
                 					Main.lng.msg(sender,"noqsell");
                 					return true;
                 				} else
                 					price = configManager.werte.get(KrimBlockName.getIdByItemStack(i));
                 				
                 			}
                 			
                 			else if(args.length == 2) 
                 			{
                 				try
                 				{
                 					price = Integer.parseInt(args[1]);
                 				}
                 				catch(Exception e)
                 				{
                 					Main.lng.msg(sender,"err_num",new Object[]{"Price"});
                 					return true;
                 				}
                 				
                 				i = ((Player) sender).getItemInHand();
 
                 			//Verkaufe Gegenstand aus dem Chat
                 			} else if(args.length > 2)
                 			{
                 				
                 				if(args.length == 4)
                 				{
 	                				try
 	                				{
 	                					maxAm = Integer.parseInt(args[3]);
 	                				}
 	                				catch(Exception e)
 	                				{
 	                					Main.lng.msg(sender,"err_num",new Object[]{"Amount"});
 	                					return true;
 	                				}
                 				} else maxAm = 999999;
                 				
                 				try
                 				{
                 					price = Integer.parseInt(args[2]);
                 				}
                 				catch(Exception e)
                 				{
                 					Main.lng.msg(sender,"err_num",new Object[]{"Price"});
                 					return true;
                 				}
                 				i = KrimBlockName.parseName(args[1]);
                 				
                 			}
                 			
                 			if(price > 10000000)
                 			{
                 				Main.lng.msg(sender,"err_too_high",new Object[]{"Price"});
             					return true;
                 			}
                 			
                 			if(i == null  || i.getType() == Material.AIR)
             				{
             					Main.lng.msg(sender,"err_block_404");
             					return true;
             				}
                 			
                 			if(maxAm != 0)
                 				i.setAmount(maxAm);
                 			
             				int am = Main.helper.removeItemsFromPlayer((Player) sender, i, i.getAmount());
             				if(am <= 0)
             				{
             					Main.lng.msg(sender,"err_noitem");
             				} else
             				{
                 				KSOffer of = new KSOffer(i,sender.getName(),price,am);
                 				if(of.payFee() == false)
                 				{
                 					Main.lng.msg(sender,"err_nomoney_fee",new Object[]{of.getFee()}); 
                 					Main.helper.giveBack(of);	
                 				} else
                 				{
 	                				if(Main.helper.enlistItem(of) == true)
 	                				{
 	                					Main.lng.msg(sender,"suc_offer",new Object[]{am,of.getFullPrice()});
 	                					if(of.getFee() > 0)
 	                					{
 	                						Main.lng.msg(sender,"suc_fee_paid",new Object[]{of.getFee()});
 	                					}
 	                				}
 	                				else
 	                				{
 	                					Main.helper.giveBack(of);
 	                					Main.lng.msg(sender,"err");
 	                				}
                 				}
             				}
                 		}
         			} else if(args[0].equalsIgnoreCase("request"))
         			{
         				if(!sender.hasPermission("ks.buy"))
         				{
         					Main.lng.msg(sender,"err_noperm");
         					return true;
         				}
         				
         				
         				if(configManager.enderForTransaction == 1 && this.enderChestClose(sender) == false)
     					{
         					Main.lng.msg(sender,"err_to_ah");
         					return true;
     					}
     				
         				
         				if(args.length < 3)
                 		{
         					Main.lng.msg(sender,"usage_request");
                 		} else 
                 		{
                 			int price=0, amount=0;
                 			ItemStack i = null;
                 			//Requeste aus Hand
                 			if(args.length == 3)
 	                		{
 	            				try
 	            				{
 	            					amount = Integer.parseInt(args[2]);
 	            				}
 	            				catch(Exception e)
 	            				{
 	            					Main.lng.msg(sender,"err_num",new Object[]{"Amount"});
 	            					return true;
 	            				}
 	            				try
 	            				{
 	            					price = Integer.parseInt(args[1]);
 	            				}
 	            				catch(Exception e)
 	            				{
 	            					Main.lng.msg(sender,"err_num",new Object[]{"Price"});
 	            					return true;
 	            				}
 	            				
 	            				i = ((Player) sender).getItemInHand().clone();
 	            			//Requeste aus Chat
 	                		} else if(args.length == 4)
 	                		{
 	                			try
                 				{
                 					amount = Integer.parseInt(args[3]);
                 				}
                 				catch(Exception e)
                 				{
                 					Main.lng.msg(sender,"err_num",new Object[]{"Amount"});
                 					return true;
                 				}
                 				try
                 				{
                 					price = Integer.parseInt(args[2]);
                 				}
                 				catch(Exception e)
                 				{
                 					Main.lng.msg(sender,"err_num",new Object[]{"Price"});
                 					return true;
                 				}
                 				i = KrimBlockName.parseName(args[1]);
 	                		}
                 			
                 			if(price > 10000000)
                 			{
                 				Main.lng.msg(sender,"err_toohigh",new Object[]{"Price"});
             					return true;
                 			}
                 			
                 			if(i == null || i.getType() == Material.AIR)
             				{
             					Main.lng.msg(sender,"err_block");
             					return true;
             				}
             				i.setAmount(amount);
             				int bought = Main.helper.buyItems(i, price, sender.getName());
             				if(bought == -1)
             				{
             					Main.lng.msg(sender,"err_nomoney");
             				}
             				else if(bought == amount)
             				{
             					Main.lng.msg(sender,"suc_bought");
             				} else
             				{
             					if(bought > 0)
             						Main.lng.msg(sender,"suc_bought_part",new Object[]{bought,amount});
             					int req = amount - bought;
             					
             					KSOffer o = new KSOffer(i,sender.getName(),price);
             					int resp = Main.helper.enlistRequest(o);
             					if(resp == 1)
             					{
             						if(o.getAmount() == req)
             							Main.lng.msg(sender,"suc_req",new Object[]{req,(req*price)});
             						else
             							Main.lng.msg(sender,"suc_req_part",new Object[]{o.getAmount(),(o.getAmount()*price)});
             						Main.lng.msg(sender,"req_info");
 
             					} else if(resp == -2)
             					{
             						Main.lng.msg(sender,"err_noperm");
             					} else if(resp == -1)
             					{
             						Main.lng.msg(sender,"err_nomoney");
             					}
             				}
                 		}
         			} else if(args[0].equalsIgnoreCase("buy"))
         			{
         				if(!sender.hasPermission("ks.buy"))
         				{
         					Main.lng.msg(sender,"err_noperm");
         					return true;
         				}
         				
         				//KAUFE
         				if(args.length < 2)
                 		{
         					Main.lng.msg(sender,"usage_buy");
                 		} else
                 		{
                 			if(configManager.enderForTransaction == 1 && this.enderChestClose(sender) == false)
         					{
             					Main.lng.msg(sender,"err_to_ah");	
             					return true;
         					}
                 			
                 			int price=0, amount=0;
                 			ItemStack i = null;
                 			
                 			double money = Main.econ.getBalance(sender.getName());
                 			
                 			//Kaufe Gegenstand in der Hand ohne Preis
                 			if(args.length == 2)
                 			{
                 				try
                 				{
                 					amount = Integer.parseInt(args[1]);
                 				}
                 				catch(Exception e)
                 				{
                 					Main.lng.msg(sender,"err_num",new Object[]{"amount"});
                 					return true;
                 				}
                 				
                 				price = (int) (money / amount);
                 				//Block == IteminHand
                 				i = ((Player) sender).getItemInHand().clone();
                 			}
                 			
                 			//Kaufe Gegenstand ohne Preis
                 			if(args.length == 3)
                 			{
                 				try
                 				{
                 					amount = Integer.parseInt(args[2]);
                 				}
                 				catch(Exception e)
                 				{
                 					Main.lng.msg(sender,"err_num",new Object[]{"Amount"});
                 					return true;
                 				}
                 				
                 				price = (int) (money / amount);
                 				i = KrimBlockName.parseName(args[1]);
                 			//Kaufe Gegenstand aus dem Chat mit maximalpreis
                 			} else if(args.length == 4)
                 			{
                 				//Normale Usage
                 				
                 				try
                 				{
                 					amount = Integer.parseInt(args[3]);
                 				}
                 				catch(Exception e)
                 				{
                 					Main.lng.msg(sender,"err_num",new Object[]{"Amount"});
                 					return true;
                 				}
                 				try
                 				{
                 					price = Integer.parseInt(args[2]);
                 				}
                 				catch(Exception e)
                 				{
                 					Main.lng.msg(sender,"err_num",new Object[]{"Price"});
                 					return true;
                 				}
                 				i = KrimBlockName.parseName(args[1]);
                 			} 
                 			
                 			if(price > 10000000)
                 			{
                 				Main.lng.msg(sender,"err_toohigh",new Object[]{"Price"});
             					return true;
                 			}
                 			
             				if(i == null || i.getType() == Material.AIR)
             				{
             					Main.lng.msg(sender,"err_block_404");
             					return true;
             				}
             				i.setAmount(amount);
             				
             				
             				
             				if((money / price) < amount)
             					Main.lng.msg(sender,"err_nomoney");
             				
             				int bought = Main.helper.buyItems(i, price, sender.getName());
             				if(bought == -1)
             				{
             					Main.lng.msg(sender,"err_nomoney");
             				}
             				else if(bought == amount)
             				{
             					Main.lng.msg(sender,"suc_bought");
             				} else if(bought == 0)
             				{
             					Main.lng.msg(sender,"err_nooffer");
             				} else
             				{
             					Main.lng.msg(sender,"suc_bought_part",new Object[]{bought,amount});
             				}
                 		}
         			} else if(args[0].equalsIgnoreCase("overview"))
         			{
         				if(!sender.hasPermission("ks.list"))
         				{
         					Main.lng.msg(sender,"err_noperm");
         					return true;
         				}
         				
         				//ZEIGE
         				if(args.length < 1)
                 		{
         					Main.lng.msg(sender,"usage_overview");
                 		} else
                 		{
                 			int page = 1;
             				try
             				{
             					page = Integer.parseInt(args[1]);
             				}
             				catch(Exception e) { }
             				
             				
             				int amount = Main.helper.getOfferAmount();
             				int maxpage = (int) Math.ceil(amount / 5.0);
             				
             				if(amount == 0)
             				{
             					Main.lng.msg(sender,"err_noreq");
             				} else
             					Main.lng.msg(sender,"header_list",new Object[]{amount,"offers",page,maxpage});
             				
             				page = page -1;
             				page = page * 5;
             				Map<Integer,KSOffer> l = Main.helper.getOffers(1,5,page);
             				for(Map.Entry<Integer, KSOffer> e: l.entrySet())
             				{
             					sender.sendMessage("Block: "+KrimBlockName.getNameByItemStack(e.getValue().getItemStack()) + " Amount: "+e.getValue().getAmount()+ " for "+e.getValue().getPrice()+ " "+Main.econ.currencyNamePlural()+" each");
             				}
                 		}
        			} else if(args[0].equalsIgnoreCase("overviewrequest"))
         			{
         				if(!sender.hasPermission("ks.list"))
         				{
         					Main.lng.msg(sender,"err_noperm");
         					return true;
         				}
         				
         				//ZEIGE
         				if(args.length < 1)
                 		{
         					Main.lng.msg(sender,"usage_overview");
                 		} else
                 		{
                 			int page = 1;
             				try
             				{
             					page = Integer.parseInt(args[1]);
             				}
             				catch(Exception e) { }
             				
             				
             				int amount = Main.helper.getRequestsAmount();
             				int maxpage = (int) Math.ceil(amount / 5.0);
             				
             				if(amount == 0)
             				{
             					Main.lng.msg(sender,"err_noreq");
             				} else
             					Main.lng.msg(sender,"header_list",new Object[]{amount,"requests",page,maxpage});
             				
             				page = page -1;
             				page = page * 5;
             				Map<Integer,KSOffer> l = Main.helper.getRequests(1,5,page);
             				for(Map.Entry<Integer, KSOffer> e: l.entrySet())
             				{
             					sender.sendMessage("Block: "+KrimBlockName.getNameByItemStack(e.getValue().getItemStack()) + " Amount: "+e.getValue().getAmount()+ " for "+e.getValue().getPrice()+ " "+Main.econ.currencyNamePlural()+ " each");
             				}
                 		}
         			} else if(args[0].equalsIgnoreCase("detail"))
         			{
         				if(!sender.hasPermission("ks.list"))
         				{
         					Main.lng.msg(sender,"err_noperm");
         					return true;
         				}
         				
         				//ZEIGE
         				if(args.length < 2)
                 		{
         					Main.lng.msg(sender,"usage_detail");
                 		} else
                 		{
                 			ItemStack i = KrimBlockName.parseName(args[1]);
                 			if(i != null)
                 				Main.helper.sendInfos((Player)sender, i);
                 			else
                 				Main.lng.msg(sender,"err_block_404");	
                 		}
         			} else if(args[0].equalsIgnoreCase("collect"))
         			{
         				if(!sender.hasPermission("ks.buy"))
         				{
         					Main.lng.msg(sender,"err_noperm");
         					return true;
         				} else
         				{
         					int am = Main.helper.hasDelivery((Player)sender);
         					if(am == 0)
         						Main.lng.msg(sender,"err_nodeliver");
         					/*else
         						Main.lng.msg.(sender,"You've '"+am+"' items waiting for delivery");*/
         					
         					if(am > 0)
         					{
 	        					if(this.enderChestClose(sender))
 	        					{
 	        						Main.helper.getDelivery((Player)sender);
 	        					} else
 	        					{
 	        						Main.lng.msg(sender,"err_to_ah");
 	        					}
         					}
         				}
         			}
         		}
         	}
         }
 		return true;
     }
 }
