 /*
  * See the NOTICE file distributed with this work for additional
  * information regarding copyright ownership.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.xwiki.editor.tool.autocomplete.internal;
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import static org.junit.Assert.*;
 import static org.mockito.Mockito.*;
 
 import org.apache.velocity.VelocityContext;
 import org.apache.velocity.context.Context;
 import org.junit.*;
 import org.xwiki.component.manager.ComponentManager;
 import org.xwiki.editor.tool.autocomplete.AutoCompletionMethodFinder;
 import org.xwiki.editor.tool.autocomplete.HintData;
 import org.xwiki.editor.tool.autocomplete.Hints;
 import org.xwiki.editor.tool.autocomplete.TargetContent;
 import org.xwiki.editor.tool.autocomplete.TargetContentLocator;
 import org.xwiki.editor.tool.autocomplete.TargetContentType;
 import org.xwiki.script.service.ScriptServiceManager;
 import org.xwiki.test.mockito.MockitoComponentMockingRule;
 import org.xwiki.velocity.VelocityManager;
 
 /**
  * Unit tests for {@link AutoCompletionResource}.
  * 
  * @version $Id:$
  */
 public class AutoCompletionResourceTest
 {
     @Rule
     public MockitoComponentMockingRule<AutoCompletionResource> mocker =
         new MockitoComponentMockingRule<AutoCompletionResource>(AutoCompletionResource.class);
 
     private class AncillaryTestClass
     {
         public void method()
         {
         }
     }
 
     private class TestClass
     {
         public AncillaryTestClass doWork()
         {
             return new AncillaryTestClass();
         }
 
         public String getSomething()
         {
             return "";
         }
         
         public String getSomething(String parameter)
         {
             return "";
         }
 
         public void method1()
         {
         }
 
         public String method2()
         {
             return "";
         }
     }
 
     @Test
     public void getAutoCompletionHintsWhenNoDollarSign() throws Exception
     {
         setupMocks("whatever", new VelocityContext());
         String velocity = "{{velocity}}whatever";
 
         Hints hints = mocker.getComponentUnderTest().getAutoCompletionHints(velocity.length(), "xwiki/2.0", velocity);
 
         assertEquals(0, hints.getHints().size());
     }
 
     @Test
     public void getAutoCompletionHintsWhenOnlyDollarSign() throws Exception
     {
         // Note that we create nested Velocity Context in order to verify that we support that in getAutoCompletionHints
         Context innerContext = new VelocityContext();
         innerContext.put("key1", "value1");
         VelocityContext vcontext = new VelocityContext(innerContext);
         vcontext.put("key2", "value2");
         setupMocks("$", vcontext);
 
         String velocity = "{{velocity}}$";
 
         Hints hints = mocker.getComponentUnderTest().getAutoCompletionHints(velocity.length(), "xwiki/2.0", velocity);
 
         // Verify we can get keys from the Velocity Context and its inner context too. We test this since our
         // Velocity tools are put in an inner Velocity Context.
         assertEquals(5, hints.getHints().size());
 
         // Also verifies that hints are sorted
         SortedSet<HintData> expected = new TreeSet<HintData>();
         expected.addAll(Arrays.asList(
             new HintData("doc", "doc"),
             new HintData("key1", "key1"),
             new HintData("key2", "key2"),
             new HintData("sdoc", "sdoc"),
             new HintData("tdoc", "tdoc")
         ));
         assertEquals(expected, hints.getHints());
     }
 
     @Test
     public void getAutoCompletionHintsWhenOnlyDollarAndBangSigns() throws Exception
     {
         setupMocks("$!", createTestVelocityContext("key", "value", "otherKey", "otherValue"));
 
         String velocity = "{{velocity}}$!";
         Hints hints = mocker.getComponentUnderTest().getAutoCompletionHints(velocity.length(), "xwiki/2.0", velocity);
 
         assertEquals(5, hints.getHints().size());
         assertTrue(hints.getHints().contains(new HintData("key", "key")));
         assertTrue(hints.getHints().contains(new HintData("otherKey", "otherKey")));
     }
 
     @Test
     public void getAutoCompletionHintsWhenOnlyDollarAndCurlyBracketSigns() throws Exception
     {
         setupMocks("${", createTestVelocityContext("key", "value"));
 
         String velocity = "{{velocity}}${";
         Hints hints = mocker.getComponentUnderTest().getAutoCompletionHints(velocity.length(), "xwiki/2.0", velocity);
 
         assertEquals(4, hints.getHints().size());
         assertTrue(hints.getHints().contains(new HintData("key", "key")));
     }
 
     @Test
     public void getAutoCompletionHintsWhenOnlyDollarBangAndCurlyBracketSigns() throws Exception
     {
         setupMocks("$!{", createTestVelocityContext("key", "value"));
 
         String velocity = "{{velocity}}$!{";
         Hints hints = mocker.getComponentUnderTest().getAutoCompletionHints(velocity.length(), "xwiki/2.0", velocity);
 
         assertEquals(4, hints.getHints().size());
         assertTrue(hints.getHints().contains(new HintData("key", "key")));
     }
 
     @Test
     public void getAutoCompletionHintsWhenDollarSignFollowedBySomeLetters() throws Exception
     {
         setupMocks("$ke", createTestVelocityContext("key", "value", "otherKey", "otherValue"));
 
         String velocity = "{{velocity}}$ke";
         Hints hints = mocker.getComponentUnderTest().getAutoCompletionHints(velocity.length(), "xwiki/2.0", velocity);
 
         assertEquals(1, hints.getHints().size());
         assertTrue(hints.getHints().contains(new HintData("key", "key")));
     }
 
     @Test
     public void getAutoCompletionHintsWhenDollarSignFollowedByBangSymbol() throws Exception
     {
         setupMocks("$!ke", createTestVelocityContext("key", "value", "otherKey", "otherValue"));
 
         String velocity = "{{velocity}}$!ke";
         Hints hints = mocker.getComponentUnderTest().getAutoCompletionHints(velocity.length(), "xwiki/2.0", velocity);
 
         assertEquals(1, hints.getHints().size());
         assertTrue(hints.getHints().contains(new HintData("key", "key")));
     }
 
     @Test
     public void getAutoCompletionHintsWhenDollarSignFollowedByCurlyBracketSymbol() throws Exception
     {
         setupMocks("${ke", createTestVelocityContext("key", "value", "otherKey", "otherValue"));
 
         String velocity = "{{velocity}}${ke";
         Hints hints = mocker.getComponentUnderTest().getAutoCompletionHints(velocity.length(), "xwiki/2.0", velocity);
 
         assertEquals(1, hints.getHints().size());
         assertTrue(hints.getHints().contains(new HintData("key", "key")));
     }
 
     @Test
     public void getAutoCompletionHintsWhenDollarSignFollowedByBangAndCurlyBracketSymbol() throws Exception
     {
         setupMocks("$!{ke", createTestVelocityContext("key", "value", "otherKey", "otherValue"));
 
         String velocity = "{{velocity}}$!{ke";
         Hints hints = mocker.getComponentUnderTest().getAutoCompletionHints(velocity.length(), "xwiki/2.0", velocity);
 
         assertEquals(1, hints.getHints().size());
         assertTrue(hints.getHints().contains(new HintData("key", "key")));
     }
 
     @Test
     public void getAutoCompletionHintsWhenDollarSignFollowedBySomeNonMatchingLetters() throws Exception
     {
         setupMocks("$o", createTestVelocityContext("key", "value"));
 
         String velocity = "{{velocity}}$o";
         Hints hints = mocker.getComponentUnderTest().getAutoCompletionHints(velocity.length(), "xwiki/2.0", velocity);
 
         assertEquals(0, hints.getHints().size());
     }
 
     @Test
     public void getAutoCompletionHintsWhenInvalidAutoCompletion() throws Exception
     {
         setupMocks("$k ", createTestVelocityContext());
 
         String velocity = "{{velocity}}$k ";
         Hints hints = mocker.getComponentUnderTest().getAutoCompletionHints(velocity.length(), "xwiki/2.0", velocity);
 
         assertEquals(0, hints.getHints().size());
     }
 
     @Test
     public void getAutoCompletionHintsForMethodWhenJustAfterTheDot() throws Exception
     {
         setupMocks("$key.", createTestVelocityContext("key", new TestClass()));
 
         Hints expectedMethods = new Hints().withHints(
             new HintData("doWork", "doWork(...) AncillaryTestClass"),
             new HintData("something", "something String"),
             new HintData("getSomething", "getSomething(...) String"),
             new HintData("method1", "method1(...)"),
             new HintData("method2", "method2(...) String")
         );
         setupMethodFinderMock(expectedMethods, "", TestClass.class);
 
         String velocity = "{{velocity}}$key.";
         Hints hints = mocker.getComponentUnderTest().getAutoCompletionHints(velocity.length(), "xwiki/2.0", velocity);
 
         assertEquals(5, hints.getHints().size());
 
         // Verify methods are returned sorted
         SortedSet<HintData> expected = new TreeSet<HintData>();
         expected.add(new HintData("doWork", "doWork(...) AncillaryTestClass"));
         expected.add(new HintData("getSomething", "getSomething(...) String"));
         expected.add(new HintData("method1", "method1(...)"));
         expected.add(new HintData("method2", "method2(...) String"));
         expected.add(new HintData("something", "something String"));
         assertEquals(expected, hints.getHints());
     }
 
     @Test
     public void getAutoCompletionHintsForMethodWhenThereIsContentAfterCursor() throws Exception
     {
         setupMocks("$key.do", createTestVelocityContext("key", new TestClass()));
 
         Hints expectedMethods = new Hints().withHints(new HintData("doWork", "doWork(...) AncillaryTestClass"));
         setupMethodFinderMock(expectedMethods, "do", TestClass.class);
 
         String velocity = "{{velocity}}$key.doWork";
         Hints hints =
             mocker.getComponentUnderTest().getAutoCompletionHints(velocity.length() - 4, "xwiki/2.0", velocity);
 
         assertEquals(1, hints.getHints().size());
 
         // Verify methods are returned sorted
         SortedSet<HintData> expected = new TreeSet<HintData>();
         expected.add(new HintData("doWork", "doWork(...) AncillaryTestClass"));
         assertEquals(expected, hints.getHints());
     }
 
     @Test
     public void getAutoCompletionHintsForMethodWhenThereIsContentAfterCursorWithCapitalLetters() throws Exception
     {
         setupMocks("$key.doW", createTestVelocityContext("key", new TestClass()));
 
         Hints expectedMethods = new Hints().withHints(new HintData("doWork", "doWork(...) AncillaryTestClass"));
         setupMethodFinderMock(expectedMethods, "doW", TestClass.class);
 
         String velocity = "{{velocity}}$key.doWork";
         Hints hints =
             mocker.getComponentUnderTest().getAutoCompletionHints(velocity.length() - 3, "xwiki/2.0", velocity);
 
         assertEquals(1, hints.getHints().size());
 
         // Verify methods are returned sorted
         SortedSet<HintData> expected = new TreeSet<HintData>();
         expected.add(new HintData("doWork", "doWork(...) AncillaryTestClass"));
         assertEquals(expected, hints.getHints());
     }
 
     @Test
     public void getAutoCompletionHintsForMethodsWhenGetter() throws Exception
     {
         setupMocks("$key.s", createTestVelocityContext("key", new TestClass()));
 
         Hints expectedMethods = new Hints().withHints(new HintData("something", "something String"));
         setupMethodFinderMock(expectedMethods, "s", TestClass.class);
 
         String velocity = "{{velocity}}$key.s";
         Hints hints = mocker.getComponentUnderTest().getAutoCompletionHints(velocity.length(), "xwiki/2.0", velocity);
 
         assertEquals(1, hints.getHints().size());
     }
 
     @Test
     public void getAutoCompletionHintsForMethod() throws Exception
     {
         setupMocks("$key.m", createTestVelocityContext("key", new TestClass()));
 
         Hints expectedMethods = new Hints().withHints(
             new HintData("method", "method1(...)"),
             new HintData("method2", "method2(...) String")
         );
         setupMethodFinderMock(expectedMethods, "m", TestClass.class);
 
         String velocity = "{{velocity}}$key.m";
         Hints hints = mocker.getComponentUnderTest().getAutoCompletionHints(velocity.length(), "xwiki/2.0", velocity);
 
         assertEquals(2, hints.getHints().size());
     }
 
     @Test
     public void getAutoCompletionHintsForScriptServiceProperty() throws Exception
     {
         ComponentManager cm = mocker.getInstance(ComponentManager.class);
         AutoCompletionMethodFinder scriptServiceFinder = mock(AutoCompletionMethodFinder.class);
         when(cm.hasComponent(AutoCompletionMethodFinder.class, "services")).thenReturn(true);
         when(cm.getInstance(AutoCompletionMethodFinder.class, "services")).thenReturn(scriptServiceFinder);
 
         ScriptServiceManager scriptServiceManager = mock(ScriptServiceManager.class);
         setupMocks("$services.t", createTestVelocityContext("services", scriptServiceManager));
         when(scriptServiceFinder.findMethods(scriptServiceManager.getClass(), "t")).thenReturn(
             new Hints().withHints(new HintData("test", "test")));
 
         String velocity = "{{velocity}}$services.t";
         Hints hints = mocker.getComponentUnderTest().getAutoCompletionHints(velocity.length(), "xwiki/2.0", velocity);
 
         assertEquals(1, hints.getHints().size());
         assertTrue(hints.getHints().contains(new HintData("test", "test")));
     }
 
     @Test
     @Ignore("Not working yet")
     public void getAutoCompletionHintsWhenSetDoneAbove() throws Exception
     {
         String velocity = "#set ($mydoc = $key.doWork())\n$mydoc.";
         setupMocks(velocity, createTestVelocityContext("key", new TestClass()));
 
         String content = "{{velocity}}" + velocity;
         Hints hints = mocker.getComponentUnderTest().getAutoCompletionHints(content.length(), "xwiki/2.0", content);
 
         assertEquals(2, hints.getHints().size());
     }
 
     @Test
     public void getAutoCompletionHintsForChainedMethod() throws Exception
     {
         setupMocks("$key.doWork().m", createTestVelocityContext("key", new TestClass()));
 
         Hints expectedMethods = new Hints().withHints(
             new HintData("method", "method")
         );
         AutoCompletionMethodFinder methodFinder = mocker.getInstance(AutoCompletionMethodFinder.class);
         when(methodFinder.findMethods(AncillaryTestClass.class, "m")).thenReturn(expectedMethods);
         when(methodFinder.findMethodReturnTypes(TestClass.class, "doWork")).thenReturn(
             Arrays.asList((Class) AncillaryTestClass.class));
 
         String velocity = "{{velocity}}$key.doWork().";
         Hints hints = mocker.getComponentUnderTest().getAutoCompletionHints(velocity.length(), "xwiki/2.0", velocity);
 
         assertEquals(1, hints.getHints().size());
         assertEquals(new HintData("method", "method"), hints.getHints().first());
        assertEquals(velocity.length() - 1, hints.getStartOffset());
     }
 
     @Test
     public void getAutoCompletionHintsForChainedMethodEndingWithDot() throws Exception
     {
         setupMocks("$key.doWork().", createTestVelocityContext("key", new TestClass()));
 
         Hints expectedMethods = new Hints().withHints(
             new HintData("method", "method")
         );
         AutoCompletionMethodFinder methodFinder = mocker.getInstance(AutoCompletionMethodFinder.class);
         when(methodFinder.findMethods(AncillaryTestClass.class, "")).thenReturn(expectedMethods);
         when(methodFinder.findMethodReturnTypes(TestClass.class, "doWork")).thenReturn(
             Arrays.asList((Class) AncillaryTestClass.class));
 
         String velocity = "{{velocity}}$key.doWork().";
         Hints hints = mocker.getComponentUnderTest().getAutoCompletionHints(velocity.length(), "xwiki/2.0", velocity);
 
         assertEquals(1, hints.getHints().size());
         assertEquals(new HintData("method", "method"), hints.getHints().first());
        assertEquals(velocity.length(), hints.getStartOffset());
     }
 
     private void setupMocks(String expectedContent, VelocityContext velocityContext) throws Exception
     {
         TargetContentLocator locator = mocker.getInstance(TargetContentLocator.class);
         when(locator.locate(anyString(), eq("xwiki/2.0"), anyInt())).thenReturn(
             new TargetContent(expectedContent, expectedContent.length(), TargetContentType.VELOCITY));
 
         VelocityManager velocityManager = mocker.getInstance(VelocityManager.class);
         when(velocityManager.getVelocityContext()).thenReturn(velocityContext);
     }
 
     private void setupMethodFinderMock(Hints expectedMethodNames, String fragmentToMatch, Class methodClass)
         throws Exception
     {
         AutoCompletionMethodFinder methodFinder = mocker.getInstance(AutoCompletionMethodFinder.class);
         when(methodFinder.findMethods(methodClass, fragmentToMatch)).thenReturn(expectedMethodNames);
     }
 
     private VelocityContext createTestVelocityContext(Object... properties)
     {
         final VelocityContext context = new VelocityContext();
         for (int i = 0; i < properties.length; i += 2) {
             context.put((String) properties[i], properties[i + 1]);
         }
         return context;
     }
 }
