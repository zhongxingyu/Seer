 /**
  * Created On: 25-Jun-07 5:07:47 PM
  * $Id$
  */
 package com.thinkparity.ophelia.browser.platform.action.container;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.text.MessageFormat;
 
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
  * <b>Title:</b>thinkParity Ophelia UI Export Audit Report<br>
  * <b>Description:</b>An action to export an audit report for a container.<br>
  * 
  * @author raymond@thinkparity.com
  * @version 1.1.2.1
  */
 public final class ExportAuditReport extends AbstractBrowserAction {
 
     /**
      * Generate a directory name for the report.
      * 
      * @param container
      *            A <code>Container</code>.
      * @return A directory name <code>String</code>.
      */
     private static String exportFileName(final Container container) {
         return MessageFormat.format("{0}.pdf", container.getName());
     }
 
     /** The browser application. */
     private final Browser browser;
 
     /**
      * Create ExportAuditReport.
      * 
      * @param browser
      *            The thinkParity browser application.
      */
     public ExportAuditReport(final Browser browser) {
         super(ActionId.CONTAINER_EXPORT_AUDIT_REPORT);
         this.browser = browser;
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.platform.action.AbstractAction#invoke(com.thinkparity.ophelia.browser.platform.action.Data)
      * 
      */
     @Override
     public void invoke(final Data data) {
         final Long containerId = (Long) data.get(DataKey.CONTAINER_ID);
         final ContainerModel containerModel = getContainerModel();
         final Container container = containerModel.read(containerId);
         final File file = new File(Constants.Directories.USER_DATA, exportFileName(container));
         if (file.exists()) {
             if (browser.confirm("AuditReport.ConfirmOverwrite", new Object[] {file.getName()})) {
                 if (file.delete()) {
                     exportAuditReport(containerModel, file, container);
                     openAuditReport(file);
                 } else {
                     browser.displayErrorDialog("AuditReport.CannotDelete",
                             new Object[] {file.getName()});
                 }
             }
         } else {
             exportAuditReport(containerModel, file, container);
         }
     }
 
     /**
      * Export an audit report for the container.
      * 
      * @param containerModel
      *            A <code>ContainerModel</code>.
      * @param file
      *            A <code>File</code>.
      * @param containerId
      *            A container id <code>Long</code>.
      */
     private void exportAuditReport(final ContainerModel containerModel,
             final File file, final Container container) {
         try {
             final OutputStream outputStream = new FileOutputStream(file);
             try {
                 containerModel.exportAuditReport(outputStream, container.getId());
             } finally {
                 try {
                     outputStream.flush();
                 } finally {
                     outputStream.close();
                 }
             }
         } catch (final IOException iox) {
             browser.displayErrorDialog("AuditReport.CannotExport",
                     new Object[] {container.getName()});
         }
     }
 
     /**
      * Open the audit report.
      * 
      * @param file
      *            A <code>File</code>.
      */
     private void openAuditReport(final File file) {
         try {
             DesktopUtil.open(file);
         } catch (final DesktopException dx) {
             logger.logError(dx, "Cannot open file {0}.", file);
         }
     }
 
     public enum DataKey { CONTAINER_ID }
 }
