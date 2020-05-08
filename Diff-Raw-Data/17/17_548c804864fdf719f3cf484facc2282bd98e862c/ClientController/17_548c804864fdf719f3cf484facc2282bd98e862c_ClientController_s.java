 package com.game.rania.controller;
 
 import java.util.HashMap;
 
 import com.game.rania.model.Location;
 import com.game.rania.model.Planet;
 import com.game.rania.model.Player;
 import com.game.rania.model.User;
 import com.game.rania.net.NetController;
 import com.game.rania.userdata.Client;
 
 public class ClientController {
 
 	private Client mClient = null;
 	private NetController nController = null;
 	
 	public ClientController(CommandController commandController){
 		nController = new NetController(commandController);
 	}
 	
 	public boolean login(String login, String password){
 		mClient = nController.ClientLogin(login, password);
		if (mClient.socket.isConnected())
 		{
 			updateLocationList();
 			return true;
 		}
 		return false;
 	}
 	
 	public void disconnect(){
 		nController.ClientDisconnect(mClient);
 		nController.dispose();
 	}
 	
 	public Player getPlayerData(){
 		return nController.getPlayerData(mClient);
 	}
 	
 	public int getServerTime(){
 		return mClient.serverTime;
 	}
 	
 	//send command
 	public void SendTouchPoint(int x, int y, int pX, int pY) {
 		nController.SendTouchPoint(x, y, pX, pY, mClient);
 	}
 
 	//location
 	private HashMap<String, Location> locations = null;
 	private HashMap<String, Planet>   planets   = new HashMap<String, Planet>();
 	private HashMap<String, User> 	  users     = new HashMap<String, User>();
 	
 	public HashMap<String, Location> getLocations(){
 		return locations;
 	}
 	
 	public Location getLocation(int id){
 		return locations.get(String.valueOf(id));
 	}
 	
 	public HashMap<String, Planet> getPlanets(){
 		return planets;
 	}
 	
 	public Planet getPlanet(int id){
 		return planets.get(String.valueOf(id));
 	}
 	
 	public HashMap<String, User> getUsers(){
 		return users;
 	}
 	
 	public User getUser(int id){
 		return users.get(String.valueOf(id));
 	}
 	
 	public void updateLocationList(){
 		locations = nController.GetAllLocations(mClient);
 	}
 	
 	public void updatePlanetList(){
 		planets = nController.GetCurrentPlanets(mClient);
 	}
 	
 	public void updateUsersList(){
 		users = nController.GetUsersInLocation(mClient);
 	}
 	
 	public void updateCurrentLocation(){
 		updatePlanetList();
 		updateUsersList();
 	}
 }
