 package com.github.soniex2.endermoney.trading.tileentity;
 
 import java.util.HashMap;
 import java.util.Set;
 import java.util.Map.Entry;
 
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 
 import com.github.soniex2.endermoney.trading.base.AbstractTraderTileEntity;
 import com.github.soniex2.endermoney.trading.base.AbstractTraderTileEntity.TradeStatus;
 import com.github.soniex2.endermoney.trading.helper.itemstack.ItemStackCounter;
 
 public class TileEntityItemTrader extends AbstractTraderTileEntity {
 
 	public TileEntityItemTrader() {
 		super(18);
 	}
 
 	@Override
 	public ItemStack[] getTradeInputs() {
 		ItemStack[] tradeInputs = new ItemStack[9];
 		for (int i = 0; i < 9; i++) {
 			tradeInputs[i] = ItemStack.copyItemStack(inv[i]);
 		}
 		return tradeInputs;
 	}
 
 	@Override
 	public ItemStack[] getTradeOutputs() {
 		ItemStack[] tradeOutputs = new ItemStack[9];
 		for (int i = 0; i < 9; i++) {
 			tradeOutputs[i] = ItemStack.copyItemStack(inv[i + 9]);
 		}
 		return tradeOutputs;
 	}
 
 	@Override
 	public String getInventoryName() {
 		return "endermoney.traders.item";
 	}
 
 	@Override
 	public boolean hasCustomInventoryName() {
 		return false;
 	}
 
 	@Override
 	public void openInventory() {
 
 	}
 
 	@Override
 	public void closeInventory() {
 
 	}
 
 	@Override
 	public TradeStatus doTrade(IInventory fakeInv, int inputMinSlot,
 			int inputMaxSlot, int outputMinSlot, int outputMaxSlot,
 			boolean really) {
 		if (fakeInv == null)
 			return TradeStatus.INVALID;
 		if (fakeInv.getSizeInventory() <= inputMinSlot
 				|| inputMinSlot > inputMaxSlot
 				|| fakeInv.getSizeInventory() <= outputMinSlot
 				|| outputMinSlot > outputMaxSlot || inputMinSlot < 0
 				|| outputMinSlot < 0) {
 			return TradeStatus.INVALID;
 		}
 
 		// INPUT
 		ItemStackCounter tradeInputs = new ItemStackCounter();
 		for (ItemStack is : getTradeInputs()) {
 			tradeInputs.put(is);
 		}
 		ItemStackCounter playerInputs = new ItemStackCounter();
 		for (int i = inputMinSlot; i <= inputMaxSlot; i++) {
 			playerInputs.put(fakeInv.getStackInSlot(i));
 		}
 		HashMap<NBTTagCompound, Integer> newPlayerInputs = new HashMap<NBTTagCompound, Integer>();
 		for (Entry<NBTTagCompound, Integer> entry : tradeInputs) {
			int got = playerInputs.get(entry.getKey()) != null ? playerInputs
					.get(entry.getKey()) : 0;
 			int value = entry.getValue();
 			if (got < value) {
 				return TradeStatus.NOT_ENOUGH_INPUT;
 			} else {
 				if (really) {
 					got -= value;
 					newPlayerInputs.put(entry.getKey(), got);
 				}
 			}
 		}
 		// INPUT END
 
 		// OUTPUT
 		ItemStack[] tradeOutputs = getTradeOutputs();
 		ItemStack[] playerOutputs = new ItemStack[outputMaxSlot - outputMinSlot
 				+ 1];
 		for (int i = 0; i < tradeOutputs.length; i++) {
 			ItemStack is = tradeOutputs[i];
 			if (is == null)
 				continue;
 			for (int j = outputMinSlot; j <= outputMaxSlot; j++) {
 				ItemStack o = fakeInv.getStackInSlot(j);
 				if (really) {
 					playerOutputs[j] = o;
 				}
 				if (o == null) {
 					if (really) {
 						playerOutputs[j] = tradeOutputs[i];
 					}
 					tradeOutputs[i] = null;
 				} else if (is.isItemEqual(o)
 						&& ItemStack.areItemStackTagsEqual(is, o)) {
 					int maxSize = is.getMaxStackSize();
 					if (o.stackSize >= maxSize) {
 						continue;
 					}
 					if (is.stackSize + o.stackSize <= maxSize) {
 						if (really) {
 							ItemStack nis = is.copy();
 							nis.stackSize += o.stackSize;
 							playerOutputs[j] = nis;
 						}
 						tradeOutputs[i] = null;
 					} else {
 						if (really) {
 							ItemStack nis = is.copy();
 							nis.stackSize = maxSize;
 							playerOutputs[j] = nis;
 						}
 						// is.stackSize = is.stackSize + o.stackSize - maxSize;
 						// this is only + and - so operator order doesn't matter
 						is.stackSize += o.stackSize - maxSize;
 					}
 				}
 			}
 		}
 		for (ItemStack is : tradeOutputs) {
 			if (is != null)
 				return TradeStatus.RESULTS_FULL;
 		}
 		// OUTPUT END
 
 		// TODO implement this
 		if (true)
 			return TradeStatus.INVALID;
 
 		// SET STUFF
 		if (!really) {
 			return TradeStatus.AVAILABLE;
 		}
 		Set<Entry<NBTTagCompound, Integer>> entrySet = newPlayerInputs
 				.entrySet();
 		int inputSlot = inputMinSlot;
 		for (Entry<NBTTagCompound, Integer> entry : entrySet) {
 			NBTTagCompound k = entry.getKey();
 			k.setByte("Count", (byte) 0);
 			ItemStack is = ItemStack.loadItemStackFromNBT(k);
 			if (entry.getValue() > is.getMaxStackSize()) {
 				is.stackSize = is.getMaxStackSize();
 				int stacks = entry.getValue() / is.getMaxStackSize();
 				if (inputSlot > inputMaxSlot) {
 					fakeInv.markDirty();
 					return TradeStatus.OVERFLOW;
 				}
 				fakeInv.setInventorySlotContents(inputSlot++, is);
 				for (int i = 1; i < stacks; i++) {
 					if (inputSlot > inputMaxSlot) {
 						fakeInv.markDirty();
 						return TradeStatus.OVERFLOW;
 					}
 					fakeInv.setInventorySlotContents(inputSlot++, is.copy());
 				}
 				ItemStack nis = is.copy();
 				nis.stackSize = entry.getValue()
 						- (stacks * is.getMaxStackSize());
 				if (inputSlot > inputMaxSlot) {
 					fakeInv.markDirty();
 					return TradeStatus.OVERFLOW;
 				}
 				fakeInv.setInventorySlotContents(inputSlot++, nis);
 			} else {
 				is.stackSize = entry.getValue();
 				if (inputSlot > inputMaxSlot) {
 					fakeInv.markDirty();
 					return TradeStatus.OVERFLOW;
 				}
 				fakeInv.setInventorySlotContents(inputSlot++, is);
 			}
 		}
 		while (inputSlot <= inputMaxSlot) {
 			fakeInv.setInventorySlotContents(inputSlot++, null);
 		}
 		for (int i = 0; i < playerOutputs.length; i++) {
 			fakeInv.setInventorySlotContents(i + outputMinSlot,
 					playerOutputs[i]);
 		}
 		fakeInv.markDirty();
 		// SET STUFF END
 
 		return TradeStatus.SUCCESS;
 	}
 
 }
