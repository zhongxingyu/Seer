 /**
  *
  */
 package org.iplantc.de.client.viewer.views;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.iplantc.core.jsonutil.JsonUtil;
 import org.iplantc.core.uicommons.client.ErrorHandler;
 import org.iplantc.core.uicommons.client.events.EventBus;
 import org.iplantc.core.uicommons.client.models.diskresources.File;
 import org.iplantc.core.uidiskresource.client.views.dialogs.SaveAsDialog;
 import org.iplantc.de.client.I18N;
 import org.iplantc.de.client.Services;
 import org.iplantc.de.client.services.impl.FileSaveCallback;
 import org.iplantc.de.client.viewer.events.SaveFileEvent;
 import org.iplantc.de.client.viewer.events.SaveFileEvent.SaveFileEventHandler;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.core.client.JavaScriptObject;
 import com.google.gwt.event.logical.shared.ResizeEvent;
 import com.google.gwt.event.logical.shared.ResizeHandler;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONString;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiTemplate;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Widget;
 import com.sencha.gxt.core.client.dom.XElement;
 import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
 import com.sencha.gxt.widget.core.client.container.SimpleContainer;
 import com.sencha.gxt.widget.core.client.event.SelectEvent;
 import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
 
 /**
  * @author sriram
  * 
  */
 public class TextViewerImpl extends AbstractFileViewer implements EditingSupport {
 
     private static TextViewerUiBinder uiBinder = GWT.create(TextViewerUiBinder.class);
 
     @UiTemplate("TextViewer.ui.xml")
     interface TextViewerUiBinder extends UiBinder<Widget, TextViewerImpl> {
     }
 
     private final Widget widget;
 
     @UiField
     SimpleContainer center;
 
     @UiField
     BorderLayoutContainer con;
 
     @UiField(provided = true)
     TextViewPagingToolBar toolbar;
 
     private long file_size;
 
     private int totalPages;
 
     private String data;
 
     protected boolean editing;
 
     private Presenter presenter;
 
     protected JavaScriptObject jso;
     
     private final List<HandlerRegistration> eventHandlers = new ArrayList<HandlerRegistration>();
 
     public TextViewerImpl(File file, String infoType, boolean editing) {
         super(file, infoType);
         this.editing = editing;
         toolbar = initToolBar();
         widget = uiBinder.createAndBindUi(this);
 
         addWrapHandler();
 
         if (file != null) {
             loadData();
         } else {
             setData("");
         }
 
         center.addResizeHandler(new ResizeHandler() {
 
             @Override
             public void onResize(ResizeEvent event) {
                 if (jso != null) {
                     resizeDisplay(jso, center.getElement().getOffsetWidth(), center.getElement()
                             .getOffsetHeight());
                 }
             }
         });
 
      // handle data events
         eventHandlers.add(EventBus.getInstance().addHandler(SaveFileEvent.TYPE, new SaveFileEventHandler() {
 
             @Override
             public void onSave(SaveFileEvent event) {
                 save();
             }
         }
 
         ));
 
     }
 
     TextViewPagingToolBar initToolBar() {
         TextViewPagingToolBar textViewPagingToolBar = new TextViewPagingToolBar(this, editing);
         textViewPagingToolBar.addHandler(new SaveFileEventHandler() {
 
             @Override
             public void onSave(SaveFileEvent event) {
                 save();
 
             }
 
         }, SaveFileEvent.TYPE);
         return textViewPagingToolBar;
     }
     
     
     @Override
     public void cleanUp() {
     	 EventBus eventBus = EventBus.getInstance();
          for (HandlerRegistration hr : eventHandlers) {
              eventBus.removeHandler(hr);
          }
          file = null;
          jso = null;
          toolbar = null;
          data = null;
     }
 
     private void addWrapHandler() {
         toolbar.addWrapCbxChangeHandler(new ValueChangeHandler<Boolean>() {
 
             @Override
             public void onValueChange(ValueChangeEvent<Boolean> event) {
                setData(data);
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
 
     @Override
     public void loadData() {
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
 
     @Override
     public Widget asWidget() {
         return widget;
     }
 
     @Override
     public void setPresenter(Presenter p) {
         this.presenter = p;
     }
 
     @Override
     public void setData(Object data) {
         clearDisplay();
         boolean allowEditing = toolbar.getToltalPages() == 1 &&  editing;
 		jso = displayData(this, center.getElement(), infoType, (String)data, center.getElement()
                 .getOffsetWidth(), center.getElement().getOffsetHeight(), toolbar.isWrapText(),
                 allowEditing);
         toolbar.setEditing(allowEditing);
         /**
          * XXX - SS - support editing for files with only one page
          */
     }
 
     protected void clearDisplay() {
         center.getElement().removeChildren();
         center.forceLayout();
     }
 
     @Override
     public void setDirty(Boolean dirty) {
         if (presenter.isDirty() == dirty) {
             return;
         }
         presenter.setVeiwDirtyState(dirty);
     }
 
     public static native JavaScriptObject displayData(final TextViewerImpl instance, XElement textArea,
             String mode, String val, int width, int height, boolean wrap, boolean editing) /*-{
 		var myCodeMirror = $wnd.CodeMirror(textArea, {
 			value : val,
 			mode : mode
 		});
 		myCodeMirror.setOption("lineWrapping", wrap);
 		myCodeMirror.setSize(width, height);
 		myCodeMirror.setOption("readOnly", !editing);
 		if (editing) {
 			myCodeMirror
 					.on(
 							"change",
 							$entry(function() {
 								instance.@org.iplantc.de.client.viewer.views.TextViewerImpl::setDirty(Ljava/lang/Boolean;)(@java.lang.Boolean::TRUE);
 							}));
 		}
 		return myCodeMirror;
     }-*/;
 
     public static native String getEditorContent(JavaScriptObject jso) /*-{
 		return jso.getValue();
     }-*/;
 
     public static native boolean isClean(JavaScriptObject jso) /*-{
 		return jso.isClean();
     }-*/;
 
     public static native void resizeDisplay(JavaScriptObject jso, int width, int height) /*-{
 		jso.setSize(width, height);
     }-*/;
 
     @Override
     public void save() {
         con.mask("Saving...");
         if (file == null) {
             final SaveAsDialog saveDialog = new SaveAsDialog();
             saveDialog.addOkButtonSelectHandler(new SelectHandler() {
 
                 @Override
                 public void onSelect(SelectEvent event) {
                     String destination = saveDialog.getSelectedFolder().getPath() + "/"
                             + saveDialog.getFileName();
                     Services.FILE_EDITOR_SERVICE.uploadTextAsFile(destination, getEditorContent(jso),
                             true, new FileSaveCallback(destination, true, con));
                 }
             });
             saveDialog.addCancelButtonSelectHandler(new SelectHandler() {
 
                 @Override
                 public void onSelect(SelectEvent event) {
                     con.unmask();
                 }
             });
             saveDialog.show();
             saveDialog.toFront();
         } else {
             Services.FILE_EDITOR_SERVICE.uploadTextAsFile(file.getPath(), getEditorContent(jso), false,
                     new FileSaveCallback(file.getPath(), false, con));
         }
     }
 
     @Override
     public boolean isDirty() {
         return isClean(jso);
     }
 }
