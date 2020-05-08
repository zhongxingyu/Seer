 package com.sk.util.builder;
 
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.sk.domain.ProductWithQuantity;
 import com.sk.domain.ShoppingCart;
 
 public class ShoppingCartBuilder extends BaseBuilder<ShoppingCart, ShoppingCartBuilder> {
	
	private Set<ProductWithQuantity> items = new HashSet<ProductWithQuantity>();

 
 	@Override
 	protected ShoppingCart doBuild() {
 		ShoppingCart shoppingCart = new ShoppingCart();
 		return shoppingCart;
 	}
	
	public ShoppingCartBuilder(ProductWithQuantity... items) {
		this.items.addAll(Arrays.asList(items));
	}
 
 }
