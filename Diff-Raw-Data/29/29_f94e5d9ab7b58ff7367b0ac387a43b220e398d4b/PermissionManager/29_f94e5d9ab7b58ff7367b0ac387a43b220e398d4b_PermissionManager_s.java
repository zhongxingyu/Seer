 package Enderware.permissions;
 
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.server.dedicated.DedicatedServer;
 import net.minecraft.server.dedicated.PropertyManager;
 
 public class PermissionManager {
 	public static PermissionManager instance = new PermissionManager();
 	public PropertyManager permmanager = null;
 	public PropertyManager groupmanager = null;
 	public PropertyManager playermanager = null;
 private PermissionManager(){
 	DedicatedServer server = null;
 	if(!(MinecraftServer.getServer() instanceof DedicatedServer))return;
 	server = (DedicatedServer)MinecraftServer.getServer();

 	this.permmanager = new PropertyManager(server.getFile("permission"));
 	this.groupmanager = new PropertyManager(server.getFile("groups"));
 	this.playermanager = new PropertyManager(server.getFile("playergroup"));
 }
 
 
 public void registerPermission(String name,int defaultlevel){
 	permmanager.getIntProperty(name, defaultlevel);
 	
 }
 public void setPlayerGroup(String name,String GroupName,int defaultpermissionLevel){
 	groupmanager.getIntProperty(GroupName, defaultpermissionLevel);
 	playermanager.setProperty(name, " "+GroupName);
 	playermanager.saveProperties();
 }
 public void registerGroup(String name,int defaultlevel){
 	groupmanager.getIntProperty(name, defaultlevel);
 	
 }
 public String getPlayerGroup(String name){
 	return playermanager.getProperty(name, " user");
 }
 public int getPlayerPermissionLevel(String name){
 	return groupmanager.getIntProperty(getPlayerGroup(name), 0);
 	
 }
 public int getPermisionLevel(String permname){
 	return permmanager.getIntProperty(permname, 0);
 }
 public boolean hasPlayerPermission(String playername,String permname){
 	return  getPlayerPermissionLevel(playername)>= getPermisionLevel(permname);
 }
 }
