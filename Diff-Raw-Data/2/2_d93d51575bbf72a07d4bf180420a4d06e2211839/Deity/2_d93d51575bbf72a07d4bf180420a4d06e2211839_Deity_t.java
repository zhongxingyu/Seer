 package com.censoredsoftware.demigods.deity;
 
 import com.censoredsoftware.demigods.Elements;
 import com.censoredsoftware.demigods.ability.Ability;
 import com.censoredsoftware.demigods.player.DCharacter;
 import com.censoredsoftware.demigods.player.DPlayer;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 public interface Deity
 {
 	public String getName();
 
 	public Elements.ListedDeity getListedDeity();
 
 	public String getAlliance();
 
 	public String getPermission();
 
 	public ChatColor getColor();
 
 	public Set<Material> getClaimItems();
 
 	public List<String> getLore();
 
 	public Deity.Type getType();
 
 	public Set<Ability> getAbilities();
 
 	public enum Type
 	{
 		TIER1, TIER2, TIER3
 	}
 
 	@Override
 	public String toString();
 
 	public static class Util
 	{
 		public static Set<String> getLoadedDeityAlliances()
 		{
 			return new HashSet<String>()
 			{
 				{
 					for(Elements.ListedDeity deity : Elements.Deities.values())
 					{
 						if(!contains(deity.getDeity().getAlliance())) add(deity.getDeity().getAlliance());
 					}
 				}
 			};
 		}
 
 		public static Set<Deity> getAllDeitiesInAlliance(final String alliance)
 		{
 			return new HashSet<Deity>()
 			{
 				{
 					for(Elements.ListedDeity deity : Elements.Deities.values())
 					{
 						if(deity.getDeity().getAlliance().equalsIgnoreCase(alliance)) add(deity.getDeity());
 					}
 				}
 			};
 		}
 
 		public static Deity getDeity(String deity)
 		{
 			return Elements.Deities.get(deity);
 		}
 
 		public static boolean canUseDeity(DCharacter character, String deity)
 		{
 			if(character == null) return false;
 			if(!character.getOfflinePlayer().isOnline()) return canUseDeitySilent(character, deity);
 			Player player = character.getOfflinePlayer().getPlayer();
 			if(!character.isDeity(deity))
 			{
 				player.sendMessage(ChatColor.RED + "You haven't claimed " + deity + "! You can't do that!");
 				return false;
 			}
 			return true;
 		}
 
 		public static boolean canUseDeitySilent(DCharacter character, String deity)
 		{
 			return character != null && character.isDeity(deity);
 		}
 
 		public static boolean canUseDeitySilent(Player player, String deityName)
 		{
 			Deity deity = DPlayer.Util.getPlayer(player).getCurrentDeity();
			return deity.getName().equalsIgnoreCase(deityName);
 		}
 	}
 }
