 package se.DMarby.Pets;
 
 import net.minecraft.server.v1_7_R1.EntityHorse;
 import net.minecraft.server.v1_7_R1.EntityHuman;
 import net.minecraft.server.v1_7_R1.World;
 import org.bukkit.Bukkit;
 import org.bukkit.craftbukkit.v1_7_R1.CraftServer;
 import org.bukkit.craftbukkit.v1_7_R1.entity.CraftEntity;
 import org.bukkit.craftbukkit.v1_7_R1.entity.CraftHorse;
 import org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer;
 import org.bukkit.entity.Horse;
 import org.bukkit.entity.Player;
 
 import java.util.Random;
 
 public class EntityHorsePet extends EntityHorse { // new AI
     private final Player owner;
     private Boolean ridable;
 
     public EntityHorsePet(World world, Player owner) {
         super(world);
         this.owner = owner;
         this.ridable = false;
         if (owner != null)
             Util.clearGoals(this.goalSelector, this.targetSelector);
     }
 
     public EntityHorsePet(World world, Player owner, Boolean ridable) {
         super(world);
         this.owner = owner;
         this.ridable = ridable;
         if (owner != null)
             Util.clearGoals(this.goalSelector, this.targetSelector);
     }
 
      public EntityHorsePet(World world) {
         this(world, null);
     }
 
     private int distToOwner() {
         EntityHuman handle = ((CraftPlayer) owner).getHandle();
         return (int) (Math.pow(locX - handle.locX, 2) + Math.pow(locY - handle.locY, 2) + Math.pow(locZ
                 - handle.locZ, 2));
     }
 
     @Override
     protected void bn() {
         super.bn();
         if (owner == null)
             return;
        this.getNavigation().a(owner.getLocation().getX(), owner.getLocation().getY(), owner.getLocation().getZ(), 1.1F);
         this.getNavigation().a(false);
         if (distToOwner() > Util.MAX_DISTANCE)
             this.getBukkitEntity().teleport(owner);
     }
 
     public Boolean isRidable() {
         return ridable;
     }
 
     public void giveShit() {
         this.datawatcher.watch(16, Integer.valueOf(4));
         this.datawatcher.watch(22, Integer.valueOf(new Random().nextInt(4 - 1) + 1));
     }
 
     @Override
     public CraftEntity getBukkitEntity() {
         if (owner != null && bukkitEntity == null)
             bukkitEntity = new BukkitHorsePet(this);
         return super.getBukkitEntity();
     }
 
     public static class BukkitHorsePet extends CraftHorse implements PetEntity {
         private final Player owner;
 
         public BukkitHorsePet(EntityHorsePet entity) {
             super((CraftServer) Bukkit.getServer(), (EntityHorse) entity);
             this.owner = entity.owner;
         }
 
         @Override
         public Horse getBukkitEntity() {
             return this;
         }
 
         @Override
         public Player getOwner() {
             return owner;
         }
 
         @Override
         public void upgrade() {
             // upgrade logic here
             /*
                         int size = getSize() + 1;
                         if (Util.MAX_LEVEL != -1)
                             size = Math.min(Util.MAX_LEVEL, size);
                         setSize(size);
                         Location petLoc = getLocation();
                         getWorld().playSound(petLoc, Sound.LEVEL_UP, size, 1);
                         for (int i = 0; i < size; i++)
                             getWorld().playEffect(petLoc, Effect.SMOKE, (size + i) / 8);
             */
         }
 
         @Override
         public void setLevel(int level) {
             // setSize(level);
         }
     }
 }
