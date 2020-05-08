 package cz.vojtamaniak.komplex.listeners;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 import cz.vojtamaniak.komplex.Komplex;
 import cz.vojtamaniak.komplex.User;
 
 public class PlayerListener extends IListener {
 
 	public PlayerListener(Komplex plg) {
 		super(plg);
 	}
 	
 	/**
 	 * @param e - PlayerJoinEvent
 	 * @return
 	 */
 	@EventHandler(priority = EventPriority.LOW)
 	public void onPlayerJoin(PlayerJoinEvent e){
 		e.setJoinMessage(null);
 		for(Player p : Bukkit.getOnlinePlayers()){
 			if(p.hasPermission("komplex.messages.onjoin.receive")){
 				p.sendMessage(msgManager.getMessage("MESSAGE_JOIN").replaceAll("%NICK%", e.getPlayer().getName()));
 			}
 		}
 		User user = new User(e.getPlayer());
 		plg.addUser(user);
 	}
 	
 	/**
 	 * @param e - PlayerQuitEvent
 	 * @return
 	 */
 	@EventHandler(priority = EventPriority.LOW)
 	public void onPlayerQuit(PlayerQuitEvent e){
 		for(Player p : Bukkit.getOnlinePlayers()){
 			if(p.hasPermission("komplex.messages.onquit.receive")){
				p.sendMessage(msgManager.getMessage("MESSAGE_QUIT").replaceAll("%NICK%", e.getPlayer().getName()));				
 			}
 		}
 		plg.removeUser(e.getPlayer().getName());
 	}
 	
 	/**
 	 * @param e - PlayerMoveEvent
 	 * @return
 	 */
 	@EventHandler(priority = EventPriority.LOW)
 	public void onPlayerMove(PlayerMoveEvent e){
 		
 	}
 }
