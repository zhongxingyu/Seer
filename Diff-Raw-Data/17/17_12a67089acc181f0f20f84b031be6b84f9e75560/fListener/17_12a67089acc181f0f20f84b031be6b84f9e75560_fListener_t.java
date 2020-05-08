 import java.sql.*;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.logging.Logger;
 public class fListener extends PluginListener{
     static final Logger log = Logger.getLogger("Minecraft");
     Factions plugin;
   public boolean usehmoddb;
   public String dbUsername; 
   public String dbPassword;
   public String dbDriver;
   public String dbUrl;
   HashMap<Player,String> newfaction = new HashMap();
   HashMap<Player,String> invite = new HashMap();
   HashMap<Player,Boolean> disband = new HashMap();
   ArrayList<Player> tobepaid = new ArrayList();
   int balance = -1;
   double dbalance = -1;
   factionSQL sql = new factionSQL(this); 
   public fListener(Factions instance)
      { 
          this.plugin = instance;
          usehmoddb = plugin.tpp.getBoolean("useCanaryDb");
          
          
          if(usehmoddb){
              PropertiesFile properties    = new    PropertiesFile("mysql.properties");
              try {
                  dbDriver = properties.getString("driver", "com.mysql.jdbc.Driver");
                  dbUrl = properties.getString("db", "jdbc:mysql://localhost:3306/minecraft");
                  dbUsername = properties.getString("user", "user");
                  dbPassword = properties.getString("pass", "pass");
                  
              } catch (Exception ex) {
                  plugin.log.severe( "Factions - exception while reading from mysql.properties " + ex);
                  ex.printStackTrace();
                  return;
              }
          }
          else{
              
              dbUsername = plugin.tpp.getString("username");
              dbPassword = plugin.tpp.getString("password");
              dbDriver = plugin.tpp.getString("MySQL-Driver");
              dbUrl = plugin.tpp.getString("db");
          }
          
          
      }
   public int sig = plugin.tpp.getInt("Signatures-To-Create-Faction");
   
   public String[] nf;
   public String ss;
     public void makeTables(){
         sql.CreateTable();
     }
         public void changeFac(Player player, String n){
         String sin = newfaction.get(player);
         String[] sa = sin.split(",");
         String out = sin.replaceAll(sa[0],n);
         newfaction.remove(player);
         newfaction.put(player, out);
         }
         
         public void combineSig(Player player, String s){
             String sin = newfaction.get(player);
             if(sin == null){
                 sin = s;
                 newfaction.remove(player);
             newfaction.put(player,sin);
             return;
             }
             sin += s;
             newfaction.remove(player);
             newfaction.put(player,sin);
         }
 	
 	public boolean onCommand(Player player, java.lang.String[] split) {
             String name = player.getName();
             if(plugin.mysql){
                 if(split[0].equalsIgnoreCase("/factions")){
                     this.sendCommands(player);
                     return true;
                 }
 		if(split[0].equalsIgnoreCase("/fc") || split[0].equalsIgnoreCase("/fcreate") && player.canUseCommand("/fcreate")){
                     
                     
                     if(sql.keyExists(player.getName())){
                         player.sendMessage("§cYou cannot create a faction while you're in one!");
                         return true;
                     }
                     if(plugin.max != -1){
                         
                         if(plugin.max <= sql.countUniqueFactions()){
                         
                         player.sendMessage("§cMaximum number of factions reached.");
                         return true;
                         }
                     }
                     if(split.length == 2){
                         if(sql.factionExists(split[1])){
                             player.sendMessage("§cThat faction already exists!");
                          return true;   
                         }
                     if(newfaction.containsKey(player)){
                         if(sql.factionExists(split[1])){
                             player.sendMessage("§cThat faction already exists!");
                             return true;
                         }
                      ss = split[1];
                      changeFac(player,split[1]);
                      player.sendMessage("§6Faction name changed to " + split[1]);
                      return true;
                     } 
                     if(plugin.economy){
                         if(plugin.dconomy){
                         dbalance = (Double)etc.getLoader().callCustomHook("dCBalance", new Object[] { "Player-Balance", name} );  
                         player.sendMessage("§6The faction will cost you " + plugin.cost + " " + plugin.currency);
                         player.sendMessage("§6The " + plugin.currency + " will be removed when you get enough invitees.");
                         
                         }
                         else{
                             balance = Hooked.getInt("iBalance", new Object[] { "balance", player.getName() });
                             player.sendMessage("§6The faction will cost you " + plugin.cost + " " + plugin.currency);
                         player.sendMessage("§6The " + plugin.currency + " will be removed when you get enough invitees.");
                         }
                     }
                     
                     ss = split[1] + "," + player.getName() + ",";
                     combineSig(player,ss);
                     nf = newfaction.get(player).split(",");
                     int sigs = sig + 1;
                     sigs = sigs - nf.length;
                     player.sendMessage("Faction application made you need " + sigs + " more signatures");
                     player.sendMessage("Use /finvite <player> to get signatures.");
                     return true;
                     }
                     player.sendMessage("Usage is /fcreate <faction name>.");
                     return true;
                     }     
                     
                 
                 
                 if(split[0].equalsIgnoreCase("/fi") || split[0].equalsIgnoreCase("/finvite") && player.canUseCommand("/finvite")){
                     // mysql inviting
                     if(!sql.keyExists(player.getName()) && !newfaction.containsKey(player)){
                             player.sendMessage("§cYou are not in a faction yet!");
                             return true;
                             
                         }
                     if(split.length == 2){
                         Player p2 = etc.getServer().matchPlayer(split[1]);
                         
                         if(p2 == null){
                             player.sendMessage(split[1] + " §cis not online or doesn't exist");
                             return true;
                         }
                         if(p2 == player){
                             player.sendMessage("§cYou cannot invite yourself!");
                             return true;
                         }
                         if(sql.keyExists(p2.getName())){
                           player.sendMessage(p2.getName() + " §c§cis already in a faction!")  ;
                           return true;
                         }
                         if(invite.containsKey(p2)){
                             player.sendMessage(p2.getName() + " already has an invite out");
                             return true;
                         }
                         
                         if(newfaction.containsKey(player)){
                           
                          nf = newfaction.get(player).split(",");
                          if(this.isAlreadyInvited(nf, p2.getName(), player)){
                              player.sendMessage("§cNice try but you can't invite the same person twice");
                              return true;
                          }
                          player.sendMessage("§6Sending invite to §f" + p2.getName());
                          p2.sendMessage(player.getName() + " §6has invited you to the faction " + nf[0]);
                          p2.sendMessage("§6Type /ff accept to accept or /ff decline to decline");
                          invite.put(p2, player.getName());
                          return true;
                         }else if(sql.keyExists(player.getName())){
                             if(sql.getRank(name) == 4){
                                 player.sendMessage("§cYou are not high enough rank to invite!");
                                 return true;
                             }
                          player.sendMessage("Sending invite to " + p2.getName());
                          p2.sendMessage(player.getName() + " §6has invited you to the faction " + sql.getFaction(player.getName()));
                          p2.sendMessage("§6Type /ff accept to accept or /ff decline to decline");
                          invite.put(p2, player.getName());
                          return true;
                         }
                     }
                     player.sendMessage("§cUsage is /finvite <player>");
                     return true;
                     // mysql inviting
                 }
                 
                 if(split[0].equalsIgnoreCase("/ff")){
                  
                     if(tobepaid.contains(player)){
                         nf = newfaction.get(player).split(",");
                         if(split.length == 2)
                             if(split[1].equalsIgnoreCase("pay")){
                        double diff;
                                if(plugin.dconomy){
                                    dbalance =  (Double)etc.getLoader().callCustomHook("dCBalance", new Object[] { "Player-Balance", player.getName()});
                                    if(plugin.cost > dbalance){
                                        diff = plugin.cost - dbalance;
                                        player.sendMessage("§cYou do not have enough " + plugin.currency + " to make the faction.");
                                        player.sendMessage("§cYou need " + diff + " more " + plugin.currency + " type /ff pay to pay.");
                                        return true;
                                    }
                                    Double cost = (double)plugin.cost;
                                   Hooked.call("dCBalance", new Object[] { "Player-Charge", player.getName(), cost});
                                     
                                     sql.createFaction(nf);
                                     newfaction.remove(player);
                                }else{
                                    balance = Hooked.getInt("iBalance", new Object[]{"balance" , name});
                                    if(plugin.cost > balance){
                                        diff = plugin.cost - balance;
                                        player.sendMessage("§cYou do not have enough " + plugin.currency + " to make the faction.");
                                        player.sendMessage("§cYou need " + diff + " more " + plugin.currency + " type /ff pay to pay.");
                                        return true;
                                    }
                                    Hooked.call("iBalance", new Object[]{ "set", player.getName(), balance - plugin.cost });
                                    Hooked.call("iBalance",new Object[]{ "show", player.getName() });
                                    sql.createFaction(nf);
                                     newfaction.remove(player);
                                }
                             }
                         return true;
                     }
                     
                  if(invite.containsKey(player)){
                          Player p1 = etc.getServer().matchPlayer(invite.get(player));   
                  if(split.length == 2){
                      
                      if(split[1].equalsIgnoreCase("accept")){
                          
                          p1.sendMessage(player.getName() + " has accepted your invitation");
                          player.sendMessage("You have accepted the invitation");
                          invite.remove(player);
                         
                         if(sql.keyExists(p1.getName())){
                             sql.addPlayerToFaction(name, sql.getFaction(p1.getName()));
                             return true;
                         }
                          ss = player.getName() + ",";
                          this.combineSig(p1, ss);
                          nf = newfaction.get(p1).split(",");
                          
                          if(nf.length >= sig + 1){
                              if(plugin.economy){
                                  double diff;
                                if(plugin.dconomy){
                                    dbalance =  (Double)etc.getLoader().callCustomHook("dCBalance", new Object[] { "Player-Balance", p1.getName()});
                                    if(plugin.cost > dbalance){
                                        diff = plugin.cost - dbalance;
                                        p1.sendMessage("§cYou do not have enough " + plugin.currency + " to make the faction.");
                                        p1.sendMessage("§cYou need " + diff + " more " + plugin.currency + " type /ff pay to pay.");
                                        tobepaid.add(p1);
                                        return true;
                                    }
                                    Double cost = (double)plugin.cost;
                                    Hooked.call("dBalance", new Object[] { "Player-Charge", p1.getName(), cost});
                                }else{
                                    balance = Hooked.getInt("iBalance", new Object[]{"balance" , p1.getName()});
                                    if(plugin.cost > balance){
                                        diff = plugin.cost - balance;
                                        p1.sendMessage("§cYou do not have enough " + plugin.currency + " to make the faction.");
                                        p1.sendMessage("§cYou need " + diff + " more " + plugin.currency + " type /ff pay to pay.");
                                        tobepaid.add(p1);
                                        return true;
                                    }
                                    Hooked.call("iBalance", new Object[]{ "set", p1.getName(), balance - plugin.cost });
                                    Hooked.call("iBalance",new Object[]{ "show", p1.getName() });
                                }
                              }
                              sql.createFaction(nf);
                              newfaction.remove(p1);
                              return true;
                          }
                          int sigs = sig + 1;
                          sigs = sigs - nf.length;
                          p1.sendMessage("You need " + sigs + " more signatures");
                          
                          
                          return true;
                      }
                      else if(split[1].equalsIgnoreCase("decline")){
                         etc.getServer().getPlayer(invite.get(player)).sendMessage(player.getName() + " has declined your invitation");
                          player.sendMessage("You have declined the invitation");
                          invite.remove(player);
                          return true; 
                      }
                      return true;
                  
                  }
                  else if(split.length == 2 && sql.keyExists(invite.get(player))){
                      if(split[1].equalsIgnoreCase("accept")){
                       p1.sendMessage(player.getName() + " has accepted your invitation");
                       player.sendMessage("You have accepted the invitation");
                       invite.remove(player);
                       sql.addPlayerToFaction(player.getName(), sql.getFaction(p1.getName()));
                       return true;
                      }
                      else if(split[1].equalsIgnoreCase("decline")){
                       p1.sendMessage(player.getName() + " has declined your invitation");
                       player.sendMessage("You have declined the invitation");
                       invite.remove(player);
                       
                       
                      }
                      return true;
                  }
                 }
                 }
                 
                 if(split[0].equalsIgnoreCase("/fr") || split[0].equalsIgnoreCase("/froster")){
                     if(!sql.keyExists(player.getName())){
                      player.sendMessage("§6You are not in a faction")   ;
                      return true;
                     }
                     sql.factionRoster(player);
                     return true;
                 }
                 if(split[0].equalsIgnoreCase("/fchat") || split[0].equalsIgnoreCase("/fb")){
                     if(!sql.keyExists(player.getName())){
                         player.sendMessage("§cYou are not in a faction!");
                         return true;
                     }
                     String[] ma = split;
                     ma[0] = "";
                     String message = stringBuilder(ma);
                     sql.factionChat(player, message);
                     return true;
                 }
                 if(split[0].equalsIgnoreCase("/fp") || split[0].equalsIgnoreCase("/fpromote")){
                     
                     if(!sql.keyExists(player.getName())){
                         player.sendMessage("§cYou are not in a faction!");
                         return true;
                     }
                     if(sql.getRank(name) > 2){
                         player.sendMessage("§cYou are not high enough rank to promote!");
                         return true;
                     }
                     if(split.length == 2){
                         int rank = 0;
                         int newrank = 0;
                         Player p2 = etc.getServer().matchPlayer(split[1]);
                         if(p2 != null)
                             split[1] = p2.getName();
                        if(player == p2){
                             player.sendMessage("You cannot promote yourself");
                             return true;
                         }
                         if(!sql.keyExists(split[1])){
                             player.sendMessage("§cThat player is not in a faction!");
                             return true;
                         }
                         if(!sql.getFaction(name).equalsIgnoreCase(sql.getFaction(split[1]))){
                            player.sendMessage("§cThat player is not in your faction!");
                             return true; 
                         }
                        rank = sql.getRank(split[1]);
                        if(rank < sql.getRank(name)){
                         player.sendMessage("§cYou cannot promote those higher than yourself!");
                            return true;   
                        }
                        newrank = rank - 1;
                        if(newrank < sql.getRank(name)){
                            player.sendMessage("§cYou cannot promote higher than yourself!");
                            return true;
                        }
                        if(newrank == 0){
                            player.sendMessage("§cThere can only be one owner of a faction!");
                            player.sendMessage("§6To give ownership of a faction use /fowner");
                            return true;
                        }
                       
                        if(p2 != null){
                            sql.setRank(p2.getName(),newrank);
                            player.sendMessage("§6" + p2.getName() + " promoted to rank " + getStars(newrank));
                            sql.promoted(player, p2.getName());
                            return true;
                        }
                            
                        sql.setRank(split[1], newrank);
                        player.sendMessage("§6" + split[1] + " promoted to rank " + getStars(newrank));
                        sql.promoted(player, split[1]);
                        return true;
                         
                     }
                     player.sendMessage("§cUsage is /fpromote <player>");
                     return true;
                 }
                 if(split[0].equalsIgnoreCase("/fd") || split[0].equalsIgnoreCase("/fdemote")){
                     
                     if(!sql.keyExists(player.getName())){
                         player.sendMessage("§cYou are not in a faction!");
                         return true;
                     }
                     if(sql.getRank(name) > 2){
                         player.sendMessage("§cYou are not high enough rank to demote!");
                         return true;
                     }
                     if(split.length == 2){
                         int rank = 0;
                         int newrank = 0;
                         Player p2 = etc.getServer().matchPlayer(split[1]);
                         if(p2 != null)
                             split[1] = p2.getName();
                         if(player == p2){
                             player.sendMessage("You cannot demote yourself");
                             return true;
                         }
                         if(!sql.keyExists(split[1])){
                             player.sendMessage("§cThat player is not in a faction!");
                             return true;
                         }
                         if(!sql.getFaction(name).equalsIgnoreCase(sql.getFaction(split[1]))){
                            player.sendMessage("§cThat player is not in your faction!");
                             return true; 
                         }
                        rank = sql.getRank(split[1]);
                        if(rank <= sql.getRank(name)){
                         player.sendMessage("§cYou cannot demote those higher than yourself!");
                            return true;   
                        }
                        newrank = rank + 1;
                        
                        if(newrank > 4){
                            player.sendMessage("§c" + split[1] + " is already at the lowest rank!");
                            return true;
                        }
                       
                        if(p2 != null){
                            sql.setRank(p2.getName(),newrank);
                            player.sendMessage("§6" + p2.getName() + " demoted to rank " + getStars(newrank));
                            sql.demoted(player, p2.getName());
                            return true;
                        }
                            
                        sql.setRank(split[1], newrank);
                        player.sendMessage("§6" + split[1] + " demoted to rank " + getStars(newrank));
                        sql.demoted(player, split[1]);
                        return true;
                         
                     }
                     player.sendMessage("§cUsage is /fdemote <player>");
                     return true;
                 }
                 if(split[0].equalsIgnoreCase("/fq") || split[0].equalsIgnoreCase("/fquit")){
                     if(!sql.keyExists(name)){
                         player.sendMessage("§cYou are not in a faction!");
                         return true;
                     }
                     if(sql.getRank(name) == 0){
                         player.sendMessage("§cYou cannot quit your own faction");
                         player.sendMessage("§cYou must disband the faction or give away ownership.");
                         return true;
                     }
                     sql.quitFaction(name);
                     player.sendMessage("§6You have left the faction");
                     return true;
                 }
                 if(split[0].equalsIgnoreCase("/fk") || split[0].equalsIgnoreCase("/fkick")){
                 if(!sql.keyExists(name)){
                         player.sendMessage("§cYou are not in a faction!");
                         return true;
                     }
                 if(sql.getRank(name) > 1){
                     player.sendMessage("§cYou are not high enough rank to kick");
                     return true;
                 }
                 if(split.length == 2){
                     Player p2 = etc.getServer().matchPlayer(split[1]);
                     if(p2!=null)
                       split[1] = p2.getName();
                     
                     if(!sql.keyExists(split[1])){
                         player.sendMessage("§c" + split[1] + " is not in a faction!");
                         return true;
                     }
                     
                     if(name.equalsIgnoreCase(split[1])){
                         player.sendMessage("§cYou cannot kick yourself, use /fquit");
                         return true;
                     }
                     if(!sql.getFaction(name).equalsIgnoreCase(sql.getFaction(split[1]))){
                         player.sendMessage("§c" + split[1] + " is not in your faction!")  ;
                         return true;
                     }
                     if(sql.getRank(split[1]) < sql.getRank(name)){
                      player.sendMessage("§cYou cannot kick those higher rank than you.")   ;
                      return true;
                     }
                     sql.kickFaction(name,split[1]);
                     player.sendMessage("§6You have kicked " + split[1] + " from the faction.");
                     return true;
                 }
                 player.sendMessage("Usage is /fkick <player>");
                 return true;
                 }
                 
                 if(split[0].equalsIgnoreCase("/fdisband")){
                  if(!sql.keyExists(name)){
                         player.sendMessage("§cYou are not in a faction!");
                         return true;
                     }
                 if(sql.getRank(name) > 0){
                     player.sendMessage("§cYou aren't the owner of the faction!");
                     return true;
                 }   
                 
                 if(!disband.containsKey(player)){
                     player.sendMessage("§cWARNING! Are you sure you want to disaband your faction?");
                     player.sendMessage("§cType /fdisband yes to disband or /fdisband no to cancel");
                     disband.put(player, true);
                     return true;
                 }
                 if(split.length == 2 && disband.containsKey(player)){
                     if(split[1].equalsIgnoreCase("yes")){
                         player.sendMessage("§cFaction disbanded");
                         sql.disbandFaction(name);
                         disband.remove(player);
                         return true;
                     }
                     else if(split[1].equalsIgnoreCase("no")){
                         player.sendMessage("§6Disband order removed");
                         disband.remove(player);
                         return true;
                         
                     }
                 }
                 }
                 if(split[0].equalsIgnoreCase("/fo") || split[0].equalsIgnoreCase("/fowner")){
                  
                 if(split.length == 2){
                     if(!sql.keyExists(name)){
                         player.sendMessage("§cYou are not in a faction!");
                         return true;
                     }
                 if(sql.getRank(name) > 0){
                     player.sendMessage("§cYou aren't the owner of the faction!");
                     return true;
                 } 
                     Player p2 = etc.getServer().matchPlayer(split[1]);
                     if(p2!=null)
                         split[1] = p2.getName();
                  if(!sql.keyExists(split[1])){
                         player.sendMessage("§c" + split[1] + " is not in a faction!");
                         return true;
                     }
                      if(!sql.getFaction(name).equalsIgnoreCase(sql.getFaction(split[1]))){
                         player.sendMessage("§c" + split[1] + " is not in your faction!")  ;
                         return true;
                     }  
                     if(name.equalsIgnoreCase(split[1])){
                         player.sendMessage("§cYou cannot give ownership to yourself");
                         return true;
                     }
                     
                     sql.setRank(name, 1);
                     sql.setRank(split[1], 0);
                     sql.newOwner(split[1], sql.getFaction(name));
                     player.sendMessage("§6You have set " + split[1] + " as the new owner.");
                     
                     return true;
                     
                     
                 }
                 else if(split.length == 3){
                     if(!player.canUseCommand("/fownership")){
                         return false;
                     }
                     Player p2 = etc.getServer().matchPlayer(split[2]);
                     if(p2 !=null)
                         split[2] = p2.getName();
                     
                     if(!sql.keyExists(split[2]) || !sql.getFaction(split[2]).equalsIgnoreCase(split[1])){
                         player.sendMessage("§c" + split[2] + " is not in the target faction.");
                         return true;
                     }
                     String owner = sql.getOwner(split[1]);
                     sql.setRank(owner, 1);
                     sql.setRank(split[2], 0);
                     sql.newOwner(split[2], sql.getFaction(split[2]));
                     return true;
                 }
                 player.sendMessage("§cUsage is /fowner [faction] <player>");
                 return true;
                 }
                 if(split[0].equalsIgnoreCase("/ft") || split[0].equalsIgnoreCase("/ftitle")){
                     if(!sql.keyExists(name)){
                         player.sendMessage("§cYou are not in a faction!");
                         return true;
                     }
                 if(sql.getRank(name) > 2){
                     player.sendMessage("§cYou aren't high enough rank to change titles!");
                     
                     return true;
                 } 
                 if(split.length == 3){
                     Player p2 = etc.getServer().matchPlayer(split[1]);
                     String n2 = "";
                     if(p2 !=null)
                         split[1] = p2.getName();
                     n2 = split[1];
                     if(!sql.keyExists(split[1])){
                         player.sendMessage("§c" + split[1] + " is not in a faction!");
                         return true;
                     }
                      if(!sql.getFaction(name).equalsIgnoreCase(sql.getFaction(split[1]))){
                         player.sendMessage("§c" + split[1] + " is not in your faction!")  ;
                         return true;
                     }
                     String colored = parseColors(split[2]);
                     sql.setTitle(n2,colored);
                     player.sendMessage("§6Changed " + n2 + "'s title to " + colored);
                     if(p2!=null){
                         p2.sendMessage("§6Title changed to " + colored);
                     }
                     return true;
                     
                 }
                 else if(split.length == 2){
                  String colored = parseColors(split[1]);
                     sql.setTitle(name,colored);
                     player.sendMessage("§6Changed your title to " + colored);
                     return true;
                 }
                 player.sendMessage("§cUsage is /ftitle [player] <title>");
                 return true;
                 }
             }
             if(split[0].equalsIgnoreCase("/flist") || split[0].equalsIgnoreCase("/fl")){
                 sql.factionList(player);
                 return true;
             }
             return false;
 	}
         public String parseColors(String value) {
       String out = "";
       String symbol = "&";
       
 
       String replace = value.replaceAll(symbol, "§");
       if (value.contains(symbol + "4")) value = replace;
       else if (value.contains(symbol + "c")) value = replace;
       else if (value.contains(symbol + "6")) value = replace;
       else if (value.contains(symbol + "e")) value = replace;
       else if (value.contains(symbol + "2")) value = replace;
       else if (value.contains(symbol + "a")) value = replace;
       else if (value.contains(symbol + "b")) value = replace;
       else if (value.contains(symbol + "3")) value = replace;
       else if (value.contains(symbol + "1")) value = replace;
       else if (value.contains(symbol + "9")) value = replace;
       else if (value.contains(symbol + "d")) value = replace;
       else if (value.contains(symbol + "5")) value = replace;
       else if (value.contains(symbol + "f")) value = replace;
       else if (value.contains(symbol + "7")) value = replace;
       else if (value.contains(symbol + "8")) value = replace;
       else if (value.contains(symbol + "0")) value = replace;
       
       out = value;
       out = out + "§f";
       return out;
     }
         public String getStars(int rank){
             String out = "";
             
             if(rank == 0)
                 out = "*****";
             if(rank == 1)
                 out = "****";
             if(rank == 2)
                 out = "***";
             if(rank == 3)
                 out = "**";
             if(rank == 4)
                 out = "*";
            
             return out;
         }
         
         public void sendCommands(Player player){
           player.sendMessage("§6/fcreate <Faction> | /fc - §fCreates faction or renames faction in progress");
           player.sendMessage("§6/finvite <player> | /fi - §fInvites player to faction or requests a signature");
           player.sendMessage("§6/fpromote <player> | /fp - §fPromotes player in your faction");
           player.sendMessage("§6/fdemote <player> | /fd - §fDemotes player in your faction");
           player.sendMessage("§6/ftitle <player> <title> | /ft - §fChanges title of player");
           player.sendMessage("§6/fchat <message> | /fb - §fChats to your faction");
           player.sendMessage("§6/fkick <player> | /fk - §fKicks player from your faction");
           player.sendMessage("§6/fquit | /fq - §fLeaves the faction");
           player.sendMessage("§c/fdisband <yes|no> - §fDisbands the faction you own");
           player.sendMessage("§6/fowner <player> | /fo - §fChanges the ownership of your faction to the player");
           player.sendMessage("§6/flist | /fl - §fShows all factions on the server ordered by most members.");
           if(player.canUseCommand("/fownership"))
           player.sendMessage("§c/fowner [faction] <player> - §fForces the ownership unto target player.");
             
         }
         public String stringBuilder(String[] m){
             String out = "";
             String delim = " ";
             for (int i = 0; i < m.length; i++) {
       if (i != 0) out = out + delim;
       out = out + m[i];
     }
             out = trimLeft(out);
             return out;
         }
         public static String trimLeft(String s) {
     return s.replaceAll("^\\s+", "");
 }
         public boolean isAlreadyInvited(String[] players, String invitee, Player player){
             boolean invited = false;
             for(int i=0;i < players.length;i++){
                
                 if(players[i].equalsIgnoreCase(invitee)){
                     invited = true;
                     
                     break;
                 }
             }
             return invited;
         }
 }
