 package no.runsafe.ItemControl;
 
 import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.IOutput;
 import no.runsafe.framework.api.event.entity.IMobSpawnerPulsed;
 import no.runsafe.framework.minecraft.entity.RunsafeLivingEntity;
 
 public class EntityListener implements IMobSpawnerPulsed
 {
	public EntityListener(Globals globals, IOutput console)
 	{
 		this.globals = globals;
 		this.console = console;
 	}
 
 	@Override
 	public boolean OnMobSpawnerPulsed(RunsafeLivingEntity entity, ILocation location)
 	{
 		if (!globals.spawnerTypeValid(entity.getEntityType(), null))
 		{
 			console.logInformation(
 				"SPAWNER WARNING: &cBlocked invalid spawner of &e%s&c at (%s,%d,%d,%d)",
 				entity.getRaw().getType().name(),
 				location.getWorld().getName(),
 				location.getBlockX(),
 				location.getBlockY(),
 				location.getBlockZ()
 			);
 			return false;
 		}
 		return true;
 	}
 
 	private Globals globals;
	private IOutput console;
 }
