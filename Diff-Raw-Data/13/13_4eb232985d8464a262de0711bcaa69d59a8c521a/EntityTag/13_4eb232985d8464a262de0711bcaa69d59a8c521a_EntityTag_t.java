 package boq.cctags.tag;
 
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.util.*;
 import net.minecraft.world.World;
 import net.minecraftforge.common.ForgeDirection;
 import boq.cctags.CCTags;
 import boq.cctags.EntityPacketHandler;
 import boq.cctags.cc.*;
 import boq.cctags.cc.ItemMisc.Subtype;
 import boq.utils.coord.Bounds;
 import boq.utils.coord.BoundsRotator;
 import boq.utils.misc.Dyes;
 
 import com.google.common.base.Preconditions;
 import com.google.common.io.ByteArrayDataInput;
 import com.google.common.io.ByteArrayDataOutput;
 
 import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class EntityTag extends Entity implements IEntityAdditionalSpawnData {
 
    public final static double AABB_THICKNESS = 0.05;
    public final static double AABB_MARGIN = 0.00;
    public final static int POOL_PERIOD = 20;
     private int tickCounter;
 
     private Bounds relativeBounds() {
         if (data.tagType == TagType.BIG)
             return new Bounds(-0.5 + AABB_MARGIN, -0.5 + AABB_MARGIN, 0.5 - AABB_THICKNESS,
                     0.5 - AABB_MARGIN, 0.5 - AABB_MARGIN, 0.5 + AABB_THICKNESS);
 
         return new Bounds(-0.25 - AABB_MARGIN, -0.25 - AABB_MARGIN, 0.5 - AABB_THICKNESS,
                 0.25 + AABB_MARGIN, 0.25 + AABB_MARGIN, 0.5 + AABB_THICKNESS);
 
     }
 
     public EntityTag(World world) {
         super(world);
         data = new TagData();
         noClip = true;
         setSize(0.2f, 0.2f);
     }
 
     public EntityTag(World world, TagData data) {
         super(world);
         this.data = data;
         noClip = true;
         setSize(0.2f, 0.2f);
     }
 
     public final TagData data;
 
     @Override
     protected void entityInit() {}
 
     @Override
     protected void readEntityFromNBT(NBTTagCompound tag) {
         NBTTagCompound tagData = tag.getCompoundTag("TagData");
         data.readFromNBT(tagData);
         updateAABB();
     }
 
     @Override
     protected void writeEntityToNBT(NBTTagCompound tag) {
         NBTTagCompound tagData = new NBTTagCompound();
         data.writeToNBT(tagData);
         tag.setTag("TagData", tagData);
     }
 
     @Override
     public boolean canBeCollidedWith()
     {
         return true;
     }
 
     private void checkMovement(double x, double y, double z) {
         if (!isDead && (x != 0 || y != 0 || z != 0)) {
             if (worldObj.isRemote)
                 spawnParticles();
             else
                 dropItemStack();
             setDead();
         }
     }
 
     @Override
     public void moveEntity(double dx, double dy, double dz) {
         checkMovement(dx, dy, dz);
     }
 
     @Override
     public void addVelocity(double vx, double vy, double vz) {
         checkMovement(vx, vy, vz);
     }
 
     private void updateAABB() {
         Bounds b = relativeBounds();
         BoundsRotator.flipDirection(b, data.side);
         b.setAABB(boundingBox, posX, posY, posZ);
     }
 
     @Override
     public void setPosition(double x, double y, double z) {
         posX = x;
         posY = y;
         posZ = z;
 
         // possible in Entity constructor or before additional data is received
         if (data != null && data.side != null)
             updateAABB();
     }
 
     @Override
     @SideOnly(Side.CLIENT)
     public void setPositionAndRotation2(double par1, double par3, double par5, float par7, float par8, int par9) {
         // super method checks for collisions...
         super.setPositionAndRotation(par1, par3, par5, par7, par8);
     }
 
     @Override
     public boolean func_85031_j(Entity entity) {
         if (entity instanceof EntityPlayer)
             return attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer)entity), 0);
 
         return false;
     }
 
     @Override
     public boolean attackEntityFrom(DamageSource source, float amount) {
         if (isDead)
             return false;
 
         if (worldObj.isRemote)
             spawnParticles();
         else {
             setDead();
             setBeenAttacked();
 
             Entity entity = source.getEntity();
             boolean isCreative = entity instanceof EntityPlayer && ((EntityPlayer)entity).capabilities.isCreativeMode;
 
             if (!isCreative)
                 dropItemStack();
         }
 
         return true;
     }
 
     private void spawnParticles() {
         double x = (boundingBox.maxX + boundingBox.minX) / 2.0;
         double y = (boundingBox.maxY + boundingBox.minY) / 2.0;
         double z = (boundingBox.maxZ + boundingBox.minZ) / 2.0;
         for (int i = 0; i < 5; i++)
             worldObj.spawnParticle("slime", x, y, z, 0, 0, 0);
     }
 
     @Override
     public void onUpdate() {
         if (tickCounter++ >= POOL_PERIOD) {
             checkWalls();
             tickCounter = 0;
         }
     }
 
     private void checkWalls() {
         if (isDead)
             return;
 
         int x = MathHelper.floor_double(posX);
         int y = MathHelper.floor_double(posY);
         int z = MathHelper.floor_double(posZ);
 
         if (worldObj.isAirBlock(x, y, z)) {
             setDead();
             if (worldObj.isRemote)
                 spawnParticles();
             else
                 dropItemStack();
         }
     }
 
     private ItemStack toItemStack() {
         Preconditions.checkState(!worldObj.isRemote, "Can't run this method on client side");
         return CCTags.instance.itemTag.createFromData(data);
     }
 
     @Override
     public ItemStack getPickedResult(MovingObjectPosition target) {
         // return toItemStack(); // called on client side, so we don't have all data...
         return null;
     }
 
     private void dropItemStack() {
         ItemStack stack = toItemStack();
         entityDropItem(stack, 0);
     }
 
     @Override
     public void writeSpawnData(ByteArrayDataOutput data) {
         this.data.writeToStream(data);
     }
 
     @Override
     public void readSpawnData(ByteArrayDataInput data) {
         this.data.readFromStream(data);
         updateAABB();
     }
 
     @Override
     public float getBrightness(float par1) {
         return 1.0f;
     }
 
     @Override
     public boolean isInRangeToRenderDist(double distanceSquared) {
         final double limit = 16 * 64;
         return distanceSquared < limit * limit;
     }
 
     @Override
     @SideOnly(Side.CLIENT)
     public int getBrightnessForRender(float par1) {
         final ForgeDirection side = data.side;
         if (side != null) {
             int x = MathHelper.floor_double(posX + side.offsetX);
             int z = MathHelper.floor_double(posZ + side.offsetZ);
 
             if (worldObj.blockExists(x, 0, z)) {
                 int y = MathHelper.floor_double(posY + side.offsetY);
                 return worldObj.getLightBrightnessForSkyBlocks(x, y, z, 0);
             }
         }
         return 0;
     }
 
     @Override
     public boolean func_130002_c(EntityPlayer player) {
         if (!worldObj.isRemote) {
             ItemStack held = player.getHeldItem();
 
             Integer dyeColor = Dyes.dyeStackToColor(held);
             if (dyeColor != null && dyeColor != data.color) {
                 data.color = dyeColor;
                 EntityPacketHandler.sendUpdateToAllTrackers(this);
 
                 if (!player.capabilities.isCreativeMode)
                     if (--held.stackSize <= 0)
                         player.destroyCurrentEquippedItem();
 
                 return true;
             }
 
             if (held != null && (held.getItem() instanceof ItemReader || ItemMisc.checkItem(held, Subtype.HANDHELD_OLD))) {
                 Object[] params = data.tagDescription(this);
                 player.sendChatToPlayer(ChatMessageComponent.func_111082_b("handheld.desc", params));
             } else {
                 data.rotation = data.rotation.rotateCCW();
                 EntityPacketHandler.sendUpdateToAllTrackers(this);
             }
         }
         return true;
     }
 
     @Override
     public boolean shouldRenderInPass(int pass) {
         return (data.tagType == TagType.GLASS ? 1 : 0) == pass;
     }
    
    @Override
    public float getCollisionBorderSize()
    {
        return 0;
    }
 
 }
