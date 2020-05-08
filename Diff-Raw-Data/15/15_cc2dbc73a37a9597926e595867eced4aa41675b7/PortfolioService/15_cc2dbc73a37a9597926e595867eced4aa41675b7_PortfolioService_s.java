 package com.osgo.plugin.portfolio.api;
 
 import java.util.List;
 import java.util.Map;
 
 import com.google.appengine.api.images.Image;
 import com.googlecode.objectify.Key;
 import com.osgo.plugin.portfolio.model.objectify.Category;
 import com.osgo.plugin.portfolio.model.objectify.Picture;
 import com.osgo.plugin.portfolio.model.objectify.Project;
 
 public interface PortfolioService {
 	
 	/** Returns list of Projects */
 	List<Project> getProjectList(); //TODO: add a category Id
 	Project getProject(long id);
 	void deleteProject(long id);
 	Project addProject(Map<String, Object> input);
 	Project updateProject(Project project);
 	void addImage(Picture image, Project project);
 	
 	List<Category> getCategoryList();
 	Category getCategory(long id);
 	void deleteCategory(long id);
 	Category addCategory(Map<String, Object> input);
 	Category updateCategory(Category category);
 	
 	Object update(Object entity);
 	
 }
