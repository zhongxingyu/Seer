 /*
  * Copyright 2009 Kjetil Valstadsve
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package vanadis.extrt;
 
 import junit.framework.Assert;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import vanadis.concurrent.OperationQueuer;
 import vanadis.concurrent.ThreadedDispatch;
 import vanadis.common.time.TimeSpan;
 import vanadis.ext.Inject;
 import vanadis.ext.ModuleSystemException;
 import vanadis.ext.Retract;
 import vanadis.osgi.Context;
 import vanadis.osgi.Registration;
 import vanadis.osgi.ServiceProperties;
 import vanadis.osgi.impl.BareBonesContext;
 
 import java.io.IOException;
 import java.util.concurrent.atomic.AtomicReference;
 
 @SuppressWarnings({"UnusedDeclaration"})
 public class InjectorTest {
 
     private Context context;
 
     @Before
     public void setUp() {
         context = new BareBonesContext();
     }
 
     @After
     public void tearDown() {
         context = null;
     }
 
     public interface HRService {
         void complain();
     }
 
     public static class HRServiceImpl implements HRService {
         @Override
         public void complain() {
             // ignore
         }
     }
 
     public class Employee {
 
         private final AtomicReference<HRService> service = new AtomicReference<HRService>();
 
         private final AtomicReference<HRService> illegalService = new AtomicReference<HRService>();
 
         @Inject(attributeName = "HR")
         public void connectWithHR(HRService service) {
             this.service.set(service);
         }
 
         public HRService getIllegalService() {
             return illegalService.get();
         }
 
         public HRService getService() {
             return service.get();
         }
 
         @Retract(attributeName = "HR")
         public void cutTiesWithHR(HRService service) {
             this.service.set(null);
             this.illegalService.set(service);
         }
     }
 
     @Test
     public void simpleInjection() throws IOException {
         Employee employee = new Employee();
         OperationQueuer operationQueuer = new ThreadedDispatch("dispatch", TimeSpan.seconds(5));
         ObjectManagerImpl.create(context, null, null, employee, null, operationQueuer);
 
         Registration<HRService> registration =
                 context.register(new HRServiceImpl(), ServiceProperties.create(HRService.class));
         operationQueuer.synchUp();
 
         Assert.assertNotNull(employee.getService());
         Assert.assertNull(employee.getIllegalService());
 
         registration.unregister();
         operationQueuer.synchUp();
 
         Assert.assertNotNull(employee + " has no illegal service", employee.getIllegalService());
         Assert.assertNull("Unexpected service: " + employee.getService(), employee.getService());
 
         operationQueuer.close();
     }
 
     @Test
     public void unemployableMalformedHRInteractionNoArgs() {
         dismiss(new Object() {
             @Inject
             public void setHRService() {
             }
         });
     }
 
     @Test
     public void unemployableMalformedHRInteractionTooManyArgs() {
         dismiss(new Object() {
             @Inject
             public void setHRService(HRService one, Long two, Long tree) {
             }
         });
     }
 
     @Test
     public void unemployableMalformedHRInteractionBadArgs() {
         dismiss(new Object() {
             @Inject
             public void setHRService(HRService one, Long two) {
             }
         });
     }
 
     @Test
     public void unemployableMalformedHRInteractionManyBadArgs() {
         dismiss(new Object() {
             @Inject
             public void setHRService(HRService one,
                                      ServiceProperties<HRService> serviceProperties,
                                      Long two) {
             }
         });
     }
 
     @Test
     public void employableOKArgs() {
         hire(new Object() {
             @Inject
             public void setHRService(HRService one) {
             }
         });
     }
 
     @Test
     public void employableOKArgsWithProperties() {
         hire(new Object() {
             @Inject
             public void setHRService(HRService one,
                                      ServiceProperties<HRService> serviceProperties) {
             }
         });
     }
 
     @Test
     public void hireImplicitRetract() {
         hire(new Object() {
             @Inject
             public void addHR(HRService hr) {
             }
 
             @Retract
             public void removeHR(HRService hr) {
             }
         });
     }
 
     @Test
     public void unemployableBecauseMininumCanNotBeSetForNonMulti() {
         dismiss(new Object() {
             @Inject(minimum = 2)
             public void setHRService(HRService service) {
             }
         });
     }
 
     @Test
     public void employableForField() {
         hire(new Object() {
             @Inject
             private HRService hrService;
         });
     }
 
     private void dismiss(Object employee) {
         try {
            Assert.fail(ObjectManagerImpl.create(context, null, null, employee, null, null) +
                     " should not get the job!");
        } catch (ModuleSystemException ignore) { }
     }
 
     private void hire(Object employee) {
        ObjectManagerImpl.create(context, null, null, employee, null, null);
     }
 }
