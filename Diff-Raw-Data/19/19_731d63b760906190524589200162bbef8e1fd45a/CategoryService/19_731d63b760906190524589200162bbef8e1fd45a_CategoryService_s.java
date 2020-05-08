 package com.sk.service;
 
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.sk.domain.Category;
 import com.sk.domain.dao.CategoryDao;
 
 @Service
 public class CategoryService {
 	
	@Autowired
 	private CategoryDao categoryDao;
	
	@Autowired
 	private UrlGeneratorService urlGeneratorService;
 
 	public CategoryService() {}
 	
 	public CategoryService(CategoryDao categoryDao, UrlGeneratorService urlGeneratorService) {
		super();
 		this.categoryDao = categoryDao;
 		this.urlGeneratorService = urlGeneratorService;
 	}
 
 	public List<Category> getAll() {
 		return categoryDao.getAll();
 	}
 	
 	@Transactional
 	public void delete(Category category) {
 		categoryDao.delete(category);
 	}
 
 	public void createURLFromName(Category category) {
 		String url = urlGeneratorService.generateUrlFrom(category.getName());
 		category.setUrl(url);
 	}
 	
 	@Transactional
 	public Category save(Category category) {
 		return categoryDao.persist(category);
 	}
 	
 	public Category findByUrl(String url) {
 		return categoryDao.findByUrl(url);
 	}
 	
 
 }
