 package me.corriekay.pppopp3.utils;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import me.corriekay.pppopp3.Mane;
 import me.corriekay.pppopp3.ponyville.Pony;
 import me.corriekay.pppopp3.ponyville.Ponyville;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.EventException;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.plugin.EventExecutor;
 
 public abstract class PSCmdExe implements EventExecutor, CommandExecutor, Listener {
 
 	public final String name;
 	protected final String pinkieSays = ChatColor.LIGHT_PURPLE+"Pinkie Pie: ";
 	protected final HashMap<Class<? extends Event>, Method> methodMap = new HashMap<Class<? extends Event>, Method>();
 	protected final ConsoleCommandSender console = Bukkit.getConsoleSender();
 	protected final String notPlayer = pinkieSays+"Silly console, You need to be a player to do that!";
 	protected final String notEnoughArgs = pinkieSays+"Uh oh, youre gonna need to provide more arguments for that command than that!";
 	protected final String cantFindPlayer = pinkieSays+"I looked high and low, but I couldnt find that pony! :C";
 	
 	@SuppressWarnings("unchecked")
 	public PSCmdExe(String name, String[] cmds){
 		this.name = name;
 		for(Method method : this.getClass().getMethods()){
 			method.setAccessible(true);
 			Annotation eh = method.getAnnotation(EventHandler.class);
 			if(eh != null){
 				Class<?>[] params = method.getParameterTypes();
 				if(params.length == 1){
 					registerEvent((Class<? extends Event>)params[0], ((EventHandler)eh).priority(),method);
 				}
 			}
 		}
 		if (cmds != null) {
 			registerCommands(cmds);
 		}
 	}
 	private void registerEvent(Class<? extends Event> event, EventPriority priority,Method method){
 		try {
 			Bukkit.getPluginManager().registerEvent(event, this, priority,this, Mane.getInstance());
 			methodMap.put(event, method);
 		} catch (NullPointerException e) {
 			Bukkit.getLogger().severe("Illegal event registration!");
 		}
 	}
 	private void registerCommands(String[] cmds){
 		for(String cmd : cmds){
 			Mane.getInstance().getCommand(cmd).setExecutor(this);
 			Mane.getInstance().getCommand(cmd).setPermissionMessage(pinkieSays+"Oh no! You cant do this :c");
 		}
 	}
 	@Override
 	public void execute(Listener arg0, Event arg1) throws EventException {
 		Method method = methodMap.get(arg1.getClass());
 		if(method == null){
 			return;
 		}
 		try{
 			method.invoke(this,arg1);
 		} catch(Exception e){
 			if(e instanceof InvocationTargetException){
 				InvocationTargetException ite = (InvocationTargetException)e;
 				e.setStackTrace(ite.getCause().getStackTrace());
 			}
 			String eventName = arg1.getClass().getCanonicalName();
 			PonyLogger.logListenerException(e, "Error on event: "+e.getMessage(), name, eventName);
 			sendMessage(console,"Unhandled exception in listener! Please check the error logs for more information: "+name+":"+e.getClass().getCanonicalName());
 		}
 
 	}
 
 	@Override
 	public final boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		try{
 			return handleCommand(sender,cmd,label,args);
 		} catch (Exception e){
 			String message = "Exception thrown on command: "+cmd.getName()+"\nLabel: "+label;
 			message +="\nArgs: ";
 			for(String arg : args){
 				message+= arg+" ";
 			}
 			PonyLogger.logCmdException(e, message, name);
 			sendMessage(console,"Unhandled exception on Command! Please check the error log for more information!");
 			sendMessage(sender,"OH SWEET CELESTIA SOMETHING WENT HORRIBLY HORRIBLY WRONG! YOU GOTTA TELL THE SERVER ADMINS AS SOON AS YOU CAN D:");
 			return false;
 		}
 	}
 	public boolean handleCommand(CommandSender sender, Command cmd, String label, String[] args){
 		return false;
 	}
 	public void logAdmin(CommandSender sender, String message){
 		String name = "";
 		if(sender instanceof Player){
 			name = ((Player)sender).getName();
 		} else if(sender instanceof ConsoleCommandSender){
 			name = "Console";
 		}
 		PonyLogger.logAdmin(name, message);
 	}
         
         /**
          * Sends a message to a CommandSender prepended with "Pinkie Pie: ".
          * @param sender CommandSender object to issue a command to.
          * @param message Message to send to sender.
          */
 	public void sendMessage(CommandSender sender, String message){
 		sender.sendMessage(pinkieSays+message);
 	}
 
 	/*methods for getting players*/
 	private void tooManyMatches(ArrayList<String> playerNames, CommandSender requester){
 		String message = pinkieSays+"There were too many matches!!";
 		for(String string : playerNames){
 			message+= string+ChatColor.LIGHT_PURPLE+" ";
 		}
 		requester.sendMessage(message);
 	}
 	
 	private void tooManyMatches(List<Player> playerNames, CommandSender requester){
 		String message = pinkieSays+"There were too many matches!!";
 		for(Player derp : playerNames){
 			message+= derp.getName()+ChatColor.LIGHT_PURPLE+" ";
 		}
 		requester.sendMessage(message);
 	}
 	/**
 	 * Player get methods
 	 */
 	protected Pony getSingleOnlinePony(String pName, CommandSender requester){
 		Player target = getSingleOnlinePlayer(pName,requester);
 		if(target == null){
 			return null;
 		}
 		Pony pony = Ponyville.getPony(target);
 		if (pony == null){
 			Mane.getInstance().getLogger().severe("PONYVILLE OUT OF SYNC WITH ONLINE PLAYERS. PONY NOT FOUND IN PONYVILLE");
 			StackTraceElement[] ste = Thread.getAllStackTraces().get(Thread.currentThread());
 			StringBuilder e = new StringBuilder();
 			for(StackTraceElement s : ste){
 				e.append(s.toString()+"\n");
 			}
 			System.out.println(e);
 			return null;
 		}
 		return pony;
 	}
 	protected Player getSingleOnlinePlayer(String pName, CommandSender requester){
 		pName = pName.toLowerCase();
 		ArrayList<String> players = new ArrayList<String>();
 		for(Player player : Bukkit.getOnlinePlayers()){
 			String playername = player.getName().toLowerCase();
 			String playerDname = ChatColor.stripColor(player.getDisplayName()).toLowerCase();
 			if(playername.equals(pName)||playerDname.equals(pName)){
 				return player;
 			} else {
 				if(playername.contains(pName)||playerDname.contains(pName)){
 					//if(!InvisibilityHandler.ih.isHidden(player.getName())||requester.hasPermission("pppopp2.seehidden")){
 						players.add(player.getName()); //TODO add invis check
 					//}
 				}
 			}
 		}
 		if(players.size()>1){
 			tooManyMatches(players,requester);
 			return null;
 		} else if(players.size()<1){
 			sendMessage(requester,"I looked high and low, but I couldnt find that pony! :C");
 			return null;
 		} else {
 			return Bukkit.getPlayerExact(players.get(0));
 		}
 	}
 	protected Pony getOnlinePonyCanNull(String pName, CommandSender requester){
 		ArrayList<Player> players = getOnlinePlayerCanNull(pName,requester);
 		if(players.size()<1){
 			return null;
 		} else {
 			try {
 				Pony pony = new Pony(players.get(0));
 				return pony;
 			} catch (FileNotFoundException e) {
 				return null;
 			}
 		}
 	}
 	protected ArrayList<Player> getOnlinePlayerCanNull(String pName, CommandSender requester){
 		pName = pName.toLowerCase();
 		ArrayList<Player> players = new ArrayList<Player>();
 		for(Player player : Bukkit.getOnlinePlayers()){
 			if(player.getName().toLowerCase().equals(pName)||ChatColor.stripColor(player.getDisplayName()).toLowerCase().equals(pName)){
 				players = new ArrayList<Player>();
 				players.add(player);
 				return players;
 			}
 			if (player.getName().toLowerCase().contains(pName)||ChatColor.stripColor(player.getDisplayName()).toLowerCase().contains(pName)) {
 				//if (InvisibilityHandler.ih.isHidden(player.getName())) {
 					if (requester.hasPermission("pppopp2.seehidden")) {
 						players.add(player);
 					}
 				//} else {
 					players.add(player);//TODO add invis check
 				//}
 			}
 		}
 		if(players.size()>1){
 			tooManyMatches(players,requester);
 			return null;
 		} else {
 			return players;
 		}
 	}
         /**
          * Works like {@link 
          * #getSinglePlayer(String, CommandSender) getSinglePlayer},
          * except it returns a Pony object rather than a String.
          * @param pName Full or partial String of a username to find. 
          * @param requester Reference to the issuer of the command.
          * @return Pony object that would be returned by getSinglePlayer.
          * @see #getSinglePlayer(String, CommandSender) 
          */
 	protected Pony getSinglePony(String pName, CommandSender requester){
 		String target = getSinglePlayer(pName,requester);
 		if(target == null){
 			return null;
 		}
 		try {
 			Pony pony = new Pony(target);
 			return pony;
 		} catch (FileNotFoundException e) {
 			return null;
 		}
 	}
         /**
          * Returns a String object based on the parameters given that returns 
          * null should more than one player file contain pName.  This will only
          * work using account names, rather than nicknames.
          * @param pName Full or partial String of a username to find.
          * @param requester Reference to the issuer of the command.
          * @return If only one match is found, this returns a String containing
          *         the extensionless filename of the player's file that contains
          *         pName.  Otherwise, this returns <code>null</code>.
          */
 	protected String getSinglePlayer(String pName, CommandSender requester){
 		pName = pName.toLowerCase();
 		ArrayList<String> player = new ArrayList<String>();
 		File dir = new File(Mane.getInstance().getDataFolder()+File.separator+"Players");
 		if(!dir.isDirectory()){
 			sendMessage(requester,"I looked high and low, but I couldnt find that pony! :C");
 			return null;
 		}
 		File[] files = dir.listFiles();
 		for(File file : files){
			String fname = file.getName().substring(0,file.getName().length()-4);
 			if(fname.toLowerCase().equals(pName)){
 				return fname;
 			}
 			if(fname.toLowerCase().contains(pName)){
 				player.add(fname);
 			}
 		}
 		if(player.size()==0){
 			sendMessage(requester,"I looked high and low, but I couldnt find that pony! :C");
 			return null;
 		}
 		if(player.size()>1){
 			tooManyMatches(player,requester);
 			return null;
 		}
 		return player.get(0);
 	}
 
         /**
          * Sends a message to all Players connected, including the console.
          * @param message Message to send.
          */
 	protected void broadcastMessage(String message){
 		for(Player p : Bukkit.getOnlinePlayers()){
 			p.sendMessage(message);
 		}
 		console.sendMessage(message);
 		//RemotePonyAdmin.rpa.message(message); //TODO
 		
 	}
 
 	public void deactivate(){
 		
 	}
 	public void reload(){
 		
 	}
 }
