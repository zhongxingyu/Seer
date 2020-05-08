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
 
 
 
 import org.ojim.logic.Logic;
 import org.ojim.logic.rules.GameRules;
 import org.ojim.logic.state.GameState;
 import org.ojim.logic.state.Player;
 import org.ojim.logic.state.fields.BuyableField;
 import org.ojim.logic.state.fields.Street;
 import org.ojim.rmi.server.NetOjim;
 
 
 import edu.kit.iti.pse.iface.IServer;
 
 /**
  * Simpler client Implementation. This client only wraps the methods of IServer
  * and checks it with the rules.
  * 
  * But no “Connection” and “IClient” support.
  * 
  * @author Fabian Neundorf
  */
 public class SimpleClient {
 
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
 
 	protected void setMyPlayer(Player player) {
 		this.me = player;
 	}
 	
 	protected Player getMe() {
 		return this.me;
 	}
 	
 	protected Logic getLogic() {
 		return this.logic;
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
 //			return ((NetOjim) this.server).getMoneyToPay(position);
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
 //			return ((NetOjim) this.server).getRoundsToWait(position);
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
 
 	protected final void rollDice() {
 		if (this.getGameRules().isPlayerOnTurn(this.me)) {
 			this.server.rollDice(this.playerId);
 		} else {
 			System.out.println("not on turn");
 		}
 	}
 
 	protected final void endTurn() {
 		if (this.getGameRules().isPlayerOnTurn(this.me)) {
 			this.server.endTurn(this.playerId);
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
 
 	protected final void toggleMortage(BuyableField street) {
 		if (this.getGameRules().isFieldMortgageable(this.me, street)) {
 			this.server.toggleMortgage(this.playerId, street.getPosition());
 		}
 	}
 	
 	protected final void sendMessage(String text) {
 		this.server.sendMessage(text, this.playerId);
 	}
 
 	protected final void sendPrivateMessage(String text, int reciever) {
 		this.server.sendPrivateMessage(text, this.playerId, reciever);
 	}
 	
 	protected final void payFine() {
 		this.server.payFine(this.playerId);
 	}
 	
 	protected final void useGetOutOfJailCard() {
 		this.server.useGetOutOfJailCard(this.playerId);
 	}
 	
 	/*
 	 * RULES
 	 */
 	
 	public final boolean isMyTurn() {
 		return this.getGameRules().isPlayerOnTurn(this.me);
 	}
 }
