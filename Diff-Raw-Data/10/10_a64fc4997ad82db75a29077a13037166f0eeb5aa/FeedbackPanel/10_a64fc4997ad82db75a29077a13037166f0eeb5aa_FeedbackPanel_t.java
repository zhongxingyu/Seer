 /*
  * Copyright 2011 Raffael Herzog
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package ch.raffael.util.swing.components.feedback;
 
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Insets;
 import java.awt.LayoutManager2;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.awt.event.ContainerAdapter;
 import java.awt.event.ContainerEvent;
 import java.awt.event.HierarchyBoundsListener;
 import java.awt.event.HierarchyEvent;
 import java.awt.event.HierarchyListener;
 import java.util.IdentityHashMap;
 import java.util.Map;
 
 import javax.swing.JLayeredPane;
 import javax.swing.SwingUtilities;
 
 import org.jetbrains.annotations.NotNull;
 
 import ch.raffael.util.swing.SwingUtil;
 
 
 /**
  * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
  */
 public class FeedbackPanel extends JLayeredPane {
 
     private Component content;
     private final Map<Component, Tracker> trackers = new IdentityHashMap<Component, Tracker>();
     private Placement defaultPlacement = Placement.BOTTOM_RIGHT;
 
     public FeedbackPanel() {
         setLayout(new ContentLayoutManager());
         addContainerListener(new ContainerAdapter() {
             @Override
             public void componentRemoved(ContainerEvent e) {
                 if ( e.getChild() instanceof Feedback ) {
                     Tracker tracker = trackers.remove(e.getChild());
                     if ( tracker != null ) {
                         tracker.destroy();
                     }
                 }
             }
         });
         //setDoubleBuffered(true);
     }
 
     public FeedbackPanel(Component content) {
         this();
         setContent(content);
     }
 
 
     public Placement getDefaultPlacement() {
         return defaultPlacement;
     }
 
     public void setDefaultPlacement(Placement defaultPlacement) {
         this.defaultPlacement = defaultPlacement;
     }
 
     @Override
     protected void addImpl(Component comp, Object constraints, int index) {
         if ( comp == content ) {
             super.addImpl(comp, constraints, 0);
             return;
         }
         if ( constraints == null || !(constraints instanceof Component) ) {
             throw new IllegalArgumentException("Constraints must be a component");
         }
         if ( !checkFeedbackPanel((Component)constraints) ) {
             throw new IllegalArgumentException("Component " + constraints + " is not an ancestor of " + this);
         }
         if ( !(comp instanceof Feedback) ) {
             throw new IllegalArgumentException("Component must implement " + Feedback.class);
         }
         super.addImpl(comp, POPUP_LAYER, -1);
         Tracker tracker = new Tracker((Component)constraints, comp);
         trackers.put(comp, tracker);
         tracker.reposition();
     }
 
     @Override
     public Component add(Component comp) {
         addImpl(comp, this, 0);
         return comp;
     }
 
     @Override
     public Component add(Component comp, int index) {
         addImpl(comp, this, index);
         return comp;
     }
 
     public Component getContent() {
         return content;
     }
 
     public void setContent(Component content) {
         if ( this.content != null ) {
             remove(this.content);
         }
         this.content = content;
         super.addImpl(content, FRAME_CONTENT_LAYER, -1);
     }
 
     public void repositionFeedbacks() {
         for ( Tracker tracker : trackers.values() ) {
             tracker.reposition();
         }
     }
 
     private boolean checkFeedbackPanel(Component component) {
         Container feedbackPanel = SwingUtilities.getAncestorOfClass(FeedbackPanel.class, component);
         while ( feedbackPanel != null && feedbackPanel != this ) {
             feedbackPanel = SwingUtilities.getAncestorOfClass(FeedbackPanel.class, feedbackPanel);
         }
         return feedbackPanel == this;
     }
 
     private class ContentLayoutManager implements LayoutManager2 {
 
         @Override
         public Dimension maximumLayoutSize(Container parent) {
             Insets insets = parent.getInsets();
             Dimension max;
             if ( content != null ) {
                 max = content.getMaximumSize();
             }
             else {
                 max = new Dimension();
             }
             max.width += insets.left + insets.right;
             max.height += insets.top + insets.bottom;
             return max;
         }
 
         @Override
         public Dimension preferredLayoutSize(Container parent) {
             Insets insets = parent.getInsets();
             Dimension pref;
             if ( content != null ) {
                 pref = content.getPreferredSize();
             }
             else {
                 pref = new Dimension();
             }
             pref.width += insets.left + insets.right;
             pref.height += insets.top + insets.bottom;
             return pref;
         }
 
         @Override
         public Dimension minimumLayoutSize(Container parent) {
             Insets insets = parent.getInsets();
             Dimension min;
             if ( content != null ) {
                 min = content.getMaximumSize();
             }
             else {
                 min = new Dimension();
             }
             min.width += insets.left + insets.right;
             min.height += insets.top + insets.bottom;
             return min;
         }
 
         @Override
         public void layoutContainer(Container parent) {
             Insets insets = parent.getInsets();
            Rectangle bounds = new Rectangle();
            bounds.x = insets.left;
            bounds.y = insets.top;
            bounds.width = parent.getWidth() - insets.left + insets.right;
            bounds.height = parent.getHeight() - insets.top + insets.bottom;
             content.setBounds(bounds);
         }
 
         @Override
         public void addLayoutComponent(Component comp, Object constraints) {}
         @Override
         public void removeLayoutComponent(Component comp) {}
         @Override
         public float getLayoutAlignmentX(Container target) {return 0f;}
         @Override
         public float getLayoutAlignmentY(Container target) {return 0f;}
         @Override
         public void invalidateLayout(Container target) {}
         @Override
         public void addLayoutComponent(String name, Component comp) {}
     }
 
     private class Tracker implements HierarchyListener, HierarchyBoundsListener, ComponentListener {
 
         private final Component component;
         private final Component feedbackComponent;
         private Rectangle prevBounds = null;
         private Rectangle currentBounds = new Rectangle();
         private Point location = null;
 
         private Tracker(Component component, Component feedbackComponent) {
             this.component = component;
             this.feedbackComponent = feedbackComponent;
             component.addHierarchyListener(this);
             component.addHierarchyBoundsListener(this);
             component.addComponentListener(this);
             ((Feedback)feedbackComponent).attach(component);
             updateVisibility();
         }
 
         private void destroy() {
             ((Feedback)feedbackComponent).detach(component);
             component.removeHierarchyListener(this);
             component.removeHierarchyBoundsListener(this);
             component.removeComponentListener(this);
         }
 
         private void updateVisibility() {
             boolean visible = SwingUtil.isVisible(component);
             feedbackComponent.setVisible(visible);
         }
 
         private void reposition() {
             Point pos = SwingUtilities.convertPoint(component, 0, 0, FeedbackPanel.this);
             Dimension prefSize = feedbackComponent.getPreferredSize();
             Feedback feedback = (Feedback)feedbackComponent;
             Placement placement = feedback.getPlacement();
             if ( placement == null ) {
                 placement = defaultPlacement;
             }
             feedback.prepare(component, placement);
             currentBounds.x = pos.x + placement.getXOffset(component.getWidth(), prefSize.width);
             currentBounds.y = pos.y + placement.getYOffset(component.getHeight(), prefSize.height);
             if ( location == null ) {
                 location = currentBounds.getLocation();
             }
             else {
                 location.x = currentBounds.x;
                 location.y = currentBounds.y;
             }
             location = feedback.translate(component, location, placement);
             currentBounds.setLocation(location);
             currentBounds.width = prefSize.width;
             currentBounds.height = prefSize.height;
             if ( !currentBounds.equals(prevBounds) ) {
                 if ( prevBounds == null ) {
                     prevBounds = new Rectangle(currentBounds);
                 }
                 else {
                     prevBounds.setBounds(currentBounds);
                 }
                 feedbackComponent.setBounds(currentBounds);
             }
             }
 
         @Override
         public void hierarchyChanged(HierarchyEvent e) {
             if ( (e.getChangeFlags() & HierarchyEvent.PARENT_CHANGED) != 0 ) {
                 if ( !checkFeedbackPanel(component) ) {
                     remove(feedbackComponent);
                 }
             }
             if ( (e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 ) {
                 updateVisibility();
             }
         }
 
         @Override
         public void ancestorMoved(HierarchyEvent e) {
             reposition();
         }
 
         @Override
         public void ancestorResized(HierarchyEvent e) {
             reposition();
         }
 
         @Override
         public void componentResized(ComponentEvent e) {
             reposition();
         }
 
         @Override
         public void componentMoved(ComponentEvent e) {
             reposition();
         }
 
         @Override
         public void componentShown(ComponentEvent e) {
         }
 
         @Override
         public void componentHidden(ComponentEvent e) {
         }
     }
 
     public static enum Placement {
         TOP_LEFT(true, true) {
             @Override
             public int getXOffset(int componentWidth, int feedbackWidth) {
                 return 0;
             }
 
             @Override
             public int getYOffset(int componentHeight, int feedbackHeight) {
                 return -feedbackHeight;
             }
         },
         TOP_RIGHT(false, true) {
             @Override
             protected int getXOffset(int componentWidth, int feedbackWidth) {
                 return componentWidth - feedbackWidth;
             }
 
             @Override
             protected int getYOffset(int componentHeight, int feedbackHeight) {
                 return -feedbackHeight;
             }
         },
         BOTTOM_LEFT(true, false) {
             @Override
             protected int getXOffset(int componentWidth, int feedbackWidth) {
                 return 0;
             }
 
             @Override
             protected int getYOffset(int componentHeight, int feedbackHeight) {
                 return componentHeight;
             }
         },
         BOTTOM_RIGHT(false, false) {
             @Override
             protected int getXOffset(int componentWidth, int feedbackWidth) {
                 return componentWidth - feedbackWidth;
             }
 
             @Override
             protected int getYOffset(int componentHeight, int feedbackHeight) {
                 return componentHeight;
             }
         },;
 
         private final boolean isTop;
         private final boolean isLeft;
 
         Placement(boolean left, boolean top) {
             isLeft = left;
             isTop = top;
         }
 
         public boolean isLeft() {
             return isLeft;
         }
 
         public boolean isTop() {
             return isTop;
         }
 
         protected abstract int getXOffset(int componentWidth, int feedbackWidth);
 
         protected abstract int getYOffset(int componentHeight, int feedbackHeight);
 
         public void translate(@NotNull Point point, int dx, int dy) {
             point.x = translateX(point.x, dx);
             point.y = translateY(point.y, dy);
         }
         
         public int translateX(int x, int delta) {
             if ( isLeft() ) {
                 return x + delta;
             }
             else {
                 return x - delta;
             }
         }
         public int translateY(int y, int delta) {
             if ( isTop() ) {
                 return y + delta;
             }
             else {
                 return y - delta;
             }
         }
 
     }
     
 }
