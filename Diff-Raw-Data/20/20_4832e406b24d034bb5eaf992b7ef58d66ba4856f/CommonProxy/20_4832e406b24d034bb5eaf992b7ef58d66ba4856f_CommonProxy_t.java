 package ak.MultiToolHolders;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.world.World;
 import ak.MultiToolHolders.Client.GuiToolHolder;
 import cpw.mods.fml.common.network.IGuiHandler;
 
 public class CommonProxy implements IGuiHandler
 {
 	public CommonProxy(){}
 	public void registerClientInformation(){}
 
 	public void registerTileEntitySpecialRenderer(){}
 
 	//returns an instance of the Container you made earlier
 	@Override
 	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
 		if(id == MultiToolHolders.guiIdHolder3)
 		{
 			if(player.getCurrentEquippedItem().getItem() instanceof ItemMultiToolHolder)
 			{
 				ItemStack stack = player.getCurrentEquippedItem();
 				return new ContainerToolHolder(player.inventory, ((ItemMultiToolHolder)stack.getItem()).getData(stack, world), 3);
 			}
 			else
 				return null;
 		}
 		else if(id == MultiToolHolders.guiIdHolder5)
 		{
 			if(player.getCurrentEquippedItem().getItem() instanceof ItemMultiToolHolder)
 			{
 				ItemStack stack = player.getCurrentEquippedItem();
 				return new ContainerToolHolder(player.inventory, ((ItemMultiToolHolder)stack.getItem()).getData(stack, world), 5);
 			}
 			else
 				return null;
 		}
 		else if(id == MultiToolHolders.guiIdHolder7)
 		{
 			if(player.getCurrentEquippedItem().getItem() instanceof ItemMultiToolHolder)
 			{
 				ItemStack stack = player.getCurrentEquippedItem();
 				return new ContainerToolHolder(player.inventory, ((ItemMultiToolHolder)stack.getItem()).getData(stack, world), 7);
 			}
 			else
 				return null;
 		}
 		else if(id == MultiToolHolders.guiIdHolder9)
 		{
 			if(player.getCurrentEquippedItem().getItem() instanceof ItemMultiToolHolder)
 			{
 				ItemStack stack = player.getCurrentEquippedItem();
 				return new ContainerToolHolder(player.inventory, ((ItemMultiToolHolder)stack.getItem()).getData(stack, world), 9);
 			}
 			else
 				return null;
 		}
 		else
 			return null;
 	}
 
 	//returns an instance of the Gui you made earlier
 	@Override
 	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
 		if(id == MultiToolHolders.guiIdHolder3)
 		{
 			if(player.getCurrentEquippedItem().getItem() instanceof ItemMultiToolHolder)
 			{
 				ItemStack stack = player.getCurrentEquippedItem();
 				return new GuiToolHolder(player.inventory, ((ItemMultiToolHolder)stack.getItem()).getData(stack, world), 3);
 			}
 			else
 				return null;
 		}
 		else if(id == MultiToolHolders.guiIdHolder5)
 		{
 			if(player.getCurrentEquippedItem().getItem() instanceof ItemMultiToolHolder)
 			{
 				ItemStack stack = player.getCurrentEquippedItem();
 				return new GuiToolHolder(player.inventory, ((ItemMultiToolHolder)stack.getItem()).getData(stack, world), 5);
 			}
 			else
 				return null;
 		}
		else if(id == MultiToolHolders.guiIdHolder7)
		{
			if(player.getCurrentEquippedItem().getItem() instanceof ItemMultiToolHolder)
			{
				ItemStack stack = player.getCurrentEquippedItem();
				return new GuiToolHolder(player.inventory, ((ItemMultiToolHolder)stack.getItem()).getData(stack, world), 7);
			}
			else
				return null;
		}
 		else if(id == MultiToolHolders.guiIdHolder9)
 		{
 			if(player.getCurrentEquippedItem().getItem() instanceof ItemMultiToolHolder)
 			{
 				ItemStack stack = player.getCurrentEquippedItem();
 				return new GuiToolHolder(player.inventory, ((ItemMultiToolHolder)stack.getItem()).getData(stack, world), 9);
 			}
 			else
 				return null;
 		}
 		else
 			return null;
 	}
 	public World getClientWorld()
 	{
 		return null;
 	}
 }
