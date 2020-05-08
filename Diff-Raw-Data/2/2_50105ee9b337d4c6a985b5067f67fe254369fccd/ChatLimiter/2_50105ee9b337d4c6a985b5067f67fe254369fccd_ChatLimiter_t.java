 /**
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 3
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  * 
  */
 
 package amrcci;
 
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 
 public class ChatLimiter implements Listener {
 
 	private Main main;
 	private Config config;
 	public ChatLimiter(Main main, Config config)
 	{
 		this.main = main;
 		this.config = config;
 	}
 	
 	private ConcurrentHashMap<String, Long> playerspeaktime = new ConcurrentHashMap<String, Long>();
 	private ConcurrentHashMap<String, Integer> playerspeakcount = new ConcurrentHashMap<String, Integer>();
 
 	
 	@EventHandler(priority=EventPriority.HIGH,ignoreCancelled=true)
 	public void onChat(AsyncPlayerChatEvent e)
 	{
 		if (!config.chatlimiterenabled) {return;}
 		
 		final String playername = e.getPlayer().getName();
 		if (playerspeaktime.containsKey(playername))
 		{
 			if (System.currentTimeMillis()-playerspeaktime.get(playername) < config.chatlimitermsecdiff)
 			{
				e.getPlayer().sendMessage(ChatColor.RED+"Можно говорить только раз в "+config.chatlimitermsecdiff/1000+" секунд");
 				e.setCancelled(true);
 				return;
 			} else
 			{
 				playerspeaktime.remove(playername);
 			}
 		} else
 		{
 			playerspeaktime.put(playername, System.currentTimeMillis());
 		}
 		if (playerspeakcount.containsKey(playername))
 		{
 			if (playerspeakcount.get(playername) > config.chatlimitermaxmessagecount)
 			{
 				e.getPlayer().sendMessage(ChatColor.RED+"Вы исчерпали свой лимит сообщений на этот час");
 				e.setCancelled(true);
 				return;
 			} else
 			{
 				playerspeakcount.put(playername, playerspeakcount.get(playername)+1);
 			}
 		} else
 		{
 			playerspeakcount.put(playername, 1);
 			Bukkit.getScheduler().scheduleSyncDelayedTask(main, new Runnable()
 			{
 				public void run()
 				{
 					playerspeakcount.remove(playername);
 				}
 			} ,20*60*60);
 		}
 	}
 
 	
 	
 }
