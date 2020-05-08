 package com.irondish.mvc;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.annotation.PostConstruct;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 @Component
 public class SimpleProductCatalog implements ProductCatalog {
 
 	Map<Long, Product> productMap = new HashMap<Long, Product>();
 	
 	@Autowired
 	private IdGenerator idGenerator;
 	
 	public SimpleProductCatalog() {
 	}
 
 	@PostConstruct
 	public void init() {
 		add("iron bucket", 100);
 		add("wooden bucket", 150);
 		add("diamond bucket", 999999);
 		add("plastic bucket", 50);
 
 	}
 	
 	private void add(String name, int price) {
 		Product product = new Product(idGenerator.nextId("Product"), name, price);
 		addProduct(product);
 	}
 	
 	@Override
 	public Product findById(Long id) {
 		Product product = productMap.get(id);
 		if (product == null) {
 			throw new RuntimeException("Product id not found: " + id);
 		}
 		return product;
 	}
 
 	@Override
 	public List<Product> getProducts() {
 		return new ArrayList<Product>(productMap.values());
 	}
 	@Override
 	public void deleteProduct(Long id) {
 		System.out.println("deleting product: ...." + id);
 		productMap.remove(id);
 	}
 	@Override
 	public void addProduct(String productName, String price) {
 		addProduct(productName, Integer.parseInt(price));
 	}
 
 	@Override
 	public void addProduct(String productName, Integer price) {
 		add(productName, price);
 	}
 
 	@Override
 	public void addProduct(Product product) {
		if (product.getId() == null) {
			product.setId(idGenerator.nextId("Product"));
		}
 		productMap.put(product.getId(), product);		
 	}
 
 }
