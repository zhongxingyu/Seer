 package com.censoredsoftware.demigods.engine.element;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 
 import com.censoredsoftware.demigods.engine.Demigods;
 import com.censoredsoftware.demigods.engine.player.DCharacter;
 import com.censoredsoftware.demigods.engine.player.DPlayer;
 
 public abstract class Deity
 {
 	private final Info info;
 	private final Set<Ability> abilities;
 
 	public Deity(Info info, Set<Ability> abilities)
 	{
 		this.info = info;
 		this.abilities = abilities;
 	}
 
 	public Info getInfo()
 	{
 		return info;
 	}
 
 	public Set<Ability> getAbilities()
 	{
 		return abilities;
 	}
 
 	public enum Type
 	{
 		DEMO, TIER1, TIER2, TIER3
 	}
 
 	@Override
 	public String toString()
 	{
 		return info.getName();
 	}
 
 	public static Set<String> getLoadedDeityAlliances()
 	{
 		return new HashSet<String>()
 		{
 			{
 				for(Deity deity : Demigods.getLoadedDeities().values())
 				{
 					if(!contains(deity.getInfo().getAlliance())) add(deity.getInfo().getAlliance());
 				}
 			}
 		};
 	}
 
 	public static class Info
 	{
 		private final String name;
 		private final String alliance;
 		private final String permission;
 		private final ChatColor color;
 		private final Set<Material> claimItems;
 		private final List<String> lore;
 		private final Deity.Type type;
 
 		public Info(String name, String alliance, String permission, ChatColor color, Set<Material> claimItems, List<String> lore, Deity.Type type)
 		{
 			this.name = name;
 			this.color = color;
 			this.alliance = alliance;
 			this.permission = permission;
 			this.claimItems = claimItems;
 			this.lore = lore;
 			this.type = type;
 		}
 
 		public String getName()
 		{
 			return name;
 		}
 
 		public String getAlliance()
 		{
 			return alliance;
 		}
 
 		public String getPermission()
 		{
 			return permission;
 		}
 
 		public ChatColor getColor()
 		{
 			return color;
 		}
 
 		public Set<Material> getClaimItems()
 		{
 			return claimItems;
 		}
 
 		public List<String> getLore()
 		{
 			return lore;
 		}
 
 		public Deity.Type getType()
 		{
 			return type;
 		}
 	}
 
 	public static class Util
 	{
 		public static Set<Deity> getAllDeitiesInAlliance(final String alliance)
 		{
 			return new HashSet<Deity>()
 			{
 				{
 					for(Deity deity : Demigods.getLoadedDeities().values())
 					{
 						if(deity.getInfo().getAlliance().equalsIgnoreCase(alliance)) add(deity);
 					}
 				}
 			};
 		}
 
 		public static Deity getDeity(String deity)
 		{
			return Demigods.getLoadedDeities().get(deity.toLowerCase());
 		}
 
 		public static boolean canUseDeity(Player player, String deity)
 		{
 			DCharacter character = DPlayer.Util.getPlayer(player).getCurrent();
 			if(character == null || !character.isImmortal())
 			{
 				player.sendMessage(ChatColor.RED + "You can't do that, mortal!");
 				return false;
 			}
 			else if(!character.isDeity(deity))
 			{
 				player.sendMessage(ChatColor.RED + "You haven't claimed " + deity + "! You can't do that!");
 				return false;
 			}
 			return true;
 		}
 
 		public static boolean canUseDeitySilent(Player player, String deity)
 		{
 			DCharacter character = DPlayer.Util.getPlayer(player).getCurrent();
 			return character != null && character.isImmortal() && character.isDeity(deity);
 		}
 	}
 }
