 package mmm.esm.thirst;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.util.DamageSource;
 
 public class ThirstStats 
 {
 	
 	private int thirst_level = getMaxCapacity();
 	private int tickcounter = getTickCountBetweenDamage();
 	
 	
 	
 	public int getTickCountBetweenDamage()
 	{
 		return 80;		
 	}
 	
 	public int getMaxCapacity() 
 	{
 		// TODO Auto-generated method stub
		return 2000;
 	}
 	
 	public float thirstLevelPercentage()
 	{
 		float thirst = this.thirst_level;
 		float max = this.getMaxCapacity();
 		float result = thirst/max;
 		return result;
 	}
 
 	public void onUpdate(EntityPlayer entityPlayer) {
 		
 		modThirst(-1);
 		if(thirst_level == 0)
 		{
 			if(tickcounter == 0)
 			{
 				
 				entityPlayer.attackEntityFrom(DamageSource.starve, 1);
 				this.tickcounter = getTickCountBetweenDamage();
 			}
 			else
 				tickcounter--;				
 		}
 		
 	}
 	
 	public int modThirst(int mod)
 	{
 		int newthirst = thirst_level + mod < 0 ? 0 : thirst_level + mod;
 		newthirst = newthirst > this.getMaxCapacity() ? this.getMaxCapacity() : newthirst;
 		this.thirst_level = newthirst;
 		
 		return newthirst;
 	}
 
 	public void readNBT(NBTTagCompound par1nbtTagCompound) {
 		if (par1nbtTagCompound.hasKey("thirst_Level"))
         {
             this.thirst_level = par1nbtTagCompound.getInteger("thirst_level");
             this.tickcounter = par1nbtTagCompound.getInteger("thirst_tick_counter");
         }
     }
 
     /**
      * Writes food stats to an NBT object.
      */
 	public void writeNBT(NBTTagCompound par1nbtTagCompound) {
 		par1nbtTagCompound.setInteger("thirst_Level", this.thirst_level);
 		par1nbtTagCompound.setInteger("thirst_tick_counter", this.tickcounter);
 		
 	}
 
 	public int getThirstLevel() {
 		// TODO Auto-generated method stub
 		return this.thirst_level;
 	}
 	
 	@SideOnly(Side.CLIENT)
 	public void setThirstLevel(int thirstlevel) {
   		this.thirst_level = thirstlevel;
 	}
 	
 	
 
 }
