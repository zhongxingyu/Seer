 package vb.eindopdracht.symboltable;
 
 import java.util.*;
 import java.util.Map.Entry;
 
 @SuppressWarnings("hiding")
 public class SymbolTable<Entry extends IdEntry> {
 
 	protected ArrayList<SymbolTableMap<Entry>> symbolMapList;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @ensures this.currentLevel() == -1
 	 */
 	public SymbolTable() {
 		symbolMapList = new ArrayList<SymbolTableMap<Entry>>();
 	}
 
 	/**
 	 * Opens a new scope.
 	 * 
 	 * @ensures this.currentLevel() == old.currentLevel()+1;
 	 */
 	public void openScope() {
 		openScope(false);
 	}
 
 	/**
 	 * Opens a new scope.
 	 * 
 	 * @ensures this.currentLevel() == old.currentLevel()+1;
 	 */
 	public void openScope(boolean functionalScope) {
 		symbolMapList.add(new SymbolTableMap<Entry>());
 		symbolMapList.get(this.currentLevel()).setFunctionalScope(functionalScope);
 	}
 	
 	/**
 	 * Return the size of the LB stack
 	 * @return
 	 */
 	public int getCurrentLocalBaseSize() {
 		return symbolMapList.get(this.currentLevel()).getLbSize();
 	}
 
 	/**
 	 * Closes the current scope. All identifiers in the current scope will be
 	 * removed from the SymbolTable.
 	 * 
 	 * @requires old.currentLevel() > -1;
 	 * @ensures this.currentLevel() == old.currentLevel()-1;
 	 */
 	public SymbolTableMap<Entry> closeScope() {
		SymbolTableMap<Entry> stm = symbolMapList.get(getCurrentLocalBaseSize());
 		symbolMapList.remove(this.currentLevel());
 		return stm;
 	}
 	
 	public boolean isFunctionalScope(int level) {
 		return symbolMapList.get(level).isFunctionalScope();
 	}
 	
 	/**
 	 * Returns the current scope level.
 	 */
 	public int currentLevel() {
 		return symbolMapList.size() - 1;
 	}
 
 	/**
 	 * Enters an id together with an entry into this SymbolTable using the
 	 * current scope level. The entry's level is set to currentLevel().
 	 * 
 	 * @requires id != null && id.length() > 0 && entry != null;
 	 * @ensures this.retrieve(id).getLevel() == currentLevel();
 	 * @throws SymbolTableException
 	 *             when there is no valid current scope level, or when the id is
 	 *             already declared on the current level.
 	 */
 	public void enter(String id, Entry entry) throws Exception {
 		if (this.currentLevel() > -1
 				&& !symbolMapList.get(this.currentLevel()).getMap().containsKey(id)) {
 			entry.setLevel(this.currentLevel());
 			symbolMapList.get(this.currentLevel()).add(id, entry);
 		} else {
 			throw new Exception("On this level (" + this.currentLevel() + "), " + 
 								 id + " is already declared.");
 		}
 	}
 
 	/**
 	 * Get the Entry corresponding with id whose level is the highest; in other
 	 * words, that is defined last.
 	 * 
 	 * @return Entry of this id on the highest level null if this SymbolTable
 	 *         does not contain id
 	 */
 	public Entry retrieve(String id) {
 		for (int i = this.currentLevel(); i > -1; i--) {
 			HashMap<String, Entry> tempHM = symbolMapList.get(i).getMap();
 			if (tempHM.containsKey(id))
 				return tempHM.get(id);
 		}
 		return null;
 	}
 	
 	/**
 	 * Prints the complete SymbolTable at the moment in time;
 	 */
 	public String toString() {
 		StringBuilder sb = new StringBuilder();
 		sb.append("\n\n====== Current symbolTable ======= \n");
 		sb.append("level | entryType                |\n");
 		sb.append("----------------------------------\n");
 		for(SymbolTableMap<Entry> hm : symbolMapList) {
 			for(String str : hm.getMap().keySet()) {
 				sb.append("   " + hm.getMap().get(str).getLevel() + "  |" + str + "\n");
 			}
 		}
 		sb.append("\n*Keep in mind that the closed levels are removed.");
 		return sb.toString();
 	}
 	
 	/**
 	 * Fixt de addressen van variabeleparameters
 	 * in het geval van subprocedures
 	 */
 	public void goDeeper() throws Exception {
 		for (int i = this.currentLevel(); i > -1; i--) {
 			Iterator<Map.Entry<String, Entry>> it = symbolMapList.get(i).getMap().entrySet().iterator();
 		    while (it.hasNext()) {
 		    	Map.Entry<String, Entry> pairs = (Map.Entry<String, Entry>)it.next();
 		        IdEntry ie = pairs.getValue();
 		        String address = ie.getAddress();
 		        try {
 		        	String addressEnd = address.substring(ie.getAddress().length()-4, ie.getAddress().length());
 			        
 			        if(addressEnd.equals("[LB]"))
 			        	ie.setAddress(address.substring(0, ie.getAddress().length()-4) + "[L1]");
 			        else if(addressEnd.equals("[L1]"))
 			        	ie.setAddress(address.substring(0, ie.getAddress().length()-4) + "[L2]");
 			        else if(addressEnd.equals("[L2]"))
 			        	ie.setAddress(address.substring(0, ie.getAddress().length()-4) + "[L3]");
 			        else if(addressEnd.equals("[L3]"))
 			        	ie.setAddress(address.substring(0, ie.getAddress().length()-4) + "[L4]");
 			        else if(addressEnd.equals("[L4]"))
 			        	ie.setAddress(address.substring(0, ie.getAddress().length()-4) + "[L5]");
 			        else if(addressEnd.equals("[L5]"))
 			        	ie.setAddress(address.substring(0, ie.getAddress().length()-4) + "[L6]");
 			        else if(addressEnd.startsWith("[L") && addressEnd.endsWith("]"))
 			        	throw new Exception("We can't go deeper, I blame " + pairs.getKey());
 		        }
 		        catch(Exception e) {
 		        	//The encountered entry is a constant and therefore has no address
 		        }
 		    }
 		}
 	}
 	
 	/**
 	 * Fixt de addressen van variabeleparameters
 	 * in het geval van subprocedures
 	 */
 	public void goShallower() throws Exception {
 		for (int i = this.currentLevel(); i > -1; i--) {
 			Iterator<Map.Entry<String, Entry>> it = symbolMapList.get(i).getMap().entrySet().iterator();
 		    while (it.hasNext()) {
 		    	Map.Entry<String, Entry> pairs = (Map.Entry<String, Entry>)it.next();
 		        IdEntry ie = pairs.getValue();
 		        String address = ie.getAddress();
 		        try {
 		        	String addressEnd = address.substring(ie.getAddress().length()-4, ie.getAddress().length());
 			        if(addressEnd.equals("[L1]"))
 			        	ie.setAddress(address.substring(0, ie.getAddress().length()-4) + "[LB]");
 			        else if(addressEnd.equals("[L2]"))
 			        	ie.setAddress(address.substring(0, ie.getAddress().length()-4) + "[L1]");
 			        else if(addressEnd.equals("[L3]"))
 			        	ie.setAddress(address.substring(0, ie.getAddress().length()-4) + "[L2]");
 			        else if(addressEnd.equals("[L4]"))
 			        	ie.setAddress(address.substring(0, ie.getAddress().length()-4) + "[L3]");
 			        else if(addressEnd.equals("[L5]"))
 			        	ie.setAddress(address.substring(0, ie.getAddress().length()-4) + "[L4]");
 			        else if(addressEnd.equals("[L6]"))
 			        	ie.setAddress(address.substring(0, ie.getAddress().length()-4) + "[L5]");
 			        else if(addressEnd.equals("[LB]"))
 			        	throw new Exception("We can't go shallower, I blame " + pairs.getKey());
 		        }
 		        catch(Exception e) {
 		        	//The encountered entry is a constant and therefore has no address
 		        }
 		    }
 		}
 	}
 	
 	/**
 	 * Een SymbolTableMap is een item in de SymbolTable. 
 	 * Dit item is een scope (een level binnen een programma).
 	 *
 	 * @param <Entry>
 	 */
 	public class SymbolTableMap<Entry extends IdEntry> {
 		private HashMap<String, Entry> map;
 		private int lbSize;
 		private boolean functionalScope;
 		
 		/**
 		 * Verkrijg de map met de waardes in de symtab
 		 * @return
 		 */
 		public HashMap<String, Entry> getMap() {
 			return map;
 		}
 		
 		/**
 		 * Voeg een waarde toe aan de symtab
 		 * @param id
 		 * @param entry
 		 */
 		public void add(String id, Entry entry) {
 			if(!(entry instanceof ProcEntry) && !(entry instanceof FuncEntry) && !entry.isVarparam())
 				lbSize++;
 			map.put(id, entry);
 		}
 		
 		/**
 		 * Verkrijg de grootte van de local base (StackBase in geval currentlevel = 0)
 		 * @return
 		 */
 		public int getLbSize() {
 			return lbSize;
 		}
 		
 		/**
 		 * Houdt bij of deze scope gepopt of gereturned moet worden
 		 * @param functionalScope
 		 */
 		public void setFunctionalScope(boolean functionalScope) {
 			this.functionalScope = functionalScope;
 		}
 		
 		/**
 		 * Returnt of deze scope bij een procedure hoort
 		 * @return
 		 */
 		public boolean isFunctionalScope() {
 			return this.functionalScope;
 		}
 		
 		/**
 		 * Maak een nieuwe SymbolTableMap (variabelen op een eigen level in de code).
 		 */
 		public SymbolTableMap() {
 			lbSize = 0;
 			map = new HashMap<String, Entry>();
 		}
 		
 		/**
 		 * Returns a string representing the map, listing all entries
 		 */
 		public String toString() {
 			String result = lbSize + " ";
 			Iterator<Map.Entry<String, Entry>> it = map.entrySet().iterator();
 		    while (it.hasNext()) {
 		        Map.Entry<String, Entry> pairs = (Map.Entry<String, Entry>)it.next();
 		        result += pairs.getKey() + ": " + pairs.getValue().getClass().toString() + "\n";
 		    }
 		    return result;
 		}
 	}
 }
