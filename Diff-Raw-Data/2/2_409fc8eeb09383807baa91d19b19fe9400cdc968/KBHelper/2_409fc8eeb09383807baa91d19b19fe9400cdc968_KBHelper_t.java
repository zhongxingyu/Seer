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
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 
 import de.bdh.kb.util.configManager;
 import de.bdh.kb2.KBArea;
 import de.bdh.ks.KSHelper;
 import de.bdh.ks.KrimBlockName;
 
 public class KBHelper 
 {
 	public Main m;
 	public KBHelper(Main m)
 	{
 		this.m = m;
 		this.worlds = new ArrayList<World>();
 		String w = configManager.worlds;
 		if(w != null && w.length() > 0)
 		{
 			String[] tmpBoh = w.split(",");
 			for (String bl: tmpBoh) {
 				if(Bukkit.getWorld(bl) != null)
 				{
 					System.out.println("[KB] Enabled for World: "+bl);
 					this.worlds.add(Bukkit.getWorld(bl));
 				}
 			}
 		}
 		
 		this.loadPubAreas();
 	}
 	
 	public class LotValue { public Integer value; public Long time; };
 	public HashMap<Integer,Boolean> blockedEvent = new HashMap<Integer,Boolean>();
 	public HashMap<String,String> pass = new HashMap<String,String>();
 	public HashMap<String,String> ruleset = new HashMap<String,String>();
 	public HashMap<Player,List<Integer>> userarea = new HashMap<Player,List<Integer>>();
 	public HashMap<Integer,LotValue> values = new HashMap<Integer,LotValue>();
 	public Map<Player, Block> lastBlock = new HashMap<Player, Block>();
 	public List<Integer> pubList;
 	public HashMap<Integer,KBArea> areas = new HashMap<Integer,KBArea>();
 	public List<World> worlds;
 	
 	public void loadPubAreas()
 	{
 		this.pubList = new ArrayList<Integer>();
 		try
 		{
     		Connection conn = Main.Database.getConnection();
         	PreparedStatement ps;
         	
     		String strg = (new StringBuilder()).append("SELECT kb.id as id FROM ").append(configManager.SQLTable).append("_krimbuy as kb,").append(configManager.SQLTable).append("_krimbuy_rules as r WHERE kb.sold = 0 AND r.nobuild = 0 AND kb.ruleset=r.ruleset AND r.level = 1").toString();
     		ps = conn.prepareStatement(strg);
     		ResultSet rs = ps.executeQuery();
     			
     		while(rs.next())
     		{
     			this.pubList.add(rs.getInt("id"));
     		}
     		
     		if(ps != null)
 				ps.close();
 			if(rs != null)
 				rs.close();
 
 		} catch (SQLException e)
 		{
 			System.out.println((new StringBuilder()).append("[KB] unable to get open regions: ").append(e).toString());
 		}
 	}
 	
 	public int getTS()
 	{
 		return (int) (System.currentTimeMillis()  / 1000L);
 	}
 	
 	public void loadPlayerAreas(Player p)
 	{
 		List<Integer> pl = new ArrayList<Integer>();
 		try
 		{
     		Connection conn = Main.Database.getConnection();
         	PreparedStatement ps;
         	
         	
     		String strg = (new StringBuilder()).append("SELECT id,UNIX_TIMESTAMP() as `timestamp`, buyer, ruleset FROM ").append(configManager.SQLTable).append("_krimbuy WHERE buyer=? or (pass=? AND pass != \"\" AND sold=1)").toString();
     		ps = conn.prepareStatement(strg);
     		ps.setString(1,p.getName());
     		if(this.pass.get(p.getName()) != null)
     			ps.setString(2, this.pass.get(p.getName()));
     		else
     			ps.setString(2,"no");
     		ResultSet rs = ps.executeQuery();
     		KBArea a;
     		Boolean loosegs;
     		while(rs.next())
     		{
     			loosegs = false;
     		
     			a = this.getArea(rs.getInt("id"));
     			if(a.miet != 0 && a.owner.equalsIgnoreCase(p.getName()))
     			{
     				int days = ( rs.getInt("timestamp") - a.lastpay) / (60*60*24);
 					if(days > 1)
 					{
 						int daily = 0;
 						if(a.miet == 1) daily = a.price; else daily = a.miet;
 						int money = daily * days;
 						Double prc = new Double(money);
 						
 						if(this.m.econ.getBalance(p.getName()) >= prc)
 						{
 							this.m.econ.withdrawPlayer(p.getName(), prc);
 							if(configManager.lang.equalsIgnoreCase("de"))
 								p.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Dir wurde die Miete von '").append(money).append("'").append(this.m.econ.currencyNamePlural()).append(" für ").append(days).append(" Tag(e) für dein Grundstück eingezogen").toString());
 							else
 								p.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("The daily rental fee of '").append(money).append("'").append(this.m.econ.currencyNamePlural()).append(" for ").append(days).append(" day(s) has been collected").toString());
 
 							PreparedStatement ps3;
 							ps3 = conn.prepareStatement((new StringBuilder()).append("UPDATE ").append(configManager.SQLTable).append("_krimbuy SET lastpay=UNIX_TIMESTAMP() WHERE id = ? LIMIT 1").toString());
 							ps3.setInt(1, rs.getInt("id"));
 							ps3.executeUpdate();
 							a.lastpay = rs.getInt("timestamp");
 							
 							if(ps3 != null)
 								ps3.close();
 						} else
 						{
 							loosegs = true;
 							if(configManager.lang.equalsIgnoreCase("de"))
 							{
 								p.sendMessage((new StringBuilder()).append(ChatColor.RED).append("Du kannst die Miete von '").append(money).append("'").append(this.m.econ.currencyNamePlural()).append(" für dein Grundstück nicht bezahlen.").toString());
 								p.sendMessage((new StringBuilder()).append(ChatColor.RED).append("Dein Grundstück wurde dir entzogen").toString());
 							}
 							else
 							{
 								p.sendMessage((new StringBuilder()).append(ChatColor.RED).append("You can't afford the rental fee of '").append(money).append("'").append(this.m.econ.currencyNamePlural()).append(" for your lot").toString());
 								p.sendMessage((new StringBuilder()).append(ChatColor.RED).append("Your lot has been free'd").toString());
 							
 							}
 							
 							System.out.println("[KB] Player "+rs.getString("buyer") +" lost his lot - not enough money");
 							
 							if(a.clear > 0)
 								a.clearGS();
 							this.freeGS(rs.getInt("id"));
 							this.updateArea(p,a.getInteractBlock());
 							a.loadByID(rs.getInt("id"));
 							
 						}
 					}
     			}
     			if(loosegs == false)
     			{
     				if(this.m.permission != null)
     				{
     					this.m.permission.playerAddTransient(rs.getString("buyer"), "kb.owns."+rs.getString("ruleset"));
     					this.m.permission.playerAddTransient(rs.getString("buyer"), "kb.owns.group."+a.gruppe);
     				}
     				
     				pl.add(rs.getInt("id"));
     			}
     		}
     		
     		this.userarea.put(p, pl);
     		
     		if(ps != null)
 				ps.close();
 			if(rs != null)
 				rs.close();
 
 		} catch (SQLException e)
 		{
 			System.out.println((new StringBuilder()).append("[KB] unable to get regions: ").append(e).toString());
 		}
 	}
 	
 	public List<Integer> getPlayerAreas(Player p)
 	{
 		if(this.userarea.get(p) == null)
 		{
 			this.loadPlayerAreas(p);
 		}
 		
 		return this.userarea.get(p);
 	}
 	
 	public KBArea getArea(int id)
     {
     	if(this.areas.get(id) == null)
     	{
     		KBArea tmp = new KBArea(this.m);
     		if(!tmp.loadByID(id))
     			return null;
     		else
     			this.areas.put(id, tmp);
     		return tmp;
     	} else return this.areas.get(id);
     }
 	
 	public KBArea getAreaByLocation(Location l)
 	{
 		KBArea ret = null;
 		int id = this.getAreaIdByLocation(l);
 		if(id != -1)
 			ret = this.getArea(id);
 		return ret;
 	}
 	
 	public int getAreaIdByLocation(Location l)
 	{
 		int ret = -1;
 		try
 		{
 			Connection conn = Main.Database.getConnection();
         	PreparedStatement ps;
     		String strg = (new StringBuilder()).append("SELECT id FROM ").append(configManager.SQLTable).append("_krimbuy WHERE bx <= ? AND `by` <= ? AND bz <= ? AND tx >= ? AND ty >= ? AND tz >= ? LIMIT 0,1").toString();
     		ps = conn.prepareStatement(strg);
     		ps.setInt(1, l.getBlockX());
     		ps.setInt(2, l.getBlockY());
     		ps.setInt(3, l.getBlockZ());
     		ps.setInt(4, l.getBlockX());
     		ps.setInt(5, l.getBlockY());
     		ps.setInt(6, l.getBlockZ());
     		ResultSet rs = ps.executeQuery();
 			if(rs.next())
 			{
 				ret = rs.getInt("id");
 			} 
 			
 			if(ps != null)
 				ps.close();
 			if(rs != null)
 				rs.close();
 			
 		} catch(SQLException e)
 		{
 			System.out.println((new StringBuilder()).append("[KB] unable to get KBArea from LOC: ").append(e).toString());
 		} 
 		return ret;
 	}
 	
 	public void updateLastOnline(Player p)
 	{
     	try
 		{
     		Connection conn = Main.Database.getConnection();
         	PreparedStatement ps;
         	
     		String strg = (new StringBuilder()).append("UPDATE ").append(configManager.SQLTable).append("_krimbuy SET lastonline = UNIX_TIMESTAMP() WHERE buyer = ?").toString();
     		String strg2 = (new StringBuilder()).append("UPDATE ").append(configManager.SQLTable).append("_krimbuy SET noloose=0 WHERE buyer = ? AND noloose != 2").toString();
 
     		ps = conn.prepareStatement(strg);
     		ps.setString(1,p.getName());
     		ps.executeUpdate();
     		
     		ps = conn.prepareStatement(strg2);
     		ps.setString(1,p.getName());
     		ps.executeUpdate();
     		
     		if(ps != null)
 				ps.close();
 		} catch (SQLException e)
 		{
 			System.out.println((new StringBuilder()).append("[KB] unable to update last online region: ").append(e).toString());
 		}
 	}
 	
 	public boolean canPVPHere(Player p)
 	{
 		if(!this.worlds.contains(p.getWorld()))
     		return true;
     	
 		if(p.hasPermission("kb.neverpvp") && !p.isOp())
 			return false;
 		
 		if(p.hasPermission("kb.pvp") && !p.isOp())
 			return true;
 
     	if(configManager.worldLimit.get(p.getWorld()) != null)
 		{
 			if(!configManager.worldLimit.get(p.getWorld()).isIn(p.getLocation(),p))
 			{
 				if(configManager.basepvp == false)
 					return true;
 			}
 			
 			else if(configManager.worldLimit.get(p.getWorld()).blockpvp == false)
 				return true;
 		} else if(configManager.basepvp == false)
 			return true;
     	
 		KBArea item = null;
 		List<Integer> li = this.getPlayerAreas(p);
     	if(li != null)
     	{
     		for (Integer i: li) 
     		{	
     			item = this.getArea(i);
     			if(item != null && item.pvp == true)
     			{
     				String pw = this.pass.get(p.getName());
     				if((pw != null && item.pass.length() > 0 && item.pass.equals(pw)) || item.owner.equals(p.getName()))
     				{
 		    			if(item.isIn(p.getLocation()))
 		    			{
 		    				if(item.indoor == 1)
 		    				{
 		    					if(p.getWorld().getHighestBlockAt(p.getLocation()).getY() > p.getLocation().getBlockY())
 		    						return true;
 		    				} else
 		    					return true;
 		    			}
     				}
     			}
     		}
     	}
     	
     	for(Integer i: this.pubList)
     	{
     		item = this.getArea(i);
     		if(item != null)
 			{
     			if(item.pvp == true)
     			{
     				if(item.isIn(p.getLocation()))
 	    			{
     					if(this.hasPerm(p, item.perm))
     					{
     						if(item.indoor == 1)
 		    				{
 		    					if(p.getWorld().getHighestBlockAt(p.getLocation()).getY() > p.getLocation().getBlockY())
 		    						return true;
 		    				} else
 		    					return true;
     					}
 	    			}
     			}
 			}
     	}
     	
     	return false;
 	}
 	
 	public boolean canBuildHereData(Player p, Block b, boolean interact)
 	{	
 		KBArea item = null;
     	List<Integer> li = this.getPlayerAreas(p);
     	if(li != null)
     	{
     		for (Integer i: li) 
     		{	
     			item = this.getArea(i);
     			if(item != null)
     			{
     				String pw = this.pass.get(p.getName());
     				if((pw != null && item.pass.length() > 0 && item.pass.equals(pw)) || item.owner.equals(p.getName()))
     				{
 		    			if(item.isIn(b.getLocation()))
 		    			{
 		    				if(b == null || item.canPlaceBlock(b) || interact == true )
 		    				{
 		    					if(item.indoor == 1)
 			    				{
 			    					if(b.getWorld().getHighestBlockAt(b.getLocation()).getY() > b.getLocation().getBlockY())
 			    						return true;
 			    				} else
 			    					return true;
 		    				}
 		    			}
     				 }
     			}
     		}
     	}
     	
     	for(Integer i: this.pubList)
     	{
     		item = this.getArea(i);
     		if(item != null)
 			{
     			if(item.nobuild == 0)
     			{
     				if(item.isIn(b.getLocation()))
 	    			{
     					if(this.hasPerm(p, item.perm))
     					{
     						if(b == null || item.canPlaceBlock(b) || interact == true)
     						{
     							if(item.indoor == 1)
 			    				{
 			    					if(b.getWorld().getHighestBlockAt(b.getLocation()).getY() > b.getLocation().getBlockY())
 			    						return true;
 			    				} else
     	    					return true;
     						}
     					}
 	    			}
     			}
 			}
     	}
     	
     	return false;
 	}
 	
 	public boolean canBuildHere(Player p, Block b, boolean interact)
 	{
 		if(!this.worlds.contains(p.getWorld()))
     		return true;
 		
		if(p.hasPermission("kb.notlot") && !p.isOp())
 		{
 			int a = this.getAreaIdByLocation(b.getLocation());
 			if(a != -1)
 			{
 				return false;
 			}
 		}
 		
 		if(p.hasPermission("kb.build")) return true;
     	if(b.getTypeId() == 328) return true;
     	
     	
 		
     	if(configManager.worldLimit.get(p.getWorld()) != null)
 		{
 			if(!configManager.worldLimit.get(p.getWorld()).isIn(b.getLocation(),p))
 				return true;
 		}
     	
     	if(p.hasPermission("kb.disableLots"))
     		return false;
     	else
     		return this.canBuildHereData(p, b, interact);
 	}
 	
 	public void passwordChanged(int id)
 	{
 		KBArea a = this.getArea(id);
 		if(a != null)
 		{
 			for (Player p: Bukkit.getServer().getOnlinePlayers()) 
 	        {
 				List<Integer> tmp = this.userarea.get(p);
 				if(tmp != null)
 				{
 					if(a.pass.length() > 0 && this.pass.get(p.getName()) != null && a.pass.equals(this.pass.get(p.getName())))
 					{
 						//Passwort stimmt überein
 						if(!tmp.contains(id))
 						{
 							tmp.add(id);
 							this.userarea.put(p, tmp);
 						}
 					} else
 					{
 						//Passwort stimmt nicht | keins vorhanden
 						if(tmp.contains(id))
 							tmp.remove((Object)id);
 						this.userarea.put(p, tmp);
 					}
 				}
 	        }
 		} else
 		{
 			System.out.println((new StringBuilder()).append("[KB] unable to func passwordChanged: not found ").append(id).toString());
 		}
 	}
 	
 	public void obtainGS(int id, String p)
 	{
 		KBArea a = this.getArea(id);
 		if(a != null)
 		{
 			a.owner = p;
 			a.pass = "";
 			a.sold = 1;
 			a.paid = a.price;
 			a.kaufzeit = this.getTS();
 			a.lastonline = this.getTS();
 			a.lastpay = this.getTS();
 			
 			Player plx = Bukkit.getServer().getPlayer(p);
 			if(plx != null && this.userarea.get(plx) != null)
 			{
 				List<Integer> tmp = this.userarea.get(plx);
 				tmp.add(id);
 				this.userarea.put(plx, tmp);
 			}
 			
 			//Nicht mehr in der Publiste vorhanden
 			if(this.pubList.contains((Object)id))
 			{
 				this.pubList.remove((Object)id);
 			}
 			
 			//Give Perms
 			if(this.m.permission != null)
 			{
 				this.m.permission.playerAddTransient(p, "kb.owns."+a.ruleset);
 				this.m.permission.playerAddTransient(p, "kb.owns.group."+a.gruppe);
 			}
 			
 			if(a.getInteractBlock() != null)
 			{
 				Block b = a.getInteractBlock();
 				if(b.getTypeId() != 0)
 				{
 					if(b.getRelative(BlockFace.UP).getTypeId() == Material.SPONGE.getId())
 						b.getRelative(BlockFace.UP).setTypeId(0);
 				}
 			}
 			
 			try
 			{
 				Connection conn = Main.Database.getConnection();
 				String strg = (new StringBuilder()).append("UPDATE ").append(configManager.SQLTable).append("_krimbuy SET pass=\"\",level=1,sold=1, buyer=?, paid=?, kaufzeit=UNIX_TIMESTAMP(), lastonline=UNIX_TIMESTAMP(), lastpay=UNIX_TIMESTAMP() WHERE id = ?").append(" LIMIT 1").toString();
 	        	PreparedStatement ps = conn.prepareStatement(strg);
 				ps.setString(1,p);
 				ps.setInt(2,a.price);
 				ps.setInt(3,a.id);
 				ps.executeUpdate();
 			} catch(SQLException e)
 			{
 				System.out.println((new StringBuilder()).append("[KB] unable to func obtainGS: ").append(e).toString());
 			}
 			
 		} else
 		{
 			System.out.println((new StringBuilder()).append("[KB] unable to func obtainGS: not found ").append(id).toString());
 		}
 	}
 	
 	public void freeGS(int id)
 	{
 		KBArea a = this.getArea(id);
 		if(a != null)
 		{
 			Player plx = Bukkit.getServer().getPlayer(a.owner);
 			if(plx != null && this.userarea.get(plx) != null)
 			{
 				List<Integer> tmp = this.userarea.get(plx);
 				tmp.remove((Object)id);
 				this.userarea.put(plx, tmp);
 			}
 			
 			//Take Perms
 			if(this.m.permission != null)
 			{
 				this.m.permission.playerRemoveTransient(a.owner, "kb.owns."+a.ruleset);
 				this.m.permission.playerRemoveTransient(a.owner, "kb.owns.group."+a.gruppe);
 			}
 			
 			a.owner = "";
 			a.pass = "";
 			a.sold = 0;
 			a.level = 1;
 			a.kaufzeit = 0;
 			a.lastpay = 0;
 			a.lastonline = 0;
 			
 			this.passwordChanged(id);
 			
 			if(a.getInteractBlock() != null)
 			{
 				Block b = a.getInteractBlock();
 				if(b.getTypeId() != 0 && configManager.doSponge == 1)
 				{
 					b.getRelative(BlockFace.UP).setTypeId(Material.SPONGE.getId());
 				}
 				
 				if(configManager.doSign == 1)
 				{
 					if(configManager.breakSign == 1)
 					{
 						Block sign = this.near(b, Material.WALL_SIGN.getId());
 						if(sign == null)
 							this.near(b, Material.SIGN_POST.getId());
 						if(sign != null)
 							sign.breakNaturally();
 					} else
 						this.updateSign(b);
 				}
 			}
 			
 			if(a.nobuild == 0)
 			{
 				this.pubList.add(id);
 			}
 			
 			try
 			{
 				Connection conn = Main.Database.getConnection();
 				String strg = (new StringBuilder()).append("UPDATE ").append(configManager.SQLTable).append("_krimbuy SET pass=\"\",level=1,sold=0, buyer=\"\", paid=0, kaufzeit=0, lastonline=0, lastpay=0 WHERE id = ?").append(" LIMIT 1").toString();
 	        	PreparedStatement ps = conn.prepareStatement(strg);
 				ps.setInt(1,a.id);
 				ps.executeUpdate();
 			} catch(SQLException e)
 			{
 				System.out.println((new StringBuilder()).append("[KB] unable to func freeGS: ").append(e).toString());
 			}
 			
 		} else
 		{
 			System.out.println((new StringBuilder()).append("[KB] unable to func freeGS: not found ").append(id).toString());
 		}
 	}
 	
 	public void killGS(int id)
     {
 		KBArea a = this.getArea(id);
 		if(a != null)
 		{
 			if(a.sold == 1)
 			{
 				a.pass = "";
 				this.passwordChanged(id);
 				
 				Player plx = Bukkit.getServer().getPlayer(a.owner);
 				if(plx != null && this.userarea.get(plx) != null)
 				{
 					List<Integer> tmp = this.userarea.get(plx);
 					tmp.remove((Object)id);
 					this.userarea.put(plx, tmp);
 				}
 			} else
 			{
 				if(this.pubList.contains(id))
 					this.pubList.remove((Object)id);
 			}
 			
 			if(a.getInteractBlock() != null)
 			{
 				Block b = a.getInteractBlock();
 				if(b.getTypeId() != 0)
 				{
 					if(b.getRelative(BlockFace.UP).getTypeId() == Material.SPONGE.getId())
 						b.getRelative(BlockFace.UP).setTypeId(0);
 					b.setTypeId(0);
 				}
 				
 				if(configManager.doSign == 1)
 				{
 					if(configManager.breakSign == 1)
 					{
 						Block sign = this.near(b, Material.WALL_SIGN.getId());
 						if(sign == null)
 							this.near(b, Material.SIGN_POST.getId());
 						if(sign != null)
 							sign.breakNaturally();
 					} 
 				}
 			}
 			
 			
 			
 			try
 			{
 				Connection conn = Main.Database.getConnection();
 				String strg = (new StringBuilder()).append("DELETE FROM ").append(configManager.SQLTable).append("_krimbuy WHERE id = ?").append(" LIMIT 1").toString();
 	        	PreparedStatement ps = conn.prepareStatement(strg);
 				ps.setInt(1,a.id);
 				ps.executeUpdate();
 			} catch(SQLException e)
 			{
 				System.out.println((new StringBuilder()).append("[KB] unable to func killGS: ").append(e).toString());
 			}
 			this.areas.remove(id);
 			
 		} else
 		{
 			System.out.println((new StringBuilder()).append("[KB] unable to func killGS: not found ").append(id).toString());
 		}
     }
 	
 	public int getIDbyBlock(Block b)
 	{
 		int ret = 0;
 		try
 		{
 			Connection conn = Main.Database.getConnection();
         	PreparedStatement ps;
     		String strg = (new StringBuilder()).append("SELECT id FROM ").append(configManager.SQLTable).append("_krimbuy WHERE blockx = ? AND blocky = ? AND blockz = ? AND world = ? LIMIT 0,1").toString();
     		ps = conn.prepareStatement(strg);
     		ps.setInt(1, b.getX());
     		ps.setInt(2, b.getY());
     		ps.setInt(3, b.getZ());
     		ps.setString(4, b.getWorld().getName());
     		ResultSet rs = ps.executeQuery();
 			if(rs.next())
 			{
 				ret = rs.getInt("id");
 			} 
 			if(ps != null)
 				ps.close();
 			if(rs != null)
 				rs.close();
 			
 		} catch (SQLException e) 
 		{ 
 			System.out.println((new StringBuilder()).append("[KB] unable to get ID from block: ").append(e).toString());
 	    }
 		return ret;
 	}
 	
 	public boolean hasPerm(Player p, String perm)
 	{
 		boolean hasperm = false;
 		boolean tmt = true;
 		String[] tmperms;
 		if(perm.length() > 0)
 		{
 			if(perm.contains(","))
 			{
 				tmperms = perm.split(",");
 			} else
 				tmperms = new String[] { perm };
 				
 
 			for (String tmperm: tmperms) 
 			{
 				if(!tmperm.startsWith("&") && !tmperm.startsWith("!"))
 					if(p.hasPermission(tmperm))
 						hasperm = true;
 			}
 			
 			for (String tmperm: tmperms) 
 			{
 				if(tmperm.startsWith("&"))
 				{
 					
 					if(!p.hasPermission(tmperm.substring(1,tmperm.length())))
 						tmt = false;
 					else
 						hasperm = true;
 				}
 			}
 			
 			if(tmt == false)
 				hasperm = false;
 			
 			for (String tmperm: tmperms) 
 			{
 				if(tmperm.startsWith("!"))
 				{
 					if(p.hasPermission(tmperm.substring(1,tmperm.length())))
 						hasperm = false;
 					else if(tmperms.length == 1)
 						hasperm = true;
 				}
 			}
 		} else hasperm = true;
 		
 		return hasperm;
 	}
 	public int canUpgradeArea(Player p, Block b)
     {
     	int ret = 0;
     	try
 		{
 			Connection conn = Main.Database.getConnection();
         	PreparedStatement ps;
         	
     		String strg = (new StringBuilder()).append("SELECT ruleset,level,blockx,blocky,blockz,tx,ty,tz,bx,`by`,bz,buyer FROM ").append(configManager.SQLTable).append("_krimbuy WHERE blockx = ? AND blocky = ? AND blockz = ? AND world = ? LIMIT 0,1").toString();
     		ps = conn.prepareStatement(strg);
     		ps.setInt(1, b.getX());
     		ps.setInt(2, b.getY());
     		ps.setInt(3, b.getZ());
     		ps.setString(4,b.getWorld().getName());
     		
     		ResultSet rs = ps.executeQuery();
 			if(rs.next())
 			{
 				if(rs.getString("buyer").equalsIgnoreCase(p.getName()))
 				{
 					if(!rs.getString("ruleset").equals("0") && rs.getInt("level") != 0 && rs.getString("ruleset").length() > 0)
 					{
 						PreparedStatement ps2;
 						ps2 = conn.prepareStatement((new StringBuilder()).append("SELECT price,permissionnode FROM ").append(configManager.SQLTable).append("_krimbuy_rules WHERE ruleset = ? AND level = ? LIMIT 0,1").toString());
 						ps2.setString(1, rs.getString("ruleset"));
 						ps2.setInt(2, (rs.getInt("level")+1));
 						ResultSet rs2 = ps2.executeQuery();
 						if(rs2.next())
 						{
 							if(!rs2.getString("permissionnode").equals(""))
 							{
 								if(this.hasPerm(p, rs2.getString("permissionnode")))
 									ret = rs2.getInt("price");
 								else
 									ret = 0;
 							} else
 								ret = rs2.getInt("price");
 						}
 						
 						if(ps2 != null)
 							ps2.close();
 						if(rs2 != null)
 							rs2.close();
 					}
 				}
 			}
 			
 			if(ps != null)
 				ps.close();
 			
 			if(rs != null)
 				rs.close();
 		} catch (SQLException e)
 		{
 			System.out.println((new StringBuilder()).append("[KB] unable to get upgrade details: ").append(e).toString());
 		}
     	return ret;
     }
 	
 	public void updateSign(Block b)
 	{
 		Block sign = null, interact = null;
 		if(b.getTypeId() == configManager.interactBlock)
 		{
 			interact = b;
 		} else if(b.getType() == Material.WALL_SIGN || b.getType() == Material.SIGN_POST)
 		{
 			sign = b;
 		}
 		
 		if(sign == null && interact == null) return;
 		if(sign == null)
 		{
 			//Suche Schild
 			sign = this.near(interact,Material.SIGN_POST.getId());
 			if(sign == null)
 				sign = this.near(interact,Material.WALL_SIGN.getId());
 			if(sign == null)
 				return;
 		}
 
 		if(interact == null)
 		{
 			interact = this.near(sign,configManager.interactBlock);
 			if(interact == null)
 				return;
 		}
 		
 		int id = this.getIDbyBlock(interact);
 		
 		if(id != 0)
 		{
 			if(sign.getState() instanceof Sign)
 			{
 				KBArea a = this.getArea(id);
 				Sign e = (Sign)sign.getState();
 				int value = 0;
 				
 				if(this.m.KShelper != null && Bukkit.getWorld(a.world) != null)
 				{
 					LotValue v = this.values.get(id);
 					if(v != null)
 					{
 						if(System.currentTimeMillis() - v.time < 1000*60*60)
 						{
 							value = v.value;
 						} else
 							value = -1;
 					}
 					
 					if(value == -1 || v == null)
 					{
 						Block tmp = null;
 						double val = 0.0;
 						value = 0;
 						for(int x = a.bx; x <= a.tx; ++x)
 						{
 							for(int y = a.by; y <= a.ty; ++y)
 							{
 								for(int z = a.bz; z <= a.tz; ++z)
 								{
 									tmp = Bukkit.getWorld(a.world).getBlockAt(x, y, z);
 									if(tmp != null && tmp.getType() != Material.AIR && tmp.getTypeId() != configManager.interactBlock)
 									{
 										val = ((KSHelper)this.m.KShelper).getDurchschnitsspreis(KrimBlockName.getStackByBlock(tmp), 14);
 										value += Math.floor(val);
 									}
 								}
 							}
 						}
 						
 						v = new LotValue();
 						v.time = System.currentTimeMillis();
 						v.value = value;
 						this.values.put(id,v);
 					}
 				}
 				
 				if(a.sold == 1)
 				{
 					e.setLine(0, "'"+a.owner+"'");
 					
 					if(configManager.lang.equalsIgnoreCase("de"))
 					{
 						if(a.level > 1)
 							e.setLine(1, "Typ: "+a.gruppe+ " "+a.level);
 						else
 							e.setLine(1, "Typ: "+a.gruppe);
 						e.setLine(2,"Preis: "+a.paid);
 						if(value != 0)
 							e.setLine(3, "Wert: "+value);
 					} else
 					{
 						if(a.level > 1)
 							e.setLine(1, "Type: "+a.gruppe+ " "+a.level);
 						else
 							e.setLine(1, "Type: "+a.gruppe);
 						e.setLine(2,"Price: "+a.paid);
 						if(value != 0)
 							e.setLine(3, "Value: "+value);
 					}
 				} else
 				{
 					int ln = 1;
 					
 					if(configManager.lang.equalsIgnoreCase("de"))
 					{
 						if(a.getMiet() != 0)
 						{
 							ln = 0;
 							e.setLine(3, "Miete: "+a.getMiet());
 						} else
 						{
 							e.setLine(0,"Zum Verkauf");
 						}
 						e.setLine(ln, "Preis: "+a.price);
 						e.setLine(ln+1, "Typ: "+a.gruppe);
 						e.setLine(ln+2, "Dim: "+a.getDim());
 					} else
 					{
 						if(a.getMiet() != 0)
 						{
 							ln = 0;
 							e.setLine(3, "Rental: "+a.getMiet());
 						} else
 						{
 							e.setLine(0,"For Sale");
 						}
 						e.setLine(ln, "Price: "+a.price);
 						e.setLine(ln+1, "Type: "+a.gruppe);
 						e.setLine(ln+2, "Dim: "+a.getDim());
 					}
 				}
 				
 				e.update();
 			}
 		}
 	}
 	
 	public Block near(Block b,int type)
 	{
 		BlockFace faces[] = 
 		{
             BlockFace.EAST_NORTH_EAST, BlockFace.EAST_SOUTH_EAST, BlockFace.NORTH_EAST, BlockFace.NORTH_NORTH_EAST, BlockFace.NORTH_NORTH_WEST, BlockFace.SOUTH_EAST, BlockFace.SOUTH_SOUTH_EAST, BlockFace.SOUTH_SOUTH_WEST, BlockFace.SOUTH_WEST, BlockFace.WEST_NORTH_WEST, BlockFace.WEST_SOUTH_WEST, BlockFace.NORTH, BlockFace.WEST, BlockFace.EAST, BlockFace.SOUTH, BlockFace.UP, BlockFace.DOWN
         };
 		
 		if(b.getTypeId() == type)
 			return b;
 		
 		int len$ = faces.length;
 		Block temp = null;
 		
         for(int i$ = 0; i$ < len$; i$++)
         {
         	temp = b.getRelative(faces[i$]);
         	if(temp.getTypeId() == type)
         		return temp;
         }
         
         return null;
 	}
 	
 	public void updateArea(Player p, Block b)
 	{
 		try
 		{
 			Connection conn = Main.Database.getConnection();
         	PreparedStatement ps;
     		String strg = (new StringBuilder()).append("SELECT ruleset,level,sold,id FROM ").append(configManager.SQLTable).append("_krimbuy WHERE blockx = ? AND blocky = ? AND blockz = ? AND world = ? LIMIT 0,1").toString();
     		ps = conn.prepareStatement(strg);
     		ps.setInt(1, b.getX());
     		ps.setInt(2, b.getY());
     		ps.setInt(3, b.getZ());
     		ps.setString(4, b.getWorld().getName());
     		ResultSet rs = ps.executeQuery();
 			if(rs.next())
 			{
 				if(rs.getInt("sold") == 0 && configManager.doSponge == 1)
 				{
 					b.getRelative(BlockFace.UP).setTypeId(19);
 				}
 				
 				if(rs.getInt("level") != 0 && !rs.getString("ruleset").equals("0"))
 				{
 					//Kaufdings hat ein Level und ein Ruleset
 					PreparedStatement ps2;
 					ps2 = conn.prepareStatement((new StringBuilder()).append("SELECT nobuild,controlblockheight,blocks,bottom,height,deep FROM ").append(configManager.SQLTable).append("_krimbuy_rules WHERE ruleset = ? AND level = ? LIMIT 0,1").toString());
 					ps2.setString(1, rs.getString("ruleset"));
 					ps2.setInt(2, rs.getInt("level"));
 					ResultSet rs2 = ps2.executeQuery();
 					boolean found = false;
 					if(rs2.next())
 					{
 						found = true;
 						List<Integer> bot = new ArrayList<Integer>();
 						if(rs2.getString("bottom") != null && rs2.getString("bottom").length() > 0)
 						{
 							String[] tmpBot = rs2.getString("bottom").split(",");
 							for (String bl: tmpBot) {
 							   bot.add(Integer.parseInt(bl));
 							}
 						}
 						
 						Block begin = b.getRelative(0,(rs2.getInt("controlblockheight") * -1),0);
 						if(bot.contains(begin.getTypeId()))
 						{
 							Block unter = begin.getRelative(0, 0, 0);
 							Block uber = begin.getRelative(0, 0, 0);
 							Block temp;
 							
 							//OK kann losgehen
 							temp = unter;
 							while(bot.contains(temp.getTypeId()))
 							{
 								unter = temp;
 								temp = unter.getRelative(-1, 0, 0);
 							}
 							
 							temp = unter;
 							while(bot.contains(temp.getTypeId()))
 							{
 								unter = temp;
 								temp = unter.getRelative(0, 0, -1);
 							}
 
 							temp = uber;
 							while(bot.contains(temp.getTypeId()))
 							{
 								uber = temp;
 								temp = uber.getRelative(1, 0, 0);
 							}
 							
 							temp = uber;
 							while(bot.contains(temp.getTypeId()))
 							{
 								uber = temp;
 								temp = uber.getRelative(0, 0, 1);
 							}
 							
 							//oberster und unterster Eintrag gefunden
 							if((uber.getY() + rs2.getInt("height")) > (uber.getWorld().getMaxHeight() - 1))
 								uber = uber.getRelative(0, uber.getWorld().getMaxHeight() - 1 - uber.getY(), 0);
 							else
 								uber = uber.getRelative(0, rs2.getInt("height"), 0);
 							
 							if((unter.getY() - rs2.getInt("deep")) < 0)
 									unter = unter.getRelative(0,0 - unter.getY(),0);
 							else
 								unter = unter.getRelative(0, (rs2.getInt("deep")*-1), 0);
 							
 							//Aktualisiere Eintrag
 							ps2 = conn.prepareStatement((new StringBuilder()).append("UPDATE ").append(configManager.SQLTable).append("_krimbuy SET tx=?,ty=?,tz=?,bx=?,`by`=?,bz=? WHERE blockx=? AND blocky=? AND blockz=? AND world = ? LIMIT 1").toString());
 							ps2.setInt(1, uber.getX());
 				    		ps2.setInt(2, uber.getY());
 				    		ps2.setInt(3, uber.getZ());
 				    		ps2.setInt(4, unter.getX());
 				    		ps2.setInt(5, unter.getY());
 				    		ps2.setInt(6, unter.getZ());
 							ps2.setInt(7, b.getX());
 				    		ps2.setInt(8, b.getY());
 				    		ps2.setInt(9, b.getZ());
 				    		ps2.setString(10, b.getWorld().getName());
 				    		ps2.executeUpdate();
 				    		
 				    		
 				    		//NoBuild add to Pub List
 				    		if(rs2.getInt("nobuild") == 0 && rs.getInt("sold") == 0)
 				    		{
 				    			this.pubList.add(rs.getInt("id"));
 				    		}
 				    		
 				    		//Suche Boden (Hoechstes Level)
 							PreparedStatement ps3;
 							ps3 = conn.prepareStatement((new StringBuilder()).append("SELECT bottom FROM ").append(configManager.SQLTable).append("_krimbuy_rules WHERE ruleset = ? ORDER BY level DESC LIMIT 0,1").toString());
 							ps3.setString(1, rs.getString("ruleset"));
 							ResultSet rs3 = ps3.executeQuery();
 							String floor = "";
 							if(rs3.next())
 							{
 								List<Block> tmp = this.getBottom(b,rs2.getInt("controlblockheight"),rs3.getString("bottom"));
 								if(p != null)
 								{
 									if(p.hasPermission("kb.admin"))
 									{
 										int vol = (uber.getX() - unter.getX()) * (uber.getY() - unter.getY()) * (uber.getZ() - unter.getZ());
 										if(configManager.lang.equalsIgnoreCase("de"))
 											p.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("GS Daten: Bodenfläche: ").append(tmp.size()).append(" - Volumen: ").append(vol).toString());
 										else
 											p.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Lot data: floorsize: ").append(tmp.size()).append(" - capacity: ").append(vol).toString());
 									}
 								}
 								
 								if(tmp.size() > 3500)
 								{
 									if(p != null)
 									{
 										if(p.hasPermission("kb.admin"))
 										{
 											if(configManager.lang.equalsIgnoreCase("de"))
 												p.sendMessage((new StringBuilder()).append(ChatColor.RED).append("Das GS ist zu gross, das Muster des Bodens wurde daher nicht gespeichert. Sofern das GS einen einheitlichen Boden besitzt kannst du dies ignorieren.").toString());
 											else
 												p.sendMessage((new StringBuilder()).append(ChatColor.RED).append("This lot is too big. The floor can't be saved. If this lot has a mono-typed-floor: you can ignore this").toString());
 
 										}
 									}
 								} else {
 									floor = this.getCodedBottom(tmp);
 									ps2 = conn.prepareStatement((new StringBuilder()).append("UPDATE ").append(configManager.SQLTable).append("_krimbuy SET `floor`=? WHERE blockx=? AND blocky=? AND blockz=? AND world = ? LIMIT 1").toString());
 									ps2.setString(1, floor);
 									ps2.setInt(2, b.getX());
 						    		ps2.setInt(3, b.getY());
 						    		ps2.setInt(4, b.getZ());
 						    		ps2.setString(5, b.getWorld().getName());
 						    		ps2.executeUpdate();
 								}
 							}
 				    		
 						} else
 						{
 							//Etwas stimmt hiermit nicht. Entfernen
 							int id = this.getIDbyBlock(b);
 							if(id != 0)
 								this.killGS(id);
 							if(p != null)
 							{
 								if(configManager.lang.equalsIgnoreCase("de"))
 									p.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Dieser Untergrund passt nicht zum Ruleset. GS Entfernt").toString());
 								else
 									p.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("This floor doesn't fit to the ruleset. Lot removed").toString());
 
 							}
 						}
 					}
 					
 					if(!found)
 					{
 						//Ruleset gibts nicht mehr
 						ps2 = conn.prepareStatement((new StringBuilder()).append("UPDATE ").append(configManager.SQLTable).append("_krimbuy SET bx=0,`by`=0,bz=0,tx=0,ty=0,tz=0,level=0,ruleset=\"0\" WHERE blockx=? AND blocky=? AND blockz=? AND world = ? LIMIT 1").toString());
 						ps2.setInt(1, b.getX());
 			    		ps2.setInt(2, b.getY());
 			    		ps2.setInt(3, b.getZ());
 			    		ps2.setString(4, b.getWorld().getName());
 			    		ps2.executeUpdate();
 					}
 					if(ps2 != null)
 						ps2.close();
 					if(rs2 != null)
 						rs2.close();
 				}
 			}
 			
 			if(ps != null)
 				ps.close();
 			if(rs != null)
 				rs.close();
 		} catch (SQLException e)
 		{
 			System.out.println((new StringBuilder()).append("[KB] unable to update area: ").append(e).toString());
 		}
 	}
 	
 	public List<Block> getBottom(Block b, int height, String bottom)
     {
     	List<Block> ret = new ArrayList<Block>();
     	List<Integer> bot = new ArrayList<Integer>();
 		if(bottom != null && bottom.length() > 0)
 		{
 			
 			String[] tmpBot = bottom.split(",");
 			for (String bl: tmpBot) {
 			   bot.add(Integer.parseInt(bl));
 			}
 		}
 		
 		Block begin = b.getRelative(0,(height * -1),0);
 		if(bot.contains(begin.getTypeId()))
 		{
 			Block unter = begin.getRelative(0, 0, 0);
 			Block uber = begin.getRelative(0, 0, 0);
 			Block temp;
 			
 			//OK kann losgehen
 			temp = unter;
 			while(bot.contains(temp.getTypeId()))
 			{
 				unter = temp;
 				temp = unter.getRelative(-1, 0, 0);
 			}
 			
 			temp = unter;
 			while(bot.contains(temp.getTypeId()))
 			{
 				unter = temp;
 				temp = unter.getRelative(0, 0, -1);
 			}
 
 			temp = uber;
 			while(bot.contains(temp.getTypeId()))
 			{
 				uber = temp;
 				temp = uber.getRelative(1, 0, 0);
 			}
 			
 			temp = uber;
 			while(bot.contains(temp.getTypeId()))
 			{
 				uber = temp;
 				temp = uber.getRelative(0, 0, 1);
 			}
 			
 			for(int i=unter.getX();i<=uber.getX();++i)
 			{
 				for(int j=unter.getZ();j<=uber.getZ();++j)
 				{
 					ret.add(unter.getWorld().getBlockAt(i, uber.getY(), j));
 				}
 			}
 			return ret;
 		} else return null;
 		
     }
 	
 	public String getCodedBottom(List<Block> l)
     {
     	if(l == null) return "";
     	StringBuilder ret = new StringBuilder();
     	Block b = null;
     	for(int i=0;i<l.size();i++)
     	{
     		b = l.get(i);
     		if(b != null)
     			ret.append(b.getX()).append(",").append(b.getY()).append(",").append(b.getZ()).append(",").append(b.getTypeId()).append(";");
     	}
     	return ret.toString();
     }
 	
 	public void upgradeArea(Player p,Block b)
     {
     	int toLvl = 0;
     	try
 		{
 			Connection conn = Main.Database.getConnection();
         	PreparedStatement ps;
         	
     		String strg = (new StringBuilder()).append("SELECT ruleset,level,blockx,blocky,blockz,tx,ty,tz,bx,`by`,bz FROM ").append(configManager.SQLTable).append("_krimbuy WHERE blockx = ? AND blocky = ? AND blockz = ? AND world = ? LIMIT 0,1").toString();
     		ps = conn.prepareStatement(strg);
     		ps.setInt(1, b.getX());
     		ps.setInt(2, b.getY());
     		ps.setInt(3, b.getZ());
     		ps.setString(4, b.getWorld().getName());
     		ResultSet rs = ps.executeQuery();
 			if(rs.next())
 			{	
 				toLvl = rs.getInt("level") + 1;
 				if(rs.getInt("level") != 0 && !rs.getString("ruleset").equals("0"))
 				{
 					//Kaufdings hat ein Level und ein Ruleset
 					PreparedStatement ps2;
 					ps2 = conn.prepareStatement((new StringBuilder()).append("SELECT controlblockheight,blocks,bottom,height,deep,price FROM ").append(configManager.SQLTable).append("_krimbuy_rules WHERE ruleset = ? AND level = ? LIMIT 0,1").toString());
 					ps2.setString(1, rs.getString("ruleset"));
 					ps2.setInt(2, toLvl);
 					ResultSet rs2 = ps2.executeQuery();
 					boolean found = false;
 					if(rs2.next())
 					{
 						found = true;
 						int price = rs2.getInt("price");
 						List<Integer> bot = new ArrayList<Integer>();
 						if(rs2.getString("bottom") != null && rs2.getString("bottom").length() > 0)
 						{
 							String[] tmpBot = rs2.getString("bottom").split(",");
 							for (String bl: tmpBot) {
 							   bot.add(Integer.parseInt(bl));
 							}
 						}
 						
 						Block unter = b.getWorld().getBlockAt(rs.getInt("bx"), b.getRelative(0,(rs2.getInt("controlblockheight") * -1),0).getY(), rs.getInt("bz"));
 						Block uber = b.getWorld().getBlockAt(rs.getInt("tx"), b.getRelative(0,(rs2.getInt("controlblockheight") * -1),0).getY(), rs.getInt("tz"));
 						
 						Block temp;
 							
 						//OK kann losgehen
 						temp = unter.getRelative(-1, 0, 0);
 						while(bot.contains(temp.getTypeId()))
 						{
 							unter = temp;
 							temp = unter.getRelative(-1, 0, 0);
 						}
 						
 						temp = unter.getRelative(0, 0, -1);
 						while(bot.contains(temp.getTypeId()))
 						{
 							unter = temp;
 							temp = unter.getRelative(0, 0, -1);
 						}
 
 						temp = uber.getRelative(1, 0, 0);
 						while(bot.contains(temp.getTypeId()))
 						{
 							uber = temp;
 							temp = uber.getRelative(1, 0, 0);
 						}
 						
 						temp = uber.getRelative(0, 0, 1);
 						while(bot.contains(temp.getTypeId()))
 						{
 							uber = temp;
 							temp = uber.getRelative(0, 0, 1);
 						}
 						
 						//oberster und unterster Eintrag gefunden
 						if((uber.getY() + rs2.getInt("height")) > (uber.getWorld().getMaxHeight() - 1))
 							uber = uber.getRelative(0, uber.getWorld().getMaxHeight() - 1 - uber.getY(), 0);
 						else
 							uber = uber.getRelative(0, rs2.getInt("height"), 0);
 						
 						if((unter.getY() - rs2.getInt("deep")) < 0)
 								unter = unter.getRelative(0,0 - unter.getY() + 1,0);
 						else
 							unter = unter.getRelative(0, (rs2.getInt("deep")*-1), 0);
 
 						//Aktualisiere Eintrag
 						ps2 = conn.prepareStatement((new StringBuilder()).append("UPDATE ").append(configManager.SQLTable).append("_krimbuy SET tx=?,ty=?,tz=?,bx=?,`by`=?,bz=?,paid=paid+? WHERE blockx=? AND blocky=? AND blockz=? AND world=? LIMIT 1").toString());
 						ps2.setInt(1, uber.getX());
 			    		ps2.setInt(2, uber.getY());
 			    		ps2.setInt(3, uber.getZ());
 			    		ps2.setInt(4, unter.getX());
 			    		ps2.setInt(5, unter.getY());
 			    		ps2.setInt(6, unter.getZ());
 			    		ps2.setInt(7, price);
 						ps2.setInt(8, b.getX());
 			    		ps2.setInt(9, b.getY());
 			    		ps2.setInt(10, b.getZ());
 			    		ps2.setString(11, b.getWorld().getName());
 			    		ps2.executeUpdate();
 						
 					} 
 					
 					if(!found)
 					{
 						//Ruleset gibts nicht mehr
 						ps2 = conn.prepareStatement((new StringBuilder()).append("UPDATE ").append(configManager.SQLTable).append("_krimbuy SET bx=0,`by`=0,bz=0,tx=0,ty=0,tz=0,level=0,ruleset=\"0\" WHERE blockx=? AND blocky=? AND blockz=? AND world=? LIMIT 1").toString());
 						ps2.setInt(1, b.getX());
 			    		ps2.setInt(2, b.getY());
 			    		ps2.setInt(3, b.getZ());
 			    		ps2.setString(4, b.getWorld().getName());
 			    		ps2.executeUpdate();
 					} else
 					{
 						//Aktualisiere auf aktuellen level
 						String strg2 = (new StringBuilder()).append("UPDATE ").append(configManager.SQLTable).append("_krimbuy SET level = ? WHERE blockx = ? AND blocky = ? AND blockz = ? AND world = ? LIMIT 1").toString();
 			    		ps2 = conn.prepareStatement(strg2);
 			    		ps2.setInt(1, toLvl);
 			    		ps2.setInt(2, b.getX());
 			    		ps2.setInt(3, b.getY());
 			    		ps2.setInt(4, b.getZ());
 			    		ps2.setString(5, b.getWorld().getName());
 			    		ps2.executeUpdate();
 					}
 					
 		    		int id = this.getIDbyBlock(b);
 		    		if(id != 0)
 		    		{
 		    			KBArea a = this.getArea(id);
 		    			if(a != null)
 		    			{
 		    				a.loadByID(id);
 		    			}
 		    		}
 		    		
 					if(ps2 != null)
 						ps2.close();
 					if(rs2 != null)
 						rs2.close();
 				}
 			}
 			
     		
 			if(ps != null)
 				ps.close();
 
 			if(rs != null)
 				rs.close();
 		} catch (SQLException e)
 		{
 			System.out.println((new StringBuilder()).append("[KB] unable to upgrade area: ").append(e).toString());
 		}
     }
 	
 	public int getGSAmount(Player p, String s, String gr)
     {
     	int ret = 0;
     	try
 		{
     		Connection conn = Main.Database.getConnection();
         	PreparedStatement ps;
         	String str = (new StringBuilder()).append("SELECT COUNT(*) as c FROM ").append(configManager.SQLTable).append("_krimbuy WHERE buyer=? AND (ruleset=? OR ruleset IN (SELECT ruleset FROM ").append(configManager.SQLTable).append("_krimbuy_rules WHERE gruppe=? AND level = 1 AND gruppe != \"\"))").toString();
         	ps = conn.prepareStatement(str);
 			ps.setString(1, p.getName());
 			ps.setString(2, s);
 			ps.setString(3, gr);
 			ResultSet rs = ps.executeQuery();
 			if(rs.next())
 			{
 				ret = rs.getInt("c");
 			}
     		if(ps != null)
 				ps.close();
     		if(rs != null)
 				rs.close();
 		} catch (SQLException e)
 		{
 			System.out.println((new StringBuilder()).append("[KB] unable to get gs amount: ").append(e).toString());
 		}
     	return ret;
     }
 	
 	public void Tick()
 	{
 		try
 		{
     		Connection conn = Main.Database.getConnection();
         	PreparedStatement ps;
         	String str = (new StringBuilder()).append("SELECT b.id as id FROM ").append(configManager.SQLTable).append("_krimbuy as b, ").append(configManager.SQLTable).append("_krimbuy_rules as r WHERE b.noloose != 1 AND b.level = r.level AND b.ruleset = r.ruleset AND r.autofree != 0 AND b.sold != 0 AND (b.lastonline + (r.autofree * 60*60*24)) < UNIX_TIMESTAMP()").toString();
 			//System.out.println(str);
         	ps = conn.prepareStatement(str);
 			ResultSet rs = ps.executeQuery();
 			KBArea a;
 			while(rs.next())
 			{
 				a = this.getArea(rs.getInt("id"));
 				if(a != null)
 				{
 					System.out.println("[KB] Free GS with ID:"+a.id);
 					if(a.clear > 0)
 						a.clearGS();
 					this.freeGS(a.id);
 					this.updateArea(null,a.getInteractBlock());
 					a.loadByID(rs.getInt("id"));
 				}
 			}
     		if(ps != null)
 				ps.close();
     		if(rs != null)
 				rs.close();
 		} catch (SQLException e)
 		{
 			System.out.println((new StringBuilder()).append("[KB] unable to autoclear gs: ").append(e).toString());
 		}
 	}
 }
