 package nl.lolmen.apply;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import nl.lolmen.apply.Applicant.todo;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 
 public class Listeners implements Listener{
 
 	private Main plugin;
 
 	public Listeners(Main plugin){
 		this.plugin = plugin;
 	}
 
 	@EventHandler
 	public void onSignChange(SignChangeEvent event){
 		if(event.isCancelled()){
 			return;
 		}
 		if(event.getLine(0).equalsIgnoreCase("[Apply]")){
 			if(!event.getPlayer().hasPermission("apply.createSign")){
 				event.getPlayer().sendMessage("You can't do that :O");
 				event.setCancelled(true);
 				return;
 			}
 			event.setLine(1, ChatColor.GREEN + "Hit this sign");
 			event.setLine(2, ChatColor.GREEN + "to apply to Cent");
 			event.setLine(3, ChatColor.GREEN + "and start fun!");
 		}
 	}
 
 	@EventHandler
 	public void onPlayerJoin(PlayerJoinEvent event){
 		if(event.getPlayer().hasPermission("apply.check")){
 			ResultSet set = this.getMySQL().executeQuery("SELECT * FROM " + this.getTable() + " WHERE promoted=0");
 			if(set == null){
 				return;
 			}
 			try {
 				set.last();
 				if(set.getRow() == 0){
 					return;
 				}
 				if(set.getRow() == 1){
 					event.getPlayer().sendMessage("There is 1 application waiting!");
 					return;
 				}
 				event.getPlayer().sendMessage("There are " + set.getRow() + " applications waiting!");
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	private MySQL getMySQL(){
 		return this.getPlugin().getMySQL();
 	}
 	
 	private Main getPlugin(){
 		return this.plugin;
 	}
 	
 	private String getTable(){
 		return this.getPlugin().getSettings().getTable();
 	}
 	
 	@EventHandler
 	public void onPlayerInteract(PlayerInteractEvent event){
 		if(event.isCancelled()){
 			return;
 		}
 		if(event.getAction().equals(Action.LEFT_CLICK_BLOCK) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
 			if(!event.getClickedBlock().getType().equals(Material.WALL_SIGN)){
 				return;
 			}
 			Sign s = (Sign)event.getClickedBlock().getState();
 			if(!s.getLine(0).equalsIgnoreCase("[Apply]")){
 				return;
 			}
 			//Check if the player already is applied, applying or whatever
 			ResultSet set = this.getMySQL().executeQuery("SELECT * FROM " + this.getTable() + " WHERE player='" + event.getPlayer().getName() + "' LIMIT 1");
 			if(set == null){
 				event.getPlayer().sendMessage("Something went wrong with MySQL.. not sure why.");
 				return;
 			}
 			try {
 				while(set.next()){
 					boolean promoted = (set.getInt("promoted") == 0 ? false : true);
 					if(promoted){
 						if(event.getPlayer().hasPermission("apply.check")){
 							this.getPlugin().getServer().dispatchCommand(this.getPlugin().getServer().getConsoleSender(), "apply");
 						}else{
 							event.getPlayer().sendMessage("You've already been promoted, by " + (set.getString("promoter").equals(null) ? "No-one? eeh.. okay." : set.getString("promoter")));
 						}
 						return;
 					}else{
 						if(set.getString("country")!= null){
 							if(this.getPlugin().list.containsKey(event.getPlayer().getName())){
 								event.getPlayer().sendMessage("Please confirm your application: /apply");
 								return;
 							}
 							event.getPlayer().sendMessage("A moderator will look at your application soon!");
 						}else{
 							event.getPlayer().sendMessage("You have to finish the application first!");
 						}
 						return;
 					}
 				}
 				//Doesn't contain his name, didn't start yet.
 				this.getPlugin().list.put(event.getPlayer().getName(), new Applicant(this.getPlugin(), event.getPlayer()));
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerChat(PlayerChatEvent event){
 		Player p = event.getPlayer();
 		for(String pl: this.getPlugin().list.keySet()){
 			event.getRecipients().remove(this.getPlugin().getServer().getPlayer(pl));
 		}
 		if(this.getPlugin().list.containsKey(p.getName())){
 			event.setCancelled(true);
 			Applicant c = this.getPlugin().list.get(p.getName());
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
