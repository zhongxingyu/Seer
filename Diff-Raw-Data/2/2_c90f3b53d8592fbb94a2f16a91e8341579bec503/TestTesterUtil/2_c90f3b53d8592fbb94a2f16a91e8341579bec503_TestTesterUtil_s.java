 /*******************************************************************************
  * Copyright (c) 2004, 2014 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.rc.swing.tester;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertFalse;
 import static junit.framework.Assert.fail;
 
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Label;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JTextField;
 import javax.swing.JToggleButton;
 
 import org.eclipse.jubula.rc.common.adaptable.AdapterFactoryRegistry;
 import org.eclipse.jubula.rc.common.adaptable.IAdapterFactory;
 import org.eclipse.jubula.rc.common.adaptable.ITextRendererAdapter;
 import org.eclipse.jubula.rc.common.exception.StepExecutionException;
 import org.eclipse.jubula.rc.swing.tester.util.TesterUtil;
 import org.eclipse.jubula.tools.messagehandling.MessageIDs;
 import org.junit.After;
 import org.junit.Test;
 
 @SuppressWarnings("serial")
 public class TestTesterUtil {
 
     private static class CanvasWithPublicGetText extends Canvas {
         public String getText() {
             return "CanvasWithPublicGetText"; 
         }
     }
 
     private static class CanvasWithOverriddenGetText extends CanvasWithPublicGetText {
         public String getText() {
             return "CanvasWithOverriddenGetText"; 
         }
     }
 
     private static class CanvasWithInvalidGetText extends Canvas {
         @SuppressWarnings("unused")
         public Color getText() {
             return Color.BLACK;
         }
     }
 
     private static class CanvasWithPrivateGetText extends Canvas {
         private String getText() {
             return "CanvasWithPrivateGetText"; 
         }
     }
 
     private static class CanvasWithNullGetText extends Canvas {
         @SuppressWarnings("unused")
         public String getText() {
             return null; 
         }
     }
 
     private static class CanvasWithExceptionGetText extends Canvas {
         @SuppressWarnings("unused")
         public String getText() {
             throw new RuntimeException(); 
         }
     }
 
     private final String m_renderedText = "RenderedText";
 
     private IAdapterFactory m_registeredAdapterFactory = null;
 
     private void assertRenderedTextEquals(
             String expectedText, Component renderer) {
         
         assertEquals(
                 expectedText, TesterUtil.getRenderedText(renderer));
     }
 
     private void assertRendererNotSupported(Component renderer) {
         try {
             TesterUtil.getRenderedText(renderer);
             fail();
         } catch (StepExecutionException e) {
             assertEquals(e.getErrorId(), MessageIDs.E_EVENT_SUPPORT);
         }
     }
     
     @After
     public void deregisterAdapterFactory() {
         if (m_registeredAdapterFactory != null) {
             AdapterFactoryRegistry.getInstance()
                 .signOffFactory(m_registeredAdapterFactory);
         }
     }
     
     @Test
     public void testRenderedNotSupported() {
         assertRendererNotSupported(new Canvas());
     }
     
     @Test
     public void testLabelRenderer() {
         assertRenderedTextEquals(
                 m_renderedText,
                 new Label(m_renderedText));
     }
     
     @Test
     public void testToggleButtonRenderer() {
         JToggleButton toggleButton = new JToggleButton("Button text", false);
 
         assertRenderedTextEquals(
                 Boolean.FALSE.toString(),
                 toggleButton);
         
         toggleButton.setSelected(true);
 
         assertRenderedTextEquals(
                 Boolean.TRUE.toString(),
                 toggleButton);
     }
     
     @Test
     public void testButtonRenderer() {
         assertRenderedTextEquals(
                 m_renderedText,
                 new JButton(m_renderedText));
     }
     
     @Test
     public void testTextComponentRenderer() {
         assertRenderedTextEquals(
                 m_renderedText,
                 new JTextField(m_renderedText));
     }
     
     @Test
     public void testNullRenderer() {
         assertRenderedTextEquals(
                 "",
                 new JLabel() {
                     @Override
                     public String getText() {
                         return null;
                     }
                 });
     }
     
     @SuppressWarnings("rawtypes")
     @Test
     public void testAdaptedRenderer() {
         final String canvasText = "CanvasRenderedText";
 
         m_registeredAdapterFactory = new IAdapterFactory() {
 
             public Class[] getSupportedClasses() {
                 return new Class[] {Canvas.class};
             }
             
             public Object getAdapter(Class targetAdapterClass, Object objectToAdapt) {
                 if (objectToAdapt instanceof Canvas) {
                     return new ITextRendererAdapter() {
                         public String getText() {
                             return canvasText;
                         }
                     };
                 }
 
                 return null;
             }
         };
                 
         AdapterFactoryRegistry.getInstance()
             .registerFactory(m_registeredAdapterFactory);
         
         assertRenderedTextEquals(canvasText, new Canvas());
 
     }
 
     @Test
     @SuppressWarnings("rawtypes")
     public void testNullAdaptedRenderer() {
         m_registeredAdapterFactory = new IAdapterFactory() {
 
             public Class[] getSupportedClasses() {
                 return new Class[] {Canvas.class};
             }
             
             public Object getAdapter(Class targetAdapterClass, Object objectToAdapt) {
                 if (objectToAdapt instanceof Canvas) {
                     return new ITextRendererAdapter() {
                         public String getText() {
                             return null;
                         }
                     };
                 }
 
                 return null;
             }
         };
         
         AdapterFactoryRegistry.getInstance()
             .registerFactory(m_registeredAdapterFactory);
         
         assertRenderedTextEquals("", new Canvas());
 
     }
 
     @Test
     public void testPublicGetText() {
         CanvasWithPublicGetText c = new CanvasWithPublicGetText();
         assertRenderedTextEquals(c.getText(), c);
     }
 
     @Test
     public void testPrivateGetText() {
         CanvasWithPrivateGetText c = new CanvasWithPrivateGetText();
         assertRenderedTextEquals(c.getText(), c);
     }
 
     @Test
     public void testOverriddenGetText() {
         CanvasWithOverriddenGetText c = new CanvasWithOverriddenGetText();
         assertRenderedTextEquals(c.getText(), c);
     }
 
     @Test
     public void testNullGetText() {
         CanvasWithNullGetText c = new CanvasWithNullGetText();
         assertRenderedTextEquals("", c);
     }
 
     @Test
     public void testInvalidGetText() {
         CanvasWithInvalidGetText c = new CanvasWithInvalidGetText();
         assertRendererNotSupported(c);
     }
     
     @Test
     public void testGetTextExceptionHandling() {
         CanvasWithExceptionGetText c = new CanvasWithExceptionGetText();
         try {
             TesterUtil.getRenderedText(c);
             fail();
         } catch (StepExecutionException e) {
             // An error occurring while getting the text does not mean that the
             // renderer is not supported.
            assertFalse(MessageIDs.E_EVENT_SUPPORT.equals(e.getErrorId()));
         }
     }
 }
