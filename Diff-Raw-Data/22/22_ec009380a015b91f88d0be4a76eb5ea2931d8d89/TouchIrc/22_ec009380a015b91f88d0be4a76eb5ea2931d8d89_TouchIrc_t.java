 package org.touchirc;
 
 import org.touchirc.db.Database;
 import org.touchirc.model.Profile;
 import org.touchirc.model.Server;
 
 import android.content.Context;
 import android.util.SparseArray;
 
 public class TouchIrc{
 	
 	private static TouchIrc instance;
 	
 	private SparseArray<Server> availableServers;
 	private SparseArray<Profile> availableProfiles;
 	
 	private int idDefaultProfile;
 	private boolean loaded;
 	
 	public TouchIrc(){
 		idDefaultProfile = -1; // set to null
 		loaded = false;
 	}
 	
 	public void load(Context c) {
 		// always load Profiles before Servers !
 		if(!loaded){
 			loadProfiles(c);
 			loadServers(c);
 			loaded = true;
 		}
 	}
 
 	public static TouchIrc getInstance(){
 		if(instance == null)
 			instance = new TouchIrc();
 		
 		return instance;
 	}
 	
 
 
 	private void loadServers(Context c) {
 		Database db = new Database(c);
 		availableServers = db.getServerList();
 		db.close();		
 
 	}
 	
 	private void loadProfiles(Context c) {
 		Database db = new Database(c);
 		availableProfiles = db.getProfileList();
 		db.close();		
 
 		// TODO Load default profile
 	}
 	
 	/*   SERVERS   */
 	public SparseArray<Server> getAvailableServers(){
 		return availableServers;
 	}
 	
	public boolean addServer(Server server, Context c){
		for(int i = 0 ; i < availableServers.size() ; i++){
			if(availableServers.valueAt(i).equals(server))
					return false;
		}
 		Database db = new Database(c);
 		db.addServer(server);
 		loadServers(c);
		db.close();
		
		return true;
 	}
 	
 	public void updateServer(int idServer, Server server, Context c){
 		Database db = new Database(c);
 		db.updateServer(idServer, server);
 		loadServers(c);
 		db.close();	
 	}
 	
 	public boolean deleteServer(int idServer, Context c){
 		Database db = new Database(c);
 		boolean b = db.deleteServer(idServer);
 		loadServers(c);
 		db.close();	
 		return b;
 	}
 	
 	/*   PROFILES   */
 	public SparseArray<Profile> getAvailableProfiles(){
 		return availableProfiles;
 	}
 	
	public boolean addProfile(Profile profile, Context c){
		for(int i = 0 ; i < availableProfiles.size() ; i++){
			if(availableProfiles.valueAt(i).equals(profile))
				return false;
		}
 		Database db = new Database(c);
 		db.addProfile(profile);
 		loadProfiles(c);
		db.close();
		return true;
 	}
 	
 	public void updateProfile(int idProfile, Profile profile, Context c){
 		Database db = new Database(c);
 		db.updateProfile(idProfile, profile);
 		loadProfiles(c);
 		db.close();		
 	}
 	
 	public void deleteProfile(int idProfile, Context c){
 		Database db = new Database(c);
 		db.deleteProfile(idProfile);
 		loadServers(c);
 		db.close();	
 	}
 	
 	public int getIdDefaultProfile(){
 		return (idDefaultProfile != -1) ? idDefaultProfile : 1;
 	}
 	
 	// Use idProfile = 0 to unset profile
 	public void setProfile(int idServer, int idProfile, Context c){
 		Server server = availableServers.get(idServer);
 		if(idProfile == 0){
 			server.setProfile(null);
 		}else{
 			server.setProfile(availableProfiles.get(idProfile));
 		}
 		updateServer(idServer, server, c);
 	}
 	
 	public Profile getDefaultProfile(){
 		return this.availableProfiles.get(getIdDefaultProfile());
 	}
 
 
 	public boolean setDefaultProfile(int id) {
 		idDefaultProfile = id;
 		// TODO 
 		return true;
 	}
 
 
 	
 }
