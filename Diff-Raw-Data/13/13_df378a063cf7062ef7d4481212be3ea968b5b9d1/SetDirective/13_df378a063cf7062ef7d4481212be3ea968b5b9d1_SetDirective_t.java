 package org.webmacro.directive;
 
 import java.io.*;
 import org.webmacro.*;
 import org.webmacro.engine.*;
 
 public class SetDirective extends Directive {
 
   private static final int SET_TARGET = 1;
  private static final int SET_RESULT = 2;
 
   private Variable target;
   private Object   result;
 
   private static final ArgDescriptor[] 
     myArgs = new ArgDescriptor[] {
       new LValueArg(SET_TARGET), 
      new AssignmentArg(),
       new RValueArg(SET_RESULT)
     };
 
   private static final DirectiveDescriptor 
     myDescr = new DirectiveDescriptor("set", SetDirective.class, myArgs, 
                                       null);
 
   public static DirectiveDescriptor getDescriptor() {
     return myDescr;
   }
 
   public Object build(DirectiveBuilder builder, 
                       BuildContext bc) 
   throws BuildException {
     target = (Variable) builder.getArg(SET_TARGET, bc);
     result = builder.getArg(SET_RESULT, bc);
     return this;
   }
 
   public void write(FastWriter out, Context context) 
     throws ContextException, IOException {
     boolean done=false;
 
     try {
       if (result instanceof Macro) 
         target.setValue(context, ((Macro) result).evaluate(context));
       else
         target.setValue(context, result);
     } catch (ContextException e) {
      String errorText = "Set: Unable to set value: " + target;
      context.getBroker().getLog("engine").error(errorText);
      writeWarning(errorText, out);
     }
   } 
 
   public void accept(TemplateVisitor v) {
     v.beginDirective(myDescr.name);
     v.visitDirectiveArg("SetTarget", target);
     v.visitDirectiveArg("SetValue", result);
     v.endDirective();
   }
 
 }
