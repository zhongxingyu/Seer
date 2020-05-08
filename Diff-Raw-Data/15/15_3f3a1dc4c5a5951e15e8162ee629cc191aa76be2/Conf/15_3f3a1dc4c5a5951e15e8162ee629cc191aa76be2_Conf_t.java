 package net.immortalcraft.races;
 
 import com.massivecraft.mcore.store.Entity;
 
 public class Conf extends Entity<Conf>
 {
 
 	// -------------------------------------------- //
 	// META
 	// -------------------------------------------- //
 
 	public static Conf get(Object worldNameExtractable)
 	{
 		return ConfColls.i.get2(worldNameExtractable);
 	}
 }
