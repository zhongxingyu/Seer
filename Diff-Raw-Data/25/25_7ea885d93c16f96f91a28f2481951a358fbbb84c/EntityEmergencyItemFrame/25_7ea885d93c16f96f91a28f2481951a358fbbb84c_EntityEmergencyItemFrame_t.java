 package net.runfast.frangiblebuttons.entity;
 
 import java.io.IOException;
 
 import net.minecraft.block.Block;
 import net.minecraft.client.renderer.tileentity.RenderItemFrame;
 import net.minecraft.entity.DataWatcher;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.item.EntityItem;
 import net.minecraft.entity.item.EntityItemFrame;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTBase;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.util.DamageSource;
 import net.minecraft.util.Direction;
 import net.minecraft.world.World;
 import net.runfast.frangiblebuttons.EffectsManager;
 
 import com.google.common.io.ByteArrayDataInput;
 import com.google.common.io.ByteArrayDataOutput;
 
 import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
 
 /**
  * NB: Implementing {@link IEntityAdditionalSpawnData} causes Forge to
  * automatically call writeSpawnData and readSpawnData, which we use (in a fit
  * of laziness) to transfer the entire entity NBT data.
  */
 public class EntityEmergencyItemFrame extends EntityItemFrame implements
         IEntityAdditionalSpawnData {
 
     /** Object id in DataWatcher of the displayed item stack. */
     protected static final int OID_DISPLAYED_ITEMSTACK = 2;
     protected static final int OID_GLASS_INTACT = 10;
 
     public EntityEmergencyItemFrame(World world) {
         // Client calls this one.
         super(world);
     }
 
     public EntityEmergencyItemFrame(EntityItemFrame frame) {
         // Server calls this one.
         super(frame.worldObj, frame.xPosition, frame.yPosition,
                 frame.zPosition, frame.hangingDirection);
         if (frame.getDisplayedItem() != null) {
             this.setDisplayedItem(frame.getDisplayedItem().copy());
         }
         this.setItemRotation(frame.getRotation());
     }
 
     @Override
     protected void entityInit() {
         super.entityInit();
        // Glass initially intact.
        dataWatcher.addObject(OID_GLASS_INTACT, new Byte((byte) 1));
     }
 
     @Override
     public void writeSpawnData(ByteArrayDataOutput data) {
         NBTTagCompound nbt = new NBTTagCompound();
         writeToNBT(nbt);
         try {
             NBTBase.writeNamedTag(nbt, data);
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     @Override
     public void readSpawnData(ByteArrayDataInput data) {
         try {
             readFromNBT((NBTTagCompound) NBTBase.readNamedTag(data));
         } catch (IOException e) {
             e.printStackTrace();
         } catch (ClassCastException e) {
             e.printStackTrace();
         }
     }
 
     public void setGlassIntact(boolean b) {
         dataWatcher
                 .updateObject(OID_GLASS_INTACT, new Byte((byte) (b ? 1 : 0)));
     }
 
     public boolean isGlassIntact() {
         return dataWatcher.getWatchableObjectByte(OID_GLASS_INTACT) > 0;
     }
 
     @Override
     public boolean interact(EntityPlayer player) {
         // Do nothing if the glass is intact.
         // Otherwise replace the glass if we're holding a pane.
         // Otherwise pretend to be a normal item frame.
        ItemStack hand = player.getHeldItem();
         if (isGlassIntact()) {
             return false;
        } else if (hand != null && hand.itemID == Block.thinGlass.blockID
                && !player.isSneaking()) {
             EffectsManager.broadcastGlassReplaceEffect(player.worldObj,
                     xPosition, yPosition, zPosition);
             setGlassIntact(true);
            if (!player.capabilities.isCreativeMode) {
                hand.stackSize -= 1;
            }
            // NB: Returning true prevents, for some reason, a held item with
            // stackSize==0 from being removed.
            return false;
         } else {
             return super.interact(player);
         }
     }
 
     @Override
     public void writeEntityToNBT(NBTTagCompound nbt) {
         nbt.setBoolean("GlassIntact", isGlassIntact());
         super.writeEntityToNBT(nbt);
     }
 
     @Override
     public void readEntityFromNBT(NBTTagCompound nbt) {
         setGlassIntact(nbt.getBoolean("GlassIntact"));
         super.readEntityFromNBT(nbt);
     }
 
     @Override
     public boolean attackEntityFrom(DamageSource source, int par2) {
         Entity entity = source.getEntity();
         if (isGlassIntact() && entity instanceof EntityPlayer) {
             breakGlass((EntityPlayer) entity);
             // Not sure what exactly returning true here means, but it
             // must be important because false fucks things up.
             return true;
         }
         return super.attackEntityFrom(source, par2);
     }
 
     public void breakGlass(EntityPlayer player) {
         if (isGlassIntact()) {
             setGlassIntact(false);
             EffectsManager.broadcastGlassBreakEffect(player, xPosition
                     + Direction.offsetX[hangingDirection], yPosition, zPosition
                     + Direction.offsetZ[hangingDirection]);
             if (!player.worldObj.isRemote) {
                 dropItemStackOnly(player);
             }
         }
     }
 
     @Override
     public boolean shouldRenderInPass(int pass) {
         // Renders in both passes.
         return true;
     }
 
     /**
      * Drop the item currently on this item frame, but not the item frame
      * itself. Contrast with {@link EntityItemFrame#dropItemStack()}, which
      * drops both.
      * 
      * @param player
      *            Entity which initiated this action.
      */
     public void dropItemStackOnly(Entity player) {
         ItemStack itemstack = this.getDisplayedItem();
 
         if (itemstack != null) {
             this.quickDropItem(player, itemstack.copy(), 0.5);
             this.setDisplayedItem(null);
         }
     }
 
     /**
      * Overridden to return null when the stack size is zero. This causes
      * {@link RenderItemFrame} to behave correctly in that case.
      */
     @Override
     public ItemStack getDisplayedItem() {
         ItemStack s = super.getDisplayedItem();
         if (s != null && s.stackSize == 0) {
             return null;
         }
         return s;
     }
 
     /**
      * Overridden to deal with nulls correctly. Since {@link DataWatcher} will
      * not countenance updating something to null, we set the stack size to zero
      * instead.
      */
     @Override
     public void setDisplayedItem(ItemStack stack) {
         if (stack == null) {
             ItemStack s = getDisplayedItem();
             if (s != null) {
                 // NB: Since the DataWatcher uses .equals to short-circuit
                 // "unnecessary" updates, and doesn't give us a copy, we need to
                 // create a copy here. Otherwise the update gets ignored.
                 s = s.copy();
                 s.stackSize = 0;
                 this.getDataWatcher().updateObject(OID_DISPLAYED_ITEMSTACK, s);
                 this.setItemRotation(0);
             }
         } else {
             super.setDisplayedItem(stack);
         }
     }
 
     /**
      * Drops an item in such a way that the given entity has a good chance of
      * instantly picking it up.
      * 
      * @param velocity
      *            How much the item loves the player. A value of 0.5 seems to
      *            work well.
      */
     public EntityItem quickDropItem(Entity player, ItemStack stack,
             double velocity) {
         // Move the item out a little bit so it doesn't collide with the frame
         // and bounce off in a random direction.
         double out = 0.25;
         EntityItem item = new EntityItem(this.worldObj, this.posX + out
                 * Direction.offsetX[hangingDirection], this.posY, this.posZ
                 + out * Direction.offsetZ[hangingDirection], stack);
         // Pick up immediately.
         item.delayBeforeCanPickup = 0;
         // Calculate & normalize direction to the player
         double dx = player.posX - item.posX;
         double dy = player.posY + player.height - item.posY;
         double dz = player.posZ - item.posZ;
         double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
         dx /= d;
         dy /= d;
         dz /= d;
 
         item.setVelocity(velocity * dx, velocity * dy, velocity * dz);
         this.worldObj.spawnEntityInWorld(item);
         return item;
     }
 }
