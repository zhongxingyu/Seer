 package com.df.masterdata.service.impl;
 
 import java.util.List;
 
 import com.df.masterdata.dal.CategoryDAL;
 import com.df.masterdata.entity.Category;
 import com.df.masterdata.service.inf.CategoryServiceInf;
 
 public class CategoryServiceImpl implements CategoryServiceInf {
 
     private CategoryDAL categoryDAL;
 
     public void setCategoryDAL(CategoryDAL categoryDAL) {
 	this.categoryDAL = categoryDAL;
     }
 
     @Override
     public List<Category> getRootCategories() {
 	return categoryDAL.getRootCategories();
     }
 
     @Override
     public int removeCategory(long categoryId) {
	return categoryDAL.removeCategoryById(categoryId); 
     }
 
     @Override
     public Category newCategory(Category c, Long parentCategoryId) {
	if(parentCategoryId!=null){
 	    Category parent = categoryDAL.find(Category.class, parentCategoryId);
	    if(parent != null){
 		c.setParent(parent);
 	    }
	} 
 	return categoryDAL.merge(c);
     }
 
 }
