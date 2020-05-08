 package syam.CraftIRCBridge.Listeners;
 
 import java.util.logging.Logger;
 
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.server.ServerCommandEvent;
 import org.bukkit.event.weather.WeatherChangeEvent;
 
 import com.dthielke.herochat.ChannelChatEvent;
 import com.dthielke.herochat.Chatter;
 import com.dthielke.herochat.Chatter.Result;
 import com.dthielke.herochat.ConversationChannel;
 
 import syam.CraftIRCBridge.CraftIRCBridge;
 import syam.CraftIRCBridge.Bridge.Bridge;
 import syam.CraftIRCBridge.Bridge.BridgeManager;
 
 public class BridgePlayerListener implements Listener{
 	public final static Logger log = CraftIRCBridge.log;
 
 	private final CraftIRCBridge plugin;
 
 	public BridgePlayerListener(final CraftIRCBridge plugin){
 		this.plugin = plugin;
 	}
 
 	/* 登録するイベントはここから下に */
 
 	/**
 	 * コマンド実行イベント
 	 * @param event
 	 */
 	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
 	public void onServerCommand(final ServerCommandEvent event) {
 		String cmd = event.getCommand();
 		if (cmd.length() > 4 && cmd.toLowerCase().startsWith("say")) {
 			String message = event.getCommand().substring(4, event.getCommand().length());
 			postIRConce("[Server]", message); // キャスト
 		}
 		if (cmd.length() > 6 && cmd.toLowerCase().startsWith("bcast")) {
 			String message = event.getCommand().substring(6, event.getCommand().length());
 			postIRConce("[Server]", message); // キャスト
 		}
 		if (cmd.length() > 12 && cmd.toLowerCase().startsWith("admin bcast")) {
 			String message = event.getCommand().substring(12, event.getCommand().length());
 			postIRConce("[sys]", message); // キャスト
 		}
 	}
 
 	/**
 	 * チャンネルチャットイベント [HeroChat]
 	 * @param e
 	 */
 	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
 	public void onChannelChat(final ChannelChatEvent e) {
 		if (e.getResult() == Result.ALLOWED) {
 			String message = e.getBukkitEvent().getMessage();
 			String c = null;
 			String n = null;
 
 			if (e.getChannel() instanceof ConversationChannel) {
 				ConversationChannel cc = (ConversationChannel) e.getChannel();
 				c = "-> ";
 				for (Chatter ch : cc.getMembers()) {
 					if (ch == e.getSender()) continue;
 					c = c.concat(ch.getName());
 				}
 				n = c;
 			} else {
 				c = (e.getChannel().getName() == null) ? "" : e.getChannel().getName();
 				n = (e.getChannel().getNick() == null) ? "" : e.getChannel().getNick();
 			}
 
 			for (Bridge b : BridgeManager.getBridges(e.getChannel())) {
 				b.endPoint.messageOut(message, e.getSender().getName(), b.craftIRCTag, c, n);
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
 	public void onPlayerJoin(final PlayerJoinEvent event) {
 		Player p = event.getPlayer();
		postIRConce("*** ", "'" + p.getDisplayName() + "' さんが接続しました！ ("+p.getLocation()+") [/"+p.getAddress().getHostString()+"]");
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
 	public void onPlayerQuit(final PlayerQuitEvent event) {
 		Player p = event.getPlayer();
		postIRConce("*** ", "'" + event.getPlayer().getDisplayName() + "' さんが切断しました！ ("+p.getLocation()+")");
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
 	public void onPlayerKick(final PlayerKickEvent event) {
 		Player p = event.getPlayer();
 		postIRConce("*** ", "'" + p.getDisplayName() + "' はKickされました:["+event.getReason()+"]["+p.getAddress().getHostString()+"]");
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
 	public void onPlayerDeath(final PlayerDeathEvent event) {
 		Player p = event.getEntity();
		postIRConce("** ", "'" + p.getDisplayName() + "' が死にました！ ("+p.getLocation()+")");
 	}
 
 	//***** 以下通常メソッド *****
 
 	private void postIRConce(String sender, String message){
 		for (Bridge b : BridgeManager.bridges) {
 			b.endPoint.messageOut(message, sender, b.craftIRCTag, b.GameChannel.getName(), b.GameChannel.getNick());
 			break; // 同じIRCチャンネルなので1回のみのキャストにする
 		}
 	}
 
 	private String getLocString(Location loc){
 		return loc.getWorld().getName()+": "+loc.getBlockX()+", "+loc.getBlockY()+", "+loc.getBlockZ();
 	}
 
 
 	private String removeCC(String message){
 		// もしかしたらCraftIRC内部で自動消去されてるかも..
 		message = message.replaceAll("&([0-9A-Fa-fk-or])", "");
 		return message;
 	}
 }
