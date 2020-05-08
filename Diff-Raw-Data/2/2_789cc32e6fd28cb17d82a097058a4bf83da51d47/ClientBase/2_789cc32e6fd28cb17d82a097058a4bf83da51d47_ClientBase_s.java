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
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.ojim.iface.IClient;
 import org.ojim.logic.Logic;
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
 import org.ojim.network.ClientConnection;
 
 import edu.kit.iti.pse.iface.IServer;
 
 /**
  * Basis Client für den GUIClient und AIClient.
  * 
  * @author Fabian Neundorf
  */
 public class ClientBase extends SimpleClient implements IClient {
 
 	private ClientConnection connection;
 
 	private String name;
 
 	public ClientBase() {
 		super();
 		this.connection = new ClientConnection();
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
 					group = new StreetFieldGroup(groupColor, this.getEstateHousePrice(position));
 				}
 
 				int[] rentByLevel = new int[Street.MAXMIMUM_BUILT_LEVEL];
 				for (int builtLevel = 0; builtLevel < rentByLevel.length; builtLevel++) {
 					rentByLevel[builtLevel] = this.getEstateRent(position,
 							builtLevel);
 				}
 
 				Street street = new Street(name, position, rentByLevel,
 						this.getEstateHouses(position), price);
 				street.setMortgaged(this.isMortgaged(position));
 
 				field = group.addField(street);
 			} else {
 				switch (this.getEstateColorGroup(position)) {
 				case FieldGroup.GO:
 					field = new GoField(name, position);
 					break;
 				case FieldGroup.JAIL:
 					field = new Jail(name, position, this.getMoneyToPay(position),
 							this.getRoundsToWait(position));
 					break;
 				case FieldGroup.FREE_PARKING:
 					field = new FreeParking(name, position);
 					break;
 				case FieldGroup.GO_TO_JAIL:
 					field = new GoToJail(name, position);
 					break;
 				case FieldGroup.EVENT:
 					field = new CardField(name, position);
 					break;
 				case FieldGroup.COMMUNITY:
 					field = new CardField(name, position);
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
 				default :
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
 
 	protected final boolean connect(String host, String name) {
 		IServer server = this.connection.connect(host, name);
 		if (server == null) {
 			return false;
 		}
 		this.setParameters(new Logic(server.getRules()),
 				server.addPlayer(this), server);
 		this.loadGameBoard();
 		return true;
 	}
 
 	protected final void connect(IServer server) {
 		this.setParameters(new Logic(server.getRules()),
 				server.addPlayer(this), server);
 		this.loadGameBoard();
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
 	public void informBankruptcy() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void informCardPull(String text, boolean communityCard) {
 		Player active = this.getGameState().getActivePlayer();
 		active.setNumberOfGetOutOfJailCards(this.getNumberOfGetOutOfJailCards(active.getId()));
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void informCashChange(int player, int cashChange) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void informConstruct(int street) {
 		Field field = this.getLogic().getGameState().getFieldAt(street);
 		if (field instanceof Street) {
 			this.getLogic().upgrade((Street) field, +1);
 		}
 	}
 
 	@Override
 	public void informDestruct(int street) {
 		Field field = this.getLogic().getGameState().getFieldAt(street);
 		if (field instanceof Street) {
 			this.getLogic().upgrade((Street) field, -1);
 		}
 	}
 
 	@Override
 	public void informDiceValues(int[] diceValues) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void informMessage(String text, int sender, boolean privateMessage) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void informMortgageToogle(int street) {
 		Field field = this.getLogic().getGameState().getFieldAt(street);
 		if (field instanceof BuyableField) {
 			this.getLogic().toggleMortgage((BuyableField) field);
 		}
 	}
 
 	@Override
 	public void informStartGame(int[] ids) {
 		for (int id : ids) {
 			Player player = new Player(this.getPlayerName(id),
 					this.getPlayerPiecePosition(id), this.getPlayerCash(id),
 					id, this.getPlayerColor(id));
 			this.getGameState().setPlayer(id, player);
 		}
 
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
 	}
 
 	@Override
 	public void informTrade(int actingPlayer, int partnerPlayer) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void informTurn(int player) {
 
 		// TODO Auto-generated method stub
 
 		// GameState bescheid sagen, wer jetzt dran ist
 	}
 
 	@Override
	public void informMove(int position, int playerId) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void informBuy(int player, int position) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void informAuction(int auctionState) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
