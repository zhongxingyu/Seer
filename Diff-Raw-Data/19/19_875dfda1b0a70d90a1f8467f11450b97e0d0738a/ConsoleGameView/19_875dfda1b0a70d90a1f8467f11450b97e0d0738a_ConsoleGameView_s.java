 package com.sshipsey.holdem.views.console;
 
 import java.util.Observable;
 
 import com.sshipsey.holdem.models.Game;
 import com.sshipsey.holdem.views.GameView;
 
 public class ConsoleGameView extends BaseConsoleView implements GameView {
 	
 	private Game m_game;
 
 	@Override
 	public void setModel(Observable model) {
 		m_game = (Game) model;
 		m_game.addObserver(this);
 	}
 	
 	@Override
 	public void update(Observable obs, Object arg) {
 		display("Game state updated: ");
 		display("  Current Blinds: %d/%d", m_game.getSmallBlind(), 2 * m_game.getSmallBlind());
 		display("  Current Pot: %d", m_game.getPot());
		display("  Table Cards: %s", m_game.getTableCards());
 		display("  Current Bet: %d", m_game.getBet());
 	}
 }
