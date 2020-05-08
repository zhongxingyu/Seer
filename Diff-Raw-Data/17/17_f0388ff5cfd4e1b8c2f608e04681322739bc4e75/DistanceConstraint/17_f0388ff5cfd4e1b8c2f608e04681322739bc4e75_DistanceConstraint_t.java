 package au.com.mineauz.PlayerSpy.search;
 
 import org.bukkit.Location;
 
 import au.com.mineauz.PlayerSpy.Records.BlockChangeRecord;
 import au.com.mineauz.PlayerSpy.Records.ILocationAware;
import au.com.mineauz.PlayerSpy.Records.InventoryTransactionRecord;
 import au.com.mineauz.PlayerSpy.Records.Record;
 import au.com.mineauz.PlayerSpy.search.interfaces.Constraint;
 
 public class DistanceConstraint extends Constraint
 {
 	public double distance;
 	public Location location;
 
 	public DistanceConstraint()
 	{
 		
 	}
 	public DistanceConstraint(double distance, Location location)
 	{
 		this.distance = distance;
 		this.location = location.clone();
 	}
 	@Override
 	public boolean matches( Record record )
 	{
 		if(record instanceof ILocationAware)
 		{
 			Location other = ((ILocationAware)record).getLocation();
 			if(other == null)
 				return false;
 			
 			if(record instanceof BlockChangeRecord)
 				other = other.clone().add(0.5,0.5,0.5);
			if(record instanceof InventoryTransactionRecord && ((InventoryTransactionRecord)record).getInventoryInfo().getBlock() != null)
				other = other.clone().add(0.5,0.5,0.5);
 			
 			if(location.getWorld().equals(other.getWorld()))
 			{
 				if(location.distanceSquared(other) <= (distance * distance))
 					return true;
 			}
 		}
 		return false;
 	}
 }
