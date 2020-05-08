 package org.iplantc.de.client.viewer.views;
 
 import org.iplantc.core.uicommons.client.models.diskresources.File;
 import org.iplantc.de.client.Services;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiTemplate;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Widget;
 import com.sencha.gxt.core.client.dom.ScrollSupport.ScrollMode;
 import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
 
 /**
  * 
  * 
  * @author sriram
  * 
  */
 public class ImageViewerImpl extends AbstractFileViewer {
 
     private static ImageViewerUiBinder uiBinder = GWT.create(ImageViewerUiBinder.class);
 
     private final Widget widget;
 
     @UiField(provided = true)
     Image img;
 
     @UiField
     VerticalLayoutContainer con;
 
    private File file;

     public ImageViewerImpl(File file) {
         super(file, null);
         img = new Image(Services.FILE_EDITOR_SERVICE.getServletDownloadUrl(this.file.getId()));
         widget = uiBinder.createAndBindUi(this);
         con.setScrollMode(ScrollMode.AUTO);
     }
 
     @UiTemplate("ImageViewer.ui.xml")
     interface ImageViewerUiBinder extends UiBinder<Widget, ImageViewerImpl> {
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
         // Do nothing intentionally
 
     }
 
     @Override
     public void loadData() {
         // Do nothing intentionally
     }
 }
