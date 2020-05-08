 package com.ikkerens.worldedit.model;
 
 import java.util.AbstractMap.SimpleEntry;
 import java.util.ArrayList;
 import java.util.HashSet;
 
 import com.ikkerens.worldedit.exceptions.BlockLimitException;
 import com.ikkerens.worldedit.model.pattern.SetBlockType;
 
 import com.mbserver.api.game.Chunk;
 import com.mbserver.api.game.World;
 
 public class WEAction {
     private final World                                  world;
     private final boolean                                recordAction;
     private final int                                    limit;
     private int                                          affected;
 
     private final HashSet< Chunk >                       chunks;
     private ArrayList< SimpleEntry< Integer[], Short > > undoList;
 
     WEAction( final World world, final boolean recordAction, final int limit ) {
         this.world = world;
         this.recordAction = recordAction;
         this.limit = limit;
 
         if ( this.recordAction )
             this.undoList = new ArrayList< SimpleEntry< Integer[], Short > >();
 
         this.chunks = new HashSet< Chunk >();
     }
 
     public void setBlock( final int x, final int y, final int z, final short blockID ) throws BlockLimitException {
         final short current = this.world.getBlockID( x, y, z );
 
         if ( current != blockID ) {
             if ( ( this.limit != -1 ) && ( this.affected >= this.limit ) )
                 throw new BlockLimitException();
 
             if ( this.recordAction )
                 this.undoList.add( new SimpleEntry< Integer[], Short >( new Integer[] { x, y, z }, current ) );
 
             this.world.setBlockWithoutUpdate( x, y, z, blockID );
 
             this.chunks.add( this.world.getChunk( x, y, z, true ) );
 
             this.affected++;
         }
     }
 
     public void setBlock( final int x, final int y, final int z, final SetBlockType type ) throws BlockLimitException {
         this.setBlock( x, y, z, type.getNextBlock( x, y, z ) );
     }
 
     public void undo() {
         for ( final SimpleEntry< Integer[], Short > entry : this.undoList ) {
             final Integer[] keys = entry.getKey();
             this.world.setBlockWithoutUpdate( keys[ 0 ], keys[ 1 ], keys[ 2 ], entry.getValue() );
         }
     }
 
     public void finish() {
         for ( final Chunk ch : this.chunks )
             if ( ch != null )
                 ch.recalculateLight();
         this.chunks.clear();
     }
 
     public int getAffected() {
         return this.affected;
     }
 }
