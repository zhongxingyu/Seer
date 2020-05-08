 package net.minecraft.src;
 
 import java.util.Timer;
 
 public class SkillSprint extends Skill{
	public Skill(int id, String name){
 		super(id, name);
 
 		this.setIconCoord(10, 3);
 	}
 
 	public ItemStack onItemRightClick(ItemStack item, World world, EntityPlayer player){
 		// Timer timeout = new Timer();
 
 		// timeout.schedule()
 
 		player.capabilities.setWalkSpeed(0.15F);
 
 		// Class c = ModLoader.getMinecraftInstance().thePlayer.capabilities.getClass();
 		// Field f = c.getDeclaredField("walkSpeed");
 		// f.setAccessible(true);
 		// f.set(ModLoader.getMinecraftInstance().thePlayer.capabilities, 1.0F);
 
 		return item;
 	}
 }
