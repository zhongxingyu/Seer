 package com.plugtree.smartknowledgediscovery.server;
 
 import java.util.List;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 import com.plugtree.smartknowledgediscovery.client.services.CategoryService;
import com.plugtree.smartprocessdiscovery.dao.impl.CategoryDaoJpa;
 import com.plugtree.smartprocessdiscovery.model.common.Category;
 
 @SuppressWarnings("serial")
 public class CategoryServiceImpl extends RemoteServiceServlet implements CategoryService {
 
	private CategoryDaoJpa categoryDaoJpa = new CategoryDaoJpa();  
	
	@Override
     public List<Category> fetch() {
		return categoryDaoJpa.listAll();
     }
 
     @Override
     public List<Category> fetchWithFilter(String filter) {
        return categoryDaoJpa.listWithFilter(filter);
     }
 
     @Override
     public List<Category> add(Category category) {
        categoryDaoJpa.save(category);
        
        return categoryDaoJpa.listAll();
     }
 
     @Override
     public List<Category> remove(Long categoryId) {
    	
    	Category category = categoryDaoJpa.findById(categoryId);
        categoryDaoJpa.remove(category);
        
        return categoryDaoJpa.listAll();
     }
 
     @Override
     public List<Category> update(Category category) {
        categoryDaoJpa.update(category);
        
        return categoryDaoJpa.listAll();    
     }
 }
     
