 package org.iplantc.de.client.viewer.views;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.iplantc.core.jsonutil.JsonUtil;
 import org.iplantc.core.uicommons.client.ErrorHandler;
 import org.iplantc.core.uicommons.client.models.diskresources.File;
 import org.iplantc.de.client.I18N;
 import org.iplantc.de.client.Services;
 import org.iplantc.de.client.viewer.models.StructuredText;
 import org.iplantc.de.client.viewer.models.StructuredTextAutoBeanFactory;
 
 import com.google.gwt.core.shared.GWT;
 import com.google.gwt.http.client.URL;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONString;
 import com.google.gwt.json.client.JSONValue;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.web.bindery.autobean.shared.AutoBean;
 import com.google.web.bindery.autobean.shared.AutoBeanCodex;
 import com.google.web.bindery.autobean.shared.Splittable;
 import com.google.web.bindery.autobean.shared.impl.StringQuoter;
 import com.sencha.gxt.core.client.ValueProvider;
 import com.sencha.gxt.core.client.dom.ScrollSupport.ScrollMode;
 import com.sencha.gxt.core.client.util.Margins;
 import com.sencha.gxt.data.shared.ListStore;
 import com.sencha.gxt.data.shared.ModelKeyProvider;
 import com.sencha.gxt.widget.core.client.ContentPanel;
 import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
 import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer.BorderLayoutData;
 import com.sencha.gxt.widget.core.client.container.MarginData;
 import com.sencha.gxt.widget.core.client.container.SimpleContainer;
 import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
 import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
 import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
 import com.sencha.gxt.widget.core.client.grid.ColumnModel;
 import com.sencha.gxt.widget.core.client.grid.Grid;
 import com.sencha.gxt.widget.core.client.grid.filters.GridFilters;
 import com.sencha.gxt.widget.core.client.grid.filters.StringFilter;
 
 public class StrcturedTextViewerImpl extends AbstractTextViewer {
 
     private final class StructuredTextValueProvider implements ValueProvider<JSONObject, String> {
         private final int index;
 
         private StructuredTextValueProvider(int index) {
             this.index = index;
         }
 
         @Override
         public String getValue(JSONObject object) {
             JSONValue val = object.get(index + "");
             if (val != null) {
                 return val.isString().stringValue();
             } else {
                 return "";
             }
         }
 
         @Override
         public void setValue(JSONObject object, String value) {
             // TODO Auto-generated method stub
 
         }
 
         @Override
         public String getPath() {
             return index + "";
         }
     }
 
     private Grid<JSONObject> grid;
     private TextViewPagingToolBar toolbar;
     private ListStore<JSONObject> store;
     private BorderLayoutContainer container;
     private ContentPanel north;
     private VerticalLayoutContainer center;
     private final String COMMA_SEPARATOR = ",";
     private final String TAB_SEPARATOR = "\t";
     private StructuredText text_bean;
     private List<JSONObject> skippedRows;
 
     private final StructuredTextAutoBeanFactory factory = GWT
             .create(StructuredTextAutoBeanFactory.class);
     private boolean hasHeader;
     private JSONObject headerRow;
 
     public StrcturedTextViewerImpl(File file, String infoType) {
         super(file, infoType);
         initLayout();
         initToolbar();
         loadData();
     }
 
     private void initLayout() {
         container = new BorderLayoutContainer();
         north = new ContentPanel();
         north.setCollapsible(false);
         north.setHeaderVisible(false);
 
         center = new VerticalLayoutContainer();
         center.setScrollMode(ScrollMode.AUTO);
         container.setNorthWidget(north, getNorthData());
         container.setCenterWidget(center, getCenterData());
     }
 
     private void initToolbar() {
         toolbar = new TextViewPagingToolBar(this, infoType, false);
         north.add(toolbar);
     }
 
     private BorderLayoutData getNorthData() {
         BorderLayoutData northData = new BorderLayoutData(30);
         northData.setMargins(new Margins(5));
         northData.setCollapsible(false);
         northData.setSplit(false);
         return northData;
     }
 
     private MarginData getCenterData() {
         return new MarginData();
     }
 
     @Override
     public void loadData() {
         String url = "read-csv-chunk";
         container.mask(I18N.DISPLAY.loadingMask());
         Services.FILE_EDITOR_SERVICE.getDataChunk(url, getRequestBody(), new AsyncCallback<String>() {
 
             @Override
             public void onSuccess(String result) {
                 AutoBean<StructuredText> bean = AutoBeanCodex.decode(factory, StructuredText.class,
                         result);
                 text_bean = bean.as();
                 if (grid == null) {
                     initGrid(Integer.parseInt(text_bean.getMaxColumns()));
                 }
                 Splittable sp = StringQuoter.split(result);
                 setData(sp);
                 container.unmask();
             }
 
             @Override
             public void onFailure(Throwable caught) {
                 ErrorHandler.post(I18N.ERROR.unableToRetrieveFileData(file.getName()), caught);
                 container.unmask();
             }
         });
     }
 
     private void initGrid(int columns) {
         List<ColumnConfig<JSONObject, ?>> configs = new ArrayList<ColumnConfig<JSONObject, ?>>();
         GridFilters<JSONObject> filters = new GridFilters<JSONObject>();
         if (columns > 0) {
             for (int i = 0; i < columns; i++) {
                 final int index = i;
                 StructuredTextValueProvider valueProvider = new StructuredTextValueProvider(index);
                 ColumnConfig<JSONObject, String> col = new ColumnConfig<JSONObject, String>(
                         valueProvider);
                 col.setHeader(index + "");
                 StringFilter<JSONObject> strFilter = new StringFilter<JSONObject>(valueProvider);
                 filters.addFilter(strFilter);
                 configs.add(col);
             }
         }
 
         grid = new Grid<JSONObject>(getStore(), new ColumnModel<JSONObject>(configs));
         grid.getView().setStripeRows(true);
         filters.initPlugin(grid);
         filters.setLocal(true);
         grid.setHeight(center.getOffsetHeight(true));
         center.add(grid, new VerticalLayoutData(1, 1, new Margins(0)));
     }
 
     private ListStore<JSONObject> getStore() {
         if (store == null) {
             store = new ListStore<JSONObject>(new ModelKeyProvider<JSONObject>() {
 
                 private int index;
 
                 @Override
                 public String getKey(JSONObject item) {
                     return index++ + "";
                 }
 
             });
         }
 
         return store;
     }
 
     private JSONObject getRequestBody() {
         JSONObject obj = new JSONObject();
         obj.put("path", new JSONString(file.getId()));
         obj.put("separator", new JSONString(getSeparator()));
        // position starts at 0
        if (text_bean == null) {
            obj.put("position", new JSONString("0"));
        } else {
            int end = Integer.parseInt(text_bean.getEndPosition());
            obj.put("position", new JSONString((end + 1) + ""));
        }
         obj.put("chunk-size", new JSONString("" + toolbar.getPageSize()));
         return obj;
     }
 
     private String getSeparator() {
         if (infoType.equalsIgnoreCase("csv")) {
             return COMMA_SEPARATOR;
         } else if (infoType.equalsIgnoreCase("tsv") || infoType.equalsIgnoreCase("vcf")
                 || infoType.equalsIgnoreCase("gff")) {
             return URL.encode(TAB_SEPARATOR);
         } else {
             return " ";
         }
     }
 
     @Override
     public void setData(Object data) {
         Splittable textData = (Splittable)data;
         JSONObject obj = JsonUtil.getObject(textData.getPayload());
         JSONArray arr = obj.get("csv").isArray();
 
         if (arr != null && arr.size() > 0) {
             for (int i = 0; i < arr.size(); i++) {
                 store.add(arr.get(i).isObject());
             }
         }
 
         if (toolbar.getPageNumber() == 1 && headerRow == null) {
             defineColumnHeader();
         }
 
     }
 
     private void defineColumnHeader() {
         ColumnModel<JSONObject> cm = grid.getColumnModel();
         List<ColumnConfig<JSONObject, ?>> configs = cm.getColumns();
         JSONObject firstRow = store.get(0);
         int index = 0;
         for (ColumnConfig<JSONObject, ?> conf : configs) {
             JSONString string = (firstRow.get(index + "") != null) ? firstRow.get(index + "").isString()
                     : null;
             if (hasHeader) {
                 conf.setHeader((string != null) ? string.stringValue() : index + "");
             } else {
                 conf.setHeader(index + "");
             }
             index++;
         }
 
         // converted first row to header. so remove first row
         if (hasHeader) {
             store.remove(firstRow);
             headerRow = firstRow;
         } else {
             if (headerRow != null) {
                 // if it was removed prev, add the back row at 1st position
                 store.add(0, headerRow);
                 headerRow = null;
             }
         }
 
         grid.reconfigure(store, cm);
         grid.getView().refresh(true);
     }
 
     @Override
     public Widget asWidget() {
         SimpleContainer widget = new SimpleContainer();
         widget.add(container);
         return widget;
     }
 
     @Override
     public void loadDataWithHeader(boolean header) {
         hasHeader = header;
         defineColumnHeader();
     }
 
     @Override
     public String getViewName() {
         return "Tabular View: " + file.getName();
     }
 
     @Override
     public void skipRows(int val) {
         if (val > 0 && val < store.size() + ((skippedRows != null) ? skippedRows.size() : 0)) {
             if (skippedRows == null) {
                 skippedRows = new ArrayList<JSONObject>();
                 // increment
                 skippedRows.addAll(store.subList(0, val));
             } else if (skippedRows.size() > val) {
                 // reduction
                 for (int j = skippedRows.size() - 1; j >= val; j--) {
                     store.add(0, skippedRows.remove(j));
                 }
             } else if (skippedRows.size() < val) {
                 // increment
                 skippedRows.addAll(store.subList(0, val - skippedRows.size()));
             }
             for (JSONObject obj : skippedRows) {
                 store.remove(obj);
             }
 
             // redefine column headers if necessary
             if (hasHeader) {
                 defineColumnHeader();
             }
 
         } else {
             if (val == 0 && skippedRows != null && skippedRows.size() > 0) {
                 // add back the skipped rows.
                 for (int j = 0; j < skippedRows.size(); j++) {
                     store.add(j, skippedRows.get(j));
                 }
             }
             skippedRows = null;
         }
 
     }
 }
