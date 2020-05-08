 package com.nexus.client;
 
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import com.google.gson.Gson;
 import com.nexus.LogLevel;
 import com.nexus.Packet;
 import com.nexus.playlist.Playlist;
 import com.nexus.playlist.PlaylistItemMetadataUpdateArray;
 import com.nexus.webserver.WebServerStatus;
 
 public abstract class NexusClientPlaylistUser extends NexusClientMusicDatabaseUser {
 
 	public ArrayList<Playlist> FollowingPlaylists = new ArrayList<Playlist>();
 	
 	public NexusClientPlaylistUser(String ClientName, int ClientID, String Token) {
 		super(ClientName, ClientID, Token);
 	}
 
 	public NexusClientPlaylistUser(String ClientName, String Token) {
 		super(ClientName, Token);
 	}
 	
 	public void OnPlaylistUpdate(PlaylistItemMetadataUpdateArray Metadata){
 		synchronized (FollowingPlaylists){
 			for(int i = 0; i < FollowingPlaylists.size(); i++){
 				Playlist pl = FollowingPlaylists.get(i);
 				if(pl.GetID() == Metadata.PLID){
 					this.SendQueue.addToSendQueue("{\"PlaylistChangeData\":" + new Gson().toJson(Metadata) + "}");
 				}
 			}
 		}
 	}
 	
 	public void OnOpenPlaylist(Playlist playlist){
 		
 	}
 	
 	public void OnClosePlaylist(Playlist playlist){
 		
 	}
 	
 	private String OnPlaylistItemMetadataUpdate(PlaylistItemMetadataUpdateArray Updates){
 		Playlist playlist = new Playlist().FromID(Updates.PLID);
 		return playlist.OnTrackMetadataChanged(Updates, this);
 	}
 	
 	public void OnDataReceived(Packet Package) throws Exception{
 		super.OnDataReceived(Package);
 		if(Package.IsNotify) return;
 		if(!Package.Data.split("/")[2].equalsIgnoreCase("Playlist")) return;
		String Command = Package.Data.split("/")[3];
		if(Command.equalsIgnoreCase("UpdateTrackMeta")){
 			PlaylistItemMetadataUpdateArray updates = new Gson().fromJson(URLDecoder.decode(Package.Data.split("/")[4], "UTF-8"), PlaylistItemMetadataUpdateArray.class);
 			Playlist playlist = new Playlist().FromID(updates.PLID);
 			if(!playlist.CanView(this.AuthenticatedUser)){
 				Package.WebServerHandler.sendHeaders(WebServerStatus.OK);
 				Package.WriteOutput("{\"error\":\"You don\'t have permission to view this playlist\"}");
 			}else if(playlist.CanEdit(this.AuthenticatedUser)){
 				String s = OnPlaylistItemMetadataUpdate(updates);
 				Package.WebServerHandler.sendHeaders(WebServerStatus.OK);
 				Package.WriteOutput("{\"error\":\"None\", \"data\":" + s + "}");
 			}else{
 				Package.WebServerHandler.sendHeaders(WebServerStatus.OK);
 				Package.WriteOutput("{\"error\":\"You don\'t have permission to edit this playlist\"}");
 			}
 			/*int PlaylistID = Integer.parseInt(Package.Request.parameters().get("PLID"));
 			PlaylistItemMetadata meta = new Gson().fromJson(URLDecoder.decode(Package.Data.split("/")[4], "UTF-8"), PlaylistItemMetadata.class);
 			Playlist playlist = new Playlist().FromID(PlaylistID);
 			if(!playlist.CanView(this.AuthenticatedUser)){
 				Package.WebServerHandler.sendHeaders(WebServerStatus.OK);
 				Package.WriteOutput("{\"error\":\"You don\'t have permission to view this playlist\"}");
 			}else if(playlist.CanEdit(this.AuthenticatedUser)){
 				playlist.OnTrackMetadataChanged(meta, this);
 				Package.WebServerHandler.sendHeaders(WebServerStatus.OK);
 				Package.WriteOutput("{\"error\":\"None\"}");
 			}else{
 				Package.WebServerHandler.sendHeaders(WebServerStatus.OK);
 				Package.WriteOutput("{\"error\":\"You don\'t have permission to edit this playlist\"}");
 			}*/
		}else if(Command.equalsIgnoreCase("Create")){
			String name = URLDecoder.decode(Package.Request.parameters().get("name"), "UTF-8");
			String readonly = URLDecoder.decode(Package.Request.parameters().get("readonly"), "UTF-8");
			String readwrite = URLDecoder.decode(Package.Request.parameters().get("readwrite"), "UTF-8");
			//String name = URLDecoder.decode(Package.Data.split("/")[4], "UTF-8");
			//String readonly = URLDecoder.decode(Package.Data.split("/")[5], "UTF-8");
			//String readwrite = URLDecoder.decode(Package.Data.split("/")[6], "UTF-8");
 			Playlist plst = Server.PlaylistManager.CreateNewPlaylist(name, this.AuthenticatedUser, readonly, readwrite);
 			Server.ClientManager.Log("User " + this.AuthenticatedUser.Username + " created a new playlist named " + name,LogLevel.INFO);
 			this.OnOpenPlaylist(plst);
 			Package.WebServerHandler.sendHeaders(WebServerStatus.OK);
 			Package.WriteOutput("{\"error\":\"None\", \"Playlist\":" + plst.toString() + "}");
 		}else if(Command.equalsIgnoreCase("GETVIEWABLE")){
 			ArrayList<Playlist> playlists = Server.PlaylistManager.GetViewablePlaylists(this.AuthenticatedUser);
 			String s = new Gson().toJson(playlists);
 			Package.WebServerHandler.sendHeaders(WebServerStatus.OK);
 			Package.WriteOutput("{\"error\":\"None\", \"Playlists\":" + s + "}");
 		}else if(Command.equalsIgnoreCase("OPEN")){
 			int id = Integer.parseInt(Package.Data.split("/")[4]);
 			Playlist p = new Playlist().FromID(id);
 			if(p.CanView(AuthenticatedUser)){
 				synchronized (FollowingPlaylists){
 					FollowingPlaylists.add(p);
 				}
 				this.OnOpenPlaylist(p);
 				Package.WebServerHandler.sendHeaders(WebServerStatus.OK);
 				Package.WriteOutput("{\"error\":\"None\",\"Playlist\":" + p.toString() + ", \"Content\":" + p.GetContent().toJson() + "}");
 			}else{
 				Package.WebServerHandler.sendHeaders(WebServerStatus.OK);
 				Package.WriteOutput("{\"error\":\"You don\'t have permission to view this playlist\"}");
 			}
 		}else if(Command.equalsIgnoreCase("CLOSE")){
 			int id1 = Integer.parseInt(Package.Data.split("/")[4]);
 			Playlist p1 = new Playlist().FromID(id1);
 			boolean found = false;
 			synchronized (FollowingPlaylists){
 				for(int i = 0; i < FollowingPlaylists.size(); i++){
 					Playlist pl = FollowingPlaylists.get(i);
 					if(pl.GetID() == p1.GetID()){
 						FollowingPlaylists.remove(pl);
 						found = true;
 					}
 				}
 			}
 			if(found){
 				this.OnClosePlaylist(p1);
 				Package.WebServerHandler.sendHeaders(WebServerStatus.OK);
 				Package.WriteOutput("{\"error\":\"None\"");
 			}else{
 				Package.WebServerHandler.sendHeaders(WebServerStatus.OK);
 				Package.WriteOutput("{\"error\":\"You aren\'t following this playlist!\"");
 			}
 		}else if(Command.equalsIgnoreCase("CHANGENAME")){
 			Playlist pl = new Playlist().FromID(Integer.parseInt(Package.Data.split("/")[4]));
 			if(pl.GetOwner().toString().equals(this.AuthenticatedUser.toString())){
 				pl.SetName(Package.Data.split("/")[5]);
 				Package.WebServerHandler.sendHeaders(WebServerStatus.OK);
 				Package.WriteOutput("{\"error\":\"None\"}");
 			}else{
 				Package.WebServerHandler.sendHeaders(WebServerStatus.OK);
 				Package.WriteOutput("{\"error\":\"You do not own this playlist, so you cannot change the name\"}");
 			}
 		}else if(Command.equalsIgnoreCase("CHANGEREAD")){
 			Playlist pl1 = new Playlist().FromID(Integer.parseInt(Package.Data.split("/")[4]));
 			if(pl1.GetOwner().toString().equals(this.AuthenticatedUser.toString())){
 				pl1.SetReadonly(Package.Data.split("/")[5]);
 				Package.WebServerHandler.sendHeaders(WebServerStatus.OK);
 				Package.WriteOutput("{\"error\":\"None\", \"Response\":" + pl1.toString() + "}");
 			}else{
 				Package.WebServerHandler.sendHeaders(WebServerStatus.OK);
 				Package.WriteOutput("{\"error\":\"You do not own this playlist, so you cannot change the viewers\"}");
 			}
 		}else if(Command.equalsIgnoreCase("CHANGEWRITE")){
 			Playlist pl2 = new Playlist().FromID(Integer.parseInt(Package.Data.split("/")[4]));
 			if(pl2.GetOwner().toString().equals(this.AuthenticatedUser.toString())){
 				pl2.SetReadWrite(Package.Data.split("/")[5]);
 				Package.WebServerHandler.sendHeaders(WebServerStatus.OK);
 				Package.WriteOutput("{\"error\":\"None\", \"Response\":" + pl2.toString() + "}");
 			}else{
 				Package.WebServerHandler.sendHeaders(WebServerStatus.OK);
 				Package.WriteOutput("{\"error\":\"You do not own this playlist, so you cannot change the editors\"}");
 			}
 		}
 	}
 }
