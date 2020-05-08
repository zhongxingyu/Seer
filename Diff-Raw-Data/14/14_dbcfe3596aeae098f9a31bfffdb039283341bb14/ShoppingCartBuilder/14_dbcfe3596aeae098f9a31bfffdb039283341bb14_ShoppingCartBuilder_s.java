 package com.sk.util.builder;
 
 import com.sk.domain.ShoppingCart;
 
 public class ShoppingCartBuilder extends BaseBuilder<ShoppingCart, ShoppingCartBuilder> {
 
 	@Override
 	protected ShoppingCart doBuild() {
 		ShoppingCart shoppingCart = new ShoppingCart();
 		return shoppingCart;
 	}
 
 }
