 
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
      try {
         Object ret = bc.getLocal(_names);
         if (_filtered) {
            Filter f = bc.getFilter(Variable.makePropertyNames(_names));
            bc.push(ret);
            ret = f.evaluate(bc);
            bc.pop();
         }
         return ret;
      } catch (Exception e) {
         throw new BuildException("Property error occured during parameter "
               + "build pocess: " + e);
      }
    }
 
    public String toString() {
       return "param:" + Variable.makeName(_names);
    }
 
 }
 
