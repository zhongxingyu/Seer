 package syam.CraftIRCBridge.Bridge;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 
 import syam.CraftIRCBridge.CraftIRCBridge;
 
 import com.dthielke.herochat.Channel;
 import com.ensifera.animosity.craftirc.EndPoint;
 import com.ensifera.animosity.craftirc.RelayedMessage;
 
 public class BridgeEndPoint implements EndPoint{
 	private Channel GameChannel;
 
 	BridgeEndPoint(Channel GameChannel) {
 		this.GameChannel = GameChannel;
 	}
 
 	@Override
 	public boolean adminMessageIn(RelayedMessage arg0) {
 		return false;
 	}
 
 	@Override
 	public Type getType() {
 		return Type.MINECRAFT;
 	}
 
 	@Override
 	public List<String> listDisplayUsers() {
 		List<String> ret = new ArrayList<String>();
 		for (Player p: Bukkit.getServer().getOnlinePlayers()) {
 			ret.add(p.getName());
 		}
 		return ret;
 	}
 
 	@Override
 	public List<String> listUsers() {
 		List<String> ret = new ArrayList<String>();
 		for (Player p: Bukkit.getServer().getOnlinePlayers()) {
 			ret.add(p.getName());
 		}
 		return ret;
 	}
 
 	@Override
 	public void messageIn(RelayedMessage rm) {
 		if (rm == null || GameChannel == null) return;
 		//if (rm.getEvent() == null) { System.out.println("null event"); return; }
 		//if (rm.getEvent().equalsIgnoreCase("chat")) {
 			/*String sender = rm.getField("sender") == null ? "" : rm.getField("sender");
 			String message = rm.getField("message") == null ? "" : rm.getField("message");
 			GameChannel.announce("(IRC) " + sender + ": " + message);*/
			GameChannel.announce(rm.getMessage(this));
 		//}
 	}
 
 	@Override
 	public boolean userMessageIn(String user, RelayedMessage message) {
 		return false;
 	}
 
 	public void messageOut(String message, String sender, String tag, String channelName, String channelNick) {
 		RelayedMessage rm = CraftIRCBridge.craftIRC.newMsg(this, null, "chat");
 		rm.setField("message", message);
 		rm.setField("sender", sender);
 		rm.setField("channelName", channelName);
 		rm.setField("channelNick", channelNick);
 		rm.post();
 	}
 }
