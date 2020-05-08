 package org.dawb.common.ui.wizard.persistence;
 
 import java.io.File;
 import java.lang.reflect.InvocationTargetException;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.dawb.common.services.IPersistenceService;
 import org.dawb.common.services.IPersistentFile;
 import org.dawb.common.services.ServiceManager;
 import org.dawb.common.ui.monitor.ProgressMonitorWrapper;
 import org.dawb.common.ui.util.EclipseUtils;
 import org.dawb.common.ui.wizard.CheckWizardPage;
 import org.dawb.common.ui.wizard.NewFileChoosePage;
 import org.dawnsci.plotting.api.IPlottingSystem;
 import org.dawnsci.plotting.api.region.IRegion;
 import org.dawnsci.plotting.api.tool.IToolPage;
 import org.dawnsci.plotting.api.tool.IToolPageSystem;
 import org.dawnsci.plotting.api.trace.IImageTrace;
 import org.dawnsci.plotting.api.trace.ITrace;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IExportWizard;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPart;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.BooleanDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import uk.ac.diamond.scisoft.analysis.fitting.functions.IFunction;
 import uk.ac.diamond.scisoft.analysis.fitting.functions.IFunctionService;
 import uk.ac.diamond.scisoft.analysis.io.IDiffractionMetadata;
 import uk.ac.diamond.scisoft.analysis.io.IMetaData;
 import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;
 
 public class PersistenceExportWizard extends AbstractPerstenceWizard implements IExportWizard {
 	
 	public static final String ID = "org.dawnsci.plotting.exportMask";
 	
 	private NewFileChoosePage  fcp;
 	private CheckWizardPage options;
 	
 	private static IPath  containerFullPath;
 	private static String staticFileName;
  
 	public PersistenceExportWizard() {
 		setWindowTitle("Export");
 		this.fcp = new NewFileChoosePage("Export Location", new StructuredSelection());
 		fcp.setDescription("Please choose the location of the file to export. (This file will be a nexus HDF5 file.)");
 		fcp.setFileExtension("nxs");
 		addPage(fcp);
 		
 		this.options = new CheckWizardPage("Export Options", createDefaultOptions());
 		Map<String,String> stringOptions = new HashMap<String, String>(1);
 		stringOptions.put("Mask", "");
 		options.setStringValues(stringOptions);
 		options.setDescription("Please choose things to export.");
 		
 		addPage(options);
 		
 	}
 	
 	public void createPageControls(Composite pageContainer) {
 		super.createPageControls(pageContainer);
 		
 		if (containerFullPath==null && staticFileName==null) {
 			try {
 			    final IFile file = EclipseUtils.getSelectedFile();
 			    if (file!=null) {
 			    	containerFullPath = file.getParent().getFullPath();
 			    	staticFileName    = file.getName();
 			    }
 			} catch (Throwable ne) {
 				// Nowt
 			}
 		}
 		
 		if (containerFullPath!=null) fcp.setContainerFullPath(containerFullPath);
 		if (staticFileName!=null)    fcp.setFileName(staticFileName);
 	}
 
     public boolean canFinish() {
    	if (fcp.isPageComplete()) {
     		options.setDescription("Please choose the things to save in '"+fcp.getFileName()+"'.");
     		options.setOptionEnabled("Original Data",   false);
     		options.setOptionEnabled("Image History",    false);
     		options.setOptionEnabled("Mask",            false);
     		options.setOptionEnabled("Regions",         false);
     		options.setOptionEnabled("Diffraction Metadata", false);
     		options.setOptionEnabled("Functions",       false);
 
     		File                file=null;
     		IPersistentFile     pf=null;
     		
     		PERSIST_BLOCK: try {
         		IPersistenceService service = (IPersistenceService)ServiceManager.getService(IPersistenceService.class);
         		file = fcp.getFile();
         		if (!file.exists()) break PERSIST_BLOCK;
     		    pf    = service.getPersistentFile(file.getAbsolutePath());
     		    final List<String>  names = pf.getMaskNames(null);
     		    if (names!=null && !names.isEmpty()) {
     		    	options.setStringValue("Mask", names.get(0));
     		    }
     		        		    
     		} catch (Throwable ne) {
     			logger.error("Cannot read persistence file at "+file);
     		} finally {
     			if (pf!=null) pf.close();
     		}
 
     		final IPlottingSystem  system   = getPlottingSystem();
     		if (system!=null) {
     			if (system != null) {
     				ITrace trace  = system.getTraces().iterator().next();
     				if (trace!=null) {
     					
     					options.setOptionEnabled("Original Data", true);
     					
     					if (trace instanceof IImageTrace && ((IImageTrace)trace).getMask()!=null) {
     						options.setOptionEnabled("Mask", true);
     					}
     				}
     				
     				boolean requireHistory = false;
     				final IToolPageSystem tsystem = (IToolPageSystem)system.getAdapter(IToolPageSystem.class);
     				final IToolPage       tool    = tsystem.getActiveTool();
     				if (tool != null && tool.getToolId().equals("org.dawb.workbench.plotting.tools.imageCompareTool")) {
     					final Map<String, IDataset> data = (Map<String, IDataset>)tool.getToolData();
     					if (data!=null && !data.isEmpty()) requireHistory = true;
     				}
       				options.setOptionEnabled("Image History", requireHistory);
     				
     				final Collection<IRegion> regions = system.getRegions();
     				if (regions != null && !regions.isEmpty()) {
     					options.setOptionEnabled("Regions", true);
     				}
     				
     				if (trace!=null && trace instanceof IImageTrace && trace.getData() != null) {
     					IMetaData meta = ((AbstractDataset)trace.getData()).getMetadata();
     					if (meta != null && (meta instanceof IDiffractionMetadata)) {
     						options.setOptionEnabled("Diffraction Metadata", true);
     					}
     				}
     				
     				final IWorkbenchPart   part   = EclipseUtils.getPage().getActivePart();
     				if (part!=null) {
     					final IFunctionService funcService = (IFunctionService)part.getAdapter(IFunctionService.class);
     					if (funcService != null) {
     						options.setOptionEnabled("Functions", true);
     					}
     				}
 
     			}
     		}
     	}
         return super.canFinish();
     }
 
 	@Override
 	public void init(IWorkbench workbench, IStructuredSelection selection) {
 		      
  	}
 
 	@Override
 	public boolean performFinish() {
 		
 		 IFile file = null;
 		 try {
 			 file   = fcp.createNewFile();
 			 			 
 			 final IWorkbenchPart  part   = EclipseUtils.getPage().getActivePart();
 			 final IPlottingSystem system = getPlottingSystem();
 			 final IFunctionService funcService = (IFunctionService)part.getAdapter(IFunctionService.class);
 
 			 final IFile finalFile = file;
 			 getContainer().run(true, true, new IRunnableWithProgress() {
 
 				 @Override
 				 public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
 					 
 					 IPersistentFile file = null;
 					 try {
 						 IPersistenceService service = (IPersistenceService)ServiceManager.getService(IPersistenceService.class);
 						 file    = service.createPersistentFile(finalFile.getLocation().toOSString());
 						 
 						 final IMonitor mon = new ProgressMonitorWrapper(monitor);
 
 						 // Save things.
 						 if (options.is("Original Data")) {
 							 Collection<ITrace> traces  = system.getTraces();
 							 for (ITrace trace : traces) {
 								 file.setData((AbstractDataset)trace.getData());
 								 if (trace instanceof IImageTrace) {
 									 final List<IDataset> iaxes = ((IImageTrace)trace).getAxes();
 									 if (iaxes!=null) file.setAxes(iaxes);
 								 }
 							 }
 						 }
 						 if (options.is("Image History")) {
 			    				final IToolPageSystem tsystem = (IToolPageSystem)system.getAdapter(IToolPageSystem.class);
 			    				final IToolPage       tool    = tsystem.getActiveTool();
 			    				if (tool != null && tool.getToolId().equals("org.dawb.workbench.plotting.tools.imageCompareTool")) {
 			    					final Map<String, IDataset> data = (Map<String, IDataset>)tool.getToolData();
 			    					if (data!=null && !data.isEmpty()) {
 			    						file.setHistory(data.values().toArray(new IDataset[data.size()]));
 			    					}
 			    				}
 						 }
 						 
 						 final ITrace trace = system.getTraces().iterator().next();
 						 if (options.is("Mask") && trace instanceof IImageTrace) {
 							 IImageTrace image = (IImageTrace)trace;
 							 final String name = options.getString("Mask");
 							 if (image.getMask()!=null) {
 								 file.addMask(name, (BooleanDataset)image.getMask(), mon);
 							 }
 						 }
 						 
 						 final Collection<IRegion> regions = system.getRegions();
 						 if (options.is("Regions") && regions!=null && !regions.isEmpty()) {
 							 for (IRegion iRegion : regions) {
 								 if (!file.isRegionSupported(iRegion.getROI())) continue;
 								 file.addROI(iRegion.getName(), iRegion.getROI());
 								 file.setRegionAttribute(iRegion.getName(), "Region Type", iRegion.getRegionType().getName());
 								 if (iRegion.getUserObject()!=null) {
 									 file.setRegionAttribute(iRegion.getName(), "User Object", iRegion.getUserObject().toString()); 
 								 }
 							 }
 						 }
 						 
 						 if (options.is("Diffraction Metadata")) {
 							 if (trace!=null && trace instanceof IImageTrace && trace.getData() != null) {
 								 IMetaData meta = trace.getData().getMetadata();
 								 if (meta == null || meta instanceof IDiffractionMetadata) {
 									 file.setDiffractionMetadata((IDiffractionMetadata) meta);
 								 }
 							 }
 						 }
 						 
 						 if (options.is("Functions")) {
 							 if (funcService != null) {
 								 Map<String, IFunction> functions = funcService.getFunctions();
 								 if (functions != null) {
 									 file.setFunctions(functions);
 								}
 							 }
 						 }
 						 
 						 
 					 } catch (Exception e) {
 						 throw new InvocationTargetException(e);
 					 } finally {
 						 if (file!=null) file.close();
 					 }
 				 }
 			 });
 		 } catch (Throwable ne) {
 			 if (ne instanceof InvocationTargetException && ((InvocationTargetException)ne).getCause()!=null){
 				 ne = ((InvocationTargetException)ne).getCause();
 			 }
 			 String message = null;
 			 if (file!=null) {
 				 message = "Cannot export '"+file.getName()+"' ";
 			 } else {
 				 message = "Cannot export file.";
 			 }
 			 logger.error("Cannot export file!", ne);
 		     ErrorDialog.openError(Display.getDefault().getActiveShell(), "Export failure", message, new Status(IStatus.WARNING, "org.dawb.common.ui", ne.getMessage(), ne));
 		     return true;
 		 } finally {
 			 try {
 				 file.getParent().refreshLocal(IResource.DEPTH_ONE, new NullProgressMonitor());
 			 } catch (CoreException e) {
 				 logger.error("Cannot refresh dir "+file, e);
 			 }
 		 }
 		 
 		 containerFullPath = fcp.getContainerFullPath();
 		 staticFileName    = fcp.getFileName();
 		 
 		 return true;
 	}
 
 	private IPlottingSystem getPlottingSystem() {
 		
 		
 		// Perhaps the plotting system is on a dialog
 		final Shell[] shells = Display.getDefault().getShells();
 		if (shells!=null) for (Shell shell : shells) {
 			final Object o = shell.getData();
 			if (o!=null && o instanceof IAdaptable) {
 				IPlottingSystem s = (IPlottingSystem)((IAdaptable)o).getAdapter(IPlottingSystem.class);
 				if (s!=null) return s;
 			} 
 		}
 		
 		final IWorkbenchPart  part   = EclipseUtils.getPage().getActivePart();
 		if (part!=null) {
 			return (IPlottingSystem)part.getAdapter(IPlottingSystem.class);
 		}
 		
 		return null;
 	}
 
 }
