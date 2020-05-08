 /**
  * 
  */
 package com.jsar.client.json;
 
 import com.google.gwt.json.client.JSONString;
 
 /**
  * @author hyahn
  *
  */
 public class RestaurantJson extends AbstractJson{
 
 	 public static final String NAME="name";
 	  public static final String ADDRESS="address";
 	  public static final String TELEPHONENUMBER= "telephoneNumber";
 	  public static final String TYPEOFFOOD ="typeOfFood";
 
 
 	  
 	  public void setName(String name){
 	    jsonObject.put(NAME, new JSONString(name));
 	  }
 
 	  public String getName(){
	    return jsonObject.get(NAME).isString().stringValue();
 	  }
 	  public void setAddress(String description){
 	    jsonObject.put(ADDRESS, new JSONString(description));
 	  }
 
 	  public String getAddress(){
	    return jsonObject.get(ADDRESS).isString().stringValue();
 	  }
 	  public void setTelephoneNumber(String telephoneNumber){
 	    jsonObject.put(TELEPHONENUMBER, new JSONString(telephoneNumber));
 	  }
 
 	  public void getTelephoneNumber(){
	    jsonObject.get(TELEPHONENUMBER).isString().stringValue();
 	  }
 	  
 	  /*
 	   * public void setTypeOfFood(ArrayList typeOfFood){
 		  JSONArray jsonArray=new JSONArray
 		  jsonArray.
 		  for(int i=0;i<typeOfFood.size();++i)
 		  {
 			  jsonArray.set(i, value)
 		   
 		  }
 	  }
 */
 	  public void getTypeOfFood(){
 		    jsonObject.get(TYPEOFFOOD).isString().toString();
 		}
 	
 }
