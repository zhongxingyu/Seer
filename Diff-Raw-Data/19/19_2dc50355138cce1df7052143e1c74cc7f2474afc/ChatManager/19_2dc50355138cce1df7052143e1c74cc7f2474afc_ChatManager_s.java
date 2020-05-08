 package cz.opt.morgulplugin.managers;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Hashtable;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 
 import cz.opt.morgulplugin.MorgulPlugin;
 import cz.opt.morgulplugin.config.Config;
 import cz.opt.morgulplugin.database.DataBase;
 import cz.opt.morgulplugin.entity.MorgPlayer;
 import cz.opt.morgulplugin.event.CommandEvent;
 import cz.opt.morgulplugin.listener.CommandListener;
 import cz.opt.morgulplugin.structs.ChatChannel;
 
 public class ChatManager implements CommandListener
 {
 	private static final String SECTION = "Chat";
 	private static int MAX_QUED_MESSEGES;
 	private static Hashtable<String, ChatChannel> channels;
 	private static ChatChannel worldChannel;
 	public static ChatManager instance;
 	
 	public static void init()
 	{
 		instance = new ChatManager();
 		worldChannel = new ChatChannel();
 		MAX_QUED_MESSEGES = Integer.parseInt(Config.get(SECTION, "Max_QuedMesseges"));
 		CommandManager.registerListener("w", instance);
 		CommandManager.registerListener("msg", instance);
 		CommandManager.registerListener("whisper", instance);
 		CommandManager.registerListener("chat", instance);
 	}
 	
 	public static void onPlayerChatEvent(AsyncPlayerChatEvent e)
 	{
 		MorgulPlugin.log("Chated.");
 		ArrayList<ChatChannel> tempCha = PlayerManager.getPlayer(e.getPlayer().getName()).getChatChannels();
 		for(int i = 0; i < tempCha.size(); i++)
 		{
 			String msg = "[" + e.getPlayer().getName() + "]: " + e.getMessage();
 			tempCha.get(i).sendMsg(msg);
 		}
 	}
 	
 	public static void addChannel(String name, ChatChannel e)
 	{
 		channels.put(name, e);
 	}
 	
 	public static void removeChannel(String name)
 	{
 		channels.remove(name);
 	}
 
 	@Override
 	public boolean onCommand(CommandEvent e)
 	{
 		String senderName = "";
 		if(e.getSender() instanceof Player)
 			senderName = "[" + e.getSender().getName() + "]: ";
 		else
 			senderName = "[System]: ";
 		if(e.getCommand().getName().equalsIgnoreCase("w") || e.getCommand().getName().equalsIgnoreCase("msg") || e.getCommand().getName().equalsIgnoreCase("whisper"))
 		{
 			if(PlayerManager.playerExist(e.getArgs()[0]))
 			{
 				String msg = "";
 				for (int i = 1; i < e.getArgs().length; i++)
 				    msg += e.getArgs()[i]; 
 				if(msg.length() > 242)
 				{
 					e.getSender().sendMessage("Zprava je prilis dlouha.");
 					return true;
 				}
 				e.getSender().sendMessage(senderName + msg);
 				sendMsg(e.getSender(), e.getArgs()[0], senderName + msg);
 				return true;
 			}
 			else
 			{
 				e.getSender().sendMessage("Hrac Jmenem " + e.getArgs()[0] + " neexistuje.");
 				return true;
 			}
 		}
 		else if(e.getCommand().getName().equalsIgnoreCase("chat"))
 		{
 			
 		}
 		return false;
 	}
 	
 	public static boolean sendMsg(CommandSender sender, String name, String msg)
 	{
 		if(PlayerManager.getPlayer(name) != null)
 		{
 			PlayerManager.getPlayer(name).getPlayer().sendMessage(msg);
 			return true;
 		}
 		else
 		{
 			//Player doesnt exist or is offline
 			if(PlayerManager.playerExist(name))
 			{
 				//Save Qued msg.
 				saveQuedMsg(sender, name, msg);
 				return true;
 			}
 			else
 			{
 				sender.sendMessage("Hrac Jmenem " + name + " neexistuje.");
 				return false;
 			}
 		}
 	}
 	
 	private static void saveQuedMsg(CommandSender sender, String plName, String msg)
 	{
 		if(DataBase.query("Select * from qued_msg WHERE id='" + PlayerManager.getPlayerId(plName) + "'").keySet().size() >= MAX_QUED_MESSEGES)
 		{
 			sender.sendMessage("Hrac Jmenem " + plName + " presahl pocet cekajicich zprav. Vase zprava nebude dorucena.");
 			return;
 		}
 		msg = new SimpleDateFormat("[dd.MM|HH:mm]").format(Calendar.getInstance().getTime()) + msg;
 		DataBase.update("INSERT INTO qued_msg (id, msg) VALUES ('" + PlayerManager.getPlayerId(plName) + "', '" + msg + "')");
 		
 		sender.sendMessage("Hrac dostane zpravu a jse pripoji znovu do hry.");
 	}
 	
 	public static void acceptQuedMsgs(MorgPlayer pl)
 	{
 		HashMap<Integer, HashMap<String, String>> rs = DataBase.query("Select * from qued_msg WHERE id='" + PlayerManager.getPlayerId(pl.getName()) + "'");
 		for(int i = 1; i < rs.keySet().size() + 1; i++)
 			pl.getPlayer().sendMessage(rs.get(i).get("msg"));
 		DataBase.update("DELETE From qued_msg WHERE id='" + PlayerManager.getPlayerId(pl.getName()) + "'");
 	}
 	
 	@Override
 	public boolean isPreLogin(){ return false; }
 	@Override
 	public boolean isCmdCom(){ return true; }
 
 	public static ChatChannel getWorldChannel()
 	{
 		return worldChannel;
 	}
 }
