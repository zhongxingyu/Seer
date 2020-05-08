 /*
  * Creator - Bukkit Plugin
  * Copyright (C) 2012 Rusketh & Oskar94 <www.Rusketh.com>
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.rusketh.creator.tasks;
 
 import java.util.ArrayList;
 import org.bukkit.Chunk;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BrewingStand;
 import org.bukkit.block.Chest;
 import org.bukkit.block.Dispenser;
 import org.bukkit.block.Furnace;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 import com.rusketh.creator.blocks.BlockArray;
 import com.rusketh.creator.blocks.BlockID;
 import com.rusketh.creator.blocks.CreatorBlock;
 import com.rusketh.creator.blocks.CreatorItemStack;
 import com.rusketh.creator.blocks.StoredBlock;
 import com.rusketh.creator.exceptions.MaxBlocksChangedException;
 import com.rusketh.creator.masks.Mask;
 
 public abstract class Task {
 	
 	public Task( TaskSession session, World world, int rate ) {
 		this.session = session;
 		this.world = world;
 		this.rate = rate;
 	}
 	
 	/*========================================================================================================*/
 	
 	public TaskSession getSession( ) {
 		return session;
 	}
 	
 	public int getRate( ) {
 		return rate;
 	}
 	
 	public World getWorld( ) {
 		return world;
 	}
 	
 	/*========================================================================================================*/
 	
 	public void setBag( TaskBag bag ) {
 		this.bag = bag;
 	}
 	
 	public TaskBag getBag( ) {
 		return bag;
 	}
 	
 	/*========================================================================================================*/
 	
 	public void setMask( Mask mask ) {
 		this.mask = mask;
 	}
 	
 	public Mask getMask( ) {
 		return mask;
 	}
 	
 	/*========================================================================================================*/
 	
 	public void setNoneQueued( ) {
 		queued = false;
 	}
 	
 	public void setBlockPrice(int price) {
 		this.price = price;
 	}
 	
 	/*========================================================================================================*/
 	
 	public int getCount( ) {
 		return counter;
 	}
 	
 	/*========================================================================================================*/
 	
 	public ArrayList< StoredBlock > getUndoArray( ) {
 		return oldBlocks;
 	}
 	
 	public ArrayList< StoredBlock > getRedoArray( ) {
 		return newBlocks;
 	}
 	
 	/*========================================================================================================*/
 	
 	public boolean run( ) {
 		if ( bag != null ) bag.loadInventory( );
 		
 		if ( !processing ) {
 			processing = runTask( );
 			
		} else if ( queued && process < 3 ) {
			
			if ( process == 0 ) {
				if ( doQueue( queueAfter ) ) process++;
			} else if ( process == 1 ) {
				if ( doQueue( queueLast ) ) process++;
			} else if ( process == 2 ) {
				if ( doQueue( queueFinal ) ) process++;
			}
 			
 		} else if ( finish( ) ) {
 			if ( bag != null ) bag.pushChanges( );
 			
 			return true;
 		}
 		
 		if ( bag != null ) bag.pushChanges( );
 		
 		return false;
 	}
 	
 	/*========================================================================================================*/
 	
 	public void stopTask( ) {
 		for ( Chunk chunk : newChunks ) {
 			if ( chunk.isLoaded( ) ) {
 				chunk.unload( false, true );
 			}
 		}
 	}
 	
 	public abstract boolean runTask( );
 	
 	/*========================================================================================================*/
 	
 	private boolean doQueue( TaskQue queue ) {
 		for ( int i = 1; i <= rate; i++ ) {
 			if ( queue.valid( ) ) changeBlock( queue.getBlock( ), queue.getTypeId( ), queue.getData( ) );
 			if ( !queue.next( ) ) return true;
 		}
 		
 		return false;
 	}
 	
 	private boolean finish( ) {
 		return true;
 	}
 	
 	/*========================================================================================================*/
 	
 	public boolean changeBlock( Block block, ItemStack stack ) {
 		return changeBlock( block, stack.getTypeId(), stack.getData().getData() );
 	}
 	
 	public boolean changeBlock( Block block, int type, byte data ) {
 		int y = block.getY( );
 		if ( y < 0 || y > world.getMaxHeight( ) ) return false;
 		
 		Chunk chunk = world.getChunkAt( block );
 		if ( !world.isChunkLoaded( chunk ) ) {
 			chunk.load(true);
 			newChunks.add( chunk );
 		}
 		
 		if ( price > 0 && !session.chargePrice(price) ) return false;
 		
 		if ( mask != null ) {
 			if ( !mask.check( block ) ) return false;
 		}
 		
 		if ( CreatorBlock.get( block.getTypeId( ) ).isContainerCreatorBlock( ) ) {
 			Inventory inventory = getInventory( block );
 			
 			if ( inventory != null ) {
 				//if ( bag != null ) bag.storeItems( inventory.getContents( ) ); Note: Can be used to cheat items with undo/redo.
 				inventory.clear( );
 			}
 			
 		} else if ( block.getTypeId( ) == BlockID.ICE ) {
 			block.setTypeId( BlockID.AIR );
 		}
 		
 		if ( bag == null ) {
 			bag.storeBlockDrops( block );
 			
 			if ( !bag.takeItem( new CreatorItemStack(type, data) ) ) {
 				if ( missing.contains( type, data ) ) {
 					missing.put(type, data, missing.get( type, data ) + 1);
 				} else {
 					missing.put(type, data, 1);
 				}
 				
 				return false;
 			}
 		}
 		
 		block.setTypeId( type );
 		block.setData( data );
 		
 		newBlocks.add( new StoredBlock( block ) );
 		
 		return true;
 	}
 	
 	/*========================================================================================================*/
 	
 	public Inventory getInventory( Block block ) {
 		if ( block instanceof Chest ) {
 			return ( (Chest) block ).getInventory( );
 		} else if ( block instanceof Furnace ) {
 			return ( (Furnace) block ).getInventory( );
 		} else if ( block instanceof BrewingStand ) {
 			return ( (BrewingStand) block ).getInventory( );
 		} else if ( block instanceof Dispenser ) {
 			return ( (Dispenser) block ).getInventory( );
 		}
 		
 		return null;
 	}
 	
 	/*========================================================================================================*/
 	
 	public boolean queBlock( Block block, ItemStack stack ) {
 		return queBlock( block, stack.getTypeId(), stack.getData().getData() );
 	}
 	
 	public boolean queBlock( Block block, int type, byte data ) throws MaxBlocksChangedException {
 		int maxBlocks = session.getMaxBlocks( );
 		if ( counter++ > maxBlocks && maxBlocks != -1 ) throw new MaxBlocksChangedException( maxBlocks );
 		oldBlocks.add( new StoredBlock( block ) );
 		
 		if ( queued ) {
 			CreatorBlock cBlock = CreatorBlock.get( type );
 			
 			if ( cBlock.shouldPlaceLast( ) ) {
 				queueLast.add( block, type, data );
 				return !( type == block.getTypeId( ) && data == block.getData( ) );
 				
 			} else if ( cBlock.shouldPlaceFinal( ) ) {
 				queueFinal.add( block, type, data );
 				return !( type == block.getTypeId( ) && data == block.getData( ) );
 				
 			} else if ( CreatorBlock.get( block.getTypeId( ) ).shouldPlaceLast( ) ) {
 				changeBlock( block, 0, (byte) 0 );
 				
 			} else {
 				queueAfter.add( block, type, data );
 				return !( type == block.getTypeId( ) && data == block.getData( ) );
 			}
 		}
 		
 		return changeBlock( block, type, data );
 	}
 	
 	/*========================================================================================================*/
 	
 	private TaskSession					session;
 	private World						world;
 	private int							rate;
 	
 	private TaskBag						bag;
 	private Mask						mask;
 	
 	protected int						counter			= 0;
 	protected int						price			= 0;
 	
 	private boolean						queued			= true;
 	private boolean						processing		= false;
 	private byte						process			= 0;
 	
 	private ArrayList< Chunk >			newChunks		= new ArrayList< Chunk >( );
 	private BlockArray<Integer>			missing			= new BlockArray<Integer>();
 	
 	private ArrayList< StoredBlock >	oldBlocks		= new ArrayList< StoredBlock >( );
 	private ArrayList< StoredBlock >	newBlocks		= new ArrayList< StoredBlock >( );
 	
 	private TaskQue						queueAfter		= new TaskQue( );
 	private TaskQue						queueLast		= new TaskQue( );
 	private TaskQue						queueFinal		= new TaskQue( );
 	
 }
