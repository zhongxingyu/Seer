 /**
  * Created On: 1-Sep-06 3:00:09 PM
  * $Id$
  */
 package com.thinkparity.ophelia.browser.platform.action.container;
 
 
 import java.io.File;
 import java.text.MessageFormat;
 
 import com.thinkparity.codebase.FileUtil;
 
 import com.thinkparity.codebase.model.container.Container;
 
 import com.thinkparity.ophelia.model.container.ContainerModel;
 
 import com.thinkparity.ophelia.browser.Constants;
 import com.thinkparity.ophelia.browser.application.browser.Browser;
 import com.thinkparity.ophelia.browser.platform.action.AbstractBrowserAction;
 import com.thinkparity.ophelia.browser.platform.action.ActionId;
 import com.thinkparity.ophelia.browser.platform.action.Data;
 import com.thinkparity.ophelia.browser.util.jdic.DesktopUtil;
 
 import org.jdesktop.jdic.desktop.DesktopException;
 
 /**
  * @author rob_masako@shaw.ca
  * @version $Revision$
  */
 public class Export extends AbstractBrowserAction {
 
     /**
      * Generate a directory name for export.
      * 
      * @param container
      *            A <code>Container</code>.
      * @return A directory name <code>String</code>.
      */
     private static String exportDirectoryName(final Container container) {
         return MessageFormat.format("{0}", container.getName());
     }
 
     /** The browser application. */
     private final Browser browser;
 
     /**
      * Create Export.
      * 
      * @param browser
      *            The thinkParity browser application.
      */
     public Export(final Browser browser) {
         super(ActionId.CONTAINER_EXPORT);
         this.browser = browser;
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.platform.action.AbstractAction#invoke(com.thinkparity.ophelia.browser.platform.action.Data)
      */
     @Override
     public void invoke(final Data data) {
         final Long containerId = (Long) data.get(DataKey.CONTAINER_ID);
         final ContainerModel containerModel = getContainerModel();
         final Container container = containerModel.read(containerId);
         final File file = new File(Constants.Directories.USER_DATA, exportDirectoryName(container));
         if (file.exists()) {
             if (browser.confirm("Export.ConfirmOverwrite", new Object[] {file.getName()})) {
                 try {
                     FileUtil.deleteTree(file);
                     export(containerModel, file, container);
                     openExportRoot(file);
                 } catch (final Throwable t) {
                     browser.displayErrorDialog("Export.CannotDelete",
                             new Object[] {file.getName()});
                 }
             }
         } else {
             export(containerModel, file, container);
            openExportRoot(file);
         }
     }
 
     /**
      * Export a container via the model to the file.
      * 
      * @param containerModel
      *            A <code>ContainerModel</code>.
      * @param exportRoot
      *            An export root directory <code>File</code>.
      * @param containerId
      *            A container id <code>Long</code>.
      */
     private void export(final ContainerModel containerModel,
             final File exportRoot, final Container container) {
         containerModel.export(exportRoot, container.getId());
     }
 
     /**
      * Open the export root directory.
      * 
      * @param exportRoot
      *            An export root directory <code>File</code>.
      */
     private void openExportRoot(final File exportRoot) {
         try {
             DesktopUtil.open(exportRoot);
         } catch (final DesktopException dx) {
             logger.logError(dx, "Cannot open file {0}.", exportRoot);
         }
     }
 
     public enum DataKey { CONTAINER_ID }
 }
