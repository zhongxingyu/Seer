 /** 
  * Copyright (C) 2011 Flow86
  * 
  * AdditionalBuildcraftObjects is open-source.
  *
  * It is distributed under the terms of my Open Source License. 
  * It grants rights to read, modify, compile or run the code. 
  * It does *NOT* grant the right to redistribute this software or its 
  * modifications in any form, binary or source, except if expressively
  * granted by the copyright holder.
  */
 
 package net.minecraft.src.AdditionalBuildcraftObjects;
 
 import net.minecraft.src.buildcraft.transport.Pipe;
 import net.minecraft.src.buildcraft.transport.PipeLogicStone;
import net.minecraft.src.buildcraft.transport.PipeTransportItems;
 
 public class PipeItemsInsertion extends Pipe {
 	public PipeItemsInsertion(int itemID) {
 		super(new PipeTransportItemsInsertion(), new PipeLogicStone(), itemID);
		
		((PipeTransportItems) transport).allowBouncing = true;
 	}
 
 	@Override
 	public int getMainBlockTexture() {
 		return 8 * 16 + 0;
 	}
 
 }
