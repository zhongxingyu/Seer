 package net.immortalcraft.races;
 
 import org.bukkit.Bukkit;
 
 import com.massivecraft.mcore.Aspect;
 import com.massivecraft.mcore.AspectColl;
 import com.massivecraft.mcore.MPlugin;
 
 import net.immortalcraft.races.cmd.CmdBase;
 import net.immortalcraft.races.listener.TheListener;
 
 public class Races extends MPlugin 
 {
   // -------------------------------------------- //
 	// INSTANCE & CONSTRUCT
 	// -------------------------------------------- //
 
 	public static Races r;
 	public static Races get() { return r; }
 	public Races() { Races.r = this; }
 
 	// -------------------------------------------- //
 	// FIELDS
 	// -------------------------------------------- //
 
 	// Commands
 	public CmdBase cmdBase;
 
 	// Aspects
 	public Aspect playerAspect;
 	public Aspect configAspect;
 
 	// -------------------------------------------- //
 	// OVERRIDE
 	// -------------------------------------------- //
 
 	@Override
 	public void onEnable()
 	{
 		if ( ! preEnable()) return;
 
 		// Init aspects
 		this.playerAspect = AspectColl.get().get(Const.playerAspectId, true);
 		this.playerAspect.register();
 		this.playerAspect.setDesc(
 			"<i>Everything player related:", 
 			"<i>Is the player a race or not?",
 			"<i>What was the race?",
 			"<i>Check <h>"+Const.configAspectId+" <i>for rules and balancing."
 		);
 
 		this.configAspect = AspectColl.get().get(Const.configAspectId, true);
 		this.configAspect.register();
 		this.configAspect.setDesc(
 			"<i>Config options for balancing:", 
 			"<i>What is the abilities for a certain race?",
 			"<i>I have no idea?",
 			"<i>Check <h>"+Const.playerAspectId+" <i>for player state."
 		);
 
 		// Load Conf from disk
		//Lang.i.load();
 		ConfServer.i.load();
 		ConfColls.i.init();
 		RPlayerColls.i.init();
 
 		// Initialize collections
 		// RPlayerColl.i.init();
 
 		// Add Base Commands
 		this.cmdBase = new CmdBase();
 		this.cmdBase.register(this, true);
 
 		// Start timer
 		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new TheTask(), 0, ConfServer.taskInterval);
 
 		// Register events
		//new TheListener(this);
 
 		postEnable();
 	}
 
 }
