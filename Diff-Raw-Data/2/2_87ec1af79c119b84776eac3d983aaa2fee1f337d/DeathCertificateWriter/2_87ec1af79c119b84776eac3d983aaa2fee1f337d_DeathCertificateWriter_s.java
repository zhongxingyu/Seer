 package me.goosemonkey.deathcertificate;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import net.minecraft.server.NBTTagCompound;
 import net.minecraft.server.NBTTagList;
 import net.minecraft.server.NBTTagString;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.craftbukkit.inventory.CraftItemStack;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Fireball;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.ThrownPotion;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.inventory.ItemStack;
 
 public class DeathCertificateWriter
 {
 	DeathCertificate plugin;
 	
 	public DeathCertificateWriter(DeathCertificate inst)
 	{
 		plugin = inst;
 	}
 	
 	ItemStack getWrittenCertificate(PlayerDeathEvent event)
 	{
 		CraftItemStack book = new CraftItemStack(Material.WRITTEN_BOOK, 1);
 		
 		book.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 10);
 		
 		book.getHandle().getTag().setString("author", this.getKiller(event));
 		
 		book.getHandle().getTag().setString("title", ChatColor.RED + event.getEntity().getName() + "'s Death Certificate");
 		
 		
 		List<String> pages = getBookPages(event);
 		
 		NBTTagList pages_tag = new NBTTagList("pages");
 		
 		for (int i = 0; i < pages.size(); i++)
 		{
 			pages_tag.add(new NBTTagString((i + 1) + "", pages.get(i)));
 		}
 		
 		NBTTagCompound tag = book.getHandle().getTag();
 		
 		if(tag == null)
 		{
 			tag = new NBTTagCompound();
 		}
 		
 		tag.set("pages", pages_tag);
 		book.getHandle().setTag(tag);
 		
 		
 		book.removeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL);
 		
 		return book;
 	}
 	
 	private String getKiller(PlayerDeathEvent event)
 	{
 		if (event.getEntity().getKiller() != null)
 			return event.getEntity().getKiller().getDisplayName();
 		
 		if (event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent)
 		{
 			EntityDamageByEntityEvent event2 = (EntityDamageByEntityEvent) event.getEntity().getLastDamageCause();
 			
 			return this.getFriendlyMobName(event, event2);
 		}
 		
 		switch (event.getEntity().getLastDamageCause().getCause())
 		{
 		case BLOCK_EXPLOSION: return "an Explosion";
 		case CONTACT: return "a Cactus";
 		case CUSTOM: return "Herobrine";
 		case DROWNING: return "Drowning";
 		case ENTITY_EXPLOSION: return "a Creeper";
 		case FALL: return "Falling";
 		case FIRE: return "Fire";
 		case FIRE_TICK: return "Fire";
 		case LAVA: return "Lava";
 		case LIGHTNING: return "Lightning";
 		case MAGIC: return "Magic";
 		case MELTING: return "Melting?";
 		case POISON: return "Poison";
 		case STARVATION: return "Starvation";
 		case SUFFOCATION: return "Suffocation";
 		case SUICIDE: return "Suicide";
 		case VOID: return "the Void";
 		default: break;
 		}
 		
 		return "Dying";
 	}
 	
 	private String getFriendlyMobName(PlayerDeathEvent event, EntityDamageByEntityEvent event2)
 	{
 		switch (event2.getDamager().getType())
 		{
 		case ARROW: 
 			if (((Arrow) event2.getDamager()).getShooter() == null)
 				return "an Arrow";
 			else if (((Arrow) event2.getDamager()).getShooter().getType() == EntityType.SKELETON)
 				return "a Skeleton";
 			else
 				return ((Player)((Arrow) event2.getDamager()).getShooter()).getName();
 		case BLAZE: return "a Blaze";
 		case CAVE_SPIDER: return "a Cave Spider";
 		case CREEPER: return "a Creeper";
 		case ENDER_DRAGON: return "the Enderdragon";
 		case ENDERMAN: return "an Enderman";
 		case FIREBALL:
 			if (((Fireball) event2.getDamager()).getShooter() == null)
 				return "a Fireball";
 			else if (((Fireball) event2.getDamager()).getShooter().getType() == EntityType.GHAST)
 				return "a Ghast";
 			else if (((Fireball) event2.getDamager()).getShooter().getType() == EntityType.BLAZE)
 				return "a Blaze";
 			else
 				return "a Fireball";
 		case FISHING_HOOK: return "a Fishing Pole";
 		case GHAST: return "a Ghast";
 		case GIANT: return "a Giant";
 		case IRON_GOLEM: return "an Iron Golem";
 		case LIGHTNING: return "Lightning";
 		case MAGMA_CUBE: return "a Magma Cube";
 		case PIG_ZOMBIE: return "a Zombie Pigman";
 		case PLAYER: return "a Player";
 		case PRIMED_TNT: return "TNT";
 		case SILVERFISH: return "a Silverfish";
 		case SKELETON: return "a Skeleton";
 		case SLIME: return "a Slime";
 		case SMALL_FIREBALL: return "a Fireball";
 		case SNOWBALL: return "a Snowball";
 		case SNOWMAN: return "a Snow Golem";
 		case SPIDER: return "a Spider";
 		case SPLASH_POTION: 
 			if (((ThrownPotion) event2.getDamager()).getShooter().getType() == EntityType.PLAYER)
 				return ((Player) ((ThrownPotion) event2.getDamager()).getShooter()).getName();
 			else
 				return "a Splash Potion";
 		case WOLF: return "a Wolf";
 		case ZOMBIE: return "a Zombie";
 		default: return "Herobrine";
 		}
 	}
 	
 	private List<String> getBookPages(PlayerDeathEvent event)
 	{
 		List<String> i = new ArrayList<String>();
 		
 		SimpleDateFormat sdf = new SimpleDateFormat(this.plugin.getConfig().getBoolean("Options.AmericanDateFormat", false) ? "MM/dd/yy" : "dd/MM/yy");
 		String dateString = sdf.format(new Date());
 	
 		SimpleDateFormat stf = new SimpleDateFormat("hh:mm aaa z");
 		String timeString = stf.format(new Date());
 		
 		i.add(ChatColor.ITALIC + "" + ChatColor.UNDERLINE + ChatColor.BLUE + "Death Certificate:" + "\n"
 				+ ChatColor.DARK_BLUE + ChatColor.BOLD + event.getEntity().getName() + "\n" + ChatColor.RESET + "\n"
 				+ ChatColor.BLACK + ChatColor.ITALIC + "Killed by\n" + ChatColor.RESET + ChatColor.RED + getKiller(event) + "\n" + ChatColor.RESET + "\n"
 				+ ChatColor.BLACK + ChatColor.ITALIC + "On " + ChatColor.RESET + ChatColor.BLUE + dateString + "\n"
 				+ ChatColor.BLACK + ChatColor.ITALIC + "at " + ChatColor.RESET + ChatColor.DARK_BLUE + timeString);
 		
 		i.add("Level " + ChatColor.RESET + ChatColor.DARK_GREEN + event.getEntity().getLevel() + "\n"
				+ ChatColor.BLACK + "XP: " + ChatColor.DARK_GREEN + event.getDroppedExp());
 		
 		/*
 		 * Code to show items dropped on death. May be in a future version.
 		 * 
 		
 		String drops;
 		
 		if (event.getDrops().size() != 0)
 		{
 			int done = 0;
 			
 			StringBuilder n = new StringBuilder();
 			
 			n.append("Drops: \n" + ChatColor.RESET + "\n");
 			
 			while (done < event.getDrops().size())
 			{
 				ItemStack item = event.getDrops().get(done);
 				
 				n.append(item.getAmount() + " " + item.getType().name() + "\n");
 				
 				done++;
 				
 				if (done % 10 == 0)
 				{
 					n.append(" $next$ ");
 				}
 			}
 			
 			drops = n.toString();
 			
 			if (drops.contains("$next$"))
 			{
 				String[] dropsSep = drops.split("\\$next\\$", 10);
 				
 				for (String c : dropsSep)
 				{
 					i.add(c);
 				}
 			}
 			else
 			{
 				i.add(drops);
 			}
 		}
 		
 		*/
 		
 		return i;
 	}
 }
