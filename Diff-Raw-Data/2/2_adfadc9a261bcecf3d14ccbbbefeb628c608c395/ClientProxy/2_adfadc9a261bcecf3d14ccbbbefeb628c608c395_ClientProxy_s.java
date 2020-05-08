 
 package me.heldplayer.mods.Smartestone.client;
 
 import me.heldplayer.api.Smartestone.micro.IMicroBlockMaterial;
 import me.heldplayer.api.Smartestone.micro.IMicroBlockSubBlock;
 import me.heldplayer.api.Smartestone.micro.MicroBlockAPI;
 import me.heldplayer.api.Smartestone.micro.MicroBlockInfo;
 import me.heldplayer.api.Smartestone.micro.rendering.RenderFaceHelper;
 import me.heldplayer.mods.Smartestone.CommonProxy;
 import me.heldplayer.mods.Smartestone.ModSmartestone;
 import me.heldplayer.mods.Smartestone.block.BlockMicro;
 import me.heldplayer.mods.Smartestone.block.BlockMulti;
 import me.heldplayer.mods.Smartestone.client.renderer.BlockRendererMicroBlock;
 import me.heldplayer.mods.Smartestone.client.renderer.BlockRendererSmartestones;
 import me.heldplayer.mods.Smartestone.client.renderer.ItemRendererMicroBlock;
 import me.heldplayer.mods.Smartestone.client.renderer.tileentity.TileEntityCraftingChestRenderer;
 import me.heldplayer.mods.Smartestone.client.renderer.tileentity.TileEntityInductionishFurnaceRenderer;
 import me.heldplayer.mods.Smartestone.client.renderer.tileentity.TileEntityItemStandRenderer;
 import me.heldplayer.mods.Smartestone.item.ItemMicroBlock;
 import me.heldplayer.mods.Smartestone.tileentity.TileEntityCraftingChest;
 import me.heldplayer.mods.Smartestone.tileentity.TileEntityInductionishFurnace;
 import me.heldplayer.mods.Smartestone.tileentity.TileEntityItemStand;
 import me.heldplayer.mods.Smartestone.util.Objects;
 import me.heldplayer.mods.Smartestone.util.RayTrace;
 import me.heldplayer.util.HeldCore.client.MC;
 import net.minecraft.block.Block;
 import net.minecraft.client.renderer.entity.RenderManager;
 import net.minecraft.client.renderer.texture.TextureMap;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.util.EnumMovingObjectType;
 import net.minecraft.util.Icon;
 import net.minecraft.util.MovingObjectPosition;
 import net.minecraft.world.World;
 import net.minecraftforge.client.MinecraftForgeClient;
 import net.minecraftforge.client.event.DrawBlockHighlightEvent;
 import net.minecraftforge.client.event.RenderGameOverlayEvent.Text;
 import net.minecraftforge.common.ForgeDirection;
 import net.minecraftforge.event.ForgeSubscribe;
 import cpw.mods.fml.client.registry.ClientRegistry;
 import cpw.mods.fml.client.registry.RenderingRegistry;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 @SideOnly(Side.CLIENT)
 public class ClientProxy extends CommonProxy {
 
     public static final String textureLocation = "/mods/Smartestone/textures/";
     public static Icon missingTextureIcon;
 
     @Override
     public void preInit(FMLPreInitializationEvent event) {
         super.preInit(event);
 
         if (ModSmartestone.HDTextures.getValue()) {
            Objects.TEXTURE_PREFIX += "/HD";
         }
     }
 
     @Override
     public void postInit(FMLPostInitializationEvent event) {
         super.postInit(event);
 
         BlockMulti.renderId = RenderingRegistry.getNextAvailableRenderId();
         RenderingRegistry.registerBlockHandler(BlockMulti.renderId, new BlockRendererSmartestones(BlockMulti.renderId));
 
         BlockMicro.renderId = RenderingRegistry.getNextAvailableRenderId();
         RenderingRegistry.registerBlockHandler(BlockMicro.renderId, new BlockRendererMicroBlock(BlockMicro.renderId));
 
         try {
             ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCraftingChest.class, new TileEntityCraftingChestRenderer());
             ClientRegistry.bindTileEntitySpecialRenderer(TileEntityInductionishFurnace.class, new TileEntityInductionishFurnaceRenderer());
             ClientRegistry.bindTileEntitySpecialRenderer(TileEntityItemStand.class, new TileEntityItemStandRenderer());
         }
         catch (NoSuchMethodError e) {}
 
         MinecraftForgeClient.registerItemRenderer(Objects.itemMicroBlock.itemID, new ItemRendererMicroBlock());
     }
 
     @ForgeSubscribe
     public void onText(Text event) {
         event.right.add(RenderFaceHelper.getDebugString());
     }
 
     @ForgeSubscribe(receiveCanceled = true)
     public void onDrawBlockHighlight(DrawBlockHighlightEvent event) {
         ItemStack stack = event.currentItem;
 
         if (event.target.typeOfHit == EnumMovingObjectType.TILE) {
             MovingObjectPosition pos = event.target;
             EntityPlayer player = event.player;
             World world = player.worldObj;
 
             int blockId = world.getBlockId(pos.blockX, pos.blockY, pos.blockZ);
 
             if (Block.blocksList[blockId] instanceof BlockMicro) {
                 RayTrace.rayTrace(world, player, pos.blockX, pos.blockY, pos.blockZ);
             }
 
             if (stack != null) {
                 if (stack.getItem() instanceof ItemMicroBlock) {
                     ForgeDirection direction = ForgeDirection.getOrientation(event.target.sideHit);
 
                     int id = world.getBlockId(pos.blockX + direction.offsetX, pos.blockY + direction.offsetY, pos.blockZ + direction.offsetZ);
 
                     if (id != 0 && id != Objects.blockMicro.blockID) {
                         return;
                     }
 
                     NBTTagCompound compound = stack.getTagCompound();
 
                     IMicroBlockMaterial material = MicroBlockAPI.getMaterial(compound != null ? compound.getString("Material") : "null");
                     IMicroBlockSubBlock subBlock = MicroBlockAPI.getSubBlock(compound != null ? compound.getString("Type") : "null");
 
                     if (material == null || subBlock == null) {
                         return;
                     }
 
                     MicroBlockInfo info = new MicroBlockInfo(material, subBlock, 0);
 
                     int data = subBlock.onItemUse(player, pos.sideHit, (float) pos.hitVec.xCoord, (float) pos.hitVec.yCoord, (float) pos.hitVec.zCoord);
 
                     info.setData(data);
 
                     if (RenderManager.instance.renderEngine != null) {
                         MC.getRenderEngine().func_110577_a(TextureMap.field_110575_b);
                     }
 
                     subBlock.drawHitbox(event, info);
                 }
             }
         }
     }
 }
