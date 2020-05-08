 /*
     This file is part of The Simplicity Engine.
 
     The Simplicity Engine is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published
     by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 
     The Simplicity Engine is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License along with The Simplicity Engine. If not, see <http://www.gnu.org/licenses/>.
  */
 package com.se.simplicity.jogl.test.viewport;
 
 import static org.easymock.classextension.EasyMock.createMock;
 import static org.easymock.classextension.EasyMock.replay;
 import static org.easymock.classextension.EasyMock.reset;
 import static org.easymock.classextension.EasyMock.verify;
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.se.simplicity.jogl.rendering.engine.SimpleJOGLRenderingEngine;
 import com.se.simplicity.jogl.test.mocks.MockGL;
 import com.se.simplicity.jogl.viewport.SimpleJOGLViewport;
 
 /**
  * <p>
  * Unit tests for the class {@link com.se.simplicity.jogl.viewport.SimpleJOGLViewport SimpleJOGLViewport}.
  * </p>
  * 
  * @author Gary Buyn
  */
 public class SimpleJOGLViewportTest
 {
     /**
      * An instance of the class being unit tested.
      */
     private SimpleJOGLViewport testObject;
 
     /**
      * <p>
      * Setup to perform before each unit test.
      * </p>
      */
     @Before
     public final void before()
     {
         testObject = new SimpleJOGLViewport();
     }
 
     /**
      * <p>
      * Unit test the method {@link com.se.simplicity.jogl.viewport.SimpleJOGLViewport.displaySceneGraph displaySceneGraph()}.
      * </p>
      */
     @Test
     public void displayScene()
     {
         SimpleJOGLRenderingEngine mockRenderingEngine = createMock(SimpleJOGLRenderingEngine.class);
 
         testObject.setRenderingEngine(mockRenderingEngine);
 
         reset(mockRenderingEngine);
         mockRenderingEngine.advance();
         replay(mockRenderingEngine);
 
         testObject.displayScene();
 
         verify(mockRenderingEngine);
     }
 
     /**
      * <p>
      * Unit test the method {@link com.se.simplicity.jogl.viewport.SimpleJOGLViewport.displaySceneGraph displaySceneGraph()} with the special
      * condition that the {@link com.se.simplicity.jogl.viewport.SimpleJOGLViewport SimpleJOGLViewport} being tested does not have a
      * <code>RenderingEngine</code>.
      * </p>
      */
     @Test(expected = IllegalStateException.class)
     public void displaySceneNoRenderingEngine()
     {
         testObject.displayScene();
     }
 
     /**
      * <p>
      * Unit test the method {@link com.se.simplicity.jogl.viewport.SimpleJOGLViewport.setGL setGL()}.
      * </p>
      */
     @Test
     public void setGL()
     {
         MockGL mockGl = new MockGL();
         SimpleJOGLRenderingEngine mockRenderingEngine = createMock(SimpleJOGLRenderingEngine.class);
 
         testObject.setRenderingEngine(mockRenderingEngine);
 
         reset(mockRenderingEngine);
         mockRenderingEngine.setGL(mockGl);
         replay(mockRenderingEngine);
 
         testObject.setGL(mockGl);
 
         verify(mockRenderingEngine);
     }
 
     /**
      * <p>
      * Unit test the method {@link com.se.simplicity.jogl.viewport.SimpleJOGLViewport.setRenderingEngine setRenderingEngine()}.
      * </p>
      */
     @Test
     public void setRenderingEngine()
     {
         MockGL mockGl = new MockGL();
         SimpleJOGLRenderingEngine mockRenderingEngine = createMock(SimpleJOGLRenderingEngine.class);
 
         testObject.setGL(mockGl);
 
         reset(mockRenderingEngine);
         mockRenderingEngine.setGL(mockGl);
         replay(mockRenderingEngine);
 
         testObject.setRenderingEngine(mockRenderingEngine);
 
         verify(mockRenderingEngine);
     }
 
     /**
      * <p>
      * Unit test the method {@link com.se.simplicity.jogl.viewport.SimpleJOGLViewport.setSize setSize()}.
      * </p>
      */
     @Test
     public void setSize()
     {
         MockGL mockGl = new MockGL();
 
         testObject.setGL(mockGl);
 
         mockGl.reset();
 
         testObject.setSize(200, 200);
 
         assertEquals(1, mockGl.getMethodCallCount("glViewport", new Object[] {0, 0, 200, 200}), 0);
     }
 }
