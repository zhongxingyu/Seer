 package to.joe.manager;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import to.joe.J2;
 import to.joe.util.Flag;
 import to.joe.util.User;
 
 
 public class Users {
 	private J2 j2;
 	public Users(J2 J2){
 		this.j2=J2;
 		this.startTimer();
 		this.restartManager();
 	}
	
 	public void restartManager(){
 		this.users=new ArrayList<User>();
 		this.groups=new HashMap<String, ArrayList<Flag>>();
 		this.clearedAdmins=new ArrayList<String>();
 	}
 	public void restartGroups(){
 		this.groups=new HashMap<String, ArrayList<Flag>>();
 	}
 
 	public boolean isCleared(String name){
 		synchronized(clearlock){
 			return this.clearedAdmins.contains(name);
 		}			
 	}
 
 	public void clear(String name){
 		synchronized(clearlock){
 			this.clearedAdmins.add(name);
 		}	
 	}
 	public void playerReset(String name){
 		synchronized(clearlock){
 			this.clearedAdmins.remove(name);
 		}	
 	}
 
 	public User getUser(String name){
 		synchronized (this.userlock){
 			for(User u:this.users){
 				if(u.getName().equalsIgnoreCase(name))
 					return u;
 			}
 			return null;
 		}
 	}
 	public boolean isOnline(String playername){
 		synchronized (this.userlock){
 			for(User u:this.users){
 				if(u.getName().equalsIgnoreCase(playername)){
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 	public User getUser(Player player){
 		return getUser(player.getName());
 	}
 	public void addUser(String playerName){
 		synchronized (this.userlock){
 			User user=this.j2.mysql.getUser(playerName);
			this.users.add(user);
 		}
 	}
 	public void delUser(String name){
 		synchronized (this.userlock){
 			User toremove=null;
 			for(User user : this.users){
 				if(user.getName().equalsIgnoreCase(name))
 					toremove=user;
 			}
 			if(toremove!=null)
 				this.users.remove(toremove);
 		}
 	}
 	public void addFlag(String name, Flag flag){
 		User user=getUser(name);
 		if(user==null){
 			user=this.j2.mysql.getUser(name);
 		}
 		this.addFlagLocal(name, flag);
 		j2.debug("Adding flag "+flag.getChar()+" for "+name);
 		this.j2.mysql.setFlags(name, user.getUserFlags());
 	}
 	public void addFlagLocal(String name, Flag flag){
 		User user=getUser(name);
 		if(user!=null){
 			user.addFlag(flag);
 		}
 	}
 	public void dropFlag(String name, Flag flag){
 		User user=getUser(name);
 		if(user==null){
 			user=this.j2.mysql.getUser(name);
 		}
 		this.dropFlagLocal(name, flag);
 		j2.debug("Dropping flag "+flag.getChar()+" for "+name);
 		this.j2.mysql.setFlags(name, user.getUserFlags());
 	}
 	public void dropFlagLocal(String name, Flag flag){
 		User user=getUser(name);
 		if(user!=null){
 			user.dropFlag(flag);
 		}
 	}
 	public void setGroups(HashMap<String, ArrayList<Flag>> Groups){
 		this.groups=Groups;
 	}
 
 	public boolean groupHasFlag(String group, Flag flag){
 		return (this.groups.get(group) != null ? this.groups.get(group).contains(flag) : false);
 	}
 
 	public ArrayList<Flag> getGroupFlags(String groupname){
 		return this.groups.get(groupname);
 	}
 
 	public ArrayList<Flag> getAllFlags(Player player){
 		ArrayList<Flag> all=new ArrayList<Flag>();
 		User user=getUser(player);
 		all.addAll(user.getUserFlags());
 		all.addAll(getGroupFlags(user.getGroup()));
 		return all;
 
 	}
 
 	public void jail(String name, String reason, String admin){
 		if(isOnline(name)){
 			addFlag(name,Flag.JAILED);
 		}
 		else {
 			j2.mysql.getUser(name);
 			addFlag(name,Flag.JAILED);
 			delUser(name);
 		}
 		synchronized(jaillock){
 			this.jailReasons.put(name, reason);
 		}
 		//j2.mysql.jail(name,reason,admin);
 	}
 
 	public void unJail(String name){
 		if(isOnline(name)){
 			dropFlag(name,Flag.JAILED);
 		}
 		else {
 			j2.mysql.getUser(name);
 			dropFlag(name,Flag.JAILED);
 			delUser(name);
 		}
 		synchronized(jaillock){
 			this.jailReasons.remove(name);
 		}
 		//j2.mysql.unJail(name);
 	}
 
 	public String getJailReason(String name){
 		return this.jailReasons.get(name);
 	}
 
 	public void jailSet(HashMap<String,String> incoming){
 		this.jailReasons=incoming;
 	}
	
 	private boolean stop=true;
 	private void startTimer() {
 		stop = false;
 		final Timer timer = new Timer();
 		timer.schedule(new TimerTask() {
 			@Override
 			public void run() {
 				if (stop) {
 					timer.cancel();
 					return;
 				}
 				checkOnline();
 			}
 		}, 10000, 10000);
 	}
	
 	private void checkOnline(){
 		synchronized (this.userlock){
 			for(User u:new ArrayList<User>(this.users)){
 				Player p=j2.getServer().getPlayer(u.getName());
 				if(p==null||!p.isOnline()){
 					this.users.remove(u);
 					if(p!=null){
 						p.kickPlayer("You have glitched. Rejoin");
 					}
 				}
 			}
 		}
 	}
	
 	public void processJoin(Player player){
 		String name=player.getName();
 		j2.irc.processJoin(name);
 		j2.ip.processJoin(name);
 		j2.warps.processJoin(name);
 		j2.damage.processJoin(name);
 		j2.jail.processJoin(player);
 		this.playerReset(name);
 		if(player.getInventory().getHelmet().equals(Material.FIRE)){
 			player.getInventory().setHelmet(new ItemStack(Material.GRASS));
 			player.sendMessage(ChatColor.RED+"You fizzle out");
 		}
 		if(j2.maintenance){
 			player.sendMessage(ChatColor.YELLOW+"We are in maintenance mode");
 		}
 		try{
 			j2.mcbans.processJoin(name);
 		}
 		catch (Exception e){
			
 		}
 		for(String line : j2.motd){
 			player.sendMessage(line);
 		}
 		if(j2.hasFlag(name, Flag.ADMIN)){
 			int count=this.j2.reports.numReports();
 			if(count>0){
 				player.sendMessage(ChatColor.LIGHT_PURPLE+"There are "+count+" reports. Say /r");
 			}
 		}
 		j2.minitrue.processJoin(player);
 		if(j2.hasFlag(player, Flag.CONTRIBUTOR)){
 			player.sendMessage(ChatColor.LIGHT_PURPLE+"We think you're an "+ChatColor.GOLD+"AMAZING CONTRIBUTOR");
 			player.sendMessage(ChatColor.LIGHT_PURPLE+"to the minecraft community as a whole! "+ChatColor.RED+"<3");
 		}
 	}
 
 
 	public Location jail;
 	private ArrayList<User> users;
 	private HashMap<String, ArrayList<Flag>> groups;
 	private Object userlock= new Object(),jaillock=new Object(),clearlock=new Object();
 	private HashMap<String, String> jailReasons;
 	private ArrayList<String> clearedAdmins;
 }
