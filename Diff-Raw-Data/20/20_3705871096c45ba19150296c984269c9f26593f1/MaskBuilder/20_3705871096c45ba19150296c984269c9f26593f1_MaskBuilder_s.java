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
 
 package com.rusketh.creator.masks;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
import org.bukkit.command.CommandException;
 import org.bukkit.inventory.ItemStack;
 
 import com.rusketh.creator.Extensions.ItemExtension;
 import com.rusketh.creator.blocks.CreatorBlock;
 import com.rusketh.creator.blocks.CreatorItem;
 import com.rusketh.creator.exceptions.WildDataException;
 
 
 public class MaskBuilder {
 	
 	public MaskBuilder(String string) {
 		buffer = string;
 		pos = 0;
 		
 		mask = nextMask();
 	}
 	
 	/*========================================================================================================*/
 	
 	public Mask getMask() {
 		return mask;
 	}
 	
 	/*========================================================================================================*/
 	
 	public Mask nextMask() {
 		
 		Mask mask;
 		
 		switch( buffer.charAt( pos ) ) {
 			case '(':
 				pos++;
 				mask = nextMask();
				if (buffer.charAt( pos ) != ')') throw new CommandException("Invalid mask.");
 				break;
 				
 			case '!':
 				pos++;
 				mask = new NotMask( nextMask() );
 				break;
 				
 			case '>':
 				pos++;
 				mask = new UnderMask( nextMask() );
 				break;
 			
 			case '<':
 				pos++;
 				mask = new AboveMask( nextMask() );
 				break;
 			
 			case '&': case '|': //These can't be used here.
				throw new CommandException("Invalid mask.");
 				
 			default:
 				mask = nextBlockMask();
 		}
 		
 		//Now find the operators.
 		switch( buffer.charAt( pos ) ) {
 			case '&':
 				pos++;
 				mask = new AndMask( mask, nextMask() );
 				break;
 				
 			case '!':
 				pos++;
 				mask = new OrMask( mask, nextMask() );
 				break;
 		}
 		
 		return mask;
 	}
 	
 	/*========================================================================================================*/
 	
 	public Mask nextBlockMask() {
 		Matcher m = pattern.matcher( buffer.substring( pos ) );
 		
		if ( !m.matches( ) ) throw new CommandException("Invalid mask.");
 		
 		BlockMask mask = new BlockMask();
 		
 		for ( String string : m.group( ).split( "," ) ) {
 			pos += 1 + string.length( );
 			
 			try {
 				ItemStack item = ItemExtension.stringToItemStack( string );
 				
 				int type = item.getTypeId( );
 				byte data = item.getData( ).getData( );
 				
				if ( CreatorBlock.get( type ) == null ) throw new CommandException( new StringBuilder( "'" ).append( CreatorItem.get( type ).niceName( data ) ).append( "' is not a placeable block." ).toString( ) );
 				
 				mask.add(type, data);
 			} catch (WildDataException e) {
				if ( CreatorBlock.get( e.typeId ) == null ) throw new CommandException( new StringBuilder( "'" ).append( CreatorItem.get( e.typeId ).name( ) ).append( "' is not a placeable block." ).toString( ) );
 				
 				for ( int data : CreatorItem.get( e.typeId ).dataValues( ).values( ) ) mask.add( e.typeId, (byte) data);
 			}
 		}
 	
 		pos--;
 		return mask;
 	}
 	
 	/*========================================================================================================*/
 	
 	private int pos;
 	private String buffer;
 	private Mask mask;
 	
 	public final Pattern					pattern	= Pattern.compile( "(^[0-9a-zA-z:*,]+)" );
 }
