 package jmt.adams.edu;
 
 import java.net.*;
 import java.io.*;
 
 public class Player extends Character {
 
 	private Socket s;
 	
 	public Player(String name, int id, Room location, Socket s) {
 		super(name, id, location);
 		
 		this.s = s;
 	}
 	
 	public void setSocket(Socket s) {
 		this.s = s;
 	}
 	
 	public Socket getSocket() {
 		return s;
 	}
 	
 	public void move(int dir) {
 		try {
 			if(location.getExit(dir) != null) {
 				location.leave(this, dir);
 				location = location.getExit(dir);
				location.look(this);
 				location.arrive(this, Direction.reverse[dir]);
 			}
 			else {
 				Telnet.writeLine(s, "There is no exit in that direction");
 			}
 		}
 		catch (IOException e) {
 			System.out.println("Client error: " + e);
 		}
 	}
 	
 	public void look() {
 		location.look(this);
 	}
 }
