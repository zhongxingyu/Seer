 package me.MnC.MnC_SERVER_MOD;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.TreeMap;
 
 import me.MnC.MnC_SERVER_MOD.chat.ChatHandler;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 public class VipManager
 {
 	//needs table mnc_vip{ id - int auto increment primary key, user_id - int unique, expiration - long}
 	
 	public static final int VIP_PERMANENT = -1;
 	
 	public VipManager()
 	{
 	}
 
 	public void setVipPrefix(String name)
 	{		
 		Player p = MnC_SERVER_MOD.getInstance().getServer().getPlayerExact(name);
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
 	
 	public void onVipLogOn(Player player)
 	{
 		VipUser vip = getVip(player.getName());
 		if(vip.getExpiration() < System.currentTimeMillis()/1000 && vip.getExpiration() != VIP_PERMANENT)
 		{
 			removeVip(player.getName());
 			return;
 		}
		this.flyingVips.put(player.getName(),true);
 		// VIPs should be able to fly right after logging in
 		// (Don't remember that awkward moment logging in in mid-air?)
 		if(isFlyEnabled(player.getWorld().getName()))
 		{
 			player.setAllowFlight(true);
 			player.setFlying(true);
 		}
 	}
 	
 	/**
 	 * 
 	 * @param name the name of the player
 	 * @param duration duration to be added in seconds
 	 * @return true on success, false on failure
 	 */
 	public boolean addVip(String playername,long duration)
 	{
 		VipUser vip = this.getVip(playername);
 		if(vip !=null && vip.getExpiration() == VIP_PERMANENT)
 		{
 			return true; // let infinity + |anything| = infinity 
 		}
 		else
 		{
 			try(PreparedStatement stat = DatabaseManager.getConnection().prepareStatement("INSERT INTO mnc_vip (user_id,expiration) VALUES(?,UNIX_TIMESTAMP(DATE_ADD(FROM_UNIXTIME(?),INTERVAL ? SECOND))) ON DUPLICATE KEY UPDATE expiration = UNIX_TIMESTAMP(DATE_ADD(FROM_UNIXTIME(expiration),INTERVAL ? SECOND))");)
 			{
 				stat.setInt(1, UserManager.getInstance().getUserId(playername));
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
 		}
 		return false;
 	}
 
 	
 	public boolean isVip(String name)
 	{
 		try(PreparedStatement stat = DatabaseManager.getConnection().prepareStatement("SELECT count(*)=1 AS is_vip FROM `mnc_vip` vip LEFT JOIN mnc_users u ON vip.user_id = u.id WHERE u.username_clean = ?");)
 		{
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
 		return false;
 	}
 	
 	public boolean removeVip(String name)
 	{
 		try(PreparedStatement stat = DatabaseManager.getConnection().prepareStatement("DELETE FROM mnc_vip WHERE user_id = (SELECT `id` FROM mnc_users WHERE username_clean = ? LIMIT 1);");)
 		{
 			stat.setString(1, name.toLowerCase());
 			stat.executeUpdate();
 			return stat.getUpdateCount() == 1;
 		}catch(Exception e)
 		{
 			e.printStackTrace();
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
 		Player p = MnC_SERVER_MOD.getInstance().getServer().getPlayerExact(name);
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
 		try(PreparedStatement stat = DatabaseManager.getConnection().prepareStatement("SELECT vip.expiration as expiration, u.username as name FROM `mnc_vip` vip LEFT JOIN mnc_users u ON vip.user_id=u.id WHERE u.username_clean = ?");)
 		{
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
 		try(PreparedStatement stat = DatabaseManager.getConnection().prepareStatement("SELECT count(*)=1 AS is_vip FROM `mnc_vip` WHERE user_id = ? LIMIT 1;"))
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
 		try(PreparedStatement stat = DatabaseManager.getConnection().prepareStatement("INSERT INTO mnc_vip (user_id,expiration) VALUES(?,?) ON DUPLICATE KEY UPDATE expiration = ?");)
 		{
 		    stat.setInt(1, UserManager.getInstance().getUserId(name));
 		    stat.setLong(2, expiration);
 		    stat.setLong(3, expiration);
 		    stat.executeUpdate();
 		    return stat.getUpdateCount()>=1;
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	
 	public ArrayList<VipUser> listAllVips()
 	{
 		ArrayList<VipUser> vips = new ArrayList<VipUser>();
 		try(PreparedStatement stat = DatabaseManager.getConnection().prepareStatement("SELECT vip.expiration as expiration, u.username as name FROM `mnc_vip` vip LEFT JOIN mnc_users u ON vip.user_id=u.id");)
 		{
 			ResultSet result = stat.executeQuery();
 			while(result.next())
 			{
 				vips.add(new VipUser(result.getString("name"),result.getLong("expiration")));
 			}
 		}catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 		return vips;
 	}
 
 	/**
 	 * 
 	 * @param world_name The name of the World
 	 * @return true if VIP fly is enabled in <b>world_name</b>, false otherwise
 	 */
 	public static boolean isFlyEnabled(String world_name)
 	{
 		return world_name.equalsIgnoreCase("world") || world_name.equalsIgnoreCase("world_mine") || world_name.equalsIgnoreCase("world_basic") || world_name.equalsIgnoreCase("world_nether");
 	}
 }
