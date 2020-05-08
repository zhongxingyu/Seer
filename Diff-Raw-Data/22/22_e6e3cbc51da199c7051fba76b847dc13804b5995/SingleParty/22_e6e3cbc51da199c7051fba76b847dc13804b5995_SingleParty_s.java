 package com.theminequest.MineQuest.Group;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 
 import com.theminequest.MineQuest.API.Group.Group;
 import com.theminequest.MineQuest.API.Group.GroupException;
 import com.theminequest.MineQuest.API.Group.GroupException.GroupReason;
 import com.theminequest.MineQuest.API.Group.QuestGroup;
 import com.theminequest.MineQuest.API.Quest.Quest;
 import com.theminequest.MineQuest.API.Quest.QuestDetails;
 
 public class SingleParty implements QuestGroup {
 	
 	private Player player;
 	private Quest activeQuest;
 	
 	public SingleParty(Player player, Quest activeQuest){
 		this.player = player;
 		this.activeQuest = activeQuest;
 	}
 
 	@Override
 	public long getID() {
 		return -1;
 	}
 
 	@Override
 	public Player getLeader() {
 		return player;
 	}
 
 	@Override
 	public void setLeader(Player p) throws GroupException {
 		throw new GroupException(GroupReason.MAINWORLDQUEST);
 	}
 
 	@Override
 	public List<Player> getMembers() {
 		LinkedList<Player> tr = new LinkedList<Player>();
 		tr.addFirst(player);
 		return tr;
 	}
 
 	@Override
 	public void add(Player p) throws GroupException {
 		throw new GroupException(GroupReason.MAINWORLDQUEST);
 	}
 
 	@Override
 	public void remove(Player p) throws GroupException {
 		throw new GroupException(GroupReason.MAINWORLDQUEST);
 	}
 
 	@Override
 	public boolean contains(Player p) {
 		if (p.equals(player))
 			return true;
 		return false;
 	}
 
 	@Override
 	public int getCapacity() {
 		return 1;
 	}
 
 	@Override
 	public void setCapacity(int capacity) throws GroupException {
 		throw new GroupException(GroupReason.MAINWORLDQUEST);
 	}
 
 	@Override
 	public boolean isPVP() {
 		return false;
 	}
 
 	@Override
 	public void setPVP(boolean on) {
 		// do nothing
 	}
 
 	@Override
 	public void teleportPlayers(Location l) {
 		player.teleport(l);
 	}
 
 	@Override
 	public int compareTo(Group arg0) {
 		return Integer.valueOf(player.getEntityId()).compareTo(arg0.getLeader().getEntityId());
 	}
 
 	@Override
 	public Quest getQuest() {
 		return activeQuest;
 	}
 
 	@Override
 	public QuestStatus getQuestStatus() {
 		return QuestStatus.MAINWORLDQUEST;
 	}
 
 	@Override
 	public void startQuest(QuestDetails d) throws GroupException {
 		throw new GroupException(GroupReason.MAINWORLDQUEST);
 	}
 
 	@Override
 	public void abandonQuest() throws GroupException {
 		throw new GroupException(GroupReason.MAINWORLDQUEST);
 	}
 
 	@Override
 	public void enterQuest() throws GroupException {
 		throw new GroupException(GroupReason.MAINWORLDQUEST);
 	}
 
 	@Override
 	public void exitQuest() throws GroupException {
 		throw new GroupException(GroupReason.MAINWORLDQUEST);
 	}
 
 	@Override
 	public void finishQuest() throws GroupException {
		// do nothing
 	}
 
 }
