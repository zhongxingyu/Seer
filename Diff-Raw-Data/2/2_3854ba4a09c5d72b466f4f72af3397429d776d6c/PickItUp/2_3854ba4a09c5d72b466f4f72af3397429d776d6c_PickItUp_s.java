 package pickitup;
 import pickitup.api.*;
 import pickitup.vanilla.*;
 
 import java.lang.reflect.Method;
 import java.util.Vector;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import cpw.mods.fml.common.event.FMLInterModComms.IMCEvent;
 import cpw.mods.fml.common.event.FMLInterModComms.IMCMessage;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.Loader;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockHalfSlab;
 import net.minecraft.block.BlockStairs;
 import net.minecraft.client.Minecraft;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.EntityPlayerMP;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.event.entity.living.LivingHurtEvent;
 import net.minecraftforge.event.entity.living.LivingDeathEvent;
 import net.minecraftforge.event.entity.player.PlayerEvent;
 import net.minecraftforge.event.entity.player.PlayerInteractEvent;
 import net.minecraftforge.event.EventPriority;
 import net.minecraftforge.event.ForgeSubscribe;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.ChunkCoordinates;
 import net.minecraft.util.EntityDamageSource;
 import net.minecraft.util.EnumMovingObjectType;
 import net.minecraft.util.MathHelper;
 import net.minecraft.util.MovingObjectPosition;
 import net.minecraft.util.Vec3;
 import net.minecraft.world.World;
 
 import static net.minecraftforge.event.Event.Result.*;
 
 @Mod(modid = "PickItUp",
      name = "PickItUp",
      version = "%conf:VERSION%")
 @NetworkMod(serverSideRequired = false,
             clientSideRequired = true,
             channels = { "pickitup" },
             packetHandler = pickitup.PacketHandler.class)
             //connectionHandler = pickitup.ConnectionHandler.class)
 public class PickItUp {
     public static final int DEFAULT_DW_INDEX = 27;
     public static final String DW_INDEX_DOC = "The index on EntityPlayer's DataWatcher used to store whether they are holding a block.";
     public static int DW_INDEX = DEFAULT_DW_INDEX;
 
     public static final String DEFAULT_WHITELIST = "3-6,12,13,15,17,18,-26,22-28,29:0-5,31,32,33:0-5,35,37-46,48,50,53,54,-64,57-70,72,-79,76-88,91-93,96,98,101-109,112-118,121-126,128,130,133-136,-144,-153,139-159,170-175";
     public static final String WHITELIST_DOC = "The comma-separated list of blocks allowed to be picked up.\nThe format is [-]id[-max_id][:meta[-max_meta]], where [] denotes an optional section.\nEntries starting with - are explicit blacklists, which can be used to override later whitelist entries.  Similarly, earlier whitelist entries will override later blacklist entries.\nThe default whitelist is tuned to be fairly permissive, but not allow you to bypass major milestones like an iron pick or silk touch.\nIt also disables some problematic blocks, like lilly pads and piston heads.";
     public static Vector<BlockRange> whitelist = new Vector<BlockRange>();
 
     public static Vector<ISimplePickup> pickupHandlers = new Vector<ISimplePickup>();
 
     public static final String HELD_TAG = "PickItUp_held";
 
     public static Configuration config = null;
 
     public static Method getBackpack = null;
 
     @SidedProxy(clientSide="pickitup.ClientProxy",
                serverSide="pickitup.ServerProxy")
     public static CommonProxy proxy;
 
     @Mod.PreInit
     public void preInit(FMLPreInitializationEvent event) {
         // Load config file.
         config = new Configuration(event.getSuggestedConfigurationFile());
 
         // Add the vanilla handlers.
         addHandler(new SignHandler());
     }
 
     @Mod.Init
     @SuppressWarnings("unchecked")
     public void init(FMLInitializationEvent event) {
         try {
             config.load();
         } catch (RuntimeException e) {} // Just regenerate the config if it's
                                         // broken.
 
 
         // Fetch the DataWatcher ID for the held block.
         DW_INDEX = config.get(config.CATEGORY_GENERAL,
                               "holdingBlockDataWatcherIndex",
                               DEFAULT_DW_INDEX,
                               DW_INDEX_DOC).getInt(DEFAULT_DW_INDEX);
 
         // Fetch the whitelist.
         String whitelist_string = config.get(config.CATEGORY_GENERAL,
                                              "blockWhitelist",
                                              DEFAULT_WHITELIST,
                                              WHITELIST_DOC).getString();
 
         try {
             config.save();
         } catch (RuntimeException e) {
             System.out.println("PickItUp: Unable to save config!");
         }
 
         for (String whitelist_entry : whitelist_string.split(",")) {
             BlockRange range = new BlockRange(whitelist_entry);
             if (range.valid) {
                 whitelist.add(range);
             }
         }
 
         // Register our listeners.
         MinecraftForge.EVENT_BUS.register(new EventListener());
         GameRegistry.registerPlayerTracker(new PlayerTracker());
 
         // Interface with BetterStorage.
         if (Loader.isModLoaded("betterstorage")) {
             try {
                 Class itemBackpack = Class.forName("net.mcft.copy.betterstorage.item.ItemBackpack");
                 getBackpack = itemBackpack.getMethod("getBackpack", EntityPlayer.class);
             } catch (Exception e) {
                 getBackpack = null;
             }
         }
     }
 
     // This is called when we get an IMC message.  We use it to allow other
     // mods to add hooks without compiling against the PickItUp core.
     // (They'll still need ISimplePickup and possibly ICanBePickedUp, of
     //  course.)
     @Mod.IMCCallback
     public void gotIMC(IMCEvent event) {
         for (IMCMessage message : event.getMessages()) {
             try {
                 if ("addHandler".equals(message.key)) {
                     addHandler(message.getStringValue());
                 } else if ("clearHandlers".equals(message.key)) {
                     clearHandlers(message.getItemStackValue());
                 } else {
                     System.out.println("PickItUp: Bad IMC message from " + message.getSender() + " with key " + message.key + ": Unrecognized key.");
                 }
             } catch (Exception e) {
                 System.out.println("PickItUp: Bad IMC message from " + message.getSender() + " with key " + message.key + ": " + e);
             }
         }
     }
 
     public static boolean hasBackpack(EntityPlayer player) {
         if (getBackpack != null) {
             try {
                 ItemStack stack = (ItemStack) getBackpack.invoke(null, player);
                 return stack != null;
             } catch (Exception e) { }
         }
 
         return false;
     }
 
     public static boolean onWhitelist(int id, int meta) {
         if (id < 0 || id > Block.blocksList.length
                    || Block.blocksList[id] == null) {
             return false;
         }
 
         for (BlockRange range : whitelist) {
             if (range.matches(id, meta)) {
                 return range.allow;
             }
         }
 
         return false;
     }
 
     // --- Handler stuff ---
 
     // Adds a handler.  This can also be accomplished by sending an IMC message:
     // {"addHandler": "name of a class that implements ISimplePickup"}
     public static void addHandler(ISimplePickup handler) {
         pickupHandlers.add(handler);
     }
 
     // This is the version of addHandler invoked by IMC.
     // This would be so much easier if the message could just contain an
     // ISimplePickup implementer directly, but there's no way to extract
     // the contents as a raw object.  :(
     public static void addHandler(String handlerName) {
         try {
             Class handlerClass = Class.forName(handlerName);
             if (!ISimplePickup.class.isAssignableFrom(handlerClass)) {
                 System.out.println("PickItUp: Handler class " + handlerName + " does not implement pickitup.ISimplePickup!");
                 return;
             }
 
             @SuppressWarnings("unchecked")
             ISimplePickup handler = (ISimplePickup) handlerClass.getConstructor().newInstance();
             addHandler(handler);
         } catch (Exception e) {
             System.out.println("PickItUp: Unable to get an instance of handler class " + handlerName + ": " + e);
         }
     }
 
     // Removes all handlers that handle a given id/meta.  This is useful for
     // overriding the default handlers provided by PickItUp.  This can also
     // be accomplished by sending an IMC message:
     // {"clearHandlers": ItemStack(id, 1, meta)}
     public static void clearHandlers(int id, int meta) {
         for (int i=0; i<pickupHandlers.size(); i++) {
             ISimplePickup handler = pickupHandlers.get(i);
 
             if (handler.handlesPickupOf(id, meta)) {
                 pickupHandlers.remove(i);
 
                 // Back up a step, so we don't skip an element.
                 i--;
             }
         }
     }
 
     // This is the version of clearHandlers invoked by IMC.
     public static void clearHandlers(ItemStack item) {
         clearHandlers(item.itemID, item.getItemDamage());
     }
 
     // Get the ISimplePickup that will handle this id and meta.  May be null.
     public static ISimplePickup getSimpleHandler(int id, int meta) {
         for (ISimplePickup handler : pickupHandlers) {
             if (handler.handlesPickupOf(id, meta)) {
                 return handler;
             }
         }
 
         return null;
     }
 
     // Get the ICanBePickedUp that will handle this id and meta.  May be null.
     public static ICanBePickedUp getFullHandler(int id, int meta) {
         for (ISimplePickup handler : pickupHandlers) {
             if (handler instanceof ICanBePickedUp &&
                 handler.handlesPickupOf(id, meta)) {
                 return (ICanBePickedUp) handler;
             }
         }
 
         return null;
     }
 
 
     // --- The meat of block pick up and placement. ---
 
     public static boolean pickUpBlock(EntityPlayer player, int x, int y, int z) {
         // Basic information about the block.
         World world = player.worldObj;
         int id = world.getBlockId(x, y, z);
         int meta = world.getBlockMetadata(x, y, z);
 
         ICanBePickedUp handler = getFullHandler(id, meta);
 
         try {
             if (handler != null && !handler.allowPickup(player, x, y, z)) {
                 // Forbidden by handler.
                 return false;
             }
         } catch (Exception e) {
             System.out.println("PickItUp: Exception in handler.allowPickup: " + e);
         }
 
         NBTTagCompound item_tag = null;
         if (handler != null) {
             try {
                 item_tag = handler.pickup(player, x, y, z);
             } catch (Exception e) {
                 System.out.println("PickItUp: Exception in handler.pickup: " + e);
             }
         }
 
         if (item_tag == null) {
             // Advanced information about the block, if any.
             TileEntity te = world.getBlockTileEntity(x, y, z);
             NBTTagCompound block_data = null;
             if (te != null) {
                 block_data = new NBTTagCompound();
                 te.writeToNBT(block_data);
             }
 
             // Pack the block into an NBT tag.
             item_tag = new NBTTagCompound();
             item_tag.setInteger("packed_id", id);
             item_tag.setInteger("packed_meta", meta);
             if (block_data != null) {
                 item_tag.setCompoundTag("packed_data", block_data);
             }
 
             // Delete the block from the world.
             world.removeBlockTileEntity(x, y, z);
             world.setBlock(x, y, z, 0);
         }
 
         if (handler != null) {
             try {
                 handler.afterPickup(player, x, y, z, item_tag);
             } catch (Exception e) {
                 System.out.println("PickItUp: Exception in handler.afterPickup: " + e);
             }
         }
 
         // Save the block in the player's NBT data.
         setBlockHeld(player, item_tag);
 
         return true;
     }
 
     // Try to place the block nicely.
     public static void tryToPlace(NBTTagCompound block, EntityPlayer player,
                            int x, int y, int z, int face,
                            boolean force) {
         int id = block.getInteger("packed_id");
         int meta = block.getInteger("packed_meta");
         ItemStack fakeStack = new ItemStack(id, 1, meta);
 
         // Check to see if the player has permission to place there.
         if (player.canPlayerEdit(x, y, z, face, fakeStack)) {
             if (face == 0) { --y; }
             if (face == 1) { ++y; }
             if (face == 2) { --z; }
             if (face == 3) { ++z; }
             if (face == 4) { --x; }
             if (face == 5) { ++x; }
 
             // Consult with the handler.
             ICanBePickedUp handler = getFullHandler(id, meta);
 
             Boolean allow = null;
             if (handler != null) {
                 try {
                     allow = handler.allowPutdown(player, x, y, z, face, block);
                 } catch (Exception e) {
                     System.out.println("PickItUp: Exception in handler.allowPutdown: " + e);
                 }
             }
 
             if (allow == null) {
                 allow = player.worldObj.canPlaceEntityOnSide(id, x, y, z, false, face, player, fakeStack);
             }
 
             // Check to see if the target is a valid place to put the block.
             if (allow == Boolean.TRUE) {
                 if (placeAt(block, player, x, y, z, face)) {
                     clearBlockHeld(player);
                     return;
                 }
             }
         }
 
         if (force) {
             forcePlace(block, player);
         }
     }
 
     // Try very hard to find a place to put the block.  This scans an 11x11x11
     // cube to find the best place to put it.  If no valid locations are found,
     // the block is deleted.
     public static void forcePlace(NBTTagCompound block, EntityPlayer player) {
         int held_id = block.getInteger("packed_id");
         int held_meta = block.getInteger("packed_meta");
         ICanBePickedUp handler = getFullHandler(held_id, held_meta);
 
         World world = player.worldObj;
         int x = MathHelper.floor_double(player.posX);
         int y = MathHelper.floor_double(player.posY);
         int z = MathHelper.floor_double(player.posZ);
 
         int best_dx = 0;
         int best_dy = 0;
         int best_dz = 0;
         double best_score = Double.POSITIVE_INFINITY;
 
         for (int dy = -5; dy <= 5; dy++) {
             if (y + dy < 0 || y + dy > 255) {
                 continue;
             }
 
             for (int dx = -5; dx <= 5; dx++) {
                 for (int dz = -5; dz <= 5; dz++) {
                     double score = Math.sqrt(dx*dx + dy*dy + dz*dz);
 
                     int id = world.getBlockId(x+dx, y+dy, z+dz);
                     if (id != 0) {
                         Block block_here = Block.blocksList[id];
                         if (block_here.blockMaterial.isReplaceable() ||
                             block_here.isBlockReplaceable(world, x, y, z)) {
                             // We prefer open air over replaceable blocks.
                             score += 10;
                         } else {
                             // Unreplaceable blocks... can't be replaced.  Duh.
                             continue;
                         }
                     }
 
                     if (dx == 0 && dz == 0) {
                         if (dy == 0) {
                             // We'd rather not place it at our feet.
                             score += 20;
                         } else if (dy == 1) {
                             // And we'd REALLY rather not place it in our
                             // head!
                             score += 40;
                         }
                     }
 
                     try {
                         if (handler != null && handler.allowPutdown(player, x+dx, y+dy, z+dz, -1, block) == Boolean.FALSE) {
                             continue;
                         }
                     } catch (Exception e) {
                         System.out.println("PickItUp: Exception in handler.allowPutdown: " + e);
                     }
 
                     if (score < best_score) {
                         // This is our new best place.
                         best_dx = dx;
                         best_dy = dy;
                         best_dz = dz;
                         best_score = score;
                     }
                 }
             }
         }
 
         // If we found a valid spot, use it.
         if (best_score != Double.POSITIVE_INFINITY) {
             placeAt(block, player, x+best_dx, y+best_dy, z+best_dz, -1);
         }
 
         // If not, well, sucks to be you.  This was a force-place, so the block
         // is destroyed.
         // Also, how did you manage to get entombed in an 11x11x11 area filled
         // with non-replaceable blocks?
         // At a guess, the most likely time to hit this is if the player fell
         // out of the world, in which case y+5 is still below the world.
         clearBlockHeld(player);
     }
 
     // Mostly stolen from ItemBlock.placeBlockAt, this handles the nitty-gritty
     // of putting the block in the world, with all appropriate notifications.
     public static boolean placeAt(NBTTagCompound block, EntityPlayer player,
                            int x, int y, int z, int face) {
         if (!(player instanceof EntityPlayerMP)) {
             // This should only be running server-side.
             return false;
         }
 
         World world = player.worldObj;
         int id = block.getInteger("packed_id");
         int meta = block.getInteger("packed_meta");
         ICanBePickedUp handler = getFullHandler(id, meta);
 
         boolean placed = false;
         if (handler != null) {
             try {
                 placed = handler.putdown(player, x, y, z, face, block);
             } catch (Exception e) {
                 System.out.println("PickItUp: Exception in handler.putdown: " + e);
             }
         }
 
         if (!placed) {
             NBTTagCompound data = block.getCompoundTag("packed_data");
             ItemStack fakeStack = new ItemStack(id, 1, meta);
 
             // Get the fractional-block hit components, for slabs and such.
             float hitX = 0f;
             float hitY = 0f;
             float hitZ = 0f;
             double reach = ((EntityPlayerMP)player).theItemInWorldManager.getBlockReachDistance() + 1;
 
             // This would be so much simpler if player.rayTrace actually
             // respected the height of the player's view.  Or if getEyeHeight
             // actually included the offset from sneaking.
             Vec3 playerPos = player.getPosition(0f);
             if (player.isSneaking()) {
                 playerPos.yCoord += 1.54;
             } else {
                 // Er, what?  You should always be sneaking while carrying!
                 // Ah, well, we'll handle it properly anyway.
                 playerPos.yCoord += 1.62;
             }
             Vec3 playerLook = player.getLook(0f);
             Vec3 playerLookTarget = playerPos.addVector(
                                         playerLook.xCoord * reach,
                                         playerLook.yCoord * reach,
                                         playerLook.zCoord * reach);
             MovingObjectPosition target = player.worldObj.rayTraceBlocks(playerPos, playerLookTarget);
 
             // Now that we've finished the ray trace, we can grab the fractional
             // block components out of the hit vector.
             if (target != null) {
                 hitX = (float) (target.hitVec.xCoord - Math.floor(target.hitVec.xCoord));
                 hitY = (float) (target.hitVec.yCoord - Math.floor(target.hitVec.yCoord));
                 hitZ = (float) (target.hitVec.zCoord - Math.floor(target.hitVec.zCoord));
             }
 
             Block theBlock = Block.blocksList[id];
 
             if (theBlock instanceof BlockHalfSlab) {
                 if (!theBlock.isOpaqueCube()) {
                     // Half slab, zero out the "flipped upside-down" bit.
                     meta &= ~0x8;
                 }
             } else if (theBlock instanceof BlockStairs) {
                 // Stairs block, zero out the "flipped upside-down" bit.
                 meta &= ~0x4;
             }
 
             // Give the Block a chance to change the metadata we're about to
             // set.
             meta = Block.blocksList[id].onBlockPlaced(world, x, y, z, face,
                                                       hitX, hitY, hitZ,
                                                       meta);
 
             if (!world.setBlock(x, y, z, id, meta, 3))
             {
                 return false;
             }
 
             if (world.getBlockId(x, y, z) == id)
             {
                 Block.blocksList[id].onBlockPlacedBy(world, x, y, z, player,
                                                      fakeStack);
                 Block.blocksList[id].onPostBlockPlaced(world, x, y, z, meta);
 
                 if (data != null && !data.hasNoTags()) {
                     TileEntity te = TileEntity.createAndLoadEntity(data);
                     world.setBlockTileEntity(x, y, z, te);
                 }
             }
         }
 
         ISimplePickup simpleHandler = null;
         if (handler != null) {
             simpleHandler = handler;
         } else {
             simpleHandler = getSimpleHandler(id, meta);
         }
 
         if (simpleHandler != null) {
             try {
                 simpleHandler.afterPutdown(player, x, y, z, face);
             } catch (Exception e) {
                 System.out.println("PickItUp: Exception in handler.afterPutdown: " + e);
             }
         }
 
         return true;
     }
 
 
     // --- Stuff stored in the player's NBT ---
 
     // Fetches the special tag that's guaranteed to survive player respawns,
     // including dimensional teleports.
     public static NBTTagCompound getPersistedTag(EntityPlayer player) {
         NBTTagCompound player_data = player.getEntityData();
         if (!player_data.hasKey(player.PERSISTED_NBT_TAG)) {
             player_data.setCompoundTag(player.PERSISTED_NBT_TAG, new NBTTagCompound());
         }
         return player_data.getCompoundTag(player.PERSISTED_NBT_TAG);
     }
 
     // Returns the tag for the block the player is currently holding, if any.
     @SuppressWarnings("unchecked")
     public static NBTTagCompound getBlockHeld(EntityPlayer player) {
         if (player.worldObj.isRemote) {
             try {
                 ItemStack watchedStack = player.getDataWatcher().getWatchableObjectItemStack(DW_INDEX);
                 if (watchedStack.stackSize > 0) {
                     return watchedStack.getTagCompound();
                 } else {
                     return null;
                 }
 
             } catch (NullPointerException e) {
                 return null;
             }
         } else {
             NBTTagCompound player_persisted = getPersistedTag(player);
             if (!player_persisted.hasKey(HELD_TAG)) {
                 return null;
             } else {
                 return player_persisted.getCompoundTag(HELD_TAG);
             }
         }
     }
 
     // Is the player currently holding a block?
     public static boolean isHoldingBlock(EntityPlayer player) {
         if (player.worldObj.isRemote) {
             try {
                 return player.getDataWatcher().getWatchableObjectItemStack(DW_INDEX).stackSize > 0;
             } catch (NullPointerException e) {
                 return false;
             }
         } else {
             NBTTagCompound player_persisted = getPersistedTag(player);
             return player_persisted.hasKey(HELD_TAG);
         }
     }
 
     // As getBlockHeld, but for the local player.
     @SideOnly(Side.CLIENT)
     public static NBTTagCompound getMyBlockHeld() {
         return getBlockHeld(Minecraft.getMinecraft().thePlayer);
     }
 
     // As isHoldingBlock, but for the local player.
     @SideOnly(Side.CLIENT)
     public static boolean amIHoldingABlock() {
         return isHoldingBlock(Minecraft.getMinecraft().thePlayer);
     }
 
     // Where should we render the block that's currently held?
     @SideOnly(Side.CLIENT)
     public static ChunkCoordinates getHeldRenderCoords(float partialTick) {
         Minecraft mc = Minecraft.getMinecraft();
         EntityPlayer player = mc.thePlayer;
 
         double reach = (double)mc.playerController.getBlockReachDistance();
         MovingObjectPosition target = mc.renderViewEntity.rayTrace(reach, 0);
         if (target != null) {
             // We're looking at a block, so draw it placed there.
             int x = target.blockX;
             int y = target.blockY;
             int z = target.blockZ;
             int face = target.sideHit;
             if (face == 0) { --y; }
             if (face == 1) { ++y; }
             if (face == 2) { --z; }
             if (face == 3) { ++z; }
             if (face == 4) { --x; }
             if (face == 5) { ++x; }
 
             return new ChunkCoordinates(x, y, z);
         } else {
             // Not looking at a block, just put it somewhere in front of us.
             Vec3 look = player.getLook(partialTick);
             Vec3 spot = player.getPosition(partialTick).addVector(look.xCoord * 3.5,
                                                                   look.yCoord * 3.5,
                                                                   look.zCoord * 3.5);
             return new ChunkCoordinates(MathHelper.floor_double(spot.xCoord),
                                         MathHelper.floor_double(spot.yCoord),
                                         MathHelper.floor_double(spot.zCoord));
         }
     }
 
     // Builds an ItemStack containing the data about the block held.
     public static ItemStack buildHeldItemStack(EntityPlayer player) {
         NBTTagCompound block = getBlockHeld(player);
 
         if (block == null) {
             return new ItemStack(0, 0, 0);
         }
 
         // Build an ItemStack for the block held.
         int id = block.getInteger("packed_id");
         int meta = block.getInteger("packed_meta");
         ItemStack syncStack = new ItemStack(id, 1, meta);
         syncStack.setTagCompound(block);
 
         return syncStack;
     }
 
     // Sets the block the player is currently holding.
     public static void setBlockHeld(EntityPlayer player, NBTTagCompound block) {
         NBTTagCompound player_persisted = getPersistedTag(player);
         player_persisted.setCompoundTag(HELD_TAG, block);
 
         // Sync the block held.
         player.getDataWatcher().updateObject(27, buildHeldItemStack(player));
     }
 
     // Remove the stored data after the player set down (or otherwise returned
     // to the world) the block they were holding.
     public static void clearBlockHeld(EntityPlayer player) {
         NBTTagCompound player_persisted = getPersistedTag(player);
         if (player_persisted.hasKey(HELD_TAG)) {
             player_persisted.removeTag(HELD_TAG);
         }
         player.getDataWatcher().updateObject(27, new ItemStack(0, 0, 0));
     }
 
     public static class EventListener {
         // This is called when the player right clicks on a block.
         // (Or a couple other conditions, but we ignore them.)
         //
         // We use it to handle picking up and placing blocks under normal
         // conditions.
         //
         // Set to high priority just in case someone else is mucking with right
         // clicks.  We've got specific enough circumstances that we require that
         // it's safer to get ours out of the way before anyone else pokes at it.
         // Highest priority is left free, just in case someone *really* needs to
         // override us.
         @ForgeSubscribe(priority=EventPriority.HIGH)
         public void onInteract(PlayerInteractEvent event) {
             if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR
              || event.action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK
              || event.isCanceled() || event.useBlock == DENY)
             {
                 // We can ignore this.
                 return;
             }
 
             NBTTagCompound block_held = getBlockHeld(event.entityPlayer);
             if (block_held != null) {
                 if (!event.entityPlayer.worldObj.isRemote) {
                     // Try to place the block.
                     tryToPlace(block_held, event.entityPlayer, event.x, event.y, event.z,
                                event.face, !event.entityPlayer.isSneaking());
                 }
 
                 // Prevent most things from happening.  This does not block
                 // the packet from client to server, but it will stop a fence
                 // gate from opening client-side, for instance.
                 event.useBlock = DENY;
                 event.useItem = DENY;
                 return;
             }
 
             if (event.entityPlayer.getHeldItem() != null) {
                 // They're holding an item, so let them place/use it.
                 return;
             }
 
             if (!event.entityPlayer.isSneaking()) {
                 // They're not sneaking, so let htem interact/place normally.
                 return;
             }
 
             // BetterStorage integration: Allow the click to fall through to
             // BetterStorage if it would put a backpack down.
             //
             // Ideally, we'd check for it being a valid place to put the
             // backpack, too, but that's more effort than it's worth.  Submit
             // a pull request if it really bothers you.  :)
             if (event.face == 1 && hasBackpack(event.entityPlayer)) {
                 return;
             }
 
             // The player has right-clicked on a block, their hand is empty, and
             // they are sneaking.  Check to see if the block can be picked up.
             int id = event.entityPlayer.worldObj.getBlockId(event.x, event.y, event.z);
             int meta = event.entityPlayer.worldObj.getBlockMetadata(event.x, event.y, event.z);
             if (event.entityPlayer.worldObj.isRemote || onWhitelist(id, meta)) {
                 ICanBePickedUp handler = getFullHandler(id, meta);
                 if (handler != null &&
                     !handler.allowPickup(event.entityPlayer, event.x, event.y,
                                          event.z)) {
                     return;
                 }
 
                 // Valid block, PickItUp.
                 if (pickUpBlock(event.entityPlayer, event.x, event.y, event.z)) {
                     // Prevent most things from happening.  This does not block
                     // the packet from client to server, but it will stop a
                     // fence gate from opening client-side, for instance.
                     event.useBlock = DENY;
                     event.useItem = DENY;
                     return;
                 }
             } else {
                 // Let's just make sure the player isn't left holding a phantom
                 // block.
                 PlayerTracker.updateHeldState(event.entityPlayer);
             }
         }
 
         // This is called whenever an entity (including the player) dies.
         //
         // We use it to force the held block to be placed in world before
         // the player's items drop.
         @ForgeSubscribe
         public void onDeath(LivingDeathEvent event) {
             if (!(event.entity instanceof EntityPlayer) ||
                 event.entity.worldObj.isRemote) {
                 return;
             }
 
             EntityPlayer player = (EntityPlayer) event.entity;
 
             // If keepInventory is true, you can hold a block through a respawn.
             if (player.worldObj.getGameRules().getGameRuleBooleanValue("keepInventory")) {
                 return;
             }
 
 
             NBTTagCompound block_held = getBlockHeld(player);
             if (block_held != null) {
                 forcePlace(block_held, player);
             }
         }
 
 
         // This is called whenever damage is inflicted on an entity.
         //
         // We use it to reduce attack damage while holding a block.
         //
         // Low priority is used here to come after any mods that set the damage
         // directly, as we're applying a 50% damage penalty.
         @ForgeSubscribe(priority=EventPriority.LOW)
         @SuppressWarnings("unchecked")
         public void onDamage(LivingHurtEvent event) {
             if (!event.source.damageType.equals("player")) {
                 return;
             }
 
             EntityPlayer player = (EntityPlayer) ((EntityDamageSource) event.source).getEntity();
             // ammount [sic]
             if (isHoldingBlock(player) && event.ammount > 0) {
                 event.ammount /= 2;
                 if (event.ammount == 0) {
                     event.ammount = 1;
                 }
             }
         }
 
         // This is called to determine how quickly a player can break a block.
         //
         // We use it to make block-breaking with a held block slow.
         //
         // Low priority is used here to come after any mods that set the speed
         // directly, as we're applying a 50% speed penalty.
         @ForgeSubscribe(priority=EventPriority.LOW)
         public void digSpeed(PlayerEvent.BreakSpeed event) {
             if (isHoldingBlock(event.entityPlayer)) {
                 event.newSpeed /= 2.0f;
             }
         }
     }
 
     public static class BlockRange {
         public static final Pattern REGEX = Pattern.compile("^(-)?([0-9]+)(?:-([0-9]+))?(?::([0-9]+)(?:-([0-9]+))?)?$");
         public int id_start = -1;
         public int id_end = -1;
         public int meta_start = -1;
         public int meta_end = -1;
         public boolean allow = true;
         public boolean valid = true;
 
         public BlockRange(String init) {
             Matcher match = REGEX.matcher(init);
             if (!match.matches()) {
                 System.out.println("Incorrect block range format: " + init);
                 valid = false;
                 return;
             }
 
             if ("-".equals(match.group(1))) {
                 allow = false;
             }
 
             try {
                 id_start = Integer.parseInt(match.group(2));
                 if (match.group(3) != null) {
                     id_end = Integer.parseInt(match.group(3));
                 } else {
                     id_end = id_start;
                 }
                 if (match.group(4) != null) {
                     meta_start = Integer.parseInt(match.group(4));
                 }
                 if (match.group(5) != null) {
                     meta_end = Integer.parseInt(match.group(5));
                 } else {
                     meta_end = meta_start;
                 }
             } catch (NumberFormatException e) {
                 System.out.println("Incorrect block range format: " + init);
                 valid = false;
             }
         }
 
         public boolean matches(int id, int meta) {
             if (id < id_start || id > id_end) {
                 return false;
             }
 
             if (meta_start == -1) {
                 return true;
             }
 
             if (meta < meta_start || meta > meta_end) {
                 return false;
             }
 
             return true;
         }
     }
 }
