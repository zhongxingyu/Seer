 package me.limebyte.battlenight.core.listeners;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 
 import me.limebyte.battlenight.api.BattleNightAPI;
 import me.limebyte.battlenight.api.managers.ClassManager;
 import me.limebyte.battlenight.api.util.PlayerClass;
 import me.limebyte.battlenight.core.BattleNight;
 import me.limebyte.battlenight.core.util.ClassSign;
 import me.limebyte.battlenight.core.util.Messenger;
 import me.limebyte.battlenight.core.util.Messenger.Message;
 import me.limebyte.battlenight.core.util.Metadata;
 import me.limebyte.battlenight.core.util.ParticleEffect;
 import me.limebyte.battlenight.core.util.config.ConfigManager;
 import me.limebyte.battlenight.core.util.config.ConfigManager.Config;
 
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.potion.PotionEffect;
 
 public class SignListener extends APIRelatedListener {
 
     private static final String LINE = "----------";
     public static Set<ClassSign> classSigns = new HashSet<ClassSign>();
 
     public SignListener(BattleNightAPI api) {
         super(api);
     }
 
     @EventHandler(priority = EventPriority.NORMAL)
     public void onSignChange(SignChangeEvent e) {
         Player player = e.getPlayer();
         String title = e.getLine(0);
 
         HashMap<String, PlayerClass> classes = new HashMap<String, PlayerClass>();
         ClassManager manager = BattleNight.instance.getAPI().getClassManager();
         for (PlayerClass clazz : manager.getClasses()) {
             classes.put(clazz.getName(), clazz);
         }
 
         if (classes != null) {
             if (classes.containsKey(title)) {
                 if (!e.getLine(1).isEmpty() || !e.getLine(2).isEmpty() || !e.getLine(3).isEmpty()) {
                     Messenger.tell(player, Message.UNSUCCESSFUL_SIGN, title);
                     return;
                 }
 
                 e.setLine(1, LINE);
                 Messenger.tell(player, Message.SUCCESSFUL_SIGN, title);
                 return;
             }
         }
     }
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onPlayerInteract(PlayerInteractEvent event) {
 
         if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
             Block block = event.getClickedBlock();
             Player player = event.getPlayer();
 
             if (block.getState() instanceof Sign) {
                 Sign sign = (Sign) block.getState();
                 String title = sign.getLine(0);
 
                 HashMap<String, PlayerClass> classes = new HashMap<String, PlayerClass>();
                 ClassManager manager = BattleNight.instance.getAPI().getClassManager();
                 for (PlayerClass clazz : manager.getClasses()) {
                     classes.put(clazz.getName(), clazz);
                 }
 
                 if (getAPI().getBattle().containsPlayer(player)) {
                     if (classes.containsKey(title)) {
                         PlayerClass playerClass = classes.get(title);
 
                         if (player.hasPermission(playerClass.getPermission())) {
                             addName(player, sign);
 
                            if (Metadata.getPlayerClass(player) != playerClass) {
                                 ParticleEffect.classSelect(player, ConfigManager.get(Config.MAIN).getString("Particles.ClassSelection", "smoke"));
                             }
 
                             reset(player);
                             getAPI().setPlayerClass(player, classes.get(title));
                         } else {
                             Messenger.tell(player, Message.NO_PERMISSION_CLASS);
                         }
                     }
                 }
             }
         }
     }
 
     private static void addName(Player player, Sign sign) {
         getClassSign(sign).add(player);
     }
 
     public static void cleanSigns() {
         for (ClassSign s : classSigns) {
             s.clear();
         }
     }
 
     public static void cleanSigns(Player player) {
         for (ClassSign s : classSigns) {
             s.remove(player);
         }
     }
 
     private static ClassSign getClassSign(Sign sign) {
         for (ClassSign s : classSigns) {
             if (s.getSign().equals(sign)) return s;
         }
 
         ClassSign s = new ClassSign(sign);
         classSigns.add(s);
         return s;
     }
 
     private static void reset(Player player) {
         player.getInventory().clear();
         player.getInventory().setArmorContents(new ItemStack[player.getInventory().getArmorContents().length]);
         for (PotionEffect effect : player.getActivePotionEffects()) {
             player.addPotionEffect(new PotionEffect(effect.getType(), 0, 0), true);
         }
     }
 }
