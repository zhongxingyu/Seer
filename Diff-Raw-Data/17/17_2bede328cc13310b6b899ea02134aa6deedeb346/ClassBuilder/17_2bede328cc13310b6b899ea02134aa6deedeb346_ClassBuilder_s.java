 package jeez.interpreter.execution;
 
 import static jeez.interpreter.execution.Bootstrap.getObjectClass;
 import static jeez.lang.Constants.NEW;
 
 import java.util.List;
 
 import jeez.lang.JeezClass;
 import jeez.lang.JeezObject;
 import jeez.lang.Method;
 import jeez.lang.expression.Expression;
 
 public class ClassBuilder {
   
   public JeezClass build(String name) {
     final JeezClass clazz = new JeezClass(name);
     clazz.setSuperClass(getObjectClass());
     
     Method newMethod = new Method(clazz, clazz, NEW) {
       @Override
       public JeezObject execute(JeezObject target, List<Expression> arguments, ExecutionContext context) {
         return clazz.createNewObject(arguments, context);
       }
       
       @Override
       public boolean acceptArguments(List<Expression> arguments) {
         return true;
       }
     };
     clazz.addToMethods(newMethod);
     
     return clazz;
   }
 }
