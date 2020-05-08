 package com.censoredsoftware.demigods.greek;
 
 import com.censoredsoftware.censoredlib.trigger.Trigger;
 import com.censoredsoftware.demigods.engine.deity.Alliance;
 import com.censoredsoftware.demigods.engine.deity.Deity;
 import com.censoredsoftware.demigods.engine.mythos.Mythos;
 import com.censoredsoftware.demigods.engine.mythos.MythosPlugin;
 import com.censoredsoftware.demigods.engine.structure.Structure;
 import com.censoredsoftware.demigods.greek.deity.GreekAlliance;
 import com.censoredsoftware.demigods.greek.deity.GreekDeity;
 import com.censoredsoftware.demigods.greek.structure.GreekStructure;
 import com.censoredsoftware.demigods.greek.trigger.DivinityUnbalanced;
 import com.censoredsoftware.demigods.greek.trigger.NewPlayerNeedsHelp;
 import com.censoredsoftware.demigods.greek.trigger.ProcessAltars;
 import com.google.common.base.Function;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Sets;
 import org.bukkit.event.Listener;
 import org.bukkit.permissions.Permission;
 import org.bukkit.plugin.ServicePriority;
 
 import java.util.Collection;
 
 public class GreekMythos extends MythosPlugin
 {
 	private static boolean PRIMARY = true;
 
 	/**
 	 * The Bukkit enable method.
 	 */
 	@Override
 	public void onEnable()
 	{
		getServer().getServicesManager().register(Mythos.class, this, this, ServicePriority.Lowest);
 	}
 
 	/**
 	 * The Bukkit disable method.
 	 */
 	@Override
 	public void onDisable()
	{

	}
 
 	@Override
 	public String getTitle()
 	{
 		return "Greek";
 	}
 
 	@Override
 	public String getTagline()
 	{
 		return "Greek mythology, as described by Hesiod, Homer, and other Greek bards.";
 	}
 
 	@Override
 	public String getAuthor()
 	{
 		return "_Alex and HmmmQuestionMark";
 	}
 
 	@Override
 	public boolean isPrimary()
 	{
 		return PRIMARY;
 	}
 
 	@Override
 	public boolean allowSecondary()
 	{
 		return true;
 	}
 
 	@Override
 	public String[] getIncompatible()
 	{
 		return new String[] {};
 	}
 
 	@Override
 	public boolean useBaseGame()
 	{
 		return true;
 	}
 
 	@Override
 	public Collection<Alliance> getAlliances()
 	{
 		return Sets.newHashSet((Alliance[]) GreekAlliance.values());
 	}
 
 	@Override
 	public Collection<Deity> getDeities()
 	{
 		return Sets.newHashSet((Deity[]) GreekDeity.values());
 	}
 
 	@Override
 	public Collection<Structure> getStructures()
 	{
 		return Sets.newHashSet((Structure[]) GreekStructure.values());
 	}
 
 	// Default Greek Skill Leveling
 	public boolean levelSeperateSkills()
 	{
 		return true;
 	}
 
 	// Default Greek Listeners
 	public Collection<Listener> getListeners()
 	{
 		return Sets.newHashSet();
 	}
 
 	// Default Greek Permissions
 	public Collection<Permission> getPermissions()
 	{
 		return Sets.newHashSet();
 	}
 
 	// Default Greek Triggers
 	public Collection<Trigger> getTriggers()
 	{
 		if(!isPrimary()) return Sets.newHashSet();
 		return Collections2.transform(Sets.newHashSet(GreekMythos.GreekTrigger.values()), new Function<GreekMythos.GreekTrigger, Trigger>()
 		{
 			@Override
 			public Trigger apply(GreekMythos.GreekTrigger listedTrigger)
 			{
 				return listedTrigger.getTrigger();
 			}
 		});
 	}
 
 	@Override
 	public void setSecondary()
 	{
 		PRIMARY = false;
 	}
 
 	// Triggers
 	public enum GreekTrigger
 	{
 		/**
 		 * Balance related.
 		 */
 		DIVINITY_UNBALANCED(new DivinityUnbalanced()), NEW_PLAYER_NEEDS_HELP(new NewPlayerNeedsHelp()), PROCESS_ALTARS(new ProcessAltars());
 
 		private Trigger trigger;
 
 		private GreekTrigger(Trigger trigger)
 		{
 			this.trigger = trigger;
 		}
 
 		public Trigger getTrigger()
 		{
 			return trigger;
 		}
 	}
 }
