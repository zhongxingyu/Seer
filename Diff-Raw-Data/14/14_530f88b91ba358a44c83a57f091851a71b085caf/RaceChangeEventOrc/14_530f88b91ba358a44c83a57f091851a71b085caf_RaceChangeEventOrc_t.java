 package net.immortalcraft.races.events;
 
 import org.bukkit.event.HandlerList;
 
 import net.immortalcraft.races.RPlayer;
 
 	public class RaceChangeEventOrc extends CancellableRaceEvent
 	{
 		// -------------------------------------------- //
 		// REQUIRED EVENT CODE
 		// -------------------------------------------- //
 
 		private static final HandlerList handlers = new HandlerList();
 		public HandlerList getHandlers() { return handlers; }
 		public static HandlerList getHandlerList() { return handlers; }
 
 		// -------------------------------------------- //
 		// FIELD
 		// -------------------------------------------- //
 
 		protected final boolean orc;
		public boolean isOrc() { return this.orc; }
 
 		protected final RPlayer rplayer;
 		public RPlayer getrplayer() { return this.rplayer; }
 
 		// -------------------------------------------- //
 		// CONSTRUCT
 		// -------------------------------------------- //
 
 		public RaceChangeEventOrc(boolean orc, RPlayer rplayer)
 		{
 			this.orc = orc;
 			this.rplayer = rplayer;
 		}
 }
