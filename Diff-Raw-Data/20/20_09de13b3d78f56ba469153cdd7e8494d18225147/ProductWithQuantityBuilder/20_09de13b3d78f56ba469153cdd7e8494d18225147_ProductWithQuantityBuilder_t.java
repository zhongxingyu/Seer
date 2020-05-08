 package com.sk.util.builder;
 
 import com.sk.domain.Product;
 import com.sk.domain.ProductWithQuantity;
 
 public class ProductWithQuantityBuilder extends BaseBuilder<ProductWithQuantity,ProductWithQuantityBuilder> {
 	
 	private Product product = new ProductBuilder().build();
 	private int quantity = 1;
 
 
	public ProductWithQuantityBuilder product(Product product) {
 		this.product = product;
 		return this;
 	}
 
 
	public ProductWithQuantityBuilder quantity(int quantity) {
 		this.quantity = quantity;
 		return this;
 	}
 
 	@Override
 	protected ProductWithQuantity doBuild() {
 		ProductWithQuantity productWithQuantity = new ProductWithQuantity();
 		productWithQuantity.setQuantity(quantity);
 		productWithQuantity.setProduct(product);
 		return productWithQuantity;
 	}
 
 }
