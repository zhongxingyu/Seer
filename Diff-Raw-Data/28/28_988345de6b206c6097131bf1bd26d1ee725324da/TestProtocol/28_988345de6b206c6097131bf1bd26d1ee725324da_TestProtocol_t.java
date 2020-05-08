 package org.aldeon.protocol;
 
 import org.aldeon.common.Observer;
 import org.aldeon.protocol.action.Action;
 import org.aldeon.protocol.action.ExampleAction;
 import org.aldeon.protocol.query.ExampleQuery;
 import org.aldeon.protocol.query.Query;
 import org.aldeon.protocol.response.Response;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class TestProtocol implements Protocol {
 
     private Map<Class<?>, Action<? extends Query, ?>> actions;
 
     public TestProtocol() {
         actions = new HashMap<>();
 
         // Register all the actions here
         actions.put(ExampleQuery.class, new ExampleAction());
     }
 
    @SuppressWarnings("unchecked")
     @Override
     public Response respond(Query query, Observer observer) {
 
         Action<? extends Query,?> action = actions.get(query.getClass());
 
         return (action == null)
                 ? null
                : ((Action<Query, ?>) action).respond(query, observer); //the cast here if fine - Query is abstract
     }
 }
