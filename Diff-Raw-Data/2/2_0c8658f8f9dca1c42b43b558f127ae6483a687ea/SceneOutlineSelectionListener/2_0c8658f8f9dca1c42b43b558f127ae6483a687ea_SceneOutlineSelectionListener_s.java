 /*
     This file is part of The Simplicity Engine.
 
     The Simplicity Engine is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published
     by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 
     The Simplicity Engine is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License along with The Simplicity Engine. If not, see <http://www.gnu.org/licenses/>.
  */
 package com.se.simplicity.editor.ui.views;
 
 import java.util.Map;
 
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.widgets.TreeItem;
 
 import com.se.simplicity.editor.internal.SceneManager;
 import com.se.simplicity.rendering.Camera;
 import com.se.simplicity.rendering.Light;
 import com.se.simplicity.scenegraph.Node;
 
 /**
  * <p>
  * Listens for selection events on a tree outline of the contents of the <code>Scene</code> displayed in the active editor (if there is one) and
  * notifies the {@link com.se.simplicity.editor.internal.SceneManager SceneManager} of the change in active <code>Node</code>.
  * </p>
  * 
  * @author Gary Buyn
  */
 public class SceneOutlineSelectionListener implements SelectionListener
 {
     /**
      * <p>
      * Determines if this <code>SceneOutlineSelectionListener</code> should respond to events.
      * </p>
      */
     private boolean fEnabled;
 
     /**
      * <p>
      * A map from <code>TreeItem</code> to <code>Scene</code> components. Used for notifications of changes in active <code>Node</code>.
      * </p>
      */
     private Map<TreeItem, Object> fSceneComponents;
 
     /**
      * <p>
      * Creates an instance of <code>SceneOutlineSelectionListener</code>.
      * </p>
      * 
      * @param sceneComponents A map from <code>TreeItem</code> to <code>Scene</code> components.
      */
     public SceneOutlineSelectionListener(final Map<TreeItem, Object> sceneComponents)
     {
         fSceneComponents = sceneComponents;
 
        fEnabled = false;
     }
 
     /**
      * <p>
      * Stops this <code>SceneOutlineSelectionListener</code> from responding to events.
      * </p>
      */
     public void disable()
     {
         fEnabled = false;
     }
 
     /**
      * <p>
      * Ensures this <code>SceneOutlineSelectionListener</code> is responding to events.
      * </p>
      */
     public void enable()
     {
         fEnabled = true;
     }
 
     /**
      * <p>
      * Determines whether this <code>SceneOutlineSelectionListener</code> is responding to events.
      * </p>
      * 
      * @return True if this <code>SceneOutlineSelectionListener</code> is responding to events, false otherwise.
      */
     public boolean isEnabled()
     {
         return (fEnabled);
     }
 
     @Override
     public void widgetDefaultSelected(final SelectionEvent e)
     {
         notify(e);
     }
 
     @Override
     public void widgetSelected(final SelectionEvent e)
     {
         notify(e);
     }
 
     /**
      * <p>
      * Responds to a <code>SelectionEvent</code> by notifying the {@link com.se.simplicity.editor.internal.SceneManager SceneManager} of the change in
      * active <code>Node</code>.
      * </p>
      * 
      * @param e The <code>SelectionEvent</code> to respond to.
      */
     protected void notify(final SelectionEvent e)
     {
         if (!fEnabled)
         {
             return;
         }
 
         if (e.item instanceof TreeItem)
         {
             Object sceneComponent = fSceneComponents.get((TreeItem) e.item);
 
             if (sceneComponent instanceof Camera)
             {
                 SceneManager.getSceneManager().setActiveCamera((Camera) sceneComponent);
             }
             else if (sceneComponent instanceof Light)
             {
                 SceneManager.getSceneManager().setActiveLight((Light) sceneComponent);
             }
             else if (sceneComponent instanceof Node)
             {
                 SceneManager.getSceneManager().setActiveNode((Node) sceneComponent);
             }
         }
     }
 
 }
