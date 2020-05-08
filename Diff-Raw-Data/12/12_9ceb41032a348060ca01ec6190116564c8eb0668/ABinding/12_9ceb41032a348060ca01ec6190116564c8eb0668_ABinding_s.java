 /*
  * Copyright (C) 2012 brweber2
  */
 package com.brweber2.unify.impl;
 
 import com.brweber2.term.Term;
 import com.brweber2.term.Variable;
 import com.brweber2.unify.Binding;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 import java.util.logging.Logger;
 
 public class ABinding implements Binding
 {
     private static Logger log = Logger.getLogger( ABinding.class.getName() );
 
     private Map<Variable,String> vars = new HashMap<Variable, String>(  );
     private Map<String,Term> values = new HashMap<String, Term>(  );
 
     public ABinding()
     {
     }
 
     public ABinding(Binding parent)
     {
         this.vars = parent.getVars();
         this.values = parent.getValues();
     }
 
     public boolean isBound( Variable a )
     {
         return vars.containsKey( a );
     }
 
     public Set<Variable> getVariables()
     {
         return vars.keySet();
     }
 
     public void shareValues( Variable a, Variable b )
     {
         if ( !isBound( b ) )
         {
             Term value = null;
             String uuid = UUID.randomUUID().toString();
             vars.put( b, uuid );
             values.put( uuid, value );
         }
     }
 
     public void instantiate( Term a, Variable b )
     {
         if ( isBound( b ) && resolve( b ) != null )
         {
             if ( !new Unify().unify(a,resolve(b)).succeeded() )
             {
                 throw new FailedToUnifyException(a + "[" + a + "] does not unify with " + b + "[" + resolve(b) + "]");
             }
         }
         else if ( isBound( b ) )
         {
             values.put( vars.get( b ), a );
         }
         else
         {
             String uuid = UUID.randomUUID().toString();
             vars.put( b, uuid );
             values.put( uuid, a );
         }
     }
     
     public void instantiate( Variable a, Term b )
     {
         // no need to ever call in this direction....
         System.err.println( "instantiating " + a + " to " + b );
     }
 
     public Term resolve( Variable a )
     {
         if ( isBound( a ) )
         {
             return values.get( vars.get( a ) );
         }
         return null;
     }
 
     public Map<Variable, String> getVars()
     {
        return vars;
     }
 
     public Map<String, Term> getValues()
     {
        return values;
     }
 
     public void dumpVariables()
     {
         for ( Variable variable : vars.keySet() )
         {
             Term value = resolve( variable );
             System.out.println( variable + ": " + value );
         }
     }
 
     @Override
     public String toString() {
         StringBuilder str= new StringBuilder();
         str.append("ABinding {");
         for (Variable variable : vars.keySet()) {
             str.append(variable);
             str.append("=");
             str.append(values.get(vars.get(variable)));
             str.append(",");
         }
         str.append("}");
         return str.toString();
     }
 }
