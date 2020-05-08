 package org.dawb.workbench.plotting.tools.reduction;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.List;
 
 import ncsa.hdf.object.Group;
 
 import org.dawb.common.ui.monitor.ProgressMonitorWrapper;
 import org.dawb.common.ui.plot.tool.IDataReductionToolPage;
 import org.dawb.common.ui.slicing.DimsDataList;
 import org.dawb.common.ui.slicing.SliceUtils;
 import org.dawb.common.ui.util.EclipseUtils;
 import org.dawb.hdf5.HierarchicalDataFactory;
 import org.dawb.hdf5.IHierarchicalDataFile;
 import org.dawb.hdf5.Nexus;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.emf.common.ui.dialogs.WorkspaceResourceDialog;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CCombo;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.IExportWizard;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.io.IMetaData;
 import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
 import uk.ac.diamond.scisoft.analysis.io.SliceObject;
 
 public class DataReductionWizard extends Wizard implements IExportWizard {
 
 	public static final String ID = "org.dawb.workbench.plotting.dataReductionExportWizard";
 	
 	private static final Logger logger = LoggerFactory.getLogger(DataReductionWizard.class);
 	
 	private IFile                  source;
 	private List<String>           h5Selections;
 	private String                 h5Path;
 	private IDataReductionToolPage dataReductionPage;
 	private DimsDataList           sliceData;
 	
 	public DataReductionWizard() {
 		super();
 		addPage(new ReductionPage("Data Reduction"));
 		setWindowTitle("Export Reduced Data");
 	}
 
 	@Override
 	public void init(IWorkbench workbench, IStructuredSelection selection) {
 	}
 
 	@Override
 	public boolean performFinish() {
 		
 		 try {
 			 getContainer().run(true, true, new IRunnableWithProgress() {
 
 				 @Override
 				 public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
 
 					 final ReductionPage rp = (ReductionPage)getPages()[0];
 					
 					 // We write to the reduction file here:
 					 final IFile      export = rp.getPath();
 					 
 					 // Generate slice data
 					 final String     path   = rp.getH5Path();
 					 final DimsDataList dl   = getSliceData();
 					 IMetaData meta;
 					 try {
 						 meta = LoaderFactory.getMetaData(source.getLocation().toOSString(), new ProgressMonitorWrapper(monitor));
 					 } catch (Exception e1) {
 						 logger.error("Cannot expand slices required!", e1);
 						 return;
 					 }
 					 
 					 final int[]             shape  = meta.getDataShapes().get(path);
 					 final List<SliceObject> slices = SliceUtils.getExpandedSlices(shape, dl);
 
 					 try {
 						 export.getParent().refreshLocal(IResource.DEPTH_ONE, monitor);
 						 if (export.exists()) {
 							 export.delete(true, monitor);
 						 }
						 monitor.beginTask("Running '"+getTool().getTitle()+"'", slices.size());
 
 						 final IHierarchicalDataFile hf = HierarchicalDataFactory.getWriter(export.getLocation().toOSString());
 						 
 						 Group entry=null, group=null;
 						 try {
 							 entry = hf.group("entry");
 							 hf.setNexusAttribute(entry, Nexus.ENTRY);
 							 group = hf.group("reduction", entry);
 							 hf.setNexusAttribute(group, Nexus.DATA);	
 							 
 							 // Iterate slice data
 							 for (SliceObject slice : slices) {
 								 slice.setPath(source.getLocation().toOSString());
 								 slice.setName(path);
 
 								 final AbstractDataset set = SliceUtils.getSlice(slice, monitor);
 								 getTool().export(hf, group, set);
 								 monitor.worked(1);
 								 
 								 if (monitor.isCanceled()) break;
 							 }
 							 
 						 } finally {
 							 entry.close(group.getFID());						 
 							 hf.close();
 							 
 							 monitor.done();
 							 export.getParent().refreshLocal(IResource.DEPTH_ONE, monitor);
 						 }	
 
 						 if (rp.isOpen()) {
 							 Display.getDefault().asyncExec(new Runnable() {
 								 public void run() {
 									 try {
 										 EclipseUtils.openEditor(rp.getPath());
 									 } catch (PartInitException e) {
 										 logger.error("Opening file "+rp.getPath(), e);
 									 }
 								 }
 							 });
 						 }
 
 					 } catch (Exception ne) {
 						 logger.error("Cannot reduce data from '"+source.getName()+"' to '"+export.getName()+"'.", ne);
 					 }
 				 }
 			 });
 		 } catch (Exception ne) {
 			 logger.error("Cannot run export process for data reduction from tool "+getTool().getTitle(), ne);
 		 }
 		 
 		 return true;
 	}
 	
 	public boolean needsProgressMonitor() {
 		return true;
 	}
 		
 	private final class ReductionPage extends WizardPage {
 
 		private Text    txtPath;
 		private boolean overwrite = true;
 		private boolean open      = true;
 		private IFile   path;
 
 		protected ReductionPage(String pageName) {
 			super(pageName);
 		}
 
 		@Override
 		public void createControl(Composite parent) {
 			
 			Composite container = new Composite(parent, SWT.NULL);
 			GridLayout layout = new GridLayout();
 			container.setLayout(layout);
 			layout.numColumns = 3;
 			layout.verticalSpacing = 9;
 
 			Label label = new Label(container, SWT.NULL);
 			label.setText("Data File ");
 
 			label = new Label(container, SWT.NULL);
 			label.setText("'"+getSource().getLocation().toOSString()+"'");
 			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
 			
 			label = new Label(container, SWT.NULL);
 			label.setText("Data ");
 
 			final CCombo dataChoice = new CCombo(container, SWT.READ_ONLY);
 			dataChoice.setItems(getSelections().toArray(new String[getSelections().size()]));
 			dataChoice.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
 			dataChoice.select(0);
 			h5Path = getSelections().get(0);
 			dataChoice.addSelectionListener(new SelectionAdapter() {
 				public void widgetSelected(SelectionEvent e) {
 					h5Path = getSelections().get(dataChoice.getSelectionIndex());
 				}
 			});
 			
 			label = new Label(container, SWT.NULL);
 			label.setText("Slice ");
 
 			label = new Label(container, SWT.NULL);
 			label.setText(getSliceText());
 			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
 
 
 			label = new Label(container, SWT.NULL);
 			label.setText("Tool ");
 
 			label = new Label(container, SWT.NULL);
 			label.setText("'"+getTool().getTitle()+"'");
 			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
 			
 			label = new Label(container, SWT.NULL);
 			label.setText("Export &File  ");
 			txtPath = new Text(container, SWT.BORDER);
 			txtPath.setEditable(false);
 			txtPath.setEnabled(false);
 			txtPath.setText(getPath().getFullPath().toOSString());
 			GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
 			txtPath.setLayoutData(gd);
 			txtPath.addModifyListener(new ModifyListener() {			
 				@Override
 				public void modifyText(ModifyEvent e) {
 					pathChanged();
 				}
 			});
 
 			Button button = new Button(container, SWT.PUSH);
 			button.setText("...");
 			button.addSelectionListener(new SelectionAdapter() {
 				@Override
 				public void widgetSelected(SelectionEvent e) {
 					handleBrowse();
 				}
 			});
 			
 			final Button over = new Button(container, SWT.CHECK);
 			over.setText("Overwrite file if it exists.");
 			over.setSelection(true);
 			over.addSelectionListener(new SelectionAdapter() {
 				@Override
 				public void widgetSelected(SelectionEvent e) {
 					overwrite = over.getSelection();
 					pathChanged();
 				}
 			});
 			over.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
 			
 			final Button open = new Button(container, SWT.CHECK);
 			open.setText("Open file after export.");
 			open.setSelection(true);
 			open.addSelectionListener(new SelectionAdapter() {
 				@Override
 				public void widgetSelected(SelectionEvent e) {
 					ReductionPage.this.open = open.getSelection();
 					pathChanged();
 				}
 			});
 			open.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
 
 
 			pathChanged();
 			setControl(container);
 
 		}
 		
 		IFile getPath() {
 			if (path==null) { // We make one up from the source
 				IFile source = getSource();
 				final String strPath = source.getName().substring(0, source.getName().indexOf("."))+
 						               "_"+getShortToolName()+"_reduction.h5";
 				this.path = source.getParent().getFile(new Path(strPath));
 			}
 			return path;
 		}
 
 		/**
 		 * Uses the standard container selection dialog to choose the new value for the container field.
 		 */
 
 		private void handleBrowse() {
 			final IFile p = WorkspaceResourceDialog.openNewFile(PlatformUI.getWorkbench().getDisplay().getActiveShell(), 
 					"Export location", "Please choose a location to export the reduced data to. This must be a hdf5 file.", 
 					getPath().getFullPath(), null);
 			if (p!=null) {
 				this.path = p;
 			    txtPath.setText(this.path.getFullPath().toOSString());
 			}
 		}
 
 		/**
 		 * Ensures that both text fields are set.
 		 */
 
 		private void pathChanged() {
 
             final String p = txtPath.getText();
 			if (p==null || p.length() == 0) {
 				updateStatus("Please select a file to export to.");
 				return;
 			}
 			if (getPath().exists() && !overwrite) {
 				updateStatus("Please confirm overwrite of the file.");
 				return;
 			}
 			if (!getPath().getName().toLowerCase().endsWith(".h5")) {
 				updateStatus("Please set the file name to export as a file with the extension 'h5'.");
 				return;
 			}
 			if (getTool().getPlottingSystem().getRegions().size()<1) {
 				updateStatus("Please make a selection using '"+getTool().getTitle()+"' before running data reduction.");
 				return;
 				
 			}
 			updateStatus(null);
 		}
 
 		private void updateStatus(String message) {
 			setErrorMessage(message);
 			setPageComplete(message == null);
 		}
 
 		public boolean isOpen() {
 			return open;
 		}
 
 		public String getH5Path() {
 			return h5Path;
 		}
 
 	}
 
 	public IFile getSource() {
 		return source;
 	}
 
 	public String getSliceText() {
 		try {
 			IMetaData meta = LoaderFactory.getMetaData(source.getLocation().toOSString(), null);
 			final int[]    shape = meta.getDataShapes().get(h5Path);
 			return sliceData.toString(shape);
 		} catch (Exception e) {
 			logger.error("Cannot extract meta data from file "+getSource(), e);
 			return null;
 		}
 	}
 
 	private String getShortToolName() {
 		final IDataReductionToolPage page = getTool();
 		if (page==null) return null;
 		return page.getTitle().replace(' ', '_');
 	}
 
 	public void setSource(IFile filePath) {
 		this.source = filePath;
 	}
 
 	public List<String> getSelections() {
 		return h5Selections;
 	}
 
 	public void setSelections(List<String> selections) {
 		this.h5Selections = selections;
 	}
 
 	public IDataReductionToolPage getTool() {
 		return dataReductionPage;
 	}
 
 	public void setTool(IDataReductionToolPage dataReductionPage) {
 		this.dataReductionPage = dataReductionPage;
 		getPages()[0].setDescription("This wizard runs '"+getTool().getTitle()+"' over a stack of data. Please check the data to slice, "+
 		                             "confirm the export file and then press 'Finish' to run the tool on each slice.");
 
 	}
 
 	public DimsDataList getSliceData() {
 		return sliceData;
 	}
 
 	public void setSliceData(DimsDataList sliceData) {
 		this.sliceData = sliceData;
 	}
 
 }
