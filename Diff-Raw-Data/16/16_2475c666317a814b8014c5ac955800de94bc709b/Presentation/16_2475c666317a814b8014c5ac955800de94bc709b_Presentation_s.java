 /*
  * Copyright 2011 Vaadin Ltd.
  * 
  * Licensed under the GNU Affero General Public License, Version 2 (the 
  * "License"); you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at
  * 
  * http://www.gnu.org/licenses/agpl.html
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package org.vaadin.jefferson;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.security.AccessController;
 import java.security.PrivilegedAction;
 
 import com.vaadin.ui.AbstractOrderedLayout;
 import com.vaadin.ui.Component;
 
 /**
  * A mechanism for rendering and styling a content tree, composed of
  * {@link Composite}s and descendant {@link View}s.
  * <p>
  * To define your own renditions and styles for specific view classes, inherit
  * from this class and create methods with the following signatures, replacing
  * <code>T</code> with the type of view you want to render or style:
  * 
  * <pre>
  *      Component render(T view)
  *      void style(T view)
  * </pre>
  * 
  * @author Marlon Richert @ Vaadin
  */
 public class Presentation {
     private static final String RENDER = "render";
     private static final String STYLE = "style";
     private static final String INVALID_CSS = "[^-_a-zA-Z]";
     private static final String WHITESPACE = "\\s+";
 
     /**
      * Renders the given view, calls {@link View#accept(Presentation)} and then
      * styles the view's rendition.
      * 
      * @param view
      *            The view to render and style.
      * @return The view's new rendition.
      */
     public <T extends Component> T visit(View<T> view) {
         call(RENDER, view);
         T rendition = view.getRendition();
 
         view.accept(this);
 
         call(STYLE, view);
         rendition.addStyleName(view.getName().replaceAll(
                 WHITESPACE, "-").replaceAll(INVALID_CSS, "").toLowerCase());
 
         return rendition;
     }
 
     /**
      * Sets the given view's {@link View#createFallback()} method and sets its
      * rendition to the result.
      */
     protected <T extends Component> void render(View<T> view) {
         setRendition(view, view.createFallback());
     }
 
     /**
      * Provides the given view's rendition with basic styling.
      */
     protected void style(View<?> view) {
         getRendition(view).setSizeUndefined();
     }
 
     protected <T extends Component> void setRendition(View<T> view, T rendition) {
         view.setRendition(rendition);
     }
 
     protected <T extends Component> T getRendition(View<T> view) {
        return view.getRendition();
     }
 
     /**
      * Convenience method for setting the rendition's expand ratio to 1.
      * 
      * @see AbstractOrderedLayout#setExpandRatio(Component, float)
      */
     protected static void expand(Component rendition) {
         Component parentRendition = rendition.getParent();
         if (parentRendition instanceof AbstractOrderedLayout) {
             ((AbstractOrderedLayout) parentRendition).setExpandRatio(
                     rendition, 1);
         }
     }
 
     private Object call(String name, View<?> view) {
         Method method = getMethod(name, view);
         try {
             return method.invoke(this, view);
         } catch (IllegalAccessException e) {
             throw new ExceptionInInitializerError(e);
         } catch (InvocationTargetException e) {
             throw new ExceptionInInitializerError(e);
         }
     }
 
     private Method getMethod(String name, View<?> view) {
         Class<?> presentationCls = getClass();
         while (presentationCls != null) {
             Class<?> viewCls = view.getClass();
             while (viewCls != null) {
                 try {
                     Method method = presentationCls.getDeclaredMethod(
                             name, viewCls);
                     return AccessController.doPrivileged(
                             new AccessibleMethod(method));
                 } catch (NoSuchMethodException e) {
                     viewCls = viewCls.getSuperclass();
                 }
             }
             presentationCls = presentationCls.getSuperclass();
         }
         throw new ExceptionInInitializerError("Can't find any method "
                 + getClass() + "." + name + "(" + view.getClass() + ").");
     }
 
     private final static class AccessibleMethod implements
             PrivilegedAction<Method> {
         private final Method method;
 
         private AccessibleMethod(Method method) {
             this.method = method;
         }
 
         public Method run() {
             method.setAccessible(true);
             return method;
         }
     }
 }
