 package no.runsafe.toybox.events;
 
 import no.runsafe.framework.event.entity.IEntityChangeBlockEvent;
 import no.runsafe.framework.output.IOutput;
import no.runsafe.framework.server.block.RunsafeBlock;
 import no.runsafe.framework.server.event.entity.RunsafeEntityChangeBlockEvent;
 import no.runsafe.toybox.handlers.CarePackageHandler;
 
 public class ChangeBlockEvent implements IEntityChangeBlockEvent
 {
	public ChangeBlockEvent(CarePackageHandler handler)
 	{
 		this.handler = handler;
 	}
 
 	@Override
 	public void OnEntityChangeBlockEvent(RunsafeEntityChangeBlockEvent event)
 	{
		RunsafeBlock changedBlock = event.getBlock();
		RunsafeBlock chest = changedBlock.getWorld().getBlockAt(changedBlock.getLocation());
 		Integer entityID = event.getEntity().getEntityId();
 
 		if (this.handler.CheckDrop(entityID))
			this.handler.ProcessDrop(entityID, chest);
 	}
 
 	private CarePackageHandler handler;
 }
