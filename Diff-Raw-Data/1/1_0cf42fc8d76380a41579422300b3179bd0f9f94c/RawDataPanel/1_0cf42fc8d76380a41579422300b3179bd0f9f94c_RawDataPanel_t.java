 package org.iplantc.de.client.views.panels;
 
 import java.util.List;
 import java.util.Map;
 
 import org.iplantc.core.uidiskresource.client.models.File;
 import org.iplantc.core.uidiskresource.client.models.FileIdentifier;
 import org.iplantc.de.client.I18N;
 import org.iplantc.de.client.controllers.DataMonitor;
 
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.MessageBox;
 import com.extjs.gxt.ui.client.widget.form.TextArea;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.user.client.Element;
 
 /**
  * Provides a user interface for presenting raw data.
  * 
  * @author amuir
  * 
  */
 public class RawDataPanel extends ProvenanceContentPanel implements DataMonitor {
     private final String data;
     private TextArea areaData;
     private String textOrig = new String();
     private final MessageBox wait;
     private int tabIndex;
 
     /**
      * Instantiate from a file identifier, data and editable flag.
      * 
      * @param fileIdentifier file associated with this panel.
      * @param data data to display.
      * @param editable true if the user can edit this data.
      */
     public RawDataPanel(FileIdentifier fileIdentifier, String data) {
         super(fileIdentifier);
         this.data = data;
         
         wait = MessageBox.wait(I18N.DISPLAY.progress(), I18N.DISPLAY.fileSaveProgress(),
                 I18N.DISPLAY.saving() + "..."); //$NON-NLS-1$
         wait.close();
 
         buildTextArea();
 
         setTabIndex(0);
     }
 
     private void buildTextArea() {
        areaData = buildTextArea(false);
         areaData.setId("idRawDataField"); //$NON-NLS-1$
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void onRender(Element parent, int index) {
         super.onRender(parent, index);
 
         if (data != null) {
             textOrig = data;
             areaData.setValue(data);
             areaData.setWidth(getWidth());
 
             ContentPanel panel = new ContentPanel();
             panel.setHeaderVisible(false);
             panel.setLayout(new FitLayout());
             panel.setWidth(getWidth());
             panel.add(areaData);
 
             add(panel, centerData);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void afterRender() {
         super.afterRender();
         areaData.el().setElementAttribute("spellcheck", "false"); //$NON-NLS-1$ //$NON-NLS-2$
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String getTabHeader() {
         return I18N.DISPLAY.raw();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public int getTabIndex() {
         return tabIndex;
     }
 
     /**
      * Sets the desired tab position with the given index.
      * 
      * @param index desired tab position.
      */
     @Override
     public void setTabIndex(int index) {
         tabIndex = index;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void addFile(String idParentFolder, File info) {
         // intentionally do nothing
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void fileSavedAs(String idOrig, String idParent, File info) {
         wait.close();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void folderCreated(String idParentFolder, JSONObject jsonFolder) {
         // intentionally do nothing
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void fileRename(String id, String name) {
         // intentionally do nothing
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void folderRename(String id, String name) {
         // intentionally do nothing
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void deleteResources(List<String> folders, List<String> files) {
         // intentionally do nothing
     }
 
     @Override
     public void fileMove(Map<String, String> files) {
         // intentionally do nothing
     }
 
     @Override
     public void folderMove(Map<String, String> folders) {
         // intentionally do nothing
     }
 }
