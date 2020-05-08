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
 
 package org.ojim.client;
 
 import org.ojim.iface.IClient;
 import org.ojim.log.OJIMLogger;
 import org.ojim.logic.Logic;
 import org.ojim.logic.rules.GameRules;
 import org.ojim.logic.state.GameState;
 import org.ojim.logic.state.Player;
 import org.ojim.logic.state.fields.BuyableField;
 import org.ojim.logic.state.fields.Street;
 import org.ojim.rmi.server.NetOjim;
 
 import edu.kit.iti.pse.iface.IServer;
 import edu.kit.iti.pse.iface.IServerAuction;
 import edu.kit.iti.pse.iface.IServerTrade;
 
 /**
  * Simpler client Implementation. This client only wraps the methods of IServer
  * and checks it with the rules.
  * 
  * But no “Connection” and “IClient” support.
  * 
  * @author Fabian Neundorf.
  */
 public class SimpleClient {
 
 	/**
 	 * Different trade states.
 	 * 
 	 * @author Fabian Neundorf.
 	 * @see {@link IServerTrade#getTradeState()}.
 	 */
 	public enum TradeState {
 		NOT_RUNNING(-1), WAITING_PROPOSAL(0), WAITING_PROPOSED(1), ACCEPTED(2), DECLINED(3);
 
 		public final int value;
 
 		TradeState(int value) {
 			this.value = value;
 		}
 
 		public static TradeState getState(int state) {
 			switch (state) {
 			case -1:
 				return NOT_RUNNING;
 			case 0:
 				return WAITING_PROPOSAL;
 			case 1:
 				return WAITING_PROPOSED;
 			case 2:
 				return ACCEPTED;
 			case 3:
 				return DECLINED;
 			default:
 				throw new IllegalArgumentException("state is not recognized");
 			}
 		}
 	}
 
 	private IServer server;
 	private Logic logic;
 	private Player me;
 
 	private int playerId;
 
 	public SimpleClient() {
 
 	}
 
 	public SimpleClient(Logic logic, int playerId, IServer server) {
 		this.setParameters(logic, playerId, server);
 		this.me = this.getGameState().getPlayerByID(playerId);
 	}
 
 	private void setMyPlayer(Player player) {
 		this.me = player;
 	}
 
 	protected Player getMe() {
 		return this.me;
 	}
 
 	protected Logic getLogic() {
 		return this.logic;
 	}
 
 	protected void setPlayerId(int id) {
 		this.playerId = id;
 	}
 
 	protected GameState getGameState() {
 		return this.logic.getGameState();
 	}
 
 	protected GameRules getGameRules() {
 		return this.logic.getGameRules();
 	}
 
 	protected void setParameters(Logic logic, int playerId, IServer server) {
 		this.logic = logic;
 		this.playerId = playerId;
 		this.server = server;
 	}
 
 	protected void setParameters(IServer server, IClient client) {
 		this.setParameters(server, client, new GameState());
 	}
 
 	protected void setParameters(IServer server, IClient client, GameState state) {
 		this.server = server;
 		this.logic = new Logic(state, server.getRules());
 		this.playerId = server.addPlayer(client);
 		// Load my data
 		client.informNewPlayer(this.playerId);
 		this.setMyPlayer(state.getPlayerByID(this.playerId));
 	}
 
 	public final int getPlayerId() {
 		return this.playerId;
 	}
 
 	/*
 	 * GETTER OF ISERVER
 	 */
 
 	public int getEstateColorGroup(int position) {
 		return this.server.getEstateColorGroup(position);
 	}
 
 	public String getEstateName(int position) {
 		return this.server.getEstateName(position);
 	}
 
 	public int getEstatePrice(int position) {
 		return this.server.getEstatePrice(position);
 	}
 
 	public int getEstateRent(int position, int houses) {
 		return this.server.getEstateRent(position, houses);
 	}
 
 	public int getEstateHouses(int position) {
 		return this.server.getEstateHouses(position);
 	}
 
 	public boolean isMortgaged(int position) {
 		return this.server.isMortgaged(position);
 	}
 
 	public int getOwner(int position) {
 		return this.server.getOwner(position);
 	}
 
 	// Bank
 	public int getNumberOfHousesLeft() {
 		return this.server.getNumberOfHousesLeft();
 	}
 
 	public int getNumberOfHotelsLeft() {
 		return this.server.getNumberOfHotelsLeft();
 	}
 
 	// Spieler
 	public int getPlayerCash(int playerID) {
 		return this.server.getPlayerCash(playerID);
 	}
 
 	public String getPlayerName(int player) {
 		return this.server.getPlayerName(player);
 	}
 
 	public int getPlayerColor(int player) {
 		return this.server.getPlayerColor(player);
 	}
 
 	public int getPlayerPiecePosition(int playerID) {
 		return this.server.getPlayerPiecePosition(playerID);
 	}
 
 	public int getEstateHousePrice(int position) {
 		return this.server.getEstateHousePrice(position);
 	}
 
 	public int getNumberOfGetOutOfJailCards(int playerID) {
 		return this.server.getNumberOfGetOutOfJailCards(playerID);
 	}
 
 	/*
 	 * ADITIONAL GETTER
 	 */
 
 	/**
 	 * Returns the money the player has to pay to leave the jail.
 	 * 
 	 * @param position
 	 *            The position of the jail.
 	 * @return The money the player has to pay. If there is no money the return
 	 *         is undefined;.
 	 */
 	public int getMoneyToPay(int position) {
 		if (this.server instanceof NetOjim) {
 //			 return ((NetOjim) this.server).getMoneyToPay(position);
 			return 1000;
 		} else {
 			return 1000; // TODO: (xZise) Is this the correct value?
 		}
 	}
 
 	/**
 	 * Returns the number of rounds the player has to wait if the player is in
 	 * jail.
 	 * 
 	 * @param position
 	 *            The position of the jail.
 	 * @return The number of rounds the player has to wait. If this is no jail
 	 *         it return is undefined.
 	 */
 	public int getRoundsToWait(int position) {
 		if (this.server instanceof NetOjim) {
 			// return ((NetOjim) this.server).getRoundsToWait(position);
 			return 3;
 		} else {
 			return 3; // TODO: (xZise) Is this the correct value?
 		}
 	}
 
 	/*
 	 * ACTION METHODS
 	 */
 
 	/**
 	 * Setzt diesen Spieler auf „bereit“.
 	 */
 	protected final void ready() {
 		this.server.setPlayerReady(this.playerId);
 	}
 
 	/**
 	 * Akzeptiert die Nachricht.
 	 * 
 	 * @see {@link edu.kit.iti.pse.iface.IServer.accept}
 	 */
 	protected final void accept() {
 		this.server.accept(this.playerId);
 	}
 
 	protected final void decline() {
 		this.server.decline(this.playerId);
 	}
 
 	protected final boolean rollDice() {
 		if (this.getGameRules().isPlayerOnTurn(this.me)) {
 			return this.server.rollDice(this.playerId);
 		} else {
 			System.out.println("not on turn");
 			return false;
 		}
 	}
 
 	protected final void endTurn() {
 		if (this.getGameRules().isPlayerOnTurn(this.me)) {
 			this.server.endTurn(this.playerId);
 		} else {
 			System.out.println("not on turn");
 		}
 	}
 
 	protected final void declareBankruptcy() {
 		this.server.declareBankruptcy(this.playerId);
 	}
 
 	protected final void construct(Street street) {
 		if (this.getGameRules().isFieldUpgradable(this.me, street, +1)) {
 			this.server.construct(this.playerId, street.getPosition());
 		}
 	}
 
 	protected final void destruct(Street street) {
 		if (this.getGameRules().isFieldUpgradable(this.me, street, -1)) {
 			this.server.deconstruct(this.playerId, street.getPosition());
 		}
 	}
 
 	protected final void toggleMortgage(BuyableField street) {
 		if (this.getGameRules().isFieldMortgageable(this.me, street)) {
 			this.server.toggleMortgage(this.playerId, street.getPosition());
 		}
 	}
 
 	protected final void sendMessage(String text) {
 		this.server.sendMessage(text, this.playerId);
 	}
 
 	/**
 	 * @deprecated Use {@link #sendPrivateMessage(String, Player)}
 	 */
 	protected final void sendPrivateMessage(String text, int reciever) {
 		this.server.sendPrivateMessage(text, this.playerId, reciever);
 	}
 
 	protected final void sendPrivateMessage(String text, Player reciever) {
 		this.server.sendPrivateMessage(text, this.playerId, reciever.getId());
 	}
 
 	protected final boolean payFine() {
 		return this.server.payFine(this.playerId);
 	}
 
 	protected final boolean useGetOutOfJailCard() {
 		return this.server.useGetOutOfJailCard(this.playerId);
 	}
 
 	/*
 	 * RULES
 	 */
 
 	public final boolean isMyTurn() {
 		return this.getGameRules().isPlayerOnTurn(this.me);
 	}
 
 	/*
 	 * TRADE
 	 */
 
 	/**
 	 * @deprecated Use {@link #initTrade(Player)}
 	 */
 	public final boolean initTrade(int partnerPlayer) {
 		return ((IServerTrade) this.server).initTrade(playerId, partnerPlayer);
 	}
 
 	public final boolean initTrade(Player partnerPlayer) {
 		int id = -1;
 		if (partnerPlayer != null) {
 			id = partnerPlayer.getId();
 		}
 		return ((IServerTrade) this.server).initTrade(this.playerId, id);
 	}
 
 	/**
 	 * @deprecated Use {@link #getTradeState()}
 	 */
 	public final int getTradeStateO() {
 		return ((IServerTrade) this.server).getTradeState();
 	}
 
 	public final TradeState getTradeState() {
 		return TradeState.getState(((IServerTrade) this.server).getTradeState());
 	}
 
 	/**
 	 * @deprecated Use {@link #getPartner()}
 	 */
 	public final int getPartnerO() {
 		return ((IServerTrade) this.server).getPartner();
 	}
 
 	public final Player getPartner() {
 		int id = ((IServerTrade) this.server).getPartner();
 		Player partner = null;
 		if (id >= 0) {
 			partner = this.getGameState().getPlayerByID(id);
 			if (partner == null) {
 				OJIMLogger.getLogger(this.getClass().toString()).severe("partner is unkown");
 			}
 		}
 		return partner;
 	}
 
 	public final boolean offerCash(int amount) {
 		return ((IServerTrade) this.server).offerCash(playerId, amount);
 	}
 
 	public final boolean offerGetOutOfJailCard() {
 		return this.offerGetOutOfJailCard(1) == 0;
 	}
 
 	/**
 	 * Offers so often a get out of jail card until, it returns false.
 	 * 
 	 * @param amount
 	 *            the number of offered cards.
 	 * @return false if the offering fails. Then it has offered less than the
 	 *         given amount. If all cards could be offered it returns true.
 	 */
 	public final int offerGetOutOfJailCard(int amount) {
 		while (amount-- > 0) {
 			if (!((IServerTrade) this.server).offerGetOutOfJailCard(playerId)) {
 				return amount + 1;
 			}
 		}
 		return amount;
 	}
 
 	/**
 	 * @deprecated Use {@link #offerEstate(BuyableField)}
 	 */
 	public final boolean offerEstate(int position) {
 		return ((IServerTrade) this.server).offerEstate(playerId, position);
 	}
 
 	public final boolean offerEstate(BuyableField field) {
 		return ((IServerTrade) this.server).offerEstate(playerId, field.getPosition());
 	}
 
 	public final boolean requireCash(int amount) {
 		return ((IServerTrade) this.server).requireCash(playerId, amount);
 	}
 
 	public final boolean requireGetOutOfJailCard() {
 		return this.requireGetOutOfJailCard(1) == 0;
 	}
 
 	/**
 	 * Requires so often a get out of jail card until, it returns false.
 	 * 
 	 * @param amount
 	 *            the number of required cards.
 	 * @return The number of remaining cards which couldn't be required.
 	 */
 	public final int requireGetOutOfJailCard(int amount) {
 		while (amount-- > 0) {
 			if (!((IServerTrade) this.server).requireGetOutOfJailCard(playerId)) {
 				return amount + 1;
 			}
 		}
 		return 0;
 	}
 
 	/**
 	 * @deprecated Use {@link #requireEstate(BuyableField)}
 	 */
 	public final boolean requireEstate(int position) {
 		return ((IServerTrade) this.server).requireEstate(playerId, position);
 	}
 
 	public final boolean requireEstate(BuyableField field) {
 		return ((IServerTrade) this.server).requireEstate(playerId, field.getPosition());
 	}
 
 	/**
 	 * @deprecated Use {@link #getOfferedEstate()}
 	 */
 	public final int[] getOfferedEstatesO() {
 		return ((IServerTrade) this.server).getOfferedEstates();
 	}
 
 	public final BuyableField[] getOfferedEstate() {
 		int[] fields = ((IServerTrade) this.server).getOfferedEstates();
 		BuyableField[] result = new BuyableField[fields.length];
 		for (int i = 0; i < fields.length; i++) {
 			result[i] = (BuyableField) this.getGameState().getFieldAt(fields[i]);
 		}
 		return result;
 	}
 
 	public final int getOfferedCash() {
 		return ((IServerTrade) this.server).getOfferedCash();
 	}
 
 	public final int getNumberOfOfferedGetOutOfJailCards() {
 		return ((IServerTrade) this.server).getNumberOfOfferedGetOutOfJailCards();
 	}
 
 	/**
 	 * @deprecated Use {@link #getRequiredEstates()}
 	 */
 	public final int[] getRequiredEstatesO() {
 		return ((IServerTrade) this.server).getRequiredEstates();
 	}
 
 	public final BuyableField[] getRequiredEstates() {
 		int[] fields = ((IServerTrade) this.server).getRequiredEstates();
 		BuyableField[] result = new BuyableField[fields.length];
 		for (int i = 0; i < fields.length; i++) {
 			result[i] = (BuyableField) this.getGameState().getFieldAt(fields[i]);
 		}
 		return result;
 	}
 
 	public final int getRequiredCash() {
 		return ((IServerTrade) this.server).getRequiredCash();
 	}
 
 	public final int getNumberOfRequiredGetOutOfJailCards() {
 		return ((IServerTrade) this.server).getNumberOfRequiredGetOutOfJailCards();
 	}
 
 	public final boolean cancelTrade() {
 		return ((IServerTrade) this.server).cancelTrade(this.playerId);
 	}
 
 	public final boolean proposeTrade() {
 		return ((IServerTrade) this.server).proposeTrade(this.playerId);
 	}
 
 	/*
 	 * AUCTION
 	 */
 
 	public final int getAuctionState() {
 		return ((IServerAuction) this.server).getAuctionState();
 	}
 
 	/**
 	 * @deprecated Use {@link #getAuctionedEstate()}
 	 */
 	public final int getAuctionedEstateO() {
 		return ((IServerAuction) this.server).getAuctionedEstate();
 	}
 
 	public final BuyableField getAuctionedEstate() {
 		return (BuyableField) this.getGameState().getFieldAt(((IServerAuction) this.server).getAuctionedEstate());
 	}
 
 	public final int getHighestBid() {
 		return ((IServerAuction) this.server).getHighestBid();
 	}
 
	/**
	 * Returns the highest bidder. If nobody has bid its null.
	 * @return the highest bidder. If nobody has bid its null.
	 */
 	public final Player getBidder() {
		int id = ((IServerAuction) this.server).getBidder();
		Player bidder = null;
		if (id >= 0) {
			bidder = this.getGameState().getPlayerByID(id);
			if (bidder == null) {
				OJIMLogger.getLogger(this.getClass().toString()).severe("bidder is unkown");
			}
		}
		return bidder;
 	}
 
 	public final boolean placeBid(int amount) {
 		return ((IServerAuction) this.server).placeBid(this.playerId, amount);
 	}
 }
