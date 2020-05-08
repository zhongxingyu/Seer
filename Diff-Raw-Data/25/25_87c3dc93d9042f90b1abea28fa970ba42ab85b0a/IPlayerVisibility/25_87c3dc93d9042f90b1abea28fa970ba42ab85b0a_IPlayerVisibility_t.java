 package no.runsafe.framework.api.hook;
 
 import no.runsafe.framework.minecraft.player.RunsafePlayer;
 
 public interface IPlayerVisibility extends IFrameworkHook
 {
 	/**
 	 * Called by {@link no.runsafe.framework.minecraft.player.RunsafePlayer#shouldNotSee(no.runsafe.framework.minecraft.player.RunsafePlayer)} to test if a player should be aware of someone else or not
 	 */
	@Deprecated
 	boolean canPlayerASeeB(RunsafePlayer a, RunsafePlayer b);
 
 	/**
 	 * Called by {@link no.runsafe.framework.minecraft.player.RunsafePlayer#isVanished()} to test if a player has vanished or not
 	 */
 	boolean isPlayerVanished(RunsafePlayer player);
 }
