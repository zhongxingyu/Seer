 import sun.reflect.ReflectionFactory;
 
 import java.lang.reflect.Method;
 import java.lang.RuntimeException;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.jmock.api.Invocation;
 import org.jmock.lib.action.CustomAction;
 
 /**
  * This is use in jmock, if you used some other mock framework just need a little change.
  */
 public class ActionWithParameter extends CustomAction {
     private static final String HANDLE_METHOD_NAME = "handle";

     private Method handler;
     private int[] parameterFilter;
 
     public ActionWithParameter(int ... parameterFilter) {
         super("ActionWithParameter");
        for (Method m : this.getClass().getDeclaredMethods()) {
             if (m.getName().equals(HANDLE_METHOD_NAME)) {
                 handler = m;
                 if (parameterFilter.length != 0
                         && handler.getParameterTypes().length != parameterFilter.length) {
                     throw new RuntimeException("handler parameter number wrong");
                 }
             }
         }
         if (handler == null) {
             throw new RuntimeException("no handler");
         }
         this.parameterFilter = parameterFilter;
     }
 
    @Override
     public Object invoke(Invocation invocation) throws Throwable {
         Object[] param = null;
         if (parameterFilter.length == 0) {
             param = invocation.getParametersAsArray();
         } else {
             List<Object> parameter = new LinkedList<Object>();
             for (int i : parameterFilter) {
                 parameter.add(invocation.getParameter(i));
             }
             param = parameter.toArray();
         }
         // Don't use Method.invoke because it check if the Class of this can be accessed.
         // Such as Anonymous Classes in some method can't accessed public, it will fail the check.
         // So I use the way Method.invoke do except the accessiblity check.
         return ReflectionFactory.getReflectionFactory().newMethodAccessor(handler).invoke(this, param);
     }
 }
