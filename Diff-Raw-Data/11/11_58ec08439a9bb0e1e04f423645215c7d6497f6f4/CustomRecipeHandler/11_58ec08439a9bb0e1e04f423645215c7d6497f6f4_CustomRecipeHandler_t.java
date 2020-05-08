 package no.runsafe.ItemControl;
 
 import no.runsafe.framework.RunsafePlugin;
 import no.runsafe.framework.api.event.IServerReady;
 import no.runsafe.framework.api.event.inventory.IInventoryClick;
 import no.runsafe.framework.api.item.ICustomRecipe;
 import no.runsafe.framework.api.log.IConsole;
 import no.runsafe.framework.minecraft.Item;
 import no.runsafe.framework.minecraft.event.inventory.RunsafeInventoryClickEvent;
 import no.runsafe.framework.minecraft.inventory.RunsafeInventory;
 import no.runsafe.framework.minecraft.inventory.RunsafeInventoryType;
 import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 public class CustomRecipeHandler implements IServerReady, IInventoryClick
 {
 	public CustomRecipeHandler(IConsole console)
 	{
 		this.console = console;
 	}
 
 	@Override
 	public void OnServerReady()
 	{
 		recipes.addAll(RunsafePlugin.getPluginAPI(ICustomRecipe.class));
 		console.logInformation("Loaded " + recipes.size() + " custom recipes");
 	}
 
 	@Override
 	public void OnInventoryClickEvent(RunsafeInventoryClickEvent event)
 	{
 		RunsafeInventory inventory = event.getInventory();
 		if (inventory.getType() == RunsafeInventoryType.WORKBENCH)
 		{
 			List<RunsafeMeta> items = new ArrayList<RunsafeMeta>(0);
 			for (int i = 1; i < inventory.getSize(); i++)
 			{
 				RunsafeMeta slotItem = inventory.getItemInSlot(i);
 				if (slotItem != null)
 					items.add(i, slotItem);
 			}
 
 			RunsafeMeta cursorItem = event.getCurrentItem();
 			if (cursorItem != null && !cursorItem.is(Item.Unavailable.Air))
 				items.add(event.getSlot(), cursorItem);
 
 			checkRecipes(items, inventory);
 		}
 	}
 
 	private void checkRecipes(List<RunsafeMeta> workbench, RunsafeInventory inventory)
 	{
 		for (ICustomRecipe recipe : recipes)
 		{
			boolean failed = false;
 			for (Map.Entry<Integer, RunsafeMeta> node : recipe.getRecipe().entrySet())
 			{
 				RunsafeMeta workbenchItem = workbench.get(node.getKey());
 				if (workbenchItem == null || !workbenchItem.equals(node.getValue()))
				{
					failed = true;
					break;
				}
			}
 
			if (!failed)
			{
 				inventory.setItemInSlot(recipe.getResult(), 0);
 				break;
 			}
 		}
 	}
 
 	private final IConsole console;
 	private final List<ICustomRecipe> recipes = new ArrayList<ICustomRecipe>(0);
 }
