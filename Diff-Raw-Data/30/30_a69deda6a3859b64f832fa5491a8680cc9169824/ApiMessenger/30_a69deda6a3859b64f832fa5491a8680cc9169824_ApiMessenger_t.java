 package com.gmail.ne0nx3r0.rareitems.http;
 
 import com.gmail.ne0nx3r0.persistence.PlayerProfile;
 import com.gmail.ne0nx3r0.rareitems.RareItems;
 import com.gmail.ne0nx3r0.rareitems.item.ItemProperty;
 import com.gmail.ne0nx3r0.rareitems.item.RareItem;
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Queue;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 
 public class ApiMessenger
 {
     private int TASK_ID;
     private int LAST_SENT_ID;
     
     private HashMap<String,Long> LastChecked = new HashMap<>();
     Queue<String> q = new LinkedList<>();
     
     public ApiMessenger()
     {
         LAST_SENT_ID = 0;
         
        TASK_ID = Bukkit.getScheduler().scheduleAsyncRepeatingTask(RareItems.self,new Runnable(){
             @Override
             public void run()
             {
                 if(Bukkit.getOnlinePlayers().length > 0)
                 {
                     ArrayList<String> playersToFetch = new ArrayList<>();
                     
                     for(int i=0;i<30 && i<q.size();i++)
                     {
                         String sPlayerNameLowerCase = q.poll();
                         
                         if(Bukkit.getPlayer(sPlayerNameLowerCase) != null)
                         {
                             playersToFetch.add(sPlayerNameLowerCase);
 
                             q.add(sPlayerNameLowerCase);
                         }
                     }

                    if(!playersToFetch.isEmpty())
                    {
                        ApiMessenger.fetchPlayerRareItems(
                                playersToFetch.toArray(new String[playersToFetch.size()]),true);
                    }
                 }
             }
         }, 20*60*30, 20*60*30);
     }
                
     public static void fetchPlayerRareItems(int id,boolean onlyPending)
     {
         fetchPlayerRareItems("ids="+id,onlyPending);
     }
 
     public static void fetchPlayerRareItems(Player p,boolean onlyPending)
     {
         fetchPlayerRareItems("players="+p.getName().toLowerCase(),onlyPending);
     }
     
     public static void fetchPlayerRareItems(String[] sPlayerNames,boolean onlyPending)
     {
         String sQuery = "";
         
         for(int i=0;i<sPlayerNames.length;i++)
         {
             sQuery += sPlayerNames[i].toLowerCase()+",";
         }
         
         fetchPlayerRareItems("players="+sQuery.substring(0,sQuery.length()-1),onlyPending);
     }    
     
     public static void fetchPlayerRareItems(Player[] players,boolean onlyPending)
     {
         String sQuery = "";
         
         for(Player p : players)
         {
             sQuery += p.getName().toLowerCase()+",";
         }
         
         fetchPlayerRareItems("players="+sQuery.substring(0,sQuery.length()-1),onlyPending);
     }
     
     public static void fetchPlayerRareItems(final String sQuery,final boolean onlyPending)
     {
         if(RareItems.DEBUG_MODE)
         {
             RareItems.logger.log(Level.INFO, "query:{0}&onlyPending={1}&serverPort={2}",
                     new Object[]{sQuery, onlyPending?'1':'0', Bukkit.getServer().getPort()});
         }
         (new Thread(){
             @Override
             public void run()
             {
                 try{
                     URLConnection connection = new URL("http://www.rareitemsplugin.tk/api2/getPlayerData/").openConnection();
                     connection.setDoOutput(true);
                     connection.setDoInput(true);
 
                     try(OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream()))
                     {
                         writer.write(sQuery
                             +"&onlyPending="+(onlyPending?'1':'0')
                             +"&serverPort="+Bukkit.getServer().getPort()
                         );
                         writer.flush();
                     }
 
                     BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
 
                     String line;
                     String returnString = "";
 
                     while ((line = reader.readLine()) != null)
                     {
                         returnString += line;
                     }
                     
                     final String sReturnString = returnString;
 
                     Bukkit.getScheduler().scheduleSyncDelayedTask(RareItems.self,new Runnable(){
                         @Override
                         public void run()
                         {
                             ApiMessenger.receivePlayersUpdate(sReturnString);
                         }
                     });
                 }
                 catch (Exception ex)
                 {
                     System.out.println("query:"+sQuery
                             +"&onlyPending="+(onlyPending?'1':'0')
                             +"&serverPort="+Bukkit.getServer().getPort());
                     RareItems.logger.log(Level.SEVERE, null, ex);
                 }
             }
         }).start();
     }
     
     public static void receivePlayersUpdate(String response)
     {
         if(RareItems.DEBUG_MODE)
         {
             RareItems.logger.log(Level.INFO, "Response: {0}", response);
         }
         
         try
         {
             JSONObject json = (JSONObject) (new JSONParser()).parse(response);
             
             if(((String) json.get("status")).equals("success") && json.get("players") != null)
             {
                 Map<String,Object> playersData = (Map<String,Object>) json.get("players");
                 
                 for(String sPlayerName : playersData.keySet())
                 {
                     HashMap<String,Object> playerData = (HashMap<String,Object>) playersData.get(sPlayerName);
                     
                     //verify player is still online
                     Player p = Bukkit.getPlayer(sPlayerName);
                     int iSiteId = Integer.parseInt((String) playerData.get("siteId"));
                     
                     if(p != null)
                     {
                         PlayerProfile pp = RareItems.pm.getPlayerProfile(sPlayerName,iSiteId);
                         
                         pp.setMoney(Integer.parseInt((String) playerData.get("money")));
                         
                         pp.removeAllRareItems();
                         
                         Map<String,Object> pendingItems = (Map<String,Object>) playerData.get("items");
                             
                         for(String sPendingItemId : pendingItems.keySet())
                         {   
                             Map<String,Object> pendingItem = (Map<String,Object>) pendingItems.get(sPendingItemId);
                             
                             HashMap<ItemProperty,Integer> ips = new HashMap<>();
                             
                             Map<String,String> pendingItemProperties = (Map<String,String>) pendingItem.get("ip");
                             for(String sIpId : pendingItemProperties.keySet())
                             {
                                 int ipId = Integer.parseInt(sIpId);
                                 
                                 ItemProperty ip = RareItems.ipm.getItemProperty(ipId);
 
                                 if(ip != null)
                                 {
                                     int ipLevel = Integer.parseInt(pendingItemProperties.get(sIpId));
 
                                     ips.put(
                                         ip,
                                         ipLevel
                                     );
                                 }
                                 else
                                 {
                                     RareItems.logger.log(Level.WARNING, "Server returned an invalid ItemProperty ID{0}! (is RareItems up to date?)", ipId);
                                     
                                     for(Player pAnnounceTo : Bukkit.getOnlinePlayers())
                                     {
                                         if(pAnnounceTo.isOp() || pAnnounceTo.hasPermission("rareitems.errors"))
                                         {
                                             pAnnounceTo.sendMessage("Server returned an invalid ItemProperty ID"+ipId+"! (is RareItems up to date?)");
                                         }
                                     }
                                 }
                             }
                             
                             RareItem ri = new RareItem(
                                 Integer.parseInt(sPendingItemId),
                                 sPlayerName,
                                 Integer.parseInt((String) pendingItem.get("m")),
                                 Byte.parseByte((String) pendingItem.get("dv")),
                                 ips
                             );
                             
                             pp.addRareItem(ri);
                             
                             if(((String) pendingItem.get("p")).equals("1"))
                             {
                                 RareItems.self.getServer().broadcastMessage("-------------------    RareItems   -----------------");
                                 RareItems.self.getServer().broadcastMessage(p.getName() + " scored a "+ri.getDisplayName()+"!");
                                 RareItems.self.getServer().broadcastMessage("----------------------------------------------------");
                             
                                 p.sendMessage(ChatColor.GOLD+"(You can claim this item with /ri claim)");
                             }
                         }
                         
                         RareItems.pm.savePlayerProfile(pp);
                     }
                 }
                 
             }
         }
         catch(ParseException ex)
         {
             System.out.println("Response: "+response);
             Logger.getLogger(ApiMessenger.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public void removePlayerFromQueue(Player p)
     {
         q.remove(p.getName().toLowerCase());
     }
 
     public void addPlayerToQueue(Player p)
     {
         q.add(p.getName().toLowerCase());
     }
 }
