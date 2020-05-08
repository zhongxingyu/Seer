 package net.battlenexus.paintball.game.weapon;
 
 import net.battlenexus.paintball.Paintball;
 import net.battlenexus.paintball.entities.PBPlayer;
 import net.battlenexus.paintball.game.PaintballGame;
 import org.bukkit.*;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Snowball;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.util.Vector;
 
 import java.util.Random;
 
 public abstract class AbstractWeapon implements Weapon {
     private PBPlayer owner;
     private int bullets;
     protected int currentClip;
 
     public static AbstractWeapon createWeapon(Class<? extends AbstractWeapon> class_, PBPlayer owner) {
         try {
             AbstractWeapon w = class_.newInstance();
             w.owner = owner;
             return w;
         } catch (InstantiationException e) {
             e.printStackTrace();
         } catch (IllegalAccessException e) {
             e.printStackTrace();
         }
         return null;
     }
 
     protected AbstractWeapon() {
     }
 
     protected void updateGUI() {
         if (owner == null)
             return;
         float max = (float) getMaxBullets();
         float percent = (max == 0.0f ? 0.0f : (float) bullets / max);
         getOwner().getBukkitPlayer().setExp(percent); //TODO Display EXP properly
         getOwner().getBukkitPlayer().setLevel(bullets);
     }
 
     @Override
     public void emptyGun() {
         bullets = 0;
         if (owner == null)
             return;
 
         Inventory inventory = owner.getBukkitPlayer().getInventory();
         ItemStack[] items = owner.getBukkitPlayer().getInventory().getContents();
         for (ItemStack i : items) {
             int c = Weapon.WeaponUtils.getBulletCount(i);
             if (c == 0)
                 continue;
             inventory.remove(i);
         }
         owner.getBukkitPlayer().updateInventory();
         updateGUI();
     }
 
     public int getMaxBullets() {
         if (owner == null)
             return 0;
         ItemStack[] items = owner.getBukkitPlayer().getInventory().getContents();
         int i = 0;
         for (ItemStack item : items) {
             i += Weapon.WeaponUtils.getBulletCount(item);
         }
         return i;
     }
 
     @Override
     public Material getMaterial() {
         if (owner == null || getOwner().getCurrentGame() == null || getOwner().getCurrentTeam() == null)
             return getNormalMaterial();
         else {
             PaintballGame pg = getOwner().getCurrentGame();
             if (pg.getConfig().getBlueTeam().equals(getOwner().getCurrentTeam())) {
                 return getBlueTeamMaterial();
             } else {
                 return getRedTeamMaterial();
             }
         }
     }
 
     @Override
     public void addBullets(int amount) {
         if (owner == null)
             return;
         if (owner.getBukkitPlayer().getInventory().firstEmpty() == -1) {
             combineBullets();
             combineBullets();
             combineBullets();
             if (owner.getBukkitPlayer().getInventory().firstEmpty() == -1)
                 return;
         }
         if (amount <= clipeSize()) {
             ItemStack item = Weapon.WeaponUtils.createReloadItem(getReloadItem(), amount);
             owner.getBukkitPlayer().getInventory().addItem(item);
         } else {
             while (amount > 0) {
                 int t;
                 if (amount - clipeSize() >= 0) {
                     t = clipeSize();
                 } else {
                     t = amount;
                 }
 
                 ItemStack item = Weapon.WeaponUtils.createReloadItem(getReloadItem(), t);
                 owner.getBukkitPlayer().getInventory().addItem(item);
                 amount -= t;
             }
         }
         updateGUI();
     }
 
     public void combineBullets() {
         final Inventory inventory = owner.getBukkitPlayer().getInventory();
         int i = inventory.first(getReloadItem());
         ItemStack item = inventory.getItem(i);
         int bulletCount = Weapon.WeaponUtils.getBulletCount(item);
         inventory.remove(i);
 
         i = inventory.first(getReloadItem());
         if (i == -1) {
             inventory.addItem(item);
             return;
         }
         item = inventory.getItem(i);
         int bulletCount2 = Weapon.WeaponUtils.getBulletCount(item);
         inventory.remove(i);
 
         int newAmount = bulletCount + bulletCount2;
         item = Weapon.WeaponUtils.createReloadItem(getReloadItem(), newAmount);
         inventory.addItem(item);
     }
 
     public abstract Material getBlueTeamMaterial();
 
     public abstract Material getRedTeamMaterial();
 
     public abstract Material getNormalMaterial();
 
     private boolean reloading = false;
 
     @Override
     public void reload(ItemStack item) {
         if (owner == null)
             return;
         if (!reloading) {
             int c = Weapon.WeaponUtils.getBulletCount(item);
             if (c == 0)
                 return;
 
             int bneeded = clipeSize() - bullets;
             if (bneeded == 0) {
                 owner.sendMessage(ChatColor.DARK_RED + "Reload failed! Your gun is full!");
                 return;
             }
             int take = 0;
            float reloadtime = (bneeded / clipeSize()) * reloadDelay();
             if (bneeded == c) {
                 take = c;
             } else if (bneeded < c) {
                 take = bneeded;
             } else if (bneeded > c) {
                 take = c;
             }
             bullets += take;
             reloading = true;
             if (c - take > 0) {
                 Weapon.WeaponUtils.setBulletCount(item, c - take);
             } else {
                 if (item.getAmount() > 1) {
                     item.setAmount(item.getAmount() - 1);
                 } else {
                     owner.getBukkitPlayer().getInventory().clear(owner.getBukkitPlayer().getInventory().getHeldItemSlot());
                     final Inventory i = owner.getBukkitPlayer().getInventory();
                     while (true) {
                         int first_index = i.first(getReloadItem());
                         if (first_index == -1) {
                             reloading = false;
                             owner.sendMessage(ChatColor.DARK_RED + "You're all out!");
                             break;
                         }
                         ItemStack item_to_move = i.getItem(first_index);
                         if (!item_to_move.hasItemMeta() || !item_to_move.getItemMeta().hasDisplayName() || !item_to_move.getItemMeta().hasLore()) {
                             i.remove(first_index);
                             continue;
                         }
                         i.clear(first_index);
                         int clear = i.firstEmpty();
                         i.setItem(clear, item_to_move);
                         break;
                     }
                     owner.getBukkitPlayer().updateInventory();
                 }
             }
             updateGUI();
             owner.getBukkitPlayer().sendMessage(Paintball.formatMessage("Reloading..."));
             owner.getBukkitPlayer().playSound(owner.getBukkitPlayer().getLocation(), Sound.ANVIL_USE, 60, 1);
             displayReloadAnimation(reloadtime);
 
             Runnable stopReload = new Runnable() {
 
                 @Override
                 public void run() {
                     reloading = false;
                     owner.getBukkitPlayer().sendMessage(Paintball.formatMessage(ChatColor.GREEN + "Reloaded!"));
                     updateGUI();
                 }
             };
             Bukkit.getScheduler().scheduleSyncDelayedTask(Paintball.INSTANCE, stopReload, (long) Math.round(reloadtime * 20));
         }
     }
 
     private void displayReloadAnimation(float speed) {
         if (owner == null)
             return;
         owner.getBukkitPlayer().setExp(0);
         final Player thePlayer = owner.getBukkitPlayer();
        final float percentage = 1 / speed;
         Runnable fixTask = new Runnable() {
 
             @Override
             public void run() {
                thePlayer.setExp(thePlayer.getExp() + percentage);
             }
         };
        for (int i = 1; i <= speed; i++) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(Paintball.INSTANCE, fixTask, (long) 20 * i);
         }
     }
 
     @Override
     public void shoot() {
         shoot(0);
     }
 
     @Override
     public int currentClipSize() {
         return bullets;
     }
 
     @Override
     public int totalBullets() {
         return getMaxBullets();
     }
 
     long lastFire = -1;
 
     public void shoot(double spreadfactor) {
         if (owner == null)
             return;
         if (reloading) {
             owner.sendMessage(ChatColor.DARK_RED + "You cant shoot while reloading!");
             return;
         }
         if (bullets < getShotRate()) {
             return;
         }
 
         if (lastFire == -1 || (System.currentTimeMillis() - lastFire) >= getFireDelay()) {
 
             int fire = getShotRate();
             bullets -= fire;
 
             updateGUI();
 
             called = false;
             while (fire > 0) {
                 onShoot(spreadfactor);
                 if (!called)
                     throw new RuntimeException("super.onShoot was not called! Try putting super.onShoot at the top of your method!");
                 fire--;
             }
             lastFire = System.currentTimeMillis();
         }
     }
 
     @Override
     public PBPlayer getOwner() {
         return owner;
     }
 
     private boolean onehit;
     @Override
     public void setOneHitKill(boolean value) {
         this.onehit = value;
     }
 
     @Override
     public boolean isOneHitKill() {
         return onehit;
     }
 
     private boolean called;
 
     protected void onFire(final Snowball snowball, Player bukkitPlayer, double spread) {
         if (bukkitPlayer == null)
             return;
         snowball.setShooter(bukkitPlayer);
         snowball.setTicksLived(2400);
         Vector vector;
         if (spread == 0) {
             vector = bukkitPlayer.getLocation().getDirection().multiply(strength());
         } else {
             final Random random = new Random();
             Location ploc = bukkitPlayer.getLocation();
             double dir = -ploc.getYaw() - 90;
             double pitch = -ploc.getPitch();
             double xwep = ((random.nextInt((int) (spread * 100)) - random.nextInt((int) (spread * 100))) + 0.5) / 100.0;
             //double ywep = ((random.nextInt((int) (spread * 100)) - random.nextInt((int) (spread * 100))) + 0.5) / 100.0;
             double zwep = ((random.nextInt((int) (spread * 100)) - random.nextInt((int) (spread * 100))) + 0.5) / 100.0;
             double xd = Math.cos(Math.toRadians(dir)) * Math.cos(Math.toRadians(pitch)) + xwep;
             double yd = Math.sin(Math.toRadians(pitch));
             double zd = -Math.sin(Math.toRadians(dir)) * Math.cos(Math.toRadians(pitch)) + zwep;
             vector = new Vector(xd, yd, zd).multiply(strength());
         }
         snowball.setVelocity(vector);
 
         Runnable task = new Runnable() {
 
             @Override
             public void run() {
                 Location loc = snowball.getLocation();
                 for (int a = 1; a <= 2; a++) {
                     snowball.getWorld().playEffect(loc, Effect.SMOKE, 10);
                 }
             }
         };
         for (int i = 1; i <= 10; i++) {
             Bukkit.getScheduler().scheduleSyncDelayedTask(Paintball.INSTANCE, task, 2L * i);
         }
     }
 
     protected void onShoot(double spread) {
         if (owner == null)
             return;
         called = true;
         Player bukkitPlayer = owner.getBukkitPlayer();
         bukkitPlayer.playEffect(bukkitPlayer.getLocation(), Effect.CLICK1, 10);
 
         final Snowball snowball = bukkitPlayer.getWorld().spawn(bukkitPlayer.getEyeLocation(), Snowball.class);
         onFire(snowball, bukkitPlayer, spread);
     }
 }
