 package org.dawb.common.ui.wizard;
 
 import java.io.File;
 import java.lang.reflect.InvocationTargetException;
 
 import org.dawb.common.services.ServiceManager;
 import org.dawb.common.services.conversion.IConversionContext;
 import org.dawb.common.services.conversion.IConversionService;
 import org.dawb.common.ui.monitor.ProgressMonitorWrapper;
 import org.dawb.common.ui.util.EclipseUtils;
 import org.dawnsci.plotting.api.IPlottingSystem;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IExportWizard;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPart;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 
 public class PlotDataConversionWizard extends Wizard implements IExportWizard {
 	
 	public static final String ID = "org.dawb.common.ui.wizard.plotdataconversion";
 	
 	private IConversionService            service;
 	private IConversionContext            context;
 	private AbstractPlotConversionVisitor            visitor;
 	private PlotDataConversionPage        conversionPage;
 	private IPlottingSystem system;
 	
 	private static final Logger logger = LoggerFactory.getLogger(PlotDataConversionWizard.class);
 	
 	public PlotDataConversionWizard() {
 		super();
 		setWindowTitle("Convert Data");
 		
 		// It's an OSGI service, not required to use ServiceManager
 		try {
 			this.service = (IConversionService)ServiceManager.getService(IConversionService.class);
 		} catch (Exception e) {
 			logger.error("Cannot get conversion service!", e);
 			return;
 		}
 	}
 	
 	public void addPages() {
 		
 		if (system == null) system = getPlottingSystem();
 		
 		if (system == null) {
 			logger.error("Could not find plotting system to export data from");
 			return;
 		}
 		
 		if (system.is2D()) {
 			visitor = new Plot2DConversionVisitor(system);
 		} else {
 			visitor = new Plot1DConversionVisitor(system);
 		}
 		
 		String[] junk = new String[1];
 		junk[0] = "junk";
 		context = service.open(junk);
 		context.setConversionVisitor(visitor);
 		
 		//HACK - put in a dataset so the conversion class goes straight to iterate
 		context.setLazyDataset(AbstractDataset.zeros(new int[]{10},AbstractDataset.INT32));
		context.addSliceDimension(0, null);
 		
 		conversionPage = new PlotDataConversionPage();
 		conversionPage.setPath(System.getProperty("user.home") +File.separator+ "plotdata."+ visitor.getExtension());
 		
 		conversionPage.setDescription("Convert plotted data to file");
 		
 		addPage(conversionPage);
 	
 	}
 
 	@Override
 	public void init(IWorkbench workbench, IStructuredSelection selection) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public boolean performFinish() {
 		try {
 			getContainer().run(true, true, new IRunnableWithProgress() {
 				@Override
 				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {				 
 
 					try {
 						context.setOutputPath(conversionPage.getAbsoluteFilePath());
 						context.setMonitor(new ProgressMonitorWrapper(monitor));
 						context.setConversionVisitor(visitor);
 						// Bit with the juice
 						monitor.beginTask(visitor.getConversionSchemeName(), context.getWorkSize());
 						monitor.worked(1);
 						service.process(context);
 
 					} catch (final Exception ne) {
 
 						logger.error("Cannot run export process for "+visitor.getConversionSchemeName()+"'", ne);
 
 					} 
 
 				}
 			});
 		} catch (Exception ne) {
 			logger.error("Cannot run export process  "+visitor.getConversionSchemeName(), ne);
 		}
 		return true;
 	}
 	
 	public void setPlottingSystem(IPlottingSystem system) {
 		this.system = system;
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
