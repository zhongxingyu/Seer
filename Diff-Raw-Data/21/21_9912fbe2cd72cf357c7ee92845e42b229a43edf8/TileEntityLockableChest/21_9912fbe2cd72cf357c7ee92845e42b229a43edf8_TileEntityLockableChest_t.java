 package uk.co.jacekk.bukkit.grouplock.nms;
 
 import java.util.ArrayList;
 
 import net.minecraft.server.NBTTagCompound;
 import net.minecraft.server.NBTTagList;
 import net.minecraft.server.NBTTagString;
 import net.minecraft.server.TileEntityChest;
 
 public class TileEntityLockableChest extends TileEntityChest implements TileEntityLockable {
 	
 	private String ownerName;
 	
 	private ArrayList<String> allowedPlayers;
 	private ArrayList<String> allowedGroups;
 	
 	public TileEntityLockableChest(){
 		this.ownerName = null;
 		
 		this.allowedPlayers = new ArrayList<String>();
 		this.allowedGroups = new ArrayList<String>();
 	}
 	
 	@Override // load()
 	public void a(NBTTagCompound compound){
 		super.a(compound);
 		
 		NBTTagCompound data = compound.getCompound("GroupLock");
 		
		if (data != null){
 			this.ownerName = data.getString("owner-name");
 			
			if (this.ownerName == null || this.ownerName.isEmpty()){
 				this.ownerName = null;
 			}
 			
 			NBTTagList allowedPlayers = data.getList("allowed-players");
 			NBTTagList allowedGroups = data.getList("allowed-groups");
 			
 			if (allowedPlayers != null){
 				for (int i = 0; i < allowedPlayers.size(); ++i){
 					this.allowedPlayers.add(((NBTTagString) allowedPlayers.get(i)).data);
 				}
 			}
 			
 			if (allowedGroups != null){
 				for (int i = 0; i < allowedGroups.size(); ++i){
 					this.allowedPlayers.add(((NBTTagString) allowedGroups.get(i)).data);
 				}
 			}
 		}
 	}
 	
 	@Override // save()
 	public void b(NBTTagCompound compound){
 		super.b(compound);
 		
 		NBTTagCompound data = new NBTTagCompound("GroupLock");
 		
 		if (this.hasOwnerName()){
 			data.set("owner-name", new NBTTagString("owner-name", this.ownerName));
 		}
 		
 		NBTTagList allowedPlayers = new NBTTagList();
 		NBTTagList allowedGroups = new NBTTagList();
 		
 		for (int i = 0; i < this.allowedPlayers.size(); ++i){
 			allowedPlayers.add(new NBTTagString(Integer.toString(i), this.allowedPlayers.get(i)));
 		}
 		
 		for (int i = 0; i < this.allowedGroups.size(); ++i){
 			allowedGroups.add(new NBTTagString(Integer.toString(i), this.allowedGroups.get(i)));
 		}
 		
 		data.set("allowed-players", allowedPlayers);
 		data.set("allowed-groups", allowedGroups);
 		
 		compound.setCompound("GroupLock", data);
 	}
 	
 	public boolean hasOwnerName(){
 		return (this.ownerName != null);
 	}
 	
 	public boolean isOwnerName(String playerName){
 		return (this.hasOwnerName() && this.ownerName.equals(playerName));
 	}
 	
 	public boolean canAccess(String playerName){
 		return ((!this.hasOwnerName() || this.ownerName.equals(playerName)) || this.allowedPlayers.contains(playerName)); // TODO || playerInGroup()
 	}
 	
 	public boolean canModify(String playerName){
 		return (!this.hasOwnerName() || this.ownerName.equals(playerName)); 
 	}
 	
 	public String getOwnerName(){
 		return this.ownerName;
 	}
 	
 	public void setOwnerName(String ownerName){
 		this.ownerName = ownerName;
 		
 		System.out.println("NAME SET TO " + this.ownerName);
 	}
 	
 	public ArrayList<String> getAllowedPlayers(){
 		return this.allowedPlayers;
 	}
 	
 	public void addAllowedPlayer(String playerName){
 		this.allowedPlayers.add(playerName);
 	}
 	
 	public void removeAllowedPlayer(String playerName){
 		this.allowedPlayers.remove(playerName);
 	}
 	
 	public void reset(){
 		this.ownerName = null;
 		this.allowedPlayers.clear();
 		this.allowedGroups.clear();
 	}
 	
 }
