 package me.Guga.Guga_SERVER_MOD;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.TreeMap;
 
 import me.Guga.Guga_SERVER_MOD.Handlers.ChatHandler;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 public class VipManager
 {
 	//needs table mnc_vip{ id - int auto increment primary key, user_id - int unique, expiration - long}
 	
 	private Guga_SERVER_MOD plugin;
 	
 	public static final int VIP_PERMANENT = -1;
 	
 	public VipManager(Guga_SERVER_MOD plugin)
 	{
 		this.plugin = plugin;
 	}
 
 	public void setVipPrefix(String name)
 	{		
 		Player p = plugin.getServer().getPlayerExact(name);
 		if (p==null)
 			return;
 		
 		if (isVip(name))
 		{
 			ChatHandler.SetPrefix(p, "vip");
 			p.setPlayerListName(ChatColor.GOLD+p.getName());
 		}
 		else
 		{
 			p.setDisplayName(p.getName());
 			p.setPlayerListName(p.getName());
 		}
 	}
 	
 	public void onVipLogOn(String name)
 	{
 		VipUser vip = getVip(name);
		if(vip.getExpiration() < System.currentTimeMillis()/1000 && vip.getExpiration() != VIP_PERMANENT)
 		{
 			removeVip(name);
 			return;
 		}
 		this.flyingVips.put(name,true);
 	}
 	
 	/**
 	 * 
 	 * @param name user name
 	 * @param duration Duration to be added in seconds
 	 * @return true on success, false on failure
 	 */
 	public boolean addVip(String name,long duration)
 	{
 		VipUser vip = this.getVip(name);
 		if(vip !=null && vip.getExpiration() == VIP_PERMANENT)
 		{
 			return true; // let infinity + |anything| = infinity 
 		}
 		else
 		{
 			return _addVip(name,duration);
 		}
 	}
 	
 	/**
 	 * 
 	 * @param name name of the player
 	 * @param duration duration in seconds
 	 * @return
 	 */
 	private boolean _addVip(String name,long duration)
 	{
 		PreparedStatement stat=null;
 		try
 		{
 		    stat = plugin.dbConfig.getConection().prepareStatement("INSERT INTO mnc_vip (user_id,expiration) VALUES(?,UNIX_TIMESTAMP(DATE_ADD(FROM_UNIXTIME(?),INTERVAL ? SECOND))) ON DUPLICATE KEY UPDATE expiration = UNIX_TIMESTAMP(DATE_ADD(FROM_UNIXTIME(expiration),INTERVAL ? SECOND))");
 		    stat.setInt(1, plugin.userManager.getUserId(name));
 		    stat.setLong(2, System.currentTimeMillis()/1000); // current time in seconds is supplied from minecraft server
 		    stat.setLong(3, duration);
 		    stat.setLong(4, duration);
 		    stat.executeUpdate();
 		    return stat.getUpdateCount()>=1;
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 		finally
 		{
 			try {
 				if(stat!=null)
 					stat.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		return false;
 	}
 	
 	public boolean isVip(String name)
 	{
 		PreparedStatement stat=null;
 		try
 		{
 		    stat = plugin.dbConfig.getConection().prepareStatement("SELECT count(*)=1 AS is_vip FROM `mnc_vip` vip LEFT JOIN mnc_users u ON vip.user_id = u.id WHERE u.username_clean = ?");
 		    stat.setString(1, name.toLowerCase());
 		    ResultSet result = stat.executeQuery();
 		    if(result.next())
 		    {
 		    	return result.getBoolean("is_vip");
 		    }
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 		finally{
 			try {
 				stat.close();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		return false;
 	}
 	
 	public boolean removeVip(String name)
 	{
 		PreparedStatement stat = null;
 		try{
 			stat = this.plugin.dbConfig.getConection().prepareStatement("DELETE FROM mnc_vip WHERE user_id = (SELECT `id` FROM mnc_users WHERE username_clean = ? LIMIT 1);");
 			stat.setString(1, name.toLowerCase());
 			stat.executeUpdate();
 			return stat.getUpdateCount() == 1;
 		}catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 		finally
 		{
 			if(stat!=null)
 				try {
 					stat.close();
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 		}
 		return false;
 	}
 	
 	public synchronized boolean addVipItem(Player player,String item)
 	{
 		String[] args = item.split(":");
 		if(args.length > 2)
 		{
 			return false;
 		}
 		int itemId = Integer.valueOf(args[0]);
 		short itemType = (args.length==1)? 0 : Short.valueOf(args[1]);
 		if(VipItems.IsVipItem(itemId))
 		{
 			player.getInventory().addItem(new ItemStack(itemId, 64, itemType));
 			return true;
 		}
 		return false;
 	}
 	
 	public Location GetLastTeleportLoc(String name)
 	{
 		return this.teleportLocations.get(name);
 	}
 	public void SetLastTeleportLoc(String name,Location location)
 	{
 		this.teleportLocations.put(name, location);
 	}
 	
 	
 	public synchronized void setFly(String name,boolean fly)
 	{
 		Player p = plugin.getServer().getPlayerExact(name);
 		if(p==null)
 			return;
 		p.setAllowFlight(fly);
 		p.setFlying(fly);
 		this.flyingVips.put(name, fly);
 	}
 	
 	
 	private TreeMap<String,Boolean> flyingVips = new TreeMap<String,Boolean>();
 	private TreeMap<String,Location> teleportLocations = new TreeMap<String,Location>();
 	
 	public class VipUser
 	{
 		public VipUser(String name,long expiration)
 		{
 			this.name = name;
 			this.expiration = expiration;
 		}
 		
 		public long getExpiration(){ return this.expiration;}
 		public String getName(){ return this.name;}
 		
 		private String name;
 		private long expiration;
 		
 		public String toString(){
 			return String.format("%s expires %s", getName(),(this.expiration==-1)? "NEVER": new Date(expiration).toString());
 		}
 	}
 	
 	public enum VipItems
 	{
 		SAND(12), COBBLESTONE(4), WOODEN_PLANKS(5), STONE(1), DIRT(3), SANDSTONE(24);
 		private VipItems(int id)
 		{
 			this.id = id;
 		}
 		public int GetID()
 		{
 			return this.id;
 		}
 		public static boolean IsVipItem(int itemID)
 		{
 			for (VipItems i : VipItems.values())
 			{
 				if (i.GetID() == itemID)
 					return true;
 			}
 			return false;
 		}
 		private int id;
 	}
 
 	public VipUser getVip(String name)
 	{
 		VipUser vip = null;
 		PreparedStatement stat = null;
 		try{
 			stat = this.plugin.dbConfig.getConection().prepareStatement("SELECT vip.expiration as expiration, u.username as name " +
 				"FROM `mnc_vip` vip LEFT JOIN mnc_users u ON vip.user_id=u.id WHERE u.username_clean = ?");
 			stat.setString(1, name.toLowerCase());
 			ResultSet result = stat.executeQuery();
 			if(result.next())
 			{
 				String vipname = result.getString("name");
 				long expir = result.getLong("expiration");
 				vip = new VipUser(vipname,expir);
 			}
 		}catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 		finally
 		{
 			if(stat!=null)
 				try {
 					stat.close();
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 		}
 		return vip;
 	}
 
 	public void onVipLogOut(String name)
 	{
 		this.flyingVips.remove(name);
 		this.teleportLocations.remove(name);		
 	}
 
 	
 	public boolean isPermanent(long expiration)
 	{
 		return expiration == VIP_PERMANENT;
 	}
 
 	public boolean isVip(int id)
 	{
 		try(PreparedStatement stat = plugin.dbConfig.getConection().prepareStatement("SELECT count(*)=1 AS is_vip FROM `mnc_vip` WHERE user_id = ? LIMIT 1;"))
 		{
 		    stat.setInt(1, id);
 		    ResultSet result = stat.executeQuery();
 		    if(result.next())
 		    {
 		    	return result.getBoolean("is_vip");
 		    }
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	
 	public boolean setVip(String name, long expiration)
 	{
 		PreparedStatement stat=null;
 		try
 		{
 		    stat = plugin.dbConfig.getConection().prepareStatement("INSERT INTO mnc_vip (user_id,expiration) VALUES(?,?) ON DUPLICATE KEY UPDATE expiration = ?");
 		    stat.setInt(1, plugin.userManager.getUserId(name));
 		    stat.setLong(2, expiration);
 		    stat.setLong(3, expiration);
 		    stat.executeUpdate();
 		    return stat.getUpdateCount()>=1;
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 		finally
 		{
 			try {
 				if(stat!=null)
 					stat.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		return false;
 	}
 
 	
 	public ArrayList<VipUser> listAllVips()
 	{
 		ArrayList<VipUser> vips = new ArrayList<VipUser>();
 		PreparedStatement stat = null;
 		try{
 			stat = this.plugin.dbConfig.getConection().prepareStatement("SELECT vip.expiration as expiration, u.username as name " +
 				"FROM `mnc_vip` vip LEFT JOIN mnc_users u ON vip.user_id=u.id");
 			ResultSet result = stat.executeQuery();
 			while(result.next())
 			{
 				vips.add(new VipUser(result.getString("name"),result.getLong("expiration")));
 			}
 		}catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 		finally
 		{
 			if(stat!=null)
 				try {
 					stat.close();
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 		}
 		return vips;
 	}
 }
