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
 
 import java.rmi.RemoteException;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.ojim.client.triggers.OnAuction;
 import org.ojim.client.triggers.OnBankruptcy;
 import org.ojim.client.triggers.OnBuy;
 import org.ojim.client.triggers.OnCardPull;
 import org.ojim.client.triggers.OnCashChange;
 import org.ojim.client.triggers.OnConstruct;
 import org.ojim.client.triggers.OnDestruct;
 import org.ojim.client.triggers.OnDiceValues;
 import org.ojim.client.triggers.OnMessage;
 import org.ojim.client.triggers.OnMortgageToogle;
 import org.ojim.client.triggers.OnMove;
 import org.ojim.client.triggers.OnNewPlayer;
 import org.ojim.client.triggers.OnPlayerLeft;
 import org.ojim.client.triggers.OnStartGame;
 import org.ojim.client.triggers.OnTrade;
 import org.ojim.client.triggers.OnTurn;
 import org.ojim.iface.IClient;
 import org.ojim.log.OJIMLogger;
 import org.ojim.logic.state.GameState;
 import org.ojim.logic.state.Player;
 import org.ojim.logic.state.fields.BuyableField;
 import org.ojim.logic.state.fields.CardField;
 import org.ojim.logic.state.fields.Field;
 import org.ojim.logic.state.fields.FieldGroup;
 import org.ojim.logic.state.fields.FreeParking;
 import org.ojim.logic.state.fields.GoField;
 import org.ojim.logic.state.fields.GoToJail;
 import org.ojim.logic.state.fields.InfrastructureField;
 import org.ojim.logic.state.fields.Jail;
 import org.ojim.logic.state.fields.Station;
 import org.ojim.logic.state.fields.StationFieldGroup;
 import org.ojim.logic.state.fields.Street;
 import org.ojim.logic.state.fields.StreetFieldGroup;
 import org.ojim.logic.state.fields.TaxField;
 import org.ojim.rmi.client.ImplNetClient;
 import org.ojim.rmi.client.NetClient;
 import org.ojim.rmi.client.StartNetClient;
 import org.ojim.rmi.server.NetOjim;
 
 import edu.kit.iti.pse.iface.IServer;
 
 /**
  * Basis Client für den GUIClient und AIClient.
  * 
  * @author Fabian Neundorf
  */
 public abstract class ClientBase extends SimpleClient implements IClient {
 
 	private String name;
 	private ExecutorService executor;
 	private Logger logger;
 
 	public ClientBase() {
 		super();
 		this.executor = Executors.newFixedThreadPool(1);
 		this.logger = OJIMLogger.getLogger(this.getClass().toString());
 	}
 
 	/*
 	 * MISC
 	 */
 
 	private void loadGameBoard() {
 
 		GameState state = this.getGameState();
 
 		state.getBank().setHotels(this.getNumberOfHotelsLeft());
 		state.getBank().setHouses(this.getNumberOfHousesLeft());
 
 		StationFieldGroup stations = new StationFieldGroup();
 		FieldGroup infrastructures = new FieldGroup(FieldGroup.INFRASTRUCTURE);
 		Map<Integer, StreetFieldGroup> colorGroups = new HashMap<Integer, StreetFieldGroup>(
 				8);
 
 		/* This loop asks the properties for every field on the board. */
 		for (int position = 0; position < GameState.FIELDS_AMOUNT; position++) {
 			Field field = null;
 			String name = this.getEstateName(position);
 			int price = this.getEstatePrice(position);
 			int groupColor = this.getEstateColorGroup(position);
 			// Street
 			if (groupColor >= 0) {
 
 				StreetFieldGroup group = colorGroups.get(groupColor);
 				if (group == null) {
 					int delim = name.indexOf(":");
 					String groupName = "";
 					if (delim > 0) {
 						groupName = name.substring(0, delim);
 					}
 					group = new StreetFieldGroup(groupColor, groupName, this
 							.getEstateHousePrice(position));
					colorGroups.put(groupColor, group);
 				}
 
 				name = name.substring(name.indexOf(":") + 1).trim();
 
 				int[] rentByLevel = new int[Street.MAXMIMUM_BUILT_LEVEL];
 				for (int builtLevel = 0; builtLevel < rentByLevel.length; builtLevel++) {
 					rentByLevel[builtLevel] = this.getEstateRent(position,
 							builtLevel);
 				}
 
 				Street street = new Street(name, position, rentByLevel, this
 						.getEstateHouses(position), price);
 				street.setMortgaged(this.isMortgaged(position));
 
 				field = group.addField(street);
 			} else {
 				switch (this.getEstateColorGroup(position)) {
 				case FieldGroup.GO:
 					field = new GoField(name, position);
 					break;
 				case FieldGroup.JAIL:
 					field = new Jail(name, position, this
 							.getMoneyToPay(position), this
 							.getRoundsToWait(position));
 					break;
 				case FieldGroup.FREE_PARKING:
 					field = new FreeParking(name, position);
 					break;
 				case FieldGroup.GO_TO_JAIL:
 					field = new GoToJail(name, position);
 					break;
 				case FieldGroup.EVENT:
 					field = new CardField(name, position, false);
 					break;
 				case FieldGroup.COMMUNITY:
 					field = new CardField(name, position, true);
 					break;
 				case FieldGroup.STATIONS:
 					field = stations
 							.addField(new Station(name, position, price));
 					break;
 				case FieldGroup.INFRASTRUCTURE:
 					field = infrastructures.addField(new InfrastructureField(
 							name, position, price));
 					break;
 				case FieldGroup.TAX:
 					field = new TaxField(name, position, this.getEstateRent(
 							position, 0));
 					break;
 				default:
 					field = null;
 					break;
 				}
 			}
 
 			if (field == null) {
 				// TODO: show error
 			} else {
 				state.setFieldAt(field, position);
 			}
 		}
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	/*
 	 * ACTION METHODS
 	 */
 
 	protected final boolean connect(String host, int port) {
 		try {
 			StartNetClient starter = new StartNetClient();
 			NetOjim netojim = starter.createClientRMIConnection(port, host, this);
 			ImplNetClient  server = new ImplNetClient(this, netojim);
 			this.setParameters(server, this);
 			this.loadGameBoard();
 		} catch (RemoteException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 
 	protected final void connect(IServer server, GameState state) {
 		this.setParameters(server, this, state);
 		this.loadGameBoard();
 	}
 	
 	protected final void connect(IServer server) {
 		this.connect(server, new GameState());
 	}
 
 	/*
 	 * TRIGGER-METHODS
 	 */
 
 	@Override
 	public String getLanguage() {
 		return "eng";
 	}
 
 	@Override
 	public String getName() {
 		return this.name;
 	}
 
 	@Override
 	public final void informBankruptcy() {
 		this.logger.log(Level.INFO, "informBankruptcy()");
 	//	this.onBankruptcy();
 		this.executor.execute(new OnBankruptcy(this));
 	}
 
 	public abstract void onBankruptcy();
 
 	@Override
 	public final void informCardPull(String text, boolean communityCard) {
 		this.logger.log(Level.INFO, "informCardPull(" + text + ", " + communityCard + ")");
 		Player active = this.getGameState().getActivePlayer();
 		active.setNumberOfGetOutOfJailCards(this
 				.getNumberOfGetOutOfJailCards(active.getId()));
 		//this.onCardPull(text, communityCard);
 		this.executor.execute(new OnCardPull(this, text, communityCard));
 	}
 
 	public abstract void onCardPull(String text, boolean communityCard);
 
 	@Override
 	public final void informCashChange(int playerId, int cashChange) {
 		this.logger.log(Level.INFO, "informCashChange(" + playerId + "," + cashChange + ")");
 		Player player = this.getGameState().getPlayerByID(playerId);
 		if (player != null) {
 			player.transferMoney(cashChange);
 		//	this.onCashChange(player, cashChange);
 			this.executor.execute(new OnCashChange(this, player, cashChange));
 		} else {
 			OJIMLogger.getLogger(this.getClass().toString()).warning(
 					"Get informCashChange with invalid player (" + playerId
 							+ ").");
 		}
 	}
 
 	public abstract void onCashChange(Player player, int cashChange);
 
 	@Override
 	public final void informConstruct(int street) {
 		this.logger.log(Level.INFO, "informConstruct(" + street + ")");		
 		Field field = this.getLogic().getGameState().getFieldAt(street);
 		if (field instanceof Street) {
 			this.getLogic().upgrade((Street) field, +1);
 			//this.onConstruct((Street) field);
 			this.executor.execute(new OnConstruct(this, (Street) field));
 		} else {
 			OJIMLogger.getLogger(this.getClass().toString()).warning(
 					"Get informConstruct with invalid street.");
 		}
 	}
 
 	public abstract void onConstruct(Street street);
 
 	@Override
 	public final void informDestruct(int street) {
 		this.logger.log(Level.INFO, "informDestruct(" + street + ")");		
 		Field field = this.getLogic().getGameState().getFieldAt(street);
 		if (field instanceof Street) {
 			this.getLogic().upgrade((Street) field, -1);
 		//	this.onDestruct((Street) field);
 			this.executor.execute(new OnDestruct(this, (Street) field));
 		} else {
 			OJIMLogger.getLogger(this.getClass().toString()).warning(
 					"Get informDestruct with invalid street.");
 		}
 	}
 
 	public abstract void onDestruct(Street street);
 
 	@Override
 	public final void informDiceValues(int[] diceValues) {
 		this.logger.log(Level.INFO, "informDiceValues(" + Arrays.toString(diceValues) + ")");
 	//	this.onDiceValues(diceValues);
 		this.executor.execute(new OnDiceValues(this, diceValues));
 	}
 
 	public abstract void onDiceValues(int[] diceValues);
 
 	@Override
 	public final void informMessage(String text, int sender,
 			boolean privateMessage) {
 		this.logger.log(Level.INFO, "informMessage(" + text + "," + sender + "," + privateMessage + ")");	
 		Player player = null;
 		if ((sender == -1)
 				|| (player = this.getGameState().getPlayerByID(sender)) != null) {
 			//this.onMessage(text, player, privateMessage);
 			this.executor.execute(new OnMessage(this, text, player,
 					privateMessage));
 		} else {
 			OJIMLogger.getLogger(this.getClass().toString()).warning(
 					"Get informMessage with invalid player (" + sender + ").");
 		}
 	}
 
 	public abstract void onMessage(String text, Player sender,
 			boolean privateMessage);
 
 	@Override
 	public final void informMortgageToogle(int street) {
 		this.logger.log(Level.INFO, "informMortgageToogle(" + street + ")");
 		Field field = this.getLogic().getGameState().getFieldAt(street);
 		if (field instanceof BuyableField) {
 			this.getLogic().toggleMortgage((BuyableField) field);
 			//this.onMortgageToogle((BuyableField) field);
 			this.executor.execute(new OnMortgageToogle(this,
 					(BuyableField) field));
 		} else {
 			OJIMLogger.getLogger(this.getClass().toString()).warning(
 					"Get informMortgageToogle with invalid buyable field.");
 		}
 	}
 
 	public abstract void onMortgageToogle(BuyableField street);
 
 	@Override
 	public final void informStartGame(int[] ids) {
 		this.logger.log(Level.INFO, "informStartGame(" + Arrays.toString(ids) + ")");		
 		GameState state = this.getGameState();
 		Player[] order = new Player[ids.length];
 		for (int i = 0; i < ids.length; i++) {
 			order[i] = state.getPlayerByID(ids[i]);
 		}
 		state.setPlayerOrder(order);
 
 		// Load all owners
 		for (int position = 0; position < GameState.FIELDS_AMOUNT; position++) {
 			Field field = this.getGameState().getFieldAt(position);
 			if (field instanceof BuyableField) {
 				int id = this.getOwner(position);
 				if (id >= 0) {
 					((BuyableField) field).buy(this.getGameState()
 							.getPlayerByID(id));
 				}
 			}
 		}
 		//this.onStartGame(this.getGameState().getPlayers());
 		this.executor.execute(new OnStartGame(this, this.getGameState()
 				.getPlayers()));
 	}
 
 	public abstract void onStartGame(Player[] players);
 
 	@Override
 	public final void informTrade(int actingPlayer, int partnerPlayer) {
 		this.logger.log(Level.INFO, "informTrade(" + actingPlayer + "," + partnerPlayer + ")");
 		Player acting = this.getGameState().getPlayerByID(actingPlayer);
 		if (acting != null) {
 			Player partner = this.getGameState().getPlayerByID(partnerPlayer);
 			if (partner != null) {
 				//this.onTrade(acting, partner);
 				this.executor.execute(new OnTrade(this, acting, partner));
 			} else {
 				OJIMLogger.getLogger(this.getClass().toString()).warning(
 						"Get informTrade with invalid partner player.");
 			}
 		} else {
 			OJIMLogger.getLogger(this.getClass().toString()).warning(
 					"Get informTrade with invalid acting player.");
 		}
 	}
 
 	public abstract void onTrade(Player actingPlayer, Player partnerPlayer);
 
 	@Override
 	public final void informTurn(int player) {
 		this.logger.log(Level.INFO, "informTurn(" + player + ")");
 		Player newPlayer = this.getGameState().getPlayerByID(player);
 		if (newPlayer != null) {
 			this.getGameState().setActivePlayer(newPlayer);
 			//this.onTurn(newPlayer);
 			this.executor.execute(new OnTurn(this, newPlayer));
 		} else {
 			OJIMLogger.getLogger(this.getClass().toString()).warning(
 					"Get informTurn with invalid player.");
 		}
 	}
 
 	public abstract void onTurn(Player player);
 
 	@Override
 	public final void informMove(int playerId, int position) {
 		this.logger.log(Level.INFO, "informMove(" + playerId + ", " + position + ")");
 		Player player = this.getGameState().getPlayerByID(playerId);
 		if (player != null) {
 			player.setPosition(position);
 			if (position < 0 && this.getGameState().getFieldAt(Math.abs(position)) instanceof Jail) {
 				player.sendToJail((Jail) this.getGameState().getFieldAt(Math.abs(position)));
 			} else {
 				player.sendToJail(null);
 			}
 			
 			//this.onMove(player, position);
 			this.executor.execute(new OnMove(this, player, position));
 		} else {
 			OJIMLogger.getLogger(this.getClass().toString()).warning(
 					"Get informMove with invalid player, ID = " + playerId);
 			assert (false);
 		}
 	}
 
 	public abstract void onMove(Player player, int position);
 
 	@Override
 	public final void informBuy(int playerId, int position) {
 		this.logger.log(Level.INFO, "informBuy(" + playerId + ", " + position + ")");
 		Player player = this.getGameState().getPlayerByID(playerId);
 		if (player != null) {
 			Field field = this.getGameState().getFieldAt(position);
 			if (field instanceof BuyableField) {
 				((BuyableField) field).buy(player);
 			//	this.onBuy(player, (BuyableField) field);
 				this.executor.execute(new OnBuy(this, player,
 						(BuyableField) field));
 			} else {
 				OJIMLogger.getLogger(this.getClass().toString()).warning(
 						"Get informBuy with invalid position.");
 			}
 		} else {
 			OJIMLogger.getLogger(this.getClass().toString()).warning(
 					"Get informBuy with invalid player.");
 		}
 	}
 
 	public abstract void onBuy(Player player, BuyableField field);
 
 	@Override
 	public final void informAuction(int auctionState) {
 		this.logger.log(Level.INFO, "informAuction(" + auctionState + ")");
 		//this.onAuction(auctionState);
 		this.executor.execute(new OnAuction(this, auctionState));
 	}
 
 	public abstract void onAuction(int auctionState);
 
 	public final void informNewPlayer(int playerId) {
 		this.logger.log(Level.INFO, "informNewPlayer(" + playerId + ")");
 		Player player = new Player(this.getPlayerName(playerId), this
 				.getPlayerPiecePosition(playerId),
 				this.getPlayerCash(playerId), playerId, this
 						.getPlayerColor(playerId));
 		this.getGameState().setPlayer(player);
 		if (this.getPlayerId() == player.getId()) {
 			this.setMyPlayer(player);
 		}
 		//this.onNewPlayer(player);
 		this.executor.execute(new OnNewPlayer(this, player));
 	}
 
 	public abstract void onNewPlayer(Player player);
 
 	public final void informPlayerLeft(int playerId) {
 		this.logger.log(Level.INFO, "informPlayerLeft(" + playerId + ")");
 		Player old = this.getGameState().getPlayerByID(playerId);
 		this.getGameState().removePlayer(old);
 		//this.onPlayerLeft(old);
 		this.executor.execute(new OnPlayerLeft(this, old));
 	}
 
 	public abstract void onPlayerLeft(Player player);
 
 	@Override
 	public void setPlayerId(int newId) {
 		super.setPlayerId(newId);
 	}
 }
