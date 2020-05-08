 package de.bdh.kb2;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import de.bdh.brauxp.XPVaultProcessor;
 import de.bdh.kb.util.configManager;
 import de.bdh.kb2.Main;
 
 public class Commander implements CommandExecutor {
 	
 	Main plugin;
 	KBHelper helper;
 	public Commander(Main plugin)
 	{
 		this.plugin = plugin;
 		this.helper = Main.helper;
 	}
 	
 	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String args[])
     {
 		if(command.getName().equalsIgnoreCase("kbreload") && ((sender instanceof Player) && sender.hasPermission("kb.admin")) || !(sender instanceof Player))
     	{
 			this.plugin.reload();
 			sender.sendMessage("KrimBuy reloaded");
     	}
 		else if(sender instanceof Player)
         {
 			if(command.getName().equalsIgnoreCase("autoclearGS"))
         	{
         		if(sender.hasPermission("kb.admin"))
         		{
         			if(configManager.lang.equalsIgnoreCase("de"))
         				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Autofree Grundstuecke werden geleert").toString());
         			else
         				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Clearing 'autofree' lots").toString());
         			
         			this.helper.Tick();
         			return true;	
         		}
         	}
 			else if(command.getName().equalsIgnoreCase("giveGS"))
         	{
         		if(sender.hasPermission("kb.admin"))
         		{
         			Block b = this.helper.lastBlock.get(((Player)sender));
 	        		if(b == null)
 	        		{
 	        			if(configManager.lang.equalsIgnoreCase("de"))
 	        				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast kein Grundstueck ausgewaehlt").toString());
 	        			else
 	        				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("You've not selected a lot").toString());
 
 	        			return true;
 	        		}
 	        		else
 	        		{
 	        			int id = this.helper.getIDbyBlock(b);
 	        			if(id != 0 && this.helper.getArea(id) != null)
 	        			{
 	        				if(args.length == 0)
 			                {
 	        					this.helper.freeGS(id);
 	        					KBArea a = this.helper.getArea(id);
 	        					if(a.clear > 0)
 	        						a.clearGS();
 	        					if(configManager.lang.equalsIgnoreCase("de"))
 	        						sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Das Grundstueck wurde freigestellt").toString());
 	        					else
 	        						sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("The lot is now free").toString());
 
 	        				} else
 			                {
 			                	this.helper.obtainGS(id, args[0]);
 			                	if(configManager.lang.equalsIgnoreCase("de"))
 			                		sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Das Grundstueck gehoert nun: ").append(args[0]).toString());
 			                	else
 	        						sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("This lot is now owned by: ").append(args[0]).toString());
 			                }
 	        				
 	        			}
 	        		}
         		} else
         		{
         			if(configManager.lang.equalsIgnoreCase("de"))
         				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast keine Berechtigung ein Grundstueck zu bearbeiten").toString());
         			else
         				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("You dont have permissions to do that").toString());
 
         		}
         	}
 			else if(command.getName().equalsIgnoreCase("freeGS"))
 			{
 
         		if(args.length == 0)
                 {
         			if(configManager.lang.equalsIgnoreCase("de"))
         				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("USAGE: /freeGS Type").toString());
         			else
         				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("USAGE: /freeGS TYPE").toString());
         			return true;
                 } else
                 {
                 	int am = this.helper.howMuchFreeGS(args[0]);
                 	if(configManager.lang.equalsIgnoreCase("de"))
         				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Es sind noch ").append(am).append(" freie GS vom Typ '").append(args[0]).append("' frei").toString());
         			else
         				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("There are still ").append(am).append(" lots of type '").append(args[0]).append("' for sale").toString());
                 }
 			}
         	else if(command.getName().equalsIgnoreCase("useGS"))
         	{
         		if(sender.hasPermission("kb.buy"))
         		{
 	        		if(args.length == 0)
 	                {
 	        			if(configManager.lang.equalsIgnoreCase("de"))
 	        				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Bitte gib das Passwort fuer ein GS ein: /useGS PASSWORT").toString());
 	        			else
 	        				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Please enter the password for the lot: /useGS PASSWORT").toString());
 	        			return true;
 	                } else
 	                {
 	                	if(configManager.lang.equalsIgnoreCase("de"))
 	                		sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast ein Passwort gesetzt. Du kannst nun auf Grundstuecken bauen, welche dieses PW nutzen.").toString());
 	                	else
 	                		sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("You've entered a password. Now you can build on lots, which use this pw").toString());
 
 	                	this.helper.pass.put(sender.getName(), args[0]);
 	                	this.helper.loadPlayerAreas((Player)sender);
 	                }
         		} else
         		{
         			if(configManager.lang.equalsIgnoreCase("de"))
         				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast keine Berechtigung ein Grundstueck zu bearbeiten").toString());
         			else
         				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("You dont have permissions to do that").toString());
         		}
         	}
         	else if(command.getName().equalsIgnoreCase("listGS"))
         	{
         		try
         		{
         			boolean found = false;
         			Location b = ((Player)sender).getLocation();
 	            	String strg = "";
 	            	String param = "";
 	            	if(args.length == 0)
 	                {
 	            		if(configManager.lang.equalsIgnoreCase("de"))
 	            			sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Grundstuecke auf deiner Position:").toString());
 	            		else
 	            			sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Lots on your position:").toString());
 
 		        		strg = (new StringBuilder()).append("SELECT world,blockx,blocky,blockz,bx,`by`,bz,tx,ty,tz,buyer,sold,level,ruleset,pass,id FROM ").append(configManager.SQLTable).append("_krimbuy WHERE tx >= ").append(b.getBlockX()).append(" AND ty >= ").append(b.getBlockY()).append(" AND tz >= ").append(b.getBlockZ()).append(" AND bx <= ").append(b.getBlockX()).append(" AND `by` <= ").append(b.getBlockY()).append(" AND bz <= ").append(b.getBlockZ()).append(" AND world = ? AND NOT (bx=0 AND `by` = 0 AND bz = 0 AND tx = 0 AND ty = 0 AND tz=0)").toString();
 		        		param = b.getWorld().getName();
 	                } else if(sender.hasPermission("kb.admin"))
 	                {
 	                	if(configManager.lang.equalsIgnoreCase("de"))
 	                		sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Grundstuecke des Spielers ").append(args[0]).append(":").toString());
 	                	else
 	                		sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Lots owned by player ").append(args[0]).append(":").toString());
 
 		        		strg = (new StringBuilder()).append("SELECT world,blockx,blocky,blockz,bx,`by`,bz,tx,ty,tz,buyer,sold,level,ruleset,pass,id FROM ").append(configManager.SQLTable).append("_krimbuy WHERE buyer LIKE ?").toString();
 		        		param = "%"+args[0]+"%";
 	                } else
 	                {
 	                	if(configManager.lang.equalsIgnoreCase("de"))
 	                		sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast keine Berechtigung fuer diesen Befehl").toString());
 	                	else
 	        				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("You dont have permissions to do that").toString());
 	                	return true;
 	                }
 	            	Connection conn = Main.Database.getConnection();
 	            	PreparedStatement ps;
 	        		ps = conn.prepareStatement(strg);
 	        		ps.setString(1, param);
 	        		ResultSet rs = ps.executeQuery();
 	    			while(rs.next())
 	    			{
 	    				found = true;
 	    				if(configManager.lang.equalsIgnoreCase("de"))
 	    					sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append(rs.getInt("id")).append(": Besitzer '").append(rs.getString("buyer")).append("' - Typ '").append(rs.getString("ruleset")).append("' - Level '").append(rs.getInt("level")).append("'").toString());
 	    				else
 	    					sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append(rs.getInt("id")).append(": Owner '").append(rs.getString("buyer")).append("' - Type '").append(rs.getString("ruleset")).append("' - Level '").append(rs.getInt("level")).append("'").toString());
 
 	    			}
 	    			if(!found)
 	    			{
 	    				if(configManager.lang.equalsIgnoreCase("de"))
 	    					sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Keine Grundstuecke gefunden").toString());
 	    				else
 	    					sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Nothing found").toString());
 
 	    			}
 	    			if(ps != null)
 	    				ps.close();
 	    			
 	    			if(rs != null)
 	    				rs.close();
         		} catch (SQLException e)
         		{
         			System.out.println((new StringBuilder()).append("[KB] unable to list gs: ").append(e).toString());
         		}
         	}
         	else if(command.getName().equalsIgnoreCase("mineGS"))
         	{
         		if(sender.hasPermission("kb.buy"))
         		{
         			if(args.length != 1)
 	                {
         				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("USAGE: /mineGS TYP").toString());
         				return true;
 	                }
         			try
         			{
 	        			String strg = "";
 	        			boolean found = false;
 		        		strg = (new StringBuilder()).append("SELECT world,blockx,blocky,blockz,bx,`by`,bz,tx,ty,tz,buyer,sold,level,ruleset,pass,id FROM ").append(configManager.SQLTable).append("_krimbuy WHERE ruleset LIKE ? AND buyer=? ORDER BY ABS(blockx - ?) + ABS(blockz - ?) ASC LIMIT 0,1").toString();
 		        		Connection conn = Main.Database.getConnection();
 		            	PreparedStatement ps;
 		        		ps = conn.prepareStatement(strg);
 		        		ps.setString(1,args[0] + "%");
 		        		ps.setString(2,sender.getName());
 		        		ps.setInt(3,((Player) sender).getLocation().getBlockX());
 		        		ps.setInt(4,((Player) sender).getLocation().getBlockZ());
 		        		ResultSet rs = ps.executeQuery();
 		    			if(rs.next())
 		    			{
 		    				found = true;
 		    				Location t = Bukkit.getWorld(rs.getString("world")).getBlockAt(rs.getInt("blockx"), rs.getInt("blocky"), rs.getInt("blockz")).getLocation();
 		    				((Player) sender).setCompassTarget(t);
 		    				if(configManager.lang.equalsIgnoreCase("de"))
 		    					sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Dein Kompass zeigt nun auf das naechste dir gehoerende Grundstueck").toString());
 		    				else
 		    					sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Your compass now points to your closest lot").toString());
 	
 		    			}
 		    			
 		    			if(!found)
 		    			{
 		    				if(configManager.lang.equalsIgnoreCase("de"))
 		    					sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Sorry - kein GS gefunden").toString());
 		    				else
 		    					sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Sorry - nothing found").toString());
 
 		    			}
 		    			
 		    			if(ps != null)
 		    				ps.close();
 		    			
 		    			if(rs != null)
 		    				rs.close();
 		    			
         			} catch (SQLException e)
 	        		{
 	        			System.out.println((new StringBuilder()).append("[KB] unable to next gs: ").append(e).toString());
 	        		}
         		} else
         		{
         			if(configManager.lang.equalsIgnoreCase("de"))
         				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast keine Berechtigung fuer diesen Befehl").toString());	
         			else
         				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("You dont have permissions to do that").toString());
         			return true;
         		}
         	}
         	else if(command.getName().equalsIgnoreCase("nextgs"))
         	{
         		if(sender.hasPermission("kb.buy"))
         		{
         			if(args.length != 1)
 	                {
         				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("USAGE: /nextGS TYP. Example: /nextGS slums").toString());
         				return true;
 	                }
         			try
         			{
 	        			String strg = "";
 	        			boolean found = false;
 		        		strg = (new StringBuilder()).append("SELECT world,blockx,blocky,blockz,bx,`by`,bz,tx,ty,tz,buyer,sold,level,ruleset,pass,id FROM ").append(configManager.SQLTable).append("_krimbuy WHERE ruleset LIKE ? AND sold=0 ORDER BY ABS(blockx - ?) + ABS(blockz - ?) ASC LIMIT 0,1").toString();
 		        		Connection conn = Main.Database.getConnection();
 		            	PreparedStatement ps;
 		        		ps = conn.prepareStatement(strg);
 		        		ps.setString(1,args[0] + "%");
 		        		ps.setInt(2,((Player) sender).getLocation().getBlockX());
 		        		ps.setInt(3,((Player) sender).getLocation().getBlockZ());
 		        		ResultSet rs = ps.executeQuery();
 		    			if(rs.next())
 		    			{
 		    				found = true;
 		    				Location t = Bukkit.getWorld(rs.getString("world")).getBlockAt(rs.getInt("blockx"), rs.getInt("blocky"), rs.getInt("blockz")).getLocation();
 		    				((Player) sender).setCompassTarget(t);
 		    				if(configManager.lang.equalsIgnoreCase("de"))
 		    					sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Dein Kompass zeigt nun auf das naechste freie Grundstueck").toString());
 		    				else
 		    					sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Your compass points to the next free lot").toString());
 
 		    			}
 		    			
 		    			if(!found)
 		    			{
 		    				if(configManager.lang.equalsIgnoreCase("de"))
 		    					sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Sorry - kein GS gefunden").toString());
 		    				else
 		    					sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Sorry - nothing found").toString());
 
 		    			}
 		    			
 		    			if(ps != null)
 		    				ps.close();
 		    			
 		    			if(rs != null)
 		    				rs.close();
 		    			
         			} catch (SQLException e)
 	        		{
 	        			System.out.println((new StringBuilder()).append("[KB] unable to next gs: ").append(e).toString());
 	        		}
         		} else
         		{
         			if(configManager.lang.equalsIgnoreCase("de"))
         				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast keine Berechtigung fuer diesen Befehl").toString());	
         			else
         				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("You dont have permissions to do that").toString());
         			return true;
         		}
         	}
         	else if(command.getName().equalsIgnoreCase("tpGS"))
         	{
         		if(sender.hasPermission("kb.admin"))
         		{
         			if(args.length == 0)
 	                {
         				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("USAGE: /tpgs ID").toString());
         				return true;
 	                }
         			try
         			{
 	        			String strg = "";
 	        			boolean found = false;
 		        		strg = (new StringBuilder()).append("SELECT world,blockx,blocky,blockz,bx,`by`,bz,tx,ty,tz,buyer,sold,level,ruleset,pass,id FROM ").append(configManager.SQLTable).append("_krimbuy WHERE id = ?").toString();
 		        		Connection conn = Main.Database.getConnection();
 		            	PreparedStatement ps;
 		        		ps = conn.prepareStatement(strg);
 		        		ps.setInt(1,Integer.parseInt(args[0]));
 		        		ResultSet rs = ps.executeQuery();
 		    			if(rs.next())
 		    			{
 		    				if(configManager.lang.equalsIgnoreCase("de"))
 		    					sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Teleportiere dich zum GS des Spielers: ").append(rs.getString("buyer")).toString());	
 		    				else
 		    					sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Teleporting you to the lot of the player: ").append(rs.getString("buyer")).toString());	
 
 		    				found = true;
 		    				((Player) sender).teleport(Bukkit.getWorld(rs.getString("world")).getBlockAt(rs.getInt("blockx"), rs.getInt("blocky"), rs.getInt("blockz")).getRelative(BlockFace.UP).getLocation());
 		    			}
 		    			
 		    			if(!found)
 		    			{
 		    				if(configManager.lang.equalsIgnoreCase("de"))
 		    					sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Grundstueck wurde nicht gefunden").toString());
 		    				else
 		    					sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("No lot found").toString());
 
 		    			}
 		    			
 		    			if(ps != null)
 		    				ps.close();
 		    			
 		    			if(rs != null)
 		    				rs.close();
 		    			
         			} catch (SQLException e)
 	        		{
 	        			System.out.println((new StringBuilder()).append("[KB] unable to tp gs: ").append(e).toString());
 	        		}
         		} else if(sender.hasPermission("kb.teleport"))
     			{
         			if(args.length == 0)
 	                {
         				if(configManager.lang.equalsIgnoreCase("de"))
         					sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Bitte waehle einen Grundstueckstyp aus. Beispiel: /tpgs slums").toString());		
         				else
         					sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Please select a lot type. Example: /tpgs slums").toString());		
 
 	                } else
 	                {
 	        			String to = args[0];
 	        			String onlyfree = "AND price=0";
 	        			if(sender.hasPermission("kb.teleport.all"))
 	        				onlyfree = "";
 	        			try
 	        			{
 		        			String strg = "";
 		        			boolean found = false;
 			        		strg = (new StringBuilder()).append("SELECT world,blockx,blocky,blockz,bx,`by`,bz,tx,ty,tz,buyer,sold,level,ruleset,pass,id FROM ").append(configManager.SQLTable).append("_krimbuy WHERE ruleset LIKE ? AND sold=0 ? ORDER BY ABS(blockx - ?) + ABS(blockz - ?) ASC LIMIT 0,1").toString();
 			        		Connection conn = Main.Database.getConnection();
 			            	PreparedStatement ps;
 			        		ps = conn.prepareStatement(strg);
 			        		ps.setString(1,to + "%");
 			        		ps.setString(2, onlyfree);
 			        		ps.setInt(3,((Player) sender).getLocation().getBlockX());
 			        		ps.setInt(4,((Player) sender).getLocation().getBlockZ());
 			        		ResultSet rs = ps.executeQuery();
 			    			if(rs.next())
 			    			{
 			    				found = true;
 			    				Location t = Bukkit.getWorld(rs.getString("world")).getBlockAt(rs.getInt("blockx"), rs.getInt("blocky") + 1, rs.getInt("blockz")).getLocation();
 			    				((Player) sender).teleport(t);
 			    				if(configManager.lang.equalsIgnoreCase("de"))
 			    					sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du wurdest auf das naechste freie Grundstueck teleportiert").toString());
 			    				else
 			    					sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("You've been teleported to the next free lot").toString());
 
 			    			}
 			    			
 			    			if(!found)
 			    			{
 			    				if(configManager.lang.equalsIgnoreCase("de"))
 			    					sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Sorry - kein freies Grundstueck gefunden").toString());
 			    				else
 			    					sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Sorry - no free lot found").toString());
 
 			    			}
 			    			
 			    			if(ps != null)
 			    				ps.close();
 			    			
 			    			if(rs != null)
 			    				rs.close();
 			    			
 	        			} catch (SQLException e)
 		        		{
 		        			System.out.println((new StringBuilder()).append("[KB] unable to next gs: ").append(e).toString());
 		        		}		
 	                }
     			} else
         		{
     				if(configManager.lang.equalsIgnoreCase("de"))
     					sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast keine Berechtigung fuer diesen Befehl").toString());	
     				else
         				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("You dont have permissions to do that").toString());
     				return true;
         		}
         	} else if(command.getName().equalsIgnoreCase("delGS"))
         	{
         		if(args.length > 0)
         		{	
 	        		if(sender.hasPermission("kb.admin"))
 	        		{
 	        			int id = Integer.parseInt(args[0]);
 	        			if(this.helper.getArea(id) != null)
 	        			{
 	        				this.helper.killGS(id);
 	        				if(configManager.lang.equalsIgnoreCase("de"))
 	        					sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Das Grundstueck wurde entfernt").toString());
 	        				else
 	        					sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("The lot has been removed").toString());
 
 	        				return true;
 	        			}
 	        			else
 	        			{
 	        				if(configManager.lang.equalsIgnoreCase("de"))
 	        					sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Grundstueck nicht gefunden").toString());
 	        				else
 	        					sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Lot not found").toString());
 
 	        			}
 	        		} else
 	        		{
 	        			if(configManager.lang.equalsIgnoreCase("de"))
 	        				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast keine Berechtigung Grundstuecke zu entfernen").toString());
 	        			else
 	        				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("You dont have permissions to do that").toString());
 	        			return true;
 	        		}
 	        	} else
 	        	{
 	        		if(configManager.lang.equalsIgnoreCase("de"))
 	        			sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast keine ID eingegeben").toString());
 	        		else
 	        			sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Please enter an ID").toString());
 
 	        	}
         	}
         	else if(command.getName().equalsIgnoreCase("delallGS"))
         	{
         		if(args.length > 1)
         		{	
         			if(args[1].equals("okay"))
         			{
 		        		if(sender.hasPermission("kb.admin"))
 		        		{
 		        			try
 		        			{
 		        				String strg = (new StringBuilder()).append("SELECT id FROM ").append(configManager.SQLTable).append("_krimbuy WHERE ruleset LIKE ?").toString();
 				        		Connection conn = Main.Database.getConnection();
 				            	PreparedStatement ps;
 				        		ps = conn.prepareStatement(strg);
 				        		ps.setString(1, "%" + args[0] + "%");
 				        		int found = 0;
 				        		ResultSet rs = ps.executeQuery();
 				        		int id = 0;
 				    			while(rs.next())
 				    			{
 				    				id = rs.getInt("id");
 				    				
 				    				if(this.helper.getArea(id) != null)
 				        			{ 
 				    					++found;
 				        				this.helper.killGS(id);
 				        			}
 				    			}
 				    			
 				    			if(found == 0)
 				    			{
 				    				if(configManager.lang.equalsIgnoreCase("de"))
 			        					sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Kein Grundstueck gefunden").toString());
 			        				else
 			        					sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("No Lots found").toString());
 				    			} else
 				    			{
 				    				if(configManager.lang.equalsIgnoreCase("de"))
 			        					sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Es wurden '").append(found).append("' Grundstuecke entfernt").toString());
 			        				else
 			        					sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("'").append(found).append("' lots has been removed").toString());
 				    			}
 				    			
 		        				if(ps != null)
 				    				ps.close();
 				    			
 				    			if(rs != null)
 				    				rs.close();
 		        			} catch (SQLException e)
 		        			{
 		        				System.out.println((new StringBuilder()).append("[KB] unable to next dellallgs: ").append(e).toString());
 		        			}
 		        		} else
 		        		{
         					sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("No permissions :(").toString());
 
 		        		}
         			} else
         			{
         				if(configManager.lang.equalsIgnoreCase("de"))
         					sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Bitte bestätige diesen Befehl via /dellallgs TYP okay").toString());
         				else
         					sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Please validate this command by typing /dellallgs TYPE okay").toString());
 
         			}
         		} else
         		{
         			sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("USAGE: /dellallGS TYPE").toString());
         		}
         		
         	}
         	else if(command.getName().equalsIgnoreCase("sellGS"))
         	{
         		if(sender.hasPermission("kb.buy"))
         		{
         			Block b = this.helper.lastBlock.get(((Player)sender));
 	        		if(b == null)
 	        		{
 	        			if(configManager.lang.equalsIgnoreCase("de"))
 	        				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast kein Grundstueck ausgewaehlt").toString());
 	        			else
 	        				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("You've not selected a lot").toString());
 	        			return true;
 	        		} else
 	        		{
 	        			int id = this.helper.getIDbyBlock(b);
 	        			KBArea a = this.helper.getArea(id);
 	        			if(a != null)
 	        			{
 		        			if(a.cansell > 0 && a.sold == 1 && a.owner.equalsIgnoreCase(sender.getName()))
 		        			{
 		        				int amount = (new Double(a.paid * (new Double(a.cansell) / 100.0))).intValue();
 		        				if(args.length == 0)
 		        				{
 		        					if(configManager.lang.equalsIgnoreCase("de"))
 		        					{
 		        						if(amount < 0)
 			        						sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du musst zum Verkaufen ").append(amount * -1).append(this.plugin.econ.currencyNamePlural()).append(" zurueckzahlen. Gib hierzu '/sellGS okay' ein").toString());
 		        						else
 		        							sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du kannst dieses Grundstueck fuer ").append(amount).append(this.plugin.econ.currencyNamePlural()).append(" verkaufen. Gib hierzu '/sellGS okay' ein").toString());
 		        						if(a.pricexp > 0)
 			        						sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Achtung! Deine EXP bekommst du nicht zurueck!").toString());
 		        					}
 		        					else
 		        					{
 		        						if(amount < 0)
 			        						sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("You'll have to pay ").append(amount * -1).append(this.plugin.econ.currencyNamePlural()).append(" to sell this lot. Just enter '/sellGS okay'").toString());
 		        						else
 		        							sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("You can sell this lot for ").append(amount).append(this.plugin.econ.currencyNamePlural()).append(". Just enter '/sellGS okay'").toString());
 		        						if(a.pricexp > 0)
 			        						sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Be aware! You will not get your EXP back!").toString());
 
 		        					}
 	
 		        				}
 		        				else
 		        				{
 		        					if(amount < 0)
 		        					{
 		        						if(this.plugin.econ.getBalance(sender.getName()) < (amount * -1))
 		        						{
 		        							if(configManager.lang.equalsIgnoreCase("de"))
 				        						sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast nicht genug Geld").toString());
 				        					else
 				        						sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("You don't have enough money").toString());
 		        							return true;
 		        						} else
 		        							this.plugin.econ.withdrawPlayer(sender.getName(), (amount * -1));
 		        					} else
 		        						this.plugin.econ.depositPlayer(sender.getName(), amount);
 		        					
 		        					if(a.clear > 0)
 		        					{
 		        						a.clearGS();
 		        						this.helper.updateArea((Player)sender,b);
 		        					}
 		        					
 		        					this.helper.freeGS(id);
 		        					a.loadByID(id);
 		        					
 		        					if(configManager.lang.equalsIgnoreCase("de"))
 		        						sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast das Grundstueck erfolgreich verkauft und ").append(amount).append(this.plugin.econ.currencyNamePlural()).append(" erhalten").toString());
 		        					else
 		        						sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("You've sold the lot and gained ").append(amount).append(this.plugin.econ.currencyNamePlural()).toString());
 	
 		        				}
 		        			} else
 		        			{
 		        				if(configManager.lang.equalsIgnoreCase("de"))
 		        					sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du kannst dieses Grundstueck nicht verkaufen").toString());
 		        				else
 		        					sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("You can't sell this lot").toString());
 	
 		        			}
 	        			} else
 	        			{
 	        				if(configManager.lang.equalsIgnoreCase("de"))
 	            				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast kein gueltiges Grundstueck ausgewaehlt").toString());
 	            			else
 	            				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("You've not selected a valid lot").toString());
 
 	        			}
 	        		}
         		} else
         		{
         			if(configManager.lang.equalsIgnoreCase("de"))
         				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast keine Berechtigung ein Grundstueck zu verkaufen").toString());
         			else
         				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("You dont have permissions to do that").toString());
         		}
         	}
         	else if(command.getName().equalsIgnoreCase("passwortGS") || command.getName().equalsIgnoreCase("passGS"))
         	{
         		if(sender.hasPermission("kb.buy"))
         		{
 	        		Block b = this.helper.lastBlock.get(((Player)sender));
 	        		if(b == null)
 	        		{
 	        			if(configManager.lang.equalsIgnoreCase("de"))
 	        				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast kein Grundstueck ausgewaehlt").toString());
 	        			else
 	        				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("You've not selected a lot").toString());
 	        			return true;
 	        		}
 	        		
 	        		if(args.length == 0)
 	                {
 	        			if(configManager.lang.equalsIgnoreCase("de"))
 	        				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Bitte gib ein neues Passwort fuer ein GS ein: /passGS PASSWORT").toString());
 	        			else
 	        				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Please enter a password for the lot: /passGS PASSWORT").toString());
 
 	        			return true;
 	                } else
 	                {
 	                	int id = this.helper.getIDbyBlock(b);
 	                	if(id != 0)
 	                	{
 	                		KBArea a = this.helper.getArea(id);
 	                		if(a != null)
 	                		{
 	                			if(a.owner.equalsIgnoreCase(sender.getName()))
 	                			{
 	                				try
 	                				{
 	                					Connection conn = Main.Database.getConnection();
 	                					PreparedStatement ps2;
 				    					ps2 = conn.prepareStatement((new StringBuilder()).append("UPDATE ").append(configManager.SQLTable).append("_krimbuy SET pass=? WHERE id = ? LIMIT 1").toString());
 				    					ps2.setString(1, args[0]);
 				    					ps2.setInt(2, id);
 						        		ps2.executeUpdate();
 						        		if(configManager.lang.equalsIgnoreCase("de"))
 						        		{
 					    					sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast ein Passwort gesetzt. Andere Spieler koennen nun mit Hilfe des Passwortes auf dem GS bauen").toString());
 						                	sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Hierzu muss dieser /useGS ").append(args[0]).append(" eingeben.").toString());
 						        		} else
 						        		{
 						        			sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("You've set a new password. Other players can now start to build on your lot").toString());
 						                	sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("The other player just need to enter /useGS ").append(args[0]).append(" with your password.").toString());
 
 						        		}
 						        		if(ps2 != null)
 						    				ps2.close();
 					                	
 					                	a.pass = args[0];
 					                	this.helper.passwordChanged(id);
 					                	
 	                				} catch (SQLException e)
 	        		        		{
 	        		        			System.out.println((new StringBuilder()).append("[KB] unable to change password: ").append(e).toString());
 	        		        		}
 	                			} else
 	                			{
 	                				if(configManager.lang.equalsIgnoreCase("de"))
 	                					sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du kannst nicht das Passwort eines fremden Grundstueckes aendern").toString());
 	                				else
 	                					sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("You cant change the password of a lot you don't own").toString());
 
 	                			}
 	                		} else
 	                		{
 	                			if(configManager.lang.equalsIgnoreCase("de"))
 	                				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Das ausgewaehlte Grundstueck ist ungueltig").toString());
 	                			else
 	                				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("This selected lot is invalid").toString());
 
 	                		}
 	                	} else 
 	                	{
 	                		if(configManager.lang.equalsIgnoreCase("de"))
 	                			sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Das ausgewaehlte Grundstueck ist ungueltig").toString());
 	                		else
                 				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("This selected lot is invalid").toString());
 	                	}
 	                }
         		} else
         		{
         			if(configManager.lang.equalsIgnoreCase("de"))
         				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast keine Berechtigung ein Grundstueck zu bearbeiten").toString());
         			else
         				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("You dont have permissions to do that").toString());
         		}
         		return true;
         	}
         	else if(command.getName().equalsIgnoreCase("nolooseGS") || command.getName().equalsIgnoreCase("neverlooseGS"))
         	{
         		if(sender.hasPermission("kb.admin"))
         		{
         			if(args.length == 0)
 	                {
 	        			sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("USAGE /nolooseGS NAME").toString());
 	        			return true;
 	                } else
 	                {
 	        			try
 	        			{
 	        	    		Connection conn = Main.Database.getConnection();
 	        	        	PreparedStatement ps;
 	        	        	
 	        	        	int l = 1;
 	        	        	if(command.getName().equalsIgnoreCase("neverlooseGS"))
 	        	        		l = 2;
 	        	        	
 	        	    		String strg = (new StringBuilder()).append("UPDATE ").append(configManager.SQLTable).append("_krimbuy SET noloose=? WHERE buyer = ?").toString();
 	        	    		ps = conn.prepareStatement(strg);
 	        	    		ps.setString(2,args[0]);
 	        	    		ps.setInt(1,l);
 	        	    		ps.executeUpdate();
 	        	    		
 	        	    		if(ps != null)
 	        					ps.close();
 	        	    		
 	        	    		if(configManager.lang.equalsIgnoreCase("de"))
 	        	    			sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Alle Grundstuecke von '").append(args[0]).append("' werden nun nicht verfallen - solange er offline bleibt").toString());
 	        	    		else
 	        	    			sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("All lots owned by '").append(args[0]).append("' won't be free'd - until he comes back online").toString());
 
 	        			} catch (SQLException e)
 	        			{
 	        				System.out.println((new StringBuilder()).append("[KB] unable to update noloose region: ").append(e).toString());
 	        			}
 	                }
         		} else
         		{
         			if(configManager.lang.equalsIgnoreCase("de"))
         				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast keine Berechtigung hierzu").toString());
         			else
         				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("You dont have permissions to do that").toString());
         			}
         	}
         	else if(command.getName().equalsIgnoreCase("upgradeGS"))
         	{
         		if(sender.hasPermission("kb.upgrade"))
         		{
         			Block b = this.helper.lastBlock.get(((Player)sender));
 	        		if(b == null)
 	        		{
 	        			if(configManager.lang.equalsIgnoreCase("de"))
 	        				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast kein Grundstueck ausgewaehlt").toString());
 	        			else
 	        				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("You've not selected a lot").toString());
 	        			return true;
 	        		}
 	        		int exp = this.helper.canUpgradeArea((Player)sender,b);
 	        		if(exp >= 0)
 	        		{
 	        			Double prc = new Double(exp);
 	        			if(this.plugin.econ.getBalance(sender.getName()) > prc)
 						{
 							this.plugin.econ.withdrawPlayer(sender.getName(), prc);
 							if(this.helper.upgradeArea((Player)sender, b,(this.plugin.XPVault != null)) == true)
 							{
 								if(configManager.lang.equalsIgnoreCase("de"))
 									sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Das Grundstueck wurde erweitert").toString());
 								else
 									sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Your lot has been expanded").toString());
 							} else
 							{
 								if(configManager.lang.equalsIgnoreCase("de"))
 									sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast nicht genug EXP").toString());
 								else
 									sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("You don't have enough EXP").toString());
 					
 							}
 							return true;
 						} else
 						{
 							if(configManager.lang.equalsIgnoreCase("de"))
 								sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast nicht genug Geld").toString());
 							else
 								sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("You don't have enough money").toString());
 
 							return true;
 						}
 	        		} else if(exp == -1)
 	        		{
 	        			if(configManager.lang.equalsIgnoreCase("de"))
 	        				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du kannst dieses Grundstueck nicht erweitern").toString());
 	        			else
 	        				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("You can't expand this lot anymore").toString());
 
 	        			return true;
 					} else if(exp < 0)
 	        		{
 	        			if(configManager.lang.equalsIgnoreCase("de"))
 	        				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du benoetigst mehr EXP zum erweitern: ").append(exp * -1).toString());
 	        			else
 	        				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("You can't expand this lot. You need more EXP: ").append(exp * -1).toString());
 
 	        			return true;
 					}	
         		}
         	}
         	else if(command.getName().equalsIgnoreCase("buyGS"))
         	{
         		if(sender.hasPermission("kb.buy"))
         		{
 	        		Block b = this.helper.lastBlock.get(((Player)sender));
 	        		if(b == null)
 	        		{
 	        			if(configManager.lang.equalsIgnoreCase("de"))
 	        				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast kein Grundstueck ausgewaehlt").toString());
 	        			else
 	        				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("You've not selected a lot").toString());
 	        			return true;
 	        		} else
 	        		{
 	        			int id = this.helper.getIDbyBlock(b);
 	        			if(id != 0)
 	        			{
 	        				KBArea a = this.helper.getArea(id);
 	        				if(a != null)
 	        				{
 	        					if(a.nobuy == 0)
 		        				{
 		        					if(a.sold != 1)
 		        					{
 		        						Double prc = new Double(a.price);
 										
 										if(this.plugin.econ.getBalance(sender.getName()) >= prc)
 										{
 					
 											boolean hasxp = false;
 											if(a.pricexp > 0)
 											{
 												try
 												{
 													XPVaultProcessor xp = (XPVaultProcessor) this.plugin.XPVault;
 													if(xp.getBalance(sender.getName()) < a.pricexp)
 														hasxp = false;
 													else
 														hasxp = true;
 												} catch (Exception e)
 												{
 													e.printStackTrace();
 													hasxp = false;
 												}
 											} else hasxp = true;
 											
 											if(hasxp == true)
 											{
 												boolean hasperm = Main.helper.hasPerm((Player)sender,a.perm);
 												
 												if(hasperm == true || a.perm.length() == 0)
 												{
 													if(a.onlyamount == 0 || this.helper.getGSAmount((Player)sender,a.ruleset,a.gruppe) < a.onlyamount)
 													{
 														if(prc < 0)
 														{
 															this.helper.obtainGS(id, sender.getName());
 															prc = prc*-1;
															this.plugin.econ.depositPlayer(sender.getName(), prc);
 															
 															if(configManager.lang.equalsIgnoreCase("de"))
 																sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast das Grundstueck und ").append(prc).append(this.plugin.econ.currencyNamePlural()).append(" erhalten").toString());
 															else
 																sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("You've got the lot and additionally you've gained ").append(prc).append(this.plugin.econ.currencyNamePlural()).append("").toString());
 														}
 														else if(this.plugin.econ.withdrawPlayer(sender.getName(), prc).transactionSuccess())
 														{
 															this.helper.obtainGS(id, sender.getName());
 															if(configManager.lang.equalsIgnoreCase("de"))
 																sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast das Grundstueck gekauft").toString());
 															else
 																sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("You've bought this lot").toString());
 		
 														} else
 														{
 															if(configManager.lang.equalsIgnoreCase("de"))
 																sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Etwas ist schiefgelaufen. Bitte erstelle eine /PE").toString());
 															else
 																sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Something went wrong. Please contact an administrator").toString());
 			
 														}
 													} else
 													{
 														if(configManager.lang.equalsIgnoreCase("de"))
 															sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast bereits zuviele Grundstuecke von diesem Typ").toString());
 														else
 															sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("You've already got too many lots of this type").toString());
 		
 													}
 												} else
 												{
 													if(configManager.lang.equalsIgnoreCase("de"))
 														sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du darfst dieses Grundstueck nicht kaufen").toString());
 													else
 														sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("You're not allowed to buy this lot").toString());
 		
 												}
 											} else
 											{
 												if(configManager.lang.equalsIgnoreCase("de"))
 													sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast nicht genug EXP").toString());
 												else
 													sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("You don't have enough EXP").toString());
 		
 											}
 										} else
 										{
 											if(configManager.lang.equalsIgnoreCase("de"))
 												sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast nicht genug Geld").toString());
 											else
 												sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("You don't have enough money").toString());
 	
 										}
 									} else
 									{
 										if(configManager.lang.equalsIgnoreCase("de"))
 											sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Dieses Grundstueck ist bereits verkauft").toString());
 										else
 											sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("This lot is already sold").toString());
 									}
 		        				}
 	        					else
 								{
 									if(configManager.lang.equalsIgnoreCase("de"))
 										sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Dieses Grundstueck ist nicht verkaeuflich").toString());
 									else
 										sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("This lot is not for sale").toString());
 								}
 	        				} else
 	        				{
 	        					if(configManager.lang.equalsIgnoreCase("de"))
 	        						sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Das Grundstueck ist ungueltig").toString());
 	        					else
 	        						sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("This lot is invalid").toString());
 
 	        				}
 	        			} else
 	        			{
 	        				if(configManager.lang.equalsIgnoreCase("de"))
 	        					sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Das Grundstueck ist ungueltig").toString());
 	        				else
         						sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("This lot is invalid").toString());
 	        			}
 	        		}
         		} else
         		{
         			if(configManager.lang.equalsIgnoreCase("de"))
         				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du darfst keine Grundstuecke kaufen").toString());
         			else
         				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("You're not allowed to buy lots").toString());
 
         		}
         	} else if(command.getName().equalsIgnoreCase("kbupdate") && sender.hasPermission("kb.create"))
         	{
         		Block b = this.helper.lastBlock.get(((Player)sender));
             	if(b == null)
         		{
             		if(configManager.lang.equalsIgnoreCase("de"))
             			sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast kein Grundstueck ausgewaehlt").toString());
             		else
         				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("You've not selected a lot").toString());
         		} else
         		{
         			this.helper.updateArea((Player)sender, b);
         			if(configManager.lang.equalsIgnoreCase("de"))
         				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Das Grundstueck wurde aktualisiert").toString());
         			else
         				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("This lot has been updated").toString());
 
         		}
             	return true;
         	} else if(command.getName().equalsIgnoreCase("kbruleset"))
         	{
         		if(args.length == 0)
                 {
         			this.helper.ruleset.remove(sender.getName());
         			if(configManager.lang.equalsIgnoreCase("de"))
         				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast dein Ruleset geloescht").toString());
         			else
         				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("You've deleted your ruleset").toString());
 
         			return true;
                 } else
                 {
 	        		try {
 	        			Connection conn = Main.Database.getConnection();
 	                	PreparedStatement ps = null;
 	                	ps = conn.prepareStatement((new StringBuilder()).append("SELECT id,ruleset from ").append(configManager.SQLTable).append("_krimbuy_rules WHERE ruleset=? AND level=1 LIMIT 0,1").toString());
 	            		ps.setString(1, args[0]);
 	            		ResultSet rs = ps.executeQuery();
 	            		boolean found = false;
 	    				if(rs.next())
 	    				{
 	    					found = true;
 	    					//OK GEFUNDEN
 	    					if(sender.hasPermission("kb.create") || sender.hasPermission("kb.create."+rs.getString("ruleset")))
 	    					{
 		    					this.helper.ruleset.put(sender.getName(),rs.getString("ruleset"));
 		    					if(configManager.lang.equalsIgnoreCase("de"))
 		    						sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du legst nun Gebiete mit den Regeln ").append(this.helper.ruleset.get(sender.getName())).append(" an").toString());
 		    					else
 		    						sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("You're creating lots with the ruleset ").append(this.helper.ruleset.get(sender.getName())).append("").toString());
 	    					} else
 	    					{
 	    						if(configManager.lang.equalsIgnoreCase("de"))
 		    						sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast keine Genehmigung Gebiete mit den Regeln von ").append(this.helper.ruleset.get(sender.getName())).append(" anzulegen").toString());
 		    					else
 		    						sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("You dont have permission to create lots with the ruleset ").append(this.helper.ruleset.get(sender.getName())).append("").toString());
 
 	    					}
 	    				}
 	    				if(rs != null)
 	        				rs.close();
 	    				if(ps != null)
 	        				ps.close();
 	    				
 	    				if(!found)
 	    				{
 	    					if(configManager.lang.equalsIgnoreCase("de"))
 	    						sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Es wurde kein Regelwerk mit dieser Bezeichnung gefunden").toString());
 	    					else
 	    						sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("There is no ruleset with this name").toString());
 
 	    				}
 	        		} catch (SQLException e) { sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Error - sry :/").toString()); }
                 }
         		return true;
         	}
         	else if((command.getName().equalsIgnoreCase("makesell") || command.getName().equalsIgnoreCase("kbsell")) && (sender.hasPermission("kb.create") || ( this.helper.ruleset.get(sender.getName()) != null && sender.hasPermission("kb.create."+this.helper.ruleset.get(sender.getName())))))
         	{
         		if(args.length == 0)
                 {
         			String add = "";
         			if(this.plugin.XPVault != null)
         				add = "[EXP]";
         			
         			if(configManager.lang.equalsIgnoreCase("de"))
         				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Verkaufe Block - /makesell PREIS ").append(add).toString());
         			else
         				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Sell Lot - /makesell price ").append(add).toString());
                 } else
                 {
                 	Block b = this.helper.lastBlock.get(((Player)sender));
                 	if(b == null)
 	        		{
                 		if(configManager.lang.equalsIgnoreCase("de"))
                 			sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast kein Grundstueck ausgewaehlt").toString());
                 		else
 	        				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("You've not selected a lot").toString());
                 		return true;
 	        		}
                 	Connection conn = Main.Database.getConnection();
                 	PreparedStatement ps = null;
             		try {
             			int pxp = 0;
             			if(args.length == 2)
             				pxp = Integer.parseInt(args[1]);
 						ps = conn.prepareStatement((new StringBuilder()).append("INSERT INTO ").append(configManager.SQLTable).append("_krimbuy (price,blockx,blocky,blockz,world,floor,ruleset,pricexp) VALUES (?,?,?,?,?,\"\",\"\",?)").toString());
 	            		ps.setInt(1, Integer.parseInt(args[0]));
 	            		ps.setInt(2,b.getX());
 	            		ps.setInt(3,b.getY());
 	            		ps.setInt(4,b.getZ());
 	            		ps.setString(5, b.getWorld().getName());
 	            		ps.setInt(6, pxp);
 	        			ps.executeUpdate();
 	        			if(ps != null)
 	        				ps.close();
 	        			
 	        			//Wenn Regelset Ausgewaehlt:
 	        			if(this.helper.ruleset.get(sender.getName()) != null)
 	        			{
 	        				ps = conn.prepareStatement((new StringBuilder()).append("UPDATE ").append(configManager.SQLTable).append("_krimbuy SET ruleset=?, level=1 WHERE blockx=? AND blocky=? AND blockz = ? AND world = ? LIMIT 1").toString());
 		            		ps.setString(1, this.helper.ruleset.get(sender.getName()));
 		            		ps.setInt(2,b.getX());
 		            		ps.setInt(3,b.getY());
 		            		ps.setInt(4,b.getZ());
 		            		ps.setString(5,b.getWorld().getName());
 		            		
 		        			ps.executeUpdate();
 		        			if(ps != null)
 		        				ps.close();
 		        			if(configManager.lang.equalsIgnoreCase("de"))
 		        				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Das Grundstueck hat nun den Typ ").append(this.helper.ruleset.get(sender.getName())).append("").toString());
 		        			else
 		        				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("This lot has the type ").append(this.helper.ruleset.get(sender.getName())).append("").toString());
 
 		        			this.helper.updateArea((Player)sender,b);
 	        			}
 	        			
 	        			if(configManager.lang.equalsIgnoreCase("de"))
 	        				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Der Block ist nun Kaufbar zum Preis von ").append(Integer.parseInt(args[0])).append(this.plugin.econ.currencyNamePlural()).toString());
 	        			else
 	        				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("This lot is for sale for ").append(Integer.parseInt(args[0])).append(this.plugin.econ.currencyNamePlural()).toString());
 
             		} catch (SQLException e) { 
             			sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Error - sry :/").toString());
             			System.out.println((new StringBuilder()).append("[KB] unable to makesell block: ").append(e).toString());
             			}
                 }
         		
         	}
         	
         }
         return true;
     }
 }
