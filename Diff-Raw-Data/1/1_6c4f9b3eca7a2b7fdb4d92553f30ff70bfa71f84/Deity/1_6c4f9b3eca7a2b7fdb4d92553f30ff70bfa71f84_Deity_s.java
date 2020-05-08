 package com.censoredsoftware.Demigods.Engine.Object.Deity;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 import com.censoredsoftware.Demigods.Engine.Demigods;
 import com.censoredsoftware.Demigods.Engine.Object.Ability.Ability;
 import com.censoredsoftware.Demigods.Engine.Object.Player.PlayerCharacter;
 import com.censoredsoftware.Demigods.Engine.Object.Player.PlayerWrapper;
 
 public abstract class Deity
 {
 	private DeityInfo info;
 	private Set<Ability> abilities;
 
 	public Deity(DeityInfo info, Set<Ability> abilities)
 	{
 		this.info = info;
 		this.abilities = abilities;
 	}
 
 	public DeityInfo getInfo()
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
 				for(Deity deity : Demigods.getLoadedDeities())
 				{
 					if(!contains(deity.getInfo().getAlliance())) add(deity.getInfo().getAlliance());
 				}
 			}
 		};
 	}
 
 	public static Set<Deity> getAllDeitiesInAlliance(final String alliance)
 	{
 		return new HashSet<Deity>()
 		{
 			{
 				for(Deity deity : Demigods.getLoadedDeities())
 				{
 					if(deity.getInfo().getAlliance().equalsIgnoreCase(alliance)) add(deity);
 				}
 			}
 		};
 	}
 
 	public static Deity getDeity(String deity)
 	{
 		for(Deity loaded : Demigods.getLoadedDeities())
 		{
 			if(loaded.getInfo().getName().equalsIgnoreCase(deity)) return loaded;
 		}
 		return null;
 	}
 
 	public static boolean canUseDeity(Player player, String deity)
 	{
 		PlayerCharacter character = PlayerWrapper.getPlayer(player).getCurrent();
 		if(!character.isDeity(deity))
 		{
 			player.sendMessage(ChatColor.RED + "You haven't claimed " + deity + "! You can't do that!");
 			return false;
 		}
 		else if(!character.isImmortal())
 		{
 			player.sendMessage(ChatColor.RED + "You can't do that, mortal!");
 			return false;
 		}
 		return true;
 	}
 
 	public static boolean canUseDeitySilent(Player player, String deity)
 	{
 		PlayerCharacter character = PlayerWrapper.getPlayer(player).getCurrent();
 		return character != null && character.isDeity(deity) && character.isImmortal();
 	}
 }
