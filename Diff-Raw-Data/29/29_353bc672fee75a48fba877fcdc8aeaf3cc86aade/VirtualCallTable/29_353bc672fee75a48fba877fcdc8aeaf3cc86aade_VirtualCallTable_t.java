 /**
  *  Andromeda, a galaxy extension language.
  *  Copyright (C) 2010 J. 'gex' Finis  (gekko_tgh@gmx.de, sc2mod.com)
  * 
  *  Because of possible Plagiarism, Andromeda is not yet
  *	Open Source. You are not allowed to redistribute the sources
  *	in any form without my permission.
  *  
  */
 package com.sc2mod.andromeda.classes;
 
 import java.util.ArrayList;
 
 import com.sc2mod.andromeda.environment.AbstractFunction;
 import com.sc2mod.andromeda.environment.Environment;
 import com.sc2mod.andromeda.environment.Method;
 import com.sc2mod.andromeda.environment.types.Class;
 import com.sc2mod.andromeda.environment.types.RecordType;
 
 /**
  * A Virtual Call Table (VCT) contains information for calling virtual methods.
  * 
  * @author J. 'gex' Finis
  *
  */
 public class VirtualCallTable {
 	
 
 	/**
 	 * Generates a VCT for each class in an environment and links it
 	 * to the respective class
 	 * @param env the environment for whose classes to generate VCTs for
 	 */
 	public static void generateVCTs(Environment env){
 		for(Class clazz : env.typeProvider.getClasses()){
 			//Create a class table for all top classes, these will
 			//then recursivly create VCTs for child classes.
 			if(clazz.isTopClass()) new VirtualCallTable(clazz, env);
 		}
 	}
 	
 	/**
 	 * The actual virtual call table.
 	 * The information is contained in the methods in the table.
 	 */
 	public ArrayList<AbstractFunction> table = new ArrayList<AbstractFunction>();
 	
 	/**
 	 * The VCT of the superclass, if the class to which this VCT
 	 * belongs has one.
 	 */
 	VirtualCallTable superTable;
 	
 	public Class clazz;
 	
 	/**
 	 * VCTs of direct decendant classes.
 	 */
 	public ArrayList<VirtualCallTable> subTables = new ArrayList<VirtualCallTable>(4);
 	
 	/**
 	 * Constructor. Creates the call table and the call tables for child classes
 	 * @param clazz the class for which to create the table
 	 * @param env the environment
 	 */
 	private VirtualCallTable(Class clazz, Environment env){
 		this.clazz = clazz;
 		
 		//Set this as table of the class
 		clazz.setVirtualCallTable(this);
 		
 		//Add the table from the superclass
 		Class superClass = clazz.getSuperClass();
 		if(superClass != null){
 			superTable = superClass.getVirtualCallTable();
 			superTable.subTables.add(this);
 			table.addAll(superTable.table);		
 		}
 		
 		ArrayList<AbstractFunction> methods = clazz.getMethods().getMyMethods();
 		int size = methods.size();
 		for(int i=0;i<=size;i++){
 			AbstractFunction m;
 			if(i<size){
 				//First we test all methods
 				m = methods.get(i);
 			} else {
 				//Lastly the destructor
 				m = clazz.getDestructor();
 				if(m == null) break;
 				
 				//No destructor added?
 				if(m.getContainingType() != clazz) break;
 				
 			}
 			//Methods without body and static methods and methods that are not called virtually are ignored
 			if(m.isStatic()||!m.isCalledVirtually()) continue;
 								
 			AbstractFunction overrides = m.getOverridenMethod();
 			int callIndex;
 			int tableIndex;
 			if(overrides != null && overrides.isCalledVirtually()){
 				//This method is already in the call table!
 				tableIndex = overrides.getVirtualTableIndex();
 				if(m.isAbstract()){					
 					callIndex = overrides.getCurVirtualCallChildIndex();
 				} else {				
 					callIndex = superTable.incCallIndex(tableIndex);
 				}				
 				
 				//Set in table
 				table.set(tableIndex, m);
 			} else {
 				//This method is new in the call table!
 				tableIndex = table.size();
 				if(m.isAbstract()){					
 					callIndex = -1;
 				} else {				
 					callIndex = 0;
 				}
 				
 				//Add to table
 				table.add(m);	
 				
 				env.registerMaxVCTSize(table.size());
 			}
 			//Set the indices for the method
 			m.setVirtualCallIndex(callIndex);
 			m.setVirtualTableIndex(tableIndex);	
 		}
 		
 		//Generate tables for subclasses
 		for(RecordType r : clazz.getDecendants()){
 			new VirtualCallTable((Class) r, env);
 		}
 	}
 
 	/**
 	 * Gets the next call index and increments the
 	 * call index for all super methods.
 	 * @param tableIndex for which index in the table to increment the call index
 	 * @return the incremented call index
 	 */
 	private int incCallIndex(int tableIndex) {
 		if(superTable != null){
 			superTable.incCallIndex(tableIndex);
 		}
 		if(table.size()<=tableIndex) return -1;
 		AbstractFunction m = table.get(tableIndex);
 		
		//If the method is identic with the method in the top table, we do not increment the index
		//(since we have done it already in the top table in the recursive call)
 		if(superTable != null && superTable.table.get(tableIndex)==m) return m.getCurVirtualCallChildIndex();
 		return table.get(tableIndex).getNextVirtualCallChildIndex();
 	}
 
 	/**
 	 * Retrieves the actual table
 	 * @return the vct table
 	 */
 	public ArrayList<AbstractFunction> getTable() {
 		return table;
 	}
 
 
 }
