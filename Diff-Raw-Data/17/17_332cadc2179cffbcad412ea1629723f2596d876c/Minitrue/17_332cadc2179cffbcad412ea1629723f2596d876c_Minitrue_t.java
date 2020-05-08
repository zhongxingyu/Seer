 package to.joe.manager;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import com.echo28.bukkit.vanish.Vanish;
 import to.joe.J2Plugin;
 import to.joe.util.Flag;
 
 public class Minitrue {
 	private J2Plugin j2;
 	private Vanish vanish;
 	private boolean vanishing;
 	public Minitrue(J2Plugin j2){
 		this.j2=j2;
 		this.vanishing=false;
 	}
 	public void getToWork(){
 		Vanish p = null;
 		Plugin test = this.j2.getServer().getPluginManager().getPlugin("Vanish");
 		if(test != null && test instanceof Vanish) {
 			p = (Vanish)test;
 		}
 		if(p == null) {
 			Logger.getLogger("Minecraft").warning("Failed to find J2Plugin. Oh dear.");
 		}
 		else{
 			this.vanishing=true;
 		}
 
 		this.vanish = p;
 	}
 	public void vanish(Player player){
 		if(this.vanishing){
 			vanish.vanish(player);
 			if(this.invisible(player)){//assume player is NOW invisible
 				this.announceLeave(player.getName());
 			}
 			else{//now visible
 				this.announceJoin(player.getName());
 			}
 		}
 		else{
 			player.sendMessage(ChatColor.AQUA+"Can't vanish D:");
 		}
 	}
 	public boolean invisible(Player player){
 		if(this.vanishing)
 			return vanish.invisible.contains(player);
 		else return false;
 	}
 	public void announceJoin(String playerName){
 		j2.chat.msgAll(ChatColor.YELLOW+"Now arriving: "+playerName);
 	}
 	public void announceLeave(String playerName){
 		if(j2.users.isOnline(playerName)){
 			j2.chat.msgAll(ChatColor.YELLOW+"Now departing: "+playerName);
 		}
 	}
 	public boolean chat(Player player,String message){
 		if(this.invisible(player)){
 			j2.chat.aMsg(player.getName(), message);
 			return true;
 		}
 		return false;
 	}
 
 	public int numinvis(){
 		if(this.vanishing)
 			return this.vanish.invisible.size();
 		return 0;
 	}
 
 	public void who(Player player){
 		Player[] players=j2.getServer().getOnlinePlayers();
 		boolean isAdmin=j2.hasFlag(player, Flag.ADMIN);
 		int curlen=0;
 		int maxlen=320;
 		int playercount=players.length;
 		if(!isAdmin)
 			playercount-=this.numinvis();
 		String msg="Players ("+playercount+"/"+j2.playerLimit+"):";
 
 		for(char ch:msg.toCharArray()){
 			curlen+=Chats.characterWidths[(int)ch];
 		} //now we have our base length
 		int pc=0;
 
 		for(Player p: players){
 			boolean invis=j2.minitrue.invisible(p);
 			if(!invis||isAdmin){
 				String name=p.getName();
 				String cname=j2.users.getUser(name).getColorName();
 				if(isAdmin&&j2.hasFlag(p,Flag.ADMIN)){
 					if(invis)
 						cname=ChatColor.AQUA+name;
 					else
 						cname=ChatColor.RED+name;
 				}
 				int thislen=0;
 				for(char ch:name.toCharArray()){
 					thislen+=Chats.characterWidths[(int)ch];
 				}
 				if(thislen+1+curlen>maxlen){
 					player.sendMessage(msg);
 					msg=cname;
 				}
 				else{
 					msg+=" "+cname;
 				}
 				pc++;
 			}
 		}
 		player.sendMessage(msg);
 	}
 
 	public List<Player> matchPlayer(String name,boolean isAdmin){
 		List<Player> players=j2.getServer().matchPlayer(name);
 		if(!isAdmin){
 			ArrayList<Player> toremove=new ArrayList<Player>();
 			for(Player p:players){
 				if(p!=null&&this.invisible(p)){
 					toremove.add(p);
 				}
 			}
 			players.removeAll(toremove);
 		}
 		return players;
 	}
 
 	public Player[] getOnlinePlayers(){
 		Player[] players=j2.getServer().getOnlinePlayers();
		Player[] toreturn=new Player[players.length-this.numinvis()];
		int cur=0;
 		for(Player p:players){
 			if(!this.invisible(p)){
				toreturn[cur]=p;
				cur++;
 			}
 		}
		return toreturn;
 	}
 }
