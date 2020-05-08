 package no.runsafe.framework.event.block;
 
 import no.runsafe.framework.event.IRunsafeEvent;
import no.runsafe.framework.server.event.block.RunsafeBlockRedstoneEvent;
 
 public interface IBlockRedstone extends IRunsafeEvent
 {
	public void OnBlockRedstoneEvent(RunsafeBlockRedstoneEvent event);
 }
