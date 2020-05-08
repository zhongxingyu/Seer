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
 
 package com.rusketh.creator.tasks.selection;
 
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.Action;
 import org.bukkit.util.Vector;
 
 import com.rusketh.util.CreatorString;
 import com.rusketh.util.Direction;
 
 public class BoxSelection extends Selection {
 	
 	public BoxSelection( World world ) {
 		super( world );
 	}
 	
 	/*========================================================================================================*/
 	
 	public void setPos1( Vector pos1 ) {
 		this.pos1 = pos1;
 		reCaculate( );
 	}
 	
 	public Vector getPos1() {
 		return pos1;
 	}
 	
 	/*========================================================================================================*/
 	
 	public void setPos2( Vector pos2 ) {
 		this.pos2 = pos2;
 		reCaculate( );
 	}
 	
 	public Vector getPos2() {
 		return pos2;
 	}
 	
 	/*========================================================================================================*/
 	
 	public int getVolume( ) {
 		Vector size = getSize();
 		return (int) (size.getBlockX( ) * size.getBlockY( ) * size.getBlockZ( ));
 	}
 	
 	/*========================================================================================================*/
 	
 	public int expand(Direction dir, int amount) {
 		int cur = getVolume( );
 		
 		Vector min = getMin();
 		Vector max = getMax();
 		
 		if ( dir.getX() < 1 ) min.setX(min.getX() + (dir.getX() * amount));
 		if ( dir.getX() > 1 ) min.setX(max.getX() + (dir.getX() * amount));
 		
 		if ( dir.getY() < 1 ) min.setX(min.getY() + (dir.getY() * amount));
 		if ( dir.getY() > 1 ) min.setX(max.getY() + (dir.getY() * amount));
 		
 		if ( dir.getZ() < 1 ) min.setX(min.getZ() + (dir.getZ() * amount));
 		if ( dir.getZ() > 1 ) min.setX(max.getZ() + (dir.getZ() * amount));
 		
 		pos1 = min;
 		pos2 = max;
 		
 		setMin(min);
 		setMax(max);
 		
 		return getVolume( ) - cur;
 	}
 	
 	/*========================================================================================================*/
 	
 	public void shift(Direction dir, int amount) {
 		setMin(pos1 = getMin().add(dir.vector().multiply(amount)));
 		setMax(pos1 = getMax().add(dir.vector().multiply(amount)));
 	}
 	
 	/*========================================================================================================*/
 	
 	public void first( ) {
 		Vector min = getMin();
 		
 		indexX = min.getBlockX();
 		indexY = min.getBlockY();
 		indexZ = min.getBlockZ();
 		
 		nextBlock = getWorld().getBlockAt(indexX, indexY, indexZ);
 	}
 	
 	public Block nextBlock( ) {
 		Vector max = getMax();
 		Vector min = getMin();
 		
 		Block block = nextBlock;
 		
 		if ( indexX > max.getBlockX() ) {
			indexX = min.getBlockY( );
 			
 			if ( indexY > max.getBlockY( ) ) {
 				indexY = min.getBlockY( );
 				
 				if ( indexZ > max.getBlockZ( ) ) {
 					nextBlock = null;
 					return block;
 				} else {
 					indexZ++;
 				}
 				
 			} else {
 				indexY++;
 			}
 			
 		} else {
 			indexX++;
 		}
 		
 		nextBlock = getWorld().getBlockAt(indexX, indexY, indexZ);
 		return block;
 	}
 	
 	public boolean hasNextBlock() {
 		return (nextBlock != null);
 	}
 	
 	/*========================================================================================================*/
 	
 	public boolean contains( Vector pos ) {
 		Vector min = getMin();
 		Vector max = getMax();
 		
 		double x = pos.getBlockX();
 		double y = pos.getBlockY();
 		double z = pos.getBlockZ();
 		
 		return (x >= min.getBlockX() && x <= max.getBlockX()) && (y >= min.getBlockY() && y <= max.getBlockY()) && (z >= min.getBlockZ() && z <= max.getBlockZ());
 	}
 	
 	public boolean isValid( ) {
 		return ((pos1 != null) && (pos2 != null));
 	}
 	
 	/*========================================================================================================*/
 	
 	public Selection clone( ) {
 		BoxSelection clone = new BoxSelection(getWorld());
 		
 		clone.setPos1( pos1 );
 		clone.setPos2( pos2 );
 		
 		return clone;
 	}
 	
 	/*========================================================================================================*/
 	
 	public void reset( ) {
 		super.reset();
 		pos1 = null;
 		pos2 = null;
 	}
 	
 	private void reCaculate() {
 		if ( !isValid() ) return;
 		
 		Vector min = pos1.clone( );
 		Vector max = pos2.clone( );
 		
 		if ( pos1.getX( ) > pos2.getX( ) ) {
 			min.setX( pos2.getX( ) );
 			max.setX( pos1.getX( ) );
 		}
 		
 		if ( pos1.getY( ) > pos2.getY( ) ) {
 			min.setY( pos2.getY( ) );
 			max.setY( pos1.getY( ) );
 		}
 		
 		if ( pos1.getZ( ) > pos2.getZ( ) ) {
 			min.setZ( pos2.getZ( ) );
 			max.setZ( pos1.getZ( ) );
 		}
 		
 		setMin(min);
 		setMax(max);
 	}
 	
 	/*========================================================================================================*/
 	
 	public boolean wandEvent(Player player, Block block, Action action) {
 		Vector pos = block.getLocation().toVector();
 		
 		if ( action == Action.LEFT_CLICK_BLOCK ) {
 			if ( !block.getWorld().equals(getWorld()) ) reset();
 			
 			setPos1(pos);
 			player.sendMessage( new CreatorString("%gPosition 1 set (%b", pos.toString(), "%g).").toString() );
 			
 			return true;
 		
 		}else if ( action == Action.RIGHT_CLICK_BLOCK ) {
 			if ( !block.getWorld().equals(getWorld()) ) reset();
 			
 			setPos2(pos);
 			player.sendMessage( new CreatorString("%gPosition 2 set (%b", pos.toString(), "%g).").toString() );
 			
 			return true;
 		}
 		
 		return false;
 	}
 	
 	/*========================================================================================================*/
 	
 	private int indexX, indexY, indexZ = -1;
 	private Vector pos1, pos2;
 	private Block nextBlock;
 }
