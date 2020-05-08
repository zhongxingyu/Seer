 package com.mymed.controller.core.requesthandler.matchmaking;
 
 import static com.mymed.model.data.application.MOntologyID.ENUM;
 
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map.Entry;
 
 import com.mymed.controller.core.requesthandler.AbstractRequestHandler;
 import com.mymed.model.data.application.MDataBean;
 
 /*
  * Copyright 2012 INRIA
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
 public abstract class AbstractMatchMaking extends AbstractRequestHandler {
 	
     private static final long serialVersionUID = 967382846767543709L;
 
     /**
      * JSON 'predicate' attribute.
      */
     protected static final String JSON_NAMESPACE = JSON.get("json.namespace");
     
 	/**
 	 * JSON 'predicate' attribute.
 	 */
     protected static final String JSON_PREDICATE = JSON.get("json.predicate");
 
 	/**
 	 * JSON 'predicateList' attribute.
 	 */
     protected static final String JSON_PREDICATE_LIST = JSON.get("json.predicate.list");
     
     /**
      * JSON 'asyncexec' attribute.
      */
     protected static final String JSON_ASYNCEXEC = JSON.get("json.asyncexec");
 
    
     /** 
       Class that repsent one line of combinatory with potential list of values (instead of single value) 
       Example:
       predicates: A="a", B="b|b2", "C="c" 
       Gives the following combi lines:
         * A=a
         * A=a, B=[b, b2] 
         * B=[b, b2] C=c
         * A=a, C=c
         * C=c
       Which gives, after expansion :
       "Aa", "AaBb", "AaBb2", "BbCc", "Bb2Cc", "AaCc", "Cc" 
       */
     static private class CombiLine {
         
         private LinkedHashMap<String, List<String>> map = new LinkedHashMap<String, List<String>>();
         
         // Add a value fot this key.
         public void put(String key, String val) {
             if (this.map.containsKey(key)) {
                 this.map.get(key).add(val);
             } else {
                 ArrayList<String> list = new ArrayList<String>();
                 list.add(val);
                 this.map.put(key, list);
             }
         }
 
         
         // Expand a list of all predicates
         public List<String> expand() {
             
             // Show state of combi before expanding
             // LOGGER.info("CombiLine: {}", this.map.toString());
             
             // List of keys, in their order of apparence
             final String keys[] = this.map.keySet().toArray(new String[]{});
             
             // Buffer of all possibilities
             final List<String> result = new ArrayList<String>();
                         
             // Dummy anynomous class for inner method
             new Object() {
                 
                 // Recursive method to generate predicates
                 public void predicatesRec(String prefix, int keyIdx) {
                            
                     // Reached end of combi line 
                     if (keyIdx == keys.length) {
                         result.add(prefix);
                         return; 
                     }
                    
                     // Get key/vals
                     String key = keys[keyIdx]; 
                     List<String> values = CombiLine.this.map.get(key);
                                     
                     // Loop on values
                     for (int i=0; i< values.size(); i++) {
 
                         // Recursive call. Add "keyval"
                         this.predicatesRec(
                                 prefix + key + values.get(i), 
                                 keyIdx + 1);
                     }
                     
                 }
                 
             // Init the recursion    
             }.predicatesRec("", 0);
             
             LOGGER.info("Expanded combis : {}", result);
             return result;
         
         } // End of #expand()
         
     } // End of class CombiLine
     
     /** Build full predicate, with concatened "keyval" */
     static public String getSubPredicate(List<MDataBean> predicate) {
         StringBuffer buff = new StringBuffer();
         for(MDataBean pred : predicate) {
             buff.append(pred.toString());
         }
         return buff.toString();
     }
     
     
     
     
 	/**
 	 * 
 	 * contructs all possible indexes between 1 and level from predicateListObject
 	 * 
 	 *  ex: [A, B, C] with level = 2 returns [A.toString(), B.toString(), C.toString(),
 	 *  				 A.toString()+B.toString(), A.toString()+C.toString(), B.toString()+C.toString()]
 	 * 
 	 * 
 	 * @param predicateListObject
 	 * @param level
 	 * @return List of StringBuffers
 	 */
 	static public List<String> getPredicate(
 	        final List<MDataBean> dataBeans, 
 	        final int minLevel,
 	        final int maxLevel) 
 	{
   
 		int n = dataBeans.size();
 		
 		List<String> result = new ArrayList<String>();
 		
 		// Loop on number of predicates 
 		for (int k=minLevel; k<=maxLevel; k++) {
 		    
 		    // Loop on all possiblities for this number of predicates
 			for (long i = (1 << k) - 1; (i >>> n) == 0; i = nextCombo(i)) {
 			    
 			    // Create one combi line
 			    CombiLine combiLine = new CombiLine();
 			    
 				int mask = (int) i;
 				int j = 0;
 
 				// Loop on DataBean 
 				while (mask > 0) {
 				    
 				    // Add it ?
 					if ((mask & 1) == 1) {
 					    
 					    // Add one or several values to the combi line fot this key 
 					    MDataBean dataBean = dataBeans.get(j);
 					    
 					    // ENUM => Split value into list of values     
 					    if (dataBean.getOntologyID() == ENUM) {
 					        String[] values = dataBean.getValue().split("\\|");
 					        for (String value :  values) {
 					            combiLine.put(dataBean.getKey(), value);         
 					        }
 					    } else { // Add simple value for the key
 					        combiLine.put(dataBean.getKey(), dataBean.getValue());  
 					    }
 					}
 					mask >>= 1;
 					j++;
 				} // End of loop on data beans
 				
 				// Expand the current combi line
 				result.addAll(combiLine.expand());
 				
 				
 			} // End of loop on possibilities for this number of predicates
 		
 		} // Loop on number of predicates
 		
 		return result;
 	}
 	
 
     static private long nextCombo(long n) {
     	// Gosper's hack, doesn't support level>= 64, there are other recursive functions to replace it without this limit
 		
     	// moves to the next combination (of n's bits) with the same number of 1 bits
    	
 		long u = n & (-n);
 		long v = u + n;
 		return v + (((v ^ n) / u) >> 2);
 	}
 
 }
