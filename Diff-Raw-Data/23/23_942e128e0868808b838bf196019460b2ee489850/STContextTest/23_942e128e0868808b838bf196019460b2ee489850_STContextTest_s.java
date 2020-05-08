 /*
  * Copyright (c) 2013 KloudTek Ltd
  */
 
 package com.kloudtek.systyrant;
 
 import com.kloudtek.systyrant.exception.InvalidAttributeException;
 import com.kloudtek.systyrant.exception.ResourceCreationException;
 import com.kloudtek.systyrant.exception.STRuntimeException;
 import com.kloudtek.systyrant.resource.AbstractAction;
 import com.kloudtek.systyrant.resource.Resource;
 import com.kloudtek.systyrant.resource.SyncAction;
 import org.mockito.Mockito;
 import org.testng.annotations.Test;
 
 import java.util.List;
 
 import static com.kloudtek.systyrant.resource.Resource.State.EXECUTED;
 import static com.kloudtek.systyrant.resource.Resource.State.FAILED;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 import static org.testng.Assert.*;
 
 public class STContextTest extends AbstractContextTest {
     @Test
     public void testSimpleElementCreation() throws Throwable {
         createTestResource("test1");
         execute();
     }
 
 
     @Test(dependsOnMethods = "testSimpleElementCreation", expectedExceptions = ResourceCreationException.class)
     public void testCreateElementDuringExecutionStage() throws Throwable {
         Resource test1 = createTestResource("test1");
         test1.addAction(Stage.EXECUTE, new AbstractAction() {
             @Override
             public void execute(STContext context, Resource resource, Stage stage, boolean postChildren) throws STRuntimeException {
                 try {
                     context.getResourceManager().createResource(TEST);
                 } catch (ResourceCreationException e) {
                     throw new STRuntimeException(e.getMessage(),e);
                 }
             }
         });
         execute();
     }
 
     @Test
     public void testResourcesDependenciesAndSorting() throws ResourceCreationException, STRuntimeException {
         Resource test1 = createTestResource("test1");
         Resource test2 = createTestResource("test2");
         Resource test3 = createTestResource("test3");
         Resource test4 = createTestResource("test4");
         test1.addDependency(test2);
         test2.addDependency(test3);
         test2.addDependency(test4);
         String child1 = "test3_child1";
         TestResource.createChild(test3, child1);
         test4.addDependency(test3);
         assertTrue(ctx.execute());
         List<Resource> els = resourceManager.findResourcesById(child1);
         assertEquals(els.size(), 1);
         Resource test3_child1 = els.get(0);
         assertTrue(test3_child1.getDependencies().contains(test3));
         assertBefore(test2, test1);
         assertBefore(test3, test2, test3_child1);
         assertBefore(test4, test2);
         assertBefore(test4, test2);
     }
 
     @Test
     public void testDependenciesSetAsBeforeAttr() throws Throwable {
         Resource r1 = createTestResource();
         Resource r2 = createTestResource("someid");
         Resource r3 = createTestResource();
         r3.set("before", "someid");
         Resource r4 = createTestResource();
         createTestResource();
         execute();
         assertTrue(r1.getDependencies().isEmpty());
        assertTrue(r2.getDependencies().isEmpty());
        assertEquals(r3.getDependencies().size(), 1);
        assertSame(r3.getDependencies().iterator().next(), r2);
         assertTrue(r4.getDependencies().isEmpty());
     }
 
     @Test
     public void testDependenciesSetAsAfterAttr() throws Throwable {
         Resource r1 = createTestResource();
         Resource r2 = createTestResource("someid");
         Resource r3 = createTestResource();
         r3.set("after", "someid");
         Resource r4 = createTestResource();
         createTestResource();
         execute();
         assertTrue(r1.getDependencies().isEmpty());
        assertEquals(r2.getDependencies().size(), 1);
        assertSame(r2.getDependencies().iterator().next(), r3);
        assertTrue(r3.getDependencies().isEmpty());
         assertTrue(r4.getDependencies().isEmpty());
     }
 
     @Test()
     public void testCreateSingleUniqueElements() throws Throwable {
         ctx.getResourceManager().createResource(UNIQUETEST);
         execute();
     }
 
     @Test(dependsOnMethods = "testCreateSingleUniqueElements", expectedExceptions = ResourceCreationException.class, expectedExceptionsMessageRegExp = "Cannot create more than one instance of test:uniquetest")
     public void testCreateDuplicateUniqueElements() throws Throwable {
         ctx.getResourceManager().createResource(UNIQUETEST);
         ctx.getResourceManager().createResource(UNIQUETEST);
         execute();
     }
 
     @Test
     public void testGenerateId() throws STRuntimeException, InvalidAttributeException, ResourceCreationException {
         Resource el1 = createTestResource("uid", "test2");
         Resource el2 = createTestResource("id", "testval");
         Resource el3 = createTestResource();
         Resource el4 = createTestResource();
         ctx.validateAndGenerateIdentifiers();
         assertEquals(el1.getUid(), "test2");
         assertEquals(el2.getUid(), "testval");
         assertEquals(el3.getUid(), "test:test1");
         assertEquals(el4.getUid(), "test:test2");
     }
 
     @Test
     public void testFailurePropagation() throws InvalidAttributeException, STRuntimeException, ResourceCreationException {
         ctx.clearFatalException();
         Resource el1 = createTestResource("1");
         el1.addAction(Stage.EXECUTE, new FailAction());
         Resource el2 = createTestResource("2", el1);
         Resource el3 = createTestResource("3", el2);
         Resource el4 = createTestResource("4", el3);
         Resource el5 = createTestResource("5");
         el5.addAction(Stage.PREPARE, new FailAction());
         Resource el6 = createTestResource("6", el5);
         Resource el7 = createTestResource("7", el6);
         Resource el8 = createTestResource("8");
         Resource el9 = createTestResource("9");
         assertFalse(ctx.execute());
         assertEquals(el1.getState(), FAILED);
         assertEquals(el2.getState(), FAILED);
         assertEquals(el3.getState(), FAILED);
         assertEquals(el4.getState(), FAILED);
         assertEquals(el5.getState(), FAILED);
         assertEquals(el6.getState(), FAILED);
         assertEquals(el7.getState(), FAILED);
         assertSame(el8.getState(), EXECUTED);
         assertSame(el9.getState(), EXECUTED);
     }
 
     @Test
     public void testPostChildrenExecution() throws Throwable {
         Resource rs1 = createJavaTestResource("1");
         Resource rs2 = createChildTestResource("2", rs1);
         Resource rs3 = createChildTestResource("3", rs2);
         Resource rs4 = createChildTestResource("4", rs3);
         Resource rs5 = createJavaTestResource("5", rs4);
         execute();
         Integer rs1ts = rs1.getJavaImpl(TestResource.class).getPostChildrenOrder();
         Integer rs2ts = rs2.getJavaImpl(TestResource.class).getPostChildrenOrder();
         Integer rs3ts = rs3.getJavaImpl(TestResource.class).getPostChildrenOrder();
         Integer rs4ts = rs4.getJavaImpl(TestResource.class).getPostChildrenOrder();
         Integer rs5ts = rs5.getJavaImpl(TestResource.class).getPostChildrenOrder();
         assertNotNull(rs1ts);
         assertNotNull(rs2ts);
         assertNotNull(rs3ts);
         assertNull(rs4ts);
         assertNull(rs5ts);
         assertTrue(rs3ts.intValue() > 0);
         assertEquals(rs2ts.intValue(), rs3ts + 1);
         assertEquals(rs1ts.intValue(), rs2ts + 1);
     }
 
     @Test(expectedExceptions = InvalidAttributeException.class)
     public void testConflictingUid() throws Throwable {
         Resource rs1 = createTestResource();
         rs1.setUid("myuid");
         Resource rs2 = createTestResource();
         rs2.setUid("myuid");
         execute();
     }
 
     @Test(expectedExceptions = InvalidAttributeException.class)
     public void testConflictingId() throws Throwable {
         createTestResource("id1");
         createTestResource("id1");
         execute();
     }
 
     @Test
     public void testVerifyNoChange() throws Throwable {
         Resource res = createTestResource("x");
         SyncAction action = Mockito.mock(SyncAction.class);
         res.addAction(Stage.EXECUTE, action);
         when(action.verify(ctx, res, Stage.EXECUTE, false)).thenReturn(false);
         execute();
         verify(action, Mockito.times(1)).verify(ctx, res, Stage.EXECUTE, false);
         verify(action, Mockito.times(1)).execute(ctx, res, Stage.EXECUTE, false);
     }
 
     @Test
     public void testVerifyChanged() throws Throwable {
         Resource res = createTestResource("x");
         SyncAction action = Mockito.mock(SyncAction.class);
         res.addAction(Stage.EXECUTE, action);
         when(action.verify(ctx, res, Stage.EXECUTE, false)).thenReturn(true);
         execute();
         verify(action, Mockito.times(1)).verify(ctx, res, Stage.EXECUTE, false);
         verify(action, Mockito.never()).execute(ctx, res, Stage.EXECUTE, false);
     }
 }
