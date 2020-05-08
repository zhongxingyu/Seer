 /*	
  * Copyright 2009 Mex (ellism88@gmail.com)
  * 
  *   This program is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   This program is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package Kill;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 /**
  * Object used to save into db40 databse. Conntains a list of kill message for a
  * user.
  * 
  * @author Mex 2009
  * 
  */
 public class KillLists {
 	private String owner;
 	private List<String> kills;
 	private static Random rng = new Random();
 	private static final String DEFAULT_KILL = "stabs %NAME with a elongated frozen eel";
 
 	/**
 	 * Default constuctor for a kill list. Creates a kill list with no kill
 	 * messages.
 	 * 
 	 * @param owner
 	 *            The owner of the list.
 	 */
 	public KillLists(String owner) {
 		this.owner = owner;
 		kills = new ArrayList<String>();
 	}
 
 	/**
 	 * Get the Owner of this list.
 	 * 
 	 * @return String of nick of the owner
 	 */
 	public String getOwner() {
 		return owner;
 	}
 
 	/**
 	 * Set the owner of this list. This should probably not be used.
 	 * 
 	 * @param owner The new owner of the list.
 	 */
 	public void setOwner(String owner) {
 		this.owner = owner;
 	}
 
 	public List<String> getKills() {
		return kills;
 	}
 
 	public void setKills(List<String> kills) {
 		this.kills = kills;
 	}
 	
 	public String getRandomKill() {
 		if (kills.size() > 0) {
 			int random = rng.nextInt(kills.size());
 			return kills.get(random);
 		} else {
 			return DEFAULT_KILL;
 		}
 	}
 
 }
