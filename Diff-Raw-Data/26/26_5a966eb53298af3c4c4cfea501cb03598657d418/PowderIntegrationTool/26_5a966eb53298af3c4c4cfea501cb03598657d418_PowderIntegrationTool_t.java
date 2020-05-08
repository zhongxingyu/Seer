 package org.dawnsci.plotting.tools.powderintegration;
 
 import java.io.File;
 import java.util.List;
 
 import javax.measure.unit.NonSI;
 import javax.measure.unit.UnitFormat;
 
 import ncsa.hdf.object.Dataset;
 import ncsa.hdf.object.Datatype;
 import ncsa.hdf.object.Group;
 import ncsa.hdf.object.HObject;
 import ncsa.hdf.object.h5.H5Datatype;
 
 import org.dawb.common.services.IPersistenceService;
 import org.dawb.common.services.IPersistentFile;
 import org.dawb.common.services.ServiceManager;
 import org.dawb.common.ui.menu.MenuAction;
 import org.dawb.common.ui.plot.tools.IDataReductionToolPage;
 import org.dawb.common.ui.util.EclipseUtils;
 import org.dawb.hdf5.IHierarchicalDataFile;
 import org.dawb.hdf5.Nexus;
 import org.dawb.hdf5.nexus.NexusUtils;
 import org.dawnsci.plotting.api.IPlottingSystem;
 import org.dawnsci.plotting.api.PlotType;
 import org.dawnsci.plotting.api.PlottingFactory;
 import org.dawnsci.plotting.api.tool.AbstractToolPage;
 import org.dawnsci.plotting.api.tool.IToolPageSystem;
 import org.dawnsci.plotting.api.trace.IImageTrace;
 import org.dawnsci.plotting.api.trace.ITraceListener;
 import org.dawnsci.plotting.api.trace.TraceEvent;
 import org.dawnsci.plotting.api.trace.TraceWillPlotEvent;
 import org.dawnsci.plotting.tools.Activator;
 import org.dawnsci.plotting.tools.diffraction.DiffractionUtils;
 import org.dawnsci.plotting.tools.powderintegration.PowderIntegrationJob.IntegrationMode;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IViewPart;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.IPageSite;
 
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import uk.ac.diamond.scisoft.analysis.diffraction.DetectorProperties;
 import uk.ac.diamond.scisoft.analysis.io.IDiffractionMetadata;
 import uk.ac.diamond.scisoft.analysis.io.ILoaderService;
 import uk.ac.diamond.scisoft.analysis.io.IMetaData;
 import uk.ac.diamond.scisoft.analysis.io.NexusDiffractionMetaReader;
 import uk.ac.diamond.scisoft.analysis.roi.ROIProfile.XAxis;
 
 public class PowderIntegrationTool extends AbstractToolPage implements IDataReductionToolPage {
 	
 	IPlottingSystem system;
 	private ITraceListener traceListener;
 	private IDiffractionMetadata metadata;
 	private PowderIntegrationJob fullImageJob;
 	private Label statusMessage;
 	String[] statusString;
 	ILoaderService service;
 	Composite baseComposite;
 	XAxis xAxis = XAxis.Q;
 	IntegrationMode mode = IntegrationMode.NONSPLITTING;
 	boolean correctSolidAngle = false;
 	IDiffractionMetadata importedMeta;
 	
 	public PowderIntegrationTool() {
 		try {
 			system = PlottingFactory.createPlottingSystem();
 		} catch (Exception e) {
 			e.printStackTrace();
 			return;
 		}
 		
 		
 		this.traceListener = new ITraceListener.Stub() {
 			
 			@Override
 			public void traceWillPlot(TraceWillPlotEvent evt) {
 				PowderIntegrationTool.this.update(evt.getImage());
 			}
 			@Override
 			public void traceRemoved(TraceEvent evt) {
 				if (system!= null) system.clear();
 			}
 			
 		};
 		
 		this.service = (ILoaderService)PlatformUI.getWorkbench().getService(ILoaderService.class);
 		statusString = new String[1];
 	}
 
 	@Override
 	public ToolPageRole getToolPageRole() {
 		return ToolPageRole.ROLE_2D;
 	}
 	
 	@Override
 	public void activate() {
 		
 		if (isActive()) return;
 		super.activate();
 		getPlottingSystem().addTraceListener(traceListener);
 		
 		IImageTrace im = getImageTrace();
 		
 		if (im != null && im.getData() != null) update(im.getData()); 
 	}
 	
 	@Override
 	public void deactivate() {
 		getPlottingSystem().removeTraceListener(traceListener);
 		super.deactivate();
 	}
 
 	@Override
 	public void createControl(Composite parent) {
 		
 		baseComposite = new Composite(parent, SWT.NONE);
 		baseComposite.setLayout(new GridLayout());
 		
 		createActions();
 		
 		final IPageSite site = getSite();
 		IActionBars actionbars = site!=null?site.getActionBars():null;
 		system.createPlotPart(baseComposite, 
 				getTitle(), 
 				actionbars, 
 				PlotType.XY,
 				this.getViewPart());
 		
 		system.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 
 		system.getSelectedYAxis().setAxisAutoscaleTight(true);
 		
 		statusMessage = new Label(baseComposite, SWT.WRAP);
 		statusMessage.setText("Status...");
 		statusMessage.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
 		statusMessage.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY));
 		statusMessage.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
 		
 		update(null);
 	}
 	
 	private void createActions() {
 		
 		final MenuAction modeSelect= new MenuAction("Select Mode");
 		
 		final Action nonAction = new Action("Non pixel splitting") {
 			@Override
 			public void run() {
 				mode = IntegrationMode.NONSPLITTING;
 				modeSelect.setSelectedAction(this);
 				update(null);
 			}
 		};
 		
 		nonAction.setImageDescriptor(Activator.getImageDescriptor("icons/pixel.png"));
 		
 		final Action splitAction = new Action("Pixel splitting") {
 			@Override
 			public void run() {
 				mode = IntegrationMode.SPLITTING;
 				modeSelect.setSelectedAction(this);
 				update(null);
 			}
 		};
 		
 		splitAction.setImageDescriptor(Activator.getImageDescriptor("icons/splitPixel.png"));
 		
 		final Action split2DAction = new Action("Pixel splitting 2D") {
 			@Override
 			public void run() {
 				mode = IntegrationMode.SPLITTING2D;
 				modeSelect.setSelectedAction(this);
 				update(null);
 			}
 		};
 		split2DAction.setImageDescriptor(Activator.getImageDescriptor("icons/cake.png"));
 		
 		final MenuAction axisSelect= new MenuAction("Select Axis");
 
 		final Action qAction = new Action("Q") {
 			@Override
 			public void run() {
 				PowderIntegrationTool.this.fullImageJob.setAxisType(XAxis.Q);
 				xAxis = XAxis.Q;
 				axisSelect.setSelectedAction(this);
 				update(null);
 			}
 		};
 
 		final Action tthAction = new Action("2\u03b8") {
 			@Override
 			public void run() {
 				PowderIntegrationTool.this.fullImageJob.setAxisType(XAxis.ANGLE);
 				xAxis = XAxis.ANGLE;
 				axisSelect.setSelectedAction(this);
 				update(null);
 			}
 
 		};
 		
 		final MenuAction corrections= new MenuAction("Corrections");
 		final Action solidAngle = new Action("Solid Angle Correction",IAction.AS_CHECK_BOX) {
 			@Override
 			public void run() {
 				correctSolidAngle = isChecked();
 				update(null);
 			}
 
 		};
 		
 		corrections.setImageDescriptor(Activator.getImageDescriptor("icons/function.png"));
 
 		final Action clearImported = new Action("Clear imported metadata") {
 			@Override
 			public void run() {
 				importedMeta = null;
 				fullImageJob = null;
 				this.setEnabled(false);
 				update(null);
 			}
 		};
 		
 		clearImported.setEnabled(false);
 		
 		final MenuAction loadMeta= new MenuAction("Experimental metadata");
 		loadMeta.setImageDescriptor(Activator.getImageDescriptor("icons/detectorlight.png"));
 		final Action loadMetaAction = new Action("Import metadata from NeXus file") {
 			@Override
 			public void run() {
 				try {
 
 					FileDialog dialog =  new FileDialog(getViewPart().getSite().getShell(), SWT.OPEN);
 					dialog.setText("Open metadata file");
 					dialog.setFilterExtensions(new String[]{"*.nxs"});
 					dialog.open();
 					String[] names =  dialog.getFileNames();
 					if (names.length == 0) return;
 
 					NexusDiffractionMetaReader nexusDiffReader = new NexusDiffractionMetaReader(dialog.getFilterPath() + File.separator + names[0]);
 
 					IDiffractionMetadata md = nexusDiffReader.getDiffractionMetadataFromNexus(null);
 
 					if (nexusDiffReader.isPartialRead()) {
 						importedMeta = md;
 						fullImageJob = null;
 						logger.debug("meta loaded from file");
 						clearImported.setEnabled(true);
 						system.clear();
 						update(null);
 					} else {
 						MessageDialog.openError(
 								getViewPart().getSite().getShell(),
 								"Error!",
 								"Metadata could not be read from file!");
 					}
 
 				} catch (Exception e) {
 					MessageDialog.openError(
 							getViewPart().getSite().getShell(),
 							"Error!",
 							"Metadata could not be read from file: " + e.getMessage());
 				}
 				return;
 			}
 
 		};
 		
 		//if (importedMeta == null) clearImported.setEnabled(false);
 		
 		loadMetaAction.setImageDescriptor(Activator.getImageDescriptor("icons/mask-import-wiz.png"));
 		clearImported.setImageDescriptor(Activator.getImageDescriptor("icons/delete.gif"));
 		
 		loadMeta.add(loadMetaAction);
 		loadMeta.add(clearImported);
 		
 		modeSelect.add(nonAction);
 		modeSelect.add(splitAction);
 		modeSelect.add(split2DAction);
 		modeSelect.setSelectedAction(nonAction);
 		
 		axisSelect.add(qAction);
 		axisSelect.add(tthAction);
 		axisSelect.setSelectedAction(qAction);
 		
 		corrections.add(solidAngle);
 		
 		getSite().getActionBars().getToolBarManager().add(modeSelect);
 		getSite().getActionBars().getMenuManager().add(modeSelect);
 		
 		getSite().getActionBars().getToolBarManager().add(axisSelect);
 		getSite().getActionBars().getMenuManager().add(axisSelect);
 		
 		getSite().getActionBars().getToolBarManager().add(corrections);
 		getSite().getActionBars().getMenuManager().add(corrections);
 		
 		getSite().getActionBars().getToolBarManager().add(loadMeta);
 		getSite().getActionBars().getMenuManager().add(loadMeta);
 	}
 
 	@Override
 	public Control getControl() {
 		return baseComposite;
 	}
 
 	@Override
 	public void setFocus() {
 		if (system != null) system.setFocus();
 	}
 	
 	private void update(IDataset ds) {
 		
 		if (system == null) return; 
 		if (system.getPlotComposite() == null) return;
 		
 		IImageTrace im = getImageTrace();
 		if (ds == null && im == null) return;
 		if (ds == null && im != null) ds = im.getData();
 		
 		logger.debug("Update");
 		
 		//Imported metadata first
 		if (importedMeta != null) {
 			DetectorProperties d = importedMeta.getDetector2DProperties();
 			if(d.getPx() != ds.getShape()[1] || d.getPy() != ds.getShape()[0])  {
 				statusMessage.setText("Data shape not compatible with current metadata!!!");
 				statusMessage.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
 				return;
 			} else {
 				statusMessage.setText("Using imported metadata");
 				statusMessage.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY));
 			}
			if (fullImageJob == null) fullImageJob = new PowderIntegrationJob(importedMeta, system);
 		} else {
 			
 			IDiffractionMetadata m = getUpdatedMetadata(ds, statusString);
 			
 			if (m == null && metadata == null) return;
 
 			if (metadata == null) {
 				metadata = m;
 				fullImageJob = new PowderIntegrationJob(metadata, system);
 			} else {
 				if (m != null && (!metadata.getDetector2DProperties().equals(m.getDetector2DProperties()) ||
 						!metadata.getDiffractionCrystalEnvironment().equals(m.getDiffractionCrystalEnvironment()))) {
 					metadata = m;
 					fullImageJob = new PowderIntegrationJob(metadata, system);
 					statusMessage.setText("Meta data updated");
 					statusMessage.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY));
 				}
 			}
 			
 			if (fullImageJob == null) fullImageJob = new PowderIntegrationJob(metadata, system);
 		}
 		
 		AbstractDataset mask = null;
 		
 		if (im != null) mask = DatasetUtils.convertToAbstractDataset(im.getMask());
 		
 		fullImageJob.setData(DatasetUtils.convertToAbstractDataset(ds),
 				mask, null);
 		
 		fullImageJob.setAxisType(xAxis);
 		fullImageJob.setIntegrationMode(mode);
 		fullImageJob.setCorrectSolidAngle(correctSolidAngle);
 		
 		fullImageJob.schedule();
 	}
 	
 	private IDiffractionMetadata getUpdatedMetadata(IDataset ds, String[] statusString) {
 		
 		//check class metadata ok
 		if (metadata != null) {
 			DetectorProperties d = metadata.getDetector2DProperties();
 			if(d.getPx() != ds.getShape()[1] || d.getPy() != ds.getShape()[0])  {
 				metadata = null;
 				statusMessage.setText("Data shape not compatible with current metadata");
 				statusMessage.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
 			} else {
 				statusMessage.setText("Metadata OK");
 				statusMessage.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY));
 			}
 		}
 		
 		//look in data set
 		IDiffractionMetadata m = null;
 		if (ds.getMetadata() != null && ds.getMetadata() instanceof IDiffractionMetadata) {
 			m = (IDiffractionMetadata)ds.getMetadata();
 			statusMessage.setText("Metadata from data set");
 			statusMessage.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY));
 		}
 		
 		//read from preferences first time
 		if (m == null && metadata == null) m = getDiffractionMetaData(ds);
 		
 		if (m != null) {
 			if (statusString[0] != null) {
 				statusMessage.setText(statusString[0]);
 				statusString[0] = null;
 			}
 		}
 		
 		return m;
 	}
 	
 	
 	private IDiffractionMetadata getDiffractionMetaData(IDataset image) {
 		IWorkbenchPart part = getPart();
 		String altPath = null;
 		if(part instanceof IEditorPart){
 			altPath = EclipseUtils.getFilePath(((IEditorPart) part).getEditorInput());
 		} else if (part instanceof IViewPart){
 			try {
 				if (image == null) return null;
 				IMetaData md = image.getMetadata();
 				if(md != null)
 					altPath = md.getFilePath();
 			} catch (Exception e) {
 				logger.debug("Exception getting the image metadata", e);
 			}
 		}
 		return DiffractionUtils.getDiffractionMetadata(image, altPath, service, statusString);
 	}
 	
 	/**
 	 * Same tool called recursively from the DataReductionWizard
 	 */
 	@Override
 	public DataReductionInfo export(DataReductionSlice slice) throws Exception {
 		
 		if (fullImageJob == null) throw new IllegalArgumentException("Integration not correctly configured!");
 		
 		IHierarchicalDataFile file = slice.getFile();
 		Group dataGroup = slice.getParent();
 		
 		Group resultGroup = file.group("integration_result", dataGroup);
 		file.setNexusAttribute(resultGroup, Nexus.DATA);
 		slice.setParent(resultGroup);
 		
 		List<AbstractDataset> out = fullImageJob.process(DatasetUtils.convertToAbstractDataset(slice.getData()));
 		
 		AbstractDataset axis = out.get(0);
 		
 		//Test if axis is made
 		boolean axisMade = false;
 		
 		for (HObject ob : resultGroup.getMemberList()) {
 			if (ob.getName().equals(axis.getName())) { 
 				axisMade = true;
 				break;
 			}
 		}
 
 		if (!axisMade) {
 			H5Datatype dType = new H5Datatype(Datatype.CLASS_FLOAT, 64/8, Datatype.NATIVE, Datatype.NATIVE);
 			axis = axis.squeeze();
 			Dataset s = file.createDataset(axis.getName(),  dType,  new long[]{axis.getShape()[0]}, axis.getBuffer(), resultGroup);
 			UnitFormat unitFormat = UnitFormat.getUCUMInstance();
 			
 			switch (xAxis) {
 			case Q:
 				String angstrom = unitFormat.format(NonSI.ANGSTROM.inverse());
 				file.setAttribute(s, "units", angstrom);
 				break;
 			case ANGLE:
 				String degrees = unitFormat.format(NonSI.DEGREE_ANGLE);
 				file.setAttribute(s, "units", degrees);
 				break;
 
 			default:
 				break;
 			}
 			
 			file.setNexusAttribute(s, Nexus.SDS);
 			
 			if (mode == IntegrationMode.SPLITTING2D) {
 				file.setIntAttribute(s, NexusUtils.AXIS, 3);
 				axis = out.get(2);
 				axis = axis.squeeze();
 				Dataset s2 = file.createDataset(axis.getName(),  dType,  new long[]{axis.getShape()[0]}, axis.getBuffer(), resultGroup);
 				file.setAttribute(s2, "units", unitFormat.format(NonSI.DEGREE_ANGLE));
 				file.setIntAttribute(s2, NexusUtils.AXIS, 2);
 				
 			} else {
 				file.setIntAttribute(s, NexusUtils.AXIS, 2);
 			}
 			
 			IPersistenceService service = (IPersistenceService)ServiceManager.getService(IPersistenceService.class);
 			IPersistentFile pf = service.createPersistentFile(file);
 			pf.setDiffractionMetadata(metadata);
 		}
 		
 		AbstractDataset signal = out.get(1);
 		signal.setName("data");
 		slice.appendData(signal);
 		
 		if (!axisMade) {
 			for (HObject ob :resultGroup.getMemberList()) {
 				if (ob instanceof Dataset && ob.getName().equals(signal.getName())) {
 					Dataset ds = (Dataset)ob;
 					file.setIntAttribute(ds, NexusUtils.SIGNAL, 1);
 				}
 			}
 		}
 		
 		return new DataReductionInfo(Status.OK_STATUS);
 	}
 	
 	@Override
 	public Object getAdapter(@SuppressWarnings("rawtypes") Class clazz) {
 		if (clazz == IToolPageSystem.class) {
 			return system;
 		} else {
 			return super.getAdapter(clazz);
 		}
 	}
 
 }
