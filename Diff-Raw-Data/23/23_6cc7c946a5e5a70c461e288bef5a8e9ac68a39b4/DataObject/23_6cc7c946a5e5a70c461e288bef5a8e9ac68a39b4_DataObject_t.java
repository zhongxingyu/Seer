 package com.cs310.ubc.meetupscheduler.server;
 
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * DataObject is a parent class for all the JDO objects in our application. This parent class allows for use of only a single Service
  * to do asynchronous retrieval, as well as some nifty inherited methods.
  */
 public class DataObject {
 	  public DataObject() {
 		  //jdoNewInstrance needs a no-arg constructor.
 	  }
 	  
 	  /**
 	   * This creates a data object.
 	   * @param myFields A Map of Strings where the key is the column name as defined in the object's fields enum and the value
 	   * 				 is the value to be used in the created object.
 	   * @throws IllegalArgumentException
 	   * @throws IllegalAccessException
 	   * @throws SecurityException
 	   * @throws NoSuchFieldException
 	   */
 	  public DataObject(Map<String, String> myFields) throws IllegalArgumentException, IllegalAccessException, SecurityException, NoSuchFieldException {
 		  Class<?> myClass = this.getClass();
 		  for (String key : myFields.keySet()) {
 			  Field f = myClass.getDeclaredField(key);
 			  if (f.getType() == Long.class)
 				  f.set(this, new Long(myFields.get(key)));
 			  else if (f.getType() == ArrayList.class)
 				  f.set(this, new ArrayList<String>(Arrays.asList(myFields.get(key).split(","))));
 			  else
 				  f.set(this, myFields.get(key));
 		  }
 	  }
 	  
 	  /**
 	   * This method sets the value of a field in an existing DataObject to a new value.
 	   * @param field The field name you want to change.
 	   * @param newValue The value to which the column name will be changed.
 	   * @throws IllegalArgumentException
 	   * @throws IllegalAccessException
 	   * @throws SecurityException
 	   * @throws NoSuchFieldException
 	   */
 	  public void setField(String field, String newValue) throws IllegalArgumentException, IllegalAccessException, SecurityException, NoSuchFieldException {
 		  Class<?> myClass = this.getClass();
 		  Field f = myClass.getDeclaredField(field);
 		  if (f.getType() == Long.class)
 			  f.set(this, new Long(newValue));
 		  else if (f.getType() == ArrayList.class)
 			  f.set(this, new ArrayList<String>(Arrays.asList(newValue.split(","))));
 		  else
 			  f.set(this, newValue);
 	  }
 	  
 	  /**
 	   * This method returns the fields of an object as a HashMap of Strings for use in tables.
 	   * @return A HashMap of Strings where the key is the field name and the value is the value. Remember to cast the value if you want to do something fancy.
 	   * @throws IllegalArgumentException
 	   * @throws IllegalAccessException
 	   */
 	  @SuppressWarnings("unchecked")
 	  public HashMap<String, String> formatForTable() throws IllegalArgumentException, IllegalAccessException {
 		  HashMap<String, String> myValues = new HashMap<String, String>();
 		  Class<?> myClass = this.getClass();
 		  Field[] f = myClass.getDeclaredFields();
 		  for (Field field: f) 
 			  if (field.getType() == String.class)
 				  myValues.put(field.getName(), (String) field.get(this));
 			  else if (field.getType() == Long.class)
 				  myValues.put(field.getName(), field.get(this).toString());
 			  else if (field.getType() == ArrayList.class) {
				  if (field.get(this) != null) {
					  StringBuilder sb = new StringBuilder();
					  for (String item : (ArrayList<String>) field.get(this))
						  sb.append(item + ",");
					  String output = sb.toString();
					  if (output.length() > 0)
						  output = output.substring(0, output.length() -1);
					  myValues.put(field.getName(), output); //Remove the trailing comma
				  }
 			  }
 		  return myValues;
 	  }
 	  
 	  /**
 	   * Gets the value of a field of a DataObject
 	   * @param field The field whose value you want to get.
 	   * @return The value of the specified field in String form.
 	   * @throws IllegalArgumentException
 	   * @throws IllegalAccessException
 	   * @throws SecurityException
 	   * @throws NoSuchFieldException
 	   */
 	  public String getField(String field) throws IllegalArgumentException, IllegalAccessException, SecurityException, NoSuchFieldException {
 		  Class<?> myClass = this.getClass();
 		  Field f = myClass.getDeclaredField(field);
 		  return f.get(this).toString();
 	  }
 	  
 	  public static String getDefaultOrdering() {
 		  return null;
 	  }
 }
