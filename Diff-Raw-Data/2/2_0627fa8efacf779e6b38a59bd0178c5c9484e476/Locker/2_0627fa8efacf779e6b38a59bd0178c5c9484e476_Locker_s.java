 package me.Guga.Guga_SERVER_MOD;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import org.bukkit.block.Block;
 
 public class Locker 
 {
 	Locker(Guga_SERVER_MOD plugin)
 	{
 		this.plugin = plugin;
 	}
 
 	public static enum LockableBlocks
 	{
 		CHEST(54, "chest"),
 		FURNANCE(61, "furnance"),
 		BURNING_FURNANCE(62, "furnance"),
 		DISPENSER(23, "dispenser");
 
 		LockableBlocks(int blockID, String name)
 		{
 			this.blockID = blockID;
 			this.name = name;
 		}
 		public String getName()
 		{
 			return this.name;
 		}
 		public int getID()
 		{
 			return this.blockID;
 		}
 		public static String getNameByID(int id)
 		{
 			LockableBlocks[] lb = LockableBlocks.values();
 			int i;
 			for(i=0;i<lb.length;i++)
 			{
 				if(lb[i].getID() == id)
 					return lb[i].getName();
 			}
 			return "";
 		}
 		
 		public static boolean isLockableBlock(int id)
 		{
 			for (LockableBlocks lb : LockableBlocks.values())
 			{
 				if(lb.getID() == id)
 					return true;
 			}
 			return false;
 		}
 		
 		private int blockID;
 		private String name;
 	}
 	private Guga_SERVER_MOD plugin;
 
 	
 	public boolean LockBlock(Block block,String owner)
 	{
 		PreparedStatement stat = null;
 		String blockType = LockableBlocks.getNameByID(block.getTypeId());
 		if(blockType == "")
 		{
 			plugin.log.warning("Attempt to lock a non-lockable block");
 			return false;
 		}
 		try{
 			stat = this.plugin.dbConfig.getConection().prepareStatement("INSERT INTO `"+this.plugin.dbConfig.getName()+"`.mnc_chests (owner_id, x, y, z, world,type)" +
 					" SELECT  u.id, ?, ?, ?, ?, ?" +
 					" FROM `"+this.plugin.dbConfig.getName()+"`.mnc_users u WHERE u.username_clean = ? LIMIT 1;");
 			stat.setInt(1, block.getLocation().getBlockX());
 			stat.setInt(2, block.getLocation().getBlockY());
 			stat.setInt(3, block.getLocation().getBlockZ());
 			stat.setString(4, block.getLocation().getWorld().getName());
 			stat.setString(5, blockType);
 			stat.setString(6, owner.toLowerCase());
 			boolean sucess = (stat.executeUpdate()==1);
 			stat.close();
 			return sucess;
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
 	
 	public boolean HasAccessPermission(Block block,String user)
 	{
 		String owner = GetBlockOwner(block);
		if(owner == null || owner.matcher("") || owner.equalsIgnoreCase(user))
 			return true;
 		return false;
 	}
 	
 	public void UnlockBlock(Block block)
 	{
 		PreparedStatement stat = null;
 		try{
 			stat = this.plugin.dbConfig.getConection().prepareStatement("DELETE FROM mnc_chests WHERE mnc_chests.world = ? AND mnc_chests.x = ? AND mnc_chests.z = ? AND mnc_chests.y = ? LIMIT 1");
 			stat.setString(1, block.getLocation().getWorld().getName());
 			stat.setInt(2, block.getLocation().getBlockX());
 			stat.setInt(3, block.getLocation().getBlockZ());
 			stat.setInt(4, block.getLocation().getBlockY());
 			stat.executeUpdate();
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
 	}
 	
 	public String GetBlockOwner(Block block)
 	{
 		PreparedStatement stat = null;
 		String uname = "";
 		try{
 			stat = this.plugin.dbConfig.getConection().prepareStatement("SELECT count(u.username) as count,u.username as username " +
 					"FROM `"+this.plugin.dbConfig.getName()+"`.mnc_chests c " +
 					"LEFT JOIN `"+this.plugin.dbConfig.getName()+"`.mnc_users u ON c.owner_id=u.id " +
 					"WHERE c.world = ? AND c.x = ? AND c.z = ? AND c.y = ?");
 			stat.setString(1, block.getLocation().getWorld().getName());
 			stat.setInt(2, block.getLocation().getBlockX());
 			stat.setInt(3, block.getLocation().getBlockZ());
 			stat.setInt(4, block.getLocation().getBlockY());
 			ResultSet res = stat.executeQuery();
 			if(res.next())
 			{
 				uname = res.getString("username");
 				res.close();
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
 		//TODO: what about door? 
 		return uname;
 	}
 	
 	public boolean IsLocked(Block block)
 	{
 		PreparedStatement stat = null;
 		try{
 			stat = this.plugin.dbConfig.getConection().prepareStatement("SELECT count(*) as count FROM `"+this.plugin.dbConfig.getName()+"`.mnc_chests c " +
 					"WHERE c.world = ? AND c.x = ? AND c.z = ? AND c.y = ?");
 			stat.setString(1, block.getLocation().getWorld().getName());
 			stat.setInt(2, block.getLocation().getBlockX());
 			stat.setInt(3, block.getLocation().getBlockZ());
 			stat.setInt(4, block.getLocation().getBlockY());
 			ResultSet res = stat.executeQuery();
 			if(res.next())
 			{
 				if(res.getInt("count")==1)
 				{
 					res.close();
 					return true;
 				}
 				res.close();
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
 		//TODO: what about door? 
 		return false;
 	}
 
 	void LoadChests()
 	{
 		
 	}
 }
