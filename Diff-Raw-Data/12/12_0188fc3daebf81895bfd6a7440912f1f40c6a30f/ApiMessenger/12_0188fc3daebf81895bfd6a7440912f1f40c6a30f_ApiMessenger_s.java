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
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 
 public class ApiMessenger
 {
     private int TASK_ID;
     
     public ApiMessenger()
     {
         TASK_ID = Bukkit.getScheduler().scheduleAsyncRepeatingTask(RareItems.self,new Runnable()
         {
             @Override
             public void run()
             {
                ApiMessenger.fetchPlayerRareItems(Bukkit.getOnlinePlayers(),true);
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
     
     public static void fetchPlayerRareItems(ArrayList<Integer> ids,boolean onlyPending)
     {
         String sQuery = "";
         
         for(int id : ids)
         {
             sQuery += id+",";
         }
         
         fetchPlayerRareItems("ids="+sQuery.substring(0,sQuery.length()-1),onlyPending);
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
                     System.out.println("query:"+sQuery
                             +"&onlyPending="+(onlyPending?'1':'0')
                             +"&serverPort="+Bukkit.getServer().getPort());
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
         System.out.println("Response: "+response);
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
                                 
                                 ItemProperty ip = RareItems.rig.getItemProperty(ipId);
 
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
                                     RareItems.logger.log(Level.WARNING,"Server returned an invalid ItemProperty ID"+ipId+"! (is RareItems up to date?)");
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
                                 RareItems.self.getServer().broadcastMessage("----------------------------------------------------");
                                 RareItems.self.getServer().broadcastMessage(sPlayerName + " scored a "+ri.getDisplayName()+"!");
                                 RareItems.self.getServer().broadcastMessage("----------------------------------------------------");
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
 }
