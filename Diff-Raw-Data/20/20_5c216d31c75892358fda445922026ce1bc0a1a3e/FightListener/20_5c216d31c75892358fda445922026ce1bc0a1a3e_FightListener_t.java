 package fr.aumgn.diamondrush.stage.listeners;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.inventory.ItemStack;
 
 import fr.aumgn.diamondrush.DiamondRush;
 import fr.aumgn.diamondrush.game.Game;
 import fr.aumgn.diamondrush.game.Team;
 import fr.aumgn.diamondrush.stage.FightStage;
 
 public class FightListener implements Listener {
 
     private FightStage stage;
     private Set<Player> itemsToGiveAterRespawn;
 
     public FightListener(FightStage stage) {
         this.stage = stage;
         this.itemsToGiveAterRespawn = new HashSet<Player>();
     }
 
     @EventHandler
     public void onDeath(PlayerDeathEvent event) {
         Player player = event.getEntity();
         EntityDamageEvent damageCause = player.getLastDamageCause();
         if (!(damageCause instanceof EntityDamageByEntityEvent)) {
             return;
         }
 
         Entity damagerEntity = ((EntityDamageByEntityEvent) damageCause).getDamager();
         if (damagerEntity instanceof Projectile) {
             damagerEntity = ((Projectile) damagerEntity).getShooter();
         }
         if (!(damagerEntity instanceof Player)) {
             return;
         }
 
         Player damager = (Player) damagerEntity;
         Game game = stage.getGame();
         if (!game.contains(damager) || !game.contains(player)) {
             return;
         }
 
         Team team = game.getTeam(player);
         if (team == game.getTeam(damager)) {
             return;
         }
 
         stage.incrementDeathCount(team);
         if (stage.getDeathCount(team) > DiamondRush.getConfig().getMaxDiamond()) {
             event.getDrops().add(DiamondRush.getConfig().getItemForKill());
         } else {
             event.getDrops().add(new ItemStack(Material.DIAMOND));
         }
         itemsToGiveAterRespawn.add(player);
     }
 
     @EventHandler
     public void onRespawn(PlayerRespawnEvent event) {
         Player player = event.getPlayer();
         if (itemsToGiveAterRespawn.contains(player)) {
             player.getInventory().addItem(DiamondRush.getConfig().getItemForDeath());
             itemsToGiveAterRespawn.remove(player);
         }
     }
 
     @EventHandler
     public void onInteract(PlayerInteractEvent event) {
         if (event.getAction() != Action.RIGHT_CLICK_AIR &&
                 event.getAction() != Action.RIGHT_CLICK_BLOCK) {
             return;
         }
 
         Player player = event.getPlayer();
         Game game = stage.getGame();
         if (!game.contains(player)) {
             return;
         }
 
         ItemStack item = player.getItemInHand();
         int type = item.getTypeId();
         if (type != DiamondRush.getConfig().getSurrenderItem()) {
             return;
         }
 
        if (!stage.canSurrender()) {
            player.sendMessage("Une équipe s'est déjà rendu");
            return;
        }

         Team team = game.getTeam(player);
         if (stage.getDeathCount(team) >= DiamondRush.getConfig().getDeathNeededForSurrender()) {
             if (item.getAmount() == 1) {
                 player.setItemInHand(new ItemStack(0));
             } else {
                 item.setAmount(item.getAmount() - 1);
                 player.setItemInHand(item);
             }
             stage.surrender(team);
         } else {
             player.sendMessage(ChatColor.RED + 
                     "Il faut au moins une mort dans l'equipe pour pouvoir se rendre.");
         }
     }
 }
