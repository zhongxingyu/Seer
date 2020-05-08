 package rebelkeithy.mods.keithyutils;
 
 import java.util.ArrayList;
 import java.util.List;
 
import net.minecraft.world.World;

 public class Coord 
 {
 	public int x;
 	public int y;
 	public int z;
 	
 	public Coord(int dx, int dy, int dz)
 	{
 		x = dx;
 		y = dy;
 		z = dz;
 	}
 
 	public static List<Coord> get4AdjacentSides()
 	{
 		return get4AdjacentSides(0, 0, 0);
 	}
 	
 	public static List<Coord> get4AdjacentSides(int x, int y, int z)
 	{
 		List<Coord> coords = new ArrayList<Coord>();
 		
 		coords.add(new Coord(x + 1, y, z));
 		coords.add(new Coord(x - 1, y, z));
 		coords.add(new Coord(x, y, z + 1));
 		coords.add(new Coord(x, y, z - 1));
 		
 		return coords;
 	}
 
 	public static List<Coord> get6AdjacentSides()
 	{
 		return get6AdjacentSides(0, 0, 0);
 	}
 	
 	public static List<Coord> get6AdjacentSides(int x, int y, int z)
 	{
 		List<Coord> coords = new ArrayList<Coord>();
 
 		coords.add(new Coord(x + 1, y, z));
 		coords.add(new Coord(x - 1, y, z));
 		coords.add(new Coord(x, y, z + 1));
 		coords.add(new Coord(x, y, z - 1));
 		coords.add(new Coord(x, y + 1, z));
 		coords.add(new Coord(x, y - 1, z));
 		
 		return coords;
 	}
 	
 	public int getBlockID(World world)
 	{
 		return world.getBlockId(x, y, z);
 	}
 	
 	public int getMetadata(World world)
 	{
 		return world.getBlockMetadata(x, y, z);
 	}
 }
