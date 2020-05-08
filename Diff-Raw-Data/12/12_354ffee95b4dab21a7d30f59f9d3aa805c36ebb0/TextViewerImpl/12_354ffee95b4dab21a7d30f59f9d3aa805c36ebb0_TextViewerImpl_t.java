 /**
  *
  */
 package org.iplantc.de.client.viewer.views;
 
 import org.iplantc.core.jsonutil.JsonUtil;
 import org.iplantc.core.uicommons.client.ErrorHandler;
 import org.iplantc.core.uidiskresource.client.models.File;
 import org.iplantc.de.client.I18N;
 import org.iplantc.de.client.Services;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyDownEvent;
 import com.google.gwt.event.dom.client.KeyDownHandler;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONString;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiTemplate;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.TextArea;
 import com.google.gwt.user.client.ui.Widget;
 import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
 import com.sencha.gxt.widget.core.client.event.SelectEvent;
 import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
 
 /**
  * @author sriram
  * 
  */
 public class TextViewerImpl implements FileViewer {
 
     public static final int MIN_PAGE_SIZE_KB = 8;
     public static final int MAX_PAGE_SIZE_KB = 64;
     public static final int PAGE_INCREMENT_SIZE_KB = 8;
 
     private static TextViewerUiBinder uiBinder = GWT.create(TextViewerUiBinder.class);
 
     @UiTemplate("TextViewer.ui.xml")
     interface TextViewerUiBinder extends UiBinder<Widget, TextViewerImpl> {
     }
 
     private final Widget widget;
 
     @UiField
     TextArea textArea;
 
     @UiField
     BorderLayoutContainer con;
 
     @UiField
     TextViewPagingToolBar toolbar;
 
     private File file;
 
     private long file_size;
 
     private int totalPages;
 
     private String data;
 
     public TextViewerImpl(File file) {
         this.file = file;
         file_size = Long.parseLong(file.getSize());
         widget = uiBinder.createAndBindUi(this);
         textArea.getElement().setAttribute("wrap", "off");
         addWrapHandler();
         loadData();
         computeTotalPages();
 
         addFirstHandler();
 
         addPrevHandler();
 
         addNextHandler();
 
         addLastHandler();
 
         addPageSizeChangeHandler();
 
         addSelectPageKeyHandler();
     }
 
     private void computeTotalPages() {
         long pageSize = toolbar.getPageSize();
         if (file_size < pageSize) {
             totalPages = 1;
         } else {
             totalPages = (int)((file_size / pageSize));
             if (file_size % pageSize > 0) {
                 totalPages++;
             }
 
         }
 
         if (totalPages == 1) {
             toolbar.setFirstEnabled(false);
             toolbar.setNextEnabled(false);
             toolbar.setPrevEnabled(false);
             toolbar.setLastEnabled(false);
         }
 
         toolbar.setTotalPagesText(totalPages);
     }
 
     private void addWrapHandler() {
         toolbar.addWrapCbxChangeHandler(new ValueChangeHandler<Boolean>() {
 
             @Override
             public void onValueChange(ValueChangeEvent<Boolean> event) {
                 setData(data);
             }
         });
     }
 
     private void addPageSizeChangeHandler() {
         toolbar.addPageSizeChangeHandler(new ValueChangeHandler<Integer>() {
 
             @Override
             public void onValueChange(ValueChangeEvent<Integer> event) {
                 computeTotalPages();
                 toolbar.setPageNumber(1);
                 loadData();
 
             }
         });
     }
 
     private void addLastHandler() {
         toolbar.addLastSelectHandler(new SelectHandler() {
 
             @Override
             public void onSelect(SelectEvent event) {
                 toolbar.setPageNumber(totalPages);
                 toolbar.setLastEnabled(false);
                 toolbar.setNextEnabled(false);
 
                 toolbar.setFirstEnabled(true);
                 toolbar.setPrevEnabled(true);
                 loadData();
             }
         });
     }
 
     private void addNextHandler() {
         toolbar.addNextSelectHandler(new SelectHandler() {
 
             @Override
             public void onSelect(SelectEvent event) {
                 int temp = toolbar.getPageNumber() + 1;
                 toolbar.setPageNumber(temp);
 
                 if (temp == totalPages) {
                     toolbar.setLastEnabled(false);
                     toolbar.setNextEnabled(false);
                 }
 
                 toolbar.setFirstEnabled(true);
                 toolbar.setPrevEnabled(true);
                 loadData();
             }
         });
     }
 
     private void addPrevHandler() {
         toolbar.addPrevSelectHandler(new SelectHandler() {
 
             @Override
             public void onSelect(SelectEvent event) {
                 int temp = toolbar.getPageNumber() - 1;
                 toolbar.setPageNumber(temp);
                 // chk first page
                 if (temp - 1 == 0) {
                     toolbar.setFirstEnabled(false);
                     toolbar.setPrevEnabled(false);
                     toolbar.setLastEnabled(true);
                     toolbar.setNextEnabled(true);
                 }
                 loadData();
             }
         });
     }
 
     private void addFirstHandler() {
         toolbar.addFirstSelectHandler(new SelectHandler() {
 
             @Override
             public void onSelect(SelectEvent event) {
                 toolbar.setPageNumber(1);
                 toolbar.setFirstEnabled(false);
                 toolbar.setPrevEnabled(false);
                 toolbar.setLastEnabled(true);
                 toolbar.setNextEnabled(true);
                 loadData();
             }
         });
     }
 
     private JSONObject getRequestBody() {
         JSONObject obj = new JSONObject();
         obj.put("path", new JSONString(file.getId()));
         // position starts at 0
         obj.put("position", new JSONString("" + toolbar.getPageSize() * (toolbar.getPageNumber() - 1)));
         obj.put("chunk-size", new JSONString("" + toolbar.getPageSize()));
         return obj;
     }
 
     private void loadData() {
         String url = "read-chunk";
         con.mask(I18N.DISPLAY.loadingMask());
         Services.FILE_EDITOR_SERVICE.getDataChunk(url, getRequestBody(), new AsyncCallback<String>() {
 
             @Override
             public void onSuccess(String result) {
                 data = JsonUtil.getString(JsonUtil.getObject(result), "chunk");
                 setData(data);
                 con.unmask();
             }
 
             @Override
             public void onFailure(Throwable caught) {
                 ErrorHandler.post(I18N.ERROR.unableToRetrieveFileData(file.getName()), caught);
                 con.unmask();
             }
         });
 
     }
 
     private void addSelectPageKeyHandler() {
         toolbar.addSelectPageKeyHandler(new KeyDownHandler() {
 
             @Override
             public void onKeyDown(KeyDownEvent event) {
                 if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                     int pageNumber = toolbar.getPageNumber();
                     if (pageNumber <= totalPages && pageNumber > 0) {
                         toolbar.pageText.clearInvalid();
                         loadData();
                        if (pageNumber == 1) {
                            toolbar.setFirstEnabled(false);
                            toolbar.setPrevEnabled(false);
                            toolbar.setLastEnabled(true);
                            toolbar.setNextEnabled(true);
                        } else if (pageNumber == totalPages) {
                            toolbar.setLastEnabled(false);
                            toolbar.setNextEnabled(false);
                        } else {
                            toolbar.setPrevEnabled(true);
                            toolbar.setNextEnabled(true);
                        }
                     } else {
                         toolbar.pageText.markInvalid(I18N.DISPLAY.inValidPage());
                     }
                 }
 
             }
         });
     }
 
     @Override
     public Widget asWidget() {
         return widget;
     }
 
     @Override
     public void setPresenter(Presenter p) {/* Not Used */
     }
 
     @Override
     public void setData(Object data) {
         if (toolbar.isWrapText()) {
             textArea.getElement().setAttribute("wrap", "on");
         } else {
             textArea.getElement().setAttribute("wrap", "off");
         }
         textArea.setValue((String)data);
     }
 }
