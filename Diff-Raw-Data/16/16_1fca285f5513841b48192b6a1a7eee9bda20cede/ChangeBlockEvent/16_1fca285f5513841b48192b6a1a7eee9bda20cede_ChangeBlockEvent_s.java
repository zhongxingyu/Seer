 package no.runsafe.toybox.events;
 
 import no.runsafe.framework.event.entity.IEntityChangeBlockEvent;
 import no.runsafe.framework.output.IOutput;
 import no.runsafe.framework.server.event.entity.RunsafeEntityChangeBlockEvent;
 import no.runsafe.toybox.handlers.CarePackageHandler;
 
 public class ChangeBlockEvent implements IEntityChangeBlockEvent
 {
	public ChangeBlockEvent(CarePackageHandler handler, IOutput output)
 	{
 		this.handler = handler;
		this.output = output;
 	}
 
 	@Override
 	public void OnEntityChangeBlockEvent(RunsafeEntityChangeBlockEvent event)
 	{
		this.output.write("Entity change event detected: " + event.getBlock().getTypeId());
		this.output.write("Block type: " + event.getBlock().getClass().getName());
 		Integer entityID = event.getEntity().getEntityId();
 
 		if (this.handler.CheckDrop(entityID))
			this.handler.ProcessDrop(entityID, event.getBlock());
 	}
 
 	private CarePackageHandler handler;
	private IOutput output;
 }
