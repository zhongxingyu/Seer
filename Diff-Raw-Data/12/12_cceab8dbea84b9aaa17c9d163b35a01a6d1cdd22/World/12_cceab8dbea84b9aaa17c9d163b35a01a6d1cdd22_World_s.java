 /**
  * Copyright (c) 2011-2013 Robert Maupin
  * 
  * This software is provided 'as-is', without any express or implied
  * warranty. In no event will the authors be held liable for any damages
  * arising from the use of this software.
  * 
  * Permission is granted to anyone to use this software for any purpose,
  * including commercial applications, and to alter it and redistribute it
  * freely, subject to the following restrictions:
  * 
  *    1. The origin of this software must not be misrepresented; you must not
  *    claim that you wrote the original software. If you use this software
  *    in a product, an acknowledgment in the product documentation would be
  *    appreciated but is not required.
  * 
  *    2. Altered source versions must be plainly marked as such, and must not be
  *    misrepresented as being the original software.
  * 
  *    3. This notice may not be removed or altered from any source
  *    distribution.
  */
 package org.csdgn.fxm.model;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.UUID;
 
 import org.csdgn.fxm.Config;
 import org.csdgn.fxm.net.InputHandler;
 import org.csdgn.fxm.net.ctrl.Game;
 
 /**
  * Tracks game data.
  * @author Chase
  */
 public class World {
 	public static World instance = new World();
	public InputHandler gameHandler = new Game();
	public HashMap<UUID, Character> charUUID = new HashMap<UUID, Character>();
	public ArrayList<Area> areas = new ArrayList<Area>();
 	
 	public Character getCharacter(UUID uuid) {
 		return charUUID.get(uuid);
 	}
 	
 	public void join(Character chara) {
 		charUUID.put(chara.uuid, chara);
 	}
 	
 	public void leave(Character chara) {
 		charUUID.remove(chara.uuid);
 	}
 	
 	public Room getRoomByUUID(UUID uuid) {
 		for(Area a : areas) {
 			Room r = a.getRoomByUUID(uuid);
 			if(r != null)
 				return r;
 		}
 		return null;
 	}
 	
 	/**
 	 * Loads all rooms from file.
 	 * @throws IOException If there is a read error
 	 */
 	public void loadWorld() throws IOException {
 		for(File f : Area.getAreaList()) {
 			areas.add(Area.load(f));
 		}
 		getStartRoom();
 	}
 	
 	/**
 	 * Places the character in the room referenced in its roomUUID.
 	 */
 	public void placeCharacterInRoom(Character chara) {
 		Room r = getRoomByUUID(chara.roomUUID);
 		if(r == null)
 			r = getStartRoom();
 		chara.setRoom(r);
 	}
 	
 	//Temporary
 	private Room startRoom = null;
 	
 	public Room getStartRoom() {
 		if(startRoom == null) {
 			startRoom = getRoomByUUID(Config.START_ROOM_UUID);
 			if(startRoom == null) {
 				throw new RuntimeException("Cannot find start room!");
 			}
 		}
 		return startRoom;
 	}
 }
