 package com.zajacmp3.keepshit;
 
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 import java.sql.*;
 
 public class KeepShit extends JavaPlugin implements Listener{
 	@SuppressWarnings("rawtypes")
 	static List dropList;
 	public void onDisable(){
 		System.out.println("KeepShit is beeing disabled");
 	}
 	public void onEnable(){
 		try {
 			load();
 		} catch (Exception e) {
 			
 		}
 		PluginDescriptionFile desc = this.getDescription();
 		System.out.println("KeepShit "+desc.getVersion()+" is beeing enabled");
 		EventListener(this);
 	}
 	private void load() throws Exception{
 		Class.forName("org.sqlite.JDBC");
 	}
 	private void EventListener(KeepShit keepShit) {
 		Bukkit.getServer().getPluginManager().registerEvents(this, this);		
 	}
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onDeath(final EntityDeathEvent event) throws SQLException{
 		if(event.getEntity() instanceof Player){
 			Player player = (Player) event.getEntity();
 			List<ItemStack> list = event.getDrops();
 			int size = list.size();
			Connection conn = DriverManager.getConnection("jdbc:sqlite:plugins/KeepShit/"+player.getName()+".db");
 			conn.createStatement().execute("Create Table "+player.getName()+"(id int primary key, TypeID int, Amount int)");
 			for(int temp = 0;temp<size;temp++){
 				conn.createStatement().execute("Insert into "+player.getName()+" Values("+(temp+1)+","+list.get(temp).getTypeId()+","+list.get(temp).getAmount()+")");
 			}
 			event.getDrops().clear();
 
 		}
 	}
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void whenSpawned(final PlayerRespawnEvent event) throws SQLException{
 		Player player = event.getPlayer();
		Connection conn = DriverManager.getConnection("jdbc:sqlite:plugins/KeepShit/"+player.getName()+".db");
 		ResultSet data = conn.createStatement().executeQuery("Select * From "+player.getName());
 		while(data.next()){
 			int TypeID = data.getInt("TypeID");
 			int Amount = data.getInt("Amount");
 			player.getInventory().addItem(new ItemStack(TypeID,Amount));
 		}
 		conn.createStatement().execute("Drop Table "+player.getName());
 	}
 /*	@EventHandler(priority = EventPriority.NORMAL)
 	public void whenDropItem(final PlayerDropItemEvent event){
 		Player player = event.getPlayer();
 		Item item = event.getItemDrop();
 		item.remove();
 		player.getInventory().addItem(item.getItemStack());
 	}*/
 }
