 /*
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy
  * of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed
  * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
  * OR CONDITIONS OF ANY KIND, either express or implied. See the License for
  * the specific language governing permissions and limitations under the
  * License.
  */
 package org.amplafi.flow;
 
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import static org.apache.commons.lang.StringUtils.*;
 
 import org.amplafi.flow.flowproperty.FlowPropertyDefinitionBuilder;
 import org.amplafi.flow.impl.DefaultFlowValuesMapKey;
 import org.amplafi.json.JSONStringer;
 import org.apache.commons.lang.ObjectUtils;
 
 
 
 /**
  *
  */
 @Deprecated // TODO need to figure out a way to get rid of this as a utility class -- fold it into something else.
 public class FlowUtils {
 
     public static final FlowUtils INSTANCE = new FlowUtils();
 
     /**
      * Converts a {@link Map} into a {@link List} of <em>key='value'</em> strings.
      *
      * @param map The map to convert
      * @return list of strings in <em>key='value'</em> form
      */
    public List<String> createInitialValues(Map<String, String> map) {
         List<String> result = new ArrayList<String>();
         for(Map.Entry<String, String> entry: map.entrySet()) {
             addInitialValues(result, entry.getKey(), entry.getValue());
         }
         return result;
     }
    /**
     * @param values
     * @param keyValue
     */
    public void addInitialValues(List<String> values, Object keyValue) {
        addInitialValues(values, keyValue, keyValue);
    }
 
     /**
      * Converts each passed key value pair to the form: <em>key='value'</em>
      * and adds the result into the given list.
      *
      * @param values the list in which to include the key value pairs
      * @param keyValues the key value pairs
      */
     public void addInitialValues(List<String> values, Object... keyValues) {
         for(int i = 0; i < keyValues.length; i+=2) {
             if ( keyValues[i] != null) {
                 values.add(keyValues[i].toString()+"='"+ObjectUtils.toString(keyValues[i+1])+"'");
             }
         }
     }
     public String toFlowString(Object value) {
         String strV = null;
         if ( value instanceof String ) {
             strV = (String) value;
         } else if ( value != null ) {
             JSONStringer stringer = new JSONStringer();
             stringer.value(value);
             strV = stringer.toString();
             if (strV.startsWith("\"") && strV.endsWith("\"")) {
                 // trim off unneeded " that appear when handling simple objects.
                 strV = strV.substring(1, strV.length()-1);
             }
         }
         return strV;
     }
     public String toLowerCase(String flowName) {
         StringBuilder sb = new StringBuilder();
         if ( isNotBlank(flowName)) {
             for(int i = 0; i < flowName.length(); i++) {
                 if ( Character.isUpperCase(flowName.charAt(i)) ) {
                     if ( i > 0 ) {
                         sb.append('-');
                     }
                     sb.append(Character.toLowerCase(flowName.charAt(i)));
                 } else {
                     sb.append(flowName.charAt(i));
                 }
             }
         }
         return sb.toString();
     }
     public List<String> createInitialValues(Object... keyValues) {
         Map<String, String> map = createState(keyValues);
         return this.createInitialValues(map);
     }
     /**
      * Utility to create the initial state to be used when starting flows.
      * Users should supply a list of key-value object pairs.<p/>
      * If a value is null, nothing is added to the map.<p/>
      *
      * Before being added to the map, toString() is called on all objects.
      * {@link java.util.List Lists} are a special case - toString() is called
      * on all elements and the results are joined together in a newline
      * delimited string.
      *
      *
      * @param objects the key-value pairs
      * @return the map with the state info, or an empty map if no arguments passed
      */
     public Map<String, String> createState(Object...objects) {
         Map<String, String> ret = new LinkedHashMap<String, String>();
         if (objects==null) {
             return ret;
         }
         int evenSize = (objects.length>>1)<<1;
         for (int i=0; i<evenSize; i+=2) {
             String key;
             if ( objects[i] instanceof Class<?>) {
                 key = FlowPropertyDefinitionBuilder.toPropertyName((Class<?>) objects[i]);
             } else {
                 key = objects[i].toString();
             }
             Object value = objects[i+1];
             if ( value != null ) {
                 String strV = toFlowString(value);
                 ret.put(key, strV);
             }
         }
         return ret;
     }
 
     public String toKey(String namespace, Object key) {
         return new DefaultFlowValuesMapKey(namespace, key).toString();
     }
 }
