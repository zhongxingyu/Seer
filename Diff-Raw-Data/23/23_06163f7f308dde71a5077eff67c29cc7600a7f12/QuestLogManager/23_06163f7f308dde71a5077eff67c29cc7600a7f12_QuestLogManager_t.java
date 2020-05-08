 package net.skycraftmc.SkyQuest;
 
 import java.util.ArrayList;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryCloseEvent;
 import org.bukkit.inventory.Inventory;
 
 public class QuestLogManager implements Listener
 {
 	private ArrayList<OpenQuestLog> openlogs = new ArrayList<OpenQuestLog>();
 	private SkyQuestPlugin plugin;
 	public QuestLogManager(SkyQuestPlugin plugin)
 	{
 		this.plugin = plugin;
 	}
 	public void openQuestLog(Player player)
 	{
 		if(getOpenQuestLog(player) != null)return;
 		Inventory inv = plugin.getServer().createInventory(null, 36, "Quest Log");
 		PlayerQuestLog log = plugin.getQuestManager().getQuestLog(player.getName());
 		OpenQuestLog o = new OpenQuestLog(player, log, inv);
 		o.update();
 		openlogs.add(o);
 	}
 	public OpenQuestLog getOpenQuestLog(Player player)
 	{
 		for(OpenQuestLog o:openlogs)
 		{
 			if(o.getPlayer() == player)return o;
 		}
 		return null;
 	}
 	@EventHandler
 	public void close(InventoryCloseEvent event)
 	{
 		Player player = (Player) event.getView().getPlayer();
 		OpenQuestLog oql = getOpenQuestLog(player);
		if(oql != null)
		{
			openlogs.remove(oql);
			oql.dispose();
		}
 	}
 	@SuppressWarnings("deprecation")
 	@EventHandler
 	public void click(InventoryClickEvent event)
 	{
 		Player player = (Player) event.getView().getPlayer();
 		OpenQuestLog oql = getOpenQuestLog(player);
 		if(oql == null)return;
 		event.setCancelled(true);
 		if(event.getView() != oql.getInventoryView())
 		{
 			player.updateInventory();
 			return;
 		}
 		if(event.getCurrentItem() == null)
 		{
 			player.updateInventory();
 			return;
 		}
 		Quest sel = oql.getSelected();
 		if(sel == null)
 		{
 			if(event.getSlot() == 27)
 			{
 				oql.setViewingCompleted(!oql.isViewingCompleted());
 				oql.update();
 			}
 			else
 			{
 				int index = (oql.getPage() - 1)*27 + event.getSlot();
 				Quest[] q = oql.isViewingCompleted() ? oql.getCurrentCompletedList() : oql.getCurrentQuestList();
 				if(index < q.length)
 				{
 					oql.setSelected(q[index]);
 					oql.update(); 
 				}
 			}
 		}
 		else if(event.getSlot() == 27)
 		{
 			oql.setSelected(null);
 			oql.update();
 		}
 		player.updateInventory();
 	}
 }
