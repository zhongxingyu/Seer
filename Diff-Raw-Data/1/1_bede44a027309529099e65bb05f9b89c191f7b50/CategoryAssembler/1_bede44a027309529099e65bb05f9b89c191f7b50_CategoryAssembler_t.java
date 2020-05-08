 package com.shopservice.assemblers;
 
 import com.google.inject.Inject;
 import com.shopservice.Util;
 import com.shopservice.dao.CategoryRepository;
 import com.shopservice.dao.ProductEntryRepository;
 import com.shopservice.domain.Category;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 public class CategoryAssembler {
 
     ProductEntryRepository productEntryRepository;
     CategoryRepository categoryRepository;
 
     public CategoryAssembler(ProductEntryRepository productEntryRepository, CategoryRepository categoryRepository) {
         this.productEntryRepository = productEntryRepository;
         this.categoryRepository = categoryRepository;
     }
 
     public CategoryResponse getCategories(String clientId, String groupId) throws Exception {
         Map<String,Category> categories = new HashMap<String,Category>();
         for (Category category : categoryRepository.getCategories())
             categories.put(category.id, category);
 
         Map<String,Integer> productsCountPerCategory = productEntryRepository.getCountPerCategory(clientId, groupId);
 
         for (String categoryId : categories.keySet())
             categories.get(categoryId).count =
                     productsCountPerCategory.containsKey(categoryId) ? productsCountPerCategory.get(categoryId) : 0;
 
         int totalCount = Util.sum(productsCountPerCategory.values());
 
         return new CategoryResponse( categories.values(), totalCount );
     }
 
     public static class CategoryResponse
     {
         public Collection<Category> categories;
         public int totalCount;
 
         public CategoryResponse(Collection<Category> categories, int totalCount) {
             this.categories = categories;
             this.totalCount = totalCount;
         }
     }
 
 }
