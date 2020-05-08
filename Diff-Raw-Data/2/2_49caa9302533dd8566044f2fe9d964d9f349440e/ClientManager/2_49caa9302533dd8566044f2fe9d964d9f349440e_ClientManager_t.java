 package com.nexus.client;
 
 import java.util.ArrayList;
 
 import com.nexus.LogLevel;
 import com.nexus.NexusServer;
 import com.nexus.playlist.PlaylistItemMetadataUpdateArray;
 
 public class ClientManager {
 	
	private int NewClientID = 0;
 	public ArrayList<NexusClient> NexusClients;
 	
 	private NexusServer Server;
 	
 	public ClientManager(NexusServer Server){
 		NexusClients = new ArrayList<NexusClient>();
 		this.Server = Server;
 		this.Log("Up and running", LogLevel.FINE);
 	}
 	
 	public void Log(String Message, LogLevel level){
 		Server.log.write(Message, "ClientManager", level);
 	}
 	
 	public int RegisterNewClient(NexusClient NewClient){
 		NexusClients.add(NewClient.GetClientID(), NewClient);
 		Log("New client with type '" + NewClient.GetClientTypeName() + "' and name '" + NewClient.GetName() + "' connected with id " + NewClient.GetClientID(), LogLevel.FINE);
 		NewClient.OnConnect();
 		return NewClient.GetClientID();
 	}
 	
 	public int GetAvailableClientID(){
 		NewClientID ++;
 		return NewClientID -1;
 	}
 	
 	public NexusClient GetClientByID(int ClientID){
 		return NexusClients.get(ClientID);
 	}
 
 	public NexusClient GetClientFromToken(String token) {
 		for(NexusClient client : NexusClients){
 			if(client.Token.equalsIgnoreCase(token)){
 				return client;
 			}
 		}
 		return null;
 	}
 	
 	public void OnPlaylistUpdate(PlaylistItemMetadataUpdateArray Metadata, NexusClientPlaylistUser sender){
 		for(NexusClient client : NexusClients){
 			if(client instanceof NexusClientPlaylistUser && !client.toString().equals(sender.toString())){
 				((NexusClientPlaylistUser) client).OnPlaylistUpdate(Metadata);
 			}
 		}
 	}
 	
 	public void OnCurrentNowplayingUpdate(int ClientID){
 		for(NexusClient client : NexusClients){
 			if(client instanceof NexusClientRemoteControl){
 				((NexusClientRemoteControl) client).OnCurrentNowplayingUpdate(ClientID);
 			}
 		}
 	}
 	
 	public void OnUpcomingNowplayingUpdate(int ClientID){
 		for(NexusClient client : NexusClients){
 			if(client instanceof NexusClientRemoteControl){
 				((NexusClientRemoteControl) client).OnUpcomingNowplayingUpdate(ClientID);
 			}
 		}
 	}
 	
 	public void OnPreviousNowplayingUpdate(int ClientID){
 		for(NexusClient client : NexusClients){
 			if(client instanceof NexusClientRemoteControl){
 				((NexusClientRemoteControl) client).OnPreviousNowplayingUpdate(ClientID);
 			}
 		}
 	}
 	
 	public void OnPlayerstateChange(int ClientID){
 		for(NexusClient client : NexusClients){
 			if(client instanceof NexusClientRemoteControl){
 				((NexusClientRemoteControl) client).OnPlayerstateChange(ClientID);
 			}
 		}
 	}
 	
 	public void OnTick(){
 		for(NexusClient client : NexusClients){
 			client.OnTick();
 		}
 	}
 	
 }
