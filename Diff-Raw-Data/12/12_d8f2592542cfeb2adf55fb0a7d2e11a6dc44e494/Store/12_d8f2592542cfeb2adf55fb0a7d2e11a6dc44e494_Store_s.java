 package nl.giantit.minecraft.GiantBanks.Commands.Chat;
 
 import nl.giantit.minecraft.GiantBanks.GiantBanks;
 import nl.giantit.minecraft.GiantBanks.Bank.UserAccount;
 import nl.giantit.minecraft.GiantBanks.core.Items.Items;
 import nl.giantit.minecraft.GiantBanks.core.Items.ItemID;
 
 import org.bukkit.entity.Player;
 
 public class Store {
 	
 	private static Items iH = GiantBanks.getPlugin().getItemHandler();
 
	public static void Store(Player p, String[] args) {
 		if(args.length > 2) {
 			String item = null;
 			int id = 0;
 			Integer type = null;
 			int amount = 0;
 			
 			for(int i = 1; i < args.length; i++) {
 				if(args[i].equalsIgnoreCase("-i")) {
 					i++;
 					if(args.length - 1 >= i) {
 						item = args[i];
 						continue;
 					}else{
 						break;
 					}
 				}else if(args[i].startsWith("-i:")) {
 					item = args[i].replaceFirst("-i:", "");
 					continue;
 				}else if(args[i].equalsIgnoreCase("-id")) {
 					i++;
 					if(args.length - 1 >= i) {
 						try{
 							id = Integer.parseInt(args[i].replaceFirst("-a:", ""));
 						}catch(NumberFormatException e) {
 							//ignore
 						}
 						continue;
 					}else{
 						break;
 					}
 				}else if(args[i].startsWith("-id:")) {
 					try{
 						id = Integer.parseInt(args[i].replaceFirst("-id:", ""));
 					}catch(NumberFormatException e) {
 						//ignore
 					}
 					continue;
 				}else if(args[i].equalsIgnoreCase("-t")) {
 					i++;
 					if(args.length - 1 >= i) {
 						try{
 							type = Integer.parseInt(args[i].replaceFirst("-t:", ""));
 						}catch(NumberFormatException e) {
 							//ignore
 						}
 						continue;
 					}else{
 						break;
 					}
 				}else if(args[i].startsWith("-t:")) {
 					try{
 						type = Integer.parseInt(args[i].replaceFirst("-t:", ""));
 					}catch(NumberFormatException e) {
 						//ignore
 					}
 					continue;
 				}else if(args[i].equalsIgnoreCase("-a")) {
 					i++;
 					if(args.length - 1 >= i) {
 						try{
 							amount = Integer.parseInt(args[i].replaceFirst("-a:", ""));
 						}catch(NumberFormatException e) {
 							//ignore
 						}
 						continue;
 					}else{
 						break;
 					}
 				}else if(args[i].startsWith("-a:")) {
 					try{
 						amount = Integer.parseInt(args[i].replaceFirst("-a:", ""));
 					}catch(NumberFormatException e) {
 						//ignore
 					}
 					continue;
 				}
 			}
 			
 			if(item == null) {
 				if(id > 0) {
					if(0 == type)
 						type = null;
 					if(iH.isValidItem(id, type)) {
 						item = iH.getItemNameByID(id, type);
 					}else{
 						p.sendMessage("Item invalid you fuck!");
 						return;
 					}
 				}else{
 					p.sendMessage("Item id is invalid wanker!");
 					return;
 				}
 			}
 			
 			
 			UserAccount uA = UserAccount.getUserAccount(p.getName());
 			if(null == uA)
 				uA = UserAccount.createUserAccount(p.getName());
 			
 			ItemID iID = iH.getItemIDByName(item);
 			
 			if(iID != null) {
 				if(amount > 0) {
 					String m = uA.add(iID, amount);
 					if(null == m) {
 						p.sendMessage("Successfully stored " + String.valueOf(amount) + " of " + item + "!");
 						return;
 					}else{
						
 					}
 				}else{
					p.sendMessage("Srsly... I need more then 0 of that to append!");
 					return;
 				}
 			}else{
 				p.sendMessage("Item invalid you fucktard!");
 				return;
 			}
 		}
 	}
 }
