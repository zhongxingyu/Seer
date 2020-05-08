 package model.productcontainer;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Map.Entry;
 
 import model.common.IModel;
 import model.common.Vault;
 import common.Result;
 import common.util.QueryParser;
 import model.productcontainer.StorageUnit;
 
 
 /**
  * The Product class provides a way to query for ProductModels within the select data backend
  * (A Model superclass or interface could be created for this)
  * <PRE>
  * Product.find(2) // returns ProductModel with id of 2
  * </PRE>
  * Other findBy* methods may be implemented.
  */
 public class StorageUnitVault extends Vault{
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	static StorageUnitVault currentInstance;
 	
 	/**
 	 * Private constructor, for the singleton design pattern.
 	 */
 	private StorageUnitVault(){
 		currentInstance = this;
 	}
 
 	/**
 	 * Returns a reference to the only instance allowed for this class.
 	 */
 	public static synchronized StorageUnitVault getInstance(){
 		if(currentInstance == null) currentInstance = new StorageUnitVault();
 		return currentInstance;
 	}
 
     
 	/**
 	 * Returns just one StorageUnit based on the query sent in. 
 	 * If you need more than one StorageUnit returned use FindAll
 	 * 
 	 * @param attribute Which attribute should we search on for each Product
 	 * @param value What value does the column have
 	 * 
 	 */
 	public  StorageUnit find(String query)  {
 		QueryParser MyQuery = new QueryParser(query);
 
 		
 		//Do a linear Search first
 		//TODO: Add ability to search by index
 		try {
             ArrayList<StorageUnit> results = linearSearch(MyQuery,1);
             if(results.size() == 0)
                 return null;
             return results.get(0);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		
 		return null;
 	}
 	
 	
 	/**
 	 * Returns a list of StorageUnits which match the criteria
 	 * 
 	 * @param attribute 
 	 * @param value
 	 * 
 	 */
 	public  ArrayList<StorageUnit> findAll(String query) {
 		QueryParser MyQuery = new QueryParser(query);
 
 		
 		//Do a linear Search first
 		//TODO: Add ability to search by index
 		try {
 			ArrayList<StorageUnit> results = linearSearch(MyQuery,0);
 			return results;
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		return null;
 	}
 	
 	private  ArrayList<StorageUnit> linearSearch(QueryParser MyQuery,int count)
             throws IllegalAccessException, IllegalArgumentException,
             InvocationTargetException, NoSuchMethodException, SecurityException{
 		ArrayList<StorageUnit> results = new ArrayList<StorageUnit>();
 
 		String attrName = MyQuery.getAttrName();
 		String value = MyQuery.getValue();
 		
 
 		StorageUnit mySU = new StorageUnit();
 		
 		//Class associated with the product model
 		Class<? extends StorageUnit> suCls = mySU.getClass();
 		//Method we will call to get the value
 		Method method;
 		method = suCls.getMethod("get"+attrName);
 
 		
 		//Loop through entire hashmap and check values one at a time
 		for (Entry<Integer, IModel> entry : dataVault.entrySet()) {
 			mySU = (StorageUnit) entry.getValue();
 			String mySUValue; 
 			mySUValue = (String) method.invoke(mySU);
 
 		    if(mySUValue.equals(value) && !mySU.isDeleted()){
 		    	results.add(new StorageUnit(mySU));
 		    }
 		    if(count != 0 && results.size() == count )
 		    	return results;
 		}
 		return results;
 	}
 	
 	public int getLastIndex(){
		return (int)dataVault.size()+StorageUnitVault.getInstance().size();
 	}
 	/**
 	 * Checks if the model passed in already exists in the current map
 	 * - Must have a unique name
 	 * 
 	 * @param model
 	 * @return Result of the check
 	 */
 	protected  Result validateNew(StorageUnit model){
 		Result result = new Result();
 		result = checkUniqueName(model);
 		if(result.getStatus() == false)
 			return result;
 
         model.setValid(true);
 		return result;
 	}
 	
 	private  Result checkUniqueName(StorageUnit model){
         //Null check
         if(model.getName() == null)
             return new Result(false, "Name can't be null");
 
         if(model.getName() == "")
             return new Result(false, "Name can't be empty");
 
 		int size = findAll("Name = "+model.getName()).size();
 		if(size!=0)
 			return new Result(false,"Duplicate storage container name.");
 		return new Result(true);
 	}
 
     public  StorageUnit get(int id){
     	StorageUnit su = (StorageUnit)dataVault.get(id);
     	if(su == null)
     		return null;
         return new StorageUnit(su);
     }
 	/**
 	 * Checks if the model already exists in the map
 	 * 
 	 * @param model
 	 * @return Result of the check
 	 */
 	protected Result validateModified(StorageUnit model){
 		assert(model!=null);
         assert(!dataVault.isEmpty());
 		
 		//Delete current model
 		StorageUnit currentModel = (StorageUnit)dataVault.get(model.getId());
 		currentModel.delete();
 		//Validate passed in model
 		Result result = validateNew(model);
 		//Add current model back
 		currentModel.unDelete();
 		
 		if(result.getStatus() == true)
 			model.setValid(true);
         return result;
 	}
 
 	/**
 	 * Adds the StorageUnit to the map if it's new.  Should check before doing so.
 	 * 
 	 * @param model StorageUnit to add
 	 * @return Result of request
 	 */
 	protected  Result saveNew(StorageUnit model){
         if(!model.isValid())
             return new Result(false, "Model must be valid prior to saving,");
 
         int id = 0;
         if(dataVault.isEmpty() && model.productGroupVault.size() == 0 )
             id = 0;
         else
             id = dataVault.size() + 1 +model.productGroupVault.size();
 
         model.setId(id);
         model._rootParentId = id;
         model.setSaved(true);
         this.addModel(new StorageUnit(model));
         return new Result(true);
 	}
 
 	/**
 	 * Adds the StorageUnit to the map if it already exists.  Should check before doing so.
 	 * 
 	 * @param model StorageUnit to add
 	 * @return Result of request
 	 */
 	protected  Result saveModified(StorageUnit model){
         if(!model.isValid())
             return new Result(false, "Model must be valid prior to saving,");
         model.setSaved(true);
         this.addModel(new StorageUnit(model));
         return new Result(true);
 	}
 }
 
