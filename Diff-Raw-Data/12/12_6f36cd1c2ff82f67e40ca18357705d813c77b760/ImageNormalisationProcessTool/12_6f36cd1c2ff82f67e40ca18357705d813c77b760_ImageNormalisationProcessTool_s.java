 package org.dawnsci.plotting.tools.processing;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.dawb.common.util.io.IOUtils;
 import org.dawnsci.common.widgets.content.FileContentProposalProvider;
 import org.dawnsci.common.widgets.radio.RadioGroupWidget;
 import org.dawnsci.plotting.api.IPlottingSystem;
 import org.dawnsci.plotting.api.region.IRegion.RegionType;
 import org.dawnsci.plotting.api.trace.IImageTrace;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.fieldassist.ContentProposalAdapter;
 import org.eclipse.jface.fieldassist.TextContentAdapter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.DropTarget;
 import org.eclipse.swt.dnd.DropTargetAdapter;
 import org.eclipse.swt.dnd.DropTargetEvent;
 import org.eclipse.swt.dnd.FileTransfer;
 import org.eclipse.swt.dnd.TextTransfer;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Spinner;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.part.ResourceTransfer;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
 import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
 import uk.ac.diamond.scisoft.analysis.optimize.ApachePolynomial;
 import uk.ac.diamond.scisoft.analysis.roi.IROI;
 import uk.ac.diamond.scisoft.analysis.roi.ROIProfile;
 import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
 
 /**
  * Tool to normalize an image with given specific parameters
  * @author wqk87977
  *
  */
 public class ImageNormalisationProcessTool extends ImageProcessingTool {
 
 	private final Logger logger = LoggerFactory.getLogger(ImageNormalisationProcessTool.class);
 	private AbstractDataset profile;
 
 	private Spinner smoothingSpinner;
 	private int smoothLevel = 1;
 
 	private enum NormaliseType{
 		NONE, ROI, AUX;
 	}
 
 	private NormaliseType type = NormaliseType.NONE;
 
 	private Text inputLocation;
 	private String inputFile;
 	private Button inputBrowse;
 
 	public ImageNormalisationProcessTool() {
 	}
 
 	@Override
 	protected void configureSelectionPlottingSystem(IPlottingSystem plotter) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	protected void configureReviewPlottingSystem(IPlottingSystem plotter) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	protected void createControlComposite(Composite parent) {
 		try {
 			Group radioGroupNorm = new Group(parent, SWT.NONE);
 			radioGroupNorm.setLayout(new GridLayout(1, false));
 			radioGroupNorm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 			radioGroupNorm.setText("Normalisation type");
 			RadioGroupWidget normActionRadios = new RadioGroupWidget(radioGroupNorm);
 			normActionRadios.setActions(createNormActions());
 
 			Composite auxComp = new Composite(radioGroupNorm, SWT.NONE);
 			auxComp.setLayout(new GridLayout(1, false));
 			auxComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 			createInputField(auxComp);
 
 			Composite smoothingComp = new Composite(parent, SWT.NONE);
 			smoothingComp.setLayout(new GridLayout(2, false));
 			Label smoothingLabel = new Label(smoothingComp, SWT.NONE);
 			smoothingLabel.setText("Smoothing:");
 			smoothingSpinner = new Spinner(smoothingComp, SWT.BORDER);
 			smoothingSpinner.setMinimum(1);
 			smoothingSpinner.setMaximum(Integer.MAX_VALUE);
 			smoothingSpinner.addSelectionListener(new SelectionListener() {
 				@Override
 				public void widgetSelected(SelectionEvent e) {
 					widgetDefaultSelected(e);
 				}
 				@Override
 				public void widgetDefaultSelected(SelectionEvent e) {
 					smoothLevel = smoothingSpinner.getSelection();
 					updateProfiles(false);
 				}
 			});
 		} catch (Exception e) {
 			logger.error("Could not create controls:"+e);
 		}
 	}
 
 	private List<Action> createNormActions(){
 		List<Action> radioActions = new ArrayList<Action>();
 
 		Action noNormalisationAction = new Action() {
 			@Override
 			public void run() {
 				type = NormaliseType.NONE;
 				updateProfiles(true);
 				setInputFieldEnabled(false);
 			}
 		};
 		noNormalisationAction.setText("None");
 		noNormalisationAction.setToolTipText("None/Reset");
 
 		Action roiNormalisationAction = new Action() {
 			@Override
 			public void run() {
 				type = NormaliseType.ROI;
 				selectionPlottingSystem.updatePlot2D(originalData, originalAxes, null);
 				updateProfiles(true);
 				setInputFieldEnabled(false);
 			}
 		};
 		roiNormalisationAction.setText("ROI normalisation");
 		roiNormalisationAction.setToolTipText("ROI normalisation");
 
 		Action auxNormalisationAction = new Action() {
 			@Override
 			public void run() {
 				type = NormaliseType.AUX;
 				if(auxiliaryData != null)
 					selectionPlottingSystem.updatePlot2D((IDataset)auxiliaryData.squeeze(), originalAxes, null);
 				updateProfiles(true);
 				setInputFieldEnabled(true);
 			}
 		};
 		auxNormalisationAction.setText("Auxiliary normalisation");
 		auxNormalisationAction.setToolTipText("Auxiliary normalisation");
 
 		radioActions.add(noNormalisationAction);
 		radioActions.add(roiNormalisationAction);
 		radioActions.add(auxNormalisationAction);
 		return radioActions;
 	}
 
 	private void createInputField(Composite parent){
 		Label lb = new Label(parent, SWT.NONE);
 		lb.setText("Choose Gold Calibration:");
 
 		Composite comp = new Composite(parent, SWT.NONE);
 		comp.setLayout(new GridLayout(2, false));
 		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 
 		inputLocation = new Text(comp, SWT.BORDER);
 		if (inputFile != null)
 			inputLocation.setText(inputFile);
 		inputLocation.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
 		FileContentProposalProvider prov = new FileContentProposalProvider();
 		ContentProposalAdapter ad = new ContentProposalAdapter(inputLocation, 
 				new TextContentAdapter(), prov, null, null);
 		ad.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
 		inputLocation.setToolTipText("Input file location");
 		inputLocation.addSelectionListener(getInputLocationListener());
 
 		//add drop support
 		DropTarget dt = new DropTarget(inputLocation, DND.DROP_MOVE
 				| DND.DROP_DEFAULT | DND.DROP_COPY);
 		dt.setTransfer(new Transfer[] { TextTransfer.getInstance(),
 				FileTransfer.getInstance(), ResourceTransfer.getInstance() });
 		dt.addDropListener(getDropTargetEvent());
 
 		inputBrowse = new Button(comp, SWT.NONE);
 		inputBrowse.setText("...");
 		inputBrowse.setToolTipText("Choose Auxiliary Data Input");
 		inputBrowse.addSelectionListener(getInputBrowseListener());
 
 		setInputFieldEnabled(false);
 	}
 
 	private void setInputFieldEnabled(boolean value){
 		inputLocation.setEnabled(value);
 		inputBrowse.setEnabled(value);
 	}
 
 	private SelectionListener getInputLocationListener(){
 		return new SelectionAdapter() {
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e) {
 				final String path = inputLocation.getText();
 				loadAuxiliaryData(path);
 //				else {
 //					inputLocation.setText(inputFile);
 //				}
 			}
 		};
 	}
 
 	private SelectionListener getInputBrowseListener(){
 		return new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				FileDialog fChooser = new FileDialog(getSite().getShell());
 				fChooser.setText("Choose File to load from");
 				fChooser.setFilterPath(inputFile);
 				final String path = fChooser.open();
 				loadAuxiliaryData(path);
 			}
 		};
 	}
 	
 	private DropTargetAdapter getDropTargetEvent(){
 		return new DropTargetAdapter() {
 			@Override
 			public void drop(DropTargetEvent event) {
 				String path = null;
 				Object data = event.data;
 				if (data instanceof IResource[]) {
 					IResource[] res = (IResource[]) data;
 					path = res[0].getRawLocation().toOSString();
 				} else if (data instanceof File[]) {
 					path = ((File[]) data)[0].getAbsolutePath();
 				}
 				if (path != null) {
 					inputLocation.setText(path);
 					loadAuxiliaryData(path);
 				}
 			}
 		};
 	}
 
 	private void loadAuxiliaryData(String path){
 		if (path == null)return;
 		if (IOUtils.checkFile(path, true)) {
 			inputFile = path;
 			auxiliaryData = loadDataset(path);
 			if(auxiliaryData == null) return;
 //			if (!auxiliaryDatasets.containsKey(currentAuxData.getName()))
 //				addDataset(currentAuxData);
 //			else
 //				currentAuxData = auxiliaryDatasets.get(currentAuxData.getName());
 			
 			List<? extends IDataset> axes = getImageTrace().getAxes();
 			selectionPlottingSystem.updatePlot2D((IDataset)auxiliaryData.squeeze(), axes, null);
 
 			updateProfiles(true);
 		}
 	}
 
 	@Override
 	protected void createSelectionProfile(IImageTrace image, IROI roi, IProgressMonitor monitor) {
 		AbstractDataset data = (AbstractDataset)image.getData();
 		AbstractDataset ds = data.clone();
 
 		if(type.equals(NormaliseType.NONE)) {
 			if(originalData == null) return;
 			IDataset currentData = getPlottingSystem().getTraces().iterator().next().getData();
 			if(!currentData.equals(originalData))
 				getPlottingSystem().updatePlot2D(originalData, originalAxes, monitor);
 
 			if(selectionPlottingSystem.getTraces().size()==0){
 				selectionPlottingSystem.updatePlot2D(originalData, originalAxes, monitor);
 			} else {
 				IDataset selectionData = selectionPlottingSystem.getTraces().iterator().next().getData();
 				if(!selectionData.equals(originalData))
 					selectionPlottingSystem.updatePlot2D(originalData, originalAxes, monitor);
 			}
 		} else if (type.equals(NormaliseType.ROI) || type.equals(NormaliseType.AUX)){
 			if(roi == null) return;
 	
 			double width = ((RectangularROI)roi).getLengths()[0];
 
 			AbstractDataset[] profiles = ROIProfile.box(ds, (RectangularROI)roi);
 			profile = profiles[1];
 			profile.idivide(width);
 		}
 	}
 
 	@Override
 	protected void createReviewProfile(IProgressMonitor monitor) {
 		List<IDataset> data = new ArrayList<IDataset>();
 		if(type.equals(NormaliseType.NONE)){
 			data.add(new IntegerDataset(originalData.getShape()[0]));
 			data.get(0).setName("Norm");
 			reviewPlottingSystem.updatePlot1D(originalAxes.get(1), data, monitor);
 		} else if(type.equals(NormaliseType.ROI) || type.equals(NormaliseType.AUX)){
 			// smooth the review plot
 			AbstractDataset tmpProfile = profile.clone();
 			
 			if(smoothLevel > 1){
 				try {
 					tmpProfile = ApachePolynomial.getPolynomialSmoothed((AbstractDataset)originalAxes.get(1), profile, smoothLevel, 3);
 				} catch (Exception e) {
 					logger.error("Could not smooth the plot:"+e);
 				}
 			}
 			
			data.add(tmpProfile);
 			data.get(0).setName("Norm");
 			reviewPlottingSystem.updatePlot1D(originalAxes.get(1), data, monitor);
 
 			AbstractDataset tile = tmpProfile.reshape(tmpProfile.getShape()[0],1);
 
 			AbstractDataset ds = (AbstractDataset) originalData.clone();
 
 			AbstractDataset correctionDataset = DatasetUtils.tile(tile, ds.getShape()[1]);
 			ds.idivide(correctionDataset);
 
 			userPlotBean.addList("norm", ds.clone());
 			userPlotBean.addList("norm_profile", profile.clone());
 			userPlotBean.addList("norm_correction", correctionDataset.clone());
 			
 			getPlottingSystem().updatePlot2D(ds, originalAxes, monitor);
 		}
 	}
 
 	@Override
 	protected RegionType getCreateRegionType() {
 		return RegionType.XAXIS;
 	}
 
 	private IDataset loadDataset(final String path) {
 		try {
 			auxiliaryData = LoaderFactory.getDataSet(path,
 					"/entry1/analyser/data", null);
 		
 			if (auxiliaryData != null) return auxiliaryData;
 					
 			auxiliaryData = LoaderFactory.getDataSet(path,
 					"/entry/calibration/analyser/data", null);
 			return auxiliaryData;
 		
 		} catch (Exception e) {
 			logger.warn("Could not open file correctly");
 		}
 		return null;
 	}
 	
 }
