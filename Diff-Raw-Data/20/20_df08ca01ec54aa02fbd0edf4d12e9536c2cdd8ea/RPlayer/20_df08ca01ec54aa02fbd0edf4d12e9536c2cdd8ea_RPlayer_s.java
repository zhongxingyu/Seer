 package net.immortalcraft.races;
 
 //Java
 import com.massivecraft.mcore.store.SenderEntity;
 
 
 	public class RPlayer extends SenderEntity<RPlayer>
 	{
 		// -------------------------------------------- //
 		// META
 		// -------------------------------------------- //
 
 		public static RPlayer get(Object worldNameExtractable)
 		{
 			return RPlayerColls.i.get2(worldNameExtractable);
 		}
 
 		// -------------------------------------------- //
 		// LOAD
 		// -------------------------------------------- //
 
 		@Override
 		public RPlayer load(RPlayer that)
 		{
			//TODO load races and stuffs
 		}
 		
 		public void tick(long ticks)
 		{
 			//TODO add certain ticks
 		}
 	}
