 package au.com.mineauz.SkyQuest.pedestals;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import au.com.mineauz.SkyQuest.Book;
 import au.com.mineauz.SkyQuest.quests.Quest;
 import au.com.mineauz.SkyQuest.quests.QuestFactory;
 
 public class BlankQuestPedestal extends PedestalBase{
 	
 	public BlankQuestPedestal(Location location){
 		super(new ItemStack(Material.SMOOTH_BRICK, 1, (short)3), new ItemStack(Material.PAPER), location);
 	}
 	
 	public BlankQuestPedestal(){
		
 	}
 
 	@Override
 	protected void onPlayerActivatePedestal(Player player) {
 		if(player.getItemInHand().getType() == Material.BOOK_AND_QUILL){
 			Book questBook = new Book(player.getItemInHand());
 			boolean successful = false;
 			Quest quest = null;
 			String problem = "";
 			try{
 				quest = new Quest(questBook);
 				successful = true;
 			}
 			catch(IllegalArgumentException e){
 				problem = e.getCause().getMessage();
 				successful = false;
 			}
 			
 			if(successful){
 				QuestFactory.addQuest(quest.getQuestName(), quest);
 				Location loc = this.getLocation();
				this.getLocation().getBlock().breakNaturally();
 				Pedestals.addPedestal(new QuestPedestal(quest.getQuestName(), loc));
 			}
 			else
 				player.sendMessage(ChatColor.RED + "Error: " + problem);
 		}
 		else{
 			player.sendMessage("This is a blank pedestal. Use a quest template on this pedestal.");
 		}
 		
 	}
 
 	@Override
 	protected void onPedestalDestroyed() {
 		Pedestals.removePedestal(getLocation());
 	}
 
 }
