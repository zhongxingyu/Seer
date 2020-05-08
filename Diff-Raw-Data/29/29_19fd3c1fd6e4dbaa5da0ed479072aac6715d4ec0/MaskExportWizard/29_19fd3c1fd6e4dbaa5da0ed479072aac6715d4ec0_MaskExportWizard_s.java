 package org.dawnsci.plotting.tools.masking;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import org.dawb.common.ui.plot.IPlottingSystem;
 import org.dawb.common.ui.plot.PlottingFactory;
 import org.dawb.common.ui.plot.ThreadSafePlottingSystem;
 import org.dawb.common.ui.util.EclipseUtils;
 import org.dawb.common.ui.wizard.CheckWizardPage;
 import org.dawb.common.ui.wizard.FileChoosePage;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.ui.IExportWizard;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.PlatformUI;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class MaskExportWizard extends Wizard implements IExportWizard {
 	
 	private static final Logger logger = LoggerFactory.getLogger(MaskExportWizard.class);
 
 	public static final String ID = "org.dawnsci.plotting.exportMask";
 	
 	private FileChoosePage  fcp;
 	private CheckWizardPage options;
 
 	public MaskExportWizard() {
 		setWindowTitle("Export Mask");
 		this.fcp = new FileChoosePage("Export Location", new StructuredSelection());
 		fcp.setDescription("Please choose the location of the file to export. (This file will be a nexus HDF5 file.)");
 		fcp.setFileExtension("nxs");
 		addPage(fcp);
 		
 		this.options = new CheckWizardPage("Export Options", createDefaultOptions());
 		addPage(options);
 		
 	}
 	
     public boolean canFinish() {
         if (fcp.isPageComplete()) {
         	options.setDescription("Please choose the things to save in '"+fcp.getFileName()+"'.");
         }
         return super.canFinish();
     }
 
 
 	private Map<String, Boolean> createDefaultOptions() {
 		final Map<String, Boolean> options = new LinkedHashMap<String, Boolean>(3);
 		options.put("Original Data", true);
 		options.put("Mask",          true);
 		options.put("Regions",       true);
 		return options;
 	}
 
 	@Override
 	public void init(IWorkbench workbench, IStructuredSelection selection) {
 		      
  	}
 
 	@Override
 	public boolean performFinish() {
 		
		 final IFile           file   = fcp.createNewFile();
		 final IWorkbenchPart  part   = EclipseUtils.getPage().getActivePart();
		 final IPlottingSystem system = new ThreadSafePlottingSystem((IPlottingSystem)part.getAdapter(IPlottingSystem.class));
		 
 		 try {
 			 getContainer().run(true, true, new IRunnableWithProgress() {
 
 				 @Override
 				 public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					 
 					 System.out.println(system);
 
 					 // TODO FIXME use the wizzy new service for saving mask! Send regions off to it...
 				 }
 			 });
 		 } catch (Exception ne) {
			 logger.error("Cannot save masking file!", ne);
 		 }
 		 
 		 return true;
 	}
 
 }
