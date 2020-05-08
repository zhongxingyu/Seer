 package com.inspedio.system.helper;
 
 import com.inspedio.system.helper.record.SaveManager;
 
 /**
  * InsSave is a class for handling Save/Load mechanism.<br>
  */
 public abstract class InsSave extends SaveManager{
 	
	public boolean autoLoad;
	
	public InsSave(String Name, String Version, boolean AutoLoad){
 		super(Name, Version);
		this.autoLoad = AutoLoad;
 	}
 	
 	public InsSave(String RecordName){
 		this(RecordName, "1.0", true);
 	}
 		
 	/**
 	 * Implement this method by Putting your Data into SaveObject.<br>
 	 * This method will automatically called before calling save().
 	 */
 	public abstract void assignData();
 	
 	/**
 	 * Implement this method by Retrieving SaveObject into your Data.<br>
 	 * This method will automatically called after calling load().
 	 */
 	public abstract void retrieveData();
 	
 	
 	public void save(){
 		this.assignData();
 		super.save();
 	}
 	
 	public boolean load(){
 		boolean success = super.load();
 		this.retrieveData();
 		return success;
 	}
 	
 }
