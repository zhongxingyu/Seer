 package forrest.main;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Stack;
 
 import fire.ForrestFireException;
 
 /**
  * Keeps track of all identifiers used in a Forrest application
  */
 public class IdStore {
     // A set of currently declared identifiers.
     private Stack<HashMap<String, StoreItem>> liveIdSet = new Stack<HashMap<String, StoreItem>>();
     
     public IdStore() {
     }
     	
 	/** Opens a new scope. */
     public void openScope()  {
         liveIdSet.push(new HashMap<String, StoreItem>());
     }
 
     /** Closes the current scope. */
     public void closeScope() {
         liveIdSet.pop();
     }
 
     /** Returns the current scope level. */
     public int currentLevel() {
         return liveIdSet.size() - 1;
     }
 
     /** Returns the current scope level. */
     public Map<String, StoreItem> currentScope() {
         return liveIdSet.peek();
     }
     
     /** 
      * Declare an identifier
      * @param t - the identifier tree node 
      * @throws ForrestFireException if the identifier has been used for any 
      *	constant or if the identifier already exists in the current scope.
      */
     protected void declareId(ForrestTree t) throws ForrestFireException {
     	ForrestTree id = (ForrestTree) t.getChild(0);
     	ForrestTree type = (ForrestTree) t.getChild(1);
 		declare(id.getText(), Type.valueOf(type.getText().toUpperCase())); //TODO workaround for java, explain
     }
     
     /**
      * Declares the identifier for the current scope
      * @param id - the identifier to declare
      */
     protected void declare(String id, Type type) {
     	StoreItem entry = new StoreItem(type, getCurrentScopeSize(), currentLevel());
 		this.currentScope().put(id, entry);
     }
     
     
     /**
      * Declare a constant
      * @param t - the id of the constant
      * @throws ForrestFireException if the identifier has been used already or
      	if the constant is declared inside a nested scope
      */
     protected void declareConst(ForrestTree t) throws ForrestFireException {
     	ForrestTree id = (ForrestTree) t.getChild(0);
     	ForrestTree expr = (ForrestTree) t.getChild(1);
 		declare(id.getText(), expr.getReturnType());
     }
     
     /**
      * Set the type for a ForrestTree node that is an identifier
      * @param id - ForrestTree that needs its type set 
      */
     public StoreItem getItem(String id)  {
     	StoreItem storeItem = null;
     	for (int i = currentLevel(); storeItem == null && i >= 0; i--) {
     		Map<String, StoreItem> scopeSet = liveIdSet.get(i);
     		if (scopeSet.containsKey(id)) {
     			storeItem = scopeSet.get(id);
     		}
     	}
     	return storeItem;
     }
     
     /**
      * Set the type for a ForrestTree node that is an identifier
      * @param id - ForrestTree that needs its type set 
      */
     public int getAddress(String id)  {
    	StoreItem item = this.getItem(id);
     	int addrMod = 0;
    	for (int i = 0; i < liveIdSet.size(); i++) {
    		if (i != item.getScopeLevel()) {
        		HashMap<String, StoreItem> scope = liveIdSet.get(i);
     			addrMod += scope.size();
     		}
     	}
     	return this.getItem(id).getAddress() + addrMod;
     }
     
     /**
      * @return the number of declared identifiers in the current scope
      */
 	public int getCurrentScopeSize() {
 		return currentScope().size();
 	}
 }
