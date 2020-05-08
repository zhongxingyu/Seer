 package spia1001.InvFall;
 
 import java.util.ArrayList;
 
 import org.bukkit.entity.Player;
 
 /*
 InvFall Plugin
 
 @author Chris Lloyd (SPIA1001)
 */
 
 public class PlayerManager {
 	private FileManager fm = new FileManager();
 	private String fileName = "players.txt";
 	private ArrayList<String> players = new ArrayList<String>();
 	
 	public PlayerManager()
 	{
 		load();
 	}
 	public boolean playerIsEnabled(Player p)
 	{
 		return players.contains(p.getName());
 	}
 	public void enablePlayer(Player p)
 	{
 		players.add(p.getName());
 	}
 	public void disablePlayer(Player p)
 	{
 		players.remove(p.getName());
 	}
 	public void save()
 	{
 		fm.createFile(fileName);
		fm.writeFile(fileName, packPlayers());
 	}
 	private String packPlayers()
 	{
 		String list = "";
 		for(int i = 0; i < players.size(); i++)
 			list += players.get(i) + '-';
 		return list;
 	}
 	public void load()
 	{
		String raw = fm.readFile(fileName);
		String split[] = raw.split("-");
		for(int i = 0; i < split.length; i++)
			players.add(split[i]);	
 	}
 	public void dropAll(Player player)
 	{
 		for(int i = 0; i < 9; i++)
 			new ItemFall(player,0,i);
 	}
 }
