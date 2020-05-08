 package de.bdh.kb2;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 
 import de.bdh.kb.util.configManager;
 import de.bdh.kb2.Main;
 
 public class KBArea 
 {
 	public int indoor=0,nobuy=0,ix,iy,iz,bx,by,bz,tx,ty,tz,id,lastpay,bh=0,price,upgradeprice=0,paid,height=0,deep=0,clear=0,cansell=0,miet=0,autofree=0,onlyamount=0,nobuild=1,level,lastonline,noloose,kaufzeit,timestamp,sold;
 	public List<Integer> bot = null;
 	public List<Integer> boh = null;
 	public HashMap<Block,Integer> floor = null;
 	String world = "", pass = "", owner = "", ruleset = "", perm = "", gruppe = "";
 	boolean pvp = false;
 	boolean invers = false;
 	Main m;
 	
 	public KBArea(Main m)
 	{
 		this.m = m;
 	}
 	
 	public boolean loadByID(int id)
 	{
 		boolean ret = true;
 		try
 		{
 			Connection conn = Main.Database.getConnection();
         	PreparedStatement ps;
     		String strg = (new StringBuilder()).append("SELECT world,bx,`by`,bz,tx,ty,tz, price, paid, blockx,blocky,blockz, sold, buyer, pass, ruleset, level, lastonline, lastpay, noloose, kaufzeit, UNIX_TIMESTAMP() as `timestamp` FROM ").append(configManager.SQLTable).append("_krimbuy WHERE id = ? LIMIT 0,1").toString();
     		ps = conn.prepareStatement(strg);
     		ps.setInt(1, id);
     		ResultSet rs = ps.executeQuery();
 			if(rs.next())
 			{
 				this.id = id;
 				this.world = rs.getString("world");
 				this.pass = rs.getString("pass");
 				this.owner = rs.getString("buyer");
 				this.ruleset = rs.getString("ruleset");
 				this.bx = rs.getInt("bx");
 				this.by = rs.getInt("by");
 				this.bz = rs.getInt("bz");
 				this.tx = rs.getInt("tx");
 				this.ty = rs.getInt("ty");
 				this.tz = rs.getInt("tz");
 				this.ix = rs.getInt("blockx");
 				this.iy = rs.getInt("blocky");
 				this.iz = rs.getInt("blockz");
 				this.sold = rs.getInt("sold");
 				this.lastpay = rs.getInt("lastpay");
 				this.price = rs.getInt("price");
 				this.paid = rs.getInt("paid");
 				this.level = rs.getInt("level");
 				this.lastonline = rs.getInt("lastonline");
 				this.noloose = rs.getInt("noloose");
 				this.kaufzeit = rs.getInt("kaufzeit");
 				
 				
 				if(this.ruleset.length() > 0)
 				{
 					PreparedStatement ps2;
 					ps2 = conn.prepareStatement((new StringBuilder()).append("SELECT pvp,indoor,height,deep,miet,autofree,blocks,bottom,controlblockheight,clear,cansell,permissionnode,nobuild,onlyamount,price,gruppe,nobuy FROM ").append(configManager.SQLTable).append("_krimbuy_rules WHERE ruleset = ? AND level = ? LIMIT 0,1").toString());
 					ps2.setString(1, rs.getString("ruleset"));
 					int level = rs.getInt("level");
 					if(level == 0) level = 1;
 					ps2.setInt(2, level);
 					ResultSet rs2 = ps2.executeQuery();
 					if(rs2.next())
 					{
 						this.height = rs2.getInt("height");
 						this.deep = rs2.getInt("deep");
 						this.miet = rs2.getInt("miet");
 						this.autofree = rs2.getInt("autofree");
 						this.bh = rs2.getInt("controlblockheight");
 						this.clear = rs2.getInt("clear");
 						this.cansell = rs2.getInt("cansell");
 						this.onlyamount = rs2.getInt("onlyamount");
 						this.nobuild = rs2.getInt("nobuild");
 						this.perm = rs2.getString("permissionnode");
 						this.upgradeprice = rs2.getInt("price");
 						this.gruppe = rs2.getString("gruppe");
 						this.nobuy = rs2.getInt("nobuy");
 						this.indoor = rs2.getInt("indoor");
 								
 						int prvp = rs2.getInt("pvp");
 						if(prvp == 1)
 							this.pvp = true;
 						
 						if(this.gruppe.length() == 0)
 							this.gruppe = this.ruleset;
 						
 						String blocks = rs2.getString("blocks");
 						this.boh = new ArrayList<Integer>();
 						if(blocks != null && blocks.length() > 0)
 						{
 							String[] tmpBoh = blocks.split(",");
 							for (String bl: tmpBoh) 
 							{
 								if(bl.equalsIgnoreCase("!"))
 									this.invers = true;
 								else
 									this.boh.add(Integer.parseInt(bl));
 							}
 						}
 						
 						String bottom = rs2.getString("bottom");
 						this.bot = new ArrayList<Integer>();
 						if(bottom != null && bottom.length() > 0)
 						{
 							String[] tmpBoh = bottom.split(",");
 							for (String bl: tmpBoh) {
 							   this.bot.add(Integer.parseInt(bl));
 							}
 						}
 						
 					}
 					if(ps2 != null)
 						ps2.close();
 					if(rs2 != null)
 						rs2.close();
 				}
 				
 			} else
 			{
 				ret = false;
 			}
 			
 			if(ps != null)
 				ps.close();
 			if(rs != null)
 				rs.close();
 			return ret;
 		} catch(SQLException e)
 		{
 			System.out.println((new StringBuilder()).append("[KB] unable to get KBArea from ID: ").append(e).toString());
 		}
 		return false;
 	}
 	
 	public boolean isIn(Location l)
 	{
 		return this.isIn(l.getWorld(),l.getBlockX(),l.getBlockY(),l.getBlockZ());
 	}
 	
 	public boolean isIn(World w, int x, int y, int z)
 	{
 		if(bx <= x && by <= y && bz <= z && tx >= x && ty >= y && tz >= z && w.getName().equals(this.world))
 			return true;
 		else
 			return false;
 	}
 	
 	public boolean canPlaceBlock(int id)
 	{
 		if(id == 0)
 			return true;
 		
 		boolean ret;
 		if(this.boh.contains(id))
 			ret = false;
 		else ret = true;
 		
 		
 		if(this.invers)
 			return !ret;
 		else
 			return ret;
 	}
 	
 	public boolean canPlaceBlock(Block b)
 	{
 		return this.canPlaceBlock(b.getTypeId());
 	}
 	
 	public void loadFloor()
     {
     	String strg = "";
     	try
 		{
     		Connection conn = Main.Database.getConnection();
         	PreparedStatement ps;
         	
     		String cmd = (new StringBuilder()).append("SELECT floor FROM ").append(configManager.SQLTable).append("_krimbuy WHERE id=?").toString();
     		ps = conn.prepareStatement(cmd);
     		ps.setInt(1,this.id);
     		ResultSet rs = ps.executeQuery();
     		if(rs.next())
     		{
     			strg = rs.getString("floor");
     		}
 		} catch (SQLException e)
 		{
 			System.out.println((new StringBuilder()).append("[KB] unable to get floor: ").append(e).toString());
 		}
     	
     	if(strg.length() > 0)
     	{
     		this.floor = new HashMap<Block,Integer>();
     		if(strg != null && strg.length() > 0)
     		{
     			String[] tmpFl = strg.split(";");
     			for (String bl: tmpFl) 
     			{
     			   String[] tmpFla = bl.split(",");
     			   this.floor.put(Bukkit.getWorld(this.world).getBlockAt(Integer.parseInt(tmpFla[0]),Integer.parseInt(tmpFla[1]), Integer.parseInt(tmpFla[2])),Integer.parseInt(tmpFla[3]));
     			}
     		}
     	}
     }
 	
 	public Block getInteractBlock()
 	{
 		return Bukkit.getWorld(this.world).getBlockAt(ix, iy, iz);
 	}
 	
 	public String getDim()
 	{
 		String s = "";
		s = (tx-bx+1) + "x"+ (tz-bz+1);
 		return s;
 	}
 	
 	public int getMiet()
 	{
 		if(this.miet == 0) return 0;
 		if(this.miet == 1) return this.price;
 		else return this.miet;
 	}
 	
 	public void clearGS()
 	{
 		int x = bx;
 		int y = by;
 		int z = bz;
 		Block b;
 		Boolean all = false;
 		if(this.clear == 2)
 			all = true;
 		
 		if(all == true)
 		{
 			while(x <= tx)
 			{
 				y = by;
 				while(y <= ty)
 				{
 					z = bz;
 					while(z <= tz)
 					{
 						b = Bukkit.getWorld(this.world).getBlockAt(x, y, z);
 						if(this.canPlaceBlock(b) && (b.getTypeId() != configManager.interactBlock && b.getTypeId() != 19))
 						{
 							//System.out.println((new StringBuilder()).append(ChatColor.RED).append("X,Y,Z").append(x).append(",").append(y).append(",").append(z).toString());
 							b.setTypeId(0);
 						} 
 						++z;
 					}
 					++y;
 				}
 				++x;
 			}
 		}
 		
 		this.loadFloor();
 		
 		if(this.floor == null || this.floor.size() == 0)
 		{
 			Block cb = Bukkit.getWorld(this.world).getBlockAt(this.ix,this.iy,this.iz);
 			
 			cb = cb.getRelative(0, bh * -1, 0);
 			y = cb.getY();
 			x = bx;
 			z = bz;
 			int bottom = this.bot.get(0);
 			while(x <= tx)
 			{
 				z = bz;
 				while(z <= tz)
 				{
 					b = Bukkit.getWorld(this.world).getBlockAt(x, y, z);
 					if(this.canPlaceBlock(b) && b.getTypeId() != configManager.interactBlock)
 					{
 						b.setTypeId(bottom);
 					} 
 					++z;
 				}
 				++x;
 			}
 			System.out.println("[KB] Regenerating Floor from scratch on ID:"+this.id);
 		} else
 		{
 			for (Map.Entry<Block,Integer> entry : this.floor.entrySet())
 	    	{
 				entry.getKey().setTypeId(entry.getValue());
 	    	}
 		}
 	}
 }
