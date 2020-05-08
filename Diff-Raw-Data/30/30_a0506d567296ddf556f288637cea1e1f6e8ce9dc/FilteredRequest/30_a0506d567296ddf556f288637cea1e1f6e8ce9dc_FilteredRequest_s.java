 /*
  * Copyright 2013 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package grails.plugin.snakecaseparams;
 
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Collections;
 import java.util.Enumeration;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletRequestWrapper;
 
import grails.util.GrailsNameUtils;

 public class FilteredRequest extends HttpServletRequestWrapper {
     private final Map<String, String[]> params;
 
     public FilteredRequest(HttpServletRequest request) {
         super(request);
         params = new HashMap<String, String[]>();
 
         Map<String, String[]> oldParams = super.getParameterMap();
 
         String originalKey = null;
         String newKey = null;
 
         for (Object key: oldParams.keySet()) {
             originalKey = (String)key;
            newKey = GrailsNameUtils.getPropertyNameForLowerCaseHyphenSeparatedName(originalKey.replace("_","-"));
             params.put(newKey, oldParams.get(originalKey));
        }        
     }
 
     @Override
    public String getParameter(final String paramName) {            
         String[] strings = getParameterMap().get(paramName);
         if (strings != null) {
             return strings[0];
         }
        return super.getParameter(paramName);        
     }
 
     @Override
     public Map<String, String[]> getParameterMap() {
         return Collections.unmodifiableMap(params);
    }     
 
     @Override
     public Enumeration<String> getParameterNames() {
         return Collections.enumeration(getParameterMap().keySet());
     }
 
     @Override
    public String[] getParameterValues(final String paramName) {           
         return getParameterMap().get(paramName);
    }   
 }
