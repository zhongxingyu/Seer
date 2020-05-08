  /*
   * Copyright  2012 Mufasa developer unit
   *
   * This file is part of Mufasa Budget.
   *
   *	Mufasa Budget is free software: you can redistribute it and/or modify
   * it under the terms of the GNU General Public License as published by
   * the Free Software Foundation, either version 3 of the License, or
   * (at your option) any later version.
   *
   * Mufasa Budget is distributed in the hope that it will be useful,
   * but WITHOUT ANY WARRANTY; without even the implied warranty of
   * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   * GNU General Public License for more details.
   *
   * You should have received a copy of the GNU General Public License
   * along with Mufasa Budget.  If not, see <http://www.gnu.org/licenses/>.
   */
 package it.chalmers.mufasa.android_budget_app.model;
 
 /**
  * A class representing categories in which transactions will be placed in.
  * 
  * @author marcusisaksson
  * 
  */
 
 public class Category {
 	private String name;
 	private int id;
 	//This category's id has to be either Constants.EXPENSE_ID or Consntants.INCOME_ID
 	private Category parent;
 
 	public Category(String name, int id, Category parent) {
 		setName(name);
 		setId(id);
 		setParent(parent);
 	}
	
	public Category clone(){
		return new Category(name, id, parent);
	}
 
 	private void setName(String name) {
 		this.name = name;
 	}
 
 	private void setId(int id) {
 		this.id = id;
 	}
 
 	private void setParent(Category parent) {
 		this.parent = parent;
 	}
 
 	public String getName() {
 		return this.name;
 	}
 
 	public int getId() {
 		return this.id;
 	}
 
 	public Category getParent() {
 		return this.parent;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + id;
 		result = prime * result + ((name == null) ? 0 : name.hashCode());
 		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj){
 			return true;
 		}
 		if (obj == null){
 			return false;
 		}
 		if (getClass() != obj.getClass()){
 			return false;
 		}
 			
 		Category other = (Category) obj;
 		if (id != other.id){
 			return false;
 		}
 		if (name == null) {
 			if (other.name != null)
 				return false;
 		} else if (!name.equals(other.name))
 			return false;
 		if (parent == null) {
 			if (other.parent != null)
 				return false;
 		} else if (!parent.equals(other.parent))
 			return false;
 		return true;
 	}
 
 	@Override
 	public String toString() {
 		return "Category [name=" + name + ", id=" + id + ", parent=" + parent
 				+ "]";
 	}
 
 }
