 /*******************************************************************************************************************
  * Authors:   SanAndreasP
  * Copyright: SanAndreasP
  * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
  *                http://creativecommons.org/licenses/by-nc-sa/4.0/
  *******************************************************************************************************************/
 package de.sanandrew.mods.particledeco.tileentity;
 
 import de.sanandrew.core.manpack.mod.client.particle.EntityParticle;
 import de.sanandrew.core.manpack.mod.client.particle.SAPEffectRenderer;
 import de.sanandrew.core.manpack.util.SAPUtils;
 import de.sanandrew.mods.particledeco.client.particle.EntityDustFX;
 import de.sanandrew.mods.particledeco.util.ParticleBoxData;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.network.NetworkManager;
 import net.minecraft.network.Packet;
 import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraftforge.common.util.ForgeDirection;
 import org.apache.commons.lang3.mutable.MutableFloat;
 
 public class TileEntityParticleBox
     extends TileEntity
 {
     private int ticksExisted;
     public ParticleBoxData particleData = new ParticleBoxData();
     public int prevColor = this.particleData.particleColor;
     public float[] particleColorSplit = SAPUtils.getRgbaFromColorInt(this.particleData.particleColor).getColorFloatArray();
 
     public TileEntityParticleBox() {
     }
 
     public boolean canUpdate() {
         return true;
     }
 
     @Override
     public void updateEntity() {
         this.ticksExisted++;
 
         if( this.worldObj.isRemote && this.ticksExisted % 2 == 0 ) {
             if( this.prevColor != this.particleData.particleColor ) {
                 this.particleColorSplit = SAPUtils.getRgbaFromColorInt(this.particleData.particleColor).getColorFloatArray();
                 this.prevColor = this.particleData.particleColor;
             }
 
             MutableFloat[] motions = new MutableFloat[] {new MutableFloat(0.0F), new MutableFloat(0.0F), new MutableFloat(0.0F)};
             this.changeDirection(motions[0], motions[1], motions[2]);
 
             EntityParticle particle = new EntityDustFX(this.worldObj, this.xCoord + 0.5F, this.yCoord + 0.5F, this.zCoord + 0.5F, this.particleData.particleHeight,
                                                        this.particleData.particleSpeed, motions[0].getValue(), motions[1].getValue(), motions[2].getValue());
             particle.setParticleColorRNG(this.particleColorSplit[0], this.particleColorSplit[1], this.particleColorSplit[2]);
             particle.setBrightness(0xF0);
             SAPEffectRenderer.INSTANCE.addEffect(particle);
         }
     }
 
     @Override
     public void writeToNBT(NBTTagCompound nbt) {
         super.writeToNBT(nbt);
 
         this.particleData.writeDataToNBT(nbt);
     }
 
     @Override
     public void readFromNBT(NBTTagCompound nbt) {
         super.readFromNBT(nbt);
 
         this.particleData = ParticleBoxData.getDataFromNbt(nbt);
     }
 
     @Override
     public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
         this.particleData = ParticleBoxData.getDataFromNbt(pkt.func_148857_g());
     }
 
     @Override
     public Packet getDescriptionPacket() {
         NBTTagCompound nbt = new NBTTagCompound();
         this.particleData.writeDataToNBT(nbt);
 
         return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 0, nbt);
     }
 
     private void changeDirection(MutableFloat motionX, MutableFloat motionY, MutableFloat motionZ) {
         ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata());
         switch( this.particleData.particleSpread ) {
             case SPREADED_FOUR_DIAG:
                 int ticksMod = this.ticksExisted % 5;
                 if( ticksMod != 0 ) {
                    float facingMulti = 0.025F - (ticksMod % 2) * 0.05F;
                     motionX.setValue(0.05F * dir.offsetX + facingMulti * (ticksMod < 3 ? dir.offsetY : dir.offsetZ));
                     motionY.setValue(0.05F * dir.offsetY + facingMulti * (ticksMod < 3 ? dir.offsetZ : dir.offsetX));
                     motionZ.setValue(0.05F * dir.offsetZ + facingMulti * (ticksMod < 3 ? dir.offsetX : dir.offsetY));
                 } else {
                     motionX.setValue(0.075F * dir.offsetX);
                     motionY.setValue(0.075F * dir.offsetY);
                     motionZ.setValue(0.075F * dir.offsetZ);
                 }
 
                 break;
             case STRAIGHT_UP:
                 motionX.setValue(0.075F * dir.offsetX);
                 motionY.setValue(0.075F * dir.offsetY);
                 motionZ.setValue(0.075F * dir.offsetZ);
                 break;
         }
     }
 }
