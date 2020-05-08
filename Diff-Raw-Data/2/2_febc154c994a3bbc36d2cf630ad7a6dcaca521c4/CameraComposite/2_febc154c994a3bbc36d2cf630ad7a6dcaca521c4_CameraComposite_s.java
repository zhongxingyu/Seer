 /*-
  * Copyright © 2011 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package uk.ac.gda.video.views;
 
 import gda.epics.CAClient;
 import gda.epics.LazyPVFactory;
 import gda.epics.PV;
 import gda.images.camera.ImageListener;
 import gda.images.camera.MotionJpegOverHttpReceiverSwt;
 import gov.aps.jca.event.MonitorEvent;
 import gov.aps.jca.event.MonitorListener;
 
 import java.io.IOException;
 import java.text.DateFormat;
 import java.util.Date;
 
 import org.eclipse.draw2d.ColorConstants;
 import org.eclipse.draw2d.FigureCanvas;
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.SashForm;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.events.ShellEvent;
 import org.eclipse.swt.events.ShellListener;
 import org.eclipse.swt.graphics.ImageData;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.util.StringUtils;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.utils.SWTImageUtils;
 import uk.ac.gda.richbeans.ACTIVE_MODE;
 import uk.ac.gda.richbeans.components.scalebox.DemandBox;
 import uk.ac.gda.richbeans.components.scalebox.NumberBox;
 import uk.ac.gda.richbeans.event.ValueAdapter;
 import uk.ac.gda.richbeans.event.ValueEvent;
 import uk.ac.gda.ui.utils.SWTUtils;
 
 public class CameraComposite extends Composite {
 
 	static final Logger logger = LoggerFactory.getLogger(CameraComposite.class);
 	private static final int[] WEIGHTS_SHOW_EPICS = new int[] { 30, 70 };
 	private static final int[] WEIGHTS_NOSHOW_EPICS = new int[] { 1, 99 };
 	private uk.ac.gda.client.viewer.ImageViewer viewer;
 	private MotionJpegOverHttpReceiverSwt videoReceiver;
 	private VideoListener listener = new VideoListener();
 
 	String[] cams;
 
 	Combo cameraSelect;
 	Composite parent;
 
 	//which camera is selected
 	int selected = 0;
 
 	Button start;
 	Button stop;
 
 	private CAClient ca_client;
 
 	FigureCanvas canvas;
 
 	private DemandBox gainBox, periodBox, exposureBox;
 
 	//is the panel of Epics controls to be shown
 	private boolean showEpics = true;
 	
 	static final int labelWidth = 60;
 	ICameraConfig cameraConfig;
 	NewImageListener newImageListener;
 	CameraComposite(Composite parent, int style, ICameraConfig cameraConfig, @SuppressWarnings("unused") Display display,
 			NewImageListener newImageListener) {
 		super(parent, style);
 		this.newImageListener = newImageListener;
 		ca_client = new CAClient();
 		this.cameraConfig = cameraConfig;
 //		setBackground(getDisplay().getSystemColor(SWT.COLOR_BLUE));
 
 		setLayout(new GridLayout(1, false));
 
 		CameraParameters[] camParameters = cameraConfig.getCameras();
 		cams = new String[camParameters.length];
 
 		for (int i = 0; i < camParameters.length; i++) {
 			cams[i] = camParameters[i].getName();
 		}
 
 		Label lblCamera = new Label(this, SWT.NONE);
 		lblCamera.setText("Select Camera URL: ");
 		cameraSelect = new Combo(this, 0);
 		GridDataFactory.fillDefaults().applyTo(cameraSelect);
 		cameraSelect.addSelectionListener(new SelectionListener() {
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e) {
 			}
 
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				selected = cameraSelect.getSelectionIndex();
 				handleSelection(selected);
 			}
 
 		});
 
 		sashForm = new SashForm(this, SWT.NONE);
 		GridDataFactory.fillDefaults().grab(true, true).applyTo(sashForm);
 		sashForm.setOrientation(SWT.VERTICAL);
 
 		createEpicsComposite(sashForm);
 		createImageComposite(sashForm);
 
 		exposureBox.on();
 		periodBox.on();
 		gainBox.on();
 
 		viewer.showDefaultImage();
 
 		cameraSelect.setItems(cams);
 
 		// end of sash
 		handleShowEpics(true);
 
 		videoReceiver = new MotionJpegOverHttpReceiverSwt();
 		videoReceiver.addImageListener(listener);
 
 		pack();
 	}
 
 	private void createEpicsComposite(Composite sashForm2) {
 		Composite compEpics = new Composite(sashForm2, SWT.NONE);
 		RowLayout rowLayout = new RowLayout();
 		rowLayout.pack = false;
 		compEpics.setLayout(rowLayout);
 
 		exposureBox = new DemandBox(compEpics, SWT.NONE);
 		exposureBox.setUnit("s");
 		exposureBox.setLabel("Exposure");
 		exposureBox.setLabelWidth(labelWidth);
 		exposureBox.setDecimalPlaces(3);
 		exposureBox.setEnabled(false); //enabled when channel connection is made
 //		GridDataFactory.fillDefaults().applyTo(exposureBox);
 
 		exposureBox.addValueListener(new ValueAdapter("exposureBox listener") {
 			@Override
 			public void valueChangePerformed(ValueEvent e) {
 				exposureListener.setValue(e.getDoubleValue());
 			}
 		});
 
 		periodBox = new DemandBox(compEpics, SWT.NONE);
 		periodBox.setUnit("s");
 		periodBox.setLabel("Period");
 		periodBox.setLabelWidth(labelWidth);
 		periodBox.setDecimalPlaces(3);
 		periodBox.setEnabled(false); //enabled when channel connection is made
 //		GridDataFactory.fillDefaults().applyTo(periodBox);
 
 		periodBox.addValueListener(new ValueAdapter("periodBox listener") {
 			@Override
 			public void valueChangePerformed(ValueEvent e) {
 				periodListener.setValue(e.getDoubleValue());
 			}
 		});
 
 		gainBox = new DemandBox(compEpics, SWT.NONE);
 		gainBox.setUnit("");
 		gainBox.setLabel("Gain");
 		gainBox.setLabelWidth(labelWidth);
 		gainBox.setActiveMode(ACTIVE_MODE.SET_ENABLED_AND_ACTIVE);
 		gainBox.setActive(true);
 		gainBox.setDecimalPlaces(3);
 		gainBox.setEnabled(false); //enabled when channel connection is made
 //		GridDataFactory.fillDefaults().applyTo(gainBox);
 
 		gainBox.addValueListener(new ValueAdapter("gainBox listener") {
 			@Override
 			public void valueChangePerformed(ValueEvent e) {
 				gainListener.setValue(e.getDoubleValue());
 			}
 		});
 
 		// Filler
 //		GridDataFactory.swtDefaults().applyTo(new Label(compEpics, SWT.NONE));
 
 		Composite acqComp = new Composite(compEpics, SWT.NONE);
 		acqComp.setLayout(new GridLayout(2, false));
 
 		lblAcquire = new Label(acqComp, SWT.NONE);
 		lblAcquire.setText("Acquire");
 		GridDataFactory.swtDefaults().applyTo(lblAcquire);
 
 		Composite canvasComposite = new Composite(acqComp, SWT.NONE);
 		GridDataFactory.swtDefaults().applyTo(canvasComposite);
 		canvasComposite.setLayout(new GridLayout(3, false));
 
 		canvas = new FigureCanvas(canvasComposite, SWT.DOUBLE_BUFFERED);
 		canvas.setSize(27, 27);
 
 		GridData gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
 		gridData.widthHint = 27;
 		gridData.heightHint = 27;
 		canvas.setLayoutData(gridData);
 
 		start = new Button(canvasComposite, SWT.NONE);
 		start.setText("Start");
 		start.addSelectionListener(new SelectionListener() {
 
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				setAcquire(true);
 			}
 
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e) {
 			}
 
 		});
 
 		stop = new Button(canvasComposite, SWT.NONE);
 		stop.setText("Stop");
 		stop.addSelectionListener(new SelectionListener() {
 
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				setAcquire(false);
 			}
 
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e) {
 			}
 
 		});
 	}
 
 	private void createImageComposite(SashForm sashForm2) {
 		Composite imageComp = new Composite(sashForm2, SWT.NONE);
 		imageComp.setLayout(new GridLayout(1, true));
 
 		Composite btnComp = new Composite(imageComp, SWT.NONE);
 		GridDataFactory.fillDefaults().applyTo(btnComp);
 		btnComp.setLayout(new GridLayout(3, false));
 
 		Button zoomFit = new Button(btnComp, SWT.PUSH);
 		GridDataFactory.fillDefaults().applyTo(zoomFit);
 		zoomFit.setText("Auto-zoom");
 		zoomFit.addSelectionListener(new SelectionListener() {
 
 			@Override
 			public void widgetDefaultSelected(SelectionEvent arg0) {
 			}
 
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				zoomFit();
 			}
 		});
 
 		showImageOnly = new Button(btnComp, SWT.CHECK);
 		GridDataFactory.fillDefaults().applyTo(showImageOnly);
 		showImageOnly.setText("ShowImageOnly");
 		showImageOnly.addSelectionListener(new SelectionListener() {
 
 			@Override
 			public void widgetDefaultSelected(SelectionEvent arg0) {
 			}
 
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				handleShowEpics(!showImageOnly.getSelection());
 			}
 		});
 		
 		lastImageId = new Label(btnComp, SWT.NONE);
 		GridDataFactory.fillDefaults().applyTo(lastImageId);
 		lastImageId.setText("Time of last Image");
 		
 
 		viewer = new uk.ac.gda.client.viewer.ImageViewer(imageComp, SWT.DOUBLE_BUFFERED);
 	}
 
 	protected void handleShowEpics(boolean b) {
 		showEpics = b;
 		sashForm.setWeights(showEpics ? WEIGHTS_SHOW_EPICS : WEIGHTS_NOSHOW_EPICS);
 	}
 
 	void setAcquire(final boolean acquire) {
 		final String pv = getAcquirePV();
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				try {
 					ca_client.caput(pv, acquire ? 1 : 0);
 					getDisplay().asyncExec(new Runnable() {
 
 						@Override
 						public void run() {
 							canvas.setBackground(acquire ? ColorConstants.green : ColorConstants.gray);
 						}
 					});
 				} catch (Exception e) {
 					logger.error("Error writing to " + pv, e);
 				}
 			}
 		}).start();
 
 	}
 
 	boolean isSelectedCameraEpics() {
 		return getEpicsCameraParameters() != null;
 	}
 
 	EpicsCameraParameters getEpicsCameraParameters() {
 		CameraParameters cameraParameters = cameraConfig.getCameras()[selected];
 		return (cameraParameters instanceof EpicsCameraParameters) ? (EpicsCameraParameters) cameraParameters : null;
 	}
 
 	String getAcquirePV() {
 		return isSelectedCameraEpics() ? getEpicsCameraParameters().getAcquirePV() : "";
 	}
 
 	String getAcqPeriodPV() {
 		return isSelectedCameraEpics() ? getEpicsCameraParameters().getAcqPeriodPV() : "";
 	}
 
 	String getExposurePV() {
 		return isSelectedCameraEpics() ? getEpicsCameraParameters().getExposurePV() : "";
 	}
 
 	String getGainPV() {
 		return isSelectedCameraEpics() ? getEpicsCameraParameters().getGainPV() : "";
 	}
 
 	void select(int selected) {
 		cameraSelect.select(selected);
 		handleSelection(selected);
 	}
 
 	PVMonitorListener gainListener, periodListener, exposureListener;
 
 	private void closeDownVideo(boolean full){
 		videoReceiver.removeImageListener(listener);
 		videoReceiver.closeConnection();
 		listener.clear();
 		if( full){
 			videoReceiver = null;
 		}
 	}
 	protected void handleSelection(int selected2) {
 		selected = selected2;
 		closeDownVideo(false);
 		try {
 			new Thread( new Runnable(){
 
 				@Override
 				public void run() {
 					try{
 						String mjpegURL=null;
 						EpicsCameraParameters epicsCameraParameters = getEpicsCameraParameters();
 						if( epicsCameraParameters != null){
 							String urlPV = epicsCameraParameters.getUrlPV();
 							if( urlPV != null){
 								mjpegURL = CAClient.get(urlPV);
 								logger.info("mjpegULR = " + mjpegURL);
 							}
 						}
 						if( mjpegURL == null ){
 							mjpegURL = cameraConfig.getCameras()[selected].getMjpegURL();
 						}
 						videoReceiver.setUrl(mjpegURL);
 						videoReceiver.configure();
 						videoReceiver.addImageListener(listener);
 						videoReceiver.start();
 					} catch(Exception e){
 						logger.error("Error starting videoReceiver",e);
 					}
 				}
 				
 			}).start();
 
 			if (exposureListener != null) {
 				exposureListener.disConnect();
 				exposureListener = null;
 			}
 
 			if (gainListener != null) {
 				gainListener.disConnect();
 				gainListener = null;
 			}
 
 			if (periodListener != null) {
 				periodListener.disConnect();
 				periodListener = null;
 			}
 
 			epicsPVsGiven = isSelectedCameraEpics();
 			showImageOnly.setSelection(!epicsPVsGiven);
 			handleShowEpics(epicsPVsGiven);
 			showImageOnly.setVisible(epicsPVsGiven);
 			if (!epicsPVsGiven) {
 				return;
 			}
 
 			exposureListener = new PVMonitorListener(getExposurePV(), exposureBox, getDisplay());
 			gainListener = new PVMonitorListener(getGainPV(), gainBox, getDisplay());
 			periodListener = new PVMonitorListener(getAcqPeriodPV(), periodBox, getDisplay());
 
 			final String acquirePV = getAcquirePV();
 				new Thread(new Runnable() {
 
 					@Override
 					public void run() {
 						try {
 							final boolean running = Double.parseDouble(ca_client.caget(acquirePV)) == 1.0;
 							getDisplay().asyncExec(new Runnable() {
 
 								@Override
 								public void run() {
 									canvas.setBackground(running ? ColorConstants.green : ColorConstants.gray);
 
 								}
 							});
 						} catch (Exception e) {
 							logger.error("Error reading from pv " + acquirePV);
 						} finally {
 							getDisplay().asyncExec(new Runnable() {
 
 								@Override
 								public void run() {
 									handleShowEpics(true);
 								}
 							});
 
 						}
 					}
 				}).start();
 		} catch (Exception e) {
 			logger.error("Error handling selection ", e);
 		}
 
 	}
 
 
 	@Override
 	public boolean setFocus() {
 		return viewer != null ? viewer.setFocus() : false;
 	}
 
 	@Override
 	public void dispose() {
 		super.dispose();
 		closeDownVideo(true);
 		viewer.dispose();
 	}
 
 	void resetView() {
 		if (viewer != null)
 			viewer.resetView();
 	}
 
 	void zoomFit() {
 		if (viewer != null)
 			viewer.zoomFit();
 	}
 	IDataset getDataset(){
 		return lastImage != null ? new SWTImageDataConverter(lastImage).toIDataset() : null;
 	}
 
 	private Label lblAcquire;
 	private SashForm sashForm;
 	private Button showImageOnly;
 	private boolean epicsPVsGiven;
 	ImageData lastImage;
 	private Label lastImageId;
 	DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG);
 	private final class VideoListener implements ImageListener<ImageData> {
 		private String name;
 
 		@Override
 		public void setName(String name) {
 			this.name = name;
 		}
 
 		public void clear() {
 			viewer.showDefaultImage();
 		}
 
 		@Override
 		public String getName() {
 			return name;
 		}
 
 		boolean processingImage=false;
 		@Override
 		public void processImage(final ImageData image) {
 			if (image == null)
 				return;
 			lastImage=image;
 			if (viewer != null) {
 				if(isDisposed())
 					return;
 				if(processingImage)
 					return;
 				processingImage=true;
 				try {
 					Thread.sleep(100);
 				} catch (InterruptedException e) {
 					logger.error("Error sleeping for 100ms", e);
 				}
 				getDisplay().asyncExec(new Runnable() {
 					@Override
 					public void run() {
 						boolean showingDefault = viewer.isShowingDefault();
 						while(true){
 							//ensure we don't miss an image that arrives while we process the first
 							ImageData lastImage2 = lastImage;
 							viewer.loadImage(lastImage2);
 							if (showingDefault) {
 								zoomFit();
 							}
 							lastImageId.setText(df.format(new Date()));
 							if( newImageListener != null)
 								newImageListener.handlerNewImageNotification();
 							if( lastImage2 == lastImage){
 								processingImage=false;
 								break;
 							}
 							processingImage=false;
 							break; //TODO we may remain in UI thread if frame rate is very high
 						}
 					}
 				});
 			}
 		}
 	}
 
 	public static void main(String... args) throws Exception {
 
 		Display display = new Display();
 		Shell shell = new Shell(display);
 		shell.setLayout(new FillLayout());
 
 		CameraConfig cc = new CameraConfig();
 		/*
 		 * CameraParameters cp = new CameraParameters(); cp.setAcqPeriodPV("t"); cp.setAcquirePV("t");
 		 * cp.setExposurePV("t"); cp.setGainPV("t"); cp.setMjpegURL("test"); cp.setName("1"); cp.afterPropertiesSet();
 		 * CameraParameters cp1 = new CameraParameters(); cp1.setMjpegURL("test"); cp1.setName("2");
 		 * cp1.afterPropertiesSet();
 		 */
 		EpicsCameraParameters cp = new EpicsCameraParameters();
 		cp.setAcqPeriodPV("BL13J-DI-PHDGN-01:CAM:AcquirePeriod");
 		cp.setAcquirePV("BL13J-DI-PHDGN-01:CAM:Acquire");
 		cp.setExposurePV("BL13J-DI-PHDGN-01:CAM:AcquireTim");
 		cp.setGainPV("BL13J-DI-PHDGN-01:CAM:Gain");
 		cp.setUrlPV("BL13J-DI-PHDGN-01:FFMPEG:JPG_URL_RBV");
 		cp.setName("D1 - Post Mirror  1");
 		cp.afterPropertiesSet();
 
 		EpicsCameraParameters cp2 = new EpicsCameraParameters();
 		cp2.setAcqPeriodPV("BL13J-DI-PHDGN-02:CAM:AcquirePeriod");
 		cp2.setAcquirePV("BL13J-DI-PHDGN-02:CAM:Acquire");
 		cp2.setExposurePV("BL13J-DI-PHDGN-02:CAM:AcquireTim");
 		cp2.setGainPV("BL13J-DI-PHDGN-02:CAM:Gain");
 		cp2.setUrlPV("BL13J-DI-PHDGN-02:FFMPEG:JPG_URL_RBV");
 		cp2.setName("D2 - Post Absorber 1");
 		cp2.afterPropertiesSet();
 
 		CameraParameters cp4 = new CameraParameters();
 		cp4.setMjpegURL("http://172.23.104.179/mjpg/video.mjpg");
 		cp4.setName("Webcam");
 		cp4.afterPropertiesSet();
 
 		CameraParameters[] cameraList = new CameraParameters[] { cp, cp2, cp4 };
 		cc.setCameras(cameraList);
 		final CameraComposite comp = new CameraComposite(shell, SWT.NONE, cc, display, null);
 		comp.select(0);
 		comp.setVisible(true);
 		
 		Button exitBtn = new Button(shell, SWT.PUSH);
 		exitBtn.setText("Exit");
 		exitBtn.addSelectionListener(new SelectionListener(){
 
 			@Override
 			public void widgetDefaultSelected(SelectionEvent arg0) {
 			}
 
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				comp.dispose();
 			}});
 		
 		
 		shell.pack();
 		shell.setSize(400, 400);
 		shell.addShellListener(new ShellListener(){
 
 			@Override
 			public void shellActivated(ShellEvent arg0) {
 			}
 
 			@Override
 			public void shellClosed(ShellEvent arg0) {
 				comp.dispose();
 			}
 
 			@Override
 			public void shellDeactivated(ShellEvent arg0) {
 			}
 
 			@Override
 			public void shellDeiconified(ShellEvent arg0) {
 			}
 
 			@Override
 			public void shellIconified(ShellEvent arg0) {
 			}});
 		SWTUtils.showCenteredShell(shell);
 	}
 }
 
 class PVMonitorListener {
 	MonitorListener listener;
 	PV<Double> pv;
 	final String pvName;
 	final NumberBox numberBox;
 	private final Display display;
 
 	public PVMonitorListener(String pvName, NumberBox numberBox, Display display) {
 		super();
 		this.pvName = pvName;
 		this.numberBox = numberBox;
 		this.display = display;
 		if (StringUtils.hasLength(pvName)) {
 			numberBox.setEnabled(true);
 			pv = LazyPVFactory.newDoublePV(pvName);
 			listener = new MonitorListener() {
 				@Override
 				public void monitorChanged(MonitorEvent arg0) {
 					final Double double1 = pv.extractValueFromDbr(arg0.getDBR());
 					PVMonitorListener.this.display.asyncExec(new Runnable() {
 
 						@Override
 						public void run() {
 							PVMonitorListener.this.numberBox.setValue(double1);
 
 						}
 					});
 
 				}
 			};
 			(new Thread(new Runnable() {
 
 				@Override
 				public void run() {
 					boolean connectedNonFinal=false;
 					try {
 						pv.addMonitorListener(listener);
 						connectedNonFinal = true;
 					} catch (IOException e) {
 						CameraComposite.logger.error("Error adding listener to pv " + pv, e);
 					}
 					final boolean connectedFinal = connectedNonFinal;
 					Runnable runnable = new Runnable(){
 						@Override
 						public void run() {
 							PVMonitorListener.this.numberBox.setEnabled(connectedFinal);
 						}
 						
 					};
 					PVMonitorListener.this.display.asyncExec(runnable);
 				}
 			})).start();
 
 		} else {
 			listener = null;
 			pv = null;
 			numberBox.setEnabled(false);
 		}
 	}
 
 	public void setValue(final double destPosition) {
 		if (pv != null) {
 			display.asyncExec(new Runnable() {
 
 				@Override
 				public void run() {
 					try {
						pv.put(destPosition);
 					} catch (IOException e) {
 						CameraComposite.logger.error("Error sending value " + destPosition + " to " + pvName, e);
 					}
 				}
 
 			});
 		}
 
 	}
 
 	void disConnect() {
 		if (pv != null) {
 			pv.removeMonitorListener(listener);
 			listener = null;
 		}
 		pv = null;
 	}
 }
 
 class SWTImageDataConverter {
 	ImageData imageData;
 	IDataset idataset;
 
 	public SWTImageDataConverter(ImageData imageData) {
 		this.imageData = imageData;
 	}
 
 	public IDataset toIDataset() {
 		if (imageData == null) {
 			return null;
 		}
 		if( idataset == null){
 			idataset = SWTImageUtils.createRGBDataset(imageData).createGreyDataset(AbstractDataset.FLOAT32);
 		}
 		return idataset;
 
 	}
 
 }
