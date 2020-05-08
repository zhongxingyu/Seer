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
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.se.simplicity.editor.internal.SceneChangedEvent;
 import com.se.simplicity.editor.internal.SceneChangedEventType;
 import com.se.simplicity.editor.internal.SceneManager;
 import com.se.simplicity.editor.ui.views.CameraView;
 import com.se.simplicity.rendering.Camera;
 import com.se.simplicity.rendering.ProjectionMode;
 import com.se.simplicity.scenegraph.Node;
 import com.se.simplicity.util.metadata.rendering.MetaDataCamera;
 
 /**
  * <p>
  * Unit tests for the class {@link com.se.simplicity.editor.ui.views.CameraView CameraView}.
  * </p>
  * 
  * @author Gary Buyn
  */
 public class CameraViewTest
 {
     /**
      * An instance of the class being unit tested.
      */
     private CameraView testObject;
 
     /**
      * <p>
      * Setup to perform before each unit test.
      * </p>
      */
     @Before
     public void before()
     {
         testObject = new CameraView(new Shell(), SWT.NONE);
 
         SceneManager.getSceneManager().reset();
     }
 
     /**
      * <p>
      * Unit test the constructor {@link com.se.simplicity.editor.ui.views.CameraView#CameraView(Composite, int) CameraView(Composite, int)}.
      * </p>
      */
     @Test
     public void cameraView()
     {
         testObject = new CameraView(new Shell(), SWT.NONE);
 
         assertTrue(SceneManager.getSceneManager().getSceneChangedListeners().contains(testObject));
     }
 
     /**
      * <p>
      * Unit test the method {@link com.se.simplicity.editor.ui.views.CameraView#dispose() dispose()}.
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
      * Unit test the method {@link com.se.simplicity.editor.ui.views.CameraView#sceneChanged(SceneChangedEvent) sceneChanged(SceneChangedEvent)} with
      * the special condition that the event is of type 'CAMERA_ACTIVATED'.
      * </p>
      */
     @Test
     public void sceneChangedCameraActivated()
     {
         // Create dependencies.
         SceneChangedEvent mockEvent = createMock(SceneChangedEvent.class);
         Camera mockCamera = createMock(Camera.class);
         Node mockNode = createMock(Node.class);
 
         // Dictate correct behaviour.
         expect(mockEvent.getSceneComponent()).andStubReturn(mockCamera);
         expect(mockEvent.getType()).andStubReturn(SceneChangedEventType.CAMERA_ACTIVATED);
         expect(mockCamera.getNode()).andStubReturn(mockNode);
         expect(mockCamera.getProjectionMode()).andStubReturn(ProjectionMode.PERSPECTIVE);
         expect(mockCamera.getFrameAspectRatio()).andStubReturn(0.75f);
         expect(mockCamera.getFrameX()).andStubReturn(0.0f);
         expect(mockCamera.getFrameY()).andStubReturn(0.0f);
         expect(mockCamera.getFrameWidth()).andStubReturn(0.1f);
         expect(mockCamera.getFrameHeight()).andStubReturn(0.075f);
         expect(mockCamera.getNearClippingDistance()).andStubReturn(0.1f);
         expect(mockCamera.getFarClippingDistance()).andStubReturn(1000.0f);
         expect(mockNode.getID()).andStubReturn(0);
         replay(mockEvent, mockCamera, mockNode);
 
         // Verify test environment.
         Control[] sections = testObject.getChildren();
 
         Control[] idWidgets = ((Composite) sections[0]).getChildren();
         assertEquals("", ((Text) idWidgets[1]).getText());
 
         Control[] generalWidgets = ((Composite) sections[1]).getChildren();
         assertEquals("", ((Text) generalWidgets[1]).getText());
         assertEquals("", ((Combo) generalWidgets[3]).getText());
 
         Control[] frameWidgets = ((Composite) sections[2]).getChildren();
         assertEquals("", ((Text) frameWidgets[1]).getText());
         assertEquals("", ((Text) frameWidgets[3]).getText());
         assertEquals("", ((Text) frameWidgets[5]).getText());
         assertEquals("", ((Text) frameWidgets[7]).getText());
         assertEquals("", ((Text) frameWidgets[9]).getText());
 
         Control[] clippingWidgets = ((Composite) sections[3]).getChildren();
         assertEquals("", ((Text) clippingWidgets[1]).getText());
         assertEquals("", ((Text) clippingWidgets[3]).getText());
 
         Control[] reflectionWidgets = ((Composite) sections[4]).getChildren();
         assertEquals("", ((Text) reflectionWidgets[1]).getText());
 
         // Perform test.
         testObject.sceneChanged(mockEvent);
 
         // Verify test.
         assertEquals("CameraX", ((Text) idWidgets[1]).getText());
 
         assertEquals("0", ((Text) generalWidgets[1]).getText());
         assertEquals("PERSPECTIVE", ((Combo) generalWidgets[3]).getText());
 
         assertEquals("0.75", ((Text) frameWidgets[1]).getText());
         assertEquals("0.0", ((Text) frameWidgets[3]).getText());
         assertEquals("0.0", ((Text) frameWidgets[5]).getText());
         assertEquals("0.1", ((Text) frameWidgets[7]).getText());
         assertEquals("0.075", ((Text) frameWidgets[9]).getText());
 
         assertEquals("0.1", ((Text) clippingWidgets[1]).getText());
         assertEquals("1000.0", ((Text) clippingWidgets[3]).getText());
 
        assertEquals("$Proxy8", ((Text) reflectionWidgets[1]).getText());
     }
 
     /**
      * <p>
      * Unit test the method {@link com.se.simplicity.editor.ui.views.CameraView#sceneChanged(SceneChangedEvent) sceneChanged(SceneChangedEvent)} with
      * the special condition that the event is of type 'CAMERA_ACTIVATED' and the <code>Camera</code> that was activated was a
      * <code>MetaDataCamera</code>.
      * </p>
      */
     @Test
     public void sceneChangedMetaDataCameraActivated()
     {
         // Create dependencies.
         SceneChangedEvent mockEvent = createMock(SceneChangedEvent.class);
         MetaDataCamera mockMetaDataCamera = createMock(MetaDataCamera.class);
         Camera mockCamera = createMock(Camera.class);
         Node mockNode = createMock(Node.class);
 
         // Dictate correct behaviour.
         expect(mockEvent.getSceneComponent()).andStubReturn(mockMetaDataCamera);
         expect(mockEvent.getType()).andStubReturn(SceneChangedEventType.CAMERA_ACTIVATED);
         expect(mockMetaDataCamera.getWrappedCamera()).andStubReturn(mockCamera);
         expect(mockMetaDataCamera.getAttribute("name")).andStubReturn("Test");
         expect(mockMetaDataCamera.getNode()).andStubReturn(mockNode);
         expect(mockMetaDataCamera.getProjectionMode()).andStubReturn(ProjectionMode.PERSPECTIVE);
         expect(mockMetaDataCamera.getFrameAspectRatio()).andStubReturn(0.75f);
         expect(mockMetaDataCamera.getFrameX()).andStubReturn(0.0f);
         expect(mockMetaDataCamera.getFrameY()).andStubReturn(0.0f);
         expect(mockMetaDataCamera.getFrameWidth()).andStubReturn(0.1f);
         expect(mockMetaDataCamera.getFrameHeight()).andStubReturn(0.075f);
         expect(mockMetaDataCamera.getNearClippingDistance()).andStubReturn(0.1f);
         expect(mockMetaDataCamera.getFarClippingDistance()).andStubReturn(1000.0f);
         expect(mockNode.getID()).andStubReturn(0);
         replay(mockEvent, mockMetaDataCamera, mockCamera, mockNode);
 
         // Verify test environment.
         Control[] sections = testObject.getChildren();
 
         Control[] idWidgets = ((Composite) sections[0]).getChildren();
         assertEquals("", ((Text) idWidgets[1]).getText());
 
         Control[] reflectionWidgets = ((Composite) sections[4]).getChildren();
         assertEquals("", ((Text) reflectionWidgets[1]).getText());
 
         // Perform test.
         testObject.sceneChanged(mockEvent);
 
         // Verify test.
         assertEquals("Test", ((Text) idWidgets[1]).getText());
 
        assertEquals("$Proxy8", ((Text) reflectionWidgets[1]).getText());
     }
 
     /**
      * <p>
      * Unit test the method {@link com.se.simplicity.editor.ui.views.CameraView#sceneChanged(SceneChangedEvent) sceneChanged(SceneChangedEvent)} with
      * the special condition that the scene component held in the event is null.
      * </p>
      */
     @Test
     public void sceneChangedNullSceneComponent()
     {
         // Create dependencies.
         SceneChangedEvent mockEvent = createMock(SceneChangedEvent.class);
 
         // Dictate correct behaviour.
         expect(mockEvent.getSceneComponent()).andStubReturn(null);
         replay(mockEvent);
 
         // Perform test.
         testObject.sceneChanged(mockEvent);
     }
 }
