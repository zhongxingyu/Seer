 package com.danstoncube.plugin.drzoid.Boutique;
 
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.block.Chest;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 
 import com.danstoncube.plugin.drzoid.Boutique.SignTypes.BoutiqueSign;
 import com.danstoncube.plugin.drzoid.Boutique.SignTypes.BoutiqueSignChest;
 import com.danstoncube.plugin.drzoid.Boutique.SignTypes.BoutiqueSignDummy;
 import com.danstoncube.plugin.drzoid.Boutique.SignTypes.BoutiqueSignServer;
 import com.danstoncube.plugin.drzoid.Boutique.SignTypes.BoutiqueSignWebAuction;
 
 @SuppressWarnings("unused")
 public class BoutiqueSignManager
 {
 	
 	private HashMap<String,BoutiqueSign> _signs = new HashMap<String,BoutiqueSign>();
 	
 	//TODO: changer ca
 	private String plugName = "Boutique";
 	
 	private Boutique plugin = null;
 	
 	private static BoutiqueSignManager _instance = null;
 	public static BoutiqueSignManager getInstance()
 	{
 		return _instance;
 	}
 	
 	public BoutiqueSignManager(Boutique boutique)
 	{
 		this.plugin = boutique;
 		BoutiqueSignManager._instance = this;
 	}
 
 
 	public Boolean haveLocation(String l)
 	{
 		return _signs.containsKey(l);			
 	}
 	
 	public Boolean haveLocation(Location l)
 	{		
 		return _signs.containsKey(getLocationString(l));			
 	}
 	
 	private String getLocationString(Location l)
 	{
 		return l.getWorld().getName() + ":" + l.getBlockX() + ":" +  l.getBlockY() + ":" + l.getBlockZ(); 
 	}
 	
 	public Boolean isEnabled(Sign s)
 	{
 		BoutiqueSign bs = _signs.get(s.getBlock().getLocation());
 		
 		if(bs == null) 
 			return false;
 		
 		return bs.isEnabled();		
 	}
 	
 	public BoutiqueSign getBoutiqueSign(Block b)
 	{
 		return _signs.get(getLocationString(b.getLocation()));	
 	}
 	
 
 	public void saveSignData()	
 	{
 		
 	}
 	
 
 	public void useSign(Block b, Player p) 
 	{
 		BoutiqueSign bs = getBoutiqueSign(b);
 		
 		if (!bs.isEnabled())
 		{
 			//TODO: formatter le chat ailleurs ? 
 			p.sendMessage(ChatColor.YELLOW + "Le panneau n'est pas actif.");
 			p.sendMessage(ChatColor.YELLOW + "Activez le en reposant le panneau.");
 			return;
 		}		
 		
 		// See if player has permission to use signs.
 		if(!PermissionsHandler.canUseSign(p))
 		{
 			p.sendMessage(plugName + PermissionsHandler.permissionErr);
 			return;
 		}
 		
 		// Vérifie les items / lignes
 		if(!bs.checkLines(p))
 			return;
 		
 		// Lance les actions (echange / achat / vente / etc)
 		if(!bs.doAction(p))
 			return;
 		
 		// Actualise les détails du panneau
 		bs.Render();
 	}
 	
 	
 	
 	
 
 
 	//private void signSetter(String[] lines, Player p, Block s) 
 	
 	private void signSetter(Block b, Player p, String[] lines) 
 	{		
 		//TODO: virer debug
 		//p.sendMessage("dbg1");
 		
 		
 		if(b==null) 
 			return;
 		
 		BoutiqueSign bs = new BoutiqueSign();
 		
 		bs.setOwner(p);
 		bs.setLocation(b.getLocation());
 		bs.setLines(lines);
 
 		//TODO: virer debug
 		/*
 		p.sendMessage("dbg1 : line1 = " + bs.getLine1());
 		p.sendMessage("dbg1 : line2 = " + bs.getLine2());
 		p.sendMessage("dbg1 : line3 = " + bs.getLine3());
 		p.sendMessage("dbg1 : line4 = " + bs.getLine4());		
 		p.sendMessage("dbg2 : type = " + bs.getType());
 		*/
 		
 		if(bs.isSignServer())
 		{
 			
 			if (!PermissionsHandler.canSetGlobalSign(p))
 			{
 				p.sendMessage(PermissionsHandler.permissionErr);
 				return;
 			}
 			
 			if(!bs.checkLines(p))
 			{
 				return;
 			}
 			
 			p.sendMessage(plugName + "Panneau serveur ajouté à la liste :)");
 		}
 		
 		else if(bs.isSignChest())
 		{
 			if (!PermissionsHandler.canSetPersonalSign(p))
 			{
 				p.sendMessage(PermissionsHandler.permissionErr);
 				return;
 			}
 			
 			if(!bs.checkLines(p))
 			{
 				//TODO: virer debug
 				//p.sendMessage("erreur checkline");
 				return;
 			}
 			
 			p.sendMessage(plugName + "Panneau joueur ajouté à la liste :)");
 		}	
 		else if(bs.isSignWebAuction())
 		{
 			if (!PermissionsHandler.canSetWebAuctionSign(p))
 			{
 				p.sendMessage(PermissionsHandler.permissionErr);
 				return;
 			}
 			
 			if(!bs.checkLines(p))
 			{
 				//TODO: virer debug
 				//p.sendMessage("erreur checkline");
 				return;
 			}
 			
 			p.sendMessage(plugName + "Panneau web joueur ajouté à la liste :)");
 		}
 		else
 		{
 			return;
 		}
 		
 		
 		bs.setLine4("ok");			
 		bs.Render();
 		
 		this.put(bs);
 		plugin.signmanager.saveGlobalSigns();
 	}
 	
 	
 	public void displaySignInfo(Block b, Player p) 
 	{
 		//TODO virer debug
 		//p.sendMessage(plugName + "displaySignInfo");
 		
 		BoutiqueSign bs = getBoutiqueSign(b);
 		
 		String signOwnerString = bs.getOwnerString();
 		String signTypeStr = bs.getType();
 		String separator = "-----------------------------------";
 		
 
 		//debut texte
 		p.sendMessage(separator);
 		
 		if (!bs.isEnabled())
 		{
 			//TODO: formatter le chat ailleurs ? 
 			p.sendMessage(ChatColor.YELLOW + "Le panneau n'est pas actif.");
 			p.sendMessage(ChatColor.YELLOW + "Activez le en reposant le panneau.");
 			return;
 		}		
 					
 		
 		
 		/* Affiche le type de panneau et le propriétaire */
 		
 		//TODO virer debug
 		//p.sendMessage("dbg1: Type=" + bs.getType());
 		
 		if(bs.isSignServer())
 		{
 			//Type BoutiqueSignServer
 			p.sendMessage("Ce panneau fait partie du magasin du serveur.");
 		}
 		else if(bs.isSignChest())
 		{	
 			//Type BoutiqueSignChest
 			
 			p.sendMessage("Ce panneau fait partie du magasin de " + ChatColor.RED + signOwnerString + ChatColor.WHITE +  ".");			
 			
 			String signchest = bs.getChestString();			
 			if(signchest.isEmpty()) 
 			{
 				p.sendMessage(ChatColor.RED + "Aucun coffre n'est relié au panneau pour le moment !");
 			}
 			
 		}
 		else if(bs.isSignWebAuction())
 		{
 			//Type BoutiqueSignWebAuction
 			p.sendMessage("Ce panneau fait partie du magasin web de " + ChatColor.RED + signOwnerString + ChatColor.WHITE +  ".");
 		}
 		else
 		{
 			//Type inconnu
 			p.sendMessage(ChatColor.RED + "Ce panneau est d'un type inconnu et " + ChatColor.WHITE +  signOwnerString + ChatColor.RED + " en est le propriétaire." );
 			return;
 		}
 			
 		
 		Integer qtyFrom = bs.getQtyFrom();
 		Integer qtyTo = bs.getQtyTo();
 		BoutiqueItem itemTo = bs.getItemTo();
 		BoutiqueItem itemFrom = bs.getItemFrom();
 		Double moneyFrom = bs.getMoneyFrom();
 		Double moneyTo = bs.getMoneyTo();
 		
 		
 		/* Test iconomy */
 		if(!EconomyHandler.currencyEnabled)
 		{
 			p.sendMessage("Il n'y a pas iConomy sur le serveur !");
 			p.sendMessage(separator);
 			return;
 		}
 		
 		
 		if(!bs.checkLines(p))
 		{
 			//TODO virer debug
 			return;
 		}
 		
 		/* Détermine le message en fonction du type de panneau */
 		
 		//Freebies
 		if(bs.isFreebiesSign())
 		{
 			if(itemTo == null)
 			{
 				//TODO: message && currencystring
 				p.sendMessage
 				(
 					"Un clic-droit sur ce panneau te donnera " + 
 					ChatColor.RED + moneyTo + "Eus" + ChatColor.WHITE
 				);			
 			}
 			else
 			{
 				//TODO: message
 				p.sendMessage
 				(
 					"Un clic-droit sur ce panneau te donnera  " + 
 					ChatColor.RED + qtyTo + ChatColor.WHITE + 	" "	+ 
 					ChatColor.RED + itemTo.itemName + ChatColor.WHITE + 
 					" gratuitement."
 				);				
 			}
 		}
 		//Donation (item ou $ => rien)
 		else if(bs.isDonationSign())
 		{
 			if(itemFrom == null)
 			{
 				//TODO: message && currencystring
 				p.sendMessage
 				(
 					"Ce panneau recoit des dons de " + 
 					ChatColor.RED + moneyFrom + "Eus" + ChatColor.WHITE
 				);			
 			}
 			else
 			{
 				//TODO: message
 				p.sendMessage
 				(
 					"Ce panneau recoit des dons de " +
 					ChatColor.RED + qtyFrom + ChatColor.WHITE + " "	+ 
 					ChatColor.RED + itemFrom.itemName + ChatColor.WHITE
 				);				
 			}
 		}
 		else if(bs.isSellSign())
 		{
 			p.sendMessage(
 				"Un clic-droit sur ce panneau te donnera " + 
 				ChatColor.RED + qtyTo + ChatColor.WHITE + " " + 
 				ChatColor.RED + itemTo.itemName + ChatColor.WHITE + 
 				" pour la somme de " + 
 				ChatColor.RED + moneyFrom + " Eus" + ChatColor.WHITE + 
 				"."
 			);
 		}
 		else if(bs.isBuySign())
 		{
 			p.sendMessage
 			(
 				"Ce panneau te donne " + ChatColor.RED + moneyTo + "Eus " + 
 				ChatColor.WHITE + " pour chaque lot de " + 
 				ChatColor.RED + qtyFrom + "x " + itemFrom.itemName + 
 				ChatColor.WHITE + " que tu lui donneras."
 			);
 		}
 		else if(bs.isTradeSign())
 		{
 			p.sendMessage
 			(
 				"Ce panneau échange " + 
 				ChatColor.RED + qtyFrom + "x" + ChatColor.WHITE + " " + 
 				ChatColor.RED + itemFrom.itemName + ChatColor.WHITE + ", contre " +
 				ChatColor.RED + qtyTo + "x"  + ChatColor.WHITE + " " +
 				ChatColor.RED + itemTo.itemName + ChatColor.WHITE + "."
 			);
 		}
 		else
 		{
 			p.sendMessage
 			(
 				"VROUM VROUM, c'est quoi ce panneau omg, il a fumé ou quoi ???"
 			);
 			
 			return;
 		}
 		
 		bs.Render();
 		
 		p.sendMessage(separator);
 	}
 
 
 	
 	
 	
 	
 
 
 	public boolean sellItem(BoutiqueSign bs, Player p)
 	{
 		Double costAmount = bs.getMoneyFrom();
 		String signOwner = bs.getOwnerString();
 		Integer qty = bs.getQtyTo();
 		Integer id = bs.getItemTo().itemId;
 		Integer damage = bs.getItemTo().itemDamage;
 		
 		int econ = EconomyHandler.hasEnough(p.getName(), costAmount);
 		
 		//TODO changer plugname par chatprefix
 		String plugName = "";
 		
 		//Signe Serveur
 		if (bs.isSignServer())
 		{
 			if (econ != 1)
 			{
 				p.sendMessage(plugName + EconomyHandler.getEconError(econ));
 				return false;
 			}
 
 			EconomyHandler.modifyMoney(p.getName(), -costAmount);
 			PlayerOperator.givePlayerItem(qty, id, damage, p);
 			
 			//todo: serverplayername
 			signOwner = "[Serveur]";
 		}
 		//Signe coffre
 		else if (bs.isSignChest())
 		{
 			
 			Chest chest = bs.getChest();
 			
 			if(chest == null)
 			{			
 				p.sendMessage(plugName + "Pas trouvé de coffre associé au panneau");
 				return false;
 			}
 			
 			if (econ != 1)
 			{
 				p.sendMessage(plugName + EconomyHandler.getEconError(econ));
 				return false;
 			}
 			else if (!ChestOperator.containsEnough(qty, id, damage, chest))
 			{
 				p.sendMessage(plugName + ChestOperator.notEnoughErr);
 				return false;
 			}
 			
 			
 			int econOwner = EconomyHandler.modifyMoney(signOwner, costAmount);
 			if (econOwner != 1)
 			{
 				p.sendMessage(plugName + EconomyHandler.getEconError(econ));
 				return false;
 			}
 			
 			
 			
 			EconomyHandler.modifyMoney(p.getName(), -costAmount);			
 			ChestOperator.removeFromChestStock(qty, id, damage, chest);
 			PlayerOperator.givePlayerItem(qty, id, damage , p);
 		}
 		
 		//Signe webauction
 		else if (bs.isSignWebAuction())
 		{
 			//TODO virer debug
 			//p.sendMessage("dbg1: isSignWebAuction");
 			
 			
 			if (econ != 1)
 			{
 				p.sendMessage(plugName + EconomyHandler.getEconError(econ));
 				return false;
 			}
 			//TO CHANGE
 			if(!plugin.db.wa_HasEnoughItem(signOwner, id, damage, qty))
 			{
 				p.sendMessage(plugName + WebItemsOperator.notEnoughErr);
 				return false;
 			}
 			
 			int econOwner = EconomyHandler.modifyMoney(signOwner, costAmount);
 			if (econOwner != 1)
 			{
 				p.sendMessage(plugName + EconomyHandler.getEconError(econ));
 				return false;
 			}
 			
 			//TODO virer debug
 			//p.sendMessage("dbg2");
 			
 			if(!plugin.db.wa_RemoveFromStock(signOwner, id, damage, qty))
 			{
 				p.sendMessage("Impossible d'enlever dans les stocks web !");
 				
 				return false;
 			}
 			
 				
 				
 			int retmoney = EconomyHandler.modifyMoney(p.getName(), -costAmount);
 			
 			//TODO virer debug
 			//p.sendMessage("dbg4: retmoney = " + retmoney);
 			
 			PlayerOperator.givePlayerItem(qty, id, damage, p);	
 			
 			//TODO virer debug
 			//p.sendMessage("dbg4: wa_RemoveFromStock");
 			
 		}
 		
 		
 		p.sendMessage(plugName + "Tu as recu " + qty + " " + bs.getItemTo().itemName);
 		p.sendMessage(plugName + "Il as désormais " + EconomyHandler.playerHave(p.getName()));
 			
 		try 
 		{
 			plugin.db.logTransaction(p.getLocation(), signOwner, p.getName(),  id , damage , qty, costAmount,"toto","titi");
 		}
 		catch (SQLException e) 
 		{
 			e.printStackTrace();
 		} 
 		catch (Exception e) 
 		{
 			e.printStackTrace();
 		} 
 		
 		return true;
 		
 	}
 	
 	
 	public boolean giveFree(BoutiqueSign bs, Player p)
 	{
 		Double costAmount = bs.getMoneyFrom();
 		String signOwner = bs.getOwnerString();
 		Integer qty = bs.getQtyTo();
 		Integer id = bs.getItemTo().itemId;
 		Integer damage = bs.getItemTo().itemDamage;
 		
 		int econ = EconomyHandler.hasEnough(p.getName(), costAmount);
 			
 		//Signe Serveur
 		if (bs.isSignServer())
 		{
 			PlayerOperator.givePlayerItem(qty, id, damage, p);
 			signOwner = "[Serveur]";
 		}
 		//Signe coffre
 		else if (bs.isSignChest())
 		{
 			
 			Chest chest = bs.getChest();
 			
 			if(chest == null)
 			{			
 				p.sendMessage(plugName + "Pas trouvé de coffre associé au panneau");
 				return false;
 			}
 			
 			if (!ChestOperator.containsEnough(qty, id, damage, chest))
 			{
 				p.sendMessage(plugName + ChestOperator.notEnoughErr);
 				return false;
 			}
 			
 			ChestOperator.removeFromChestStock(qty, id, damage, chest);
 			PlayerOperator.givePlayerItem(qty, id, damage , p);
 		}
 		
 		//Signe webauction
 		else if (bs.isSignWebAuction())
 		{
 			//TODO virer debug
 			//p.sendMessage("dbg1: isSignWebAuction");
 			
 			//TO CHANGE
 			if(!plugin.db.wa_HasEnoughItem(signOwner, id, damage, qty))
 			{
 				p.sendMessage(plugName + WebItemsOperator.notEnoughErr);
 				return false;
 			}
 			
 			if(!plugin.db.wa_RemoveFromStock(signOwner, id, damage, qty))
 			{
 				p.sendMessage("Impossible d'enlever dans les stocks web !");
 				return false;
 			}
 			
 			PlayerOperator.givePlayerItem(qty, id, damage, p);	
 		}
 
 		p.sendMessage(plugName + "Tu as recu " + qty + " " + bs.getItemTo().itemName);
 		
 		//p.sendMessage(plugName + "Il te reste " + EconomyHandler.playerHave(p.getName()));
 			
 		try 
 		{
 			plugin.db.logTransaction(p.getLocation(), signOwner, p.getName(),  id , damage , qty, 0.0,"toto","titi");
 		}
 		catch (SQLException e) 
 		{
 			e.printStackTrace();
 		} 
 		catch (Exception e) 
 		{
 			e.printStackTrace();
 		} 
 
 		return true;
 	}
 	
 	
 	
 	
 	
 	public boolean buyItem(BoutiqueSign bs, Player p)
 	{
 		Double costAmount = bs.getMoneyTo();
 		String signOwner = bs.getOwnerString();
 		Integer qty = bs.getQtyFrom();
 		Integer id = bs.getItemFrom().itemId;
 		Integer damage = bs.getItemFrom().itemDamage;
 		
 		//int econ = EconomyHandler.hasEnough(p.getName(), costAmount);
 		
 		//TODO changer plugname par chatprefix
 		String plugName = "";
 		
 		if (!PlayerOperator.playerHasEnough( qty, id, damage, p))
 		{
 			p.sendMessage(plugName + PlayerOperator.playerStockErr);
 			return false;
 		}
 		
 		// Panneau "Serveur"
 		if (bs.isSignServer())
 		{
 			//Enleve les objets dans l'inventaire du joueur
 			PlayerOperator.removeFromPlayer(qty, id ,damage, p);
 			
 			//Ajoute les thunes au compte du joueur
 			EconomyHandler.modifyMoney(p.getName(), costAmount);
 			
 			//Enleve le propriétaire du panneau pour les logs (car panneau serveur) !
 			signOwner = "";
 		}
 		
 		// Panneau "webauctions"
 		// Achete des objet, et les met dans le stock "webauctions"
 		else if (bs.isSignWebAuction())
 		{
 			
 			// Vérifie que l'acheteur à assez d'argent
 			int econ = EconomyHandler.hasEnough(signOwner, costAmount);
 			if (econ != 1)
 			{
 				p.sendMessage(plugName + EconomyHandler.getEconError(econ));
 				return false;
 			}
 			
 			// Ajoute les objets au stock WebAuctions
 			if(!plugin.db.wa_AddToStock(p.getName(), id, damage, qty))
 			{
 				p.sendMessage(plugName + "Erreur lors de l'ajout dans les stocks web de "  + signOwner + " :(");
 				return false;
 			}
 			
 			
 			// Enleve les objets de l'inventaire du vendeur
 			PlayerOperator.removeFromPlayer(qty, id, damage, p);
 			
 			// Enleve la somme du compte de l'acheteur
 			EconomyHandler.modifyMoney(signOwner, -costAmount);
 			
 			// Credite le vendeur
 			EconomyHandler.modifyMoney(p.getName(), costAmount);
 			
 		}
 		
 		// Panneau "coffre"
 		// Achete des objet, et les met dans le coffre relié
 		else if (bs.isSignChest())
 		{
 			//TODO virer debug
 			//p.sendMessage("dbg0: debut signchest");
 			
 			// Cherche le coffre relié au panneau
 			Chest chest = bs.getChest();
 			if(chest==null)	
 			{
 				//TODO virer debug
 				//p.sendMessage("dbg1: coffre introuvable");
 				return false;
 			}
 				
 			
 			
 			// Vérifie qu'il reste de la place dans le coffre pour stocker les objets à acheter
 			if (!ChestOperator.hasEnoughSpace(qty,id,damage, chest))
 			{
 				p.sendMessage(plugName + ChestOperator.notEnoughSpaceErr);
 				return false;
 			}			
 			
 			// Vérifie que l'acheteur possède assez d'argent
 			int econ = EconomyHandler.hasEnough(signOwner, costAmount);
 			if (econ != 1)
 			{
 				p.sendMessage(plugName + EconomyHandler.getEconError(econ));
 				return false;
 			}
 			
 			// Debitte la somme au a l'acheteur
 			EconomyHandler.modifyMoney(signOwner, -costAmount);
 			
 			// Credite le coffre de l'acheteur
 			ChestOperator.addToChestStock(qty, id, damage, chest);
 			
 			// Retire les objets au vendeur
 			PlayerOperator.removeFromPlayer(qty, id, damage, p);
 			
 			// Credite le compte du vendeur
 			EconomyHandler.modifyMoney(p.getName(), costAmount);
 			
 			//TODO virer debug
 			//p.sendMessage("dbg2: fin signchest");
 		}
 		
 		
 		p.sendMessage(plugName + "Tu as maintenant " + EconomyHandler.playerHave(p.getName()) + ".");
 		
 		try 
 		{
 			Boutique.getInstance().db.logTransaction(p.getLocation(), p.getName(), signOwner, id, damage, qty, costAmount,"toto","titi");
 		}
 		catch (Exception e) 
 		{
 			// TODO
 			e.printStackTrace();
 		} 
 		
 		return true;
 	}
 	
 	
 
 	public boolean getDonation(BoutiqueSign bs, Player p)
 	{
 		//Double costAmount = bs.getMoneyTo();
 		
 		String signOwner = bs.getOwnerString();
 
 		
 		//TODO changer plugname par chatprefix
 		String plugName = "";
 		
 		
 		//Teste donation item ou thunes ?
 		
 		//Don d'item
 		if(bs.getItemFrom() != null)
 		{
 			Integer qty = bs.getQtyFrom();
 			Integer id = bs.getItemFrom().itemId;
 			Integer damage = bs.getItemFrom().itemDamage;
 			
 			if (!PlayerOperator.playerHasEnough( qty, id, damage, p))
 			{
 				p.sendMessage(plugName + PlayerOperator.playerStockErr);
 				return false;
 			}
 
 			if (bs.isSignServer())
 			{
 				//Enleve le propriétaire du panneau pour les logs (car panneau serveur) !
 				signOwner = "";
 			}
 			else if (bs.isSignWebAuction())
 			{
 				// Panneau "webauctions"
 				// Ajoute les objets au stock WebAuctions
 				if(!plugin.db.wa_AddToStock(p.getName(), id, damage, qty))
 				{
 					p.sendMessage(plugName + "Erreur lors de l'ajout dans les stocks web de "  + signOwner + " :(");
 					return false;
 				}
 			}			
 			else if (bs.isSignChest())
 			{
 				//p.sendMessage("dbg0: debut signchest");
 				
 				// Cherche le coffre relié au panneau
 				Chest chest = bs.getChest();
 				if(chest==null)	
 				{
 					//p.sendMessage("dbg1: coffre introuvable");
 					return false;
 				}
 								
 				// Vérifie qu'il reste de la place dans le coffre pour stocker les objets à acheter
 				if (!ChestOperator.hasEnoughSpace(qty,id,damage, chest))
 				{
 					p.sendMessage(plugName + ChestOperator.notEnoughSpaceErr);
 					return false;
 				}			
 
 				// Credite le coffre de l'acheteur
 				ChestOperator.addToChestStock(qty, id, damage, chest);
 			}
 			
 			// Retire les objets au donneur
 			PlayerOperator.removeFromPlayer(qty, id, damage, p);
 			
 			p.sendMessage(plugName + "Tu as donné " + qty + "x " + bs.getItemFrom().itemName + " " + (bs.isSignServer() ? "au serveur.": "à " + signOwner + "."));
 			
 			
 			try 
 			{
 				Boutique.getInstance().db.logTransaction(p.getLocation(), p.getName(), signOwner, id , damage, qty , 0.0 ,"toto","titi");
 			}
 			catch (Exception e) 
 			{
 				e.printStackTrace();
 			}
 			
 		}
 		else if(bs.getMoneyFrom() != null)
 		{
 			
 			//Don d'argent
 			
 			
 			//Verif money doneur
 			if (EconomyHandler.hasEnough(p.getName(), bs.getMoneyFrom())!=1)
 			{
 				p.sendMessage(plugName + EconomyHandler.noFundsErr);
 				return false;
 			}
 			
 			//Crédite le receveur (sauf si serveur)
 			if(!bs.isSignServer())
 			{
 				EconomyHandler.modifyMoney(bs.getOwnerString(), bs.getMoneyFrom());
 			}
 			
 			//Débite le doneur
 			EconomyHandler.modifyMoney(p.getName(), -bs.getMoneyFrom());
 			
 
 			//TODO: messages configurables
 			p.sendMessage(plugName + "Tu as donné " + BoutiqueSign.formatMoney(bs.getMoneyFrom()) + " " + BoutiqueSign.formatCurrency(bs.getMoneyFrom()) + " " + (bs.isSignServer() ? "au serveur.": "à " + signOwner));			
 			p.sendMessage(plugName + "Il te reste " + EconomyHandler.playerHave(p.getName()));
 			
 			
 			try 
 			{
 				Boutique.getInstance().db.logTransaction(p.getLocation(), p.getName(), signOwner, 0, 0, 0, bs.getMoneyFrom() ,"toto","titi");
 			}
 			catch (Exception e) 
 			{
 				e.printStackTrace();
 			}
 			
 		}
 		else
 		{
 			//TODO: message Hein ta fumé
 			p.sendMessage(plugName + "VROUM VROUM");
 			return false;
 		}
 
 		return true;
 	}
 	
 
 	public static boolean tradeItems(BoutiqueSign bs, Player p)
 	{
 		
 		//TODO changer plugname par chatprefix
 		String plugName = "";
 		
 		
 		int qtyFrom = bs.getQtyFrom();
 		int qtyTo = bs.getQtyTo();
 		int damageFrom = bs.getItemFrom().itemDamage;
 		int damageTo = bs.getItemTo().itemDamage;
 		int idFrom = bs.getItemFrom().itemId;
 		int idTo = bs.getItemTo().itemId;
 		
 		
 		
 		if (bs.isSignServer())
 		{
 			if (!PlayerOperator.playerHasEnough(qtyFrom, idFrom, damageFrom, p))
 			{
 				p.sendMessage(plugName + PlayerOperator.playerStockErr);
 				return false;
 			}
 			
 			PlayerOperator.removeFromPlayer(qtyFrom, idFrom, damageFrom, p);
 			PlayerOperator.givePlayerItem(qtyTo, idTo, damageTo, p);
 		
 		}
 		else if (bs.isSignChest())
 		{
 			
 			Chest chest = bs.getChest();
 			if(chest==null)
 				return false;
 			
 			if (!PlayerOperator.playerHasEnough(qtyFrom, idFrom, damageFrom, p))
 			{
 				p.sendMessage(plugName + PlayerOperator.playerStockErr);
 				return false;
 			}
 			else if (!ChestOperator.containsEnough(qtyTo, idTo, damageTo, chest))
 			{
 				p.sendMessage(plugName + ChestOperator.notEnoughErr);
 				return false;
 			}
 			else if (!ChestOperator.hasEnoughSpace(qtyFrom, idFrom, damageFrom, chest))
 			{
 				p.sendMessage(plugName + ChestOperator.notEnoughSpaceErr);
 				return false;
 			}
 			
 			
 			ChestOperator.removeFromChestStock(qtyTo, idTo, damageTo, chest);
 			ChestOperator.addToChestStock(qtyFrom, idFrom, damageFrom, chest);
 			
 			PlayerOperator.removeFromPlayer(qtyFrom, idFrom, damageFrom, p);
 			PlayerOperator.givePlayerItem(qtyTo, idTo, damageTo, p);
 						
 		}
 		else if (bs.isSignWebAuction())
 		{
 
 			if (!PlayerOperator.playerHasEnough(qtyFrom, idFrom, damageFrom, p))
 			{
 				p.sendMessage(plugName + PlayerOperator.playerStockErr);
 				return false;
 			}
 			else if (!WebItemsOperator.containEnough(bs.getOwnerString(), idTo, damageTo, qtyTo))
 			{
 				//TODO: message "La personne avec qui tu échanges n'a plus assez d'items à échanger !"
 				p.sendMessage(plugName + "La personne avec qui tu échanges n'a plus assez d'items en stock !");
 				return false;
 			}
 			
 			
 			//Prend l'objet source
 			PlayerOperator.removeFromPlayer(qtyFrom, idFrom, damageFrom, p);
 			WebItemsOperator.addToWebStock(bs.getOwnerString(), idFrom, damageFrom, qtyFrom);
 			
 			//Donne l'objet de destination
 			WebItemsOperator.removeFromWebStock(bs.getOwnerString(), idTo, damageTo, qtyTo);
 			PlayerOperator.givePlayerItem(qtyTo, idTo, damageTo, p);						
 		}
 		
 		
 		
 		
 		
 		//log transaction
 		
 		try 
 		{
 			Boutique.getInstance().db.logTransaction(p.getLocation(), p.getName(), bs.getOwnerString(), idFrom, damageFrom, qtyFrom, 0.0, "toto", "TRADE");
 			Boutique.getInstance().db.logTransaction(p.getLocation(), bs.getOwnerString(), p.getName(), idTo, damageTo, qtyTo, 0.0, "toto", "TRADE");		
 		}
 		catch (Exception e) 
 		{
 			// TODO
 			e.printStackTrace();
 		} 
 		
 		return true;
 
 	}
 
 	public void put(String locationstr, BoutiqueSign bs)
 	{
 		this._signs.put(locationstr, bs);
 	}
 	
 	public void put(Location location, BoutiqueSign bs)
 	{
 		this._signs.put(getLocationString(location), bs);
 	}
 	
 	public void remove(Location location)
 	{
		this._signs.remove(BoutiqueSign.getLocationString(location));
 	}
 
 
 	public void saveGlobalSigns()
 	{
 		// TODO Auto-generated method stub
 		plugin.fileio.saveGlobalSigns();
 	}
 
 
 	public Set<Entry<String, BoutiqueSign>> entrySet()
 	{
 		return this._signs.entrySet();
 	}
 
 
 	public void setChest(Sign sign, Chest chest, Player p)
 	{
 		setChest(sign.getBlock(), chest,  p);
 		return;
 	}
 	
 	/* Enregistre le coffre pour un BoutiqueSignChest */
 	public void setChest(Block sign, Chest chest, Player p)
 	{
 		BoutiqueSign bs = this.getBoutiqueSign(sign);
 		
 		
 		if(bs == null)
 		{
 			// TODO: message "Impossible de trouver le panneau en question"
 			p.sendMessage(plugName + "Choisi un panneau avant !");
 			return;
 		}
 		else
 		{
 			String newChestLoc = "";
 			String oldChestLoc = "";
 			
 			Chest bsc = bs.getChest();
 			
 			
 			oldChestLoc = bs.getChestString();
 			newChestLoc = BoutiqueSign.getLocationString(chest.getBlock().getLocation());
 			
 			
 			
 			//verifie que le panneau n'etait pas deja relié au coffre			
 			if (oldChestLoc == newChestLoc)
 			{
 				p.sendMessage(plugName + "Ce panneau est déjà relié à ce coffre :/");
 				return;
 			}
 			
 			int distX = sign.getX() - chest.getBlock().getX();
 			int distZ = sign.getZ() - chest.getBlock().getZ();
 		
 			
 			int maxDist = 15;
 			if (distX > maxDist || distZ > maxDist ) 
 			{
 				p.sendMessage(plugName + "Le coffre est trop loin du panneau ! (" + maxDist + " blocs max.)");
 				return;
 			}
 			
 			//DEBUG
 			//p.sendMessage(plugName + "Enregistrement coffre");
 						
 			bs.setChest(chest);			
 			
 			updateSignDb(bs);
 			
 			p.sendMessage(plugName + "Panneau et coffre reliés !");
 					
 			//DEBUG
 			//p.sendMessage(plugName + "panneau: " + bs.getChestString());
 			
 		}		
 	}
 
 
 
 	
 	
 	public void setOwner(Sign s, Player p, String newowner)
 	{
 		
 		
 		if (!isSign(s))
 		{
 			p.sendMessage(plugName + "Ce panneau est mal configuré. Reposes le correctement avant.");
 			return;
 		}
 		
 		
 		
 		Location location = s.getBlock().getLocation();
 		
 		
 		BoutiqueSign bs = getBoutiqueSign(s.getBlock());
 		if(bs==null)
 			return;
 		
 		bs.setOwnerString(newowner);
 		
 		updateSignDb(bs);
 		
 	}
 
 
 	
 	///
 	/// Le joueur peut-il casser le panneau ?
 	///
 	public boolean canBreakSign(Sign s, Player p) 
 	{
 		if (!this.isSign(s))
 			return false;
 		
 		boolean bool = false;
 		bool = isSignOwner(s,p);
 		if (!bool)
 			bool = p.isOp();
 		
 		return bool;
 	}
 	
 	
 
 
 	public void setSign(Block b, Player p, String[] lines) 
 	{
 		this.signSetter(b,p,lines);
 		
 	}
 
 	
 	
 
 
 	public boolean isSign(Block b) 
 	{
 		if (!(b.getState() instanceof Sign))
 			return false;
 		
 		return isSign((Sign) b);
 	}
 	
 	public boolean isSign(Sign s) 
 	{
 		return haveLocation(s.getBlock().getLocation());
 	}
 	
 
 	/* Renvoi true si le player est le proprio du panneau */
 	public boolean isSignOwner(Sign sign, Player p)
 	{	
 		return getBoutiqueSign(sign.getBlock()).getOwnerString().compareToIgnoreCase(p.getName()) == 0;
 	}
 
 	
 	public boolean isBoutiqueSign(Sign clickedSign)
 	{
 		return haveLocation(clickedSign.getBlock().getLocation());
 	}
 
 	public boolean isBoutiqueSign(Block clickedBlock)
 	{
 		return haveLocation(clickedBlock.getLocation());
 	}
 
 
 	
 	
 
 	private void insertSignDb(BoutiqueSign bs)
 	{
 		//TODO: if datafile
 		put(bs);
 		saveGlobalSigns();
 		
 		//TODO: else if datasql
 		
 	}
 
 	private void updateSignDb(BoutiqueSign bs)
 	{
 		//TODO: if datafile
 		put(bs);
 		saveGlobalSigns();
 		
 		//TODO: else if datasql
 	}
 	
 	public void loadGlobalSignData()
 	{
 		plugin.fileio.loadGlobalSignData();
 	}
 
 	public void put(BoutiqueSign bs)
 	{
 		if(this._signs.containsKey(bs.getLocationString()))
 		{
 			this._signs.remove(bs.getLocationString());
 		}
 		
 		put(bs.getLocationString(), bs);		
 	}
 
 	public boolean isChestOwner(Chest chest, Player p)
 	{
 		//TODO: Check worldguard
 		//TODO: Cehck LWC
 		return WorldGuardOperator.canBuild(p, chest.getBlock());
 	}
 
 	
 
 
 	
 
 	
 
 }
