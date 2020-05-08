 /*
     This file is part of The Simplicity Engine.
 
     The Simplicity Engine is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published
     by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 
     The Simplicity Engine is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License along with The Simplicity Engine. If not, see <http://www.gnu.org/licenses/>.
  */
 package com.se.simplicity.editor.test.ui.views;
 
 import static org.easymock.EasyMock.expect;
 import static org.easymock.classextension.EasyMock.createMock;
 import static org.easymock.classextension.EasyMock.replay;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.se.simplicity.editor.internal.SceneChangedEvent;
 import com.se.simplicity.editor.internal.SceneChangedEventType;
 import com.se.simplicity.editor.internal.SceneManager;
 import com.se.simplicity.editor.ui.views.NodeView;
 import com.se.simplicity.scenegraph.Node;
 import com.se.simplicity.vector.TransformationMatrixf;
 import com.se.simplicity.vector.TranslationVectorf;
 
 /**
  * <p>
  * Unit tests for the class {@link com.se.simplicity.editor.ui.views.NodeView NodeView}.
  * </p>
  * 
  * @author Gary Buyn
  */
 public class NodeViewTest
 {
     /**
      * An instance of the class being unit tested.
      */
     private NodeView testObject;
 
     /**
      * <p>
      * Setup to perform before each unit test.
      * </p>
      */
     @Before
     public void before()
     {
         testObject = new NodeView(new Shell(), SWT.NONE);
 
         SceneManager.getSceneManager().reset();
     }
 
     /**
      * <p>
      * Unit test the constructor {@link com.se.simplicity.editor.ui.views.NodeView#NodeView(Composite, int) NodeView(Composite, int)}.
      * </p>
      */
     @Test
     public void nodeView()
     {
         testObject = new NodeView(new Shell(), SWT.NONE);
 
         assertTrue(SceneManager.getSceneManager().getSceneChangedListeners().contains(testObject));
     }
 
     /**
      * <p>
      * Unit test the method {@link com.se.simplicity.editor.ui.views.NodeView#dispose() dispose()}.
      * </p>
      */
     @Test
     public void dispose()
     {
         testObject.dispose();
 
         assertTrue(!SceneManager.getSceneManager().getSceneChangedListeners().contains(testObject));
     }
 
     /**
      * <p>
      * Unit test the method {@link com.se.simplicity.editor.ui.views.NodeView#sceneChanged(SceneChangedEvent) sceneChanged(SceneChangedEvent)} with
      * the special condition that the event is of type 'NODE_ACTIVATED'.
      * </p>
      */
     @Test
     public void sceneChangedNodeActivated()
     {
         // Create dependencies.
         SceneChangedEvent mockEvent = createMock(SceneChangedEvent.class);
         Node mockNode = createMock(Node.class);
 
         TransformationMatrixf mockTransformation = createMock(TransformationMatrixf.class);
         TranslationVectorf mockTranslation = createMock(TranslationVectorf.class);
 
         // Dictate correct behaviour.
         expect(mockEvent.getSceneComponent()).andStubReturn(mockNode);
         expect(mockEvent.getType()).andStubReturn(SceneChangedEventType.NODE_ACTIVATED);
         expect(mockNode.getID()).andStubReturn(0);
         expect(mockNode.isCollidable()).andStubReturn(true);
         expect(mockNode.isModifiable()).andStubReturn(true);
         expect(mockNode.isVisible()).andStubReturn(true);
         expect(mockNode.getTransformation()).andStubReturn(mockTransformation);
         expect(mockTransformation.getXAxisRotation()).andStubReturn(90.0f * (float) Math.PI / 180.0f);
         expect(mockTransformation.getYAxisRotation()).andStubReturn(180.0f * (float) Math.PI / 180.0f);
         expect(mockTransformation.getZAxisRotation()).andStubReturn(270.0f * (float) Math.PI / 180.0f);
         expect(mockTransformation.getTranslation()).andStubReturn(mockTranslation);
         expect(mockTranslation.getX()).andStubReturn(5.0f);
         expect(mockTranslation.getY()).andStubReturn(10.0f);
         expect(mockTranslation.getZ()).andStubReturn(15.0f);
         replay(mockEvent, mockNode, mockTransformation, mockTranslation);
 
         // Verify test environment.
         Control[] sections = testObject.getChildren();
 
         Control[] idWidgets = ((Composite) sections[0]).getChildren();
         assertEquals("", ((Text) idWidgets[1]).getText());
         assertEquals("", ((Text) idWidgets[3]).getText());
 
         Control[] propertyWidgets = ((Composite) sections[1]).getChildren();
         assertEquals(false, ((Button) propertyWidgets[0]).getSelection());
         assertEquals(false, ((Button) propertyWidgets[1]).getSelection());
         assertEquals(false, ((Button) propertyWidgets[2]).getSelection());
 
         Control[] translationWidgets = ((Composite) sections[2]).getChildren();
         assertEquals("", ((Text) translationWidgets[1]).getText());
         assertEquals("", ((Text) translationWidgets[3]).getText());
         assertEquals("", ((Text) translationWidgets[5]).getText());
 
         Control[] rotationWidgets = ((Composite) sections[3]).getChildren();
         assertEquals("", ((Text) rotationWidgets[1]).getText());
         assertEquals("", ((Text) rotationWidgets[3]).getText());
         assertEquals("", ((Text) rotationWidgets[5]).getText());
 
         Control[] reflectiveWidgets = ((Composite) sections[4]).getChildren();
         assertEquals("", ((Text) reflectiveWidgets[1]).getText());
 
         // Perform test.
         testObject.sceneChanged(mockEvent);
 
         // Verify test.
         assertEquals("0", ((Text) idWidgets[1]).getText());
         assertEquals("Node0", ((Text) idWidgets[3]).getText());
 
         assertEquals(true, ((Button) propertyWidgets[0]).getSelection());
         assertEquals(true, ((Button) propertyWidgets[1]).getSelection());
         assertEquals(true, ((Button) propertyWidgets[2]).getSelection());
 
         assertEquals("5.0", ((Text) translationWidgets[1]).getText());
         assertEquals("10.0", ((Text) translationWidgets[3]).getText());
         assertEquals("15.0", ((Text) translationWidgets[5]).getText());
 
         assertEquals("90.0", ((Text) rotationWidgets[1]).getText());
         assertEquals("180.0", ((Text) rotationWidgets[3]).getText());
         assertEquals("270.0", ((Text) rotationWidgets[5]).getText());
 
        assertEquals("$Proxy5", ((Text) reflectiveWidgets[1]).getText());
     }
 }
