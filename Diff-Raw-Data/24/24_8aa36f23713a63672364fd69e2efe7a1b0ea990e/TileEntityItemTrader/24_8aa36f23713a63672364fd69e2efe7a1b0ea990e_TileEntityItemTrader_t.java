 package com.github.soniex2.endermoney.trading.tileentity;
 
 import java.math.BigInteger;
 import java.util.HashMap;
 
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.ItemStack;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraftforge.common.ForgeDirection;
 
 import com.github.soniex2.endermoney.trading.base.AbstractTraderTileEntity;
 import com.github.soniex2.endermoney.trading.exception.OutOfInventorySpaceException;
 import com.github.soniex2.endermoney.trading.exception.TradeException;
 import com.github.soniex2.endermoney.trading.helper.inventory.InventoryHelper;
 import com.github.soniex2.endermoney.trading.helper.item.ItemStackMapKey;
 import com.github.soniex2.endermoney.trading.helper.money.MoneyHelper;
 
 public class TileEntityItemTrader extends AbstractTraderTileEntity {
 
 	public TileEntityItemTrader() {
 		super(18);
 	}
 
 	public ItemStack[] getTradeInputs() {
 		ItemStack[] tradeInputs = new ItemStack[9];
 		for (int i = 0; i < 9; i++) {
 			tradeInputs[i] = ItemStack.copyItemStack(inv[i]);
 		}
 		return tradeInputs;
 	}
 
 	public ItemStack[] getTradeOutputs() {
 		ItemStack[] tradeOutputs = new ItemStack[9];
 		for (int i = 0; i < 9; i++) {
 			tradeOutputs[i] = ItemStack.copyItemStack(inv[i + 9]);
 		}
 		return tradeOutputs;
 	}
 
 	public boolean canTrade(IInventory fakeInv, int inputMinSlot, int inputMaxSlot,
 			int outputMinSlot, int outputMaxSlot) {
 		HashMap<ItemStackMapKey, Integer> tradeInput = InventoryHelper
 				.inventoryToHashMap(InventoryHelper.itemStackArrayToInventory(getTradeInputs()));
 		BigInteger requiredMoney = MoneyHelper.extractFromHashMap(tradeInput);
 
 		HashMap<ItemStackMapKey, Integer> invInput = InventoryHelper.inventoryToHashMap(fakeInv,
 				inputMinSlot, inputMaxSlot);
 		BigInteger availableMoney = MoneyHelper.extractFromHashMap(tradeInput);
 
 		if (!InventoryHelper.canRemoveFromHashMap(invInput, tradeInput)) return false;
 
 		BigInteger newMoney = availableMoney.subtract(requiredMoney);
 		if (newMoney.signum() == -1) {
 			return false;
 		}
 		return true;
 	}
 
 	public boolean doTrade(IInventory fakeInv, int inputMinSlot, int inputMaxSlot,
 			int outputMinSlot, int outputMaxSlot) throws TradeException {
 		if (fakeInv == null) {
 			throw new TradeException(new NullPointerException());
 		}
 
 		// Collect trade
 		HashMap<ItemStackMapKey, Integer> tradeInput = InventoryHelper
 				.inventoryToHashMap(InventoryHelper.itemStackArrayToInventory(getTradeInputs()));
 		// Extract money from trade
 		BigInteger requiredMoney = MoneyHelper.extractFromHashMap(tradeInput);
 
 		// Collect input
 		HashMap<ItemStackMapKey, Integer> invInput = InventoryHelper.inventoryToHashMap(fakeInv,
 				inputMinSlot, inputMaxSlot);
 		// Extract money from input
 		BigInteger availableMoney = MoneyHelper.extractFromHashMap(invInput);
 
 		// Remove from map
 		if (!InventoryHelper.removeFromHashMap(invInput, tradeInput)) {
 			return false;
 		}
 
 		// Decrease trade money from input money
 		BigInteger newMoney = availableMoney.subtract(requiredMoney);
 		// Compare
 		if (newMoney.signum() == -1) {
 			return false;
 		}
 
 		HashMap<ItemStackMapKey, Integer> nearestInventory = null;
 		IInventory nearestInv = null;
 		// Get nearestInventory
 		for (ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
 			TileEntity tile = worldObj.getBlockTileEntity(xCoord + d.offsetX, yCoord + d.offsetY,
 					zCoord + d.offsetZ);
 			if (tile != null && tile instanceof IInventory
 					&& ((IInventory) tile).getSizeInventory() > 2) {
 				nearestInv = (IInventory) tile;
 				nearestInventory = InventoryHelper.inventoryToHashMap(nearestInv);
 				break;
 			}
 		}
 		if (nearestInventory == null) return false;
 
 		BigInteger nearestInvMoney = MoneyHelper.extractFromHashMap(nearestInventory);
 		nearestInvMoney = nearestInvMoney.add(requiredMoney);
		InventoryHelper.addToHashMap(nearestInventory, tradeInput);
 
 		// Remove output from nearestInventory
 		HashMap<ItemStackMapKey, Integer> tradeOutput = InventoryHelper
 				.inventoryToHashMap(InventoryHelper.itemStackArrayToInventory(getTradeOutputs()));
 		BigInteger outputMoney = MoneyHelper.extractFromHashMap(tradeOutput);
 		nearestInvMoney = nearestInvMoney.subtract(outputMoney);
 		if (nearestInvMoney.signum() == -1) {
 			return false;
 		}
 		InventoryHelper.removeFromHashMap(nearestInventory, tradeOutput);
 
 		MoneyHelper.insertIntoHashMap(nearestInventory, nearestInvMoney);
 
 		MoneyHelper.insertIntoHashMap(invInput, newMoney);
 
 		ItemStack[] outBackup = InventoryHelper.inventoryToItemStackArray(fakeInv, outputMinSlot,
 				outputMaxSlot);
 		ItemStack[] inBackup = InventoryHelper.inventoryToItemStackArray(fakeInv, inputMinSlot,
 				inputMaxSlot);
 		ItemStack[] nearbyBackup = InventoryHelper.inventoryToItemStackArray(nearestInv);
 		// Insert output
 		if (!InventoryHelper.itemStackArrayIntoInventory(fakeInv, getTradeOutputs(), outputMinSlot,
 				outputMaxSlot)) {
 			throw new OutOfInventorySpaceException();
 		}
 		// Clear input
 		for (int i = inputMinSlot; i <= inputMaxSlot; i++) {
 			fakeInv.setInventorySlotContents(i, null);
 		}
 		// Insert input
 		if (!InventoryHelper.hashMapIntoInventory(fakeInv, invInput, inputMinSlot, inputMaxSlot)) {
 			InventoryHelper.itemStackArrayIntoInventory(fakeInv, outBackup, outputMinSlot,
 					outputMaxSlot);
 			InventoryHelper.itemStackArrayIntoInventory(fakeInv, inBackup, inputMinSlot,
 					inputMaxSlot);
 			throw new OutOfInventorySpaceException();
 		}
 		for (int i = 0; i < nearestInv.getSizeInventory(); i++) {
 			nearestInv.setInventorySlotContents(i, null);
 		}
 		if (!InventoryHelper.hashMapIntoInventory(nearestInv, nearestInventory)) {
 			InventoryHelper.itemStackArrayIntoInventory(fakeInv, outBackup, outputMinSlot,
 					outputMaxSlot);
 			InventoryHelper.itemStackArrayIntoInventory(fakeInv, inBackup, inputMinSlot,
 					inputMaxSlot);
 			InventoryHelper.itemStackArrayIntoInventory(nearestInv, nearbyBackup);
 			throw new OutOfInventorySpaceException();
 		}
 		return true;
 	}
 
 	@Override
 	public String getInvName() {
 		return "endermoney.traders.item";
 	}
 
 	@Override
 	public boolean isInvNameLocalized() {
 		return false;
 	}
 
 	@Override
 	public void openChest() {
 	}
 
 	@Override
 	public void closeChest() {
 	}
 
 }
