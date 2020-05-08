 
 /**
  * Created On: 17-Jul-06 1:57:42 PM
  */
 package com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.container;
 
 import java.awt.Component;
 import java.awt.event.MouseEvent;
 
 import javax.swing.ImageIcon;
 import javax.swing.JPopupMenu;
 import javax.swing.border.Border;
 
 import com.thinkparity.codebase.model.document.Document;
 
 import com.thinkparity.ophelia.browser.Constants.InsetFactors;
 import com.thinkparity.ophelia.browser.application.browser.Browser;
 import com.thinkparity.ophelia.browser.application.browser.component.MenuFactory;
 import com.thinkparity.ophelia.browser.application.browser.display.avatar.main.MainCellImageCacheTest.TabCellIconTest;
 import com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.DefaultTabCell;
 import com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.TabCell;
 import com.thinkparity.ophelia.browser.platform.Platform.Connection;
 import com.thinkparity.ophelia.browser.platform.action.ActionId;
 import com.thinkparity.ophelia.browser.platform.action.Data;
 import com.thinkparity.ophelia.browser.platform.action.document.OpenVersion;
 import com.thinkparity.ophelia.browser.platform.action.document.PrintVersion;
 import com.thinkparity.ophelia.browser.util.ArtifactUtil;
 
 /**
  * @author rob_masako@shaw.ca, raykroeker@gmail.com
  * @version 1.1.2.1
  */
 public class ContainerVersionDocumentCell extends DefaultTabCell {
 
     /** The document's version. */
     private final ContainerVersionCell version;
     
     /** The document associated with this cell. */
     private Document document;
     
     /** Create a CellDocument. */
     public ContainerVersionDocumentCell(final ContainerVersionCell version, final Document document) {
         this.document = new Document(document.getCreatedBy(), document.getCreatedOn(), document.getDescription(),
                 document.getFlags(), document.getUniqueId(), document.getName(), document.getUpdatedBy(),
                 document.getUpdatedOn());
         this.document.setId(document.getId());
         this.document.setRemoteInfo(document.getRemoteInfo());
         this.document.setState(document.getState());
         this.version = version;
     }
 
     /**
      * @see com.thinkparity.codebase.model.artifact.Artifact#equals(java.lang.Object)
      */
     @Override
     public boolean equals(final Object obj) {
         if (null != obj && obj instanceof ContainerVersionDocumentCell) {
             return ((ContainerVersionDocumentCell) obj).document.equals(document) &&
                    ((ContainerVersionDocumentCell) obj).version.equals(version);
         }
         return false;
     }
     
     /**
      * Get the document Id.
      * 
      * @return The document id.
      */
     public Long getId() {
         return document.getId();
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.TabCell#getBorder(int)
      */
     public Border getBorder(final int index, final Boolean isFirstInGroup, final Boolean lastCell) {
         return getParent().getBorder(index, isFirstInGroup, lastCell);
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.TabCell#getParent()
      * 
      */
     public TabCell getParent() {
         return version;
     }
     
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.TabCell#getSecondNodeIcon()
      */
     public ImageIcon getSecondNodeIcon() {
         final String extension = ArtifactUtil.getNameExtension(document);
         if (extension.equalsIgnoreCase(".DOC")) {
             return imageCacheTest.read(TabCellIconTest.DOCUMENT_WORD); 
         } else if (extension.equalsIgnoreCase(".XLS")) {
             return imageCacheTest.read(TabCellIconTest.DOCUMENT_EXCEL);  
         } else {
             return imageCacheTest.read(TabCellIconTest.DOCUMENT_NOTEPAD); 
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.TabCell#getTextNoClipping(com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.TabCell.TextGroup)
      *
      */
     public String getTextNoClipping(TextGroup textGroup) {
         if (textGroup == TextGroup.MAIN_TEXT) {
             return document.getName();
         } else {
             return null;
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.TabCell#getTextInsetFactor()
      */
     public Float getTextInsetFactor() {
        return InsetFactors.LEVEL_1;
     }
 
     /**
      * @see com.thinkparity.codebase.model.artifact.Artifact#hashCode()
      */
     @Override
     public int hashCode() {
         return toString().hashCode();
     }
 
     /**
      * @see java.lang.Object#toString()
      */
     @Override
     public String toString() {
         return new StringBuffer(getClass().getName()).append("//")
             .append(document.getId()).append("/")
             .append(version.getArtifactId()).append("/")
             .append(version.getVersionId())
             .toString();
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.TabCell#triggerPopup(com.thinkparity.ophelia.browser.platform.Platform.Connection, java.awt.Component, java.awt.event.MouseEvent, int, int)
      */
     public void triggerPopup(final Connection connection,
             final Component invoker, final MouseEvent e) {
         final JPopupMenu jPopupMenu = MenuFactory.createPopup();
 
         final Data openData = new Data(2);
         openData.set(OpenVersion.DataKey.DOCUMENT_ID, document.getId());
         openData.set(OpenVersion.DataKey.VERSION_ID, ((ContainerVersionCell)getParent()).getVersionId());
         jPopupMenu.add(popupItemFactory.createPopupItem(ActionId.DOCUMENT_OPEN_VERSION, openData));
         
         jPopupMenu.addSeparator();
         
         final Data printData = new Data(2);
         printData.set(PrintVersion.DataKey.DOCUMENT_ID, document.getId());
         openData.set(PrintVersion.DataKey.VERSION_ID, ((ContainerVersionCell)getParent()).getVersionId());
         jPopupMenu.add(popupItemFactory.createPopupItem(ActionId.DOCUMENT_PRINT_VERSION, printData));
         jPopupMenu.show(invoker, e.getX(), e.getY());
     }
     
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.TabCell#triggerDoubleClickAction(com.thinkparity.ophelia.browser.application.browser.Browser)
      */
     public void triggerDoubleClickAction(Browser browser) {  
         browser.runOpenDocumentVersion(document.getId(), ((ContainerVersionCell)getParent()).getVersionId());
     }
 }
