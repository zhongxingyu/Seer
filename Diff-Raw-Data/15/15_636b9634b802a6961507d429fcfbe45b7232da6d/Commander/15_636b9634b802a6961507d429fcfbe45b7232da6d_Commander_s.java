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
 
 import de.bdh.kb.util.configManager;
 import de.bdh.kb2.Main;
 
 public class Commander implements CommandExecutor {
 	
 	Main plugin;
 	KBHelper helper;
 	public Commander(Main plugin)
 	{
 		this.plugin = plugin;
 		this.helper = plugin.helper;
 	}
 	
 	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String args[])
     {
         if(sender instanceof Player)
         {
         	if(command.getName().equals("giveGS"))
         	{
         		if(sender.hasPermission("kb.admin"))
         		{
         			Block b = this.helper.lastBlock.get(((Player)sender));
 	        		if(b == null)
 	        		{
 						sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast kein Grundstueck ausgewaehlt").toString());
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
 	        					sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Das Grundstck wurde freigestellt").toString());
 			                } else
 			                {
 			                	this.helper.obtainGS(id, args[0]);
 			                	sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Das Grundstck gehrt nun: ").append(args[0]).toString());
 			                }
 	        				
 	        			}
 	        		}
         		} else
         			sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast keine Berechtigung ein Grundstck zu bearbeiten").toString());
 
         	}
         	else if(command.getName().equals("useGS"))
         	{
         		if(sender.hasPermission("kb.buy"))
         		{
 	        		if(args.length == 0)
 	                {
 	        			sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Bitte gib das Passwort fr ein GS ein: /useGS PASSWORT").toString());
 	        			return true;
 	                } else
 	                {
 	                	sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast ein Passwort gesetzt. Du kannst nun auf Grundstcken bauen, welche dieses PW nutzen.").toString());
 	                	this.helper.pass.put(sender.getName(), args[0]);
 	                	this.helper.loadPlayerAreas((Player)sender);
 	                }
         		} else
         			sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast keine Berechtigung ein Grundstck zu bearbeiten").toString());
         	}
         	else if(command.getName().equals("listGS"))
         	{
         		try
         		{
         			boolean found = false;
         			Location b = ((Player)sender).getLocation();
 	            	String strg = "";
 	            	String param = "";
 	            	if(args.length == 0)
 	                {
 	            		sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Grundstcke auf deiner Position:").toString());
 		        		strg = (new StringBuilder()).append("SELECT world,blockx,blocky,blockz,bx,`by`,bz,tx,ty,tz,buyer,sold,level,ruleset,pass,id FROM ").append(configManager.SQLTable).append("_krimbuy WHERE tx >= ").append(b.getBlockX()).append(" AND ty >= ").append(b.getBlockY()).append(" AND tz >= ").append(b.getBlockZ()).append(" AND bx <= ").append(b.getBlockX()).append(" AND `by` <= ").append(b.getBlockY()).append(" AND bz <= ").append(b.getBlockZ()).append(" AND world = ? AND NOT (bx=0 AND `by` = 0 AND bz = 0 AND tx = 0 AND ty = 0 AND tz=0)").toString();
 		        		param = b.getWorld().getName();
 	                } else if(sender.hasPermission("kb.admin"))
 	                {
 	                	sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Grundstcke des Spielers ").append(args[0]).append(":").toString());
 		        		strg = (new StringBuilder()).append("SELECT world,blockx,blocky,blockz,bx,`by`,bz,tx,ty,tz,buyer,sold,level,ruleset,pass,id FROM ").append(configManager.SQLTable).append("_krimbuy WHERE buyer LIKE ?").toString();
 		        		param = "%"+args[0]+"%";
 	                } else
 	                {
 	                	sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast keine Berechtigung fr diesen Befehl").toString());
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
 	        			sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append(rs.getInt("id")).append(": Besitzer '").append(rs.getString("buyer")).append("' - Typ '").append(rs.getString("ruleset")).append("' - Level '").append(rs.getInt("level")).append("'").toString());
 	    			}
 	    			if(!found)
 	    			{
 	        			sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Keine Grundstcke gefunden").toString());
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
         	else if(command.getName().equals("mineGS"))
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
 		    				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Dein Kompass zeigt nun auf das nchste dir gehrende Grundstck").toString());
 		    			}
 		    			
 		    			if(!found)
 		    			{
 		    				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Sorry - kein GS gefunden").toString());
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
         			sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast keine Berechtigung fr diesen Befehl").toString());	
         			return true;
         		}
         	}
         	else if(command.getName().equals("nextGS"))
         	{
         		if(sender.hasPermission("kb.buy"))
         		{
         			if(args.length != 1)
 	                {
         				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("USAGE: /nextGS TYP").toString());
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
 		    				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Dein Kompass zeigt nun auf das nchste freie Grundstck").toString());
 		    			}
 		    			
 		    			if(!found)
 		    			{
 		    				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Sorry - kein GS gefunden").toString());
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
         			sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast keine Berechtigung fr diesen Befehl").toString());	
         			return true;
         		}
         	}
         	else if(command.getName().equals("tpGS"))
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
 	        			sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Grundstcke des Spielers ").append(args[0]).append(":").toString());
 		        		strg = (new StringBuilder()).append("SELECT world,blockx,blocky,blockz,bx,`by`,bz,tx,ty,tz,buyer,sold,level,ruleset,pass,id FROM ").append(configManager.SQLTable).append("_krimbuy WHERE id = ?").toString();
 		        		Connection conn = Main.Database.getConnection();
 		            	PreparedStatement ps;
 		        		ps = conn.prepareStatement(strg);
 		        		ps.setInt(1,Integer.parseInt(args[0]));
 		        		ResultSet rs = ps.executeQuery();
 		    			if(rs.next())
 		    			{
 		    				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Teleportiere dich zum GS des Spielers: ").append(rs.getString("buyer")).toString());	
 		    				found = true;
 		    				((Player) sender).teleport(Bukkit.getWorld(rs.getString("world")).getBlockAt(rs.getInt("blockx"), rs.getInt("blocky"), rs.getInt("blockz")).getRelative(BlockFace.UP).getLocation());
 		    			}
 		    			
 		    			if(!found)
 		    			{
 		    				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Grundstck wurde nicht gefunden").toString());	
 		    			}
 		    			
 		    			if(ps != null)
 		    				ps.close();
 		    			
 		    			if(rs != null)
 		    				rs.close();
 		    			
         			} catch (SQLException e)
 	        		{
 	        			System.out.println((new StringBuilder()).append("[KB] unable to tp gs: ").append(e).toString());
 	        		}
         		} else
         		{
         			sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast keine Berechtigung fr diesen Befehl").toString());	
         			return true;
         		}
         	} else if(command.getName().equals("delGS"))
         	{
         		if(args.length > 0)
         		{	
 	        		if(sender.hasPermission("kb.admin"))
 	        		{
 	        			int id = Integer.parseInt(args[0]);
 	        			if(this.helper.getArea(id) != null)
 	        			{
 	        				this.helper.killGS(id);
 	        				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Das Grundstck wurde entfernt").toString());
 	        				return true;
 	        			}
 	        			else
 	        				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Grundstck nicht gefunden").toString());
 	        		} else
 	        			sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast keine Berechtigung Grundstcke zu entfernen").toString());
         		} else
         			sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast keine ID eingegeben").toString());
         	}
         	else if(command.getName().equals("sellGS"))
         	{
         		if(sender.hasPermission("kb.buy"))
         		{
         			Block b = this.helper.lastBlock.get(((Player)sender));
 	        		if(b == null)
 	        		{
 						sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast kein Grundstck ausgewaehlt").toString());
 						return true;
 	        		} else
 	        		{
 	        			int id = this.helper.getIDbyBlock(b);
 	        			KBArea a = this.helper.getArea(id);
 	        			if(a.cansell > 0 && a.sold == 1 && a.owner.equalsIgnoreCase(sender.getName()))
 	        			{
 	        				int amount = (new Double(a.paid * (new Double(a.cansell) / 100.0))).intValue();
 	        				if(args.length == 0)
 			        			sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du kannst dieses Grundstck fr ").append(amount).append("BM verkaufen. Gib hierzu /sellGS okay ein").toString());
 	        				else
 	        				{
 	        					this.plugin.econ.depositPlayer(sender.getName(), amount);
 	        					if(a.clear > 0)
 	        					{
 	        						a.clearGS();
 	        						this.helper.freeGS(id);
 	        						this.helper.updateArea((Player)sender,b);
 	        						a.loadByID(id);
 	        					}
 		    					
 		    					
 								sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast das Grundstck erfolgreich verkauft und ").append(amount).append("BM erhalten").toString());
 
 	        				}
 	        			} else
 	    					sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du kannst dieses Grundstck nicht verkaufen").toString());
 	        		}
         		} else
         			sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast keine Berechtigung ein Grundstck zu verkaufen").toString());
 
         	}
         	else if(command.getName().equals("passwortGS") || command.getName().equals("passGS"))
         	{
         		if(sender.hasPermission("kb.buy"))
         		{
 	        		Block b = this.helper.lastBlock.get(((Player)sender));
 	        		if(b == null)
 	        		{
 						sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast kein Grundstueck ausgewaehlt").toString());
 						return true;
 	        		}
 	        		
 	        		if(args.length == 0)
 	                {
 	        			sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Bitte gib ein neues Passwort fr ein GS ein: /passGS PASSWORT").toString());
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
 				    					sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast ein Passwort gesetzt. Andere Spieler knnen nun mit Hilfe des Passwortes auf dem GS bauen").toString());
 					                	sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Hierzu muss dieser /useGS ").append(args[0]).append(" eingeben.").toString());
 					                	if(ps2 != null)
 						    				ps2.close();
 					                	
 					                	a.pass = args[0];
 					                	this.helper.passwordChanged(id);
 					                	
 	                				} catch (SQLException e)
 	        		        		{
 	        		        			System.out.println((new StringBuilder()).append("[KB] unable to change password: ").append(e).toString());
 	        		        		}
 	                			} else
 	                				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du kannst nicht das Passwort eines fremden Grundstckes ndern").toString());
 
 	                		} else
 	                			sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Das ausgewhlte Grundstck ist ungltig").toString());
 	                	} else 
 	                		sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Das ausgewhlte Grundstck ist ungltig").toString());
 	                }
         		} else
         			sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast keine Berechtigung ein Grundstck zu bearbeiten").toString());
         		return true;
         	}
         	else if(command.getName().equals("nolooseGS") || command.getName().equals("neverlooseGS"))
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
 	        	        	if(command.getName().equals("neverlooseGS"))
 	        	        		l = 2;
 	        	        	
 	        	    		String strg = (new StringBuilder()).append("UPDATE ").append(configManager.SQLTable).append("_krimbuy SET noloose=? WHERE buyer = ?").toString();
 	        	    		ps = conn.prepareStatement(strg);
 	        	    		ps.setString(2,args[0]);
 	        	    		ps.setInt(1,l);
 	        	    		ps.executeUpdate();
 	        	    		
 	        	    		if(ps != null)
 	        					ps.close();
 	        	    		
 	        	    		sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Alle Grundstcke von '").append(args[0]).append("' werden nun nicht verfallen - solange er offline bleibt").toString());
 	
 	        			} catch (SQLException e)
 	        			{
 	        				System.out.println((new StringBuilder()).append("[KB] unable to update noloose region: ").append(e).toString());
 	        			}
 	                }
         		} else
         			sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast keine Berechtigung hierzu").toString());
         	}
         	else if(command.getName().equals("upgradeGS"))
         	{
         		if(sender.hasPermission("kb.upgrade"))
         		{
         			Block b = this.helper.lastBlock.get(((Player)sender));
 	        		if(b == null)
 	        		{
 						sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast kein Grundstueck ausgewaehlt").toString());
 						return true;
 	        		}
 	        		int exp = this.helper.canUpgradeArea((Player)sender,b);
 	        		if(exp != 0)
 	        		{
 	        			Double prc = new Double(exp);
 	        			if(this.plugin.econ.getBalance(sender.getName()) > prc)
 						{
							this.plugin.econ.depositPlayer(sender.getName(), (prc*-1));
 							this.helper.upgradeArea((Player)sender, b);
 							sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Das Grundstck wurde erweitert").toString());
 							return true;
 						} else
 						{
 							sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast nicht genug Geld").toString());
 							return true;
 						}
 	        		} else
 	        		{
 						sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du kannst dieses Grundstck nicht erweitern").toString());
 						return true;
 					}	
         		}
         	}
         	else if(command.getName().equals("buyGS"))
         	{
         		if(sender.hasPermission("kb.buy"))
         		{
 	        		Block b = this.helper.lastBlock.get(((Player)sender));
 	        		if(b == null)
 	        		{
 						sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast kein Grundstueck ausgewaehlt").toString());
 						return true;
 	        		} else
 	        		{
 	        			int id = this.helper.getIDbyBlock(b);
 	        			if(id != 0)
 	        			{
 	        				KBArea a = this.helper.getArea(id);
 	        				if(a != null)
 	        				{
 	        					if(a.sold != 1)
 	        					{
 	        						Double prc = new Double(a.price);
 									
 									if(this.plugin.econ.getBalance(sender.getName()) >= prc)
 									{
 										if((a.perm.length() > 0 && sender.hasPermission(a.perm)) || a.perm.length() == 0)
 										{
 											if(a.onlyamount == 0 || this.helper.getGSAmount((Player)sender,a.ruleset,a.gruppe) < a.onlyamount)
 											{
 												if(this.plugin.econ.withdrawPlayer(sender.getName(), prc).transactionSuccess())
 												{
 													this.helper.obtainGS(id, sender.getName());
 													sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast das Grundstueck gekauft").toString());
 												} else
 													sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Etwas ist schiefgelaufen. Bitte erstelle eine /PE").toString());
 											} else
 												sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast bereits zuviele Grundstcke von diesem Typ").toString());
 										} else
 											sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du darfst dieses Grundstck nicht kaufen").toString());
 									} else
 										sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast nicht genug Geld").toString());
 	        					} else
 	        						sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Dieses Grundstueck ist bereits verkauft").toString());
 	        				} else
 	        					sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Das Grundstck ist ungltig").toString());
 	        			} else
 	        				sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Das Grundstck ist ungltig").toString());
 	        		}
         		} else
         			sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du darfst keine Grundstuecke kaufen").toString());
         	} else if(command.getName().equals("kbupdate") && sender.hasPermission("kb.create"))
         	{
         		Block b = this.helper.lastBlock.get(((Player)sender));
             	if(b == null)
         		{
 					sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast kein Grundstueck ausgewaehlt").toString());
         		} else
         		{
         			this.helper.updateArea((Player)sender, b);
         			sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Das Grundstck wurde aktualisiert").toString());
         		}
             	return true;
         	} else if(command.getName().equals("kbruleset") && sender.hasPermission("kb.create"))
         	{
         		if(args.length == 0)
                 {
         			this.helper.ruleset.remove(sender.getName());
         			sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast dein ruleset gelscht").toString());
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
 	    					this.helper.ruleset.put(sender.getName(),rs.getString("ruleset"));
 	    					sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du legst nun Gebiete mit den Regeln ").append(this.helper.ruleset.get(sender.getName())).append(" an").toString());
 	    				}
 	    				if(rs != null)
 	        				rs.close();
 	    				if(ps != null)
 	        				ps.close();
 	    				
 	    				if(!found)
 	    					sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Es wurde kein Regelwerk mit dieser Bezeichnung gefunden").toString());
 	    
 	        		} catch (SQLException e) { sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Error - sry :/").toString()); }
                 }
         		return true;
         	}
         	else if(command.getName().equals("makesell") && sender.hasPermission("kb.create"))
         	{
         		if(args.length == 0)
                 {
         			sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Verkaufe Block - /makesell PREIS").toString());
                 } else
                 {
                 	Block b = this.helper.lastBlock.get(((Player)sender));
                 	if(b == null)
 	        		{
 						sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast kein Grundstueck ausgewaehlt").toString());
 						return true;
 	        		}
                 	Connection conn = Main.Database.getConnection();
                 	PreparedStatement ps = null;
             		try {
 						ps = conn.prepareStatement((new StringBuilder()).append("INSERT INTO ").append(configManager.SQLTable).append("_krimbuy (price,blockx,blocky,blockz,world,floor,ruleset) VALUES (?,?,?,?,?,\"\",\"\")").toString());
 	            		ps.setInt(1, Integer.parseInt(args[0]));
 	            		ps.setInt(2,b.getX());
 	            		ps.setInt(3,b.getY());
 	            		ps.setInt(4,b.getZ());
 	            		ps.setString(5, b.getWorld().getName());
 	        			ps.executeUpdate();
 	        			if(ps != null)
 	        				ps.close();
 	        			
 	        			//Wenn Regelset Ausgewhlt:
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
 		        			sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Das Grundstck hat nun den Typ ").append(this.helper.ruleset.get(sender.getName())).append("").toString());
 	        				this.helper.updateArea((Player)sender,b);
 	        			}
 	        			
 	        			sender.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Der Block ist nun Kaufbar zum Preis von ").append(Integer.parseInt(args[0])).toString());
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
