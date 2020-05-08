 /*
  * Copyright 2000,2005 wingS development team.
  *
  * This file is part of wingS (http://wingsframework.org).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 package org.wings;
 
 import java.awt.Adjustable;
 import java.awt.LayoutManager;
 import java.awt.Rectangle;
 
 import javax.swing.BoundedRangeModel;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.EventListenerList;
 
 import org.wings.event.SViewportChangeEvent;
 import org.wings.event.SViewportChangeListener;
 import org.wings.plaf.ScrollPaneCG;
 
 /**
  * A pane which allows to add {@link Scrollable} components on this pane
  * and display only a viewport of it. Can be used to scroll graphically
  * (default) or pages (using {@link SPageScroller} components.).
  *
  * @author <a href="mailto:haaf@mercatis.de">Armin Haaf</a>
  */
 public class SScrollPane
         extends SContainer
         implements javax.swing.ScrollPaneConstants {
 
     public static final int MODE_SCROLLING = 0;
     public static final int MODE_COMPLETE = 1;
     public static final int MODE_PAGING = 2;
 
     /**
      * The element which should be scrolled.
      */
     protected Scrollable scrollable;
 
     protected Adjustable verticalScrollBar = null;
 
     protected Adjustable horizontalScrollBar = null;
 
     protected int horizontalScrollBarPolicy = HORIZONTAL_SCROLLBAR_AS_NEEDED;
 
     protected int verticalScrollBarPolicy = VERTICAL_SCROLLBAR_AS_NEEDED;
 
     protected int horizontalExtent = 10;
 
     protected int verticalExtent = 10;
 
     protected int mode = MODE_SCROLLING;
 
     /**
      * While the (horizontal) maximum of columns displayed by this scrollpane
      * will never be greater than the number of available columns in the model
      * of the contained scrollable, the (vertical) maximum of rows displayed by
      * this scrollpane CAN be greater than the number of available rows in the
      * model of the contained scrollable. For example this might be true if ...
      *   - MODE == SCROLLING and this scrollpane's vertical extent was set to
      *     something greater than the number of available rows in the model, or
      *   - MODE == PAGING and there are not enough rows in the model in order
      *     to fill up the last page so that the full vertical extent is reached.
      * In such cases the scrollable's CG fills the difference with empty lines.
      * The "virtualViewportHeight" equals der number of available rows in the
      * scrollable's model (typically "scrollable.getScrollableViewport()") PLUS
      * the number of empty lines needed to fill the viewport until the vertical
      * extent of this scrollpane is reached.
      * If MODE == COMPLETE this variable equals the number of available rows.
      */
     protected int virtualViewportHeight;
 
     /**
      * Holds the viewport that the scrollable had before adding it to this
      * scrollpane. The scrollable's viewport is reset to this value, if it
      * is removed from the this scrollpane.
      */
     protected Rectangle backupViewport;
 
     protected SViewportSynchronizationModel horizontalModel = new SViewportSynchronizationModel(true);
 
     protected SViewportSynchronizationModel verticalModel = new SViewportSynchronizationModel(false);
 
 
     public SScrollPane() {
         super(new SScrollPaneLayout());
         setHorizontalScrollBar(new SScrollBar(SConstants.HORIZONTAL));
         setVerticalScrollBar(new SScrollBar(SConstants.VERTICAL));
     }
 
     public SScrollPane(SComponent c) {
         this();
         setViewportView(c);
     }
 
     /**
      * Sets the element which should be scrolled.
      *
      * @param c the element which should be scrolled.
      */
     protected void setScrollable(SComponent c) {
         if (scrollable != null) {
             // reset the scrollable's viewport to the one
             // it had before adding it to this scrollpane
             scrollable.setViewportSize(backupViewport);
         }
 
         if (c instanceof Scrollable) {
             scrollable = (Scrollable) c;
             horizontalModel.setScrollable(scrollable);
             verticalModel.setScrollable(scrollable);
 
             // keep the scrollable's original viewport - the
             // one it had before adding it to this scrollpane
             backupViewport = scrollable.getViewportSize();
 
             // apply new viewport to scrollable
             setInitialViewportSize();
         } else {
             scrollable = null;
         }
 
         reload();
     }
 
     /**
      * Returns the element which should be scrolled.
      *
      * @return the element which should be scrolled.
      */
     public final Scrollable getScrollable() {
         return scrollable;
     }
 
     /**
      * Sets the scrollable.
      * If there is already one, it will be removed first.
      *
      * @param view the component to add to the viewport
      */
     public void setViewportView(SComponent view) {
        add(view);
     }
 
     /**
      * Only {@link Scrollable scrollables} are allowed here!
      */
     public SComponent addComponent(SComponent c, Object constraint, int index) {
         if (c instanceof Scrollable || constraint == SScrollPaneLayout.VIEWPORT) {
             super.addComponent(c, SScrollPaneLayout.VIEWPORT, index);
             setScrollable(c);
         } else {
             super.addComponent(c, constraint, index);
         }
         return c;
     }
 
     protected SComponent addMe(SComponent c, Object constraint, int index) {
         return super.addComponent(c, constraint, index);
     }
 
     public int getMode() {
         return mode;
     }
 
     public void setMode(int mode) {
         reloadIfChange(this.mode, mode);
         this.mode = mode;
         setInitialViewportSize();
     }
 
     public void setCG(ScrollPaneCG cg) {
         super.setCG(cg);
     }
 
     /**
      * Returns the horizontal scroll bar.
      *
      * @return the scrollbar that controls the viewports horizontal view position
      */
     public Adjustable getHorizontalScrollBar() {
         return horizontalScrollBar;
     }
 
     /**
      * Sets the horizontal scroll bar.
      *
      * @param sb the scrollbar that controls the viewports horizontal view position
      */
     public void setHorizontalScrollBar(Adjustable sb) {
         setHorizontalScrollBar(sb, SScrollPaneLayout.SOUTH);
     }
 
     /**
      * Sets the horizontal scrollbar.
      *
      * @param sb         the scrollbar that controls the viewports horizontal view position
      * @param constraint the constraint for the {@link LayoutManager} of this {@link SContainer}.
      *                   The {@link LayoutManager} is per default {@link SScrollPaneLayout}.
      */
     public void setHorizontalScrollBar(Adjustable sb, String constraint) {
         if (horizontalScrollBar != null) {
             if (horizontalScrollBar instanceof SAbstractAdjustable)
                 ((SAbstractAdjustable) horizontalScrollBar).setModel(new SDefaultBoundedRangeModel());
 
             if (horizontalScrollBar instanceof SComponent)
                 super.remove((SComponent) horizontalScrollBar);
         }
 
         horizontalScrollBar = sb;
 
         if (horizontalScrollBar != null) {
             if (horizontalScrollBar instanceof SComponent)
                 super.addComponent((SComponent) horizontalScrollBar, constraint, getComponentCount());
 
             if (horizontalScrollBar instanceof SAbstractAdjustable) {
                 SAbstractAdjustable scrollbar = (SAbstractAdjustable) horizontalScrollBar;
                 if (scrollbar.getOrientation() == SConstants.HORIZONTAL)
                     scrollbar.setModel(horizontalModel);
                 else
                     scrollbar.setModel(verticalModel);
             }
 
             adoptScrollBarVisibility(horizontalScrollBar, horizontalScrollBarPolicy);
         }
 
         reload();
     }
 
     /**
      * Returns the horizontal scroll bar policy value.
      *
      * @return the horizontal scrollbar policy.
      * @see #setHorizontalScrollBarPolicy(int)
      */
     public final int getHorizontalScrollBarPolicy() {
         return horizontalScrollBarPolicy;
     }
 
     /**
      * Returns the vertical scrollbar.
      *
      * @return the scrollbar that controls the viewports vertical view position
      */
     public final Adjustable getVerticalScrollBar() {
         return verticalScrollBar;
     }
 
     /**
      * Sets the vertical scroll bar.
      *
      * @param sb the scrollbar that controls the viewports vertical view position
      */
     public void setVerticalScrollBar(Adjustable sb) {
         setVerticalScrollBar(sb, SScrollPaneLayout.EAST);
     }
 
     /**
      * Sets the vertical scroll bar.
      *
      * @param sb         the scrollbar that controls the viewports vertical view position
      * @param constraint the constraint for the {@link LayoutManager} of this {@link SContainer}.
      *                   The {@link LayoutManager} is per default {@link SScrollPaneLayout}.
      */
     public void setVerticalScrollBar(Adjustable sb, String constraint) {
         if (verticalScrollBar != null) {
             if (verticalScrollBar instanceof SAbstractAdjustable)
                 ((SAbstractAdjustable) verticalScrollBar).setModel(new SDefaultBoundedRangeModel());
 
             if (verticalScrollBar instanceof SComponent)
                 super.remove((SComponent) verticalScrollBar);
         }
 
         verticalScrollBar = sb;
 
         if (verticalScrollBar != null) {
             if (verticalScrollBar instanceof SComponent)
                 super.addComponent((SComponent) verticalScrollBar, constraint, getComponentCount());
 
             if (verticalScrollBar instanceof SAbstractAdjustable) {
                 SAbstractAdjustable scrollbar = (SAbstractAdjustable) verticalScrollBar;
                 if (scrollbar.getOrientation() == SConstants.HORIZONTAL)
                     scrollbar.setModel(horizontalModel);
                 else
                     scrollbar.setModel(verticalModel);
             }
 
             adoptScrollBarVisibility(verticalScrollBar, verticalScrollBarPolicy);
         }
 
         reload();
     }
 
     /**
      * Returns the vertical scroll bar policy value.
      *
      * @return the vertical scrollbar policy.
      * @see #setVerticalScrollBarPolicy(int)
      */
     public final int getVerticalScrollBarPolicy() {
         return verticalScrollBarPolicy;
     }
 
     /**
      * Determines when the horizontal scrollbar appears in the scrollpane.
      * The options are:
      * <li><code>SScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED</code></li>
      * <li><code>SScrollPane.HORIZONTAL_SCROLLBAR_NEVER</code></li>
      * <li><code>SScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS</code></li>
      */
     public void setHorizontalScrollBarPolicy(int policy) {
         if (policy != horizontalScrollBarPolicy) {
             horizontalScrollBarPolicy = policy;
             adoptScrollBarVisibility(horizontalScrollBar, policy);
             reload();
         }
     }
 
     /**
      * Determines when the vertical scrollbar appears in the scrollpane.
      * The options are:
      * <li><code>SScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED</code></li>
      * <li><code>SScrollPane.VERTICAL_SCROLLBAR_NEVER</code></li>
      * <li><code>SScrollPane.VERTICAL_SCROLLBAR_ALWAYS</code></li>
      */
     public void setVerticalScrollBarPolicy(int policy) {
         if (policy != verticalScrollBarPolicy) {
             verticalScrollBarPolicy = policy;
             adoptScrollBarVisibility(verticalScrollBar, policy);
             reload();
         }
     }
 
     public void setHorizontalExtent(int horizontalExtent) {
         reloadIfChange(this.horizontalExtent, horizontalExtent);
         this.horizontalExtent = horizontalExtent;
         horizontalModel.setExtent(horizontalExtent);
     }
 
     public final int getHorizontalExtent() {
         return horizontalExtent;
     }
 
     public void setVerticalExtent(int verticalExtent) {
         reloadIfChange(this.verticalExtent, verticalExtent);
         this.verticalExtent = verticalExtent;
         verticalModel.setExtent(verticalExtent);
     }
 
     public final int getVerticalExtent() {
         return verticalExtent;
     }
 
     public void scrollRectToVisible(Rectangle aRect) {
         Rectangle viewport = scrollable.getScrollableViewportSize();
 
         // This should never happen. If it happen we got a serious
         // problem, because we cannot determine what to scroll...
         if (viewport == null) {
             return;
         }
 
         Adjustable hbar = getHorizontalScrollBar();
         if (hbar != null &&
                 getHorizontalScrollBarPolicy() != HORIZONTAL_SCROLLBAR_NEVER) {
             int nval = scrollValue(hbar.getValue(), getHorizontalExtent(),
                     aRect.x, aRect.width,
                     hbar.getUnitIncrement());
             if (nval != hbar.getValue()) {
                 hbar.setValue(nval);
             }
         }
 
         Adjustable vbar = getVerticalScrollBar();
         if (vbar != null &&
                 getVerticalScrollBarPolicy() != VERTICAL_SCROLLBAR_NEVER) {
             int nval = scrollValue(vbar.getValue(), getVerticalExtent(),
                     aRect.y, aRect.height,
                     vbar.getUnitIncrement());
             if (nval != vbar.getValue()) {
                 vbar.setValue(nval);
             }
         }
     }
 
     /**
      * Calculate the best new position to show the given range.
      *
      * @param pos   the current position
      * @param size  the current visible amount
      * @param rpos  the start-position of the range to expose
      * @param rsize the size of the range to expose
      * @param inc   the unit-increment to advance pos
      * @return pos the new position
      */
     protected int scrollValue(int pos, int size, int rpos, int rsize, int inc) {
         if (pos <= rpos &&
                 (pos + size) >= (rpos + rsize)) {
             // nothing to do
             return pos;
         }
 
         if (pos > rpos) {
             // scroll backward - ignore rsize, either it fits or it doesn't,
             // just make sure the difference between pos and rpos is as
             // small as possible.
             while (pos > rpos) {
                 pos -= inc;
             }
         } else {
             // scroll forward
             while ((pos + size) < (rpos + rsize) && (pos + inc) <= rpos) {
                 pos += inc;
             }
         }
         return pos;
     }
 
     private void setInitialViewportSize() {
         if (scrollable == null) return;
 
         if (mode == MODE_COMPLETE){
             scrollable.setViewportSize(scrollable.getScrollableViewportSize());
         } else {
             scrollable.setViewportSize(new Rectangle(0, 0, horizontalExtent, verticalExtent));
             adoptScrollBarVisibility(horizontalScrollBar, horizontalScrollBarPolicy);
             adoptScrollBarVisibility(verticalScrollBar, verticalScrollBarPolicy);
         }
     }
 
     protected void adoptScrollBarVisibility(Adjustable scrollbar, int policy) {
         if (scrollbar != null && scrollable != null) {
             Rectangle maxVp = scrollable.getScrollableViewportSize();
             Rectangle curVp = scrollable.getViewportSize();
             if (maxVp != null && curVp != null) {
                 boolean newVisibility;
                 if (scrollbar.getOrientation() == SConstants.HORIZONTAL) {
                     newVisibility = isScrollBarVisible(policy, maxVp.width, curVp.width);
                 } else {
                     newVisibility = isScrollBarVisible(policy, maxVp.height, curVp.height);
                 }
                 ((SComponent) scrollbar).setVisible(newVisibility);
             }
         }
     }
 
     private boolean isScrollBarVisible(int policy, int maxRecords, int maxDisplayed) {
         return isPolicyAlways(policy) || (isPolicyAsNeeded(policy) && maxRecords > maxDisplayed);
     }
 
     private boolean isPolicyAlways(int policy) {
         return policy == HORIZONTAL_SCROLLBAR_ALWAYS || policy == VERTICAL_SCROLLBAR_ALWAYS;
     }
 
     private boolean isPolicyAsNeeded(int policy) {
         return policy == HORIZONTAL_SCROLLBAR_AS_NEEDED || policy == VERTICAL_SCROLLBAR_AS_NEEDED;
     }
 
     /**
      * A model synchronizing the scrollbar settings with the viewport of the given scrollable.
      */
     class SViewportSynchronizationModel implements SBoundedRangeModel, SViewportChangeListener {
 
         private boolean horizontal;
         private Scrollable scrollable;
         private boolean isAdjusting = false;
         private boolean delayEvents = false;
 
         /**
          * Indicates if we have got a delayed Event
          */
         protected boolean gotDelayedEvent = false;
 
         /**
          * Only one <code>ChangeEvent</code> is needed per model instance
          * since the event's only (read-only) state is the source property.
          */
         protected transient ChangeEvent changeEvent = null;
 
         /**
          * The listeners waiting for model or viewport changes respectively.
          */
         protected EventListenerList listenerList = new EventListenerList();
 
         /**
          * Constructs a SViewportSynchronizationModel for either a horizontal or
          * vertical scrollbar that has to be synchronized with the scrollable.
          */
         public SViewportSynchronizationModel(boolean horizontal) {
             this.horizontal = horizontal;
         }
 
         public Scrollable getScrollable() {
             return scrollable;
         }
 
         public void setScrollable(Scrollable scrollable) {
             if (this.scrollable != null)
                 this.scrollable.removeViewportChangeListener(this);
 
             this.scrollable = scrollable;
 
             if (this.scrollable != null)
                 this.scrollable.addViewportChangeListener(this);
         }
 
         public int getValue() {
             if (!isViewportAvailable())	return 0;
             Rectangle curVp = scrollable.getViewportSize();
 
             if (horizontal)	return curVp.x;
             else return curVp.y;
         }
 
         public void setValue(int newValue) {
             if (!isViewportAvailable()) return;
             Rectangle curVp = scrollable.getViewportSize();
             Rectangle maxVp = scrollable.getScrollableViewportSize();
 
             if (horizontal) {
                 // Check range in order to prevent unnecessary update of views.
                 // This is primarily useful to prevent reloads in case the user
                 // wants to scroll further left/right than it's actually possible.
                 newValue = Math.min(maxVp.width - curVp.width, newValue);
                 newValue = Math.max(0, newValue);
                 if (curVp.x != newValue) {
                     curVp.x = newValue;
                     updateViews();
                 }
             } else {
                 // Check range in order to prevent unnecessary update of views.
                 // This is primarily useful to prevent reloads in case the user
                 // wants to scroll further up/down than it's actually possible.
                 newValue = Math.min(virtualViewportHeight - curVp.height, newValue);
                 newValue = Math.max(0, newValue);
                 if (curVp.y != newValue) {
                     curVp.y = newValue;
                     updateViews();
                 }
             }
         }
 
         public int getExtent() {
             if (!isViewportAvailable()) return 10;
             Rectangle curVp = scrollable.getViewportSize();
 
             if (horizontal)	return curVp.width;
             else return curVp.height;
         }
 
         public void setExtent(int newExtent) {
             if (!isViewportAvailable()) return;
             Rectangle curVp = scrollable.getViewportSize();
 
             if (horizontal && curVp.width != newExtent) {
                 // Keep extent in sync with scrollpane
                 if (horizontalExtent != newExtent) {
                     horizontalExtent = newExtent;
                     SScrollPane.this.reload();
                 }
                 curVp.width = newExtent;
                 updateViews();
             } else if (curVp.height != newExtent) {
                 // Keep extent in sync with scrollpane
                 if (verticalExtent != newExtent) {
                     verticalExtent = newExtent;
                     SScrollPane.this.reload();
                 }
                 curVp.height = newExtent;
                 updateViews();
             }
         }
 
         public int getMinimum() {
             return 0;
         }
 
         public void setMinimum(int newMinimum) {
             // minimum should always be zero
         }
 
         public int getMaximum() {
             if (!isScrollableViewportAvailable()) return 100;
             Rectangle maxVp = scrollable.getScrollableViewportSize();
 
             // The horizontal maximum should never be greater than the
             // number of available columns in the scrollable's model.
             // In contrast, the vertical maximum CAN be greater than the
             // number of available rows in the scrollable's model, i.e.
             // if this scrollpane's vertical extent was set to something
             // greater than the number of available rows. In such cases
             // the scrollable's CG fills the difference with empty lines.
             if (horizontal)	return maxVp.width;
             else return virtualViewportHeight;
         }
 
         public void setMaximum(int newMaximum) {
             if (!isScrollableViewportAvailable()) return;
             Rectangle maxVp = scrollable.getScrollableViewportSize();
 
             if (horizontal && maxVp.width != newMaximum) {
                 maxVp.width = newMaximum;
                 updateViews();
             } else if (maxVp.height != newMaximum) {
                 maxVp.height = newMaximum;
                 updateViews();
             }
         }
 
         public boolean getValueIsAdjusting() {
             return isAdjusting;
         }
 
         public void setValueIsAdjusting(boolean b) {
             isAdjusting = b;
         }
 
         public void setRangeProperties(int value, int extent, int min, int max, boolean adjusting) {
             setValue(value);
             setExtent(extent);
             setMaximum(max);
             setValueIsAdjusting(adjusting);
         }
 
         public boolean getDelayEvents() {
             return delayEvents;
         }
 
         public void setDelayEvents(boolean b) {
             delayEvents = b;
         }
 
         public void fireDelayedIntermediateEvents() {
             // there are no intermediate events to fire
         }
 
         public void fireDelayedFinalEvents() {
             if (!delayEvents && gotDelayedEvent) {
                 // Reloads scrollbar
                 fireStateChanged();
                 gotDelayedEvent = false;
             }
         }
 
         public void viewportChanged(SViewportChangeEvent e) {
             if (mode == MODE_COMPLETE) {
                 // In this mode we don't have to do anything special like adjusting
                 // values or printing empty lines. We always want so see everything
                 // - not more and not less!
                 scrollable.setViewportSize(scrollable.getScrollableViewportSize());
             } else if (horizontal == e.isHorizontalChange()) {
                 // If the event was for THIS and not the other model ...
                 Rectangle maxVp = scrollable.getScrollableViewportSize();
                 Rectangle curVp = scrollable.getViewportSize();
 
                 if (maxVp != null && curVp != null) {
 
                     SComponent scrollbar;
                     boolean newVisibility;
 
                     if (horizontal) {
                         // Do not show more than we want or more than we have.
                         curVp.width = Math.min(horizontalExtent, maxVp.width);
 
                         // Check range! This is primarily useful to prevent the
                         // indices in the CGs to be out of bounds. An application
                         // developer might accidentally set such a wrong viewport.
                         curVp.x = Math.min(maxVp.width - curVp.width, curVp.x);
                         curVp.x = Math.max(0, curVp.x);
 
                         // Determine the new visibility of the horizontal scrollbar
                         scrollbar = (SComponent) horizontalScrollBar;
                         newVisibility = isScrollBarVisible(horizontalScrollBarPolicy, maxVp.width, curVp.width);
                     } else {
                         // Set the "virtualViewportHeight"
                         if (mode == MODE_PAGING) {
                             // Determine the number of empty cells that are needed in order to fill the last page
                             int emptyCells = (verticalExtent - (maxVp.height % verticalExtent)) % verticalExtent;
                             virtualViewportHeight = maxVp.height + emptyCells;
                         }
                         else
                             virtualViewportHeight = Math.max(verticalExtent, maxVp.height);
 
                         // Check range! This is primarily useful to prevent the
                         // indices in the CGs to be out of bounds. An application
                         // developer might accidentally set such a wrong viewport.
                         curVp.y = Math.min(virtualViewportHeight - curVp.height, curVp.y);
                         curVp.y = Math.max(0, curVp.y);
 
                         // Determine the new visibility of the vertical scrollbar
                         scrollbar = (SComponent) verticalScrollBar;
                         newVisibility = isScrollBarVisible(verticalScrollBarPolicy, maxVp.height, curVp.height);
                     }
 
                     if (scrollbar != null) {
                         // Prevent the scrollbar(s) from an unnecessary reload
                         if (!scrollbar.isVisible() && newVisibility == false) {
                             return;
                         }
                         // Apply the scrollbar's new visibility
                         scrollbar.setVisible(newVisibility);
                     }
 
                     if (delayEvents) {
                         gotDelayedEvent = true;
                     } else {
                         // Reloads scrollbar
                         fireStateChanged();
                     }
                 }
             }
         }
 
         /**
          * Adds a <code>ChangeListener</code>.
          *
          * @param l the <code>ChangeListener</code> to add
          * @see #removeChangeListener
          * @see BoundedRangeModel#addChangeListener
          */
         public void addChangeListener(ChangeListener l) {
             listenerList.add(ChangeListener.class, l);
         }
 
         /**
          * Removes a <code>ChangeListener</code>.
          *
          * @param l the <code>ChangeListener</code> to remove
          * @see #addChangeListener
          * @see BoundedRangeModel#removeChangeListener
          */
         public void removeChangeListener(ChangeListener l) {
             listenerList.remove(ChangeListener.class, l);
         }
 
         /**
          * Runs each <code>ChangeListener</code>'s <code>stateChanged</code> method.
          *
          * @see #setRangeProperties
          * @see EventListenerList
          */
         protected void fireStateChanged() {
             Object[] listeners = listenerList.getListenerList();
             for (int i = listeners.length - 2; i >= 0; i -=2) {
                 if (listeners[i] == ChangeListener.class) {
                     if (changeEvent == null) {
                         changeEvent = new ChangeEvent(scrollable);
                     }
                     ((ChangeListener) listeners[i+1]).stateChanged(changeEvent);
                 }
             }
         }
 
         private void updateViews() {
             // Reload scrollbar and scrollable in order to display the changes
             viewportChanged(new SViewportChangeEvent(scrollable, horizontal));
             ((SComponent) scrollable).reload();
         }
 
         private boolean isViewportAvailable() {
             return scrollable != null && scrollable.getViewportSize() != null;
         }
 
         private boolean isScrollableViewportAvailable() {
             return scrollable != null && scrollable.getScrollableViewportSize() != null;
         }
     }
 }
