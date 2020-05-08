 package org.openstreetmap.OSMZmiany;
 
 import java.awt.Dimension;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.NotSerializableException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.util.ArrayList;
 
 
 public class Configuration implements Serializable {
 	class Profile implements Serializable{
 		private static final long serialVersionUID = 1L;
 		//Type of list 0-off;1=whitelist;2=blacklist;
 		private short listType=0;
 		private ArrayList<User> users=new ArrayList<User>();
 		//Show data
 		//-1=ALL;0=created;1=modified;2=deleted
 		private short showType=-1;
 		private String name;
 		private Configuration conf;		
 		private MapFilter mapfilter;
 		
 		public Profile(String name){
 			this.name=name;
 		}
 		public void setConfList(Configuration a){
 			conf=a;
 		}
 		//SETTERS
 		public void setMapFilter(MapFilter mf){
 			mapfilter=mf;
 			if(conf!=null)
 				conf.sendProfileChanged(this);			
 		}		
 		public void setShowType(short type){
 			showType=type;
 			if(conf!=null)
 				conf.sendProfileChanged(this);			
 		}
 		public void setListType(short type){
 			listType=type;
 			if(conf!=null)
 				conf.sendProfileChanged(this);		
 		}
 		public void setName(String name){
 			this.name=name;
 			if(conf!=null)
 				conf.sendProfileChanged(this);			
 		}
 		//GETTERS
 		public MapFilter getMapFilter(){
 			return mapfilter;
 		}		
 		public short getShowType(){
 			return showType;
 		}
 		public short getListType(){
 			return listType;
 		}
 		public String getName(){
 			return name;
 		}
 		//USERS
 		public void addUser(User user){
 			//if it doesn't exist on list
 			boolean is=false;
 			for(int i=0;i<users.size();i++)
 				if(user.equals(users.get(i)))
 					is=true;
 			if(is==false)
 				users.add(user);
 		}
 		public void removeUser(User user){
 			for(int i=0;i<users.size();){
 				if(user.equals(users.get(i)))
 					users.remove(i);
 				i++;
 			}
 		}
 		
 		public User[] getUsers(){
 			//TODO sort
 			User []user=new User[users.size()];
 			for(int i=0;i<user.length;i++){
 				user[i]=users.get(i);
 			}
 			return user;
 		}
 	}
 	
 	private static final long serialVersionUID = 1L;	
 	private ArrayList<Profile> profiles=new ArrayList<Profile>();
 	private int selectedProfile=0;
 	private ArrayList<ConfigurationListener> configurationListeners=new ArrayList<ConfigurationListener>();
 	private Dimension windowSize;
 	private int dividerLocation=400;
	private String diffBaseUrl = "http://planet.openstreetmap.org/redaction-period/minute-replicate/";
 	
 	
 	public Configuration(){
 		//default
 		Profile p=new Profile("My profile");
 		//p.drawStyle=new SelectedDrawStyle();
 		p.mapfilter=null;
 		profiles.add(p);
 	}
 	
 	public Dimension getWindowSize(){
 		if(windowSize==null)windowSize=new Dimension(723, 472);
 		return windowSize;
 	}	
 	public int getDividerLocation(){
 		return dividerLocation;
 	}
 	public void setDividerLocation(int a){
 		dividerLocation=a;
 	}
 	public void setWindowSize(Dimension m){
 		windowSize=m;
 	}
 	
 	public String getDiffBaseUrl() {
 		return diffBaseUrl;
 	}
 	
 	public void setDiffBaseURL(String diffBaseUrl) {
 		this.diffBaseUrl = diffBaseUrl;
 	}
 	
 	public void addProfile(Profile profile){
 		//TODO NAME TEST
 		profiles.add(profile);
 		profile.setConfList(this);
 	}
 	
 	public Profile addProfile(String str){
 		boolean isIn=false;
 		for(int i=0;i<profiles.size();i++)
 			if(profiles.get(i).name.equals(str))
 				isIn=true;
 		if(isIn)
 			return null;
 		Profile p=new Profile(str);
 		//TODO NAME TEST
 		profiles.add(p);
 		p.setConfList(this);
 		return p;
 	}
 	
 	
 	public void removeProfile(String name){
 		if(profiles.size()<2)return;
 		for(int i=0;i<profiles.size();i++)
 			if(profiles.get(i).name.equals(name)){
 				profiles.remove(i);
 				return;
 			}
 	}
 	
 	public Profile[] getProfiles(){
 		//TODO Sort
 		Profile []pros=new Profile[profiles.size()];
 		for(int i=0;i<pros.length;i++){
 			pros[i]=profiles.get(i);
 		}
 		return pros;
 	}
 	
 	public Profile getSelectedProfile(){
 		if(selectedProfile>=profiles.size())selectedProfile=0;
 		return profiles.get(selectedProfile);
 	}
 	
 	public void selectProfile(Profile p){
 		int z=profiles.lastIndexOf(p);
 		if(z!=-1){
 			selectedProfile=z;
 			sendProfileChanged(profiles.get(selectedProfile));
 		}
 	}
 	
 	public void sendProfileChanged(Profile z){
 		for(int j=0;j<configurationListeners.size();j++)
 			configurationListeners.get(j).profileChanged(z);
 	}
 	
 	public void addConfigurationListener(ConfigurationListener cl){
 		configurationListeners.add(cl);
 	}
 
 	public static Configuration instance;
 	//////////////////////////LOAD AND SAVE SECTION///////////////////////////////
 	private static Configuration loadFromFile(String name) throws FileNotFoundException,IOException, NotSerializableException, ClassNotFoundException{
 	    ObjectInputStream ois= new ObjectInputStream(new FileInputStream(name));
 	    Configuration c=(Configuration) ois.readObject();
 	    ois.close();
 		return c;
 	}
 	
 	public static Configuration loadFromFile(){
 		//TODO CROSSPLATFORM HOMEDIR
 		try {
 			instance=loadFromFile(System.getProperty("user.home") + File.separator + ".OSMZMIANY_CONFIG");
 			return instance;
 		} catch (FileNotFoundException e) {
 			//Logger.printStackTrace(e,"Configuration load: File not found");
 		} catch (NotSerializableException e) {
 			Logger.printStackTrace(e,"Configuration load: Serializable error");	
 		} catch (IOException e) {
 			try {
 				instance=loadFromFile(System.getProperty("user.home") + File.separator + ".OSMZMIANY_CONFIG");
 				return instance;
 			} catch (Exception e1) {				
 				Logger.printStackTrace(e,"Configuration load: File error");
 			}
 		} catch (ClassNotFoundException e) {
 			Logger.printStackTrace(e,"Configuration load: Object error");
 		}
 		//Create default configuration
 		instance=new Configuration();
 		return instance;
 	}
 	
 	private void saveToFile(String filename) throws IOException{
 		//Clear listeners		
 		configurationListeners.clear();		
         ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename));
         oos.writeObject(this);
         oos.close();
 	}
 	
 		
 	
 	public void saveToFile(){
 		try {
 			saveToFile(System.getProperty("user.home") + File.separator + ".OSMZMIANY_CONFIG");
 		} catch (IOException e) {
 			Logger.printStackTrace(e,"Configuration save: File save error");
 		}
 	}
 	
 	
 }
