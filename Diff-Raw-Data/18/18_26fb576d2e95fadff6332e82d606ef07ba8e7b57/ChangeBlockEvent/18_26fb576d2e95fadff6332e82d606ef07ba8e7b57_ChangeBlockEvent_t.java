 package no.runsafe.toybox.events;
 
import no.runsafe.framework.event.entity.IEntityChangeBlockEvent;
import no.runsafe.framework.server.block.RunsafeBlock;
import no.runsafe.framework.server.entity.RunsafeLivingEntity;
import no.runsafe.framework.server.material.RunsafeMaterial;
 import no.runsafe.toybox.handlers.CarePackageHandler;
 
public class ChangeBlockEvent implements IEntityChangeBlockEvent
 {
 	public ChangeBlockEvent(CarePackageHandler handler)
 	{
 		this.handler = handler;
 	}
 
 	@Override
	public void OnEntityChangeBlockEvent(RunsafeLivingEntity entity, RunsafeBlock block, RunsafeMaterial material)
 	{
		Integer entityID = entity.getEntityId();
 		if (this.handler.CheckDrop(entityID))
			this.handler.ProcessDrop(entityID, block);
 	}
 
 	private CarePackageHandler handler;
 }
