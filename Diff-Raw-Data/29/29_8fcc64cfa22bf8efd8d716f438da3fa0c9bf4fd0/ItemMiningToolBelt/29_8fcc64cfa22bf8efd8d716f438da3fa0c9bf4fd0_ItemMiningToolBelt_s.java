 /*
  * This program is free software: you can redistribute it and/or modify it under
  * the terms of the GNU Lesser General Public License as published by the Free
  * Software Foundation, either version 3 of the License, or (at your option) any
  * later version. This program is distributed in the hope that it will be
  * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
  * Public License for more details. You should have received a copy of the GNU
  * Lesser General Public License along with this program. If not, see
  * <http://www.gnu.org/licenses/>
  */
 package slimevoid.tmf.items.tools;
 
 import java.util.List;
 
 import net.minecraft.block.Block;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.EntityLivingBase;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.EnumAction;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.world.World;
 import net.minecraftforge.event.entity.player.EntityInteractEvent;
 import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
 import net.minecraftforge.event.entity.player.PlayerEvent.HarvestCheck;
 import net.minecraftforge.event.entity.player.PlayerInteractEvent;
 import slimevoid.tmf.core.TheMinersFriend;
 import slimevoid.tmf.core.helpers.ItemHelper;
 import slimevoid.tmf.core.lib.CommandLib;
 import slimevoid.tmf.core.lib.DataLib;
 import slimevoid.tmf.core.lib.GuiLib;
 import slimevoid.tmf.core.lib.NBTLib;
 import slimevoid.tmf.items.ItemTMF;
 import slimevoid.tmf.items.tools.inventory.InventoryMiningToolBelt;
 import slimevoidlib.nbt.NBTHelper;
 import thaumcraft.api.IRepairable;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class ItemMiningToolBelt extends ItemTMF implements IRepairable {
 
     public ItemMiningToolBelt(int itemID) {
         super(itemID);
         this.setMaxStackSize(1);
         this.setFull3D();
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
         if (ItemHelper.isToolBelt(itemstack)) {
             ItemStack tool = getSelectedTool(itemstack);
             if (tool != null) {
                 return tool.getItem().itemInteractionForEntity(tool,
                                                                entityplayer,
                                                                entitylivingbase);
             }
         }
         return super.itemInteractionForEntity(itemstack,
                                               entityplayer,
                                               entitylivingbase);
     }
 
     @Override
     public boolean isDamaged(ItemStack itemstack) {
         return this.isToolDamaged(itemstack);
     }
 
     protected boolean isToolDamaged(ItemStack itemstack) {
         // Retrieves the Selected Tool within the held Tool Belt
         ItemStack tool = ItemHelper.getSelectedTool(itemstack);
         if (tool != null) {
             return tool.isItemDamaged();
         }
         return false;
     }
 
     @Override
     public int getMaxDamage(ItemStack itemstack) {
         return this.getMaxToolDamage(itemstack);
     }
 
     protected int getMaxToolDamage(ItemStack itemstack) {
         // Retrieves the Selected Tool within the held Tool Belt
         ItemStack tool = ItemHelper.getSelectedTool(itemstack);
         if (tool != null) {
             return tool.getMaxDamage();
         }
         return 0;
     }
 
     @Override
     public int getDamage(ItemStack itemstack) {
         ItemStack tool = ItemHelper.getSelectedTool(itemstack);
         if (tool != null) {
             return tool.getItemDamage();
         }
         return super.getDamage(itemstack);
     }
 
     @Override
     public int getDisplayDamage(ItemStack itemstack) {
         return this.getToolDisplayDamage(itemstack);
     }
 
     @Override
     public String getItemStackDisplayName(ItemStack itemstack) {
         ItemStack tool = this.getSelectedTool(itemstack);
         if (tool != null && tool.getItem() != null) {
             return tool.getItem().getItemStackDisplayName(tool);
         }
         return super.getItemStackDisplayName(itemstack);
     }
 
     protected int getToolDisplayDamage(ItemStack itemstack) {
         // Retrieves the Selected Tool within the held Tool Belt
         ItemStack tool = ItemHelper.getSelectedTool(itemstack);
         if (tool != null) {
             return tool.getItemDamage();
         }
         return 0;
     }
 
     @Override
     public void setDamage(ItemStack itemstack, int damage) {
         this.setToolDamage(itemstack,
                            damage);
     }
 
     protected void setToolDamage(ItemStack itemstack, int damage) {
         ItemStack tool = getSelectedTool(itemstack);
         if (tool != null) {
             tool.setItemDamage(damage);
             updateToolBelt(itemstack,
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
             ItemStack tool = getSelectedTool(itemstack);
             if (tool != null) {
                 if (this.doTickTool(tool,
                                     world,
                                     entity,
                                     tick,
                                     isHeld)) {
                     this.updateToolBelt(world,
                                         (EntityLivingBase) entity,
                                         itemstack,
                                         tool);
                 }
             }
         }
     }
 
     protected boolean doTickTool(ItemStack tool, World world, Entity entity, int tick, boolean isHeld) {
         if (tool != null && tool.getItem() != null) {
             tool.getItem().onUpdate(tool,
                                     world,
                                     entity,
                                     tick,
                                     isHeld);
             return true;
         }
         return false;
     }
 
     @Override
     public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityplayer) {
         if (entityplayer.isSneaking()) {
             entityplayer.openGui(TheMinersFriend.instance,
                                  GuiLib.GUIID_TOOL_BELT,
                                  world,
                                  (int) entityplayer.posX,
                                  (int) entityplayer.posY,
                                  (int) entityplayer.posZ);
         } else {
             doItemRightClick(itemstack,
                              world,
                              entityplayer);
         }
         return itemstack;
     }
 
     @Override
     public boolean onItemUse(ItemStack itemstack, EntityPlayer entityplayer, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
         return doItemUse(itemstack,
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
         return doItemUseFirst(itemstack,
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
         doFoodEaten(itemstack,
                     world,
                     entityplayer);
         return itemstack;
     }
 
     @Override
     public int getMaxItemUseDuration(ItemStack itemstack) {
         ItemStack tool = getSelectedTool(itemstack);
         if (tool != null) {
             return tool.getMaxItemUseDuration();
         }
         return super.getMaxItemUseDuration(itemstack);
     }
 
     public void doItemRightClick(ItemStack itemstack, World world, EntityPlayer entityplayer) {
         // Retrieves the Selected Tool within the held Tool Belt
         ItemStack tool = getSelectedTool(itemstack);
         if (tool != null) {
             tool.useItemRightClick(world,
                                    entityplayer);
            if (ItemStack.areItemStacksEqual(entityplayer.getItemInUse(),
                                             tool)) {
                 entityplayer.setItemInUse(itemstack,
                                           tool.getMaxItemUseDuration());
             }
             updateToolBelt(world,
                            entityplayer,
                            itemstack,
                            tool);
         }
     }
 
     public boolean doItemUse(ItemStack itemstack, EntityPlayer entityplayer, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
         // Retrieves the Selected Tool within the held Tool Belt
         ItemStack tool = getSelectedTool(itemstack);
         if (tool != null && tool.getItem() != null) {
             if (tool.getItem().onItemUse(tool,
                                          entityplayer,
                                          world,
                                          x,
                                          y,
                                          z,
                                          side,
                                          hitX,
                                          hitY,
                                          hitZ)) {
                 updateToolBelt(world,
                                entityplayer,
                                itemstack,
                                tool);
                 return true;
             }
         }
         return false;
     }
 
     public boolean doItemUseFirst(ItemStack itemstack, EntityPlayer entityplayer, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
         // Retrieves the Selected Tool within the held Tool Belt
         ItemStack tool = getSelectedTool(itemstack);
         if (tool != null) {
             if (tool.getItem().onItemUseFirst(tool,
                                               entityplayer,
                                               world,
                                               x,
                                               y,
                                               z,
                                               side,
                                               hitX,
                                               hitY,
                                               hitZ)) {
                 updateToolBelt(world,
                                entityplayer,
                                itemstack,
                                tool);
                 return true;
             }
         }
         return false;
     }
 
     public void doFoodEaten(ItemStack itemstack, World world, EntityPlayer entityplayer) {
         // Retrieves the Selected Tool within the held Tool Belt
         ItemStack tool = getSelectedTool(itemstack);
         if (tool != null) {
             updateToolBelt(world,
                            entityplayer,
                            itemstack,
                            tool.onFoodEaten(world,
                                             entityplayer));
         }
     }
 
     @Override
     public void onUsingItemTick(ItemStack itemstack, EntityPlayer entityplayer, int count) {
         ItemStack tool = this.getSelectedTool(itemstack);
         if (tool != null && tool.getItem() != null) {
             tool.getItem().onUsingItemTick(tool,
                                            entityplayer,
                                            count);
             this.updateToolBelt(entityplayer.worldObj,
                                 entityplayer,
                                 itemstack,
                                 tool);
         }
     }
 
     public void onPlayerStoppedUsing(ItemStack itemstack, World world, EntityPlayer entityplayer, int count) {
         ItemStack tool = this.getSelectedTool(itemstack);
         if (tool != null && tool.getItem() != null) {
             tool.getItem().onPlayerStoppedUsing(tool,
                                                 world,
                                                 entityplayer,
                                                 count);
             this.updateToolBelt(world,
                                 entityplayer,
                                 itemstack,
                                 tool);
         }
     }
 
     @Override
     public boolean onLeftClickEntity(ItemStack itemstack, EntityPlayer entityplayer, Entity entity) {
         return this.doLeftClickEntity(itemstack,
                                       entityplayer,
                                       entity);
     }
 
     private boolean doLeftClickEntity(ItemStack itemstack, EntityPlayer entityplayer, Entity entity) {
         // Retrieves the Selected Tool within the held Tool Belt
         ItemStack tool = getSelectedTool(itemstack);
         boolean onLeftClickEntity = false;
         if (tool != null) {
             // Perform the onLeftClickEntity method for the itemstack
             onLeftClickEntity = tool.getItem().onLeftClickEntity(tool,
                                                                  entityplayer,
                                                                  entity);
             updateToolBelt(entityplayer.worldObj,
                            entityplayer,
                            itemstack,
                            tool);
         }
         // Otherwise return the original value
         return onLeftClickEntity;
     }
 
     @Override
     public boolean hitEntity(ItemStack stack, EntityLivingBase mob, EntityLivingBase player) {
         return this.doHitEntity(stack,
                                 mob,
                                 player);
     }
 
     private boolean doHitEntity(ItemStack itemstack, EntityLivingBase mob, EntityLivingBase entityplayer) {
         // Retrieves the Selected Tool within the held Tool Belt
         ItemStack tool = getSelectedTool(itemstack);
         boolean hitEntity = false;
         if (tool != null && tool.getItem() != null) {
             // Perform the hitEntity method for the itemstack
             hitEntity = tool.getItem().hitEntity(tool,
                                                  mob,
                                                  entityplayer);
             updateToolBelt(entityplayer.worldObj,
                            entityplayer,
                            itemstack,
                            tool);
         }
         // Otherwise return the original value
         return hitEntity;
     }
 
     @Override
     public boolean onBlockDestroyed(ItemStack itemstack, World world, int x, int y, int z, int side, EntityLivingBase entityliving) {
         return doDestroyBlock(itemstack,
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
         return doStartBreakBlock(itemstack,
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
         // Retrieves the Selected Tool within the held Tool Belt
         ItemStack tool = getSelectedTool(itemstack);
         if (tool != null && tool.getItem() != null) {
             // Perform the onBlockStartBreak method for the itemstack
             onBlockStartBreak = tool.getItem().onBlockStartBreak(tool,
                                                                  x,
                                                                  y,
                                                                  z,
                                                                  entityplayer);
             updateToolBelt(entityplayer.worldObj,
                            entityplayer,
                            itemstack,
                            tool);
         }
         // Otherwise return the original value
         return onBlockStartBreak;
     }
 
     /**
      * Attempts to destroy the block using the selected item in the Tool Belt
      * 
      * @param itemstack
      *            the Held Item
      * @param world
      *            the World
      * @param x
      * @param y
      * @param z
      * @param side
      * @param entityliving
      *            (Usually the player)
      * @param onBlockDestroyed
      *            the current result
      * @return
      */
     public boolean doDestroyBlock(ItemStack itemstack, World world, int x, int y, int z, int side, EntityLivingBase entityliving, boolean onBlockDestroyed) {
         // Retrieves the Selected Tool within the held Tool Belt
         ItemStack tool = getSelectedTool(itemstack);
         if (tool != null && tool.getItem() != null) {
             // Perform the onBlockDestroyed method for the itemstack
             onBlockDestroyed = tool.getItem().onBlockDestroyed(tool,
                                                                world,
                                                                x,
                                                                y,
                                                                z,
                                                                side,
                                                                entityliving);
             updateToolBelt(world,
                            entityliving,
                            itemstack,
                            tool);
         }
         // Otherwise return the original value
         return onBlockDestroyed;
     }
 
     @Override
     public float getStrVsBlock(ItemStack itemstack, Block block, int meta) {
         // Retrieves the Selected Tool within the held Tool Belt
         ItemStack tool = ItemHelper.getSelectedTool(itemstack);
         float strVsBlock = 1.0f;
         if (tool != null && tool.getItem() != null) {
             // Perform the onBlockStartBreak method for the itemstack
             strVsBlock = tool.getItem().getStrVsBlock(tool,
                                                       block,
                                                       meta);
         }
         // Otherwise return the original value
         return strVsBlock;
     }
 
     private void updateToolBelt(ItemStack toolBelt, ItemStack tool) {
         InventoryMiningToolBelt data = new InventoryMiningToolBelt(toolBelt);
         if (tool.stackSize == 0) {
             tool = null;
         }
         data.setInventorySlotContents(data.getSelectedSlot(),
                                       tool);
         updateToolBeltData(toolBelt,
                            data);
     }
 
     private void updateToolBelt(World world, EntityLivingBase entityliving, ItemStack toolBelt, ItemStack tool) {
         InventoryMiningToolBelt data = new InventoryMiningToolBelt(world, entityliving, toolBelt);
         if (tool.stackSize == 0) {
             tool = null;
         }
         data.setInventorySlotContents(data.getSelectedSlot(),
                                       tool);
         updateToolBeltData(toolBelt,
                            data);
     }
 
     private void updateToolBeltData(ItemStack toolBelt, InventoryMiningToolBelt data) {
         toolBelt.stackTagCompound = new NBTTagCompound();
         data.writeToNBT(toolBelt.stackTagCompound);
     }
 
     public void toggleMiningMode(World world, EntityLivingBase entitylivingbase, ItemStack itemstack) {
         if (ItemHelper.isToolBelt(itemstack)) {
             InventoryMiningToolBelt data = new InventoryMiningToolBelt(world, entitylivingbase, itemstack);
             data.toggleMiningMode();
             updateToolBeltData(itemstack,
                                data);
         }
     }
 
     private boolean isMiningModeEnabled(ItemStack itemstack) {
         if (itemstack.hasTagCompound()) {
             if (itemstack.stackTagCompound.hasKey(NBTLib.MINING_MODE)) {
                 return itemstack.stackTagCompound.getBoolean(NBTLib.MINING_MODE);
             }
         }
         return false;
     }
 
     /**
      * This performs the interrupted breakSpeed event from Forge Only activates
      * if the player is holding a tool belt Used to retrieve a new speed based
      * on Tool Belt tool in use
      * 
      * @param event
      *            The event to use
      * @param heldItem
      */
     public void doBreakSpeed(BreakSpeed event, ItemStack heldItem) {
         // If the player is still holding the tool belt
         if (ItemHelper.isToolBelt(heldItem)) {
             // Retrieves the Tool Belt data
             ItemStack selectedStack = null;
             // Checks if the player is in Mining Mode
             // If true then auto select tool for the best STR vs Block
             // Otherwise return our selected tool
             if (isMiningModeEnabled(heldItem)) {
                 selectedStack = selectToolForBlock(event.entityLiving.worldObj,
                                                    event.entityLiving,
                                                    heldItem,
                                                    event.block,
                                                    event.metadata,
                                                    event.originalSpeed);
             } else {
                 selectedStack = getSelectedTool(heldItem);
             }
             // If an item exists in the selected slot of the Tool Belt
             if (selectedStack != null && selectedStack.getItem() != null) {
                 // Generate break speed for that Tool vs. Block
                 float newSpeed = (selectedStack.getItem().getStrVsBlock(selectedStack,
                                                                         event.block,
                                                                         event.metadata))
                                  * DataLib.MINING_MODE_STRENGTH;
                 // If the new speed is greater than the speed being parsed in
                 // the event then set the new speed
                 event.newSpeed = newSpeed > event.originalSpeed ? newSpeed : event.originalSpeed;
             }
         }
     }
 
     public ItemStack selectToolForBlock(World world, EntityLivingBase entitylivingbase, ItemStack toolBelt, Block block, int metadata, float currentBreakSpeed) {
         float fastestSpeed = currentBreakSpeed;
         int selection = getSelectedSlot(toolBelt);
         InventoryMiningToolBelt data = new InventoryMiningToolBelt(world, entitylivingbase, toolBelt);
         ItemStack[] tools = data.getTools();
         if (tools != null) {
             for (int i = 0; i <= DataLib.TOOL_BELT_SELECTED_MAX; i++) {
                 ItemStack itemstack = tools[i];
                 if (itemstack != null && itemstack.getItem() != null) {
                     float breakSpeed = itemstack.getItem().getStrVsBlock(itemstack,
                                                                          block,
                                                                          metadata);
                     if (breakSpeed > fastestSpeed) {
                         fastestSpeed = breakSpeed;
                         selection = i;
                     }
                 }
             }
         }
         return setSelectedTool(toolBelt,
                                selection);
     }
 
     /**
      * This performs the harvesting check event from Forge Activates when the
      * player has successfully mined a block Used to correctly determine whether
      * or not the tool in slot will harvest the block broken
      * 
      * @param event
      *            The harvesting event
      * @param heldItem
      */
     public void doHarvestCheck(HarvestCheck event, ItemStack heldItem) {
         // Retrieves the Selected Tool within the held Tool Belt
         ItemStack tool = getSelectedTool(heldItem);
         if (tool != null) {
             // Run a harvest check on that Tool and set the result
             event.success = tool.canHarvestBlock(event.block);
         }
     }
 
     /**
      * This performs the entity interact event from Forge Activates when the
      * player interacts (Right clicks) with an entity Used to correctly
      * determine whether or not the tool in the slot can interact with the
      * entity (Should only allow use when the player is sneaking)
      * 
      * @param event
      *            The Interaction Event
      * @param heldItem
      * 
      * @return whether or not we interacted with the entity Thus whether or not
      *         to continue processing the 'normal' interaction
      */
     public boolean doEntityInteract(EntityInteractEvent event, ItemStack heldItem) {
         boolean flag = false;
         if (ItemHelper.isToolBelt(heldItem)) {
             // First checks if the player is sneaking
             if (event.entityPlayer.isSneaking()) {
                 // Retrieves the Selected Tool within the held Tool Belt
                 ItemStack tool = getSelectedTool(heldItem);
                 if (tool != null) {
                     flag = tool.func_111282_a(event.entityPlayer,
                                               (EntityLivingBase) event.target);// .interactWith(event.target);
                     updateToolBelt(event.entityPlayer.worldObj,
                                    event.entityPlayer,
                                    heldItem,
                                    tool);
                 }
             }
         }
         return flag;
     }
 
     public boolean doLeftClickBlock(PlayerInteractEvent event) {
         if (event.entityPlayer.isSneaking()) {
             System.out.println("Left Clicked Block");
             return true;
         }
         return false;
     }
 
     public boolean doRightClickBlock(PlayerInteractEvent event) {
         if (event.entityPlayer.isSneaking()) {
             System.out.println("Right Clicked Block");
             return true;
         }
         return false;
     }
 
     public boolean doRightClickAir(PlayerInteractEvent event) {
         if (event.entityPlayer.isSneaking()) {
             System.out.println("Right Clicked Air");
             return true;
         }
         return false;
     }
 
     public ItemStack getToolInSlot(ItemStack toolBelt, int selectedTool) {
         if (selectedTool >= 0 && selectedTool < DataLib.TOOL_BELT_MAX_SIZE) {
             ItemStack[] miningTools = ItemHelper.getTools(toolBelt);
             return miningTools[selectedTool];
         }
         return null;
     }
 
     private int getSelectedSlot(ItemStack itemstack) {
         if (ItemHelper.isToolBelt(itemstack)) {
             int selectedTool = NBTHelper.getTagInteger(itemstack,
                                                        NBTLib.SELECTED_TOOL,
                                                        0);
             return selectedTool;
         }
         return 0;
     }
 
     public ItemStack getSelectedTool(ItemStack itemstack) {
         if (ItemHelper.isToolBelt(itemstack)) {
             return getToolInSlot(itemstack,
                                  getSelectedSlot(itemstack));
         }
         return null;
     }
 
     private ItemStack setSelectedTool(ItemStack toolBelt, int selectedTool) {
         if (ItemHelper.isToolBelt(toolBelt)) {
             if (toolBelt.hasTagCompound()) {
                 toolBelt.stackTagCompound.setInteger(NBTLib.SELECTED_TOOL,
                                                      selectedTool);
             }
         }
         return this.getSelectedTool(toolBelt);
     }
 
     public ItemStack cycleTool(ItemStack itemstack, int direction) {
         if (ItemHelper.isToolBelt(itemstack)) {
             int selectedTool = this.getSelectedSlot(itemstack);
             if (direction == CommandLib.CYCLE_TOOLBELT_UP) {
                 selectedTool = cycleToolUp(selectedTool);
             }
             if (direction == CommandLib.CYCLE_TOOLBELT_DOWN) {
                 selectedTool = cycleToolDown(selectedTool);
             }
             return this.setSelectedTool(itemstack,
                                         selectedTool);
         }
         return null;
     }
 
     private int cycleToolUp(int currentTool) {
         currentTool++;
         if (currentTool > DataLib.TOOL_BELT_SELECTED_MAX) {
             currentTool = 0;
         }
         return currentTool;
     }
 
     private int cycleToolDown(int currentTool) {
         currentTool--;
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
         int selectedSlot = getSelectedSlot(itemstack);
         ItemStack tool = ItemHelper.getSelectedTool(itemstack);
         String name = tool != null ? tool.getDisplayName() : "Slot "
                                                              + selectedSlot
                                                              + " - Empty";
         return "ToolBelt : " + name;
     }
 
     @Override
     public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List lines, boolean par4) {
         ItemStack[] tools = ItemHelper.getTools(itemstack);
         for (int i = 0; i < DataLib.TOOL_BELT_MAX_SIZE; i++) {
             ItemStack tool = tools[i];
             String toolString;
             if (tool == null) {
                 toolString = "None Equipped";
             } else {
                 toolString = tool.getDisplayName() + " x" + tool.stackSize;
             }
             lines.add("Slot [" + i + "]: " + toolString);
         }
     }
 
     @Override
     public boolean getShareTag() {
         return true;
     }
 
     @Override
     public boolean hasEffect(ItemStack itemstack, int pass) {
         ItemStack tool = ItemHelper.getSelectedTool(itemstack);
         if (tool != null && tool.getItem() != null) {
             return tool.getItem().hasEffect(tool,
                                             pass);
         }
         return false;
     }
 
     @Override
     public boolean isItemTool(ItemStack itemstack) {
         ItemStack tool = ItemHelper.getSelectedTool(itemstack);
         if (tool != null && tool.getItem() != null) {
             return tool.getItem().isItemTool(tool);
         }
         return false;
     }
 
     @Override
     public EnumAction getItemUseAction(ItemStack itemstack) {
         ItemStack tool = this.getSelectedTool(itemstack);
         if (tool != null) {
             return tool.getItemUseAction();
         }
         return EnumAction.none;
     }
 
     @SideOnly(Side.CLIENT)
     public int getColorFromItemStack(ItemStack itemstack, int pass) {
         if (ItemHelper.isToolBelt(itemstack)) {
             ItemStack tool = getSelectedTool(itemstack);
             if (tool != null && tool.getItem() != null) {
                 return tool.getItem().getColorFromItemStack(tool,
                                                             pass);
             }
         }
         return super.getColorFromItemStack(itemstack,
                                            pass);
     }
 
     @Override
     public boolean onEntitySwing(EntityLivingBase entitylivingbase, ItemStack itemstack) {
         ItemStack tool = ItemHelper.getSelectedTool(itemstack);
         if (tool != null && tool.getItem() != null) {
             return tool.getItem().onEntitySwing(entitylivingbase,
                                                 tool);
         }
         return super.onEntitySwing(entitylivingbase,
                                    itemstack);
     }
 
     @Override
     public ItemStack getContainerItemStack(ItemStack itemstack) {
         ItemStack tool = this.getSelectedTool(itemstack);
         if (tool != null && tool.getItem() != null) {
             return tool.getItem().getContainerItemStack(tool);
         }
         return null;
     }
 }
