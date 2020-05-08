 package com.timgroup.tucker.info.status;
 
 import static com.timgroup.tucker.info.Status.OK;
 import static java.util.Arrays.asList;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 import static org.mockito.Mockito.doNothing;
 import static org.mockito.Mockito.doThrow;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.spy;
 import static org.mockito.Mockito.verify;
 
 import java.util.List;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import com.timgroup.tucker.info.Component;
 import com.timgroup.tucker.info.Report;
 import com.timgroup.tucker.info.Status;
 import com.timgroup.tucker.info.component.VersionComponent;
 
 public class AsyncStatusPageGeneratorTest {
 
     private final VersionComponent versionComponent = new VersionComponent() {
         @Override public Report getReport() {
             return new Report(Status.INFO, "0.0.1");
         }
     };
     
     @Test
     public void startsAllComponents() {
         AsyncComponent first = mock(AsyncComponent.class);
         AsyncComponent second = mock(AsyncComponent.class);
         AsyncComponent third = mock(AsyncComponent.class);
         
         AsyncStatusPageGenerator generator = new AsyncStatusPageGenerator("my-app", versionComponent, asList(first, second, third));
         
         generator.start();
         
         verify(first).start();
         verify(second).start();
         verify(third).start();
     }
     
     @Test
     public void stopsAllComponents() throws Exception {
         AsyncComponent first = mock(AsyncComponent.class);
         AsyncComponent second = mock(AsyncComponent.class);
         AsyncComponent third = mock(AsyncComponent.class);
         
         AsyncStatusPageGenerator generator = new AsyncStatusPageGenerator("my-app", versionComponent, asList(first, second, third));
         
         generator.start();
         generator.stop();
         
         verify(first).stop();
         verify(second).stop();
         verify(third).stop();
     }
     
     @Test
    public void attemptsToStopAllComponentsAndGathersErrorsWhenAnyFail() {
         AsyncComponent first = spy(AsyncComponent.wrapping(component("first-id")).build());
         AsyncComponent second = spy(AsyncComponent.wrapping(component("second-id")).build());
         AsyncComponent third = spy(AsyncComponent.wrapping(component("third-id")).build());
         AsyncComponent fourth = spy(AsyncComponent.wrapping(component("fourth-id")).build());
 
         InterruptedException lastThrown = new InterruptedException("third barfed");
         
         try {
             doThrow(new InterruptedException("second borked")).when(second).stop();
             doThrow(lastThrown).when(third).stop();
             
             List<AsyncComponent> allComponents = asList(first, second, third, fourth);
             for (AsyncComponent component : allComponents) {
                 doNothing().when(component).start();
             }
         
             AsyncStatusPageGenerator generator = new AsyncStatusPageGenerator("my-app", versionComponent, allComponents);
         
             generator.start();
             generator.stop();
             
             fail("Expected exception");
         } catch (InterruptedException e) {
             assertEquals("Failed to stop components: [second-id, third-id]. Last failure: third barfed", e.getMessage());
             Assert.assertSame(lastThrown, e.getCause());
         }
         
     }
     
     private Component component(String id) {
         return new Component(id, "The Label") {
             @Override public Report getReport() { return new Report(OK, "All good"); }
         };
     }
 
 }
