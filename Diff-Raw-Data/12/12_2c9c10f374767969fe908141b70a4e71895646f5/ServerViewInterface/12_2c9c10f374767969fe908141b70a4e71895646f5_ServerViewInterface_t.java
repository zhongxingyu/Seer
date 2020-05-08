 /*
  * wePoker: Play poker with your friends, wherever you are!
  * Copyright (C) 2012, The AmbientTalk team.
  *
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the
  * Free Software Foundation; either version 2 of the License, or (at your
  * option) any later version.
  *
  * This program is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
  * Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  */
 
 package edu.vub.at.nfcpoker.ui;
 
 import java.util.List;
 
 import android.content.Context;
 import edu.vub.at.nfcpoker.Card;
 import edu.vub.at.nfcpoker.PokerGameState;
 import edu.vub.at.nfcpoker.PlayerState;
 
 public interface ServerViewInterface {
 
 	// Table
 	
 	public void revealCards(final Card[] cards);
 
 	public void resetCards();
 	
 	public void updateGameState(PokerGameState newState);
 
 	public void updatePoolMoney(int chipsPool);
 	
 	public void setPlayerButtons(PlayerState dealer, PlayerState smallBlind, PlayerState bigBlind);
 
 	// Players
 
 	public void addPlayer(PlayerState player);
 
 	public void updatePlayerStatus(PlayerState player);
 	
 	public void removePlayer(PlayerState player);
 
 	// Other
 	
 	public Context getContext();
 
 	public void showWinners(List<PlayerState> remainingPlayers, int chipsPool);
 
 
 	
 }
