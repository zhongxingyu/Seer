 
 package org.webmacro.engine;
 
 import org.webmacro.*;
 
 public class ParamBuilder implements Builder
 {
    
 
    private Object[] _names;
    private final boolean _filtered;
 
    public ParamBuilder(Object[] names, boolean filtered) {
       _names = names;
       _filtered = filtered;
    }
 
    public final Object build(BuildContext bc) 
       throws BuildException
    {
      Variable var = VariableBuilder.newVariable(_names,bc,_filtered);
      return var.evaluate(bc);
    }
 
    public String toString() {
       return "param:" + Variable.makeName(_names);
    }
 
 }
 
