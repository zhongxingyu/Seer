 package com.manning.gia.todo.model;
 
 import android.text.TextUtils;
 
 public class Item {
 	private static final String MISSING_TEXT_MESSAGE = "Please enter text for the todo item";
 	private long id;
 	private String name;
 	
 	public Item() {
 		
 	}
 	
 	public Item(String name) {
 		this.name = name;
 	}
 
 	public long getId() {
 		return id;
 	}
 
 	public void setId(long id) {
 		this.id = id;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	@Override
 	public String toString() {
 		return name;
 	}
 	
 	/**
 	 * Validates whether or not this can be saved.
 	 * @return null if it validates, string with validation errors otherwise
 	 */
 	public String validate() {
 		String errorMessage = null;
		if (TextUtils.isEmpty(name)) {
 			errorMessage = MISSING_TEXT_MESSAGE;
 		}
 		return errorMessage;
 	}
 	
 }
