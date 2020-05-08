 package com.debortoliwines.openerp.reporting.di;
 
 /*
  *   This file is part of OpenERPJavaReportHelper
  *
  *   OpenERPJavaAPI is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU Lesser General Public License as published by
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   OpenERPJavaAPI is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU Lesser General Public License for more details.
  *
  *   You should have received a copy of the GNU Lesser General Public License
  *   along with OpenERPJavaAPI.  If not, see <http://www.gnu.org/licenses/>.
  *
  *   Copyright 2012 De Bortoli Wines Pty Limited (Australia)
  */
 
 import java.util.ArrayList;
 
 import com.debortoliwines.openerp.api.Field.FieldType;
 
 /**
  * Object to hold query information that will be used to fetch data.
  * One object should be created for every modelPath and instance number.
  * The OpenERPHelper class generates this class
  * @author Pieter van der Merwe
  * @since  Jan 5, 2012
  */
 public class OpenERPQueryItem{
 	private final String relatedField;
 	private final FieldType relationType;
 	private final String modelName;
 	private final int instanceNum;
 	private final ArrayList<String> fields = new ArrayList<String>();
 	private ArrayList<OpenERPFilterInfo> filters = new ArrayList<OpenERPFilterInfo>();
 	private final ArrayList<OpenERPQueryItem> childItems = new ArrayList<OpenERPQueryItem>();
 	private OpenERPQueryItem parentQueryItem = null;
 	
 	/**
 	 * Default constructor
 	 * @param relatedField The field name that relates this field to its parent
 	 * @param relationType Many2One, Many2Many etc
 	 * @param modelName Name of the model this field is for.
 	 * @param instanceNum Instance of the model this field is for
 	 */
 	public OpenERPQueryItem(String relatedField, FieldType relationType, String modelName, int instanceNum){
 		this.relatedField = relatedField;
 		this.relationType = relationType;
 		this.modelName = modelName;
 		this.instanceNum = instanceNum;
 	}
 	
 	/**
 	 * Add a fields to the list that will be selected fetched using this query item
 	 * @param fieldName Field name to add
 	 */
 	public void addField(String fieldName){
 		if (!fields.contains(fieldName)){
 			fields.add(fieldName);
 		}
 	}
 	
 	/**
 	 * Adds a OpenERPQueryItem as a child to this item
 	 * @param item
 	 */
 	public void addChildQuery(OpenERPQueryItem item){
 		childItems.add(item);
 		item.setParentQueryItem(this);
 	}
 	
 	/**
 	 * Get the field that relates this object to its parent
 	 * @return
 	 */
 	public String getRelatedField(){
 		return relatedField;
 	}
 	
 	/**
 	 * Gets the additional filters that are associated with this QueryItem.  It will be used to 
 	 * add additional filters to an object (additional to the default child ids from the related field).
 	 * @return
 	 */
 	public ArrayList<OpenERPFilterInfo> getFilters(){
 		return filters;
 	}
 	
 	/**
    * Sets the additional filters that are associated with this QueryItem.  It will be used to 
    * add additional filters to an object (additional to the default child ids from the related field).
   * @return
    */
 	public void setFilters(ArrayList<OpenERPFilterInfo> filters) {
 		this.filters = filters;
 	}
 	
 	/**
 	 * Get the model name this QueryItem is for
 	 * @return
 	 */
 	public String getModelName() {
 		return modelName;
 	}
 	
 	/**
 	 * Gets the relation type to the parent object
 	 * @return
 	 */
 	public FieldType getRelationType() {
 		return relationType;
 	}
 	
 	/**
 	 * Get the list of fields that must be fetched from this object
 	 * @return
 	 */
 	public ArrayList<String> getFields() {
 		return fields;
 	}
 	
 	/**
 	 * Get the instance number of this model path that makes this object unique
 	 * @return
 	 */
 	public int getInstanceNum() {
 		return instanceNum;
 	}
 	
 	/** 
 	 * Get the immediate child items linked to this QueryItem
 	 * @return
 	 */
 	public ArrayList<OpenERPQueryItem> getChildItems() {
 		return childItems;
 	}
 	
 	/**
 	 * Only returns child items that from a related field and instance number.
 	 * @param relatedField Field that relates to a child query
 	 * @param instanceNum Instance number of the child query to fetch
 	 * @return
 	 */
 	public OpenERPQueryItem getChildQuery(String relatedField, int instanceNum){
 		for (OpenERPQueryItem item : childItems){
 			if (item.getRelatedField().equals(relatedField)
 					&& item.getInstanceNum() == instanceNum){
 				return item;
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Returns a flat list of all items.  This is useful if you need to get a flat list
 	 * of all modelPaths that data will be fetched for assuming you call this function on the
 	 * root item.
 	 * @return
 	 */
 	public ArrayList<OpenERPQueryItem> getAllChildItems(){
 		ArrayList<OpenERPQueryItem> children = new ArrayList<OpenERPQueryItem>();
 		children.addAll(childItems);
 		for (OpenERPQueryItem item : childItems){
 			children.addAll(item.getAllChildItems());
 		}
 		return children;
 	}
 	
 	/**
 	 * Sets the parent item of a queryItem.  Should really only be called from addChildQuery
 	 * @param parentQueryItem
 	 */
 	private void setParentQueryItem(OpenERPQueryItem parentQueryItem) {
     this.parentQueryItem = parentQueryItem;
   }
 
 	/**
 	 * Returns the modelPath for this QueryItem
 	 * @return
 	 */
 	public String getModelPath() {
 		if (this.parentQueryItem == null){
 			return "[" + modelName + "]";
 		}
 		else {
 			return parentQueryItem.getModelPath() + ".[" + relatedField + "]";
 		}
 	}
 }
