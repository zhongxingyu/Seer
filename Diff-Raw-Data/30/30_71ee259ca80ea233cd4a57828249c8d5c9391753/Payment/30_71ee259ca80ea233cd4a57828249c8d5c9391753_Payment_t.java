 package com.fullwall.Citizens.Economy;
 
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.material.MaterialData;
 
 import com.fullwall.Citizens.Traders.ItemPrice;
 
 public class Payment {
 
 	private int price;
 	private ItemStack item;
 	private boolean iConomy;
 
 	/**
 	 * Defines a payment, which can be either iConomy or an item payment. This
 	 * constructor defines an item payment.
 	 * 
 	 * @param price
 	 * @param item
 	 * @param iConomy
 	 */
 	public Payment(int price, ItemStack item, boolean iConomy) {
 		this.setPrice(price);
 		this.setItem(item);
 		this.setiConomy(iConomy);
 	}
 
 	/**
 	 * Defines a payment, which can be either iConomy or an item payment. This
 	 * constructor defines an iConomy payment.
 	 * 
 	 * @param price
 	 * @param item
 	 * @param iConomy
 	 */
 	public Payment(int price, boolean iConomy) {
 		this.setPrice(price);
 		this.setItem(null);
 		this.setiConomy(iConomy);
 	}
 
 	public Payment(ItemPrice price2) {
 		this.setPrice(price2.getPrice());
 		ItemStack stack = new ItemStack(price2.getItemID(), price2.getPrice());
 		stack.setData(new MaterialData(price2.getData()));
 		this.setItem(stack);
 		this.setiConomy(price2.isiConomy());
 	}
 
	public Payment(ItemStack stocking, boolean iConomy) {
		this.setiConomy(iConomy);
		this.setItem(stocking);
		this.setPrice(stocking.getAmount());
	}

 	public void setiConomy(boolean iConomy) {
 		this.iConomy = iConomy;
 	}
 
 	public boolean isiConomy() {
 		return iConomy;
 	}
 
 	public ItemStack getItem() {
 		return item;
 	}
 
 	public void setItem(ItemStack item) {
 		this.item = item;
 	}
 
 	public void setPrice(int price) {
 		this.price = price;
 	}
 
 	public int getPrice() {
 		return price;
 	}
 }
