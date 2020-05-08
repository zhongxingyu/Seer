 /*
  * Created on Apr 11, 2003
  *
  */
 package org.soulspace.template.datasource.impl;
 
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.soulspace.template.datasource.IDataSource;
 import org.soulspace.template.symbols.ISymbolTable;
 import org.soulspace.template.symbols.impl.ListSymbol;
 import org.soulspace.template.symbols.impl.SymbolTable;
 
 /**
  * @author soulman
  * 
  * Transforms a bean (hierachy) to a SymbolTable.<br>
  * Calls the getter methods for all properties.
  * The following mapping is used:<br>
  * Object (except String) to MapSymbol<br>
  * Simple types (incl. String) to Scalar Symbol<br>
  * Lists to ListSymbol<br>
  * Object Array to ListSymbol
  */
 public class BeanDataSource implements IDataSource {
 
   // TODO change the instanceof checks for List to Collection to handle
   // TODO other collections as well.
   // TODO Map isn't a Collection, so it should be safe.
   
 	ISymbolTable symbolTable = new SymbolTable();
   Map<Object, ISymbolTable> cache = new HashMap<Object, ISymbolTable>();
 	boolean parsed = false;
 
 	public BeanDataSource() {
 		this.symbolTable = new SymbolTable();
 	}
 	
 	/**
 	 * Constructor
 	 * @param object
 	 */
 	public BeanDataSource(Object rootBean) {
 		this.symbolTable = parse(rootBean);
 	}	
 	
 	public void add(String name, Object bean) {
     if(bean instanceof Map) {
     	insert(symbolTable, name, bean);
     } else if(bean instanceof Collection) {
     	insert(symbolTable, name, bean);
     } else {
     	symbolTable.addNewMapSymbol(name, parse(bean));
     }
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.soulspace.dataloader.IDataSource#getSymbolTable()
 	 */
 	public ISymbolTable getSymbolTable() {
 		return symbolTable;
 	}
 
 	/**
 	 * Parse an Object into the SymbolTable
 	 * @param symbolTable
 	 * @param object
 	 * @return
 	 */
 	ISymbolTable parse(Object object) {
 		ISymbolTable symbolTable = new SymbolTable();
     if(object instanceof Map) {
     	insert(symbolTable, "MAP", object);
     } else if(object instanceof Collection) {
     	insert(symbolTable, "LIST", object);
     } else {
   		// put symbolTable into the cache first to prevent endless loops
       cache.put(object, symbolTable);
       
   		Class<? extends Object> myClass = object.getClass();
   		Method methods[] = myClass.getMethods();
   		
   		for(int i = 0; i < methods.length; i++) {
   			Method method = methods[i];
   			String methodName = method.getName();
 
   			if(method.getParameterTypes().length > 0
   					|| !Modifier.isPublic(method.getModifiers())
   					|| Modifier.isStatic(method.getModifiers())) {			
   				continue;
   			}
 
   			String symbolName;
   			if(methodName.startsWith("get") && !methodName.equals("getClass")) {
   				// standard getter
   				symbolName = methodName.substring(3);
   			} else if(methodName.startsWith("is")) {
   				// boolean getter
   				symbolName = methodName.substring(2);
   			} else {
   				// not a getter
   				continue;
   			}
 
   			Object result = null;
   			try {
   				result = method.invoke(object, (Object[]) null);
   			} catch(Exception ex) {
   				ex.printStackTrace();
   			}
 
   			// Add Symbol to SymbolTable
   			insert(symbolTable, symbolName, result);
   		}
     }
     
 		return symbolTable;
 	}
 
 	/**
 	 * 
 	 * @param listSymbol
 	 * @param object
 	 * @return
 	 */
 	boolean parse(ListSymbol listSymbol, Object object) {
 		Class<? extends Object> myClass = object.getClass();
 		Method methods[] = myClass.getMethods();
 		
 		for(int i = 0; i < methods.length; i++) {
 			Method method = methods[i];
 			String methodName = method.getName();
 
 			if(method.getParameterTypes().length > 0
 					|| !Modifier.isPublic(method.getModifiers())
 					|| Modifier.isStatic(method.getModifiers())) {			
 				continue;
 			}
 
 //			String symbolName;
 			if(methodName.startsWith("get") && !methodName.equals("getClass")) {
 //				symbolName = methodName.substring(3);
 			} else if(methodName.startsWith("is")) {
 //				symbolName = methodName.substring(2);
 			} else {
 				continue;
 			}
 
 			Object result = null;
 			try {
 				result = method.invoke(object, (Object[]) null);
 			} catch(Exception ex) {
 				ex.printStackTrace();
 			}
 
 			// insert symbol into list
 			insert(listSymbol, result);
 
 		}
 		return true;
 	}
 
 
 	public void insert(ISymbolTable symbolTable, String symbolName, Object result) {
 		// Add Symbol to SymbolTable
 		if(result instanceof String) {
 			// String
 			symbolTable.addNewStringSymbol(symbolName, (String) result);
 		} else if(result instanceof Boolean/*.TYPE*/) {
 			// boolean
 			if(((Boolean) result).booleanValue()) {
 				symbolTable.addNewStringSymbol(symbolName, "1");
 			} else {
 				symbolTable.addNewStringSymbol(symbolName, "0");							
 			}
 		}	else if(result instanceof Byte
 								|| result instanceof Short
 								|| result instanceof Integer
 								|| result instanceof Long
 								|| result instanceof Double
 								|| result instanceof Float
                 || result instanceof Character) {
 			// Number
 			symbolTable.addNewNumericSymbol(symbolName, String.valueOf(result));
 		} else if(result instanceof Void/*.TYPE*/) {
 			// Void
     } else if(result instanceof Map) {
       // Map
       ISymbolTable st = new SymbolTable();
       Iterator it = ((Map) result).keySet().iterator();
       while(it.hasNext()) {
         Object key = it.next();
        if(key != null) {
          insert(st, key.toString(), ((Map) result).get(key));        	
        }
       }
       symbolTable.addNewMapSymbol(symbolName, st);
 		} else if(result instanceof Collection) {
 			// List
 			List list = new ArrayList();
 			symbolTable.addNewListSymbol(symbolName, list);
 			ListSymbol lSymbol = (ListSymbol) symbolTable.getSymbol(symbolName);
 			Iterator it = ((Collection) result).iterator();
 			while(it.hasNext()) {
 				insert(lSymbol, it.next());
 			}
     } else if(result instanceof Object[]) {
       // Object Array
       // TODO add handling for arrays of base types
       Object[] objects = (Object[]) result; 
       List list = new ArrayList();
       symbolTable.addNewListSymbol(symbolName, list);
       ListSymbol lSymbol = (ListSymbol) symbolTable.getSymbol(symbolName);
       for(int i = 0; i < objects.length; i++) {
         insert(lSymbol, objects[i]);              
       }
 		} else if(result instanceof Object) {
 			// Object
       ISymbolTable sTable;
       if((sTable = (ISymbolTable) cache.get(result)) == null) {
         sTable = parse(result);
         // put it into cache first to prevent endless loops
 //        cache.put(result, sTable);
       }
 			symbolTable.addNewMapSymbol(symbolName, sTable);
 		}
 	}
 	
 	public void insert(ListSymbol listSymbol, Object result) {
 	
 		if(result instanceof String) {
 			// String
 			listSymbol.addNewStringSymbol((String) result);
 		} else if(result instanceof Boolean/*.TYPE*/) {
 			// boolean
 			if(((Boolean) result).booleanValue()) {
 				listSymbol.addNewStringSymbol("1");
 			} else {
 				listSymbol.addNewStringSymbol("0");							
 			}
 		}	else if(result instanceof Byte
 								|| result instanceof Short
 								|| result instanceof Integer
 								|| result instanceof Long
 								|| result instanceof Double
 								|| result instanceof Float
                 || result instanceof Character) {
 			// Number
 			listSymbol.addNewNumericSymbol(String.valueOf(result));
 		} else if(result instanceof Void/*.TYPE*/) {
 			// Void
 		} else if(Collection.class.isAssignableFrom(result.getClass())) {
 			// List
 			List list = new ArrayList();
 			listSymbol.addNewListSymbol(list);
 //      ListSymbol lSymbol = (ListSymbol) listSymbol.getSymbol(symbolName);
 			Iterator it = ((Collection) result).iterator();
 			while(it.hasNext()) {
 				// TODO change this! Don't use a SymbolTable, use a ListSymbol.
 				// Let ListSymbol implement ISymbolTable (to an extend)
 				// Maybe use an Interface hierarchy ISymbolContainer
 				// -> ISymbolTable, ISymbolList
 
 //					parse(lSymbol, it.next());
 			}
     } else if(result instanceof Object[]) {
       // Object Array
       // TODO add handling for arrays of base types
       Object[] objects = (Object[]) result; 
       List list = new ArrayList();
       listSymbol.addNewListSymbol(list);
 //      ListSymbol lSymbol = (ListSymbol) symbolTable.getSymbol(symbolName);
       for(int i = 0; i < objects.length; i++) {
         // TODO change this! Don't use a SymbolTable, use a ListSymbol.
         // Let ListSymbol implement ISymbolTable (to an extend)
         // Maybe use an Interface hierarchy ISymbolContainer
         // -> ISymbolTable, ISymbolList
 //        insert(lSymbol, objects[i]);              
       }
 
 		} else if(result instanceof Object) {
 			// Object
 			ISymbolTable sTable;
       if((sTable = (ISymbolTable) cache.get(result)) == null) {
         sTable = parse(result);
       }
 			listSymbol.addNewMapSymbol(sTable);
 		}
 
 	}
 }
