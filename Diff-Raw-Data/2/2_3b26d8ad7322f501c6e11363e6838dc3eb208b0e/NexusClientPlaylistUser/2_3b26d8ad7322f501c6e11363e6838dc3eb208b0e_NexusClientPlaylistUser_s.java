 package com.nexus.client;
 
 import java.util.ArrayList;
 
 import com.nexus.NexusServer;
 import com.nexus.event.EventListener;
 import com.nexus.event.events.PlaylistEvent;
 import com.nexus.main.HTTPPacket;
 import com.nexus.playlist.Playlist;
 import com.nexus.playlist.PlaylistMetadataUpdateArray;
 import com.nexus.utils.JSONPacket;
 import com.nexus.utils.Utils;
 import com.nexus.webserver.WebServerStatus;
 
 public abstract class NexusClientPlaylistUser extends NexusClientAssetUser{
 	
 	public ArrayList<Integer> FollowingPlaylistIDs = new ArrayList<Integer>();
 	
 	@EventListener
 	public void OnUpdatePlaylist(PlaylistEvent.Update event){
 		System.out.println("UPDATE");
 		if(this.FollowingPlaylistIDs.contains(event.Updates.PLID)){
 			JSONPacket Packet = new JSONPacket();
 			Packet.put("SynchronisationUpdates", event.Updates);
 			this.SendQueue.addToSendQueue(Packet);
 		}
 	}
 	
 	@Override
 	public void OnDataReceived(HTTPPacket Packet) throws Exception{
 		super.OnDataReceived(Packet);
 		if(Packet.IsNotify) return;
 		if(!Packet.Data.split("/")[2].equalsIgnoreCase("Playlist")) return;
 		String Command = Packet.Data.split("/")[3];
 		if(Command.equalsIgnoreCase("Open")){
 			int PLID = Integer.parseInt(Packet.Request.GetParameter("id"));
 			Playlist p = Playlist.FromID(PLID);
 			if(p == null){
 				Packet.Response.SendHeaders(WebServerStatus.BadRequest);
 				Packet.Response.SendError("Unknown playlist!");
 				Packet.Response.Close();
 				return;
 			}
 			if(!NexusServer.EventBus.post(new PlaylistEvent.Open(p, this))){
 				this.FollowingPlaylistIDs.add(p.getID());
 				JSONPacket packet = new JSONPacket();
 				packet.addErrorPayload("none");
 				packet.put("Playlist", p.toJsonMap());
 				packet.put("Content", p.GetContentAsJSON());
 				Packet.Response.SendHeaders(WebServerStatus.OK);
 				Packet.Response.SendData(packet);
 				Packet.Response.Close();
 			}else{
 				Packet.Response.SendHeaders(WebServerStatus.OK);
 				Packet.Response.SendError("Canceled");
 				Packet.Response.Close();
 			}
 		}else if(Command.equalsIgnoreCase("Close")){
 			int PLID = Integer.parseInt(Packet.Request.GetParameter("id"));
 			Playlist p = Playlist.FromID(PLID);
 			if(p == null){
 				Packet.Response.SendHeaders(WebServerStatus.BadRequest);
 				Packet.Response.SendError("Unknown playlist!");
 				Packet.Response.Close();
 				return;
 			}
 			if(this.FollowingPlaylistIDs.contains(p.getID())){
 				NexusServer.EventBus.post(new PlaylistEvent.Close(p, this));
 				Packet.Response.SendHeaders(WebServerStatus.OK);
 				Packet.Response.SendError("none");
 				Packet.Response.Close();
				this.FollowingPlaylistIDs.remove(p.getID());
 			}else{
 				Packet.Response.SendHeaders(WebServerStatus.BadRequest);
 				Packet.Response.SendError("Not following that playlist");
 				Packet.Response.Close();
 			}
 		}else if(Command.equalsIgnoreCase("GetViewable")){
 			ArrayList<Playlist> playlists = this.Server.PlaylistManager.GetViewablePlaylists(this.AuthenticatedUser);
 			JSONPacket p = new JSONPacket();
 			p.addErrorPayload("none");
 			p.put("Playlists", playlists);
 			Packet.Response.SendHeaders(WebServerStatus.OK);
 			Packet.Response.SendData(p);
 			Packet.Response.Close();
 		}else if(Command.equalsIgnoreCase("Synchronisation")){
 			Command = Packet.Data.split("/")[4];
 			if(Command.equalsIgnoreCase("Update")){
 				String data = Utils.URLDecode(Packet.Request.GetParameter("data"));
 				PlaylistMetadataUpdateArray Updates = Utils.Gson.fromJson(data, PlaylistMetadataUpdateArray.class);
 				if(NexusServer.Instance.PlaylistManager.HandleMetadataChange(Updates)){
 					JSONPacket p = new JSONPacket();
 					p.addErrorPayload("none");
 					p.put("Canceled", false);
 					Packet.Response.SendHeaders(WebServerStatus.OK);
 					Packet.Response.SendData(p);
 					Packet.Response.Close();
 				}else{
 					JSONPacket p = new JSONPacket();
 					p.addErrorPayload("none");
 					p.put("Canceled", true);
 					Packet.Response.SendHeaders(WebServerStatus.OK);
 					Packet.Response.SendData(p);
 					Packet.Response.Close();
 				}
 			}
 		}
 	}
 }
