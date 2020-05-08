 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.drools.fipa;
 
 import java.util.HashMap;
 import java.util.Map;
 
 
 import org.drools.base.DroolsQuery;
 import org.drools.fipa.body.content.Action;
 import org.drools.fipa.body.content.NamedVariable;
 import org.drools.fipa.body.content.Query;
 import org.drools.fipa.body.content.Ref;
 import org.drools.fipa.mappers.MyMapArgsEntryType;
 import org.drools.fipa.mappers.MyMapReferenceEntryType;
 import org.drools.runtime.rule.impl.NativeQueryResults;
 import org.drools.runtime.rule.QueryResults;
 /**
  *
  * @author salaboy
  */
 public class MessageContentHelper {
 
     public static Object[] getActionArgsArray(Action action) {
 //        return args != null ? args.values().toArray() : new Object[0];
         Object[] myArray = new Object[action.getArgs().size()];
         int i = 0;
         for(MyMapArgsEntryType entry : action.getArgs()){
             myArray[i] = entry.getValue();
             i++;
         }
         return myArray;
     }
 
     public static Ref getActionReferences(Action action, QueryResults results) {
         Map<String, Object> map = new HashMap<String, Object>();
        System.out.println("outer = "+results);
         org.drools.QueryResults inner = ((NativeQueryResults) results).getResults();
        System.out.println("inner = "+inner);
         DroolsQuery query = (DroolsQuery) inner.get(0).get(-1);
        System.out.println("action references = "+ action.getReferences().size());
         for (MyMapReferenceEntryType entry : action.getReferences()) {
            map.put(action.getReferences().get(entry.getKey()).getValue(), query.getElements()[entry.getKey()]);
         }
         Ref ref = new Ref();
        System.out.println("THIS ARE MY ACTION REFERENCES : "+ref);
         ref.setReferences(MapArgsAdapterHelper.marshal(map));
         return ref;
     }
 
     public static NamedVariable variable(String ref) {
         return new NamedVariable(ref);
     }
     
     public static Ref getQueryReferences(Query query, QueryResults results) {
        Map<String,Object> map = new HashMap<String,Object>();
         org.drools.QueryResults inner = ((NativeQueryResults) results).getResults();
             DroolsQuery droolsQuery = (DroolsQuery) inner.get(0).get(-1);
            
         int i = 0;
         for (MyMapReferenceEntryType entry : query.getReferences()) {
            
             map.put(query.getReferences().get(i).getValue(), droolsQuery.getElements()[i]);
             i++;
         }
 
         Ref ref = new Ref();
         ref.setReferences(MapArgsAdapterHelper.marshal(map));
        System.out.println("THIS ARE MY QUERY REFERENCES : "+ref);
         return ref;
     }
 }
