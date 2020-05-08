 package no.runsafe.framework.api.hook;
 
 import no.runsafe.framework.minecraft.player.RunsafePlayer;
 
 public interface IPlayerPvPFlag extends IFrameworkHook
 {
 	/**
 	 * Called by {@link no.runsafe.framework.minecraft.player.RunsafePlayer#isPvPFlagged()} for plugins controlling pvp actions
 	 */
 	boolean isFlaggedForPvP(RunsafePlayer player);
 }
