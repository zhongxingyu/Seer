 /*
  * $Id: CdkTestRunner.java 16902 2010-05-05 23:50:39Z alexsmirnov $
  *
  * License Agreement.
  *
  * Rich Faces - Natural Ajax for Java Server Faces (JSF)
  *
  * Copyright (C) 2007 Exadel, Inc.
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License version 2.1 as published by the Free Software Foundation.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
  */
 
 package org.jboss.test.faces.mock;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.junit.runner.notification.RunNotifier;
 import org.junit.runners.BlockJUnit4ClassRunner;
 import org.junit.runners.model.FrameworkMethod;
 import org.junit.runners.model.InitializationError;
 
 /**
  * <p class="changed_added_4_0">
  * </p>
  * 
  * @author asmirnov@exadel.com
  * 
  */
 public class MockTestRunner extends BlockJUnit4ClassRunner {
 
     /**
      * <p class="changed_added_4_0">
      * </p>
      * 
      * @param klass
      * @throws InitializationError
      * @throws InitializationError
      */
     public MockTestRunner(Class<?> klass) throws InitializationError {
         super(klass);
     }
 
     /**
      * Gets all declared fields and all inherited fields.
      */
     protected Set<Field> getFields(Class<?> c) {
         Set<Field> fields = new HashSet<Field>(Arrays.asList(c.getDeclaredFields()));
         while ((c = c.getSuperclass()) != null) {
             for (Field f : c.getDeclaredFields()) {
                 if (!Modifier.isStatic(f.getModifiers()) && !Modifier.isPrivate(f.getModifiers())) {
                     fields.add(f);
                 }
             }
         }
         return fields;
     }
 
     @Override
     protected void runChild(FrameworkMethod method, RunNotifier notifier) {
         super.runChild(method, notifier);
     }
 
     @Override
     protected Object createTest() throws Exception {
         Class<?> c = getTestClass().getJavaClass();
         Set<Field> testFields = getFields(c);
 
         // make sure we have one (and only one) @Unit field
         // Field unitField = getUnitField(testFields);
         // if ( unitField.getAnnotation(Mock.class) != null ) {
         // throw new IncompatibleAnnotationException(Unit.class, Mock.class);
         // }
         //        
         final Map<Field, Binding> fieldValues = getMockValues(testFields);
         // if ( fieldValues.containsKey(unitField)) {
         // throw new IncompatibleAnnotationException(Unit.class, unitField.getType());
         // }
 
         Object test = super.createTest();
 
         // any field values created by AtUnit but not injected by the container are injected here.
         for (Field field : fieldValues.keySet()) {
             Binding binding = fieldValues.get(field);
             field.setAccessible(true);
             if (null != binding.getValue() && field.get(test) == null) {
                 field.set(test, binding.getValue());
             } 
         }
 
         return test;
     }
 
  
     protected static final class FieldModule implements MockController {
         final Map<Field, Binding> fields;
 
         public FieldModule(Map<Field, Binding> fields) {
             this.fields = fields;
         }
 
        public void replay() {
             for (Binding field : fields.values()) {
                 if(field.isMock()){
                     FacesMock.replay(field.getValue());
                 }
             }
         }
 
        public void verify() {
             for (Binding field : fields.values()) {
                 if(field.isMock()){
                     FacesMock.verify(field.getValue());
                 }
             }
         }
     }
 
     /**
      * <p class="changed_added_4_0">Binding definition storage</p>
      * @author asmirnov@exadel.com
      *
      */
     protected static final class Binding {
         private Object value;
         private boolean mock;
         protected Binding() {
         }
         /**
          * <p class="changed_added_4_0"></p>
          * @param value the value to set
          */
         void setValue(Object value) {
             this.value = value;
         }
         /**
          * <p class="changed_added_4_0"></p>
          * @return the value
          */
         Object getValue() {
             return value;
         }
         /**
          * <p class="changed_added_4_0"></p>
          * @param mock the mock to set
          */
         public void setMock(boolean mock) {
             this.mock = mock;
         }
         /**
          * <p class="changed_added_4_0"></p>
          * @return the mock
          */
         public boolean isMock() {
             return mock;
         }
     }
 
     private Map<Field, Binding> getMockValues(Set<Field> testFields) {
         Map<Field, Binding> mocksAndStubs = new HashMap<Field, Binding>();
         // TODO - create annotation attribute that tells runner to use the scme Mock Controller to create related mocks.
         for (Field field : testFields) {
             if (field.isAnnotationPresent(Strict.class)) {
                 mocksAndStubs.put(field, createMockBinding(field, FacesMock.createStrictMock(notEmpty(field.getAnnotation(Strict.class).value()),field.getType())));
             } if (field.isAnnotationPresent(Mock.class)) {
                     mocksAndStubs.put(field, createMockBinding(field, FacesMock.createMock(notEmpty(field.getAnnotation(Mock.class).value()),field.getType())));
             } else if (field.isAnnotationPresent(Stub.class)) {
                 mocksAndStubs.put(field, createMockBinding(field, FacesMock.createNiceMock(notEmpty(field.getAnnotation(Stub.class).value()),field.getType())));
             } else if(field.getType().isAssignableFrom(MockController.class)){
                 FieldModule module = new FieldModule(mocksAndStubs);
                 mocksAndStubs.put(field, createBinding(field, module));
             }
         }
 
         return mocksAndStubs;
     }
     
     private Binding createMockBinding(Field field,Object value) {
         Binding bind = createBinding(field,value);
         bind.setMock(true);
         if(field.isAnnotationPresent(Environment.class)){
             MockFacesEnvironment environment = (MockFacesEnvironment) value;
             for (Environment.Feature feature : field.getAnnotation(Environment.class).value()) {
                 switch(feature){
                     case EXTERNAL_CONTEXT:
                         environment.withExternalContext();
                         break;
                     case APPLICATION:
                         environment.withApplication();
                         break;
                     case FACTORIES:
                         environment.withFactories();
                         break;
                     case RENDER_KIT:
                         environment.withRenderKit();
                         break;
                     case SERVLET_REQUEST:
                         environment.withServletRequest();
                         break;
                 }
             }
         }
         return bind;
     }
 
     private Binding createBinding(Field field,Object value) {
         Binding bind = new Binding();
         bind.setValue(value);
         return bind;
     }
 
     private String notEmpty(String value) {
         return "".equals(value)?null:value;
     }
 }
