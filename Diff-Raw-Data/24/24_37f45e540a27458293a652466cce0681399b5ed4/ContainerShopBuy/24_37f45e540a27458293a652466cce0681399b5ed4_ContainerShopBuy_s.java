 package hunternif.mc.dota2items.inventory;
 
 import hunternif.mc.dota2items.Dota2Items;
 import hunternif.mc.dota2items.config.Config;
 import hunternif.mc.dota2items.core.EntityStats;
 import hunternif.mc.dota2items.item.Dota2Item;
 import hunternif.mc.dota2items.item.ItemRecipe;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.InventoryPlayer;
 import net.minecraft.inventory.Container;
 import net.minecraft.inventory.InventoryBasic;
 import net.minecraft.inventory.Slot;
 import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemStack;
 
 public class ContainerShopBuy extends Container {
 	private static final int SHOP_INV_X = 8;
 	private static final int SHOP_INV_Y = 60;
 	private static final int PLAYER_INV_X = 35;
 	private static final int PLAYER_INV_Y = 215;
 	
 	public InventoryShop invShop = InventoryShop.newFullShop(4);
 	private InventoryBasic invResult = new InventoryBasic("Buying", false, 1);
 	private InventoryPlayer invPlayer;
 	private int slotResultNumber;
 	
 	private ItemStack recipeResultItem;
 	private List<ItemStack> recipeIngredients;
 	
 	public ContainerShopBuy(InventoryPlayer inventoryPlayer) {
 		this.invPlayer = inventoryPlayer;
 		// Assign shopkeeper's inventory
 		for (int i = 0; i < invShop.getRows(); i++) {
 			for (int j = 0; j < invShop.getColumns(); j++) {
 				addSlotToContainer(new SlotShop(invShop, i * invShop.getColumns() + j, SHOP_INV_X + j * 18, SHOP_INV_Y + i * 18));
 			}
 		}
 		// Assign player's hotbar
 		for (int i = 0; i < 9; ++i) {
 			addSlotToContainer(new Slot(inventoryPlayer, i, PLAYER_INV_X + i * 18, PLAYER_INV_Y));
 		}
 		slotResultNumber = addSlotToContainer(new SlotShopBuyResult(invResult, 0, 160, 175)).slotNumber;
 	}
 
 	@Override
 	public boolean canInteractWith(EntityPlayer entityplayer) {
 		return true;
 	}
 	
 	public SlotShopBuyResult getSlotResult() {
 		return (SlotShopBuyResult)inventorySlots.get(slotResultNumber);
 	}
 	
 	@Override
 	public ItemStack slotClick(int slotNumber, int mouseClick, int holdShift, EntityPlayer player) {
 		if (slotNumber != slotResultNumber && slotNumber >= 0) {
 			Slot slot = (Slot)this.inventorySlots.get(slotNumber);
 			if (slot.inventory instanceof InventoryShop) {
 				ItemStack stack = invShop.getStackInSlot(slotNumber);
 				if (stack != null) {
 					Dota2Item item = (Dota2Item) stack.getItem();
 					setRecipeResultItem(item);
 					EntityStats stats = Dota2Items.mechanics.getEntityStats(player);
 					if (stats.getGold() >= item.getTotalPrice()) {
 						setResultItem(item);
 					} else {
 						setResultItem((Dota2Item)null);
 					}
 				} else {
 					setRecipeResultItem(null);
 					setResultItem((Dota2Item)null);
 				}
 				return null;
 			}
 		}
 		return super.slotClick(slotNumber, mouseClick, holdShift, player);
 	}
 	
 	public ItemStack getRecipeResultItem() {
 		return recipeResultItem;
 	}
 	public List<ItemStack> getRecipeIngredients() {
 		return recipeIngredients;
 	}
 	public void setRecipeResultItem(Dota2Item item) {
 		if (item != null) {
			recipeResultItem = new ItemStack(item, item.getDefaultQuantity());
 			if (item.hasRecipe()) {
 				recipeIngredients = new ArrayList<ItemStack>();
 				for (Dota2Item curRecipeItem : item.getRecipe()) {
					recipeIngredients.add(new ItemStack(curRecipeItem, curRecipeItem.getDefaultQuantity()));
 				}
 				if (item.isRecipeItemRequired()) {
 					recipeIngredients.add(ItemRecipe.forItem(item, true));
 				}
 			} else {
 				recipeIngredients = null;
 			}
 		} else {
 			recipeResultItem = null;
 			recipeIngredients = null;
 		}
 	}
 	
 	public ItemStack getResultItem() {
 		return invResult.getStackInSlot(0);
 	}
 	public void setResultItem(Dota2Item item) {
 		if (item != null) {
 			if (invShop.contains(item)) {
 				invResult.setInventorySlotContents(0, new ItemStack(item, ((Dota2Item)item).getDefaultQuantity()));
 			}
 		} else {
 			invResult.setInventorySlotContents(0, null);
 		}
 	}
 	public void setResultItem(ItemStack stack) {
 		if (stack != null) {
 			if (stack.getItem() instanceof Dota2Item) {
 				setResultItem((Dota2Item)stack.getItem());
 			} else if (stack.itemID == Config.recipe.getID()) {
 				invResult.setInventorySlotContents(0, ItemRecipe.copy(stack, false));
 			}
 		} else {
 			invResult.setInventorySlotContents(0, null);
 		}
 	}
 	
 	@Override
 	public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par2) {
 		return null;
 	}
 }
