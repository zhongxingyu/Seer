 /**
  * This file is part of Distro Wars (Client).
  * 
  *  Distro Wars (Client) is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *  Distro Wars (Client) is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  * 
  *  You should have received a copy of the GNU General Public License
  *  along with Distro Wars (Client).  If not, see <http://www.gnu.org/licenses/>.
 */
 package net.k3rnel.arena.client.network;
 
 import java.awt.EventQueue;
 import java.io.IOException;
 
 import net.k3rnel.arena.client.GameClient;
 import net.k3rnel.arena.client.network.NetworkProtocols.LoginData;
 import net.k3rnel.arena.client.network.NetworkProtocols.RegistrationData;
 
 import com.esotericsoftware.kryonet.Client;
 import com.esotericsoftware.kryonet.Connection;
 import com.esotericsoftware.kryonet.Listener;
 
 /**
  * Handles packets received from players over TCP
  * @author Nushio
  *
  */
 public class TCPManager {
 		
 	private String host;
 	private Client client;
 	private GameClient m_game;
 	
 	/**
 	 * Constructor
 	 * @param login
 	 * @param logout
 	 */
 	public TCPManager(String server,GameClient game)  throws IOException {
         this.m_game = game;
 		this.client = new Client();
 		this.host = server;
 		client.start();
 		NetworkProtocols.register(client);
 				
 		//This allows us to get incoming classes and all. 
 		client.addListener(new Listener(){
 			public void connected (Connection connection) {
 			}
 			public void received (Connection connection, Object object) {
 				if(object instanceof LoginData){
 					LoginData data = (LoginData)object;
 					switch(data.state) {
 					case 0:
						GameClient.messageDialog("Registration Successful.", GameClient.getInstance().getDisplay());
 						break;
 					case 1:
 						GameClient.messageDialog("Error: Player Limit Reached.", GameClient.getInstance().getDisplay());
 						break;
 					case 2:
 						GameClient.messageDialog("Database Error: Database cannot be reached.", GameClient.getInstance().getDisplay());
 						break;
 					case 3:
 						GameClient.messageDialog("Error: Invalid Username/Password", GameClient.getInstance().getDisplay());
 						break;
 					default:
 						GameClient.messageDialog("Error code: "+data.state+". What a Terrible Failure.", GameClient.getInstance().getDisplay());
 						break;
 					}
 					System.out.println(data.state);
 					m_game.getLoginScreen().setVisible(false);
 					m_game.getLoadingScreen().setVisible(false);
 					m_game.setPlayerId(0);//TODO: Save player id
 					m_game.getUi().setVisible(true);
 					m_game.getUi().getChat().setVisible(true);
 	                m_game.getTimeService().setTime(data.hours,data.minutes);
 				} else if(object instanceof RegistrationData){
 					RegistrationData data = (RegistrationData)object;
 					System.out.println(data.state);
 					switch(data.state) {
 					case 0:
 						GameClient.messageDialog("Registration Successful.", GameClient.getInstance().getDisplay());
 						break;
 					case 1:
 						GameClient.messageDialog("Error: Username exists or is forbidden.", GameClient.getInstance().getDisplay());
 						break;
 					case 2:
 						GameClient.messageDialog("Error: Email already in use.", GameClient.getInstance().getDisplay());
 						break;
 					case 3:
 						GameClient.messageDialog("Error: Email is too long.", GameClient.getInstance().getDisplay());
 						break;
 					case 4:
 						GameClient.messageDialog("Error: Database cannot be reached.", GameClient.getInstance().getDisplay());
 						break;
 					default:
 						GameClient.messageDialog("Error code: "+data.state+". What a Terrible Failure.", GameClient.getInstance().getDisplay());
 						break;
 					}
 					m_game.getLoadingScreen().setVisible(false);
 					m_game.getLoginScreen().showLogin();
 				}
 			}
 
 			public void disconnected (Connection connection) {
 				EventQueue.invokeLater(new Runnable() {
 					public void run () {
 					}
 				});
 			}
 			
 		});
 		client.connect(5000, host, NetworkProtocols.tcp_port);
 	}
 	// This holds per connection state.
 	static class PlayerConnection extends Connection {
 		public String name; //Default is IP
 	}
 	public Client getClient(){
 		return client;
 	}
 }
