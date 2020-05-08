 package fr.vaevictis.vveconomy;
 
import java.util.HashSet;
import java.util.Set;

 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class VVEconomy extends JavaPlugin
 {
 	
 	@Override
 	public void onEnable()
 	{
		HashSet<String, Location> JoueurInBank = new HashSet<String, Location>();
 	}
 	
 	@Override
 	public void onDisable()
 	{
 		
 	}
 	
 	/*public int toId(String entree)
 	{
 		entree.toLowerCase();
 		switch (entree)
 		{
 		case "terre":
 			return 3;
 		case "cobblestone":
 			return 4;
 		case "stone":
 			return 1;
 		case "sable":
 			return 12;
 		case "sandstone":
 			return 24;
 		case "charbon":
 			return 263;
 		case "bois":
 			return 17;
 		}
 			
 	}*/
 	
 	/*
 	public void saveLocationJoueur(Player p, Location l)
 	{
 		Joueur j = new Joueur(p);
 		j.setAnciennePosition(j, l);
 		FileConfiguration config = this.getConfig();
 		config.set("world." + p.getName(), l.getWorld().getName());
 		config.set("X." + p.getName(), l.getX());
 		config.set("Y." + p.getName(), l.getY());
 		config.set("Z." + p.getName(), l.getZ());
 		config.saveConfig();
 		
 	}
 	*/
 	
 	/*
 	public void resetLocationJoueur(Player p)
 	{
 		FileConfiguration config = this.getConfig();
 		String world = "world";
 		config.set("world." + p.getName(), world);
 		config.set("X." + p.getName(), 0);
 		config.set("Y." + p.getName(), 0);
 		config.set("Z." + p.getName(), 0);
 		config.saveConfig();
 		
 	}
 	*/
 	
 	/*public Location getAncienneLocationJoueur(Player p)
 	{
 		/*
 		if (Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").getConfig().get("X." + p.getName()) == null)
 		{
 			this.resetLocationJoueur(p);
 		}
 		Location loc = new Location(Bukkit.getServer().getWorld(Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").getConfig().get("world." + p.getName())), Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").getConfig().get("X." + p.getName()), Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").getConfig().get("X." + p.getName()), Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").getConfig().get("X." + p.getName()));
 		
 		return loc;
 	}
 	*/
 
 	
 	
 	public void setLocationBanque(Location l)
 	{
 		Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").getConfig().set("banqueWorld", l.getWorld().getName());
 		Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").getConfig().set("banqueX", l.getX());
 		Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").getConfig().set("banqueY", l.getY());
 		Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").getConfig().set("banqueZ", l.getZ());
 		Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").saveConfig();
 	}
 	
 	//TODO � v�rifier (getWorld � besoin d'une Chaine et il obtient un Object pour le moment)
 	public Location getBanqueLocation()
 	{
		int x = Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").getConfig().getInt("banqueX");
		double xdouble = (double) x;
		int y = Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").getConfig().getInt("banqueY");
		double ydouble = (double) y;
		int z = Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").getConfig().getInt("banqueZ");
		double zdouble = (double) z;
		Location pos = new Location(Bukkit.getServer().getWorld(Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").getConfig().getString("banqueWorld")), xdouble, ydouble, zdouble);
 		return pos;
 	}
 	
 	public int getArgentBanque()
 	{
 		if (Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").getConfig().get("argentBanque") == null)
 		{
 			Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").getConfig().set("argentBanque", 0);
 			Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").saveConfig();
 		}
 		return Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").getConfig().getInt("argentBanque");
 	}
 	
 	public void ajouterArgentBanque(int somme)
 	{
 		int argentActuel = this.getArgentBanque();
 		int argentApres = argentActuel + somme;
 		Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").getConfig().set("argentBanque", argentApres);
 		Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").saveConfig();
 	}
 	
 	public boolean soustraireArgentBanque(int somme)
 	{
 		int argentActuel = this.getArgentBanque();
 		int argentApres = argentActuel - somme;
 		if (argentApres >= 0)
 		{
 			Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").getConfig().set("argentBanque", argentApres);
 			Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").saveConfig();
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}
 	
 	public static int getArgent(Player p)
 	{
 		if (Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").getConfig().get("argent." + p.getName()) == null)
 		{
 			Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").getConfig().set("argent." + p.getName(), 0);
 			Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").saveConfig();
 		}
 		return Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").getConfig().getInt("argent." + p.getName());
 	}
 	
 	public int getStock(ItemStack item)
 	{
 		if (Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").getConfig().get("stock." + item.getType().toString()) == null)
 		{
 			Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").getConfig().set("stock." + item.getType().toString(), 0);
 			Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").saveConfig();
 		}
 		return Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").getConfig().getInt("stock." + item.getType().toString());
 	}
 	
 	public boolean soustraireStock(int quantite, ItemStack item)
 	{
 		if (Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").getConfig().get("stock." + item.getType().toString()) == null)
 		{
 			return false;
 		}
 		else if (this.getStock(item) < quantite)
 		{
 			return false;
 		}
 		else if (this.getStock(item) >= quantite)
 		{
 			int stockActuel = this.getStock(item);
 			int stockApres = stockActuel - quantite;
 			Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").getConfig().set("stock." + item.getType().toString(), stockApres);
 			Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").saveConfig();
 			return true;
 		}		
 		return false;
 	}
 	
 	public void ajouterStock(int quantite, ItemStack item)
 	{
 		if (Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").getConfig().get("stock." + item.getType().toString()) == null)
 		{
 			Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").getConfig().set("stock." + item.getType().toString(), 0);
 			Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").saveConfig();
 		}
 		int stockActuel = this.getStock(item);
 		int stockApres = stockActuel + quantite;
 		Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").getConfig().set("stock." + item.getType().toString(), stockApres);
 		Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").saveConfig();
 	}
 	
 	public void setCurrency(ItemStack item, int currency)
 	{
 		if (Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").getConfig().get("currency." + item.getType().toString()) == null)
 		{
 			Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").getConfig().set("currency." + item.getType().toString(), 0);
 		}
 		Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").getConfig().set("currency." + item.getType().toString(), currency);
 		Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").saveConfig();
 	}
 	
 	//Donne la valeur en as associ�e � un ID
 	public int getCurrency(ItemStack item)
 	{
 		if (Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").getConfig().get("currency." + item.getType().toString()) == null)
 		{
 			return -1;
 		}
 		return Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").getConfig().getInt("currency." + item.getType().toString());
 	}
 	
 	//Donne la valeur que devra payer le client pour acheter la quantite de blocs qu'il a demand�
 	public int getPrixAchat(ItemStack item)
 	{
 		int currency = this.getCurrency(item);
 		int stock = this.getStock(item);
 		int prix = ((-(3/172) * currency * stock) + ((4 * currency) + ((3/172) * currency))) * 4;
 		if (currency == -1)
 		{
 			return -1;
 		}
 		else if (prix <= 1)
 		{
 			return 1;
 		}
 		else
 		{
 			return prix;
 		}
 	}
 	
 	//Donne la valeur que devra payer la banque au client lui vendant des blocs
 	public int getPrixVente(ItemStack item)
 	{
 		int currency = this.getCurrency(item);
 		int stock = this.getStock(item);
 		int prix = (-(3/172) * currency * stock) + ((4 * currency) + ((3/172) * currency));
 		if (currency == -1)
 		{
 			return -1;
 		}
 		else if (prix <= 1)
 		{
 			return 1;
 		}
 		else
 		{
 			return prix;
 		}
 	
 	}
 	
 	public static void payer(Player p1, Player p2, int as)
 	{
 		if (Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").getConfig().get("argent." + p1.getName()) == null)
 		{
 			Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").getConfig().set("argent." + p1.getName(), 0);
 			Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").saveConfig();
 		}
 		if (Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").getConfig().get("argent." + p2.getName()) == null)
 		{
 			Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").getConfig().set("argent." + p2.getName(), 0);
 			Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").saveConfig();
 		}
 		if (Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").getConfig().getInt("argent." + p1.getName()) >= as)
 		{
 			setArgent(p1, getArgent(p1) - as);
 			setArgent(p2, getArgent(p2) + as);
 			Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").saveConfig();
 		}
 	}
 	public static void setArgent(Player p, int as)
 	{
 		if (Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").getConfig().get("argent." + p.getName()) == null)
 		{
 			Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").getConfig().set("argent." + p.getName(), 0);
 		}
 		Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").getConfig().set("argent." + p.getName(), as);
 		Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").saveConfig();
 	}
 	
 	public boolean onCommand(CommandSender sender, Command command, String label,String[] args)
 	{
 		Player p = (Player) sender;
 		Joueur j = (Joueur) p;
 		
 		if (label.equalsIgnoreCase("argent") && p.hasPermission("vveconomy.basic"))
 		{
 			if (args.length == 0)
 			{
 				p.sendMessage(ChatColor.GOLD + "Vous poss�dez " + getArgent(p) + " As.");
 				return true;
 			}
 			else if (args.length == 1)
 			{
 				Player p2 = Bukkit.getServer().getPlayer(args[0]);
 				if (p2 != null)
 				{
 					p.sendMessage(ChatColor.GOLD + p2.getDisplayName() + " poss�de " + getArgent(p2) + " As.");
 				}
 			}
 		}
 		else if (label.equalsIgnoreCase("payer") && args.length == 2 && p.hasPermission("vveconomy.basic"))
 		{
 			Player p2 = Bukkit.getServer().getPlayer(args[0]);
 			if (p2 != null && Integer.valueOf(args[1]) > 0)
 			{
 				if (Integer.valueOf(args[1]) > getArgent(p))
 				{
 					p.sendMessage(ChatColor.GOLD + "Vous n'avez pas assez d'argent !");
 					return true;
 				}
 				payer(p, p2, Integer.valueOf(args[1]));
 				p.sendMessage(ChatColor.GOLD + "Vous avez envoy� " + Integer.valueOf(args[1]) + " As � " + p2.getDisplayName() + ".");
 				p2.sendMessage(ChatColor.GOLD + "Vous avez re�u " + Integer.valueOf(args[1]) + " As de " + p.getDisplayName() + ".");
 				return true;
 			}
 		}
 		else if (label.equalsIgnoreCase("mettreargent") && args.length == 2 && p.hasPermission("vveconomy.admin"))
 		{
 			Player p2 = Bukkit.getServer().getPlayer(args[0]);
 			if(p2 != null && Integer.valueOf(args[1]) > 0)
 			{
 				setArgent(p2, Integer.valueOf(args[1]));
 				p.sendMessage(ChatColor.GOLD + p2.getDisplayName() + " a d�sormais " + Integer.valueOf(args[1]) + " As.");
 				return true;
 			}
 		}
 		else if (label.equalsIgnoreCase("banque" ) && p.hasPermission("vveconomy.basic"))
 		{
 			if (args[0].equalsIgnoreCase("tp"))
 					{
 				//TODO tp � la banque
 				j.setAnciennePosition(p.getLocation());
 				j.setHasQuit(false);
 				p.teleport(this.getBanqueLocation());
 					}
 			else if (args[0].equalsIgnoreCase("quitter"))
 			{
 				if (!j.isHasQuit())
 				{
 					j.setHasQuit(true);
 					p.teleport(j.getAnciennePosition());
 				}
 				else
 				{
 					p.sendMessage("Vous vous �tes d�connect� depuis votre derni�re t�l�portation � la banque, vous allez �tre ramen� chez vous");
 					//execution de la commande /maison
 					p.performCommand("maison");
 				}
 			}
 			else if (args[0].equalsIgnoreCase("vendre"))
 			{
 				//TODO � v�rifier
 				if (p.getItemInHand().getType() == Material.STONE || p.getItemInHand().getType() == Material.DIRT || p.getItemInHand().getType() == Material.COBBLESTONE || p.getItemInHand().getType() == Material.SAND || p.getItemInHand().getType() == Material.SANDSTONE || p.getItemInHand().getType() == Material.OBSIDIAN || p.getItemInHand().getType() == Material.NETHER_BRICK || p.getItemInHand().getType() == Material.NETHERRACK || p.getItemInHand().getType() == Material.GLOWSTONE_DUST || p.getItemInHand().getType() == Material.SOUL_SAND || p.getItemInHand().getType() == Material.REDSTONE  && p.getItemInHand().getAmount() == 64)
 				{
 					int prixVente = this.getPrixVente(p.getItemInHand());
 					if (this.soustraireArgentBanque(prixVente) == true)
 					{
 						this.ajouterStock(64, p.getItemInHand());
 						p.setItemInHand(new ItemStack(0));
 						int argentJoueurActuel = VVEconomy.getArgent(p);
 						int argentJoueur = argentJoueurActuel + prixVente;
 						VVEconomy.setArgent(p, argentJoueur);
 					}
 					else
 					{
 						p.sendMessage(ChatColor.GOLD + "La banque ne dispose pas d'assez d'argent pour acheter vos biens");
 					}
 				
 				}
 				else if (p.getItemInHand().getType() == Material.COAL || p.getItemInHand().getType() == Material.IRON_INGOT || p.getItemInHand().getType() == Material.GOLD_INGOT && p.getItemInHand().getAmount() >= 16)
 				{
 					int prixVente = this.getPrixVente(p.getItemInHand());
 					if (this.soustraireArgentBanque(prixVente) == true)
 					{
 						this.ajouterStock(16, p.getItemInHand());
 						int reste = p.getItemInHand().getAmount() - 16;
 						p.setItemInHand(new ItemStack(p.getItemInHand().getType(), reste));
 						int argentJoueurActuel = VVEconomy.getArgent(p);
 						int argentJoueur = argentJoueurActuel + prixVente;
 						VVEconomy.setArgent(p, argentJoueur);
 					}
 					else
 					{
 						p.sendMessage(ChatColor.GOLD + "La banque ne dispose pas d'assez d'argent pour acheter vos biens");
 					}
 				}
 				else if (p.getItemInHand().getType() == Material.DIAMOND)
 				{
 					int prixVente = this.getPrixVente(p.getItemInHand());
 					if (this.soustraireArgentBanque(prixVente) == true)
 					{
 						this.ajouterStock(1, p.getItemInHand());
 						int reste = p.getItemInHand().getAmount() - 1;
 						p.setItemInHand(new ItemStack(p.getItemInHand().getType(), reste));
 						int argentJoueurActuel = VVEconomy.getArgent(p);
 						int argentJoueur = argentJoueurActuel + prixVente;
 						VVEconomy.setArgent(p, argentJoueur);
 					}
 					else
 					{
 						p.sendMessage(ChatColor.GOLD + "La banque ne dispose pas d'assez d'argent pour acheter vos biens");
 					}
 					
 				}
 				else
 				{
 					p.sendMessage(ChatColor.GOLD + "Vous ne pouvez pas vendre cet objet � la banque");
 				}
 				
 			}
 			else
 			{
 				p.sendMessage(ChatColor.GOLD + "Les arguments disponibles pour la banque sont :\n - tp : pour vous rendre � la banque (achat)\n - vendre : pour vendre ce que vous avez dans la main\n - quitter : quitter la banque");
 			}
 			
 			return true;
 		
 		}
 		
 		//ATTENTION UNIQUEMENT FAIT PAR DES GENS QUI CONNAISSENT LA COMMANDE : IL FAUT AVOIR UN ITEM COMPATIBLE EN MAIN ET SURTOUT NE PAS ENTRER AUTRE CHOSE QU'UN INT EN ARGUMENT
 		else if (label.equalsIgnoreCase("mettrecurrency") && p.hasPermission("vveconomy.admin"))
 		{
 			if (args.length == 1)
 			{
 				this.setCurrency(p.getItemInHand(), Integer.parseInt(args[0]));
 			}
 			return true;
 		}
 		else if (label.equalsIgnoreCase("setbanque") && p.hasPermission("vveconomy.admin"))
 		{
 			this.setLocationBanque(p.getLocation());
 		}
 		return false;
 	}
 }
