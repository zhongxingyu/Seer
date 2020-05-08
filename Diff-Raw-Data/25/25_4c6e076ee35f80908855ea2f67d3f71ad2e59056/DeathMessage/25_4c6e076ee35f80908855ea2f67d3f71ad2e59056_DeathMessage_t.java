 package org.Bukkitdev.Xon;
 
 import org.bukkit.ChatColor;
 import org.json.simpleForBukkit.JSONObject;
 
import com.alecgorge.minecraft.jsonapi.api.JSONAPIStreamMessage;
 import com.herocraftonline.squallseed31.heroicdeath.DeathCertificate;
 
 public class DeathMessage extends JSONAPIStreamMessage {
 	private DeathCertificate DeathCertificate;
 	private String streamName;
 	
 	public DeathMessage(String streamName, DeathCertificate DeathCertificate) {
 		this.DeathCertificate = DeathCertificate;
 		this.streamName = streamName;
 		setTime();
 	}
 	
 	@Override
 	public String streamName() {
 		return streamName;
 	}
 
 	@Override
 	public JSONObject toJSONObject() {
 		JSONObject o = new JSONObject();
 		o.put("time", getTime());
 		o.put("defender", DeathCertificate.getDefender());
 		o.put("attacker", DeathCertificate.getAttacker());
 		if (DeathCertificate.getCause() != null)
 		{		
 			o.put("cause", DeathCertificate.getCause().name());
 		}
 		else
 		{
 			o.put("cause", "Unknown");
 		}
 		if (DeathCertificate.getMurderWeapon() != null)
 		{
 			o.put("MurderWeapon", DeathCertificate.getMurderWeapon().getItemType().name());
 		}
 		else
 		{
 			o.put("cause", "None");
 		}		
 		o.put("message", ChatColor.stripColor(DeathCertificate.getMessage()));
 		return o;
 	}
 
 }
 
