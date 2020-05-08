 package com.brweber2.unify;
 
 import com.brweber2.term.Term;
 import com.brweber2.term.Variable;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.UUID;
 
 /**
  * @author brweber2
  *         Copyright: 2012
  */
 public class UnificationScope {
 
     private UnificationScope parent;
     private Map<String,Term> values = new HashMap<String,Term>();
     private Map<Variable,String> uuids = new HashMap<Variable,String>();
 
     public UnificationScope() {
         this.parent = null;
     }
 
     public UnificationScope(UnificationScope parent) {
         this.parent = parent;
     }
 
     public boolean set( Variable variable, Term value )
     {
         Term val = get( variable );
         if ( val == null )
         {
             String uuid = UUID.randomUUID().toString();
             uuids.put(variable,uuid);
             if ( value instanceof Variable )
             {
                 uuids.put( (Variable) value, uuid );
             }
             else
             {
                 values.put( uuid, value );
                 val = value;
             }
         }
         return value.equals( val );
     }
 
     public boolean has( Variable variable )
     {
         String uuid = uuids.get(variable);
         if ( values.containsKey( uuid ) )
         {
             return true;
         }
         if ( parent != null )
         {
             return parent.has( variable );
         }
         return false;
     }
     
     public Term get( Variable variable )
     {
         String uuid = uuids.get(variable);
         if ( values.containsKey( uuid ) )
         {
             return values.get( uuid );
         }
         if ( parent != null )
         {
             return parent.get( variable );
         }
         return null;
     }
 }
