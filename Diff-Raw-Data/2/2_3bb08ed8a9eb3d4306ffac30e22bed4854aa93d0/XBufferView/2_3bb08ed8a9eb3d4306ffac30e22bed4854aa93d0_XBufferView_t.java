 /*
  * Ext GWT 2.1.3 - Ext for GWT
  * Copyright(c) 2007-2010, Ext JS, LLC.
  * licensing@extjs.com
  *
  * http://extjs.com/license
  */
 package com.extjs.gxt.ui.client.widget.grid;
 
 import com.extjs.gxt.ui.client.GXT;
 import com.extjs.gxt.ui.client.data.ModelData;
 import com.extjs.gxt.ui.client.event.BaseEvent;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.store.ListStore;
 import com.extjs.gxt.ui.client.store.Record;
 import com.extjs.gxt.ui.client.util.DelayedTask;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.dom.client.NodeList;
 import com.google.gwt.user.client.ui.Widget;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Renders the rows as they scroll into view. This GridView is fast for
  * displaying many rows at once, but it does not support all features the normal
  * {link @GridView} supports, such has expanding rows.
  *
  * <p />
  * Only works with constant row heights that can be specified using
  * {@link #setRowHeight(int)}.
  */
 public class XBufferView extends GridView {
 
   private int rowHeight = 19;
   private int cacheSize = 20;
   private int cleanDelay = 500;
   private DelayedTask cleanTask;
   private DelayedTask renderTask;
   private int scrollDelay = 0;
   private boolean bufferEnabled = true;
 
   /**
    * Returns the amount of rows that should be cached.
    *
    * @return the cache size
    */
   public int getCacheSize() {
     return cacheSize;
   }
 
   /**
    * Returns the amount of time before cleaning is done.
    *
    * @return the clean delay
    */
   public int getCleanDelay() {
     return cleanDelay;
   }
 
   /**
    * Returns the height of one row.
    *
    * @return the height of one row
    */
   public int getRowHeight() {
     return rowHeight;
   }
 
   /**
    * Returns the amount of time before new rows are displayed after scrolling
    *
    * @return the scroll delay
    */
   public int getScrollDelay() {
     return scrollDelay;
   }
 
   /**
    * Returns true if buffering is enabled.
    *
    * @return true for buffering
    */
   public boolean isBufferEnabled() {
     return bufferEnabled;
   }
 
   @Override
   public void layout() {
     super.layout();
     update();
   }
 
   /**
    * True to enabled buffered functionality (defaults to true).
    *
    * @param bufferEnabled true to buffer, otherwise false
    */
   public void setBufferEnabled(boolean bufferEnabled) {
     this.bufferEnabled = bufferEnabled;
   }
 
   /**
    * Sets the amount of rows that should be cached (default to 20).
    *
    * @param cacheSize the new cache size
    */
   public void setCacheSize(int cacheSize) {
     this.cacheSize = cacheSize;
   }
 
   /**
    * Sets the amount of time before cleaning is done (defaults to 500).
    *
    * @param cleanDelay the new clean delay
    */
   public void setCleanDelay(int cleanDelay) {
     this.cleanDelay = cleanDelay;
   }
 
   /**
    * Sets the height of one row (defaults to 19).
    *
    * @param rowHeight the new row height.
    */
   public void setRowHeight(int rowHeight) {
     this.rowHeight = rowHeight;
   }
 
   /**
    * Sets the amount of time before new rows are displayed after scrolling
    * (defaults to 0).
    *
    * @param scrollDelay the new scroll delay.
    */
   public void setScrollDelay(int scrollDelay) {
     this.scrollDelay = scrollDelay;
   }
 
   // a buffered method to clean rows
   protected void clean() {
     if (cleanTask == null) {
       cleanTask = new DelayedTask(new Listener<BaseEvent>() {
         public void handleEvent(BaseEvent be) {
           doClean();
         }
       });
     }
     cleanTask.delay(cleanDelay);
   }
 
   @Override
   protected void doAttach() {
     super.doAttach();
     update();
   }
 
   protected void doClean() {
     int count = getVisibleRowCount();
     if (count > 0) {
       int[] vr = getVisibleRows(count);
       vr[0] -= cacheSize;
       vr[1] += cacheSize;
 
       int i = 0;
       NodeList<Element> rows = getRows();
       // if first is less than 0, all rows have been rendered
       // so lets clean the end...
       if (vr[0] <= 0) {
         i = vr[1] + 1;
       }
       for (int len = grid.getStore().getCount(); i < len; i++) {
         // if current row is outside of first and last and
         // has content, update the innerHTML to nothing
         if ((i < vr[0] || i > vr[1])) {
           detachWidget(i, false);
           widgetList.set(i, null);
           rows.getItem(i).setInnerHTML("");
         }
       }
     }
   }
 
   @Override
   protected String doRender(List<ColumnData> cs, List<ModelData> rows, int startRow, int colCount, boolean stripe) {
     if (!bufferEnabled) {
       return super.doRender(cs, rows, startRow, colCount, stripe);
     }
     return doRender(cs, rows, startRow, colCount, stripe, false);
   }
 
   protected String doRender(List<ColumnData> cs, List<ModelData> rows, int startRow, int colCount, boolean stripe,
       boolean onlyBody) {
 
     int last = colCount - 1;
 
     int rowBodyColSpanCount = colCount;
     if (enableRowBody) {
       if (grid.getSelectionModel() instanceof CheckBoxSelectionModel<?>) {
         CheckBoxSelectionModel<?> sm = (CheckBoxSelectionModel<?>) grid.getSelectionModel();
         if (cm.getColumnById(sm.getColumn().getId()) != null) {
           rowBodyColSpanCount--;
         }
       }
       for (ColumnConfig c : cm.getColumns()) {
         if (c instanceof RowExpander || c instanceof RowNumberer) {
           rowBodyColSpanCount--;
         }
       }
     }
 
     int rh = getStyleRowHeight();
     int[] vr = getVisibleRows(getVisibleRowCount());
     String tstyle = "width:" + getTotalWidth() + "px;height:" + rh + "px;";
     // buffers
     StringBuilder buf = new StringBuilder();
     StringBuilder cb = new StringBuilder();
     for (int j = 0, len = rows.size(); j < len; j++) {
       ModelData model = (ModelData) rows.get(j);
 
       model = prepareData(model);
 
       Record r = ds.hasRecord(model) ? ds.getRecord(model) : null;
       int rowIndex = (j + startRow);
       boolean visible = rowIndex >= vr[0] && rowIndex <= vr[1];
       if (!onlyBody) {
         widgetList.add(rowIndex, new ArrayList<Widget>());
       }
       if (visible) {
         for (int i = 0; i < colCount; i++) {
           ColumnData c = cs.get(i);
           c.css = c.css == null ? "" : c.css;
           String rv = getRenderedValue(c, rowIndex, i, model, c.name);
 
           String css = (i == 0 ? "x-grid-cell-first " : (i == last ? "x-grid3-cell-last " : " ")) + " "
               + (c.css == null ? "" : c.css);
           String attr = c.cellAttr != null ? c.cellAttr : "";
           String cellAttr = c.cellAttr != null ? c.cellAttr : "";
 
           if (isShowInvalidCells() && r != null && !r.isValid(c.id)) {
             buf.append(" x-grid3-invalid-cell");
           }
           if (isShowDirtyCells() && r != null && r.getChanges().containsKey(c.id)) {
             css += " x-grid3-dirty-cell";
           }
 
           cb.append("<td class=\"x-grid3-col x-grid3-cell x-grid3-td-");
           cb.append(c.id);
           cb.append(" ");
           cb.append(css);
           cb.append("\" style=\"");
           cb.append(c.style);
           cb.append("\" tabIndex=0 ");
           cb.append(cellAttr);
           cb.append("><div unselectable=\"on\" class=\"x-grid3-cell-inner x-grid3-col-");
           cb.append(c.id);
           cb.append("\" ");
           cb.append(attr);
           cb.append(">");
           cb.append(rv);
           cb.append("</div></td>");
 
         }
       }
       String alt = "";
       if (stripe && ((rowIndex + 1) % 2 == 0)) {
         alt += " x-grid3-row-alt";
       }
 
       if (isShowDirtyCells() && r != null && r.isDirty()) {
         alt += " x-grid3-dirty-row";
       }
 
       if (viewConfig != null) {
         alt += " " + viewConfig.getRowStyle(model, rowIndex, ds);
       }
 
       if (visible) {
 
         if (!onlyBody) {
           buf.append("<div class=\"x-grid3-row ");
           buf.append(alt);
           buf.append("\" style=\"");
           buf.append(tstyle);
           buf.append("\">");
         }
         buf.append("<table class=x-grid3-row-table border=0 cellspacing=0 cellpadding=0 style=\"");
         buf.append(tstyle);
         buf.append("\"><tbody><tr>");
         buf.append(cb.toString());
         buf.append("</tr>");
         if (enableRowBody) {
           buf.append("<tr class=x-grid3-row-body-tr style=\"\"><td colspan=");
           buf.append(rowBodyColSpanCount);
           buf.append(" class=x-grid3-body-cell tabIndex=0><div class=x-grid3-row-body>${body}</div></td></tr>");
         }
         buf.append("</tbody></table>");
         if (!onlyBody) {
           buf.append("</div>");
         }
 
       } else {
         buf.append("<div class=\"x-grid3-row ");
         buf.append(alt);
         buf.append("\" style=\"");
         buf.append(tstyle);
         buf.append("\"></div>");
       }
 
       cb = new StringBuilder();
     }
     return buf.toString();
   }
 
   protected void doUpdate() {
     int count = getVisibleRowCount();
     if (count > 0) {
       ColumnModel cm = grid.getColumnModel();
 
       ListStore<ModelData> store = grid.getStore();
       List<ColumnData> cs = getColumnData();
       boolean stripe = grid.isStripeRows();
       int[] vr = getVisibleRows(count);
       int cc = cm.getColumnCount();
       for (int i = vr[0]; i <= vr[1]; i++) {
         // if row is NOT rendered and is visible, render it
         if (!isRowRendered(i)) {
           List<ModelData> list = new ArrayList<ModelData>();
           list.add(store.getAt(i));
           widgetList.add(i, new ArrayList<Widget>());
           String html = doRender(cs, list, i, cc, stripe, true);
           getRow(i).setInnerHTML(html);
           renderWidgets(i, i);
         }
       }
       clean();
     }
   }
 
   protected int getCalculatedRowHeight() {
     return rowHeight + borderWidth;
   }
 
   protected int getStyleRowHeight() {
     return rowHeight + (GXT.isBorderBox ? borderWidth : 0);
   }
 
   protected int getVisibleRowCount() {
     int rh = getCalculatedRowHeight();
     int visibleHeight = scroller.getHeight(true);
     return (int) ((visibleHeight < 1) ? 0 : Math.ceil(((double) visibleHeight / rh)));
   }
 
   protected int[] getVisibleRows(int count) {
     int sc = scroller.getScrollTop();
     int start = (int) (sc == 0 ? 0 : Math.floor(sc / getCalculatedRowHeight()) - 1);
     int first = Math.max(start, 0);
     //int last = Math.min(start + count + 2, grid.getStore().getCount() - 1);
    int last = (start == 0) ? grid.getStore().getCount() - 1 : Math.min(start + count + 2, grid.getStore().getCount() - 1);
     return new int[] {first, last};
   }
 
   protected boolean isRowRendered(int index) {
     Element row = getRow(index);
     return row != null && row.hasChildNodes();
   }
 
   @Override
   protected void notifyShow() {
     super.notifyShow();
     update();
   }
 
   @Override
   protected void onAdd(ListStore<ModelData> store, List<ModelData> models, int index) {
     super.onAdd(store, models, index);
     clean();
   }
 
   @Override
   protected void onRemove(ListStore<ModelData> ds, ModelData m, int index, boolean isUpdate) {
     super.onRemove(ds, m, index, isUpdate);
     update();
   }
 
   @Override
   protected void syncScroll() {
     super.syncScroll();
     update();
   }
 
   protected void update() {
     if (grid == null || !grid.isViewReady() || !bufferEnabled) {
       return;
     }
     if (renderTask == null) {
       renderTask = new DelayedTask(new Listener<BaseEvent>() {
         public void handleEvent(BaseEvent be) {
           doUpdate();
         }
       });
     }
     renderTask.delay(scrollDelay);
 
   }
 }
