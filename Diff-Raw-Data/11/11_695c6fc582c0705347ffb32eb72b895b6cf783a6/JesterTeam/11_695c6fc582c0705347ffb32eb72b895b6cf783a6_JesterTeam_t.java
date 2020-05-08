 /*
  * This program is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along with
  * this program. If not, see <http://www.gnu.org/licenses/>
  */
 package com.googlecode.prmf.corleone.game.team;
 
 import com.googlecode.prmf.corleone.game.Player;
 
 public class JesterTeam extends Team {
 	//currently not in use
 	public JesterTeam() {
 		setName("JesterTeam");
 	}
 	
 	@Override
 	public String getName() {
 		return "JesterTeam";
 	}
 	
 	@Override
 	public boolean hasWon(Player[] players) {
		for(Player player : getList())
 		{
 			if(player.getCauseOfDeath().equals(Player.causesOfDeath.LYNCH))
 			{
 				System.err.println("JesterTeam has won the game");
 				return true;
 			}
 		}
 		return false;
 	}
 }
