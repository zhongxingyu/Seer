 /*******************************************************************************
  * Copyright (c) 2012 GamezGalaxy.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  ******************************************************************************/
 package com.gamezgalaxy.GGS.API.action;
 
 import com.gamezgalaxy.GGS.API.EventHandler;
 import com.gamezgalaxy.GGS.API.Listener;
 import com.gamezgalaxy.GGS.API.player.PlayerBlockChangeEvent;
 import com.gamezgalaxy.GGS.iomodel.Player;
 import com.gamezgalaxy.GGS.world.Block;
 import com.gamezgalaxy.GGS.world.PlaceMode;
 
 public class BlockChangeAction extends Action<BlockChangeAction> implements Listener {
 
 	private int X;
 	private int Y;
 	private int Z;
 	private Block holding;
 	private PlaceMode mode;
 	
 	private boolean found;
 	
 	private BlockChangeAction(Player p, int x, int y, int z, Block holding, PlaceMode mode) {
 		setPlayer(p);
 		this.X = x;
 		this.Y = y;
 		this.Z = z;
 		this.holding = holding;
 		this.mode = mode;
 	}
 	
 	public BlockChangeAction() {
 	}
 
 	/**
 	 * The X coordinate the block change happened.
 	 * @return
 	 *        The X coordinate       
 	 */
 	public int getX() {
 		return X;
 	}
 	
 	/**
 	 * The Y coordinate the block change happened.
 	 * @return
 	 *        The Y coordinate       
 	 */
 	public int getY() {
 		return Y;
 	}
 	
 	/**
 	 * The Z coordinate the block change happened.
 	 * @return
 	 *        The Z coordinate       
 	 */
 	public int getZ() {
 		return Z;
 	}
 	
 	/**
 	 * Get the block the player was holding
 	 * when the block change happened.
 	 * @return
 	 *        The block
 	 */
 	public Block getHolding() {
 		return holding;
 	}
 	
 	/**
 	 * Get the block the player broke, if the player placed a block.
 	 * Then air is returned.
 	 * @return
 	 *        The block.
 	 */
 	public Block getOrginalBlock() {
 		return getPlayer().getLevel().getTile(X, Y, Z);
 	}
 	
 	/**
 	 * Get weather the player broke or placed a block
 	 * @return
 	 */
 	public PlaceMode getMode() {
 		return mode;
 	}
 	
 	@Override
 	protected void setup() {
 		getPlayer().getServer().getEventSystem().registerEvents(this);
 	}
 
 	@Override
 	protected BlockChangeAction getResponse() {
 		BlockChangeAction c = new BlockChangeAction(getPlayer(), X, Y, Z, holding, mode);
 		PlayerBlockChangeEvent.getEventList().unregister(this);
 		return c;
 	}
 
 	@Override
 	public boolean isCompleted() {
 		return found;
 	}
 	
 	@EventHandler
 	public void onChat(PlayerBlockChangeEvent event) {
 		if (event.getPlayer() == getPlayer()) {
 			found = true;
 			X = event.getX();
 			Y = event.getY();
 			Z = event.getZ();
 			holding = event.getBlock();
 			mode = event.getPlaceType();
 			super.wakeUp();
 			event.Cancel(true);
 		}
 	}
 }
