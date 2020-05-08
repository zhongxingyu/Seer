 package emasher.sockets.modules;
 
 import java.util.List;
 
 import cpw.mods.fml.common.Loader;
 import cpw.mods.fml.common.registry.GameRegistry;
 import net.minecraft.block.Block;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.CraftingManager;
 import net.minecraft.item.crafting.FurnaceRecipes;
 import net.minecraft.util.EnumChatFormatting;
 import net.minecraftforge.common.ForgeDirection;
 import net.minecraftforge.oredict.OreDictionary;
 import net.minecraftforge.oredict.ShapedOreRecipe;
 import emasher.api.CentrifugeRecipeRegistry;
 import emasher.api.SideConfig;
 import emasher.api.SocketModule;
 import emasher.api.SocketTileAccess;
 import emasher.api.CentrifugeRecipeRegistry.CentrifugeRecipe;
 import emasher.core.EmasherCore;
 import emasher.sockets.PacketHandler;
 import emasher.sockets.SocketsMod;
 
 public class ModCentrifuge extends SocketModule
 {
 
 	public ModCentrifuge(int id)
 	{
 		super(id, "sockets:centrifuge", "sockets:centrifuge_active");
 	}
 
 	@Override
 	public String getLocalizedName()
 	{
 		return "Centrifuge";
 	}
 	
 	@Override
 	public void addRecipe()
 	{	
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(SocketsMod.module, 1, moduleID), "tpt", "tct", "gbg", Character.valueOf('t'), "ingotTin", Character.valueOf('p'), EmasherCore.psu,
 				Character.valueOf('c'), "ingotCopper", Character.valueOf('g'), Item.ghastTear, Character.valueOf('b'), SocketsMod.blankSide));
 	}
 	
 	@Override
 	public void getToolTip(List l)
 	{
 		l.add("Used to purify impure metal dusts");
 		l.add("Sometimes gives bonus pure dusts");
 	}
 	
 	@Override
 	public void getIndicatorKey(List l)
 	{
 		l.add(SocketsMod.PREF_GREEN + "Input Inventory");
 		l.add(SocketsMod.PREF_YELLOW + "Outputs to Machine Output");
 		l.add(SocketsMod.PREF_AQUA + "Requires 2 MJ/tick");
 		l.add("Cannot be installed on a socket with other machines");
 	}
 	
 	@Override
 	public boolean hasInventoryIndicator() { return true; }
 	
 	@Override
 	public boolean isMachine() { return true; }
 	
 	@Override
 	public boolean canBeInstalled(SocketTileAccess ts, ForgeDirection side)
 	{
 		for(int i = 0; i < 6; i++)
 		{
 			SocketModule m = ts.getSide(ForgeDirection.getOrientation(i));
 			if(m != null && m.isMachine()) return false;
 		}
 		
 		return true;
 	}
 	
 	@Override
 	public void onRemoved(SocketTileAccess ts, SideConfig config, ForgeDirection side)
 	{
 		ts.sideInventory.setInventorySlotContents(side.ordinal(), null);
 	}
 	
 	@Override
 	public void updateSide(SideConfig config, SocketTileAccess ts, ForgeDirection side)
 	{
 		boolean updateClient = false;
 		if(config.inventory >= 0 && config.inventory <= 2)
 		{
 			if(ts.sideInventory.getStackInSlot(side.ordinal()) == null)
 			{
 				if(ts.getStackInInventorySlot(config.inventory) != null)
 				{
 					ItemStack toIntake = ts.getStackInInventorySlot(config.inventory);
 					
 					ItemStack product = null;
 					
 					CentrifugeRecipe r = CentrifugeRecipeRegistry.getRecipe(toIntake);
 					if(r != null) product = r.getOutput();
 					
 					if(product != null)
 					{
 						ts.sideInventory.setInventorySlotContents(side.ordinal(), ts.extractItemInternal(true, config.inventory, 1));
 						config.meta = 180;
 						config.rsControl[0] = false;
 						config.rsControl[1] = false;
 						config.rsControl[2] = false;
 						updateClient = true;
 					}
 				}
 			}
 			else if(ts.useEnergy(2.0F, false)>= 2.0F && config.meta > 0 && ! config.rsControl[2])
 			{
 				ts.useEnergy(2.0F, true);
 				config.meta--;
 				if(config.meta == 0) updateClient = true;
 				if(! config.rsControl[0] && config.meta > 0)
 				{
 					config.rsControl[0] = true;
 					updateClient = true;
 				}
 			}
 			else
 			{
 				if(config.rsControl[0])
 				{
 					config.rsControl[0] = false;
 					updateClient = true;
 				}
 			}
 			
 			//Output primary
 			if(config.meta == 0 && ts.sideInventory.getStackInSlot(side.ordinal()) != null)
 			{
 				CentrifugeRecipe re = CentrifugeRecipeRegistry.getRecipe(ts.sideInventory.getStackInSlot(side.ordinal()));
 				if(re != null)
 				{
 					int num = ts.forceOutputItem(re.getOutput().copy());
					if(num != 0) config.rsControl[2] = true;
 					else
 					{
 						config.rsControl[2] = false;
 						ts.sideInventory.setInventorySlotContents(side.ordinal(), null);
 					}
 				}
 				
 			}
 			
 			//Output secondary
 			if(config.meta == 60 && (! config.rsControl[1] || config.rsControl[2]) && ts.sideInventory.getStackInSlot(side.ordinal()) != null)
 			{
 				config.rsControl[1] = true;
 				CentrifugeRecipe re = CentrifugeRecipeRegistry.getRecipe(ts.sideInventory.getStackInSlot(side.ordinal()));
 				if(re != null && (re.shouldOuputSecondary(ts.worldObj.rand) || config.rsControl[2]))
 				{
 					int num = ts.forceOutputItem(re.getSecondaryOutput().copy());
					if(num != 0) config.rsControl[2] = true;
 					else config.rsControl[2] = false;
 				}
 			}
 			if(updateClient) ts.sendClientSideState(side.ordinal());
 		}
 	}
 	
 	@Override
 	public int getCurrentTexture(SideConfig config)
 	{
 		if(config.meta == 0 || ! config.rsControl[0]) return 0;
 		return 1;
 	}
 
 }
 
