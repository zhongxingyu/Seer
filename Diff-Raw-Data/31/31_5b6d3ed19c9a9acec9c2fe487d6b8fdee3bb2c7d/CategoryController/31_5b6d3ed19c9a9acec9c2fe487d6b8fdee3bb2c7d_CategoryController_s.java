 package controller;
 import view.*;
 
 public class CategoryController {
 	
 	private CategoryView view;
 	public CategoryController(CategoryView view){
 		this.view = view;
 	}
 	
	public void addTask(CategoryPanel cat){
 		
 		view.add(cat);
 		view.revalidate();
 		view.repaint();
 		
 	}
 	
 	public void removeCategory(CategoryPanel cat){
 		
 		view.remove(cat);
 	}
 	
 	public void updateCategoryView(){
 		
 		view.repaint();
 	}
 		
 }
