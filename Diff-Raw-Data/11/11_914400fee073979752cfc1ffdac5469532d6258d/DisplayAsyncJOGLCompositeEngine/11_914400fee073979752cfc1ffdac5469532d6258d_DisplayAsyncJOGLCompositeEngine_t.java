 /*
     This file is part of The Simplicity Engine.
 
     The Simplicity Engine is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published
     by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 
     The Simplicity Engine is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License along with The Simplicity Engine. If not, see <http://www.gnu.org/licenses/>.
  */
 package com.se.simplicity.editor.internal.engine;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.media.opengl.GLContext;
 
import org.apache.log4j.Logger;
 import org.eclipse.swt.opengl.GLCanvas;
 import org.eclipse.swt.widgets.Display;
 
 import com.se.simplicity.SENotSupportedException;
 import com.se.simplicity.engine.CompositeEngine;
 import com.se.simplicity.engine.Engine;
 
 /**
  * <p>
  * A {@link com.se.simplicity.engine.CompositeEngine CompositeEngine} whose sub-engines are advanced asynchronously by SWT.
  * </p>
  * 
  * @author Gary Buyn
  */
 public class DisplayAsyncJOGLCompositeEngine implements CompositeEngine
 {
     /**
      * <p>
      * The {@link java.lang.Runnable Runnable} that is run asynchronously by SWT.
      * </p>
      * 
      * @author Gary Buyn
      */
     private class AsyncAdvancer implements Runnable
     {
         @Override
         public void run()
         {
             if (!fCanvas.isDisposed())
             {
                 advance();
 
                 // Run this asynchronously again.
                 fDisplay.asyncExec(this);
             }
         }
     }
 
     /**
      * <p>
      * The 3D canvas to render to.
      * </p>
      */
     private GLCanvas fCanvas;
 
     /**
      * <p>
      * The display to request synchronous calls against.
      * </p>
      */
     private Display fDisplay;
 
     /**
      * <p>
      * The sub-engines managed by this <code>DisplaySyncJOGLCompositeEngine</code>.
      * </p>
      */
     private List<Engine> fEngines;
 
     /**
      * <p>
      * The <code>GLContext</code> to use when rendering.
      * </p>
      */
     private GLContext fGlContext;
 
     /**
      * <p>
      * Creates an instance of <code>DisplaySyncJOGLCompositeEngine</code>.
      * </p>
      * 
      * @param canvas The 3D canvas to render to.
      * @param glContext The <code>GLContext</code> to use when rendering.
      */
     public DisplayAsyncJOGLCompositeEngine(final GLCanvas canvas, final GLContext glContext)
     {
         fCanvas = canvas;
         fDisplay = fCanvas.getDisplay();
         fGlContext = glContext;
         fEngines = new ArrayList<Engine>();
     }
 
     @Override
     public void addEngine(final Engine engine)
     {
         fEngines.add(engine);
     }
 
     @Override
     public void advance()
     {
         fCanvas.setCurrent();
         fGlContext.makeCurrent();
 
         for (Engine engine : fEngines)
         {
            try
            {
                engine.advance();
            }
            catch (Exception e)
            {
                Logger.getLogger(getClass()).error("Managed engine failed to advance.", e);
            }
         }
 
         fCanvas.swapBuffers();
         fGlContext.release();
     }
 
     @Override
     public void destroy()
     {
         for (Engine engine : fEngines)
         {
             engine.destroy();
         }
     }
 
     @Override
     public int getPreferredFrequency()
     {
         return (-1);
     }
 
     @Override
     public void init()
     {
         for (Engine engine : fEngines)
         {
             engine.init();
         }
     }
 
     @Override
     public void removeEngine(final Engine engine)
     {
         fEngines.remove(engine);
     }
 
     @Override
     public void reset()
     {
         for (Engine engine : fEngines)
         {
             engine.reset();
         }
     }
 
     @Override
     public void run()
     {
         init();
 
         fDisplay.asyncExec(new AsyncAdvancer());
 
         destroy();
     }
 
     @Override
     public void setPreferredFrequency(final int preferredFrequency)
     {
         throw new SENotSupportedException();
     };
 }
