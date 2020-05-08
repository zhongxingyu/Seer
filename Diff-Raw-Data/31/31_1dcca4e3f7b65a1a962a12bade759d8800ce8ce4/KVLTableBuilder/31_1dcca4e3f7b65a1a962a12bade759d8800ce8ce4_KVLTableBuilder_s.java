 //
 // KVLTableBuilder.java
 // darkcircle dot 0426 at gmail dot com
 //
 // This source can be distributed under the terms of GNU General Public License version 3
 // which is derived from the license of Manalith bot.
 
 package org.manalith.ircbot.plugin.KVL;
 
 public class KVLTableBuilder {
	TokenArray array;
 	public KVLTableBuilder ()
 	{
 		this.setArray ( null );
 	}
 	public KVLTableBuilder ( TokenArray newArray )
 	{
 		this.setArray ( newArray );
 	}
 	
 	public void setArray ( TokenArray newArray )
 	{
 		this.array = newArray;
 	}
 	public TokenArray getArray ()
 	{
 		return this.array;
 	}
 	
 	public KVLTable generateKernelVersionTable ( )
 	{
 		KVLTable result = new KVLTable();
 		
 		String newTag = "";
 		String newVersion = "";
 		
 		int i = 0;
 		int columncount = 0; 
 		int arraysize = this.getArray().getSize();
 		
 		TokenUnit uTokenUnit = null;
 				
 		while ( i < arraysize )
 		{
 			if ( this.getArray().getElement(i++).getTokenSubtype() == TokenSubtype.TableOpen )
 			{
 				while ( this.getArray().getElement(i).getTokenSubtype() != TokenSubtype.TableClose )
 				{
 					if ( this.getArray().getElement(i++).getTokenSubtype() == TokenSubtype.TROpen )
 					{
 						while ( this.getArray().getElement(i).getTokenSubtype() != TokenSubtype.TRClose )
 						{
 							uTokenUnit = this.getArray().getElement(i++);
 							if ( uTokenUnit.getTokenSubtype() == TokenSubtype.TDOpen )
 							{
 							
 								columncount++; // columncount = 1;
 								while ( uTokenUnit.getTokenSubtype() != TokenSubtype.TDClose )
 								{
 									if( columncount == 1 && uTokenUnit.getTokenSubtype() == TokenSubtype.TextString )
 									{
 										newTag = uTokenUnit.getTokenString().substring(0,uTokenUnit.getTokenString().length()-1);
 										uTokenUnit = this.getArray().getElement(i++);
 										
 									}
 									else if( columncount == 2 && uTokenUnit.getTokenSubtype() == TokenSubtype.TextString )
 									{
 										newVersion = uTokenUnit.getTokenString();
 										result.addVersionInfo(newTag, newVersion);
 										uTokenUnit = this.getArray().getElement(i++);
 
 									}
 									else 
 									{
 										uTokenUnit = this.getArray().getElement(i++);
 									}
 									
 								}
 								
 							}
 						}
 						columncount = 0;
 					}
 				}
 			}
 		}
 
 		
 		return result;
 	}
 	
 }
