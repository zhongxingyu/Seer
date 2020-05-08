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
 	private NetController netController = null;
 	
 	public ClientController(NetController controller){
 		netController = controller; 
 	}
 	
 	public boolean login(String login, String password){
 		mClient = netController.ClientLogin(login, password);
		if (mClient != null && mClient.socket.isConnected() && mClient.isLogin)
 			return true;
 		return false;
 	}
 	
 	public void disconnect(){
 		netController.ClientDisconnect(mClient);
 		netController.dispose();
 	}
 	
 	public Player getPlayerData(){
 		return netController.getPlayerData(mClient);
 	}
 	
 	public int getServerTime(){
 		return mClient.serverTime;
 	}
 	
 	//send command
 	public void SendTouchPoint(int x, int y, int pX, int pY) {
 		netController.SendTouchPoint(x, y, pX, pY, mClient);
 	}
 
 	public HashMap<Integer, Location> getLocationList(){
 		return netController.GetAllLocations(mClient);
 	}
 	
 	public HashMap<Integer, Planet> getPlanetList(int idLocation){
 		return netController.GetPlanets(mClient, idLocation, true);
 	}
 	
 	public void updatePlanetList(int idLocation){
 		netController.GetPlanets(mClient, idLocation, false);
 	}
 	
 	public HashMap<Integer, User> getUsersList(){
 		return netController.GetUsersInLocation(mClient);
 	}
 }
