 package org.opengeo.gwcdistributed.seed;
 
 import java.io.Serializable;
 import java.util.Iterator;
 import java.util.NoSuchElementException;
 
import junit.framework.Assert;

 import org.geowebcache.storage.DiscontinuousTileRange;
 import org.geowebcache.storage.TileRange;
 import org.geowebcache.storage.TileRangeIterator;
 
 import com.hazelcast.core.AtomicNumber;
 
 public class DistributedTileRangeIterator implements Serializable, TileRangeIterator{
     final private TileRange tr;
 
     final private int metaX;
 
     final private int metaY;
     
     final private AtomicNumber step;
 
     /**
      * Converts a TileRangeIterator to a DistributedTileRangeIterator.  The 
      * resulting iterator has the same range and metatiling as the original.  
      * The current state of the original will be ignored and the new iterator 
      * will start from the beginning.
      * @param trItr the TileRangeIterator to use as a template
      * @param step A HazelCast AtomicNumber to track the current step in the iteration, must be set to 0.
      */
     public DistributedTileRangeIterator(TileRangeIterator trItr, AtomicNumber step){
     	this(trItr.getTileRange(), trItr.getMetaTilingFactors(), step);
     }
     
     /**
      * Iterates over the tile locations in a TileRange.  Returns exactly one tile location within each meta-tile.
      * @param tr A TileRange. DiscontinuousTileRange is not supported.
      * @param metaTilingFactors The dimensions of the meta-tiles to use.
      * @param step A HazelCast AtomicNumber to track the current step in the iteration, must be set to 0.
      */
 	public DistributedTileRangeIterator(TileRange tr, int[] metaTilingFactors,
 			AtomicNumber step) {
 		super();
		Assert.assertTrue("Step must start at 0", step.get()==0);
		Assert.assertTrue("DiscontinuousTileRange not supported by DistributedTileRangeIterator",!(tr instanceof DiscontinuousTileRange));
 		this.tr = tr;
 		this.metaX = metaTilingFactors[0];
 		this.metaY = metaTilingFactors[1];
 		this.step = step;
 	}
 
 	/**
 	 * Get the next grid location
 	 * @return
 	 */
 	public long[] nextMetaGridLocation() {
 		return gridLocAtStep(step.getAndAdd(1));
 	}
 	
 	/**
 	 * Check if there are any remaining tiles
 	 * @return
 	 */
 	public boolean hasNext() {
 		long remaining = step.get()+1; // Check the next tile
         int z = tr.getZoomStart();
         
         // Drill down to the correct level
         long zoomTiles;
         while(remaining >= (zoomTiles=getTilesAtLevel(z))){
             remaining-=zoomTiles; // Subtract the tiles at this level from those that need to be traversed
             z++;
             
             if(z>tr.getZoomStop()) return false; // Past the end of the sequence
         }
 		return true;
 	}
 	
 	/**
      * Get the location of the step'th tile in the sequence.
      * @param step
      * @return
      */
     private long[] gridLocAtStep(final long step) {
         int z = tr.getZoomStart();
         
         long remaining = step;
 
         // Drill down to the correct level
         long zoomTiles;
         while(remaining >= (zoomTiles=getTilesAtLevel(z))){
             remaining-=zoomTiles; // Subtract the tiles at this level from those that need to be traversed
             z++;
             
             if(z>tr.getZoomStop()) return null; // Past the end of the sequence
         }
         
         final long[] metaDims = metaTileDimensions(z);
         final long[] tileBounds = tr.rangeBounds(z);
         
         final long x = remaining%metaDims[0]*metaX+tileBounds[0];
         final long y = remaining/metaDims[0]*metaY+tileBounds[1];
         
         return new long[] {x,y,z};
     }
     
     /**
      * Get the dimensions of a zoom level in metaTiles
      * @param z
      * @return
      */
     private long[] metaTileDimensions(int z) {
         final long[] levelBounds = tr.rangeBounds(z);
         
         // Bounds are inclusive, so add one
         final long tilesX = levelBounds[2]-levelBounds[0]+1;
         final long tilesY = levelBounds[3]-levelBounds[1]+1;
         
         // Round up
         final long mtilesX = tilesX/metaX + ((tilesX%metaX>0) ? 1:0);
         final long mtilesY = tilesY/metaY + ((tilesY%metaY>0) ? 1:0);
         
         return new long[] {mtilesX, mtilesY};
     }
 
     
     private long getTilesAtLevel(int z){
         
         long[] dims = metaTileDimensions(z);
         
         return dims[0]*dims[1];
     }
 
 	public TileRange getTileRange() {
 		return tr;
 	}
 
 	public int[] getMetaTilingFactors() {
 		return new int[]{metaX, metaY};
 	}
 
 }
