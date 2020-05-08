 package com.ngdb.entities.shop;
 
 import static com.google.common.collect.Collections2.filter;
 import static java.lang.Double.MAX_VALUE;
 import static java.lang.Double.MIN_VALUE;
 
 import java.util.Collection;
 import java.util.Set;
 
 import javax.persistence.Embeddable;
 import javax.persistence.OneToMany;
 
 import com.google.common.base.Predicate;
 import com.ngdb.Predicates;
 import com.ngdb.entities.reference.State;
 
 @Embeddable
 public class ShopItems {
 
	@OneToMany(mappedBy = "article")
 	private Set<ShopItem> shopItems;
 
 	public int getAvailableCopyCount() {
 		return getShopItemsForSale().size();
 	}
 
 	public Collection<ShopItem> getShopItemsForSale() {
 		return filter(shopItems, Predicates.shopItemsForSale);
 	}
 
 	public boolean hasShopItemInState(State state) {
 		return !shopItemsByState(state).isEmpty();
 	}
 
 	public int getAvailableCopyInState(State state) {
 		return shopItemsByState(state).size();
 	}
 
 	public double getAveragePriceInState(State state) {
 		Collection<ShopItem> shopItems = shopItemsByState(state);
 		if (shopItems.size() == 0) {
 			return 0;
 		}
 		double sum = 0;
 		for (ShopItem shopItem : shopItems) {
 			sum += shopItem.getPrice();
 		}
 		return sum / shopItems.size();
 	}
 
 	public double getMaxPriceInState(State state) {
 		Double max = MIN_VALUE;
 		for (ShopItem shopItem : shopItemsByState(state)) {
 			max = Math.max(max, shopItem.getPrice());
 		}
 		if (max.equals(MIN_VALUE)) {
 			return 0;
 		}
 		return max;
 	}
 
 	public double getMinPriceInState(State state) {
 		Double min = MAX_VALUE;
 		for (ShopItem shopItem : shopItemsByState(state)) {
 			min = Math.min(min, shopItem.getPrice());
 		}
 		if (min.equals(MAX_VALUE)) {
 			return 0;
 		}
 		return min;
 	}
 
 	private Collection<ShopItem> shopItemsByState(State state) {
 		return filter(shopItems, new StateFilter(state.getTitle()));
 	}
 
 	private class StateFilter implements Predicate<ShopItem> {
 		private String state;
 
 		StateFilter(String state) {
 			this.state = state;
 		}
 
 		@Override
 		public boolean apply(ShopItem shopItem) {
 			if (shopItem == null) {
 				return false;
 			}
 			return shopItem.isSold() && state.equals(shopItem.getState().getTitle());
 		}
 	}
 }
