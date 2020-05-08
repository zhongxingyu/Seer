 package com.rusketh.creator.tasks.selection;
 
 import java.util.ArrayList;
 
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.Action;
 import org.bukkit.util.Vector;
 
 import com.rusketh.util.Direction;
 
 public abstract class Selection implements Cloneable {
 	
 	public Selection( World world ) {
 		this.world = world;
 		reset();
 	}
 	
 	/*========================================================================================================*/
 	
 	public World getWorld( ) {
 		return world;
 	}
 	
 	public void setWorld( World world ) {
 		this.world = world;
 		reset( );
 	}
 	
 	/*========================================================================================================*/
 	
 	public void setMin( Vector min ) {
 		this.min = min;
 	}
 	
 	public void setMax( Vector max ) {
 		this.max = max;
 	}
 	
 	/*========================================================================================================*/
 	
 	public Vector getMin( ) {
 		return min;
 	}
 	
 	public Vector getMax( ){
 		return max;
 	}
 	
 	public Vector getCenter( ) {
 		return min.add( getSize().divide( new Vector(2, 2, 2 ) ) );
 	}
 	
 	/*========================================================================================================*/
 	
 	public abstract int getVolume( );
 	
 	public Vector getSize( ) {
		return max.subtract( min );
 	}
 	
 	/*========================================================================================================*/
 	
 	public abstract int expand(Direction dir, int amount);
 	
 	public abstract void shift(Direction dir, int amount);
 	
 	/*========================================================================================================*/
 	
 	public abstract void first( );
 	
 	public abstract Block nextBlock( );
 	
 	public abstract boolean hasNextBlock();
 	
 	public ArrayList< Block > nextBlocks( int count ) {
 		ArrayList< Block > blocks = new ArrayList< Block >();
 		
 		for (int i = 0; (i < count && hasNextBlock()); i++) {
 			Block block = nextBlock( );
 			blocks.add( block );
 		}
 		
 		return blocks;
 	}
 	
 	/*========================================================================================================*/
 	
 	public abstract boolean contains( Vector pos );
 	
 	public abstract boolean isValid();
 	
 	/*========================================================================================================*/
 	
 	public abstract Selection clone();
 	
 	/*========================================================================================================*/
 	
 	public void reset() {
 		min = new Vector(0, 0, 0);
 		max = new Vector(0, 0, 0);
 	}
 	
 	/*========================================================================================================*/
 	
 	public abstract boolean wandEvent(Player player, Block block, Action action);
 	
 	/*========================================================================================================*/
 	
 	private World	world;
 	private Vector 	min, max;
 }
