 package slimevoid.tmf.items.tools;
 
 import java.util.List;
 
 import net.minecraft.block.Block;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.EntityLivingBase;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.EnumAction;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagList;
 import net.minecraft.util.StatCollector;
 import net.minecraft.world.World;
 import net.minecraftforge.event.entity.player.PlayerInteractEvent;
 import slimevoid.compatibility.thaumcraft.ThaumcraftStatic;
 import slimevoid.compatibility.tinkersconstruct.TinkersConstructStatic;
 import slimevoid.tmf.core.helpers.ItemHelper;
 import slimevoid.tmf.core.lib.CommandLib;
 import slimevoid.tmf.core.lib.DataLib;
 import slimevoid.tmf.core.lib.MessageLib;
 import slimevoid.tmf.core.lib.NBTLib;
 import slimevoid.tmf.core.lib.PacketLib;
 import slimevoid.tmf.items.ItemTMF;
 import slimevoidlib.nbt.NBTHelper;
 import thaumcraft.api.IRepairable;
 import thaumcraft.api.IRepairableExtended;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class ItemMiningToolBelt extends ItemTMF implements IRepairable,
         IRepairableExtended {
     public ItemMiningToolBelt(int itemID) {
         super(itemID);
         this.setMaxStackSize(1);
         this.setFull3D();
     }
 
     @Override
     public String getUnlocalizedName(ItemStack itemstack) {
         ItemStack tool = this.getSelectedTool(itemstack);
         return tool != null ? tool.getUnlocalizedName() : super.getUnlocalizedName(itemstack);
     }
 
     @Override
     public boolean requiresMultipleRenderPasses() {
         return true;
     }
 
     @Override
     public int getRenderPasses(int metadata) {
         return super.getRenderPasses(metadata);
     }
 
     @Override
     public boolean itemInteractionForEntity(ItemStack itemstack, EntityPlayer entityplayer, EntityLivingBase entitylivingbase) {
         boolean flag = false;
 
         if (ItemHelper.isToolBelt(itemstack)) {
             ItemStack tool = this.getSelectedTool(itemstack);
             ItemStack toolCopy = ItemStack.copyItemStack(tool);
 
             if (tool != null) {
                 flag = tool.getItem().itemInteractionForEntity(tool,
                                                                entityplayer,
                                                                entitylivingbase);
                 this.updateToolInToolBelt(entityplayer.getEntityWorld(),
                                           entityplayer,
                                           itemstack,
                                           tool,
                                           toolCopy);
             }
         }
 
         return flag;
     }
 
     @Override
     public boolean isDamaged(ItemStack itemstack) {
         return this.isToolDamaged(itemstack);
     }
 
     protected boolean isToolDamaged(ItemStack itemstack) {
         ItemStack tool = ItemHelper.getSelectedTool(itemstack);
         return tool != null ? tool.isItemDamaged() : false;
     }
 
     @Override
     public int getMaxDamage(ItemStack itemstack) {
         return this.getMaxToolDamage(itemstack);
     }
 
     protected int getMaxToolDamage(ItemStack itemstack) {
         ItemStack tool = ItemHelper.getSelectedTool(itemstack);
         return tool != null ? tool.getMaxDamage() : 0;
     }
 
     @Override
     public int getDamage(ItemStack itemstack) {
         ItemStack tool = ItemHelper.getSelectedTool(itemstack);
         return tool != null ? tool.getItemDamage() : super.getDamage(itemstack);
     }
 
     @Override
     public int getDisplayDamage(ItemStack itemstack) {
         return this.getToolDisplayDamage(itemstack);
     }
 
     @Override
     public String getItemStackDisplayName(ItemStack itemstack) {
         ItemStack tool = this.getSelectedTool(itemstack);
         return tool != null && tool.getItem() != null ? tool.getItem().getItemStackDisplayName(tool) : super.getItemStackDisplayName(itemstack);
     }
 
     protected int getToolDisplayDamage(ItemStack itemstack) {
         ItemStack tool = ItemHelper.getSelectedTool(itemstack);
         return tool != null ? tool.getItemDamage() : 0;
     }
 
     @Override
     public void setDamage(ItemStack itemstack, int damage) {
         this.setToolDamage(itemstack,
                            damage);
     }
 
     protected void setToolDamage(ItemStack itemstack, int damage) {
         ItemStack tool = this.getSelectedTool(itemstack);
 
         if (tool != null) {
             tool.setItemDamage(damage);
             this.updateToolBelt(itemstack,
                                 tool);
         }
     }
 
     @Override
     public void onUpdate(ItemStack itemstack, World world, Entity entity, int tick, boolean isHeld) {
         if (!world.isRemote && entity instanceof EntityLivingBase) {
             if (!itemstack.hasTagCompound()) {
                 this.getSelectedSlot(itemstack);
             }
 
             this.doTickTools(itemstack,
                              world,
                              entity,
                              tick,
                              isHeld);
         }
     }
 
     protected void doTickTools(ItemStack itemstack, World world, Entity entity, int tick, boolean isHeld) {
         if (ItemHelper.isToolBelt(itemstack)) {
             ItemStack tool = this.getSelectedTool(itemstack);
 
             if (tool != null && tool.getItem() != null) {
                 ItemStack toolCopy = ItemStack.copyItemStack(tool);
                 tool.getItem().onUpdate(tool,
                                         world,
                                         entity,
                                         tick,
                                         isHeld);
                 this.updateToolInToolBelt(world,
                                           (EntityLivingBase) entity,
                                           itemstack,
                                           tool,
                                           toolCopy);
             }
         }
     }
 
     @Override
     public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityplayer) {
         return this.doItemRightClick(itemstack,
                                      world,
                                      entityplayer);
     }
 
     @Override
     public boolean onItemUse(ItemStack itemstack, EntityPlayer entityplayer, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
         return this.doItemUse(itemstack,
                               entityplayer,
                               world,
                               x,
                               y,
                               z,
                               side,
                               hitX,
                               hitY,
                               hitZ);
     }
 
     @Override
     public boolean onItemUseFirst(ItemStack itemstack, EntityPlayer entityplayer, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
         return this.doItemUseFirst(itemstack,
                                    entityplayer,
                                    world,
                                    x,
                                    y,
                                    z,
                                    side,
                                    hitX,
                                    hitY,
                                    hitZ);
     }
 
     @Override
     public ItemStack onEaten(ItemStack itemstack, World world, EntityPlayer entityplayer) {
         this.doFoodEaten(itemstack,
                          world,
                          entityplayer);
         return itemstack;
     }
 
     @Override
     public int getMaxItemUseDuration(ItemStack itemstack) {
         ItemStack tool = this.getSelectedTool(itemstack);
         return tool != null ? tool.getMaxItemUseDuration() : super.getMaxItemUseDuration(itemstack);
     }
 
     public ItemStack doItemRightClick(ItemStack itemstack, World world, EntityPlayer entityplayer) {
         ItemStack tool = this.getSelectedTool(itemstack);
         ItemStack toolCopy = ItemStack.copyItemStack(tool);
 
         if (tool != null) {
             tool = tool.useItemRightClick(world,
                                           entityplayer);
 
             if (entityplayer.isUsingItem()) {
                 entityplayer.setItemInUse(itemstack,
                                           tool.getMaxItemUseDuration());
             }
 
             this.updateToolInToolBelt(world,
                                       entityplayer,
                                       itemstack,
                                       tool,
                                       toolCopy);
         }
         return itemstack;
     }
 
     public boolean doItemUse(ItemStack itemstack, EntityPlayer entityplayer, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
         ItemStack tool = this.getSelectedTool(itemstack);
         ItemStack toolCopy = ItemStack.copyItemStack(tool);
         boolean flag = false;
 
         if (tool != null && tool.getItem() != null) {
             flag = tool.getItem().onItemUse(tool,
                                             entityplayer,
                                             world,
                                             x,
                                             y,
                                             z,
                                             side,
                                             hitX,
                                             hitY,
                                             hitZ);
             this.updateToolInToolBelt(world,
                                       entityplayer,
                                       itemstack,
                                       tool,
                                       toolCopy);
         }
         return flag;
     }
 
     public boolean doItemUseFirst(ItemStack itemstack, EntityPlayer entityplayer, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
         ItemStack tool = this.getSelectedTool(itemstack);
         ItemStack toolCopy = ItemStack.copyItemStack(tool);
         boolean flag = false;
        if (tool != null && tool.getItem() != null) {
             flag = tool.getItem().onItemUseFirst(tool,
                                                  entityplayer,
                                                  world,
                                                  x,
                                                  y,
                                                  z,
                                                  side,
                                                  hitX,
                                                  hitY,
                                                  hitZ);
             this.updateToolInToolBelt(world,
                                       entityplayer,
                                       itemstack,
                                       tool,
                                       toolCopy);
         }
        return flag;
     }
 
     public void doFoodEaten(ItemStack itemstack, World world, EntityPlayer entityplayer) {
         ItemStack tool = this.getSelectedTool(itemstack);
         ItemStack toolCopy = ItemStack.copyItemStack(tool);
 
         if (tool != null) {
             tool = tool.onFoodEaten(world,
                                     entityplayer);
             this.updateToolInToolBelt(world,
                                       entityplayer,
                                       itemstack,
                                       tool,
                                       toolCopy);
         }
     }
 
     @Override
     public void onUsingItemTick(ItemStack itemstack, EntityPlayer entityplayer, int count) {
         ItemStack tool = this.getSelectedTool(itemstack);
         ItemStack toolCopy = ItemStack.copyItemStack(tool);
 
         if (tool != null && tool.getItem() != null) {
             tool.getItem().onUsingItemTick(tool,
                                            entityplayer,
                                            count);
             this.updateToolInToolBelt(entityplayer.getEntityWorld(),
                                       entityplayer,
                                       itemstack,
                                       tool,
                                       toolCopy);
         }
     }
 
     @Override
     public void onPlayerStoppedUsing(ItemStack itemstack, World world, EntityPlayer entityplayer, int count) {
         ItemStack tool = this.getSelectedTool(itemstack);
         ItemStack toolCopy = ItemStack.copyItemStack(tool);
 
         if (tool != null && tool.getItem() != null) {
             tool.getItem().onPlayerStoppedUsing(tool,
                                                 world,
                                                 entityplayer,
                                                 count);
             this.updateToolInToolBelt(world,
                                       entityplayer,
                                       itemstack,
                                       tool,
                                       toolCopy);
         }
     }
 
     @Override
     public boolean onLeftClickEntity(ItemStack itemstack, EntityPlayer entityplayer, Entity entity) {
         return this.doLeftClickEntity(itemstack,
                                       entityplayer,
                                       entity);
     }
 
     private boolean doLeftClickEntity(ItemStack itemstack, EntityPlayer entityplayer, Entity entity) {
         ItemStack tool = this.getSelectedTool(itemstack);
         ItemStack toolCopy = ItemStack.copyItemStack(tool);
         boolean onLeftClickEntity = false;
 
         if (tool != null) {
             onLeftClickEntity = tool.getItem().onLeftClickEntity(tool,
                                                                  entityplayer,
                                                                  entity);
             this.updateToolInToolBelt(entityplayer.getEntityWorld(),
                                       entityplayer,
                                       itemstack,
                                       tool,
                                       toolCopy);
         }
 
         return onLeftClickEntity;
     }
 
     @Override
     public boolean hitEntity(ItemStack stack, EntityLivingBase mob, EntityLivingBase player) {
         return this.doHitEntity(stack,
                                 mob,
                                 player);
     }
 
     private boolean doHitEntity(ItemStack itemstack, EntityLivingBase mob, EntityLivingBase entityplayer) {
         ItemStack tool = this.getSelectedTool(itemstack);
         ItemStack toolCopy = ItemStack.copyItemStack(tool);
         boolean hitEntity = false;
 
         if (tool != null && tool.getItem() != null) {
             hitEntity = tool.getItem().hitEntity(tool,
                                                  mob,
                                                  entityplayer);
             this.updateToolInToolBelt(((EntityPlayer) entityplayer).getEntityWorld(),
                                       entityplayer,
                                       itemstack,
                                       tool,
                                       toolCopy);
         }
 
         return hitEntity;
     }
 
     @Override
     public boolean onBlockDestroyed(ItemStack itemstack, World world, int x, int y, int z, int side, EntityLivingBase entityliving) {
         return this.doDestroyBlock(itemstack,
                                    world,
                                    x,
                                    y,
                                    z,
                                    side,
                                    entityliving,
                                    super.onBlockDestroyed(itemstack,
                                                           world,
                                                           x,
                                                           y,
                                                           z,
                                                           side,
                                                           entityliving));
     }
 
     @Override
     public boolean onBlockStartBreak(ItemStack itemstack, int x, int y, int z, EntityPlayer entityplayer) {
         return this.doStartBreakBlock(itemstack,
                                       x,
                                       y,
                                       z,
                                       entityplayer,
                                       super.onBlockStartBreak(itemstack,
                                                               x,
                                                               y,
                                                               z,
                                                               entityplayer));
     }
 
     private boolean doStartBreakBlock(ItemStack itemstack, int x, int y, int z, EntityPlayer entityplayer, boolean onBlockStartBreak) {
         ItemStack tool = this.getSelectedTool(itemstack);
         ItemStack toolCopy = ItemStack.copyItemStack(tool);
 
         if (tool != null && tool.getItem() != null) {
             onBlockStartBreak = tool.getItem().onBlockStartBreak(tool,
                                                                  x,
                                                                  y,
                                                                  z,
                                                                  entityplayer);
             this.updateToolInToolBelt(entityplayer.getEntityWorld(),
                                       entityplayer,
                                       itemstack,
                                       tool,
                                       toolCopy);
         }
 
         return onBlockStartBreak;
     }
 
     public boolean doDestroyBlock(ItemStack itemstack, World world, int x, int y, int z, int side, EntityLivingBase entitylivingbase, boolean onBlockDestroyed) {
         ItemStack tool = this.getSelectedTool(itemstack);
         ItemStack toolCopy = ItemStack.copyItemStack(tool);
 
         if (tool != null && tool.getItem() != null) {
             onBlockDestroyed = tool.getItem().onBlockDestroyed(tool,
                                                                world,
                                                                x,
                                                                y,
                                                                z,
                                                                side,
                                                                entitylivingbase);
             this.updateToolInToolBelt(world,
                                       entitylivingbase,
                                       itemstack,
                                       tool,
                                       toolCopy);
         }
 
         return onBlockDestroyed;
     }
 
     @Override
     public float getStrVsBlock(ItemStack itemstack, Block block) {
         return this.getStrVsBlock(itemstack,
                                   block,
                                   0);
     }
 
     @Override
     public float getStrVsBlock(ItemStack itemstack, Block block, int metadata) {
         ItemStack tool = ItemHelper.getSelectedTool(itemstack);
         float strVsBlock = 1.0F;
 
         if (tool != null && tool.getItem() != null) {
             strVsBlock = tool.getItem().getStrVsBlock(tool,
                                                       block,
                                                       metadata);
         }
 
         return this.getStrengthFromTool(itemstack,
                                         block,
                                         metadata,
                                         strVsBlock);
     }
 
     protected float getStrengthFromTool(ItemStack itemstack, Block block, int metadata, float originalSpeed) {
         if (ItemHelper.isToolBelt(itemstack)) {
             ItemStack tool = null;
             float multiplier = 1.0F;
 
             if (this.isMiningModeEnabled(itemstack)) {
                 tool = this.selectToolForBlock(itemstack,
                                                block,
                                                metadata,
                                                originalSpeed);
                 multiplier = DataLib.MINING_MODE_STRENGTH;
             } else {
                 tool = this.getSelectedTool(itemstack);
             }
             // If an item exists in the selected slot of the Tool Belt
             if (tool != null && tool.getItem() != null) {
                 // Generate break speed for that Tool vs. Block
                 float newSpeed = tool.getItem().getStrVsBlock(tool,
                                                               block,
                                                               metadata);
                 return newSpeed > originalSpeed ? newSpeed * multiplier : originalSpeed;
             }
         }
 
         return originalSpeed;
     }
 
     protected ItemStack selectToolForBlock(ItemStack itemstack, Block block, int metadata, float originalSpeed) {
         float fastestSpeed = originalSpeed;
         int selection = this.getSelectedSlot(itemstack);
         int selectedSlot = selection;
         ItemStack[] tools = ItemHelper.getTools(itemstack);
 
         if (tools != null) {
             for (int i = 0; i <= DataLib.TOOL_BELT_SELECTED_MAX; ++i) {
                 ItemStack tool = tools[i];
 
                 if (tool != null && tool.getItem() != null) {
                     float breakSpeed = tool.getItem().getStrVsBlock(tool,
                                                                     block,
                                                                     metadata);
 
                     if (breakSpeed > fastestSpeed) {
                         fastestSpeed = breakSpeed;
                         selection = i;
                     }
                 }
             }
         }
 
         return selection == selectedSlot ? this.getSelectedTool(itemstack) : this.setSelectedTool(itemstack,
                                                                                                   selection);
     }
 
     @Override
     public boolean canHarvestBlock(Block block, ItemStack itemstack) {
         if (ItemHelper.isToolBelt(itemstack)) {
             ItemStack tool = this.getSelectedTool(itemstack);
             boolean selectedToolCanHarvest = tool != null ? tool.canHarvestBlock(block) : false;
 
             if (this.isMiningModeEnabled(itemstack)) {
                 return selectedToolCanHarvest ? true : this.isToolAvailable(block,
                                                                             itemstack);
             }
 
             if (tool != null) {
                 return selectedToolCanHarvest;
             }
         }
 
         return super.canHarvestBlock(block,
                                      itemstack);
     }
 
     private boolean isToolAvailable(Block block, ItemStack itemstack) {
         ItemStack[] tools = ItemHelper.getTools(itemstack);
         boolean canHarvest = false;
 
         for (int i = 0; i < tools.length; ++i) {
             ItemStack tool = tools[i];
 
             if (tool != null && tool.canHarvestBlock(block)) {
                 canHarvest = true;
             }
         }
 
         return canHarvest;
     }
 
     public void updateToolBelt(ItemStack toolBelt, ItemStack tool) {
         if (tool.stackSize == 0) {
             tool = null;
         }
 
         if (toolBelt.hasTagCompound()) {
             int selectedSlot = this.getSelectedSlot(toolBelt);
             ItemStack[] tools = ItemHelper.getTools(toolBelt);
             tools[selectedSlot] = tool;
             this.refreshTools(toolBelt,
                               tools);
         }
     }
 
     private void refreshTools(ItemStack toolBelt, ItemStack[] tools) {
         NBTTagList toolsTag = new NBTTagList();
 
         for (int i = 0; i < tools.length; ++i) {
             if (tools[i] != null) {
                 NBTTagCompound tagCompound = new NBTTagCompound();
                 tagCompound.setByte(NBTLib.SLOT,
                                     (byte) i);
                 tools[i].writeToNBT(tagCompound);
                 toolsTag.appendTag(tagCompound);
             }
         }
 
         toolBelt.stackTagCompound.setTag(NBTLib.TOOLS,
                                          toolsTag);
     }
 
     private void refreshToolBelt(ItemStack toolBelt) {
         ItemStack tool = this.getSelectedTool(toolBelt);
         this.updateMirroredTags(toolBelt.stackTagCompound,
                                 tool);
     }
 
     private void updateMirroredTags(NBTTagCompound nbttagcompound, ItemStack tool) {
         NBTTagList enchantments = null;
 
         if (tool != null && tool.hasTagCompound()
             && tool.getEnchantmentTagList() != null) {
             enchantments = tool.getEnchantmentTagList();
         }
 
         if (enchantments != null) {
             nbttagcompound.setTag(NBTLib.ENCHANTMENTS,
                                   enchantments);
         } else if (nbttagcompound.hasKey(NBTLib.ENCHANTMENTS)) {
             nbttagcompound.removeTag(NBTLib.ENCHANTMENTS);
         }
 
         if (ItemHelper.isItemInfiTool(tool)) {
             NBTTagCompound tag = tool.getTagCompound().getCompoundTag(TinkersConstructStatic.INFI_TOOL);
             nbttagcompound.setTag(TinkersConstructStatic.INFI_TOOL,
                                   tag);
         } else if (nbttagcompound.hasKey(TinkersConstructStatic.INFI_TOOL)) {
             nbttagcompound.removeTag(TinkersConstructStatic.INFI_TOOL);
         }
     }
 
     public void updateToolInToolBelt(World world, EntityLivingBase entityliving, ItemStack toolBelt, ItemStack tool, ItemStack toolCopy) {
         if (!ItemStack.areItemStacksEqual(tool,
                                           toolCopy)) {
             this.updateToolBelt(world,
                                 entityliving,
                                 toolBelt,
                                 tool);
         }
     }
 
     private void updateToolBelt(World world, EntityLivingBase entityliving, ItemStack toolBelt, ItemStack tool) {
         this.updateToolBelt(toolBelt,
                             tool);
         if (entityliving instanceof EntityPlayer) {
             EntityPlayer entityplayer = (EntityPlayer) entityliving;
             ItemHelper.updateContainerInfo(world,
                                            entityplayer,
                                            toolBelt);
         }
     }
 
     public void toggleMiningMode(World world, EntityLivingBase entitylivingbase, ItemStack itemstack) {
         if (ItemHelper.isToolBelt(itemstack)) {
             boolean mode = !itemstack.getTagCompound().getBoolean(NBTLib.MINING_MODE);
             itemstack.stackTagCompound.setBoolean(NBTLib.MINING_MODE,
                                                   mode);
             PacketLib.sendMiningModeMessage(world,
                                             entitylivingbase,
                                             mode);
         }
     }
 
     public boolean isMiningModeEnabled(ItemStack itemstack) {
         return itemstack.hasTagCompound()
                && itemstack.stackTagCompound.hasKey(NBTLib.MINING_MODE) ? itemstack.stackTagCompound.getBoolean(NBTLib.MINING_MODE) : false;
     }
 
     public boolean doLeftClickBlock(PlayerInteractEvent event) {
         if (event.entityPlayer.isSneaking()) {
             System.out.println("Left Clicked Block");
             return true;
         } else {
             return false;
         }
     }
 
     public boolean doRightClickBlock(PlayerInteractEvent event) {
         if (event.entityPlayer.isSneaking()) {
             System.out.println("Right Clicked Block");
             return true;
         } else {
             return false;
         }
     }
 
     public boolean doRightClickAir(PlayerInteractEvent event) {
         if (event.entityPlayer.isSneaking()) {
             System.out.println("Right Clicked Air");
             return true;
         } else {
             return false;
         }
     }
 
     public ItemStack getToolInSlot(ItemStack toolBelt, int selectedTool) {
         if (selectedTool >= 0 && selectedTool < 4) {
             ItemStack[] miningTools = ItemHelper.getTools(toolBelt);
             return miningTools[selectedTool];
         } else {
             return null;
         }
     }
 
     public int getSelectedSlot(ItemStack itemstack) {
         if (ItemHelper.isToolBelt(itemstack)) {
             int selectedSlot = NBTHelper.getTagInteger(itemstack,
                                                        NBTLib.SELECTED_TOOL,
                                                        0);
             return selectedSlot;
         } else {
             return 0;
         }
     }
 
     public ItemStack getSelectedTool(ItemStack itemstack) {
         return ItemHelper.isToolBelt(itemstack) ? this.getToolInSlot(itemstack,
                                                                      this.getSelectedSlot(itemstack)) : null;
     }
 
     private ItemStack setSelectedTool(ItemStack toolBelt, int selectedTool) {
         if (ItemHelper.isToolBelt(toolBelt) && toolBelt.hasTagCompound()) {
             toolBelt.stackTagCompound.setInteger(NBTLib.SELECTED_TOOL,
                                                  selectedTool);
             this.refreshToolBelt(toolBelt);
         }
 
         return this.getSelectedTool(toolBelt);
     }
 
     public ItemStack cycleTool(ItemStack itemstack, int direction) {
         if (ItemHelper.isToolBelt(itemstack)) {
             int selectedTool = this.getSelectedSlot(itemstack);
 
             if (direction == CommandLib.CYCLE_TOOLBELT_UP) {
                 selectedTool = this.cycleToolUp(selectedTool);
             }
 
             if (direction == CommandLib.CYCLE_TOOLBELT_DOWN) {
                 selectedTool = this.cycleToolDown(selectedTool);
             }
 
             return this.setSelectedTool(itemstack,
                                         selectedTool);
         } else {
             return null;
         }
     }
 
     private int cycleToolUp(int currentTool) {
         ++currentTool;
 
         if (currentTool > DataLib.TOOL_BELT_SELECTED_MAX) {
             currentTool = 0;
         }
 
         return currentTool;
     }
 
     private int cycleToolDown(int currentTool) {
         --currentTool;
 
         if (currentTool < 0) {
             currentTool = DataLib.TOOL_BELT_SELECTED_MAX;
         }
 
         return currentTool;
     }
 
     @Override
     public String getItemDisplayName(ItemStack itemstack) {
         return this.getToolDisplayName(itemstack);
     }
 
     private String getToolDisplayName(ItemStack itemstack) {
         int selectedSlot = this.getSelectedSlot(itemstack);
         ItemStack tool = ItemHelper.getSelectedTool(itemstack);
         String name = tool != null ? tool.getDisplayName() : "Slot "
                                                              + selectedSlot
                                                              + " - Empty";
         return StatCollector.translateToLocal(this.getUnlocalizedName()
                                               + ".name")
                + " : " + name;
     }
 
     /**
      * allows items to add custom lines of information to the mouseover
      * description
      */
     @Override
     public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List lines, boolean par4) {
         ItemStack[] tools = ItemHelper.getTools(itemstack);
 
         for (int i = 0; i < 4; ++i) {
             ItemStack tool = tools[i];
             String toolString;
 
             if (tool == null) {
                 toolString = StatCollector.translateToLocal(MessageLib.TOOL_SELECTED_NONE);
             } else {
                 toolString = tool.getDisplayName() + " x" + tool.stackSize;
             }
 
             lines.add("Slot [" + i + "]: " + toolString);
         }
     }
 
     /**
      * If this function returns true (or the item is damageable), the
      * ItemStack's NBT tag will be sent to the client.
      */
     @Override
     public boolean getShareTag() {
         return true;
     }
 
     @Override
     public boolean hasEffect(ItemStack itemstack, int pass) {
         ItemStack tool = ItemHelper.getSelectedTool(itemstack);
         return tool != null && tool.getItem() != null ? tool.getItem().hasEffect(tool,
                                                                                  pass) : false;
     }
 
     /**
      * Checks isDamagable and if it cannot be stacked
      */
     @Override
     public boolean isItemTool(ItemStack itemstack) {
         ItemStack tool = ItemHelper.getSelectedTool(itemstack);
         return tool != null && tool.getItem() != null ? tool.getItem().isItemTool(tool) : false;
     }
 
     /**
      * returns the action that specifies what animation to play when the items
      * is being used
      */
     @Override
     public EnumAction getItemUseAction(ItemStack itemstack) {
         ItemStack tool = this.getSelectedTool(itemstack);
         return tool != null ? tool.getItemUseAction() : EnumAction.none;
     }
 
     @Override
     @SideOnly(Side.CLIENT)
     public int getColorFromItemStack(ItemStack itemstack, int pass) {
         ItemStack tool = this.getSelectedTool(itemstack);
         return tool != null && tool.getItem() != null ? tool.getItem().getColorFromItemStack(tool,
                                                                                              pass) : super.getColorFromItemStack(itemstack,
                                                                                                                                  pass);
     }
 
     @Override
     public boolean onEntitySwing(EntityLivingBase entitylivingbase, ItemStack itemstack) {
         ItemStack tool = this.getSelectedTool(itemstack);
         ItemStack toolCopy = ItemStack.copyItemStack(tool);
         if (tool != null && tool.getItem() != null) {
             tool.getItem().onEntitySwing(entitylivingbase,
                                          tool);
             this.updateToolInToolBelt(entitylivingbase.worldObj,
                                       entitylivingbase,
                                       itemstack,
                                       tool,
                                       toolCopy);
         }
 
         return super.onEntitySwing(entitylivingbase,
                                    itemstack);
     }
 
     @Override
     public ItemStack getContainerItemStack(ItemStack itemstack) {
         ItemStack tool = this.getSelectedTool(itemstack);
         return tool != null && tool.getItem() != null ? tool.getItem().getContainerItemStack(tool) : null;
     }
 
     @Override
     public boolean doRepair(ItemStack itemstack, EntityPlayer entityplayer, int level) {
         return ThaumcraftStatic.doRepair(itemstack,
                                          entityplayer,
                                          level);
     }
 }
