 /*
 	Mafiamanager - a tool to support the referee of the parlor game "Mafia"
     Copyright (C) 2011  Thomas Högner
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 import java.util.ArrayList;
 
 import javax.swing.JFrame;
 
 
 public class CharScharping {
 
 	// general
 	public String type;
 	public String name;
 	public ArrayList<Player> player;
 	public int size;
 	
 	
 	public CharScharping(int _size, String _name){
 		
 		// handover
 		size = _size;
 		name = _name;
 		
 		// initialize
 		type = "scharping";
 		player = new ArrayList<Player>();
 	}
 	
 	public void night(
 			// handover
 			JFrame _parentframe){
 		
 		// ACTIONS
 		// first night
 		if (Keys.round == 1){
			Keys.bufferCommand.add(Messages.getString("night.scharping.awake") + " " + name);
			
 			DialogSet dialog = new DialogSet(
 					Keys.playerlist,
 					_parentframe,
 					size,
 					Keys.bufferHead,
 					Keys.bufferCommand,
 					Messages.getString("night.scharping.who"),
 					"onlyunknown");
 			Keys.bufferCommand.clear();
 			Keys.bufferNote.clear();
 			
 			ArrayList<String> scharpings = dialog.getPlayer();
 			for (String scharping : scharpings){
 				Keys.playerlist.get(scharping).scharping = this;
 				player.add(Keys.playerlist.get(scharping));
 			}
 			
 			// set buffer
 			Keys.bufferCommand.add(Messages.getString("night.scharping.sleep"));
 		}
 	}
 	
	// returns avtive scharpings
	public ArrayList<Player> actives(){
		ArrayList<Player> retActives = new ArrayList<Player>();
		
		for (Player curPlayer : player){
			if (curPlayer.dieround != 0){ retActives.add(curPlayer); }
		}
		
		return retActives;
	}
	
 	// returns how much players are alive
 	public int playeralive(){
 		int alive = 0;
 		
 		for (Player p : player){
 			if (p.alive) { alive++; }
 		}
 		
 		return alive;
 	}
 }
