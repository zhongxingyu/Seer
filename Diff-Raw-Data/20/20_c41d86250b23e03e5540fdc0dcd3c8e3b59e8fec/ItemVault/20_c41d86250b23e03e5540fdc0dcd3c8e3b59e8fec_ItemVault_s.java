 package model.item;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Map.Entry;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 import model.product.Product;
 import model.common.IModel;
 import model.common.Vault;
 import common.Result;
 import common.util.QueryParser;
 
 
 /**
  * The Item class provides a way to query for ItemModels within the select data backend
  * (A Model superclass or interface could be created for this)
  * <PRE>
  * Item.find(2) // returns ItemModel with id of 2
  * </PRE>
  * Other findBy* methods may be implemented.
  */
 public class ItemVault extends Vault{
 	static ItemVault currentInstance;
 	private ItemVault(){
 		currentInstance = this;
 	}
 	public static synchronized ItemVault getInstance(){
 		if(currentInstance == null) currentInstance = new ItemVault();
 		return currentInstance;
 	}

 	
 	
 	/**
 	 * Returns just one item based on the query sent in. 
 	 * If you need more than one item returned use FindAll
 	 * 
 	 * @param query of form obj.attr = value
 	 * 
 	 * product.id = 5
 	 * name = 'cancer'
 	 * id = 2
 	 * @throws InvocationTargetException 
 	 * @throws IllegalArgumentException 
 	 * @throws IllegalAccessException 
 	 * @throws SecurityException 
 	 * @throws NoSuchMethodException 
 	 * 
 	 */
 	public Item find(String query)  {
 		QueryParser MyQuery = new QueryParser(query);
 
 		
 		//Do a linear Search first
 		//TODO: Add ability to search by index
 		try {
             ArrayList<Item> results = linearSearch(MyQuery,1);
             if(results.size() == 0)
                 return null;
             return results.get(0);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		
 		return null;
 	}
 	
 	/**
 	 * Returns a list of Items which match the criteria
 	 * 
 	 * @param query of form obj.attr = value 
 	 * 
 	 */
 	public ArrayList<Item> findAll(String query) {
 		QueryParser MyQuery = new QueryParser(query);
 
 		
 		//Do a linear Search first
 		//TODO: Add ability to search by index
 		try {
 			ArrayList<Item> results = linearSearch(MyQuery,0);
 			return results;
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		return null;
 	}
 	
 	//Search an ordered hashmap one at a time
 	private ArrayList<Item> linearSearch(QueryParser MyQuery,int count)
             throws IllegalAccessException, IllegalArgumentException,
             InvocationTargetException, NoSuchMethodException, SecurityException{
 		ArrayList<Item> results = new ArrayList<Item>();
 		String objName = MyQuery.getObjName();
 		String attrName = MyQuery.getAttrName();
 		String value = MyQuery.getValue();
 		
 		Item myItem = new Item();
 		Product myProduct = new Product();
 		//Class associated with the item model
 		Class<? extends Item> cls = myItem.getClass();
 		//Method we will call to get the value
 		Method method;
 		
 		
 		method = cls.getMethod("get"+attrName);
 
 		
 		//Loop through entire hashmap and check values one at a time
 		for (Entry<Integer, IModel> entry : dataVault.entrySet()) {
 			myItem = (Item) entry.getValue();
 			String myItemValue; 
 
 			myItemValue = method.invoke(myItem).toString();
 		    
 		    if(myItemValue.equals(value) && !myItem.isDeleted()){
 		    	results.add(new Item(myItem));
 		    }
 		    if(count != 0 && results.size() == count )
 		    	return results;
 		}
 		return results;
 	}
 
     public Item get(int id){
     	Item i = (Item)dataVault.get(id);
     	if(i == null)
     		return null;
 
         return new Item(i);
     }
 
 
 	/**
 	 * Checks if the model passed in already exists in the current map
 	 * - Item must have a unique barcode
 	 * 
 	 * @param model
 	 * @return Result of the check
 	 */
 	public Result validateNew(Item model){
 		assert(model!=null);
 
 		int count = findAll("Barcode = " + model.getBarcode().toString()).size();
 		if(count ==0){
             model.setValid(true);
 			return new Result(true);
         }
         else
 			return new Result(false,"Duplicate barcode");
 	}
 
 	/**
 	 * Checks if the model already exists in the map
 	 * - Retrieve current model by index
 	 * - If barcode is the same do nothing, if it's changed check
 	 * 
 	 * @param model
 	 * @return Result of the check
 	 */
 	public  Result validateModified(Item model){
 		assert(model!=null);
         assert(!dataVault.isEmpty());
 		
 		//Delete current model
 		Item currentModel = this.get(model.getId());
 		currentModel.delete();
 		//Validate passed in model
 		Result result = this.validateNew(model);
 		//Add current model back
 		currentModel.unDelete();
 		if(result.getStatus() == true)
 			model.setValid(true);
         return result;
 	}
 
 	
 	/**
 	 * Adds the Item to the map if it's new.  Should check before doing so.
 	 * 
 	 * @param model Item to add
 	 * @return Result of request
 	 */
 	public  Result saveNew(Item model){
 		if(!model.isValid())
             return new Result(false, "Model must be valid prior to saving,");
 
         int id = 0;
         if(dataVault.isEmpty())
             id = 0;
         else
             id = dataVault.lastKey()+1;
 
         model.setId(id);
         model.setSaved(true);
         dataVault.put(id,model);
         return new Result(true);
 	}
 
 	/**
 	 * Adds the Item to the map if it already exists.  Should check before doing so.
 	 * 
 	 * @param model Item to add
 	 * @return Result of request
 	 */
 	public  Result saveModified(Item model){
         if(!model.isValid())
             return new Result(false, "Model must be valid prior to saving,");
 
         int id = model.getId();
         model.setSaved(true);
         dataVault.put(id,model);
         return new Result(true);
 	}
 }
 
