 package com.bukkit.Top_Cat.MIA;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 
 import twitter4j.Status;
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 import twitter4j.TwitterFactory;
 import twitter4j.http.AccessToken;
 import twitter4j.http.RequestToken;
 
 public class MIAFunctions {
     private final MIA plugin;
     
     public MIAFunctions(MIA instance) {
         plugin = instance;
         
         sblock = plugin.getServer().getWorlds().get(0).getBlockAt(409, 4, -354);
         gzone = new Zone(plugin, 0, sblock, sblock, "everywhere", 0, true, true, false, false, 0);
     }
     
     RequestToken requestToken;
     Twitter twitter;
     sqllogin sqllogin = new sqllogin();
     
     public void post_tweet(String s) {
     	Date time = new Date();
     	s += " (" + String.valueOf(Math.round(time.getTime() * 1000)).substring(5) + ")";
     	
         twitter = new TwitterFactory().getInstance();
         AccessToken accessToken = sqllogin.accessToken;
        twitter.setOAuthConsumer(sqllogin.consumerKey, sqllogin.consumerSecret);
         twitter.setOAuthAccessToken(accessToken);
         
         Status status;
 		try {
 			status = twitter.updateStatus(s);
 	        System.out.println("Successfully updated the status to [" + status.getText() + "].");
 		} catch (TwitterException e) {
 			System.out.println("Didn't update status. " + e); 
 			//e.printStackTrace();
 		}
         
     }
     
     public void rebuild_cache() {
     	cache_zones();
     	cache_towns();
     }
     
     public List<Town> towns = new ArrayList<Town>();
     
     public void cache_towns() {
     	towns.clear();
     	PreparedStatement pr;
 		try {
 			String q = "SELECT a.*, b.radius FROM towns as a, town_type as b WHERE a.ttype = b.Id";
 			pr = plugin.conn.prepareStatement(q);
 			ResultSet r = pr.executeQuery();
 			while (r.next()) {
 				String[] cs = r.getString("center").split(",");
 				List<String> usrs = new ArrayList<String>();
 				
 				String q2 = "SELECT name FROM users WHERE town = '" + r.getInt("Id") + "'";
 				pr = plugin.conn.prepareStatement(q2);
 				ResultSet r2 = pr.executeQuery();
 				
 				while (r2.next()) {
 					usrs.add(r2.getString("name"));
 				}
 				
 				Block b = plugin.getServer().getWorlds().get(r.getInt("world")).getBlockAt(Integer.parseInt(cs[0]), 0, Integer.parseInt(cs[1]));
 				Town t = new Town(plugin, r.getInt("Id"), b, r.getString("name"), r.getInt("mayor"), Town.towntypes.TOWN, usrs);
 				towns.add(t);
 				zones.add(t);
 			}
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     }
     
     public int intown(Block b) {
     	Town t = intownR(b);
     	if (t != null) {
     		return intownR(b).getId();
     	}
     	return 0;
     }
     
     public int intown(Location l) {
     	Town t = intownR(l);
     	if (t != null) {
     		return intownR(l).getId();
     	}
     	return 0;
     }
     
     public int intown(Player p) {
     	Town t = intownR(p);
     	if (t != null) {
     		return intownR(p).getId();
     	}
     	return 0;
     }
     
     public Town townR(Player p) {
     	if (towns.size() == 0)
     		rebuild_cache();
     	
     	for (Town i : towns) {
     		if (i.intown(p)) {
 	    		return i;
     		}
     	}
     	return null;
     }
     
     public Town intownR(Block b) {
     	return intownR(b.getX(), b.getZ(), b.getWorld());
     }
     
     public Town intownR(Location l) {
     	return intownR(l.getBlockX(), l.getBlockZ(), l.getWorld());
     }
     
     public Town intownR(Player p) {
     	return intownR(p.getLocation().getBlockX(), p.getLocation().getBlockZ(), p.getWorld());
     }
     
     public Town intownR(int x, int z, World w) {
     	if (towns.size() == 0)
     		rebuild_cache();
     	
     	for (Town i : towns) {
     		if (i.inZone(new Location(w, x, 0, z))) {
 	    		return i;
     		}
     	}
     	return null;
     }
     
     public void updatestats(HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> stats, HashMap<String, HashMap<Integer, HashMap<Integer, Boolean>>> overw) {
     	PreparedStatement pr;
 		String u = "";
 		try {
 			String q = "SELECT * FROM stats";
 			pr = plugin.conn.prepareStatement(q);
 			ResultSet r = pr.executeQuery();
 			while (r.next()){
 				if (stats.containsKey(r.getString("player"))) {
 					if (stats.get(r.getString("player")).containsKey(r.getInt("type"))) {
 						if (stats.get(r.getString("player")).get(r.getInt("type")).containsKey(r.getInt("blockid"))) {
 							int amm = stats.get(r.getString("player")).get(r.getInt("type")).get(r.getInt("blockid"));
 							String ad = "'";
 							if (!overw.get(r.getString("player")).get(r.getInt("type")).get(r.getInt("blockid"))) {
 								ad = "count + '";
 							}
 							u = "UPDATE stats SET count = " + ad + amm + "' WHERE Id = '" + r.getInt("Id") + "'";
 							pr = plugin.conn.prepareStatement(u);
 							//System.out.println(u);
 							pr.executeUpdate();
 							stats.get(r.getString("player")).get(r.getInt("type")).remove(r.getInt("blockid"));					}
 					}
 				}
 			}
 			for (String p : stats.keySet()) {
 				for (Integer p2 : stats.get(p).keySet()) {
 					for (Integer p3 : stats.get(p).get(p2).keySet()) {
 						int amm = stats.get(p).get(p2).get(p3);
 						u = "INSERT INTO stats (player, type, blockid, count) VALUES ('" + p + "', '" + p2 + "', '" + p3 + "', '" + amm + "')";
 						pr = plugin.conn.prepareStatement(u);
 						pr.executeUpdate();
 					}
 				}
 			}
 		} catch (Exception e) {
 			System.out.println("Meow :" + u);
 			e.printStackTrace();
 		}
     }
     
     HashMap<Block, ArrayList<String>> dests = new HashMap<Block, ArrayList<String>>();
     
     public ArrayList<String> getDest(Block sign) {
     	if (dests.containsKey(sign)) {
     		return dests.get(sign);
     	} else {
 	    	PreparedStatement pr;
 	    	ArrayList<String> out = new ArrayList<String>();
 			try {
 				String blk = sign.getX() + "," + sign.getY() + "," + sign.getZ();
 				String q = "SELECT name, cblock FROM stargates WHERE cblock != '" + blk + "' and network = (SELECT network FROM stargates WHERE cblock = '" + blk + "')";
 				pr = plugin.conn.prepareStatement(q);
 				ResultSet r = pr.executeQuery();
 				while (r.next()) {
 					out.add(r.getString("name"));
 				}
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			dests.put(sign, out);
 	    	return out;
     	}
     }
     
     ArrayList<Block> gateblocks = new ArrayList<Block>();
     
     public void getGates() {
     	if (gateblocks.size() == 0) {
 	    	PreparedStatement pr;
 	    	ArrayList<Block> out = new ArrayList<Block>();
 			try {
 				String q = "SELECT cblock, world FROM stargates";
 				pr = plugin.conn.prepareStatement(q);
 				ResultSet r = pr.executeQuery();
 				while (r.next()) {
 					String[] so = r.getString("cblock").split(",");
 					Integer[] l = new Integer[3];
 					for (int i = 0; i < 3; i++) {
 						l[i] = Integer.parseInt(so[i]);
 					}
 					out.add(plugin.getServer().getWorlds().get(r.getInt("world")).getBlockAt(l[0], l[1], l[2]));
 				}
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			gateblocks = out;
     	}
     }
     
     public String gateName(Block sign) {
     	PreparedStatement pr;
 		try {
 			String blk = sign.getX() + "," + sign.getY() + "," + sign.getZ();
 			String q = "SELECT name FROM stargates WHERE cblock = '" + blk + "'";
 			pr = plugin.conn.prepareStatement(q);
 			ResultSet r = pr.executeQuery();
 			if (r.first()) {
 				return r.getString("name");
 			}
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     	return null;
     }
     
     public Integer gateId(String name) {
     	PreparedStatement pr;
 		try {
 			String q = "SELECT Id FROM stargates WHERE name= '" + name + "'";
 			pr = plugin.conn.prepareStatement(q);
 			ResultSet r = pr.executeQuery();
 			if (r.first()) {
 				return r.getInt("Id");
 			}
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     	return 0;
     }
     
     public Integer[] gateSign(Object gateid) {
     	Integer[] out = new Integer[4];
     	HashMap<String, String> inf = gateData(gateid);
 		String[] so = inf.get("cblock").split(",");
 		for (int i = 0; i < 3; i++) {
 			out[i] = Integer.parseInt(so[i]);
 		}
 		out[3] = Integer.parseInt(inf.get("world"));
     	return out;
     }
     
     public HashMap<String, String> gateData(Block fromsign) {
     	getGates();
     	if (fromsign.getType() == Material.STONE_BUTTON) {
     		for (Block i : gateblocks) {
     			if (i.getWorld() == fromsign.getWorld() && (i.getX() == fromsign.getX() && Math.abs(i.getZ() - fromsign.getZ()) == 3) || (i.getZ() == fromsign.getZ() && Math.abs(i.getX() - fromsign.getX()) == 3)) {
     				fromsign = i;
     				break;
     			}
     		}
     	}
     	return gateData(gateId(gateName(fromsign)));
     }
     
     public HashMap<String, String> gateData(String fromsign) {
     	return gateData(gateId(fromsign));
     }
     
     public HashMap<String, String> gateData(Object gateid) {
     	if (gateid instanceof String) {
     		return gateData((String) gateid);
     	} else if (gateid instanceof Block) {
     		return gateData((Block) gateid);
     	} else if (gateid instanceof Integer) {
     		return gateData((Integer) gateid);
     	}
     	return null;
     }
     HashMap<Integer, HashMap<String, String>> gateinfo_cache = new HashMap<Integer, HashMap<String, String>>();
     public HashMap<String, String> gateData(Integer id) {
     	if (gateinfo_cache.containsKey(id)) {
     		return gateinfo_cache.get(id);
     	} else {
 	    	HashMap<String, String> out = new HashMap<String, String>();
 	    	PreparedStatement pr;
 			try {
 				String q = "SELECT * FROM stargates WHERE Id= '" + id + "'";
 				pr = plugin.conn.prepareStatement(q);
 				ResultSet r = pr.executeQuery();
 				if (r.first()) {
 					out.put("Id", r.getString("Id"));
 					out.put("name", r.getString("name"));
 					out.put("world", r.getString("world"));
 					out.put("cblock", r.getString("cblock"));
 					out.put("rot", r.getString("rot"));
 					out.put("network", r.getString("network"));
 				}
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			gateinfo_cache.put(id, out);
 	    	return out;
     	}
     }
     
     public int playertownId(Player p) {
     	return plugin.playerListener.userinfo.get(p.getDisplayName()).getTown();
     }
     
     public void sendmsg(Player rec, String msg) {
 		Player[] pa = new Player[1];
 		pa[0] = rec;
 		sendmsg(pa, msg);
 	}
     
     public void sendmsg(Player[] rec, String msg) {
     	for (Player p : rec) {
     		if (p != null)
     		p.sendMessage(msg);
     	}
     }
     
     public Integer[] intarray(String[] in) {
     	Integer[] out = new Integer[in.length];
     	int k = 0;
     	for (String i : in) {
     		out[k++] = Integer.parseInt(i);
     	}
 		return out;
     }
     
     List<Zone> zones = new ArrayList<Zone>();
     Block sblock;
     Zone gzone;
     
     public void cache_zones() {
     	zones.clear();
     	PreparedStatement pr;
 		try {
 			String q = "SELECT * FROM zones";
 			pr = plugin.conn.prepareStatement(q);
 			ResultSet r = pr.executeQuery()	;
 			while (r.next()) {
 				String c = r.getString("corners");
 				String[] bls = c.split(":");
 				Integer[] cs = intarray(bls[0].split(","));
 				Integer[] cs2 = intarray(bls[1].split(","));
 				Block b1 = plugin.getServer().getWorlds().get(r.getInt("world")).getBlockAt(cs[0], cs[1], cs[2]);
 				Block b2 = plugin.getServer().getWorlds().get(r.getInt("world")).getBlockAt(cs2[0], cs2[1], cs2[2]);
 				zones.add(new Zone(plugin, r.getInt("Id"), b1, b2, r.getString("name"), r.getInt("healing"), r.getBoolean("PvP"), r.getBoolean("mobs"), r.getBoolean("chest"), r.getBoolean("protect"), r.getInt("owner")));
 			}
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     }
     
     public int inzone(Player ps) {
     	return inzoneR(ps).getId();
     }
     
     public Zone inzoneR(Player ps) {
     	return inzoneR(ps.getLocation());
     }
     
     public Zone inzoneR(Block ps) {
     	return inzoneR(ps.getLocation());
     }
     
     public Zone inzoneR(Location ps) {
     	if (zones.size() == 0)
     		rebuild_cache();
     	
     	for (Zone i : zones) {
     		if (i.inZone(ps)) {
     			return i;
     		}
     	}
     	return gzone;
     }
     
     public boolean ownzone(Player p) {
     	return inzoneR(p).ownzone(p);
     }
     
     public void changestock(int zone, int itemid, int itemamm) {
     	PreparedStatement pr;
 		try {
 			String q = "UPDATE ishopitems as a, ishop as b SET stock = stock + '" + itemamm + "' WHERE a.name = b.name and b.zone = '" + zone + "' and a.itemId = '" + itemid + "'";
 			pr = plugin.conn.prepareStatement(q);
 			pr.executeUpdate();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     }
     
     public HashMap<Integer, Integer[]> shopitems(int zone) {
     	HashMap<Integer, Integer[]> out = new HashMap<Integer, Integer[]>();
     	PreparedStatement pr;
 		try {
 			String q = "SELECT a.itemId, a.buy, a.sell, a.stock FROM ishopitems as a, ishop as b WHERE a.name = b.name and b.zone = '" + zone + "'";
 			pr = plugin.conn.prepareStatement(q);
 			ResultSet r = pr.executeQuery();
 			while (r.next()) {
 				Integer[] o = new Integer[3];
 				o[0] = r.getInt("buy");
 				o[1] = r.getInt("sell");
 				o[2] = r.getInt("stock");
 				out.put(r.getInt("itemId"), o);
 			}
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     	return out;
     }
     
     public boolean inowntown(Player p) {
     	if (playertownId(p) == intown(p))
     		return true;
     	return false;
     }
     
     public boolean notothertown(Player p) {
     	if (playertownId(p) == intown(p) || intown(p) == 0)
     		return true;
     	return false;
     }
     
     public void openportal(Block sign) {
     	sign.getWorld().loadChunk(sign.getWorld().getChunkAt(sign));
     	ePortal(sign, Material.FIRE);
     }
     
     public void closeportal(Block sign, World w) {
     	ePortal(sign, Material.AIR);
     	
 		Sign si = (Sign) sign.getState();
 		si.setLine(0, "--" + plugin.mf.gateName(sign) + "--");
 		si.setLine(1, "Right click to");
         si.setLine(2, "use the gate");
         si.setLine(3, " (" + plugin.mf.gateData(sign).get("network") + ") ");
         si.update();
     }
     
     public void spawn(Player p) {
     	World w = p.getWorld();
     	w.loadChunk(467, -325);
     	p.teleportTo(new Location(w, 467d, 114d, -325d, 180, 0));
     }
     
     public void ePortal(Block sign, Material m) {
     	Integer[] l = new Integer[3];
 		l[0] = sign.getX();
 		l[1] = sign.getY();
 		l[2] = sign.getZ();
 		
 		
 		Integer rot = Integer.parseInt(gateData(sign).get("rot"));
 
 		int xo2 = 1;
 		int zo2 = -1;
 		if (rot == 0) {
 			xo2 = -1;
 			zo2 = -1;
 		} else if (rot == 2) {
 			xo2 = 1;
 			zo2 = 1;
 		} else if (rot == 3) {
 			xo2 = -1;
 			zo2 = 1;
 		}
 
 		sign.getWorld().getBlockAt(l[0] + xo2, l[1] - 1, l[2] + zo2).setType(m);
     }
     
     HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>> stats = new HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>>();
     HashMap<String, HashMap<Integer, HashMap<Integer, Boolean>>> overw = new HashMap<String, HashMap<Integer, HashMap<Integer, Boolean>>>();
     
     public void updatestats(Player p, int type, int id) {
     	updatestats(p, type, id, 1, false);
     }
     
     public void updatestats(Player p, int type, int id, int amm) {
     	updatestats(p, type, id, amm, false);
     }
     
     public void updatestats(Player pl, int type, int id, int amm, Boolean overwrite) {
     	String p = pl.getDisplayName();
     	/*if (updatec++ > 100) {
     		updatec = 0;
     		System.out.println("Update, is this too frequent?");
     		updatestats(stats, overw);
     	}*/
 		if (!stats.containsKey(p)){
 			stats.put(p, new HashMap<Integer, HashMap<Integer, Integer>>());
 			overw.put(p, new HashMap<Integer, HashMap<Integer, Boolean>>());
 		}
 		if (!stats.get(p).containsKey(type)){
 			stats.get(p).put(type, new HashMap<Integer, Integer>());
 			overw.get(p).put(type, new HashMap<Integer, Boolean>());
 		}
 		HashMap<Integer, Integer> pblocks = stats.get(p).get(type);
 		overw.get(p).get(type).put(id, overwrite);
 		if (pblocks.containsKey(id)) {
 			pblocks.put(id, pblocks.get(id) + amm);
 		} else {
 			pblocks.put(id, amm);
 		}
     }
     
     public void updatestats() {
     	updatestats(stats, overw);
     }
     
 }
