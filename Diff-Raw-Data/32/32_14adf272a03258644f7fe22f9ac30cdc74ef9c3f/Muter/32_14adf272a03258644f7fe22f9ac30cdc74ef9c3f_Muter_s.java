 package nl.giantit.minecraft.GiantPM.core.Tools.Muter;
 
 import nl.giantit.minecraft.GiantPM.GiantPM;
 import nl.giantit.minecraft.GiantPM.core.Database.db;
 
 import org.bukkit.OfflinePlayer;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.logging.Level;
 import java.util.Map;
 import nl.giantit.minecraft.GiantPM.Misc.Messages;
 
 /**
  *
  * @author Giant
  */
 public class Muter {
 	
 	private static HashMap<OfflinePlayer, Muter> instance = new HashMap<OfflinePlayer, Muter>();
 	
 	private OfflinePlayer p;
	private ArrayList<OfflinePlayer> muted = new ArrayList<OfflinePlayer>();
 	private boolean init = false;
 	
 	private void loadMutes() {
 		db DB = db.Obtain();
 		
 		ArrayList<String> fields = new ArrayList<String>();
 		fields.add("muted");
 		
 		HashMap<String, String> where = new HashMap<String, String>();
 		where.put("owner", p.getName());
 		
 		ArrayList<HashMap<String, String>> mResSet = DB.select(fields).from("#__muted").where(where).execQuery();
 		
 		if(mResSet.size() > 0) {
 			HashMap<String, String> res = mResSet.get(0);
 			if(res.get("muted").contains(";")) {
 				for(String u : res.get("muted").split(";")) {
 					OfflinePlayer m = GiantPM.getPlugin().getSrvr().getOfflinePlayer(u);
 					if(m != null) {
						muted.add(m);
 					}else
 						GiantPM.getPlugin().getLogger().log(Level.WARNING, "Invalid muted player passed! (" + p.getName() + ":" + u + ")");
 				}
 			}
 		}else{
 			init = true;
 		}
 	}
 	
 	
 	private void saveMutes() {
 		db DB = db.Obtain();
 		
 		String m = "";
		for(OfflinePlayer u : muted) {
			m += u.getName() + ";";
 		}
 		
 		if(!init) {
 			HashMap<String, String> tmp = new HashMap<String, String>();
 			tmp.put("muted", m);
 			
 			HashMap<String, String> where = new HashMap<String, String>();
 			where.put("owner", p.getName());
 
 			DB.update("#__muted").set(tmp).where(where).updateQuery();
 		}else{
 			ArrayList<String> fields = new ArrayList<String>();
 			fields.add("owner");
 			fields.add("muted");
 			
 			ArrayList<HashMap<Integer, HashMap<String, String>>> values = new ArrayList<HashMap<Integer, HashMap<String, String>>>();
 			HashMap<Integer, HashMap<String, String>> tmp = new HashMap<Integer, HashMap<String, String>>();
 			
 			int i = 0;
 			for(String field : fields) {
 				HashMap<String, String> temp = new HashMap<String, String>();
 				if(field.equalsIgnoreCase("owner")) {
 					temp.put("data", p.getName());
 					tmp.put(i, temp);
 				}else if(field.equalsIgnoreCase("muted")) {
 					temp.put("data", m);
 					tmp.put(i, temp);
 				}
 
 				i++;
 			}
 			values.add(tmp);
 
 			DB.insert("#__muted", fields, values).updateQuery();
 		}
 	}
 	
 	private Muter(OfflinePlayer p) {
 		this.p = p;
 		this.loadMutes();
 	}
 	
 	public String getMutesString() {
 		String s = "";
 		
 		int i = 0;
		for(OfflinePlayer r : muted) {
 			if(i > 0) {
 				s += GiantPM.getPlugin().getMsgHandler().getMsg(Messages.msgType.MAIN, "muteListCommaSeperator");
 			}
 			i++;
 			
			s += r.getName();
 		}
 		
 		return s;
 	}
 	
 	public MuterResp mute(OfflinePlayer m) {
 		if(m != null) {
			if(!muted.contains(m)) {
				muted.add(m);
 				return new MuterResp(MuterResp.MuterRespType.SUCCESS, "");
 			}else
 				return new MuterResp(MuterResp.MuterRespType.FAIL, "Passed player is already muted!");
 		}else
 			return new MuterResp(MuterResp.MuterRespType.FAIL, "Passed player is null!");
 	}
 	
 	public MuterResp unmute(OfflinePlayer m) {
 		if(m != null) {
			if(muted.contains(m)) {
				muted.remove(m);
 				return new MuterResp(MuterResp.MuterRespType.SUCCESS, "");
 			}else
 				return new MuterResp(MuterResp.MuterRespType.FAIL, "Passed player is not muted!");
 		}else
 			return new MuterResp(MuterResp.MuterRespType.FAIL, "Passed player is null!");
 	}
 	
 	public boolean isMuted(OfflinePlayer m) {
		return muted.contains(m);
 	}
 	
 	public boolean isMutedBy(OfflinePlayer m) {
		return Muter.getMuter(m).isMuted(m);
 	}
 	
 	public static Muter getMuter(OfflinePlayer p) {
 		if(instance.containsKey(p)) {
 			return instance.get(p);
 		}
 		
 		Muter m = new Muter(p);
 		instance.put(p, m);
 		return m;
 	}
 	
 	public static void save() {
 		for(Map.Entry<OfflinePlayer, Muter> m : instance.entrySet()) {
 			m.getValue().saveMutes();
 		}
 	}
 }
