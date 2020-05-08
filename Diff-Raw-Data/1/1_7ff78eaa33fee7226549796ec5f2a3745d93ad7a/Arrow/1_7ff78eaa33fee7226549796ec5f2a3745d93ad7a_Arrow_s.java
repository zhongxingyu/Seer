 package TechGuard.x1337x.Archers.Arrow;
 
 import java.util.Random;
 
 import net.minecraft.server.EntityArrow;
 import net.minecraft.server.EntityHuman;
 import net.minecraft.server.EntityLiving;
 import net.minecraft.server.EntityTNTPrimed;
 import net.minecraft.server.Item;
 import net.minecraft.server.ItemStack;
 import net.minecraft.server.MathHelper;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.TreeType;
 import org.bukkit.World;
 import org.bukkit.craftbukkit.CraftWorld;
 import org.bukkit.craftbukkit.entity.CraftLivingEntity;
 import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.util.Vector;
 /**
  * @author TechGuard
  */
 public class Arrow extends EntityArrow{
 	public EnumBowMaterial material;
 	private int moving = 0;
 
 	public Arrow(World world, LivingEntity entityliving, EnumBowMaterial material) {
 		super(((CraftWorld)world).getHandle(), ((CraftLivingEntity)entityliving).getHandle());	
 		this.material = material;
 	}
 
         public Arrow(World w, LivingEntity el, EnumBowMaterial material, int thrice) {
 		super(((CraftWorld)w).getHandle());
 		this.material = material;
 		EntityLiving entityliving = ((CraftLivingEntity)el).getHandle();
 
 	    this.shooter = entityliving;
 	    b(0.5F, 0.5F);
 	    int int0 = 0;
 	    if(thrice==0) int0 = -10;
 	    if(thrice==1) int0 = 10;
 	    setPositionRotation(entityliving.locX, entityliving.locY + entityliving.s(), entityliving.locZ, entityliving.yaw+int0, entityliving.pitch);
 	    this.locX -= MathHelper.cos(this.yaw / 180.0F * 3.141593F) * 0.16F;
 	    this.locY -= 0.1000000014901161D;
 	    this.locZ -= MathHelper.sin(this.yaw / 180.0F * 3.141593F) * 0.16F;
 	    setPosition(this.locX, this.locY, this.locZ);
 	    this.height = 0.0F;
 	    this.motX = (-MathHelper.sin(this.yaw / 180.0F * 3.141593F) * MathHelper.cos(this.pitch / 180.0F * 3.141593F));
 	    this.motZ = (MathHelper.cos(this.yaw / 180.0F * 3.141593F) * MathHelper.cos(this.pitch / 180.0F * 3.141593F));
 	    this.motY = (-MathHelper.sin(this.pitch / 180.0F * 3.141593F));
 	    a(this.motX, this.motY, this.motZ, 1.5F, 1.0F);
 	}
 
 	public void p_() {
 		super.p_();
 
 	    if(lastX == locX && lastY== locY && lastZ == locZ && moving == 0){
 			moving = 1;
 		}
 		if(moving == 1){
 			destroy();
 			moving = 2;
 		}
 	}
 
 	public void destroy(){
 		if(material == EnumBowMaterial.ICE){
 			int radius = 3;
 			int radiusSq = (int)Math.pow(radius, 2.0D);
 			World world = getBukkitEntity().getWorld();
 
 		    for(int x = getBukkitEntity().getLocation().getBlockX()-radius; x <= getBukkitEntity().getLocation().getBlockX()+radius; x++){
 			  for(int z = getBukkitEntity().getLocation().getBlockZ()-radius; z <= getBukkitEntity().getLocation().getBlockZ()+radius; z++){
 				  if(new Vector(x, getBukkitEntity().getLocation().getBlockY(), z).distanceSquared(new Vector(
 				  getBukkitEntity().getLocation().getX(),getBukkitEntity().getLocation().getY(), getBukkitEntity().getLocation().getZ())) > radiusSq){
 					  continue;
 			      }
 				  if((new Random()).nextInt(4) > 0){
 					  continue;
 				  }
 				  for(int y = getBukkitEntity().getLocation().getBlockY()+radius; y >= getBukkitEntity().getLocation().getBlockY()-radius; y--){
 					  int id = world.getBlockTypeIdAt(x, y, z);
 					  if ((id == 6) || (id == 10) || (id == 11) || (id == 37) || (id == 38) || (id == 39) || (id == 40) || (id == 44) ||
 					  (id == 50) || (id == 51) || (id == 53) || (id == 55) || (id == 59) || ((id >= 63) && (id <= 72)) || (id == 75) ||
 					  (id == 76) || (id == 77) || (id == 78) || (id == 79) || (id == 81) || (id == 83) || (id == 85) || (id == 90)){
 						  break;
 					  }
 					  if ((id == 8) || (id == 9)) {
 						  world.getBlockAt(x, y, z).setTypeId(79);
 						  break;
 				      }
 					  if (id != 0) {
 						  if (y == 127){
 							  break;
 						  }
 						  world.getBlockAt(x, y+1, z).setTypeId(78);
 						  break;
 					  }
 				  }
 			  }
 			}
 		} else
 		if(material == EnumBowMaterial.FIRE){
 			World world = getBukkitEntity().getWorld();
 			world.getBlockAt((int)locX, (int)locY, (int)locZ).setType(Material.FIRE);
 		} else
 		if(material == EnumBowMaterial.TNT){
 			EntityTNTPrimed tnt = new EntityTNTPrimed(this.world, locX, locY, locZ);
 
 			tnt.a = 0;
 			world.addEntity(tnt);
 			tnt.f_();
 		} else
 		if(material == EnumBowMaterial.THUNDER){
 			World world = getBukkitEntity().getWorld();
 			world.strikeLightning(new Location(world, locX, locY, locZ));
 		}else
                 if(material == EnumBowMaterial.MONSTER){
 			World world = getBukkitEntity().getWorld();
 			CreatureType[] types = { CreatureType.CREEPER, CreatureType.SKELETON, CreatureType.SLIME, CreatureType.SPIDER, CreatureType.ZOMBIE };
 			world.spawnCreature(getBukkitEntity().getLocation(), types[(new Random()).nextInt(5)]);
 		}
                 else if(material == EnumBowMaterial.TREE){
                 	World world = getBukkitEntity().getWorld();
                 	Location loc = getBukkitEntity().getLocation();
                    world.generateTree(loc, TreeType.TREE);
                    
                 }
                 else if(material == EnumBowMaterial.ZEUS){
                 	Location loc = getBukkitEntity().getLocation();
                 	World worldf = loc.getWorld();
                 	worldf.strikeLightning(loc);
                 	loc.getBlock().setType(Material.FIRE);
                 	EntityTNTPrimed tnt = new EntityTNTPrimed(this.world, locX, locY, locZ);
 
         			tnt.a = 0;
         			world.addEntity(tnt);
         			tnt.f_();
                 }
                 else if(material == EnumBowMaterial.TP){
                 	if(shooter.getBukkitEntity() instanceof Player){
                 		Player p = ((Player)shooter.getBukkitEntity());
                 		p.teleportTo(new Location(p.getWorld(),locX, locY, locZ, shooter.yaw, shooter.pitch));
                 	}
                 }
         else if(material == EnumBowMaterial.THRICE){
 			die();
 		}
 
 }
 
 	public void b(EntityHuman entityhuman) {
 		if ((!this.world.isStatic) && (this.shooter == entityhuman) && moving==2 && (entityhuman.inventory.canHold(new ItemStack(Item.ARROW, 1)))) {
 			this.world.makeSound(this, "random.pop", 0.2F, ((this.random.nextFloat() - this.random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
 			entityhuman.receive(this, 1);
 			die();
 		}
 	}
 }
