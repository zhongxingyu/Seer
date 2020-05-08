 package tzer0.Games;
 
 import java.util.LinkedList;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.util.config.Configuration;
 
 
 import com.nijiko.permissions.PermissionHandler;
 
 // TODO: Auto-generated Javadoc
 /**
  * The is a listener interface for receiving PlayerCommandPreprocessEvent events.
  * 
  */
 public class GamesPlayerListener extends PlayerListener  {
     Configuration conf;
     private final Games plugin;
     public PermissionHandler permissions;
 
     public GamesPlayerListener(final Games plugin) {
         this.plugin = plugin;
     }
 
     /**
      * Sets the pointers so that they can be referenced later in the code
      *
      * @param permissions Permissions-handler (if available)
      */
     public void setPointers(Configuration config, PermissionHandler permissions) {
         conf = config;
         this.permissions = permissions;
     }
     public void onPlayerInteract(PlayerInteractEvent event) {
         if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
             return;
         }
         Player pl = event.getPlayer();
         if (event.getClickedBlock().getType() == Material.WALL_SIGN || event.getClickedBlock().getType() == Material.SIGN_POST) {
             Sign tmp = (Sign)event.getClickedBlock().getState();
             String lines[] = tmp.getLines();
             Board brd = plugin.findBoard(tmp.getLine(1));
             if (lines[0].equalsIgnoreCase(ChatColor.DARK_GREEN + "[gol]")) {
                 if (brd == null) {
                     pl.sendMessage(ChatColor.RED + "Could not find board.");
                 } else if (! (brd instanceof GOL)) {
                     pl.sendMessage(ChatColor.RED + "Board is of incorrect type.");
                 } else {
                     ((GOL)brd).handleSignal(tmp, pl);
                     return;
                 }
             } else if (lines[0].equalsIgnoreCase(ChatColor.DARK_GREEN+"[tetris]")) {
                 if (brd == null) {
                     pl.sendMessage(ChatColor.RED + "Could not find board.");
                 } else if (! (brd instanceof Tetris)) {
                     pl.sendMessage(ChatColor.RED + "Board is of incorrect type.");
                 } else {
                     ((Tetris)brd).handleSignal(tmp, pl);
                     return;
                 }
            } else if (lines[0].equalsIgnoreCase(ChatColor.DARK_GREEN + "[cont]")) {
                 pl.sendMessage(ChatColor.RED + "This sign cannot be directly executed.");
             }
         }
         LinkedList<Board> list = plugin.boards.get(pl.getWorld());
         if (list != null) {
             for (Board tmp : list) {
                 if (tmp instanceof Interactable) {
                     if (((Interactable)tmp).isInBoard(event.getClickedBlock())) {
                         ((Interactable)tmp).interact(event.getClickedBlock(), pl);
                     }
                 }
             }
         }
         /*if (plugin.temp.isInBoard(event.getClickedBlock())) {
             event.setCancelled(true);
             plugin.temp.modField(event.getClickedBlock());
         }*/
     }
 }
