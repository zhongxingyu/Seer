 /*-
  * Copyright 2013 Diamond Light Source Ltd.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.dawb.workbench.ui.views;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.vecmath.Vector3d;
 
 import org.dawb.common.services.ILoaderService;
 import org.dawb.common.ui.monitor.ProgressMonitorWrapper;
 import org.dawb.common.ui.parts.PartUtils;
 import org.dawb.common.ui.util.EclipseUtils;
 import org.dawb.workbench.ui.Activator;
 import org.dawnsci.plotting.api.IPlottingSystem;
 import org.dawnsci.plotting.api.region.IRegion;
 import org.dawnsci.plotting.api.region.IRegion.RegionType;
 import org.dawnsci.plotting.api.region.RegionUtils;
 import org.dawnsci.plotting.api.trace.IImageTrace;
 import org.dawnsci.plotting.api.trace.ITrace;
 import org.dawnsci.plotting.api.trace.ITraceListener;
 import org.dawnsci.plotting.api.trace.TraceEvent;
 import org.dawnsci.plotting.tools.diffraction.DiffractionImageAugmenter;
 import org.dawnsci.plotting.tools.diffraction.DiffractionTool;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.draw2d.ColorConstants;
 import org.eclipse.jface.viewers.CellEditor;
 import org.eclipse.jface.viewers.CheckboxCellEditor;
 import org.eclipse.jface.viewers.EditingSupport;
 import org.eclipse.jface.viewers.ILabelProviderListener;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.events.MouseAdapter;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IPartListener2;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.IWorkbenchPartReference;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.ViewPart;
 
 import uk.ac.diamond.scisoft.analysis.crystallography.CalibrationFactory;
 import uk.ac.diamond.scisoft.analysis.crystallography.CalibrationStandards;
 import uk.ac.diamond.scisoft.analysis.crystallography.HKL;
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.diffraction.DetectorProperties;
 import uk.ac.diamond.scisoft.analysis.diffraction.DiffractionCrystalEnvironment;
 import uk.ac.diamond.scisoft.analysis.diffraction.PowderRingsUtils;
 import uk.ac.diamond.scisoft.analysis.diffraction.QSpace;
 import uk.ac.diamond.scisoft.analysis.fitting.Fitter;
 import uk.ac.diamond.scisoft.analysis.fitting.functions.Polynomial;
 import uk.ac.diamond.scisoft.analysis.io.AbstractFileLoader;
 import uk.ac.diamond.scisoft.analysis.io.IDiffractionMetadata;
 import uk.ac.diamond.scisoft.analysis.roi.EllipticalROI;
 import uk.ac.diamond.scisoft.analysis.roi.IROI;
 
 /**
  * This listens for a selected editor (of a diffraction image) and allows
  * 
  * 1) selection of calibrant
  * 2) movement, scaling and tilting of rings
  * 3) refinement of fit
  * 4) calibration (other images too?)
  *
  * Should display relevant metadata, allow a number of files to contribute to final calibration
  */
 public class DiffractionCalibrationView extends ViewPart {
 
 	private ScrolledComposite sComp;
 	private IPlottingSystem currentSystem;
 	private MyData currentData;
 	private List<MyData> model = new ArrayList<MyData>();
 	private Map<IPlottingSystem, Listener> listeners = new HashMap<IPlottingSystem, Listener>();
 	private ILoaderService service;
 	private TableViewer tableViewer;
 	private IPartListener2 listener;
 	private Button calibrateImages;
 	private Button calibrateWD;
 
 	enum ManipulateMode {
 		LEFT, RIGHT, UP, DOWN, ENLARGE, SHRINK, ELONGATE, SQUASH, CLOCKWISE, ANTICLOCKWISE
 	}
 
 	public DiffractionCalibrationView() {
 		service = (ILoaderService)PlatformUI.getWorkbench().getService(ILoaderService.class);
 	}
 
 	@Override
 	public void createPartControl(Composite parent) {
 		parent.setLayout(new FillLayout());
 
 		// add part listener for relevant editors and views
 		listener = new IPartListener2() {
 			@Override
 			public void partVisible(IWorkbenchPartReference partRef) {
 				IWorkbenchPart part = partRef.getPart(false);
 				System.err.println("part visible: " + part.getTitle());
 				IPlottingSystem system = PartUtils.getPlottingSystem(part);
 				if (system != null) {
 					String altPath = part instanceof IEditorPart ? EclipseUtils.getFilePath(((IEditorPart) part).getEditorInput()) : null;
 					setData(altPath, system);
 				}
 			}
 			
 			@Override
 			public void partOpened(IWorkbenchPartReference partRef) {
 				IWorkbenchPart part = partRef.getPart(false);
 				System.err.println("part opened: " + part.getTitle());
 			}
 			
 			@Override
 			public void partInputChanged(IWorkbenchPartReference partRef) {
 				IWorkbenchPart part = partRef.getPart(false);
 				System.err.println("input changed: " + part.getTitle());
 			}
 			
 			@Override
 			public void partHidden(IWorkbenchPartReference partRef) {
 			}
 			
 			@Override
 			public void partDeactivated(IWorkbenchPartReference partRef) {
 			}
 			
 			@Override
 			public void partClosed(IWorkbenchPartReference partRef) {
 				IWorkbenchPart part = partRef.getPart(false);
 				IPlottingSystem system = PartUtils.getPlottingSystem(part);
 				if (system != null) {
 					removePlottingSystem(system);
 				}
 			}
 			
 			@Override
 			public void partBroughtToTop(IWorkbenchPartReference partRef) {
 			}
 			
 			@Override
 			public void partActivated(IWorkbenchPartReference partRef) {
 				IWorkbenchPart part = partRef.getPart(false);
 				System.err.println("part activated: " + part.getTitle());
 			}
 		};
 		getSite().getPage().addPartListener(listener);
 
 		// make entire view a scrolled composite
 		sComp = new ScrolledComposite(parent, SWT.V_SCROLL);
 
 		Composite sHolder = new Composite(sComp, SWT.NONE);
 		RowLayout rl = new RowLayout(SWT.VERTICAL);
 		rl.fill = true;
 		sHolder.setLayout(rl);
 
 		Composite gHolder = new Composite(sHolder, SWT.NONE);
 		gHolder.setLayout(new GridLayout(4, false));
 
 		// create calibrant combo
 		Label l = new Label(gHolder, SWT.NONE);
 		l.setText("Calibrant");
 		l.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
 		final Combo calibrant = new Combo(gHolder, SWT.READ_ONLY);
 		final CalibrationStandards standards = CalibrationFactory.getCalibrationStandards();
 		calibrant.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				standards.setSelectedCalibrant(calibrant.getItem(calibrant.getSelectionIndex()));
 				drawCalibrantRings();
 			}
 		});
 		for (String c : standards.getCalibrantList()) {
 			calibrant.add(c);
 		}
 		String s = standards.getSelectedCalibrant();
 		if (s != null) {
 			calibrant.setText(s);
 		}
 //		calibrant.setText("Please select a calibrant..."); // won't work with read-only
 		calibrant.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
 
 		// create motion buttons cluster
 		l = new Label(gHolder, SWT.NONE);
 		l = new Label(gHolder, SWT.NONE);
 		Button b = new Button(gHolder, SWT.ARROW | SWT.UP);
 		b.setToolTipText("Move rings up");
 		b.addMouseListener(new RepeatingMouseAdapter(parent.getDisplay(), new SlowFastRunnable() {
 			@Override
 			public void run() {
 				changeRings(ManipulateMode.UP, isFast());
 			}
 		}));
 		b.setLayoutData(new GridData(SWT.CENTER, SWT.BOTTOM, false, false));
 		l = new Label(gHolder, SWT.NONE);
 
 		l = new Label(gHolder, SWT.NONE);
 		b = new Button(gHolder, SWT.ARROW | SWT.LEFT);
 		b.setToolTipText("Shift rings left");
 		b.addMouseListener(new RepeatingMouseAdapter(parent.getDisplay(), new SlowFastRunnable() {
 			@Override
 			public void run() {
 				changeRings(ManipulateMode.LEFT, isFast());
 			}
 		}));
 		b.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
 		l = new Label(gHolder, SWT.NONE);
 		b = new Button(gHolder, SWT.ARROW | SWT.RIGHT);
 		b.setToolTipText("Shift rings right");
 		b.addMouseListener(new RepeatingMouseAdapter(parent.getDisplay(), new SlowFastRunnable() {
 			@Override
 			public void run() {
 				changeRings(ManipulateMode.RIGHT, isFast());
 			}
 		}));
 		b.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
 
 		l = new Label(gHolder, SWT.NONE);
 		l = new Label(gHolder, SWT.NONE);
 		b = new Button(gHolder, SWT.ARROW | SWT.DOWN);
 		b.setToolTipText("Move rings down");
 		b.addMouseListener(new RepeatingMouseAdapter(parent.getDisplay(), new SlowFastRunnable() {
 			@Override
 			public void run() {
 				changeRings(ManipulateMode.DOWN, isFast());
 			}
 		}));
 		b.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false));
 		l = new Label(gHolder, SWT.NONE);
 
 		l = new Label(gHolder, SWT.NONE);
 		l = new Label(gHolder, SWT.NONE);
 		l = new Label(gHolder, SWT.NONE);
 		l = new Label(gHolder, SWT.NONE);
 
 		l = new Label(gHolder, SWT.NONE);
 		b = new Button(gHolder, SWT.PUSH);
 		b.setText("-");
 		b.setToolTipText("Make rings smaller");
 		b.addMouseListener(new RepeatingMouseAdapter(parent.getDisplay(), new SlowFastRunnable() {
 			@Override
 			public void run() {
 				changeRings(ManipulateMode.SHRINK, isFast());
 			}
 
 			@Override
 			public void stop() {
 				refreshTable();
 			}
 		}));
 		b.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
 		l = new Label(gHolder, SWT.NONE);
 		b = new Button(gHolder, SWT.PUSH);
 		b.setText("+");
 		b.setToolTipText("Make rings larger");
 		b.addMouseListener(new RepeatingMouseAdapter(parent.getDisplay(), new SlowFastRunnable() {
 			@Override
 			public void run() {
 				changeRings(ManipulateMode.ENLARGE, isFast());
 			}
 
 			@Override
 			public void stop() {
 				refreshTable();
 			}
 		}));
 		b.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
 
 		l = new Label(gHolder, SWT.NONE);
 		b = new Button(gHolder, SWT.PUSH);
 		b.setText("Squash");
 		b.setToolTipText("Make rings more circular");
 		b.addMouseListener(new RepeatingMouseAdapter(parent.getDisplay(), new SlowFastRunnable() {
 			@Override
 			public void run() {
 				changeRings(ManipulateMode.SQUASH, isFast());
 			}
 
 			@Override
 			public void stop() {
 				refreshTable();
 			}
 		}));
 		b.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
 		l = new Label(gHolder, SWT.NONE);
 		b = new Button(gHolder, SWT.PUSH);
 		b.setText("Elongate");
 		b.setToolTipText("Make rings more elliptical");
 		b.addMouseListener(new RepeatingMouseAdapter(parent.getDisplay(), new SlowFastRunnable() {
 			@Override
 			public void run() {
 				changeRings(ManipulateMode.ELONGATE, isFast());
 			}
 
 			@Override
 			public void stop() {
 				refreshTable();
 			}
 		}));
 		b.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
 
 		l = new Label(gHolder, SWT.NONE);
 		b = new Button(gHolder, SWT.PUSH);
 		b.setImage(Activator.getImage("icons/arrow_rotate_anticlockwise.png"));
 		b.setToolTipText("Rotate rings anti-clockwise");
 		b.addMouseListener(new RepeatingMouseAdapter(parent.getDisplay(), new SlowFastRunnable() {
 			@Override
 			public void run() {
 				changeRings(ManipulateMode.ANTICLOCKWISE, isFast());
 			}
 
 			@Override
 			public void stop() {
 				refreshTable();
 			}
 		}));
 		b.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
 		l = new Label(gHolder, SWT.NONE);
 		b = new Button(gHolder, SWT.PUSH);
 		b.setImage(Activator.getImage("icons/arrow_rotate_clockwise.png"));
 		b.setToolTipText("Rotate rings clockwise");
 		b.addMouseListener(new RepeatingMouseAdapter(parent.getDisplay(), new SlowFastRunnable() {
 			@Override
 			public void run() {
 				changeRings(ManipulateMode.CLOCKWISE, isFast());
 			}
 
 			@Override
 			public void stop() {
 				refreshTable();
 			}
 		}));
 		b.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
 
 		l = new Label(gHolder, SWT.NONE);
 		l = new Label(gHolder, SWT.NONE);
 		l = new Label(gHolder, SWT.NONE);
 		l = new Label(gHolder, SWT.NONE);
 
 		l = new Label(gHolder, SWT.NONE);
 		b = new Button(gHolder, SWT.PUSH);
 		b.setText("Find rings in image");
 		b.setToolTipText("Use pixel values to find rings in image near calibration rings");
 		b.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
 		b.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				findRings();
 			}
 		});
 
 		l = new Label(gHolder, SWT.NONE);
 		l = new Label(gHolder, SWT.NONE);
 		l = new Label(gHolder, SWT.NONE);
 		l = new Label(gHolder, SWT.NONE);
 
 //		gHolder.setSize(gHolder.computeSize(SWT.DEFAULT, SWT.DEFAULT));
 //		gHolder.layout();
 
 		// table of images and found rings
 		tableViewer = new TableViewer(sHolder, SWT.NONE);
 //		tableViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, true, 5, 5));
 		createColumns(tableViewer);
 		tableViewer.getTable().setHeaderVisible(true);
 		tableViewer.setContentProvider(new MyContentProvider());
 		tableViewer.setLabelProvider(new MyLabelProvider());
 		tableViewer.setInput(model);
 		tableViewer.refresh();
 
 		calibrateImages = new Button(sHolder, SWT.PUSH);
 		calibrateImages.setText("Calibrate chosen images");
 		calibrateImages.setToolTipText("Calibrate detector in chosen images");
 		calibrateImages.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				calibrateImages();
 			}
 		});
 		calibrateImages.setEnabled(false);
 
 		calibrateWD = new Button(sHolder, SWT.PUSH);
 		calibrateWD.setText("Calibrate wavelength");
 		calibrateWD.setToolTipText("Calibrate wavelength from images chosen in table");
 		calibrateWD.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				calibrateWavelength();
 			}
 		});
 		calibrateWD.setEnabled(false);
 
 		//		sHolder.setSize(sHolder.computeSize(SWT.DEFAULT, SWT.DEFAULT));
 		sHolder.layout();
 		sComp.setContent(sHolder);
 		sComp.setExpandHorizontal(true);
 		sComp.setExpandVertical(true);
 		sComp.setMinSize(sHolder.computeSize(SWT.DEFAULT, SWT.DEFAULT));
 		sComp.layout();
 	}
 
 	protected void findRings() {
 		if (currentData == null)
 			return;
 
 		DiffractionImageAugmenter aug = currentData.augmenter;
 		if (aug == null)
 			return;
 
 		final List<IROI> resROIs = aug.getResolutionROIs();
 		final IImageTrace image = getImageTrace(currentSystem);
 		final Display display = sComp.getDisplay();
 		if (currentData.rois == null) {
 			currentData.rois = new ArrayList<IROI>();
 		} else {
 			currentData.rois.clear();
 		}
 		clearFoundRings();
 		Job job = new Job("Ellipse rings finding") {
 			@Override
 			protected IStatus run(final IProgressMonitor monitor) {
 				IStatus stat = Status.OK_STATUS;
 				double last = -1;
 				int n = 0;
 				for (final IROI r : resROIs) {
 					try {
 						if (!(r instanceof EllipticalROI)) // cannot cope with other conic sections for now
 							continue;
 						EllipticalROI e = (EllipticalROI) r;
 						double major = e.getSemiAxis(0);
 						double delta = last < 0 ? 0.1*major : 0.2*(major - last);
 						if (delta > 50)
 							delta = 50;
 						last = major;
 						IROI roi = DiffractionTool.runEllipseFit(monitor, display, currentSystem, image, e, e.isCircular(), delta);
 						if (roi == null)
 							return Status.CANCEL_STATUS;
 
 						double[] ec = e.getPointRef();
 						double[] c = roi.getPointRef();
 						if (Math.hypot(c[0] - ec[0], c[1] - ec[1]) > delta) {
 							System.err.println("Dropping as too far from centre: " + roi + " cf " + e);
 							currentData.rois.add(null); // null placeholder
 							continue;
 						}
 						currentData.rois.add(roi);
 						n++;
 
 						stat = drawFoundRing(monitor, display, currentSystem, roi, e.isCircular());
 						if (!stat.isOK())
 							break;
 					} catch (IllegalArgumentException ex) {
 						currentData.rois.add(null); // null placeholder
 						System.err.println("Could not find " + r + ": " + ex);
 					}
 				}
 				currentData.nrois = n;
 				display.asyncExec(new Runnable() {
 					@Override
 					public void run() {
 						if (currentData.nrois > 0) {
 							currentData.use = true;
 							setCalibrateButtons();
 						}
 						refreshTable();
 					}
 				});
 				return stat;
 			}
 		};
 		job.setPriority(Job.SHORT);
 //		 job.setUser(true);
 		job.schedule();
 	}
 
 	protected void calibrateImages() {
 		final Display display = sComp.getDisplay();
 		Job job = new Job("Calibrate detector") {
 			@Override
 			protected IStatus run(final IProgressMonitor monitor) {
 				IStatus stat = Status.OK_STATUS;
 				final ProgressMonitorWrapper mon = new ProgressMonitorWrapper(monitor);
 				monitor.beginTask("Calibrate detector", IProgressMonitor.UNKNOWN);
 				List<HKL> spacings = CalibrationFactory.getCalibrationStandards().getCalibrant().getHKLs();
 				for (MyData data : model) {
 					IDiffractionMetadata md = data.md;
 					if (!data.use || data.nrois <= 0 || md == null) {
 						continue;
 					}
 					monitor.subTask("Fitting rings in " + data.name);
 					data.q = null;
 
 					DetectorProperties dp = md.getDetector2DProperties();
 					DiffractionCrystalEnvironment ce = md.getDiffractionCrystalEnvironment();
 					if (dp == null || ce == null) {
 						continue;
 					}
 					try {
 						data.q = PowderRingsUtils.fitAllEllipsesToQSpace(mon, dp, ce, data.rois, spacings, true);
 
 						System.err.println(data.q);
 						data.od = dp.getDetectorDistance(); // store old values
 						data.ow = ce.getWavelength();
 
 					} catch (IllegalArgumentException e) {
 						System.err.println(e);
 					}
 				}
 
 				display.asyncExec(new Runnable() {
 					@Override
 					public void run() {
 						for (MyData data : model) {
 							IDiffractionMetadata md = data.md;
 							if (!data.use || data.nrois <= 0 || md == null) {
 								continue;
 							}
 							DetectorProperties dp = md.getDetector2DProperties();
 							DiffractionCrystalEnvironment ce = md.getDiffractionCrystalEnvironment();
 							if (dp == null || ce == null || data.q == null) {
 								continue;
 							}
 
 							DetectorProperties fp = data.q.getDetectorProperties();
 							double[] angs = fp.getNormalAnglesInDegrees();
 							dp.setNormalAnglesInDegrees(angs);
 							dp.setOrigin(fp.getOrigin());
 							ce.setWavelength(data.q.getWavelength());
 						}
 
 						if (currentData == null || currentData.md == null || currentData.q == null)
 							return;
 
 						refreshTable();
 						hideFoundRings();
 						drawCalibrantRings();
 						setCalibrateButtons();
 					}
 				});
 				return stat;
 			}
 		};
 		job.setPriority(Job.SHORT);
 //		 job.setUser(true);
 		job.schedule();
 	}
 
 	protected void calibrateWavelength() {
 		final Display display = sComp.getDisplay();
 		Job job = new Job("Calibrate wavelength") {
 			@Override
 			protected IStatus run(final IProgressMonitor monitor) {
 				IStatus stat = Status.OK_STATUS;
 				monitor.beginTask("Calibrate wavelength", IProgressMonitor.UNKNOWN);
 				List<Double> odist = new ArrayList<Double>();
 				List<Double> ndist = new ArrayList<Double>();
 //				double[] centre = null;
 				for (MyData data : model) {
 					if (!data.use || data.nrois <= 0 || data.md == null) {
 						continue;
 					}
 					
 					if (data.q == null || Double.isNaN(data.od)) {
 						continue;
 					}
 
 //					if (centre == null) {
 //						
 //					}
 					odist.add(data.od);
 					ndist.add(data.q.getDetectorProperties().getDetectorDistance());
 				}
 				Polynomial p = new Polynomial(1);
 				Fitter.llsqFit(new AbstractDataset[] {AbstractDataset.createFromList(odist)}, AbstractDataset.createFromList(ndist), p);
 				System.err.println(p);
 
 				double f = p.getParameterValue(0);
 				for (MyData data : model) {
 					if (!data.use || data.nrois <= 0 || data.md == null) {
 						continue;
 					}
 
 					DiffractionCrystalEnvironment ce = data.md.getDiffractionCrystalEnvironment();
 					if (ce != null) {
 						data.ow = ce.getWavelength();
 						ce.setWavelength(data.ow/f);
 					}
 				}
 
 				display.asyncExec(new Runnable() {
 					@Override
 					public void run() {
 						refreshTable();
 						drawCalibrantRings();
 					}
 				});
 				return stat;
 			}
 		};
 		job.setPriority(Job.SHORT);
 		job.schedule();
 	}
 
 	protected void drawCalibrantRings() {
 		if (currentData == null)
 			return;
 
 		DiffractionImageAugmenter aug = currentData.augmenter;
 		if (aug == null)
 			return;
 
 		CalibrationStandards standards = CalibrationFactory.getCalibrationStandards();
 		aug.drawCalibrantRings(true, standards.getCalibrant());
 		aug.drawBeamCentre(true);
 	}
 
 	private static String REGION_PREFIX = "Pixel peaks";
 
 	private void clearFoundRings() {
 		for (IRegion r : currentSystem.getRegions()) {
 			String n = r.getName();
 			if (n.startsWith(REGION_PREFIX)) {
 				currentSystem.removeRegion(r);
 			}
 		}
 	}
 
 	private void hideFoundRings() {
 		for (IRegion r : currentSystem.getRegions()) {
 			String n = r.getName();
 			if (n.startsWith(REGION_PREFIX)) {
 				r.setVisible(false);
 			}
 		}
 	}
 
 	private IStatus drawFoundRing(final IProgressMonitor monitor, Display display, final IPlottingSystem plotter, final IROI froi, final boolean circle) {
 		final boolean[] status = {true};
 		display.syncExec(new Runnable() {
 
 			public void run() {
 				try {
 					IRegion region = plotter.createRegion(RegionUtils.getUniqueName(REGION_PREFIX, plotter), circle ? RegionType.CIRCLEFIT : RegionType.ELLIPSEFIT);
 					region.setROI(froi);
 					region.setRegionColor(circle ? ColorConstants.cyan : ColorConstants.orange);
 					monitor.subTask("Add region");
 					region.setUserRegion(false);
 					plotter.addRegion(region);
 					monitor.worked(1);
 				} catch (Exception e) {
 					status[0] = false;
 				}
 			}
 		});
 
 		return status[0] ? Status.OK_STATUS : Status.CANCEL_STATUS;
 	}
 
 	private IImageTrace getImageTrace(IPlottingSystem system) {
 		Collection<ITrace> traces = system.getTraces();
 		if (traces != null && traces.size() > 0) {
 			ITrace trace = traces.iterator().next();
 			if (trace instanceof IImageTrace) {
 				return (IImageTrace) trace;
 			}
 		}
 		return null;
 	}
 
 	class Listener extends ITraceListener.Stub {
 		private MyData data;
 		
 		public Listener(MyData mydata) {
 			setData(mydata);
 		}
 
 		public void setData(MyData mydata) {
 			data = mydata;
 		}
 
 		@Override
 		public void traceRemoved(TraceEvent evt) {
 			data.path = null;
 		}
 
 		@Override
 		protected void update(TraceEvent evt) {
 			if (data == null || data.system == null)
 				return;
 
 			IImageTrace image = getImageTrace(data.system);
 			if (image == null)
 				return;
 
 			System.err.println("We have an image, Houston!");
 
 			DiffractionImageAugmenter aug = data.augmenter;
 			if (aug == null) {
 				aug = new DiffractionImageAugmenter(data.system);
 				data.augmenter = aug;
 			}
 			aug.activate();
 			if (data.path == null) {
 				String path = getPathFromTrace(image);
 				data.path = path;
 				data.name = path.substring(path.lastIndexOf(File.separatorChar) + 1);
 				data.md = DiffractionTool.getDiffractionMetadata(image.getData(), data.path, service, null);
 			}
 			if (data.md == null)
 				data.md = DiffractionTool.getDiffractionMetadata(image.getData(), data.path, service, null);
 
 			aug.setDiffractionMetadata(data.md);
 			refreshTable();
 			drawCalibrantRings();
 		}
 	}
 
 	class MyData {
 		IPlottingSystem system;
 		String path;
 		String name;
 		DiffractionImageAugmenter augmenter;
 		IDiffractionMetadata md;
 		Listener listener;
 		List<IROI> rois;
 		QSpace q;
 		double ow = Double.NaN;
 		double od = Double.NaN;
 		int nrois = -1;
 		boolean use = false;
 	}
 
 	class MyContentProvider implements IStructuredContentProvider {
 		@Override
 		public void dispose() {
 		}
 
 		@Override
 		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 		}
 
 		@Override
 		public Object[] getElements(Object inputElement) {
 			if (inputElement == null) {
 				return null;
 			}
 			return ((List<?>) inputElement).toArray();
 		}
 	}
 
 	private static final Image TICK = Activator.getImageDescriptor("icons/tick.png").createImage();
 
 	class MyLabelProvider implements ITableLabelProvider {
 		@Override
 		public void addListener(ILabelProviderListener listener) {
 		}
 
 		@Override
 		public void dispose() {
 		}
 
 		@Override
 		public boolean isLabelProperty(Object element, String property) {
 			return true;
 		}
 
 		@Override
 		public void removeListener(ILabelProviderListener listener) {
 		}
 
 		@Override
 		public Image getColumnImage(Object element, int columnIndex) {
 			if (columnIndex != 0)
 				return null;
 			if (element == null)
 				return null;
 
 			MyData data = (MyData) element;
 			if (data.use)
 				return TICK;
 			return null;
 		}
 
 		@Override
 		public String getColumnText(Object element, int columnIndex) {
 			if (columnIndex == 0)
 				return null;
 			if (element == null)
 				return null;
 
 			MyData data = (MyData) element;
 			if (columnIndex == 1) {
 				return data.name;
 			} else if (columnIndex == 2) {
 				if (data.rois == null)
 					return null;
 				return String.valueOf(data.nrois);
 			}
 
 			IDiffractionMetadata md = data.md;
 			if (md == null)
 				return null;
 
 			if (columnIndex == 3) {
 				DetectorProperties dp = md.getDetector2DProperties();
 				if (dp == null)
 					return null;
 				return String.format("%.2f", dp.getDetectorDistance());
 			} else if (columnIndex == 4) {
 				DiffractionCrystalEnvironment ce = md.getDiffractionCrystalEnvironment();
 				if (ce == null)
 					return null;
 				return String.format("%.3f", ce.getWavelength());
 			}
 			return null;
 		}
 	}
 
 	class MyEditingSupport extends EditingSupport {
 		private TableViewer tv;
 
 		public MyEditingSupport(TableViewer viewer) {
 			super(viewer);
 			tv = viewer;
 		}
 
 		@Override
 		protected CellEditor getCellEditor(Object element) {
 			return new CheckboxCellEditor(null, SWT.CHECK);
 		}
 
 		@Override
 		protected boolean canEdit(Object element) {
 			return true;
 		}
 
 		@Override
 		protected Object getValue(Object element) {
 			MyData data = (MyData) element;
 			return data.use;
 		}
 
 		@Override
 		protected void setValue(Object element, Object value) {
 			MyData data = (MyData) element;
 			data.use = (Boolean) value;
 			tv.refresh();
 
 			setCalibrateButtons();
 		}
 		
 	}
 
 	private void createColumns(TableViewer tv) {
 		TableViewerColumn tvc = new TableViewerColumn(tv, SWT.NONE);
 		tvc.setEditingSupport(new MyEditingSupport(tv));
 		TableColumn tc = tvc.getColumn();
 		tc.setText("Use");
 		tc.setWidth(40);
 
 		String[] headings = { "Image", "#rings", "Distance", "Wavelength" };
 		for (String h : headings) {
 			tvc = new TableViewerColumn(tv, SWT.NONE);
 			tc = tvc.getColumn();
 			tc.setText(h);
 			tc.setWidth(60);
 		}
 	}
 
 	private String getPathFromTrace(IImageTrace trace) {
 		String path = null;
 		if (trace != null) {
 			path = trace.getData().getName();
 			if (!new File(path).canRead()) {
 				int i = path.lastIndexOf(AbstractFileLoader.FILEPATH_DATASET_SEPARATOR);
 				path = i < 0 ? null : path.substring(0, i);
 			}
 		}
 		return path;
 	}
 
 	private void setData(String path, IPlottingSystem system) {
 		if (path == null) { // cope with PlotView
 			path = getPathFromTrace(getImageTrace(system));
 		}
 
 		MyData data = null;
 		if (path != null) {
 			for (MyData d : model) {
 				if (path.equals(d.path)) {
 					data = d;
 					break;
 				}
 			}
 		}
 		if (system == currentSystem && data == currentData)
 			return;
 
 		if (currentSystem != null && currentData != null) {
 			DiffractionImageAugmenter aug = currentData.augmenter;
 			if (aug != null)
 				aug.deactivate();
 		}
 		currentSystem = system;
 		if (currentSystem == null)
 			return;
 
 		if (data == null) {
 			data = new MyData();
 			model.add(data);
 			if (path != null && new File(path).canRead()) {
 				data.path = path;
 				data.name = path.substring(path.lastIndexOf(File.separatorChar) + 1);
 			}
 		}
 		currentData = data;
 		data.system = currentSystem;
 
 		Listener listener = data.listener;
 		if (listener == null) {
 			listener = listeners.get(currentSystem);
 			if (listener == null) {
 				listener = new Listener(data);
 				data.listener = listener;
 				currentSystem.addTraceListener(listener);
 				listeners.put(currentSystem, listener);
 			}
 		} else {
 			listener.setData(data);
 		}
 
 		if (data.augmenter != null) {
 			data.augmenter.activate();
 			drawCalibrantRings();
 		}
 		refreshTable();
 		// highlight current image
 		tableViewer.setSelection(new StructuredSelection(data), true);
 	}
 
 	private void refreshTable() {
 		tableViewer.refresh();
 		for (TableColumn c : tableViewer.getTable().getColumns()) {
 			c.pack();
 		}
 		tableViewer.getControl().getParent().layout();
 	}
 
 	private void setCalibrateButtons() {
 		// enable/disable calibrate button according to use column
 		int used = 0;
 		for (MyData d : model) {
 			if (d.use) {
 				used++;
 			}
 		}
 		calibrateImages.setEnabled(used > 0);
 		calibrateWD.setEnabled(used > 2);
 	}
 
 	private void changeRings(ManipulateMode mode, boolean fast) {
 		if (currentSystem == null || currentData == null || currentData.md == null)
 			return;
 
 		DetectorProperties detprop = currentData.md.getDetector2DProperties();
 		if (detprop == null)
 			return;
 
 		if (mode == ManipulateMode.UP) {
 			Vector3d orig = detprop.getOrigin();
 			Vector3d col = detprop.getPixelColumn();
 			if (fast)
 				col.scale(10);
 			orig.add(col);
 		} else if (mode == ManipulateMode.DOWN) {
 			Vector3d orig = detprop.getOrigin();
 			Vector3d col = detprop.getPixelColumn();
 			if (fast)
 				col.scale(10);
 			orig.sub(col);
 		} else if (mode == ManipulateMode.LEFT) {
 			Vector3d orig = detprop.getOrigin();
 			Vector3d row = detprop.getPixelRow();
 			if (fast)
 				row.scale(10);
 			orig.add(row);
 		} else if (mode == ManipulateMode.RIGHT) {
 			Vector3d orig = detprop.getOrigin();
 			Vector3d row = detprop.getPixelRow();
 			if (fast)
 				row.scale(10);
 			orig.sub(row);
 		} else if (mode == ManipulateMode.ENLARGE) {
 			Vector3d norm = new Vector3d(detprop.getNormal());
 			norm.scale((fast ? 15 : 1)*detprop.getHPxSize());
 			double[] bc = detprop.getBeamCentreCoords();
 			Vector3d orig = detprop.getOrigin();
 			orig.sub(norm);
 			if (!Double.isNaN(bc[0])) { // fix on beam centre
 				detprop.setBeamCentreCoords(bc);
 			}
 		} else if (mode == ManipulateMode.SHRINK) {
 			Vector3d norm = new Vector3d(detprop.getNormal());
 			norm.scale((fast ? 15 : 1)*detprop.getHPxSize());
 			double[] bc = detprop.getBeamCentreCoords();
 			Vector3d orig = detprop.getOrigin();
 			orig.add(norm);
 			if (!Double.isNaN(bc[0])) { // fix on beam centre
 				detprop.setBeamCentreCoords(bc);
 			}
 		} else if (mode == ManipulateMode.ELONGATE) {
 			double tilt = Math.toDegrees(detprop.getTiltAngle());
 			double[] angle = detprop.getNormalAnglesInDegrees();
 			tilt += fast ? 2 : 0.2;
 			if (tilt > 90)
 				tilt = 90;
 			detprop.setNormalAnglesInDegrees(tilt, 0, angle[2]);
 			System.err.println("p: " + tilt);
 		} else if (mode == ManipulateMode.SQUASH) {
 			double tilt = Math.toDegrees(detprop.getTiltAngle());
 			double[] angle = detprop.getNormalAnglesInDegrees();
 			tilt -= fast ? 2 : 0.2;
 			if (tilt < 0)
 				tilt = 0;
 			detprop.setNormalAnglesInDegrees(tilt, 0, angle[2]);
 			System.err.println("o: " + tilt);
 		} else if (mode == ManipulateMode.ANTICLOCKWISE) {
 			double[] angle = detprop.getNormalAnglesInDegrees();
 			angle[2] -= fast ? 2 : 0.5;
 			if (angle[2] < 0)
 				angle[2] += 360;
 			detprop.setNormalAnglesInDegrees(angle[0], angle[1], angle[2]);
 			System.err.println("a: " + angle[2]);
 		} else if (mode == ManipulateMode.CLOCKWISE) {
 			double[] angle = detprop.getNormalAnglesInDegrees();
 			angle[2] += fast ? 2 : 0.5;
 			if (angle[2] > 360)
 				angle[2] = 360;
 			detprop.setNormalAnglesInDegrees(angle[0], angle[1], angle[2]);
 			System.err.println("c: " + angle[2]);
 		}
 		drawCalibrantRings();
 	}
 
 	private void removePlottingSystem(IPlottingSystem system) {
 		Listener listener = listeners.remove(system);
 		if (listener != null)
 			system.removeTraceListener(listener);
 
 		for (MyData d : model) {
 			if (d.system == system) {
 				d.system = null;
 				d.augmenter = null;
 			}
 		}
 		refreshTable();
 	}
 
 	@Override
 	public void dispose() {
 		super.dispose();
 		getSite().getPage().removePartListener(listener);
 	}
 
 	@Override
 	public void setFocus() {
 		sComp.getContent().setFocus();
 		// this should be done with a listener...
 		tableViewer.refresh();
 		drawCalibrantRings();
 	}
 }
 
 
 class SlowFastRunnable implements Runnable {
 	private boolean fast = false;
 
 	public void setFast(boolean fast) {
 		this.fast = fast;
 	}
 
 	public boolean isFast() {
 		return fast;
 	}
 
 	@Override
 	public void run() {
 	}
 
 	public void stop() {
 	}
 }
 
 class RepeatingMouseAdapter extends MouseAdapter {
 	private final EventRepeater repeater;
 
 	public RepeatingMouseAdapter(Display display, SlowFastRunnable task) {
 		repeater = new EventRepeater(display, task);
 	}
 
 	@Override
 	public void mouseDown(MouseEvent e) {
 		repeater.setStarter(e);
 		repeater.run();
 	}
 
 	@Override
 	public void mouseUp(MouseEvent e) {
 		repeater.stop();
 	}
 }
 
 class EventRepeater implements Runnable {
 	boolean stop;
 	Display display;
 	static int slow = 200;
 	static int mid = 40;
 	static int fast = 8;
 	static int[] threshold = {4, 8};
 	int[] count;
 	private SlowFastRunnable task;
 
 	public EventRepeater(Display display, SlowFastRunnable task) {
 		this.display = display;
 		this.task = task;
 		stop = true;
 	}
 
 	MouseEvent first;
 	public void setStarter(MouseEvent me) {
 		first = me;
 		stop = false;
 		count = threshold.clone();
 		task.setFast(false);
 	}
 
 	@Override
 	public void run() {
 		if (!stop) {
 			task.run();
 //			System.out.printf(".");
 			if (count[0] >= 0) {
 				count[0]--;
 				display.timerExec(slow, this);
 			} else if (count[1] >= 0) {
 				count[1]--;
 				display.timerExec(mid, this);
 			} else {
 				task.setFast(true);
 				display.timerExec(fast, this);
 			}
 		}
 	}
 
 	public void stop() {
 		stop = true;
 		task.stop();
 	}
 }
