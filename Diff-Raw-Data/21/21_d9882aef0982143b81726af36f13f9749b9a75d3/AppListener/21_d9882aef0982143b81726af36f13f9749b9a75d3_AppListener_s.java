 package nl.lolmen.apply;
 
 import nl.lolmen.apply.Applicant.todo;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 
 public class AppListener implements Listener{
 	public Main plugin;
 	public AppListener(Main m){
 		plugin = m;
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerJoin(final PlayerJoinEvent event){
 		if(plugin.perm.getUser(event.getPlayer()).inGroup("not-applied") && ! (plugin.perm.getUser(event.getPlayer()).inGroup("applied") || plugin.perm.getUser(event.getPlayer()).inGroup("Moderator"))){
 			Bukkit.getServer().getScheduler().scheduleAsyncDelayedTask(new Main(), new Runnable(){
 				public void run() {
					event.getPlayer().sendMessage("You have not been applied yet! Please type /apply to write your application!");
 				}
 			}, 60L);
 		}			
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerChat(PlayerChatEvent event){
 		Player p = event.getPlayer();
 		for(Player pl: plugin.list.keySet()){
 			event.getRecipients().remove(pl);
 		}
 		if(plugin.list.containsKey(p)){
 			event.setCancelled(true);
 			Applicant c = plugin.list.get(p);
 			todo t = c.getNext();
 			switch (t) {
 			case GOODAT:
 				c.setGoodat(event.getMessage());
 				c.setNext(todo.BANNED);
 				p.sendMessage("");
 				p.sendMessage(ChatColor.RED +"All right. " + ChatColor.WHITE + "Next question: Have you ever been" +ChatColor.RED + " banned" + ChatColor.WHITE +" before?");
 				p.sendMessage("And if yes, " + ChatColor.RED +  "why?");
 				return;
 			case BANNED:
 				c.setBanned(event.getMessage());
 				c.setNext(todo.NAME);
 				p.sendMessage("");
 				p.sendMessage("Okay. We're almost done! The last 3 questions " + ChatColor.RED +  "don't have to be true.");
 				p.sendMessage("What is your " + ChatColor.RED + "first name?");
 				return;
 			case NAME:
 				c.setName(event.getMessage());
 				c.setNext(todo.AGE);
 				p.sendMessage("");
 				p.sendMessage("Alright, " + ChatColor.RED +  "almost done!");
 				p.sendMessage("How " + ChatColor.RED +  "old" + ChatColor.WHITE+" are you?");
 				return;
 			case AGE:
 				c.setAge(event.getMessage());
 				c.setNext(todo.COUNTRY);
 				p.sendMessage("");
 				p.sendMessage(ChatColor.GREEN + "Last question!");
 				p.sendMessage("In what " + ChatColor.RED +  "country" + ChatColor.WHITE +" do you live?");
 				return;
 			case COUNTRY:
 				c.setCountry(event.getMessage());
 				c.setNext(todo.CONFIRM);
 				p.sendMessage("");
 				p.sendMessage("Okay, that were all the questions! Could you look if this is alright?");
 				p.sendMessage("Good at: " + ChatColor.RED + c.getGoodat());
 				p.sendMessage("Banned: " + ChatColor.RED + c.getBanned());
 				p.sendMessage("Name: " + ChatColor.RED + c.getName());
 				p.sendMessage("Age: " + ChatColor.RED + c.getAge());
 				p.sendMessage("Country: " + ChatColor.RED + c.getCountry());
 				p.sendMessage("If this is alright, type " +ChatColor.RED + "/apply " + ChatColor.WHITE + "To confirm! Otherwise, type " + ChatColor.RED + "/apply reset");
 				return;
 			}
 		}
 	}
 
 }
