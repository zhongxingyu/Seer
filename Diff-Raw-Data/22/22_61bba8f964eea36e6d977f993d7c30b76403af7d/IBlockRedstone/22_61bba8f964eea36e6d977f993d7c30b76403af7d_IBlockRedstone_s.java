 package no.runsafe.framework.event.block;
 
 import no.runsafe.framework.event.IRunsafeEvent;
 
 public interface IBlockRedstone extends IRunsafeEvent
 {
	/**
	 * Respond to red stone power states changing for a block
	 *
	 * @param oldCurrent the previous current
	 * @param newCurrent the new current
	 * @return if < 0, does nothing, if >= 0, overrides the new current
	 */
	public int OnBlockRedstone(int oldCurrent, int newCurrent);
 }
