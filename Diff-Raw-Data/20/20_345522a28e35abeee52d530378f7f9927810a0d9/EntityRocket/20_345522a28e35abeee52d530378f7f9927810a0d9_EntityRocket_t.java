 package ru.secondfry.TestMod.entities;
 
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
 import net.minecraft.entity.Entity;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.world.World;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Rustam Second_Fry Gubaydullin
  * Date: 21.09.13 7:41
  */
public class EntityRocket extends Entity implements IEntityAdditionalSpawnData {
 
 	private int type = -1;
 	private int start = -1;
 
 	public EntityRocket(World world) {
 		super(world);
		motionY = 0.1;
 	}
 
 	@Override
 	public void onUpdate() {
 		super.onUpdate();
 
 		if (!worldObj.isRemote) {
 			if (posY - start >= 10) {
 				setDead();
 			}
 		}
 
 		setPosition(posX + motionX, posY + motionY, posZ + motionZ);
 	}
 
 	public void setType(int type) {
 		this.type = type;
 	}
 
 	public int getType() {
 		return type;
 	}
 
 	public void setStart(int start) {
 		this.start = start;
 	}
 
 	@Override
 	protected void entityInit() {
 	}
 
 	@Override
 	protected void readEntityFromNBT(NBTTagCompound compound) {
 		type = compound.getByte("Type");
 	}
 
 	@Override
 	protected void writeEntityToNBT(NBTTagCompound compound) {
 		compound.setByte("Type", (byte) type);
 	}

	@Override
	public void writeSpawnData(ByteArrayDataOutput data) {
		data.writeInt(type);
	}

	@Override
	public void readSpawnData(ByteArrayDataInput data) {
		type = data.readInt();
	}
 }
