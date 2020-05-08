 package com.theminequest.events;
 
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.theminequest.common.Common;
 import com.theminequest.common.quest.v1.V1EventManager;
 import com.theminequest.events.area.AreaEvent;
 import com.theminequest.events.area.SingleAreaEvent;
 import com.theminequest.events.block.AdvancedBlockEvent;
 import com.theminequest.events.block.BlockCDEvent;
 import com.theminequest.events.block.BlockDCEvent;
 import com.theminequest.events.block.BlockEvent;
 import com.theminequest.events.block.BlockInteractEvent;
 import com.theminequest.events.entity.EntitySpawnNumberEvent;
 import com.theminequest.events.entity.EntitySpawnerCompleteEvent;
 import com.theminequest.events.entity.EntitySpawnerEvent;
 import com.theminequest.events.entity.EntitySpawnerNoMove;
 import com.theminequest.events.entity.EntitySpawnerNoMoveComplete;
 import com.theminequest.events.entity.HealthEntitySpawn;
 import com.theminequest.events.env.ArrowEvent;
 import com.theminequest.events.env.ExplosionEvent;
 import com.theminequest.events.env.LightningEvent;
 import com.theminequest.events.env.WeatherEvent;
import com.theminequest.events.targeted.DamageEvent;
 import com.theminequest.events.targeted.ExplosionTargetEvent;
 import com.theminequest.events.targeted.HealthTargetEvent;
 import com.theminequest.events.targeted.LightningTargetEvent;
 import com.theminequest.events.targeted.PoisonEvent;
 import com.theminequest.events.targeted.TeleportEvent;
 
 public class MQCoreEvents extends JavaPlugin {
 
 	/* (non-Javadoc)
 	 * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
 	 */
 	@Override
 	public void onEnable() {
 		if (!getServer().getPluginManager().isPluginEnabled("MineQuest")) {
 			getServer().getLogger().severe("============= MineQuest-Events ===============");
 			getServer().getLogger().severe("MineQuest is required for MineQuest-Events!");
 			getServer().getLogger().severe("Please install MineQuest first!");
 			getServer().getLogger().severe("You can find the latest version here:");
 			getServer().getLogger().severe("http://dev.bukkit.org/server-mods/minequest/");
 			getServer().getLogger().severe("==============================================");
 			setEnabled(false);
 			return;
 		}
 		V1EventManager e = Common.getCommon().getV1EventManager();
 		//e.registerEvent("I", IdleEvent.class);
 		e.addEvent("AreaEvent", AreaEvent.class);
 		e.addEvent("SingleAreaEvent", SingleAreaEvent.class);
 		e.addEvent("AdvancedBlockEvent", AdvancedBlockEvent.class);
 		e.addEvent("BlockCDEvent", BlockCDEvent.class);
 		e.addEvent("BlockDCEvent", BlockDCEvent.class);
 		e.addEvent("BlockEvent", BlockEvent.class);
 		e.addEvent("BlockInteractEvent", BlockInteractEvent.class);
 		e.addEvent("EntitySpawnerCompleteEvent", EntitySpawnerCompleteEvent.class);
 		e.addEvent("EntitySpawnerEvent", EntitySpawnerEvent.class);
 		e.addEvent("EntitySpawnNumberEvent", EntitySpawnNumberEvent.class);
 		e.addEvent("EntitySpawnerNoMove", EntitySpawnerNoMove.class);
 		e.addEvent("EntitySpawnerCompleteNMEvent", EntitySpawnerNoMoveComplete.class);
 		e.addEvent("HealthEntitySpawn", HealthEntitySpawn.class);
 		e.addEvent("ArrowEvent", ArrowEvent.class);
 		e.addEvent("ExplosionEvent", ExplosionEvent.class);
 		e.addEvent("LightningEvent", LightningEvent.class);
 		e.addEvent("WeatherEvent", WeatherEvent.class);
 		
 		e.addEvent("DamageEvent", DamageEvent.class);
 		e.addEvent("ExplosionTargetEvent", ExplosionTargetEvent.class);
 		e.addEvent("HealthTargetEvent", HealthTargetEvent.class);
 		e.addEvent("LightningTargetEvent", LightningTargetEvent.class);
 		e.addEvent("PoisonEvent", PoisonEvent.class);
 		e.addEvent("TeleportEvent", TeleportEvent.class);
 	}
 
 }
