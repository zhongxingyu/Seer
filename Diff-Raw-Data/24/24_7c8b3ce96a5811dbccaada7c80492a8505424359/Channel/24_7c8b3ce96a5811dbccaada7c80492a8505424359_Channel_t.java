 package com.minecraftdimensions.bungeesuite.objects;
 
 import java.util.ArrayList;
 
 import com.minecraftdimensions.bungeesuite.managers.PlayerManager;
 
 
 public class Channel {
 	private String name;
 	private String format;
 	private String owner;
 	private boolean muted;
 	private boolean isDefault;
 	private boolean open;
 	private ArrayList<BSPlayer> members;
 	
 	public Channel(String name, String format, String owner, boolean muted, boolean isDefault,boolean open){
 		this.name = name;
 		this.format = format;
 		this.owner = owner;
 		this.muted = muted;
 		this.isDefault=isDefault;
 		members = new ArrayList<BSPlayer>();
 	}
 	
 	public Channel(String serialised){
 		String data[] = serialised.split("~");
 		name = data[0];
 		format = data[1];
 		owner = data[2];
 		muted = Boolean.parseBoolean(data[3]);
 		isDefault = Boolean.parseBoolean(data[4]);
 		open = Boolean.parseBoolean(data[5]);
 		members = new ArrayList<BSPlayer>();
 	}
 	
 	
 	public String getName(){
 		return name;
 	}
 	public void setName(String name){
 		this.name = name;
 	}
 	public String format(){
 		return format;
 	}
 	public void setFormat(String format){
 		this.format=format;
 	}
 	public String getOwner(){
 		return owner;
 	}
 	public void setOwner(String owner){
 		this.owner = owner;
 	}
 	public boolean isOwner(String owner){
 		return this.owner.equals(owner);
 	}
 	public boolean isMuted(){
 		return muted;
 	}
 	public void setMuted(boolean mute){
 		this.muted = mute;
 	}
 	public boolean isMember(BSPlayer player){
 		return members.contains(player);
 	}
 	public boolean isDefault(){
 		return isDefault;
 	}
 	public boolean hasMemberLike(String player){
 		for(BSPlayer bsplayer: members){
 			if(bsplayer.getName().contains(player)){
 			return true;
 			}
 		}
 		return false;
 	}
 	public BSPlayer getPlayer(String name){
 		for(BSPlayer player: members){
 			if(player.getName().equals(name)){
 				return player;
 			}
 		}
 		return null;
 	}
 	public BSPlayer getSimilarPlayer(String name){
 		for(BSPlayer player: members){
 			if(player.getName().contains(name)){
 				return player;
 			}
 		}
 		return null;
 	}
 	public void addMember(String player){
 		BSPlayer p = PlayerManager.getPlayer(player);
 		members.add(p);
 		p.joinChannel(this);
 	}
 	public void removeMember(String player){
 		members.remove(PlayerManager.getPlayer(player));
 	}
 	public ArrayList<BSPlayer> getMembers(){
 		return members;
 	}
 	
 	public String serialise(){
 		return name+"~"+format+"~"+owner+"~"+muted+"~"+isDefault+"~"+open;
 	}
 
 	public void addMember(BSPlayer p) {
 		this.members.add(p);
 		p.joinChannel(this);
 		
 	}
 
 	public String getStatus() {
 		if(open){
 			return "open";
 		}else{
 		return "close";
 		}
 	}
	
	public boolean isOpen(){
		return open;
	}
	
	public void setOpen(boolean open){
		this.open=open;
	}
 }
