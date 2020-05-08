 package it.chalmers.mufasa.android_budget_app.model;
 
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.util.List;
 
 import android.content.Context;
 
 /**
  * version 1
  * 
  * @author FannyM A class which contains the current state of the
  *         ManageCategoryActivity.
  */
 
 public class ManageCategoryModel {
 
 	private Account account;
 	private PropertyChangeSupport pcs;
 	private Category currentParentCategory;
 	private boolean editMode = false;
 
 	public ManageCategoryModel(Context context) {
 		pcs = new PropertyChangeSupport(this);
 		this.account = Account.getInstance(context);
 		this.setCurrentParentCategory(account.getCategory(2));
 	}
 
 	public List<Category> getCategoryList() {
 		return account.getCategories(currentParentCategory);
 	}
 
 	public void addCategory(String name, Category parent) {
		
 		account.addCategory(name, parent);
 		pcs.firePropertyChange("added_category", null, null);
 	}
 
 	public void removeCategory(Category category) {
 		account.removeCategory(category);
 		pcs.firePropertyChange("removed_category", null, null);
 	}
 
 	public void editCategory(Category category, String newName) {
 		account.editCategory(category, newName);
 		pcs.firePropertyChange("edited_category", null, null);
 	}
 
 	public Category getCurrentParentCategory() {
 		return currentParentCategory;
 	}
 
 	public void setCurrentParentCategory(int id) {
 		this.setCurrentParentCategory(account.getCategory(id));
 	}
 
 	public void setCurrentParentCategory(Category category) {
 		currentParentCategory = category;
 		pcs.firePropertyChange("changed_parent_category", null, null);
 	}
 
 	public void addPropertyChangeListener(PropertyChangeListener listener) {
 		pcs.addPropertyChangeListener(listener);
 	}
 
 	public void removePropertyChangeListener(PropertyChangeListener listener) {
 		pcs.removePropertyChangeListener(listener);
 	}
 
 	public boolean isEditMode() {
 		return editMode;
 	}
 
 	public void setEditMode(boolean set) {
 		if (this.editMode != set) {
 			this.editMode = set;
 			pcs.firePropertyChange("changed_editmode", null, null);
 		}
 	}
 }
