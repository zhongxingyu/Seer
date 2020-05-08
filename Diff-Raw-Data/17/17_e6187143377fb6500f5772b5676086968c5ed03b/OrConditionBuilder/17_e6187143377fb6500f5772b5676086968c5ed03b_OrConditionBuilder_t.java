 
 package org.webmacro.engine;
 
 import org.webmacro.*;
 
 final class OrConditionBuilder implements Builder 
 {
 
    private final Builder _l,_r;
    OrConditionBuilder(Builder l, Builder r) { _l = l; _r = r; }
 
    /**
      * Returns Boolean.TRUE, Boolean.FALSE, or a Condition
      */
    public final Object build(BuildContext pc) 
       throws BuildException
    {
       Object l, r;
       l = _l.build(pc);
      r = _r.build(pc);
 
       return ((l instanceof Condition) && (r instanceof Condition)) ?
          (Object) new OrCondition((Condition) l, (Condition) r)
          :
          (l instanceof Condition) ?
             (Object) (Condition.isTrue(r) ? Boolean.TRUE : l)
             :
             (Object) (Condition.isTrue(l) ? Boolean.TRUE : r);
    }
 }
 
 /**
   * Utility class
   */
 final class OrCondition extends Condition implements Macro {
    
    private final Condition _l,_r;
   OrCondition(Condition l, Condition r) { _l = l; _r = r; 
      System.out.println("Condition: " + l + " || + " + r);
   }
 
    public final boolean test(Object context) {
      System.out.println("_l.test: " + _l.test(context));
      System.out.println("_r.test: " + _r.test(context));
       return (_l.test(context) || _r.test(context));
    }
 }
 
