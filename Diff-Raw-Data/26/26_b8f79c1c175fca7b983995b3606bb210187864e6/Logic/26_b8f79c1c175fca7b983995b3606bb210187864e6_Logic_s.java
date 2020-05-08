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
 
 package org.ojim.logic;
 
 import org.ojim.client.ClientBase;
 import org.ojim.iface.Rules;
 import org.ojim.logic.rules.GameRules;
 import org.ojim.logic.state.GameState;
 import org.ojim.logic.state.Player;
 import org.ojim.logic.state.ServerPlayer;
 import org.ojim.logic.state.fields.BuyableField;
 import org.ojim.logic.state.fields.Field;
 import org.ojim.logic.state.fields.Jail;
 import org.ojim.logic.state.fields.Street;
 
 public class Logic {
 
 	private GameState state;
 	private GameRules rules;
 
 	public Logic(Rules rules) {
 		/*
 		 * xZise: Unable to call Logic(GameState, GameRules) because GameRules
 		 * needs a GameState which doesn't exists. So I call Logic(GameState,
 		 * Rules).
 		 */
 		this(new GameState(), rules);
 	}
 
 	public Logic(GameState state, Rules rules) {
 		this(state, new GameRules(state, rules));
 	}
 
 	public Logic(GameRules rules) {
 		this(new GameState(), rules);
 	}
 
 	public Logic(GameState state, GameRules rules) {
 		this.state = state;
 		this.rules = rules;
 	}
 
 	public void buyStreet(Player active) {
 		Field field = this.state.getFieldAt(active.getPosition());
 		if (field instanceof BuyableField) {
 			((BuyableField) field).buy(active);
 		}
 	}
 
 	public void buyStreet() {
 		// Get active player
 		Player active = this.state.getActivePlayer();
 		this.buyStreet(active);
 	}
 
 	public void addDiceRoll(int[] diceValues) {
 
 	}
 
 	public void toggleMortgage(BuyableField field) {
 		// TODO: Finished?
 		field.setMortgaged(!field.isMortgaged());
 	}
 
 	public GameState getGameState() {
 		return this.state;
 	}
 	
 	public GameRules getGameRules() {
 		return this.rules;
 	}
 
 	public boolean upgrade(Street street, int level) {
		return street.upgrade(level, this.getGameState().getBank());
 	}
 }
