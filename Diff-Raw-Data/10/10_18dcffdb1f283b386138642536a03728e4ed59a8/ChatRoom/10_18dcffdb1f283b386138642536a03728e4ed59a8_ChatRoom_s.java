 //
 //  ChatRoom.java
 //  ChatServer
 //
 //  Created by John McKisson on 3/31/08.
 //  Copyright 2008 __MyCompanyName__. All rights reserved.
 //
 package com.presence.chat;
 
 import java.util.Iterator;
 import java.util.Vector;
 import java.util.logging.*;
 
 import com.presence.chat.*;
 import com.webobjects.foundation.*;
 
 import static com.presence.chat.ANSIColor.*;
 
 public class ChatRoom {
 	String name;
 	String password;
 	boolean destroyable;
 	boolean silent;
 			
 	Vector<ChatClient> people;
 	Vector<ChatClient> listeners;
 	
 	ChatLog roomLog;
 
 	public ChatRoom(String roomName) {
 		name = roomName;
 		password = null;
 		
 		destroyable = true;
 		silent = false;
 		
 		people = new Vector<ChatClient>();
 		listeners = new Vector<ChatClient>();
 		
 		roomLog = new ChatLog(roomName);
 		
 		if (!name.equals("main"))
 			ChatServer.getRoom("main").echo(String.format("%s[%s%s%s] Room [%s] has been created",
 				RED, WHT, ChatPrefs.getName(), RED, name), null);
 				
 		ChatServer.getStats().rooms++;
 		
 		//RoomCreate hook
 		NSNotificationCenter.defaultCenter().postNotification("RoomCreate", getName());
 	}
 	
 	public Vector<ChatClient> getListeners() {
 		return listeners;
 	}
 	
 	public boolean isDestroyable() {
 		return destroyable;
 	}
 	
 	public void setDestroyable(boolean val) {
 		destroyable = val;
 	}
 	
 	public boolean isSilent() {
 		return silent;
 	}
 	
 	public void setSilent(boolean val) {
 		silent = val;
 	}
 	
 	public ChatLog getLog() {
 		return roomLog;
 	}
 	
 	public String getName() {
 		return name;
 	}
 	
 	public String getPassword() {
 		return password;
 	}
 	
 	public void setPassword(String str) {
 		password = str;
 	}
 	
 	public Vector<ChatClient> getPeople() {
 		return people;
 	}
 	
 	
 	/**
 	 * Adds a person to a room.  This also sends a notification to the room that the person has joined,
 	 * and a notification to the person letting them know they have joined the room.
 	 * @param person Person joining the room
 	 */
 	public void addPerson(ChatClient person, boolean echo) {
 	
 		if (this.equals(person.getRoom())) {
 			//Person is already in the room!
 			Logger.getLogger("global").warning(String.format("addPerson: %s is already in room [%s]!\n", person.getName(), name));
 			return;
 		}
 		
 		//Remove them from listen list if they were listening to this channel
 		if (listeners.contains(person))
 			listeners.remove(person);
 		
 		//Notify the room that a new person has joined
 		if (echo)
 			echo(String.format("%s[%s%s%s] %s%s%s has joined the room", RED, WHT, ChatPrefs.getName(), RED, WHT, person.getName(), RED), null);
 		
 		//Notify person
 		person.sendChat(String.format("%s[%s%s%s] You have joined room [%s%s%s]", RED, WHT, ChatPrefs.getName(), RED, WHT, name, RED));
 		
 		Logger.getLogger("global").info(String.format("%s joined room %s", person.getName(), name));
 			
 		//Add dude to this room
 		people.add(person);
 		
 		//RoomJoin hook
 		NSNotificationCenter.defaultCenter().postNotification("RoomJoin", person.getName() + ":" + getName());
 	}
 	
 	
 	/*
 	 * Removes person from this room and sets their room to null
 	 */
 	public void removePerson(ChatClient person) {
 	
 		ChatRoom oldRoom = person.getRoom();
 
 		if (oldRoom == null) {
 			Logger.getLogger("global").warning(String.format("ChatRoom[%s]:: removePerson: %s is not in the room!\n" , name, person.getName()));
 			return;
 		}
 		
 		people.remove(person);
 		
 		//RoomLeave hook
 		NSNotificationCenter.defaultCenter().postNotification("RoomLeave", person.getName() + ":" + getName());
 		
 		//This needs to act on the users previous room
 		if (destroyable && people.size() == 0) {
 			//Notify people in main room that this room has been destroyed
 			
 			ChatServer.getRoom("main").echo(String.format("%s[%s%s%s] Room [%s] has been destroyed", RED, WHT, ChatPrefs.getName(), RED, name), null);
 			
 			ChatServer.getRooms().remove(name);
 			listeners.clear();
 			
 			//RoomDestroy hook
 			NSNotificationCenter.defaultCenter().postNotification("RoomDestroy", getName());
 		}
 		
 		
 		//Notify the room that a person has left
 		echo(String.format("%s[%s%s%s] %s%s%s has left the room", RED, WHT, ChatPrefs.getName(), RED, WHT, person.getName(), RED), null);
 		
 		//System.out.printf("ChatRoom[%s]:: addPerson: %s leaves!\n", name, person.getName());
 	}
 	
 	public void addListener(ChatClient person, boolean echo) {
 		if (this.equals(person.getRoom())) {
 			//Person is already in the room!
 			Logger.getLogger("global").warning(String.format("addListener: %s is already in room [%s]!\n", person.getName(), name));
 			return;
 		}
 		
 		//Notify the room that a new person is listening
 		if (echo)
 			echo(String.format("%s[%s%s%s] %s%s%s is now listening to this room", RED, WHT, ChatPrefs.getName(), RED, WHT, person.getName(), RED), null);
 		
 		//Notify person
 		person.sendChat(String.format("%s[%s%s%s] You are now listening to room [%s%s%s]", RED, WHT, ChatPrefs.getName(), RED, WHT, name, RED));
 		
 		Logger.getLogger("global").info(String.format("%s listens to room %s", person.getName(), name));
 			
 		//Add dude to this room
 		listeners.add(person);
 	}
 	
 	public void removeListener(ChatClient person, boolean echo) {
 		ChatRoom oldRoom = person.getRoom();
 
 		if (oldRoom == null) {
 			Logger.getLogger("global").warning(String.format("ChatRoom[%s]:: removeListener: %s is not listening to the room!\n" , name, person.getName()));
 			return;
 		}
 		
 		//Notify person
 		person.sendChat(String.format("%s[%s%s%s] You are no longer listening to room [%s%s%s]", RED, WHT, ChatPrefs.getName(), RED, WHT, name, RED));
 		
 		listeners.remove(person);
 		
 		//Notify the room that a person has left
 		if (echo)
 			echo(String.format("%s[%s%s%s] %s%s%s stops listening to this room", RED, WHT, ChatPrefs.getName(), RED, WHT, person.getName(), RED), null);
 	}
 	
 	
 	/**
 	 * Echo a message to everyone in the room
 	 * @param msg Message
 	 */
 	public void echo(String msg, ChatClient from) {
 		echo(msg, from, null);
 	}
 	
 	/**
 	 * Echo a message to everyone in the room, except excluded client
 	 * @param msg Message
 	 * @param exclude Client to be excluded from the echo list
 	 */
 	public void echo(String msg, ChatClient from, ChatClient exclude) {
 	
 		//Check silent status, if room is silent only people level 3 and above may speak
 		if (silent && from.getAccount().getLevel() < 3) {
 			from.sendChat("This room has been silenced!");
 			return;
 		}
 	
 		roomLog.addEntry(msg);
 		
 		ChatAccount ac = (from != null ? from.getAccount() : null);
 		String accountName = (ac != null ? ac.getName() : ChatPrefs.getName());
 		
 		String hookMsg = accountName + ":" + name + ":" + msg.replaceAll("\u001b\\[[0-9;]+m", "");
 		
 		//Probably need to add carriage returns back in
		//msg = "\n" + msg;
 		
 		Iterator<ChatClient> it = people.iterator();
 		
 		while (it.hasNext()) {
 			ChatClient client = it.next();
 			
 			if (exclude == client)
 				continue;
 				
 			//If the client has the sending account in their gag list, dont send them anything
 			if (ac != null && client.getAccount().hasGagged(accountName))
 				continue;
 			
 			client.sendChatAll(msg);
 		}
 		
 		it = listeners.iterator();
 		
 		while (it.hasNext()) {
 			ChatClient client = it.next();
 			
 			if (exclude == client)
 				continue;
 				
 			//If the client has the sending account in their gag list, dont send them anything
 			if (ac != null && client.getAccount().hasGagged(ac.getName()))
 				continue;
 			
 			client.sendChatAll(msg);
 		}
 	
 		//ChatAll hook
 		NSNotificationCenter.defaultCenter().postNotification("ChatAll", hookMsg);
 	}
 	
 	
 	public boolean equals(ChatRoom room) {
 		if (room == null)
 			return false;
 			
 		return name.toLowerCase().equals(room.getName().toLowerCase());
 	}
 }
