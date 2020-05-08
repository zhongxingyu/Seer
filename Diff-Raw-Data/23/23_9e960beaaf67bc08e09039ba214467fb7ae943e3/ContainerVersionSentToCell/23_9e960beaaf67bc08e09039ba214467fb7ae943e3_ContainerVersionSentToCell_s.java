 /**
  * Created On: 13-Sep-06 4:09:05 PM
  * $Id$
  */
 package com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.container;
 
 import javax.swing.ImageIcon;
 import javax.swing.border.Border;
 
 import com.thinkparity.ophelia.browser.Constants.InsetFactors;
 import com.thinkparity.ophelia.browser.application.browser.display.avatar.main.MainCellImageCacheTest.TabCellIconTest;
 import com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.DefaultTabCell;
 import com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.TabCell;
 import com.thinkparity.ophelia.browser.util.localization.MainCellL18n;
 
 /**
  * @author rob_masako@shaw.ca
  * @version $Revision$
  */
 public class ContainerVersionSentToCell extends DefaultTabCell {
        
     /** The version. */
     private final ContainerVersionCell version;
     
     /** The number of users (child cells) */
     private Integer numberOfUsers = 0;
     
     /** The draft cell localization. */
     private final MainCellL18n localization; 
 
     /** Create ContainerVersionSentToCell. */
     public ContainerVersionSentToCell(final ContainerVersionCell version) {      
         this.version = version;
         this.localization = new MainCellL18n("MainCellContainerVersionSentTo");
     }
     
     /**
      * @see com.thinkparity.codebase.model.artifact.Artifact#equals(java.lang.Object)
      * 
      */
     public boolean equals(final Object obj) {
         if (null != obj && obj instanceof ContainerVersionSentToCell) {
             return ((ContainerVersionSentToCell) obj).version.equals(version);
         }
         return false;
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.TabCell#getBorder(int)
      * 
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
         if (isExpanded()) {
             return imageCacheTest.read(TabCellIconTest.FOLDER_OPEN); 
         } else {
             return imageCacheTest.read(TabCellIconTest.FOLDER_CLOSED); 
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.TabCell#getTextNoClipping(com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.TabCell.TextGroup)
      *
      */
     public String getTextNoClipping(TextGroup textGroup) {
         if (textGroup == TextGroup.MAIN_TEXT) {
             final Integer number = numberOfUsers;
             return localization.getString("Text", new Object[] {number});
         } else {
             return null;
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.TabCell#getTextInsetFactor()
      * 
      */
     public Float getTextInsetFactor() {
        return InsetFactors.LEVEL_2;
     }
 
     /**
      * @see com.thinkparity.codebase.model.artifact.Artifact#hashCode()
      * 
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
             .append(version.getArtifactId()).append("/")
             .append(version.getVersionId())
             .toString();
     }
     
     /**
      * Set the number of child users.
      * 
      * @param users
      *            The number of users.
      */
     public void setNumberOfUsers(final Integer numberOfUsers) {
         this.numberOfUsers = numberOfUsers;
     }
     
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.TabCell#isChildren()
      */
     public Boolean isChildren() {
         return Boolean.TRUE;
     }  
 }
