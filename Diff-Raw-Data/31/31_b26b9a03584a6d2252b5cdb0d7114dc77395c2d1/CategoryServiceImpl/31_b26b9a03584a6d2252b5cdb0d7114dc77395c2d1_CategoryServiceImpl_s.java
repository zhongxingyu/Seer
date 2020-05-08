 package com.plugtree.smartknowledgediscovery.server;
 
import java.util.LinkedList;
 import java.util.List;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 import com.plugtree.smartknowledgediscovery.client.services.CategoryService;
 import com.plugtree.smartprocessdiscovery.model.common.Category;
 
 @SuppressWarnings("serial")
 public class CategoryServiceImpl extends RemoteServiceServlet implements CategoryService {
 
    @Override
     public List<Category> fetch() {
        return new LinkedList<Category>();
     }
 
     @Override
     public List<Category> fetchWithFilter(String filter) {
        return new LinkedList<Category>();
     }
 
     @Override
     public List<Category> add(Category category) {
        return new LinkedList<Category>();
     }
 
     @Override
     public List<Category> remove(Long categoryId) {
        return new LinkedList<Category>();
     }
 
     @Override
     public List<Category> update(Category category) {
        return new LinkedList<Category>();
     }
 }
     
