 /*
  * Created by IntelliJ IDEA.
  * User: e_ridge
  * Date: Oct 18, 2002
  * Time: 4:44:50 PM
  * To change template for new class use
  * Code Style | Class Templates options (Tools | IDE Options).
  */
 package org.webmacro.directive;
 
 import org.webmacro.directive.*;
 import org.webmacro.engine.*;
 import org.webmacro.*;
 
 import java.io.IOException;
 
 /**
  * #count $i from 1 to 100 [step 1]
  */
 public class CountDirective extends org.webmacro.directive.Directive {
 
     private static final int COUNT_ITERATOR = 1;
     private static final int COUNT_FROM_K = 2;
     private static final int COUNT_START = 3;
     private static final int COUNT_TO_K = 4;
     private static final int COUNT_END = 5;
     private static final int COUNT_STEP_K = 6;
     private static final int COUNT_STEP = 7;
     private static final int COUNT_BODY = 8;
 
     private static final Directive.ArgDescriptor[] _args = new Directive.ArgDescriptor[]{
         new Directive.LValueArg(COUNT_ITERATOR),
         new Directive.KeywordArg(COUNT_FROM_K, "from"),
         new Directive.RValueArg(COUNT_START),
         new Directive.KeywordArg(COUNT_TO_K, "to"),
         new Directive.RValueArg(COUNT_END),
         new Directive.OptionalGroup(2),
         new Directive.KeywordArg(COUNT_STEP_K, "step"),
         new Directive.RValueArg(COUNT_STEP),
         new Directive.BlockArg(COUNT_BODY),
     };
 
     private static final DirectiveDescriptor _desc = new DirectiveDescriptor("for", null, _args, null);
 
     public static DirectiveDescriptor getDescriptor() {
         return _desc;
     }
 
     private Variable _iterator;
     private Macro _body;
     private Object _objStart;
     private Object _objEnd;
     private Object _objStep;
 
     private int _start, _end, _step = 1;
 
     public Object build(DirectiveBuilder builder, BuildContext bc) throws BuildException {
         try {
             _iterator = (Variable) builder.getArg(COUNT_ITERATOR, bc);
         } catch (ClassCastException e) {
             throw new Directive.NotVariableBuildException(_desc.name, e);
         }
         _objStart = builder.getArg(COUNT_START, bc);
         _objEnd = builder.getArg(COUNT_END, bc);
         _objStep = builder.getArg(COUNT_STEP, bc);
         _body = (Block) builder.getArg(COUNT_BODY, bc);
 
         // attempt to go ahead and force the start, end, and step values into primitive ints
         if (_objStart != null) {
             if (_objStart instanceof Number) {
                 _start = ((Number) _objStart).intValue();
                 _objStart = null;
             } else if (_objStart instanceof String) {
                 _start = Integer.parseInt((String) _objStart);
                 _objStart = null;
             }
         }
         if (_objEnd != null) {
             if (_objEnd instanceof Number) {
                 _end = ((Number) _objEnd).intValue();
                 _objEnd = null;
             } else if (_objEnd instanceof String) {
                 _end = Integer.parseInt((String) _objEnd);
                 _objEnd = null;
             }
         }
         if (_objStep != null) {
             if (_objStep instanceof Number) {
                 _step = ((Number) _objStep).intValue();
                 _objStep = null;
             } else if (_objStep instanceof String) {
                 _step = Integer.parseInt((String) _objStep);
                 _objStep = null;
             }
         }
 
         return this;
     }
 
     public void write(FastWriter out, Context context) throws PropertyException, IOException {
         int start=_start, end=_end, step = _step;
         Object tmp;
 
         // if necessary, do run-time evaluation of our
         // start, end, and step objects.
         if ( (tmp = _objStart) != null) {
             while (tmp instanceof Macro)
                 tmp = ((Macro) tmp).evaluate(context);
             if (tmp != null)
                 start = Integer.parseInt(tmp.toString());
             else
                 throw new PropertyException ("Starting value cannot be null");
         }
 
         if ( (tmp = _objEnd) != null) {
             while (tmp instanceof Macro)
                 tmp = ((Macro) tmp).evaluate(context);
             if (tmp != null)
                 end = Integer.parseInt(tmp.toString());
             else
                 throw new PropertyException ("Ending value cannot be null");
         }
 
         if ( (tmp = _objStep) != null) {
             while (tmp instanceof Macro)
                 tmp = ((Macro) tmp).evaluate(context);
             if (tmp != null)
                 step = Integer.parseInt(tmp.toString());
             else
                 throw new PropertyException ("Step value cannot be null");
         }
 
         // just do it
        for (; start<=end; start+=step) {
             _iterator.setValue(context, new Integer(start));
             _body.write(out, context);
         }
     }
 }
