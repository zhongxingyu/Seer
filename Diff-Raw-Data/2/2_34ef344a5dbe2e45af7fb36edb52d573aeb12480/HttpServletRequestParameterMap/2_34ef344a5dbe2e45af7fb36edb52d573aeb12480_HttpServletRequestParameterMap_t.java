 package uk.ac.ebi.arrayexpress.utils;
 
 /*
  * Copyright 2009-2010 Microarray Informatics Group, European Bioinformatics Institute
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 
 import javax.servlet.http.HttpServletRequest;
 import java.util.HashMap;
 import java.util.Map;
 
 public class HttpServletRequestParameterMap extends HashMap<String,String[]>
 {
    private final static RegexHelper ALL_SANS_SQUARE_BRACKETS = new RegexHelper("^(.*)\\[\\d*\\]$", "ig");
 
     public HttpServletRequestParameterMap( HttpServletRequest request )
     {
         if (null != request) {
             Map params = request.getParameterMap();
             for ( Object param : params.entrySet() ) {
                 Map.Entry p = (Map.Entry) param;
                 this.put(filterArrayBrackets((String)p.getKey()), (String[])p.getValue());
             }
         }
     }
 
     public void put( String key, String value )
     {
         String[] arrValue = new String[1];
         arrValue[0] = value;
         this.put(key, arrValue);
     }
 
     private String filterArrayBrackets( String key )
     {
         String result = ALL_SANS_SQUARE_BRACKETS.matchFirst(key);
         return !"".equals(result) ? result : key;
     }
 
 }
