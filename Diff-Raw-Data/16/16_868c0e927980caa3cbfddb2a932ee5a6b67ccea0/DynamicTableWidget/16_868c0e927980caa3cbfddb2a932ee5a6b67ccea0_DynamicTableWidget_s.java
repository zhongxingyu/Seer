 package com.scurab.gwt.rlw.client.components;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Set;
 
 import com.google.gwt.cell.client.Cell;
 import com.google.gwt.cell.client.Cell.Context;
 import com.google.gwt.cell.client.TextCell;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.safehtml.shared.SafeHtml;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.user.cellview.client.CellTable;
 import com.google.gwt.user.cellview.client.Column;
 import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
 import com.google.gwt.user.cellview.client.SimplePager;
 import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.HasVerticalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.view.client.ListDataProvider;
 import com.google.gwt.view.client.SelectionModel;
 import com.scurab.gwt.rlw.client.RemoteLogWeb;
 import com.scurab.gwt.rlw.shared.SharedParams;
 
 /**
  * Dynamic table widget setData must be called before is widget attechd into element
  * 
  * @author Joe Scurab
  * 
  */
 public class DynamicTableWidget extends Composite {
 
     private List<HashMap<String, Object>> mData = null;
 
     private CellTable<HashMap<String, Object>> mCellTable = null;
 
     private ListDataProvider<HashMap<String, Object>> mListDataProvider = null;
 
     private LazyPager pager;
 
     private DynamicTableWidgetOverrider mOverrider = null;
 
     private boolean mSuccesfullSortByUnderscore = false;
 
     private CheckBox mFilterCheckBox;
 
     private Button mFilterButton;
 
     private Button mReloadButton;
 
     public interface DynamicTableWidgetOverrider {
         /**
          * PageSize is overrided only if value > 0
          * 
          * @return
          */
         int getPageSize();
 
         String getColumnName(String key);
 
         List<String> getColumnsOrder(List<HashMap<String, Object>> mData);
 
         void overrideColumn(Column<HashMap<String, Object>, String> c);
 
         /**
          * If returned value is null, used default implementation
          * 
          * @param key
          * @param dataExample
          * @return if render was overrided
          */
         boolean onColumnRender(String name, Column<HashMap<String, Object>, String> column, Context context,
                 HashMap<String, Object> object, SafeHtmlBuilder sb);
     }
 
     public DynamicTableWidget() {
 
     }
 
     public DynamicTableWidget(List<HashMap<String, Object>> data) {
         this();
         setData(data);
     }
 
     /**
      * Throws {@link NullPointerException} when data is null
      * 
      * @param data
      */
     public void setData(List<HashMap<String, Object>> data) {
         if (data == null) {
             data = new ArrayList<HashMap<String, Object>>();
         }
         mData = data;
         if (mListDataProvider == null) {
             init(mData);
         } else {
             mListDataProvider.setList(data);
             pager.resetLazy();
         }
     }
 
     public List<HashMap<String, Object>> getData() {
         return mData;
     }
 
     public void setOverrirder(DynamicTableWidgetOverrider overrider) {
         mOverrider = overrider;
     }
 
     private void init(List<HashMap<String, Object>> data) {
         mListDataProvider = new ListDataProvider<HashMap<String, Object>>(data);
         mCellTable = new CellTable<HashMap<String, Object>>();
         mCellTable.setEmptyTableWidget(new Label(RemoteLogWeb.WORDS.NoData()).asWidget());
         mCellTable.setLoadingIndicator(new Label("Loading").asWidget());
         mCellTable.setPageSize(getPageSize());
         mCellTable.setSelectionModel(getSelectionModel());
 
         // Create a Pager to control the table.
         SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
         pager = new LazyPager(TextLocation.CENTER, pagerResources, false, 0, true);
         pager.setDisplay(mCellTable);
         pager.setPageSize(getPageSize());
 
         // init columns
        ListHandler<HashMap<String, Object>> sortHandler = new ListHandler<HashMap<String, Object>>(
                 mListDataProvider.getList());
        initTableColumns(data, mCellTable, sortHandler);
        mCellTable.addColumnSortHandler(sortHandler);
         mListDataProvider.addDataDisplay(mCellTable);
 
         //add top panel 
         HorizontalPanel hp = new HorizontalPanel();
         mReloadButton = onCreateReloadButton();
         hp.add(getReloadButton());
 
         hp.add(pager);
         mFilterButton = onCreateFilterButton();
         hp.add(mFilterButton);
         mFilterCheckBox = onCreateFilterCheckBox();
         hp.add(mFilterCheckBox);
         hp.setCellVerticalAlignment(mFilterCheckBox, HasVerticalAlignment.ALIGN_MIDDLE);
 
         VerticalPanel vp = new VerticalPanel();
         vp.setWidth("100%");
         vp.add(hp);
         vp.add(mCellTable);
         initWidget(vp);
         mSuccesfullSortByUnderscore = false;
     }
 
     protected CheckBox onCreateFilterCheckBox() {
         CheckBox c = new CheckBox(RemoteLogWeb.WORDS.Filter());
         return c;
     }
 
     protected Button onCreateFilterButton() {
         Button b = new Button(RemoteLogWeb.WORDS.SetFilter());
         return b;
     }
 
     protected Button onCreateReloadButton(){
         return new Button(RemoteLogWeb.WORDS.Reload());
     }
 
     /**
      * Called when table and pages is generated<br>
      * By this value you can override default (50) page size
      * 
      * @return
      */
     protected int getPageSize() {
         if (mOverrider != null) {
             int value = mOverrider.getPageSize();
             if (value > 0) {
                 return value;
             }
         }
         return SharedParams.PAGE_SIZE;
     }
 
     /**
      * Here you can define selection model for table<br>
      * By default, there is no selection model, so here just return null
      * 
      * @return
      */
     protected SelectionModel<HashMap<String, Object>> getSelectionModel() {
         return null;
     }
 
     private void initTableColumns(List<HashMap<String, Object>> mData, CellTable<HashMap<String, Object>> cellTable,
             ListHandler<HashMap<String, Object>> sortHandler) {
         // call handler
         onStartCreatingColumns(mData, cellTable, sortHandler);
         // get order of columns
         List<String> columns = getColumnsOrder(mData);
 
         for (String columnName : columns) {
             // object
             Object dataExample = mData.get(0).get(columnName);
             // create column
             Column<HashMap<String, Object>, String> column = onCreateColumn(columnName, dataExample);
 
             // make it sortable
             boolean sortable = isColumnSortable(columnName);
             column.setSortable(sortable);
             if (sortable) {
                 sortHandler.setComparator(column, getComparator(columnName, dataExample));
             }
 
             // call overrider
             overrideColumn(column);
 
             // add to table
             cellTable.addColumn(column, convertToSafeHtml(getColumnName(columnName)),
                     convertToSafeHtml(getFooterValue(columnName, mData)));
         }
         // call handler
         onFinishCreatingColumns(mData, cellTable, sortHandler);
     }
 
     protected SafeHtml convertToSafeHtml(String value) {
         if (value == null) {
             return null;
         }
         return new SafeHtmlBuilder().appendHtmlConstant(value).toSafeHtml();
     }
 
     protected void onFinishCreatingColumns(List<HashMap<String, Object>> mData,
             CellTable<HashMap<String, Object>> cellTable, ListHandler<HashMap<String, Object>> sortHandler) {
 
     }
 
     protected void onStartCreatingColumns(List<HashMap<String, Object>> mData,
             CellTable<HashMap<String, Object>> cellTable, ListHandler<HashMap<String, Object>> sortHandler) {
 
     }
 
     /**
      * Overrides default order of columns and which will be generated<br>
      * Values must be same, you can skip some key. By default if all columns has NAME_{ORDER} its sorted by ORDER value
      * 
      * @param mData
      *            data downloaded from server
      * @return List of keys how should be table generated
      */
     protected List<String> getColumnsOrder(List<HashMap<String, Object>> mData) {
         List<String> keys = null;
         // check if overrider wants handle it
         if (mOverrider != null) {
             keys = mOverrider.getColumnsOrder(mData);
             if (keys != null) {
                 return keys;
             }
         }
 
         Set<String> columns = mData.get(0).keySet();
         try {
             mSuccesfullSortByUnderscore = true;
 
             HashMap<String, String> ma = new HashMap<String, String>();
 
             for (String columnName : columns) {
                 String[] vals = columnName.split("_");
                 mSuccesfullSortByUnderscore = mSuccesfullSortByUnderscore && vals.length > 1;
                 String orderValue = vals[vals.length - 1];
                 ma.put(orderValue, columnName);
             }
 
             List<String> sorted = new ArrayList<String>(ma.keySet());
             Collections.sort(sorted);
             keys = new ArrayList<String>();
 
             for (String sort : sorted) {
                 keys.add(ma.get(sort));
             }
         } catch (Exception e) {
             keys = new ArrayList<String>(mData.get(0).keySet());
             mSuccesfullSortByUnderscore = false;
         }
         return keys;
     }
 
     /**
      * Shows footer value, ie sum of values in column
      * 
      * @param columnName
      * @param mData
      * @return
      */
     public String getFooterValue(String columnName, List<HashMap<String, Object>> mData) {
         return null;
     }
 
     public void overrideColumn(Column<HashMap<String, Object>, String> c) {
         if (mOverrider != null) {
             mOverrider.overrideColumn(c);
         }
     }
 
     /**
      * Called when column is created to replace default db column name to some user friendly value
      * 
      * @param key
      *            columnName from db
      * @return value to show user as column name
      */
     protected String getColumnName(String key) {
         if (mOverrider != null) {
             String v = mOverrider.getColumnName(key);
             if (v != null) {
                 return v;
             }
         }
         String name = key;
         try {
             name = changeColumnName(key);
         } catch (Exception e) {
             // fucking stupids
         }
         return name;
     }
 
     /**
      * Renamte column_1 => column if sorting by this suffix was success
      * 
      * @param key
      * @return
      */
     private String changeColumnName(String key) {
         if (mSuccesfullSortByUnderscore) {
             StringBuilder sb = new StringBuilder();
             String[] split = key.split("_");
             if (split.length > 1) {
                 for (int i = 0; i < split.length - 1; i++) {
                     // omit last order value
                     sb.append(split[i] + "_");
                 }
                 return key.substring(0, sb.length() - 1);
             }
         }
         return key;
     }
 
     /**
      * Called when column is created. <br>
      * Define if column is sortable
      * 
      * @param key
      * @return
      */
     protected boolean isColumnSortable(String key) {
         return true;
     }
 
     /**
      * Create column for particular column
      * 
      * @param key
      * @param dataExample
      *            firts row value of column
      * @return
      */
     protected Column<HashMap<String, Object>, String> onCreateColumn(final String key, Object dataExample) {
 
         Column<HashMap<String, Object>, String> column = new Column<HashMap<String, Object>, String>(getCell(key,
                 dataExample)) {
             // get value to show user
             @Override
             public String getValue(HashMap<String, Object> object) {
                 return getValueToShow(key, object.get(key));
             }
 
             // render value
             @Override
             public void render(Context context, HashMap<String, Object> object, SafeHtmlBuilder sb) {
                 boolean render = onColumnRender(key, this, context, object, sb);
                 if (!render) {
                     super.render(context, object, sb);
                 }
             }
         };
         return column;
     }
 
     protected Cell<String> getCell(final String key, Object dataExample) {
         return new TextCell();
     }
 
     /**
      * Way how to override columnRender
      * 
      * @param name
      * @param column
      * @param context
      * @param object
      * @param sb
      * @return true if render was overrided, false means default render
      */
     protected boolean onColumnRender(String name, Column<HashMap<String, Object>, String> column, Context context,
             HashMap<String, Object> object, SafeHtmlBuilder sb) {
         if (mOverrider != null) {
             return mOverrider.onColumnRender(name, column, context, object, sb);
         }
         return false;
     }
 
     /**
      * Called when data are transforemd into text value to table<br>
      * Now you can handle formating, etc...
      * 
      * @param columnName
      * @param o
      * @return string value to show user
      */
     protected String getValueToShow(String columnName, Object o) {
         if (o == null) {
             return "";
         } else {
             return String.valueOf(o);
         }
     }
 
     /**
      * Generate value comparator<br>
      * It's only neccessary if column is sortable
      * 
      * @param key
      *            columnName
      * @param dataExample
      *            example of data
      * @return
      */
     protected Comparator<HashMap<String, Object>> getComparator(final String key, Object dataExample) {
         return new Comparator<HashMap<String, Object>>() {
             @Override
             public int compare(HashMap<String, Object> o1, HashMap<String, Object> o2) {
                 return String.valueOf(o1.get(key)).compareTo(String.valueOf(o2.get(key)));
             }
         };
     }
 
     /**
      * Notify object to refresh view after datachange
      */
     public void notifyDataChange() {
         mListDataProvider.refresh();
     }
 
     public void addData(Collection<HashMap<String, Object>> data) {
        mListDataProvider.getList().addAll(data);
         notifyDataChange();
     }
 
     public void setLoadListener(LazyPager.OnPageLoaderListener loadListener) {
         pager.setLoadListener(loadListener);
     }
 
     public CheckBox getFilterCheckBox() {
         return mFilterCheckBox;
     }
 
     public Button getFilterButton() {
         return mFilterButton;
     }
 
     public void clear() {
         mListDataProvider.getList().clear();
         notifyDataChange();
     }
 
     public Button getReloadButton() {
         return mReloadButton;
     }
 }
