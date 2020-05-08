 package sidben.redstonejukebox.helper;
 
 
 import java.util.EnumSet;
 
 import sidben.redstonejukebox.ModRedstoneJukebox;
 import sidben.redstonejukebox.net.PacketHelper;
 import cpw.mods.fml.common.FMLCommonHandler;
 import cpw.mods.fml.common.IScheduledTickHandler;
 import cpw.mods.fml.common.TickType;
 import cpw.mods.fml.relauncher.Side;
 
 
 
 /*
  * This class will run on the server to check if clients are
  * playing records.
  */
 public class MusicTickHandler implements IScheduledTickHandler {
 
 
    private static int     c                   = 0;             // Counter
     private static boolean musicFirstFullCheck = false;         // Flag to indicate the first check, before got any answer. Updated by the TickHandler.
 
 
 
     /**
      * Called at the "start" phase of a tick
      * 
      * Multiple ticks may fire simultaneously- you will only be called once with all the firing ticks
      * 
      * @param type
      * @param tickData
      */
     @Override
     public void tickStart(EnumSet<TickType> type, Object... tickData) {}
 
 
     /**
      * Called at the "end" phase of a tick
      * 
      * Multiple ticks may fire simultaneously- you will only be called once with all the firing ticks
      * 
      * @param type
      * @param tickData
      */
     @Override
     public void tickEnd(EnumSet<TickType> type, Object... tickData) {
 
         if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
             // Check if should run
             if (PlayMusicHelper.musicCheckActive) {
                 ++MusicTickHandler.c;
 
 
                 if (MusicTickHandler.c < PlayMusicHelper.musicProcessFrequency) {
                     // Process responses, but only after the first full check
                     if (!MusicTickHandler.musicFirstFullCheck) {
                         PlayMusicHelper.ProcessResponseList(false);
                     }
                 }
                 else {
                     MusicTickHandler.c = 0;
 
                     // Process responses. If didn't get a response by now, mark as no clients playing.
                     if (!MusicTickHandler.musicFirstFullCheck) {
                         PlayMusicHelper.ProcessResponseList(true);
                     }
 
                     // Reset the Players response list
                     PlayMusicHelper.ResetResponseList();
 
                     // Sends question to all players if they are playing something.
                     // (only if still active)
                     if (PlayMusicHelper.musicCheckActive) {
                         ModRedstoneJukebox.logDebugInfo("Sending playing music question to all players.");
                         PacketHelper.sendIsPlayingQuestionPacket();
                     }
 
                     // Updates the first check flag.
                     MusicTickHandler.musicFirstFullCheck = false;
 
                 }
 
 
             }
         }
 
     }
 
 
     /**
      * Returns the list of ticks this tick handler is interested in receiving at the minute
      */
     @Override
     public EnumSet<TickType> ticks() {
         return EnumSet.of(TickType.SERVER);
     }
 
 
     /**
      * A profiling label for this tick handler
      */
     @Override
     public String getLabel() {
         return "MusicPlayingTickHandler";
     }
 
 
     /**
      * Return the number of actual ticks that will pass
      * before your next tick will fire. This will be called
      * just after your last tick fired to compute the next delay.
      * 
      * @param tick
      * @return
      */
     @Override
     public int nextTickSpacing() {
         return PlayMusicHelper.musicCheckTickSize;
     }
 
 
     public static void refreshTichHanlder() {
         // Makes sure the tick handler always start fresh
         MusicTickHandler.c = 0;
         MusicTickHandler.musicFirstFullCheck = true;
     }
 
 
 
 }
