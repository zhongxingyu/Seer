 /*******************************************************************************
  * Copyright 2010 Nathan Kopp
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
  ******************************************************************************/
 package org.d2.query;
 
 
 public class D2QueryBuilder
 {
     private D2Query query;
     private D2QueryBuilder parent;
     
     public D2QueryBuilder()
     {
         this.query = new D2Query();
     }
     
     public static D2QueryBuilder start()
     {
         return new D2QueryBuilder();
     }
     
     public D2QueryBuilder add(D2QueryNode node)
     {
         query.add(node);
         return this;
     }
     
     public D2QueryBuilder add(D2QueryNode node, Occurs occurs)
     {
         node.setOccurs(occurs);
         query.add(node);
         return this;
     }
     
     public D2QueryBuilder addFilter(Filter filter)
     {
         D2QueryBuilder root = this;
         while(root.getParent()!=null) root = root.getParent();
         root.getQuery().addFilter(filter);
         return this;
     }
     
     public D2QueryBuilder exact(String field, String value, Occurs occurs)
     {
         if(occurs==Occurs.MUST) addFilter(new FieldFilter(field, value));
         
         return add(new D2Term(field, value, TermType.EXACT, occurs));
     }
     
     public D2QueryBuilder parse(String defaultField, String queryText, Occurs occurs)
     {
         return add(new D2Term(defaultField, queryText, TermType.PARSED, occurs));
     }
 
     public D2QueryBuilder range(String field, String value1, String value2, Occurs occurs)
     {
         return add(new D2RangeTerm(field, value1, value2, true, occurs));
     }
     
    public D2QueryBuilder range(String field, Number value1, Number value2, Occurs occurs)
    {
        return add(new D2NumericRangeTerm(field, value1, value2, true, occurs));
    }
    
     public D2QueryBuilder prefix(String field, String value, Occurs occurs)
     {
         return add(new D2Term(field, value, TermType.PREFIX, occurs));
     }
     
     public D2QueryBuilder wildcard(String field, String value, Occurs occurs)
     {
         return add(new D2Term(field, value, TermType.WILDCARD, occurs));
     }
     
     // ==========================================
     
     public D2QueryBuilder exact(String field, String value)
     {
         return add(new D2Term(field, value, TermType.EXACT, Occurs.SHOULD));
     }
     
     public D2QueryBuilder parse(String defaultField, String queryText)
     {
         return add(new D2Term(defaultField, queryText, TermType.PARSED, Occurs.SHOULD));
     }
 
     public D2QueryBuilder range(String field, String value1, String value2)
     {
         return add(new D2RangeTerm(field, value1, value2, true, Occurs.SHOULD));
     }
     
    public D2QueryBuilder range(String field, Number value1, Number value2)
    {
        return add(new D2NumericRangeTerm(field, value1, value2, true, Occurs.SHOULD));
    }
    
     public D2QueryBuilder prefix(String field, String value)
     {
         return add(new D2Term(field, value, TermType.PREFIX, Occurs.SHOULD));
     }
     
     public D2QueryBuilder wildcard(String field, String value)
     {
         return add(new D2Term(field, value, TermType.WILDCARD, Occurs.SHOULD));
     }
     
     // ==========================================
 
     public D2QueryBuilder group(Occurs occurs)
     {
         D2QueryBuilder builder = new D2QueryBuilder();
         builder.getQuery().setOccurs(occurs);
         add(builder.getQuery());
         return builder;
     }
     
     public D2QueryBuilder endGroup()
     {
         return (D2QueryBuilder)getParent();
     }
     
     
     
     public D2Query getQuery()
     {
         return query;
     }
 
     public D2QueryBuilder getParent()
     {
         return parent;
     }
 }
