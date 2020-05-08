 package com.shopservice.dao;
 
 import com.shopservice.domain.Product;
 
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 public class CachedProductDAO implements ProductDAO {
 
     private ProductDAO productDAO;
 
    private Map<String, List<Product>> categoryToProducts = new ConcurrentHashMap<String, List<Product>>();
 
     public CachedProductDAO(ProductDAO productDAO) {
         this.productDAO = productDAO;
     }
 
     @Override
     public List<Product> getProducts(String categoryId) {
         if (!categoryToProducts.containsKey(categoryId))
             categoryToProducts.put(categoryId, productDAO.getProducts(categoryId));
 
         return categoryToProducts.get(categoryId);    }
 
     @Override
     public List<Product> getProducts(Collection<String> productIds) {
         //no cached
         return productDAO.getProducts(productIds);
     }
 }
