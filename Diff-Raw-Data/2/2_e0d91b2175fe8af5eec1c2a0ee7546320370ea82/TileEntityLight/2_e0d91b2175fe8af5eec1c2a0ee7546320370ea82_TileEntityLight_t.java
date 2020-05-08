 
 package net.specialattack.discotek.tileentity;
 
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.List;
 
 import me.heldplayer.util.HeldCore.sync.ISyncable;
 import me.heldplayer.util.HeldCore.sync.ISyncableObjectOwner;
 import me.heldplayer.util.HeldCore.sync.SBoolean;
 import me.heldplayer.util.HeldCore.sync.SFloat;
 import me.heldplayer.util.HeldCore.sync.SInteger;
 import me.heldplayer.util.HeldCore.sync.packet.Packet4InitiateClientTracking;
 import me.heldplayer.util.HeldCore.sync.packet.PacketHandler;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.network.packet.Packet;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.AxisAlignedBB;
 import net.specialattack.discotek.ModDiscoTek;
 import net.specialattack.discotek.client.ClientProxy;
 
 import com.google.common.io.ByteArrayDataInput;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class TileEntityLight extends TileEntity implements ISyncableObjectOwner {
 
     //private int color = 0xFFFFFF;
     private SInteger color;
     private int prevColor = 0xFFFFFF;
     //private boolean hasLens = true; // Relax, don't do it
     private SBoolean hasLens;
     //private float pitch = 0.0F;
     private SFloat pitch;
     private float prevPitch = 0.0F;
     //private float yaw = 0.0F;
     private SFloat yaw;
     private float prevYaw = 0.0F;
     //private float brightness = 1.0F;
     private SFloat brightness;
     private float prevBrightness = 1.0F;
     //private float focus = 1.0F;
     private SFloat focus;
     private float prevFocus = 1.0F;
     private List<ISyncable> syncables;
 
     //Channels 1 - 512 (0 - 511)
     public int[] channels;
     private int[] cachedLevels = new int[256];
 
    private SInteger direction;
 
     public TileEntityLight() {
         this.color = new SInteger(this, 0xFFFFFF);
         this.hasLens = new SBoolean(this, true);
         this.pitch = new SFloat(this, 0.0F);
         this.yaw = new SFloat(this, 0.0F);
         this.brightness = new SFloat(this, 1.0F);
         this.focus = new SFloat(this, 1.0F);
         this.direction = new SInteger(this, 0);
         this.syncables = Arrays.asList((ISyncable) this.color, this.hasLens, this.pitch, this.yaw, this.brightness, this.focus, this.direction);
     }
 
     public void setDirection(int side) {
         this.direction.setValue(side);
     }
 
     @Override
     public void validate() {
         super.validate();
 
         if (this.worldObj != null && this.worldObj.isRemote) {
             ClientProxy.addTile(this);
         }
     }
 
     @Override
     public void invalidate() {
         super.invalidate();
 
         if (this.worldObj != null && this.worldObj.isRemote) {
             ClientProxy.addTile(this);
         }
     }
 
     public int getDirection() {
         return this.direction.getValue();
     }
 
     public int getColor(float partialTicks) {
         int red = (int) (((this.prevColor >> 16) & 0xFF) + (((this.color.getValue() >> 16) & 0xFF) - ((this.prevColor >> 16) & 0xFF)) * partialTicks);
         int green = (int) (((this.prevColor >> 8) & 0xFF) + (((this.color.getValue() >> 8) & 0xFF) - ((this.prevColor >> 8) & 0xFF)) * partialTicks);
         int blue = (int) ((this.prevColor & 0xFF) + ((this.color.getValue() & 0xFF) - (this.prevColor & 0xFF)) * partialTicks);
         return red << 16 | green << 8 | blue;
     }
 
     public void setColor(int color) {
         this.prevColor = color;
         this.color.setValue(color);
     }
 
     public boolean hasLens() {
         return this.hasLens.getValue();
     }
 
     public void setHasLens(boolean hasLens) {
         this.hasLens.setValue(hasLens);
     }
 
     public float getPitch(float partialTicks) {
         return this.prevPitch + (this.pitch.getValue() - this.prevPitch) * partialTicks;
     }
 
     public void setPitch(float pitch) {
         this.prevPitch = pitch;
         this.pitch.setValue(pitch);
     }
 
     public float getYaw(float partialTicks) {
         return this.prevYaw + (this.yaw.getValue() - this.prevYaw) * partialTicks;
     }
 
     public void setYaw(float yaw) {
         this.prevYaw = yaw;
         this.yaw.setValue(yaw);
     }
 
     public float getBrightness(float partialTicks) {
         return this.prevBrightness + (this.brightness.getValue() - this.prevBrightness) * partialTicks;
     }
 
     public void setBrightness(float brightness) {
         this.prevBrightness = brightness;
         this.brightness.setValue(brightness);
     }
 
     public float getFocus(float partialTicks) {
         return this.prevFocus + (this.focus.getValue() - this.prevFocus) * partialTicks;
     }
 
     public void setFocus(float focus) {
         this.prevFocus = focus;
         this.focus.setValue(focus);
     }
 
     public float getValue(int index) {
         switch (index) {
         case 2:
             return this.brightness.getValue();
         case 3:
             return this.pitch.getValue();
         case 4:
             return this.yaw.getValue();
         case 5:
             return this.focus.getValue();
         case 6: // Red
             return (float) ((this.color.getValue() & 0xFF0000) >> 16) / 255.0F;
         case 7: // Green
             return (float) ((this.color.getValue() & 0x00FF00) >> 8) / 255.0F;
         case 8: // Blue
             return (float) (this.color.getValue() & 0x0000FF) / 255.0F;
         default:
             return 0.0F;
         }
     }
 
     public void setValue(int index, float value) {
         switch (index) {
         case 2:
             this.brightness.setValue(value);
         break;
         case 3:
             this.pitch.setValue(value);
         break;
         case 4:
             this.yaw.setValue(value);
         break;
         case 5:
             this.focus.setValue(value);
         break;
         case 6: // Red
             this.color.setValue((this.color.getValue() & 0x00FFFF) | (((int) (value * 255.0F) << 16) & 0xFF0000));
         break;
         case 7: // Green
             this.color.setValue((this.color.getValue() & 0xFF00FF) | (((int) (value * 255.0F) << 8) & 0x00FF00));
         break;
         case 8: // Blue
             this.color.setValue((this.color.getValue() & 0xFFFF00) | ((int) (value * 255.0F) & 0x0000FF));
         break;
         }
     }
 
     public void setValue(int index, int value) {
         switch (index) {
         case 2:
             this.brightness.setValue((float) value / 255.0F);
         break;
         case 3:
             this.pitch.setValue((float) value * 1.6F / 255.0F - 0.8F);
         break;
         case 4:
             this.yaw.setValue((float) value * 6.28318530718F / 255.0F); // 2 Pi Radians
         break;
         case 5:
             this.focus.setValue((float) value * 20F / 255.0F);
         break;
         case 6:
             this.color.setValue((this.color.getValue() & 0x00FFFF) | ((value << 16) & 0xFF0000));
         break;
         case 7:
             this.color.setValue((this.color.getValue() & 0xFF00FF) | ((value << 8) & 0x00FF00));
         break;
         case 8:
             this.color.setValue((this.color.getValue() & 0xFFFF00) | (value & 0x0000FF));
         break;
         }
     }
 
     @Override
     public void readFromNBT(NBTTagCompound compound) {
         super.readFromNBT(compound);
         this.color.setValue(compound.getInteger("color"));
         this.hasLens.setValue(compound.getBoolean("hasLens"));
         this.pitch.setValue(compound.getFloat("pitch"));
         this.prevPitch = this.pitch.getValue();
         this.yaw.setValue(compound.getFloat("yaw"));
         this.prevYaw = this.yaw.getValue();
         this.brightness.setValue(compound.getFloat("brightness"));
         this.prevBrightness = this.brightness.getValue();
         this.focus.setValue(compound.getFloat("focus"));
         this.prevFocus = this.focus.getValue();
         this.channels = compound.getIntArray("channels");
         this.direction.setValue(compound.getInteger("direction"));
     }
 
     @Override
     public void writeToNBT(NBTTagCompound compound) {
         super.writeToNBT(compound);
         compound.setInteger("color", this.color.getValue());
         compound.setBoolean("hasLens", this.hasLens.getValue());
         compound.setFloat("pitch", this.pitch.getValue());
         compound.setFloat("yaw", this.yaw.getValue());
         compound.setFloat("brightness", this.brightness.getValue());
         compound.setFloat("focus", this.focus.getValue());
         compound.setIntArray("channels", this.channels);
         compound.setInteger("direction", this.direction.getValue());
     }
 
     @Override
     public Packet getDescriptionPacket() {
         return PacketHandler.instance.createPacket(new Packet4InitiateClientTracking(xCoord, yCoord, zCoord));
     }
 
     @Override
     public void updateEntity() {
         this.prevPitch = this.pitch.getValue();
         this.prevYaw = this.yaw.getValue();
         this.prevBrightness = this.brightness.getValue();
         this.prevFocus = this.focus.getValue();
         this.prevColor = this.color.getValue();
 
         if (this.pitch.getValue() > 0.8F) {
             this.prevPitch = 0.8F;
             this.pitch.setValue(0.8F);
         }
         else if (this.pitch.getValue() < -0.8F) {
             this.prevPitch = -0.8F;
             this.pitch.setValue(-0.8F);
         }
 
         if (this.brightness.getValue() > 1.0F) {
             this.prevBrightness = 1.0F;
             this.brightness.setValue(1.0F);
         }
         else if (this.brightness.getValue() < 0.0F) {
             this.prevBrightness = 0.0F;
             this.brightness.setValue(0.0F);
         }
 
         if (this.focus.getValue() > 20.0F) {
             this.prevFocus = 20.0F;
             this.focus.setValue(20.0F);
         }
         else if (this.focus.getValue() < 0.0F) {
             this.prevFocus = 0.0F;
             this.focus.setValue(0.0F);
         }
 
         if (!this.worldObj.isRemote) {
             int size = 0;
             switch (this.getBlockMetadata() & 0xFF) {
             case 0:
                 size = 1;
             break;
             case 1:
                 size = 4;
             break;
             case 2:
                 size = 7;
             break;
             case 3:
                 size = 1;
             break;
             case 4:
                 size = 7;
             break;
             }
             if (this.channels == null || this.channels.length != size) {
                 this.channels = new int[size];
             }
         }
     }
 
     @Override
     @SideOnly(Side.CLIENT)
     public AxisAlignedBB getRenderBoundingBox() {
         if (this.getBlockMetadata() == 3) {
             return super.getRenderBoundingBox();
         }
         return super.getRenderBoundingBox().expand(64.0D, 64.0D, 64.0D);
     }
 
     public void sendUniverseData(int[] levels) {
         for (int i = 0; this.channels != null && i < this.channels.length; i++) {
             if (this.channels[i] == 0) {
                 continue;
             }
             if (levels[this.channels[i]] != this.cachedLevels[this.channels[i]]) {
                 this.setValue(i + 2, levels[this.channels[i]]);
             }
         }
         if (this.getBlockMetadata() == 3) {
             this.worldObj.notifyBlocksOfNeighborChange(this.xCoord, this.yCoord, this.zCoord, ModDiscoTek.blockLightId.getValue());
         }
         System.arraycopy(levels, 0, this.cachedLevels, 0, levels.length);
     }
 
     // ISyncableObjectOwner
 
     @Override
     public boolean isNotValid() {
         return super.isInvalid();
     }
 
     @Override
     public List<ISyncable> getSyncables() {
         return this.syncables;
     }
 
     @Override
     public void readSetup(ByteArrayDataInput in) throws IOException {
         for (int i = 0; i < this.syncables.size(); i++) {
             ISyncable syncable = this.syncables.get(i);
             syncable.setId(in.readInt());
             syncable.read(in);
         }
     }
 
     @Override
     public void writeSetup(DataOutputStream out) throws IOException {
         for (int i = 0; i < this.syncables.size(); i++) {
             ISyncable syncable = this.syncables.get(i);
             out.writeInt(syncable.getId());
             syncable.write(out);
         }
     }
 
     @Override
     public int getPosX() {
         return this.xCoord;
     }
 
     @Override
     public int getPosY() {
         return this.yCoord;
     }
 
     @Override
     public int getPosZ() {
         return this.zCoord;
     }
 
     @Override
     public void onDataChanged(ISyncable syncable) {}
 
 }
