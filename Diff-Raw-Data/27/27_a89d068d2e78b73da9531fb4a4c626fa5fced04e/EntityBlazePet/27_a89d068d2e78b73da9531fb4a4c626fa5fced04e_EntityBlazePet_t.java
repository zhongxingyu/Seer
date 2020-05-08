 package se.DMarby.Pets;
 
 import net.minecraft.server.v1_7_R1.EntityBlaze;
 import net.minecraft.server.v1_7_R1.EntityHuman;
 import net.minecraft.server.v1_7_R1.World;
 import org.bukkit.Bukkit;
 import org.bukkit.craftbukkit.v1_7_R1.CraftServer;
 import org.bukkit.craftbukkit.v1_7_R1.entity.CraftBlaze;
 import org.bukkit.craftbukkit.v1_7_R1.entity.CraftEntity;
 import org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer;
 import org.bukkit.entity.Blaze;
 import org.bukkit.entity.Player;
 
 public class EntityBlazePet extends EntityBlaze { // old AI
     private final Player owner;
 
     public EntityBlazePet(World world, Player owner) {
         super(world);
         this.owner = owner;
     }
 
     public EntityBlazePet(World world) {
         this(world, null);
     }
 
     private int distToOwner() {
         EntityHuman handle = ((CraftPlayer) owner).getHandle();
         return (int) (Math.pow(locX - handle.locX, 2) + Math.pow(locY - handle.locY, 2) + Math.pow(locZ
                 - handle.locZ, 2));
     }
 
     @Override
     protected void bq() {
         if (owner == null) {
             super.bq();
             return;
         }
        this.getNavigation().a(owner.getLocation().getX(), owner.getLocation().getY(), owner.getLocation().getZ(), 10F);
         this.getNavigation().a(false);
         getEntitySenses().a();
         getNavigation().f();
         getControllerMove().c();
         getControllerLook().a();
         getControllerJump().b();
 
         if (distToOwner() > Util.MAX_DISTANCE)
             this.getBukkitEntity().teleport(owner);
     }
 
     @Override
     public CraftEntity getBukkitEntity() {
         if (owner != null && bukkitEntity == null)
             bukkitEntity = new BukkitBlazePet(this);
         return super.getBukkitEntity();
     }
 
     public static class BukkitBlazePet extends CraftBlaze implements PetEntity {
         private final Player owner;
 
         public BukkitBlazePet(EntityBlazePet entity) {
             super((CraftServer) Bukkit.getServer(), entity);
             this.owner = entity.owner;
         }
 
         @Override
         public Blaze getBukkitEntity() {
             return this;
         }
 
         @Override
         public Player getOwner() {
             return owner;
         }
 
         @Override
         public void upgrade() {
         }
 
         @Override
         public void setLevel(int level) {
             // setSize(level);
         }
     }
 }
