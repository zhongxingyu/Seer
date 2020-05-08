 /*
  * Copyright (c) 2012 European Synchrotron Radiation Facility,
  *                    Diamond Light Source Ltd.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */ 
 
 package org.dawb.workbench.ui.editors;
 
 import java.io.File;
 import java.text.NumberFormat;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.TreeSet;
 
 import org.dawb.common.services.ILoaderService;
 import org.dawb.common.services.ServiceManager;
 import org.dawb.common.ui.plot.AbstractPlottingSystem;
 import org.dawb.common.ui.plot.AbstractPlottingSystem.ColorOption;
 import org.dawb.common.ui.plot.PlotType;
 import org.dawb.common.ui.plot.PlottingFactory;
 import org.dawb.common.ui.slicing.SliceUtils;
 import org.dawb.common.ui.util.EclipseUtils;
 import org.dawb.common.ui.util.GridUtils;
 import org.dawb.common.ui.views.HeaderTablePage;
 import org.dawb.common.ui.widgets.ActionBarWrapper;
 import org.dawb.gda.extensions.loaders.LoaderService;
 import org.dawb.workbench.ui.Activator;
 import org.dawb.workbench.ui.views.PlotDataPage;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.action.ToolBarManager;
 import org.eclipse.jface.preference.PreferenceDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.SWTException;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.ImageData;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Slider;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.widgets.ToolBar;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.IActionBars2;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.IReusableEditor;
 import org.eclipse.ui.IShowEditorInput;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.dialogs.PreferencesUtil;
 import org.eclipse.ui.ide.FileStoreEditorInput;
 import org.eclipse.ui.internal.WorkbenchPage;
 import org.eclipse.ui.part.EditorPart;
 import org.eclipse.ui.part.FileEditorInput;
 import org.eclipse.ui.part.Page;
import org.embl.cca.utils.imageviewer.MemoryImageEditorInput;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 //import fable.framework.logging.FableLogger;
import org.embl.cca.utils.imageviewer.ExecutableManager;
import org.embl.cca.utils.imageviewer.FilenameCaseInsensitiveComparator;
import org.embl.cca.utils.imageviewer.TrackableJob;
import org.embl.cca.utils.imageviewer.TrackableRunnable;
import org.embl.cca.utils.imageviewer.WildCardFileFilter;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.ByteDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.FloatDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.ShortDataset;
 
 
 /**
  * An editor which combines a plot with a graph of data sets.
  * 
  * Currently this is for 1D analyses only so if the data does not contain 1D, this
  * editor will not show.
  * 
  */
 public class PlotImageEditor extends EditorPart implements IReusableEditor, IShowEditorInput {
 	
 	public static final String ID = "org.dawb.workbench.editors.plotImageEditor";
 
 	private static Logger logger = LoggerFactory.getLogger(PlotImageEditor.class);
 	
 	// This view is a composite of two other views.
 	private AbstractPlottingSystem      plottingSystem;	
 	private Composite                   tools;
 
 //	private Label currentSliderImageLabel;
 	private Label totalSliderImageLabel;
 	private Slider imageSlider;
 	private Text imageFilesWindowWidthText;
 	private int imageFilesWindowWidth; //aka batchAmount
 	private File[] allImageFiles;
 //	private File[] loadedImageFiles;
 //	private TreeSet<Integer> toLoadImageFilesIndices; //Indices in toLoadImagesFiles which are going to be loaded
 	private TreeSet<File> loadedImageFiles; //Indices in loadedImagesFiles which are loaded
 	boolean autoFollow;
 	Button imageFilesAutoLatestButton;
 	AbstractDataset resultSet = null;
 	static private NumberFormat decimalFormat = NumberFormat.getNumberInstance();
 
 	ExecutableManager imageLoaderManager = null;
 	Thread imageFilesAutoLatestThread = null;
 
 	public PlotImageEditor() {
 	
 		try {
 	        this.plottingSystem = PlottingFactory.getPlottingSystem();
 	        plottingSystem.setColorOption(ColorOption.NONE);
 		} catch (Exception ne) {
 			logger.error("Cannot locate any plotting systems!", ne);
 		}
  	}
 
 	@Override
 	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
 		
 		setSite(site);
 		super.setInput(input);
 		setPartName(input.getName());	
 	}
 	
 	@Override
 	public void setInput(final IEditorInput input) {
 		super.setInput(input);
 		setPartName(input.getName());
 		editorInputChanged();
 	}
 
 
 	@Override
 	public boolean isDirty() {
 		return false;
 	}
 	
 
 	public void setToolbarsVisible(boolean isVisible) {
 		GridUtils.setVisible(tools, isVisible);
 		tools.getParent().layout(new Control[]{tools});
 	}
 /*
 	private void initSlider( int amount ){ 
 		//if(!label.isDisposed() && label !=null){  
 //		currentSliderImageLabel.setText("");//new Integer(selection).toString());
 		imageSlider.setValues( 1, 1, amount+1, 1, 1, Math.max(1, amount/5) );
 		totalSliderImageLabel.setText( "1" + "/" + amount );
 		totalSliderImageLabel.getParent().pack();
 		//}  
 	}  
 */
 	private void updateSlider( int sel ) {
 		synchronized (imageSlider) {
 			final int min = 1;
 			final int total = allImageFiles.length;
 			final int selection = Math.max(Math.min(sel,total + 1),min);
 			
 			try {  
 				if( imageSlider == null || imageSlider.isDisposed() )
 					return;
 //			if( imageSlider.getSelection() == selection )
 //				return;
 //			currentSliderImageLabel.setText("");//new Integer(selection).toString());
 				imageFilesWindowWidth = imageSlider.getThumb();
 				imageSlider.setValues(selection, min, total+1, imageFilesWindowWidth, 1, Math.max(imageFilesWindowWidth, total/5));
 				totalSliderImageLabel.setText( "" + selection + "/" + total + "   ");
 				totalSliderImageLabel.getParent().pack();
 				sliderMoved( selection );
 //			imageLoaderTracker = RunnableTracker.addRequest(new TrackableRunnable(imageLoaderTracker) {
 //				public void runThis() {
 //					sliderMoved( selection );
 //				}
 //			});
 //			Display.getDefault().asyncExec(new Runnable(){  
 //				public void run() {  
 //					sliderMoved( selection );
 //				}  
 //			});
 			} catch (SWTException e) {  
 				//eat it!  
 			}  
 		}
 	}
 	
 	private void updateBatchAmount( int amount ) {
 		synchronized (imageSlider) {
 //			if( resultSetOriginalDatatype != AbstractDataset.FLOAT32 )
 //				amount = 1;
 			if( imageFilesWindowWidth == amount )
 				return;
 			int oldSel = imageSlider.getSelection();
 			int newSel = oldSel;
 			if( amount < 1 )
 				amount = 1;
 			else if( amount > imageSlider.getMaximum() - oldSel && oldSel > 1 ) {
 				newSel = imageSlider.getMaximum() - amount;
 				if( newSel < 1 ) {
 					newSel = 1;
 					amount = imageSlider.getMaximum() - newSel;
 				}
 //				amount = imageSlider.getMaximum() - imageSlider.getSelection();
 			}
 			imageSlider.setThumb( amount );
 			if( oldSel != newSel )
 				imageSlider.setSelection( newSel );
 			else
 				updateSlider( newSel );
 /*
 			imageSlider.setSelection(imageSlider.getSelection());
 
 			imageFilesWindowWidth = amount;
 			imageFilesWindowWidthText.setText( "" + amount );
 			imageFilesWindowWidthText.getParent().pack();
 			sliderMoved( imageSlider.getSelection() ); //Updates loaded files and draw image
 */
 		}
 	}
 
 	private void sliderMoved( int pos ) {
 		File[] toLoadImageFiles = null;
 		synchronized (allImageFiles) {
 			int iMax = imageFilesWindowWidth;
 			toLoadImageFiles = new File[iMax];
 			for( int i = 0; i < iMax; i++ )
 				toLoadImageFiles[ i ] = allImageFiles[ pos - 1 + i ];
 		}
 		createPlot(toLoadImageFiles);
 	}
 
 	public void onImageFilesAutoLatestButtonSelected() {
 		if( autoFollow != imageFilesAutoLatestButton.getSelection() ) {
 			autoFollow = imageFilesAutoLatestButton.getSelection();
 			if( autoFollow ) {
 	//			imageSlider.setEnabled( false );
 				imageFilesAutoLatestThread = new Thread() {
 					ExecutableManager imageFilesAutoLatestManager = null;
 					protected boolean checkDirectory() {
 						final IPath imageFilename = getPath( getEditorInput() );
 						final File[] currentAllImageFiles = listIndexedFilesOf( imageFilename );
 						TreeSet<File> currentAllImageFilesSet = new TreeSet<File>( Arrays.asList(currentAllImageFiles) );
 						TreeSet<File> allImageFilesSet = new TreeSet<File>( Arrays.asList(allImageFiles) );
 						if( currentAllImageFilesSet.containsAll(allImageFilesSet)
 								&& allImageFilesSet.containsAll(currentAllImageFilesSet) )
 							return false;
 						if( imageLoaderManager.isAlive() )
 							return false;
 						final TrackableRunnable runnable = new TrackableRunnable(imageFilesAutoLatestManager) {
 							@Override
 							public void runThis() {
 								synchronized (imageSlider) {
 									allImageFiles = currentAllImageFiles; 
 									updateSlider( allImageFiles.length - imageFilesWindowWidth + 1 );
 								}
 							}
 						};
 						imageFilesAutoLatestManager = ExecutableManager.addRequest(runnable);
 						return true;
 					}
 					@Override
 					public void run() {
 						do {
 							int sleepTime = 10; //Sleeping some even if directory updated, so user can move slider (and abort this thread)
 							if( !checkDirectory() )
 								sleepTime = 100;
 							try {
 								sleep(sleepTime);
 							} catch (InterruptedException e) {
 								break;
 							}
 						} while( true );
 					}
 					@Override
 					public void interrupt() {
 						if( imageFilesAutoLatestManager != null )
 							imageFilesAutoLatestManager.interrupt();
 						super.interrupt();
 					}
 				};
 				imageFilesAutoLatestThread.start();
 			} else {
 				imageFilesAutoLatestThread.interrupt();
 	//			imageSlider.setEnabled( true );
 			}
 		}
 	}
 
 	private void createImageSelectorUI(Composite parent) {
 		final Composite sliderMain = new Composite(parent, SWT.NONE);
 		sliderMain.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
 		sliderMain.setLayout(new GridLayout(5, false));
 		GridUtils.removeMargins(sliderMain);
 		
 //		currentSliderImageLabel = new Label(sliderMain, SWT.NULL);
 //		currentSliderImageLabel.setToolTipText("Currently selected image");
 //		currentSliderImageLabel.setText("0");
 		imageSlider = new Slider(sliderMain, SWT.HORIZONTAL);
 		imageSlider.setThumb(imageFilesWindowWidth);
 //		imageSlider.setBounds(115, 50, 25, 15);
 		totalSliderImageLabel = new Label(sliderMain, SWT.NONE);
 		totalSliderImageLabel.setToolTipText("Selected image/Number of images");
 		totalSliderImageLabel.setText("0/0");
 		imageSlider.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 //				imageSlider.setSelection( imageSlider.getSelection() );
 				if( autoFollow ) {
 					imageFilesAutoLatestButton.setSelection( false );
 					//setSelection does not trigger the Selection event because we are in Selection event here already,
 					onImageFilesAutoLatestButtonSelected(); //so we have to call it manually, which is lame.
 				}
 				updateSlider( imageSlider.getSelection() );
 			}
 		});
 		final Label imageFilesWindowWidthLabel = new Label(sliderMain, SWT.NONE);
 		imageFilesWindowWidthLabel.setToolTipText("Number of images to sum up");
 		imageFilesWindowWidthLabel.setText("Batch Amount");
 		imageFilesWindowWidthText = new Text(sliderMain, SWT.BORDER | SWT.RIGHT);
 		imageFilesWindowWidthText.setToolTipText(imageFilesWindowWidthLabel.getToolTipText());
 		imageFilesWindowWidthText.setText( "" + imageFilesWindowWidth );
 		imageFilesWindowWidthText.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
 		imageFilesWindowWidthText.addModifyListener(new ModifyListener() {
 			@Override
 			public void modifyText(ModifyEvent e) {
 				if( imageFilesWindowWidthText == null || imageFilesWindowWidthText.isDisposed() ) return;
 				if( !imageFilesWindowWidthText.isEnabled() || imageFilesWindowWidthText.getText().isEmpty() )
 					return;
 				try {
 					updateBatchAmount( decimalFormat.parse( imageFilesWindowWidthText.getText() ).intValue() );
 				} catch (ParseException exc) {
 					logger.error("Unable to parse batch amount value: " + imageFilesWindowWidthText.getText(), exc);
 				}
 			}
 		});
 //		imageFilesWindowWidthText.setEnabled(false);
 		imageFilesAutoLatestButton = new Button(sliderMain, SWT.CHECK);
 		imageFilesAutoLatestButton.setText("Auto latest");
 		imageFilesAutoLatestButton.setToolTipText("Automatically scan directory and display last batch");
 		autoFollow = false;
 		imageFilesAutoLatestButton.addSelectionListener(new SelectionListener() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				onImageFilesAutoLatestButtonSelected();
 			}
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e) {
 			}
 		});
 	}
 
 	protected IPath getPath( IEditorInput editorInput ) {
 		final IPath imageFilename;
 		if( editorInput instanceof FileEditorInput )
 			imageFilename = new Path( ((FileEditorInput)editorInput).getURI().getPath() ); 
 		else if( editorInput instanceof FileStoreEditorInput )
 			imageFilename = new Path( ((FileStoreEditorInput)editorInput).getURI().getPath() ); 
 		else {
 			IFile iF = (IFile)editorInput.getAdapter(IFile.class);
 			if( iF != null )
 				imageFilename = iF.getLocation().makeAbsolute();
 			else {
 				logger.error("Cannot determine full path of requested file");
 				return null;
 			}
 		}
 		return imageFilename;
 	}
 
 	protected File[] listIndexedFilesOf( IPath imageFilename ) {
 		File[] result = null;
 		String q = imageFilename.removeFileExtension().lastSegment().toString();
 		String r = q.replaceAll("[0-9]*$", "");
 		int len = q.length() - r.length();
 		for( int i = 0; i < len; i++ )
 		  r += "?";
 		r += "." + imageFilename.getFileExtension();
 		result = new File(imageFilename.removeLastSegments(1).toString()).listFiles( new WildCardFileFilter(r) );
 		Arrays.sort( result, new FilenameCaseInsensitiveComparator() );
 		return result;
 	}
 
 	@Override
 	public void createPartControl(final Composite parent) {
 		
 		final Composite  main       = new Composite(parent, SWT.NONE);
 		final GridLayout gridLayout = new GridLayout(1, false);
 		gridLayout.verticalSpacing = 0;
 		gridLayout.marginWidth = 0;
 		gridLayout.marginHeight = 0;
 		gridLayout.horizontalSpacing = 0;
 		main.setLayout(gridLayout);
 		
 		this.tools = new Composite(main, SWT.RIGHT);
 		tools.setLayout(new GridLayout(3, false));
 		GridUtils.removeMargins(tools);
 		tools.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
 
 		final Text point = new Text(tools, SWT.LEFT);
 		//EclipseUtils.setBold(point);
 
 		point.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
 		point.setEditable(false);
 		GridUtils.setVisible(point, true);
 		point.setBackground(tools.getBackground());
 		plottingSystem.setPointControls(point);
 
 		// We use a local toolbar to make it clear to the user the tools
 		// that they can use, also because the toolbar actions are 
 		// hard coded.
 		ToolBarManager toolMan = new ToolBarManager(SWT.FLAT|SWT.RIGHT);
 		final ToolBar  toolBar = toolMan.createControl(tools);
 		toolBar.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
 
 		ToolBarManager rightMan = new ToolBarManager(SWT.FLAT|SWT.RIGHT);
 		final ToolBar          rightBar = rightMan.createControl(tools);
 		rightBar.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
 
 		final MenuManager    menuMan = new MenuManager();
 		final IActionBars bars = this.getEditorSite().getActionBars();
 		ActionBarWrapper wrapper = new ActionBarWrapper(toolMan,menuMan,null,(IActionBars2)bars);
 
 		loadedImageFiles = new TreeSet<File>();
 		imageFilesWindowWidth = 1;
 		/* Top line containing image selector sliders */
 		createImageSelectorUI(tools);
 
 		// NOTE use name of input. This means that although two files of the same
 		// name could be opened, the editor name is clearly visible in the GUI and
 		// is usually short.
 		final String plotName = this.getEditorInput().getName();
 
 		final Composite plot = new Composite(main, SWT.NONE);
 		plot.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		plot.setLayout(new FillLayout());
 		
         plottingSystem.createPlotPart(plot, plotName, wrapper, PlotType.IMAGE, this);
 
         Action menuAction = new Action("", Activator.getImageDescriptor("/icons/DropDown.png")) {
 	    	@Override
 	    	public void run() {
 	    		final Menu   mbar = menuMan.createContextMenu(toolBar);
 	    		mbar.setVisible(true);
 	    	}
 	    };
 	    rightMan.add(menuAction);
 
 		if (toolMan!=null)  toolMan.update(true);
 		if (rightMan!=null) rightMan.update(true);
 
 		editorInputChanged();
  	}
 
 	private void editorInputChanged() {
 		if (getEditorInput() instanceof MemoryImageEditorInput) {
 			IEditorInput miei = getEditorInput();
 			final FloatDataset set; 
 			if ("ExpSimImgInput".equals(getEditorInput().getName())) {
 				//convert from in[] to float dataset
 				//set = new FloatDataset(new IntegerDataset(((MemoryImageEditorInput)getEditorInput()).getIntDataset(), 
 				//		new int[]{miei.getImageDescriptor().getImageData().width, miei.getImageDescriptor().getImageData().height})	);
 				set = new FloatDataset(((MemoryImageEditorInput)getEditorInput()).getFloatArray(), 
 						new int[]{miei.getImageDescriptor().getImageData().width, miei.getImageDescriptor().getImageData().height});
 			} else {
 				ImageData id = miei.getImageDescriptor().getImageData();
 				byte[] srcdata = id.data;
 				set = new FloatDataset(new IntegerDataset(srcdata, new int[] { id.width, id.height } ) );
 				System.out.println("First block of received image (set):");
 				for( int j = 0; j < 10; j++ ) {
 					for( int i = 0; i < 10; i++ ) {
 						System.out.print( " " + Integer.toHexString( set.getInt( j, i ) ) );
 					}
 					System.out.println();
 				}
 			}
 //			int jMax = miei.getImageDescriptor().getImageData().width * miei.getImageDescriptor().getImageData().height;
 //			byte[] bdata = set.getData();
 //			for( int j = 0; j < jMax; j++ )
 //				bdata[j] = srcdata[j];
 //			set.setDirty();
 			createPlot(set);
 		} else {
 		final IPath imageFilename = getPath( getEditorInput() );
 		allImageFiles = listIndexedFilesOf( imageFilename );
 		//final String filePath = EclipseUtils.getFilePath(getEditorInput());
 //		initSlider( allImageFiles.length );
 		String actFname = imageFilename.lastSegment().toString();
 		int pos;
 		for (pos = 0; pos < allImageFiles.length; pos++ )
 			if (allImageFiles[pos].getName().equals(actFname))
 				break;				
 		updateSlider( pos + 1 ); //it calls (and must call) createPlot()
 		}
  	}
 
 	private void createPlot(AbstractDataset set, IProgressMonitor monitor) {
 		final List<AbstractDataset> axes = new ArrayList<AbstractDataset>(2);
 		axes.add(SliceUtils.createAxisDataset((set.getShape()[0])));
 		axes.add(SliceUtils.createAxisDataset((set.getShape()[1])));
 		plottingSystem.createPlot2D(set, axes, monitor);
 	}
 
 	private void createPlot(final AbstractDataset set) {
 		final TrackableJob job = new TrackableJob(imageLoaderManager, "Read image data") {
 			public IStatus runThis(IProgressMonitor monitor) {
 				/* Since running this and others as well through imageLoaderManager,
 				 * the single access of loading data is guaranteed.
 				 */
 				IStatus result = Status.CANCEL_STATUS;
 				System.out.println("CreatePlot calling");
 				createPlot(set, monitor);
 				System.out.println("CreatePlot called");
 				result = Status.OK_STATUS;
 				return result;
 			}
 		};
 		job.setUser(false);
 		job.setPriority(Job.BUILD);
 		imageLoaderManager = ExecutableManager.setRequest(job);
 	}
 
 	private void createPlot(final File[] toLoadImageFiles) {
 		final TrackableJob job = new TrackableJob(imageLoaderManager, "Read image data") {
 			TreeSet<File> toLoadImageFilesJob = new TreeSet<File>( Arrays.asList(toLoadImageFiles) );
 			AbstractDataset set = null;
 			int resultSetOriginalDatatype = AbstractDataset.BOOL; //BOOL is set, which means NONE for us (such value is not available)
 
 			public IStatus processImage(File imageFile, boolean add) {
 				if( add || loadedImageFiles.size() > 1 ) {
 					final String filePath = imageFile.getAbsolutePath();
 					try {
 						set = new LoaderService().getDataset(filePath);
 					} catch (Throwable e) {
 						logger.error("Cannot load file "+filePath, e);
 						return Status.CANCEL_STATUS;
 					}
 					if (set==null || set.getShape()==null) {
 						logger.error("Cannot read file "+getEditorInput().getName());
 						return Status.CANCEL_STATUS;
 					}
 					if( isAborting() )
 						return Status.OK_STATUS;
 					resultSetOriginalDatatype = set.getDtype();
 					//Obsoleted: Accepting only set of float images, or 1 not float image
 					//Accepting set of all kind of images
 					if( loadedImageFiles.size() == 0 ) {
 						if( add )
 							resultSet = new FloatDataset(set);
 					} else {
 						if( resultSetOriginalDatatype != AbstractDataset.FLOAT32 ) {
 							set = new FloatDataset(set);
 							resultSetOriginalDatatype = AbstractDataset.FLOAT32;
 						}
 /*						if( add )
 							((FloatDataset)resultSet).iadd(set); //"Official" solution, but very slow
 						else
 							((FloatDataset)resultSet).isubtract(set); //"Official" solution, but very slow
 */						((FloatDataset)resultSet).checkCompatibility( set );
 						float[] fsetdata = ((FloatDataset)resultSet).getData();
 						float[] fdata = ((FloatDataset)set).getData();
 						int jMax = Math.min( fsetdata.length, fdata.length ); //Normally lengths can not differ
 						if( add )
 							for( int j = 0; j < jMax; j++ )
 								fsetdata[j] += fdata[j];
 						else
 							for( int j = 0; j < jMax; j++ )
 								fsetdata[j] -= fdata[j];
 						((FloatDataset)resultSet).setDirty();
 					}
 				}
 				if( add )
 					loadedImageFiles.add( imageFile );
 				else {
 					loadedImageFiles.remove( imageFile );
 					if( loadedImageFiles.size() == 0 )
 						resultSet = null;
 				}
 				return Status.OK_STATUS;
 			}
 
 			public IStatus runThis(IProgressMonitor monitor) {
 				/* Since running this and others aswell through imageLoaderManager,
 				 * the single access of loading data is guaranteed.
 				 */
 				IStatus result = Status.CANCEL_STATUS;
 				do {
 					TreeSet<File> adding = new TreeSet<File>( toLoadImageFilesJob );
 					adding.removeAll( loadedImageFiles );
 					TreeSet<File> removing = new TreeSet<File>( loadedImageFiles );
 					removing.removeAll( toLoadImageFilesJob );
 					if( adding.size() + removing.size() > toLoadImageFilesJob.size() ) {
 						adding = toLoadImageFilesJob;
 						removing.clear();
 						loadedImageFiles.clear();
 					}
 					for( File i : adding ) {
 						if( isAborting() )
 							break;
 						result = processImage(i, true);
 						if( result != Status.OK_STATUS )
 							break;
 					}
 					for( File i : removing ) {
 						if( isAborting() )
 							break;
 						result = processImage(i, false);
 						if( result != Status.OK_STATUS )
 							break;
 					}
 					if( isAborting() )
 						break;
 					AbstractDataset resultSetDivided = resultSet;
 					if( loadedImageFiles.size() > 1 ) {
 						resultSetDivided = resultSet.clone();
 /*						((FloatDataset)resultSetDivided).idivide( loadedImageFilesIndices.size() ); //"Official" solution, but very slow
 */						float[] fsetdata = ((FloatDataset)resultSetDivided).getData();
 						int divider = loadedImageFiles.size();
 						int jMax = fsetdata.length;
 						for( int j = 0; j < jMax; j++ )
 							fsetdata[ j ] /= divider;
 						((FloatDataset)resultSetDivided).setDirty();
 					}
 					
 					//set.setName(getEditorInput().getName());
 
 					if( isAborting() )
 						break;
 					createPlot(resultSetDivided, monitor);
 					if( loadedImageFiles.size() > 0 ) { //Checking for sure
 						Display.getDefault().syncExec(new Runnable(){  
 							public void run() {  
 								setPartName(loadedImageFiles.first().getName());
 							}  
 						});
 					}
 					result = Status.OK_STATUS;
 				} while( false );
 				if( isAborting() ) {
 					setAborted();
 					return Status.CANCEL_STATUS;
 				}
 //				if( resultSetOriginalDatatype != AbstractDataset.FLOAT32 ) {
 //					Display.getDefault().syncExec(new Runnable(){  
 //						public void run() {  
 //							imageFilesWindowWidthText.setEnabled(false);
 //						}  
 //					});
 //				} else {
 //					Display.getDefault().syncExec(new Runnable(){  
 //						public void run() {  
 //							imageFilesWindowWidthText.setEnabled(true);
 //						}  
 //					});
 //				}
 				return result;
 			}
 		};
 		job.setUser(false);
 		job.setPriority(Job.BUILD);
 		imageLoaderManager = ExecutableManager.setRequest(job);
 //Previous source using base Job... This block can be deleted when newer source works.
 //		final Job job = new Job("Read image data") {
 //
 //			@Override
 //			protected IStatus run(IProgressMonitor monitor) {
 //				final File[] thisImageFiles;
 //				final int[] thisLoadedImageFiles;
 //				synchronized (loadedImageFiles) {
 //					thisImageFiles = imageFiles;
 //					thisLoadedImageFiles = loadedImageFiles;
 //				}
 //				
 ////				final String filePath = EclipseUtils.getFilePath(getEditorInput());
 //////				List<AbstractDataset> sets = new ArrayList<AbstractDataset>();
 //				int setFound = 0;
 //				AbstractDataset resultSet = null;
 //				int iMax = thisLoadedImageFiles.length;
 //				for( int i = 0; i < iMax; i++ ) {
 //					final String filePath = thisImageFiles[ thisLoadedImageFiles[ i ] ].getAbsolutePath();
 //					AbstractDataset set = null;
 //					try {
 //						set = LoaderService.getDataset(filePath);
 //					} catch (Throwable e) {
 //						logger.error("Cannot load file "+filePath, e);
 //						return Status.CANCEL_STATUS;
 //					}
 //					if (set==null || set.getShape()==null) {
 //						logger.error("Cannot read file "+getEditorInput().getName());
 //						return Status.CANCEL_STATUS;
 //					}
 //					//Accepting set of float images, or 1 not float image
 //					if( set.getDtype() != AbstractDataset.FLOAT32 ) {
 //						if( setFound == 0 ) {
 //							resultSet = set;
 //							setFound++;
 //						}
 //						break;
 //					}
 //					if( setFound == 0 ) {
 //						resultSet = set;
 //					} else {
 //						if( setFound == 1 )
 //							resultSet = resultSet.clone();
 //						float[] fsetdata = ((FloatDataset)resultSet).getData();
 //						float[] fdata = ((FloatDataset)set).getData();
 //						int jMax = Math.min( fsetdata.length, fdata.length );
 //						for( int j = 0; j < jMax; j++ )
 //							fsetdata[j] += fdata[j];
 //					}
 //					setFound++;
 //				}
 //				if( setFound > 1 ) {
 //					float[] fsetdata = ((FloatDataset)resultSet).getData();
 //					for( int j = 0; j < setFound; j++ )
 //						fsetdata[ j ] /= setFound;
 //				}
 //				
 //				//set.setName(getEditorInput().getName());
 //				
 //				final List<AbstractDataset> axes = new ArrayList<AbstractDataset>(2);
 //				axes.add(SliceUtils.createAxisDataset((resultSet.getShape()[0])));
 //				axes.add(SliceUtils.createAxisDataset((resultSet.getShape()[1])));
 //				
 //				plottingSystem.createPlot(resultSet, axes, PlotType.IMAGE, monitor);
 //
 //				return Status.OK_STATUS;
 //			}
 //			
 //		};
 //		job.setUser(false);
 //		job.setPriority(Job.BUILD);
 //		job.schedule();
 	}
 	
 
 	/**
 	 * Override to provide extra content.
 	 * @param toolMan
 	 */
 	protected void createCustomToolbarActionsRight(final ToolBarManager toolMan) {
 
 		toolMan.add(new Separator(getClass().getName()+"Separator1"));
 
 		final Action tableColumns = new Action("Open editor preferences.", IAction.AS_PUSH_BUTTON) {
 			@Override
 			public void run() {
 				PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "org.edna.workbench.editors.preferencePage", null, null);
 				if (pref != null) pref.open();
 			}
 		};
 		tableColumns.setChecked(false);
 		tableColumns.setImageDescriptor(Activator.getImageDescriptor("icons/application_view_columns.png"));
 
 		toolMan.add(tableColumns);
 		
 	}
 
 	@Override
 	public void setFocus() {
 		
 	}
 
 	@Override
 	public void doSave(IProgressMonitor monitor) {
 
 	}
 
 	@Override
 	public void doSaveAs() {
 
 	}
 
 	@Override
 	public boolean isSaveAsAllowed() {
 		return false;
 	}
 
     @Override
     public void dispose() {
      	if (plottingSystem!=null) plottingSystem.dispose();
      	super.dispose();
     }
 
     public Object getAdapter(final Class clazz) {
 		
 		if (clazz == Page.class) {
 			return new HeaderTablePage(EclipseUtils.getFilePath(getEditorInput()));
 		}
 		
 		return super.getAdapter(clazz);
 	}
 
     public AbstractPlottingSystem getPlottingSystem() {
     	return this.plottingSystem;
     }
 
 	@Override
 	public void showEditorInput(IEditorInput editorInput) {
 		this.setInput(editorInput);		
 	}
 
 }
