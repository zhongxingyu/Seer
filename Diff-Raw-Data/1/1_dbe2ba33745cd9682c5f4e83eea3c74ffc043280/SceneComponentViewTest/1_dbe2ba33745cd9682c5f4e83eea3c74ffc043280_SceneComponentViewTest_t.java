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
 import static org.junit.Assert.assertTrue;
 
 import org.eclipse.swt.custom.StackLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Shell;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.se.simplicity.editor.internal.SceneChangedEvent;
 import com.se.simplicity.editor.internal.SceneChangedEventType;
 import com.se.simplicity.editor.internal.SceneManager;
 import com.se.simplicity.editor.ui.views.CameraView;
 import com.se.simplicity.editor.ui.views.LightView;
 import com.se.simplicity.editor.ui.views.NodeView;
 import com.se.simplicity.editor.ui.views.SceneComponentView;
 
 /**
  * <p>
  * Unit tests for the class {@link com.se.simplicity.editor.ui.views.SceneComponentView SceneComponentView}.
  * </p>
  * 
  * @author Gary Buyn
  */
 public class SceneComponentViewTest
 {
     /**
      * An instance of the class being unit tested.
      */
     private SceneComponentView testObject;
 
     /**
      * <p>
      * Setup to perform before each unit test.
      * </p>
      */
     @Before
     public void before()
     {
         testObject = new SceneComponentView();
 
         SceneManager.getSceneManager().reset();
     }
 
     /**
      * <p>
      * Unit test the constructor {@link com.se.simplicity.editor.ui.views.SceneComponentView#SceneComponentView() SceneComponentView()}.
      * </p>
      */
     @Test
     public void sceneComponentView()
     {
         testObject = new SceneComponentView();
 
         assertTrue(SceneManager.getSceneManager().getSceneChangedListeners().contains(testObject));
     }
 
     /**
      * <p>
      * Unit test the method {@link com.se.simplicity.editor.ui.views.SceneComponentView#dispose() dispose()}.
      * </p>
      */
     @Test
     public void dispose()
     {
         testObject.createPartControl(new Shell());
 
         testObject.dispose();
 
         assertTrue(!SceneManager.getSceneManager().getSceneChangedListeners().contains(testObject));
     }
 
     /**
      * <p>
      * Unit test the method {@link com.se.simplicity.editor.ui.views.SceneComponentView#sceneChanged(SceneChangedEvent)
      * sceneChanged(SceneChangedEvent)} with the special condition that the event is of type 'CAMERA_ACTIVATED'.
      * </p>
      */
     @Test
     public void sceneChangedCameraActivated()
     {
         // Create dependencies.
         SceneChangedEvent mockEvent = createMock(SceneChangedEvent.class);
 
         // Dictate correct behaviour.
         expect(mockEvent.getSceneComponent()).andStubReturn(new Object());
         expect(mockEvent.getType()).andStubReturn(SceneChangedEventType.CAMERA_ACTIVATED);
         replay(mockEvent);
 
         // Initialise test environment.
         testObject.createPartControl(new Shell());
 
         // Perform test.
         testObject.sceneChanged(mockEvent);
 
         // Verify test.
         assertTrue(((StackLayout) testObject.getParent().getLayout()).topControl instanceof CameraView);
     }
 
     /**
      * <p>
      * Unit test the method {@link com.se.simplicity.editor.ui.views.SceneComponentView#sceneChanged(SceneChangedEvent)
      * sceneChanged(SceneChangedEvent)} with the special condition that the event is of type 'LIGHT_ACTIVATED'.
      * </p>
      */
     @Test
     public void sceneChangedLightActivated()
     {
         // Create dependencies.
         SceneChangedEvent mockEvent = createMock(SceneChangedEvent.class);
 
         // Dictate correct behaviour.
         expect(mockEvent.getSceneComponent()).andStubReturn(new Object());
         expect(mockEvent.getType()).andStubReturn(SceneChangedEventType.LIGHT_ACTIVATED);
         replay(mockEvent);
 
         // Initialise test environment.
         testObject.createPartControl(new Shell());
 
         // Perform test.
         testObject.sceneChanged(mockEvent);
 
         // Verify test.
         assertTrue(((StackLayout) testObject.getParent().getLayout()).topControl instanceof Composite);
     }
 
     /**
      * <p>
      * Unit test the method {@link com.se.simplicity.editor.ui.views.SceneComponentView#sceneChanged(SceneChangedEvent)
      * sceneChanged(SceneChangedEvent)} with the special condition that the event is of type 'NODE_ACTIVATED'.
      * </p>
      */
     @Test
     public void sceneChangedNodeActivated()
     {
         // Create dependencies.
         SceneChangedEvent mockEvent = createMock(SceneChangedEvent.class);
 
         // Dictate correct behaviour.
         expect(mockEvent.getSceneComponent()).andStubReturn(new Object());
         expect(mockEvent.getType()).andStubReturn(SceneChangedEventType.NODE_ACTIVATED);
         replay(mockEvent);
 
         // Initialise test environment.
         testObject.createPartControl(new Shell());
 
         // Perform test.
         testObject.sceneChanged(mockEvent);
 
         // Verify test.
         assertTrue(((StackLayout) testObject.getParent().getLayout()).topControl instanceof NodeView);
     }
 
     /**
      * <p>
      * Unit test the method {@link com.se.simplicity.editor.ui.views.SceneComponentView#sceneChanged(SceneChangedEvent)
      * sceneChanged(SceneChangedEvent)} with the special condition that the scene component held in the event is null.
      * </p>
      */
     @Test
     public void sceneChangedNullSceneComponent()
     {
         // Create dependencies.
         SceneChangedEvent mockEvent = createMock(SceneChangedEvent.class);
 
         // Dictate correct behaviour.
         expect(mockEvent.getSceneComponent()).andStubReturn(null);
        expect(mockEvent.getType()).andStubReturn(SceneChangedEventType.CAMERA_ACTIVATED);
         replay(mockEvent);
 
         // Initialise test environment.
         testObject.createPartControl(new Shell());
 
         // Perform test.
         testObject.sceneChanged(mockEvent);
 
         // Verify test results.
         assertTrue(!(((StackLayout) testObject.getParent().getLayout()).topControl instanceof CameraView));
         assertTrue(!(((StackLayout) testObject.getParent().getLayout()).topControl instanceof LightView));
         assertTrue(!(((StackLayout) testObject.getParent().getLayout()).topControl instanceof NodeView));
     }
 }
