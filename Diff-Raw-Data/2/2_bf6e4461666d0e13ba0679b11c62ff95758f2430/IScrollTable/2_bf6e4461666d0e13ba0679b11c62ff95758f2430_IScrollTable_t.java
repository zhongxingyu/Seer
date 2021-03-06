 /* 
 @ITMillApache2LicenseForJavaFiles@
  */
 
 package com.itmill.toolkit.terminal.gwt.client.ui;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.Vector;
 
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.DeferredCommand;
 import com.google.gwt.user.client.Element;
 import com.google.gwt.user.client.Event;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.Panel;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.ScrollListener;
 import com.google.gwt.user.client.ui.ScrollPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.itmill.toolkit.terminal.gwt.client.ApplicationConnection;
 import com.itmill.toolkit.terminal.gwt.client.ContainerResizedListener;
 import com.itmill.toolkit.terminal.gwt.client.Paintable;
 import com.itmill.toolkit.terminal.gwt.client.UIDL;
 import com.itmill.toolkit.terminal.gwt.client.Util;
 import com.itmill.toolkit.terminal.gwt.client.ui.IScrollTable.IScrollTableBody.IScrollTableRow;
 
 /**
  * IScrollTable
  * 
  * IScrollTable is a FlowPanel having two widgets in it: * TableHead component *
  * ScrollPanel
  * 
  * TableHead contains table's header and widgets + logic for resizing,
  * reordering and hiding columns.
  * 
  * ScrollPanel contains IScrollTableBody object which handles content. To save
  * some bandwidth and to improve clients responsiveness with loads of data, in
  * IScrollTableBody all rows are not necessary rendered. There are "spacers" in
  * IScrollTableBody to use the exact same space as non-rendered rows would use.
  * This way we can use seamlessly traditional scrollbars and scrolling to fetch
  * more rows instead of "paging".
  * 
  * In IScrollTable we listen to scroll events. On horizontal scrolling we also
  * update TableHeads scroll position which has its scrollbars hidden. On
  * vertical scroll events we will check if we are reaching the end of area where
  * we have rows rendered and
  * 
  * TODO implement unregistering for child components in Cells
  */
 public class IScrollTable extends Composite implements Table, ScrollListener,
         ContainerResizedListener {
 
     public static final String CLASSNAME = "i-table";
     /**
      * multiple of pagelenght which component will cache when requesting more
      * rows
      */
     private static final double CACHE_RATE = 2;
     /**
      * fraction of pageLenght which can be scrolled without making new request
      */
     private static final double CACHE_REACT_RATE = 1.5;
 
     public static final char ALIGN_CENTER = 'c';
     public static final char ALIGN_LEFT = 'b';
     public static final char ALIGN_RIGHT = 'e';
     private int firstRowInViewPort = 0;
     private int pageLength = 15;
     private int lastRequestedFirstvisible = 0; // to detect "serverside scroll"
 
     private boolean showRowHeaders = false;
 
     private String[] columnOrder;
 
     private ApplicationConnection client;
     private String paintableId;
 
     private boolean immediate;
 
     private int selectMode = Table.SELECT_MODE_NONE;
 
     private final HashSet selectedRowKeys = new HashSet();
 
     private boolean initializedAndAttached = false;
 
     private final TableHead tHead = new TableHead();
 
     private final ScrollPanel bodyContainer = new ScrollPanel();
 
     private int totalRows;
 
     private Set collapsedColumns;
 
     private final RowRequestHandler rowRequestHandler;
     private IScrollTableBody tBody;
     private String width;
     private String height;
     private int firstvisible = 0;
     private boolean sortAscending;
     private String sortColumn;
     private boolean columnReordering;
 
     /**
      * This map contains captions and icon urls for actions like: * "33_c" ->
      * "Edit" * "33_i" -> "http://dom.com/edit.png"
      */
     private final HashMap actionMap = new HashMap();
     private String[] visibleColOrder;
     private boolean initialContentReceived = false;
     private Element scrollPositionElement;
     private final FlowPanel panel;
     private boolean enabled;
     private boolean showColHeaders;
 
     /** flag to indicate that table body has changed */
     private boolean isNewBody = true;
 
     /**
      * Stores old height for IE, that sometimes fails to return correct height
      * for container element. Then this value is used as a fallback.
      */
     private int oldAvailPixels;
 
     public IScrollTable() {
 
         bodyContainer.addScrollListener(this);
         bodyContainer.setStyleName(CLASSNAME + "-body");
 
         panel = new FlowPanel();
         panel.setStyleName(CLASSNAME);
         panel.add(tHead);
         panel.add(bodyContainer);
 
         rowRequestHandler = new RowRequestHandler();
 
         initWidget(panel);
     }
 
     public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
         if (client.updateComponent(this, uidl, true)) {
             return;
         }
 
         enabled = !uidl.hasAttribute("disabled");
 
         this.client = client;
         paintableId = uidl.getStringAttribute("id");
         immediate = uidl.getBooleanAttribute("immediate");
         final int newTotalRows = uidl.getIntAttribute("totalrows");
         if (newTotalRows != totalRows) {
             totalRows = newTotalRows;
             if (tBody != null) {
                 initializedAndAttached = false;
                 initialContentReceived = false;
                 isNewBody = true;
             }
         }
 
         pageLength = uidl.getIntAttribute("pagelength");
         if (pageLength == 0) {
             pageLength = totalRows;
         }
         firstvisible = uidl.hasVariable("firstvisible") ? uidl
                 .getIntVariable("firstvisible") : 0;
        if (firstvisible != lastRequestedFirstvisible && tBody != null) {
             // received 'surprising' firstvisible from server: scroll there
             firstRowInViewPort = firstvisible;
             bodyContainer
                     .setScrollPosition(firstvisible * tBody.getRowHeight());
         }
 
         showRowHeaders = uidl.getBooleanAttribute("rowheaders");
         showColHeaders = uidl.getBooleanAttribute("colheaders");
 
         if (uidl.hasAttribute("width")) {
             width = uidl.getStringAttribute("width");
         }
         if (uidl.hasAttribute("height")) {
             height = uidl.getStringAttribute("height");
         }
 
         if (uidl.hasVariable("sortascending")) {
             sortAscending = uidl.getBooleanVariable("sortascending");
             sortColumn = uidl.getStringVariable("sortcolumn");
         }
 
         if (uidl.hasVariable("selected")) {
             final Set selectedKeys = uidl
                     .getStringArrayVariableAsSet("selected");
             selectedRowKeys.clear();
             for (final Iterator it = selectedKeys.iterator(); it.hasNext();) {
                 selectedRowKeys.add(it.next());
             }
         }
 
         if (uidl.hasAttribute("selectmode")) {
             if (uidl.getBooleanAttribute("readonly")) {
                 selectMode = Table.SELECT_MODE_NONE;
             } else if (uidl.getStringAttribute("selectmode").equals("multi")) {
                 selectMode = Table.SELECT_MODE_MULTI;
             } else if (uidl.getStringAttribute("selectmode").equals("single")) {
                 selectMode = Table.SELECT_MODE_SINGLE;
             } else {
                 selectMode = Table.SELECT_MODE_NONE;
             }
         }
 
         if (uidl.hasVariable("columnorder")) {
             columnReordering = true;
             columnOrder = uidl.getStringArrayVariable("columnorder");
         }
 
         if (uidl.hasVariable("collapsedcolumns")) {
             tHead.setColumnCollapsingAllowed(true);
             collapsedColumns = uidl
                     .getStringArrayVariableAsSet("collapsedcolumns");
         } else {
             tHead.setColumnCollapsingAllowed(false);
         }
 
         UIDL rowData = null;
         for (final Iterator it = uidl.getChildIterator(); it.hasNext();) {
             final UIDL c = (UIDL) it.next();
             if (c.getTag().equals("rows")) {
                 rowData = c;
             } else if (c.getTag().equals("actions")) {
                 updateActionMap(c);
             } else if (c.getTag().equals("visiblecolumns")) {
                 tHead.updateCellsFromUIDL(c);
             }
         }
         updateHeader(uidl.getStringArrayAttribute("vcolorder"));
 
         if (initializedAndAttached) {
             updateBody(rowData, uidl.getIntAttribute("firstrow"), uidl
                     .getIntAttribute("rows"));
         } else {
             if (tBody != null) {
                 tBody.removeFromParent();
                 client.unregisterChildPaintables(tBody);
             }
             tBody = new IScrollTableBody();
 
             tBody.renderInitialRows(rowData, uidl.getIntAttribute("firstrow"),
                     uidl.getIntAttribute("rows"));
             bodyContainer.add(tBody);
             initialContentReceived = true;
             if (isAttached()) {
                 sizeInit();
             }
         }
         hideScrollPositionAnnotation();
     }
 
     private void updateActionMap(UIDL c) {
         final Iterator it = c.getChildIterator();
         while (it.hasNext()) {
             final UIDL action = (UIDL) it.next();
             final String key = action.getStringAttribute("key");
             final String caption = action.getStringAttribute("caption");
             actionMap.put(key + "_c", caption);
             if (action.hasAttribute("icon")) {
                 // TODO need some uri handling ??
                 actionMap.put(key + "_i", client.translateToolkitUri(action
                         .getStringAttribute("icon")));
             }
         }
 
     }
 
     public String getActionCaption(String actionKey) {
         return (String) actionMap.get(actionKey + "_c");
     }
 
     public String getActionIcon(String actionKey) {
         return (String) actionMap.get(actionKey + "_i");
     }
 
     private void updateHeader(String[] strings) {
         if (strings == null) {
             return;
         }
 
         int visibleCols = strings.length;
         int colIndex = 0;
         if (showRowHeaders) {
             tHead.enableColumn("0", colIndex);
             visibleCols++;
             visibleColOrder = new String[visibleCols];
             visibleColOrder[colIndex] = "0";
             colIndex++;
         } else {
             visibleColOrder = new String[visibleCols];
             tHead.removeCell("0");
         }
 
         int i;
         for (i = 0; i < strings.length; i++) {
             final String cid = strings[i];
             visibleColOrder[colIndex] = cid;
             tHead.enableColumn(cid, colIndex);
             colIndex++;
         }
 
         tHead.setVisible(showColHeaders);
 
     }
 
     /**
      * @param uidl
      *                which contains row data
      * @param firstRow
      *                first row in data set
      * @param reqRows
      *                amount of rows in data set
      */
     private void updateBody(UIDL uidl, int firstRow, int reqRows) {
         if (uidl == null || reqRows < 1) {
             // container is empty, remove possibly existing rows
             if (firstRow < 0) {
                 while (tBody.getLastRendered() > tBody.firstRendered) {
                     tBody.unlinkRow(false);
                 }
                 tBody.unlinkRow(false);
             }
             return;
         }
 
         tBody.renderRows(uidl, firstRow, reqRows);
 
         final int optimalFirstRow = (int) (firstRowInViewPort - pageLength
                 * CACHE_RATE);
         boolean cont = true;
         while (cont && tBody.getLastRendered() > optimalFirstRow
                 && tBody.getFirstRendered() < optimalFirstRow) {
             // client.console.log("removing row from start");
             cont = tBody.unlinkRow(true);
         }
         final int optimalLastRow = (int) (firstRowInViewPort + pageLength + pageLength
                 * CACHE_RATE);
         cont = true;
         while (cont && tBody.getLastRendered() > optimalLastRow) {
             // client.console.log("removing row from the end");
             cont = tBody.unlinkRow(false);
         }
         tBody.fixSpacers();
 
     }
 
     /**
      * Gives correct column index for given column key ("cid" in UIDL).
      * 
      * @param colKey
      * @return column index of visible columns, -1 if column not visible
      */
     private int getColIndexByKey(String colKey) {
         // return 0 if asked for rowHeaders
         if ("0".equals(colKey)) {
             return 0;
         }
         for (int i = 0; i < visibleColOrder.length; i++) {
             if (visibleColOrder[i].equals(colKey)) {
                 return i;
             }
         }
         return -1;
     }
 
     private boolean isCollapsedColumn(String colKey) {
         if (collapsedColumns == null) {
             return false;
         }
         if (collapsedColumns.contains(colKey)) {
             return true;
         }
         return false;
     }
 
     private String getColKeyByIndex(int index) {
         return tHead.getHeaderCell(index).getColKey();
     }
 
     private void setColWidth(int colIndex, int w) {
         final HeaderCell cell = tHead.getHeaderCell(colIndex);
         cell.setWidth(w);
         tBody.setColWidth(colIndex, w);
     }
 
     private int getColWidth(String colKey) {
         return tHead.getHeaderCell(colKey).getWidth();
     }
 
     private IScrollTableRow getRenderedRowByKey(String key) {
         final Iterator it = tBody.iterator();
         IScrollTableRow r = null;
         while (it.hasNext()) {
             r = (IScrollTableRow) it.next();
             if (r.getKey().equals(key)) {
                 return r;
             }
         }
         return null;
     }
 
     private void reOrderColumn(String columnKey, int newIndex) {
 
         final int oldIndex = getColIndexByKey(columnKey);
 
         // Change header order
         tHead.moveCell(oldIndex, newIndex);
 
         // Change body order
         tBody.moveCol(oldIndex, newIndex);
 
         /*
          * Build new columnOrder and update it to server Note that columnOrder
          * also contains collapsed columns so we cannot directly build it from
          * cells vector Loop the old columnOrder and append in order to new
          * array unless on moved columnKey. On new index also put the moved key
          * i == index on columnOrder, j == index on newOrder
          */
         final String oldKeyOnNewIndex = visibleColOrder[newIndex];
         if (showRowHeaders) {
             newIndex--; // columnOrder don't have rowHeader
         }
         // add back hidden rows,
         for (int i = 0; i < columnOrder.length; i++) {
             if (columnOrder[i].equals(oldKeyOnNewIndex)) {
                 break; // break loop at target
             }
             if (isCollapsedColumn(columnOrder[i])) {
                 newIndex++;
             }
         }
         // finally we can build the new columnOrder for server
         final String[] newOrder = new String[columnOrder.length];
         for (int i = 0, j = 0; j < newOrder.length; i++) {
             if (j == newIndex) {
                 newOrder[j] = columnKey;
                 j++;
             }
             if (i == columnOrder.length) {
                 break;
             }
             if (columnOrder[i].equals(columnKey)) {
                 continue;
             }
             newOrder[j] = columnOrder[i];
             j++;
         }
         columnOrder = newOrder;
         // also update visibleColumnOrder
         int i = showRowHeaders ? 1 : 0;
         for (int j = 0; j < newOrder.length; j++) {
             final String cid = newOrder[j];
             if (!isCollapsedColumn(cid)) {
                 visibleColOrder[i++] = cid;
             }
         }
         client.updateVariable(paintableId, "columnorder", columnOrder, false);
     }
 
     protected void onAttach() {
         super.onAttach();
         if (initialContentReceived) {
             sizeInit();
         }
     }
 
     protected void onDetach() {
         rowRequestHandler.cancel();
         super.onDetach();
         // ensure that scrollPosElement will be detached
         if (scrollPositionElement != null) {
             final Element parent = DOM.getParent(scrollPositionElement);
             if (parent != null) {
                 DOM.removeChild(parent, scrollPositionElement);
             }
         }
     }
 
     /**
      * Run only once when component is attached and received its initial
      * content. This function : * Syncs headers and bodys "natural widths and
      * saves the values. * Sets proper width and height * Makes deferred request
      * to get some cache rows
      */
     private void sizeInit() {
         /*
          * We will use browsers table rendering algorithm to find proper column
          * widths. If content and header take less space than available, we will
          * divide extra space relatively to each column which has not width set.
          * 
          * Overflow pixels are added to last column.
          * 
          */
 
         Iterator headCells = tHead.iterator();
         int i = 0;
         int totalExplicitColumnsWidths = 0;
         int total = 0;
 
         final int[] widths = new int[tHead.visibleCells.size()];
 
         if (width == null) {
             // if this is a re-init, remove old manually fixed size
             bodyContainer.setWidth("");
             tHead.setWidth("");
             super.setWidth("");
         }
 
         tHead.enableBrowserIntelligence();
         // first loop: collect natural widths
         while (headCells.hasNext()) {
             final HeaderCell hCell = (HeaderCell) headCells.next();
             int w = hCell.getWidth();
             if (w > 0) {
                 // server has defined column width explicitly
                 totalExplicitColumnsWidths += w;
             } else {
                 final int hw = hCell.getOffsetWidth();
                 final int cw = tBody.getColWidth(i);
                 w = (hw > cw ? hw : cw) + IScrollTableBody.CELL_EXTRA_WIDTH;
             }
             widths[i] = w;
             total += w;
             i++;
         }
 
         tHead.disableBrowserIntelligence();
 
         if (height == null) {
             bodyContainer.setHeight((tBody.getRowHeight() * pageLength) + "px");
         } else {
             mySetHeight(height);
             iLayout();
         }
 
         if (width == null) {
             int w = total;
             w += getScrollbarWidth();
             bodyContainer.setWidth(w + "px");
             tHead.setWidth(w + "px");
             super.setWidth(w + "px");
         } else {
             if (width.indexOf("px") > 0) {
                 bodyContainer.setWidth(width);
                 tHead.setWidth(width);
                 super.setWidth(width);
             } else if (width.indexOf("%") > 0) {
                 if (!width.equals("100%")) {
                     super.setWidth(width);
                 }
                 // contained blocks are relatively to container element
                 bodyContainer.setWidth("100%");
                 tHead.setWidth("100%");
 
             }
         }
 
         int availW = tBody.getAvailableWidth();
         // Hey IE, are you really sure about this?
         availW = tBody.getAvailableWidth();
 
         if (availW > total) {
             // natural size is smaller than available space
             final int extraSpace = availW - total;
             final int totalWidthR = total - totalExplicitColumnsWidths;
             if (totalWidthR > 0) {
                 // now we will share this sum relatively to those without
                 // explicit width
                 headCells = tHead.iterator();
                 i = 0;
                 HeaderCell hCell;
                 while (headCells.hasNext()) {
                     hCell = (HeaderCell) headCells.next();
                     if (hCell.getWidth() == -1) {
                         int w = widths[i];
                         final int newSpace = extraSpace * w / totalWidthR;
                         w += newSpace;
                         widths[i] = w;
                     }
                     i++;
                 }
             }
         } else {
             // bodys size will be more than available and scrollbar will appear
         }
 
         // last loop: set possibly modified values or reset if new tBody
         i = 0;
         headCells = tHead.iterator();
         while (headCells.hasNext()) {
             final HeaderCell hCell = (HeaderCell) headCells.next();
             if (isNewBody || hCell.getWidth() == -1) {
                 final int w = widths[i];
                 setColWidth(i, w);
             }
             i++;
         }
 
         isNewBody = false;
 
         if (firstvisible > 0) {
             // Deferred due some Firefox oddities. IE & Safari could survive
             // without
             DeferredCommand.addCommand(new Command() {
                 public void execute() {
                     bodyContainer.setScrollPosition(firstvisible
                             * tBody.getRowHeight());
                     firstRowInViewPort = firstvisible;
                 }
             });
         }
 
         if (enabled) {
             // Do we need cache rows
             if (tBody.getLastRendered() + 1 < firstRowInViewPort + pageLength
                     + CACHE_REACT_RATE * pageLength) {
                 DeferredCommand.addCommand(new Command() {
                     public void execute() {
                         if (totalRows - 1 > tBody.getLastRendered()) {
                             // fetch cache rows
                             rowRequestHandler.setReqFirstRow(tBody
                                     .getLastRendered() + 1);
                             rowRequestHandler
                                     .setReqRows((int) (pageLength * CACHE_RATE));
                             rowRequestHandler.deferRowFetch(1);
                         }
                     }
                 });
             }
         }
         initializedAndAttached = true;
     }
 
     public void iLayout() {
         if (height != null) {
             if (height.equals("100%")) {
                 /*
                  * We define height in pixels with 100% not to include borders
                  * which is what users usually want. So recalculate pixels via
                  * setHeight.
                  */
                 mySetHeight(height);
             }
 
             int contentH = (DOM.getElementPropertyInt(getElement(),
                     "clientHeight") - tHead.getOffsetHeight());
             if (contentH < 0) {
                 contentH = 0;
             }
             bodyContainer.setHeight(contentH + "px");
         }
     }
 
     private int getScrollbarWidth() {
         return bodyContainer.getOffsetWidth()
                 - DOM.getElementPropertyInt(bodyContainer.getElement(),
                         "clientWidth");
     }
 
     /**
      * This method has logic which rows needs to be requested from server when
      * user scrolls
      */
     public void onScroll(Widget widget, int scrollLeft, int scrollTop) {
         if (!initializedAndAttached) {
             return;
         }
         if (!enabled) {
             bodyContainer.setScrollPosition(firstRowInViewPort
                     * tBody.getRowHeight());
             return;
         }
 
         rowRequestHandler.cancel();
 
         // fix headers horizontal scrolling
         tHead.setHorizontalScrollPosition(scrollLeft);
 
         firstRowInViewPort = (int) Math.ceil(scrollTop
                 / (double) tBody.getRowHeight());
         // ApplicationConnection.getConsole().log(
         // "At scrolltop: " + scrollTop + " At row " + firstRowInViewPort);
 
         int postLimit = (int) (firstRowInViewPort + pageLength + pageLength
                 * CACHE_REACT_RATE);
         if (postLimit > totalRows - 1) {
             postLimit = totalRows - 1;
         }
         int preLimit = (int) (firstRowInViewPort - pageLength
                 * CACHE_REACT_RATE);
         if (preLimit < 0) {
             preLimit = 0;
         }
         final int lastRendered = tBody.getLastRendered();
         final int firstRendered = tBody.getFirstRendered();
 
         if (postLimit <= lastRendered && preLimit >= firstRendered) {
             // remember which firstvisible we requested, in case the server has
             // a differing opinion
             lastRequestedFirstvisible = firstRowInViewPort;
             client.updateVariable(paintableId, "firstvisible",
                     firstRowInViewPort, false);
             return; // scrolled withing "non-react area"
         }
 
         if (firstRowInViewPort - pageLength * CACHE_RATE > lastRendered
                 || firstRowInViewPort + pageLength + pageLength * CACHE_RATE < firstRendered) {
             // need a totally new set
             // ApplicationConnection.getConsole().log(
             // "Table: need a totally new set");
             rowRequestHandler
                     .setReqFirstRow((int) (firstRowInViewPort - pageLength
                             * CACHE_RATE));
             int last = firstRowInViewPort + (int) CACHE_RATE * pageLength
                     + pageLength;
             if (last > totalRows) {
                 last = totalRows - 1;
             }
             rowRequestHandler.setReqRows(last
                     - rowRequestHandler.getReqFirstRow() + 1);
             rowRequestHandler.deferRowFetch();
             return;
         }
         if (preLimit < firstRendered) {
             // need some rows to the beginning of the rendered area
             // ApplicationConnection
             // .getConsole()
             // .log(
             // "Table: need some rows to the beginning of the rendered area");
             rowRequestHandler
                     .setReqFirstRow((int) (firstRowInViewPort - pageLength
                             * CACHE_RATE));
             rowRequestHandler.setReqRows(firstRendered
                     - rowRequestHandler.getReqFirstRow());
             rowRequestHandler.deferRowFetch();
 
             return;
         }
         if (postLimit > lastRendered) {
             // need some rows to the end of the rendered area
             // ApplicationConnection.getConsole().log(
             // "need some rows to the end of the rendered area");
             rowRequestHandler.setReqFirstRow(lastRendered + 1);
             rowRequestHandler.setReqRows((int) ((firstRowInViewPort
                     + pageLength + pageLength * CACHE_RATE) - lastRendered));
             rowRequestHandler.deferRowFetch();
         }
 
     }
 
     private void announceScrollPosition() {
         if (scrollPositionElement == null) {
             scrollPositionElement = DOM.createDiv();
             DOM.setElementProperty(scrollPositionElement, "className",
                     "i-table-scrollposition");
             DOM.appendChild(getElement(), scrollPositionElement);
         }
 
         DOM.setStyleAttribute(scrollPositionElement, "position", "absolute");
         DOM.setStyleAttribute(scrollPositionElement, "marginLeft", (DOM
                 .getElementPropertyInt(getElement(), "offsetWidth") / 2 - 80)
                 + "px");
         DOM.setStyleAttribute(scrollPositionElement, "marginTop", -(DOM
                 .getElementPropertyInt(getElement(), "offsetHeight") - 2)
                 + "px");
 
         // indexes go from 1-totalRows, as rowheaders in index-mode indicate
         int last = (firstRowInViewPort + (bodyContainer.getOffsetHeight() / tBody
                 .getRowHeight()));
         if (last > totalRows) {
             last = totalRows;
         }
         DOM.setInnerHTML(scrollPositionElement, "<span>"
                 + (firstRowInViewPort + 1) + " &ndash; " + last + "..."
                 + "</span>");
         DOM.setStyleAttribute(scrollPositionElement, "display", "block");
     }
 
     private void hideScrollPositionAnnotation() {
         if (scrollPositionElement != null) {
             DOM.setStyleAttribute(scrollPositionElement, "display", "none");
         }
     }
 
     private class RowRequestHandler extends Timer {
 
         private int reqFirstRow = 0;
         private int reqRows = 0;
 
         public void deferRowFetch() {
             deferRowFetch(250);
         }
 
         public void deferRowFetch(int msec) {
             if (reqRows > 0 && reqFirstRow < totalRows) {
                 schedule(msec);
 
                 // tell scroll position to user if currently "visible" rows are
                 // not rendered
                 if ((firstRowInViewPort + pageLength > tBody.getLastRendered())
                         || (firstRowInViewPort < tBody.getFirstRendered())) {
                     announceScrollPosition();
                 } else {
                     hideScrollPositionAnnotation();
                 }
             }
         }
 
         public void setReqFirstRow(int reqFirstRow) {
             if (reqFirstRow < 0) {
                 reqFirstRow = 0;
             } else if (reqFirstRow >= totalRows) {
                 reqFirstRow = totalRows - 1;
             }
             this.reqFirstRow = reqFirstRow;
         }
 
         public void setReqRows(int reqRows) {
             this.reqRows = reqRows;
         }
 
         public void run() {
             ApplicationConnection.getConsole().log(
                     "Getting " + reqRows + " rows from " + reqFirstRow);
 
             int firstToBeRendered = tBody.firstRendered;
             if (reqFirstRow < firstToBeRendered) {
                 firstToBeRendered = reqFirstRow;
             } else if (firstRowInViewPort - (int) (CACHE_RATE * pageLength) > firstToBeRendered) {
                 firstToBeRendered = firstRowInViewPort
                         - (int) (CACHE_RATE * pageLength);
                 if (firstToBeRendered < 0) {
                     firstToBeRendered = 0;
                 }
             }
 
             int lastToBeRendered = tBody.lastRendered;
 
             if (reqFirstRow + reqRows - 1 > lastToBeRendered) {
                 lastToBeRendered = reqFirstRow + reqRows - 1;
             } else if (firstRowInViewPort + pageLength + pageLength
                     * CACHE_RATE < lastToBeRendered) {
                 lastToBeRendered = (firstRowInViewPort + pageLength + (int) (pageLength * CACHE_RATE));
                 if (lastToBeRendered >= totalRows) {
                     lastToBeRendered = totalRows - 1;
                 }
             }
 
             client.updateVariable(paintableId, "firstToBeRendered",
                     firstToBeRendered, false);
 
             client.updateVariable(paintableId, "lastToBeRendered",
                     lastToBeRendered, false);
             // remember which firstvisible we requested, in case the server has
             // a differing opinion
             lastRequestedFirstvisible = firstRowInViewPort;
             client.updateVariable(paintableId, "firstvisible",
                     firstRowInViewPort, false);
             client.updateVariable(paintableId, "reqfirstrow", reqFirstRow,
                     false);
             client.updateVariable(paintableId, "reqrows", reqRows, true);
         }
 
         public int getReqFirstRow() {
             return reqFirstRow;
         }
 
         public int getReqRows() {
             return reqRows;
         }
 
         /**
          * Sends request to refresh content at this position.
          */
         public void refreshContent() {
             int first = (int) (firstRowInViewPort - pageLength * CACHE_RATE);
             int reqRows = (int) (2 * pageLength * CACHE_RATE + pageLength);
             if (first < 0) {
                 reqRows = reqRows + first;
                 first = 0;
             }
             setReqFirstRow(first);
             setReqRows(reqRows);
             run();
         }
     }
 
     public class HeaderCell extends Widget {
 
         private static final int DRAG_WIDGET_WIDTH = 4;
 
         private static final int MINIMUM_COL_WIDTH = 20;
 
         Element td = DOM.createTD();
 
         Element captionContainer = DOM.createDiv();
 
         Element colResizeWidget = DOM.createDiv();
 
         Element floatingCopyOfHeaderCell;
 
         private boolean sortable = false;
         private final String cid;
         private boolean dragging;
 
         private int dragStartX;
         private int colIndex;
         private int originalWidth;
 
         private boolean isResizing;
 
         private int headerX;
 
         private boolean moved;
 
         private int closestSlot;
 
         private int width = -1;
 
         private char align = ALIGN_LEFT;
 
         public void setSortable(boolean b) {
             sortable = b;
         }
 
         public HeaderCell(String colId, String headerText) {
             cid = colId;
 
             DOM.setElementProperty(colResizeWidget, "className", CLASSNAME
                     + "-resizer");
             DOM.setStyleAttribute(colResizeWidget, "width", DRAG_WIDGET_WIDTH
                     + "px");
             DOM.sinkEvents(colResizeWidget, Event.MOUSEEVENTS);
 
             setText(headerText);
 
             DOM.appendChild(td, colResizeWidget);
 
             DOM.setElementProperty(captionContainer, "className", CLASSNAME
                     + "-caption-container");
 
             // ensure no clipping initially (problem on column additions)
             DOM.setStyleAttribute(captionContainer, "overflow", "visible");
 
             DOM.sinkEvents(captionContainer, Event.MOUSEEVENTS);
 
             DOM.appendChild(td, captionContainer);
 
             DOM.sinkEvents(td, Event.MOUSEEVENTS);
 
             setElement(td);
         }
 
         public void setWidth(int w) {
             if (width == -1) {
                 // go to default mode, clip content if necessary
                 DOM.setStyleAttribute(captionContainer, "overflow", "");
             }
             width = w;
             DOM.setStyleAttribute(captionContainer, "width", (w
                     - DRAG_WIDGET_WIDTH - 4)
                     + "px");
             setWidth(w + "px");
         }
 
         public int getWidth() {
             return width;
         }
 
         public void setText(String headerText) {
             DOM.setInnerHTML(captionContainer, headerText);
         }
 
         public String getColKey() {
             return cid;
         }
 
         private void setSorted(boolean sorted) {
             if (sorted) {
                 if (sortAscending) {
                     this.setStyleName(CLASSNAME + "-header-cell-asc");
                 } else {
                     this.setStyleName(CLASSNAME + "-header-cell-desc");
                 }
             } else {
                 this.setStyleName(CLASSNAME + "-header-cell");
             }
         }
 
         /**
          * Handle column reordering.
          */
         public void onBrowserEvent(Event event) {
             if (enabled) {
                 if (isResizing
                         || DOM.compare(DOM.eventGetTarget(event),
                                 colResizeWidget)) {
                     onResizeEvent(event);
                 } else {
                     handleCaptionEvent(event);
                 }
             }
         }
 
         private void createFloatingCopy() {
             floatingCopyOfHeaderCell = DOM.createDiv();
             DOM.setInnerHTML(floatingCopyOfHeaderCell, DOM.getInnerHTML(td));
             floatingCopyOfHeaderCell = DOM
                     .getChild(floatingCopyOfHeaderCell, 1);
             DOM.setElementProperty(floatingCopyOfHeaderCell, "className",
                     CLASSNAME + "-header-drag");
             updateFloatingCopysPosition(DOM.getAbsoluteLeft(td), DOM
                     .getAbsoluteTop(td));
             DOM.appendChild(RootPanel.get().getElement(),
                     floatingCopyOfHeaderCell);
         }
 
         private void updateFloatingCopysPosition(int x, int y) {
             x -= DOM.getElementPropertyInt(floatingCopyOfHeaderCell,
                     "offsetWidth") / 2;
             DOM.setStyleAttribute(floatingCopyOfHeaderCell, "left", x + "px");
             if (y > 0) {
                 DOM.setStyleAttribute(floatingCopyOfHeaderCell, "top", (y + 7)
                         + "px");
             }
         }
 
         private void hideFloatingCopy() {
             DOM.removeChild(RootPanel.get().getElement(),
                     floatingCopyOfHeaderCell);
             floatingCopyOfHeaderCell = null;
         }
 
         protected void handleCaptionEvent(Event event) {
             switch (DOM.eventGetType(event)) {
             case Event.ONMOUSEDOWN:
                 ApplicationConnection.getConsole().log(
                         "HeaderCaption: mouse down");
                 if (columnReordering) {
                     dragging = true;
                     moved = false;
                     colIndex = getColIndexByKey(cid);
                     DOM.setCapture(getElement());
                     headerX = tHead.getAbsoluteLeft();
                     ApplicationConnection
                             .getConsole()
                             .log(
                                     "HeaderCaption: Caption set to capture mouse events");
                     DOM.eventPreventDefault(event); // prevent selecting text
                 }
                 break;
             case Event.ONMOUSEUP:
                 ApplicationConnection.getConsole()
                         .log("HeaderCaption: mouseUP");
                 if (columnReordering) {
                     dragging = false;
                     DOM.releaseCapture(getElement());
                     ApplicationConnection.getConsole().log(
                             "HeaderCaption: Stopped column reordering");
                     if (moved) {
                         hideFloatingCopy();
                         tHead.removeSlotFocus();
                         if (closestSlot != colIndex
                                 && closestSlot != (colIndex + 1)) {
                             if (closestSlot > colIndex) {
                                 reOrderColumn(cid, closestSlot - 1);
                             } else {
                                 reOrderColumn(cid, closestSlot);
                             }
                         }
                     }
                 }
 
                 if (!moved) {
                     // mouse event was a click to header -> sort column
                     if (sortable) {
                         if (sortColumn.equals(cid)) {
                             // just toggle order
                             client.updateVariable(paintableId, "sortascending",
                                     !sortAscending, false);
                         } else {
                             // set table scrolled by this column
                             client.updateVariable(paintableId, "sortcolumn",
                                     cid, false);
                         }
                         // get also cache columns at the same request
                         bodyContainer.setScrollPosition(0);
                         firstvisible = 0;
                         rowRequestHandler.setReqFirstRow(0);
                         rowRequestHandler.setReqRows((int) (2 * pageLength
                                 * CACHE_RATE + pageLength));
                         rowRequestHandler.deferRowFetch();
                     }
                     break;
                 }
                 break;
             case Event.ONMOUSEMOVE:
                 if (dragging) {
                     ApplicationConnection.getConsole().log(
                             "HeaderCaption: Dragging column, optimal index...");
                     if (!moved) {
                         createFloatingCopy();
                         moved = true;
                     }
                     final int x = DOM.eventGetClientX(event)
                             + DOM.getElementPropertyInt(tHead.hTableWrapper,
                                     "scrollLeft");
                     int slotX = headerX;
                     closestSlot = colIndex;
                     int closestDistance = -1;
                     int start = 0;
                     if (showRowHeaders) {
                         start++;
                     }
                     final int visibleCellCount = tHead.getVisibleCellCount();
                     for (int i = start; i <= visibleCellCount; i++) {
                         if (i > 0) {
                             final String colKey = getColKeyByIndex(i - 1);
                             slotX += getColWidth(colKey);
                         }
                         final int dist = Math.abs(x - slotX);
                         if (closestDistance == -1 || dist < closestDistance) {
                             closestDistance = dist;
                             closestSlot = i;
                         }
                     }
                     tHead.focusSlot(closestSlot);
 
                     updateFloatingCopysPosition(DOM.eventGetClientX(event), -1);
                     ApplicationConnection.getConsole().log("" + closestSlot);
                 }
                 break;
             default:
                 break;
             }
         }
 
         private void onResizeEvent(Event event) {
             switch (DOM.eventGetType(event)) {
             case Event.ONMOUSEDOWN:
                 isResizing = true;
                 DOM.setCapture(getElement());
                 dragStartX = DOM.eventGetClientX(event);
                 colIndex = getColIndexByKey(cid);
                 originalWidth = getWidth();
                 DOM.eventPreventDefault(event);
                 break;
             case Event.ONMOUSEUP:
                 isResizing = false;
                 DOM.releaseCapture(getElement());
                 break;
             case Event.ONMOUSEMOVE:
                 if (isResizing) {
                     final int deltaX = DOM.eventGetClientX(event) - dragStartX;
                     if (deltaX == 0) {
                         return;
                     }
 
                     int newWidth = originalWidth + deltaX;
                     if (newWidth < MINIMUM_COL_WIDTH) {
                         newWidth = MINIMUM_COL_WIDTH;
                     }
                     setColWidth(colIndex, newWidth);
                 }
                 break;
             default:
                 break;
             }
         }
 
         public String getCaption() {
             return DOM.getInnerText(captionContainer);
         }
 
         public boolean isEnabled() {
             return getParent() != null;
         }
 
         public void setAlign(char c) {
             if (align != c) {
                 switch (c) {
                 case ALIGN_CENTER:
                     DOM.setStyleAttribute(captionContainer, "textAlign",
                             "center");
                     break;
                 case ALIGN_RIGHT:
                     DOM.setStyleAttribute(captionContainer, "textAlign",
                             "right");
                     break;
                 default:
                     DOM.setStyleAttribute(captionContainer, "textAlign", "");
                     break;
                 }
             }
             align = c;
         }
 
         public char getAlign() {
             return align;
         }
 
     }
 
     /**
      * HeaderCell that is header cell for row headers.
      * 
      * Reordering disabled and clicking on it resets sorting.
      */
     public class RowHeadersHeaderCell extends HeaderCell {
 
         RowHeadersHeaderCell() {
             super("0", "");
         }
 
         protected void handleCaptionEvent(Event event) {
             // NOP: RowHeaders cannot be reordered
             // TODO It'd be nice to reset sorting here
         }
     }
 
     public class TableHead extends Panel implements ActionOwner {
 
         private static final int WRAPPER_WIDTH = 9000;
 
         Vector visibleCells = new Vector();
 
         HashMap availableCells = new HashMap();
 
         Element div = DOM.createDiv();
         Element hTableWrapper = DOM.createDiv();
         Element hTableContainer = DOM.createDiv();
         Element table = DOM.createTable();
         Element headerTableBody = DOM.createTBody();
         Element tr = DOM.createTR();
 
         private final Element columnSelector = DOM.createDiv();
 
         private int focusedSlot = -1;
 
         public TableHead() {
             DOM.setStyleAttribute(hTableWrapper, "overflow", "hidden");
             DOM.setElementProperty(hTableWrapper, "className", CLASSNAME
                     + "-header");
 
             // TODO move styles to CSS
             DOM.setElementProperty(columnSelector, "className", CLASSNAME
                     + "-column-selector");
             DOM.setStyleAttribute(columnSelector, "display", "none");
 
             DOM.appendChild(table, headerTableBody);
             DOM.appendChild(headerTableBody, tr);
             DOM.appendChild(hTableContainer, table);
             DOM.appendChild(hTableWrapper, hTableContainer);
             DOM.appendChild(div, hTableWrapper);
             DOM.appendChild(div, columnSelector);
             setElement(div);
 
             setStyleName(CLASSNAME + "-header-wrap");
 
             DOM.sinkEvents(columnSelector, Event.ONCLICK);
 
             availableCells.put("0", new RowHeadersHeaderCell());
         }
 
         public void updateCellsFromUIDL(UIDL uidl) {
             Iterator it = uidl.getChildIterator();
             HashSet updated = new HashSet();
             updated.add("0");
             while (it.hasNext()) {
                 final UIDL col = (UIDL) it.next();
                 final String cid = col.getStringAttribute("cid");
                 updated.add(cid);
 
                 String caption = buildCaptionHtmlSnippet(col);
                 HeaderCell c = getHeaderCell(cid);
                 if (c == null) {
                     c = new HeaderCell(cid, caption);
                     availableCells.put(cid, c);
                     if (initializedAndAttached) {
                         // we will need a column width recalculation
                         initializedAndAttached = false;
                         initialContentReceived = false;
                         isNewBody = true;
                     }
                 } else {
                     c.setText(caption);
                 }
 
                 if (col.hasAttribute("sortable")) {
                     c.setSortable(true);
                     if (cid.equals(sortColumn)) {
                         c.setSorted(true);
                     } else {
                         c.setSorted(false);
                     }
                 }
                 if (col.hasAttribute("align")) {
                     c.setAlign(col.getStringAttribute("align").charAt(0));
                 }
                 if (col.hasAttribute("width")) {
                     final String width = col.getStringAttribute("width");
                     c.setWidth(Integer.parseInt(width));
                 }
             }
             // check for orphaned header cells
             it = availableCells.keySet().iterator();
             while (it.hasNext()) {
                 String cid = (String) it.next();
                 if (!updated.contains(cid)) {
                     removeCell(cid);
                     it.remove();
                 }
             }
 
         }
 
         public void enableColumn(String cid, int index) {
             final HeaderCell c = getHeaderCell(cid);
             if (!c.isEnabled() || getHeaderCell(index) != c) {
                 setHeaderCell(index, c);
                 if (c.getWidth() == -1) {
                     if (initializedAndAttached) {
                         // column is not drawn before,
                         // we will need a column width recalculation
                         initializedAndAttached = false;
                         initialContentReceived = false;
                         isNewBody = true;
                     }
                 }
             }
         }
 
         public int getVisibleCellCount() {
             return visibleCells.size();
         }
 
         public void setHorizontalScrollPosition(int scrollLeft) {
             DOM.setElementPropertyInt(hTableWrapper, "scrollLeft", scrollLeft);
         }
 
         public void setColumnCollapsingAllowed(boolean cc) {
             if (cc) {
                 DOM.setStyleAttribute(columnSelector, "display", "block");
             } else {
                 DOM.setStyleAttribute(columnSelector, "display", "none");
             }
         }
 
         public void disableBrowserIntelligence() {
             DOM.setStyleAttribute(hTableContainer, "width", WRAPPER_WIDTH
                     + "px");
         }
 
         public void enableBrowserIntelligence() {
             DOM.setStyleAttribute(hTableContainer, "width", "");
         }
 
         public void setHeaderCell(int index, HeaderCell cell) {
             if (cell.isEnabled()) {
                 // we're moving the cell
                 DOM.removeChild(tr, cell.getElement());
                 orphan(cell);
             }
             if (index < visibleCells.size()) {
                 // insert to right slot
                 DOM.insertChild(tr, cell.getElement(), index);
                 adopt(cell);
                 visibleCells.insertElementAt(cell, index);
 
             } else if (index == visibleCells.size()) {
                 // simply append
                 DOM.appendChild(tr, cell.getElement());
                 adopt(cell);
                 visibleCells.add(cell);
             } else {
                 throw new RuntimeException(
                         "Header cells must be appended in order");
             }
         }
 
         public HeaderCell getHeaderCell(int index) {
             if (index < visibleCells.size()) {
                 return (HeaderCell) visibleCells.get(index);
             } else {
                 return null;
             }
         }
 
         /**
          * Get's HeaderCell by it's column Key.
          * 
          * Note that this returns HeaderCell even if it is currently collapsed.
          * 
          * @param cid
          *                Column key of accessed HeaderCell
          * @return HeaderCell
          */
         public HeaderCell getHeaderCell(String cid) {
             return (HeaderCell) availableCells.get(cid);
         }
 
         public void moveCell(int oldIndex, int newIndex) {
             final HeaderCell hCell = getHeaderCell(oldIndex);
             final Element cell = hCell.getElement();
 
             visibleCells.remove(oldIndex);
             DOM.removeChild(tr, cell);
 
             DOM.insertChild(tr, cell, newIndex);
             visibleCells.insertElementAt(hCell, newIndex);
         }
 
         public Iterator iterator() {
             return visibleCells.iterator();
         }
 
         public boolean remove(Widget w) {
             if (visibleCells.contains(w)) {
                 visibleCells.remove(w);
                 orphan(w);
                 DOM.removeChild(DOM.getParent(w.getElement()), w.getElement());
                 return true;
             }
             return false;
         }
 
         public void removeCell(String colKey) {
             final HeaderCell c = getHeaderCell(colKey);
             remove(c);
         }
 
         private void focusSlot(int index) {
             removeSlotFocus();
             if (index > 0) {
                 DOM.setElementProperty(DOM.getFirstChild(DOM.getChild(tr,
                         index - 1)), "className", CLASSNAME + "-resizer "
                         + CLASSNAME + "-focus-slot-right");
             } else {
                 DOM.setElementProperty(DOM.getFirstChild(DOM
                         .getChild(tr, index)), "className", CLASSNAME
                         + "-resizer " + CLASSNAME + "-focus-slot-left");
             }
             focusedSlot = index;
         }
 
         private void removeSlotFocus() {
             if (focusedSlot < 0) {
                 return;
             }
             if (focusedSlot == 0) {
                 DOM.setElementProperty(DOM.getFirstChild(DOM.getChild(tr,
                         focusedSlot)), "className", CLASSNAME + "-resizer");
             } else if (focusedSlot > 0) {
                 DOM.setElementProperty(DOM.getFirstChild(DOM.getChild(tr,
                         focusedSlot - 1)), "className", CLASSNAME + "-resizer");
             }
             focusedSlot = -1;
         }
 
         public void onBrowserEvent(Event event) {
             if (enabled) {
                 if (DOM.compare(DOM.eventGetTarget(event), columnSelector)) {
                     final int left = DOM.getAbsoluteLeft(columnSelector);
                     final int top = DOM.getAbsoluteTop(columnSelector)
                             + DOM.getElementPropertyInt(columnSelector,
                                     "offsetHeight");
                     client.getContextMenu().showAt(this, left, top);
                 }
             }
         }
 
         class VisibleColumnAction extends Action {
 
             String colKey;
             private boolean collapsed;
 
             public VisibleColumnAction(String colKey) {
                 super(IScrollTable.TableHead.this);
                 this.colKey = colKey;
                 caption = tHead.getHeaderCell(colKey).getCaption();
             }
 
             public void execute() {
                 client.getContextMenu().hide();
                 // toggle selected column
                 if (collapsedColumns.contains(colKey)) {
                     collapsedColumns.remove(colKey);
                 } else {
                     tHead.removeCell(colKey);
                     collapsedColumns.add(colKey);
                 }
 
                 // update variable to server
                 client.updateVariable(paintableId, "collapsedcolumns",
                         collapsedColumns.toArray(), false);
                 // let rowRequestHandler determine proper rows
                 rowRequestHandler.refreshContent();
             }
 
             public void setCollapsed(boolean b) {
                 collapsed = b;
             }
 
             /**
              * Override default method to distinguish on/off columns
              */
             public String getHTML() {
                 final StringBuffer buf = new StringBuffer();
                 if (collapsed) {
                     buf.append("<span class=\"i-off\">");
                 } else {
                     buf.append("<span class=\"i-on\">");
                 }
                 buf.append(super.getHTML());
                 buf.append("</span>");
 
                 return buf.toString();
             }
 
         }
 
         /*
          * Returns columns as Action array for column select popup
          */
         public Action[] getActions() {
             Object[] cols;
             if (columnReordering) {
                 cols = columnOrder;
             } else {
                 // if columnReordering is disabled, we need different way to get
                 // all available columns
                 cols = visibleColOrder;
                 cols = new Object[visibleColOrder.length
                         + collapsedColumns.size()];
                 int i;
                 for (i = 0; i < visibleColOrder.length; i++) {
                     cols[i] = visibleColOrder[i];
                 }
                 for (final Iterator it = collapsedColumns.iterator(); it
                         .hasNext();) {
                     cols[i++] = it.next();
                 }
             }
             final Action[] actions = new Action[cols.length];
 
             for (int i = 0; i < cols.length; i++) {
                 final String cid = (String) cols[i];
                 final HeaderCell c = getHeaderCell(cid);
                 final VisibleColumnAction a = new VisibleColumnAction(c
                         .getColKey());
                 a.setCaption(c.getCaption());
                 if (!c.isEnabled()) {
                     a.setCollapsed(true);
                 }
                 actions[i] = a;
             }
             return actions;
         }
 
         public ApplicationConnection getClient() {
             return client;
         }
 
         public String getPaintableId() {
             return paintableId;
         }
 
         /**
          * Returns column alignments for visible columns
          */
         public char[] getColumnAlignments() {
             final Iterator it = visibleCells.iterator();
             final char[] aligns = new char[visibleCells.size()];
             int colIndex = 0;
             while (it.hasNext()) {
                 aligns[colIndex++] = ((HeaderCell) it.next()).getAlign();
             }
             return aligns;
         }
 
     }
 
     /**
      * This Panel can only contain IScrollTableRow type of widgets. This
      * "simulates" very large table, keeping spacers which take room of
      * unrendered rows.
      * 
      */
     public class IScrollTableBody extends Panel {
 
         public static final int CELL_EXTRA_WIDTH = 20;
 
         public static final int DEFAULT_ROW_HEIGHT = 24;
 
         /**
          * Amount of padding inside one table cell (this is reduced from the
          * "cellContent" element's width). You may override this in your own
          * widgetset.
          */
         public static final int CELL_CONTENT_PADDING = 8;
 
         private int rowHeight = -1;
 
         private final List renderedRows = new Vector();
 
         private boolean initDone = false;
 
         Element preSpacer = DOM.createDiv();
         Element postSpacer = DOM.createDiv();
 
         Element container = DOM.createDiv();
 
         Element tBody = DOM.createTBody();
         Element table = DOM.createTable();
 
         private int firstRendered;
 
         private int lastRendered;
 
         private char[] aligns;
 
         IScrollTableBody() {
             constructDOM();
             setElement(container);
         }
 
         private void constructDOM() {
             DOM.setElementProperty(table, "className", CLASSNAME + "-table");
             DOM.setElementProperty(preSpacer, "className", CLASSNAME
                     + "-row-spacer");
             DOM.setElementProperty(postSpacer, "className", CLASSNAME
                     + "-row-spacer");
 
             DOM.appendChild(table, tBody);
             DOM.appendChild(container, preSpacer);
             DOM.appendChild(container, table);
             DOM.appendChild(container, postSpacer);
 
         }
 
         public int getAvailableWidth() {
             return DOM.getElementPropertyInt(preSpacer, "offsetWidth");
         }
 
         public void renderInitialRows(UIDL rowData, int firstIndex, int rows) {
             firstRendered = firstIndex;
             lastRendered = firstIndex + rows - 1;
             final Iterator it = rowData.getChildIterator();
             aligns = tHead.getColumnAlignments();
             while (it.hasNext()) {
                 final IScrollTableRow row = new IScrollTableRow((UIDL) it
                         .next(), aligns);
                 addRow(row);
             }
             if (isAttached()) {
                 fixSpacers();
             }
         }
 
         public void renderRows(UIDL rowData, int firstIndex, int rows) {
             // FIXME REVIEW
             aligns = tHead.getColumnAlignments();
             final Iterator it = rowData.getChildIterator();
             if (firstIndex == lastRendered + 1) {
                 while (it.hasNext()) {
                     final IScrollTableRow row = createRow((UIDL) it.next());
                     addRow(row);
                     lastRendered++;
                 }
                 fixSpacers();
             } else if (firstIndex + rows == firstRendered) {
                 final IScrollTableRow[] rowArray = new IScrollTableRow[rows];
                 int i = rows;
                 while (it.hasNext()) {
                     i--;
                     rowArray[i] = createRow((UIDL) it.next());
                 }
                 for (i = 0; i < rows; i++) {
                     addRowBeforeFirstRendered(rowArray[i]);
                     firstRendered--;
                 }
             } else {
                 // completely new set of rows
                 while (lastRendered + 1 > firstRendered) {
                     unlinkRow(false);
                 }
                 final IScrollTableRow row = createRow((UIDL) it.next());
                 firstRendered = firstIndex;
                 lastRendered = firstIndex - 1;
                 addRow(row);
                 lastRendered++;
                 setContainerHeight();
                 fixSpacers();
                 while (it.hasNext()) {
                     addRow(createRow((UIDL) it.next()));
                     lastRendered++;
                 }
                 fixSpacers();
             }
             // this may be a new set of rows due content change,
             // ensure we have proper cache rows
             int reactFirstRow = (int) (firstRowInViewPort - pageLength
                     * CACHE_REACT_RATE);
             int reactLastRow = (int) (firstRowInViewPort + pageLength + pageLength
                     * CACHE_REACT_RATE);
             if (reactFirstRow < 0) {
                 reactFirstRow = 0;
             }
             if (reactLastRow > totalRows) {
                 reactLastRow = totalRows - 1;
             }
             if (lastRendered < reactLastRow) {
                 // get some cache rows below visible area
                 rowRequestHandler.setReqFirstRow(lastRendered + 1);
                 rowRequestHandler.setReqRows(reactLastRow - lastRendered - 1);
                 rowRequestHandler.deferRowFetch(1);
             } else if (IScrollTable.this.tBody.getFirstRendered() > reactFirstRow) {
                 /*
                  * Branch for fetching cache above visible area.
                  * 
                  * If cache needed for both before and after visible area, this
                  * will be rendered after-cache is reveived and rendered. So in
                  * some rare situations table may take two cache visits to
                  * server.
                  */
                 rowRequestHandler.setReqFirstRow(reactFirstRow);
                 rowRequestHandler.setReqRows(firstRendered - reactFirstRow);
                 rowRequestHandler.deferRowFetch(1);
             }
         }
 
         /**
          * This method is used to instantiate new rows for this table. It
          * automatically sets correct widths to rows cells and assigns correct
          * client reference for child widgets.
          * 
          * This method can be called only after table has been initialized
          * 
          * @param uidl
          */
         private IScrollTableRow createRow(UIDL uidl) {
             final IScrollTableRow row = new IScrollTableRow(uidl, aligns);
             final int cells = DOM.getChildCount(row.getElement());
             for (int i = 0; i < cells; i++) {
                 final Element cell = DOM.getChild(row.getElement(), i);
                 final int w = IScrollTable.this
                         .getColWidth(getColKeyByIndex(i));
                 DOM.setStyleAttribute(DOM.getFirstChild(cell), "width",
                         (w - CELL_CONTENT_PADDING) + "px");
                 DOM.setStyleAttribute(cell, "width", w + "px");
             }
             return row;
         }
 
         private void addRowBeforeFirstRendered(IScrollTableRow row) {
             IScrollTableRow first = null;
             if (renderedRows.size() > 0) {
                 first = (IScrollTableRow) renderedRows.get(0);
             }
             if (first != null && first.getStyleName().indexOf("-odd") == -1) {
                 row.addStyleName(CLASSNAME + "-row-odd");
             } else {
                 row.addStyleName(CLASSNAME + "-row");
             }
             if (row.isSelected()) {
                 row.addStyleName("i-selected");
             }
             DOM.insertChild(tBody, row.getElement(), 0);
             adopt(row);
             renderedRows.add(0, row);
         }
 
         private void addRow(IScrollTableRow row) {
             IScrollTableRow last = null;
             if (renderedRows.size() > 0) {
                 last = (IScrollTableRow) renderedRows
                         .get(renderedRows.size() - 1);
             }
             if (last != null && last.getStyleName().indexOf("-odd") == -1) {
                 row.addStyleName(CLASSNAME + "-row-odd");
             } else {
                 row.addStyleName(CLASSNAME + "-row");
             }
             if (row.isSelected()) {
                 row.addStyleName("i-selected");
             }
             DOM.appendChild(tBody, row.getElement());
             adopt(row);
             renderedRows.add(row);
         }
 
         public Iterator iterator() {
             return renderedRows.iterator();
         }
 
         /**
          * @return false if couldn't remove row
          */
         public boolean unlinkRow(boolean fromBeginning) {
             if (lastRendered - firstRendered < 0) {
                 return false;
             }
             int index;
             if (fromBeginning) {
                 index = 0;
                 firstRendered++;
             } else {
                 index = renderedRows.size() - 1;
                 lastRendered--;
             }
             final IScrollTableRow toBeRemoved = (IScrollTableRow) renderedRows
                     .get(index);
             client.unregisterChildPaintables(toBeRemoved);
             DOM.removeChild(tBody, toBeRemoved.getElement());
             orphan(toBeRemoved);
             renderedRows.remove(index);
             fixSpacers();
             return true;
         }
 
         public boolean remove(Widget w) {
             throw new UnsupportedOperationException();
         }
 
         protected void onAttach() {
             super.onAttach();
             setContainerHeight();
         }
 
         /**
          * Fix container blocks height according to totalRows to avoid
          * "bouncing" when scrolling
          */
         private void setContainerHeight() {
             fixSpacers();
             DOM.setStyleAttribute(container, "height", totalRows
                     * getRowHeight() + "px");
         }
 
         private void fixSpacers() {
             int prepx = getRowHeight() * firstRendered;
             if (prepx < 0) {
                 prepx = 0;
             }
             DOM.setStyleAttribute(preSpacer, "height", prepx + "px");
             int postpx = getRowHeight() * (totalRows - 1 - lastRendered);
             if (postpx < 0) {
                 postpx = 0;
             }
             DOM.setStyleAttribute(postSpacer, "height", postpx + "px");
         }
 
         public int getRowHeight() {
             if (initDone) {
                 return rowHeight;
             } else {
                 if (DOM.getChildCount(tBody) > 0) {
                     rowHeight = DOM
                             .getElementPropertyInt(tBody, "offsetHeight")
                             / DOM.getChildCount(tBody);
                 } else {
                     return DEFAULT_ROW_HEIGHT;
                 }
                 initDone = true;
                 return rowHeight;
             }
         }
 
         public int getColWidth(int i) {
             if (initDone) {
                 final Element e = DOM.getChild(DOM.getChild(tBody, 0), i);
                 return DOM.getElementPropertyInt(e, "offsetWidth");
             } else {
                 return 0;
             }
         }
 
         public void setColWidth(int colIndex, int w) {
             final int rows = DOM.getChildCount(tBody);
             for (int i = 0; i < rows; i++) {
                 final Element cell = DOM.getChild(DOM.getChild(tBody, i),
                         colIndex);
                 DOM.setStyleAttribute(DOM.getFirstChild(cell), "width",
                         (w - CELL_CONTENT_PADDING) + "px");
                 DOM.setStyleAttribute(cell, "width", w + "px");
             }
         }
 
         public int getLastRendered() {
             return lastRendered;
         }
 
         public int getFirstRendered() {
             return firstRendered;
         }
 
         public void moveCol(int oldIndex, int newIndex) {
 
             // loop all rows and move given index to its new place
             final Iterator rows = iterator();
             while (rows.hasNext()) {
                 final IScrollTableRow row = (IScrollTableRow) rows.next();
 
                 final Element td = DOM.getChild(row.getElement(), oldIndex);
                 DOM.removeChild(row.getElement(), td);
 
                 DOM.insertChild(row.getElement(), td, newIndex);
 
             }
 
         }
 
         public class IScrollTableRow extends Panel implements ActionOwner {
 
             Vector childWidgets = new Vector();
             private boolean selected = false;
             private final int rowKey;
 
             private String[] actionKeys = null;
 
             private IScrollTableRow(int rowKey) {
                 this.rowKey = rowKey;
                 setElement(DOM.createElement("tr"));
                 DOM.sinkEvents(getElement(), Event.ONCLICK);
                 attachContextMenuEvent(getElement());
             }
 
             protected void onDetach() {
                 Util.removeContextMenuEvent(getElement());
                 super.onDetach();
             }
 
             /**
              * Attaches context menu event handler to given element. Attached
              * handler fires showContextMenu function.
              * 
              * @param el
              *                element where to attach contenxt menu event
              */
             private native void attachContextMenuEvent(Element el)
             /*-{
             	var row = this;
             	el.oncontextmenu = function(e) {
             		if(!e)
             			e = $wnd.event;
             		row.@com.itmill.toolkit.terminal.gwt.client.ui.IScrollTable.IScrollTableBody.IScrollTableRow::showContextMenu(Lcom/google/gwt/user/client/Event;)(e);
             		return false;
             	};
             }-*/;
 
             public String getKey() {
                 return String.valueOf(rowKey);
             }
 
             public IScrollTableRow(UIDL uidl, char[] aligns) {
                 this(uidl.getIntAttribute("key"));
 
                 String rowStyle = uidl.getStringAttribute("rowstyle");
                 if (rowStyle != null) {
                     addStyleName(CLASSNAME + "-row-" + rowStyle);
                 }
 
                 tHead.getColumnAlignments();
                 int col = 0;
                 // row header
                 if (showRowHeaders) {
                     addCell(buildCaptionHtmlSnippet(uidl), aligns[col++], "");
                 }
 
                 if (uidl.hasAttribute("al")) {
                     actionKeys = uidl.getStringArrayAttribute("al");
                 }
 
                 final Iterator cells = uidl.getChildIterator();
                 while (cells.hasNext()) {
                     final Object cell = cells.next();
                     if (cell instanceof String) {
                         String style = "";
                         if (uidl.hasAttribute("style-"
                                 + (showRowHeaders ? col - 1 : col))) {
                             style = uidl.getStringAttribute("style-"
                                     + (showRowHeaders ? col - 1 : col));
                         }
                         addCell(cell.toString(), aligns[col++], style);
                     } else {
                         final Paintable cellContent = client
                                 .getPaintable((UIDL) cell);
                         (cellContent).updateFromUIDL((UIDL) cell, client);
                         String style = "";
                         if (uidl.hasAttribute("style")) {
                             style = uidl.getStringAttribute("style");
                         }
                         addCell((Widget) cellContent, aligns[col++], style);
                     }
                 }
                 if (uidl.hasAttribute("selected") && !isSelected()) {
                     toggleSelection();
                 }
             }
 
             public void addCell(String text, char align, String style) {
                 // String only content is optimized by not using Label widget
                 final Element td = DOM.createTD();
                 final Element container = DOM.createDiv();
                 String className = CLASSNAME + "-cell-content";
                 if (style != null && !style.equals("")) {
                     className += " " + CLASSNAME + "-cell-content-" + style;
                 }
 
                 DOM.setElementProperty(container, "className", className);
                 DOM.setInnerHTML(container, text);
                 if (align != ALIGN_LEFT) {
                     switch (align) {
                     case ALIGN_CENTER:
                         DOM.setStyleAttribute(container, "textAlign", "center");
                         break;
                     case ALIGN_RIGHT:
                     default:
                         DOM.setStyleAttribute(container, "textAlign", "right");
                         break;
                     }
                 }
                 DOM.appendChild(td, container);
                 DOM.appendChild(getElement(), td);
             }
 
             public void addCell(Widget w, char align, String style) {
                 final Element td = DOM.createTD();
                 final Element container = DOM.createDiv();
                 String className = CLASSNAME + "-cell-content";
                 if (style != null && !style.equals("")) {
                     className += " " + CLASSNAME + "-cell-content-" + style;
                 }
                 DOM.setElementProperty(container, "className", className);
                 // TODO most components work with this, but not all (e.g.
                 // Select)
                 // Old comment: make widget cells respect align.
                 // text-align:center for IE, margin: auto for others
                 if (align != ALIGN_LEFT) {
                     switch (align) {
                     case ALIGN_CENTER:
                         DOM.setStyleAttribute(container, "textAlign", "center");
                         break;
                     case ALIGN_RIGHT:
                     default:
                         DOM.setStyleAttribute(container, "textAlign", "right");
                         break;
                     }
                 }
                 DOM.appendChild(td, container);
                 DOM.appendChild(getElement(), td);
                 DOM.appendChild(container, w.getElement());
                 adopt(w);
                 childWidgets.add(w);
             }
 
             public Iterator iterator() {
                 return childWidgets.iterator();
             }
 
             public boolean remove(Widget w) {
                 // TODO Auto-generated method stub
                 return false;
             }
 
             /*
              * React on click that occur on content cells only
              */
             public void onBrowserEvent(Event event) {
                 switch (DOM.eventGetType(event)) {
                 case Event.ONCLICK:
                     final Element tdOrTr = DOM.getParent(DOM
                             .eventGetTarget(event));
                     if (DOM.compare(getElement(), tdOrTr)
                             || DOM.compare(getElement(), DOM.getParent(tdOrTr))) {
                         if (selectMode > Table.SELECT_MODE_NONE) {
                             toggleSelection();
                             client.updateVariable(paintableId, "selected",
                                     selectedRowKeys.toArray(), immediate);
                         }
                     }
                     break;
 
                 default:
                     break;
                 }
                 super.onBrowserEvent(event);
             }
 
             public void showContextMenu(Event event) {
                 ApplicationConnection.getConsole().log("Context menu");
                 if (enabled && actionKeys != null) {
                     int left = DOM.eventGetClientX(event);
                     int top = DOM.eventGetClientY(event);
                     top += Window.getScrollTop();
                     left += Window.getScrollLeft();
                     client.getContextMenu().showAt(this, left, top);
                 }
             }
 
             public boolean isSelected() {
                 return selected;
             }
 
             private void toggleSelection() {
                 selected = !selected;
                 if (selected) {
                     if (selectMode == Table.SELECT_MODE_SINGLE) {
                         deselectAll();
                     }
                     selectedRowKeys.add(String.valueOf(rowKey));
                     addStyleName("i-selected");
                 } else {
                     selectedRowKeys.remove(String.valueOf(rowKey));
                     removeStyleName("i-selected");
                 }
             }
 
             /*
              * (non-Javadoc)
              * 
              * @see com.itmill.toolkit.terminal.gwt.client.ui.IActionOwner#getActions()
              */
             public Action[] getActions() {
                 if (actionKeys == null) {
                     return new Action[] {};
                 }
                 final Action[] actions = new Action[actionKeys.length];
                 for (int i = 0; i < actions.length; i++) {
                     final String actionKey = actionKeys[i];
                     final TreeAction a = new TreeAction(this, String
                             .valueOf(rowKey), actionKey);
                     a.setCaption(getActionCaption(actionKey));
                     a.setIconUrl(getActionIcon(actionKey));
                     actions[i] = a;
                 }
                 return actions;
             }
 
             public ApplicationConnection getClient() {
                 return client;
             }
 
             public String getPaintableId() {
                 return paintableId;
             }
         }
     }
 
     public void deselectAll() {
         final Object[] keys = selectedRowKeys.toArray();
         for (int i = 0; i < keys.length; i++) {
             final IScrollTableRow row = getRenderedRowByKey((String) keys[i]);
             if (row != null && row.isSelected()) {
                 row.toggleSelection();
             }
         }
         // still ensure all selects are removed from (not necessary rendered)
         selectedRowKeys.clear();
 
     }
 
     public void add(Widget w) {
         throw new UnsupportedOperationException(
                 "ITable can contain only rows created by itself.");
     }
 
     public void clear() {
         panel.clear();
     }
 
     public Iterator iterator() {
         return panel.iterator();
     }
 
     public boolean remove(Widget w) {
         return panel.remove(w);
     }
 
     public void mySetHeight(String height) {
         // workaround very common 100% height problem - extract borders
         if (height.equals("100%")) {
             final int borders = getBorderSpace();
             final Element parentElem = DOM.getParent(getElement());
 
             // put table away from flow for a moment
             DOM.setStyleAttribute(getElement(), "position", "absolute");
             // get containers natural space for table
             int availPixels = DOM.getElementPropertyInt(parentElem,
                     "offsetHeight");
             if (Util.isIE()) {
                 if (availPixels == 0) {
                     // In complex layouts IE sometimes rather randomly returns 0
                     // although container really has height. Use old value if
                     // one exits.
                     if (oldAvailPixels > 0) {
                         availPixels = oldAvailPixels;
                     }
                 } else {
                     oldAvailPixels = availPixels;
                 }
             }
             // put table back to flow
             DOM.setStyleAttribute(getElement(), "position", "static");
             // set 100% height with borders
             int pixelSize = (availPixels - borders);
             if (pixelSize < 0) {
                 pixelSize = 0;
             }
             super.setHeight(pixelSize + "px");
         } else {
             // normally height don't include borders
             super.setHeight(height);
         }
     }
 
     private int getBorderSpace() {
         final Element el = getElement();
         return DOM.getElementPropertyInt(el, "offsetHeight")
                 - DOM.getElementPropertyInt(el, "clientHeight");
     }
 
     public void setWidth(String width) {
         // NOP size handled internally
     }
 
     public void setHeight(String height) {
         // NOP size handled internally
     }
 
     /*
      * Overridden due Table might not survive of visibility change (scroll pos
      * lost). Example ITabPanel just set contained components invisible and back
      * when changing tabs.
      */
     public void setVisible(boolean visible) {
         if (isVisible() != visible) {
             super.setVisible(visible);
             if (initializedAndAttached) {
                 if (visible) {
                     DeferredCommand.addCommand(new Command() {
                         public void execute() {
                             bodyContainer.setScrollPosition(firstRowInViewPort
                                     * tBody.getRowHeight());
                         }
                     });
                 }
             }
         }
     }
 
     /**
      * Helper function to build html snippet for column or row headers
      * 
      * @param uidl
      *                possibly with values caption and icon
      * @return html snippet containing possibly an icon + caption text
      */
     private String buildCaptionHtmlSnippet(UIDL uidl) {
         String s = uidl.getStringAttribute("caption");
         if (uidl.hasAttribute("icon")) {
             s = "<img src=\""
                     + client.translateToolkitUri(uidl
                             .getStringAttribute("icon"))
                     + "\" alt=\"icon\" class=\"i-icon\">" + s;
         }
         return s;
     }
 
 }
