 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 
 package org.amanzi.awe.gpeh.wizard;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 
 import org.amanzi.awe.gpeh.GpehTransferData;
 import org.amanzi.awe.gpeh.filebrowser.prowider.View;
 import org.amanzi.awe.gpeh.parser.Events;
 import org.amanzi.neo.loader.core.CommonConfigData;
 import org.amanzi.neo.loader.ui.wizards.DirectoryEditor;
 import org.amanzi.neo.loader.ui.wizards.LoaderPage;
 import org.apache.commons.lang.StringUtils;
 import org.eclipse.jface.preference.DirectoryFieldEditor;
 import org.eclipse.jface.viewers.ITreeSelection;
 import org.eclipse.jface.viewers.TreePath;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 
 //TODO: LN: comments!!!!!
 
 /**
  * TODO Purpose of 
  * <p>
  *
  * </p>
  * @author Kasnitskij_V
  * @since 1.0.0
  */
 public class GpehCsvParserWizardPage extends LoaderPage<CommonConfigData> {
     private String outputDirectory;
     
     protected String datasetName;
     private DirectoryFieldEditor outputDir;
     private List<File> selectedDirectories;
     
     public  static final String SELECTED_EVENT = "GPEH SELECTED_EVENTS";
     private static final String WIZARD_PAGE_TITLE = "Save GPEH-data to CSV files";
     
     protected GpehCsvParserWizardPage() {
         super(WIZARD_PAGE_TITLE);
     }
 
     @Override
     public void createControl(Composite parent) {
         final Composite main = new Composite(parent, SWT.NULL);
         main.setLayout(new GridLayout(3, false));
         
         View view = new View();
         view.createPartControl(main);
         final TreeViewer treeViewer = view.getTreeViewer();
         GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
         gridData.horizontalSpan = 3;
         gridData.minimumHeight = 500;
         gridData.heightHint = 300;
         treeViewer.getTree().setLayoutData(gridData);
         
         treeViewer.getTree().addSelectionListener(new SelectionListener() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				ITreeSelection treeSelection = (ITreeSelection) treeViewer.getSelection();
 				selectedDirectories = new ArrayList<File>();
 				for (TreePath path : treeSelection.getPaths()) {
 					selectedDirectories.add(new File(path.getLastSegment().toString()));
 				}
 				update();
 			}
 			
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e) {
 				// TODO Auto-generated method stub
 			}
 		});
         
         outputDir = new DirectoryEditor("editor", "Output directory: ", main);
         outputDir.setEmptyStringAllowed(true);
         outputDir.getTextControl(main).addModifyListener(new ModifyListener() {
             public void modifyText(ModifyEvent e) {
                 outputDirectory = outputDir.getStringValue();
                 update();
             }
         });
         
         setAccessType(false);
         setControl(main);
         
         getLoadersDescriptions();
         selectLoader(0);
         update();
     }
 
     @Override
     protected boolean validateConfigData(CommonConfigData configurationData) {
         try {
             if (StringUtils.isEmpty(outputDirectory)) {
                 return false;
             }
             File fileOutput = new File(outputDirectory);
             if (selectedDirectories.size() == 0 || 
             		(!(fileOutput.isAbsolute() && fileOutput.exists()))) {
                 return false;
             }
             
             configurationData.setMultiRoots(selectedDirectories);
             
             HashSet<Events> selectedEvents = new HashSet<Events>();
             
             Events event = null;
             for (int i = 384; i < 396; i++) {
                 event = Events.findById(i);
                 selectedEvents.add(event);
             }
             
             for (int i = 397; i < 409; i++) {
                 event = Events.findById(i);
                 selectedEvents.add(event);
             }
             selectedEvents.add(Events.findById(410));
             
             for (int i = 413; i < 424; i++) {
                 event = Events.findById(i);
                 selectedEvents.add(event);
             }
             
             for (int i = 425; i < 457; i++) {
                 event = Events.findById(i);
                 selectedEvents.add(event);
             }
            
            for (int i = 458; i < 460; i++) {
                event = Events.findById(i);
                selectedEvents.add(event);
            }
            selectedEvents.add(Events.findById(475));
            
             configurationData.getAdditionalProperties().put(SELECTED_EVENT, new HashSet<Events>(selectedEvents));
             configurationData.getAdditionalProperties().put(GpehTransferData.OUTPUT_DIRECTORY, outputDir.getStringValue());
             return true;
         } catch (Exception e) {
             return false;
         }
     }
 
 }
