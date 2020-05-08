 package com.aol.wsdl;
 
import org.junit.Test;
 import org.junit.Assert;
 
 import java.util.HashMap;
 
 public class ExecuteOperationActionTest {
 
     @Test
     public void shouldCallSimpleWSDLMethod() throws Exception {
        ServiceLocator serviceLocator = new ServiceLocator();
         String operation = "getAvailability";
         FieldDescriptor fieldDescriptor = serviceLocator.createFieldDescriptor(operation);
         ExecuteOperationAction action = new ExecuteOperationAction();
         HashMap map = new HashMap();
         String value = "something";
         map.put("dslNumber", value);
         action.setParameters(map);
         action.getValues(fieldDescriptor);
         Assert.assertEquals(value, fieldDescriptor.get(0).getValue());
     }
 }
