 /* Copyright (c) 2008 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.opensocial.client;
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 
import org.json.JSONObject;

 public class OpenSocialRequestParameter {
   private List<String> valuesList = null;
   private Map<String, String> valuesMap = null;
 
   public OpenSocialRequestParameter(Map<String, String> valuesMap) {
     this.valuesMap = valuesMap;
   }
 
   public OpenSocialRequestParameter(String[] values) {
     valuesList = new Vector<String>(values.length);
     this.addValues(values);
   }
 
   public OpenSocialRequestParameter(String value) {
     valuesList = new Vector<String>(1);
     this.addValue(value);
   }
 
   private void addValues(String[] values) {
     this.valuesList.addAll(Arrays.asList(values));
   }
 
   private void addValue(String value) {
     this.valuesList.add(value);
   }
 
   public Map<String, String> getValuesMap() {
     return valuesMap;
   }
 
   public List<String> getValuesList() {
     return valuesList;
   }
 
   public String getValuesString() {
    if (valuesList != null) {
      return getValuesListAsString();
     }
    else if (valuesMap != null) {
    	return getValuesMapAsString();
    }
    else {
    	return null;
    }
  }
  
  public String getValuesListAsString() {
    
     StringBuilder list = new StringBuilder();
 
     for (int i = 0; i < valuesList.size(); i++) {
       if (i != 0) {
         list.append(",");
       }
 
       list.append(valuesList.get(i));
     }
 
     return list.toString();
   }
  
  public String getValuesMapAsString() {
	  JSONObject json = new JSONObject(valuesMap);
	  
	  if (json == null) {
		  return null;
	  }
	  
	  return json.toString();
  }
   /**
    * Returns true if the number of stored values is greater than one, false
    * otherwise.
    */
   public boolean isMultivalued() {
     return valuesList.size() > 1;
   }
 
   /**
    * Returns true if there are one or more key-value pairs associated with this parameter.
    */
   public boolean isMultikeyed() {
     return valuesMap != null;
   }
 }
