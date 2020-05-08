 /*
  Copyright (c) 2012 Lee Barney
  Permission is hereby granted, free of charge, to any person obtaining a 
  copy of this software and associated documentation files (the "Software"), 
  to deal in the Software without restriction, including without limitation the 
  rights to use, copy, modify, merge, publish, distribute, sublicense, 
  and/or sell copies of the Software, and to permit persons to whom the Software 
  is furnished to do so, subject to the following conditions:
 
  The above copyright notice and this permission notice shall be 
  included in all copies or substantial portions of the Software.
 
 
  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
  INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A 
  PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT 
  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
  CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE 
  OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 
 
  */
 package org.quickconnectfamily.kvkit.orm;
 
 import java.lang.ref.WeakReference;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.lang.reflect.ParameterizedType;
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Queue;
 import java.util.WeakHashMap;
 
 
 import android.app.Application;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Looper;
 
 public class KVKitORM {
 	private WeakHashMap<Thread,Queue<ORMStorable>> storableQueues = null;
 	private HashMap<String,WeakReference<ORMStorable>>loadedStorables = null;
 	private KVKitOpenHelper theHelper = null;
 
 	static KVKitORM theKVKit = null;
 
 	static{
 		theKVKit = new KVKitORM();
 		theKVKit.loadedStorables = new HashMap<String, WeakReference<ORMStorable>>();
 		theKVKit.storableQueues = new WeakHashMap<Thread,Queue<ORMStorable>>();
 	}
 
 	public static KVKitORM getInstance(){
 		return theKVKit;
 	}
 	/*
 	 * This method should be called only from the Application's onCreate method.
 	 */
 	public void initialize(Application theApplication, String aName, int aVersion) throws InitializationException{
 		if(theHelper == null){
 			/*
 			 *   Initialize the backing database
 			 */
 			theHelper = new KVKitOpenHelper(theApplication, aName,aVersion); 
 			/*
 			 * Get the list of already existent tables.
 			 */
 			boolean storableTableExists = false;
 			SQLiteDatabase theDb = theHelper.getReadableDatabase();
 			Cursor tableCursor = theDb.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
 			while(tableCursor.moveToNext()){
 				String tableName = tableCursor.getString(0);
 				//System.out.println("table found: "+tableName);
 				if(tableName.equals("parent_child")){
 					storableTableExists = true;
 				}
 				else if(!tableName.equals("android_metadata") && !tableName.equals("attribute")
 						&& !tableName.equals("child_element") && !tableName.equals("parent_child")){
 					//System.out.println("tableName: "+tableName);
 					String className = tableName.replace('_', '.');
 					//System.out.println("className: "+className);
 					try {
 						Class<?> aClass = Class.forName(className);
 						ORMStorable.addExistingTable(aClass);
 					} catch (ClassNotFoundException e) {
 						throw new InitializationException(e);
 					}
 				}
 			}
 			if(!storableTableExists){
 				/*
 				 *
 				 * 													Table						Table
 				 * 											child_element			parent_child
 				 *											id (TEXT) primary key		parent_fk (TEXT) primary key
 				 *											text_value (TEXT)			child_fk  (TEXT) primary key
 				 * 											number_value (NUMBER) 		attribute_name (TEXT)
 				 * 											array_order (NUMBER)		attribute_type (TEXT)	
 				 * 																		map_key (TEXT)
 				 * 											
 				 */
 
 				/*
 				 * 														Table							
 				 * 													Storable_Table	(created with the name of the Storable class)		
 				 * 												id (TEXT) primary key		
 				 * 																			
 				 * 																			
 				 */
 				theDb.execSQL("CREATE TABLE parent_child(parent_fk TEXT NOT NULL, child_fk TEXT NOT NULL, attribute_name TEXT NOT NULL, attribute_type TEXT NOT NULL, map_key TEXT, PRIMARY KEY(parent_fk,child_fk, attribute_name))");
 
 				theDb.execSQL("CREATE TABLE child_element(id TEXT NOT NULL PRIMARY KEY, text_value TEXT, number_value NUMBER, array_order NUMBER)");
 			}
 		}
 		else{
 			throw new InitializationException("KVKit Already Initialized");
 		}
 	}
 
 	private ORMStorable findExistingStorable(String aUUID){
 		ORMStorable foundStorable = null;
 		WeakReference<ORMStorable> aReference = this.loadedStorables.get(aUUID);
 		if(aReference != null){
 			foundStorable = aReference.get();
 		}
 		return foundStorable;
 	}
 
 	private void addExistingStorable(ORMStorable aStorable) {
 		WeakReference<ORMStorable> aReference = new WeakReference<ORMStorable>(aStorable);
 		this.loadedStorables.put(aStorable.getUUID(), aReference);
 	}
 
 	public void beginMultiStore(){
 		Thread curThread = Thread.currentThread();
 		Queue<ORMStorable> aQueue = this.storableQueues.get(curThread);
 		if(aQueue == null){
 			aQueue = new LinkedList<ORMStorable>();
 			this.storableQueues.put(curThread, aQueue);
 		}
 	}
 
 	public void addToMultiStore(ORMStorable aStorable) {
 		Queue<ORMStorable> aQueue = this.storableQueues.get(Thread.currentThread());
 		if(aQueue == null){
 			beginMultiStore();
 		}
 		aQueue.add(aStorable);
 	}
 
 	public void commitMultiStore() throws KVKitORMException, KVKitClassConfigurationException {
 		if (Looper.myLooper() != null && Looper.myLooper() == Looper.getMainLooper()) {
 			throw new KVKitOnMainThreadException();
 		}
 		Thread curThread = Thread.currentThread();
 		Queue<ORMStorable> aQueue = this.storableQueues.get(curThread);
 		if(aQueue == null || aQueue.size() == 0){
 			return;
 		}
 		//start a transaction
 		SQLiteDatabase theDb = theHelper.getWritableDatabase();
 		theDb.beginTransaction();
 		Exception failedException = null;
 		try{
 			while(aQueue.size() > 0){
 				ORMStorable aStorable = aQueue.remove();
 				aStorable.store(theDb);
 			}
 			theDb.setTransactionSuccessful();
 		}
 		catch(Exception e){
 			failedException = e;
 		}
 		theDb.endTransaction();//does a commit or rollback
 		// after the transaction is successful delete the queue.
 		this.storableQueues.remove(curThread);
 		if(failedException != null){
 			if(failedException.getClass() == KVKitORMException.class){
 				throw (KVKitORMException)failedException;
 			}
 			else if(failedException.getClass() == KVKitClassConfigurationException.class){
 				throw (KVKitClassConfigurationException)failedException;
 			}
 			else{
 				throw new KVKitORMException(failedException);
 			}
 		}
 	}
 
 	public void cleanupMultiStore(Thread aThread){
 		this.storableQueues.remove(aThread);
 	}
 
 	public void store(ORMStorable aStorable) throws KVKitORMException, KVKitClassConfigurationException{
 		if (Looper.myLooper() != null && Looper.myLooper() == Looper.getMainLooper()) {
 			throw new KVKitOnMainThreadException();
 		}
 		SQLiteDatabase theDb = theHelper.getWritableDatabase();
 		Exception failedException = null;
 		//System.out.println("about to start transaction");
 		theDb.beginTransaction();
 		try{
 			aStorable.store(theDb);
 			theDb.setTransactionSuccessful();
 			addExistingStorable(aStorable);
 			//System.out.println("individual store succeeded");
 		}
 		catch(Exception e){
 			//System.out.println("individual store failed");
 			failedException = e;
 		}
 		theDb.endTransaction();//does a commit or rollback
 		//System.out.println("transaction complete");
 		if(failedException != null){
 			if(failedException.getClass() == KVKitORMException.class){
 				throw (KVKitORMException)failedException;
 			}
 			else if(failedException.getClass() == KVKitClassConfigurationException.class){
 				throw (KVKitClassConfigurationException)failedException;
 			}
 			else{
 				throw new KVKitORMException(failedException);
 			}
 		}
 	}
 
 	public void remove(ORMStorable aStorable) throws KVKitORMException{
 		remove(aStorable, false);
 	}
 
 	public void remove(ORMStorable aStorable, boolean isTemplate) throws KVKitORMException{
 		if (Looper.myLooper() != null && Looper.myLooper() == Looper.getMainLooper()) {
 			throw new KVKitOnMainThreadException();
 		}
 		SQLiteDatabase theDb = theHelper.getWritableDatabase();
 		//System.out.println("remove KVKit storable");
 		if(!isTemplate){
 			//System.out.println("removing defined storable");
 			aStorable.remove(theDb);
 		}
 		else{
 			//System.out.println("using template");
 			//query the objects to remove
 			ArrayList<ORMStorable> found = this.get(aStorable, null);
 			KVKitORMException failedException = null;
 			//System.out.println("about to start transaction");
 			theDb.beginTransaction();
 			try{
 				for(ORMStorable storableToRemove : found){
 					storableToRemove.remove(theDb);
 					WeakReference<ORMStorable> removed = this.loadedStorables.get(aStorable.getUUID());
					if(removed.get() == null){
 						this.loadedStorables.remove(aStorable.getUUID());
 					}
 				}
 				theDb.setTransactionSuccessful();
 			}
 			catch(KVKitORMException e){
 				failedException = e;
 			}
 			theDb.endTransaction();//does a commit or rollback
 			//System.out.println("transaction complete");
 			if(failedException != null){
 				throw failedException;
 			}
 		}
 	}
 
 
 	//remove any found
 	public int remove(Class<?> aType, String keyPath, String comparison) throws BadKeyPathException, KVKitORMException{
 		if (Looper.myLooper() != null && Looper.myLooper() == Looper.getMainLooper()) {
 			throw new KVKitOnMainThreadException();
 		}
 		int numRemoved = 0;
 		validateComparitor(comparison);
 		ArrayList<ORMStorable> foundStorables = this.get(aType, keyPath, comparison);
 		SQLiteDatabase theDb = theHelper.getWritableDatabase();
 		theDb.beginTransaction();
 		Exception issue = null;
 		try{
 			for(ORMStorable found : foundStorables){
 				found.remove(theDb);
 			}
 			theDb.setTransactionSuccessful();
 		}
 		catch(Exception e){
 			issue  = e;
 		}
 		theDb.endTransaction();
 		if(issue != null){
 			throw new KVKitORMException(issue);
 		}
 		numRemoved = foundStorables.size();
 		return numRemoved;
 	}
 	private void validateComparitor(String aComparitor) throws BadKeyPathException {
 		if(aComparitor != null){
 			aComparitor = aComparitor.trim();
 			if(aComparitor.length() == 0){
 				aComparitor = null;
 			}
 			else{
 				String upperedComparison = aComparitor.toUpperCase();
 				char firstChar = upperedComparison.charAt(0);
 				switch(firstChar){
 				case '=': case '<': case '>': case '!': case 'L': case 'B': case 'I':
 					break;
 				default:
 					throw new BadKeyPathException("ERROR: The comparision string "+aComparitor+" does not begin with a valid comparitor.");
 				}
 			}
 		}
 	}
 
 	//loads an attribute of the passed storable from the db
 	protected void load(ORMStorable aStorable, Field anAttribute) throws KVKitORMException{
 
 		SQLiteDatabase theDb = theHelper.getWritableDatabase();
 		KVKitORMException failedException = null;
 		theDb.beginTransaction();
 		try{
 			aStorable.load(theDb, anAttribute);
 			theDb.setTransactionSuccessful();
 		}
 		catch(Exception e){
 			failedException = new KVKitORMException(e);
 		}
 		theDb.endTransaction();//does a commit or rollback
 		if(failedException != null){
 			throw failedException;
 		}
 	}
 	//get all objects of type storableType that have a keyPath with the value.
 	public ArrayList<ORMStorable> get(Class<?> aType, String keyPath, String comparitor) throws BadKeyPathException, KVKitORMException{
 		if (Looper.myLooper() != null && Looper.myLooper() == Looper.getMainLooper()) {
 			throw new KVKitOnMainThreadException();
 		}
 		if( !ORMStorable.class.isAssignableFrom(aType)){
 			throw new KVKitORMException("ERROR: Invalid class type. Class "+ aType.getCanonicalName()+" does not inherit from ORMStorable.");
 		}
 		if(keyPath == null || keyPath.trim().length() == 0 || comparitor == null || comparitor.trim().length() == 0){
 			throw new BadKeyPathException("ERROR: Both the keypath and the comparitor must not be null and not be empty strings.");
 		}
 		validateComparitor(comparitor);
 		LinkedList<String>attributeNameQueue = new LinkedList<String>();
 		keyPath = keyPath.trim();
 		String[] pathElements = keyPath.split("\\.");
 
 		for(String element : pathElements){
 			attributeNameQueue.add(element);
 		}
 
 		/*
 		 * Person							Address
 		 * 	ArrayList<Address>addresses		String[] streets
 		 * 
 		 * Tables
 		 * Person		Address			parent_child		child_element
 		 * 
 		 *
 		 *
 		 * 		Table		Table							Table						Table
 		 * 	  Person		Address						child_element				parent_child
 		 *		id (TEXT)	id(TEXT)				id (TEXT) primary key		parent_fk (TEXT) primary key
 		 *											text_value (TEXT)			child_fk  (TEXT) primary key
 		 * 											number_value (NUMBER) 		attribute_name (TEXT)
 		 * 											array_order (NUMBER)		attribute_type (TEXT)	
 		 * 																		map_key (TEXT)
 		 * 
 		 * 
 		 */
 
 		StringBuilder sqlBuilder = new StringBuilder();
 
 		int numForAlias = 1;
 		int currentChar = 97;
 		String alias = (""+(char)currentChar) +numForAlias;
 		sqlBuilder.append("SELECT ");
 		sqlBuilder.append(alias);
 		sqlBuilder.append(".* FROM ");
 		String tableName = aType.getCanonicalName().replace('.', '_');
 		sqlBuilder.append(tableName);
 		sqlBuilder.append(" ");
 		sqlBuilder.append(alias);
 
 		Class<?>currentClass = aType;
 		while(!attributeNameQueue.isEmpty()){
 			String attributeName = attributeNameQueue.remove();
 			Class<?> attributeClass = null;
 			Field pathField = null;
 			try {
 				pathField = currentClass.getDeclaredField(attributeName);
 			} catch (NoSuchFieldException e) {
 				throw new BadKeyPathException("ERROR: the field named "+attributeName+" doesn't exist in the class "+currentClass.getCanonicalName());
 			}
 			attributeClass = pathField.getType();
 			if(attributeClass.isArray()){
 				attributeClass = attributeClass.getComponentType();
 			}
 			else if(Collection.class.isAssignableFrom(attributeClass)){
 				ParameterizedType collectionType = (ParameterizedType) pathField.getGenericType();
 				attributeClass = (Class<?>) collectionType.getActualTypeArguments()[0];
 			}
 			else if(Map.class.isAssignableFrom(attributeClass)){
 				ParameterizedType mapType = (ParameterizedType) pathField.getGenericType();
 				attributeClass = (Class<?>) mapType.getActualTypeArguments()[1];
 			}
 			if(ORMStorable.class.isAssignableFrom(attributeClass)){
 				/*
 				 * add to builder 
 				 */
 				//INNER JOIN Address a ON pc1.child_fk = a.id
 				int oldAliasNum = numForAlias;
 				numForAlias++;
 				String oldAlias = alias;
 				alias = (""+(char)currentChar++) + (numForAlias);
 				sqlBuilder.append(" INNER JOIN ");
 				sqlBuilder.append(attributeClass.getCanonicalName().replace('.', '_'));
 				sqlBuilder.append(" ");
 				sqlBuilder.append(alias);
 				sqlBuilder.append(" ON pc");
 				sqlBuilder.append(oldAliasNum);
 				sqlBuilder.append(".child_fk = ");
 				sqlBuilder.append(alias);
 				sqlBuilder.append(".id");
 
 				//INNER JOIN parent_child pc ON a.id = pc.parent_fk
 				sqlBuilder.append(" INNER JOIN parent_child pc");
 				sqlBuilder.append(oldAliasNum);
 				sqlBuilder.append(" ON ");
 				sqlBuilder.append(oldAlias);
 				sqlBuilder.append(".id = pc");
 				sqlBuilder.append(oldAliasNum);
 				sqlBuilder.append(".parent_fk");
 
 			}
 			else if(!attributeClass.isArray()){
 				if(!attributeNameQueue.isEmpty()){
 					throw new BadKeyPathException("ERROR: the field named "+attributeName
 							+" is not the last element of the keypath. Only arrays,collections, maps, and Storables can be used as intermediary path elements.");
 				}
 				sqlBuilder.append(" WHERE ");
 				sqlBuilder.append(alias);
 				sqlBuilder.append(".");
 				sqlBuilder.append(attributeName);
 				sqlBuilder.append(comparitor);
 			}
 			currentClass = attributeClass;
 		}
 		String sql = sqlBuilder.toString();
 
 
 		SQLiteDatabase theDb = this.theHelper.getReadableDatabase();
 		Cursor theCursor = theDb.rawQuery(sql, null);
 		HashSet<ORMStorable> results = new HashSet<ORMStorable>();
 		try {
 			while(theCursor.moveToNext()){
 				@SuppressWarnings("unchecked")
 				ORMStorable aStorable = KVKitORM.getInstance().buildStorableFromRecord(theCursor, (Class<? extends ORMStorable>) aType);
 				ORMStorable existingStorable = this.findExistingStorable(aStorable.getUUID());
 				if(existingStorable == null){
 					this.addExistingStorable(aStorable);
 				}
 				else{
 					aStorable = existingStorable;
 				}
 				results.add(aStorable);
 			}
 		} catch (Exception e) {
 			throw new KVKitORMException(e);
 		}
 
 
 		return new ArrayList<ORMStorable>(results);
 	}
 
 	public ArrayList<ORMStorable> get(ORMStorable aTemplate, String orderByAttributeName) throws KVKitORMException{
 		if (Looper.myLooper() != null && Looper.myLooper() == Looper.getMainLooper()) {
 			throw new KVKitOnMainThreadException();
 		}
 		HashMap<String,Field>instanceFields = new HashMap<String,Field>();
 		SQLiteDatabase theDb = theHelper.getReadableDatabase();
 
 
 		ArrayList<Field> ignoreFields = new ArrayList<Field>();
 		try {
 			Field hiddenStorableField = ORMStorable.class.getDeclaredField("tablesExist");
 			ignoreFields.add(hiddenStorableField);
 		} catch (NoSuchFieldException e) {
 			throw new KVKitORMException(e);
 		}
 		Field theUUIDField = null;
 		try {
 			theUUIDField = ORMStorable.class.getDeclaredField("theUUID");
 			instanceFields.put("theUUID", theUUIDField);
 		} catch (NoSuchFieldException e1) {
 			throw new KVKitORMException(e1);
 		}
 
 		ArrayList<String> bindList = new ArrayList<String>();
 		int numForAlias = 1;
 		int currentChar = 97;
 		ArrayList<String> inheritanceList = new ArrayList<String>();
 		StringBuilder whereBuilder = null;
 		StringBuilder selectBuilder = new StringBuilder();
 		StringBuilder aliasBuilder = new StringBuilder();
 		StringBuilder fieldBuilder = new StringBuilder();
 		fieldBuilder.append("a1.id");
 		//selectBuilder.append("SELECT * FROM ");
 		selectBuilder.append("SELECT ");
 		int inheritanceLevel = 0;
 		//System.out.println("starting query build");
 		for(Class<?> currentClass = aTemplate.getClass(); currentClass != Object.class; currentClass = currentClass.getSuperclass(), currentChar++, numForAlias++, inheritanceLevel++){
 			if(currentChar > 122){
 				currentChar = 97;
 			}
 			if(currentClass == ORMStorable.class){
 				//System.out.println("done");
 				continue;
 			}
 			/*
 			 * build select clause
 			 */
 			String alias = null;
 			if(currentClass != ORMStorable.class){
 				if(numForAlias != 1){//isn't first table name for list
 					aliasBuilder.append(", ");
 				}
 				String tableName = currentClass.getCanonicalName().replace('.', '_');
 				aliasBuilder.append(tableName);
 
 				alias = (""+(char)currentChar) +numForAlias;
 				aliasBuilder.append(" ");
 				aliasBuilder.append(alias);
 				inheritanceList.add(alias);
 			}
 
 			if(currentClass.equals(ORMStorable.class)){
 				//System.out.println("at storable class");
 				continue;
 			}
 			/*
 			 * iterate over all of the fields of the class to assemble the where clause
 			 * and the select clause
 			 */
 			Field[] theFields = currentClass.getDeclaredFields();
 			//System.out.println("fields: "+theFields.length);
 			//System.out.println("ignoreFields: "+ignoreFields);
 			boolean appendAnd = false;
 			for(int i = 0; i < theFields.length; i++){
 				Field aField = theFields[i];
 				if(aField == theUUIDField){
 					//System.out.println("field: id");
 				}
 				else if(!aField.getName().equals("tablesExist")){
 					//System.out.println("class: "+currentClass.getCanonicalName()+" field: "+aField.getName());
 				}
 				if(!ignoreFields.contains(aField)){
 					aField.setAccessible(true);
 					try {
 						String fieldName = aField.getName();
 						fieldBuilder.append(", ");
 						fieldBuilder.append(alias);
 						fieldBuilder.append('.');
 						fieldBuilder.append(fieldName);
 						instanceFields.put(fieldName, aField);
 
 						Object aValue = aField.get(aTemplate);
 						//System.out.println("value type: "+aField.getType());
 						if(aValue != null && alias != null){
 
 							if(appendAnd || inheritanceList.size() > 1){//isn't the first portion of the where clause
 								whereBuilder.append(" AND ");
 							}
 							if(whereBuilder == null){
 								whereBuilder = new StringBuilder();
 								whereBuilder.append(" WHERE ");
 							}
 							whereBuilder.append(alias);
 							whereBuilder.append('.');
 							whereBuilder.append(fieldName);
 							whereBuilder.append(" = ?");
 							appendAnd = true;
 							String valueAsString = "";
 							//System.out.println("checking "+aValue.getClass());
 							if(aField.getType().isArray() 
 									|| aField.getType().isAssignableFrom(Collection.class)
 									|| aField.getType().isAssignableFrom(Map.class)){
 								if(aValue != null){
 									valueAsString = "IS NOT NULL";
 								}
 								//System.out.println("is array");
 							}
 							else if(aField.getType().isAssignableFrom(ORMStorable.class)){
 								valueAsString = ((ORMStorable)aValue).getUUID();
 								//System.out.println("is storable");
 							}
 							else if(aField.getType().isAssignableFrom(String.class)){
 								valueAsString = (String)aValue;
 								//System.out.println("is String");
 							}
 							else if(aField.getType().isAssignableFrom(Number.class)){
 								valueAsString = ((Number)aValue).toString();
 								//System.out.println("is number");
 							}
 							else if(aField.getType().isAssignableFrom(Boolean.class)){
 								int valueAsInt = ((Boolean)aValue).booleanValue() ? 1 : 0;
 								valueAsString = Integer.toString(valueAsInt);
 								//System.out.println("is boolean");
 							}
 							else if(aField.getType().isAssignableFrom(Date.class)){
 								Timestamp aStamp = new Timestamp(((Date)aValue).getTime());
 								valueAsString = aStamp.toString();
 								//System.out.println("is date");
 							}
 							else if(aField.getType().isAssignableFrom(Byte.class)){
 								Byte aByte = new Byte((Byte)aValue);
 								valueAsString = aByte.toString();
 								//System.out.println("is byte: "+valueAsString);
 							}
 							else if(aField.getType().isAssignableFrom(Character.class)){
 								Character aChar = new Character((Character)aValue);
 								valueAsString = String.valueOf(aChar);
 								//System.out.println("is char: "+valueAsString);
 							}
 							else{
 								//System.out.println("unknown type");
 								valueAsString = aValue.toString();
 							}
 							bindList.add(valueAsString);
 						}
 					}
 					catch(Exception e){
 						throw new KVKitORMException(e);
 					}
 				}//end of field has value
 			}//end of field iteration
 		}//end of class iteration
 
 		StringBuilder joinBuilder = new StringBuilder();
 
 		for(int i = 1; i < inheritanceList.size(); i++){
 			if(i != 1){
 				joinBuilder.append(" AND ");
 			}
 			joinBuilder.append(inheritanceList.get(0));
 			joinBuilder.append(".id = ");
 			joinBuilder.append(inheritanceList.get(i));
 			joinBuilder.append(".id");
 		}
 
 		//System.out.println(selectBuilder.toString());
 		//System.out.println(fieldBuilder.toString());
 		//System.out.println(joinBuilder.toString());
 
 		selectBuilder.append(fieldBuilder);
 		selectBuilder.append(" FROM ");
 		selectBuilder.append(aliasBuilder);
 		//System.out.println("inheritance level: "+inheritanceLevel);
 		if(whereBuilder != null){
 			selectBuilder.append(whereBuilder);
 			//System.out.println(whereBuilder.toString());
 		}
 		else if(inheritanceLevel > 2 && whereBuilder == null){
 			selectBuilder.append(" WHERE ");
 		}
 		selectBuilder.append(joinBuilder);
 		String sql = selectBuilder.toString();
 
 
 
 		String[] parameterList =  new String[bindList.size()];
 		for(int i = 0; i < bindList.size(); i++){
 			parameterList[i] = bindList.get(i);
 			//System.out.println("parameter value: "+parameterList[i]);
 		}
 
 		//System.out.println("load query: "+sql);
 		//System.out.println("parameters: "+Arrays.toString(parameterList));
 		Cursor resultCursor = theDb.rawQuery(sql, parameterList);
 		//System.out.println("number results: "+resultCursor.getCount());
 
 		ArrayList<ORMStorable> storables = new ArrayList<ORMStorable>();
 		//column names match field names
 		String[] columnNames = resultCursor.getColumnNames();
 		while(resultCursor.moveToNext()){
 			//System.out.println("creating a storable.");
 			try {
 				//ArrayList<Field> neededChildren = new ArrayList<Field>();
 				ORMStorable aStorable = (ORMStorable)aTemplate.getClass().newInstance();
 				for(int i = 0; i < columnNames.length; i++){
 					//System.out.println("working column: "+columnNames[i]);
 					String columnName = columnNames[i];
 					if(columnName.equals("id")){
 						columnName = "theUUID";
 					}
 					Field aField =  instanceFields.get(columnName);
 					if(ORMStorable.class.isAssignableFrom(aField.getType()) 
 							|| aField.getType().isArray()
 							|| Collection.class.isAssignableFrom(aField.getType())
 							|| Map.class.isAssignableFrom(aField.getType())){
 						continue;
 					}
 					else{
 						aField.setAccessible(true);
 						int columnType = resultCursor.getType(i);
 						if(columnType == Cursor.FIELD_TYPE_BLOB){
 							byte[] bytes = resultCursor.getBlob(i);
 							if(aField.getType().isAssignableFrom(BigInteger.class)){
 								aField.set(aStorable, new BigInteger(bytes));
 							}
 							else{
 								aField.set(aStorable, bytes);
 							}
 						}
 						else if(columnType == Cursor.FIELD_TYPE_FLOAT){
 							//System.out.println("type: "+aField.getType()+" "+aField.getName());
 							if(aField.getType().isAssignableFrom(Double.class)){
 								aField.set(aStorable, resultCursor.getDouble(i));
 							}
 							else{
 								aField.set(aStorable, resultCursor.getFloat(i));
 							}
 						}
 						else if(columnType == Cursor.FIELD_TYPE_INTEGER){
 							//System.out.println("doing integer");
 							long longValue = resultCursor.getLong(i);
 							//System.out.println("field type: "+aField.getType());
 							if(aField.getType().isAssignableFrom(Boolean.class)){
 								//System.out.println("is boolean");
 								aField.set(aStorable, longValue == 1 ? true : false);
 							}
 							else if(aField.getType().isAssignableFrom(Short.class)){
 								aField.set(aStorable, (short)longValue);
 							}
 							else if(aField.getType().isAssignableFrom(Byte.class)){
 								aField.set(aStorable, (byte)longValue);
 							}
 							else if(aField.getType().isAssignableFrom(Integer.class)){
 								aField.set(aStorable, (int)longValue);
 							}
 							else{
 								aField.set(aStorable, longValue);
 							}
 						}
 						else if(columnType == Cursor.FIELD_TYPE_STRING){
 							//System.out.println("field type: "+aField.getType());
 							boolean isUUID = false;
 							if(aField.getName().equals("theUUID")){
 								ORMStorable foundStorable = findExistingStorable(resultCursor.getString(i));
 								if(foundStorable != null){
 									//System.out.println("found: "+resultCursor.getString(i));
 									aStorable = foundStorable;
 									continue;
 								}
 								isUUID = true;
 							}
 							if(aField.getType().isAssignableFrom(Character.class)){
 								byte[] resultBytes = resultCursor.getBlob(i);
 								aField.set(aStorable, (char)resultBytes[0]);
 							}
 							else if(aField.getType().isAssignableFrom(BigDecimal.class)){
 								String decimalAsString = resultCursor.getString(i);
 								Constructor<?> theConstructor = aField.getType().getDeclaredConstructor(String.class);
 								aField.set(aStorable, theConstructor.newInstance(decimalAsString));
 							}
 							else if(aField.getType().isAssignableFrom(BigInteger.class)){
 								String integerAsString = resultCursor.getString(i);
 								Constructor<?> theConstructor = aField.getType().getDeclaredConstructor(String.class);
 								aField.set(aStorable, theConstructor.newInstance(integerAsString));
 							}
 							else{
 								aField.set(aStorable, resultCursor.getString(i));
 								if(isUUID){
 									//System.out.println("adding new: "+resultCursor.getString(i));
 									addExistingStorable(aStorable);
 								}
 							}
 						}
 					}
 					//else must be null so don't set it
 				}
 				/*
 				//System.out.println("about to add to list: "+aStorable.getUUID());
 				for(Field storableFieldToLoad : neededChildren){
 					aStorable.load(theDb, storableFieldToLoad);
 				}
 				 */
 				storables.add(aStorable);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		return storables;
 	}
 	protected ORMStorable buildStorableFromRecord(Cursor theCursor, Class<? extends ORMStorable>theStorableClass) throws InstantiationException, IllegalAccessException, NoSuchFieldException{
 		ORMStorable result = theStorableClass.newInstance();
 		int numColumns = theCursor.getColumnCount();
 		for(int i = 0; i < numColumns; i++){
 
 			int theDBFieldType = theCursor.getType(i);
 			String fieldName = theCursor.getColumnName(i);
 			//System.out.println("column name: "+fieldName);
 			Field theField = null;
 			if(fieldName.equals("id")){
 				theField = ORMStorable.class.getDeclaredField("theUUID");
 			}
 			else{
 				theField = theStorableClass.getDeclaredField(fieldName);
 			}
 			theField.setAccessible(true);
 			if(theDBFieldType == Cursor.FIELD_TYPE_NULL){
 				continue;
 			}
 			else if(theDBFieldType == Cursor.FIELD_TYPE_STRING){
 				theField.set(result, theCursor.getString(i));
 			}
 			else if(theDBFieldType == Cursor.FIELD_TYPE_INTEGER){
 				//System.out.println("Field: "+theField.getName()+" "+theField.getType());
 				int fieldValue = theCursor.getInt(i);
 				if(theField.getType().isAssignableFrom(Boolean.class)){
 					boolean fieldValueAsBoolean = fieldValue == 1 ? true : false;
 					theField.set(result, fieldValueAsBoolean);
 				}
 				else if(theField.getType().isAssignableFrom(Integer.class)){
 					theField.set(result, new Integer(fieldValue));
 				}
 				else{
 					theField.set(result, fieldValue);
 				}
 			}
 			else if(theDBFieldType == Cursor.FIELD_TYPE_FLOAT){
 				if(theField.getType().isAssignableFrom(Double.class)){
 					theField.set(result, new Double(theCursor.getFloat(i)));
 				}
 				else if(theField.getType().isAssignableFrom(BigDecimal.class)){
 					theField.set(result, new BigDecimal(theCursor.getString(i)));
 				}
 				else{
 					theField.set(result, theCursor.getFloat(i));
 				}
 			}
 			else if(theDBFieldType == Cursor.FIELD_TYPE_BLOB){
 				if(theField.getType().isAssignableFrom(BigInteger.class)){
 					theField.set(result, new BigInteger(theCursor.getBlob(i)));
 				}
 			}
 		}
 		return result;
 	}
 
 	public void close(){
 		theHelper.close();
 	}
 }
