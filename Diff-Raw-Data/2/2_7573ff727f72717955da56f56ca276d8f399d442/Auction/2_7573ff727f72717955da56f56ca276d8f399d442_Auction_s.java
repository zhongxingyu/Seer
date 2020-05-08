 /*  Copyright (C) 2010 - 2011  Fabian Neundorf, Philip Caroli,
  *  Maximilian Madlung,	Usman Ghani Ahmed, Jeremias Mechler
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *  
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *  
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.ojim.logic.state;
 
 import org.ojim.client.SimpleClient.AuctionState;
 import org.ojim.logic.state.fields.BuyableField;
 
 public class Auction {
 	
 	public final BuyableField objective;
 	private AuctionState auctionState;
 	private Player highestBidder;
 	private int currentBid;
 	
 	public Auction(BuyableField objective) {
 		this(objective, AuctionState.WAITING);
 	}
 	
 	public Auction(BuyableField objective, AuctionState state) {
 		this.objective = objective;
 		this.auctionState = state;
 		this.highestBidder = null; // Normally nobody will buy this
 		this.currentBid = 0; // Starting by 0
 		//See: GameRules.getAuctionStartBid(objective);		
 	}
 	
 	public void setState(AuctionState state) {
 		if (state == AuctionState.NOT_RUNNING) {
 			throw new IllegalArgumentException("The state can't be not running.");
 		}
 		this.auctionState = state;
 	}
 	
 	public AuctionState getState() {
 		return this.auctionState;
 	}
 	
 	public Player getHighestBidder() {
 		return this.highestBidder;
 	}
 
 	public int getHighestBid() {
 		return this.currentBid;
 	}
 	
 	/**
 	 * Places a bid. It doesn't inform other player/server.
 	 * @param bidder The new bidder.
 	 * @param bid The new bid.
 	 * @return if the bid is allowed (higher) than the previous.
 	 */
 	public boolean placeBid(Player bidder, int bid) {
		if (bid <= this.currentBid) {
 			return false;
 		}
 		
 		// Set the Bidder as the Highest Bidder
 		this.highestBidder = bidder;
 		this.currentBid = bid;
 		this.setState(AuctionState.WAITING);
 		return true;
 	}
 }
