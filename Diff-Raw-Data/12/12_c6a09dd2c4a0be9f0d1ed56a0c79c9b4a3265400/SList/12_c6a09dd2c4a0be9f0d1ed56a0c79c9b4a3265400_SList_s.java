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
 
 import org.wings.event.SViewportChangeEvent;
 import org.wings.event.SViewportChangeListener;
 import org.wings.event.SMouseEvent;
 import org.wings.plaf.ListCG;
 import org.wings.style.CSSAttributeSet;
 import org.wings.style.CSSProperty;
 import org.wings.style.CSSStyleSheet;
 import org.wings.style.Selector;
 import org.wings.sdnd.TextAndHTMLTransferable;
 import org.wings.sdnd.CustomDragHandler;
 import org.wings.sdnd.SDropMode;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import javax.swing.*;
 import javax.swing.event.EventListenerList;
 import javax.swing.event.ListDataListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import java.awt.*;
 import java.awt.datatransfer.Transferable;
 import java.util.ArrayList;
 import java.util.List;
 
 
 /**
  * Allows the user to select one or more objects from a list.
  * <em>CAVEAT</em>
  * A list in a form has special implications to take care of:
  * Problem with a form request
  * is, that we should fire the selection change events not until the states
  * of all components are consistent. Unfortunately we cannot avoid events
  * before that
  * entirely. Problem is, that we use Swing Models for selection and they
  * don't know anything about asynchronous state change. They will fire their
  * events just after we set a state. But inside a form we have to change
  * many states of many components, all at once. And events should arise
  * first, after we set the new state of all components. So as a trade-off we
  * decided to use the setValueIsAdjusting in the ListSelectionModel as an
  * indicator,
  * if components are consistent. That is, if you get an SelectionEvent with
  * getValueIsAdjusting true, you cannot be sure, that component states are
  * consistent, so don't use that events. But you will get an event without
  * isValueAdjusting. You can work with that event. If you want to avoid that
  * problem, just use the selection events from the list itself, register your
  * listener at SList rather than at the ListSelectionModel...
  *
  * @author Holger Engels
  * @author <a href="mailto:armin.haaf@mercatis.de">Armin Haaf</a>
  * @see javax.swing.ListModel
  * @see SDefaultListModel
  * @see javax.swing.ListSelectionModel
  * @see SListCellRenderer
  */
 public class SList extends SComponent implements Scrollable, LowLevelEventListener, ListDataListener {
     private static final Log LOG = LogFactory.getLog(SList.class);
 
     /**
      * The type for an ordered list. See {@link #setType(String)} and ORDER_TYPE_xxx
      */
     public static final String ORDERED_LIST = "ol";
 
     /**
      * The type for an unordered list. See {@link #setType(String)}
      */
     public static final String UNORDERED_LIST = "ul";
 
     /**
      * The type for an menu-like list. See {@link #setType(String)}
      */
     public static final String MENU_LIST = "menu";
 
     /**
      * The type for an TO-DO list. See {@link #setType(String)}
      */
     public static final String DIR_LIST = "dir";
 
     /**
      * Order type for for {@link #setOrderType(String)}
      */
     public static final String[] ORDER_TYPE_CIRCLE = {"ul", "circle"};
     /**
      * Order type for for {@link #setOrderType(String)}
      */
     public static final String[] ORDER_TYPE_SQUARE = {"ul", "square"};
     /**
      * Order type for for {@link #setOrderType(String)}
      */
     public static final String[] ORDER_TYPE_DISC = {"ul", "disc"};
     /**
      * Order type for for {@link #setOrderType(String)}
      */
     public static final String[] ORDER_TYPE_BIG_ALPHA = {"ol", "A"};
     /**
      * Order type for for {@link #setOrderType(String)}
      */
     public static final String[] ORDER_TYPE_SMALL_ALPHA = {"ol", "a"};
     /**
      * Order type for for {@link #setOrderType(String)}
      */
     public static final String[] ORDER_TYPE_NUMBER = {"ol", null};
     /**
      * Order type for for {@link #setOrderType(String)}
      */
     public static final String[] ORDER_TYPE_NORMAL = {"ul", null};
     /**
      * Order type for for {@link #setOrderType(String)}
      */
     public static final String[] ORDER_TYPE_BIG_ROMAN = {"ol", "I"};
     /**
      * Order type for for {@link #setOrderType(String)}
      */
     public static final String[] ORDER_TYPE_SMALL_ROMAN = {"ol", "i"};
 
     /**
      * Table selection model. See {@link SList#setSelectionMode(int)}
      */
     public static final int NO_SELECTION = SListSelectionModel.NO_SELECTION;
     /**
      * Table selection model. See {@link SList#setSelectionMode(int)}
      */
     public static final int SINGLE_SELECTION = SListSelectionModel.SINGLE_SELECTION;
     /**
      * Table selection model. See {@link SList#setSelectionMode(int)}
      */
     public static final int SINGLE_INTERVAL_SELECTION = SListSelectionModel.SINGLE_INTERVAL_SELECTION;
     /**
      * Table selection model. See {@link SList#setSelectionMode(int)}
      */
     public static final int MULTIPLE_SELECTION = SListSelectionModel.MULTIPLE_INTERVAL_SELECTION;
     /**
      * Table selection model. See {@link SList#setSelectionMode(int)}
      */
     public static final int MULTIPLE_INTERVAL_SELECTION = SListSelectionModel.MULTIPLE_INTERVAL_SELECTION;
 
     /**
      * The Selector for this component.
      */
     public static final Selector SELECTOR_SELECTION = new Selector("SELECTION");
 
     /**
      * The preferred extent of the list.
      */
     private int visibleRowCount = 8;
 
 
     private SListSelectionModel selectionModel;
 
 
     private ListModel dataModel;
 
 
     private SListCellRenderer cellRenderer;
 
     /**
      * Implementation of the {@link Scrollable} interface.
      */
     protected Rectangle viewport;
 
     /**
      * @see LowLevelEventListener#isEpochCheckEnabled()
      */
     private boolean epochCheckEnabled = true;
 
     /**
      * <li type="...">
      */
     protected String type = UNORDERED_LIST;
 
     /**
      * <li type="...">
      */
     protected String orderType = null;
 
     /**
      * <li start="...">
      */
     protected int start = 0;
 
 
     /**
      * used to forward selection events to selection listeners of the list
      */
     private final ListSelectionListener fwdSelectionEvents = new ListSelectionListener() {
         public void valueChanged(ListSelectionEvent e) {
             fireSelectionValueChanged(e.getFirstIndex(), e.getLastIndex(), e.getValueIsAdjusting());
             if (isUpdatePossible() && SList.class.isAssignableFrom(SList.this.getClass())) {
                 List deselectedIndices = new ArrayList();
                 List selectedIndices = new ArrayList();
                 for (int index = e.getFirstIndex(); index <= e.getLastIndex(); ++index) {
                     int visibleIndex = index;
                     if (getViewportSize() != null) {
                         visibleIndex = index - getViewportSize().y;
                         if (visibleIndex < 0 || visibleIndex >= getViewportSize().height)
                             continue;
                     }
                    if (isSelectedIndex(index)) {
                        selectedIndices.add(new Integer(visibleIndex));
                    } else {
                        deselectedIndices.add(new Integer(visibleIndex));
                     }
                 }
                 update(((ListCG) getCG()).getSelectionUpdate(SList.this, deselectedIndices, selectedIndices));
             } else {
                 reload();
             }
        }
     };
 
     /**
      * Construct a SList that displays the elements in the specified model.
      */
     public SList(ListModel dataModel) {
         if (dataModel == null) {
             throw new IllegalArgumentException("dataModel must not be null");
         }
         if (this.dataModel != null) this.dataModel.removeListDataListener(this);
         this.dataModel = dataModel;
         this.dataModel.addListDataListener(this);
         setSelectionModel(createSelectionModel());
         installTransferHandler();
         createActionMap();
     }
 
 
     /**
      * Construct a SList that displays the elements in the specified
      * array.
      */
     public SList(final Object[] listData) {
         this(new AbstractListModel() {
             public int getSize() {
                 return listData.length;
             }
 
             public Object getElementAt(int i) {
                 return listData[i];
             }
         });
     }
 
 
     /**
      * Construct a SList that displays the elements in the specified
      * Vector.
      */
     public SList(final List listData) {
         this(new AbstractListModel() {
             public int getSize() {
                 return listData.size();
             }
 
             public Object getElementAt(int i) {
                 return listData.get(i);
             }
         });
     }
 
 
     /**
      * Constructs a SList with an empty model.
      */
     public SList() {
         this(new AbstractListModel() {
             public int getSize() {
                 return 0;
             }
 
             public Object getElementAt(int i) {
                 return "No Data Model";
             }
         });
     }
 
     /**
      * Returns the cell renderer.
      *
      * @return the ListCellRenderer
      * @see #setCellRenderer
      */
     public final SListCellRenderer getCellRenderer() {
         return cellRenderer;
     }
 
     /**
      * Sets the renderer that's used to write out each cell in the list.
      *
      * @param cellRenderer the SListCellRenderer that paints list cells
      *                     description: The component used to draw the cells.
      * @see #getCellRenderer
      */
     public void setCellRenderer(SListCellRenderer cellRenderer) {
         SListCellRenderer oldValue = this.cellRenderer;
         this.cellRenderer = cellRenderer;
         reloadIfChange(oldValue, cellRenderer);
         propertyChangeSupport.firePropertyChange("cellRenderer", oldValue, this.cellRenderer);
     }
 
 
     /**
      * Return the background color.
      *
      * @return the background color
      */
     public Color getSelectionBackground() {
         return dynamicStyles == null || dynamicStyles.get(SELECTOR_SELECTION) == null ? null : CSSStyleSheet.getBackground((CSSAttributeSet) dynamicStyles.get(SELECTOR_SELECTION));
     }
 
     /**
      * Set the foreground color.
      *
      * @param color the new foreground color
      */
     public void setSelectionBackground(Color color) {
         Color oldVal = this.getSelectionBackground();
         setAttribute(SELECTOR_SELECTION, CSSProperty.BACKGROUND_COLOR, CSSStyleSheet.getAttribute(color));
         propertyChangeSupport.firePropertyChange("selectionBackground", oldVal, this.getSelectionBackground());
     }
 
     /**
      * Return the foreground color.
      *
      * @return the foreground color
      */
     public Color getSelectionForeground() {
         return dynamicStyles == null || dynamicStyles.get(SELECTOR_SELECTION) == null ? null : CSSStyleSheet.getForeground((CSSAttributeSet) dynamicStyles.get(SELECTOR_SELECTION));
     }
 
     /**
      * Set the foreground color.
      *
      * @param color the new foreground color
      */
     public void setSelectionForeground(Color color) {
         Color oldVal = this.getSelectionForeground();
         setAttribute(SELECTOR_SELECTION, CSSProperty.COLOR, CSSStyleSheet.getAttribute(color));
         propertyChangeSupport.firePropertyChange("selectionForeground", oldVal, this.getSelectionForeground());
     }
 
     /**
      * Set the font.
      *
      * @param font the new font
      */
     public void setSelectionFont(SFont font) {
         SFont oldVal = this.getSelectionFont();
         setAttributes(SELECTOR_SELECTION, CSSStyleSheet.getAttributes(font));
         propertyChangeSupport.firePropertyChange("selectionFont", oldVal, this.getSelectionFont());
     }
 
     /**
      * Return the font.
      *
      * @return the font
      */
     public SFont getSelectionFont() {
         return dynamicStyles == null || dynamicStyles.get(SELECTOR_SELECTION) == null ? null : CSSStyleSheet.getFont((CSSAttributeSet) dynamicStyles.get(SELECTOR_SELECTION));
     }
 
     /**
      * Return the preferred number of visible rows. If rendered as a form
      * component it is used for the size-attribute.
      *
      * @return the preferred number of rows to display
      * @see #setVisibleRowCount
      */
     public final int getVisibleRowCount() {
         return visibleRowCount;
     }
 
     /**
      * Set the preferred number of rows in the list that can be displayed
      * without a scollbar.
      * <p/>
      * The default value of this property is 8.
      *
      * @param visibleRowCount the preferred number of visible rows
      *                        description: The preferred number of cells that can be displayed without a scrollbar.
      * @see #getVisibleRowCount
      */
     public void setVisibleRowCount(int visibleRowCount) {
         if (this.visibleRowCount != visibleRowCount) {
             int oldVal = this.visibleRowCount;
             this.visibleRowCount = Math.max(0, visibleRowCount);
             reload();
             propertyChangeSupport.firePropertyChange("visibleRowCount", oldVal, this.visibleRowCount);
         }
     }
 
 
     /**
      * --- ListModel Support ---
      */
 
 
     /**
      * Returns the data model that holds the items.
      *
      * @return the ListModel
      * @see #setModel
      */
     public ListModel getModel() {
         return dataModel;
     }
 
     /**
      * Sets the model
      *
      * @param model the ListModel that provides the list of items
      *              description: The object that contains the data to be shownin the list.
      * @see #getModel
      */
     public void setModel(ListModel model) {
         if (model == null) {
             throw new IllegalArgumentException("model must be non null");
         }
         if (isDifferent(dataModel, model)) {
             ListModel oldVal = this.dataModel;
             clearSelection();
             dataModel = model;
             dataModel.addListDataListener(this);
 
             fireViewportChanged(false);
             reload();
             propertyChangeSupport.firePropertyChange("model", oldVal, this.dataModel);
         }
     }
 
 
     /**
      * A convenience method that constructs a ListModel from an array of Objects
      * and then applies setModel to it.
      *
      * @param listData an array of Objects containing the items to display
      *                 in the list
      * @see #setModel
      */
     public void setListData(final Object[] listData) {
         setModel(new AbstractListModel() {
             public int getSize() {
                 return listData.length;
             }
 
             public Object getElementAt(int i) {
                 return listData[i];
             }
         });
     }
 
 
     /**
      * A convenience method that constructs a ListModel from a List
      * and then applies setModel to it.
      *
      * @param listData a Vector containing the items to display in the list
      * @see #setModel
      */
     public void setListData(final List listData) {
         setModel(new AbstractListModel() {
             public int getSize() {
                 return listData.size();
             }
 
             public Object getElementAt(int i) {
                 return listData.get(i);
             }
         });
     }
 
     /**
      * creates the default selection model. It uses the swing
      * DefaultListSelectionModel, and wraps some methods to support
      * {@link SListSelectionModel#NO_SELECTION}
      */
     protected SListSelectionModel createSelectionModel() {
         return new SDefaultListSelectionModel();
     }
 
 
     /**
      * Returns the current selection model. If selection mode is
      * {@link SListSelectionModel#NO_SELECTION} it return <em>null</em>
      *
      * @return the ListSelectionModel that implements list selections.
      *         If selection mode is {@link SListSelectionModel#NO_SELECTION} it return
      *         <em>null</em>
      * @see #setSelectionMode(int)
      * @see ListSelectionModel
      */
     public SListSelectionModel getSelectionModel() {
         return selectionModel;
     }
 
 
     /**
      * This method notifies all ListSelectionListeners that
      * the selection model has changed.
      *
      * @see #addListSelectionListener
      * @see #removeListSelectionListener
      * @see EventListenerList
      */
     protected void fireSelectionValueChanged(int firstIndex, int lastIndex,
                                              boolean isAdjusting) {
         Object[] listeners = getListenerList();
         ListSelectionEvent e = null;
 
         for (int i = listeners.length - 2; i >= 0; i -= 2) {
             if (listeners[i] == ListSelectionListener.class) {
                 if (e == null) {
                     e = new ListSelectionEvent(this, firstIndex, lastIndex,
                             isAdjusting);
                 }
                 ((ListSelectionListener) listeners[i + 1]).valueChanged(e);
             }
         }
     }
 
 
     /**
      * Add a listener to the list that's notified each time a change
      * to the selection occurs.
      *
      * <p>
      * If you want to receive immedate an event when the user clicks a new item
      * on the client side you have to register additionally a Java script listener
      * which triggers a form submit. <br>
      * <code>combobox.addScriptListener(ListCG.JS_ON_CHANGE_SUBMIT)</code>
      *
      * @param listener A ListSelectionListener to be added
      * @see #getSelectionModel
      */
     public void addListSelectionListener(ListSelectionListener listener) {
         addEventListener(ListSelectionListener.class, listener);
     }
 
 
     /**
      * Remove a listener from the list that's notified each time a
      * change to the selection occurs.
      *
      * @param listener The ListSelectionListener to remove.
      * @see #addListSelectionListener
      * @see #getSelectionModel
      */
     public void removeListSelectionListener(ListSelectionListener listener) {
         removeEventListener(ListSelectionListener.class, listener);
     }
 
 
     /**
      * Returns an array of all the <code>ListSelectionListener</code>s added
      * to this JList with addListSelectionListener().
      *
      * @return all of the ListSelectionListener added
      * @since 1.4
      */
     public ListSelectionListener[] getListSelectionListeners() {
         return (ListSelectionListener[]) getListeners(ListSelectionListener.class);
     }
 
 
     /**
      * Set the selectionModel for the list.
      * The selection model keeps track of which items are selected.
      * <p/>
      * description: The selection model, recording which cells are selected.
      *
      * @see #getSelectionModel
      */
     public void setSelectionModel(SListSelectionModel selectionModel) {
         if (selectionModel == null) {
             throw new IllegalArgumentException("selectionModel must be non null");
         }
 
         SListSelectionModel oldVal = this.selectionModel;
         if (this.selectionModel != null)
             this.selectionModel.removeListSelectionListener(fwdSelectionEvents);
 
         selectionModel.addListSelectionListener(fwdSelectionEvents);
 
         this.selectionModel = selectionModel;
         propertyChangeSupport.firePropertyChange("selectionModel", oldVal, this.selectionModel);
     }
 
 
     /**
      * Allow / permit multiple selection
      * <ul>
      * <li> <code>SINGLE_SELECTION</code>
      * Only one list index can be selected at a time.
      * <li> <code>MULTIPLE_INTERVAL_SELECTION</code>
      * Multiple items can be selected.
      * </ul>
      *
      * @param selectionMode single or multiple selections
      *                      enum: SINGLE_SELECTION            ListSelectionModel.SINGLE_SELECTION
      *                      MULTIPLE_INTERVAL_SELECTION ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
      * @see #getSelectionMode
      */
     public void setSelectionMode(int selectionMode) {
         int oldVal = selectionModel.getSelectionMode();
         selectionModel.setSelectionMode(selectionMode);
         propertyChangeSupport.firePropertyChange("selectionMode", oldVal, selectionModel.getSelectionMode());
     }
 
     /**
      * Returns whether single-item or multiple-item selections are allowed.
      *
      * @return The value of the selectionMode property.
      * @see #setSelectionMode
      */
     public int getSelectionMode() {
         return getSelectionModel().getSelectionMode();
     }
 
 
     /**
      * @return The index that most recently anchored an interval selection.
      * @see ListSelectionModel#getAnchorSelectionIndex
      * @see #addSelectionInterval
      * @see #setSelectionInterval
      * @see #addListSelectionListener
      */
     public int getAnchorSelectionIndex() {
         return getSelectionModel().getAnchorSelectionIndex();
     }
 
 
     /**
      * @return The index that most recently ended a interval selection.
      * @see ListSelectionModel#getLeadSelectionIndex
      * @see #addSelectionInterval
      * @see #setSelectionInterval
      * @see #addListSelectionListener
      */
     public int getLeadSelectionIndex() {
         return getSelectionModel().getLeadSelectionIndex();
     }
 
 
     /**
      * @return The smallest selected cell index.
      * @see ListSelectionModel#getMinSelectionIndex
      * @see #addListSelectionListener
      */
     public int getMinSelectionIndex() {
         return getSelectionModel().getMinSelectionIndex();
     }
 
 
     /**
      * @return The largest selected cell index.
      * @see ListSelectionModel#getMaxSelectionIndex
      * @see #addListSelectionListener
      */
     public int getMaxSelectionIndex() {
         return getSelectionModel().getMaxSelectionIndex();
     }
 
 
     /**
      * @return True if the specified index is selected.
      * @see ListSelectionModel#isSelectedIndex
      * @see #setSelectedIndex
      * @see #addListSelectionListener
      */
     public boolean isSelectedIndex(int index) {
         return getSelectionModel().isSelectedIndex(index);
     }
 
 
     /**
      * @return True if nothing is selected
      * @see ListSelectionModel#isSelectionEmpty
      * @see #clearSelection
      * @see #addListSelectionListener
      */
     public boolean isSelectionEmpty() {
         return getSelectionModel().isSelectionEmpty();
     }
 
 
     /**
      * @see ListSelectionModel#clearSelection
      * @see #isSelectionEmpty
      * @see #addListSelectionListener
      */
     public void clearSelection() {
         if (!getSelectionModel().isSelectionEmpty()) {
             getSelectionModel().clearSelection();
             reload();
         }
     }
 
 
     /**
      * @param anchor The first index to select
      * @param lead   The last index to select
      * @see ListSelectionModel#setSelectionInterval
      * @see #addSelectionInterval
      * @see #removeSelectionInterval
      * @see #addListSelectionListener
      */
     public void setSelectionInterval(int anchor, int lead) {
         int[] oldVal = {this.getAnchorSelectionIndex(), this.getLeadSelectionIndex()};
         getSelectionModel().setSelectionInterval(anchor, lead);
         int[] newVal = {this.getAnchorSelectionIndex(), this.getLeadSelectionIndex()};
         propertyChangeSupport.firePropertyChange("selectionInterval", oldVal, newVal);
     }
 
 
     /**
      * @param anchor The first index to add to the selection
      * @param lead   The last index to add to the selection
      * @see ListSelectionModel#addSelectionInterval
      * @see #setSelectionInterval
      * @see #removeSelectionInterval
      * @see #addListSelectionListener
      */
     public void addSelectionInterval(int anchor, int lead) {
         getSelectionModel().addSelectionInterval(anchor, lead);
     }
 
 
     /**
      * @param index0 The first index to remove from the selection
      * @param index1 The last index to remove from the selection
      * @see ListSelectionModel#removeSelectionInterval
      * @see #setSelectionInterval
      * @see #addSelectionInterval
      * @see #addListSelectionListener
      */
     public void removeSelectionInterval(int index0, int index1) {
         getSelectionModel().removeSelectionInterval(index0, index1);
     }
 
 
     /**
      * @param b the value for valueIsAdjusting
      * @see ListSelectionModel#setValueIsAdjusting
      */
     public void setValueIsAdjusting(boolean b) {
         boolean oldVal = getSelectionModel().getValueIsAdjusting();
         getSelectionModel().setValueIsAdjusting(b);
         boolean newVal = getSelectionModel().getValueIsAdjusting();
         propertyChangeSupport.firePropertyChange("valueIsAdjusting", oldVal, newVal);
     }
 
     /**
      * @return the value of valueIsAdjusting
      * @see ListSelectionModel#getValueIsAdjusting
      */
     public boolean getValueIsAdjusting() {
         return getSelectionModel().getValueIsAdjusting();
     }
 
 
     /**
      * Return an array of all of the selected indices.
      *
      * @return all selected indices.
      * @see #removeSelectionInterval
      * @see #addListSelectionListener
      */
     public int[] getSelectedIndices() {
         ListSelectionModel sm = getSelectionModel();
         int iMin = sm.getMinSelectionIndex();
         int iMax = sm.getMaxSelectionIndex();
 
         if ((iMin < 0) || (iMax < 0)) {
             return new int[0];
         }
 
         int[] rvTmp = new int[1 + (iMax - iMin)];
         int n = 0;
         for (int i = iMin; i <= iMax; i++) {
             if (sm.isSelectedIndex(i)) {
                 rvTmp[n++] = i;
             }
         }
         int[] rv = new int[n];
         System.arraycopy(rvTmp, 0, rv, 0, n);
         return rv;
     }
 
 
     /**
      * Select a single cell.
      *
      * @param index The index of the one cell to select
      * @see ListSelectionModel#setSelectionInterval
      * @see #isSelectedIndex
      * @see #addListSelectionListener
      */
     public void setSelectedIndex(int index) {
         int oldVal = this.getSelectedIndex();
         getSelectionModel().setSelectionInterval(index, index);
         propertyChangeSupport.firePropertyChange("selectedIndex", oldVal, this.getSelectedIndex());
     }
 
 
     /**
      * Select some cells.
      *
      * @param indices The indices of the cells to select
      * @see ListSelectionModel#addSelectionInterval
      * @see #isSelectedIndex
      * @see #addListSelectionListener
      */
     public void setSelectedIndices(int[] indices) {
         ListSelectionModel sm = getSelectionModel();
         int[] oldVal = this.getSelectedIndices();
         sm.clearSelection();
         for (int i = 0; i < indices.length; i++) {
             sm.addSelectionInterval(indices[i], indices[i]);
         }
         propertyChangeSupport.firePropertyChange("selectedIndices", oldVal, this.getSelectedIndices());
     }
 
 
     /**
      * Return the values of the selected cells.
      * Returns only the selected elements which are in the model.
      * If the selection model indices a selection outside the the datamodel it is ignored
      *
      * @return the selected values
      * @see #isSelectedIndex
      * @see #getModel
      * @see #addListSelectionListener
      */
     public Object[] getSelectedValues() {
         ListSelectionModel sm = getSelectionModel();
         ListModel dm = getModel();
 
         int iMin = sm.getMinSelectionIndex();
         int iMax = sm.getMaxSelectionIndex();
 
         if ((iMin < 0) || (iMax < 0)) {
             return new Object[0];
         }
 
         Object[] rvTmp = new Object[1 + (iMax - iMin)];
         int n = 0;
         for (int i = iMin; i <= iMax; i++) {
             if (sm.isSelectedIndex(i) && i < dm.getSize()) {
                 rvTmp[n++] = dm.getElementAt(i);
             }
         }
         Object[] rv = new Object[n];
         System.arraycopy(rvTmp, 0, rv, 0, n);
         return rv;
     }
 
 
     /**
      * A convenience method that returns the first selected index.
      *
      * @return The first selected index.
      * @see #getMinSelectionIndex
      * @see #addListSelectionListener
      */
     public int getSelectedIndex() {
         return getMinSelectionIndex();
     }
 
 
     /**
      * A convenience method that returns the first selected value
      * or null, if nothing is selected.
      *
      * @return The first selected value.
      * @see #getMinSelectionIndex
      * @see #getModel
      * @see #addListSelectionListener
      */
     public Object getSelectedValue() {
         int i = getMinSelectionIndex();
         return (i == -1) ? null : getModel().getElementAt(i);
     }
 
 
     /**
      * Selects the specified object.
      *
      * @param anObject the Object to be selected
      */
     public void setSelectedValue(Object anObject) {
         if (anObject == null)
             setSelectedIndex(-1);
         else if (!anObject.equals(getSelectedValue())) {
             int i, c;
             ListModel dm = getModel();
             for (i = 0, c = dm.getSize(); i < c; i++)
                 if (anObject.equals(dm.getElementAt(i))) {
                     setSelectedIndex(i);
                     return;
                 }
             setSelectedIndex(-1);
         }
     }
 
 
     /*
      * Sets the list type. Use one of the following types:
      * <UL>
      *  <LI>{@link SConstants#ORDERED_LIST}
      *  <LI>{@link SConstants#UNORDERED_LIST}
      *  <LI>{@link SConstants#MENU_LIST}
      *  <LI>{@link SConstants#DIR_LIST}
      * </UL>
      * null sets default list.
      *
      * @param t the type
      */
     public void setType(String t) {
         String oldVal = this.type;
         if (t != null)
             type = t;
         else
             type = UNORDERED_LIST;
         propertyChangeSupport.firePropertyChange("type", oldVal, this.type);
     }
 
     /**
      * Return the type.
      *
      * @return the type;
      */
     public String getType() {
         return type;
     }
 
     /**
      * <li type="...">
      */
     public void setOrderType(String t) {
         String oldVal = this.orderType;
         orderType = t;
         propertyChangeSupport.firePropertyChange("orderType", oldVal, this.orderType);
     }
 
     /**
      * <li type="...">
      */
     public String getOrderType() {
         return orderType;
     }
 
     /*
      * <li type="...">
      * <code>null</code> is default style.
      */
     public void setType(String[] t) {
         if (t == null) {
             setType((String) null);
             setOrderType(null);
         } else if (t.length == 2) {
             setType(t[0]);
             setOrderType(t[1]);
         }
     }
 
     /**
      * <li start="...">
      */
     public void setStart(int s) {
         int oldVal = this.start;
         start = s;
         propertyChangeSupport.firePropertyChange("start", oldVal, this.start);
     }
 
     /**
      * <li start="...">
      */
     public int getStart() {
         return start;
     }
 
     public void fireIntermediateEvents() {
         getSelectionModel().fireDelayedIntermediateEvents();
     }
 
     public void fireFinalEvents() {
         super.fireFinalEvents();
         // fire selection events...
         getSelectionModel().fireDelayedFinalEvents();
     }
 
     /**
      * @see LowLevelEventListener#isEpochCheckEnabled()
      */
     public boolean isEpochCheckEnabled() {
         return epochCheckEnabled;
     }
 
     /**
      * @see LowLevelEventListener#isEpochCheckEnabled()
      */
     public void setEpochCheckEnabled(boolean epochCheckEnabled) {
         boolean oldVal = this.epochCheckEnabled;
         this.epochCheckEnabled = epochCheckEnabled;
         propertyChangeSupport.firePropertyChange("epochCheckEnabled", oldVal, this.epochCheckEnabled);
     }
 
     private int lastSelectedIndex;
 
     protected void addSelectionEvent(int index, boolean ctrlKey, boolean shiftKey) {
         if(index != -1) {
             if(shiftKey == false && ctrlKey == false) {
                 setSelectionInterval(index, index);
                 lastSelectedIndex = index;
             } else if(ctrlKey == true && shiftKey == false) {
                 if(!isSelectedIndex(index))
                     addSelectionInterval(index, index);
                 else
                     removeSelectionInterval(index, index);
                 lastSelectedIndex = index;
             } else if(ctrlKey == false && shiftKey == true) {
                 setSelectionInterval(lastSelectedIndex, index);
             }
         }
     }
 
     /*
      * Implement {@link LowLevelEventListener} interface.
      * @param action the name
      * @param value the value
      */
     public void processLowLevelEvent(String action, String[] values) {
         processKeyEvents(values);
         if (action.endsWith("_keystroke"))
             return;
 
         // delay events...
         getSelectionModel().setDelayEvents(true);
         getSelectionModel().setValueIsAdjusting(true);
 
         // in a form, we only get events for selected items, so for every
         // selected item, which is not in values, deselect it...
         if (getShowAsFormComponent()) {
             ArrayList selectedIndices = new ArrayList();
             for (int i = 0; i < values.length; i++) {
                 String indexString = values[i];
                 if (indexString.length() < 1) continue; // false format
 
                 try {
                     int index = Integer.parseInt(indexString);
 
                     // in a form all parameters are select parameters...
                     selectedIndices.add(new Integer(index));
                     addSelectionInterval(index, index);
                 } catch (Exception ex) {
                 }
             }
             // remove all selected indices, which are not explicitely selected by a parameter
             for (int i = 0; i < getModel().getSize(); ++i) {
                 if (isSelectedIndex(i) && !selectedIndices.contains(new Integer(i))) {
                     removeSelectionInterval(i, i);
                 }
             }
         } else {
             int index = -1;
             for (int i = 0; i < values.length; i++) {
                 String[] paramVals = values[i].split(";");
                 boolean shiftKey = false;
                 boolean ctrlKey = false;
                 for(int j = 0; j < paramVals.length; j++) {
                     String indexString = paramVals[j];
                     if (indexString.length() < 1) continue; // false format
 
                     if(indexString.startsWith("ctrlKey=")) {
                         ctrlKey = Boolean.parseBoolean(indexString.substring(indexString.indexOf("=") + 1));
                         continue;
                     } else if(indexString.startsWith("shiftKey=")) {
                         shiftKey = Boolean.parseBoolean(indexString.substring(indexString.indexOf("=") + 1));
                         continue;
                     }
 
                     try {
                         index = Integer.parseInt(indexString);
                         // toggle selection for given index
                     } catch (Exception ex) {
                     }
                 }
 
                 addSelectionEvent(index, ctrlKey, shiftKey);
             }
         }
         getSelectionModel().setValueIsAdjusting(false);
         getSelectionModel().setDelayEvents(false);
 
         SForm.addArmedComponent(this);
     }
 
     /**
      * The size of the component in respect to scrollable units.
      */
     public Rectangle getScrollableViewportSize() {
         return new Rectangle(0, 0, 1, dataModel.getSize());
     }
 
     /**
      * Returns the actual visible part of a scrollable.
      */
     public Rectangle getViewportSize() {
         return viewport;
     }
 
     /**
      * Sets the actual visible part of a scrollable.
      */
     public void setViewportSize(Rectangle newViewport) {
         Rectangle oldViewport = viewport;
         viewport = newViewport;
 
         if (isDifferent(oldViewport, newViewport)) {
             if (oldViewport == null || newViewport == null) {
                 fireViewportChanged(true);
                 fireViewportChanged(false);
             } else {
                 if (newViewport.x != oldViewport.x || newViewport.width != oldViewport.width) {
                     fireViewportChanged(true);
                 }
                 if (newViewport.y != oldViewport.y || newViewport.height != oldViewport.height) {
                     fireViewportChanged(false);
                 }
             }
             reload();
         }
         propertyChangeSupport.firePropertyChange("biewPortSize", oldViewport, this.viewport);
     }
 
     /**
      * Adds the given <code>SViewportChangeListener</code> to the scrollable.
      *
      * @param l the listener to be added
      */
     public void addViewportChangeListener(SViewportChangeListener l) {
         addEventListener(SViewportChangeListener.class, l);
     }
 
     /**
      * Removes the given <code>SViewportChangeListener</code> from the scrollable.
      *
      * @param l the listener to be removed
      */
     public void removeViewportChangeListener(SViewportChangeListener l) {
         removeEventListener(SViewportChangeListener.class, l);
     }
 
     /**
      * Notifies all listeners that have registered interest for notification
      * on changes to this scrollable's viewport in the specified direction.
      *
      * @see EventListenerList
      */
     protected void fireViewportChanged(boolean horizontal) {
         Object[] listeners = getListenerList();
         for (int i = listeners.length - 2; i >= 0; i -= 2) {
             if (listeners[i] == SViewportChangeListener.class) {
                 SViewportChangeEvent event = new SViewportChangeEvent(this, horizontal);
                 ((SViewportChangeListener) listeners[i + 1]).viewportChanged(event);
             }
         }
     }
 
     public void setParent(SContainer p) {
         super.setParent(p);
         if (getCellRendererPane() != null)
             getCellRendererPane().setParent(p);
     }
 
     protected void setParentFrame(SFrame f) {
         super.setParentFrame(f);
         if (getCellRendererPane() != null)
             getCellRendererPane().setParentFrame(f);
     }
 
 
     // do not initalize with null!
     private SCellRendererPane cellRendererPane = new SCellRendererPane();
 
 
     public SCellRendererPane getCellRendererPane() {
         return cellRendererPane;
     }
 
 
     public void removeCellRendererPane() {
         cellRendererPane.setParent(null);
         cellRendererPane = null;
     }
 
     public void setCG(ListCG cg) {
         super.setCG(cg);
     }
 
     public String getToggleSelectionParameter(int index) {
         return isSelectedIndex(index) ? getDeselectionParameter(index) :
                 getSelectionParameter(index);
     }
 
     public String getSelectionParameter(int index) {
         return Integer.toString(index);
     }
 
     public String getDeselectionParameter(int index) {
         return Integer.toString(index); 
     }
 
     // Changes to the model should force a reload.
     public void contentsChanged(javax.swing.event.ListDataEvent e) {
         fireViewportChanged(false);
         reload();
     }
 
     public void intervalAdded(javax.swing.event.ListDataEvent e) {
         fireViewportChanged(false);
         reload();
     }
 
     public void intervalRemoved(javax.swing.event.ListDataEvent e) {
         fireViewportChanged(false);
         reload();
     }
 
     /**
      * Drag and Drop stuff
      */
     private SDropMode dropMode = null;
     private boolean dragEnabled = false;
 
     protected void createActionMap() {
         ActionMap map = getActionMap();
 
         map.put(STransferHandler.getCutAction().getValue(Action.NAME), STransferHandler.getCutAction());
         map.put(STransferHandler.getCopyAction().getValue(Action.NAME), STransferHandler.getCopyAction());
         map.put(STransferHandler.getPasteAction().getValue(Action.NAME), STransferHandler.getPasteAction());
     }
 
     public static final class DropLocation extends STransferHandler.DropLocation {
         private int index;
 
         public DropLocation(SList list, SPoint point) {
             super(point);
 
             try {
                 index = Integer.parseInt(point.getCoordinates());
                 Rectangle currentViewport = list.getViewportSize();
                 if(currentViewport != null)
                     index += currentViewport.y;
             } catch(Exception e) {
                 index = 0;
             }
         }
 
         public int getIndex() {
             return index;
         }
     }
 
     public void setDropMode(SDropMode dropMode) {
         this.dropMode = dropMode;
         
         getSession().getSDragAndDropManager().addDropTarget(this);
     }
 
     public SDropMode getDropMode() {
         return this.dropMode;
     }
 
     protected DropLocation dropLocationForPoint(SPoint p) {
         if(p.getCoordinates() == null)
             return null;
         return new SList.DropLocation(this, p);
     }
 
     private void installTransferHandler() {
         if(getTransferHandler() == null) {
             setTransferHandler(new DefaultTransferHandler());
         }
     }
     
     public void setDragEnabled(boolean dragEnabled) {
         if(getSelectionModel() == null && dragEnabled == true)
             throw new IllegalStateException("Unable to enable DND - no selection mode set in " + this);
         
         if(getShowAsFormComponent() && dragEnabled == true) {
             LOG.warn("NOTE: setDragEnabled(true) called when getShowAsFormComponent was false - dragging won't work in internet explorer");
             return;
         }
 
         if(dragEnabled != this.dragEnabled) {
             if(dragEnabled) {
                 this.getSession().getSDragAndDropManager().addDragSource(this);
             } else {
                 this.getSession().getSDragAndDropManager().removeDragSource(this);
             }
             
             this.dragEnabled = dragEnabled;
         }
     }
 
     public static class DefaultTransferHandler extends STransferHandler implements CustomDragHandler {
         public DefaultTransferHandler() {
             super(null);
         }
 
         protected Transferable createTransferable(SComponent component) {
             SList list = (SList)component;
             String htmlData = "<html><body><ul>";
             String plainTextData = "";
             for(Object obj:list.getSelectedValues()) {
                 plainTextData += obj.toString() + "\n";
                 htmlData += "<li>" + obj.toString() + "</li>";
             }
             
             htmlData += "</ul></body></html>";
             return new TextAndHTMLTransferable(plainTextData, htmlData);
         }
 
         public int getSourceActions(SComponent component) {
             return COPY;
         }
 
         public boolean dragStart(SComponent source, SComponent target, int action, SMouseEvent event) {
             try {
                 String[] coords = event.getPoint().getCoordinates().split(":");
                 int index = Integer.parseInt(coords[0]);
                 if(coords.length < 3)
                     return false;
 
                 boolean ctrlKey = false;
                 boolean shiftKey = false;
                 for(int i=1; i<coords.length; ++i) {
                     String[] keyVal = coords[i].split("=");
                     if("ctrlKey".equals(keyVal[0])) {
                         ctrlKey = Boolean.parseBoolean(keyVal[1]);
                     } else if("shiftKey".equals(keyVal[0])) {
                         shiftKey = Boolean.parseBoolean(keyVal[1]);
                     }
                 }
 
 
                 if(index != -1) {
                     if(source instanceof SList) {
                         if(source.getShowAsFormComponent())
                             return false;
                         SList list = (SList)source;
                         
                         Rectangle currentViewport = list.getViewportSize();
                         if(currentViewport != null)
                             index += currentViewport.y;
 
                         if(list.isSelectedIndex(index))
                             return false;
 
                         list.addSelectionEvent(index, ctrlKey, shiftKey);
                     }
                 }
             } catch(Exception e) {
 
             }
             return false;
         }
     }
 }
