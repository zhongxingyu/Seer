 /**
  * This file, Ability.java, is part of MineQuest:
  * A full featured and customizable quest/mission system.
  * Copyright (C) 2012 The MineQuest Team
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  **/
 package com.theminequest.MQCoreRPG.API.Abilities;
 
 import java.lang.reflect.Method;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerEvent;
 
 import com.theminequest.MQCoreRPG.MQCoreRPG;
 import com.theminequest.MQCoreRPG.BukkitEvents.AbilityRefreshedEvent;
 import com.theminequest.MQCoreRPG.Class.ClassDetails;
 import com.theminequest.MQCoreRPG.Player.PlayerDetails;
 import com.theminequest.MineQuest.MineQuest;
 import com.theminequest.MineQuest.Group.Group;
 import com.theminequest.MineQuest.Quest.QuestManager;
 
 public abstract class Ability {
 	
 	/**
 	 * Give this ability a name, please?
 	 * @return ability name
 	 */
 	public abstract String getName();
 
 	/**
 	 * How much power (or endurance) this ability uses.<br>
	 * Remember that the power of a person is (base power)*level.<br>
	 * If you want to adjust how much power is used (e.g, always deplete
	 * entire power), you can retrieve the player's level by accessing
	 * the details that are passed in.
	 * @return total power to remove from Player.
 	 */
	public abstract int getPower(PlayerDetails d);
 	
 	/**
 	 * Cooldown time after using this ability, in seconds.
 	 * @return cooldown time in seconds.
 	 */
 	public abstract long getCooldown();
 	
 	/**
 	 * Abilities are listeners for all events. When an event
 	 * is called, abilities need to do something with it.<br>
 	 * If this is the event the ability is looking for, i.e.
 	 * the player is right, the event is the BlockEvent that
 	 * you are looking for, and everything is in order to
 	 * perform the event, return true.<br>
 	 * <b>Hint</b>: For a specific event, use <i>instanceof</i>
 	 * as an if statement to check if the event is the one you
 	 * want. The four events we support are
 	 * {@link org.bukkit.event.player.PlayerEggThrowEvent},
 	 * {@link org.bukkit.event.player.PlayerFishEvent},
 	 * {@link org.bukkit.event.player.PlayerInteractEntityEvent},
 	 * and {@link org.bukkit.event.player.PlayerInteractEvent}.
 	 * @param e Event caught.
 	 * @return true if this is the event we want to use.
 	 */
 	public abstract boolean isRightEvent(PlayerEvent e);
 	
 	/**
 	 * Execute the event given the parameters. This time
 	 * you can do casting without checking, as the event
 	 * should have been checked earlier.<br>
 	 * @param p The player. Useful if you need details such as level.
 	 * @param e The event passed in earlier
 	 */
 	public abstract void executeEvent(Player p, PlayerEvent e);
 	
 	/**
 	 * Quests can disallow certain abilities.
 	 * If the quest refuses to allow this event to happen,
 	 * this will return false.
 	 * @param p Player Name
 	 */
 	public boolean questAllow(Player p){
 		long teamid = MineQuest.groupManager.indexOf(p);
 		if (teamid==-1)
 			return true;
 		Group g = MineQuest.groupManager.getGroup(teamid);
 		// outside the quest, of course you can use abilities
 		if (!g.isInQuest())
 			return true;
 		// inside the quest...
 		List<String> abilities = g.getQuest().getDisallowedAbilities();
 		for (String s : abilities){
 			if (s.equalsIgnoreCase(getName()))
 				return false;
 		}
 		return true;
 	}
 	
 	private boolean checkCoolDown(Player pl, PlayerDetails p){
 		if (p.abilitiesCoolDown.containsKey(getName())){
 			long currentseconds = System.currentTimeMillis()*1000;
 			long timeelapsed = currentseconds-p.abilitiesCoolDown.get(this);
 			if (timeelapsed<getCooldown()){
 				pl.sendMessage(ChatColor.YELLOW+"Ability " + getName() + " is recharging... "
 						+ ChatColor.GRAY + "(" +(getCooldown()-timeelapsed)+ " s)");
 				return false;
 			}
 			p.abilitiesCoolDown.remove(getName());
 			return true;
 		}
 		return true;
 	}
 	
 	private boolean checkEnabled(PlayerDetails p){
 		return p.getAbilitiesEnabled();
 	}
 	
 	private boolean checkClassAbilities(PlayerDetails p){
 		ClassDetails d = MQCoreRPG.classManager.getClassDetail(p.getClassID());
 		if (d==null)
 			return false;
 		List<String> list = d.getAbilities();
 		return (list.contains(getName()));
 	}
 	
 	protected boolean onEventCaught(PlayerEvent e){
 		if (!isRightEvent(e))
 			return false;
 		final Player player = e.getPlayer();
 		PlayerDetails pd = MQCoreRPG.playerManager.getPlayerDetails(player);
 		if (!checkEnabled(pd))
 			return false;
 		if (!checkCoolDown(player,pd))
 			return false;
 		if (!checkClassAbilities(pd))
 			return false;
 		if (!questAllow(player))
 			return false;
 		
		pd.modifyPowerBy(-1*getPower(pd));
 		executeEvent(player, e);
 		pd.abilitiesCoolDown.put(this, System.currentTimeMillis()*1000);
 		player.sendMessage(ChatColor.GRAY + "Used ability " + getName() + ".");
 		final Ability a = this;
 		Bukkit.getScheduler().scheduleAsyncDelayedTask(MineQuest.activePlugin,
 				new Runnable(){
 
 					@Override
 					public void run() {
 						Bukkit.getPluginManager().callEvent(new AbilityRefreshedEvent(a,player));
 					}
 			
 		}, 20*getCooldown());
 		return true;
 
 	}
 	
 }
