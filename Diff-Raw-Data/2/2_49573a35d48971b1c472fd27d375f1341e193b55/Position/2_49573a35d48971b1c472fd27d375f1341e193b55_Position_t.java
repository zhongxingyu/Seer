 package iggy.Regions;
 
 
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 
 public class Position {
 	public long _x;
 	public long _z;
 	public String _world;
 	
 	
 	
 	Position (String world,long x, long z){
 		_x=x;
 		_z=z;
 		_world = world;
 	}
 	Position (Location location) {
 		_x = floorDivide(location.getBlockX(),8);
 		_z = floorDivide(location.getBlockZ(),8);
 		_world = location.getWorld().getName();
 	}
 	
 	Position (){
 		_x=0;
 		_z=0;
 		_world=null;
 	}
 	
 	private long floorDivide(long input, int mod) {
		return input<0?((input+1)/8)-1:input/8;
 	}
 	
 	public void placeTorches() {
 		World plotWorld = Bukkit.getWorld(_world);
 		Location corner1 = new Location(plotWorld,_x*8,0,_z*8);
 		Location corner2 = new Location(plotWorld,_x*8,0,_z*8+7);
 		Location corner3 = new Location(plotWorld,_x*8+7,0,_z*8);
 		Location corner4 = new Location(plotWorld,_x*8+7,0,_z*8+7);
 		plotWorld.getHighestBlockAt(corner1).setType(Material.TORCH);
 		plotWorld.getHighestBlockAt(corner2).setType(Material.TORCH);
 		plotWorld.getHighestBlockAt(corner3).setType(Material.TORCH);
 		plotWorld.getHighestBlockAt(corner4).setType(Material.TORCH);
 	}
 	
 	
 	public boolean setFromString(String input) {
 		for (int i = 0; i< input.length(); i++){
 			if (input.charAt(i)==','){
 				_world = input.substring(0,i);
 				input = input.substring(i+1);
 				break;
 			}
 		}
 		for (int i = 0; i< input.length(); i++){
 			if (input.charAt(i)==','){
 				_x = Long.valueOf(input.substring(0,i));
 				input = input.substring(i+1);
 				break;
 			}
 		}
 		// check to make sure there is no extra comma
 		for (int i = 0; i< input.length(); i++){
 			if (input.charAt(i)==','){
 				return false;
 			}
 		}
 		_z = Long.valueOf(input);
 		return true;
 	}
 	@Override
 	public String toString() {
 		return (_world+","+_x+","+_z);
 	}
 	@Override
 	public boolean equals(Object obj) {
 		if (obj == null) {
 			return false;
 		}
         if (getClass() != obj.getClass()) {
             return false;
         }
         final Position other = (Position) obj;
         if (this._world != other._world && (this._world == null || !this._world.equals(other._world))) {
         	return false;
         }
         if (Double.doubleToLongBits(this._x) != Double.doubleToLongBits(other._x)) {
 			return false;
         }
         if (Double.doubleToLongBits(this._z) != Double.doubleToLongBits(other._z)) {
         	return false;
         }
         	return true;
 	}
 		
 	@Override
 	public int hashCode() {
 		int hash = 3;
 		hash = 19 * hash + (this._world != null ? this._world.hashCode() : 0);
 		hash = 19 * hash + (int) (Double.doubleToLongBits(this._x) ^ (Double.doubleToLongBits(this._x) >>> 32));
 		hash = 19 * hash + (int) (Double.doubleToLongBits(this._z) ^ (Double.doubleToLongBits(this._z) >>> 32));
 		return hash;
 	}
 	
 	@Override
 	public Position clone() {
 		try {
 			Position l = (Position) super.clone();
 			l._world = _world;
 			l._x = _x;
             l._z = _z;
             return l;
 		} catch (CloneNotSupportedException e) {
             e.printStackTrace();
         }
 		return null;
 	}
 }
