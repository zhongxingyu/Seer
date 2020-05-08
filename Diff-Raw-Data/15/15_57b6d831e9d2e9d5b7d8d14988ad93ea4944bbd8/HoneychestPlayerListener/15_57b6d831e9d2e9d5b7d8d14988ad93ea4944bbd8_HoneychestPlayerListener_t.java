 package syam.Honeychest;
 
 import java.util.logging.Logger;
 
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 
 public class HoneychestPlayerListener implements Listener {
 	public final static Logger log = Honeychest.log;
 	private static final String logPrefix = Honeychest.logPrefix;
 	private static final String msgPrefix = Honeychest.msgPerfix;
 
 	private final Honeychest plugin;
 
 	public HoneychestPlayerListener(Honeychest plugin){
 		this.plugin = plugin;
 	}
 
 	/* 登録するイベントはここから下に */
 
 	// プレイヤーがブロックをクリックした
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onPlayerInteract(PlayerInteractEvent event){
 		Player player = event.getPlayer();
 		Block block = event.getClickedBlock();
 
 		// インベントリが閉じられたかチェック
 		Honeychest.containerManager.checkInventoryClose(player);
 
 		if (block != null) {
 			Location loc = block.getLocation();
 
 			switch (block.getType()){
 				case FURNACE:
 				case DISPENSER:
 				case CHEST:
 					if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
 						// コンテナのインベントリを開いた
 						Honeychest.containerManager.checkInventoryOpen(player, block);
 					}
 					break;
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
 	public void createHoneyChest(PlayerInteractEvent event){
 		Player player = event.getPlayer();
 		Block block = event.getClickedBlock();
 		if (block != null) {
 			Location loc = block.getLocation();
 
 			switch (block.getType()){
 				case FURNACE:
 				case DISPENSER:
 				case CHEST:
 					if (event.getAction() == Action.RIGHT_CLICK_BLOCK && HoneyData.isCreator(player)) {
 						if (player.getItemInHand().getTypeId() == plugin.getHCConfig().getToolId() && player.hasPermission("honeychest.manage")){
 							// 管理モードで特定のアイテムを持ったままコンテナブロックを右クリックした
 							if (HoneyData.getHc(loc) == null) {
 								HoneyData.setHc(loc, "*");
 								Actions.message(null, player, "&aハニーチェストを作りました！");
 							}else{
 								HoneyData.removeHc(loc);
 								Actions.message(null, player, "&aハニーチェストを解除しました！");
 							}
 							event.setCancelled(true);
 						}
 					}
 					break;
 			}
 		}
 	}
 
 	/* 他のイベントでコンテナインベントリが閉じられていないかチェックする */
 
 	// チャット
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onPlayerChat(PlayerChatEvent event){
 		// インベントリが閉じられたかチェック
 		Honeychest.containerManager.checkInventoryClose(event.getPlayer());
 	}
 
 	// コマンド
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event){
 		// インベントリが閉じられたかチェック
 		Honeychest.containerManager.checkInventoryClose(event.getPlayer());
 	}
 
 	// テレポート
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onPlayerTeleport(PlayerTeleportEvent event) {
 		// インベントリが閉じられたかチェック
 		Honeychest.containerManager.checkInventoryClose(event.getPlayer());
 	}

	// 切断
	//@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerQuit(PlayerQuitEvent event) {
		// アクセスリストから削除
		Honeychest.containerManager.removeAccessList(event.getPlayer());
	}
 }
