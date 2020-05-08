 package com.nexus.client;
 
import com.nexus.Packet;
import com.nexus.PlaylistManager;
import com.nexus.webserver.WebServerStatus;
 
public abstract class NexusClientPlaylistUser extends NexusClient {

	public NexusClientPlaylistUser(String ClientName, String Token) {
		super(ClientName, Token);
	}

	public NexusClientPlaylistUser(String ClientName, int ClientID, String Token) {
		super(ClientName, ClientID, Token);
	}

	public void OnDataReceived(Packet Package) {
		super.OnDataReceived(Package);
		try{
			String[] Data = Package.Data.split("/");
			if(Data[2].equalsIgnoreCase("Playlist")){
				if(Data[3].equalsIgnoreCase("Create")){
					PlaylistManager.CreatePlaylist(Data[4], Package.SenderClient.AuthenticatedUser);
				}
			}
		}catch(Exception e){}
	}
}
