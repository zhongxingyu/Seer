 package model.common;
 
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 import common.Result;
 
 public abstract class Vault implements IVault {
 
 	
 	protected SortedMap<Integer, IModel> dataVault = new TreeMap<Integer, IModel>();
 	
 	/**
 	 * Constructor.
 	 * 
 	 * 
 	 */
 	public Vault(){
 		return;
 	}
 	
 	
	
 	
 	
 	/**
 	 * adds the item to the map
 	 * 
 	 * @param newItem
 	 */
 	protected Result addItem(IModel newItem){
 		return null;
 	}
 	
 
 }
