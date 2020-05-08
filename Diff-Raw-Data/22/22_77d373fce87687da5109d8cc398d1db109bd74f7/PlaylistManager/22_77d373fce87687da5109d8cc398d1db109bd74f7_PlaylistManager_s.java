 package com.nexus.playlist;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.util.ArrayList;
import java.util.List;

 import com.nexus.User;
import com.nexus.client.NexusClient;
import com.nexus.webserver.WebServerStatus;
 
 public class PlaylistManager {

	private static int NewPlaylistID = 0;
 	
 	public PlaylistManager(){
 		
 	}
 	
 	public Playlist CreateNewPlaylist(String Name, User Owner, String ReadonlyUsers, String ReadWriteUsers){
 		Playlist pl = new Playlist(Name, Owner);
 		Connection conn = null;
 		try{
 		    conn = DriverManager.getConnection("jdbc:mysql://localhost/nexus_test?" + "user=root&password=laserint");
 		    Statement stmt = conn.createStatement();
 		    stmt.executeUpdate("INSERT INTO playlists(Name, Owner, Content, ReadonlyUsers, ReadWriteUsers) VALUES(\'" + Name + "\',\'" + Owner.Username + "\',\'{\"Tracks\":[]}\',\'" + ReadonlyUsers + "\',\'" + ReadWriteUsers + "\')");
 		    ResultSet rs = stmt.executeQuery("SELECT ID FROM playlists WHERE Name=\'" + Name + "\' AND Owner=\'" + Owner.Username + "\'");
 		    rs.first();
 		    pl.FromID(rs.getInt("ID"));
 		}catch(Exception e){e.printStackTrace();}
 		return pl;
 	}
 	
 	public ArrayList<Playlist> GetAllPlaylists(){
 		ArrayList<Playlist> resp = new ArrayList<Playlist>();
 		Connection conn = null;
 		try{
 		    conn = DriverManager.getConnection("jdbc:mysql://localhost/nexus_test?" + "user=root&password=laserint");
 		    Statement stmt = conn.createStatement();
 		    ResultSet rs = stmt.executeQuery("SELECT ID FROM playlists");
 			while(!rs.isLast()){
 				rs.next();
 				resp.add(new Playlist().FromID(rs.getInt("ID")));
 			}
 		}catch(Exception e){}
 		return resp;
 	}
 	
 	public ArrayList<Playlist> GetViewablePlaylists(User u){
 		ArrayList<Playlist> resp = new ArrayList<Playlist>();
 		for(Playlist pl: GetAllPlaylists()){
 			if(pl.CanView(u)){
 				resp.add(pl);
 			}
 		}
 		return resp;
 	}
 	
 	public ArrayList<Playlist> GetEditablePlaylists(User u){
 		ArrayList<Playlist> resp = new ArrayList<Playlist>();
 		for(Playlist pl: GetAllPlaylists()){
 			if(pl.CanEdit(u)){
 				resp.add(pl);
 			}
 		}
 		return resp;
 	}
 }
