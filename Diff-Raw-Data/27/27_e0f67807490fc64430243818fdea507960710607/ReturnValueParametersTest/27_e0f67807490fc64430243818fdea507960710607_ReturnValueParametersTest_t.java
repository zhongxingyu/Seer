 package org.polyforms.event.spring;
 
 import org.easymock.EasyMock;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.polyforms.parameter.Parameter;
 import org.polyforms.parameter.ReturnParameterAware;
 
 public class ReturnValueParametersTest {
     private ReturnValueParameters<Parameter> returnValueParameters;
     private ReturnParameterAware<Parameter> parameters;
     private final Parameter parameter1 = createParameter(String.class, "name", 0);
     private final Parameter parameter2 = createParameter(Integer.class, "index", 1);
     private final Parameter parameter3 = createParameter(Integer.class, "returnValue", 2);
 
     @Before
     @SuppressWarnings("unchecked")
     public void setUp() {
         parameters = EasyMock.createMock(ReturnParameterAware.class);
         returnValueParameters = new ReturnValueParameters<Parameter>(parameters);
         parameters.getParameters();
         EasyMock.expectLastCall().andReturn(new Parameter[] { parameter1, parameter2 });
     }
 
     private Parameter createParameter(final Class<?> type, final String name, final int index) {
         final Parameter parameter = new Parameter();
         parameter.setType(type);
         parameter.setName(name);
         parameter.setIndex(index);
         return parameter;
     }
 
     @Test
     public void getParameters() {
         parameters.getReturnParameter();
         EasyMock.expectLastCall().andReturn(parameter3);
         EasyMock.replay(parameters);
 
         Assert.assertArrayEquals(new Parameter[] { parameter1, parameter2, parameter3 },
                 returnValueParameters.getParameters());
         EasyMock.verify(parameters);
     }
 
     @Test
     public void noReturnValue() {
         parameters.getReturnParameter();
         EasyMock.expectLastCall().andReturn(null);
         EasyMock.replay(parameters);
 
         Assert.assertArrayEquals(new Parameter[] { parameter1, parameter2 }, returnValueParameters.getParameters());
         EasyMock.verify(parameters);
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void newInstanceWithNull() {
         new ReturnValueParameters<Parameter>(null);
     }
 
     @Test
     public void hashcode() {
         Assert.assertEquals(parameters.hashCode(), returnValueParameters.hashCode());
     }
 
     @Test
     public void equalsSame() {
         Assert.assertTrue(returnValueParameters.equals(returnValueParameters));
     }
 
     @Test
     public void notEqualsNull() {
         Assert.assertFalse(returnValueParameters.equals(null));
     }
 
     @Test
     public void notEqualsOtherClass() {
         Assert.assertFalse(returnValueParameters.equals(new Object()));
     }
 
     @Test
     public void equals() {
         Assert.assertTrue(returnValueParameters.equals(new ReturnValueParameters<Parameter>(parameters)));
     }
 }
