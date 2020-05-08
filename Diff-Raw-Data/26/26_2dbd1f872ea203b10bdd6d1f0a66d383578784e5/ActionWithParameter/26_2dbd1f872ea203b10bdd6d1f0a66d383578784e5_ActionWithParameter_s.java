 import sun.reflect.ReflectionFactory;
 
 import java.lang.reflect.Method;
 import java.lang.RuntimeException;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.jmock.api.Invocation;
 import org.jmock.lib.action.CustomAction;
 
 /**
  * This is use in jmock, if you used some other mock framework just need a little change.
 *
 * Example:
 * 1, this will use all the parameter when call CallSomeMethod to invoke "handle"
 * EXCEPT(XXX).CallSomeMethod(p1, p2).will(new ActionWithParameter(){
        public void handle(P1 p1, P2 p2) {
            // do something.
        }
    })
 * 2, this will use the special parameter(first, third) of CallSomeMethod to invoke "handle"
 * EXCEPT(XXX).CallSomeMethod(p1, p2, p3, p4, p5).will(new ActionWithParameter(0, 2){
        public R handle(P1 p1, P3 p3) {
            // do something.
            return new R();
        }
    })
  */
 public class ActionWithParameter extends CustomAction {
     private static final String HANDLE_METHOD_NAME = "handle";
     private Method handler;
     private int[] parameterFilter;
 

     public ActionWithParameter(int ... parameterFilter) {
         super("ActionWithParameter");
        for (Method m : this.getClass().getMethods()) {
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
