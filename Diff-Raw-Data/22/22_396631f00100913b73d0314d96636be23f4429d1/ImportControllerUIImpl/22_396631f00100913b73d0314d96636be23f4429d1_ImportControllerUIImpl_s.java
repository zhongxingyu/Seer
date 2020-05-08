 /*
  * This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
  * Copyright (C) 2011 Korean Bioinformation Center(KOBIC)
  * 
  * MONGKIE is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your processorUI) any later version.
  * 
  * MONGKIE is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  * 
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.mongkie.ui.importer;
 
 import java.awt.Component;
 import java.awt.Dialog;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.InvocationTargetException;
 import java.text.MessageFormat;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JComponent;
 import javax.swing.JFileChooser;
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import org.mongkie.importer.*;
 import org.mongkie.importer.spi.*;
 import org.mongkie.longtask.LongTask;
 import org.mongkie.longtask.LongTaskErrorHandler;
 import org.mongkie.longtask.LongTaskExecutor;
 import org.mongkie.longtask.progress.Progress;
 import org.mongkie.longtask.progress.ProgressTicket;
 import org.mongkie.ui.importer.attributes.ImportAttributesPanel;
 import org.mongkie.ui.importer.csv.ImportCSVEdgeTableWizardPanel;
 import org.mongkie.ui.importer.csv.ImportCSVNodeTableWizardPanel;
 import org.mongkie.ui.importer.csv.ImportCSVWizardPanel;
 import org.mongkie.visualization.VisualizationController;
 import org.netbeans.validation.api.ui.swing.ValidationPanel;
 import org.openide.DialogDescriptor;
 import org.openide.DialogDisplayer;
 import org.openide.NotifyDescriptor;
 import org.openide.WizardDescriptor;
 import org.openide.awt.StatusDisplayer;
 import org.openide.filesystems.FileObject;
 import org.openide.filesystems.FileUtil;
 import org.openide.util.Exceptions;
 import org.openide.util.Lookup;
 import org.openide.util.NbBundle;
 import org.openide.util.NbPreferences;
 import org.openide.util.lookup.ServiceProvider;
 
 /**
  *
  * @author Yeongjun Jang <yjjang@kribb.re.kr>
  */
 @ServiceProvider(service = ImportControllerUI.class)
 public class ImportControllerUIImpl implements ImportControllerUI {
 
     private final ImportController controller;
     private final LongTaskExecutor executor;
 
     public ImportControllerUIImpl() {
         controller = Lookup.getDefault().lookup(ImportController.class);
         executor = new LongTaskExecutor(true, "Importer");
         executor.setDefaultErrorHandler(new LongTaskErrorHandler() {
 
             @Override
             public void fatalError(Throwable t) {
                 if (t instanceof OutOfMemoryError) {
                     return;
                 }
                String message = t.getMessage();
                if (message == null || message.isEmpty()) {
                    message = t.getCause().getMessage();
                }
                NotifyDescriptor.Message msg = new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(msg);
             }
         });
     }
 
     @Override
     public ImportController getImportController() {
         return controller;
     }
 
     @Override
     public void importGraph() {
         importFile(GraphFileImporterBuilder.class);
     }
 
     @Override
     public void importFile(Class<? extends FileImporterBuilder> builderClass) {
         String builderName = builderClass.getName().replaceAll("\\.", "_");
         final String LAST_PATH = builderName + "_ImportFile_Last_Path";
         final String LAST_PATH_DEFAULT = builderName + "_ImportFile_Last_Path_Default";
         String lastPathDefault = NbPreferences.forModule(ImportControllerUIImpl.class).get(LAST_PATH_DEFAULT, null);
         String lastPath = NbPreferences.forModule(ImportControllerUIImpl.class).get(LAST_PATH, lastPathDefault);
         ImportFileChooserUI chooserUI = Lookup.getDefault().lookup(ImportFileChooserUIFactory.class).createUI(builderClass, lastPath);
         //Show
         int state = chooserUI.showOpenDialog(null);
         if (state == JFileChooser.APPROVE_OPTION) {
             File file = chooserUI.getSelectedFile();
             NbPreferences.forModule(ImportControllerUIImpl.class).put(LAST_PATH, file.getAbsolutePath());
             file = FileUtil.normalizeFile(file);
             FileObject fileObject = FileUtil.toFileObject(file);
             //Do
             importFile(builderClass, fileObject);
         }
     }
 
     private void importFile(Class<? extends FileImporterBuilder> builderClass, FileObject fileObject) {
         try {
             final FileImporter importer = controller.getFileImporter(builderClass, FileUtil.toFile(fileObject));
             if (importer == null) {
                 NotifyDescriptor.Message msg = new NotifyDescriptor.Message(
                         NbBundle.getMessage(ImportControllerUIImpl.class, "ImportControllerUIImpl.importFile.noMatchingError.text", fileObject.getNameExt()),
                         NotifyDescriptor.WARNING_MESSAGE);
                 DialogDisplayer.getDefault().notify(msg);
                 return;
             }
 
             //Execute task
             final String fileName = fileObject.getNameExt();
             final InputStream stream = fileObject.getInputStream();
             final ImportTask task = new ImportTask();
 
             executor.execute(task, new Runnable() {
 
                 @Override
                 public void run() {
                     ProgressTicket ticket = task.getProgressTicket();
                     Progress.start(ticket);
                     try {
                         Container container = controller.importFile(stream, importer);
                         if (container != null) {
                             container.setSource(fileName);
                             finishImport(container);
                         }
                     } catch (Exception ex) {
                         throw new RuntimeException(ex);
                     } finally {
                         Progress.finish(ticket);
                     }
                 }
             }, NbBundle.getMessage(ImportControllerUIImpl.class, "ImportControllerUIImpl.importFile.taskName", fileName));
             if (fileObject.getPath().startsWith(System.getProperty("java.io.tmpdir"))) {
                 try {
                     fileObject.delete();
                 } catch (IOException ex) {
                     Exceptions.printStackTrace(ex);
                 }
             }
         } catch (Exception ex) {
             Logger.getLogger("").log(Level.WARNING, "", ex);
         }
     }
 
     private void finishImport(Container container) {
         if (container instanceof GraphContainer) {
             finishImport((GraphContainer) container);
             return;
         }
 
         if (!container.verify()) {
             Logger.getLogger(ImportControllerUIImpl.class.getName()).severe("Bad import container");
             return;
         }
 
         //StatusLine notify
         String source = container.getSource();
         StatusDisplayer.getDefault().setStatusText(
                 NbBundle.getMessage(ImportControllerUIImpl.class, "ImportControllerUIImpl.status.importSuccess",
                 source.isEmpty()
                 ? NbBundle.getMessage(ImportControllerUIImpl.class, "ImportControllerUIImpl.status.importSource.default") : source));
     }
 
     private void finishImport(GraphContainer container) {
         if (!container.verify()) {
             Logger.getLogger(ImportControllerUIImpl.class.getName()).severe("Bad import container");
             return;
         }
 
         ReportPanel reportPanel = new ReportPanel();
         reportPanel.setData(container);
         DialogDescriptor reportDescriptor = new DialogDescriptor(reportPanel,
                 NbBundle.getMessage(ImportControllerUIImpl.class, "ReportPanel.reportDialog.title"));
         if (!DialogDisplayer.getDefault().notify(reportDescriptor).equals(NotifyDescriptor.OK_OPTION)) {
             reportPanel.destroy();
             return;
         } else {
             reportPanel.apply();
         }
 
         //Process
         final Processor processor = reportPanel.getProcessor();
         final Processor.UI processorUI = controller.getProcessorUI(processor);
         final ValidResult result = new ValidResult();
         if (processorUI != null) {
             try {
                 SwingUtilities.invokeAndWait(new Runnable() {
 
                     @Override
                     public void run() {
                         JPanel panel = processorUI.getPanel();
                         processorUI.setup(processor);
                         final DialogDescriptor processorDescriptor = new DialogDescriptor(panel,
                                 NbBundle.getMessage(ImportControllerUIImpl.class, "ImportControllerUIImpl.processor.settingUI.title", processorUI.getTitle()));
                         if (panel instanceof ValidationPanel) {
                             ValidationPanel vp = (ValidationPanel) panel;
                             vp.addChangeListener(new ChangeListener() {
 
                                 @Override
                                 public void stateChanged(ChangeEvent e) {
                                     processorDescriptor.setValid(!((ValidationPanel) e.getSource()).isFatalProblem());
                                 }
                             });
                             processorDescriptor.setValid(!vp.isFatalProblem());
                         }
                         Object option = DialogDisplayer.getDefault().notify(processorDescriptor);
                         if (option.equals(NotifyDescriptor.CANCEL_OPTION) || option.equals(NotifyDescriptor.CLOSED_OPTION)) {
                             result.setValid(false);
                         } else {
                             processorUI.apply(true); //true
                             result.setValid(true);
                         }
                     }
                 });
             } catch (InterruptedException ex) {
                 Exceptions.printStackTrace(ex);
             } catch (InvocationTargetException ex) {
                 Exceptions.printStackTrace(ex);
             }
         }
         if (result.isValid()) {
             controller.process(container, processor);
 
             //StatusLine notify
             String source = container.getSource();
             StatusDisplayer.getDefault().setStatusText(
                     NbBundle.getMessage(ImportControllerUIImpl.class, "ImportControllerUIImpl.status.importSuccess",
                     source.isEmpty()
                     ? NbBundle.getMessage(ImportControllerUIImpl.class, "ImportControllerUIImpl.status.importSource.default") : source));
         }
     }
 
     @Override
     public void importAttributes() {
         final ImportAttributesPanel importPanel = new ImportAttributesPanel();
         ValidationPanel vp = importPanel.getValidationPanel();
         final DialogDescriptor dd = new DialogDescriptor(vp,
                 NbBundle.getMessage(ImportControllerUIImpl.class, "ImportControllerUIImpl.importAttributes.dialog.title"));
         importPanel.addChangeListener(new ChangeListener() {
 
             @Override
             public void stateChanged(ChangeEvent e) {
                 dd.setValid(importPanel.isOk());
             }
         });
         dd.setValid(false);
         Object closeOption = DialogDisplayer.getDefault().notify(dd);
         if (closeOption.equals(NotifyDescriptor.OK_OPTION)) {
             final File attributeFile = importPanel.getAttributeFile();
             final String source = attributeFile.getName();
             final ImportTask task = new ImportTask();
             executor.execute(task, new Runnable() {
 
                 @Override
                 public void run() {
                     ProgressTicket ticket = task.getProgressTicket();
                     Progress.start(ticket);
                     try {
                         controller.importAttributes(FileUtil.toFileObject(attributeFile).getInputStream(),
                                 importPanel.getTargetTable(),
                                 importPanel.getHeaderNames(), importPanel.hasHeader(), importPanel.isMultipleValueEnabled(),
                                 importPanel.getAttributeKeyField(), importPanel.getNetworkKeyField());
                     } catch (Exception ex) {
                         StatusDisplayer.getDefault().setStatusText(
                                 NbBundle.getMessage(ImportControllerUIImpl.class, "ImportControllerUIImpl.status.importFail", source));
                         throw new RuntimeException(ex);
                     } finally {
                         Progress.finish(ticket);
                     }
                     StatusDisplayer.getDefault().setStatusText(
                             NbBundle.getMessage(ImportControllerUIImpl.class, "ImportControllerUIImpl.status.importSuccess", source));
                     Lookup.getDefault().lookup(VisualizationController.class).getDisplay().fireGraphChangedEvent();
                 }
             }, NbBundle.getMessage(ImportControllerUIImpl.class, "ImportControllerUIImpl.importAttributes.taskName", source));
         }
     }
 
     @Override
     public void importCSV() {
         WizardDescriptor wizardDescriptor = new WizardDescriptor(getImportCSVWizardPanels());
         // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
         wizardDescriptor.setTitleFormat(new MessageFormat("{0}"));
         wizardDescriptor.setTitle(NbBundle.getMessage(ImportControllerUIImpl.class, "ImportControllerUIImpl.importCSV.wizardDialog.title"));
         Dialog wizardDialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
         wizardDialog.setVisible(true);
         wizardDialog.toFront();
         if (wizardDescriptor.getValue().equals(WizardDescriptor.FINISH_OPTION)) {
             final File nodesFile = (File) wizardDescriptor.getProperty(ImportCSVWizardPanel.PROP_NODETABLE_CSV_FILE);
             final String nodeId = (String) wizardDescriptor.getProperty(ImportCSVWizardPanel.PROP_NODETABLE_ID_COLUMN);
             final String nodeLabel = (String) wizardDescriptor.getProperty(ImportCSVWizardPanel.PROP_NODETABLE_LABEL_COLUMN);
             final String[] nodeColumns = (String[]) wizardDescriptor.getProperty(ImportCSVWizardPanel.PROP_NODETABLE_COLUMNS);
             final boolean nodeHasHeader = (Boolean) wizardDescriptor.getProperty(ImportCSVWizardPanel.PROP_NODETABLE_HAS_HEADER);
             final File edgesFile = (File) wizardDescriptor.getProperty(ImportCSVWizardPanel.PROP_EDGETABLE_CSV_FILE);
             final String edgeSource = (String) wizardDescriptor.getProperty(ImportCSVWizardPanel.PROP_EDGETABLE_SOURCE_COLUMN);
             final String edgeTarget = (String) wizardDescriptor.getProperty(ImportCSVWizardPanel.PROP_EDGETABLE_TARGET_COLUMN);
             final String edgeLabel = (String) wizardDescriptor.getProperty(ImportCSVWizardPanel.PROP_EDGETABLE_LABEL_COLUMN);
             final boolean directed = (Boolean) wizardDescriptor.getProperty(ImportCSVWizardPanel.PROP_EDGETABLE_IS_DIRECTED);
             final String[] edgeColumns = (String[]) wizardDescriptor.getProperty(ImportCSVWizardPanel.PROP_EDGETABLE_COLUMNS);
             final boolean edgeHasHeader = (Boolean) wizardDescriptor.getProperty(ImportCSVWizardPanel.PROP_EDGETABLE_HAS_HEADER);
 
             final String source = (nodesFile == null ? "" : nodesFile.getName())
                     + (edgesFile == null ? "" : ((nodesFile == null ? "" : " and ") + edgesFile.getName()));
 
             final ImportTask task = new ImportTask();
             executor.execute(task, new Runnable() {
 
                 @Override
                 public void run() {
                     ProgressTicket ticket = task.getProgressTicket();
                     Progress.start(ticket);
                     try {
 //                        GraphContainer container = controller.importCSV(
 //                                nodesFile != null ? FileUtil.toFileObject(nodesFile).getInputStream() : null,
 //                                edgesFile != null ? FileUtil.toFileObject(edgesFile).getInputStream() : null,
 //                                nodeId, nodeLabel, edgeSource, edgeTarget, edgeLabel, directed);
                         GraphContainer container = controller.importCSV(
                                 nodesFile != null ? FileUtil.toFileObject(nodesFile).getInputStream() : null,
                                 edgesFile != null ? FileUtil.toFileObject(edgesFile).getInputStream() : null,
                                 nodeColumns, nodeHasHeader, edgeColumns, edgeHasHeader,
                                 nodeId, nodeLabel, edgeSource, edgeTarget, edgeLabel, directed);
                         if (container != null) {
                             container.setSource(source);
                             finishImport(container);
                         }
                     } catch (Exception ex) {
                         throw new RuntimeException(ex);
                     } finally {
                         Progress.finish(ticket);
                     }
                 }
             }, NbBundle.getMessage(ImportControllerUIImpl.class, "ImportControllerUIImpl.importFile.taskName", source));
 
 //            Lookup.getDefault().lookup(DataTablesController.class).refreshCurrentTable();
         }
     }
 
     /**
      * Initialize panels representing individual wizard's steps and sets
      * various properties for them influencing wizard appearance.
      */
     private WizardDescriptor.Panel[] getImportCSVWizardPanels() {
         WizardDescriptor.Panel[] panels = new WizardDescriptor.Panel[]{
             new ImportCSVNodeTableWizardPanel(), new ImportCSVEdgeTableWizardPanel()
         };
         String[] steps = new String[panels.length];
         for (int i = 0; i < panels.length; i++) {
             Component c = panels[i].getComponent();
             // Default step name to component name of panel. Mainly useful
             // for getting the name of the target chooser to appear in the
             // list of steps.
             steps[i] = c.getName();
             if (c instanceof JComponent) { // assume Swing components
                 JComponent jc = (JComponent) c;
                 // Sets step number of a component
                 jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                 // Sets steps names for a panel
                 jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                 // Turn on subtitle creation on each step
                 jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, true);
                 // Show steps on the left side with the image on the background
                 jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, true);
                 // Turn on numbering of all steps
                 jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, true);
             }
         }
         return panels;
     }
 
     private static final class ValidResult {
 
         private boolean valid = true;
 
         public boolean isValid() {
             return valid;
         }
 
         public void setValid(boolean valid) {
             this.valid = valid;
         }
     }
 
     private static class ImportTask implements LongTask {
 
         private final Importer importer;
         private ProgressTicket progressTicket;
 
         ImportTask() {
             this.importer = null;
         }
 
         ImportTask(Importer importer) {
             this.importer = importer;
         }
 
         @Override
         public boolean cancel() {
             return importer.cancel();
         }
 
         ProgressTicket getProgressTicket() {
             return progressTicket;
         }
 
         @Override
         public void setProgressTicket(ProgressTicket progressTicket) {
             this.progressTicket = progressTicket;
         }
     }
 }
