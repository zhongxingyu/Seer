 package no.runsafe.toybox.events;
 
 import no.runsafe.framework.api.event.entity.IEntityChangeBlockEvent;
 import no.runsafe.framework.minecraft.RunsafeLocation;
 import no.runsafe.framework.minecraft.event.entity.RunsafeEntityChangeBlockEvent;
 import no.runsafe.toybox.handlers.CarePackageHandler;
 import no.runsafe.toybox.handlers.MobDropHandler;
 import org.bukkit.Material;
 
 public class ChangeBlockEvent implements IEntityChangeBlockEvent
 {
 	public ChangeBlockEvent(CarePackageHandler handler, MobDropHandler mobDropHandler)
 	{
 		this.handler = handler;
 		this.mobDropHandler = mobDropHandler;
 	}
 
 	@Override
 	public void OnEntityChangeBlockEvent(RunsafeEntityChangeBlockEvent event)
 	{
 		Integer entityID = event.getEntity().getEntityId();
 
 		if (this.handler.CheckDrop(entityID))
 		{
 			RunsafeLocation blockLocation = event.getBlock().getLocation();
 			blockLocation.getBlock().setTypeId(Material.CHEST.getId());
 
 			this.handler.ProcessDrop(entityID, blockLocation.getBlock());
			event.setCancelled(true);
 		}
 
 		if (this.mobDropHandler.entityIsMobDropper(entityID))
 		{
 			this.mobDropHandler.processMobDropper(entityID, event.getBlock().getLocation());
			event.setCancelled(true);
 		}
 	}
 
 	private final CarePackageHandler handler;
 	private final MobDropHandler mobDropHandler;
 }
